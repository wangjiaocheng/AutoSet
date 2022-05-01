package top.autoget.autosee.recycle.common

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

open class DataBindingHolderBase<BD : ViewDataBinding>(view: View) : ViewHolderBase(view) {
    val dataBinding = DataBindingUtil.bind<BD>(view)
}//方便DataBinding使用