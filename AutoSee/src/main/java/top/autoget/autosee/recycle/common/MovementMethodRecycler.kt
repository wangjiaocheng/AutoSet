package top.autoget.autosee.recycle.common

import android.text.Selection
import android.text.Spannable
import android.text.method.BaseMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView

object MovementMethodRecycler : BaseMovementMethod() {
    override fun initialize(widget: TextView, text: Spannable) = Selection.removeSelection(text)
    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            val x = event.x.toInt() - widget.totalPaddingLeft + widget.scrollX
            val y = event.y.toInt() - widget.totalPaddingTop + widget.scrollY
            val spans = widget.layout
                .run { getOffsetForHorizontal(getLineForVertical(y), x.toFloat()) }
                .let { buffer.getSpans(it, it, ClickableSpan::class.java) }
            when {
                spans.isEmpty() -> Selection.removeSelection(buffer)
                else -> {
                    when (action) {
                        MotionEvent.ACTION_UP -> spans[0].onClick(widget)
                        else -> Selection.setSelection(
                            buffer, buffer.getSpanStart(spans[0]), buffer.getSpanEnd(spans[0])
                        )
                    }
                    return true
                }
            }
        }
        return false
    }
}