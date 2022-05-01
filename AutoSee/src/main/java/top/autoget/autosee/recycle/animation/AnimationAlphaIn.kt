package top.autoget.autosee.recycle.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator

class AnimationAlphaIn @JvmOverloads constructor(private val mFrom: Float = DEFAULT_ALPHA_FROM) :
    AnimationBase {
    companion object {
        private const val DEFAULT_ALPHA_FROM = 0f
    }

    override fun animators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "alpha", mFrom, 1f).apply {
            duration = 300L
            interpolator = LinearInterpolator()
        })
}