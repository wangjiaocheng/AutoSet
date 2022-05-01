package top.autoget.automap

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class MapOfflinePagerAdapter(
    private val viewPager: ViewPager?,
    private val offlineAllList: View, private val offlineDownloadedList: View
) : PagerAdapter() {
    override fun getCount(): Int = 2
    override fun isViewFromObject(view: View, any: Any): Boolean = view === any
    override fun startUpdate(container: ViewGroup) {}
    override fun finishUpdate(container: ViewGroup) {}
    override fun saveState(): Parcelable? = null
    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
    override fun instantiateItem(container: ViewGroup, position: Int): Any = when (position) {
        0 -> offlineAllList.apply { viewPager?.addView(this) }
        else -> offlineDownloadedList.apply { viewPager?.addView(this) }
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        when (position) {
            0 -> viewPager?.removeView(offlineAllList)
            else -> viewPager?.removeView(offlineDownloadedList)
        }
    }
}