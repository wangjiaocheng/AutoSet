package top.autoget.autokit

import top.autoget.autokit.HandleKit.backgroundHandler
import top.autoget.autokit.HandleKit.mainHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

object BusKit : LoggerKit {
    private class BusInfo internal constructor(
        var className: String, var funName: String, var paramType: String, var paramName: String,
        var sticky: Boolean, var threadMode: String, var priority: Int
    ) {
        var method: Method? = null
        var classNames: MutableList<String> = CopyOnWriteArrayList()
        override fun toString(): String =
            "BusInfo { desc: $className#$funName${if ("" == paramType) "()" else "($paramType $paramName)"}, sticky: $sticky, threadMode: $threadMode, method: $method, priority: $priority }"
    }

    private val mTag_BusInfoListMap: MutableMap<String, MutableList<BusInfo>> = hashMapOf()
    fun registerBus(
        tag: String, className: String, funName: String,
        paramType: String, paramName: String, sticky: Boolean,
        threadMode: String, priority: Int = 0
    ) {
        var busInfoList: MutableList<BusInfo>? = mTag_BusInfoListMap[tag]
        if (busInfoList == null) {
            busInfoList = ArrayList()
            mTag_BusInfoListMap[tag] = busInfoList
        }
        busInfoList
            .add(BusInfo(className, funName, paramType, paramName, sticky, threadMode, priority))
    }

    override fun toString(): String = "$loggerTag: $mTag_BusInfoListMap"
    private val mClassName_BusesMap: MutableMap<String, MutableSet<Any>> = ConcurrentHashMap()
    private val mClassName_TagsMap: MutableMap<String, MutableList<String>> = hashMapOf()
    fun register(bus: Any?) = bus?.let {
        val busClass: Class<*> = bus.javaClass
        val busClassName = busClass.name
        synchronized(mClassName_BusesMap) {
            var buses: MutableSet<Any>? = mClassName_BusesMap[busClassName]
            if (buses == null) {
                buses = CopyOnWriteArraySet()
                mClassName_BusesMap[busClassName] = buses
            }
            buses.add(bus)
        }
        var tags: MutableList<String>? = mClassName_TagsMap[busClassName]
        if (tags == null) {
            synchronized(mClassName_TagsMap) {
                tags = mClassName_TagsMap[busClassName]
                if (tags == null) {
                    tags = mutableListOf()
                    for ((key, value) in mTag_BusInfoListMap) {
                        for (busInfo in value) {
                            try {
                                if (Class.forName(busInfo.className).isAssignableFrom(busClass)) {
                                    tags?.add(key)
                                    busInfo.classNames.add(busClassName)
                                }
                            } catch (e: ClassNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                    mClassName_TagsMap[busClassName] = tags ?: mutableListOf()
                }
            }
        }
        processSticky(bus)
    }

    private val mClassName_Tag_Arg4StickyMap: MutableMap<String, MutableMap<String, Any>> =
        ConcurrentHashMap()

    private fun processSticky(bus: Any) = synchronized(mClassName_Tag_Arg4StickyMap) {
        mClassName_Tag_Arg4StickyMap[bus.javaClass.name]?.let { tagArgMap ->
            for ((key, value) in tagArgMap) {
                post(key, value)
            }
        }
    }

    private val nullAny: Any = "null"

    @JvmOverloads
    fun post(tag: String, arg: Any = nullAny, sticky: Boolean = false) {
        val busInfoList: MutableList<BusInfo>? = mTag_BusInfoListMap[tag]
        busInfoList?.let {
            for (busInfo in busInfoList) {
                if (busInfo.method == null) busInfo.method = getMethodByBusInfo(busInfo) ?: return
                invokeMethod(tag, arg, busInfo, sticky)
            }
        } ?: error("${loggerTag}->The bus of tag <$tag> is not exists.")
    }

    private fun getMethodByBusInfo(busInfo: BusInfo): Method? = try {
        when (busInfo.paramType) {
            "" -> Class.forName(busInfo.className).getDeclaredMethod(busInfo.funName)
            else -> Class.forName(busInfo.className)
                .getDeclaredMethod(busInfo.funName, getClassName(busInfo.paramType))
        }
    } catch (e: ClassNotFoundException) {
        e.printStackTrace()
        null
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
        null
    }

    @Throws(ClassNotFoundException::class)
    private fun getClassName(paramType: String): Class<*>? = when (paramType) {
        "byte" -> Byte::class.javaPrimitiveType
        "short" -> Short::class.javaPrimitiveType
        "char" -> Char::class.javaPrimitiveType
        "int" -> Int::class.javaPrimitiveType
        "long" -> Long::class.javaPrimitiveType
        "float" -> Float::class.javaPrimitiveType
        "double" -> Double::class.javaPrimitiveType
        "boolean" -> Boolean::class.javaPrimitiveType
        else -> Class.forName(paramType)
    }

    private fun invokeMethod(tag: String, arg: Any, busInfo: BusInfo, sticky: Boolean) =
        Runnable { realInvokeMethod(tag, arg, busInfo, sticky) }.let { runnable ->
            when (busInfo.threadMode) {
                "MAIN" -> mainHandler.post(runnable)
                "SINGLE" -> ThreadKit.poolSingle?.execute(runnable)
                "CACHED" -> ThreadKit.poolCached?.execute(runnable)
                "IO" -> ThreadKit.poolIo?.execute(runnable)
                "CPU" -> ThreadKit.poolCpu?.execute(runnable)
                else -> backgroundHandler.post(runnable)
            }
        }

    private fun realInvokeMethod(tag: String, arg: Any, busInfo: BusInfo, sticky: Boolean) {
        val buses: MutableSet<Any> = hashSetOf()
        for (className in busInfo.classNames) {
            val subBuses: MutableSet<Any>? = mClassName_BusesMap[className]
            subBuses?.run { if (isNotEmpty()) buses.addAll(subBuses) }
        }
        when (buses.size) {
            0 -> if (!sticky) error("${loggerTag}->The bus of tag <$tag> was not registered before.")
            else -> try {
                when {
                    arg === nullAny -> for (bus in buses) {
                        busInfo.method?.invoke(bus)
                    }
                    else -> for (bus in buses) {
                        busInfo.method?.invoke(bus, arg)
                    }
                }
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }
    }

    fun unregister(bus: Any?) = bus?.let {
        synchronized(mClassName_BusesMap) {
            mClassName_BusesMap[bus.javaClass.name]?.let { buses ->
                when {
                    buses.contains(bus) -> buses.remove(bus)
                    else -> error("$loggerTag->The bus of <$bus> was not registered before.")
                }
            } ?: error("$loggerTag->The bus of <$bus> was not registered before.")
        }
    }

    @JvmOverloads
    fun postSticky(tag: String, arg: Any = nullAny) {
        val busInfoList: MutableList<BusInfo>? = mTag_BusInfoListMap[tag]
        busInfoList?.let {
            for (busInfo in busInfoList) {
                when {
                    busInfo.sticky -> {
                        synchronized(mClassName_Tag_Arg4StickyMap) {
                            var tagArgMap: MutableMap<String, Any>? =
                                mClassName_Tag_Arg4StickyMap[busInfo.className]
                            if (tagArgMap == null) {
                                tagArgMap = hashMapOf()
                                mClassName_Tag_Arg4StickyMap[busInfo.className] = tagArgMap
                            }
                            tagArgMap.put(tag, arg)
                        }
                        post(tag, arg, true)
                    }
                    else -> post(tag, arg)
                }
            }
        } ?: error("${loggerTag}->The bus of tag <$tag> is not exists.")
    }

    fun removeSticky(tag: String) {
        val busInfoList: MutableList<BusInfo>? = mTag_BusInfoListMap[tag]
        busInfoList?.let {
            for (busInfo in busInfoList) {
                when {
                    busInfo.sticky -> {
                        synchronized(mClassName_Tag_Arg4StickyMap) {
                            val tagArgMap: MutableMap<String, Any>? =
                                mClassName_Tag_Arg4StickyMap[busInfo.className]
                            when {
                                tagArgMap?.containsKey(tag) == true -> tagArgMap.remove(tag)
                                else -> {
                                    error("${loggerTag}->The sticky bus of tag <$tag> didn't post.")
                                    return
                                }
                            }
                        }
                    }
                    else -> error("${loggerTag}->The bus of tag <$tag> is not sticky.")
                }
            }
        } ?: error("${loggerTag}->The bus of tag <$tag> is not exists.")
    }

    init {
        inject()
    }

    private fun inject() {}
    enum class ThreadMode { MAIN, SINGLE, CACHED, IO, CPU, POSTING }

    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
    )
    @Retention(AnnotationRetention.BINARY)
    annotation class Bus(
        val tag: String, val sticky: Boolean = false,
        val threadMode: ThreadMode = ThreadMode.POSTING, val priority: Int = 0
    )
}