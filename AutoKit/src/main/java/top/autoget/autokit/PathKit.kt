package top.autoget.autokit

import android.os.Environment
import top.autoget.autokit.AKit.app
import top.autoget.autokit.SdKit.isSdCardDisable
import top.autoget.autokit.VersionKit.aboveLollipop
import top.autoget.autokit.VersionKit.aboveNougat
import java.io.File

object PathKit {
    val pathRoot: String
        get() = getAbsolutePath(Environment.getRootDirectory())//“/system”

    private fun getAbsolutePath(file: File?): String =
        file?.run { "$absolutePath${File.separator}" } ?: ""

    val pathData: String
        get() = getAbsolutePath(Environment.getDataDirectory())//“/data”
    val pathDownloadCache: String
        get() = getAbsolutePath(Environment.getDownloadCacheDirectory())//“/cache”
    val pathInternalAppData: String
        get() = when {
            aboveNougat -> getAbsolutePath(app.dataDir)//app.filesDir.parent
            else -> "${app.applicationInfo.dataDir}${File.separator}"
        }//“/data/data/package”
    val pathInternalAppCache: String
        get() = getAbsolutePath(app.cacheDir)//“/data/data/package/cache”
    val pathInternalAppCodeCache: String
        get() = when {
            aboveLollipop -> getAbsolutePath(app.codeCacheDir)
            else -> "${app.applicationInfo.dataDir}${File.separator}code_cache"
        }//“/data/data/package/code_cache”
    val pathInternalAppFiles: String
        get() = getAbsolutePath(app.filesDir)//“/data/data/package/files”
    val pathInternalAppNoBackupFiles: String
        get() = when {
            aboveLollipop -> getAbsolutePath(app.noBackupFilesDir)
            else -> "${app.applicationInfo.dataDir}${File.separator}no_backup"
        }//“/data/data/package/no_backup”
    val pathInternalAppSp: String
        get() = "${pathInternalAppData}shared_prefs"//“/data/data/package/shared_prefs”
    val pathInternalAppDbs: String
        get() = "${pathInternalAppData}databases"//“/data/data/package/databases”

    fun getPathInternalAppDb(name: String): String =
        app.getDatabasePath(name).absolutePath//“/data/data/package/databases/name”

    val pathExternal: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStorageDirectory())
        }//“/storage/emulated/0”
    val pathExternalMusic: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC))
        }//“/storage/emulated/0/Music”
    val pathExternalPodcasts: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PODCASTS))
        }//“/storage/emulated/0/Podcasts”
    val pathExternalRingtones: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES))
        }//“/storage/emulated/0/Ringtones”
    val pathExternalAlarms: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS))
        }//“/storage/emulated/0/Alarms”
    val pathExternalNotifications: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_NOTIFICATIONS))
        }//“/storage/emulated/0/Notifications”
    val pathExternalPictures: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
        }//“/storage/emulated/0/Pictures”
    val pathExternalMovies: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES))
        }//“/storage/emulated/0/Movies”
    val pathExternalDownload: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        }//“/storage/emulated/0/Download”
    val pathExternalDcim: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM))
        }//“/storage/emulated/0/DCIM”
    val pathExternalDocuments: String
        get() = when {
            isSdCardDisable -> ""
            else -> when {
                aboveLollipop ->
                    getAbsolutePath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS))
                else -> "${pathExternal}Documents"
            }
        }//“/storage/emulated/0/Documents”
    val pathExternalAppData: String
        get() = when {
            isSdCardDisable || app.externalCacheDir == null -> ""
            else -> getAbsolutePath(app.externalCacheDir?.parentFile)
        }//“/storage/emulated/0/Android/data/package”
    val pathExternalAppCache: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.externalCacheDir)
        }//“/storage/emulated/0/Android/data/package/cache”
    val pathExternalAppFiles: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(null))
        }//“/storage/emulated/0/Android/data/package/files”
    val pathExternalAppMusic: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_MUSIC))
        }//“/storage/emulated/0/Android/data/package/files/Music”
    val pathExternalAppPodcasts: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_PODCASTS))
        }//“/storage/emulated/0/Android/data/package/files/Podcasts”
    val pathExternalAppRingtones: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_RINGTONES))
        }//“/storage/emulated/0/Android/data/package/files/Ringtones”
    val pathExternalAppAlarms: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_ALARMS))
        }//“/storage/emulated/0/Android/data/package/files/Alarms”
    val pathExternalAppNotifications: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_NOTIFICATIONS))
        }//“/storage/emulated/0/Android/data/package/files/Notifications”
    val pathExternalAppPictures: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        }//“/storage/emulated/0/Android/data/package/files/Pictures”
    val pathExternalAppMovies: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_MOVIES))
        }//“/storage/emulated/0/Android/data/package/files/Movies”
    val pathExternalAppDownload: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))
        }//“/storage/emulated/0/Android/data/package/files/Download”
    val pathExternalAppDcim: String
        get() = when {
            isSdCardDisable -> ""
            else -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_DCIM))
        }//“/storage/emulated/0/Android/data/package/files/DCIM”
    val pathExternalAppDocuments: String
        get() = when {
            isSdCardDisable -> ""
            else -> when {
                aboveLollipop -> getAbsolutePath(app.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS))
                else -> "${getAbsolutePath(app.getExternalFilesDir(null))}Documents"
            }
        }//“/storage/emulated/0/Android/data/package/files/Documents”
    val pathExternalAppObb: String
        get() = if (isSdCardDisable) "" else getAbsolutePath(app.obbDir)//“/storage/emulated/0/Android/obb/package”
}