# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview*{public *;}
# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile
#高德3D地图（V5.0.0之后）
-keep class com.amap.api.maps**{*;}
-keep class com.amap.api.trace**{*;}
-keep class com.autonavi**{*;}
#高德导航
-keep class com.amap.api.navi**{*;}
#-keep class com.autonavi**{*;}
#内置语音（V5.6.0之后）
-keep class com.google**{*;}
-keep class com.alibaba.idst.nls**{*;}
-keep class com.nlspeech.nlscodec**{*;}
#高德定位
-keep class com.amap.api.location**{*;}
-keep class com.amap.api.fence**{*;}
-keep class com.autonavi.aps.amapapi.model**{*;}
#高德搜索
-keep class com.amap.api.services**{*;}
#Volley混淆
-keep class com.android.volley**{*;}
-keep class com.android.volley.toolbox**{*;}
-keep class com.android.volley.Response$*{*;}
-keep class com.android.volley.Request$*{*;}
-keep class com.android.volley.RequestQueue$*{*;}
-keep class com.android.volley.toolbox.HurlStack$*{*;}
-keep class com.android.volley.toolbox.ImageLoader$*{*;}