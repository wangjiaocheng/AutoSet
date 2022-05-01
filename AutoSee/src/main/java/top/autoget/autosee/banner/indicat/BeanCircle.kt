package top.autoget.autosee.banner.indicat

data class BeanCircle(
    var normalColor: Int = -2,//圆点默认颜色
    var selectedColor: Int = -2,//圆点选中颜色
    var horizonMargin: Int = 0,//圆点之间margin
    var isCanMove: Boolean = true,//是否可以移动，默认true
    var circleSize: Int = 0,//圆点大小
    var rectWidth: Int = 0,//当圆点变矩形矩形宽度，矩形高度cirSize
    var scaleFactor: Float = 1f,//放大缩小倍速，默认不放大
    var typeIndicatorCircle: TypeIndicatorCircle? = null
)