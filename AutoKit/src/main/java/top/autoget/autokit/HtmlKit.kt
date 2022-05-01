package top.autoget.autokit

import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object HtmlKit {
    fun keywordMadeRed(sourceString: String?, keyword: String?): String = when {
        sourceString == null || sourceString.trim { it <= ' ' } == "" -> ""
        else -> when {
            keyword == null || keyword.trim { it <= ' ' } == "" -> sourceString
            else -> sourceString.replace(keyword.toRegex(), addHtmlRedFlag(keyword))
        }
    }//Html.fromHtml()转为Spanned传给TextView

    fun addHtmlRedFlag(string: String): String = "<font color=\"ERROR_ALERT_RED\">$string</font>"
    fun getJson(data: String, url: String): String? = doHttpAction(url, true, true, data)
    fun getForm(data: String, url: String): String? = doHttpAction(url, true, false, data)
    fun postJson(data: String, url: String): String? = doHttpAction(url, false, true, data)
    fun postForm(data: String, url: String): String? = doHttpAction(url, false, false, data)
    private fun doHttpAction(url: String, isGet: Boolean, isJson: Boolean, data: String): String? =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15000
            readTimeout = 19000
            requestMethod = if (isGet) "GET" else "POST"
            doInput = true
            doOutput = true
            useCaches = false
            instanceFollowRedirects = true//true系统重定向；否则http reply中分析新url自己重连接
            when {
                isJson -> setRequestProperty("Content-Type", "application/json")
                else -> {
                    setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    setRequestProperty("Content-Length", "${data.length}")
                }
            }
        }.run {
            try {
                connect()
                DataOutputStream(outputStream).use {
                    data.toByteArray().let { bytes -> it.write(bytes, 0, bytes.size) }
                    it.flush()
                }
                inputStream.use {
                    Scanner(it).let { scanner ->
                        scanner.useDelimiter("\\A")
                        if (scanner.hasNext()) return scanner.next()
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                disconnect()
            }
        }
}