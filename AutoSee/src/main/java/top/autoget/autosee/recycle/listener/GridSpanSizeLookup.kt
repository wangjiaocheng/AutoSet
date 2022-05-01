package top.autoget.autosee.recycle.listener

import androidx.recyclerview.widget.GridLayoutManager

interface GridSpanSizeLookup {
    fun getSpanSize(gridLayoutManager: GridLayoutManager, viewType: Int, position: Int): Int
}