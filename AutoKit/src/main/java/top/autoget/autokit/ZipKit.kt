package top.autoget.autokit

import top.autoget.autokit.FileKit.createFileNone
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.StringKit.isSpace
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object ZipKit : LoggerKit {
    @Throws(IOException::class)
    @JvmOverloads
    fun zipFiles(
        srcFilePaths: Collection<String>?, zipFilePath: String?,
        comment: String = "", rootPath: String = "", zipListener: ZipListener
    ): Boolean = when {
        srcFilePaths == null || zipFilePath == null -> false
        else -> FileOutputStream(zipFilePath).use { fileOutputStream ->
            BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                ZipOutputStream(bufferedOutputStream).use { zipOutputStream ->
                    for (srcFilePath in srcFilePaths) {
                        if (isStopZip) break
                        getFileByPath(srcFilePath)?.let {
                            if (!zipFile(it, zipOutputStream, comment, rootPath, zipListener))
                                return false
                        } ?: return false
                    }
                    true
                }
            }
        }
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun zipFiles(
        srcFiles: Collection<File>?, zipFile: File?,
        comment: String = "", rootPath: String = "", zipListener: ZipListener
    ): Boolean =
        when {
            srcFiles == null || zipFile == null -> false
            else -> FileOutputStream(zipFile).use { fileOutputStream ->
                BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                    ZipOutputStream(bufferedOutputStream).use { zipOutputStream ->
                        for (srcFile in srcFiles) {
                            if (isStopZip) break
                            if (!zipFile(srcFile, zipOutputStream, comment, rootPath, zipListener))
                                return false
                        }
                        true
                    }
                }
            }
        }

    @Throws(IOException::class)
    @JvmOverloads
    fun zipFile(
        srcFilePath: String, zipFilePath: String,
        comment: String = "", rootPath: String = "", zipListener: ZipListener
    ): Boolean = zipFile(
        getFileByPath(srcFilePath), getFileByPath(zipFilePath), comment, rootPath, zipListener
    )

    @Throws(IOException::class)
    @JvmOverloads
    fun zipFile(
        srcFile: File?, zipFile: File?,
        comment: String = "", rootPath: String = "", zipListener: ZipListener
    ): Boolean = when {
        srcFile == null || zipFile == null -> false
        else -> FileOutputStream(zipFile).use { fileOutputStream ->
            BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                ZipOutputStream(bufferedOutputStream).use { zipOutputStream ->
                    zipFile(srcFile, zipOutputStream, comment, rootPath, zipListener)
                }
            }
        }
    }

    interface ZipListener {
        fun zipProgress(zipProgress: Int)
    }

    var isStopZip: Boolean = false
    private const val BUFFER_SIZE: Int = 1024 * 1024

    @Throws(IOException::class)
    private fun zipFile(
        srcFile: File, zipOutputStream: ZipOutputStream,
        commentStr: String, rootPath: String, zipListener: ZipListener
    ): Boolean = zipOutputStream.use {
        String(
            "$rootPath${(if (isSpace(rootPath)) "" else File.separator)}${srcFile.name}"
                .toByteArray(charset("8859_1")), charset("GB2312")
        ).let { path ->
            when {
                srcFile.isDirectory -> srcFile.listFiles().let { files ->
                    when {
                        files == null || files.isEmpty() -> ZipEntry("$path/")
                            .apply { if (isSpace(commentStr)) comment = commentStr }
                            .let { zipEntry ->
                                it.run {
                                    putNextEntry(zipEntry)
                                    closeEntry()
                                }
                            }
                        else -> {
                            (1 / (files.size + 1f) * 100).toInt().let { progress ->
                                error("zipProgress->$progress%")
                                zipListener.zipProgress(progress)
                            }
                            for ((index, file) in files.withIndex()) {
                                if (isStopZip) break
                                ((index + 2) / (files.size + 1f) * 100).toInt().let { progress ->
                                    error("zipProgress->$progress%")
                                    zipListener.zipProgress(progress)
                                }
                                if (!zipFile(file, it, commentStr, path, zipListener)) return false
                            }
                        }
                    }
                }
                else -> FileInputStream(srcFile).use { fileInputStream ->
                    BufferedInputStream(fileInputStream).use { bufferedInputStream ->
                        ZipEntry(path).apply { if (isSpace(commentStr)) comment = commentStr }
                            .let { zipEntry ->
                                it.run {
                                    putNextEntry(zipEntry)
                                    ByteArray(BUFFER_SIZE).let { bytes ->
                                        while (true) {
                                            if (isStopZip) break
                                            if (bufferedInputStream.read(bytes) != -1) write(bytes) else break
                                        }
                                    }
                                    flush()
                                    closeEntry()
                                }
                            }
                    }
                }
            }
            true
        }
    }

    fun fileToZip(srcFilePath: String?, destFilePath: String?, fileName: String?): Boolean =
        srcFilePath?.let { fileToZip(getFileByPath(it), destFilePath, fileName) }
            ?: false.apply { println("待压缩的文件目录：$srcFilePath 不存在。") }

    fun fileToZip(srcFile: File?, destFilePath: String?, fileName: String?): Boolean =
        srcFile?.let {
            when {
                it.exists() -> try {
                    when {
                        destFilePath == null || isSpace(destFilePath) ->
                            false.apply { println("$destFilePath 目录名称错误。") }
                        fileName == null || isSpace(fileName) ->
                            false.apply { println("$fileName 文件名称错误。") }
                        else -> File("${destFilePath.run { if (endsWith(File.separator)) this else "$this${File.separator}" }}$fileName.zip").let { destFile ->
                            when {
                                destFile.exists() -> false.apply { println("$destFilePath 目录下存在名字为:$fileName.zip打包文件。") }
                                else -> it.listFiles()?.run {
                                    when {
                                        isEmpty() -> false.apply { println("待压缩的文件目录：$srcFile 里面不存在文件，无需压缩。") }
                                        else -> {
                                            for (file in this) {
                                                subFileToZip(file, destFile)
                                            }
                                            true
                                        }
                                    }
                                } ?: false.apply { println("待压缩的文件目录：$srcFile 里面不存在文件，无需压缩。") }
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw RuntimeException(e)
                }
                else -> false.apply { println("待压缩的文件目录：$srcFile 不存在。") }
            }
        } ?: false.apply { println("待压缩的文件目录：$srcFile 不存在。") }

    private fun subFileToZip(subFile: File, zipFile: File) =
        FileInputStream(subFile).use { fileInputStream ->
            BufferedInputStream(fileInputStream).use { bufferedInputStream ->
                FileOutputStream(zipFile).use { fileOutputStream ->
                    BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                        ZipOutputStream(bufferedOutputStream).use { zipOutputStream ->
                            zipOutputStream.run {
                                putNextEntry(ZipEntry(subFile.name))
                                ByteArray(BUFFER_SIZE).let { bytes ->
                                    while (true) {
                                        if (bufferedInputStream.read(bytes) != -1) write(bytes) else break
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    @JvmOverloads
    fun unzipSelectedFile(
        zipFilePath: String, destDirPath: String, nameContains: String = ""
    ): MutableList<File>? =
        unzipSelectedFile(getFileByPath(zipFilePath), getFileByPath(destDirPath), nameContains)

    @JvmOverloads
    fun unzipSelectedFile(
        zipFile: File?, destDir: File?, nameContains: String = ""
    ): MutableList<File>? = when {
        zipFile == null || destDir == null -> null
        else -> try {
            mutableListOf<File>().apply {
                ZipFile(zipFile).use {
                    for (zipEntry in it.entries()) {
                        when {
                            zipEntry.name.contains("../") -> error("$loggerTag->entryName: ${zipEntry.name} is dangerous!")
                            zipEntry.name.contains(nameContains) ->
                                File(destDir, zipEntry.name).let { destFile ->
                                    createFileNone(destFile)
                                    it.getInputStream(zipEntry).use { inputStream ->
                                        BufferedInputStream(inputStream).use { bufferedInputStream ->
                                            FileOutputStream(destFile).use { fileOutputStream ->
                                                BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                                                    ByteArray(BUFFER_SIZE).let { bytes ->
                                                        while (true) {
                                                            if (bufferedInputStream.read(bytes) != -1)
                                                                bufferedOutputStream.write(bytes) else break
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    add(destFile)
                                }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }//解压文件名包含传入文字的文件，传入文字为空字符串解压所有文件
    }

    fun unzipOneFile(
        zipFilePath: String, destDirPath: String, nameEquals: String
    ): MutableList<File>? =
        unzipOneFile(getFileByPath(zipFilePath), getFileByPath(destDirPath), nameEquals)

    fun unzipOneFile(zipFile: File?, destDir: File?, nameEquals: String): MutableList<File>? =
        when {
            zipFile == null || destDir == null -> null
            else -> try {
                mutableListOf<File>().apply {
                    ZipFile(zipFile).use {
                        for (zipEntry in it.entries()) {
                            when {
                                zipEntry.name.contains("../") -> error("$loggerTag->entryName: ${zipEntry.name} is dangerous!")
                                zipEntry.name == nameEquals ->
                                    File(destDir, zipEntry.name).let { destFile ->
                                        createFileNone(destFile)
                                        it.getInputStream(zipEntry).use { inputStream ->
                                            BufferedInputStream(inputStream).use { bufferedInputStream ->
                                                FileOutputStream(destFile).use { fileOutputStream ->
                                                    BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                                                        ByteArray(BUFFER_SIZE).let { bytes ->
                                                            while (true) {
                                                                if (bufferedInputStream.read(bytes) != -1)
                                                                    bufferedOutputStream.write(bytes) else break
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        add(destFile)
                                    }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    @Throws(IOException::class)
    fun getFilesPath(zipFilePath: String): MutableList<String>? =
        getFilesPath(getFileByPath(zipFilePath))

    @Throws(IOException::class)
    fun getFilesPath(zipFile: File?): MutableList<String>? = zipFile?.let {
        mutableListOf<String>().apply {
            ZipFile(zipFile).use {
                for (zipEntry in it.entries()) {
                    if (zipEntry.name.contains("../"))
                        error("$loggerTag->entryName: ${zipEntry.name} is dangerous!")
                    add(String(zipEntry.name.toByteArray(charset("GB2312")), charset("8859_1")))
                }
            }
        }
    }//路径列表

    @Throws(IOException::class)
    fun getComments(zipFilePath: String): MutableList<String>? =
        getComments(getFileByPath(zipFilePath))

    @Throws(IOException::class)
    fun getComments(zipFile: File?): MutableList<String>? = zipFile?.let {
        mutableListOf<String>().apply {
            ZipFile(zipFile).use {
                for (zipEntry in it.entries()) {
                    if (zipEntry.comment.contains("../"))
                        error("$loggerTag->entryName: ${zipEntry.name} is dangerous!")
                    add(String(zipEntry.comment.toByteArray(charset("GB2312")), charset("8859_1")))
                }
            }
        }
    }//注释列表

    @Throws(IOException::class)
    fun getEntries(zipFilePath: String): Enumeration<*>? = getEntries(getFileByPath(zipFilePath))

    @Throws(IOException::class)
    fun getEntries(zipFile: File?): Enumeration<*>? =
        zipFile?.run { ZipFile(this).use { it.entries() } }//文件对象
}