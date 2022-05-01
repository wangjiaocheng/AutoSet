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

[![](https://img.shields.io/badge/CopyRight-%E7%8E%8B%E6%95%99%E6%88%90-brightgreen.svg)](https://github.com/wangjiaocheng/AutoSet/tree/master/automap/src/main/java/top/autoget/automap)
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
    implementation "com.github.wangjiaocheng.AutoSet:AutoMap:1.0.0"//不可用则直接Import Module，依赖AutoKit和AutoSee
}//build.gradle
```

## **地图库AutoMap**

| 序号 | 类库                                                                                                                                                               | 功能          |
|:-----|:------------------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------|
| 001  | *001.MapActivity*                                                                                                                                                 | 地图(3031)     |
| 002  | *002.MapIndoorFloorSwitchView*                                                                                                                                    | 室内(250)      |
| 003  | *003.MapCubeRender、MapCube*                                                                                                                                      | 自制(170)      |
| 004  | *004.MapBusLineAdapter、MapBusLineOverlay*                                                                                                                        | 线路(157)      |
| 005  | *005.MapCloudOverlay、MapCloudImageCache、MapCloudDetailActivity、MapCloudPreviewActivity*                                                                         | 云图(286)     |
| 006  | *006.MapPoiListAdapter、MapPoiSubAdapter、MapPoiOverlay*                                                                                                          | 兴趣(130)      |
| 007  | *007.MapOfflineListAdapter、MapOfflineDownloadedAdapter、MapOfflinePagerAdapter、MapOfflineChild*                                                                  | 离线(450)     |
| 008  | *008.NoScrollGridView、MapCommon、MapErrorToast*                                                                                                                  | 依赖(245)      |
| 009  | *009.MapRouteCalculateActivity、MapRouteSearchActivity、MapTipListAdapter*                                                                                        | 路线-计算(426) |
| 010  | *010.MapRouteActivity、MapRouteBusDetailActivity、MapRouteDriveDetailActivity、MapRouteRideDetailActivity、MapRouteWalkDetailActivity*                             | 路线-详情(496) |
| 011  | *011.MapRouteBusResultListAdapter、MapRouteBusSegmentListAdapter、MapRouteDriveSegmentListAdapter、MapRouteRideSegmentListAdapter、MapRouteWalkSegmentListAdapter* | 路线-结果(488) |
| 012  | *012.MapRouteOverlay、MapRouteBusOverlay、MapRouteDriveOverlay、MapRouteRideOverlay、MapRouteWalkOverlay*                                                          | 路线-覆盖(650) |
| 013  | *013.MapNaviActivity、MapNaviDriveActivity、MapNaviRideActivity、MapNaviWalkActivity、MapNaviComponentActivity*                                                    | 路线-导航(755) |
| 014  | *014.TtsControllerAMap、TtsController、TtsSystem、TtsIFly、Tts、TtsCallBack*                                                                                       | 路线-语音(427) |

| 序号 | 类库(7961)                                | 功能（开发-依赖03、开发-独立29）                                                                     |
|:-----|:------------------------------------------|:--------------------------------------------------------------------------------------------------|
| 001  | *001.MapActivity(3031)*                   | 地图(ToastKit-StringKit-DensityKit-FileKit-FileIoKit-ImageKit-LoggerKit-PathKit-DateKit-HandleKit) |
| 002  | *002.MapIndoorFloorSwitchView(250)*       | 室内(DensityKit)                                                                                   |
| 003  | *003.MapCubeRender(49)*                   | 自制                                                                                               |
| 004  | *004.MapCube(121)*                        | 自制                                                                                               |
| 005  | *005.MapBusLineAdapter(42)*               | 线路                                                                                               |
| 006  | *006.MapBusLineOverlay(115)*              | 线路                                                                                               |
| 007  | *007.MapCloudOverlay(54)*                 | 云图                                                                                               |
| 008  | *008.MapCloudImageCache(15)*              | 云图                                                                                               |
| 009  | *009.MapCloudDetailActivity(98)*          | 云图(ScreenKit)                                                                                    |
| 010  | *010.MapCloudPreviewActivity(119)*        | 云图                                                                                               |
| 011  | *011.MapPoiListAdapter(41)*               | 兴趣-列表                                                                                          |
| 012  | *012.MapPoiSubAdapter(28)*                | 兴趣-子表                                                                                          |
| 013  | *013.MapPoiOverlay(61)*                   | 兴趣-覆盖                                                                                          |
| 014  | *014.MapOfflineListAdapter(102)*          | 离线                                                                                               |
| 015  | *015.MapOfflineDownloadedAdapter(63)*     | 离线(LoggerKit-DateKit)                                                                            |
| 016  | *016.MapOfflinePagerAdapter(30)*          | 离线                                                                                               |
| 017  | *017.MapOfflineChild(255)*                | 离线(ToastKit-LoggerKit-HandleKit)                                                                 |
| 018  | *018.NoScrollGridView(12)*                | （开发-依赖01）依赖-视图                                                                            |
| 019  | *019.MapCommon(94)*                       | （开发-依赖02）依赖-工具                                                                            |
| 020  | *020.MapErrorToast(139)*                  | （开发-依赖03）依赖-错误(ToastKit-LoggerKit-HandleKit)                                              |
| 021  | *021.MapRouteCalculateActivity(226)*      | （开发-独立01）路线-计算(ToastKit-StringKit-LoggerKit)                                              |
| 022  | *022.MapRouteSearchActivity(152)*         | （开发-独立02）路线-计算(StringKit)                                                                 |
| 023  | *023.MapTipListAdapter(48)*               | （开发-独立03）路线-计算(StringKit)                                                                 |
| 024  | *024.MapRouteActivity(337)*               | （开发-独立04）路线-详情(ToastKit)                                                                  |
| 025  | *025.MapRouteBusDetailActivity(63)*       | （开发-独立05）路线-详情                                                                            |
| 026  | *026.MapRouteDriveDetailActivity(42)*     | （开发-独立06）路线-详情(LoggerKit)                                                                 |
| 027  | *027.MapRouteRideDetailActivity(27)*      | （开发-独立07）路线-详情                                                                            |
| 028  | *028.MapRouteWalkDetailActivity(27)*      | （开发-独立08）路线-详情                                                                            |
| 029  | *029.MapRouteBusResultListAdapter(54)*    | （开发-独立09）路线-结果                                                                            |
| 030  | *030.MapRouteBusSegmentListAdapter(205)*  | （开发-独立10）路线-结果                                                                            |
| 031  | *031.MapRouteDriveSegmentListAdapter(77)* | （开发-独立11）路线-结果                                                                            |
| 032  | *032.MapRouteRideSegmentListAdapter(76)*  | （开发-独立12）路线-结果                                                                            |
| 033  | *033.MapRouteWalkSegmentListAdapter(76)*  | （开发-独立13）路线-结果                                                                            |
| 034  | *034.MapRouteOverlay(122)*                | （开发-独立14）路线-覆盖                                                                            |
| 035  | *035.MapRouteBusOverlay(238)*             | （开发-独立15）路线-覆盖                                                                            |
| 036  | *036.MapRouteDriveOverlay(179)*           | （开发-独立16）路线-覆盖                                                                            |
| 037  | *037.MapRouteRideOverlay(51)*             | （开发-独立17）路线-覆盖                                                                            |
| 038  | *038.MapRouteWalkOverlay(60)*             | （开发-独立18）路线-覆盖                                                                            |
| 039  | *039.MapNaviActivity(397)*                | （开发-独立19）路线-导航(ToastKit-LoggerKit-DateKit)                                                |
| 040  | *040.MapNaviDriveActivity(217)*           | （开发-独立20）路线-导航(ToastKit)                                                                  |
| 041  | *041.MapNaviRideActivity(23)*             | （开发-独立21）路线-导航(ToastKit)                                                                  |
| 042  | *042.MapNaviWalkActivity(23)*             | （开发-独立22）路线-导航(ToastKit)                                                                  |
| 043  | *043.MapNaviComponentActivity(95)*        | （开发-独立23）路线-导航                                                                            |
| 044  | *044.TtsControllerAMap(115)*              | （开发-独立24）路线-语音(ToastKit)                                                                  |
| 045  | *045.TtsController(140)*                  | （开发-独立25）路线-语音                                                                            |
| 046  | *046.TtsSystem(59)*                       | （开发-独立26）路线-语音                                                                            |
| 047  | *047.TtsIFly(98)*                         | （开发-独立27）路线-语音                                                                            |
| 048  | *048.Tts(10)*                             | （开发-独立28）路线-语音                                                                            |
| 049  | *049.TtsCallBack(5)*                      | （开发-独立29）路线-语音                                                                            |

>- [AndroidManifest.xml](../../../../AndroidManifest.xml)
>
>- values
>
>>1. [strings.xml](../../../../res/values/strings.xml)
>>2. [strings.xml](../../../../res/values-zh-rCN/strings.xml)
>>3. [strings.xml](../../../../res/values-zh-rHK/strings.xml)
>>4. [colors.xml](../../../../res/values/colors.xml)
>>5. [dimen.xml](../../../../res/values/dimen.xml)
>
>- layout
>
>>1. [activity_map.xml:MapActivity](../../../../res/layout/activity_map.xml)
>>2. [poikeywordsearch_uri.xml:MapActivity](../../../../res/layout/poikeywordsearch_uri.xml)
>>3. [route_inputs.xml:MapActivity](../../../../res/layout/route_inputs.xml)
>>4. [offline_list_download.xml:MapActivity](../../../../res/layout/offline_list_download.xml)
>>5. [offline_list_downloaded.xml:MapActivity](../../../../res/layout/offline_list_downloaded.xml)
>>6. [busline_dialog.xml:MapActivity](../../../../res/layout/busline_dialog.xml)
>>7. [busline_item.xml:MapActivity-MapBusLineAdapter](../../../../res/layout/busline_item.xml)
>>8. [layout_item.xml:MapCloudDetailActivity](../../../../res/layout/layout_item.xml)
>>9. [layout_photo.xml:MapCloudDetailActivity](../../../../res/layout/layout_photo.xml)
>>10. [activity_cloud_detail.xml:MapCloudDetailActivity](../../../../res/layout/activity_cloud_detail.xml)
>>11. [activity_cloud_preview.xml:MapCloudPreviewActivity](../../../../res/layout/activity_cloud_preview.xml)
>>12. [listview_item.xml:MapPoiListAdapter](../../../../res/layout/listview_item.xml)
>>13. [gridview_item.xml:MapPoiSubAdapter](../../../../res/layout/gridview_item.xml)
>>14. [offline_group.xml:MapOfflineListAdapter](../../../../res/layout/offline_group.xml)
>>15. [offline_child.xml:MapOfflineChild](../../../../res/layout/offline_child.xml)
>>16. [activity_route_calculate.xml:MapRouteCalculateActivity](../../../../res/layout/activity_route_calculate.xml)
>>17. [activity_route_search.xml:MapRouteSearchActivity](../../../../res/layout/activity_route_search.xml)
>>18. [layout_search_header.xml:MapRouteSearchActivity](../../../../res/layout/layout_search_header.xml)
>>19. [tip_item.xml:MapTipListAdapter](../../../../res/layout/tip_item.xml)
>>20. [activity_route.xml:MapRouteActivity](../../../../res/layout/activity_route.xml)
>>21. [activity_route_detail.xml:MapRouteBusDetailActivity-MapRouteDriveDetailActivity-MapRouteRideDetailActivity-MapRouteWalkDetailActivity](../../../../res/layout/activity_route_detail.xml)
>>22. [item_bus_result.xml:MapRouteBusResultListAdapter](../../../../res/layout/item_bus_result.xml)
>>23. [item_bus_segment_ex.xml:MapRouteBusSegmentListAdapter](../../../../res/layout/item_bus_segment_ex.xml)
>>24. [item_bus_segment.xml:MapRouteBusSegmentListAdapter-MapRouteDriveSegmentListAdapter-MapRouteRideSegmentListAdapter-MapRouteWalkSegmentListAdapter](../../../../res/layout/item_bus_segment.xml)
>>25. [activity_navi_basic.xml:MapNaviDriveActivity](../../../../res/layout/activity_navi_basic.xml)
>>26. [activity_index.xml:MapNaviComponentActivity](../../../../res/layout/activity_index.xml)
>
>- drawable
>
>>1. [black_click_selector.xml:tip_item.xml](../../../../res/drawable/black_click_selector.xml)
>>2. [offline_title_btn.xml:offline_child.xml](../../../../res/drawable/offline_title_btn.xml)
>>3. [shape.xml:layout_search_header.xml](../../../../res/drawable/shape.xml)
>>4. [shape_text_cursor.xml:shape_text_cursor](../../../../res/drawable/shape_text_cursor.xml)
>>5. [whiteborder.xml:layout_item.xml-activity_cloud_detail.xml](../../../../res/drawable/whiteborder.xml)
>
>- mipmap
>
>>1. ![amap_bus:MapBusLineOverlay-MapRouteOverlay](../../../../res/mipmap-hdpi/amap_bus.png)
>>2. ![amap_car:MapRouteOverlay](../../../../res/mipmap-hdpi/amap_car.png)
>>3. ![amap_end:MapBusLineOverlay-MapRouteOverlay](../../../../res/mipmap-hdpi/amap_end.png)
>>4. ![amap_man:MapRouteOverlay](../../../../res/mipmap-hdpi/amap_man.png)
>>5. ![amap_ride:MapRouteOverlay](../../../../res/mipmap-hdpi/amap_ride.png)
>>6. ![amap_start:MapBusLineOverlay-MapRouteOverlay](../../../../res/mipmap-hdpi/amap_start.png)
>>7. ![amap_through:MapRouteOverlay](../../../../res/mipmap-hdpi/amap_through.png)
>>8. ![arrow_down:MapOfflineListAdapter-MapOfflineChild-offline_group.xml-offline_child.xml](../../../../res/mipmap-hdpi/arrow_down.png)
>>9. ![arrow_right:MapOfflineListAdapter-item_bus_result.xml](../../../../res/mipmap-hdpi/arrow_right.png)
>>10. ![arrow_right_blue:item_bus_result.xml-activity_route.xml](../../../../res/mipmap-hdpi/arrow_right_blue.png)
>>11. ![b1:MapNaviDriveActivity](../../../../res/mipmap-hdpi/b1.png)
>>12. ![b2:MapNaviDriveActivity-MapActivity](../../../../res/mipmap-hdpi/b2.png)
>>13. ![back:activity_cloud_preview.xml-activity_route_detail.xml](../../../../res/mipmap-hdpi/back.png)
>>14. ![bg_bottom_bar.9:ctivity_route.xml-activity_route_detail.xml](../../../../res/mipmap-hdpi/bg_bottom_bar.9.png)
>>15. ![border_bg.9:item_bus_result.xml](../../../../res/mipmap-hdpi/border_bg.9.png)
>>16. ![car:备用](../../../../res/mipmap-hdpi/car.png)
>>17. ![custom_info_bubble.9:MapActivity](../../../../res/mipmap-hdpi/custom_info_bubble.9.png)
>>18. ![custom_info_bubble.9:MapActivity](../../../../res/mipmap-mdpi/custom_info_bubble.9.png)
>>19. ![custtexture:MapActivity-MapNaviDriveActivity](../../../../res/mipmap-hdpi/custtexture.png)
>>20. ![custtexture_aolr:MapNaviDriveActivity](../../../../res/mipmap-hdpi/custtexture_aolr.png)
>>21. ![custtexture_bad:MapNaviDriveActivity](../../../../res/mipmap-hdpi/custtexture_bad.png)
>>22. ![custtexture_grayred:MapNaviDriveActivity](../../../../res/mipmap-hdpi/custtexture_grayred.png)
>>23. ![custtexture_green:MapNaviDriveActivity](../../../../res/mipmap-hdpi/custtexture_green.png)
>>24. ![custtexture_no:MapNaviDriveActivity](../../../../res/mipmap-hdpi/custtexture_no.png)
>>25. ![custtexture_slow:MapNaviDriveActivity](../../../../res/mipmap-hdpi/custtexture_slow.png)
>>26. ![dir1:MapCommon](../../../../res/mipmap-hdpi/dir1.png)
>>27. ![dir10:MapCommon](../../../../res/mipmap-hdpi/dir10.png)
>>28. ![dir11:MapCommon](../../../../res/mipmap-hdpi/dir11.png)
>>29. ![dir12:备用](../../../../res/mipmap-hdpi/dir12.png)
>>30. ![dir13:MapCommon-MapRouteBusSegmentListAdapter](../../../../res/mipmap-hdpi/dir13.png)
>>31. ![dir14:MapRouteBusSegmentListAdapter](../../../../res/mipmap-hdpi/dir14.png)
>>32. ![dir15备用](../../../../res/mipmap-hdpi/dir15.png)
>>33. ![dir16:MapRouteBusSegmentListAdapter](../../../../res/mipmap-hdpi/dir16.png)
>>34. ![dir2：MapCommon](../../../../res/mipmap-hdpi/dir2.png)
>>35. ![dir3：MapCommon](../../../../res/mipmap-hdpi/dir3.png)
>>36. ![dir4：MapCommon](../../../../res/mipmap-hdpi/dir4.png)
>>37. ![dir5：MapCommon](../../../../res/mipmap-hdpi/dir5.png)
>>38. ![dir6：MapCommon](../../../../res/mipmap-hdpi/dir6.png)
>>39. ![dir7：MapCommon](../../../../res/mipmap-hdpi/dir7.png)
>>40. ![dir8：MapCommon](../../../../res/mipmap-hdpi/dir8.png)
>>41. ![dir9：MapCommon](../../../../res/mipmap-hdpi/dir9.png)
>>42. ![dir_end:MapRouteBusSegmentListAdapter-MapRouteDriveSegmentListAdapter-MapRouteRideSegmentListAdapter-MapRouteWalkSegmentListAdapter](../../../../res/mipmap-hdpi/dir_end.png)
>>43. ![dir_start:MapRouteBusSegmentListAdapter-MapRouteDriveSegmentListAdapter-MapRouteRideSegmentListAdapter-MapRouteWalkSegmentListAdapter-item_bus_segment.xml](../../../../res/mipmap-hdpi/dir_start.png)
>>44. ![dir_station:item_bus_segment_ex.xml](../../../../res/mipmap-hdpi/dir_station.png)
>>45. ![down:item_bus_segment.xml](../../../../res/mipmap-hdpi/down.png)
>>46. ![end:备用](../../../../res/mipmap-anydpi-v26/end.png)
>>47. ![end:备用](../../../../res/mipmap-hdpi/end.png)
>>48. ![end:备用](../../../../res/mipmap-mdpi/end.png)
>>49. ![end:备用](../../../../res/mipmap-xhdpi/end.png)
>>50. ![gps_point：MapActivity](../../../../res/mipmap-hdpi/gps_point.png)
>>51. ![groundoverlay:MapActivity](../../../../res/mipmap-hdpi/groundoverlay.png)
>>52. ![icon_car:MapActivity](../../../../res/mipmap-hdpi/icon_car.png)
>>53. ![infowindow_bg.9:MapActivity](../../../../res/mipmap-hdpi/infowindow_bg.9.png)
>>54. ![lane00:MapNaviDriveActivity](../../../../res/mipmap-hdpi/lane00.png)
>>55. ![location:tip_item.xml](../../../../res/mipmap-hdpi/location.png)
>>56. ![location_marker:MapActivity-MapCloudDetailActivity](../../../../res/mipmap-hdpi/location_marker.png)
>>57. ![map_alr:MapActivity](../../../../res/mipmap-hdpi/map_alr.png)
>>58. ![map_alr_night:MapActivity](../../../../res/mipmap-hdpi/map_alr_night.png)
>>59. ![map_indoor_select:MapIndoorFloorSwitchView](../../../../res/mipmap-hdpi/map_indoor_select.png)
>>60. ![marker_blue:MapActivity](../../../../res/mipmap-hdpi/marker_blue.png)
>>61. ![marker_other_highlight:MapActivity](../../../../res/mipmap-hdpi/marker_other_highlight.png)
>>62. ![navi_end:MapNaviDriveActivity](../../../../res/mipmap-hdpi/navi_end.png)
>>63. ![navi_start:MapNaviDriveActivity](../../../../res/mipmap-hdpi/navi_start.png)
>>64. ![navi_way:MapNaviDriveActivity](../../../../res/mipmap-hdpi/navi_way.png)
>>65. ![offline_common_bar_bg.9:offline_list_downloaded.xml](../../../../res/mipmap-hdpi/offline_common_bar_bg.9.png)
>>66. ![offlinearrow_but_normal.9:offline_title_btn.xml](../../../../res/mipmap-hdpi/offlinearrow_but_normal.9.png)
>>67. ![offlinearrow_but_pressed.9:offline_title_btn.xml](../../../../res/mipmap-hdpi/offlinearrow_but_pressed.9.png)
>>68. ![offlinearrow_down:MapOfflineChild-offline_child.xml-offline_group.xml](../../../../res/mipmap-hdpi/offlinearrow_down.png)
>>69. ![offlinearrow_download:MapOfflineChild-offline_child.xml](../../../../res/mipmap-hdpi/offlinearrow_download.png)
>>70. ![offlinearrow_start:MapOfflineChild](../../../../res/mipmap-hdpi/offlinearrow_start.png)
>>71. ![offlinearrow_stop:MapOfflineChild](../../../../res/mipmap-hdpi/offlinearrow_stop.png)
>>72. ![offlinearrow_tab1_normal.9:MapActivity](../../../../res/mipmap-hdpi/offlinearrow_tab1_normal.9.png)
>>73. ![offlinearrow_tab1_pressed.9:MapActivity](../../../../res/mipmap-hdpi/offlinearrow_tab1_pressed.9.png)
>>74. ![offlinearrow_tab2_normal.9:MapActivity](../../../../res/mipmap-hdpi/offlinearrow_tab2_normal.9.png)
>>75. ![offlinearrow_tab2_pressed.9:MapActivity](../../../../res/mipmap-hdpi/offlinearrow_tab2_pressed.9.png)
>>76. ![poi_marker_1:MapActivity](../../../../res/mipmap-hdpi/poi_marker_1.png)
>>77. ![poi_marker_10:MapActivity](../../../../res/mipmap-hdpi/poi_marker_10.png)
>>78. ![poi_marker_2:MapActivity](../../../../res/mipmap-hdpi/poi_marker_2.png)
>>79. ![poi_marker_3:MapActivity](../../../../res/mipmap-hdpi/poi_marker_3.png)
>>80. ![poi_marker_4:MapActivity](../../../../res/mipmap-hdpi/poi_marker_4.png)
>>81. ![poi_marker_5:MapActivity](../../../../res/mipmap-hdpi/poi_marker_5.png)
>>82. ![poi_marker_6:MapActivity](../../../../res/mipmap-hdpi/poi_marker_6.png)
>>83. ![poi_marker_7:MapActivity](../../../../res/mipmap-hdpi/poi_marker_7.png)
>>84. ![poi_marker_8:MapActivity](../../../../res/mipmap-hdpi/poi_marker_8.png)
>>85. ![poi_marker_9:MapActivity](../../../../res/mipmap-hdpi/poi_marker_9.png)
>>86. ![poi_marker_pressed:MapActivity](../../../../res/mipmap-hdpi/poi_marker_pressed.png)
>>87. ![point4:MapActivity](../../../../res/mipmap-hdpi/point4.png)
>>88. ![purple_pin:MapActivity](../../../../res/mipmap-hdpi/purple_pin.png)
>>89. ![r1:MapNaviDriveActivity](../../../../res/mipmap-hdpi/r1.png)
>>90. ![route_bus_normal:MapRouteActivity-activity_route.xml](../../../../res/mipmap-hdpi/route_bus_normal.png)
>>91. ![route_bus_select:MapRouteActivity](../../../../res/mipmap-hdpi/route_bus_select.png)
>>92. ![route_drive_normal:MapRouteActivity-activity_route.xml](../../../../res/mipmap-hdpi/route_drive_normal.png)
>>93. ![route_drive_select:MapRouteActivity](../../../../res/mipmap-hdpi/route_drive_select.png)
>>94. ![route_walk_normal:MapRouteActivity-activity_route.xml](../../../../res/mipmap-hdpi/route_walk_normal.png)
>>95. ![route_walk_select:MapRouteActivity](../../../../res/mipmap-hdpi/route_walk_select.png)
>>96. ![search_icon:layout_search_header.xml](../../../../res/mipmap-hdpi/search_icon.png)
>>97. ![start:MapRouteCalculateActivity](../../../../res/mipmap-anydpi-v26/start.png)
>>98. ![start:MapRouteCalculateActivity](../../../../res/mipmap-hdpi/start.png)
>>99. ![start:MapRouteCalculateActivity](../../../../res/mipmap-mdpi/start.png)
>>100. ![start:MapRouteCalculateActivity](../../../../res/mipmap-xhdpi/start.png)
>>101. ![start_uri:poikeywordsearch_uri.xml](../../../../res/mipmap-hdpi/start_uri.png)
>>102. ![title_background.9:activity_route.xml-activity_route_detail.xml](../../../../res/mipmap-hdpi/title_background.9.png)
>>103. ![up:备用](../../../../res/mipmap-hdpi/up.png)
>
>- raw
>
>- [point10w.txt:MapActivity](../../../../res/raw/point10w.txt)
>
>- assets
>
>1. [location.html:MapActivity](../../../../assets/location.html)
>2. style.data:MapActivity
>3. style_dark.data:MapActivity
>4. style_extra.data:MapActivity
>5. style_extra_dark.data:MapActivity

### *001.地图MapActivity(3025)*

| 序号 | 方法                            | 功能（设置-选项06、设置-开关04、开发-调用114、开发-创建22、开发-依赖31-03）                                     |
|:-----|:--------------------------------|:-----------------------------------------------------------------------------------------------------------|
| 001  | 001.selectMapCustomStyleFile    | （设置-选项01）选择地图自定义风格文件                                                                         |
| 002  | 002.selectTypeMap               | （设置-选项02）选择地图类型：普通、卫星、夜景、导航、公交                                                      |
| 003  | 003.selectTypeLocationSpinner   | （设置-选项03）选择定位类型：功能已实现，只需创建选择器                                                        |
| 004  | 004.selectTypeLanguage          | （设置-选项04）选择语言类型：中文、英文                                                                       |
| 005  | 005.selectPositionLogo          | （设置-选项05）选择logo位置：下左、下中、下右                                                                 |
| 006  | 006.selectPositionControls      | （设置-选项06）选择控件位置：右中、右下                                                                       |
| 007  | 007.changeStop                  | （开发-调用001）地图改变动画停止                                                                             |
| 008  | 008.changeCamera                | （开发-调用002）地图改变动画：是否动画、持续时间、完成回调、取消回调                                            |
| 009  | 009.changePosition              | （开发-调用003）地图改变坐标：缩放、倾斜、转向、图标                                                           |
| 010  | 010.Direction                   | （开发-调用004）地图移动方向：左、上、右、下                                                                  |
| 011  | 011.changeScroll                | （开发-调用005）地图移动像素                                                                                 |
| 012  | 012.changeLimits                | （开发-调用006）地图改变坐标区域                                                                             |
| 013  | 013.zoomIn                      | （开发-调用007）地图放大                                                                                     |
| 014  | 014.zoomOut                     | （开发-调用008）地图缩小                                                                                     |
| 015  | 015.zoomLevel                   | （开发-调用009）地图设置缩放等级：当前、最大、最小、是否重置                                                   |
| 016  | 016.showUiSettings              | （设置-开关01_9）地图启用手势控件定位：拖拽、缩放、倾斜、旋转、比例尺、缩放按钮、指南针、定位按钮、室内地图控件    |
| 017  | 017.showLayers                  | （设置-开关02_4）地图显示图层：交通、建筑、文字、室内地图                                                      |
| 018  | 018.mapIndoorFloorSwitchView    | （开发-创建01）自定义室内地图：控件已实现，只需创建其实例                                                      |
| 019  | 019.showIndoor                  | （设置-开关03_1）显示自定义室内地图：【室内】                                                                 |
| 020  | 020.showOpenGl                  | （开发-调用010）显示自定义图形：【自制】                                                                      |
| 021  | 021.Companion                   | （开发-调用011）自定义静态属性：地域级别（国、省、市、区、圈）、离线消息、热力图梯度                             |
| 022  | 022.showHeatMap                 | （开发-调用012）显示热力图：批量坐标TileOverlay                                                               |
| 023  | 023.showOverlayTile             | （开发-调用013）显示Tile覆盖物：指定"http://a.tile.openstreetmap.org/%d/%d/%d.png"                           |
| 024  | 024.showOverlayGround           | （开发-调用014）显示Ground覆盖物：指定坐标范围和图片资源                                                       |
| 025  | 025.showOverlayMultiPoint       | （开发-调用015）显示MultiPoint覆盖物：指定文本资源                                                            |
| 026  | 026.showNavigateArrow           | （开发-调用016）显示导航箭头：指定宽度和坐标                                                                  |
| 027  | 027.showText                    | （开发-调用017）显示文本：坐标、文字、字号、字型、字色、背景、角度、深度                                        |
| 028  | 028.showMarkers                 | （开发-调用018）显示批量Marker                                                                               |
| 029  | 029.showMarkerDefault           | （开发-调用019）显示原生Marker：标题、详情、可拖动性、是否平坦、刷新频率、原生图标、角度、像素位置、信息窗口      |
| 030  | 030.showMarkerRes               | （开发-调用020）显示自定义资源Marker：标题、详情、可拖动性、是否平坦、刷新频率、资源图标、角度、像素位置、信息窗口 |
| 031  | 031.showMarkerView              | （开发-调用021）显示自定义视图Marker：标题、详情、可拖动性、是否平坦、刷新频率、视图图标、角度、像素位置、信息窗口 |
| 032  | 032.showMarkerIcons             | （开发-调用022）显示自定义多图Marker：标题、详情、可拖动性、是否平坦、刷新频率、多图图标、角度、像素位置、信息窗口 |
| 033  | 033.showMarkerLocation          | （开发-调用023）显示定位Marker                                                                               |
| 034  | 034.showMarkerCar               | （开发-调用024）显示汽车Marker                                                                               |
| 035  | 035.showMarkerInScreenCenter    | （开发-调用025）显示屏中Marker                                                                               |
| 036  | 036.showAnimationDrop           | （开发-调用026）Marker降低动画                                                                               |
| 037  | 037.showAnimationGrow           | （开发-调用027）Marker生长动画                                                                               |
| 038  | 038.showAnimationGrowClick      | （开发-调用028）Marker点击生长动画                                                                           |
| 039  | 039.showAnimationJump           | （开发-调用029）Marker跳动动画                                                                               |
| 040  | 040.showAnimationJumpClick      | （开发-调用030）Marker点击跳动动画                                                                           |
| 041  | 041.showPolygon                 | （开发-调用031）显示多边形：坐标列表、宽度、边框颜色、填充颜色                                                 |
| 042  | 042.showPolygonRectangle        | （开发-调用032）显示矩形：中心坐标、半宽、半高、边框宽度、边框颜色、填充颜色                                    |
| 043  | 043.showPolygonEllipse          | （开发-调用033）显示椭圆：中心坐标、边框宽度、边框颜色、填充颜色                                                |
| 044  | 044.showArc                     | （开发-调用034）显示弧线：起点、中点、终点、边框颜色                                                           |
| 045  | 045.showCircle                  | （开发-调用035）显示圆形：中心坐标、半径、边框宽度、边框颜色、填充颜色                                          |
| 046  | 046.showCirclePolyline          | （开发-调用036）显示多边圆形：中心坐标、半径、边框宽度、边框颜色、纹理、渐变、虚线、大地                         |
| 047  | 047.showPolyline                | （开发-调用037）显示线段：坐标列表、边框宽度、边框颜色、纹理、渐变、虚线、大地                                   |
| 048  | 048.update                      | （开发-调用038）坐标经纬度等值偏移                                                                           |
| 049  | 049.showColorsPolyline          | （开发-调用039）显示多彩线段：坐标、偏移、边框宽度、边框颜色、纹理、渐变、虚线、大地                             |
| 050  | 050.showTexturePolyline         | （开发-调用040）显示纹理线段：坐标、偏移、边框宽度                                                             |
| 051  | 051.showPolylineInPlayGround    | （开发-调用041）显示操场线段：中心坐标、半径、边框宽度、渐变、虚线、大地                                        |
| 052  | 052.listenerEvent               | （设置-开关04_9）启用事件监听：加载、移动、点击、长按、触摸、兴趣点点击、Marker拖动、Marker点击、信息窗口点击     |
| 053  | 053.listenerShot                | （开发-调用042）地图截屏                                                                                     |
| 054  | 054.cityCode                    | （开发-调用043）城市代码处理：截取“-”后部分，搜索公交站点线路需要                                              |
| 055  | 055.searchStation               | （开发-调用044）搜索公交站点                                                                                 |
| 056  | 056.searchLine                  | （开发-调用045）搜索公交线路：【线路】                                                                        |
| 057  | 057.tableId                     | （开发-依赖04）搜索云详情需要                                                                                |
| 058  | 058.lineId                      | （开发-依赖05）搜索云详情需要                                                                                |
| 059  | 059.searchById                  | （开发-调用046）搜索云详情：通过tableId和lineId                                                              |
| 060  | 060.keyWord                     | （开发-依赖06）搜索周边范围关键词                                                                            |
| 061  | 061.pointCenter                 | （开发-依赖07）搜索周边范围中心坐标                                                                           |
| 062  | 062.searchByBound               | （开发-调用047）搜索周边范围：通过tableId、关键词和中心坐标                                                    |
| 063  | 063.point1                      | （开发-依赖08）多边形坐标1                                                                                   |
| 064  | 064.point2                      | （开发-依赖09）多边形坐标2                                                                                   |
| 065  | 065.point3                      | （开发-依赖10）多边形坐标3                                                                                   |
| 066  | 066.point4                      | （开发-依赖11）多边形坐标4                                                                                   |
| 067  | 067.searchByPolygon             | （开发-调用048）搜索多边形范围：通过tableId、关键词和中心坐标                                                  |
| 068  | 068.localCityName               | （开发-依赖12）本地城市名称                                                                                  |
| 069  | 069.searchByLocal               | （开发-调用049）搜索城市范围：【云图】                                                                        |
| 070  | 070.searchLatLngByAddress       | （开发-调用050）搜索城市地址：地址、城市                                                                      |
| 071  | 071.toLatLonPoints              | （开发-调用051）双精度数组转经纬度点列表                                                                      |
| 072  | 072.searchAddressesByLatLonList | （开发-调用052）搜索地址以经纬度点列表                                                                        |
| 073  | 073.point                       | （开发-依赖13）经纬度点                                                                                      |
| 074  | 074.searchAddressByLatLon       | （开发-调用053）搜索地址以经纬度点                                                                           |
| 075  | 075.isWithBoundary              | （开发-依赖14）是否带行政区划边界                                                                            |
| 076  | 076.searchDistrict              | （开发-调用054）搜索行政区划                                                                                 |
| 077  | 077.searchDistrictWithBoundary  | （开发-调用055）搜索行政区划带边界                                                                           |
| 078  | 078.infoCountry                 | （开发-依赖15）国家信息：用于获取，而非赋值                                                                   |
| 079  | 079.infoProvince                | （开发-依赖16）省州信息：用于获取，而非赋值                                                                   |
| 080  | 080.infoCity                    | （开发-依赖17）城市信息：用于获取，而非赋值                                                                   |
| 081  | 081.infoDistrict                | （开发-依赖18）区划信息：用于获取，而非赋值                                                                   |
| 082  | 082.spinnerProvince             | （开发-创建02）选择省州：功能已实现，只需创建选择器                                                            |
| 083  | 083.spinnerCity                 | （开发-创建03）选择城市：功能已实现，只需创建选择器                                                            |
| 084  | 084.spinnerDistrict             | （开发-创建04）选择区划：功能已实现，只需创建选择器                                                            |
| 085  | 085.keywordText                 | （开发-依赖19）关键词文本输入框：AutoCompleteTextView类型                                                     |
| 086  | 086.poiSearchList               | （开发-依赖20）兴趣点搜索列表视图：ListView类型                                                               |
| 087  | 087.searchPoiSub                | （开发-调用056）搜索子兴趣点：【兴趣-列表、兴趣-子表】                                                         |
| 088  | 088.detailMarker                | （开发-创建05）详情Marker：需要创建                                                                          |
| 089  | 089.mPoiName                    | （开发-创建06）兴趣点名称：需要创建TextView                                                                   |
| 090  | 090.mPoiAddress                 | （开发-创建07）兴趣点地址：需要创建TextView                                                                   |
| 091  | 091.mPoiInfo                    | （开发-创建08）兴趣点信息：需要创建TextView                                                                   |
| 092  | 092.mPoiDetail                  | （开发-创建09）兴趣点详情：需要创建RelativeLayout                                                             |
| 093  | 093.searchPoiId                 | （开发-调用057）搜索兴趣点以ID                                                                               |
| 094  | 094.searchPoiKeyword            | （开发-调用058）搜索兴趣点以关键词：【兴趣-覆盖】                                                             |
| 095  | 095.nextPagePoiKeyword          | （开发-依赖21）下一页兴趣点关键词                                                                            |
| 096  | 096.resetLastMarker             | （开发-调用059）重置最近Marker                                                                               |
| 097  | 097.searchPoiAround             | （开发-调用060）搜索兴趣点周边                                                                               |
| 098  | 098.cityNameTv                  | （开发-创建10）城市名称：需要创建TextView                                                                    |
| 099  | 099.reportTimeLive              | （开发-创建11）即报时间：需要创建TextView                                                                    |
| 100  | 100.weatherTv                   | （开发-创建12）天气：需要创建TextView                                                                        |
| 101  | 101.temperatureTv               | （开发-创建13）温度：需要创建TextView                                                                        |
| 102  | 102.windTv                      | （开发-创建14）风力：需要创建TextView                                                                        |
| 103  | 103.humidityTv                  | （开发-创建15）湿度：需要创建TextView                                                                        |
| 104  | 104.reportTimeForecast          | （开发-创建16）预报时间：需要创建TextView                                                                    |
| 105  | 105.forecastTv                  | （开发-创建17）预报：需要创建TextView                                                                        |
| 106  | 106.searchWeatherLiveOrForecast | （开发-调用061）搜索天气即报或预报                                                                           |
| 107  | 107.urlView                     | （开发-创建18）路线WebView：需要创建                                                                         |
| 108  | 108.shareRoute                  | （开发-调用062）分享路线：起点、终点                                                                          |
| 109  | 109.sharePoi                    | （开发-调用063）分享兴趣点：坐标、标题、详情                                                                  |
| 110  | 110.shareLocation               | （开发-调用064）分享定位：坐标、标题、详情                                                                    |
| 111  | 111.shareNavi                   | （开发-调用065）分享导航：起点、终点                                                                          |
| 112  | 112.contentViewPage             | （开发-创建19）内容视图页面：功能已实现，需要创建ViewPager                                                     |
| 113  | 113.textDownload                | （开发-创建20）离线下载动态：功能已实现，需要创建TextView                                                      |
| 114  | 114.textDownloaded              | （开发-创建21）离线已下载动态：功能已实现，需要创建TextView                                                    |
| 115  | 115.offlineStartInPause         | （开发-调用066）暂停中开始离线下载：【离线】                                                                  |
| 116  | 116.offlineCancelInPause        | （开发-调用067）暂停中取消离线下载                                                                           |
| 117  | 117.offlineStop                 | （开发-调用068）停止离线下载                                                                                 |
| 118  | 118.offlineLog                  | （开发-调用069）离线下载日志                                                                                 |
| 119  | 119.backImage                   | （开发-依赖22）离线结束退出                                                                                  |
| 120  | 120.metrePerPixel               | （开发-调用070）当前每像素地理距离（米）                                                                      |
| 121  | 121.distance                    | （开发-调用071）两点坐标距离                                                                                 |
| 122  | 122.markersList                 | （开发-调用072）屏显地图所有Marker列表                                                                       |
| 123  | 123.markersSize                 | （开发-调用073）屏显地图所有Marker数量                                                                       |
| 124  | 124.isContain                   | （开发-调用074）指定多边形是否包含坐标                                                                        |
| 125  | 125.isContain                   | （开发-调用075）显示范围内是否包含坐标                                                                        |
| 126  | 126.toPoint                     | （开发-调用076）经纬度转点                                                                                   |
| 127  | 127.toLatLng                    | （开发-调用077）点转经纬度                                                                                   |
| 128  | 128.types                       | （开发-调用078）地图类型数组：百度、图吧、wgs84、图盟、搜搜、阿里云、谷歌                                       |
| 129  | 129.Convert                     | （开发-调用079）高德坐标转指定地图类型坐标                                                                    |
| 130  | 130.startNavi                   | （开发-调用080）导航到Marker坐标                                                                             |
| 131  | 131.watchText                   | （开发-调用081）AutoCompleteTextView输入文本监测                                                             |
| 132  | 132.startMove                   | （开发-调用082）按坐标列表移动                                                                               |
| 133  | 133.register                    | （开发-调用083）注册加速传感器和地磁传感器                                                                    |
| 134  | 134.unRegister                  | （开发-调用084）取消加速传感器和地磁传感器                                                                    |
| 135  | 135.markerRotate                | （开发-创建22）转向Marker：需要创建                                                                          |
| 136  | 136.onOrientationListener       | （开发-依赖23）方向改变监听器：监听手机转向改变Marker方向                                                      |
| 137  | 137.startTrace                  | （开发-调用085）开始轨迹纠偏                                                                                 |
| 138  | 138.stopTrace                   | （开发-调用086）停止轨迹纠偏                                                                                 |
| 139  | 139.distanceSum                 | （开发-调用087）纠偏覆盖物总计距离：用户获取，而非赋值                                                         |
| 140  | 140.timeWait                    | （开发-调用088）纠偏覆盖物等待时间：用户获取，而非赋值                                                         |
| 141  | 141.coordinateType              | （开发-调用089）设置纠偏坐标类型                                                                             |
| 142  | 142.traceGrasp                  | （开发-调用090）纠偏控制                                                                                     |
| 143  | 143.cleanFinishTrace            | （开发-调用091）清除完成纠偏                                                                                 |
| 144  | 144.setMapFragment              | （开发-调用092）MapFragment初始化：Activity中不可用                                                          |
| 145  | 145.aMapLocationClientOption    | （开发-依赖24）定位选项：可以重设                                                                            |
| 146  | 146.locationMsg                 | （开发-调用093）定位消息：用户获取，而非赋值，包含定位资料                                                     |
| 147  | 147.locationStr                 | （开发-调用094）定位资料：用户获取，而非赋值                                                                  |
| 148  | 148.errorStr                    | （开发-调用095）定位错误：用户获取，而非赋值                                                                  |
| 149  | 149.alarmInterval               | （开发-依赖25）定位警报间隔时间                                                                              |
| 150  | 150.locationStart               | （开发-调用096）开始定位                                                                                     |
| 151  | 151.locationStop                | （开发-调用097）停止定位                                                                                     |
| 152  | 152.assistantStart              | （开发-调用098）开始H5辅助定位                                                                               |
| 153  | 153.assistantStop               | （开发-调用099）停止H5辅助定位                                                                               |
| 154  | 154.locationLast                | （开发-调用100）最近定位                                                                                     |
| 155  | 155.locationDestroy             | （开发-调用101）销毁定位                                                                                     |
| 156  | 156.isAvailable                 | （开发-调用102）DPoint可用与否                                                                               |
| 157  | 157.Convert                     | （开发-调用103）DPoint转指定地图类型DPoint                                                                   |
| 158  | 158.addCenterMarker             | （开发-调用104）添加指定坐标可拖动中心Marker                                                                  |
| 159  | 159.addPolygonMarker            | （开发-调用105）添加指定坐标可拖动多边Marker                                                                  |
| 160  | 160.locationInfo                | （开发-调用106）定位信息                                                                                     |
| 161  | 161.toDPoint                    | （开发-调用107）经纬度转DPoint                                                                               |
| 162  | 162.customId                    | （开发-依赖26）地理围栏自定义ID                                                                              |
| 163  | 163.addPolygonFence             | （开发-调用108）添加多边形地理围栏                                                                           |
| 164  | 164.keyword                     | （开发-依赖27）地理围栏关键词                                                                                |
| 165  | 165.addDistrictFence            | （开发-调用109）添加行政区划地理围栏                                                                          |
| 166  | 166.centerLatLng                | （开发-依赖28）地理围栏坐标                                                                                  |
| 167  | 167.radiusStr                   | （开发-调用110）地理围栏半径：转浮点数                                                                        |
| 168  | 168.addRoundFence               | （开发-调用111）添加周边地理围栏                                                                             |
| 169  | 169.poiType                     | （开发-依赖29）地理围栏兴趣点类型                                                                            |
| 170  | 170.sizeStr                     | （开发-依赖30）地理围栏尺寸                                                                                  |
| 171  | 171.addNearbyFence              | （开发-调用112）添加附近地理围栏                                                                             |
| 172  | 172.city                        | （开发-依赖31）地理围栏城市                                                                                  |
| 173  | 173.addKeywordFence             | （开发-调用113）添加城市关键词地理围栏                                                                        |
| 174  | 174.clearGeoFence               | （开发-调用114）清除地理围栏                                                                                 |

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    aMapNaviView?.apply {
        if (isTrafficBar) lazyTrafficBarView = activityNaviBasicBinding.trafficBarView.apply {
            setTmcBarHeightWhenLandscape(0.5)//横屏<0.1用0.1，值>1用1：为原始图片一半
            setTmcBarHeightWhenPortrait(1.0)//竖屏<0.1用0.1，值>1用1：和原始图片一致
            unknownTrafficColor = Color.WHITE
            smoothTrafficColor = Color.GREEN
            slowTrafficColor = Color.YELLOW
            jamTrafficColor = Color.DKGRAY
            veryJamTrafficColor = Color.BLACK
        }
    }
    aMapNaviView?.viewOptions?.apply {
        if (isTrafficBar) {
            isTrafficBarEnabled = isTrafficBar
            tilt = 0//倾斜[0，60]，0为2D模式
            setPointToCenter(1.0 / 2, 1.0 / 2)//自车位置锁定在屏幕中的位置，一参宽度，二参高度
        }
    }
}
override fun onConfigurationChanged(newConfig: Configuration) {
    activityNaviBasicBinding.trafficBarView.onConfigurationChanged(isOrientationLandscape)
}
override fun onTrafficStatusUpdate() {
    if (isTrafficBar) {
        val end: Int = aMapNaviPath?.allLength ?: 0
        val start: Int = end - remainingDistance
        activityNaviBasicBinding.trafficBarView.update(
            aMapNavi?.getTrafficStatuses(start, end), remainingDistance
        )
    }
}
override fun onCalculateRouteSuccess(aMapCalcRouteResult: AMapCalcRouteResult?) {
    if (isTrafficBar) {
        aMapNaviPath = aMapNavi?.naviPath
        val end: Int = aMapNaviPath?.allLength ?: 0
        activityNaviBasicBinding.trafficBarView
            .update(aMapNavi?.getTrafficStatuses(0, end), end)
    }
}//api "com.amap.api:navi-3dmap:7.6.0_3dmap7.6.0"升级api "com.amap.api:navi-3dmap:8.1.0_3dmap8.1.0"后TrafficBarView不存在后删除代码
```

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

