package top.autoget.autokit

import android.animation.ValueAnimator
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.view.Surface
import android.view.Window
import android.view.WindowManager
import top.autoget.autokit.AKit.app

object WindowKit {
    val displayRotation: Int
        get() = when (app.windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    val isLandscape: Boolean
        get() = Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun setLandscape(activity: Activity) {
        if (activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }//在Activity中加属性android:screenOrientation="landscape"同效；不设置Activity的android:configChanges，重新调用各个生命周期，切横屏执行一次，切竖屏执行两次；设置Activity的android:configChanges="orientation"，重新调用各个生命周期，切横、竖屏执行一次；设置Activity的android:configChanges="orientation|keyboardHidden|screenSize"（4.0以上必须带最后一个参数）不会重新调用各个生命周期，只会执行onConfigurationChanged方法。

    val isPortrait: Boolean
        get() = Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    fun setPortrait(activity: Activity) {
        if (activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    fun toggleFullScreen(activity: Activity): Window = when {
        isFullScreen(activity) -> setNoFullScreen(activity)
        else -> setFullScreen(activity)
    }//setContentView之前调用，否则报错；Activity可以继承AppCompatActivity，启动时状态栏显示再隐藏；加属性android:callback="@android:style/Theme.NoTitleBar.Fullscreen"的Activity不能继承AppCompatActivity，否则报错

    fun isFullScreen(activity: Activity): Boolean =
        activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == WindowManager.LayoutParams.FLAG_FULLSCREEN

    fun setNoFullScreen(activity: Activity): Window =
        activity.apply { requestWindowFeature(Window.FEATURE_OPTIONS_PANEL) }.window
            .apply { clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) }

    fun setFullScreen(activity: Activity): Window =
        activity.apply { requestWindowFeature(Window.FEATURE_NO_TITLE) }.window
            .apply { addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) }

    fun dimBackground(activity: Activity, from: Float = 0.0f, to: Float = 1.0f) {
        ValueAnimator.ofFloat(from, to).apply {
            duration = 500
            addUpdateListener { activity.window.attributes.alpha = animatedValue as Float }
        }.start()
    }
}