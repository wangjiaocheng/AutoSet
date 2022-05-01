package top.autoget.automap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.amap.api.services.busline.BusStationItem
import com.amap.api.services.route.BusStep
import com.amap.api.services.route.RailwayStationItem
import top.autoget.automap.databinding.ItemBusSegmentBinding
import top.autoget.automap.databinding.ItemBusSegmentExBinding

class MapRouteBusSegmentListAdapter(
    private val context: Context, busStepList: MutableList<BusStep>
) : BaseAdapter() {
    class SchemeBusStep(step: BusStep?) : BusStep() {
        init {
            step?.let {
                walk = step.walk
                busLines = step.busLines
                railway = step.railway
                taxi = step.taxi
            }
        }

        var isWalk: Boolean = false
        var isBus: Boolean = false
        var isRailway: Boolean = false
        var isTaxi: Boolean = false
        var isStart: Boolean = false
        var isEnd: Boolean = false
    }

    private val schemeBusSteps: MutableList<SchemeBusStep> = mutableListOf<SchemeBusStep>().apply {
        add(SchemeBusStep(null).apply { isStart = true })
        for (busStep: BusStep in busStepList) {
            when {
                busStep.walk != null && busStep.walk.distance > 0 -> add(SchemeBusStep(busStep).apply {
                    isWalk = true
                })
                busStep.busLines != null -> add(SchemeBusStep(busStep).apply { isBus = true })
                busStep.railway != null -> add(SchemeBusStep(busStep).apply { isRailway = true })
                busStep.taxi != null -> add(SchemeBusStep(busStep).apply { isTaxi = true })
            }
        }
        add(SchemeBusStep(null).apply { isEnd = true })
    }

    override fun getCount(): Int = schemeBusSteps.size
    override fun getItem(position: Int): Any = schemeBusSteps[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View {
        val itemBusSegmentBinding: ItemBusSegmentBinding =
            ItemBusSegmentBinding.inflate(LayoutInflater.from(context), viewGroup, false)
        return (convertView ?: itemBusSegmentBinding.root.apply {
            tag = ViewHolder().apply {
                busDirIcon = itemBusSegmentBinding.busDirIcon
                busLineName = itemBusSegmentBinding.busLineName
                busStationNum = itemBusSegmentBinding.busStationNum
                busExpandImage = itemBusSegmentBinding.busExpandImage
                busDirUp = itemBusSegmentBinding.busDirIconUp
                busDirDown = itemBusSegmentBinding.busDirIconDown
                splitLine = itemBusSegmentBinding.busSegSplitLine
                parent = itemBusSegmentBinding.busItem
                expandContent = itemBusSegmentBinding.expandContent
            }
        }).apply {
            schemeBusSteps[position].let { schemeBusStep ->
                (tag as ViewHolder).apply {
                    when (position) {
                        0 -> {
                            busDirIcon?.setImageResource(R.mipmap.dir_start)
                            busLineName?.text = "出发"
                            busStationNum?.visibility = View.GONE
                            busExpandImage?.visibility = View.GONE
                            busDirUp?.visibility = View.INVISIBLE
                            busDirDown?.visibility = View.VISIBLE
                            splitLine?.visibility = View.GONE
                        }
                        schemeBusSteps.size - 1 -> {
                            busDirIcon?.setImageResource(R.mipmap.dir_end)
                            busLineName?.text = "到达终点"
                            busStationNum?.visibility = View.INVISIBLE
                            busExpandImage?.visibility = View.INVISIBLE
                            busDirUp?.visibility = View.VISIBLE
                            busDirDown?.visibility = View.INVISIBLE
                        }
                        else -> when {
                            schemeBusStep.isWalk && schemeBusStep.walk != null && schemeBusStep.walk.distance > 0 -> {
                                busDirIcon?.setImageResource(R.mipmap.dir13)
                                busLineName?.text = "步行${schemeBusStep.walk.distance as Int}米"
                                busStationNum?.visibility = View.GONE
                                busExpandImage?.visibility = View.GONE
                                busDirUp?.visibility = View.VISIBLE
                                busDirDown?.visibility = View.VISIBLE
                            }
                            schemeBusStep.isBus && schemeBusStep.busLines.size > 0 -> {
                                busDirIcon?.setImageResource(R.mipmap.dir14)
                                busLineName?.text = schemeBusStep.busLines[0].busLineName
                                busStationNum?.apply {
                                    visibility = View.VISIBLE
                                    text = "${(schemeBusStep.busLines[0].passStationNum + 1)}站"
                                }
                                busExpandImage?.visibility = View.VISIBLE
                                busDirUp?.visibility = View.VISIBLE
                                busDirDown?.visibility = View.VISIBLE
                                parent?.tag = position
                                parent?.setOnClickListener(ArrowClick(this, schemeBusStep))
                            }
                            schemeBusStep.isRailway && schemeBusStep.railway != null -> {
                                busDirIcon?.setImageResource(R.mipmap.dir16)
                                busLineName?.text = schemeBusStep.railway.name
                                busStationNum?.apply {
                                    visibility = View.VISIBLE
                                    text = "${(schemeBusStep.railway.viastops.size + 1)}站"
                                }
                                busExpandImage?.visibility = View.VISIBLE
                                busDirUp?.visibility = View.VISIBLE
                                busDirDown?.visibility = View.VISIBLE
                                parent?.tag = position
                                parent?.setOnClickListener(ArrowClick(this, schemeBusStep))
                            }
                            schemeBusStep.isTaxi && schemeBusStep.taxi != null -> {
                                busDirIcon?.setImageResource(R.mipmap.dir14)
                                busLineName?.text = "打车到终点"
                                busStationNum?.visibility = View.GONE
                                busExpandImage?.visibility = View.GONE
                                busDirUp?.visibility = View.VISIBLE
                                busDirDown?.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    private inner class ViewHolder {
        var busDirIcon: ImageView? = null
        var busLineName: TextView? = null
        var busStationNum: TextView? = null
        var busExpandImage: ImageView? = null
        var busDirUp: ImageView? = null
        var busDirDown: ImageView? = null
        var splitLine: ImageView? = null
        var parent: RelativeLayout? = null
        var expandContent: LinearLayout? = null
        var arrowExpend: Boolean = false
    }

    private inner class ArrowClick
    constructor(private val viewHolder: ViewHolder?, private var schemeBusStep: SchemeBusStep) :
        View.OnClickListener {
        override fun onClick(view: View) {
            viewHolder?.apply {
                when {
                    arrowExpend -> {
                        arrowExpend = false
                        busExpandImage?.setImageResource(R.mipmap.down)
                        expandContent?.removeAllViews()
                    }
                    else -> {
                        arrowExpend = true
                        busExpandImage?.setImageResource(R.mipmap.up)
                        schemeBusStep = schemeBusSteps[view.tag.toString().toInt()]
                        when {
                            schemeBusStep.isBus -> {
                                addBusStation(schemeBusStep.busLines[0].departureBusStation)
                                for (busStationItem in schemeBusStep.busLines[0].passStations) {
                                    addBusStation(busStationItem)
                                }
                                addBusStation(schemeBusStep.busLines[0].arrivalBusStation)
                            }
                            schemeBusStep.isRailway -> {
                                addRailwayStation(schemeBusStep.railway.departurestop)
                                for (railwayStationItem in schemeBusStep.railway.viastops) {
                                    addRailwayStation(railwayStationItem)
                                }
                                addRailwayStation(schemeBusStep.railway.arrivalstop)
                            }
                        }
                    }
                }
            }
        }

        private val itemBusSegmentExBinding: ItemBusSegmentExBinding =
            ItemBusSegmentExBinding.inflate(LayoutInflater.from(context))
        private val rootView: View = itemBusSegmentExBinding.root
        private fun addBusStation(station: BusStationItem) = rootView.apply {
            itemBusSegmentExBinding.busLineStationNameEx.apply { text = station.busStationName }
        }.let { viewHolder?.expandContent?.addView(it) }

        private fun addRailwayStation(station: RailwayStationItem) = rootView.apply {
            itemBusSegmentExBinding.busLineStationNameEx
                .apply { text = station.run { "$name ${getRailwayTime(time)}" } }
        }.let { viewHolder?.expandContent?.addView(it) }
    }

    companion object {
        fun getRailwayTime(time: String): String =
            time.run { "${substring(0, 2)}:${substring(2, length)}" }
    }
}