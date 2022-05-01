package top.autoget.automap

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.services.route.DrivePath
import com.amap.api.services.route.DriveRouteResult
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.info
import top.autoget.automap.MapCommon.getFriendlyLength
import top.autoget.automap.MapCommon.getFriendlyTime
import top.autoget.automap.databinding.ActivityRouteDetailBinding

class MapRouteDriveDetailActivity : AppCompatActivity(), LoggerKit {
    private var drivePath: DrivePath? = intent.getParcelableExtra("drive_path")
    private var driveRouteResult: DriveRouteResult? = intent.getParcelableExtra("drive_result")
    private val activityRouteDetailBinding: ActivityRouteDetailBinding =
        ActivityRouteDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityRouteDetailBinding.root)
        drivePath?.run {
            for (driveStep in steps) {
                for (tmc in driveStep.tmCs) {
                    info(tmc.run { "$loggerTag->${polyline.size}${status}${distance}${polyline}" })
                }
            }
        }
        activityRouteDetailBinding.titleCenter.text = "驾车路线详情"
        activityRouteDetailBinding.firstline.text =
            "${getFriendlyTime(drivePath?.duration?.toInt() ?: 0)}(${getFriendlyLength(drivePath?.distance?.toInt() ?: 0)})"
        activityRouteDetailBinding.secondline.apply {
            text = "打车约${driveRouteResult?.taxiCost}元"
            visibility = View.VISIBLE
        }
        activityRouteDetailBinding.busSegmentList.adapter =
            drivePath?.steps?.let { MapRouteDriveSegmentListAdapter(applicationContext, it) }
    }

    fun onBackClick(view: View?) = finish()
}