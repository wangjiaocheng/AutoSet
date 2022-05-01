package top.autoget.autokit

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import top.autoget.autokit.AKit.app
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.SdKit.isSdCardEnable
import java.text.SimpleDateFormat
import java.util.*

object PhotoKit : LoggerKit {
    private const val PHOTO_REQUEST_GALLERY = 5001
    fun openGalleryMedia(activity: Activity) = activity.startActivityForResult(Intent().apply {
        action = Intent.ACTION_PICK
        data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }, PHOTO_REQUEST_GALLERY)

    fun openGalleryMedia(fragment: Fragment) = fragment.startActivityForResult(Intent().apply {
        action = Intent.ACTION_PICK
        data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }, PHOTO_REQUEST_GALLERY)

    fun openGalleryImage(activity: Activity) = activity.startActivityForResult(Intent().apply {
        action = Intent.ACTION_GET_CONTENT
        type = "image/*"
    }, PHOTO_REQUEST_GALLERY)

    fun openGalleryImage(fragment: Fragment) = fragment.startActivityForResult(Intent().apply {
        action = Intent.ACTION_GET_CONTENT
        type = "image/*"
    }, PHOTO_REQUEST_GALLERY)

    private const val PHOTO_REQUEST_CAMERA = 5002

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    fun openCameraImage(activity: Activity) = activity.startActivityForResult(
        getCameraIntent(createImageOutputUri(activity)), PHOTO_REQUEST_CAMERA
    )

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    fun openCameraImage(fragment: Fragment) = fragment.startActivityForResult(
        getCameraIntent(createImageOutputUri(fragment.context)), PHOTO_REQUEST_CAMERA
    )

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    private fun getCameraIntent(outputUri: Uri = createImageOutputUri(app)): Intent =
        Intent().apply {
            action = MediaStore.ACTION_IMAGE_CAPTURE
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
        }//MediaStore.EXTRA_OUTPUT不设置时，自动生成uri，只返回缩略图，onActivityResult中intent.data.extras.get("data")获取

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    private fun createImageOutputUri(context: Context?): Uri = context?.let {
        nowMillis.let { time ->
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date(time)).let { name ->
                ContentValues(3).apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.DATE_TAKEN, time)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }.let { values ->
                    when {
                        isSdCardEnable -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        else -> MediaStore.Images.Media.INTERNAL_CONTENT_URI
                    }.let { uri ->
                        it.contentResolver.insert(uri, values)
                            .apply { info("$loggerTag->生成照片输出路径：${this}") }
                    }
                }
            }
        }
    } ?: Uri.parse("").apply { info("$loggerTag->生成照片输出路径：${this}") }

    private const val PHOTO_REQUEST_CUT = 5003

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmOverloads
    fun cropImage(
        activity: Activity, inputUri: Uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI
    ) = getCropIntent(createImageOutputUri(activity)).apply { data = inputUri }
        .let { activity.startActivityForResult(it, PHOTO_REQUEST_CUT) }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmOverloads
    fun cropImage(
        fragment: Fragment, inputUri: Uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI
    ) = getCropIntent(createImageOutputUri(fragment.context)).apply { data = inputUri }
        .let { fragment.startActivityForResult(it, PHOTO_REQUEST_CUT) }

    /*
    比例宽高都不设置，裁剪框可自行调整（比例大小都可随意调整）；
    只设置裁剪框比例aspect，裁剪框比例固定只能调整大小；
    裁剪后图片宽高output的设置与裁剪框无关，只决定最终生成图片大小；
    裁剪框宽高比例aspect可与裁剪后生成图片比例output不同，以裁剪框宽为准，按裁剪宽高比例生成图片，与框选部分可能不同，可能截取框选部分，可能超出框选部分，向下延伸补足
    */
    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    @JvmOverloads
    fun getCropIntent(
        outputUri: Uri = createImageOutputUri(app), outputX: Int = 300, outputY: Int = 300,
        inputUri: Uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI,
        aspectX: Int = 1, aspectY: Int = 1, isScale: Boolean = true
    ): Intent = Intent().apply {
        action = "com.android.camera.action.CROP"
        data = inputUri
        type = "image/*"
        putExtra("crop", true)
        putExtra("scale", isScale)//true去黑边
        putExtra("scaleUpIfNeeded", true)//剪裁不足黑边解决
        putExtra("aspectX", if (aspectX > 0) aspectX else 1)
        putExtra("aspectY", if (aspectY > 0) aspectY else 1)
        putExtra("outputX", outputX)
        putExtra("outputY", outputY)
        putExtra("outputFormat", Bitmap.CompressFormat.JPEG)//截大图用Uri不用Bitmap，截小图用Uri或用Bitmap
        putExtra("noFaceDetection", true)
        putExtra("return-data", false)//返回Bitmap大图裁剪存在问题，指定MediaStore.EXTRA_OUTPUT保存Uri
        putExtra(MediaStore.EXTRA_OUTPUT, outputUri)//读取文件路径和裁剪写入路径区分，否则造成文件0byte
    }
}