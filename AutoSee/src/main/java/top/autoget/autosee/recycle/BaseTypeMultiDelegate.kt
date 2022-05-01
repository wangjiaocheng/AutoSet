package top.autoget.autosee.recycle

import android.util.SparseIntArray
import androidx.annotation.LayoutRes

abstract class BaseTypeMultiDelegate<T>(private var layouts: SparseIntArray = SparseIntArray()) {
    fun getLayoutId(viewType: Int): Int = layouts.get(viewType).apply {
        require(this != 0) { "ViewType: $viewType found layoutResId，please use registerItemType() first!" }
    }

    private var autoMode: Boolean = false
    private var selfMode: Boolean = false
    fun addItemTypeAutoIncrease(@LayoutRes vararg layoutResIds: Int): BaseTypeMultiDelegate<T> =
        apply {
            autoMode = true
            checkMode(selfMode)
            for ((index, value) in layoutResIds.withIndex()) {
                registerItemType(index, value)
            }
        }

    fun addItemType(type: Int, @LayoutRes layoutResId: Int): BaseTypeMultiDelegate<T> = apply {
        selfMode = true
        checkMode(autoMode)
        registerItemType(type, layoutResId)
    }

    private fun checkMode(mode: Boolean) = require(!mode) { "Don't mess two register mode" }
    private fun registerItemType(type: Int, @LayoutRes layoutResId: Int) =
        apply { layouts.put(type, layoutResId) }

    abstract fun getItemType(data: List<T>, position: Int): Int
}