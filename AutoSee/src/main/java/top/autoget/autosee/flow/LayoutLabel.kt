package top.autoget.autosee.flow

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import top.autoget.autosee.R
import top.autoget.autosee.flow.adapter.AdapterLabel
import top.autoget.autosee.flow.adapter.FlowListenerAdapter
import top.autoget.autosee.flow.bean.BeanLabel
import top.autoget.autosee.flow.layout.LayoutScroll

class LayoutLabel//标签流式布局：支持单选多选
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LayoutScroll(context, attrs, defStyleAttr) {
    var mMaxSelectCount: Int
    var isAutoScroll: Boolean
    private var mShowMoreLines = -1
    private var mShowMoreColor: Int
    private var mShowMoreLayoutId: Int
    private var mHandUpLayoutId: Int
    private var isHasMoreView = false
    private var mMoreView: View? = null
    private var mHandUpView: View? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LayoutLabel)
        try {
            typedArray.run {
                mMaxSelectCount = getInteger(R.styleable.LayoutLabel_label_maxSelectCount, 1)
                isAutoScroll = getBoolean(R.styleable.LayoutLabel_label_isAutoScroll, true)
                mShowMoreLines = getInteger(R.styleable.LayoutLabel_label_showLine, -1)
                labelLines = mShowMoreLines
                mShowMoreColor =
                    getColor(R.styleable.LayoutLabel_label_showMore_Color, Color.RED)
                mShowMoreLayoutId =
                    getResourceId(R.styleable.LayoutLabel_label_showMore_layoutId, -1)
                mHandUpLayoutId =
                    getResourceId(R.styleable.LayoutLabel_label_handUp_layoutId, -1)
            }
        } finally {
            typedArray.recycle()
        }
        if (mShowMoreLayoutId != -1) {
            isHasMoreView = true
            mMoreView = LayoutInflater.from(getContext())
                .inflate(mShowMoreLayoutId, this@LayoutLabel, false)
        }
        if (mHandUpLayoutId != -1)
            mHandUpView = LayoutInflater.from(getContext())
                .inflate(mHandUpLayoutId, this@LayoutLabel, false)
        isClickable = true
        setWillNotDraw(false)
    }

    override var isLabelAutoScroll: Boolean = isAutoScroll
    fun setLabelBean(beanLabel: BeanLabel?) {
        if (mMaxSelectCount != beanLabel?.maxSelectCount)
            mMaxSelectCount = beanLabel?.maxSelectCount ?: 0
        if (isAutoScroll != beanLabel?.isAutoScroll) isAutoScroll = beanLabel?.isAutoScroll ?: false
        if (mShowMoreLines != beanLabel?.showLines) {
            mShowMoreLines = beanLabel?.showLines ?: 0
            labelLines = mShowMoreLines
        }
        if (beanLabel?.showMoreColor != -2) mShowMoreColor = beanLabel?.showMoreColor ?: -2
        if (beanLabel?.showMoreLayoutId != -1) {
            mShowMoreLayoutId = beanLabel?.showMoreLayoutId ?: -1
            mMoreView = LayoutInflater.from(context).inflate(mShowMoreLayoutId, this, false)
            isHasMoreView = true
        }
        if (beanLabel?.handUpLayoutId != -1) {
            mHandUpLayoutId = beanLabel?.handUpLayoutId ?: -1
            mHandUpView = LayoutInflater.from(context).inflate(mHandUpLayoutId, this, false)
        }
    }//设置自定义属性

    private var mAdapter: AdapterLabel<*>? = null
    private val notifyData = {
        removeAllViews()
        for (i in 0 until (mAdapter?.itemCount ?: 0)) {
            val view =
                mAdapter?.layoutId?.let { LayoutInflater.from(context).inflate(it, this, false) }
            mAdapter?.bindView(view, mAdapter?.dataList?.get(i), i)
            addView(view)
            onItemViewConfig(mAdapter, view, i)
        }
    }

    private var mLastPosition = -1
    fun setSelects(vararg index: Int?) {
        if (index.isNotEmpty()) for (i in index) {
            for (j in 0 until childCount) {
                val view = getChildAt(j)
                if (i == j) {
                    view.isSelected = true
                    mLastPosition = j
                    mAdapter?.onItemSelectState(view, true)
                    break
                } else mAdapter?.onItemSelectState(view, false)
            }
        }
    }//设置要选中的数据

    private fun onItemViewConfig(flowAdapter: AdapterLabel<*>?, view: View?, position: Int) {
        view?.setOnLongClickListener { flowAdapter?.onItemLongClick(it, position) ?: false }
        view?.setOnClickListener OnClickListener@{
            flowAdapter?.onItemClick(it, flowAdapter.dataList?.get(position), position)
            when (mMaxSelectCount) {
                1 -> if (mLastPosition != position) {
                    flowAdapter?.onFocusChanged(getSelectedView()?.apply {
                        isSelected = false
                        flowAdapter.onItemSelectState(this, false)
                    }, it)
                    when {
                        it.isSelected -> {
                            it.isSelected = false
                            flowAdapter?.onItemSelectState(it, false)
                        }
                        else -> {
                            it.isSelected = true
                            flowAdapter?.onItemSelectState(it, true)
                        }
                    }//进行反选
                }
                else -> {
                    when {
                        it.isSelected -> {
                            it.isSelected = false
                            flowAdapter?.onItemSelectState(it, false)
                        }
                        else -> {
                            it.isSelected = true
                            flowAdapter?.onItemSelectState(it, true)
                        }
                    }//进行反选
                    if (getSelectedCount() > mMaxSelectCount) {
                        it.isSelected = false
                        flowAdapter?.onItemSelectState(it, false)
                        flowAdapter?.onReachMaxCount(getSelectedList(), mMaxSelectCount)
                        return@OnClickListener
                    }
                }
            }//是否单选
            mLastPosition = position
        }
    }

    private fun getSelectedCount(): Int {
        var count = 0
        for (i in 0 until childCount) {
            if (getChildAt(i).isSelected) count++
        }
        return count
    }//获取选中个数

    private fun getSelectedView(): View? {
        for (i in 0 until childCount) {
            getChildAt(i).run { if (isSelected) return this }
        }
        return null
    }//获取选中view，适合单选

    private fun getSelectedList(): MutableList<Int?> = mutableListOf<Int?>().apply {
        for (i in 0 until childCount) {
            if (getChildAt(i).isSelected) add(i)
        }
    }//获取选中数据

    fun setAdapter(adapter: AdapterLabel<*>?) {
        mAdapter = adapter
        mAdapter?.flowListenerAdapter = LabelListener()
        notifyData
    }

    inner class LabelListener : FlowListenerAdapter() {
        override fun notifyDataChanged() {
            super.notifyDataChanged()
            notifyData
        }

        override fun resetAllStatus() {
            super.resetAllStatus()
            for (i in 0 until childCount) {
                mAdapter?.onItemSelectState(getChildAt(i).apply { isSelected = false }, false)
            }
        }
    }

    private val mBitRect: RectF = RectF()
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (isLabelMoreLine) mMoreView?.let {
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> if (mBitRect.contains(ev.x, ev.y)) return true
            }//如果在范围内，截取该事件
            return super.onInterceptTouchEvent(ev)
        }
        return super.onInterceptTouchEvent(ev)
    }

    private var mBitmap: Bitmap? = null
    private var isHasHandUpView = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> if (mBitRect.contains(event.x, event.y)) mAdapter?.let {
                mBitmap = null
                when {
                    isHasMoreView && isLabelMoreLine -> {
                        labelLines = -1
                        mAdapter?.onShowMoreClick(mMoreView)
                        mHandUpView?.let {
                            isHasHandUpView = true
                            isHasMoreView = false
                        }
                    }
                    isHasHandUpView -> {
                        labelLines = mShowMoreLines
                        mAdapter?.onHandUpClick(mHandUpView)
                        mMoreView?.let {
                            isHasHandUpView = false
                            isHasMoreView = true
                        }
                    }
                }
                requestLayout()
            }
        }
        return super.onTouchEvent(event)
    }

    private val mPaint: Paint = Paint().apply { isAntiAlias = true }
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        when {
            isHasMoreView && mBitmap == null && mMoreView != null -> {
                mMoreView?.layout(0, 0, width, mMoreView?.measuredHeight ?: 0)
                mMoreView?.buildDrawingCache()
                mBitmap = mMoreView?.drawingCache
                mPaint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.TRANSPARENT, mShowMoreColor, Shader.TileMode.CLAMP
                )
                mBitRect.set(
                    l.toFloat(), height - (mMoreView?.measuredHeight?.toFloat()
                        ?: 0f), r.toFloat(), height.toFloat()
                )
            }
            isHasHandUpView && mBitmap == null -> {
                mHandUpView?.layout(0, 0, width, mHandUpView?.measuredHeight ?: 0)
                mHandUpView?.buildDrawingCache()
                mBitmap = mHandUpView?.drawingCache
                mBitRect.set(
                    l.toFloat(), height - (mMoreView?.measuredHeight?.toFloat()
                        ?: 0f), r.toFloat(), height.toFloat()
                )
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
        mBitmap?.let {
            when {
                isLabelMoreLine && isHasMoreView -> {
                    canvas?.drawPaint(mPaint)
                    canvas?.drawBitmap(it, mBitRect.left, mBitRect.top, null)
                }
                isHasHandUpView -> canvas?.drawBitmap(it, mBitRect.left, mBitRect.top, null)
                else -> {
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isHasMoreView) {
            measureChild(mMoreView, widthMeasureSpec, heightMeasureSpec)
            mViewHeight += (mMoreView?.measuredHeight ?: 0) / 2//添加它的1/2来变模糊
            setMeasuredDimension(mLineWidth, mViewHeight)
        }
        if (isHasHandUpView) {
            measureChild(mHandUpView, widthMeasureSpec, heightMeasureSpec)
            mViewHeight += (mHandUpView?.measuredHeight ?: 0)
            setMeasuredDimension(mLineWidth, mViewHeight)
        }
    }
}