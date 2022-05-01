package top.autoget.autokit

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import top.autoget.autokit.AKit.app

object ColorKit {
    val randomColor: Int
        get() = getRandomColor(true)

    fun getRandomColor(isArgb: Boolean): Int =
        (if (isArgb) (Math.random() * 0x100).toInt() shl 24 else -0x1000000) or (Math.random() * 0x1000000).toInt()

    fun getColor(colorId: Int): Int = ContextCompat.getColor(app, colorId)
    fun setAlphaComponent(colorInt: Int, alpha: Int): Int =
        (colorInt and 0x00ffffff) or (alpha shl 24)

    fun setAlphaComponent(colorInt: Int, alpha: Float): Int =
        (colorInt and 0x00ffffff) or ((alpha * 255.0f + 0.5f).toInt() shl 24)

    fun setRedComponent(colorInt: Int, red: Int): Int = (colorInt and -0xff0001) or (red shl 16)
    fun setRedComponent(colorInt: Int, red: Float): Int =
        (colorInt and -0xff0001) or ((red * 255.0f + 0.5f).toInt() shl 16)

    fun setGreenComponent(colorInt: Int, green: Int): Int = (colorInt and -0xff01) or (green shl 8)
    fun setGreenComponent(colorInt: Int, green: Float): Int =
        (colorInt and -0xff01) or ((green * 255.0f + 0.5f).toInt() shl 8)

    fun setBlueComponent(colorInt: Int, blue: Int): Int = (colorInt and -0x100) or blue//0x0..0xFF
    fun setBlueComponent(colorInt: Int, blue: Float): Int =
        (colorInt and -0x100) or (blue * 255.0f + 0.5f).toInt()//0.0..1.0

    fun string2ColorInt(colorString: String): Int = Color.parseColor(colorString)

    @JvmOverloads
    fun int2ArgbOrRgbString(colorInt: Int, isArgb: Boolean = true): String {
        var color = Integer.toHexString(if (isArgb) colorInt else colorInt and 0x00ffffff)
        while (color.length < 6) {
            color = "0$color"
        }
        if (isArgb) while (color.length < 8) {
            color = "f$color"
        }
        return "#$color"
    }

    fun tintCursorDrawable(editText: EditText, colorInt: Int) {
        try {
            TextView::class.java.getDeclaredField("mCursorDrawableRes")
                .apply { isAccessible = true }.getInt(editText).let { cursorDrawableRes ->
                    if (cursorDrawableRes > 0) tintDrawable(
                        editText.context.resources.getDrawable(cursorDrawableRes),
                        ColorStateList.valueOf(colorInt)
                    ).let { drawable ->
                        TextView::class.java.getDeclaredField("mEditor")
                            .apply { isAccessible = true }.get(editText).let { editor ->
                                editor.javaClass.getDeclaredField("mCursorDrawable")
                                    .apply { isAccessible = true }
                                    .set(editor, arrayOf(drawable, drawable))
                            }
                    }
                }
        } catch (ignored: Throwable) {
        }
    }//@SuppressLint("SoonBlockedPrivateApi")

    fun tintDrawable(drawable: Drawable, colorStateList: ColorStateList): Drawable =
        DrawableCompat.wrap(drawable.mutate()).apply {
            DrawableCompat.setTintList(this, colorStateList)
        }
}//Color.parseColor(colorString)->colorString：“#RRGGBB、#AARRGGBB；red、blue、green、black、white、gray、cyan、magenta、yellow、lightgray、darkgray、grey、lightgrey、darkgrey、aqua、fuchsia、lime、maroon、navy、olive、purple、silver、teal”