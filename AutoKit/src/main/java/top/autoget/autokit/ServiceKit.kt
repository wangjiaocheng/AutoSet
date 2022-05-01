package top.autoget.autokit

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import top.autoget.autokit.AKit.app

object ServiceKit {
    val allRunningServices: MutableSet<*>?
        get() = app.activityManager.getRunningServices(0x7FFFFFFF)?.let {
            when (it.size) {
                0 -> null
                else -> mutableSetOf<String>().apply {
                    for (runningServiceInfo in it) {
                        add(runningServiceInfo.service.className)
                    }
                }
            }
        }

    fun isServiceRunning(className: String): Boolean = isServiceRunning(Class.forName(className))
    fun isServiceRunning(clazz: Class<*>): Boolean =
        app.activityManager.getRunningServices(0x7FFFFFFF)?.let {
            when (it.size) {
                0 -> false
                else -> {
                    for (runningServiceInfo in it) {
                        if (clazz.name == runningServiceInfo.service.className) return true
                    }
                    false
                }
            }
        } ?: false

    fun startService(className: String): ComponentName? = try {
        startService(Class.forName(className))
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun startService(clazz: Class<*>): ComponentName? =
        app.run { startService(Intent(this, clazz)) }

    fun stopService(className: String): Boolean = try {
        stopService(Class.forName(className))
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    fun stopService(clazz: Class<*>): Boolean = app.run { stopService(Intent(this, clazz)) }
    fun bindService(className: String, serviceConnection: ServiceConnection, flags: Int): Boolean =
        try {
            bindService(Class.forName(className), serviceConnection, flags)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }//Context.BIND_AUTO_CREATE、Context.BIND_DEBUG_UNBIND、Context.BIND_NOT_FOREGROUND、Context.BIND_ABOVE_CLIENT、Context.BIND_ALLOW_OOM_MANAGEMENT、Context.BIND_WAIVE_PRIORITY

    fun bindService(clazz: Class<*>, serviceConnection: ServiceConnection, flags: Int): Boolean =
        app.run { bindService(Intent(this, clazz), serviceConnection, flags) }

    fun unbindService(serviceConnection: ServiceConnection) =
        app.unbindService(serviceConnection)
}