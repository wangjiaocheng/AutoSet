package top.autoget.autokit

import android.app.Activity
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.pm.PackageManager
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ApplicationKit.appPackageName

object MetaDataKit {
    fun getMetaDataInApp(key: String): String = try {
        app.packageManager.getApplicationInfo(appPackageName, PackageManager.GET_META_DATA)
            .metaData?.get(key).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        ""
    }

    fun getMetaDataInActivity(activity: Activity, key: String): String =
        getMetaDataInActivity(activity.javaClass, key)

    fun getMetaDataInActivity(clazz: Class<out Activity>, key: String): String = try {
        app.packageManager.getActivityInfo(ComponentName(app, clazz), PackageManager.GET_META_DATA)
            .metaData?.get(key).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        ""
    }

    fun getMetaDataInService(service: Service, key: String): String =
        getMetaDataInService(service.javaClass, key)

    fun getMetaDataInService(clazz: Class<out Service>, key: String): String = try {
        app.packageManager.getServiceInfo(ComponentName(app, clazz), PackageManager.GET_META_DATA)
            .metaData?.get(key).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        ""
    }

    fun getMetaDataInReceiver(receiver: BroadcastReceiver, key: String): String =
        getMetaDataInReceiver(receiver.javaClass, key)

    fun getMetaDataInReceiver(clazz: Class<out BroadcastReceiver>, key: String): String = try {
        app.packageManager
            .getReceiverInfo(ComponentName(app, clazz), PackageManager.GET_META_DATA)
            .metaData?.get(key).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        ""
    }
}