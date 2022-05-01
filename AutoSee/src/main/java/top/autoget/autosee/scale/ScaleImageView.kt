package top.autoget.autosee.scale

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetManager
import android.content.res.TypedArray
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.AsyncTask
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AnyThread
import top.autoget.autokit.DateKit.nowMillis
import top.autoget.autokit.LoggerKit
import top.autoget.autokit.VersionKit.aboveHoneycomb
import top.autoget.autokit.VersionKit.aboveIceCreamSandwich
import top.autoget.autokit.debug
import top.autoget.autokit.info
import top.autoget.autokit.warn
import top.autoget.autosee.R
import java.io.File
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.Executor
import kotlin.math.*

open class ScaleImageView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) :
    View(context, attr), LoggerKit {
    var scaleMax = 2f
        set(dpi) {
            field = resources.displayMetrics.run { xdpi + ydpi } / 2 / dpi
        }
    var scaleDoubleTapDpiZoom = 1f
        set(dpi) {
            field = resources.displayMetrics.run { xdpi + ydpi } / 2 / dpi
        }
    private val longClickHandler: Handler
    private var onLongClickListener: OnLongClickListener? = null
    private var touchCountMax: Int = 0
    override fun setOnLongClickListener(longClickListener: OnLongClickListener?) {
        onLongClickListener = longClickListener
    }

    private val quickScaleThreshold: Float
    var isEnabledZoom = true

    class ImageSource {
        var region: Rect? = null
            set(region) {
                field = region
                invariants
            }
        var tile: Boolean = false
        var mWidth: Int = 0
            private set
        var mHeight: Int = 0
            private set
        private val invariants
            get() = region?.run {
                tile = true
                mWidth = width()
                mHeight = height()
            }
        val mBitmap: Bitmap?
        fun dimensions(width: Int, height: Int) = run {
            mBitmap?.let {
                mWidth = width
                mHeight = height
            }
            invariants
        }

        val mResource: Int?
        val mUri: Uri?
        var isCached: Boolean = false

        private constructor(bitmap: Bitmap, cached: Boolean) {
            tile = false
            mWidth = bitmap.width
            mHeight = bitmap.height
            mBitmap = bitmap
            mResource = null
            mUri = null
            isCached = cached
        }

        private constructor(resource: Int) {
            tile = true
            mBitmap = null
            mResource = resource
            mUri = null
        }

        private constructor(uri: Uri) {
            tile = true
            mBitmap = null
            mResource = null
            mUri = uri.toString().run {
                when {
                    startsWith(SCHEME_FILE) && !File(substring(SCHEME_FILE.length - 1)).exists() -> try {
                        Uri.parse(URLDecoder.decode(this, "UTF-8"))
                    } catch (e: UnsupportedEncodingException) {
                        uri
                    }
                    else -> uri
                }//如果文件不存在，Url解码Uri再试一次
            }
        }

        companion object {
            fun bitmap(bitmap: Bitmap?): ImageSource = bitmap?.let { ImageSource(it, false) }
                ?: throw NullPointerException("Bitmap must not be null")

            fun bitmapCached(bitmap: Bitmap?): ImageSource = bitmap?.let { ImageSource(it, true) }
                ?: throw NullPointerException("Bitmap must not be null")

            fun resource(resId: Int): ImageSource = ImageSource(resId)
            const val SCHEME_ASSET = "file:///android_asset/"
            fun asset(assetName: String?): ImageSource = assetName?.let { uri("$SCHEME_ASSET$it") }
                ?: throw NullPointerException("Asset name must not be null")

            fun uri(uri: Uri?): ImageSource =
                uri?.let { ImageSource(it) } ?: throw NullPointerException("Uri must not be null")

            const val SCHEME_FILE = "file:///"
            fun uri(uriStr: String?): ImageSource {
                var uri: String = uriStr ?: throw NullPointerException("Uri must not be null")
                if (!uri.contains("://")) {
                    if (uri.startsWith("/")) uri = uri.substring(1)
                    uri = "$SCHEME_FILE$uri"
                }
                return ImageSource(Uri.parse(uri))
            }
        }
    }

    init {
        scaleMax = 160f
        scaleDoubleTapDpiZoom = 160f
        setGestureDetector(context)
        longClickHandler = Handler { message ->
            onLongClickListener?.let {
                if (message.what == MESSAGE_LONG_CLICK) {
                    touchCountMax = 0
                    super@ScaleImageView.setOnLongClickListener(it)
                    performLongClick()
                    super@ScaleImageView.setOnLongClickListener(null)
                }
            }
            true
        }
        quickScaleThreshold = TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, context.resources.displayMetrics)
        attr?.let {
            val typedArray: TypedArray =
                getContext().obtainStyledAttributes(attr, R.styleable.ScaleImageView)
            try {
                typedArray.run {
                    if (hasValue(R.styleable.ScaleImageView_panEnabled))
                        isEnabledPan = getBoolean(R.styleable.ScaleImageView_panEnabled, true)
                    if (hasValue(R.styleable.ScaleImageView_zoomEnabled))
                        isEnabledZoom = getBoolean(R.styleable.ScaleImageView_zoomEnabled, true)
                    if (hasValue(R.styleable.ScaleImageView_quickScaleEnabled))
                        isEnabledQuickScale =
                            getBoolean(R.styleable.ScaleImageView_quickScaleEnabled, true)
                    if (hasValue(R.styleable.ScaleImageView_tileBackgroundColor))
                        setTileBackgroundColor(
                            getColor(
                                R.styleable.ScaleImageView_tileBackgroundColor,
                                Color.argb(0, 0, 0, 0)
                            )
                        )
                    if (hasValue(R.styleable.ScaleImageView_assetName))
                        getString(R.styleable.ScaleImageView_assetName)?.let { assetName ->
                            if (assetName.isNotEmpty())
                                setImage(ImageSource.asset(assetName).apply { tile = true })
                        }
                    if (hasValue(R.styleable.ScaleImageView_src))
                        getResourceId(R.styleable.ScaleImageView_src, 0).let { resId ->
                            if (resId > 0)
                                setImage(ImageSource.resource(resId).apply { tile = true })
                        }
                }
            } finally {
                typedArray.recycle()
            }
        }
    }

    private var detector: GestureDetector? = null
    private var vTranslate: PointF? = null
    var scale: Float = 0f
        private set
    var isReady: Boolean = false
        private set
    var isEnabledPan = true
        set(panEnabled) {
            field = panEnabled
            vTranslate?.let {
                if (!panEnabled) {
                    it.apply {
                        x = width / 2 - scale * (sWidthSelect / 2)
                        y = height / 2 - scale * (sHeightSelect / 2)
                    }
                    if (isReady) {
                        refreshRequiredTiles(true)
                        invalidate()
                    }
                }
            }
        }

    private data class Tile(
        var sampleSize: Int = 0,
        var loading: Boolean = false, var visible: Boolean = false, var bitmap: Bitmap? = null,
        var vRect: Rect? = null, var sRect: Rect? = null, var fileSRect: Rect? = null
    )

    interface OnImageEventListener {
        fun onReady()
        fun onTileLoadError(e: Exception)
        fun onImageLoaded()
        fun onImageLoadError(e: Exception)
        fun onPreviewReleased()
        fun onPreviewLoadError(e: Exception)
    }

    class DefaultOnImageEventListener : OnImageEventListener {
        override fun onReady() {}
        override fun onTileLoadError(e: Exception) {}
        override fun onImageLoaded() {}
        override fun onImageLoadError(e: Exception) {}
        override fun onPreviewReleased() {}
        override fun onPreviewLoadError(e: Exception) {}
    }

    var onImageEventListener: OnImageEventListener? = null

    private class TaskTileLoad
    constructor(view: ScaleImageView, decoder: RegionDecoder, tile: Tile) :
        AsyncTask<Void, Void, Bitmap>(), LoggerKit {
        private val refView: WeakReference<ScaleImageView> = WeakReference(view)
        private val refDecoder: WeakReference<RegionDecoder> = WeakReference(decoder)
        private val refTile: WeakReference<Tile> = WeakReference(tile)
        private var exception: Exception? = null

        init {
            tile.loading = true
        }

        override fun doInBackground(vararg params: Void): Bitmap? = try {
            refView.get()?.let { view ->
                refTile.get()?.let { tile ->
                    refDecoder.get()?.let { decoder ->
                        when {
                            tile.visible && decoder.isReady -> {
                                tile.sRect?.let {
                                    view.debugScale(
                                        "TileLoadTask.doInBackground, tile.sRect=%s, tile.sampleSize=%d",
                                        it, tile.sampleSize
                                    )
                                }
                                synchronized(view.decoderLock) {
                                    view.fileSRect(tile.sRect, tile.fileSRect)
                                    view.sRegion?.run { tile.fileSRect?.offset(left, top) }
                                    tile.fileSRect
                                        ?.let { return decoder.decodeRegion(it, tile.sampleSize) }
                                }
                            }
                            else -> {
                                tile.loading = false
                                null
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            error("${loggerTag}->Failed to decode tile")
            exception = e
            null
        } catch (e: OutOfMemoryError) {
            error("${loggerTag}->Failed to decode tile - OutOfMemoryError")
            exception = RuntimeException(e)
            null
        }

        override fun onPostExecute(bitmapSrc: Bitmap?) {
            refView.get()?.let { view ->
                refTile.get()?.let { tile ->
                    bitmapSrc?.let {
                        tile.apply {
                            bitmap = it
                            loading = false
                        }
                        view.onTileLoaded()
                    } ?: exception?.let { view.onImageEventListener?.onTileLoadError(it) }
                }
            }
        }
    }

    @AnyThread
    private fun fileSRect(sRect: Rect?, target: Rect?) = target?.run {
        sRect?.run {
            when (requiredRotation) {
                0 -> set(sRect)
                90 -> set(top, sHeight - right, bottom, sHeight - left)
                180 -> set(sWidth - right, sHeight - bottom, sWidth - left, sHeight - top)
                else -> set(sWidth - bottom, left, sWidth - top, right)
            }
        }
    }

    protected val onImageLoaded = {}
    private val checkImageLoaded: Boolean = isBaseLayerReady.apply {
        if (!isImageLoaded && this) {
            isImageLoaded = true
            preDraw
            onImageLoaded
            onImageEventListener?.onImageLoaded()
        }
    }

    @Synchronized
    private fun onTileLoaded() {
        debugScale("onTileLoaded")
        checkReady
        checkImageLoaded
        if (isBaseLayerReady) bitmap?.let {
            when {
                isCachedBitmap -> onImageEventListener?.onPreviewReleased()
                else -> it.recycle()
            }
            bitmap = null
            isCachedBitmap = false
            bitmapIsPreview = false
        }
        invalidate()
    }

    var isDebug: Boolean = false

    @AnyThread
    private fun debugScale(message: String, vararg args: Any) {
        if (isDebug) debug("$loggerTag->${String.format(message, *args)}")
    }

    private var tileMap: MutableMap<Int, MutableList<Tile>>? = null
    private var decoder: RegionDecoder? = null
    private var fullImageSampleSize: Int = 0
    private fun refreshRequiredTiles(load: Boolean) = tileMap?.let { map ->
        decoder?.let { decode ->
            min(fullImageSampleSize, calculateInSampleSize(scale)).let { sampleSize ->
                for (list in map.values) {
                    for (tile in list) {
                        tile.sampleSize.let {
                            if (it < sampleSize || it > sampleSize && it != fullImageSampleSize) tile.run {
                                visible = false
                                bitmap?.recycle()
                                bitmap = null
                            }
                            when (it) {
                                sampleSize -> when {
                                    tileVisible(tile) -> tile.run {
                                        visible = true
                                        if (load && !loading && bitmap == null)
                                            execute(TaskTileLoad(this@ScaleImageView, decode, this))
                                    }
                                    it != fullImageSampleSize -> tile.run {
                                        visible = false
                                        bitmap?.recycle()
                                        bitmap = null
                                    }
                                }
                                fullImageSampleSize -> tile.visible = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun tileVisible(tile: Tile): Boolean = tile.sRect.run {
        !(viewToSourceX(0f) > right || left > viewToSourceX(width.toFloat()) ||
                viewToSourceY(0f) > bottom || top > viewToSourceY(height.toFloat()))
    }

    var parallelLoadingEnabled: Boolean = false
    private fun execute(asyncTask: AsyncTask<Void, Void, *>) {
        if (parallelLoadingEnabled && aboveHoneycomb) try {
            AsyncTask::class.java
                .getMethod("executeOnExecutor", Executor::class.java, Array<Any>::class.java)
                .invoke(
                    asyncTask, AsyncTask::class.java.getField("THREAD_POOL_EXECUTOR")
                        .get(null) as Executor, null
                )
            return
        } catch (e: Exception) {
            info("${loggerTag}->Failed to execute AsyncTask on thread pool executor, falling back to single threaded executor")
        }
        asyncTask.execute()
    }

    private var isZooming: Boolean = false

    interface OnAnimationEventListener {
        fun onComplete()
        fun onInterruptedByUser()
        fun onInterruptedByNewAnim()
    }

    class DefaultOnAnimationEventListener : OnAnimationEventListener {
        override fun onComplete() {}
        override fun onInterruptedByUser() {}
        override fun onInterruptedByNewAnim() {}
    }

    private data class Anim(
        var duration: Long = 500,
        var origin: Int = ORIGIN_ANIM,
        var easing: Int = EASE_IN_OUT_QUAD,
        var interruptible: Boolean = true,
        var listener: OnAnimationEventListener? = null,
        var scaleStart: Float = 0f,
        var scaleEnd: Float = 0f,
        var sCenterStart: PointF? = null,
        var sCenterEnd: PointF? = null,
        var sCenterEndRequested: PointF? = null,
        var vFocusStart: PointF? = null,
        var vFocusEnd: PointF? = null,
        var time: Long = nowMillis
    )

    inner class BuilderAnim @JvmOverloads constructor(
        private val targetScale: Float = scale,
        private val targetSCenter: PointF? = center, private val vFocus: PointF? = null
    ) {
        var mDuration: Long = 500
        var mOrigin: Int = ORIGIN_ANIM
        var mEasing: Int = EASE_IN_OUT_QUAD
        var mPanLimited = true
        var mInterruptible = true
        var onAnimationEventListener: OnAnimationEventListener? = null
        fun start() {
            try {
                anim?.listener?.onInterruptedByNewAnim()
            } catch (e: Exception) {
                warn("${loggerTag}->Error thrown by animation listener")
            }
            limitedScale(targetScale).let { targetScale ->
                targetSCenter?.let {
                    (if (mPanLimited) limitedSCenter(it.x, it.y, targetScale, PointF()) else it)
                        .let { targetSCenter ->
                            anim = Anim().apply {
                                duration = mDuration
                                origin = mOrigin
                                easing = mEasing
                                interruptible = mInterruptible
                                listener = onAnimationEventListener
                                scaleStart = scale
                                scaleEnd = targetScale
                                sCenterStart = center
                                sCenterEnd = targetSCenter
                                sCenterEndRequested = targetSCenter
                                vFocusStart = sourceToViewCoord(targetSCenter)
                                vFocusEnd = PointF(
                                    paddingLeft + (width - paddingRight - paddingLeft) / 2f,
                                    paddingTop + (height - paddingBottom - paddingTop) / 2f
                                )
                                time = nowMillis
                            }
                        }
                }
                vFocus?.let { vFocus ->
                    anim?.sCenterStart?.run {
                        (vFocus.x - targetScale * x).let { vTranslateXEnd ->
                            (vFocus.y - targetScale * y).let { vTranslateYEnd ->
                                ScaleAndTranslate(
                                    targetScale, PointF(vTranslateXEnd, vTranslateYEnd)
                                ).let { satEnd ->
                                    fitToBounds(true, satEnd)
                                    anim?.vFocusEnd = PointF(
                                        vFocus.x + (satEnd.vTranslateData.x - vTranslateXEnd),
                                        vFocus.y + (satEnd.vTranslateData.y - vTranslateYEnd)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            invalidate()
        }
    }

    private fun limitedSCenter(
        sCenterX: Float, sCenterY: Float, scale: Float, sTarget: PointF
    ): PointF = sTarget.apply {
        vTranslateForSCenter(sCenterX, sCenterY, scale).let { vTranslate ->
            set(
                (paddingLeft + (width - paddingRight - paddingLeft) / 2 - vTranslate.x) / scale,
                (paddingTop + (height - paddingBottom - paddingTop) / 2 - vTranslate.y) / scale
            )
        }
    }

    fun animateScale(scale: Float): BuilderAnim? = if (isReady) BuilderAnim(scale) else null
    fun animateCenter(center: PointF): BuilderAnim? =
        if (isReady) BuilderAnim(targetSCenter = center) else null

    fun animateScaleAndCenter(scale: Float, sCenter: PointF): BuilderAnim? =
        if (isReady) BuilderAnim(scale, sCenter) else null

    var isEnabledQuickScale = true
    private var scaleStart: Float = 0f
    private var isQuickScaling: Boolean = false
    private var quickScaleMoved: Boolean = false
    private var quickScaleLastDistance: Float = 0f
    private var quickScaleVStart: PointF? = null
    private var quickScaleSCenter: PointF? = null
    private var quickScaleVLastPoint: PointF? = null
    private var vCenterStart: PointF? = null
    private var vTranslateStart: PointF? = null
    private fun setGestureDetector(context: Context) {
        detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float
            ): Boolean {
                vTranslate?.let {
                    if (isReady && isEnabledPan && !isZooming && e1 != null && e2 != null &&
                        (abs(e1.x - e2.x) > 50 || abs(e1.y - e2.y) > 50) &&
                        (abs(velocityX) > 500 || abs(velocityY) > 500)
                    ) {
                        PointF(it.x + velocityX * 0.25f, it.y + velocityY * 0.25f)
                            .let { vTranslateEnd ->
                                BuilderAnim(
                                    targetSCenter = PointF(
                                        (width / 2 - vTranslateEnd.x) / scale,
                                        (height / 2 - vTranslateEnd.y) / scale
                                    )
                                ).apply {
                                    mOrigin = ORIGIN_FLING
                                    mEasing = EASE_OUT_QUAD
                                    mPanLimited = false
                                }.start()
                            }
                        return true
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean =
                true.apply { performClick() }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                vTranslate?.let {
                    if (isReady && isEnabledZoom) {
                        setGestureDetector(context)
                        return when {
                            isEnabledQuickScale -> {
                                scaleStart = scale
                                isZooming = true
                                isQuickScaling = true
                                quickScaleMoved = false
                                quickScaleLastDistance = -1f
                                quickScaleVStart = PointF(e.x, e.y)
                                quickScaleSCenter = viewToSourceCoord(e.x, e.y)
                                quickScaleVLastPoint = viewToSourceCoord(e.x, e.y)
                                vCenterStart = viewToSourceCoord(e.x, e.y)
                                vTranslateStart = PointF(it.x, it.y)
                                false
                            }
                            else -> true.apply {
                                doubleTapZoom(viewToSourceCoord(e.x, e.y), PointF(e.x, e.y))
                            }
                        }
                    }
                }
                return super.onDoubleTapEvent(e)
            }
        })
    }

    @JvmOverloads
    fun viewToSourceCoord(vx: Float, vy: Float, sTarget: PointF = PointF()): PointF? =
        vTranslate?.let { sTarget.apply { set(viewToSourceX(vx), viewToSourceY(vy)) } }

    private fun viewToSourceX(vx: Float): Float = vTranslate?.let { (vx - it.x) / scale }
        ?: Float.NaN

    private fun viewToSourceY(vy: Float): Float = vTranslate?.let { (vy - it.y) / scale }
        ?: Float.NaN

    private var mOrientation: Int = 0
    val requiredRotation: Int
        @AnyThread
        get() = if (orientation == ORIENTATION_USE_EXIF) mOrientation else orientation
    var sWidth: Int = 0
        private set
    var sHeight: Int = 0
        private set
    private val sWidthSelect: Int
        get() = if (requiredRotation == 90 || requiredRotation == 270) sHeight else sWidth
    private val sHeightSelect: Int
        get() = if (requiredRotation == 90 || requiredRotation == 270) sWidth else sHeight
    var minScaleType: Int = SCALE_TYPE_CENTER_INSIDE
        set(scaleType) {
            require(VALID_SCALE_TYPE.contains(scaleType)) { "Invalid scale type: $scaleType" }
            field = scaleType
            if (isReady) {
                fitToBounds(true)
                invalidate()
            }
        }

    private data class ScaleAndTranslate(var scaleData: Float, val vTranslateData: PointF)

    private fun fitToBounds(center: Boolean) {
        var init = false
        (satTemp ?: ScaleAndTranslate(0f, PointF(0f, 0f))
            .apply { satTemp = this }).apply {
            scaleData = scale
            vTranslate?.let { vTranslateData.set(it) }
        }.let {
            fitToBounds(center, it)
            scale = it.scaleData
            (vTranslate ?: PointF(0f, 0f).apply {
                init = true
                vTranslate = this
            }).set(it.vTranslateData)
        }
        if (init) vTranslate?.set(
            vTranslateForSCenter(sWidthSelect / 2f, sHeightSelect / 2f, scale)
        )
    }

    var panLimit = PAN_LIMIT_INSIDE
        set(panLimit) {
            require(VALID_PAN_LIMIT.contains(panLimit)) { "Invalid pan limit: $panLimit" }
            field = panLimit
            if (isReady) {
                fitToBounds(true)
                invalidate()
            }
        }

    private fun fitToBounds(center: Boolean, sat: ScaleAndTranslate) =
        limitedScale(sat.scaleData).let { scale ->
            (if (panLimit == PAN_LIMIT_OUTSIDE && isReady) false else center).let { isCenter ->
                sat.vTranslateData.apply {
                    (scale * sWidthSelect).let { scaleWidth ->
                        x = min(
                            when {
                                panLimit == PAN_LIMIT_CENTER && isReady ->
                                    max(x, width / 2 - scaleWidth)
                                isCenter -> max(x, width - scaleWidth)
                                else -> max(x, -scaleWidth)
                            }, when {
                                panLimit == PAN_LIMIT_CENTER && isReady ->
                                    max(0, width / 2).toFloat()
                                isCenter -> max(
                                    0f, (width - scaleWidth) * (when {
                                        paddingLeft > 0 || paddingRight > 0 ->
                                            paddingLeft / (paddingLeft + paddingRight).toFloat()
                                        else -> 0.5f
                                    })
                                )
                                else -> max(0, width).toFloat()
                            }
                        )
                    }
                    (scale * sHeightSelect).let { scaleHeight ->
                        y = min(
                            when {
                                panLimit == PAN_LIMIT_CENTER && isReady ->
                                    max(y, height / 2 - scaleHeight)
                                isCenter -> max(y, height - scaleHeight)
                                else -> max(y, -scaleHeight)
                            }, when {
                                panLimit == PAN_LIMIT_CENTER && isReady ->
                                    max(0, height / 2).toFloat()
                                isCenter -> max(
                                    0f, (height - scaleHeight) * (when {
                                        paddingTop > 0 || paddingBottom > 0 ->
                                            paddingTop / (paddingTop + paddingBottom).toFloat()
                                        else -> 0.5f
                                    })
                                )
                                else -> max(0, height).toFloat()
                            }
                        )
                    }
                }
            }
            sat.scaleData = scale
        }

    private fun limitedScale(targetScale: Float): Float = min(scaleMax, max(minScale, targetScale))
    private fun vTranslateForSCenter(sCenterX: Float, sCenterY: Float, scale: Float): PointF =
        (satTemp ?: ScaleAndTranslate(0f, PointF(0f, 0f))
            .apply { satTemp = this }).apply {
            scaleData = scale
            vTranslateData.set(
                (paddingLeft + (width - paddingRight - paddingLeft) / 2) - sCenterX * scale,
                (paddingTop + (height - paddingBottom - paddingTop) / 2) - sCenterY * scale
            )
            fitToBounds(true, this)
        }.vTranslateData

    var minScaleDpi: Float = minScale
        set(dpi) {
            field = resources.displayMetrics.run { xdpi + ydpi } / 2 / dpi
        }
    private val minScale: Float
        get() = ((width - paddingLeft - paddingRight) / sWidthSelect.toFloat()).let { w ->
            ((height - paddingTop - paddingBottom) / sHeightSelect.toFloat()).let { h ->
                when {
                    minScaleType == SCALE_TYPE_CENTER_CROP -> max(w, h)
                    minScaleType == SCALE_TYPE_CUSTOM && minScaleDpi > 0 -> minScaleDpi
                    else -> min(w, h)
                }
            }
        }
    var doubleTapZoomStyle: Int = ZOOM_FOCUS_FIXED
        set(doubleTapZoomStyle) {
            require(VALID_ZOOM_FOCUS.contains(doubleTapZoomStyle)) { "Invalid zoom style: $doubleTapZoomStyle" }
            field = doubleTapZoomStyle
        }
    var doubleTapZoomDuration = 500
        set(durationMs) {
            field = max(0, durationMs)
        }

    private fun doubleTapZoom(sCenter: PointF?, vFocus: PointF?) = sCenter?.let {
        if (!isEnabledPan) sRequestedCenter?.let {
            sCenter.apply {
                x = it.x
                y = it.y
            }
        } ?: sCenter.apply {
            x = sWidthSelect / 2f
            y = sHeightSelect / 2f
        }
        min(scaleMax, scaleDoubleTapDpiZoom).let { doubleTapScale ->
            (scale <= doubleTapScale * 0.9).let { zoomIn ->
                (if (zoomIn) doubleTapScale else minScale).let { targetScale ->
                    when {
                        doubleTapZoomStyle == ZOOM_FOCUS_CENTER_IMMEDIATE ->
                            setScaleAndCenter(targetScale, sCenter)
                        doubleTapZoomStyle == ZOOM_FOCUS_CENTER || !zoomIn || !isEnabledPan ->
                            BuilderAnim(targetScale, sCenter).apply {
                                mDuration = doubleTapZoomDuration.toLong()
                                mOrigin = ORIGIN_DOUBLE_TAP_ZOOM
                                mInterruptible = false
                            }.start()
                        doubleTapZoomStyle == ZOOM_FOCUS_FIXED ->
                            BuilderAnim(targetScale, sCenter, vFocus).apply {
                                mDuration = doubleTapZoomDuration.toLong()
                                mOrigin = ORIGIN_DOUBLE_TAP_ZOOM
                                mInterruptible = false
                            }.start()
                    }
                }
            }
        }
        invalidate()
    }

    private var anim: Anim? = null
    private var pendingScale: Float? = null
    private var sPendingCenter: PointF? = null
    private var sRequestedCenter: PointF? = null
    fun setScaleAndCenter(scale: Float, sCenter: PointF) = apply {
        anim = null
        pendingScale = scale
        sPendingCenter = sCenter
        sRequestedCenter = sCenter
    }.let { invalidate() }

    val resetScaleAndCenter = {
        anim = null
        pendingScale = limitedScale(0f)
        sPendingCenter = when {
            isReady -> PointF((sWidthSelect / 2).toFloat(), (sHeightSelect / 2).toFloat())
            else -> PointF(0f, 0f)
        }
        invalidate()
    }
    private var paintTileBg: Paint? = null
    fun setTileBackgroundColor(tileBgColor: Int) {
        paintTileBg = when {
            Color.alpha(tileBgColor) == 0 -> null
            else -> Paint().apply {
                style = Paint.Style.FILL
                color = tileBgColor
            }
        }
        invalidate()
    }

    data class ImageViewState(val scale: Float, val center: PointF, val orientation: Int) :
        Serializable

    private var pRegion: Rect? = null
    private var sRegion: Rect? = null
    private var isCachedBitmap: Boolean = false
    private var uri: Uri? = null

    private class TaskBitmapLoad internal constructor(
        view: ScaleImageView, context: Context,
        decoderFactory: DecoderFactory<out ImageDecoder>,
        private val source: Uri, private val preview: Boolean
    ) : AsyncTask<Void, Void, Int>(), LoggerKit {
        private val refView: WeakReference<ScaleImageView> = WeakReference(view)
        private val refContext: WeakReference<Context> = WeakReference(context)
        private val refImageDecoder: WeakReference<DecoderFactory<out ImageDecoder>> =
            WeakReference(decoderFactory)
        private var bitmap: Bitmap? = null
        private var exception: Exception? = null
        override fun doInBackground(vararg params: Void): Int? = try {
            refView.get()?.let { view ->
                refContext.get()?.let { context ->
                    refImageDecoder.get()?.let { decoderFactory ->
                        view.debugScale("BitmapLoadTask.doInBackground")
                        bitmap = decoderFactory.make().decode(context, source)
                        view.getExifOrientation(context, source.toString())
                    }
                }
            }
        } catch (e: Exception) {
            error("${loggerTag}->Failed to load bitmap")
            exception = e
            null
        } catch (e: OutOfMemoryError) {
            error("${loggerTag}->Failed to load bitmap - OutOfMemoryError")
            exception = RuntimeException(e)
            null
        }

        override fun onPostExecute(orientation: Int?) = refView.get()?.let {
            when {
                bitmap != null && orientation != null -> when {
                    preview -> it.onPreviewLoaded(bitmap)
                    else -> it.onImageLoaded(bitmap, orientation, false)
                }
                exception != null && it.onImageEventListener != null -> when {
                    preview -> it.onImageEventListener!!.onPreviewLoadError(exception!!)
                    else -> it.onImageEventListener!!.onImageLoadError(exception!!)
                }
            }
        } ?: Unit
    }

    @Synchronized
    private fun onPreviewLoaded(previewBitmap: Bitmap?) {
        debugScale("onPreviewLoaded")
        previewBitmap?.let {
            when {
                bitmap != null || isImageLoaded -> it.recycle()
                else -> {
                    bitmap = pRegion?.run { Bitmap.createBitmap(it, left, top, width(), height()) }
                        ?: it
                    bitmapIsPreview = true
                    if (checkReady) {
                        invalidate()
                        requestLayout()
                    }
                }
            }
        }
    }

    @Synchronized
    private fun onImageLoaded(bitmapLoad: Bitmap?, orientation: Int, isCached: Boolean) {
        debugScale("onImageLoaded")
        bitmapLoad?.run {
            if (sWidth > 0 && sHeight > 0 && (sWidth != width || sHeight != height))
                reset(false)
        }
        bitmap?.let {
            when {
                isCachedBitmap -> onImageEventListener?.onPreviewReleased()
                else -> it.recycle()
            }
        }
        isCachedBitmap = isCached
        bitmapIsPreview = false
        bitmapLoad?.run {
            bitmap = this
            sWidth = width
            sHeight = height
        }
        mOrientation = orientation
        if (checkReady || checkImageLoaded) {
            invalidate()
            requestLayout()
        }
    }

    private class TaskTilesInit internal constructor(
        view: ScaleImageView, context: Context,
        decoderFactory: DecoderFactory<out RegionDecoder>, private val source: Uri
    ) : AsyncTask<Void, Void, IntArray>(), LoggerKit {
        private val refView: WeakReference<ScaleImageView> = WeakReference(view)
        private val refContext: WeakReference<Context> = WeakReference(context)
        private val refRegionDecoder: WeakReference<DecoderFactory<out RegionDecoder>> =
            WeakReference(decoderFactory)
        private var decoder: RegionDecoder? = null
        private var exception: Exception? = null
        override fun doInBackground(vararg params: Void): IntArray? = try {
            refView.get()?.let { view ->
                refContext.get()?.let { context ->
                    refRegionDecoder.get()?.let { decoderFactory ->
                        view.debugScale("TilesInitTask.doInBackground")
                        decoderFactory.make().apply { decoder = this }.init(context, source)?.run {
                            intArrayOf(
                                view.sRegion?.width() ?: x, view.sRegion?.height()
                                    ?: y, view.getExifOrientation(context, source.toString())
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            error("${loggerTag}->Failed to initialise bitmap decoder")
            exception = e
            null
        }

        override fun onPostExecute(xyo: IntArray?) = refView.get()?.let { view ->
            when {
                xyo != null && xyo.size == 3 ->
                    decoder?.let { view.onTilesInited(it, xyo[0], xyo[1], xyo[2]) }
                else -> exception?.let { view.onImageEventListener?.onImageLoadError(it) }
            }
        } ?: Unit
    }

    @AnyThread
    private fun getExifOrientation(context: Context, sourceUri: String): Int = when {
        sourceUri.startsWith(ContentResolver.SCHEME_CONTENT) -> {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(
                    Uri.parse(sourceUri),
                    arrayOf(MediaStore.Images.Media.ORIENTATION), null, null, null
                )
                when {
                    cursor?.moveToFirst() == true -> cursor.getInt(0).let { orientation ->
                        when {
                            VALID_ORIENTATION.contains(orientation) && orientation != ORIENTATION_USE_EXIF -> orientation
                            else -> ORIENTATION_0.apply { warn("${loggerTag}->Unsupported orientation: $orientation") }
                        }
                    }
                    else -> ORIENTATION_0
                }
            } catch (e: Exception) {
                ORIENTATION_0.apply { warn("${loggerTag}->Could not get orientation of image from media store") }
            } finally {
                cursor?.close()
            }
        }
        sourceUri.startsWith(ImageSource.SCHEME_FILE) && !sourceUri.startsWith(ImageSource.SCHEME_ASSET) -> try {
            when (val orientationAttr =
                ExifInterface(sourceUri.substring(ImageSource.SCHEME_FILE.length - 1))
                    .getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                    )) {
                ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_UNDEFINED -> ORIENTATION_0
                ExifInterface.ORIENTATION_ROTATE_90 -> ORIENTATION_90
                ExifInterface.ORIENTATION_ROTATE_180 -> ORIENTATION_180
                ExifInterface.ORIENTATION_ROTATE_270 -> ORIENTATION_270
                else -> ORIENTATION_0.apply { warn("${loggerTag}->Unsupported EXIF orientation: $orientationAttr") }
            }
        } catch (e: Exception) {
            ORIENTATION_0.apply { warn("${loggerTag}->Could not get EXIF orientation of image") }
        }
        else -> ORIENTATION_0
    }

    var maxTileWidth: Int = tileSizeAuto
    var maxTileHeight: Int = tileSizeAuto

    @Synchronized
    private fun onTilesInited(
        regionDecoder: RegionDecoder, width: Int, height: Int, orientation: Int
    ) {
        debugScale(
            "onTilesInited sWidth=%d, sHeight=%d, sOrientation=%d",
            width, height, this.orientation
        )
        if (sWidth > 0 && sHeight > 0 && (sWidth != width || sHeight != height)) {
            reset(false)
            bitmap?.let {
                when {
                    isCachedBitmap -> onImageEventListener?.onPreviewReleased()
                    else -> it.recycle()
                }
                bitmap = null
                isCachedBitmap = false
                bitmapIsPreview = false
            }
        }
        decoder = regionDecoder
        sWidth = width
        sHeight = height
        mOrientation = orientation
        checkReady
        if (!checkImageLoaded && width > 0 && height > 0 && maxTileWidth > 0 && maxTileHeight > 0 &&
            maxTileWidth != tileSizeAuto && maxTileHeight != tileSizeAuto
        ) initialiseBaseLayer(Point(maxTileWidth, maxTileHeight))
        invalidate()
        requestLayout()
    }

    class DecoderFactory<T>(private val clazz: Class<out T>) {
        @Throws(IllegalAccessException::class, InstantiationException::class)
        fun make(): T = clazz.newInstance()
    }

    class ImageDecoder {
        companion object {
            private const val PREFIX_FILE = "file://"
            private const val PREFIX_ASSET = "$PREFIX_FILE/android_asset/"
            private const val PREFIX_RESOURCE = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://"
        }

        @Throws(Exception::class)
        fun decode(context: Context, uri: Uri): Bitmap = uri.toString().run {
            BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
                .let { options ->
                    when {
                        startsWith(PREFIX_FILE) ->
                            BitmapFactory.decodeFile(substring(PREFIX_FILE.length), options)
                        startsWith(PREFIX_ASSET) -> BitmapFactory.decodeStream(
                            context.assets.open(substring(PREFIX_ASSET.length)), null, options
                        )
                        startsWith(PREFIX_RESOURCE) -> {
                            val packageName: String = uri.authority ?: ""
                            uri.pathSegments.run {
                                when {
                                    size == 2 && this[0] == "drawable" -> when (context.packageName) {
                                        packageName -> context.resources
                                        else -> context.packageManager
                                            .getResourcesForApplication(packageName)
                                    }.getIdentifier(this[1], "drawable", packageName)
                                    size == 1 && TextUtils.isDigitsOnly(this[0]) -> try {
                                        this[0].toInt()
                                    } catch (ignored: NumberFormatException) {
                                        0
                                    }
                                    else -> 0
                                }.let {
                                    BitmapFactory.decodeResource(context.resources, it, options)
                                }
                            }
                        }
                        else -> context.contentResolver.openInputStream(uri)
                            .use { BitmapFactory.decodeStream(it, null, options) }
                    }
                        ?: throw RuntimeException("Skia image region decoder returned null bitmap - image format may not be supported")
                }
        }
    }

    var imageDecoder: DecoderFactory<out ImageDecoder> = DecoderFactory(ImageDecoder::class.java)

    class RegionDecoder {
        private var decoder: BitmapRegionDecoder? = null
        val isReady: Boolean
            get() = decoder?.run { !isRecycled } ?: false

        companion object {
            private const val PREFIX_FILE = "file://"
            private const val PREFIX_ASSET = "$PREFIX_FILE/android_asset/"
            private const val PREFIX_RESOURCE = "${ContentResolver.SCHEME_ANDROID_RESOURCE}://"
        }

        @Throws(Exception::class)
        fun init(context: Context, uri: Uri): Point? = uri.toString().run {
            when {
                startsWith(PREFIX_FILE) -> BitmapRegionDecoder.newInstance(
                    substring(PREFIX_FILE.length), false
                )
                startsWith(PREFIX_ASSET) -> BitmapRegionDecoder.newInstance(
                    context.assets
                        .open(substring(PREFIX_ASSET.length), AssetManager.ACCESS_RANDOM), false
                )
                startsWith(PREFIX_RESOURCE) -> {
                    val packageName: String = uri.authority ?: ""
                    uri.pathSegments.run {
                        when {
                            size == 2 && this[0] == "drawable" -> when (context.packageName) {
                                packageName -> context.resources
                                else -> context.packageManager
                                    .getResourcesForApplication(packageName)
                            }.getIdentifier(this[1], "drawable", packageName)
                            size == 1 && TextUtils.isDigitsOnly(this[0]) -> try {
                                this[0].toInt()
                            } catch (ignored: NumberFormatException) {
                                0
                            }
                            else -> 0
                        }.let {
                            BitmapRegionDecoder
                                .newInstance(context.resources.openRawResource(it), false)
                        }
                    }
                }
                else -> context.contentResolver.openInputStream(uri)
                    ?.use { BitmapRegionDecoder.newInstance(it, false) }
            }.apply { decoder = this }?.run { Point(width, height) }
        }

        private val decoderLock = Any()
        fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap = synchronized(decoderLock) {
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }.let { decoder?.decodeRegion(sRect, it) }
                ?: throw RuntimeException("Skia image decoder returned null bitmap - image format may not be supported")
        }

        fun recycle() = decoder?.recycle() ?: Unit
    }

    var regionDecoder: DecoderFactory<out RegionDecoder> = DecoderFactory(RegionDecoder::class.java)

    @JvmOverloads
    fun setImage(
        imageSource: ImageSource?, previewSource: ImageSource? = null, state: ImageViewState? = null
    ) = imageSource?.let { imageSrc ->
        reset(true)
        state?.let { restoreState(it) }
        previewSource?.let { previewSrc ->
            require(imageSrc.mBitmap == null) { "Preview image cannot be used when a bitmap is provided for the main image" }
            require(imageSrc.mWidth > 0 && imageSrc.mHeight > 0) { "Preview image cannot be used unless dimensions are provided for the main image" }
            sWidth = imageSrc.mWidth
            sHeight = imageSrc.mHeight
            pRegion = previewSrc.region
            previewSrc.mBitmap?.let {
                isCachedBitmap = previewSrc.isCached
                onPreviewLoaded(it)
            } ?: previewSrc.mUri.let {
                when {
                    it != null || previewSrc.mResource == null -> it
                    else -> Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${previewSrc.mResource}")
                }?.let { uri ->
                    execute(TaskBitmapLoad(this, context, imageDecoder, uri, true))
                }
            }
        }
        when {
            imageSrc.mBitmap != null && imageSrc.region != null -> imageSrc.region?.run {
                onImageLoaded(
                    Bitmap.createBitmap(imageSrc.mBitmap, left, top, width(), height()),
                    ORIENTATION_0, false
                )
            }
            imageSrc.mBitmap != null ->
                onImageLoaded(imageSrc.mBitmap, ORIENTATION_0, imageSrc.isCached)
            else -> {
                sRegion = imageSrc.region
                imageSrc.mUri.let {
                    when {
                        it != null || imageSrc.mResource == null -> it
                        else -> Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${imageSrc.mResource}")
                    }.apply { uri = this }?.let { uri ->
                        when {
                            imageSrc.tile || sRegion != null ->
                                execute(TaskTilesInit(this, context, regionDecoder, uri))
                            else -> execute(TaskBitmapLoad(this, context, imageDecoder, uri, false))
                        }
                    }
                }
            }
        }
    } ?: throw NullPointerException("imageSource must not be null")

    private var vDistStart: Float = 0f
    private var isPanning: Boolean = false
    private var vTranslateBefore: PointF? = null
    private var satTemp: ScaleAndTranslate? = null
    private var mMatrix: Matrix? = null
    private var sRect: RectF? = null
    private val decoderLock = Any()
    private var bitmap: Bitmap? = null
    val hasImage: Boolean = uri != null || bitmap != null
    var isImageLoaded: Boolean = false
        private set
    private var bitmapIsPreview: Boolean = false
    private fun reset(newImage: Boolean) {
        debugScale("reset newImage=$newImage")
        pendingScale = 0f
        sPendingCenter = null
        sRequestedCenter = null
        touchCountMax = 0
        fullImageSampleSize = 0
        vDistStart = 0f
        scale = 0f
        scaleStart = 0f
        isPanning = false
        isZooming = false
        isQuickScaling = false
        quickScaleMoved = false
        quickScaleLastDistance = 0f
        quickScaleVStart = null
        quickScaleSCenter = null
        quickScaleVLastPoint = null
        vCenterStart = null
        vTranslateStart = null
        vTranslateBefore = null
        vTranslate = null
        anim = null
        satTemp = null
        mMatrix = null
        sRect = null
        if (newImage) {
            uri = null
            decoder?.run {
                synchronized(decoderLock) {
                    recycle()
                    decoder = null
                }
            }
            bitmap?.run {
                if (!isCachedBitmap) recycle()
                onImageEventListener?.let { if (isCachedBitmap) it.onPreviewReleased() }
            }
            sWidth = 0
            sHeight = 0
            mOrientation = 0
            sRegion = null
            pRegion = null
            isReady = false
            isImageLoaded = false
            bitmap = null
            bitmapIsPreview = false
            isCachedBitmap = false
        }
        tileMap?.let {
            for (list in it.values) {
                for (tile in list) {
                    tile.apply {
                        visible = false
                        bitmap?.recycle()
                        bitmap = null
                    }
                }
            }
            tileMap = null
        }
        setGestureDetector(context)
    }

    var orientation = ORIENTATION_0
        set(orientation) {
            require(VALID_ORIENTATION.contains(orientation)) { "Invalid orientation: $orientation" }
            field = orientation
            reset(false)
            invalidate()
            requestLayout()
        }

    private fun restoreState(state: ImageViewState?) = state?.center?.let {
        if (VALID_ORIENTATION.contains(state.orientation)) {
            pendingScale = state.scale
            sPendingCenter = state.center
            orientation = state.orientation
            invalidate()
        }
    }

    val state: ImageViewState?
        get() = when {
            vTranslate != null && sWidth > 0 && sHeight > 0 ->
                center?.let { ImageViewState(scale, it, orientation) }
            else -> null
        }
    val center: PointF?
        get() = viewToSourceCoord((width / 2).toFloat(), (height / 2).toFloat())

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        debugScale("onSizeChanged %dx%d -> %dx%d", oldw, oldh, w, h)
        center?.let {
            if (isReady) this.apply {
                anim = null
                pendingScale = scale
                sPendingCenter = it
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY).let { resizeWidth ->
            (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY).let { resizeHeight ->
                if (sWidth > 0 && sHeight > 0) when {
                    resizeWidth && resizeHeight -> {
                        width = sWidthSelect
                        height = sHeightSelect
                    }
                    resizeWidth -> width =
                        (sWidthSelect.toDouble() / sHeightSelect.toDouble() * height).toInt()
                    resizeHeight -> height =
                        (sHeightSelect.toDouble() / sWidthSelect.toDouble() * width).toInt()
                }
            }
        }
        setMeasuredDimension(max(width, suggestedMinimumWidth), max(height, suggestedMinimumHeight))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (anim?.interruptible) {
            false -> return true.apply { requestDisallowInterceptTouchEvent(true) }
            else -> try {
                anim?.listener?.onInterruptedByUser()
                anim = null
            } catch (e: Exception) {
                warn("${loggerTag}->Error thrown by animation listener")
            }
        }
        return vTranslate?.let {
            when {
                isQuickScaling || detector?.onTouchEvent(event) == false -> {
                    vCenterStart ?: PointF(0f, 0f).apply { vCenterStart = this }
                    vTranslateStart ?: PointF(0f, 0f).apply { vTranslateStart = this }
                    sendStateChanged(scale, (vTranslateBefore
                        ?: PointF(0f, 0f).apply { vTranslateBefore = this }).apply { set(it) },
                        ORIGIN_TOUCH
                    )
                    onTouchEventInternal(event) || super.onTouchEvent(event)
                }
                else -> {
                    isZooming = false
                    isPanning = false
                    touchCountMax = 0
                    true
                }
            }
        } ?: true
    }

    private fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) =
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)

    interface OnStateChangedListener {
        fun onScaleChanged(newScale: Float, origin: Int)
        fun onCenterChanged(newCenter: PointF?, origin: Int)
    }

    class DefaultOnStateChangedListener : OnStateChangedListener {
        override fun onCenterChanged(newCenter: PointF?, origin: Int) {}
        override fun onScaleChanged(newScale: Float, origin: Int) {}
    }

    var onStateChangedListener: OnStateChangedListener? = null
    private fun sendStateChanged(oldScale: Float, oldVTranslate: PointF, origin: Int) =
        onStateChangedListener?.run {
            if (scale != oldScale) onScaleChanged(scale, origin)
            if (vTranslate != oldVTranslate) onCenterChanged(center, origin)
        }

    private val density: Float = resources.displayMetrics.density
    private fun onTouchEventInternal(event: MotionEvent): Boolean {
        val touchCount = event.pointerCount
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_1_DOWN, MotionEvent.ACTION_POINTER_2_DOWN -> {
                anim = null
                requestDisallowInterceptTouchEvent(true)
                touchCountMax = max(touchCountMax, touchCount)
                when {
                    touchCount >= 2 -> {
                        when {
                            isEnabledZoom -> {
                                scaleStart = scale
                                vDistStart = distance(
                                    event.getX(0), event.getX(1), event.getY(0), event.getY(1)
                                )
                                vTranslate?.run { vTranslateStart?.set(x, y) }
                                vCenterStart?.set(
                                    (event.getX(0) + event.getX(1)) / 2,
                                    (event.getY(0) + event.getY(1)) / 2
                                )
                            }
                            else -> touchCountMax = 0
                        }
                        longClickHandler.removeMessages(MESSAGE_LONG_CLICK)
                    }
                    !isQuickScaling -> {
                        vTranslate?.run { vTranslateStart?.set(x, y) }
                        vCenterStart?.set(event.x, event.y)
                        longClickHandler.sendEmptyMessageDelayed(MESSAGE_LONG_CLICK, 600)
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                var consumed = false
                if (touchCountMax > 0) when {
                    touchCount >= 2 -> distance(
                        event.getX(0), event.getX(1), event.getY(0), event.getY(1)
                    ).let { vDistEnd ->
                        ((event.getX(0) + event.getX(1)) / 2).let { vCenterEndX ->
                            ((event.getY(0) + event.getY(1)) / 2).let { vCenterEndY ->
                                if (isEnabledZoom && (distance(
                                        vCenterStart?.x ?: 0f, vCenterEndX,
                                        vCenterStart?.y ?: 0f, vCenterEndY
                                    ) > 5 || abs(vDistEnd - vDistStart) > 5 || isPanning)
                                ) {
                                    isZooming = true
                                    isPanning = true
                                    consumed = true
                                    val previousScale = scale.toDouble()
                                    scale = min(scaleMax, vDistEnd / vDistStart * scaleStart)
                                    when {
                                        scale <= minScale -> {
                                            vDistStart = vDistEnd
                                            scaleStart = minScale
                                            vCenterStart?.set(vCenterEndX, vCenterEndY)
                                            vTranslate?.let { vTranslateStart?.set(it) }
                                        }
                                        isEnabledPan -> vCenterStart?.let { vCenterStart ->
                                            vTranslateStart?.let { vTranslateStart ->
                                                vTranslate?.apply {
                                                    x =
                                                        vCenterEndX - (vCenterStart.x - vTranslateStart.x) * (scale / scaleStart)
                                                    y =
                                                        vCenterEndY - (vCenterStart.y - vTranslateStart.y) * (scale / scaleStart)
                                                }
                                                if (previousScale * sHeightSelect < height && scale * sHeightSelect >= height ||
                                                    previousScale * sWidthSelect < width && scale * sWidthSelect >= width
                                                ) {
                                                    fitToBounds(true)
                                                    vCenterStart.set(vCenterEndX, vCenterEndY)
                                                    vTranslate?.let { vTranslateStart.set(it) }
                                                    scaleStart = scale
                                                    vDistStart = vDistEnd
                                                }
                                            }
                                        }
                                        sRequestedCenter != null -> vTranslate?.apply {
                                            x = width / 2 - scale * sRequestedCenter!!.x
                                            y = height / 2 - scale * sRequestedCenter!!.y
                                        }
                                        else -> vTranslate?.apply {
                                            x = width / 2 - scale * (sWidthSelect / 2)
                                            y = height / 2 - scale * (sHeightSelect / 2)
                                        }
                                    }
                                    fitToBounds(true)
                                    refreshRequiredTiles(false)
                                }
                            }
                        }
                    }
                    isQuickScaling -> quickScaleVStart?.let { quickScaleVStart ->
                        var dist = abs(quickScaleVStart.y - event.y) * 2 + quickScaleThreshold
                        if (quickScaleLastDistance == -1f) quickScaleLastDistance = dist
                        quickScaleVLastPoint?.set(0f, event.y)
                        (abs(1 - dist / quickScaleLastDistance) * 0.5f).let { spanDiff ->
                            if (spanDiff > 0.03f || quickScaleMoved) {
                                quickScaleMoved = true
                                val previousScale = scale.toDouble()
                                scale = max(
                                    minScale, min(
                                        scaleMax, scale * when {
                                            quickScaleLastDistance > 0 -> when {
                                                event.y > quickScaleVLastPoint!!.y -> 1 + spanDiff
                                                else -> 1 - spanDiff
                                            }
                                            else -> 1f
                                        }
                                    )
                                )
                                when {
                                    isEnabledPan -> vCenterStart?.let { vCenterStart ->
                                        vTranslateStart?.let { vTranslateStart ->
                                            vTranslate?.apply {
                                                x =
                                                    vCenterStart.x - (vCenterStart.x - vTranslateStart.x) * (scale / scaleStart)
                                                y =
                                                    vCenterStart.y - (vCenterStart.y - vTranslateStart.y) * (scale / scaleStart)
                                            }
                                            if (previousScale * sHeightSelect < height && scale * sHeightSelect >= height ||
                                                previousScale * sWidthSelect < width && scale * sWidthSelect >= width
                                            ) {
                                                fitToBounds(true)
                                                quickScaleSCenter?.let { sourceToViewCoord(it) }
                                                    ?.let { vCenterStart.set(it) }
                                                vTranslate?.let { vTranslateStart.set(it) }
                                                scaleStart = scale
                                                dist = 0f
                                            }
                                        }
                                    }
                                    sRequestedCenter != null -> vTranslate?.apply {
                                        x = width / 2 - scale * sRequestedCenter!!.x
                                        y = height / 2 - scale * sRequestedCenter!!.y
                                    }
                                    else -> vTranslate?.apply {
                                        x = width / 2 - scale * (sWidthSelect / 2)
                                        y = height / 2 - scale * (sHeightSelect / 2)
                                    }
                                }
                            }
                        }
                        quickScaleLastDistance = dist
                        fitToBounds(true)
                        refreshRequiredTiles(false)
                        consumed = true
                    }
                    !isZooming -> vCenterStart?.let { it0 ->
                        vTranslateStart?.let { it1 ->
                            abs(event.x - it0.x).let { dx ->
                                abs(event.y - it0.y).let { dy ->
                                    (density * 5).let { offset ->
                                        if (dx > offset || dy > offset || isPanning) {
                                            consumed = true
                                            vTranslate?.apply {
                                                x = it1.x + (event.x - it0.x)
                                                y = it1.y + (event.y - it0.y)
                                            }
                                            val lastX = vTranslate?.x
                                            val lastY = vTranslate?.y
                                            fitToBounds(true)
                                            (lastX != vTranslate?.x).let { atXEdge ->
                                                (lastY != vTranslate?.y).let { atYEdge ->
                                                    when {
                                                        !(atXEdge && dx > dy && !isPanning) && !(atYEdge && dy > dx && !isPanning) &&
                                                                (!atXEdge || !atYEdge || (lastY == vTranslate?.y && dy > offset * 3) || isPanning) ->
                                                            isPanning = true
                                                        dx > offset || dy > offset -> {
                                                            touchCountMax = 0
                                                            longClickHandler
                                                                .removeMessages(MESSAGE_LONG_CLICK)
                                                            requestDisallowInterceptTouchEvent(false)
                                                        }
                                                        else -> {
                                                        }
                                                    }
                                                }
                                            }
                                            if (!isEnabledPan) {
                                                vTranslate?.apply {
                                                    x = it1.x
                                                    y = it1.y
                                                }
                                                requestDisallowInterceptTouchEvent(false)
                                            }
                                            refreshRequiredTiles(false)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (consumed) {
                    longClickHandler.removeMessages(MESSAGE_LONG_CLICK)
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_POINTER_2_UP -> {
                longClickHandler.removeMessages(MESSAGE_LONG_CLICK)
                if (isQuickScaling) {
                    isQuickScaling = false
                    if (!quickScaleMoved) doubleTapZoom(quickScaleSCenter, vCenterStart)
                }
                if (touchCountMax > 0 && (isZooming || isPanning)) {
                    if (isZooming && touchCount == 2) {
                        isPanning = true
                        vTranslate?.run { vTranslateStart?.set(x, y) }
                        vCenterStart?.run {
                            when (event.actionIndex) {
                                1 -> set(event.getX(0), event.getY(0))
                                else -> set(event.getX(1), event.getY(1))
                            }
                        }
                    }
                    if (touchCount < 3) isZooming = false
                    if (touchCount < 2) {
                        isPanning = false
                        touchCountMax = 0
                    }
                    refreshRequiredTiles(true)
                    return true
                }
                if (touchCount == 1) {
                    isZooming = false
                    isPanning = false
                    touchCountMax = 0
                }
                return true
            }
        }
        return false
    }

    private fun distance(x0: Float, x1: Float, y0: Float, y1: Float): Float =
        sqrt((x0 - x1).pow(2) + (y0 - y1).pow(2))

    @JvmOverloads
    fun sourceToViewCoord(sxy: PointF, vTarget: PointF = PointF()): PointF? =
        sourceToViewCoord(sxy.x, sxy.y, vTarget)

    @JvmOverloads
    fun sourceToViewCoord(sx: Float, sy: Float, vTarget: PointF = PointF()): PointF? =
        vTranslate?.let { vTarget.apply { set(sourceToViewX(sx), sourceToViewY(sy)) } }

    private fun sourceToViewX(sx: Float): Float = vTranslate?.run { sx * scale + x } ?: Float.NaN
    private fun sourceToViewY(sy: Float): Float = vTranslate?.run { sy * scale + y } ?: Float.NaN
    val recycle = {
        reset(true)
        paintBitmap = null
        paintDebug = null
        paintTileBg = null
    }
    private var paintBitmap: Paint? = null
    private var paintDebug: Paint? = null
    private val createPaints = {
        if (paintBitmap == null) paintBitmap = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }
        if (paintDebug == null && isDebug) paintDebug = Paint().apply {
            textSize = 18f
            color = Color.MAGENTA
            style = Paint.Style.STROKE
        }
    }
    private val preDraw = {
        if (width != 0 && height != 0 && sWidth > 0 && sHeight > 0) {
            if (pendingScale != null && sPendingCenter != null) {
                scale = pendingScale!!
                (vTranslate ?: PointF().apply { vTranslate = this }).apply {
                    x = width / 2 - scale * sPendingCenter!!.x
                    y = height / 2 - scale * sPendingCenter!!.y
                }
                pendingScale = null
                sPendingCenter = null
                fitToBounds(true)
                refreshRequiredTiles(true)
            }
            fitToBounds(false)
        }
    }
    private val checkReady: Boolean = (width > 0 && height > 0 && sWidth > 0 && sHeight > 0 &&
            (bitmap != null || isBaseLayerReady)).apply {
        if (!isReady && this) {
            preDraw
            isReady = true
            onReady
            onImageEventListener?.onReady()
        }
    }
    protected val onReady = {}
    private val isBaseLayerReady: Boolean
        get() = when {
            bitmap == null || bitmapIsPreview -> tileMap?.let {
                for ((key, value) in it) {
                    if (key == fullImageSampleSize) for (tile in value) {
                        if (tile.loading || tile.bitmap == null) return false
                    }
                }
                true
            } ?: false
            else -> true
        }
    private val srcArray = FloatArray(8)
    private val dstArray = FloatArray(8)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        createPaints
        if (sWidth == 0 || sHeight == 0 || width == 0 || height == 0) return
        decoder?.let { if (tileMap == null) initialiseBaseLayer(getMaxBitmapDimensions(canvas)) }
        if (checkReady) {
            preDraw
            anim?.let {
                (nowMillis - it.time).let { elapsed ->
                    val scaleBefore = scale
                    min(elapsed, it.duration).let { scaleElapsed ->
                        scale = ease(
                            it.easing, scaleElapsed, it.scaleStart,
                            it.scaleEnd - it.scaleStart, it.duration
                        )
                        vTranslate?.apply {
                            x -= sourceToViewX(it.sCenterEnd!!.x) - ease(
                                it.easing, scaleElapsed, it.vFocusStart!!.x,
                                it.vFocusEnd!!.x - it.vFocusStart!!.x, it.duration
                            )
                            y -= sourceToViewY(it.sCenterEnd!!.y) - ease(
                                it.easing, scaleElapsed, it.vFocusStart!!.y,
                                it.vFocusEnd!!.y - it.vFocusStart!!.y, it.duration
                            )
                        }
                    }
                    (elapsed > it.duration).let { finished ->
                        fitToBounds(finished || it.scaleStart == it.scaleEnd)
                        sendStateChanged(
                            scaleBefore, (vTranslateBefore
                                ?: PointF(0f, 0f).apply { vTranslateBefore = this })
                                .apply { vTranslate?.let { p -> set(p) } }, it.origin
                        )
                        refreshRequiredTiles(finished)
                        if (finished) {
                            try {
                                it.listener?.onComplete()
                            } catch (e: Exception) {
                                warn("${loggerTag}->Error thrown by animation listener")
                            }
                            anim = null
                        }
                    }
                }
                invalidate()
            }
            when {
                tileMap != null && isBaseLayerReady ->
                    min(fullImageSampleSize, calculateInSampleSize(scale)).let { sampleSize ->
                        var hasMissingTiles = false
                        tileMap?.let { map ->
                            for ((key, value) in map) {
                                if (key == sampleSize) for (tile in value) {
                                    if (tile.run { visible && (loading || bitmap == null) })
                                        hasMissingTiles = true
                                }
                            }
                            for ((key, value) in map) {
                                if (key == sampleSize || hasMissingTiles) for (tile in value) {
                                    sourceToViewRect(tile.sRect!!, tile.vRect!!)
                                    when {
                                        !tile.loading && tile.bitmap != null -> {
                                            paintTileBg?.let { tileBgPaint ->
                                                tile.vRect?.run {
                                                    canvas.drawRect(this, tileBgPaint)
                                                }
                                            }
                                            (mMatrix ?: Matrix().apply { mMatrix = this }).reset()
                                            tile.bitmap?.run {
                                                setMatrixArray(
                                                    srcArray, 0f, 0f, width.toFloat(),
                                                    0f, width.toFloat(), height.toFloat(), 0f,
                                                    height.toFloat()
                                                )
                                            }
                                            tile.vRect?.run {
                                                when (requiredRotation) {
                                                    ORIENTATION_0 -> setMatrixArray(
                                                        dstArray, left.toFloat(), top.toFloat(),
                                                        right.toFloat(), top.toFloat(),
                                                        right.toFloat(), bottom.toFloat(),
                                                        left.toFloat(), bottom.toFloat()
                                                    )
                                                    ORIENTATION_90 -> setMatrixArray(
                                                        dstArray, right.toFloat(), top.toFloat(),
                                                        right.toFloat(), bottom.toFloat(),
                                                        left.toFloat(), bottom.toFloat(),
                                                        left.toFloat(), top.toFloat()
                                                    )
                                                    ORIENTATION_180 -> setMatrixArray(
                                                        dstArray, right.toFloat(), bottom.toFloat(),
                                                        left.toFloat(), bottom.toFloat(),
                                                        left.toFloat(), top.toFloat(),
                                                        right.toFloat(), top.toFloat()
                                                    )
                                                    ORIENTATION_270 -> setMatrixArray(
                                                        dstArray, left.toFloat(), bottom.toFloat(),
                                                        left.toFloat(), top.toFloat(),
                                                        right.toFloat(), top.toFloat(),
                                                        right.toFloat(), bottom.toFloat()
                                                    )
                                                }
                                            }
                                            tile.bitmap?.run {
                                                mMatrix?.apply {
                                                    setPolyToPoly(srcArray, 0, dstArray, 0, 4)
                                                }?.let { canvas.drawBitmap(this, it, paintBitmap) }
                                            }
                                            if (isDebug) tile.vRect
                                                ?.run { canvas.drawRect(this, paintDebug!!) }
                                        }
                                        tile.loading && isDebug -> tile.vRect?.run {
                                            canvas.drawText(
                                                "LOADING", left + 5f, top + 35f, paintDebug!!
                                            )
                                        }
                                    }
                                    if (tile.visible && isDebug) tile.sRect?.run {
                                        canvas.drawText(
                                            "ISS ${tile.sampleSize} RECT $top,$left,$bottom,$right",
                                            left + 5f, top + 15f, paintDebug!!
                                        )
                                    }
                                }
                            }
                        }
                    }
                bitmap != null -> bitmap?.let {
                    (mMatrix ?: Matrix().apply { mMatrix = this }).apply {
                        reset()
                        postScale(
                            if (bitmapIsPreview) scale * (sWidth.toFloat() / it.width) else scale,
                            if (bitmapIsPreview) scale * (sHeight.toFloat() / it.height) else scale
                        )
                        postRotate(requiredRotation.toFloat())
                        vTranslate?.run { postTranslate(x, y) }
                    }.run {
                        when (requiredRotation) {
                            ORIENTATION_90 -> postTranslate(scale * sHeight, 0f)
                            ORIENTATION_180 -> postTranslate(scale * sWidth, scale * sHeight)
                            ORIENTATION_270 -> postTranslate(0f, scale * sWidth)
                            else -> {
                            }
                        }
                    }
                    paintTileBg?.let { tileBgPaint ->
                        (sRect ?: RectF().apply { sRect = this }).set(
                            0f, 0f, (if (bitmapIsPreview) it.width else sWidth).toFloat(),
                            (if (bitmapIsPreview) it.height else sHeight).toFloat()
                        )
                        mMatrix?.mapRect(sRect)
                        sRect?.let { it1 -> canvas.drawRect(it1, tileBgPaint) }
                    }
                    mMatrix?.run { canvas.drawBitmap(it, this, paintBitmap) }
                }
            }
            if (isDebug) canvas.run {
                paintDebug?.apply {
                    drawText(
                        "Scale: ${String.format(Locale.ENGLISH, "%.2f", scale)}",
                        5f, 15f, this
                    )
                    vTranslate?.let {
                        drawText(
                            "Translate: ${
                                String.format(Locale.ENGLISH, "%.2f", it.x)
                            }:${
                                String.format(Locale.ENGLISH, "%.2f", it.y)
                            }", 5f, 35f, this
                        )
                    }
                    center?.let {
                        drawText(
                            "Source center: ${
                                String.format(Locale.ENGLISH, "%.2f", it.x)
                            }:${
                                String.format(Locale.ENGLISH, "%.2f", it.y)
                            }", 5f, 55f, this
                        )
                    }
                    strokeWidth = 2f
                    anim?.let { anim ->
                        anim.sCenterStart?.let {
                            sourceToViewCoord(it)?.let { vCenterStart ->
                                drawCircle(vCenterStart.x, vCenterStart.y, 10f, this)
                            }
                        }
                        anim.sCenterEndRequested?.let {
                            sourceToViewCoord(it)?.let { vCenterEndRequested ->
                                drawCircle(
                                    vCenterEndRequested.x, vCenterEndRequested.y, 20f,
                                    apply { color = Color.RED })
                            }
                        }
                        anim.sCenterEnd?.let {
                            sourceToViewCoord(it)?.let { vCenterEnd ->
                                drawCircle(
                                    vCenterEnd.x, vCenterEnd.y, 25f, apply { color = Color.BLUE })
                            }
                        }
                        drawCircle(
                            (width / 2).toFloat(), (height / 2).toFloat(), 30f,
                            apply { color = Color.CYAN })
                    }
                    vCenterStart?.let { drawCircle(it.x, it.y, 20f, apply { color = Color.RED }) }
                    quickScaleSCenter?.let {
                        drawCircle(
                            sourceToViewX(it.x), sourceToViewY(it.y), 35f,
                            apply { color = Color.BLUE })
                    }
                    quickScaleVStart?.let {
                        drawCircle(it.x, it.y, 30f, apply { color = Color.CYAN })
                    }
                    color = Color.MAGENTA
                    strokeWidth = 1f
                }
            }
        }
    }

    @Synchronized
    private fun initialiseBaseLayer(maxTileDimensions: Point) {
        debugScale(
            "initialiseBaseLayer maxTileDimensions=%dx%d",
            maxTileDimensions.x, maxTileDimensions.y
        )
        satTemp = ScaleAndTranslate(0f, PointF(0f, 0f))
        satTemp?.let { satTemp ->
            fitToBounds(true, satTemp)
            fullImageSampleSize =
                calculateInSampleSize(satTemp.scaleData).let { if (it > 1) it / 2 else it }
        }
        when {
            fullImageSampleSize == 1 && sRegion == null && sWidthSelect < maxTileDimensions.x && sHeightSelect < maxTileDimensions.y -> {
                decoder?.recycle()
                decoder = null
                uri?.let { execute(TaskBitmapLoad(this, context, imageDecoder, it, false)) }
            }
            else -> {
                initialiseTileMap(maxTileDimensions)
                tileMap?.let { map ->
                    map[fullImageSampleSize]?.let { baseGrid ->
                        for (baseTile in baseGrid) {
                            decoder?.let { execute(TaskTileLoad(this, it, baseTile)) }
                        }
                    }
                }
                refreshRequiredTiles(true)
            }
        }
    }

    private fun initialiseTileMap(maxTileDimensions: Point) {
        debugScale(
            "initialiseTileMap maxTileDimensions=%dx%d",
            maxTileDimensions.x, maxTileDimensions.y
        )
        var xTiles = 1
        var yTiles = 1
        var imageSampleSize = fullImageSampleSize
        tileMap = mutableMapOf()
        while (true) {
            var sTileWidth = sWidthSelect / xTiles
            var subTileWidth = sTileWidth / imageSampleSize
            while (subTileWidth + xTiles + 1 > maxTileDimensions.x || subTileWidth > width * 1.25 && imageSampleSize < fullImageSampleSize) {
                xTiles += 1
                sTileWidth = sWidthSelect / xTiles
                subTileWidth = sTileWidth / imageSampleSize
            }
            var sTileHeight = sHeightSelect / yTiles
            var subTileHeight = sTileHeight / imageSampleSize
            while (subTileHeight + yTiles + 1 > maxTileDimensions.y || subTileHeight > height * 1.25 && imageSampleSize < fullImageSampleSize) {
                yTiles += 1
                sTileHeight = sHeightSelect / yTiles
                subTileHeight = sTileHeight / imageSampleSize
            }
            tileMap?.let {
                it[imageSampleSize] = ArrayList<Tile>(xTiles * yTiles).apply {
                    for (x in 0 until xTiles) {
                        for (y in 0 until yTiles) {
                            add(Tile().apply {
                                sampleSize = imageSampleSize
                                visible = imageSampleSize == fullImageSampleSize
                                vRect = Rect(0, 0, 0, 0)
                                sRect = Rect(
                                    x * sTileWidth, y * sTileHeight,
                                    if (x == xTiles - 1) sWidthSelect else (x + 1) * sTileWidth,
                                    if (y == yTiles - 1) sHeightSelect else (y + 1) * sTileHeight
                                )
                                fileSRect = Rect(sRect)
                            })
                        }
                    }
                }
            }
            if (imageSampleSize == 1) break else imageSampleSize /= 2
        }
    }

    private fun getMaxBitmapDimensions(canvas: Canvas): Point {
        var maxWidth = 2048
        var maxHeight = 2048
        if (aboveIceCreamSandwich) try {
            maxWidth = Canvas::class.java.getMethod("getMaximumBitmapWidth").invoke(canvas) as Int
            maxHeight = Canvas::class.java.getMethod("getMaximumBitmapHeight").invoke(canvas) as Int
        } catch (e: Exception) {
        }
        return Point(min(maxWidth, maxTileWidth), min(maxHeight, maxTileHeight))
    }

    private fun ease(type: Int, time: Long, from: Float, change: Float, duration: Long): Float =
        when (type) {
            EASE_OUT_QUAD -> easeOutQuad(time, from, change, duration)
            EASE_IN_OUT_QUAD -> easeInOutQuad(time, from, change, duration)
            else -> throw IllegalStateException("Unexpected easing type: $type")
        }

    private fun easeOutQuad(time: Long, from: Float, change: Float, duration: Long): Float =
        (time.toFloat() / duration.toFloat()).let { progress -> from - change * progress * (progress - 2) }

    private fun easeInOutQuad(time: Long, from: Float, change: Float, duration: Long): Float {
        var timeF = time / (duration / 2f)
        return when {
            timeF < 1 -> from + change / 2f * timeF.pow(2)
            else -> {
                timeF--
                from - change / 2f * (timeF.pow(2) - timeF * 2 - 1)
            }
        }
    }

    var minTileDpi = -1
        set(dpi) {
            field = min(resources.displayMetrics.run { xdpi + ydpi } / 2, dpi.toFloat()).toInt()
            if (isReady) {
                reset(false)
                invalidate()
            }
        }

    private fun calculateInSampleSize(scale: Float): Int =
        (if (minTileDpi > 0) minTileDpi / (resources.displayMetrics.run { xdpi + ydpi } / 2) * scale else scale).let {
            (sWidthSelect * it).toInt().let { reqWidth ->
                (sHeightSelect * it).toInt().let { reqHeight ->
                    var inSampleSize = 1
                    return when {
                        reqWidth == 0 || reqHeight == 0 -> 32
                        else -> {
                            if (sHeightSelect > reqHeight || sWidthSelect > reqWidth)
                                (sHeightSelect.toFloat() / reqHeight.toFloat()).roundToInt()
                                    .let { heightRatio ->
                                        (sWidthSelect.toFloat() / reqWidth.toFloat()).roundToInt()
                                            .let { widthRatio ->
                                                inSampleSize =
                                                    if (heightRatio < widthRatio) heightRatio else widthRatio
                                            }
                                    }
                            var power = 1
                            while (power * 2 < inSampleSize) {
                                power *= 2
                            }
                            power
                        }
                    }
                }
            }
        }

    private fun sourceToViewRect(sRect: Rect, vTarget: Rect): Rect = vTarget.apply {
        set(
            sourceToViewX(sRect.left.toFloat()).toInt(),
            sourceToViewY(sRect.top.toFloat()).toInt(),
            sourceToViewX(sRect.right.toFloat()).toInt(),
            sourceToViewY(sRect.bottom.toFloat()).toInt()
        )
    }

    private fun setMatrixArray(
        array: FloatArray, f0: Float, f1: Float, f2: Float, f3: Float,
        f4: Float, f5: Float, f6: Float, f7: Float
    ) {
        array[0] = f0
        array[1] = f1
        array[2] = f2
        array[3] = f3
        array[4] = f4
        array[5] = f5
        array[6] = f6
        array[7] = f7
    }

    companion object {
        const val ORIENTATION_0 = 0
        const val ORIENTATION_90 = 90
        const val ORIENTATION_180 = 180
        const val ORIENTATION_270 = 270
        const val ORIENTATION_USE_EXIF = -1
        const val ZOOM_FOCUS_FIXED = 1
        const val ZOOM_FOCUS_CENTER = 2
        const val ZOOM_FOCUS_CENTER_IMMEDIATE = 3
        const val EASE_OUT_QUAD = 1
        const val EASE_IN_OUT_QUAD = 2
        const val PAN_LIMIT_INSIDE = 1
        const val PAN_LIMIT_OUTSIDE = 2
        const val PAN_LIMIT_CENTER = 3
        const val SCALE_TYPE_CENTER_INSIDE = 1
        const val SCALE_TYPE_CENTER_CROP = 2
        const val SCALE_TYPE_CUSTOM = 3
        const val ORIGIN_ANIM = 1
        const val ORIGIN_TOUCH = 2
        const val ORIGIN_FLING = 3
        const val ORIGIN_DOUBLE_TAP_ZOOM = 4
        private val VALID_ORIENTATION: MutableList<Int> = mutableListOf(
            ORIENTATION_0, ORIENTATION_90, ORIENTATION_180, ORIENTATION_270, ORIENTATION_USE_EXIF
        )
        private val VALID_ZOOM_FOCUS: MutableList<Int> =
            mutableListOf(ZOOM_FOCUS_FIXED, ZOOM_FOCUS_CENTER, ZOOM_FOCUS_CENTER_IMMEDIATE)
        private val VALID_EASE: MutableList<Int> = mutableListOf(EASE_OUT_QUAD, EASE_IN_OUT_QUAD)
        private val VALID_PAN_LIMIT: MutableList<Int> =
            mutableListOf(PAN_LIMIT_INSIDE, PAN_LIMIT_OUTSIDE, PAN_LIMIT_CENTER)
        private val VALID_SCALE_TYPE: MutableList<Int> =
            mutableListOf(SCALE_TYPE_CENTER_INSIDE, SCALE_TYPE_CENTER_CROP, SCALE_TYPE_CUSTOM)
        private val VALID_ORIGIN: MutableList<Int> =
            mutableListOf(ORIGIN_ANIM, ORIGIN_TOUCH, ORIGIN_FLING, ORIGIN_DOUBLE_TAP_ZOOM)
        private const val MESSAGE_LONG_CLICK = 1
        var tileSizeAuto: Int = Integer.MAX_VALUE
    }
}