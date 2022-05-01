package top.autoget.autokit

import java.util.concurrent.ConcurrentHashMap

object ApiKit : LoggerKit {
    private val apiMapInject: MutableMap<Class<*>?, Class<*>> = hashMapOf()
    fun registerApi(apiClass: Class<*>) {
        apiMapInject[apiClass.superclass] = apiClass
    }

    override fun toString(): String = "$loggerTag: $apiMapInject"
    abstract class BaseApi

    private val apiMap: MutableMap<Class<*>, BaseApi?> = ConcurrentHashMap()
    fun getApi(apiClass: Class<BaseApi>): BaseApi? {
        var baseApi: BaseApi? = apiMap[apiClass]
        if (baseApi == null) synchronized(this) {
            baseApi = apiMap[apiClass]
            if (baseApi == null) apiMapInject[apiClass]?.run {
                try {
                    baseApi = newInstance() as BaseApi
                    apiMap[apiClass] = baseApi
                } catch (ignore: Exception) {
                    error("$loggerTag->The <$this> has no parameterless constructor.")
                    return null
                }
            } ?: run {
                error("$loggerTag->The <$apiClass> doesn't implement.")
                return null
            }
        }
        return baseApi
    }

    init {
        inject()
    }

    private fun inject() {}

    @Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.BINARY)
    annotation class Api(val isMock: Boolean = false)
}