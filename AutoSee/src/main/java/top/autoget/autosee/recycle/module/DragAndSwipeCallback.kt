package top.autoget.autosee.recycle.module

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import top.autoget.autosee.R
import top.autoget.autosee.recycle.BaseAdapterQuick

class DragAndSwipeCallback(private val mDraggableModule: DraggableModuleBase?) :
    ItemTouchHelper.Callback() {
    private var mMoveThreshold = 0.1f
    fun setMoveThreshold(moveThreshold: Float) = apply { mMoveThreshold = moveThreshold }
    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float = mMoveThreshold
    private var mSwipeThreshold = 0.7f
    fun setSwipeThreshold(swipeThreshold: Float) = apply { mSwipeThreshold = swipeThreshold }
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = mSwipeThreshold
    private var mDragMoveFlags =
        ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT

    fun setDragMoveFlags(dragMoveFlags: Int) = apply { mDragMoveFlags = dragMoveFlags }
    private var mSwipeMoveFlags = ItemTouchHelper.END
    fun setSwipeMoveFlags(swipeMoveFlags: Int) = apply { mSwipeMoveFlags = swipeMoveFlags }
    override fun getMovementFlags(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ): Int =
        when {
            isViewCreateByAdapter(viewHolder) -> makeMovementFlags(0, 0)
            else -> makeMovementFlags(mDragMoveFlags, mSwipeMoveFlags)
        }

    private fun isViewCreateByAdapter(viewHolder: RecyclerView.ViewHolder): Boolean =
        viewHolder.itemViewType.let {
            it == BaseAdapterQuick.HEADER_VIEW || it == BaseAdapterQuick.LOAD_MORE_VIEW ||
                    it == BaseAdapterQuick.FOOTER_VIEW || it == BaseAdapterQuick.EMPTY_VIEW
        }

    override fun isLongPressDragEnabled(): Boolean =
        mDraggableModule?.run { isDragEnabled && !hasToggleView } ?: false

    override fun isItemViewSwipeEnabled(): Boolean = mDraggableModule?.isSwipeEnabled ?: false
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (!isViewCreateByAdapter(viewHolder)) viewHolder.itemView.run {
            if (getTag(R.id.BaseAdapterQuick_dragging_support) as Boolean) {
                mDraggableModule?.onItemDragEnd(viewHolder)
                setTag(R.id.BaseAdapterQuick_dragging_support, false)
            }
            if (getTag(R.id.BaseAdapterQuick_swiping_support) as Boolean) {
                mDraggableModule?.onItemSwipeClear(viewHolder)
                setTag(R.id.BaseAdapterQuick_swiping_support, false)
            }
        }
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            when {
                actionState == ItemTouchHelper.ACTION_STATE_DRAG && !isViewCreateByAdapter(it) -> {
                    mDraggableModule?.onItemDragStart(it)
                    it.itemView.setTag(R.id.BaseAdapterQuick_dragging_support, true)
                }
                actionState == ItemTouchHelper.ACTION_STATE_SWIPE && !isViewCreateByAdapter(it) -> {
                    mDraggableModule?.onItemSwipeStart(it)
                    it.itemView.setTag(R.id.BaseAdapterQuick_swiping_support, true)
                }
            }
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun onChildDrawOver(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && !isViewCreateByAdapter(viewHolder)) {
            c.save()
            viewHolder.itemView.run {
                when {
                    dX > 0 -> {
                        c.clipRect(left.toFloat(), top.toFloat(), left + dX, bottom.toFloat())
                        c.translate(left.toFloat(), top.toFloat())
                    }
                    else -> {
                        c.clipRect(right + dX, top.toFloat(), right.toFloat(), bottom.toFloat())
                        c.translate(right + dX, top.toFloat())
                    }
                }
            }
            mDraggableModule?.onItemSwiping(c, viewHolder, dX, dY, isCurrentlyActive)
            c.restore()
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (!isViewCreateByAdapter(viewHolder)) mDraggableModule?.onItemSwiped(viewHolder)
    }

    override fun onMove(
        recyclerView: RecyclerView, source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
    ): Boolean = source.itemViewType == target.itemViewType

    override fun onMoved(
        recyclerView: RecyclerView, source: RecyclerView.ViewHolder,
        fromPos: Int, target: RecyclerView.ViewHolder, toPos: Int, x: Int, y: Int
    ) {
        super.onMoved(recyclerView, source, fromPos, target, toPos, x, y)
        mDraggableModule?.onItemDragMoving(source, target)
    }
}