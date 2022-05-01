package top.autoget.automap

import android.opengl.Matrix
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CustomRenderer
import com.amap.api.maps.model.LatLng
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MapCubeRender(aMap: AMap?, latLng: LatLng) : CustomRenderer {
    private val position: LatLng = latLng

    init {
        aMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
    }

    override fun OnMapReferencechanged() {}//地图坐标系统刷新，需要重新计算坐标
    var surfaceWidth = 0f
    var surfaceHeight = 0f
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = width.toFloat()
        surfaceHeight = height.toFloat()
    }

    private var mapCube: MapCube? = null
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mapCube = MapCube(0.2f, 0.2f, 0.2f).apply { initShader }
    }

    var mvp = FloatArray(16)
    private val map: AMap? = aMap

    companion object {
        var SCALE = 0.005f
    }

    override fun onDrawFrame(gl: GL10?) {
        mapCube?.let {
            mvp.apply {
                Matrix.setIdentityM(mvp, 0)//恒等矩阵
                Matrix.multiplyMM(mvp, 0, map?.projectionMatrix, 0, map?.viewMatrix, 0)
                map?.projection?.toOpenGLLocation(position)
                    ?.let { pointF -> Matrix.translateM(mvp, 0, pointF.x, pointF.y, 0f) }
                Matrix.scaleM(mvp, 0, SCALE, SCALE, SCALE)
            }.run { it.drawES20(this) }
        }
    }
}