package top.autoget.autosee.recycle.module

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import top.autoget.autosee.recycle.BaseAdapterQuick
import top.autoget.autosee.recycle.common.ViewHolderBase
import kotlin.math.max

class PageInfo {
    var page = 0
    val nextPage = page++//每次加载成功
    val reset = { page = 0 }//刷新重置
    val isFirstPage: Boolean
        get() = page == 0//首页setList非首页addData
}//加载更多翻页信息

interface LoadMoreModule//需要向下加载更多BaseQuickAdapter继承
object LoadMoreModuleConfig {
    @JvmStatic
    var defLoadMoreView: LoadMoreViewBase = LoadMoreViewSimple()//设置全局LodeMoreView
}

open class LoadMoreModuleBase(private val baseQuickAdapter: BaseAdapterQuick<*, *>) :
    LoadMoreListener {
    var loadMoreView = LoadMoreModuleConfig.defLoadMoreView//设置加载更多布局
    private var mLoadMoreListener: OnLoadMoreListener? = null
    var loadMoreStatus = LoadMoreStatus.Complete
        private set
    val isLoading: Boolean
        get() = loadMoreStatus == LoadMoreStatus.Loading
    var isEnableLoadMore = false
        set(isEnableLoadMore) {
            val oldHasLoadMore = hasLoadMoreView
            field = isEnableLoadMore
            val newHasLoadMore = hasLoadMoreView
            when {
                oldHasLoadMore -> if (!newHasLoadMore) {
                    baseQuickAdapter.notifyItemRemoved(loadMoreViewPosition)
                }
                else -> if (newHasLoadMore) {
                    loadMoreStatus = LoadMoreStatus.Complete
                    baseQuickAdapter.notifyItemInserted(loadMoreViewPosition)
                }
            }
        }
    var isLoadEndMoreGone: Boolean = false
        private set
    val hasLoadMoreView: Boolean = when {
        mLoadMoreListener == null || !isEnableLoadMore -> false
        loadMoreStatus == LoadMoreStatus.End && isLoadEndMoreGone -> false
        else -> baseQuickAdapter.data.isNotEmpty()
    }
    internal val reset = mLoadMoreListener?.let {
        isEnableLoadMore = true
        loadMoreStatus = LoadMoreStatus.Complete
    }

    override fun setOnLoadMoreListener(listener: OnLoadMoreListener?) {
        mLoadMoreListener = listener
        isEnableLoadMore = true
    }

    private val loadMoreViewPosition: Int
        get() = when {
            baseQuickAdapter.hasEmptyView -> -1
            else -> baseQuickAdapter.let {
                it.headerLayoutCount + it.data.size + it.footerLayoutCount
            }
        }
    private val invokeLoadMoreListener = {
        loadMoreStatus = LoadMoreStatus.Loading
        baseQuickAdapter.mRecyclerView?.post { mLoadMoreListener?.onLoadMore() }
            ?: mLoadMoreListener?.onLoadMore()
    }//触发加载更多监听
    private val loadMoreToLoading = {
        if (loadMoreStatus != LoadMoreStatus.Loading) {
            loadMoreStatus = LoadMoreStatus.Loading
            baseQuickAdapter.notifyItemChanged(loadMoreViewPosition)
            invokeLoadMoreListener
        }
    }
    var enableLoadMoreEndClick = false
    internal fun setupViewHolder(viewHolder: ViewHolderBase) =
        viewHolder.itemView.setOnClickListener {
            when {
                loadMoreStatus == LoadMoreStatus.Complete -> loadMoreToLoading
                loadMoreStatus == LoadMoreStatus.Fail -> loadMoreToLoading
                loadMoreStatus == LoadMoreStatus.End && enableLoadMoreEndClick -> loadMoreToLoading
            }
        }

    var isAutoLoadMore = true
    private var mNextLoadEnable = true//不满一屏时，是否可以继续加载的标记位
    var preLoadNumber = 1
        set(preLoadNumber) {
            field = max(1, preLoadNumber)
        }

    internal fun autoLoadMore(position: Int) {
        if (loadMoreStatus == LoadMoreStatus.Complete && loadMoreStatus != LoadMoreStatus.Loading &&
            hasLoadMoreView && isAutoLoadMore && mNextLoadEnable &&
            position >= baseQuickAdapter.itemCount - preLoadNumber
        ) invokeLoadMoreListener
    }

    var isEnableLoadMoreIfNotFullPage = true//当自动加载开启，同时数据不满一屏时，是否继续执行自动加载更多
    val checkDisableLoadMoreIfNotFullPage = {
        if (!isEnableLoadMoreIfNotFullPage) {
            mNextLoadEnable = false//先把标记位设置为false
            baseQuickAdapter.mRecyclerView?.run {
                layoutManager?.let {
                    when (it) {
                        is LinearLayoutManager -> postDelayed({
                            if (isFullScreen(it)) mNextLoadEnable = true
                        }, 50)
                        is StaggeredGridLayoutManager -> postDelayed({
                            val positions = IntArray(it.spanCount)
                            it.findLastCompletelyVisibleItemPositions(positions)
                            if (getTheBiggestNumber(positions) + 1 != baseQuickAdapter.itemCount)
                                mNextLoadEnable = true
                        }, 50)
                        else -> {
                        }
                    }
                }
            }
        }
    }//用来检查数据是否满一屏，如果满足条件，再开启

    private fun isFullScreen(layoutManager: LinearLayoutManager): Boolean = layoutManager.run {
        findLastCompletelyVisibleItemPosition() + 1 != baseQuickAdapter.itemCount ||
                findFirstCompletelyVisibleItemPosition() != 0
    }

    private fun getTheBiggestNumber(numbers: IntArray?): Int {
        var tmp = -1
        return when {
            numbers == null || numbers.isEmpty() -> tmp
            else -> {
                for (num in numbers) {
                    tmp = max(tmp, num)
                }
                tmp
            }
        }
    }

    val loadMoreFail = {
        if (hasLoadMoreView) {
            loadMoreStatus = LoadMoreStatus.Fail
            baseQuickAdapter.notifyItemChanged(loadMoreViewPosition)
        }
    }
    val loadMoreComplete = {
        if (hasLoadMoreView) {
            loadMoreStatus = LoadMoreStatus.Complete
            baseQuickAdapter.notifyItemChanged(loadMoreViewPosition)
            checkDisableLoadMoreIfNotFullPage
        }
    }

    @JvmOverloads
    fun loadMoreEnd(gone: Boolean = false) {
        if (hasLoadMoreView) {
            isLoadEndMoreGone = gone
            loadMoreStatus = LoadMoreStatus.End
            when {
                gone -> baseQuickAdapter.notifyItemRemoved(loadMoreViewPosition)
                else -> baseQuickAdapter.notifyItemChanged(loadMoreViewPosition)
            }
        }
    }
}