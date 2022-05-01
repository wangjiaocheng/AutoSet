package top.autoget.autokit

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import top.autoget.autokit.ApplicationKit.appPackageName
import java.lang.reflect.InvocationTargetException
import java.util.*

object AKit {
    class FileProvider4AutoKit : FileProvider() {
        override fun onCreate(): Boolean = true.apply { initContext(context) }
    }

    class ContentProvider4AutoKit : ContentProvider() {
        override fun onCreate(): Boolean = true.apply { initContext(context) }
        override fun getType(uri: Uri): String? = null
        override fun query(
            uri: Uri, projection: Array<String>?,
            selection: String?, selectionArgs: Array<String>?, sortOrder: String?
        ): Cursor? = null

        override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0
        override fun insert(uri: Uri, values: ContentValues?): Uri? = null
        override fun update(
            uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?
        ): Int = 0
    }

    fun initContext(context: Context?) =
        initApplication((context?.applicationContext as Application?) ?: applicationByReflect)

    private var application: Application? = null
    private val applicationByReflect: Application
        get() {
            try {
                "android.app.ActivityThread"::class.java.run {
                    (getMethod("getApplication")
                        .invoke(getMethod("currentActivityThread").invoke(null)) as Application?
                        ?: throw NullPointerException("u should initApplication first"))
                }
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
            throw NullPointerException("u should initApplication first")
        }
    val app: Application
        get() = application ?: applicationByReflect.apply { initApplication(this) }

    interface OnAppStatusChangedListener {
        fun onForeground()
        fun onBackground()
    }

    interface OnActivityDestroyedListener {
        fun onActivityDestroyed(activity: Activity)
    }

    internal class ActivityLifecycleImpl : ActivityLifecycleCallbacks {
        val activityList: LinkedList<Activity> = LinkedList()
        private val topActivityByReflect: Activity?
            get() {
                try {
                    "android.app.ActivityThread"::class.java.let { activityThread ->
                        activityThread.getDeclaredField("activityList")
                            .apply { isAccessible = true }.let { activityList ->
                                for (activityRecord in (activityList.get(
                                    activityThread.getMethod("currentActivityThread").invoke(null)
                                ) as Map<*, *>).values) {
                                    activityRecord?.let {
                                        activityRecord::class.java.let { activityRecordClass ->
                                            activityRecordClass.getDeclaredField("paused")
                                                .apply { isAccessible = true }.let { paused ->
                                                    if (!paused.getBoolean(activityRecord))
                                                        activityRecordClass.getDeclaredField("activity")
                                                            .apply { isAccessible = true }
                                                            .let { activity ->
                                                                activity.get(activityRecord) as Activity
                                                            }
                                                }
                                        }
                                    }
                                }
                            }
                    }
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                } catch (e: NoSuchFieldException) {
                    e.printStackTrace()
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
                return null
            }
        var topActivity: Activity?
            get() = when {
                activityList.isNotEmpty() && activityList.last != null -> activityList.last
                topActivityByReflect != null -> topActivityByReflect
                else -> null
            }
            private set(activity) = activityList.run {
                activity?.let {
                    when {
                        activity::class.java.name != "$appPackageName.helper.PermissionHelper\$Builder\$PermissionActivity" &&
                                contains(activity) && last != activity -> {
                            remove(activity)
                            addLast(activity)
                        }
                        else -> addLast(activity)
                    }
                } ?: addLast(activity)
            }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            topActivity = activity
        }

        private var isBackground = false
        private var configCount = 0
        private var foregroundCount = 0
        override fun onActivityStarted(activity: Activity) {
            if (!isBackground) topActivity = activity
            if (configCount < 0) ++configCount else ++foregroundCount
        }

        override fun onActivityResumed(activity: Activity) {
            topActivity = activity
            if (isBackground) {
                isBackground = false
                postStatus(true)
            }
        }

        private val statusListenerMap: MutableMap<Any, OnAppStatusChangedListener> = mutableMapOf()
        private fun postStatus(isForeground: Boolean) {
            if (statusListenerMap.isNotEmpty())
                for (onAppStatusChangedListener in statusListenerMap.values) {
                    when {
                        isForeground -> onAppStatusChangedListener.onForeground()
                        else -> onAppStatusChangedListener.onBackground()
                    }
                }
        }

        fun addOnAppStatusChangedListener(any: Any, listener: OnAppStatusChangedListener) =
            statusListenerMap.set(any, listener)

        fun removeOnAppStatusChangedListener(any: Any): OnAppStatusChangedListener? =
            statusListenerMap.remove(any)

        override fun onActivityPaused(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {
            when {
                activity.isChangingConfigurations -> --configCount
                else -> {
                    --foregroundCount
                    if (foregroundCount <= 0) {
                        isBackground = true
                        postStatus(false)
                    }
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {
            activityList.remove(activity)
            consumeOnActivityDestroyedListener(activity)
            fixSoftInputLeaks(activity)
        }

        private val destroyedListenerMap: MutableMap<Activity, MutableSet<OnActivityDestroyedListener>> =
            mutableMapOf()

        private fun consumeOnActivityDestroyedListener(activity: Activity) {
            for ((key, value) in destroyedListenerMap) {
                if (key === activity) for (listener in value) {
                    listener.onActivityDestroyed(activity)
                }
            }
        }

        fun addOnActivityDestroyedListener(
            activity: Activity?, listener: OnActivityDestroyedListener?
        ) {
            if (activity != null && listener != null) {
                val listeners: MutableSet<OnActivityDestroyedListener>?
                when {
                    destroyedListenerMap.containsKey(activity) -> {
                        listeners = destroyedListenerMap[activity]
                        if (listeners == null || listeners.contains(listener)) return
                    }
                    else -> {
                        listeners = mutableSetOf()
                        destroyedListenerMap[activity] = listeners
                    }
                }
                listeners.add(listener)
            }
        }

        fun removeOnActivityDestroyedListener(activity: Activity?): MutableSet<OnActivityDestroyedListener>? =
            activity?.let { destroyedListenerMap.remove(it) }

        private fun fixSoftInputLeaks(activity: Activity?) = activity?.let {
            arrayOf("mCurRootView", "mServedView", "mLastSrvView", "mNextServedView").let { array ->
                for (leakView in array) {
                    try {
                        InputMethodManager::class.java.getDeclaredField(leakView)
                            .apply { if (!isAccessible) isAccessible = true }.run {
                                (get(app.inputMethodManager) as? View)?.let {
                                    if (it.rootView === activity.window.decorView.rootView)
                                        set(app.inputMethodManager, null)
                                }
                            }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
        }
    }

    internal val activityLifecycle = ActivityLifecycleImpl()
    internal val activityLinkedList: LinkedList<Activity>
        get() = activityLifecycle.activityList
    internal val isForegroundApp: Boolean
        get() = app.activityManager.runningAppProcesses?.run {
            when (size) {
                0 -> false
                else -> {
                    for (runningAppProcessInfo in this) {
                        if (runningAppProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                            return runningAppProcessInfo.processName == app.packageName
                    }
                    false
                }
            }
        } ?: false
    internal val topActivityOrApp: Context
        get() = if (isForegroundApp) activityLifecycle.topActivity ?: app else app

    private fun initApplication(app: Application?) = application?.let {
        app?.let {
            if (app.javaClass != application?.javaClass) application = app.apply {
                registerActivityLifecycleCallbacks(activityLifecycle.apply {
                    application?.unregisterActivityLifecycleCallbacks(this)
                    activityList.clear()
                })
            }
        }
    } ?: run {
        application = (app ?: applicationByReflect)
            .apply { registerActivityLifecycleCallbacks(activityLifecycle) }
    }
}