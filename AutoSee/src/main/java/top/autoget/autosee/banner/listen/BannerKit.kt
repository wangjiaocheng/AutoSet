package top.autoget.autosee.banner.listen

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

object BannerKit {
    const val LOOP_COUNT = 5000
    const val LOOP_TAIL_MODE = 0x1001
    const val LOOP_MODE = 0x1002
    const val GLIDE_MODE = 0x1002
    const val VIEWPAGER_DATA_URL = 0x2002
    const val VIEWPAGER_DATA_RES = 0x2003
    const val VIEWPAGER_DATA_VIEW = 0x2004
    fun initSwitchTime(context: Context?, viewPager2: ViewPager2, time: Int) = try {
        val recyclerView = viewPager2.getChildAt(0) as RecyclerView
        recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        val proxyLayoutManger = ProxyLayoutManger(context, viewPager2.orientation, time)
        recyclerView.layoutManager = proxyLayoutManger
        ViewPager2::class.java.getDeclaredField("mLayoutManager")
            .apply { isAccessible = true }.let { it[viewPager2] = proxyLayoutManger }
        ViewPager2::class.java.getDeclaredField("mPageTransformerAdapter")
            .apply { isAccessible = true }.let {
                it[viewPager2]?.let { adapter ->
                    adapter.javaClass.getDeclaredField("mLayoutManager")
                        .apply { isAccessible = true }
                        .let { manager -> manager[adapter] = proxyLayoutManger }
                }
            }
        ViewPager2::class.java.getDeclaredField("mScrollEventAdapter")
            .apply { isAccessible = true }.let {
                it[viewPager2]?.let { adapter ->
                    adapter.javaClass.getDeclaredField("mLayoutManager")
                        .apply { isAccessible = true }
                        .let { manager -> manager[adapter] = proxyLayoutManger }
                }
            }
    } catch (e: NoSuchFieldException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    }

    private class ProxyLayoutManger(context: Context?, orientation: Int, var time: Int) :
        LinearLayoutManager(context, orientation, false) {
        override fun smoothScrollToPosition(
            recyclerView: RecyclerView, state: RecyclerView.State, position: Int
        ) = object : LinearSmoothScroller(recyclerView.context) {
            override fun calculateTimeForDeceleration(dx: Int): Int = (time * (1 - .3356)).toInt()
        }.apply { targetPosition = position }.let { startSmoothScroll(it) }
    }
}