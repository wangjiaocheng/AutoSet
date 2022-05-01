package top.autoget.automap

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetManager
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.amap.api.fence.GeoFence
import com.amap.api.fence.GeoFenceClient
import com.amap.api.location.*
import com.amap.api.maps.*
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.ScaleAnimation
import com.amap.api.maps.model.animation.TranslateAnimation
import com.amap.api.maps.offlinemap.OfflineMapCity
import com.amap.api.maps.offlinemap.OfflineMapManager
import com.amap.api.maps.offlinemap.OfflineMapProvince
import com.amap.api.maps.offlinemap.OfflineMapStatus
import com.amap.api.maps.utils.SpatialRelationUtil
import com.amap.api.maps.utils.overlay.SmoothMoveMarker
import com.amap.api.services.busline.*
import com.amap.api.services.cloud.CloudItem
import com.amap.api.services.cloud.CloudItemDetail
import com.amap.api.services.cloud.CloudResult
import com.amap.api.services.cloud.CloudSearch
import com.amap.api.services.cloud.CloudSearch.*
import com.amap.api.services.core.*
import com.amap.api.services.core.AMapException
import com.amap.api.services.district.DistrictItem
import com.amap.api.services.district.DistrictResult
import com.amap.api.services.district.DistrictSearch
import com.amap.api.services.district.DistrictSearchQuery
import com.amap.api.services.geocoder.*
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import com.amap.api.services.share.ShareSearch
import com.amap.api.services.weather.LocalWeatherForecastResult
import com.amap.api.services.weather.LocalWeatherLiveResult
import com.amap.api.services.weather.WeatherSearch
import com.amap.api.services.weather.WeatherSearchQuery
import com.amap.api.trace.*
import org.json.JSONException
import org.json.JSONObject
import top.autoget.autokit.*
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.DateKit.sdfDateByFullX
import top.autoget.autokit.DensityKit.dip2px
import top.autoget.autokit.FileIoKit.writeFileFromIS
import top.autoget.autokit.FileKit.createDirNone
import top.autoget.autokit.HandleKit.backgroundHandler
import top.autoget.autokit.ImageKit.compressByScale
import top.autoget.autokit.PathKit.pathExternal
import top.autoget.autokit.StringKit.isEmptyTrim
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.ToastKit.showShort
import top.autoget.automap.MapCommon.toLatLng
import top.autoget.automap.MapErrorToast.showError
import top.autoget.automap.MapErrorToast.showTvToast
import top.autoget.automap.databinding.ActivityMapBinding
import top.autoget.automap.databinding.BuslineDialogBinding
import java.io.*
import java.net.URL
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MapActivity : AppCompatActivity(), LoggerKit, AMap.OnMapScreenShotListener,
    BusLineSearch.OnBusLineSearchListener, BusStationSearch.OnBusStationSearchListener,
    OnCloudSearchListener, AMap.InfoWindowAdapter, AMap.OnInfoWindowClickListener,
    GeocodeSearch.OnGeocodeSearchListener,
    DistrictSearch.OnDistrictSearchListener, AdapterView.OnItemSelectedListener,
    View.OnClickListener, TraceStatusListener, TraceListener, SensorEventListener {
    private var activityMapBinding: ActivityMapBinding? = null
    private var aMap: AMap? = null
    private var uiSettings: UiSettings? = null
    private var map: TextureMapView? = activityMapBinding?.map
    private val initMap = {
        if (aMap == null) aMap = map?.map
    }
    private val initUiSettings = {
        if (uiSettings == null) uiSettings = aMap?.uiSettings
    }
    private val init = {
        initMap
        initUiSettings
        selectMapCustomStyleFile()
        selectTypeMap()
        selectTypeLocation
        selectTypeLanguage()
        selectPositionLogo()
        selectPositionControls()
    }

    fun selectMapCustomStyleFile(isDark: Boolean = false) {
        val styleData = if (isDark) "style_dark.data" else "style.data"
        val styleExtraData = if (isDark) "style_extra_dark.data" else "style_extra.data"
        val texturesZip = if (isDark) "textures_dark.zip" else "textures.zip"
        var filePath: String? = null
        try {
            filePath = filesDir.absolutePath
            assets.open(styleData).use { writeFileFromIS("$filePath/$styleData", it) }
            assets.open(styleExtraData).use { writeFileFromIS("$filePath/$styleExtraData", it) }
            assets.open(texturesZip).use { writeFileFromIS("$filePath/$texturesZip", it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        CustomMapStyleOptions().setEnable(true)
            .setStyleDataPath("$filePath/$styleData").setStyleExtraPath("$filePath/$styleExtraData")
            .setStyleTexturePath("$filePath/$texturesZip")
            .let { aMap?.apply { setCustomMapStyle(it) }?.showMapText(false) }
    }

    fun selectTypeMap(type: Int = AMap.MAP_TYPE_NORMAL) = aMap?.apply { mapType = type }
    var selectTypeLocationSpinner: Spinner? = null//TODO
    private val selectTypeLocation = {
        arrayOf("展示", "定位", "追随", "旋转", "旋转位置", "追随不移动到中心点", "旋转不移动到中心点", "旋转位置不移动到中心点").let {
            selectTypeLocationSpinner?.adapter =
                ArrayAdapter(this, android.R.layout.simple_spinner_item, it)
                    .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }
        selectTypeLocationSpinner?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    when (position) {
                        0 -> aMap?.myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
                        1 -> aMap?.myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
                        2 -> aMap?.myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
                        3 -> aMap?.myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE)
                        4 -> aMap?.myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE)
                        5 -> aMap?.myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER)
                        6 -> aMap?.myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER)
                        7 -> aMap?.myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                    }
                }
            }
        aMap?.setOnMyLocationChangeListener { location ->
            location?.run {
                error("$loggerTag->定位成功：lat: $latitude；lon: $longitude")
                extras?.run {
                    error(
                        """$loggerTag->定位信息：
                        |errorCode: ${getInt(MyLocationStyle.ERROR_CODE)}；
                        |errorInfo: ${getString(MyLocationStyle.ERROR_INFO)}；
                        |locationType: ${getInt(MyLocationStyle.LOCATION_TYPE)}""".trimMargin()
                    )
                } ?: error("$loggerTag->定位信息：bundle is null")
            } ?: error("$loggerTag->定位失败")
        }
    }

    fun selectTypeLanguage(language: String = AMap.CHINESE) =
        aMap?.apply { setMapLanguage(language) }//CHINESE、ENGLISH

    fun selectPositionLogo(position: Int = AMapOptions.LOGO_POSITION_BOTTOM_LEFT) =
        aMap?.uiSettings?.apply { logoPosition = position }//LEFT、CENTER、RIGHT

    fun selectPositionControls(position: Int = AMapOptions.ZOOM_POSITION_RIGHT_CENTER) =
        aMap?.uiSettings?.apply { zoomPosition = position }//RIGHT_BOTTOM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMapBinding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(activityMapBinding?.root)
        MapsInitializer.sdcardDir =
            "${pathExternal}amapsdk/offlineMap/".apply { createDirNone(this) }
        map?.onCreate(savedInstanceState)//必须重写
        init
        manager = sensorManager
        AKit.app.registerReceiver(alarmReceiver, IntentFilter().apply { addAction("LOCATION") })
        AKit.app.registerReceiver(geoFenceReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
                .apply { addAction(GEO_FENCE_BROADCAST_ACTION) })
    }

    override fun onResume() {
        super.onResume()
        map?.onResume()
    }//必须重写

    override fun onPause() {
        super.onPause()
        map?.onPause()
    }//必须重写

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        map?.onSaveInstanceState(outState)
    }//必须重写

    override fun onDestroy() {
        super.onDestroy()
        map?.onDestroy()
        executorService?.shutdownNow()
        alarmReceiver?.let {
            AKit.app.unregisterReceiver(it)
            alarmReceiver = null
        }
        geoFenceReceiver?.let {
            AKit.app.unregisterReceiver(it)
            geoFenceReceiver = null
        }
    }//必须重写

    val changeStop = aMap?.stopAnimation()

    @JvmOverloads
    fun changeCamera(
        update: CameraUpdate, isAnimation: Boolean = true, duration: Long = 1000,
        callback: AMap.CancelableCallback? = object : AMap.CancelableCallback {
            override fun onFinish() {}//TODO
            override fun onCancel() {}//TODO
        }
    ) = aMap?.run {
        when {
            isAnimation -> animateCamera(update, duration, callback)
            else -> moveCamera(update)
        }
    }

    @JvmOverloads
    fun changePosition(
        position: LatLng, zoom: Float = 18f, tilt: Float = 30f, bearing: Float = 0f,
        hue: Float = BitmapDescriptorFactory.HUE_RED
    ) {
        CameraUpdateFactory.newCameraPosition(CameraPosition(position, zoom, tilt, bearing))
            .run { changeCamera(this) }
        MarkerOptions().position(position).icon(BitmapDescriptorFactory.defaultMarker(hue))
            .let { aMap?.apply { clear() }?.addMarker(it) }
    }//HUE_RED、HUE_ORANGE、HUE_YELLOW、HUE_GREEN、HUE_CYAN、HUE_AZURE、HUE_BLUE、HUE_VIOLET、HUE_MAGENTA、HUE_ROSE

    enum class Direction { LEFT, RIGHT, UP, DOWN }

    @JvmOverloads
    fun changeScroll(direction: Direction, px: Float = 100f) = when (direction) {
        Direction.LEFT -> changeCamera(CameraUpdateFactory.scrollBy(-px, 0f))
        Direction.RIGHT -> changeCamera(CameraUpdateFactory.scrollBy(px, 0f))
        Direction.UP -> changeCamera(CameraUpdateFactory.scrollBy(0f, -px))
        Direction.DOWN -> changeCamera(CameraUpdateFactory.scrollBy(0f, px))
    }

    fun changeLimits(latLngA: LatLng, latLngB: LatLng) = aMap?.apply {
        setMapStatusLimits(LatLngBounds(latLngA, latLngB))
        addMarker(MarkerOptions().position(latLngA))
        addMarker(MarkerOptions().position(latLngB))
        changeCamera(CameraUpdateFactory.zoomTo(8f))
    }

    val zoomIn = changeCamera(CameraUpdateFactory.zoomIn())
    val zoomOut = changeCamera(CameraUpdateFactory.zoomOut())
    fun zoomLevel(
        current: Float = 18f, max: Float = 20f, min: Float = 3f,
        reset: Boolean = false
    ) = aMap?.apply {
        maxZoomLevel = max
        minZoomLevel = min
        changeCamera(CameraUpdateFactory.zoomTo(current))
        info("currentZoomLevel->${cameraPosition.zoom}")
        if (reset) resetMinMaxZoomPreference()//3..19|20
    }

    @JvmOverloads
    fun showUiSettings(
        scroll: Boolean = true, zoom: Boolean = true, tilt: Boolean = true, rotate: Boolean = true,
        scale: Boolean = true, zoomControls: Boolean = true,
        compass: Boolean = true, location: Boolean = true, indoor: Boolean = true
    ) = aMap?.apply {
        uiSettings?.apply {
            isScrollGesturesEnabled = scroll//拖拽
            isZoomGesturesEnabled = zoom//缩放
            isTiltGesturesEnabled = tilt//倾斜
            isRotateGesturesEnabled = rotate//旋转
            isScaleControlsEnabled = scale//比例尺，室内地图必须
            isZoomControlsEnabled = zoomControls//缩放按钮
            isCompassEnabled = compass//指南针
            isMyLocationButtonEnabled = location//定位按钮
            isIndoorSwitchEnabled = indoor//关闭SDK自带室内地图控件
        }
        isMyLocationEnabled = location//触发定位并显示定位层
        if (location) myLocationStyle = MyLocationStyle().apply {
            myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point))
            strokeWidth(5f)
            strokeColor(Color.argb(180, 3, 145, 255))
            radiusFillColor(Color.argb(10, 0, 0, 180))
        }
    }

    @JvmOverloads
    fun showLayers(
        traffic: Boolean = true, building: Boolean = true, mapText: Boolean = true,
        indoorMap: Boolean = true
    ) = aMap?.apply {
        isTrafficEnabled = traffic
        if (traffic) myTrafficStyle = MyTrafficStyle().apply {
            seriousCongestedColor = -0x6dfff6//严重拥堵
            congestedColor = -0x15fcee//拥堵
            slowColor = -0x8af8//缓慢
            smoothColor = -0xff5df7//顺畅
        }
        showBuildings(building)
        showMapText(mapText)
        showIndoorMap(indoorMap)//默认关闭
    }

    private var indoorBuildingInfo: IndoorBuildingInfo? = null
    var mapIndoorFloorSwitchView: MapIndoorFloorSwitchView? = null//TODO
    fun showIndoor(position: LatLng) = aMap?.run {
        setOnIndoorBuildingActiveListener {
            info("$loggerTag->indoor OnIndoorBuilding $it")
            backgroundHandler.post {
                if (indoorBuildingInfo == null || indoorBuildingInfo?.poiid != it.poiid)
                    mapIndoorFloorSwitchView?.items = it.floor_names.toMutableList()
                mapIndoorFloorSwitchView?.setSelection(it.activeFloorName)
                indoorBuildingInfo = it
            }
        }
        mapIndoorFloorSwitchView?.onIndoorFloorSwitchListener = object :
            MapIndoorFloorSwitchView.OnIndoorFloorSwitchListener {
            override fun onSelected(selectedIndex: Int) {
                info("$loggerTag->indoor onselected $selectedIndex")
                indoorBuildingInfo?.apply {
                    activeFloorIndex = floor_indexs[selectedIndex]
                    activeFloorName = floor_names[selectedIndex]
                }.run { aMap?.setIndoorBuildingInfo(this) }
            }
        }
        showIndoorMap(true)//默认关闭
        uiSettings?.isIndoorSwitchEnabled = false//关闭SDK自带室内地图控件
        moveCamera(CameraUpdateFactory.newLatLngZoom(position, 19f))
    }

    fun showOpenGl(position: LatLng) =
        aMap?.apply { setCustomRenderer(MapCubeRender(aMap, position)) }?.runOnDrawFrame()

    companion object {
        const val COUNTRY = "country"//国级
        const val PROVINCE = "province"//省级
        const val CITY = "city"//市级
        const val DISTRICT = "district"//区级
        const val BUSINESS = "biz_area"//商圈级
        private const val LIST_UPDATE = 0
        private const val MSG_SHOW = 1
        private const val DIALOG_DISMISS = 2
        private const val DIALOG_SHOW = 3
        private val ALT_HEAT_MAP_GRADIENT_COLORS: IntArray = intArrayOf(
            Color.argb(0, 0, 255, 255),
            Color.argb(255 / 3 * 2, 0, 255, 0),
            Color.rgb(125, 191, 0),
            Color.rgb(185, 71, 0),
            Color.rgb(255, 0, 0)
        )
        private val ALT_HEAT_MAP_GRADIENT_START_POINTS: FloatArray =
            floatArrayOf(0.0f, 0.10f, 0.20f, 0.60f, 1.0f)
        val ALT_HEAT_MAP_GRADIENT: Gradient =
            Gradient(ALT_HEAT_MAP_GRADIENT_COLORS, ALT_HEAT_MAP_GRADIENT_START_POINTS)
        private const val MSG_LOCATION_START = 0
        private const val MSG_LOCATION_FINISH = 1
        private const val MSG_LOCATION_STOP = 2
        private const val GEO_FENCE_BROADCAST_ACTION: String =
            "top.autoget.automap.geofence.multiple"
    }

    @JvmOverloads
    fun showHeatMap(latLngList: MutableList<LatLng?> = mutableListOf()): TileOverlay? =
        HeatmapTileProvider.Builder().data(latLngList).gradient(ALT_HEAT_MAP_GRADIENT).build()
            .let { aMap?.addTileOverlay(TileOverlayOptions().tileProvider(it)) }

    private val changeZoom = aMap?.apply {
        moveCamera(CameraUpdateFactory.zoomTo(4f))
        mapTextZIndex = 2
    }

    @JvmOverloads
    fun showOverlayTile(url: String = "http://a.tile.openstreetmap.org/%d/%d/%d.png"): TileOverlay? =
        TileOverlayOptions().memoryCacheEnabled(true).memCacheSize(100000)//默认5MB
            .diskCacheEnabled(true).diskCacheSize(100000)//默认20MB
            .diskCacheDir("/storage/emulated/0/amap/cache")
            .tileProvider(object : UrlTileProvider(256, 256) {
                override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                    return try {
                        URL(String.format(url, zoom, x, y))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }).let {
                changeZoom
                aMap?.addTileOverlay(it)
            }

    @JvmOverloads
    fun showOverlayGround(latLngA: LatLng, latLngB: LatLng, res: Int = R.mipmap.groundoverlay) =
        GroundOverlayOptions().transparency(0.7f).anchor(0.5f, 0.5f)//图片对齐方式：[0,0]左上角、[1,1]右下角
            .image(BitmapDescriptorFactory.fromResource(res))
            .positionFromBounds(LatLngBounds.Builder().include(latLngA).include(latLngB).build())
            .let {
                aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngA, 18f))
                aMap?.addGroundOverlay(it)
            }

    @JvmOverloads
    fun showOverlayMultiPoint(res: Int = R.raw.point10w) {
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(3f))
        val multiPointOverlay = aMap?.addMultiPointOverlay(MultiPointOverlayOptions().apply {
            anchor(0.5f, 0.5f)
            icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_blue))
        })
        aMap?.setOnMultiPointClickListener { pointItem ->
            MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .let {
                    aMap?.addMarker(it)?.apply {
                        position = pointItem.latLng
                        setToTop()
                    }
                }
            false
        }
        showProgressDialog
        Thread {
            mutableListOf<MultiPointItem>().apply {
                try {
                    this@MapActivity.resources.openRawResource(res).use { inputStream ->
                        InputStreamReader(inputStream).use { inputStreamReader ->
                            BufferedReader(inputStreamReader).use { bufferedReader ->
                                while (true) {
                                    bufferedReader.readLine()?.let { line ->
                                        line.split(",").toTypedArray().run {
                                            val lat = this[1].trim { it <= ' ' }.toDouble()
                                            val lng = this[0].trim { it <= ' ' }.toDouble()
                                            add(MultiPointItem(LatLng(lat, lng, false)))
                                        }
                                    } ?: break
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.let {
                multiPointOverlay?.apply {
                    setItems(it)
                    setEnable(true)
                }
            }
            dismissProgressDialog
        }.start()
    }

    fun showNavigateArrow(
        width: Float = 20f, latLngA: LatLng, latLngB: LatLng, latLngC: LatLng, latLngD: LatLng
    ): NavigateArrow? =
        NavigateArrowOptions().width(width).add(latLngA, latLngB, latLngC, latLngD).let {
            aMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(CameraPosition(latLngA, 16f, 38.5f, 300f))
            )//坐标、缩放、倾斜、角度
            aMap?.addNavigateArrow(it)
        }

    fun showText(
        latLng: LatLng, text: String, size: Int = 30,
        typeface: Typeface = Typeface.DEFAULT_BOLD, font: Int = Color.BLACK, back: Int = Color.BLUE,
        rotate: Float = 20f, z: Float = 1f
    ): Text? = TextOptions().apply {
        position(latLng)
        text(text)
        fontSize(size)
        typeface(typeface)
        fontColor(font)
        backgroundColor(back)
        rotate(rotate)
        zIndex(z)
        align(Text.ALIGN_CENTER_HORIZONTAL, Text.ALIGN_CENTER_VERTICAL)
    }.let { aMap?.addText(it) }

    fun showMarkers(optionList: ArrayList<MarkerOptions>): ArrayList<Marker>? =
        aMap?.addMarkers(optionList, true)

    fun showMarkerDefault(
        latLng: LatLng, title: String = "", snippet: String = "",
        drag: Boolean = false, flat: Boolean = true,
        period: Int = 10, hue: Float = BitmapDescriptorFactory.HUE_AZURE,
        rotate: Float = 90f, pixels: Int = 400, info: Boolean = true
    ): Marker? = MarkerOptions().apply {
        position(latLng).title(title).snippet(snippet)
        draggable(drag)
        if (!drag) anchor(0.5f, 0.5f)
        isFlat = flat
        period(period)
        icon(BitmapDescriptorFactory.defaultMarker(hue))
    }.let {
        aMap?.addMarker(it)?.apply {
            rotateAngle = rotate
            setPositionByPixels(pixels, pixels)
            if (info) showInfoWindow()
        }
    }

    fun showMarkerRes(
        latLng: LatLng, title: String = "", snippet: String = "",
        drag: Boolean = false, flat: Boolean = true,
        period: Int = 10, res: Int = R.mipmap.location_marker,//临时
        rotate: Float = 90f, pixels: Int = 400, info: Boolean = true
    ): Marker? = MarkerOptions().apply {
        position(latLng).title(title).snippet(snippet)
        draggable(drag)
        if (!drag) anchor(0.5f, 0.5f)
        isFlat = flat
        period(period)
        icon(BitmapDescriptorFactory.fromResource(res))
    }.let {
        aMap?.addMarker(it)?.apply {
            rotateAngle = rotate
            setPositionByPixels(pixels, pixels)
            if (info) showInfoWindow()
        }
    }

    fun showMarkerView(
        latLng: LatLng, title: String = "", snippet: String = "",
        drag: Boolean = false, flat: Boolean = true,
        period: Int = 10, view: View = TextView(this),
        rotate: Float = 90f, pixels: Int = 400, info: Boolean = true
    ): Marker? = MarkerOptions().apply {
        position(latLng).title(title).snippet(snippet)
        draggable(drag)
        if (!drag) anchor(0.5f, 0.5f)
        isFlat = flat
        period(period)
        icon(BitmapDescriptorFactory.fromView(view))
    }.let {
        aMap?.addMarker(it)?.apply {
            rotateAngle = rotate
            setPositionByPixels(pixels, pixels)
            if (info) showInfoWindow()
        }
    }

    private val iconList: ArrayList<BitmapDescriptor> = arrayListOf<BitmapDescriptor>().apply {
        add(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        add(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        add(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
    }

    fun showMarkerIcons(
        latLng: LatLng, title: String = "", snippet: String = "",
        drag: Boolean = false, flat: Boolean = true,
        period: Int = 10, icons: ArrayList<BitmapDescriptor> = iconList,
        rotate: Float = 90f, pixels: Int = 400, info: Boolean = true
    ): Marker? = MarkerOptions().apply {
        position(latLng).title(title).snippet(snippet)
        draggable(drag)
        if (!drag) anchor(0.5f, 0.5f)
        isFlat = flat
        period(period)
        icons(icons)
    }.let {
        aMap?.addMarker(it)?.apply {
            rotateAngle = rotate
            setPositionByPixels(pixels, pixels)
            if (info) showInfoWindow()
        }
    }

    val LatLng.showMarkerLocation: Marker?
        get() = MarkerOptions().position(this)
            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.location_marker))
            .let { aMap?.addMarker(it) }
    val LatLng.showMarkerCar: Marker?
        get() = MarkerOptions().position(this).draggable(true)
            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_car))
            .let { aMap?.addMarker(it) }
    val showMarkerInScreenCenter: Marker? = aMap?.run {
        projection.toScreenLocation(cameraPosition.target).let {
            MarkerOptions().anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.purple_pin))
                .let { options -> addMarker(options).apply { setPositionByPixels(it.x, it.y) } }
        }
    }
    val Marker.showAnimationDrop: Boolean
        get() = run {
            val start = SystemClock.uptimeMillis()
            aMap?.projection?.run { fromScreenLocation(Point(toScreenLocation(position).x, 0)) }
                ?.let { latLng ->
                    backgroundHandler.post(object : Runnable {
                        override fun run() {
                            AccelerateInterpolator().getInterpolation((SystemClock.uptimeMillis() - start) / 800f)
                                .let {
                                    val lng = it * position.longitude + (1 - it) * latLng.longitude
                                    val lat = it * position.latitude + (1 - it) * latLng.latitude
                                    position = LatLng(lat, lng)
                                    if (it < 1.0) backgroundHandler.postDelayed(this, 16)
                                }
                        }
                    })
                } ?: false
        }
    val Marker.showAnimationGrow: Boolean
        get() = ScaleAnimation(0f, 1f, 0f, 1f).apply {
            setInterpolator(LinearInterpolator())
            setDuration(1000)
        }.let { apply { setAnimation(it) }.startAnimation() }
    val Marker.showAnimationGrowClick: Boolean
        get() = run {
            isVisible = false
            val start = SystemClock.uptimeMillis()
            val bitMap = icons[0].bitmap
            var lastMarkerBitMap: Bitmap? = null
            var count = 1
            backgroundHandler.post(object : Runnable {
                override fun run() {
                    AccelerateInterpolator().getInterpolation((SystemClock.uptimeMillis() - start) / 250f)
                        .let { if (it > 1) 1f else it }.let {
                            icons[0].bitmap.let { temp ->
                                val scaleWidth = (it * temp.width).toInt()
                                val scaleHeight = (it * temp.height).toInt()
                                if (scaleWidth > 0 && scaleHeight > 0) {
                                    setIcon(
                                        BitmapDescriptorFactory.fromBitmap(
                                            compressByScale(temp, scaleWidth, scaleHeight, false)
                                        )
                                    )
                                    isVisible = true
                                    lastMarkerBitMap?.run { if (!isRecycled) recycle() }
                                    icons?.run { if (size > 0) lastMarkerBitMap = this[0].bitmap }
                                    count++
                                }
                            }
                            when {
                                it < 1.0 && count < 10 -> backgroundHandler.postDelayed(this, 16)
                                else -> {
                                    lastMarkerBitMap?.run { if (!isRecycled) recycle() }
                                    setIcon(BitmapDescriptorFactory.fromBitmap(bitMap))
                                    isVisible = true
                                }
                            }
                        }
                }
            })
        }
    val Marker.showAnimationJump: Boolean
        get() = aMap?.projection?.toScreenLocation(position)
            ?.apply { y -= dip2px(125f) }?.let { point ->
                TranslateAnimation(aMap?.projection?.fromScreenLocation(point)).apply {
                    setInterpolator {
                        when {
                            it > 0.5 -> 0.5f - sqrt((it - 0.5) * (1.5 - it))
                            else -> 0.5f - 2 * (0.5 - it) * (0.5 - it)
                        }.toFloat()
                    }
                    setDuration(600)
                }.let { apply { setAnimation(it) }.startAnimation() }
            } ?: false
    val Marker.showAnimationJumpClick: Boolean
        get() = run {
            val start = SystemClock.uptimeMillis()
            aMap?.projection?.run {
                fromScreenLocation(toScreenLocation(position)?.apply { offset(0, -100) })
            }?.let { latLng ->
                backgroundHandler.post(object : Runnable {
                    override fun run() {
                        BounceInterpolator().getInterpolation((SystemClock.uptimeMillis() - start) / 1500f)
                            .let {
                                val lng = it * position.longitude + (1 - it) * latLng.longitude
                                val lat = it * position.latitude + (1 - it) * latLng.latitude
                                position = LatLng(lat, lng)
                                if (it < 1.0) backgroundHandler.postDelayed(this, 16)
                            }
                    }
                })
            } ?: false
        }

    @JvmOverloads
    fun showPolygon(
        latLngList: MutableList<LatLng>, width: Float = 15f,
        stroke: Int = Color.argb(50, 1, 1, 1),
        fill: Int = Color.argb(1, 1, 1, 1)
    ) = PolygonOptions().apply {
        for (latLng in latLngList) {
            add(latLng)
        }
        strokeWidth(width)
        strokeColor(stroke)
        fillColor(fill)
    }.let {
        changeZoom
        aMap?.addPolygon(it)
    }

    @JvmOverloads
    fun showPolygonRectangle(
        center: LatLng, halfWidth: Double, halfHeight: Double,
        width: Float = 1f, stroke: Int = Color.RED, fill: Int = Color.LTGRAY
    ) = mutableListOf<LatLng>().apply {
        add(LatLng(center.latitude - halfHeight, center.longitude - halfWidth))//左下
        add(LatLng(center.latitude - halfHeight, center.longitude + halfWidth))//右下
        add(LatLng(center.latitude + halfHeight, center.longitude + halfWidth))//右上
        add(LatLng(center.latitude + halfHeight, center.longitude - halfWidth))//左上
    }.let { list ->
        PolygonOptions().strokeWidth(width).strokeColor(stroke).fillColor(fill)
            .addAll(list).let {
                changeZoom
                aMap?.addPolygon(it)
            }
    }

    @JvmOverloads
    fun showPolygonEllipse(
        center: LatLng, width: Float = 25f,
        stroke: Int = Color.argb(50, 1, 1, 1),
        fill: Int = Color.argb(50, 1, 1, 1)
    ) = PolygonOptions().apply {
        val numPoints = 400
        val phase = 2 * Math.PI / numPoints
        val semiVerticalAxis = 2.5f
        val semiHorizontalAxis = 5f
        for (i in 0..numPoints) {
            add(
                LatLng(
                    center.latitude + semiVerticalAxis * sin(i * phase),
                    center.longitude + semiHorizontalAxis * cos(i * phase)
                )
            )
        }
        strokeWidth(width)
        strokeColor(stroke)
        fillColor(fill)
    }.let {
        changeZoom
        aMap?.addPolygon(it)
    }

    @JvmOverloads
    fun showArc(start: LatLng, middle: LatLng, end: LatLng, color: Int = Color.RED) =
        ArcOptions().point(start, middle, end).strokeColor(color).let {
            changeZoom
            aMap?.addArc(it)
        }

    @JvmOverloads
    fun showCircle(
        center: LatLng, radius: Double = 4000.0,
        width: Float = 25f, stroke: Int = 50, fill: Int = 50
    ) = CircleOptions().center(center).radius(radius).strokeWidth(width)
        .strokeColor(Color.argb(stroke, 1, 1, 1))
        .fillColor(Color.argb(fill, 1, 1, 1)).let {
            aMap?.apply { moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12f)) }
                ?.addCircle(it)
        }

    @JvmOverloads
    fun showCirclePolyline(
        center: LatLng, radius: Int, width: Float = 10f, color: Int = Color.RED,
        isTexture: Boolean = false, isGradient: Boolean = true,
        isDotted: Boolean = true, isGeodesic: Boolean = false
    ) = PolylineOptions().apply {
        val numPoints = 360
        val phase = 2 * Math.PI / numPoints
        val r = 6371000.79
        for (i in 0 until numPoints) {
            val dy = radius * sin(i * phase)
            val dLat = dy / (r * Math.PI / 180)
            val dx = radius * cos(i * phase)
            val dLng = dx / (r * cos(center.latitude * Math.PI / 180) * Math.PI / 180)
            add(LatLng(center.latitude + dLat, center.longitude + dLng))
        }
        width(width)
        color(color)
        if (isTexture) customTexture = BitmapDescriptorFactory.defaultMarker()
        useGradient(isGradient)
        isDottedLine = isDotted
        geodesic(isGeodesic)
    }.let { aMap?.addPolyline(it) }

    @JvmOverloads
    fun showPolyline(
        latLngList: MutableList<LatLng>, width: Float = 10f, color: Int = Color.RED,
        isTexture: Boolean = false, isGradient: Boolean = true,
        isDotted: Boolean = true, isGeodesic: Boolean = false
    ) = PolylineOptions().apply {
        for (latLnt in latLngList) {
            add(latLnt)
        }
        width(width)
        color(color)
        if (isTexture) customTexture = BitmapDescriptorFactory.defaultMarker()
        useGradient(isGradient)
        isDottedLine = isDotted
        geodesic(isGeodesic)
    }

    fun LatLng.update(offset: Double) = run { LatLng(latitude + offset, longitude + offset) }

    @JvmOverloads
    fun showColorsPolyline(
        latLngA: LatLng, latLngB: LatLng, latLngC: LatLng, latLngD: LatLng,
        offset: Double = 0.0001, width: Float = 10f,
        isTexture: Boolean = false, isGradient: Boolean = true,
        isDotted: Boolean = true, isGeodesic: Boolean = false
    ) = PolylineOptions().apply {
        add(
            latLngA.update(offset), latLngB.update(offset),
            latLngC.update(offset), latLngD.update(offset)
        )
        colorValues(mutableListOf<Int>().apply {
            add(Color.RED)
            add(Color.YELLOW)
            add(Color.GREEN)
            add(Color.BLACK)//第四色不加，最后一段显示上一段颜色
        })//colorValues多颜色，color单色线
        width(width)
        if (isTexture) customTexture = BitmapDescriptorFactory.defaultMarker()
        useGradient(isGradient)
        isDottedLine = isDotted
        geodesic(isGeodesic)
    }.let {
        changeZoom
        aMap?.addPolyline(it)
    }//0.0004

    @JvmOverloads
    fun showTexturePolyline(
        latLngA: LatLng, latLngB: LatLng, latLngC: LatLng, latLngD: LatLng,
        offset: Double = 1.0, width: Float = 20f
    ) = PolylineOptions().apply {
        add(
            latLngA.update(offset), latLngB.update(offset),
            latLngC.update(offset), latLngD.update(offset)
        )
        customTextureList = mutableListOf<BitmapDescriptor>().apply {
            add(BitmapDescriptorFactory.fromResource(R.mipmap.map_alr))
            add(BitmapDescriptorFactory.fromResource(R.mipmap.custtexture))
            add(BitmapDescriptorFactory.fromResource(R.mipmap.map_alr_night))
        }
        customTextureIndex = mutableListOf<Int>().apply {
            add(0)
            add(2)
            add(1)
        }
        width(width)
    }.let {
        changeZoom
        aMap?.addPolyline(it)
    }

    @JvmOverloads
    fun showPolylineInPlayGround(
        center: LatLng, radius: Int = 50, width: Float = 15f,
        isGradient: Boolean = true, isDotted: Boolean = true, isGeodesic: Boolean = false
    ) = PolylineOptions().apply {
        val numPoints = 36
        val phase = 2 * Math.PI / numPoints
        val r = 6371000.79
        for (i in 0 until numPoints) {
            val dy = radius * sin(i * phase) * 1.6
            val dLat = dy / (r * Math.PI / 180)
            val dx = radius * cos(i * phase)
            val dLng = dx / (r * cos(center.latitude * Math.PI / 180) * Math.PI / 180)
            var newlng = center.longitude + dLng
            when {
                newlng < center.longitude - 0.00046 -> newlng = center.longitude - 0.00046
                newlng > center.longitude + 0.00046 -> newlng = center.longitude + 0.00046
            }
            add(LatLng(center.latitude + dLat, newlng))
        }
        add(points[0])
        colorValues(mutableListOf<Int>().apply {
            val colors = intArrayOf(
                Color.argb(255, 0, 255, 0),
                Color.argb(255, 255, 255, 0),
                Color.argb(255, 255, 0, 0)
            )
            val random = Random()
            for (i in 0 until numPoints step 2) {
                colors[random.nextInt(3)].let {
                    add(it)
                    add(it)
                }
            }
            add(this[0])
        })
        width(width)
        useGradient(isGradient)
        isDottedLine = isDotted
        geodesic(isGeodesic)
    }.let {
        changeZoom
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 17f))
        aMap?.addPolyline(it)
    }

    @JvmOverloads
    fun listenerEvent(
        loaded: Boolean = true, cameraChange: Boolean = true,
        click: Boolean = true, longClick: Boolean = true, touch: Boolean = true,
        poi: Boolean = true, drag: Boolean = true, marker: Boolean = true,
        infoWindow: Boolean = true
    ) = aMap?.apply {
        if (loaded) setOnMapLoadedListener {}//TODO
        if (cameraChange) setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(cameraPosition: CameraPosition?) {}//TODO
            override fun onCameraChangeFinish(cameraPosition: CameraPosition?) {}//TODO
        })
        if (click) setOnMapClickListener {}//TODO
        if (longClick) setOnMapLongClickListener {}//TODO
        if (touch) setOnMapTouchListener {}//TODO
        if (poi) setOnPOIClickListener {
            clear()
            info("${it.poiId}${it.name}")
            MarkerOptions().position(it.coordinate).title("标题").snippet("摘要")
                .icon(BitmapDescriptorFactory.fromView(TextView(applicationContext).apply {
                    text = "到${it.name}去"
                    gravity = Gravity.CENTER
                    setTextColor(Color.BLACK)
                    setBackgroundResource(R.mipmap.custom_info_bubble)
                })).run { addMarker(this) }
        }//TODO
        if (drag) setOnMarkerDragListener(object : AMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker?) {}//TODO
            override fun onMarkerDrag(marker: Marker?) {}//TODO
            override fun onMarkerDragEnd(marker: Marker?) {}//TODO
        })
        if (marker) setOnMarkerClickListener {
            resetLastMarker
            it.setIcon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.mipmap.poi_marker_pressed)
                )
            )
            it.showInfoWindow()
            return@setOnMarkerClickListener false
        }//TODO
        if (infoWindow) setInfoWindowAdapter(object : AMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker?): View? = null
            override fun getInfoWindow(marker: Marker?): View? =
                layoutInflater.inflate(R.layout.poikeywordsearch_uri, null).apply {
                    (marker?.getObject() as PoiItem?)?.run {
                        val poiTitle: TextView = findViewById(R.id.title)
                        poiTitle.text = title
                        val poiSnippet: TextView = findViewById(R.id.snippet)
                        poiSnippet.text = "${snippet}${distance}"
                        val button: ImageButton = findViewById(R.id.start_amap_app)
                        button.setOnClickListener { marker?.startNavi }
                    }
                }
        })//TODO
        if (infoWindow) setOnInfoWindowClickListener {}//TODO
    }

    val listenerShot = aMap?.getMapScreenShot(this)
    override fun onMapScreenShot(bitmap: Bitmap?) {}
    override fun onMapScreenShot(bitmap: Bitmap?, arg: Int) {
        bitmap?.let {
            try {
                FileOutputStream(
                    "${pathExternal}MapScreenShot_${sdfDateByFullX.format(Date())}.png"
                ).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it).let { success ->
                        it.flush()
                        StringBuffer().apply {
                            if (success) append("截屏成功 ") else append("截屏失败 ")
                            if (arg == 0) append("地图未渲染完成，截屏有网格") else append("地图渲染完成，截屏无网格")
                        }.run { showShort(toString() as CharSequence) }
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private var progressDialog: ProgressDialog? = null
    private val showProgressDialog = {
        if (progressDialog == null) progressDialog = ProgressDialog(this)
        progressDialog?.apply {
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
            isIndeterminate = false
            setCancelable(true)
            setMessage("正在搜索:\n")
        }?.show()
    }
    var cityCode = ""
        set(cityCode) = cityCode.run { substring(indexOf("-") + 1) }

    fun searchStation(station: String) {
        showProgressDialog
        BusStationQuery(station.trim { it <= ' ' }.let { if (it == "") "站名" else it }, cityCode)
            .let {
                BusStationSearch(this, it).apply { setOnBusStationSearchListener(this@MapActivity) }
                    .searchBusStationAsyn()
            }
    }

    private val dismissProgressDialog = progressDialog?.dismiss()
    override fun onBusStationSearched(result: BusStationResult?, rCode: Int) {
        dismissProgressDialog
        when (rCode) {
            AMapException.CODE_AMAP_SUCCESS -> when {
                result != null && result.pageCount > 0 && result.busStations != null && result.busStations.size > 0 -> {
                    StringBuffer().apply {
                        for ((index, value) in result.busStations.withIndex()) {
                            append(" station: $index name: ${value.busStationName}")
                            debug("$loggerTag->stationName:${value.busStationName}stationPos:${value.latLonPoint}")
                        }
                    }.toString().let { showShort(it as CharSequence) }
                }
                else -> showShort(R.string.no_result as CharSequence)
            }
            else -> showError(rCode)
        }
    }

    private var currentPage = 0
    private var busLineQuery: BusLineQuery? = null
    private var busLineSearch: BusLineSearch? = null
    fun searchLine(line: String) {
        currentPage = 0
        showProgressDialog
        BusLineQuery(line.trim { it <= ' ' }.let { if (it == "") "000" else it },
            BusLineQuery.SearchType.BY_LINE_NAME, cityCode
        ).apply {
            pageSize = 10
            pageNumber = currentPage
            busLineQuery = this
        }.let {
            BusLineSearch(this, it).apply {
                setOnBusLineSearchListener(this@MapActivity)
                busLineSearch = this
            }.searchBusLineAsyn()
        }
    }

    private var busLineResult: BusLineResult? = null
    override fun onBusLineSearched(result: BusLineResult?, rCode: Int) {
        dismissProgressDialog
        when (rCode) {
            AMapException.CODE_AMAP_SUCCESS -> when {
                result != null && result.query != null && result.query == busLineQuery -> when {
                    result.query.category === BusLineQuery.SearchType.BY_LINE_NAME -> {
                        if (result.pageCount > 0 && result.busLines != null && result.busLines.size > 0)
                            showResultList(result.apply { busLineResult = this }.busLines)
                    }
                    result.query.category === BusLineQuery.SearchType.BY_LINE_ID -> {
                        aMap?.clear()
                        MapBusLineOverlay(
                            this, aMap, result.apply { busLineResult = this }.busLines[0]
                        ).apply {
                            removeFromMap
                            addToMap
                            zoomToSpan
                        }
                    }
                }
                else -> showShort(R.string.no_result as CharSequence)
            }
            else -> showError(rCode)
        }
    }

    private fun showResultList(busLineItems: MutableList<BusLineItem>?) =
        BusLineDialog(this, busLineItems).apply {
            onListItemClickListener = object : OnListItemClickListener {
                override fun onListItemClick(dialog: BusLineDialog?, item: BusLineItem) {
                    showProgressDialog
                    busLineQuery =
                        BusLineQuery(item.busLineId, BusLineQuery.SearchType.BY_LINE_ID, cityCode)
                    BusLineSearch(this@MapActivity, busLineQuery)
                        .apply { setOnBusLineSearchListener(this@MapActivity) }
                        .searchBusLineAsyn()
                }
            }
        }.show()

    internal inner class BusLineDialog(context: Context, theme: Int) :
        Dialog(context, theme), View.OnClickListener {
        constructor(context: Context, busLineItems: MutableList<BusLineItem>?) :
                this(context, android.R.style.Theme_NoTitleBar) {
            mapBusLineItems = busLineItems
            mapBusLineAdapter = MapBusLineAdapter(context, busLineItems)
        }

        private var buslineDialogBinding: BuslineDialogBinding? = null
        private var mapBusLineItems: MutableList<BusLineItem>? = null
        private var mapBusLineAdapter: MapBusLineAdapter? = null
        var onListItemClickListener: OnListItemClickListener? = null
        override fun onCreate(savedInstanceState: Bundle) {
            super.onCreate(savedInstanceState)
            buslineDialogBinding = BuslineDialogBinding.inflate(layoutInflater)
            setContentView(buslineDialogBinding?.root)
            buslineDialogBinding?.preButton?.setOnClickListener(this)
            buslineDialogBinding?.nextButton?.setOnClickListener(this)
            if (currentPage <= 0) buslineDialogBinding?.preButton?.isEnabled = false
            if (currentPage >= (busLineResult?.pageCount ?: 0) - 1)
                buslineDialogBinding?.nextButton?.isEnabled = false
            buslineDialogBinding?.listView?.apply {
                adapter = mapBusLineAdapter
                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    mapBusLineItems?.get(position)
                        ?.let { onListItemClickListener?.onListItemClick(this@BusLineDialog, it) }
                    dismiss()
                }
            }
        }

        override fun onClick(view: View) {
            dismiss()
            if ((view == buslineDialogBinding?.preButton)) currentPage-- else if ((view == buslineDialogBinding?.nextButton)) currentPage++
            showProgressDialog
            busLineQuery?.pageNumber = currentPage
            busLineSearch?.apply { setOnBusLineSearchListener(this@MapActivity) }
                ?.searchBusLineAsyn()
        }
    }

    internal interface OnListItemClickListener {
        fun onListItemClick(dialog: BusLineDialog?, item: BusLineItem)
    }

    private val items: MutableList<CloudItem?> = mutableListOf()
    private var query: Query? = null
    private var cloudSearch: CloudSearch =
        CloudSearch(this).apply { setOnCloudSearchListener(this@MapActivity) }
    var tableId = "tableId"
    var lineId = "1"
    fun searchById(view: View?) {
        showProgressDialog
        items.clear()
        cloudSearch.searchCloudDetailAsyn(tableId, lineId)
    }

    var keyWord = ""
    var pointCenter: LatLonPoint = LatLonPoint(39.942753, 116.428650)
    fun searchByBound(view: View?) {
        showProgressDialog
        items.clear()
        try {
            query = Query(tableId, keyWord, SearchBound(pointCenter, 4000))
                .apply {
                    pageSize = 10
                    sortingrules = Sortingrules("_id", false)
                }
            cloudSearch.searchCloudAsyn(query)
        } catch (e: AMapException) {
            e.printStackTrace()
        }
    }

    var point1: LatLonPoint = LatLonPoint(39.941711, 116.382248)
    var point2: LatLonPoint = LatLonPoint(39.884882, 116.359566)
    var point3: LatLonPoint = LatLonPoint(39.878120, 116.437630)
    var point4: LatLonPoint = LatLonPoint(39.941711, 116.382248)
    fun searchByPolygon(view: View?) {
        showProgressDialog
        items.clear()
        mutableListOf<LatLonPoint>().apply {
            add(point1)
            add(point2)
            add(point3)
            add(point4)
        }.let {
            try {
                query = Query(tableId, keyWord, SearchBound(it))
                cloudSearch.searchCloudAsyn(query)
            } catch (e: AMapException) {
                e.printStackTrace()
            }
        }
    }

    var localCityName = ""
    fun searchByLocal(view: View?) {
        showProgressDialog
        items.clear()
        try {
            query = Query(tableId, keyWord, SearchBound(localCityName))
            cloudSearch.searchCloudAsyn(query)
        } catch (e: AMapException) {
            e.printStackTrace()
        }
    }

    private var markerCloudId: Marker? = null
    override fun onCloudItemDetailSearched(item: CloudItemDetail?, rCode: Int) {
        dismissProgressDialog
        when {
            rCode == AMapException.CODE_AMAP_SUCCESS && item != null -> {
                markerCloudId?.destroy()
                aMap?.clear()
                val position: LatLng = item.latLonPoint.toLatLng
                aMap?.animateCamera(
                    CameraUpdateFactory.newCameraPosition(CameraPosition(position, 18f, 0f, 30f))
                )
                markerCloudId = aMap?.addMarker(
                    MarkerOptions().position(position).title(item.title)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
                items.add(item)
                debug("${loggerTag}_id${item.id}")
                debug("${loggerTag}_location${item.latLonPoint}")
                debug("${loggerTag}_name${item.title}")
                debug("${loggerTag}_address${item.snippet}")
                debug("${loggerTag}_caretetime${item.createtime}")
                debug("${loggerTag}_updatetime${item.updatetime}")
                debug("${loggerTag}_distance${item.distance}")
                for ((key, value) in item.customfield) {
                    debug("$loggerTag$key:$value")
                }
            }
            else -> showError(rCode)
        }
    }

    private var cloudItems: MutableList<CloudItem>? = null
    private var poiMapCloudOverlay: MapCloudOverlay? = null
    override fun onCloudSearched(result: CloudResult?, rCode: Int) {
        dismissProgressDialog
        when (rCode) {
            AMapException.CODE_AMAP_SUCCESS -> {
                result?.query?.let {
                    if (result.query == query) {
                        cloudItems = result.clouds as MutableList<CloudItem>
                        when {
                            (cloudItems?.size ?: 0) > 0 -> {
                                aMap?.clear()
                                poiMapCloudOverlay = MapCloudOverlay(
                                    aMap, cloudItems as MutableList<CloudItem>
                                ).apply {
                                    removeFromMap
                                    addToMap
                                    zoomToSpan
                                }
                                for (item in cloudItems as MutableList<CloudItem>) {
                                    items.add(item)
                                    debug("${loggerTag}_id ${item.id}")
                                    debug("${loggerTag}_location ${item.latLonPoint}")
                                    debug("${loggerTag}_name ${item.title}")
                                    debug("${loggerTag}_address ${item.snippet}")
                                    debug("${loggerTag}_caretetime ${item.createtime}")
                                    debug("${loggerTag}_updatetime ${item.updatetime}")
                                    debug("${loggerTag}_distance ${item.distance}")
                                    for ((key, value) in item.customfield) {
                                        debug("${loggerTag}$key:$value")
                                    }
                                }
                                when {
                                    query?.bound?.shape.equals(SearchBound.BOUND_SHAPE) -> {
                                        aMap?.addCircle(
                                            CircleOptions().strokeWidth(5f).strokeColor(Color.RED)
                                                .fillColor(Color.argb(50, 1, 1, 1))
                                                .radius(5000.0).center(pointCenter.toLatLng)
                                        )
                                        aMap?.moveCamera(
                                            CameraUpdateFactory
                                                .newLatLngZoom(pointCenter.toLatLng, 12f)
                                        )
                                    }
                                    query?.bound?.shape.equals(SearchBound.POLYGON_SHAPE) -> {
                                        aMap?.addPolygon(
                                            PolygonOptions().strokeWidth(1f).strokeColor(Color.RED)
                                                .fillColor(Color.argb(50, 1, 1, 1))
                                                .add(point1.toLatLng).add(point2.toLatLng)
                                                .add(point3.toLatLng).add(point4.toLatLng)
                                        )
                                        val bounds: LatLngBounds = LatLngBounds.Builder()
                                            .include(point1.toLatLng).include(point2.toLatLng)
                                            .include(point3.toLatLng).build()
                                        aMap?.moveCamera(
                                            CameraUpdateFactory.newLatLngBounds(bounds, 50)
                                        )
                                    }
                                    query?.bound?.shape.equals(SearchBound.LOCAL_SHAPE) -> poiMapCloudOverlay?.zoomToSpan
                                }
                            }
                            else -> showShort(R.string.no_result as CharSequence)
                        }
                    }
                } ?: showShort(R.string.no_result as CharSequence)
            }
            else -> showError(rCode)
        }
    }

    override fun getInfoContents(marker: Marker?): View? = null
    override fun getInfoWindow(marker: Marker?): View? = null
    override fun onInfoWindowClick(marker: Marker) {
        for (item in items) {
            if (marker.title == item?.title) {
                Intent(this@MapActivity, MapCloudDetailActivity::class.java)
                    .apply { intent.putExtra("clouditem", item) }.run { startActivity(this) }
                break
            }
        }
    }

    private var geoCoderSearch: GeocodeSearch =
        GeocodeSearch(this).apply { setOnGeocodeSearchListener(this@MapActivity) }

    fun searchLatLngByAddress(address: String, cityCode: String) {
        showProgressDialog
        geoCoderSearch.getFromLocationNameAsyn(GeocodeQuery(address, cityCode))
    }

    private var geoMarker: Marker? = aMap?.addMarker(
        MarkerOptions().anchor(0.5f, 0.5f)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
    )

    override fun onGeocodeSearched(result: GeocodeResult?, rCode: Int) {
        dismissProgressDialog
        when (rCode) {
            AMapException.CODE_AMAP_SUCCESS -> when {
                result?.geocodeAddressList != null && result.geocodeAddressList.size > 0 ->
                    result.geocodeAddressList[0].run {
                        aMap?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(latLonPoint.toLatLng, 15f)
                        )
                        geoMarker?.position = latLonPoint.toLatLng
                        showShort("经纬度值:${latLonPoint}\n位置描述:${formatAddress}" as CharSequence)
                    }
                else -> showShort(R.string.no_result as CharSequence)
            }
            else -> showError(rCode)
        }
    }

    val DoubleArray.toLatLonPoints: MutableList<LatLonPoint>
        get() = let {
            mutableListOf<LatLonPoint>().apply {
                for (i in 0..it.size step 2) {
                    add(LatLonPoint(it[i + 1], it[i]))
                }
            }
        }
    private var executorService: ExecutorService? = null
    private val msgHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) = showError(msg.arg1)
    }

    fun searchAddressesByLatLonList(latLonPoints: MutableList<LatLonPoint>) {
        if (executorService == null) executorService = Executors.newSingleThreadExecutor()
        for (point in latLonPoints) {
            executorService?.submit {
                try {
                    geoCoderSearch.getFromLocation(RegeocodeQuery(point, 200f, GeocodeSearch.AMAP))
                        ?.formatAddress?.let {
                            aMap?.addMarker(MarkerOptions().title(it).position(point.toLatLng))
                        }
                } catch (e: AMapException) {
                    msgHandler.sendMessage(msgHandler.obtainMessage().apply { arg1 = e.errorCode })
                }
            }
        }
    }

    var point: LatLonPoint? = null
    fun searchAddressByLatLon(latLonPoint: LatLonPoint) {
        point = latLonPoint
        showProgressDialog
        geoCoderSearch.getFromLocationAsyn(RegeocodeQuery(latLonPoint, 200f, GeocodeSearch.AMAP))
    }

    private var reGeoMarker: Marker? = aMap?.addMarker(
        MarkerOptions().anchor(0.5f, 0.5f)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
    )

    override fun onRegeocodeSearched(result: RegeocodeResult?, rCode: Int) {
        dismissProgressDialog
        when (rCode) {
            AMapException.CODE_AMAP_SUCCESS -> result?.regeocodeAddress?.formatAddress?.let {
                aMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(point?.toLatLng, 15f))
                reGeoMarker?.position = point?.toLatLng
                showShort("${it}附近" as CharSequence)
            } ?: showShort(R.string.no_result as CharSequence)
            else -> showError(rCode)
        }
    }

    var isWithBoundary: Boolean = false

    @JvmOverloads
    fun searchDistrict(district: String = "中国") {
        isWithBoundary = false
        DistrictSearch(this).apply {
            query = DistrictSearchQuery().apply { keywords = district }
            setOnDistrictSearchListener(this@MapActivity)
        }.searchDistrictAsyn()
    }

    fun searchDistrictWithBoundary(district: String) {
        isWithBoundary = true
        aMap?.clear()
        DistrictSearch(applicationContext).apply {
            query = DistrictSearchQuery().apply {
                keywords = district
                isShowBoundary = true
            }
            setOnDistrictSearchListener(this@MapActivity)
        }.searchDistrictAsyn()
    }

    private val DistrictItem.toDistrictInfoStr: String
        get() = run {
            StringBuffer().apply {
                append("区划名称:$name\n区域编码:$adcode\n城市编码:$citycode\n区划级别:$level\n经纬度:(${center.longitude}, ${center.latitude})\n")
            }.toString()
        }
    var infoCountry: String? = null
    private var isInit = false
    private var currentDistrictItem: DistrictItem? = null
    private val subDistrictMap: MutableMap<String, MutableList<DistrictItem>> = mutableMapOf()
    override fun onDistrictSearched(result: DistrictResult?) {
        when (isWithBoundary) {
            false -> {
                var subDistrictList: MutableList<DistrictItem>? = null
                result?.let {
                    when (result.aMapException.errorCode) {
                        AMapException.CODE_AMAP_SUCCESS -> {
                            val district: MutableList<DistrictItem> = it.district
                            if (!isInit) {
                                isInit = true
                                currentDistrictItem = district[0]
                                infoCountry = currentDistrictItem?.toDistrictInfoStr
                            }
                            for (districtItem in district) {
                                districtItem.run { subDistrictMap[adcode] = subDistrict }
                            }
                            subDistrictList = subDistrictMap[currentDistrictItem?.adcode]
                        }
                        else -> showError(it.aMapException.errorCode)
                    }
                }
                setSpinnerView(subDistrictList)
            }
            true -> result?.run {
                district?.let {
                    when (aMapException?.errorCode) {
                        AMapException.CODE_AMAP_SUCCESS -> it[0].let { districtItem ->
                            districtItem.center.toLatLng.let { latLng ->
                                aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8f))
                            }
                            object : Thread() {
                                override fun run() {
                                    for (string in districtItem.districtBoundary()) {
                                        PolylineOptions().apply {
                                            var isFirst = true
                                            var first: LatLng? = null
                                            for (latLngStr in string.split(";").toTypedArray()) {
                                                val array = latLngStr.split(",").toTypedArray()
                                                if (isFirst) {
                                                    isFirst = false
                                                    first = LatLng(
                                                        array[1].toDouble(), array[0].toDouble()
                                                    )
                                                }
                                                add(
                                                    LatLng(array[1].toDouble(), array[0].toDouble())
                                                )
                                            }
                                            first?.let { add(first) }
                                        }.width(10f).color(Color.BLUE)
                                            .run { aMap?.addPolyline(this) }
                                    }
                                }
                            }.start()
                        }
                        else -> aMapException
                            ?.let { e -> showError(e.errorCode) }
                    }
                }
            }
        }
    }

    private var selectedLevel: String = COUNTRY
    private var listProvince: MutableList<DistrictItem> = mutableListOf()
    private var listCity: MutableList<DistrictItem> = mutableListOf()
    private var listDistrict: MutableList<DistrictItem> = mutableListOf()
    var spinnerProvince: Spinner? = null//TODO
    var spinnerCity: Spinner? = null//TODO
    var spinnerDistrict: Spinner? = null//TODO
    var infoProvince: String? = null
    var infoCity: String? = null
    var infoDistrict: String? = null
    private fun setSpinnerView(subDistrictList: MutableList<DistrictItem>?) {
        when {
            subDistrictList != null && subDistrictList.isNotEmpty() -> mutableListOf<String>().apply {
                for (subDistrict in subDistrictList) {
                    add(subDistrict.name)
                }
            }.let { nameList ->
                ArrayAdapter(this, android.R.layout.simple_spinner_item, nameList).let { adapter ->
                    when {
                        selectedLevel.equals(COUNTRY, true) -> {
                            listProvince = subDistrictList
                            spinnerProvince?.adapter = adapter
                        }
                        selectedLevel.equals(PROVINCE, true) -> {
                            listCity = subDistrictList
                            spinnerCity?.adapter = adapter
                        }
                        selectedLevel.equals(CITY, true) -> {
                            listDistrict = subDistrictList
                            if (nameList.size <= 0) infoDistrict = ""
                            spinnerDistrict?.adapter = adapter
                        }
                    }
                }
            }
            else -> ArrayAdapter(
                this, android.R.layout.simple_spinner_item, mutableListOf<String>()
            ).let { adapter ->
                if (selectedLevel.equals(COUNTRY, true)) {
                    spinnerProvince?.adapter = adapter
                    spinnerCity?.adapter = adapter
                    spinnerDistrict?.adapter = adapter
                    infoProvince = ""
                    infoCity = ""
                    infoDistrict = ""
                }
                if (selectedLevel.equals(PROVINCE, true)) {
                    spinnerCity?.adapter = adapter
                    spinnerDistrict?.adapter = adapter
                    infoCity = ""
                    infoDistrict = ""
                }
                if (selectedLevel.equals(CITY, true)) {
                    spinnerDistrict?.adapter = adapter
                    infoDistrict = ""
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    private val DistrictItem?.querySubDistrict
        get() = let {
            DistrictSearch(this@MapActivity).apply {
                query = DistrictSearchQuery().apply { keywords = it?.name }
                setOnDistrictSearchListener(this@MapActivity)
            }.searchDistrictAsyn()
        }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent) {
            spinnerProvince -> {
                selectedLevel = PROVINCE
                listProvince[position].apply { infoProvince = toDistrictInfoStr }
            }
            spinnerCity -> {
                selectedLevel = CITY
                listCity[position].apply { infoCity = toDistrictInfoStr }
            }
            spinnerDistrict -> {
                selectedLevel = DISTRICT
                listDistrict[position].apply { infoDistrict = toDistrictInfoStr }
            }
            else -> null
        }?.run {
            currentDistrictItem = this
            subDistrictMap[adcode]?.let { list -> setSpinnerView(list) } ?: querySubDistrict
        }
    }

    var keywordText: AutoCompleteTextView? = null
        get() = field?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) =
                    s.toString().trim { it <= ' ' }.let { str ->
                        if (!isEmptyTrim(str)) Inputtips(this@MapActivity,
                            InputtipsQuery(str, text.toString()).apply { cityLimit = true }).apply {
                            setInputtipsListener { tipList: MutableList<Tip>, rCode: Int ->
                                when (rCode) {
                                    AMapException.CODE_AMAP_SUCCESS -> ArrayAdapter(
                                        applicationContext, R.layout.route_inputs,
                                        tipList.map { it.name }.toMutableList()
                                    ).apply { field?.setAdapter(this) }.notifyDataSetChanged()
                                    else -> showError(rCode)
                                }
                            }
                        }.requestInputtipsAsyn()
                    }
            })
        }//TODO
    var poiSearchList: ListView? = null//TODO
    private var mapPoiListAdapter: MapPoiListAdapter? = null
    fun searchPoiSub(poi: String) =
        PoiSearch(this@MapActivity, PoiSearch.Query(poi, "", keywordText?.text.toString()).apply {
            requireSubPois(true)
            pageSize = 10
            pageNum = 0
        }).apply {
            setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
                override fun onPoiSearched(poiResult: PoiResult?, rCode: Int) {
                    when (rCode) {
                        AMapException.CODE_AMAP_SUCCESS -> poiResult?.let {
                            poiSearchList?.adapter = MapPoiListAdapter(this@MapActivity, it.pois)
                                .apply { mapPoiListAdapter = this }
                        }
                        else -> showError(rCode)
                    }
                }

                override fun onPoiItemSearched(poiItem: PoiItem?, rCode: Int) {
                    when (rCode) {
                        AMapException.CODE_AMAP_SUCCESS -> poiItem?.let {
                            poiSearchList?.adapter = MapPoiListAdapter(
                                this@MapActivity, mutableListOf<PoiItem>().apply { add(it) })
                                .apply { mapPoiListAdapter = this }
                        }
                        else -> showError(rCode)
                    }
                }
            })
        }.searchPOIAsyn()

    var detailMarker: Marker? = null//TODO
    var mPoiName: TextView? = null//TODO
    var mPoiAddress: TextView? = null//TODO
    var mPoiInfo: TextView? = null//TODO
    var mPoiDetail: RelativeLayout? = null//TODO
    fun searchPoiId(poiId: String) = PoiSearch(this@MapActivity, null).apply {
        setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
            override fun onPoiSearched(result: PoiResult?, rcode: Int) {}
            override fun onPoiItemSearched(poiItem: PoiItem?, rCode: Int) {
                when (rCode) {
                    AMapException.CODE_AMAP_SUCCESS -> poiItem?.run {
                        detailMarker = aMap?.addMarker(MarkerOptions())
                            ?.apply { position = latLonPoint?.toLatLng }
                        mPoiName?.text = title
                        mPoiAddress?.text = snippet
                        mPoiInfo?.text = poiExtension?.run { "营业时间：$opentime；评分：${getmRating()}" }
                        mPoiDetail?.visibility = View.VISIBLE
                    }
                    else -> showError(rCode)
                }
            }
        })
    }.searchPOIIdAsyn(poiId)

    private var editCity: EditText? = null
    private var poiSearchQuery: PoiSearch.Query? = null
    private var poiSearch: PoiSearch? = null
    private var poiResult: PoiResult? = null
    fun searchPoiKeyword(poiKeyword: String) {
        showProgressDialog
        currentPage = 0
        poiSearchQuery = PoiSearch.Query(poiKeyword, "", editCity?.text.toString()).apply {
            pageSize = 10
            pageNum = currentPage
        }//一参搜索字符串、二参搜索类型、三参搜索区域（空字符串代表全国）
        PoiSearch(this, poiSearchQuery).apply {
            setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
                override fun onPoiItemSearched(poiItem: PoiItem?, rCode: Int) {}
                override fun onPoiSearched(result: PoiResult?, rCode: Int) {
                    dismissProgressDialog
                    when (rCode) {
                        AMapException.CODE_AMAP_SUCCESS -> result?.query?.let {
                            if (it == poiSearchQuery) {
                                poiResult = result
                                val poiItems: MutableList<PoiItem>? = poiResult?.pois
                                val suggestionCities: MutableList<SuggestionCity>? =
                                    poiResult?.searchSuggestionCitys
                                when {
                                    poiItems?.isNotEmpty() == true -> aMap?.run {
                                        clear()
                                        MapPoiOverlay(this, poiItems).apply {
                                            removeFromMap
                                            addToMap
                                            zoomToSpan
                                        }
                                    }
                                    suggestionCities?.isNotEmpty() == true ->
                                        showSuggestCity(suggestionCities)
                                    else -> showShort(R.string.no_result)
                                }
                            }
                        } ?: showShort(R.string.no_result)
                        else -> showError(rCode)
                    }
                }
            })
            poiSearch = this
        }.searchPOIAsyn()
    }

    private fun showSuggestCity(cities: MutableList<SuggestionCity>) {
        var information: String? = "推荐城市\n"
        for (city in cities) {
            information += city.run { "城市名称:${cityName}城市区号:${cityCode}城市编码:${adCode}\n" }
        }
        showShort(information)
    }

    val nextPagePoiKeyword = {
        if (poiSearchQuery != null && poiSearch != null && poiResult != null) when {
            (poiResult?.pageCount ?: 0) - 1 > currentPage -> {
                poiSearchQuery?.pageNum = ++currentPage
                poiSearch?.searchPOIAsyn()
            }
            else -> showShort(R.string.no_result)
        }
    }
    private val latLonPoint: LatLonPoint? = LatLonPoint(39.993743, 116.472995)
    private var locationMarker: Marker? = null
    private var poiItems: MutableList<PoiItem>? = null
    private var poiDetail: RelativeLayout? = null
    private var poiOverlay: InnerPoiOverlay? = null
    private var lastMarker: Marker? = null
    val resetLastMarker = poiOverlay?.getPoiIndex(lastMarker)?.let { index ->
        when {
            index < 10 -> lastMarker?.setIcon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, markers[index])
                )
            )
            else -> lastMarker?.setIcon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.mipmap.marker_other_highlight)
                )
            )
        }
        lastMarker = null
    }

    fun searchPoiAround(poi: String) {
        locationMarker = aMap?.addMarker(
            MarkerOptions().anchor(0.5f, 0.5f).position(latLonPoint?.toLatLng).icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.mipmap.point4)
                )
            )
        )
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLonPoint?.toLatLng, 14f))
        poiSearchQuery = PoiSearch.Query(poi, "", "").apply {
            pageSize = 20
            pageNum = 0.apply { currentPage = this }
        }//一参搜索字符串，二参搜索类型，三参搜索区域（空字符串代表全国）
        latLonPoint?.let {
            PoiSearch(this@MapActivity, poiSearchQuery).apply {
                setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
                    override fun onPoiItemSearched(poiItem: PoiItem?, rcode: Int) {}
                    override fun onPoiSearched(result: PoiResult?, rcode: Int) {
                        when (rcode) {
                            AMapException.CODE_AMAP_SUCCESS -> result?.query?.let {
                                if (result.query == poiSearchQuery) {
                                    poiItems = result.apply { poiResult = this }.pois
                                    poiResult?.searchSuggestionCitys.let { suggestionCities ->
                                        when {
                                            poiItems?.isNotEmpty() == true -> {
                                                poiDetail?.visibility = View.GONE
                                                lastMarker?.let { resetLastMarker }
                                                poiOverlay?.removeFromMap
                                                aMap?.clear()
                                                poiOverlay = InnerPoiOverlay(aMap, poiItems).apply {
                                                    addToMap
                                                    zoomToSpan
                                                }
                                                aMap?.addMarker(
                                                    MarkerOptions().anchor(0.5f, 0.5f)
                                                        .position(latLonPoint.toLatLng).icon(
                                                            BitmapDescriptorFactory.fromBitmap(
                                                                BitmapFactory.decodeResource(
                                                                    resources, R.mipmap.point4
                                                                )
                                                            )
                                                        )
                                                )
                                                aMap?.addCircle(
                                                    CircleOptions()
                                                        .center(latLonPoint.toLatLng).radius(5000.0)
                                                        .strokeWidth(2f).strokeColor(Color.BLUE)
                                                        .fillColor(Color.argb(50, 1, 1, 1))
                                                )
                                            }
                                            suggestionCities?.isNotEmpty() == true ->
                                                showSuggestCity(suggestionCities)
                                            else -> showShort(R.string.no_result)
                                        }
                                    }
                                }
                            } ?: showShort(R.string.no_result)
                            else -> showError(rcode)
                        }
                    }
                })
                bound = PoiSearch.SearchBound(it, 5000, true)
                poiSearch = this
            }.searchPOIAsyn()
        }
    }

    private val markers: IntArray = intArrayOf(
        R.mipmap.poi_marker_1,
        R.mipmap.poi_marker_2,
        R.mipmap.poi_marker_3,
        R.mipmap.poi_marker_4,
        R.mipmap.poi_marker_5,
        R.mipmap.poi_marker_6,
        R.mipmap.poi_marker_7,
        R.mipmap.poi_marker_8,
        R.mipmap.poi_marker_9,
        R.mipmap.poi_marker_10
    )

    private inner class InnerPoiOverlay(
        private val aMap: AMap?, private val poiList: MutableList<PoiItem>?
    ) {
        private val poiMarkers: MutableList<Marker> = mutableListOf()
        val addToMap = poiList?.run {
            for (i in indices) {
                aMap?.addMarker(getMarkerOptions(i))?.apply { setObject(poiList[i]) }
                    ?.let { poiMarkers.add(it) }
            }
        }

        private fun getMarkerOptions(index: Int): MarkerOptions? = poiList?.get(index)?.run {
            MarkerOptions().position(latLonPoint.toLatLng).title(title).snippet(snippet)
                .icon(getBitmapDescriptor(index))
        }

        private fun getBitmapDescriptor(index: Int): BitmapDescriptor = when {
            index < 10 -> BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, markers[index])
            )
            else -> BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.mipmap.marker_other_highlight)
            )
        }

        val removeFromMap = {
            for (mark in poiMarkers) {
                mark.remove()
            }
        }
        private val latLngBounds: LatLngBounds
            get() = LatLngBounds.builder().apply {
                poiList?.let {
                    for (poi in it) {
                        include(poi.latLonPoint.toLatLng)
                    }
                }
            }.build()
        val zoomToSpan = {
            if (poiList?.isNotEmpty() == true)
                aMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100))
        }

        fun getPoiItem(index: Int): PoiItem? =
            poiList?.run { if (index in indices) this[index] else null }

        fun getPoiIndex(marker: Marker?): Int {
            for ((index, value) in poiMarkers.withIndex()) {
                if (value == marker) return index
            }
            return -1
        }
    }

    var cityNameTv: TextView? = null//TODO
    var reportTimeLive: TextView? = null//TODO
    var weatherTv: TextView? = null//TODO
    var temperatureTv: TextView? = null//TODO
    var windTv: TextView? = null//TODO
    var humidityTv: TextView? = null//TODO
    var reportTimeForecast: TextView? = null//TODO
    var forecastTv: TextView? = null//TODO

    @JvmOverloads
    fun searchWeatherLiveOrForecast(cityName: String, isLive: Boolean = true) =
        WeatherSearch(this).apply {
            query = WeatherSearchQuery(
                cityName, when {
                    isLive -> WeatherSearchQuery.WEATHER_TYPE_LIVE
                    else -> WeatherSearchQuery.WEATHER_TYPE_FORECAST
                }
            )
            setOnWeatherSearchListener(object : WeatherSearch.OnWeatherSearchListener {
                override fun onWeatherLiveSearched(result: LocalWeatherLiveResult?, rCode: Int) {
                    when (rCode) {
                        AMapException.CODE_AMAP_SUCCESS -> result?.liveResult?.run {
                            cityNameTv?.text = cityName
                            reportTimeLive?.text = "${reportTime}发布"
                            weatherTv?.text = weather
                            temperatureTv?.text = "${temperature}°"
                            windTv?.text = "${windDirection}风${windPower}级"
                            humidityTv?.text = "湿度${humidity}%"
                        } ?: showShort(R.string.no_result)
                        else -> showError(rCode)
                    }
                }

                override fun onWeatherForecastSearched(
                    result: LocalWeatherForecastResult?, rCode: Int
                ) {
                    when (rCode) {
                        AMapException.CODE_AMAP_SUCCESS -> when {
                            (result?.forecastResult?.weatherForecast?.size
                                ?: 0) > 0 -> result?.forecastResult?.run {
                                cityNameTv?.text = cityName
                                reportTimeForecast?.text = "${reportTime}发布"
                                var forecast = ""
                                for (localDayWeatherForecast in weatherForecast) {
                                    localDayWeatherForecast.run {
                                        val week: String = when (week.toInt()) {
                                            1 -> "周一"
                                            2 -> "周二"
                                            3 -> "周三"
                                            4 -> "周四"
                                            5 -> "周五"
                                            6 -> "周六"
                                            7 -> "周日"
                                            else -> ""
                                        }
                                        forecast += "$date$week${
                                            String
                                                .format("%-3s/%3s", "${dayTemp}°", "$nightTemp°")
                                        }\n\n"
                                    }
                                }
                                forecastTv?.text = forecast
                            }
                            else -> showShort(R.string.no_result)
                        }
                        else -> showError(rCode)
                    }
                }
            })
        }.searchWeatherAsyn()

    var urlView: WebView? = null//TODO
    private val shareSearch: ShareSearch = ShareSearch(AKit.app).apply {
        setOnShareSearchListener(object : ShareSearch.OnShareSearchListener {
            override fun onBusRouteShareUrlSearched(url: String, errorCode: Int) {
                dismissProgressDialog
                when (errorCode) {
                    AMapException.CODE_AMAP_SUCCESS -> urlView?.loadUrl(url)
                    else -> showError(errorCode)
                }
            }

            override fun onDrivingRouteShareUrlSearched(url: String, errorCode: Int) {
                dismissProgressDialog
                when (errorCode) {
                    AMapException.CODE_AMAP_SUCCESS -> urlView?.loadUrl(url)
                    else -> showError(errorCode)
                }
            }

            override fun onWalkRouteShareUrlSearched(url: String, errorCode: Int) {
                dismissProgressDialog
                when (errorCode) {
                    AMapException.CODE_AMAP_SUCCESS -> urlView?.loadUrl(url)
                    else -> showError(errorCode)
                }
            }

            override fun onPoiShareUrlSearched(url: String, errorCode: Int) {
                dismissProgressDialog
                when (errorCode) {
                    AMapException.CODE_AMAP_SUCCESS -> urlView?.loadUrl(url)
                    else -> showError(errorCode)
                }
            }

            override fun onLocationShareUrlSearched(url: String, errorCode: Int) {
                dismissProgressDialog
                when (errorCode) {
                    AMapException.CODE_AMAP_SUCCESS -> urlView?.loadUrl(url)
                    else -> showError(errorCode)
                }
            }

            override fun onNaviShareUrlSearched(url: String, errorCode: Int) {
                dismissProgressDialog
                when (errorCode) {
                    AMapException.CODE_AMAP_SUCCESS -> urlView?.loadUrl(url)
                    else -> showError(errorCode)
                }
            }
        })
    }

    fun shareRoute(start: LatLonPoint, end: LatLonPoint) {
        addRouteMarker(start, end)
        showProgressDialog
        shareSearch.searchDrivingRouteShareUrlAsyn(
            ShareSearch.ShareDrivingRouteQuery(
                ShareSearch.ShareFromAndTo(start, end), ShareSearch.DrivingDefault
            )
        )
    }

    private fun addRouteMarker(start: LatLonPoint, end: LatLonPoint) {
        aMap?.clear()
        addMarker(start, "", "", BitmapDescriptorFactory.fromResource(R.mipmap.start))
        addMarker(end, "", "", BitmapDescriptorFactory.fromResource(R.mipmap.end))
        LatLngBounds.builder().apply {
            include(start.toLatLng)
            include(end.toLatLng)
        }.build().let { aMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(it, 50)) }
    }

    private fun addMarker(
        latLonPoint: LatLonPoint, title: String, snippet: String, icon: BitmapDescriptor
    ) = MarkerOptions().position(latLonPoint.toLatLng).title(title).snippet(snippet).icon(icon)
        .let { aMap?.addMarker(it) }

    fun sharePoi(poiPoint: LatLonPoint, title: String, snippet: String) {
        addPoiMarker(poiPoint, title, snippet)
        showProgressDialog
        shareSearch.searchPoiShareUrlAsyn(PoiItem(null, poiPoint, title, snippet))
    }

    private fun addPoiMarker(poiPoint: LatLonPoint, title: String, snippet: String) {
        aMap?.clear()
        addMarker(poiPoint, title, snippet, BitmapDescriptorFactory.defaultMarker())
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(poiPoint.toLatLng, 18f))
    }

    fun shareLocation(poiPoint: LatLonPoint, title: String, snippet: String) {
        addLocationMarker(poiPoint, title, snippet)
        showProgressDialog
        shareSearch.searchLocationShareUrlAsyn(
            LatLonSharePoint(poiPoint.latitude, poiPoint.longitude, snippet)
        )
    }

    private fun addLocationMarker(poiPoint: LatLonPoint, title: String, snippet: String) {
        aMap?.clear()
        addMarker(
            poiPoint, title, snippet, BitmapDescriptorFactory.fromResource(R.mipmap.location_marker)
        )
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(poiPoint.toLatLng, 18f))
    }

    fun shareNavi(start: LatLonPoint, end: LatLonPoint) {
        addRouteMarker(start, end)
        showProgressDialog
        shareSearch.searchNaviShareUrlAsyn(
            ShareSearch.ShareNaviQuery(
                ShareSearch.ShareFromAndTo(start, end), ShareSearch.NaviDefault
            )
        )
    }

    private var offlineListAdapter: MapOfflineListAdapter? = null
    private var offlineDownloadedAdapter: MapOfflineDownloadedAdapter? = null
    var contentViewPage: ViewPager? = null//TODO
    private val offlineHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                LIST_UPDATE -> when (contentViewPage?.currentItem) {
                    0 -> offlineListAdapter?.notifyDataSetChanged()
                    else -> offlineDownloadedAdapter?.notifyDataChange
                }
                MSG_SHOW -> showTvToast(this@MapActivity, msg.obj as String)
                DIALOG_DISMISS -> {
                    progressDialog?.dismiss()
                    sendEmptyMessage(LIST_UPDATE)
                }
                DIALOG_SHOW -> progressDialog?.show()
            }
        }
    }
    private var allOfflineMapList: ExpandableListView? = null
    private val provinceList: MutableList<OfflineMapProvince?> = mutableListOf()
    private val initProvinceListAndCityMap = {
        provinceList.add(null)
        provinceList.add(null)
        provinceList.add(null)//添加3个null防止后面添加出现index out of bounds
        val municipalityList: ArrayList<OfflineMapCity> = arrayListOf()
        val hkMacaoList: ArrayList<OfflineMapCity> = arrayListOf()
        val sketchList: ArrayList<OfflineMapCity> = arrayListOf()
        offlineMapManager?.offlineMapProvinceList?.run {
            for ((index, province) in withIndex()) {
                when (province.cityList.size) {
                    1 -> {
                        val name: String = province.provinceName
                        when {
                            name.contains("香港") -> hkMacaoList.addAll(province.cityList)
                            name.contains("澳门") -> hkMacaoList.addAll(province.cityList)
                            name.contains("全国概要图") -> sketchList.addAll(province.cityList)
                            else -> municipalityList.addAll(province.cityList)
                        }
                    }
                    else -> provinceList.add(index + 3, province)
                }
            }
        }
        provinceList[0] = OfflineMapProvince().apply {
            provinceName = "概要图"
            cityList = sketchList
        }
        provinceList[1] = OfflineMapProvince().apply {
            provinceName = "直辖市"
            cityList = municipalityList
        }
        provinceList[2] = OfflineMapProvince().apply {
            provinceName = "港澳"
            cityList = hkMacaoList
        }
    }
    private val initAllCityList = {
        allOfflineMapList = LayoutInflater.from(this@MapActivity)
            .inflate(R.layout.offline_list_download, null)
            .findViewById(R.id.province_list_download)
        initProvinceListAndCityMap
        MapOfflineListAdapter(this@MapActivity, offlineMapManager, provinceList)
            .apply { offlineListAdapter = this }.let {
                allOfflineMapList?.apply {
                    setAdapter(it)
                    setOnGroupExpandListener(it)
                    setOnGroupCollapseListener(it)
                    setGroupIndicator(null)
                }
            }
    }
    private var downLoadedList: ListView? = null
    private val initDownloadedList = {
        downLoadedList = (LayoutInflater.from(this@MapActivity)
            .inflate(R.layout.offline_list_downloaded, null) as ListView).apply {
            layoutParams = AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT
            )
            adapter = MapOfflineDownloadedAdapter(this@MapActivity, offlineMapManager)
                .apply { offlineDownloadedAdapter = this }
        }
    }
    private var pageAdapter: PagerAdapter? = null
    var textDownload: TextView? = null//TODO
    var textDownloaded: TextView? = null//TODO
    private val initViewPager = contentViewPage?.apply {
        adapter = allOfflineMapList?.let { all ->
            downLoadedList?.let { downLoaded ->
                MapOfflinePagerAdapter(contentViewPage, all, downLoaded)
                    .apply { pageAdapter = this }
            }
        }
        currentItem = 0
        setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(arg0: Int) {}
            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageSelected(arg0: Int) {
                textDownloaded?.apply {
                    val paddingH = paddingLeft
                    val paddingV = paddingTop
                    when (arg0) {
                        0 -> setBackgroundResource(R.mipmap.offlinearrow_tab2_normal)
                        1 -> setBackgroundResource(R.mipmap.offlinearrow_tab2_pressed)
                    }
                    setPadding(paddingH, paddingV, paddingH, paddingV)
                    textDownload?.apply {
                        when (arg0) {
                            0 -> setBackgroundResource(R.mipmap.offlinearrow_tab1_pressed)
                            1 -> setBackgroundResource(R.mipmap.offlinearrow_tab1_normal)
                        }
                        setPadding(paddingH, paddingV, paddingH, paddingV)
                    }
                }
                handler.sendEmptyMessage(LIST_UPDATE)
            }
        })
    }
    private val offlineMapManager: OfflineMapManager? = null
        get() = OfflineMapManager(
            AKit.app, object : OfflineMapManager.OfflineMapDownloadListener {
                override fun onDownload(status: Int, completeCode: Int, downName: String) {
                    when (status) {
                        OfflineMapStatus.SUCCESS -> {
                        }
                        OfflineMapStatus.LOADING -> debug("$loggerTag->download: $completeCode%,$downName")
                        OfflineMapStatus.UNZIP -> debug("$loggerTag->unzip: $completeCode%,$downName")
                        OfflineMapStatus.WAITING -> debug("$loggerTag->waiting: $completeCode%,$downName")
                        OfflineMapStatus.PAUSE -> debug("$loggerTag->pause: $completeCode%,$downName")
                        OfflineMapStatus.STOP -> {
                        }
                        OfflineMapStatus.ERROR -> error("$loggerTag->download:ERROR $downName")
                        OfflineMapStatus.EXCEPTION_AMAP -> error("$loggerTag->download:EXCEPTION_AMAP $downName")
                        OfflineMapStatus.EXCEPTION_NETWORK_LOADING -> {
                            error("$loggerTag->download:EXCEPTION_NETWORK_LOADING $downName")
                            showShort("网络异常")
                            field?.pause()
                        }
                        OfflineMapStatus.EXCEPTION_SDCARD -> error("$loggerTag->download:EXCEPTION_SDCARD $downName")
                        else -> {
                        }
                    }
                    offlineHandler.sendEmptyMessage(LIST_UPDATE)
                }

                override fun onCheckUpdate(hasNew: Boolean, name: String) {
                    info("$loggerTag->onCheckUpdate $name : $hasNew")
                    offlineHandler.sendMessage(Message().apply {
                        what = MSG_SHOW
                        obj = "CheckUpdate $name : $hasNew"
                    })
                }

                override fun onRemove(success: Boolean, name: String, describe: String) {
                    info("$loggerTag->onRemove $name : $success , $describe")
                    offlineHandler.run {
                        sendEmptyMessage(LIST_UPDATE)
                        sendMessage(Message().apply {
                            what = MSG_SHOW
                            obj = "onRemove $name : $success , $describe"
                        })
                    }
                }
            }).apply {
            setOnOfflineLoadedListener {
                initAllCityList
                initDownloadedList
                initViewPager
                dismissProgressDialog
            }
        }
    val offlineStartInPause = offlineMapManager?.run {
        for (mapCity in downloadingCityList) {
            if (mapCity.state == OfflineMapStatus.PAUSE) try {
                downloadByCityName(mapCity.city)
            } catch (e: com.amap.api.maps.AMapException) {
                e.printStackTrace()
            }
        }
    }
    val offlineCancelInPause = offlineMapManager?.run {
        for (mapCity in downloadingCityList) {
            if (mapCity.state == OfflineMapStatus.PAUSE) {
                remove(mapCity.city)
            }
        }
    }
    val offlineStop = offlineMapManager?.stop()
    val offlineLog = offlineMapManager?.run {
        for (offlineMapCity in downloadingCityList) {
            info("$loggerTag->${offlineMapCity.city},${offlineMapCity.state}")
        }
        for (offlineMapCity in downloadOfflineMapCityList) {
            info("$loggerTag->${offlineMapCity.city},${offlineMapCity.state}")
        }
    }
    var backImage: ImageView? = null//TODO
    override fun onClick(v: View) {
        when (v) {
            textDownload -> textDownload?.apply {
                val paddingH = paddingLeft
                val paddingV = paddingTop
                contentViewPage?.currentItem = 0
                setBackgroundResource(R.mipmap.offlinearrow_tab1_pressed)
                setPadding(paddingH, paddingV, paddingH, paddingV)
                textDownloaded?.apply {
                    setBackgroundResource(R.mipmap.offlinearrow_tab2_normal)
                    setPadding(paddingH, paddingV, paddingH, paddingV)
                }
                offlineDownloadedAdapter?.notifyDataChange
            }
            textDownloaded -> textDownloaded?.apply {
                val paddingH = paddingLeft
                val paddingV = paddingTop
                contentViewPage?.currentItem = 1
                textDownload?.apply {
                    setBackgroundResource(R.mipmap.offlinearrow_tab1_normal)
                    setPadding(paddingH, paddingV, paddingH, paddingV)
                }
                setBackgroundResource(R.mipmap.offlinearrow_tab2_pressed)
                setPadding(paddingH, paddingV, paddingH, paddingV)
                offlineDownloadedAdapter?.notifyDataChange
            }
            backImage -> finish()
        }
    }

    val metrePerPixel: Float? = aMap?.scalePerPixel
    fun distance(latLngA: LatLng, latLngB: LatLng): Float =
        AMapUtils.calculateLineDistance(latLngA, latLngB)

    val markersList: MutableList<Marker>? = aMap?.mapScreenMarkers
    val markersSize: Int? = markersList?.size
    fun LatLng.isContain(polygon: Polygon): Boolean = polygon.contains(this)
    val LatLng.isContain: Boolean
        get() = aMap?.projection?.visibleRegion?.latLngBounds?.contains(this) ?: false
    val LatLng.toPoint: Point?
        get() = aMap?.projection?.toScreenLocation(this)
    val Point.toLatLng: LatLng?
        get() = aMap?.projection?.fromScreenLocation(this)
    val types: Array<String> =
        arrayOf("BAIDU", "MAPBAR", "GPS", "MAPABC", "SOSOMAP", "ALIYUN", "GOOGLE")

    fun LatLng.Convert(type: String = types[0]): LatLng = let {
        CoordinateConverter(this@MapActivity).apply {
            from(CoordinateConverter.CoordType.valueOf(type))
            coord(it)
        }.convert()
    }

    val Marker.startNavi
        get() = let {
            try {
                AMapUtils.openAMapNavi(NaviPara().apply {
                    targetPoint = it.position
                    naviStyle = NaviPara.DRIVING_AVOID_CONGESTION//避免拥堵
                }, applicationContext)
            } catch (e: AMapException) {
                AMapUtils.getLatestAMapApp(applicationContext)
            }
        }
    private var inputListView: ListView? = null//TODO
    val AutoCompleteTextView.watchText
        get() = addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                InputtipsQuery(
                    s.toString().trim { it <= ' ' }, text.toString()
                ).apply { cityLimit = true }
                    .let { query ->
                        Inputtips(this@MapActivity, query).apply {
                            setInputtipsListener { tipList: MutableList<Tip>, rCode: Int ->
                                when (rCode) {
                                    AMapException.CODE_AMAP_SUCCESS -> mutableListOf<MutableMap<String, String?>>().apply {
                                        for (tip in tipList) {
                                            mutableMapOf<String, String?>().apply {
                                                this["name"] = tip.name
                                                this["address"] = tip.district
                                            }.let { add(it) }
                                        }
                                    }.let {
                                        SimpleAdapter(
                                            applicationContext, it,
                                            R.layout.layout_item, arrayOf("name", "address"),
                                            intArrayOf(R.id.poi_field_id, R.id.poi_value_id)
                                        ).apply { inputListView?.adapter = this }
                                            .notifyDataSetChanged()
                                    }
                                    else -> showError(rCode)
                                }
                            }
                        }.requestInputtipsAsyn()
                    }
            }
        })
    val MutableList<LatLng>?.startMove
        get() = this?.let { points ->
            aMap?.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds(points[0], points[points.size - 2]), 50
                )
            )
            SmoothMoveMarker(aMap).apply {
                val pair: Pair<Int, LatLng> =
                    SpatialRelationUtil.calShortestDistancePoint(points, points[0])
                points[pair.first] = points[0]
                setPoints(points.subList(pair.first, points.size))
                setTotalDuration(40)
                setDescriptor(BitmapDescriptorFactory.fromResource(R.mipmap.icon_car))
                setMoveListener { distance ->
                    runOnUiThread {
                        infoWindowLayout?.let { title?.text = "距离终点还有： ${distance}米" }
                    }
                }
                aMap?.setInfoWindowAdapter(object : AMap.InfoWindowAdapter {
                    override fun getInfoWindow(marker: Marker): View = getInfoWindowView(marker)
                    override fun getInfoContents(marker: Marker): View = getInfoWindowView(marker)
                })
                marker.showInfoWindow()
            }.startSmoothMove()
        } ?: showTvToast(this@MapActivity, "请先设置路线")
    private var infoWindowLayout: LinearLayout? = null
    private var title: TextView? = null
    private fun getInfoWindowView(marker: Marker): View = infoWindowLayout
        ?: LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.mipmap.infowindow_bg)
            addView(TextView(this@MapActivity).apply {
                setTextColor(Color.BLACK)
                title = this
            })
            addView(TextView(this@MapActivity).apply { setTextColor(Color.BLACK) })
            infoWindowLayout = this
        }

    private var manager: SensorManager? = null
    private val accelerometer: Sensor? = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticField: Sensor? = manager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val register = {
        manager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        manager?.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL)
    }
    val unRegister = manager?.unregisterListener(this)
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    private var accelerometerValues = FloatArray(3)//加速度值
    private var magneticValues = FloatArray(3)//地磁值

    interface OnOrientationListener {
        fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float)
    }

    var markerRotate: Marker? = null
    var onOrientationListener: OnOrientationListener? = object : OnOrientationListener {
        override fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float) {
            markerRotate?.rotateAngle = azimuth
        }
    }//TODO

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) accelerometerValues = event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) magneticValues = event.values
        FloatArray(9).apply {
            SensorManager.getRotationMatrix(this, null, accelerometerValues, magneticValues)
        }.let { rArray ->
            FloatArray(3).apply { SensorManager.getOrientation(rArray, this) }.run {
                val azimuth = Math.toDegrees(this[0].toDouble()).toFloat()
                    .let { if (it < 0) it + 360f else it } / 5 * 5//航向角，正北左负右正,处理表示以5°为幅度
                val pitch = Math.toDegrees(this[1].toDouble()).toFloat()//俯仰角
                val roll = Math.toDegrees(this[2].toDouble()).toFloat()//翻滚角
                onOrientationListener?.onOrientationChanged(azimuth, pitch, roll)
            }
        }
    }

    private var traceClient: LBSTraceClient? = null
    val startTrace = {
        if (traceClient == null) traceClient = LBSTraceClient.getInstance(this@MapActivity)
        traceClient?.startTrace(this)
    }
    val stopTrace = {
        if (traceClient == null) traceClient = LBSTraceClient.getInstance(this@MapActivity)
        traceClient?.stopTrace()
    }
    private var traceOverlay: TraceOverlay? = null
    override fun onTraceStatus(
        locations: MutableList<TraceLocation?>, rectifications: MutableList<LatLng?>,
        errorInfo: String?
    ) {
        error("$loggerTag->source count->${locations.size};result count->${rectifications.size}")
        traceOverlay?.remove()
        traceOverlay = TraceOverlay(aMap, rectifications)
        traceOverlay?.zoopToSpan()
    }

    private var traceList: MutableList<TraceLocation> =
        parseLocationsData(AKit.app.assets, "traceRecord${File.separator}AMapTrace.txt")

    private fun parseLocationsData(
        assetManager: AssetManager, filePath: String
    ): MutableList<TraceLocation> = mutableListOf<TraceLocation>().apply {
        try {
            assetManager.open(filePath).use { inputStream ->
                InputStreamReader(inputStream).use { inputStreamReader ->
                    BufferedReader(inputStreamReader).use { bufferedReader ->
                        while (true) {
                            bufferedReader.readLine()?.let { line ->
                                JSONObject(line).run {
                                    TraceLocation().apply {
                                        latitude = optDouble("lat")
                                        longitude = optDouble("lon")
                                        bearing = optDouble("bearing").toFloat()
                                        time = optLong("loctime")
                                        speed = optDouble("speed").toFloat()
                                    }.let { add(it) }
                                }
                            } ?: break
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var recordChooseArray: Array<String>? = recordNames(AKit.app.assets)
    private fun recordNames(assetManager: AssetManager): Array<String>? = try {
        assetManager.list("traceRecord")
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    private var recordChoose: Spinner = Spinner(AKit.app).apply {
        adapter = recordChooseArray?.let {
            ArrayAdapter(AKit.app, android.R.layout.simple_spinner_item, it)
                .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View, pos: Int, id: Long) {
                traceList.clear()
                sequenceLineID = 1000 + pos
                recordChooseArray?.get(pos)?.run {
                    traceList =
                        parseLocationsData(AKit.app.assets, "traceRecord${File.separator}$this")
                    coordinateType = when {
                        startsWith("AMap") -> LBSTraceClient.TYPE_AMAP
                        startsWith("Baidu") -> LBSTraceClient.TYPE_BAIDU
                        startsWith("GPS") -> LBSTraceClient.TYPE_GPS
                        else -> LBSTraceClient.TYPE_AMAP
                    }
                }
            }
        }
    }
    private val overlayList: ConcurrentMap<Int, TraceOverlay?> =
        ConcurrentHashMap<Int, TraceOverlay?>()

    override fun onRequestFailed(lineID: Int, errorInfo: String?) {
        debug("$loggerTag->onRequestFailed")
        showShort(errorInfo)
        if (overlayList.containsKey(lineID)) (overlayList[lineID] as TraceOverlay)
            .apply { traceStatus = TraceOverlay.TRACE_STATUS_FAILURE }
            .run { setDistanceAndWait(this) }
    }

    var distanceSum: String = ""
    var timeWait: String = ""
    private fun setDistanceAndWait(overlay: TraceOverlay?) = DecimalFormat("0.0").run {
        overlay?.run {
            distanceSum = "总距离：${format(distance / 1000.0)} KM"
            timeWait = "等   待：${format(waitTime / 60.0)} 分钟"
        }
    }

    override fun onTraceProcessing(lineID: Int, index: Int, segments: MutableList<LatLng?>?) {
        debug("$loggerTag->onTraceProcessing")
        segments?.let {
            if (overlayList.containsKey(lineID)) (overlayList[lineID] as TraceOverlay)
                .apply { traceStatus = TraceOverlay.TRACE_STATUS_PROCESSING }.add(it)
        }
    }

    override fun onFinished(
        lineID: Int, linepoints: MutableList<LatLng?>?, distanceSum: Int, timeWait: Int
    ) {
        debug("$loggerTag->onFinished")
        showShort("onFinished")
        if (overlayList.containsKey(lineID)) (overlayList[lineID] as TraceOverlay).apply {
            traceStatus = TraceOverlay.TRACE_STATUS_FINISH
            distance = distanceSum
            waitTime = timeWait
        }.run { setDistanceAndWait(this) }
    }

    private var sequenceLineID = 1000
    var coordinateType: Int = LBSTraceClient.TYPE_AMAP//TYPE_GPS、TYPE_BAIDU
    val traceGrasp = when {
        overlayList.containsKey(sequenceLineID) -> overlayList[sequenceLineID]?.let {
            it.zoopToSpan()
            when (it.traceStatus) {
                TraceOverlay.TRACE_STATUS_FAILURE -> "该线路轨迹纠偏失败"
                TraceOverlay.TRACE_STATUS_PREPARE -> "该线路轨迹纠偏开始"
                TraceOverlay.TRACE_STATUS_PROCESSING -> "该线路轨迹纠偏进行".apply { setDistanceAndWait(it) }
                TraceOverlay.TRACE_STATUS_FINISH -> "该线路轨迹纠偏完成".apply { setDistanceAndWait(it) }
                else -> ""
            }.let { tip -> showShort(tip) }
        }
        else -> {
            overlayList[sequenceLineID] = TraceOverlay(aMap)
                .apply { setProperCamera(traceLocation2LatLng(traceList)) }
            distanceSum = "总距离："
            timeWait = "等   待："
            traceClient = LBSTraceClient.getInstance(AKit.app)
            traceClient?.queryProcessedTrace(sequenceLineID, traceList, coordinateType, this)
        }
    }

    private fun traceLocation2LatLng(traceLocationList: MutableList<TraceLocation>?): MutableList<LatLng> =
        mutableListOf<LatLng>().apply {
            traceLocationList?.let {
                for (location in it) {
                    add(LatLng(location.latitude, location.longitude))
                }
            }
        }

    val cleanFinishTrace = {
        for ((key, value) in overlayList) {
            (value as TraceOverlay).run {
                if (traceStatus == TraceOverlay.TRACE_STATUS_FAILURE ||
                    traceStatus == TraceOverlay.TRACE_STATUS_FINISH
                ) {
                    remove()
                    overlayList.remove(key)
                }
            }
        }
    }

    fun setMapFragment(position: LatLng) {
        TextureMapFragment.newInstance(AMapOptions().apply {
            mapType(AMap.MAP_TYPE_NORMAL)
            logoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT)
            scrollGesturesEnabled(true)
            zoomGesturesEnabled(true)
            tiltGesturesEnabled(true)
            rotateGesturesEnabled(true)
            scaleControlsEnabled(true)
            zoomControlsEnabled(true)
            compassEnabled(true)
            zOrderOnTop(true)
            CameraPosition.Builder().target(position).zoom(18f).tilt(30f).bearing(0f).build()
                .let { camera(it) }
        }).run { fragmentManager.beginTransaction().add(R.id.content, this, "map").commit() }
    }

    private var alarmReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "LOCATION") aMapLocationClient?.startLocation()
        }
    }
    var aMapLocationClientOption: AMapLocationClientOption? =
        AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy//定位模式，默认高精度
            isGpsFirst = false//GPS优先，仅高精度模式下有效，默认false
            httpTimeOut = 30000//网络请求超时，默认30秒，仅设备模式下无效
            interval = 2000//定位间隔，默认2秒
            isNeedAddress = true//返回逆地理地址信息，默认true
            isOnceLocation = false//单次定位，默认false
            isOnceLocationLatest = false//WiFi刷新，默认false；true自动变为单次定位，持续定位时不使用
            AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP)//网络请求协议，HTTP或HTTPS，默认HTTP
            isSensorEnable = false//传感器，默认false
            isWifiScan = true//WiFi扫描，默认true；false停止主动刷新，完全依赖系统刷新，位置可能存在误差
            isLocationCacheEnable = true//缓存定位，默认true
        }//Hight_Accuracy、Device_Sensors、Battery_Saving
    var locationMsg: String = ""
    val AMapLocation.locationStr: String
        get() = run {
            StringBuffer().apply {
                when (errorCode) {
                    0 -> append(
                        """定位成功
                        定位类型:$locationType
                        经    度:$longitude
                        纬    度:$latitude
                        精    度:${accuracy}米
                        提供  者:$provider
                        速    度:${speed}米/秒
                        角    度:$bearing
                        星    数:$satellites
                        国    家:$country
                        省      :$province
                        市      :$city
                        城市编码:$cityCode
                        区      :$district
                        区域  码:$adCode
                        地    址:$address
                        兴趣  点:$poiName
                        定位时间:${DateKit.sdfDateByFullEn.format(nowMillis)}"""
                    )
                    else -> append(
                        """定位失败
                        错误  码:$errorCode
                        错误信息:$errorInfo
                        错误描述:$locationDetail"""
                    )
                }
                append(
                    """定位质量报告
                WIFI开关:${if (locationQualityReport.isWifiAble) "开启" else "关闭"}
                GPS 状态:${
                        when (locationQualityReport.gpsStatus) {
                            AMapLocationQualityReport.GPS_STATUS_OK -> "GPS状态正常"
                            AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER -> "手机中没有GPS Provider，无法进行GPS定位"
                            AMapLocationQualityReport.GPS_STATUS_OFF -> "GPS关闭，建议开启GPS，提高定位质量"
                            AMapLocationQualityReport.GPS_STATUS_MODE_SAVING -> "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量"
                            AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION -> "没有GPS定位权限，建议开启gps定位权限"
                            else -> ""
                        }
                    }
                GPS 星数:${locationQualityReport.gpsSatellites}
                回调时间:${DateKit.sdfDateByFullEn.format(nowMillis)}"""
                )
            }.toString()
        }
    private val locationHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun dispatchMessage(msg: Message) {
            locationMsg = when (msg.what) {
                MSG_LOCATION_START -> "正在定位..."
                MSG_LOCATION_FINISH -> (msg.obj as AMapLocation).locationStr
                MSG_LOCATION_STOP -> "定位停止"
                else -> ""
            }
        }
    }
    var errorStr: String = ""
    private val aMapLocationListener: AMapLocationListener = AMapLocationListener { location ->
        location?.let {
            locationHandler.run {
                sendMessage(obtainMessage().apply {
                    obj = it
                    what = MSG_LOCATION_FINISH
                })
            }
        }
        errorStr = location?.locationStr ?: "定位失败，loc is null"
    }
    private var aMapLocationClient: AMapLocationClient? =
        AMapLocationClient(AKit.app).apply {
            setLocationOption(aMapLocationClientOption)
            setLocationListener(aMapLocationListener)
        }
    var alarmInterval: Int = 5
    private val alarmIntent: PendingIntent =
        PendingIntent.getBroadcast(AKit.app, 0, Intent().apply { action = "LOCATION" }, 0)
    val locationStart = {
        aMapLocationClient?.apply { setLocationOption(aMapLocationClientOption) }?.startLocation()
        locationHandler.sendEmptyMessage(MSG_LOCATION_START)
        alarmManager.setRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 2 * 1000,
            alarmInterval * 1000.toLong(), alarmIntent
        )
    }
    val locationStop = {
        aMapLocationClient?.stopLocation()
        locationHandler.sendEmptyMessage(MSG_LOCATION_STOP)
        alarmManager.cancel(alarmIntent)
    }
    private var webView: WebView? = WebView(AKit.app).apply {
        settings.apply {
            javaScriptEnabled = true
            setGeolocationEnabled(false)//不许地理定位（H5辅助定位）；允许地理定位，地理定位失败H5辅助定位
        }
        webViewClient = object : android.webkit.WebViewClient() {}
        webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onJsAlert(
                view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?
            ): Boolean = true

            override fun onJsConfirm(
                view: WebView?, url: String?, message: String?, result: android.webkit.JsResult?
            ): Boolean = true

            override fun onGeolocationPermissionsShowPrompt(
                origin: String?, callback: android.webkit.GeolocationPermissions.Callback?
            ) {
                callback?.invoke(origin, true, false)
                super.onGeolocationPermissionsShowPrompt(origin, callback)
            }

            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                window.setFeatureInt(android.view.Window.FEATURE_PROGRESS, newProgress * 100)
                super.onProgressChanged(view, newProgress)
            }
        }
        loadUrl("file:///android_asset/location.html")
    }
    val assistantStart = aMapLocationClient?.startAssistantLocation(webView)
    val assistantStop = aMapLocationClient?.stopAssistantLocation()
    val locationLast: AMapLocation? = aMapLocationClient?.lastKnownLocation
    val locationDestroy = aMapLocationClient?.run {
        onDestroy()
        aMapLocationClient = null
        aMapLocationClientOption = null
        webView?.destroy()
        webView = null
        alarmReceiver?.let { alarmReceiver = null }//在此取消注册出错unregisterReceiver(it)
        geoFenceReceiver?.let { geoFenceReceiver = null }//在此取消注册出错unregisterReceiver(it)
    }//在此移除地理围栏出错geoFenceClient.removeGeoFence()
    val DPoint.isAvailable: Boolean
        get() = com.amap.api.location.CoordinateConverter.isAMapDataAvailable(latitude, longitude)

    fun DPoint.Convert(type: String = types[0]): DPoint = let {
        com.amap.api.location.CoordinateConverter(applicationContext).apply {
            from(com.amap.api.location.CoordinateConverter.CoordType.valueOf(type))
            coord(it)
        }.convert()
    }

    private val lock: Any = Any()
    private var fenceList: MutableList<GeoFence?>? = mutableListOf()
    private val fenceMap: MutableMap<String?, GeoFence?>? = mutableMapOf()
    private val drawFence2Map = object : Thread() {
        override fun run() {
            try {
                synchronized(lock) {
                    fenceList?.run {
                        if (isNotEmpty()) for (fence in this) {
                            fenceMap?.let {
                                if (!it.containsKey(fence?.fenceId)) {
                                    drawFence(fence)
                                    it[fence?.fenceId] = fence
                                }
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
            }
        }
    }.start()
    private val boundsBuilder: LatLngBounds.Builder? = LatLngBounds.Builder()
    private val polygonPoints: MutableList<LatLng?> = mutableListOf()
    private var markerOption: MarkerOptions = MarkerOptions().draggable(true)
    private var centerMarker: Marker? = null
    private val markerList: MutableList<Marker?> = mutableListOf()
    private val removeMarkers = {
        centerMarker?.run {
            remove()
            centerMarker = null
        }
        markerList.run {
            if (size > 0) {
                for (marker in this) {
                    marker?.remove()
                }
                clear()
            }
        }
    }

    fun addCenterMarker(latLng: LatLng?) {
        if (centerMarker == null) centerMarker = aMap?.addMarker(markerOption)
        markerList.add(centerMarker?.apply {
            position = latLng
            isVisible = true
        })
    }

    fun addPolygonMarker(latLng: LatLng?) {
        polygonPoints.add(latLng)
        markerList.add(aMap?.addMarker(markerOption)?.apply { position = latLng })
    }

    private fun drawFence(fence: GeoFence?) {
        when (fence?.type) {
            GeoFence.TYPE_ROUND, GeoFence.TYPE_AMAPPOI -> drawCircle(fence)
            GeoFence.TYPE_POLYGON, GeoFence.TYPE_DISTRICT -> drawPolygon(fence)
        }
        aMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder?.build(), 150))
        polygonPoints.clear()
        removeMarkers
    }

    private val strokeWidth = 5f
    private val strokeColor: Int = Color.argb(180, 63, 145, 252)
    private val fillColor: Int = Color.argb(163, 118, 212, 243)
    private fun drawCircle(fence: GeoFence?) = fence?.run {
        center.run { LatLng(latitude, longitude) }.let {
            CircleOptions().center(it).radius(radius.toDouble()).strokeWidth(strokeWidth)
                .strokeColor(strokeColor).fillColor(fillColor)
                .let { options -> aMap?.addCircle(options) }
            boundsBuilder?.include(it)
        }
    }

    private fun drawPolygon(fence: GeoFence?) = fence?.pointList?.run {
        if (isNotEmpty()) for (subList in this) {
            mutableListOf<LatLng?>().apply {
                subList?.let {
                    for (point in it) {
                        point?.run {
                            add(LatLng(latitude, longitude))
                            boundsBuilder?.include(LatLng(latitude, longitude))
                        }
                    }
                }
            }.let { latLngList ->
                PolygonOptions().addAll(latLngList).strokeWidth(strokeWidth)
                    .strokeColor(strokeColor).fillColor(fillColor)
                    .let { aMap?.addPolygon(it) }
            }
        }
    }

    var locationInfo: String = ""
    private val fenceHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                0 -> {
                    StringBuffer().apply {
                        append("添加围栏成功")
                        (msg.obj as String).let { if (isNotSpace(it)) append("customId: $it") }
                    }.let {
                        showShort(it.toString())
                    }
                    drawFence2Map
                }
                1 -> showShort("添加围栏失败 ${msg.arg1}")
                2 -> locationInfo = "${msg.obj as String}\n"
            }
        }
    }
    private var geoFenceReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == GEO_FENCE_BROADCAST_ACTION) intent.extras?.run {
                val status = getInt(GeoFence.BUNDLE_KEY_FENCESTATUS)
                Message.obtain().apply {
                    obj = StringBuffer().apply {
                        when (status) {
                            GeoFence.STATUS_LOCFAIL -> append("定位失败")
                            GeoFence.STATUS_IN -> append("进入围栏 ")
                            GeoFence.STATUS_OUT -> append("离开围栏 ")
                            GeoFence.STATUS_STAYED -> append("停留在围栏内 ")
                        }
                        if (status != GeoFence.STATUS_LOCFAIL) {
                            getString(GeoFence.BUNDLE_KEY_CUSTOMID)
                                .let { if (isNotSpace(it)) append(" customId: $it") }
                            append(" fenceId: ${getString(GeoFence.BUNDLE_KEY_FENCEID)}")
                        }
                    }.toString()
                    what = 2
                }.let { fenceHandler.sendMessage(it) }
            }
        }
    }
    private val geoFenceClient: GeoFenceClient = GeoFenceClient(AKit.app).apply {
        createPendingIntent(GEO_FENCE_BROADCAST_ACTION)
        setGeoFenceListener { geoFenceList, errorCode, customId ->
            Message.obtain().apply {
                when (errorCode) {
                    GeoFence.ADDGEOFENCE_SUCCESS -> {
                        fenceList = geoFenceList
                        obj = customId
                        what = 0
                    }
                    else -> {
                        arg1 = errorCode
                        what = 1
                    }
                }
            }.let { fenceHandler.sendMessage(it) }
        }
        setActivateAction(GeoFenceClient.GEOFENCE_IN)//GEOFENCE_OUT、GEOFENCE_STAYED
    }
    val LatLng.toDPoint: DPoint
        get() = run { DPoint(latitude, longitude) }
    var customId: String = ""
    val addPolygonFence = when {
        polygonPoints.size < 3 -> showShort("参数不全")
        else -> mutableListOf<DPoint?>().apply {
            for (latLng in polygonPoints) {
                add(latLng?.toDPoint)
            }
        }.let { geoFenceClient.addGeoFence(it, customId) }
    }
    var keyword: String = ""
    val addDistrictFence = when {
        isSpace(keyword) -> showShort("参数不全")
        else -> geoFenceClient.addGeoFence(keyword, customId)
    }
    var centerLatLng: LatLng? = null
    var radiusStr: String = ""
    val addRoundFence = when {
        centerLatLng == null || isSpace(radiusStr) -> showShort("参数不全")
        else -> geoFenceClient.addGeoFence(centerLatLng?.toDPoint, radiusStr.toFloat(), customId)
    }
    var poiType: String = ""
    var sizeStr: String = ""
    val addNearbyFence = centerLatLng?.run {
        var aroundRadius = 3000f
        if (isNotSpace(radiusStr)) try {
            aroundRadius = radiusStr.toFloat()
        } catch (e: Throwable) {
        }
        var size = 10
        if (isNotSpace(sizeStr)) try {
            size = sizeStr.toInt()
        } catch (e: Throwable) {
        }
        geoFenceClient.addGeoFence(
            keyword, poiType, DPoint(latitude, longitude), aroundRadius, size, customId
        )
    } ?: showShort("参数不全")
    var city: String = ""
    val addKeywordFence = {
        var size = 10
        if (isNotSpace(sizeStr)) {
            try {
                size = sizeStr.toInt()
            } catch (e: Throwable) {
            }
        }
        when {
            isSpace(keyword) || isSpace(poiType) -> showShort("参数不全")
            else -> geoFenceClient.addGeoFence(keyword, poiType, city, size, customId)
        }
    }
    val clearGeoFence = geoFenceClient.removeGeoFence()
}