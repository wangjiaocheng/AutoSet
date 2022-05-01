package top.autoget.autosee

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.ImageView
import java.util.*

class Captcha private constructor(type: TYPE) {
    companion object {
        private var captcha: Captcha? = null
        fun build(type: TYPE = TYPE.CHARS): Captcha =
            captcha ?: Captcha(type).apply { captcha = this }

        private val charsNumber = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        private val charsLetter = charArrayOf(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
        )
        private val charsAll = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
        )
    }

    enum class TYPE { NUMBER, LETTER, CHARS }

    var mType = TYPE.CHARS

    init {
        mType = type
    }

    var codeLength = 4
    private val random = Random()
    val makeCode: String
        get() = StringBuilder().apply {
            when (mType) {
                TYPE.NUMBER -> for (i in 0 until codeLength) {
                    append(charsNumber[random.nextInt(charsNumber.size)])
                }
                TYPE.LETTER -> for (i in 0 until codeLength) {
                    append(charsLetter[random.nextInt(charsLetter.size)])
                }
                TYPE.CHARS -> for (i in 0 until codeLength) {
                    append(charsAll[random.nextInt(charsAll.size)])
                }
            }
        }.toString()
    var width = 200
    var height = 70
    var color = 0xdf//默认背景颜色值
    var fontSize = 60
    var code: String? = null
        get() = field?.toLowerCase(Locale.getDefault())
    private var paddingLeft: Int = 0
    private var paddingTop: Int = 0
    private val paddingBaseLeft = 20
    private val paddingBaseTop = 42
    private val paddingRangeLeft = 20
    private val paddingRangeTop = 15
    private val randomPadding = run {
        paddingLeft += paddingBaseLeft + random.nextInt(paddingRangeLeft)
        paddingTop = paddingBaseTop + random.nextInt(paddingRangeTop)
    }
    var lineNumber = 0//干扰线数量
    val makeBitmap: Bitmap
        get() = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).run {
                drawColor(Color.rgb(color, color, color))
                Paint().apply { textSize = fontSize.toFloat() }.let { paint ->
                    paddingLeft = 0
                    for (element in makeCode.apply { code = this }) {
                        randomPadding
                        drawText(
                            "$element", paddingLeft.toFloat(),
                            paddingTop.toFloat(), randomTextStyle(paint)
                        )
                    }
                    for (i in 0 until lineNumber) {
                        drawLine(this, paint)
                    }
                }
                save()
                restore()
            }
        }

    private fun randomTextStyle(paint: Paint): Paint = paint.apply {
        color = randomColor()
        textSkewX =
            (random.nextInt(11) / 10f).let { if (random.nextBoolean()) it else -it }//负数右斜，正数左斜
        isFakeBoldText = random.nextBoolean()//true粗体字，false非粗体
        isUnderlineText = true//true为下划线，false非下划线
        isStrikeThruText = true//true为删除线，false非删除线
    }

    private fun drawLine(canvas: Canvas, paint: Paint) = canvas.drawLine(
        random.nextInt(width).toFloat(), random.nextInt(height).toFloat(),
        random.nextInt(width).toFloat(), random.nextInt(height).toFloat(), paint.apply {
            strokeWidth = 1f
            color = randomColor()
        })

    private fun randomColor(rate: Int = 1): Int = Color.rgb(
        random.nextInt(256) / rate,
        random.nextInt(256) / rate, random.nextInt(256) / rate
    )

    fun into(imageView: ImageView?): Bitmap? = makeBitmap.apply { imageView?.setImageBitmap(this) }
}