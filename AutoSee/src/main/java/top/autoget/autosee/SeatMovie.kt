package top.autoget.autosee

import android.animation.Animator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.DecelerateInterpolator
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.DensityKit.dip2px
import top.autoget.autokit.HandleKit.backgroundHandler
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.ToastKit.showShort
import top.autoget.autokit.debug
import java.util.*
import kotlin.math.abs

class SeatMovie
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr), LoggerKit {
    init {
        initData(context, attrs)
    }

    private var seatResIDSold: Int = R.mipmap.seat_sold
    private var seatResIDChecked: Int = R.mipmap.seat_green
    private var seatResIDAvailable: Int = R.mipmap.seat_gray
    private var overviewSold: Int = Color.RED
    private var overviewChecked: Int = Color.parseColor("#5A9E64")
    private var colorTxt: Int = Color.WHITE
    private fun initData(context: Context, attrs: AttributeSet?) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SeatMovie)
        try {
            typedArray.run {
                seatResIDSold = getResourceId(R.styleable.SeatMovie_seat_sold, R.mipmap.seat_sold)
                seatResIDChecked =
                    getResourceId(R.styleable.SeatMovie_seat_checked, R.mipmap.seat_green)
                seatResIDAvailable =
                    getResourceId(R.styleable.SeatMovie_seat_available, R.mipmap.seat_gray)
                overviewSold = getColor(R.styleable.SeatMovie_overview_sold, Color.RED)
                overviewChecked =
                    getColor(R.styleable.SeatMovie_overview_checked, Color.parseColor("#5A9E64"))
                colorTxt = getColor(R.styleable.SeatMovie_txt_color, Color.WHITE)
            }
        } finally {
            typedArray.recycle()
        }
    }

    private val seatBitmapSold: Bitmap = BitmapFactory.decodeResource(resources, seatResIDSold)
    private val seatBitmapChecked: Bitmap =
        BitmapFactory.decodeResource(resources, seatResIDChecked)
    private val seatBitmapAvailable: Bitmap =
        BitmapFactory.decodeResource(resources, seatResIDAvailable)
    private val defaultImgW = 40f//默认座位图宽度，使用自己座位图比此尺寸大或小，缩放到此大小
    private val defaultImgH = 34f//默认座位图高度
    private var xScale1 = 1f
    private var yScale1 = 1f
    private var seatWidth: Int = 0//座位图片宽度
    private var seatHeight: Int = 0//座位图片高度
    var numRow: Int = 0
    var numColumn: Int = 0
    private val spacing: Int = dip2px(5f)//座位水平间距
    private val spacingVer: Int = dip2px(10f)//座位垂直间距
    private var seatBitmapWidth: Int = 0//整个座位图宽度
    private var seatBitmapHeight: Int = 0//整个座位图高度
    private val overviewScale = 4.8f//概览图比例
    private var rectWidth: Float = 0f//概览图白色方块宽度
    private var rectHeight: Float = 0f//概览图白色方块高度
    private var overviewSpacing: Float = 0f//概览图上方块水平间距
    private var overviewSpacingVer: Float = 0f//概览图上方块垂直间距
    private var rectW: Float = 0f//整个概览图宽度
    private var rectH: Float = 0f//整个概览图高度
    private var overviewBitmap: Bitmap? = null
    private val numberWidth: Int = dip2px(20f)//行号宽度
    private val headHeight: Float = dip2px(30f).toFloat()//顶部高度（可选、已选、已售区域高度）
    private val screenHeight: Float = dip2px(20f).toFloat()//荧幕高度
    private val matrixTool = Matrix()
    private var lineNumberTxtHeight: Float = 0f
    private var lineNumberPaintFontMetrics: Paint.FontMetrics? = null
    var lineNumbers: MutableList<String> = mutableListOf()
        set(lineNumbers) {
            field = lineNumbers
            invalidate()
        }
    private val initData = {
        xScale1 = defaultImgW / seatBitmapAvailable.width
        yScale1 = defaultImgH / seatBitmapAvailable.height
        seatWidth = (seatBitmapAvailable.width * xScale1).toInt()
        seatHeight = (seatBitmapAvailable.height * yScale1).toInt()
        seatBitmapWidth =
            (numColumn.toFloat() * seatBitmapAvailable.width.toFloat() * xScale1 + (numColumn - 1) * spacing).toInt()
        seatBitmapHeight =
            (numRow.toFloat() * seatBitmapAvailable.height.toFloat() * yScale1 + (numRow - 1) * spacingVer).toInt()
        rectWidth = seatWidth / overviewScale
        rectHeight = seatHeight / overviewScale
        overviewSpacing = spacing / overviewScale
        overviewSpacingVer = spacingVer / overviewScale
        rectW = numColumn * rectWidth + (numColumn - 1) * overviewSpacing + overviewSpacing * 2
        rectH = numRow * rectHeight + (numRow - 1) * overviewSpacingVer + overviewSpacingVer * 2
        overviewBitmap = Bitmap.createBitmap(rectW.toInt(), rectH.toInt(), Bitmap.Config.ARGB_4444)
        matrixTool.postTranslate(
            (numberWidth + spacing).toFloat(),
            headHeight + screenHeight + borderHeight + spacingVer
        )
        lineNumberTxtHeight = paintLineNumber.measureText("4")
        lineNumberPaintFontMetrics = paintLineNumber.fontMetrics
        lineNumbers.run {
            if (size <= 0) for (i in 0 until numRow) {
                add("${i + 1}")
            }
        }
    }
    val setData = {
        initData
        invalidate()
    }//先设置numRow、numColumn
    val selectedSeat: MutableList<String>
        get() = mutableListOf<String>().apply {
            for (i in 0 until numRow) {
                for (j in 0 until numColumn) {
                    if (isHave(getID(i, j)) >= 0) add("$i,$j")
                }
            }
        }

    interface SeatChecker {
        fun isValidSeat(row: Int, column: Int): Boolean//是否可用座位
        fun isSold(row: Int, column: Int): Boolean//是否已售
        fun checked(row: Int, column: Int)
        fun unCheck(row: Int, column: Int)
        fun checkedSeatTxt(row: Int, column: Int): Array<String>?
    }//获取选中后座位上显示的文字，返回2个元素的数组，第一个元素是第一行的文字，第二个元素是第二行文字，如果只返回一个元素则会绘制到座位图的中间位置。

    var seatChecker: SeatChecker? = null
        set(seatChecker) {
            field = seatChecker
            invalidate()
        }

    fun getRowNumber(row: Int): Int = seatChecker?.run {
        var result = row
        for (i in 0 until row) {
            for (j in 0 until numColumn) {
                if (isValidSeat(i, j)) break
                if (j == numColumn - 1) if (i == row) return -1 else result--
            }
        }
        result
    } ?: -1

    fun getColumnNumber(row: Int, column: Int): Int = seatChecker?.run {
        var result = column
        for (i in 0 until column) {
            if (!isValidSeat(row, i)) if (i == column) return -1 else result--
        }
        result
    } ?: -1

    private var bitmapHead: Bitmap? = null
    var isDrawOverview = false//是否绘制概览图
    var isRenewOverview = true//是否更新概览图
    var isDebug = false
    override fun onDraw(canvas: Canvas) {
        if (numRow > 0 && numColumn != 0) {
            val totalStart = nowMillis
            drawSeat(canvas)
            drawNumber(canvas)
            canvas.drawBitmap(
                bitmapHead
                    ?: drawHeadInfo().apply { bitmapHead = this }, 0f, 0f, null
            )
            drawScreen(canvas)
            if (isDrawOverview) {
                val overviewStart = nowMillis
                if (isRenewOverview) drawOverview()
                overviewBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
                drawOverview(canvas)
                if (isDebug) debug("OverviewDrawTime:${nowMillis - overviewStart}")
            }
            if (isDebug) debug("TotalDrawTime:${nowMillis - totalStart}")
        }
    }

    private val matrixValues = FloatArray(9)
    private val matrixScaleX: Float
        get() = matrixValues[Matrix.MSCALE_X].apply { matrixTool.getValues(matrixValues) }
    private var zoom: Float = matrixScaleX
    private val translateY: Float
        get() = matrixValues[5].apply { matrixTool.getValues(matrixValues) }
    private val translateX: Float
        get() = matrixValues[2].apply { matrixTool.getValues(matrixValues) }

    companion object {
        private const val SEAT_TYPE_SOLD = 1//已售
        private const val SEAT_TYPE_SELECTED = 2//选中
        private const val SEAT_TYPE_AVAILABLE = 3//可选
        private const val SEAT_TYPE_NOT_AVAILABLE = 4//不可用
    }

    private val paint: Paint = Paint().apply { color = Color.RED }
    private fun drawSeat(canvas: Canvas) {
        val startTime = nowMillis
        for (i in 0 until numRow) {
            (i * zoom * (yScale1 * seatBitmapAvailable.height + spacingVer) + translateY).let { top ->
                (zoom * yScale1 * seatBitmapAvailable.height + top).let { bottom ->
                    if (top <= height && bottom >= 0) for (j in 0 until numColumn) {
                        (j * zoom * (xScale1 * seatBitmapAvailable.width + spacing) + translateX).let { left ->
                            (zoom * xScale1 * seatBitmapAvailable.width + left).let { right ->
                                if (left <= width && right >= 0) {
                                    tempMatrix.apply {
                                        setTranslate(left, top)
                                        postScale(xScale1, yScale1, left, top)
                                        postScale(zoom, zoom, left, top)
                                    }
                                    when (getSeatType(i, j)) {
                                        SEAT_TYPE_SOLD ->
                                            canvas.drawBitmap(seatBitmapSold, tempMatrix, paint)
                                        SEAT_TYPE_SELECTED -> {
                                            canvas.drawBitmap(seatBitmapChecked, tempMatrix, paint)
                                            drawText(canvas, i, j, top, left)
                                        }
                                        SEAT_TYPE_AVAILABLE -> canvas.drawBitmap(
                                            seatBitmapAvailable, tempMatrix, paint
                                        )
                                        SEAT_TYPE_NOT_AVAILABLE -> Unit
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isDebug) debug("seatDrawTime:${nowMillis - startTime}")
    }

    private fun getSeatType(row: Int, column: Int): Int = when {
        isHave(getID(row, column)) >= 0 -> SEAT_TYPE_SELECTED
        else -> {
            seatChecker?.run {
                when {
                    !isValidSeat(row, column) -> SEAT_TYPE_NOT_AVAILABLE
                    isSold(row, column) -> SEAT_TYPE_SOLD
                    else -> SEAT_TYPE_AVAILABLE
                }
            } ?: SEAT_TYPE_AVAILABLE
        }
    }

    private val selects: MutableList<Int> = mutableListOf()
    private fun isHave(seat: Int?): Int = Collections.binarySearch<Int>(selects, seat)
    private fun drawText(canvas: Canvas, row: Int, column: Int, top: Float, left: Float) {
        var txt0 = "${(row + 1)}排"
        var txt1: String? = "${(column + 1)}座"
        seatChecker?.checkedSeatTxt(row, column)?.let { strings ->
            if (strings.isNotEmpty()) {
                txt0 = strings[0]
                txt1 = if (strings.size == 1) null else strings[1]
            }
        }
        (seatHeight * matrixScaleX).let { seatHeight ->
            TextPaint().apply {
                flags = Paint.ANTI_ALIAS_FLAG
                color = colorTxt
                textSize = seatHeight / 3
                typeface = Typeface.DEFAULT_BOLD
            }.let { txtPaint ->
                (left + seatWidth * matrixScaleX / 2 - txtPaint.measureText(txt0) / 2).let { startX ->
                    canvas.run {
                        txt1?.let {
                            drawText(
                                txt0, startX,
                                getBaseLine(txtPaint, top, top + seatHeight / 2), txtPaint
                            )
                            drawText(
                                it, startX,
                                getBaseLine(txtPaint, top + seatHeight / 2, top + seatHeight),
                                txtPaint
                            )
                        } ?: drawText(
                            txt0, startX,
                            getBaseLine(txtPaint, top, top + seatHeight), txtPaint
                        )
                    }
                }
            }
        }
        if (isDebug) debug("drawText:top:$top")
    }//选中座位行号列号

    private fun getBaseLine(paint: Paint, top: Float, bottom: Float): Float =
        paint.fontMetrics.let { (bottom + top - it.bottom - it.top) / 2 }

    private val rectF: RectF = RectF()
    private val matrixScaleY: Float
        get() = matrixValues[4].apply { matrixTool.getValues(matrixValues) }
    private val paintLineNumber: Paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        textSize = dip2px(16f).toFloat()
        textAlign = Paint.Align.CENTER
    }
    private val bacColor: Int = Color.parseColor("#7e000000")
    private fun drawNumber(canvas: Canvas) {
        val startTime = nowMillis
        canvas.run {
            drawRoundRect(rectF.apply {
                left = 0f
                top = translateY - lineNumberTxtHeight / 2
                right = numberWidth.toFloat()
                bottom = translateY + lineNumberTxtHeight / 2 + matrixScaleY * seatBitmapHeight
            }, numberWidth / 2f, numberWidth / 2f, paintLineNumber.apply { color = bacColor })
            for (i in 0 until numRow) {
                val top = i * (seatHeight + spacingVer) * matrixScaleY + translateY
                val bottom =
                    (i * (seatHeight + spacingVer) + seatHeight) * matrixScaleY + translateY
                val baseline = lineNumberPaintFontMetrics
                    ?.let { (top + bottom - it.bottom - it.top) / 2 } ?: 0f
                drawText(
                    lineNumbers[i], numberWidth / 2f, baseline,
                    paintLineNumber.apply { color = Color.WHITE })
            }
        }
        if (isDebug) debug("drawNumberTime:${nowMillis - startTime}")
    }//行号

    private val paintHead: Paint = Paint().apply {
        isAntiAlias = true
        textSize = 24f
        style = Paint.Style.FILL
    }
    private val tempMatrix = Matrix()
    private fun drawHeadInfo(): Bitmap =
        Bitmap.createBitmap(width, headHeight.toInt(), Bitmap.Config.ARGB_8888).apply {
            Canvas(this).run {
                drawRect(0f, 0f, width.toFloat(), headHeight,
                    paintHead.apply { color = Color.WHITE })
                paintHead.measureText("已售").let { txtWidth ->
                    dip2px(10f).let { spacing0 ->
                        dip2px(5f).let { spacing1 ->
                            ((3 * txtWidth + 2 * spacing0 + 3 * spacing1 + width - seatBitmapAvailable.width + seatBitmapSold.width + seatBitmapChecked.width) / 2).let { startX ->
                                (txtWidth + spacing0 + spacing1 + startX + seatBitmapAvailable.width).let { soldSeatBitmapX ->
                                    (txtWidth + spacing0 + spacing1 + soldSeatBitmapX + seatBitmapSold.width).let { checkedSeatBitmapX ->
                                        tempMatrix.apply { setScale(xScale1, yScale1) }
                                        paintHead.apply { color = Color.BLACK }
                                        drawBitmap(
                                            seatBitmapAvailable, tempMatrix.apply {
                                                postTranslate(startX, (headHeight - seatHeight) / 2)
                                            }, paintHead
                                        )
                                        drawBitmap(
                                            seatBitmapSold, tempMatrix.apply {
                                                postTranslate(
                                                    soldSeatBitmapX, (headHeight - seatHeight) / 2
                                                )
                                            }, paintHead
                                        )
                                        drawBitmap(
                                            seatBitmapChecked, tempMatrix.apply {
                                                postTranslate(
                                                    checkedSeatBitmapX,
                                                    (headHeight - seatBitmapAvailable.height) / 2
                                                )
                                            }, paintHead
                                        )
                                        getBaseLine(paintHead, 0f, headHeight).let { txtY ->
                                            drawText(
                                                "可选", seatWidth + spacing1 + startX,
                                                txtY, paintHead
                                            )
                                            drawText(
                                                "已售", seatWidth + spacing1 + soldSeatBitmapX,
                                                txtY, paintHead
                                            )
                                            drawText(
                                                "已选", seatWidth + spacing1 + checkedSeatBitmapX,
                                                txtY, paintHead
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                drawLine(0f, headHeight, width.toFloat(), headHeight, paintHead.apply {
                    color = Color.GRAY
                    strokeWidth = 1f
                })//分隔线
            }
        }

    private val paintPath: Paint = Paint().apply {
        flags = Paint.ANTI_ALIAS_FLAG
        style = Paint.Style.FILL
    }
    private val borderHeight = 1//头部下面横线的高度
    private val screenWidthScale = 0.5f//荧幕默认宽度与座位图比例
    private val screenWidthDefault: Int = dip2px(80f)//荧幕最小宽度
    var screenName = ""//荧幕名称
    private fun drawScreen(canvas: Canvas) =
        (seatBitmapWidth * matrixScaleX / 2 + translateX).let { centerX ->
            (headHeight + borderHeight).let { startY ->
                (seatBitmapWidth * screenWidthScale * matrixScaleX)
                    .let { if (it < screenWidthDefault) screenWidthDefault.toFloat() else it }
                    .let { screenWidth ->
                        canvas.drawPath(Path().apply {
                            moveTo(centerX, startY)
                            lineTo(centerX - screenWidth / 2, startY)
                            lineTo(
                                centerX - screenWidth / 2 + 20, startY + screenHeight * matrixScaleY
                            )
                            lineTo(
                                centerX + screenWidth / 2 - 20, startY + screenHeight * matrixScaleY
                            )
                            lineTo(centerX + screenWidth / 2, startY)
                        }, paintPath.apply { color = Color.parseColor("#e2e2e2") })
                        canvas.drawText(screenName,
                            centerX - paintPath.apply { textSize = 20 * matrixScaleX }
                                .measureText(screenName) / 2,
                            getBaseLine(paintPath, startY, startY + screenHeight * matrixScaleY),
                            paintPath.apply { color = Color.BLACK })
                    }
            }
        }//绘制中间屏幕

    private val paintOverview: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private fun drawOverview(): Bitmap? = overviewBitmap?.apply {
        isRenewOverview = false
        eraseColor(Color.TRANSPARENT)
        Canvas(this).run {
            drawRect(0f, 0f, rectW, rectH,
                paintOverview.apply { color = Color.parseColor("#7e000000") })
            for (i in 0 until numRow) {
                loop@ for (j in 0 until numColumn) {
                    when (getSeatType(i, j)) {
                        SEAT_TYPE_SOLD -> paintOverview.color = overviewSold
                        SEAT_TYPE_SELECTED -> paintOverview.color = overviewChecked
                        SEAT_TYPE_AVAILABLE -> paintOverview.color = Color.WHITE
                        SEAT_TYPE_NOT_AVAILABLE -> continue@loop
                    }
                    (j * rectWidth + (j + 1) * overviewSpacing).let { left ->
                        (i * rectHeight + (i + 1) * overviewSpacingVer).let { top ->
                            drawRect(left, top, left + rectWidth, top + rectHeight, paintOverview)
                        }
                    }
                }
            }
        }
    }//灰色背景

    private val paintRedBorder: Paint = Paint().apply {
        isAntiAlias = true
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = dip2px(1f).toFloat()
    }

    private fun drawOverview(canvas: Canvas) {
        val left = (if (-translateX < 0) 0f else -translateX) / overviewScale / matrixScaleX
        val top =
            (((headHeight - translateY).let { if (it < 0) 0f else it }) / overviewScale / matrixScaleY)
                .let { if (it > 0) it + overviewSpacingVer else it }
        val currentWidth =
            (translateX + (numColumn * seatWidth + spacing * (numColumn - 1)) * matrixScaleX)
                .let { if (it > width) it - width else it }
        val right = rectW - currentWidth / overviewScale / matrixScaleX
        val currentHeight =
            (translateY + (numRow * seatHeight + spacingVer * (numRow - 1)) * matrixScaleY)
                .let { if (it > height) it - height else it }
        val bottom = rectH - currentHeight / overviewScale / matrixScaleY
        canvas.drawRect(left, top, right, bottom, paintRedBorder)
    }//红框

    private var isScaling: Boolean = false//标识是否正在缩放
    private var isFirstScale: Boolean = true//是否首次缩放
    private var scaleMatrixX: Float = 0f
    private var scaleMatrixY: Float = 0f
    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                isScaling = true
                detector.run {
                    if (isFirstScale) {
                        scaleMatrixX = currentSpanX
                        scaleMatrixY = currentSpanY
                        isFirstScale = false
                    }
                    (if (matrixScaleY * scaleFactor > 3) 3 / matrixScaleY else scaleFactor)
                        .let { if (matrixScaleY * it < 0.5) 0.5f / matrixScaleY else it }
                        .let { scale ->
                            matrixTool.postScale(
                                scale,
                                scale,
                                scaleMatrixX,
                                scaleMatrixY
                            )
                        }
                }
                invalidate()
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean = true
            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isScaling = false
                isFirstScale = true
            }
        })
    private var isOnClick: Boolean = false
    var isNeedDrawSeatBitmap = true//标识是否需要绘制座位图
    var maxSelected: Int = Integer.MAX_VALUE//最多可以选择的座位数量
    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                isOnClick = true
                for (i in 0 until numRow) {
                    for (j in 0 until numColumn) {
                        val tempX = (j * seatWidth + j * spacing) * matrixScaleX + translateX
                        val tempY = (i * seatHeight + i * spacingVer) * matrixScaleY + translateY
                        val maxTemX = tempX + seatWidth * matrixScaleX
                        val maxTempY = tempY + seatHeight * matrixScaleY
                        if (seatChecker?.run { isValidSeat(i, j) && !isSold(i, j) } == true &&
                            e.x in tempX..maxTemX && e.y in tempY..maxTempY) {
                            isHave(getID(i, j)).let { index ->
                                when {
                                    index < 0 -> when {
                                        selects.size < maxSelected -> {
                                            addChooseSeat(i, j)
                                            seatChecker?.checked(i, j)
                                        }
                                        else -> return super.onSingleTapConfirmed(e)
                                            .apply { showShort("最多只能选择${maxSelected}个") }
                                    }
                                    else -> {
                                        remove(index)
                                        seatChecker?.unCheck(i, j)
                                    }
                                }
                            }
                            isNeedDrawSeatBitmap = true
                            isRenewOverview = true
                            if (matrixScaleY < 1.7) {
                                scaleMatrixX = e.x
                                scaleMatrixY = e.y
                                zoomAnimate(matrixScaleY, 1.9f)
                            }
                            invalidate()
                            break
                        }
                    }
                }
                return super.onSingleTapConfirmed(e)
            }
        })

    private fun getID(row: Int, column: Int): Int = row * numColumn + column + 1
    private fun addChooseSeat(row: Int, column: Int) {
        getID(row, column).let { id ->
            for ((index, value) in selects.withIndex()) {
                if (id < value) {
                    selects.add(index, id)
                    return
                }
            }
            selects.add(id)
        }
    }

    private fun remove(index: Int): Int = selects.removeAt(index)
    private var isPointer: Boolean = false
    private var downX: Int = 0
    private var downY: Int = 0
    private val hideOverviewRunnable = Runnable {
        isDrawOverview = false
        invalidate()
    }
    private var lastX: Int = 0
    private var lastY: Int = 0
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        event.x.toInt().let { x ->
            event.y.toInt().let { y ->
                if (event.pointerCount > 1) isPointer = true
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = x
                        downY = y
                        isPointer = false
                        isDrawOverview = true
                        backgroundHandler.removeCallbacks(hideOverviewRunnable)
                        invalidate()
                    }
                    MotionEvent.ACTION_MOVE ->
                        if (!isScaling && !isOnClick && !isPointer && (abs(x - downX) > 10 || abs(y - downY) > 10)) {
                            matrixTool.postTranslate((x - lastX).toFloat(), (y - lastY).toFloat())
                            invalidate()
                        }
                    MotionEvent.ACTION_UP -> {
                        backgroundHandler.postDelayed(hideOverviewRunnable, 1500)
                        autoScale()
                        if (!isPointer && (abs(x - downX) > 10 || abs(y - downY) > 10)) autoScroll()
                    }
                }
                isOnClick = false
                lastY = y
                lastX = x
            }
        }
        return true
    }

    private fun autoScale() = when {
        matrixScaleX > 2.2 -> zoomAnimate(matrixScaleX, 2f)
        matrixScaleX < 0.98 -> zoomAnimate(matrixScaleX, 1f)
        else -> Unit
    }

    inner class ZoomAnimation :
        Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {}
        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
        override fun onAnimationUpdate(animation: ValueAnimator) {
            ((animation.animatedValue as Float).apply { zoom = this } / matrixScaleX)
                .let { matrixTool.postScale(it, it, scaleX, scaleY) }
            invalidate()
            if (isDebug) debug("zoom:$zoom")
        }
    }

    private fun zoomAnimate(cur: Float, tar: Float) = ValueAnimator().apply {
        setFloatValues(cur, tar)
        duration = 400
        interpolator = DecelerateInterpolator()
        ZoomAnimation().let {
            addListener(it)
            addUpdateListener(it)
        }
    }.start()

    private fun autoScroll() {
        var moveXLength: Float
        (seatBitmapWidth * matrixScaleX).let { currentSeatBitmapWidth ->
            moveXLength = when {
                currentSeatBitmapWidth < width && (translateX < 0 || matrixScaleX < numberWidth + spacing) -> numberWidth + spacing - translateX
                translateX >= 0 || currentSeatBitmapWidth + translateX <= width -> when {
                    currentSeatBitmapWidth + translateX < width -> width - translateX - currentSeatBitmapWidth
                    else -> numberWidth + spacing - translateX
                }
                else -> 0f
            }
        }
        var moveYLength: Float
        (seatBitmapHeight * matrixScaleY).let { currentSeatBitmapHeight ->
            ((screenHeight + spacingVer) * matrixScaleY + headHeight + borderHeight).let { startYPosition ->
                moveYLength = when {
                    currentSeatBitmapHeight + headHeight < height -> startYPosition - translateY
                    translateY >= 0 || currentSeatBitmapHeight + translateY <= height -> when {
                        currentSeatBitmapHeight + translateY < height -> height - translateY - currentSeatBitmapHeight
                        else -> startYPosition - translateY
                    }
                    else -> 0f
                }
            }
        }
        moveAnimate(Point().apply {
            x = translateX.toInt()
            y = translateY.toInt()
        }, Point().apply {
            x = (translateX + moveXLength).toInt()
            y = (translateY + moveYLength).toInt()
        })
    }//自动回弹：整个大小不超控件大小时：左滑，回弹到行号右；右滑，回弹到右；上下滑动，回弹到顶。整个大小超过控件大小时：左滑，回弹到最右；右滑，回弹到最左；上滑，回弹到底；下滑，回弹到顶。

    inner class MoveEvaluator : TypeEvaluator<Any> {
        override fun evaluate(fraction: Float, startValue: Any, endValue: Any): Any =
            (startValue as Point).let { start ->
                (endValue as Point).let { end ->
                    Point(
                        (start.x + fraction * (end.x - start.x)).toInt(),
                        (start.y + fraction * (end.y - start.y)).toInt()
                    )
                }
            }
    }

    inner class MoveAnimation : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            (animation.animatedValue as Point)
                .run { matrixTool.postTranslate(x - translateX, y - translateY) }
            invalidate()
        }
    }

    private fun moveAnimate(start: Point, end: Point) =
        ValueAnimator.ofObject(MoveEvaluator(), start, end).apply {
            duration = 400
            interpolator = DecelerateInterpolator()
            addUpdateListener(MoveAnimation())
        }.start()
}