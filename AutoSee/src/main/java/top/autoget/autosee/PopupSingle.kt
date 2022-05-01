package top.autoget.autosee

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import top.autoget.autokit.DensityKit
import top.autoget.autokit.ScreenKit

class PopupSingle(
    private val context: Context, layout: Int = R.layout.popup_list_layout,
    width: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
    height: Int = ViewGroup.LayoutParams.WRAP_CONTENT
) : PopupWindow() {
    data class ActionItem(var title: CharSequence = "", var resourcesId: Int = 0)
    interface OnItemOnClickListener {
        fun onItemClick(item: ActionItem?, position: Int)
    }

    private var listView: ListView? = null
    var itemOnClickListener: OnItemOnClickListener? = null//弹窗子类项选中时监听
    private var isDirty = false
    private val actionItems = mutableListOf<ActionItem>()
    val cleanAction = {
        if (actionItems.isEmpty()) {
            actionItems.clear()
            isDirty = true
        }
    }

    fun addAction(action: ActionItem?) = action?.let {
        actionItems.add(action)
        isDirty = true
    }

    fun getAction(position: Int): ActionItem? =
        if (position in 0..actionItems.size) actionItems[position] else null

    init {
        isFocusable = true//设置可以获得焦点
        isTouchable = true//设置弹窗内可点击
        isOutsideTouchable = true//设置弹窗外可点击
        setBackgroundDrawable(BitmapDrawable())
        setWidth(width)//设置弹窗宽度
        setHeight(height)//设置弹窗高度
        contentView = LayoutInflater.from(context).inflate(layout, null)
        listView = contentView.findViewById(R.id.title_list)
        listView?.onItemClickListener = AdapterView.OnItemClickListener { _, _, index, _ ->
            dismiss()
            itemOnClickListener?.onItemClick(actionItems[index], index)
        }//点击子类项后弹窗消失
    }

    private val location = IntArray(2)
    private val rect = Rect()
    var colorItemText = 0
    private val populateActions = {
        isDirty = false
        listView?.adapter = object : BaseAdapter() {
            override fun getCount(): Int = actionItems.size
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getItem(position: Int): Any = actionItems[position]
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? =
                (convertView ?: LayoutInflater.from(context)
                    .inflate(R.layout.popup_list_item, null)).apply {
                    val actionItem = actionItems[position]
                    if (colorItemText == 0) colorItemText =
                        context.resources.getColor(android.R.color.white, null)
                    (findViewById<TextView>(R.id.tv_pop)).apply {
                        text = actionItem.title//设置文本文字
                        textSize = 14f
                        setTextColor(colorItemText)
                        setPadding(0, 10, 0, 10)//设置文本域范围
                        isSingleLine = true//设置文本在一行内显示（不换行）
                        gravity = Gravity.CENTER//设置文本居中
                    }
                    (findViewById<ImageView>(R.id.iv_pop)).apply {
                        when (actionItem.resourcesId) {
                            0 -> visibility = View.GONE
                            else -> {
                                visibility = View.VISIBLE
                                setImageResource(actionItem.resourcesId)
                            }
                        }
                    }
                }
        }
    }//设置弹窗列表子项
    private val popupGravity = Gravity.NO_GRAVITY
    private val listPadding = 10//列表弹窗间隔
    fun show(view: View) {
        view.getLocationOnScreen(location)//获得屏幕点击位置坐标
        rect[location[0], location[1], location[0] + view.width] = location[1] + view.height
        if (isDirty) populateActions//判断是否需要添加或更新列表子类项
        showAtLocation(
            view, popupGravity, ScreenKit.screenWidth - listPadding - width / 2,
            rect.bottom + DensityKit.dip2px(7.5f)
        )//显示弹窗位置
    }

    fun show(view: View, dex: Int) {
        view.getLocationOnScreen(location)
        rect[location[0], location[1], location[0] + view.width] = location[1] + view.height
        if (isDirty) populateActions
        showAtLocation(view, popupGravity, location[0], rect.bottom + dex)
    }
}