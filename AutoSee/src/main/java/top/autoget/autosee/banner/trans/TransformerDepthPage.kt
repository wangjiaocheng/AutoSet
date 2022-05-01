package top.autoget.autosee.banner.trans

import android.view.View
import kotlin.math.abs

class TransformerDepthPage : Transformer() {
    override fun transform(view: View, position: Float) {
        view.apply {
            when {
                position < -1 -> alpha = 0f
                position <= 0 -> {
                    alpha = 1f
                    translationX = 0f
                    scaleX = 1f
                    scaleY = 1f
                }
                position <= 1 -> {
                    alpha = 1 - position
                    translationX = width * -position
                    val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> alpha = 0f
            }
        }
    }

    companion object {
        private const val MIN_SCALE = 0.75f
    }
}