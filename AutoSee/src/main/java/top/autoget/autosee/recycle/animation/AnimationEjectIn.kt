package top.autoget.autosee.recycle.animation

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.Interpolator
import kotlin.math.pow
import kotlin.math.sin

class AnimationEjectIn : AnimationBase {
    override fun animators(view: View): Array<Animator> = arrayOf(
        ObjectAnimator.ofFloat(view, "translationX", -view.rootView.width.toFloat(), 0f).apply {
            duration = 800
            interpolator = EjectInterpolator()
        })

    inner class EjectInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            val factor = 0.7f
            return (2.0.pow(-10.0 * input) * sin((input - factor / 4) * (2 * Math.PI) / factor) + 1).toFloat()
        }
    }
}