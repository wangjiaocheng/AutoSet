package top.autoget.autosee.recycle

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import top.autoget.autosee.recycle.common.ViewHolderBase

abstract class BaseAdapterMultiProvider<T>(data: MutableList<T>? = null) :
    BaseAdapterQuick<T, ViewHolderBase>(0, data) {
    override fun getDefItemViewType(position: Int): Int = getItemType(data, position)
    protected abstract fun getItemType(data: List<T>, position: Int): Int
    private val mItemProviders by lazy(LazyThreadSafetyMode.NONE) { SparseArray<BaseProviderItem<T>>() }
    protected open fun getItemProvider(viewType: Int): BaseProviderItem<T>? =
        mItemProviders.get(viewType)

    open fun addItemProvider(provider: BaseProviderItem<T>) {
        provider.setAdapter(this)
        mItemProviders.put(provider.itemViewType, provider)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBase {
        val provider = getItemProvider(viewType)
        checkNotNull(provider) { "ViewType: $viewType no such provider found，please use addItemProvider() first!" }
        return provider.apply { context = parent.context }.onCreateViewHolder(parent, viewType)
            .apply { provider.onViewHolderCreated(this, viewType) }
    }

    override fun convert(holder: ViewHolderBase, item: T) =
        getItemProvider(holder.itemViewType)?.convert(holder, item) ?: Unit

    override fun convert(holder: ViewHolderBase, item: T, payloads: List<Any>) =
        getItemProvider(holder.itemViewType)?.convert(holder, item, payloads) ?: Unit

    override fun onViewAttachedToWindow(holder: ViewHolderBase) {
        super.onViewAttachedToWindow(holder)
        getItemProvider(holder.itemViewType)?.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: ViewHolderBase) {
        super.onViewDetachedFromWindow(holder)
        getItemProvider(holder.itemViewType)?.onViewDetachedFromWindow(holder)
    }

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
                mItemProviders.get(viewHolder.itemViewType)
                    .onClick(viewHolder, it, data[position], position)
            }
        }
        if (onItemLongClickListener == null) viewHolder.itemView.setOnLongClickListener {
            var position = viewHolder.adapterPosition
            when (position) {
                RecyclerView.NO_POSITION -> return@setOnLongClickListener false
                else -> {
                    position -= headerLayoutCount
                    mItemProviders.get(viewHolder.itemViewType)
                        .onLongClick(viewHolder, it, data[position], position)
                }
            }
        }
    }

    protected open fun bindChildClick(viewHolder: ViewHolderBase, viewType: Int) =
        getItemProvider(viewType)?.let { provider ->
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
}//多种条目时，避免convert()中太多业务逻辑，逻辑放在对应ItemProvider中。适用于：实体类不方便扩展，此Adapter数据类型可任意类型，只需getItemType中返回对应类型；item类型较多，convert()中管理复杂。ViewHolder由BaseProviderItem实现，每个可拥有自己类型的ViewHolder类型。