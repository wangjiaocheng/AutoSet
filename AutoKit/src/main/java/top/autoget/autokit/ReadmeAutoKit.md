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

[![](https://img.shields.io/badge/CopyRight-%E7%8E%8B%E6%95%99%E6%88%90-brightgreen.svg)](https://github.com/wangjiaocheng/AutoSet/tree/master/autokit/src/main/java/top/autoget/autokit)
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
    implementation "com.github.wangjiaocheng.AutoSet:AutoKit:1.0.0"//不可用则直接Import Module，依赖无
}//build.gradle
```

## **工具库AutoKit**

| 序号 | 类库                                                               | 类别       |
|:-----|:------------------------------------------------------------------|:-----------|
| 001  | *001.设备、存储、网络、无线、热点、蓝牙、定位、亮度、闪光、振动、蜂鸣* | 硬件(2357) |
| 002  | *002.系统、版本、机型、重启、命令、破解、应用、崩溃、退出、清理、快捷* | 系统(1574) |
| 003  | *003.路由、活动、分享、片段、服务、管理、轮询、广播、意图、元数、权限* | 组件(2624) |
| 004  | *004.屏幕、窗口、多栏、视图、网视、着色、吐司、零食、连点、防抖、抖动* | 视图(2518) |
| 005  | *005.全局、接口、总线、内存、磁盘、双重、线程、进程、反射、单例、空判* | 管理(2012) |
| 006  | *006.输入、剪贴、字串、富文、文本、随机、验证、身份、银行、图码、软包* | 字符(4268) |
| 007  | *007.编码、加密、位算、压缩、平面、计算、尺寸、坐标、转换、迁移、数库* | 运算(1792) |
| 008  | *008.文件、存取、压制、密压、打开、图像、照片、图片、动画、信息、属性* | 文件(3795) |
| 009  | *009.记录、日志、数据、解析、处理、标记、路径、资源、共享、主题、消息* | 资源(2232) |
| 010  | *010.日期、农历、关闭、处理、通讯、地图、网服、网连、标识、安全、超文* | 连接(1514) |

| 序号 | 类库(24686)                                          | 功能                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|:-----|:----------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 001  | *001.DeviceKit(446)*                                | 设备AutoKit(VersionKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| 002  | *002.SdKit(188)*                                    | 存储AutoKit(CleanKit-FileKit-PathKit-PhotoKit-ScreenKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| 003  | *003.NetKit(585)*                                   | 网络AutoKit(ApKit-BroadcastKit-WifiKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| 004  | *004.WifiKit(268)*                                  | 无线AutoKit(ApKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 005  | *005.ApKit(182)*                                    | 热点                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 006  | *006.BluetoothKit(240)*                             | 蓝牙AutoKit(AKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| 007  | *007.LocationKit(283)*                              | 定位                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 008  | *008.BrightnessKit(48)*                             | 亮度                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 009  | *009.FlashlightKit(58)*                             | 闪光                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 010  | *010.VibrateKit(20)*                                | 振动AutoKit(BeepKit-ClickKit-ShakeKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 011  | *011.BeepKit(39)*                                   | 蜂鸣                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 012  | *012.SystemKit(231)：SystemLanguage*                | 系统AutoKit(LogKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 013  | *013.VersionKit(68)*                                | 版本AutoKit(ActivityKit-ApplicationKit-BarKit-DeviceKit-EmptyKit-EncodeKit-FileKit-FragmentKit-ImageKit-IntentKit-LogKit-NetKit-PathKit-PermissionKit-ProcessKit-RomKit-ScreenKit-SdKit-SystemKit-ToastKit-UriKit-ViewKit-WebViewKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| 014  | *014.RomKit(215)*                                   | 机型AutoKit(IntentKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 015  | *015.RebootKit(35)*                                 | 重启                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 016  | *016.ShellKit(86)*                                  | 命令AutoKit(ApplicationKit-NetKit-RebootKit-RootKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 017  | *017.RootKit(43)*                                   | 破解AutoKit(ApplicationKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 018  | *018.ApplicationKit(604)*                           | 应用AutoKit(ActivityKit-AKit-CrashKit-FileKit-IntentKit-LogKit-MetaDataKit-PermissionKit-ProcessKit-ResourceKit-ToastKit-UiMessageKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 019  | *019.CrashKit(143)*                                 | 崩溃                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 020  | *020.ExitKit(59)*                                   | 退出                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 021  | *021.CleanKit(50)*                                  | 清理                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 022  | *022.ShortcutKit(40)*                               | 快捷                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 023  | *023.RouteKit(142)*                                 | 路由                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 024  | *024.ActivityKit(669)*                              | 活动AutoKit(CrashKit-ExitKit-ShareKit-SystemKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| 025  | *025.ShareKit(238)*                                 | 分享                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 026  | *026.FragmentKit(485)*                              | 片段                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 027  | *027.ServiceKit(66)*                                | 服务                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 028  | *028.ManagerKit(159)*                               | 管理                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 029  | *029.PollingKit(38)*                                | 轮询                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 030  | *030.BroadcastKit(22)*                              | 广播                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 031  | *031.IntentKit(316)*                                | 意图AutoKit(ActivityKit-ContactsKit-PermissionKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 032  | *032.MetaDataKit(53)*                               | 元数                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 033  | *033.PermissionKit(436)*                            | 权限                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 034  | *034.ScreenKit(260)*                                | 屏幕AutoKit(BarKit-InputMethodKit)——AutoSee(flow_layout_LayoutScroll-PopupSingleShineView)——AutoMap(MapCloudDetailActivity)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 035  | *035.WindowKit(60)*                                 | 窗口                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 036  | *036.BarKit(531)*                                   | 多栏                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 037  | *037.ViewKit(946)：ViewTouch、ViewClick、ViewShadow* | 视图                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 038  | *038.WebViewKit(76)*                                | 网视                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 039  | *039.ColorKit(76)*                                  | 着色                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 040  | *040.ToastKit(360)*                                 | 吐司(TODO)AutoKit(ApKit-BluetoothKit-ClickKit-ContactsKit-CrashKit-ExitKit-LocationKit-OpenKit-ShareKit-WifiKit)——AutoSee(SeatAirplane-SeatMovie)——AutoMap(MapActivity-MapErrorToast-MapNaviActivity-MapNaviDriveActivity-MapNaviRideActivity-MapNaviWalkActivity-MapOfflineChild-MapRouteActivity-MapRouteCalculateActivity-TtsControllerAMap)                                                                                                                                                                                                                                                                                                                                                               |
| 041  | *041.SnackKit(107)*                                 | 零食                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 042  | *042.ClickKit(55)*                                  | 连点                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 043  | *043.AntiShakeKit(21)*                              | 防抖                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 044  | *044.ShakeKit(26)*                                  | 抖动                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 045  | *045.AKit(276)*                                     | 全局AutoKit(ActivityKit-ApKit-ApplicationKit-BarKit-BluetoothKit-BrightnessKit-CacheDiskKit-CleanKit-ClipboardKit-ColorKit-ContactsKit-CrashKit-DeviceKit-ExitKit-FileKit-FileIoKit-FlashlightKit-ImageKit-InputMethodKit-IntentKit-LocationKit-MapKit-MetaDataKit-NetKit-OpenKit-PathKit-PermissionKit-PhotoKit-PreferenceKit-ProcessKit-RebootKit-ResourceKit-RootKit-ScreenKit-SdKit-ServiceKit-SpanKit-SystemKit-ThemeKit-ToastKit-UriKit-VibrateKit-WebViewKit-WifiKit-WindowKit)                                                                                                                                                                                                                        |
| 046  | *046.ApiKit(44)*                                    | 接口                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 047  | *047.BusKit(239)*                                   | 总线                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 048  | *048.CacheMemoryKit(79)*                            | 内存AutoKit(CacheDoubleKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 049  | *049.CacheDiskKit(524)*                             | 磁盘AutoKit(CacheDoubleKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 050  | *050.CacheDoubleKit(246)*                           | 双重                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 051  | *051.ThreadKit(328)*                                | 线程AutoKit(ApKit-BluetoothKit-BusKit-ConnectionKit-CrashKit-LogKit-WebServiceKit-WifiKit)——AutoPay(PayKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| 052  | *052.ProcessKit(151)*                               | 进程                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 053  | *053.ReflectionKit(64)*                             | 反射                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 054  | *054.SingletonKit.Singleton(16)*                    | 单例                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 055  | *055.EmptyKit(72)*                                  | 空判                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 056  | *056.InputMethodKit(248)*                           | 输入AutoSee(TextAutoZoom-Title)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| 057  | *057.ClipboardKit(20)*                              | 剪贴                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 058  | *058.StringKit(120)*                                | 字串AutoKit(ApplicationKit-BluetoothKit-CacheDiskKit-ContactsKit-ConvertKit-CrashKit-DataKit-DeviceKit-EncryptKit-FileKit-FileIoKit-ImageKit-LogKit-OpenKit-PermissionKit-PictureKit-PollingKit-RandomKit-ResourceKit-RomKit-SystemKit-TextKit-ValidationKit-WifiKit-ZipKit-ZipPlusKit)——AutoSee(notice_NoticeBase-RulerWheel-ShoppingView-Title)——AutoMap(MapActivity-MapRouteCalculateActivity-MapRouteSearchActivity-MapTipListAdapter)                                                                                                                                                                                                                                                                    |
| 059  | *059.SpanKit(795)*                                  | 富文                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 060  | *060.TextKit(444)*                                  | 文本                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 061  | *061.RandomKit(78)*                                 | 随机                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 062  | *062.ValidationKit(280)*                            | 验证AutoKit(IdKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 063  | *063.IdKit(396)*                                    | 身份                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 064  | *064.BankKit(1720)*                                 | 银行                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 065  | *065.BarQRKit(149)*                                 | 图码                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 066  | *066.PackageKit(18)*                                | 软包AutoKit(MapKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 067  | *067.EncodeKit(115)*                                | 编码AutoKit(EncryptKit-FileKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| 068  | *068.EncryptKit(510)*                               | 加密                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 069  | *069.BitKit(35)*                                    | 位算                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 070  | *070.CompressKit(92)*                               | 压缩                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 071  | *071.PlaneKit(15)*                                  | 平面                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 072  | *072.CalculateKit(222)*                             | 计算                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 073  | *073.DensityKit(58)*                                | 尺寸AutoKit(WebKit)——AutoSee(card_CardStackView-Cobweb-PopupSingle-ProgressView-SeatAirplane-SeatMovie-Seek-ShoppingView-Side-Title)——AutoMap(MapActivity-MapIndoorFloorSwitchView)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 074  | *074.CoordinateKit(94)*                             | 坐标AutoKit(MapKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 075  | *075.ConvertKit(520)*                               | 转换AutoKit(ApplicationKit-CacheDiskKit-CleanKit-DateKit-EncryptKit-FileKit-ImageKit-LunarKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| 076  | *076.MigrationKit(96)*                              | 迁移                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 077  | *077.DbKit(35)*                                     | 数库                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 078  | *078.FileKit(914)*                                  | 文件AutoKit(ApplicationKit-CacheDiskKit-CleanKit-CrashKit-ExifKit-FileIoKit-ImageKit-IntentKit-LogKit-OpenKit-PictureKit-ResourceKit-RootKit-SdKit-ShareKit-ZipKit-ZipPlusKit)——AutoMap(MapActivity)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 079  | *079.FileIoKit(366)*                                | 存取AutoKit(CacheDiskKit-CrashKit-FileKit-LogKit-ResourceKit)——AutoMap(MapActivity)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| 080  | *080.ZipKit(336)*                                   | 压制                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 081  | *081.ZipPlusKit(320)*                               | 密压                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 082  | *082.OpenKit(102)*                                  | 打开                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 083  | *083.ImageKit(1298)*                                | 图像AutoKit(BarQRKit-PictureKit-ThemeKit)——AutoSee(notice_NoticeBigPic-Cobweb-Seek-Wave)——AutoMap(MapActivity)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| 084  | *084.PhotoKit(126)*                                 | 照片                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 085  | *085.PictureKit(43)*                                | 图片                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 086  | *086.AnimationKit(209)*                             | 动画AutoSee(PopupViewManager)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| 087  | *087.ExifKit(41)*                                   | 信息                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 088  | *088.PropertiesKit(40)*                             | 属性                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 089  | *089.LoggerKit(84)*                                 | 记录AutoKit(ActivityKit-AnimationKit-ApiKit-ApplicationKit-BitKit-BusKit-CacheDiskKit-CrashKit-DateKit-DbKit-DeviceKit-ExifKit-ExitKit-FileKit-FlashlightKit-FragmentKit-IdKit-ImageKit-InputMethodKit-IntentKit-JsonKit-LocationKit-LogKit-MigrationKit-NetKit-PermissionKit-PhotoKit-ProcessKit-PropertiesKit-ScreenKit-SpanKit-SystemKit-TextKit-ThreadKit-ToastKit-UiMessageKit-UriKit-ValidationKit-XmlParseKit-ZipKit-ZipPlusKit)——AutoSee(card_CardStackView-scale_ScaleImageView-SeatMovie-ShineView-SwipeCaptcha)——AutoMap(MapActivity-MapErrorToast-MapNaviActivity-MapOfflineChild-MapOfflineDownloadedAdapter-MapRouteCalculateActivity-MapRouteDriveDetailActivity)——AutoPay(PayWxEntryActivity) |
| 090  | *090.LogKit(681)*                                   | 日志AutoKit(SpanKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 091  | *091.DataKit(146)*                                  | 数据AutoKit(LocationKit-StringKit)——AutoSee(NetSpeedView-ProgressRound)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| 092  | *092.JsonKit(273)*                                  | 解析                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 093  | *093.GsonKit(50)*                                   | 处理                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 094  | *094.XmlParseKit(138)*                              | 标记                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 095  | *095.PathKit(177)*                                  | 路径AutoKit(CleanKit-ContactsKit-DbKit-FileKit-SdKit-UriKit)——AutoMap(MapActivity)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| 096  | *096.ResourceKit(251)*                              | 资源                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 097  | *097.PreferenceKit(109)*                            | 共享AutoKit(SystemKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 098  | *098.ThemeKit(156)*                                 | 主题                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 099  | *099.UiMessageKit(167)*                             | 消息AutoKit(HandleKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 100  | *100.DateKit(514)*                                  | 日期AutoKit(AntiShakeKit-ApplicationKit-CacheDiskKit-CacheMemoryKit-ClickKit-ConvertKit-CrashKit-IdKit-LogKit-PhotoKit-ProcessKit-ViewKit)——AutoSee(banner_Banner-notice_NoticeBase-scale_ScaleImageView-SeatMovie)——AutoMap(MapActivity-MapNaviActivity-MapOfflineDownloadedAdapter)                                                                                                                                                                                                                                                                                                                                                                                                                         |
| 101  | *101.LunarKit(177)*                                 | 农历                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 102  | *102.CloseKit(25)*                                  | 关闭                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 103  | *103.HandleKit(121)*                                | 处理AutoKit(ApKit-BusKit-ToastKit-UiMessageKit)——AutoSee(recycl_diff_DifferAsync-HeartLayout-NetSpeedView-SeatMovie)——AutoMap(MapActivity-MapErrorToast-MapOfflineChild)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| 104  | *104.ContactsKit(221)*                              | 通讯                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 105  | *105.MapKit(69)*                                    | 地图                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 106  | *106.WebServiceKit(66)*                             | 网服                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 107  | *107.ConnectionKit(114)*                            | 网连                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 108  | *108.UriKit(91)*                                    | 标识AutoKit(ShareKit)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| 109  | *109.SslKit(57)*                                    | 安全                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| 110  | *110.HtmlKit(59)*                                   | 超文                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |

>- [AndroidManifest.xml](../../../../AndroidManifest.xml)
>- [strings.xml](../../../../res/values/strings.xml)

### *001.设备DeviceKit(446)*

| 序号 | 方法                    | 功能                                                               |
|:-----|:------------------------|:------------------------------------------------------------------|
| 01   | 01. isAdbEnabled        | Adb是否启用                                                        |
| 02   | 02. numberCpuSerial     | CPU序列号                                                         |
| 03   | 03. statePhone          | 手机状态：android.permission.READ_PRIVILEGED_PHONE_STATE           |
| 04   | 04. imsi                | 唯一用户ID：android.permission.READ_PRIVILEGED_PHONE_STATE         |
| 05   | 05. carrierDevice       | imsi方式获取运营商                                                 |
| 06   | 06. androidId           | 安卓ID                                                            |
| 07   | 07. deviceId            | 唯一设备ID：android.permission.READ_PRIVILEGED_PHONE_STATE         |
| 08   | 08. imei                | IMEI：android.permission.READ_PRIVILEGED_PHONE_STATE              |
| 09   | 09. getImei             | 根据slotId获取IMEI：android.permission.READ_PRIVILEGED_PHONE_STATE |
| 10   | 10. meid                | MEID：android.permission.READ_PRIVILEGED_PHONE_STATE              |
| 11   | 11. getMeid             | 根据slotId获取MEID：android.permission.READ_PRIVILEGED_PHONE_STATE |
| 12   | 12. deviceSoftVersion   | 设备软件版本号                                                     |
| 13   | 13. stateSim            | sim卡状态                                                         |
| 14   | 14. isSimReady          | sim卡是否准备好                                                    |
| 15   | 15. simCountryIso       | sim卡国家代码                                                      |
| 16   | 16. simCountry          | sim国家                                                           |
| 17   | 17. getCountryCode      | 获取国家代码                                                       |
| 18   | 18. simOperator         | 移动国家码网络码                                                   |
| 19   | 19. simCarrier          | 网络码方式获取运营商                                               |
| 20   | 20. simOperatorName     | 运营商名称                                                         |
| 21   | 21. simType             | sim卡类型                                                         |
| 22   | 22. isPhone             | 是否手机                                                           |
| 23   | 23. isTablet            | 是否平板                                                           |
| 24   | 24. numberSimSerial     | sim卡序列号：android.permission.READ_PRIVILEGED_PHONE_STATE        |
| 25   | 25. networkCountryIso   | 网络国家代码                                                       |
| 26   | 26. networkOperator     | 网络运营商                                                         |
| 27   | 27. networkOperatorName | 网络运营商名称                                                     |
| 28   | 28. networkType         | 网络类型                                                           |
| 29   | 29. numberPhone         | 手机号码                                                           |
| 30   | 30. numberVoiceMail     | 语音信箱号码                                                       |
| 31   | 31. stateCall           | 手机状态：0无活动；1响铃；2待机。                                   |
| 32   | 32. stateLocation       | 手机方位                                                           |

### *002.存储SdKit(188)*

| 序号 | 方法                    | 功能         |
|:-----|:------------------------|:------------|
| 01   | 01. isSdCardEnable      | Sd是否启用  |
| 02   | 02. isSdCardDisable     | Sd是否禁用  |
| 03   | 03. isSdCardAvailable   | Sd是否可用  |
| 04   | 04. isSdCardUnavailable | Sd是否停用  |
| 05   | 05. sdCardPathEx        | Sd路径列表  |
| 06   | 06. sdCardPath          | Sd路径      |
| 07   | 07. getAllBytes         | 获取路径容量 |
| 08   | 08. getAvailableBytes   | 获取可用容量 |
| 09   | 09. getFreeBytes        | 获取剩余容量 |
| 10   | 10. getStatFs           | 获取StatFs  |
| 11   | 11. sdCardInfo          | Sd信息      |
| 12   | 12. sdCardInfoList      | Sd信息列表  |

### *003.网络NetKit(585)*

| 序号 | 方法                          | 功能                                                                                   |
|:-----|:------------------------------|:--------------------------------------------------------------------------------------|
| 01   | 01. openSettingsWireless      | 打开网络设置界面                                                                       |
| 02   | 02. settingsWireless          | 网络设置界面                                                                           |
| 03   | 03. currentNetworkState       | 当前网络状态                                                                           |
| 04   | 04. isConnectingByState       | 是否网络连接中                                                                         |
| 05   | 05. isConnectedByState        | 是否网络已连接                                                                         |
| 06   | 06. isSuspendedByState        | 是否网络已暂停                                                                         |
| 07   | 07. isDisconnectingByState    | 是否网络连接正取消                                                                     |
| 08   | 08. isDisconnectedByState     | 是否网络连接已取消                                                                     |
| 09   | 09. isUnknownByState          | 是否网络未知状态                                                                       |
| 10   | 10. currentNetworkType        | 当前网络类型                                                                           |
| 11   | 11. isWifiByType              | 是否wifi网络                                                                           |
| 12   | 12. isMobileByType            | 是否移动网络                                                                           |
| 13   | 13. isMobileMmsByType         | 是否移动网络mms                                                                        |
| 14   | 14. isMobileSuplByType        | 是否移动网络supl                                                                       |
| 15   | 15. isMobileDunByType         | 是否移动网络dun                                                                        |
| 16   | 16. isMobileHipriByType       | 是否移动网络hipri                                                                      |
| 17   | 17. isWimaxByType             | 是否wimax网络                                                                          |
| 18   | 18. isBluetoothByType         | 是否蓝牙网络                                                                           |
| 19   | 19. isDummyByType             | 是否dummy网络                                                                          |
| 20   | 20. isEthernetByType          | 是否以太网络                                                                           |
| 21   | 21. currentNetworkSubtype     | 当前网络子类型                                                                         |
| 22   | 22. isEDGEBySubtype           | 是否网络子类型edge                                                                     |
| 23   | 23. isGPRSBySubtype           | 是否网络子类型gprs                                                                     |
| 24   | 24. isCDMABySubtype           | 是否网络子类型cdma                                                                     |
| 25   | 25. is1XRTTBySubtype          | 是否网络子类型1xrtt                                                                    |
| 26   | 26. isIDENBySubtype           | 是否网络子类型iden                                                                     |
| 27   | 27. isEVDO_ABySubtype         | 是否网络子类型evdo_a                                                                   |
| 28   | 28. isUMTSBySubtype           | 是否网络子类型umts                                                                     |
| 29   | 29. isEVDO_0BySubtype         | 是否网络子类型evdo_0                                                                   |
| 30   | 30. isHSDPABySubtype          | 是否网络子类型hsdpa                                                                    |
| 31   | 31. isHSUPABySubtype          | 是否网络子类型hsupa                                                                    |
| 32   | 32. isHSPABySubtype           | 是否网络子类型hspa                                                                     |
| 33   | 33. isEVDO_BBySubtype         | 是否网络子类型evdo_b                                                                   |
| 34   | 34. isEHRPDBySubtype          | 是否网络子类型ehrpd                                                                    |
| 35   | 35. isHSPAPBySubtype          | 是否网络子类型hspap                                                                    |
| 36   | 36. isLTEBySubtype            | 是否网络子类型lte                                                                      |
| 37   | 37. isUNKNOWNBySubtype        | 是否网络子类型未知                                                                     |
| 38   | 38. isChinaMobile2G           | 是否移动2G                                                                             |
| 39   | 39. isChinaUnicom2G           | 是否联通2G                                                                             |
| 40   | 40. isChinaTelecom2G          | 是否电信2G                                                                             |
| 41   | 41. isChinaUnicom3G           | 是否联通3G                                                                             |
| 42   | 42. isChinaTelecom3G          | 是否电信3G                                                                             |
| 43   | 43. NetworkType               | 网络类型枚举                                                                           |
| 44   | 44. netWorkType               | 网络类型                                                                               |
| 45   | 45. netWorkTypeName           | 网络类型名称                                                                           |
| 46   | 46. networkState              | 网络状态                                                                               |
| 47   | 47. isConnectedWifiOrMobile   | 是否已连接wifi或移动数据网络                                                            |
| 48   | 48. isConnectedNetwork        | 是否已连接网络                                                                         |
| 49   | 49. isConnected               | 是否已连接                                                                             |
| 50   | 50. isAvailable               | 网络连接是否可用                                                                       |
| 51   | 51. isAvailableByPing         | 网络连接是否可用通过ping                                                               |
| 52   | 52. ping                      | 是否连接外网                                                                           |
| 53   | 53. wifiEnabled               | wifi是否启用                                                                           |
| 54   | 54. isWifiAvailable           | wifi是否可用                                                                           |
| 55   | 55. isWifiConnected           | wifi是否已连接                                                                         |
| 56   | 56. wifiState                 | wifi状态                                                                              |
| 57   | 57. wifiConnectionInfo        | wifi连接信息                                                                           |
| 58   | 58. wifiScanResults           | wifi扫描结果                                                                           |
| 59   | 59. getWifiScanResultsByBSSID | wifi扫描结果通过bssid                                                                  |
| 60   | 60. gateWayByWifi             | wifi网关                                                                              |
| 61   | 61. netMaskByWifi             | wifi网络掩码                                                                           |
| 62   | 62. ipAddressByWifi           | 获取ip地址通过wifi                                                                     |
| 63   | 63. serverAddressByWifi       | 获取服务器地址通过wifi                                                                 |
| 64   | 64. is4GConnected             | 是否4G网络已连接                                                                       |
| 65   | 65. isDataEnabled             | 是否数据启用                                                                           |
| 66   | 66. setDataEnabled            | 设置数据启用                                                                           |
| 67   | 67. setMobileDataEnabled      | 设置移动数据启用                                                                       |
| 68   | 68. isDataOpen                | 数据是否打开                                                                           |
| 69   | 69. isDataConnected           | 数据是否已连接                                                                         |
| 70   | 70. getAddressDomain          | 获取域名地址                                                                           |
| 71   | 71. addressProxy              | 获取代理地址                                                                           |
| 72   | 72. ipBroadcast               | 广播的ip地址                                                                           |
| 73   | 73. ipWifi                    | wifi的ip地址                                                                          |
| 74   | 74. ipGprs                    | gprs的ip地址                                                                          |
| 75   | 75. getIpGprs                 | gprs的ip地址带是否ipv4                                                                 |
| 76   | 76. addressIp                 | ip地址                                                                                |
| 77   | 77. macBluetooth              | 蓝牙mac地址：android.permission.LOCAL_MAC_ADDRESS android.permission.BLUETOOTH_CONNECT |
| 78   | 78. addressMac                | mac地址                                                                               |
| 79   | 79. getAddressMac             | 获取mac地址：android.permission.LOCAL_MAC_ADDRESS                                      |

### *004.无线WifiKit(254)*

| 序号 | 方法                           | 功能                         |
|:-----|:-------------------------------|:-----------------------------|
| 01   | 01. connectWifi                | 连接wifi                     |
| 02   | 02. checkState                 | wifi是否可用                 |
| 03   | 03. openWifi                   | 打开wifi                     |
| 04   | 04. closeWifi                  | 关闭wifi                     |
| 05   | 05. wifiConfigList             | 配置好的wifi网络             |
| 06   | 06. hiddenSSID                 | 隐藏ssid                     |
| 07   | 07. displaySSID                | 显示ssid                     |
| 08   | 08. configWifiList             | 获取已保存配置的wifi网络      |
| 09   | 09. timeOut                    | wifi默认连接超时时间          |
| 10   | 10. onWifiConnectStateListener | wifi连接状态监听器            |
| 11   | 11. wifiInfo                   | 得到WifiInfo所有信息包        |
| 12   | 12. mac                        | wifi的mac地址                |
| 13   | 13. ip                         | wifi的ip地址                 |
| 14   | 14. currentNetId               | 当前网络ID                   |
| 15   | 15. bssId                      | bssid                        |
| 16   | 16. ssId                       | ssid                         |
| 17   | 17. checkSSIDState             | 判断某个wifi网络是否连接      |
| 18   | 18. isConnectSuccess           | wifi是否连接成功             |
| 19   | 19. wifiList                   | wifi列表                     |
| 20   | 20. getSSID                    | 通过网络ID获取ssid           |
| 21   | 21. getBSSID                   | 通过网络ID获取物理地址        |
| 22   | 22. getFrequency               | 通过网络ID获取频率            |
| 23   | 23. getLevel                   | 通过网络ID获取强度            |
| 24   | 24. getCapabilities            | 通过网络ID获取功能            |
| 25   | 25. scanResultList             | 扫描结果列表                 |
| 26   | 26. lookUpScan                 | 扫描结果字符串                |
| 27   | 27. checkScanResult            | 判断能不能搜索到指定网络      |
| 28   | 28. startScan                  | 开始扫描wifi                 |
| 29   | 29. isExistSSID                | 判断某个网络是否已保存在配置中 |
| 30   | 30. connectConfigurationWifi   | 连接配置好的WiFi             |
| 31   | 31. addNetwork                 | 添加一个网络并连接            |
| 32   | 32. removeWifi                 | 删除指定ID网络               |
| 33   | 33. disconnectWifi             | 断开指定ID网络               |
| 34   | 34. connectConfiguration       | 指定配置好的网络进行连接      |
| 35   | 35. releaseWifiLock            | 解锁WifiLock                 |
| 36   | 36. acquireWifiLock            | 锁定WifiLock                 |
| 37   | 37. release                    | 释放wifi资源                 |

### *005.热点ApKit(182)*

| 序号 | 方法                              | 功能                  |
|:-----|:----------------------------------|:---------------------|
| 01   | 01. isWifiConnectSuccess          | 是否wifi连接成功      |
| 02   | 02. wifiApState                   | wifi热点状态          |
| 03   | 03. isWifiApEnable                | wifi热点是否启用      |
| 04   | 04. onWifiAPStatusChangedListener | wifi热点状态改变监听器 |
| 05   | 05. wifiAPSsid                    | wifi热点ssid          |
| 06   | 06. wifiAPPassword                | wifi热点密码          |
| 07   | 07. startWifiAp                   | 开始WLAN热点          |
| 08   | 08. closeWifiAp                   | 关闭WLAN热点          |
| 09   | 09. startAp                       | 开始wifi热点          |
| 10   | 10. closeAp                       | 关闭wifi热点          |
| 11   | 11. securityType                  | wifi安全类型          |
| 12   | 12. release                       | 释放wifi热点资源      |

### *006.蓝牙BluetoothKit(236)*

| 序号 | 方法                          | 功能                                                                                     |
|:-----|:------------------------------|:----------------------------------------------------------------------------------------|
| 01   | 01. bluetoothAdapter          | 蓝牙适配器                                                                               |
| 02   | 02. getBluetoothDevice        | 根据地址获取蓝牙设备                                                                      |
| 03   | 03. isOpenBluetooth           | 蓝牙是否打开                                                                             |
| 04   | 04. onBluetoothDeviceListener | 蓝牙设备监听器                                                                           |
| 05   | 05. openBluetooth             | 打开蓝牙：android.permission.BLUETOOTH_CONNECT                                           |
| 06   | 06. bluetoothDeviceFilter     | 蓝牙设备过滤器                                                                           |
| 07   | 07. isCorrectDevice           | 是否指定蓝牙设备                                                                          |
| 08   | 08. stopSearch                | 停止搜索蓝牙：android.permission.BLUETOOTH_SCAN                                          |
| 09   | 09. startSearch               | 开始搜索蓝牙：android.permission.BLUETOOTH_SCAN                                          |
| 10   | 10. searchDevices             | 搜索蓝牙设备带监听:android.permission.BLUETOOTH_SCAN android.permission.BLUETOOTH_CONNECT |
| 11   | 11. createBind                | 匹配蓝牙设备可带PIN码：android.permission.BLUETOOTH_SCAN                                  |
| 12   | 12. pairBtDevice              | 根据地址匹配具体蓝牙设备：android.permission.BLUETOOTH_SCAN                               |
| 13   | 13. isCorrectDevice           | 蓝牙设备是否正确                                                                          |
| 14   | 14. isBluetoothBond           | 蓝牙是否已绑定：android.permission.BLUETOOTH_SCAN                                         |
| 15   | 15. isBtAddressValid          | 蓝牙地址是否有效                                                                          |
| 16   | 16. release                   | 蓝牙资源释放：android.permission.BLUETOOTH_SCAN android.permission.BLUETOOTH_CONNECT     |

### *007.定位LocationKit(285)*

| 序号 | 方法                    | 功能                |
|:-----|:------------------------|:-------------------|
| 01   | 01. settingsGps         | GPS设置界面         |
| 02   | 02. LocationService     | 定位服务            |
| 03   | 03. KitLocationListener | 定位监听器，注意名称 |
| 04   | 04. registerLocation    | 注册定位            |
| 05   | 05. unRegisterLocation  | 注销定位            |
| 06   | 06. getAddress          | 获取地址            |
| 07   | 07. getCountryName      | 获取国家            |
| 08   | 08. getLocality         | 获取地点            |
| 09   | 09. getStreet           | 获取街道            |
| 10   | 10. isMove              | 是否移动            |
| 11   | 11. isBetterLocation    | 是否更佳定位        |
| 12   | 12. isSameProvider      | 是否相同提供者      |
| 13   | 13. isLocationEnabled   | 是否定位可用        |
| 14   | 14. isGpsEnabled        | 是否GPS可用         |
| 15   | 15. getLocation         | 获取定位            |
| 16   | 16. locationToDegree    | 定位转度数          |

### *008.亮度BrightnessKit(48)*

| 序号 | 方法                         | 功能               |
|:-----|:-----------------------------|:------------------|
| 01   | 01. isAutoBrightnessEnabled  | 是否自动亮度启用   |
| 02   | 02. setAutoBrightnessEnabled | 设置是否自动亮度   |
| 03   | 03. brightness               | 获取亮度          |
| 04   | 04. setBrightness            | 设置亮度0..255    |
| 05   | 05. getWindowBrightness      | 获取窗口亮度       |
| 06   | 06. setWindowBrightness      | 设置窗口亮度0..255 |

### *009.闪光FlashlightKit(58)*

| 序号 | 方法                    | 功能           |
|:-----|:------------------------|:--------------|
| 01   | 01. isFlashlightEnable  | 闪光灯是否可用 |
| 02   | 02. isFlashlightOn      | 闪光灯是否打开 |
| 03   | 03. setFlashlightStatus | 闪光灯设置开关 |
| 04   | 04. destroy             | 闪光灯销毁    |

### *010.振动VibrateKit(20)*

| 序号 | 方法                   | 功能     |
|:-----|:-----------------------|:--------|
| 01   | 01. vibrateOnce        | 一次振动 |
| 02   | 02. vibrateComplicated | 周期振动 |
| 03   | 03. vibrateStop        | 停止振动 |

### *011.蜂鸣BeepKit(39)*

| 序号 | 方法         | 功能         |
|:-----|:-------------|:------------|
| 01   | 01. playBeep | 播放蜂鸣声音 |

>- beep.ogg

### *012.系统SystemKit(231)：SystemLanguage*

| 序号 | 方法                                 | 功能                        |
|:-----|:-------------------------------------|:---------------------------|
| 01   | 01. buildManufacturer                | 厂商                       |
| 02   | 02. buildModel                       | 型号                       |
| 03   | 03. serial                           | 序列号                     |
| 04   | 04. uniqueSerial                     | 唯一序列号                  |
| 05   | 05. uniqueId                         | 设备物理唯一标识符，伪唯一ID |
| 06   | 06. buildSerial                      | 编译序列号                  |
| 07   | 07. buildBrand                       | 编译厂商                   |
| 08   | 08. buildHost                        | 编译主机                   |
| 09   | 09. buildUser                        | 编译作者                   |
| 10   | 10. buildTags                        | 编译描述                   |
| 11   | 11. buildTime                        | 编译时间                   |
| 12   | 12. buildFingerprint                 | 编译指纹                   |
| 13   | 13. buildProduct                     | 编译产品                   |
| 14   | 14. buildDevice                      | 编译设备                   |
| 15   | 15. buildHardware                    | 编译硬件                   |
| 16   | 16. buildBoard                       | 编译主板                   |
| 17   | 17. buildID                          | 修订版本列表                |
| 18   | 18. buildDisplayVersion              | 系统版本                   |
| 19   | 19. buildBootloaderVersion           | 启动程序版本                |
| 20   | 20. buildRadioVersion                | 基带版本                   |
| 21   | 21. buildAbis                        | CPU指令集                  |
| 22   | 22. buildVersionSDK                  | 系统SDK版本                |
| 23   | 23. buildVersionRelease              | 编译版本                   |
| 24   | 24. buildVersionCodename             | 开发代号                   |
| 25   | 25. buildVersionIncremental          | 源码控制版本                |
| 26   | 26. locales                          | 可用地区                   |
| 27   | 27. currentLocale                    | 当前地区                   |
| 28   | 28. currentLanguage                  | 当前语言                   |
| 29   | 29. gsfId                            | 谷歌服务框架ID              |
| 30   | 30. googleAccounts                   | 谷歌账号                   |
| 31   | #### SystemLanguage                  | 系统语言                   |
| 32   | 01. applyLanguageSystem              | 应用系统语言                |
| 33   | 02. applyLanguageCustom              | 应用自定义语言              |
| 34   | 03. isAppliedLanguageSystem          | 是否已应用系统语言          |
| 35   | 04. applyLanguageSystemInAppOnCreate | 创建生命周期应用系统语言     |
| 36   | 05. isAppliedLanguageCustom          | 是否已应用自定义语言        |
| 37   | 06. applyLanguageCustomInAppOnCreate | 创建生命周期应用自定义语言   |
| 38   | 07. applyLanguage                    | 应用语言                   |

### *013.版本VersionKit(68)*

| 序号 | 方法                         | 功能              |
|:-----|:-----------------------------|:------------------|
| 01   | 01. aboveAstro               | 安卓系统版本01以上 |
| 02   | 02. aboveBender              | 安卓系统版本02以上 |
| 03   | 03. aboveCupcake             | 安卓系统版本03以上 |
| 04   | 04. aboveDonut               | 安卓系统版本04以上 |
| 05   | 05. aboveEclair              | 安卓系统版本05以上 |
| 06   | 06. aboveEclair01            | 安卓系统版本06以上 |
| 07   | 07. aboveEclairMR1           | 安卓系统版本07以上 |
| 08   | 08. aboveFroyo               | 安卓系统版本08以上 |
| 09   | 09. aboveGingerbread         | 安卓系统版本09以上 |
| 10   | 10. aboveGingerbreadMR1      | 安卓系统版本10以上 |
| 11   | 11. aboveHoneycomb           | 安卓系统版本11以上 |
| 12   | 12. aboveHoneycombMR1        | 安卓系统版本12以上 |
| 13   | 13. aboveHoneycombMR2        | 安卓系统版本13以上 |
| 14   | 14. aboveIceCreamSandwich    | 安卓系统版本14以上 |
| 15   | 15. aboveIceCreamSandwichMR1 | 安卓系统版本15以上 |
| 16   | 16. aboveJellyBean           | 安卓系统版本16以上 |
| 17   | 17. aboveJellyBeanMR1        | 安卓系统版本17以上 |
| 18   | 18. aboveJellyBeanMR2        | 安卓系统版本18以上 |
| 19   | 19. aboveKitKat              | 安卓系统版本19以上 |
| 20   | 20. aboveKitKatWatch         | 安卓系统版本20以上 |
| 21   | 21. aboveLollipop            | 安卓系统版本21以上 |
| 22   | 22. aboveLollipopMR1         | 安卓系统版本22以上 |
| 23   | 23. aboveMarshmallow         | 安卓系统版本23以上 |
| 24   | 24. aboveNougat              | 安卓系统版本24以上 |
| 25   | 25. aboveNougatMR1           | 安卓系统版本25以上 |
| 26   | 26. aboveOreo                | 安卓系统版本26以上 |
| 27   | 27. aboveOreoMR1             | 安卓系统版本27以上 |
| 28   | 28. abovePie                 | 安卓系统版本28以上 |
| 29   | 29. aboveQ                   | 安卓系统版本29以上 |
| 30   | 30. aboveR                   | 安卓系统版本30以上 |
| 31   | 31. aboveS                   | 安卓系统版本31以上 |

### *014.机型RomKit(215)*

| 序号 | 方法            | 功能           |
|:-----|:----------------|:--------------|
| 01   | 01. isHuawei    | 是否华为手机   |
| 02   | 02. isVivo      | 是否Vivo手机  |
| 03   | 03. isXiaomi    | 是否小米手机   |
| 04   | 04. isOppo      | 是否Oppo手机  |
| 05   | 05. isLeeco     | 是否乐视手机   |
| 06   | 06. is360       | 是否360手机   |
| 07   | 07. isZte       | 是否中兴手机   |
| 08   | 08. isOneplus   | 是否一加手机   |
| 09   | 09. isNubia     | 是否努比亚手机 |
| 10   | 10. isCoolpad   | 是否酷派手机   |
| 11   | 11. isLg        | 是否LG手机    |
| 12   | 12. isGoogle    | 是否谷歌手机   |
| 13   | 13. isSamsung   | 是否三星手机   |
| 14   | 14. isMeizu     | 是否魅族手机   |
| 15   | 15. isLenovo    | 是否联想手机   |
| 16   | 16. isSmartisan | 是否锤子手机   |
| 17   | 17. isHtc       | 是否宏达手机   |
| 18   | 18. isSony      | 是否索尼手机   |
| 19   | 19. isAmigo     | 是否金立手机   |

### *015.重启RebootKit(35)*

| 序号 | 方法                  | 功能                          |
|:-----|:----------------------|:-----------------------------|
| 01   | 01. reboot            | 重启android.permission.REBOOT |
| 02   | 02. reboot2Recovery   | 重启到恢复界面                |
| 03   | 03. reboot2Bootloader | 重启到引导界面                |
| 04   | 04. shutdown          | 关机                          |

### *016.命令ShellKit(86)*

| 序号 | 方法        | 功能     |
|:-----|:------------|:--------|
| 01   | 01. execCmd | 执行命令 |

### *017.破解RootKit(43)*

| 序号 | 方法               | 功能         |
|:-----|:-------------------|:------------|
| 01   | 01. isRoot         | 是否root    |
| 02   | 02. isRooted       | 是否已root   |
| 03   | 03. rootPermission | 获取root权限 |

### *018.应用ApplicationKit(604)*

| 序号 | 方法                                   | 功能                    |
|:-----|:---------------------------------------|:-----------------------|
| 01   | 01. numCores                           | CPU内核数量             |
| 02   | 02. isServiceRunning                   | 指定服务是否运行         |
| 03   | 03. stopRunningService                 | 停止运行指定服务         |
| 04   | 04. runScript                          | 运行脚本                |
| 05   | 05. killProcess                        | 结束进程                |
| 06   | 06. registerAppStatusChangedListener   | 注册应用状态改变监听器   |
| 07   | 07. unregisterAppStatusChangedListener | 取消应用状态改变监听器   |
| 08   | 08. appPackageName                     | 应用包名                |
| 09   | 09. installApp                         | 安装应用                |
| 10   | 10. installAppSilent                   | 静默安装应用            |
| 11   | 11. uninstallApp                       | 卸载应用                |
| 12   | 12. uninstallAppSilent                 | 静默卸载应用            |
| 13   | 13. isAppInstalled                     | 是否安装应用            |
| 14   | 14. isAppRoot                          | 是否破解应用            |
| 15   | 15. isAppDebug                         | 是否调试应用            |
| 16   | 16. isAppSystem                        | 是否系统应用            |
| 17   | 17. isAppBackground                    | 是否背景应用            |
| 18   | 18. isAppForeground                    | 是否前景应用            |
| 19   | 19. launchApp                          | 打开应用                |
| 20   | 20. relaunchApp                        | 重启应用                |
| 21   | 21. launchAppDetailsSettings           | 打开应用详细设置         |
| 22   | 22. getAppInstaller                    | 获取应用安装器          |
| 23   | 23. getAppFirstInstallTime             | 获取应用首次安装时间     |
| 24   | 24. getAppLastUpdateTime               | 获取应用最近更新时间     |
| 25   | 25. getAppTargetSdkVersion             | 获取应用目标系统版本     |
| 26   | 26. getAppUid                          | 获取应用uid             |
| 27   | 27. getApplicationMetaData             | 获取应用元数据          |
| 28   | 28. getAppSize                         | 获取应用尺寸            |
| 29   | 29. appIcon                            | 获取应用图标            |
| 30   | 30. getAppIcon                         | 获取应用图标指定包       |
| 31   | 31. appName                            | 获取应用名称            |
| 32   | 32. getAppName                         | 获取应用名称指定包       |
| 33   | 33. appPath                            | 获取应用路径            |
| 34   | 34. getAppPath                         | 获取应用路径指定包       |
| 35   | 35. appVersionName                     | 获取应用版本名称         |
| 36   | 36. getAppVersionName                  | 获取应用版本名称指定包   |
| 37   | 37. appVersionCode                     | 获取应用版本号码         |
| 38   | 38. getAppVersionCode                  | 获取应用版本号码指定包   |
| 39   | 39. appSignature                       | 获取应用签名            |
| 40   | 40. getAppSignature                    | 获取应用签名指定包       |
| 41   | 41. appSignatureSHA1                   | 获取应用签名SHA1        |
| 42   | 42. getAppSignatureSHA1                | 获取应用签名SHA1指定包   |
| 43   | 43. appSignatureSHA256                 | 获取应用签名SHA256      |
| 44   | 44. getAppSignatureSHA256              | 获取应用签名SHA256指定包 |
| 45   | 45. appSignatureMD5                    | 获取应用签名MD5         |
| 46   | 46. getAppSignatureMD5                 | 获取应用签名MD5指定包    |
| 47   | 47. appsInfo                           | 获取应用信息列表         |
| 48   | 48. appInfo                            | 获取应用信息            |
| 49   | 49. getAppInfo                         | 获取应用信息指定包       |
| 50   | 50. getApkInfo                         | 获取安装包信息          |

### *019.崩溃CrashKit(143)*

| 序号 | 方法                | 功能     |
|:-----|:--------------------|:--------|
| 01   | 01. init            | 初始崩溃 |
| 02   | 02. crashTip        | 崩溃提示 |
| 03   | 03. OnCrashListener | 崩溃监听 |

### *020.退出ExitKit(59)*

| 序号 | 方法                    | 功能           |
|:-----|:------------------------|:--------------|
| 01   | 01. exitApp             | 退出应用      |
| 02   | 02. OnExitClickListener | 退出点击监听器 |

### *021.清理CleanKit(50)*

| 序号 | 方法                      | 功能                |
|:-----|:--------------------------|:-------------------|
| 01   | 01. cleanAppData          | 清除应用数据        |
| 02   | 02. cleanInternalFiles    | 清除应用文件        |
| 03   | 03. cleanInternalCache    | 清除应用缓存        |
| 04   | 04. cleanExternalCache    | 清除应用缓存位于SD卡 |
| 05   | 05. cleanInternalSP       | 清除应用SP数据      |
| 06   | 06. cleanInternalDbs      | 清除应用所有数据库   |
| 07   | 07. cleanInternalDbByName | 清除应用指定数据库   |
| 08   | 08. cleanCustomCache      | 清除自定义数据      |
| 09   | 09. totalCacheSize        | 缓存尺寸总计        |

### *022.快捷ShortcutKit(40)*

| 序号 | 方法            | 功能         |
|:-----|:----------------|:------------|
| 01   | 01. hasShortcut | 是否存在快捷 |
| 02   | 02. addShortcut | 添加快捷    |
| 03   | 03. delShortcut | 删除快捷    |

### *023.路由RouteKit(142)*

| 序号 | 方法               | 功能           |
|:-----|:-------------------|:--------------|
| 01   | 01. requestCode    | 请求码         |
| 02   | 02. newIntent      | 新意图         |
| 03   | 03. addFlags       | 添加标志       |
| 04   | 04. putExtraParam  | 放入Extra参数  |
| 05   | 05. setCallback    | 设置路由回调   |
| 06   | 06. to             | 设置to活动片段 |
| 07   | 07. putBundle      | 放入Bundle    |
| 08   | 08. putBundleParam | 放入Bundle参数 |
| 09   | 09. options        | 设置选项       |
| 10   | 10. anim           | 设置进出动画   |
| 11   | 11. launch         | 打开          |
| 12   | 12. pop            | 弹出          |

### *024.活动ActivityKit(669)*

| 序号 | 方法                                | 功能                    |
|:-----|:------------------------------------|:------------------------|
| 01   | 01. startActivity                   | 开始单活动              |
| 02   | 02. startActivityForResult          | 开始活动返回结果         |
| 03   | 03. startActivities                 | 开始多活动              |
| 04   | 04. startHomeActivity               | 开始主活动              |
| 05   | 05. activityList                    | 活动列表                |
| 06   | 06. currentActivity                 | 当前活动                |
| 07   | 07. topActivityName                 | 顶活动名                |
| 08   | 08. launcherActivityName            | launcher活动名          |
| 09   | 09. getLauncherActivityName         | 获取指定包launcher活动名 |
| 10   | 10. getActivityByView               | 获取视图所在活动         |
| 11   | 11. isActivityExists                | 活动是否存在            |
| 12   | 12. isActivityExistsInList          | 活动是否存在于列表       |
| 13   | 13. finishActivity                  | 结束活动                |
| 14   | 14. finishToActivity                | 结束转到活动            |
| 15   | 15. finishOtherActivities           | 结束其它活动            |
| 16   | 16. finishAllActivitiesExceptNewest | 结束所有活动除了最近使用 |
| 17   | 17. finishAllActivities             | 结束所有活动            |
| 18   | 18. getActivityIcon                 | 获取活动图标            |
| 19   | 19. getActivityLogo                 | 获取活动logo            |

### *025.分享ShareKit(238)*

| 序号 | 方法                                              | 功能                      |
|:-----|:--------------------------------------------------|:-------------------------|
| 01   | 01. shareMultiplePicture                          | 分享多图片                |
| 02   | 02. shareMultiplePictureForResult                 | 分享多图片返回结果        |
| 03   | 03. shareMultiplePictureToWeChatCircle            | 分享多图片到朋友圈        |
| 04   | 04. shareMultiplePictureToWeChatCircleForResult   | 分享多图片到朋友圈返回结果 |
| 05   | 05. shareMultiplePictureToWeChatContacts          | 分享多图片到联系人        |
| 06   | 06. shareMultiplePictureToWeChatContactsForResult | 分享多图片到联系人返回结果 |
| 07   | 07. sharePicture                                  | 分享单图片                |
| 08   | 08. sharePictureToWeChatCircle                    | 分享单图片到朋友圈        |
| 09   | 09. sharePictureToWeChatContacts                  | 分享单图片到联系人        |
| 10   | 10. sharePictureForResult                         | 分享单图片返回结果        |
| 11   | 11. sharePictureToWeChatCircleForResult           | 分享单图片到朋友圈返回结果 |
| 12   | 12. sharePictureToWeChatContactsForResult         | 分享单图片到联系人返回结果 |
| 13   | 13. shareVideo                                    | 分享视频                 |
| 14   | 14. shareVideoToWeChatContacts                    | 分享视频到联系人          |
| 15   | 15. shareVideoForResult                           | 分享视频返回结果          |
| 16   | 16. shareVideoToWeChatContactsForResult           | 分享视频到联系人返回结果   |
| 17   | 17. shareFile                                     | 分享文件                 |
| 18   | 18. shareFileToWeChatContacts                     | 分享文件到联系人          |
| 19   | 19. shareFileForResult                            | 分享文件返回结果          |
| 20   | 20. shareFileToWeChatContactsForResult            | 分享文件到联系人返回结果   |

### *026.片段FragmentKit(485)*

| 序号 | 方法                       | 功能              |
|:-----|:---------------------------|:-----------------|
| 01   | 01. addFragment            | 添加单片段        |
| 02   | 02. addFragments           | 添加多片段        |
| 03   | 03. hideAddFragment        | 隐藏添加片段      |
| 04   | 04. popAddFragment         | 弹出添加片段      |
| 05   | 05. replaceFragment        | 替换片段          |
| 06   | 06. showFragment           | 显示单片段        |
| 07   | 07. showFragments          | 显示多片段        |
| 08   | 08. hideFragment           | 隐藏单片段        |
| 09   | 09. hideFragments          | 隐藏多片段        |
| 10   | 10. hideAllShowFragment    | 隐藏所有显示片段   |
| 11   | 11. hideOthersShowFragment | 隐藏其他显示片段   |
| 12   | 12. hideShowFragment       | 隐藏显示片段      |
| 13   | 13. removeFragment         | 移除单片段        |
| 14   | 14. removeToFragment       | 移除转到片段      |
| 15   | 15. removeFragments        | 移除多片段        |
| 16   | 16. popFragment            | 弹出单片段        |
| 17   | 17. popToFragment          | 弹出转到片段      |
| 18   | 18. popFragments           | 弹出多片段        |
| 19   | 19. getLastAddFragment     | 获取最近添加片段   |
| 20   | 20. getTopShowFragment     | 获取顶部显示片段   |
| 21   | 21. getFragments           | 获取所有片段      |
| 22   | 22. getNodes               | 获取片段节点      |
| 23   | 23. getPreFragment         | 获取之前片段      |
| 24   | 24. getFragment            | 获取片段          |
| 25   | 25. OnBackClickListener    | 返回点击监听器    |
| 26   | 26. dispatchBackPress      | 执行返回点击      |
| 27   | 27. setBackgroundColor     | 设置背景颜色      |
| 28   | 28. setBackgroundResource  | 设置背景资源      |
| 29   | 29. setBackgroundDrawable  | 设置背景可绘制对象 |
| 30   | 30. getSimpleName          | 获取片段名称      |

### *027.服务ServiceKit(66)*

| 序号 | 方法                   | 功能           |
|:-----|:-----------------------|:--------------|
| 01   | 01. allRunningServices | 所有运行中服务 |
| 02   | 02. isServiceRunning   | 是否服务运行中 |
| 03   | 03. startService       | 开始服务      |
| 04   | 04. stopService        | 停止服务      |
| 05   | 05. bindService        | 绑定服务      |
| 06   | 06. unbindService      | 解绑服务      |

### *028.管理ManagerKit(159)*

| 序号 | 方法                          | 功能           |
|:-----|:------------------------------|:--------------|
| 01   | 01. accessibilityManager      | 访问管理器     |
| 02   | 02. accountManager            | 账号管理器     |
| 03   | 03. activityManager           | 活动管理器     |
| 04   | 04. alarmManager              | 警告管理器     |
| 05   | 05. appOpsManager             | 应用管理器     |
| 06   | 06. audioManager              | 音频管理器     |
| 07   | 07. batteryManager            | 电池管理器     |
| 08   | 08. bluetoothManager          | 蓝牙管理器     |
| 09   | 09. cameraManager             | 相机管理器     |
| 10   | 10. captioningManager         | 照片管理器     |
| 11   | 11. carrierConfigManager      | 运输管理器     |
| 12   | 12. clipboardManager          | 剪贴管理器     |
| 13   | 13. companionDeviceManager    | 附件管理器     |
| 14   | 14. connectivityManager       | 连接管理器     |
| 15   | 15. consumerIrManager         | 消费管理器     |
| 16   | 16. devicePolicyManager       | 设备管理器     |
| 17   | 17. displayManager            | 显示管理器     |
| 18   | 18. downloadManager           | 下载管理器     |
| 19   | 19. fingerprintManager        | 指纹管理器     |
| 20   | 20. hardwarePropertiesManager | 硬件管理器     |
| 21   | 21. inputManager              | 输入管理器     |
| 22   | 22. inputMethodManager        | 输法管理器     |
| 23   | 23. keyguardManager           | 键锁管理器     |
| 24   | 24. layoutInflater            | 布局管理器     |
| 25   | 25. locationManager           | 定位管理器     |
| 26   | 26. mediaProjectionManager    | 媒体工程管理器 |
| 27   | 27. mediaSessionManager       | 媒体场节管理器 |
| 28   | 28. midiManager               | 乐器管理器     |
| 29   | 29. networkStatsManager       | 网态管理器     |
| 30   | 30. nfcManager                | nfc管理器     |
| 31   | 31. notificationManager       | 通知管理器     |
| 32   | 32. nsdManager                | nsd管理器     |
| 33   | 33. powerManager              | 动力管理器     |
| 34   | 34. printManager              | 打印管理器     |
| 35   | 35. restrictionsManager       | 限制管理器     |
| 36   | 36. searchManager             | 搜索管理器     |
| 37   | 37. sensorManager             | 传感管理器     |
| 38   | 38. shortcutManager           | 快捷管理器     |
| 39   | 39. storageManager            | 存储管理器     |
| 40   | 40. storageStatsManager       | 存储状态管理器 |
| 41   | 41. systemHealthManager       | 系统健康管理器 |
| 42   | 42. telecomManager            | 电信管理器     |
| 43   | 43. telephonyManager          | 通信管理器     |
| 44   | 44. textClassificationManager | 文本管理器     |
| 45   | 45. tvInputManager            | tv输入管理器   |
| 46   | 46. uiModeManager             | ui模式管理器   |
| 47   | 47. usageStatsManager         | 使用状态管理器 |
| 48   | 48. usbManager                | usb管理器     |
| 49   | 49. userManager               | 用户管理器     |
| 50   | 50. vibrator                  | 振动管理器     |
| 51   | 51. wallpaperManager          | 墙纸管理器     |
| 52   | 52. wifiAwareManager          | wifi意识管理器 |
| 53   | 53. wifiManager               | wifi管理器    |
| 54   | 54. wifiP2pManager            | wifi点点管理器 |
| 55   | 55. windowManager             | 窗口管理器     |

### *029.轮询PollingKit(38)*

| 序号 | 方法                      | 功能            |
|:-----|:--------------------------|:----------------|
| 01   | 01. isExistPollingService | 是否存在轮询服务 |
| 02   | 02. startPollingService   | 开始轮询服务    |
| 03   | 03. stopPollingService    | 停止轮询服务    |

### *030.广播BroadcastKit(22)*

| 序号 | 方法                        | 功能                |
|:-----|:----------------------------|:-------------------|
| 01   | 01. registerReceiverNetWork | 注册网络状态改变广播 |

### *031.意图IntentKit(316)*

| 序号 | 方法                            | 功能                |
|:-----|:--------------------------------|:-------------------|
| 01   | 01. isIntentAvailable           | 是否意图可用        |
| 02   | 02. getInstallAppIntent         | 获取安装应用意图    |
| 03   | 03. getUninstallAppIntent       | 获取卸载应用意图    |
| 04   | 04. getLaunchAppIntent          | 获取打开应用意图    |
| 05   | 05. getAppDetailsSettingsIntent | 获取应用详细设置意图 |
| 06   | 06. getAppStoreIntent           | 获取应用商店意图    |
| 07   | 07. getCaptureIntent            | 获取拍照意图        |
| 08   | 08. getPickIntentWithGallery    | 获取选择图片意图    |
| 09   | 09. getPickIntentWithDocuments  | 获取选择文件意图    |
| 10   | 10. buildImageGetIntent         | 构建图片获取意图    |
| 11   | 11. buildImageCropIntent        | 构建图片裁剪意图    |
| 12   | 12. getShareImageIntent         | 获取分享图片意图    |
| 13   | 13. getShareTextIntent          | 获取分享文本意图    |
| 14   | 14. getDialIntent               | 获取拨号意图        |
| 15   | 15. getCallIntent               | 获取拨打意图        |
| 16   | 16. getSendSmsIntent            | 获取发送短信意图    |
| 17   | 17. shutdownIntent              | 关机意图            |
| 18   | 18. getShutdownIntent           | 获取关机意图        |
| 19   | 19. getComponentIntent          | 获取包类意图        |

### *032.元数MetaDataKit(53)*

| 序号 | 方法                      | 功能            |
|:-----|:--------------------------|:----------------|
| 01   | 01. getMetaDataInApp      | 获取应用元数据   |
| 02   | 02. getMetaDataInActivity | 获取活动元数据   |
| 03   | 03. getMetaDataInService  | 获取服务元数据   |
| 04   | 04. getMetaDataInReceiver | 获取接收器元数据 |

### *033.权限PermissionKit(436)*

| 序号 | 方法                         | 功能                |
|:-----|:-----------------------------|:-------------------|
| 01   | 01. builderSimple            | 简单权限构建        |
| 02   | 02. addPermission            | 添加权限            |
| 03   | 03. initPermission           | 初始权限            |
| 04   | 04. builderPermissions       | 复杂权限构建        |
| 05   | 05. request                  | 请求权限            |
| 06   | 06. rationale                | 设置监听            |
| 07   | 07. simple                   | 设置简单回调        |
| 08   | 08. full                     | 设置完整回调        |
| 09   | 09. theme                    | 设置主题回调        |
| 10   | 10. permission               | 权限构建器          |
| 11   | 11. getPermissions           | 获取权限列表        |
| 12   | 12. launchAppDetailsSettings | 打开应用详细设置界面 |
| 13   | 13. isGrantedWriteSettings   | 是否可写权限        |
| 14   | 14. requestWriteSettings     | 请求可写权限        |
| 15   | 15. isGrantedDrawOverlays    | 是否请求绘制覆盖物   |
| 16   | 16. requestDrawOverlays      | 请求绘制覆盖物      |
| 17   | 17. isGranted                | 是否请求            |
| 18   | #### PermissionConstants     | 权限常量            |
| 19   | 01. PHONE                    | 手机权限            |
| 20   | 02. SENSORS                  | 传感权限            |
| 21   | 03. LOCATION                 | 定位权限            |
| 22   | 04. MICROPHONE               | 麦克权限            |
| 23   | 05. CAMERA                   | 相机权限            |
| 24   | 06. STORAGE                  | 存储权限            |
| 25   | 07. CALENDAR                 | 日历权限            |
| 26   | 08. CONTACTS                 | 联系权限            |
| 27   | 09. SMS                      | 短信权限            |
| 28   | 10. getPermissions           | 获取权限列表        |

### *034.屏幕ScreenKit(260)*

| 序号 | 方法                            | 功能              |
|:-----|:--------------------------------|:-----------------|
| 01   | 01. isScreenLock                | 是否屏幕锁定      |
| 02   | 02. sleepDuration               | 休眠持续时间      |
| 03   | 03. noShootScreen               | 禁止截屏         |
| 04   | 04. shootScreenWithStatusBar    | 截屏含状态栏      |
| 05   | 05. shootScreenWithoutStatusBar | 截屏无状态栏      |
| 06   | 06. shootWebView                | 截屏网络视图      |
| 07   | 07. realScreenHeight            | 真实屏幕高度      |
| 08   | 08. screenWidthByPoint          | 屏幕宽度点获取    |
| 09   | 09. screenHeightByPoint         | 屏幕高度点获取    |
| 10   | 10. screenWidth                 | 屏幕宽度         |
| 11   | 11. screenWidth8                | 屏幕宽度八成      |
| 12   | 12. screenHeight                | 屏幕高度         |
| 13   | 13. screenHeight8               | 屏幕高度八成      |
| 14   | 14. navigationAreaHeight        | 虚拟按键区域高度  |
| 15   | 15. navigationBarHeight         | 导航栏高度       |
| 16   | 16. toolbarHeight               | 工具栏高度       |
| 17   | 17. statusBarHeight             | 状态栏高度       |
| 18   | 18. getTitleBarHeight           | 标题栏高度       |
| 19   | 19. screenDisplayId             | 屏幕显示ID       |
| 20   | 20. screenDensity               | 屏幕尺寸         |
| 21   | 21. screenDensityDpi            | 屏幕尺寸dpi      |
| 22   | 22. screenDensityDpiStr         | 屏幕尺寸dpi字符串 |
| 23   | 23. getSysSampleSize            | 获取系统样本尺寸  |
| 24   | 24. adaptWidth                  | 适配宽度         |
| 25   | 25. adaptHeight                 | 适配高度         |
| 26   | 26. closeAdapt                  | 关闭适配         |

### *035.窗口WindowKit(60)*

| 序号 | 方法                 | 功能                |
|:-----|:---------------------|:-------------------|
| 01   | 01. displayRotation  | 显示方向            |
| 02   | 02. isLandscape      | 是否横屏            |
| 03   | 03. setLandscape     | 设置横屏            |
| 04   | 04. isPortrait       | 是否竖屏            |
| 05   | 05. setPortrait      | 设置竖屏            |
| 06   | 06. toggleFullScreen | 切换全屏            |
| 07   | 07. isFullScreen     | 是否全屏            |
| 08   | 08. setNoFullScreen  | 设置非全屏          |
| 09   | 09. setFullScreen    | 设置全屏            |
| 10   | 10. dimBackground    | 活动背景透明消失动画 |

### *036.多栏BarKit(531)*

| 序号 | 方法                                                | 功能                                                              |
|:-----|:----------------------------------------------------|:-----------------------------------------------------------------|
| 01   | 01. isStatusBarExists                               | 状态栏是否存在                                                    |
| 02   | 02. isStatusBarVisible                              | 状态栏是否可见                                                    |
| 03   | 03. setStatusBarVisibility                          | 设置状态栏可见性                                                  |
| 04   | 04. isStatusBarModeDark                             | 状态栏是否暗模式                                                  |
| 05   | 05. Mode                                            | NOON,MIUI,FLYME,OTHERS                                           |
| 06   | 06. setStatusBarModeDark                            | 设置状态栏暗模式                                                  |
| 07   | 07. getStatusBarColor                               | 获取状态栏颜色                                                    |
| 08   | 08. setStatusBarColor                               | 设置状态栏颜色                                                    |
| 09   | 09. setStatusBarColorDiff                           | 设置状态栏颜色，5.0以上状态栏不透明颜色                             |
| 10   | 10. setStatusBarColorForDrawerLayout                | 设置DrawerLayout状态栏颜色                                        |
| 11   | 11. setStatusBarColorForDrawerLayoutDiff            | 设置DrawerLayout状态栏颜色，5.0以上状态栏半透明颜色                 |
| 12   | 12. setStatusBarColorForSwipeBack                   | 设置滑动返回状态栏颜色                                             |
| 13   | 13. setAllTransparent                               | 设置全透明                                                        |
| 14   | 14. setStatusBarTranslucent                         | 设置状态栏透明度                                                  |
| 15   | 15. setStatusBarTransparent                         | 设置状态栏透明                                                    |
| 16   | 16. setStatusBarTranslucentDiff                     | 设置状态栏透明，5.0以上状态栏半透明无色，界面背景图片填充到状态栏适用 |
| 17   | 17. setStatusBarTranslucentForDrawerLayout          | 设置DrawerLayout状态栏透明                                        |
| 18   | 18. setStatusBarTranslucentForDrawerLayoutDiff      | 设置DrawerLayout状态栏透明，5.0以上状态栏半透明无色                 |
| 19   | 19. setStatusBarTranslucentForCoordinatorLayout     | 设置CoordinatorLayout状态栏透明，界面背景图片填充到状态栏适用       |
| 20   | 20. setStatusBarTranslucentForImageViewIsInFragment | 设置片段中图片视图状态栏透明                                       |
| 21   | 21. setNotificationBarVisibility                    | 设置通知栏可见性                                                  |
| 22   | 22. actionBarHeight                                 | ActionBar高度                                                    |
| 23   | 23. isNavBarSupported                               | 是否支持导航栏                                                    |
| 24   | 24. isNavBarVisible                                 | 是否显示导航栏                                                    |
| 25   | 25. setNavBarVisibility                             | 设置导航栏可见性                                                  |
| 26   | 26. getNavBarColor                                  | 获取导航栏颜色                                                    |
| 27   | 27. setNavBarColor                                  | 设置导航栏颜色                                                    |

### *037.视图ViewKit(946)：ViewTouch、ViewClick、ViewShadow*

| 序号 | 方法                          | 功能              |
|:-----|:------------------------------|:-----------------|
| 01   | 01. showPopupWindow           | 显示自制弹窗      |
| 02   | 02. dismissPopupWindow        | 取消显示弹窗      |
| 03   | 03. setTVUnderLine            | 设置显示框下划线   |
| 04   | 04. setViewEnabled            | 批量设置视图可用性 |
| 05   | 05. isLayoutRtl               | 是否Rtl布局       |
| 06   | 06. isTouchInView             | 是否视图内部触摸   |
| 07   | 07. fixScrollViewTopping      | 修复滚动视图回顶   |
| 08   | 08. removeSelfFromParent      | 从父视图移除自身   |
| 09   | 09. requestLayoutParent       | 请求根父视图      |
| 10   | 10. getActivity               | 获取视图所在活动   |
| 11   | 11. bitmapView                | 视图截图1         |
| 12   | 12. shootView                 | 视图截图2         |
| 13   | 13. captureView               | 视图截图3         |
| 14   | 14. captureActivity           | 活动截图          |
| 15   | 15. bigImage                  | 缩放获取新图      |
| 16   | #### ViewTouch                | 视图触摸          |
| 17   | 01. UNKNOWN                   | 未知              |
| 18   | 02. LEFT                      | 左                |
| 19   | 03. UP                        | 上                |
| 20   | 04. RIGHT                     | 右                |
| 21   | 05. DOWN                      | 下                |
| 22   | 06. Direction                 | 方向注解          |
| 23   | 07. setOnTouchListener        | 设置触摸监听器    |
| 24   | 08. OnTouchUtilsListener      | 触摸监听器        |
| 25   | #### ViewClick                | 视图点击          |
| 26   | 01. applyPressedViewScale     | 批量按压缩放      |
| 27   | 02. applyPressedViewAlpha     | 批量按压透明      |
| 28   | 03. applyPressedBgAlpha       | 按压背景透明      |
| 29   | 04. applyPressedBgDark        | 按压背景变暗      |
| 30   | 05. applyDebouncingGlobal     | 全部视图防抖      |
| 31   | 06. OnDebouncingClickListener | 全部视图防抖监听器 |
| 32   | 07. applyDebouncingSingle     | 单个视图防抖      |
| 33   | 08. OnMultiClickListener      | 连点监听器        |
| 34   | #### ViewShadow               | 视图阴影          |
| 35   | 01. apply                     | 视图设置阴影      |

### *038.网视WebViewKit(76)*

| 序号 | 方法          | 功能            |
|:-----|:--------------|:----------------|
| 01   | 01. init      | 初始化网络视图   |
| 02   | 02. userAgent | 浏览器指纹      |
| 03   | 03. loadData  | 网络视图加载数据 |
| 04   | 04. goBack    | 网络视图返回    |

### *039.着色ColorKit(76)*

| 序号 | 方法                    | 功能                                                           |
|:-----|:------------------------|:--------------------------------------------------------------|
| 01   | 01. randomColor         | 随机颜色                                                       |
| 02   | 02. getRandomColor      | 获取随机颜色                                                   |
| 03   | 03. getColor            | 根据ID获取颜色                                                 |
| 04   | 04. setAlphaComponent   | 颜色设置透明度                                                 |
| 05   | 05. setRedComponent     | 颜色设置红成分                                                 |
| 06   | 06. setGreenComponent   | 颜色设置绿成分                                                 |
| 07   | 07. setBlueComponent    | 颜色设置蓝成分                                                 |
| 08   | 08. string2ColorInt     | 颜色字符串转颜色值                                              |
| 09   | 09. int2ArgbOrRgbString | 颜色值转ARGB或RGB颜色字符串                                     |
| 10   | 10. tintCursorDrawable  | 输入框着色：API32起不可用@SuppressLint("SoonBlockedPrivateApi") |
| 11   | 11. tintDrawable        | 可绘制对象着色                                                 |

### *040.吐司ToastKit(360)*

| 序号 | 方法                   | 功能（API25时WindowManager.LayoutParams.LAST_APPLICATION_WINDOW正常） |
|:-----|:-----------------------|:---------------------------------------------------------------------|
| 01   | 01. TopCenter          | 顶部居中                                                             |
| 02   | 02. TopLeft            | 顶部居左                                                             |
| 03   | 03. TopRight           | 顶部居右                                                             |
| 04   | 04. Center             | 中部居中                                                             |
| 05   | 05. CenterLeft         | 中部居左                                                             |
| 06   | 06. CenterRight        | 中部居右                                                             |
| 07   | 07. BottomCenter       | 底部居中                                                             |
| 08   | 08. BottomLeft         | 底部居左                                                             |
| 09   | 09. BottomRight        | 底部居右                                                             |
| 10   | 10. setGravity         | 设置对齐                                                             |
| 11   | 11. setMsgTextSize     | 设置消息文本尺寸                                                      |
| 12   | 12. COLOR_TEXT_DEFAULT | 消息文本默认颜色                                                      |
| 13   | 13. setMsgColor        | 设置消息文本颜色                                                      |
| 14   | 14. setBgResource      | 设置背景资源                                                          |
| 15   | 15. COLOR_BG_INFO      | 信息背景颜色                                                          |
| 16   | 16. COLOR_BG_SUCCESS   | 成功背景颜色                                                          |
| 17   | 17. COLOR_BG_WARNING   | 警告背景颜色                                                          |
| 18   | 18. COLOR_BG_ERROR     | 错误背景颜色                                                          |
| 19   | 19. setBgColor         | 设置背景颜色                                                          |
| 20   | 20. showShort          | 显示短吐司                                                           |
| 21   | 21. showLong           | 显示长吐司                                                           |
| 22   | 22. cancel             | 取消显示                                                             |
| 23   | 23. showCustomShort    | 显示自定义视图短吐司                                                  |
| 24   | 24. showCustomLong     | 显示自定义视图长吐司                                                  |

### *041.零食SnackKit(107)*

| 序号 | 方法                      | 功能                  |
|:-----|:--------------------------|:---------------------|
| 01   | 01. colorSnackbar         | 颜色零食              |
| 02   | 02. typeSnackbar          | 类型零食              |
| 03   | 03. DEFAULT_INFO_BLUE     | 默认蓝类型            |
| 04   | 04. SUCCESS_CONFIRM_GREEN | 成功绿类型            |
| 05   | 05. WARNING_ORANGE        | 警告橙类型            |
| 06   | 06. ERROR_ALERT_RED       | 错误红类型            |
| 07   | 07. ERROR_ALERT_YELLOW    | 错误黄类型            |
| 08   | 08. switchType            | 切换类型              |
| 09   | 09. setColor              | 设置颜色              |
| 10   | 10. setBottomMargin       | 设置底部间距          |
| 11   | 11. setBackgroundResource | 设置背景资源          |
| 12   | 12. setAction             | 设置文本及其颜色和监听 |
| 13   | 13. getView               | 从零食获取视图        |
| 14   | 14. addView               | 添加视图到零食        |

>- implementation "com.google.android.material:material:1.4.0"

### *042.连点ClickKit(55)*

| 序号 | 方法                        | 功能           |
|:-----|:----------------------------|:--------------|
| 01   | 01. OnDoListener            | 点击执行监听器 |
| 02   | 02. initFastClickAndVibrate | 快速点击振动   |
| 03   | 03. isFastDoubleClick       | 是否快速点击   |
| 04   | 04. doClick                 | 连续点击      |

### *043.防抖AntiShakeKit(21)*

| 序号 | 方法        | 功能         |
|:-----|:------------|:------------|
| 01   | 01. isValid | 防抖是否可用 |

### *044.抖动ShakeKit(26)*

| 序号 | 方法               | 功能           |
|:-----|:-------------------|:--------------|
| 01   | 01. shakeAnimation | 抖动动画      |
| 02   | 02. shake          | 系列输入框抖动 |

### *045.全局AKit(276)*

| 序号 | 方法                        | 功能                |
|:-----|:----------------------------|:-------------------|
| 01   | 01. FileProvider4AutoKit    | 文件提供者，注意名称 |
| 02   | 02. ContentProvider4AutoKit | 内容提供者，注意名称 |
| 03   | 03. initContext             | 初始化上下文        |
| 04   | 04. app                     | 应用上下文          |
| 05   | 05. activityLifecycle       | 活动生命周期        |
| 06   | 06. activityLinkedList      | 活动列表            |
| 07   | 07. isForegroundApp         | 是否当前APP         |
| 08   | 08. topActivityOrApp        | 顶部活动或应用上下文 |

>- [provider_paths.xml](../../../../res/xml/provider_paths.xml)

### *046.接口ApiKit(44)*

| 序号 | 方法            | 功能                         |
|:-----|:----------------|:----------------------------|
| 01   | 01. registerApi | 注册API                     |
| 02   | 02. toString    | "$loggerTag: $apiMapInject" |
| 03   | 03. BaseApi     | API基类                     |
| 04   | 04. getApi      | 获取API                     |
| 05   | 05. Api         | API注解                     |

### *047.总线BusKit(239)*

| 序号 | 方法             | 功能                                |
|:-----|:-----------------|:-----------------------------------|
| 01   | 01. registerBus  | 注册总线                           |
| 02   | 02. toString     | "$loggerTag: $mTag_BusInfoListMap" |
| 03   | 03. register     | 注册                               |
| 04   | 04. post         | 执行                               |
| 05   | 05. unregister   | 取消注册                           |
| 06   | 06. postSticky   | 执行黏性                           |
| 07   | 07. removeSticky | 移除黏性                           |
| 08   | 08. ThreadMode   | 线程模式                           |
| 09   | 09. Bus          | 总线                               |

### *048.内存CacheMemoryKit(79)*

| 序号 | 方法              | 功能                                            |
|:-----|:------------------|:-----------------------------------------------|
| 01   | 01. SEC           | 1秒                                            |
| 02   | 02. MIN           | 60秒                                           |
| 03   | 03. HOUR          | 3600秒                                         |
| 04   | 04. DAY           | 86400秒                                        |
| 05   | 05. getInstance   | 获取实例                                       |
| 06   | 06. toString      | "$cacheKey@${Integer.toHexString(hashCode())}" |
| 07   | #### CacheMemory  | 内存缓存                                       |
| 08   | 01. cacheCount    | 默认缓存大小                                    |
| 09   | 02. getCacheCount | 获取缓存大小                                    |
| 10   | 03. put           | 放入缓存                                       |
| 11   | 04. get           | 从缓存获取                                     |
| 12   | 05. remove        | 从缓存移除                                     |
| 13   | 06. clear         | 清空缓存                                       |

### *049.磁盘CacheDiskKit(524)*

| 序号 | 方法                  | 功能                                            |
|:-----|:----------------------|:-----------------------------------------------|
| 01   | 01. SEC               | 1秒                                            |
| 02   | 02. MIN               | 60秒                                           |
| 03   | 03. HOUR              | 3600秒                                         |
| 04   | 04. DAY               | 86400秒                                        |
| 05   | 05. getInstance       | 获取实例                                       |
| 06   | 06. toString          | "$cacheKey@${Integer.toHexString(hashCode())}" |
| 07   | 07. XFileOutputStream | 文件输出流缓存                                  |
| 08   | #### CacheDisk        | 磁盘缓存                                       |
| 09   | 01. cacheCount        | 默认缓存大小                                    |
| 10   | 02. getCacheCount     | 获取缓存大小                                    |
| 11   | 03. cacheSize         | 默认缓存尺寸                                    |
| 12   | 04. getCacheSize      | 获取缓存尺寸                                    |
| 13   | 05. put               | 放入缓存                                       |
| 14   | 06. getBytes          | 从缓存获取字节数组                              |
| 15   | 07. getString         | 从缓存获取字符串                                |
| 16   | 08. getJSONObject     | 从缓存获取JsonObject                           |
| 17   | 09. getJSONArray      | 从缓存获取JsonArray                            |
| 18   | 10. getBitmap         | 从缓存获取位图                                  |
| 19   | 11. getDrawable       | 从缓存获取可绘制对象                            |
| 20   | 12. getSerializable   | 从缓存获取Serializable                         |
| 21   | 13. getParcelable     | 从缓存获取Parcelable                           |
| 22   | 14. remove            | 从缓存移除                                     |
| 23   | 15. clear             | 清空缓存                                       |

### *050.双重CacheDoubleKit(246)*

| 序号 | 方法                    | 功能                   |
|:-----|:------------------------|:----------------------|
| 01   | 01. SEC                 | 1秒                   |
| 02   | 02. MIN                 | 60秒                  |
| 03   | 03. HOUR                | 3600秒                |
| 04   | 04. DAY                 | 86400秒               |
| 05   | 05. getInstance         | 获取实例               |
| 06   | #### CacheDouble        | 双重缓存               |
| 07   | 01. cacheMemoryCount    | 内存缓存大小           |
| 08   | 02. getCacheMemoryCount | 获取内存缓存大小       |
| 09   | 03. cacheDiskCount      | 磁盘缓存大小           |
| 10   | 04. getCacheDiskCount   | 获取磁盘缓存大小       |
| 11   | 05. cacheDiskSize       | 磁盘缓存尺寸           |
| 12   | 06. getCacheDiskSize    | 获取磁盘缓存尺寸       |
| 13   | 07. put                 | 放入缓存               |
| 14   | 08. getBytes            | 从缓存获取字节数组     |
| 15   | 09. getString           | 从缓存获取字符串       |
| 16   | 10. getJSONObject       | 从缓存获取JsonObject   |
| 17   | 11. getJSONArray        | 从缓存获取JsonArray    |
| 18   | 12. getBitmap           | 从缓存获取位图         |
| 19   | 13. getDrawable         | 从缓存获取可绘制对象   |
| 20   | 14. getSerializable     | 从缓存获取Serializable |
| 21   | 15. getParcelable       | 从缓存获取Parcelable   |
| 22   | 16. remove              | 从缓存移除             |
| 23   | 17. clear               | 清空缓存               |

### *051.线程ThreadKit(328)*

| 序号 | 方法                         | 功能                                                                             |
|:-----|:-----------------------------|:---------------------------------------------------------------------------------|
| 01   | 01. defaultThreadPoolSize    | 获取默认线程池尺寸                                                                |
| 02   | 02. isMainThread             | 是否主线程                                                                       |
| 03   | 03. SimpleTask               | 简单任务                                                                         |
| 04   | 04. Task                     | 复杂任务                                                                         |
| 05   | 05. cancel                   | 取消任务                                                                         |
| 06   | 06. executeBySingleWithDelay | 单线程池执行任务，设置延迟时间，或者立即执行                                        |
| 07   | 07. executeByCachedWithDelay | 缓存线程池执行任务，设置延迟时间，或者立即执行                                      |
| 08   | 08. executeByIOWithDelay     | 输入输出线程池执行任务，设置延迟时间，或者立即执行                                  |
| 09   | 09. executeByCPUWithDelay    | CPU线程池执行任务，设置延迟时间，或者立即执行                                       |
| 10   | 10. executeByFixedWithDelay  | 补丁线程池执行任务，设置延迟时间，或者立即执行                                      |
| 11   | 11. executeWithDelay         | 指定线程池执行任务，设置延迟时间，或者立即执行                                      |
| 12   | 12. executeBySingleAtFixRate | 单线程池执行任务，固定执行间隔，设置首次延迟                                        |
| 13   | 13. executeByCachedAtFixRate | 缓存线程池执行任务，固定执行间隔，设置首次延迟                                      |
| 14   | 14. executeByIOAtFixRate     | 输入输出线程池执行任务，固定执行间隔，设置首次延迟                                  |
| 15   | 15. executeByCPUAtFixRate    | CPU线程池执行任务，固定执行间隔，设置首次延迟                                       |
| 16   | 16. executeByFixedAtFixRate  | 补丁线程池执行任务，固定执行间隔，设置首次延迟                                      |
| 17   | 17. executeAtFixedRate       | 指定线程池执行任务，固定执行间隔，设置首次延迟                                      |
| 18   | 18. poolSingle               | 单线程池                                                                         |
| 19   | 19. getPoolSingle            | 单线程池带优先级                                                                  |
| 20   | 20. poolCached               | 缓存线程池                                                                       |
| 21   | 21. getPoolCached            | 缓存线程池带优先级                                                                |
| 22   | 22. poolIo                   | 输入输出线程池                                                                    |
| 23   | 23. getPoolIo                | 输入输出线程池带优先级                                                            |
| 24   | 24. poolCpu                  | CPU线程池                                                                        |
| 25   | 25. getPoolCpu               | CPU线程池带优先级                                                                 |
| 26   | 26. getPoolFixed             | 补丁线程池带优先级                                                                |
| 27   | 27. getPool                  | 获取线程池                                                                       |
| 28   | 28. executorService          | 线程服务                                                                         |
| 29   | 29. shutDown                 | 关闭线程，已提交执行完，不接受新提交                                               |
| 30   | 30. shutDownNow              | 立即关闭线程，已提交也停止，返回等待列表                                           |
| 31   | 31. isShutDown               | 是否关闭线程                                                                     |
| 32   | 32. isTerminated             | 是否全部完成，关闭后判断是否全部完成                                               |
| 33   | 33. awaitTermination         | 等待完成，请求关闭、超时、线程中断，阻塞到所有任务执行完成                           |
| 34   | 34. submit                   | 提交任务                                                                         |
| 35   | 35. invokeAny                | 取消未完成任务，正常异常返回取消未完成任务                                          |
| 36   | 36. invokeAll                | 返回所有任务，返回列表顺序与给定列表顺序同，未超时已完成所有任务，已超时未完成所有任务 |

### *052.进程ProcessKit(151)*

| 序号 | 方法                           | 功能            |
|:-----|:-------------------------------|:----------------|
| 01   | 01. isMainProcess              | 是否主进程      |
| 02   | 02. currentProcessName         | 当前进程名      |
| 03   | 03. foregroundProcessName      | 前景进程名      |
| 04   | 04. allBackgroundProcesses     | 所有背景进程    |
| 05   | 05. killBackgroundProcesses    | 结束背景进程    |
| 06   | 06. killAllBackgroundProcesses | 结束所有背景进程 |

### *053.反射ReflectionKit(64)*

| 序号 | 方法                      | 功能         |
|:-----|:--------------------------|:------------|
| 01   | 01. getClassListByPackage | 获取包类列表 |
| 02   | 02. isInstance            | 是否实例    |
| 03   | 03. newInstance           | 创建实例    |
| 04   | 04. invokeMethod          | 执行方法    |
| 05   | 05. invokeStaticMethod    | 执行静态方法 |
| 06   | 06. getProperty           | 获取属性    |
| 07   | 07. getStaticProperty     | 获取静态属性 |
| 08   | 08. setProperty           | 设置属性    |
| 09   | 09. setStaticProperty     | 设置静态属性 |

### *054.单例SingletonKit.Singleton(16)*

| 序号 | 方法            | 功能     |
|:-----|:----------------|:--------|
| 01   | 01. newInstance | 创建单例 |
| 02   | 02. getInstance | 获取单例 |

### *055.空判EmptyKit(72)*

| 序号 | 方法               | 功能            |
|:-----|:-------------------|:----------------|
| 01   | 01. isEmptyAny     | 是否空对象      |
| 02   | 02. isNotEmptyAny  | 是否非空对象    |
| 03   | 03. hashCode       | 对象哈希码      |
| 04   | 04. getOrDefault   | 获取对象带默认值 |
| 05   | 05. equalsAny      | 比较对象        |
| 06   | 06. requireNonNull | 比较非空对象    |

### *056.输入InputMethodKit(248)*

| 序号 | 方法                                   | 功能                  |
|:-----|:---------------------------------------|:---------------------|
| 01   | 01. showInputMethod                    | 显示输入法            |
| 02   | 02. toggleInputMethod                  | 开关输入法            |
| 03   | 03. hideInputMethod                    | 隐藏输入法            |
| 04   | 04. hideInputMethodTimer               | 定时隐藏输入法        |
| 05   | 05. isInputMethodActive                | 是否可用输入法        |
| 06   | 06. isInputMethodVisible               | 是否可见输入法        |
| 07   | 07. OnSoftInputChangedListener         | 输入法改变监听器      |
| 08   | 08. registerSoftInputChangedListener   | 注册输入法改变监听器   |
| 09   | 09. unregisterSoftInputChangedListener | 取消输入法改变监听器   |
| 10   | 10. fixSoftInputLeaks                  | 修复输入法内存泄露    |
| 11   | 11. fixAndroidBug5497                  | 修复输入法5427bug     |
| 12   | 12. clickBlankArea2HideSoftInput       | 点击空白区域隐藏输入法 |

### *057.剪贴ClipboardKit(20)*

| 序号 | 方法           | 功能       |
|:-----|:---------------|:----------|
| 01   | 01. textClip   | 文本剪贴板 |
| 02   | 02. uriClip    | 地址剪贴板 |
| 03   | 03. intentClip | 意图剪贴板 |

### *058.字串StringKit(120)*

| 序号 | 方法                   | 功能                                              |
|:-----|:-----------------------|:-------------------------------------------------|
| 01   | 01. isEmptyNoTrim      | 是否空字符或长度为零                              |
| 02   | 02. isEmptyTrim        | 除空格后是否空字符或长度为零                       |
| 03   | 03. isNotNull          | 是否非空字符和“null”字符                          |
| 04   | 04. isNull             | 是否空字符或“null”字符                            |
| 05   | 05. isNotSpace         | 是否非空字符和长度为零和空格、制表符、回车符、换行符 |
| 06   | 06. isSpace            | 是否空字符或长度为零或空格、制表符、回车符、换行符   |
| 07   | 07. isNumberStr        | 是否数字字符串                                    |
| 08   | 08. isIntegerStr       | 是否整数字符串                                    |
| 09   | 09. isDoubleStr        | 是否小数字符串                                    |
| 10   | 10. isCnCharContains   | 是否包含中文字符                                  |
| 11   | 11. isCnCharAll        | 是否全是中文字符                                  |
| 12   | 12. isNotCnChar        | 是否非中文字符                                    |
| 13   | 13. isCnChar           | 是否是中文字符                                    |
| 14   | 14. isMessyCode        | 是否乱码                                          |
| 15   | 15. equalsString       | 比较字符串                                        |
| 16   | 16. equalsCharSequence | 比较字符序列                                      |
| 17   | 17. firstMatcher       | 获取首个匹配字符串                                |
| 18   | 18. firstSplit         | 按首个分隔符获取字符串数组                         |
| 19   | 19. anySplit           | 按指定分隔符获取字符串数组                         |
| 20   | 20. concatNoSpilt      | 无分隔符拼接字符串                                |
| 21   | 21. concatSpilt        | 指定分隔符拼接字符串                              |

### *059.富文SpanKit(795)*

| 序号 | 方法                   | 功能            |
|:-----|:-----------------------|:----------------|
| 01   | 01. builderSpan        | 构建富文本      |
| 02   | 02. setFlag            | 设置标识        |
| 03   | 03. setForegroundColor | 设置前景色      |
| 04   | 04. setBackgroundColor | 设置背景色      |
| 05   | 05. ALIGN_BOTTOM       | 底对齐          |
| 06   | 06. ALIGN_BASELINE     | 基线对齐        |
| 07   | 07. ALIGN_CENTER       | 中间对齐        |
| 08   | 08. ALIGN_TOP          | 顶对齐          |
| 09   | 09. setLineHeight      | 设置行高        |
| 10   | 10. setQuoteColor      | 设置引用颜色    |
| 11   | 11. setMargin          | 设置间距        |
| 12   | 12. setLeadingMargin   | 设置行距        |
| 13   | 13. setBullet          | 设置子弹        |
| 14   | 14. setFontSize        | 设置字号        |
| 15   | 15. setFontProportion  | 设置字体比例    |
| 16   | 16. setFontXProportion | 设置字体横向比例 |
| 17   | 17. setStrikeThrough   | 设置删除线      |
| 18   | 18. setUnderline       | 设置下划线      |
| 19   | 19. setSuperscript     | 设置上标        |
| 20   | 20. setSubscript       | 设置下标        |
| 21   | 21. setBold            | 设置粗体        |
| 22   | 22. setItalic          | 设置斜体        |
| 23   | 23. setBoldItalic      | 设置粗斜体      |
| 24   | 24. setFontFamily      | 设置字体系列    |
| 25   | 25. setTypeface        | 设置字体        |
| 26   | 26. setHorizontalAlign | 设置水平对齐    |
| 27   | 27. setVerticalAlign   | 设置垂直对齐    |
| 28   | 28. setClickSpan       | 设置点击跨度    |
| 29   | 29. setUrl             | 设置超链接      |
| 30   | 30. setBlur            | 设置模糊        |
| 31   | 31. setShader          | 设置着色        |
| 32   | 32. setShadow          | 功能阴影        |
| 33   | 33. setSpans           | 功能跨度        |
| 34   | 34. append             | 添加字符串      |
| 35   | 35. appendLine         | 添加行          |
| 36   | 36. appendImage        | 添加图片        |
| 37   | 37. appendSpace        | 添加空白        |
| 38   | 38. create             | 创建富文本      |

### *060.文本TextKit(444)*

| 序号 | 方法                      | 功能              |
|:-----|:--------------------------|:-----------------|
| 01   | 01. string2Unicode        | 字符串转Unicode码 |
| 02   | 02. string2Ascii          | 字符串转Ascii码   |
| 03   | 03. string2Dbc            | 字符串转半角      |
| 04   | 04. string2Sbc            | 字符串转全角      |
| 05   | 05. reverseStr            | 倒序字符串        |
| 06   | 06. underLine2Camel       | 下划线转驼峰      |
| 07   | 07. camel2UnderLine       | 驼峰转下划线      |
| 08   | 08. upperFirstLetter      | 首字母大写        |
| 09   | 09. lowerFirstLetter      | 首字母小写        |
| 10   | 10. getPyFirstLetters     | 获取拼音首字母    |
| 11   | 11. allGb2Py              | 多GB2312转拼音    |
| 12   | 12. oneGb2Py              | 单GB2312转拼音    |
| 13   | 13. oneGb2Ascii           | 单GB2312转Ascii码 |
| 14   | 14. getSurnameFirstLetter | 获取多音字首字母  |
| 15   | 15. surname2Py            | 多音字转拼音      |

### *061.随机RandomKit(78)*

| 序号 | 方法                           | 功能                |
|:-----|:-------------------------------|:-------------------|
| 01   | 01. getRandomNumbersAndLetters | 获取随机数字字母组合 |
| 02   | 02. getRandomNumbers           | 获取随机数字组合    |
| 03   | 03. getRandomLetters           | 获取随机字母组合    |
| 04   | 04. getRandomCapitalLetters    | 获取随机大写字母组合 |
| 05   | 05. getRandomLowerCaseLetters  | 获取随机小写字母组合 |
| 06   | 06. getRandom                  | 获取随机字符串或数字 |
| 07   | 07. randomColor                | 随机颜色            |
| 08   | 08. shuffle                    | 打乱                |

### *062.验证ValidationKit(280)*

| 序号 | 方法                    | 功能                             |
|:-----|:------------------------|:--------------------------------|
| 01   | 01. cutStringFromChar   | 从指定字符串开始截取指定长度字符串 |
| 02   | 02. cutString           | 截取指定长度字符串                |
| 03   | 03. getStringLength     | 获取字符串长度                   |
| 04   | 04. getReplaceFirst     | 替换首个匹配子字符串              |
| 05   | 05. getReplaceAll       | 替换所有匹配子字符串              |
| 06   | 06. getSplits           | 按分隔符提取子字符串数组          |
| 07   | 07. getMatches          | 获取所有匹配子字符串              |
| 08   | 08. isMatch             | 是否匹配                         |
| 09   | 09. isDigit             | 是否正负整数                     |
| 10   | 10. isPositiveDigit     | 是否正整数                       |
| 11   | 11. isNegativeDigit     | 是否负整数                       |
| 12   | 12. isNotNegativeDigit  | 是否非正整数                     |
| 13   | 13. isNotPositiveDigit  | 是否非负整数                     |
| 14   | 14. isDecimals          | 是否正负小数                     |
| 15   | 15. isPositiveDecimals  | 是否正小数                       |
| 16   | 16. isNegativeDecimals  | 是否负小数                       |
| 17   | 17. isNotZeroNumeric    | 是否无零数字                     |
| 18   | 18. isNumeric           | 是否数字                         |
| 19   | 19. isBlankLine         | 是否空行                         |
| 20   | 20. isUpLetter          | 是否大写字母                     |
| 21   | 21. isLowLetter         | 是否小写字母                     |
| 22   | 22. isLetter            | 是否字母                         |
| 23   | 23. isChinese           | 是否中文                         |
| 24   | 24. isDoubleByteChar    | 是否双字节字符                   |
| 25   | 25. hasSpecialCharacter | 是否特殊字符                     |
| 26   | 26. isBirthday          | 是否生日                         |
| 27   | 27. isDate              | 是否日期                         |
| 28   | 28. isPay               | 是否金额                         |
| 29   | 29. isBankNo            | 是否银行卡号                     |
| 30   | 30. isQqNo              | 是否QQ号码                       |
| 31   | 31. isVehicleNo         | 是否车牌号码                     |
| 32   | 32. isOneCode           | 是否条码                         |
| 33   | 33. isPostalCode        | 是否邮编                         |
| 34   | 34. isChinaPostalCode   | 是否中国邮编                     |
| 35   | 35. hasChinaPostalCode  | 是否包含中国邮编                 |
| 36   | 36. isEmail             | 是否电子邮箱                     |
| 37   | 37. isSimplePhone       | 是否简单手机号码                 |
| 38   | 38. isExactPhone        | 是否真实手机号码                 |
| 39   | 39. isChinaPlane        | 是否中国手机号码                 |
| 40   | 40. isGlobalPlane       | 是否国家+城市+号码               |
| 41   | 41. isIpAddress         | 是否IP地址                       |
| 42   | 42. isUrl               | 是否Url                          |
| 43   | 43. isUserName          | 是否用户名                       |
| 44   | 44. isRealName          | 是否实名                         |
| 45   | 45. isPassword          | 是否密码                         |
| 46   | 46. isCss               | 是否CSS属性                      |
| 47   | 47. isHtmlTag           | 是否HTML标签                     |
| 48   | 48. isHtmlNotes         | 是否HTML注释                     |
| 49   | 49. isHtmlHyperLink     | 是否HTML超链接                   |
| 50   | 50. isHtmlImage         | 是否HTML图片                     |
| 51   | 51. isHtmlColor         | 是否HTML颜色                     |
| 52   | 52. isHtmlRoute         | 是否HTML文件路径及扩展名          |
| 53   | 53. isUrlInText         | 是否包含Url                      |
| 54   | 54. isHttpOrHttps       | 是否HTTP或HTTPS                  |
| 55   | 55. isIpV4              | 是否IPV4                         |
| 56   | 56. isIpV6              | 是否IPV6                         |
| 57   | 57. isIeVersion         | 是否IE版本                       |
| 58   | 58. isIntStr            | 是否Int字符串                    |
| 59   | 59. isDoubleTwoUp       | 是否两位以上小数                 |
| 60   | 60. isContinuousNo      | 是否连续数字                     |
| 61   | 61. isContinuousWord    | 是否连续字母                     |
| 62   | 62. isPeculiarStr       | 是否特殊字符                     |
| 63   | 63. isNumberLetter      | 是否数字字母                     |
| 64   | 64. isContainChinese    | 是否包含中文                     |
| 65   | 65. lengthChinese       | 中文长度                         |
| 66   | 66. lengthString        | 字符串长度                       |
| 67   | 67. lengthCharStringSub | 包含指定子字符串长度              |
| 68   | 68. uuid                | 通用唯一识别码32字符小写字符串    |

### *063.身份IdKit(396)*

| 序号 | 方法                    | 功能               |
|:-----|:------------------------|:------------------|
| 01   | 01. isIdCard15          | 是否15位身份证号   |
| 02   | 02. isIdCard18          | 是否18位身份证号   |
| 03   | 03. isIdCard            | 是否身份证号       |
| 04   | 04. isIdCardExact       | 是否真实身份证号   |
| 05   | 05. validateCard        | 验证身份证号       |
| 06   | 06. cnCityCode          | 省份代码          |
| 07   | 07. validateIdCard18    | 验证18位身份证号   |
| 08   | 08. validateIdCard15    | 验证15位身份证号   |
| 09   | 09. card15ToCard18      | 15位身份证号转18位 |
| 10   | 10. getProvinceByIdCard | 身份证号获取省份   |
| 11   | 11. getBirthdayByIdCard | 身份证号获取生日   |
| 12   | 12. getBirthByIdCard    | 身份证号获取出生   |
| 13   | 13. getAgeByIdCard      | 身份证号获取年龄   |
| 14   | 14. getYearByIdCard     | 身份证号获取年     |
| 15   | 15. getMonthByIdCard    | 身份证号获取月     |
| 16   | 16. getDateByIdCard     | 身份证号获取日     |
| 17   | 17. getGenderByIdCard   | 身份证号获取省性别 |
| 18   | 18. cnMinority          | 中国民族          |
| 19   | 19. validateIdCard10    | 验证10位身份证号   |
| 20   | 20. hkFirstCode         | 香港身份证首代码   |
| 21   | 21. validateHKCard      | 验证香港身份证号   |
| 22   | 22. twFirstCode         | 台湾身份证首代码   |
| 23   | 23. validateTWCard      | 验证台湾身份证号   |

### *064.银行BankKit(1720)*

| 序号 | 方法                     | 功能              |
|:-----|:-------------------------|:-----------------|
| 01   | 01. getNameOfBank        | 获取银行名称      |
| 02   | 02. checkBankCard        | 验证银行卡号      |
| 03   | 03. getBankCardCheckCode | 获取银行卡号校验码 |

### *065.图码BarQRKit(149)*

| 序号 | 方法                 | 功能       |
|:-----|:---------------------|:----------|
| 01   | 01. decodeFromBitmap | 从图片解码 |
| 02   | 02. builderBarCode   | 构建条形码 |
| 03   | 03. createBarCode    | 创建条形码 |
| 04   | 04. builderQRCode    | 构建二维码 |
| 05   | 05. createQRCode     | 创建二维码 |

>- implementation "com.google.zxing:core:3.4.1"

### *066.软包PackageKit(18)*

| 序号 | 方法                        | 功能           |
|:-----|:----------------------------|:--------------|
| 01   | 01. isExistPackageName      | 是否存在安装包 |
| 02   | 02. getInstalledPackageInfo | 所有安装包信息 |

### *067.编码EncodeKit(115)*

| 序号 | 方法                    | 功能               |
|:-----|:------------------------|:------------------|
| 01   | 01. base64Encode2String | Base64编码字符串   |
| 02   | 02. base64EncodeUrlSafe | Base64编码Url安全 |
| 03   | 03. base64Encode        | Base64编码字节数组 |
| 04   | 04. base64Decode        | Base64解码        |
| 05   | 05. binEncode           | 字符串二进制编码   |
| 06   | 06. binDecode           | 字符串二进制解码   |
| 07   | 07. urlEncode           | Url编码           |
| 08   | 08. urlDecode           | Url解码           |
| 09   | 09. htmlEncode          | HTML编码          |
| 10   | 10. htmlDecode          | HTML解码          |

### *068.加密EncryptKit(510)*

| 序号 | 方法                           | 功能                   |
|:-----|:-------------------------------|:----------------------|
| 01   | 01. xorEncode                  | 异或加密              |
| 02   | 02. xorDecode                  | 异或解密              |
| 03   | 03. checkMD2                   | 检查MD2是否符合        |
| 04   | 04. encryptMD2ToString         | MD2加密字符串          |
| 05   | 05. encryptMD2                 | MD2加密字节数组        |
| 06   | 06. checkMD5                   | 检查MD5是否符合        |
| 07   | 07. encryptMD5ToString         | MD5加密字符串          |
| 08   | 08. encryptMD5                 | MD5加密字节数组        |
| 09   | 09. encryptMD5ToStringWithSalt | MD5加密字符串加盐      |
| 10   | 10. checkSHA1                  | 检查SHA1是否符合       |
| 11   | 11. encryptSHA1ToString        | SHA1加密字符串         |
| 12   | 12. encryptSHA1                | SHA1加密字节数组       |
| 13   | 13. checkSHA224                | 检查SHA224是否符合     |
| 14   | 14. encryptSHA224ToString      | SHA224加密字符串       |
| 15   | 15. encryptSHA224              | SHA224加密字节数组     |
| 16   | 16. checkSHA256                | 检查SHA256是否符合     |
| 17   | 17. encryptSHA256ToString      | SHA256加密字符串       |
| 18   | 18. encryptSHA256              | SHA256加密字节数组     |
| 19   | 19. checkSHA384                | 检查SHA384是否符合     |
| 20   | 20. encryptSHA384ToString      | SHA384加密字符串       |
| 21   | 21. encryptSHA384              | SHA256加密字节数组     |
| 22   | 22. checkSHA512                | 检查SHA512是否符合     |
| 23   | 23. encryptSHA512ToString      | SHA512加密字符串       |
| 24   | 24. encryptSHA512              | SHA512加密字节数组     |
| 25   | 25. checkHmacMD5               | 检查HmacMD5是否符合    |
| 26   | 26. encryptHmacMD5ToString     | HmacMD5加密字符串      |
| 27   | 27. encryptHmacMD5             | HmacMD5加密字节数组    |
| 28   | 28. checkHmacSHA1              | 检查HmacSHA1是否符合   |
| 29   | 29. encryptHmacSHA1ToString    | HmacSHA1加密字符串     |
| 30   | 30. encryptHmacSHA1            | HmacSHA1加密字节数组   |
| 31   | 31. checkHmacSHA224            | 检查HmacSHA224是否符合 |
| 32   | 32. encryptHmacSHA224ToString  | HmacSHA224加密字符串   |
| 33   | 33. encryptHmacSHA224          | HmacSHA224加密字节数组 |
| 34   | 34. checkHmacSHA256            | 检查HmacSHA256是否符合 |
| 35   | 35. encryptHmacSHA256ToString  | HmacSHA256加密字符串   |
| 36   | 36. encryptHmacSHA256          | HmacSHA256加密字节数组 |
| 37   | 37. checkHmacSHA384            | 检查HmacSHA384是否符合 |
| 38   | 38. encryptHmacSHA384ToString  | HmacSHA384加密字符串   |
| 39   | 39. encryptHmacSHA384          | HmacSHA384加密字节数组 |
| 40   | 40. checkHmacSHA512            | 检查HmacSHA512是否符合 |
| 41   | 41. encryptHmacSHA512ToString  | HmacSHA512加密字符串   |
| 42   | 42. encryptHmacSHA512          | HmacSHA512加密字节数组 |
| 43   | 43. checkFile                  | 检查文件加密是否符合   |
| 44   | 44. encryptFile2String         | 文件加密字符串         |
| 45   | 45. encryptFile                | 文件加密字节数组       |
| 46   | 46. initKeyDES                 | 初始DESKey            |
| 47   | 47. encryptDES2Base64          | DES加密Base64字节数组  |
| 48   | 48. encryptDES2HexString       | DES加密十六进制字符串  |
| 49   | 49. encryptDES                 | DES加密字节数组        |
| 50   | 50. decryptDES4Base64          | DES解密Base64字节数组  |
| 51   | 51. decryptDES4HexString       | DES解密十六进制字符串  |
| 52   | 52. decryptDES                 | DES解密字节数组        |
| 53   | 53. initKey3DES                | 初始3DESKey           |
| 54   | 54. encrypt3DES2Base64         | 3DES加密Base64字节数组 |
| 55   | 55. encrypt3DES2HexString      | 3DES加密十六进制字符串 |
| 56   | 56. encrypt3DES                | 3DES加密字节数组       |
| 57   | 57. decrypt3DES4Base64         | 3DES解密Base64字节数组 |
| 58   | 58. decrypt3DES4HexString      | 3DES解密十六进制字符串 |
| 59   | 59. decrypt3DES                | 3DES解密字节数组       |
| 60   | 60. initKeyAES                 | 初始AESKey            |
| 61   | 61. encryptAES2Base64          | AES加密Base64字节数组  |
| 62   | 62. encryptAES2HexString       | AES加密十六进制字符串  |
| 63   | 63. encryptAES                 | AES加密字节数组        |
| 64   | 64. decryptAES4Base64          | AES解密Base64字节数组  |
| 65   | 65. decryptAES4HexString       | AES解密十六进制字符串  |
| 66   | 66. decryptAES                 | AES解密字节数组        |
| 67   | 67. AES_Transformation         | AES变换               |
| 68   | 68. DES_Transformation         | DES变换               |
| 69   | 69. TripleDES_Transformation   | TripleDES变换         |
| 70   | 70. initKeyRSA                 | 初始RSAKey            |
| 71   | 71. encryptRSA2Base64          | RSA加密Base64字节数组  |
| 72   | 72. encryptRSA2HexString       | RSA加密十六进制字符串  |
| 73   | 73. encryptRSA                 | RSA加密字节数组        |
| 74   | 74. decryptRSA4Base64          | RSA解密Base64字节数组  |
| 75   | 75. decryptRSA4HexString       | RSA解密十六进制字符串  |
| 76   | 76. decryptRSA                 | RSA解密字节数组        |

### *069.位算BitKit(35)*

| 序号 | 方法                | 功能     |
|:-----|:--------------------|:--------|
| 01   | 01. checkBitValue   | 检查位值 |
| 02   | 02. getBitValue     | 获取位值 |
| 03   | 03. reverseBitValue | 异或位值 |
| 04   | 04. setBitValue     | 设置位值 |

### *070.压缩CompressKit(92)*

| 序号 | 方法           | 功能         |
|:-----|:---------------|:------------|
| 01   | 01. compress   | 压缩字节数组 |
| 02   | 02. decompress | 解压字节数组 |

### *071.平面PlaneKit(15)*

| 序号 | 方法               | 功能     |
|:-----|:-------------------|:--------|
| 01   | 01. pointToDegrees | 点转角度 |
| 02   | 02. distance       | 两点距离 |
| 03   | 03. checkInRound   | 是否圆中 |

### *072.计算CalculateKit(222)*

| 序号 | 方法                  | 功能         |
|:-----|:----------------------|:------------|
| 01   | 01. add               | 加          |
| 02   | 02. subtract          | 减          |
| 03   | 03. multiply          | 乘          |
| 04   | 04. divide            | 除          |
| 05   | 05. remainder         | 余          |
| 06   | 06. round             | 四舍五入    |
| 07   | 07. compareBigDecimal | 比较小数大小 |
| 08   | 08. formatMoney       | 格式化金额   |
| 09   | 09. adjustDouble      | 调整数字精度 |

### *073.尺寸DensityKit(58)*

| 序号 | 方法                  | 功能                |
|:-----|:----------------------|:-------------------|
| 01   | 01. dip2px            | dip转px            |
| 02   | 02. sp2px             | sp转px             |
| 03   | 03. value2px          | 指定值转px值        |
| 04   | 04. px2dip            | px转dip            |
| 05   | 05. px2sp             | px转sp             |
| 06   | 06. px2value          | px值转指定值        |
| 07   | 07. OnGetSizeListener | 获取尺寸监听器      |
| 08   | 08. forceGetViewSize  | 获取视图尺寸执行监听 |
| 09   | 09. getViewWidth      | 获取视图宽度        |
| 10   | 10. getViewHeight     | 获取视图高度        |
| 11   | 11. measureView       | 测量视图尺寸        |

### *074.坐标CoordinateKit(94)*

| 序号 | 方法             | 功能         |
|:-----|:-----------------|:-------------|
| 01   | 01. bd09ToWgs84  | bd09转wgs84  |
| 02   | 02. bd09ToGcj02  | bd09转gcj02  |
| 03   | 03. gcj02ToWgs84 | gcj02转wgs84 |
| 04   | 04. outOfChina   | 超出中国范围 |
| 05   | 05. transformLng | 转换纬度     |
| 06   | 06. transformLat | 转换经度     |
| 07   | 07. transform    | 转换经纬度   |
| 08   | 08. wgs84ToBd09  | wgs84转bd09  |
| 09   | 09. wgs84ToGcj02 | wgs84转gcj02 |
| 10   | 10. gcj02ToBd09  | gcj02转bd09  |

### *075.转换ConvertKit(520)*

| 序号 | 方法                        | 功能                      |
|:-----|:----------------------------|:-------------------------|
| 01   | 01. BYTE                    | 单字节                   |
| 02   | 02. KB                      | 千字节                   |
| 03   | 03. MB                      | 兆字节                   |
| 04   | 04. GB                      | 吉字节                   |
| 05   | 05. MemoryUnit              | 存储单位                 |
| 06   | 06. memorySize2ByteSize     | 尺寸单位转字节            |
| 07   | 07. byteSize2MemorySize     | 字节转尺寸单位            |
| 08   | 08. byteSize2MemorySizeFit  | 字节转尺寸单位友好显示    |
| 09   | 09. byteSize2MemorySizeDesc | 字节转尺寸单位描述        |
| 10   | 10. MSEC                    | 毫                       |
| 11   | 11. SEC                     | 秒                       |
| 12   | 12. MIN                     | 分                       |
| 13   | 13. HOUR                    | 时                       |
| 14   | 14. DAY                     | 日                       |
| 15   | 15. TimeUnit                | 时间单位                 |
| 16   | 16. timeSpan2Millis         | 时间单位转毫秒            |
| 17   | 17. millis2TimeSpan         | 毫秒转时间单位            |
| 18   | 18. millis2TimeSpanFit      | 毫秒转时间单位友好显示    |
| 19   | 19. millis2TimeSpanFitByNow | 毫秒转时间单位友好显示现在 |
| 20   | 20. timeZonePhone           | 手机时区                 |
| 21   | 21. timeZoneBeijing         | 北京时区                 |
| 22   | 22. phoneTime2BeijingTime   | 手机时间转北京时间        |
| 23   | 23. beijingTime2PhoneTime   | 北京时间转手机时间        |
| 24   | 24. changeTimeZone          | 改变时区                 |
| 25   | 25. millis2Date             | 毫秒转日期                |
| 26   | 26. date2Millis             | 日期转毫秒                |
| 27   | 27. millis2String           | 毫秒转字符串              |
| 28   | 28. string2Millis           | 字符串转毫秒              |
| 29   | 29. date2String             | 日期转字符串              |
| 30   | 30. string2Date             | 字符串转日期              |
| 31   | 31. bytes2Chars             | 字节数组转字符数组        |
| 32   | 32. chars2Bytes             | 字符数组转字节数组        |
| 33   | 33. bytes2Bits              | 字节数组转位字符串        |
| 34   | 34. bits2Bytes              | 位字符串转字节数组        |
| 35   | 35. hexDigitsLower          | 十六进制数字小写          |
| 36   | 36. hexDigitsUpper          | 十六进制数字大写          |
| 37   | 37. bytes2HexStringIsUpper  | 字节数组转十六进制大写    |
| 38   | 38. bytes2HexString         | 字节数组转十六进制        |
| 39   | 39. hexString2Bytes         | 十六进制转字节数组        |
| 40   | 40. hex2Int                 | 十六进制转十进制Int       |
| 41   | 41. outputStream2String     | 输出流转字符串            |
| 42   | 42. outputStream2Bytes      | 输出流转字节数组          |
| 43   | 43. string2OutputStream     | 字符串转输出流            |
| 44   | 44. bytes2OutputStream      | 字节数组转输出流          |
| 45   | 45. inputStream2String      | 输入流转字符串            |
| 46   | 46. inputStream2Bytes       | 输入流转字节数组          |
| 47   | 47. bytes2InputStream       | 字节数组转输入流          |
| 48   | 48. input2OutputStream      | 输入流转输出流            |
| 49   | 49. output2InputStream      | 输出流转输入流            |
| 50   | 50. string2InputStream      | 字符串转输入流            |
| 51   | 51. drawable2Bytes          | 可绘制对象转字节数组      |
| 52   | 52. bitmap2Bytes            | 位图转字节数组            |
| 53   | 53. drawable2Bitmap         | 可绘制对象转位图          |
| 54   | 54. bytes2Drawable          | 字节数组转可绘制对象      |
| 55   | 55. bitmap2Drawable         | 位图转可绘制对象          |
| 56   | 56. bytes2Bitmap            | 字节数组转位图            |
| 57   | 57. nullOfString            | 字符串去空                |
| 58   | 58. saveDecimals            | 保存指定位数小数          |
| 59   | 59. int2Bytes               | Int转字节数组             |
| 60   | 60. bytes2Int               | 字节数组转Int             |
| 61   | 61. ip2Long                 | IP转Long                 |
| 62   | 62. string2Byte             | 字符串转Byte             |
| 63   | 63. string2Short            | 字符串转Short            |
| 64   | 64. string2Int              | 字符串转Int              |
| 65   | 65. string2Long             | 字符串转Long             |
| 66   | 66. string2Float            | 字符串转Float            |
| 67   | 67. string2Double           | 字符串转Double           |
| 68   | 68. string2Boolean          | 字符串转Boolean          |
| 69   | 69. double2Int              | Double转Int              |
| 70   | 70. double2Long             | Double转Long             |
| 71   | 71. long2Int                | Long转Int                |
| 72   | 72. long2Double             | Long转Double             |
| 73   | 73. char2Int                | Char转Int                |

### *076.迁移MigrationKit(96)*

| 序号 | 方法        | 功能               |
|:-----|:------------|:------------------|
| 01   | 01. isDebug | 是否调试           |
| 02   | 02. migrate | 迁移SQLiteDatabase |

>- implementation "org.greenrobot:greendao:3.3.0"

### *077.数库DbKit(35)*

| 序号 | 方法                | 功能              |
|:-----|:--------------------|:-----------------|
| 01   | 01. sqlInjection    | 防止数据注入      |
| 02   | 02. exportDb2SdCard | 导出数据库到存储卡 |

### *078.文件FileKit(914)*

| 序号 | 方法                          | 功能                                          |
|:-----|:------------------------------|:---------------------------------------------|
| 01   | 01. zip                       | 压缩流                                        |
| 02   | 02. unzip                     | 解压流                                        |
| 03   | 03. bitmap2JpegFile           | 位图存jpeg文件                                |
| 04   | 04. bitmap2PngFile            | 位图存png文件                                 |
| 05   | 05. encodeFile2Base64String   | 文件转Base64编码字符串                         |
| 06   | 06. decoderBase64String2File  | Base64编码字符串转文件                         |
| 07   | 07. pathCacheImageAppIcon     | 应用缓存菜单按钮图片路径                       |
| 08   | 08. pathCacheImageMainGallery | 应用缓存主页图片路径                           |
| 09   | 09. pathCacheImageBrowse      | 应用缓存浏览图片路径                           |
| 10   | 10. pathCacheImageChooseHead  | 应用缓存用户头像路径                           |
| 11   | 11. pathCacheImage            | 应用缓存其他图片路径                           |
| 12   | 12. getCacheSdCard            | 获取SD卡缓存文件                              |
| 13   | 13. getFileByPath             | 通过路径获取文件                               |
| 14   | 14. isExistsTimestamp         | 是否文件已经过期                               |
| 15   | 15. isExistDirSdCard          | 是否存在SD卡目录                              |
| 16   | 16. isExistsDir               | 是否存在目录                                  |
| 17   | 17. isExistFileSdCard         | 是否存在SD卡目录                              |
| 18   | 18. isExistsFile              | 是否存在文件                                  |
| 19   | 19. pathRootData              | 数据根路径含SD卡                              |
| 20   | 20. createRootData            | 创建数据根路径含SD卡                           |
| 21   | 21. pathAppData               | 应用数据路径含SD卡                             |
| 22   | 22. createAppData             | 创建应用数据路径含SD卡                         |
| 23   | 23. pathAppFiles              | 应用文件路径含SD卡                             |
| 24   | 24. createAppFiles            | 创建应用文件路径含SD卡                         |
| 25   | 25. pathAppCache              | 应用缓存路径含SD卡                             |
| 26   | 26. createAppCache            | 创建应用缓存路径含SD卡                         |
| 27   | 27. createDirSdCard           | 创建SD卡目录路径                              |
| 28   | 28. createDirNone             | 创建目录如果不存在                             |
| 29   | 29. createDirNew              | 创建新目录                                    |
| 30   | 30. createFileSdCard          | 创建SD卡文件路径                              |
| 31   | 31. createFileNone            | 创建文件如果不存在                             |
| 32   | 32. createFileNew             | 创建新文件                                    |
| 33   | 33. cutDir                    | 剪切目录                                      |
| 34   | 34. copyDir                   | 复制目录                                      |
| 35   | 35. cutFile                   | 剪切文件                                      |
| 36   | 36. copyFile                  | 复制文件                                      |
| 37   | 37. cut                       | 移动文件到                                    |
| 38   | 38. copy                      | 复制文件到                                    |
| 39   | 39. mergeFiles                | 合并文件                                      |
| 40   | 40. shareFile                 | 分享文件                                      |
| 41   | 41. downloadFile              | 下载文件                                      |
| 42   | 42. DownloadService           | 下载服务                                      |
| 43   | 43. upgradeApp                | 更新应用                                      |
| 44   | 44. delete                    | 删除文件或目录                                |
| 45   | 45. deleteDir                 | 删除目录                                      |
| 46   | 46. deleteFile                | 删除单文件                                    |
| 47   | 47. deleteFiles               | 删除多文件                                    |
| 48   | 48. deleteFilesByFilter       | 删除文件带过滤器                               |
| 49   | 49. listFilesInDirWithFilter  | 列出目录中文件带过滤器                         |
| 50   | 50. getFileLastModified       | 获取文件最后修改时间                           |
| 51   | 51. getFileCharset            | 获取文件字符集                                |
| 52   | 52. getFileLines              | 获取文件行数                                  |
| 53   | 53. allSizeInternal           | 数据路径容量                                  |
| 54   | 54. allSizeExternal           | SD卡容量                                      |
| 55   | 55. getAllSize                | 路径容量                                      |
| 56   | 56. availableSizeInternal     | 数据路径可用容量                               |
| 57   | 57. availableSizeExternal     | SD卡可用容量                                  |
| 58   | 58. getAvailableSize          | 路径可用容量                                  |
| 59   | 59. freeSizeInternal          | 数据路径空闲容量                               |
| 60   | 60. freeSizeExternal          | SD卡空闲容量                                  |
| 61   | 61. getFreeSize               | 路径空闲容量                                  |
| 62   | 62. getDirsSizeFit            | 获取系列目录容量友好显示                       |
| 63   | 63. getDirsSize               | 获取系列目录容量                               |
| 64   | 64. getDirSizeFit             | 获取目录容量友好显示                           |
| 65   | 65. getDirSize                | 获取目录容量                                  |
| 66   | 66. getFileSizeFit            | 获取文件大小友好显示                           |
| 67   | 67. getFileSize               | 获取文件大小                                  |
| 68   | 68. getDirName                | 获取目录名                                    |
| 69   | 69. getFileName               | 获取文件名                                    |
| 70   | 70. getFileNoExtension        | 获取文件名不含扩展名                           |
| 71   | 71. getFileExtension          | 获取文件扩展名                                |
| 72   | 72. getFileIntent             | 获取文件意图                                  |
| 73   | 73. getNativeM3u8             | 获取m3u8                                      |
| 74   | 74. getFileUri                | 获取文件Uri                                   |
| 75   | 75. getImageContentUri        | 获取图片文件Uri                               |
| 76   | 76. getFileFromUri            | 用Uri获取文件                                 |
| 77   | 77. getPathFromUri            | 用Uri获取路径                                 |
| 78   | 78. isExternalStorageDocument | 是否com.android.externalstorage.documents     |
| 79   | 79. isDownloadsDocument       | 是否com.android.providers.downloads.documents |
| 80   | 80. isMediaDocument           | 是否com.android.providers.media.documents     |
| 81   | 81. isGooglePhotosUri         | 是否com.google.android.apps.photos.content    |
| 82   | 82. getDataColumn             | 获取数据列                                    |
| 83   | 83. rename                    | 文件重命名                                    |

### *079.存取FileIoKit(366)*

| 序号 | 方法                            | 功能                    |
|:-----|:--------------------------------|:-----------------------|
| 01   | 01. writeFileFromIS             | 输入流写到文件          |
| 02   | 02. writeFileFromString         | 字符串写到文件          |
| 03   | 03. writeAppFileFormBytes       | 字节数组写到文件        |
| 04   | 04. writeFileFromBytesByStream  | 字节数组流方式写到文件   |
| 05   | 05. writeFileFromBytesByChannel | 字节数组通道方式写到文件 |
| 06   | 06. writeFileFromBytesByMap     | 字节数组映射方式写到文件 |
| 07   | 07. readFile2List               | 读文件到列表            |
| 08   | 08. readFile2StringByLine       | 按行读文件到字符串      |
| 09   | 09. readFile2StringByBytes      | 读文件到字符串          |
| 10   | 10. readFile2BytesByStream      | 流方式读文件到字节数组   |
| 11   | 11. readFile2BytesByChannel     | 通道方式读文件到字节数组 |
| 12   | 12. readFile2BytesByMap         | 映射方式读文件到字节数组 |

### *080.压制ZipKit(336)*

| 序号 | 方法                  | 功能              |
|:-----|:----------------------|:-----------------|
| 01   | 01. zipFiles          | 批量压缩文件      |
| 02   | 02. zipFile           | 压缩文件          |
| 03   | 03. ZipListener       | 压缩监听器        |
| 04   | 04. isStopZip         | 是否停止压缩      |
| 05   | 05. fileToZip         | 文件压缩到压缩文件 |
| 06   | 06. unzipSelectedFile | 解压选中文件      |
| 07   | 07. unzipOneFile      | 压缩文件解压到目录 |
| 08   | 08. getFilesPath      | 获取压缩文件路径   |
| 09   | 09. getComments       | 获取注释列表      |
| 10   | 10. getEntries        | 获取文件对象列表   |

### *081.密压ZipPlusKit(320)*

| 序号 | 方法                        | 功能                |
|:-----|:----------------------------|:-------------------|
| 01   | 01. zipEncrypt              | 加密压缩            |
| 02   | 02. zipEncryptRargo         | 加密分卷压缩        |
| 03   | 03. zipInfo                 | 压缩文件尺寸信息    |
| 04   | 04. unzipFilesByKeyword     | 批量解压加密压缩文件 |
| 05   | 05. unzipFileByKeyword      | 解压加密压缩文件    |
| 06   | 06. unzipFile               | 加压压缩文件        |
| 07   | 07. unzipFileWithMonitor    | 解压压缩文件带监视器 |
| 08   | 08. removeDirFromZipArchive | 从压缩文件移除目录   |

>- implementation "net.lingala.zip4j:zip4j:2.10.0"

### *082.打开OpenKit(102)*

| 序号 | 方法                           | 功能            |
|:-----|:-------------------------------|:---------------|
| 01   | 01. openInputStreamByUrlString | 从url打开输入流 |
| 02   | 02. openWebSite                | 打开网站       |
| 03   | 03. openImage                  | 打开图片       |
| 04   | 04. openVideo                  | 打开视频       |
| 05   | 05. openPdfFile                | 打开PDF        |
| 06   | 06. openWordFile               | 打开Word       |
| 07   | 07. openOfficeFileByWps        | 打开WPS        |

### *083.图像ImageKit(1298)*

| 序号 | 方法                        | 功能                      |
|:-----|:----------------------------|:-------------------------|
| 01   | 01. makeKey                 | 网址转字节数组            |
| 02   | 02. isSameKey               | 是否相同                 |
| 03   | 03. crc64Long               | 字节数组crc64Long        |
| 04   | 04. getColorByInt           | 通过Int获取颜色           |
| 05   | 05. getColorHexString       | 通过十六进制字符串获取颜色 |
| 06   | 06. getAlphaPercent         | 获取透明度百分比          |
| 07   | 07. setColorAlphaByInt      | 设置颜色的Int透明度       |
| 08   | 08. setColorAlphaByFloat    | 设置颜色的Float透明度     |
| 09   | 09. getColorLightness       | 获取颜色亮度              |
| 10   | 10. setColorLightness       | 设置颜色亮度              |
| 11   | 11. getDrawableFromMap      | 获取缓存可绘制对象        |
| 12   | 12. getBitmapFromMap        | 获取缓存图片              |
| 13   | 13. recycleBitmaps          | 清空缓存图片              |
| 14   | 14. getBitmapFromLocalOrNet | 从本地或网络获取图片      |
| 15   | 15. getPicPathFromUri       | 从Uri获取图片路径         |
| 16   | 16. getBitmap               | 从各处获取图片            |
| 17   | 17. drawNinePatch           | 绘制NinePatch            |
| 18   | 18. drawColor               | 绘制颜色                 |
| 19   | 19. getDropShadow           | 图片阴影                 |
| 20   | 20. toClip                  | 图片裁剪                 |
| 21   | 21. toSkew                  | 图片倾斜                 |
| 22   | 22. toRotate                | 图片旋转                 |
| 23   | 23. getRotateDegree         | 获取旋转角度              |
| 24   | 24. toRound                 | 图片圆形                 |
| 25   | 25. toRoundCorner           | 图片圆角                 |
| 26   | 26. addBorder               | 图片边框                 |
| 27   | 27. addReflection           | 图片倒影                 |
| 28   | 28. createTextImage         | 图片添加文字              |
| 29   | 29. addTextWatermark        | 图片添加文字水印          |
| 30   | 30. addImageWatermark       | 图片添加图片水印          |
| 31   | 31. toAlpha                 | 图片透明                 |
| 32   | 32. setAlpha                | 图片设置透明度            |
| 33   | 33. toGray                  | 图片灰色                 |
| 34   | 34. toGrey                  | 图片变灰                 |
| 35   | 35. grayMasking             | 图片光晕                 |
| 36   | 36. fastBlur                | 图片快速模糊              |
| 37   | 37. renderScriptBlur        | 图片Render模糊           |
| 38   | 38. stackBlur               | 图片Stack模糊            |
| 39   | 39. BoxBlurFilter           | 图片高斯模糊              |
| 40   | 40. blur                    | 图片模糊                 |
| 41   | 41. clamp                   | 限制值范围                |
| 42   | 42. saveImage               | 保存图片                 |
| 43   | 43. isImage                 | 是否图片                 |
| 44   | 44. getImageType            | 获取图片类型              |
| 45   | 45. getImageFormat          | 获取图片格式              |
| 46   | 46. getThumbVideo           | 获取视频缩略图            |
| 47   | 47. getThumbBitmap          | 获取图片缩略图            |
| 48   | 48. compressByScale         | 图片缩放压缩              |
| 49   | 49. compressByQuality       | 图片质量压缩              |
| 50   | 50. compressBySampleSize    | 图片Sample压缩           |
| 51   | 51. calculateInSampleSize   | 计算Sample               |
| 52   | 52. getBitmapSize           | 获取图片尺寸              |

### *084.照片PhotoKit(126)*

| 序号 | 方法                 | 功能         |
|:-----|:---------------------|:------------|
| 01   | 01. openGalleryMedia | 打开媒体界面 |
| 02   | 02. openGalleryImage | 打开图片界面 |
| 03   | 03. openCameraImage  | 打开拍照界面 |
| 04   | 04. cropImage        | 裁剪图片    |
| 05   | 05. getCropIntent    | 获取裁剪意图 |

### *085.图片PictureKit(43)*

| 序号 | 方法                    | 功能            |
|:-----|:------------------------|:----------------|
| 01   | 01. getChoosedImagePath | 获取选中图片路径 |
| 02   | 02. getChoosedImage     | 获取选中图片    |
| 03   | 03. getTakePictureFile  | 获取拍照后照片   |

### *086.动画AnimationKit(209)*

| 序号 | 方法                       | 功能           |
|:-----|:---------------------------|:--------------|
| 01   | 01. isRunning              | 动画是否正运行 |
| 02   | 02. isStarted              | 动画是否已开始 |
| 03   | 03. start                  | 开始动画      |
| 04   | 04. stop                   | 停止动画      |
| 05   | 05. popupAppear            | 弹出显示      |
| 06   | 06. popupDisappear         | 弹出隐藏      |
| 07   | 07. animationCardFlip      | 翻转动画      |
| 08   | 08. animationZoomIn        | 放大动画      |
| 09   | 09. animationZoomOut       | 缩小动画      |
| 10   | 10. ScaleUpDown            | 从下到上动画   |
| 11   | 11. animateHeight          | 高度动画      |
| 12   | 12. OnDoIntListener        | 动画更新监听器 |
| 13   | 13. animationColorGradient | 渐变色动画    |
| 14   | 14. addTouchDark           | 视图深色模式   |
| 15   | 15. addTouchLight          | 视图浅色模式   |

### *087.信息ExifKit(41)*

| 序号 | 方法                    | 功能           |
|:-----|:------------------------|:--------------|
| 01   | 01. writeLatLonIntoJpeg | 经纬度写到图片 |

### *088.属性PropertiesKit(40)*

| 序号 | 方法                 | 功能         |
|:-----|:---------------------|:------------|
| 01   | 01. propertiesAll    | 所有属性    |
| 02   | 02. getPropertyByKey | 获取属性    |
| 03   | 03. init             | 初始属性文件 |

### *089.记录LoggerKit(84)*

| 序号 | 方法                    | 功能                |
|:-----|:------------------------|:-------------------|
| 01   | 01. loggerTag           | 记录tag             |
| 02   | 02. getLogger           | 获取记录器          |
| 03   | 03. verbose             | 冗余                |
| 04   | 04. debug               | 调试                |
| 05   | 05. info                | 信息                |
| 06   | 06. warn                | 警告                |
| 07   | 07. error               | 错误                |
| 08   | 08. wtf                 | 断言                |
| 09   | 09. getStackTraceString | 获取日志栈跟踪字符串 |

### *090.日志LogKit(681)*

| 序号 | 方法           | 功能             |
|:-----|:---------------|:----------------|
| 01   | 01. IFormatter | 格式器接口       |
| 02   | 02. config     | 配置参数         |
| 03   | 03. v          | VERBOSE记录      |
| 04   | 04. d          | DEBUG记录        |
| 05   | 05. i          | INFO记录         |
| 06   | 06. w          | WARN记录         |
| 07   | 07. e          | ERROR记录        |
| 08   | 08. a          | ASSERT记录       |
| 09   | 09. vTag       | VERBOSE记录带tag |
| 10   | 10. dTag       | DEBUG记录带tag   |
| 11   | 11. iTag       | INFO记录带tag    |
| 12   | 12. wTag       | WARN记录带tag    |
| 13   | 13. eTag       | ERROR记录带tag   |
| 14   | 14. aTag       | ASSERT记录带tag  |
| 15   | 15. file       | file记录         |
| 16   | 16. json       | json记录         |
| 17   | 17. xml        | xml记录          |

>- implementation "com.google.code.gson:gson:2.9.0"

### *091.数据DataKit(146)*

| 序号 | 方法                    | 功能                  |
|:-----|:------------------------|:---------------------|
| 01   | 01. toDoubleWithDefault | 对象转Double带默认值  |
| 02   | 02. isNumberByAny       | 是否数字              |
| 03   | 03. isIntegerByAny      | 是否整数              |
| 04   | 04. isDecimalByAny      | 是否小数              |
| 05   | 05. isNullOrEmpty       | 是否null或空字符串    |
| 06   | 06. compileRegex        | 获取字符串匹配模板    |
| 07   | 07. toIntWithDefault    | 对象转Int带默认值     |
| 08   | 08. toLongWithDefault   | 对象转Long带默认值    |
| 09   | 09. toStringWithDefault | 对象转字符串带默认值   |
| 10   | 10. isTrue              | 是否true             |
| 11   | 11. isFalse             | 是否false            |
| 12   | 12. isBoolean           | 是否布尔              |
| 13   | 13. fitDistance         | 距离友好显示          |
| 14   | 14. hideMobileMid       | 隐藏手机号码中间四位   |
| 15   | 15. hideId18Mid         | 隐藏身份证号码中间十位 |
| 16   | 16. hideBankMid         | 隐藏银行卡号中间      |
| 17   | 17. hideBankLeft        | 隐藏银行卡号左边      |
| 18   | 18. formatDefault       | 默认数字格式化        |
| 19   | 19. formatZero          | 整数带零位小数        |
| 20   | 20. formatTwo           | 整数带两位小数        |
| 21   | 21. formatPercent       | 整数带两位小数百分    |
| 22   | 22. formatE             | 整数带两位小数科计    |
| 23   | 23. formatSplit         | 逗号分隔每三位数      |
| 24   | 24. formatAdd           | 十位整数带两位小数    |
| 25   | 25. formatAmount        | 格式化数字            |
| 26   | 26. string2Ints         | 字符串转Int数字       |
| 27   | 27. getPercentValue     | 获取百分比值          |
| 28   | 28. getRoundUp          | 获取四舍五入值        |

### *092.解析JsonKit(273)*

| 序号 | 方法                     | 功能                   |
|:-----|:-------------------------|:----------------------|
| 01   | 01. getInt               | 从Json获取Int          |
| 02   | 02. getLong              | 从Json获取Long         |
| 03   | 03. getDouble            | 从Json获取Double       |
| 04   | 04. getBoolean           | 从Json获取Boolean      |
| 05   | 05. getString            | 从Json获取String       |
| 06   | 06. getJSONObject        | 从Json获取JsonObject   |
| 07   | 07. getJSONArray         | 从Json获取JsonArray    |
| 08   | 08. string2JSONObject    | Json字符串转JsonObject |
| 09   | 09. any2JsonArray        | 数组转JsonArray        |
| 10   | 10. collection2JsonArray | 集合转JsonArray        |
| 11   | 11. map2JsonObject       | 映射转JsonArray        |
| 12   | 12. any2json             | 对象转Json字符串       |
| 13   | 13. string2json          | 字符串转Json字符串     |
| 14   | 14. array2json           | 数组转Json字符串       |
| 15   | 15. list2json            | 列表转Json字符串       |
| 16   | 16. set2json             | 集合转Json字符串       |
| 17   | 17. map2json             | 映射转Json字符串       |
| 18   | 18. formatJson           | 格式化Json             |

### *093.处理GsonKit(50)*

| 序号 | 方法             | 功能                   |
|:-----|:-----------------|:----------------------|
| 01   | 01. gson         | 获取Gson              |
| 02   | 02. gsonNoNulls  | 获取无空Gson          |
| 03   | 03. deepClone    | 深度克隆              |
| 04   | 04. toJson       | 把对象转Json          |
| 05   | 05. fromJson     | 从Json或Reader获取对象 |
| 06   | 06. getArrayType | 获取数组类型           |
| 07   | 07. getListType  | 获取列表类型           |
| 08   | 08. getSetType   | 获取集合类型           |
| 09   | 09. getMapType   | 获取映射类型           |
| 10   | 10. getType      | 获取对象类型           |

>- implementation "com.google.code.gson:gson:2.9.0"

### *094.标记XmlParseKit(138)*

| 序号 | 方法             | 功能            |
|:-----|:-----------------|:---------------|
| 01   | 01. getXmlList   | 解析xml获取列表 |
| 02   | 02. getXmlObject | 解析xml获取对象 |

### *095.路径PathKit(177)*

| 序号 | 方法                             | 功能                                                          |
|:-----|:---------------------------------|:-------------------------------------------------------------|
| 01   | 01. pathRoot                     | /system                                                      |
| 02   | 02. pathData                     | /data                                                        |
| 03   | 03. pathDownloadCache            | /cache                                                       |
| 04   | 04. pathInternalAppData          | /data/data/package                                           |
| 05   | 05. pathInternalAppCache         | /data/data/package/cache                                     |
| 06   | 06. pathInternalAppCodeCache     | /data/data/package/code_cache                                |
| 07   | 07. pathInternalAppFiles         | /data/data/package/files                                     |
| 08   | 08. pathInternalAppNoBackupFiles | /data/data/package/no_backup                                 |
| 09   | 09. pathInternalAppSp            | /data/data/package/shared_prefs                              |
| 10   | 10. pathInternalAppDbs           | /data/data/package/databases                                 |
| 11   | 11. getPathInternalAppDb         | /data/data/package/databases/name                            |
| 12   | 12. pathExternal                 | /storage/emulated/0                                          |
| 13   | 13. pathExternalMusic            | /storage/emulated/0/Music                                    |
| 14   | 14. pathExternalPodcasts         | /storage/emulated/0/Podcasts                                 |
| 15   | 15. pathExternalRingtones        | /storage/emulated/0/Ringtones                                |
| 16   | 16. pathExternalAlarms           | /storage/emulated/0/Alarms                                   |
| 17   | 17. pathExternalNotifications    | /storage/emulated/0/Notifications                            |
| 18   | 18. pathExternalPictures         | /storage/emulated/0/Pictures                                 |
| 19   | 19. pathExternalMovies           | /storage/emulated/0/Movies                                   |
| 20   | 20. pathExternalDownload         | /storage/emulated/0/Download                                 |
| 21   | 21. pathExternalDcim             | /storage/emulated/0/DCIM                                     |
| 22   | 22. pathExternalDocuments        | /storage/emulated/0/Documents                                |
| 23   | 23. pathExternalAppData          | /storage/emulated/0/Android/data/package                     |
| 24   | 24. pathExternalAppCache         | /storage/emulated/0/Android/data/package/cache               |
| 25   | 25. pathExternalAppFiles         | /storage/emulated/0/Android/data/package/files               |
| 26   | 26. pathExternalAppMusic         | /storage/emulated/0/Android/data/package/files/Music         |
| 27   | 27. pathExternalAppPodcasts      | /storage/emulated/0/Android/data/package/files/Podcasts      |
| 28   | 28. pathExternalAppRingtones     | /storage/emulated/0/Android/data/package/files/Ringtones     |
| 29   | 29. pathExternalAppAlarms        | /storage/emulated/0/Android/data/package/files/Alarms        |
| 30   | 30. pathExternalAppNotifications | /storage/emulated/0/Android/data/package/files/Notifications |
| 31   | 31. pathExternalAppPictures      | /storage/emulated/0/Android/data/package/files/Pictures      |
| 32   | 32. pathExternalAppMovies        | /storage/emulated/0/Android/data/package/files/Movies        |
| 33   | 33. pathExternalAppDownload      | /storage/emulated/0/Android/data/package/files/Download      |
| 34   | 34. pathExternalAppDcim          | /storage/emulated/0/Android/data/package/files/DCIM          |
| 35   | 35. pathExternalAppDocuments     | /storage/emulated/0/Android/data/package/files/Documents     |
| 36   | 36. pathExternalAppObb           | /storage/emulated/0/Android/obb/package                      |

### *096.资源ResourceKit(251)*

| 序号 | 方法                      | 功能                    |
|:-----|:--------------------------|:-----------------------|
| 01   | 01. ID                    | ID                     |
| 02   | 02. LAYOUT                | 布局                   |
| 03   | 03. MENU                  | 目录                   |
| 04   | 04. DRAWABLE              | 可绘制对象              |
| 05   | 05. MIPMAP                | 图片                   |
| 06   | 06. ANIM                  | 动画                   |
| 07   | 07. RAW                   | 原始                   |
| 08   | 08. STYLE                 | 风格                   |
| 09   | 09. STYLEABLE             | 自制控件                |
| 10   | 10. DIMEN                 | 尺寸                   |
| 11   | 11. COLOR                 | 颜色                   |
| 12   | 12. INTEGER               | 数字                   |
| 13   | 13. BOOL                  | 布尔                   |
| 14   | 14. STRING                | 字符串                  |
| 15   | 15. ATTR                  | 数组                   |
| 16   | 16. getResIdByName        | 通过名字获取资源ID      |
| 17   | 17. getColorById          | 通过ID获取颜色          |
| 18   | 18. getDrawableById       | 通过ID获取可绘制对象    |
| 19   | 19. getStringById         | 通过ID获取字符串        |
| 20   | 20. getStringWithArgsById | 通过ID获取系列格式字符串 |
| 21   | 21. getStringArrayById    | 通过ID获取字符串数组    |
| 22   | 22. getResourceId         | 通过ID获取资源          |
| 23   | 23. unZipAssets           | 解压到Assets            |
| 24   | 24. unZipRaw              | 解压到Raw               |
| 25   | 25. copyFileByAssets      | 复制Assets到文件        |
| 26   | 26. copyFileByRaw         | 复制Raw到文件           |
| 27   | 27. getBytesByAssets      | 通过Assets获取字节数组  |
| 28   | 28. getBytesByRaw         | 通过Raw获取字节数组     |
| 29   | 29. getStringByAssets     | 通过Assets获取字符串    |
| 30   | 30. getStringByRaw        | 通过Raw获取字符串       |
| 31   | 31. getListByAssets       | 通过Assets获取列表      |
| 32   | 32. getListByRaw          | 通过Raw获取列表         |

### *097.共享PreferenceKit(109)*

| 序号 | 方法             | 功能           |
|:-----|:-----------------|:--------------|
| 01   | 01. fileName     | 名称          |
| 02   | 02. all          | 所有值        |
| 03   | 03. putAll       | 批量放入值    |
| 04   | 04. getImage     | 提取图片      |
| 05   | 05. putImage     | 放入图片      |
| 06   | 06. get          | 提取值        |
| 07   | 07. put          | 放入值        |
| 08   | 08. getStringSet | 提取字符串集合 |
| 09   | 09. putStringSet | 放入字符串集合 |
| 10   | 10. contains     | 是否包含值    |
| 11   | 11. remove       | 移除值        |
| 12   | 12. clear        | 清空值        |

### *098.主题ThemeKit(157)*

| 序号 | 方法                                | 功能                            |
|:-----|:------------------------------------|:--------------------------------|
| 01   | 01. resolveColor                    | 解析颜色                        |
| 02   | 02. resolveDimension                | 解析尺寸                        |
| 03   | 03. resolveBoolean                  | 解析布尔                        |
| 04   | 04. resolveDrawable                 | 解析可绘制对象                   |
| 05   | 05. resolveString                   | 解析字串                        |
| 06   | 06. resolveFloat                    | 解析浮点                        |
| 07   | 07. resolveInt                      | 解析整型                        |
| 08   | 08. getColorFromAttrRes             | 从属性获取颜色                   |
| 09   | 09. resolveActionTextColorStateList | 从颜色数组获取ColorStateList     |
| 10   | 10. getActionTextColorStateList     | 用颜色ID获取ColorStateList跨版本 |
| 11   | 11. getActionTextStateList          | 用颜色ID获取ColorStateList      |
| 12   | 12. getColorArray                   | 获取颜色数组                    |
| 13   | 13. isNightMode                     | 是否深色模式                    |

### *099.消息UiMessageKit(167)*

| 序号 | 方法                  | 功能           |
|:-----|:----------------------|:--------------|
| 01   | 01. send              | 发送UI消息    |
| 02   | 02. UiMessage         | UI消息        |
| 03   | 03. UiMessageCallback | UI消息回调    |
| 04   | 04. addListener       | 添加UI消息回调 |
| 05   | 05. removeListener    | 移除UI消息回调 |

### *100.日期DateKit(514)*

| 序号 | 方法                         | 功能                        |
|:-----|:-----------------------------|:---------------------------|
| 001  | 01. timeZoneNameCurrent      | 当前时区                   |
| 002  | 02. createGmtOffsetString    | 创建GMT时区字符串           |
| 003  | 03. sdfDateExcel             | excel格式日期              |
| 004  | 04. sdfDateByFullFileName    | 文件名全格式日期时间        |
| 005  | 05. sdfYear                  | 年格式                     |
| 006  | 06. sdfMonth                 | 月格式                     |
| 007  | 07. sdfDay                   | 日格式                     |
| 008  | 08. sdfHour                  | 时格式                     |
| 009  | 09. sdfMinute                | 分格式                     |
| 010  | 10. sdfSecond                | 秒格式                     |
| 011  | 11. sdfMillisecond           | 毫格式                     |
| 012  | 12. sdfTimeCn                | 中文时间格式                |
| 013  | 13. sdfDateCn                | 中文日期格式                |
| 014  | 14. sdfDateByHourCn          | 中文日期时格式              |
| 015  | 15. sdfDateByMinuteCn        | 中文日期时分格式            |
| 016  | 16. sdfDateByFullCn          | 中文日期时间格式            |
| 017  | 17. sdfDateByAllCn           | 中文日期时间毫格式          |
| 018  | 18. sdfTimeEn                | 英文时间格式                |
| 019  | 19. sdfDateEn                | 英文日期格式                |
| 020  | 20. sdfDateByHourEn          | 英文日期时格式              |
| 021  | 21. sdfDateByMinuteEn        | 英文日期时分格式            |
| 022  | 22. sdfDateByFullEn          | 英文日期时间格式            |
| 023  | 23. sdfDateByAllEn           | 英文日期时间毫格式          |
| 024  | 24. sdfTimeX                 | 时间连续格式                |
| 025  | 25. sdfDateX                 | 日期连续格式                |
| 026  | 26. sdfDateByHourX           | 日期时连续格式              |
| 027  | 27. sdfDateByMinuteX         | 日期时分连续格式            |
| 028  | 28. sdfDateByFullX           | 日期时间连续格式            |
| 029  | 29. sdfDateByAllX            | 日期时间毫连续格式          |
| 030  | 30. nowDateByFullRandom2     | 日期时间连续格式加两位随机   |
| 031  | 31. nowDateByFullRandom4     | 日期时间连续格式加四位随机   |
| 032  | 32. nowDateByFullRandom6     | 日期时间连续格式加六位随机   |
| 033  | 33. nowDateByFullRandom8     | 日期时间连续格式加八位随机   |
| 034  | 34. nowDateByAllRandom2      | 日期时间毫连续格式加八位随机 |
| 035  | 35. nowDateByAllRandom4      | 日期时间毫连续格式加八位随机 |
| 036  | 36. nowDateByAllRandom6      | 日期时间毫连续格式加八位随机 |
| 037  | 37. nowDateByAllRandom8      | 日期时间毫连续格式加八位随机 |
| 038  | 38. calendarToday            | 日历今天                   |
| 039  | 39. calendarTodayNextMonth   | 日历下月今天                |
| 040  | 40. sdfDateByFull            | 单线程英文日期时间格式      |
| 041  | 41. getTimeEn                | 英文格式化时间              |
| 042  | 42. getTimeCn                | 中文格式化时间              |
| 043  | 43. getTimeAlone             | 时间去文字                  |
| 044  | 44. getYyyy                  | 时间取年                   |
| 045  | 45. getMm                    | 时间取月                   |
| 046  | 46. getDd                    | 时间取日                   |
| 047  | 47. getWeekNumberCn          | 中文一周第几天              |
| 048  | 48. getWeekNumber            | 英文一周第几天              |
| 049  | 49. getWeekOfMonth           | 月第几周                   |
| 050  | 50. getWeekOfYear            | 年第几周                   |
| 051  | 51. getDaysForMonth          | 月天数日历方式              |
| 052  | 52. getDaysOfMonth           | 月天数                     |
| 053  | 53. isLeapYear               | 是否闰年                   |
| 054  | 54. getSecondsNightOrMorning | 前一天或后一天毫秒形式      |
| 055  | 55. getDayForwardOrBackward  | 前一天或后一天              |
| 056  | 56. getNowOffsetDay          | 增减天数                   |
| 057  | 57. yesterdayString          | 昨日此时                   |
| 058  | 58. getYesterdayString       | 昨日此时指定格式            |
| 059  | 59. nowString                | 此时                       |
| 060  | 60. getNowString             | 此时指定格式                |
| 061  | 61. nowMillis                | 当前毫秒                   |
| 062  | 62. nowDate                  | 当前日期                   |
| 063  | 63. getStringByTimeSpan      | 时间增减字符串              |
| 064  | 64. getMillisByTimeSpan      | 时间增减毫秒数              |
| 065  | 65. getDateByTimeSpan        | 时间增减日期对象            |
| 066  | 66. getTimeSpan              | 时间跨度                   |
| 067  | 67. getTimeSpanFit           | 时间跨度友好显示            |
| 068  | 68. getTimeSpanFitByNow      | 当前时间跨度友好显示        |
| 069  | 69. timeOffset               | 日期格式化01时间偏移        |
| 070  | 70. ymd                      | 日期格式化02年月日          |
| 071  | 71. mdy                      | 日期格式化03月日年          |
| 072  | 72. amOrPm                   | 日期格式化04上午或下午      |
| 073  | 73. hmsAmOrPm                | 日期格式化05时分秒上午或下午 |
| 074  | 74. hms                      | 日期格式化06时分秒          |
| 075  | 75. hm                       | 日期格式化07时分            |
| 076  | 76. timeAll                  | 日期格式化08全时间          |
| 077  | 77. time2Second              | 日期格式化09转秒            |
| 078  | 78. time2Millis              | 日期格式化10转毫            |
| 079  | 79. yearFullName             | 日期格式化11年全名          |
| 080  | 80. yearReferred             | 日期格式化12年描述          |
| 081  | 81. month                    | 日期格式化13月              |
| 082  | 82. monthFullName            | 日期格式化14月全名          |
| 083  | 83. monthReferred            | 日期格式化15月描述          |
| 084  | 84. weekFullName             | 日期格式化16周全名          |
| 085  | 85. weekReferred             | 日期格式化17周描述          |
| 086  | 86. day2Year                 | 日期格式化18日转年          |
| 087  | 87. dayOne                   | 日期格式化19单月日          |
| 088  | 88. dayTwo                   | 日期格式化20全月日          |
| 089  | 89. hourL                    | 日期格式化21十二小时制      |
| 090  | 90. hourH                    | 日期格式化22二十四小时制    |
| 091  | 91. minute                   | 日期格式化23分两位          |
| 092  | 92. second                   | 日期格式化24秒两位          |
| 093  | 93. millis                   | 日期格式化25毫三位          |
| 094  | 94. subtle                   | 日期格式化26微九位          |
| 095  | 95. isDateTrue               | 是否真实时间格式化方式      |
| 096  | 96. isDateReal               | 是否真实时间                |
| 097  | 97. isDaySame                | 是否相同时间                |
| 098  | 98. isToday                  | 是否今日                   |
| 099  | 99. zeroTimeOfToday          | 今日零点                   |
| 100  | 100. compareTime             | 比较时间                   |
| 101  | 101. isEqualTime             | 是否相同时间对象            |
| 102  | 102. isBeforeTime            | 是否之前时间                |
| 103  | 103. getWeekCn               | 中文周几字符串              |
| 104  | 104. getWeekUs               | 英文周几字符串              |
| 105  | 105. getValueByCalendarField | 通过日历属性获取对应值      |

### *101.农历LunarKit(177)*

| 序号 | 方法                 | 功能       |
|:-----|:---------------------|:----------|
| 01   | 01. getZodiac        | 获取星座   |
| 02   | 02. getZodiacCn      | 获取属相   |
| 03   | 03. lunarYear2GanZhi | 农历转干支 |
| 04   | 04. solar2Lunar      | 公历转农历 |
| 05   | 05. lunar2Solar      | 农历转公历 |

### *102.关闭CloseKit(25)*

| 序号 | 方法               | 功能              |
|:-----|:-------------------|:-----------------|
| 01   | 01. closeIo        | 关闭资源          |
| 02   | 02. closeIoQuietly | 关闭资源，忽略异常 |

### *103.工具HandleKit(121)*

| 序号 | 方法                     | 功能                    |
|:-----|:-------------------------|:-----------------------|
| 01   | 01. mainHandler          | UI线程处理器            |
| 02   | 02. backgroundHandler    | 背景处理器              |
| 03   | 03. OnSimpleListener     | 延迟操作监听器          |
| 04   | 04. delayToDo            | 延迟操作                |
| 05   | 05. runOnUiThread        | 运行UI线程              |
| 06   | 06. runOnUiThreadDelayed | 延迟运行UI线程          |
| 07   | 07. fixListViewHeight    | 计算ListView高度        |
| 08   | 08. countDown            | 倒数                   |
| 09   | 09. setEditNumberAuto    | 设置EditText数字        |
| 10   | 10. setEditNumber        | 自动设置EditText数字    |
| 11   | 11. setEditDecimal       | 设置EditText小数        |
| 12   | 12. setEditType          | 设置EditText类型        |
| 13   | 13. stringFilter         | 只许数字汉字字符串过滤器 |

### *104.通讯ContactsKit(221)*

| 序号 | 方法                         | 功能           |
|:-----|:-----------------------------|:--------------|
| 01   | 01. call                     | 直接拨号      |
| 02   | 02. dial                     | 拨号界面      |
| 03   | 03. sendSmsActivity          | 短信界面      |
| 04   | 04. sendSmsSilent            | 超长短信      |
| 05   | 05. sendSmsWithReceiver      | 短信回报      |
| 06   | 06. allSms2Xml               | 短信转xml     |
| 07   | 07. toContantNumberActivity  | 联系人单选界面 |
| 08   | 08. toContactsChooseActivity | 联系人多选界面 |
| 09   | 09. getContantNumberChoosed  | 选中联系人号码 |
| 10   | 10. contacts                 | 所有联系人号码 |

### *105.地图MapKit(69)*

| 序号 | 方法                    | 功能            |
|:-----|:------------------------|:----------------|
| 01   | 01. openMap             | 打开地图进行导航 |
| 02   | 02. openGaodeMapToGuide | 打开高德地图导航 |
| 03   | 03. openBaiduMapToGuide | 打开百度地图导航 |
| 04   | 04. openBrowserToGuide  | 打开浏览器导航   |
| 05   | 05. metreToScreenPixel  | 米转屏幕像素    |
| 06   | 06. screenPixelToMetre  | 屏幕像素转米    |

### *106.网服WebServiceKit(66)*

| 序号 | 方法                   | 功能           |
|:-----|:-----------------------|:--------------|
| 01   | 01. WebServiceCallBack | WebService回调 |
| 02   | 02. callWebService     | 调用WebService |

>- implementation "com.google.code.ksoap2-android:ksoap2-android:3.6.4"

### *107.网连ConnectionKit(114)*

| 序号 | 方法           | 功能         |
|:-----|:---------------|:------------|
| 01   | 01. CallBack   | 请求完成回调 |
| 02   | 02. doGetAsyn  | 异步get     |
| 03   | 03. doGet      | 普通get     |
| 04   | 04. doPostAsyn | 异步post    |
| 05   | 05. doPost     | 普通post    |

### *108.标识UriKit(91)*

| 序号 | 方法                 | 功能           |
|:-----|:---------------------|:--------------|
| 01   | 01. getURLWithParams | 获取网址和参数 |
| 02   | 02. file2Uri         | 文件转URI     |
| 03   | 03. uri2File         | URI转文件     |

### *109.安全SslKit(57)*

| 序号 | 方法                    | 功能                 |
|:-----|:------------------------|:--------------------|
| 01   | 01. getSslSocketFactory | 获取SSLSocketFactory |
| 02   | 02. getKeyManagers      | 获取KeyManager       |

### *110.超文HtmlKit(59)*

| 序号 | 方法               | 功能               |
|:-----|:-------------------|:------------------|
| 01   | 01. keywordMadeRed | 关键词着红色       |
| 02   | 02. addHtmlRedFlag | 添加红色标签       |
| 03   | 03. getJson        | get方法json数据    |
| 04   | 04. getForm        | get方法非json数据  |
| 05   | 05. postJson       | post方法json数据   |
| 06   | 06. postForm       | post方法非json数据 |

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

