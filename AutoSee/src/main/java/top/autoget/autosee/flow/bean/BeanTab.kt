package top.autoget.autosee.flow.bean

import top.autoget.autosee.flow.common.ConstantsFlow

data class BeanTab(
    var tabType: Int = -1,//rect、tri、round、res、color、cus
    var tabColor: Int = -2,
    var tabWidth: Int = -1,
    var tabHeight: Int = -1,
    var tabRoundSize: Int = -1,//type选择round时圆角大小
    var tabMarginLeft: Int = -1,
    var tabMarginTop: Int = -1,
    var tabMarginRight: Int = -1,
    var tabMarginBottom: Int = -1,
    var tabClickAnimTime: Int = -1,//点击切换速度，当没有viewpager时相当于滑动速度
    var tabItemRes: Int = -1,//type为res时要关联的res如bitmap、shape等
    var autoScale: Boolean = false,//是否自动放大缩小效果
    var scaleFactor: Float = 1f,//放大倍数
    var tabOrientation: Int = ConstantsFlow.HORIZONTAL,//TabFlow方向：FlowConstants.HORIZONTAL横向、FlowConstants.VERTICAL竖向
    var actionOrientation: Int = -1,//tab为rect或tri时：左边还是右边
    var isAutoScroll: Boolean = true,//是否自动滚动
    var visualCount: Int = -1//可视个数
)