package top.autoget.autosee.notice

import androidx.core.app.NotificationCompat

class NoticeBigText : NoticeBase() {
    override fun beforeBuild() {
        NotificationCompat.BigTextStyle().setBigContentTitle(mContentTitle).bigText(mContentText)
            .setSummaryText(mSummaryText).let { setStyle<NoticeBase?>(it) }
    }
}