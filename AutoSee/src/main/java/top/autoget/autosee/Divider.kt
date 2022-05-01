package top.autoget.autosee

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class Divider(
    private var space: Int = 0, private var isTop: Boolean = false,
    private var spaceBottom: Int = space, private var spaceTop: Int = space,
    private var spaceRight: Int = space, private var spaceLeft: Int = space
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.apply {
            when {
                parent.getChildLayoutPosition(view) == 0 && isTop -> {
                    bottom = spaceBottom
                    top = 0
                    right = 0
                    left = 0
                }
                else -> {
                    bottom = spaceTop
                    top = spaceBottom
                    right = spaceRight
                    left = spaceLeft
                }
            }
        }
    }
}