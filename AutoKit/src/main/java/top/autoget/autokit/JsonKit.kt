package top.autoget.autokit

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

object JsonKit : LoggerKit {
    @JvmOverloads
    fun getInt(json: String, key: String, defaultValue: Int = -1): Int =
        getValueByType(json, key, defaultValue, TYPE_INT) as Int

    @JvmOverloads
    fun getInt(jsonObject: JSONObject, key: String, defaultValue: Int = -1): Int =
        getValueByType(jsonObject, key, defaultValue, TYPE_INT) as Int

    @JvmOverloads
    fun getLong(json: String, key: String, defaultValue: Long = -1): Long =
        getValueByType(json, key, defaultValue, TYPE_LONG) as Long

    @JvmOverloads
    fun getLong(jsonObject: JSONObject, key: String, defaultValue: Long = -1): Long =
        getValueByType(jsonObject, key, defaultValue, TYPE_LONG) as Long

    @JvmOverloads
    fun getDouble(json: String, key: String, defaultValue: Double = -1.0): Double =
        getValueByType(json, key, defaultValue, TYPE_DOUBLE) as Double

    @JvmOverloads
    fun getDouble(jsonObject: JSONObject, key: String, defaultValue: Double = -1.0): Double =
        getValueByType(jsonObject, key, defaultValue, TYPE_DOUBLE) as Double

    @JvmOverloads
    fun getBoolean(json: String, key: String, defaultValue: Boolean = false): Boolean =
        getValueByType(json, key, defaultValue, TYPE_BOOLEAN) as Boolean

    @JvmOverloads
    fun getBoolean(jsonObject: JSONObject, key: String, defaultValue: Boolean = false): Boolean =
        getValueByType(jsonObject, key, defaultValue, TYPE_BOOLEAN) as Boolean

    @JvmOverloads
    fun getString(json: String, key: String, defaultValue: String = ""): String =
        getValueByType(json, key, defaultValue, TYPE_STRING) as String

    @JvmOverloads
    fun getString(jsonObject: JSONObject, key: String, defaultValue: String = ""): String =
        getValueByType(jsonObject, key, defaultValue, TYPE_STRING) as String

    fun getJsonObject(json: String, key: String, defaultValue: JSONObject): JSONObject =
        getValueByType(json, key, defaultValue, TYPE_JSON_OBJECT) as JSONObject

    fun getJsonObject(jsonObject: JSONObject, key: String, defaultValue: JSONObject): JSONObject =
        getValueByType(jsonObject, key, defaultValue, TYPE_JSON_OBJECT) as JSONObject

    fun getJsonArray(json: String, key: String, defaultValue: JSONArray): JSONArray =
        getValueByType(json, key, defaultValue, TYPE_JSON_ARRAY) as JSONArray

    fun getJsonArray(jsonObject: JSONObject, key: String, defaultValue: JSONArray): JSONArray =
        getValueByType(jsonObject, key, defaultValue, TYPE_JSON_ARRAY) as JSONArray

    private fun getValueByType(json: String?, key: String?, defaultValue: Any, type: Byte): Any =
        when {
            json == null || json.isEmpty() || key == null || key.isEmpty() -> defaultValue
            else -> try {
                getValueByType(JSONObject(json), key, defaultValue, type)
            } catch (e: JSONException) {
                error("$loggerTag->getValueByType: $e")
                defaultValue
            }
        }

    private const val TYPE_INT: Byte = 0x00
    private const val TYPE_LONG: Byte = 0x01
    private const val TYPE_DOUBLE: Byte = 0x02
    private const val TYPE_BOOLEAN: Byte = 0x03
    private const val TYPE_STRING: Byte = 0x04
    private const val TYPE_JSON_OBJECT: Byte = 0x05
    private const val TYPE_JSON_ARRAY: Byte = 0x06
    private fun getValueByType(
        jsonObject: JSONObject?, key: String?, defaultValue: Any, type: Byte
    ): Any = when {
        jsonObject == null || key == null || key.isEmpty() -> defaultValue
        else -> try {
            jsonObject.run {
                when (type) {
                    TYPE_INT -> getInt(key)
                    TYPE_LONG -> getLong(key)
                    TYPE_DOUBLE -> getDouble(key)
                    TYPE_BOOLEAN -> getBoolean(key)
                    TYPE_STRING -> getString(key)
                    TYPE_JSON_OBJECT -> getJSONObject(key)
                    TYPE_JSON_ARRAY -> getJSONArray(key)
                    else -> defaultValue
                }
            }
        } catch (e: JSONException) {
            error("$loggerTag->getValueByType: $e")
            defaultValue
        }
    }

    fun string2JsonObject(json: String): JSONObject? = try {
        JSONTokener(json).nextValue() as JSONObject
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    @Throws(JSONException::class)
    fun any2JsonArray(data: Any?): JSONArray = when {
        data?.javaClass?.isArray ?: false -> JSONArray().apply {
            data?.let {
                for (any in (data as Array<*>)) {
                    put(wrap(any))
                }
            }
        }
        else -> throw JSONException("Not a primitive data: ${data?.javaClass}")
    }

    fun collection2JsonArray(collection: Collection<*>?): JSONArray = JSONArray().apply {
        collection?.let {
            for (any in collection) {
                put(wrap(any))
            }
        }
    }

    fun map2JsonObject(map: Map<*, *>?): JSONObject = JSONObject().apply {
        map?.let {
            for ((key, value) in map) {
                try {
                    put(key as String, wrap(value))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun wrap(any: Any?): Any? = try {
        when (any) {
            null -> null
            is Byte, is Short, is Int, is Long, is Float, is Double, is Boolean, is Char,
            is String, is JSONArray, is JSONObject -> any
            javaClass.getPackage()?.name?.startsWith("java.") -> any.toString()
            javaClass.isArray -> any2JsonArray(any)
            is Collection<*> -> collection2JsonArray(any)
            is Map<*, *> -> map2JsonObject(any)
            else -> null
        }
    } catch (ignored: Exception) {
        null
    }

    fun any2json(any: Any?): String = StringBuilder().apply {
        when (any) {
            null -> append("\"\"")
            is Byte, is Short, is Int, is Long, is Float, is Double, is Boolean, is Char,
            is String, is BigInteger, is BigDecimal -> append("\"${string2json(any.toString())}\"")
            is Array<*> -> append(array2json(any))
            is List<*> -> append(list2json(any))
            is Set<*> -> append(set2json(any))
            is Map<*, *> -> append(map2json(any))
        }
    }.toString()

    fun string2json(string: String?): String = string?.let {
        StringBuilder().apply {
            for (char in string) {
                when (char) {
                    '"' -> append("\\\"")
                    '/' -> append("\\/")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")//退格
                    '\t' -> append("\\t")//TAB
                    '\r' -> append("\\r")//回车
                    '\n' -> append("\\n")//换行
                    '\u000C' -> append("\\f")//换页
                    else -> when (char) {
                        in '\u0000'..'\u001F' -> Integer.toHexString(char.toInt()).let {
                            append("\\u")
                            for (i in 0 until 4 - it.length) {
                                append('0')
                            }
                            append(it.toUpperCase(Locale.getDefault()))
                        }
                        else -> append(char)
                    }
                }
            }
        }.toString()
    } ?: ""

    fun array2json(array: Array<*>?): String = StringBuilder().apply {
        append("[")
        array?.let {
            when {
                array.isEmpty() -> append("]")
                else -> {
                    for (any in array) {
                        append("${any2json(any)},")
                    }
                    setCharAt(length - 1, ']')
                }
            }
        } ?: append("]")
    }.toString()

    fun list2json(list: List<*>?): String = StringBuilder().apply {
        append("[")
        list?.let {
            when {
                list.isEmpty() -> append("]")
                else -> {
                    for (any in list) {
                        append("${any2json(any)},")
                    }
                    setCharAt(length - 1, ']')
                }
            }
        } ?: append("]")
    }.toString()

    fun set2json(set: Set<*>?): String = StringBuilder().apply {
        append("[")
        set?.let {
            when {
                set.isEmpty() -> append("]")
                else -> {
                    for (any in set) {
                        append("${any2json(any)},")
                    }
                    setCharAt(length - 1, ']')
                }
            }
        } ?: append("]")
    }.toString()

    fun map2json(map: Map<*, *>?): String = StringBuilder().apply {
        append("{")
        map?.let {
            when {
                map.isEmpty() -> append("}")
                else -> {
                    for ((key, value) in map) {
                        append("${any2json(key)}:${any2json(value)},")
                    }
                    setCharAt(length - 1, '}')
                }
            }
        } ?: append("}")
    }.toString()

    @JvmOverloads
    fun formatJson(json: String, indentSpaces: Int = 4): String {
        try {
            for (char in json) {
                when {
                    char == '{' -> return JSONObject(json).toString(indentSpaces)
                    char == '[' -> return JSONArray(json).toString(indentSpaces)
                    !Character.isWhitespace(char) -> return json
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return json
    }
}