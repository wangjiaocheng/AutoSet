package top.autoget.autokit

import android.content.ClipData
import android.content.Intent
import android.os.Build.VERSION_CODES.JELLY_BEAN
import android.os.Bundle
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.collection.SimpleArrayMap
import com.google.gson.GsonBuilder
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import top.autoget.autokit.ApplicationKit.appVersionCode
import top.autoget.autokit.ApplicationKit.appVersionName
import top.autoget.autokit.DateKit.sdfDateByFullEn
import top.autoget.autokit.DateKit.sdfDateEn
import top.autoget.autokit.FileIoKit.writeFileFromString
import top.autoget.autokit.FileKit.createFileNone
import top.autoget.autokit.FileKit.pathAppCache
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.SystemKit.buildManufacturer
import top.autoget.autokit.SystemKit.buildModel
import top.autoget.autokit.SystemKit.buildVersionRelease
import top.autoget.autokit.SystemKit.buildVersionSDK
import top.autoget.autokit.ThreadKit.poolSingle
import top.autoget.autokit.VersionKit.aboveIceCreamSandwichMR1
import top.autoget.autokit.VersionKit.aboveJellyBean
import java.io.File
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.UnknownHostException
import java.text.ParseException
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import kotlin.math.min

object LogKit : LoggerKit {
    private val tags: Array<String> = arrayOf("VERBOSE", "DEBUG", "INFO", "WARN", "ERROR", "ASSERT")

    abstract class IFormatter {
        abstract fun format(any: Any?): String
    }

    private val iFormatterMap = SimpleArrayMap<Class<*>, IFormatter>()
    private val sepLine: String = System.getProperty("line.separator") ?: "\r\n"

    class Config {
        var isSwitchLog = true
        var isSwitchLogHead = true
        var isSwitchLogBorder = true
        var isSwitchLog2File = false
        var isSwitchLog2Console = true
        var isSwitchSingleTag = true
        var isTagSpace = true
            private set//tagGlobal中自动改变
        val dir: String
            get() = customDir ?: "${pathAppCache}log${File.separator}"
        var customDir: String? = null
            set(customDir) {
                field = when {
                    isSpace(customDir) -> null
                    else -> customDir?.run { if (endsWith(File.separator)) this else "$this${File.separator}" }
                }
            }
        var filePrefix = "Helper"
            set(filePrefix) {
                field = if (isSpace(filePrefix)) "Helper" else filePrefix
            }
        var saveDays = -1
            set(saveDays) {
                field = if (saveDays < 1) -1 else saveDays
            }
        private val fileFilter: String
            get() = tags[filterFile - Log.VERBOSE]
        var filterFile = Log.VERBOSE
            set(filterFile) {
                field = if (filterFile in 2..7) filterFile else Log.VERBOSE
            }
        private val consoleFilter: String
            get() = tags[filterConsole - Log.VERBOSE]
        var filterConsole = Log.VERBOSE
            set(filterConsole) {
                field = if (filterConsole in 2..7) filterConsole else Log.VERBOSE
            }
        var globalTag: String = "null"
            set(globalTag) {
                field = when {
                    isSpace(globalTag) -> "null".apply { isTagSpace = true }
                    else -> globalTag.apply { isTagSpace = false }
                }
            }
        var stackOffset = 0
            set(stackOffset) {
                field = if (stackOffset < 0) 0 else stackOffset
            }
        var stackDeep = 1
            set(stackDeep) {
                field = if (stackDeep < 1) 1 else stackDeep
            }

        fun addFormatter(iFormatter: IFormatter?): IFormatter? =
            iFormatter?.apply { iFormatterMap.put(getTypeClassFromParadigm(this), this) }

        private fun getTypeClassFromParadigm(formatter: IFormatter): Class<*>? =
            formatter.javaClass.run {
                (if (genericInterfaces.size == 1) genericInterfaces[0] else genericSuperclass).let { typeTemp ->
                    var type: Type? = (typeTemp as ParameterizedType).actualTypeArguments[0]
                    while (type is ParameterizedType) {
                        type = type.rawType
                    }
                    type.toString().run {
                        when {
                            startsWith("class ") -> substring(6)
                            startsWith("interface ") -> substring(10)
                            else -> null
                        }?.let { className ->
                            try {
                                Class.forName(className)
                            } catch (e: ClassNotFoundException) {
                                e.printStackTrace()
                                null
                            }
                        }
                    }
                }
            }

        override fun toString(): String = """
                |log:           $isSwitchLog$sepLine
                |head:          $isSwitchLogHead$sepLine
                |border:        $isSwitchLogBorder$sepLine
                |file:          $isSwitchLog2File$sepLine
                |console:       $isSwitchLog2Console$sepLine
                |singleTag:     $isSwitchSingleTag$sepLine
                |tagSpace:      $isTagSpace$sepLine
                |dir:           $dir$sepLine
                |filePrefix:    $filePrefix$sepLine
                |saveDays:      $saveDays$sepLine
                |fileFilter:    $fileFilter$sepLine
                |consoleFilter: $consoleFilter$sepLine
                |globalTag:     $globalTag$sepLine
                |stackOffset:   $stackOffset$sepLine
                |stackDeep:     $stackDeep$sepLine
                |formatter:     $iFormatterMap""".trimMargin()
    }

    val config = Config()
    fun v(vararg contents: Any) = log(Log.VERBOSE, config.globalTag, *contents)
    fun d(vararg contents: Any) = log(Log.DEBUG, config.globalTag, *contents)
    fun i(vararg contents: Any) = log(Log.INFO, config.globalTag, *contents)
    fun w(vararg contents: Any) = log(Log.WARN, config.globalTag, *contents)
    fun e(vararg contents: Any) = log(Log.ERROR, config.globalTag, *contents)
    fun a(vararg contents: Any) = log(Log.ASSERT, config.globalTag, *contents)
    fun vTag(tag: String, vararg contents: Any) = log(Log.VERBOSE, tag, *contents)
    fun dTag(tag: String, vararg contents: Any) = log(Log.DEBUG, tag, *contents)
    fun iTag(tag: String, vararg contents: Any) = log(Log.INFO, tag, *contents)
    fun wTag(tag: String, vararg contents: Any) = log(Log.WARN, tag, *contents)
    fun eTag(tag: String, vararg contents: Any) = log(Log.ERROR, tag, *contents)
    fun aTag(tag: String, vararg contents: Any) = log(Log.ASSERT, tag, *contents)
    private const val FILE = 0x10
    private const val JSON = 0x20
    private const val XML = 0x30
    fun file(content: Any) = log(FILE or Log.DEBUG, config.globalTag, content)
    fun json(content: Any) = log(JSON or Log.DEBUG, config.globalTag, content)
    fun xml(content: String) = log(XML or Log.DEBUG, config.globalTag, content)
    fun file(tag: String, content: Any) = log(FILE or Log.DEBUG, tag, content)
    fun json(tag: String, content: Any) = log(JSON or Log.DEBUG, tag, content)
    fun xml(tag: String, content: String) = log(XML or Log.DEBUG, tag, content)

    @IntDef(Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, Log.ASSERT)
    @Retention(AnnotationRetention.SOURCE)
    annotation class TYPE//详细、调试、信息、警告、错误、断言

    fun file(@TYPE type: Int, content: Any) = log(FILE or type, config.globalTag, content)
    fun json(@TYPE type: Int, content: Any) = log(JSON or type, config.globalTag, content)
    fun xml(@TYPE type: Int, content: String) = log(XML or type, config.globalTag, content)
    fun file(@TYPE type: Int, tag: String, content: Any) = log(FILE or type, tag, content)
    fun json(@TYPE type: Int, tag: String, content: Any) = log(JSON or type, tag, content)
    fun xml(@TYPE type: Int, tag: String, content: String) = log(XML or type, tag, content)
    private fun log(type: Int, tag: String, vararg contents: Any) {
        if (config.isSwitchLog && (config.isSwitchLog2Console || config.isSwitchLog2File))
            (type and 0x0f).let { typeLow ->
                (type and 0xf0).let { typeHigh ->
                    if (typeLow >= config.filterConsole || typeLow >= config.filterFile)
                        processTagAndHead(tag).let { tagHead ->
                            processBody(typeHigh, *contents).let { body ->
                                if (config.isSwitchLog2Console && typeHigh != FILE && typeLow >= config.filterConsole)
                                    print2Console(typeLow, tagHead.tag, tagHead.consoleHead, body)
                                if ((config.isSwitchLog2File || typeHigh == FILE) && typeLow >= config.filterFile)
                                    print2File(typeLow, tagHead.tag, "${tagHead.fileHead}$body")
                            }
                        }
                }
            }
    }

    private class TagHead internal constructor(
        internal var tag: String,
        internal var consoleHead: Array<String?>?, internal var fileHead: String
    )

    private fun processTagAndHead(tag: String): TagHead {
        var tagTemp = tag
        when {
            config.isTagSpace || config.isSwitchLogHead -> Throwable().stackTrace.let { stackTrace ->
                (3 + config.stackOffset).let { stackIndex ->
                    if (stackIndex >= stackTrace.size) getFileName(stackTrace[3]).let { fileName ->
                        if (config.isTagSpace && isSpace(tagTemp)) fileName.indexOf('.')
                            .let { index ->
                                tagTemp =
                                    if (index == -1) fileName else fileName.substring(0, index)
                            }//混淆可能不能发现'.'
                        return TagHead(tagTemp, null, ": ")
                    }
                    stackTrace[stackIndex].run {
                        getFileName(this).let { fileName ->
                            if (config.isTagSpace && isSpace(tagTemp)) fileName.indexOf('.')
                                .let { index ->
                                    tagTemp =
                                        if (index == -1) fileName else fileName.substring(0, index)
                                }//混淆可能不能发现'.'
                            if (config.isSwitchLogHead) Thread.currentThread().name.let { tName ->
                                Formatter().format(
                                    "%s, %s.%s(%s:%d)",
                                    tName, className, methodName, fileName, lineNumber
                                ).toString().let { head ->
                                    return when {
                                        config.stackDeep > 1 -> arrayOfNulls<String>(
                                            min(config.stackDeep, stackTrace.size - stackIndex)
                                        ).apply { this[0] = head }.let { consoleHead ->
                                            Formatter().format("%${tName.length + 2}s", "")
                                                .toString().let { space ->
                                                    for (i in 1 until consoleHead.size) {
                                                        stackTrace[i + stackIndex].run {
                                                            consoleHead[i] = Formatter().format(
                                                                "%s%s.%s(%s:%d)",
                                                                space, className, methodName,
                                                                getFileName(this), lineNumber
                                                            ).toString()
                                                        }
                                                    }
                                                }
                                            TagHead(tagTemp, consoleHead, " [$head]: ")
                                        }
                                        else -> TagHead(tagTemp, arrayOf(head), " [$head]: ")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else -> tagTemp = config.globalTag
        }
        return TagHead(tagTemp, null, ": ")
    }

    private fun getFileName(targetElement: StackTraceElement): String {
        targetElement.fileName?.let { return it }
        var className = targetElement.className
        className.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            .run { if (isNotEmpty()) className = this[size - 1] }
        className.indexOf('$')
            .let { index -> if (index != -1) className = className.substring(0, index) }
        return "$className.java"
    }//如果文件名为null，混淆文件添加“-keepattributes SourceFile,LineNumberTable”

    private fun processBody(type: Int, vararg contents: Any): String = when (contents.size) {
        1 -> formatObject(type, contents[0])
        else -> StringBuilder().apply {
            for ((index, any) in contents.withIndex()) {
                append("args[$index] = ${formatObject(any)}$sepLine")
            }
        }.toString()
    }.run { if (isEmpty()) "log nothing" else this }

    private object LogFormatter {
        internal fun formatXml(xml: String): String = try {
            StringReader(xml).use { stringReader ->
                StringWriter().use { stringWriter ->
                    StreamResult(stringWriter).let { streamResult ->
                        TransformerFactory.newInstance().newTransformer().apply {
                            setOutputProperty(OutputKeys.INDENT, "yes")
                            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
                        }.transform(StreamSource(stringReader), streamResult)
                        streamResult.writer.toString().replaceFirst(">".toRegex(), ">$sepLine")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            xml
        }

        private val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
        internal fun object2Json(any: Any): String = when (any) {
            is CharSequence -> formatJson(any.toString())
            else -> try {
                gson.toJson(any)
            } catch (t: Throwable) {
                any.toString()
            }
        }

        private fun formatJson(json: String): String = try {
            when {
                json.startsWith("{") -> JSONObject(json).toString(2)
                json.startsWith("[") -> JSONArray(json).toString(2)
                else -> json
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            json
        }

        internal fun object2String(any: Any): String = when {
            any.javaClass.isArray -> array2String(any)
            else -> (any as? Throwable)?.let { throwable2String(it) }
                ?: ((any as? Bundle)?.let { bundle2String(it) }
                    ?: ((any as? Intent)?.let { intent2String(it) } ?: any.toString()))
        }

        private fun array2String(any: Any): String = when (any) {
            is Array<*> -> any.contentDeepToString()
            is ByteArray -> any.contentToString()
            is ShortArray -> any.contentToString()
            is CharArray -> any.contentToString()
            is IntArray -> any.contentToString()
            is LongArray -> any.contentToString()
            is FloatArray -> any.contentToString()
            is DoubleArray -> any.contentToString()
            is BooleanArray -> any.contentToString()
            else -> throw IllegalArgumentException("Array has incompatible type: ${any.javaClass}")
        }

        private fun throwable2String(t: Throwable): String {
            var throwable: Throwable? = t
            while (throwable != null) {
                if (throwable is UnknownHostException) return "" else throwable = throwable.cause
            }
            StringWriter().use { stringWriter ->
                return stringWriter.apply {
                    PrintWriter(this).use { printWriter ->
                        t.printStackTrace(printWriter)
                        var cause: Throwable? = t.cause
                        while (cause != null) {
                            cause.printStackTrace(printWriter)
                            cause = cause.cause
                        }
                        printWriter.flush()
                    }
                }.toString()
            }
        }

        private fun bundle2String(bundle: Bundle): String {
            bundle.keySet().iterator().let { iterator ->
                when {
                    iterator.hasNext() -> StringBuilder(128).run {
                        append("Bundle { ")
                        while (true) {
                            iterator.next().let { key ->
                                bundle.get(key).let { value ->
                                    append("$key=")
                                    when (value) {
                                        is Bundle -> append(
                                            when {
                                                value === bundle -> "(this Bundle)"
                                                else -> bundle2String(value)
                                            }
                                        )
                                        else -> append(formatObject(value))
                                    }
                                }
                            }
                            if (iterator.hasNext()) append(", ") else return append(" }").toString()
                        }
                    }
                    else -> return "Bundle {}"
                }
            }
        }

        private fun intent2String(intent: Intent): String = StringBuilder(128).apply {
            append("Intent { ")
            var first = true
            intent.action?.let {
                if (!first) append(' ')
                first = false
                append("act=$it")
            }
            intent.data?.let {
                if (!first) append(' ')
                first = false
                append("dat=$it")
            }
            intent.type?.let {
                if (!first) append(' ')
                first = false
                append("typ=$it")
            }
            intent.getPackage()?.let {
                if (!first) append(' ')
                first = false
                append("pkg=$it")
            }
            intent.flags.let {
                if (it != 0) {
                    if (!first) append(' ')
                    first = false
                    append("flg=0x${Integer.toHexString(it)}")
                }
            }
            intent.categories?.let {
                if (!first) append(' ')
                first = false
                append("cat=[")
                var firstCategory = true
                for (string in it) {
                    if (!firstCategory) append(',')
                    firstCategory = false
                    append(string)
                }
                append("]")
            }
            intent.component?.let {
                if (!first) append(' ')
                first = false
                append("cmp=${it.flattenToShortString()}")
            }
            intent.sourceBounds?.let {
                if (!first) append(' ')
                first = false
                append("bnds=${it.toShortString()}")
            }
            intent.extras?.let {
                if (!first) append(' ')
                first = false
                append("extras={${bundle2String(it)}}")
            }
            if (aboveIceCreamSandwichMR1) intent.selector?.let {
                if (!first) append(' ')
                first = false
                append("sel={${if (it === intent) "(this Intent)" else intent2String(it)}}")
            }
            if (aboveJellyBean) intent.clipData?.let {
                if (!first) append(' ')
                first = false
                append(clipData2String(it))
            }
            append(" }")
        }.toString()

        @RequiresApi(JELLY_BEAN)
        private fun clipData2String(clipData: ClipData): String = StringBuilder().apply {
            clipData.getItemAt(0)?.let { item ->
                append("ClipData.Item { ")
                item.text?.let { append("T:$it}") }
                    ?: item.htmlText?.let { append("H:$it}") }
                    ?: item.uri?.let { append("U:$it}") }
                    ?: item.intent?.let { append("INFO:${intent2String(it)}}") }
                    ?: append("NULL}")
            } ?: append("ClipData.Item {}")
        }.toString()
    }

    private fun formatObject(type: Int, any: Any?): String = any?.let {
        when (type) {
            JSON -> LogFormatter.object2Json(any)
            XML -> LogFormatter.formatXml(any.toString())
            else -> formatObject(any)
        }
    } ?: "null"

    private fun formatObject(any: Any?): String = any?.let {
        when {
            iFormatterMap.isEmpty -> LogFormatter.object2String(any)
            else -> iFormatterMap[getClassFromAny(any)]?.format(any) ?: "null"
        }
    } ?: "null"

    private fun getClassFromAny(any: Any): Class<*> = any.javaClass.apply {
        if (isAnonymousClass || isSynthetic)
            (if (genericInterfaces.size == 1) genericInterfaces[0] else genericSuperclass).let { typeTemp ->
                var type: Type? = typeTemp
                while (type is ParameterizedType) {
                    type = type.rawType
                }
                type.toString().run {
                    when {
                        startsWith("class ") -> substring(6)
                        startsWith("interface ") -> substring(10)
                        else -> null
                    }?.let { className ->
                        try {
                            return Class.forName(className)
                        } catch (e: ClassNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }

    private fun print2Console(type: Int, tag: String, heads: Array<String?>?, msg: String) = when {
        config.isSwitchSingleTag -> printSingleTagMsg(type, tag, processSingleTagMsg(heads, msg))
        else -> {
            printBorder(type, tag, true)
            printHead(type, tag, heads)
            printMsg(type, tag, msg)
            printBorder(type, tag, false)
        }
    }

    private const val MAX_LEN = 3000
    private const val PLACEHOLDER = " "
    private const val CORNER_TOP = "┌"
    private const val CORNER_MIDDLE = "├"
    private const val CORNER_BOTTOM = "└"
    private const val DIVIDER_SIDE = "────────────────────────────────────────────────────────"
    private const val DIVIDER_MIDDLE = "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄"
    private const val BORDER_LEFT = "│ "
    private const val BORDER_TOP = "$CORNER_TOP$DIVIDER_SIDE$DIVIDER_SIDE"//“┌──”
    private const val BORDER_MIDDLE = "$CORNER_MIDDLE$DIVIDER_MIDDLE$DIVIDER_MIDDLE"//“├┄┄”
    private const val BORDER_BOTTOM = "$CORNER_BOTTOM$DIVIDER_SIDE$DIVIDER_SIDE"//“└──”
    private fun printSingleTagMsg(type: Int, tag: String, msg: String) {
        val len = msg.length
        val countOfSub = len / MAX_LEN
        val gap = "$PLACEHOLDER$sepLine"//“ \n”
        val head = "$BORDER_TOP$sepLine$BORDER_LEFT"//“┌──\n│ ”
        val foot = "$sepLine$BORDER_BOTTOM"//“\n└──”
        when {
            countOfSub > 0 -> when {
                config.isSwitchLogBorder -> msg.run {
                    Log.println(type, tag, "${substring(0, MAX_LEN)}$foot")
                    var index = MAX_LEN
                    for (i in 1 until countOfSub) {
                        Log.println(type, tag, "$gap$head${substring(index, index + MAX_LEN)}$foot")
                        index += MAX_LEN
                    }
                    if (index != len) Log.println(type, tag, "$gap$head${substring(index, len)}")
                }
                else -> msg.run {
                    Log.println(type, tag, substring(0, MAX_LEN))
                    var index = MAX_LEN
                    for (i in 1 until countOfSub) {
                        Log.println(type, tag, "$gap${substring(index, index + MAX_LEN)}")
                        index += MAX_LEN
                    }
                    if (index != len) Log.println(type, tag, "$gap${substring(index, len)}")
                }
            }
            else -> Log.println(type, tag, msg)
        }
    }

    private fun processSingleTagMsg(heads: Array<String?>?, msg: String): String =
        StringBuilder().apply {
            append("$PLACEHOLDER$sepLine")//“ \n”
            when {
                config.isSwitchLogBorder -> {
                    append("$BORDER_TOP$sepLine")//“┌──\n”
                    heads?.let {
                        for (head in heads) {
                            append("$BORDER_LEFT$head$sepLine")//“│ *\n”
                        }
                        append("$BORDER_MIDDLE$sepLine")//“├┄┄\n”
                    }
                    for (line in msg.split(sepLine.toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()) {
                        append("$BORDER_LEFT$line$sepLine")//“│ *\n”
                    }
                    append(BORDER_BOTTOM)//“└──”
                }
                else -> {
                    heads?.let {
                        for (head in heads) {
                            append("$head$sepLine")
                        }
                    }
                    append(msg)
                }
            }
        }.toString()

    private fun printBorder(type: Int, tag: String, isTop: Boolean) {
        if (config.isSwitchLogBorder)
            Log.println(type, tag, if (isTop) BORDER_TOP else BORDER_BOTTOM)
    }//“┌──”；“└──”

    private fun printHead(type: Int, tag: String, heads: Array<String?>?) = heads?.let {
        for (head in heads) {
            head?.let {
                Log.println(type, tag, if (config.isSwitchLogBorder) "$BORDER_LEFT$head" else head)
            }//“│ ”
        }
        if (config.isSwitchLogBorder) Log.println(type, tag, BORDER_MIDDLE)//“├┄┄”
    }

    private fun printMsg(type: Int, tag: String, msg: String) {
        val len = msg.length
        val countOfSub = len / MAX_LEN
        when {
            countOfSub > 0 -> {
                var index = 0
                for (i in 0 until countOfSub) {
                    printSubMsg(type, tag, msg.substring(index, index + MAX_LEN))
                    index += MAX_LEN
                }
                if (index != len) printSubMsg(type, tag, msg.substring(index, len))
            }
            else -> printSubMsg(type, tag, msg)
        }
    }

    private fun printSubMsg(type: Int, tag: String, msg: String) {
        when {
            config.isSwitchLogBorder ->
                for (line in msg.split(sepLine.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()) {
                    Log.println(type, tag, "$BORDER_LEFT$line")//“│ ”
                }
            else -> Log.println(type, tag, msg)
        }
    }

    private fun print2File(type: Int, tag: String, msg: String) =
        sdfDateByFullEn.format(Date()).run {
            ("${config.dir}${config.filePrefix}-${substring(0, 10)}.txt").let { fullPath ->
                when {
                    createFile(fullPath) -> writeFileFromString(
                        fullPath,
                        "${substring(11)}${tags[type - Log.VERBOSE]}/$tag$msg$sepLine"
                    )
                    else -> error("$loggerTag->create $fullPath failed!")
                }
            }
        }

    private fun createFile(filePath: String): Boolean =
        filePath.apply { deleteDueLogs(this) }
            .let { if (createFileNone(it)) true.apply { printDeviceInfo(it) } else false }

    private fun deleteDueLogs(filePath: String) = File(filePath).parentFile
        ?.listFiles { _, name -> name.matches(("^${config.filePrefix}-[0-9]{4}-[0-9]{2}-[0-9]{2}.txt$").toRegex()) }
        ?.let { files ->
            if (files.isNotEmpty()) try {
                sdfDateEn.run {
                    for (file in files) {
                        if ((parse(file.name.run { substring(length - 14, length - 4) })?.time
                                ?: 0) <=
                            (parse(filePath.run { substring(length - 14, length - 4) })?.time
                                ?: 0) - config.saveDays * 86400000L
                        ) poolSingle?.execute { if (!file.delete()) error("$loggerTag->delete $file failed!") }
                    }
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

    private fun printDeviceInfo(filePath: String) = """
            |********** Log Head **********
            |Date of Log        : ${filePath.run { substring(length - 14, length - 4) }}
            |Device Manufacturer: $buildManufacturer
            |Device Model       : $buildModel
            |Android Version    : $buildVersionRelease
            |Android SDK        : $buildVersionSDK
            |App VersionName    : $appVersionName
            |App VersionCode    : $appVersionCode
            |********** Log Head **********
            """.trimMargin().run { poolSingle?.execute { writeFileFromString(this, filePath) } }
}