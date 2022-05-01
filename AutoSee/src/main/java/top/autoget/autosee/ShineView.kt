package top.autoget.autosee

import android.animation.Animator
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.ScreenKit.screenHeight
import top.autoget.autokit.VersionKit.aboveLollipop
import top.autoget.autokit.error
import java.util.*
import kotlin.math.*

class ShineView : View {
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
            super(context, attrs, defStyleAttr)

    private val paint = Paint()
    private val paintBig = Paint()
    private val paintSmall = Paint()
    private var mShineButton: ShineButton? = null
    private var shineAnimator: ShineAnimator? = null
    private var clickAnimator: ValueAnimator? = null
    var clickValue = 0f

    constructor(context: Context, shineButton: ShineButton, shineParams: ShineParams) :
            super(context) {
        initShineParams(shineParams, shineButton)
        paint.apply {
            color = Color.WHITE
            strokeWidth = 20f
            strokeCap = Paint.Cap.ROUND
        }
        paintBig.apply {
            color = shineColorBig
            strokeWidth = 20f
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        paintSmall.apply {
            color = shineColorSmall
            strokeWidth = 10f
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        mShineButton = shineButton
        shineAnimator =
            ShineAnimator(shineDistanceMultiple, animDuration.toLong(), animDurationClick.toLong())
                .apply {
                    ValueAnimator.setFrameDelay(25)//默认10ms，25ms节省CPU
                    addListener(object : Animator.AnimatorListener {
                        override fun onAnimationCancel(animator: Animator) {}
                        override fun onAnimationRepeat(animator: Animator) {}
                        override fun onAnimationStart(animator: Animator) {}
                        override fun onAnimationEnd(animator: Animator) {
                            shineButton.removeView(this@ShineView)
                        }
                    })
                }
        clickAnimator = ValueAnimator().apply {
            setFloatValues(0f, 1.1f)
            ValueAnimator.setFrameDelay(25)//默认10ms，25ms节省CPU
            duration = animDurationClick.toLong()
            interpolator = EasingInterpolator(Ease.QUART_OUT)
            addUpdateListener { valueAnimator ->
                clickValue = valueAnimator.animatedValue as Float
                invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    clickValue = 0f
                    invalidate()
                }
            })
        }
    }

    companion object {
        var colorRandom: IntArray = IntArray(10).apply {
            this[0] = Color.parseColor("#FFFF99")
            this[1] = Color.parseColor("#FFCCCC")
            this[2] = Color.parseColor("#996699")
            this[3] = Color.parseColor("#FF6666")
            this[4] = Color.parseColor("#FFFF66")
            this[5] = Color.parseColor("#F44336")
            this[6] = Color.parseColor("#666666")
            this[7] = Color.parseColor("#CCCC00")
            this[8] = Color.parseColor("#666666")
            this[9] = Color.parseColor("#999933")
        }
    }

    data class ShineParams(
        var enableFlashing: Boolean = false,
        var allowRandomColor: Boolean = false,
        var shineColorBig: Int = 0,
        var shineColorSmall: Int = 0,
        var shineSize: Int = 0,
        var shineCount: Int = 7,
        var animDuration: Int = 1500,
        var animDurationClick: Int = 200,
        var shineTurnAngle: Float = 20f,
        var smallShineOffsetAngle: Float = 20f,
        var shineDistanceMultiple: Float = 1.5f
    )

    var enableFlashing = false
    var allowRandomColor = false
    var shineColorBig: Int = colorRandom[1]
    var shineColorSmall: Int = colorRandom[0]
    var shineSize = 0
    var shineCount: Int = 7
    var animDuration: Int = 1500
    var animDurationClick: Int = 200
    var shineTurnAngle: Float = 20f
    var smallShineOffsetAngle: Float = 20f
    var shineDistanceMultiple: Float = 1.5f
    private fun initShineParams(shineParams: ShineParams, shineButton: ShineButton) {
        enableFlashing = shineParams.enableFlashing
        allowRandomColor = shineParams.allowRandomColor
        shineColorBig = shineParams.shineColorBig.let { if (it == 0) shineButton.colorFill else it }
        shineColorSmall = shineParams.shineColorSmall.let { if (it == 0) colorRandom[6] else it }
        shineSize = shineParams.shineSize
        shineCount = shineParams.shineCount
        animDuration = shineParams.animDuration
        animDurationClick = shineParams.animDurationClick
        shineTurnAngle = shineParams.shineTurnAngle
        smallShineOffsetAngle = shineParams.smallShineOffsetAngle
        shineDistanceMultiple = shineParams.shineDistanceMultiple
    }

    private val colorCount: Int = colorRandom.size//10
    private var isRun = false
    override fun onDraw(canvas: Canvas) = canvas.run {
        super.onDraw(this)
        for (i in 0 until shineCount) {
            paintBig.apply {
                if (allowRandomColor) color =
                    colorRandom[abs(colorCount / 2 - i).let { if (it < colorCount) it else colorCount - 1 }]
            }
            drawArc(
                rectFBig, 360f / shineCount * i + 1 + (value - 1) * shineTurnAngle,
                0.1f, false, getConfigPaint(paintBig)
            )
            drawArc(
                rectFSmall,
                360f / shineCount * i + 1 + (value - 1) * shineTurnAngle - smallShineOffsetAngle,
                0.1f, false, getConfigPaint(paintSmall)
            )
        }
        drawPoint(
            centerAnimX.toFloat(), centerAnimY.toFloat(), paintBig.apply {
                strokeWidth =
                    btnWidth.toFloat() * clickValue * (shineDistanceMultiple - distanceOffset)
            })
        drawPoint(
            centerAnimX.toFloat(), centerAnimY.toFloat(), paint.apply {
                strokeWidth = when (clickValue) {
                    0f -> 0f
                    else -> btnWidth.toFloat() * clickValue * (shineDistanceMultiple - distanceOffset) - 8
                }
            })
        shineAnimator?.run {
            if (!isRun) {
                isRun = true
                mShineButton?.let { showAnimation(it) }
            }
        } ?: Unit
    }

    private val random = Random()
    private fun getConfigPaint(paint: Paint): Paint =
        paint.apply { if (enableFlashing) color = colorRandom[random.nextInt(colorCount - 1)] }

    private var btnWidth: Int = 0
    private var btnHeight: Int = 0
    private var thirdLength: Double = 0.0
    private var centerAnimX: Int = 0
    private var centerAnimY: Int = 0
    private var value: Float = 0f
    private val rectFBig = RectF()
    private val rectFSmall = RectF()
    private val distanceOffset = 0.2f
    fun showAnimation(shineButton: ShineButton) {
        thirdLength = getThirdLength(shineButton.width.apply { btnWidth = this },
            shineButton.height.apply { btnHeight = this })
        centerAnimX = IntArray(2).apply { shineButton.getLocationInWindow(this) }[0] + btnWidth / 2
        centerAnimY = measuredHeight - shineButton.bottomHeight + btnHeight / 2
        shineAnimator?.addUpdateListener { valueAnimator ->
            value = valueAnimator.animatedValue as Float
            paintBig.apply {
                strokeWidth =
                    if (shineSize > 0) shineSize * (shineDistanceMultiple - value) else btnWidth / 2 * (shineDistanceMultiple - value)
            }
            paintSmall.apply {
                strokeWidth =
                    if (shineSize > 0) shineSize.toFloat() / 3 * 2 * (shineDistanceMultiple - value) else btnWidth / 3 * (shineDistanceMultiple - value)
            }
            rectFBig.set(
                centerAnimX - btnWidth / (3 - shineDistanceMultiple) * value,
                centerAnimY - btnHeight / (3 - shineDistanceMultiple) * value,
                centerAnimX + btnWidth / (3 - shineDistanceMultiple) * value,
                centerAnimY + btnHeight / (3 - shineDistanceMultiple) * value
            )
            rectFSmall.set(
                centerAnimX - btnWidth / (3 - shineDistanceMultiple + distanceOffset) * value,
                centerAnimY - btnHeight / (3 - shineDistanceMultiple + distanceOffset) * value,
                centerAnimX + btnWidth / (3 - shineDistanceMultiple + distanceOffset) * value,
                centerAnimY + btnHeight / (3 - shineDistanceMultiple + distanceOffset) * value
            )
            invalidate()
        }
        shineAnimator?.start()
        clickAnimator?.start()
    }

    private fun getThirdLength(btnHeight: Int, btnWidth: Int): Double =
        sqrt(btnWidth.toDouble().pow(2) + btnHeight.toDouble().pow(2))

    class ShineButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : PorterShapeImageView(context, attrs, defStyleAttr), LoggerKit {
        init {
            init(context, attrs)
        }

        private var activity: Activity? = null
        private var onButtonClickListener: OnButtonClickListener? = null
        var colorButton: Int = Color.GRAY
            set(colorButton) {
                field = colorButton
                paintColor = field
            }
        var colorFill: Int = Color.BLACK
        private val shineParams = ShineParams()
        private fun init(context: Context, attrs: AttributeSet?) {
            if (context is Activity) {
                activity = context
                setOnClickListener(OnButtonClickListener().apply { onButtonClickListener = this })
            }
            attrs?.let {
                val typedArray: TypedArray =
                    context.obtainStyledAttributes(attrs, R.styleable.ShineButton)
                try {
                    typedArray.run {
                        colorButton = getColor(R.styleable.ShineButton_btn_color, Color.GRAY)
                        colorFill = getColor(R.styleable.ShineButton_btn_color_fill, Color.BLACK)
                        shineParams.apply {
                            enableFlashing =
                                getBoolean(R.styleable.ShineButton_enable_flashing, false)
                            allowRandomColor =
                                getBoolean(R.styleable.ShineButton_allow_random_color, false)
                            shineColorBig = getColor(R.styleable.ShineButton_shine_color_big, 0)
                            shineColorSmall = getColor(R.styleable.ShineButton_shine_color_small, 0)
                            shineSize = getDimensionPixelSize(R.styleable.ShineButton_shine_size, 0)
                            shineCount = getInteger(R.styleable.ShineButton_shine_count, 7)
                            animDuration =
                                getInteger(R.styleable.ShineButton_animation_duration_shine, 1500)
                            animDurationClick =
                                getInteger(R.styleable.ShineButton_animation_duration_click, 200)
                            shineTurnAngle = getFloat(R.styleable.ShineButton_shine_turn_angle, 20f)
                            smallShineOffsetAngle =
                                getFloat(R.styleable.ShineButton_shine_offset_angle_small, 20f)
                            shineDistanceMultiple =
                                getFloat(R.styleable.ShineButton_shine_distance_multiple, 1.5f)
                        }
                    }
                } finally {
                    typedArray.recycle()
                }
                paintColor = colorButton
            }
        }

        private var shineView: ShineView? = null
        var isChecked = false
            private set

        @JvmOverloads
        fun setChecked(checked: Boolean, anim: Boolean = false) {
            isChecked = checked
            when {
                checked -> {
                    paintColor = colorFill
                    if (anim) showAnim
                }
                else -> {
                    paintColor = colorButton
                    if (anim) setCancel
                }
            }
            onListenerUpdate(checked)
        }

        interface OnCheckedChangeListener {
            fun onCheckedChanged(view: View, checked: Boolean)
        }

        var checkedChangeListener: OnCheckedChangeListener? = null
        private fun onListenerUpdate(checked: Boolean) =
            checkedChangeListener?.onCheckedChanged(this, checked)

        private val shakeAnimator: ValueAnimator = ValueAnimator().apply {
            setFloatValues(0.4f, 1f, 0.9f, 1f)
            duration = 500
            startDelay = 180
            interpolator = LinearInterpolator()
            invalidate()
            addUpdateListener { valueAnimator ->
                scaleX = valueAnimator.animatedValue as Float
                scaleY = valueAnimator.animatedValue as Float
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    paintColor = colorFill
                }

                override fun onAnimationEnd(animator: Animator) {
                    paintColor = if (isChecked) colorFill else colorButton
                }

                override fun onAnimationCancel(animator: Animator) {
                    paintColor = colorButton
                }

                override fun onAnimationRepeat(animator: Animator) {}
            })
        }
        val setCancel = {
            paintColor = colorButton
            shakeAnimator.run {
                end()
                cancel()
            }
        }
        val showAnim = activity?.let {
            shineView = ShineView(it, this, shineParams)
            (it.findViewById<View>(Window.ID_ANDROID_CONTENT) as ViewGroup).addView(
                shineView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            shakeAnimator.start()
        } ?: error("${loggerTag}->Please init.")

        inner class OnButtonClickListener @JvmOverloads constructor(onClickListener: OnClickListener? = null) :
            OnClickListener {
            var clickListener: OnClickListener? = onClickListener
            override fun onClick(view: View) {
                when {
                    isChecked -> {
                        isChecked = false
                        setCancel
                    }
                    else -> {
                        isChecked = true
                        showAnim
                    }
                }
                onListenerUpdate(isChecked)
                clickListener?.onClick(view)
            }
        }

        override fun setOnClickListener(onClickListener: OnClickListener?) {
            when (onClickListener) {
                is OnButtonClickListener -> super.setOnClickListener(onClickListener)
                else -> onButtonClickListener?.clickListener = onClickListener
            }
        }

        var defaultWidth = 50
        var defaultHeight = 50
        val bottomHeight: Int
            get() = activity?.run { screenHeight - IntArray(2).apply { getLocationInWindow(this) }[1] }
                ?: 0

        fun setShapeResource(raw: Int) {
            shape =
                resources.run { if (aboveLollipop) getDrawable(raw, null) else getDrawable(raw) }
        }

        fun removeView(view: View) =
            (activity?.findViewById<View>(Window.ID_ANDROID_CONTENT) as? ViewGroup)?.removeView(view)
                ?: error("${loggerTag}->Please init.")
    }

    open class PorterShapeImageView
    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        PorterImageView(context, attrs, defStyle) {
        var shape: Drawable? = null
            set(shape) {
                field = shape
                invalidate()
            }

        init {
            val typedArray: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.PorterShapeImageView, defStyle, 0)
            try {
                shape = typedArray.getDrawable(R.styleable.PorterShapeImageView_siShape)
            } finally {
                typedArray.recycle()
            }
        }

        override fun paintMaskCanvas(
            maskCanvas: Canvas, maskPaint: Paint, width: Int, height: Int
        ) = shape?.let {
            if (shape is BitmapDrawable) {
                configureBitmapBounds(getWidth(), getHeight())
                drawMatrix?.let {
                    maskCanvas.run {
                        val drawableSaveCount = saveCount
                        save()
                        concat(mMatrix)
                        shape?.draw(this)
                        restoreToCount(drawableSaveCount)
                    }
                    return
                }
            }
            shape?.apply { setBounds(0, 0, getWidth(), getHeight()) }?.draw(maskCanvas)
        } ?: Unit

        private var drawMatrix: Matrix? = null
        private val mMatrix = Matrix()
        private fun configureBitmapBounds(viewWidth: Int, viewHeight: Int) {
            drawMatrix = null
            shape?.run {
                if (intrinsicWidth > 0 && intrinsicHeight > 0 && !(intrinsicWidth == viewWidth && intrinsicHeight == viewHeight)) {
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                    min(
                        viewWidth.toFloat() / intrinsicWidth, viewHeight.toFloat() / intrinsicHeight
                    ).let { scale ->
                        mMatrix.apply { setScale(scale, scale) }.postTranslate(
                            (viewWidth - intrinsicWidth * scale) * 0.5f + 0.5f,
                            (viewHeight - intrinsicHeight * scale) * 0.5f + 0.5f
                        )
                    }
                }
            }
        }
    }

    abstract class PorterImageView
    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
        AppCompatImageView(context, attrs, defStyle), LoggerKit {
        init {
            if (scaleType == ScaleType.FIT_CENTER) scaleType = ScaleType.CENTER_CROP
        }

        private var invalidated = true
        override fun invalidate() {
            invalidated = true
            super.invalidate()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            createMaskCanvas(w, h, oldw, oldh)
        }

        private var maskBitmap: Bitmap? = null
        private var maskCanvas: Canvas? = null
        private val maskPaint: Paint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = Color.BLACK
        }
        private var drawableBitmap: Bitmap? = null
        private val drawableCanvas = Canvas()
        private val drawablePaint = Paint()
        var paintColor: Int = Color.GRAY
            set(paintColor) {
                field = paintColor
                setImageDrawable(ColorDrawable(field))
                drawablePaint.apply { color = field }
                invalidate()
            }

        private fun createMaskCanvas(width: Int, height: Int, wOld: Int, hOld: Int) {
            if (width > 0 && height > 0 && (width != wOld || height != hOld || maskCanvas == null)) {
                paintMaskCanvas(Canvas(
                    Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        .apply { maskBitmap = this })
                    .apply { maskCanvas = this }, maskPaint.apply { reset() }, width, height
                )
                drawableCanvas.setBitmap(Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    .apply { drawableBitmap = this })
                drawablePaint.apply {
                    flags = Paint.ANTI_ALIAS_FLAG
                    color = paintColor
                }
                invalidated = true
            }
        }

        protected abstract fun paintMaskCanvas(
            maskCanvas: Canvas, maskPaint: Paint, width: Int, height: Int
        )

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) = super.onMeasure(
            if (widthMeasureSpec == 0) 50 else widthMeasureSpec,
            if (heightMeasureSpec == 0) 50 else heightMeasureSpec
        )

        companion object {
            private val porterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }

        override fun onDraw(canvas: Canvas) {
            when {
                isInEditMode -> super.onDraw(canvas)
                else -> {
                    val saveCount = canvas.saveLayer(
                        0.0f, 0.0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG
                    )
                    try {
                        if (invalidated) drawable?.let {
                            invalidated = false
                            imageMatrix?.let {
                                val drawableSaveCount = drawableCanvas.saveCount
                                drawableCanvas.apply {
                                    save()
                                    concat(it)
                                }
                                drawable.draw(drawableCanvas)
                                drawableCanvas.restoreToCount(drawableSaveCount)
                            } ?: drawable.draw(drawableCanvas)
                            maskBitmap?.let {
                                drawableCanvas.drawBitmap(it, 0.0f, 0.0f, drawablePaint.apply {
                                    reset()
                                    isFilterBitmap = false
                                    xfermode = porterDuffXfermode
                                })
                            }
                        }
                        drawableBitmap?.let {
                            if (!invalidated) canvas.drawBitmap(
                                it, 0.0f, 0.0f, drawablePaint.apply { xfermode = null })
                        }
                    } catch (e: Exception) {
                        error("${loggerTag}->Exception occured while drawing $id", e)
                    } finally {
                        canvas.restoreToCount(saveCount)
                    }
                }
            }
        }
    }

    class ShineAnimator @JvmOverloads constructor(
        maxValue: Float = 1.5f, animDuration: Long = 1500L, delay: Long = 200L
    ) : ValueAnimator() {
        var mMaxValue = 1.5f
            set(value) {
                field = value.apply { setFloatValues(1f, this) }
            }

        init {
            setFloatValues(1f, maxValue.apply { mMaxValue = this })
            duration = animDuration
            startDelay = delay
            interpolator = EasingInterpolator(Ease.QUART_OUT)
        }
    }

    class EasingInterpolator(private val ease: Ease) : TimeInterpolator {
        override fun getInterpolation(input: Float): Float = EasingProvider[ease, input]

        object EasingProvider {
            operator fun get(ease: Ease, elapsedTimeRate: Float): Float = when (ease) {
                Ease.LINEAR -> elapsedTimeRate
                Ease.BOUNCE_IN -> getBounceIn(elapsedTimeRate)
                Ease.BOUNCE_OUT -> getBounceOut(elapsedTimeRate)
                Ease.BOUNCE_IN_OUT -> when {
                    elapsedTimeRate < 0.5 -> getBounceIn(elapsedTimeRate * 2) * 0.5f
                    else -> getBounceOut(elapsedTimeRate * 2 - 1) * 0.5f + 0.5f
                }
                Ease.QUAD_IN -> getPowIn(elapsedTimeRate, 2.0)
                Ease.QUAD_OUT -> getPowOut(elapsedTimeRate, 2.0)
                Ease.QUAD_IN_OUT -> getPowInOut(elapsedTimeRate, 2.0)
                Ease.CUBIC_IN -> getPowIn(elapsedTimeRate, 3.0)
                Ease.CUBIC_OUT -> getPowOut(elapsedTimeRate, 3.0)
                Ease.CUBIC_IN_OUT -> getPowInOut(elapsedTimeRate, 3.0)
                Ease.QUART_IN -> getPowIn(elapsedTimeRate, 4.0)
                Ease.QUART_OUT -> getPowOut(elapsedTimeRate, 4.0)
                Ease.QUART_IN_OUT -> getPowInOut(elapsedTimeRate, 4.0)
                Ease.QUINT_IN -> getPowIn(elapsedTimeRate, 5.0)
                Ease.QUINT_OUT -> getPowOut(elapsedTimeRate, 5.0)
                Ease.QUINT_IN_OUT -> getPowInOut(elapsedTimeRate, 5.0)
                Ease.ELASTIC_IN -> getElasticIn(elapsedTimeRate, 0.3, 1.0)
                Ease.ELASTIC_OUT -> getElasticOut(elapsedTimeRate, 0.3, 1.0)
                Ease.ELASTIC_IN_OUT -> getElasticInOut(elapsedTimeRate, 0.45, 1.0)
                Ease.SINE_IN -> (1 - cos(elapsedTimeRate * Math.PI / 2)).toFloat()
                Ease.SINE_OUT -> sin(elapsedTimeRate * Math.PI / 2).toFloat()
                Ease.SINE_IN_OUT -> (-0.5 * (cos(Math.PI * elapsedTimeRate) - 1)).toFloat()
                Ease.BACK_IN -> (elapsedTimeRate.pow(2) * ((1.7 + 1) * elapsedTimeRate - 1.7)).toFloat()
                Ease.BACK_OUT -> ((elapsedTimeRate - 1) * elapsedTimeRate * ((1.7 + 1) * elapsedTimeRate + 1.7) + 1).toFloat()
                Ease.BACK_IN_OUT -> getBackInOut(elapsedTimeRate, 1.7f)
                Ease.CIRC_IN -> -(sqrt(1 - elapsedTimeRate.pow(2)) - 1)
                Ease.CIRC_OUT -> sqrt(1 - (elapsedTimeRate - 1) * elapsedTimeRate)
                Ease.CIRC_IN_OUT -> when {
                    elapsedTimeRate * 2 < 1 -> -0.5 * (sqrt(1 - elapsedTimeRate.pow(2)) - 1)
                    else -> 0.5 * (sqrt(1 - (elapsedTimeRate - 2) * elapsedTimeRate) + 1)
                }.toFloat()
            }

            private fun getBounceIn(elapsedTimeRate: Float): Float =
                1 - getBounceOut(1 - elapsedTimeRate)

            private fun getBounceOut(elapsedTimeRate: Float): Float = when {
                elapsedTimeRate < 1 / 2.75 -> 7.5625 * elapsedTimeRate.pow(2)
                elapsedTimeRate < 2 / 2.75 -> 7.5625 * (elapsedTimeRate - 1.5 / 2.75) * elapsedTimeRate + 0.75
                elapsedTimeRate < 2.5 / 2.75 -> 7.5625 * (elapsedTimeRate - 2.25 / 2.75) * elapsedTimeRate + 0.9375
                else -> 7.5625 * (elapsedTimeRate - 2.625 / 2.75) * elapsedTimeRate + 0.984375
            }.toFloat()

            private fun getPowIn(elapsedTimeRate: Float, pow: Double): Float =
                elapsedTimeRate.toDouble().pow(pow).toFloat()

            private fun getPowOut(elapsedTimeRate: Float, pow: Double): Float =
                (1 - (1.0 - elapsedTimeRate).pow(pow)).toFloat()

            private fun getPowInOut(elapsedTimeRate: Float, pow: Double): Float = when {
                elapsedTimeRate * 2 < 1 -> 0.5 * elapsedTimeRate.toDouble().pow(pow)
                else -> 1 - 0.5 * abs((2.0 - elapsedTimeRate).pow(pow))
            }.toFloat()

            private fun getElasticIn(
                elapsedTimeRate: Float, period: Double, amplitude: Double
            ): Float = when (elapsedTimeRate) {
                0f, 1f -> elapsedTimeRate
                else -> -(amplitude * 2.0.pow(10.0 * (elapsedTimeRate - 1)) *
                        sin((elapsedTimeRate - s(period, amplitude)) * PI2 / period)).toFloat()
            }

            private const val PI2 = Math.PI * 2
            private fun s(period: Double, amplitude: Double) = period / PI2 * asin(1 / amplitude)
            private fun getElasticOut(
                elapsedTimeRate: Float, period: Double, amplitude: Double
            ): Float = when (elapsedTimeRate) {
                0f, 1f -> elapsedTimeRate
                else -> (amplitude * 2.0.pow(-10.0 * elapsedTimeRate) *
                        sin((elapsedTimeRate - s(period, amplitude)) * PI2 / period) + 1).toFloat()
            }

            private fun getElasticInOut(
                elapsedTimeRate: Float, period: Double, amplitude: Double
            ): Float = s(period, amplitude).let { s ->
                when {
                    elapsedTimeRate * 2 < 1 -> -0.5 * amplitude * 2.0.pow(10.0 * (elapsedTimeRate - 1)) *
                            sin((elapsedTimeRate - s) * PI2 / period)
                    else -> amplitude * 2.0.pow(-10.0 * (elapsedTimeRate - 1)) *
                            sin((elapsedTimeRate - s) * PI2 / period) * 0.5 + 1
                }.toFloat()
            }

            private fun getBackInOut(elapsedTimeRate: Float, amount: Float): Float =
                (amount * 1.525f).let { tempAmount ->
                    when {
                        elapsedTimeRate * 2 < 1 -> 0.5 * elapsedTimeRate.pow(2) *
                                ((tempAmount + 1) * elapsedTimeRate - tempAmount)
                        else -> 0.5 * ((elapsedTimeRate - 2) * elapsedTimeRate *
                                ((tempAmount + 1) * elapsedTimeRate + tempAmount) + 2)
                    }.toFloat()
                }
        }
    }

    enum class Ease {
        LINEAR,
        BOUNCE_IN, BOUNCE_OUT, BOUNCE_IN_OUT,
        QUAD_IN, QUAD_OUT, QUAD_IN_OUT,
        CUBIC_IN, CUBIC_OUT, CUBIC_IN_OUT,
        QUART_IN, QUART_OUT, QUART_IN_OUT,
        QUINT_IN, QUINT_OUT, QUINT_IN_OUT,
        ELASTIC_IN, ELASTIC_OUT, ELASTIC_IN_OUT,
        SINE_IN, SINE_OUT, SINE_IN_OUT,
        BACK_IN, BACK_OUT, BACK_IN_OUT,
        CIRC_IN, CIRC_OUT, CIRC_IN_OUT
    }
}