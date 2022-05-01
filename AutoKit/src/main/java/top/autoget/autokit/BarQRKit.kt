package top.autoget.autokit

import android.graphics.Bitmap
import android.widget.ImageView
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import top.autoget.autokit.ImageKit.compressByScale
import java.util.*

object BarQRKit {
    class BitmapLuminanceSource(bitmap: Bitmap) : LuminanceSource(bitmap.width, bitmap.height) {
        private val pixels: ByteArray = ByteArray(width * height).apply {
            IntArray(size)
                .apply { bitmap.getPixels(this, 0, width, 0, 0, width, height) }.let {
                    for ((index, value) in it.withIndex()) {
                        this[index] = value.toByte()
                    }//IntArray→ByteArray，取像素值中蓝色值
                }
        }

        override fun getMatrix(): ByteArray = pixels
        override fun getRow(y: Int, row: ByteArray): ByteArray =
            pixels.copyOfRange(width * y, width * (y + 1))
    }

    fun decodeFromBitmap(bitmap: Bitmap?): Result? = bitmap?.let {
        MultiFormatReader().run {
            setHints(Hashtable<DecodeHintType, Any>(2).apply {
                this[DecodeHintType.POSSIBLE_FORMATS] = Vector<BarcodeFormat>(17).apply {
                    add(BarcodeFormat.UPC_A)
                    add(BarcodeFormat.UPC_E)
                    add(BarcodeFormat.EAN_8)
                    add(BarcodeFormat.EAN_13)
                    add(BarcodeFormat.RSS_14)//以上产品
                    add(BarcodeFormat.CODE_39)
                    add(BarcodeFormat.CODE_93)
                    add(BarcodeFormat.CODE_128)
                    add(BarcodeFormat.ITF)//以上条码
                    add(BarcodeFormat.QR_CODE)//二维
                    add(BarcodeFormat.DATA_MATRIX)//以上需要
                    add(BarcodeFormat.AZTEC)
                    add(BarcodeFormat.CODABAR)
                    add(BarcodeFormat.MAXICODE)
                    add(BarcodeFormat.PDF_417)
                    add(BarcodeFormat.RSS_EXPANDED)
                    add(BarcodeFormat.UPC_EAN_EXTENSION)
                }
                this[DecodeHintType.CHARACTER_SET] = "UTF8"
            })
            compressByScale(it, it.width / 2f, it.height / 2f, true)?.let { bitmap ->
                try {
                    decodeWithState(BinaryBitmap(HybridBinarizer(BitmapLuminanceSource(bitmap))))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
    }

    fun builderBarCode(text: CharSequence): BuilderBarCode = BuilderBarCode(text)
    class BuilderBarCode(private val content: CharSequence) {
        private var mCodeWidth = 1000
        fun codeWidth(codeWidth: Int): BuilderBarCode = apply { mCodeWidth = codeWidth }
        private var mCodeHeight = 300
        fun codeHeight(codeHeight: Int): BuilderBarCode = apply { mCodeHeight = codeHeight }
        private var mCodeColor = -0x1000000
        fun codeColor(codeColor: Int): BuilderBarCode = apply { mCodeColor = codeColor }
        private var mBackgroundColor = -0x1
        fun backColor(backgroundColor: Int): BuilderBarCode =
            apply { mBackgroundColor = backgroundColor }

        fun into(imageView: ImageView?): Bitmap? =
            createBarCode(imageView, content, mCodeWidth, mCodeHeight, mCodeColor, mBackgroundColor)
    }

    @JvmOverloads
    fun createBarCode(
        ivCode: ImageView?, content: CharSequence?, barWidth: Int = 1000, barHeight: Int = 300,
        codeColor: Int = -0x1000000, backgroundColor: Int = -0x1
    ): Bitmap? = try {
        content?.let {
            when {
                it == "" || it.isEmpty() -> null
                else -> MultiFormatWriter()
                    .encode("$it", BarcodeFormat.CODE_128, barWidth, barHeight, null).run {
                        IntArray(width * height).apply {
                            for (y in 0 until height) {
                                for (x in 0 until width) {
                                    this[width * y + x] =
                                        if (get(x, y)) codeColor else backgroundColor
                                }
                            }
                        }.let { pixels ->
                            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                .apply { setPixels(pixels, 0, width, 0, 0, width, height) }
                        }
                    }
            }
        }
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }.apply { ivCode?.setImageBitmap(this) }

    fun builderQRCode(text: CharSequence): BuilderQRCode = BuilderQRCode(text)
    class BuilderQRCode(private val content: CharSequence) {
        private var mCodeSide = 800
        fun codeSide(codeSide: Int): BuilderQRCode = apply { mCodeSide = codeSide }
        private var mCodeColor = -0x1000000
        fun codeColor(codeColor: Int): BuilderQRCode = apply { mCodeColor = codeColor }
        private var mBackgroundColor = -0x1
        fun backColor(backgroundColor: Int): BuilderQRCode =
            apply { mBackgroundColor = backgroundColor }

        fun into(imageView: ImageView?): Bitmap? =
            createQRCode(imageView, content, mCodeSide, mCodeSide, mCodeColor, mBackgroundColor)
    }

    @JvmOverloads
    fun createQRCode(
        ivCode: ImageView?, content: CharSequence?, qrWidth: Int = 800, qrHeight: Int = 800,
        codeColor: Int = -0x1000000, backgroundColor: Int = -0x1
    ): Bitmap? = try {
        content?.let {
            when {
                it == "" || it.isEmpty() -> null
                else -> QRCodeWriter().encode("$it", BarcodeFormat.QR_CODE, qrWidth, qrHeight,
                    Hashtable<EncodeHintType, String>()
                        .apply { this[EncodeHintType.CHARACTER_SET] = "utf-8" }).run {
                    IntArray(width * height).apply {
                        for (y in 0 until height) {
                            for (x in 0 until width) {
                                this[width * y + x] = if (get(x, y)) codeColor else backgroundColor
                            }
                        }
                    }.let { pixels ->
                        Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                            .apply { setPixels(pixels, 0, width, 0, 0, width, height) }
                    }
                }
            }
        }
    } catch (e: WriterException) {
        e.printStackTrace()
        null
    }.apply { ivCode?.setImageBitmap(this) }
}