package top.autoget.autokit

import android.Manifest.permission.CALL_PHONE
import android.Manifest.permission.SEND_SMS
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.SystemClock
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.util.Xml
import androidx.annotation.RequiresPermission
import top.autoget.autokit.AKit.app
import top.autoget.autokit.IntentKit.isIntentAvailable
import top.autoget.autokit.PathKit.pathExternal
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.ToastKit.showShort
import java.io.File
import java.io.FileOutputStream

object ContactsKit {
    @RequiresPermission(CALL_PHONE)
    fun call(phoneNum: String): Boolean = Intent().apply {
        action = Intent.ACTION_CALL
        data = Uri.parse("tel:${phoneNum.trim { it <= ' ' }}")
        if (isIntentAvailable(this)) flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }.let { if (isIntentAvailable(it)) true.apply { app.startActivity(it) } else false }//直接呼叫

    fun dial(phoneNum: String): Boolean = Intent().apply {
        action = Intent.ACTION_DIAL
        data = Uri.parse("tel:${phoneNum.trim { it <= ' ' }}")
        if (isIntentAvailable(this)) flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }.let { if (isIntentAvailable(it)) true.apply { app.startActivity(it) } else false }//拨号界面

    @RequiresPermission(SEND_SMS)
    fun sendSmsActivity(phoneNum: String, content: String?): Boolean = Intent().apply {
        action = Intent.ACTION_SENDTO//Intent.ACTION_VIEW
        data = Uri.parse("smsto:${phoneNum.trim { it <= ' ' }}")
        if (isIntentAvailable(this)) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("sms_body", if (isNotSpace(content)) content else "")
        }
    }.let { if (isIntentAvailable(it)) true.apply { app.startActivity(it) } else false }

    @RequiresPermission(SEND_SMS)
    fun sendSmsSilent(phoneNum: String, content: String?) {
        if (isNotSpace(content)) SmsManager.getDefault().run {
            PendingIntent.getBroadcast(app, 0, Intent("send"), 0).let { sentIntent ->
                phoneNum.trim { it <= ' ' }
                when {
                    (content?.length ?: 0) < 70 ->
                        sendTextMessage(phoneNum, null, content, sentIntent, null)
                    else -> for (str in divideMessage(content)) {
                        sendTextMessage(phoneNum, null, str, sentIntent, null)
                    }
                }
            }
        }
    }

    @RequiresPermission(SEND_SMS)
    fun sendSmsWithReceiver(phoneNum: String, content: String?) = app.run {
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(_context: Context, _intent: Intent) {
                when (resultCode) {
                    Activity.RESULT_OK -> showShort("短信发送成功")
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> {
                    }
                    SmsManager.RESULT_ERROR_RADIO_OFF -> {
                    }
                    SmsManager.RESULT_ERROR_NULL_PDU -> {
                    }
                }
            }
        }, IntentFilter("SENT_SMS_ACTION"))
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(_context: Context, _intent: Intent) {
                showShort("${phoneNum}短信接收成功")
            }
        }, IntentFilter("DELIVERED_SMS_ACTION"))
        if (isNotSpace(content)) SmsManager.getDefault().let { smsManager ->
            PendingIntent.getBroadcast(this, 0, Intent("SENT_SMS_ACTION"), 0).let { sendIntent ->
                PendingIntent.getBroadcast(this, 0, Intent("DELIVERED_SMS_ACTION"), 0)
                    .let { backIntent ->
                        phoneNum.trim { it <= ' ' }
                        for (text in smsManager.divideMessage(content)) {
                            smsManager.sendTextMessage(phoneNum, null, text, sendIntent, backIntent)
                        }
                    }
            }
        }
    }//发送短信，监听发送接收状态

    val allSms2Xml: File?
        get() = Xml.newSerializer().run {
            try {
                File("${pathExternal}backupsms.xml").let { file ->
                    FileOutputStream(file).use { setOutput(it, "utf-8") }
                    startDocument("utf-8", true)//独立保存
                    startTag(null, "smss")
                    app.contentResolver.query(
                        Uri.parse("content://sms"), arrayOf("address", "date", "type", "body"),
                        null, null, null
                    )?.use { cursor ->
                        println(cursor.count)
                        while (cursor.moveToNext()) {
                            SystemClock.sleep(1000)
                            startTag(null, "sms")
                            startTag(null, "address")
                            text(cursor.getString(0))
                            endTag(null, "address")
                            startTag(null, "date")
                            text(cursor.getString(1))
                            endTag(null, "date")
                            startTag(null, "type")
                            text(cursor.getString(2))
                            endTag(null, "type")
                            startTag(null, "body")
                            text(cursor.getString(3))
                            endTag(null, "body")
                            endTag(null, "sms")
                        }
                    }
                    endTag(null, "smss")
                    endDocument()
                    flush()
                    file
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /*    override protected fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
            super.onActivityResult(requestCode, resultCode, data)
            var num: String? = null
            getContentResolver().query(data.data, null, null, null, null).use { cursor ->
                cursor.run {
                    while (moveToNext()) {
                        num = getString(getColumnIndex("data1"))
                    }
                }
            }
            num = num?.replace("-", "")
        }*/
    fun toContantNumberActivity(activity: Activity) =
        activity.startActivityForResult(Intent().apply {
            action = Intent.ACTION_PICK
            type = "vnd.android.cursor.dir/phone_v2"
        }, 0)//联系人选择界面，点击联系人获取号码

    fun toContactsChooseActivity(activity: Activity, requestCode: Int) =
        activity.startActivityForResult(Intent().apply {
            action = Intent.ACTION_PICK
            data = ContactsContract.Contacts.CONTENT_URI
        }, requestCode)//联系人选择界面，点击联系人批量获取

    fun getContantNumberChoosed(activity: Activity, resultCode: Int, intent: Intent): String {
        if (Activity.RESULT_OK == resultCode)
            activity.managedQuery(intent.data, null, null, null, null).use { cursor ->
                cursor.apply { moveToFirst() }.run {
                    if (getInt(getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                        activity.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ${
                                getString(
                                    getColumnIndex(ContactsContract.Contacts._ID)
                                )
                            }", null, null
                        )?.use { phones ->
                            if (phones.moveToFirst()) while (!phones.isAfterLast) {
                                if (phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)) == 2)
                                    return phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                phones.moveToNext()
                            }
                        }
                }
            }
        return ""
    }

    val contacts: MutableList<MutableMap<String, String>>
        get() = mutableListOf<MutableMap<String, String>>().apply {
            SystemClock.sleep(3000)
            app.contentResolver.let { resolver ->
                resolver.query(
                    Uri.parse("content://com.android.contacts/raw_contacts"),
                    arrayOf("contact_id"), null, null, null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        cursor.getString(0).let { contactId ->
                            if (isNotSpace(contactId)) resolver.query(
                                Uri.parse("content://com.android.contacts/data"),
                                arrayOf("data1", "mimetype"), "raw_contact_id=?",
                                arrayOf(contactId), null
                            )?.use { phones ->
                                mutableMapOf<String, String>().let { mutableMap ->
                                    while (phones.moveToNext()) {
                                        phones.getString(0).let {
                                            when (phones.getString(1)) {
                                                "vnd.android.cursor.item/name" ->
                                                    mutableMap["name"] = it
                                                "vnd.android.cursor.item/phone_v2" ->
                                                    mutableMap["phone"] = it
                                            }
                                        }
                                    }
                                    add(mutableMap)
                                }
                            }
                        }
                    }
                }
            }
        }
}