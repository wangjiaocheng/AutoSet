package top.autoget.automap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.amap.api.services.cloud.CloudItem
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.NetworkImageView
import com.android.volley.toolbox.Volley
import top.autoget.automap.databinding.ActivityCloudPreviewBinding

class MapCloudPreviewActivity : AppCompatActivity() {
    private inner class
    MapPagerAdapter(private val listViews: MutableList<NetworkImageView>) : PagerAdapter() {
        override fun finishUpdate(container: View) {}
        override fun isViewFromObject(view: View, any: Any): Boolean = view === any
        override fun getItemPosition(any: Any): Int = POSITION_NONE
        private val size: Int = listViews.size
        override fun getCount(): Int = size
        override fun destroyItem(container: View, position: Int, any: Any) =
            (container as ViewPager).removeView(listViews[position % size])

        override fun instantiateItem(container: View, position: Int): Any = try {
            listViews[position % size].apply { (container as ViewPager).addView(this, 0) }
        } catch (e: Exception) {
        }
    }

    private var requestQueue: RequestQueue? = null
    private var imageLoader: ImageLoader? = null
    private var listViews: MutableList<NetworkImageView>? = null
    private var adapter: MapPagerAdapter? = null
    private var pageIndex = 0
    private var pageCount = 0
    private val pageChangeListener: ViewPager.OnPageChangeListener =
        object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                downLoadImage(position)
                pageIndex = position
                activityCloudPreviewBinding.titleDesText.text = "${(pageIndex + 1)}/$pageCount"
            }
        }
    private var cloudItem: CloudItem? = null
    private val initListViews = {
        if (listViews == null) listViews = mutableListOf()
        NetworkImageView(this).apply {
            setBackgroundColor(-0x1000000)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
        }.run { listViews?.add(this) }
    }
    private var activityCloudPreviewBinding: ActivityCloudPreviewBinding =
        ActivityCloudPreviewBinding.inflate(layoutInflater)

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(activityCloudPreviewBinding.root)
        requestQueue = Volley.newRequestQueue(applicationContext)
        imageLoader = ImageLoader(requestQueue, MapCloudImageCache())
        activityCloudPreviewBinding.titleDesText.paint.isFakeBoldText = true
        activityCloudPreviewBinding.viewpagerPhoto.adapter =
            listViews?.let { MapPagerAdapter(it) }.apply { adapter = this }
        activityCloudPreviewBinding.viewpagerPhoto.currentItem =
            intent.getIntExtra("position", 0).apply { pageIndex = this }
        activityCloudPreviewBinding.viewpagerPhoto.setOnPageChangeListener(pageChangeListener)
        activityCloudPreviewBinding.back.setOnClickListener {
            Intent(this@MapCloudPreviewActivity, MapCloudDetailActivity::class.java)
                .apply { addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) }.let { startActivity(it) }
            finish()
        }
        cloudItem = intent.getParcelableExtra("clouditem")
        pageCount = cloudItem?.cloudImage?.size ?: 0
        for (i in 0 until pageCount) {
            initListViews
        }
        if (pageCount > 0) activityCloudPreviewBinding.titleDesText.text =
            "${(pageIndex + 1)}/$pageCount"
        if (pageIndex == 0) downLoadImage(0)
    }

    @Synchronized
    private fun downLoadImage(index: Int) {
        activityCloudPreviewBinding.pbLoading.visibility = View.VISIBLE
        cloudItem?.cloudImage?.get(index)?.url.let { imageUrl ->
            listViews?.get(index)?.setImageUrl(imageUrl, imageLoader)
            imageLoader?.get(imageUrl, object : ImageLoader.ImageListener {
                override fun onErrorResponse(error: VolleyError) {
                    activityCloudPreviewBinding.pbLoading.visibility = View.GONE
                }

                override fun onResponse(
                    response: ImageLoader.ImageContainer, isImmediate: Boolean
                ) = response.bitmap?.let {
                    activityCloudPreviewBinding.pbLoading.visibility = View.GONE
                    adapter?.notifyDataSetChanged()
                } ?: Unit
            })
        }
    }

    fun onBackClick(view: View?) = finish()
}