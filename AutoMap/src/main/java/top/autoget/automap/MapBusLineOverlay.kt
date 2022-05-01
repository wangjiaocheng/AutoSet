package top.autoget.automap

import android.content.Context
import android.graphics.Color
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.*
import com.amap.api.services.busline.BusLineItem
import com.amap.api.services.busline.BusStationItem
import com.amap.api.services.core.LatLonPoint
import top.autoget.automap.MapCommon.toLatLng

class MapBusLineOverlay(
    private val context: Context, private val aMap: AMap?, private val busLineItem: BusLineItem
) {
    private var mBusLinePolyline: Polyline? = null
    private val busColor: Int
        get() = Color.parseColor("#537edc")
    private val busLineWidth: Float
        get() = 18f
    private val mBusStations: MutableList<BusStationItem> = busLineItem.busStations
    private val mBusStationMarks: MutableList<Marker> = mutableListOf()
    val addToMap = try {
        aMap?.run {
            mBusLinePolyline = addPolyline(
                PolylineOptions().color(busColor).width(busLineWidth)
                    .addAll(
                        busLineItem.directionsCoordinates
                            .map { LatLng(it.latitude, it.longitude) }.toMutableList()
                    )
            )
            when {
                mBusStations.size >= 1 -> mBusStationMarks.apply {
                    add(addMarker(getMarkerOptions(0)))
                    for (i in 1 until mBusStations.size - 1) {
                        add(addMarker(getMarkerOptions(i)))
                    }
                    add(addMarker(getMarkerOptions(mBusStations.size - 1)))
                }
                else -> Unit
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }
    private var startBitmapDescriptor: BitmapDescriptor? =
        BitmapDescriptorFactory.fromResource(R.mipmap.amap_start)
    private var endBitmapDescriptor: BitmapDescriptor? =
        BitmapDescriptorFactory.fromResource(R.mipmap.amap_end)
    private var busBitmapDescriptor: BitmapDescriptor? =
        BitmapDescriptorFactory.fromResource(R.mipmap.amap_bus)

    private fun getMarkerOptions(index: Int): MarkerOptions =
        MarkerOptions().title(getTitle(index)).snippet(getSnippet(index))
            .position(mBusStations[index].latLonPoint.toLatLng).apply {
                when (index) {
                    0 -> icon(startBitmapDescriptor)
                    mBusStations.size - 1 -> icon(endBitmapDescriptor)
                    else -> icon(busBitmapDescriptor).anchor(0.5f, 0.5f)
                }
            }

    private fun getTitle(index: Int): String = mBusStations[index].busStationName
    private fun getSnippet(index: Int): String = ""
    private val destroyBit = {
        startBitmapDescriptor?.run {
            recycle()
            startBitmapDescriptor = null
        }
        endBitmapDescriptor?.run {
            recycle()
            endBitmapDescriptor = null
        }
        busBitmapDescriptor?.run {
            recycle()
            busBitmapDescriptor = null
        }
    }
    val removeFromMap = {
        mBusLinePolyline?.remove()
        try {
            for (mark in mBusStationMarks) {
                mark.remove()
            }
            destroyBit
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    val zoomToSpan = try {
        busLineItem.directionsCoordinates?.let {
            if (it.size > 0)
                aMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(getLatLngBounds(it), 5))
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    private fun getLatLngBounds(points: MutableList<LatLonPoint>): LatLngBounds =
        LatLngBounds.builder().apply {
            for (point in points) {
                include(point.toLatLng)
            }
        }.build()

    fun getBusStationIndex(marker: Marker): Int {
        for ((index, value) in mBusStationMarks.withIndex()) {
            if (value == marker) return index
        }
        return -1
    }

    fun getBusStationItem(index: Int): BusStationItem? =
        if (index < 0 || index >= mBusStations.size) null else mBusStations[index]
}