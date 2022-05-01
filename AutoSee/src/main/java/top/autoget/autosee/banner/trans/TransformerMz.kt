package top.autoget.autosee.banner.trans

import android.view.View
import kotlin.math.abs

class TransformerMz : Transformer() {
    override fun transform(view: View, position: Float) {
        view.scaleY = when {
            position < -1 -> MIN_SCALE
            position <= 1 -> MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
            else -> MIN_SCALE
        }//每次滑动后进行微小移动是为防止三星某些手机上出现两边页面为显示的情况
    }

    companion object {
        private const val MIN_SCALE = 0.9f//0.85f
    }
}