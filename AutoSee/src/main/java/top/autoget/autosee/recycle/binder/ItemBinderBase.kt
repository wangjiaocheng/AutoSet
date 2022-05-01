package top.autoget.autosee.recycle.binder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import top.autoget.autosee.recycle.BaseAdapterBinder
import top.autoget.autosee.recycle.common.ViewHolderBase

abstract class ItemBinderBase<T, VH : ViewHolderBase> {
    internal var binderContext: Context? = null
    val context: Context
        get() = checkNotNull(binderContext) {
            """This $this has not been attached to BaseBinderAdapter yet.
                    You should not call the method before onCreateViewHolder()."""
        }
    internal var binderAdapter: BaseAdapterBinder? = null
    val adapter: BaseAdapterBinder
        get() = checkNotNull(binderAdapter) {
            """This $this has not been attached to BaseBinderAdapter yet.
                    You should not call the method before addItemBinder()."""
        }
    val data: MutableList<Any> get() = adapter.data
    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
    abstract fun convert(holder: VH, data: T)//设置item数据
    open fun convert(holder: VH, data: T, payloads: List<Any>) {}//局部刷新调用
    open fun onFailedToRecycleView(holder: VH): Boolean = false
    open fun onViewAttachedToWindow(holder: VH) {}//此[ItemBinderBase]出现在屏幕上时调用
    open fun onViewDetachedFromWindow(holder: VH) {}//此[ItemBinderBase]从屏幕上移除时调用
    open fun onClick(holder: VH, view: View, data: T, position: Int) {}//条目点击
    open fun onLongClick(holder: VH, view: View, data: T, position: Int): Boolean = false//条目长按
    open fun onChildClick(holder: VH, view: View, data: T, position: Int) {}
    open fun onChildLongClick(holder: VH, view: View, data: T, position: Int): Boolean = false
    private val clickViewIds by lazy(LazyThreadSafetyMode.NONE) { ArrayList<Int>() }
    fun addChildClickViewIds(@IdRes vararg ids: Int) = ids.forEach { clickViewIds.add(it) }
    fun getChildClickViewIds() = clickViewIds
    private val longClickViewIds by lazy(LazyThreadSafetyMode.NONE) { ArrayList<Int>() }
    fun addChildLongClickViewIds(@IdRes vararg ids: Int) = ids.forEach { longClickViewIds.add(it) }
    fun getChildLongClickViewIds() = longClickViewIds
}