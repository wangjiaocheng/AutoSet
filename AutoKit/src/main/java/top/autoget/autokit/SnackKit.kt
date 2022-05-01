package top.autoget.autokit

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference

object SnackKit {
    @JvmOverloads
    fun colorSnackbar(
        view: View, message: String,
        @ColorInt backgroundColor: Int = Color.BLACK, @ColorInt messageColor: Int = Color.WHITE,
        durationLength: Int = BaseTransientBottomBar.LENGTH_LONG,
        bottomMargin: Int = 0, @DrawableRes backgroundResource: Int = 0,
        text: CharSequence = "", @ColorInt actionColor: Int = Color.WHITE,
        listener: View.OnClickListener? = null
    ): Snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).apply {
        duration = durationLength
        setColor(this, backgroundColor, messageColor)
        if (bottomMargin != 0) setBottomMargin(this, bottomMargin)
        if (backgroundResource != 0) setBackgroundResource(this, backgroundResource)
        listener?.let { if (text != "") setAction(this, text, actionColor, it) }
    }.let { WeakReference(it).get()!! }

    @JvmOverloads
    fun typeSnackbar(
        view: View, message: String, type: Int = OTHER_MESSAGE,
        durationLength: Int = BaseTransientBottomBar.LENGTH_LONG,
        bottomMargin: Int = 0, @DrawableRes backgroundResource: Int = 0,
        text: CharSequence = "", @ColorInt actionColor: Int = Color.WHITE,
        listener: View.OnClickListener? = null
    ): Snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).apply {
        duration = durationLength
        switchType(this, type)
        if (bottomMargin != 0) setBottomMargin(this, bottomMargin)
        if (backgroundResource != 0) setBackgroundResource(this, backgroundResource)
        listener?.let { if (text != "") setAction(this, text, actionColor, it) }
    }.let { WeakReference(it).get()!! }

    private const val DEFAULT_INFO = 1
    private const val SUCCESS_CONFIRM = 2
    private const val WARNING = 3
    private const val ERROR_ALERT = 4
    private const val OTHER_MESSAGE = 5
    var DEFAULT_INFO_BLUE = -0xDE6A0D//-0x1000001
    var SUCCESS_CONFIRM_GREEN = -0xB350B0//-0xD44A00
    var WARNING_ORANGE = -0x3EF9//-0x3F00
    var ERROR_ALERT_RED = -0xBBCCA//-0x10000
    var ERROR_ALERT_YELLOW = Color.YELLOW//-0x1

    @JvmOverloads
    fun switchType(snackBar: Snackbar, type: Int = OTHER_MESSAGE) = when (type) {
        DEFAULT_INFO -> setColor(snackBar, DEFAULT_INFO_BLUE)
        SUCCESS_CONFIRM -> setColor(snackBar, SUCCESS_CONFIRM_GREEN)
        WARNING -> setColor(snackBar, WARNING_ORANGE)
        ERROR_ALERT -> setColor(snackBar, ERROR_ALERT_RED, ERROR_ALERT_YELLOW)
        OTHER_MESSAGE -> setColor(snackBar)
        else -> setColor(snackBar)
    }

    @JvmOverloads
    fun setColor(
        snackBar: Snackbar,
        @ColorInt backgroundColor: Int = Color.BLACK, @ColorInt messageColor: Int = Color.WHITE
    ) = snackBar.view.apply { setBackgroundColor(backgroundColor) }
        .let { (it.findViewById<View>(R.id.snackbar_text) as TextView).setTextColor(messageColor) }

    fun setBottomMargin(snackBar: Snackbar, marginBottom: Int = 0) =
        (snackBar.view.layoutParams as ViewGroup.MarginLayoutParams)
            .apply { bottomMargin = marginBottom }

    fun setBackgroundResource(snackBar: Snackbar, @DrawableRes backgroundResource: Int) =
        snackBar.view.setBackgroundResource(backgroundResource)

    fun setAction(
        snackBar: Snackbar, text: CharSequence, @ColorInt actionColor: Int,
        listener: View.OnClickListener
    ): Snackbar = snackBar.setAction(text, listener).setActionTextColor(actionColor)

    fun getView(snackBar: Snackbar): View = snackBar.view
    fun addView(snackBar: Snackbar, child: View) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { gravity = Gravity.CENTER_VERTICAL }.let { layoutParams ->
        snackBar.view.apply { setPadding(0, 0, 0, 0) }
            .let { (it as Snackbar.SnackbarLayout).addView(child, layoutParams) }
    }

    fun addView(snackBar: Snackbar, @LayoutRes layoutId: Int, index: Int = -1) =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.CENTER_VERTICAL }.let { layoutParams ->
            snackBar.view.apply { setPadding(0, 0, 0, 0) }.let {
                (it as Snackbar.SnackbarLayout).addView(
                    LayoutInflater.from(it.context).inflate(layoutId, null), index, layoutParams
                )
            }
        }
}