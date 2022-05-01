package top.autoget.autosee.flow.adapter

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView

abstract class AdapterTemplate<T>(layoutId: Int, data: MutableList<*>?) :
    AdapterFlow<T?>(layoutId, data) {
    fun addChildrenClick(view: View?, viewId: Int, position: Int): AdapterFlow<*> = apply {
        val child = view?.findViewById<View?>(viewId)
        child?.setOnClickListener { onItemChildClick(child, position) }
    }

    fun addChildrenLongClick(view: View?, viewId: Int, position: Int): AdapterFlow<*> = apply {
        val child = view?.findViewById<View?>(viewId)
        child?.setOnLongClickListener { onItemChildLongClick(child, position) }
    }

    fun setText(view: View?, viewId: Int, resId: Int): AdapterTemplate<*> =
        apply { view?.findViewById<TextView?>(viewId)?.setText(resId) }

    fun setText(view: View?, viewId: Int, msg: String?): AdapterTemplate<*> =
        apply { view?.findViewById<TextView?>(viewId)?.text = msg }

    fun setTextColor(view: View?, viewId: Int, textColor: Int): AdapterTemplate<*> =
        apply { view?.findViewById<TextView?>(viewId)?.setTextColor(textColor) }

    fun setImageView(view: View?, viewId: Int, res: Int): AdapterTemplate<*> =
        apply { view?.findViewById<ImageView?>(viewId)?.setImageResource(res) }

    fun setImageView(view: View?, viewId: Int, bitmap: Bitmap?): AdapterTemplate<*> =
        apply { view?.findViewById<ImageView?>(viewId)?.setImageBitmap(bitmap) }

    fun setVisible(view: View?, viewId: Int, isVisible: Boolean): AdapterTemplate<*> = apply {
        view?.findViewById<View?>(viewId)?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun setVisible(view: View?, viewId: Int, visible: Int): AdapterTemplate<*> =
        apply { view?.findViewById<View?>(viewId)?.visibility = visible }
}