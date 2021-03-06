package top.autoget.automap

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import com.amap.api.navi.enums.AimLessMode
import com.amap.api.navi.model.*
import com.amap.api.navi.view.RouteOverLay
import top.autoget.autokit.ToastKit.showLong
import top.autoget.automap.databinding.ActivityNaviBasicBinding

class MapNaviDriveActivity : MapNaviActivity() {
    var isTurn: Boolean = false
    var isDirection: Boolean = false
    var isTrafficButton: Boolean = false
    var isTrafficProgress: Boolean = false
    var isTrafficBar: Boolean = false
    var isWay: Boolean = false
    var isZoomIn: Boolean = false
    var isZoom: Boolean = false
    var isOverview: Boolean = false
    var isMenu: Boolean = false
    var isRoute: Boolean = false
    var isTexture: Boolean = false
    var isCar: Boolean = false
    private val activityNaviBasicBinding: ActivityNaviBasicBinding =
        ActivityNaviBasicBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navi_basic)
        when {
            isHub -> {
                aMapHudView = findViewById(R.id.hudview)
                aMapHudView?.onCreate(savedInstanceState)
                aMapHudView?.setHudViewListener(this)
            }
            else -> {
                aMapNaviView = findViewById(R.id.navi_view)
                aMapNaviView?.onCreate(savedInstanceState)
                aMapNaviView?.setAMapNaviViewListener(this)
            }
        }
        aMapNaviView?.apply {
            if (isTurn) lazyNextTurnTipView = activityNaviBasicBinding.nextTurnTipView
            if (isDirection) lazyDirectionView = activityNaviBasicBinding.directionView
            if (isTrafficButton) lazyTrafficButtonView = activityNaviBasicBinding.trafficButtonView
            if (isTrafficProgress) activityNaviBasicBinding.trafficProgressBar.apply {
                setUnknownTrafficColor(Color.WHITE)
                setSmoothTrafficColor(Color.GREEN)
                setSlowTrafficColor(Color.YELLOW)
                setJamTrafficColor(Color.DKGRAY)
                setVeryJamTrafficColor(Color.BLACK)
            }
            if (isWay) lazyDriveWayView = activityNaviBasicBinding.driveWayView
            if (isZoomIn) lazyZoomInIntersectionView =
                activityNaviBasicBinding.zoomInIntersectionView
            if (isZoom) setLazyZoomButtonView(activityNaviBasicBinding.zoomButtonView)
            if (isOverview) setLazyOverviewButtonView(activityNaviBasicBinding.overviewButtonView)
        }
        aMapNaviView?.viewOptions?.apply {
            if (isMenu) isSettingMenuEnabled = true
            if (isRoute || isTexture) isAutoDrawRoute = false
            if (isTexture) routeOverlayOptions = RouteOverlayOptions().apply {
                arrowOnTrafficRoute =
                    BitmapFactory.decodeResource(resources, R.mipmap.custtexture_aolr)
                normalRoute = BitmapFactory.decodeResource(resources, R.mipmap.custtexture)
                unknownTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_no)
                smoothTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_green)
                slowTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_slow)
                jamTraffic = BitmapFactory.decodeResource(resources, R.mipmap.custtexture_bad)
                veryJamTraffic =
                    BitmapFactory.decodeResource(resources, R.mipmap.custtexture_grayred)
            }
            if (isTrafficProgress) isTrafficBarEnabled = isTrafficProgress
            if (isCar) {
                carBitmap = BitmapFactory.decodeResource(resources, R.mipmap.car)
                fourCornersBitmap = BitmapFactory.decodeResource(resources, R.mipmap.lane00)
                setStartPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.navi_start))
                setWayPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.navi_way))
                setEndPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.navi_end))
            }
            if (isAimless) mapView = activityNaviBasicBinding.map
        }
    }

    private val isOrientationLandscape: Boolean =
        requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onGpsSignalWeak(boolean: Boolean) {}
    fun setCarNumber(number: String) =
        aMapNavi?.setCarInfo(AMapCarInfo().apply { carNumber = number })

    val calculateDriveRoute = try {
        aMapNavi?.run {
            calculateDriveRoute(
                startPoints, endPoints, wayPoints, strategyConvert(true, false, false, false, false)
            )//???????????????????????????A???????????????B???????????????AB???????????????
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    override fun onInitNaviSuccess() {
        super.onInitNaviSuccess()
        when {
            isAimless -> aMapNavi?.startAimlessMode(AimLessMode.CAMERA_AND_SPECIALROAD_DETECTED)
            else -> calculateDriveRoute
        }
    }

    override fun onTrafficStatusUpdate() {
        super.onTrafficStatusUpdate()
    }

    private var remainingDistance: Int = 0
    override fun onNaviInfoUpdate(naviInfo: NaviInfo?) {
        super.onNaviInfoUpdate(naviInfo)
        when {
            isTrafficBar -> remainingDistance = naviInfo?.pathRetainDistance ?: 0
            isTrafficProgress -> naviInfo?.let {
                aMapNavi?.run {
                    activityNaviBasicBinding.trafficProgressBar
                        .update(naviPath.allLength, it.pathRetainDistance, getTrafficStatuses(0, 0))
                }
            }
        }
    }

    private var array: Array<String?> = arrayOf(
        "????????????",
        "????????????",
        "?????????????????????",
        "????????????",
        "?????????????????????",
        "???????????????",
        "????????????????????????",
        " ??????????????????????????????",
        "??????????????????",
        "???????????????????????????",
        "???????????????????????????",
        "????????????????????????",
        "????????????????????????",
        "???",
        "???",
        "????????????????????????"
    )
    private var actions: Array<String?> = arrayOf(
        "??????",
        "??????",
        "???????????????",
        "??????",
        "???????????????",
        "?????????",
        "??????????????????",
        " ????????????????????????",
        "????????????",
        "?????????????????????",
        "?????????????????????",
        "??????????????????",
        "??????????????????",
        "???",
        "???",
        "???????????????"
    )
    private val Char.toHex: Int
        get() = when (this) {
            in '0'..'9' -> (this - '0')
            in 'a'..'f' -> (this - 'a' + 10)
            in 'A'..'F' -> (this - 'A' + 10)
            else -> throw Exception("error param")
        }

    override fun showLaneInfo(
        laneInfos: Array<AMapLaneInfo?>?,
        laneBackgroundInfo: ByteArray?, laneRecommendedInfo: ByteArray?
    ) {
        super.showLaneInfo(laneInfos, laneBackgroundInfo, laneRecommendedInfo)
        if (isWay) StringBuffer().apply {
            laneInfos?.let {
                append("???${laneInfos.size}??????")
                for ((index, value) in laneInfos.withIndex()) {
                    try {
                        value?.run { append("??????${index + 1}?????????${array[laneTypeIdArray[0].toHex]}??????????????????????????????${actions[laneTypeIdArray[1].toHex]}") }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }.run { showLong(toString()) }
    }

    private var aMapNaviPath: AMapNaviPath? = null
    override fun onCalculateRouteSuccess(aMapCalcRouteResult: AMapCalcRouteResult?) {
        if (isRoute) IntArray(10).apply {
            this[0] = Color.BLACK
            this[1] = Color.RED
            this[2] = Color.BLUE
            this[3] = Color.YELLOW
            this[4] = Color.GRAY
        }.let {
            RouteOverLay(aMapNaviView?.map, aMapNavi?.naviPath, this).apply {
                setStartPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.r1))
                setEndPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.b1))
                setWayPointBitmap(BitmapFactory.decodeResource(resources, R.mipmap.b2))
                isTrafficLine = false
            }.addToMap(it, aMapNavi?.naviPath?.wayPointIndex)//??????width = 30f
        }
        super.onCalculateRouteSuccess(aMapCalcRouteResult)
    }
}