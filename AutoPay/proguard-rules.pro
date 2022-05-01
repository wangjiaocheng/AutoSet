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
#微信支付
-keep class com.tencent.wxop**{*;}
-keep class com.tencent.mm.sdk**{*;}
-keep class com.tencent.mm.opensdk**{*;}
#阿里支付
-keep class com.ut.device**{*;}
-keep class com.ta.utdid2**{*;}
-keep class org.json.alipay**{*;}
-keep class com.alipay.mobile.framework.service.annotation**{*;}
-keep class com.alipay.mobilesecuritysdk.face**{*;}
-keep class com.alipay.apmobilesecuritysdk**{*;}
-keep class com.alipay.android.phone.mrpc.core**{*;}
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub*{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub*{*;}
-keep class com.alipay.tscenter**{*;}
-keep class com.alipay.tscenter.biz.rpc**{*;}
-keep class com.alipay.sdk.app.AuthTask{public *;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.H5PayCallback{<fields>;<methods>;}
#JavaBean混淆
-keep class top.autoget.autopay.PayKit$PrePayInfo{*;}
#GSON混淆
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes EnclosingMethod
# Gson specific classes
-keep class sun.misc.Unsafe*{*;}
-keep class com.google.gson.stream**{*;}
#Volley混淆
-keep class com.android.volley**{*;}
-keep class com.android.volley.toolbox**{*;}
-keep class com.android.volley.Response$*{*;}
-keep class com.android.volley.Request$*{*;}
-keep class com.android.volley.RequestQueue$*{*;}
-keep class com.android.volley.toolbox.HurlStack$*{*;}
-keep class com.android.volley.toolbox.ImageLoader$*{*;}
#OkHttp3混淆
-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3**{*;}
-keep interface okhttp3**{*;}
-dontwarn okhttp3.**
-dontwarn okio.**
#Retrofit2混淆
-dontwarn retrofit2.**
-keep class retrofit2**{*;}
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {@retrofit2.http.* <methods>;}