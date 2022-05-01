package top.autoget.autokit

import android.location.Location
import android.media.ExifInterface
import top.autoget.autokit.FileKit.isExistsTimestamp
import kotlin.math.abs

object ExifKit : LoggerKit {
    fun writeLatLonIntoJpeg(picPath: String, dLat: Double, dLon: Double) {
        if (isExistsTimestamp(picPath)) try {
            ExifInterface(picPath).run {
                if (getAttribute(ExifInterface.TAG_GPS_LATITUDE) == null &&
                    getAttribute(ExifInterface.TAG_GPS_LONGITUDE) == null
                ) apply {
                    setAttribute(ExifInterface.TAG_GPS_LATITUDE, gpsInfoConvert(dLat))
                    setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (dLat > 0) "N" else "S")
                    setAttribute(ExifInterface.TAG_GPS_LONGITUDE, gpsInfoConvert(dLon))
                    setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (dLon > 0) "E" else "W")
                    saveAttributes()
                }
                debug(
                    """
                        |${getAttribute(ExifInterface.TAG_GPS_LATITUDE)}
                        |${getAttribute(ExifInterface.TAG_GPS_LONGITUDE)}
                        |${getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD)}
                        |${getAttribute(ExifInterface.TAG_IMAGE_LENGTH)}
                        |${getAttribute(ExifInterface.TAG_IMAGE_WIDTH)}
                    """.trimMargin()
                )
            }
        } catch (e: Exception) {
        }
    }

    private fun gpsInfoConvert(gpsInfo: Double): String =
        Location.convert(abs(gpsInfo), Location.FORMAT_SECONDS)
            .split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().let { array ->
                array[2].split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    .let { "${array[0]}/1,${array[1]}/1,${if (it.isEmpty()) array[2] else it[0]}/1" }
            }
}