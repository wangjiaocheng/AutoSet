package top.autoget.autosee.flow.adapter

open class FlowListenerAdapter {
    open fun notifyDataChanged() {}
    open fun resetAllStatus() {}
    open fun resetAllTextColor(viewId: Int, color: Int) {}
}