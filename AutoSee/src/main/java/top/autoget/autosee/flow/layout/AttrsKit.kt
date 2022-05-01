package top.autoget.autosee.flow.layout

import android.content.res.TypedArray
import android.graphics.Color
import top.autoget.autosee.R
import top.autoget.autosee.flow.bean.BeanTab
import top.autoget.autosee.flow.common.ConstantsFlow

object AttrsKit {
    fun getBeanTab(typedArray: TypedArray): BeanTab = BeanTab().apply {
        try {
            typedArray.run {
                tabType = getInteger(R.styleable.LayoutTab_tab_type, -1)
                tabColor = getColor(R.styleable.LayoutTab_tab_color, Color.RED)
                tabWidth = getDimensionPixelSize(R.styleable.LayoutTab_tab_width, -1)
                tabHeight = getDimensionPixelSize(R.styleable.LayoutTab_tab_height, -1)
                tabRoundSize = getDimensionPixelSize(R.styleable.LayoutTab_tab_round_size, 10)
                tabMarginLeft = getDimensionPixelSize(R.styleable.LayoutTab_tab_margin_l, 0)
                tabMarginTop = getDimensionPixelSize(R.styleable.LayoutTab_tab_margin_t, 0)
                tabMarginRight = getDimensionPixelSize(R.styleable.LayoutTab_tab_margin_r, 0)
                tabMarginBottom = getDimensionPixelSize(R.styleable.LayoutTab_tab_margin_b, 0)
                tabClickAnimTime = getInt(R.styleable.LayoutTab_tab_click_animTime, 300)
                tabItemRes = getResourceId(R.styleable.LayoutTab_tab_item_res, -1)
                autoScale = getBoolean(R.styleable.LayoutTab_tab_item_autoScale, false)
                scaleFactor = getFloat(R.styleable.LayoutTab_tab_scale_factor, 1f)
                tabOrientation =
                    getInteger(R.styleable.LayoutTab_tab_orientation, ConstantsFlow.HORIZONTAL)
                actionOrientation = getInteger(R.styleable.LayoutTab_tab_action_orientation, -1)
                isAutoScroll = getBoolean(R.styleable.LayoutTab_tab_isAutoScroll, true)
                visualCount = getInteger(R.styleable.LayoutTab_tab_visual_count, -1)
            }
        } finally {
            typedArray.recycle()
        }
    }//获取BeanTab自定义数据

    fun diffBeanTab(xmlBeanTab: BeanTab, userBeanTab: BeanTab): BeanTab = xmlBeanTab.apply {
        if (userBeanTab.tabType != -1) xmlBeanTab.tabType = userBeanTab.tabType
        if (userBeanTab.tabColor != -2) xmlBeanTab.tabColor = userBeanTab.tabColor
        if (userBeanTab.tabWidth != -1) xmlBeanTab.tabWidth = userBeanTab.tabWidth
        if (userBeanTab.tabHeight != -1) xmlBeanTab.tabHeight = userBeanTab.tabHeight
        if (userBeanTab.tabRoundSize != -1) xmlBeanTab.tabRoundSize = userBeanTab.tabRoundSize
        if (userBeanTab.tabMarginLeft != -1) xmlBeanTab.tabMarginLeft = userBeanTab.tabMarginLeft
        if (userBeanTab.tabMarginTop != -1) xmlBeanTab.tabMarginTop = userBeanTab.tabMarginTop
        if (userBeanTab.tabMarginRight != -1) xmlBeanTab.tabMarginRight = userBeanTab.tabMarginRight
        if (userBeanTab.tabMarginBottom != -1)
            xmlBeanTab.tabMarginBottom = userBeanTab.tabMarginBottom
        if (userBeanTab.tabClickAnimTime != -1)
            xmlBeanTab.tabClickAnimTime = userBeanTab.tabClickAnimTime
        if (userBeanTab.tabItemRes != -1) xmlBeanTab.tabItemRes = userBeanTab.tabItemRes
        if (userBeanTab.autoScale) xmlBeanTab.autoScale = true
        if (userBeanTab.scaleFactor != 1f) xmlBeanTab.scaleFactor = userBeanTab.scaleFactor
        if (userBeanTab.tabOrientation != ConstantsFlow.HORIZONTAL)
            xmlBeanTab.tabOrientation = userBeanTab.tabOrientation
        if (userBeanTab.actionOrientation != -1)
            xmlBeanTab.actionOrientation = userBeanTab.actionOrientation
        if (!userBeanTab.isAutoScroll) xmlBeanTab.isAutoScroll = false
        if (userBeanTab.visualCount != -1) xmlBeanTab.visualCount = userBeanTab.visualCount
    }//对比数据：xml数据被用户数据覆盖
}