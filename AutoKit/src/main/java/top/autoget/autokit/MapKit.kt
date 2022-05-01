package top.autoget.autokit

import android.content.Context
import android.content.Intent
import android.net.Uri
import top.autoget.autokit.AKit.app
import top.autoget.autokit.CoordinateKit.gcj02ToBd09
import top.autoget.autokit.CoordinateKit.wgs84ToGcj02
import top.autoget.autokit.PackageKit.isExistPackageName

object MapKit {
    @JvmOverloads
    fun openMap(
        from: DoubleArray, to: DoubleArray, storeName: String, context: Context = app
    ) = when {
        isExistPackageName(context, "com.autonavi.minimap") ->
            openGaodeMapToGuide(from, to, storeName, context)
        isExistPackageName(context, "com.baidu.BaiduMap") ->
            openBaiduMapToGuide(to, storeName, context)
        else -> openBrowserToGuide(to, storeName, context)
    }

    @JvmOverloads
    fun openGaodeMapToGuide(
        from: DoubleArray, to: DoubleArray, storeName: String, context: Context = app
    ) = wgs84ToGcj02(from[0], from[1]).let { from0 ->
        wgs84ToGcj02(to[0], to[1]).let { to0 ->
            context.startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                addCategory(Intent.CATEGORY_DEFAULT)
                data =
                    Uri.parse("androidamap://route?sourceApplication=amap&slat=${from0.second}&slon=${from0.first}&dlat=${to0.second}&dlon=${to0.first}&dname=$storeName&dev=0&t=0")
            })
        }
    }

    @JvmOverloads
    fun openBaiduMapToGuide(from: DoubleArray, storeName: String, context: Context = app) =
        wgs84ToGcj02(from[0], from[1]).let { from0 ->
            gcj02ToBd09(from0.first, from0.second).let { to ->
                context.startActivity(Intent().apply {
                    data =
                        Uri.parse("baidumap://map/direction?destination=name:$storeName|latlng:${to.second},${to.first}&mode=driving&sy=3&index=0&target=1")
                })
            }
        }

    @JvmOverloads
    fun openBrowserToGuide(from: DoubleArray, storeName: String, context: Context = app) =
        wgs84ToGcj02(from[0], from[1]).let {
            context.startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                data =
                    Uri.parse("http://uri.amap.com/navigation?to=${it.second},${it.first},$storeName&mode=car&policy=1&src=mypage&coordinate=gaode&callnative=0")
            })
        }

    @JvmOverloads
    fun metreToScreenPixel(
        distance: Double, currScale: Double, context: Context = app
    ): Double =
        distance / (25.39999918 / currScale * context.resources.displayMetrics.densityDpi / 1000)

    @JvmOverloads
    fun screenPixelToMetre(
        pxLength: Double, currScale: Double, context: Context = app
    ): Double =
        pxLength * (25.39999918 / currScale * context.resources.displayMetrics.densityDpi / 1000)
}