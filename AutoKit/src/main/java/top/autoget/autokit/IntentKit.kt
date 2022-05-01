package top.autoget.autokit

import android.Manifest.permission.CALL_PHONE
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ApplicationKit.appPackageName
import top.autoget.autokit.ApplicationKit.isAppSystem
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.RomKit.isLeeco
import top.autoget.autokit.RomKit.isSamsung
import top.autoget.autokit.VersionKit.aboveGingerbread
import top.autoget.autokit.VersionKit.aboveKitKat
import top.autoget.autokit.VersionKit.aboveNougat
import java.io.File
import java.util.*

object IntentKit : LoggerKit {
    fun isIntentAvailable(intent: Intent): Boolean = app.packageManager
        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size > 0

    @JvmOverloads
    fun getInstallAppIntent(filePath: String, isNewTask: Boolean = false): Intent? =
        getInstallAppIntent(getFileByPath(filePath), isNewTask)

    @JvmOverloads
    fun getInstallAppIntent(file: File?, isNewTask: Boolean = false): Intent? = file?.let {
        Intent().apply {
            action = Intent.ACTION_VIEW
            if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (aboveNougat)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            data = (when {
                aboveNougat -> app.run {
                    FileProvider.getUriForFile(this, "$packageName.helper.provider", it)
                }
                else -> Uri.fromFile(it)
            }).apply {
                app.grantUriPermission(appPackageName, this, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                app.grantUriPermission(appPackageName, this, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            type = "application/vnd.android.package-archive"
        }
    }

    @JvmOverloads
    fun getUninstallAppIntent(packageName: String, isNewTask: Boolean = false): Intent =
        Intent().apply {
            action = Intent.ACTION_DELETE
            if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("package:$packageName")
        }

    @JvmOverloads
    fun getLaunchAppIntent(packageName: String, isNewTask: Boolean = false): Intent? =
        app.packageManager.getLaunchIntentForPackage(packageName)
            ?.apply { if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK }

    @JvmOverloads
    fun getAppDetailsSettingsIntent(
        packageName: String = appPackageName, isNewTask: Boolean = false
    ): Intent = Intent().apply {
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        when {
            aboveGingerbread -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:$packageName")
            }
            else -> {
                action = Intent.ACTION_VIEW
                putExtra("com.android.settings.ApplicationPkgName", packageName)
                component = ComponentName(
                    "com.android.settings", "com.android.settings.InstalledAppDetails"
                )
            }
        }//Uri.fromParts("package", packageName, null)
    }

    private const val GOOGLE_PLAY_APP_STORE_PACKAGE_NAME = "com.android.vending"

    @JvmOverloads
    fun getAppStoreIntent(
        isIncludeGooglePlayStore: Boolean = false, packageName: String = appPackageName
    ): Intent? {
        if (isSamsung) getAppStoreIntentSamsung(packageName)?.let { return it }
        if (isLeeco) getAppStoreIntentLeeco(packageName)?.let { return it }
        val intent = Intent().apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("market://details?id=$packageName")
        }
        app.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).let {
            when {
                it.isEmpty() -> return null.apply { error("$loggerTag->No app store!") }
                else -> {
                    var googleIntent: Intent? = null
                    for (resolveInfo in it) {
                        when (val pkgName = resolveInfo.activityInfo.packageName) {
                            GOOGLE_PLAY_APP_STORE_PACKAGE_NAME -> googleIntent =
                                intent.apply { setPackage(GOOGLE_PLAY_APP_STORE_PACKAGE_NAME) }
                            else -> if (isAppSystem(pkgName))
                                return intent.apply { setPackage(pkgName) }
                        }
                    }
                    return when {
                        isIncludeGooglePlayStore && googleIntent != null -> googleIntent
                        else -> intent.apply { setPackage(it[0].activityInfo.packageName) }
                    }
                }
            }
        }
    }

    private fun getAppStoreIntentSamsung(packageName: String): Intent? = Intent().apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        component = ComponentName(
            "com.sec.android.app.samsungapps", "com.sec.android.app.samsungapps.Main"
        )
        data =
            Uri.parse("http://www.samsungapps.com/appquery/appDetail.as?appId=$packageName")
    }.let { if (isIntentAvailable(it)) it else null }

    private fun getAppStoreIntentLeeco(packageName: String): Intent? = Intent().apply {
        action = "com.letv.app.appstore.appdetailactivity"
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        component = ComponentName(
            "com.letv.app.appstore", "com.letv.app.appstore.appmodule.details.DetailsActivity"
        )
        putExtra("packageName", packageName)
    }.let { if (isIntentAvailable(it)) it else null }

    @JvmOverloads
    fun getCaptureIntent(outUri: Uri, isNewTask: Boolean = false): Intent = Intent().apply {
        action = MediaStore.ACTION_IMAGE_CAPTURE
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(MediaStore.EXTRA_OUTPUT, outUri)
    }

    @JvmOverloads
    fun getPickIntentWithGallery(isNewTask: Boolean = false): Intent = Intent().apply {
        action = Intent.ACTION_PICK
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        type = "image*//*"
    }

    @JvmOverloads
    fun getPickIntentWithDocuments(isNewTask: Boolean = false): Intent = Intent().apply {
        action = Intent.ACTION_GET_CONTENT
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        type = "image*//*"
    }

    @JvmOverloads
    fun buildImageGetIntent(
        saveTo: Uri, outputX: Int, outputY: Int,
        returnData: Boolean, isNewTask: Boolean = false, aspectX: Int = 1, aspectY: Int = 1
    ): Intent = Intent().apply {
        action = if (aboveKitKat) Intent.ACTION_OPEN_DOCUMENT else Intent.ACTION_GET_CONTENT
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (aboveKitKat) addCategory(Intent.CATEGORY_OPENABLE)
        type = "image*//*"
        putExtra("scale", true)
        putExtra("aspectX", aspectX)
        putExtra("aspectY", aspectY)
        putExtra("outputX", outputX)
        putExtra("outputY", outputY)
        putExtra("output", saveTo)
        putExtra("outputFormat", Bitmap.CompressFormat.PNG)
        putExtra("return-data", returnData)
    }

    @JvmOverloads
    fun buildImageCropIntent(
        uriFrom: Uri, uriTo: Uri, outputX: Int, outputY: Int,
        returnData: Boolean, isNewTask: Boolean = false, aspectX: Int = 1, aspectY: Int = 1
    ): Intent = Intent().apply {
        action = "com.android.camera.action.CROP"
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        data = uriFrom
        type = "image*//*"
        putExtra("crop", true)
        putExtra("scale", true)
        putExtra("aspectX", aspectX)
        putExtra("aspectY", aspectY)
        putExtra("outputX", outputX)
        putExtra("outputY", outputY)
        putExtra("output", uriTo)
        putExtra("outputFormat", Bitmap.CompressFormat.PNG)
        putExtra("return-data", returnData)
    }

    @JvmOverloads
    fun getShareImageIntent(
        content: String, imagePath: String?, isNewTask: Boolean = false
    ): Intent? = when {
        imagePath == null || imagePath.isEmpty() -> null
        else -> getShareImageIntent(content, File(imagePath), isNewTask)
    }

    @JvmOverloads
    fun getShareImageIntent(content: String, image: File?, isNewTask: Boolean = false): Intent? =
        when {
            image == null || !image.isFile -> null
            else -> getShareImageIntent(content, file2Uri(image), isNewTask)
        }

    private fun file2Uri(file: File?): Uri? = file?.let {
        if (aboveNougat)
            app.run { FileProvider.getUriForFile(this, "$packageName.helper.provider", it) }
        else Uri.fromFile(it)
    }

    @JvmOverloads
    fun getShareImageIntent(content: String, uri: Uri?, isNewTask: Boolean = false): Intent =
        Intent().apply {
            action = Intent.ACTION_SEND
            if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            type = "image/*"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_STREAM, uri)
        }

    @JvmOverloads
    fun getShareImageIntent(
        content: String, imagePaths: LinkedList<String>?, isNewTask: Boolean = false
    ): Intent? = when {
        imagePaths == null || imagePaths.isEmpty() -> null
        else -> mutableListOf<File>().apply {
            for (imagePath in imagePaths) {
                add(File(imagePath))
            }
        }.let { getShareImageIntent(content, it, isNewTask) }
    }

    @JvmOverloads
    fun getShareImageIntent(
        content: String, images: MutableList<File>?, isNewTask: Boolean = false
    ): Intent? = when {
        images == null || images.isEmpty() -> null
        else -> arrayListOf<Uri>().apply {
            for (image in images) {
                if (image.isFile) file2Uri(image)?.let { add(it) }
            }
        }.let { getShareImageIntent(content, it, isNewTask) }
    }

    @JvmOverloads
    fun getShareImageIntent(
        content: String, uris: ArrayList<Uri>, isNewTask: Boolean = false
    ): Intent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        type = "image/*"
        putExtra(Intent.EXTRA_TEXT, content)
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
    }

    @JvmOverloads
    fun getShareTextIntent(content: String, isNewTask: Boolean = false): Intent = Intent().apply {
        action = Intent.ACTION_SEND
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, content)
    }

    @JvmOverloads
    fun getDialIntent(phoneNumber: String, isNewTask: Boolean = false): Intent = Intent().apply {
        action = Intent.ACTION_DIAL
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        data = Uri.parse("tel:$phoneNumber")
    }

    @RequiresPermission(CALL_PHONE)
    @JvmOverloads
    fun getCallIntent(phoneNumber: String, isNewTask: Boolean = false): Intent = Intent().apply {
        action = Intent.ACTION_CALL
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        data = Uri.parse("tel:$phoneNumber")
    }

    @JvmOverloads
    fun getSendSmsIntent(phoneNumber: String, content: String, isNewTask: Boolean = false): Intent =
        Intent().apply {
            action = Intent.ACTION_SENDTO
            if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("smsto:$phoneNumber")
            putExtra("sms_body", content)
        }

    val shutdownIntent: Intent
        get() = getShutdownIntent(false)

    fun getShutdownIntent(isNewTask: Boolean): Intent = Intent().apply {
        action = "android.intent.action.ACTION_REQUEST_SHUTDOWN"
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra("android.intent.extra.KEY_CONFIRM", false)
    }

    @JvmOverloads
    fun getComponentIntent(
        packageName: String, className: String, isNewTask: Boolean = false, bundle: Bundle? = null
    ): Intent = Intent().apply {
        action = Intent.ACTION_VIEW
        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
        component = ComponentName(packageName, className)
        bundle?.let { putExtras(it) }
    }
}