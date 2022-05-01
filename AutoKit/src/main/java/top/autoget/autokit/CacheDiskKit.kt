package top.autoget.autokit

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ConvertKit.bitmap2Bytes
import top.autoget.autokit.ConvertKit.bytes2Bitmap
import top.autoget.autokit.ConvertKit.bytes2Drawable
import top.autoget.autokit.ConvertKit.drawable2Bytes
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.FileIoKit.writeFileFromBytesByChannel
import top.autoget.autokit.FileKit.createDirNone
import top.autoget.autokit.StringKit.isSpace
import java.io.*
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

object CacheDiskKit {
    class CacheDisk private constructor(
        private val cacheKey: String, private val cacheDir: File,
        private val maxCount: Int, private val maxSize: Long
    ) : LoggerKit {
        private val diskCacheManager: DiskCacheManager?
            get() = when {
                createDirNone(cacheDir) -> DiskCacheManager(cacheDir, maxCount, maxSize)
                else -> {
                    error("$loggerTag->can't make dirs in ${cacheDir.absolutePath}")
                    null
                }
            }
        val cacheCount: Int
            get() = diskCacheManager?.getCacheCount() ?: 0
        val cacheSize: Long
            get() = diskCacheManager?.getCacheSize() ?: 0

        @JvmOverloads
        fun put(key: String, value: ByteArray?, saveTime: Int = -1) = value?.let {
            (if (saveTime < 0) it else newBytesWithDueTime(saveTime, it)).let { bytes ->
                diskCacheManager?.run {
                    getFileBeforePut(key).let { file ->
                        writeFileFromBytesByChannel(file, bytes, true)
                        updateModify(file)
                        put(file)
                    }
                }
            }
        }

        private fun newBytesWithDueTime(second: Int, data: ByteArray): ByteArray =
            createDueTime(second).toByteArray() + data

        private fun createDueTime(seconds: Int): String = String.format(
            Locale.getDefault(), "_$%010d\$_", nowMillis / 1000 + seconds
        )//"_$%010d${'$'}_"

        @JvmOverloads
        fun getBytes(key: String, defaultValue: ByteArray? = null): ByteArray? =
            diskCacheManager?.run {
                getFileIfExists(key)?.let { file ->
                    file2Bytes(file)?.let { bytes ->
                        when {
                            isDue(bytes) -> defaultValue.apply { removeByKey(key) }
                            else -> getBytesWithoutDueTime(bytes).apply { updateModify(file) }
                        }
                    }
                } ?: defaultValue
            } ?: defaultValue

        private fun file2Bytes(file: File): ByteArray? = try {
            RandomAccessFile(file, "r").use { randomAccessFile ->
                randomAccessFile.channel.use {
                    ByteArray(it.size().toInt()).apply {
                        it.map(FileChannel.MapMode.READ_ONLY, 0, size.toLong()).load().get(this)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        private fun isDue(data: ByteArray): Boolean = getDueTime(data).toInt()
            .let { millis -> millis != -1 && nowMillis > millis }

        private fun getDueTime(data: ByteArray): Long = when {
            hasTimeInfo(data) -> try {
                String(data.copyOfRange(2, 12)).toLong() * 1000
            } catch (e: NumberFormatException) {
                -1L
            }
            else -> -1L
        }

        private fun hasTimeInfo(data: ByteArray?): Boolean = data?.run {
            size >= TIME_INFO_LEN && this[0] == '_'.toByte() && this[1] == '$'.toByte() && this[12] == '$'.toByte() && this[13] == '_'.toByte()
        } ?: false

        private fun getBytesWithoutDueTime(data: ByteArray): ByteArray =
            if (hasTimeInfo(data)) data.copyOfRange(TIME_INFO_LEN, data.size) else data

        @JvmOverloads
        fun put(key: String, value: String, saveTime: Int = -1) =
            put(key, string2Bytes(value), saveTime)

        private fun string2Bytes(string: String?): ByteArray? = string?.toByteArray()

        @JvmOverloads
        fun getString(key: String, defaultValue: String? = null): String? =
            getBytes(key)?.let { bytes2String(it) } ?: defaultValue

        private fun bytes2String(bytes: ByteArray?): String? = bytes?.let { String(it) }

        @JvmOverloads
        fun put(key: String, value: JSONObject, saveTime: Int = -1) =
            put(key, jsonObject2Bytes(value), saveTime)

        private fun jsonObject2Bytes(jsonObject: JSONObject?): ByteArray? =
            jsonObject?.toString()?.toByteArray()

        @JvmOverloads
        fun getJSONObject(key: String, defaultValue: JSONObject? = null): JSONObject? =
            getBytes(key)?.let { bytes2JSONObject(it) } ?: defaultValue

        private fun bytes2JSONObject(bytes: ByteArray?): JSONObject? = try {
            bytes?.let { JSONObject(String(it)) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        @JvmOverloads
        fun put(key: String, value: JSONArray, saveTime: Int = -1) =
            put(key, jsonArray2Bytes(value), saveTime)

        private fun jsonArray2Bytes(jsonArray: JSONArray?): ByteArray? =
            jsonArray?.toString()?.toByteArray()

        @JvmOverloads
        fun getJSONArray(key: String, defaultValue: JSONArray? = null): JSONArray? =
            getBytes(key)?.let { bytes2JSONArray(it) } ?: defaultValue

        private fun bytes2JSONArray(bytes: ByteArray?): JSONArray? = try {
            bytes?.let { JSONArray(String(it)) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        @JvmOverloads
        fun put(key: String, value: Bitmap, saveTime: Int = -1) =
            put(key, bitmap2Bytes(value, Bitmap.CompressFormat.PNG), saveTime)

        @JvmOverloads
        fun getBitmap(key: String, defaultValue: Bitmap? = null): Bitmap? =
            getBytes(key)?.let { bytes2Bitmap(it) } ?: defaultValue

        @JvmOverloads
        fun put(key: String, value: Drawable, saveTime: Int = -1) =
            put(key, drawable2Bytes(value, Bitmap.CompressFormat.PNG), saveTime)

        @JvmOverloads
        fun getDrawable(key: String, defaultValue: Drawable? = null): Drawable? =
            getBytes(key)?.let { bytes2Drawable(it) } ?: defaultValue

        @JvmOverloads
        fun put(key: String, value: Serializable, saveTime: Int = -1) =
            put(key, serializable2Bytes(value), saveTime)

        private fun serializable2Bytes(serializable: Serializable?): ByteArray? =
            serializable?.let {
                try {
                    ByteArrayOutputStream().use { byteArrayOutputStream ->
                        byteArrayOutputStream.apply {
                            ObjectOutputStream(this).use { objectOutputStream ->
                                objectOutputStream.writeObject(it)
                            }
                        }.toByteArray()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

        @JvmOverloads
        fun getSerializable(key: String, defaultValue: Any? = null): Any? =
            getBytes(key)?.let { bytes2Serializable(it) } ?: defaultValue

        private fun bytes2Serializable(bytes: ByteArray?): Any? = bytes?.let {
            try {
                ByteArrayInputStream(it).use { byteArrayInputStream ->
                    ObjectInputStream(byteArrayInputStream).use { objectInputStream ->
                        objectInputStream.readObject()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @JvmOverloads
        fun put(key: String, value: Parcelable, saveTime: Int = -1) =
            put(key, parcelable2Bytes(value), saveTime)

        private fun parcelable2Bytes(parcelable: Parcelable?): ByteArray? = parcelable?.let {
            Parcel.obtain().run {
                it.writeToParcel(this, 0)
                val bytes = marshall()
                recycle()
                bytes
            }
        }

        @JvmOverloads
        fun <T> getParcelable(
            key: String, creator: Parcelable.Creator<T>, defaultValue: T? = null
        ): T? = getBytes(key)?.let { bytes2Parcelable(it, creator) } ?: defaultValue

        private fun <T> bytes2Parcelable(bytes: ByteArray?, creator: Parcelable.Creator<T>): T? =
            bytes?.let {
                Parcel.obtain().run {
                    unmarshall(it, 0, it.size)
                    setDataPosition(0)
                    val result = creator.createFromParcel(this)
                    recycle()
                    result
                }
            }

        internal inner class XFileOutputStream @Throws(FileNotFoundException::class)
        constructor(var file: File) : FileOutputStream(file) {
            @Throws(IOException::class)
            override fun close() {
                super.close()
                diskCacheManager?.run {
                    updateModify(file)
                    put(file)
                }
            }
        }

        @Throws(FileNotFoundException::class)
        fun put(key: String): OutputStream? =
            diskCacheManager?.run { XFileOutputStream(newFile(key)) }

        @Throws(FileNotFoundException::class)
        operator fun get(key: String): InputStream? =
            diskCacheManager?.run { newFile(key).apply { updateModify(this) } }
                ?.run { if (exists()) FileInputStream(this) else null }

        fun file(key: String): File? =
            diskCacheManager?.newFile(key)?.run { if (exists()) this else null }

        fun remove(key: String): Boolean = diskCacheManager?.removeByKey(key) ?: true
        fun clear(): Boolean = diskCacheManager?.clear() ?: true
        override fun toString(): String = "$cacheKey@${Integer.toHexString(hashCode())}"
        private class DiskCacheManager constructor
            (private val cacheDir: File, private val countLimit: Int, private val sizeLimit: Long) {
            private val cacheCount = AtomicInteger()
            private val cacheSize = AtomicLong()
            private val lastUsageDates = Collections.synchronizedMap(mutableMapOf<File, Long>())
            private val thread: Thread = Thread {
                cacheDir.listFiles { _, name -> name.startsWith(CACHE_PREFIX) }
                    ?.let { cachedFiles ->
                        var count = 0
                        var size = 0L
                        for (cachedFile in cachedFiles) {
                            count += 1
                            size += cachedFile.length()
                            lastUsageDates[cachedFile] = cachedFile.lastModified()
                        }
                        cacheCount.getAndAdd(count)
                        cacheSize.getAndAdd(size)
                    }
            }

            init {
                thread.start()
            }

            fun getCacheCount(): Int = cacheCount.get().apply {
                try {
                    thread.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            fun getCacheSize(): Long = cacheSize.get().apply {
                try {
                    thread.join()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

            fun newFile(key: String): File = File(cacheDir, "$CACHE_PREFIX${key.hashCode()}")
            fun getFileBeforePut(key: String): File = newFile(key).apply {
                if (exists()) {
                    cacheCount.addAndGet(-1)
                    cacheSize.addAndGet(-length())
                }
            }

            fun updateModify(file: File) = nowMillis.apply {
                file.setLastModified(this)
                lastUsageDates[file] = this
            }

            fun put(file: File) {
                cacheCount.addAndGet(1)
                cacheSize.addAndGet(file.length())
                while (cacheCount.get() > countLimit || cacheSize.get() > sizeLimit) {
                    cacheCount.addAndGet(-1)
                    cacheSize.addAndGet(-removeOldest())
                }
            }

            private fun removeOldest(): Long = when {
                lastUsageDates.isEmpty() -> 0L
                else -> {
                    var oldestUsage: Long = Long.MAX_VALUE
                    var oldestFile: File? = null
                    synchronized(lastUsageDates) {
                        for ((key, lastValueUsage) in lastUsageDates) {
                            if (lastValueUsage < oldestUsage) {
                                oldestUsage = lastValueUsage
                                oldestFile = key
                            }
                        }
                    }
                    oldestFile?.run {
                        if (delete()) length().apply {
                            lastUsageDates.remove(
                                oldestFile
                            )
                        } else 0L
                    }
                        ?: 0L
                }
            }

            fun getFileIfExists(key: String): File? =
                newFile(key).run { if (exists()) this else null }

            fun removeByKey(key: String): Boolean = getFileIfExists(key)?.let {
                when {
                    it.delete() -> true.apply {
                        cacheCount.addAndGet(-1)
                        cacheSize.addAndGet(-it.length())
                        lastUsageDates.remove(it)
                    }
                    else -> false
                }
            } ?: true

            fun clear(): Boolean =
                cacheDir.listFiles { _, name -> name.startsWith(CACHE_PREFIX) }?.run {
                    when {
                        isEmpty() -> true
                        else -> {
                            var flag = true
                            for (cachedFile in this) {
                                if (!cachedFile.delete()) {
                                    flag = false
                                    continue
                                }
                                cacheCount.addAndGet(-1)
                                cacheSize.addAndGet(-cachedFile.length())
                                lastUsageDates.remove(cachedFile)
                            }
                            if (flag) {
                                cacheCount.set(0)
                                cacheSize.set(0)
                                lastUsageDates.clear()
                            }
                            flag
                        }
                    }
                } ?: true
        }

        companion object {
            const val SEC = 1
            const val MIN = 60
            const val HOUR = 3600
            const val DAY = 86400
            private const val TIME_INFO_LEN = 14
            private const val CACHE_PREFIX = "cdh"
            private const val DEFAULT_MAX_COUNT = Int.MAX_VALUE
            private const val DEFAULT_MAX_SIZE = Long.MAX_VALUE
            val instance: CacheDisk
                get() = getInstance("")

            @JvmOverloads
            fun getInstance(
                cacheName: String,
                maxCount: Int = DEFAULT_MAX_COUNT, maxSize: Long = DEFAULT_MAX_SIZE
            ): CacheDisk = when {
                isSpace(cacheName) -> "cacheHelper"
                else -> cacheName
            }.let { getInstance(File(app.cacheDir, it), maxCount, maxSize) }

            private val CACHE_MAP: MutableMap<String, CacheDisk> = mutableMapOf()

            @JvmOverloads
            fun getInstance(
                cacheDir: File, maxCount: Int = DEFAULT_MAX_COUNT, maxSize: Long = DEFAULT_MAX_SIZE
            ): CacheDisk = "${cacheDir.absoluteFile}_${maxCount}_$maxSize".let { cacheKey ->
                CACHE_MAP[cacheKey] ?: synchronized(CacheDisk::class.java) {
                    CACHE_MAP[cacheKey] ?: CacheDisk(cacheKey, cacheDir, maxCount, maxSize)
                        .apply { CACHE_MAP[cacheKey] = this }
                }
            }
        }
    }

    private var defaultCacheDisk: CacheDisk? = null
        get() = field ?: CacheDisk.instance
    val cacheCount: Int
        get() = getCacheCount(defaultCacheDisk)

    fun getCacheCount(cacheDisk: CacheDisk?): Int = cacheDisk?.cacheCount ?: 0
    val cacheSize: Long
        get() = getCacheSize(defaultCacheDisk)

    fun getCacheSize(cacheDisk: CacheDisk?): Long = cacheDisk?.cacheSize ?: 0

    @JvmOverloads
    fun put(
        key: String, value: ByteArray?, saveTime: Int = -1, cacheDisk: CacheDisk? = defaultCacheDisk
    ) = cacheDisk?.put(key, value, saveTime)

    @JvmOverloads
    fun getBytes(
        key: String, defaultValue: ByteArray? = null, cacheDisk: CacheDisk? = defaultCacheDisk
    ): ByteArray? = cacheDisk?.getBytes(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: String, saveTime: Int = -1, cacheDisk: CacheDisk? = defaultCacheDisk
    ) = cacheDisk?.put(key, value, saveTime)

    @JvmOverloads
    fun getString(
        key: String, defaultValue: String? = null, cacheDisk: CacheDisk? = defaultCacheDisk
    ): String? = cacheDisk?.getString(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: JSONObject, saveTime: Int = -1, cacheDisk: CacheDisk? = defaultCacheDisk
    ) = cacheDisk?.put(key, value, saveTime)

    @JvmOverloads
    fun getJSONObject(
        key: String, defaultValue: JSONObject? = null, cacheDisk: CacheDisk? = defaultCacheDisk
    ): JSONObject? = cacheDisk?.getJSONObject(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: JSONArray, saveTime: Int = -1, cacheDisk: CacheDisk? = defaultCacheDisk
    ) = cacheDisk?.put(key, value, saveTime)

    @JvmOverloads
    fun getJSONArray(
        key: String, defaultValue: JSONArray? = null, cacheDisk: CacheDisk? = defaultCacheDisk
    ): JSONArray? = cacheDisk?.getJSONArray(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: Bitmap, saveTime: Int = -1, cacheDisk: CacheDisk? = defaultCacheDisk
    ) = cacheDisk?.put(key, value, saveTime)

    @JvmOverloads
    fun getBitmap(
        key: String, defaultValue: Bitmap? = null, cacheDisk: CacheDisk? = defaultCacheDisk
    ): Bitmap? = cacheDisk?.getBitmap(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: Drawable, saveTime: Int = -1, cacheDisk: CacheDisk? = defaultCacheDisk
    ) = cacheDisk?.put(key, value, saveTime)

    @JvmOverloads
    fun getDrawable(
        key: String, defaultValue: Drawable? = null, cacheDisk: CacheDisk? = defaultCacheDisk
    ): Drawable? = cacheDisk?.getDrawable(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: Serializable, saveTime: Int = -1,
        cacheDisk: CacheDisk? = defaultCacheDisk
    ) = cacheDisk?.put(key, value, saveTime)

    @JvmOverloads
    fun getSerializable(
        key: String, defaultValue: Any? = null, cacheDisk: CacheDisk? = defaultCacheDisk
    ): Any? = cacheDisk?.getSerializable(key, defaultValue)

    @JvmOverloads
    fun put(
        key: String, value: Parcelable, saveTime: Int = -1, cacheDisk: CacheDisk? = defaultCacheDisk
    ) = cacheDisk?.put(key, value, saveTime)

    @JvmOverloads
    fun <T> getParcelable(
        key: String, creator: Parcelable.Creator<T>, defaultValue: T? = null,
        cacheDisk: CacheDisk? = defaultCacheDisk
    ): T? = cacheDisk?.getParcelable(key, creator, defaultValue)

    @JvmOverloads
    fun remove(key: String, cacheDisk: CacheDisk? = defaultCacheDisk): Boolean =
        cacheDisk?.remove(key) ?: true

    @JvmOverloads
    fun clear(cacheDisk: CacheDisk? = defaultCacheDisk): Boolean = cacheDisk?.clear() ?: true
}