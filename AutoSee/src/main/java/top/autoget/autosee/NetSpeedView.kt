package top.autoget.autosee

import android.content.Context
import android.content.res.TypedArray
import android.net.TrafficStats
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import top.autoget.autokit.DataKit.formatTwo
import top.autoget.autokit.HandleKit.backgroundHandler

class NetSpeedView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    FrameLayout(context, attrs, defStyle) {
    private var sumTV: TextView? = null
    private var bigRL: RelativeLayout? = null
    private var mobileTx: TextView? = null
    private var mobileTxTV: TextView? = null
    private var mobileRx: TextView? = null
    private var mobileRxTV: TextView? = null
    private var wlanTx: TextView? = null
    private var wlanTxTV: TextView? = null
    private var wlanRx: TextView? = null
    private var wlanRxTV: TextView? = null
    private var mTextColor: Int = resources.getColor(R.color.white)
    private var mTextSize: Int = 12
    var isMulti = false
        set(isMulti) {
            field = isMulti
            sumTV?.visibility = if (field) View.GONE else View.VISIBLE
            bigRL?.visibility = if (field) View.VISIBLE else View.GONE
        }
    var timeInterval: Long = 500//5秒
    private var timeSpan = 2000.0
    private var totalTX: Long = 0
    private var sumMobileRecv: Long = 0
    private var sumMobileSend: Long = 0
    private var sumWLANRecv: Long = 0
    private var sumWLANSend: Long = 0
    val updateViewData =
        (TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes()).let { tempSum ->
            ((tempSum - totalTX) * 1000 / timeSpan).let { totalSpeed ->
                totalTX = tempSum
                TrafficStats.getMobileRxBytes().apply { sumMobileRecv = this }.let { tempMobileRx ->
                    TrafficStats.getMobileTxBytes().apply { sumMobileSend = this }
                        .let { tempMobileTx ->
                            when {
                                isMulti -> {
                                    ((tempMobileRx - sumMobileRecv) * 1000 / timeSpan).let { speedMobileRecv ->
                                        if (speedMobileRecv >= 0.0)
                                            mobileRxTV?.text = showSpeed(speedMobileRecv)
                                    }
                                    ((tempMobileTx - sumMobileSend) * 1000 / timeSpan).let { speedMobileSend ->
                                        if (speedMobileSend >= 0.0)
                                            mobileTxTV?.text = showSpeed(speedMobileSend)
                                    }
                                    (((TrafficStats.getTotalRxBytes() - tempMobileRx).apply {
                                        sumWLANRecv = this
                                    } - sumWLANRecv) * 1000 / timeSpan).let { speedWLANRecv ->
                                        if (speedWLANRecv >= 0.0)
                                            wlanRxTV?.text = showSpeed(speedWLANRecv)
                                    }
                                    (((TrafficStats.getTotalTxBytes() - tempMobileTx).apply {
                                        sumWLANSend = this
                                    } - sumWLANSend) * 1000 / timeSpan).let { speedWLANSend ->
                                        if (speedWLANSend >= 0.0)
                                            wlanTxTV?.text = showSpeed(speedWLANSend)
                                    }
                                }
                                else -> if (totalSpeed >= 0.0) sumTV?.text = showSpeed(totalSpeed)
                            }
                        }
                }
            }
        }

    private fun showSpeed(speed: Double): String = when {
        speed < 1048576.0 -> "${formatTwo.format(speed / 1024.0)}KB/s"
        else -> "${formatTwo.format(speed / 1048576.0)}MB/s"//1024*1024
    }

    private val task = object : Runnable {
        override fun run() {
            backgroundHandler.postDelayed(this, timeInterval)
            updateViewData
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.netspeed_view, this)
        sumTV = findViewById(R.id.SumTV)
        bigRL = findViewById(R.id.BigRL)
        mobileTx = findViewById(R.id.MobileTx)
        mobileTxTV = findViewById(R.id.MobileTxTV)
        mobileRx = findViewById(R.id.MobileRx)
        mobileRxTV = findViewById(R.id.MobileRxTV)
        wlanTx = findViewById(R.id.WlanTx)
        wlanTxTV = findViewById(R.id.WlanTxTV)
        wlanRx = findViewById(R.id.WlanRx)
        wlanRxTV = findViewById(R.id.WlanRxTV)
        val typedArray: TypedArray =
            getContext().obtainStyledAttributes(attrs, R.styleable.NetSpeedView)
        try {
            typedArray.run {
                mTextColor =
                    getColor(R.styleable.NetSpeedView_TextColor, resources.getColor(R.color.white))
                mTextSize = getDimensionPixelSize(R.styleable.NetSpeedView_NsTextSize, 12)
                isMulti = getBoolean(R.styleable.NetSpeedView_isMulti, false)
            }
        } finally {
            typedArray.recycle()
        }
        setTextColor(mTextColor)
        setTextSize(mTextSize)
        backgroundHandler.postDelayed(task, timeInterval)
    }

    fun setTextColor(textColor: Int) {
        if (textColor != 0) {
            sumTV?.setTextColor(textColor)
            mobileTx?.setTextColor(textColor)
            mobileTxTV?.setTextColor(textColor)
            mobileRx?.setTextColor(textColor)
            mobileRxTV?.setTextColor(textColor)
            wlanTx?.setTextColor(textColor)
            wlanTxTV?.setTextColor(textColor)
            wlanRx?.setTextColor(textColor)
            wlanRxTV?.setTextColor(textColor)
        }
    }

    fun setTextSize(textSize: Int) {
        if (textSize != 0) {
            sumTV?.textSize = textSize.toFloat()
            mobileTx?.textSize = textSize.toFloat()
            mobileTxTV?.textSize = textSize.toFloat()
            mobileRx?.textSize = textSize.toFloat()
            mobileRxTV?.textSize = textSize.toFloat()
            wlanTx?.textSize = textSize.toFloat()
            wlanTxTV?.textSize = textSize.toFloat()
            wlanRx?.textSize = textSize.toFloat()
            wlanRxTV?.textSize = textSize.toFloat()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        backgroundHandler.removeCallbacks(task)
    }
}