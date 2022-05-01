package top.autoget.autosee.notice

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import top.autoget.autokit.AKit

object NoticeKit {
    private var mNotificationManager: NotificationManager? = null
    fun buildSimple(
        id: Int, smallIcon: Int, contentTitle: CharSequence?,
        contentText: CharSequence?, contentIntent: PendingIntent?
    ): NoticeBase? = NoticeBase().setId<NoticeBase?>(id)
        ?.setBaseInfo<NoticeBase?>(smallIcon, contentTitle, contentText)
        ?.setContentIntent(contentIntent)

    fun buildBigPic(
        id: Int, smallIcon: Int, contentTitle: CharSequence?, summaryText: CharSequence?
    ): NoticeBigPic? = NoticeBigPic().setId<NoticeBase?>(id)
        ?.setSmallIcon<NoticeBase?>(smallIcon)
        ?.setContentTitle<NoticeBase?>(contentTitle)?.setSummaryText(summaryText)

    fun buildBigText(
        id: Int, smallIcon: Int, contentTitle: CharSequence?, contentText: CharSequence?
    ): NoticeBigText? = NoticeBigText().setId<NoticeBase?>(id)
        ?.setBaseInfo(smallIcon, contentTitle, contentText)

    fun buildMailBox(id: Int, smallIcon: Int, contentTitle: CharSequence?): NoticeMailbox? =
        NoticeMailbox().setId<NoticeBase?>(id)
            ?.setSmallIcon<NoticeBase?>(smallIcon)?.setContentTitle(contentTitle)

    fun buildProgress(
        id: Int, smallIcon: Int, contentTitle: CharSequence?, max: Int, progress: Int
    ): NoticeProgress? = NoticeProgress().setProgress(max, progress)
        ?.setId<NoticeBase?>(id)
        ?.setSmallIcon<NoticeBase?>(smallIcon)?.setContentTitle(contentTitle)

    fun buildProgress(id: Int, smallIcon: Int, contentTitle: CharSequence?): NoticeProgress? =
        NoticeProgress().setIndeterminate(true)?.setId<NoticeBase?>(id)
            ?.setSmallIcon<NoticeBase?>(smallIcon)?.setContentTitle(contentTitle)

    fun buildCustomView(
        id: Int, smallIcon: Int, contentTitle: CharSequence?, packageName: String?, layoutId: Int
    ): NoticeCustomView? = NoticeCustomView(packageName, layoutId)
        .setId<NoticeBase?>(id)
        ?.setSmallIcon<NoticeBase?>(smallIcon)?.setContentTitle(contentTitle)

    fun isNotifyPermissionOpen(context: Context): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
            NotificationManagerCompat.from(context).importance != NotificationManager.IMPORTANCE_NONE
        else -> NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun openNotifyPermissionSetting(context: Context) = try {
        val intent = Intent().apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> intent.apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
            }.let { context.startActivity(it) }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> intent.apply {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", context.packageName)
                putExtra("app_uid", context.applicationInfo.uid)
            }.let { context.startActivity(it) }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT -> intent.apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:${context.packageName}")
            }.let { context.startActivity(it) }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD -> intent.apply {
                action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                data = Uri.fromParts("package", context.packageName, null)
            }.let { context.startActivity(intent) }//4.4以下没有从app跳转到应用通知设置页面的Action，可考虑跳转到应用详情页面
            else -> intent.apply {
                action = Intent.ACTION_VIEW
                setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
                putExtra("com.android.settings.ApplicationPkgName", context.packageName)
            }.let { context.startActivity(it) }
        }//直接跳转到应用通知设置
    } catch (e: Exception) {
        e.printStackTrace()
    }

    private val notificationManager: NotificationManager? =
        AKit.app.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager

    fun getManager(): NotificationManager? {
        if (mNotificationManager == null) mNotificationManager = notificationManager
        return mNotificationManager
    }

    fun notify(id: Int, notification: Notification?) {
        if (mNotificationManager == null) mNotificationManager = notificationManager
        mNotificationManager?.notify(id, notification)
    }

    fun cancel(id: Int) {
        if (mNotificationManager == null) mNotificationManager = notificationManager
        mNotificationManager?.cancel(id)
    }

    val cancelAll = {
        if (mNotificationManager == null) mNotificationManager = notificationManager
        mNotificationManager?.cancelAll()
    }
}