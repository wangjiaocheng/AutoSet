package top.autoget.autosee

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import top.autoget.autokit.DataKit.formatAmount
import top.autoget.autokit.DataKit.formatTwo
import kotlin.math.floor
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ProgressRound @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    var circleColor: Int = Color.WHITE
    var circleProgressColor: Int = Color.parseColor("#F6B141")
    var textColor: Int = Color.GREEN
    var textSize: Float = 15f
    var roundWidth: Float = 20f

    @get:Synchronized
    var max: Double = 100.0
        @Synchronized set(max) {
            field = max.apply { require(max >= 0) { "max not less than 0" } }
        }
    private var textIsDisplayable: Boolean = true//是否显示中间进度

    companion object {
        const val STROKE = 0
        const val FILL = 1
    }

    private var paintStyle: Int = STROKE

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ProgressRound)
        try {
            typedArray.run {
                circleColor = getColor(R.styleable.ProgressRound_circleColor, Color.WHITE)
                circleProgressColor = getColor(
                    R.styleable.ProgressRound_circleProgressColor, Color.parseColor("#F6B141")
                )
                textColor = getColor(R.styleable.ProgressRound_textColor, Color.GREEN)
                textSize = getDimension(R.styleable.ProgressRound_PrTextSize, 15f)
                roundWidth = getDimension(R.styleable.ProgressRound_roundWidth, 20f)
                max = getInteger(R.styleable.ProgressRound_max, 100).toDouble()
                textIsDisplayable = getBoolean(R.styleable.ProgressRound_textIsDisplayable, true)
                paintStyle = getInt(R.styleable.ProgressRound_style, STROKE)
            }
        } finally {
            typedArray.recycle()
        }
    }

    private val paintText: Paint = Paint()
    private val paintMoney: Paint = Paint()
    private val paintMoneyD: Paint = Paint()
    private val paint: Paint = Paint()
    var progress: Double = 0.0
        @Synchronized get() = field.apply { require(field >= 0) { "progress not less than 0" } }
        @Synchronized set(progress) {
            when {
                progress < 0 -> field = progress
                progress > max -> field = max
                progress in 0.0..max -> {
                    field = progress
                    postInvalidate()//非UI线程刷新
                }
            }
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        (width / 2 - 90).let { centre ->
            (centre - roundWidth / 2).let { radius ->
                RectF().apply {
                    left = centre - radius + 90f
                    top = centre - radius + 90f
                    right = centre + radius + 90f
                    bottom = centre + radius + 90f
                }.let { oval ->
                    canvas.run {
                        paintText.apply {
                            isAntiAlias = true
                            color = circleColor
                            textSize = 36f
                        }//画最外层大圆环
                        drawText(
                            "0元", (radius - sqrt(2.0) * (radius / 2) + 10).toFloat(),
                            (2 * radius - sqrt(2.0) * (radius / 4) + 130).toFloat(), paintText
                        )//左边最小值
                        drawText(
                            "${max}元", (radius + sqrt(2.0) * (radius / 2) + 138.0).toFloat(),
                            (2 * radius - sqrt(2.0) * (radius / 4) + 130).toFloat(), paintText
                        )//右边最大值
                        paintMoney.apply {
                            isAntiAlias = true
                            color = circleColor
                            textSize = 65f
                        }
                        when {
                            progress < 50 -> (progress * 1 + floor(Math.random() * max)).let { money ->
                                drawText(
                                    "$money",
                                    centre + 90 - paintMoney.measureText("$money") / 2 - 15,
                                    centre + 165f, paintMoney
                                )//右边最大值
                            }
                            else -> drawText(
                                formatAmount("$progress", formatTwo),
                                centre + 90 - paintMoney.measureText(
                                    formatAmount("$progress", formatTwo)
                                ) / 2 - 15, centre + 105f, paintMoney
                            )//右边最大值
                        }
                        paintMoneyD.apply {
                            isAntiAlias = true
                            color = circleColor
                            textSize = 48f
                        }
                        drawText(
                            "元", centre + 90 + paintMoney.measureText(
                                formatAmount("$progress", formatTwo)
                            ) / 2 - 10, centre + 105f, paintMoneyD
                        )//右边最大值
                        paint.apply {
                            isAntiAlias = true
                            color = circleColor
                            strokeWidth = roundWidth
                            strokeCap = Paint.Cap.ROUND//边缘圆角
                            style = Paint.Style.STROKE
                        }//drawRect(0f, 0f, width.toFloat(), width.toFloat(), paint)//画正方形
                        drawArc(oval, 135f, 270f, false, paint)
                        paint.apply {
                            color = circleProgressColor
                            strokeWidth = roundWidth
                            if (paintStyle == STROKE) style = Paint.Style.STROKE
                        }//画进度圆弧
                        if (progress >= 0) drawArc(
                            oval, 135f, 270 * (progress.toFloat() / max.toFloat()),
                            false, paint
                        )
                        paint.apply {
                            color = textColor
                            strokeWidth = 0f
                            textSize = textSize
                            typeface = Typeface.DEFAULT_BOLD
                        }//画进度百分比
                        (progress.toFloat() / max.toFloat() * 100).toInt().let { percent ->
                            if (textIsDisplayable && percent != 0 && paintStyle == STROKE)
                                drawText(
                                    "$percent%", centre + 90 - paint.measureText("$percent%") / 2,
                                    centre + 90 + textSize / 2, paint
                                )
                        }//先转Float再除，否则始终0
                    }
                }
            }
        }
    }
}