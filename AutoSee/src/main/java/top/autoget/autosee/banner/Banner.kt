package top.autoget.autosee.banner

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autosee.R
import top.autoget.autosee.banner.indicat.IndicatorCircle
import top.autoget.autosee.banner.indicat.IndicatorRect
import top.autoget.autosee.banner.indicat.IndicatorText
import top.autoget.autosee.banner.listen.*
import top.autoget.autosee.banner.trans.TransformerCard
import top.autoget.autosee.banner.trans.TransformerDepthPage
import top.autoget.autosee.banner.trans.TransformerMz
import top.autoget.autosee.banner.trans.TransformerZoomOutPage

class Banner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    val viewPager2: ViewPager2 = ViewPager2(context).apply {
        clipToPadding = false
        layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    private var isAutoLoop: Boolean = false//是否自动轮播
    private var isCycle: Boolean = false//是否填充循环
    private var mLoopTime: Int = 2000
    private var mSmoothTime: Int = 600
    private var mLoopMaxCount = -1
    private var mLeftMargin: Int = 0
    private var mRightMargin: Int = 0
    private var mCardHeight: Int = 15
    private var mTypeBannerTrans: TypeBannerTrans? = TypeBannerTrans.UNKNOWN
    private val mInflater: LayoutInflater
    private val mScreenRect: Rect

    init {
        removeAllViews()
        clipChildren = false
        addView(viewPager2)//Viewpager2为final类不能继承，只能通过FrameLayout添加
        initAttrs(attrs)
        mInflater = LayoutInflater.from(context)
        BannerKit.initSwitchTime(getContext(), viewPager2, mSmoothTime)
        DisplayMetrics().apply {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getMetrics(this)
        }.let { mScreenRect = Rect(0, 0, it.widthPixels, it.heightPixels) }
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Banner)
        try {
            typedArray.run {
                isAutoLoop = getBoolean(R.styleable.Banner_banner_isAutoLoop, false)
                isCycle = getBoolean(R.styleable.Banner_banner_isCycle, false)
                if (isAutoLoop) isCycle = true//如果支持自动轮播，则自动循环填充数据
                mLoopTime = getInteger(R.styleable.Banner_banner_loopTime, 2000)
                mSmoothTime = getInteger(R.styleable.Banner_banner_switchTime, 600)
                mLoopMaxCount = getInteger(R.styleable.Banner_banner_loop_max_count, -1)
                mLeftMargin = getDimensionPixelSize(R.styleable.Banner_banner_l_margin, 0)
                mRightMargin = getDimensionPixelSize(R.styleable.Banner_banner_r_margin, 0)
                mCardHeight = getDimensionPixelSize(R.styleable.Banner_banner_card_height, 15)
                val type = getInteger(R.styleable.Banner_banner_transformer, -1)
                mTypeBannerTrans =
                    if (type != -1) TypeBannerTrans.values()[type] else TypeBannerTrans.UNKNOWN
                setTransformer(mTypeBannerTrans, mCardHeight)
            }
        } finally {
            typedArray.recycle()
        }
    }

    private var mCurrentIndex = 0
    fun setCurrentPosition(index: Int): Banner = this.apply { mCurrentIndex = index }

    companion object {
        private const val LOOP_MSG = 0x1001
        private const val LOOP_COUNT = 5000
    }

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == LOOP_MSG) {
                if (isAutoLoop) {
                    mCurrentIndex = viewPager2.currentItem//重新获取index
                    if (mCurrentIndex >= LOOP_COUNT / 2) mCurrentIndex++
                    if (mCurrentIndex > LOOP_COUNT) mCurrentIndex = LOOP_COUNT / 2
                    viewPager2.currentItem = mCurrentIndex
                    sendEmptyMessageDelayed(LOOP_MSG, mLoopTime.toLong())
                }
            }
        }
    }
    private var mIndicator: View? = null
    fun addIndicator(indicator: View?): Banner = this.apply { mIndicator = indicator }
    fun addPageBean(beanPage: BeanPage): Banner = this.apply {
        isAutoLoop = beanPage.isAutoLoop
        if (isAutoLoop) isCycle = true
        if (beanPage.loopTime != 0) mLoopTime = beanPage.loopTime
        if (beanPage.smoothScrollTime != 0) mSmoothTime = beanPage.smoothScrollTime
        if (beanPage.loopMaxCount != -1) mLoopMaxCount = beanPage.loopMaxCount
        if (beanPage.cardHeight != 0) mCardHeight = beanPage.cardHeight
        if (beanPage.typeBannerTrans != TypeBannerTrans.UNKNOWN) {
            mTypeBannerTrans = beanPage.typeBannerTrans
            setTransformer(mTypeBannerTrans, mCardHeight)
        }
    }//放在setPageListener之前

    private fun setTransformer(typeBannerTrans: TypeBannerTrans?, cardHeight: Int) =
        when (typeBannerTrans) {
            TypeBannerTrans.CARD -> TransformerCard()//throw RuntimeException("Banner cannot support Card mode")
            TypeBannerTrans.MZ -> TransformerMz().apply { setMzMargin(mLeftMargin, mRightMargin) }
            TypeBannerTrans.ZOOM -> TransformerZoomOutPage()
            TypeBannerTrans.DEPTH -> TransformerDepthPage()
            else -> null
        }?.let { viewPager2.setPageTransformer(it.transformer) }

    private fun setMzMargin(leftWidth: Int, rightWidth: Int) {
        (viewPager2.getChildAt(0) as RecyclerView).apply {
            if (viewPager2.orientation == ViewPager2.ORIENTATION_HORIZONTAL)
                setPadding(leftWidth, viewPager2.paddingTop, rightWidth, viewPager2.paddingBottom)
            viewPager2.setPageTransformer(TransformerMz().transformer)
            clipToPadding = false
        }
    }//超出屏幕

    val isOutVisibleWindow: Boolean
        get() = IntArray(2).apply { getLocationOnScreen(this) }[1]
            .let { it <= 0 || it > mScreenRect.height() - height }//超出屏幕
    val stopAnim = {
        if (isAutoLoop) mHandler.removeMessages(LOOP_MSG)
    }
    val startAnim = {
        if (isAutoLoop) mHandler.run {
            removeMessages(LOOP_MSG)
            sendEmptyMessageDelayed(LOOP_MSG, mLoopTime.toLong())
        }
    }
    private var mDataCount = 0
    private val mDataList: MutableList<Any> = mutableListOf()
    private var viewAdapter: ViewAdapter<*>? = null
    fun <T> setPageListener(
        layoutId: Int, dataList: MutableList<T?>?, listener: PageListener<T>
    ) {
        stopAnim
        if (dataList?.isNotEmpty() == true) {
            mDataCount = dataList.size
            if (mLoopMaxCount != -1) isCycle = mDataCount >= mLoopMaxCount
            mDataList.apply { clear() }.addAll(listOf(dataList))
            listener.setDataList(mDataList)
            viewPager2.apply {
                adapter =
                    ViewAdapter<Any?>(dataList, layoutId, listener).apply { viewAdapter = this }
                offscreenPageLimit = 3
                currentItem = getStartSelectItem(mDataCount) + mCurrentIndex
            }
            mIndicator?.let { chooseIndicator(dataList.size, it) }
        }
    }

    internal inner class ViewAdapter<T>(
        var list: List<T>, var layoutId: Int, var listener: PageListener<*>
    ) : RecyclerView.Adapter<RecyclerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder =
            RecyclerViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

        override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
            val index: Int = if (isCycle) position % list.size else position
            viewTouch(holder.itemView, listener, index)
            listener.bindView(holder.itemView, list[index], index)
        }

        override fun getItemCount(): Int =
            if (isCycle) list.size + LOOP_COUNT else list.size
    }

    private var mLastTime: Long = 0
    private fun viewTouch(view: View?, listener: PageListener<*>?, position: Int) {
        if (view != null && position >= 0) view.setOnTouchListener(OnTouchListener { _, event ->
            stopAnim
            when (event.action) {
                MotionEvent.ACTION_DOWN -> mLastTime = nowMillis
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (nowMillis - mLastTime < 200 && listener != null && mDataList.size > 0)
                        listener.onItemClick(view, mDataList[position], position)
                    startAnim
                }
            }
            false
        })
    }

    private fun getStartSelectItem(readCount: Int): Int = when {
        readCount == 0 -> 0
        isCycle -> {
            val count = LOOP_COUNT / 2
            var currentItem = count
            when {
                count % readCount == 0 -> currentItem
                else -> {
                    while (currentItem % readCount != 0) currentItem++
                    currentItem
                }
            }
        }
        else -> 0
    }

    private fun chooseIndicator(count: Int, indicator: View) = when (indicator) {
        is IndicatorText -> indicator.addPagerData(count, viewPager2)
        is IndicatorCircle -> indicator.addPagerData(count, viewPager2)
        is IndicatorRect -> indicator.addPagerData(count, viewPager2)
        else -> {
        }
    }

    private var firstLayout = true
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (viewPager2.adapter != null) try {
            ViewPager2::class.java.getDeclaredField("mFirstLayout")
                .apply { isAccessible = true }.let { it[this] = firstLayout }
            viewPager2.currentItem = viewPager2.currentItem
        } catch (e: Exception) {
            e.printStackTrace()
        }//处理recyclerview回收机制导致轮播图不起作用的问题
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        firstLayout = false
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (isAutoLoop && visibility == VISIBLE) startAnim else stopAnim
    }

    override fun detachAllViewsFromParent() {
        super.detachAllViewsFromParent()
        mHandler.removeCallbacksAndMessages(null)
    }
}