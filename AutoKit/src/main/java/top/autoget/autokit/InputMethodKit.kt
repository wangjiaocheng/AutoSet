package top.autoget.autokit

import android.app.Activity
import android.graphics.Rect
import android.os.Build.VERSION_CODES.JELLY_BEAN
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ScreenKit.navigationBarHeight
import top.autoget.autokit.ScreenKit.statusBarHeight
import java.util.*
import kotlin.math.abs

object InputMethodKit : LoggerKit {
    fun showInputMethod(activity: Activity) =
        showInputMethod(activity.currentFocus ?: View(activity))

    fun showInputMethod(view: View) = view.apply {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }.let {
        app.inputMethodManager.showSoftInput(it, InputMethodManager.SHOW_FORCED,
            object : ResultReceiver(Handler(Looper.getMainLooper())) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                    if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN || resultCode == InputMethodManager.RESULT_HIDDEN)
                        toggleInputMethod()
                }
            })
    }//manifest.xml中activity设置android:windowSoftInputMode="stateVisible|adjustResize"

    fun toggleInputMethod() = app.inputMethodManager
        .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS)

    fun showInputMethod(editText: EditText) = editText.apply {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }.let { view ->
        view.context.inputMethodManager
            .showSoftInput(view, InputMethodManager.SHOW_FORCED,
                object : ResultReceiver(Handler(Looper.getMainLooper())) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                        if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN || resultCode == InputMethodManager.RESULT_HIDDEN)
                            toggleInputMethod(view)
                    }
                })
    }

    fun toggleInputMethod(editText: EditText) = editText.apply {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }.context.inputMethodManager
        .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS)

    fun hideInputMethod(activity: Activity) =
        hideInputMethod(activity.currentFocus ?: View(activity))

    fun hideInputMethod(view: View) = app.inputMethodManager.run {
        if (isActive) hideSoftInputFromWindow(view.windowToken, 0,
            object : ResultReceiver(Handler(Looper.getMainLooper())) {
                override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                    if (resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN || resultCode == InputMethodManager.RESULT_SHOWN)
                        toggleInputMethod()
                }
            })//activity.window.currentFocus|peekDecorView().windowToken
    }

    fun hideInputMethod(editText: EditText) = editText.apply { clearFocus() }.let { view ->
        view.context.inputMethodManager.run {
            if (isActive) hideSoftInputFromWindow(view.windowToken, 0,
                object : ResultReceiver(Handler(Looper.getMainLooper())) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                        if (resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN || resultCode == InputMethodManager.RESULT_SHOWN)
                            toggleInputMethod(view)
                    }
                })
        }
    }

    fun hideInputMethodTimer(activity: Activity) =
        hideInputMethodTimer(activity.currentFocus ?: View(activity))

    fun hideInputMethodTimer(view: View) = Timer().schedule(object : TimerTask() {
        override fun run() {
            view.context.inputMethodManager.run {
                if (isActive) hideSoftInputFromWindow(view.applicationWindowToken, 0,
                    object : ResultReceiver(Handler(Looper.getMainLooper())) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                            if (resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN || resultCode == InputMethodManager.RESULT_SHOWN)
                                toggleInputMethod()
                        }
                    })
            }
        }
    }, 10)

    fun hideInputMethodTimer(editText: EditText) = Timer().schedule(object : TimerTask() {
        override fun run() {
            editText.apply { clearFocus() }.let { view ->
                view.context.inputMethodManager.run {
                    if (isActive) hideSoftInputFromWindow(view.applicationWindowToken, 0,
                        object : ResultReceiver(Handler(Looper.getMainLooper())) {
                            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                                if (resultCode == InputMethodManager.RESULT_UNCHANGED_SHOWN || resultCode == InputMethodManager.RESULT_SHOWN)
                                    toggleInputMethod(view)
                            }
                        })
                }
            }
        }
    }, 10)

    var isInputMethodActive: Boolean = app.inputMethodManager.isActive
    fun isInputMethodVisible(activity: Activity?): Boolean =
        getDecorViewInvisibleHeight(activity) > 0

    private var decorViewDelta = 0
    private fun getDecorViewInvisibleHeight(activity: Activity?): Int =
        activity?.window?.decorView?.let { view ->
            Rect().apply { view.getWindowVisibleDisplayFrame(this) }.let { rect ->
                debug("$loggerTag->getDecorViewInvisibleHeight: ${(view.bottom - rect.bottom)}")
                abs(view.bottom - rect.bottom).let { delta ->
                    when {
                        delta > navigationBarHeight -> delta - decorViewDelta
                        else -> {
                            decorViewDelta = delta
                            0
                        }
                    }
                }
            }
        } ?: decorViewInvisibleHeightPre

    interface OnSoftInputChangedListener {
        fun onSoftInputChanged(height: Int)
    }

    private var decorViewInvisibleHeightPre: Int = 0
    private var onSoftInputChangedListener: OnSoftInputChangedListener? = null
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    fun registerSoftInputChangedListener(activity: Activity, listener: OnSoftInputChangedListener) {
        if (activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS != 0)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        decorViewInvisibleHeightPre = getDecorViewInvisibleHeight(activity)
        onSoftInputChangedListener = listener
        onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            onSoftInputChangedListener?.run {
                getDecorViewInvisibleHeight(activity).let {
                    if (decorViewInvisibleHeightPre != it) {
                        onSoftInputChanged(it)
                        decorViewInvisibleHeightPre = it
                    }
                }
            }
        }
        activity.findViewById<FrameLayout>(android.R.id.content).viewTreeObserver
            .addOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    @RequiresApi(JELLY_BEAN)
    fun unregisterSoftInputChangedListener(activity: Activity) {
        activity.findViewById<View>(android.R.id.content).viewTreeObserver
            .removeOnGlobalLayoutListener(onGlobalLayoutListener)
        onGlobalLayoutListener = null
        onSoftInputChangedListener = null
    }

    fun fixSoftInputLeaks(activity: Activity?) = activity?.let {
        for (leak in arrayOf("mLastSrvView", "mCurRootView", "mServedView", "mNextServedView")) {
            try {
                InputMethodManager::class.java.getDeclaredField(leak)
                    .apply { if (!isAccessible) isAccessible = true }.let { declaredField ->
                        (declaredField.get(app.inputMethodManager) as View).run {
                            if (rootView === activity.window.decorView.rootView)
                                declaredField.set(app.inputMethodManager, null)
                        }
                    }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    private var contentViewInvisibleHeightPre5497: Int = 0
    fun fixAndroidBug5497(activity: Activity) {
        activity.window.run { setSoftInputMode(attributes.softInputMode and WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) }
        contentViewInvisibleHeightPre5497 = getContentViewInvisibleHeight(activity)
        activity.findViewById<FrameLayout>(android.R.id.content).let { contentView ->
            contentView.viewTreeObserver.addOnGlobalLayoutListener {
                getContentViewInvisibleHeight(activity).let {
                    if (contentViewInvisibleHeightPre5497 != it) {
                        contentView.getChildAt(0).run {
                            setPadding(
                                paddingLeft, paddingTop, paddingRight,
                                paddingBottom + getDecorViewInvisibleHeight(activity)
                            )
                        }
                        contentViewInvisibleHeightPre5497 = it
                    }
                }
            }
        }
    }

    private fun getContentViewInvisibleHeight(activity: Activity): Int =
        activity.findViewById<View>(android.R.id.content)?.let { contentView ->
            Rect().apply { contentView.getWindowVisibleDisplayFrame(this) }.let { rect ->
                debug("$loggerTag->getContentViewInvisibleHeight: ${(contentView.bottom - rect.bottom)}")
                abs(contentView.bottom - rect.bottom)
                    .let { delta -> if (delta > statusBarHeight + navigationBarHeight) delta else 0 }
            }
        } ?: contentViewInvisibleHeightPre5497

    fun clickBlankArea2HideSoftInput() {
        info("$loggerTag->Please refer to the following code.")
/*        override onTouchEvent (motionEvent:MotionEvent):Boolean{
            currentFocus?.run { return inputMethodManager.hideSoftInputFromWindow(windowToken, 0) }
            return super.onTouchEvent(motionEvent)
        }
        override dispatchTouchEvent (motionEvent:MotionEvent):Boolean {
            if (motionEvent.action == MotionEvent.ACTION_DOWN) currentFocus.let {
                if (isShouldHideKeyboard(it, motionEvent)) inputMethodManager
                    .hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
            return super.dispatchTouchEvent(motionEvent)
        }
        private isShouldHideKeyboard (view:View, motionEvent: MotionEvent):Boolean = view?.run{
            when {
                this is EditText -> arrayOf(0, 0).apply { getLocationInWindow(this) }.let {
                    !(motionEvent.getX() > it[0] && motionEvent.getX() < it[0] + view.width &&
                            motionEvent.getY() > it[1] && motionEvent.getY() < it[1] + view.height)
                }
                else -> false
            }
        } ?: false*/
    }
}