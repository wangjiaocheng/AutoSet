package top.autoget.autosee

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import top.autoget.autokit.DensityKit.dip2px
import kotlin.math.min

class ProgressView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr), Runnable {
    init {
        initAttrs(attrs)
    }

    private var stopColor: Int = 0
    private var pauseColor: Int = 0
    private var finishColor: Int = 0
    private var loadingColor: Int = 0
    private var mTextSize: Int = 12
    private var borderWidth: Int = 1
    private var radius: Int = 0
    private fun initAttrs(attrs: AttributeSet?) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressView)
        try {
            typedArray.run {
                stopColor =
                    getColor(R.styleable.ProgressView_stopColor, Color.parseColor("#ff9800"))
                pauseColor =
                    getColor(R.styleable.ProgressView_stopColor, Color.parseColor("#ff9800"))
                finishColor =
                    getColor(R.styleable.ProgressView_finishColor, Color.parseColor("#3CB371"))
                loadingColor =
                    getColor(R.styleable.ProgressView_loadingColor, Color.parseColor("#40c4ff"))
                mTextSize = getDimension(R.styleable.ProgressView_PbTextSize, 12f).toInt()
                borderWidth = getDimension(R.styleable.ProgressView_borderWidth, 1f).toInt()
                radius = getDimension(R.styleable.ProgressView_radius, 0f).toInt()
            }
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec), when (MeasureSpec.getMode(heightMeasureSpec)) {
                MeasureSpec.AT_MOST -> dip2px(35f)
                MeasureSpec.EXACTLY, MeasureSpec.UNSPECIFIED ->
                    MeasureSpec.getSize(heightMeasureSpec)
                else -> 0
            }
        )
        if (pgBitmap == null) init()
    }

    private var bgPaint: Paint? = null
    private var pgPaint: Paint? = null
    private var textPaint: Paint? = null
    private var textRect: Rect? = null
    private var bgRectF: RectF? = null
    private var progressColor: Int = 0//边框、进度条、进度文本颜色
    private var flickerBitmap: Bitmap? = null//左右来回移动滑块
    private var flickerLeft: Float = 0f//滑块移动最左位置，控制移动
    private fun init() {
        bgPaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG
            style = Paint.Style.STROKE
            strokeWidth = borderWidth.toFloat()
        }
        pgPaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            style = Paint.Style.FILL
        }
        textPaint = Paint().apply {
            flags = Paint.ANTI_ALIAS_FLAG
            textSize = mTextSize.toFloat()
        }
        textRect = Rect()
        bgRectF = RectF(
            borderWidth.toFloat(), borderWidth.toFloat(),
            (measuredWidth - borderWidth).toFloat(), (measuredHeight - borderWidth).toFloat()
        )
        progressColor = if (isStop) pauseColor else loadingColor
        flickerBitmap = BitmapFactory.decodeResource(resources, R.mipmap.flicker)
        flickerLeft = -(flickerBitmap?.width ?: 0).toFloat()
        initPgBitmap()
    }

    private var pgBitmap: Bitmap? = null//进度条bitmap含滑块
    private var pgCanvas: Canvas? = null
    private var thread: Thread? = null
    private fun initPgBitmap() {
        pgCanvas = Canvas(
            Bitmap.createBitmap(
                measuredWidth - borderWidth, measuredHeight - borderWidth,
                Bitmap.Config.ARGB_8888
            ).apply { pgBitmap = this })
        Thread(this).apply { thread = this }.start()
    }

    private val maxProgress = 100f
    override fun run() = try {
        while (!isStop && !thread!!.isInterrupted) {
            if (flickerLeft + dip2px(5f).toFloat() >= progress / maxProgress * measuredWidth)
                flickerLeft = -(flickerBitmap?.width ?: 0).toFloat()
            postInvalidate()
            Thread.sleep(20)
        }
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBackGround(canvas)
        drawProgress(canvas)
        drawProgressText(canvas)
        drawProgressTextColor(canvas)
    }

    private fun drawBackGround(canvas: Canvas) = bgPaint?.run {
        bgRectF?.let {
            canvas.drawRoundRect(
                it, radius.toFloat(), radius.toFloat(), apply { color = progressColor })
        }
    }//left、top、right、bottom不贴控件边，否则border一半绘在控件内，圆角线条显粗

    private var bitmapShader: BitmapShader? = null
    private val porterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    private fun drawProgress(canvas: Canvas) {
        pgCanvas?.run {
            save()
            clipRect(
                0f, 0f, progress / maxProgress * measuredWidth, measuredHeight.toFloat()
            )
            drawColor(progressColor)
            restore()
        }
        pgPaint?.apply {
            color = progressColor
            if (!isStop) {
                xfermode = porterDuffXfermode
                flickerBitmap?.let { pgCanvas?.drawBitmap(it, flickerLeft, 0f, pgPaint) }
                xfermode = null
            }
            shader = pgBitmap?.let {
                BitmapShader(
                    it, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
                ).apply { bitmapShader = this }
            }
        }
        bgRectF?.let {
            pgPaint?.let { it1 ->
                canvas.drawRoundRect(it, radius.toFloat(), radius.toFloat(), it1)
            }
        }
    }

    private var progressText: String = getProgressText()
    private fun getProgressText(): String = when {
        isFinish -> "下载完成"
        isStop -> "继续"
        else -> "下载中$progress%"
    }

    private fun drawProgressText(canvas: Canvas) {
        textPaint?.apply {
            color = progressColor
            getTextBounds(progressText, 0, progressText.length, textRect)
        }
        textPaint?.let {
            canvas.drawText(
                progressText, ((measuredWidth - textRect!!.width()) / 2).toFloat(),
                ((measuredHeight + textRect!!.height()) / 2).toFloat(), it
            )
        }
    }

    private fun drawProgressTextColor(canvas: Canvas) = textRect?.width()?.let { tWidth ->
        ((measuredWidth - tWidth) / 2).toFloat().let { xCoordinate ->
            (progress / maxProgress * measuredWidth).let { progressWidth ->
                if (progressWidth > xCoordinate) canvas.run {
                    save()
                    clipRect(
                        xCoordinate, 0f,
                        min(progressWidth, xCoordinate + tWidth * 1.1f), measuredHeight.toFloat()
                    )
                    textPaint?.apply { color = Color.WHITE }?.let {
                        drawText(
                            progressText, xCoordinate,
                            ((measuredHeight + textRect!!.height()) / 2).toFloat(), it
                        )
                    }
                    restore()
                }
            }
        }
    }

    var isStop: Boolean = false
        private set
    var isFinish: Boolean = false
        private set
    val finishLoad = {
        isFinish = true
        setFinish(true)
    }

    private fun setFinish(stop: Boolean) {
        isStop = stop
        when {
            isStop -> {
                progressColor = finishColor
                thread?.interrupt()
            }
            else -> {
                progressColor = loadingColor
                Thread(this).apply { thread = this }.start()
            }
        }
        invalidate()
    }

    var progress: Float = 0f
        set(progress) {
            if (!isStop) {
                field = if (progress < maxProgress) progress else maxProgress.apply { finishLoad }
                invalidate()
            }
        }
    val resetProgress = {
        setStop(true)
        isStop = false
        isFinish = false
        progress = 0f
        progressColor = loadingColor
        progressText = ""
        flickerLeft = -(flickerBitmap?.width ?: 0).toFloat()
        initPgBitmap()
    }
    val toggle = if (isFinish) Unit else (if (isStop) setStop(false) else setStop(true))
    fun setStop(stop: Boolean) {
        isStop = stop
        when {
            isStop -> {
                progressColor = pauseColor
                thread?.interrupt()
            }
            else -> {
                progressColor = loadingColor
                Thread(this).apply { thread = this }.start()
            }
        }
        invalidate()
    }
}