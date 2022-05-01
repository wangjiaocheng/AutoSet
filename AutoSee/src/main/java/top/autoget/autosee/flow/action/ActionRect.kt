package top.autoget.autosee.flow.action

import android.graphics.Canvas
import android.graphics.Paint
import top.autoget.autosee.flow.LayoutTab
import top.autoget.autosee.flow.bean.TabValue

class ActionRect : ActionBase() {
    override fun config(parentView: LayoutTab) {
        super.config(parentView)
        mPaint.strokeCap = Paint.Cap.ROUND
        parentView.getChildAt(mCurrentIndex)?.let { child ->
            if (mTabRect.isEmpty) {
                var l: Float
                var t: Float
                var r: Float
                var b: Float
                when {
                    isLeftAction -> {
                        l = child.left.toFloat() + (mBeanTab?.tabMarginLeft ?: 0)
                        t = child.top.toFloat() + (mBeanTab?.tabMarginTop ?: 0)
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
            }
        }
        parentView.postInvalidate()
    }

    override fun valueChange(value: TabValue?) = when {
        isVertical -> {
            mTabRect.top = value?.top ?: 0f
            mTabRect.bottom = value?.bottom ?: 0f
            when {
                isLeftAction -> {
                    mTabRect.left = value?.left ?: 0f
                    mTabRect.right = (mBeanTab?.tabWidth ?: 0) + mTabRect.left
                }
                else -> {
                    mTabRect.left = value?.right ?: 0f
                    mTabRect.right = mTabRect.left - (mBeanTab?.tabWidth ?: 0)
                }
            }
        }
        else -> super.valueChange(value)
    }

    override fun draw(canvas: Canvas?) {
        canvas?.drawRect(mTabRect, mPaint)
    }
}