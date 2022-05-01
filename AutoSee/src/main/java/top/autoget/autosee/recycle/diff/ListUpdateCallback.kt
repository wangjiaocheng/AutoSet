package top.autoget.autosee.recycle.diff

import androidx.recyclerview.widget.ListUpdateCallback
import top.autoget.autosee.recycle.BaseAdapterQuick

class ListUpdateCallback(private val mAdapter: BaseAdapterQuick<*, *>) : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) =
        mAdapter.notifyItemRangeInserted(position + mAdapter.headerLayoutCount, count)

    override fun onRemoved(position: Int, count: Int) = when {
        mAdapter.mLoadMoreModule?.hasLoadMoreView == true && mAdapter.itemCount == 0 ->
            mAdapter.notifyItemRangeRemoved(position + mAdapter.headerLayoutCount, count + 1)
        else -> mAdapter.notifyItemRangeRemoved(position + mAdapter.headerLayoutCount, count)
    }//如果注册了加载更多，并且当前itemCount为0，则需要加上loadMore所占用的一行

    override fun onMoved(fromPosition: Int, toPosition: Int) = mAdapter.notifyItemMoved(
        fromPosition + mAdapter.headerLayoutCount, toPosition + mAdapter.headerLayoutCount
    )

    override fun onChanged(position: Int, count: Int, payload: Any?) =
        mAdapter.notifyItemRangeChanged(position + mAdapter.headerLayoutCount, count, payload)
}