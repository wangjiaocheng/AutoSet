package top.autoget.autokit

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

object CompressKit {
    fun compress(data: ByteArray): ByteArray = Deflater().apply {
        reset()
        setInput(data)
        finish()
    }.run {
        try {
            ByteArrayOutputStream(data.size).use { byteArrayOutputStream ->
                byteArrayOutputStream.apply {
                    ByteArray(1024).let { bytes ->
                        while (!finished()) {
                            write(bytes, 0, deflate(bytes))
                        }
                    }
                }.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            data
        } finally {
            end()
        }
    }

    fun compress(data: ByteArray, outputStream: OutputStream) = try {
        outputStream.use {
            DeflaterOutputStream(it).use { deflaterOutputStream ->
                deflaterOutputStream.run {
                    write(data, 0, data.size)
                    finish()
                    flush()
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    @JvmOverloads
    fun decompress(data: ByteArray, offset: Int = 0, length: Int = data.size): ByteArray =
        Inflater().apply {
            reset()
            setInput(data, offset, length)
        }.run {
            try {
                ByteArrayOutputStream(data.size).use { byteArrayOutputStream ->
                    byteArrayOutputStream.apply {
                        ByteArray(1024).let { bytes ->
                            while (!finished()) {
                                write(bytes, 0, inflate(bytes))
                            }
                        }
                    }.toByteArray()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                data
            } finally {
                end()
            }
        }

    fun decompress(inputStream: InputStream): ByteArray = try {
        inputStream.use {
            InflaterInputStream(it).use { inflaterInputStream ->
                ByteArray(1024).let { bytes ->
                    ByteArrayOutputStream(1024).use { byteArrayOutputStream ->
                        byteArrayOutputStream.apply {
                            while (true) {
                                if (inflaterInputStream.read(bytes) != -1) write(bytes) else break
                            }
                        }.toByteArray()
                    }
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        byteArrayOf()
    }
}