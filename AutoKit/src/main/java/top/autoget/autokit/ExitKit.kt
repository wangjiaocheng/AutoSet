package top.autoget.autokit

import android.content.Context
import android.content.Intent
import android.os.Process
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ActivityKit.finishAllActivities
import top.autoget.autokit.ToastKit.showShort
import java.util.*
import kotlin.system.exitProcess

object ExitKit : LoggerKit {
    val exitApp: Boolean
        get() = exitApp()

    interface OnExitClickListener {
        fun onRetry()//再点击一次
        fun onExit()//退出
    }//退出点击监听

    fun exitApp(
        intervalMillis: Long = 2000, onExitClickListener: OnExitClickListener? = null,
        context: Context = app,
    ): Boolean {
        var isExit = false
        when (isExit) {
            false -> {
                isExit = true
                onExitClickListener?.onRetry() ?: showShort("再按一次退出程序")
                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        isExit = false
                    }
                }, intervalMillis)
            }
            else -> onExitClickListener?.onExit() ?: try {
                finishAllActivities()
                Intent().apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }.let { context.startActivity(it) }
                app.run { activityManager.restartPackage(packageName) }
                System.gc()
                Process.killProcess(Process.myPid())
                exitProcess(0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Runtime.getRuntime().run {
            info("$loggerTag->exitApp->$isExit")
            info("$loggerTag->exitApp->最大内存：${maxMemory()}")
            info("$loggerTag->exitApp->占用内存：${totalMemory()}")
            info("$loggerTag->exitApp->空闲内存：${freeMemory()}")
        }
        return isExit
    }
}