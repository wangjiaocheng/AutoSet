package top.autoget.autokit

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiConfiguration.KeyMgmt
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WifiLock
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import top.autoget.autokit.AKit.app
import top.autoget.autokit.NetKit.isConnectedNetwork
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.ThreadKit.poolSingle
import top.autoget.autokit.ToastKit.showShort

object WifiKit {
    fun connectWifi(wifiSsid: String?, wifiPassword: String?) =
        poolSingle?.execute(WifiConnectRunnable(wifiSsid, wifiPassword))

    private val wifiManager: WifiManager = app.wifiManager
    val checkState: Boolean = wifiManager.isWifiEnabled
    val openWifi = wifiManager.run { if (!isWifiEnabled) isWifiEnabled = true }
    val closeWifi = wifiManager.run { if (isWifiEnabled) isWifiEnabled = false }
    var wifiConfigList: MutableList<WifiConfiguration?>? = null
        private set//配置好的网络

    fun hiddenSSID(NetId: Int): WifiConfiguration? =
        wifiConfigList?.get(NetId)?.apply { hiddenSSID = true }

    fun displaySSID(NetId: Int): WifiConfiguration? =
        wifiConfigList?.get(NetId)?.apply { hiddenSSID = false }

    val configWifiList: MutableList<String?> = mutableListOf<String?>().apply {
        wifiConfigList?.let {
            if (it.size > 0) for ((index, wifiConfiguration) in it.withIndex()) {
                add("${index}号--  ${wifiConfiguration?.SSID}")
            }
        }
    }//获取已保存配置的网络

    private class WifiConnectRunnable(
        private val mWifiSsid: String?, private val mWifiPassword: String?
    ) : Runnable {
        @RequiresPermission(ACCESS_FINE_LOCATION)
        override fun run() {
            if (!checkState) {
                openWifi
                checkWifiEnable()
                try {
                    Thread.sleep(2000)//延时等待WiFi自动连接
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    onWifiConnectFinished(false)
                    return
                }
            }
            when {
                checkSSIDState(mWifiSsid) -> checkWifiConnect(mWifiSsid)//如网络已连接，直接上传数据
                else -> {
                    wifiConfigList = startScan()
                    wifiConfigList?.let { isExistSSID(it, mWifiSsid) }
                        ?.let { connectConfigurationWifi(it) }//如果该网络已保存在配置中，直接连到该网络
                        ?: addNetwork(
                            createWifiInfo(
                                mWifiSsid, mWifiPassword, if (isNotSpace(mWifiPassword)) 3 else 1
                            )
                        )
                    checkWifiConnect(mWifiSsid)
                }
            }//检测现有连接是否是配置中路由
        }
    }

    var timeOut: Int = 25//默认连接超时时间
    private fun checkWifiEnable() {
        var timeCount = 0
        while (!checkState) {
            if (timeCount >= timeOut) {
                onWifiConnectFinished(false)
                return
            }//超过25秒退出这次启用
            try {
                Thread.sleep(1000)//每隔1秒检查一次是否成功启用WiFi
            } catch (e: InterruptedException) {
                e.printStackTrace()
                onWifiConnectFinished(false)
                return
            }
            timeCount++
        }
    }

    interface OnWifiConnectStateListener {
        fun onWifiConnectSuccess()
        fun onWifiConnectFailed()
    }

    var onWifiConnectStateListener: OnWifiConnectStateListener? = null
    private fun onWifiConnectFinished(isConnectSuccess: Boolean) = onWifiConnectStateListener?.run {
        if (isConnectSuccess) onWifiConnectSuccess() else onWifiConnectFailed()
    }

    private var mWifiInfo: WifiInfo = wifiManager.connectionInfo
    val wifiInfo: String = mWifiInfo.toString()//得到WifiInfo所有信息包

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun mac(): String {
        if (ActivityCompat.checkSelfPermission(app, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) PermissionKit.BuilderPermissions.launchAppDetailsSettings()
        return mWifiInfo.macAddress ?: "NULL"
    }

    val ip: Int = mWifiInfo.ipAddress
    val currentNetId: Int = mWifiInfo.networkId
    val bssId: String = mWifiInfo.bssid ?: "NULL"
    val ssId: String = mWifiInfo.ssid ?: "NULL"
    fun checkSSIDState(ssid: String?): Boolean {
        mWifiInfo = wifiManager.connectionInfo
        return ssId == "\"$ssid\"" || ssId == ssid
    }//判断某个网络是否连接

    private fun checkWifiConnect(wifiSsid: String?) {
        var timeCount = 0
        while (!isConnectSuccess(wifiSsid)) {
            if (timeCount >= timeOut) {
                onWifiConnectFinished(false)
                return
            }//超过25秒退出这次连接
            try {
                Thread.sleep(1000)//每隔1秒检查一次是否成功连接WiFi
            } catch (e: InterruptedException) {
                e.printStackTrace()
                onWifiConnectFinished(false)
                return
            }
            timeCount++
        }
        showShort("连接路由花费时间：${timeCount}秒")
        onWifiConnectFinished(true)
    }

    fun isConnectSuccess(wifiSsid: String?): Boolean =
        checkState && checkSSIDState(wifiSsid) && isConnectedNetwork//判断WiFi是否连接成功

    var wifiList: MutableList<ScanResult?>? = null
        private set

    fun getSSID(NetId: Int): String? = wifiList?.get(NetId)?.SSID
    fun getBSSID(NetId: Int): String? = wifiList?.get(NetId)?.BSSID//物理地址
    fun getFrequency(NetId: Int): Int? = wifiList?.get(NetId)?.frequency//频率
    fun getLevel(NetId: Int): Int? = wifiList?.get(NetId)?.level//强度
    fun getCapabilities(NetId: Int): String? = wifiList?.get(NetId)?.capabilities//功能
    val scanResultList: MutableList<String?> = mutableListOf<String?>().apply {
        wifiList?.let {
            if (it.size > 0) for ((index, scanResult) in it.withIndex()) {
                add("${index}号--  ${scanResult?.SSID}")
            }
        }
    }//获取扫描结果
    val lookUpScan: String = wifiList?.let {
        StringBuilder().apply {
            for ((index, scanResult) in it.withIndex()) {
                append("编号：${index + 1}:$scanResult\n")
            }
            append("--------------------连接|配置--------------------")
            wifiConfigList?.let { configs ->
                for (value in configs) {
                    append("$value\n")
                }
            }
        }.toString()//查看扫描结果
    } ?: ""//ScanResult信息转换成字符串包包括：BSSID、SSID、capabilities、frequency、level

    fun checkScanResult(ssid: String?): Boolean {
        wifiList?.run {
            if (size > 0) for (scanResult in this) {
                scanResult?.SSID?.let { if (it == ssid || it == "\"$ssid\"") return true }
            }
        }
        return false
    }//判断能不能搜索到指定网络

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun startScan(): MutableList<WifiConfiguration?> = wifiManager.apply { startScan() }.run {
        wifiList = scanResults//扫描结果
        if (ActivityCompat.checkSelfPermission(app, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) PermissionKit.BuilderPermissions.launchAppDetailsSettings()
        configuredNetworks//配置好的网络连接
    }

    fun isExistSSID(
        wifiConfigurations: MutableList<WifiConfiguration?>,
        ssid: String?
    ): WifiConfiguration? {
        for (existingConfig in wifiConfigurations) {
            if (existingConfig?.SSID == "\"$ssid\"" || existingConfig?.SSID == ssid) return existingConfig
        }
        return null
    }//判断某个网络是否已保存在配置中

    fun connectConfigurationWifi(wcg: WifiConfiguration?): Boolean =
        wcg?.networkId?.let { wifiManager.enableNetwork(it, true) } ?: false//连接配置好的WiFi

    fun addNetwork(wcg: WifiConfiguration?): Boolean =
        wifiManager.addNetwork(wcg).let { wifiManager.enableNetwork(it, true) }
            ?: false//添加一个网络并连接

    private fun createWifiInfo(ssid: String?, Password: String?, Type: Int): WifiConfiguration =
        WifiConfiguration().apply {
            allowedKeyManagement.clear()
            allowedAuthAlgorithms.clear()
            allowedGroupCiphers.clear()
            allowedPairwiseCiphers.clear()
            allowedProtocols.clear()
            SSID = "\"$ssid\""
            when (Type) {
                1 -> allowedKeyManagement.set(KeyMgmt.NONE)//WIFICIPHER_NOPASS
                2 -> {
                    allowedKeyManagement.set(KeyMgmt.NONE)
                    allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                    wepTxKeyIndex = 0
                    wepKeys[0] = "\"$Password\""
                    hiddenSSID = true
                }//WIFICIPHER_WEP
                3 -> {
                    allowedKeyManagement.set(KeyMgmt.WPA_PSK)
                    allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                    allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                    allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                    allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                    status = WifiConfiguration.Status.ENABLED
                    preSharedKey = "\"$Password\""
                    hiddenSSID = true
                }//WIFICIPHER_WPA
            }
        }//创建网络连接配置

    fun removeWifi(netId: Int) = wifiManager.removeNetwork(netId)//删除指定ID网络
    fun disconnectWifi(netId: Int) =
        wifiManager.apply { disableNetwork(netId) }.disconnect()//断开指定ID网络

    fun connectConfiguration(index: Int) = wifiConfigList?.run {
        if (index <= size) wifiManager.enableNetwork(get(index)?.networkId ?: 0, true)
    }//指定配置好的网络进行连接

    private var wifiLock: WifiLock? = null
    val releaseWifiLock = wifiLock?.run { if (isHeld) release() }//解锁WifiLock
    fun acquireWifiLock(tag: String? = "lock") {
        if (wifiLock == null) wifiLock = wifiManager.createWifiLock(tag)
        wifiLock?.acquire()
    }//锁定WifiLock

    val release = {
        wifiList?.run { if (size > 0) clear() }
        wifiConfigList?.run { if (size > 0) clear() }
        onWifiConnectStateListener = null
    }
}