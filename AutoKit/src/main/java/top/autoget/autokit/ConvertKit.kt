package top.autoget.autokit

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.DateKit.sdfDateByFull
import top.autoget.autokit.DateKit.zeroTimeOfToday
import top.autoget.autokit.StringKit.isSpace
import java.io.*
import java.math.BigDecimal
import java.text.DateFormat
import java.text.ParseException
import java.util.*
import kotlin.experimental.or
import kotlin.math.min

object ConvertKit {
    const val BYTE = 1
    const val KB = 1024
    const val MB = 1048576
    const val GB = 1073741824

    @IntDef(BYTE, KB, MB, GB)
    @Retention(AnnotationRetention.SOURCE)
    annotation class MemoryUnit

    fun memorySize2ByteSize(memorySize: Long, @MemoryUnit unit: Int): Long =
        if (memorySize < 0) -1L else memorySize * unit

    fun byteSize2MemorySize(byteSize: Long, @MemoryUnit unit: Int): Double =
        if (byteSize < 0) -1.0 else byteSize.toDouble() / unit

    fun byteSize2MemorySizeFit(byteSize: Long, @IntRange(from = 0, to = 4) precision: Int): String =
        when {
            byteSize > 0 && precision > 0 -> StringBuilder().apply {
                intArrayOf(GB, MB, KB, BYTE).let { unitLen ->
                    arrayOf("GB", "MB", "KB", "BYTE").let { units ->
                        var byteSizeTemp = byteSize
                        for (i in 0 until min(precision, 4)) {
                            if (byteSizeTemp >= unitLen[i]) (byteSizeTemp / unitLen[i]).let { mode ->
                                byteSizeTemp -= mode * unitLen[i]
                                append("$mode${units[i]}")
                            }
                        }
                    }
                }
            }.toString()
            else -> "0"
        }

    fun byteSize2MemorySizeFit(byteSize: Long): String = when {
        byteSize < 0 -> "shouldn't be less than zero!"
        byteSize < KB -> String.format("%.3fBYTE", byteSize.toDouble())
        byteSize < MB -> String.format("%.3fKB", byteSize.toDouble() / KB)
        byteSize < GB -> String.format("%.3fMB", byteSize.toDouble() / MB)
        else -> String.format("%.3fGB", byteSize.toDouble() / GB)
    }//3位小数

    fun byteSize2MemorySizeDesc(byteSize: Double): String {
        val kByte = byteSize / 1024
        if (kByte < 1) return "${
            byteSize.toBigDecimal().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
        }BYTE"
        val mByte = kByte / 1024
        if (mByte < 1) return "${
            kByte.toBigDecimal().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
        }KB"
        val gByte = mByte / 1024
        if (gByte < 1) return "${
            mByte.toBigDecimal().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
        }MB"
        val tByte = gByte / 1024
        if (tByte < 1) return "${
            gByte.toBigDecimal().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
        }GB"
        return "${
            tByte.toBigDecimal().setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
        }TB"
    }

    const val MSEC = 1
    const val SEC = 1000
    const val MIN = 60000
    const val HOUR = 3600000
    const val DAY = 86400000

    @IntDef(MSEC, SEC, MIN, HOUR, DAY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class TimeUnit

    fun timeSpan2Millis(timeSpan: Long, @TimeUnit unit: Int): Long =
        if (timeSpan < 0) -1L else timeSpan * unit

    fun millis2TimeSpan(millis: Long, @TimeUnit unit: Int): Long =
        if (millis < 0) -1L else millis / unit

    fun millis2TimeSpanFit(millis: Long, @IntRange(from = 0, to = 5) precision: Int): String =
        arrayOf("天", "小时", "分钟", "秒", "毫秒").let { units ->
            when (millis) {
                0L -> "0${units[min(precision, 5) - 1]}"
                else -> StringBuilder().apply {
                    var millisTemp: Long = if (millis < 0) -millis.apply { append("-") } else millis
                    intArrayOf(DAY, HOUR, MIN, SEC, MSEC).let { unitLen ->
                        for (i in 0 until min(precision, 5)) {
                            if (millisTemp >= unitLen[i]) (millisTemp / unitLen[i]).let { mode ->
                                millisTemp -= mode * unitLen[i]
                                append("$mode${units[i]}")
                            }
                        }
                    }
                }.toString()
            }
        }

    fun millis2TimeSpanFitByNow(millis: Long): String = (nowMillis - millis).let { timeSpan ->
        when {
            timeSpan < MSEC -> String.format("%tc", millis)//time()
            timeSpan < SEC -> "刚刚"
            timeSpan < MIN ->
                String.format(Locale.getDefault(), "%d秒前", timeSpan / SEC)
            timeSpan < HOUR ->
                String.format(Locale.getDefault(), "%d分钟前", timeSpan / MIN)
            millis >= zeroTimeOfToday -> String.format("今天%tR", millis)//hm()
            millis >= zeroTimeOfToday - DAY -> String.format("昨天%tR", millis)//hm()
            else -> String.format("%tF", millis)//ymd()
        }
    }

    val timeZonePhone: TimeZone
        get() = TimeZone.getDefault()
    val timeZoneBeijing: TimeZone
        get() = TimeZone.getTimeZone("GMT+8:00")

    fun phoneTime2BeijingTime(phoneTime: String?): String =
        date2String(changeTimeZone(string2Date(phoneTime), timeZonePhone, timeZoneBeijing))

    fun beijingTime2PhoneTime(beijingTime: String?): String =
        date2String(changeTimeZone(string2Date(beijingTime), timeZoneBeijing, timeZonePhone))

    fun changeTimeZone(date: Date?, oldZone: TimeZone, newZone: TimeZone): Date? =
        date?.run { Date(time - (oldZone.rawOffset - newZone.rawOffset)) }

    fun millis2Date(millis: Long?): Date? = millis?.let { Date(it) }
    fun date2Millis(date: Date?): Long = date?.time ?: 0L
    fun millis2String(millis: Long?, dateFormat: DateFormat = sdfDateByFull): String =
        millis?.let { dateFormat.format(Date(it)) } ?: ""

    fun string2Millis(time: String?, dateFormat: DateFormat = sdfDateByFull): Long = try {
        time?.let { dateFormat.parse(it)?.time } ?: 0L
    } catch (e: ParseException) {
        e.printStackTrace()
        0L
    }

    fun date2String(date: Date?, dateFormat: DateFormat = sdfDateByFull): String =
        date?.let { dateFormat.format(it) } ?: ""

    fun string2Date(time: String?, dateFormat: DateFormat = sdfDateByFull): Date? = try {
        time?.let { dateFormat.parse(it) }
    } catch (e: ParseException) {
        e.printStackTrace()
        null
    }

    fun bytes2Chars(bytes: ByteArray?): CharArray? = bytes?.size?.let { length ->
        when {
            length > 0 -> CharArray(length).apply {
                for (i in bytes.indices) {
                    this[i] = (bytes[i].toInt() and 0xff).toChar()
                }
            }
            else -> null
        }
    }

    fun chars2Bytes(chars: CharArray?): ByteArray? = chars?.size?.let { length ->
        when {
            length > 0 -> ByteArray(length).apply {
                for (i in chars.indices) {
                    this[i] = chars[i].toByte()
                }
            }
            else -> null
        }
    }

    fun bytes2Bits(bytes: ByteArray?): String = bytes?.run {
        when {
            isEmpty() -> ""
            else -> StringBuilder().apply {
                for (byte in bytes) {
                    for (i in 7 downTo 0) {
                        append(if (byte.toInt() shr i and 0x01 == 0) '0' else '1')
                    }
                }
            }.toString()
        }
    } ?: ""

    fun bits2Bytes(bits: String): ByteArray? = when {
        isSpace(bits) -> null
        else -> {
            var byteLen = bits.length / 8
            var bitsTemp = bits
            (bits.length % 8).let {
                if (it != 0) {
                    byteLen++
                    for (i in it..7) {
                        bitsTemp = "0$bitsTemp"
                    }
                }
            }
            ByteArray(byteLen).apply {
                for (i in 0 until byteLen) {
                    for (j in 0..7) {
                        this[i] =
                            (this[i].toInt() shl 1).toByte() or (bitsTemp[i * 8 + j] - '0').toByte()
                    }
                }
            }
        }
    }

    val hexDigitsLower =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    val hexDigitsUpper =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    @JvmOverloads
    fun bytes2HexStringIsUpper(bytes: ByteArray?, isUpper: Boolean = true): String = bytes?.run {
        when {
            isEmpty() -> ""
            else -> String(CharArray(size shl 1).apply {
                (if (isUpper) hexDigitsUpper else hexDigitsLower).let { hexDigits ->
                    var i = 0
                    for (byte in bytes) {
                        this[i++] = hexDigits[byte.toInt() shr 4 and 0x0f]
                        this[i++] = hexDigits[byte.toInt() and 0x0f]
                    }//(48..57).toChar()->（0..9）；(97..102).toChar()->（A..F）
                }
            })
        }
    } ?: ""

    fun bytes2HexString(bytes: ByteArray?): String = bytes?.run {
        when {
            isEmpty() -> ""
            else -> StringBuilder().apply {
                for (byte in bytes) {
                    Integer.toHexString(0xFF and byte.toInt()).let { str ->
                        if (str.length == 1) append("0$str") else append(str)
                    }
                }
            }.toString()
        }
    } ?: ""

    fun hexString2Bytes(hexString: String): ByteArray? = when {
        isSpace(hexString) -> null
        else -> {
            var length = hexString.length
            (if (length % 2 == 0) hexString else "0$hexString".apply { length += 1 })
                .toUpperCase(Locale.getDefault()).toCharArray().let { hexBytes ->
                    ByteArray(length shr 1).apply {
                        for (i in 0 until length step 2) {
                            this[i shr 1] =
                                ((hex2Int(hexBytes[i]) shl 4) or hex2Int(hexBytes[i + 1])).toByte()
                        }
                    }
                }
        }
    }

    fun hex2Int(hexChar: Char): Int = when (hexChar) {
        in '0'..'9' -> hexChar - '0'
        in 'A'..'F' -> hexChar - 'A' + 10
        else -> throw IllegalArgumentException()
    }//"0123456789ABCDEF".indexOf(char).toByte().toInt()

    fun outputStream2String(outputStream: OutputStream?, charsetName: String): String =
        outputStream?.use {
            when {
                isSpace(charsetName) -> ""
                else -> try {
                    outputStream2Bytes(it)?.run { String(this, charset(charsetName)) } ?: ""
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    ""
                }
            }
        } ?: ""

    fun outputStream2Bytes(outputStream: OutputStream?): ByteArray? =
        (outputStream as ByteArrayOutputStream?)?.use { it.toByteArray() }

    fun string2OutputStream(string: String?, charsetName: String): OutputStream? = string?.let {
        when {
            isSpace(charsetName) -> null
            else -> try {
                bytes2OutputStream(it.toByteArray(charset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }
    }

    fun bytes2OutputStream(bytes: ByteArray?): OutputStream? = bytes?.run {
        when {
            isEmpty() -> null
            else -> try {
                ByteArrayOutputStream().use { it.apply { write(bytes) } }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    fun inputStream2String(inputStream: InputStream?, charsetName: String): String =
        inputStream?.use {
            when {
                isSpace(charsetName) -> ""
                else -> try {
                    inputStream2Bytes(it)?.run { String(this, charset(charsetName)) } ?: ""
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    ""
                }
            }
        } ?: ""

    fun inputStream2Bytes(inputStream: InputStream?): ByteArray? = inputStream?.use {
        input2OutputStream(it)?.use { outputStream -> outputStream.toByteArray() }
    }

    fun bytes2InputStream(bytes: ByteArray?): InputStream? =
        bytes?.run { if (isEmpty()) null else ByteArrayInputStream(this) }

    fun input2OutputStream(inputStream: InputStream?): ByteArrayOutputStream? = inputStream?.use {
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                byteArrayOutputStream.apply {
                    ByteArray(KB).let { bytes ->
                        while (true) {
                            if (it.read(bytes) != -1) write(bytes) else break
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun output2InputStream(outputStream: OutputStream?): ByteArrayInputStream? =
        (outputStream as ByteArrayOutputStream?)?.use { ByteArrayInputStream(it.toByteArray()) }

    fun string2InputStream(string: String?, charsetName: String): InputStream? = string?.let {
        when {
            isSpace(charsetName) -> null
            else -> try {
                ByteArrayInputStream(it.toByteArray(charset(charsetName)))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }
    }

    fun drawable2Bytes(drawable: Drawable?, format: Bitmap.CompressFormat): ByteArray? =
        drawable?.let { bitmap2Bytes(drawable2Bitmap(it), format) }

    fun bitmap2Bytes(bitmap: Bitmap?, format: Bitmap.CompressFormat): ByteArray? = bitmap?.run {
        ByteArrayOutputStream().use { it.apply { compress(format, 100, this) }.toByteArray() }
    }

    fun drawable2Bitmap(drawable: Drawable): Bitmap = drawable.run {
        when {
            this is BitmapDrawable && bitmap != null -> bitmap
            else -> Bitmap.createBitmap(
                if (intrinsicWidth > 0 && intrinsicHeight > 0) intrinsicWidth else 1,
                if (intrinsicWidth > 0 && intrinsicHeight > 0) intrinsicHeight else 1,
                if (opacity == PixelFormat.OPAQUE) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
            ).apply {
                Canvas(this).let { canvas ->
                    setBounds(0, 0, canvas.width, canvas.height)
                    draw(canvas)
                }
            }
        }
    }

    fun bytes2Drawable(bytes: ByteArray?): Drawable? =
        bytes?.let { bitmap2Drawable(bytes2Bitmap(it)) }

    fun bitmap2Drawable(bitmap: Bitmap?): Drawable? =
        bitmap?.run { BitmapDrawable(Resources.getSystem(), this) }

    fun bytes2Bitmap(bytes: ByteArray?): Bitmap? =
        bytes?.run { if (isEmpty()) null else BitmapFactory.decodeByteArray(this, 0, size) }

    fun nullOfString(string: String?): String = string?.trim { it <= ' ' } ?: ""
    fun saveDecimals(double: Double, digit: Int): String = when (digit) {
        2 -> String.format("%.02f", double)
        1 -> String.format("%.01f", double)
        else -> String.format("%.0f", double)
    }

    fun int2Bytes(int: Int): ByteArray = ByteArray(4).apply {
        this[0] = (int and 0xff).toByte()
        this[1] = (int shr 8 and 0xff).toByte()
        this[2] = (int shr 16 and 0xff).toByte()
        this[3] = int.ushr(24).toByte()
    }

    fun bytes2Int(bytes: ByteArray): Int = bytes[3].toInt() and 0xff or
            (bytes[2].toInt() shl 8 and 0xff00) or
            (bytes[1].toInt() shl 16 and 0xff0000) or
            (bytes[0].toInt() shl 24 and -0x1000000)//一个byte数据左移24位变成0x??000000，再右移8位变成0x00??0000

    fun ip2Long(string: String): Long = string.replace(".", ",")
        .split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        .run { (this[0].toLong() shl 24) or (this[1].toLong() shl 16) or (this[2].toLong() shl 8) or this[3].toLong() }

    fun string2Byte(string: String?): Byte = string?.let { str ->
        try {
            str.trim { it <= ' ' }.toByte()
        } catch (e: Exception) {
            0.toByte()
        }
    } ?: 0.toByte()

    fun string2Short(string: String?): Short = string?.let { str ->
        try {
            str.trim { it <= ' ' }.toShort()
        } catch (e: Exception) {
            0.toShort()
        }
    } ?: 0.toShort()

    fun string2Int(string: String?): Int = string?.let { str ->
        try {
            str.trim { it <= ' ' }.toInt()
        } catch (e: Exception) {
            0
        }
    } ?: 0

    fun string2Long(string: String?): Long = string?.let { str ->
        try {
            str.trim { it <= ' ' }.toLong()
        } catch (e: Exception) {
            0L
        }
    } ?: 0L

    fun string2Float(string: String?): Float = string?.let { str ->
        try {
            str.trim { it <= ' ' }.toFloat()
        } catch (e: Exception) {
            0f
        }
    } ?: 0f

    fun string2Double(string: String?): Double = string?.let { str ->
        try {
            str.trim { it <= ' ' }.toDouble()
        } catch (e: Exception) {
            0.0
        }
    } ?: 0.0

    fun string2Boolean(string: String?): Boolean =
        string?.trim { it <= ' ' }?.toLowerCase(Locale.getDefault())?.run {
            when (this) {
                "1" -> true
                "0" -> false
                "true", "false" -> toBoolean()
                else -> false
            }
        } ?: false

    fun double2Int(double: Double): Int = try {
        double.toString().run { substring(0, lastIndexOf(".")).toInt() }
    } catch (e: Exception) {
        0
    }

    fun double2Long(double: Double): Long = try {
        double.toString().run { substring(0, lastIndexOf(".")).toLong() }
    } catch (e: Exception) {
        0
    }

    fun long2Int(long: Long): Int = try {
        long.toString().toInt()
    } catch (e: Exception) {
        0
    }

    fun long2Double(long: Long): Double = try {
        long.toString().toDouble()
    } catch (e: Exception) {
        0.0
    }

    @Throws(Exception::class)
    fun char2Int(char: Char, index: Int): Int = Character.digit(char, 16).apply {
        if (this == -1) throw Exception("Illegal hexadecimal character $char at index $index")
    }
}