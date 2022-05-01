package top.autoget.automap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.amap.api.services.poisearch.SubPoiItem
import top.autoget.automap.databinding.GridviewItemBinding

class MapPoiSubAdapter(private val context: Context, private val poiList: MutableList<SubPoiItem>) :
    BaseAdapter() {
    override fun getCount(): Int = poiList.size
    override fun getItem(position: Int): Any = poiList[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val gridviewItemBinding: GridviewItemBinding =
            GridviewItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return (convertView ?: gridviewItemBinding.root.apply {
            tag = ViewHolder().apply { poiTitle = gridviewItemBinding.gridviewItem }
        }).apply { (tag as ViewHolder).poiTitle?.text = poiList[position].subName }
    }

    private inner class ViewHolder {
        var poiTitle: TextView? = null
    }
}