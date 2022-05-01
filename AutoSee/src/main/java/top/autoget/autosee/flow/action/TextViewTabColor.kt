package top.autoget.autosee.flow.action

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import top.autoget.autosee.R

class TextViewTabColor
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatTextView(context, attrs, defStyleAttr) {
    private val mPaint: Paint = Paint().apply {
        isAntiAlias = true
        isDither = true
    }
    private var isUseUserColor = false
    var defaultColor: Int = Color.GRAY
        set(defaultColor) {
            field = defaultColor
            isUseUserColor = false
            invalidate()
        }
    var changeColor: Int = Color.WHITE
        set(changeColor) {
            field = changeColor
            isUseUserColor = false
            invalidate()
        }

    init {
        mPaint.textSize = textSize
        if (attrs == null && defStyleAttr == 0) includeFontPadding = false
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewTabColor)
        try {
            typedArray.run {
                defaultColor =
                    getColor(R.styleable.TextViewTabColor_colorText_default_color, Color.GRAY)
                changeColor =
                    getColor(R.styleable.TextViewTabColor_colorText_change_color, Color.WHITE)
            }
        } finally {
            typedArray.recycle()
        }
    }

    companion object {
        const val DEC_LEFT = 1
        const val DEC_RIGHT = 2
    }

    var detection: Int = DEC_LEFT
        set(detection) {
            field = detection
            isUseUserColor = false
            invalidate()
        }
    var progress = 0f
        set(progress) {
            field = progress
            isUseUserColor = false
            invalidate()
        }
    private var mWidth = 0
    private var mHeight = 0
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = measuredWidth
        mHeight = measuredHeight
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        isUseUserColor = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) = when {
        isUseUserColor -> super.onDraw(canvas)
        else -> when (detection) {
            DEC_RIGHT -> {
                drawText(canvas, 0, mWidth, defaultColor)
                drawText(canvas, ((1 - progress) * mWidth).toInt(), mWidth, changeColor)
            }
            else -> {
                drawText(canvas, 0, mWidth, defaultColor)
                drawText(canvas, 0, (progress * mWidth).toInt(), changeColor)
            }
        }
    }

    private fun drawText(canvas: Canvas?, start: Int, end: Int, color: Int) {
        mPaint.color = color
        canvas?.run {
            save()
            clipRect(start, 0, end, mHeight)
            val text = text.toString()
            val metrics = mPaint.fontMetrics
            drawText(
                text, (mWidth - mPaint.measureText(text)) / 2,
                mHeight / 2 - (metrics.descent + metrics.ascent) / 2, mPaint
            )
            restore()
        }
    }
}