package top.autoget.autokit

import kotlin.math.*

object CoordinateKit {
    fun bd09ToWgs84(lng: Double, lat: Double): Pair<Double, Double> =
        bd09ToGcj02(lng, lat).run { gcj02ToWgs84(first, second) }

    private const val X_PI: Double = 3.14159265358979324 * 3000.0 / 180.0
    fun bd09ToGcj02(lng: Double, lat: Double): Pair<Double, Double> = (lng - 0.0065).let { x ->
        (lat - 0.006).let { y ->
            (sqrt(x.pow(2) + y.pow(2)) - 0.00002 * sin(y * X_PI)).let { z ->
                (atan2(y, x) - 0.000003 * cos(x * X_PI)).let { theta ->
                    Pair(z * cos(theta), z * sin(theta))
                }
            }
        }
    }

    private const val EE = 0.00669342162296594323//椭球偏心率
    private const val A = 6378245.0//Krasovsky1940(北京54)椭球长半轴
    fun gcj02ToWgs84(lng: Double, lat: Double): Pair<Double, Double> = when {
        outOfChina(lng, lat) -> Pair(lng, lat)
        else -> (lat / 180.0 * PI).let { radLat ->
            (1 - EE * sin(radLat).pow(2)).let { magic ->
                sqrt(magic).let { sqrtMagic ->
                    Pair(
                        lng + transformLng(lng - 105.0, lat - 35.0)
                                * 180.0 / (A / sqrtMagic * cos(radLat) * PI),
                        lat + transformLat(lng - 105.0, lat - 35.0)
                                * 180.0 / (A * (1 - EE) / (magic * sqrtMagic) * PI)
                    )
                }
            }
        }
    }

    fun outOfChina(lng: Double, lat: Double): Boolean =
        lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271

    fun transformLng(lng: Double, lat: Double): Double =
        300.0 + lng + 2.0 * lat + 0.1 * lng.pow(2) + 0.1 * lng * lat + 0.1 * sqrt(abs(lng)) +
                (20.0 * sin(6.0 * lng * PI) + 20.0 * sin(2.0 * lng * PI)) * 2.0 / 3.0 +
                (20.0 * sin(lng * PI) + 40.0 * sin(lng / 3.0 * PI)) * 2.0 / 3.0 +
                (150.0 * sin(lng / 12.0 * PI) + 300.0 * sin(lng / 30.0 * PI)) * 2.0 / 3.0

    fun transformLat(lng: Double, lat: Double): Double =
        -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat.pow(2) + 0.1 * lng * lat + 0.2 * sqrt(abs(lng)) +
                (20.0 * sin(6.0 * lng * PI) + 20.0 * sin(2.0 * lng * PI)) * 2.0 / 3.0 +
                (20.0 * sin(lat * PI) + 40.0 * sin(lat / 3.0 * PI)) * 2.0 / 3.0 +
                (160.0 * sin(lat / 12.0 * PI) + 320 * sin(lat * PI / 30.0)) * 2.0 / 3.0

    fun transform(lng: Double, lat: Double): Pair<Double, Double> = when {
        outOfChina(lng, lat) -> Pair(lng, lat)
        else -> (lat / 180.0 * PI).let { radLat ->
            (1 - EE * sin(radLat).pow(2)).let { magic ->
                sqrt(magic).let { sqrtMagic ->
                    Pair(
                        lng + transformLng(lng - 105.0, lat - 35.0)
                                * 180.0 / (A / sqrtMagic * cos(radLat) * PI),
                        lat + transformLat(lng - 105.0, lat - 35.0)
                                * 180.0 / (A * (1 - EE) / (magic * sqrtMagic) * PI)
                    )
                }
            }
        }
    }

    fun wgs84ToBd09(lng: Double, lat: Double): Pair<Double, Double> =
        wgs84ToGcj02(lng, lat).run { gcj02ToBd09(first, second) }

    fun wgs84ToGcj02(lng: Double, lat: Double): Pair<Double, Double> = when {
        outOfChina(lng, lat) -> Pair(lng, lat)
        else -> (lat / 180.0 * PI).let { radLat ->
            (1 - EE * sin(radLat).pow(2)).let { magic ->
                sqrt(magic).let { sqrtMagic ->
                    Pair(
                        lng + transformLng(lng - 105.0, lat - 35.0)
                                * 180.0 / (A / sqrtMagic * cos(radLat) * PI),
                        lat + transformLat(lng - 105.0, lat - 35.0)
                                * 180.0 / (A * (1 - EE) / (magic * sqrtMagic) * PI)
                    )
                }
            }
        }
    }

    fun gcj02ToBd09(lng: Double, lat: Double): Pair<Double, Double> =
        (sqrt(lng.pow(2) + lat.pow(2)) + 0.00002 * sin(lat * X_PI)).let { z ->
            (atan2(lat, lng) + 0.000003 * cos(lng * X_PI)).let { theta ->
                Pair(z * cos(theta) + 0.0065, z * sin(theta) + 0.006)
            }
        }
}