package top.autoget.autosee.notice

import androidx.core.app.NotificationCompat

class NoticeMailbox : NoticeBase() {
    private var mMessages: MutableList<String?>? = null
    fun setMessages(messages: MutableList<String?>?): NoticeMailbox =
        apply { mMessages = messages }

    fun addMsg(msg: String?): NoticeMailbox = apply {
        if (mMessages == null) mMessages = mutableListOf()
        mMessages?.add(msg)
    }

    override fun beforeBuild() {
        mMessages?.apply {
            if (size > 0) when {
                size > 1 -> {
                    val inboxStyle =
                        NotificationCompat.InboxStyle().setSummaryText("你收到了[$size]条信息")
                    for (msg in this) {
                        inboxStyle.addLine(msg)
                    }
                    setStyle<NoticeBase?>(inboxStyle)
                }
                else -> setContentText<NoticeBase?>(this[0])
            }
        }
    }
}