package top.autoget.automap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*

open class MapRouteOverlay(private val context: Context) {
    protected var mAMap: AMap? = null
    protected var pointStart: LatLng? = null
    protected var pointEnd: LatLng? = null
    protected open val latLngBounds: LatLngBounds
        get() = LatLngBounds.builder().apply {
            include(pointStart)
            include(pointEnd)
        }.build()
    val zoomToSpan = pointStart?.let {
        try {
            mAMap?.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50))
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    private var polyLines: MutableList<Polyline> = mutableListOf()
    private var stationMarkers: MutableList<Marker> = mutableListOf()
    private var bitStart: Bitmap? = null
    private var bitEnd: Bitmap? = null
    private var bitBus: Bitmap? = null
    private var bitWalk: Bitmap? = null
    private var bitDrive: Bitmap? = null
    private val destroyBit = {
        bitStart?.run {
            recycle()
            bitStart = null
        }
        bitEnd?.run {
            recycle()
            bitEnd = null
        }
        bitBus?.run {
            recycle()
            bitBus = null
        }
        bitWalk?.run {
            recycle()
            bitWalk = null
        }
        bitDrive?.run {
            recycle()
            bitDrive = null
        }
    }
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    open val removeFromMap = {
        for (polyline in polyLines) {
            polyline.remove()
        }
        for (marker in stationMarkers) {
            marker.remove()
        }
        destroyBit
        startMarker?.remove()
        endMarker?.remove()
    }
    protected var nodeIconVisible = true
        set(visible) = try {
            field = visible
            if (stationMarkers.size > 0) for (marker in stationMarkers) {
                marker.isVisible = visible
            } else Unit
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    protected open val routeZoom: Int
        get() = 15
    protected open val routeWidth: Float
        get() = 18f
    protected val colorBus: Int
        get() = Color.parseColor("#537edc")
    protected val colorWalk: Int
        get() = Color.parseColor("#6db74d")
    protected val colorDrive: Int
        get() = Color.parseColor("#537edc")
    protected val bdBus: BitmapDescriptor
        get() = BitmapDescriptorFactory.fromResource(R.mipmap.amap_bus)
    protected val bdDrive: BitmapDescriptor
        get() = BitmapDescriptorFactory.fromResource(R.mipmap.amap_car)
    protected val bdThrough: BitmapDescriptor
        get() = BitmapDescriptorFactory.fromResource(R.mipmap.amap_through)
    protected val bdRide: BitmapDescriptor
        get() = BitmapDescriptorFactory.fromResource(R.mipmap.amap_ride)
    protected val bdWalk: BitmapDescriptor
        get() = BitmapDescriptorFactory.fromResource(R.mipmap.amap_man)
    private val bdStart: BitmapDescriptor
        get() = BitmapDescriptorFactory.fromResource(R.mipmap.amap_start)
    private val bdEnd: BitmapDescriptor
        get() = BitmapDescriptorFactory.fromResource(R.mipmap.amap_end)
    protected val addStartAndEndMarker = {
        startMarker?.run {
            remove()
            startMarker = null
        }
        endMarker?.run {
            remove()
            endMarker = null
        }
        startMarker = mAMap?.addMarker(
            MarkerOptions().position(pointStart).icon(bdStart).title("\u8D77\u70B9")
        )
        endMarker =
            mAMap?.addMarker(MarkerOptions().position(pointEnd).icon(bdEnd).title("\u7EC8\u70B9"))
    }

    protected fun addStationMarker(options: MarkerOptions?) =
        options?.let { mAMap?.addMarker(it)?.let { marker -> stationMarkers.add(marker) } }

    protected fun addPolyLine(options: PolylineOptions?) =
        options?.let { mAMap?.addPolyline(it)?.let { polyline -> polyLines.add(polyline) } }
}