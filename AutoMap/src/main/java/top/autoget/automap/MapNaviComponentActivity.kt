package top.autoget.automap

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.amap.api.navi.*
import com.amap.api.navi.model.AMapNaviLocation
import top.autoget.automap.databinding.ActivityIndexBinding

class MapNaviComponentActivity : AppCompatActivity(), INaviInfoCallback {
    private val array: Array<String?> = arrayOf("起终点算路", "无起点算路", "途径点算路", "直接导航")
    var startStr: String = ""
    var endStr: String = ""
    var start: LatLng? = null
    var end: LatLng? = null
    val way: MutableList<Poi> = mutableListOf()
    private val itemClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { _, _, position, _ ->
            AmapNaviPage.getInstance().run {
                when (position) {
                    0 -> showRouteActivity(
                        applicationContext, AmapNaviParams(
                            Poi(startStr, start, ""), null, Poi(endStr, end, ""),
                            AmapNaviType.DRIVER
                        ), this@MapNaviComponentActivity
                    )
                    1 -> showRouteActivity(
                        applicationContext, AmapNaviParams(
                            null, null, Poi(endStr, end, ""),
                            AmapNaviType.DRIVER
                        ), this@MapNaviComponentActivity
                    )
                    2 -> showRouteActivity(
                        applicationContext, AmapNaviParams(
                            Poi(startStr, start, ""), way, Poi(endStr, end, ""),
                            AmapNaviType.DRIVER
                        ), this@MapNaviComponentActivity
                    )
                    3 -> showRouteActivity(
                        applicationContext, AmapNaviParams(
                            Poi(startStr, start, ""), null, Poi(endStr, end, ""),
                            AmapNaviType.DRIVER, AmapPageType.NAVI
                        ), this@MapNaviComponentActivity
                    )
                }
            }
        }
    private val activityIndexBinding: ActivityIndexBinding =
        ActivityIndexBinding.inflate(layoutInflater)
    private val initView = activityIndexBinding.list.apply {
        adapter =
            ArrayAdapter(this@MapNaviComponentActivity, android.R.layout.simple_list_item_1, array)
        title = "导航SDK ${AMapNavi.getVersion()}"
        onItemClickListener = itemClickListener
    }

    private var ttsControllerAMap: TtsControllerAMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
        initView
        ttsControllerAMap = TtsControllerAMap.getInstance(applicationContext)
        ttsControllerAMap?.init()
    }

    public override fun onDestroy() {
        super.onDestroy()
        ttsControllerAMap?.destroy()
    }

    override fun onInitNaviFailure() {}
    override fun onStrategyChanged(p0: Int) {}
    override fun onScaleAutoChanged(p0: Boolean) {}
    override fun onGetNavigationText(s: String?) = ttsControllerAMap?.onGetNavigationText(s) ?: Unit
    override fun onLocationChange(aMapNaviLocation: AMapNaviLocation?) {}
    override fun getCustomNaviBottomView(): View? = null
    override fun onArrivedWayPoint(p0: Int) {}
    override fun onArriveDestination(b: Boolean) {}
    override fun onStartNavi(i: Int) {}
    override fun onCalculateRouteSuccess(ints: IntArray?) {}
    override fun onCalculateRouteFailure(i: Int) {}
    override fun getCustomMiddleView(): View? = null
    override fun onMapTypeChanged(p0: Int) {}
    override fun onStopSpeaking(): Unit = ttsControllerAMap?.stopSpeaking() ?: Unit
    override fun onReCalculateRoute(var1: Int) {}
    override fun getCustomNaviView(): View? = null
    override fun onDayAndNightModeChanged(p0: Int) {}
    override fun onExitPage(var1: Int) {}
    override fun onNaviDirectionChanged(p0: Int) {}
    override fun onBroadcastModeChanged(p0: Int) {}
}