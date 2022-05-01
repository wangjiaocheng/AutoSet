package top.autoget.autopay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.debug
import top.autoget.autopay.PayKit.AutoPay.Companion.newInstance

class PayWxEntryActivity : AppCompatActivity(), IWXAPIEventHandler, LoggerKit {
    private var iwxapi: IWXAPI? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iwxapi = WXAPIFactory.createWXAPI(this, newInstance(null)?.weChatAppID)
        iwxapi?.handleIntent(intent, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        iwxapi?.handleIntent(intent, this)
    }

    override fun onReq(baseReq: BaseReq?) = debug("$loggerTag->微信发送的请求回调")
    override fun onResp(baseResp: BaseResp?) = baseResp?.errCode?.let { sendPayResultBroadcast(it) }
        ?: Unit//发送到微信请求的响应结果回调

    private fun sendPayResultBroadcast(resultCode: Int) {
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(Intent().apply {
            action = PayKit.WeChatPayStrategy.WECHAT_PAY_RESULT_ACTION
            putExtra(PayKit.WeChatPayStrategy.WECHAT_PAY_RESULT_EXTRA, resultCode)
        })
        finish()
    }//本地广播比全局广播高效安全
}