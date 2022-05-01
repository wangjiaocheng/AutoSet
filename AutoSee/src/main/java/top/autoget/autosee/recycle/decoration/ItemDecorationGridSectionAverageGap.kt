package top.autoget.autosee.recycle.decoration

import android.graphics.Rect
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import top.autoget.autosee.recycle.BaseAdapterSectionQuick

class ItemDecorationGridSectionAverageGap(
    private val gapHorizontalDp: Float = 10f, private val gapVerticalDp: Float = 10f,
    private val sectionEdgeHPaddingDp: Float = 20f, private val sectionEdgeVPaddingDp: Float = 15f
) : ItemDecoration() {
    private var mAdapter: BaseAdapterSectionQuick<*, *>? = null
    private var gapHSizePx = -1
    private var gapVSizePx = -1
    private var eachItemHPaddingPx = 0//每个条目应该在水平方向上加的padding总大小paddingLeft+paddingRight
    private var sectionEdgeHPaddingPx = 0
    private var sectionEdgeVPaddingPx = 0
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        when {
            parent.layoutManager is GridLayoutManager && parent.adapter is BaseAdapterSectionQuick<*, *> -> {
                val adapter: BaseAdapterSectionQuick<*, *> =
                    parent.adapter as BaseAdapterSectionQuick<*, *>
                if (mAdapter !== adapter) setUpWithAdapter(adapter)
                val position = parent.getChildAdapterPosition(view) -
                        (mAdapter?.headerLayoutCount ?: 0)
                val spanCount = (parent.layoutManager as GridLayoutManager).spanCount
                when {
                    adapter.getItem(position).isHeader -> outRect[0, 0, 0] = 0//不处理header
                    else -> {
                        if (gapHSizePx < 0 || gapVSizePx < 0)
                            transformGapDefinition(parent, spanCount)
                        val section = findSectionLastItemPos(position)
                        val visualPos = position + 1 - (section?.startPos ?: 0)
                        when {
                            visualPos % spanCount == 0 -> {
                                outRect.left = eachItemHPaddingPx - sectionEdgeHPaddingPx
                                outRect.right = sectionEdgeHPaddingPx
                            }//最后一列
                            visualPos % spanCount == 1 -> {
                                outRect.left = sectionEdgeHPaddingPx
                                outRect.right = eachItemHPaddingPx - sectionEdgeHPaddingPx
                            }//第一列
                            else -> {
                                outRect.left =
                                    gapHSizePx - (eachItemHPaddingPx - sectionEdgeHPaddingPx)
                                outRect.right = eachItemHPaddingPx - outRect.left
                            }
                        }
                        outRect.top = gapVSizePx
                        if (visualPos - spanCount <= 0) outRect.top = sectionEdgeVPaddingPx//第一行
                        outRect.bottom = 0
                        if (isLastRow(visualPos, spanCount, (section?.count ?: 0)))
                            outRect.bottom = sectionEdgeVPaddingPx//最后一行
                    }
                }
            }
            else -> super.getItemOffsets(outRect, view, parent, state)
        }
    }

    private val mDataObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() = markSections()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = markSections()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) =
            markSections()

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = markSections()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = markSections()
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
            markSections()
    }

    private inner class Section {
        var startPos = 0
        var endPos = 0
        val count: Int
            get() = endPos - startPos + 1

        operator fun contains(pos: Int): Boolean = pos in startPos..endPos
    }

    private val mSectionList: MutableList<Section?> = mutableListOf()
    private fun markSections() {
        mAdapter?.let {
            val adapter: BaseAdapterSectionQuick<*, *> = it
            mSectionList.clear()
            var section = Section()
            for (i in 0 until adapter.itemCount) {
                when {
                    adapter.getItem(i).isHeader -> {
                        if (i != 0) {
                            section.endPos = i - 1
                            mSectionList.add(section)//已经有待添加section
                        }//找到新Section起点
                        section = Section()
                        section.startPos = i + 1
                    }
                    else -> section.endPos = i
                }
            }
            if (section !in mSectionList) mSectionList.add(section)//处理末尾情况
        }
    }

    private fun setUpWithAdapter(adapter: BaseAdapterSectionQuick<*, *>?) {
        mAdapter?.unregisterAdapterDataObserver(mDataObserver)
        mAdapter = adapter
        mAdapter?.registerAdapterDataObserver(mDataObserver)
        markSections()
    }

    private fun findSectionLastItemPos(curPos: Int): Section? {
        for (section in mSectionList) {
            if (section?.contains(curPos) == true) return section
        }
        return null
    }

    private fun transformGapDefinition(parent: RecyclerView, spanCount: Int) {
        val displayMetrics = DisplayMetrics().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                parent.display.getMetrics(this)
        }
        gapHSizePx =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gapHorizontalDp, displayMetrics)
                .toInt()
        gapVSizePx =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gapVerticalDp, displayMetrics)
                .toInt()
        sectionEdgeHPaddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sectionEdgeHPaddingDp, displayMetrics
        ).toInt()
        sectionEdgeVPaddingPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sectionEdgeVPaddingDp, displayMetrics
        ).toInt()
        eachItemHPaddingPx = (sectionEdgeHPaddingPx * 2 + gapHSizePx * (spanCount - 1)) / spanCount
    }

    private fun isLastRow(visualPos: Int, spanCount: Int, sectionItemCount: Int): Boolean =
        visualPos > sectionItemCount - (sectionItemCount % spanCount).let { if (it == 0) spanCount else it }
}//应用于RecyclerView的GridLayoutManager，水平方向上固定间距大小使条目宽度自适应：配合Section使用，不对Head生效，仅对每个Head的子Grid列表生效；Section Grid中Item宽应设MATCH_PARAENT。