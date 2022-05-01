package top.autoget.autosee

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import top.autoget.autokit.DensityKit.dip2px
import top.autoget.autokit.ImageKit.setColorAlphaByInt
import kotlin.math.*

class Cobweb
@JvmOverloads constructor(mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(mContext, attrs, defStyleAttr) {
    init {
        initAttrs(attrs)
        initEvent()
    }

    var spiderNameSize: Int = 0
        set(spiderNameSize) {
            field = spiderNameSize
            invalidate()
        }
    var spiderMaxLevel: Int = 0
        set(spiderMaxLevel) {
            field = spiderMaxLevel
            initLevelPoints()
            invalidate()
        }//最大层级
    var spiderColor: Int = 0
        set(spiderColor) {
            field = spiderColor
            initLevelPoints()
            invalidate()
        }//内部填充颜色
    var spiderRadiusColor: Int = 0
        set(spiderRadiusColor) {
            field = spiderRadiusColor
            invalidate()
        }//半径颜色
    var spiderLevelColor: Int = 0//填充颜色
    var spiderLevelStrokeColor: Int = 0
        set(spiderLevelStrokeColor) {
            field = spiderLevelStrokeColor
            spiderLevelColor = setColorAlphaByInt(field, 255 / 2)
            invalidate()
        }//描边颜色
    var spiderLevelStrokeWidth: Float = 0f
        set(spiderLevelStrokeWidth) {
            field = spiderLevelStrokeWidth
            invalidate()
        }//描边宽度
    var isSpiderLevelStroke: Boolean = false
        set(isSpiderLevelStroke) {
            field = isSpiderLevelStroke
            invalidate()
        }//层级描边
    var isSpiderRotate: Boolean = false
        set(isSpiderRotate) {
            field = isSpiderRotate
            invalidate()
        }//手势旋转

    private fun initAttrs(attrs: AttributeSet?) {
        val typedArray: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.Cobweb)
        try {
            typedArray.run {
                spiderNameSize =
                    getDimensionPixelSize(R.styleable.Cobweb_spiderNameSize, dip2px(16f))
                spiderMaxLevel = getInteger(R.styleable.Cobweb_spiderMaxLevel, 4)
                spiderColor = getColor(
                    R.styleable.Cobweb_spiderColor, resources.getColor(R.color.cyan_800)
                )
                spiderRadiusColor = getColor(R.styleable.Cobweb_spiderRadiusColor, Color.WHITE)
                spiderLevelStrokeColor = getColor(
                    R.styleable.Cobweb_spiderLevelStrokeColor,
                    resources.getColor(R.color.amber_800)
                )
                spiderLevelColor = setColorAlphaByInt(spiderLevelStrokeColor, 255 / 2)
                spiderLevelStrokeWidth =
                    getFloat(R.styleable.Cobweb_spiderLevelStrokeWidth, 3f)
                isSpiderLevelStroke = getBoolean(R.styleable.Cobweb_spiderLevelStroke, true)
                isSpiderRotate = getBoolean(R.styleable.Cobweb_spiderRotate, true)
            }
        } finally {
            typedArray.recycle()
        }
    }

    private var spiderNames: Array<String> = arrayOf()//蜘蛛名称列表
    private var spiderLevels: FloatArray = floatArrayOf()//层级列表

    data class ModelSpider(var spiderName: String, var spiderLevel: Float)

    var spiderList: MutableList<ModelSpider> = mutableListOf()
        set(spiderList) {
            field = spiderList
            spiderNumber = field.size
            invalidate()
        }//蜘蛛列表
    private var spiderNumber: Int = 0//蜘蛛数量
    private var spiderStrRect: Rect = Rect()//字体矩形
    private var paintSpiderName = Paint()//字体画笔
    private var paintSpiderRank = Paint()//层级进度画笔
    private var levelPaintList: MutableList<Paint> = arrayListOf()//层级颜色
    private val initLevelPoints = {
        for (i in spiderMaxLevel downTo 1) {
            Paint().apply {
                isAntiAlias = true
                (spiderMaxLevel * 10 / 11).let { if (it < 1) 1 else it }.let { scale ->
                    color = setColorAlphaByInt(
                        spiderColor,
                        (255 / (spiderMaxLevel + 1) * (spiderMaxLevel - i - 1) + 255 / scale) % 255
                    )
                }
                style = Paint.Style.FILL//实心
                levelPaintList.add(this)
            }
        }
    }
    private var paintSpiderCenter = Paint()//蛛网半径画笔
    private fun initEvent() {
        spiderNames = arrayOf("金钱", "能力", "美貌", "智慧", "交际", "口才")
        spiderLevels = floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f)
        spiderList.apply {
            clear()
            for ((index, spiderName) in spiderNames.withIndex()) {
                spiderList.add(ModelSpider(spiderName, spiderLevels[index]))
            }
        }
        spiderNumber = spiderList.size
        paintSpiderName.apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = spiderNameSize.toFloat()
            getTextBounds(
                spiderList[0].spiderName, 0, spiderList[0].spiderName.length, spiderStrRect
            )
        }
        paintSpiderRank.apply {
            isAntiAlias = true
            color = Color.RED
            strokeWidth = 8f
            style = Paint.Style.STROKE//空心
        }
        initLevelPoints
        paintSpiderCenter.apply {
            isAntiAlias = true
            color = spiderRadiusColor
        }
    }

    private var scroller: Scroller = Scroller(mContext)
    private var flingPoint = 0f
    private var rotateOrientation = 0.0
    private var rotateAngle = 0.0
    override fun computeScroll() {
        if (scroller.computeScrollOffset())
            scroller.run { max(abs(currX), abs(currY)) }.let { max ->
                (RotateInfo.CIRCLE_ANGLE * (abs(max - flingPoint) / perimeter)).let { rotateDis ->
                    handleRotate(
                        when {
                            rotateOrientation < 0 -> rotateAngle - rotateDis
                            rotateOrientation > 0 -> rotateAngle + rotateDis
                            else -> rotateAngle
                        }
                    )
                }
                flingPoint = max.toFloat()
                invalidate()
            }
    }

    private fun handleRotate(rotate: Double) {
        rotateAngle = RotateInfo.getAngleNormalized(rotate)
        invalidate()
    }

    object RotateInfo {
        fun getAngleRotate(p1: PointF, p2: PointF, centerPointF: PointF): Double =
            centerPointF.run {
                when {
                    getQuadrant(p1, this) == getQuadrant(p2, this) ->
                        getAngle(p1, this) - getAngle(p2, this)
                    else -> 0.0
                }
            }

        fun getQuadrant(pointF: PointF, centerPointF: PointF): Int = centerPointF.run {
            when {
                pointF.x < x -> if (pointF.y < y) 2 else if (pointF.y > y) 3 else -1
                pointF.x > x -> if (pointF.y < y) 1 else if (pointF.y > y) 4 else -1
                else -> -1
            }
        }

        fun getAngle(pointF: PointF, centerPointF: PointF): Double =
            centerPointF.run { getAngleNormalized(atan((y - pointF.y / pointF.x - x).toDouble())) }

        const val CIRCLE_ANGLE = 2 * PI
        fun getAngleNormalized(angle: Double): Double {
            var angleTemp = angle
            while (angleTemp < 0) {
                angleTemp += CIRCLE_ANGLE
            }
            return angleTemp % CIRCLE_ANGLE
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float
        ): Boolean {
            handleRotate(rotateAngle + RotateInfo.getAngleRotate(
                PointF(e2.x - distanceX, e2.y - distanceY), PointF(e2.x, e2.y), centerPointF
            ).apply { rotateOrientation = this })
            return super.onScroll(e1, e2, distanceX, distanceY)
        }

        override fun onFling(
            e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
        ): Boolean {
            when {
                abs(velocityX) < abs(velocityY) -> {
                    flingPoint = e2.y
                    scroller.fling(
                        0, e2.y.toInt(), 0, velocityY.toInt(),
                        0, 0, (e2.y - perimeter).toInt(), (e2.y + perimeter).toInt()
                    )
                }
                abs(velocityX) > abs(velocityY) -> {
                    flingPoint = e2.x
                    scroller.fling(
                        e2.x.toInt(), 0, velocityX.toInt(), 0,
                        (e2.x - perimeter).toInt(), (e2.x + perimeter).toInt(), 0, 0
                    )
                }
            }
            invalidate()
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDown(e: MotionEvent): Boolean =
            true.apply { if (!scroller.isFinished) scroller.forceFinished(true) }
    }

    private var gestureDetector: GestureDetector =
        GestureDetector(mContext, GestureListener()).apply { setIsLongpressEnabled(false) }

    override fun onTouchEvent(event: MotionEvent): Boolean = when {
        isSpiderRotate -> gestureDetector.onTouchEvent(event)
        else -> super.onTouchEvent(event)
    }

    private var centerPointF: PointF = PointF()
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerPointF.set((w / 2).toFloat(), (h / 2).toFloat())
    }

    private var defaultSize = dip2px(300f)//默认大小
    private var center: Int = 0//中心点
    private var oneRadius: Float = 0f//外层菱形圆半径
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        MeasureSpec.getSize(widthMeasureSpec).let { wSize ->
            when (MeasureSpec.EXACTLY) {
                MeasureSpec.getMode(widthMeasureSpec) -> wSize
                else -> min(wSize, defaultSize)
            }
        }.let { width ->
            MeasureSpec.getSize(heightMeasureSpec).let { hSize ->
                when (MeasureSpec.EXACTLY) {
                    MeasureSpec.getMode(heightMeasureSpec) -> hSize
                    else -> min(hSize, defaultSize)
                }
            }.let { height ->
                center = width / 2
                oneRadius = (center - paddingTop - 2 * spiderStrRect.height()).toFloat()
                setMeasuredDimension(width, height)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawSpiderName(canvas)//绘制蜘蛛名称
        for (position in 0 until spiderMaxLevel) {
            drawCobweb(canvas, position)//绘制蛛网
        }
        drawSpiderRadiusLine(canvas)//绘制连接中心的线
        drawSpiderLevel(canvas)//绘制蜘蛛层级
    }

    private var perimeter = 0.0
    private fun drawSpiderName(canvas: Canvas) {
        (360 / spiderNumber).toFloat().let { averageAngle ->
            (if (averageAngle > 0 && spiderNumber % 2 == 0) averageAngle / 2 else 0f).let { offsetAngle ->
                ((paddingTop + spiderStrRect.height()).toFloat() + oneRadius).let { currentRadius ->
                    for (position in 0 until spiderNumber) {
                        Math.toRadians((position * averageAngle + offsetAngle).toDouble()).toFloat()
                            .let { nextRadians ->
                                spiderList[position].spiderName.let { text ->
                                    val textWidth = paintSpiderName.measureText(text)
                                    val textHeight =
                                        paintSpiderName.fontMetrics.run { descent - ascent }
                                    canvas.drawText(
                                        text,
                                        (center + sin(nextRadians - rotateAngle) * currentRadius - textWidth / 2).toFloat(),
                                        (center - cos(nextRadians - rotateAngle) * currentRadius + textHeight / 4).toFloat(),
                                        paintSpiderName
                                    )
                                }
                            }
                    }
                }
            }
        }
        perimeter = 2.0 * PI * oneRadius.toDouble()
    }

    private fun drawCobweb(canvas: Canvas, index: Int) = Path().apply {
        (360 / spiderNumber).toFloat().let { averageAngle ->
            (if (averageAngle > 0 && spiderNumber % 2 == 0) averageAngle / 2 else 0f).let { offsetAngle ->
                ((index + 1) * oneRadius / spiderMaxLevel).let { currentRadius ->
                    for (position in 0 until spiderNumber) {
                        Math.toRadians((position * averageAngle + offsetAngle).toDouble()).toFloat()
                            .let { nextRadians ->
                                (center + sin(nextRadians - rotateAngle) * currentRadius).toFloat()
                                    .let { nextPointX ->
                                        (center - cos(nextRadians - rotateAngle) * currentRadius).toFloat()
                                            .let { nextPointY ->
                                                when (position) {
                                                    0 -> moveTo(nextPointX, nextPointY)
                                                    else -> lineTo(nextPointX, nextPointY)
                                                }
                                            }
                                    }
                            }
                    }
                }
            }
        }
        close()
    }.let { path ->
        canvas.drawPath(path, levelPaintList[spiderMaxLevel - index - 1])
        if (isSpiderLevelStroke) Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = setColorAlphaByInt(levelPaintList[spiderMaxLevel - 1].color, 50)
            if (spiderLevelStrokeWidth > 0) strokeWidth = spiderLevelStrokeWidth
        }.let { scoreStrokePaint -> canvas.drawPath(path, scoreStrokePaint) }
    }

    private fun drawSpiderRadiusLine(canvas: Canvas) =
        (360 / spiderNumber).toFloat().let { averageAngle ->
            (if (averageAngle > 0 && spiderNumber % 2 == 0) averageAngle / 2 else 0f).let { offsetAngle ->
                for (position in 0 until spiderNumber) {
                    Math.toRadians((position * averageAngle + offsetAngle).toDouble()).toFloat()
                        .let { nextRadians ->
                            canvas.drawLine(
                                center.toFloat(), center.toFloat(),
                                (center + sin(nextRadians - rotateAngle) * oneRadius).toFloat(),
                                (center - cos(nextRadians - rotateAngle) * oneRadius).toFloat(),
                                paintSpiderCenter
                            )
                        }
                }
            }
        }

    private fun drawSpiderLevel(canvas: Canvas) = Path().apply {
        (360 / spiderNumber).toFloat().let { averageAngle ->
            (if (averageAngle > 0 && spiderNumber % 2 == 0) averageAngle / 2 else 0f).let { offsetAngle ->
                for (position in 0 until spiderNumber) {
                    Math.toRadians((position * averageAngle + offsetAngle).toDouble()).toFloat()
                        .let { nextRadians ->
                            (oneRadius * (spiderList[position].spiderLevel / spiderMaxLevel).let { if (it < 1) it else 1f }).let { currentRadius ->
                                (center + sin(nextRadians - rotateAngle) * currentRadius).toFloat()
                                    .let { nextPointX ->
                                        (center - cos(nextRadians - rotateAngle) * currentRadius).toFloat()
                                            .let { nextPointY ->
                                                when (position) {
                                                    0 -> moveTo(nextPointX, nextPointY)
                                                    else -> lineTo(nextPointX, nextPointY)
                                                }
                                            }
                                    }
                            }
                        }
                }
            }
        }
        close()
    }.let { path ->
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL_AND_STROKE
            color = spiderLevelColor
        }.let { scorePaint -> canvas.drawPath(path, scorePaint) }
        if (isSpiderLevelStroke) Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = spiderLevelStrokeColor
            if (spiderLevelStrokeWidth > 0) strokeWidth = spiderLevelStrokeWidth
        }.let { scoreStrokePaint -> canvas.drawPath(path, scoreStrokePaint) }
    }
}