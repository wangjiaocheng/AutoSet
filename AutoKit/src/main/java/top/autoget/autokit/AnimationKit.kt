package top.autoget.autokit

import android.animation.*
import android.graphics.ColorMatrixColorFilter
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.ImageView

object AnimationKit : LoggerKit {
    fun isRunning(valueAnimator: ValueAnimator?): Boolean = valueAnimator?.isRunning ?: false
    fun isStarted(valueAnimator: ValueAnimator?): Boolean = valueAnimator?.isStarted ?: false
    fun start(animator: Animator?) = animator?.run { if (!isStarted) start() }
    fun stop(animator: Animator?) = animator?.run { if (!isRunning) end() }
    fun popupAppear(view: View, durationTime: Long): ObjectAnimator =
        ObjectAnimator.ofPropertyValuesHolder(
            view.apply {
                visibility = View.VISIBLE
                alpha = 0f
            }, PropertyValuesHolder.ofFloat("alpha", 0f, 1f),
            PropertyValuesHolder.ofFloat("scaleX", 0f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 0f, 1f)
        ).apply {
            duration = durationTime
            interpolator = OvershootInterpolator()
        }

    fun popupDisappear(
        view: View, durationTime: Long, animatorListenerAdapter: AnimatorListenerAdapter?
    ): ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
        view, PropertyValuesHolder.ofFloat("alpha", 1f, 0f),
        PropertyValuesHolder.ofFloat("scaleX", 1f, 0f),
        PropertyValuesHolder.ofFloat("scaleY", 1f, 0f)
    ).apply {
        duration = durationTime
        interpolator = AnticipateOvershootInterpolator()
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                view.visibility = View.GONE
                animatorListenerAdapter?.onAnimationEnd(animation)
            }
        })
    }

    fun animationCardFlip(beforeView: View, afterView: View) = when {
        beforeView.visibility == View.GONE ->
            ObjectAnimator.ofFloat(beforeView, "rotationY", -90f, 0f)
        afterView.visibility == View.GONE ->
            ObjectAnimator.ofFloat(afterView, "rotationY", -90f, 0f)
        else -> null
    }?.apply {
        duration = 250
        interpolator = DecelerateInterpolator()
    }?.let { inVis2Vis ->
        when {
            beforeView.visibility == View.GONE ->
                ObjectAnimator.ofFloat(afterView, "rotationY", 0f, 90f)
            afterView.visibility == View.GONE ->
                ObjectAnimator.ofFloat(beforeView, "rotationY", 0f, 90f)
            else -> null
        }?.apply {
            duration = 250
            interpolator = AccelerateInterpolator()
        }?.let { vis2InVis ->
            vis2InVis.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {}
                override fun onAnimationRepeat(animator: Animator) {}
                override fun onAnimationCancel(animator: Animator) {}
                override fun onAnimationEnd(animator: Animator) {
                    afterView.visibility = View.GONE
                    (if (beforeView.visibility == View.GONE) inVis2Vis else vis2InVis).start()
                    beforeView.visibility = View.VISIBLE
                }
            })
            vis2InVis.start()
        }
    }//局部layout可达到字体翻转背景不翻转

    fun animationZoomIn(view: View, scale: Float, dist: Float) = view.apply {
        pivotY = height.toFloat()
        pivotX = width / 2f
    }.let {
        ObjectAnimator.ofFloat(it, "scaleX", 1f, scale).let { animator ->
            AnimatorSet().apply {
                duration = 300
                play(ObjectAnimator.ofFloat(it, "translationY", 0f, -dist)).with(animator)
                play(animator).with(ObjectAnimator.ofFloat(it, "scaleY", 1f, scale))
            }.start()
        }
    }

    fun animationZoomOut(view: View, scale: Float) = view.apply {
        pivotY = height.toFloat()
        pivotX = width / 2f
    }.let {
        ObjectAnimator.ofFloat(it, "scaleX", scale, 1f).let { animator ->
            AnimatorSet().apply {
                duration = 300
                play(ObjectAnimator.ofFloat(it, "translationY", it.translationY, 0f)).with(animator)
                play(animator).with(ObjectAnimator.ofFloat(it, "scaleY", scale, 1f))
            }.start()
        }
    }

    fun ScaleUpDown(view: View) = view.startAnimation(ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f).apply {
        duration = 1200
        interpolator = LinearInterpolator()
        repeatCount = -1
        repeatMode = Animation.RESTART
    })

    fun animateHeight(start: Int, end: Int, view: View) = ValueAnimator.ofInt(start, end)
        .apply { addUpdateListener { view.layoutParams.height = it.animatedValue as Int } }.start()

    interface OnDoIntListener {
        fun doSomething(intValue: Int)
    }//textView.textColor(animation.animatedValue as Int)

    fun animationColorGradient(
        beforeColor: Int, afterColor: Int, onDoIntListener: OnDoIntListener
    ) = ValueAnimator.ofObject(ArgbEvaluator(), beforeColor, afterColor).setDuration(3000)
        .apply { addUpdateListener { onDoIntListener.doSomething(it.animatedValue as Int) } }
        .start()

    private val VIEW_TOUCH_DARK: View.OnTouchListener = object : View.OnTouchListener {
        val BT_SELECTED: FloatArray = floatArrayOf(
            1f, 0f, 0f, 0f, -50f, 0f, 1f, 0f, 0f, -50f,
            0f, 0f, 1f, 0f, -50f, 0f, 0f, 0f, 1f, 0f
        )
        val BT_NOT_SELECTED: FloatArray = floatArrayOf(
            1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f
        )

        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean = false.apply {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> when (view) {
                    is ImageView -> view.apply {
                        isDrawingCacheEnabled = true
                        colorFilter = ColorMatrixColorFilter(BT_SELECTED)
                    }
                    else -> view.apply {
                        background.colorFilter = ColorMatrixColorFilter(BT_SELECTED)
                        setBackgroundDrawable(background)
                    }
                }
                MotionEvent.ACTION_UP -> when (view) {
                    is ImageView -> view.apply {
                        colorFilter = ColorMatrixColorFilter(BT_NOT_SELECTED)
                        error("变回来")
                    }
                    else -> view.apply {
                        background.colorFilter = ColorMatrixColorFilter(BT_NOT_SELECTED)
                        setBackgroundDrawable(background)
                    }
                }
            }
        }
    }

    @JvmOverloads
    fun addTouchDark(view: View, isClick: Boolean = false): View = view.apply {
        setOnTouchListener(VIEW_TOUCH_DARK)
        if (!isClick) setOnClickListener { }
    }//深

    private val VIEW_TOUCH_LIGHT: View.OnTouchListener = object : View.OnTouchListener {
        val BT_SELECTED: FloatArray = floatArrayOf(
            1f, 0f, 0f, 0f, 50f, 0f, 1f, 0f, 0f, 50f,
            0f, 0f, 1f, 0f, 50f, 0f, 0f, 0f, 1f, 0f
        )
        val BT_NOT_SELECTED: FloatArray = floatArrayOf(
            1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f, 0f, 0f, 0f, 1f, 0f
        )

        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean = false.apply {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> when (view) {
                    is ImageView -> view.apply {
                        isDrawingCacheEnabled = true
                        colorFilter = ColorMatrixColorFilter(BT_SELECTED)
                    }
                    else -> view.apply {
                        background.colorFilter = ColorMatrixColorFilter(BT_SELECTED)
                        setBackgroundDrawable(view.background)
                    }
                }
                MotionEvent.ACTION_UP -> when (view) {
                    is ImageView -> view.apply {
                        colorFilter = ColorMatrixColorFilter(BT_NOT_SELECTED)
                        error("变回来")
                    }
                    else -> view.apply {
                        background.colorFilter = ColorMatrixColorFilter(BT_NOT_SELECTED)
                        setBackgroundDrawable(view.background)
                    }
                }
            }
        }
    }

    @JvmOverloads
    fun addTouchLight(view: View, isClick: Boolean = false): View = view.apply {
        setOnTouchListener(VIEW_TOUCH_LIGHT)
        if (!isClick) setOnClickListener { }
    }//暗
}