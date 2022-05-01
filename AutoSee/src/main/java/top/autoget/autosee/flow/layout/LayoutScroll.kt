package top.autoget.autosee.flow.layout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.Scroller
import top.autoget.autokit.ScreenKit.statusBarHeight
import top.autoget.autokit.ScreenKit.toolbarHeight
import kotlin.math.abs

open class LayoutScroll//流式布局：滚动用来移动
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LayoutFlow(context, attrs, defStyleAttr) {
    protected var mRightBound = 0
    protected var mBottomRound = 0
    private val mScreenWidth: Int = resources.displayMetrics.widthPixels
    private val mScreenHeight: Int = resources.displayMetrics.heightPixels
    var isCanMove = false
    var mWidth = 0
    protected var mHeight = 0
    open var isLabelAutoScroll: Boolean = true
    open var isTabAutoScroll: Boolean = true
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (childCount > 0) {
            val child = getChildAt(childCount - 1)
            mRightBound = child.right + paddingRight
            mBottomRound = child.bottom + paddingBottom
        }
        when {
            isVerticalMove -> {
                when {
                    mViewHeight < mScreenHeight -> {
                        isCanMove = mBottomRound > mViewHeight
                        mHeight = mViewHeight
                    }
                    else -> {
                        isCanMove = mBottomRound > mScreenHeight
                        mHeight = mScreenHeight
                        mHeight = mHeight - toolbarHeight - statusBarHeight
                    }
                }
                isCanMove = isLabelAutoScroll && isTabAutoScroll
            }
            else -> {
                if (!isVertical) {
                    when (visualCount) {
                        -1 -> when {
                            mViewWidth < mScreenWidth -> {
                                isCanMove = mRightBound > mViewWidth
                                mWidth = mViewWidth
                            }
                            else -> {
                                isCanMove = mRightBound > mScreenWidth
                                mWidth = mScreenWidth
                            }
                        }
                        else -> {
                            isCanMove = childCount > visualCount
                            mWidth = mViewWidth
                        }
                    }
                    isCanMove = isTabAutoScroll
                }
            }
        }
    }

    private var mLastPos = 0f
    private var mMovePos = 0f
    private val mScroller: Scroller = Scroller(context)
    private var mVelocityTracker: VelocityTracker? = null
    private val mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (isCanMove) {
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    mLastPos = if (isVertical) ev.y else ev.x
                    mMovePos = if (isVertical) ev.y else ev.x
                    mScroller.run { if (!isFinished) abortAnimation() }
                    mVelocityTracker?.run {
                        recycle()
                        mVelocityTracker = null
                    }
                    mVelocityTracker = VelocityTracker.obtain()
                    mVelocityTracker?.addMovement(ev)
                    parent?.requestDisallowInterceptTouchEvent(true)//能滚动则屏蔽父控件触摸事件
                }
                MotionEvent.ACTION_MOVE -> {
                    val offset: Float = if (isVerticalMove) ev.y - mLastPos else ev.x - mLastPos
                    if (abs(offset) >= mTouchSlop) {
                        parent?.requestDisallowInterceptTouchEvent(true)
                        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain()
                        mVelocityTracker?.addMovement(ev)
                        return true//父控件接管触摸事件
                    }
                    mLastPos = if (isVerticalMove) ev.y else ev.x
                }
            }
            return super.onInterceptTouchEvent(ev)
        }
        return super.onInterceptTouchEvent(ev)
    }

    private var isMove = false
    private val mMaximumVelocity: Int = ViewConfiguration.get(context).scaledMaximumFlingVelocity
    private val mMinimumVelocity: Int = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private var mCurScrollPos = 0
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain()
        mVelocityTracker?.addMovement(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                val offset: Int =
                    if (isVerticalMove) (mMovePos - event.y).toInt() else (mMovePos - event.x).toInt()
                if (abs(offset) > mTouchSlop) parent?.requestDisallowInterceptTouchEvent(true)
                val scrollPos: Int = if (isVerticalMove) scrollY else scrollX
                when {
                    scrollPos + offset > 0 -> {
                        when {
                            isVerticalMove -> when {
                                scrollPos + offset < mBottomRound - mHeight -> scrollBy(0, offset)
                                else -> return true.apply { scrollTo(0, mBottomRound - mHeight) }
                            }
                            else -> when {
                                scrollPos + offset < mRightBound - mWidth -> scrollBy(offset, 0)
                                else -> return true.apply { scrollTo(mRightBound - mWidth, 0) }
                            }
                        }
                        isMove = true
                        mMovePos = if (isVerticalMove) event.y else event.x
                    }
                    else -> return true.apply { scrollTo(0, 0) }//判断边界
                }
            }//scroller向右为负向左为正
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mVelocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val velocityPos: Int = when {
                    isVerticalMove -> mVelocityTracker?.yVelocity?.toInt() ?: 0
                    else -> mVelocityTracker?.xVelocity?.toInt() ?: 0
                }
                if (abs(velocityPos) >= mMinimumVelocity) {
                    when {
                        isVerticalMove -> {
                            mCurScrollPos = scrollY
                            mScroller.fling(0, mCurScrollPos, 0, velocityPos, 0, 0, 0, height)
                        }
                        else -> {
                            mCurScrollPos = scrollX
                            mScroller.fling(mCurScrollPos, 0, velocityPos, 0, 0, width, 0, 0)
                        }
                    }
                    mVelocityTracker?.run {
                        clear()
                        recycle()
                        mVelocityTracker = null
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mScroller.computeScrollOffset()) {
            var offset: Int = when {
                isVerticalMove -> mCurScrollPos - mScroller.currY
                else -> mCurScrollPos - mScroller.currX
            }
            when {
                isVerticalMove -> {
                    if (scrollY + offset >= mBottomRound - mHeight) offset =
                        mBottomRound - mHeight - scrollY
                    if (scrollY + offset <= 0) offset = -scrollY
                    scrollBy(0, offset)
                }
                else -> {
                    if (scrollX + offset >= mRightBound - mWidth) offset =
                        mRightBound - mWidth - scrollX
                    if (scrollX + offset <= 0) offset = -scrollX
                    scrollBy(offset, 0)
                }
            }
            postInvalidate()
        }//修正超出边界
    }
}