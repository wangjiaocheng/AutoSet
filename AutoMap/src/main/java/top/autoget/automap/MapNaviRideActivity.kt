package top.autoget.automap

import android.os.Bundle
import top.autoget.autokit.ToastKit.showShort

class MapNaviRideActivity : MapNaviActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navi_basic)
        aMapNaviView = findViewById(R.id.navi_view)
        aMapNaviView?.onCreate(savedInstanceState)
        aMapNaviView?.setAMapNaviViewListener(this)
    }

    val calculateRideRoute =
        aMapNavi?.calculateWalkRoute(startPoints[0], endPoints[0]) ?: showShort("路线计算失败,检查参数情况")

    override fun onInitNaviSuccess() {
        calculateRideRoute
    }

    override fun onGpsSignalWeak(boolean: Boolean) {}
}