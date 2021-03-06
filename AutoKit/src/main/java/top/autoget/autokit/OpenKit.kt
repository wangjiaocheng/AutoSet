package top.autoget.autokit

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import top.autoget.autokit.AKit.app
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.FileKit.isExistsFile
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.ToastKit.showShort
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL

object OpenKit {
    fun openInputStreamByUrlString(urlStr: String?): InputStream? = try {
        if (isSpace(urlStr)) null else URL(urlStr).openConnection().getInputStream()
    } catch (e: MalformedURLException) {
        e.printStackTrace()
        null
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    fun openWebSite(strSiteUrl: String?) = app.startActivity(Intent().apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse(strSiteUrl)
    })

    fun openImage(filePath: String?) = app.startActivity(Intent().apply {
        action = Intent.ACTION_VIEW
        data = filePath?.let { Uri.fromFile(getFileByPath(filePath)) }
        type = "image/*"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addCategory(Intent.CATEGORY_DEFAULT)
    })

    fun openVideo(filePath: String?) = app.startActivity(Intent().apply {
        action = Intent.ACTION_VIEW
        data = filePath?.let { Uri.fromFile(getFileByPath(filePath)) }
        type = "video/*"
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra("oneshot", 0)
        putExtra("configchange", 0)
    })

    fun openPdfFile(filePath: String?) = try {
        getFileByPath(filePath)?.let { file ->
            when {
                isExistsFile(file) -> app.startActivity(Intent().apply {
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    data = Uri.fromFile(file)
                    type = "application/pdf"
                })
                else -> showShort("?????????????????????$filePath")
            }
        } ?: showShort("?????????????????????$filePath")
    } catch (e: Exception) {
        showShort("?????????????????????PDF??????????????????")
    }

    fun openWordFile(filePath: String?) = try {
        getFileByPath(filePath)?.let { file ->
            when {
                isExistsFile(file) -> app.startActivity(Intent().apply {
                    action = Intent.ACTION_VIEW
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.fromFile(file)
                    type = "application/msword"
                })
                else -> showShort("?????????????????????$filePath")
            }
        } ?: showShort("?????????????????????$filePath")
    } catch (e: Exception) {
        showShort("?????????????????????Word??????????????????")
    }

    fun openOfficeFileByWps(filePath: String?) = try {
        getFileByPath(filePath)?.let { file ->
            when {
                isExistsFile(file) -> app.startActivity(Intent().apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = Uri.fromFile(file)
                    component = ComponentName(
                        "cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity2"
                    )
                })
                else -> showShort("?????????????????????$filePath")
            }
        } ?: showShort("?????????????????????$filePath")
    } catch (e: ActivityNotFoundException) {
        showShort("???????????????WPS")
    } catch (e: Exception) {
        showShort("??????????????????")
    }
}