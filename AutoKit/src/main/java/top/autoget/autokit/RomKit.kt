package top.autoget.autokit

import android.os.Build
import android.os.Environment
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.VersionKit.abovePie
import java.io.*
import java.util.*

object RomKit {
    private const val VERSION_PROPERTY_HUAWEI = "ro.build.version.emui"
    private const val VERSION_PROPERTY_VIVO = "ro.vivo.os.build.display.id"
    private const val VERSION_PROPERTY_XIAOMI = "ro.build.version.incremental"
    private const val VERSION_PROPERTY_OPPO = "ro.build.version.opporom"
    private const val VERSION_PROPERTY_LEECO = "ro.letv.release.version"
    private const val VERSION_PROPERTY_360 = "ro.build.uiversion"
    private const val VERSION_PROPERTY_ZTE = "ro.build.MiFavor_version"
    private const val VERSION_PROPERTY_ONEPLUS = "ro.rom.version"
    private const val VERSION_PROPERTY_NUBIA = "ro.build.rom.id"
    private const val UNKNOWN = "unknown"
    private val ROM_HUAWEI: Array<String> = arrayOf("huawei")
    private val ROM_VIVO: Array<String> = arrayOf("vivo")
    private val ROM_XIAOMI: Array<String> = arrayOf("xiaomi")
    private val ROM_OPPO: Array<String> = arrayOf("oppo")
    private val ROM_LEECO: Array<String> = arrayOf("leeco", "letv")
    private val ROM_360: Array<String> = arrayOf("360", "qiku")
    private val ROM_ZTE: Array<String> = arrayOf("zte")
    private val ROM_ONEPLUS: Array<String> = arrayOf("oneplus")
    private val ROM_NUBIA: Array<String> = arrayOf("nubia")
    private val ROM_COOLPAD: Array<String> = arrayOf("coolpad", "yulong")
    private val ROM_LG: Array<String> = arrayOf("lg", "lge")
    private val ROM_GOOGLE: Array<String> = arrayOf("google")
    private val ROM_SAMSUNG: Array<String> = arrayOf("samsung")
    private val ROM_MEIZU: Array<String> = arrayOf("meizu")
    private val ROM_LENOVO: Array<String> = arrayOf("lenovo")
    private val ROM_SMARTISAN: Array<String> = arrayOf("smartisan")
    private val ROM_HTC: Array<String> = arrayOf("htc")
    private val ROM_SONY: Array<String> = arrayOf("sony")
    private val ROM_AMIGO: Array<String> = arrayOf("amigo")
    val isHuawei: Boolean
        get() = ROM_HUAWEI[0] == romInfo.name
    val isVivo: Boolean
        get() = ROM_VIVO[0] == romInfo.name
    val isXiaomi: Boolean
        get() = ROM_XIAOMI[0] == romInfo.name
    val isOppo: Boolean
        get() = ROM_OPPO[0] == romInfo.name
    val isLeeco: Boolean
        get() = ROM_LEECO[0] == romInfo.name
    val is360: Boolean
        get() = ROM_360[0] == romInfo.name
    val isZte: Boolean
        get() = ROM_ZTE[0] == romInfo.name
    val isOneplus: Boolean
        get() = ROM_ONEPLUS[0] == romInfo.name
    val isNubia: Boolean
        get() = ROM_NUBIA[0] == romInfo.name
    val isCoolpad: Boolean
        get() = ROM_COOLPAD[0] == romInfo.name
    val isLg: Boolean
        get() = ROM_LG[0] == romInfo.name
    val isGoogle: Boolean
        get() = ROM_GOOGLE[0] == romInfo.name
    val isSamsung: Boolean
        get() = ROM_SAMSUNG[0] == romInfo.name
    val isMeizu: Boolean
        get() = ROM_MEIZU[0] == romInfo.name
    val isLenovo: Boolean
        get() = ROM_LENOVO[0] == romInfo.name
    val isSmartisan: Boolean
        get() = ROM_SMARTISAN[0] == romInfo.name
    val isHtc: Boolean
        get() = ROM_HTC[0] == romInfo.name
    val isSony: Boolean
        get() = ROM_SONY[0] == romInfo.name
    val isAmigo: Boolean
        get() = ROM_AMIGO[0] == romInfo.name

    data class RomInfo(var version: String? = null, var name: String? = null)

    private val romInfo: RomInfo
        get() = RomInfo().apply {
            when {
                isRightRom(brand, manufacturer, *ROM_HUAWEI) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_HUAWEI).let { version ->
                        version.split("_".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray().let { if (it.size > 1) it[1] else version }
                    }
                    name = ROM_HUAWEI[0]
                }
                isRightRom(brand, manufacturer, *ROM_VIVO) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_VIVO)
                    name = ROM_VIVO[0]
                }
                isRightRom(brand, manufacturer, *ROM_XIAOMI) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_XIAOMI)
                    name = ROM_XIAOMI[0]
                }
                isRightRom(brand, manufacturer, *ROM_OPPO) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_OPPO)
                    name = ROM_OPPO[0]
                }
                isRightRom(brand, manufacturer, *ROM_LEECO) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_LEECO)
                    name = ROM_LEECO[0]
                }
                isRightRom(brand, manufacturer, *ROM_360) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_360)
                    name = ROM_360[0]
                }
                isRightRom(brand, manufacturer, *ROM_ZTE) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_ZTE)
                    name = ROM_ZTE[0]
                }
                isRightRom(brand, manufacturer, *ROM_ONEPLUS) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_ONEPLUS)
                    name = ROM_ONEPLUS[0]
                }
                isRightRom(brand, manufacturer, *ROM_NUBIA) -> apply {
                    version = getRomVersion(VERSION_PROPERTY_NUBIA)
                    name = ROM_NUBIA[0]
                }
                else -> apply {
                    version = getRomVersion()
                    name = when {
                        isRightRom(brand, manufacturer, *ROM_COOLPAD) -> ROM_COOLPAD[0]
                        isRightRom(brand, manufacturer, *ROM_LG) -> ROM_LG[0]
                        isRightRom(brand, manufacturer, *ROM_GOOGLE) -> ROM_GOOGLE[0]
                        isRightRom(brand, manufacturer, *ROM_SAMSUNG) -> ROM_SAMSUNG[0]
                        isRightRom(brand, manufacturer, *ROM_MEIZU) -> ROM_MEIZU[0]
                        isRightRom(brand, manufacturer, *ROM_LENOVO) -> ROM_LENOVO[0]
                        isRightRom(brand, manufacturer, *ROM_SMARTISAN) -> ROM_SMARTISAN[0]
                        isRightRom(brand, manufacturer, *ROM_HTC) -> ROM_HTC[0]
                        isRightRom(brand, manufacturer, *ROM_SONY) -> ROM_SONY[0]
                        isRightRom(brand, manufacturer, *ROM_AMIGO) -> ROM_AMIGO[0]
                        else -> manufacturer
                    }
                }
            }
        }
    private val brand: String
        get() = try {
            when {
                isSpace(Build.BRAND) -> UNKNOWN
                else -> Build.BRAND.toLowerCase(Locale.getDefault())
            }
        } catch (ignore: Throwable) {
            UNKNOWN
        }
    private val manufacturer: String
        get() = try {
            when {
                isSpace(Build.MANUFACTURER) -> UNKNOWN
                else -> Build.MANUFACTURER.toLowerCase(Locale.getDefault())
            }
        } catch (ignore: Throwable) {
            UNKNOWN
        }

    private fun isRightRom(brand: String, manufacturer: String, vararg names: String): Boolean {
        for (name in names) {
            if (brand.contains(name) || manufacturer.contains(name)) return true
        }
        return false
    }

    private fun getRomVersion(propertyName: String = ""): String = try {
        when {
            isNotSpace(propertyName) -> getSystemProperty(propertyName)
            isNotSpace(Build.DISPLAY) -> Build.DISPLAY.toLowerCase(Locale.getDefault())
            else -> ""
        }.let { if (isSpace(it)) UNKNOWN else it }
    } catch (ignore: Throwable) {
        ""
    }

    private fun getSystemProperty(propertyName: String): String = arrayOf(
        getSystemPropertyByShell(propertyName), getSystemPropertyByStream(propertyName)
    ).let {
        for (property in it) {
            if (isNotSpace(property)) return property
        }
        if (abovePie) "" else getSystemPropertyByReflect(propertyName)
    }

    private fun getSystemPropertyByShell(propertyName: String): String = try {
        Runtime.getRuntime().exec("getprop $propertyName").inputStream.use {
            InputStreamReader(it).use { inputStreamReader ->
                BufferedReader(inputStreamReader, 1024).use { bufferedReader ->
                    bufferedReader.readLine() ?: ""
                }
            }
        }
    } catch (ignore: IOException) {
        ""
    }

    private fun getSystemPropertyByStream(key: String): String = try {
        FileInputStream(File(Environment.getRootDirectory(), "build.prop")).use {
            Properties().apply { load(it) }.getProperty(key, "")
        }
    } catch (ignore: Exception) {
        ""
    }

    private fun getSystemPropertyByReflect(key: String): String = try {
        "android.os.SystemProperties"::class.java.let {
            it.getMethod("get", String::class.java, String::class.java)
                .invoke(it, key, "") as String
        }
    } catch (e: Exception) {
        ""
    }
}