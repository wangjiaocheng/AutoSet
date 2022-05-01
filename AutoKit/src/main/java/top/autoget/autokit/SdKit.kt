package top.autoget.autokit

import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import top.autoget.autokit.AKit.app
import top.autoget.autokit.FileKit.getAllSize
import top.autoget.autokit.FileKit.getAvailableSize
import top.autoget.autokit.FileKit.getFreeSize
import top.autoget.autokit.PathKit.pathData
import top.autoget.autokit.PathKit.pathExternal
import top.autoget.autokit.VersionKit.aboveJellyBeanMR2
import java.io.*
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException

object SdKit {
    val isSdCardEnable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED ||
                !Environment.isExternalStorageRemovable()
    val isSdCardDisable: Boolean
        get() = !isSdCardEnable
    val isSdCardAvailable: Boolean
        get() = if (isSdCardEnable) File(pathExternal).canWrite() else false
    val isSdCardUnavailable: Boolean
        get() = !isSdCardAvailable
    val sdCardPathEx: MutableList<String>
        get() = mutableListOf<String>().apply {
            try {
                Runtime.getRuntime().exec("mount").inputStream.use { inputStream ->
                    InputStreamReader(inputStream).use { inputStreamReader ->
                        BufferedReader(inputStreamReader).use { bufferedReader ->
                            while (true) {
                                bufferedReader.readLine()?.let { line ->
                                    if (!line.contains("secure") && !line.contains("asec"))
                                        when {
                                            line.contains("fat") ->
                                                line.split(" ".toRegex())
                                                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                                                    .let { columns ->
                                                        if (columns.size > 1) add("*${columns[1]}")
                                                    }
                                            line.contains("fuse") ->
                                                line.split(" ".toRegex())
                                                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                                                    .let { columns ->
                                                        if (columns.size > 1) add(columns[1])
                                                    }
                                        }
                                }
                            }
                        }
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    val sdCardPath: String
        get() = try {
            Runtime.getRuntime().exec("cat /proc/mounts").let { process ->
                process.inputStream.use { inputStream ->
                    BufferedInputStream(inputStream).use { bufferedInputStream ->
                        InputStreamReader(bufferedInputStream).use { inputStreamReader ->
                            BufferedReader(inputStreamReader).use { bufferedReader ->
                                while (true) {
                                    bufferedReader.readLine()?.let { line ->
                                        if (line.contains("sdcard") && line.contains(".android_secure"))
                                            line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                                                .toTypedArray().let { strings ->
                                                    if (strings.size >= 5) return strings[1]
                                                        .replace("/.android_secure", "")
                                                }
                                        if (process.waitFor() != 0 && process.exitValue() == 1) {
                                        }//0正常结束；1非正常结束
                                    } ?: break
                                }
                                pathExternal
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

    fun getAllBytes(path: String?): Long =
        path?.run { if (isSdCardEnable && startsWith(sdCardPath)) sdCardPath else pathData }
            ?.let { getAllSize(it) } ?: 0L

    fun getAvailableBytes(path: String?): Long =
        path?.run { if (isSdCardEnable && startsWith(sdCardPath)) sdCardPath else pathData }
            ?.let { getAvailableSize(it) } ?: 0L

    fun getFreeBytes(path: String?): Long =
        path?.run { if (isSdCardEnable && startsWith(sdCardPath)) sdCardPath else pathData }
            ?.let { getFreeSize(it) } ?: 0L

    fun getStatFs(path: String): StatFs = StatFs(path)
    data class SdCardInfo(
        var isExist: Boolean = false, var blockByteSize: Long = 0,
        var totalBlocks: Long = 0, var freeBlocks: Long = 0, var availableBlocks: Long = 0,
        var totalBytes: Long = 0, var freeBytes: Long = 0, var availableBytes: Long = 0
    )

    val sdCardInfo: SdCardInfo
        get() = SdCardInfo().apply {
            if (isSdCardEnable) {
                isExist = true
                if (aboveJellyBeanMR2)
                    StatFs(pathExternal).let {
                        blockByteSize = it.blockSizeLong
                        totalBlocks = it.blockCountLong
                        availableBlocks = it.availableBlocksLong
                        freeBlocks = it.freeBlocksLong
                        totalBytes = it.totalBytes
                        availableBytes = it.availableBytes
                        freeBytes = it.freeBytes
                    }
            }
        }

    data class SdCardInfoItem(
        val path: String, private val state: String, private val isRemovable: Boolean
    )

    val sdCardInfoList: MutableList<SdCardInfoItem>
        get() = mutableListOf<SdCardInfoItem>().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> try {
                    for (storageVolume in app.storageManager.storageVolumes) {
                        add(
                            SdCardInfoItem(
                                (StorageVolume::class.java.getMethod("getPath")
                                    .invoke(storageVolume) as String),
                                storageVolume.state, storageVolume.isRemovable
                            )
                        )
                    }
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
                else -> try {
                    StorageManager::class.java.getMethod("getVolumeList")
                        .invoke(app.storageManager)?.let { result ->
                            Class.forName("android.os.storage.StorageVolume").let { clazz ->
                                for (i in 0 until Array.getLength(result)) {
                                    Array.get(result, i).let { element ->
                                        (clazz.getMethod("getPath")
                                            .invoke(element) as String).let { path ->
                                            add(
                                                SdCardInfoItem(
                                                    path, StorageManager::class.java.getMethod(
                                                        "getVolumeState", String::class.java
                                                    ).invoke(
                                                        app.storageManager, path
                                                    ) as String,
                                                    clazz.getMethod("isRemovable")
                                                        .invoke(element) as Boolean
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            }
        }
}