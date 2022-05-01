package top.autoget.automap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.amap.api.services.busline.BusLineItem
import top.autoget.automap.databinding.BuslineItemBinding

class MapBusLineAdapter(context: Context?, private val busLineItems: List<BusLineItem>?) :
    BaseAdapter() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int = busLineItems?.size ?: 0
    override fun getItem(position: Int): BusLineItem? = busLineItems?.get(position)
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        val buslineItemBinding: BuslineItemBinding =
            BuslineItemBinding.inflate(layoutInflater, parent, false)
        val viewHolder: ViewHolder = view?.let { view?.tag as ViewHolder } ?: run {
            view = buslineItemBinding.root.apply {
                tag = ViewHolder().apply {
                    busName = buslineItemBinding.busname
                    busId = buslineItemBinding.busid
                }
            }
            view?.tag as ViewHolder
        }
        viewHolder.apply {
            busName?.text = "公交名:${busLineItems?.get(position)?.busLineName}"
            busId?.text = "公交ID:${busLineItems?.get(position)?.busLineId}"
        }
        return view
    }

    internal inner class ViewHolder {
        var busName: TextView? = null
        var busId: TextView? = null
    }
}