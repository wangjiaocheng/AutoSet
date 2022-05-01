package top.autoget.automap

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.services.route.WalkPath
import top.autoget.automap.MapCommon.getFriendlyLength
import top.autoget.automap.MapCommon.getFriendlyTime
import top.autoget.automap.databinding.ActivityRouteDetailBinding

class MapRouteWalkDetailActivity : AppCompatActivity() {
    private var walkPath: WalkPath? = intent.getParcelableExtra("walk_path")
    private val activityRouteDetailBinding: ActivityRouteDetailBinding =
        ActivityRouteDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityRouteDetailBinding.root)
        activityRouteDetailBinding.titleCenter.text = "步行路线详情"
        activityRouteDetailBinding.firstline.text =
            "${getFriendlyTime(walkPath?.duration?.toInt() ?: 0)}(${getFriendlyLength(walkPath?.distance?.toInt() ?: 0)})"
        activityRouteDetailBinding.busSegmentList.adapter =
            walkPath?.steps?.let { MapRouteWalkSegmentListAdapter(applicationContext, it) }
    }

    fun onBackClick(view: View?) = finish()
}