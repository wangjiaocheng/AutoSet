package top.autoget.autokit

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Service
import android.content.Intent
import android.location.*
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import androidx.annotation.RequiresPermission
import top.autoget.autokit.AKit.app
import top.autoget.autokit.DataKit.formatTwo
import top.autoget.autokit.ToastKit.COLOR_BG_SUCCESS
import top.autoget.autokit.ToastKit.setBgColor
import top.autoget.autokit.ToastKit.showShort
import java.io.IOException
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

object LocationKit : LoggerKit {
    val settingsGps
        get() = app.startActivity(Intent().apply {
            action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })

    class LocationService : Service() {
        interface OnGetLocationListener {
            fun getLocation(
                lastLatitude: String, lastLongitude: String, latitude: String, longitude: String,
                country: String, locality: String, street: String
            )
        }

        var onGetLocationListener: OnGetLocationListener? = null

        @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
        override fun onDestroy() {
            unRegisterLocation()
            onGetLocationListener = null//非空内存泄漏
            super.onDestroy()
        }

        private val loading = "loading..."
        private var lastLatitude: String = loading
        private var lastLongitude: String = loading
        private var latitude: String = loading
        private var longitude: String = loading
        private var country: String = loading
        private var locality: String = loading
        private var street: String = loading
        private val onLocationChangeListener: OnLocationChangeListener =
            object : OnLocationChangeListener {
                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onLocationChanged(location: Location) {
                    location.latitude.let { it0 ->
                        location.longitude.let { it1 ->
                            onGetLocationListener?.getLocation(
                                lastLatitude, lastLongitude,
                                it0.toString().apply { latitude = this },
                                it1.toString().apply { longitude = this },
                                country, locality, street
                            )
                            country = getCountryName(it0, it1)
                            locality = getLocality(it0, it1)
                            street = getStreet(it0, it1)
                            onGetLocationListener?.getLocation(
                                lastLatitude, lastLongitude, latitude, longitude,
                                country, locality, street
                            )
                        }
                    }
                }

                override fun getLastKnownLocation(location: Location) =
                    onGetLocationListener?.getLocation(
                        location.latitude.toString().apply { lastLatitude = this },
                        location.longitude.toString().apply { lastLongitude = this },
                        latitude, longitude, country, locality, street
                    ) ?: Unit
            }

        @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
        override fun onCreate() {
            super.onCreate()
            Thread {
                Looper.prepare()
                if (registerLocation(0, 0, onLocationChangeListener))
                    setBgColor(COLOR_BG_SUCCESS)
                showShort("initApplication success")
                Looper.loop()
            }.start()
        }

        override fun onBind(intent: Intent): IBinder = object : Binder() {
            val service: LocationService
                get() = this@LocationService
        }
    }

    interface OnLocationChangeListener {
        fun getLastKnownLocation(location: Location)
        fun onLocationChanged(location: Location)
        fun onStatusChanged(provider: String, status: Int, extras: Bundle)
    }

    class AutoKitLocationListener : LocationListener {
        override fun onProviderEnabled(provider: String) {}//GPS开启
        override fun onProviderDisabled(provider: String) {}//GPS关闭
        override fun onLocationChanged(location: Location) =
            onLocationChangeListener?.onLocationChanged(location) ?: Unit

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) =
            onLocationChangeListener?.onStatusChanged(provider, status, extras).apply {
                when (status) {
                    LocationProvider.AVAILABLE -> debug("onStatusChanged->当前GPS状态：可见")
                    LocationProvider.OUT_OF_SERVICE -> debug("onStatusChanged->当前GPS状态：服务区外")
                    LocationProvider.TEMPORARILY_UNAVAILABLE -> debug("onStatusChanged->当前GPS状态：暂停服务")
                    else -> {
                    }
                }
            } ?: Unit
    }

    private var locationManager: LocationManager? = null
    private var onLocationChangeListener: OnLocationChangeListener? = null
    private val criteria: Criteria
        get() = Criteria().apply {
            accuracy = Criteria.ACCURACY_FINE//精度：ACCURACY_COARSE粗略；ACCURACY_FINE精细
            isSpeedRequired = true//速度
            isCostAllowed = true//收费
            isBearingRequired = true//方位
            isAltitudeRequired = true//海拔
            powerRequirement = Criteria.POWER_LOW//电源
        }
    private var autoKitLocationListener: AutoKitLocationListener? = null

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    fun registerLocation(
        minTime: Long, minDistance: Long, listener: OnLocationChangeListener?
    ): Boolean = listener?.let {
        onLocationChangeListener = it
        locationManager = app.locationManager
        when {
            isLocationEnabled -> {
                locationManager?.run {
                    getBestProvider(criteria, true)?.let { provider ->
                        getLastKnownLocation(provider)?.let { location ->
                            it.getLastKnownLocation(location)
                        }
                        requestLocationUpdates(
                            provider, minTime, minDistance.toFloat(), autoKitLocationListener
                                ?: AutoKitLocationListener().apply {
                                    autoKitLocationListener = this
                                })
                    }
                }
                true
            }
            else -> {
                debug("$loggerTag->无法定位，请打开定位服务")
                false
            }
        }
    } ?: false

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    fun unRegisterLocation() {
        autoKitLocationListener?.let {
            locationManager?.removeUpdates(it)
            autoKitLocationListener = null
            locationManager = null
        }
        onLocationChangeListener?.let { onLocationChangeListener = null }
    }

    fun getAddress(latitude: Double, longitude: Double): Address? = try {
        Geocoder(app, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
            .let { if (it.size > 0) it[0] else null }
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    private const val UNKNOWN = "unknown"
    fun getCountryName(latitude: Double, longitude: Double): String =
        getAddress(latitude, longitude)?.countryName ?: UNKNOWN

    fun getLocality(latitude: Double, longitude: Double): String =
        getAddress(latitude, longitude)?.locality ?: UNKNOWN

    fun getStreet(latitude: Double, longitude: Double): String =
        getAddress(latitude, longitude)?.getAddressLine(0) ?: UNKNOWN

    fun isMove(location: Location, preLocation: Location?): Boolean = preLocation?.run {
        abs(bearing - location.bearing).toDouble().let { compass ->
            location.distanceTo(this).toDouble().let { distance ->
                (location.speed * 3.6).let { speed ->
                    when (speed) {
                        0.0 -> false
                        else -> when {
                            speed < 35 && distance > 3 && distance < 10 ->
                                (if (compass > 180) 360 - compass else compass) > 10
                            else -> speed < 40 && distance > 10 && distance < 100 ||
                                    speed < 50 && distance > 10 && distance < 100 ||
                                    speed < 60 && distance > 10 && distance < 100 ||
                                    speed < 9999 && distance > 100
                        }
                    }
                }
            }
        }
    } ?: true

    private const val TWO_MINUTES = 1000 * 60 * 2
    fun isBetterLocation(newLocation: Location, currentBestLocation: Location?): Boolean =
        currentBestLocation?.run {
            (newLocation.time - time).let { timeDelta ->
                when {
                    timeDelta > TWO_MINUTES -> true
                    timeDelta < -TWO_MINUTES -> false
                    else -> (newLocation.accuracy - accuracy).toInt().let {
                        when {
                            it < 0 -> true
                            timeDelta > 0 && it <= 0 -> true
                            timeDelta > 0 && it <= 200 &&
                                    isSameProvider(newLocation.provider, provider) -> true
                            else -> false
                        }
                    }
                }
            }
        } ?: true

    fun isSameProvider(provider0: String?, provider1: String?): Boolean =
        provider0?.let { provider0 == provider1 } ?: provider1 == null

    val isLocationEnabled: Boolean
        get() = app.locationManager.getProviders(true).size > 0//isProviderEnabled(LocationManager.NETWORK_PROVIDER) || isProviderEnabled(LocationManager.GPS_PROVIDER)
    val isGpsEnabled: Boolean
        get() = app.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    @RequiresPermission(allOf = [ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION])
    fun getLocation(minTime: Long, minDistance: Long, listener: LocationListener): Location? = try {
        locationManager = app.locationManager
        when {
            isLocationEnabled && isGpsEnabled -> locationManager?.run {
                requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, minTime, minDistance.toFloat(), listener
                )
                getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.apply {
                    removeUpdates(listener)
                }
            }
            isGpsEnabled -> locationManager?.run {
                requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, minTime, minDistance.toFloat(), listener
                )
                getLastKnownLocation(LocationManager.GPS_PROVIDER)?.apply {
                    removeUpdates(listener)
                }
            }
            else -> {
                showShort("you have to open INTERNET or GPS")
                null
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun locationToDegree(location: Double): String = floor(location).let { degree ->
        ((location - degree) * 60).let { minuteTemp ->
            floor(minuteTemp).let { minute ->
                formatTwo.format((minuteTemp - minute) * 60)
                    .let { second -> "${degree.toInt()}°${minute.toInt()}′$second″" }
            }
        }
    }//如113.202222转113°12′8″
}