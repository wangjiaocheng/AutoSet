package top.autoget.autosee

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import top.autoget.autokit.DensityKit.dip2px
import top.autoget.autokit.DensityKit.sp2px
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

class Side
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    init {
        initAttrs(context, attrs)
    }

    companion object {
        private const val DEFAULT_TEXT_SIZE = 14f//sp
        private const val DEFAULT_MAX_OFFSET = 80f//dp
        const val POSITION_RIGHT = 0
        const val POSITION_LEFT = 1
        const val TEXT_ALIGN_CENTER = 0
        const val TEXT_ALIGN_LEFT = 1
        const val TEXT_ALIGN_RIGHT = 2
        private val DEFAULT_INDEX_ITEMS = arrayOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        )
    }

    var lazyRespond = false
    var textColor: Int = Color.GRAY
        set(textColor) {
            field = textColor
            mPaint.color = textColor
            invalidate()
        }
    var textSize: Float = sp2px(DEFAULT_TEXT_SIZE).toFloat()
        set(textSize) {
            if (this.textSize != textSize) {
                field = textSize
                mPaint.textSize = textSize
                invalidate()
            }
        }
    var maxOffset: Float = dip2px(DEFAULT_MAX_OFFSET).toFloat()
        set(maxOffset) {
            field = maxOffset
            invalidate()
        }
    var sideBarPosition: Int = POSITION_RIGHT
        set(position) {
            require(!(position != POSITION_RIGHT && position != POSITION_LEFT)) { "the position must be POSITION_RIGHT or POSITION_LEFT" }
            field = position
            requestLayout()
        }
    var sideTextAlignment: Int = TEXT_ALIGN_CENTER
        set(textAlignment) {
            if (field != textAlignment) {
                mPaint.textAlign = when (textAlignment) {
                    TEXT_ALIGN_CENTER -> Paint.Align.CENTER
                    TEXT_ALIGN_LEFT -> Paint.Align.LEFT
                    TEXT_ALIGN_RIGHT -> Paint.Align.RIGHT
                    else -> throw IllegalArgumentException("the alignment must be TEXT_ALIGN_CENTER, TEXT_ALIGN_LEFT or TEXT_ALIGN_RIGHT")
                }
                field = textAlignment
                invalidate()
            }
        }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Side)
        try {
            typedArray.run {
                lazyRespond = getBoolean(R.styleable.Side_sidebar_lazy_respond, false)
                textColor = getColor(R.styleable.Side_sidebar_text_color, Color.GRAY)
                textSize = getDimension(
                    R.styleable.Side_sidebar_text_size, sp2px(DEFAULT_TEXT_SIZE).toFloat()
                )
                maxOffset = getDimension(
                    R.styleable.Side_sidebar_max_offset, dip2px(DEFAULT_MAX_OFFSET).toFloat()
                )
                sideBarPosition = getInt(R.styleable.Side_sidebar_position, POSITION_RIGHT)
                sideTextAlignment =
                    getInt(R.styleable.Side_sidebar_text_alignment, TEXT_ALIGN_CENTER)
            }
        } finally {
            typedArray.recycle()
        }
    }

    private var mPaint: Paint = Paint().apply {
        isAntiAlias = true
        color = textColor
        textSize = textSize
        textAlign = when (sideTextAlignment) {
            TEXT_ALIGN_CENTER -> Paint.Align.CENTER
            TEXT_ALIGN_LEFT -> Paint.Align.LEFT
            TEXT_ALIGN_RIGHT -> Paint.Align.RIGHT
            else -> Paint.Align.CENTER
        }
    }
    var indexItems: Array<String> = DEFAULT_INDEX_ITEMS
        set(indexItems) {
            field = indexItems
            requestLayout()
        }
    private var mBarWidth = 0f
    private var mIndexItemHeight = 0f
    private var mBarHeight = 0f
    private val mStartTouchingArea = RectF()
    private var mFirstItemBaseLineY = 0f
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        for (indexItem in indexItems) {
            mBarWidth = max(mBarWidth, mPaint.measureText(indexItem))
        }
        val areaLeft: Float =
            if (sideBarPosition == POSITION_LEFT) 0f else width - mBarWidth - paddingRight
        val areaRight =
            if (sideBarPosition == POSITION_LEFT) paddingLeft + areaLeft + mBarWidth else width.toFloat()
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val fontMetrics = mPaint.fontMetrics
        mIndexItemHeight = fontMetrics.bottom - fontMetrics.top
        mBarHeight = indexItems.size * mIndexItemHeight
        val areaTop = height / 2 - mBarHeight / 2
        val areaBottom = areaTop + mBarHeight
        mStartTouchingArea[areaLeft, areaTop, areaRight] = areaBottom
        mFirstItemBaseLineY = height / 2 -
                indexItems.size * mIndexItemHeight / 2 + (mIndexItemHeight / 2 -
                (fontMetrics.descent - fontMetrics.ascent) / 2) - fontMetrics.ascent
    }

    private var mCurrentIndex = -1
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in indexItems.indices) {
            val scale = getItemScale(i)
            val baseLineX = when (sideBarPosition) {
                POSITION_LEFT -> when (sideTextAlignment) {
                    TEXT_ALIGN_CENTER -> paddingLeft + mBarWidth / 2 + maxOffset * scale
                    TEXT_ALIGN_LEFT -> paddingLeft + maxOffset * scale
                    TEXT_ALIGN_RIGHT -> paddingLeft + mBarWidth + maxOffset * scale
                    else -> 0f
                }
                else -> when (sideTextAlignment) {
                    TEXT_ALIGN_CENTER -> width - paddingRight - mBarWidth / 2 - maxOffset * scale
                    TEXT_ALIGN_RIGHT -> width - paddingRight - maxOffset * scale
                    TEXT_ALIGN_LEFT -> width - paddingRight - mBarWidth - maxOffset * scale
                    else -> 0f
                }
            }
            canvas.drawText(
                indexItems[i], baseLineX, mFirstItemBaseLineY + mIndexItemHeight * i,
                mPaint.apply {
                    alpha = if (i == mCurrentIndex) 255 else (255 * (1 - scale)).toInt()
                    textSize = textSize + textSize * scale
                }
            )
        }
        mPaint.apply {
            alpha = 255
            textSize = textSize
        }
    }

    private var mCurrentY = -1f
    private fun getItemScale(index: Int): Float = when (mCurrentIndex) {
        -1 -> 0f
        else -> {
            val distance =
                abs(mCurrentY - (mIndexItemHeight * index + mIndexItemHeight / 2)) / mIndexItemHeight
            max(1 - distance.pow(2) / 16, 0f)
        }
    }

    private var mStartTouching = false

    interface OnSelectIndexItemListener {
        fun onSelectIndexItem(index: String?)
    }

    var onSelectIndexItemListener: OnSelectIndexItemListener? = null
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when {
            indexItems.isEmpty() -> return super.onTouchEvent(event)
            else -> {
                val eventY = event.y
                mCurrentIndex = getSelectedIndex(eventY)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> return when {
                        mStartTouchingArea.contains(event.x, eventY) -> {
                            mStartTouching = true
                            if (!lazyRespond)
                                onSelectIndexItemListener?.onSelectIndexItem(indexItems[mCurrentIndex])
                            invalidate()
                            true
                        }
                        else -> {
                            mCurrentIndex = -1
                            false
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (mStartTouching && !lazyRespond)
                            onSelectIndexItemListener?.onSelectIndexItem(indexItems[mCurrentIndex])
                        invalidate()
                        return true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (lazyRespond)
                            onSelectIndexItemListener?.onSelectIndexItem(indexItems[mCurrentIndex])
                        mCurrentIndex = -1
                        mStartTouching = false
                        invalidate()
                        return true
                    }
                }
                return super.onTouchEvent(event)
            }
        }
    }

    private fun getSelectedIndex(eventY: Float): Int {
        mCurrentY = eventY - (height / 2 - mBarHeight / 2)
        return when {
            mCurrentY > 0 -> (mCurrentY / mIndexItemHeight).toInt()
                .let { if (it < indexItems.size) it else indexItems.size - 1 }
            else -> 0
        }
    }
}