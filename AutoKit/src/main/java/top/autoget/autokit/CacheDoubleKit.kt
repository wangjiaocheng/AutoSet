package top.autoget.autokit

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

object CacheDoubleKit {
    class CacheDouble private constructor(
        private val cacheMemory: CacheMemoryKit.CacheMemory,
        private val cacheDisk: CacheDiskKit.CacheDisk
    ) {
        val cacheMemoryCount: Int
            get() = cacheMemory.cacheCount
        val cacheDiskCount: Int
            get() = cacheDisk.cacheCount
        val cacheDiskSize: Long
            get() = cacheDisk.cacheSize

        @JvmOverloads
        fun put(key: String, value: ByteArray?, saveTime: Int = -1) {
            cacheMemory.put(key, value, saveTime)
            cacheDisk.put(key, value, saveTime)
        }

        @JvmOverloads
        fun getBytes(key: String, defaultValue: ByteArray? = null): ByteArray? =
            cacheMemory.get<ByteArray>(key) ?: cacheDisk.getBytes(key, defaultValue)

        @JvmOverloads
        fun put(key: String, value: String, saveTime: Int = -1) {
            cacheMemory.put(key, value, saveTime)
            cacheDisk.put(key, value, saveTime)
        }

        @JvmOverloads
        fun getString(key: String, defaultValue: String? = null): String? =
            cacheMemory.get<String>(key) ?: cacheDisk.getString(key, defaultValue)

        @JvmOverloads
        fun put(key: String, value: JSONObject, saveTime: Int = -1) {
            cacheMemory.put(key, value, saveTime)
            cacheDisk.put(key, value, saveTime)
        }

        @JvmOverloads
        fun getJSONObject(key: String, defaultValue: JSONObject? = null): JSONObject? =
            cacheMemory.get<JSONObject>(key) ?: cacheDisk.getJSONObject(key, defaultValue)

        @JvmOverloads
        fun put(key: String, value: JSONArray, saveTime: Int = -1) {
            cacheMemory.put(key, value, saveTime)
            cacheDisk.put(key, value, saveTime)
        }

        @JvmOverloads
        fun getJSONArray(key: String, defaultValue: JSONArray? = null): JSONArray? =
            cacheMemory.get<JSONArray>(key) ?: cacheDisk.getJSONArray(key, defaultValue)

        @JvmOverloads
        fun put(key: String, value: Bitmap, saveTime: Int = -1) {
            cacheMemory.put(key, value, saveTime)
            cacheDisk.put(key, value, saveTime)
        }

        @JvmOverloads
        fun getBitmap(key: String, defaultValue: Bitmap? = null): Bitmap? =
            cacheMemory.get<Bitmap>(key) ?: cacheDisk.getBitmap(key, defaultValue)

        @JvmOverloads
        fun put(key: String, value: Drawable, saveTime: Int = -1) {
            cacheMemory.put(key, value, saveTime)
            cacheDisk.put(key, value, saveTime)
        }

        @JvmOverloads
        fun getDrawable(key: String, defaultValue: Drawable? = null): Drawable? =
            cacheMemory.get<Drawable>(key) ?: cacheDisk.getDrawable(key, defaultValue)

        @JvmOverloads
        fun put(key: String, value: Serializable, saveTime: Int = -1) {
            cacheMemory.put(key, value, saveTime)
            cacheDisk.put(key, value, saveTime)
        }

        @JvmOverloads
        fun getSerializable(key: String, defaultValue: Any? = null): Any? =
            cacheMemory.get<Any>(key) ?: cacheDisk.getSerializable(key, defaultValue)

        @JvmOverloads
        fun put(key: String, value: Parcelable, saveTime: Int = -1) {
            cacheMemory.put(key, value, saveTime)
            cacheDisk.put(key, value, saveTime)
        }

        @JvmOverloads
        fun <T> getParcelable(
            key: String, creator: Parcelable.Creator<T>, defaultValue: T? = null
        ): T? = cacheMemory.get<T>(key) ?: cacheDisk.getParcelable(key, creator, defaultValue)

        fun remove(key: String) {
            cacheMemory.remove(key)
            cacheDisk.remove(key)
        }

        fun clear() {
            cacheMemory.clear()
            cacheDisk.clear()
        }

        companion object {
            const val SEC = 1
            const val MIN = 60
            const val HOUR = 3600
            const val DAY = 86400
            val instance: CacheDouble
                get() = getInstance(
                    CacheMemoryKit.CacheMemory.instance, CacheDiskKit.CacheDisk.instance
                )
            private val CACHE_MAP = HashMap<String, CacheDouble>()
            fun getInstance(
                cacheMemory: CacheMemoryKit.CacheMemory, cacheDisk: CacheDiskKit.CacheDisk
            ): CacheDouble = "${cacheDisk}_$cacheMemory".let { cacheKey ->
                CACHE_MAP[cacheKey] ?: synchronized(CacheDouble::class.java) {
                    CACHE_MAP[cacheKey] ?: CacheDouble(cacheMemory, cacheDisk)
                        .apply { CACHE_MAP[cacheKey] = this }
                }
            }
        }
    }

    private var defaultCacheDouble: CacheDouble? = null
        get() = field ?: CacheDouble.instance
    val cacheMemoryCount: Int
        get() = getCacheMemoryCount(defaultCacheDouble)

    fun getCacheMemoryCount(cacheDouble: CacheDouble?): Int = cacheDouble?.cacheMemoryCount ?: 0
    val cacheDiskCount: Int
        get() = getCacheDiskCount(defaultCacheDouble)

    fun getCacheDiskCount(cacheDouble: CacheDouble?): Int = cacheDouble?.cacheDiskCount ?: 0
    val cacheDiskSize: Long
        get() = getCacheDiskSize(defaultCacheDouble)

    fun getCacheDiskSize(cacheDouble: CacheDouble?): Long = cacheDouble?.cacheDiskSize ?: 0

    @JvmOverloads
    fun put(
        key: String, value: ByteArray?, saveTime: Int = -1,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ) = cacheDouble?.put(key, value, saveTime)

    @JvmOverloads
    fun getBytes(
        key: String, defaultValue: ByteArray? = null, cacheDouble: CacheDouble? = defaultCacheDouble
    ): ByteArray? = cacheDouble?.getBytes(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: String, saveTime: Int = -1,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ) = cacheDouble?.put(key, value, saveTime)

    @JvmOverloads
    fun getString(
        key: String, defaultValue: String? = null, cacheDouble: CacheDouble? = defaultCacheDouble
    ): String? = cacheDouble?.getString(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: JSONObject, saveTime: Int = -1,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ) = cacheDouble?.put(key, value, saveTime)

    @JvmOverloads
    fun getJSONObject(
        key: String, defaultValue: JSONObject? = null,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ): JSONObject? = cacheDouble?.getJSONObject(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: JSONArray, saveTime: Int = -1,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ) = cacheDouble?.put(key, value, saveTime)

    @JvmOverloads
    fun getJSONArray(
        key: String, defaultValue: JSONArray? = null, cacheDouble: CacheDouble? = defaultCacheDouble
    ): JSONArray? = cacheDouble?.getJSONArray(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: Bitmap, saveTime: Int = -1,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ) = cacheDouble?.put(key, value, saveTime)

    @JvmOverloads
    fun getBitmap(
        key: String, defaultValue: Bitmap? = null, cacheDouble: CacheDouble? = defaultCacheDouble
    ): Bitmap? =
        cacheDouble?.getBitmap(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: Drawable, saveTime: Int = -1,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ) = cacheDouble?.put(key, value, saveTime)

    @JvmOverloads
    fun getDrawable(
        key: String, defaultValue: Drawable? = null, cacheDouble: CacheDouble? = defaultCacheDouble
    ): Drawable? = cacheDouble?.getDrawable(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: Serializable, saveTime: Int = -1,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ) = cacheDouble?.put(key, value, saveTime)

    @JvmOverloads
    fun getSerializable(
        key: String, defaultValue: Any? = null, cacheDouble: CacheDouble? = defaultCacheDouble
    ): Any? = cacheDouble?.getSerializable(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: Parcelable, saveTime: Int = -1,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ) = cacheDouble?.put(key, value, saveTime)

    @JvmOverloads
    fun <T> getParcelable(
        key: String, creator: Parcelable.Creator<T>, defaultValue: T? = null,
        cacheDouble: CacheDouble? = defaultCacheDouble
    ): T? = cacheDouble?.getParcelable(key, creator, defaultValue)

    @JvmOverloads
    fun remove(key: String, cacheDouble: CacheDouble? = defaultCacheDouble) =
        cacheDouble?.remove(key)

    @JvmOverloads
    fun clear(cacheDouble: CacheDouble? = defaultCacheDouble) = cacheDouble?.clear()
}