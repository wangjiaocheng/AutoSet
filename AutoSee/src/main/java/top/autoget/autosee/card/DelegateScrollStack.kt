package top.autoget.autosee.card

class DelegateScrollStack(private val cardStackView: CardStackView) : DelegateScroll {
    private var scrollX: Int = 0
    private var scrollY: Int = 0
    override var viewScrollX: Int
        get() = scrollX
        set(viewScrollX) = scrollViewTo(viewScrollX, scrollY)
    override var viewScrollY: Int
        get() = scrollY
        set(viewScrollY) = scrollViewTo(scrollX, viewScrollY)
    private val updateChildPos = cardStackView.run {
        for (i in 0 until childCount) {
            getChildAt(i).apply {
                translationY = cardStackView.getChildAt(0).let {
                    when {
                        top - scrollY < it.y -> it.y - top
                        top - scrollY > top -> 0f
                        else -> -scrollY.toFloat()
                    }
                }
            }
        }
    }

    override fun scrollViewTo(x: Int, y: Int) {
        cardStackView.run {
            scrollX = clamp(x, width - paddingLeft - paddingRight, width)
            scrollY = clamp(y, showHeight, totalLength)
        }
        updateChildPos
    }

    private fun clamp(n: Int, my: Int, child: Int): Int =
        if (my >= child || n < 0) 0 else if (my + n > child) child - my else n
}