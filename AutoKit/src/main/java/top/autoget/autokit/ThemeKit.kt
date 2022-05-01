package top.autoget.autokit

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ArrayRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import top.autoget.autokit.ImageKit.setColorAlphaByInt
import top.autoget.autokit.VersionKit.aboveMarshmallow
import kotlin.math.roundToInt

object ThemeKit {
    @ColorInt
    @JvmOverloads
    fun resolveColor(context: Context, @AttrRes attr: Int, defValue: Int = 0): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attr))
        return try {
            typedArray.getColor(0, defValue)
        } finally {
            typedArray.recycle()
        }
    }//解析主题Color属性

    @JvmOverloads
    fun resolveDimension(context: Context, @AttrRes attr: Int, defValue: Int = -1): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attr))
        return try {
            typedArray.getDimensionPixelSize(0, defValue)
        } finally {
            typedArray.recycle()
        }
    }//解析主题Dimension属性

    @JvmOverloads
    fun resolveBoolean(context: Context, @AttrRes attr: Int, defValue: Boolean = false): Boolean {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attr))
        return try {
            typedArray.getBoolean(0, defValue)
        } finally {
            typedArray.recycle()
        }
    }//解析主题Boolean属性

    @JvmOverloads
    fun resolveDrawable(
        context: Context, @AttrRes attr: Int, defValue: Drawable? = null
    ): Drawable? {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(attr))
        return try {
            var drawable = typedArray.getDrawable(0)
            if (drawable == null && defValue != null) drawable = defValue
            drawable
        } finally {
            typedArray.recycle()
        }
    }//解析主题Drawable属性

    fun resolveString(context: Context, @AttrRes attr: Int): String =
        resolveString(context.theme, attr)

    fun resolveString(theme: Theme, @AttrRes attr: Int): String = TypedValue().apply {
        theme.resolveAttribute(attr, this, true)
    }.string as String//解析主题String属性

    fun resolveFloat(context: Context, attrRes: Int): Float =
        TypedValue().apply { context.theme.resolveAttribute(attrRes, this, true) }.float

    fun resolveFloat(context: Context, attrRes: Int, defaultValue: Float = 0f): Float {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrRes))
        return try {
            typedArray.getFloat(0, defaultValue)
        } finally {
            typedArray.recycle()
        }
    }

    @JvmOverloads
    fun resolveInt(context: Context, attrRes: Int, defaultValue: Int = 0): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrRes))
        return try {
            typedArray.getInt(0, defaultValue)
        } finally {
            typedArray.recycle()
        }
    }

    fun getColorFromAttrRes(context: Context, attrRes: Int, defaultValue: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrRes))
        return try {
            typedArray.getColor(0, defaultValue)
        } finally {
            typedArray.recycle()
        }
    }

    fun resolveActionTextColorStateList(
        context: Context, @AttrRes colorAttr: Int, defValue: ColorStateList?
    ): ColorStateList? {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(colorAttr))
        return try {
            typedArray.peekValue(0)?.let { value ->
                when (value.type) {
                    in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT ->
                        getActionTextStateList(context, value.data)
                    else -> typedArray.getColorStateList(0) ?: defValue
                }
            } ?: defValue
        } finally {
            typedArray.recycle()
        }
    }

    fun getActionTextColorStateList(context: Context, @ColorRes colorId: Int): ColorStateList =
        TypedValue().apply { context.resources.getValue(colorId, this, true) }.let { value ->
            return when (value.type) {
                in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT ->
                    getActionTextStateList(context, value.data)
                else -> when {
                    aboveMarshmallow -> context.getColorStateList(colorId)
                    else -> context.resources.getColorStateList(colorId)
                }
            }
        }

    fun getActionTextStateList(context: Context, newPrimaryColor: Int): ColorStateList {
        val states = arrayOf<IntArray?>(intArrayOf(-R.attr.state_enabled), intArrayOf())
        val colors = when (newPrimaryColor) {
            0 -> resolveColor(context, R.attr.textColorPrimary)
            else -> newPrimaryColor
        }.let { intArrayOf(setColorAlphaByInt(it, (Color.alpha(it) * 0.4f).roundToInt()), it) }
        return ColorStateList(states, colors)
    }

    fun getColorArray(context: Context, @ArrayRes array: Int): IntArray? = when (array) {
        0 -> null
        else -> context.resources.obtainTypedArray(array).let { typedArray ->
            val size = typedArray.length()
            IntArray(size).apply {
                for (i in 0 until size) {
                    this[i] = typedArray.getColor(i, 0)
                }
                typedArray.recycle()
            }
        }
    }

    val isNightMode: Boolean =
        AKit.app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES//当前是否是处于深色模式
}