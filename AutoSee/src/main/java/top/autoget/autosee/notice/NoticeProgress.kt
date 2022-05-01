package top.autoget.autosee.notice

import androidx.core.app.NotificationCompat

class NoticeProgress : NoticeBase() {
    companion object {
        private const val DEFAULT_FORMAT: String = "进度:%d/%d"
    }

    private var mFormat = DEFAULT_FORMAT//模版
    fun setFormat(format: String): NoticeProgress = apply { mFormat = format }
    private var mMax = 0//最大进度
    fun setMaxProgress(max: Int): NoticeProgress = apply { mMax = max }
    private var mProgress = 0//进度条
    fun setProgress(max: Int, progress: Int): NoticeProgress = apply {
        mMax = max
        mProgress = progress
        setContentText<NoticeBase?>(String.format(DEFAULT_FORMAT, mProgress, mMax))
    }

    private var mIndeterminate = false//是否无进度条
    fun setIndeterminate(indeterminate: Boolean): NoticeProgress = apply {
        mIndeterminate = indeterminate
        if (mIndeterminate) {
            mMax = 0
            mProgress = 0
            setContentText<NoticeBase?>(null)
        }
    }

    fun updateProgress(progress: Int, format: String, vararg args: Any?) {
        mProgress = progress
        mFormat = format
        setContentText<NoticeBase?>(String.format(mFormat, *args))
    }

    fun updateProgress(progress: Int) {
        mProgress = progress
        setContentText<NoticeBase?>(String.format(DEFAULT_FORMAT, mProgress, mMax))
    }

    private val updateProgress = builder?.setProgress(mMax, mProgress, mIndeterminate)
    override fun afterBuild() {
        updateProgress
        builder?.setDefaults(0)
        builder?.priority = NotificationCompat.PRIORITY_LOW
    }
}