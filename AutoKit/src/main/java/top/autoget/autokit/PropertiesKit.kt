package top.autoget.autokit

import java.io.IOException
import java.util.*

object PropertiesKit : LoggerKit {
    private const val FILE_NAME = "/App.properties"
    val propertiesAll: MutableMap<String, String>?
        get() = try {
            init(FILE_NAME)?.let { properties ->
                properties.propertyNames()?.let { enumeration ->
                    mutableMapOf<String, String>().apply {
                        for (propertyName in enumeration) {
                            (propertyName as String).let { this[it] = properties.getProperty(it) }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            error("$loggerTag->读取所有KEY和VALUE")
            null
        }

    fun getPropertyByKey(key: String): String? = try {
        init(FILE_NAME)?.getProperty(key)
    } catch (e: Exception) {
        e.printStackTrace()
        error("$loggerTag->根据配置文件Key获取Value错误")
        null
    }

    fun init(fileName: String): Properties? = try {
        Properties().apply { this::class.java.getResourceAsStream(fileName).use { load(it) } }
    } catch (e: IOException) {
        e.printStackTrace()
        error("$loggerTag->初始化配置文件错误")
        null
    }
}