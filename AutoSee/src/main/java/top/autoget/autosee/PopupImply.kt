package top.autoget.autosee

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView

class PopupImply @JvmOverloads constructor(
    private val context: Context?, imply: String? = "PopupImply",
    width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
) : PopupWindow() {
    private var imply: TextView? = null

    init {
        isFocusable = true
        isTouchable = true
        isOutsideTouchable = true
        setBackgroundDrawable(BitmapDrawable())
        setWidth(width)
        setHeight(height)
        contentView = LayoutInflater.from(context).inflate(R.layout.popup_imply, null)
        this.imply = contentView.findViewById(R.id.tv_imply)
        this.imply?.text = imply
    }

    fun show(view: View) = showAsDropDown(view)
}