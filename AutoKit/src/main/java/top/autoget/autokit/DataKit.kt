package top.autoget.autokit

import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.StringKit.isSpace
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object DataKit {
    @JvmOverloads
    fun toDoubleWithDefault(any: Any?, defaultValue: Double = 0.0): Double = when {
        any is Number -> any.toDouble()
        isNumberByAny(any) -> any.toString().toDouble()
        else -> if (any == null) defaultValue else 0.0
    }

    fun isNumberByAny(any: Any?): Boolean =
        if (any is Number) true else isIntegerByAny(any) || isDecimalByAny(any)//是否有效数字

    fun isIntegerByAny(any: Any?): Boolean = when {
        isNullOrEmpty(any) -> false
        any is Byte || any is Short || any is Char || any is Int || any is Long -> true
        else -> compileRegex("[-+]?\\d+").matcher(any.toString()).matches()
    }//是否有效整数，any.toString().matches("[-+]?\\d+".toRegex())

    fun isDecimalByAny(any: Any?): Boolean = when {
        isNullOrEmpty(any) -> false
        any is Float || any is Double -> true
        else -> compileRegex("[-+]?\\d+\\.\\d+").matcher(any.toString()).matches()
    }//是否有效小数，any.toString().matches("[-+]?\\d+\\.\\d+".toRegex())

    fun isNullOrEmpty(any: Any?): Boolean = any == null || any.toString() == ""//是否无效值
    private val patternCache = ConcurrentHashMap<String, Pattern>()
    fun compileRegex(regex: String): Pattern =
        patternCache[regex] ?: regex.toPattern().apply { patternCache[regex] = this }

    @JvmOverloads
    fun toIntWithDefault(any: Any?, defaultValue: Int = 0): Int = when {
        any is Number -> any.toInt()
        isIntegerByAny(any) -> any.toString().toInt()
        isDecimalByAny(any) -> any.toString().toDouble().toInt()
        else -> if (any == null) defaultValue else 0
    }

    @JvmOverloads
    fun toLongWithDefault(any: Any?, defaultValue: Long = 0L): Long = when {
        any is Number -> any.toLong()
        isIntegerByAny(any) -> any.toString().toLong()
        isDecimalByAny(any) -> any.toString().toDouble().toLong()
        else -> if (any == null) defaultValue else 0L
    }

    @JvmOverloads
    fun toStringWithDefault(any: Any?, defaultValue: String? = null): String =
        any?.toString() ?: defaultValue ?: ""

    fun isTrue(any: Any?): Boolean = any.toString() == "true"
    fun isFalse(any: Any?): Boolean = any.toString() == "false"
    fun isBoolean(any: Any?): Boolean = when (any) {
        is Boolean -> true
        else -> any.toString().let { "true".equals(it, true) || "false".equals(it, true) }
    }//是否布尔，包括"true"和"false"字符串

    fun fitDistance(length: Double, displayMeter: Boolean): String = formatTwo.run {
        when {
            length < 1000 -> "${format(length)}${if (displayMeter) "米" else ""}"
            else -> "${format(length / 1000)}${if (displayMeter) "千米" else ""}"
        }
    }

    fun hideMobileMid(mobile: String?): String = mobile?.run {
        when (length) {
            11 -> replace("(\\d{3})\\d{4}(\\d{4})".toRegex(), "$1 **** $2")
            else -> "手机号码有误"
        }//"${substring(0, 3)} **** ${substring(7, 11)}"
    } ?: "手机号码有误"//“000 **** 0000”

    fun hideId18Mid(card: String?): String = card?.run {
        when (length) {
            18 -> replace("(\\d{4})\\d{10}(\\d{4})".toRegex(), "$1** **** **** $2")
            else -> "身份证号有误"
        }//"${substring(0, 4)}** **** **** ${substring(length - 4)}"
    } ?: "身份证号有误"//“0000** **** **** 0000”

    fun hideBankMid(card: String?): String = card?.run {
        when {
            length < 8 -> "银行卡号有误"
            else -> "${substring(0, 4)} **** **** ${substring(length - 4)}"
        }
    } ?: "银行卡号有误"//“0000 **** **** 0000”

    fun hideBankLeft(card: String?): String = card?.run {
        when {
            length < 8 -> "银行卡号有误"
            else -> "**** **** **** ${substring(length - 4)}"
        }
    } ?: "银行卡号有误"//“**** **** **** 0000”

    @JvmOverloads
    fun formatAmount(value: String?, decimalFormat: DecimalFormat = formatDefault): String =
        (value?.run { if (isSpace(this)) "0" else formatAmount(toDouble(), decimalFormat) } ?: "0")

    val formatDefault = DecimalFormat(",###.##")
    val formatZero: DecimalFormat = formatDefault.apply { applyPattern("#") }//整数带零位小数
    val formatTwo: DecimalFormat = formatDefault.apply { applyPattern("#.##") }//整数带两位小数
    val formatPercent: DecimalFormat = formatDefault.apply { applyPattern("#.##%") }//整数带两位小数百分
    val formatE: DecimalFormat = formatDefault.apply { applyPattern("#.##E0") }//整数带两位小数科计
    val formatSplit: DecimalFormat = formatDefault.apply { applyPattern(",###") }//逗号分隔每三位数
    val formatAdd: DecimalFormat = formatDefault.apply { applyPattern("0000000000.00") }//十位整数带两位小数

    @JvmOverloads
    fun formatAmount(value: Double?, decimalFormat: DecimalFormat = formatDefault): String =
        (value?.run { decimalFormat.format(this) } ?: "0")
            .run { if (startsWith(".")) "0$this" else this }

    fun string2Ints(string: String?): IntArray? = string?.run {
        IntArray(length).apply {
            if (isNotSpace(string)) for ((index, char) in string.withIndex()) {
                this[index] = char.toInt()
            }
        }
    }

    fun getPercentValue(value: String?, digit: Int): String = when {
        isSpace(value) -> "0"
        else -> value?.run { getPercentValue(toBigDecimal(), digit).toString() } ?: "0"
    }

    fun getPercentValue(value: Double?, digit: Int): Double =
        value?.run { getPercentValue(toBigDecimal(), digit).toDouble() } ?: 0.0

    fun getPercentValue(value: BigDecimal?, digit: Int): BigDecimal =
        value?.run { getRoundUp(multiply(100.toBigDecimal()), digit) } ?: 0.toBigDecimal()

    fun getRoundUp(value: String?, digit: Int): String = when {
        isSpace(value) -> "0"
        else -> value?.run { getRoundUp(toBigDecimal(), digit).toString() } ?: "0"
    }

    fun getRoundUp(value: Double?, digit: Int): Double =
        value?.run { getRoundUp(toBigDecimal(), digit).toDouble() } ?: 0.0

    fun getRoundUp(value: BigDecimal?, digit: Int): BigDecimal =
        value?.setScale(digit, BigDecimal.ROUND_HALF_UP) ?: 0.toBigDecimal()
}