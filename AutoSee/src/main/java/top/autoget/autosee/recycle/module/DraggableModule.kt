package top.autoget.autosee.recycle.module

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLongClickListener
import android.view.View.OnTouchListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import top.autoget.autosee.R
import top.autoget.autosee.recycle.BaseAdapterQuick
import top.autoget.autosee.recycle.common.ViewHolderBase
import java.util.*

interface DraggableModule//需要拖拽BaseQuickAdapter继承
open class DraggableModuleBase(private val baseQuickAdapter: BaseAdapterQuick<*, *>) :
    DraggableListener {
    lateinit var itemTouchHelperCallback: DragAndSwipeCallback
    lateinit var itemTouchHelper: ItemTouchHelper

    init {
        initItemTouch()
    }

    private fun initItemTouch() {
        itemTouchHelperCallback = DragAndSwipeCallback(this)
        itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
    }

    fun attachToRecyclerView(recyclerView: RecyclerView) =
        itemTouchHelper.attachToRecyclerView(recyclerView)

    companion object {
        private const val NO_TOGGLE_VIEW = 0
    }

    var toggleViewId = NO_TOGGLE_VIEW
    open val hasToggleView: Boolean = toggleViewId != NO_TOGGLE_VIEW
    private var mOnToggleViewTouchListener: OnTouchListener? = null
    private var mOnToggleViewLongClickListener: OnLongClickListener? = null
    var isDragEnabled = false
    open var isDragOnLongPressEnabled = true
        set(isDragOnLongPressEnabled) {
            field = isDragOnLongPressEnabled
            when {
                field -> {
                    mOnToggleViewTouchListener = null
                    mOnToggleViewLongClickListener = OnLongClickListener { v ->
                        if (isDragEnabled) itemTouchHelper.startDrag(v.getTag(R.id.BaseAdapterQuick_viewholder_support) as RecyclerView.ViewHolder)
                        true
                    }
                }
                else -> {
                    mOnToggleViewTouchListener = OnTouchListener { v, event ->
                        if (event.action == MotionEvent.ACTION_DOWN && !isDragOnLongPressEnabled) {
                            if (isDragEnabled) itemTouchHelper.startDrag(v.getTag(R.id.BaseAdapterQuick_viewholder_support) as RecyclerView.ViewHolder)
                            true
                        } else false
                    }
                    mOnToggleViewLongClickListener = null
                }
            }
        }

    internal fun initView(holder: ViewHolderBase) {
        if (isDragEnabled && hasToggleView) {
            val toggleView = holder.itemView.findViewById<View>(toggleViewId)
            if (toggleView != null) {
                toggleView.setTag(R.id.BaseAdapterQuick_viewholder_support, holder)
                when {
                    isDragOnLongPressEnabled ->
                        toggleView.setOnLongClickListener(mOnToggleViewLongClickListener)
                    else -> toggleView.setOnTouchListener(mOnToggleViewTouchListener)
                }
            }
        }
    }

    private var mOnItemDragListener: OnItemDragListener? = null
    override fun setOnItemDragListener(onItemDragListener: OnItemDragListener?) {
        mOnItemDragListener = onItemDragListener
    }

    open fun onItemDragStart(viewHolder: RecyclerView.ViewHolder) =
        mOnItemDragListener?.onItemDragStart(viewHolder, getViewHolderPosition(viewHolder))

    open fun onItemDragMoving(source: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) {
        val from = getViewHolderPosition(source)
        val to = getViewHolderPosition(target)
        if (inRange(from) && inRange(to)) {
            when {
                from < to -> for (i in from until to) {
                    Collections.swap(baseQuickAdapter.data, i, i + 1)
                }
                else -> for (i in from downTo to + 1) {
                    Collections.swap(baseQuickAdapter.data, i, i - 1)
                }
            }
            baseQuickAdapter.notifyItemMoved(source.adapterPosition, target.adapterPosition)
        }
        mOnItemDragListener?.onItemDragMoving(source, from, target, to)
    }

    open fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder) =
        mOnItemDragListener?.onItemDragEnd(viewHolder, getViewHolderPosition(viewHolder))

    var isSwipeEnabled = false
    private var mOnItemSwipeListener: OnItemSwipeListener? = null
    override fun setOnItemSwipeListener(onItemSwipeListener: OnItemSwipeListener?) {
        mOnItemSwipeListener = onItemSwipeListener
    }

    open fun onItemSwipeStart(viewHolder: RecyclerView.ViewHolder) {
        if (isSwipeEnabled) mOnItemSwipeListener
            ?.onItemSwipeStart(viewHolder, getViewHolderPosition(viewHolder))
    }

    open fun onItemSwiping(
        canvas: Canvas?, viewHolder: RecyclerView.ViewHolder?,
        dX: Float, dY: Float, isCurrentlyActive: Boolean
    ) {
        if (isSwipeEnabled) mOnItemSwipeListener
            ?.onItemSwipeMoving(canvas, viewHolder, dX, dY, isCurrentlyActive)
    }

    open fun onItemSwiped(viewHolder: RecyclerView.ViewHolder) {
        val pos = getViewHolderPosition(viewHolder)
        if (inRange(pos)) {
            baseQuickAdapter.data.removeAt(pos)
            baseQuickAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            if (isSwipeEnabled) mOnItemSwipeListener?.onItemSwiped(viewHolder, pos)
        }
    }

    open fun onItemSwipeClear(viewHolder: RecyclerView.ViewHolder) {
        if (isSwipeEnabled) mOnItemSwipeListener
            ?.clearView(viewHolder, getViewHolderPosition(viewHolder))
    }

    private fun getViewHolderPosition(viewHolder: RecyclerView.ViewHolder): Int =
        viewHolder.adapterPosition - baseQuickAdapter.headerLayoutCount

    private fun inRange(position: Int): Boolean =
        position >= 0 && position < baseQuickAdapter.data.size
}