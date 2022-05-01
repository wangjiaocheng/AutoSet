package top.autoget.autokit

import android.util.Log

interface LoggerKit {
    val loggerTag: String
        get() = getTag(javaClass)
}

private fun getTag(clazz: Class<*>): String =
    clazz.simpleName.let { tag -> if (tag.length > 23) tag.substring(0, 23) else tag }

fun getLogger(tag: String): LoggerKit = object : LoggerKit {
    init {
        assert(tag.length <= 23) { "The maximum tag length is 23, got $tag" }
    }

    override val loggerTag = tag
}

fun getLogger(clazz: Class<*>): LoggerKit = object : LoggerKit {
    override val loggerTag = getTag(clazz)
}

inline fun <reified T : Any> getLogger(): LoggerKit = getLogger(T::class.java)
private inline fun log(
    logger: LoggerKit, message: Any?, thr: Throwable?, level: Int,
    f: (String, String) -> Unit, fThrowable: (String, String, Throwable) -> Unit
) {
    val tag = logger.loggerTag
    if (Log.isLoggable(tag, level)) thr?.let { fThrowable(tag, message?.toString() ?: "null", it) }
        ?: f(tag, message?.toString() ?: "null")
}

fun LoggerKit.verbose(message: Any?, thr: Throwable? = null) =
    log(this, message, thr, Log.VERBOSE, { tag, msg -> Log.v(tag, msg) },
        { tag, msg, throwable -> Log.v(tag, msg, throwable) })

fun LoggerKit.debug(message: Any?, thr: Throwable? = null) =
    log(this, message, thr, Log.DEBUG, { tag, msg -> Log.d(tag, msg) },
        { tag, msg, throwable -> Log.d(tag, msg, throwable) })

fun LoggerKit.info(message: Any?, thr: Throwable? = null) =
    log(this, message, thr, Log.INFO, { tag, msg -> Log.i(tag, msg) },
        { tag, msg, throwable -> Log.i(tag, msg, throwable) })

fun LoggerKit.warn(message: Any?, thr: Throwable? = null) =
    log(this, message, thr, Log.WARN, { tag, msg -> Log.w(tag, msg) },
        { tag, msg, throwable -> Log.w(tag, msg, throwable) })

fun LoggerKit.error(message: Any?, thr: Throwable? = null) =
    log(this, message, thr, Log.ERROR, { tag, msg -> Log.e(tag, msg) },
        { tag, msg, throwable -> Log.e(tag, msg, throwable) })

fun LoggerKit.wtf(message: Any?, thr: Throwable? = null) =
    thr?.let { Log.wtf(loggerTag, message?.toString() ?: "null", it) }
        ?: Log.wtf(loggerTag, message?.toString() ?: "null")

inline fun LoggerKit.verbose(message: () -> Any?) {
    val tag = loggerTag
    if (Log.isLoggable(tag, Log.VERBOSE)) Log.v(tag, message()?.toString() ?: "null")
}

inline fun LoggerKit.debug(message: () -> Any?) {
    val tag = loggerTag
    if (Log.isLoggable(tag, Log.DEBUG)) Log.d(tag, message()?.toString() ?: "null")
}

inline fun LoggerKit.info(message: () -> Any?) {
    val tag = loggerTag
    if (Log.isLoggable(tag, Log.INFO)) Log.i(tag, message()?.toString() ?: "null")
}

inline fun LoggerKit.warn(message: () -> Any?) {
    val tag = loggerTag
    if (Log.isLoggable(tag, Log.WARN)) Log.w(tag, message()?.toString() ?: "null")
}

inline fun LoggerKit.error(message: () -> Any?) {
    val tag = loggerTag
    if (Log.isLoggable(tag, Log.ERROR)) Log.e(tag, message()?.toString() ?: "null")
}

inline fun Throwable.getStackTraceString(): String = Log.getStackTraceString(this)