package top.autoget.autokit

import android.R
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build
import android.text.TextPaint
import android.text.TextUtils
import android.util.StateSet
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.VersionKit.aboveJellyBeanMR1
import kotlin.math.*

object ViewKit {
    private var popupWindow: PopupWindow? = null
    fun showPopupWindow(context: Context, resId: Int, root: View, paramsType: Int): View =
        when (paramsType) {
            1 -> PopupWindow(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            2 -> PopupWindow(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            3 -> PopupWindow(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            4 -> PopupWindow(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
            else -> PopupWindow(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        }.apply {
            contentView = LayoutInflater.from(context).inflate(resId, null)
            isFocusable = true
            isOutsideTouchable = true
            isTouchable = true
            setBackgroundDrawable(BitmapDrawable())
            popupWindow = this
            showAsDropDown(root)
        }.contentView

    fun dismissPopupWindow() = popupWindow?.run {
        if (isShowing) {
            dismiss()
            popupWindow = null
        }
    }

    fun setTVUnderLine(textView: TextView): TextPaint = textView.paint.apply {
        flags = Paint.UNDERLINE_TEXT_FLAG
        isAntiAlias = true
    }

    @JvmOverloads
    fun setViewEnabled(view: View?, enabled: Boolean, vararg excludes: View = emptyArray()) {
        view?.apply {
            if (excludes.isNotEmpty()) for (exclude in excludes) {
                if (exclude === this) return
            }
            if (this is ViewGroup) for (i in 0 until childCount) {
                setViewEnabled(getChildAt(i), enabled, *excludes)
            }
            isEnabled = enabled
        }
    }

    val isLayoutRtl: Boolean
        get() = when {
            aboveJellyBeanMR1 -> TextUtils.getLayoutDirectionFromLocale(
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Resources.getSystem().configuration.locales[0]
                    else -> Resources.getSystem().configuration.locale
                }
            ) == View.LAYOUT_DIRECTION_RTL
            else -> false
        }

    fun isTouchInView(motionEvent: MotionEvent, view: View): Boolean =
        IntArray(2).apply { view.getLocationOnScreen(this) }.let {
            motionEvent.rawX >= it[0] && motionEvent.rawX <= it[0] + view.width &&
                    motionEvent.rawY >= it[1] && motionEvent.rawY <= it[1] + view.height
        }

    fun fixScrollViewTopping(view: View) {
        if (view is ViewGroup) view.apply { isFocusable = false }.run {
            for (i in 0..childCount) {
                (getChildAt(i).apply { isFocusable = false } as? ViewGroup)
                    ?.let { fixScrollViewTopping(it) }
            }
        }
    }

    fun removeSelfFromParent(view: View?) = view?.run { (parent as ViewGroup).removeView(this) }
    fun requestLayoutParent(view: View, isAll: Boolean): ViewParent? {
        var parent: ViewParent? = view.parent
        while (parent != null && parent is View) {
            if (!(parent as ViewGroup).isLayoutRequested) {
                parent = if (isAll) parent.apply { requestLayout() }.parent else return parent
            }
        }
        return parent
    }

    fun getActivity(view: View): Activity {
        var context: Context = view.context
        while (context is ContextWrapper) {
            context = when (context) {
                is Activity -> return context
                else -> context.baseContext
            }
        }
        throw IllegalStateException("View $view is not attached to an Activity")
    }

    fun bitmapView(view: View): Bitmap =
        view.run { Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) }.apply {
            Canvas(this).let { canvas ->
                view.run {
                    background?.draw(canvas) ?: canvas.drawColor(Color.WHITE)
                    draw(canvas)
                }
            }
        }

    fun shootView(view: View): Bitmap = view.apply {
        measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        layout(0, 0, measuredWidth, measuredHeight)
        buildDrawingCache()
    }.drawingCache

    fun captureView(view: View): Bitmap = view.apply {
        isDrawingCacheEnabled = true
        buildDrawingCache()
    }.drawingCache

    fun captureActivity(activity: Activity): Bitmap =
        activity.window.decorView.findViewById<View>(android.R.id.content)
            .apply { isDrawingCacheEnabled = true }.drawingCache

    fun bigImage(bitmap: Bitmap, big: Float): Bitmap = bitmap.run {
        Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { postScale(big, big) }, true)
    }

    object ViewTouch {
        const val UNKNOWN = 0
        const val LEFT = 1
        const val UP = 2
        const val RIGHT = 4
        const val DOWN = 8

        @IntDef(LEFT, UP, RIGHT, DOWN)
        @Retention(AnnotationRetention.SOURCE)
        annotation class Direction

        fun setOnTouchListener(view: View?, listener: OnTouchUtilsListener?) =
            listener?.let { view?.setOnTouchListener(it) }

        abstract class OnTouchUtilsListener : View.OnTouchListener {
            init {
                resetTouch()
            }

            companion object {
                private const val STATE_DOWN = 0
                private const val STATE_MOVE = 1
                private const val STATE_STOP = 2
            }

            private var downX = -1
            private var downY = -1
            private var lastX = -1
            private var lastY = -1
            private var state = STATE_DOWN
            private var direction = UNKNOWN
            private var velocityTracker: VelocityTracker? = null
            private fun resetTouch(x: Int = -1, y: Int = -1) {
                downX = x
                downY = y
                lastX = x
                lastY = y
                state = STATE_DOWN
                direction = UNKNOWN
                velocityTracker?.clear()
            }

            abstract fun onDown(view: View?, x: Int, y: Int, event: MotionEvent?): Boolean
            abstract fun onMove(
                view: View?, @Direction direction: Int, x: Int, y: Int, dx: Int, dy: Int,
                totalX: Int, totalY: Int, event: MotionEvent?
            ): Boolean

            abstract fun onStop(
                view: View?, @Direction direction: Int, x: Int, y: Int,
                totalX: Int, totalY: Int, vx: Int, vy: Int, event: MotionEvent?
            ): Boolean

            private var touchSlop = 0
            private var maxFlingVelocity = 0
            private var minFlingVelocity = 0
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (touchSlop == 0) touchSlop = ViewConfiguration.get(v.context).scaledTouchSlop
                if (maxFlingVelocity == 0)
                    maxFlingVelocity = ViewConfiguration.get(v.context).scaledMaximumFlingVelocity
                if (minFlingVelocity == 0)
                    minFlingVelocity = ViewConfiguration.get(v.context).scaledMinimumFlingVelocity
                if (velocityTracker == null) velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                return when (event.action) {
                    MotionEvent.ACTION_DOWN -> onDown(v, event)
                    MotionEvent.ACTION_MOVE -> onMove(v, event)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> onStop(v, event)
                    else -> false
                }
            }

            fun onDown(view: View, event: MotionEvent): Boolean {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()
                resetTouch(x, y)
                return onDown(view.apply { isPressed = true }, x, y, event)
            }

            fun onMove(view: View, event: MotionEvent): Boolean {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()
                if (downX == -1) {
                    resetTouch(x, y)
                    view.apply { isPressed = true }
                }
                if (state != STATE_MOVE) when {
                    abs(x - lastX) < touchSlop && abs(y - lastY) < touchSlop -> return true
                    else -> {
                        state = STATE_MOVE
                        direction = when {
                            abs(x - lastX) < abs(y - lastY) -> if (y - lastY < 0) UP else DOWN
                            else -> if (x - lastX < 0) LEFT else RIGHT
                        }
                    }
                }
                val consumeMove: Boolean =
                    onMove(view, direction, x, y, x - lastX, y - lastY, x - downX, y - downY, event)
                lastX = x
                lastY = y
                return consumeMove
            }

            fun onStop(view: View, event: MotionEvent): Boolean {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()
                var vx = 0
                var vy = 0
                velocityTracker?.apply { computeCurrentVelocity(1000, maxFlingVelocity.toFloat()) }
                    ?.run {
                        xVelocity.toInt().let { vx = if (abs(it) < minFlingVelocity) 0 else it }
                        yVelocity.toInt().let { vy = if (abs(it) < minFlingVelocity) 0 else it }
                        recycle()
                        velocityTracker = null
                    }
                view.apply { isPressed = false }
                val consumeStop =
                    onStop(view, direction, x, y, x - downX, y - downY, vx, vy, event)
                if (event.action == MotionEvent.ACTION_UP && state == STATE_DOWN) when {
                    event.eventTime - event.downTime > 1000 -> view.performLongClick()
                    else -> view.performClick()
                }
                resetTouch(-1, -1)
                return consumeStop
            }
        }
    }

    object ViewClick {
        @JvmOverloads
        fun applyPressedViewScale(views: Array<View>?, scaleFactors: FloatArray? = null) =
            views?.run {
                if (isNotEmpty()) {
                    for ((index, view) in views.withIndex()) {
                        when {
                            scaleFactors != null && index < scaleFactors.size ->
                                applyPressedViewScale(view, scaleFactors[index])
                            else -> applyPressedViewScale(view, -0.06f)
                        }
                    }
                }
            }

        private object OnHelperTouchListener : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        processScale(v, true)
                        processAlpha(v, true)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        processScale(v, false)
                        processAlpha(v, false)
                    }
                }
                return false
            }

            private fun processScale(view: View, isDown: Boolean) =
                (view.getTag(TAG_PRESSED_VIEW_SCALE) as? Float)?.run { (if (isDown) 1 + this else 1f) }
                    ?.let { view.animate().scaleX(it).scaleY(it).setDuration(200).start() }

            private fun processAlpha(view: View, isDown: Boolean) =
                (view.getTag(if (isDown) TAG_PRESSED_VIEW_ALPHA else TAG_PRESSED_VIEW_SRC) as? Float)
                    ?.run { view.alpha = this }
        }

        private const val TAG_PRESSED_VIEW_SCALE = -1
        fun applyPressedViewScale(view: View, scaleFactor: Float?) = view.apply {
            setTag(TAG_PRESSED_VIEW_SCALE, scaleFactor)
            isClickable = true
            setOnTouchListener(OnHelperTouchListener)
        }

        @JvmOverloads
        fun applyPressedViewAlpha(views: Array<View>?, alphas: FloatArray? = null) = views?.run {
            if (isNotEmpty()) for ((index, view) in views.withIndex()) {
                when {
                    alphas != null && index < alphas.size ->
                        applyPressedViewAlpha(view, alphas[index])
                    else -> applyPressedViewAlpha(view, 0.8f)
                }
            }
        }

        private const val TAG_PRESSED_VIEW_ALPHA = -2
        private const val TAG_PRESSED_VIEW_SRC = -3
        fun applyPressedViewAlpha(view: View, alpha: Float) = view.apply {
            setTag(TAG_PRESSED_VIEW_ALPHA, alpha)
            setTag(TAG_PRESSED_VIEW_SRC, view.alpha)
            isClickable = true
            setOnTouchListener(OnHelperTouchListener)
        }

        @JvmOverloads
        fun applyPressedBgAlpha(view: View?, alphaValue: Float = 0.9f) =
            applyPressedBgStyle(view, TAG_PRESSED_BG_ALPHA, alphaValue)

        @JvmOverloads
        fun applyPressedBgDark(view: View?, alphaDark: Float = 0.9f) =
            applyPressedBgStyle(view, TAG_PRESSED_BG_DARK, alphaDark)

        private fun applyPressedBgStyle(view: View?, style: Int, alpha: Float) = view?.let {
            when (val tag = view.getTag(-style)) {
                is Drawable -> ViewCompat.setBackground(view, tag)
                else -> createDrawableStyle(view.background, style, alpha).let { background ->
                    ViewCompat.setBackground(view, background)
                    view.setTag(-style, background)
                }
            }
        }

        private const val TAG_PRESSED_BG_ALPHA = 4
        private const val TAG_PRESSED_BG_DARK = 5
        private fun createDrawableStyle(src: Drawable?, style: Int, alpha: Float): Drawable =
            src?.constantState?.let {
                it.newDrawable().mutate().let { drawable ->
                    when (style) {
                        TAG_PRESSED_BG_ALPHA -> createDrawableAlpha(drawable, alpha)
                        TAG_PRESSED_BG_DARK -> createDrawableDark(drawable, alpha)
                        else -> drawable
                    }.let { pressed ->
                        StateListDrawable().apply {
                            addState(intArrayOf(R.attr.state_pressed), pressed)
                            addState(
                                intArrayOf(-R.attr.state_enabled),
                                createDrawableAlpha(pressed, 0.5f)
                            )
                            addState(StateSet.WILD_CARD, src)
                        }
                    }
                }
            } ?: src ?: ColorDrawable(0)

        private fun createDrawableAlpha(drawable: Drawable, alphaValue: Float): Drawable = when {
            VersionKit.aboveKitKatWatch || drawable is ColorDrawable ->
                drawable.apply { alpha = (alphaValue * 255).toInt() }
            else -> drawable.apply {
                alpha = (alphaValue * 255).toInt()
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            }.run {
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888).let {
                    draw(Canvas(it))
                    BitmapDrawable(Resources.getSystem(), it)
                }
            }
        }

        private fun createDrawableDark(drawable: Drawable, alphaDark: Float): Drawable = when {
            VersionKit.aboveKitKatWatch || drawable is ColorDrawable ->
                drawable.apply { colorFilter = getColorFilterDark(alphaDark) }
            else -> drawable.apply {
                colorFilter = getColorFilterDark(alphaDark)
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            }.run {
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888).let {
                    draw(Canvas(it))
                    BitmapDrawable(Resources.getSystem(), it)
                }
            }
        }

        private fun getColorFilterDark(alphaDark: Float): ColorMatrixColorFilter = floatArrayOf(
            alphaDark, 0f, 0f, 0f, 0f, 0f,
            alphaDark, 0f, 0f, 0f, 0f, 0f,
            alphaDark, 0f, 0f, 0f, 0f, 0f, 2f, 0f
        ).let { ColorMatrixColorFilter(ColorMatrix(it)) }

        @JvmOverloads
        fun applyDebouncingGlobal(
            view: View, listener: View.OnClickListener?, @IntRange(from = 0) duration: Long = 200
        ) = applyDebouncingGlobal(arrayOf(view), listener, duration)

        @JvmOverloads
        fun applyDebouncingGlobal(
            views: Array<View>?, listener: View.OnClickListener?,
            @IntRange(from = 0) duration: Long = 200
        ) = applyDebouncingBase(views, listener, duration, true)

        private const val TAG_DEBOUNCING = -7

        abstract class OnDebouncingClickListener @JvmOverloads constructor(
            private val duration: Long = 200, private val isGlobal: Boolean = true
        ) : View.OnClickListener {
            companion object {
                private var enabled = true
                private val enableAgain = Runnable { enabled = true }
                private fun isValid(view: View, duration: Long): Boolean {
                    val time = nowMillis
                    return when (val tag = view.getTag(TAG_DEBOUNCING)) {
                        is Long -> when {
                            time - tag > duration ->
                                true.apply { view.setTag(TAG_DEBOUNCING, time) }
                            else -> false
                        }
                        else -> true.apply { view.setTag(TAG_DEBOUNCING, time) }
                    }
                }
            }

            abstract fun onDebouncingClick(v: View?)
            override fun onClick(v: View) = when {
                isGlobal -> if (enabled) {
                    enabled = false
                    v.postDelayed(enableAgain, duration)
                    onDebouncingClick(v)
                } else Unit
                else -> if (isValid(v, duration)) onDebouncingClick(v) else Unit
            }
        }

        private fun applyDebouncingBase(
            views: Array<View>?, listener: View.OnClickListener?,
            @IntRange(from = 0) duration: Long, isGlobal: Boolean
        ) = listener?.let {
            views?.run {
                if (isNotEmpty()) for (view in this) {
                    view.setOnClickListener(object : OnDebouncingClickListener(duration, isGlobal) {
                        override fun onDebouncingClick(v: View?) = it.onClick(v)
                    })
                }
            }
        }

        @JvmOverloads
        fun applyDebouncingSingle(
            view: View, listener: View.OnClickListener?, @IntRange(from = 0) duration: Long = 200
        ) = applyDebouncingSingle(arrayOf(view), listener, duration)

        @JvmOverloads
        fun applyDebouncingSingle(
            views: Array<View>?, listener: View.OnClickListener?,
            @IntRange(from = 0) duration: Long = 200
        ) = applyDebouncingBase(views, listener, duration, false)

        abstract class OnMultiClickListener @JvmOverloads constructor(
            private val triggerClickCount: Int, private val clickInterval: Long = 666
        ) : View.OnClickListener {
            private var lastClickTime: Long = 0
            private var clickCount = 0
            abstract fun onTriggerClick(v: View?)
            abstract fun onBeforeTriggerClick(v: View?, count: Int)
            override fun onClick(v: View) = when {
                triggerClickCount > 1 -> {
                    val time: Long = nowMillis
                    when {
                        time - lastClickTime < clickInterval -> {
                            clickCount++
                            when {
                                clickCount == triggerClickCount -> onTriggerClick(v)
                                clickCount < triggerClickCount ->
                                    onBeforeTriggerClick(v, clickCount)
                                else -> {
                                    clickCount = 1
                                    onBeforeTriggerClick(v, clickCount)
                                }
                            }
                        }
                        else -> {
                            clickCount = 1
                            onBeforeTriggerClick(v, clickCount)
                        }
                    }
                    lastClickTime = time
                }
                else -> onTriggerClick(v)
            }
        }
    }

    object ViewShadow {
        fun apply(vararg views: View?) {
            for (view in views) {
                apply(view, ConfigShadow())
            }
        }

        private const val TAG_SHADOW = -16
        fun apply(view: View?, configShadow: ConfigShadow?) = view?.let {
            configShadow?.let {
                when (val tag = view.getTag(TAG_SHADOW)) {
                    is Drawable -> ViewCompat.setBackground(view, tag)
                    else -> configShadow.apply(view.background).let { background ->
                        ViewCompat.setBackground(view, background)
                        view.setTag(TAG_SHADOW, background)
                    }
                }
            }
        }

        class ConfigShadow {
            private var isCircle = false
            fun setCircle(): ConfigShadow = apply {
                isCircle = true
                require(radius == -1f) { "Set circle needn't set radius." }
            }

            private var radius = -1f
                get() = if (field == -1f) 0f else field
                set(radius) {
                    field = radius
                    require(!isCircle) { "Set circle needn't set radius." }
                }
            private var sizeNormal = -1f
                get() = if (field == -1f) DensityKit.dip2px(8f).toFloat() else field
                set(sizeNormal) {
                    field = if (sizeNormal == -1f) sizePressed else sizeNormal
                }
            private var sizePressed = -1f
                get() = if (field == -1f) sizeNormal else field
                set(sizePressed) {
                    field = if (sizePressed == -1f) sizeNormal else sizePressed
                }
            private var sizeMaxNormal = -1f
                get() = if (field == -1f) sizeNormal else field
                set(maxSizeNormal) {
                    field = if (maxSizeNormal == -1f) sizeNormal else maxSizeNormal
                }
            private var sizeMaxPressed = -1f
                get() = if (field == -1f) sizePressed else field
                set(maxSizePressed) {
                    field = if (maxSizePressed == -1f) sizePressed else maxSizePressed
                }
            private val colorDefault = -0x50000000
            private var colorNormal = colorDefault
                get() = if (field == colorDefault) colorPressed else field
                set(colorNormal) {
                    field = if (colorNormal == colorDefault) colorPressed else colorNormal
                }
            private var colorPressed = colorDefault
                get() = if (field == colorDefault) colorNormal else field
                set(colorPressed) {
                    field = if (colorPressed == colorDefault) colorNormal else colorPressed
                }

            fun apply(src: Drawable?): Drawable = StateListDrawable().apply {
                (src ?: ColorDrawable(Color.TRANSPARENT)).let { drawable ->
                    ShadowDrawable(
                        drawable, radius, sizeNormal, sizeMaxNormal, colorPressed, isCircle
                    ).run { addState(intArrayOf(R.attr.state_pressed), this) }
                    ShadowDrawable(
                        drawable, radius, sizePressed, sizeMaxPressed, colorNormal, isCircle
                    ).run { addState(StateSet.WILD_CARD, this) }
                }
            }
        }

        class ShadowDrawable(
            content: Drawable?, radius: Float, shadowSize: Float, maxShadowSize: Float,
            private val shadowStartColor: Int, private val isCircle: Boolean
        ) : DrawableWrapper(content) {
            private var shadowMultiplier = 1f
            private var shadowTopScale = 1f
            private var shadowHorizontalScale = 1f
            private var shadowBottomScale = 1f

            init {
                if (isCircle) {
                    shadowMultiplier = 1f
                    shadowTopScale = 1f
                    shadowHorizontalScale = 1f
                    shadowBottomScale = 1f
                }
                setShadowSize(shadowSize, maxShadowSize)
            }

            var shadowSizeRaw = 0f
                set(size) = setShadowSize(size, shadowSizeMax)
            var shadowSizeMax = 0f
                set(size) = setShadowSize(shadowSizeRaw, size)
            private var mShadowSize = 0f
            private var mMaxShadowSize = 0f
            private var isDirty = true
            fun setShadowSize(shadowSize: Float, maxShadowSize: Float) {
                require(!(shadowSize < 0 || maxShadowSize < 0)) { "invalid shadow size" }
                var sizeShadow = toEven(shadowSize).toFloat()
                val sizeShadowMax = toEven(maxShadowSize).toFloat()
                if (sizeShadow > sizeShadowMax) sizeShadow = sizeShadowMax
                if (shadowSizeRaw != sizeShadow || shadowSizeMax != sizeShadowMax) {
                    shadowSizeRaw = sizeShadow
                    shadowSizeMax = sizeShadowMax
                    mShadowSize = (sizeShadow * shadowMultiplier).roundToLong().toFloat()
                    mMaxShadowSize = sizeShadowMax
                    isDirty = true
                    invalidateSelf()
                }
            }

            private fun toEven(value: Float): Int =
                value.roundToInt().let { if (it % 2 == 1) it - 1 else it }

            override fun onBoundsChange(bounds: Rect) = run { isDirty = true }
            var cornerRadius: Float = radius.roundToLong().toFloat()
                set(radius) = radius.roundToLong().toFloat().let {
                    if (field != it) {
                        field = it
                        isDirty = true
                        invalidateSelf()
                    }
                }
            private var addPaddingForCorners = false
                set(addPaddingForCorners) {
                    field = addPaddingForCorners
                    invalidateSelf()
                }

            override fun getPadding(padding: Rect): Boolean = ceil(
                calculatePaddingH(shadowSizeMax, cornerRadius, addPaddingForCorners)
            ).toInt().let { hOffset ->
                ceil(calculatePaddingV(shadowSizeMax, cornerRadius, addPaddingForCorners)).toInt()
                    .let { vOffset -> true.apply { padding[hOffset, vOffset, hOffset] = vOffset } }
            }

            private val cos45 = cos(Math.toRadians(45.0)).toFloat()
            private fun calculatePaddingH(
                maxShadowSize: Float, cornerRadius: Float, addPaddingForCorners: Boolean
            ): Float = when {
                addPaddingForCorners -> maxShadowSize + (1 - cos45) * cornerRadius
                else -> maxShadowSize
            }

            private fun calculatePaddingV(
                maxShadowSize: Float, cornerRadius: Float, addPaddingForCorners: Boolean
            ): Float = when {
                addPaddingForCorners -> maxShadowSize * shadowMultiplier + (1 - cos45) * cornerRadius
                else -> maxShadowSize * shadowMultiplier
            }

            val minWidth: Float
                get() = 2 * max(
                    shadowSizeMax, cornerRadius + shadowSizeMax / 2
                ) + shadowSizeMax * 2
            val minHeight: Float
                get() = 2 * max(
                    shadowSizeMax, cornerRadius + shadowSizeMax * shadowMultiplier / 2
                ) + shadowSizeMax * shadowMultiplier * 2

            override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
            private val cornerShadowPaint: Paint = Paint().apply {
                flags = Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG
                style = Paint.Style.FILL
            }
            private val edgeShadowPaint: Paint =
                Paint(cornerShadowPaint).apply { isAntiAlias = false }

            override fun setAlpha(alpha: Int) {
                super.setAlpha(alpha)
                cornerShadowPaint.alpha = alpha
                edgeShadowPaint.alpha = alpha
            }

            override fun draw(canvas: Canvas) {
                if (isDirty) {
                    buildComponents(bounds)
                    isDirty = false
                }
                drawShadow(canvas)
                super.draw(canvas)
            }

            private fun buildComponents(bounds: Rect) {
                if (isCircle) cornerRadius = bounds.width() / 2.toFloat()
                (shadowSizeMax * shadowMultiplier).let { verticalOffset ->
                    contentBounds[bounds.left + shadowSizeMax, bounds.top + verticalOffset, bounds.right - shadowSizeMax] =
                        bounds.bottom - verticalOffset
                }
                contentBounds.run {
                    wrappedDrawable
                        ?.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                }
                buildShadowCorners()
            }

            private val contentBounds: RectF = RectF()
            private var cornerShadowPath: Path? = null
            private val shadowEndColor = 0
            private fun buildShadowCorners() = when {
                isCircle -> {
                    val size = contentBounds.width() / 2 - 1f
                    val innerBounds = RectF(-size, -size, size, size)
                    val outerBounds = RectF(innerBounds).apply { inset(-mShadowSize, -mShadowSize) }
                    cornerShadowPath?.run { reset() } ?: run { cornerShadowPath = Path() }
                    cornerShadowPath?.apply {
                        fillType = Path.FillType.EVEN_ODD
                        moveTo(-size, 0f)
                        rLineTo(-mShadowSize, 0f)
                        arcTo(outerBounds, 180f, 180f, false)
                        arcTo(outerBounds, 360f, 180f, false)
                        arcTo(innerBounds, 540f, -180f, false)
                        arcTo(innerBounds, 360f, -180f, false)
                    }?.close()
                    val shadowRadius = -outerBounds.top
                    if (shadowRadius > 0f) cornerShadowPaint.shader = RadialGradient(
                        0f, 0f, shadowRadius, intArrayOf(0, shadowStartColor, shadowEndColor),
                        floatArrayOf(0f, size / shadowRadius, 1f), Shader.TileMode.CLAMP
                    ) else Unit
                }
                else -> {
                    val innerBounds =
                        RectF(-cornerRadius, -cornerRadius, cornerRadius, cornerRadius)
                    val outerBounds = RectF(innerBounds).apply { inset(-mShadowSize, -mShadowSize) }
                    cornerShadowPath?.run { reset() } ?: run { cornerShadowPath = Path() }
                    cornerShadowPath?.apply {
                        fillType = Path.FillType.EVEN_ODD
                        moveTo(-cornerRadius, 0f)
                        rLineTo(-mShadowSize, 0f)
                        arcTo(outerBounds, 180f, 90f, false)
                        arcTo(innerBounds, 270f, -90f, false)
                    }?.close()
                    val shadowRadius = -outerBounds.top
                    if (shadowRadius > 0f) cornerShadowPaint.shader = RadialGradient(
                        0f, 0f, shadowRadius, intArrayOf(0, shadowStartColor, shadowEndColor),
                        floatArrayOf(0f, cornerRadius / shadowRadius, 1f), Shader.TileMode.CLAMP
                    )
                    edgeShadowPaint.run {
                        shader = LinearGradient(
                            0f, innerBounds.top, 0f, outerBounds.top,
                            intArrayOf(shadowStartColor, shadowEndColor),
                            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
                        )
                        isAntiAlias = false
                    }
                }
            }

            private var rotation = 0f
                set(rotation) {
                    if (field != rotation) {
                        field = rotation
                        invalidateSelf()
                    }
                }

            private fun drawShadow(canvas: Canvas) = cornerShadowPath?.let { path ->
                when {
                    isCircle -> canvas.save().let { saved ->
                        canvas.apply {
                            translate(contentBounds.centerX(), contentBounds.centerY())
                            drawPath(path, cornerShadowPaint)
                        }.restoreToCount(saved)
                    }
                    else -> {
                        val edgeTop: Float = -cornerRadius - mShadowSize
                        val offset: Float = cornerRadius
                        val drawEdgesH: Boolean = contentBounds.width() - 2 * offset > 0
                        val drawEdgesV: Boolean = contentBounds.height() - 2 * offset > 0
                        val offsetTop: Float = shadowSizeRaw - shadowSizeRaw * shadowTopScale
                        val offsetH = shadowSizeRaw - shadowSizeRaw * shadowHorizontalScale
                        val offsetBottom = shadowSizeRaw - shadowSizeRaw * shadowBottomScale
                        val scaleTop: Float =
                            if (offset == 0f) 1f else offset / (offset + offsetTop)
                        val scaleH: Float = if (offset == 0f) 1f else offset / (offset + offsetH)
                        val scaleBottom: Float =
                            if (offset == 0f) 1f else offset / (offset + offsetBottom)
                        val savedRotate: Int = canvas.save()
                        val saved: Int = canvas.apply {
                            rotate(rotation, contentBounds.centerX(), contentBounds.centerY())
                        }.save()
                        canvas.apply {
                            translate(contentBounds.left + offset, contentBounds.top + offset)
                            scale(scaleH, scaleTop)
                            drawPath(path, cornerShadowPaint)
                            if (drawEdgesH) {
                                scale(1f / scaleH, 1f)
                                drawRect(
                                    0f, edgeTop, contentBounds.width() - 2 * offset,
                                    -cornerRadius, edgeShadowPaint
                                )
                            }//TE
                        }.restoreToCount(saved)//LT
                        canvas.apply {
                            translate(contentBounds.right - offset, contentBounds.bottom - offset)
                            scale(scaleH, scaleBottom)
                            rotate(180f)
                            drawPath(path, cornerShadowPaint)
                            if (drawEdgesH) {
                                scale(1f / scaleH, 1f)
                                drawRect(
                                    0f, edgeTop, contentBounds.width() - 2 * offset,
                                    -cornerRadius, edgeShadowPaint
                                )
                            }//BE
                        }.restoreToCount(saved)//RB
                        canvas.apply {
                            translate(contentBounds.left + offset, contentBounds.bottom - offset)
                            scale(scaleH, scaleBottom)
                            rotate(270f)
                            drawPath(path, cornerShadowPaint)
                            if (drawEdgesV) {
                                scale(1f / scaleBottom, 1f)
                                drawRect(
                                    0f, edgeTop, contentBounds.height() - 2 * offset,
                                    -cornerRadius, edgeShadowPaint
                                )
                            }//LE
                        }.restoreToCount(saved)//LB
                        canvas.apply {
                            translate(contentBounds.right - offset, contentBounds.top + offset)
                            scale(scaleH, scaleTop)
                            rotate(90f)
                            drawPath(path, cornerShadowPaint)
                            if (drawEdgesV) {
                                scale(1f / scaleTop, 1f)
                                drawRect(
                                    0f, edgeTop, contentBounds.height() - 2 * offset,
                                    -cornerRadius, edgeShadowPaint
                                )
                            }//RE
                        }.restoreToCount(saved)//RT
                        canvas.restoreToCount(savedRotate)
                    }
                }
            }
        }

        open class DrawableWrapper(var wrappedDrawable: Drawable?) : Drawable(), Drawable.Callback {
            private var mDrawable: Drawable? = null
            override fun getState(): IntArray = mDrawable?.state ?: intArrayOf()
            override fun setState(stateSet: IntArray): Boolean =
                mDrawable?.setState(stateSet) ?: false

            override fun getChangingConfigurations(): Int = mDrawable?.changingConfigurations ?: 0
            override fun setChangingConfigurations(configs: Int) =
                mDrawable?.run { changingConfigurations = configs } ?: Unit

            override fun isAutoMirrored(): Boolean =
                mDrawable?.let { DrawableCompat.isAutoMirrored(it) } ?: false

            override fun setAutoMirrored(mirrored: Boolean) =
                mDrawable?.let { DrawableCompat.setAutoMirrored(it, mirrored) } ?: Unit

            override fun isStateful(): Boolean = mDrawable?.isStateful ?: false
            override fun getCurrent(): Drawable = mDrawable!!.current
            override fun getTransparentRegion(): Region? = mDrawable?.transparentRegion
            override fun getIntrinsicWidth(): Int = mDrawable?.intrinsicWidth ?: 0
            override fun getIntrinsicHeight(): Int = mDrawable?.intrinsicHeight ?: 0
            override fun getMinimumWidth(): Int = mDrawable?.minimumWidth ?: 0
            override fun getMinimumHeight(): Int = mDrawable?.minimumHeight ?: 0
            override fun setDither(dither: Boolean) = mDrawable?.setDither(dither) ?: Unit
            override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
                super.setVisible(visible, restart) ||
                        mDrawable?.setVisible(visible, restart) ?: false

            override fun setFilterBitmap(filter: Boolean) =
                mDrawable?.run { isFilterBitmap = filter } ?: Unit

            override fun setHotspot(x: Float, y: Float) =
                mDrawable?.let { DrawableCompat.setHotspot(it, x, y) } ?: Unit

            override fun setHotspotBounds(left: Int, top: Int, right: Int, bottom: Int) =
                mDrawable
                    ?.let { DrawableCompat.setHotspotBounds(it, left, top, right, bottom) } ?: Unit

            override fun setTint(tint: Int) =
                mDrawable?.let { DrawableCompat.setTint(it, tint) } ?: Unit

            override fun setTintList(tints: ColorStateList?) =
                mDrawable?.let { DrawableCompat.setTintList(it, tints) } ?: Unit

            override fun setTintMode(tintMode: PorterDuff.Mode?) = mDrawable?.let { drawable ->
                tintMode?.let { DrawableCompat.setTintMode(drawable, tintMode) }
            } ?: Unit

            override fun jumpToCurrentState() =
                mDrawable?.let { DrawableCompat.jumpToCurrentState(it) } ?: Unit

            override fun onLevelChange(level: Int): Boolean = mDrawable?.setLevel(level) ?: false
            override fun onBoundsChange(rect: Rect) = mDrawable?.run { bounds = rect } ?: Unit
            override fun getPadding(padding: Rect): Boolean =
                mDrawable?.getPadding(padding) ?: false

            override fun getOpacity(): Int = mDrawable?.opacity ?: 0
            override fun setAlpha(alphaInt: Int) = mDrawable?.run { alpha = alphaInt } ?: Unit
            override fun draw(canvas: Canvas) = mDrawable?.draw(canvas) ?: Unit
            override fun setColorFilter(cf: ColorFilter?) =
                mDrawable?.run { colorFilter = cf } ?: Unit

            override fun invalidateDrawable(who: Drawable) = invalidateSelf()
            override fun unscheduleDrawable(who: Drawable, what: Runnable) = unscheduleSelf(what)
            override fun scheduleDrawable(who: Drawable, what: Runnable, whenLong: Long) =
                scheduleSelf(what, whenLong)
        }
    }
}