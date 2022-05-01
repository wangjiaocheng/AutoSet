package top.autoget.autokit

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_PHONE_STATE
import android.accounts.AccountManager
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresPermission
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ActivityKit.launcherActivityName
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.VersionKit.aboveIceCreamSandwich
import top.autoget.autokit.VersionKit.aboveLollipop
import top.autoget.autokit.VersionKit.aboveOreo
import java.util.*

object SystemKit {
    val buildManufacturer: String
        get() = Build.MANUFACTURER//厂商
    val buildModel: String
        get() = Build.MODEL?.trim { it <= ' ' }?.replace("\\s*".toRegex(), "") ?: ""//型号
    val serial: String?
        get() = try {
            "android.os.SystemProperties".javaClass
                .let { it.getMethod("get", String::class.java).invoke(it, "ro.serialno") as String }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    val uniqueSerial: String
        get() = "$buildManufacturer-$buildModel-$serial"
    val uniqueId: String
        get() = ("35${buildManufacturer.length % 10}${buildModel.length % 10}${buildBrand.length % 10}${buildProduct.length % 10}${buildDevice.length % 10}${buildBoard.length % 10}${buildAbis[0].length % 10}")
            .hashCode().toLong().run {
                try {
                    Build::class.java.getField("SERIAL").get(null)?.toString().hashCode().toLong()
                        .let { UUID(this, it) }
                } catch (e: Exception) {
                    UUID(this, "ESYDV000".hashCode().toLong())
                }.toString()
            }//设备物理唯一标识符，伪唯一ID
    val buildSerial: String
        @RequiresPermission(READ_PHONE_STATE)
        get() = if (aboveOreo) Build.getSerial() else Build.SERIAL
    val buildBrand: String
        get() = Build.BRAND//编译厂商
    val buildHost: String
        get() = Build.HOST//编译主机
    val buildUser: String
        get() = Build.USER//编译作者
    val buildTags: String
        get() = Build.TAGS//编译描述
    val buildTime: Long
        get() = Build.TIME//编译时间
    val buildFingerprint: String
        get() = Build.FINGERPRINT
    val buildProduct: String
        get() = Build.PRODUCT
    val buildDevice: String
        get() = Build.DEVICE
    val buildHardware: String
        get() = Build.HARDWARE
    val buildBoard: String
        get() = Build.BOARD
    val buildID: String
        get() = Build.ID//修订版本列表
    val buildDisplayVersion: String
        get() = Build.DISPLAY//系统版本
    val buildBootloaderVersion: String
        get() = Build.BOOTLOADER//启动程序版本
    val buildRadioVersion: String
        get() = if (aboveIceCreamSandwich) Build.getRadioVersion() else ""//基带版本
    val buildAbis: Array<String>
        get() = when {
            aboveLollipop -> Build.SUPPORTED_ABIS
            else -> when {
                isSpace(Build.CPU_ABI2) -> arrayOf(Build.CPU_ABI)
                else -> arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
            }
        }//CPU指令集
    val buildVersionSDK: Int
        get() = Build.VERSION.SDK_INT//系统版本
    val buildVersionRelease: String
        get() = Build.VERSION.RELEASE//编译版本
    val buildVersionCodename: String
        get() = Build.VERSION.CODENAME//开发代号
    val buildVersionIncremental: String
        get() = Build.VERSION.INCREMENTAL//源码控制版本
    val locales: Array<Locale>
        get() = Locale.getAvailableLocales()
    val currentLocale: Locale
        get() = Locale.getDefault()//Resources.getSystem().configuration：(>=N).locales[0]或.locale
    val currentLanguage: String
        get() = currentLocale.language
    val gsfId: String?
        get() = app.contentResolver.query(
            Uri.parse("content://com.google.android.gsf.gservices"),
            null, null, arrayOf("android_id"), null
        )?.use { cursor ->
            cursor.run {
                when {
                    moveToFirst() && columnCount >= 2 -> java.lang.Long.toHexString(getString(1).toLong())
                    else -> null
                }
            }
        }//谷歌服务框架ID<uses-permission android:name="com.google.android.providers.gsf.permission.READ_GSERVICES"/>
    val googleAccounts: Array<String?>?
        @RequiresPermission(GET_ACCOUNTS)
        get() = when (app.checkCallingOrSelfPermission(GET_ACCOUNTS)) {
            PackageManager.PERMISSION_GRANTED ->
                AccountManager.get(app).getAccountsByType("com.google").let { accounts ->
                    arrayOfNulls<String>(accounts.size).apply {
                        for ((index, account) in accounts.withIndex()) {
                            this[index] = account.name
                        }
                    }
                }
            else -> null
        }

    object SystemLanguage : LoggerKit {
        fun applyLanguageSystem(activityClz: Class<out Activity?>?) =
            applyLanguageBase(currentLocale, activityClz, true, true)

        fun applyLanguageCustom(locale: Locale, activityClz: Class<out Activity?>?) =
            applyLanguageBase(locale, activityClz, false, true)

        private fun applyLanguageBase(
            locale: Locale, activityClz: Class<out Activity?>?,
            isFollowSystem: Boolean, isNeedStartActivity: Boolean
        ) = activityClz?.let {
            applyLanguageBase(locale, activityClz.name, isFollowSystem, isNeedStartActivity)
        } ?: applyLanguageBase(locale, "", isFollowSystem, isNeedStartActivity)

        private fun applyLanguageBase(
            locale: Locale, activityName: String,
            isFollowSystem: Boolean, isNeedStartActivity: Boolean
        ) {
            when {
                isFollowSystem -> PreferenceKit.put(KEY_LOCALE, VALUE_FOLLOW_SYSTEM)
                else -> PreferenceKit.put(KEY_LOCALE, locale.run { "$language$$country" })
            }
            updateLanguage(app, locale)
            if (isNeedStartActivity) app.startActivity(Intent().apply {
                component = ComponentName(
                    app, if (isSpace(activityName)) launcherActivityName else activityName
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }

        private fun updateLanguage(context: Context, locale: Locale) = context.resources.run {
            configuration.locale.let {
                if (!equals(it.language, locale.language) || !equals(it.country, locale.country)) {
                    when {
                        VersionKit.aboveJellyBeanMR1 -> {
                            configuration.setLocale(locale)
                            context.createConfigurationContext(configuration)
                        }
                        else -> configuration.locale = locale
                    }
                    updateConfiguration(configuration, displayMetrics)
                }
            }
        }

        private fun equals(cs0: CharSequence?, cs1: CharSequence?): Boolean {
            return when {
                cs0 === cs1 -> true
                else -> when {
                    cs0 != null && cs1 != null && cs0.length == cs1.length -> when {
                        cs0 is String && cs1 is String -> cs0 == cs1
                        else -> {
                            for (i in cs0.indices) {
                                if (cs0[i] != cs1[i]) return false
                            }
                            true
                        }
                    }
                    else -> false
                }
            }
        }

        fun applyLanguageSystem(activityName: String) =
            applyLanguageBase(currentLocale, activityName, true, true)

        fun applyLanguageCustom(locale: Locale, activityName: String) =
            applyLanguageBase(locale, activityName, false, true)

        private const val VALUE_FOLLOW_SYSTEM = "VALUE_FOLLOW_SYSTEM"
        val isAppliedLanguageSystem: Boolean
            get() = PreferenceKit[KEY_LOCALE, ""] == VALUE_FOLLOW_SYSTEM
        val applyLanguageSystemInAppOnCreate
            get() = run {
                if (!isAppliedLanguageSystem)
                    applyLanguageBase(currentLocale, "", true, false)
            }
        private const val KEY_LOCALE = "KEY_LOCALE"
        val isAppliedLanguageCustom: Boolean
            get() = StringKit.isNotSpace(PreferenceKit[KEY_LOCALE, ""] as String?)

        fun applyLanguageCustomInAppOnCreate(locale: Locale) {
            if (!isAppliedLanguageCustom) applyLanguageBase(locale, "", false, false)
        }

        fun applyLanguage(activity: Activity) =
            (PreferenceKit[KEY_LOCALE, ""] as String).let { spLocale ->
                if (StringKit.isNotSpace(spLocale)) when (spLocale) {
                    VALUE_FOLLOW_SYSTEM -> {
                        updateLanguage(app, currentLocale)
                        updateLanguage(activity, currentLocale)
                    }
                    else -> spLocale.split("\\$").toTypedArray().run {
                        when (size) {
                            2 -> Locale(this[0], this[1]).let {
                                updateLanguage(app, it)
                                updateLanguage(activity, it)
                            }
                            else -> error("$loggerTag->The string of $spLocale is not in the correct format.")
                        }
                    }
                }
            }
    }
}