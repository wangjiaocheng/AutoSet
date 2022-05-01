package top.autoget.autosee.recycle

import top.autoget.autosee.recycle.entity.NodeBase

abstract class BaseProviderNode : BaseProviderItem<NodeBase>() {
    override fun getAdapter(): BaseAdapterNode? = super.getAdapter() as? BaseAdapterNode
}