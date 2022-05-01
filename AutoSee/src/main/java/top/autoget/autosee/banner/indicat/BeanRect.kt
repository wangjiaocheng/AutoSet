package top.autoget.autosee.banner.indicat

data class BeanRect(
    var normalColor: Int = -2,//矩形默认颜色
    var selectedColor: Int = -2,//矩形选中颜色
    var horizonMargin: Int = 0,//矩形之间margin
    var isCanMove: Boolean = true,//是否可以移动，默认true
    var height: Int = 0,//矩形高度
    var width: Int = 0,//矩形宽度
    var roundRadius: Int = 0//矩形圆角
)