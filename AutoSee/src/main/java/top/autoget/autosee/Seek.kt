package top.autoget.autosee

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import top.autoget.autokit.DensityKit.dip2px
import top.autoget.autokit.ImageKit
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class Seek @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {
    init {
        initAttrs(context, attrs)
    }

    private var cellsCount = 1//默认1，大于1自动切回刻度模式
    private var reserveValue = 0f
    var min = 0f
        private set//真实最小值
    var max = 0f
        private set//真实最大值
    private var mThumbResId = 0//按钮背景
    private var mProgressHintBGId = 0//进度提示背景
    private var mProgressHintBG: Bitmap = when (mProgressHintBGId) {
        0 -> BitmapFactory.decodeResource(resources, R.mipmap.seek_hint)
        else -> BitmapFactory.decodeResource(resources, mProgressHintBGId)
    }
    private var colorLineSelected = 0//选择过的进度条颜色
    private var colorLineEdge = 0//未选择的进度条颜色
    private var colorPrimary = 0
    private var colorSecondary = 0
    private var mTextArray: Array<CharSequence>? = null//刻度上显示的文字
    private var mHideProgressHint = false
    var isHintHolder = false
    private var textPadding = 0//刻度与进度条间间距
    private var mTextSize = 0//刻度文字与提示文字大小
    private var mHintBGHeight = 0f//进度提示背景高度，宽度如果小于等于0自适应调整
    private var mHintBGWith = 0f
    private var mSeekBarHeight = 0
    private var mHintBGPadding = 0//进度提示背景与按钮间距离
    private var mThumbSize = 0
    private var mCellMode = 0//刻度模式：number根据数字实际比例排列；other均分排列
    private var mSeekBarMode = 0//single模式1是SeekView，range模式2是SeekBar
    private var leftSV: SeekView? = null
    fun setLeftProgressDescription(progress: String?) = leftSV?.setProgressHint(progress)
    private var rightSV: SeekView? = null
    fun setRightProgressDescription(progress: String?) = rightSV?.setProgressHint(progress)
    fun setProgressDescription(progress: String?) {
        leftSV?.setProgressHint(progress)
        rightSV?.setProgressHint(progress)
    }

    private var defaultPaddingLeftAndRight = 0
    private var defaultPaddingTop = 0
    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Seek)
        try {
            typedArray.run {
                cellsCount = getInt(R.styleable.Seek_cells, 1)
                reserveValue = getFloat(R.styleable.Seek_reserve, 0f)
                min = getFloat(R.styleable.Seek_minProgress, 0f)
                max = getFloat(R.styleable.Seek_maxProgress, 100f)
                mThumbResId =
                    getResourceId(R.styleable.Seek_seekBarResId, R.mipmap.seek_thumb)
                mProgressHintBGId = getResourceId(R.styleable.Seek_progressHintResId, 0)
                colorLineSelected = getColor(R.styleable.Seek_lineColorSelected, -0xb4269e)
                colorLineEdge = getColor(R.styleable.Seek_lineColorEdge, -0x282829)
                colorPrimary = getColor(R.styleable.Seek_thumbPrimaryColor, 0)
                colorSecondary = getColor(R.styleable.Seek_thumbSecondaryColor, 0)
                mTextArray = getTextArray(R.styleable.Seek_markTextArray)
                mHideProgressHint = getBoolean(R.styleable.Seek_hideProgressHint, false)
                isHintHolder = getBoolean(R.styleable.Seek_isHintHolder, false)
                textPadding = getDimension(
                    R.styleable.Seek_textPadding, dip2px(7f).toFloat()
                ).toInt()
                mTextSize = getDimension(
                    R.styleable.Seek_textSize2, dip2px(12f).toFloat()
                ).toInt()
                mHintBGHeight = getDimension(R.styleable.Seek_hintBGHeight, 0f)
                mHintBGWith = getDimension(R.styleable.Seek_hintBGWith, 0f)
                mSeekBarHeight = getDimension(
                    R.styleable.Seek_seekBarHeight, dip2px(2f).toFloat()
                ).toInt()
                mHintBGPadding = getDimension(R.styleable.Seek_hintBGPadding, 0f)
                    .toInt()
                mThumbSize = getDimension(
                    R.styleable.Seek_thumbSize, dip2px(26f).toFloat()
                ).toInt()
                mCellMode = getInt(R.styleable.Seek_cellMode, 0)
                mSeekBarMode = getInt(R.styleable.Seek_seekBarMode, 2)
            }
        } finally {
            typedArray.recycle()
        }
        when (mSeekBarMode) {
            2 -> {
                leftSV = SeekView(-1)
                rightSV = SeekView(1)
            }
            else -> leftSV = SeekView(-1)
        }
        defaultPaddingLeftAndRight = when (mHintBGWith) {
            0f -> dip2px(25f)
            else -> max((mHintBGWith / 2 + dip2px(5f)).toInt(), dip2px(25f))
        }
        setRules(min, max, reserveValue, cellsCount)
        defaultPaddingTop = mSeekBarHeight / 2
        mHintBGHeight =
            if (mHintBGHeight == 0f) mCursorPaint.measureText("国") * 3 else mHintBGHeight
    }

    private var heightNeeded = 0
    private var lineTop = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        heightNeeded = 2 * lineTop + mSeekBarHeight
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        heightSize = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)//精确
            MeasureSpec.AT_MOST -> MeasureSpec.makeMeasureSpec(
                if (heightSize < heightNeeded) heightSize else heightNeeded, MeasureSpec.EXACTLY
            )//最大
            else -> MeasureSpec.makeMeasureSpec(heightNeeded, MeasureSpec.EXACTLY)
        }//UNSPECIFIED未指定
        super.onMeasure(widthMeasureSpec, heightSize)
    }

    fun setRange(min: Float, max: Float) = setRules(min, max, reserveCount.toFloat(), cellsCount)
    private var reservePercent = 0f
    private var cellsPercent = 0f
    private var reserveCount = 0//两个按钮间最小距离
    fun setRules(min: Float, max: Float, reserve: Float, cells: Int) {
        require(max > min) { "setRules() max must be greater than min ! #max:$max #min:$min" }
        this.max = max
        this.min = min
        var mMin = min
        var mMax = max
        if (min < 0) {
            offsetValue = -min
            mMin = min + offsetValue
            mMax = max + offsetValue
        }
        minValue = mMin
        maxValue = mMax
        require(reserve >= 0) { "setRules() reserve must be greater than zero ! #reserve:$reserve" }
        require(reserve < mMax - mMin) { "setRules() reserve must be less than (max - min) ! #reserve:$reserve #max - min:${mMax - mMin}" }
        reserveValue = reserve
        reservePercent = reserve / (mMax - mMin)
        require(cells >= 1) { "setRules() cells must be greater than 1 ! #cells:$cells" }
        cellsCount = cells
        cellsPercent = 1f / cellsCount
        reserveCount =
            (reservePercent / cellsPercent + if (reservePercent % cellsPercent != 0f) 1 else 0).toInt()
        when {
            cellsCount > 1 -> when (mSeekBarMode) {
                2 -> when {
                    (leftSV?.currPercent ?: 0f) + cellsPercent * reserveCount <= 1 &&
                            (leftSV?.currPercent ?: 0f) + cellsPercent * reserveCount >
                            (rightSV?.currPercent ?: 0f) -> rightSV?.currPercent =
                        (leftSV?.currPercent ?: 0f) + cellsPercent * reserveCount
                    (rightSV?.currPercent ?: 0f) - cellsPercent * reserveCount >= 0 &&
                            (rightSV?.currPercent ?: 0f) - cellsPercent * reserveCount <
                            (leftSV?.currPercent ?: 0f) -> leftSV?.currPercent =
                        (rightSV?.currPercent ?: 0f) - cellsPercent * reserveCount
                }
                else -> {
                    if (1 - cellsPercent * reserveCount >= 0 &&
                        1 - cellsPercent * reserveCount < (leftSV?.currPercent ?: 0f)
                    ) leftSV?.currPercent = 1 - cellsPercent * reserveCount
                }
            }
            else -> when (mSeekBarMode) {
                2 -> when {
                    (leftSV?.currPercent ?: 0f) + reservePercent <= 1 &&
                            (leftSV?.currPercent ?: 0f) + reservePercent >
                            (rightSV?.currPercent ?: 0f) -> rightSV?.currPercent =
                        (leftSV?.currPercent ?: 0f) + reservePercent
                    (rightSV?.currPercent ?: 0f) - reservePercent >= 0 &&
                            (rightSV?.currPercent ?: 0f) - reservePercent <
                            (leftSV?.currPercent ?: 0f) ->
                        leftSV?.currPercent = (rightSV?.currPercent ?: 0f) - reservePercent
                }
                else -> {
                    if (1 - reservePercent >= 0 && 1 - reservePercent < (leftSV?.currPercent ?: 0f))
                        leftSV?.currPercent = 1 - reservePercent
                }
            }
        }
        invalidate()
    }

    private var lineLeft = 0
    private var lineRight = 0
    private var lineBottom = 0
    private val line = RectF()
    private var lineCorners = 0
    private var lineWidth = 0
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        lineLeft = defaultPaddingLeftAndRight + paddingLeft
        lineRight = w - lineLeft - paddingRight
        lineTop = mHintBGHeight.toInt() + mThumbSize / 2 - mSeekBarHeight / 2
        lineBottom = lineTop + mSeekBarHeight
        line[lineLeft.toFloat(), lineTop.toFloat(), lineRight.toFloat()] = lineBottom.toFloat()
        lineCorners = ((lineBottom - lineTop) * 0.45f).toInt()
        lineWidth = lineRight - lineLeft
        leftSV?.onSizeChanged(
            lineLeft, lineBottom, mThumbSize, lineWidth, cellsCount > 1, mThumbResId, context
        )
        if (mSeekBarMode == 2) rightSV?.onSizeChanged(
            lineLeft, lineBottom, mThumbSize, lineWidth, cellsCount > 1, mThumbResId, context
        )
    }//计算进度条位置，根据它初始化两个按钮位置

    private var mPartLength = 0
    private var maxValue = 0f
    private var minValue = 0f
    private var offsetValue = 0f
    val currentRange: FloatArray
        get() = (maxValue - minValue).let { range ->
            when (mSeekBarMode) {
                2 -> floatArrayOf(
                    -offsetValue + minValue + range * (leftSV?.currPercent ?: 0f),
                    -offsetValue + minValue + range * (rightSV?.currPercent ?: 0f)
                )
                else -> floatArrayOf(
                    -offsetValue + minValue + range * (leftSV?.currPercent ?: 0f),
                    -offsetValue + minValue + range * 1.0f
                )
            }
        }
    private val mCursorPaint = Paint().apply {
        style = Paint.Style.FILL
        color = colorLineEdge
        textSize = mTextSize.toFloat()
    }
    private val mMainPaint = Paint().apply {
        style = Paint.Style.FILL
        color = colorLineEdge
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mTextArray?.let {
            mPartLength = lineWidth / (it.size - 1)
            for ((index, value) in it.withIndex()) {
                val text2Draw = value.toString()
                when (mCellMode) {
                    1 -> {
                        mCursorPaint.color = colorLineEdge
                        lineLeft + index * mPartLength - mCursorPaint.measureText(text2Draw) / 2
                    }
                    else -> {
                        val num = text2Draw.toFloat()
                        val result = currentRange
                        mCursorPaint.color = when {
                            compareFloat(num, result[0]) != -1 &&
                                    compareFloat(num, result[1]) != 1 && mSeekBarMode == 2 ->
                                ContextCompat.getColor(context, R.color.green_500)
                            else -> colorLineEdge
                        }
                        (lineLeft + lineWidth * (num - min) / (max - min)
                                - mCursorPaint.measureText(text2Draw) / 2)//按实际比例显示
                    }
                }.run {
                    canvas.drawText(
                        text2Draw, this, lineTop - textPadding.toFloat(), mCursorPaint
                    )
                }
            }
        }//绘制刻度，根据当前位置是否在刻度范围内设置不同颜色显示
        mMainPaint.color = colorLineEdge//绘制进度条
        canvas.drawRoundRect(line, lineCorners.toFloat(), lineCorners.toFloat(), mMainPaint)
        mMainPaint.color = colorLineSelected
        leftSV?.run {
            when (mSeekBarMode) {
                2 -> canvas.drawRect(
                    left + widthSize / 2 + lineWidth * currPercent, lineTop.toFloat(),
                    rightSV?.run { left + widthSize / 2 + lineWidth * currPercent } ?: 0f,
                    lineBottom.toFloat(), mMainPaint
                )
                else -> canvas.drawRect(
                    left + widthSize / 2f, lineTop.toFloat(),
                    left + widthSize / 2 + lineWidth * currPercent,
                    lineBottom.toFloat(), mMainPaint
                )
            }
            draw(canvas)
            if (mSeekBarMode == 2) draw(canvas)
        }
    }

    private fun compareFloat(a: Float, b: Float): Int {
        val ta = (a * 1000).roundToLong()
        val tb = (b * 1000).roundToLong()
        return if (ta > tb) 1 else if (ta < tb) -1 else 0
    }

    private var isEnable = true
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        isEnable = enabled
    }

    private var currTouch: SeekView? = null

    interface OnRangeChangedListener {
        fun onRangeChanged(
            view: top.autoget.autosee.Seek?, min: Float, max: Float, isFromUser: Boolean
        )
    }

    var onRangeChangedListener: OnRangeChangedListener? = null
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when {
            isEnable -> {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        var touchResult = false
                        when {
                            rightSV != null && (rightSV?.currPercent ?: 0f) >= 1 &&
                                    (leftSV?.collide(event) ?: false) -> {
                                currTouch = leftSV
                                touchResult = true
                            }
                            rightSV != null && (rightSV?.collide(event) ?: false) -> {
                                currTouch = rightSV
                                touchResult = true
                            }
                            leftSV?.collide(event) ?: false -> {
                                currTouch = leftSV
                                touchResult = true
                            }
                        }
                        parent?.requestDisallowInterceptTouchEvent(true)
                        return touchResult
                    }
                    MotionEvent.ACTION_MOVE -> {
                        var percent: Float
                        val x = event.x
                        currTouch?.apply {
                            material = when {
                                material >= 1 -> 1f
                                else -> material + 0.1f
                            }
                        }
                        when {
                            currTouch === leftSV -> {
                                when {
                                    cellsCount > 1 -> {
                                        percent = when {
                                            x < lineLeft -> 0f
                                            else -> (x - lineLeft) * 1f / lineWidth
                                        }
                                        var touchLeftCellsValue =
                                            (percent / cellsPercent).roundToInt()
                                        percent = touchLeftCellsValue * cellsPercent
                                        val currRightCellsValue: Int = when (mSeekBarMode) {
                                            2 -> (rightSV!!.currPercent / cellsPercent).roundToInt()
                                            else -> (1f / cellsPercent).roundToInt()
                                        }
                                        while (touchLeftCellsValue > currRightCellsValue - reserveCount) {
                                            touchLeftCellsValue--
                                            if (touchLeftCellsValue >= 0)
                                                percent = touchLeftCellsValue * cellsPercent
                                        }
                                    }
                                    else -> {
                                        percent = when {
                                            x < lineLeft -> 0f
                                            else -> (x - lineLeft) * 1f / lineWidth
                                        }
                                        when (mSeekBarMode) {
                                            2 -> {
                                                if (percent > (rightSV?.currPercent
                                                        ?: 0f) - reservePercent
                                                ) percent = (rightSV?.currPercent
                                                    ?: 0f) - reservePercent
                                            }
                                            else -> {
                                                if (percent > 1.0f - reservePercent)
                                                    percent = 1.0f - reservePercent
                                            }
                                        }
                                    }
                                }
                                leftSV?.slide(percent)
                                leftSV?.isShowingHint = true
                                parent?.requestDisallowInterceptTouchEvent(true)
                            }
                            currTouch === rightSV -> {
                                when {
                                    cellsCount > 1 -> {
                                        percent = when {
                                            x > lineRight -> 1f
                                            else -> (x - lineLeft) * 1f / lineWidth
                                        }
                                        var touchRightCellsValue =
                                            (percent / cellsPercent).roundToLong()
                                        percent = touchRightCellsValue * cellsPercent
                                        val currLeftCellsValue = ((leftSV?.currPercent
                                            ?: 0f) / cellsPercent).roundToLong()
                                        while (touchRightCellsValue < currLeftCellsValue + reserveCount) {
                                            touchRightCellsValue++
                                            if (touchRightCellsValue <= maxValue - minValue)
                                                percent = touchRightCellsValue * cellsPercent
                                        }
                                    }
                                    else -> {
                                        percent = when {
                                            x > lineRight -> 1f
                                            else -> (x - lineLeft) * 1f / lineWidth
                                        }
                                        if (percent < (leftSV?.currPercent
                                                ?: 0f) + reservePercent
                                        ) percent = (leftSV?.currPercent ?: 0f) + reservePercent
                                    }
                                }
                                rightSV?.slide(percent)
                                rightSV?.isShowingHint = true
                            }
                        }
                        onRangeChangedListener?.let {
                            val result = currentRange
                            it.onRangeChanged(this, result[0], result[1], true)
                        }
                        invalidate()
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        if (isHintHolder) {
                            if (mSeekBarMode == 2) rightSV?.isShowingHint = false
                            leftSV?.isShowingHint = false
                        }
                        onRangeChangedListener?.let {
                            val result = currentRange
                            it.onRangeChanged(this, result[0], result[1], false)
                        }
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isHintHolder) {
                            if (mSeekBarMode == 2) rightSV?.isShowingHint = false
                            leftSV?.isShowingHint = false
                        }
                        currTouch?.materialRestore
                        onRangeChangedListener?.let {
                            val result = currentRange
                            it.onRangeChanged(this, result[0], result[1], false)
                        }
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                return super.onTouchEvent(event)
            }
            else -> return true
        }
    }

    public override fun onSaveInstanceState(): Parcelable? =
        SavedState(super.onSaveInstanceState()).apply {
            mMinValue = minValue - offsetValue
            mMaxValue = maxValue - offsetValue
            mReserveValue = reserveValue
            mCellsCount = cellsCount
            mCurrSelectedMin = currentRange[0]
            mCurrSelectedMax = currentRange[1]
        }

    public override fun onRestoreInstanceState(state: Parcelable) = (state as SavedState).run {
        super.onRestoreInstanceState(superState)
        setRules(mMinValue, mMaxValue, mReserveValue, mCellsCount)
        setValue(mCurrSelectedMin, mCurrSelectedMax)
    }

    private inner class SavedState : BaseSavedState {
        var mMinValue = 0f
        var mMaxValue = 0f
        var mReserveValue = 0f
        var mCellsCount = 0
        var mCurrSelectedMin = 0f
        var mCurrSelectedMax = 0f

        constructor(superState: Parcelable?) : super(superState)
        constructor(parcel: Parcel) : super(parcel) {
            mMinValue = parcel.readFloat()
            mMaxValue = parcel.readFloat()
            mReserveValue = parcel.readFloat()
            mCellsCount = parcel.readInt()
            mCurrSelectedMin = parcel.readFloat()
            mCurrSelectedMax = parcel.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(mMinValue)
            out.writeFloat(mMaxValue)
            out.writeFloat(mReserveValue)
            out.writeInt(mCellsCount)
            out.writeFloat(mCurrSelectedMin)
            out.writeFloat(mCurrSelectedMax)
        }
    }

    fun setValue(value: Float) = setValue(value, max)
    fun setValue(min: Float, max: Float) {
        val mMin: Float = min + offsetValue
        require(mMin >= minValue) { "setValue() min < (preset min - offsetValue) . #min:$mMin #preset min:$minValue #offsetValue:$offsetValue" }
        val mMax: Float = max + offsetValue
        require(mMax <= maxValue) { "setValue() max > (preset max - offsetValue) . #max:$mMax #preset max:$maxValue #offsetValue:$offsetValue" }
        when {
            reserveCount > 1 -> {
                require((mMin - minValue) % reserveCount == 0f) { "setValue() (min - preset min) % reserveCount != 0 . #min:$mMin #preset min:$minValue#reserveCount:$reserveCount#reserve:$reserveValue" }
                require((mMax - minValue) % reserveCount == 0f) { "setValue() (max - preset min) % reserveCount != 0 . #max:$mMax #preset min:$minValue#reserveCount:$reserveCount#reserve:$reserveValue" }
                leftSV?.currPercent = (mMin - minValue) / reserveCount * cellsPercent
                if (mSeekBarMode == 2)
                    rightSV?.currPercent = (mMax - minValue) / reserveCount * cellsPercent
            }
            else -> {
                leftSV?.currPercent = (mMin - minValue) / (maxValue - minValue)
                if (mSeekBarMode == 2)
                    rightSV?.currPercent = (mMax - minValue) / (maxValue - minValue)
            }
        }
        onRangeChangedListener?.let {
            when (mSeekBarMode) {
                2 -> it.onRangeChanged(
                    this, (leftSV?.currPercent ?: 0f), (rightSV?.currPercent ?: 0f), false
                )
                else -> it.onRangeChanged(
                    this, (leftSV?.currPercent ?: 0f), (leftSV?.currPercent ?: 0f), false
                )
            }
        }
        invalidate()
    }

    companion object {
        private const val DEFAULT_RADIUS = 0.5f
    }

    private var mCursorTextHeight =
        mCursorPaint.fontMetrics.run { (ceil(descent - ascent.toDouble()) + 2).toInt() }

    private inner class SeekView(position: Int) {
        private var heightSize = 0
        var widthSize = 0
        private var left = 0
        private var right = 0
        private var top = 0
        private var bottom = 0
        private var lineWidth = 0
        private var bmp: Bitmap? = null
        private var defaultPaint: Paint? = null
        private var shadowGradient: RadialGradient? = null
        fun onSizeChanged(
            x: Int, y: Int, hSize: Int, parentLineWidth: Int,
            cellsMode: Boolean, bmpResId: Int, context: Context
        ) {
            heightSize = hSize
            widthSize = heightSize
            left = x - widthSize / 2
            right = x + widthSize / 2
            top = y - heightSize / 2
            bottom = y + heightSize / 2
            lineWidth = if (cellsMode) parentLineWidth else parentLineWidth
            when {
                bmpResId > 0 -> BitmapFactory.decodeResource(context.resources, bmpResId)
                    ?.let { original ->
                        val matrix = Matrix()
                        val scaleHeight = mThumbSize * 1f / original.height
                        matrix.postScale(scaleHeight, scaleHeight)
                        if (!isInEditMode) bmp = Bitmap.createBitmap(
                            original, 0, 0, original.width, original.height, matrix, true
                        )
                    }
                else -> {
                    defaultPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    shadowGradient = RadialGradient(
                        widthSize / 2f, heightSize / 2f, widthSize * DEFAULT_RADIUS * 0.95f,
                        Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP
                    )
                }
            }
        }//计算每个按钮位置和尺寸

        var currPercent = 0f
        fun slide(percent: Float) {
            currPercent = when {
                percent < 0 -> 0f
                percent > 1 -> 1f
                else -> percent
            }
        }

        fun collide(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            val offset = (lineWidth * currPercent).toInt()
            return x > left + offset && x < right + offset && y > top && y < bottom
        }//拖动检测

        var isShowingHint = false
        private val isLeft: Boolean = position < 0
        private var anim: ValueAnimator? = null
        private var mHintText2Draw: String? = null
        fun setProgressHint(hint: String?) {
            mHintText2Draw = hint
        }

        private var isPrimary = true
        fun draw(canvas: Canvas) {
            canvas.save()
            canvas.translate(lineWidth * currPercent, 0f)
            var text2Draw = ""
            val result = currentRange
            var hintH = 0
            var hintW = 0
            when {
                mHideProgressHint -> isShowingHint = false
                else -> {
                    when {
                        isLeft -> {
                            text2Draw = mHintText2Draw ?: result[0].toInt().toString()
                            isPrimary = compareFloat(result[0], min) == 0
                        }
                        else -> {
                            text2Draw = mHintText2Draw ?: result[1].toInt().toString()
                            isPrimary = compareFloat(result[1], max) == 0
                        }
                    }
                    hintH = mHintBGHeight.toInt()
                    hintW = when (mHintBGWith) {
                        0f -> mCursorPaint.measureText(text2Draw) + defaultPaddingLeftAndRight
                        else -> mHintBGWith
                    }.toInt()
                    if (hintW < 1.5f * hintH) hintW = (1.5f * hintH).toInt()
                }
            }
            bmp?.let {
                canvas.drawBitmap(it, left.toFloat(), lineTop - it.height / 2f, null)
                if (isShowingHint) {
                    val rect = Rect()
                    rect.left = left - (hintW / 2 - it.width / 2)
                    rect.top = bottom - hintH - it.height
                    rect.right = rect.left + hintW
                    rect.bottom = rect.top + hintH
                    ImageKit.drawNinePatch(mProgressHintBG, canvas, rect)
                    mCursorPaint.color = Color.WHITE
                    val x = left + it.width / 2 - mCursorPaint.measureText(text2Draw) / 2
                    val y = bottom - hintH - it.height + hintH / 2f
                    canvas.drawText(text2Draw, x, y, mCursorPaint)
                }
            } ?: run {
                canvas.translate(left.toFloat(), 0f)
                if (isShowingHint) {
                    val rect = Rect()
                    rect.left = widthSize / 2 - hintW / 2
                    rect.top = defaultPaddingTop
                    rect.right = rect.left + hintW
                    rect.bottom = rect.top + hintH
                    ImageKit.drawNinePatch(mProgressHintBG, canvas, rect)
                    mCursorPaint.color = Color.WHITE
                    val x = widthSize / 2 - mCursorPaint.measureText(text2Draw) / 2
                    val y = hintH / 3f + defaultPaddingTop + mCursorTextHeight / 2
                    canvas.drawText(text2Draw, x, y, mCursorPaint)
                }//这里和背景形状有关，暂时根据本图形状比例计算
                drawDefault(canvas)
            }
            canvas.restore()
        }//绘制按钮和提示背景和文字

        var material = 0f
        val materialRestore = {
            anim?.cancel()
            anim = ValueAnimator.ofFloat(material, 0f)
            anim?.apply {
                addUpdateListener {
                    material = it.animatedValue as Float
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        material = 0f
                        invalidate()
                    }
                })
            }?.start()
        }
        private val typeEvaluator = TypeEvaluator<Int> { fraction, startValue, endValue ->
            val alpha = (Color.alpha(startValue) + fraction * (Color.alpha(endValue) -
                    Color.alpha(startValue))).toInt()
            val red = (Color.red(startValue) + fraction * (Color.red(endValue) -
                    Color.red(startValue))).toInt()
            val green = (Color.green(startValue) + fraction * (Color.green(endValue) -
                    Color.green(startValue))).toInt()
            val blue = (Color.blue(startValue) + fraction * (Color.blue(endValue) -
                    Color.blue(startValue))).toInt()
            Color.argb(alpha, red, green, blue)
        }

        private fun drawDefault(canvas: Canvas) {
            canvas.save()
            val radius = widthSize * DEFAULT_RADIUS
            canvas.translate(0f, radius * 0.25f)
            val centerX = widthSize / 2
            val centerY = lineBottom - mSeekBarHeight / 2
            canvas.scale(
                1 + 0.1f * material, 1 + 0.1f * material, centerX.toFloat(), centerY.toFloat()
            )
            if (defaultPaint == null) defaultPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            defaultPaint?.apply {
                style = Paint.Style.FILL
                shader = shadowGradient
            }?.let {
                canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius, it)
            }//draw shadow
            defaultPaint?.shader = null
            canvas.restore()
            defaultPaint?.apply {
                style = Paint.Style.FILL
                color = when {
                    isPrimary -> when (colorPrimary) {
                        0 -> typeEvaluator.evaluate(material, -0x1, -0x181819)
                        else -> colorPrimary
                    }
                    else -> when (colorSecondary) {
                        0 -> typeEvaluator.evaluate(material, -0x1, -0x181819)
                        else -> colorSecondary
                    }
                }
            }?.let {
                canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius, it)
            }//draw body
            defaultPaint?.apply {
                style = Paint.Style.STROKE
                color = -0x282829
            }?.let {
                canvas.drawCircle(centerX.toFloat(), centerY.toFloat(), radius, it)
            }//draw border
        }//如果没有图片资源，则绘制默认按钮
    }
}