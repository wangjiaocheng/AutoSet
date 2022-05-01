package top.autoget.autosee.banner.indicat

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import top.autoget.autosee.R

class IndicatorRect @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var mDefaultColor: Int = Color.GRAY
    private var mSelectedColor: Int = Color.WHITE
    private var mMargin: Int = 20
    private var isCanMove: Boolean = true
    private var mRectHeight: Int = 50
    private var mRectWidth: Int = 100
    private var mRoundSize: Int = 10
    private val mPaint: Paint = Paint()
    private val mRect: RectF = RectF()

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorRect)
        try {
            typedArray.run {
                mDefaultColor = getColor(R.styleable.IndicatorRect_rect_normalColor, Color.GRAY)
                mSelectedColor = getColor(R.styleable.IndicatorRect_rect_selectedColor, Color.WHITE)
                mMargin = getDimensionPixelSize(R.styleable.IndicatorRect_rect_horizon_margin, 20)
                isCanMove = getBoolean(R.styleable.IndicatorRect_rect_canMove, true)
                mRectHeight = getDimensionPixelSize(R.styleable.IndicatorRect_rect_height, 50)
                mRectWidth = getDimensionPixelSize(R.styleable.IndicatorRect_rect_width, 100)
                mRoundSize = getDimensionPixelSize(R.styleable.IndicatorRect_rect_round_size, 10)
                mPaint.apply {
                    isAntiAlias = true
                    color = mSelectedColor
                }
            }
        } finally {
            typedArray.recycle()
        }
    }

    private var mCount = 0
    private var mMoveDistance = 0
    private var mMoveSize = 0
    private var mViewPager2: ViewPager2? = null
    fun addPagerData(count: Int, viewPager2: ViewPager2?) {
        mViewPager2 = viewPager2
        if (configView(count)) return
        viewPager2?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) = rectScroll(position, positionOffset)

            override fun onPageSelected(position: Int) =
                if (!isCanMove) moveToPosition(position) else Unit
        })
    }

    private fun configView(count: Int): Boolean {
        removeAllViews()
        mMoveDistance = 0
        return when (count) {
            0 -> true
            else -> {
                mCount = count
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = mRoundSize.toFloat()
                    setSize(mRectWidth, mRectHeight)
                    setColor(mDefaultColor)
                }
                for (i in 0 until mCount) {
                    ImageView(context).apply {
                        background = drawable
                        layoutParams = LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {
                            when (i) {
                                mCount - 1 -> setMargins(mMargin, 0, mMargin, 0)
                                else -> setMargins(mMargin, 0, 0, 0)
                            }
                        }
                    }.let { addView(it) }
                }
                false
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        val child = getChildAt(0) as ImageView?
        if (child != null) {
            val cl = child.left.toFloat()
            val ct = child.top.toFloat()
            val cr = cl + child.measuredWidth
            val cb = ct + child.measuredHeight
            mRect[cl, ct, cr] = cb
            mMoveSize = mMargin + mRectWidth
            moveToPosition(mViewPager2?.currentItem ?: 0)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!mRect.isEmpty) canvas.run {
            save()
            translate(mMoveDistance.toFloat(), 0f)
            drawRoundRect(mRect, mRoundSize.toFloat(), mRoundSize.toFloat(), mPaint)
            restore()
        }
    }

    fun addRectBean(beanRect: BeanRect) {
        if (beanRect.isCanMove != isCanMove) isCanMove = beanRect.isCanMove
        if (beanRect.normalColor != -2) mDefaultColor = beanRect.normalColor
        if (beanRect.selectedColor != -2) mSelectedColor = beanRect.selectedColor
        if (beanRect.horizonMargin != 0) mMargin = beanRect.horizonMargin
        if (beanRect.width != 0) mRectWidth = beanRect.width
        if (beanRect.height != 0) mRectHeight = beanRect.height
        if (beanRect.roundRadius != 0) mRoundSize = beanRect.roundRadius
    }

    internal inner class PagerListener : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(i: Int) {}
        override fun onPageScrolled(
            position: Int, positionOffset: Float, positionOffsetPixels: Int
        ) = rectScroll(position, positionOffset)

        override fun onPageSelected(position: Int) =
            if (isCanMove) Unit else moveToPosition(position)
    }

    private fun rectScroll(position: Int, positionOffset: Float) {
        if (isCanMove) {
            mMoveDistance = when {
                position % mCount == mCount - 1 && positionOffset > 0 -> 0
                else -> (positionOffset * mMoveSize + position % mCount * mMoveSize).toInt()
            }
            invalidate()
        }
    }

    private fun moveToPosition(position: Int) {
        mMoveDistance = (position % mCount) * mMoveSize//处理不移动的情况
        invalidate()
    }
}