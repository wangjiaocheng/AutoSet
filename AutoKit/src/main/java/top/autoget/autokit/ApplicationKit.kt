package top.autoget.autokit

import android.Manifest.permission.KILL_BACKGROUND_PROCESSES
import android.Manifest.permission.PACKAGE_USAGE_STATS
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import top.autoget.autokit.AKit.activityLifecycle
import top.autoget.autokit.AKit.app
import top.autoget.autokit.AKit.isForegroundApp
import top.autoget.autokit.ConvertKit.bytes2HexString
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.ShellKit.execCmd
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.VersionKit.aboveGingerbread
import top.autoget.autokit.VersionKit.aboveLollipopMR1
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.regex.Pattern
import kotlin.system.exitProcess

object ApplicationKit : LoggerKit {
    val numCores: Int
        get() = try {
            File("/sys/devices/system/cpu/")
                .listFiles { file -> Pattern.matches("cpu[0-9]", file.name) }?.size ?: 1
        } catch (e: Exception) {
            e.printStackTrace()
            1
        }//CPU内核数

    fun isServiceRunning(className: String): Boolean {
        for (runningServiceInfo in app.activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (runningServiceInfo.service.className == className) return true
        }
        return false
    }

    fun stopRunningService(className: String): Boolean = try {
        Intent().apply { component = ComponentName(app, Class.forName(className)) }
            .let { app.stopService(it) }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    fun runScript(script: String): String? = try {
        val process = Runtime.getRuntime().exec(script)
        val input = StringBuilder()
        val inputThread = Thread {
            process.inputStream.use {
                InputStreamReader(it).use { inputStreamReader ->
                    BufferedReader(inputStreamReader, 8192).use { bufferedReader ->
                        try {
                            while (true) {
                                bufferedReader.readLine()
                                    ?.let { line -> input.append("$line\n") } ?: break
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        inputThread.start()
        val error = StringBuilder()
        val errorThread = Thread {
            process.errorStream.use {
                InputStreamReader(it).use { inputStreamReader ->
                    BufferedReader(inputStreamReader, 8192).use { bufferedReader ->
                        try {
                            while (true) {
                                bufferedReader.readLine()
                                    ?.let { line -> error.append("$line\n") } ?: break
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        errorThread.start()
        process.waitFor()
        while (inputThread.isAlive) {
            Thread.sleep(50)
        }
        if (errorThread.isAlive) errorThread.interrupt()
        input.toString() + error.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    @RequiresPermission(KILL_BACKGROUND_PROCESSES)
    fun killProcess(processName: String) = try {
        when {
            processName.contains(":") -> processName.split(":".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            else -> processName
        }.let { packageName ->
            app.activityManager.run {
                killBackgroundProcesses(packageName)
                javaClass.getDeclaredMethod("forceStopPackage", String::class.java)
                    .apply { isAccessible = true }.invoke(this, packageName)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    fun registerAppStatusChangedListener(any: Any, listener: AKit.OnAppStatusChangedListener) =
        activityLifecycle.addOnAppStatusChangedListener(any, listener)

    fun unregisterAppStatusChangedListener(any: Any) =
        activityLifecycle.removeOnAppStatusChangedListener(any)

    fun installApp(filePath: String) = installApp(getFileByPath(filePath))
    fun installApp(file: File?) = file?.run {
        if (exists() && isFile && length() > 0) app.startActivity(getInstallAppIntent(this))
    }

    fun installApp(filePath: String, activity: Activity, requestCode: Int) =
        installApp(getFileByPath(filePath), activity, requestCode)

    fun installApp(file: File?, activity: Activity, requestCode: Int) = file?.run {
        if (exists() && isFile && length() > 0)
            activity.startActivityForResult(getInstallAppIntent(this), requestCode)
    }

    val appPackageName: String
        get() = app.packageName

    private fun getInstallAppIntent(file: File?, isNewTask: Boolean = false): Intent? = file?.let {
        Intent().apply {
            action = Intent.ACTION_VIEW
            if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            data = when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.N -> Uri.fromFile(it)
                else -> FileProvider.getUriForFile(
                    app, "$appPackageName.helper.provider", it
                )
            }.apply {
                app.grantUriPermission(
                    appPackageName, this, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                app.grantUriPermission(
                    appPackageName, this, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            type = "application/vnd.android.package-archive"
        }
    }

    @JvmOverloads
    fun installAppSilent(
        filePath: String, params: String? = null,
        isRooted: Boolean = !isAppSystem || RootKit.isRooted
    ): Boolean = installAppSilent(getFileByPath(filePath), params, isRooted)

    @JvmOverloads
    fun installAppSilent(
        file: File?, params: String? = null, isRooted: Boolean = !isAppSystem || RootKit.isRooted
    ): Boolean = file?.run {
        when {
            exists() && isFile && length() > 0 -> execCmd(
                "LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm install ${if (params == null) "" else "$params "}\"$absolutePath\"",
                isRooted
            ).let {
                when {
                    it.successMsg.toLowerCase(Locale.getDefault()).contains("success") -> true
                    else -> false.apply { error("$loggerTag->installAppSilent successMsg: ${it.successMsg}, errorMsg: ${it.errorMsg}") }
                }
            }
            else -> false
        }//非root添加<uses-permission android:name="android.permission.INSTALL_PACKAGES" />
    } ?: false

    fun uninstallApp(packageName: String) =
        if (isNotSpace(packageName)) app.startActivity(getUninstallAppIntent(packageName)) else Unit

    fun uninstallApp(packageName: String, activity: Activity, requestCode: Int) {
        if (isNotSpace(packageName))
            activity.startActivityForResult(getUninstallAppIntent(packageName), requestCode)
    }

    private fun getUninstallAppIntent(packageName: String, isNewTask: Boolean = false): Intent =
        Intent().apply {
            action = Intent.ACTION_DELETE
            if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("package:$packageName")
        }

    @JvmOverloads
    fun uninstallAppSilent(
        packageName: String, isKeepData: Boolean = false,
        isRooted: Boolean = !isAppSystem || RootKit.isRooted
    ): Boolean = when {
        isSpace(packageName) -> false
        else -> execCmd(
            "LD_LIBRARY_PATH=/vendor/lib*:/system/lib* pm uninstall ${if (isKeepData) "-k " else ""}$packageName",
            isRooted
        ).let {
            when {
                it.successMsg.toLowerCase(Locale.getDefault()).contains("success") -> true
                else -> false.apply { error("$loggerTag->uninstallAppSilent successMsg: ${it.successMsg}, errorMsg: ${it.errorMsg}") }
            }
        }
    }//非root添加<uses-permission android:name="android.permission.DELETE_PACKAGES" />

    fun isAppInstalled(packageName: String): Boolean = try {
        isNotSpace(packageName) && app.packageManager.getApplicationInfo(packageName, 0) != null
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        false
    }//getLaunchAppIntent(packageName) != null

    val isAppRoot: Boolean
        get() = execCmd("echo root", true)
            .let { (it.result == 0).apply { debug("isAppRoot${it.errorMsg}") } }
    val isAppDebug: Boolean
        get() = isAppDebug(appPackageName)

    fun isAppDebug(packageName: String): Boolean = when {
        isSpace(packageName) -> false
        else -> try {
            app.packageManager.getApplicationInfo(packageName, 0)
                .run { flags and ApplicationInfo.FLAG_DEBUGGABLE != 0 }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    val isAppSystem: Boolean
        get() = isAppSystem(appPackageName)

    fun isAppSystem(packageName: String): Boolean = when {
        isSpace(packageName) -> false
        else -> try {
            app.packageManager.getApplicationInfo(packageName, 0)
                .run { flags and ApplicationInfo.FLAG_SYSTEM != 0 }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    val isAppBackground: Boolean
        @RequiresPermission(PACKAGE_USAGE_STATS)
        get() = nowMillis.let { time ->
            app.usageStatsManager
                .queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 1000, time)
                .apply { sortWith { o1, o2 -> o1.lastTimeUsed.compareTo(o2.lastTimeUsed) } }
                .run { if (isNotEmpty()) get(0).packageName != appPackageName else false }
        }
    val isAppForeground: Boolean
        get() = isForegroundApp

    @RequiresPermission(PACKAGE_USAGE_STATS)
    fun isAppForeground(packageName: String): Boolean =
        isNotSpace(packageName) && packageName == foregroundProcessName

    private val foregroundProcessName: String?
        get() {
            app.activityManager.runningAppProcesses?.run {
                if (size > 0) for (info in this) {
                    if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                        return info.processName
                }
            }
            if (aboveLollipopMR1)
                Intent().apply { action = Settings.ACTION_USAGE_ACCESS_SETTINGS }.let { intent ->
                    app.packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        .apply { info("$loggerTag->$this") }.run {
                            if (size <= 0)
                                return "".apply { info("$loggerTag->foregroundProcessName: noun of access to usage information.") }
                        }
                    try {
                        app.packageManager.getApplicationInfo(appPackageName, 0)
                            .run {
                                app.appOpsManager.let {
                                    if (it.checkOpNoThrow(
                                            AppOpsManager.OPSTR_GET_USAGE_STATS, uid, packageName
                                        ) != AppOpsManager.MODE_ALLOWED
                                    ) app.startActivity(intent.apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                    if (it.checkOpNoThrow(
                                            AppOpsManager.OPSTR_GET_USAGE_STATS, uid, packageName
                                        ) != AppOpsManager.MODE_ALLOWED
                                    ) return "".apply { info("$loggerTag->foregroundProcessName: refuse to device usage stats.") }
                                }
                            }
                        nowMillis.let { endTime ->
                            app.usageStatsManager.queryUsageStats(
                                UsageStatsManager.INTERVAL_BEST, endTime - 86400000 * 7, endTime
                            )?.run {
                                when {
                                    isEmpty() -> null
                                    else -> {
                                        var recentStats: UsageStats? = null
                                        for (usageStats in this) {
                                            if (recentStats == null || usageStats.lastTimeUsed > recentStats.lastTimeUsed)
                                                recentStats = usageStats
                                        }
                                        recentStats?.packageName
                                    }
                                }
                            }
                        }
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }
                }
            return ""
        }

    fun launchApp(packageName: String) {
        if (isNotSpace(packageName))
            app.run { startActivity(packageManager.getLaunchIntentForPackage(packageName)) }
    }

    fun launchApp(packageName: String, activity: Activity, requestCode: Int) {
        if (isNotSpace(packageName)) activity.run {
            startActivityForResult(
                packageManager.getLaunchIntentForPackage(packageName), requestCode
            )
        }
    }

    @JvmOverloads
    fun relaunchApp(isKillProcess: Boolean = false) {
        app.packageManager.getLaunchIntentForPackage(appPackageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }.let { app.startActivity(it) }
        if (isKillProcess) {
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        }
    }

    @JvmOverloads
    fun launchAppDetailsSettings(
        packageName: String = appPackageName, isNewTask: Boolean = false
    ) {
        if (isNotSpace(packageName)) Intent().apply {
            if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            when {
                aboveGingerbread -> {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:$packageName")
                }
                else -> {
                    action = Intent.ACTION_VIEW
                    component = ComponentName(
                        "com.android.settings", "com.android.settings.InstalledAppDetails"
                    )
                    putExtra("com.android.settings.ApplicationPkgName", packageName)
                }
            }//Uri.fromParts("package", packageName, null)
        }.let { app.startActivity(it) }
    }

    fun getAppInstaller(packageName: String): String? =
        app.packageManager.getInstallerPackageName(packageName)

    fun getAppFirstInstallTime(packageName: String): Long = try {
        app.packageManager.getPackageInfo(packageName, 0).firstInstallTime
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        0
    }

    fun getAppLastUpdateTime(packageName: String): Long = try {
        app.packageManager.getPackageInfo(packageName, 0).lastUpdateTime
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        0
    }

    fun getAppTargetSdkVersion(packageName: String): Int = try {
        app.packageManager.getPackageInfo(packageName, 0).applicationInfo.targetSdkVersion
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        0
    }

    fun getAppUid(packageName: String): Int = try {
        app.packageManager.getPackageInfo(packageName, 0).applicationInfo.uid
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        0
    }

    fun getApplicationMetaData(key: String): String? = try {
        app.run {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                .metaData.get(key)?.toString()
        }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }

    fun getAppSize(packageName: String): Long = try {
        File(app.packageManager.getApplicationInfo(packageName, 0).sourceDir).length()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        0
    }

    val appIcon: Drawable?
        get() = getAppIcon(appPackageName)

    fun getAppIcon(packageName: String): Drawable? = when {
        isSpace(packageName) -> null
        else -> try {
            app.packageManager
                .run { getPackageInfo(packageName, 0)?.applicationInfo?.loadIcon(this) }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    val appName: String?
        get() = getAppName(appPackageName)

    fun getAppName(packageName: String): String? = when {
        isSpace(packageName) -> ""
        else -> try {
            app.packageManager.run {
                getPackageInfo(packageName, 0)?.applicationInfo?.loadLabel(this)?.toString()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    val appPath: String?
        get() = getAppPath(appPackageName)

    fun getAppPath(packageName: String): String? = when {
        isSpace(packageName) -> ""
        else -> try {
            app.packageManager.getPackageInfo(packageName, 0)?.applicationInfo?.sourceDir
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    val appVersionName: String
        get() = getAppVersionName(appPackageName)

    fun getAppVersionName(packageName: String): String = when {
        isSpace(packageName) -> ""
        else -> try {
            app.packageManager.getPackageInfo(packageName, 0)?.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    val appVersionCode: Int
        get() = getAppVersionCode(appPackageName)

    fun getAppVersionCode(packageName: String): Int = when {
        isSpace(packageName) -> -1
        else -> try {
            app.packageManager.getPackageInfo(packageName, 0)?.versionCode ?: -1
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            -1
        }
    }

    val appSignature: Array<Signature>?
        get() = getAppSignature(appPackageName)

    fun getAppSignature(packageName: String): Array<Signature>? = when {
        isSpace(packageName) -> null
        else -> try {
            app.packageManager
                .getPackageInfo(packageName, PackageManager.GET_SIGNATURES)?.signatures
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    val appSignatureSHA1: String
        get() = getAppSignatureSHA1(appPackageName)

    fun getAppSignatureSHA1(packageName: String): String = getAppSignatureHash(packageName, "SHA1")
    val appSignatureSHA256: String
        get() = getAppSignatureSHA256(appPackageName)

    fun getAppSignatureSHA256(packageName: String): String =
        getAppSignatureHash(packageName, "SHA256")

    val appSignatureMD5: String
        get() = getAppSignatureMD5(appPackageName)

    fun getAppSignatureMD5(packageName: String): String = getAppSignatureHash(packageName, "MD5")
    private fun getAppSignatureHash(packageName: String, algorithm: String): String = when {
        isSpace(packageName) -> ""
        else -> getAppSignature(packageName)?.let { signature ->
            when {
                signature.isEmpty() -> ""
                else -> bytes2HexString(hashTemplate(signature[0].toByteArray(), algorithm))
                    .replace("(?<=[0-9A-F]{2})[0-9A-F]{2}".toRegex(), ":$0")
            }
        } ?: ""
    }

    private fun hashTemplate(data: ByteArray?, algorithm: String): ByteArray? = try {
        data?.let {
            when {
                it.isEmpty() -> null
                else -> MessageDigest.getInstance(algorithm).apply { update(it) }.digest()
            }
        }
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
        null
    }

    data class AppInfo(
        var icon: Drawable? = null, var name: String? = null,
        var packageName: String? = null, var packagePath: String? = null,
        var versionName: String? = null, var versionCode: Int = 0,
        var isSystem: Boolean = false, var isUser: Boolean = false, var isSD: Boolean = false
    )

    val appsInfo: MutableList<AppInfo>
        get() = mutableListOf<AppInfo>().apply {
            for (packageInfo in app.packageManager.getInstalledPackages(0)) {
                getBean(app.packageManager, packageInfo)?.let { add(it) }
            }
        }
    val appInfo: AppInfo?
        get() = getAppInfo(appPackageName)

    fun getAppInfo(packageName: String): AppInfo? = try {
        app.packageManager.run { getBean(this, getPackageInfo(packageName, 0)) }
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }

    fun getApkInfo(apkFile: File?): AppInfo? =
        apkFile?.run { if (isFile && exists()) getApkInfo(absolutePath) else null }

    fun getApkInfo(apkFilePath: String): AppInfo? = when {
        isSpace(apkFilePath) -> null
        else -> app.packageManager.run {
            getBean(this, getPackageArchiveInfo(apkFilePath, 0)
                ?.apply { applicationInfo.sourceDir = apkFilePath })
        }
    }

    private fun getBean(packageManager: PackageManager?, packageInfo: PackageInfo?): AppInfo? =
        packageManager?.let { pm ->
            packageInfo?.run {
                applicationInfo.let {
                    AppInfo(
                        it.loadIcon(pm), it.loadLabel(pm).toString(),
                        packageName, it.sourceDir, versionName, versionCode,
                        ApplicationInfo.FLAG_SYSTEM and it.flags != 0,
                        ApplicationInfo.FLAG_SYSTEM and it.flags != ApplicationInfo.FLAG_SYSTEM,
                        ApplicationInfo.FLAG_SYSTEM and it.flags != ApplicationInfo.FLAG_SYSTEM
                    )
                }//packageManager.getApplicationIcon(it)；packageManager.getApplicationLabel(it)
            }
        }
}