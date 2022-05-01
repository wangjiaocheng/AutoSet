package top.autoget.autokit

import android.content.Context
import top.autoget.autokit.AKit.app
import top.autoget.autokit.FileKit.createFileNone
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.FileKit.isExistsTimestamp
import top.autoget.autokit.StringKit.isSpace
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

object FileIoKit {
    @JvmOverloads
    fun writeFileFromIS(
        filePath: String?, inputStream: InputStream?, append: Boolean = false
    ): Boolean = filePath?.let { writeFileFromIS(getFileByPath(it), inputStream, append) } ?: false

    @JvmOverloads
    fun writeFileFromIS(file: File?, inputStream: InputStream?, append: Boolean = false): Boolean =
        file?.let {
            when {
                createFileNone(file) -> try {
                    inputStream?.let {
                        FileOutputStream(file, append).use { fileOutputStream ->
                            BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                                ByteArray(8192).let { bytes ->
                                    while (true) {
                                        if (it.read(bytes) != -1) bufferedOutputStream.write(bytes) else break
                                    }
                                    true
                                }
                            }
                        }
                    } ?: false
                } catch (e: IOException) {
                    e.printStackTrace()
                    false
                }
                else -> false
            }
        } ?: false

    @JvmOverloads
    fun writeFileFromString(
        filePath: String?, content: String?, append: Boolean = false,
        charsetName: String = System.getProperty("file.encoding") ?: "UTF-8"
    ): Boolean = filePath?.let {
        writeFileFromString(getFileByPath(it), content, append, charsetName)
    } ?: false

    @JvmOverloads
    fun writeFileFromString(
        file: File?, content: String?, append: Boolean = false,
        charsetName: String = System.getProperty("file.encoding") ?: "UTF-8"
    ): Boolean = file?.let {
        when {
            createFileNone(file) -> try {
                content?.let {
                    FileOutputStream(file, append).use { fileOutputStream ->
                        OutputStreamWriter(fileOutputStream, charsetName)
                            .use { outputStreamWriter ->
                                BufferedWriter(outputStreamWriter).use { bufferedWriter ->
                                    bufferedWriter.write(content)
                                    true
                                }
                            }
                    }
                } ?: false
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
            else -> false
        }
    } ?: false

    @JvmOverloads
    fun writeAppFileFormBytes(
        filePath: String?, bytes: ByteArray?, modeType: Int = Context.MODE_PRIVATE
    ): Boolean =
        filePath?.let { writeAppFileFormBytes(getFileByPath(it), bytes, modeType) } ?: false

    @JvmOverloads
    fun writeAppFileFormBytes(
        file: File?, bytes: ByteArray?, modeType: Int = Context.MODE_PRIVATE
    ): Boolean = file?.let {
        when {
            createFileNone(file) -> try {
                bytes?.let {
                    app.openFileOutput(file.absolutePath, modeType).use { it.write(bytes) }
                    true
                } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            else -> false
        }//Context.MODE_PRIVATE、Context.MODE_APPEND、Context.MODE_WORLD_READABLE、Context.MODE_WORLD_WRITEABLE
    } ?: false

    @JvmOverloads
    fun writeFileFromBytesByStream(
        filePath: String?, bytes: ByteArray?, append: Boolean = false
    ): Boolean = filePath?.let {
        writeFileFromBytesByStream(getFileByPath(it), bytes, append)
    } ?: false

    @JvmOverloads
    fun writeFileFromBytesByStream(
        file: File?, bytes: ByteArray?, append: Boolean = false
    ): Boolean = file?.let {
        when {
            createFileNone(file) -> try {
                bytes?.let {
                    FileOutputStream(file, append).use { fileOutputStream ->
                        BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                            bufferedOutputStream.write(bytes)
                            true
                        }
                    }
                } ?: false
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
            else -> false
        }
    } ?: false

    @JvmOverloads
    fun writeFileFromBytesByChannel(
        filePath: String?, bytes: ByteArray, isForce: Boolean, append: Boolean = false
    ): Boolean = filePath?.let {
        writeFileFromBytesByChannel(getFileByPath(it), bytes, isForce, append)
    } ?: false

    @JvmOverloads
    fun writeFileFromBytesByChannel(
        file: File?, bytes: ByteArray?, isForce: Boolean, append: Boolean = false
    ): Boolean = file?.let {
        when {
            createFileNone(file) -> try {
                bytes?.let {
                    FileOutputStream(file, append).use { fileOutputStream ->
                        fileOutputStream.channel.use {
                            it.run {
                                position(size())
                                write(ByteBuffer.wrap(bytes))
                                if (isForce) force(true)
                                true
                            }
                        }
                    }
                } ?: false
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
            else -> false
        }
    } ?: false

    @JvmOverloads
    fun writeFileFromBytesByMap(
        filePath: String?, bytes: ByteArray?, isForce: Boolean, append: Boolean = false
    ): Boolean = filePath?.let {
        writeFileFromBytesByMap(getFileByPath(it), bytes, isForce, append)
    } ?: false

    @JvmOverloads
    fun writeFileFromBytesByMap(
        file: File?, bytes: ByteArray?, isForce: Boolean, append: Boolean = false
    ): Boolean = file?.let {
        when {
            createFileNone(file) -> try {
                bytes?.let {
                    FileOutputStream(file, append).use { fileOutputStream ->
                        fileOutputStream.channel.use {
                            it.map(FileChannel.MapMode.READ_WRITE, it.size(), bytes.size.toLong())
                                .run {
                                    put(bytes)
                                    if (isForce) force()
                                    true
                                }
                        }
                    }
                } ?: false
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
            else -> false
        }
    } ?: false

    @JvmOverloads
    fun readFile2List(
        filePath: String?, start: Int = 0, end: Int = 0x7FFFFFFF,
        charsetName: String = System.getProperty("file.encoding") ?: "UTF-8"
    ): MutableList<String>? =
        filePath?.let { readFile2List(getFileByPath(it), start, end, charsetName) }

    @JvmOverloads
    fun readFile2List(
        file: File?, start: Int = 0, end: Int = 0x7FFFFFFF,
        charsetName: String = System.getProperty("file.encoding") ?: "UTF-8"
    ): MutableList<String>? = file?.let {
        when {
            isExistsTimestamp(file) && start <= end -> try {
                mutableListOf<String>().apply {
                    FileInputStream(file).use { fileInputStream ->
                        when {
                            isSpace(charsetName) -> InputStreamReader(fileInputStream)
                            else -> InputStreamReader(fileInputStream, charsetName)
                        }.use { inputStreamReader ->
                            BufferedReader(inputStreamReader).use { bufferedReader ->
                                var curLine = 1
                                while (true) {
                                    if (curLine <= 0x7FFFFFFF && curLine in start..end)
                                        bufferedReader.readLine()?.let { add(it) } ?: break
                                    else break
                                    curLine++
                                }
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            else -> null
        }
    }

    @JvmOverloads
    fun readFile2StringByLine(
        filePath: String?, charsetName: String = System.getProperty("file.encoding") ?: "UTF-8"
    ): String? = filePath?.let { readFile2StringByLine(getFileByPath(it), charsetName) }

    @JvmOverloads
    fun readFile2StringByLine(
        file: File?, charsetName: String = System.getProperty("file.encoding") ?: "UTF-8"
    ): String? = file?.let {
        when {
            isExistsTimestamp(file) -> try {
                FileInputStream(file).use { fileInputStream ->
                    when {
                        isSpace(charsetName) -> InputStreamReader(fileInputStream)
                        else -> InputStreamReader(fileInputStream, charsetName)
                    }.use { inputStreamReader ->
                        BufferedReader(inputStreamReader).use { bufferedReader ->
                            StringBuilder().apply {
                                println("以行为单位读取文件内容，一次读一整行：")
                                var num = 1
                                while (true) {
                                    bufferedReader.readLine()?.let { line ->
                                        append("$line\r\n")//换行：windows为\r\n；Linux为\n。
                                        println("${num++}: $line")
                                    } ?: break
                                }
                                delete(length - 2, length)
                            }.toString()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            else -> null
        }
    }

    @JvmOverloads
    fun readFile2StringByBytes(
        filePath: String?, charsetName: String = System.getProperty("file.encoding") ?: "UTF-8"
    ): String? = filePath?.let { readFile2StringByBytes(getFileByPath(it), charsetName) }

    @JvmOverloads
    fun readFile2StringByBytes(
        file: File?, charsetName: String = System.getProperty("file.encoding") ?: "UTF-8"
    ): String? = readFile2BytesByStream(file)?.run {
        when {
            isSpace(charsetName) -> String(this)
            else -> try {
                String(this, charset(charsetName))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                null
            }
        }
    }

    fun readFile2BytesByStream(filePath: String?): ByteArray? =
        filePath?.let { readFile2BytesByStream(getFileByPath(it)) }

    fun readFile2BytesByStream(file: File?): ByteArray? = file?.let {
        when {
            isExistsTimestamp(file) -> try {
                FileInputStream(file).use { fileInputStream ->
                    ByteArrayOutputStream().use { byteArrayOutputStream ->
                        byteArrayOutputStream.apply {
                            ByteArray(8192).let { bytes ->
                                while (true) {
                                    if (fileInputStream.read(bytes) != -1) write(bytes) else break
                                }
                            }
                        }.toByteArray()
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                null
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
            else -> null
        }
    }

    fun readFile2BytesByChannel(filePath: String?): ByteArray? =
        filePath?.let { readFile2BytesByChannel(getFileByPath(it)) }

    fun readFile2BytesByChannel(file: File?): ByteArray? = when {
        isExistsTimestamp(file) -> try {
            RandomAccessFile(file, "r").use { randomAccessFile ->
                randomAccessFile.channel.use {
                    ByteBuffer.allocate(it.size().toInt()).apply {
                        while (true) {
                            if (it.read(this) <= 0) break
                        }
                    }.array()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
        else -> null
    }

    fun readFile2BytesByMap(filePath: String?): ByteArray? =
        filePath?.let { readFile2BytesByMap(getFileByPath(it)) }

    fun readFile2BytesByMap(file: File?): ByteArray? = when {
        isExistsTimestamp(file) -> try {
            RandomAccessFile(file, "r").use { randomAccessFile ->
                randomAccessFile.channel.use {
                    it.size().toInt().let { size ->
                        ByteArray(size).apply {
                            it.map(FileChannel.MapMode.READ_ONLY, 0, size.toLong())
                                .load().get(this, 0, size)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
        else -> null
    }
}