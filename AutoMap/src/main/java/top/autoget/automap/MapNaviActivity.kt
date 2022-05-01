package top.autoget.automap

import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.navi.*
import com.amap.api.navi.AMapNavi.destroy
import com.amap.api.navi.enums.NaviType
import com.amap.api.navi.model.*
import com.amap.api.navi.view.RouteOverLay
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.ToastKit.showLong
import top.autoget.autokit.ToastKit.showShort
import top.autoget.autokit.debug
import top.autoget.autokit.info
import java.util.*

open class MapNaviActivity : LoggerKit, AppCompatActivity(),
    AMapNaviListener, AMapNaviViewListener, AMapHudViewListener {
    var startStr: String? = null
    var endStr: String? = null
    private val String?.toNaviLatLng: NaviLatLng?
        get() = try {
            this?.run {
                NaviLatLng(
                    split(",").toTypedArray()[0].toDouble(),
                    split(",").toTypedArray()[1].toDouble()
                )
            }
        } catch (e: Exception) {
            showShort("e:$e")
            showShort("格式:[lat],[lon]")
            null
        }
    protected var startLatLng: NaviLatLng? = startStr.toNaviLatLng
    protected var endLatLng: NaviLatLng? = endStr.toNaviLatLng
    protected val startPoints: MutableList<NaviLatLng?> =
        mutableListOf<NaviLatLng?>().apply { add(startLatLng) }
    protected val endPoints: MutableList<NaviLatLng?> =
        mutableListOf<NaviLatLng?>().apply { add(endLatLng) }
    var wayPoints: MutableList<NaviLatLng?> = mutableListOf()
    protected var ttsController: TtsController? = null
    var isGps: Boolean = true
    protected var aMapNavi: AMapNavi? = null
    var isAimless: Boolean = false
    private var needFollowTimer: Timer? = null
    private val clearTimer = needFollowTimer?.run {
        cancel()
        needFollowTimer = null
    }
    private var isNeedFollow: Boolean = true
    private val delayTime: Long = 5000
    private val startTimerSomeTimeLater = {
        clearTimer
        needFollowTimer = Timer()
        needFollowTimer?.schedule(object : TimerTask() {
            override fun run() {
                isNeedFollow = true
            }
        }, delayTime)
    }
    private var aMap: AMap? = null
    private val setMapInteractiveListener = {
        aMap?.setOnMapTouchListener { event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    clearTimer
                    isNeedFollow = false
                }
                MotionEvent.ACTION_UP -> startTimerSomeTimeLater
            }
        }
    }
    protected var mapView: MapView? = null
    private var myLocationMarker: Marker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        ttsController = TtsController.getInstance(applicationContext)
        ttsController?.init()
        isGps = intent.getBooleanExtra("gps", true)
        aMapNavi = AMapNavi.getInstance(applicationContext).apply {
            addAMapNaviListener(this@MapNaviActivity)
            addAMapNaviListener(ttsController)
            if (!isGps) setEmulatorNaviSpeed(75)
            isUseExtraGPSData = true
            showLong("点击右下角menu按钮实现设置外部GPS")
        }
        if (isAimless) {
            mapView?.onCreate(savedInstanceState)
            aMap = mapView?.map
            myLocationMarker = BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.car)
            ).let { aMap?.addMarker(MarkerOptions().icon(it)) }
            setMapInteractiveListener
        }
    }

    var isHub: Boolean = false
    protected var aMapHudView: AMapHudView? = null
    protected var aMapNaviView: AMapNaviView? = null
    override fun onResume() {
        super.onResume()
        if (isHub) aMapHudView?.onResume() else aMapNaviView?.onResume()
        startPoints.add(startLatLng)
        endPoints.add(endLatLng)
        if (isAimless) mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (isHub) aMapHudView?.onPause() else aMapNaviView?.onPause()
        ttsController?.stopSpeaking()//仅停止当前这句话，新路口会再说。
        //aMapNavi?.stopNavi()//停止后会触及底层stop，不会再有回调，但当前没有说完的话会说完
        if (isAimless) mapView?.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isAimless) mapView?.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        when {
            isHub -> {
                aMapHudView?.onDestroy()
                aMapHudView = null
            }
            else -> {
                aMapNaviView?.onDestroy()
                aMapNaviView = null
            }
        }
        aMapNavi?.apply {
            if (isAimless) stopAimlessMode() else stopNavi()
            removeAMapNaviListener(this@MapNaviActivity)
            removeAMapNaviListener(ttsController)
            destroy()//不在destroy时自动执行stopNavi
        }
        ttsController?.destroy()
        if (isAimless) mapView?.onDestroy()
    }

    private val stopNavi = {
        aMapNavi?.stopNavi()
        ttsController?.stopSpeaking()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopNavi
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    val northUp = aMapNaviView?.apply { naviMode = AMapNaviView.NORTH_UP_MODE }
    val carUp = aMapNaviView?.apply { naviMode = AMapNaviView.CAR_UP_MODE }
    val overview = aMapNaviView?.displayOverview()
    val goOnNavi = aMapNaviView?.recoverLockMode()
    override fun onInitNaviFailure() {
        showShort("init navi Failed")
    }//初始化导航失败回调

    override fun onInitNaviSuccess() {}//初始化导航成功回调
    override fun onStartNavi(type: Int) {}//开始导航回调
    override fun onTrafficStatusUpdate() {}//交通状态更新回调
    override fun onLocationChange(location: AMapNaviLocation?) {
        if (isAimless) location?.coord?.run { LatLng(latitude, longitude) }?.let {
            myLocationMarker?.position = it
            if (isNeedFollow) aMap?.animateCamera(CameraUpdateFactory.changeLatLng(it))
        } ?: showShort("定位出现异常")
    }//定位改变回调

    override fun onGetNavigationText(type: Int, text: String?) {
    }//获取导航文本（播报类型和播报文字）回调

    @Deprecated("")
    override fun onGetNavigationText(playText: String?) {
    }//获取导航文本回调

    override fun onEndEmulatorNavi() {}//结束模拟导航回调
    override fun onArriveDestination() {}//到达目的地回调
    private val errorInfoMap: MutableMap<Int?, String?> = mutableMapOf(
        Pair(-1, "路径计算失败，在导航过程中调用calculateDriveRoute方法导致的失败，导航过程中只能用reCalculate方法进行路径计算。"),
        Pair(1, "路径计算成功。"),
        Pair(2, "网络超时或网络失败，请检查网络是否通畅，如网络没问题，查看Logcat输出是否出现鉴权错误信息，如有，说明SHA1与KEY不对应导致。"),
        Pair(3, "路径规划起点经纬度不合法，请选择国内坐标点，确保经纬度格式正常。"),
        Pair(4, "协议解析错误，请稍后再试。"),
        Pair(6, "路径规划终点经纬度不合法，请选择国内坐标点，确保经纬度格式正常。"),
        Pair(7, "算路服务端编码失败。"),
        Pair(10, "起点附近没有找到可行道路，请对起点进行调整。"),
        Pair(11, "终点附近没有找到可行道路，请对终点进行调整。"),
        Pair(12, "途经点附近没有找到可行道路,请对途经点进行调整。"),
        Pair(13, "key鉴权失败，请仔细检查key绑定的sha1值与apk签名sha1值是否对应，或通过;高频问题查找相关解决办法。"),
        Pair(14, "请求的服务不存在，请稍后再试。"),
        Pair(15, "请求服务响应错误，请检查网络状况，稍后再试。"),
        Pair(16, "无权限访问此服务，请稍后再试。"),
        Pair(17, "请求超出配额。"),
        Pair(18, "请求参数非法，请检查传入参数是否符合要求。"),
        Pair(19, "未知错误。")
    )

    @Deprecated("")
    override fun onCalculateRouteFailure(errorInfo: Int) {
        error("$loggerTag->--------------------------------------------")
        info("$loggerTag->路线计算失败：错误码=$errorInfo,Error Message= ${errorInfoMap[errorInfo]}")
        info("$loggerTag->错误码详细链接见：http://lbs.amap.com/api/android-navi-sdk/guide/tools/errorcode/")
        error("$loggerTag->--------------------------------------------")
        showLong("errorInfo：$errorInfo,Message：${errorInfoMap[errorInfo]}")
    }//计算路线失败回调

    @Deprecated("")
    override fun onReCalculateRouteForYaw() {
    }//偏航后重新计算路线回调

    @Deprecated("")
    override fun onReCalculateRouteForTrafficJam() {
    }//拥堵后重新计算路线回调

    override fun onArrivedWayPoint(wayID: Int) {}//到达途径点回调
    override fun onGpsOpenStatus(enabled: Boolean) {}//GPS开关状态回调
    override fun onNaviInfoUpdate(naviInfo: NaviInfo?) {}//导航信息更新回调
    override fun updateCameraInfo(aMapCameraInfos: Array<AMapNaviCameraInfo?>?) {}//更新相机信息回调
    override fun updateIntervalCameraInfo(
        var1: AMapNaviCameraInfo?, var2: AMapNaviCameraInfo?, var3: Int
    ) {
    }//更新间隔相机信息回调

    override fun onServiceAreaUpdate(amapServiceAreaInfos: Array<AMapServiceAreaInfo?>?) {}//服务区域更新回调
    override fun showCross(aMapNaviCross: AMapNaviCross?) {}//显示转弯回调
    override fun hideCross() {}//隐藏转弯回调
    override fun showModeCross(aMapModelCross: AMapModelCross?) {}//显示模式转弯回调
    override fun hideModeCross() {}//隐藏模式转弯回调

    @Deprecated("")
    override fun showLaneInfo(
        laneInfos: Array<AMapLaneInfo?>?, laneBackgroundInfo: ByteArray?,
        laneRecommendedInfo: ByteArray?
    ) {
    }//显示车道信息

    override fun showLaneInfo(aMapLaneInfo: AMapLaneInfo?) {}//显示车道信息
    override fun hideLaneInfo() {}//隐藏车道信息

    @Deprecated("")
    override fun onCalculateRouteSuccess(ints: IntArray?) {
    }//多路径算路成功回调

    @Deprecated("")
    override fun notifyParallelRoad(i: Int) = when (i) {
        0 -> {
            showShort("当前在主辅路过渡")
            debug("$loggerTag->当前在主辅路过渡")
        }
        1 -> {
            showShort("当前在主路")
            debug("$loggerTag->当前在主路")
        }
        2 -> {
            showShort("当前在辅路")
            debug("$loggerTag->当前在辅路")
        }
        else -> {
        }
    }//通知平行道路回调

    @Deprecated("")
    override fun OnUpdateTrafficFacility(aMapNaviTrafficFacilityInfos: Array<AMapNaviTrafficFacilityInfo?>?) {
        if (isAimless) aMapNaviTrafficFacilityInfos?.run {
            for (aMapNaviTrafficFacilityInfo in this) {
                aMapNaviTrafficFacilityInfo?.run {
                    showShort("(trafficFacilityInfo.coor_X+coor_Y+distance+limitSpeed):${coorX + coorY + getDistance() + getLimitSpeed()}")
                }
            }
        }
    }//更新交通设施信息回调

    @Deprecated("")
    override fun OnUpdateTrafficFacility(aMapNaviTrafficFacilityInfo: AMapNaviTrafficFacilityInfo?) {
        if (isAimless) aMapNaviTrafficFacilityInfo?.run {
            showShort("(trafficFacilityInfo.coor_X+coor_Y+distance+limitSpeed):${coorX + coorY + getDistance() + getLimitSpeed()}")
        }
    }//更新交通设施信息回调

    @Deprecated("")
    override fun updateAimlessModeStatistics(aimLessModeStat: AimLessModeStat?) {
        if (isAimless) {
            showShort("看log")
            aimLessModeStat?.run {
                debug("distance=$aimlessModeDistance")
                debug("time=$aimlessModeTime")
            }
        }
    }//更新巡航模式统计信息

    @Deprecated("")
    override fun updateAimlessModeCongestionInfo(aimLessModeCongestionInfo: AimLessModeCongestionInfo?) {
        if (isAimless) {
            showShort("看log")
            aimLessModeCongestionInfo?.run {
                debug("roadName=$roadName")
                debug("CongestionStatus=$congestionStatus")
                debug("eventLonLat=$eventLon,$eventLat")
                debug("length=$length")
                debug("time=$time")
                for (aMapCongestionLink in amapCongestionLinks) {
                    aMapCongestionLink?.run {
                        debug("status=$congestionStatus")
                        for (naviLatLng in coords) {
                            debug("$naviLatLng")
                        }
                    }
                }
            }
        }
    }//更新巡航模式拥堵信息

    override fun onPlayRing(i: Int) {}//打圈回调
    fun showRoute(aMap: AMap? = null) =
        RouteOverLay(aMap, aMapNavi?.naviPath, applicationContext).addToMap()

    override fun onCalculateRouteSuccess(aMapCalcRouteResult: AMapCalcRouteResult?) {
        showRoute()
        aMapNavi?.naviGuideList?.run {
            for (guide in this) {
                guide.run {
                    debug("AMapNaviGuide 路线经纬度:$coord")
                    debug("AMapNaviGuide 路线名:$name")
                    debug("AMapNaviGuide 路线长:${length}m")
                    debug("AMapNaviGuide 路线耗时:${time}s")
                    debug("AMapNaviGuide 路线IconType$iconType")
                    debug("AMapNaviGuide 路段step开始索引$startSegId")
                    debug("AMapNaviGuide 路段step数量$segCount")
                }
            }
        }
        aMapNavi?.run { if (isGps) startNavi(NaviType.GPS) else startNavi(NaviType.EMULATOR) }
    }//多路径算路成功回调

    override fun onCalculateRouteFailure(aMapCalcRouteResult: AMapCalcRouteResult?) {}//计算路线失败回调
    override fun onNaviRouteNotify(aMapNaviRouteNotifyData: AMapNaviRouteNotifyData?) {}//导航路线通知回调
    override fun onGpsSignalWeak(boolean: Boolean) {}
    var i: Int = 0
    val setExtraGPSData = Location("gps仪器型号").apply {
        longitude = 116.4 - 0.01 * i
        latitude = 39.9
        speed = 5f
        accuracy = 1f
        bearing = 5f
        time = nowMillis
    }.run {
        aMapNavi?.setExtraGPSData(1, this)//1为GPS坐标，2为高德坐标
        showShort(toString())
        i++
    }//缺一不可

    override fun onNaviSetting() {
        setExtraGPSData
    }//底部导航设置点击回调

    override fun onNaviCancel() {
        stopNavi
        finish()
    } //导航取消回调

    override fun onNaviBackClick(): Boolean = false//导航返回点击回调
    override fun onNaviMapMode(isLock: Int) {}//导航地图模式（锁屏或锁车）回调
    override fun onNaviTurnClick() {}//导航转弯视图点击回调
    override fun onNextRoadClick() {}//下一个道路视图点击回调
    override fun onScanViewButtonClick() {}//全览视图按钮点击回调
    override fun onLockMap(isLock: Boolean) {}//锁地图状态发生变化时回调
    override fun onNaviViewLoaded() {
        debug("$loggerTag->导航页面加载成功")
        debug("$loggerTag->请不要使用AMapNaviView.getMap().setOnMapLoadedListener()，会overwrite导航SDK内部画线逻辑")
    }//导航地图已加载回调

    override fun onMapTypeChanged(p0: Int) {}//地图模式已改变回调
    override fun onNaviViewShowMode(p0: Int) {}//导航视图显示模式回调
    override fun onHudViewCancel() {
        stopNavi
        finish()
    }//Hub视图取消回调
}