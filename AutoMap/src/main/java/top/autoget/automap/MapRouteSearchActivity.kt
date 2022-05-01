package top.autoget.automap

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Poi
import com.amap.api.navi.view.PoiInputItemWidget
import com.amap.api.services.core.AMapException
import com.amap.api.services.core.PoiItem
import com.amap.api.services.help.Inputtips
import com.amap.api.services.help.InputtipsQuery
import com.amap.api.services.help.Tip
import com.amap.api.services.poisearch.PoiResult
import com.amap.api.services.poisearch.PoiSearch
import top.autoget.autokit.StringKit.isNotSpace
import top.autoget.autokit.StringKit.isSpace
import top.autoget.automap.databinding.ActivityRouteSearchBinding
import top.autoget.automap.databinding.LayoutSearchHeaderBinding

class MapRouteSearchActivity : AppCompatActivity(), TextWatcher, Inputtips.InputtipsListener,
    AdapterView.OnItemClickListener, PoiSearch.OnPoiSearchListener {
    private var pointType: Int = 0
    private val activityRouteSearchBinding: ActivityRouteSearchBinding =
        ActivityRouteSearchBinding.inflate(layoutInflater)
    private val layoutSearchHeaderBinding: LayoutSearchHeaderBinding =
        LayoutSearchHeaderBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityRouteSearchBinding.root)
        activityRouteSearchBinding.tvMsg.visibility = View.GONE
        activityRouteSearchBinding.resultList.onItemClickListener = this
        layoutSearchHeaderBinding.searchInput.addTextChangedListener(this)
        layoutSearchHeaderBinding.searchInput.requestFocus()
        pointType = intent?.extras?.getInt("pointType", -1) ?: 0
    }

    var city: String = ""
    override fun afterTextChanged(s: Editable?) {}
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = try {
        activityRouteSearchBinding.tvMsg
            .apply { if (visibility == View.VISIBLE) visibility = View.GONE }
        s.toString().trim { it <= ' ' }.let {
            when {
                isSpace(it) -> activityRouteSearchBinding.resultList.visibility = View.GONE
                else -> {
                    setLoadingVisible(true)
                    Inputtips(applicationContext, InputtipsQuery(it, city))
                        .apply { setInputtipsListener(this@MapRouteSearchActivity) }
                        .requestInputtipsAsyn()
                }
            }
        }
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    private fun setLoadingVisible(isVisible: Boolean) = layoutSearchHeaderBinding.searchLoading
        .apply { visibility = if (isVisible) View.VISIBLE else View.GONE }

    private var currentTipList: MutableList<Tip> = mutableListOf()
    override fun onGetInputtips(tipList: MutableList<Tip?>?, rCode: Int) {
        try {
            setLoadingVisible(false)
            when (rCode) {
                1000 -> mutableListOf<Tip>().apply {
                    tipList?.let {
                        for (tip in it) {
                            tip?.point?.let { add(tip) }
                        }
                        currentTipList = this
                    }
                }.let { tips ->
                    when {
                        tips.isNotEmpty() -> MapTipListAdapter(applicationContext, tips).let {
                            activityRouteSearchBinding.resultList.apply {
                                visibility = View.VISIBLE
                                adapter = it
                            }
                            it.notifyDataSetChanged()
                        }
                        else -> {
                            activityRouteSearchBinding.tvMsg.apply {
                                text = "抱歉，没有搜索到结果，请换个关键词试试"
                                visibility = View.VISIBLE
                            }
                            activityRouteSearchBinding.resultList.visibility = View.GONE
                        }
                    }
                }
                else -> activityRouteSearchBinding.tvMsg.apply {
                    text = "出错了，请稍后重试"
                    visibility = View.VISIBLE
                }
            }
        } catch (e: Throwable) {
            activityRouteSearchBinding.tvMsg.apply {
                text = "出错了，请稍后重试"
                visibility = View.VISIBLE
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        (parent?.getItemAtPosition(position) as Tip?)?.run {
            Poi(name, LatLng(point.latitude, point.longitude), poiID)
                .apply { selectedPoi = this }.run {
                    if (isNotSpace(poiId)) PoiSearch.Query(name, "", city).apply {
                        isDistanceSort = false
                        requireSubPois(true)
                    }.let {
                        PoiSearch(applicationContext, it)
                            .apply { setOnPoiSearchListener(this@MapRouteSearchActivity) }
                            .searchPOIIdAsyn(poiId)
                    }
                }
        }
    }

    override fun onPoiSearched(poiResult: PoiResult?, i: Int) {}
    var selectedPoi: Poi? = null
    override fun onPoiItemSearched(poiItem: PoiItem?, errorCode: Int) = try {
        var code = 0
        var latLng: LatLng? = null
        if (errorCode == AMapException.CODE_AMAP_SUCCESS) poiItem?.run {
            when (pointType) {
                PoiInputItemWidget.TYPE_START -> {
                    code = 100
                    latLng = exit?.run { LatLng(latitude, longitude) }
                        ?: enter?.run { LatLng(latitude, longitude) }
                }
                PoiInputItemWidget.TYPE_DEST -> {
                    code = 200
                    latLng = enter?.run { LatLng(latitude, longitude) }
                }
            }
        }
        (latLng?.let { selectedPoi?.run { Poi(name, it, poiId) } } ?: selectedPoi).let { poi ->
            Intent(this, MapRouteCalculateActivity::class.java)
                .apply { putExtra("poi", poi) }.let { setResult(code, it) }
        }
        finish()
    } catch (e: Throwable) {
        e.printStackTrace()
    }
}