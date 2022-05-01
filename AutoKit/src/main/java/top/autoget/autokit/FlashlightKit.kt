package top.autoget.autokit

import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.Parameters.FLASH_MODE_OFF
import android.hardware.Camera.Parameters.FLASH_MODE_TORCH
import top.autoget.autokit.AKit.app
import java.io.IOException

object FlashlightKit : LoggerKit {
    val isFlashlightEnable: Boolean
        get() = app.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    private var camera: Camera? = null
    val isFlashlightOn: Boolean
        get() = when {
            init() -> camera?.parameters?.flashMode == FLASH_MODE_TORCH
            else -> false
        }
    private var surfaceTexture: SurfaceTexture? = null
    fun setFlashlightStatus(isOn: Boolean = true) {
        if (init()) camera?.parameters?.let {
            when {
                isOn -> if (it.flashMode != FLASH_MODE_TORCH) try {
                    camera?.apply { setPreviewTexture(surfaceTexture) }?.startPreview()
                    it.flashMode = FLASH_MODE_TORCH
                } catch (e: IOException) {
                    error("$loggerTag->setFlashlightStatus: ")
                }
                else -> if (it.flashMode != FLASH_MODE_OFF) it.flashMode = FLASH_MODE_OFF
            }
        }
    }

    private fun init(): Boolean {
        if (camera == null) {
            try {
                camera = Camera.open(0)
                surfaceTexture = SurfaceTexture(0)
            } catch (t: Throwable) {
                error("$loggerTag->initApplication failed: ")
                return false
            }
        }
        if (camera == null) {
            error("$loggerTag->initApplication failed.")
            return false
        }
        return true
    }

    val destroy
        get() = camera?.let {
            it.release()
            camera = null
            surfaceTexture = null
        }
}