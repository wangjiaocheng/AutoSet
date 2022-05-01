package top.autoget.autosee.recycle.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

class AnimationSlideInBottom : AnimationBase {
    override fun animators(view: View): Array<Animator> = arrayOf(
        ObjectAnimator.ofFloat(view, "translationY", view.measuredHeight.toFloat(), 0f).apply {
            duration = 400L
            interpolator = DecelerateInterpolator(1.3f)
        })
}