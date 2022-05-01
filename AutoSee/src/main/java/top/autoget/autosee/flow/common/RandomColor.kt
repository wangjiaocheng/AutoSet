package top.autoget.autosee.flow.common

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import java.util.*

object RandomColor {
    val tabColors: IntArray = intArrayOf(
        Color.parseColor("#90C5F0"),
        Color.parseColor("#91CED5"),
        Color.parseColor("#F88F55"),
        Color.parseColor("#C0AFD0"),
        Color.parseColor("#E78F8F"),
        Color.parseColor("#67CCB7"),
        Color.parseColor("#F6BC7E"),
        Color.parseColor("#3399ff")
    )

    fun randomTagColor(): Int {
        var position = Random().nextInt() % tabColors.size
        if (position < 0) position = -position
        return tabColors[position]
    }

    fun getColorDrawable(radius: Int): Drawable = GradientDrawable().apply {
        setColor(randomTagColor())
        cornerRadius = radius.toFloat()
    }
}