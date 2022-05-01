package top.autoget.autokit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.ImageKit.saveImage
import top.autoget.autokit.StringKit.isSpace
import java.io.File
import java.io.IOException

object PictureKit {
    fun getChoosedImagePath(activity: Activity, data: Intent?): String? = data?.data?.let { uri ->
        activity.contentResolver
            .query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)?.use { cursor ->
                try {
                    cursor.apply { moveToFirst() }.run {
                        getString(getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                            .let { path -> if (isSpace(path)) uri.path else path }
                    }
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                    null
                }
            }
    }

    fun getChoosedImage(activity: Activity, data: Intent?): Bitmap? = data?.data?.let { uri ->
        try {
            MediaStore.Images.Media.getBitmap(activity.contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getTakePictureFile(data: Intent?, filePath: String?): File? =
        getFileByPath(filePath)?.apply {
            data?.extras?.getParcelable<Bitmap>("data")
                ?.let { bitmap -> saveImage(bitmap, this, Bitmap.CompressFormat.JPEG) }
        }//拍照之后照片文件
}