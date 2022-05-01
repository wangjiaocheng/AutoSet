package top.autoget.autokit

import android.os.Build
import android.text.Html
import android.util.Base64
import top.autoget.autokit.VersionKit.aboveJellyBean
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

object EncodeKit {
    fun base64Encode2String(input: ByteArray?): String = input?.run {
        if (isEmpty()) "" else Base64.encodeToString(this, Base64.NO_WRAP)
    } ?: ""

    fun base64EncodeUrlSafe(input: String?): ByteArray = input?.run {
        if (isEmpty()) byteArrayOf() else Base64.encode(toByteArray(), Base64.URL_SAFE)
    } ?: byteArrayOf()

    fun base64Encode(input: ByteArray?): ByteArray = input?.run {
        if (isEmpty()) byteArrayOf() else Base64.encode(this, Base64.NO_WRAP)
    } ?: byteArrayOf()

    fun base64Encode(input: String?): ByteArray = input?.run {
        if (isEmpty()) byteArrayOf() else Base64.encode(toByteArray(), Base64.NO_WRAP)
    } ?: byteArrayOf()

    fun base64Decode(input: ByteArray?): ByteArray = input?.run {
        if (isEmpty()) byteArrayOf() else Base64.decode(this, Base64.NO_WRAP)
    } ?: byteArrayOf()

    fun base64Decode(input: String?): ByteArray = input?.run {
        if (isEmpty()) byteArrayOf() else Base64.decode(this, Base64.NO_WRAP)
    } ?: byteArrayOf()

    fun binEncode(input: String?): String = input?.run {
        if (isEmpty()) "" else StringBuilder().apply {
            for (char in input.toCharArray()) {
                append("${Integer.toBinaryString(char.toInt())} ")
            }
        }.toString()
    } ?: ""

    fun binDecode(input: String?): String = input?.run {
        if (isEmpty()) "" else StringBuilder().apply {
            for (string in input.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()) {
                append(string.replace(" ", "").toInt(2).toChar())
            }
        }.toString()
    } ?: ""

    @JvmOverloads
    fun urlEncode(input: String?, charset: String = "UTF-8"): String = input?.run {
        if (isEmpty()) "" else try {
            URLEncoder.encode(this, charset)
        } catch (e: UnsupportedEncodingException) {
            throw AssertionError(e)
        }
    } ?: ""

    @JvmOverloads
    fun urlDecode(input: String?, charset: String = "UTF-8"): String = input?.run {
        if (isEmpty()) "" else try {
            URLDecoder.decode(this, charset)
        } catch (e: UnsupportedEncodingException) {
            throw AssertionError(e)
        }
    } ?: ""

    fun htmlEncode(input: String?): String = input?.run {
        when {
            isEmpty() -> ""
            aboveJellyBean -> Html.escapeHtml(this)
            else -> StringBuilder().apply {
                var i = 0
                while (i < input.length) {
                    val char = input[i]
                    when {
                        char.toInt() > 0x7E || char < ' ' -> append("&#${char.toInt()};")
                        char.toInt() in 0xD800..0xDFFF -> if (char.toInt() < 0xDC00 && i + 1 < input.length) {
                            val char0 = input[i + 1]
                            if (char0.toInt() in 0xDC00..0xDFFF) {
                                i++
                                append("&#${0x010000 or (char.toInt() - 0xD800 shl 10) or char0.toInt() - 0xDC00};")
                            }
                        }
                        char == ' ' -> {
                            while (i + 1 < input.length && input[i + 1] == ' ') {
                                append("&nbsp;")
                                i++
                            }
                            append(' ')
                        }
                        char == '<' -> append("&lt;")
                        char == '>' -> append("&gt;")
                        char == '&' -> append("&amp;")
                        char == '\'' -> append("&#39;")
                        char == '"' -> append("&quot;")
                        else -> append(char)
                    }
                    i++
                }
            }.toString()
        }
    } ?: ""

    fun htmlDecode(input: String?): String = input?.run {
        if (isEmpty()) "" else when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
            else -> Html.fromHtml(this)
        }.toString()
    } ?: ""
}