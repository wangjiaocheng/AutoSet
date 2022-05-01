package top.autoget.autosee.recycle.module

import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView

interface OnItemSwipeListener {
    fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder?, pos: Int)
    fun clearView(viewHolder: RecyclerView.ViewHolder?, pos: Int)
    fun onItemSwiped(viewHolder: RecyclerView.ViewHolder?, pos: Int)
    fun onItemSwipeMoving(
        canvas: Canvas?, viewHolder: RecyclerView.ViewHolder?,
        dX: Float, dY: Float, isCurrentlyActive: Boolean
    )
}