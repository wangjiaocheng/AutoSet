package top.autoget.autosee.flow.adapter

import android.view.View

abstract class AdapterFlow<T>(mLayoutId: Int, mDataList: MutableList<*>?) {
    val layoutId: Int = mLayoutId//获取id
    val dataList: MutableList<*>? = mDataList//获取数据
    val itemCount: Int = mDataList?.size ?: 0//获取个数
    var flowListenerAdapter: FlowListenerAdapter? = null
    val notifyDataChanged = flowListenerAdapter?.notifyDataChanged()//通知数据改变
    abstract fun bindView(view: View?, data: Any?, position: Int)
    open fun onItemClick(view: View?, data: Any?, position: Int) {}//单击
    open fun onItemLongClick(view: View?, position: Int): Boolean = true//长按
    open fun onItemChildClick(childView: View?, position: Int) {}//子控件单击
    open fun onItemChildLongClick(childView: View?, position: Int): Boolean = true//子控件长按
    open fun onItemSelectState(view: View?, isSelected: Boolean) {}//view是否选中状态
}