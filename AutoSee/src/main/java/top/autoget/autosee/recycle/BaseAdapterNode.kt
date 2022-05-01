package top.autoget.autosee.recycle

import androidx.annotation.IntRange
import androidx.recyclerview.widget.DiffUtil
import top.autoget.autosee.recycle.entity.NodeBase
import top.autoget.autosee.recycle.entity.NodeExpandBase
import top.autoget.autosee.recycle.entity.NodeFooter

abstract class BaseAdapterNode(nodeList: MutableList<NodeBase>? = null) :
    BaseAdapterMultiProvider<NodeBase>(null) {
    init {
        if (!nodeList.isNullOrEmpty()) data.addAll(flatData(nodeList))
    }

    private fun flatData(
        list: Collection<NodeBase>, isExpanded: Boolean? = null
    ): MutableList<NodeBase> =
        mutableListOf<NodeBase>().apply {
            for (element in list) {
                add(element)
                when (element) {
                    is NodeExpandBase -> {
                        if (isExpanded == true || element.isExpanded) element.childNode.let {
                            if (!it.isNullOrEmpty()) addAll(flatData(it, isExpanded))
                        }//如果是展开状态或需要设置为展开状态
                        isExpanded?.let { element.isExpanded = it }
                    }
                    else -> element.childNode.let {
                        if (!it.isNullOrEmpty()) addAll(flatData(it, isExpanded))
                    }
                }
                if (element is NodeFooter) element.footerNode?.let { add(it) }
            }
        }//将输入的嵌套类型数组循环递归，扁平化数据同时设置展开状态

    fun addFooterNodeProvider(provider: BaseProviderNode) =
        addFullSpanNodeProvider(provider)//添加脚部node provider铺满一行或者一列

    private val fullSpanNodeTypeSet = mutableSetOf<Int>()
    fun addFullSpanNodeProvider(provider: BaseProviderNode) {
        fullSpanNodeTypeSet.add(provider.itemViewType)
        addItemProvider(provider)
    }//添加需要铺满的node provider

    fun addNodeProvider(provider: BaseProviderNode) = addItemProvider(provider)
    override fun addItemProvider(provider: BaseProviderItem<NodeBase>) = when (provider) {
        is BaseProviderNode -> super.addItemProvider(provider)
        else -> throw IllegalStateException("Please add BaseNodeProvider, no BaseItemProvider!")
    }//勿直接此方法添加node provider

    override fun isFixedViewType(type: Int): Boolean =
        super.isFixedViewType(type) || fullSpanNodeTypeSet.contains(type)

    override fun setDiffNewData(diffResult: DiffUtil.DiffResult, list: MutableList<NodeBase>) =
        when {
            hasEmptyView -> setNewInstance(list)
            else -> super.setDiffNewData(diffResult, flatData(list))
        }

    override fun setDiffNewData(list: MutableList<NodeBase>?, commitCallback: Runnable?) = when {
        hasEmptyView -> setNewInstance(list)
        else -> super.setDiffNewData(flatData(list ?: arrayListOf()), commitCallback)
    }

    override fun setNewInstance(list: MutableList<NodeBase>?) =
        super.setNewInstance(flatData(list ?: arrayListOf()))

    override fun setList(list: Collection<NodeBase>?) =//替换整个列表数据，对某节点下子节点进行替换用nodeReplaceChildData
        super.setList(flatData(list ?: arrayListOf()))

    override fun addData(position: Int, data: NodeBase) =
        addData(position, arrayListOf(data))//对某节点下子节点进行数据操作用nodeAddData

    override fun addData(position: Int, newData: Collection<NodeBase>) =
        super.addData(position, flatData(newData))

    override fun addData(data: NodeBase) = addData(arrayListOf(data))
    override fun addData(newData: Collection<NodeBase>) = super.addData(flatData(newData))
    override fun setData(index: Int, data: NodeBase) {
        val removeCount = removeNodesAt(index)
        val newFlatData = flatData(arrayListOf(data))
        this.data.addAll(index, newFlatData)
        when (removeCount) {
            newFlatData.size -> notifyItemRangeChanged(index + headerLayoutCount, removeCount)
            else -> {
                notifyItemRangeRemoved(index + headerLayoutCount, removeCount)
                notifyItemRangeInserted(index + headerLayoutCount, newFlatData.size)
            }
        }
    }//对某节点下子节点进行数据操作nodeSetData

    override fun removeAt(position: Int) {
        notifyItemRangeRemoved(position + headerLayoutCount, removeNodesAt(position))
        compatibilityDataSizeChanged(0)
    }//对某节点下子节点进行数据操作用nodeRemoveData

    private fun removeNodesAt(position: Int): Int = when {
        position < data.size -> {
            var removeCount: Int = removeChildAt(position)//记录被移除item数量，先移除子项
            this.data.removeAt(position)//移除node自己
            removeCount += 1
            val node = this.data[position]
            if (node is NodeFooter && node.footerNode != null) {
                this.data.removeAt(position)
                removeCount += 1
            }//移除脚部
            removeCount
        }
        else -> 0
    }//从数组中移除

    private fun removeChildAt(position: Int): Int = when {
        position < data.size -> {
            var removeCount = 0//记录被移除item数量
            val node = this.data[position]
            if (!node.childNode.isNullOrEmpty()) when (node) {
                is NodeExpandBase -> if (node.isExpanded) {
                    val items = flatData(node.childNode ?: arrayListOf())
                    this.data.removeAll(items)
                    removeCount = items.size
                }
                else -> {
                    val items = flatData(node.childNode ?: arrayListOf())
                    this.data.removeAll(items)
                    removeCount = items.size
                }
            }//移除子项
            removeCount
        }
        else -> 0
    }

    fun nodeAddData(parentNode: NodeBase, data: NodeBase) =
        parentNode.childNode?.apply { add(data) }?.let {
            if (parentNode !is NodeExpandBase || parentNode.isExpanded)
                addData(this.data.indexOf(parentNode) + it.size, data)
        }//对指定父node添加子node

    fun nodeAddData(parentNode: NodeBase, childIndex: Int, data: NodeBase) =
        parentNode.childNode?.apply { add(childIndex, data) }?.let {
            if (parentNode !is NodeExpandBase || parentNode.isExpanded)
                addData(this.data.indexOf(parentNode) + 1 + childIndex, data)
        }//对指定父node在指定位置（相对于其childNodes数据的位置）添加子node，

    fun nodeAddData(parentNode: NodeBase, childIndex: Int, newData: Collection<NodeBase>) =
        parentNode.childNode?.apply { addAll(childIndex, newData) }?.let {
            if (parentNode !is NodeExpandBase || parentNode.isExpanded)
                addData(this.data.indexOf(parentNode) + 1 + childIndex, newData)
        }//对指定父node在指定位置（相对于其childNodes数据的位置）添加子

    fun nodeRemoveData(parentNode: NodeBase, childIndex: Int) = parentNode.childNode?.let {
        if (childIndex < it.size) when {
            parentNode !is NodeExpandBase || parentNode.isExpanded -> {
                super.removeAt(this.data.indexOf(parentNode) + 1 + childIndex)
                it.removeAt(childIndex)
            }
            else -> it.removeAt(childIndex)
        }
    }//对指定父node下对子node进行移除（相对于其childNodes数据的位置）

    fun nodeRemoveData(parentNode: NodeBase, childNode: NodeBase) = parentNode.childNode?.let {
        when {
            parentNode !is NodeExpandBase || parentNode.isExpanded -> {
                remove(childNode)
                it.remove(childNode)
            }
            else -> it.remove(childNode)
        }
    }//对指定父node下对子node进行移除

    fun nodeSetData(parentNode: NodeBase, childIndex: Int, data: NodeBase) =
        parentNode.childNode?.let {
            if (childIndex < it.size) when {
                parentNode !is NodeExpandBase || parentNode.isExpanded -> {
                    setData(this.data.indexOf(parentNode) + 1 + childIndex, data)
                    it[childIndex] = data
                }
                else -> it[childIndex] = data
            }
        }//改变指定父node下子node数据（相对于其childNodes数据的位置）

    fun nodeReplaceChildData(parentNode: NodeBase, newData: Collection<NodeBase>) =
        parentNode.childNode?.let {
            when {
                parentNode !is NodeExpandBase || parentNode.isExpanded -> {
                    val parentIndex = this.data.indexOf(parentNode)
                    val removeCount = removeChildAt(parentIndex)
                    it.apply { clear() }.addAll(newData)
                    val newFlatData = flatData(newData)
                    this.data.addAll(parentIndex + 1, newFlatData)
                    val positionStart = parentIndex + 1 + headerLayoutCount
                    when (removeCount) {
                        newFlatData.size -> notifyItemRangeChanged(positionStart, removeCount)
                        else -> {
                            notifyItemRangeRemoved(positionStart, removeCount)
                            notifyItemRangeInserted(positionStart, newFlatData.size)
                        }
                    }
                }
                else -> it.apply { clear() }.addAll(newData)
            }
        }//替换父节点下子节点集合

    private fun collapse(
        @IntRange(from = 0) position: Int, isChangeChildCollapse: Boolean = false,
        animate: Boolean = true, notify: Boolean = true,
        parentPayload: Any? = null
    ): Int {
        val node = this.data[position]
        return when {
            node is NodeExpandBase && node.isExpanded -> {
                node.isExpanded = false
                val adapterPosition = position + headerLayoutCount
                when {
                    node.childNode.isNullOrEmpty() -> {
                        notifyItemChanged(adapterPosition, parentPayload)
                        0
                    }
                    else -> {
                        val items = node.childNode?.let {
                            flatData(it, if (isChangeChildCollapse) false else null)
                        } ?: mutableListOf()
                        val size = items.size
                        this.data.removeAll(items)
                        if (notify) when {
                            animate -> {
                                notifyItemChanged(adapterPosition, parentPayload)
                                notifyItemRangeRemoved(adapterPosition + 1, size)
                            }
                            else -> notifyDataSetChanged()
                        }
                        size
                    }
                }
            }
            else -> 0
        }
    }//收起Node，私有方法，为减少递归复杂度，不对外暴露isChangeChildExpand，防止错误设置，true为跟随变为收起，false保持原状态。

    private fun expand(
        @IntRange(from = 0) position: Int, isChangeChildExpand: Boolean = false,
        animate: Boolean = true, notify: Boolean = true,
        parentPayload: Any? = null
    ): Int {
        val node = this.data[position]
        return when {
            node is NodeExpandBase && !node.isExpanded -> {
                node.isExpanded = true
                val adapterPosition = position + headerLayoutCount
                when {
                    node.childNode.isNullOrEmpty() -> {
                        notifyItemChanged(adapterPosition, parentPayload)
                        0
                    }
                    else -> {
                        val items = node.childNode?.let {
                            flatData(it, if (isChangeChildExpand) true else null)
                        } ?: mutableListOf()
                        val size = items.size
                        this.data.addAll(position + 1, items)
                        if (notify) when {
                            animate -> {
                                notifyItemChanged(adapterPosition, parentPayload)
                                notifyItemRangeInserted(adapterPosition + 1, size)
                            }
                            else -> notifyDataSetChanged()
                        }
                        size
                    }
                }
            }
            else -> 0
        }
    }//展开Node，私有方法，为减少递归复杂度，不对外暴露isChangeChildExpand，防止错误设置，true为跟随变为展开，false保持原状态。

    @JvmOverloads
    fun collapse(
        @IntRange(from = 0) position: Int, animate: Boolean = true,
        notify: Boolean = true, parentPayload: Any? = null
    ): Int = collapse(position, false, animate, notify, parentPayload)

    @JvmOverloads
    fun expand(
        @IntRange(from = 0) position: Int, animate: Boolean = true,
        notify: Boolean = true, parentPayload: Any? = null
    ): Int = expand(position, false, animate, notify, parentPayload)

    @JvmOverloads
    fun expandOrCollapse(
        @IntRange(from = 0) position: Int, animate: Boolean = true,
        notify: Boolean = true, parentPayload: Any? = null
    ): Int = when (val node = this.data[position]) {
        is NodeExpandBase -> when {
            node.isExpanded -> collapse(position, false, animate, notify, parentPayload)
            else -> expand(position, false, animate, notify, parentPayload)
        }
        else -> 0
    }

    @JvmOverloads
    fun collapseAndChild(
        @IntRange(from = 0) position: Int, animate: Boolean = true,
        notify: Boolean = true, parentPayload: Any? = null
    ): Int = collapse(position, true, animate, notify, parentPayload)

    @JvmOverloads
    fun expandAndChild(
        @IntRange(from = 0) position: Int, animate: Boolean = true,
        notify: Boolean = true, parentPayload: Any? = null
    ): Int = expand(position, true, animate, notify, parentPayload)

    @JvmOverloads
    fun expandAndCollapseOther(
        @IntRange(from = 0) position: Int, isExpandedChild: Boolean = false,
        isCollapseChild: Boolean = true, animate: Boolean = true,
        notify: Boolean = true, expandPayload: Any? = null,
        collapsePayload: Any? = null
    ) = expand(position, isExpandedChild, animate, notify, expandPayload).let { expandCount ->
        if (expandCount != 0) {
            val parentPosition = findParentNode(position)
            val firstPosition: Int = when (parentPosition) {
                -1 -> 0 //如果没父节点，则为最外层，从0开始
                else -> parentPosition + 1//如果有父节点，则为子节点，从父节点+1位置开始
            }//当前层级顶部开始位置
            var newPosition = position//当前position前有node收起后position位置会变
            if (position - firstPosition > 0) for (i in firstPosition until newPosition) {
                newPosition -= collapse(i, isCollapseChild, animate, notify, collapsePayload)
            }//如果此position前有node，从顶部开始位置循环，每次折叠后，重新计算新Position
            var lastPosition: Int = when (parentPosition) {
                -1 -> data.size - 1
                else -> parentPosition + (data[parentPosition].childNode?.size
                    ?: 0) + expandCount
            }//当前层级最后位置：无父节点为最外层，否则为子节点（父节点 + 子节点数量 + 展开数量）
            if ((newPosition + expandCount) < lastPosition) {
                for (i in newPosition + expandCount + 1..lastPosition) {
                    lastPosition -= collapse(
                        i,
                        isCollapseChild,
                        animate,
                        notify,
                        collapsePayload
                    )
                }
            }//如果此position后有node
        }
    }//展开某个node时折叠其他node，isExpandedChild展开时是否展开子项目，isCollapseChild折叠其他node时是否折叠子项目

    fun findParentNode(@IntRange(from = 0) position: Int): Int {
        when (position) {
            0 -> return -1
            else -> {
                val node = this.data[position]
                for (i in position - 1 downTo 0) {
                    if (this.data[i].childNode?.contains(node) == true) return i
                }
                return -1
            }
        }
    }

    fun findParentNode(node: NodeBase): Int {
        when (val position = this.data.indexOf(node)) {
            -1, 0 -> return -1
            else -> {
                for (i in position - 1 downTo 0) {
                    if (this.data[i].childNode?.contains(node) == true) return i
                }
                return -1
            }
        }
    }//查找父节点，不存在返回-1
}