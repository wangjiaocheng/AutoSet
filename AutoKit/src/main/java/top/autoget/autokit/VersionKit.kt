package top.autoget.autokit

import android.os.Build

object VersionKit {
    val aboveAstro: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE
    val aboveBender: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.BASE_1_1
    val aboveCupcake: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE
    val aboveDonut: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT
    val aboveEclair: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR
    val aboveEclair01: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_0_1
    val aboveEclairMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1
    val aboveFroyo: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO
    val aboveGingerbread: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
    val aboveGingerbreadMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1
    val aboveHoneycomb: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
    val aboveHoneycombMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1
    val aboveHoneycombMR2: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2
    val aboveIceCreamSandwich: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
    val aboveIceCreamSandwichMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1
    val aboveJellyBean: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
    val aboveJellyBeanMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
    val aboveJellyBeanMR2: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
    val aboveKitKat: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    val aboveKitKatWatch: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH
    val aboveLollipop: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    val aboveLollipopMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
    val aboveMarshmallow: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    val aboveNougat: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    val aboveNougatMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
    val aboveOreo: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    val aboveOreoMR1: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
    val abovePie: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    val aboveQ: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    val aboveR: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    val aboveS: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}