package top.autoget.automap

import com.amap.api.maps.model.LatLng
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.BusPath
import java.text.DecimalFormat

object MapCommon {
    val LatLonPoint.toLatLng: LatLng
        get() = run { LatLng(latitude, longitude) }
    val MutableList<LatLonPoint>.toLatLngList: MutableList<LatLng>
        get() = let {
            mutableListOf<LatLng>().apply {
                for (point in it) {
                    add(point.toLatLng)
                }
            }
        }
    val LatLng.toLatLonPoint: LatLonPoint
        get() = run { LatLonPoint(latitude, longitude) }
    val MutableList<LatLng>.toLatLngPointList: MutableList<LatLonPoint>
        get() = let {
            mutableListOf<LatLonPoint>().apply {
                for (point in it) {
                    add(point.toLatLonPoint)
                }
            }
        }

    fun getDriveActionID(actionName: String?): Int = when (actionName) {
        null, "" -> R.mipmap.dir3
        "右转" -> R.mipmap.dir1
        "左转" -> R.mipmap.dir2
        "直行" -> R.mipmap.dir3
        "减速行驶" -> R.mipmap.dir4
        "向右前方行驶", "靠右" -> R.mipmap.dir5
        "向左前方行驶", "靠左" -> R.mipmap.dir6
        "向右后方行驶" -> R.mipmap.dir7
        "向左后方行驶", "左转调头" -> R.mipmap.dir8
        else -> R.mipmap.dir3
    }

    fun getWalkActionID(actionName: String?): Int = when {
        actionName == null || actionName == "" -> R.mipmap.dir13
        actionName == "右转" -> R.mipmap.dir1
        actionName == "左转" -> R.mipmap.dir2
        actionName == "直行" -> R.mipmap.dir3
        actionName == "向右前方" || actionName == "靠右" || actionName.contains("向右前方") -> R.mipmap.dir5
        actionName == "向左前方" || actionName == "靠左" || actionName.contains("向左前方") -> R.mipmap.dir6
        actionName == "向右后方" || actionName.contains("向右后方") -> R.mipmap.dir7
        actionName == "向左后方" || actionName.contains("向左后方") -> R.mipmap.dir8
        actionName == "通过人行横道" -> R.mipmap.dir9
        actionName == "通过地下通道" -> R.mipmap.dir10
        actionName == "通过过街天桥" -> R.mipmap.dir11
        else -> R.mipmap.dir13
    }

    fun getBusPathTitle(busPath: BusPath?): String = busPath?.steps?.let { stepList ->
        StringBuffer().apply {
            for (busStep in stepList) {
                if (busStep.busLines.size > 0) StringBuffer().apply {
                    for (busLine in busStep.busLines) {
                        busLine?.let { line -> append("${getSimpleBusLineName(line.busLineName)} / ") }
                    }
                }.let { title -> append("${title.substring(0, title.length - 3)} > ") }
                busStep.railway?.let { item ->
                    append("${item.trip}(${item.departurestop.name} - ${item.arrivalstop.name}) > ")
                }
            }
        }.run { substring(0, length - 3) }
    } ?: ""

    fun getSimpleBusLineName(busLineName: String?): String =
        busLineName?.replace("\\(.*?\\)".toRegex(), "") ?: ""

    fun getBusPathDes(busPath: BusPath?): String = busPath?.run {
        "${getFriendlyTime(duration.toInt())} | ${getFriendlyLength(distance.toInt())} | 步行${
            getFriendlyLength(walkDistance.toInt())
        }"
    } ?: ""

    fun getFriendlyTime(second: Int): String = when {
        second > 3600 -> "${second / 3600}小时${second % 3600 / 60}分钟"
        second >= 60 -> "${second / 60}分钟"
        else -> "${second}秒"
    }

    fun getFriendlyLength(lenMeter: Int): String = when {
        lenMeter > 10000 -> "${lenMeter / 1000}公里"
        lenMeter > 1000 -> "${DecimalFormat("##0.0").format(lenMeter / 1000.0)}公里"
        lenMeter > 100 -> "${lenMeter / 50 * 50}米"
        else -> "${(lenMeter / 10 * 10).let { if (it == 0) 10 else it }}米"
    }
}