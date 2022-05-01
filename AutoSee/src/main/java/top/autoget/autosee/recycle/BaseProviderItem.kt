package top.autoget.autosee.recycle

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import top.autoget.autosee.recycle.common.ViewHolderBase
import top.autoget.autosee.recycle.common.getItemView
import java.lang.ref.WeakReference

abstract class BaseProviderItem<T> {
    lateinit var context: Context
    abstract val itemViewType: Int
    private var weakAdapter: WeakReference<BaseAdapterMultiProvider<T>>? = null
    internal fun setAdapter(adapter: BaseAdapterMultiProvider<T>) =
        apply { weakAdapter = WeakReference(adapter) }

    open fun getAdapter(): BaseAdapterMultiProvider<T>? = weakAdapter?.get()
    abstract fun convert(helper: ViewHolderBase, item: T)
    open fun convert(helper: ViewHolderBase, item: T, payloads: List<Any>) {}
    abstract val layoutId: Int @LayoutRes get
    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase =
        ViewHolderBase(parent.getItemView(layoutId))

    open fun onViewHolderCreated(viewHolder: ViewHolderBase, viewType: Int) {}
    open fun onViewAttachedToWindow(holder: ViewHolderBase) {}
    open fun onViewDetachedFromWindow(holder: ViewHolderBase) {}
    open fun onClick(helper: ViewHolderBase, view: View, data: T, position: Int) {}
    open fun onLongClick(helper: ViewHolderBase, view: View, data: T, position: Int): Boolean =
        false

    open fun onChildClick(helper: ViewHolderBase, view: View, data: T, position: Int) {}
    open fun onChildLongClick(helper: ViewHolderBase, view: View, data: T, position: Int): Boolean =
        false

    private val clickViewIds by lazy(LazyThreadSafetyMode.NONE) { ArrayList<Int>() }
    fun addChildClickViewIds(@IdRes vararg ids: Int) = ids.forEach { clickViewIds.add(it) }
    fun getChildClickViewIds() = clickViewIds
    private val longClickViewIds by lazy(LazyThreadSafetyMode.NONE) { ArrayList<Int>() }
    fun addChildLongClickViewIds(@IdRes vararg ids: Int) = ids.forEach { longClickViewIds.add(it) }
    fun getChildLongClickViewIds() = longClickViewIds
}