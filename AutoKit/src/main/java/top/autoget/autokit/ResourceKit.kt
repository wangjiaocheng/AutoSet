package top.autoget.autokit

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.annotation.*
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ApplicationKit.appPackageName
import top.autoget.autokit.FileIoKit.writeFileFromIS
import top.autoget.autokit.FileKit.createDirNew
import top.autoget.autokit.FileKit.createDirNone
import top.autoget.autokit.FileKit.createFileNew
import top.autoget.autokit.FileKit.createFileNone
import top.autoget.autokit.StringKit.isSpace
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object ResourceKit {
    const val ID = "id"
    const val LAYOUT = "layout"
    const val MENU = "menu"
    const val DRAWABLE = "drawable"
    const val MIPMAP = "mipmap"
    const val ANIM = "anim"
    const val RAW = "raw"
    const val STYLE = "style"
    const val STYLEABLE = "styleable"
    const val DIMEN = "dimen"
    const val COLOR = "color"
    const val INTEGER = "integer"
    const val BOOL = "bool"
    const val STRING = "string"
    const val ATTR = "attr"
    fun getResIdByName(context: Context, name: String, defType: String): Int =
        context.resources.getIdentifier(name, defType, context.packageName)

    fun getColorById(@ColorRes colorId: Int): Int = Resources.getSystem().getColor(colorId)
    fun getDrawableById(@DrawableRes drawableId: Int): Drawable =
        Resources.getSystem().getDrawable(drawableId)

    fun getStringById(@StringRes strId: Int): String = try {
        Resources.getSystem().getString(strId)
    } catch (ignore: Resources.NotFoundException) {
        ""
    }

    fun getStringWithArgsById(@StringRes strId: Int, vararg formatArgs: Any): String = try {
        app.getString(strId, *formatArgs)
    } catch (ignore: Resources.NotFoundException) {
        ""
    }

    fun getStringArrayById(@ArrayRes arrayId: Int): Array<String> = try {
        Resources.getSystem().getStringArray(arrayId)
    } catch (ignore: Resources.NotFoundException) {
        arrayOf()
    }

    fun getResourceId(name: String, type: String): Int = try {
        Resources.getSystem().getIdentifier(name, type, appPackageName)
    } catch (e: Exception) {
        e.printStackTrace()
        0
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun unZipAssets(
        assetsFilePath: String, destFilePath: String, isReWrite: Boolean = true
    ): Boolean = when {
        isSpace(destFilePath) -> false
        else -> app.assets.open(assetsFilePath).use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                createDirNone(destFilePath)
                var zipEntry: ZipEntry? = zipInputStream.nextEntry
                while (zipEntry != null) {
                    when {
                        zipEntry.isDirectory -> File("$destFilePath${File.separator}${zipEntry.name}")
                            .run { if (isReWrite) createDirNew(this) else createDirNone(this) }
                        else -> File("$destFilePath${File.separator}${zipEntry.name}").run {
                            if (isReWrite) createFileNew(this) else createFileNone(this)
                            FileOutputStream(this).use { fileOutputStream ->
                                ByteArray(1024 * 1024).let { bytes ->
                                    while (true) {
                                        if (zipInputStream.read(bytes) != -1)
                                            fileOutputStream.write(bytes) else break
                                    }
                                }
                            }
                        }
                    }
                    zipEntry = zipInputStream.nextEntry
                }
                true
            }
        }
    }

    @JvmOverloads
    @Throws(IOException::class)
    fun unZipRaw(@RawRes resId: Int, destFilePath: String, isReWrite: Boolean = true): Boolean =
        when {
            isSpace(destFilePath) -> false
            else -> Resources.getSystem().openRawResource(resId).use { inputStream ->
                ZipInputStream(inputStream).use { zipInputStream ->
                    createDirNone(destFilePath)
                    var zipEntry: ZipEntry? = zipInputStream.nextEntry
                    while (zipEntry != null) {
                        when {
                            zipEntry.isDirectory -> File("$destFilePath${File.separator}${zipEntry.name}")
                                .run { if (isReWrite) createDirNew(this) else createDirNone(this) }
                            else -> File("$destFilePath${File.separator}${zipEntry.name}").run {
                                if (isReWrite) createFileNew(this) else createFileNone(this)
                                FileOutputStream(this).use { fileOutputStream ->
                                    ByteArray(1024 * 1024).let { bytes ->
                                        while (true) {
                                            if (zipInputStream.read(bytes) != -1)
                                                fileOutputStream.write(bytes) else break
                                        }
                                    }
                                }
                            }
                        }
                        zipEntry = zipInputStream.nextEntry
                    }
                    true
                }
            }
        }

    fun copyFileByAssets(assetsFilePath: String, destFilePath: String): Boolean = when {
        isSpace(assetsFilePath) -> false
        else -> try {
            var isSuccess = true
            app.assets.list(assetsFilePath)?.run {
                when {
                    isEmpty() ->
                        isSuccess = app.assets.open(assetsFilePath)
                            .use { writeFileFromIS(destFilePath, it) }
                    else -> for (asset in this) {
                        isSuccess = isSuccess and
                                copyFileByAssets("$assetsFilePath/$asset", "$destFilePath/$asset")
                    }
                }
            }
            isSuccess
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun copyFileByRaw(@RawRes resId: Int, destFilePath: String): Boolean =
        Resources.getSystem().openRawResource(resId).use { writeFileFromIS(destFilePath, it) }

    fun getBytesByAssets(assetsFilePath: String): ByteArray? = when {
        isSpace(assetsFilePath) -> null
        else -> try {
            app.assets.open(assetsFilePath)
                .use { ByteArray(it.available()).apply { it.read(this) } }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getBytesByRaw(resId: Int): ByteArray? = try {
        Resources.getSystem().openRawResource(resId)
            .use { ByteArray(it.available()).apply { it.read(this) } }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    @JvmOverloads
    fun getStringByAssets(assetsFilePath: String, charsetName: String = "UTF-8"): String? = when {
        isSpace(assetsFilePath) -> null
        else -> try {
            app.assets.open(assetsFilePath)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }?.use {
            ByteArray(it.available()).apply { it.read(this) }.let { bytes ->
                when {
                    isSpace(charsetName) -> String(bytes)
                    else -> try {
                        String(bytes, charset(charsetName))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                        ""
                    }
                }
            }
        }
    }

    @JvmOverloads
    fun getStringByRaw(@RawRes resId: Int, charsetName: String = "UTF-8"): String =
        Resources.getSystem().openRawResource(resId).use {
            ByteArray(it.available()).apply { it.read(this) }.let { bytes ->
                when {
                    isSpace(charsetName) -> String(bytes)
                    else -> try {
                        String(bytes, charset(charsetName))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                        ""
                    }
                }
            }
        }

    @JvmOverloads
    fun getListByAssets(assetsPath: String, charsetName: String = "UTF-8"): MutableList<String>? =
        when {
            isSpace(assetsPath) -> null
            else -> try {
                Resources.getSystem().assets.open(assetsPath).use { is2List(it, charsetName) }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

    @JvmOverloads
    fun getListByRaw(@RawRes resId: Int, charsetName: String = "UTF-8"): MutableList<String>? =
        Resources.getSystem().openRawResource(resId).use { is2List(it, charsetName) }

    private fun is2List(inputStream: InputStream, charsetName: String?): MutableList<String>? =
        try {
            mutableListOf<String>().apply {
                inputStream.use {
                    when {
                        isSpace(charsetName) -> InputStreamReader(it)
                        else -> charsetName?.let { charset -> InputStreamReader(it, charset) }
                    }?.use { inputStreamReader ->
                        BufferedReader(inputStreamReader).use { bufferedReader ->
                            while (true) {
                                bufferedReader.readLine()?.let { line -> add(line) } ?: break
                            }
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
}