package top.autoget.autokit

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.provider.Settings
import android.view.MotionEvent
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.annotation.StringDef
import androidx.core.app.ActivityCompat
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ApplicationKit.appPackageName
import top.autoget.autokit.IntentKit.isIntentAvailable
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.VersionKit.aboveMarshmallow
import top.autoget.autokit.VersionKit.aboveOreo

object PermissionKit {
    fun builderSimple(activity: Activity): BuilderSimple = BuilderSimple(activity)
    class BuilderSimple(private val activity: Activity) {
        private val permissionList: MutableList<String> = mutableListOf()
        fun addPermission(permission: String): BuilderSimple = apply {
            if (!permissionList.contains(permission)) permissionList.add(permission)
        }

        fun initPermission(): MutableList<String> = mutableListOf<String>().apply {
            for (permission in permissionList) {
                if (ActivityCompat.checkSelfPermission(activity, permission) !=
                    PackageManager.PERMISSION_GRANTED
                ) add(permission)
            }
            if (size > 0) ActivityCompat.requestPermissions(activity, toTypedArray(), 1)
        }
    }

    fun builderPermissions(vararg permissions: String): BuilderPermissions =
        BuilderPermissions.permission(*permissions)

    object PermissionConstants {
        const val PHONE: String = Manifest.permission_group.PHONE//电话
        const val SENSORS: String = Manifest.permission_group.SENSORS//传感器
        const val LOCATION: String = Manifest.permission_group.LOCATION//位置
        const val MICROPHONE: String = Manifest.permission_group.MICROPHONE//麦克风
        const val CAMERA: String = Manifest.permission_group.CAMERA//相机
        const val STORAGE: String = Manifest.permission_group.STORAGE//存储
        const val CALENDAR: String = Manifest.permission_group.CALENDAR//日历
        const val CONTACTS: String = Manifest.permission_group.CONTACTS//联系人
        const val SMS: String = Manifest.permission_group.SMS//短信

        @StringDef(PHONE, SENSORS, LOCATION, MICROPHONE, CAMERA, STORAGE, CALENDAR, CONTACTS, SMS)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Permission

        private val GROUP_PHONE_BELOW_O: Array<String> = arrayOf(
            READ_PHONE_STATE,
            READ_PHONE_NUMBERS,
            READ_CALL_LOG,
            WRITE_CALL_LOG,
            CALL_PHONE,
            PROCESS_OUTGOING_CALLS,
            ADD_VOICEMAIL,
            USE_SIP
        )
        private val GROUP_PHONE: Array<String> = arrayOf(
            READ_PHONE_STATE,
            READ_PHONE_NUMBERS,
            READ_CALL_LOG,
            WRITE_CALL_LOG,
            CALL_PHONE,
            ANSWER_PHONE_CALLS,
            PROCESS_OUTGOING_CALLS,
            ADD_VOICEMAIL,
            USE_SIP
        )
        private val GROUP_SENSORS: Array<String> = arrayOf(BODY_SENSORS)
        private val GROUP_LOCATION: Array<String> =
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)
        private val GROUP_MICROPHONE: Array<String> = arrayOf(RECORD_AUDIO)
        private val GROUP_CAMERA: Array<String> = arrayOf(CAMERA)
        private val GROUP_STORAGE: Array<String> =
            arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE)
        private val GROUP_CALENDAR: Array<String> =
            arrayOf(READ_CALENDAR, WRITE_CALENDAR)
        private val GROUP_CONTACTS: Array<String> =
            arrayOf(READ_CONTACTS, GET_ACCOUNTS, WRITE_CONTACTS)
        private val GROUP_SMS: Array<String> = arrayOf(
            READ_SMS,
            SEND_SMS,
            RECEIVE_SMS,
            RECEIVE_MMS,
            RECEIVE_WAP_PUSH
        )

        fun getPermissions(@Permission permission: String): Array<String> = when (permission) {
            PHONE -> if (aboveOreo) GROUP_PHONE else GROUP_PHONE_BELOW_O
            SENSORS -> GROUP_SENSORS
            LOCATION -> GROUP_LOCATION
            MICROPHONE -> GROUP_MICROPHONE
            CAMERA -> GROUP_CAMERA
            STORAGE -> GROUP_STORAGE
            CALENDAR -> GROUP_CALENDAR
            CONTACTS -> GROUP_CONTACTS
            SMS -> GROUP_SMS
            else -> arrayOf(permission)
        }
    }

    class BuilderPermissions(vararg permissions: String) {
        private val permissionsSet: MutableSet<String> = mutableSetOf()

        init {
            for (permission in permissions) {
                for (permission0 in PermissionConstants.getPermissions(permission)) {
                    if (PERMISSIONS.contains(permission0)) permissionsSet.add(permission0)
                }
            }
            instance = this
        }

        private var permissionsGranted: MutableList<String>? = null
        private var permissionsRequest: MutableList<String>? = null
        fun request() {
            permissionsGranted = mutableListOf()
            permissionsRequest = mutableListOf()
            when {
                aboveMarshmallow -> {
                    for (permission in permissionsSet) {
                        when {
                            isGranted(permission) -> permissionsGranted?.add(permission)
                            else -> permissionsRequest?.add(permission)
                        }
                    }
                    when {
                        permissionsRequest!!.isEmpty() -> requestCallback()
                        else -> startPermissionActivity()
                    }
                }
                else -> {
                    permissionsGranted?.addAll(permissionsSet)
                    requestCallback()
                }
            }
        }

        interface OnRationaleListener {
            fun rationale(shouldRequest: ShouldRequest)
            interface ShouldRequest {
                fun again(again: Boolean)
            }
        }

        private var onRationaleListener: OnRationaleListener? = null
        fun rationale(listener: OnRationaleListener): BuilderPermissions =
            apply { onRationaleListener = listener }

        interface SimpleCallback {
            fun onGranted()
            fun onDenied()
        }

        private var simpleCallback: SimpleCallback? = null
        fun simple(simple: SimpleCallback): BuilderPermissions = apply { simpleCallback = simple }
        interface FullCallback {
            fun onGranted(permissionsGranted: MutableList<String>)
            fun onDenied(
                permissionsDeniedForever: MutableList<String>?,
                permissionsDenied: MutableList<String>
            )
        }

        private var fullCallback: FullCallback? = null
        fun full(full: FullCallback): BuilderPermissions = apply { fullCallback = full }
        interface ThemeCallback {
            fun onActivityCreate(activity: Activity)
        }

        private var themeCallback: ThemeCallback? = null
        fun theme(theme: ThemeCallback): BuilderPermissions = apply { themeCallback = theme }
        private fun requestCallback() {
            onRationaleListener = null
            simpleCallback?.run {
                when {
                    permissionsRequest?.size == 0 || permissionsGranted?.size == permissionsSet.size -> onGranted()
                    else -> if (permissionsDenied?.isNotEmpty() == true) onDenied()
                }
                simpleCallback = null
            }
            fullCallback?.run {
                when {
                    permissionsRequest?.size == 0 || permissionsGranted?.size == permissionsSet.size ->
                        permissionsGranted?.let { onGranted(it) }
                    else -> if (permissionsDenied?.isNotEmpty() == true)
                        permissionsDenied?.let { onDenied(permissionsDeniedForever, it) }
                }
                fullCallback = null
            }
            themeCallback = null
        }

        private var permissionsDenied: MutableList<String>? = null
        private var permissionsDeniedForever: MutableList<String>? = null

        @RequiresApi(M)
        private fun startPermissionActivity() {
            permissionsDenied = mutableListOf()
            permissionsDeniedForever = mutableListOf()
            PermissionActivity.start(app, PermissionActivity.TYPE_RUNTIME)
        }

        @RequiresApi(M)
        class PermissionActivity : Activity(), LoggerKit {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH)
                when (intent.getIntExtra(TYPE, TYPE_RUNTIME)) {
                    TYPE_RUNTIME -> instance?.let {
                        it.themeCallback?.onActivityCreate(this)
                        when {
                            it.rationale(this) -> finish()
                            else -> it.permissionsRequest?.let { request ->
                                when {
                                    request.size > 0 ->
                                        requestPermissions(request.toTypedArray(), 1)
                                    else -> finish()
                                }
                            }
                        }
                    } ?: run {
                        error("$loggerTag->request permissions failed")
                        finish()
                    }
                    TYPE_WRITE_SETTINGS -> startWriteSettingsActivity(this, TYPE_WRITE_SETTINGS)
                    TYPE_DRAW_OVERLAYS -> startOverlayPermissionActivity(this, TYPE_DRAW_OVERLAYS)
                }
            }

            override fun onRequestPermissionsResult(
                requestCode: Int, permissions: Array<String>, grantResults: IntArray
            ) {
                instance?.onRequestPermissionsResult(this)
                finish()
            }

            override fun dispatchTouchEvent(ev: MotionEvent): Boolean = true.apply { finish() }
            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
                when (requestCode) {
                    TYPE_WRITE_SETTINGS -> simpleCallback4WriteSettings?.run {
                        if (isGrantedWriteSettings) onGranted() else onDenied()
                        simpleCallback4WriteSettings = null
                    }
                    TYPE_DRAW_OVERLAYS -> simpleCallback4DrawOverlays?.run {
                        if (isGrantedDrawOverlays) onGranted() else onDenied()
                        simpleCallback4DrawOverlays = null
                    }
                }
                finish()
            }

            companion object {
                const val TYPE_RUNTIME = 0x01
                const val TYPE_WRITE_SETTINGS = 0x02
                const val TYPE_DRAW_OVERLAYS = 0x03
                private const val TYPE = "TYPE"

                @JvmOverloads
                fun start(context: Context, type: Int, isNewTask: Boolean = true) =
                    context.startActivity(Intent().apply {
                        if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        component = ComponentName(context, PermissionActivity::class.java)
                        putExtra(TYPE, type)
                    })
            }
        }

        @RequiresApi(M)
        private fun rationale(activity: Activity): Boolean {
            var isRationale = false
            onRationaleListener?.run {
                permissionsRequest?.let {
                    for (permission in it) {
                        if (activity.shouldShowRequestPermissionRationale(permission)) {
                            getPermissionsStatus(activity)
                            rationale(object : OnRationaleListener.ShouldRequest {
                                override fun again(again: Boolean) =
                                    if (again) startPermissionActivity() else requestCallback()
                            })
                            isRationale = true
                            break
                        }
                    }
                }
                onRationaleListener = null
            }
            return isRationale
        }

        private fun onRequestPermissionsResult(activity: Activity) {
            getPermissionsStatus(activity)
            requestCallback()
        }

        private fun getPermissionsStatus(activity: Activity) {
            for (permission in permissionsRequest!!) {
                when {
                    isGranted(permission) -> permissionsGranted?.add(permission)
                    else -> {
                        permissionsDenied?.add(permission)
                        if (!activity.shouldShowRequestPermissionRationale(permission))
                            permissionsDeniedForever?.add(permission)
                    }
                }
            }
        }

        companion object {
            private var instance: BuilderPermissions? = null
            fun permission(@PermissionConstants.Permission vararg permissions: String): BuilderPermissions =
                BuilderPermissions(*permissions)

            private val PERMISSIONS: MutableList<String> = permissions
            private val permissions: MutableList<String>
                get() = getPermissions(appPackageName)

            fun getPermissions(packageName: String): MutableList<String> = try {
                app.packageManager.getPackageInfo(
                    packageName, PackageManager.GET_PERMISSIONS
                ).requestedPermissions.toMutableList()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                mutableListOf()
            }

            @RequiresApi(M)
            private fun startWriteSettingsActivity(activity: Activity, requestCode: Int) =
                Intent().apply {
                    action = Settings.ACTION_MANAGE_WRITE_SETTINGS
                    data = Uri.parse("package:${appPackageName}")
                }.let {
                    when {
                        isIntentAvailable(it) -> activity.startActivityForResult(it, requestCode)
                        else -> launchAppDetailsSettings()
                    }
                }

            @JvmOverloads
            fun launchAppDetailsSettings(isNewTask: Boolean = true) = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                if (isNewTask) flags = Intent.FLAG_ACTIVITY_NEW_TASK
                data = Uri.parse("package:${appPackageName}")
            }.let { if (isIntentAvailable(it)) app.startActivity(it) }

            @RequiresApi(M)
            private fun startOverlayPermissionActivity(activity: Activity, requestCode: Int) =
                Intent().apply {
                    action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                    data = Uri.parse("package:${appPackageName}")
                }.let {
                    when {
                        isIntentAvailable(it) -> activity.startActivityForResult(it, requestCode)
                        else -> launchAppDetailsSettings()
                    }
                }

            val isGrantedWriteSettings: Boolean
                @RequiresApi(M)
                get() = Settings.System.canWrite(app)
            private var simpleCallback4WriteSettings: SimpleCallback? = null

            @RequiresApi(M)
            fun requestWriteSettings(simpleCallback: SimpleCallback?) = when {
                isGrantedWriteSettings -> simpleCallback?.onGranted()
                else -> {
                    simpleCallback4WriteSettings = simpleCallback
                    PermissionActivity.start(app, PermissionActivity.TYPE_WRITE_SETTINGS)
                }
            }

            val isGrantedDrawOverlays: Boolean
                @RequiresApi(M)
                get() = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> app.appOpsManager.checkOpNoThrow(
                        "android:system_alert_window",
                        android.os.Process.myUid(), appPackageName
                    ).let { it == AppOpsManager.MODE_ALLOWED || it == AppOpsManager.MODE_IGNORED }
                    else -> Settings.canDrawOverlays(app)
                }
            private var simpleCallback4DrawOverlays: SimpleCallback? = null

            @RequiresApi(M)
            fun requestDrawOverlays(simpleCallback: SimpleCallback?) = when {
                isGrantedDrawOverlays -> simpleCallback?.onGranted()
                else -> {
                    simpleCallback4DrawOverlays = simpleCallback
                    PermissionActivity.start(app, PermissionActivity.TYPE_DRAW_OVERLAYS)
                }
            }

            fun isGranted(vararg permissions: String): Boolean {
                for (permission in permissions) {
                    if (!isGranted(permission)) return false
                }
                return true
            }

            private fun isGranted(permission: String): Boolean = when {
                isSpace(permission) -> false
                aboveMarshmallow -> try {
                    Class.forName("android.content.Context")
                        .getMethod("checkSelfPermission", String::class.java)
                        .invoke(app, permission) as Int == PackageManager.PERMISSION_GRANTED
                } catch (e: Exception) {
                    false
                }
                else -> try {
                    app.packageManager?.run {
                        checkPermission(permission, appPackageName) ==
                                PackageManager.PERMISSION_GRANTED
                    } ?: false
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }//ActivityCompat.checkSelfPermission(app, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}