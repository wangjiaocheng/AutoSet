package top.autoget.autosee.recycle.listener

import android.view.View
import top.autoget.autosee.recycle.BaseAdapterQuick

interface OnItemClickListener {
    fun onItemClick(adapter: BaseAdapterQuick<*, *>, view: View, position: Int)
}