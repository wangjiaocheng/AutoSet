package top.autoget.autokit

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import top.autoget.autokit.AKit.app
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.ThreadKit.poolSingle
import top.autoget.autokit.ToastKit.showShort
import java.util.*

object BluetoothKit {
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    fun getBluetoothDevice(address: String?): BluetoothDevice? =
        bluetoothAdapter?.getRemoteDevice(address)
            ?: BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address)//根据地址获取蓝牙设备

    val isOpenBluetooth: Boolean = bluetoothAdapter?.isEnabled ?: false//是否已经开启蓝牙

    interface OnSearchDeviceListener {
        fun onStartDiscovery()//开始扫描设备
        fun onNewDeviceFound(device: BluetoothDevice?): Boolean//发现新设备
        fun onSearchCompleted(
            bondedList: MutableList<BluetoothDevice?>?, newList: MutableList<BluetoothDevice?>?
        )//扫描设备结束
    }//设备扫描监听

    interface OnBluetoothDeviceListener : OnSearchDeviceListener {
        fun onBondStateChanged(device: BluetoothDevice?, intent: Intent?)
        fun onBluetoothOpened()
        fun onBluetoothReOpened()
    }

    var onBluetoothDeviceListener: OnBluetoothDeviceListener? = null

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(BLUETOOTH_CONNECT)
    fun openBluetooth(isReOpen: Boolean) = poolSingle?.execute {
        if (if (isReOpen) reOpenBluetooth() else openBluetooth()) {
            onBluetoothDeviceListener?.run {
                if (isReOpen) onBluetoothReOpened() else onBluetoothOpened()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(BLUETOOTH_CONNECT)
    private fun openBluetooth(): Boolean {
        var result = false
        bluetoothAdapter?.let {
            result = if (it.isEnabled) true else enableBluetooth()
        }
        return result
    }//开启蓝牙（耗时操作）

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(BLUETOOTH_CONNECT)
    private fun reOpenBluetooth(): Boolean {
        bluetoothAdapter?.run {
            if (isEnabled) {
                disable()//关闭蓝牙
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        return enableBluetooth()
    }//重新开启蓝牙（耗时操作）

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(BLUETOOTH_CONNECT)
    private fun enableBluetooth(): Boolean {
        var result = false
        if (bluetoothAdapter != null) {
            bluetoothAdapter.enable()//重新开启蓝牙
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (bluetoothAdapter.isEnabled) result = true
        }
        return result
    }//打开蓝牙开关（耗时操作）

    private var mNewList: MutableList<BluetoothDevice?>? = null//新发现蓝牙设备集合（未绑定）
    private var mBondedList: MutableList<BluetoothDevice?>? = null//已绑定蓝牙设备集合

    private class BlueToothReceiver : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.S)
        @RequiresPermission(BLUETOOTH_CONNECT)
        override fun onReceive(context: Context?, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice?>(BluetoothDevice.EXTRA_DEVICE)
                    if (isCorrectDevice(device)) {
                        onBluetoothDeviceListener?.onNewDeviceFound(device)
                        when (device?.bondState) {
                            BluetoothDevice.BOND_NONE -> mNewList?.add(device)
                            BluetoothDevice.BOND_BONDED -> mBondedList?.add(device)
                        }
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice?>(BluetoothDevice.EXTRA_DEVICE)
                    onBluetoothDeviceListener?.onBondStateChanged(device, intent)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED ->
                    onBluetoothDeviceListener?.onSearchCompleted(mBondedList, mNewList)
            }
        }
    }

    interface IBluetoothDeviceFilter {
        fun isCorrect(device: BluetoothDevice?): Boolean
    }

    var bluetoothDeviceFilter: IBluetoothDeviceFilter? = null//蓝牙设备过滤器
    fun isCorrectDevice(device: BluetoothDevice?): Boolean =
        bluetoothDeviceFilter?.isCorrect(device) ?: true//是否指定蓝牙设备

    @Volatile
    private var mReceiver: BlueToothReceiver? = null
    private var mNeed2unRegister = false
    private val registerBTReceiver = {
        if (mReceiver == null) mReceiver = BlueToothReceiver()
        IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }.let { AKit.app.registerReceiver(mReceiver, it) }
        mNeed2unRegister = true
    }//注册蓝牙广播接收器

    fun stopSearch() {
        if (ActivityCompat.checkSelfPermission(app, BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) PermissionKit.BuilderPermissions.launchAppDetailsSettings()
        bluetoothAdapter?.run { if (isDiscovering) cancelDiscovery() }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(BLUETOOTH_SCAN)
    fun startSearch(): Boolean {
        stopSearch()
        return bluetoothAdapter?.startDiscovery() ?: false
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(allOf = [BLUETOOTH_SCAN, BLUETOOTH_CONNECT])
    fun searchDevices(listener: OnBluetoothDeviceListener?) {
        onBluetoothDeviceListener = listener
        bluetoothAdapter?.let {
            registerBTReceiver
            if (mNewList == null) mNewList = mutableListOf()
            if (mBondedList == null) mBondedList = mutableListOf()
            mNewList?.clear()
            mBondedList?.clear()
            when {
                startSearch() -> onBluetoothDeviceListener?.onStartDiscovery()
                else -> {
                    showShort("蓝牙扫描异常，正在重试")
                    openBluetooth(true)
                }
            }
        }
    }

    @Throws(Exception::class)
    fun createBind(btDevice: BluetoothDevice): Boolean =
        btDevice.javaClass.getMethod("createBond").invoke(btDevice) as Boolean//弹出蓝牙连接方法

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun createBind(btDevice: BluetoothDevice, pingCode: String): Boolean {
        if (ActivityCompat.checkSelfPermission(app, BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) PermissionKit.BuilderPermissions.launchAppDetailsSettings()
        return btDevice.apply { setPin(pingCode.toByteArray()) }.createBond()//蓝牙匹配方法
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun pairBtDevice(address: String?): Boolean {
        var result = false
        stopSearch()
        if (ActivityCompat.checkSelfPermission(app, BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) PermissionKit.BuilderPermissions.launchAppDetailsSettings()
        bluetoothAdapter?.run { if (!isEnabled) enable() }
        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            val device = getBluetoothDevice(address)
            if (isCorrectDevice(device))
                result =
                    device?.bondState == BluetoothDevice.BOND_BONDED || device?.createBond() ?: false
        }//检查蓝牙地址是否有效
        return result
    }//根据地址匹配具体蓝牙设备

    fun isCorrectDevice(address: String?): Boolean =
        isCorrectDevice(getBluetoothDevice(address))//蓝牙设备是否正确

    fun isBluetoothBond(address: String?): Boolean {
        if (ActivityCompat.checkSelfPermission(app, BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) PermissionKit.BuilderPermissions.launchAppDetailsSettings()
        return getBluetoothDevice(address)?.bondState == BluetoothDevice.BOND_BONDED//蓝牙是否已绑定
    }

    fun isBtAddressValid(address: String): Boolean = isNotSpace(address) &&
            BluetoothAdapter.checkBluetoothAddress(address.toUpperCase(Locale.getDefault()))//检验蓝牙地址有效性

    fun release() {
        if (ActivityCompat.checkSelfPermission(app, BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) PermissionKit.BuilderPermissions.launchAppDetailsSettings()
        bluetoothAdapter?.cancelDiscovery()
        if (mNeed2unRegister) {
            AKit.app.unregisterReceiver(mReceiver)
            mNeed2unRegister = false
        }
        mNewList = null
        mBondedList = null
        onBluetoothDeviceListener = null
        mReceiver = null
        bluetoothDeviceFilter = null
    }//资源释放
}