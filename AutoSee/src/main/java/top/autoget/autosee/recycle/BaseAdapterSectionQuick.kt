package top.autoget.autosee.recycle

import androidx.annotation.LayoutRes
import top.autoget.autosee.recycle.common.ViewHolderBase
import top.autoget.autosee.recycle.entity.EntitySection

abstract class BaseAdapterSectionQuick<T : EntitySection, VH : ViewHolderBase>
@JvmOverloads constructor(
    @LayoutRes private val sectionHeadResId: Int, data: MutableList<T>? = null,
    @LayoutRes layoutResId: Int = 0
) : BaseAdapterMultiQuick<T, VH>(data) {
    init {
        addItemType(EntitySection.HEADER_TYPE, sectionHeadResId)
        if (layoutResId != 0) setNormalLayout(layoutResId)
    }

    fun setNormalLayout(@LayoutRes layoutResId: Int) =//如果item不是多布局，此方法快速设置item layout；如果需要多布局item用addItemType
        addItemType(EntitySection.NORMAL_TYPE, layoutResId)

    protected abstract fun convertHeader(helper: VH, item: T)//重写设置Header
    protected open fun convertHeader(helper: VH, item: T, payloads: MutableList<Any>) {}//重写设置DiffHeader

    override fun isFixedViewType(type: Int): Boolean =
        super.isFixedViewType(type) || type == EntitySection.HEADER_TYPE

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) = when {
        payloads.isNotEmpty() ->
            when (holder.itemViewType) {
                EntitySection.HEADER_TYPE ->
                    convertHeader(holder, getItem(position - headerLayoutCount), payloads)
                else -> super.onBindViewHolder(holder, position, payloads)
            }
        else -> onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = when (holder.itemViewType) {
        EntitySection.HEADER_TYPE ->
            convertHeader(holder, getItem(position - headerLayoutCount))
        else -> super.onBindViewHolder(holder, position)
    }
}//快速实现带头部Adapter，本质属于多布局继承自BaseAdapterMultiQuick