package top.autoget.autosee.flow.action

import android.graphics.Canvas
import top.autoget.autosee.flow.LayoutTab
import top.autoget.autosee.flow.bean.BeanTab
import top.autoget.autosee.flow.bean.TabValue

class ActionRound : ActionBase() {
    private var mRound = 0f
    override fun configAttrs(beanTab: BeanTab?) {
        super.configAttrs(beanTab)
        if (beanTab?.tabRoundSize != -1) mRound = beanTab?.tabRoundSize?.toFloat() ?: -1f
    }

    override fun config(parentView: LayoutTab) {
        super.config(parentView)
        parentView.getChildAt(0)?.let { child ->
            val l = (mBeanTab?.tabMarginLeft ?: 0) + child.left.toFloat()
            val t = (mBeanTab?.tabMarginTop ?: 0) + child.top.toFloat()
            val r = child.right.toFloat() - (mBeanTab?.tabMarginRight ?: 0)
            val b = child.bottom.toFloat() - (mBeanTab?.tabMarginBottom ?: 0)
            mTabRect[l, t, r] = b
        }
        parentView.postInvalidate()
    }

    override fun valueChange(value: TabValue?) {
        if (isVertical) {
            mTabRect.top = value?.top ?: 0f
            mTabRect.bottom = value?.bottom ?: 0f
        }
        mTabRect.left = value?.left ?: 0f
        mTabRect.right = value?.right ?: 0f
    }

    override fun draw(canvas: Canvas?) {
        canvas?.drawRoundRect(mTabRect, mRound, mRound, mPaint)
    }
}