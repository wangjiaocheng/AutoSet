package top.autoget.autosee

import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import top.autoget.autokit.DensityKit.dip2px
import top.autoget.autokit.ToastKit.showShort
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class SeatAirplane
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    init {
        init()
    }

    private val rectFCabin: RectF = RectF()
    private val paint: Paint = Paint()
    private val paintOther: Paint = Paint()
    private val paintMap: Paint = Paint()
    private var moveY = 0f
    private val seats: MutableMap<String, RectF> = mutableMapOf()

    enum class SeatState { Normal, Selected, Selecting }

    private val seatSelecting: MutableMap<String, SeatState> = mutableMapOf()
    private val seatSelectingRectF: MutableMap<String, RectF> = mutableMapOf()
    val setEmptySelecting = {
        seatSelecting.clear()
        seatSelectingRectF.clear()
        invalidate()
    }

    var maxSelectStates = 10
    private val seatSelected: MutableMap<String, SeatState> = mutableMapOf()
    fun setSeatSelected(row: Int, column: Int) {
        seatSelected[getSeatKeyName(row, column)] = SeatState.Selected
    }

    private fun getSeatKeyName(row: Int, column: Int): String = "$row#$column"
    private val scaleMap = 10f

    abstract inner class MoveListener(activity: Activity) :
        OnTouchListener, GestureDetector.OnGestureListener {
        private val gestureDetector: GestureDetector = GestureDetector(activity, this)
        private var startX = 0f
        private var startY = 0f
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (gestureDetector.onTouchEvent(event)) when (event.action) {
                MotionEvent.ACTION_DOWN -> return true.apply {
                    touch(event.x.apply { startX = this }, event.y.apply { startY = this })
                }
                MotionEvent.ACTION_UP -> if (abs(event.x - startX) < 5 && abs(event.y - startY) < 5)
                    return true.apply { touch(event.x, event.y) }
            }
            if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) endGesture()
            return false
        }

        private var isScrollStart = false
        private var isUpAndDown = false
        private fun endGesture() {
            isScrollStart = false
            isUpAndDown = false
            error("AA:over")
            moveOver()
        }

        abstract fun moveDirection(v: View?, direction: Int, distanceX: Float, distanceY: Float)
        abstract fun moveUpAndDownDistance(event: MotionEvent, distance: Int, distanceY: Int)
        abstract fun moveOver()
        abstract fun touch(x: Float, y: Float)
        override fun onDown(e: MotionEvent): Boolean = true
        override fun onSingleTapUp(e: MotionEvent): Boolean = true
        override fun onShowPress(e: MotionEvent) {}
        override fun onLongPress(e: MotionEvent) {}
        override fun onScroll(
            e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float
        ): Boolean = (e1.y + 5).let {
            when {
                isScrollStart -> if (isUpAndDown) when {
                    it < e2.rawY -> moveUpAndDownDistance(e2, -3, distanceY.toInt())
                    it > e2.rawY -> moveUpAndDownDistance(e2, 3, distanceY.toInt())
                }
                else -> when {
                    abs(distanceX) / abs(distanceY) > 2 -> {
                        isScrollStart = true
                        isUpAndDown = false
                    }
                    abs(distanceY) / abs(distanceX) > 3 -> {
                        isScrollStart = true
                        isUpAndDown = true
                    }
                    else -> isScrollStart = false
                }
            }
            return true
        }

        private val flingMinDistance = 150
        private val flingMinVelocity = 50
        private val moveToLeft = 0
        private val moveToRight = 1
        private val moveToUp = 2
        private val moveToDown = 3
        override fun onFling(
            e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean {
            error("AA:A$velocityX:$velocityY")
            when {
                isUpAndDown -> return false
                e1.x - e2.x > flingMinDistance && abs(velocityX) > flingMinVelocity ->
                    moveDirection(null, moveToLeft, e1.x - e2.x, e1.y - e2.y)
                e2.x - e1.x > flingMinDistance && abs(velocityX) > flingMinVelocity ->
                    moveDirection(null, moveToRight, e2.x - e1.x, e2.y - e1.y)
                e1.y - e2.y > flingMinDistance && abs(velocityY) > flingMinVelocity ->
                    moveDirection(null, moveToUp, 0f, e1.y - e2.y)
                e2.y - e1.y > flingMinDistance && abs(velocityY) > flingMinVelocity ->
                    moveDirection(null, moveToDown, 0f, e2.y - e1.y)
            }
            return false
        }
    }

    private fun init() {
        pathFuselage.reset()
        pathArrow.reset()
        pathTail.reset()
        paint.apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        paintOther.apply {
            isAntiAlias = true
            color = Color.rgb(138, 138, 138)
            style = Paint.Style.FILL
        }
        paintMap.apply {
            isAntiAlias = true
            color = Color.rgb(138, 138, 138)
            style = Paint.Style.FILL
        }
        setOnTouchListener(object : MoveListener(context as Activity) {
            override fun moveDirection(
                v: View?, direction: Int, distanceX: Float, distanceY: Float
            ) = Unit

            override fun moveOver() = println("-----moveOver:")
            override fun moveUpAndDownDistance(event: MotionEvent, distance: Int, distanceY: Int) {
                println("-----moveUpAndDownDistance:$distance-----$distanceY")
                if (valueAnimated > 0) moveY =
                    (rectFCabin.height() - rectFCabin.width() * 2.5f).let {
                        when {
                            moveY > it -> it
                            moveY in 0f..it -> (moveY + distanceY).apply { invalidate() }
                            else -> 0f
                        }
                    }
            }

            override fun touch(x: Float, y: Float) {
                when (valueAnimated) {
                    0f -> startAnim(false)
                    else -> for ((key, value) in seats) {
                        if (value.contains(x/*x - moveY*/, y)) {
                            println("-----key$key")
                            when {
                                seatSelecting.containsKey(key) -> if (seatSelecting[key] != SeatState.Selected) {
                                    seatSelecting.remove(key)
                                    seatSelectingRectF.remove(key)
                                    invalidate()
                                }
                                else -> when {
                                    seatSelecting.size < maxSelectStates -> when {
                                        seatSelected.containsKey(key) -> showShort("The selected")
                                        else -> {
                                            seatSelecting[key] = SeatState.Selecting
                                            seatSelectingRectF[key] = RectF().apply {
                                                (rectFCabin.top + rectFCabin.width() * 0.8f + moveY / scaleMap).let {
                                                    top = it + value.top / scaleMap
                                                    bottom = it + value.bottom / scaleMap
                                                }
                                                (rectFCabin.left + value.centerX() / scaleMap).let {
                                                    left = it - value.width() / scaleMap
                                                    right = it
                                                }
                                            }
                                            invalidate()
                                        }
                                    }
                                    else -> showShort("Choose a maximum of $maxSelectStates")
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun startAnim(zoomOut: Boolean) {
        stopAnim()
        startViewAnim(zoomOut)
    }

    private fun stopAnim() = valueAnimator?.let {
        clearAnimation()
        it.apply { repeatCount = 0 }.run {
            cancel()
            end()
        }
        valueAnimated = 0f
        postInvalidate()
    }

    private var valueAnimator: ValueAnimator? = null
    private var valueAnimated = 0f
    private fun startViewAnim(
        zoomOut: Boolean, startF: Float = 0f, endF: Float = 1f, time: Long = 280
    ): ValueAnimator {
        if (zoomOut && moveY > 0) invalidate().apply { moveY = 0f }
        return ValueAnimator().apply {
            setFloatValues(startF, endF)
            duration = time
            repeatCount = 0//无限循环
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {})
            addUpdateListener { valueAnimator ->
                valueAnimated =
                    (valueAnimator.animatedValue as Float).let { if (zoomOut) 1 - it else it }
                invalidate()
            }
            valueAnimator = this
            if (!isRunning) start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnim()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        MeasureSpec.getMode(widthMeasureSpec).let { widthSpecMode ->
            MeasureSpec.getMode(heightMeasureSpec).let { heightSpecMode ->
                when {
                    widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST ->
                        setMeasuredDimension(dip2px(150f), dip2px(200f))
                    widthSpecMode == MeasureSpec.AT_MOST -> MeasureSpec.getSize(heightMeasureSpec)
                        .let { setMeasuredDimension((it * 0.75).toInt(), it) }
                    heightSpecMode == MeasureSpec.AT_MOST -> MeasureSpec.getSize(widthMeasureSpec)
                        .let { setMeasuredDimension(it, (it / 0.75).toInt()) }
                }
            }
        }
    }

    private var valueScale = 2f
    private val valueScaleMax = 3f
    private val pathArrow: Path = Path()
    private val bitmapArrow: Bitmap
        get() = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).let { canvas ->
                rectFCabin.run {
                    (top + height() / 2 + height() / 6 / 2).let { it0 ->
                        (top + height() / 2 + height() / 5).let {
                            canvas.drawPath(pathArrow.apply {
                                reset()
                                moveTo(right + width() / 2 * 1.2f, it0)
                                quadTo(
                                    right + width() / 2 * 1.3f, it,
                                    right + width() / 2 * 1.4f, it0
                                )
                                close()
                            }, paint)
                            canvas.drawPath(pathArrow.apply {
                                reset()
                                moveTo(left - width() / 2 * 1.2f, it0)
                                quadTo(
                                    left - width() / 2 * 1.3f, it,
                                    left - width() / 2 * 1.4f, it0
                                )
                                close()
                            }, paint)
                        }
                    }
                    ((top + height() / 2 + height() / 6 / 2 + top + height() * 0.55f + width() * 0.8f) / 2).let { it1 ->
                        canvas.drawPath(pathArrow.apply {
                            reset()
                            ((measuredWidth.toFloat() + right + width() / 2 * 1.5f) / 2).let {
                                moveTo(it, it1)
                                quadTo(
                                    it + width() / 2 * 0.1f, it1 + height() / 60f * 7f,
                                    it + width() / 2 * 0.2f, it1
                                )
                            }
                        }, paint)
                        canvas.drawPath(pathArrow.apply {
                            reset()
                            ((left - width() / 2 * 1.5f) / 2).let {
                                moveTo(it, it1)
                                quadTo(
                                    it - width() / 2 * 0.1f, it1 + height() / 60f * 7f,
                                    it - width() / 2 * 0.2f, it1
                                )
                            }
                        }, paint.apply {
                            color = Color.WHITE
                            alpha = 150
                        })
                    }
                }
            }
        }
    private val pathTail: Path = Path()
    private val bitmapTail: Bitmap
        get() = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).let {
                rectFCabin.run {
                    it.drawPath(pathTail.apply {
                        reset()
                        bottom -= dip2px(5f)
                        moveTo(centerX(), bottom + width() / 2)
                        (centerX() + width() * 1.5f).let {
                            lineTo(it, bottom + width() * 1.5f)
                            lineTo(it, bottom + width() * 2f)
                        }
                        lineTo(centerX(), bottom + width() * 1.5f)
                        (centerX() - width() * 1.5f).let {
                            lineTo(it, bottom + width() * 2f)
                            lineTo(it, bottom + width() * 1.5f)
                        }
                        close()
                    }, paint)
                    it.drawPath(pathTail.apply {
                        reset()
                        (bottom + width() * 1.5f).let {
                            moveTo(centerX() - width() / 2 * 0.1f, it)
                            quadTo(
                                centerX(), bottom + width() * 3f,
                                centerX() + width() / 2 * 0.1f, it
                            )
                        }
                        close()
                    }, paint.apply {
                        color = Color.WHITE
                        alpha = 150
                    })
                }
            }
        }
    private val bitmapCabin: Bitmap
        get() = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).drawRoundRect(
                rectFCabin.apply {
                    top -= dip2px(10f)
                    bottom += dip2px(5f)
                }, measuredWidth / 8f / 2f, measuredWidth / 8f / 2f,
                paint.apply { color = Color.WHITE })
        }

    override fun onDraw(canvas: Canvas) = canvas.run {
        super.onDraw(this)
        save()
        valueScale = valueScaleMax * valueAnimated
        Matrix().apply {
            postScale(1 + valueScale * 2, 1 + valueScale * 2)
            postTranslate(
                valueScale * -1 * measuredWidth,
                valueScale * -1 * (measuredWidth / 8 + measuredHeight / 3 / 3) * 2 - moveY
            )
        }.let { matrix ->
            (measuredWidth / 8f).let { rectFCabinWidth ->
                rectFCabin.apply {
                    top = measuredHeight / 3f / 3
                    bottom = measuredHeight / 3f / 3 + measuredHeight / 3 * 2
                    left = measuredWidth / 2 - rectFCabinWidth / 2
                    right = measuredWidth / 2 + rectFCabinWidth / 2
                }
                drawBitmap(getBitmapFuselage(rectFCabinWidth), matrix, paint)
            }
            drawBitmap(bitmapArrow, matrix, paint)
            drawBitmap(bitmapTail, matrix, paint)
            drawBitmap(bitmapCabin, matrix, paint)
        }
        (measuredWidth / 8 * (1 + valueScale * 2)).let { rectFCabinWidth ->
            rectFCabin.apply {
                top = measuredHeight / 3f / 3
                bottom = measuredHeight / 3 / 3 + measuredHeight / 3 * 2 * (1 + valueScale * 2)
                left = measuredWidth / 2 - rectFCabinWidth / 2
                right = measuredWidth / 2 + rectFCabinWidth / 2
            }
        }
        translate(
            0f,
            (valueScale * -1 * (measuredWidth / 8 + measuredHeight / 3 / 3) * (1 - (valueScaleMax - 2) * 0.1f)) - moveY
        )
        drawSeatFirst(this)
        drawSeatTourist(this)
        drawSeatLast(this)
        drawSeatMap(this)
        restore()
    }

    private val pathFuselage: Path = Path()
    private fun getBitmapFuselage(rectFCabinWidth: Float): Bitmap =
        Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).apply {
            pathFuselage.apply {
                rectFCabin.run {
                    moveTo(
                        measuredWidth / 2f - rectFCabinWidth / 2f - dip2px(2f).toFloat(),
                        top + rectFCabinWidth / 2f
                    )
                    (measuredWidth / 2f + rectFCabinWidth / 2f + dip2px(2f).toFloat()).let { it0 ->
                        (top - rectFCabinWidth * 1.2f).let {
                            cubicTo(
                                measuredWidth / 2f - rectFCabinWidth / 4f, it,
                                measuredWidth / 2f + rectFCabinWidth / 4f, it,
                                it0, top + rectFCabinWidth / 2f
                            )
                        }
                        top += dip2px(10f)//机翼向下平移距离
                        lineTo(it0, top + height() / 3f)
                        (top + height() * 0.55f).let {
                            lineTo(measuredWidth.toFloat(), it)
                            lineTo(measuredWidth.toFloat(), it + width() * 0.8f)
                        }
                        (top + height() / 2f + height() / 6f / 2f).let {
                            lineTo(right + width() / 2 * 1.5f, it)
                            lineTo(it0, it)
                        }
                        lineTo(it0, bottom - rectFCabinWidth / 2f)
                    }
                    (bottom + rectFCabinWidth * 2.5f).let {
                        cubicTo(
                            measuredWidth / 2f + rectFCabinWidth / 4f, it,
                            measuredWidth / 2f - rectFCabinWidth / 4f, it,
                            measuredWidth / 2f - rectFCabinWidth / 2f - dip2px(2f).toFloat(),
                            bottom - rectFCabinWidth / 2f
                        )
                    }
                    (measuredWidth / 2f - rectFCabinWidth / 2f - dip2px(2f).toFloat()).let { it0 ->
                        (top + height() / 2f + height() / 6f / 2f).let {
                            lineTo(it0, it)
                            lineTo(left - width() / 2 * 1.5f, it)
                        }
                        (top + height() * 0.55f).let {
                            lineTo(0f, it + width() * 0.8f)
                            lineTo(0f, it)
                        }
                        lineTo(it0, top + height() / 3f)
                    }
                    close()
                }
            }
            Canvas(this).drawPath(pathFuselage, paint.apply {
                color = Color.WHITE
                alpha = 150
            })
        }

    enum class SeatType { Left, Middle, Right }
    enum class CabinType { First, Second, Tourist, Last }

    private val rectFWc: RectF = RectF()
    private fun drawSeatFirst(canvas: Canvas) {
        seats.clear()
        (rectFCabin.width() / 9).let { seatWH ->
            for (i in 0 until 7/*row*/) {
                for (j in 0 until 7/*column*/) {
                    when {
                        j in 0..1 -> setSeat(i, j, canvas, seatWH, SeatType.Left, CabinType.First)
                        j in 2..4 && i in 0 until 7/*row*/ - 1 ->
                            setSeat(i, j, canvas, seatWH, SeatType.Middle, CabinType.First)
                        j in 5..6 -> setSeat(i, j, canvas, seatWH, SeatType.Right, CabinType.First)
                    }
                }
            }
            (rectFCabin.top + rectFCabin.width() / 2 + seatWH * (7/*row*/ + 1.5f)).let {
                rectFWc.apply {
                    top = it + seatWH * 2.5f
                    bottom = it + seatWH * 4.5f
                    left = rectFCabin.left + seatWH * (1 / 3)
                    right = rectFCabin.left + seatWH * (1 / 3 + 2)
                }.let {
                    canvas.drawRect(it, paintOther.apply { style = Paint.Style.STROKE })
                    drawWcText(it, canvas)
                }
                RectF().apply {
                    top = it + seatWH * 1f
                    bottom = it + seatWH * 4.5f
                    left = rectFWc.right + seatWH * 0.5f
                    right = rectFCabin.left + seatWH * (7/*column*/ - 0.5f - 1 / 3)
                }.let { rectFWifi ->
                    canvas.drawRect(rectFWifi, paintOther)
                    drawWifiLogo(rectFWifi, canvas)
                }
                rectFWc.apply {
                    top = it + seatWH * 2.5f
                    bottom = it + seatWH * 4.5f
                    left = rectFCabin.left + seatWH * (7/*column*/ - 1 / 3)
                    right = rectFCabin.left + seatWH * (7/*column*/ - 1 / 3 + 2)
                }.let {
                    canvas.drawRect(it, paintOther.apply { style = Paint.Style.STROKE })
                    drawWcText(it, canvas)
                }
            }
            drawSeatSecond(canvas, seatWH)
        }
    }

    data class SeatPoint(var row: Int, var column: Int)

    private fun setSeat(
        i: Int, j: Int, canvas: Canvas, seatWH: Float, type: SeatType, cabinType: CabinType
    ) {
        val rectFTop = when (cabinType) {
            CabinType.First -> rectFCabin.top + rectFCabin.width() / 2 + seatWH * (i + when (type) {
                SeatType.Left, SeatType.Right -> 1.5f
                SeatType.Middle -> 2f
            })
            CabinType.Second -> rectFCabin.top + rectFCabin.width() / 2 + seatWH * (i + when (type) {
                SeatType.Left, SeatType.Right -> 15.5f
                SeatType.Middle -> 16f
            })
            CabinType.Tourist, CabinType.Last -> rectFWall.bottom + seatWH * (i + 1.5f)
        }
        val rectFLeft = when (cabinType) {
            CabinType.First -> rectFCabin.left + seatWH * (j + when (type) {
                SeatType.Left -> 1 / 3
                SeatType.Middle -> 1
                SeatType.Right -> 2 + 1 / 3
            })
            CabinType.Second -> rectFCabin.left + seatWH * (j + when (type) {
                SeatType.Left -> 1 / 3
                SeatType.Middle -> 1
                SeatType.Right -> 2 - 1 / 3
            })
            CabinType.Tourist, CabinType.Last -> rectFCabin.left + seatWH * (j + when (type) {
                SeatType.Left -> 1 / 3
                SeatType.Middle -> 1
                SeatType.Right -> 2 + 1 / 3
            })
        }
        val seatPoint: SeatPoint = when (cabinType) {
            CabinType.First -> SeatPoint(i, j)
            CabinType.Second -> SeatPoint(i + 7, j)
            CabinType.Tourist -> SeatPoint(i + 10, j)
            CabinType.Last -> SeatPoint(i + 35, j)
        }
        println("-----${seatPoint.row}--${seatPoint.column}")
        if (valueAnimated == 1f) when (cabinType) {
            CabinType.First -> {
                when (type) {
                    SeatType.Left, SeatType.Right -> rectFCabin.top - rectFCabin.width() / 2 - seatWH * (valueScaleMax - 1.51f) - moveY
                    SeatType.Middle -> rectFCabin.top - rectFCabin.width() / 2 - seatWH * (valueScaleMax - 1.8f) - seatWH / 2f - moveY
                }
            }
            CabinType.Second -> {
                when (type) {
                    SeatType.Left, SeatType.Right -> rectFCabin.top - rectFCabin.width() / 2 - seatWH * (valueScaleMax - 1.25f) - moveY
                    SeatType.Middle -> rectFCabin.top - rectFCabin.width() / 2 - seatWH * (valueScaleMax - 1.75f) - seatWH / 2f - moveY
                }
            }
            CabinType.Tourist, CabinType.Last -> rectFCabin.top - rectFCabin.width() / 2 - seatWH * (valueScaleMax - 1) - moveY
        }.let {
            RectF(rectFLeft, rectFTop, rectFLeft + seatWH, rectFTop + seatWH).apply {
                top -= it
                bottom -= it
            }.run {
                if (top > 0 && bottom < measuredHeight)
                    seats[getSeatKeyName(seatPoint.row, seatPoint.column)] = this
            }
        }
        when {
            seatSelected.containsKey(getSeatKeyName(seatPoint.row, seatPoint.column)) ->
                canvas.drawBitmap(
                    getSeat(
                        seatWH, seatSelected[getSeatKeyName(seatPoint.row, seatPoint.column)]!!
                    ), rectFLeft, rectFTop, paint
                )
            seatSelecting.containsKey(getSeatKeyName(seatPoint.row, seatPoint.column)) -> {
                println("-----${seatPoint.row}--${seatPoint.column}")
                canvas.drawBitmap(
                    getSeat(
                        seatWH, seatSelecting[getSeatKeyName(seatPoint.row, seatPoint.column)]!!
                    ), rectFLeft, rectFTop, paint
                )
                if (valueAnimated == 1f &&
                    seatSelecting[getSeatKeyName(seatPoint.row, seatPoint.column)] ==
                    SeatState.Selecting
                ) {
                    paintOther.apply {
                        color = Color.WHITE
                        textSize = seatWH / 4f
                    }
                    "${(seatPoint.row + 1)},${seatPoint.column + 1}".let { text ->
                        canvas.drawText(
                            text, rectFLeft + seatWH / 2f - getFontLength(paintOther, text) / 2,
                            rectFTop + seatWH / 2f + getFontHeight(paintOther, text) / 3,
                            paintOther
                        )
                    }
                    paintOther.color = Color.rgb(138, 138, 138)
                }
            }
            else -> canvas.drawBitmap(
                getSeat(seatWH, SeatState.Normal), rectFLeft, rectFTop, paint
            )
        }
    }

    var bitmapSeatNormal: Bitmap? = null
    var bitmapSeatSelected: Bitmap? = null
    var bitmapSeatSelecting: Bitmap? = null
    private fun getSeat(width: Float, type: SeatState): Bitmap = when (type) {
        SeatState.Normal -> bitmapSeatNormal.let {
            if (it == null || abs(it.width - width) > 1)
                setBitmapSize(R.mipmap.seat_gray, width) else it
        }
        SeatState.Selected -> bitmapSeatSelected.let {
            if (it == null || abs(it.width - width) > 1)
                setBitmapSize(R.mipmap.seat_sold, width) else it
        }
        SeatState.Selecting -> bitmapSeatSelecting.let {
            if (it == null || abs(it.width - width) > 1)
                setBitmapSize(R.mipmap.seat_green, width) else it
        }
    }

    private fun setBitmapSize(iconId: Int, w: Float): Bitmap =
        BitmapFactory.decodeResource(context.resources, iconId).run {
            (w * 1.0f / width).let {
                Bitmap.createScaledBitmap(
                    this, (width * it).toInt(), (height * it).toInt(), true
                )
            }
        }

    fun getFontLength(paint: Paint, str: String): Float =
        Rect().apply { paint.getTextBounds(str, 0, str.length, this) }.width().toFloat()

    fun getFontHeight(paint: Paint, str: String): Float =
        Rect().apply { paint.getTextBounds(str, 0, str.length, this) }.height().toFloat()

    private fun drawWcText(rectFWc: RectF, canvas: Canvas) {
        paintOther.apply {
            paintOther.alpha = 150
            paintOther.textSize = rectFWc.width() / 4
        }
        canvas.drawText(
            "WC", rectFWc.centerX() - getFontLength(paintOther, "WC") / 2f,
            rectFWc.centerY() + getFontHeight(paintOther, "WC") / 3f, paintOther
        )
        paintOther.alpha = 255
    }

    private fun drawWifiLogo(rectFWifi: RectF, canvas: Canvas) =
        (rectFWifi.height() / 2f / 4f).let { signalRadius ->
            paintOther.apply {
                paintOther.alpha = 150
                paintOther.strokeWidth = signalRadius / 4
            }
            (signalRadius * (3 + 0.5f) / 2f).let { marginTop ->
                var rectF: RectF
                for (i in 0..3) {
                    rectF =
                        (if (i == 0) signalRadius / 2f else signalRadius * (i + 0.5f)).let { radius ->
                            rectFWifi.run {
                                RectF(
                                    centerX() - radius, centerY() + marginTop - radius,
                                    centerX() + radius, centerY() + marginTop + radius
                                )
                            }
                        }
                    when (i) {
                        0 -> canvas.drawArc(
                            rectF, -135f, 90f, true,
                            paintOther.apply { style = Paint.Style.FILL })
                        else -> canvas.drawArc(
                            rectF, -135f, 90f, false,
                            paintOther.apply { style = Paint.Style.STROKE })
                    }
                }
            }
            paintOther.apply {
                paintOther.alpha = 255
                paintOther.strokeWidth = 0f
            }
        }

    private val rectFWall: RectF = RectF()
    private fun drawSeatSecond(canvas: Canvas, seatWH: Float) =
        (rectFCabin.width() / 10).let { seatWH0 ->
            for (i in 0 until 3/*row*/) {
                for (j in 0 until 8/*column*/) {
                    when (j) {
                        in 0..1 -> setSeat(i, j, canvas, seatWH0, SeatType.Left, CabinType.Second)
                        in 2..5 -> setSeat(i, j, canvas, seatWH0, SeatType.Middle, CabinType.Second)
                        in 6..7 -> setSeat(i, j, canvas, seatWH0, SeatType.Right, CabinType.Second)
                    }
                }
            }
            (rectFCabin.width() / 2 + seatWH * 14 + seatWH0 * (3/*row*/ + 1.5f)).let { it0 ->
                (dip2px(2f) * valueAnimated).let { if (it < 1) 1f else it }.let { it1 ->
                    canvas.drawRoundRect(
                        rectFWall.apply {
                            top = rectFCabin.top + it0
                            bottom = rectFCabin.top + it0 + it1
                            left = rectFCabin.left + seatWH0 * (1 / 3)
                            right = rectFCabin.left + seatWH0 * (1 / 3 + 2.5f)
                        }, dip2px(1f).toFloat(), dip2px(1f).toFloat(),
                        paintOther.apply { style = Paint.Style.FILL })
                    canvas.drawRoundRect(rectFWall.apply {
                        top = rectFCabin.top + it0
                        bottom = rectFCabin.top + it0 + it1
                        left = rectFCabin.left - seatWH * (1 / 3) + seatWH0 * (8/*column*/ - 0.5f)
                        right = rectFCabin.left - seatWH * (1 / 3) + seatWH0 * (8/*column*/ + 2)
                    }, dip2px(1f).toFloat(), dip2px(1f).toFloat(), paintOther)
                }
            }
        }

    private fun drawSeatTourist(canvas: Canvas) = (rectFCabin.width() / 12).let { seatWH ->
        for (i in 0 until 25/*row*/) {
            for (j in 0 until 10/*column*/) {
                when (j) {
                    in 0..2 -> setSeat(i, j, canvas, seatWH, SeatType.Left, CabinType.Tourist)
                    in 3..6 -> setSeat(i, j, canvas, seatWH, SeatType.Middle, CabinType.Tourist)
                    in 7..9 -> setSeat(i, j, canvas, seatWH, SeatType.Right, CabinType.Tourist)
                }
            }
        }
        rectFWc.apply {
            top = rectFWall.bottom + seatWH * 27.5f
            bottom = rectFWc.top + seatWH * 3
            left = rectFCabin.left + seatWH * (1 / 3)
            right = rectFCabin.left + seatWH * (1 / 3 + 3)
        }.let {
            canvas.drawRect(it, paintOther.apply { style = Paint.Style.STROKE })
            drawWcText(it, canvas)
        }
        RectF().apply {
            top = rectFWall.bottom + seatWH * 27.5f
            bottom = top + seatWH * 3
            left = rectFWc.right + seatWH * 0.5f
            right = rectFCabin.left + seatWH * (10/*column*/ + 5.5f + 1 / 3)
        }.let { rectFWifi ->
            canvas.drawRect(rectFWifi, paintOther)
            drawWifiLogo(rectFWifi, canvas)
        }
        rectFWc.apply {
            top = rectFWall.bottom + seatWH * 27.5f
            bottom = rectFWc.top + seatWH * 3
            left = rectFCabin.left + seatWH * (10/*column*/ - 1 / 3 - 1)
            right = rectFCabin.left + seatWH * (10/*column*/ - 1 / 3 + 2)
        }.let {
            canvas.drawRect(it, paintOther.apply { style = Paint.Style.STROKE })
            drawWcText(it, canvas)
        }
    }

    private fun drawSeatLast(canvas: Canvas) = (rectFCabin.width() / 12).let { seatWH ->
        for (i in 0 until 19/*row*/) {
            for (j in 0 until 10/*column*/) {
                when {
                    j in 0..2 -> setSeat(i, j, canvas, seatWH, SeatType.Left, CabinType.Last)
                    j in 3..6 -> setSeat(i, j, canvas, seatWH, SeatType.Middle, CabinType.Last)
                    j >= 7 -> setSeat(i, j, canvas, seatWH, SeatType.Right, CabinType.Last)
                }
            }
        }
    }

    private fun drawSeatMap(canvas: Canvas) {
        if (valueAnimated == 1f) {
            (rectFCabin.top + rectFCabin.width() * 0.8f + moveY).let {
                (rectFCabin.width() / scaleMap).let { mapW ->
                    ((rectFCabin.height() - rectFCabin.width() * 2.5f) / scaleMap + measuredHeight / scaleMap).let { mapH ->
                        canvas.drawRect(
                            RectF(rectFCabin.left, it, rectFCabin.left + mapW, it + mapH),
                            paintMap.apply {
                                color = Color.rgb(138, 138, 138)
                                alpha = 80
                                style = Paint.Style.FILL
                            })
                    }
                    (height / scaleMap).let { mapH ->
                        canvas.drawRect(
                            RectF(
                                rectFCabin.left, it + moveY / scaleMap,
                                rectFCabin.left + mapW, it + moveY / scaleMap + mapH
                            ), paintMap.apply {
                                color = Color.RED
                                strokeWidth = dip2px(0.75f).toFloat()
                                style = Paint.Style.STROKE
                            })
                    }
                }
            }
            paintMap.strokeWidth = 0f
            if (seatSelectingRectF.isNotEmpty()) {
                val rectF = RectF()
                paintMap.apply {
                    color = Color.RED
                    alpha = 80
                    style = Paint.Style.FILL
                }
                for (value in seatSelectingRectF.values) {
                    canvas.drawRect(rectF.apply {
                        top = value.top + moveY
                        bottom = value.bottom + moveY
                        left = value.left - dip2px(0.5f)
                        right = value.right - dip2px(0.5f)
                    }, paintMap)
                }
            }
        }
    }

    private val fixedThreadPool: ExecutorService = Executors.newFixedThreadPool(3)
    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            msg.data?.run {
                getByteArray("bitmap")?.let {
                    BitmapFactory.decodeByteArray(it, 0, it.size)?.let { bitmap ->
                        (msg.obj as Canvas)
                            .drawBitmap(bitmap, getFloat("left"), getFloat("top"), paint)
                    }
                }
            }
        }
    }

    fun setBitmap(canvas: Canvas, seatWH: Float, type: SeatState, left: Float, top: Float) =
        fixedThreadPool.execute {
            getSeat(seatWH, type).let { bitmap ->
                canvas.drawBitmap(bitmap, left, top, paint)
                mHandler.run {
                    sendMessage(obtainMessage().apply {
                        obj = canvas
                        data = Bundle().apply {
                            putFloat("left", left)
                            putFloat("top", top)
                            ByteArrayOutputStream().use { byteArrayOutputStream ->
                                bitmap.compress(
                                    Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream
                                )
                                putByteArray("bitmap", byteArrayOutputStream.toByteArray())
                            }
                        }
                    })
                }
                bitmap.recycle()
            }
        }

    enum class CabinPosition { Top, Middle, Last }

    fun goCabinPosition(cabinPosition: CabinPosition) {
        if (valueAnimated > 0) {
            moveY = (rectFCabin.height() - rectFCabin.width() * 2.5f).let {
                when (cabinPosition) {
                    CabinPosition.Top -> 0f
                    CabinPosition.Middle -> it / 2
                    CabinPosition.Last -> it
                }
            }
            invalidate()
        }
    }
}