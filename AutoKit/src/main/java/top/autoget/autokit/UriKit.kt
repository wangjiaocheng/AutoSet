package top.autoget.autokit

import android.content.ContentResolver
import android.content.ContentUris
import android.content.CursorLoader
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.content.FileProvider
import top.autoget.autokit.AKit.app
import top.autoget.autokit.PathKit.pathExternal
import top.autoget.autokit.VersionKit.aboveKitKat
import java.io.File

object UriKit : LoggerKit {
    fun getURLWithParams(url: String, params: MutableMap<String, String>): String =
        "$url?${joinParam(params)}"

    private fun joinParam(params: MutableMap<String, String>): StringBuffer = StringBuffer().apply {
        for ((key, value) in params) {
            append("$key=$value&")
        }
        delete(length - 1, length)
    }

    fun file2Uri(file: File): Uri = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
            app.run { FileProvider.getUriForFile(this, "$packageName.helper.provider", file) }
        else -> Uri.fromFile(file)
    }

    fun uri2File(uri: Uri): File? = when {
        aboveKitKat &&
                DocumentsContract.isDocumentUri(app, uri) -> when (uri.authority) {
            "com.android.externalstorage.documents" -> DocumentsContract.getDocumentId(uri)
                .split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().let { strings ->
                    when {
                        "primary".equals(strings[0], true) ->
                            File("$pathExternal${strings[1]}")
                        else -> {
                            debug("$loggerTag->$uri parse failed 2")
                            null
                        }
                    }
                }
            "com.android.providers.media.documents" -> DocumentsContract.getDocumentId(uri)
                .split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().let { strings ->
                    when (strings[0]) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> {
                            debug("$loggerTag->$uri parse failed 4")
                            return null
                        }
                    }?.let { getFileFromUri(it, 5, "_id=?", arrayOf(strings[1])) }
                }
            "com.android.providers.downloads.documents" -> ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                DocumentsContract.getDocumentId(uri).toLong()
            ).let { getFileFromUri(it, 3) }
            else -> {
                debug("$loggerTag->$uri parse failed 6")
                null
            }
        }
        uri.scheme == ContentResolver.SCHEME_CONTENT -> getFileFromUri(uri, 1)
        uri.scheme == ContentResolver.SCHEME_FILE -> uri.path?.let { File(it) } ?: run {
            debug("$loggerTag->$uri parse failed 0")
            null
        }
        else -> {
            debug("$loggerTag->$uri parse failed 7")
            null
        }
    }

    private fun getFileFromUri(
        uri: Uri, code: Int, selection: String? = null, selectionArgs: Array<String>? = null
    ): File? = try {
        CursorLoader(app).apply {
            this.uri = uri
            projection = arrayOf("_data")
        }.loadInBackground().apply { moveToFirst() }
            .run { File(getString(getColumnIndexOrThrow("_data"))) }
    } catch (e: Exception) {
        debug("$loggerTag->$uri parse failed $code")
        null
    }
}