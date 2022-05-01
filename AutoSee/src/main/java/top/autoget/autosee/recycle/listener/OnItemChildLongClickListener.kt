package top.autoget.autosee.recycle.listener

import android.view.View
import top.autoget.autosee.recycle.BaseAdapterQuick

interface OnItemChildLongClickListener {
    fun onItemChildLongClick(adapter: BaseAdapterQuick<*, *>, view: View, position: Int): Boolean
}