package top.autoget.automap

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.amap.api.services.core.AMapException
import top.autoget.autokit.HandleKit.backgroundHandler
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.ToastKit.showShort
import top.autoget.autokit.info

object MapErrorToast : LoggerKit {
    private var toast: Toast? = null
    private val runnable = Runnable {
        toast?.run {
            cancel()
            toast = null
        }
    }

    @JvmStatic
    fun showTvToast(context: Context?, message: String?) {
        toast?.let { backgroundHandler.postDelayed(runnable, 0) } ?: run {
            toast = Toast(context).apply {
                duration = Toast.LENGTH_SHORT
                setGravity(Gravity.BOTTOM, 0, 150)
                view = TextView(context).apply {
                    text = message
                    setBackgroundColor(Color.BLACK)
                    setPadding(10, 10, 10, 10)
                }
            }
        }//不到一秒，还未取消，立即取消
        backgroundHandler.postDelayed(runnable, 1000)
        toast?.show()
    }

    fun showError(rCode: Int) {
        try {
            when (rCode) {
                1001 -> throw AMapException(AMapException.AMAP_SIGNATURE_ERROR)
                1002 -> throw AMapException(AMapException.AMAP_INVALID_USER_KEY)
                1003 -> throw AMapException(AMapException.AMAP_SERVICE_NOT_AVAILBALE)
                1004 -> throw AMapException(AMapException.AMAP_DAILY_QUERY_OVER_LIMIT)
                1005 -> throw AMapException(AMapException.AMAP_ACCESS_TOO_FREQUENT)
                1006 -> throw AMapException(AMapException.AMAP_INVALID_USER_IP)
                1007 -> throw AMapException(AMapException.AMAP_INVALID_USER_DOMAIN)
                1008 -> throw AMapException(AMapException.AMAP_INVALID_USER_SCODE)
                1009 -> throw AMapException(AMapException.AMAP_USERKEY_PLAT_NOMATCH)
                1010 -> throw AMapException(AMapException.AMAP_IP_QUERY_OVER_LIMIT)
                1011 -> throw AMapException(AMapException.AMAP_NOT_SUPPORT_HTTPS)
                1012 -> throw AMapException(AMapException.AMAP_INSUFFICIENT_PRIVILEGES)
                1013 -> throw AMapException(AMapException.AMAP_USER_KEY_RECYCLED)
                1100 -> throw AMapException(AMapException.AMAP_ENGINE_RESPONSE_ERROR)
                1101 -> throw AMapException(AMapException.AMAP_ENGINE_RESPONSE_DATA_ERROR)
                1102 -> throw AMapException(AMapException.AMAP_ENGINE_CONNECT_TIMEOUT)
                1103 -> throw AMapException(AMapException.AMAP_ENGINE_RETURN_TIMEOUT)
                1200 -> throw AMapException(AMapException.AMAP_SERVICE_INVALID_PARAMS)
                1201 -> throw AMapException(AMapException.AMAP_SERVICE_MISSING_REQUIRED_PARAMS)
                1202 -> throw AMapException(AMapException.AMAP_SERVICE_ILLEGAL_REQUEST)
                1203 -> throw AMapException(AMapException.AMAP_SERVICE_UNKNOWN_ERROR)
                1800 -> throw AMapException(AMapException.AMAP_CLIENT_ERRORCODE_MISSSING)
                1801 -> throw AMapException(AMapException.AMAP_CLIENT_ERROR_PROTOCOL)
                1802 -> throw AMapException(AMapException.AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION)
                1803 -> throw AMapException(AMapException.AMAP_CLIENT_URL_EXCEPTION)
                1804 -> throw AMapException(AMapException.AMAP_CLIENT_UNKNOWHOST_EXCEPTION)
                1806 -> throw AMapException(AMapException.AMAP_CLIENT_NETWORK_EXCEPTION)
                1900 -> throw AMapException(AMapException.AMAP_CLIENT_UNKNOWN_ERROR)
                1901 -> throw AMapException(AMapException.AMAP_CLIENT_INVALID_PARAMETER)
                1902 -> throw AMapException(AMapException.AMAP_CLIENT_IO_EXCEPTION)
                1903 -> throw AMapException(AMapException.AMAP_CLIENT_NULLPOINT_EXCEPTION)
                2000 -> throw AMapException(AMapException.AMAP_SERVICE_TABLEID_NOT_EXIST)
                2001 -> throw AMapException(AMapException.AMAP_ID_NOT_EXIST)
                2002 -> throw AMapException(AMapException.AMAP_SERVICE_MAINTENANCE)
                2003 -> throw AMapException(AMapException.AMAP_ENGINE_TABLEID_NOT_EXIST)
                2100 -> throw AMapException(AMapException.AMAP_NEARBY_INVALID_USERID)
                2101 -> throw AMapException(AMapException.AMAP_NEARBY_KEY_NOT_BIND)
                2200 -> throw AMapException(AMapException.AMAP_CLIENT_UPLOADAUTO_STARTED_ERROR)
                2201 -> throw AMapException(AMapException.AMAP_CLIENT_USERID_ILLEGAL)
                2202 -> throw AMapException(AMapException.AMAP_CLIENT_NEARBY_NULL_RESULT)
                2203 -> throw AMapException(AMapException.AMAP_CLIENT_UPLOAD_TOO_FREQUENT)
                2204 -> throw AMapException(AMapException.AMAP_CLIENT_UPLOAD_LOCATION_ERROR)
                3000 -> throw AMapException(AMapException.AMAP_ROUTE_OUT_OF_SERVICE)
                3001 -> throw AMapException(AMapException.AMAP_ROUTE_NO_ROADS_NEARBY)
                3002 -> throw AMapException(AMapException.AMAP_ROUTE_FAIL)
                3003 -> throw AMapException(AMapException.AMAP_OVER_DIRECTION_RANGE)
                4000 -> throw AMapException(AMapException.AMAP_SHARE_LICENSE_IS_EXPIRED)
                4001 -> throw AMapException(AMapException.AMAP_SHARE_FAILURE)
                else -> {
                    showShort("查询失败：$rCode")
                    logError("查询失败", rCode)
                }
            }
        } catch (e: Exception) {
            showShort(e.message)
            logError(e.message, rCode)
        }
    }

    private const val LENGTH = 80
    private val line: String = StringBuilder().apply {
        for (i in 0 until LENGTH) {
            append("=")
        }
    }.toString()

    private fun logError(info: String?, errorCode: Int) {
        print(line)
        print("                                   错误信息                                     ")
        print(line)
        print(info)
        print("错误码: $errorCode")
        print("                                                                               ")
        print("如果需要更多信息，请根据错误码到以下地址进行查询")
        print("http://lbs.amap.com/api/android-sdk/guide/map-tools/error-code/")
        print("如若仍无法解决问题，请将全部log信息提交到工单系统，多谢合作")
        print(line)
    }

    private fun print(string: String?) = info("$loggerTag->$string")
    private const val BOARD_CHAR = "|"
    private fun printLog(string: String) {
        when {
            string.length < LENGTH - 2 -> StringBuilder().apply {
                append(BOARD_CHAR).append(string)
                for (i in 0 until LENGTH - 2 - string.length) {
                    append(" ")
                }
                append(BOARD_CHAR)
            }.toString().run { print(this) }
            else -> {
                print("$BOARD_CHAR${string.substring(0, LENGTH - 2)}$BOARD_CHAR")
                printLog(string.substring(LENGTH - 2))
            }
        }
    }
}