package top.autoget.autosee.recycle.entity

interface EntitySection : EntityMultiItem {
    companion object {
        const val NORMAL_TYPE = -100
        const val HEADER_TYPE = -99
    }

    val isHeader: Boolean
    override val itemType: Int
        get() = if (isHeader) HEADER_TYPE else NORMAL_TYPE//除头布局外，默认只有NORMAL_TYPE一种布局；多布局重写
}//带头部布局实体类接口