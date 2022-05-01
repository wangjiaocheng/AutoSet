package top.autoget.autokit

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.AnimRes
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import top.autoget.autokit.AKit.activityLifecycle
import top.autoget.autokit.AKit.activityLinkedList
import top.autoget.autokit.AKit.app
import top.autoget.autokit.AKit.topActivityOrApp
import top.autoget.autokit.ApplicationKit.appPackageName
import top.autoget.autokit.IntentKit.isIntentAvailable
import top.autoget.autokit.VersionKit.aboveJellyBean
import top.autoget.autokit.VersionKit.aboveLollipop
import java.util.*

object ActivityKit : LoggerKit {
    private fun startActivityBase(
        context: Context, extras: Bundle?, pkg: String, cls: String, options: Bundle?
    ): Boolean = Intent().apply {
        action = Intent.ACTION_VIEW
        component = ComponentName(pkg, cls)
        extras?.let { putExtras(extras) }
    }.let { startActivityBase(context, it, options) }

    private fun getOptionsBundle(activity: Activity, arrayOfViews: Array<out View>?): Bundle? =
        arrayOfViews?.let { views ->
            if (aboveLollipop && views.isNotEmpty())
                arrayOfNulls<Pair<View, String>>(views.size).apply {
                    for ((index, view) in views.withIndex()) {
                        this[index] = Pair.create(view, view.transitionName)
                    }
                }.let { pairs ->
                    ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *pairs).toBundle()
                }
            else null
        }

    private fun getOptionsBundle(context: Context, enterAnim: Int, exitAnim: Int): Bundle? =
        ActivityOptionsCompat.makeCustomAnimation(context, enterAnim, exitAnim).toBundle()

    @JvmOverloads
    fun startActivity(clz: Class<out Activity>, options: Bundle? = null): Boolean =
        topActivityOrApp.let { startActivityBase(it, null, it.packageName, clz.name, options) }

    fun startActivity(clz: Class<out Activity>, vararg sharedElements: View): Boolean =
        topActivityOrApp.let {
            startActivityBase(
                it, null, it.packageName, clz.name, getOptionsBundle(it as Activity, sharedElements)
            )
        }

    fun startActivity(
        clz: Class<out Activity>, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = topActivityOrApp.let {
        startActivityBase(
            it, null, it.packageName, clz.name, getOptionsBundle(it, enterAnim, exitAnim)
        ).apply {
            if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && it is Activity)
                it.overridePendingTransition(enterAnim, exitAnim)
        }
    }

    @JvmOverloads
    fun startActivity(
        activity: Activity, clz: Class<out Activity>, options: Bundle? = null
    ): Boolean = startActivityBase(activity, null, activity.packageName, clz.name, options)

    fun startActivity(
        activity: Activity, clz: Class<out Activity>, vararg sharedElements: View
    ): Boolean = startActivityBase(
        activity, null, activity.packageName, clz.name, getOptionsBundle(activity, sharedElements)
    )

    fun startActivity(
        activity: Activity, clz: Class<out Activity>,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityBase(
        activity, null, activity.packageName, clz.name,
        getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    @JvmOverloads
    fun startActivity(extras: Bundle, clz: Class<out Activity>, options: Bundle? = null): Boolean =
        topActivityOrApp.let {
            startActivityBase(it, extras, it.packageName, clz.name, options)
        }

    fun startActivity(
        extras: Bundle, clz: Class<out Activity>, vararg sharedElements: View
    ): Boolean = topActivityOrApp.let {
        startActivityBase(
            it, extras, it.packageName, clz.name, getOptionsBundle(it as Activity, sharedElements)
        )
    }

    fun startActivity(
        extras: Bundle, clz: Class<out Activity>, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = topActivityOrApp.let {
        startActivityBase(
            it, extras, it.packageName, clz.name, getOptionsBundle(it, enterAnim, exitAnim)
        ).apply {
            if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && it is Activity)
                it.overridePendingTransition(enterAnim, exitAnim)
        }
    }

    @JvmOverloads
    fun startActivity(
        activity: Activity, extras: Bundle, clz: Class<out Activity>, options: Bundle? = null
    ): Boolean = startActivityBase(activity, extras, activity.packageName, clz.name, options)

    fun startActivity(
        activity: Activity, extras: Bundle, clz: Class<out Activity>, vararg sharedElements: View
    ): Boolean = startActivityBase(
        activity, extras, activity.packageName, clz.name, getOptionsBundle(activity, sharedElements)
    )

    fun startActivity(
        activity: Activity, extras: Bundle, clz: Class<out Activity>,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityBase(
        activity, extras, activity.packageName, clz.name,
        getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    @JvmOverloads
    fun startActivity(pkg: String, cls: String, options: Bundle? = null): Boolean =
        startActivityBase(topActivityOrApp, null, pkg, cls, options)

    fun startActivity(pkg: String, cls: String, vararg sharedElements: View): Boolean =
        topActivityOrApp.let {
            startActivityBase(it, null, pkg, cls, getOptionsBundle(it as Activity, sharedElements))
        }

    fun startActivity(
        pkg: String, cls: String, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = topActivityOrApp.let {
        startActivityBase(it, null, pkg, cls, getOptionsBundle(it, enterAnim, exitAnim)).apply {
            if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && it is Activity)
                it.overridePendingTransition(enterAnim, exitAnim)
        }
    }

    @JvmOverloads
    fun startActivity(
        activity: Activity, pkg: String, cls: String, options: Bundle? = null
    ): Boolean = startActivityBase(activity, null, pkg, cls, options)

    fun startActivity(
        activity: Activity, pkg: String, cls: String, vararg sharedElements: View
    ): Boolean =
        startActivityBase(activity, null, pkg, cls, getOptionsBundle(activity, sharedElements))

    fun startActivity(
        activity: Activity, pkg: String, cls: String,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityBase(
        activity, null, pkg, cls, getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    @JvmOverloads
    fun startActivity(extras: Bundle, pkg: String, cls: String, options: Bundle? = null): Boolean =
        startActivityBase(topActivityOrApp, extras, pkg, cls, options)

    fun startActivity(
        extras: Bundle, pkg: String, cls: String, vararg sharedElements: View
    ): Boolean = topActivityOrApp.let {
        startActivityBase(it, extras, pkg, cls, getOptionsBundle(it as Activity, sharedElements))
    }

    fun startActivity(
        extras: Bundle, pkg: String, cls: String, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = topActivityOrApp.let {
        startActivityBase(it, extras, pkg, cls, getOptionsBundle(it, enterAnim, exitAnim)).apply {
            if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && it is Activity)
                it.overridePendingTransition(enterAnim, exitAnim)
        }
    }

    @JvmOverloads
    fun startActivity(
        activity: Activity, extras: Bundle, pkg: String, cls: String, options: Bundle? = null
    ): Boolean = startActivityBase(activity, extras, pkg, cls, options)

    fun startActivity(
        activity: Activity, extras: Bundle, pkg: String, cls: String, vararg sharedElements: View
    ): Boolean =
        startActivityBase(activity, extras, pkg, cls, getOptionsBundle(activity, sharedElements))

    fun startActivity(
        activity: Activity, extras: Bundle, pkg: String, cls: String,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityBase(
        activity, extras, pkg, cls, getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    private fun startActivityBase(context: Context, intent: Intent, options: Bundle?): Boolean =
        intent.apply {
            when {
                isIntentAvailable(intent) -> if (context !is Activity)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                else -> return false.apply { error("$loggerTag->intent is unavailable") }
            }
        }.let {
            when {
                aboveJellyBean && options != null -> context.startActivity(it, options)
                else -> context.startActivity(it)
            }
            true
        }

    @JvmOverloads
    fun startActivity(intent: Intent, options: Bundle? = null): Boolean =
        startActivityBase(topActivityOrApp, intent, options)

    fun startActivity(intent: Intent, vararg sharedElements: View): Boolean = topActivityOrApp.let {
        startActivityBase(it, intent, getOptionsBundle(it as Activity, sharedElements))
    }

    fun startActivity(intent: Intent, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int): Boolean =
        topActivityOrApp.let {
            startActivityBase(it, intent, getOptionsBundle(it, enterAnim, exitAnim)).apply {
                if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && it is Activity)
                    it.overridePendingTransition(enterAnim, exitAnim)
            }
        }

    @JvmOverloads
    fun startActivity(activity: Activity, intent: Intent, options: Bundle? = null): Boolean =
        startActivityBase(activity, intent, options)

    fun startActivity(activity: Activity, intent: Intent, vararg sharedElements: View): Boolean =
        startActivityBase(activity, intent, getOptionsBundle(activity, sharedElements))

    fun startActivity(
        activity: Activity, intent: Intent, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean =
        startActivityBase(activity, intent, getOptionsBundle(activity, enterAnim, exitAnim)).apply {
            if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                activity.overridePendingTransition(enterAnim, exitAnim)
        }

    private fun startActivityForResultBase(
        activity: Activity, extras: Bundle?, pkg: String, cls: String, requestCode: Int,
        options: Bundle?
    ): Boolean = Intent().apply {
        action = Intent.ACTION_VIEW
        component = ComponentName(pkg, cls)
        extras?.let { putExtras(extras) }
    }.let { startActivityForResultBase(activity, it, requestCode, options) }

    @JvmOverloads
    fun startActivityForResult(
        activity: Activity, clz: Class<out Activity>, requestCode: Int, options: Bundle? = null
    ): Boolean = startActivityForResultBase(
        activity, null, activity.packageName, clz.name, requestCode, options
    )

    fun startActivityForResult(
        activity: Activity, clz: Class<out Activity>, requestCode: Int, vararg sharedElements: View
    ): Boolean = startActivityForResultBase(
        activity, null, activity.packageName, clz.name, requestCode,
        getOptionsBundle(activity, sharedElements)
    )

    fun startActivityForResult(
        activity: Activity, clz: Class<out Activity>, requestCode: Int,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityForResultBase(
        activity, null, activity.packageName, clz.name, requestCode,
        getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    @JvmOverloads
    fun startActivityForResult(
        activity: Activity, extras: Bundle, clz: Class<out Activity>, requestCode: Int,
        options: Bundle? = null
    ): Boolean = startActivityForResultBase(
        activity, extras, activity.packageName, clz.name, requestCode, options
    )

    fun startActivityForResult(
        activity: Activity, extras: Bundle, clz: Class<out Activity>, requestCode: Int,
        vararg sharedElements: View
    ): Boolean = startActivityForResultBase(
        activity, extras, activity.packageName, clz.name, requestCode,
        getOptionsBundle(activity, sharedElements)
    )

    fun startActivityForResult(
        activity: Activity, extras: Bundle, clz: Class<out Activity>, requestCode: Int,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityForResultBase(
        activity, extras, activity.packageName, clz.name, requestCode,
        getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    @JvmOverloads
    fun startActivityForResult(
        activity: Activity, pkg: String, cls: String, requestCode: Int, options: Bundle? = null
    ): Boolean = startActivityForResultBase(activity, null, pkg, cls, requestCode, options)

    fun startActivityForResult(
        activity: Activity, pkg: String, cls: String, requestCode: Int, vararg sharedElements: View
    ): Boolean = startActivityForResultBase(
        activity, null, pkg, cls, requestCode, getOptionsBundle(activity, sharedElements)
    )

    fun startActivityForResult(
        activity: Activity, pkg: String, cls: String, requestCode: Int,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityForResultBase(
        activity, null, pkg, cls, requestCode, getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    @JvmOverloads
    fun startActivityForResult(
        activity: Activity, extras: Bundle, pkg: String, cls: String, requestCode: Int,
        options: Bundle? = null
    ): Boolean = startActivityForResultBase(activity, extras, pkg, cls, requestCode, options)

    fun startActivityForResult(
        activity: Activity, extras: Bundle, pkg: String, cls: String, requestCode: Int,
        vararg sharedElements: View
    ): Boolean = startActivityForResultBase(
        activity, extras, pkg, cls, requestCode, getOptionsBundle(activity, sharedElements)
    )

    fun startActivityForResult(
        activity: Activity, extras: Bundle, pkg: String, cls: String, requestCode: Int,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityForResultBase(
        activity, extras, pkg, cls, requestCode, getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    private fun startActivityForResultBase(
        activity: Activity, intent: Intent, requestCode: Int, options: Bundle?
    ): Boolean = when {
        isIntentAvailable(intent) -> true.apply {
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || options == null ->
                    activity.startActivityForResult(intent, requestCode)
                else -> activity.startActivityForResult(intent, requestCode, options)
            }
        }
        else -> false.apply { error("$loggerTag->intent is unavailable") }
    }

    @JvmOverloads
    fun startActivityForResult(
        activity: Activity, intent: Intent, requestCode: Int, options: Bundle? = null
    ): Boolean = startActivityForResultBase(activity, intent, requestCode, options)

    fun startActivityForResult(
        activity: Activity, intent: Intent, requestCode: Int, vararg sharedElements: View
    ): Boolean = startActivityForResultBase(
        activity, intent, requestCode, getOptionsBundle(activity, sharedElements)
    )

    fun startActivityForResult(
        activity: Activity, intent: Intent, requestCode: Int,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean = startActivityForResultBase(
        activity, intent, requestCode, getOptionsBundle(activity, enterAnim, exitAnim)
    ).apply {
        if (this && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    private fun startActivitiesBase(context: Context, intents: Array<Intent>, options: Bundle?) =
        intents.apply {
            if (context !is Activity) for (intent in this) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }.let {
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || options == null ->
                    context.startActivities(it)
                else -> context.startActivities(it, options)
            }
        }

    @JvmOverloads
    fun startActivities(intents: Array<Intent>, options: Bundle? = null) =
        startActivitiesBase(topActivityOrApp, intents, options)

    fun startActivities(intents: Array<Intent>, vararg sharedElements: View) =
        topActivityOrApp.let {
            startActivitiesBase(it, intents, getOptionsBundle(it as Activity, sharedElements))
        }

    fun startActivities(intents: Array<Intent>, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int) =
        topActivityOrApp.let {
            startActivitiesBase(it, intents, getOptionsBundle(it, enterAnim, exitAnim))
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN && it is Activity)
                it.overridePendingTransition(enterAnim, exitAnim)
        }

    @JvmOverloads
    fun startActivities(activity: Activity, intents: Array<Intent>, options: Bundle? = null) =
        startActivitiesBase(activity, intents, options)

    fun startActivities(activity: Activity, intents: Array<Intent>, vararg sharedElements: View) =
        startActivitiesBase(activity, intents, getOptionsBundle(activity, sharedElements))

    fun startActivities(
        activity: Activity, intents: Array<Intent>, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ) {
        startActivitiesBase(activity, intents, getOptionsBundle(activity, enterAnim, exitAnim))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            activity.overridePendingTransition(enterAnim, exitAnim)
    }

    val startHomeActivity: Boolean
        get() = startActivity(Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_HOME)
        })
    val activityList: LinkedList<Activity>
        get() = activityLinkedList
    val currentActivity: Activity?
        get() = activityLifecycle.topActivity
    val topActivityName: String
        get() = currentActivity?.javaClass?.simpleName ?: ""
    val launcherActivityName: String
        get() = getLauncherActivityName(appPackageName)

    fun getLauncherActivityName(pkg: String): String = Intent().apply {
        action = Intent.ACTION_MAIN
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        addCategory(Intent.CATEGORY_LAUNCHER)
    }.let {
        for (resolveInfo in app.packageManager.queryIntentActivities(it, 0)) {
            resolveInfo.activityInfo.run { if (packageName == pkg) return name }
        }
        return "no $pkg"
    }

    fun getActivityByView(view: View): Activity? {
        var context = view.context
        while (context is ContextWrapper) {
            when (context) {
                is Activity -> return context
                else -> context = context.baseContext
            }
        }
        return null
    }

    fun isActivityExists(pkg: String, cls: String): Boolean =
        Intent().apply { component = ComponentName(pkg, cls) }.let {
            app.packageManager.run {
                it.resolveActivity(this) != null && resolveActivity(it, 0) != null
                        && queryIntentActivities(it, 0).size != 0
            }
        }

    fun isActivityExistsInList(activity: Activity): Boolean {
        for (act in activityList) {
            if (act == activity) return true
        }
        return false
    }

    fun isActivityExistsInList(clz: Class<out Activity>): Boolean {
        for (activity in activityList) {
            if (activity.javaClass == clz) return true
        }
        return false
    }

    @JvmOverloads
    fun finishActivity(activity: Activity, isLoadAnim: Boolean = false) = activity.run {
        finish()
        if (!isLoadAnim) overridePendingTransition(0, 0)
    }

    fun finishActivity(activity: Activity, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int) =
        activity.run {
            finish()
            overridePendingTransition(enterAnim, exitAnim)
        }

    @JvmOverloads
    fun finishActivity(clz: Class<out Activity>, isLoadAnim: Boolean = false) {
        for (activity in activityList) {
            if (activity.javaClass == clz) activity.run {
                finish()
                if (!isLoadAnim) overridePendingTransition(0, 0)
            }
        }
    }

    fun finishActivity(clz: Class<out Activity>, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
        for (activity in activityList) {
            if (activity.javaClass == clz) activity.run {
                finish()
                overridePendingTransition(enterAnim, exitAnim)
            }
        }
    }

    @JvmOverloads
    fun finishToActivity(
        activity: Activity, isIncludeSelf: Boolean, isLoadAnim: Boolean = false
    ): Boolean {
        for (act in activityList.reversed()) {
            if (act == activity) return true.apply {
                if (isIncludeSelf) finishActivity(act, isLoadAnim)
            }
            finishActivity(act, isLoadAnim)
        }
        return false
    }

    fun finishToActivity(
        activity: Activity, isIncludeSelf: Boolean, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean {
        for (act in activityList.reversed()) {
            if (act == activity) return true.apply {
                if (isIncludeSelf) finishActivity(act, enterAnim, exitAnim)
            }
            finishActivity(act, enterAnim, exitAnim)
        }
        return false
    }

    @JvmOverloads
    fun finishToActivity(
        clz: Class<out Activity>, isIncludeSelf: Boolean, isLoadAnim: Boolean = false
    ): Boolean {
        for (activity in activityList.reversed()) {
            if (activity.javaClass == clz) return true.apply {
                if (isIncludeSelf) finishActivity(activity, isLoadAnim)
            }
            finishActivity(activity, isLoadAnim)
        }
        return false
    }

    fun finishToActivity(
        clz: Class<out Activity>, isIncludeSelf: Boolean,
        @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ): Boolean {
        for (activity in activityList.reversed()) {
            if (activity.javaClass == clz) return true.apply {
                if (isIncludeSelf) finishActivity(activity, enterAnim, exitAnim)
            }
            finishActivity(activity, enterAnim, exitAnim)
        }
        return false
    }

    @JvmOverloads
    fun finishOtherActivities(activity: Activity, isLoadAnim: Boolean = false) {
        for (act in activityList.reversed()) {
            if (act != activity) finishActivity(act, isLoadAnim)
        }
    }

    fun finishOtherActivities(activity: Activity, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
        for (act in activityList.reversed()) {
            if (act != activity) finishActivity(act, enterAnim, exitAnim)
        }
    }

    @JvmOverloads
    fun finishOtherActivities(clz: Class<out Activity>, isLoadAnim: Boolean = false) {
        for (activity in activityList.reversed()) {
            if (activity.javaClass != clz) finishActivity(activity, isLoadAnim)
        }
    }

    fun finishOtherActivities(
        clz: Class<out Activity>, @AnimRes enterAnim: Int, @AnimRes exitAnim: Int
    ) {
        for (activity in activityList.reversed()) {
            if (activity.javaClass != clz) finishActivity(activity, enterAnim, exitAnim)
        }
    }

    @JvmOverloads
    fun finishAllActivitiesExceptNewest(isLoadAnim: Boolean = false) {
        for (i in activityList.size - 2 downTo 0) {
            finishActivity(activityList[i], isLoadAnim)
        }
    }

    fun finishAllActivitiesExceptNewest(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
        for (i in activityList.size - 2 downTo 0) {
            finishActivity(activityList[i], enterAnim, exitAnim)
        }
    }

    @JvmOverloads
    fun finishAllActivities(isLoadAnim: Boolean = false) {
        for (activity in activityList.reversed()) {
            activity.run {
                finish()
                if (!isLoadAnim) overridePendingTransition(0, 0)
            }
        }
    }

    fun finishAllActivities(@AnimRes enterAnim: Int, @AnimRes exitAnim: Int) {
        for (activity in activityList.reversed()) {
            activity.run {
                finish()
                overridePendingTransition(enterAnim, exitAnim)
            }
        }
    }

    fun getActivityIcon(activity: Activity): Drawable? = getActivityIcon(activity.componentName)
    fun getActivityIcon(cls: Class<out Activity>): Drawable? =
        getActivityIcon(ComponentName(app, cls))

    fun getActivityIcon(activityName: ComponentName): Drawable? = try {
        app.packageManager.getActivityIcon(activityName)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }

    fun getActivityLogo(activity: Activity): Drawable? = getActivityLogo(activity.componentName)
    fun getActivityLogo(cls: Class<out Activity>): Drawable? =
        getActivityLogo(ComponentName(app, cls))

    fun getActivityLogo(activityName: ComponentName): Drawable? = try {
        app.packageManager.getActivityLogo(activityName)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}