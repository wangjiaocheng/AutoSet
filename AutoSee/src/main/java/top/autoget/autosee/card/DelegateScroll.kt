package top.autoget.autosee.card

interface DelegateScroll {
    var viewScrollX: Int
    var viewScrollY: Int
    fun scrollViewTo(x: Int, y: Int)
}