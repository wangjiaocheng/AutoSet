package top.autoget.autokit

import android.Manifest.permission.WRITE_SETTINGS
import android.app.Activity
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Point
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import androidx.annotation.RequiresPermission
import top.autoget.autokit.AKit.app
import top.autoget.autokit.SdKit.isSdCardEnable
import top.autoget.autokit.VersionKit.aboveJellyBeanMR1
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sqrt

object ScreenKit : LoggerKit {
    val isScreenLock: Boolean
        get() = app.keyguardManager.inKeyguardRestrictedInputMode()
    var sleepDuration: Int
        get() = try {
            Settings.System.getInt(app.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            -123
        }
        @RequiresPermission(WRITE_SETTINGS)
        set(duration) {
            Settings.System
                .putInt(app.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, duration)
        }

    fun noShootScreen(activity: Activity) =
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

    fun shootScreenWithStatusBar(activity: Activity): String =
        fileName.let { if (savePic(captureScreen(activity, true), it)) it else "" }

    fun shootScreenWithoutStatusBar(activity: Activity): String =
        fileName.let { if (savePic(captureScreen(activity, false), it)) it else "" }

    fun shootWebView(webView: WebView): String =
        fileName.let { if (savePic(captureWebView(webView), it)) it else "" }

    private val currentTimeStr: String
        get() = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
    private val fileName: String
        get() = "$currentTimeStr.png".let {
            when {
                isSdCardEnable -> "${app.externalCacheDir}${File.separator}$it"
                else -> "${app.filesDir}$it"
            }
        }

    private fun savePic(bitmap: Bitmap, strFileName: String): Boolean = try {
        FileOutputStream(strFileName).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, it)
            it.flush()
        }
        true
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        false
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }

    val realScreenHeight: Int
        get() = try {
            DisplayMetrics().apply {
                Class.forName("android.view.Display")
                    .getMethod("getRealMetrics", DisplayMetrics::class.java)
                    .invoke(app.windowManager.defaultDisplay, this)
            }.heightPixels
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    val screenWidthByPoint: Int
        get() = Point().apply {
            when {
                aboveJellyBeanMR1 -> app.windowManager.defaultDisplay.getRealSize(this)
                else -> app.windowManager.defaultDisplay.getSize(this)
            }
        }.x
    val screenHeightByPoint: Int
        get() = Point().apply {
            when {
                aboveJellyBeanMR1 -> app.windowManager.defaultDisplay.getRealSize(this)
                else -> app.windowManager.defaultDisplay.getSize(this)
            }
        }.y
    val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels
    val screenWidth8: Int
        get() = Resources.getSystem().displayMetrics.widthPixels / 10 * 8
    val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels
    val screenHeight8: Int
        get() = Resources.getSystem().displayMetrics.heightPixels / 10 * 8
    val navigationAreaHeight: Int
        get() = realScreenHeight - screenHeight//虚拟按键区域
    val navigationBarHeight: Int
        get() = Resources.getSystem().run {
            getIdentifier("navigation_bar_height", "dimen", "android").let {
                if (it > 0) getDimensionPixelOffset(it) else 0
            }
        }
    val toolbarHeight: Int
        get() = app.theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize)).run {
            getDimension(0, 0f).apply { recycle() }.toInt()
        }
    val statusBarHeight: Int
        get() = Resources.getSystem().run {
            getIdentifier("status_bar_height", "dimen", "android").let {
                if (it > 0) getDimensionPixelSize(it) else 0
            }
        }//Rect().apply { activity.window.decorView.getWindowVisibleDisplayFrame(this) }.top

    fun getTitleBarHeight(activity: Activity): Int =
        activity.window.findViewById<View>(Window.ID_ANDROID_CONTENT).top - statusBarHeight

    private fun captureScreen(activity: Activity, isWithStatusBar: Boolean): Bitmap =
        activity.window.decorView.apply {
            isDrawingCacheEnabled = true
            buildDrawingCache()//setWillNotCacheDrawing(false)
        }.run {
            when {
                isWithStatusBar ->
                    Bitmap.createBitmap(drawingCache, 0, 0, screenWidth, screenHeight)
                else -> Bitmap.createBitmap(
                    drawingCache, 0, statusBarHeight,
                    screenWidth, screenHeight - statusBarHeight
                )
            }.apply { destroyDrawingCache() }
        }

    private fun captureWebView(webView: WebView): Bitmap = webView.capturePicture().let {
        Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888)
            .apply { it.draw(Canvas(this)) }
    }//webView加载整个内容大小

    val screenDisplayId: String
        get() = app.windowManager.defaultDisplay.displayId.toString()
    val screenDensity: Float
        get() = Resources.getSystem().displayMetrics.density
    val screenDensityDpi: Int
        get() = Resources.getSystem().displayMetrics.densityDpi
    val screenDensityDpiStr: String
        get() = when (Resources.getSystem().displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> "LDPI"
            DisplayMetrics.DENSITY_MEDIUM -> "MDPI"
            DisplayMetrics.DENSITY_TV -> "TVDPI"
            DisplayMetrics.DENSITY_HIGH -> "HDPI"
            DisplayMetrics.DENSITY_XHIGH -> "XHDPI"
            DisplayMetrics.DENSITY_400 -> "XMHDPI"
            DisplayMetrics.DENSITY_XXHIGH -> "XXHDPI"
            DisplayMetrics.DENSITY_XXXHIGH -> "XXXHDPI"
            else -> ""
        }

    @JvmOverloads
    fun getSysSampleSize(
        options: BitmapFactory.Options, minSideLength: Int = -1, maxPixels: Int = -1
    ): Int? = calculateInitSysSampleSize(options, minSideLength, maxPixels)?.let { initialSize ->
        var roundedSize: Int
        when {
            initialSize > 8 -> roundedSize = (initialSize + 7) / 8 * 8
            else -> {
                roundedSize = 1
                while (roundedSize < initialSize) {
                    roundedSize = roundedSize shl 1
                }
            }
        }
        info("$loggerTag->getSysSampleSize$roundedSize")
        return roundedSize
    }

    private fun calculateInitSysSampleSize(
        options: BitmapFactory.Options, minSideLength: Int, maxPixels: Int
    ): Int? = options.outWidth.toDouble().let { width ->
        options.outHeight.toDouble().let { height ->
            when (minSideLength) {
                -1 -> 128
                else -> min(floor(width / minSideLength), floor(height / minSideLength)).toInt()
            }.let { upperBound ->
                (if (maxPixels == -1) 1 else ceil(sqrt(width * height / maxPixels)).toInt()).let { lowerBound ->
                    when {
                        upperBound < lowerBound -> lowerBound
                        else -> when {
                            minSideLength == -1 && maxPixels == -1 -> 1
                            minSideLength == -1 -> lowerBound//minSideLength == -1 && maxPixels != -1
                            else -> upperBound
                        }//(minSideLength != -1 && maxPixels == -1) || (minSideLength != -1 && maxPixels != -1)
                    }
                }
            }
        }
    }

    fun adaptWidth(resources: Resources, designWidth: Int): Resources = resources.apply {
        setAppDmXdpi(getDisplayMetrics(this).apply { xdpi = widthPixels * 72f / designWidth }.xdpi)
    }

    fun adaptHeight(resources: Resources, designHeight: Int): Resources = resources.apply {
        setAppDmXdpi(getDisplayMetrics(this).apply {
            xdpi = heightPixels * 72f / designHeight
        }.xdpi)
    }

    fun closeAdapt(resources: Resources): Resources = resources.apply {
        setAppDmXdpi(getDisplayMetrics(this).apply { xdpi = density * 72 }.xdpi)
    }

    private fun setAppDmXdpi(xDpi: Float): DisplayMetrics =
        Resources.getSystem().displayMetrics.apply { xdpi = xDpi }

    private fun getDisplayMetrics(resources: Resources): DisplayMetrics =
        resources.run { getMIUITmpMetrics(this) ?: displayMetrics }

    private var isInitMIUI = false
    private var mTmpMetrics: Field? = null
    private fun getMIUITmpMetrics(resources: Resources): DisplayMetrics? = when {
        !isInitMIUI -> when (resources.javaClass.simpleName) {
            "MiuiResources", "XResources" -> try {
                Resources::class.java.getDeclaredField("mTmpMetrics").apply {
                    mTmpMetrics = this
                    isAccessible = true
                }.get(resources) as DisplayMetrics
            } catch (e: Exception) {
                error("$loggerTag->no field of mTmpMetrics in resources.")
                null
            }
            else -> null
        }.apply { isInitMIUI = true }
        mTmpMetrics == null -> null
        else -> try {
            mTmpMetrics?.get(resources) as DisplayMetrics
        } catch (e: Exception) {
            null
        }
    }
}