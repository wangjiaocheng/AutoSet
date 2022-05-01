package top.autoget.autokit

import top.autoget.autokit.ConvertKit.DAY
import top.autoget.autokit.ConvertKit.date2Millis
import top.autoget.autokit.ConvertKit.millis2Date
import top.autoget.autokit.ConvertKit.millis2String
import top.autoget.autokit.ConvertKit.millis2TimeSpan
import top.autoget.autokit.ConvertKit.millis2TimeSpanFit
import top.autoget.autokit.ConvertKit.millis2TimeSpanFitByNow
import top.autoget.autokit.ConvertKit.string2Date
import top.autoget.autokit.ConvertKit.string2Millis
import top.autoget.autokit.ConvertKit.timeSpan2Millis
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object DateKit : LoggerKit {
    val timeZoneNameCurrent: String
        get() = createGmtOffsetString()

    @JvmOverloads
    fun createGmtOffsetString(
        includeGmt: Boolean = true, includeMinuteSeparator: Boolean = true,
        offsetMillis: Int = TimeZone.getDefault().rawOffset
    ): String = StringBuilder(9).apply {
        (offsetMillis / 60000).let {
            if (includeGmt) append("GMT")
            append(if (it < 0) '-' else '+')
            val offsetMinutes = if (it < 0) -it else it
            appendNumber(this, 2, offsetMinutes / 60)
            if (includeMinuteSeparator) append(':')
            appendNumber(this, 2, offsetMinutes % 60)
        }
    }.toString()

    private fun appendNumber(stringBuilder: StringBuilder, count: Int, value: Int): StringBuilder =
        stringBuilder.apply {
            for (i in 0 until count - value.toString().length) {
                append('0')
            }
            append("$value")
        }

    val sdfDateExcel: SimpleDateFormat
        get() = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val sdfDateByFullFileName: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
    val sdfYear: SimpleDateFormat
        get() = SimpleDateFormat("yyyy", Locale.getDefault())
    val sdfMonth: SimpleDateFormat
        get() = SimpleDateFormat("MM", Locale.getDefault())
    val sdfDay: SimpleDateFormat
        get() = SimpleDateFormat("dd", Locale.getDefault())
    val sdfHour: SimpleDateFormat
        get() = SimpleDateFormat("HH", Locale.getDefault())
    val sdfMinute: SimpleDateFormat
        get() = SimpleDateFormat("mm", Locale.getDefault())
    val sdfSecond: SimpleDateFormat
        get() = SimpleDateFormat("ss", Locale.getDefault())
    val sdfMillisecond: SimpleDateFormat
        get() = SimpleDateFormat("SSS", Locale.getDefault())
    val sdfTimeCn: SimpleDateFormat
        get() = SimpleDateFormat("HH时mm分ss秒", Locale.getDefault())
    val sdfDateCn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
    val sdfDateByHourCn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy年MM月dd日 HH时", Locale.getDefault())
    val sdfDateByMinuteCn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy年MM月dd日 HH时mm分", Locale.getDefault())
    val sdfDateByFullCn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒", Locale.getDefault())
    val sdfDateByAllCn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒SSS毫秒", Locale.getDefault())
    val sdfTimeEn: SimpleDateFormat
        get() = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val sdfDateEn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfDateByHourEn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd HH", Locale.getDefault())
    val sdfDateByMinuteEn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val sdfDateByFullEn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val sdfDateByAllEn: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault())
    val sdfTimeX: SimpleDateFormat
        get() = SimpleDateFormat("HHmmss", Locale.getDefault())
    val sdfDateX: SimpleDateFormat
        get() = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val sdfDateByHourX: SimpleDateFormat
        get() = SimpleDateFormat("yyyyMMddHH", Locale.getDefault())
    val sdfDateByMinuteX: SimpleDateFormat
        get() = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
    val sdfDateByFullX: SimpleDateFormat
        get() = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    val sdfDateByAllX: SimpleDateFormat
        get() = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault())
    val nowDateByFullRandom2: String
        get() = "${sdfDateByFullX.format(Date())}${Random().nextInt(100)}"
    val nowDateByFullRandom4: String
        get() = "${sdfDateByFullX.format(Date())}${Random().nextInt(10000)}"
    val nowDateByFullRandom6: String
        get() = "${sdfDateByFullX.format(Date())}${Random().nextInt(1000000)}"
    val nowDateByFullRandom8: String
        get() = "${sdfDateByFullX.format(Date())}${Random().nextInt(100000000)}"
    val nowDateByAllRandom2: String
        get() = "${sdfDateByAllX.format(Date())}${Random().nextInt(100)}"
    val nowDateByAllRandom4: String
        get() = "${sdfDateByAllX.format(Date())}${Random().nextInt(10000)}"
    val nowDateByAllRandom6: String
        get() = "${sdfDateByAllX.format(Date())}${Random().nextInt(1000000)}"
    val nowDateByAllRandom8: String
        get() = "${sdfDateByAllX.format(Date())}${Random().nextInt(100000000)}"
    val calendarToday: String
        get() = Calendar.getInstance()
            .run { "${get(Calendar.YEAR)}-${get(Calendar.MONTH) + 1}-${get(Calendar.DAY_OF_MONTH)}" }
    val calendarTodayNextMonth: String
        get() = Calendar.getInstance()
            .run { "${get(Calendar.YEAR)}-${get(Calendar.MONTH) + 1}-${get(Calendar.DAY_OF_MONTH) + 2}" }
    private val sdfThreadLocal = ThreadLocal<SimpleDateFormat>()
    val sdfDateByFull: SimpleDateFormat
        get() = sdfThreadLocal.get() ?: sdfDateByFullEn.apply { sdfThreadLocal.set(this) }

    fun getTimeEn(time: String?): String = try {
        time?.let { SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(it) }
            ?.let { sdfDateByFull.format(it) } ?: ""
    } catch (e: ParseException) {
        e.printStackTrace()
        ""
    }//Wed Jan 01 00:00:00 CST 2020->2020-01-01 00:00:00

    @JvmOverloads
    fun getTimeCn(
        time: String?,
        dataFormatEn: DateFormat = sdfDateByAllEn, dataFormatCn: DateFormat = sdfDateByAllCn
    ): String = try {
        time?.let { dataFormatEn.parse(time)?.let { dataFormatCn.format(it) } } ?: ""
    } catch (e: ParseException) {
        e.printStackTrace()
        ""
    }

    fun getTimeAlone(time: String?): String = time?.let {
        Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日(\\d{1,2})时(\\d{1,2})").matcher(time).run {
            when {
                find() -> "${group(1)},${group(2)},${group(3)},${group(4)},${group(5)}"
                else -> ""
            }
        }
    } ?: ""//2020年1月1日00时00分->2020,1,1,00,00

    fun getYyyy(time: String?): Int = try {
        getYyyy(string2Date(time))
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }

    fun getYyyy(millis: Long?): Int = try {
        getYyyy(millis2Date(millis))
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }

    private val calendar: Calendar = Calendar.getInstance()
    fun getYyyy(date: Date?): Int =
        date?.let { calendar.apply { time = date }.get(Calendar.YEAR) } ?: 0

    fun getMm(time: String?): Int = try {
        getMm(string2Date(time))
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }

    fun getMm(millis: Long?): Int = try {
        getMm(millis2Date(millis))
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }

    fun getMm(date: Date?): Int =
        date?.let { calendar.apply { time = date }.get(Calendar.MONTH) } ?: 0

    fun getDd(time: String?): Int = try {
        getDd(string2Date(time))
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }

    fun getDd(millis: Long?): Int = try {
        getDd(millis2Date(millis))
    } catch (e: ParseException) {
        e.printStackTrace()
        0
    }

    fun getDd(date: Date?): Int =
        date?.let { calendar.apply { time = date }.get(Calendar.DATE) } ?: 0

    fun getWeekNumberCn(time: String?): Int = getWeekNumberCn(string2Date(time))
    fun getWeekNumberCn(millis: Long?): Int = getWeekNumberCn(millis2Date(millis))
    fun getWeekNumberCn(date: Date?): Int =
        getWeekNumber(date).let { if (Calendar.SUNDAY == it) 7 else it - 1 }

    fun getWeekNumber(time: String?): Int = getWeekNumber(string2Date(time))
    fun getWeekNumber(millis: Long?): Int = getWeekNumber(millis2Date(millis))
    fun getWeekNumber(date: Date?): Int =
        date?.let { calendar.apply { time = date }.get(Calendar.DAY_OF_WEEK) } ?: 0

    fun getWeekOfMonth(time: String?): Int = getWeekOfMonth(string2Date(time))
    fun getWeekOfMonth(millis: Long?): Int = getWeekOfMonth(millis2Date(millis))
    fun getWeekOfMonth(date: Date?): Int =
        date?.let { calendar.apply { time = date }.get(Calendar.WEEK_OF_MONTH) } ?: 0

    fun getWeekOfYear(time: String?): Int = getWeekOfYear(string2Date(time))
    fun getWeekOfYear(millis: Long?): Int = getWeekOfYear(millis2Date(millis))
    fun getWeekOfYear(date: Date?): Int =
        date?.let { calendar.apply { time = date }.get(Calendar.WEEK_OF_YEAR) } ?: 0

    fun getDaysForMonth(year: Int, month: Int): Int = calendar.apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)//0-11
        set(Calendar.DATE, 1)
        roll(Calendar.DATE, -1)//月份不变，最后一天
    }.get(Calendar.DATE)

    fun getDaysOfMonth(year: Int, month: Int): Int = when {
        month < 1 || month > 12 -> 0
        else -> when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            else -> if (isLeapYear(year)) 29 else 28
        }
    }

    fun isLeapYear(time: String?): Boolean = isLeapYear(string2Date(time))
    fun isLeapYear(millis: Long?): Boolean = isLeapYear(millis2Date(millis))
    fun isLeapYear(date: Date?): Boolean = isLeapYear(getYyyy(date))
    fun isLeapYear(year: Int): Boolean = year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)

    @JvmOverloads
    fun getSecondsNightOrMorning(calendar: Calendar, isNight: Boolean = true): Long =
        Calendar.getInstance().apply {
            set(
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0
            )//抹除时间->今日零点即今日早上
            if (isNight) getDayForwardOrBackward(this)//向后一天->明日零点即今日晚上
        }.timeInMillis

    @JvmOverloads
    fun getDayForwardOrBackward(calendar: Calendar, isForward: Boolean = true) = calendar.run {
        get(Calendar.MONTH).let {
            when {
                isForward -> when {
                    get(Calendar.DAY_OF_MONTH) == getDaysOfMonth(get(Calendar.YEAR), it + 1) ->
                        when (it) {
                            11 -> {
                                roll(Calendar.YEAR, true)
                                set(Calendar.MONTH, 0)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }//12月31日->下年1月1日
                            else -> {
                                roll(Calendar.MONTH, true)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }//非12月的最后一天->下个月1日
                        }//每月最后一天
                    else -> roll(Calendar.DAY_OF_MONTH, 1)//月内日期加一
                }//后一天
                else -> when {
                    get(Calendar.DAY_OF_MONTH) == 1 -> when (it) {
                        0 -> {
                            roll(Calendar.YEAR, false)
                            set(Calendar.MONTH, 11)
                            set(Calendar.DAY_OF_MONTH, 31)
                        }//1月1日->上年12月31日
                        else -> {
                            roll(Calendar.MONTH, false)
                            set(Calendar.DAY_OF_MONTH, getDaysOfMonth(get(Calendar.YEAR), it))
                        }//非1月的1日->上个月最后一天
                    }//每月1日
                    else -> roll(Calendar.DAY_OF_MONTH, false)//月内日期减一
                }//前一天
            }
        }
    }

    fun getNowOffsetDay(date: Date?, @ConvertKit.TimeUnit unit: Int): String =
        date?.let { sdfDateEn.format(Date(date.time + unit * 24 * 60 * 60 * 1000)) } ?: ""

    val yesterdayString: String
        get() = getYesterdayString(sdfDateByFull)

    fun getYesterdayString(format: DateFormat): String =
        format.format(Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time)

    val nowString: String
        get() = getNowString(sdfDateByFull)

    fun getNowString(format: DateFormat): String = format.format(Date())
    val nowMillis: Long
        get() = System.currentTimeMillis()
    val nowDate: Date
        get() = Date()

    fun getStringByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, time: String? = nowString
    ): String? = time?.let { millis2String(string2Millis(time) + timeSpan2Millis(timeSpan, unit)) }

    fun getStringByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, millis: Long? = nowMillis
    ): String? = millis?.let { millis2String(millis + timeSpan2Millis(timeSpan, unit)) }

    fun getStringByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, date: Date? = nowDate
    ): String? = date?.let { millis2String(date2Millis(date) + timeSpan2Millis(timeSpan, unit)) }

    fun getMillisByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, time: String? = nowString
    ): Long? = time?.let { string2Millis(time) + timeSpan2Millis(timeSpan, unit) }

    fun getMillisByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, millis: Long? = nowMillis
    ): Long? = millis?.let { millis + timeSpan2Millis(timeSpan, unit) }

    fun getMillisByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, date: Date? = nowDate
    ): Long? = date?.let { date2Millis(date) + timeSpan2Millis(timeSpan, unit) }

    fun getDateByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, time: String? = nowString
    ): Date? = time?.let { millis2Date(string2Millis(time) + timeSpan2Millis(timeSpan, unit)) }

    fun getDateByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, millis: Long? = nowMillis
    ): Date? = millis?.let { millis2Date(millis + timeSpan2Millis(timeSpan, unit)) }

    fun getDateByTimeSpan(
        timeSpan: Long, @ConvertKit.TimeUnit unit: Int, date: Date? = nowDate
    ): Date? = date?.let { millis2Date(date2Millis(date) + timeSpan2Millis(timeSpan, unit)) }

    @JvmOverloads
    fun getTimeSpan(
        time1: String?, @ConvertKit.TimeUnit unit: Int, time2: String? = nowString
    ): Long = millis2TimeSpan(string2Millis(time2) - string2Millis(time1), unit)

    @JvmOverloads
    fun getTimeSpan(
        millis1: Long?, @ConvertKit.TimeUnit unit: Int, millis2: Long? = nowMillis
    ): Long = millis2TimeSpan((millis2?.let { it } ?: 0L) - (millis1?.let { it } ?: 0L), unit)

    @JvmOverloads
    fun getTimeSpan(date1: Date?, @ConvertKit.TimeUnit unit: Int, date2: Date? = nowDate): Long =
        millis2TimeSpan(date2Millis(date2) - date2Millis(date1), unit)

    @JvmOverloads
    fun getTimeSpanFit(time1: String?, precision: Int = 5, time2: String? = nowString): String =
        millis2TimeSpanFit(string2Millis(time2) - string2Millis(time1), precision)

    @JvmOverloads
    fun getTimeSpanFit(millis1: Long?, precision: Int = 5, millis2: Long? = nowMillis): String =
        millis2TimeSpanFit((millis2?.let { it } ?: 0L) - (millis1?.let { it } ?: 0L), precision)

    @JvmOverloads
    fun getTimeSpanFit(date1: Date?, precision: Int = 5, date2: Date? = nowDate): String =
        millis2TimeSpanFit(date2Millis(date2) - date2Millis(date1), precision)

    fun getTimeSpanFitByNow(time: String?): String =
        time?.let { millis2TimeSpanFitByNow(string2Millis(it)) } ?: ""

    fun getTimeSpanFitByNow(date: Date?): String =
        date?.let { millis2TimeSpanFitByNow(it.time) } ?: ""

    fun getTimeSpanFitByNow(millis: Long?): String =
        millis?.let { millis2TimeSpanFitByNow(it) } ?: ""

    fun timeOffset(date: Date): String = String.format("%tz", date)
    fun ymd(date: Date): String = String.format("%tF", date)
    fun mdy(date: Date): String = String.format("%tD", date)
    fun amOrPm(date: Date): String = String.format("%tp", date)
    fun hmsAmOrPm(date: Date): String = String.format("%tr", date)
    fun hms(date: Date): String = String.format("%tT", date)
    fun hm(date: Date): String = String.format("%tR", date)
    fun timeAll(date: Date): String = String.format("%tc", date)//星期三 一月 01 00:00:00 CST 2020
    fun time2Second(date: Date): String = String.format("%ts", date)
    fun time2Millis(date: Date): String = String.format("%tQ", date)
    fun yearFullName(date: Date): String = String.format("%tY", date)
    fun yearReferred(date: Date): String = String.format("%ty", date)
    fun month(date: Date): String = String.format("%tm", date)
    fun monthFullName(date: Date): String = String.format("%tB", date)
    fun monthReferred(date: Date): String = String.format("%tb", date)
    fun weekFullName(date: Date): String = String.format("%tA", date)
    fun weekReferred(date: Date): String = String.format("%ta", date)
    fun day2Year(date: Date): String = String.format("%tj", date)
    fun dayOne(date: Date): String = String.format("%te", date)//1-31
    fun dayTwo(date: Date): String = String.format("%td", date)//01-31
    fun hourL(date: Date): String = String.format("%tl", date)//12制
    fun hourH(date: Date): String = String.format("%tH", date)//24制
    fun minute(date: Date): String = String.format("%tM", date)//分2位
    fun second(date: Date): String = String.format("%tS", date)//秒2位
    fun millis(date: Date): String = String.format("%tL", date)//毫秒3位
    fun subtle(date: Date): String = String.format("%tN", date)//微秒9位
    fun isDateTrue(yyyyMMdd: String?): Boolean = try {
        true.apply { sdfDateX.apply { isLenient = false }.parse("$yyyyMMdd") }
    } catch (e: ParseException) {
        e.printStackTrace()
        false
    }

    fun isDateReal(yyyyMMdd: String?): Boolean = yyyyMMdd?.run {
        when {
            length == 8 && matches("[0-9]+".toRegex()) ->
                substring(0, 4).toInt().let { year ->
                    substring(4, 6).toInt().let { month ->
                        substring(6, 8).toInt().let { day ->
                            when {
                                year > 0 && month in 0..12 && day in 0..31 -> when (month) {
                                    1, 3, 5, 7, 8, 10, 12 -> day <= 31
                                    4, 6, 9, 11 -> day <= 30
                                    else -> when {
                                        year % 400 == 0 || (year % 4 == 0 && year % 100 != 0) -> day <= 29
                                        else -> day <= 28
                                    }
                                }
                                else -> false
                            }
                        }
                    }
                }
            else -> false
        }
    } ?: false

    fun isDaySame(c1: Calendar, c2: Calendar): Boolean =
        c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH)

    fun isToday(time: String?): Boolean = isToday(string2Millis(time))
    fun isToday(date: Date?): Boolean = isToday(date2Millis(date))
    val zeroTimeOfToday: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun isToday(millis: Long?): Boolean =
        millis?.let { millis >= zeroTimeOfToday && millis < zeroTimeOfToday + DAY } ?: false

    fun compareTime(time1: String?, time2: String?, format: DateFormat?): Int = format?.let {
        string2Date(time1, it)?.let { date1 ->
            string2Date(time2, it)?.let { date2 ->
                try {
                    Calendar.getInstance().apply { time = date1 }
                        .compareTo(Calendar.getInstance().apply { time = date2 })
                } catch (e: ParseException) {
                    e.printStackTrace()
                    0
                }
            } ?: 0
        } ?: 0
    } ?: 0

    fun isEqualTime(time1: String?, time2: String?): Boolean = try {
        string2Date(time1) == string2Date(time2)
    } catch (e: ParseException) {
        e.printStackTrace()
        false
    }

    fun isEqualTime(millis1: Long?, millis2: Long?): Boolean = millis2 == millis1
    fun isEqualTime(date1: Date?, date2: Date?): Boolean = date2 == date1
    fun isBeforeTime(time1: String?, time2: String?): Boolean = try {
        string2Date(time2)?.before(string2Date(time1)) ?: false
    } catch (e: ParseException) {
        e.printStackTrace()
        false
    }

    fun isBeforeTime(millis1: Long?, millis2: Long?): Boolean = try {
        (millis2 ?: 0) > (millis1 ?: 0)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }

    fun isBeforeTime(date1: Date?, date2: Date?): Boolean = date2?.before(date1) ?: false
    fun getWeekCn(time: String?): String = getWeekCn(string2Date(time))
    fun getWeekCn(millis: Long?): String = getWeekCn(millis2Date(millis))
    fun getWeekCn(date: Date?): String =
        date?.let { SimpleDateFormat("E", Locale.CHINA).format(date) } ?: ""

    fun getWeekUs(time: String?): String = getWeekUs(string2Date(time))
    fun getWeekUs(millis: Long?): String = getWeekUs(millis2Date(millis))
    fun getWeekUs(date: Date?): String =
        date?.let { SimpleDateFormat("EEEE", Locale.US).format(date) } ?: ""

    fun getValueByCalendarField(time: String?, field: Int): Int =
        getValueByCalendarField(string2Date(time), field)

    fun getValueByCalendarField(millis: Long?, field: Int): Int =
        millis?.let { Calendar.getInstance().apply { timeInMillis = millis }.get(field) } ?: 0

    fun getValueByCalendarField(date: Date?, field: Int): Int =
        date?.let { Calendar.getInstance().apply { time = date }.get(field) } ?: 0
}