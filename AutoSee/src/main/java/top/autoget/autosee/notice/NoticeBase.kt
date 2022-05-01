package top.autoget.autosee.notice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import top.autoget.autokit.AKit
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.StringKit.isNotSpace

open class NoticeBase {
    var builder: NotificationCompat.Builder? = null
        private set
    private var mSmallIcon = 0//顶部状态栏小图标
    fun <T : NoticeBase?> setSmallIcon(smallIcon: Int): T? =
        apply { mSmallIcon = smallIcon } as T

    protected var mContentTitle: CharSequence? = null//通知中心标题
    fun <T : NoticeBase?> setContentTitle(contentTitle: CharSequence?): T? =
        apply { mContentTitle = contentTitle } as T

    protected var mContentText: CharSequence? = null//通知中心内容
    fun <T : NoticeBase?> setContentText(contentText: CharSequence?): T? =
        apply { mContentText = contentText } as T

    protected var mSummaryText: CharSequence? = null//概要内容
    fun <T : NoticeBase?> setSummaryText(summaryText: CharSequence?): T? =
        apply { mSummaryText = summaryText } as T

    fun <T : NoticeBase?> setBaseInfo(
        icon: Int, contentTitle: CharSequence?, contentText: CharSequence?
    ): T? = apply {
        mSmallIcon = icon
        mContentTitle = contentTitle
        mContentText = contentText
    } as T

    private var mHeadUp = false
    fun <T : NoticeBase?> setHeadUp(headUp: Boolean): T? = apply { mHeadUp = headUp } as T
    private var mContentIntent: PendingIntent? = null//通知点击的件
    fun <T : NoticeBase?> setContentIntent(contentIntent: PendingIntent?): T? =
        apply { mContentIntent = contentIntent } as T

    private var mDeleteIntent: PendingIntent? = null//通知删除事件
    fun <T : NoticeBase?> setDeleteIntent(deleteIntent: PendingIntent?): T? =
        apply { mDeleteIntent = deleteIntent } as T

    private var mFullscreenIntent: PendingIntent? = null//通知全屏事件
    fun <T : NoticeBase?> setFullScreenIntent(fullscreenIntent: PendingIntent?): T? =
        apply { mFullscreenIntent = fullscreenIntent } as T

    private var mId = 0//通知ID
    fun <T : NoticeBase?> setId(id: Int): T? = apply { mId = id } as T
    private var mChannelId: String? = null//通知渠道ID
    fun <T : NoticeBase?> setChannelId(channelId: String?): T? =
        apply { mChannelId = channelId } as T

    private var mChannelName: String? = null//通知渠道名称
    fun <T : NoticeBase?> setChannelName(channelName: String?): T? =
        apply { mChannelName = channelName } as T

    private var mBigIcon = 0//大图标
    fun <T : NoticeBase?> setBigIcon(bigIcon: Int): T? = apply { mBigIcon = bigIcon } as T
    private var mTicker: CharSequence? = "您有新的消息"//顶部状态栏提示信息
    fun <T : NoticeBase?> setTicker(ticker: CharSequence?): T? =
        apply { mTicker = ticker } as T

    private var mSubText: CharSequence? = null
    fun <T : NoticeBase?> setSubtext(subText: CharSequence?): T? =
        apply { mSubText = subText } as T

    private var mTime: Long = 0//通知时间
    fun <T : NoticeBase?> setTime(time: Long): T? = apply { mTime = time } as T
    private var mIsShowTime = true//是否显示通知时间
    fun <T : NoticeBase?> setIsShowWhen(isShowWhen: Boolean): T? =
        apply { mIsShowTime = isShowWhen } as T

    class BtnActionBean(val icon: Int, val text: CharSequence?, val pendingIntent: PendingIntent?)

    private var mBtnActionBeans: MutableList<BtnActionBean?>? = null//通知栏上按钮
    fun <T : NoticeBase?> addAction(
        icon: Int, text: CharSequence?, pendingIntent: PendingIntent?
    ): T? = apply {
        if (mBtnActionBeans == null) mBtnActionBeans = mutableListOf()
        if ((mBtnActionBeans?.size ?: 0) > 5) throw RuntimeException("5 buttons at most!")
        mBtnActionBeans?.add(BtnActionBean(icon, text, pendingIntent))
    } as T//增加按钮点击

    private var mPriority = NotificationCompat.PRIORITY_DEFAULT//通知优先级
    fun <T : NoticeBase?> setPriority(priority: Int): T? = apply { mPriority = priority } as T
    private var mIsSound = true//是否有声音
    private var mIsVibrate = true//是否震动
    private var mLights = true//是否闪烁
    fun <T : NoticeBase?> setDisplayForm(
        sound: Boolean, vibrate: Boolean, lights: Boolean
    ): T? = apply {
        mIsSound = sound
        mIsVibrate = vibrate
        mLights = lights
    } as T//设置表现形式

    private var mSoundUri: Uri? = null//声音资源地址
    fun <T : NoticeBase?> setSoundUri(soundUri: Uri?): T? =
        apply { mSoundUri = soundUri } as T

    private var mVibratePatten: LongArray? = null
    fun <T : NoticeBase?> setVibratePatten(vibratePatten: LongArray?): T? =
        apply { mVibratePatten = vibratePatten } as T

    private var mIsOnGoing = false//通知是否不可侧滑删除
    fun <T : NoticeBase?> setIsOnGoing(isOnGoing: Boolean): T? =
        apply { mIsOnGoing = isOnGoing } as T

    private var mIsForeGroundService = false//是否显示是前台服务通知
    fun <T : NoticeBase?> setForegroundService(): T? = apply {
        mIsForeGroundService = true
        mIsOnGoing = true
    } as T

    private var mVisibility = NotificationCompat.VISIBILITY_SECRET//通知可见度
    fun <T : NoticeBase?> setVisibility(visibility: Int): T? =
        apply { mVisibility = visibility } as T

    private var mStyle: NotificationCompat.Style? = null//通知拓展样式
    fun <T : NoticeBase?> setStyle(style: NotificationCompat.Style?): T? =
        apply { mStyle = style } as T

    private var mIsPolling = false//是否一直提示
    fun <T : NoticeBase?> setIsPolling(isPolling: Boolean): T? =
        apply { mIsPolling = isPolling } as T

    companion object {
        private val DEFAULT_CHANNEL_PREFIX_ID: String? = "autoselect_channel_id_"
        private val DEFAULT_CHANNEL_PREFIX_NAME: String? = "autoselect_channel_name_"
    }

    private var mDefaults = NotificationCompat.DEFAULT_LIGHTS//默认只有走马灯提醒
    val build = {
        beforeBuild()
        if (mChannelId == null) mChannelId = DEFAULT_CHANNEL_PREFIX_ID + mId
        if (mChannelName == null) mChannelName = DEFAULT_CHANNEL_PREFIX_NAME + mId
        builder = mChannelId?.let { it1 -> NotificationCompat.Builder(AKit.app, it1) }
        if (mSmallIcon > 0) builder?.setSmallIcon(mSmallIcon)//设置顶部状态栏小图标
        if (mBigIcon > 0)
            builder?.setLargeIcon(BitmapFactory.decodeResource(AKit.app.resources, mBigIcon))
        builder?.setTicker(mTicker)//顶部状态栏提示信息
        builder?.setContentTitle(mContentTitle)//设置通知中心标题
        if (isNotSpace(mContentText.toString())) builder?.setContentText(mContentText)//设置通知中心内容
        if (isNotSpace(mSubText.toString())) builder?.setContentText(mSubText)
        when {
            mTime > 0 -> builder?.setWhen(mTime)
            else -> builder?.setWhen(nowMillis)
        }
        builder?.setShowWhen(mIsShowTime)
        builder?.setContentIntent(mContentIntent)//通知要启动的Intent
        builder?.setDeleteIntent(mDeleteIntent)
        builder?.setFullScreenIntent(mFullscreenIntent, true)
        builder?.setAutoCancel(true)//为true点击通知栏notification后自动被取消消失，不设置点击消息后也不清除，但可滑动删除
        builder?.setOngoing(mIsOnGoing)//为true不能notification滑动删除
        builder?.priority = mPriority//优先级越高通知越靠前，优先级低不会在手机最顶部状态栏显示图标
        if (mIsSound) {
            mDefaults = mDefaults or Notification.DEFAULT_SOUND
            mSoundUri?.let { builder?.setSound(it) }
        }
        if (mIsVibrate) {
            mDefaults = mDefaults or Notification.DEFAULT_VIBRATE
            mVibratePatten?.let { builder?.setVibrate(it) }
        }
        if (mLights) mDefaults = mDefaults or Notification.DEFAULT_LIGHTS
        builder?.setDefaults(mDefaults)
        mBtnActionBeans?.apply {
            if (size > 0) for (bean in this) {
                bean?.let { builder?.addAction(bean.icon, bean.text, bean.pendingIntent) }
            }
        }//按钮
        when {
            mHeadUp -> {
                builder?.priority = NotificationCompat.PRIORITY_MAX
                builder?.setDefaults(NotificationCompat.DEFAULT_ALL)
            }
            else -> {
                builder?.priority = NotificationCompat.PRIORITY_DEFAULT
                builder?.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            }
        }//HeadUp
        builder?.setVisibility(mVisibility)
        mStyle?.let { builder?.setStyle(it) }
        afterBuild()
    }//构建通知内容

    protected open fun beforeBuild() {}
    protected open fun afterBuild() {}
    val show = {
        build
        val notification = builder?.build()
        if (mIsForeGroundService) notification?.flags = Notification.FLAG_FOREGROUND_SERVICE
        if (mIsPolling) notification?.flags = (notification?.flags
            ?: 0) or Notification.FLAG_INSISTENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationChannel(mChannelId, mChannelName, NotificationManager.IMPORTANCE_HIGH)
                .let { NoticeKit.getManager()?.createNotificationChannel(it) }
        NoticeKit.notify(mId, notification)
    }//显示通知
}