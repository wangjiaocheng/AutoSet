package top.autoget.automap

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.AMapException
import com.amap.api.maps.offlinemap.OfflineMapCity
import com.amap.api.maps.offlinemap.OfflineMapManager
import com.amap.api.maps.offlinemap.OfflineMapStatus
import top.autoget.autokit.HandleKit.runOnUiThreadDelayed
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.ToastKit.showShort
import top.autoget.autokit.debug
import top.autoget.autokit.layoutInflater

class MapOfflineChild(private val context: Context, offlineMapManager: OfflineMapManager?) :
    LoggerKit, View.OnClickListener, View.OnLongClickListener {
    var offLineChildView: View? = null
        private set
    private var mOffLineCityName: TextView? = null
    private var mOffLineCitySize: TextView? = null
    private var mDownloadProgress: TextView? = null
    private var mDownloadImage: ImageView? = null
    private val initView = {
        offLineChildView = context.layoutInflater.inflate(R.layout.offline_child, null)
        mOffLineCityName = offLineChildView?.findViewById(R.id.city_name)
        mOffLineCitySize = offLineChildView?.findViewById(R.id.city_size)
        mDownloadProgress = offLineChildView?.findViewById(R.id.download_progress)
        mDownloadImage = offLineChildView?.findViewById(R.id.download_image)
        offLineChildView?.apply {
            setOnClickListener(this@MapOfflineChild)
            setOnLongClickListener(this@MapOfflineChild)
        }
    }

    init {
        initView
    }

    private var mMapCity: OfflineMapCity? = null
    val cityName: String?
        get() = mMapCity?.city
    private val mIsDownloading = false
    fun setOffLineCity(mapCity: OfflineMapCity?) {
        mapCity?.apply { mMapCity = this }?.run {
            mOffLineCityName?.text = city
            mOffLineCitySize?.text = "${(size / 1024.0 / 1024.0 * 100).toInt() / 100.0} M"//两位小数
            notifyViewDisplay(state, getcompleteCode())
        }
    }

    private val displayStatusLoading = mMapCity?.run {
        mDownloadProgress?.apply {
            visibility = View.VISIBLE
            text = "${mMapCity?.getcompleteCode().toString()}%"
            setTextColor(Color.BLUE)
        }
        mDownloadImage?.apply {
            visibility = View.VISIBLE
            setImageResource(R.mipmap.offlinearrow_stop)
        }
    }
    private val displayStatusSuccess = {
        mDownloadProgress?.apply {
            visibility = View.VISIBLE
            text = "安装成功"
            setTextColor(context.resources.getColor(R.color.gary))
        }
        mDownloadImage?.apply { visibility = View.GONE }
    }
    private val displayStatusException = {
        mDownloadProgress?.apply {
            visibility = View.VISIBLE
            text = "下载出现异常"
            setTextColor(Color.RED)
        }
        mDownloadImage?.apply {
            visibility = View.VISIBLE
            setImageResource(R.mipmap.offlinearrow_start)
        }
    }
    private val displayStatusWaiting = {
        mDownloadProgress?.apply {
            visibility = View.VISIBLE
            text = "等待中"
            setTextColor(Color.GREEN)
        }
        mDownloadImage?.apply {
            visibility = View.VISIBLE
            setImageResource(R.mipmap.offlinearrow_start)
        }
    }
    private val displayStatusDefault = {
        mDownloadProgress?.apply { visibility = View.INVISIBLE }
        mDownloadImage?.apply {
            visibility = View.VISIBLE
            setImageResource(R.mipmap.offlinearrow_download)
        }
    }
    private val displayStatusHasNewVersion = {
        mDownloadProgress?.apply {
            visibility = View.VISIBLE
            text = "已下载-有更新"
        }
        mDownloadImage?.apply {
            visibility = View.VISIBLE
            setImageResource(R.mipmap.offlinearrow_download)
        }
    }
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            msg.run {
                super.handleMessage(this)
                val completeCode = obj as Int
                when (what) {
                    OfflineMapStatus.LOADING -> displayStatusLoading
                    OfflineMapStatus.PAUSE -> displayStatusPause(completeCode)
                    OfflineMapStatus.STOP -> {
                    }
                    OfflineMapStatus.SUCCESS -> displayStatusSuccess
                    OfflineMapStatus.UNZIP -> displayStatusUnZip(completeCode)
                    OfflineMapStatus.ERROR -> displayStatusException
                    OfflineMapStatus.WAITING -> displayStatusWaiting
                    OfflineMapStatus.CHECKUPDATES -> displayStatusDefault
                    OfflineMapStatus.EXCEPTION_AMAP, OfflineMapStatus.EXCEPTION_NETWORK_LOADING,
                    OfflineMapStatus.EXCEPTION_SDCARD -> displayStatusException()
                    OfflineMapStatus.NEW_VERSION -> displayStatusHasNewVersion
                    else -> {
                    }
                }
            }
        }
    }

    private fun displayStatusUnZip(completeCode: Int) {
        mDownloadProgress?.apply {
            visibility = View.VISIBLE
            text = "正在解压: $completeCode%"
            setTextColor(context.resources.getColor(R.color.gary))
        }
        mDownloadImage?.apply { visibility = View.GONE }
    }

    private fun notifyViewDisplay(status: Int, completeCode: Int) {
        mMapCity?.run {
            state = status
            setCompleteCode(completeCode)
        }
        handler.sendMessage(Message().apply {
            what = status
            obj = completeCode
        })
    }

    override fun onClick(view: View) {
        offLineChildView?.apply {
            isEnabled = false
            runOnUiThreadDelayed({ isEnabled = true }, 100)
        }//避免频繁点击不断从头开始和暂停下载
        mMapCity?.run {
            val completeCode = getcompleteCode()
            when (state) {
                OfflineMapStatus.UNZIP, OfflineMapStatus.SUCCESS -> {
                }
                OfflineMapStatus.LOADING -> {
                    downloadPause()
                    displayStatusPause(completeCode)
                }
                OfflineMapStatus.PAUSE, OfflineMapStatus.CHECKUPDATES, OfflineMapStatus.ERROR, OfflineMapStatus.WAITING ->
                    if (downloadStart()) displayStatusWaiting else displayStatusException
                else -> if (downloadStart()) displayStatusWaiting else displayStatusException
            }
            error("$loggerTag->$city $state")
        }
    }

    private val mapManager: OfflineMapManager? = offlineMapManager

    @Synchronized
    private fun downloadPause() = mapManager?.apply { pause() }?.restart()//暂停下载后开始下一个等待中任务
    var isProvince = false

    @Synchronized
    private fun downloadStart(): Boolean = try {
        mapManager?.run {
            when {
                isProvince -> downloadByProvinceName(mMapCity?.city)
                else -> downloadByCityName(mMapCity?.city)
            }
        }
        true
    } catch (e: AMapException) {
        e.printStackTrace()
        showShort(e.errorMessage)
        false
    }

    private fun displayStatusPause(completeCode: Int) {
        mDownloadProgress?.apply {
            visibility = View.VISIBLE
            text = "暂停中:${mMapCity?.getcompleteCode() ?: completeCode}%"
            setTextColor(Color.RED)
        }
        mDownloadImage?.apply {
            visibility = View.VISIBLE
            setImageResource(R.mipmap.offlinearrow_start)
        }
    }

    override fun onLongClick(view: View): Boolean = mMapCity?.run {
        when (state) {
            OfflineMapStatus.LOADING -> mapManager?.restart()
            else -> when {
                state == OfflineMapStatus.SUCCESS -> showDialogDeleteUpdate(city)
                state != OfflineMapStatus.CHECKUPDATES -> showDialogDelete(city)
            }
        }
        debug("$loggerTag->$city : $state")
        false
    } ?: false

    var dialog: Dialog? = null
    fun showDialogDeleteUpdate(name: String?) = AlertDialog.Builder(context).apply {
        setTitle(name)
        setNegativeButton("取消", null)
        setSingleChoiceItems(arrayOf("删除", "检查更新"), -1, DialogInterface.OnClickListener { _, arg1 ->
            dialog?.dismiss()
            mapManager?.run {
                if (arg1 == 0) remove(name) else if (arg1 == 1) try {
                    updateOfflineCityByName(name)
                } catch (e: AMapException) {
                    e.printStackTrace()
                }
            }
        })
    }.create().apply { dialog = this }.show()

    @Synchronized
    fun showDialogDelete(name: String?) = AlertDialog.Builder(context).apply {
        setTitle(name)
        setNegativeButton("取消", null)
        setSingleChoiceItems(arrayOf("删除"), -1, DialogInterface.OnClickListener { _, arg1 ->
            dialog?.dismiss()
            mapManager?.run { if (arg1 == 0) remove(name) }
        })
    }.create().apply { dialog = this }.show()//长按弹出取消下载，避免未关闭再弹出
}