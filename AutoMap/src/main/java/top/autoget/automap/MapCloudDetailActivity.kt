package top.autoget.automap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.services.cloud.CloudImage
import com.amap.api.services.cloud.CloudItem
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.Volley
import top.autoget.autokit.ScreenKit.screenDensity
import top.autoget.automap.databinding.ActivityCloudDetailBinding
import top.autoget.automap.databinding.LayoutItemBinding
import top.autoget.automap.databinding.LayoutPhotoBinding

class MapCloudDetailActivity : AppCompatActivity() {
    private var cloudItem: CloudItem? = null
    private var cloudImages: MutableList<CloudImage> = mutableListOf()
    private var activityCloudDetailBinding: ActivityCloudDetailBinding =
        ActivityCloudDetailBinding.inflate(layoutInflater)
    private val setGridView: GridView = cloudImages.run {
        activityCloudDetailBinding.grid.apply {
            layoutParams = LinearLayout.LayoutParams(
                (size * (60 + 4) * screenDensity).toInt(),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            columnWidth = (60 * screenDensity).toInt()
            horizontalSpacing = 5
            stretchMode = GridView.NO_STRETCH
            numColumns = size
            adapter = GridViewAdapter(applicationContext)
            onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ -> showImage(position) }
        }
    }

    inner class GridViewAdapter(var context: Context) : BaseAdapter() {
        override fun getCount(): Int = cloudImages.size
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getItem(position: Int): CloudImage = cloudImages[position]
        private var layoutPhotoBinding: LayoutPhotoBinding =
            LayoutPhotoBinding.inflate(layoutInflater)

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View =
            layoutPhotoBinding.root.apply {
                layoutPhotoBinding.ItemImage.apply {
                    setDefaultImageResId(R.mipmap.location_marker)//临时
                    setImageUrl(
                        cloudImages[position].preurl,
                        ImageLoader(Volley.newRequestQueue(context), MapCloudImageCache())
                    )
                }
            }
    }

    private fun showImage(position: Int) = cloudImages.run {
        if (isNotEmpty()) Intent(
            this@MapCloudDetailActivity, MapCloudPreviewActivity::class.java
        ).apply {
            intent.putExtra("clouditem", cloudItem)
            intent.putExtra("position", position)
        }.let { startActivity(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutItemBinding: LayoutItemBinding = LayoutItemBinding.inflate(layoutInflater)
        setContentView(layoutItemBinding.root)
        cloudItem = intent.getParcelableExtra("clouditem")
        cloudItem?.run {
            cloudImages = cloudImage as MutableList<CloudImage>
            if (cloudImages.isNotEmpty()) setGridView
            activityCloudDetailBinding.poiidText.text = id
            activityCloudDetailBinding.locationText.text =
                latLonPoint.run { "{$latitude,$longitude}" }
            activityCloudDetailBinding.nameText.text = title
            activityCloudDetailBinding.addressText.text = snippet
            activityCloudDetailBinding.createtimeText.text = createtime
            activityCloudDetailBinding.updateTimeText.text = updatetime
            activityCloudDetailBinding.distanceText.text = distance.toString()
            for ((key, value) in customfield) {
                layoutItemBinding.root.apply {
                    layoutItemBinding.poiFieldId.text = key
                    layoutItemBinding.poiValueId.apply {
                        text = value
                        setTextColor(resources.getColor(R.color.black, null))//AutoSee
                    }
                }.let { activityCloudDetailBinding.container.addView(it) }
            }
        }
    }
}