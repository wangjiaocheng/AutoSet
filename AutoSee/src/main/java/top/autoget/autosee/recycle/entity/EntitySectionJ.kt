package top.autoget.autosee.recycle.entity

abstract class EntitySectionJ : EntitySection {
    override val itemType: Int
        get() = if (isHeader) EntitySection.HEADER_TYPE else EntitySection.NORMAL_TYPE//除头布局外，默认只有NORMAL_TYPE一种布局；多布局重写
}//java无法实现EntitySection中默认接口实现，抽象类再封装提供默认实现