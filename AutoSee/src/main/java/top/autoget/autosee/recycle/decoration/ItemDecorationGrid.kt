package top.autoget.autosee.recycle.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class ItemDecorationGrid(
    private var dividerDrawable: Drawable?,
    private var orientation: Int = LinearLayoutManager.VERTICAL
) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        if (parent.getChildLayoutPosition(view) >= 1) dividerDrawable?.let {
            when (orientation) {
                LinearLayoutManager.VERTICAL -> outRect.top = it.intrinsicHeight
                LinearLayoutManager.HORIZONTAL -> outRect.left = it.intrinsicWidth
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        dividerDrawable?.apply {
            val rightV = parent.width
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val topH = child.top + params.topMargin
                val bottomH = child.bottom + params.bottomMargin
                val rightH = child.left - params.leftMargin
                val leftH = rightH - intrinsicWidth
                setBounds(leftH, topH, rightH, bottomH)
                draw(c)
                val leftV = parent.paddingLeft + child.paddingLeft
                val bottomV = child.top - params.topMargin
                val topV = bottomV - intrinsicHeight
                setBounds(leftV, topV, rightV, bottomV)
                draw(c)
            }
        }
    }
}