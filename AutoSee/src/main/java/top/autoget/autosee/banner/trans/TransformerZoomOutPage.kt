package top.autoget.autosee.banner.trans

import android.view.View
import kotlin.math.abs
import kotlin.math.max

class TransformerZoomOutPage : Transformer() {
    override fun transform(view: View, position: Float) {
        view.apply {
            when {
                position < -1 -> alpha = 0f
                position <= 1 -> {
                    val scaleFactor = max(MIN_SCALE, 1 - abs(position))
                    val hMargin = width * (1 - scaleFactor) / 2
                    val vMargin = height * (1 - scaleFactor) / 2
                    alpha =
                        MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)
                    translationX = when {
                        position < 0 -> hMargin - vMargin / 2
                        else -> -hMargin + vMargin / 2
                    }
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                else -> alpha = 0f
            }
        }
    }

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }
}