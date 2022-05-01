package top.autoget.autosee.recycle.binder

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import top.autoget.autosee.recycle.common.ViewHolderBase
import top.autoget.autosee.recycle.common.getItemView

abstract class ItemBinderQuick<T> : ItemBinderBase<T, ViewHolderBase>() {
    @LayoutRes
    abstract fun getLayoutId(): Int
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase =
        ViewHolderBase(parent.getItemView(getLayoutId()))
}//使用布局ID快速构建Binder