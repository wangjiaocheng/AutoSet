package top.autoget.autokit

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.core.content.ContextCompat
import top.autoget.autokit.AKit.app
import top.autoget.autokit.LogKit.e
import java.io.File
import java.lang.ref.WeakReference

object SpanKit {
    fun builderSpan(textView: TextView?): SpanBuilder = SpanBuilder(textView)
    class SpanBuilder(private val textView: TextView?) {
        private var mFlag: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE//标识

        @JvmOverloads
        fun setFlag(flag: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE): SpanBuilder =
            apply { mFlag = flag }

        private var mForegroundColor: Int = COLOR_DEFAULT//前景色

        @JvmOverloads
        fun setForegroundColor(@ColorInt foregroundColor: Int = COLOR_DEFAULT): SpanBuilder =
            apply { mForegroundColor = foregroundColor }

        private var mBackgroundColor: Int = COLOR_DEFAULT//背景色

        @JvmOverloads
        fun setBackgroundColor(@ColorInt backgroundColor: Int = COLOR_DEFAULT): SpanBuilder =
            apply { mBackgroundColor = backgroundColor }

        companion object {
            const val ALIGN_BOTTOM = 0
            const val ALIGN_BASELINE = 1
            const val ALIGN_CENTER = 2
            const val ALIGN_TOP = 3
            private const val COLOR_DEFAULT = -0x1000001
        }

        @IntDef(ALIGN_BOTTOM, ALIGN_BASELINE, ALIGN_CENTER, ALIGN_TOP)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Align

        private var mLineHeight: Int = -1
        private var mAlignLine: Int = ALIGN_CENTER

        @JvmOverloads
        fun setLineHeight(lineHeight: Int = -1, @Align alignLine: Int = ALIGN_CENTER): SpanBuilder =
            apply {
                mLineHeight = lineHeight
                mAlignLine = alignLine
            }

        private var mQuoteColor: Int = COLOR_DEFAULT//引用线色
        private var mStripeWidth: Int = 2//1起
        private var mQuoteGapWidth: Int = 2

        @JvmOverloads
        fun setQuoteColor(
            @ColorInt quoteColor: Int = COLOR_DEFAULT, stripeWidth: Int = 2, quoteGapWidth: Int = 2
        ): SpanBuilder = apply {
            mQuoteColor = quoteColor
            mStripeWidth = stripeWidth
            mQuoteGapWidth = quoteGapWidth
        }

        private var mMargin: Int = -1//间距

        @JvmOverloads
        fun setMargin(margin: Int = -1): SpanBuilder = apply {
            mMargin = margin
            mText = " $mText"
        }

        private var mFirst: Int = -1//首行
        private var mRest: Int = -1//余行
        private var isLeadingMargin: Boolean = false//缩进

        @JvmOverloads
        fun setLeadingMargin(first: Int = -1, rest: Int = -1): SpanBuilder = apply {
            mFirst = first
            mRest = rest
            isLeadingMargin = true
        }

        private var mBulletColor: Int = COLOR_DEFAULT
        private var mBulletRadius: Int = 3
        private var mBulletGapWidth: Int = 0//列表标记与文字间距
        private var isBullet: Boolean = false

        @JvmOverloads
        fun setBullet(
            @ColorInt bulletColor: Int = COLOR_DEFAULT, bulletRadius: Int = 3,
            bulletGapWidth: Int = 0
        ): SpanBuilder = apply {
            mBulletColor = bulletColor
            mBulletRadius = bulletRadius
            mBulletGapWidth = bulletGapWidth
            isBullet = true
        }

        private var mFontSize: Int = -1//字号
        private var mFontSizeIsDp: Boolean = false

        @JvmOverloads
        fun setFontSize(fontSize: Int = -1, fontSizeIsDp: Boolean = false): SpanBuilder = apply {
            mFontSize = fontSize
            mFontSizeIsDp = fontSizeIsDp
        }

        private var mYProportion: Float = -1f//字体比例

        @JvmOverloads
        fun setFontProportion(yProportion: Float = -1f): SpanBuilder =
            apply { mYProportion = yProportion }

        private var mXProportion: Float = -1f//字体横向比例

        @JvmOverloads
        fun setFontXProportion(xProportion: Float = -1f): SpanBuilder =
            apply { mXProportion = xProportion }

        private var isStrikeThrough: Boolean = false//删除线
        fun setStrikeThrough(): SpanBuilder = apply { isStrikeThrough = true }
        private var isUnderline: Boolean = false//下划线
        fun setUnderline(): SpanBuilder = apply { isUnderline = true }
        private var isSuperscript: Boolean = false//上标
        fun setSuperscript(): SpanBuilder = apply { isSuperscript = true }
        private var isSubscript: Boolean = false//下标
        fun setSubscript(): SpanBuilder = apply { isSubscript = true }
        private var isBold: Boolean = false//粗体
        fun setBold(): SpanBuilder = apply { isBold = true }
        private var isItalic: Boolean = false//斜体
        fun setItalic(): SpanBuilder = apply { isItalic = true }
        private var isBoldItalic: Boolean = false//粗斜体
        fun setBoldItalic(): SpanBuilder = apply { isBoldItalic = true }
        private var mFontFamily: String? = null//字体系列

        @JvmOverloads
        fun setFontFamily(fontFamily: String? = null): SpanBuilder =
            apply { mFontFamily = fontFamily }

        private var mTypeface: Typeface? = null//字体

        @JvmOverloads
        fun setTypeface(typeface: Typeface? = null): SpanBuilder = apply { mTypeface = typeface }

        private var mHorizontalAlign: Layout.Alignment? = null//水平对齐

        @JvmOverloads
        fun setHorizontalAlign(horizontalAlign: Layout.Alignment? = null): SpanBuilder =
            apply { mHorizontalAlign = horizontalAlign }

        private var mVerticalAlign: Int = -1//垂直对齐

        @JvmOverloads
        fun setVerticalAlign(verticalAlign: Int = -1): SpanBuilder =
            apply { mVerticalAlign = verticalAlign }

        private var mClickableSpan: ClickableSpan? = null//点击时间

        @JvmOverloads
        fun setClickSpan(clickableSpan: ClickableSpan? = null): SpanBuilder = apply {
            if (textView != null && textView.movementMethod == null)
                textView.movementMethod = LinkMovementMethod.getInstance()
            mClickableSpan = clickableSpan
        }

        private var mUrl: String? = null//超链接

        @JvmOverloads
        fun setUrl(url: String? = null): SpanBuilder = apply {
            if (textView != null && textView.movementMethod == null)
                textView.movementMethod = LinkMovementMethod.getInstance()
            mUrl = url
        }

        private var mBlurRadius: Float = -1f//模糊半径
        private var mStyle: BlurMaskFilter.Blur? = null
        private var isBlur: Boolean = false

        @JvmOverloads
        fun setBlur(blurRadius: Float = -1f, style: BlurMaskFilter.Blur? = null): SpanBuilder =
            apply {
                mBlurRadius = blurRadius
                mStyle = style////模糊样式：Blur.NORMAL；Blur.SOLID；Blur.OUTER；Blur.INNER
                isBlur = true
            }//bug：其他地方存在相同字体，出现在之前不会模糊，出现在之后一起模糊，推荐所有字体都模糊

        private var mShader: Shader? = null

        @JvmOverloads
        fun setShader(shader: Shader? = null): SpanBuilder = apply { mShader = shader }

        private var mShadowRadius: Float = -1f
        private var mShadowDx: Float = 0f
        private var mShadowDy: Float = 0f
        private var mShadowColor: Int = 0

        @JvmOverloads
        fun setShadow(
            shadowRadius: Float = -1f, shadowDx: Float = 0f, shadowDy: Float = 0f,
            shadowColor: Int = 0
        ): SpanBuilder = apply {
            mShadowRadius = shadowRadius
            mShadowDx = shadowDx
            mShadowDy = shadowDy
            mShadowColor = shadowColor
        }

        private var mSpans: Array<Any>? = null
        fun setSpans(vararg spans: Any): SpanBuilder =
            apply { if (spans.isNotEmpty()) mSpans = arrayOf(spans) }

        private val mTypeCharSequence = 0
        private var mText: CharSequence = ""

        @JvmOverloads
        fun append(text: CharSequence = ""): SpanBuilder = apply {
            apply(mTypeCharSequence)
            mText = text
        }

        @JvmOverloads
        fun appendLine(text: CharSequence = ""): SpanBuilder = apply {
            apply(mTypeCharSequence)
            mText = "$text${File.separator}"
        }

        private val mTypeImage = 1
        private var mAlignImage: Int = ALIGN_BOTTOM
        private var mBitmap: Bitmap? = null
        private var isBitmap: Boolean = false

        @JvmOverloads
        fun appendImage(imageBitmap: Bitmap?, @Align alignImage: Int = ALIGN_BOTTOM): SpanBuilder =
            apply {
                apply(mTypeImage)
                mAlignImage = alignImage
                mBitmap = imageBitmap
                mText = " $mText"
                isBitmap = true
            }

        private var mDrawable: Drawable? = null
        private var isDrawable: Boolean = false

        @JvmOverloads
        fun appendImage(
            imageDrawable: Drawable?, @Align alignImage: Int = ALIGN_BOTTOM
        ): SpanBuilder = apply {
            apply(mTypeImage)
            mAlignImage = alignImage
            mDrawable = imageDrawable
            mText = " $mText"
            isDrawable = true
        }

        private var mImageUri: Uri? = null
        private var isUri: Boolean = false

        @JvmOverloads
        fun appendImage(imageUri: Uri?, @Align alignImage: Int = ALIGN_BOTTOM): SpanBuilder =
            apply {
                apply(mTypeImage)
                mAlignImage = alignImage
                mImageUri = imageUri
                mText = " $mText"
                isUri = true
            }

        private var mImageResId: Int = -1
        private var isResourceId: Boolean = false

        @JvmOverloads
        fun appendImage(
            @DrawableRes imageResourceId: Int, @Align alignImage: Int = ALIGN_BOTTOM
        ): SpanBuilder = apply {
            apply(mTypeImage)
            mAlignImage = alignImage
            mImageResId = imageResourceId
            mText = " $mText"
            isResourceId = true
        }

        private val mTypeSpace = 2
        private var mSpaceSize: Int = -1
        private var mSpaceColor: Int = Color.TRANSPARENT

        @JvmOverloads
        fun appendSpace(
            spaceSize: Int = -1, @ColorInt spaceColor: Int = Color.TRANSPARENT
        ): SpanBuilder = apply {
            apply(mTypeSpace)
            mSpaceSize = spaceSize
            mSpaceColor = spaceColor
        }

        private var mType: Int = -1
        private fun apply(type: Int = -1) {
            applyLast()
            mType = type
        }

        private fun applyLast() {
            when (mType) {
                mTypeCharSequence -> updateCharSequence()
                mTypeImage -> updateImage()
                mTypeSpace -> updateSpace()
            }
            setDefault()
        }

        private fun updateCharSequence() = mBuilder.run {
            if (mText.isNotEmpty()) {
                if (length == 0 && mLineHeight != -1) append("${2.toChar()}\n")
                    .setSpan(AbsoluteSizeSpan(0), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                length.let { start ->
                    append(mText)
                    if (mForegroundColor != COLOR_DEFAULT)
                        setSpan(ForegroundColorSpan(mForegroundColor), start, length, mFlag)
                    if (mBackgroundColor != COLOR_DEFAULT)
                        setSpan(BackgroundColorSpan(mBackgroundColor), start, length, mFlag)
                    if (mLineHeight != -1)
                        setSpan(SpanLineHeight(mLineHeight, mAlignLine), start, length, mFlag)
                    if (mQuoteColor != COLOR_DEFAULT) setSpan(
                        SpanQuote(mQuoteColor, mStripeWidth, mQuoteGapWidth), start, length, mFlag
                    )
                    if (mFirst != -1)
                        setSpan(LeadingMarginSpan.Standard(mFirst, mRest), start, length, mFlag)
                    if (mMargin != -1) setSpan(SpanMargin(mMargin), start, length, mFlag)
                    if (mBulletColor != COLOR_DEFAULT && isBullet) setSpan(
                        SpanBullet(mBulletColor, mBulletRadius, mBulletGapWidth),
                        start, length, mFlag
                    )
                    if (mFontSize != -1)
                        setSpan(AbsoluteSizeSpan(mFontSize, mFontSizeIsDp), start, length, mFlag)
                    if (mYProportion != -1f)
                        setSpan(RelativeSizeSpan(mYProportion), start, length, mFlag)
                    if (mXProportion != -1f) setSpan(ScaleXSpan(mXProportion), start, length, mFlag)
                    if (isStrikeThrough) setSpan(StrikethroughSpan(), start, length, mFlag)
                    if (isUnderline) setSpan(UnderlineSpan(), start, length, mFlag)
                    if (isSuperscript) setSpan(SuperscriptSpan(), start, length, mFlag)
                    if (isSubscript) setSpan(SubscriptSpan(), start, length, mFlag)
                    if (isBold) setSpan(StyleSpan(Typeface.BOLD), start, length, mFlag)
                    if (isItalic) setSpan(StyleSpan(Typeface.ITALIC), start, length, mFlag)
                    if (isBoldItalic) setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, length, mFlag)
                    mFontFamily?.let { setSpan(TypefaceSpan(it), start, length, mFlag) }
                    mTypeface?.let { setSpan(SpanTypeface(it), start, length, mFlag) }
                    mHorizontalAlign
                        ?.let { setSpan(AlignmentSpan.Standard(it), start, length, mFlag) }
                    if (mVerticalAlign != -1)
                        setSpan(SpanVerticalAlign(mVerticalAlign), start, length, mFlag)
                    mClickableSpan?.let { setSpan(it, start, length, mFlag) }
                    mUrl?.let { setSpan(URLSpan(it), start, length, mFlag) }
                    if (mBlurRadius != -1f) setSpan(
                        MaskFilterSpan(BlurMaskFilter(mBlurRadius, mStyle)), start, length, mFlag
                    )
                    mShader?.let { setSpan(SpanShader(it), start, length, mFlag) }
                    if (mShadowRadius != -1f) setSpan(
                        SpanShadow(mShadowRadius, mShadowDx, mShadowDy, mShadowColor),
                        start, length, mFlag
                    )
                    mSpans?.let {
                        for (span in it) {
                            setSpan(span, start, length, mFlag)
                        }
                    }
                }
            }
        }

        private fun updateImage() = mBuilder.run {
            if (length == 0) append("${2.toChar()}")
            length.let { start ->
                append("<img>")
                mBitmap?.let { setSpan(SpanImage(it, mAlignImage), start, length, mFlag) }
                    ?: mDrawable?.let { setSpan(SpanImage(it, mAlignImage), start, length, mFlag) }
                    ?: mImageUri?.let { setSpan(SpanImage(it, mAlignImage), start, length, mFlag) }
                    ?: if (mImageResId != -1)
                        setSpan(SpanImage(mImageResId, mAlignImage), start, length, mFlag)
            }
        }

        private fun updateSpace() = mBuilder.run {
            length.let { start ->
                append("< >")
                setSpan(SpanSpace(mSpaceSize, mSpaceColor), start, length, mFlag)
            }
        }

        private fun setDefault() {
            mFlag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            mForegroundColor = COLOR_DEFAULT
            mBackgroundColor = COLOR_DEFAULT
            mLineHeight = -1
            mAlignLine = ALIGN_CENTER
            mQuoteColor = COLOR_DEFAULT
            mStripeWidth = 2
            mQuoteGapWidth = 2
            mFirst = -1
            mRest = -1
            mMargin = -1
            isLeadingMargin = false
            mBulletColor = COLOR_DEFAULT
            mBulletRadius = 3
            mBulletGapWidth = 0
            isBullet = false
            mFontSize = -1
            mFontSizeIsDp = false
            mYProportion = -1f
            mXProportion = -1f
            isStrikeThrough = false
            isUnderline = false
            isSuperscript = false
            isSubscript = false
            isBold = false
            isItalic = false
            isBoldItalic = false
            mFontFamily = null
            mTypeface = null
            mHorizontalAlign = null
            mVerticalAlign = -1
            mClickableSpan = null
            mUrl = null
            mBlurRadius = -1f
            mStyle = null
            isBlur = false
            mShader = null
            mShadowRadius = -1f
            mShadowDx = 0f
            mShadowDy = 0f
            mShadowColor = 0
            mSpans = null
            mText = ""
            mAlignImage = ALIGN_BOTTOM
            mBitmap = null
            isBitmap = false
            mDrawable = null
            isDrawable = false
            mImageUri = null
            isUri = false
            mImageResId = -1
            isResourceId = false
            mSpaceSize = -1
            mSpaceColor = Color.TRANSPARENT
            mType = -1
        }

        private val mBuilder: SpannableStringBuilder = SpannableStringBuilder()
        fun create(): SpannableStringBuilder = mBuilder.apply {
            applyLast()
            textView?.text = this
        }
    }

    internal class SpanLineHeight(private val height: Int, private val verticalAlignment: Int) :
        LineHeightSpan {
        companion object {
            var fontMetricsInt: Paint.FontMetricsInt? = null
            const val ALIGN_CENTER = 2
            const val ALIGN_TOP = 3
        }

        override fun chooseHeight(
            text: CharSequence, start: Int, end: Int, spanstartv: Int, lineHeight: Int,
            fm: Paint.FontMetricsInt
        ) {
            e(fm, fontMetricsInt as Paint.FontMetricsInt)
            fontMetricsInt?.let {
                fm.apply {
                    top = it.top
                    ascent = it.ascent
                    descent = it.descent
                    bottom = it.bottom
                    leading = it.leading
                }
            } ?: run {
                fontMetricsInt = Paint.FontMetricsInt().apply {
                    top = fm.top
                    ascent = fm.ascent
                    descent = fm.descent
                    bottom = fm.bottom
                    leading = fm.leading
                }
            }
            (height - (lineHeight + fm.descent - fm.ascent - spanstartv)).let { need ->
                if (need > 0) when (verticalAlignment) {
                    ALIGN_CENTER -> fm.apply {
                        descent += need / 2
                        ascent -= need / 2
                    }
                    ALIGN_TOP -> fm.descent += need
                    else -> fm.ascent -= need
                }
            }
            (height - (lineHeight + fm.bottom - fm.top - spanstartv)).let { need ->
                if (need > 0) when (verticalAlignment) {
                    ALIGN_CENTER -> fm.apply {
                        bottom += need / 2
                        top -= need / 2
                    }
                    ALIGN_TOP -> fm.bottom += need
                    else -> fm.top -= need
                }
            }
            if ((text as Spanned).getSpanEnd(this) == end) fontMetricsInt = null
            e(fm, fontMetricsInt as Paint.FontMetricsInt)
        }
    }

    internal class SpanQuote constructor(
        private val colorQuote: Int, private val stripeWidth: Int, private val gapWidth: Int
    ) : LeadingMarginSpan {
        override fun getLeadingMargin(first: Boolean): Int = stripeWidth + gapWidth
        override fun drawLeadingMargin(
            c: Canvas, p: Paint, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int,
            text: CharSequence, start: Int, end: Int, first: Boolean, layout: Layout
        ) {
            val styleTemp = p.style
            val colorTemp = p.color
            c.drawRect(
                x.toFloat(), top.toFloat(), (x + dir * stripeWidth).toFloat(), bottom.toFloat(),
                p.apply {
                    style = Paint.Style.FILL
                    color = colorQuote
                })
            p.apply {
                style = styleTemp
                color = colorTemp
            }
        }
    }

    internal class SpanMargin constructor(private val margin: Int) : ReplacementSpan() {
        override fun getSize(
            paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?
        ): Int = margin

        override fun draw(
            canvas: Canvas, text: CharSequence, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) {
        }
    }

    internal class SpanBullet
    constructor(private val colorBullet: Int, private val radius: Int, private val gapWidth: Int) :
        LeadingMarginSpan {
        override fun getLeadingMargin(first: Boolean): Int = 2 * radius + gapWidth
        private var mBulletPath: Path? = null
        override fun drawLeadingMargin(
            c: Canvas, p: Paint, x: Int, dir: Int, top: Int, baseline: Int, bottom: Int,
            text: CharSequence, start: Int, end: Int, first: Boolean, l: Layout
        ) {
            if ((text as Spanned).getSpanStart(this) == start) {
                val styleTemp = p.style
                val colorTemp = p.color
                p.apply {
                    style = Paint.Style.FILL
                    color = colorBullet
                }
                c.run {
                    when {
                        isHardwareAccelerated -> {
                            if (mBulletPath == null) {
                                mBulletPath = Path()
                                mBulletPath?.addCircle(0f, 0f, radius.toFloat(), Path.Direction.CW)
                            }
                            save()
                            translate((x + dir * radius).toFloat(), (top + bottom) / 2f)
                            mBulletPath?.let { drawPath(it, p) }
                            restore()
                        }
                        else -> drawCircle(
                            (x + dir * radius).toFloat(), (top + bottom) / 2f, radius.toFloat(), p
                        )
                    }
                }
                p.apply {
                    color = colorTemp
                    style = styleTemp
                }
            }
        }
    }

    internal class SpanTypeface constructor(private val newType: Typeface) : TypefaceSpan("") {
        override fun updateDrawState(textPaint: TextPaint) = apply(textPaint, newType)
        override fun updateMeasureState(textPaint: TextPaint) = apply(textPaint, newType)
        private fun apply(paint: Paint, type: Typeface) = paint.run {
            ((typeface?.style ?: 0) and type.style.inv()).let { fake ->
                if (fake and Typeface.BOLD != 0) isFakeBoldText = true
                if (fake and Typeface.ITALIC != 0) textSkewX = -0.25f
            }
            typeface = type
        }
    }

    internal class SpanVerticalAlign(private val verticalAlignment: Int) : ReplacementSpan() {
        companion object {
            const val ALIGN_CENTER = 2
            const val ALIGN_TOP = 3
        }

        override fun getSize(
            paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?
        ): Int = paint.measureText(text.subSequence(start, end).toString()).toInt()

        override fun draw(
            canvas: Canvas, text: CharSequence, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) = paint.fontMetricsInt.let { fm ->
            canvas.drawText(
                text.subSequence(start, end).toString(), x,
                (y - ((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2)).toFloat(), paint
            )
        }
    }

    internal class SpanShader constructor(private val mShader: Shader) :
        CharacterStyle(), UpdateAppearance {
        override fun updateDrawState(textPaint: TextPaint) = textPaint.run { shader = mShader }
    }

    internal class SpanShadow constructor(
        private val radius: Float, private val dx: Float, private val dy: Float,
        private val shadowColor: Int
    ) : CharacterStyle(), UpdateAppearance {
        override fun updateDrawState(textPaint: TextPaint) =
            textPaint.setShadowLayer(radius, dx, dy, shadowColor)
    }

    internal abstract class SpanDrawable
    @JvmOverloads constructor(verticalAlignment: Int = ALIGN_BOTTOM) : ReplacementSpan() {
        companion object {
            const val ALIGN_BOTTOM = 0
            const val ALIGN_BASELINE = 1
            const val ALIGN_CENTER = 2
            const val ALIGN_TOP = 3
        }

        abstract val drawable: Drawable?
        private var mDrawableRef: WeakReference<Drawable>? = null
        private val cachedDrawable: Drawable?
            get() = (mDrawableRef?.get() ?: drawable)?.apply { mDrawableRef = WeakReference(this) }
        private val mVerticalAlignment: Int = verticalAlignment
        override fun getSize(
            paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?
        ): Int = cachedDrawable?.bounds?.apply {
            fm?.apply {
                (bottom - top).let { lineHeight ->
                    if (lineHeight < height()) {
                        when (mVerticalAlignment) {
                            ALIGN_CENTER -> {
                                top = -height() / 2 - lineHeight / 4
                                bottom = height() / 2 - lineHeight / 4
                            }
                            ALIGN_TOP -> bottom = height() + top
                            else -> top = -height() + bottom
                        }
                        ascent = top
                        descent = bottom
                    }
                }
/*                (paint.fontMetrics.descent - paint.fontMetrics.ascent).toInt().let { fontHeight ->
                    if (fontHeight < height()) when (mVerticalAlignment) {
                        ALIGN_BOTTOM -> ascent -= height() - fontHeight
                        ALIGN_BASELINE -> ascent -= height() - fontHeight + descent
                        ALIGN_CENTER -> {
                            ascent -= (height() - fontHeight) / 2
                            descent += (height() - fontHeight) / 2
                        }
                        ALIGN_TOP -> descent += height() - fontHeight
                    }
                }*/
            }
        }?.right ?: 0

        override fun draw(
            canvas: Canvas, text: CharSequence, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) = canvas.run {
            save()
            cachedDrawable?.bounds?.let { rect ->
                translate(
                    x, when {
                        rect.height() < bottom - top -> when (mVerticalAlignment) {
                            ALIGN_BOTTOM -> bottom - rect.height()
                            ALIGN_BASELINE -> y - rect.height()
                            ALIGN_CENTER -> (bottom + top - rect.height()) / 2
                            else -> top
                        }
                        else -> top
                    }.toFloat()
                )
            }
/*            cachedDrawable?.bounds?.let { rect ->
                var transY: Float = (bottom - rect.bottom).toFloat()
                (paint.fontMetrics.descent - paint.fontMetrics.ascent).let { fontHeight ->
                    when {
                        rect.height() < fontHeight -> when (mVerticalAlignment) {
                            ALIGN_BASELINE -> transY -= paint.fontMetricsInt.descent
                            ALIGN_CENTER -> transY -= (fontHeight - rect.height()) / 2
                            ALIGN_TOP -> transY -= fontHeight - rect.height()
                        }
                        else -> if (mVerticalAlignment == ALIGN_BASELINE) transY -= paint.fontMetricsInt.descent
                    }
                }
                translate(x, transY)
            }*/
            cachedDrawable?.draw(this)
            restore()
        }
    }

    internal class SpanImage : SpanDrawable, LoggerKit {
        private var mDrawable: Drawable? = null
        private var mContentUri: Uri? = null
        private var mResourceId: Int = 0
        override val drawable: Drawable?
            get() = mDrawable ?: mContentUri?.let {
                try {
                    app.contentResolver.openInputStream(it)?.use { stream ->
                        BitmapDrawable(Resources.getSystem(), BitmapFactory.decodeStream(stream))
                            .apply { setBounds(0, 0, intrinsicWidth, intrinsicHeight) }
                    }
                } catch (e: Exception) {
                    null.apply { error("$loggerTag->Failed to loaded content $it") }
                }
            } ?: try {
                ContextCompat.getDrawable(app, mResourceId)
                    ?.apply { setBounds(0, 0, intrinsicWidth, intrinsicHeight) }
            } catch (e: Exception) {
                null.apply { error("$loggerTag->Unable to find resource: $mResourceId") }
            }

        constructor(bitmap: Bitmap?, verticalAlignment: Int) : super(verticalAlignment) {
            mDrawable = bitmap?.let {
                BitmapDrawable(Resources.getSystem(), it)
                    .apply { setBounds(0, 0, intrinsicWidth, intrinsicHeight) }
            }
        }

        constructor(drawable: Drawable?, verticalAlignment: Int) : super(verticalAlignment) {
            mDrawable = drawable?.apply { setBounds(0, 0, intrinsicWidth, intrinsicHeight) }
        }

        constructor(uri: Uri?, verticalAlignment: Int) : super(verticalAlignment) {
            mContentUri = uri
        }

        constructor(@DrawableRes resourceId: Int, verticalAlignment: Int) :
                super(verticalAlignment) {
            mResourceId = resourceId
        }
    }

    internal class SpanSpace
    constructor(private val width: Int, private val colorSpan: Int = Color.TRANSPARENT) :
        ReplacementSpan() {
        override fun getSize(
            paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?
        ): Int = width

        override fun draw(
            canvas: Canvas, text: CharSequence, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) {
            val styleTemp = paint.style
            val colorTemp = paint.color
            canvas.drawRect(x, top.toFloat(), x + width, bottom.toFloat(), paint.apply {
                style = Paint.Style.FILL
                color = colorSpan
            })
            paint.apply {
                style = styleTemp
                color = colorTemp
            }
        }
    }
}