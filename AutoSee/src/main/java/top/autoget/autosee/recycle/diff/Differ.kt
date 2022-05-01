package top.autoget.autosee.recycle.diff

interface Differ<T> {
    fun addListListener(listChangeListener: ListChangeListener<T>)
}