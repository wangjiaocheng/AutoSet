package top.autoget.autosee

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ViewFlipper

class TextVerticalMore
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewFlipper(context, attrs) {
    var interval = 5000
    var animDuration = 500L
    var isSetAnimDuration = false

    init {
        flipInterval = interval
        inAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_marquee_in)
            .apply { if (isSetAnimDuration) duration = animDuration }
        outAnimation = AnimationUtils.loadAnimation(context, R.anim.anim_marquee_out)
            .apply { if (isSetAnimDuration) duration = animDuration }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, view: View)
    }

    var onItemClickListener: OnItemClickListener? = null
    fun setViews(views: MutableList<View>?) = views?.run {
        if (isNotEmpty()) {
            removeAllViews()
            for ((index, view) in withIndex()) {
                addView(view.apply {
                    setOnClickListener { onItemClickListener?.onItemClick(index, view) }
                })
            }
            startFlipping()
        }
    }
}