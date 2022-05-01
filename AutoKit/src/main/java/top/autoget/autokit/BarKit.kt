package top.autoget.autokit

import android.Manifest.permission.EXPAND_STATUS_BAR
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.RequiresPermission
import androidx.drawerlayout.widget.DrawerLayout
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ScreenKit.statusBarHeight
import top.autoget.autokit.VersionKit.aboveJellyBeanMR1
import top.autoget.autokit.VersionKit.aboveJellyBeanMR2
import top.autoget.autokit.VersionKit.aboveKitKat
import top.autoget.autokit.VersionKit.aboveMarshmallow

object BarKit {
    fun isStatusBarExists(activity: Activity): Boolean =
        activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN !=
                WindowManager.LayoutParams.FLAG_FULLSCREEN

    fun isStatusBarVisible(activity: Activity): Boolean =
        activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == 0

    private const val TAG_STATUS_BAR = "TAG_STATUS_BAR"
    private const val TAG_OFFSET = "TAG_OFFSET"

    @JvmOverloads
    fun setStatusBarVisibility(activity: Activity, isVisible: Boolean = true) =
        activity.window.run {
            WindowManager.LayoutParams.FLAG_FULLSCREEN
                .let { if (isVisible) clearFlags(it) else addFlags(it) }
            (decorView as ViewGroup).findViewWithTag<View>(TAG_STATUS_BAR)
                ?.visibility = if (isVisible) View.VISIBLE else View.GONE
            decorView.findViewWithTag<View>(TAG_OFFSET)?.let {
                when {
                    isVisible -> addStatusBarHeightBaseTopMargin(it)
                    else -> subtractStatusBarHeightBaseTopMargin(it)
                }
            }
        }

    private const val KEY_OFFSET = -123
    private fun addStatusBarHeightBaseTopMargin(view: View) = view.run {
        if (aboveKitKat) {
            tag = TAG_OFFSET
            if (getTag(KEY_OFFSET) as Boolean? == false) {
                (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins(leftMargin, topMargin + statusBarHeight, rightMargin, bottomMargin)
                }
                setTag(KEY_OFFSET, true)
            }
        }
    }

    private fun subtractStatusBarHeightBaseTopMargin(view: View) = view.run {
        if (aboveKitKat) {
            if (getTag(KEY_OFFSET) as Boolean? == true) {
                (layoutParams as ViewGroup.MarginLayoutParams).apply {
                    setMargins(leftMargin, topMargin - statusBarHeight, rightMargin, bottomMargin)
                }
                setTag(KEY_OFFSET, false)
            }
        }
    }

    fun isStatusBarModeDark(activity: Activity): Boolean = when {
        aboveMarshmallow -> activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR == 0
        else -> false
    }

    enum class Mode { NOON, MIUI, FLYME, OTHERS }

    @JvmOverloads
    fun setStatusBarModeDark(activity: Activity, isDark: Boolean = true): Mode = when {
        Build.VERSION.SDK_INT in Build.VERSION_CODES.KITKAT until Build.VERSION_CODES.M -> when {
            setStatusBarModeDarkMiui(activity.window, isDark) -> Mode.MIUI
            setStatusBarModeDarkFlyme(activity.window, isDark) -> Mode.FLYME
            else -> Mode.NOON
        }
        aboveMarshmallow -> Mode.OTHERS.apply {
            activity.window.decorView.systemUiVisibility = when {
                isDark -> View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                else -> View.SYSTEM_UI_FLAG_VISIBLE
            }
/*            activity.window.decorView.apply {
                systemUiVisibility = when {
                    isDark -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    else -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }*/
        }
        else -> Mode.NOON
    }

    private fun setStatusBarModeDarkFlyme(window: Window?, isDark: Boolean = true): Boolean = try {
        WindowManager.LayoutParams::class.java.run {
            getDeclaredField("meizuFlags").apply { isAccessible = true }.let { flags ->
                getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON").apply { isAccessible = true }
                    .getInt(null).let { bit ->
                        window?.attributes?.let { layoutParams ->
                            when {
                                isDark -> flags.getInt(layoutParams) or bit
                                else -> flags.getInt(layoutParams) and bit.inv()
                            }.let { value -> flags.setInt(layoutParams, value) }
                            true
                        } ?: false
                    }
            }
        }
    } catch (e: Exception) {
        false
    }//图标深色；字体特定风格

    private fun setStatusBarModeDarkMiui(window: Window?, isDark: Boolean = true): Boolean = try {
        "android.view.MiuiWindowManager\$LayoutParams".javaClass.run {
            getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE").getInt(this).let {
                window?.javaClass?.getMethod(
                    "setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType
                )?.run {
                    when {
                        isDark -> invoke(window, it, it)
                        else -> invoke(window, 0, it)
                    }
                    true
                } ?: false
            }
        }
    } catch (e: Exception) {
        false
    }//图标深色；字体深色；状态栏透明；Miui6以上

    fun getStatusBarColor(activity: Activity): Int = activity.window.statusBarColor
    private const val ALPHA = 112

    @JvmOverloads
    fun setStatusBarColor(activity: Activity, @ColorInt color: Int, alpha: Int = ALPHA) =
        activity.window.run {
            if (aboveKitKat) when (Build.VERSION.SDK_INT) {
                in Build.VERSION_CODES.KITKAT until Build.VERSION_CODES.LOLLIPOP -> {
                    addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    (decorView as ViewGroup).let { decorView ->
                        decorView.childCount.let { count ->
                            when {
                                count > 0 && decorView.getChildAt(count - 1) is StatusBarView ->
                                    decorView.getChildAt(count - 1)
                                        .setBackgroundColor(calculateColor(color, alpha))
                                else -> decorView.addView(
                                    createStatusBarViewByColorAlpha(activity, color, alpha)
                                )
                            }
                            setRootView(activity)
                        }//decorView.findViewWithTag(TAG_STATUS_BAR) as View?
                    }//activity.findViewById<View>(android.R.id.content) as ViewGroup
                }
                else -> {
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    statusBarColor = calculateColor(color, alpha)
                }
            }
        }

    private fun calculateColor(@ColorInt color: Int, alpha: Int): Int = (1 - alpha / 255f).let {
        0xff shl 24 or (((color shr 16 and 0xff) * it + 0.5).toInt() shl 16) or
                (((color shr 8 and 0xff) * it + 0.5).toInt() shl 8) or ((color and 0xff) * it + 0.5).toInt()
    }

    class StatusBarView : View {
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        constructor(context: Context) : super(context)
    }

    private fun createStatusBarViewByColorAlpha(
        activity: Activity, @ColorInt color: Int, alpha: Int
    ): StatusBarView = StatusBarView(activity).apply {
        tag = TAG_STATUS_BAR
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)
        setBackgroundColor(calculateColor(color, alpha))
    }

    private fun setRootView(activity: Activity): ViewGroup =
        ((activity.findViewById<View>(android.R.id.content) as ViewGroup)
            .getChildAt(0) as ViewGroup).apply {
            fitsSystemWindows = true
            clipToPadding = true
        }//根布局参数

    @Deprecated("")
    fun setStatusBarColorDiff(activity: Activity, @ColorInt color: Int) {
        if (aboveKitKat) activity.window.run {
            addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            (decorView as ViewGroup).let { decorView ->
                decorView.childCount.let { count ->
                    when {
                        count > 0 && decorView.getChildAt(count - 1) is StatusBarView ->
                            decorView.getChildAt(count - 1).setBackgroundColor(color)
                        else -> decorView.addView(createStatusBarViewByColor(activity, color))
                    }
                    setRootView(activity)
                }
            }
        }
    }//5.0以上状态栏不透明颜色

    private fun createStatusBarViewByColor(
        activity: Activity, @ColorInt color: Int
    ): StatusBarView = StatusBarView(activity).apply {
        tag = TAG_STATUS_BAR
        layoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)
        setBackgroundColor(color)
    }

    @JvmOverloads
    fun setStatusBarColorForDrawerLayout(
        activity: Activity, drawerLayout: DrawerLayout, @ColorInt color: Int, alpha: Int = ALPHA
    ) {
        if (aboveKitKat) {
            when (Build.VERSION.SDK_INT) {
                in Build.VERSION_CODES.KITKAT until Build.VERSION_CODES.LOLLIPOP ->
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                else -> activity.window.run {
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    statusBarColor = Color.TRANSPARENT
                }
            }
            drawerLayout.apply { fitsSystemWindows = false }.let { drawer ->
                (drawer.getChildAt(0) as ViewGroup).apply {
                    fitsSystemWindows = false
                    clipToPadding = true
                }.run {
                    when {
                        childCount > 0 && getChildAt(0) is StatusBarView ->
                            getChildAt(0).setBackgroundColor(calculateColor(color, alpha))
                        else -> addView(createStatusBarViewByColor(activity, color), 0)
                    }
                    if (this !is LinearLayout) getChildAt(1)?.setPadding(
                        paddingLeft, statusBarHeight + paddingTop, paddingRight, paddingBottom
                    )
                }
                (drawer.getChildAt(1) as ViewGroup).fitsSystemWindows = false
            }
            addStatusBarViewByAlpha(activity, alpha)
        }
    }

    private fun addStatusBarViewByAlpha(activity: Activity, alpha: Int) =
        (activity.findViewById<View>(android.R.id.content) as ViewGroup).run {
            when {
                childCount > 1 -> getChildAt(1).setBackgroundColor(Color.argb(alpha, 0, 0, 0))
                else -> addView(createStatusBarViewByAlpha(activity, alpha))
            }//无则添加
        }

    private fun createStatusBarViewByAlpha(activity: Activity, alpha: Int): StatusBarView =
        StatusBarView(activity).apply {
            tag = TAG_STATUS_BAR
            layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)
            setBackgroundColor(Color.argb(alpha, 0, 0, 0))
        }

    @Deprecated("")
    fun setStatusBarColorForDrawerLayoutDiff(
        activity: Activity, drawerLayout: DrawerLayout, @ColorInt color: Int
    ) {
        if (aboveKitKat) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            drawerLayout.apply { fitsSystemWindows = false }.let { drawer ->
                (drawer.getChildAt(0) as ViewGroup).apply {
                    fitsSystemWindows = false
                    clipToPadding = true
                }.run {
                    when {
                        childCount > 0 && getChildAt(0) is StatusBarView ->
                            getChildAt(0).setBackgroundColor(calculateColor(color, ALPHA))
                        else -> addView(createStatusBarViewByColor(activity, color), 0)
                    }
                    if (this !is LinearLayout)
                        getChildAt(1)?.setPadding(0, statusBarHeight, 0, 0)
                }
                (drawer.getChildAt(1) as ViewGroup).fitsSystemWindows = false
            }
        }
    }//5.0以上状态栏半透明颜色

    @JvmOverloads
    fun setStatusBarColorForSwipeBack(
        activity: Activity, @ColorInt color: Int, alpha: Int = ALPHA
    ) {
        if (aboveKitKat) {
            (activity.findViewById<View>(android.R.id.content) as ViewGroup).apply {
                setPadding(0, statusBarHeight, 0, 0)
                setBackgroundColor(calculateColor(color, alpha))
            }
            setWindowTransparent(activity)
        }
    }

    private fun setWindowTransparent(activity: Activity): Window = activity.window.apply {
        if (aboveKitKat) when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.KITKAT until Build.VERSION_CODES.LOLLIPOP ->
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            else -> {
                statusBarColor = Color.TRANSPARENT
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
    }

    fun setAllTransparent(activity: Activity): Window = activity.window.apply {
        if (aboveKitKat) when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.KITKAT until Build.VERSION_CODES.LOLLIPOP -> {
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }
            else -> {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
        }
    }//可Activity的onCreate()中调用；顶部控件布局加入属性让内容出现在状态栏下：android:clipToPadding="true"、android:fitsSystemWindows="true"

    @JvmOverloads
    fun setStatusBarTranslucent(activity: Activity, alpha: Int = ALPHA) {
        if (aboveKitKat) {
            setStatusBarTransparent(activity)
            addStatusBarViewByAlpha(activity, alpha)
        }
    }

    fun setStatusBarTransparent(activity: Activity) {
        if (aboveKitKat) {
            setStatusBarTransparentNoSetRootView(activity)
            setRootView(activity)
        }
    }

    private fun setStatusBarTransparentNoSetRootView(activity: Activity): Window =
        activity.window.apply {
            when (Build.VERSION.SDK_INT) {
                in Build.VERSION_CODES.KITKAT until Build.VERSION_CODES.LOLLIPOP ->
                    addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                else -> {
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                    statusBarColor = Color.TRANSPARENT
                    decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                }
            }
        }

    @Deprecated("")
    fun setStatusBarTranslucentDiff(activity: Activity) {
        if (aboveKitKat) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            setRootView(activity)
        }
    }//5.0以上状态栏半透明无色；界面背景图片填充到状态栏适用

    @JvmOverloads
    fun setStatusBarTranslucentForDrawerLayout(
        activity: Activity, drawerLayout: DrawerLayout, alpha: Int = ALPHA
    ) {
        if (aboveKitKat) {
            when (Build.VERSION.SDK_INT) {
                in Build.VERSION_CODES.KITKAT until Build.VERSION_CODES.LOLLIPOP ->
                    activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                else -> activity.window.run {
                    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    statusBarColor = Color.TRANSPARENT
                }
            }
            drawerLayout.apply { fitsSystemWindows = false }.let { drawer ->
                (drawer.getChildAt(0) as ViewGroup).apply {
                    fitsSystemWindows = false
                    clipToPadding = true
                }.run {
                    if (this !is LinearLayout)
                        getChildAt(1)?.setPadding(0, statusBarHeight, 0, 0)
                }
                (drawer.getChildAt(1) as ViewGroup).fitsSystemWindows = false
            }
            addStatusBarViewByAlpha(activity, alpha)
        }
    }

    @Deprecated("")
    fun setStatusBarTranslucentForDrawerLayoutDiff(activity: Activity, drawerLayout: DrawerLayout) {
        if (aboveKitKat) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            drawerLayout.apply { fitsSystemWindows = false }.let { drawer ->
                (drawer.getChildAt(0) as ViewGroup).apply {
                    fitsSystemWindows = false
                    clipToPadding = true
                }
                (drawer.getChildAt(1) as ViewGroup).apply { fitsSystemWindows = false }
            }
        }
    }//5.0以上状态栏半透明无色

    @JvmOverloads
    fun setStatusBarTranslucentForCoordinatorLayout(activity: Activity, alpha: Int = ALPHA) {
        if (aboveKitKat) {
            setStatusBarTransparentNoSetRootView(activity)
            addStatusBarViewByAlpha(activity, alpha)
        }
    }//界面背景图片填充到状态栏适用

    @JvmOverloads
    fun setStatusBarTranslucentForImageViewIsInFragment(
        activity: Activity, needOffsetView: View?, alpha: Int = ALPHA, isInFragment: Boolean = false
    ) {
        if (aboveKitKat) {
            setWindowTransparent(activity)
            addStatusBarViewByAlpha(activity, alpha)
            (needOffsetView?.layoutParams as ViewGroup.MarginLayoutParams)
                .setMargins(0, statusBarHeight, 0, 0)
            if (isInFragment && Build.VERSION.SDK_INT in Build.VERSION_CODES.KITKAT until Build.VERSION_CODES.LOLLIPOP)
                clearPreviousSetting(activity)
        }
    }

    private fun clearPreviousSetting(activity: Activity) =
        (activity.window.decorView as ViewGroup).run {
            if (childCount > 0 && getChildAt(childCount - 1) is StatusBarView) {
                removeViewAt(childCount - 1)
                ((activity.findViewById<View>(android.R.id.content) as ViewGroup)
                    .getChildAt(0) as ViewGroup).setPadding(0, 0, 0, 0)
            }
        }//移除顶部视图，重置底部间距

    @JvmOverloads
    @RequiresPermission(EXPAND_STATUS_BAR)
    fun setNotificationBarVisibility(isVisible: Boolean = true, isSetting: Boolean = true) = when {
        aboveJellyBeanMR1 -> when {
            isVisible -> if (isSetting) "expandSettingsPanel" else "expandNotificationsPanel"
            else -> "collapsePanels"
        }
        else -> if (isVisible) "expand" else "collapse"
    }.let { invokePanels(it) }

    private fun invokePanels(methodName: String) = try {
        "android.app.StatusBarManager".javaClass.getMethod(methodName).invoke(app)
    } catch (e: Exception) {
        e.printStackTrace()
    }//反射唤醒通知栏

    val actionBarHeight: Int
        get() = TypedValue().run {
            when {
                app.theme.resolveAttribute(android.R.attr.actionBarSize, this, true) -> TypedValue
                    .complexToDimensionPixelSize(data, Resources.getSystem().displayMetrics)
                else -> 0
            }
        }
    val isNavBarSupported: Boolean
        get() = when {
            aboveJellyBeanMR2 -> app.windowManager.defaultDisplay.let { display ->
                Point().apply { display.getRealSize(this) }.let { realSize ->
                    Point().apply { display.getSize(this) }
                        .let { size -> realSize.x != size.x || realSize.y != size.y }
                }
            }
            else -> !ViewConfiguration.get(app).hasPermanentMenuKey()
                    && !KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        }

    fun isNavBarVisible(activity: Activity): Boolean = isNavBarVisible(activity.window)
    fun isNavBarVisible(window: Window): Boolean = (window.decorView as ViewGroup).let { vg ->
        var i = 0
        while (i < vg.childCount) {
            vg.getChildAt(i).apply {
                if (id != View.NO_ID && Resources.getSystem()
                        .getResourceEntryName(id) == "navigationBarBackground" && visibility == View.VISIBLE
                ) return vg.systemUiVisibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION == 0
            }
            i++
        }
        return false
    }

    @JvmOverloads
    fun setNavBarVisibility(activity: Activity, isVisible: Boolean = true) =
        setNavBarVisibility(activity.window, isVisible)

    @JvmOverloads
    fun setNavBarVisibility(window: Window, isVisible: Boolean = true) {
        if (aboveKitKat) (window.decorView as ViewGroup).let { vg ->
            var i = 0
            while (i < vg.childCount) {
                vg.getChildAt(i).apply {
                    if (id != View.NO_ID && Resources.getSystem()
                            .getResourceEntryName(id) == "navigationBarBackground"
                    ) visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
                }
                i++
            }
            vg.apply {
                systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                        .let { flag -> systemUiVisibility.let { if (isVisible) it and flag.inv() else it or flag } }
            }
        }
    }

    fun getNavBarColor(activity: Activity): Int = getNavBarColor(activity.window)
    fun getNavBarColor(window: Window): Int = window.navigationBarColor
    fun setNavBarColor(activity: Activity, @ColorInt color: Int): Window =
        setNavBarColor(activity.window, color)

    fun setNavBarColor(window: Window, @ColorInt color: Int): Window =
        window.apply { navigationBarColor = color }
}