package top.autoget.autokit

import android.Manifest.permission.*
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.os.Build
import android.os.Build.VERSION_CODES.*
import android.telephony.TelephonyManager
import android.text.format.Formatter
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ShellKit.execCmd
import top.autoget.autokit.VersionKit.aboveGingerbread
import top.autoget.autokit.VersionKit.aboveHoneycomb
import top.autoget.autokit.VersionKit.aboveHoneycombMR2
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.util.*

object NetKit : LoggerKit {
    fun openSettingsWireless(activity: Activity) = activity.startActivityForResult(Intent().apply {
        action = Intent.ACTION_VIEW
        component = ComponentName("com.android.settings", "com.android.settings.WirelessSettings")
    }, 0)

    val settingsWireless
        get() = app.startActivity(Intent().apply {
            action = when {
                aboveHoneycomb -> android.provider.Settings.ACTION_WIRELESS_SETTINGS
                else -> android.provider.Settings.ACTION_SETTINGS
            }//android.provider.Settings.ACTION_WIFI_SETTINGS;android.provider.Settings.ACTION_APN_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    private val activeNetworkInfo: NetworkInfo?
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = app.connectivityManager.activeNetworkInfo
    val currentNetworkState: NetworkInfo.State?
        get() = activeNetworkInfo?.state
    val isConnectingByState: Boolean
        get() = currentNetworkState == NetworkInfo.State.CONNECTING
    val isConnectedByState: Boolean
        get() = currentNetworkState == NetworkInfo.State.CONNECTED
    val isSuspendedByState: Boolean
        get() = currentNetworkState == NetworkInfo.State.SUSPENDED
    val isDisconnectingByState: Boolean
        get() = currentNetworkState == NetworkInfo.State.DISCONNECTING
    val isDisconnectedByState: Boolean
        get() = currentNetworkState == NetworkInfo.State.DISCONNECTED
    val isUnknownByState: Boolean
        get() = currentNetworkState == NetworkInfo.State.UNKNOWN
    val currentNetworkType: Int
        get() = activeNetworkInfo?.type ?: -1
    val isWifiByType: Boolean
        get() = currentNetworkType == ConnectivityManager.TYPE_WIFI
    val isMobileByType: Boolean
        get() = currentNetworkType == ConnectivityManager.TYPE_MOBILE
    val isMobileMmsByType: Boolean
        get() = currentNetworkType == ConnectivityManager.TYPE_MOBILE_MMS
    val isMobileSuplByType: Boolean
        get() = currentNetworkType == ConnectivityManager.TYPE_MOBILE_SUPL
    val isMobileDunByType: Boolean
        get() = currentNetworkType == ConnectivityManager.TYPE_MOBILE_DUN
    val isMobileHipriByType: Boolean
        get() = currentNetworkType == ConnectivityManager.TYPE_MOBILE_HIPRI
    val isWimaxByType: Boolean
        get() = currentNetworkType == ConnectivityManager.TYPE_WIMAX
    val isBluetoothByType: Boolean
        @RequiresApi(HONEYCOMB_MR2)
        get() = if (aboveHoneycombMR2) currentNetworkType == ConnectivityManager.TYPE_BLUETOOTH
        else false
    val isDummyByType: Boolean
        @RequiresApi(ICE_CREAM_SANDWICH)
        get() = if (aboveHoneycombMR2) currentNetworkType == ConnectivityManager.TYPE_DUMMY
        else false//虚拟网络
    val isEthernetByType: Boolean
        @RequiresApi(HONEYCOMB_MR2)
        get() = if (aboveHoneycombMR2) currentNetworkType == ConnectivityManager.TYPE_ETHERNET else false
    val currentNetworkSubtype: Int
        get() = activeNetworkInfo?.subtype ?: -1
    val isEDGEBySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_EDGE
    val isGPRSBySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_GPRS
    val isCDMABySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_CDMA
    val is1XRTTBySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_1xRTT
    val isIDENBySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_IDEN
    val isEVDO_ABySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_EVDO_A
    val isUMTSBySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_UMTS
    val isEVDO_0BySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_EVDO_0
    val isHSDPABySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_HSDPA
    val isHSUPABySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_HSUPA
    val isHSPABySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_HSPA
    val isEVDO_BBySubtype: Boolean
        @RequiresApi(GINGERBREAD)
        get() = if (aboveGingerbread) currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_EVDO_B else false//EDGE
    val isEHRPDBySubtype: Boolean
        @RequiresApi(HONEYCOMB)
        get() = if (aboveHoneycomb) currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_EHRPD else false
    val isHSPAPBySubtype: Boolean
        @RequiresApi(HONEYCOMB_MR2)
        get() = if (aboveHoneycombMR2) currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_HSPAP
        else false
    val isLTEBySubtype: Boolean
        @RequiresApi(HONEYCOMB)
        get() = if (aboveHoneycomb) currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_LTE else false
    val isUNKNOWNBySubtype: Boolean
        get() = currentNetworkSubtype == TelephonyManager.NETWORK_TYPE_UNKNOWN
    val isChinaMobile2G: Boolean
        get() = isEDGEBySubtype
    val isChinaUnicom2G: Boolean
        get() = isGPRSBySubtype
    val isChinaTelecom2G: Boolean
        get() = isCDMABySubtype
    val isChinaUnicom3G: Boolean
        get() = app.run { isHSDPABySubtype || isUMTSBySubtype }
    val isChinaTelecom3G: Boolean
        get() = app.run { isEVDO_0BySubtype || isEVDO_ABySubtype || isEVDO_BBySubtype }

    enum class NetworkType {
        NETWORK_ETHERNET, NETWORK_WIFI, NETWORK_4G, NETWORK_3G, NETWORK_2G, NETWORK_UNKNOWN, NETWORK_NO
    }

    val netWorkType: NetworkType
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = when {
            isEthernet -> NetworkType.NETWORK_ETHERNET
            else -> activeNetworkInfo?.let {
                when {
                    it.isAvailable -> when (it.type) {
                        ConnectivityManager.TYPE_WIFI -> NetworkType.NETWORK_WIFI
                        ConnectivityManager.TYPE_MOBILE -> when (it.subtype) {
                            TelephonyManager.NETWORK_TYPE_IWLAN/*WAP*/, TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.NETWORK_4G
                            TelephonyManager.NETWORK_TYPE_TD_SCDMA/*WAP*/, TelephonyManager.NETWORK_TYPE_EVDO_A/*电信*/,
                            TelephonyManager.NETWORK_TYPE_UMTS/*联通*/, TelephonyManager.NETWORK_TYPE_EVDO_0/*电信*/,
                            TelephonyManager.NETWORK_TYPE_HSDPA/*联通*/, TelephonyManager.NETWORK_TYPE_HSUPA,
                            TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B/*电信*/,
                            TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> NetworkType.NETWORK_3G
                            TelephonyManager.NETWORK_TYPE_GSM/*WAP*/, TelephonyManager.NETWORK_TYPE_EDGE/*移动*/,
                            TelephonyManager.NETWORK_TYPE_GPRS/*联通*/, TelephonyManager.NETWORK_TYPE_CDMA/*电信*/,
                            TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> NetworkType.NETWORK_2G
                            else -> it.subtypeName.let { name ->
                                when {
                                    name.equals("TD-SCDMA", true)
                                            || name.equals("WCDMA", true)
                                            || name.equals("CDMA2000", true) ->
                                        NetworkType.NETWORK_3G
                                    else -> NetworkType.NETWORK_UNKNOWN
                                }
                            }
                        }
                        else -> NetworkType.NETWORK_UNKNOWN
                    }
                    else -> NetworkType.NETWORK_NO
                }
            } ?: NetworkType.NETWORK_NO
        }
    val netWorkTypeName: String
        get() = when (netWorkType) {
            NetworkType.NETWORK_ETHERNET -> "NETWORK_ETHERNET"
            NetworkType.NETWORK_WIFI -> "NETWORK_WIFI"
            NetworkType.NETWORK_4G -> "NETWORK_4G"
            NetworkType.NETWORK_3G -> "NETWORK_3G"
            NetworkType.NETWORK_2G -> "NETWORK_2G"
            NetworkType.NETWORK_UNKNOWN -> "NETWORK_UNKNOWN"
            NetworkType.NETWORK_NO -> "NETWORK_NO"
        }
    private val isEthernet: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = app.connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)?.state?.let {
            it == NetworkInfo.State.CONNECTED || it == NetworkInfo.State.CONNECTING
        } ?: false
    private const val TYPE_NO = 0//WAP：手机；NET：PC、笔记本电脑、PDA
    private const val TYPE_MOBILE_CMWAP = 1
    private const val TYPE_MOBILE_CMNET = 2
    private const val TYPE_MOBILE_UNIWAP = 3
    private const val TYPE_MOBILE_UNINET = 4
    private const val TYPE_MOBILE_3GWAP = 5
    private const val TYPE_MOBLIE_3GNET = 6
    private const val TYPE_MOBILE_CTWAP = 7
    private const val TYPE_MOBILE_CTNET = 8
    private const val TYPE_WIFI = 10
    val networkState: Int
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = app.connectivityManager.activeNetworkInfo?.run {
            when {
                isAvailable -> when (type) {
                    ConnectivityManager.TYPE_MOBILE -> extraInfo?.let {
                        when (it) {
                            "cmwap" -> TYPE_MOBILE_CMWAP.apply { info("$loggerTag->当前中国移动CMWAP网络") }
                            "cmnet" -> TYPE_MOBILE_CMNET.apply { info("$loggerTag->当前中国移动CMNET网络") }
                            "uniwap" -> TYPE_MOBILE_UNIWAP.apply { info("$loggerTag->当前中国联通UNIWAP网络") }
                            "uninet" -> TYPE_MOBILE_UNINET.apply { info("$loggerTag->当前中国联通UNINET网络") }
                            "3gwap" -> TYPE_MOBILE_3GWAP.apply { info("$loggerTag->当前中国联通3GWAP网络") }
                            "3gnet" -> TYPE_MOBLIE_3GNET.apply { info("$loggerTag->当前中国联通3GNET网络") }
                            "ctwap" -> TYPE_MOBILE_CTWAP.apply { info("$loggerTag->当前中国电信CTWAP网络") }
                            "ctnet" -> TYPE_MOBILE_CTNET.apply { info("$loggerTag->当前中国电信CTNET网络") }
                            else -> TYPE_NO.apply { info("$loggerTag->当前不被考虑网络") }
                        }
                    }
                    ConnectivityManager.TYPE_WIFI -> TYPE_WIFI.apply { info("$loggerTag->当前WIFI网络") }
                    else -> TYPE_NO.apply { info("$loggerTag->当前不被考虑网络") }
                }
                else -> TYPE_NO.apply { info("$loggerTag->当前不被考虑网络") }
            }
        } ?: TYPE_NO.apply { info("$loggerTag->当前不被考虑网络") }
    val isConnectedWifiOrMobile: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = app.connectivityManager.run {
            activeNetworkInfo?.let {
                when (it.type) {
                    ConnectivityManager.TYPE_MOBILE -> {
                        info("$loggerTag->网络连接类型：TYPE_MOBILE")
                        when (getNetworkInfo(ConnectivityManager.TYPE_MOBILE)?.state) {
                            NetworkInfo.State.CONNECTED -> it.isAvailable.apply { info("$loggerTag->网络连接类型：TYPE_MOBILE连接成功！") }
                            else -> false
                        }
                    }
                    ConnectivityManager.TYPE_WIFI -> {
                        info("$loggerTag->网络连接类型：TYPE_WIFI")
                        when (getNetworkInfo(ConnectivityManager.TYPE_WIFI)?.state) {
                            NetworkInfo.State.CONNECTED -> it.isAvailable.apply { info("$loggerTag->网络连接类型：TYPE_WIFI连接成功！") }
                            else -> false
                        }
                    }
                    else -> false
                }
            } ?: false
        }
    val isConnectedNetwork: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() {
            for (networkInfo in app.connectivityManager.allNetworkInfo) {
                if (networkInfo.state == NetworkInfo.State.CONNECTED) return true
            }
            return false
        }
    val isConnected: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = activeNetworkInfo?.isConnected ?: false
    val isAvailable: Boolean
        get() = activeNetworkInfo?.isAvailable ?: false
    val isAvailableByPing: Boolean
        @RequiresPermission(INTERNET)
        get() = isAvailableByPing(null)

    @RequiresPermission(INTERNET)
    fun isAvailableByPing(ip: String?): Boolean = execCmd(
        String.format("ping -c 1 %s", if (ip == null || ip.isEmpty()) "223.5.5.5" else ip), false
    ).let {
        (it.result == 0).apply {
            debug("$loggerTag->isAvailableByPing() called${it.errorMsg}")
            debug("$loggerTag->isAvailableByPing() called${it.successMsg}")
        }
    }

    val ping: Boolean
        @RequiresPermission(INTERNET)
        get() = ping(null)//是否连接外网

    @RequiresPermission(INTERNET)
    fun ping(ip: String?): Boolean {
        var result = ""
        return try {
            Runtime.getRuntime()
                .exec("ping -c 3 -warn 100 ${if (ip == null || ip.isEmpty()) "223.5.5.5" else ip}")
                .run {
                    inputStream.use { stream ->
                        InputStreamReader(stream).use { inputStreamReader ->
                            BufferedReader(inputStreamReader).use { bufferedReader ->
                                StringBuffer().apply {
                                    while (true) {
                                        bufferedReader.readLine()?.let { append(it) } ?: break
                                    }
                                    debug("ping->result content : $this")
                                }
                            }
                        }
                    }
                    when {
                        waitFor() == 0 -> true.apply { result = "success" }
                        else -> false.apply { result = "failed" }
                    }
                }
        } catch (e: IOException) {
            false.apply { result = "IOException" }
        } catch (e: InterruptedException) {
            false.apply { result = "InterruptedException" }
        } finally {
            debug("result->result = $result")
        }
    }//主线程使用会阻塞

    var wifiEnabled: Boolean
        @RequiresPermission(ACCESS_WIFI_STATE)
        get() = app.wifiManager.isWifiEnabled//WifiManager.WIFI_STATE_ENABLED||WifiManager.WIFI_STATE_ENABLING
        @RequiresPermission(CHANGE_WIFI_STATE)
        set(enabled) = app.wifiManager.run {
            if (isWifiEnabled != enabled) isWifiEnabled = enabled
        }
    val isWifiAvailable: Boolean
        @RequiresPermission(allOf = [ACCESS_WIFI_STATE, INTERNET])
        get() = wifiEnabled && isAvailableByPing
    val isWifiConnected: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = activeNetworkInfo?.run { type == ConnectivityManager.TYPE_WIFI } ?: false
    val wifiState: Int
        @Throws(Exception::class)
        get() = app.wifiManager.wifiState
    val wifiConnectionInfo: WifiInfo
        get() = app.wifiManager.connectionInfo
    val wifiScanResults: MutableList<ScanResult>?
        @RequiresPermission(CHANGE_WIFI_STATE)
        get() = app.wifiManager.run { if (startScan()) scanResults else null }//wifi列表

    @RequiresPermission(CHANGE_WIFI_STATE)
    fun getWifiScanResultsByBSSID(bssId: String): ScanResult? = app.wifiManager.run {
        when {
            startScan() -> {
                for (scanResult in scanResults) {
                    if (scanResult.BSSID == bssId) return scanResult
                }
                return null
            }
            else -> getWifiScanResultsByBSSID(bssId)
        }
    }//过滤wifi列表

    val gateWayByWifi: String
        @RequiresPermission(ACCESS_WIFI_STATE)
        get() = Formatter.formatIpAddress(app.wifiManager.dhcpInfo.gateway)//网关
    val netMaskByWifi: String
        @RequiresPermission(ACCESS_WIFI_STATE)
        get() = Formatter.formatIpAddress(app.wifiManager.dhcpInfo.netmask)//掩码
    val ipAddressByWifi: String
        @RequiresPermission(ACCESS_WIFI_STATE)
        get() = Formatter.formatIpAddress(app.wifiManager.dhcpInfo.ipAddress)
    val serverAddressByWifi: String
        @RequiresPermission(ACCESS_WIFI_STATE)
        get() = Formatter.formatIpAddress(app.wifiManager.dhcpInfo.serverAddress)
    val is4GConnected: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = activeNetworkInfo?.run { isAvailable && subtype == TelephonyManager.NETWORK_TYPE_LTE }
            ?: false
    val isDataEnabled: Boolean
        @RequiresPermission(MODIFY_PHONE_STATE)
        get() = try {
            app.telephonyManager.run {
                when {
                    Build.VERSION.SDK_INT >= O -> isDataEnabled
                    else -> javaClass.getDeclaredMethod("getDataEnabled").invoke(this) as Boolean
                }
            }
        } catch (e: Exception) {
            false.apply { error("$loggerTag->isMobileDataEnabled: $e") }
        }

    @RequiresPermission(MODIFY_PHONE_STATE)
    fun setDataEnabled(enabled: Boolean): Boolean = try {
        app.telephonyManager.run {
            when {
                Build.VERSION.SDK_INT >= O -> true.apply {
                    isDataEnabled = enabled
                }
                else -> {
                    javaClass.getDeclaredMethod("setDataEnabled", Boolean::class.javaPrimitiveType)
                        .invoke(this, enabled)
                    true
                }
            }
        }
    } catch (e: Exception) {
        false.apply { error("$loggerTag->setMobileDataEnabled: $e") }
    }

    fun setMobileDataEnabled(enabled: Boolean) = try {
        app.connectivityManager.run {
            Class.forName(javaClass.name).getDeclaredField("mService").apply { isAccessible = true }
                .get(this).let {
                    Class.forName(it.javaClass.name)
                        .getDeclaredMethod("setMobileDataEnabled", Boolean::class.java)
                        .apply { isAccessible = true }.invoke(it, enabled)
                }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val isDataOpen: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = app.connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)?.isConnected
            ?: false
    val isDataConnected: Boolean
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = activeNetworkInfo?.run { isAvailable && type == ConnectivityManager.TYPE_MOBILE }
            ?: false

    @RequiresPermission(INTERNET)
    fun getAddressDomain(domain: String): String = try {
        InetAddress.getByName(domain).hostAddress
    } catch (e: UnknownHostException) {
        e.printStackTrace()
        ""
    }

    val addressProxy: String?
        @RequiresPermission(ACCESS_NETWORK_STATE)
        get() = app.connectivityManager.activeNetworkInfo?.run {
            when {
                isAvailable -> extraInfo?.let {
                    when (it) {
                        "cmwap", "uniwap" -> "10.0.0.172:80"
                        "ctwap" -> "10.0.0.200:80"
                        else -> null
                    }
                }
                else -> null
            }
        }
    val ipBroadcast: String
        get() = try {
            for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
                if (networkInterface.isUp && !networkInterface.isLoopback)
                    for (interfaceAddress in networkInterface.interfaceAddresses) {
                        interfaceAddress.broadcast?.run { return hostAddress }
                    }
            }
            ""
        } catch (e: SocketException) {
            e.printStackTrace()
            ""
        }
    val ipWifi: String
        get() = int2Ip(app.wifiManager.connectionInfo.ipAddress)

    private fun int2Ip(int: Int): String =
        "${(int and 0xFF)}.${int shr 8 and 0xFF}.${int shr 16 and 0xFF}.${int shr 24 and 0xFF}"

    val ipGprs: String?
        get() = getIpGprs(true)
    private val inetAddress: InetAddress?
        get() {
            try {
                for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
                    if (networkInterface.isUp && !networkInterface.isLoopback)
                        for (inetAddress in networkInterface.inetAddresses) {
                            if (!inetAddress.isLoopbackAddress) return inetAddress
                        }
                }
            } catch (e: SocketException) {
                e.printStackTrace()
            }
            return null
        }//防止小米手机返回10.0.2.15

    fun getIpGprs(useIPv4: Boolean): String? = inetAddress?.hostAddress?.run {
        when {
            useIPv4 && indexOf(':') < 0 -> this
            else -> indexOf('%').let { index ->
                when {
                    index < 0 -> toUpperCase(Locale.getDefault())
                    else -> substring(0, index).toUpperCase(Locale.getDefault())
                }
            }
        }
    }

    val addressIp: String?
        @RequiresPermission(READ_PHONE_STATE)
        get() = app.wifiManager.run { if (isWifiEnabled) ipWifi else ipGprs }
    val macBluetooth: String?
        @RequiresPermission(BLUETOOTH)
        get() = try {
            when (app.checkCallingOrSelfPermission(BLUETOOTH)) {
                PackageManager.PERMISSION_GRANTED -> BluetoothAdapter.getDefaultAdapter().address
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    val addressMac: String
        @RequiresPermission(allOf = [ACCESS_WIFI_STATE, INTERNET])
        get() = getAddressMac()
    private val macByNetworkInterface: String
        get() {
            try {
                for (networkInterface in NetworkInterface.getNetworkInterfaces()) {
                    if (networkInterface?.name?.equals("wlan0", true) == true)
                        networkInterface.hardwareAddress?.let { bytes ->
                            if (bytes.isNotEmpty()) StringBuilder().run {
                                for (byte in bytes) {
                                    append(String.format("%02x:", byte))
                                }
                                substring(0, length - 1)
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "02:00:00:00:00:00"
        }
    private val macByInetAddress: String
        get() {
            try {
                inetAddress?.let {
                    NetworkInterface.getByInetAddress(it)?.hardwareAddress?.let { bytes ->
                        if (bytes.isNotEmpty()) StringBuilder().run {
                            for (byte in bytes) {
                                append(String.format("%02x:", byte))
                            }
                            substring(0, length - 1)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return "02:00:00:00:00:00"
        }
    private val macByWifiInfo: String
        @RequiresPermission(ACCESS_FINE_LOCATION)
        get() = try {
            if (ActivityCompat.checkSelfPermission(app, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) PermissionKit.BuilderPermissions.launchAppDetailsSettings()
            app.wifiManager.connectionInfo?.macAddress ?: "02:00:00:00:00:00"
        } catch (e: Exception) {
            e.printStackTrace()
            "02:00:00:00:00:00"
        }
    private val macByFile: String
        get() {
            execCmd("getprop wifi.interface", false).let { result ->
                if (result.result == 0)
                    execCmd("cat /sys/class/net/${result.successMsg}/address", false)
                        .let { if (it.result == 0) it.successMsg.run { if (isNotEmpty()) return this } }
            }
            return "02:00:00:00:00:00"
        }

    @RequiresPermission(allOf = [ACCESS_WIFI_STATE, INTERNET])
    fun getAddressMac(vararg excepts: String): String = when {
        isMacNotInExcepts(macByNetworkInterface, *excepts) -> macByNetworkInterface
        isMacNotInExcepts(macByInetAddress, *excepts) -> macByInetAddress
        isMacNotInExcepts(macByWifiInfo, *excepts) -> macByWifiInfo
        isMacNotInExcepts(macByFile, *excepts) -> macByFile
        else -> ""
    }

    private fun isMacNotInExcepts(address: String, vararg excepts: String): Boolean {
        when {
            excepts.isEmpty() -> return "02:00:00:00:00:00" != address
            else -> {
                for (filter in excepts) {
                    if (filter == address) return false
                }
                return true
            }
        }
    }
}