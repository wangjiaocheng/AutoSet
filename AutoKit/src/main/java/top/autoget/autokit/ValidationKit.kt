package top.autoget.autokit

import top.autoget.autokit.StringKit.isNotSpace
import java.util.*
import java.util.regex.Pattern

object ValidationKit : LoggerKit {
    fun cutStringFromChar(string: String, sub: String, offset: Int): String = string.run {
        indexOf(sub).let { start ->
            when {
                isNotSpace(this) && start != -1 && length > start + offset ->
                    substring(start + offset)
                else -> ""
            }
        }
    }//从指定字符串开始截取指定长度字符串

    @JvmOverloads
    fun cutString(string: String, length: Int, dot: String? = null): String = when {
        getStringLength(string) > length -> StringBuffer(length).apply {
            var temp = 0
            for (char in string) {
                append(char)
                temp += when {
                    char.toInt() > 256 -> 2
                    else -> 1
                }
                if (temp >= length) {
                    if (dot != null) append(dot)
                    break
                }
            }
        }.toString()
        else -> string
    }//截取指定长度字符串

    @JvmOverloads
    fun getStringLength(string: String?, charset: String = "GBK"): Int = string?.run {
        if (isEmpty()) 0 else try {
            toByteArray(charset(charset)).size
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    } ?: 0

    fun getReplaceFirst(regex: String, input: String?, replacement: String): String =
        input?.let { regex.toPattern().matcher(it).replaceFirst(replacement) } ?: ""

    fun getReplaceAll(regex: String, input: String?, replacement: String): String =
        input?.let { regex.toPattern().matcher(it).replaceAll(replacement) } ?: ""

    fun getSplits(regex: String, input: String?): Array<String> =
        input?.split(regex.toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray() ?: arrayOf()

    fun getMatches(regex: String, input: String?): MutableList<String> =
        mutableListOf<String>().apply {
            input?.let { regex.toPattern().matcher(it) }?.let { matcher ->
                while (matcher.find()) {
                    add(matcher.group())
                }//***.toRegex().matches(string)=string.matches(***.toRegex())可交换
            }//***.toPattern.matcher(string)可复用
        }//***.toPattern.matcher(string).matches()=Pattern.matches(***, string)可简化

    fun isMatch(regex: String, input: String?): Boolean =//Pattern.matches(regex, this)
        input?.run { isNotEmpty() && regex.toPattern().matcher(this).matches() } ?: false

    private val digit: Pattern = "^-?[1-9]\\d*$".toPattern()//正负整数："-?[1-9]\\d+"
    private val positiveDigit: Pattern = "^[1-9]\\d*$".toPattern()//正整数
    private val negativeDigit: Pattern = "^-[1-9]\\d*$".toPattern()//负整数
    private val notNegativeDigit: Pattern = "^[1-9]\\d*|0$".toPattern()//非正整数
    private val notPositiveDigit: Pattern = "^-[1-9]\\d*|0$".toPattern()//非负整数
    private val decimals: Pattern = "-?[1-9]\\d+(\\.\\d+)?".toPattern()//正负小数
    private val positiveDecimals: Pattern = "^[1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*$".toPattern()//正小数
    private val negativeDecimals: Pattern =
        "^-[1-9]\\d*\\.\\d*|-0\\.\\d*[1-9]\\d*$".toPattern()//负小数
    private val notZeroNumeric: Pattern = "^\\+?[1-9][0-9]*$".toPattern()//无零
    private val numeric: Pattern = "^[0-9]*$".toPattern()
    private val blankLine: Pattern = "\\n\\s*\\r".toPattern()//空格、\t、\n、\r、\f、\x0B："\\s+"
    private val upLetter: Pattern = "^[A-Z]+$".toPattern()
    private val lowLetter: Pattern = "^[a-z]+$".toPattern()
    private val letter: Pattern = "^[A-Za-z]+$".toPattern()
    private val chinese: Pattern = "^[\u4e00-\u9fa5],{0,}$".toPattern()//"^[\\u4e00-\\u9fa5]+$"
    private val doubleByteChar: Pattern = "[^\\x00-\\xff]".toPattern()
    private val specialCharacter: Pattern =
        "[`~!@#$%^&*()+=|{}':;',\\[\\].<>?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]".toPattern()

    fun isDigit(string: String): Boolean = digit.matcher(string).matches()
    fun isPositiveDigit(string: String): Boolean = positiveDigit.matcher(string).matches()
    fun isNegativeDigit(string: String): Boolean = negativeDigit.matcher(string).matches()
    fun isNotNegativeDigit(string: String): Boolean = notNegativeDigit.matcher(string).matches()
    fun isNotPositiveDigit(string: String): Boolean = notPositiveDigit.matcher(string).matches()
    fun isDecimals(string: String): Boolean = decimals.matcher(string).matches()
    fun isPositiveDecimals(string: String): Boolean = positiveDecimals.matcher(string).matches()
    fun isNegativeDecimals(string: String): Boolean = negativeDecimals.matcher(string).matches()
    fun isNotZeroNumeric(string: String): Boolean = notZeroNumeric.matcher(string).matches()
    fun isNumeric(string: String): Boolean = numeric.matcher(string).matches()
    fun isBlankLine(string: String): Boolean = blankLine.matcher(string).matches()
    fun isUpLetter(string: String): Boolean = upLetter.matcher(string).matches()
    fun isLowLetter(string: String): Boolean = lowLetter.matcher(string).matches()
    fun isLetter(string: String): Boolean = letter.matcher(string).matches()
    fun isChinese(string: String): Boolean = chinese.matcher(string).matches()
    fun isDoubleByteChar(string: String): Boolean = doubleByteChar.matcher(string).matches()
    fun hasSpecialCharacter(string: String): Boolean = specialCharacter.matcher(string).find()
    private val birthday: Pattern = "[1-9]{4}([-./])\\d{1,2}\\1\\d{1,2}".toPattern()
    private val date: Pattern =
        "^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$".toPattern()//yyyy-MM-dd已考虑平闰年
    private val pay: Pattern = "^[0-9]+(.[0-9]{2})?\$".toPattern()//金额
    private val bankNo: Pattern =
        "^[0-9]{16,19}$".toPattern()//银行："^\\d{16,19}$|^\\d{6}[- ]\\d{10,13}$|^\\d{4}[- ]\\d{4}[- ]\\d{4}[- ]\\d{4,7}$"
    private val qqNo: Pattern = "[1-9][0-9]{4,}".toPattern()
    private val vehicleNo: Pattern =
        "^[\u4e00-\u9fa5]{1}[a-zA-Z]{1}[a-zA-Z_0-9]{5}$".toPattern()//车牌
    private val oneCode: Pattern = "^(([0-9])|([0-9])|([0-9]))\\d{10}$".toPattern()//条码
    private val postalCode: Pattern = "([0-9]{3})+.([0-9]{4})+".toPattern()
    private val chinaPostalCode: Pattern = "[1-9]\\d{5}(?!\\d)".toPattern()
    private val email: Pattern =
        "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$".toPattern()//"^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"
    private val simplePhone: Pattern = "^[1]\\d{10}$".toPattern()//简单手机

    /*
     * 移动：【134(0-8)、135、136、137、138、139】【147】【150、151、152、157、158、159】【178】【182、183、184、187、188】【198】
     * 联通：【130、131、132】                    【145】【155、156】             【166】【171、175、176】     【185、186】
     * 电信：【133】                                     【153】                         【173、177】     【180、181、189】【191、199】
     * 全球星：【1349】                                                      虚拟运营商：【170】
     */
    private val exactPhone: Pattern =
        "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(16[6])|(17[0,1,3,5-8])|(18[0-9])|(19[1,8,9]))\\d{8}$".toPattern()//精确手机
    private val chinaPlane: Pattern =
        "^((\\(\\d{2,3}\\))|(\\d{3}\\-))?(\\(0\\d{2,3}\\)|0\\d{2,3}-)?[1-9]\\d{6,7}(\\-\\d{1,4})?$".toPattern()//座机："^0\\d{2,3}[- ]?\\d{7,8}"
    private val globalPlane: Pattern = "(\\+\\d+)?(\\d{3,4}-?)?\\d{7,8}$".toPattern()//国家+城市+号码
    fun isBirthday(string: String): Boolean = birthday.matcher(string).matches()
    fun isDate(string: String): Boolean = date.matcher(string).matches()
    fun isPay(string: String): Boolean = pay.matcher(string).matches()
    fun isBankNo(string: String): Boolean =
        bankNo.matcher(string.apply { replace(" ".toRegex(), "") }).matches()

    fun isQqNo(string: String): Boolean = qqNo.matcher(string).matches()
    fun isVehicleNo(string: String): Boolean = vehicleNo.matcher(string).matches()
    fun isOneCode(string: String): Boolean = oneCode.matcher(string).matches()
    fun isPostalCode(string: String): Boolean = postalCode.matcher(string).matches()
    fun isChinaPostalCode(string: String): Boolean = chinaPostalCode.matcher(string).matches()
    fun hasChinaPostalCode(string: String): Boolean = chinaPostalCode.matcher(string).find()
    fun isEmail(string: String): Boolean = email.matcher(string).matches()
    fun isSimplePhone(string: String): Boolean = simplePhone.matcher(string).matches()
    fun isExactPhone(string: String): Boolean = exactPhone.matcher(string).matches()
    fun isChinaPlane(string: String): Boolean = chinaPlane.matcher(string).matches()
    fun isGlobalPlane(string: String): Boolean = globalPlane.matcher(string).matches()
    private val ipAddress: Pattern =
        "[1-9](\\d{1,2})?\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))\\.(0|([1-9](\\d{1,2})?))".toPattern()//"((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"
    private val url: Pattern =
        "(https?://(w{3}\\.)?)?\\w+\\.\\w+(\\.[a-zA-Z]+)*(:\\d{1,5})?(/\\w*)*(\\??(.+=.*)?(&.+=.*)?)?".toPattern()//"[a-zA-z]+://[^\\s]*"；"http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?"
    private val username: Pattern =
        "^[A-Za-z0-9_]{1}[A-Za-z0-9_.-]{3,31}".toPattern()//"^[\\w\\u4e00-\\u9fa5]{6,20}(?<!_)$"：a-z,A-Z,0-9,"_",汉字，不能"_"结尾，6-20位
    private val realName: Pattern = "[\u4E00-\u9FA5]{2,5}(?:·[\u4E00-\u9FA5]{2,5})*".toPattern()
    private val password: Pattern =
        "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,10}\$".toPattern()//下划线或字母开头，数字、字母、下划线、点、减号组成，4-32位

    fun isIpAddress(string: String): Boolean = ipAddress.matcher(string).matches()
    fun isUrl(string: String): Boolean = url.matcher(string).matches()
    fun isUserName(string: String): Boolean = username.matcher(string).matches()
    fun isRealName(string: String): Boolean = realName.matcher(string).matches()
    fun isPassword(string: String): Boolean = password.matcher(string).matches()
    private val css: Pattern =
        "^\\\\s*[a-zA-Z\\\\-]+\\\\s*[:]{1}\\\\s[a-zA-Z0-9\\\\s.#]+[;]{1}".toPattern()//CSS属性
    private val htmlTag: Pattern =
        "<\\\\/?\\\\w+((\\\\s+\\\\w+(\\\\s*=\\\\s*(?:\".*?\"|'.*?'|[\\\\^'\">\\\\s]+))?)+\\\\s*|\\\\s*)\\\\/?>".toPattern()//HTML标签
    private val htmlNotes: Pattern = "<!--(.*?)-->".toPattern()//HTML注释
    private val htmlHyperLink: Pattern =
        "(<a\\\\s*(?!.*\\\\brel=)[^>]*)(href=\"https?:\\\\/\\\\/)((?!(?:(?:www\\\\.)?'.implode('|(?:www\\\\.)?', \$follow_list).'))[^\"]+)\"((?!.*\\\\brel=)[^>]*)(?:[^>]*)>".toPattern()
    private val htmlImage: Pattern =
        "\\\\< *[img][^\\\\\\\\>]*[src] *= *[\\\\\"\\\\']{0,1}([^\\\\\"\\\\'\\\\ >]*)".toPattern()
    private val htmlColor: Pattern = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$".toPattern()
    private val htmlRoute: Pattern =
        "^([a-zA-Z]\\\\:|\\\\\\\\)\\\\\\\\([^\\\\\\\\]+\\\\\\\\)*[^\\\\/:*?\"<>|]+\\\\.txt(l)?$".toPattern()//文件路径及扩展名

    fun isCss(string: String): Boolean = css.matcher(string).matches()
    fun isHtmlTag(string: String): Boolean = htmlTag.matcher(string).matches()
    fun isHtmlNotes(string: String): Boolean = htmlNotes.matcher(string).matches()
    fun isHtmlHyperLink(string: String): Boolean = htmlHyperLink.matcher(string).matches()
    fun isHtmlImage(string: String): Boolean = htmlImage.matcher(string).matches()
    fun isHtmlColor(string: String): Boolean = htmlColor.matcher(string).matches()
    fun isHtmlRoute(string: String): Boolean = htmlRoute.matcher(string).matches()
    private val urlInText: Pattern =
        "^(f|ht){1}(tp|tps):\\/\\/([\\w-]+\\.)+[\\w-]+(\\/[\\w- ./?%&=]*)?".toPattern()
    private val httpOrHttps: Pattern = "/^[a-zA-Z]+:\\/\\//".toPattern()//不匹配则加前缀"http://"
    private val ipV4: Pattern =
        "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b".toPattern()
    private val ipV6: Pattern =
        "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))".toPattern()
    private val ieVersion: Pattern =
        "^.*MSIE [5-8](?:\\.[0-9]+)?(?!.*Trident\\/[5-9]\\.0).*\$".toPattern()

    fun isUrlInText(string: String): Boolean = urlInText.matcher(string).matches()
    fun isHttpOrHttps(string: String): Boolean = httpOrHttps.matcher(string).matches()
    fun isIpV4(string: String): Boolean = ipV4.matcher(string).matches()
    fun isIpV6(string: String): Boolean = ipV6.matcher(string).matches()
    fun isIeVersion(string: String): Boolean = ieVersion.matcher(string).matches()
    fun isIntStr(string: String?): Boolean = try {
        string?.let { true.apply { it.toInt() } } ?: false
    } catch (e: Exception) {
        false
    }

    fun isDoubleTwoUp(string: String?): Boolean =
        string?.run { indexOf(".") > 0 && substring(indexOf(".")).length > 3 } ?: false

    fun isContinuousNo(string: String): Boolean {
        when {
            isNotSpace(string) && isNumeric(string) -> {
                for (i in 0 until string.length - 1) {
                    if (string[i + 1] != (if (string[i] == '9') '0' else (string[i].toInt() + 1).toChar()))
                        return false
                }
                return true
            }
            else -> return false
        }
    }//连续数字

    fun isContinuousWord(string: String): Boolean {
        when {
            isNotSpace(string) && isLetter(string) -> {
                string.apply { toLowerCase(Locale.getDefault()) }
                for (i in 0 until string.length - 1) {
                    if (string[i + 1] != (if (string[i] == 'z') 'a' else (string[i].toInt() + 1).toChar()))
                        return false
                }
                return true
            }
            else -> return false
        }
    }//连续字母

    fun isPeculiarStr(string: String?): Boolean =
        string?.run { length != replace("[^0-9a-zA-Z\u4e00-\u9fa5]+".toRegex(), "").length }
            ?: false//特殊字符

    fun isNumberLetter(string: String?): Boolean =
        string?.matches("^[A-Za-z0-9]+$".toRegex()) ?: false

    fun isContainChinese(string: String): Boolean {
        if (isNotSpace(string)) for (char in string) {
            if ("$char".matches("[\u0391-\uFFE5]".toRegex())) return true
        }
        return false
    }

    fun lengthChinese(string: String): Int {
        var lengthByte = 0
        if (isNotSpace(string)) for (char in string) {
            if ("$char".matches("[\u0391-\uFFE5]".toRegex())) lengthByte += 2
        }
        return lengthByte
    }

    fun lengthString(string: String): Int {
        var lengthByte = 0
        if (isNotSpace(string)) for (char in string) {
            lengthByte += if ("$char".matches("[\u0391-\uFFE5]".toRegex())) 2 else 1
        }
        return lengthByte
    }

    fun lengthCharStringSub(string: String, byteLength: Int): Int {
        var lengthByte = 0
        if (isNotSpace(string)) for ((index, char) in string.withIndex()) {
            lengthByte += when {
                "$char".matches("[\u0391-\uFFE5]".toRegex()) -> 2
                else -> 1
            }
            if (lengthByte >= byteLength) return index
        }
        return 0
    }

    val uuid: String
        get() = UUID.randomUUID().toString().replace("-".toRegex(), "")
            .toLowerCase(Locale.getDefault())//通用唯一识别码32字符小写字符串
}