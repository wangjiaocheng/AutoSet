package top.autoget.autosee.recycle

import android.annotation.SuppressLint
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import top.autoget.autosee.recycle.binder.ItemBinderBase
import top.autoget.autosee.recycle.common.ViewHolderBase

open class BaseAdapterBinder(list: MutableList<Any>? = null) :
    BaseAdapterQuick<Any, ViewHolderBase>(0, list) {
    private val classDiffMap: MutableMap<Class<*>, DiffUtil.ItemCallback<Any>?> =
        mutableMapOf()//存储每个Binder类型对应Diff

    private inner class ItemCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem.javaClass == newItem.javaClass) classDiffMap[oldItem.javaClass]?.let {
                return it.areItemsTheSame(oldItem, newItem)
            }
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem.javaClass == newItem.javaClass) classDiffMap[oldItem.javaClass]?.let {
                return it.areContentsTheSame(oldItem, newItem)
            }
            return true
        }

        override fun getChangePayload(oldItem: Any, newItem: Any): Any? {
            if (oldItem.javaClass == newItem.javaClass)
                return classDiffMap[oldItem.javaClass]?.getChangePayload(oldItem, newItem)
            return null
        }
    }

    init {
        setDiffCallback(ItemCallback())
    }

    override fun getDefItemViewType(position: Int): Int = findViewType(data[position].javaClass)
    private fun findViewType(clazz: Class<*>): Int =
        checkNotNull(mTypeMap[clazz]) { "findViewType: ViewType: $clazz Not Find!" }

    open fun getItemBinder(viewType: Int): ItemBinderBase<Any, ViewHolderBase> =
        checkNotNull(mBinderArray[viewType] as ItemBinderBase<Any, ViewHolderBase>)
        { "getItemBinder: viewType '$viewType' no such Binder found，please use addItemBinder() first!" }

    inline fun <reified T : Any> addItemBinder(
        baseItemBinder: ItemBinderBase<T, *>, callback: DiffUtil.ItemCallback<T>? = null
    ): BaseAdapterBinder = apply { addItemBinder(T::class.java, baseItemBinder, callback) }//kotlin

    private val mTypeMap = HashMap<Class<*>, Int>()
    private val mBinderArray = SparseArray<ItemBinderBase<Any, *>>()

    @JvmOverloads
    fun <T : Any> addItemBinder(
        clazz: Class<out T>, baseItemBinder: ItemBinderBase<T, *>,
        callback: DiffUtil.ItemCallback<T>? = null
    ): BaseAdapterBinder = apply {
        val itemType = mTypeMap.size + 1
        mTypeMap[clazz] = itemType
        mBinderArray.append(itemType, baseItemBinder as ItemBinderBase<Any, *>)
        baseItemBinder.binderAdapter = this
        callback?.let { classDiffMap[clazz] = it as DiffUtil.ItemCallback<Any> }
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase =
        getItemBinder(viewType).apply { binderContext = context }
            .onCreateViewHolder(parent, viewType)

    override fun convert(holder: ViewHolderBase, item: Any) =
        getItemBinder(holder.itemViewType).convert(holder, item)

    override fun convert(holder: ViewHolderBase, item: Any, payloads: List<Any>) =
        getItemBinder(holder.itemViewType).convert(holder, item, payloads)

    override fun onViewAttachedToWindow(holder: ViewHolderBase) {
        super.onViewAttachedToWindow(holder)
        getItemBinderOrNull(holder.itemViewType)?.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolderBase) {
        super.onViewDetachedFromWindow(holder)
        getItemBinderOrNull(holder.itemViewType)?.onViewDetachedFromWindow(holder)
    }

    override fun onFailedToRecycleView(holder: ViewHolderBase): Boolean =
        getItemBinderOrNull(holder.itemViewType)?.onFailedToRecycleView(holder) ?: false

    open fun getItemBinderOrNull(viewType: Int): ItemBinderBase<Any, ViewHolderBase>? =
        mBinderArray[viewType] as? ItemBinderBase<Any, ViewHolderBase>

    override fun bindViewClickListener(viewHolder: ViewHolderBase, viewType: Int) {
        super.bindViewClickListener(viewHolder, viewType)
        bindClick(viewHolder)
        bindChildClick(viewHolder, viewType)
    }

    protected open fun bindClick(viewHolder: ViewHolderBase) {
        if (onItemClickListener == null) viewHolder.itemView.setOnClickListener {
            var position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                position -= headerLayoutCount
                getItemBinder(viewHolder.itemViewType)
                    .onClick(viewHolder, it, data[position], position)
            }
        }
        if (onItemLongClickListener == null) viewHolder.itemView.setOnLongClickListener {
            var position = viewHolder.adapterPosition
            when (position) {
                RecyclerView.NO_POSITION -> return@setOnLongClickListener false
                else -> {
                    position -= headerLayoutCount
                    getItemBinder(viewHolder.itemViewType)
                        .onLongClick(viewHolder, it, data[position], position)
                }
            }
        }
    }

    protected open fun bindChildClick(viewHolder: ViewHolderBase, viewType: Int) =
        getItemBinder(viewType).let { provider ->
            if (onItemChildClickListener == null) provider.getChildClickViewIds().forEach { id ->
                viewHolder.itemView.findViewById<View>(id)?.apply {
                    if (!isClickable) isClickable = true
                    setOnClickListener {
                        var position: Int = viewHolder.adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            position -= headerLayoutCount
                            provider.onChildClick(viewHolder, it, data[position], position)
                        }
                    }
                }
            }
            if (onItemChildLongClickListener == null) provider.getChildLongClickViewIds()
                .forEach { id ->
                    viewHolder.itemView.findViewById<View>(id)?.apply {
                        if (!isLongClickable) isLongClickable = true
                        setOnLongClickListener {
                            var position: Int = viewHolder.adapterPosition
                            when (position) {
                                RecyclerView.NO_POSITION -> return@setOnLongClickListener false
                                else -> {
                                    position -= headerLayoutCount
                                    provider.onChildLongClick(
                                        viewHolder,
                                        it,
                                        data[position],
                                        position
                                    )
                                }
                            }
                        }
                    }
                }
        }
}//用Binder实现adapter可实现单多布局，数据实体类不存继承问题。多种条目时，避免convert()中太多业务逻辑，逻辑放在对应BaseItemBinder中。适用于：实体类不方便扩展，此Adapter数据类型可任意类型，默认不需实现getItemType；item类型较多，convert()中管理复杂。ViewHolder由ItemBinderBase实现，每个可拥有自己类型的ViewHolder类型。数据类型为Any。