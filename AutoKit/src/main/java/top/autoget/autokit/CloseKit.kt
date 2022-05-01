package top.autoget.autokit

import java.io.Closeable
import java.io.IOException

object CloseKit {
    fun closeIo(vararg closeables: Closeable) {
        for (closeable in closeables) {
            try {
                closeable.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun closeIoQuietly(vararg closeables: Closeable) {
        for (closeable in closeables) {
            try {
                closeable.close()
            } catch (ignored: IOException) {
            }
        }
    }
}