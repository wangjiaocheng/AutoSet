package top.autoget.autokit

import android.Manifest
import android.os.SystemClock
import android.view.View
import androidx.annotation.RequiresPermission
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.ToastKit.showShort
import top.autoget.autokit.VibrateKit.vibrateOnce

object ClickKit {
    interface OnDoListener {
        fun doSomething()
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun initFastClickAndVibrate(v: View, onDoListener: OnDoListener): Any = when {
        isFastDoubleClick(v) -> showShort("请不要重复点击")
        else -> onDoListener.doSomething().apply { vibrateOnce(100) }
    }

    private const val DEFAULT_INTERVAL_MILLIS: Long = 1000//默认点击时间间隔（毫秒）
    private var lastClickTime: Long = 0//最近一次点击时间
    private var lastClickViewId: Int = 0//最近一次点击控件ID

    @JvmOverloads
    fun isFastDoubleClick(v: View, intervalMillis: Long = DEFAULT_INTERVAL_MILLIS): Boolean {
        val time = nowMillis
        val viewId = v.id
        return when {
            time - lastClickTime in 1 until intervalMillis && viewId == lastClickViewId -> true
            else -> {
                lastClickTime = time
                lastClickViewId = viewId
                false
            }
        }
    }//是否快速双击

    interface OnContinuousClickListener {
        fun onContinuousClick()
    }//多次点击监听

    private const val COUNTS = 5//点击次数
    private var hits: LongArray = LongArray(COUNTS)
    private const val DURATION: Long = 1000//规定有效时间
    fun doClick(onContinuousClickListener: OnContinuousClickListener?) {
        System.arraycopy(hits, 1, hits, 0, hits.size - 1)//每次点击数组向前移一位
        hits[hits.size - 1] = SystemClock.uptimeMillis()//为数组最后一位赋值，开机到现在不包含睡眠时间
        if (hits[0] >= SystemClock.uptimeMillis() - DURATION) {
            hits = LongArray(COUNTS)//重新初始化数组
            onContinuousClickListener?.onContinuousClick()
        }//如果一秒内连点五次
    }//连续点击
}