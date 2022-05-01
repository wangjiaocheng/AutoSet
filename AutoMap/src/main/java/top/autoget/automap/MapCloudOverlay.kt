package top.autoget.automap

import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.cloud.CloudItem
import top.autoget.automap.MapCommon.toLatLng

class MapCloudOverlay(aMap: AMap?, cloudItems: MutableList<CloudItem>) {
    private val mAMap: AMap? = aMap
    private val items: MutableList<CloudItem> = cloudItems
    private val marks: MutableList<Marker> = mutableListOf()
    val addToMap = mAMap?.run {
        for (i in items.indices) {
            addMarker(getMarkerOptions(i)).apply { setObject(i) }.let { marks.add(it) }
        }
    }

    private fun getMarkerOptions(index: Int): MarkerOptions = MarkerOptions()
        .title(getTitle(index)).snippet(getSnippet(index)).icon(getBitmapDescriptor(index))
        .position(items[index].latLonPoint.toLatLng)

    private fun getTitle(index: Int): String = items[index].title
    private fun getSnippet(index: Int): String = items[index].snippet
    private fun getBitmapDescriptor(index: Int): BitmapDescriptor? = null
    val removeFromMap = {
        for (mark in marks) {
            mark.remove()
        }
    }
    val zoomToSpan = {
        if (items.isNotEmpty())
            mAMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 20))
    }
    private val latLngBounds: LatLngBounds
        get() = LatLngBounds.builder().apply {
            for (i in items.indices) {
                include(items[i].latLonPoint.toLatLng)
            }
        }.build()

    fun getPoiIndex(marker: Marker?): Int {
        for ((index, value) in marks.withIndex()) {
            if (value == marker) return index
        }
        return -1
    }

    fun getPoiItem(index: Int): CloudItem? =
        if (index < 0 || index >= items.size) null else items[index]
}