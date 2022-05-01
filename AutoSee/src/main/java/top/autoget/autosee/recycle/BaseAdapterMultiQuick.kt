package top.autoget.autosee.recycle

import android.util.SparseIntArray
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import top.autoget.autosee.recycle.common.ViewHolderBase
import top.autoget.autosee.recycle.entity.EntityMultiItem

abstract class BaseAdapterMultiQuick<T : EntityMultiItem, VH : ViewHolderBase>(data: MutableList<T>? = null) :
    BaseAdapterQuick<T, VH>(0, data) {
    override fun getDefItemViewType(position: Int): Int = data[position].itemType
    private val layouts: SparseIntArray by lazy(LazyThreadSafetyMode.NONE) { SparseIntArray() }
    protected fun addItemType(type: Int, @LayoutRes layoutResId: Int) =
        layouts.put(type, layoutResId)//设置多布局

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): VH =
        createBaseViewHolder(parent, layouts.get(viewType).apply {
            require(this != 0)
            { "ViewType: $viewType found layoutResId，please use addItemType() first!" }
        })
}//多类型布局适用：类型较少，业务不复杂场景，data<T>必须实现EntityMultiItem，无法实现用BaseAdapterMultiDelegate；类型较多，分离各类型业务逻辑用BaseAdapterMultiProvider