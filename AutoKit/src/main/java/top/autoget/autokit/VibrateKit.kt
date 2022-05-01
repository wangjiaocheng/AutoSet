package top.autoget.autokit

import android.Manifest.permission.VIBRATE
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import top.autoget.autokit.AKit.app

object VibrateKit {
    private val vibrator: Vibrator = app.vibrator

    @RequiresPermission(VIBRATE)
    fun vibrateOnce(milliseconds: Int) = vibrator.vibrate(milliseconds.toLong())

    @RequiresPermission(VIBRATE)
    fun vibrateComplicated(pattern: LongArray, repeat: Int) = vibrator.vibrate(pattern, repeat)

    val vibrateStop
        @RequiresPermission(VIBRATE)
        get() = vibrator.cancel()
}//vibrateComplicated参数：等待间隔，间隔下标（剩余次数）、0一直、-1不重复