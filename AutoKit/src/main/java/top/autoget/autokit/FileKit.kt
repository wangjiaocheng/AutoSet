package top.autoget.autokit

import android.app.DownloadManager
import android.app.Service
import android.content.*
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.FileProvider
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ApplicationKit.appPackageName
import top.autoget.autokit.ApplicationKit.installApp
import top.autoget.autokit.ConvertKit.byteSize2MemorySizeFit
import top.autoget.autokit.EncodeKit.base64Decode
import top.autoget.autokit.EncodeKit.base64Encode2String
import top.autoget.autokit.FileIoKit.writeFileFromString
import top.autoget.autokit.PathKit.pathData
import top.autoget.autokit.PathKit.pathExternal
import top.autoget.autokit.PathKit.pathExternalAppCache
import top.autoget.autokit.PathKit.pathExternalAppData
import top.autoget.autokit.PathKit.pathExternalAppFiles
import top.autoget.autokit.PathKit.pathInternalAppCache
import top.autoget.autokit.PathKit.pathInternalAppData
import top.autoget.autokit.PathKit.pathInternalAppFiles
import top.autoget.autokit.SdKit.isSdCardEnable
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.VersionKit.aboveJellyBeanMR2
import top.autoget.autokit.VersionKit.aboveKitKat
import java.io.*
import java.net.URL
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.net.ssl.HttpsURLConnection

object FileKit : LoggerKit {
    fun zip(inputStream: InputStream, outputStream: OutputStream) = try {
        outputStream.use { output ->
            GZIPOutputStream(output).use { gzipOutputStream ->
                inputStream.use { input ->
                    ByteArray(1024).let { bytes ->
                        while (true) {
                            if (input.read(bytes) != -1) gzipOutputStream.write(bytes) else break
                            gzipOutputStream.flush()
                        }
                    }
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    fun unzip(inputStream: InputStream, outputStream: OutputStream) = try {
        inputStream.use { input ->
            GZIPInputStream(input).use { gzipInputStream ->
                outputStream.use { output ->
                    ByteArray(1024).let { bytes ->
                        while (true) {
                            if (gzipInputStream.read(bytes) != -1) output.write(bytes) else break
                            output.flush()
                        }
                    }
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    fun bitmap2JpegFile(bitmap: Bitmap?, filePath: String?): Boolean = try {
        getFileByPath(filePath).apply { createFileNone(this) }?.run { FileOutputStream(this) }
            ?.use { bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it).apply { it.flush() } }
            ?: false
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        false
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    fun bitmap2PngFile(bitmap: Bitmap?, filePath: String?): Boolean = try {
        getFileByPath(filePath).apply { createFileNone(this) }?.run { FileOutputStream(this) }
            ?.use { bitmap?.compress(Bitmap.CompressFormat.PNG, 100, it).apply { it.flush() } }
            ?: false
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        false
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    fun encodeFile2Base64String(filePath: String?): String? = try {
        getFileByPath(filePath)?.run { FileInputStream(this) }
            ?.use { base64Encode2String(ByteArray(it.available()).apply { it.read(this) }) }
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    fun decoderBase64String2File(base64Code: String, filePath: String?): File? = try {
        getFileByPath(filePath)
            ?.apply { FileOutputStream(this).use { it.write(base64Decode(base64Code)) } }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    val pathCacheImageAppIcon = "${pathExternalAppCache}app/icon/images/"//菜单按钮
    val pathCacheImageMainGallery = "${pathExternalAppCache}gallery/images/"//主页
    val pathCacheImageBrowse = "${pathExternalAppCache}browse/images/"//浏览
    val pathCacheImageChooseHead = "${pathExternalAppCache}head/images/"//用户
    val pathCacheImage = "${pathExternalAppCache}others/images/"
    fun getCacheSdCard(uriStr: String?): File? = try {
        when {
            isSdCardEnable -> getFileByPath(uriStr)?.apply { createDirNone(parent) }
                .apply { info("$loggerTag->获取SdCard缓存文件成功！$uriStr") }
            else -> null
        }
    } catch (e: IOException) {
        e.printStackTrace()
        error("$loggerTag->获取SdCard缓存文件失败！")
        null
    }

    fun getFileByPath(filePath: String?): File? =
        filePath?.let { if (isSpace(filePath)) null else File(filePath) }

    @JvmOverloads
    fun isExistsTimestamp(
        filePath: String?, timestamp: String? = null, expire: Long = 60_000_000L
    ): Boolean = isExistsTimestamp(getFileByPath(filePath), timestamp, expire)

    @JvmOverloads
    fun isExistsTimestamp(
        file: File?, timestamp: String? = null, expire: Long = 60_000_000L
    ): Boolean = file?.run {
        timestamp?.let { exists() && Date(lastModified()).time - expire >= it.toLong() * 1000 }
            ?: exists()
    } ?: false

    val isExistDirSdCard: Boolean
        get() = isExistsDir(getFileByPath(pathExternal))

    fun isExistsDir(dirPath: String?): Boolean = isExistsDir(getFileByPath(dirPath))
    fun isExistsDir(file: File?): Boolean = file?.run { exists() && isDirectory } ?: false
    fun isExistFileSdCard(fileName: String?): Boolean =
        isExistsFile(getFileByPath("$pathExternal$fileName"))

    fun isExistsFile(filePath: String?): Boolean = isExistsFile(getFileByPath(filePath))
    fun isExistsFile(file: File?): Boolean = file?.run { exists() && isFile } ?: false
    val pathRootData: String
        get() = if (isSdCardEnable) pathExternal else pathData

    fun createRootData(dirPath: String?): File? = "${pathRootData}$dirPath${File.separator}"
        .let { getFileByPath(it)?.apply { createDirNone(this) } }

    val pathAppData: String
        get() = if (isSdCardEnable) pathExternalAppData else pathInternalAppData

    fun createAppData(dirPath: String?): File? = "${pathAppData}$dirPath${File.separator}"
        .let { getFileByPath(it)?.apply { createDirNone(this) } }

    val pathAppFiles: String
        get() = if (isSdCardEnable) pathExternalAppFiles else pathInternalAppFiles

    fun createAppFiles(dirPath: String?): File? = "${pathAppFiles}$dirPath${File.separator}"
        .let { getFileByPath(it)?.apply { createDirNone(this) } }

    val pathAppCache: String
        get() = if (isSdCardEnable) pathExternalAppCache else pathInternalAppCache

    fun createAppCache(dirPath: String?): File? = "${pathAppCache}$dirPath${File.separator}"
        .let { getFileByPath(it)?.apply { createDirNone(this) } }

    fun createDirSdCard(dirPath: String?): File? = try {
        getFileByPath("$pathExternal$dirPath").apply { createDirNone(this) }
    } catch (e: IOException) {
        e.printStackTrace()
        error("$loggerTag->创建目录失败！")
        null
    }

    fun createDirNone(dirPath: String?): Boolean = createDirNone(getFileByPath(dirPath))
    fun createDirNone(file: File?): Boolean =
        file?.run { if (exists()) isDirectory else mkdirs() } ?: false

    fun createDirNew(dirPath: String?): Boolean = createDirNew(getFileByPath(dirPath))
    fun createDirNew(file: File?): Boolean = file?.run {
        if (exists() && isDirectory) mkdirs().apply { deleteDir(file) } else mkdirs()
    } ?: false

    fun createFileSdCard(fileName: String?): File? = try {
        getFileByPath("$pathExternal$fileName").apply { createFileNone(this) }
    } catch (e: IOException) {
        e.printStackTrace()
        error("$loggerTag->创建文件失败！")
        null
    }

    fun createFileNone(filePath: String?): Boolean = createFileNone(getFileByPath(filePath))
    fun createFileNone(file: File?): Boolean = file?.run {
        try {
            exists() && isFile ||
                    ((if (parentFile?.exists() == true) true else createDirNone(parentFile)) && createNewFile())
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    } ?: false

    fun createFileNew(filePath: String?): Boolean = createFileNew(getFileByPath(filePath))
    fun createFileNew(file: File?): Boolean = file?.run {
        try {
            exists() && isFile && delete() && createDirNone(parentFile) && createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    } ?: false

    interface OnReplaceListener {
        fun onReplace(): Boolean
    }

    private val replaceListener: OnReplaceListener = object : OnReplaceListener {
        override fun onReplace(): Boolean {
            return true
        }
    }

    @JvmOverloads
    fun cutDir(
        srcDirPath: String?, destDirPath: String?, listener: OnReplaceListener? = replaceListener
    ): Boolean = cutDir(getFileByPath(srcDirPath), getFileByPath(destDirPath), listener)

    @JvmOverloads
    fun cutDir(
        srcDir: File?, destDir: File?, listener: OnReplaceListener? = replaceListener
    ): Boolean = copyOrCutDir(srcDir, destDir, listener, true)

    @JvmOverloads
    fun copyDir(
        srcDirPath: String?, destDirPath: String?, listener: OnReplaceListener? = replaceListener
    ): Boolean = copyDir(getFileByPath(srcDirPath), getFileByPath(destDirPath), listener)

    @JvmOverloads
    fun copyDir(
        srcDir: File?, destDir: File?, listener: OnReplaceListener? = replaceListener
    ): Boolean = copyOrCutDir(srcDir, destDir, listener, false)

    private fun copyOrCutDir(
        srcDir: File?, destDir: File?, listener: OnReplaceListener?, isCut: Boolean
    ): Boolean = when {
        srcDir == null || destDir == null ||
                "${destDir.path}${File.separator}".contains("${srcDir.path}${File.separator}") -> false
        !srcDir.exists() || srcDir.isFile -> false
        destDir.exists() && destDir.isDirectory -> !(listener?.onReplace()
            ?: true) || deleteFilesByFilter(destDir)
        !createDirNone(destDir) -> false
        else -> {
            srcDir.listFiles()?.run {
                if (isNotEmpty()) for (file in this) {
                    File("${destDir.path}${File.separator}${file.name}").let {
                        when {
                            file.isFile ->
                                if (!copyOrCutFile(file, it, listener, isCut)) return false
                            file.isDirectory ->
                                if (!copyOrCutDir(file, it, listener, isCut)) return false
                        }
                    }
                }
            }
            !isCut || deleteDir(srcDir)
        }
    }

    @JvmOverloads
    fun cutFile(
        srcFilePath: String?, destFilePath: String?, listener: OnReplaceListener? = replaceListener
    ): Boolean = cutFile(getFileByPath(srcFilePath), getFileByPath(destFilePath), listener)

    @JvmOverloads
    fun cutFile(
        srcFile: File?, destFile: File?, listener: OnReplaceListener? = replaceListener
    ): Boolean = copyOrCutFile(srcFile, destFile, listener, true)

    @JvmOverloads
    fun copyFile(
        srcFilePath: String?, destFilePath: String?, listener: OnReplaceListener? = replaceListener
    ): Boolean = copyFile(getFileByPath(srcFilePath), getFileByPath(destFilePath), listener)

    @JvmOverloads
    fun copyFile(
        srcFile: File?, destFile: File?, listener: OnReplaceListener? = replaceListener
    ): Boolean = copyOrCutFile(srcFile, destFile, listener, false)

    private fun copyOrCutFile(
        srcFile: File?, destFile: File?, listener: OnReplaceListener?, isCut: Boolean
    ): Boolean = when {
        srcFile == null || destFile == null || srcFile == destFile -> false
        !srcFile.exists() || srcFile.isDirectory -> false
        destFile.exists() && destFile.isFile -> !(listener?.onReplace()
            ?: true) || destFile.delete()
        !createDirNone(destFile.parentFile) -> false
        else -> try {
            writeDestFileFromSrcFile(srcFile, destFile) && (!isCut || deleteFile(srcFile))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    fun cut(src: File, dest: File, isCover: Boolean, isCut: Boolean): Boolean = try {
        when {
            src.isFile -> {
                dest.run { if (!isFile || isCover) createNewFile() }
                copy(src, dest).apply { if (isCut) src.delete() }
            }
            src.isDirectory -> {
                dest.mkdirs()
                var bool = true
                src.listFiles()?.run {
                    if (isNotEmpty()) for (file in this) {
                        file.absolutePath.run { substring(src.absolutePath.length, length) }.let {
                            bool =
                                bool && cut(file, File("${dest.absolutePath}$it"), isCover, isCut)
                        }
                    }
                }
                bool.apply { if (isCut) src.delete() }
            }
            else -> false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false.apply { error("$loggerTag->cut:文件剪切操作出现异常！") }
    }

    fun copy(src: File, dest: File): Boolean = when {
        src.isFile -> try {
            var isCover = false
            var count = 0
            dest.listFiles()?.run {
                if (isNotEmpty()) for (file in this) {
                    if (src.name == dest.name) {
                        isCover = true
                        count++
                    }
                    if (dest.name.indexOf("复件") != -1 && dest.name.indexOf(
                            src.name.run { substring(indexOf(")") + 1, length) }) != -1
                    ) count++
                }
            }
            writeDestFileFromSrcFile(
                src, when {
                    isCover -> when (count) {
                        1 -> when {
                            dest.toString().indexOf(".") != -1 -> File("$dest\\复件 ")
                            else -> File("$dest\\复件 ${src.name}")
                        }
                        else -> when {
                            dest.toString().indexOf(".") != -1 -> File("$dest\\复件 ($count) ")
                            else -> File("$dest\\复件 ($count) ${src.name}")
                        }
                    }
                    else -> when {
                        dest.toString().indexOf(".") != -1 -> File("$dest\\")
                        else -> File("$dest\\${src.name}")
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false.apply { error("$loggerTag->copy:文件复制操作出现异常！") }
        }
        src.isDirectory -> {
            dest.mkdir()
            var bool = true
            src.listFiles()?.run {
                if (isNotEmpty()) for (file in this) {
                    file.absolutePath.run { substring(src.absolutePath.length, length) }
                        .let { bool = bool && copy(file, File("${dest.absolutePath}$it")) }
                }
            }
            bool
        }
        else -> false
    }

    private fun writeDestFileFromSrcFile(srcFile: File, destFile: File): Boolean = try {
        FileInputStream(srcFile).use { fileInputStream ->
            fileInputStream.channel.use { srcChannel ->
                FileOutputStream(destFile).use { fileOutputStream ->
                    fileOutputStream.channel.use { destChannel ->
                        true.apply { srcChannel.transferTo(0, srcChannel.size(), destChannel) }
                    }
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    fun mergeFiles(files: MutableList<File>, outputFile: File) = try {
        FileOutputStream(outputFile).use { fileOutputStream ->
            fileOutputStream.channel.use { outputChannel ->
                for (file in files) {
                    FileInputStream(file).use { fileInputStream ->
                        fileInputStream.channel.use { inputChannel ->
                            ByteBuffer.allocate(1024 * 8).run {
                                while (true) {
                                    if (inputChannel.read(this) != -1) {
                                        flip()
                                        outputChannel.write(this)
                                        clear()
                                    } else break
                                }
                            }
                        }
                    }
                }
                debug("$loggerTag->拼接完成！")
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    fun shareFile(title: String?, filePath: String?) =
        app.startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, Uri.parse("file://$filePath"))
        }, title))

    fun downloadFile(fileUrl: String?): Long = fileUrl?.run {
        app.downloadManager.enqueue(DownloadManager.Request(Uri.parse(this)).apply {
            setDestinationInExternalPublicDir("/Download/", substring(lastIndexOf("/") + 1))
        })
    } ?: -1L

    class DownloadService : Service() {
        override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            intent.getStringExtra("fileurl")?.let { fileUrl ->
                warn("onStartCommand:$fileUrl")
                fileUrl.run { substring(lastIndexOf("/") + 1) }.let { fileName ->
                    DownloadManager.Request(Uri.parse(fileUrl))
                        .apply { setDestinationInExternalPublicDir("/Download/", fileName) }
                        .let { request ->
                            registerReceiver(object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent) {
                                    downloadManager.query(DownloadManager.Query()
                                        .apply { setFilterById(downloadManager.enqueue(request)) })
                                        .use { cursor ->
                                            cursor.run {
                                                if (moveToFirst())
                                                    when (getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                                                        DownloadManager.STATUS_RUNNING -> Unit
                                                        DownloadManager.STATUS_PENDING -> Unit
                                                        DownloadManager.STATUS_PAUSED -> Unit
                                                        DownloadManager.STATUS_FAILED -> Unit
                                                        DownloadManager.STATUS_SUCCESSFUL ->
                                                            installApp("/sdcard/Download/$fileName")
                                                    }
                                            }
                                        }
                                }
                            }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                        }
                }
            }
            return super.onStartCommand(intent, flags, startId)
        }

        override fun onBind(intent: Intent): IBinder? = null
    }

    fun upgradeApp(fileUrl: String?): ComponentName? = app.startService(Intent().apply {
        component = ComponentName(app, DownloadService::class.java)
        putExtra("fileUrl", fileUrl)
    })

    fun delete(filePath: String?): Boolean = delete(getFileByPath(filePath))
    fun delete(file: File?): Boolean =
        file?.run { if (isDirectory) deleteDir(this) else deleteFile(this) } ?: false

    fun deleteDir(dirPath: String?): Boolean = deleteDir(getFileByPath(dirPath))
    fun deleteDir(dir: File?): Boolean = dir?.run {
        when {
            !exists() -> true
            isFile -> false
            else -> apply {
                listFiles()?.let { files ->
                    if (files.isNotEmpty()) for (file in files) {
                        when {
                            file.isFile && !file.delete() -> return false
                            file.isDirectory && !deleteDir(file) -> return false
                        }
                    }
                }
            }.delete()
        }
    } ?: false

    fun deleteFile(srcFilePath: String?): Boolean = deleteFile(getFileByPath(srcFilePath))
    fun deleteFile(file: File?): Boolean = file?.run { !exists() || (isFile && delete()) } ?: false
    fun deleteFiles(dirPath: String?): Boolean = deleteFiles(getFileByPath(dirPath))
    fun deleteFiles(dir: File?): Boolean = deleteFilesByFilter(dir, FileFilter { it.isFile })

    @JvmOverloads
    fun deleteFilesByFilter(dirPath: String?, filter: FileFilter = FileFilter { true }): Boolean =
        deleteFilesByFilter(getFileByPath(dirPath), filter)

    @JvmOverloads
    fun deleteFilesByFilter(dir: File?, filter: FileFilter = FileFilter { true }): Boolean =
        dir?.run {
            when {
                !exists() -> true
                isFile -> false
                else -> listFiles()?.let { files ->
                    if (files.isNotEmpty()) for (file in files) {
                        if (filter.accept(file)) when {
                            file.isFile && !file.delete() -> return false
                            file.isDirectory && !deleteDir(file) -> return false
                        }
                    }
                    true
                } ?: true
            }
        } ?: false

    @JvmOverloads
    fun listFilesInDirWithFilter(
        dirPath: String?, filter: FileFilter = FileFilter { true }, isRecursive: Boolean = false
    ): MutableList<File>? = listFilesInDirWithFilter(getFileByPath(dirPath), filter, isRecursive)

    @JvmOverloads
    fun listFilesInDirWithFilter(
        dir: File?, filter: FileFilter = FileFilter { true }, isRecursive: Boolean = false
    ): MutableList<File>? = when {
        isExistsDir(dir) -> dir?.listFiles()?.let { files ->
            when {
                files.isEmpty() -> null
                else -> mutableListOf<File>().apply {
                    for (file in files) {
                        if (filter.accept(file)) add(file)
                        if (isRecursive && file.isDirectory)
                            listFilesInDirWithFilter(file, filter, isRecursive)?.let { addAll(it) }
                    }
                }
            }
        }
        else -> null
    }

    fun getFileLastModified(filePath: String?): Long = getFileLastModified(getFileByPath(filePath))
    fun getFileLastModified(file: File?): Long = file?.lastModified() ?: -1L
    fun getFileCharset(filePath: String?): String = getFileCharset(getFileByPath(filePath))
    fun getFileCharset(file: File?): String = try {
        file?.run {
            FileInputStream(this).use { fileInputStream ->
                BufferedInputStream(fileInputStream).use { bufferedInputStream ->
                    bufferedInputStream.run { (read() shl 8) + read() }
                }
            }
        } ?: 0
    } catch (e: IOException) {
        e.printStackTrace()
        0
    }.let {
        when (it) {
            0xefbb -> "UTF-8"
            0xfeff -> "UTF-16BE"
            0xfffe -> "Unicode"
            else -> "GBK"
        }
    }

    fun getFileLines(filePath: String?): Int = getFileLines(getFileByPath(filePath))
    fun getFileLines(file: File?): Int = try {
        file?.run {
            FileInputStream(this).use { fileInputStream ->
                BufferedInputStream(fileInputStream).use { bufferedInputStream ->
                    (if (System.getProperty("line.separator")?.endsWith("\n") == true) '\n'
                    else '\r').toByte()
                        .let {
                            ByteArray(1024).let { bytes ->
                                var count = 1
                                while (true) {
                                    if (bufferedInputStream.read(bytes) == -1) break
                                    else for (byte in bytes) {
                                        if (byte == it) count++
                                    }
                                }
                                count
                            }
                        }
                }
            }
        } ?: 1
    } catch (e: IOException) {
        e.printStackTrace()
        1
    }

    val allSizeInternal: Long
        get() = getAllSize(pathData)
    val allSizeExternal: Long
        get() = if (isSdCardEnable) getAllSize(pathExternal) else 0L

    fun getAllSize(path: String?): Long = StatFs(path).run {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 ->
                blockCount.toLong() * blockSize.toLong()
            else -> blockCountLong * blockSizeLong
        }
    }

    val availableSizeInternal: Long
        get() = getFreeSize(pathData)
    val availableSizeExternal: Long
        get() = if (isSdCardEnable) getAvailableSize(pathExternal) else 0L

    fun getAvailableSize(path: String?): Long = StatFs(path).run {
        when {
            aboveJellyBeanMR2 -> (availableBlocksLong) * blockSizeLong
            else -> (availableBlocks.toLong()) * blockSize.toLong()
        }
    }

    val freeSizeInternal: Long
        get() = getFreeSize(pathData)
    val freeSizeExternal: Long
        get() = if (isSdCardEnable) getFreeSize(pathExternal) else 0L

    fun getFreeSize(path: String?): Long = StatFs(path).run {
        when {
            aboveJellyBeanMR2 -> (availableBlocksLong - 4) * blockSizeLong
            else -> (availableBlocks.toLong() - 4) * blockSize.toLong()
        }
    }

    fun getDirsSizeFit(vararg dirs: String?): String = byteSize2MemorySizeFit(getDirsSize(*dirs))
    fun getDirsSize(vararg dirs: String?): Long = arrayOfNulls<File>(dirs.size).apply {
        for ((index, dir) in dirs.withIndex()) {
            this[index] = getFileByPath(dir)
        }
    }.let { getDirsSize(*it) }

    fun getDirsSizeFit(vararg dirs: File?): String = byteSize2MemorySizeFit(getDirsSize(*dirs))
    fun getDirsSize(vararg dirs: File?): Long = try {
        var length = 0L
        for (dir in dirs) {
            length += getDirSize(dir)
        }
        length
    } catch (e: Exception) {
        e.printStackTrace()
        0L
    }

    fun getDirSizeFit(dirPath: String?): String = getDirSizeFit(getFileByPath(dirPath))
    fun getDirSizeFit(dir: File?): String =
        getDirSize(dir).let { if (it == 0L) "" else byteSize2MemorySizeFit(it, 4) }

    fun getDirSize(dirPath: String?): Long = getDirSize(getFileByPath(dirPath))
    fun getDirSize(dir: File?): Long = when {
        isExistsDir(dir) -> dir?.listFiles()?.run {
            var length = 0L
            if (isNotEmpty()) for (file in this) {
                length += if (file.isDirectory) getDirSize(file) else file.length()
            }
            length
        } ?: 0L
        else -> 0L
    }

    fun getFileSizeFit(filePath: String?): String =
        getFileSize(filePath).let { if (it == 0L) "" else byteSize2MemorySizeFit(it, 4) }

    fun getFileSize(filePath: String?): Long = filePath?.run {
        when {
            matches("[a-zA-Z]+://[^\\s]*".toRegex()) -> try {
                (URL(filePath).openConnection() as HttpsURLConnection)
                    .apply { setRequestProperty("Accept-Encoding", "identity") }.run {
                        connect()
                        if (responseCode == 200) contentLength.toLong() else 0L
                    }
            } catch (e: IOException) {
                e.printStackTrace()
                getFileSize(getFileByPath(filePath))
            }
            else -> getFileSize(getFileByPath(filePath))
        }
    } ?: 0L

    fun getFileSizeFit(file: File?): String =
        getFileSize(file).let { if (it == 0L) "" else byteSize2MemorySizeFit(it, 4) }

    fun getFileSize(file: File?): Long = if (isExistsFile(file)) file?.length() ?: 0L else 0L
    fun getDirName(file: File?): String = file?.run { getDirName(absolutePath) } ?: ""
    fun getDirName(filePath: String?): String = filePath?.run {
        when {
            isSpace(this) -> ""
            else -> lastIndexOf(File.separator)
                .let { index -> if (index == -1) "" else substring(0, index + 1) }
        }
    } ?: ""

    fun getFileName(file: File?): String = file?.run { getFileName(absolutePath) } ?: ""
    fun getFileName(filePath: String?): String = filePath?.run {
        when {
            isSpace(this) -> ""
            else -> lastIndexOf(File.separator)
                .let { index -> if (index == -1) this else substring(index + 1) }
        }
    } ?: ""

    fun getFileNoExtension(file: File?): String = file?.run { getFileNoExtension(path) } ?: ""
    fun getFileNoExtension(filePath: String?): String = filePath?.run {
        when {
            isSpace(this) -> ""
            else -> lastIndexOf('.').let { lastPoi ->
                when (val lastSep = lastIndexOf(File.separator)) {
                    -1 -> if (lastPoi == -1) this else substring(0, lastPoi)
                    else -> when {
                        lastPoi == -1 || lastSep > lastPoi -> substring(lastSep + 1)
                        else -> substring(lastSep + 1, lastPoi)
                    }
                }
            }
        }
    } ?: ""

    fun getFileExtension(file: File?): String = file?.run { getFileExtension(path) } ?: ""
    fun getFileExtension(filePath: String?): String = filePath?.run {
        when {
            isSpace(this) -> ""
            else -> lastIndexOf('.').let { index ->
                when {
                    index == -1 || lastIndexOf(File.separator) >= index -> ""
                    else -> substring(index + 1)
                }
            }
        }
    } ?: ""

    fun getFileIntent(path: String?, mimeType: String): Intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = Uri.fromFile(getFileByPath(path))
        type = mimeType
    }

    fun getNativeM3u8(file: File?, pathList: MutableList<File>): String = StringBuffer().apply {
        try {
            file?.let {
                FileInputStream(file).use { fileInputStream ->
                    InputStreamReader(fileInputStream).use { inputStreamReader ->
                        BufferedReader(inputStreamReader).use { bufferedReader ->
                            var num = 0
                            while (true) {
                                bufferedReader.readLine()?.let { line ->
                                    when {
                                        line.isNotEmpty() && line.startsWith("http://") ->
                                            append("file:${pathList[num++].absolutePath}\r\n")
                                        else -> append("$line\r\n")
                                    }
                                } ?: break
                            }
                            writeFileFromString(file, toString())
                            debug("$loggerTag->替换完成！")
                        }
                    }
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }.toString()

    fun getFileUri(file: File?): Uri? = file?.let {
        when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.N -> Uri.fromFile(file)
            else -> FileProvider.getUriForFile(app, "$appPackageName.fileprovider", file)
        }
    }

    fun getImageContentUri(imageFile: File?): Uri? = app.contentResolver.run {
        imageFile?.absolutePath?.let { filePath ->
            query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID),
                "${MediaStore.Images.Media.DATA}=? ", arrayOf(filePath), null
            )?.use { cursor ->
                cursor.run {
                    when {
                        moveToFirst() -> Uri.withAppendedPath(
                            Uri.parse("content://media/external/images/media"),
                            "${getInt(getColumnIndex(MediaStore.MediaColumns._ID))}"
                        )
                        else -> when {
                            imageFile.exists() -> ContentValues()
                                .apply { put(MediaStore.Images.Media.DATA, filePath) }
                                .let { insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, it) }
                            else -> null
                        }
                    }
                }
            }
        }
    }

    fun getFileFromUri(uri: Uri?): File? = uri?.let { getFileByPath(getPathFromUri(uri)) }
    fun getPathFromUri(uri: Uri?): String = uri?.let {
        when {
            aboveKitKat && DocumentsContract.isDocumentUri(app, uri) -> when {
                isExternalStorageDocument(uri) -> DocumentsContract.getDocumentId(uri)
                    .split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    .let { strings ->
                        when {
                            "primary".equals(strings[0], true) -> "${pathExternal}${strings[1]}"
                            else -> ""
                        }
                    }
                isDownloadsDocument(uri) -> getDataColumn(
                    ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        DocumentsContract.getDocumentId(uri).toLong()
                    ), null, null
                )
                isMediaDocument(uri) -> DocumentsContract.getDocumentId(uri)
                    .split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    .let { strings ->
                        when (strings[0]) {
                            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            else -> null
                        }?.let { getDataColumn(it, "_id=?", arrayOf(strings[1])) } ?: ""
                    }
                else -> ""
            }
            "content".equals(uri.scheme, true) -> when {
                isGooglePhotosUri(uri) -> uri.lastPathSegment
                else -> getDataColumn(uri, null, null)
            }
            "file".equals(uri.scheme, true) -> uri.path
            else -> when (uri.scheme) {
                null, ContentResolver.SCHEME_FILE -> uri.path
                ContentResolver.SCHEME_CONTENT -> app.contentResolver.query(
                    uri, arrayOf(MediaStore.Images.ImageColumns.DATA), null, null, null
                )?.use { cursor ->
                    cursor.run {
                        when {
                            moveToFirst() -> getColumnIndex(MediaStore.Images.ImageColumns.DATA)
                                .let { index -> if (index > -1) getString(index) else "" }
                            else -> ""
                        }
                    }
                } ?: ""
                else -> ""
            }
        }
    } ?: ""

    fun isExternalStorageDocument(uri: Uri): Boolean =
        uri.authority == "com.android.externalstorage.documents"

    fun isDownloadsDocument(uri: Uri): Boolean =
        uri.authority == "com.android.providers.downloads.documents"

    fun isMediaDocument(uri: Uri): Boolean =
        uri.authority == "com.android.providers.media.documents"

    fun isGooglePhotosUri(uri: Uri): Boolean =
        uri.authority == "com.google.android.apps.photos.content"

    fun getDataColumn(uri: Uri, selection: String?, selectionArgs: Array<String>?): String? =
        MediaStore.Images.Media.DATA.let { column ->
            app.contentResolver
                .query(uri, arrayOf(column), selection, selectionArgs, null)?.use { cursor ->
                    cursor.run { if (moveToFirst()) getString(getColumnIndexOrThrow(column)) else null }
                }
        }

    @JvmOverloads
    fun rename(filePath: String?, newName: String? = null): Boolean = filePath?.run {
        newName?.let { rename(getFileByPath(this), newName) }
            ?: rename(getFileByPath(this))
    } ?: false

    @JvmOverloads
    fun rename(file: File?, newName: String? = null): Boolean = file?.run {
        when {
            !exists() || isSpace(newName) -> false
            name == newName -> true
            else -> File("$parent${File.separator}${newName ?: "new$name"}")
                .run { !exists() && renameTo(this) }
        }
    } ?: false
}