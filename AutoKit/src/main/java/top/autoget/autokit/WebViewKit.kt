package top.autoget.autokit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import top.autoget.autokit.AKit.app
import top.autoget.autokit.VersionKit.aboveHoneycomb
import top.autoget.autokit.VersionKit.aboveKitKat

object WebViewKit {
    fun init(context: Context, webView: WebView): WebView = webView.apply {
        settings.apply {
            if (aboveKitKat) cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK//加载缓存否则网络
            loadsImagesAutomatically = aboveKitKat
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN//自适应屏幕
            useWideViewPort = true//扩大比例缩放
            loadWithOverviewMode = true//自适应
            setSupportZoom(true)
            builtInZoomControls = true//默认false
            displayZoomControls = false
            javaScriptEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            databaseEnabled = true
            domStorageEnabled = true//本地DOM存储任意读取默认false
            savePassword = true
            setAppCacheEnabled(true)
            pluginState = WebSettings.PluginState.ON
            mediaPlaybackRequiresUserGesture = true//手势播放Media默认true
            allowContentAccess = true
            allowFileAccessFromFileURLs = true
            if (aboveKitKat) mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        isSaveEnabled = true
        keepScreenOn = true
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(webView: WebView, url: String) {
                super.onPageFinished(webView, url)
                settings.apply { if (!loadsImagesAutomatically) loadsImagesAutomatically = true }
            }

            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean = when {
                url.startsWith("http:") || url.startsWith("https:") ->
                    false.apply { webView.loadUrl(url) }//处理网页
                else -> true.apply {
                    context.startActivity(Intent().apply {
                        action = Intent.ACTION_VIEW
                        data = Uri.parse(url)
                    })
                }//处理电话、邮件等
            }
        }//WebView打开链接，否则浏览器打开
        setDownloadListener { paramAnonymousString1, _, _, _, _ ->
            context.startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(paramAnonymousString1)
            })
        }//处理下载
        when {
            aboveHoneycomb -> setLayerType(View.LAYER_TYPE_SOFTWARE, null)//软解
            else -> setLayerType(View.LAYER_TYPE_HARDWARE, null)//硬解
        }
    }

    val userAgent: String
        get() = "${WebView(app).settings.userAgentString}__${System.getProperty("http.agent")}"//浏览器指纹

    fun loadData(webView: WebView, content: String) =
        webView.loadDataWithBaseURL(null, content, "text/html", "UTF-8", null)

    fun goBack(webView: WebView): Boolean =
        webView.run { if (canGoBack()) true.apply { goBack() } else false }
}