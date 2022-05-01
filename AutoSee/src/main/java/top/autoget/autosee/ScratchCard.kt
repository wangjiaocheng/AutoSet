package top.autoget.autosee

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class ScratchCard(context: Context) : View(context) {
    private val bgBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.img_loading)
    private val fgBitmap: Bitmap =
        bgBitmap.run { Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)//1
        canvas.run {
            drawBitmap(bgBitmap, 0f, 0f, null)//2
            drawBitmap(fgBitmap, 0f, 0f, null)//3
        }
    }

    private val canvas: Canvas =
        Canvas(fgBitmap).apply { drawColor(Color.parseColor("#1dcdef"))/*5*/ }//4
    private val path: Path = Path()
    private val paint: Paint = Paint().apply {
        alpha = 0
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 60f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.run {
                reset()
                moveTo(event.x, event.y)
                lineTo(event.x + 1, event.y + 1)
            }
            MotionEvent.ACTION_MOVE -> path.lineTo(event.x, event.y)
            MotionEvent.ACTION_UP -> {
            }
        }
        canvas.drawPath(path, paint)//6
        invalidate()
        return true
    }
}