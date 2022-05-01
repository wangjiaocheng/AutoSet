package top.autoget.automap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import com.amap.api.maps.offlinemap.OfflineMapCity
import com.amap.api.maps.offlinemap.OfflineMapManager
import com.amap.api.maps.offlinemap.OfflineMapProvince
import top.autoget.automap.databinding.OfflineGroupBinding

class MapOfflineListAdapter(
    private val context: Context, private val offlineMapManager: OfflineMapManager?,
    private val offlineProvinceList: MutableList<OfflineMapProvince?>
) : ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnGroupExpandListener,
    BaseExpandableListAdapter() {
    private val isOpen: BooleanArray = BooleanArray(offlineProvinceList.size)
    override fun onGroupCollapse(groupPosition: Int) {
        isOpen[groupPosition] = false
    }

    override fun onGroupExpand(groupPosition: Int) {
        isOpen[groupPosition] = true
    }

    override fun hasStableIds(): Boolean = true
    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    override fun getGroup(groupPosition: Int): Any? =
        offlineProvinceList[groupPosition]?.provinceName

    override fun getChild(groupPosition: Int, childPosition: Int): Any? = null
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
    override fun getGroupCount(): Int = offlineProvinceList.size
    override fun getChildrenCount(groupPosition: Int): Int =
        (offlineProvinceList[groupPosition]?.cityList?.size
            ?: 0).let { if (isNormalProvinceGroup(groupPosition)) it + 1 else it }

    private fun isNormalProvinceGroup(groupPosition: Int): Boolean =
        groupPosition > 2//非“直辖市、概要图、港澳”

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup
    ): View {
        val offlineGroupBinding: OfflineGroupBinding =
            OfflineGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return (convertView ?: offlineGroupBinding.root).apply {
            val groupText: TextView = offlineGroupBinding.groupText
            val groupImage: ImageView = offlineGroupBinding.groupImage
            groupText.text = offlineProvinceList[groupPosition]?.provinceName
            when {
                isOpen[groupPosition] ->
                    groupImage.setImageDrawable(context.resources.getDrawable(R.mipmap.arrow_down))
                else -> groupImage.setImageDrawable(context.resources.getDrawable(R.mipmap.arrow_right))
            }
        }
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean,
        convertView: View?, parent: ViewGroup
    ): View? = (convertView ?: MapOfflineChild(context, offlineMapManager).let { offLineChild ->
        offLineChild.offLineChildView
            ?.apply { tag = ViewHolder().apply { mapOfflineChild = offLineChild } }
    })?.apply {
        (tag as ViewHolder).mapOfflineChild?.apply {
            isProvince = false
            when {
                isNormalProvinceGroup(groupPosition) -> when {
                    isProvinceItem(groupPosition, childPosition) -> {
                        isProvince = true
                        getCity(offlineProvinceList[groupPosition])
                    }
                    else -> offlineProvinceList[groupPosition]?.cityList?.get(childPosition - 1)
                }
                else -> offlineProvinceList[groupPosition]?.cityList?.get(childPosition)
            }.let { setOffLineCity(it) }
        }
    }

    inner class ViewHolder {
        var mapOfflineChild: MapOfflineChild? = null
    }

    private fun isProvinceItem(groupPosition: Int, childPosition: Int): Boolean =
        isNormalProvinceGroup(groupPosition) && childPosition == 0

    private fun getCity(offlineMapProvince: OfflineMapProvince?): OfflineMapCity? =
        offlineMapProvince?.let {
            OfflineMapCity().apply {
                url = it.url
                state = it.state
                city = it.provinceName
                size = it.size
                setCompleteCode(it.getcompleteCode())
            }
        }
}