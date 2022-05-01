package top.autoget.automap

import android.graphics.Bitmap
import androidx.collection.LruCache
import com.android.volley.toolbox.ImageLoader

class MapCloudImageCache : ImageLoader.ImageCache {
    private val imageCache =
        LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024 / 10).toInt())//5*1024

    override fun getBitmap(url: String): Bitmap? = if (url == "") null else imageCache[url]
    override fun putBitmap(url: String, bitmap: Bitmap) {
        if (getBitmap(url) == null) imageCache.put(url, bitmap)
    }
}