package top.autoget.autosee.recycle.item

import top.autoget.autosee.R
import top.autoget.autosee.recycle.binder.ItemBinderQuick

abstract class RulerBinderQuick : ItemBinderQuick<RulerBean>() {
    override fun getLayoutId(): Int = R.layout.binder_ruler
}