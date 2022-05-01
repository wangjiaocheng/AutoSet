package top.autoget.autosee.recycle.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

class AnimationDropIn : AnimationBase {
    override fun animators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "scaleY", 1.3f, 1f).apply {
            duration = 350
            interpolator = DecelerateInterpolator()
        }, ObjectAnimator.ofFloat(view, "scaleX", 1.3f, 1f).apply {
            duration = 350
            interpolator = DecelerateInterpolator()
        }, ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply { duration = 350 })
}