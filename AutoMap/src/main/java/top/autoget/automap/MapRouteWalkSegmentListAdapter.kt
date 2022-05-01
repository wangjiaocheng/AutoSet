package top.autoget.automap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.services.route.WalkStep
import top.autoget.automap.MapCommon.getWalkActionID
import top.autoget.automap.databinding.ItemBusSegmentBinding

class MapRouteWalkSegmentListAdapter(
    private val context: Context, walkStepList: MutableList<WalkStep>
) : BaseAdapter() {
    private val walkSteps: MutableList<WalkStep> = mutableListOf<WalkStep>().apply {
        add(WalkStep())
        for (walkStep in walkStepList) {
            add(walkStep)
        }
        add(WalkStep())
    }

    override fun getCount(): Int = walkSteps.size
    override fun getItem(position: Int): Any = walkSteps[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemBusSegmentBinding: ItemBusSegmentBinding =
            ItemBusSegmentBinding.inflate(LayoutInflater.from(context), parent, false)
        return (convertView ?: itemBusSegmentBinding.root.apply {
            tag = ViewHolder().apply {
                lineName = itemBusSegmentBinding.busLineName
                dirIcon = itemBusSegmentBinding.busDirIcon
                dirUp = itemBusSegmentBinding.busDirIconUp
                dirDown = itemBusSegmentBinding.busDirIconDown
                splitLine = itemBusSegmentBinding.busSegSplitLine
            }
        }).apply {
            walkSteps[position].let { walkStep ->
                (tag as ViewHolder).apply {
                    when (position) {
                        0 -> {
                            dirIcon?.setImageResource(R.mipmap.dir_start)
                            lineName?.text = "出发"
                            dirUp?.visibility = View.INVISIBLE
                            dirDown?.visibility = View.VISIBLE
                            splitLine?.visibility = View.INVISIBLE
                        }
                        walkSteps.size - 1 -> {
                            dirIcon?.setImageResource(R.mipmap.dir_end)
                            lineName?.text = "到达终点"
                            dirUp?.visibility = View.VISIBLE
                            dirDown?.visibility = View.INVISIBLE
                        }
                        else -> {
                            dirIcon?.setImageResource(getWalkActionID(walkStep.action))
                            lineName?.text = walkStep.instruction
                            dirUp?.visibility = View.VISIBLE
                            dirDown?.visibility = View.VISIBLE
                            splitLine?.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private inner class ViewHolder {
        var dirIcon: ImageView? = null
        var lineName: TextView? = null
        var dirUp: ImageView? = null
        var dirDown: ImageView? = null
        var splitLine: ImageView? = null
    }
}