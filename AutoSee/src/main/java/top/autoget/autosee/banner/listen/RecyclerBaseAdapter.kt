package top.autoget.autosee.banner.listen

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

internal abstract class RecyclerBaseAdapter<T>(
    private val mLayoutId: Int, private val mDataList: MutableList<T>
) : RecyclerView.Adapter<RecyclerViewHolder?>() {
    private var mViewHolder: RecyclerViewHolder? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder =
        RecyclerViewHolder.getViewHolder(parent.context, mLayoutId, parent)
            .apply { mViewHolder = this }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    var mOnItemClickListener: OnItemClickListener? = null
    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.holderView.setOnClickListener { mOnItemClickListener?.onItemClick(it, position) }
        getViewInHolder(holder, mDataList[position])
    }

    abstract fun getViewInHolder(holder: RecyclerViewHolder?, data: T)
    override fun getItemCount(): Int = mDataList.size
}