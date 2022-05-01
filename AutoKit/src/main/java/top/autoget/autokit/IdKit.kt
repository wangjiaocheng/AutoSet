package top.autoget.autokit

import top.autoget.autokit.DateKit.sdfDateEn
import top.autoget.autokit.ValidationKit.isDate
import top.autoget.autokit.ValidationKit.isNumeric
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object IdKit : LoggerKit {
    private val idCard15: Pattern =
        "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$".toPattern()
    private val idCard18: Pattern =
        "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$".toPattern()
    private val idCard: Pattern =
        "(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$|^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|x|X)$)".toPattern()//15和18位身份证含x结尾

    fun isIdCard15(idCard: String): Boolean = idCard15.matcher(idCard).matches()
    fun isIdCard18(idCard: String): Boolean = idCard18.matcher(idCard).matches()
    fun isIdCard(idCard: String): Boolean = this.idCard.matcher(idCard).matches()
    fun isIdCardExact(idCard: String): Boolean {
        if (idCard.run { length != 15 && length != 18 })
            return false.apply { error("$loggerTag->身份证号码长度应该为15位或18位。") }
        val idCard18: String = idCard.run {
            when (length) {
                18 -> substring(0, 17)
                15 -> "${substring(0, 6)}19${substring(6, 15)}"
                else -> ""
            }
        }
        if (!isNumeric(idCard18))
            return false.apply { error("$loggerTag->身份证：15位号码都应为数字；18位号码除最后一位外，都应为数字。") }
        if (cnCityCode.get<Any?, Any?>(idCard18.substring(0, 2)) == null)
            return false.apply { error("$loggerTag->身份证地区编码错误。") }
        val strYear: String = idCard18.substring(6, 10)
        val strMonth: String = idCard18.substring(10, 12)
        val strDay: String = idCard18.substring(12, 14)
        if (!isDate("$strYear-$strMonth-$strDay"))
            return false.apply { error("$loggerTag->身份证生日无效。") }
        try {
            GregorianCalendar().run {
                if (get(Calendar.YEAR) - strYear.toInt() > 150 ||
                    time.time - (sdfDateEn.parse("$strYear-$strMonth-$strDay")?.time
                        ?: time.time) < 0
                ) return false.apply { error("$loggerTag->身份证生日不在有效范围。") }
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return false.apply { error("$loggerTag->身份证生日不在有效范围。") }
        } catch (e: ParseException) {
            e.printStackTrace()
            return false.apply { error("$loggerTag->身份证生日不在有效范围。") }
        }
        if (strMonth.toInt() > 12 || strMonth.toInt() == 0)
            return false.apply { error("$loggerTag->身份证月份无效。") }
        if (strDay.toInt() > 31 || strDay.toInt() == 0)
            return false.apply { error("$loggerTag->身份证日期无效。") }
        if (idCard.length == 18 && "$idCard18${getCheckCode18(idCard18)}" != idCard)
            return false.apply { error("$loggerTag->身份证无效，不是合法的身份证号码。") }
        return true
    }

    private val factor: IntArray = intArrayOf(7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2)
    private val suffix: CharArray =
        charArrayOf('1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2')

    private fun getCheckCode18(string: String): Char {
        var weightSum = 0
        for (i in factor.indices) {
            weightSum += (string[i] - '0') * factor[i]
        }//string[i].toString().toInt()
        return suffix[weightSum % 11]
    }

    fun validateCard(idCard: String): Boolean = idCard.trim { it <= ' ' }.run {
        when {
            validateIdCard18(this) || validateIdCard15(this) -> true
            else -> validateIdCard10(this)?.let { it[2] == "true" } ?: false
        }
    }

    val cnCityCode: MutableMap<String, String>
        get() = mutableMapOf(
            Pair("11", "北京"),
            Pair("12", "天津"),
            Pair("13", "河北"),
            Pair("14", "山西"),
            Pair("15", "内蒙古"),
            Pair("21", "辽宁"),
            Pair("22", "吉林"),
            Pair("23", "黑龙江"),
            Pair("31", "上海"),
            Pair("32", "江苏"),
            Pair("33", "浙江"),
            Pair("34", "安徽"),
            Pair("35", "福建"),
            Pair("36", "江西"),
            Pair("37", "山东"),
            Pair("41", "河南"),
            Pair("42", "湖北"),
            Pair("43", "湖南"),
            Pair("44", "广东"),
            Pair("45", "广西"),
            Pair("46", "海南"),
            Pair("50", "重庆"),
            Pair("51", "四川"),
            Pair("52", "贵州"),
            Pair("53", "云南"),
            Pair("54", "西藏"),
            Pair("61", "陕西"),
            Pair("62", "甘肃"),
            Pair("63", "青海"),
            Pair("64", "宁夏"),
            Pair("65", "新疆"),
            Pair("71", "台湾"),
            Pair("81", "香港"),
            Pair("82", "澳门"),
            Pair("91", "国外")
        )
    private const val CHINA_ID_MAX_LENGTH = 18
    fun validateIdCard18(idCard: String?): Boolean = idCard?.let {
        when (idCard.length) {
            CHINA_ID_MAX_LENGTH -> idCard.substring(0, 17).let { code17 ->
                when {
                    isNumeric(code17) -> when {
                        cnCityCode[idCard.substring(0, 2)] == null -> false
                        else -> getCheckCode18(code17) == idCard[17]
                    }
                    else -> false
                }
            }
            else -> false
        }
    } ?: false

    private const val CHINA_ID_MIN_LENGTH = 15
    fun validateIdCard15(idCard: String?): Boolean = idCard?.let {
        when (idCard.length) {
            CHINA_ID_MIN_LENGTH -> when {
                isNumeric(idCard) -> when {
                    cnCityCode[idCard.substring(0, 2)] == null -> false
                    else -> Calendar.getInstance().apply {
                        try {
                            time = SimpleDateFormat("yy", Locale.getDefault())
                                .parse(idCard.substring(6, 8)) ?: Date()
                        } catch (e: ParseException) {
                            e.printStackTrace()
                        }
                    }.let { calendar ->
                        validateDate(
                            calendar.get(Calendar.YEAR),
                            idCard.substring(8, 10).toInt(), idCard.substring(10, 12).toInt()
                        )
                    }
                }
                else -> false
            }
            else -> false
        }
    } ?: false

    private fun validateDate(idYear: Int, idMonth: Int, idDate: Int): Boolean =
        Calendar.getInstance().get(Calendar.YEAR).let { year ->
            when {
                idYear < 1930 || idYear >= year || idMonth < 1 || idMonth > 12 -> false
                else -> idDate in 1..when (idMonth) {
                    1, 3, 5, 7, 8, 10, 12 -> 31
                    4, 6, 9, 11 -> 30
                    else -> when {
                        idYear in 1931 until year &&
                                (idYear % 400 == 0 || (idYear % 4 == 0 && idYear % 100 != 0)) -> 29
                        else -> 28
                    }
                }
            }
        }

    fun card15ToCard18(idCard: String): String = when (idCard.length) {
        CHINA_ID_MIN_LENGTH -> when {
            isNumeric(idCard) -> Calendar.getInstance().apply {
                try {
                    time = SimpleDateFormat("yyMMdd", Locale.getDefault())
                        .parse(idCard.substring(6, 12)) ?: Date()
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }.let { calendar ->
                "${idCard.substring(0, 6)}${calendar.get(Calendar.YEAR)}${idCard.substring(8)}"
                    .let { idCard18 -> "$idCard18${getCheckCode18(idCard18)}" }
            }
            else -> ""
        }
        else -> ""
    }

    fun getProvinceByIdCard(idCard: String): String? = when (idCard.length) {
        CHINA_ID_MIN_LENGTH, CHINA_ID_MAX_LENGTH -> cnCityCode[idCard.substring(0, 2)] ?: ""
        else -> ""
    }

    fun getBirthdayByIdCard(idCard: String): String =
        getBirthByIdCard(idCard).replace("(\\d{4})(\\d{2})(\\d{2})".toRegex(), "$1-$2-$3")

    fun getBirthByIdCard(idCard: String): String = when (idCard.length) {
        CHINA_ID_MIN_LENGTH -> card15ToCard18(idCard).substring(6, 14)
        else -> ""
    }

    fun getAgeByIdCard(idCard: String): Int = when (idCard.length) {
        CHINA_ID_MIN_LENGTH -> Calendar.getInstance().get(Calendar.YEAR) - getYearByIdCard(idCard)
        else -> 0
    }

    fun getYearByIdCard(idCard: String): Short = when (idCard.length) {
        CHINA_ID_MIN_LENGTH -> card15ToCard18(idCard).substring(6, 10).toShort()
        else -> 0
    }

    fun getMonthByIdCard(idCard: String): Short = when (idCard.length) {
        CHINA_ID_MIN_LENGTH -> card15ToCard18(idCard).substring(10, 12).toShort()
        else -> 0
    }

    fun getDateByIdCard(idCard: String): Short = when (idCard.length) {
        CHINA_ID_MIN_LENGTH -> card15ToCard18(idCard).substring(12, 14).toShort()
        else -> 0
    }

    fun getGenderByIdCard(idCard: String): String = when (idCard.length) {
        CHINA_ID_MIN_LENGTH ->
            if (card15ToCard18(idCard).substring(16, 17).toInt() % 2 != 0) "M" else "F"
        else -> ""
    }

    val cnMinority: MutableMap<String, String>
        get() = mutableMapOf(
            Pair("汉族", "汉族"),
            Pair("壮族", "壮族"),
            Pair("满族", "满族"),
            Pair("回族", "回族"),
            Pair("苗族", "苗族"),
            Pair("维吾尔族", "维吾尔族"),
            Pair("土家族", "土家族"),
            Pair("彝族", "彝族"),
            Pair("蒙古族", "蒙古族"),
            Pair("藏族", "藏族"),
            Pair("布依族", "布依族"),
            Pair("侗族", "侗族"),
            Pair("瑶族", "瑶族"),
            Pair("朝鲜族", "朝鲜族"),
            Pair("白族", "白族"),
            Pair("哈尼族", "哈尼族"),
            Pair("哈萨克族", "哈萨克族"),
            Pair("黎族", "黎族"),
            Pair("傣族", "傣族"),
            Pair("畲族", "畲族"),
            Pair("傈僳族", "傈僳族"),
            Pair("仡佬族", "仡佬族"),
            Pair("东乡族", "东乡族"),
            Pair("高山族", "高山族"),
            Pair("拉祜族", "拉祜族"),
            Pair("水族", "水族"),
            Pair("佤族", "佤族"),
            Pair("纳西族", "纳西族"),
            Pair("羌族", "羌族"),
            Pair("土族", "土族"),
            Pair("仫佬族", "仫佬族"),
            Pair("锡伯族", "锡伯族"),
            Pair("柯尔克孜族", "柯尔克孜族"),
            Pair("达斡尔族", "达斡尔族"),
            Pair("景颇族", "景颇族"),
            Pair("毛南族", "毛南族"),
            Pair("撒拉族", "撒拉族"),
            Pair("布朗族", "布朗族"),
            Pair("塔吉克族", "塔吉克族"),
            Pair("阿昌族", "阿昌族"),
            Pair("普米族", "普米族"),
            Pair("鄂温克族", "鄂温克族"),
            Pair("怒族", "怒族"),
            Pair("京族", "京族"),
            Pair("基诺族", "基诺族"),
            Pair("德昂族", "德昂族"),
            Pair("保安族", "保安族"),
            Pair("俄罗斯族", "俄罗斯族"),
            Pair("裕固族", "裕固族"),
            Pair("乌孜别克族", "乌孜别克族"),
            Pair("门巴族", "门巴族"),
            Pair("鄂伦春族", "鄂伦春族"),
            Pair("独龙族", "独龙族"),
            Pair("塔塔尔族", "塔塔尔族"),
            Pair("赫哲族", "赫哲族"),
            Pair("珞巴族", "珞巴族")
        )

    fun validateIdCard10(idCard: String): Array<String>? = idCard.run {
        replace("[(|)]".toRegex(), "").let { card ->
            when {
                card.length == 8 || card.length == 9 || length == 10 -> when {
                    matches("^[1|5|7][0-9]{6}\\(?[0-9A-Z]\\)?$".toRegex()) ->
                        arrayOf("澳门", "N")
                    matches("^[A-Z]{1,2}[0-9]{6}\\(?[0-9A]\\)?$".toRegex()) ->
                        arrayOf("香港", "N", if (validateHKCard(this)) "true" else "false")
                    matches("^[a-zA-Z][0-9]{9}$".toRegex()) -> when (substring(1, 2)) {
                        "1" -> arrayOf("台湾", "M", if (validateTWCard(this)) "true" else "false")
                        "2" -> arrayOf("台湾", "F", if (validateTWCard(this)) "true" else "false")
                        else -> arrayOf("台湾", "N", "false")
                    }
                    else -> null
                }
                else -> null
            }
        }
    }

    val hkFirstCode: MutableMap<String, Int>
        get() = mutableMapOf(
            Pair("A", 1),
            Pair("B", 2),
            Pair("C", 3),
            Pair("N", 14),
            Pair("O", 15),
            Pair("R", 18),
            Pair("U", 21),
            Pair("W", 23),
            Pair("X", 24),
            Pair("Z", 26)
        )//香港身份首字母对应数字

    fun validateHKCard(idCard: String): Boolean {
        var card = idCard.replace("[(|)]".toRegex(), "")
        var sum: Int
        when (card.length) {
            9 -> {
                sum = (card.substring(0, 1).toUpperCase(Locale.getDefault()).toInt() - 55) * 9 +
                        (card.substring(1, 2).toUpperCase(Locale.getDefault()).toInt() - 55) * 8
                card = card.substring(1, 9)
            }
            else -> sum =
                522 + (card.substring(0, 1).toUpperCase(Locale.getDefault()).toInt() - 55) * 8
        }
        card.substring(7, 8).let { end ->
            var flag = 7
            for (char in card.substring(1, 7).toCharArray()) {
                sum += flag-- * char.toString().toInt()
            }
            return when {
                end.toUpperCase(Locale.getDefault()) == "A" -> sum + 10
                else -> sum + end.toInt()
            } % 11 == 0
        }
    }//前2位英文字符，一个英文字符表示第一位空格，前2位英文字符A-Z（10-35）；最后一位校验码为0-9的数字加上字符"A"（10）；身份证号码全部转数字，分别对应乘9-1加总，整除11有效

    val twFirstCode: MutableMap<String, Int>
        get() = mutableMapOf(
            Pair("A", 10),
            Pair("B", 11),
            Pair("C", 12),
            Pair("D", 13),
            Pair("E", 14),
            Pair("F", 15),
            Pair("G", 16),
            Pair("H", 17),
            Pair("J", 18),
            Pair("K", 19),
            Pair("L", 20),
            Pair("M", 21),
            Pair("N", 22),
            Pair("P", 23),
            Pair("Q", 24),
            Pair("R", 25),
            Pair("S", 26),
            Pair("T", 27),
            Pair("U", 28),
            Pair("V", 29),
            Pair("X", 30),
            Pair("Y", 31),
            Pair("W", 32),
            Pair("Z", 33),
            Pair("I", 34),
            Pair("O", 35)
        )//台湾身份首字母对应数字

    fun validateTWCard(idCard: String): Boolean =
        twFirstCode[idCard.substring(0, 1)]?.let { idStart ->
            var sum: Int = idStart / 10 + idStart % 10 * 9
            var flag = 8
            for (char in idCard.substring(1, 9).toCharArray()) {
                sum += flag-- * char.toString().toInt()
            }
            when {
                sum % 10 == 0 -> 0
                else -> 10 - sum % 10
            } == idCard.substring(9, 10).toInt()
        } ?: false
}