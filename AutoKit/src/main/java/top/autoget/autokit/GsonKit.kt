package top.autoget.autokit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.Reader
import java.lang.reflect.Type

object GsonKit {
    val gson: Gson
        get() = createGson(true)
    val gsonNoNulls: Gson
        get() = createGson(false)

    private fun createGson(serializeNulls: Boolean): Gson =
        GsonBuilder().apply { if (serializeNulls) serializeNulls() }.create()

    fun <T> deepClone(data: T, type: Type): T? = try {
        Gson().let { it.fromJson(it.toJson(data), type) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun <T> deepClone(data: T, clazz: Class<T>): T? = try {
        Gson().let { it.fromJson(it.toJson(data), clazz) }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    @JvmOverloads
    fun toJson(any: Any, includeNulls: Boolean = true): String =
        (if (includeNulls) gson else gsonNoNulls).toJson(any)

    fun <T> fromJson(json: String, type: Type): T = gson.fromJson(json, type)
    fun <T> fromJson(json: String, clazz: Class<T>): T = gson.fromJson(json, clazz)
    fun <T> fromJson(reader: Reader, type: Type): T = gson.fromJson(reader, type)
    fun <T> fromJson(reader: Reader, clazz: Class<T>): T = gson.fromJson(reader, clazz)
    fun getArrayType(type: Type): Type = TypeToken.getArray(type).type
    fun getListType(type: Type): Type =
        TypeToken.getParameterized(MutableList::class.java, type).type

    fun getSetType(type: Type): Type = TypeToken.getParameterized(MutableSet::class.java, type).type
    fun getMapType(keyType: Type, valueType: Type): Type =
        TypeToken.getParameterized(MutableMap::class.java, keyType, valueType).type

    fun getType(rawType: Type, vararg typeArguments: Type): Type =
        TypeToken.getParameterized(rawType, *typeArguments).type
}