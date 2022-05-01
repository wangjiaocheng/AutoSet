package top.autoget.autokit

import android.Manifest
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.EditText
import androidx.annotation.RequiresPermission
import top.autoget.autokit.VibrateKit.vibrateComplicated

object ShakeKit {
    var shakeAnimation: Animation = TranslateAnimation(0f, 10f, 0f, 0f).apply {
        duration = 300
        interpolator = CycleInterpolator(8f)
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    fun shake(vararg editTexts: EditText) {
        if (editTexts.isNotEmpty()) {
            for (editText in editTexts) {
                editText.startAnimation(shakeAnimation)
            }
            vibrateComplicated(longArrayOf(0, 500), -1)
        }
    }
}