package top.autoget.autokit

import android.os.Handler
import android.os.Looper
import android.os.Message
import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpResponseException
import org.ksoap2.transport.HttpTransportSE
import org.xmlpull.v1.XmlPullParserException
import top.autoget.autokit.ThreadKit.getPoolFixed
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

object WebServiceKit {
    interface WebServiceCallBack {
        fun callBack(result: Any)
    }

    private const val NAMESPACE = ""
    private const val WEB_SERVER_URL = ""
    private val executorService: ExecutorService =
        getPoolFixed(3) ?: Executors.newFixedThreadPool(3)

    fun callWebService(
        urlStr: String, methodName: String, properties: MutableMap<String, String>?,
        webServiceCallBack: WebServiceCallBack
    ): Future<*> = SoapObject(NAMESPACE, methodName).apply {
        properties?.let {
            for ((key, value) in it) {
                addProperty(key, value)
            }//传递参数必须arg开头
        }
    }.let { soapObject ->
        SoapSerializationEnvelope(SoapEnvelope.VER12).apply {
            dotNet = false//调用.Net的WebService为true
            bodyOut = soapObject
            setOutputSoapObject(soapObject)
        }.let {
            executorService.submit {
                var resultSoapObject: Any? = null
                try {
                    HttpTransportSE(urlStr).apply { debug = true }
                        .call("$WEB_SERVER_URL$methodName", it)
                    it.response?.run { resultSoapObject = it.bodyOut }
                } catch (e: HttpResponseException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                } finally {
                    object : Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            super.handleMessage(msg)
                            webServiceCallBack.callBack(msg.obj as Any)
                        }
                    }.run { sendMessage(obtainMessage(0, resultSoapObject)) }
                }
            }
        }//tomcat7.055for64、jdk1.6for64、web3.0填写ver12；ver11报错http415
    }
}