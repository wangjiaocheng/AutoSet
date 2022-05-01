package top.autoget.automap

import android.content.Context
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.RidePath
import com.amap.api.services.route.RideStep
import top.autoget.automap.MapCommon.toLatLng
import top.autoget.automap.MapCommon.toLatLngList

class MapRouteRideOverlay(
    context: Context, aMap: AMap?, path: RidePath, start: LatLonPoint, end: LatLonPoint
) : MapRouteOverlay(context) {
    init {
        mAMap = aMap
        pointStart = start.toLatLng
        pointEnd = end.toLatLng
    }

    private var polylineOptions: PolylineOptions? = null
    private val initPolylineOptions = {
        polylineOptions = null
        polylineOptions = PolylineOptions().color(colorDrive).width(routeWidth)
    }
    private val ridePath: RidePath = path
    val addToMap = {
        initPolylineOptions
        try {
            addStartAndEndMarker
            polylineOptions?.apply {
                add(pointStart)
                for (rideStep in ridePath.steps) {
                    addStationMarkersRide(rideStep, rideStep.polyline[0].toLatLng)
                    addAll(rideStep.polyline.toLatLngList)
                }
                add(pointEnd)
            }.let { addPolyLine(it) }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun addStationMarkersRide(rideStep: RideStep, position: LatLng) = rideStep.run {
        MarkerOptions().anchor(0.5f, 0.5f).visible(nodeIconVisible).icon(bdRide).position(position)
            .title("\u65B9\u5411:${action}\n\u9053\u8DEF:${road}").snippet(instruction)
            .let { addStationMarker(it) }
    }
}