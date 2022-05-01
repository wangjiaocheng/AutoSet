package top.autoget.automap

import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.core.PoiItem
import top.autoget.automap.MapCommon.toLatLng

class MapPoiOverlay(private val aMap: AMap?, private val poiList: MutableList<PoiItem>) {
    private val poiMarkers: MutableList<Marker> = mutableListOf()
    val addToMap = try {
        for (i in poiList.indices) {
            aMap?.addMarker(getMarkerOptions(i))?.apply { setObject(poiList[i]) }
                ?.let { poiMarkers.add(it) }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    private fun getMarkerOptions(index: Int): MarkerOptions = poiList[index].run {
        MarkerOptions().position(latLonPoint.toLatLng)
            .title(title).snippet(snippet).icon(null)
    }

    val removeFromMap = {
        for (mark in poiMarkers) {
            mark.remove()
        }
    }
    private val latLngBounds: LatLngBounds
        get() = LatLngBounds.builder().apply {
            for (poi in poiList) {
                include(poi.latLonPoint.toLatLng)
            }
        }.build()
    val zoomToSpan = {
        try {
            if (poiList.isNotEmpty()) aMap?.run {
                when (poiList.size) {
                    1 -> moveCamera(CameraUpdateFactory.newLatLngZoom(poiList[0].latLonPoint.run {
                        LatLng(latitude, longitude)
                    }, 18f))
                    else -> moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 5))
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun getPoiItem(index: Int): PoiItem? = if (index in poiList.indices) poiList[index] else null
    fun getPoiIndex(marker: Marker): Int {
        for ((index, value) in poiMarkers.withIndex()) {
            if (value == marker) return index
        }
        return -1
    }
}