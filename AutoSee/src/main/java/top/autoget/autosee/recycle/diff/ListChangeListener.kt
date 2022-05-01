package top.autoget.autosee.recycle.diff

interface ListChangeListener<T> {
    fun onCurrentListChanged(previousList: List<T>, currentList: List<T>)
}