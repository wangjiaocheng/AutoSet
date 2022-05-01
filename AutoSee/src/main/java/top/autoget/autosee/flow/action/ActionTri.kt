package top.autoget.autosee.flow.action

import android.graphics.Canvas
import android.graphics.Path
import top.autoget.autosee.flow.LayoutTab
import top.autoget.autosee.flow.bean.TabValue

class ActionTri : ActionBase() {
    private var mPath: Path? = null
    override fun config(parentView: LayoutTab) {
        super.config(parentView)
        mPath = Path()
        parentView.getChildAt(0)?.let { child ->
            var l: Float
            var t: Float
            var r: Float
            var b: Float
            when {
                isLeftAction -> {
                    l = child.left.toFloat() + mBeanTab?.tabMarginLeft?.toFloat()!!
                    t = child.top.toFloat() + mBeanTab!!.tabMarginTop.toFloat()
                    r = l + (mBeanTab?.tabWidth ?: 0)
                    b = t + child.bottom - (mBeanTab?.tabMarginBottom ?: 0)
                    if (mBeanTab?.tabHeight != -1) {
                        t += (child.measuredHeight - (mBeanTab?.tabHeight ?: 0)) / 2
                        b = t + (mBeanTab?.tabHeight ?: 0)
                    }
                }
                isRightAction -> {
                    l = child.right.toFloat() - (mBeanTab?.tabMarginRight ?: 0)
                    t = child.top.toFloat() - (mBeanTab?.tabMarginTop ?: 0)
                    r = l - (mBeanTab?.tabWidth ?: 0)
                    b = t + (mBeanTab?.tabHeight ?: 0)
                    if (mBeanTab?.tabHeight != -1) {
                        t += (child.measuredHeight - (mBeanTab?.tabHeight ?: 0)) / 2
                        b = t + (mBeanTab?.tabHeight ?: 0)
                    }
                }
                else -> {
                    l = (mBeanTab?.tabMarginLeft ?: 0) + child.left.toFloat()
                    t = (mBeanTab?.tabMarginTop
                        ?: 0) + child.bottom.toFloat() - (mBeanTab?.tabHeight
                        ?: 0) - (mBeanTab?.tabMarginBottom ?: 0)
                    r = child.right.toFloat() - (mBeanTab?.tabMarginRight ?: 0)
                    b = t + (mBeanTab?.tabHeight ?: 0)
                    if (mBeanTab?.tabWidth != -1) {
                        l += (child.measuredWidth - (mBeanTab?.tabWidth ?: 0)) / 2
                        r = (mBeanTab?.tabWidth ?: 0) + l
                    }
                }
            }
            mTabRect[l, t, r] = b
            mPath?.run {
                when {
                    isVertical -> {
                        moveTo(r, t + (mBeanTab?.tabHeight ?: 0) / 2)
                        lineTo(l, t)
                        lineTo(l, b)
                    }
                    else -> {
                        moveTo(l + (mBeanTab?.tabWidth ?: 0) / 2, t)
                        lineTo(l, b)
                        lineTo(r, b)
                    }
                }
            }
        }
    }

    override fun valueChange(value: TabValue?) {
        super.valueChange(value)
        mPath?.run {
            reset()
            when {
                isVertical -> {
                    value?.valueToRect?.let { mTabRect.set(it) }
                    var l = mTabRect.left
                    val t = mTabRect.top
                    var r = mTabRect.right
                    val b = mTabRect.bottom
                    if (isRightAction) {
                        l = r
                        r = l - (mBeanTab?.tabWidth ?: 0)
                    }
                    moveTo(r, t + (mBeanTab?.tabHeight ?: 0) / 2)
                    lineTo(l, t)
                    lineTo(l, b)
                }
                else -> {
                    moveTo(mTabRect.width() / 2 + mTabRect.left, mTabRect.top)
                    lineTo(mTabRect.left, mTabRect.bottom)
                    lineTo(mTabRect.right, mTabRect.bottom)
                }
            }
        }
    }

    override fun draw(canvas: Canvas?) {
        mPath?.let { canvas?.drawPath(it, mPaint) }
    }
}