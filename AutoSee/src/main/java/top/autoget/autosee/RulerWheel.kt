package top.autoget.autosee

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.View
import android.widget.OverScroller
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.StringKit.isSpace
import kotlin.math.*

class RulerWheel
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr), GestureDetector.OnGestureListener {
    init {
        init(attrs)
    }

    private var colorMark: Int = -0x111112
    private var colorMarkText: Int = -0x99999a
    private var colorHighlight: Int = -0x8b3c7
    private var colorFadeMark: Int = colorHighlight and -0x55000001
    private var markWidth: Float = 0f
    private var markWidthCenter: Float = 0f
    private var textSizeNormal: Float = 0f
    private var textSizeCenter: Float = 0f
    private var cursorSize: Float = 0f
    private var spaceTop: Float = 0f
    private var spaceBottom: Float = 0f
    private var intervalFactor = 1.2f
    private var markRatio = 0.7f
    var markAdditionCenter: String? = null
        set(markAdditionCenter) {
            field = markAdditionCenter
            calcIntervalDis()
            invalidate()
        }
    private var markPaint: Paint = Paint()
    private var markTextPaint: TextPaint = TextPaint()
    private var overScroller: OverScroller? = null
    private var gestureDetectorCompat: GestureDetectorCompat? = null
    var selectedPosition = -1
        set(selectedPosition) {
            field = selectedPosition
            post {
                scrollTo((selectedPosition * intervalDis - maxOverScrollDistance).toInt(), 0)
                invalidate()
                refreshCenter()
            }
        }

    private fun init(attrs: AttributeSet?) {
        resources.displayMetrics.density.let {
            markWidth = it
            markWidthCenter = it * 1.5f + 0.5f
            textSizeNormal = it * 18
            textSizeCenter = it * 22
            cursorSize = it * 18
            spaceTop = cursorSize + it * 2
            spaceBottom = it * 6
        }
        val typedArray: TypedArray? =
            attrs?.let { context.obtainStyledAttributes(it, R.styleable.RulerWheel) }
        try {
            typedArray?.run {
                colorMark = getColor(R.styleable.RulerWheel_ColorMark, colorMark)
                colorMarkText = getColor(R.styleable.RulerWheel_ColorMarkText, colorMarkText)
                colorHighlight = getColor(R.styleable.RulerWheel_ColorHighlight, colorHighlight)
                textSizeNormal =
                    getDimension(R.styleable.RulerWheel_MarkTextSize, textSizeNormal)
                textSizeCenter =
                    getDimension(R.styleable.RulerWheel_MarkTextSizeCenter, textSizeCenter)
                cursorSize = getDimension(R.styleable.RulerWheel_CursorSize, cursorSize)
                intervalFactor = getFloat(R.styleable.RulerWheel_IntervalFactor, intervalFactor)
                markRatio = getFloat(R.styleable.RulerWheel_MarkRatio, markRatio)
                markAdditionCenter = getString(R.styleable.RulerWheel_MarkAdditionalCenter)
            }
        } finally {
            typedArray?.recycle()
        }
        intervalFactor = max(1f, intervalFactor)
        markRatio = min(1f, markRatio)
        markPaint.apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = colorMark
            strokeWidth = markWidthCenter
        }
        markTextPaint.apply {
            flags = Paint.ANTI_ALIAS_FLAG
            color = colorHighlight
            textSize = textSizeCenter
            textAlign = Paint.Align.CENTER
        }
        calcIntervalDis()
        overScroller = OverScroller(context)
        gestureDetectorCompat = GestureDetectorCompat(context, this)
        selectedPosition = 0
    }

    private var markCount: Int = 0
    private var contentRectF: RectF = RectF()
    var items: MutableList<String> = mutableListOf()
        set(items) {
            markCount = field.apply {
                clear()
                addAll(items)
            }.size
            if (markCount > 0) {
                minSelectableIndex = max(minSelectableIndex, 0)
                maxSelectableIndex = min(maxSelectableIndex, markCount - 1)
            }
            contentRectF.set(0f, 0f, (markCount - 1) * intervalDis, measuredHeight.toFloat())
            selectedPosition = min(selectedPosition, markCount)
            calcIntervalDis()
            invalidate()
        }
    private var additionCenterMarkWidth: Float = 0f
    private var intervalDis: Float = 0f
    private fun calcIntervalDis() {
        val rect = Rect()
        var max = 0
        when {
            items.size > 0 -> for (item in items) {
                markTextPaint.getTextBounds(item, 0, item.length, rect)
                if (rect.width() > max) max = rect.width()
            }
            else -> {
                "888888".let { markTextPaint.getTextBounds(it, 0, it.length, rect) }
                max = rect.width()
            }
        }
        if (isNotSpace(markAdditionCenter)) {
            markTextPaint.apply { textSize = textSizeNormal }
                .getTextBounds(markAdditionCenter, 0, markAdditionCenter?.length ?: 0, rect)
            additionCenterMarkWidth = rect.width().toFloat()
            max += rect.width()
        }
        intervalDis = max * intervalFactor
    }

    interface OnWheelItemSelectedListener {
        fun onWheelItemChanged(rulerWheel: RulerWheel, position: Int)
        fun onWheelItemSelected(rulerWheel: RulerWheel, position: Int)
    }

    var onWheelItemSelectedListener: OnWheelItemSelectedListener? = null
    private fun refreshCenter(offsetX: Int = scrollX) =
        safeCenter(((offsetX + maxOverScrollDistance) / intervalDis).roundToInt()).let { index ->
            if (selectedPosition != index) {
                selectedPosition = index
                onWheelItemSelectedListener?.onWheelItemChanged(this, selectedPosition)
            }
        }

    var minSelectableIndex: Int = Integer.MIN_VALUE
        set(minSelectableIndex) {
            field = min(maxSelectableIndex, minSelectableIndex)
            safeCenter(selectedPosition)
                .let { afterCenter ->
                    if (afterCenter != selectedPosition) selectedPosition = afterCenter
                }
        }
    var maxSelectableIndex: Int = Integer.MAX_VALUE
        set(maxSelectableIndex) {
            field = max(minSelectableIndex, maxSelectableIndex)
            safeCenter(selectedPosition)
                .let { afterCenter ->
                    if (afterCenter != selectedPosition) selectedPosition = afterCenter
                }
        }

    private fun safeCenter(center: Int): Int = when {
        center < minSelectableIndex -> minSelectableIndex
        center > maxSelectableIndex -> maxSelectableIndex
        else -> center
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec))

    private fun measureWidth(widthMeasureSpec: Int): Int =
        when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.UNSPECIFIED -> suggestedMinimumWidth
            else -> suggestedMinimumWidth
        }

    private fun measureHeight(heightMeasure: Int): Int =
        MeasureSpec.getSize(heightMeasure).let { measureSize ->
            (spaceBottom + spaceTop * 2 + textSizeCenter).toInt().let { result ->
                when (MeasureSpec.getMode(heightMeasure)) {
                    MeasureSpec.EXACTLY -> max(result, measureSize)
                    MeasureSpec.AT_MOST -> min(result, measureSize)
                    else -> result
                }
            }
        }

    private var mHeight: Int = 0
    private var maxOverScrollDistance: Float = 0f
    private var viewScopeSize: Int = 0
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            mHeight = h
            maxOverScrollDistance = w / 2f
            viewScopeSize = ceil(maxOverScrollDistance / intervalDis).toInt()
            contentRectF.set(0f, 0f, (markCount - 1) * intervalDis, h.toFloat())
        }
    }

    private val centerIndicatorPath = Path()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        (cursorSize / 2f).let { sizeDiv2 ->
            (cursorSize / 3f).let { sizeDiv3 ->
                canvas.drawPath(centerIndicatorPath.apply {
                    reset()
                    moveTo(maxOverScrollDistance - sizeDiv2 + scrollX, 0f)
                    rLineTo(0f, sizeDiv3)
                    rLineTo(sizeDiv2, sizeDiv2)
                    rLineTo(sizeDiv2, -sizeDiv2)
                    rLineTo(0f, -sizeDiv3)
                    close()
                }, markPaint.apply { color = colorHighlight })
            }
        }
        (intervalDis / 5).let { tempDis ->
            (mHeight - spaceTop - spaceBottom - textSizeCenter).let { markHeight ->
                min(
                    (markHeight - markWidth) / 2, markHeight * (1 - markRatio) / 2
                ).let { smallMarkShrinkY ->
                    var start = max(selectedPosition - viewScopeSize, -viewScopeSize * 2)
                    var end =
                        min(selectedPosition + viewScopeSize + 1, markCount + viewScopeSize * 2)
                    when (selectedPosition) {
                        minSelectableIndex -> start -= viewScopeSize
                        maxSelectableIndex -> end += viewScopeSize
                    }
                    var x = start * intervalDis
                    for (i in start until end) {
                        for (offset in -2..2) {
                            (x + offset * tempDis).let { ox ->
                                (if (offset == 0) spaceTop else spaceTop + smallMarkShrinkY).let { startY ->
                                    (if (offset == 0) spaceTop + markHeight else spaceTop + markHeight - smallMarkShrinkY).let { stopY ->
                                        canvas.drawLine(ox, startY, ox, stopY, markPaint.apply {
                                            color = when {
                                                i in 0..markCount && i == selectedPosition ->
                                                    when (abs(offset)) {
                                                        0 -> colorHighlight
                                                        1 -> colorFadeMark
                                                        else -> colorMark
                                                    }
                                                else -> colorMark
                                            }
                                            strokeWidth =
                                                if (offset == 0) markWidthCenter else markWidth
                                        })
                                    }
                                }
                            }
                        }
                        if (markCount > 0 && i >= 0 && i < markCount) items[i].let { temp ->
                            when (selectedPosition) {
                                i -> {
                                    markTextPaint.apply {
                                        color = colorHighlight
                                        textSize = textSizeCenter
                                    }
                                    when {
                                        isSpace(markAdditionCenter) -> canvas.drawText(
                                            temp, 0, temp.length, x,
                                            mHeight - spaceBottom, markTextPaint
                                        )
                                        else -> canvas.run {
                                            drawText(
                                                temp, 0, temp.length,
                                                x - additionCenterMarkWidth / 2f,
                                                mHeight - spaceBottom, markTextPaint
                                            )
                                            drawText(
                                                markAdditionCenter!!, x + markTextPaint.apply {
                                                    textSize = textSizeNormal
                                                }.measureText(temp, 0, temp.length) / 2f,
                                                mHeight - spaceBottom, markTextPaint
                                            )
                                        }
                                    }
                                }
                                else -> canvas.drawText(
                                    temp, 0, temp.length, x, mHeight - spaceBottom,
                                    markTextPaint.apply {
                                        color = colorMarkText
                                        textSize = textSizeNormal
                                    })
                            }
                        }
                        x += intervalDis
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = when {
        items.size != 0 && isEnabled -> {
            var boolean = gestureDetectorCompat?.onTouchEvent(event) ?: false
            if (!isFling && event.action == MotionEvent.ACTION_UP) {
                autoSettle()
                boolean = true
            }
            boolean || super.onTouchEvent(event)
        }
        else -> false
    }

    private var isFling = false
    override fun computeScroll() {
        super.computeScroll()
        overScroller?.run {
            when {
                computeScrollOffset() -> {
                    scrollTo(currX, currY)
                    refreshCenter()
                    invalidate()
                }
                else -> if (isFling) {
                    isFling = false
                    autoSettle()
                }
            }
        }
    }

    private var lastSelectedIndex = -1
    private fun autoSettle() {
        overScroller?.startScroll(
            scrollX, 0,
            (selectedPosition * intervalDis - scrollX - maxOverScrollDistance).toInt(), 0
        )
        postInvalidate()
        if (lastSelectedIndex != selectedPosition) onWheelItemSelectedListener
            ?.onWheelItemSelected(this, selectedPosition.apply { lastSelectedIndex = this })
    }

    fun smoothSelectIndex(index: Int) {
        overScroller?.run {
            if (!isFinished) abortAnimation()
            startScroll(scrollX, 0, ((index - selectedPosition) * intervalDis).toInt(), 0)
        }
        invalidate()
    }

    override fun onDown(e: MotionEvent): Boolean {
        overScroller?.run { if (!isFinished) forceFinished(false) }
        isFling = false
        parent?.requestDisallowInterceptTouchEvent(true)
        return true
    }

    override fun onShowPress(e: MotionEvent) {}
    override fun onLongPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean {
        playSoundEffect(SoundEffectConstants.CLICK)
        refreshCenter((scrollX + e.x - maxOverScrollDistance).toInt())
        autoSettle()
        return true
    }

    override fun onScroll(
        e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float
    ): Boolean {
        scrollBy(
            when {
                scrollX < minSelectableIndex * intervalDis - maxOverScrollDistance * 2 -> 0f
                scrollX < minSelectableIndex * intervalDis - maxOverScrollDistance -> distanceX / 4
                scrollX > contentRectF.width() - (markCount - maxSelectableIndex - 1) * intervalDis -> 0f
                scrollX > contentRectF.width() - (markCount - maxSelectableIndex - 1) * intervalDis - maxOverScrollDistance -> distanceX / 4
                else -> distanceX
            }.toInt(), 0
        )
        refreshCenter()
        return true
    }

    override fun onFling(
        e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float
    ): Boolean = when {
        scrollX < minSelectableIndex * intervalDis - maxOverScrollDistance ||
                scrollX > contentRectF.width() - maxOverScrollDistance - (markCount - 1 - maxSelectableIndex) * intervalDis -> false
        else -> {
            isFling = true
            fling(-velocityX.toInt(), 0)
            true
        }
    }

    fun fling(velocityX: Int, velocityY: Int) {
        overScroller?.fling(
            scrollX, scrollY, velocityX, velocityY,
            (minSelectableIndex * intervalDis - maxOverScrollDistance).toInt(),
            (contentRectF.width() - maxOverScrollDistance - (markCount - 1 - maxSelectableIndex) * intervalDis).toInt(),
            0, 0, maxOverScrollDistance.toInt(), 0
        )
        ViewCompat.postInvalidateOnAnimation(this)
    }

    internal class SavedState : BaseSavedState {
        var min: Int = 0
        var max: Int = 0
        var index: Int = 0

        constructor(superState: Parcelable) : super(superState)
        private constructor(parcel: Parcel) : super(parcel) {
            min = parcel.readInt()
            max = parcel.readInt()
            index = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(min)
            out.writeInt(max)
            out.writeInt(index)
        }

        override fun toString(): String =
            "RulerWheelView.SavedState{${Integer.toHexString(System.identityHashCode(this))} min=$min max=$max index=$index}"

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState = SavedState(parcel)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    public override fun onSaveInstanceState(): Parcelable? = super.onSaveInstanceState()?.let {
        SavedState(it).apply {
            min = minSelectableIndex
            max = maxSelectableIndex
            index = selectedPosition
        }
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        (state as SavedState).run {
            super.onRestoreInstanceState(superState)
            minSelectableIndex = min
            maxSelectableIndex = max
            selectedPosition = index
        }
        requestLayout()
    }
}