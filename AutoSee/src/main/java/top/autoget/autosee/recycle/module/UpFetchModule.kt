package top.autoget.autosee.recycle.module

import top.autoget.autosee.recycle.BaseAdapterQuick

interface UpFetchModule//需要向上加载更多BaseQuickAdapter继承
open class UpFetchModuleBase(private val baseQuickAdapter: BaseAdapterQuick<*, *>) :
    UpFetchListener {
    private var mUpFetchListener: OnUpFetchListener? = null
    override fun setOnUpFetchListener(listener: OnUpFetchListener?) {
        mUpFetchListener = listener
    }

    var isUpFetchEnable = false
    var isUpFetching = false
    var startUpFetchPosition = 1
    internal fun autoUpFetch(position: Int) {
        if (isUpFetchEnable && !isUpFetching && position <= startUpFetchPosition) mUpFetchListener?.onUpFetch()
    }
}