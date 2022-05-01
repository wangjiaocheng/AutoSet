package top.autoget.autosee

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Outline
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import top.autoget.autokit.AnimationKit.popupAppear
import top.autoget.autokit.AnimationKit.popupDisappear
import top.autoget.autokit.VersionKit.aboveLollipop
import java.util.*

class PopupViewManager @JvmOverloads constructor(tipListener: TipListener? = null) {
    interface TipListener {
        fun onTipDismissed(view: View, anchorViewId: Int, byUser: Boolean)
    }

    private val mTipListener: TipListener? = tipListener

    class PopupView(builderPopupView: BuilderPopupView) {
        class BuilderPopupView {
            var mContext: Context? = null
            var mAnchorView: View? = null
            var mRootView: ViewGroup? = null
            var mMessage: String? = null
                set(mMessage) {
                    field = mMessage
                    field?.let { mSpannableMessage = null }
                }
            var mSpannableMessage: Spannable? = null
                set(mSpannableMessage) {
                    field = mSpannableMessage
                    field?.let { mMessage = null }
                }
            var mIsShowArrow: Boolean = true
            var mBackgroundColor: Int = mContext?.resources?.getColor(R.color.accentPink_a200) ?: 0
            var mTextColor: Int = mContext?.resources?.getColor(R.color.white) ?: 0
            var mTextSize: Int = 14
            var mElevation: Float = 0f
            var mOffsetX: Int = 0
            var mOffsetY: Int = 0

            @Gravity
            var mTextGravity: Int = GRAVITY_LEFT

            @Align
            var mAlign: Int = ALIGN_CENTER

            @Position
            var mPosition: Int = 0

            constructor(
                context: Context, anchorView: View, root: ViewGroup,
                message: String, @Position position: Int
            ) {
                mContext = context
                mAnchorView = anchorView
                mRootView = root
                mMessage = message
                mPosition = position
            }

            constructor(
                context: Context, anchorView: View, root: ViewGroup,
                message: Spannable, @Position position: Int
            ) {
                mContext = context
                mAnchorView = anchorView
                mRootView = root
                mSpannableMessage = message
                mPosition = position
            }

            val buildPopupView: PopupView
                get() = PopupView(this)
        }

        val context: Context? = builderPopupView.mContext
        val anchorView: View? = builderPopupView.mAnchorView
        val rootView: ViewGroup? = builderPopupView.mRootView
        val message: String? = builderPopupView.mMessage
        val spannableMessage: Spannable? = builderPopupView.mSpannableMessage
        val isShowArrow: Boolean = builderPopupView.mIsShowArrow
        val backgroundColor: Int = builderPopupView.mBackgroundColor
        val textColor: Int = builderPopupView.mTextColor
        val textSize: Int = builderPopupView.mTextSize
        val elevation: Float = builderPopupView.mElevation
        val offsetX: Int = builderPopupView.mOffsetX
        val offsetY: Int = builderPopupView.mOffsetY

        companion object {
            const val GRAVITY_CENTER = 0
            const val GRAVITY_LEFT = 1
            const val GRAVITY_RIGHT = 2
            const val ALIGN_CENTER = 0
            const val ALIGN_LEFT = 1
            const val ALIGN_RIGHT = 2
            const val POSITION_ABOVE = 0
            const val POSITION_BELOW = 1
            const val POSITION_LEFT_TO = 3
            const val POSITION_RIGHT_TO = 4
        }

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(GRAVITY_CENTER, GRAVITY_LEFT, GRAVITY_RIGHT)
        annotation class Gravity

        @Gravity
        val textGravityTemp: Int = builderPopupView.mTextGravity
        val textGravity: Int
            get() = when (textGravityTemp) {
                GRAVITY_CENTER -> android.view.Gravity.CENTER
                GRAVITY_LEFT -> android.view.Gravity.START
                GRAVITY_RIGHT -> android.view.Gravity.END
                else -> android.view.Gravity.CENTER
            }

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(ALIGN_CENTER, ALIGN_LEFT, ALIGN_RIGHT)
        annotation class Align

        @Align
        val align: Int = builderPopupView.mAlign

        @Retention(AnnotationRetention.SOURCE)
        @IntDef(POSITION_ABOVE, POSITION_BELOW, POSITION_LEFT_TO, POSITION_RIGHT_TO)
        annotation class Position

        @Position
        var position: Int = builderPopupView.mPosition
        val isHideArrow: Boolean
            get() = !isShowArrow
        val isTextGravityCenter: Boolean
            get() = textGravity == android.view.Gravity.CENTER
        val isTextGravityLeft: Boolean
            get() = textGravity == android.view.Gravity.START
        val isTextGravityRight: Boolean
            get() = textGravity == android.view.Gravity.END
        val isAlignedCenter: Boolean
            get() = align == ALIGN_CENTER
        val isAlignedLeft: Boolean
            get() = align == ALIGN_LEFT
        val isAlignedRight: Boolean
            get() = align == ALIGN_RIGHT
        val isPositionedAbove: Boolean
            get() = position == POSITION_ABOVE
        val isPositionedBelow: Boolean
            get() = position == POSITION_BELOW
        val isPositionedLeftTo: Boolean
            get() = position == POSITION_LEFT_TO
        val isPositionedRightTo: Boolean
            get() = position == POSITION_RIGHT_TO
    }

    var animationDuration: Int = 400
    fun show(popupView: PopupView): View? = create(popupView)
        ?.apply { popupAppear(this, animationDuration.toLong()).start() }

    companion object {
        private val isRtl: Boolean
            get() = Character.getDirectionality(
                Locale.getDefault()
                    .run { getDisplayName(this)[0] }) == Character.DIRECTIONALITY_RIGHT_TO_LEFT
    }

    object BackgroundConstructor {
        fun setBackground(tipView: View, popupView: PopupView) = when {
            popupView.isHideArrow -> setToolTipBgNoArrow(tipView, popupView.backgroundColor)
            else -> when (popupView.position) {
                PopupView.POSITION_ABOVE -> setToolTipBgAbove(tipView, popupView)
                PopupView.POSITION_BELOW -> setToolTipBgBelow(tipView, popupView)
                PopupView.POSITION_LEFT_TO -> setToolTipBgLeftTo(tipView, popupView.backgroundColor)
                PopupView.POSITION_RIGHT_TO ->
                    setToolTipBgRightTo(tipView, popupView.backgroundColor)
                else -> Unit
            }
        }

        private fun setToolTipBgNoArrow(tipView: View, color: Int) =
            setBgTip(tipView, R.mipmap.tooltip_no_arrow, color)

        private fun setBgTip(tipView: View, drawableRes: Int, color: Int) =
            setBgView(tipView, getDrawableTinted(tipView.context, drawableRes, color))

        private fun setBgView(view: View, drawable: Drawable?) = when {
            aboveLollipop -> view.background = drawable
            else -> ViewCompat.setBackground(view, drawable)
        }

        private fun getDrawableTinted(context: Context, drawableRes: Int, color: Int): Drawable? =
            context.resources.run {
                when {
                    aboveLollipop -> getDrawable(drawableRes, null)?.apply { setTint(color) }
                    else -> getDrawable(drawableRes)
                        ?.apply { setColorFilter(color, PorterDuff.Mode.SRC_ATOP) }
                }
            }

        private fun setToolTipBgAbove(tipView: View, popupView: PopupView) =
            when (popupView.align) {
                PopupView.ALIGN_CENTER -> setBgTip(
                    tipView, R.mipmap.tooltip_arrow_down, popupView.backgroundColor
                )
                PopupView.ALIGN_LEFT -> setBgTip(
                    tipView, when {
                        isRtl -> R.mipmap.tooltip_arrow_down_right
                        else -> R.mipmap.tooltip_arrow_down_left
                    }, popupView.backgroundColor
                )
                PopupView.ALIGN_RIGHT -> setBgTip(
                    tipView, when {
                        isRtl -> R.mipmap.tooltip_arrow_down_left
                        else -> R.mipmap.tooltip_arrow_down_right
                    }, popupView.backgroundColor
                )
                else -> Unit
            }

        private fun setToolTipBgBelow(tipView: View, popupView: PopupView) =
            when (popupView.align) {
                PopupView.ALIGN_CENTER ->
                    setBgTip(tipView, R.mipmap.tooltip_arrow_up, popupView.backgroundColor)
                PopupView.ALIGN_LEFT -> setBgTip(
                    tipView, when {
                        isRtl -> R.mipmap.tooltip_arrow_up_right
                        else -> R.mipmap.tooltip_arrow_up_left
                    }, popupView.backgroundColor
                )
                PopupView.ALIGN_RIGHT -> setBgTip(
                    tipView, when {
                        isRtl -> R.mipmap.tooltip_arrow_up_left
                        else -> R.mipmap.tooltip_arrow_up_right
                    }, popupView.backgroundColor
                )
                else -> Unit
            }

        private fun setToolTipBgLeftTo(tipView: View, color: Int) = setBgTip(
            tipView, when {
                isRtl -> R.mipmap.tooltip_arrow_left
                else -> R.mipmap.tooltip_arrow_right
            }, color
        )

        private fun setToolTipBgRightTo(tipView: View, color: Int) = setBgTip(
            tipView, when {
                isRtl -> R.mipmap.tooltip_arrow_right
                else -> R.mipmap.tooltip_arrow_left
            }, color
        )
    }

    class Coordinates(view: View) {
        var left: Int = 0
        var top: Int = 0
        var right: Int = 0
        var bottom: Int = 0

        init {
            IntArray(2).apply { view.getLocationOnScreen(this) }.run {
                left = this[0]
                top = this[1]
            }
            right = left + view.width
            bottom = top + view.height
        }
    }

    object CoordinatesFinder {
        fun getCoordinates(tipView: TextView, popupView: PopupView): Point = popupView.run {
            tipView
                .measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            anchorView?.let { Coordinates(it) }?.let { anchorCoordinates ->
                rootView?.let { Coordinates(it) }?.let { rootCoordinates ->
                    when (position) {
                        PopupView.POSITION_ABOVE -> getPositionAbove(
                            tipView, this, anchorCoordinates, rootCoordinates
                        )
                        PopupView.POSITION_BELOW -> getPositionBelow(
                            tipView, this, anchorCoordinates, rootCoordinates
                        )
                        PopupView.POSITION_LEFT_TO -> getPositionLeftTo(
                            tipView, this, anchorCoordinates, rootCoordinates
                        )
                        PopupView.POSITION_RIGHT_TO -> getPositionRightTo(
                            tipView, this, anchorCoordinates, rootCoordinates
                        )
                        else -> Point()
                    }.apply {
                        x = x - rootView.paddingLeft + if (isRtl) -offsetX else offsetX
                        y = y - rootView.paddingTop + offsetY
                    }
                } ?: Point()
            } ?: Point()
        }

        private fun getPositionAbove(
            tipView: TextView, popupView: PopupView,
            anchorCoordinates: Coordinates, rootLocation: Coordinates
        ): Point = Point().apply {
            x = anchorCoordinates.left + getXOffset(tipView, popupView)
            popupView.rootView?.let {
                when {
                    popupView.isAlignedCenter -> adjustHorizontalCenteredOutOfBounds(
                        tipView, it, this, rootLocation
                    )
                    popupView.isAlignedLeft -> adjustHorizontalLeftAlignmentOutOfBounds(
                        tipView, it, this, anchorCoordinates, rootLocation
                    )
                    popupView.isAlignedRight -> adjustHorizontalRightAlignmentOutOfBounds(
                        tipView, it, this, anchorCoordinates, rootLocation
                    )
                }
            }
            y = anchorCoordinates.top - tipView.measuredHeight
        }

        private fun getXOffset(tipView: View, popupView: PopupView): Int =
            popupView.anchorView?.run {
                when (popupView.align) {
                    PopupView.ALIGN_CENTER -> (width - tipView.measuredWidth) / 2
                    PopupView.ALIGN_LEFT -> 0
                    PopupView.ALIGN_RIGHT -> width - tipView.measuredWidth
                    else -> 0
                }
            } ?: 0

        private fun adjustHorizontalCenteredOutOfBounds(
            tipView: TextView, root: ViewGroup, point: Point, rootLocation: Coordinates
        ) = root.run { width - paddingLeft - paddingRight }.let { rootWidth ->
            if (tipView.measuredWidth > rootWidth) {
                point.x = rootLocation.left + root.paddingLeft
                tipView.layoutParams.apply {
                    width = rootWidth
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
                measureViewWithFixedWidth(tipView, rootWidth)
            }
        }

        private fun measureViewWithFixedWidth(tipView: TextView, width: Int) = tipView.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        private fun adjustHorizontalLeftAlignmentOutOfBounds(
            tipView: TextView, root: ViewGroup, point: Point,
            anchorCoordinates: Coordinates, rootLocation: Coordinates
        ) = (rootLocation.right - root.paddingRight).let { rootRight ->
            if (point.x + tipView.measuredWidth > rootRight) tipView.layoutParams.apply {
                width = rootRight - anchorCoordinates.left
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }.let { measureViewWithFixedWidth(tipView, it.width) }
        }

        private fun adjustHorizontalRightAlignmentOutOfBounds(
            tipView: TextView, root: ViewGroup, point: Point,
            anchorCoordinates: Coordinates, rootLocation: Coordinates
        ) = (rootLocation.left + root.paddingLeft).let { rootLeft ->
            if (point.x < rootLeft) {
                point.x = rootLeft
                tipView.layoutParams.apply {
                    width = anchorCoordinates.right - rootLeft
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }.let { measureViewWithFixedWidth(tipView, it.width) }
            }
        }

        private fun getPositionBelow(
            tipView: TextView, popupView: PopupView,
            anchorCoordinates: Coordinates, rootLocation: Coordinates
        ): Point = Point().apply {
            x = anchorCoordinates.left + getXOffset(tipView, popupView)
            popupView.rootView?.let {
                when {
                    popupView.isAlignedCenter -> adjustHorizontalCenteredOutOfBounds(
                        tipView, it, this, rootLocation
                    )
                    popupView.isAlignedLeft -> adjustHorizontalLeftAlignmentOutOfBounds(
                        tipView, it, this, anchorCoordinates, rootLocation
                    )
                    popupView.isAlignedRight -> adjustHorizontalRightAlignmentOutOfBounds(
                        tipView, it, this, anchorCoordinates, rootLocation
                    )
                }
            }
            y = anchorCoordinates.bottom
        }

        private fun getPositionLeftTo(
            tipView: TextView, popupView: PopupView,
            anchorCoordinates: Coordinates, rootLocation: Coordinates
        ): Point = Point().apply {
            x = anchorCoordinates.left - tipView.measuredWidth
            popupView.rootView?.let {
                adjustLeftToOutOfBounds(tipView, it, this, anchorCoordinates, rootLocation)
            }
            y = anchorCoordinates.top + getYCenteringOffset(tipView, popupView)
        }

        private fun adjustLeftToOutOfBounds(
            tipView: TextView, root: ViewGroup, point: Point,
            anchorCoordinates: Coordinates, rootLocation: Coordinates
        ) = (rootLocation.left + root.paddingLeft).let { rootLeft ->
            if (point.x < rootLeft) {
                point.x = rootLeft
                tipView.layoutParams.apply {
                    width = anchorCoordinates.left - rootLeft
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }.let { measureViewWithFixedWidth(tipView, it.width) }
            }
        }

        private fun getYCenteringOffset(tipView: View, popupView: PopupView): Int =
            popupView.anchorView?.run { (height - tipView.measuredHeight) / 2 } ?: 0

        private fun getPositionRightTo(
            tipView: TextView, popupView: PopupView,
            anchorCoordinates: Coordinates, rootLocation: Coordinates
        ): Point = Point().apply {
            x = anchorCoordinates.right
            popupView.rootView?.let {
                adjustRightToOutOfBounds(tipView, it, this, anchorCoordinates, rootLocation)
            }
            y = anchorCoordinates.top + getYCenteringOffset(tipView, popupView)
        }

        private fun adjustRightToOutOfBounds(
            tipView: TextView, root: ViewGroup, point: Point,
            anchorCoordinates: Coordinates, rootLocation: Coordinates
        ) = (rootLocation.right - root.paddingRight).let { rootRight ->
            if (point.x + tipView.measuredWidth > rootRight) tipView.layoutParams.apply {
                width = rootRight - anchorCoordinates.right
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }.let { measureViewWithFixedWidth(tipView, it.width) }
        }
    }

    private val tipsMap: MutableMap<Int, View> = mutableMapOf()
    private fun create(popupView: PopupView): View? = popupView.anchorView?.id?.let { anchorId ->
        popupView.rootView?.let { root ->
            when {
                tipsMap.containsKey(anchorId) -> tipsMap[anchorId]
                else -> createTipView(popupView).apply {
                    if (isRtl) switchToolTipSidePosition(popupView)
                    BackgroundConstructor.setBackground(this, popupView)
                    root.addView(this)
                    moveTipToCorrectPosition(
                        this, CoordinatesFinder.getCoordinates(this, popupView)
                    )
                    setOnClickListener { view -> dismiss(view, true) }
                    tag = anchorId
                    tipsMap[anchorId] = this
                }
            }
        }
    }

    private fun createTipView(popupView: PopupView): TextView = TextView(popupView.context).apply {
        text = popupView.message ?: popupView.spannableMessage
        setTextColor(popupView.textColor)
        textSize = popupView.textSize.toFloat()
        setTipViewElevation(this, popupView)
        gravity = popupView.textGravity
        visibility = View.INVISIBLE
    }

    private fun setTipViewElevation(textView: TextView, popupView: PopupView) {
        if (aboveLollipop && popupView.elevation > 0) textView.apply {
            elevation = popupView.elevation
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) = outline.setEmpty()
            }
        }
    }

    private fun switchToolTipSidePosition(popupView: PopupView) = popupView.apply {
        position = when {
            popupView.isPositionedLeftTo -> PopupView.POSITION_RIGHT_TO
            popupView.isPositionedRightTo -> PopupView.POSITION_LEFT_TO
            else -> -1
        }
    }

    private fun moveTipToCorrectPosition(textView: TextView, point: Point): TextView =
        textView.apply {
            Coordinates(textView).run {
                translationY = (point.y - top).toFloat()
                translationX = (point.x - left).toFloat().let { if (isRtl) -it else it }
            }
        }

    val clearTipsMap = tipsMap.run {
        if (isNotEmpty()) for ((_, view) in this) {
            dismiss(view, false)
        }
        clear()
    }

    fun findAndDismiss(anchorView: View): Boolean =
        find(anchorView.id)?.let { dismiss(it, false) } ?: false

    fun find(key: Int?): View? = if (tipsMap.containsKey(key)) tipsMap[key] else null
    fun dismiss(key: Int?): Boolean = tipsMap.containsKey(key) && dismiss(tipsMap[key], false)
    fun dismiss(tipView: View?, byUser: Boolean): Boolean = tipView?.run {
        when {
            isVisible(this) -> {
                tipsMap.remove(tag as Int)
                animateDismiss(this, byUser)
                true
            }
            else -> false
        }
    } ?: false

    fun isVisible(tipView: View): Boolean = tipView.visibility == View.VISIBLE
    private fun animateDismiss(view: View, byUser: Boolean) = popupDisappear(
        view, animationDuration.toLong(), object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mTipListener?.onTipDismissed(view, view.tag as Int, byUser)
            }
        }).start()
}