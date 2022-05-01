package top.autoget.autosee.flow.bean

import java.io.Serializable

data class BeanMenu(var menu: String? = null, var details: MutableList<BeanDetail?>? = null) :
    Serializable