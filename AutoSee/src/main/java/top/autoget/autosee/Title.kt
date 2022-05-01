package top.autoget.autosee

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import top.autoget.autokit.DensityKit.dip2px
import top.autoget.autokit.InputMethodKit.hideInputMethod
import top.autoget.autokit.StringKit.isNotNull
import top.autoget.autosee.TextAutoZoom.Companion.setNormalization

class Title @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    init {
        initView(context, attrs)
    }

    var rootLayout: RelativeLayout? = null
        private set
    var leftLL: LinearLayout? = null
        private set
    var leftIV: ImageView? = null
        private set
    var leftTV: TextView? = null
        private set
    var rightLL: LinearLayout? = null
        private set
    var rightIV: ImageView? = null
        private set
    var rightTV: TextView? = null
        private set
    var textAutoZoom: TextAutoZoom? = null
        private set
    var leftIcon: Int = 0
        set(leftIcon) {
            field = leftIcon
            leftIV?.apply { setImageResource(field) }
        }
    var isLeftIconVisibility: Boolean = true
        set(isLeftIconVisibility) {
            field = isLeftIconVisibility
            leftIV?.apply { visibility = if (field) View.VISIBLE else View.GONE }
        }
    var leftText: String? = ""
        set(leftText) {
            field = leftText
            leftTV?.apply { text = field }
        }
    var leftTextColor: Int = Color.WHITE
        set(leftTextColor) {
            field = leftTextColor
            leftTV?.apply { setTextColor(field) }
        }
    var leftTextSize: Int = dip2px(8f)
        set(leftTextSize) {
            field = leftTextSize
            leftTV?.apply { setTextSize(TypedValue.COMPLEX_UNIT_PX, field.toFloat()) }
        }
    var isLeftTextVisibility: Boolean = false
        set(isLeftTextVisibility) {
            field = isLeftTextVisibility
            leftTV?.apply { visibility = if (field) View.VISIBLE else View.GONE }
        }
    var rightIcon: Int = 0
        set(rightIcon) {
            field = rightIcon
            rightIV?.apply { setImageResource(field) }
        }
    var isRightIconVisibility: Boolean = false
        set(isRightIconVisibility) {
            field = isRightIconVisibility
            rightIV?.apply { visibility = if (field) View.VISIBLE else View.GONE }
        }
    var rightText: String? = ""
        set(rightText) {
            field = rightText
            rightTV?.apply { text = field }
        }
    var rightTextColor: Int = Color.WHITE
        set(rightTextColor) {
            field = rightTextColor
            rightTV?.apply { setTextColor(field) }
        }
    var rightTextSize: Int = dip2px(8f)
        set(rightTextSize) {
            field = rightTextSize
            rightTV?.apply { setTextSize(TypedValue.COMPLEX_UNIT_PX, field.toFloat()) }
        }
    var isRightTextVisibility: Boolean = false
        set(isRightTextVisibility) {
            field = isRightTextVisibility
            rightTV?.apply {
                visibility = if (field) View.VISIBLE else View.GONE
                if (field && isRightIconVisibility) setPadding(0, 0, 0, 0)
            }
        }
    var title: String? = ""
        set(title) {
            field = title
            textAutoZoom?.apply { setText(field) }
        }
    var titleColor: Int = Color.WHITE
        set(titleColor) {
            field = titleColor
            textAutoZoom?.apply { setTextColor(field) }
        }
    var titleSize: Int = dip2px(20f)
        set(titleSize) {
            field = titleSize
            textAutoZoom?.apply { setTextSize(TypedValue.COMPLEX_UNIT_PX, field.toFloat()) }
        }
    var isTitleVisibility: Boolean = true
        set(isTitleVisibility) {
            field = isTitleVisibility
            textAutoZoom?.apply { visibility = if (field) View.VISIBLE else View.GONE }
        }

    private fun initView(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.title_view, this)
        rootLayout = findViewById(R.id.root_layout)
        leftLL = findViewById(R.id.ll_left)
        leftIV = findViewById(R.id.iv_left)
        leftTV = findViewById(R.id.tv_left)
        rightLL = findViewById(R.id.ll_right)
        rightIV = findViewById(R.id.iv_right)
        rightTV = findViewById(R.id.tv_right)
        textAutoZoom = findViewById(R.id.text_auto_zoom)
        val typedArray: TypedArray = getContext().obtainStyledAttributes(attrs, R.styleable.Title)
        try {
            typedArray.run {
                leftIcon = getResourceId(R.styleable.Title_leftIcon, R.mipmap.icon_previous)
                isLeftIconVisibility = getBoolean(R.styleable.Title_leftIconVisibility, true)
                leftText = getString(R.styleable.Title_leftText)
                leftTextColor =
                    getColor(R.styleable.Title_leftTextColor, resources.getColor(R.color.white))
                leftTextSize = getDimensionPixelSize(R.styleable.Title_leftTextSize, dip2px(8f))
                isLeftTextVisibility = getBoolean(R.styleable.Title_leftTextVisibility, false)
                rightIcon = getResourceId(R.styleable.Title_rightIcon, R.mipmap.icon_set)
                isRightIconVisibility = getBoolean(R.styleable.Title_rightIconVisibility, false)
                rightText = getString(R.styleable.Title_rightText)
                rightTextColor =
                    getColor(R.styleable.Title_rightTextColor, resources.getColor(R.color.white))
                rightTextSize = getDimensionPixelSize(R.styleable.Title_rightTextSize, dip2px(8f))
                isRightTextVisibility = getBoolean(R.styleable.Title_rightTextVisibility, false)
                title = getString(R.styleable.Title_title)
                titleColor =
                    getColor(R.styleable.Title_titleColor, resources.getColor(R.color.white))
                titleSize = getDimensionPixelSize(R.styleable.Title_titleSize, dip2px(20f))
                isTitleVisibility = getBoolean(R.styleable.Title_titleVisibility, true)
            }
        } finally {
            typedArray.recycle()
        }
        if (leftIcon != 0) leftIcon = leftIcon
        isLeftIconVisibility = isLeftIconVisibility
        leftText = leftText
        leftTextColor = leftTextColor
        leftTextSize = leftTextSize
        isLeftTextVisibility = isLeftTextVisibility
        if (rightIcon != 0) rightIcon = rightIcon
        isRightIconVisibility = isRightIconVisibility
        rightText = rightText
        rightTextColor = rightTextColor
        rightTextSize = rightTextSize
        isRightTextVisibility = isRightTextVisibility
        if (isNotNull(title)) title = title
        titleColor = titleColor
        titleSize = titleSize
        isTitleVisibility = isTitleVisibility
        initAutoFitEditText()
    }

    private fun initAutoFitEditText() {
        textAutoZoom?.apply {
            clearFocus()
            isEnabled = false
            isFocusableInTouchMode = false
            isFocusable = false
            enableSizeCache = false
            movementMethod = null
            maxHeight = dip2px(55f)
            minTextSize = 37f
        }
        try {
            setNormalization(context as Activity, rootLayout!!, textAutoZoom!!)
            hideInputMethod(context as Activity)
        } catch (e: Exception) {
        }
    }

    fun setLeftFinish(activity: Activity) = leftLL?.setOnClickListener { activity.finish() }
    fun setLeftListener(listener: OnClickListener) = leftLL?.setOnClickListener(listener)
    fun setLeftIconListener(listener: OnClickListener) = leftIV?.setOnClickListener(listener)
    fun setLeftTextListener(listener: OnClickListener) = leftTV?.setOnClickListener(listener)
    fun setRightListener(listener: OnClickListener) = rightLL?.setOnClickListener(listener)
    fun setRightIconListener(listener: OnClickListener) = rightIV?.setOnClickListener(listener)
    fun setRightTextListener(listener: OnClickListener) = rightTV?.setOnClickListener(listener)
}