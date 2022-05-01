package top.autoget.autosee.notice

import android.app.PendingIntent
import android.graphics.Bitmap
import android.widget.RemoteViews

class NoticeCustomView(packageName: String? = null, layoutId: Int = 0) : NoticeBase() {
    private var mContentView: RemoteViews? = RemoteViews(packageName, layoutId)//自定义view
    fun setContentView(packageName: String?, layoutId: Int): NoticeCustomView =
        apply { mContentView = RemoteViews(packageName, layoutId) }

    fun setContentView(contentView: RemoteViews?): NoticeCustomView =
        apply { mContentView = contentView }

    private var mIsBigContentView = false//是否是高度最大的自定义view
    fun setIsBigContentView(isBigContentView: Boolean): NoticeCustomView =
        apply { mIsBigContentView = isBigContentView }

    fun setTextViewText(viewId: Int, text: CharSequence?): NoticeCustomView =
        apply { mContentView?.setTextViewText(viewId, text) }

    fun setImageViewResource(viewId: Int, srcId: Int): NoticeCustomView =
        apply { mContentView?.setImageViewResource(viewId, srcId) }

    fun setImageViewBitmap(viewId: Int, bitmap: Bitmap?): NoticeCustomView =
        apply { mContentView?.setImageViewBitmap(viewId, bitmap) }

    fun setOnClickPendingIntent(
        viewId: Int, pendingIntent: PendingIntent?
    ): NoticeCustomView =
        apply { mContentView?.setOnClickPendingIntent(viewId, pendingIntent) }//设置按钮点击事件

    override fun afterBuild() {
        mContentView?.let {
            when {
                mIsBigContentView -> builder?.setCustomBigContentView(it)
                else -> builder?.setCustomContentView(it)
            }
        }
    }
}