package top.autoget.autosee

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.SparseIntArray
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import top.autoget.autokit.InputMethodKit.hideInputMethod
import kotlin.math.roundToInt

class TextAutoZoom
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    AppCompatEditText(context, attrs, defStyle) {
    companion object {
        fun setNormalization(activity: Activity, rootView: View, textAutoZoom: TextAutoZoom) {
            if (rootView !is TextAutoZoom) rootView.setOnTouchListener { _, _ ->
                hideInputMethod(activity)
                if (textAutoZoom.textSize < textAutoZoom.minTextSize)
                    textAutoZoom.setText(textAutoZoom.text.toString().replace("\n", ""))
                false
            }
            if (rootView is ViewGroup) for (i in 0 until rootView.childCount) {
                setNormalization(activity, rootView.getChildAt(i), textAutoZoom)
            }
        }

        private const val NO_LINE_LIMIT = -1
    }

    private var maxLines: Int = NO_LINE_LIMIT
    override fun getMaxLines(): Int = maxLines
    override fun setMaxLines(maxlines: Int) {
        super.setMaxLines(maxlines)
        maxLines = maxlines
        adjustTextSize()
    }

    private var widthLimit = 0
    private var initialized = true
    var minTextSize =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, resources.displayMetrics)
        set(minTextSize) {
            field = minTextSize
            adjustTextSize()
        }

    private interface SizeTester {
        fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int
    }

    private val sizeTester = object : SizeTester {
        val textRect = RectF()
        override fun onTestSize(suggestedSize: Int, availableSpace: RectF): Int {
            textPaint.textSize = suggestedSize.toFloat()
            when (getMaxLines()) {
                1 -> textRect.apply {
                    right = textPaint.measureText(text.toString())
                    bottom = textPaint.fontSpacing
                }
                else -> StaticLayout(
                    text, textPaint, widthLimit, Layout.Alignment.ALIGN_NORMAL,
                    spacingMult, spacingAdd, true
                ).let { staticLayout ->
                    when {
                        getMaxLines() == NO_LINE_LIMIT || staticLayout.lineCount <= getMaxLines() -> textRect.apply {
                            var maxWidth = -1f
                            for (i in 0 until staticLayout.lineCount) {
                                if (maxWidth < staticLayout.getLineWidth(i))
                                    maxWidth = staticLayout.getLineWidth(i)
                            }
                            right = maxWidth
                            bottom = staticLayout.height.toFloat()
                        }
                        else -> return 1
                    }
                }
            }
            return if (availableSpace.contains(textRect.apply { offsetTo(0f, 0f) })) -1 else 1
        }
    }
    private val availableSpaceRect = RectF()
    private fun adjustTextSize() {
        widthLimit = measuredWidth - compoundPaddingLeft - compoundPaddingRight
        if (initialized && widthLimit > 0)
            super.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                efficientTextSizeSearch(minTextSize.roundToInt(), maxTextSize.toInt(), sizeTester,
                    availableSpaceRect.apply {
                        right = widthLimit.toFloat()
                        bottom =
                            (measuredHeight - compoundPaddingBottom - compoundPaddingTop).toFloat()
                    }).toFloat()
            )
    }

    var enableSizeCache = true
        set(enableSizeCache) {
            field = enableSizeCache
            textCachedSizes.clear()
            adjustTextSize()
        }
    private val textCachedSizes = SparseIntArray()
    private fun efficientTextSizeSearch(
        start: Int, end: Int, sizeTester: SizeTester, availableSpace: RectF
    ): Int = (text?.length ?: 0).let {
        when {
            enableSizeCache -> textCachedSizes.get(it).let { size ->
                when (size) {
                    0 -> binarySearch(start, end, sizeTester, availableSpace)
                        .apply { textCachedSizes.put(it, this) }
                    else -> size
                }
            }
            else -> binarySearch(start, end, sizeTester, availableSpace)
        }
    }

    private fun binarySearch(
        start: Int, end: Int, sizeTester: SizeTester, availableSpace: RectF
    ): Int {
        var lastBest = start
        var startTemp = start
        var endTemp = end - 1
        var mid: Int
        while (startTemp <= endTemp) {
            mid = (startTemp + endTemp).ushr(1)
            sizeTester.onTestSize(mid, availableSpace).let { midValCmp ->
                when {
                    midValCmp < 0 -> {
                        lastBest = startTemp
                        startTemp = mid + 1
                    }
                    midValCmp > 0 -> {
                        endTemp = mid - 1
                        lastBest = endTemp
                    }
                    else -> return mid
                }
            }
        }
        return lastBest
    }

    override fun setLines(lines: Int) {
        super.setLines(lines)
        maxLines = lines
        adjustTextSize()
    }

    override fun setSingleLine() {
        super.setSingleLine()
        maxLines = 1
        adjustTextSize()
    }

    override fun setSingleLine(singleLine: Boolean) {
        super.setSingleLine(singleLine)
        maxLines = if (singleLine) 1 else NO_LINE_LIMIT
        adjustTextSize()
    }

    private var spacingMult = 1.0f
    private var spacingAdd = 0.0f
    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        spacingMult = mult
        spacingAdd = add
    }

    private var textPaint = TextPaint(paint)
    override fun setTypeface(tf: Typeface?) {
        super.setTypeface(tf)
        textPaint.apply { typeface = tf }
    }

    private var maxTextSize: Float = textSize
    override fun setTextSize(size: Float) {
        maxTextSize = size
        textCachedSizes.clear()
        adjustTextSize()
    }

    override fun setTextSize(unit: Int, size: Float) {
        maxTextSize = TypedValue.applyDimension(
            unit, size, (context?.resources ?: Resources.getSystem()).displayMetrics
        )
        textCachedSizes.clear()
        adjustTextSize()
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, after: Int) {
        super.onTextChanged(text, start, before, after)
        adjustTextSize()
    }

    override fun onSizeChanged(width: Int, height: Int, oldwidth: Int, oldheight: Int) {
        super.onSizeChanged(width, height, oldwidth, oldheight)
        if (width != oldwidth || height != oldheight) adjustTextSize()
        textCachedSizes.clear()
    }
}