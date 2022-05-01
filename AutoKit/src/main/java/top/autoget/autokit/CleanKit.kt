package top.autoget.autokit

import top.autoget.autokit.AKit.app
import top.autoget.autokit.ConvertKit.byteSize2MemorySizeDesc
import top.autoget.autokit.FileKit.deleteFilesByFilter
import top.autoget.autokit.FileKit.getDirsSize
import top.autoget.autokit.PathKit.pathExternalAppCache
import top.autoget.autokit.PathKit.pathInternalAppCache
import top.autoget.autokit.PathKit.pathInternalAppDbs
import top.autoget.autokit.PathKit.pathInternalAppFiles
import top.autoget.autokit.PathKit.pathInternalAppSp
import top.autoget.autokit.SdKit.isSdCardEnable
import java.io.File

object CleanKit {
    fun cleanAppData(vararg dirPaths: String): Boolean = mutableListOf<File>().apply {
        for (dirPath in dirPaths) {
            add(File(dirPath))
        }
    }.toTypedArray().let { cleanAppData(*it) }

    fun cleanAppData(vararg dirs: File): Boolean {
        var isSuccess = cleanInternalFiles()/*“/data/data/package/files”*/ and
                cleanInternalCache()/*“/data/data/package/cache”*/ and
                cleanExternalCache()/*“/storage/emulated/0/android/data/package/cache”*/ and
                cleanInternalSP()/*“/data/data/package/shared_prefs”*/ and
                cleanInternalDbs()/*“/data/data/package/databases”；“/data/data/package/databases/dbName”*/
        for (dir in dirs) {
            isSuccess = isSuccess and cleanCustomCache(dir)
        }
        return isSuccess
    }

    fun cleanInternalFiles(): Boolean = deleteFilesByFilter(pathInternalAppFiles)
    fun cleanInternalCache(): Boolean = deleteFilesByFilter(pathInternalAppCache)
    fun cleanExternalCache(): Boolean =
        isSdCardEnable && deleteFilesByFilter(pathExternalAppCache)

    fun cleanInternalSP(): Boolean = deleteFilesByFilter(pathInternalAppSp)
    fun cleanInternalDbs(): Boolean = deleteFilesByFilter(pathInternalAppDbs)
    fun cleanInternalDbByName(dbName: String): Boolean = app.deleteDatabase(dbName)
    fun cleanCustomCache(dirPath: String): Boolean = deleteFilesByFilter(dirPath)
    fun cleanCustomCache(dir: File): Boolean = deleteFilesByFilter(dir)
    val totalCacheSize: String
        @Throws(Exception::class)
        get() = when {
            isSdCardEnable -> getDirsSize(app.cacheDir, app.externalCacheDir)
            else -> getDirsSize(app.cacheDir)
        }.toDouble().let { byteSize2MemorySizeDesc(it) }
}