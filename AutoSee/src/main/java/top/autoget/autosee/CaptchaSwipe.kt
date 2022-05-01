package top.autoget.autosee

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.debug
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class CaptchaSwipe
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr), LoggerKit {
    init {
        init(context, attrs, defStyleAttr)
    }

    private var captchaWidth: Int = 0//滑块宽高
    private var captchaHeight: Int = 0
    private var matchDeviation: Float = 0f//验证误差允许值
    private var mWidth: Int = 0//控件宽高
    private var mHeight: Int = 0
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val typedArray: TypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SwipeCaptcha, defStyleAttr, 0)
        try {
            typedArray.run {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, resources.displayMetrics)
                    .let { defaultSize ->
                        for (i in 0 until indexCount) {
                            when (val attr = getIndex(i)) {
                                R.styleable.SwipeCaptcha_captchaWidth ->
                                    captchaWidth = getDimension(attr, defaultSize).toInt()
                                R.styleable.SwipeCaptcha_captchaHeight ->
                                    captchaHeight = getDimension(attr, defaultSize).toInt()
                                R.styleable.SwipeCaptcha_matchDeviation ->
                                    matchDeviation = getDimension(
                                        attr, TypedValue.applyDimension(
                                            TypedValue.COMPLEX_UNIT_DIP, 3f,
                                            resources.displayMetrics
                                        )
                                    )
                            }
                        }
                    }
            }
        } finally {
            typedArray.recycle()
        }
        mWidth = captchaWidth
        mHeight = captchaHeight
    }

    val maxSwipeValue: Int
        get() = mWidth - captchaWidth//最大可滑动值

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w
        mHeight = h
        createMatchAnim()//动画区域会用到宽高
        post { createCaptcha() }
    }

    private var successPaint = Paint()
    private var successPath = Path()
    private var failAnim = ValueAnimator()//失败闪烁动画

    interface OnCaptchaMatchCallback {
        fun matchSuccess(captchaSwipe: CaptchaSwipe)
        fun matchFailed(captchaSwipe: CaptchaSwipe)
    }

    var onCaptchaMatchCallback: OnCaptchaMatchCallback? = null
    private var successAnim = ValueAnimator()//成功白光扫过动画
    private var successAnimOffset: Int = 0//成功动画偏移
    private var isSuccessAnimShow: Boolean = false
    private fun createMatchAnim() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 100f, resources.displayMetrics
    ).toInt().let { width ->
        successPaint.apply {
            shader = LinearGradient(
                0f, 0f, (width / 2 * 3).toFloat(), mHeight.toFloat(),
                intArrayOf(0x00ffffff, -0x77000001), floatArrayOf(0f, 0.5f), Shader.TileMode.MIRROR
            )
        }
        successPath.apply {
            moveTo(0f, 0f)
            rLineTo(width.toFloat(), 0f)
            rLineTo((width / 2).toFloat(), mHeight.toFloat())
            rLineTo(-width.toFloat(), 0f)
            close()
        }//平行四边形滚动
        failAnim.apply {
            setFloatValues(0f, 1f)
            duration = 100
            repeatCount = 4
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animation ->
                isDrawMask = (animation.animatedValue as Float)
                    .apply { debug("${loggerTag}onAnimationUpdate: $this") } >= 0.5f
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onCaptchaMatchCallback?.matchFailed(this@CaptchaSwipe)
                }
            })
        }
        successAnim.apply {
            setIntValues(mWidth + width, 0)
            duration = 500
            interpolator = FastOutLinearInInterpolator()
            addUpdateListener { animation ->
                successAnimOffset = animation.animatedValue as Int
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    isSuccessAnimShow = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    onCaptchaMatchCallback?.matchSuccess(this@CaptchaSwipe)
                    isSuccessAnimShow = false
                    isMatchMode = false
                }
            })
        }
    }//验证动画初始化区域

    fun createCaptcha() = drawable?.let {
        resetFlags()//重置一些flags，开启验证模式
        createCaptchaPath()//生成验证码Path
        createMask()//生成滑块
        invalidate()//抠图
    }

    private var isMatchMode: Boolean = false//是否验证模式：验证成功后false，其余情况true
    private fun resetFlags() {
        isMatchMode = true
    }

    private var random = Random(System.nanoTime())
    private var captchaX: Int = 0//验证码阴影左上角起点
    private var captchaY: Int = 0
    private var captchaPath = Path()//验证码阴影抠图的Path
    private fun createCaptchaPath() = (captchaWidth / 3).let { gap ->
        captchaX = random.nextInt(abs(mWidth - captchaWidth - gap))
        captchaY = random.nextInt(abs(mHeight - captchaHeight - gap))
        debug("${loggerTag}createCaptchaPath() called mWidth:$mWidth, mHeight:$mHeight, captchaX:$captchaX, captchaY:$captchaY")
        captchaPath.run {
            reset()
            lineTo(0f, 0f)
            moveTo(captchaX.toFloat(), captchaY.toFloat())//左上角
/*            lineTo((captchaX + gap).toFloat(), captchaY.toFloat())
            (captchaWidth / 2 - gap).let { radius ->
                arcTo(
                    RectF(
                        (captchaX + gap).toFloat(), (captchaY - radius).toFloat(),
                        (captchaX + gap + radius * 2).toFloat(), (captchaY + radius).toFloat()
                    ), 180f, 180f
                )*/
            lineTo((captchaX + gap).toFloat(), captchaY.toFloat())
            drawPartCircle(
                PointF((captchaX + gap).toFloat(), captchaY.toFloat()),
                PointF((captchaX + gap * 2).toFloat(), captchaY.toFloat()),
                this, random.nextBoolean()
            )//draw一个随机凹凸的圆
            lineTo((captchaX + captchaWidth).toFloat(), captchaY.toFloat())//右上角
            lineTo((captchaX + captchaWidth).toFloat(), (captchaY + gap).toFloat())
            drawPartCircle(
                PointF((captchaX + captchaWidth).toFloat(), (captchaY + gap).toFloat()),
                PointF((captchaX + captchaWidth).toFloat(), (captchaY + gap * 2).toFloat()),
                this, random.nextBoolean()
            )//draw一个随机凹凸的圆
            lineTo(
                (captchaX + captchaWidth).toFloat(), (captchaY + captchaHeight).toFloat()
            )//右下角
            lineTo(
                (captchaX + captchaWidth - gap).toFloat(), (captchaY + captchaHeight).toFloat()
            )
            drawPartCircle(
                PointF(
                    (captchaX + captchaWidth - gap).toFloat(),
                    (captchaY + captchaHeight).toFloat()
                ),
                PointF(
                    (captchaX + captchaWidth - gap * 2).toFloat(),
                    (captchaY + captchaHeight).toFloat()
                ), this, random.nextBoolean()
            )//draw一个随机凹凸的圆
            lineTo(captchaX.toFloat(), (captchaY + captchaHeight).toFloat())//左下角
            lineTo(captchaX.toFloat(), (captchaY + captchaHeight - gap).toFloat())
            drawPartCircle(
                PointF(
                    captchaX.toFloat(), (captchaY + captchaHeight - gap).toFloat()
                ),
                PointF(captchaX.toFloat(), (captchaY + captchaHeight - gap * 2).toFloat()),
                this, random.nextBoolean()
            )//draw一个随机凹凸的圆
            close()
/*                addArc(
                    RectF(
                        (captchaX + gap).toFloat(), (captchaY - radius).toFloat(),
                        (captchaX + gap + radius * 2).toFloat(), (captchaY + radius).toFloat()
                    ), 180f, 180f
                )
                lineTo((captchaX + captchaWidth).toFloat(), captchaY.toFloat())
                lineTo((captchaX + captchaWidth).toFloat(), (captchaY + gap).toFloat())//凹要多次move
                addArc(
                    RectF(
                        (captchaX + captchaWidth - radius).toFloat(),
                        (captchaY + gap).toFloat(),
                        (captchaX + captchaWidth + radius).toFloat(),
                        (captchaY + gap + radius * 2).toFloat()
                    ), 90f, 180f
                )
                moveTo((captchaX + captchaWidth).toFloat(), (captchaY + gap + radius * 2).toFloat())
            }
            lineTo((captchaX + captchaWidth).toFloat(), (captchaY + captchaHeight).toFloat())
            lineTo(captchaX.toFloat(), (captchaY + captchaHeight).toFloat())
            close()*/
        }
    }//random.nextInt(captchaWidth / 2)

    private fun drawPartCircle(start: PointF, end: PointF, path: Path, outer: Boolean) =
        PointF(start.x + (end.x - start.x) / 2, start.y + (end.y - start.y) / 2).let { middle ->
            sqrt(
                (middle.x - start.x).toDouble().pow(2.0) + (middle.y - start.y).toDouble().pow(2.0)
            ).toFloat()
                .let { radius ->
                    (radius * 0.551915024494f).let { gap ->
                        when (start.x) {
                            end.x -> (if (end.y - start.y > 0) 1 else -1).let { flag ->
                                when {
                                    outer -> path.run {
                                        cubicTo(
                                            start.x + gap * flag, start.y,
                                            middle.x + radius * flag, middle.y - gap * flag,
                                            middle.x + radius * flag, middle.y
                                        )
                                        cubicTo(
                                            middle.x + radius * flag, middle.y + gap * flag,
                                            end.x + gap * flag, end.y, end.x, end.y
                                        )
                                    }//凸的两个半圆
                                    else -> path.run {
                                        cubicTo(
                                            start.x - gap * flag, start.y,
                                            middle.x - radius * flag, middle.y - gap * flag,
                                            middle.x - radius * flag, middle.y
                                        )
                                        cubicTo(
                                            middle.x - radius * flag, middle.y + gap * flag,
                                            end.x - gap * flag, end.y, end.x, end.y
                                        )
                                    }//凹的两个半圆
                                }
                            }//绘制竖直方向，是否从上到下，旋转系数
                            else -> (if (end.x - start.x > 0) 1 else -1).let { flag ->
                                when {
                                    outer -> path.run {
                                        cubicTo(
                                            start.x, start.y - gap * flag,
                                            middle.x - gap * flag, middle.y - radius * flag,
                                            middle.x, middle.y - radius * flag
                                        )
                                        cubicTo(
                                            middle.x + gap * flag, middle.y - radius * flag,
                                            end.x, end.y - gap * flag, end.x, end.y
                                        )
                                    }//凸的两个半圆
                                    else -> path.run {
                                        cubicTo(
                                            start.x, start.y + gap * flag,
                                            middle.x - gap * flag, middle.y + radius * flag,
                                            middle.x, middle.y + radius * flag
                                        )
                                        cubicTo(
                                            middle.x + gap * flag, middle.y + radius * flag,
                                            end.x, end.y + gap * flag, end.x, end.y
                                        )
                                    }//凹的两个半圆
                                }//绘制水平方向，是否从左到右，旋转系数
                            }
                        }
                    }
                }
        }//半圆绘制path上

    private var bitmapMask: Bitmap? = null//滑块位图
    private var bitmapMaskShadow: Bitmap? = null//滑块阴影
    private var dragerOffset: Int = 0//滑块位移
    private var isDrawMask: Boolean = false//是否绘制滑块，用于验证失败闪烁动画
    private fun createMask() {
        bitmapMask = getMaskBitmap((drawable as BitmapDrawable).bitmap, captchaPath)
        bitmapMaskShadow = bitmapMask?.extractAlpha()
        dragerOffset = 0//位移重置
        isDrawMask = true
    }

    private var paintMask = Paint().apply { flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG }
    private var porterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)//滑块区域
    private fun getMaskBitmap(bitmap: Bitmap, mask: Path): Bitmap =
        Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888).apply {
            error("$loggerTag getMaskBitmap: width:${bitmap.width},  height:${bitmap.height}")
            error("$loggerTag View: width:$mWidth,  height:$mHeight")
            Canvas(this).apply {
                //clipPath(mask)//有锯齿且无法解决，换成XFermode的方法做
                drawFilter =
                    PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)//抗锯齿
            }.run {
                drawPath(mask, paintMask)//绘制遮罩圆形
                drawBitmap(bitmap, imageMatrix, paintMask.apply {
                    xfermode = porterDuffXfermode//设置遮罩模式：图像混合
                    //setShadowLayer(5f, 3f, 3f, 0xFF0000FF.toInt())
                    maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.SOLID)
                    //maskFilter = EmbossMaskFilter(floatArrayOf(1f, 1f, 1f), 1f, 6f, 3.5f)//光源方向、环境亮度、反射等级、模糊
                })//考虑到scaleType等因素，Matrix缩放Bitmap
            }
            paintMask.xfermode = null
        }

    private var paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG
        color = 0x77000000
        style = Paint.Style.STROKE
        maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.SOLID)//设置画笔遮罩滤镜
    }
    private var paintMaskShadow = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG
        color = Color.BLACK
/*        textSize = 50f
        strokeWidth = 50f
        style = Paint.Style.FILL_AND_STROKE*/
        maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.SOLID)
    }//绘制阴影Paint

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isMatchMode) {
            canvas.drawPath(captchaPath, paint)//绘制验证码阴影
            bitmapMaskShadow?.let { it0 ->
                bitmapMask?.let { it1 ->
                    if (isDrawMask) canvas.run {
                        drawBitmap(it0, (dragerOffset - captchaX).toFloat(), 0f, paintMaskShadow)
                        drawBitmap(it1, (dragerOffset - captchaX).toFloat(), 0f, null)
                    }//先绘制阴影
                }
            }
            if (isSuccessAnimShow) canvas.run {
                translate(successAnimOffset.toFloat(), 0f)
                drawPath(successPath, successPaint)
            }
        }
    }

    fun matchCaptcha() = onCaptchaMatchCallback?.run {
        if (isMatchMode) when {
            abs(dragerOffset - captchaX) < matchDeviation -> {
                debug("${loggerTag}matchCaptcha() true: dragerOffset:$dragerOffset, captchaX:$captchaX")
                matchSuccess(this@CaptchaSwipe)
                successAnim.start()
            }
            else -> {
                error("${loggerTag}matchCaptcha() false: dragerOffset:$dragerOffset, captchaX:$captchaX")
                matchFailed(this@CaptchaSwipe)
                failAnim.start()
            }
        }
    }

    fun resetCaptcha() {
        dragerOffset = 0
        invalidate()
    }

    fun setCurrentSwipeValue(value: Int) {
        dragerOffset = value
        invalidate()
    }
}