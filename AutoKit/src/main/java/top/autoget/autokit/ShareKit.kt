package top.autoget.autokit

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.ToastKit.showShort
import top.autoget.autokit.UriKit.file2Uri
import java.io.File

object ShareKit {
    fun shareMultiplePicture(activity: Activity, vararg filePaths: String?) =
        shareMultiplePicture(activity, mutableListOf(*filePaths))

    fun shareMultiplePicture(activity: Activity, filePaths: MutableList<String?>) {
        if (!shareMultiplePictureForResult(activity, filePaths)) showShort("未找到可进行分享的应用！")
    }

    fun shareMultiplePictureForResult(activity: Activity, vararg filePaths: String?): Boolean =
        shareMultiplePictureForResult(activity, mutableListOf(*filePaths))

    fun shareMultiplePictureForResult(
        activity: Activity, filePaths: MutableList<String?>
    ): Boolean = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, getMediaUrisFromPaths(filePaths))
    }.let { ActivityKit.startActivity(activity, Intent.createChooser(it, "分享到")) }//分享多图片

    fun shareMultiplePictureToWeChatCircle(
        activity: Activity, description: String?, vararg filePaths: String?
    ) = shareMultiplePictureToWeChatCircle(activity, description, mutableListOf(*filePaths))

    fun shareMultiplePictureToWeChatCircle(
        activity: Activity, description: String?, filePaths: MutableList<String?>
    ) {
        if (!shareMultiplePictureToWeChatCircleForResult(activity, description, filePaths))
            showShort("当前设备未安装微信，无法进行微信分享！")
    }

    fun shareMultiplePictureToWeChatCircleForResult(
        activity: Activity, description: String?, vararg filePaths: String?
    ): Boolean = shareMultiplePictureToWeChatCircleForResult(
        activity, description, mutableListOf(*filePaths)
    )

    fun shareMultiplePictureToWeChatCircleForResult(
        activity: Activity, description: String?, filePaths: MutableList<String?>
    ): Boolean = Intent().apply {
        component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
        action = Intent.ACTION_SEND_MULTIPLE
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, getMediaUrisFromPaths(filePaths))
        putExtra("Kdescription", description)
    }.let { ActivityKit.startActivity(activity, Intent.createChooser(it, "分享到")) }//分享多图片到朋友圈

    fun shareMultiplePictureToWeChatContacts(
        activity: Activity, description: String?, vararg filePaths: String?
    ) = shareMultiplePictureToWeChatContacts(activity, description, mutableListOf(*filePaths))

    fun shareMultiplePictureToWeChatContacts(
        activity: Activity, description: String?, filePaths: MutableList<String?>
    ) {
        if (!shareMultiplePictureToWeChatContactsForResult(activity, description, filePaths))
            showShort("当前设备未安装微信，无法进行微信分享！")
    }

    fun shareMultiplePictureToWeChatContactsForResult(
        activity: Activity, description: String?, vararg filePaths: String?
    ): Boolean = shareMultiplePictureToWeChatContactsForResult(
        activity, description, mutableListOf(*filePaths)
    )

    fun shareMultiplePictureToWeChatContactsForResult(
        activity: Activity, description: String?, filePaths: MutableList<String?>
    ): Boolean = Intent().apply {
        component = ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
        action = Intent.ACTION_SEND_MULTIPLE
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, getMediaUrisFromPaths(filePaths))
        putExtra("Kdescription", description)
    }.let { ActivityKit.startActivity(activity, Intent.createChooser(it, "分享到")) }//分享多图片到联系人

    private fun getMediaUrisFromPaths(filePaths: MutableList<String?>): ArrayList<Uri?> =
        ArrayList<Uri?>().apply {
            for (filePath in filePaths) {
                getFileByPath(filePath)?.let { add(file2Uri(it)) }
            }
        }

    enum class ShareType { DEFAULT, WE_CHAT_CONTACTS, WE_CHAT_CIRCLE }

    fun sharePicture(activity: Activity, imgUri: Uri?, shareType: ShareType?) = when (shareType) {
        ShareType.DEFAULT -> sharePicture(activity, imgUri)//分享单图片含GIF
        ShareType.WE_CHAT_CIRCLE -> sharePictureToWeChatCircle(activity, imgUri)//分享单图片到朋友圈
        ShareType.WE_CHAT_CONTACTS -> sharePictureToWeChatContacts(activity, imgUri)//分享单图片到联系人
        else -> {
        }
    }

    fun sharePicture(activity: Activity, picture: File) =
        shareFile(activity, null, picture, "image/*", "分享图片")

    fun sharePicture(activity: Activity, pictureUri: Uri?) =
        shareFile(activity, null, pictureUri, "image/*", "分享图片")

    fun sharePictureToWeChatCircle(activity: Activity, imgUri: Uri?) =
        ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
            .run { shareFile(activity, this, imgUri, "image/*", "分享图片") }

    fun sharePictureToWeChatContacts(activity: Activity, imgUri: Uri?) =
        ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            .run { shareFile(activity, this, imgUri, "image/*", "分享图片") }

    fun sharePictureForResult(activity: Activity, imgUri: Uri?, shareType: ShareType?): Boolean =
        when (shareType) {
            ShareType.DEFAULT -> sharePictureForResult(activity, imgUri)
            ShareType.WE_CHAT_CIRCLE -> sharePictureToWeChatCircleForResult(activity, imgUri)
            ShareType.WE_CHAT_CONTACTS -> sharePictureToWeChatContactsForResult(activity, imgUri)
            else -> false
        }//分享单图片含GIF、分享单图片到朋友圈、分享单图片到联系人

    fun sharePictureForResult(activity: Activity, picture: File): Boolean =
        shareFileForResult(activity, null, picture, "image/*", "分享图片")

    fun sharePictureForResult(activity: Activity, pictureUri: Uri?): Boolean =
        shareFileForResult(activity, null, pictureUri, "image/*", "分享图片")

    fun sharePictureToWeChatCircleForResult(activity: Activity, imgUri: Uri?): Boolean =
        ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
            .run { shareFileForResult(activity, this, imgUri, "image/*", "分享图片") }

    fun sharePictureToWeChatContactsForResult(activity: Activity, imgUri: Uri?): Boolean =
        ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            .run { shareFileForResult(activity, this, imgUri, "image/*", "分享图片") }

    fun shareVideo(activity: Activity, videoUri: Uri?, shareType: ShareType?) = when (shareType) {
        ShareType.DEFAULT -> shareVideo(activity, videoUri)//分享视频
        ShareType.WE_CHAT_CIRCLE -> showShort("微信朋友圈只支持分享图片！")
        ShareType.WE_CHAT_CONTACTS -> shareVideoToWeChatContacts(activity, videoUri)//分享视频到联系人
        else -> {
        }
    }

    fun shareVideo(activity: Activity, videoFile: File) =
        shareFile(activity, null, videoFile, "video/*", "分享视频")

    fun shareVideo(activity: Activity, videoUri: Uri?) =
        shareFile(activity, null, videoUri, "video/*", "分享视频")

    fun shareVideoToWeChatContacts(activity: Activity, videoUri: Uri?) =
        ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            .run { shareFile(activity, this, videoUri, "video/*", "分享视频") }

    fun shareVideoForResult(activity: Activity, videoUri: Uri?, shareType: ShareType?): Boolean =
        when (shareType) {
            ShareType.DEFAULT -> shareVideoForResult(activity, videoUri)
            ShareType.WE_CHAT_CIRCLE -> false.apply { showShort("微信朋友圈只支持分享图片！") }
            ShareType.WE_CHAT_CONTACTS -> shareVideoToWeChatContactsForResult(activity, videoUri)
            else -> false
        }//分享视频、分享视频到联系人

    fun shareVideoForResult(activity: Activity, videoFile: File): Boolean =
        shareFileForResult(activity, null, videoFile, "video/*", "分享视频")

    fun shareVideoForResult(activity: Activity, videoUri: Uri?): Boolean =
        shareFileForResult(activity, null, videoUri, "video/*", "分享视频")

    fun shareVideoToWeChatContactsForResult(activity: Activity, videoUri: Uri?): Boolean =
        ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            .run { shareFileForResult(activity, this, videoUri, "video/*", "分享视频") }

    fun shareFile(activity: Activity, fileUri: Uri?, shareType: ShareType?) = when (shareType) {
        ShareType.DEFAULT -> shareFile(activity, fileUri)//分享单个文件
        ShareType.WE_CHAT_CIRCLE -> showShort("微信朋友圈只支持分享图片！")
        ShareType.WE_CHAT_CONTACTS -> shareFileToWeChatContacts(activity, fileUri)//分享单个文件到联系人
        else -> {
        }
    }

    fun shareFile(activity: Activity, fileUri: Uri?) =
        shareFile(activity, null, fileUri, "*/*", "分享文件")

    fun shareFile(
        activity: Activity, componentName: ComponentName?,
        fileToShare: File, mimeTypeForFile: String, subjectTextToShare: String
    ) = shareFile(
        activity, componentName, file2Uri(fileToShare), mimeTypeForFile, subjectTextToShare
    )

    fun shareFile(
        activity: Activity, componentName: ComponentName?,
        fileUri: Uri?, mimeTypeForFile: String, subjectTextToShare: String
    ) {
        if (!shareFileForResult(
                activity, componentName, fileUri, mimeTypeForFile, subjectTextToShare
            )
        ) showShort("未找到可进行分享的应用！")
    }

    fun shareFileToWeChatContacts(activity: Activity, fileUri: Uri?) =
        ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            .run { shareFile(activity, this, fileUri, "*/*", "分享文件") }

    fun shareFileForResult(activity: Activity, fileUri: Uri?, shareType: ShareType?): Boolean =
        when (shareType) {
            ShareType.DEFAULT -> shareFileForResult(activity, fileUri)
            ShareType.WE_CHAT_CIRCLE -> false.apply { showShort("微信朋友圈只支持分享图片！") }
            ShareType.WE_CHAT_CONTACTS -> shareFileToWeChatContactsForResult(activity, fileUri)
            else -> false
        }//分享单个文件、分享单个文件到联系人

    fun shareFileForResult(activity: Activity, fileUri: Uri?): Boolean =
        shareFileForResult(activity, null, fileUri, "*/*", "分享文件")

    fun shareFileForResult(
        activity: Activity, componentName: ComponentName?,
        fileToShare: File, mimeTypeForFile: String, subjectTextToShare: String
    ): Boolean = shareFileForResult(
        activity, componentName, file2Uri(fileToShare), mimeTypeForFile, subjectTextToShare
    )

    fun shareFileForResult(
        activity: Activity, componentName: ComponentName?,
        fileUri: Uri?, mimeTypeForFile: String, subjectTextToShare: String
    ): Boolean = Intent().apply {
        componentName?.let { component = it }
        action = Intent.ACTION_SEND
        type = mimeTypeForFile
        putExtra(Intent.EXTRA_SUBJECT, subjectTextToShare)
        putExtra(Intent.EXTRA_STREAM, fileUri)
    }.let { ActivityKit.startActivity(activity, Intent.createChooser(it, "分享到")) }

    fun shareFileToWeChatContactsForResult(activity: Activity, fileUri: Uri?): Boolean =
        ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            .run { shareFileForResult(activity, this, fileUri, "*/*", "分享文件") }
}