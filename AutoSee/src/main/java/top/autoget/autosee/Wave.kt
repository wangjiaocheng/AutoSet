package top.autoget.autosee

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import top.autoget.autokit.ImageKit
import kotlin.math.sin

class Wave @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    init {
        init(context, attrs)
    }

    enum class ShapeType { CIRCLE, SQUARE }
    companion object {
        private const val DEFAULT_AMPLITUDE_RATIO = 0.05f
        private const val DEFAULT_WATER_LEVEL_RATIO = 0.5f
        private const val DEFAULT_WAVE_LENGTH_RATIO = 1.0f
        private const val DEFAULT_WAVE_SHIFT_RATIO = 0.0f
        val DEFAULT_WAVE_SHAPE = ShapeType.CIRCLE
        val DEFAULT_FRONT_WAVE_COLOR = Color.parseColor("#3C89CFF0")
        val DEFAULT_BEHIND_WAVE_COLOR = Color.parseColor("#2889CFF0")
    }

    var shapeType = DEFAULT_WAVE_SHAPE
        set(shapeType) {
            field = shapeType
            invalidate()
        }
    var frontWaveColor = DEFAULT_FRONT_WAVE_COLOR
    var borderWidth = 10
    var behindWaveColor = DEFAULT_BEHIND_WAVE_COLOR
    var borderColor = Color.parseColor("#4489CFF0")
    private var mAnimatorSet: AnimatorSet? = null
    private val initAnimation = {
        val animators: MutableList<Animator> = mutableListOf()
        ObjectAnimator.ofFloat(this, "amplitudeRatio", 0.0001f, 0.05f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = 5000
            interpolator = LinearInterpolator()
        }.let { amplitudeAnim -> animators.add(amplitudeAnim) }
        ObjectAnimator.ofFloat(this, "waveShiftRatio", 0f, 1f).apply {
            repeatCount = ValueAnimator.INFINITE
            duration = 1000
            interpolator = LinearInterpolator()
        }.let { waveShiftAnim -> animators.add(waveShiftAnim) }
        ObjectAnimator.ofFloat(this, "waterLevelRatio", 0f, 0.5f).apply {
            duration = 10000
            interpolator = DecelerateInterpolator()
        }.let { waterLevelAnim -> animators.add(waterLevelAnim) }
        mAnimatorSet = AnimatorSet()
        mAnimatorSet?.playTogether(animators)
    }
    val start = {
        isShowWave = true
        mAnimatorSet?.start()
    }
    val cancel = mAnimatorSet?.end()
    private fun init(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Wave)
        try {
            typedArray.run {
                shapeType = when (getInt(R.styleable.Wave_WaveShapeType, 0)) {
                    0 -> ShapeType.CIRCLE
                    1 -> ShapeType.SQUARE
                    else -> ShapeType.SQUARE
                }
                frontWaveColor =
                    getColor(R.styleable.Wave_WaveColor, Color.parseColor("#89CFF0"))
                borderWidth =
                    getDimension(R.styleable.Wave_WaveBorder, borderWidth * 1f).toInt()
            }
        } finally {
            typedArray.recycle()
        }
        behindWaveColor = ImageKit.setColorAlphaByInt(frontWaveColor, 40)
        setWaveColor(behindWaveColor, frontWaveColor)
        borderColor = ImageKit.setColorAlphaByInt(frontWaveColor, 68)
        setBorder(borderWidth, borderColor)
        initAnimation
        start
    }

    private var mDefaultAngularFrequency = 0.0
    private var mDefaultAmplitude = 0f
    private var mDefaultWaterLevel = 0f
    private var mDefaultWaveLength = 0f
    private var mWaveShader: BitmapShader? = null
    private var mViewPaint: Paint = Paint().apply { isAntiAlias = true }
    private val createShader = {
        mDefaultAngularFrequency = 2.0f * Math.PI / DEFAULT_WAVE_LENGTH_RATIO / width
        mDefaultAmplitude = height * DEFAULT_AMPLITUDE_RATIO
        mDefaultWaterLevel = height * DEFAULT_WATER_LEVEL_RATIO
        mDefaultWaveLength = width.toFloat()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val wavePaint = Paint().apply {
            strokeWidth = 2f
            isAntiAlias = true
        }
        val endX = width + 1
        val endY = height + 1
        val waveY = FloatArray(endX)
        wavePaint.color = behindWaveColor
        for (beginX in 0 until endX) {
            val beginY =
                (mDefaultWaterLevel + mDefaultAmplitude * sin(beginX * mDefaultAngularFrequency)).toFloat()
            canvas.drawLine(beginX.toFloat(), beginY, beginX.toFloat(), endY.toFloat(), wavePaint)
            waveY[beginX] = beginY
        }
        wavePaint.color = frontWaveColor
        val wave2Shift = (mDefaultWaveLength / 4).toInt()
        for (beginX in 0 until endX) {
            canvas.drawLine(
                beginX.toFloat(), waveY[(beginX + wave2Shift) % endX],
                beginX.toFloat(), endY.toFloat(), wavePaint
            )
        }
        mWaveShader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP)
        mViewPaint.shader = mWaveShader
    }

    fun setWaveColor(behindWaveColor: Int, frontWaveColor: Int) {
        this.behindWaveColor = behindWaveColor
        this.frontWaveColor = frontWaveColor
        if (width > 0 && height > 0) {
            mWaveShader = null
            createShader
            invalidate()
        }
    }

    private var mBorderPaint: Paint? = null
    fun setBorder(color: Int, width: Int) {
        if (mBorderPaint == null) mBorderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }
        borderColor = color
        borderWidth = width
        mBorderPaint?.apply {
            this.color = borderColor
            strokeWidth = borderWidth.toFloat()
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createShader
    }

    var isShowWave = false
    private var mShaderMatrix: Matrix = Matrix()
    var waveLengthRatio = DEFAULT_WAVE_LENGTH_RATIO//波长
    var amplitudeRatio = DEFAULT_AMPLITUDE_RATIO
        set(amplitudeRatio) {
            if (field != amplitudeRatio) {
                field = amplitudeRatio
                invalidate()
            }
        }//振幅
    var waveShiftRatio = DEFAULT_WAVE_SHIFT_RATIO
        set(waveShiftRatio) {
            if (field != waveShiftRatio) {
                field = waveShiftRatio
                invalidate()
            }
            field = waveShiftRatio
        }
    var waterLevelRatio = DEFAULT_WATER_LEVEL_RATIO
        set(waterLevelRatio) {
            if (field != waterLevelRatio) {
                field = waterLevelRatio
                invalidate()
            }
        }//水位

    override fun onDraw(canvas: Canvas) {
        when {
            isShowWave && mWaveShader != null -> {
                if (mViewPaint.shader == null) mViewPaint.shader = mWaveShader
                mShaderMatrix.setScale(
                    waveLengthRatio / DEFAULT_WAVE_LENGTH_RATIO,
                    amplitudeRatio / DEFAULT_AMPLITUDE_RATIO, 0f, mDefaultWaterLevel
                )
                mShaderMatrix.postTranslate(
                    waveShiftRatio * width, (DEFAULT_WATER_LEVEL_RATIO - waterLevelRatio) * height
                )
                mWaveShader?.setLocalMatrix(mShaderMatrix)
                val borderWidth = mBorderPaint?.strokeWidth ?: 0f
                when (shapeType) {
                    ShapeType.CIRCLE -> {
                        if (borderWidth > 0) mBorderPaint?.let {
                            canvas.drawCircle(
                                width / 2f, height / 2f, (width - borderWidth) / 2f - 1f, it
                            )
                        }
                        canvas.drawCircle(
                            width / 2f, height / 2f, width / 2f - borderWidth, mViewPaint
                        )
                    }
                    ShapeType.SQUARE -> {
                        if (borderWidth > 0) mBorderPaint?.let {
                            canvas.drawRect(
                                borderWidth / 2f, borderWidth / 2f,
                                width - borderWidth / 2f - 0.5f,
                                height - borderWidth / 2f - 0.5f, it
                            )
                        }
                        canvas.drawRect(
                            borderWidth, borderWidth, width - borderWidth,
                            height - borderWidth, mViewPaint
                        )
                    }
                }
            }
            else -> mViewPaint.shader = null
        }
    }
}