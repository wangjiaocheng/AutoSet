package top.autoget.autosee

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import top.autoget.autokit.HandleKit.mainHandler
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class HeartLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {
    class FloatAnimation(
        path: Path, private val mRotation: Float, parent: View, private val view: View
    ) : Animation() {
        init {
            parent.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }

        private val pathMeasure: PathMeasure = PathMeasure(path, false)
        private val distance: Float = pathMeasure.length
        override fun applyTransformation(factor: Float, transformation: Transformation) {
            pathMeasure.getMatrix(
                distance * factor, transformation.matrix, PathMeasure.POSITION_MATRIX_FLAG
            )
            transformation.alpha = 1.0f - factor
            when {
                3000.0f * factor < 200.0f -> scale(
                    factor.toDouble(),
                    0.0, 0.06666667014360428, 0.20000000298023224, 1.100000023841858
                )
                3000.0f * factor < 300.0f -> scale(
                    factor.toDouble(),
                    0.06666667014360428, 0.10000000149011612, 1.100000023841858, 1.0
                )
                else -> 1f
            }.let { scale ->
                view.apply {
                    scaleX = scale
                    scaleY = scale
                    rotation = mRotation * factor
                }
            }
        }

        private fun scale(a: Double, b: Double, c: Double, d: Double, e: Double): Float =
            ((a - b) / (c - b) * (e - d) + d).toFloat()
    }

    class PathAnimator(val config: Config) {
        class Config {
            var xRand: Int = 0
            var initX: Int = 0
            var initY: Int = 0
            var factorXPoint: Int = 0
            var factorBezier: Int = 0
            var heartWidth: Int = 0
            var heartHeight: Int = 0
            var animLengthRand: Int = 0
            var animLength: Int = 0
            var animDuration: Int = 0

            companion object {
                fun fromTypeArray(typedArray: TypedArray): Config = Config().apply {
                    typedArray.run {
                        resources.let {
                            xRand = getDimension(
                                R.styleable.HeartLayout_xRand,
                                it.getDimensionPixelOffset(R.dimen.heart_anim_bezier_x_rand)
                                    .toFloat()
                            ).toInt()
                            initX = getDimension(
                                R.styleable.HeartLayout_initX,
                                it.getDimensionPixelOffset(R.dimen.heart_anim_init_x).toFloat()
                            ).toInt()
                            initY = getDimension(
                                R.styleable.HeartLayout_initY,
                                it.getDimensionPixelOffset(R.dimen.heart_anim_init_y).toFloat()
                            ).toInt()
                            factorXPoint = getDimension(
                                R.styleable.HeartLayout_factorXPoint,
                                it.getDimensionPixelOffset(R.dimen.heart_anim_x_point_factor)
                                    .toFloat()
                            ).toInt()
                            factorBezier = getInteger(
                                R.styleable.HeartLayout_factorBezier,
                                it.getInteger(R.integer.heart_anim_bezier_factor)
                            )
                            heartWidth = getDimension(
                                R.styleable.HeartLayout_heart_width,
                                it.getDimensionPixelOffset(R.dimen.heart_size_width).toFloat()
                            ).toInt()
                            heartHeight = getDimension(
                                R.styleable.HeartLayout_heart_height,
                                it.getDimensionPixelOffset(R.dimen.heart_size_height).toFloat()
                            ).toInt()
                            animLengthRand = getDimension(
                                R.styleable.HeartLayout_animLengthRand,
                                it.getDimensionPixelOffset(R.dimen.heart_anim_length_rand).toFloat()
                            ).toInt()
                            animLength = getDimension(
                                R.styleable.HeartLayout_animLength,
                                it.getDimensionPixelOffset(R.dimen.heart_anim_length).toFloat()
                            ).toInt()
                            animDuration = getInteger(
                                R.styleable.HeartLayout_anim_duration,
                                it.getInteger(R.integer.anim_duration)
                            )
                        }
                    }
                }
            }
        }

        private val counter = AtomicInteger(0)
        private val random: Random = Random()
        private val randomRotation: Float
            get() = random.nextFloat() * 28.6f - 14.3f

        fun start(child: View, parent: ViewGroup) {
            parent.addView(child, ViewGroup.LayoutParams(config.heartWidth, config.heartHeight))
            FloatAnimation(createPath(counter, parent, 2), randomRotation, parent, child).apply {
                duration = config.animDuration.toLong()
                interpolator = LinearInterpolator()
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationStart(animation: Animation) {
                        counter.incrementAndGet()
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        mainHandler.post { parent.removeView(child) }
                        counter.decrementAndGet()
                    }
                })
                interpolator = LinearInterpolator()
            }.run { child.startAnimation(this) }
        }

        private fun createPath(counter: AtomicInteger, view: View, factor: Int): Path =
            (counter.toInt() * 15 + config.animLength * factor + random.nextInt(config.animLengthRand)).toFloat()
                .let {
                    val x: Float = config.initX.toFloat()
                    val x1: Float = (random.nextInt(config.xRand) + config.factorXPoint).toFloat()
                    val x2: Float = (random.nextInt(config.xRand) + config.factorXPoint).toFloat()
                    val y: Float = (view.height - config.initY).toFloat()
                    val y1: Float = y - it / 2
                    val y2: Float = y - it
                    val factorTemp: Float = it / config.factorBezier
                    Path().apply {
                        moveTo(x, y)
                        cubicTo(x, y - factorTemp, x1, y1 + factorTemp, x1, y1)
                        moveTo(x1, y1)
                        cubicTo(x1, y1 - factorTemp, x2, y2 + factorTemp, x2, y2)
                    }
                }
    }

    private var animator: PathAnimator? = null
        set(animator) {
            clearAnimation()
            field = animator
        }

    override fun clearAnimation() {
        for (i in 0 until childCount) {
            getChildAt(i).clearAnimation()
        }
        removeAllViews()
    }

    init {
        val typedArray: TypedArray = context
            .obtainStyledAttributes(attrs, R.styleable.HeartLayout, defStyleAttr, defStyleRes)
        try {
            animator = PathAnimator(PathAnimator.Config.fromTypeArray(typedArray))
        } finally {
            typedArray.recycle()
        }
    }

    class HeartView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
    ) : AppCompatImageView(context, attrs, defStyleAttr) {
        private var heart: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.anim_heart)
        private var heartBorder: Bitmap =
            BitmapFactory.decodeResource(resources, R.mipmap.anim_heart_border)

        @JvmOverloads
        fun setColorAndDrawables(
            color: Int, resIdHeart: Int = R.mipmap.anim_heart,
            resIdHeartBorder: Int = R.mipmap.anim_heart_border
        ) {
            if (resIdHeart != R.mipmap.anim_heart)
                heart = BitmapFactory.decodeResource(resources, resIdHeart)
            if (resIdHeartBorder != R.mipmap.anim_heart_border)
                heartBorder = BitmapFactory.decodeResource(resources, resIdHeartBorder)
            setImageDrawable(BitmapDrawable(resources, createHeart(color)))
        }

        private val canvas = Canvas()
        private val paint: Paint =
            Paint().apply { flags = Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG }

        private fun createHeart(color: Int): Bitmap? = try {
            Bitmap.createBitmap(heartBorder.width, heartBorder.height, Bitmap.Config.ARGB_8888)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
            null
        }.apply {
            canvas.setBitmap(this)
            canvas.run {
                drawBitmap(heartBorder, 0f, 0f, paint)
                drawBitmap(
                    heart, (heartBorder.width - heart.width) / 2f,
                    (heartBorder.height - heart.height) / 2f, paint.apply {
                        colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                    })
                setBitmap(null)
            }
            paint.colorFilter = null
        }
    }

    @JvmOverloads
    fun addHeart(
        color: Int, resIdHeart: Int = R.mipmap.anim_heart,
        resIdHeartBorder: Int = R.mipmap.anim_heart_border
    ) = animator?.start(HeartView(context).apply {
        setColorAndDrawables(color, resIdHeart, resIdHeartBorder)
    }, this)
}