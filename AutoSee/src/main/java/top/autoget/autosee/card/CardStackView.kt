package top.autoget.autosee.card

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.TypedArray
import android.database.Observable
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import androidx.annotation.RequiresApi
import top.autoget.autokit.DensityKit.dip2px
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.error
import top.autoget.autosee.R
import kotlin.math.abs
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CardStackView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes), DelegateScroll, LoggerKit {
    init {
        init(context, attrs, defStyleAttr, defStyleRes)
    }

    var overlapGaps: Int = dip2px(20f)
    var overlapGapsCollapse: Int = dip2px(20f)
    var duration: Int = 400
        get() = adapterAnimator?.let { field } ?: 0
    var numBottomShow: Int = 3
    private var overScroller: OverScroller? = null
    private var touchSlop: Int = 0
    private var velocityMin: Int = 0
    private var velocityMax: Int = 0
    private val initScroller = {
        isFocusable = true
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        overScroller = OverScroller(context)
        ViewConfiguration.get(context).run {
            touchSlop = scaledTouchSlop
            velocityMin = scaledMinimumFlingVelocity
            velocityMax = scaledMaximumFlingVelocity
        }
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray: TypedArray = context
            .obtainStyledAttributes(attrs, R.styleable.CardStackView, defStyleAttr, defStyleRes)
        try {
            typedArray.run {
                overlapGaps = getDimensionPixelSize(
                    R.styleable.CardStackView_stackOverlapGaps, dip2px(20f)
                )
                overlapGapsCollapse = getDimensionPixelSize(
                    R.styleable.CardStackView_stackOverlapGapsCollapse, dip2px(20f)
                )
                duration = getInt(R.styleable.CardStackView_stackDuration, 400)
                numBottomShow = getInt(R.styleable.CardStackView_stackNumBottomShow, 3)
                setAnimationType(
                    getInt(R.styleable.CardStackView_stackAnimationType, UP_DOWN_STACK)
                )
            }
        } finally {
            typedArray.recycle()
        }
        initScroller
    }

    interface ItemExpendListener {
        fun onItemExpend(expend: Boolean)
    }

    var itemExpendListener: ItemExpendListener? = null
    var selectPosition: Int = DEFAULT_SELECT_POSITION
        set(selectPosition) {
            field = selectPosition
            itemExpendListener?.onItemExpend(field != DEFAULT_SELECT_POSITION)
        }
    val isExpending: Boolean
        get() = selectPosition != DEFAULT_SELECT_POSITION
    val clearSelectPosition: Boolean
        get() = updateSelectPosition(selectPosition)

    fun updateSelectPosition(selectPosition: Int) =
        post { doCardClickAnimation(viewHolders[selectPosition], selectPosition) }

    var showHeight: Int = 0
        private set
    private val checkContentHeightByParent =
        (parent as View).run { showHeight = measuredHeight - paddingTop - paddingBottom }

    private fun doCardClickAnimation(viewHolder: ViewHolder, position: Int) {
        checkContentHeightByParent
        adapterAnimator?.itemClick(viewHolder, position)
    }

    val clearScrollYAndTranslation = {
        if (selectPosition != DEFAULT_SELECT_POSITION) clearSelectPosition
        delegateScroll?.viewScrollY = 0
        requestLayout()
    }
    var delegateScroll: DelegateScroll? = null
        private set
    var adapterAnimator: AdapterAnimator? = null
        set(adapterAnimator) {
            clearScrollYAndTranslation
            field = adapterAnimator
            delegateScroll = when (field) {
                is AdapterUpDownStackAnimator -> DelegateScrollStack(this)
                else -> this
            }
        }

    fun setAnimationType(type: Int = UP_DOWN_STACK) = when (type) {
        ALL_DOWN -> AdapterAllMoveDownAnimator(this)
        UP_DOWN -> AdapterUpDownAnimator(this)
        else -> AdapterUpDownStackAnimator(this)
    }.run { adapterAnimator = this }

    companion object {
        const val ALL_DOWN = 0
        const val UP_DOWN = 1
        const val UP_DOWN_STACK = 2
        const val ANIMATION_STATE_START = 0
        const val ANIMATION_STATE_END = 1
        const val ANIMATION_STATE_CANCEL = 2
        const val DEFAULT_SELECT_POSITION = -1
        const val INVALID_TYPE = -1
        private const val INVALID_POINTER = -1
        private fun clamp(n: Int, my: Int, child: Int): Int =
            if (my >= child || n < 0) 0 else if (my + n > child) child - my else n
    }

    var scrollEnable = true

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    abstract class AdapterAnimator(protected var cardStackView: CardStackView) {
        private val mDuration: Int = cardStackView.duration
        protected var animatorSet: AnimatorSet? = null
        protected val initAnimatorSet = {
            animatorSet = AnimatorSet().apply {
                duration = mDuration.toLong()
                interpolator = AccelerateDecelerateInterpolator()
            }
        }

        fun itemClick(viewHolder: ViewHolder, position: Int) {
            if (animatorSet?.isRunning != true) {
                initAnimatorSet
                when (position) {
                    cardStackView.selectPosition -> onItemCollapse(viewHolder)
                    else -> onItemExpand(viewHolder, position)
                }
                if (cardStackView.childCount == 1) animatorSet?.end()
            }
        }

        private fun onItemCollapse(viewHolder: ViewHolder) {
            itemCollapseAnimatorSet(viewHolder)
            animatorSet?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    super.onAnimationStart(animation)
                    cardStackView.scrollEnable = true
                    viewHolder.run {
                        onItemExpand(false)
                        onAnimationStateChange(ANIMATION_STATE_START, false)
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    cardStackView.selectPosition = DEFAULT_SELECT_POSITION
                    viewHolder.onAnimationStateChange(ANIMATION_STATE_END, false)
                }

                override fun onAnimationCancel(animation: Animator) {
                    super.onAnimationCancel(animation)
                    viewHolder.onAnimationStateChange(ANIMATION_STATE_CANCEL, false)
                }
            })
            animatorSet?.start()
        }

        protected abstract fun itemCollapseAnimatorSet(viewHolder: ViewHolder)
        private fun onItemExpand(viewHolder: ViewHolder, position: Int) =
            cardStackView.getViewHolder(cardStackView.selectPosition).let { preSelectViewHolder ->
                preSelectViewHolder?.onItemExpand(false)
                cardStackView.selectPosition = position
                itemExpandAnimatorSet(viewHolder, position)
                animatorSet?.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        super.onAnimationStart(animation)
                        cardStackView.scrollEnable = false
                        preSelectViewHolder?.onAnimationStateChange(ANIMATION_STATE_START, false)
                        viewHolder.onAnimationStateChange(ANIMATION_STATE_START, true)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        preSelectViewHolder?.onAnimationStateChange(ANIMATION_STATE_END, false)
                        viewHolder.run {
                            onItemExpand(true)
                            onAnimationStateChange(ANIMATION_STATE_END, true)
                        }
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        super.onAnimationCancel(animation)
                        preSelectViewHolder?.onAnimationStateChange(ANIMATION_STATE_CANCEL, false)
                        viewHolder.onAnimationStateChange(ANIMATION_STATE_CANCEL, true)
                    }
                })
                animatorSet?.start()
            }

        protected abstract fun itemExpandAnimatorSet(viewHolder: ViewHolder, position: Int)
        protected fun getCollapseStartTop(collapseShowItemCount: Int): Int = cardStackView.run {
            overlapGapsCollapse * ((childCount - selectPosition)
                .let { if (it > numBottomShow) numBottomShow else it - 1 } - collapseShowItemCount)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    class AdapterAllMoveDownAnimator(cardStackView: CardStackView) :
        AdapterAnimator(cardStackView) {
        override fun itemCollapseAnimatorSet(viewHolder: ViewHolder) = cardStackView.run {
            var childTop = paddingTop
            for (i in 0 until childCount) {
                getChildAt(i).let { child ->
                    child.clearAnimation()
                    (child.layoutParams as LayoutParams).let { layoutParams ->
                        childTop += layoutParams.topMargin
                        when (i) {
                            0 -> animatorSet?.play(
                                ObjectAnimator
                                    .ofFloat<View>(child, View.Y, child.y, childTop.toFloat())
                            )
                            else -> {
                                childTop -= overlapGaps * 2
                                animatorSet?.play(
                                    ObjectAnimator
                                        .ofFloat<View>(child, View.Y, child.y, childTop.toFloat())
                                )
                            }
                        }
                        childTop += layoutParams.headerHeight
                    }
                }
            }
        }

        override fun itemExpandAnimatorSet(viewHolder: ViewHolder, position: Int) =
            cardStackView.run {
                viewHolder.itemView.let { itemView ->
                    itemView.clearAnimation()
                    animatorSet?.play(
                        ObjectAnimator.ofFloat<View>(
                            itemView, View.Y, itemView.y, (scrollY + paddingTop).toFloat()
                        )
                    )
                }
                var collapseShowItemCount = 0
                for (i in 0 until childCount) {
                    if (i != selectPosition) {
                        val child: View = getChildAt(i)
                        child.clearAnimation()
                        when {
                            i > selectPosition && collapseShowItemCount < numBottomShow -> {
                                animatorSet?.play(
                                    ObjectAnimator.ofFloat<View>(
                                        child, View.Y, child.y,
                                        (showHeight - getCollapseStartTop(collapseShowItemCount) + scrollY).toFloat()
                                    )
                                )
                                collapseShowItemCount++
                            }
                            else -> animatorSet?.play(
                                ObjectAnimator.ofFloat<View>(
                                    child, View.Y, child.y, (showHeight + scrollY).toFloat()
                                )
                            )
                        }
                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    class AdapterUpDownAnimator(cardStackView: CardStackView) : AdapterAnimator(cardStackView) {
        override fun itemCollapseAnimatorSet(viewHolder: ViewHolder) = cardStackView.run {
            var childTop = paddingTop
            for (i in 0 until childCount) {
                getChildAt(i).let { child ->
                    child.clearAnimation()
                    (child.layoutParams as LayoutParams).let { layoutParams ->
                        childTop += layoutParams.topMargin
                        when (i) {
                            0 -> animatorSet?.play(
                                ObjectAnimator
                                    .ofFloat<View>(child, View.Y, child.y, childTop.toFloat())
                            )
                            else -> {
                                childTop -= overlapGaps * 2
                                animatorSet?.play(
                                    ObjectAnimator
                                        .ofFloat<View>(child, View.Y, child.y, childTop.toFloat())
                                )
                            }
                        }
                        childTop += layoutParams.headerHeight
                    }
                }
            }
        }

        override fun itemExpandAnimatorSet(viewHolder: ViewHolder, position: Int) =
            cardStackView.run {
                viewHolder.itemView.let { itemView ->
                    itemView.clearAnimation()
                    animatorSet?.play(
                        ObjectAnimator.ofFloat<View>(
                            itemView, View.Y, itemView.y, (scrollY + paddingTop).toFloat()
                        )
                    )
                }
                var collapseShowItemCount = 0
                for (i in 0 until childCount) {
                    if (i != selectPosition) {
                        val child: View = getChildAt(i)
                        child.clearAnimation()
                        when {
                            i > selectPosition && collapseShowItemCount < numBottomShow -> {
                                animatorSet?.play(
                                    ObjectAnimator.ofFloat<View>(
                                        child, View.Y, child.y,
                                        (showHeight - getCollapseStartTop(collapseShowItemCount) + scrollY).toFloat()
                                    )
                                )
                                collapseShowItemCount++
                            }
                            i < selectPosition -> animatorSet?.play(
                                ObjectAnimator.ofFloat<View>(
                                    child, View.Y, child.y, (scrollY - child.height).toFloat()
                                )
                            )
                            else -> animatorSet?.play(
                                ObjectAnimator.ofFloat<View>(
                                    child, View.Y, child.y, (showHeight + scrollY).toFloat()
                                )
                            )
                        }
                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    class AdapterUpDownStackAnimator(cardStackView: CardStackView) :
        AdapterAnimator(cardStackView) {
        override fun itemCollapseAnimatorSet(viewHolder: ViewHolder) = cardStackView.run {
            var childTop = paddingTop
            for (i in 0 until childCount) {
                getChildAt(i).let { child ->
                    child.clearAnimation()
                    (child.layoutParams as LayoutParams).let { layoutParams ->
                        childTop += layoutParams.topMargin
                        if (i != 0) childTop -= overlapGaps * 2
                        delegateScroll?.let {
                            when {
                                childTop - it.viewScrollY < getChildAt(0).y -> animatorSet?.play(
                                    ObjectAnimator
                                        .ofFloat<View>(child, View.Y, child.y, getChildAt(0).y)
                                )
                                else -> animatorSet?.play(
                                    ObjectAnimator.ofFloat<View>(
                                        child, View.Y, child.y,
                                        (childTop - it.viewScrollY).toFloat()
                                    )
                                )
                            }
                        }
                        childTop += layoutParams.headerHeight
                    }
                }
            }
        }

        override fun itemExpandAnimatorSet(viewHolder: ViewHolder, position: Int) =
            cardStackView.run {
                viewHolder.itemView.let { itemView ->
                    itemView.clearAnimation()
                    animatorSet?.play(
                        ObjectAnimator.ofFloat(itemView, View.Y, itemView.y, getChildAt(0).y)
                    )
                }
                var collapseShowItemCount = 0
                for (i in 0 until childCount) {
                    if (i != selectPosition) {
                        val child = getChildAt(i)
                        child.clearAnimation()
                        when {
                            i > selectPosition && collapseShowItemCount < numBottomShow -> {
                                animatorSet?.play(
                                    ObjectAnimator.ofFloat<View>(
                                        child, View.Y, child.y,
                                        (showHeight - getCollapseStartTop(collapseShowItemCount)).toFloat()
                                    )
                                )
                                collapseShowItemCount++
                            }
                            i < selectPosition -> animatorSet?.play(
                                ObjectAnimator.ofFloat(child, View.Y, child.y, getChildAt(0).y)
                            )
                            else -> animatorSet?.play(
                                ObjectAnimator
                                    .ofFloat<View>(child, View.Y, child.y, showHeight.toFloat())
                            )
                        }
                    }
                }
            }
    }

    var totalLength: Int = 0
        private set

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        checkContentHeightByParent
        totalLength = paddingTop + paddingBottom
        var maxWidth = 0
        for (i in 0 until childCount) {
            getChildAt(i).let { child ->
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                (child.layoutParams as LayoutParams)
                    .apply { if (headerHeight == -1) headerHeight = child.measuredHeight }.run {
                        totalLength = max(
                            totalLength, totalLength + headerHeight + topMargin + bottomMargin
                        ) - overlapGaps * 2
                        maxWidth = max(maxWidth, child.measuredWidth + leftMargin + rightMargin)
                    }
            }
        }
        setMeasuredDimension(
            View.resolveSizeAndState(maxWidth, widthMeasureSpec, 0), View.resolveSizeAndState(
                max(totalLength + overlapGaps * 2, showHeight), heightMeasureSpec, 0
            )
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val childLeft = paddingLeft
        var childTop = paddingTop
        for (i in 0 until childCount) {
            getChildAt(i).run {
                (layoutParams as LayoutParams).let { layoutParams ->
                    childTop += layoutParams.topMargin
                    when (i) {
                        0 -> layout(
                            childLeft, childTop,
                            childLeft + measuredWidth, childTop + measuredHeight
                        )
                        else -> {
                            childTop -= overlapGaps * 2
                            layout(
                                childLeft, childTop,
                                childLeft + measuredWidth, childTop + measuredHeight
                            )
                        }
                    }
                    childTop += layoutParams.headerHeight
                }
            }
        }
    }

    private var isBeingDragged = false
    private var activePointerId: Int = INVALID_POINTER
    private var lastMotionY: Int = 0
    private var nestedYOffset: Int = 0
    private var velocityTracker: VelocityTracker? = null
    private val initVelocityTrackerIfNotExists = {
        if (velocityTracker == null) velocityTracker = VelocityTracker.obtain()
    }
    private val initOrResetVelocityTracker =
        velocityTracker?.clear() ?: run { velocityTracker = VelocityTracker.obtain() }
    private val recycleVelocityTracker = velocityTracker?.run {
        recycle()
        velocityTracker = null
    }
    override var viewScrollX: Int
        get() = scrollX
        set(viewScrollX) {
            scrollX = viewScrollX
        }
    override var viewScrollY: Int
        get() = scrollY
        set(viewScrollY) {
            scrollY = viewScrollY
        }
    private val scrollRange: Int
        get() = if (childCount > 0) max(0, totalLength - showHeight) else 0

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when {
            ev.action == MotionEvent.ACTION_MOVE && isBeingDragged -> return true
            viewScrollY == 0 && !canScrollVertically(1) -> return false
            else -> {
                when (ev.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_MOVE -> when (activePointerId) {
                        INVALID_POINTER -> return false
                        else -> ev.findPointerIndex(activePointerId).let { pointerIndex ->
                            when (pointerIndex) {
                                -1 -> return false.apply { error("${loggerTag}->Invalid pointerId=$activePointerId in onInterceptTouchEvent") }
                                else -> ev.getY(pointerIndex).toInt().let {
                                    if (abs(it - lastMotionY) > touchSlop) {
                                        isBeingDragged = true
                                        lastMotionY = it
                                        nestedYOffset = 0
                                        initVelocityTrackerIfNotExists
                                        velocityTracker?.addMovement(ev)
                                        parent?.requestDisallowInterceptTouchEvent(true)
                                    }
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_DOWN -> {
                        isBeingDragged = !(overScroller?.isFinished ?: false)
                        activePointerId = ev.getPointerId(0)
                        lastMotionY = ev.y.toInt()
                        initOrResetVelocityTracker
                        velocityTracker?.addMovement(ev)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isBeingDragged = false
                        activePointerId = INVALID_POINTER
                        recycleVelocityTracker
                        if (overScroller
                                ?.springBack(viewScrollX, viewScrollY, 0, 0, 0, scrollRange) == true
                        ) postInvalidate()
                    }
                    MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
                }
                if (!scrollEnable) isBeingDragged = false
                return isBeingDragged
            }
        }
    }

    private fun onSecondaryPointerUp(motionEvent: MotionEvent) = motionEvent.run {
        (action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
            .let { pointerIndex ->
                if (getPointerId(pointerIndex) == activePointerId)
                    (if (pointerIndex == 0) 1 else 0).let { newPointerIndex ->
                        activePointerId = getPointerId(newPointerIndex)
                        lastMotionY = getY(newPointerIndex).toInt()
                        velocityTracker?.clear()
                    }
            }
    }

    val scrollOffset = IntArray(2)
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isBeingDragged) super.onTouchEvent(ev)
        when {
            scrollEnable -> ev.actionMasked.let { actionMasked ->
                initVelocityTrackerIfNotExists()
                val motionEvent = MotionEvent.obtain(ev)
                if (actionMasked == MotionEvent.ACTION_DOWN) nestedYOffset = 0
                motionEvent.offsetLocation(0f, nestedYOffset.toFloat())
                when (actionMasked) {
                    MotionEvent.ACTION_MOVE -> when (val activePointerIndex =
                        ev.findPointerIndex(activePointerId)) {
                        INVALID_POINTER -> return false.apply { error("${loggerTag}->Invalid pointerId=$activePointerId in onTouchEvent") }
                        else -> ev.getY(activePointerIndex).toInt().let { y ->
                            var deltaY = lastMotionY - y
                            if (!isBeingDragged && abs(deltaY) > touchSlop) {
                                parent?.requestDisallowInterceptTouchEvent(true)
                                isBeingDragged = true
                                deltaY = if (deltaY > 0) deltaY - touchSlop else deltaY + touchSlop
                            }
                            if (isBeingDragged) {
                                lastMotionY = y - scrollOffset[1]
                                when (delegateScroll) {
                                    is DelegateScrollStack -> delegateScroll
                                        ?.run { scrollViewTo(0, deltaY + viewScrollY) }
                                    else -> if (overScrollBy(
                                            0, deltaY, 0, viewScrollY, 0, scrollRange, 0, 0, true
                                        )
                                    ) velocityTracker?.clear()
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_DOWN -> when (childCount) {
                        0 -> return false
                        else -> {
                            overScroller?.run {
                                if (isBeingDragged == !isFinished)
                                    parent?.requestDisallowInterceptTouchEvent(true)
                                if (!isFinished) abortAnimation()
                            }
                            activePointerId = ev.getPointerId(0)
                            lastMotionY = ev.y.toInt()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isBeingDragged) velocityTracker?.run {
                            computeCurrentVelocity(1000, velocityMax.toFloat())
                            if (childCount > 0) {
                                getYVelocity(activePointerId).toInt().let { initialVelocity ->
                                    when {
                                        abs(initialVelocity) > velocityMin -> fling(-initialVelocity)
                                        else -> if (overScroller?.springBack(
                                                viewScrollX, delegateScroll?.viewScrollY ?: 0,
                                                0, 0, 0, scrollRange
                                            ) == true
                                        ) postInvalidate()
                                    }
                                }
                                activePointerId = INVALID_POINTER
                            }
                        }
                        endDrag()
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        if (isBeingDragged && childCount > 0) {
                            if (overScroller?.springBack(
                                    viewScrollX, delegateScroll?.viewScrollY ?: 0,
                                    0, 0, 0, scrollRange
                                ) == true
                            ) postInvalidate()
                            activePointerId = INVALID_POINTER
                        }
                        endDrag()
                    }
                    MotionEvent.ACTION_POINTER_DOWN -> ev.actionIndex.let { index ->
                        activePointerId = ev.getPointerId(index)
                        lastMotionY = ev.getY(index).toInt()
                    }
                    MotionEvent.ACTION_POINTER_UP -> {
                        onSecondaryPointerUp(ev)
                        lastMotionY = ev.getY(ev.findPointerIndex(activePointerId)).toInt()
                    }
                }
                velocityTracker?.addMovement(motionEvent)
                motionEvent.recycle()
                return true
            }
            else -> return true
        }
    }

    fun fling(velocityY: Int) {
        if (childCount > 0) {
            delegateScroll?.run {
                overScroller?.fling(
                    viewScrollX, viewScrollY, 0, velocityY,
                    0, 0, 0, max(0, totalLength - showHeight), 0, 0
                )
            }
            postInvalidate()
        }
    }

    private fun endDrag() {
        isBeingDragged = false
        recycleVelocityTracker
    }

    override fun computeVerticalScrollOffset(): Int = max(0, super.computeVerticalScrollOffset())
    override fun computeVerticalScrollRange(): Int = when (childCount) {
        0 -> showHeight
        else -> delegateScroll?.viewScrollY?.let { scrollY ->
            max(0, totalLength - showHeight).let { overScrollBottom ->
                when {
                    scrollY < 0 -> totalLength - scrollY
                    scrollY > overScrollBottom -> totalLength + scrollY - overScrollBottom
                    else -> totalLength
                }
            }
        } ?: totalLength
    }

    override fun computeScroll() {
        overScroller?.run {
            if (computeScrollOffset()) {
                delegateScroll?.scrollViewTo(0, currY)
                postInvalidate()
            }
        }
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        overScroller?.let {
            when {
                it.isFinished -> super.scrollTo(scrollX, scrollY)
                else -> delegateScroll?.run {
                    val oldX = viewScrollX
                    val oldY = viewScrollY
                    onScrollChanged(
                        scrollX.apply { viewScrollX = this },
                        scrollY.apply { viewScrollY = this }, oldX, oldY
                    )
                    if (clampedY) it.springBack(viewScrollX, viewScrollY, 0, 0, 0, scrollRange)
                }
            }
        }
    }

    override fun scrollViewTo(x: Int, y: Int) = scrollTo(x, y)
    override fun scrollTo(x: Int, y: Int) {
        if (childCount > 0) clamp(x, width - paddingRight - paddingLeft, width).let { x0 ->
            clamp(y, showHeight, totalLength).let { y0 ->
                if (x0 != delegateScroll?.viewScrollX || y0 != delegateScroll?.viewScrollY)
                    super.scrollTo(x0, y0)
            }
        }
    }

    class LayoutParams : MarginLayoutParams {
        var headerHeight: Int = -1

        constructor(source: ViewGroup.LayoutParams) : super(source)
        constructor(width: Int, height: Int) : super(width, height)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
            val typedArray: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.CardStackView)
            try {
                headerHeight = typedArray
                    .getDimensionPixelSize(R.styleable.CardStackView_stackHeaderHeight, -1)
            } finally {
                typedArray.recycle()
            }
        }
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams =
        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    override fun generateLayoutParams(layoutParams: ViewGroup.LayoutParams): ViewGroup.LayoutParams =
        LayoutParams(layoutParams)

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams =
        LayoutParams(context, attrs)

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean = p is LayoutParams
    inner class ViewDataObserver {
        val onChanged
            get() = refreshView
    }

    class AdapterDataObservable : Observable<ViewDataObserver>() {
        val notifyChanged = {
            for (i in mObservers.indices.reversed()) {
                mObservers[i].onChanged
            }
        }
        var hasObservers: Boolean = mObservers.isNotEmpty()
            private set
    }

    abstract class ViewHolder(var itemView: View) {
        val context: Context = itemView.context
        var itemViewType: Int = INVALID_TYPE
        var position: Int = 0
        open fun onAnimationStateChange(state: Int, willBeSelect: Boolean) {}
        abstract fun onItemExpand(boolean: Boolean)
    }

    abstract class Adapter<VH : ViewHolder> {
        abstract val itemCount: Int
        private val adapterDataObservable = AdapterDataObservable()
        val notifyDataSetChanged
            get() = adapterDataObservable.notifyChanged

        fun registerObserver(observer: ViewDataObserver) =
            adapterDataObservable.registerObserver(observer)

        fun createView(parent: ViewGroup, viewType: Int): VH =
            onCreateView(parent, viewType).apply { itemViewType = viewType }

        protected abstract fun onCreateView(parent: ViewGroup, viewType: Int): VH
        fun bindViewHolder(holder: VH, position: Int) = onBindViewHolder(holder, position)
        protected abstract fun onBindViewHolder(holder: VH, position: Int)
        open fun getItemViewType(position: Int): Int = 0
    }

    abstract class AdapterStack<T>(val context: Context) : Adapter<ViewHolder>() {
        val layoutInflater: LayoutInflater = LayoutInflater.from(context)
        var dataList: MutableList<T> = mutableListOf()
            set(dataList) {
                field.apply {
                    clear()
                    addAll(dataList)
                }
                notifyDataSetChanged
            }
        override val itemCount: Int
            get() = dataList.size

        public override fun onBindViewHolder(holder: ViewHolder, position: Int) =
            bindView(getItem(position), position, holder)

        fun getItem(position: Int): T = dataList[position]
        abstract fun bindView(data: T, position: Int, holder: ViewHolder)
    }

    private val viewDataObserver = ViewDataObserver()
    private val viewHolders: MutableList<ViewHolder> = mutableListOf()
    private val refreshView = {
        removeAllViews()
        viewHolders.clear()
        adapterStack?.let {
            for (i in 0 until it.itemCount) {
                getViewHolder(i)?.apply { position = i }?.run {
                    onItemExpand(selectPosition == i)
                    addView(itemView)
                    setClickAnimator(this, i)
                    it.bindViewHolder(this, i)
                }
            }
        }
        requestLayout()
    }

    fun getViewHolder(i: Int): ViewHolder? = adapterStack?.let {
        when {
            i == DEFAULT_SELECT_POSITION -> null
            viewHolders.size > i && viewHolders[i].itemViewType == it.getItemViewType(i) -> viewHolders[i]
            else -> it.createView(this, it.getItemViewType(i)).apply { viewHolders.add(this) }
        }
    }

    private fun setClickAnimator(holder: ViewHolder, position: Int = selectPosition) {
        setOnClickListener { if (position != DEFAULT_SELECT_POSITION) performItemClick(viewHolders[position]) }
        holder.itemView.setOnClickListener { performItemClick(holder) }
    }

    fun performItemClick(viewHolder: ViewHolder) =
        doCardClickAnimation(viewHolder, viewHolder.position)

    var adapterStack: AdapterStack<*>? = null
        set(adapterStack) {
            field = adapterStack?.apply { registerObserver(viewDataObserver) }
            refreshView
        }
    val cardNext = selectPosition.let {
        if (it != DEFAULT_SELECT_POSITION && it != viewHolders.size - 1)
            performItemClick(viewHolders[it + 1])
    }
    val cardPrevious = selectPosition.let {
        if (it != DEFAULT_SELECT_POSITION && it != 0) performItemClick(viewHolders[it - 1])
    }
}//import top.autoget.autokit.error不能少，默认不可使用