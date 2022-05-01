package top.autoget.automap

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.services.route.RidePath
import top.autoget.automap.MapCommon.getFriendlyLength
import top.autoget.automap.MapCommon.getFriendlyTime
import top.autoget.automap.databinding.ActivityRouteDetailBinding

class MapRouteRideDetailActivity : AppCompatActivity() {
    private var ridePath: RidePath? = intent.getParcelableExtra("ride_path")
    private val activityRouteDetailBinding: ActivityRouteDetailBinding =
        ActivityRouteDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityRouteDetailBinding.root)
        activityRouteDetailBinding.titleCenter.text = "骑行路线详情"
        activityRouteDetailBinding.firstline.text =
            "${getFriendlyTime(ridePath?.duration?.toInt() ?: 0)}(${getFriendlyLength(ridePath?.distance?.toInt() ?: 0)})"
        activityRouteDetailBinding.busSegmentList.adapter =
            ridePath?.steps?.let { MapRouteRideSegmentListAdapter(applicationContext, it) }
    }

    fun onBackClick(view: View?) = finish()
}