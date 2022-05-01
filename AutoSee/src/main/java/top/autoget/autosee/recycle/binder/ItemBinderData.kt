package top.autoget.autosee.recycle.binder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import top.autoget.autosee.recycle.common.ViewHolderBase

abstract class ItemBinderData<T, DB : ViewDataBinding> :
    ItemBinderBase<T, ItemBinderData.BinderDataBindingHolder<DB>>() {
    class BinderDataBindingHolder<DB : ViewDataBinding>(val dataBinding: DB) :
        ViewHolderBase(dataBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BinderDataBindingHolder<DB> =
        BinderDataBindingHolder(
            onCreateDataBinding(LayoutInflater.from(parent.context), parent, viewType)
        )

    abstract fun onCreateDataBinding(
        layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int
    ): DB
}//使用DataBinding快速构建Binder，仅适用BaseAdapterBinder；BinderDataBindingHolder中val不能少