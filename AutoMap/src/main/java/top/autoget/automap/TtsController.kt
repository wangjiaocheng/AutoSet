package top.autoget.automap

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.amap.api.navi.AMapNaviListener
import com.amap.api.navi.model.*
import java.util.*

class TtsController private constructor(context: Context?) : TtsCallBack, AMapNaviListener {
    private val mContext: Context? = context?.applicationContext
    var ttsSystem: TtsSystem? = TtsSystem.getInstance(mContext)
    var ttsIFly: TtsIFly? = TtsIFly.getInstance(mContext)
    var tts: Tts? = ttsIFly
        set(tts) {
            field = tts
            field?.setCallback(this)
        }
    private val wordList: LinkedList<String?> = LinkedList()
    private val ttsPlay: Int = 1
    private val ttsPlayCheck: Int = 2
    private var ttsHandler: Handler? = null
        get() = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    ttsPlay -> if (wordList.size > 0) tts?.playText(wordList.removeFirst())
                    ttsPlayCheck -> if (tts?.isPlaying() != true) field?.obtainMessage(1)
                        ?.sendToTarget()
                }
            }
        }

    fun init() {
        ttsSystem?.initTts()
        ttsIFly?.initTts()
        tts?.setCallback(this)
    }

    fun stopSpeaking() {
        ttsSystem?.stopSpeak()
        ttsIFly?.stopSpeak()
        wordList.clear()
    }

    companion object {
        private var ttsManager: TtsController? = null
        fun getInstance(context: Context?): TtsController? {
            if (ttsManager == null) ttsManager = TtsController(context)
            return ttsManager
        }
    }

    fun destroy() {
        ttsSystem?.destroy()
        ttsIFly?.destroy()
        ttsManager = null
    }

    override fun onCompleted(code: Int) {
        ttsHandler?.obtainMessage(1)?.sendToTarget()
    }

    override fun onInitNaviFailure() {}
    override fun onInitNaviSuccess() {}
    override fun onStartNavi(type: Int) {}
    override fun onTrafficStatusUpdate() {}
    override fun onLocationChange(location: AMapNaviLocation?) {}
    override fun onGetNavigationText(type: Int, text: String?) {
        wordList.addLast(text)
        ttsHandler?.obtainMessage(ttsPlayCheck)?.sendToTarget()
    }

    @Deprecated("")
    override fun onGetNavigationText(playText: String?) {
    }

    override fun onEndEmulatorNavi() {}
    override fun onArriveDestination() {}

    @Deprecated("")
    override fun onCalculateRouteFailure(errorInfo: Int) = wordList.addLast("路线规划失败")
    override fun onReCalculateRouteForYaw() = wordList.addLast("路线重新规划")
    override fun onReCalculateRouteForTrafficJam() = wordList.addLast("前方路线拥堵，路线重新规划")
    override fun onArrivedWayPoint(wayID: Int) {}
    override fun onGpsOpenStatus(enabled: Boolean) {}
    override fun onNaviInfoUpdate(naviInfo: NaviInfo?) {}
    override fun updateCameraInfo(aMapCameraInfos: Array<AMapNaviCameraInfo?>?) {}
    override fun updateIntervalCameraInfo(
        var1: AMapNaviCameraInfo?, var2: AMapNaviCameraInfo?, var3: Int
    ) {
    }

    override fun onServiceAreaUpdate(amapServiceAreaInfos: Array<AMapServiceAreaInfo?>?) {}
    override fun showCross(aMapNaviCross: AMapNaviCross?) {}
    override fun hideCross() {}
    override fun showModeCross(aMapModelCross: AMapModelCross?) {}
    override fun hideModeCross() {}

    @Deprecated("")
    override fun showLaneInfo(
        laneInfos: Array<AMapLaneInfo?>?, laneBackgroundInfo: ByteArray?,
        laneRecommendedInfo: ByteArray?
    ) {
    }

    override fun showLaneInfo(aMapLaneInfo: AMapLaneInfo?) {}
    override fun hideLaneInfo() {}

    @Deprecated("")
    override fun onCalculateRouteSuccess(ints: IntArray?) {
    }

    @Deprecated("")
    override fun notifyParallelRoad(i: Int) {
    }

    @Deprecated("")
    override fun OnUpdateTrafficFacility(aMapNaviTrafficFacilityInfos: Array<AMapNaviTrafficFacilityInfo?>?) {
    }

    @Deprecated("")
    override fun OnUpdateTrafficFacility(aMapNaviTrafficFacilityInfo: AMapNaviTrafficFacilityInfo?) {
    }

    @Deprecated("")
    override fun updateAimlessModeStatistics(aimLessModeStat: AimLessModeStat?) {
    }

    @Deprecated("")
    override fun updateAimlessModeCongestionInfo(aimLessModeCongestionInfo: AimLessModeCongestionInfo?) {
    }

    override fun onPlayRing(i: Int) {}
    override fun onCalculateRouteSuccess(aMapCalcRouteResult: AMapCalcRouteResult?) {}
    override fun onCalculateRouteFailure(aMapCalcRouteResult: AMapCalcRouteResult?) {}
    override fun onNaviRouteNotify(aMapNaviRouteNotifyData: AMapNaviRouteNotifyData?) {}
    override fun onGpsSignalWeak(boolean: Boolean) {}
}