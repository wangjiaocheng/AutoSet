package top.autoget.autosee.banner.indicat

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import top.autoget.autosee.R

class IndicatorCircle @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var mDefaultColor: Int = Color.GRAY
    private var mSelectedColor: Int = Color.WHITE
    private var mMargin: Int = 20
    private var isCanMove: Boolean = true
    private var mSize: Int = 100
    private var mRectWidth: Int = mSize
    private var mScaleFactor: Float = 1f
    private var mTypeIndicatorCircle: TypeIndicatorCircle? = TypeIndicatorCircle.NORMAL
    private val mPaint: Paint = Paint()
    private val mRect: RectF = RectF()

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicatorCircle)
        try {
            typedArray.run {
                mDefaultColor = getColor(R.styleable.IndicatorCircle_cir_normalColor, Color.GRAY)
                mSelectedColor =
                    getColor(R.styleable.IndicatorCircle_cir_selectedColor, Color.WHITE)
                mMargin = getDimensionPixelSize(R.styleable.IndicatorCircle_cir_horizon_margin, 20)
                isCanMove = getBoolean(R.styleable.IndicatorCircle_cir_canMove, true)
                mSize = getDimensionPixelSize(R.styleable.IndicatorCircle_cir_size, 100)
                mRectWidth =
                    getDimensionPixelSize(R.styleable.IndicatorCircle_cir_rect_width, mSize)
                mScaleFactor = getFloat(R.styleable.IndicatorCircle_cir_scale_factor, 1f)
                val type = getInteger(R.styleable.IndicatorCircle_cir_type, 0)
                mTypeIndicatorCircle = TypeIndicatorCircle.values()[type]
                if (mTypeIndicatorCircle == TypeIndicatorCircle.SCALE) isCanMove = false
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
    private var mLastPosition = 0
    private var mViewPager2: ViewPager2? = null
    fun addPagerData(count: Int, viewPager2: ViewPager2?) {
        mViewPager2 = viewPager2
        if (configView(count)) return
        viewPager2?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) = circleScroll(position, positionOffset)

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
                    shape = GradientDrawable.OVAL
                    setSize(mSize, mSize)
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
            val cl: Float
            val cr: Float
            when (mTypeIndicatorCircle) {
                TypeIndicatorCircle.CIR_TO_RECT -> {
                    val offset = (mRectWidth - mSize) / 2f
                    cl = child.left - offset
                    cr = cl + mRectWidth
                }
                else -> {
                    cl = child.left.toFloat()
                    cr = cl + child.measuredWidth
                }
            }
            val ct = child.top.toFloat()
            val cb = ct + child.measuredHeight
            mRect[cl, ct, cr] = cb
            mMoveSize = mMargin + mSize
            val currentItem = mViewPager2?.currentItem ?: 0
            if (mTypeIndicatorCircle == TypeIndicatorCircle.SCALE && currentItem % mCount == 0)
                doScaleAnim(child, ANIM_OUT)
            moveToPosition(currentItem)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!mRect.isEmpty && mTypeIndicatorCircle != TypeIndicatorCircle.SCALE) canvas.run {
            save()
            translate(mMoveDistance.toFloat(), 0f)
            drawRoundRect(mRect, mSize.toFloat(), mSize.toFloat(), mPaint)
            restore()
        }
    }

    fun addCircleBean(beanCircle: BeanCircle) {
        if (beanCircle.typeIndicatorCircle != TypeIndicatorCircle.UNKNOWN)
            mTypeIndicatorCircle = beanCircle.typeIndicatorCircle
        if (beanCircle.normalColor != -2) mDefaultColor = beanCircle.normalColor
        if (beanCircle.selectedColor != -2) mSelectedColor = beanCircle.selectedColor
        if (beanCircle.circleSize != 0) mSize = beanCircle.circleSize
        if (beanCircle.horizonMargin != 0) mMargin = beanCircle.horizonMargin
        if (beanCircle.scaleFactor != 1f) mScaleFactor = beanCircle.scaleFactor
        if (beanCircle.rectWidth != 0) mRectWidth = beanCircle.rectWidth
        if (isCanMove != beanCircle.isCanMove) isCanMove = beanCircle.isCanMove
        if (mTypeIndicatorCircle == TypeIndicatorCircle.SCALE) isCanMove = false
    }

    internal inner class PagerListener : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(i: Int) {}
        override fun onPageScrolled(
            position: Int, positionOffset: Float, positionOffsetPixels: Int
        ) = circleScroll(position, positionOffset)

        override fun onPageSelected(position: Int) =
            if (isCanMove) Unit else moveToPosition(position)
    }

    private fun circleScroll(position: Int, positionOffset: Float) {
        if (isCanMove) {
            mMoveDistance = when {
                position % mCount == mCount - 1 && positionOffset > 0 -> 0
                else -> (positionOffset * mMoveSize + position % mCount * mMoveSize).toInt()
            }
            invalidate()
        }
    }

    companion object {
        private const val ANIM_IN = 0x1001
        private const val ANIM_OUT = 0x1002
        private const val ANIM_OUT_TIME = 400
        private const val ANIM_IN_TIME = 300
    }

    private fun moveToPosition(position: Int) {
        mMoveDistance = (position % mCount) * mMoveSize//处理不移动的情况
        if (mTypeIndicatorCircle == TypeIndicatorCircle.SCALE) {
            if (mLastPosition >= 0) (getChildAt(mLastPosition) as ImageView?)
                ?.apply { isSelected = false }?.let { doScaleAnim(it, ANIM_IN) }
            (getChildAt(position) as ImageView?)
                ?.apply { isSelected = true }?.let { doScaleAnim(it, ANIM_OUT) }
            mLastPosition = position
        }//处理放大缩小的情况
        invalidate()
    }

    private fun doScaleAnim(view: ImageView, type: Int) {
        val scaleX: ObjectAnimator
        val scaleY: ObjectAnimator
        val colorAnim: ObjectAnimator
        val animatorSet = AnimatorSet()
        when (type) {
            ANIM_OUT -> {
                scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, mScaleFactor)
                scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, mScaleFactor)
                colorAnim =
                    ObjectAnimator.ofInt(view.background, "color", mDefaultColor, mSelectedColor)
                animatorSet.duration = ANIM_OUT_TIME.toLong()
            }
            else -> {
                scaleX = ObjectAnimator.ofFloat(view, "scaleX", mScaleFactor, 1f)
                scaleY = ObjectAnimator.ofFloat(view, "scaleY", mScaleFactor, 1f)
                colorAnim =
                    ObjectAnimator.ofInt(view.background, "color", mSelectedColor, mDefaultColor)
                animatorSet.duration = ANIM_IN_TIME.toLong()
            }
        }
        colorAnim.setEvaluator(HsvEvaluator())
        animatorSet.play(scaleX).with(scaleY).with(colorAnim)
        animatorSet.apply { interpolator = AccelerateDecelerateInterpolator() }.start()
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setSize(mSize, mSize)
        }
        colorAnim.addUpdateListener {
            drawable.setColor(it.animatedValue as Int)
            view.background = drawable
        }
    }

    private class HsvEvaluator : TypeEvaluator<Int> {
        var startHsv = FloatArray(3)
        var endHsv = FloatArray(3)
        var outHsv = FloatArray(3)
        override fun evaluate(fraction: Float, startValue: Int, endValue: Int): Int {
            Color.colorToHSV(startValue, startHsv)
            Color.colorToHSV(endValue, endHsv)
            when {
                endHsv[0] - startHsv[0] > 180 -> endHsv[0] = endHsv[0] - 360
                endHsv[0] - startHsv[0] < -180 -> endHsv[0] = endHsv[0] + 360
            }
            outHsv[0] = startHsv[0] + (endHsv[0] - startHsv[0]) * fraction
            when {
                outHsv[0] > 360 -> outHsv[0] = outHsv[0] - 360
                outHsv[0] < 0 -> outHsv[0] = outHsv[0] + 360
            }
            outHsv[1] = startHsv[1] + (endHsv[1] - startHsv[1]) * fraction
            outHsv[2] = startHsv[2] + (endHsv[2] - startHsv[2]) * fraction
            val alpha =
                startValue shr 24 + ((endValue shr 24 - startValue shr 24) * fraction).toInt()
            return Color.HSVToColor(alpha, outHsv)
        }
    }//处理颜色渐变
}