package top.autoget.autosee.recycle.listener

import android.view.View
import top.autoget.autosee.recycle.BaseAdapterQuick

interface OnItemChildClickListener {
    fun onItemChildClick(adapter: BaseAdapterQuick<*, *>, view: View, position: Int)
}