package top.autoget.automap

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import top.autoget.autokit.DensityKit.dip2px

class MapIndoorFloorSwitchView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    ScrollView(context, attrs, defStyle) {
    init {
        init(context)
    }

    private var mContext: Context = context
    var selectBitmap: Bitmap? = null
    val destroy = selectBitmap?.run {
        if (!isRecycled) {
            recycle()
            selectBitmap = null
        }
    }
    private var views: LinearLayout? = null
    private var scrollerTask: Runnable? = null
    private var initialY = 0
    private var itemHeight = 0
    var selectedIndex = 1
    var offset = 1//当第一个显示在中间时上面有几个空白，也会影响整体显示：如设置1，上下各偏移1，总共显3个；设置2总共显示5个

    interface OnIndoorFloorSwitchListener {
        fun onSelected(selectedIndex: Int)
    }

    var onIndoorFloorSwitchListener: OnIndoorFloorSwitchListener? = null
    val selectIndex: Int
        get() = items.run {
            when (size) {
                0 -> 0
                else -> kotlin.math.min(
                    size - 2 * offset,
                    kotlin.math.max(0, size - 1 - selectedIndex - offset)
                )
            }
        }
    private val onSelectedCallBack = try {
        onIndoorFloorSwitchListener?.onSelected(selectIndex)
    } catch (e: Throwable) {
    }
    private val newCheck = 50L
    private fun init(context: Context) {
        mContext = context
        isVerticalScrollBarEnabled = false
        selectBitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.map_indoor_select)
        views = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        addView(views)
        scrollerTask = Runnable {
            when {
                initialY - scrollY == 0 -> (initialY / itemHeight).let { divided ->
                    when (val remainder = initialY % itemHeight) {
                        0 -> {
                            selectedIndex = divided + offset
                            onSelectedCallBack
                        }
                        else -> when {
                            remainder > itemHeight / 2 -> post {
                                smoothScrollTo(0, initialY - remainder + itemHeight)
                                selectedIndex = divided + offset + 1
                                onSelectedCallBack
                            }
                            else -> post {
                                smoothScrollTo(0, initialY - remainder)
                                selectedIndex = divided + offset
                                onSelectedCallBack
                            }
                        }
                    }
                }
                else -> {
                    initialY = scrollY
                    postDelayed(scrollerTask, newCheck)
                }
            }
        }
    }

    var items: MutableList<String> = mutableListOf()
        set(items) {
            field.apply {
                clear()
                addAll(items)
                for (i in 0 until offset) {
                    add(0, "")
                    add("")
                }
            }
            initData
        }
    private var displayItemCount = 0//每页显示数目
    private val initData = items.run {
        if (size != 0) {
            views?.removeAllViews()
            displayItemCount = offset * 2 + 1
            for (item in reversed()) {
                views?.addView(createView(item))
            }
            refreshItemView(0)
        }
    }

    private fun createView(item: String): TextView = TextView(mContext).apply {
        layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        isSingleLine = true
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        text = item
        gravity = Gravity.CENTER
        paint.isFakeBoldText = true
        val paddingH = dip2px(8f)
        val paddingV = dip2px(6f)
        setPadding(paddingH, paddingV, paddingH, paddingV)
        if (itemHeight == 0) {
            itemHeight = measureView(this).second
            views?.layoutParams =
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, itemHeight * displayItemCount)
            this@MapIndoorFloorSwitchView.layoutParams =
                LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, itemHeight * displayItemCount)
        }
    }

    private fun measureView(view: View): Pair<Int, Int> = view.run {
        measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        )
        return Pair(measuredWidth, measuredHeight)
    }

    private fun refreshItemView(y: Int) = (y / itemHeight + offset).let { position ->
        when (val remainder = y % itemHeight) {
            0 -> position
            else -> if (remainder > itemHeight / 2) position + 1 else position
        }.let {
            views?.childCount?.let { size ->
                for (i in 0 until size) {
                    (views?.getChildAt(i) as TextView).apply {
                        when (it) {
                            i -> setTextColor(Color.parseColor("#0288ce"))
                            else -> setTextColor(Color.parseColor("#bbbbbb"))
                        }
                    }
                }
            }
        }
    }

    val selectItem: String = items[selectedIndex]
    fun setSelection(selectValue: String?) {
        if (items.size != 0) (items.size - offset - 1 - items.indexOf(selectValue)).let {
            selectedIndex = it + offset
            post { smoothScrollTo(0, it * itemHeight) }
        }
    }

    var isVisible: Boolean = visibility == View.VISIBLE
        set(isEnable) = when {
            isEnable && !field -> visibility = View.VISIBLE
            !isEnable && field -> visibility = View.GONE
            else -> {
            }
        }
    private var backGroundColor: Int = Color.parseColor("#eeffffff")
    override fun setBackgroundColor(color: Int) {
        backGroundColor = color
    }

    private var viewWidth = 0
    var strokeColor: Int = Color.parseColor("#44383838")
    var strokeW = 4f
    override fun setBackgroundDrawable(drawable: Drawable) {
        if (viewWidth == 0) viewWidth = (mContext as Activity).windowManager.defaultDisplay.width
        super.setBackgroundDrawable(object : Drawable() {
            override fun setColorFilter(cf: ColorFilter?) {}
            override fun setAlpha(alpha: Int) {}
            override fun getOpacity(): Int = PixelFormat.UNKNOWN
            override fun draw(canvas: Canvas) = try {
                canvas.run {
                    drawColor(backGroundColor)
                    selectBitmap?.let {
                        drawBitmap(it, Rect().apply {
                            left = 0
                            top = 0
                            right = 0 + it.width
                            bottom = 0 + it.height
                        }, Rect().apply {
                            left = 0
                            top = itemHeight * offset
                            right = 0 + viewWidth
                            bottom = itemHeight * (offset + 1)
                        }, Paint())
                    }
                    drawRect(clipBounds, Paint().apply {
                        color = strokeColor
                        style = Paint.Style.STROKE
                        strokeWidth = strokeW
                    })
                }
            } catch (e: Throwable) {
            }
        })
    }

    companion object {
        private const val SCROLL_DIRECTION_UP = 0
        private const val SCROLL_DIRECTION_DOWN = 1
    }

    private var scrollDirection = -1
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        refreshItemView(t)
        scrollDirection = if (t > oldt) SCROLL_DIRECTION_DOWN else SCROLL_DIRECTION_UP
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w
        background = null
    }

    override fun fling(velocityY: Int) = super.fling(velocityY / 3)
    val startScrollerTask = {
        initialY = scrollY
        postDelayed(scrollerTask, newCheck)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_UP) startScrollerTask
        return super.onTouchEvent(ev)
    }
}