package top.autoget.autokit

import android.os.Bundle
import android.os.Handler
import android.os.Message
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.model.enums.EncryptionMethod
import top.autoget.autokit.FileKit.createDirNone
import top.autoget.autokit.FileKit.createFileNone
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.StringKit.isSpace
import java.io.File

object ZipPlusKit : LoggerKit {
    @JvmOverloads
    fun zipEncrypt(
        srcFilePath: String?, destPath: String?,
        isCreateDir: Boolean = false, passWord: String? = null
    ): String? = srcFilePath?.let { zipEncrypt(getFileByPath(it), destPath, isCreateDir, passWord) }

    @JvmOverloads
    fun zipEncrypt(
        srcFile: File?, destPath: String?, isCreateDir: Boolean = false, passWord: String? = null
    ): String? = when {
        srcFile == null || destPath == null -> null
        else -> buildZipPath(srcFile, destPath).let { dest ->
            ZipParameters().apply {
                compressionMethod = CompressionMethod.DEFLATE//默认
                compressionLevel = CompressionLevel.NORMAL
                if (isNotSpace(passWord)) {
                    isEncryptFiles = true
                    encryptionMethod = EncryptionMethod.ZIP_STANDARD
                }
            }.let { param ->
                try {
                    when {
                        isSpace(passWord) -> ZipFile(dest)
                        else -> ZipFile(dest, passWord?.toCharArray())
                    }.run {
                        when {
                            srcFile.isDirectory -> when {
                                isCreateDir -> addFolder(srcFile, param)
                                else -> addFiles(srcFile.listFiles()?.toList(), param)
                            }
                            else -> addFile(srcFile, param)
                        }
                    }
                    dest
                } catch (e: ZipException) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    private fun buildZipPath(srcFile: File, destPath: String): String =
        srcFile.name.run { substring(0, lastIndexOf(".")) }.let { fileName ->
            when {
                isSpace(destPath) -> when {
                    srcFile.isDirectory -> "${srcFile.parent}${File.separator}${srcFile.name}.zip"
                    else -> "${srcFile.parent}${File.separator}$fileName.zip"
                }
                else -> when {
                    destPath.endsWith(File.separator) -> when {
                        srcFile.isDirectory -> "$destPath${srcFile.name}.zip"
                        else -> "$destPath$fileName.zip"
                    }
                    else -> destPath
                }.apply { createDirOrFile(destPath) }
            }
        }

    private fun createDirOrFile(destPath: String): Boolean = when {
        destPath.endsWith(File.separator) -> createDirNone(File(destPath))
        else -> createFileNone(File(destPath.run { substring(0, lastIndexOf(File.separator)) }))
    }

    @JvmOverloads
    fun zipEncryptRargo(
        srcFilePath: String?, destPath: String?, unitKB: Int,
        isCreateDir: Boolean = false, passWord: String? = null
    ): String? = srcFilePath?.let {
        zipEncryptRargo(getFileByPath(it), destPath, unitKB, isCreateDir, passWord)
    }

    @JvmOverloads
    fun zipEncryptRargo(
        srcFile: File?, destPath: String?, kB: Int,
        isCreateDir: Boolean = false, passWord: String? = null
    ): String? = when {
        srcFile == null || destPath == null -> null
        else -> buildZipPath(srcFile, destPath).let { dest ->
            ZipParameters().apply {
                compressionMethod = CompressionMethod.DEFLATE//默认
                compressionLevel = CompressionLevel.NORMAL
                if (isNotSpace(passWord)) {
                    isEncryptFiles = true
                    encryptionMethod = EncryptionMethod.NONE
                }
            }.let { param ->
                (kB * 1024L).let { byte ->
                    try {
                        when {
                            isSpace(passWord) -> ZipFile(dest)
                            else -> ZipFile(dest, passWord?.toCharArray())
                        }.run {
                            when {
                                srcFile.isDirectory -> when {
                                    isCreateDir ->
                                        createSplitZipFileFromFolder(srcFile, param, true, byte)
                                    else -> createSplitZipFile(
                                        srcFile.listFiles()?.toList(), param, true, byte
                                    )
                                }
                                else -> createSplitZipFile(
                                    srcFile.listFiles()?.toList(), param, true, byte
                                )
                            }
                        }
                        dest.apply { println("分割成功！总共分割成了${zipInfo(this).toInt() / kB + 1}个文件！") }
                    } catch (e: ZipException) {
                        e.printStackTrace()
                        null
                    }
                }
            }
        }
    }//分卷

    @Throws(ZipException::class)
    fun zipInfo(zipFile: String): Double = ZipFile(zipFile).fileHeaders.let { headers ->
        var zipCompressedSize: Long = 0
        for (fileHeader in headers) {
            fileHeader.run {
                zipCompressedSize += compressedSize
                println(
                    """$zipFile->文件相关信息如下：
                |Name:$fileName
                |Compressed Size:${compressedSize.toDouble() / 1024}KB
                |Uncompressed Size:${uncompressedSize.toDouble() / 1024}KB
                |CRC32:$crc""".trimMargin()
                )
            }
        }
        zipCompressedSize.toDouble() / 1024
    }//KB

    @JvmOverloads
    fun unzipFilesByKeyword(
        zipFilePaths: Collection<String>?, destDirPath: String?, passWord: String? = null
    ): Boolean {
        when {
            zipFilePaths == null || destDirPath == null -> return false
            else -> getFileByPath(destDirPath)?.let { destDir ->
                for (zipFilePath in zipFilePaths) {
                    getFileByPath(zipFilePath)?.let { zipFile ->
                        if (!unzipFileByKeyword(zipFile, destDir, passWord)) return false
                    } ?: return false
                }
                return true
            } ?: return false
        }
    }

    @JvmOverloads
    fun unzipFilesByKeyword(
        zipFiles: Collection<File>?, destDir: File?, passWord: String? = null
    ): Boolean {
        when {
            zipFiles == null || destDir == null -> return false
            else -> {
                for (zipFile in zipFiles) {
                    if (!unzipFileByKeyword(zipFile, destDir, passWord)) return false
                }
                return true
            }
        }
    }

    @JvmOverloads
    fun unzipFileByKeyword(
        zipFilePath: String, destDirPath: String, passWord: String? = null
    ): Boolean =
        unzipFileByKeyword(getFileByPath(zipFilePath), getFileByPath(destDirPath), passWord)

    @JvmOverloads
    fun unzipFileByKeyword(zipFile: File?, destDir: File?, passWord: String? = null): Boolean =
        unzipFile(zipFile, destDir, passWord) != null

    @JvmOverloads
    fun unzipFile(
        zipFilePath: String, destDirPath: String, keyword: String? = null
    ): MutableList<File>? =
        unzipFile(getFileByPath(zipFilePath), getFileByPath(destDirPath), keyword)

    @JvmOverloads
    fun unzipFile(
        zipFile: File?, destDir: File?, passWord: String? = null
    ): MutableList<File>? = try {
        when {
            zipFile == null -> throw ZipException("压缩文件不存在。")
            destDir == null -> throw ZipException("解压缩路径不存在。")
            destDir.isDirectory -> createDirNone(destDir)
        }
        (if (isSpace(passWord)) ZipFile(zipFile) else ZipFile(zipFile, passWord?.toCharArray()))
            .apply { if (isValidZipFile) extractAll(destDir?.absolutePath) else throw ZipException("压缩文件不合法，可能被损坏。") }
            .fileHeaders.let { headers ->
                mutableListOf<File>().apply {
                    for (fileHeader in headers) {
                        fileHeader.run { if (!isDirectory) add(File(destDir, fileName)) }
                    }
                }
            }
    } catch (e: ZipException) {
        e.printStackTrace()
        null
    }

    @JvmOverloads
    fun unzipFileWithMonitor(
        srcFilePath: String?, destPath: String?, passWord: String? = null,
        handler: Handler?, isDeleteZipFile: Boolean = false
    ): String? = srcFilePath?.let {
        unzipFileWithMonitor(getFileByPath(it), destPath, passWord, handler, isDeleteZipFile)
    }

    @JvmOverloads
    fun unzipFileWithMonitor(
        srcFile: File?, destPath: String?, passWord: String? = null,
        handler: Handler?, isDeleteZipFile: Boolean = false
    ): String? = when {
        srcFile == null || destPath == null -> null
        else -> try {
            (if (isSpace(passWord)) ZipFile(srcFile) else ZipFile(srcFile, passWord?.toCharArray()))
                .apply { if (!isValidZipFile) throw ZipException("压缩文件不合法，可能被损坏。") }
                .let { zipFile ->
                    createDirNone(getFileByPath(destPath))
                    Thread(Runnable {
                        try {
                            handler?.run {
                                sendEmptyMessage(CompressStatus.START)
                                while (true) {
                                    Thread.sleep(1000)
                                    val percentDone: Int = zipFile.progressMonitor.percentDone
                                    sendMessage(Message().apply {
                                        what = CompressStatus.HANDLING
                                        data = Bundle().apply {
                                            putInt(CompressKeys.PERCENT, percentDone)
                                        }
                                    })
                                    if (percentDone >= 100) break
                                }
                                sendEmptyMessage(CompressStatus.COMPLETED)
                            } ?: return@Runnable
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                            handler?.sendMessage(Message().apply {
                                what = CompressStatus.ERROR
                                data = Bundle().apply { putString(CompressKeys.ERROR, e.message) }
                            })
                        } finally {
                            if (isDeleteZipFile) srcFile.deleteOnExit()
                        }
                    }).start()
                    zipFile.apply { isRunInThread = true }.extractAll(destPath)
                    destPath
                }
        } catch (e: ZipException) {
            e.printStackTrace()
            null
        }
    }

    object CompressStatus {
        const val START = 0
        const val HANDLING = 1
        const val COMPLETED = 2
        const val ERROR = 3
    }

    object CompressKeys {
        const val PERCENT = "PERCENT"
        const val ERROR = "ERROR"
    }

    fun removeDirFromZipArchive(srcFilePath: String?, removeDir: String?): Boolean =
        srcFilePath?.let { removeDirFromZipArchive(getFileByPath(it), removeDir) } ?: false

    fun removeDirFromZipArchive(srcFile: File?, removeDir: String?): Boolean = when {
        srcFile == null || removeDir == null -> false
        else -> try {
            ZipFile(srcFile).let { zipFile ->
                (zipFile.getFileHeader(removeDir.run { if (endsWith(File.separator)) this else "$this${File.separator}" }))?.run {
                    mutableListOf<String>().let { removeHeaderNames ->
                        for (fileHeader in zipFile.fileHeaders) {
                            fileHeader.fileName.let { subHeaderName ->
                                if (subHeaderName.startsWith(fileName) && subHeaderName != fileName)
                                    removeHeaderNames.add(subHeaderName)
                            }
                        }
                        for (removeHeaderName in removeHeaderNames) {
                            zipFile.removeFile(removeHeaderName)
                        }
                    }
                    zipFile.removeFile(this)
                    true
                } ?: false
            }
        } catch (e: ZipException) {
            e.printStackTrace()
            false
        }
    }
}