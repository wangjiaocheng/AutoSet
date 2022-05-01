package top.autoget.autosee.banner.listen

data class BeanPage(
    var isAutoLoop: Boolean = false,//是否开始自动轮播
    var isAutoCycle: Boolean = false,//是否循环填充数据，当isAutoLoop为true则isAutoCycle也为true，当数据大于loopMaxCount时也为true，如果不需自动轮播只需循环填为true
    var loopTime: Int = 0,//轮播时间，即每个item停留时间
    var smoothScrollTime: Int = 0,//Viewpager切换时间
    var loopMaxCount: Int = -1,//循环轮播最大个数，数据个数小于此值不支持轮播
    var cardHeight: Int = 0,//卡片高度
    var typeBannerTrans: TypeBannerTrans? = null//Viewpager的TransFormer效果，默认支持4种
)