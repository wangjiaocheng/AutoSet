package top.autoget.autokit

import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfiguration.KeyMgmt
import android.net.wifi.WifiManager
import android.os.Build
import top.autoget.autokit.HandleKit.mainHandler
import top.autoget.autokit.NetKit.isConnectedNetwork
import top.autoget.autokit.ThreadKit.poolSingle
import top.autoget.autokit.ToastKit.showShort
import top.autoget.autokit.WifiKit.checkSSIDState
import top.autoget.autokit.WifiKit.checkState
import java.lang.reflect.InvocationTargetException

object ApKit {
    private val wifiManager = AKit.app.wifiManager
    fun isWifiConnectSuccess(ssid: String?): Boolean =
        checkState && checkSSIDState(ssid) && isConnectedNetwork

    private class CloseWifiRunnable : Runnable {
        override fun run() {
            when (wifiManager.wifiState) {
                WifiManager.WIFI_STATE_ENABLED -> {
                    wifiManager.isWifiEnabled = false
                    closeWifiRunnable?.let { mainHandler.postDelayed(it, 100) }
                }
                WifiManager.WIFI_STATE_DISABLING ->
                    closeWifiRunnable?.let { mainHandler.postDelayed(it, 100) }
                WifiManager.WIFI_STATE_DISABLED -> mainHandler.post {
                    startWifiApTh
                    showShort("已关闭WiFi")
                }
            }
        }
    }

    private var closeWifiRunnable: CloseWifiRunnable? = null
    private val closeWifiTh = {
        closeWifiRunnable = CloseWifiRunnable()
        poolSingle?.execute(closeWifiRunnable)
    }//关闭WiFi线程
    private val isVersion = try {
        Build.VERSION.SDK_INT
    } catch (e: NumberFormatException) {
        0
    } >= Build.VERSION_CODES.HONEYCOMB
    private val wifiApStateDisabling = if (isVersion) 10 else 0
    private val wifiApStateDisabled = if (isVersion) 11 else 1
    private val wifiApStateEnabling = if (isVersion) 12 else 2
    private val wifiApStateEnabled = if (isVersion) 13 else 3
    private val wifiApStateFailed = if (isVersion) 14 else 4
    val wifiApState: Int = try {
        wifiManager.javaClass.getMethod("getWifiApState").invoke(wifiManager) as Int
    } catch (e: Exception) {
        wifiApStateFailed
    }
    val isWifiApEnable: Boolean = wifiApState == wifiApStateEnabled

    interface OnWifiAPStatusChangedListener {
        fun onWifiAPStatusChanged(isEnable: Boolean)
    }

    var onWifiAPStatusChangedListener: OnWifiAPStatusChangedListener? = null

    private class StartWifiApRunnable : Runnable {
        override fun run() {
            when (wifiApState) {
                wifiApStateFailed -> mainHandler.post {
                    showShort("打开热点失败，请到系统设置里检查热点状态！")
                    onWifiAPStatusChangedListener?.onWifiAPStatusChanged(false)
                }
                wifiApStateDisabled ->
                    startWifiApRunnable?.let { mainHandler.postDelayed(it, 100) }
                wifiApStateEnabling ->
                    startWifiApRunnable?.let { mainHandler.postDelayed(it, 100) }
                wifiApStateEnabled -> mainHandler.post {
                    showShort("已开启WLAN热点")
                    onWifiAPStatusChangedListener?.onWifiAPStatusChanged(true)
                }
            }
        }
    }

    private var startWifiApRunnable: StartWifiApRunnable? = null
    var wifiAPSsid: String? = null//热点ssid
    var wifiAPPassword: String? = null//热点密码
    private val startWifiApTh = {
        if (!isWifiApEnable) startAp(wifiAPSsid, wifiAPPassword)
        startWifiApRunnable = StartWifiApRunnable()
        poolSingle?.execute(startWifiApRunnable)
    }//开启热点线程
    val startWifiAp = wifiManager.run {
        if (isWifiEnabled) closeWifiTh else startWifiApTh
    }//开启WLAN热点

    private class CloseWifiApRunnable : Runnable {
        override fun run() {
            when (wifiApState) {
                wifiApStateEnabled -> {
                    closeAp(wifiAPSsid, wifiAPPassword)
                    closeWifiApRunnable?.let { mainHandler.postDelayed(it, 100) }
                }
                wifiApStateDisabling, wifiApStateFailed ->
                    closeWifiApRunnable?.let { mainHandler.postDelayed(it, 100) }
                wifiApStateDisabled -> mainHandler.post {
                    showShort("已关闭WLAN热点！")
                    onWifiAPStatusChangedListener?.onWifiAPStatusChanged(false)
                }
            }
        }
    }

    private var closeWifiApRunnable: CloseWifiApRunnable? = null
    private val closeWifiApTh = {
        closeWifiApRunnable = CloseWifiApRunnable()
        poolSingle?.execute(closeWifiApRunnable)
    }//关闭热点线程
    val closeWifiAp = closeWifiApTh//关闭WLAN热点
    fun startAp(wifiApSsid: String?, wifiApPassword: String?) = try {
        wifiManager.javaClass.getMethod(
            "setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType
        ).invoke(wifiManager, getWifiApConfig(wifiApSsid, wifiApPassword), true)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: SecurityException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }

    fun closeAp(wifiAPSsid: String?, wifiAPPassword: String?) = try {
        wifiManager.javaClass.getMethod(
            "setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType
        ).invoke(wifiManager, getWifiApConfig(wifiAPSsid, wifiAPPassword), false)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: SecurityException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }

    val securityType: Int = try {
        KeyMgmt::class.java.getField("WPA2_PSK")[null] as Int
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        0
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
        0
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
        0
    }

    private fun getWifiApConfig(wifiApSsid: String?, wifiApPassword: String?): WifiConfiguration =
        WifiConfiguration().apply {
            SSID = wifiApSsid//名称
            preSharedKey = wifiApPassword//密码
            allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            allowedProtocols.set(WifiConfiguration.Protocol.RSN)
            allowedProtocols.set(WifiConfiguration.Protocol.WPA)
            allowedKeyManagement.set(securityType)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
            allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        }

    val release = {
        mainHandler.removeCallbacksAndMessages(null)
        onWifiAPStatusChangedListener = null
    }//资源释放
}