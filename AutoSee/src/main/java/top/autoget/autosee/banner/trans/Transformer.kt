package top.autoget.autosee.banner.trans

import android.view.View
import androidx.viewpager2.widget.ViewPager2

abstract class Transformer {
    val transformer: ViewPager2.PageTransformer
        get() = ViewPager2.PageTransformer { page, position -> transform(page, position) }

    abstract fun transform(view: View, position: Float)
}