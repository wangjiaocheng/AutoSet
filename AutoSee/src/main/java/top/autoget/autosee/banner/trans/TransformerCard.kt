package top.autoget.autosee.banner.trans

import android.util.Log
import android.view.View

class TransformerCard(private var cardHeight: Float = 10f) : Transformer() {
    override fun transform(view: View, position: Float) {
        view.apply {
            when {
                position > 0 -> {
                    translationX = -width * position
                    translationY = cardHeight * position
                    val scale = (width - cardHeight * position) / width
                    Log.d(TAG, "transform: $scale")
                    scaleX = scale
                    scaleY = scale
                    isClickable = false

                }
                else -> {
                    translationX = 0f
                    isClickable = true
                }
            }
        }
    }

    companion object {
        private const val TAG = "TransformerCard"
    }
}//卡片式