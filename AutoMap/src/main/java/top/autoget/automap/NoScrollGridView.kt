package top.autoget.automap

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

class NoScrollGridView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) :
    GridView(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) = super.onMeasure(
        widthMeasureSpec, MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
    )
}