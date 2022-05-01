package top.autoget.autokit

import android.Manifest.permission.KILL_BACKGROUND_PROCESSES
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Process
import android.provider.Settings
import androidx.annotation.RequiresPermission
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ApplicationKit.appPackageName
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.VersionKit.aboveLollipopMR1

object ProcessKit : LoggerKit {
    val isMainProcess: Boolean
        get() = appPackageName == currentProcessName
    val currentProcessName: String
        get() = app.activityManager.runningAppProcesses?.run {
            when (size) {
                0 -> ""
                else -> {
                    for (runningAppProcessInfo in this) {
                        runningAppProcessInfo.run { processName?.let { if (Process.myPid() == pid) return it } }
                    }
                    ""
                }
            }
        } ?: ""
    val foregroundProcessName: String
        get() {
            app.activityManager.runningAppProcesses?.run {
                if (size > 0) for (runningAppProcessInfo in this) {
                    runningAppProcessInfo.run { if (importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) return processName }
                }
            }
            when {
                aboveLollipopMR1 -> {
                    val packageManager: PackageManager = app.packageManager
                    val intent: Intent =
                        Intent().apply { action = Settings.ACTION_USAGE_ACCESS_SETTINGS }
                    val list: MutableList<ResolveInfo> = packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    info("$loggerTag->$list")
                    when {
                        list.size > 0 -> try {
                            app.appOpsManager.run {
                                packageManager.getApplicationInfo(appPackageName, 0).let {
                                    if (checkOpNoThrow(
                                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                                            it.uid, it.packageName
                                        ) != AppOpsManager.MODE_ALLOWED
                                    ) app.startActivity(intent.apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                    if (checkOpNoThrow(
                                            AppOpsManager.OPSTR_GET_USAGE_STATS,
                                            it.uid, it.packageName
                                        ) != AppOpsManager.MODE_ALLOWED
                                    ) return "".apply { info("$loggerTag->getForegroundProcessName: refuse to device usage stats.") }
                                }
                            }
                            nowMillis.let { time ->
                                return app.usageStatsManager.queryUsageStats(
                                    UsageStatsManager.INTERVAL_BEST, time - 86400000 * 7, time
                                )?.let { usageStatsList ->
                                    when {
                                        usageStatsList.isEmpty() -> ""
                                        else -> {
                                            var recentStats: UsageStats? = null
                                            for (usageStats in usageStatsList) {
                                                if (recentStats == null || usageStats.lastTimeUsed > recentStats.lastTimeUsed)
                                                    recentStats = usageStats
                                            }
                                            recentStats?.packageName ?: ""
                                        }
                                    }
                                } ?: ""
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                            return ""
                        }
                        else -> return "".apply { info("$loggerTag->getForegroundProcessName: noun of access to usage information.") }
                    }
                }
                else -> return ""
            }
        }
    val allBackgroundProcesses: MutableSet<String>
        @RequiresPermission(KILL_BACKGROUND_PROCESSES)
        get() = mutableSetOf<String>().apply {
            app.activityManager.runningAppProcesses?.let {
                for (runningAppProcessInfo in it) {
                    addAll(runningAppProcessInfo.pkgList)
                }
            }
        }

    @RequiresPermission(KILL_BACKGROUND_PROCESSES)
    fun killBackgroundProcesses(packageName: String?): Boolean = packageName?.let {
        when {
            it.isEmpty() || it == "null" -> false
            else -> app.activityManager.run {
                runningAppProcesses?.let { startList ->
                    when {
                        startList.isEmpty() -> true
                        else -> {
                            for (runningAppProcessInfo in startList) {
                                if (runningAppProcessInfo.pkgList.contains(it))
                                    killBackgroundProcesses(it)
                            }
                            runningAppProcesses?.let { endList ->
                                when {
                                    endList.isEmpty() -> true
                                    else -> {
                                        for (runningAppProcessInfo in endList) {
                                            if (runningAppProcessInfo.pkgList.contains(it)) return false
                                        }
                                        true
                                    }
                                }
                            } ?: true
                        }
                    }
                } ?: true
            }
        }
    } ?: false

    @RequiresPermission(KILL_BACKGROUND_PROCESSES)
    fun killAllBackgroundProcesses(): MutableSet<String> = mutableSetOf<String>().apply {
        app.activityManager.run {
            for (runningAppProcessInfo in runningAppProcesses) {
                for (pkg in runningAppProcessInfo.pkgList) {
                    killBackgroundProcesses(pkg)
                    add(pkg)
                }
            }
            for (runningAppProcessInfo in runningAppProcesses) {
                for (pkg in runningAppProcessInfo.pkgList) {
                    remove(pkg)
                }
            }
        }
    }
}