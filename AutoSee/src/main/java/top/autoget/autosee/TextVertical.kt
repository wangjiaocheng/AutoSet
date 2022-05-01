package top.autoget.autosee

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.TextSwitcher
import android.widget.TextView
import android.widget.ViewSwitcher

class TextVertical
@JvmOverloads constructor(private var mContext: Context?, attrs: AttributeSet? = null) :
    TextSwitcher(mContext, attrs), ViewSwitcher.ViewFactory {
    init {
        mContext = context
    }

    var mPadding = 5
    var mTextSize = 16f
    var mTextColor = Color.BLACK

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    var mOnItemClickListener: OnItemClickListener? = null
    private var currentId = -1
    var titles: MutableList<String> = mutableListOf()
        set(titles) {
            field.run {
                clear()
                addAll(titles)
            }
            currentId = -1
        }

    override fun makeView(): View = TextView(mContext).apply {
        gravity = Gravity.CENTER_VERTICAL or Gravity.START
        setPadding(mPadding, mPadding, mPadding, mPadding)
        textSize = mTextSize
        setTextColor(mTextColor)
        maxLines = 1
        isClickable = true
        setOnClickListener {
            mOnItemClickListener?.let {
                if (titles.size > 0 && currentId != -1)
                    it.onItemClick(currentId % titles.size)
            }
        }
    }

    private var mHandler: Handler? = null

    companion object {
        private const val FLAG_AUTO_SCROLL_START = 0
        private const val FLAG_AUTO_SCROLL_STOP = 1
    }

    val autoScrollStart: Boolean?
        get() = mHandler?.sendEmptyMessage(FLAG_AUTO_SCROLL_START)
    val autoScrollStop: Boolean?
        get() = mHandler?.sendEmptyMessage(FLAG_AUTO_SCROLL_STOP)

    fun setTimeTextStill(time: Long) {
        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    FLAG_AUTO_SCROLL_START -> {
                        if (titles.size > 0) {
                            currentId++
                            setText(titles[currentId % titles.size])
                        }
                        mHandler?.sendEmptyMessageDelayed(FLAG_AUTO_SCROLL_START, time)
                    }
                    FLAG_AUTO_SCROLL_STOP -> mHandler?.removeMessages(FLAG_AUTO_SCROLL_START)
                    else -> {
                    }
                }
            }
        }
    }//间隔时间

    fun setTimeAnim(animDuration: Long) {
        setFactory(this)
        inAnimation = TranslateAnimation(0f, 0f, animDuration.toFloat(), 0f).apply {
            duration = animDuration
            interpolator = AccelerateInterpolator()
        }
        outAnimation = TranslateAnimation(0f, 0f, 0f, -animDuration.toFloat()).apply {
            duration = animDuration
            interpolator = AccelerateInterpolator()
        }
    }
}