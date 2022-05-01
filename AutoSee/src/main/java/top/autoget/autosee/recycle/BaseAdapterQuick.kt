package top.autoget.autosee.recycle

import android.animation.Animator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.annotation.IntRange
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import top.autoget.autosee.recycle.animation.*
import top.autoget.autosee.recycle.common.ViewHolderBase
import top.autoget.autosee.recycle.common.getItemView
import top.autoget.autosee.recycle.diff.DifferAsync
import top.autoget.autosee.recycle.diff.DifferAsyncConfig
import top.autoget.autosee.recycle.diff.ListUpdateCallback
import top.autoget.autosee.recycle.listener.*
import top.autoget.autosee.recycle.module.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

private interface BaseAdapterQuickModule {
    fun addLoadMoreModule(baseQuickAdapter: BaseAdapterQuick<*, *>): LoadMoreModuleBase =
        LoadMoreModuleBase(baseQuickAdapter)

    fun addUpFetchModule(baseQuickAdapter: BaseAdapterQuick<*, *>): UpFetchModuleBase =
        UpFetchModuleBase(baseQuickAdapter)

    fun addDraggableModule(baseQuickAdapter: BaseAdapterQuick<*, *>): DraggableModuleBase =
        DraggableModuleBase(baseQuickAdapter)
}//获取模块，重写可自定义

abstract class BaseAdapterQuick<T, VH : ViewHolderBase>
@JvmOverloads constructor(@LayoutRes private val layoutResId: Int, data: MutableList<T>? = null) :
    RecyclerView.Adapter<VH>(), BaseAdapterQuickModule, BaseListener {
    var data: MutableList<T> = data ?: mutableListOf()
        internal set//数据只允许get
    internal var mLoadMoreModule: LoadMoreModuleBase? = null
    val loadMoreModule: LoadMoreModuleBase
        get() = checkNotNull(mLoadMoreModule) { "Please first implements LoadMoreModule" }//加载更多模块
    private var mUpFetchModule: UpFetchModuleBase? = null
    val upFetchModule: UpFetchModuleBase
        get() = checkNotNull(mUpFetchModule) { "Please first implements UpFetchModule" }//向上加载模块
    private var mDraggableModule: DraggableModuleBase? = null
    val draggableModule: DraggableModuleBase
        get() = checkNotNull(mDraggableModule) { "Please first implements DraggableModule" }//拖拽模块
    private val checkModule = {
        if (this is LoadMoreModule) mLoadMoreModule = addLoadMoreModule(this)
        if (this is UpFetchModule) mUpFetchModule = addUpFetchModule(this)
        if (this is DraggableModule) mDraggableModule = addDraggableModule(this)
    }

    init {
        checkModule
    }

    protected abstract fun convert(holder: VH, item: T)
    protected open fun convert(holder: VH, item: T, payloads: List<Any>) {}

    companion object {
        const val HEADER_VIEW = 0x10000111
        const val LOAD_MORE_VIEW = 0x10000222
        const val FOOTER_VIEW = 0x10000333
        const val EMPTY_VIEW = 0x10000555
    }

    private lateinit var mHeaderLayout: LinearLayout
    private lateinit var mFooterLayout: LinearLayout
    private lateinit var mEmptyLayout: FrameLayout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = when (viewType) {
        HEADER_VIEW -> {
            mHeaderLayout.parent.apply { if (this is ViewGroup) removeView(mHeaderLayout) }
            createBaseViewHolder(mHeaderLayout)
        }
        LOAD_MORE_VIEW -> (mLoadMoreModule ?: addLoadMoreModule(this))
            .loadMoreView.getRootView(parent).let {
                createBaseViewHolder(it).apply { mLoadMoreModule?.setupViewHolder(this) }
            }
        FOOTER_VIEW -> {
            mFooterLayout.parent.apply { if (this is ViewGroup) removeView(mFooterLayout) }
            createBaseViewHolder(mFooterLayout)
        }
        EMPTY_VIEW -> {
            mEmptyLayout.parent?.apply { if (this is ViewGroup) removeView(mEmptyLayout) }
            createBaseViewHolder(mEmptyLayout)
        }
        else -> onCreateDefViewHolder(parent, viewType).apply {
            bindViewClickListener(this, viewType)
            mDraggableModule?.initView(this)
            onItemViewHolderCreated(this, viewType)
        }
    }

    protected open fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH =
        createBaseViewHolder(parent, layoutResId)

    protected open fun createBaseViewHolder(parent: ViewGroup, @LayoutRes layoutResId: Int): VH =
        createBaseViewHolder(parent.getItemView(layoutResId))

    protected open fun createBaseViewHolder(view: View): VH {
        var temp: Class<*>? = javaClass
        var z: Class<*>? = null
        while (temp != null && z == null) {
            z = getInstancedGenericKClass(temp)
            temp = temp.superclass
        }
        return when (z) {
            null -> ViewHolderBase(view) as VH
            else -> createBaseGenericKInstance(z, view)
        } ?: ViewHolderBase(view) as VH//泛型擦除会导致z为null
    }//创建ViewHolder可重写

    private fun getInstancedGenericKClass(z: Class<*>): Class<*>? {
        try {
            val type = z.genericSuperclass
            if (type is ParameterizedType) for (temp in type.actualTypeArguments) {
                when (temp) {
                    is Class<*> -> if (ViewHolderBase::class.java.isAssignableFrom(temp)) return temp
                    is ParameterizedType -> {
                        val rawType = temp.rawType
                        if (rawType is Class<*> && ViewHolderBase::class.java
                                .isAssignableFrom(rawType)
                        ) return rawType
                    }
                }
            }
        } catch (e: java.lang.reflect.GenericSignatureFormatError) {
            e.printStackTrace()
        } catch (e: TypeNotPresentException) {
            e.printStackTrace()
        } catch (e: java.lang.reflect.MalformedParameterizedTypeException) {
            e.printStackTrace()
        }
        return null
    }

    private fun createBaseGenericKInstance(z: Class<*>, view: View): VH? {
        try {
            return when {
                z.isMemberClass && !Modifier.isStatic(z.modifiers) ->
                    z.getDeclaredConstructor(javaClass, View::class.java)
                        .apply { isAccessible = true }.newInstance(this, view) as VH
                else -> z.getDeclaredConstructor(View::class.java)
                    .apply { isAccessible = true }.newInstance(view) as VH
            }//Constructor<*>
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
        return null
    }

    protected open fun onItemViewHolderCreated(viewHolder: VH, viewType: Int) {}//对ViewHolder进行处理
    val headerLayoutCount: Int
        get() = if (hasHeaderLayout) 1 else 0

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        when {
            payloads.isEmpty() -> onBindViewHolder(holder, position)
            else -> {
                mLoadMoreModule?.autoLoadMore(position)
                mUpFetchModule?.autoUpFetch(position)
                when (holder.itemViewType) {
                    LOAD_MORE_VIEW -> mLoadMoreModule?.let {
                        it.loadMoreView.convert(holder, position, it.loadMoreStatus)
                    }
                    HEADER_VIEW, FOOTER_VIEW, EMPTY_VIEW -> return
                    else -> convert(holder, getItem(position - headerLayoutCount), payloads)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        mLoadMoreModule?.autoLoadMore(position)
        mUpFetchModule?.autoUpFetch(position)
        when (holder.itemViewType) {
            LOAD_MORE_VIEW -> mLoadMoreModule?.let {
                it.loadMoreView.convert(holder, position, it.loadMoreStatus)
            }
            HEADER_VIEW, FOOTER_VIEW, EMPTY_VIEW -> return
            else -> convert(holder, getItem(position - headerLayoutCount))
        }
    }

    open fun getItem(@IntRange(from = 0) position: Int): T = data[position]
    open fun getItemOrNull(@IntRange(from = 0) position: Int): T? = data.getOrNull(position)
    open fun getItemPosition(item: T?): Int =
        if (item != null && data.isNotEmpty()) data.indexOf(item) else -1//返回-1表示不存在

    override fun onViewAttachedToWindow(holder: VH) {
        super.onViewAttachedToWindow(holder)
        if (isFixedViewType(holder.itemViewType)) setFullSpan(holder) else addAnimation(holder)
    }

    protected open fun isFixedViewType(type: Int): Boolean =
        type == EMPTY_VIEW || type == HEADER_VIEW || type == FOOTER_VIEW || type == LOAD_MORE_VIEW

    protected open fun setFullSpan(holder: RecyclerView.ViewHolder) = holder.itemView.layoutParams
        .apply { if (this is StaggeredGridLayoutManager.LayoutParams) isFullSpan = true }

    var animationEnable: Boolean = false//是否打开动画
    var adapterAnimation: AnimationBase? = null
        set(adapterAnimation) {
            animationEnable = true
            field = adapterAnimation
        }//设置自定义动画

    enum class AnimationType { AlphaIn, ScaleIn, SlideInBottom, SlideInLeft, SlideInRight }

    fun setAnimationWithDefault(animationType: AnimationType) {
        adapterAnimation = when (animationType) {
            AnimationType.AlphaIn -> AnimationAlphaIn()
            AnimationType.ScaleIn -> AnimationScaleIn()
            AnimationType.SlideInBottom -> AnimationSlideInBottom()
            AnimationType.SlideInLeft -> AnimationSlideInLeft()
            AnimationType.SlideInRight -> AnimationSlideInRight()
        }
    }//使用内置默认动画设置

    var isAnimationFirstOnly = true//动画是否仅第一次执行
    private var mLastPosition = -1
    private fun addAnimation(holder: RecyclerView.ViewHolder) {
        if (animationEnable) {
            if (!isAnimationFirstOnly || holder.layoutPosition > mLastPosition) {
                val animation: AnimationBase = adapterAnimation ?: AnimationAlphaIn()
                animation.animators(holder.itemView).forEach {
                    startAnim(it, holder.layoutPosition)
                }
                mLastPosition = holder.layoutPosition
            }
        }
    }

    protected open fun startAnim(anim: Animator, index: Int) = anim.start()
    internal var mRecyclerView: RecyclerView? = null
    var recyclerView: RecyclerView
        get() = checkNotNull(mRecyclerView) { "Please get it after onAttachedToRecyclerView()" }
        set(recyclerView) {
            mRecyclerView = recyclerView
        }
    val context: Context
        get() = recyclerView.context
    var headerViewAsFlow: Boolean = false//为true页眉/页脚排列方式与普通项目视图相同，仅GridLayoutManager时有效忽略跨距大小
    var footerViewAsFlow: Boolean = false
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
        mDraggableModule?.attachToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            val defSpanSizeLookup = manager.spanSizeLookup
            manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val type = getItemViewType(position)
                    return when {
                        type == HEADER_VIEW && headerViewAsFlow -> 1
                        type == FOOTER_VIEW && footerViewAsFlow -> 1
                        else -> mSpanSizeLookup?.let {
                            when {
                                isFixedViewType(type) -> manager.spanCount
                                else -> it.getSpanSize(manager, type, position - headerLayoutCount)
                            }
                        } ?: when {
                            isFixedViewType(type) -> manager.spanCount
                            else -> defSpanSizeLookup.getSpanSize(position)
                        }
                    }
                }
            }
        }
    }

    fun getViewByPosition(position: Int, @IdRes viewId: Int): View? =
        (mRecyclerView?.findViewHolderForLayoutPosition(position) as ViewHolderBase?)?.getViewOrNull(
            viewId
        )

    var isUseEmpty = true//是否使用空布局
    val hasEmptyView: Boolean = when {
        !::mEmptyLayout.isInitialized || mEmptyLayout.childCount == 0 -> false
        !isUseEmpty -> false
        else -> data?.isEmpty() ?: true
    }
    var headerWithEmptyEnable = false//当显示空布局时，是否显示Header
    var footerWithEmptyEnable = false//当显示空布局时，是否显示Foot
    val hasHeaderLayout: Boolean = ::mHeaderLayout.isInitialized && mHeaderLayout.childCount > 0
    val hasFooterLayout: Boolean = ::mFooterLayout.isInitialized && mFooterLayout.childCount > 0
    val footerLayoutCount: Int
        get() = if (hasFooterLayout) 1 else 0

    override fun getItemCount(): Int = when {
        hasEmptyView -> {
            var count = 1
            if (headerWithEmptyEnable && hasHeaderLayout) count++
            if (footerWithEmptyEnable && hasFooterLayout) count++
            count
        }
        else -> headerLayoutCount + getDefItemCount() + footerLayoutCount +
                if (mLoadMoreModule?.hasLoadMoreView == true) 1 else 0
    }//不要重写，需要可重写getDefItemCount

    protected open fun getDefItemCount(): Int = data.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int): Int = when {
        hasEmptyView -> (headerWithEmptyEnable && hasHeaderLayout).let {
            when (position) {
                0 -> if (it) HEADER_VIEW else EMPTY_VIEW
                1 -> if (it) EMPTY_VIEW else FOOTER_VIEW
                2 -> FOOTER_VIEW
                else -> EMPTY_VIEW
            }
        }
        else -> when {
            hasHeaderLayout && position == 0 -> HEADER_VIEW
            else -> {
                val adjPosition = if (hasHeaderLayout) position - 1 else position
                val dataSize = data.size
                when {
                    adjPosition < dataSize -> getDefItemViewType(adjPosition)
                    else -> when {
                        adjPosition - dataSize < if (hasFooterLayout) 1 else 0 -> FOOTER_VIEW
                        else -> LOAD_MORE_VIEW
                    }
                }
            }
        }
    }//不要重写，需要可重写getDefItemViewType

    protected open fun getDefItemViewType(position: Int): Int = super.getItemViewType(position)
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mRecyclerView = null
    }

    private var mSpanSizeLookup: GridSpanSizeLookup? = null
    override fun setGridSpanSizeLookup(spanSizeLookup: GridSpanSizeLookup?) {
        this.mSpanSizeLookup = spanSizeLookup
    }

    private var mOnItemClickListener: OnItemClickListener? = null
    val onItemClickListener: OnItemClickListener? = mOnItemClickListener
    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.mOnItemClickListener = listener
    }

    private var mOnItemLongClickListener: OnItemLongClickListener? = null
    val onItemLongClickListener: OnItemLongClickListener? = mOnItemLongClickListener
    override fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        this.mOnItemLongClickListener = listener
    }

    private var mOnItemChildClickListener: OnItemChildClickListener? = null
    val onItemChildClickListener: OnItemChildClickListener? = mOnItemChildClickListener
    override fun setOnItemChildClickListener(listener: OnItemChildClickListener?) {
        this.mOnItemChildClickListener = listener
    }

    private var mOnItemChildLongClickListener: OnItemChildLongClickListener? = null
    val onItemChildLongClickListener: OnItemChildLongClickListener? = mOnItemChildLongClickListener
    override fun setOnItemChildLongClickListener(listener: OnItemChildLongClickListener?) {
        this.mOnItemChildLongClickListener = listener
    }

    val childClickViewIds = LinkedHashSet<Int>()
    fun addChildClickViewIds(@IdRes vararg viewIds: Int) {
        for (viewId in viewIds) {
            childClickViewIds.add(viewId)
        }
    }

    val childLongClickViewIds = LinkedHashSet<Int>()
    fun addChildLongClickViewIds(@IdRes vararg viewIds: Int) {
        for (viewId in viewIds) {
            childLongClickViewIds.add(viewId)
        }
    }

    protected open fun bindViewClickListener(viewHolder: VH, viewType: Int) {
        mOnItemClickListener?.let {
            viewHolder.itemView.setOnClickListener { v ->
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION)
                    setOnItemClick(v, position - headerLayoutCount)
            }
        }
        mOnItemLongClickListener?.let {
            viewHolder.itemView.setOnLongClickListener { v ->
                when (val position = viewHolder.adapterPosition) {
                    RecyclerView.NO_POSITION -> return@setOnLongClickListener false
                    else -> setOnItemLongClick(v, position - headerLayoutCount)
                }
            }
        }
        mOnItemChildClickListener?.let {
            for (id in childClickViewIds) {
                viewHolder.itemView.findViewById<View>(id)?.apply {
                    if (!isClickable) isClickable = true
                    setOnClickListener { v ->
                        val position = viewHolder.adapterPosition
                        if (position != RecyclerView.NO_POSITION)
                            setOnItemChildClick(v, position - headerLayoutCount)
                    }
                }
            }
        }
        mOnItemChildLongClickListener?.let {
            for (id in childLongClickViewIds) {
                viewHolder.itemView.findViewById<View>(id)?.apply {
                    if (!isLongClickable) isLongClickable = true
                    setOnLongClickListener { v ->
                        when (val position = viewHolder.adapterPosition) {
                            RecyclerView.NO_POSITION -> return@setOnLongClickListener false
                            else -> setOnItemChildLongClick(v, position - headerLayoutCount)
                        }
                    }
                }
            }
        }
    }//绑定item点击事件

    protected open fun setOnItemClick(v: View, position: Int) =
        mOnItemClickListener?.onItemClick(this, v, position)

    protected open fun setOnItemLongClick(v: View, position: Int): Boolean =
        mOnItemLongClickListener?.onItemLongClick(this, v, position) ?: false

    protected open fun setOnItemChildClick(v: View, position: Int) =
        mOnItemChildClickListener?.onItemChildClick(this, v, position)

    protected open fun setOnItemChildLongClick(v: View, position: Int): Boolean =
        mOnItemChildLongClickListener?.onItemChildLongClick(this, v, position) ?: false

    val headerLayout: LinearLayout?
        get() = if (this::mHeaderLayout.isInitialized) mHeaderLayout else null

    @JvmOverloads
    fun setHeaderView(view: View, index: Int = 0, orientation: Int = LinearLayout.VERTICAL): Int =
        when {
            this::mHeaderLayout.isInitialized && mHeaderLayout.childCount > index -> {
                mHeaderLayout.removeViewAt(index)
                mHeaderLayout.addView(view, index)
                index
            }
            else -> addHeaderView(view, index, orientation)
        }

    val headerViewPosition: Int
        get() {
            when {
                hasEmptyView -> if (headerWithEmptyEnable) return 0
                else -> return 0
            }
            return -1
        }

    @JvmOverloads
    fun addHeaderView(view: View, index: Int = -1, orientation: Int = LinearLayout.VERTICAL): Int {
        if (!this::mHeaderLayout.isInitialized) mHeaderLayout = LinearLayout(view.context).apply {
            this.orientation = orientation
            layoutParams = when (orientation) {
                LinearLayout.VERTICAL -> RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                else -> RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            }
        }
        val childCount = mHeaderLayout.childCount
        return (if (index < 0 || index > childCount) childCount else index).apply {
            mHeaderLayout.addView(view, this)
            if (mHeaderLayout.childCount == 1) {
                val position = headerViewPosition
                if (position != -1) notifyItemInserted(position)
            }
        }
    }

    fun removeHeaderView(header: View) {
        if (hasHeaderLayout) {
            mHeaderLayout.removeView(header)
            if (mHeaderLayout.childCount == 0) {
                val position = headerViewPosition
                if (position != -1) notifyItemRemoved(position)
            }
        }
    }

    val removeAllHeaderView = {
        if (hasHeaderLayout) {
            mHeaderLayout.removeAllViews()
            val position = headerViewPosition
            if (position != -1) notifyItemRemoved(position)
        }
    }
    val footerLayout: LinearLayout?
        get() = if (this::mFooterLayout.isInitialized) mFooterLayout else null

    @JvmOverloads
    fun setFooterView(view: View, index: Int = 0, orientation: Int = LinearLayout.VERTICAL): Int =
        when {
            this::mFooterLayout.isInitialized && mFooterLayout.childCount > index -> {
                mFooterLayout.removeViewAt(index)
                mFooterLayout.addView(view, index)
                index
            }
            else -> addFooterView(view, index, orientation)
        }

    val footerViewPosition: Int
        get() {
            when {
                hasEmptyView -> if (footerWithEmptyEnable)
                    return if (headerWithEmptyEnable && hasHeaderLayout) 2 else 1
                else -> return headerLayoutCount + data.size
            }
            return -1
        }

    @JvmOverloads
    fun addFooterView(view: View, index: Int = -1, orientation: Int = LinearLayout.VERTICAL): Int {
        if (!this::mFooterLayout.isInitialized) mFooterLayout = LinearLayout(view.context).apply {
            this.orientation = orientation
            layoutParams = when (orientation) {
                LinearLayout.VERTICAL -> RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                else -> RecyclerView.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
            }
        }
        val childCount = mFooterLayout.childCount
        return (if (index < 0 || index > childCount) childCount else index).apply {
            mFooterLayout.addView(view, this)
            if (mFooterLayout.childCount == 1) {
                val position = footerViewPosition
                if (position != -1) notifyItemInserted(position)
            }
        }
    }

    fun removeFooterView(footer: View) {
        if (hasFooterLayout) {
            mFooterLayout.removeView(footer)
            if (mFooterLayout.childCount == 0) {
                val position = footerViewPosition
                if (position != -1) notifyItemRemoved(position)
            }
        }
    }

    val removeAllFooterView = {
        if (hasFooterLayout) {
            mFooterLayout.removeAllViews()
            val position = footerViewPosition
            if (position != -1) notifyItemRemoved(position)
        }
    }
    val emptyLayout: FrameLayout?
        get() = if (this::mEmptyLayout.isInitialized) mEmptyLayout else null

    fun setEmptyView(layoutResId: Int) = mRecyclerView?.let {
        setEmptyView(LayoutInflater.from(it.context).inflate(layoutResId, it, false))
    }

    fun setEmptyView(emptyView: View) {
        val oldItemCount = itemCount
        var insert = false
        when {
            this::mEmptyLayout.isInitialized -> emptyView.layoutParams?.let {
                mEmptyLayout.layoutParams.apply {
                    width = it.width
                    height = it.height
                }
            }
            else -> {
                mEmptyLayout = FrameLayout(emptyView.context).apply {
                    layoutParams = emptyView.layoutParams?.let {
                        ViewGroup.LayoutParams(it.width, it.height)
                    } ?: ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
                insert = true
            }
        }
        mEmptyLayout.removeAllViews()
        mEmptyLayout.addView(emptyView)
        isUseEmpty = true
        if (insert && hasEmptyView) when {
            itemCount > oldItemCount ->
                notifyItemInserted(if (headerWithEmptyEnable && hasHeaderLayout) 1 else 0)
            else -> notifyDataSetChanged()
        }
    }//设置空布局视图，data必须为空数组

    val removeEmptyView = {
        if (this::mEmptyLayout.isInitialized) mEmptyLayout.removeAllViews()
    }

    fun setDiffCallback(diffCallback: DiffUtil.ItemCallback<T>) =
        setDiffConfig(
            DifferAsyncConfig.Builder(diffCallback).build()
        )//设置Diff Callback，快速生成Diff Config。

    private var differAsync: DifferAsync<T>? = null
    val differ: DifferAsync<T> =
        checkNotNull(differAsync) { "Please use setDiffCallback() or setDiffConfig() first!" }

    fun setDiffConfig(config: DifferAsyncConfig<T>) {
        differAsync = DifferAsync(this, config)
    }//需自定义线程用，用setDiffNewData前必须用

    @JvmOverloads
    open fun setDiffNewData(list: MutableList<T>?, commitCallback: Runnable? = null) = when {
        hasEmptyView -> {
            setNewInstance(list)
            commitCallback?.run()
        }
        else -> differAsync?.submitList(list, commitCallback)
    }
    //使用Diff设置新实例，异步Diff无需考虑性能问题，使用之前设置setDiffCallback或setDiffConfig

    open fun setDiffNewData(@NonNull diffResult: DiffUtil.DiffResult, list: MutableList<T>) {
        when {
            hasEmptyView -> setNewInstance(list)
            else -> {
                diffResult.dispatchUpdatesTo(ListUpdateCallback(this))
                this.data = list
            }
        }
    }//使用DiffResult设置新实例

    open fun setNewInstance(list: MutableList<T>?) {
        if (list !== this.data) {
            this.data = list ?: mutableListOf()
            mLoadMoreModule?.reset
            mLastPosition = -1
            notifyDataSetChanged()
            mLoadMoreModule?.checkDisableLoadMoreIfNotFullPage
        }
    }//设置新数据实例，替换原有内存引用，用setList修改内容

    open fun setList(list: Collection<T>?) {
        when {
            list === this.data -> when {
                list.isNullOrEmpty() -> this.data.clear()
                else -> this.data.apply { clear() }.addAll(ArrayList(list))
            }
            else -> {
                this.data.clear()
                if (!list.isNullOrEmpty()) this.data.addAll(list)
            }
        }
        mLoadMoreModule?.reset
        mLastPosition = -1
        notifyDataSetChanged()
        mLoadMoreModule?.checkDisableLoadMoreIfNotFullPage
    }//使用新数据集合，改变原数据集合，不替换原内存引用只替换内容

    open fun setData(@IntRange(from = 0) index: Int, data: T) {
        if (index < this.data.size) {
            this.data[index] = data
            notifyItemChanged(index + headerLayoutCount)
        }
    }//改变某一位置数据

    protected fun compatibilityDataSizeChanged(size: Int) {
        if (this.data.size == size) notifyDataSetChanged()
    }

    open fun addData(@NonNull data: T) {
        this.data.add(data)
        notifyItemInserted(this.data.size + headerLayoutCount)
        compatibilityDataSizeChanged(1)
    }//添加一条新数据

    open fun addData(@IntRange(from = 0) position: Int, data: T) {
        this.data.add(position, data)
        notifyItemInserted(position + headerLayoutCount)
        compatibilityDataSizeChanged(1)
    }//在指定位置添加一条新数据

    open fun addData(@NonNull newData: Collection<T>) {
        this.data.addAll(newData)
        notifyItemRangeInserted(this.data.size - newData.size + headerLayoutCount, newData.size)
        compatibilityDataSizeChanged(newData.size)
    }//添加数据

    open fun addData(@IntRange(from = 0) position: Int, newData: Collection<T>) {
        this.data.addAll(position, newData)
        notifyItemRangeInserted(position + headerLayoutCount, newData.size)
        compatibilityDataSizeChanged(newData.size)
    }//在指定位置添加数据

    open fun removeAt(@IntRange(from = 0) position: Int) {
        if (position < data.size) {
            this.data.removeAt(position)
            val internalPosition = position + headerLayoutCount
            notifyItemRemoved(internalPosition)
            compatibilityDataSizeChanged(0)
            notifyItemRangeChanged(internalPosition, this.data.size - internalPosition)
        }
    }//在指定位置删除一条旧数据

    open fun remove(data: T) {
        val index = this.data.indexOf(data)
        if (index != -1) removeAt(index)
    }
}