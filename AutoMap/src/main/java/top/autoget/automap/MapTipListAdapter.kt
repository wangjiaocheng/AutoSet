package top.autoget.automap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.amap.api.services.help.Tip
import top.autoget.autokit.StringKit.isSpace
import top.autoget.automap.databinding.TipItemBinding

class MapTipListAdapter(private val context: Context?, private val tipList: MutableList<Tip>) :
    BaseAdapter() {
    override fun getCount(): Int = tipList.size
    override fun getItem(position: Int): Any = tipList[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val tipItemBinding: TipItemBinding =
            TipItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return (convertView ?: tipItemBinding.root.apply {
            tag = ViewHolder().apply {
                tipName = tipItemBinding.name
                tipAddress = tipItemBinding.adress
            }
        }).apply {
            tipList[position].run {
                (tag as ViewHolder).apply {
                    tipName?.text = name
                    tipAddress?.apply {
                        when {
                            isSpace(address) -> visibility = View.GONE
                            else -> {
                                visibility = View.VISIBLE
                                text = address
                            }
                        }
                    }
                }
            }
        }
    }

    private inner class ViewHolder {
        var tipName: TextView? = null
        var tipAddress: TextView? = null
    }
}