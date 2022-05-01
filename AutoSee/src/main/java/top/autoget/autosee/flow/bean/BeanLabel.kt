package top.autoget.autosee.flow.bean

data class BeanLabel(
    var maxSelectCount: Int = 1,//多选最大个数
    var isAutoScroll: Boolean = true,//自动滚动，默认true
    var showLines: Int = -1,//最多显示行数，默认全部
    var showMoreLayoutId: Int = -1,//显示更多layoutId
    var showMoreColor: Int = -2,//显示更多背景色，建议与主布局背景色一致
    var handUpLayoutId: Int = -1//收起的layoutId
)