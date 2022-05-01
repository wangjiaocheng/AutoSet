package top.autoget.autosee.flow.common

import android.content.Context
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager

object ViewPagerKit {
    const val LOOP_COUNT = 5000
    const val LOOP_TAIL_MODE = 0x1001
    const val LOOP_MODE = 0x1002
    const val GLIDE_MODE = 0x1002
    const val VIEWPAGER_DATA_URL = 0x2002
    const val VIEWPAGER_DATA_RES = 0x2003
    const val VIEWPAGER_DATA_VIEW = 0x2004
    fun getViewPageClickItem(viewPager: ViewPager?): Int = viewPager?.let {
        try {
            viewPager.javaClass.getDeclaredField("mCurItem")
                .apply { isAccessible = true }.getInt(viewPager)
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    } ?: 0

    fun initSwitchTime(context: Context?, viewPager: ViewPager?, time: Int) = try {
        ViewPager::class.java.getDeclaredField("mScroller")
            .apply { isAccessible = true }[viewPager] = ViewPagerScroller(context, time)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    class ViewPagerScroller(context: Context?, var time: Int) : Scroller(context) {
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, time)
        }
    }
}