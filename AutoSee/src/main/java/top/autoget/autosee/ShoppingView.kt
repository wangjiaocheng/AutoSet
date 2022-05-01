package top.autoget.autosee

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import top.autoget.autokit.DensityKit.sp2px
import top.autoget.autokit.StringKit.isSpace
import kotlin.math.ceil
import kotlin.math.pow

class ShoppingView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {
    init {
        init(attrs)
    }

    private var mDuration: Int = 0//动画时长
    private var shoppingText: String = "加入购物车"//展示文案
    private var paintBg = Paint()
    private var paintMinus = Paint()
    private var paintText = Paint()
    private var paintNum = Paint()
    private var maxHeight: Int = 0
    private var maxWidth: Int = 0
    private var minusBtnPosition = 0
    private var textPosition = 0
    private fun init(attrs: AttributeSet?) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ShoppingView)
        mDuration = typeArray.getInt(R.styleable.ShoppingView_sv_duration, 250)
        shoppingText = typeArray.getString(R.styleable.ShoppingView_sv_text)
            ?.let { if (isSpace(it)) shoppingText else it } ?: shoppingText
        val size = typeArray
            .getDimension(R.styleable.ShoppingView_sv_text_size, sp2px(16f).toFloat()).toInt()
        val bgColor = typeArray.getColor(
            R.styleable.ShoppingView_sv_bg_color,
            ContextCompat.getColor(context, R.color.deepPurple_A200)
        )
        typeArray.recycle()
        paintBg.apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = bgColor
        }
        paintMinus.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = bgColor
            strokeWidth = (size / 6).toFloat()
        }
        paintText.apply {
            isAntiAlias = true
            color = Color.WHITE
            strokeWidth = (size / 6).toFloat()
            textSize = size.toFloat()
        }
        paintNum.apply {
            isAntiAlias = true
            color = Color.BLACK
            strokeWidth = (size / 6).toFloat()
            textSize = (size / 3 * 4).toFloat()
        }
        maxHeight = size * 2
        maxWidth = (getTextWidth(shoppingText, paintText) / 5 * 8)
            .let { if (it / maxHeight.toFloat() < 3.5) (maxHeight * 3.5).toInt() else it }
        minusBtnPosition = maxHeight / 2
        textPosition = maxWidth / 2
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        setMeasuredDimension(maxWidth, maxHeight)

    companion object {
        private const val STATE_NONE = 0
        private const val STATE_MOVE = 1
        private const val STATE_MOVE_OVER = 2
        private const val STATE_ROTATE = 3
        private const val STATE_ROTATE_OVER = 4
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (state) {
            STATE_NONE -> {
                drawBgMove(canvas)
                drawShoppingText(canvas)
            }
            STATE_MOVE -> drawBgMove(canvas)
            STATE_MOVE_OVER -> {
                state = STATE_ROTATE
                when {
                    isLowToUp -> {
                        drawAddBtn(canvas)
                        startRotateAnim()
                    }
                    else -> {
                        drawBgMove(canvas)
                        drawShoppingText(canvas)
                        state = STATE_NONE
                        isLowToUp = true
                        num = 0
                    }
                }
            }
            STATE_ROTATE -> {
                paintMinus.alpha = alpha
                paintNum.alpha = alpha
                drawMinusBtn(canvas, angle.toFloat())
                drawNumText(canvas)
                drawAddBtn(canvas)
            }
            STATE_ROTATE_OVER -> {
                drawMinusBtn(canvas, angle.toFloat())
                drawNumText(canvas)
                drawAddBtn(canvas)
                if (!isLowToUp) startMoveAnim()
            }
        }
    }

    private fun drawBgMove(canvas: Canvas) = canvas.run {
        drawArc(
            RectF(mWidth.toFloat(), 0f, (mWidth + maxHeight).toFloat(), maxHeight.toFloat()),
            90f, 180f, false, paintBg
        )
        drawRect(
            RectF(
                (mWidth + maxHeight / 2).toFloat(), 0f,
                (maxWidth - maxHeight / 2).toFloat(), maxHeight.toFloat()
            ), paintBg
        )
        drawArc(
            RectF(
                (maxWidth - maxHeight).toFloat(), 0f, maxWidth.toFloat(), maxHeight.toFloat()
            ), 180f, 270f, false, paintBg
        )
    }//绘制移动背景

    private fun drawShoppingText(canvas: Canvas) = canvas.drawText(
        shoppingText, maxWidth / 2 - getTextWidth(shoppingText, paintText) / 2f,
        maxHeight / 2 + getTextHeight(shoppingText, paintText) / 2f, paintText
    )//绘制购物车文案

    private fun drawAddBtn(canvas: Canvas) = canvas.run {
        drawCircle(
            (maxWidth - maxHeight / 2).toFloat(), (maxHeight / 2).toFloat(),
            (maxHeight / 2).toFloat(), paintBg
        )
        drawLine(
            (maxWidth - maxHeight / 2).toFloat(), (maxHeight / 4).toFloat(),
            (maxWidth - maxHeight / 2).toFloat(), (maxHeight / 4 * 3).toFloat(), paintText
        )
        drawLine(
            (maxWidth - maxHeight / 2 - maxHeight / 4).toFloat(), (maxHeight / 2).toFloat(),
            (maxWidth - maxHeight / 4).toFloat(), (maxHeight / 2).toFloat(), paintText
        )
    }//绘制加号按钮

    private fun drawMinusBtn(canvas: Canvas, angle: Float) = canvas.run {
        if (angle != 0f) rotate(angle, minusBtnPosition.toFloat(), (maxHeight / 2).toFloat())
        drawCircle(
            minusBtnPosition.toFloat(), (maxHeight / 2).toFloat(),
            (maxHeight / 2 - maxHeight / 20).toFloat(), paintMinus
        )
        drawLine(
            (minusBtnPosition - maxHeight / 4).toFloat(), (maxHeight / 2).toFloat(),
            (minusBtnPosition + maxHeight / 4).toFloat(), (maxHeight / 2).toFloat(), paintMinus
        )
        if (angle != 0f) rotate(-angle, minusBtnPosition.toFloat(), (maxHeight / 2).toFloat())
    }//绘制减号按钮

    private fun drawNumText(canvas: Canvas) = drawText(
        canvas, num.toString(),
        textPosition - getTextWidth(num.toString(), paintNum) / 2f,
        maxHeight / 2 + getTextHeight(num.toString(), paintNum) / 2f,
        paintNum, angle.toFloat()
    )//绘制购买数量

    private fun getTextWidth(str: String?, paint: Paint?): Int = str?.run {
        if (isNotEmpty()) FloatArray(length)
            .apply { paint?.getTextWidths(str, this) }.let { widths ->
                var width = 0
                for (i in 0 until length) {
                    width += ceil(widths[i].toDouble()).toInt()
                }
                width
            } else 0
    } ?: 0

    private fun getTextHeight(str: String, paint: Paint): Int =
        (Rect().apply { paint.getTextBounds(str, 0, str.length, this) }.height() / 33f * 29).toInt()

    private fun drawText(
        canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, angle: Float
    ) = canvas.run {
        if (angle != 0f) rotate(angle, x, y)
        drawText(text, x, y, paint)
        if (angle != 0f) rotate(-angle, x, y)
    }//绘制Text带角度

    private var state = STATE_NONE//当前状态
    private var isLowToUp = true//是否从小到大，对应从大到小
    private var mWidth = 0
    private fun startMoveAnim() {
        state = STATE_MOVE
        ValueAnimator().apply {
            when {
                isLowToUp -> setIntValues(0, maxWidth - maxHeight)
                else -> setIntValues(maxWidth - maxHeight, 0)
            }
            duration = mDuration.toLong()
            addUpdateListener { animator ->
                mWidth = animator.animatedValue as Int
                when {
                    isLowToUp -> if (mWidth == maxWidth - maxHeight) state = STATE_MOVE_OVER
                    else -> if (mWidth == 0) state = STATE_MOVE_OVER
                }
                invalidate()
            }
        }.start()
    }//开始移动动画

    private var num = 0//购买数量

    interface OnShoppingClickListener {
        fun onAddClick(num: Int)
        fun onMinusClick(num: Int)
    }

    var onShoppingClickListener: OnShoppingClickListener? = null
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (state) {
                    STATE_NONE -> {
                        ++num
                        startMoveAnim()
                        onShoppingClickListener?.onAddClick(num)
                    }
                    STATE_ROTATE_OVER -> when {
                        isPointInCircle(
                            PointF(event.x, event.y),
                            PointF((maxWidth - maxHeight / 2).toFloat(), (maxHeight / 2).toFloat()),
                            (maxHeight / 2).toFloat()
                        ) -> {
                            if (num > 0) {
                                ++num
                                isLowToUp = true
                                onShoppingClickListener?.onAddClick(num)
                            }
                            invalidate()
                        }
                        isPointInCircle(
                            PointF(event.x, event.y),
                            PointF((maxHeight / 2).toFloat(), (maxHeight / 2).toFloat()),
                            (maxHeight / 2).toFloat()
                        ) -> when {
                            num > 1 -> {
                                --num
                                onShoppingClickListener?.onMinusClick(num)
                                invalidate()
                            }
                            else -> {
                                onShoppingClickListener?.onMinusClick(0)
                                state = STATE_ROTATE
                                isLowToUp = false
                                startRotateAnim()
                            }
                        }
                    }
                }
                return true
            }
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isPointInCircle(pointF: PointF, circle: PointF, radius: Float): Boolean =
        (pointF.x - circle.x).toDouble().pow(2.0) + (pointF.y - circle.y).toDouble().pow(2.0) <=
                radius.toDouble().pow(2.0)

    private var angle = 0
    private var alpha = 0
    private fun startRotateAnim() {
        val animatorTextRotate: ValueAnimator = ValueAnimator().apply {
            if (isLowToUp) setIntValues(0, 360) else setIntValues(360, 0)
            duration = mDuration.toLong()
            addUpdateListener { valueAnimator ->
                angle = valueAnimator.animatedValue as Int
                when {
                    isLowToUp -> if (angle == 360) state = STATE_ROTATE_OVER
                    else -> if (angle == 0) state = STATE_ROTATE_OVER
                }
            }
        }
        val animatorAlpha: ValueAnimator = ValueAnimator().apply {
            if (isLowToUp) setIntValues(0, 255) else setIntValues(255, 0)
            duration = mDuration.toLong()
            addUpdateListener { valueAnimator ->
                alpha = valueAnimator.animatedValue as Int
                when {
                    isLowToUp -> if (alpha == 255) state = STATE_ROTATE_OVER
                    else -> if (alpha == 0) state = STATE_ROTATE_OVER
                }
            }
        }
        val animatorTextMove: ValueAnimator = ValueAnimator().apply {
            when {
                isLowToUp -> setIntValues(maxWidth - maxHeight / 2, maxWidth / 2)
                else -> setIntValues(maxWidth / 2, maxWidth - maxHeight / 2)
            }
            duration = mDuration.toLong()
            addUpdateListener { valueAnimator ->
                textPosition = valueAnimator.animatedValue as Int
                when {
                    isLowToUp -> if (textPosition == maxWidth / 2) state = STATE_ROTATE_OVER
                    else -> if (textPosition == maxWidth - maxHeight / 2)
                        state = STATE_ROTATE_OVER
                }
            }
        }
        val animatorBtnMove: ValueAnimator = ValueAnimator().apply {
            when {
                isLowToUp -> setIntValues(maxWidth - maxHeight / 2, maxHeight / 2)
                else -> setIntValues(maxHeight / 2, maxWidth - maxHeight / 2)
            }
            duration = mDuration.toLong()
            addUpdateListener { valueAnimator ->
                minusBtnPosition = valueAnimator.animatedValue as Int
                when {
                    isLowToUp -> if (minusBtnPosition == maxHeight / 2) state = STATE_ROTATE_OVER
                    else -> if (minusBtnPosition == maxWidth - maxHeight / 2)
                        state = STATE_ROTATE_OVER
                }
                invalidate()
            }
        }
        AnimatorSet().apply {
            duration = mDuration.toLong()
            playTogether(arrayListOf<Animator>().apply {
                add(animatorTextRotate)
                add(animatorAlpha)
                add(animatorTextMove)
                add(animatorBtnMove)
            })
        }.start()
    }//开始旋转动画

    fun setTextNum(num: Int) {
        this.num = num
        state = STATE_ROTATE_OVER
        invalidate()
    }//设置购买数量
}