package top.autoget.autokit

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

object DensityKit {
    fun dip2px(dpValue: Float): Int = value2px(dpValue, TypedValue.COMPLEX_UNIT_DIP)
    fun sp2px(spValue: Float): Int = value2px(spValue, TypedValue.COMPLEX_UNIT_SP)
    fun value2px(value: Float, unit: Int): Int = Resources.getSystem().displayMetrics.run {
        (when (unit) {
            TypedValue.COMPLEX_UNIT_PX -> value
            TypedValue.COMPLEX_UNIT_DIP -> value * density
            TypedValue.COMPLEX_UNIT_SP -> value * scaledDensity
            TypedValue.COMPLEX_UNIT_PT -> value * xdpi * (1.0f / 72)
            TypedValue.COMPLEX_UNIT_IN -> value * xdpi
            TypedValue.COMPLEX_UNIT_MM -> value * xdpi * (1.0f / 25.4f)
            else -> 0f - 0.5f
        } + 0.5f).toInt()
    }

    fun px2dip(pxValue: Float): Int = px2value(pxValue, TypedValue.COMPLEX_UNIT_DIP)
    fun px2sp(pxValue: Float): Int = px2value(pxValue, TypedValue.COMPLEX_UNIT_SP)
    fun px2value(pxValue: Float, unit: Int): Int = Resources.getSystem().displayMetrics.run {
        (when (unit) {
            TypedValue.COMPLEX_UNIT_PX -> pxValue
            TypedValue.COMPLEX_UNIT_DIP -> pxValue / density
            TypedValue.COMPLEX_UNIT_SP -> pxValue / scaledDensity
            TypedValue.COMPLEX_UNIT_PT -> pxValue / (xdpi * (1.0f / 72))
            TypedValue.COMPLEX_UNIT_IN -> pxValue / xdpi
            TypedValue.COMPLEX_UNIT_MM -> pxValue / (xdpi * (1.0f / 25.4f))
            else -> 0f - 0.5f
        } + 0.5f).toInt()
    }

    interface OnGetSizeListener {
        fun onGetSize(view: View)
    }

    fun forceGetViewSize(view: View, listener: OnGetSizeListener?): Boolean =
        view.post { listener?.onGetSize(view) }

    fun getViewWidth(view: View): Int = measureView(view).first
    fun getViewHeight(view: View): Int = measureView(view).second
    fun measureView(view: View): Pair<Int, Int> = (view.layoutParams ?: ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )).let { layoutParams ->
        view.measure(
            ViewGroup.getChildMeasureSpec(0, 0, layoutParams.width), when {
                layoutParams.height > 0 -> View.MeasureSpec
                    .makeMeasureSpec(layoutParams.height, View.MeasureSpec.EXACTLY)
                else -> View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            }
        )
        Pair(view.measuredWidth, view.measuredHeight)
    }
}