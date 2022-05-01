package top.autoget.autokit

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import top.autoget.autokit.StringKit.isNotSpace

object PollingKit {
    fun isExistPollingService(context: Context?, cls: Class<*>?): Boolean =
        PendingIntent.getService(
            context, 0, Intent(context, cls), PendingIntent.FLAG_NO_CREATE
        ) != null//是否存在轮询服务

    @JvmOverloads
    fun startPollingService(
        context: Context?, interval: Int, cls: Class<*>?, action: String? = null
    ) {
        val intent =
            Intent(context, cls).apply { if (isNotSpace(action)) this.action = action }
        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val triggerAtTime = SystemClock.elapsedRealtime()
        context?.alarmManager?.setRepeating(
            AlarmManager.ELAPSED_REALTIME, triggerAtTime, interval * 1000L, pendingIntent
        )
    }//开启轮询服务

    @JvmOverloads
    fun stopPollingService(context: Context?, cls: Class<*>?, action: String? = null) {
        val intent =
            Intent(context, cls).apply { if (isNotSpace(action)) this.action = action }
        val pendingIntent =
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        context?.alarmManager?.cancel(pendingIntent)
    }//停止轮询服务
}