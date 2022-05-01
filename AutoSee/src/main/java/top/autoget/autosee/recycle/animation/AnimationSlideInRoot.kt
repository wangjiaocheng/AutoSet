package top.autoget.autosee.recycle.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

class AnimationSlideInRoot : AnimationBase {
    override fun animators(view: View): Array<Animator> =
        arrayOf(ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply { duration = 450 },
            ObjectAnimator.ofFloat(view, "translationY", view.rootView.height.toFloat(), 0f).apply {
                duration = 450
                interpolator = DecelerateInterpolator(1.2f)
            })
}