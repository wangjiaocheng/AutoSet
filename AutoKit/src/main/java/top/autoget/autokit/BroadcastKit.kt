package top.autoget.autokit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import top.autoget.autokit.NetKit.netWorkType

object BroadcastKit {
    class BroadcastReceiverNetWork : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            netWorkType
        }
    }//网络状态改变广播

    fun registerReceiverNetWork(context: Context): BroadcastReceiverNetWork =
        BroadcastReceiverNetWork().apply {
            IntentFilter().apply { addAction(ConnectivityManager.CONNECTIVITY_ACTION) }
                .let { context.registerReceiver(this, it) }
        }//注册网络状态改变广播
}