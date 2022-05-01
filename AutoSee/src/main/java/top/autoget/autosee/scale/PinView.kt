package top.autoget.autosee.scale

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import top.autoget.autosee.R

class PinView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) :
    ScaleImageView(context, attr) {
    private val initPin: Bitmap = resources.displayMetrics.densityDpi.let { density ->
        BitmapFactory.decodeResource(resources, R.mipmap.pushpin_blue).run {
            Bitmap.createScaledBitmap(
                this, density / 420 * width, density / 420 * height, true
            )
        }
    }
    private val pin: Bitmap
        get() = initPin
    var pinS: PointF = PointF()
        set(pinS) {
            field = pinS
            initPin
            invalidate()
        }
    private val paint: Paint = Paint().apply { isAntiAlias = true }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isReady) sourceToViewCoord(pinS)?.let {
            pin.run {
                canvas.drawBitmap(this, it.x - width / 2, it.y - height, paint)
            }
        }
    }
}