package top.autoget.autokit

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import top.autoget.autokit.AKit.activityLifecycle
import top.autoget.autokit.AKit.app
import top.autoget.autokit.AKit.topActivityOrApp
import top.autoget.autokit.ApplicationKit.appPackageName
import top.autoget.autokit.HandleKit.mainHandler
import top.autoget.autokit.VersionKit.aboveJellyBean
import top.autoget.autokit.VersionKit.aboveJellyBeanMR1
import java.lang.reflect.Field

object ToastKit : LoggerKit {
    const val TopCenter: Int = Gravity.TOP
    const val TopLeft: Int = Gravity.TOP or Gravity.START
    const val TopRight: Int = Gravity.TOP or Gravity.END
    const val Center: Int = Gravity.CENTER
    const val CenterLeft: Int = Gravity.CENTER or Gravity.START
    const val CenterRight: Int = Gravity.CENTER or Gravity.END
    const val BottomCenter: Int = Gravity.BOTTOM
    const val BottomLeft: Int = Gravity.BOTTOM or Gravity.START
    const val BottomRight: Int = Gravity.BOTTOM or Gravity.END
    private var mGravity = -1
    private var mXOffset = -1
    private var mYOffset = -1

    @JvmOverloads
    fun setGravity(gravity: Int = BottomCenter, xOffset: Int = 0, yOffset: Int = 0) {
        mGravity = gravity
        mXOffset = xOffset
        mYOffset = yOffset
    }

    private var mMsgTextSize = -1
    fun setMsgTextSize(msgTextSize: Int) {
        mMsgTextSize = msgTextSize
    }

    @ColorInt
    val COLOR_TEXT_DEFAULT: Int = Color.parseColor("#FFFFFF")
    private const val COLOR_DEFAULT = -0x1000001
    private var mMsgColor = COLOR_DEFAULT

    @JvmOverloads
    fun setMsgColor(@ColorInt msgColor: Int = COLOR_TEXT_DEFAULT) {
        mMsgColor = msgColor
    }

    private var mBgResource = -1
    fun setBgResource(@DrawableRes bgResource: Int) {
        mBgResource = bgResource
    }

    @ColorInt
    val COLOR_BG_INFO: Int = Color.parseColor("#3F51B5")

    @ColorInt
    val COLOR_BG_SUCCESS: Int = Color.parseColor("#388E3C")

    @ColorInt
    val COLOR_BG_WARNING: Int = Color.parseColor("#FFA900")

    @ColorInt
    val COLOR_BG_ERROR: Int = Color.parseColor("#FD4C5B")
    private var mBgColor: Int = COLOR_DEFAULT

    @JvmOverloads
    fun setBgColor(@ColorInt bgColor: Int = COLOR_BG_INFO) {
        mBgColor = bgColor
    }

    fun showShort(@StringRes resIdFormat: Int, vararg args: Any): Boolean =
        show(resIdFormat, Toast.LENGTH_SHORT, *args)

    fun showLong(@StringRes resIdFormat: Int, vararg args: Any): Boolean =
        show(resIdFormat, Toast.LENGTH_LONG, *args)

    private fun show(@StringRes resIdFormat: Int, duration: Int, vararg args: Any): Boolean = try {
        show(
            String.format(Resources.getSystem().getText(resIdFormat).toString(), *args)
                    as CharSequence, duration
        )
    } catch (ignore: Exception) {
        show(resIdFormat.toString() as CharSequence, duration)
    }

    fun showShort(strFormat: String, vararg args: Any): Boolean =
        show(strFormat, Toast.LENGTH_SHORT, *args)

    fun showLong(strFormat: String, vararg args: Any): Boolean =
        show(strFormat, Toast.LENGTH_LONG, *args)

    private fun show(strFormat: String?, duration: Int, vararg args: Any): Boolean =
        show((strFormat?.let { String.format(it, *args) } ?: "") as CharSequence, duration)

    fun showShort(@StringRes resIdText: Int): Boolean = show(resIdText, Toast.LENGTH_SHORT)
    fun showLong(@StringRes resIdText: Int): Boolean = show(resIdText, Toast.LENGTH_LONG)
    private fun show(@StringRes resIdText: Int, duration: Int): Boolean = try {
        show(Resources.getSystem().getText(resIdText), duration)
    } catch (ignore: Exception) {
        show(resIdText.toString() as CharSequence, duration)
    }

    fun showShort(strText: CharSequence?): Boolean = show(strText ?: "", Toast.LENGTH_SHORT)
    fun showLong(strText: CharSequence?): Boolean = show(strText ?: "", Toast.LENGTH_LONG)
    private var iToast: IToast? = null
    private fun show(strText: CharSequence, duration: Int): Boolean = mainHandler.post {
        cancel()
        iToast = ToastFactory.toastMake(app, strText, duration).apply {
            if (mGravity != -1 || mXOffset != -1 || mYOffset != -1)
                setGravity(mGravity, mXOffset, mYOffset)
            view?.findViewById<TextView>(android.R.id.message)?.apply {
                if (mMsgTextSize != -1) textSize = mMsgTextSize.toFloat()
                if (mMsgColor != COLOR_DEFAULT) setTextColor(mMsgColor)
            }?.let { setBg(it) }
        }
        iToast?.show()
    }

    fun cancel() = iToast?.cancel()
    private fun setBg(tvMsg: TextView) = iToast?.view?.run {
        when {
            mBgResource != -1 -> {
                setBackgroundResource(mBgResource)
                tvMsg.setBackgroundColor(Color.TRANSPARENT)
            }
            mBgColor != COLOR_DEFAULT -> background.let { tvBg ->
                tvMsg.background.let { msgBg ->
                    when {
                        tvBg != null && msgBg != null -> {
                            tvBg.colorFilter =
                                PorterDuffColorFilter(mBgColor, PorterDuff.Mode.SRC_IN)
                            tvMsg.setBackgroundColor(Color.TRANSPARENT)
                        }
                        tvBg != null && msgBg == null -> tvBg.colorFilter =
                            PorterDuffColorFilter(mBgColor, PorterDuff.Mode.SRC_IN)
                        tvBg == null && msgBg != null -> msgBg.colorFilter =
                            PorterDuffColorFilter(mBgColor, PorterDuff.Mode.SRC_IN)
                        else -> setBackgroundColor(mBgColor)
                    }
                }
            }
        }
    }

    fun showCustomShort(@LayoutRes layoutId: Int): View =
        getView(layoutId).apply { show(this, Toast.LENGTH_SHORT) }

    fun showCustomLong(@LayoutRes layoutId: Int): View =
        getView(layoutId).apply { show(this, Toast.LENGTH_LONG) }

    private fun getView(@LayoutRes layoutId: Int): View = app.layoutInflater.inflate(layoutId, null)
    private fun show(layoutView: View, duration: Int): Boolean = mainHandler.post {
        cancel()
        iToast = ToastFactory.toastNew(app).apply {
            view = layoutView
            setDuration(duration)
            if (mGravity != -1 || mXOffset != -1 || mYOffset != -1)
                setGravity(mGravity, mXOffset, mYOffset)
            setBg()
        }
        iToast?.show()
    }

    private fun setBg() = iToast?.view?.run {
        when {
            mBgResource != -1 -> setBackgroundResource(mBgResource)
            mBgColor != COLOR_DEFAULT -> background?.let { tvBg ->
                tvBg.colorFilter = PorterDuffColorFilter(mBgColor, PorterDuff.Mode.SRC_IN)
            } ?: when {
                aboveJellyBean -> background = ColorDrawable(mBgColor)
                else -> ViewCompat.setBackground(this, ColorDrawable(mBgColor))
            }
        }
    }

    internal object ToastFactory {
        fun toastNew(context: Context): IToast = when {
            NotificationManagerCompat.from(context).areNotificationsEnabled() ->
                SystemToast(Toast(context))
            else -> ToastWithoutNotification(Toast(context))
        }

        fun toastMake(context: Context, text: CharSequence, duration: Int): IToast = when {
            NotificationManagerCompat.from(context).areNotificationsEnabled() ->
                SystemToast(toastMakeNormal(context, text, duration))
            else -> ToastWithoutNotification(toastMakeNormal(context, text, duration))
        }

        private fun toastMakeNormal(context: Context, text: CharSequence, duration: Int): Toast =
            Toast.makeText(context, text, duration)
    }

    internal class SystemToast(toast: Toast) : AbsToast(toast) {
        internal class SafeHandler(private val handler: Handler) : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) = handler.handleMessage(msg)
            override fun dispatchMessage(msg: Message) = try {
                handler.dispatchMessage(msg)
            } catch (e: Exception) {
                error("$loggerTag->$e")
            }
        }

        companion object {
            private var fieldTN: Field? = null
            private var fieldHandler: Field? = null
        }

        init {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1) try {
                fieldTN = Toast::class.java.getDeclaredField("mTN").apply { isAccessible = true }
                fieldHandler = fieldTN?.type?.getDeclaredField("mHandler")?.apply {
                    isAccessible = true
                    fieldTN?.get(toast).let { mTN -> set(mTN, SafeHandler(get(mTN) as Handler)) }
                }
            } catch (ignored: Exception) {
            }
        }

        override fun show() = toast?.show()
        override fun cancel() = toast?.cancel()
    }

    internal class ToastWithoutNotification(toast: Toast) : AbsToast(toast) {
        private var mView: View? = null
        private var windowManager: WindowManager? = null
        private val layoutParams = WindowManager.LayoutParams()

        companion object {
            private val listener: AKit.OnActivityDestroyedListener
                get() = object : AKit.OnActivityDestroyedListener {
                    override fun onActivityDestroyed(activity: Activity) = iToast?.cancel() ?: Unit
                }
        }

        override fun show() {
            toast?.view.apply { mView = this }?.let {
                it.context?.let { contextView ->
                    when {
                        Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1 -> topActivityOrApp.let { context ->
                            when {
                                context !is Activity -> {
                                    error("$loggerTag->Couldn't get top Activity.")
                                    return
                                }
                                context.isFinishing || context.isDestroyed -> {
                                    error("$loggerTag->$context is useless")
                                    return
                                }
                                else -> {
                                    windowManager = context.windowManager
                                    layoutParams.type =
                                        WindowManager.LayoutParams.LAST_APPLICATION_WINDOW
                                    activityLifecycle
                                        .addOnActivityDestroyedListener(context, listener)
                                }
                            }
                        }
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1 -> {
                            windowManager = contextView.windowManager
                            layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST
                        }
                        else -> {
                            windowManager = contextView.windowManager
                            layoutParams.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 37
                        }
                    }
                    layoutParams.apply {
                        title = "ToastWithoutNotification"
                        packageName = appPackageName
                        flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        format = PixelFormat.TRANSLUCENT
                        windowAnimations = android.R.style.Animation_Toast
                        x = toast?.xOffset ?: 0
                        y = toast?.yOffset ?: 0
                        width = WindowManager.LayoutParams.WRAP_CONTENT
                        height = WindowManager.LayoutParams.WRAP_CONTENT
                        this.gravity = toast?.gravity?.let { grv ->
                            when {
                                aboveJellyBeanMR1 -> Gravity.getAbsoluteGravity(
                                    grv, contextView.resources.configuration.layoutDirection
                                )
                                else -> grv
                            }
                        } ?: BottomCenter
                        if (gravity and Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.FILL_HORIZONTAL)
                            horizontalWeight = 1.0f
                        if (gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.FILL_VERTICAL)
                            verticalWeight = 1.0f
                    }
                }
                try {
                    windowManager?.addView(it, layoutParams)
                } catch (ignored: Exception) {
                }
                mainHandler.postDelayed(
                    { cancel() },
                    (if (toast?.duration == Toast.LENGTH_SHORT) 2000 else 3500).toLong()
                )
            }
        }

        override fun cancel() {
            try {
                windowManager?.removeViewImmediate(mView)
            } catch (ignored: Exception) {
            }
            mView = null
            windowManager = null
            toast = null
        }
    }

    internal abstract class AbsToast(var toast: Toast?) : IToast {
        override var view: View?
            get() = toast?.view
            set(layoutView) = toast.run { this?.view = layoutView }

        override fun setDuration(duration: Int) = toast?.run { this.duration = duration }
        override fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) =
            toast?.setGravity(gravity, xOffset, yOffset)

        override fun setText(resId: Int) = toast?.setText(resId)
        override fun setText(charSequence: CharSequence) = toast?.setText(charSequence)
    }

    internal interface IToast {
        var view: View?
        fun show(): Unit?
        fun cancel(): Unit?
        fun setDuration(duration: Int): Unit?
        fun setGravity(gravity: Int, xOffset: Int, yOffset: Int): Unit?
        fun setText(@StringRes resId: Int): Unit?
        fun setText(charSequence: CharSequence): Unit?
    }
}