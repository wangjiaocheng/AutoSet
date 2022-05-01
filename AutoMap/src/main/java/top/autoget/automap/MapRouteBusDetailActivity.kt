package top.autoget.automap

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.AMap
import com.amap.api.services.route.BusPath
import com.amap.api.services.route.BusRouteResult
import top.autoget.automap.MapCommon.getFriendlyLength
import top.autoget.automap.MapCommon.getFriendlyTime
import top.autoget.automap.databinding.ActivityRouteDetailBinding

class MapRouteBusDetailActivity : AppCompatActivity(), AMap.OnMapLoadedListener {
    private var aMap: AMap? = null
    private var buspath: BusPath? = intent.getParcelableExtra("bus_path")
    private var busRouteResult: BusRouteResult? = intent.getParcelableExtra("bus_result")
    private val activityRouteDetailBinding: ActivityRouteDetailBinding =
        ActivityRouteDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_detail)
        activityRouteDetailBinding.routeMap.onCreate(savedInstanceState)
        if (aMap == null) aMap = activityRouteDetailBinding.routeMap.map
        aMap?.setOnMapLoadedListener(this)
        activityRouteDetailBinding.titleCenter.text = "公交路线详情"
        activityRouteDetailBinding.firstline.text =
            "${getFriendlyTime(buspath?.duration?.toInt() ?: 0)}(${getFriendlyLength(buspath?.distance?.toInt() ?: 0)})"
        activityRouteDetailBinding.secondline.apply {
            text = "打车约${busRouteResult?.taxiCost}元"
            visibility = View.VISIBLE
        }
        activityRouteDetailBinding.titleMap.visibility = View.VISIBLE
        activityRouteDetailBinding.busSegmentList.adapter =
            buspath?.steps?.let { MapRouteBusSegmentListAdapter(applicationContext, it) }
    }

    private var routeBusOverlay: MapRouteBusOverlay? = null
    override fun onMapLoaded() {
        routeBusOverlay?.run {
            addToMap
            zoomToSpan
        }
    }

    fun onBackClick(view: View?) = finish()
    fun onMapClick(view: View?) {
        activityRouteDetailBinding.titleMap.visibility = View.GONE
        activityRouteDetailBinding.busPath.visibility = View.GONE
        activityRouteDetailBinding.routeMap.visibility = View.VISIBLE
        aMap?.clear()
        routeBusOverlay = aMap?.let {
            buspath?.let { it1 ->
                busRouteResult?.startPos?.let { it2 ->
                    busRouteResult?.targetPos?.let { it3 ->
                        MapRouteBusOverlay(this, it, it1, it2, it3)
                    }
                }
            }
        }
        routeBusOverlay?.removeFromMap
    }
}