# ***库文档***

| 序号 | 分层                                   | 功能（四库共50490行）                                               |
|:-----|:---------------------------------------|:------------------------------------------------------------------|
| 01   | 01. 服务器数据层                        | 数据库                                                            |
| 02   | 02. 服务器实体层entity                  | 数据库实体类data类型                                               |
| 03   | 03. 服务器持久层repository              | 数据库修改类interface类型                                          |
| 04   | 04. 服务器业务层service                 | 业务逻辑实现class类型                                              |
| 05   | 05. 服务器控制层controller              | 业务开关控制class类型                                              |
| 06   | 06. 服务器表现层：客户端实体层Model      | 对应数据实体data类型                                               |
| 07   | 07. 服务器表现层：客户端控制层Controller | 对应业务逻辑object类型(工具库AutoKit有24686行、支付库AutoPay有633行) |
| 08   | 08. 服务器表现层：客户端视图层View       | 布局控件逻辑activity类型                                           |
| 09   | 09. 服务器表现层：客户端布局层View       | 布局控件位置xml类型(控件库AutoSee有17210行、地图库AutoMap有7961行)   |

[![](https://img.shields.io/badge/CopyRight-%E7%8E%8B%E6%95%99%E6%88%90-brightgreen.svg)](https://github.com/wangjiaocheng/AutoSet/tree/master/autopay/src/main/java/top/autoget/autopay)
[![API](https://img.shields.io/badge/API-30%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=30)
[![](https://jitpack.io/v/wangjiaocheng/AutoSet.svg)](https://jitpack.io/#wangjiaocheng/AutoSet)

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url "https://jitpack.io" }
    }
}//settings.gradle
dependencies {
    implementation "com.github.wangjiaocheng.AutoSet:AutoPay:1.0.0"//不可用则直接Import Module，依赖AutoKit
}//build.gradle
```

## **支付库AutoPay**

| 序号 | 类库(633)                    | 功能                                                |
|:-----|:-----------------------------|:---------------------------------------------------|
| 001  | *001.PayKit(592)*            | 支付AutoKit(ThreadKit)——AutoPay(PayWxEntryActivity) |
| 002  | *002.PayWxEntryActivity(41)* | 微信AutoKit(LoggerKit)                              |

>- [AndroidManifest.xml](../../../../AndroidManifest.xml)
>- [strings.xml](../../../../res/values/strings.xml)

### *001.支付PayKit(592)*

| 序号 | 方法                                 | 功能                                                     |
|:-----|:-------------------------------------|:--------------------------------------------------------|
| 01   | 01. HttpType                         | 网络方法类型：Get、Post                                  |
| 02   | 02. NetworkClientType                | 网络连接类型：HttpUrlConnection、Volley、OkHttp、Retrofit |
| 03   | 03. PayWay                           | 支付渠道类型：WeChatPay、ALiPay、UPPay                    |
| 04   | 04. PayParams                        | 支付参数                                                 |
| 05   | 05. NetworkClientInter               | 网络连接接口                                             |
| 06   | 06. OnPayInfoRequestListener         | 支付信息请求监听                                         |
| 07   | 07. OnPayResultListener              | 支付结果监听                                             |
| 08   | 08. execute                          | 线程池执行                                               |
| 09   | 09. shutdown                         | 线程池关闭                                               |
| 10   | 10. HttpUrlConnectionClient          | 网络连接实现支付get和post                                 |
| 11   | 11. VolleyClient                     | 网络连接实现支付get和post                                 |
| 12   | 12. OkHttpClientImpl                 | 网络连接实现支付get和post                                 |
| 13   | 13. PrePayInfoService                | 预支付信息服务                                           |
| 14   | 14. RetrofitClient                   | 网络连接实现支付get和post                                 |
| 15   | 15. newClient                        | 创建网络连接                                             |
| 16   | 16. PayStrategyInter                 | 支付策略接口                                             |
| 17   | 17. PayContext                       | 支付上下文                                               |
| 18   | 18. PayStrategyBase                  | 支付策略基类                                             |
| 19   | 19. PrePayInfo                       | 预支付信息                                               |
| 20   | 20. WeChatPayStrategy                | 微信支付策略                                             |
| 21   | 21. ALiPayResult                     | 阿里支付结果                                             |
| 22   | 22. ALiPayStrategy                   | 阿里支付策略                                             |
| 23   | 23. UPPayStrategy                    | 银联支付策略                                             |
| 24   | #### AutoPay                         | 自动支付                                                 |
| 25   | 01. weChatAppID                      | 微信支付ID                                               |
| 26   | 02. requestPayInfo                   | 请求支付信息                                             |
| 27   | 03. toPay                            | 执行支付                                                 |
| 28   | 04. COMMON_OK_PAY                    | 支付正常                                                 |
| 29   | 05. COMMON_ERR_PAY                   | 支付错误                                                 |
| 30   | 06. COMMON_ERR_USER_CANCELED         | 用户取消错误                                             |
| 31   | 07. COMMON_ERR_NETWORK_NOT_AVAILABLE | 网络不可用错误                                           |
| 32   | 08. COMMON_ERR_REQUEST_TIME_OUT      | 请求超时错误                                             |
| 33   | 09. WECHAT_ERR_SENT_FAILED           | 微信发送失败错误                                         |
| 34   | 10. WECHAT_ERR_AUTH_DENIED           | 微信作者否认错误                                         |
| 35   | 11. WECHAT_ERR_UNSUPPORT             | 微信不支持错误                                           |
| 36   | 12. WECHAT_ERR_BAN                   | 微信禁止错误                                             |
| 37   | 13. WECHAT_ERR_NOT_INSTALLED         | 微信未安装错误                                           |
| 38   | 14. ALI_PAY_ERR_WAIT_CONFIRM         | 支付宝等待确认错误                                        |
| 39   | 15. ALI_PAY_ERR_NET                  | 支付宝网络错误                                           |
| 40   | 16. ALI_PAY_ERR_UNKNOWN              | 支付宝未知错误                                           |
| 41   | 17. ALI_PAY_ERR_OTHER                | 支付宝其他错误                                           |
| 42   | 18. UPPAY_PLUGIN_NOT_INSTALLED       | 插件未安装                                               |
| 43   | 19. UPPAY_PLUGIN_NEED_UPGRADE        | 插件需更新                                               |
| 44   | 20. newInstance                      | 创建支付实例                                             |

>- data.bin

```kotlin
PayKit.AutoPay.newInstance(
    PayKit.PayParams(
        this@CenterActivity, channel, price,//分
        "效享-充值", "充值${price}分",
        "http://xxx.xxx.xxx.xxx:8080/pay/unifiedOrder",//APP服务器host主机地址
        "appid",//仅微信支付需
        PayKit.HttpType.Post, PayKit.NetworkClientType.OkHttp
    )
)?.requestPayInfo(object : PayKit.OnPayInfoRequestListener {
    override fun onPayInfoRequestStart() {}//做loading操作，如progressBar.show()
    override fun onPayInfoRequestSuccess() {}//去loading状态，请求预支付信息成功，跳转到客户端支付
    override fun onPayInfoRequestFailure() {}//去loading状态，请求预支付信息失败，得到支付失败回调
})?.toPay(object : PayKit.OnPayResultListener {
    override fun onPaySuccess(payWay: PayKit.PayWay?) {}
    override fun onPayCancel(payWay: PayKit.PayWay?) {}
    override fun onPayFailure(payWay: PayKit.PayWay?, errCode: Int) {}
})
```

### *002.微信PayWxEntryActivity(41)*

>1. [Oracle](https://www.oracle.com/cn/index.html)
>2. [Android](https://developer.android.google.cn/index.html)
>3. [JetBrains](https://www.jetbrains.com/)
>4. [MySQL](https://www.mysql.com/)
>5. [Git](https://git-scm.com/)
>6. [Gradle](https://gradle.org/)
>7. [Axure](https://www.axure.com/)
>8. [XMind](https://www.xmind.cn/)
>9. [GitHub](https://github.com/)
>10. [Gitee](https://gitee.com/)
>11. [阿里云](https://www.aliyun.com/)
>12. [腾讯云](https://www.qcloud.com/)
>13. [百度云](https://cloud.baidu.com/)
>14. [高德地图](http://lbs.amap.com/)
>15. [腾讯地图](http://lbs.qq.com/index.html)
>16. [百度地图](http://lbsyun.baidu.com/)
>17. [阿里支付](https://open.alipay.com/platform/home.htm)
>18. [腾讯支付](https://open.weixin.qq.com/)
>19. [银联支付](https://open.unionpay.com/tjweb/index)
>20. [讯飞语音](https://www.xfyun.cn/)
>21. [爪哇示例](http://www.javased.com/)
>22. [在线工具](http://tool.lu/)
>23. [JitPack](https://jitpack.io/)
>24. [ProcessOn](https://www.processon.com/)
>25. [墨刀](https://modao.cc/)
>26. [Kotlin](https://www.kotlincn.net/)
>27. [安卓教程](http://hukai.me/android-training-course-in-chinese/index.html)
>28. [Spring](https://spring.io/)
>29. [菜鸟教程](http://www.runoob.com/)
>30. [黑马教程](http://yun.itheima.com/)
>31. [下载JavaSE](https://www.oracle.com/java/technologies/javase-downloads.html)
>32. [下载AndroidStudio](https://developer.android.google.cn/studio#downloads)
>33. [东软开源镜像](https://mirrors.neusoft.edu.cn/)
>34. [下载IDEA](https://www.jetbrains.com/idea/download/#section=windows)
>35. [下载MySQL](https://dev.mysql.com/downloads/installer/)
>36. [下载Git](https://npm.taobao.org/mirrors/git-for-windows/)
>37. [下载Gradle](https://gradle.org/releases/)
>38. [下载Axure](https://axure.cachefly.net/AxureRP-Setup.exe)
>39. [下载Axure汉化包](https://www.pmyes.com/thread-35068.htm)
>40. [下载XMind](https://www.xmind.cn/download/)

