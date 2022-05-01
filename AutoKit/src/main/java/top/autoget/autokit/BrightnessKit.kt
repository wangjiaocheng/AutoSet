package top.autoget.autokit

import android.provider.Settings
import android.view.Window
import androidx.annotation.IntRange
import top.autoget.autokit.AKit.app

object BrightnessKit {
    val isAutoBrightnessEnabled: Boolean
        get() = try {
            Settings.System.getInt(
                app.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE
            ) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            false
        }

    fun setAutoBrightnessEnabled(enabled: Boolean = true): Boolean = Settings.System.putInt(
        app.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
        when {
            enabled -> Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            else -> Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        }
    )

    val brightness: Int
        get() = try {
            Settings.System.getInt(app.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            0
        }

    fun setBrightness(@IntRange(from = 0, to = 255) brightness: Int): Boolean =
        app.contentResolver.run {
            Settings.System.putInt(this, Settings.System.SCREEN_BRIGHTNESS, brightness).apply {
                notifyChange(Settings.System.getUriFor("screen_brightness"), null)
            }
        }

    fun getWindowBrightness(window: Window): Int = window.attributes.screenBrightness.let {
        (if (it < 0) it else (it * 255)).toInt()
    }

    fun setWindowBrightness(window: Window, @IntRange(from = 0, to = 255) brightness: Int) =
        window.attributes.run { screenBrightness = brightness / 255f }
}