package top.autoget.autokit

import android.util.LruCache
import top.autoget.autokit.DateKit.nowMillis

object CacheMemoryKit {
    class CacheMemory private constructor
        (private val cacheKey: String, private val cacheMemory: LruCache<String, CacheValue>) {
        val cacheCount: Int
            get() = cacheMemory.size()

        @JvmOverloads
        fun put(key: String, value: Any?, saveTime: Int = -1): CacheValue? = value?.let {
            (if (saveTime < 0) -1 else nowMillis + saveTime * 1000).let { dueTime ->
                cacheMemory.put(key, CacheValue(dueTime, it))
            }
        }

        @JvmOverloads
        operator fun <T> get(key: String, defaultValue: T? = null): T? =
            cacheMemory.get(key)?.let { cacheValue ->
                when {
                    cacheValue.dueTime.toInt() == -1 ||
                            cacheValue.dueTime >= nowMillis -> cacheValue.value as T
                    else -> defaultValue.apply { cacheMemory.remove(key) }
                }
            } ?: defaultValue

        fun remove(key: String): Any? = cacheMemory.remove(key)?.value
        fun clear() = cacheMemory.evictAll()
        override fun toString(): String = "$cacheKey@${Integer.toHexString(hashCode())}"
        data class CacheValue internal constructor(
            internal var dueTime: Long,
            internal var value: Any
        )

        companion object {
            const val SEC = 1
            const val MIN = 60
            const val HOUR = 3600
            const val DAY = 86400
            private const val DEFAULT_MAX_COUNT = 256
            val instance: CacheMemory
                get() = getInstance(DEFAULT_MAX_COUNT)
            private val CACHE_MAP: MutableMap<String, CacheMemory> = mutableMapOf()

            @JvmOverloads
            fun getInstance(maxCount: Int, cacheKey: String = maxCount.toString()): CacheMemory =
                CACHE_MAP[cacheKey] ?: synchronized(CacheMemory::class.java) {
                    CACHE_MAP[cacheKey] ?: CacheMemory(cacheKey, LruCache(maxCount))
                        .apply { CACHE_MAP[cacheKey] = this }
                }
        }
    }

    private var defaultCacheMemory: CacheMemory? = null
        get() = field ?: CacheMemory.instance
    val cacheCount: Int
        get() = getCacheCount(defaultCacheMemory)

    fun getCacheCount(cacheMemory: CacheMemory?): Int = cacheMemory?.cacheCount ?: 0

    @JvmOverloads
    fun put(
        key: String, value: Any?, saveTime: Int = -1, cacheMemory: CacheMemory? = defaultCacheMemory
    ): CacheMemory.CacheValue? = cacheMemory?.put(key, value, saveTime)

    @JvmOverloads
    operator fun <T> get(
        key: String, defaultValue: T? = null, cacheMemory: CacheMemory? = defaultCacheMemory
    ): T? = cacheMemory?.get(key, defaultValue)

    @JvmOverloads
    fun remove(key: String, cacheMemory: CacheMemory? = defaultCacheMemory): Any? =
        cacheMemory?.remove(key)

    @JvmOverloads
    fun clear(cacheMemory: CacheMemory? = defaultCacheMemory) = cacheMemory?.clear()
}