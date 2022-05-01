package top.autoget.automap

import android.content.Context
import android.graphics.Color
import com.amap.api.maps.AMap
import com.amap.api.maps.model.*
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.DrivePath
import com.amap.api.services.route.DriveStep
import com.amap.api.services.route.TMC
import top.autoget.automap.MapCommon.toLatLng
import kotlin.math.*

class MapRouteDriveOverlay(
    context: Context, aMap: AMap?, path: DrivePath, start: LatLonPoint, end: LatLonPoint,
    private val points: MutableList<LatLonPoint>?
) : MapRouteOverlay(context) {
    init {
        mAMap = aMap
        pointStart = start.toLatLng
        pointEnd = end.toLatLng
    }

    private var polylineOptions: PolylineOptions? = null
    override var routeWidth = 25f
    private val initPolylineOptions = {
        polylineOptions = null
        polylineOptions = PolylineOptions().color(colorDrive).width(routeWidth)
    }
    private val drivePath: DrivePath? = path
    private var tmcList: MutableList<TMC> = mutableListOf()
    private var latLngListOfPath: MutableList<LatLng> = mutableListOf()
    var throughPointMarkerVisible = true
        set(visible) = try {
            field = visible
            throughPointMarkers?.run {
                if (size > 0) for (marker in this) {
                    marker.isVisible = field
                }
            } ?: Unit
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    private val throughPointMarkers: MutableList<Marker>? = mutableListOf()
    private val addThroughPointMarker = points?.run {
        if (size > 0) for (latLonPoint in this) {
            mAMap?.addMarker(
                MarkerOptions().visible(throughPointMarkerVisible).icon(bdThrough)
                    .position(latLonPoint.toLatLng).title("\u9014\u7ECF\u70B9")
            )?.let { throughPointMarkers?.add(it) }
        }
    }
    var isColorFullLine = true
    private var polylineOptionsColor: PolylineOptions? = null
    val addToMap = {
        initPolylineOptions
        try {
            if (mAMap != null && routeWidth != 0f && drivePath != null) {
                addStartAndEndMarker
                polylineOptions?.apply {
                    add(pointStart)
                    for (driveStep in drivePath.steps) {
                        tmcList.addAll(driveStep.tmCs)
                        val points = driveStep.polyline
                        addStationMarkersDrive(driveStep, points[0].toLatLng)
                        for (point in points) {
                            add(point.toLatLng)
                            latLngListOfPath.add(point.toLatLng)
                        }
                    }
                    add(pointEnd)
                }
                addThroughPointMarker
                when {
                    isColorFullLine && tmcList.size > 0 -> {
                        colorWayUpdate(tmcList)
                        addPolyLine(polylineOptionsColor)
                    }
                    else -> addPolyLine(polylineOptions)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun addStationMarkersDrive(driveStep: DriveStep, position: LatLng) = driveStep.run {
        MarkerOptions().anchor(0.5f, 0.5f).visible(nodeIconVisible).icon(bdDrive).position(position)
            .title("\u65B9\u5411:${action}\n\u9053\u8DEF:${road}").snippet(instruction)
            .let { addStationMarker(it) }
    }

    private fun colorWayUpdate(tmcList: MutableList<TMC>?) {
        if (mAMap != null && tmcList != null && tmcList.isNotEmpty()) {
            polylineOptionsColor = null
            polylineOptionsColor = PolylineOptions().apply {
                val colorList: MutableList<Int> = mutableListOf()
                add(pointStart)
                add(tmcList[0].polyline[0].toLatLng)
                colorList.add(colorDrive)
                for (tmc in tmcList) {
                    val polyline = tmc.polyline
                    val color = getColor(tmc.status)
                    for (i in 1 until polyline.size) {
                        add(polyline[i].toLatLng)
                        colorList.add(color)
                    }
                }
                add(pointEnd)
                colorList.add(colorDrive)
                colorValues(colorList).width(routeWidth)
            }
        }
    }

    private fun getColor(status: String): Int = when (status) {
        "畅通" -> Color.GREEN
        "缓行" -> Color.YELLOW
        "拥堵" -> Color.RED
        "严重拥堵" -> Color.parseColor("#990033")
        else -> Color.parseColor("#537edc")
    }

    override val removeFromMap = {
        super.removeFromMap
        try {
            throughPointMarkers?.run {
                if (size > 0) {
                    for (marker in this) {
                        marker.remove()
                    }
                    clear()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    override val latLngBounds: LatLngBounds
        get() = LatLngBounds.builder().apply {
            include(LatLng(pointStart!!.latitude, pointStart!!.longitude))
            include(LatLng(pointEnd!!.latitude, pointEnd!!.longitude))
            points?.run {
                if (size > 0) for (point in points) {
                    include(point.toLatLng)
                }
            }
        }.build()

    companion object {
        fun getPointForDis(start: LatLng, end: LatLng, dis: Double): LatLng =
            (dis / calculateDistance(start, end)).let {
                LatLng(
                    (end.latitude - start.latitude) * it + start.latitude,
                    (end.longitude - start.longitude) * it + start.longitude
                )
            }

        fun calculateDistance(start: LatLng, end: LatLng): Int =
            calculateDistance(start.longitude, start.latitude, end.longitude, end.latitude)

        fun calculateDistance(x1: Double, y1: Double, x2: Double, y2: Double): Int {
            val piNF = 0.01745329251994329
            val x1pi = x1 * piNF
            val y1pi = y1 * piNF
            val x2pi = x2 * piNF
            val y2pi = y2 * piNF
            val y1cos = cos(y1pi)
            val y2cos = cos(y2pi)
            DoubleArray(3).apply {
                this[0] = y1cos * cos(x1pi) - y2cos * cos(x2pi)
                this[1] = y1cos * sin(x1pi) - y2cos * sin(x2pi)
                this[2] = sin(y1pi) - sin(y2pi)
            }.let {
                return (asin(sqrt(it[0].pow(2) + it[1].pow(2) + it[2].pow(2)) / 2) * 12742001.5798544).toInt()
            }
        }
    }
}