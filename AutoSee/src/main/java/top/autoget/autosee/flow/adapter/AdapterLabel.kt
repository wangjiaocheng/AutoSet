package top.autoget.autosee.flow.adapter

import android.view.View

abstract class AdapterLabel<T>(layoutId: Int, data: MutableList<T?>?) :
    AdapterTemplate<T?>(layoutId, data) {
    val resetStatus = flowListenerAdapter?.resetAllStatus()//恢复所有状态
    open fun onReachMaxCount(ids: MutableList<Int?>, count: Int) {}//达到最大值
    open fun onFocusChanged(oldView: View?, newView: View?) {}//上个焦点和当前焦点的焦点情况，方便自定义动画，或者其他属性
    open fun onShowMoreClick(view: View?) {}//显示更多
    open fun onHandUpClick(view: View?) {}//收起
}