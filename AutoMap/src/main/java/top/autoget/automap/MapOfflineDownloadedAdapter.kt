package top.autoget.automap

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.amap.api.maps.offlinemap.OfflineMapCity
import com.amap.api.maps.offlinemap.OfflineMapManager
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.debug

class MapOfflineDownloadedAdapter(
    private val context: Context, private val offlineMapManager: OfflineMapManager?
) : LoggerKit, BaseAdapter() {
    private val cities: MutableList<OfflineMapCity> = mutableListOf()
    private val initCityList = {
        var start = nowMillis
        cities.apply {
            for (mapCity in this) {
                remove(mapCity)
            }
        }
        debug("$loggerTag->Offline Downloading notifyData cities iterator cost: ${nowMillis - start}")
        start = nowMillis
        cities.apply {
            offlineMapManager?.run {
                addAll(downloadOfflineMapCityList)
                addAll(downloadingCityList)
            }
        }
        debug("$loggerTag->Offline Downloading notifyData getDownloadingCityList cost: ${nowMillis - start}")
        start = nowMillis
        notifyDataSetChanged()
        debug("$loggerTag->Offline Downloading notifyData notifyDataSetChanged cost: ${nowMillis - start}")
    }

    init {
        initCityList
    }

    val notifyDataChange = {
        val start = nowMillis
        initCityList
        debug("$loggerTag->Offline Downloading notifyDataChange cost: ${nowMillis - start}")
    }

    override fun getItem(index: Int): Any = cities[index]
    override fun getItemViewType(position: Int): Int = 0
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = cities.size
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? =
        (convertView ?: MapOfflineChild(context, offlineMapManager).let { offLineChild ->
            offLineChild.offLineChildView
                ?.apply { tag = ViewHolder().apply { mapOfflineChild = offLineChild } }
        })?.apply {
            (tag as ViewHolder).mapOfflineChild?.apply { setOffLineCity(getItem(position) as OfflineMapCity) }
        }

    inner class ViewHolder {
        var mapOfflineChild: MapOfflineChild? = null
    }
}