package top.autoget.autosee.banner.listen

import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView

abstract class PageListener<T> {
    private val mDataList: MutableList<Any> = ArrayList()
    fun setDataList(dataList: MutableList<Any>) = mDataList.apply { clear() }.addAll(dataList)
    abstract fun bindView(view: View?, data: Any?, position: Int)
    open fun onItemClick(view: View?, data: Any, position: Int) {}
    fun onItemChildClick(childView: View?, data: T, position: Int) {}
    fun onItemChildLongClick(childView: View?, data: T, position: Int): Boolean = true
    fun addChildrenClick(view: View, viewId: Int, position: Int): PageListener<*> = apply {
        val child = view.findViewById<View>(viewId)
        child?.setOnClickListener { onItemChildClick(child, mDataList[position] as T, position) }
    }

    fun addChildrenLongClick(view: View, viewId: Int, position: Int): PageListener<*> =
        apply {
            val child = view.findViewById<View>(viewId)
            child?.setOnLongClickListener {
                onItemChildLongClick(child, mDataList[position] as T, position)
            }
        }

    fun setText(view: View, viewId: Int, resId: Int): PageListener<*> =
        apply { view.findViewById<TextView>(viewId)?.setText(resId) }

    fun setText(view: View, viewId: Int, msg: String?): PageListener<*> =
        apply { view.findViewById<TextView>(viewId)?.text = msg }

    fun setTextColor(view: View, viewId: Int, textColor: Int): PageListener<*> =
        apply { view.findViewById<TextView>(viewId)?.setTextColor(textColor) }

    fun setImageView(view: View, viewId: Int, res: Int): PageListener<*> =
        apply { view.findViewById<ImageView>(viewId)?.setImageResource(res) }

    fun setImageView(view: View, viewId: Int, bitmap: Bitmap?): PageListener<*> =
        apply { view.findViewById<ImageView>(viewId)?.setImageBitmap(bitmap) }

    fun setVisible(view: View, viewId: Int, isVisible: Boolean): PageListener<*> = apply {
        view.findViewById<View>(viewId)?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun setVisible(view: View, viewId: Int, visible: Int): PageListener<*> =
        this.apply { view.findViewById<View>(viewId)?.visibility = visible }
}