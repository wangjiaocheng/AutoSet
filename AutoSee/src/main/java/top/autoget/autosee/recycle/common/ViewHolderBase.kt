package top.autoget.autosee.recycle.common

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

open class ViewHolderBase(view: View) : RecyclerView.ViewHolder(view) {
    private val views: SparseArray<View> = SparseArray()

    @Deprecated(
        "Please use BaseDataBindingHolder class",
        ReplaceWith("DataBindingUtil.getBinding(itemView)", "androidx.databinding.DataBindingUtil")
    )
    open fun <B : ViewDataBinding> getBinding(): B? =
        DataBindingUtil.getBinding(itemView)//如果使用DataBinding绑定View，可获取ViewDataBinding

    open fun <T : View> getView(@IdRes viewId: Int): T =
        checkNotNull(getViewOrNull(viewId)) { "No view found with id $viewId" }

    open fun <T : View> getViewOrNull(@IdRes viewId: Int): T? = (views.get(viewId) as T?)
        ?: itemView.findViewById<T>(viewId)?.apply { views.put(viewId, this) }

    open fun <T : View> Int.findView(): T? = itemView.findViewById(this)
    open fun setText(@IdRes viewId: Int, value: CharSequence?): ViewHolderBase =
        apply { getView<TextView>(viewId).text = value }

    open fun setTextRes(@IdRes viewId: Int, @StringRes strId: Int): ViewHolderBase? =
        apply { getView<TextView>(viewId).setText(strId) }

    open fun setTextColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHolderBase =
        apply { getView<TextView>(viewId).setTextColor(color) }

    open fun setTextColorRes(@IdRes viewId: Int, @ColorRes colorRes: Int): ViewHolderBase =
        apply { getView<TextView>(viewId).setTextColor(itemView.resources.getColor(colorRes)) }

    open fun setImageResource(@IdRes viewId: Int, @DrawableRes imageResId: Int): ViewHolderBase =
        apply { getView<ImageView>(viewId).setImageResource(imageResId) }

    open fun setImageDrawable(@IdRes viewId: Int, drawable: Drawable?): ViewHolderBase =
        apply { getView<ImageView>(viewId).setImageDrawable(drawable) }

    open fun setImageBitmap(@IdRes viewId: Int, bitmap: Bitmap?): ViewHolderBase =
        apply { getView<ImageView>(viewId).setImageBitmap(bitmap) }

    open fun setBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int): ViewHolderBase =
        apply { getView<View>(viewId).setBackgroundColor(color) }

    open fun setBackgroundResource(
        @IdRes viewId: Int,
        @DrawableRes backgroundRes: Int
    ): ViewHolderBase =
        apply { getView<View>(viewId).setBackgroundResource(backgroundRes) }

    open fun setVisible(@IdRes viewId: Int, isVisible: Boolean): ViewHolderBase =
        apply { getView<View>(viewId).visibility = if (isVisible) View.VISIBLE else View.INVISIBLE }

    open fun setGone(@IdRes viewId: Int, isGone: Boolean): ViewHolderBase =
        apply { getView<View>(viewId).visibility = if (isGone) View.GONE else View.VISIBLE }

    open fun setEnabled(@IdRes viewId: Int, isEnabled: Boolean): ViewHolderBase =
        apply { getView<View>(viewId).isEnabled = isEnabled }
}