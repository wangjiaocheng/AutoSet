package top.autoget.autosee.flow.bean

import android.graphics.RectF

class TabValue(var left: Float = 0f, var right: Float = 0f) {
    var top = 0f
    var bottom = 0f
    val valueToRect: RectF = RectF(left, top, right, bottom)
    val RectF?.rectToValue: TabValue
        get() = let {
            TabValue().apply {
                left = it?.left ?: 0f
                top = it?.top ?: 0f
                right = it?.right ?: 0f
                bottom = it?.bottom ?: 0f
            }
        }
}