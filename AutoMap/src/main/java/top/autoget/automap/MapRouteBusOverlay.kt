package top.autoget.automap

import android.content.Context
import com.amap.api.maps.AMap
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.*
import top.autoget.automap.MapCommon.toLatLng
import top.autoget.automap.MapCommon.toLatLngList

class MapRouteBusOverlay(
    context: Context, aMap: AMap?, path: BusPath, start: LatLonPoint, end: LatLonPoint
) : MapRouteOverlay(context) {
    init {
        mAMap = aMap
        pointStart = start.toLatLng
        pointEnd = end.toLatLng
    }

    private var latLng: LatLng? = null
    val addToMap = try {
        path.steps.run {
            for ((index, busStep) in withIndex()) {
                if (index < size - 1) this[index + 1].let { busStep1 ->
                    if (busStep.walk != null && busStep.busLines[0] != null)
                        checkWalkToBusLine(busStep)
                    if (busStep.busLines[0] != null && busStep1.walk != null && busStep1.walk.steps.size > 0)
                        checkBusLineToNextWalk(busStep, busStep1)
                    if (busStep.busLines[0] != null && busStep1.busLines[0] != null && busStep1.walk == null)
                        checkBusEndToNextBusStart(busStep, busStep1)
                    if (busStep.busLines[0] != null && busStep1.busLines[0] != null && busStep1.walk == null)
                        checkBusToNextBusNoWalk(busStep, busStep1)
                    if (busStep.busLines[0] != null && busStep1.railway != null)
                        checkBusLineToNextRailway(busStep, busStep1)
                    if (busStep.railway != null && busStep1.walk != null && busStep1.walk.steps.size > 0)
                        checkRailwayToNextWalk(busStep, busStep1)
                    if (busStep.railway != null && busStep1.railway != null)
                        checkRailwayToNextRailway(busStep, busStep1)
                    if (busStep.railway != null && busStep1.taxi != null)
                        checkRailwayToNextTaxi(busStep, busStep1)
                }
                when {
                    busStep.walk != null && busStep.walk.steps.size > 0 -> addStepsWalk(busStep)
                    busStep.busLines[0] == null && busStep.railway == null && busStep.taxi == null ->
                        addPolylineWalk(latLng, pointEnd)
                }
                busStep.busLines[0]?.let {
                    addStepsBusLine(it)
                    addStationMarkersBus(it)
                    if (index == size - 1)
                        addPolylineWalk(getBusLinePointLast(busStep).toLatLng, pointEnd)
                }
                busStep.railway?.let {
                    addStepRailway(it)
                    addMarkersRailway(it)
                    if (index == size - 1)
                        addPolylineWalk(it.arrivalstop.location.toLatLng, pointEnd)
                }
                busStep.taxi?.let {
                    addStepTaxi(it)
                    addMarkersTaxi(it)
                }
            }
        }
        addStartAndEndMarker
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    private fun checkBusEndToNextBusStart(busStep: BusStep, busStep1: BusStep) {
        val busEndLatLng = getBusLinePointLast(busStep).toLatLng
        val busStartLatLng = getBusLinePointFirst(busStep1).toLatLng
        if (busEndLatLng != busStartLatLng) drawLineArrow(busEndLatLng, busStartLatLng)
    }

    private fun checkBusToNextBusNoWalk(busStep: BusStep, busStep1: BusStep) {
        val busEndLatLng = getBusLinePointLast(busStep).toLatLng
        val busStartLatLng = getBusLinePointFirst(busStep1).toLatLng
        if (busStartLatLng.latitude - busEndLatLng.latitude > 0.0001
            || busStartLatLng.longitude - busEndLatLng.longitude > 0.0001
        ) drawLineArrow(busEndLatLng, busStartLatLng)
    }

    private fun checkBusLineToNextRailway(busStep: BusStep, busStep1: BusStep) {
        val busLastPoint = getBusLinePointLast(busStep)
        val railwayFirstPoint = busStep1.railway.departurestop.location
        if (busLastPoint != railwayFirstPoint) addPolyLineWalk(busLastPoint, railwayFirstPoint)
    }

    private fun checkBusLineToNextWalk(busStep: BusStep, busStep1: BusStep) {
        val busLastPoint = getBusLinePointLast(busStep)
        val walkFirstPoint = getWalkPointFirst(busStep1)
        if (busLastPoint != walkFirstPoint) addPolyLineWalk(busLastPoint, walkFirstPoint)
    }

    private fun checkRailwayToNextRailway(busStep: BusStep, busStep1: BusStep) {
        val railwayLastPoint = busStep.railway.arrivalstop.location
        val railwayFirstPoint = busStep1.railway.departurestop.location
        if (railwayLastPoint != railwayFirstPoint)
            addPolyLineWalk(railwayLastPoint, railwayFirstPoint)
    }

    private fun checkRailwayToNextWalk(busStep: BusStep, busStep1: BusStep) {
        val railwayLastPoint = busStep.railway.arrivalstop.location
        val walkFirstPoint = getWalkPointFirst(busStep1)
        if (railwayLastPoint != walkFirstPoint) addPolyLineWalk(railwayLastPoint, walkFirstPoint)
    }

    private fun checkRailwayToNextTaxi(busStep: BusStep, busStep1: BusStep) {
        val railwayLastPoint = busStep.railway.arrivalstop.location
        val taxiFirstPoint = busStep1.taxi.origin
        if (railwayLastPoint != taxiFirstPoint) addPolyLineWalk(railwayLastPoint, taxiFirstPoint)
    }

    private fun checkWalkToBusLine(busStep: BusStep) {
        val walkLastPoint = getWalkPointLast(busStep)
        val busLineFirstPoint = getBusLinePointFirst(busStep)
        if (walkLastPoint != busLineFirstPoint) addPolyLineWalk(walkLastPoint, busLineFirstPoint)
    }

    private fun addStepsBusLine(routeBusLineItem: RouteBusLineItem) =
        addStepsBusLine(routeBusLineItem.polyline)

    private fun addStepsBusLine(points: MutableList<LatLonPoint>) {
        if (points.isNotEmpty()) PolylineOptions().addAll(points.toLatLngList)
            .color(colorBus).width(routeWidth).let { addPolyLine(it) }
    }

    private fun addStepRailway(routeRailwayItem: RouteRailwayItem) =
        mutableListOf<RailwayStationItem>().apply {
            add(routeRailwayItem.departurestop)
            addAll(routeRailwayItem.viastops)
            add(routeRailwayItem.arrivalstop)
        }.let { railwayStationItems ->
            mutableListOf<LatLng>().apply {
                for (railwayStationItem in railwayStationItems) {
                    add(railwayStationItem.location.toLatLng)
                }
            }.let { addPolylineRailway(it) }
        }

    private fun addStepTaxi(taxi: TaxiItem) = taxi.run {
        PolylineOptions().add(origin.toLatLng).add(destination.toLatLng)
            .color(colorBus).width(routeWidth).let { addPolyLine(it) }
    }

    private fun addStepsWalk(busStep: BusStep) = busStep.walk.steps.run {
        for ((index, walkStep) in withIndex()) {
            if (index == 0) addStationMarkersWalk(
                walkStep.polyline[0].toLatLng,
                walkStep.road,
                getSnippetWalk(this)
            )
            walkStep.polyline.toLatLngList.let {
                latLng = it[it.size - 1]
                addPolylineWalk(it)
                if (index < size - 1) {
                    val lastLatLng = it[it.size - 1]
                    val firstLatLng = this[index + 1].polyline[0].toLatLng
                    if (lastLatLng != firstLatLng) addPolylineWalk(lastLatLng, firstLatLng)
                }
            }
        }
    }

    private fun addStationMarkersBus(routeBusLineItem: RouteBusLineItem) = MarkerOptions()
        .anchor(0.5f, 0.5f).visible(nodeIconVisible)
        .icon(bdBus).position(routeBusLineItem.departureBusStation.latLonPoint.toLatLng)
        .title(routeBusLineItem.busLineName).snippet(getSnippetBusLine(routeBusLineItem))
        .let { addStationMarker(it) }

    private fun addMarkersRailway(railway: RouteRailwayItem) {
        MarkerOptions().anchor(0.5f, 0.5f).visible(nodeIconVisible)
            .icon(bdBus).position(railway.departurestop.location.toLatLng)
            .title("${railway.departurestop.name}上车").snippet(railway.name)
            .let { addStationMarker(it) }
        MarkerOptions().anchor(0.5f, 0.5f).visible(nodeIconVisible)
            .icon(bdBus).position(railway.arrivalstop.location.toLatLng)
            .title("${railway.arrivalstop.name}下车").snippet(railway.name)
            .let { addStationMarker(it) }
    }

    private fun addMarkersTaxi(taxiItem: TaxiItem) = MarkerOptions()
        .anchor(0.5f, 0.5f).visible(nodeIconVisible)
        .icon(bdDrive).position(taxiItem.origin.toLatLng)
        .title("${taxiItem.getmSname()}打车").snippet("到终点").let { addStationMarker(it) }

    private fun addStationMarkersWalk(latLng: LatLng, title: String, snippet: String) =
        MarkerOptions()
            .anchor(0.5f, 0.5f).visible(nodeIconVisible).icon(bdWalk).position(latLng)
            .title(title).snippet(snippet).let { addStationMarker(it) }

    private fun addPolylineRailway(latLngList: MutableList<LatLng>) =
        addPolyLine(PolylineOptions().addAll(latLngList).color(colorDrive).width(routeWidth))

    private fun addPolyLineWalk(pointFrom: LatLonPoint, pointTo: LatLonPoint) =
        addPolylineWalk(pointFrom.toLatLng, pointTo.toLatLng)

    private fun addPolylineWalk(latLngFrom: LatLng?, latLngTo: LatLng?) = addPolyLine(
        PolylineOptions().add(latLngFrom, latLngTo).color(colorWalk).width(routeWidth)
            .setDottedLine(true)
    )

    private fun addPolylineWalk(latLngList: MutableList<LatLng>) = addPolyLine(
        PolylineOptions().addAll(latLngList).color(colorWalk).width(routeWidth).setDottedLine(true)
    )

    fun drawLineArrow(latLngFrom: LatLng?, latLngTo: LatLng?) =
        addPolyLine(PolylineOptions().add(latLngFrom, latLngTo).color(colorBus).width(routeWidth))

    private fun getSnippetBusLine(routeBusLineItem: RouteBusLineItem): String =
        routeBusLineItem.run { "(${departureBusStation.busStationName}-->${arrivalBusStation.busStationName}) \u7ECF\u8FC7${passStationNum + 1}\u7AD9" }

    private fun getSnippetWalk(walkSteps: List<WalkStep>): String {
        var disSum = 0f
        for (walkStep in walkSteps) {
            disSum += walkStep.distance
        }
        return "\u6B65\u884C$disSum\u7C73"
    }

    private fun getBusLinePointFirst(busStep: BusStep): LatLonPoint =
        busStep.busLines[0].polyline[0]

    private fun getBusLinePointLast(busStep: BusStep): LatLonPoint =
        busStep.busLines[0].polyline.run { this[size - 1] }

    private fun getWalkPointFirst(busStep1: BusStep): LatLonPoint =
        busStep1.walk.steps[0].polyline[0]

    private fun getWalkPointLast(busStep: BusStep): LatLonPoint =
        busStep.walk.steps.run { this[size - 1].polyline.run { this[size - 1] } }

    private fun getPointEntrance(busStep: BusStep): LatLonPoint = busStep.entrance.latLonPoint
    private fun getPointExit(busStep: BusStep): LatLonPoint = busStep.exit.latLonPoint
}