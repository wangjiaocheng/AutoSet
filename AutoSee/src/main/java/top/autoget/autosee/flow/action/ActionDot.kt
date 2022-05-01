package top.autoget.autosee.flow.action

import android.graphics.Canvas
import top.autoget.autosee.flow.LayoutTab
import top.autoget.autosee.flow.bean.TabValue

class ActionDot : ActionBase() {
    override fun config(parentView: LayoutTab) {
        super.config(parentView)
        parentView.getChildAt(0)?.let { child ->
            val l = parentView.paddingLeft + child.measuredWidth / 2f
            val t = parentView.paddingTop + child.measuredHeight - (mBeanTab?.tabHeight
                ?: 0) / 2 - (mBeanTab?.tabMarginBottom ?: 0).toFloat()
            val r = (mBeanTab?.tabWidth ?: 0) + l
            val b = child.measuredHeight.toFloat() - (mBeanTab?.tabMarginBottom ?: 0)
            mTabRect.set(l, t, r, b)
        }
    }

    override fun valueChange(value: TabValue?) {
        super.valueChange(value)
        mTabRect.left = (value?.left ?: 0f) + (mBeanTab?.tabWidth ?: 0) / 2
    }//自定义都从left开始算起，需要加上圆的半径

    override fun draw(canvas: Canvas?) {
        canvas?.drawCircle(mTabRect.left, mTabRect.top, (mBeanTab?.tabWidth ?: 0) / 2f, mPaint)
    }
}//自定义演示圆点