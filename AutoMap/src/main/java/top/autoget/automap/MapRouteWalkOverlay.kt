package top.autoget.automap

import android.content.Context
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.WalkPath
import com.amap.api.services.route.WalkStep
import top.autoget.automap.MapCommon.toLatLng
import top.autoget.automap.MapCommon.toLatLngList

class MapRouteWalkOverlay(
    context: Context, aMap: AMap?, path: WalkPath, start: LatLonPoint, end: LatLonPoint
) : MapRouteOverlay(context) {
    init {
        mAMap = aMap
        pointStart = start.toLatLng
        pointEnd = end.toLatLng
    }

    private var polylineOptions: PolylineOptions? = null
    private val initPolylineOptions = {
        polylineOptions = null
        polylineOptions = PolylineOptions().color(colorWalk).width(routeWidth)
    }
    private val walkPath: WalkPath = path
    val addToMap = {
        initPolylineOptions
        try {
            addStartAndEndMarker
            polylineOptions?.apply {
                add(pointStart)
                for (walkStep in walkPath.steps) {
                    addStationMarkersWalk(walkStep, walkStep.polyline[0].toLatLng)
                    addAll(walkStep.polyline.toLatLngList)
                }
                add(pointEnd)
            }.let { addPolyLine(it) }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun addStationMarkersWalk(walkStep: WalkStep, position: LatLng?) = walkStep.run {
        MarkerOptions().anchor(0.5f, 0.5f).visible(nodeIconVisible).icon(bdWalk).position(position)
            .title("\u65B9\u5411:${action}\n\u9053\u8DEF:${road}").snippet(instruction)
            .let { addStationMarker(it) }
    }

    private fun checkDistanceToNextStep(walkStep: WalkStep, walkStep1: WalkStep) {
        val nextFirstPoint = walkStep1.polyline[0]
        val lastPoint = walkStep.polyline[walkStep.polyline.size - 1]
        if (lastPoint != nextFirstPoint) addPolyLineWalk(lastPoint, nextFirstPoint)
    }

    private fun addPolyLineWalk(pointFrom: LatLonPoint, pointTo: LatLonPoint) =
        polylineOptions?.add(pointFrom.toLatLng, pointTo.toLatLng)
}