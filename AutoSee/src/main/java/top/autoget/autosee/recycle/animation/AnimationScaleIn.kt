package top.autoget.autosee.recycle.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

class AnimationScaleIn @JvmOverloads constructor(private val mFrom: Float = DEFAULT_SCALE_FROM) :
    AnimationBase {
    companion object {
        private const val DEFAULT_SCALE_FROM = .5f
    }

    override fun animators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "scaleX", mFrom, 1f).apply {
            duration = 300L
            interpolator = DecelerateInterpolator()
        }, ObjectAnimator.ofFloat(view, "scaleY", mFrom, 1f).apply {
            duration = 300L
            interpolator = DecelerateInterpolator()
        })
}