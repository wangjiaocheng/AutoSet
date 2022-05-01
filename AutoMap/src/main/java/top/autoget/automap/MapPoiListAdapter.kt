package top.autoget.automap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import com.amap.api.services.core.PoiItem
import top.autoget.automap.databinding.ListviewItemBinding

class MapPoiListAdapter(private val context: Context, private val poiList: MutableList<PoiItem>) :
    BaseAdapter() {
    override fun getCount(): Int = poiList.size
    override fun getItem(position: Int): Any = poiList[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listviewItemBinding: ListviewItemBinding =
            ListviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return (convertView ?: listviewItemBinding.root.apply {
            tag = ViewHolder().apply {
                poiTitle = listviewItemBinding.poiTitle
                poiSubs = listviewItemBinding.listviewItemGridview
            }
        }).apply {
            poiList[position].run {
                (tag as ViewHolder).apply {
                    poiTitle?.text = title
                    if (subPois.size > 0)
                        poiSubs?.adapter = MapPoiSubAdapter(context, subPois)
                }
            }
        }
    }

    private inner class ViewHolder {
        var poiTitle: TextView? = null
        var poiSubs: GridView? = null
    }
}