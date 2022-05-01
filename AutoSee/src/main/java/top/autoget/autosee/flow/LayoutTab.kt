package top.autoget.autosee.flow

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import android.widget.LinearLayout
import android.widget.Scroller
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import top.autoget.autosee.R
import top.autoget.autosee.flow.action.*
import top.autoget.autosee.flow.adapter.AdapterTab
import top.autoget.autosee.flow.adapter.FlowListenerAdapter
import top.autoget.autosee.flow.bean.BeanTab
import top.autoget.autosee.flow.common.ConstantsFlow
import top.autoget.autosee.flow.layout.AttrsKit
import top.autoget.autosee.flow.layout.LayoutScroll

class LayoutTab//表格流式布局：数据封装重绘
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LayoutScroll(context, attrs, defStyleAttr) {
    private var mBeanTab: BeanTab?
    private val mScroller: Scroller?
    private val reAdjustLayoutParams = {
        if (!isVertical && width > mWidth) when (parent as ViewGroup) {
            is LinearLayout -> layoutParams =
                (layoutParams as LinearLayout.LayoutParams).apply { gravity = Gravity.START }
            is ConstraintLayout -> {
                val params = layoutParams as ConstraintLayout.LayoutParams
                if (params.width == ConstraintLayout.LayoutParams.WRAP_CONTENT && isCanMove)
                    layoutParams = params.apply { width = 0 }
            }
        }
    }
    private var isFirst = true
    private var mActionBase: ActionBase? = null

    init {
        isClickable = true
        mBeanTab =
            AttrsKit.getBeanTab(context.obtainStyledAttributes(attrs, R.styleable.LayoutTab))
        mScroller = Scroller(getContext())
        visualCount = mBeanTab?.visualCount ?: 0
        tabOrientation = mBeanTab?.tabOrientation ?: 0
        chooseTabType(mBeanTab?.tabType ?: 0)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                reAdjustLayoutParams
                if (isFirst) {
                    isFirst = false
                    mActionBase?.run {
                        config(this@LayoutTab)
                        chooseIndex(mLastIndex, mCurrentIndex)
                        if (mViewPager == null)
                            getChildAt(mCurrentIndex)?.let { updateScroll(it, false) }//滚动对应位置
                    } ?: return
                }//横竖屏或异常重启后，重新对位置，选中index等恢复原状态
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun chooseTabType(tabStyle: Int) {
        if (tabStyle != -1) mActionBase = when (tabStyle) {
            ConstantsFlow.RECT -> ActionRect()
            ConstantsFlow.TRI -> ActionTri()
            ConstantsFlow.ROUND -> ActionRound()
            ConstantsFlow.COLOR -> ActionColor()
            ConstantsFlow.RES -> ActionRes()
            else -> null
        }
        mActionBase?.let {
            it.mContext = (this.context)
            it.configAttrs(mBeanTab)
        }//配置自定义属性给action
    }

    fun setTabBean(beanTab: BeanTab?): LayoutTab? = beanTab?.let {
        apply {
            mBeanTab = mBeanTab?.let { AttrsKit.diffBeanTab(it, beanTab) }
            if (beanTab.tabType != -1) chooseTabType(beanTab.tabType)
            if (mBeanTab != null) mActionBase?.let {
                it.configAttrs(mBeanTab)
                if (mViewPager != null && it.mViewPager == null) {
                    it.setViewPager(mViewPager)?.setTextId(mTextId)
                        ?.setSelectedColor(mSelectedColor)?.setUnSelectedColor(mUnSelectedColor)
                }
            }
            tabOrientation = (beanTab.tabOrientation)
            if (beanTab.visualCount != -1) visualCount = (beanTab.visualCount)
        }
    }//自定义属性配置覆盖xml属性

    fun setViewPager(viewPager: ViewPager?): LayoutTab? = apply {
        mViewPager = viewPager
        mActionBase?.setViewPager(viewPager)
    }

    fun setDefaultPosition(position: Int): LayoutTab? = apply { mCurrentIndex = position }
    fun setTextId(textId: Int): LayoutTab? = apply {
        mTextId = textId
        mActionBase?.setTextId(textId)
    }//不设置textId颜色选择不起作用

    fun setSelectedColor(selectedColor: Int): LayoutTab? = apply {
        mSelectedColor = selectedColor
        mActionBase?.setSelectedColor(selectedColor)
    }//设置选中颜色，在TabTextColorView不起作用

    fun setUnSelectedColor(unSelectedColor: Int): LayoutTab? = apply {
        mUnSelectedColor = unSelectedColor
        mActionBase?.setUnSelectedColor(unSelectedColor)
    }//设置默认颜色，在TabTextColorView不起作用

    private var mViewPager: ViewPager? = null
    private var mCurrentIndex = 0
    private var mLastIndex = 0
    private val notifyChanged = {
        removeAllViews()
        val adapter = mAdapter
        for (i in 0 until (adapter?.itemCount ?: 0)) {
            val view =
                adapter?.layoutId?.let { LayoutInflater.from(context).inflate(it, this, false) }
            adapter?.bindView(view, adapter.dataList?.get(i), i)
            configClick(view, i)
            addView(view)
        }
        if (mWidth == 0 && width == 0 || visualCount > 0)
            postDelayed({
                if (childCount > 0) {
                    reAdjustLayoutParams
                    mActionBase?.let {
                        it.config(this@LayoutTab)
                        mViewPager?.setCurrentItem(mCurrentIndex, false)
                        it.chooseIndex(mLastIndex, mCurrentIndex)
                        updateScroll(getChildAt(mCurrentIndex), false)
                    }
                }
            }, 5)//如果此时width为0，则是加载完布局但是数据还没有导入，则需要重新适配一下
    }
    var isItemClick = false
    private fun configClick(view: View?, i: Int) {
        view?.setOnClickListener {
            isItemClick = true
            chooseItem(i, view)
        }
        view?.setOnLongClickListener { mAdapter?.onItemLongClick(view, i) ?: false }
    }

    private fun chooseItem(position: Int, view: View?) {
        mLastIndex = mCurrentIndex
        mCurrentIndex = position
        mViewPager?.let { mLastIndex = mActionBase?.mCurrentIndex ?: 0 }
        mActionBase?.onItemClick(mLastIndex, position)
        mAdapter?.onItemClick(view, mAdapter?.dataList?.get(position), position)
        if (mViewPager == null) {
            updateScroll(view, true)
            invalidate()
        }//没有viewpager用scroller平滑过渡
    }

    private var mLastScrollPos = 0
    private fun updateScroll(view: View?, smoothScroll: Boolean) {
        if (isCanMove && view != null) {
            var scrollPos: Int = if (isVertical) view.top else view.left
            var offset: Int
            if (scrollPos != mLastScrollPos) when {
                isVertical -> when {
                    scrollPos > mHeight / 2 -> {
                        scrollPos -= mHeight / 2
                        when {
                            scrollPos < mBottomRound - mHeight -> {
                                offset = scrollPos - mLastScrollPos
                                when {
                                    smoothScroll -> mScroller?.startScroll(0, scrollY, 0, offset)
                                    else -> scrollTo(0, offset)
                                }
                                mLastScrollPos = scrollPos
                            }
                            else -> {
                                offset = mBottomRound - mHeight - scrollY
                                if (scrollY >= mBottomRound - mHeight) offset = 0
                                when {
                                    smoothScroll -> mScroller?.startScroll(0, scrollY, 0, offset)
                                    else -> scrollTo(0, mBottomRound - mHeight)
                                }
                                mLastScrollPos = mBottomRound - mHeight - offset
                            }
                        }
                    }//下边界
                    else -> {
                        offset = -scrollPos
                        when {
                            smoothScroll -> mScroller?.startScroll(0, scrollY, 0, offset)
                            else -> scrollTo(0, 0)
                        }
                        mLastScrollPos = 0
                    }
                }
                else -> when {
                    scrollPos > mWidth / 2 -> {
                        scrollPos -= mWidth / 2
                        when {
                            scrollPos < mRightBound - mWidth -> {
                                offset = scrollPos - mLastScrollPos
                                when {
                                    smoothScroll -> mScroller?.startScroll(scrollX, 0, offset, 0)
                                    else -> scrollTo(offset, 0)
                                }
                                mLastScrollPos = scrollPos
                            }
                            else -> {
                                offset = mRightBound - mWidth - scrollX
                                if (scrollX >= mRightBound - mWidth) offset = 0
                                when {
                                    smoothScroll -> mScroller?.startScroll(scrollX, 0, offset, 0)
                                    else -> scrollTo(mRightBound - mWidth, 0)
                                }
                                mLastScrollPos = mRightBound - mWidth - offset
                            }
                        }//有边界提醒
                    }
                    else -> {
                        offset = -scrollPos
                        when {
                            smoothScroll -> mScroller?.startScroll(scrollX, 0, offset, 0)
                            else -> scrollTo(0, 0)
                        }
                        mLastScrollPos = 0
                    }
                }
            }
        }//超过中间让父控件跟着移动
    }

    var mAdapter: AdapterTab<*>? = null
        set(adapter) {
            field = adapter
            field?.flowListenerAdapter = FlowListener()
            notifyChanged
        }

    inner class FlowListener : FlowListenerAdapter() {
        override fun notifyDataChanged() {
            super.notifyDataChanged()
            notifyChanged
        }

        override fun resetAllTextColor(viewId: Int, color: Int) {
            super.resetAllTextColor(viewId, color)
            for (i in 0 until childCount) {
                getChildAt(i).findViewById<TextView?>(viewId)?.setTextColor(color)
            }
        }
    }

    fun setItemClickByOutSet(position: Int) {
        isItemClick = false
        if (position in 0 until childCount) chooseItem(position, getChildAt(position))
    }//由外部设置位置，为不是自身点击，常用于recyclerview联动效果

    private var mTextId = -1
    private var mSelectedColor = -1
    private var mUnSelectedColor = -1
    fun setCusAction(actionBase: ActionBase?) {
        mActionBase = actionBase
        mActionBase?.apply { configAttrs(mBeanTab) }?.let {
            if (mViewPager != null && it.mViewPager == null) {
                it.setViewPager(mViewPager)?.setTextId(mTextId)
                    ?.setSelectedColor(mSelectedColor)?.setUnSelectedColor(mUnSelectedColor)
            }
        }
    }

    fun setItemAnim(position: Int) {
        mLastIndex = mCurrentIndex
        mCurrentIndex = position
        mActionBase?.run {
            autoScaleView
            doAnim(mLastIndex, mCurrentIndex, mBeanTab?.tabClickAnimTime ?: 0)
        }
    }//设置某个item动画

    override var isLabelFlow: Boolean = false
    override var isTabAutoScroll: Boolean = mBeanTab?.isAutoScroll ?: false
    override fun dispatchDraw(canvas: Canvas?) {
        mActionBase?.draw(canvas)
        super.dispatchDraw(canvas)
    }

    override fun computeScroll() {
        super.computeScroll()
        if (mViewPager == null && mScroller?.computeScrollOffset() == true) {
            var offset: Int
            when {
                isVertical -> {
                    offset = mScroller.currY
                    if (offset >= mBottomRound - mHeight) offset = mBottomRound - mHeight
                }
                else -> {
                    offset = mScroller.currX
                    if (offset >= mRightBound - mWidth) offset = mRightBound - mWidth
                }
            }
            if (offset <= 0) offset = 0
            if (isVertical) scrollTo(0, offset) else scrollTo(offset, 0)
            postInvalidate()
        }//有边界
    }

    override fun onSaveInstanceState(): Parcelable? = Bundle().apply {
        putParcelable("instancestatus", super.onSaveInstanceState())
        mViewPager?.let {
            mCurrentIndex = it.currentItem
            mLastIndex = 0
        } ?: run { mLastIndex = mActionBase?.mLastIndex ?: 0 }
        putInt("index", mCurrentIndex)
        putInt("lastindex", mLastIndex)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var parcelable = state
        if (parcelable is Bundle) {
            val bundle = parcelable as Bundle?
            parcelable = bundle?.getParcelable("instancestatus")
            mCurrentIndex = bundle?.getInt("index") ?: 0
            mLastIndex = bundle?.getInt("lastindex") ?: 0
        }
        super.onRestoreInstanceState(parcelable)
    }
}