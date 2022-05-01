package top.autoget.autokit

import top.autoget.autokit.DataKit.compileRegex
import top.autoget.autokit.DataKit.isNullOrEmpty

object StringKit {
    fun isEmptyNoTrim(charSequence: CharSequence?): Boolean =
        charSequence?.run { this == "" || isEmpty() } ?: true

    fun isEmptyTrim(charSequence: CharSequence?): Boolean =
        charSequence?.run { this == "" || trim { it <= ' ' }.isEmpty() } ?: true

    fun isNotNull(string: String?): Boolean = !isNull(string)
    fun isNull(string: String?): Boolean = string?.run { this == "" || this == "null" } ?: true
    fun isNotSpace(string: String?): Boolean = !isSpace(string)
    fun isSpace(string: String?): Boolean = string?.run {
        when {
            this == "" || isEmpty() || trim { it <= ' ' }.isEmpty() -> return true
            else -> {
                for (char in this) {
                    if (char != ' ' && char != '\t' && char != '\r' && char != '\n') return false
                }//!Character.isWhitespace(char)
                return true
            }
        }
    } ?: true//空格、制表符、回车符、换行符、null、空字符串""

    fun isNumberStr(value: String?): Boolean = isIntegerStr(value) || isDoubleStr(value)
    fun isIntegerStr(value: String?): Boolean = try {
        value?.run { true.apply { toInt() } } ?: false
    } catch (e: NumberFormatException) {
        false
    }

    fun isDoubleStr(value: String?): Boolean = try {
        value?.run { contains(".").apply { toDouble() } } ?: false
    } catch (e: NumberFormatException) {
        false
    }

    fun isCnCharContains(string: String?): Boolean = string?.run {
        for (char in toCharArray()) {
            if (char in '\u4e00'..'\u9fa5') return true
        }//isCnChar(char)
        return false
    } ?: false

    fun isCnCharAll(string: String?): Boolean = string?.run {
        for (char in toCharArray()) {
            if (isNotCnChar(char)) return false
        }
        return true
    } ?: false

    fun isNotCnChar(char: Char): Boolean = !isCnChar(char)
    fun isCnChar(char: Char): Boolean = Character.UnicodeBlock.of(char).let {
        (it === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || it === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || it === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || it === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || it === Character.UnicodeBlock.GENERAL_PUNCTUATION
                || it === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS)
    }//'\u4e00'..'\u9fa5'

    @JvmOverloads
    fun isMessyCode(string: String?, percent: Double = 0.4): Boolean = string?.run {
        var count = 0.0
        var cnLength = 0.0
        for (char in "\\s*|\t*|\r*|\n*".toPattern().matcher(this).replaceAll("")
            .replace("\\p{P}".toRegex(), "").trim { it <= ' ' }.toCharArray()) {
            if (!Character.isLetterOrDigit(char)) {
                if (isNotCnChar(char)) count++
                cnLength++
            }
        }
        return count / cnLength > percent
    } ?: false//是否乱码

    @JvmOverloads
    fun equalsString(string1: String?, string2: String?, ignoreCase: Boolean = true): Boolean =
        string1.equals(string2, ignoreCase)

    fun equalsCharSequence(charSequence1: CharSequence?, charSequence2: CharSequence?): Boolean =
        charSequence1?.let {
            charSequence2?.let {
                when {
                    charSequence1 === charSequence2 -> true
                    charSequence1.length == charSequence2.length -> when {
                        charSequence1 is String && charSequence2 is String ->
                            charSequence1 == charSequence2
                        else -> {
                            for ((index, char) in charSequence1.withIndex()) {
                                if (charSequence2[index] != char) return false
                            }
                            true
                        }
                    }
                    else -> false
                }
            } ?: false
        } ?: false

    fun firstMatcher(string: String?, regex: String): String? =
        string?.run { compileRegex(regex).matcher(string).run { if (find()) group() else null } }

    fun firstSplit(string: String?, split: String): Array<String> =
        string?.split(split.toRegex(), 2)?.toTypedArray() ?: arrayOf()

    fun anySplit(any: Any?, split: String): Array<String>? = when {
        isNullOrEmpty(any) -> null
        else -> any.toString().split(split.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }

    fun concatNoSpilt(vararg more: Any): String = concatSpilt("", *more)
    fun concatSpilt(split: String, vararg more: Any): String = StringBuilder().apply {
        for (any in more) {
            append("$split$any")
        }
    }.toString().substring(1)
}