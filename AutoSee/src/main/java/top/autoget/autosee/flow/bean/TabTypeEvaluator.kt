package top.autoget.autosee.flow.bean

import android.animation.TypeEvaluator

class TabTypeEvaluator : TypeEvaluator<TabValue> {
    var tabValue: TabValue = TabValue()
    override fun evaluate(fraction: Float, startValue: TabValue, endValue: TabValue): TabValue =
        tabValue.apply {
            left = startValue.left + fraction * (endValue.left - startValue.left)
            top = startValue.top + fraction * (endValue.top - startValue.top)
            right = startValue.right + fraction * (endValue.right - startValue.right)
            bottom = startValue.bottom + fraction * (endValue.bottom - startValue.bottom)
        }//全部采用匀速
}