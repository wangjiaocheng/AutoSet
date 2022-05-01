package top.autoget.autokit

import android.app.Activity
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
import android.provider.MediaStore
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import top.autoget.autokit.AKit.app
import top.autoget.autokit.ConvertKit.bitmap2Drawable
import top.autoget.autokit.FileKit.createFileNew
import top.autoget.autokit.FileKit.getFileByPath
import top.autoget.autokit.FileKit.getFileExtension
import top.autoget.autokit.StringKit.isSpace
import top.autoget.autokit.VersionKit.aboveHoneycombMR1
import top.autoget.autokit.VersionKit.aboveJellyBeanMR2
import top.autoget.autokit.VersionKit.aboveKitKat
import java.io.*
import java.lang.ref.WeakReference
import java.net.URL
import java.util.*
import kotlin.math.*

object ImageKit : LoggerKit {
    fun makeKey(httpUrl: String): ByteArray = httpUrl.toByteArray()
    fun isSameKey(key: ByteArray, buffer: ByteArray): Boolean {
        when {
            key.size != buffer.size -> return false
            else -> {
                for ((index, byte) in key.withIndex()) {
                    if (byte != buffer[index]) return false
                }
                return true
            }
        }
    }

    fun crc64Long(string: String?): Long =
        string?.run { if (isEmpty()) 0L else crc64Long(toByteArray()) } ?: 0L

    private const val INITIAL_CRC = -0x1L
    private const val POLY64REV = -0x6a536cd653b4364bL
    private val crcTable: LongArray = LongArray(256).apply {
        var part: Long
        for (i in 0..255) {
            part = i.toLong()
            for (j in 0..7) {
                part = part shr 1 xor (if (part.toInt() and 1 != 0) POLY64REV else 0)
            }
            this[i] = part
        }
    }

    fun crc64Long(bytes: ByteArray): Long {
        var crc = INITIAL_CRC
        for (byte in bytes) {
            crc = crcTable[crc.toInt() xor byte.toInt() and 0xff] xor (crc shr 8)
        }
        return crc
    }

    fun getColorByInt(colorInt: Int): Int = colorInt or -16777216

    @JvmOverloads
    fun getColorHexString(color: Int, showAlpha: Boolean = true): String = String.format(
        if (showAlpha) "#%08X" else "#%06X", if (showAlpha) -0x1 else 0xFFFFFF and color
    ).toUpperCase(Locale.getDefault())

    fun getAlphaPercent(argb: Int): Float = Color.alpha(argb) / 255f
    fun setColorAlphaByInt(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    fun setColorAlphaByFloat(color: Int, alpha: Float): Int =
        alphaValueAsInt(alpha) shl 24 or (0x00ffffff and color)

    private fun alphaValueAsInt(alpha: Float): Int = (alpha * 255).roundToInt()
    fun getColorLightness(color: Int): Float =
        FloatArray(3).apply { Color.colorToHSV(color, this) }[2]

    fun setColorLightness(color: Int, lightness: Float): Int = FloatArray(3).apply {
        Color.colorToHSV(color, this)
        this[2] = lightness
    }.let { Color.HSVToColor(it) }

    private val drawables: WeakHashMap<Int, WeakReference<Drawable?>> = WeakHashMap()
    fun getDrawableFromMap(resId: Int): Drawable? = try {
        drawables[resId].apply {
            if (!drawables.containsKey(resId)) try {
                drawables[resId] = WeakReference(bitmap2Drawable(getBitmap(resId)))
            } catch (e: OutOfMemoryError) {
                e.printStackTrace()
            }
        }?.get()
    } catch (e: Exception) {
        e.printStackTrace()
        bitmap2Drawable(getBitmap(resId))
    }

    private val bitmaps: WeakHashMap<Int, WeakReference<Bitmap?>> = WeakHashMap()
    fun getBitmapFromMap(resId: Int): Bitmap? = bitmaps[resId].apply {
        if (!bitmaps.containsKey(resId)) bitmaps[resId] = WeakReference(getBitmap(resId))
    }?.get()

    fun recycleBitmaps() = bitmaps.clear().apply {
        for (value in bitmaps.values) {
            value.get()?.recycle()
        }
    }

    fun getBitmapFromLocalOrNet(url: String): Bitmap? = try {
        URL(url).openStream().use { inputStream ->
            BufferedInputStream(inputStream).use { bufferedInputStream ->
                ByteArrayOutputStream().use { byteArrayOutputStream ->
                    byteArrayOutputStream.apply {
                        BufferedOutputStream(byteArrayOutputStream).use { bufferedOutputStream ->
                            ByteArray(1024).let { bytes ->
                                while (true) {
                                    if (bufferedInputStream.read(bytes) != -1)
                                        bufferedOutputStream.write(bytes) else break
                                }
                            }
                            bufferedOutputStream.flush()
                        }
                    }.toByteArray().run { BitmapFactory.decodeByteArray(this, 0, size) }
                }
            }
        }//MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }

    fun getPicPathFromUri(uri: Uri, activity: Activity): String = (uri.path ?: "").let { url ->
        try {
            when {
                url.startsWith("/external") -> activity.contentResolver.query(
                    uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null
                )?.use { cursor ->
                    when {
                        cursor.count > 0 -> cursor.run {
                            moveToFirst()
                            getString(getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        }
                        else -> url
                    }
                } ?: url
                else -> url
            }
        } catch (e: Exception) {
            e.printStackTrace()
            url
        }
    }

    @JvmOverloads
    fun getBitmap(filePath: String?, maxWidth: Int = 0, maxHeight: Int = 0): Bitmap? = when {
        isSpace(filePath) -> null
        else -> when {
            maxWidth > 0 && maxHeight > 0 ->
                BitmapFactory.decodeFile(filePath, BitmapFactory.Options().apply {
                    inJustDecodeBounds = true//获取宽高，不占内存
                    BitmapFactory.decodeFile(filePath, this)
                    inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                    inPreferredConfig = Bitmap.Config.RGB_565
                    inJustDecodeBounds = false
                })
            else -> BitmapFactory.decodeFile(filePath)
        }
    }

    @JvmOverloads
    fun getBitmap(file: File?, maxWidth: Int = 0, maxHeight: Int = 0): Bitmap? =
        file?.absolutePath?.let { path ->
            when {
                maxWidth > 0 && maxHeight > 0 ->
                    BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeFile(path, this)
                        inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inJustDecodeBounds = false
                    })
                else -> BitmapFactory.decodeFile(path)
            }
        }

    @JvmOverloads
    fun getBitmap(inputStream: InputStream?, maxWidth: Int = 0, maxHeight: Int = 0): Bitmap? =
        inputStream?.use { stream ->
            when {
                maxWidth > 0 && maxHeight > 0 ->
                    BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeStream(stream, null, this)
                        inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inJustDecodeBounds = false
                    })
                else -> BitmapFactory.decodeStream(stream)
            }
        }

    @JvmOverloads
    fun getBitmap(
        bytes: ByteArray?, offset: Int = 0, length: Int = bytes?.size ?: offset,
        maxWidth: Int = 0, maxHeight: Int = 0
    ): Bitmap? = bytes?.let { array ->
        when {
            array.isEmpty() -> null
            else -> when {
                maxWidth > 0 && maxHeight > 0 -> BitmapFactory
                    .decodeByteArray(array, offset, length, BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeByteArray(array, offset, length, this)
                        inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inJustDecodeBounds = false
                    })
                else -> BitmapFactory.decodeByteArray(array, offset, length)
            }
        }
    }

    @JvmOverloads
    fun getBitmap(@DrawableRes resId: Int, maxWidth: Int = 0, maxHeight: Int = 0): Bitmap? =
        Resources.getSystem()?.let { resources ->
            when {
                maxWidth > 0 && maxHeight > 0 ->
                    BitmapFactory.decodeResource(resources, resId, BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeResource(resources, resId, this)
                        inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inJustDecodeBounds = false
                    })
                else -> BitmapFactory.decodeResource(resources, resId)
            }
        }

    @JvmOverloads
    fun getBitmap(fileDescriptor: FileDescriptor?, maxWidth: Int = 0, maxHeight: Int = 0): Bitmap? =
        fileDescriptor?.let { fd ->
            when {
                maxWidth > 0 && maxHeight > 0 ->
                    BitmapFactory.decodeFileDescriptor(fd, null, BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                        BitmapFactory.decodeFileDescriptor(fd, null, this)
                        inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inJustDecodeBounds = false
                    })
                else -> BitmapFactory.decodeFileDescriptor(fd)
            }
        }

    fun drawNinePatch(src: Bitmap, canvas: Canvas, rect: Rect) =
        NinePatch(src, src.ninePatchChunk, null).draw(canvas, rect)

    @JvmOverloads
    fun drawColor(src: Bitmap, @ColorInt color: Int, recycle: Boolean = false): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> (if (recycle) src else src.copy(src.config, true))
            .apply { Canvas(this).drawColor(color, PorterDuff.Mode.DARKEN) }
    }

    private fun isEmptyBitmap(src: Bitmap?): Boolean =
        src?.run { width == 0 || height == 0 } ?: true

    fun getDropShadow(imageView: ImageView, src: Bitmap, radius: Float, shadowColor: Int): Bitmap =
        Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).run {
                src.extractAlpha().let { bitmap ->
                    Paint().apply {
                        isAntiAlias = true
                        color = shadowColor
                    }.let { paint ->
                        drawBitmap(bitmap, 0f, 0f, paint)
                        drawBitmap(bitmap, 0f, 0f, paint.apply {
                            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER)
                        })
                    }
                }
            }
            imageView.setImageBitmap(this)
        }

    @JvmOverloads
    fun toClip(
        src: Bitmap, x: Int, y: Int, width: Int, height: Int, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> Bitmap.createBitmap(src, x, y, width, height)
            .apply { if (recycle && !src.isRecycled && this != src) src.recycle() }
    }

    @JvmOverloads
    fun toSkew(
        src: Bitmap, kx: Float, ky: Float, px: Float = 0f, py: Float = 0f, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> Matrix().apply { setSkew(kx, ky, px, py) }.let { matrix ->
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
                .apply { if (recycle && !src.isRecycled && this != src) src.recycle() }
        }
    }

    @JvmOverloads
    fun toRotate(
        src: Bitmap, degrees: Float = 0f, px: Float = 0f, py: Float = 0f, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        degrees == 0f -> src
        else -> Matrix().apply { setRotate(degrees, px, py) }.let { matrix ->
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
                .apply { if (recycle && !src.isRecycled && this != src) src.recycle() }
        }
    }

    fun getRotateDegree(filePath: String): Int = try {
        ExifInterface(filePath)
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL).let {
                when (it) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }
    } catch (e: IOException) {
        e.printStackTrace()
        -1
    }

    @JvmOverloads
    fun toRound(
        src: Bitmap, borderSize: Float = 0f, @ColorInt borderColor: Int = 0,
        recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> {
            val width = src.width
            val height = src.height
            val size = min(width, height)
            RectF(0f, 0f, width.toFloat(), height.toFloat())
                .apply { inset((width - size) / 2f, (height - size) / 2f) }.let { rectF ->
                    Matrix().apply {
                        setTranslate(rectF.left, rectF.top)
                        if (width != height)
                            preScale(size.toFloat() / width, size.toFloat() / height)
                    }.let { matrix ->
                        Paint().apply {
                            shader = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                                .apply { setLocalMatrix(matrix) }
                            isAntiAlias = true
                            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                        }.let { paint ->
                            Bitmap.createBitmap(width, height, src.config).apply {
                                Canvas(this).run {
                                    drawRoundRect(rectF, size / 2f, size / 2f, paint)
                                    if (borderSize > 0) drawCircle(
                                        width / 2f, height / 2f, size / 2f - borderSize / 2f,
                                        paint.apply {
                                            shader = null
                                            color = borderColor
                                            strokeWidth = borderSize
                                            style = Paint.Style.STROKE
                                        })
                                }
                                if (recycle && !src.isRecycled && this != src) src.recycle()
                            }
                        }
                    }
                }
        }
    }

    @JvmOverloads
    fun toRoundCorner(
        src: Bitmap, radius: Float, borderSize: Float = 0f, @ColorInt borderColor: Int = 0,
        recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> {
            val width = src.width
            val height = src.height
            RectF(0f, 0f, width.toFloat(), height.toFloat())
                .apply { inset(borderSize / 2f, borderSize / 2f) }.let { rectF ->
                    Paint().apply {
                        shader = BitmapShader(src, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                        isAntiAlias = true
                    }.let { paint ->
                        Bitmap.createBitmap(width, height, src.config).apply {
                            Canvas(this).run {
                                drawRoundRect(rectF, radius, radius, paint)
                                if (borderSize > 0) drawRoundRect(
                                    rectF, radius, radius, paint.apply {
                                        shader = null
                                        color = borderColor
                                        strokeWidth = borderSize
                                        strokeCap = Paint.Cap.ROUND
                                        style = Paint.Style.STROKE
                                    })
                            }
                            if (recycle && !src.isRecycled && this != src) src.recycle()
                        }
                    }
                }
        }
    }

    @JvmOverloads
    fun addBorder(
        src: Bitmap, borderSize: Float = 0f, @ColorInt borderColor: Int = 0,
        isCircleOrCorner: Boolean = true, cornerRadius: Float = 0f, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> Paint().apply {
            isAntiAlias = true
            color = borderColor
            strokeWidth = borderSize
            style = Paint.Style.STROKE
        }.let { paint ->
            (if (recycle) src else src.copy(src.config, true)).apply {
                Canvas(this).run {
                    when {
                        isCircleOrCorner -> drawCircle(
                            width / 2f, height / 2f,
                            min(width, height) / 2f - borderSize / 2f, paint
                        )
                        else -> (borderSize.toInt() shr 1).toFloat().let {
                            drawRoundRect(
                                RectF(it, it, width - it, height - it),
                                cornerRadius, cornerRadius, paint
                            )
                        }
                    }
                }
            }
        }
    }

    @JvmOverloads
    fun addReflection(
        src: Bitmap, reflectionHeight: Int, reflectionGap: Float = 0f, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> {
            val srcWidth = src.width
            val srcHeight = src.height
            Bitmap.createBitmap(
                src, 0, srcHeight - reflectionHeight,
                srcWidth, reflectionHeight, Matrix().apply { preScale(1f, -1f) }, false
            ).let { reflectionBitmap ->
                Bitmap.createBitmap(srcWidth, srcHeight + reflectionHeight, src.config).apply {
                    Canvas(this).run {
                        drawBitmap(src, 0f, 0f, null)
                        drawBitmap(reflectionBitmap, 0f, srcHeight + reflectionGap, null)
                        save()
                        drawRect(
                            0f, srcHeight + reflectionGap,
                            srcWidth.toFloat(), height + reflectionGap, Paint().apply {
                                shader = LinearGradient(
                                    0f, srcHeight.toFloat(), 0f, height + reflectionGap,
                                    0x70FFFFFF, 0x00FFFFFF, Shader.TileMode.MIRROR
                                )
                                isAntiAlias = true
                                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                            }
                        )
                        restore()
                    }
                    if (!reflectionBitmap.isRecycled) reflectionBitmap.recycle()
                    if (recycle && !src.isRecycled && this != src) src.recycle()
                }
            }
        }
    }//倒影

    fun createTextImage(
        src: Bitmap, innerText: String?, size: Float, textColor: Int, textBgColor: Int
    ): Bitmap? = when {
        isEmptyBitmap(src) || innerText == null -> null
        else -> when {
            src.width > size * innerText.length -> src.width + size * innerText.length
            else -> size * innerText.length
        }.let { width ->
            (src.height + size).let { height ->
                return Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_4444)
                    .apply {
                        Canvas(this).run {
                            val posX = (width - size * innerText.length) / 2
                            val posY = height / 2
                            val textX = posX + size * innerText.length / 4
                            drawBitmap(src, (width - src.width) / 2, 0f, Paint())
                            drawRoundRect(RectF().apply {
                                left = posX
                                right = posX + size * innerText.length
                                top = posY
                                bottom = posY + size
                            }, 10f, 10f, Paint().apply {
                                color = textBgColor
                                strokeWidth = 3f
                                style = Paint.Style.FILL_AND_STROKE
                            })
                            drawText(innerText, textX, posY + size - 2, Paint().apply {
                                isAntiAlias = true
                                color = textColor
                                textSize = size
                            })
                        }
                    }
            }
        }
    }

    @JvmOverloads
    fun addTextWatermark(
        src: Bitmap, content: String?, size: Float, @ColorInt textColor: Int, textAlpha: Int,
        x: Float, y: Float, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) || content == null -> null
        else -> src.copy(src.config, true).apply {
            Canvas(this).drawText(content, x, y + size, Paint().apply {
                isAntiAlias = true
                textSize = size
                color = textColor
                alpha = textAlpha
                getTextBounds(content, 0, content.length, Rect())
            })
            if (recycle && !src.isRecycled && this != src) src.recycle()
        }
    }

    @JvmOverloads
    fun addImageWatermark(
        src: Bitmap, watermark: Bitmap, bitmapAlpha: Int,
        x: Float, y: Float, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> src.copy(src.config, true).apply {
            if (!isEmptyBitmap(watermark))
                Canvas(this).drawBitmap(watermark, x, y, Paint().apply {
                    isAntiAlias = true
                    alpha = bitmapAlpha
                })
            if (recycle && !src.isRecycled && this != src) src.recycle()
        }
    }

    @JvmOverloads
    fun toAlpha(src: Bitmap, recycle: Boolean = false): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> src.extractAlpha()
            .apply { if (recycle && !src.isRecycled && this != src) src.recycle() }
    }

    @JvmOverloads
    fun setAlpha(src: Bitmap, alpha: Int, recycle: Boolean = false): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> IntArray(src.width * src.height).apply {
            src.getPixels(this, 0, src.width, 0, 0, src.width, src.height)
            for (i in indices) {
                if (this[i] and -0x1000000 != 0x00000000)
                    this[i] = (alpha * 255 / 100) shl 24 or (this[i] and 0xFFFFFF)//透明不做处理,修改最高2位
            }
        }.let {
            Bitmap.createBitmap(it, src.width, src.height, Bitmap.Config.ARGB_8888)
                .apply { if (recycle && !src.isRecycled && this != src) src.recycle() }
        }
    }

    @JvmOverloads
    fun toGray(src: Bitmap, recycle: Boolean = false): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> Bitmap.createBitmap(src.width, src.height, src.config).apply {
            Canvas(this).drawBitmap(src, 0f, 0f, Paint().apply {
                colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
            })
            if (recycle && !src.isRecycled && this != src) src.recycle()
        }
    }//src.config或Bitmap.Config.RGB_565

    fun toGrey(src: Bitmap): Bitmap = src.run {
        IntArray(width * height).apply {
            src.getPixels(this, 0, width, 0, 0, width, height)
            for (i in 0 until height) {
                for (j in 0 until width) {
                    var grey = this[width * i + j]
                    grey =
                        ((grey and 0x00FF0000 shr 16) * 0.3 + (grey and 0x0000FF00 shr 8) * 0.59 + (grey and 0x000000FF) * 0.11).toInt()
                    this[width * i + j] = (0xFF shl 24) or (grey shl 16) or (grey shl 8) or grey
                }
            }
        }.let {
            Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                .apply { setPixels(it, 0, width, 0, 0, width, height) }
        }
    }

    fun grayMasking(src: Bitmap, x: Int, y: Int, radius: Float): Bitmap =
        Bitmap.createBitmap(src.width, src.height, Bitmap.Config.RGB_565).apply {
            var newR: Int
            var newG: Int
            var newB: Int
            var idx: Int
            val delta = 18//值越小图越亮
            IntArray(width * height).let { pixels ->
                src.getPixels(pixels, 0, width, 0, 0, width, height)
                intArrayOf(1, 2, 1, 2, 4, 2, 1, 2, 1).let { gauss ->
                    var i = 1
                    while (i < height - 1) {
                        var j = 1
                        while (j < width - 1) {
                            if ((j - x).toDouble().pow(2) + (i - y).toDouble().pow(2)
                                > radius.pow(2)
                            ) {
                                newR = 0
                                newG = 0
                                newB = 0
                                idx = 0
                                for (m in -1..1) {
                                    for (n in -1..1) {
                                        pixels[(i + m) * width + j + n].let { pixColor ->
                                            newR += gauss[idx] * Color.red(pixColor)
                                            newG += gauss[idx] * Color.green(pixColor)
                                            newB += gauss[idx] * Color.blue(pixColor)
                                        }
                                        idx++
                                    }
                                }
                                pixels[i * width + j] = Color.argb(
                                    255,
                                    min(255, max(0, newR / delta)),
                                    min(255, max(0, newG / delta)),
                                    min(255, max(0, newB / delta))
                                )
                            }//半径矩形区域外部模糊
                            j++
                        }
                        i++
                    }
                }
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        }//光晕效果

    @JvmOverloads
    fun fastBlur(
        src: Bitmap, @FloatRange(from = 0.0, to = 1.0, fromInclusive = false) scale: Float,
        @FloatRange(from = 0.0, to = 25.0, fromInclusive = false) radius: Float,
        recycle: Boolean = false, isReturnScale: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> Bitmap.createBitmap(
            src, 0, 0, src.width, src.height, Matrix().apply { setScale(scale, scale) }, true
        ).let {
            Canvas().apply { scale(scale, scale) }.drawBitmap(
                it, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
                    colorFilter = PorterDuffColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_ATOP)
                })
            when {
                aboveJellyBeanMR2 -> renderScriptBlur(it, radius, recycle)
                else -> stackBlur(it, radius.toInt(), recycle)
            }.let { scaleBitmap ->
                when {
                    scale == 1f || isReturnScale -> scaleBitmap
                        .apply { if (recycle && !src.isRecycled && this != src) src.recycle() }
                    else -> Bitmap
                        .createScaledBitmap(scaleBitmap, src.width, src.height, true).apply {
                            if (!scaleBitmap.isRecycled) scaleBitmap.recycle()
                            if (recycle && !src.isRecycled && this != src) src.recycle()
                        }
                }
            }
        }
    }//快速模糊：缩小模糊，恢复尺寸

    @RequiresApi(JELLY_BEAN_MR1)
    @JvmOverloads
    fun renderScriptBlur(
        src: Bitmap, @FloatRange(from = 0.0, to = 25.0, fromInclusive = false) radius: Float,
        recycle: Boolean = false
    ): Bitmap = (if (recycle) src else src.copy(src.config, true)).apply {
        RenderScript.create(app)
            .apply { messageHandler = RenderScript.RSMessageHandler() }.let { renderScript ->
                Allocation.createFromBitmap(
                    renderScript, this,
                    Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT
                ).let { input ->
                    Allocation.createTyped(renderScript, input.type).let { output ->
                        ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)).apply {
                            setInput(input)
                            setRadius(if (radius > 25) 25.0f else if (radius <= 0) 1.0f else radius)
                        }.forEach(output)
                        output.copyTo(this)
                    }
                }
            }
    }

    @JvmOverloads
    fun stackBlur(src: Bitmap, radius: Int, recycle: Boolean = false): Bitmap =
        (if (recycle) src else src.copy(src.config, true)).apply {
            (if (radius < 1) 1 else radius).let { radius0 ->
                (radius0 * 2 + 1).let { div ->
                    IntArray(width * height).let { pix ->
                        getPixels(pix, 0, width, 0, 0, width, height)
                        val dv = ((div + 1) shr 1).toDouble().pow(2).toInt().let { divSum ->
                            IntArray(256 * divSum).apply {
                                var i = 0
                                while (i < 256 * divSum) {
                                    this[i] = (i / divSum)
                                    i++
                                }
                            }
                        }
                        val vMin = IntArray(max(width, height))
                        val r = IntArray(width * height)
                        val g = IntArray(width * height)
                        val b = IntArray(width * height)
                        val stack = Array(div) { IntArray(3) }
                        var yi = 0
                        var yw = 0
                        var y = 0
                        while (y < height) {
                            var rSum = 0
                            var gSum = 0
                            var bSum = 0
                            var rOutSum = 0
                            var gOutSum = 0
                            var bOutSum = 0
                            var rInSum = 0
                            var gInSum = 0
                            var bInSum = 0
                            var i = -radius0
                            while (i <= radius0) {
                                val p = pix[yi + min(width - 1, max(i, 0))]
                                val sir = stack[i + radius0]
                                sir[0] = (p and 0xff0000) shr 16
                                sir[1] = (p and 0x00ff00) shr 8
                                sir[2] = (p and 0x0000ff)
                                val rbs = radius0 + 1 - abs(i)
                                rSum += sir[0] * rbs
                                gSum += sir[1] * rbs
                                bSum += sir[2] * rbs
                                when {
                                    i > 0 -> {
                                        rInSum += sir[0]
                                        gInSum += sir[1]
                                        bInSum += sir[2]
                                    }
                                    else -> {
                                        rOutSum += sir[0]
                                        gOutSum += sir[1]
                                        bOutSum += sir[2]
                                    }
                                }
                                i++
                            }
                            var x = 0
                            while (x < width) {
                                r[yi] = dv[rSum]
                                g[yi] = dv[gSum]
                                b[yi] = dv[bSum]
                                rSum -= rOutSum
                                gSum -= gOutSum
                                bSum -= bOutSum
                                val stackStart = radius0 - radius0 + div
                                var sir = stack[stackStart % div]
                                rOutSum -= sir[0]
                                gOutSum -= sir[1]
                                bOutSum -= sir[2]
                                if (y == 0) vMin[x] = min(x + radius0 + 1, width - 1)
                                val p = pix[yw + vMin[x]]
                                sir[0] = (p and 0xff0000) shr 16
                                sir[1] = (p and 0x00ff00) shr 8
                                sir[2] = (p and 0x0000ff)
                                rInSum += sir[0]
                                gInSum += sir[1]
                                bInSum += sir[2]
                                rSum += rInSum
                                gSum += gInSum
                                bSum += bInSum
                                val stackPointer = (radius0 + 1) % div
                                sir = stack[(stackPointer) % div]
                                rOutSum += sir[0]
                                gOutSum += sir[1]
                                bOutSum += sir[2]
                                rInSum -= sir[0]
                                gInSum -= sir[1]
                                bInSum -= sir[2]
                                yi++
                                x++
                            }
                            yw += width
                            y++
                        }
                        var x = 0
                        while (x < width) {
                            var rSum = 0
                            var gSum = 0
                            var bSum = 0
                            var rOutSum = 0
                            var gOutSum = 0
                            var bOutSum = 0
                            var rInSum = 0
                            var gInSum = 0
                            var bInSum = 0
                            var yp = -radius0 * width
                            var i = -radius0
                            while (i <= radius0) {
                                yi = max(0, yp) + x
                                val sir = stack[i + radius0]
                                sir[0] = r[yi]
                                sir[1] = g[yi]
                                sir[2] = b[yi]
                                val rbs = radius0 + 1 - abs(i)
                                rSum += r[yi] * rbs
                                gSum += g[yi] * rbs
                                bSum += b[yi] * rbs
                                when {
                                    i > 0 -> {
                                        rInSum += sir[0]
                                        gInSum += sir[1]
                                        bInSum += sir[2]
                                    }
                                    else -> {
                                        rOutSum += sir[0]
                                        gOutSum += sir[1]
                                        bOutSum += sir[2]
                                    }
                                }
                                if (i < height - 1) yp += width
                                i++
                            }
                            yi = x
                            var y = 0
                            while (y < height) {
                                pix[yi] =
                                    (-0x1000000 and pix[yi]) or (dv[rSum] shl 16) or (dv[gSum] shl 8) or dv[bSum]
                                rSum -= rOutSum
                                gSum -= gOutSum
                                bSum -= bOutSum
                                val stackStart = radius0 - radius0 + div
                                var sir = stack[stackStart % div]
                                rOutSum -= sir[0]
                                gOutSum -= sir[1]
                                bOutSum -= sir[2]
                                if (x == 0) vMin[y] = min(y + radius0 + 1, height - 1) * width
                                val p = x + vMin[y]
                                sir[0] = r[p]
                                sir[1] = g[p]
                                sir[2] = b[p]
                                rInSum += sir[0]
                                gInSum += sir[1]
                                bInSum += sir[2]
                                rSum += rInSum
                                gSum += gInSum
                                bSum += bInSum
                                sir = stack[(radius0 + 1) % div]
                                rOutSum += sir[0]
                                gOutSum += sir[1]
                                bOutSum += sir[2]
                                rInSum -= sir[0]
                                gInSum -= sir[1]
                                bInSum -= sir[2]
                                yi += width
                                y++
                            }
                            x++
                        }
                        setPixels(pix, 0, width, 0, 0, width, height)
                    }
                }
            }
        }

    private const val hRadius = 2//水平方向模糊度
    private const val vRadius = 2//竖直方向模糊度
    private const val iterations = 7//模糊迭代度
    fun BoxBlurFilter(src: Bitmap): Bitmap =
        Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888).apply {
            IntArray(width * height).let { inPixels ->
                IntArray(width * height).let { outPixels ->
                    src.getPixels(inPixels, 0, width, 0, 0, width, height)
                    for (i in 0 until iterations) {
                        blur(inPixels, outPixels, width, height, hRadius)
                        blur(outPixels, inPixels, height, width, vRadius)
                    }
                    blurFractional(inPixels, outPixels, width, height, hRadius)
                    blurFractional(outPixels, inPixels, height, width, vRadius)
                    setPixels(inPixels, 0, width, 0, 0, width, height)
                }
            }
        }//高斯模糊

    fun blur(inPixels: IntArray, outPixels: IntArray, width: Int, height: Int, radius: Int) =
        (2 * radius + 1).let { tableSize ->
            IntArray(256 * tableSize).apply {
                for (i in 0 until 256 * tableSize) {
                    this[i] = i / tableSize
                }
            }
        }.let { divide ->
            var inIndex = 0
            for (y in 0 until height) {
                var tableA = 0
                var tableR = 0
                var tableG = 0
                var tableB = 0
                for (i in -radius..radius) {
                    val rgb = inPixels[inIndex + clamp(i, 0, width - 1)]
                    tableA += rgb shr 24 and 0xff
                    tableR += rgb shr 16 and 0xff
                    tableG += rgb shr 8 and 0xff
                    tableB += rgb and 0xff
                }
                var outIndex = y
                for (x in 0 until width) {
                    outPixels[outIndex] =
                        (divide[tableA] shl 24 or (divide[tableR] shl 16) or (divide[tableG] shl 8) or divide[tableB])
                    val rgb1 =
                        inPixels[inIndex + if (x + radius + 1 > width - 1) width - 1 else x + radius + 1]
                    val rgb2 = inPixels[inIndex + if (x - radius < 0) 0 else x - radius]
                    tableA += (rgb1 shr 24 and 0xff) - (rgb2 shr 24 and 0xff)
                    tableR += (rgb1 and 0xff0000) - (rgb2 and 0xff0000) shr 16
                    tableG += (rgb1 and 0xff00) - (rgb2 and 0xff00) shr 8
                    tableB += (rgb1 and 0xff) - (rgb2 and 0xff)
                    outIndex += height
                }
                inIndex += width
            }
        }

    fun clamp(x: Int, a: Int, b: Int): Int = if (x < a) a else if (x > b) b else x
    private fun blurFractional(
        inPixels: IntArray, outPixels: IntArray, width: Int, height: Int, radius: Int
    ) = (1 / (1 + 2 * radius)).let {
        var inIndex = 0
        for (y in 0 until height) {
            outPixels[y] = inPixels[0]
            var outIndex = y + height
            for (x in 1 until width - 1) {
                val i = inIndex + x
                val rgb1 = inPixels[i - 1]
                val rgb2 = inPixels[i]
                val rgb3 = inPixels[i + 1]
                outPixels[outIndex] =
                    ((rgb2 shr 24 and 0xff + (rgb1 shr 24 and 0xff + rgb3 shr 24 and 0xff) * radius) * it shl 24) or
                            ((rgb2 shr 16 and 0xff + (rgb1 shr 16 and 0xff + rgb3 shr 16 and 0xff) * radius) * it shl 16) or
                            ((rgb2 shr 8 and 0xff + (rgb1 shr 8 and 0xff + rgb3 shr 8 and 0xff) * radius) * it shl 8) or
                            ((rgb2 and 0xff + (rgb1 and 0xff + rgb3 and 0xff) * radius) * it)
                outIndex += height
            }
            outPixels[outIndex] = inPixels[width - 1]
            inIndex += width
        }
    }

    @JvmOverloads
    fun saveImage(
        src: Bitmap, filePath: String, format: Bitmap.CompressFormat, recycle: Boolean = false
    ): Boolean = saveImage(src, getFileByPath(filePath), format, recycle)

    @JvmOverloads
    fun saveImage(
        src: Bitmap, file: File?, format: Bitmap.CompressFormat, recycle: Boolean = false
    ): Boolean = when {
        isEmptyBitmap(src) || !createFileNew(file) -> false
        else -> try {
            println("${src.width}, ${src.height}")
            file?.let {
                FileOutputStream(it).use { fileOutputStream ->
                    BufferedOutputStream(fileOutputStream).use { bufferedOutputStream ->
                        src.compress(format, 100, bufferedOutputStream)
                            .apply { if (recycle && !src.isRecycled) src.recycle() }
                    }
                }
            } ?: false
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    fun isImage(file: File?): Boolean =
        file?.run { if (exists()) isImage(path) else false } ?: false

    fun isImage(filePath: String): Boolean = try {
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, this)
        }.run { outWidth != -1 && outHeight != -1 }
    } catch (e: Exception) {
        false
    }//filePath.toUpperCase().run { endsWith(".BMP") || endsWith(".JPG") || endsWith(".JPEG") || endsWith(".PNG") || endsWith(".GIF") }

    fun getImageType(filePath: String?): String? = filePath?.let { getImageType(getFileByPath(it)) }
    fun getImageType(file: File?): String? = file?.let {
        try {
            FileInputStream(it).use { fileInputStream ->
                getImageType(fileInputStream)
                    ?: getFileExtension(it.absolutePath).toUpperCase(Locale.getDefault())
            }
        } catch (e: IOException) {
            e.printStackTrace()
            getFileExtension(it.absolutePath).toUpperCase(Locale.getDefault())
        }
    }

    private fun getImageType(inputStream: InputStream?): String? = inputStream?.use {
        try {
            ByteArray(8).run { if (it.read(this) == -1) null else getImageType(this) }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun getImageType(bytes: ByteArray): String? = when {
        isBMP(bytes) -> "BMP"
        isJPEG(bytes) -> "JPEG"
        isPNG(bytes) -> "PNG"
        isGIF(bytes) -> "GIF"
        else -> null
    }

    private fun isBMP(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0] == 0x42.toByte() && bytes[1] == 0x4d.toByte()

    private fun isJPEG(bytes: ByteArray): Boolean =
        bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()

    private fun isPNG(bytes: ByteArray): Boolean = bytes.size >= 8
            && bytes[0] == 137.toByte() && bytes[1] == 80.toByte() && bytes[2] == 78.toByte() && bytes[3] == 71.toByte()
            && bytes[4] == 13.toByte() && bytes[5] == 10.toByte() && bytes[6] == 26.toByte() && bytes[7] == 10.toByte()

    private fun isGIF(bytes: ByteArray): Boolean = bytes.size >= 6
            && bytes[0] == 'G'.toByte() && bytes[1] == 'I'.toByte() && bytes[2] == 'F'.toByte() && bytes[3] == '8'.toByte()
            && (bytes[4] == '7'.toByte() || bytes[4] == '9'.toByte()) && bytes[5] == 'a'.toByte()

    fun getImageFormat(filePath: String?): String? = when {
        isSpace(filePath) -> null
        else -> BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, this)
        }.outMimeType.let { type ->
            (if (isSpace(type)) "未能识别的图片" else type.substring(6, type.length))
                .apply { debug("image type -> $this") }
        }
    }

    fun getImageFormat(file: File?): String? = file?.absolutePath?.let { path ->
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
        }.outMimeType.let { type ->
            (if (isSpace(type)) "未能识别的图片" else type.substring(6, type.length))
                .apply { debug("image type -> $this") }
        }
    }

    fun getImageFormat(inputStream: InputStream?): String? = inputStream?.let { stream ->
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(stream, null, this)
        }.outMimeType.let { type ->
            (if (isSpace(type)) "未能识别的图片" else type.substring(6, type.length))
                .apply { debug("image type -> $this") }
        }
    }

    @JvmOverloads
    fun getImageFormat(
        bytes: ByteArray?, offset: Int = 0, length: Int = bytes?.size ?: offset
    ): String? = bytes?.let { array ->
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeByteArray(array, offset, length, this)
        }.outMimeType.let { type ->
            (if (isSpace(type)) "未能识别的图片" else type.substring(6, type.length))
                .apply { debug("image type -> $this") }
        }
    }

    fun getImageFormat(resId: Int): String = Resources.getSystem().let { resources ->
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, resId, this)
        }.outMimeType.let { type ->
            (if (isSpace(type)) "未能识别的图片" else type.substring(6, type.length))
                .apply { debug("image type -> $this") }
        }
    }

    fun getImageFormat(fileDescriptor: FileDescriptor?): String? = fileDescriptor?.let { fd ->
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFileDescriptor(fd, null, this)
        }.outMimeType.let { type ->
            (if (isSpace(type)) "未能识别的图片" else type.substring(6, type.length))
                .apply { debug("image type -> $this") }
        }
    }

    fun getThumbVideo(filePath: String, kind: Int): Bitmap? =
        ThumbnailUtils.createVideoThumbnail(filePath, kind)

    fun getThumbBitmap(src: Bitmap, width: Int, height: Int): Bitmap =
        ThumbnailUtils.extractThumbnail(src, width, height)

    @JvmOverloads
    fun compressByScale(
        src: Bitmap, newWidth: Int, newHeight: Int, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> Bitmap.createScaledBitmap(src, newWidth, newHeight, true)
            .apply { if (recycle && !src.isRecycled && this != src) src.recycle() }
    }

    @JvmOverloads
    fun compressByScale(
        src: Bitmap, scaleWidth: Float, scaleHeight: Float, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> Matrix().apply { setScale(scaleWidth, scaleHeight) }.let { matrix ->
            Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
                .apply { if (recycle && !src.isRecycled && this != src) src.recycle() }
        }//Matrix().apply { postScale(scaleWidth / src.width, scaleHeight / src.height) }
    }

    @JvmOverloads
    fun compressByQuality(src: Bitmap, quality: Int, recycle: Boolean = false): Bitmap? = when {
        isEmptyBitmap(src) || quality < 0 || quality > 100 -> null
        else -> ByteArrayOutputStream().use { byteArrayOutputStream ->
            byteArrayOutputStream.apply { src.compress(Bitmap.CompressFormat.JPEG, quality, this) }
                .toByteArray().run {
                    if (recycle && !src.isRecycled) src.recycle()
                    BitmapFactory.decodeByteArray(this, 0, size)
                }
        }
    }

    @JvmOverloads
    fun compressByQuality(
        src: Bitmap, maxByteSize: Long, recycle: Boolean = false, algorithm: String = "default"
    ): Bitmap? = when {
        isEmptyBitmap(src) || maxByteSize <= 0 -> null
        else -> when (algorithm) {
            "default" -> ByteArrayOutputStream().use { byteArrayOutputStream ->
                src.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                when {
                    byteArrayOutputStream.size() <= maxByteSize -> byteArrayOutputStream.toByteArray()
                    else -> {
                        byteArrayOutputStream.reset()
                        src.compress(Bitmap.CompressFormat.JPEG, 0, byteArrayOutputStream)
                        when {
                            byteArrayOutputStream.size() >= maxByteSize -> byteArrayOutputStream.toByteArray()
                            else -> {
                                var start = 0
                                var end = 100
                                var mid = 0
                                loop@ while (start < end) {
                                    mid = (start + end) / 2
                                    byteArrayOutputStream.reset()
                                    src.compress(
                                        Bitmap.CompressFormat.JPEG, mid, byteArrayOutputStream
                                    )
                                    val length = byteArrayOutputStream.size().toLong()
                                    when {
                                        length < maxByteSize -> start = mid + 1
                                        length > maxByteSize -> end = mid - 1
                                        length == maxByteSize -> break@loop
                                    }
                                }
                                if (end == mid - 1) {
                                    byteArrayOutputStream.reset()
                                    src.compress(
                                        Bitmap.CompressFormat.JPEG, start, byteArrayOutputStream
                                    )
                                }
                                byteArrayOutputStream.toByteArray()
                            }
                        }
                    }
                }.run {
                    if (recycle && !src.isRecycled) src.recycle()
                    BitmapFactory.decodeByteArray(this, 0, size)
                }
            }
            else -> ByteArrayOutputStream().use { byteArrayOutputStream ->
                var quality = 100
                src.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
                while (byteArrayOutputStream.toByteArray().size > maxByteSize && quality >= 0) {
                    quality -= 5
                    byteArrayOutputStream.reset()
                    src.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
                }
                when {
                    quality < 0 -> null
                    else -> byteArrayOutputStream.toByteArray().run {
                        if (recycle && !src.isRecycled) src.recycle()
                        BitmapFactory.decodeByteArray(this, 0, size)
                    }
                }
            }
        }
    }

    @JvmOverloads
    fun compressBySampleSize(src: Bitmap, sampleSize: Int, recycle: Boolean = false): Bitmap? =
        when {
            isEmptyBitmap(src) -> null
            else -> ByteArrayOutputStream().use { byteArrayOutputStream ->
                byteArrayOutputStream.apply { src.compress(Bitmap.CompressFormat.JPEG, 100, this) }
                    .toByteArray().run {
                        if (recycle && !src.isRecycled) src.recycle()
                        BitmapFactory.decodeByteArray(this, 0, size,
                            BitmapFactory.Options().apply { inSampleSize = sampleSize })
                    }
            }
        }

    @JvmOverloads
    fun compressBySampleSize(
        src: Bitmap, maxWidth: Int, maxHeight: Int, recycle: Boolean = false
    ): Bitmap? = when {
        isEmptyBitmap(src) -> null
        else -> ByteArrayOutputStream().use { byteArrayOutputStream ->
            byteArrayOutputStream.apply { src.compress(Bitmap.CompressFormat.JPEG, 100, this) }
                .toByteArray().run {
                    BitmapFactory.Options().apply { inJustDecodeBounds = true }.let { options ->
                        BitmapFactory.decodeByteArray(this, 0, size, options)
                        if (recycle && !src.isRecycled) src.recycle()
                        BitmapFactory.decodeByteArray(this, 0, size, options.apply {
                            inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                            inJustDecodeBounds = false
                        })
                    }
                }
        }
    }

    @JvmOverloads
    fun calculateInSampleSize(
        options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int, algorithm: String = "default"
    ): Int = when (algorithm) {
        "default" -> when {
            maxWidth > 0 && maxHeight > 0 -> {
                var inSampleSize = 1
                while (options.outWidth shr 1 >= maxWidth && options.outHeight shr 1 >= maxHeight) {
                    inSampleSize = inSampleSize shl 1
                }
                inSampleSize
            }
            else -> 1
        }
        else -> options.run {
            when {
                maxWidth > 0f && maxHeight > 0f && (outHeight > maxHeight || outWidth > maxWidth) ->
                    (outHeight.toFloat() / maxHeight).roundToInt().let { heightRatio ->
                        (outWidth.toFloat() / maxWidth).roundToInt().let { widthRatio ->
                            if (heightRatio < widthRatio) heightRatio else widthRatio
                        }
                    }
                else -> 1
            }
        }
    }

    fun getBitmapSize(bitmap: Bitmap): Int = bitmap.run {
        when {
            aboveHoneycombMR1 -> byteCount
            aboveKitKat -> allocationByteCount
            else -> rowBytes * height
        }
    }

    fun getBitmapSize(bitmapPath: String): Pair<Int, Int> = getBitmapSize(getFileByPath(bitmapPath))
    fun getBitmapSize(bitmapFile: File?): Pair<Int, Int> = bitmapFile?.let {
        BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(it.absolutePath, this)
        }.run { Pair(outWidth, outHeight) }
    } ?: Pair(0, 0)
}