package top.autoget.autosee.banner.listen

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

internal class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val mViewSparseArray: SparseArray<View?> = SparseArray()
    val holderView: View = itemView

    fun getItemView(resId: Int): View? {
        var view = mViewSparseArray[resId]
        if (view == null) {
            view = holderView.findViewById(resId)
            mViewSparseArray.put(resId, view)
        }
        return view
    }

    fun setText(viewId: Int, data: String?): RecyclerViewHolder =
        this.apply { (getItemView(viewId) as TextView).text = data }

    fun setDrawable(viewId: Int, resId: Int): RecyclerViewHolder =
        this.apply { (getItemView(viewId) as ImageView).setBackgroundResource(resId) }

    fun onItemClickListener(v: View?, position: Int) {}

    companion object {
        fun getViewHolder(
            context: Context?, layoutId: Int, parent: ViewGroup?
        ): RecyclerViewHolder =
            RecyclerViewHolder(LayoutInflater.from(context).inflate(layoutId, parent, false))
    }//相当于在onCreateViewHolder中设置好数据返回，在这里设置宽度可以动态居中
}