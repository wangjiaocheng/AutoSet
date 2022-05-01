package top.autoget.autokit

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri

object ShortcutKit {
    fun hasShortcut(activity: Activity): Boolean = activity.contentResolver.query(
        Uri.parse("content://com.android.launcher.settings/favorites?notify=true"),
        arrayOf("title", "iconResource"), "title=?",
        arrayOf(activity.getString(R.string.app_name).trim { it <= ' ' }), null
    )?.use { it.count > 0 } ?: false

    fun addShortcut(activity: Activity, res: Int) = Intent().apply {
        action = "com.android.launcher.action.INSTALL_SHORTCUT"
        putExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent().apply {
            action = Intent.ACTION_MAIN
            setClassName(activity, activity.javaClass.name)
        })
        putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getString(R.string.app_name))
        putExtra(
            Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
            Intent.ShortcutIconResource.fromContext(activity, res)
        )
        putExtra("duplicate", false)//不许重复创建
    }.let { activity.sendBroadcast(it) }

    fun delShortcut(activity: Activity) = Intent().apply {
        action = "com.android.launcher.action.UNINSTALL_SHORTCUT"
        putExtra(Intent.EXTRA_SHORTCUT_INTENT, Intent().apply {
            action = Intent.ACTION_MAIN
            component = ComponentName(
                activity.packageName,
                "${activity.packageName}.${activity.localClassName}"
            )
        })
        putExtra(Intent.EXTRA_SHORTCUT_NAME, activity.getString(R.string.app_name))
    }.let { activity.sendBroadcast(it) }
}