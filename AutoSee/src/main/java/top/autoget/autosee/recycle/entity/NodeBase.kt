package top.autoget.autosee.recycle.entity

abstract class NodeBase {
    abstract val childNode: MutableList<NodeBase>?
}//重写获取子节点，没有子节点返回null或空数组，返回null无法对子节点数据进行新增和删除等操作