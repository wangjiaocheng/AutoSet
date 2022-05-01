package top.autoget.autosee.flow.action

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import top.autoget.autosee.flow.LayoutTab
import top.autoget.autosee.flow.bean.BeanTab
import top.autoget.autosee.flow.bean.TabTypeEvaluator
import top.autoget.autosee.flow.bean.TabValue
import top.autoget.autosee.flow.common.ConstantsFlow
import kotlin.math.abs

abstract class ActionBase : OnPageChangeListener {
    var mPaint: Paint = Paint().apply { isAntiAlias = true }
    private var mParentView: LayoutTab? = null
    var mBeanTab: BeanTab? = null
    open fun configAttrs(beanTab: BeanTab?) {
        mBeanTab = beanTab
    }//自定属性

    val isVertical: Boolean
        get() = mBeanTab?.tabOrientation == ConstantsFlow.VERTICAL
    val isLeftAction: Boolean
        get() = mBeanTab?.run { actionOrientation != -1 && actionOrientation == ConstantsFlow.LEFT }
            ?: false
    val isRightAction: Boolean
        get() = mBeanTab?.run { actionOrientation != -1 && actionOrientation == ConstantsFlow.RIGHT }
            ?: false
    var mContext: Context? = null
    private var mViewWidth = 0
    private var mRightBound = 0
    private var mOffset = 0f
    private var isColorText = false
    private var isTextView = false
    open fun config(parentView: LayoutTab) {
        mParentView = parentView
        if (parentView.childCount > 0) mBeanTab?.let {
            mContext = mParentView?.context
            mViewWidth = mParentView?.mWidth ?: 0
            val childCount = mParentView?.childCount ?: 0
            if (childCount > 0) mRightBound =
                mParentView?.run { getChildAt(childCount - 1).right + paddingRight }
                    ?: 0
            mParentView?.getChildAt(0)?.let { child ->
                mOffset = when {
                    isVertical -> (mBeanTab?.tabHeight ?: 0) * 1f / child.measuredHeight
                    else -> (mBeanTab?.tabWidth ?: 0) * 1f / child.measuredWidth
                }
                if (mTextViewId != -1) child.findViewById<View?>(mTextViewId).apply {
                    if (this is TextViewTabColor) {
                        isColorText = true
                        setTextColor(changeColor)
                    }
                    if (this is TextView) isTextView = true
                }
                if (mBeanTab?.autoScale == true && (mBeanTab?.scaleFactor ?: 1f) > 1) child.apply {
                    scaleX = mBeanTab?.scaleFactor ?: 1f
                    scaleY = mBeanTab?.scaleFactor ?: 1f
                }
                mParentView?.mAdapter?.onItemSelectState(child, true)
            }
        }
    }//配置数据

    private var mTextViewId = -1
    fun setTextId(textId: Int): ActionBase? = apply { mTextViewId = textId }//不设置颜色不起作用
    private var mSelectedColor = -1
    fun setSelectedColor(selectedColor: Int): ActionBase? =
        apply { mSelectedColor = selectedColor }//在TextViewTabColor不起作用

    private var mUnSelectedColor = -1
    fun setUnSelectedColor(unSelectedColor: Int): ActionBase? =
        apply { mUnSelectedColor = unSelectedColor }//在TextViewTabColor不起作用

    var mViewPager: ViewPager? = null
    fun setViewPager(viewPager: ViewPager?): ActionBase? = apply {
        mViewPager = viewPager.apply {
            viewPager?.removeOnPageChangeListener(this@ActionBase)
            viewPager?.addOnPageChangeListener(this@ActionBase)
        }
    }

    private var isTabClick = false
    var mCurrentIndex = 0
    var mLastIndex = 0
    val autoScaleView = {
        if (mBeanTab?.autoScale == true && (mBeanTab?.scaleFactor ?: 1f) > 1) mParentView?.run {
            getChildAt(mLastIndex)?.animate()?.scaleX(1f)?.scaleY(1f)
                ?.setDuration((mBeanTab?.tabClickAnimTime ?: 0).toLong())
                ?.setInterpolator(LinearInterpolator())?.start()
            getChildAt(mCurrentIndex)?.animate()?.scaleX(mBeanTab?.scaleFactor ?: 1f)
                ?.scaleY(mBeanTab?.scaleFactor ?: 1f)
                ?.setDuration((mBeanTab?.tabClickAnimTime ?: 0).toLong())
                ?.setInterpolator(LinearInterpolator())?.start()
        }
    }//放大缩小效果
    val clearColorText = {
        if (isColorText && abs(mCurrentIndex - mLastIndex) > 0) mParentView?.run {
            for (i in 0 until childCount) {
                getChildAt(i)?.findViewById<TextViewTabColor>(mTextViewId)
                    ?.apply { setTextColor(defaultColor) }
            }
            getChildAt(mCurrentIndex)?.findViewById<TextViewTabColor>(mTextViewId)
                ?.apply { setTextColor(changeColor) }
        }
    }//为防TextViewTabColor滚动时残留先清掉
    private var isClickMore = false
    fun onItemClick(lastIndex: Int, curIndex: Int) {
        isTabClick = true
        mCurrentIndex = curIndex
        mLastIndex = lastIndex
        when (mViewPager) {
            null -> {
                autoScaleView
                doAnim(lastIndex, curIndex, mBeanTab?.tabClickAnimTime ?: 0)
            }
            else -> {
                if (abs(mCurrentIndex - mLastIndex) > 1) {
                    clearColorText
                    isClickMore = true
                    autoScaleView
                    doAnim(lastIndex, curIndex, mBeanTab?.tabClickAnimTime ?: 0)
                }
            }
        }
    }//点击事件

    private var mAnimator: ValueAnimator? = null
    var mTabRect: RectF = RectF()
    fun doAnim(lastIndex: Int, curIndex: Int, animTime: Int) {
        if (mCurrentIndex != mLastIndex) {
            mAnimator?.run {
                cancel()
                mAnimator = null
            }
            mParentView?.run {
                val curView = getChildAt(curIndex)
                val lastView = getChildAt(lastIndex)
                when {
                    curView != null && lastView != null -> {
                        val lastValue = getValue(lastView)
                        val curValue = getValue(curView)
                        when {
                            isVertical -> {
                                if (mBeanTab?.tabHeight != -1) {
                                    lastValue?.apply {
                                        top = mTabRect.top
                                        bottom = mTabRect.bottom
                                    }
                                    curValue?.apply {
                                        top = (curView.measuredHeight - (mBeanTab?.tabHeight
                                            ?: -1)) / 2 + curView.top.toFloat()
                                        bottom = (mBeanTab?.tabHeight ?: -1) + curValue.top
                                    }
                                }
                            }
                            else -> {
                                if (mBeanTab?.tabWidth != -1) {
                                    lastValue?.apply {
                                        left = mTabRect.left
                                        right = mTabRect.right
                                    }
                                    val width = curView.measuredWidth
                                    when (mBeanTab?.tabType) {
                                        ConstantsFlow.RECT -> curValue?.apply {
                                            left += (1 - mOffset) * width / 2
                                            right = width * mOffset + left
                                        }
                                        else -> curValue?.apply {
                                            left = (width - (mBeanTab?.tabWidth
                                                ?: -1)) / 2 + curView.left.toFloat()
                                            right = (mBeanTab?.tabWidth ?: -1) + left
                                        }
                                    }
                                }
                            }
                        }
                        ObjectAnimator.ofObject(TabTypeEvaluator(), lastValue, curValue).apply {
                            duration = animTime.toLong()
                            interpolator = LinearInterpolator()
                            addUpdateListener {
                                valueChange(it.animatedValue as TabValue)
                                mParentView?.postInvalidate()
                            }
                            addListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    super.onAnimationEnd(animation)
                                    mViewPager?.let {
                                        mParentView?.mAdapter?.let { adapter ->
                                            for (i in 0 until adapter.itemCount) {
                                                val child = mParentView?.getChildAt(i)
                                                when (i) {
                                                    mCurrentIndex ->
                                                        adapter.onItemSelectState(child, true)
                                                    else -> adapter.onItemSelectState(child, false)
                                                }
                                            }
                                        }
                                    }
                                }
                            })
                            mAnimator = this
                        }.start()
                    }
                    else -> mAnimator?.run {
                        end()
                        mAnimator = null
                    }
                }
            }
        }
    }//执行点击移动动画

    private fun getValue(view: View?): TabValue? = TabValue().apply {
        left = (view?.left ?: 0).toFloat() + (mBeanTab?.tabMarginLeft ?: 0)
        top = (view?.top ?: 0).toFloat() + (mBeanTab?.tabMarginTop ?: 0)
        right = (view?.right ?: 0).toFloat() - (mBeanTab?.tabMarginRight ?: 0)
        bottom = (view?.bottom ?: 0).toFloat() - (mBeanTab?.tabMarginBottom ?: 0)
    }

    protected open fun valueChange(value: TabValue?) = mTabRect.run {
        left = value?.left ?: 0f
        right = value?.right ?: 0f
    }//item偏移变化

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        mParentView?.run {
            val curView = getChildAt(position)
            val offset = curView.measuredWidth * positionOffset
            var scrollX = (curView.left + offset).toInt()
            if (offset > 0 && positionOffset > 0) {
                if (!isClickMore && position < childCount - 1) {
                    val transView = getChildAt(position + 1)//偏移view
                    if ((mBeanTab?.autoScale == true) && (mBeanTab?.scaleFactor ?: 1f) > 0) {
                        val factor = (mBeanTab?.scaleFactor ?: 1f) % 1
                        val transScale = 1 + factor * positionOffset
                        val curScale = 1 + factor * (1 - positionOffset)
                        transView?.scaleX = transScale
                        transView?.scaleY = transScale
                        curView.scaleX = curScale
                        curView.scaleY = curScale
                    }//大小渐变
                    var left = curView.left + positionOffset * ((transView?.left
                        ?: 0) - curView.left)//左边偏移量
                    var right =
                        curView.right + positionOffset * (transView.right - curView.right)//右边宽度变化
                    if (mBeanTab?.tabWidth != -1) {
                        left = curView.left + (curView.measuredWidth - (mBeanTab?.tabWidth
                            ?: 0)) / 2f//拿到左边初始坐标
                        left += positionOffset * (curView.measuredWidth + transView.measuredWidth) / 2f//拿到偏移坐标
                        right = left + (mBeanTab?.tabWidth ?: 0)
                    }
                    mTabRect.left = left
                    mTabRect.right = right
                    valueChange(TabValue(mTabRect.left, mTabRect.right))
                    postInvalidate()
                    if (mTextViewId != -1 && isColorText) {
                        curView.findViewById<TextViewTabColor?>(mTextViewId)?.apply {
                            detection = TextViewTabColor.DEC_RIGHT
                            progress = 1 - positionOffset
                        }
                        transView.findViewById<TextViewTabColor?>(mTextViewId)?.apply {
                            detection = TextViewTabColor.DEC_LEFT
                            progress = positionOffset
                        }
                    }//颜色渐变
                }
                if (isCanMove) when {
                    scrollX > mViewWidth / 2 - paddingLeft -> {
                        scrollX -= mViewWidth / 2 - paddingLeft
                        when {
                            scrollX > mRightBound - mViewWidth ->
                                scrollTo(mRightBound - mViewWidth, 0)
                            else -> scrollTo(scrollX, 0)
                        }
                    }
                    else -> scrollTo(0, 0)
                }
            }
        }
    }//为了避免卡顿，点击时不让它持续变化

    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_SETTLING) {
            if (!isTabClick && mViewPager != null) {
                mLastIndex = mCurrentIndex
                mCurrentIndex = mViewPager?.currentItem ?: 0
                if (abs(mCurrentIndex - mLastIndex) > 1) {
                    isClickMore = true
                    clearColorText
                    doAnim(mLastIndex, mCurrentIndex, mBeanTab!!.tabClickAnimTime)
                    autoScaleView
                }
            }
        }
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            isClickMore = false
            isTabClick = false
        }
    }

    override fun onPageSelected(position: Int) {
        mLastIndex = mCurrentIndex
        mCurrentIndex = position
        chooseSelectedPosition(position)
    }

    fun chooseSelectedPosition(position: Int) = mParentView?.run {
        if (mTextViewId != -1 && isTextView && !isColorText) {
            for (i in 0 until childCount) {
                val textView = getChildAt(i)?.findViewById<TextView?>(mTextViewId)
                when (i) {
                    position -> textView?.setTextColor(mSelectedColor)
                    else -> textView?.setTextColor(mUnSelectedColor)
                }
            }
        }
    }//选择某个item

    private val clearScale = mParentView?.run {
        if ((mBeanTab?.autoScale == true) && (mBeanTab?.scaleFactor ?: 1f) > 1) {
            for (i in 0 until childCount) {
                getChildAt(i).apply {
                    scaleX = 1f
                    scaleY = 1f
                }
            }
        }
    }//清掉属性动画

    fun chooseIndex(lastIndex: Int, curIndex: Int) {
        mCurrentIndex = curIndex
        mLastIndex = lastIndex
        mViewPager?.let { chooseSelectedPosition(curIndex) }
        clearColorText
        clearScale
        mParentView?.getChildAt(mCurrentIndex)?.let { child ->
            doAnim(mLastIndex, mCurrentIndex, 0)
            mOffset = (mBeanTab?.tabWidth ?: -1) * 1f / child.measuredWidth
            if (mTextViewId != -1) child.findViewById<View?>(mTextViewId).apply {
                if (this is TextViewTabColor) {
                    isColorText = true
                    setTextColor(changeColor)
                }
                if (this is TextView) isTextView = true
            }
            if ((mBeanTab?.autoScale == true) && (mBeanTab?.scaleFactor ?: 1f) > 1) {
                child.scaleX = mBeanTab?.scaleFactor ?: 1f
                child.scaleY = mBeanTab?.scaleFactor ?: 1f
            }
        }//恢复之前效果
    }//选中默认颜色

    abstract fun draw(canvas: Canvas?)//绘制不同view
}