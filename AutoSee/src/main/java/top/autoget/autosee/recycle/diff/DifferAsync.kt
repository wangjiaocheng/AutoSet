package top.autoget.autosee.recycle.diff

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.ListUpdateCallback
import top.autoget.autokit.HandleKit.mainHandler
import top.autoget.autosee.recycle.BaseAdapterQuick
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor

class DifferAsync<T>(
    private val adapter: BaseAdapterQuick<T, *>, private val config: DifferAsyncConfig<T>
) : Differ<T> {
    private class MainThreadExecutor() : Executor {
        override fun execute(command: Runnable) {
            mainHandler.post(command)
        }
    }

    private val sMainThreadExecutor: Executor = MainThreadExecutor()
    private var mMainThreadExecutor: Executor = config.mainThreadExecutor ?: sMainThreadExecutor
    private val mUpdateCallback: ListUpdateCallback = ListUpdateCallback(adapter)
    fun addData(index: Int, data: T) {
        val previousList: List<T> = adapter.data
        adapter.data.add(index, data)
        mUpdateCallback.onInserted(index, 1)
        onCurrentListChanged(previousList, null)
    }

    fun addData(data: T) {
        val previousList: List<T> = adapter.data
        adapter.data.add(data)
        mUpdateCallback.onInserted(previousList.size, 1)
        onCurrentListChanged(previousList, null)
    }

    fun addList(list: List<T>?) = list?.let {
        val previousList: List<T> = adapter.data
        adapter.data.addAll(list)
        mUpdateCallback.onInserted(previousList.size, list.size)
        onCurrentListChanged(previousList, null)
    }

    fun changeData(index: Int, newData: T, payload: T?) {
        val previousList: List<T> = adapter.data
        adapter.data[index] = newData
        mUpdateCallback.onChanged(index, 1, payload)
        onCurrentListChanged(previousList, null)
    }

    fun removeAt(index: Int) {
        val previousList: List<T> = adapter.data
        adapter.data.removeAt(index)
        mUpdateCallback.onRemoved(index, 1)
        onCurrentListChanged(previousList, null)
    }

    fun remove(t: T) {
        val previousList: List<T> = adapter.data
        val index = adapter.data.indexOf(t)
        if (index != -1) {
            adapter.data.removeAt(index)
            mUpdateCallback.onRemoved(index, 1)
            onCurrentListChanged(previousList, null)
        }
    }

    private var mMaxScheduledGeneration = 0

    @JvmOverloads
    fun submitList(newList: MutableList<T>?, commitCallback: Runnable? = null) {
        val runGeneration: Int = ++mMaxScheduledGeneration
        if (newList === adapter.data) {
            commitCallback?.run()
            return
        }
        val oldList: List<T> = adapter.data
        if (newList == null) {
            val countRemoved: Int = adapter.data.size
            adapter.data = mutableListOf()
            mUpdateCallback.onRemoved(0, countRemoved)
            onCurrentListChanged(oldList, commitCallback)
            return
        }
        if (adapter.data.isEmpty()) {
            adapter.data = newList
            mUpdateCallback.onInserted(0, newList.size)
            onCurrentListChanged(oldList, commitCallback)
            return
        }
        config.backgroundThreadExecutor?.execute {
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldList.size
                override fun getNewListSize(): Int = newList.size
                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    return when {
                        oldItem != null && newItem != null ->
                            config.diffCallback.getChangePayload(oldItem, newItem)
                        else -> throw AssertionError()
                    }
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    return when {
                        oldItem != null && newItem != null ->
                            config.diffCallback.areItemsTheSame(oldItem, newItem)
                        else -> oldItem == null && newItem == null
                    }
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    return when {
                        oldItem != null && newItem != null ->
                            config.diffCallback.areContentsTheSame(oldItem, newItem)
                        oldItem == null && newItem == null -> true
                        else -> throw AssertionError()
                    }
                }
            }).let { result ->
                mMainThreadExecutor.execute {
                    if (mMaxScheduledGeneration == runGeneration)
                        latchList(newList, result, commitCallback)
                }
            }
        }
    }

    private fun latchList(
        newList: MutableList<T>,
        diffResult: DiffResult,
        commitCallback: Runnable?
    ) {
        val previousList: List<T> = adapter.data
        adapter.data = newList
        diffResult.dispatchUpdatesTo(mUpdateCallback)
        onCurrentListChanged(previousList, commitCallback)
    }

    private val mListeners: MutableList<ListChangeListener<T>> = CopyOnWriteArrayList()
    private fun onCurrentListChanged(previousList: List<T>, commitCallback: Runnable?) {
        for (listener in mListeners) {
            listener.onCurrentListChanged(previousList, adapter.data)
        }
        commitCallback?.run()
    }

    val clearAllListListener = mListeners.clear()
    fun removeListListener(listChangeListener: ListChangeListener<T>) =
        mListeners.remove(listChangeListener)

    override fun addListListener(listChangeListener: ListChangeListener<T>) {
        mListeners.add(listChangeListener)
    }
}