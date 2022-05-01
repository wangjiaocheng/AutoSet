package top.autoget.autosee.recycle.module

import androidx.recyclerview.widget.RecyclerView

interface OnItemDragListener {
    fun onItemDragStart(viewHolder: RecyclerView.ViewHolder?, pos: Int)
    fun onItemDragMoving(
        source: RecyclerView.ViewHolder?, from: Int, target: RecyclerView.ViewHolder?, to: Int
    )

    fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder?, pos: Int)
}