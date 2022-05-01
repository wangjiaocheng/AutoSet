package top.autoget.autosee.flow.action

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import top.autoget.autosee.flow.LayoutTab
import top.autoget.autosee.flow.bean.BeanTab
import top.autoget.autosee.flow.bean.TabValue

class ActionRes : ActionBase() {
    private var mRes = -1
    override fun configAttrs(beanTab: BeanTab?) {
        super.configAttrs(beanTab)
        if (beanTab?.tabItemRes != -1) mRes = beanTab?.tabItemRes ?: -1
    }

    private var mDrawable: Drawable? = null
    private var mBitmap: Bitmap? = null
    private var mSrcRect: Rect? = null
    override fun config(parentView: LayoutTab) {
        super.config(parentView)
        if (mRes != -1) mDrawable = mContext?.let { ContextCompat.getDrawable(it, mRes) }
        parentView.getChildAt(0)?.let { child ->
            mDrawable?.let {
                val width = child.measuredWidth
                val height = child.measuredHeight
                it.setBounds(0, 0, width, height)
                it.draw(
                    Canvas(
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            .apply { mBitmap = this })
                )
                mSrcRect = Rect(0, 0, width, height)
                val l = (mBeanTab?.tabMarginLeft ?: 0) + child.left.toFloat()
                val t = (mBeanTab?.tabMarginTop ?: 0) + child.top.toFloat()
                val r = child.right.toFloat() - (mBeanTab?.tabMarginRight ?: 0)
                val b = child.bottom.toFloat() - (mBeanTab?.tabMarginBottom ?: 0)
                mTabRect[l, t, r] = b
            }
        }
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
        mBitmap?.let { canvas?.drawBitmap(it, mSrcRect, mTabRect, mPaint) }
    }
}