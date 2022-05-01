package top.autoget.autosee.banner.indicat

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback

class IndicatorText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), ViewPager.OnPageChangeListener {
    private var mCount = 0
    private var mTextString = ""
    fun addPagerData(count: Int, viewPager2: ViewPager2?) {
        if (count != 0) {
            mCount = count
            mTextString = "1/$mCount"
            text = mTextString
            viewPager2?.let {
                viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        viewPagerSelected(position)
                    }
                })
                viewPagerSelected(viewPager2.currentItem)
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) = viewPagerSelected(position)
    private fun viewPagerSelected(position: Int) {
        mTextString = "${((position % mCount) + 1)}/$mCount"
        text = mTextString
    }
}