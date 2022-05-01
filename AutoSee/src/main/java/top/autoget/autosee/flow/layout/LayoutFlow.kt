package top.autoget.autosee.flow.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import top.autoget.autosee.flow.common.ConstantsFlow
import kotlin.math.max
import kotlin.math.min

open class LayoutFlow//流式布局：只测量子控件
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ViewGroup(context, attrs, defStyleAttr) {
    open var isLabelFlow: Boolean = true
    var tabOrientation = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        when {
            isLabelFlow -> measureLabelVertical(widthMeasureSpec, heightMeasureSpec)
            else -> when (tabOrientation) {
                ConstantsFlow.HORIZONTAL ->
                    measureTabHorizontal(widthMeasureSpec, heightMeasureSpec)
                else -> measureTabVertical(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    private val mAllViews: MutableList<MutableList<View?>?>? = mutableListOf()
    private var mLineHeights: MutableList<Int?>? = mutableListOf()
    var labelLines = -1
    var isLabelMoreLine = false
    protected var mLineWidth = 0
    protected var mViewHeight = 0
    private fun measureLabelVertical(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        mAllViews?.clear()////可能添加多次，所以需要清掉
        mLineHeights?.clear()
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var lineWidth = 0
        var lineHeight = 0
        var viewGroupHeight = 0
        var lineViews: MutableList<View?> = mutableListOf()
        loop@ for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            val params = child.layoutParams as MarginLayoutParams
            val cWidth = child.measuredWidth + params.leftMargin + params.rightMargin
            val cHeight = child.measuredHeight + params.topMargin + params.bottomMargin
            when {
                lineWidth + cWidth > widthSize - (paddingLeft + paddingRight) -> {
                    viewGroupHeight += lineHeight
                    mLineHeights?.add(lineHeight)
                    mAllViews?.add(lineViews)
                    lineViews = mutableListOf<View?>().apply { add(child) }
                    lineWidth = cWidth//重置为下一个child的宽度
                    lineHeight = cHeight
                    if (labelLines != -1 && mLineHeights?.size ?: 0 >= labelLines) {
                        isLabelMoreLine = true
                        break@loop
                    } else isLabelMoreLine = false//是否设置了显示行数
                }//换行
                else -> {
                    lineWidth += cWidth
                    lineHeight = max(lineHeight, cHeight)
                    mLineWidth = max(mLineWidth, lineWidth)
                    lineViews.add(child)
                }//未换
            }//确定是否换行
            if (i == childCount - 1) {
                viewGroupHeight += lineHeight
                mLineHeights?.add(lineHeight)
                mAllViews?.add(lineViews)
            }//加上最后一行
        }
        when (heightMode) {
            MeasureSpec.EXACTLY -> viewGroupHeight = heightSize
            MeasureSpec.AT_MOST -> {
                viewGroupHeight = min(viewGroupHeight, heightSize)
                viewGroupHeight += paddingTop + paddingBottom
            }
            else -> viewGroupHeight += paddingTop + paddingBottom//UNSPECIFIED
        }
        mViewHeight = viewGroupHeight
        setMeasuredDimension(widthSize, viewGroupHeight)//测量完成的高，重设给父控件
    }

    var visualCount = -1
    protected var mViewWidth = 0
    private fun measureTabHorizontal(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthPixels =
            if (MeasureSpec.EXACTLY == widthMode) widthSize else resources.displayMetrics.widthPixels
        var width = 0
        var height = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            val params = child.layoutParams as MarginLayoutParams
            var cw: Int
            when (visualCount) {
                -1 -> {
                    cw = child.measuredWidth + params.leftMargin + params.rightMargin
                    width += cw
                }
                else -> {
                    cw = widthPixels / visualCount
                    params.width = cw
                    child.layoutParams = params
                    width = widthPixels
                }
            }
            val ch = child.measuredHeight + params.topMargin + params.bottomMargin
            height = max(height, ch)
        }
        width =
            if (widthMode == MeasureSpec.EXACTLY) widthSize else width + paddingLeft + paddingRight
        mViewWidth = width
        height =
            if (heightMode == MeasureSpec.EXACTLY) heightSize else height + paddingTop + paddingBottom
        setMeasuredDimension(width, height)
    }

    private fun measureTabVertical(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var width = 0
        var height = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
            val params = child.layoutParams as MarginLayoutParams
            val cw = child.measuredWidth + params.leftMargin + params.rightMargin
            val ch = child.measuredHeight + params.topMargin + params.bottomMargin
            height += ch
            width = max(width, cw)
        }
        when (MeasureSpec.EXACTLY) {
            widthMode -> width = widthSize
            else -> width += paddingLeft + paddingRight
        }
        when (MeasureSpec.EXACTLY) {
            heightMode -> height = heightSize
            else -> height += paddingTop + paddingBottom
        }
        mViewHeight = height
        setMeasuredDimension(width, height)
    }

    val isVertical: Boolean = tabOrientation == ConstantsFlow.VERTICAL
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        when {
            isLabelFlow -> {
                var left = paddingLeft
                var top = paddingTop
                for (i in 0 until (mAllViews?.size ?: 0)) {
                    val lineViews = mAllViews?.get(i)
                    for (j in 0 until (lineViews?.size ?: 0)) {
                        val child = lineViews?.get(j)
                        val params = child?.layoutParams as MarginLayoutParams
                        val cWidth = child.measuredWidth
                        val cHeight = child.measuredHeight
                        val cl = left + params.leftMargin
                        val ct = top + params.topMargin
                        val cr = cl + cWidth
                        val cb = ct + cHeight
                        child.layout(cl, ct, cr, cb)
                        left += cWidth + params.leftMargin + params.rightMargin
                    }
                    left = paddingLeft
                    top += mLineHeights?.get(i) ?: 0
                }
            }
            else -> {
                var left = paddingLeft
                var top = paddingTop
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    val params = child.layoutParams as MarginLayoutParams
                    val cl: Int =
                        if (isVertical) (width - child.measuredWidth) / 2 else left + params.leftMargin
                    val ct = top + params.topMargin
                    val cr = cl + child.measuredWidth
                    val cb = ct + child.measuredHeight
                    child.layout(cl, ct, cr, cb)
                    when (tabOrientation) {
                        ConstantsFlow.HORIZONTAL -> left += child.measuredWidth + params.leftMargin + params.rightMargin
                        else -> top += child.measuredHeight + params.topMargin + params.bottomMargin
                    }//下个控件起始位置
                }
            }
        }
    }

    val isVerticalMove: Boolean = isVertical || isLabelFlow
    override fun generateDefaultLayoutParams(): LayoutParams? =
        MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

    override fun generateLayoutParams(p: LayoutParams?): LayoutParams? = MarginLayoutParams(p)
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams? =
        MarginLayoutParams(context, attrs)

    override fun checkLayoutParams(p: LayoutParams?): Boolean = p is MarginLayoutParams
}