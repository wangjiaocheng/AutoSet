package top.autoget.autosee.recycle.listener

import android.view.View
import top.autoget.autosee.recycle.BaseAdapterQuick

interface OnItemLongClickListener {
    fun onItemLongClick(adapter: BaseAdapterQuick<*, *>, view: View, position: Int): Boolean
}