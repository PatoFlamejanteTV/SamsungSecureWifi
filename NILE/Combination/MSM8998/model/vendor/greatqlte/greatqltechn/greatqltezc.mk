$(call inherit-product, device/samsung/greatqltechn/device.mk)
include vendor/samsung/configs/greatqlte_common/greatqlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

# Include gms packages depending on product seperately
include vendor/samsung/configs/greatqltechn/gms_greatqltezc.mk

PRODUCT_NAME := greatqltezc
PRODUCT_DEVICE := greatqltechn
PRODUCT_MODEL := SM-N9500
PRODUCT_LOCALES := zh_CN en_GB en_US fr_FR de_DE zh_HK zh_TW bo_CN ja_JP nl_NL it_IT es_ES ko_KR pt_PT da_DK fi_FI ru_RU iw_IL th_TH in_ID vi_VN ar_AE

# for samsung hardware init
PRODUCT_COPY_FILES += \
    device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:root/init.carrier.rc

# QCOM eMBMS MSDC
PRODUCT_PACKAGES += \
    -QAS_DVC_MSP_VZW

# eMBMS VzW API library
PRODUCT_PACKAGES += \
    -vzw_msdc_api

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    imsservice \
    vsimmanager \
    secimshttpclient \
    ImsLogger+ \
    ImsSettings \
    libsec-ims \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    -verizon.net.sip \
    -verizon_net_sip_library.xml

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    -FBAppManager_NS \
    -appmanager.conf \
    -FBInstaller_NS \
    -FBServices \
    -FBInstagram_stub \
    -FBMessenger_stub

# Add WhatsAppDownloader
PRODUCT_PACKAGES += \
    -WhatsAppDownloader
 
# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_N

#####################################################################
# Add or Change Package for CHN model
#####################################################################

# Add VisionIntelligence
PRODUCT_PACKAGES += \
	-VisionIntelligence \
	VisionIntelligence_CN

# Video TG
PRODUCT_PACKAGES += \
    SamsungOnlineVideo

# Samsung Members v2.x for Dream/Dream2
PRODUCT_PACKAGES += \
    SamsungMembers_CHN_Removable

PRODUCT_PACKAGES += \
    OnlineMusicChina

# Add Open Calendar
PRODUCT_PACKAGES += \
	OpenCalendar_N    

# Add Firewall_N
PRODUCT_PACKAGES += \
	FirewallN
    
# Add S Assistant
PRODUCT_PACKAGES += \
    SAssistant_downloadable 
    
PRODUCT_COPY_FILES += \
    applications/par/apps/SmartAssistant/sreminder:system/etc/sreminder 

#add for Tencent PhoneNumberLocator
PRODUCT_PACKAGES += \
	PhoneNumberLocatorService

#add for MessagingExtension
PRODUCT_PACKAGES += \
	MessagingExtension_Go_Chn

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# Copy Prebuilt Library for Offline NetworkLocation
PRELOAD_LOCAL_PATH := applications/3rd_party/chn/common/NetworkLocation/NetworkLocation_Baidu/
PRODUCT_COPY_FILES += \
	$(PRELOAD_LOCAL_PATH)/lib/armeabi/liblocSDK_2_5OEM.so:system/lib/liblocSDK_2_5OEM.so \
	$(PRELOAD_LOCAL_PATH)/lib/armeabi/liblocSDK6c.so:system/lib/liblocSDK6c.so \
	$(PRELOAD_LOCAL_PATH)/lib/arm64/liblocSDK_2_5OEM.so:system/lib64/liblocSDK_2_5OEM.so \
	$(PRELOAD_LOCAL_PATH)/lib/arm64/liblocSDK6c.so:system/lib64/liblocSDK6c.so

PRODUCT_PACKAGES += \
	OfflineNetworkLocation_Baidu \
	NlpHub \
	FusedLocation_Baidu

# Copy Prebuilt Library for Baidu FLP
PRELOAD_LOCAL_PATH := applications/3rd_party/chn/common/FusedLocationBaidu/
PRODUCT_COPY_FILES += \
    $(PRELOAD_LOCAL_PATH)/lib/arm/liblocSDK7.so:system/lib/liblocSDK7.so \
    $(PRELOAD_LOCAL_PATH)/lib/arm64/liblocSDK7.so:system/lib64/liblocSDK7.so

# Add QRAgent
PRODUCT_PACKAGES += \
  QRAgent

# Add SoftsimService
PRODUCT_PACKAGES += \
	SoftsimService_Dream_N

# Add SRoaming
PRODUCT_PACKAGES += \
	SRoaming_v12_N
	
#add for airmessage
PRODUCT_PACKAGES += \
    SAirMessage_N \
    SAirMessageProxy_N

# add spam call
PRODUCT_PACKAGES += \
   BstSpamCallService

# Add AppLock
PRODUCT_PACKAGES += \
    AppLock

# Arrange YellowPage
PRODUCT_PACKAGES += \
    SamsungYellowPageN
	
PRODUCT_COPY_FILES += \
    applications/par/apps/YellowPages/SamsungYellowPages/MAIN/lib/arm64-v8a/libsmartdialernet_oem_x64.so:system/lib64/libsmartdialernet_oem_x64.so \
    applications/par/apps/YellowPages/SamsungYellowPages/MAIN/lib/arm64-v8a/libsmartdialer_oem_x64.so:system/lib64/libsmartdialer_oem_x64.so \
    applications/par/apps/YellowPages/SamsungYellowPages/MAIN/lib/armeabi/libsmartdialernet_oem.so:system/lib/libsmartdialernet_oem.so \
    applications/par/apps/YellowPages/SamsungYellowPages/MAIN/lib/armeabi/libsmartdialer_oem.so:system/lib/libsmartdialer_oem.so

# Setup SmartManager chn
PRODUCT_PACKAGES += \
    -SmartManager_v5 \
    SmartManager_v5_CN \
    -SmartManager_v5_DeviceSecurity \
    SmartManager_v5_DeviceSecurity_CN

# Add HongbaoAssistant
PRODUCT_PACKAGES += \
    HongbaoAssistant

#HongbaoModeService
PRODUCT_PACKAGES += \
    hongbaoservice

PRODUCT_BOOT_JARS += \
    hongbaoservice

# WeChatSight
PRODUCT_PACKAGES += \
    WeChatSight

# ChinaHoliday Provider
PRODUCT_PACKAGES += \
	ChinaHolidayProvider_N

# Add S Voice IME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Autologin DM
PRODUCT_PACKAGES += \
	DeviceManagement

# ChinaHiddenMenu
PRODUCT_PACKAGES += \
    ChinaHiddenMenu

# Add Library for POI
PRODUCT_COPY_FILES += \
    applications/par/apps/YellowPages/PoiLib/arm64-v8a/libPoiLib.so:system/lib64/libPoiLib.so \
    applications/par/apps/YellowPages/PoiLib/armeabi-v7a/libPoiLib.so:system/lib/libPoiLib.so

#####################################################################
# 3rd party Application for CHN model
#####################################################################

PRODUCT_PACKAGES += \
    BaiduMap_V9.0_Deletable \
    Sinamicroblog_V6.0_Deletable \
    WeChat_V6.0_Deletable \
    SearchBoxBaidu_V8.0 \
    Alipay_V10.0_Deletable \
    -SohuNews_V5.0_S7 \
    SohuNewsEdge_V2.0_Deletable

# Add ChinaUnionPay
PRODUCT_PACKAGES += \
    ChinaUnionPay

# Add ProductSearch
PRODUCT_PACKAGES += \
    ProductSearchDream

# Copy Prebuilt Library for SohuNews
PRODUCT_COPY_FILES += \
    applications/3rd_party/chn/open/SohuNews/libweibosdkcore.so:system/lib64/libweibosdkcore.so

# Add baidumap channel so file
PRODUCT_COPY_FILES += \
    applications/3rd_party/chn/open/BaiduMap/Phone/libBDoeminfo_baidu.so:system/lib/libBDoeminfo_baidu.so

# Copy Prebuilt Library for Baidu Search
PRODUCT_COPY_FILES += \
    applications/3rd_party/chn/open/SearchBoxBaidu/libBDoeminfo_baidusearch.so:system/lib/libBDoeminfo_baidusearch.so

#####################################################################
# Remove Package for CHN model
#####################################################################

# Remove skype
PRODUCT_PACKAGES += \
    -MSSkype_stub

# Remove FlipboardBriefing app
PRODUCT_PACKAGES += \
    -FlipboardBriefing

# Delete Google Search
PRODUCT_PACKAGES += \
	-QuickSearchBox

# Remove Pico TTS
PRODUCT_PACKAGES += \
	-PicoTts

# Remove SNS
PRODUCT_PACKAGES += \
	-SNS_v2_N \
	-SnsImageCache_N

# Remove CloudGatewayProvider
PRODUCT_PACKAGES += \
    -CloudGateway2017

# Add SamsungCloudEnabler(CHN only)
PRODUCT_PACKAGES += \
	SamsungCloudEnabler

# China Specific Fonts
PRODUCT_PACKAGES += \
	-CoolEUKor \
	-ChocoEUKor \
	-RoseEUKor \
	-SamsungSans \
	-Foundation \
	Miao \
	ShaoNv \
	Kaiti

# Remove MyPlaces
PRODUCT_PACKAGES += \
    -MyPlaces_N

# Remove SmartCallProvider
PRODUCT_PACKAGES += \
    -SmartCallProvider
  
# WeChatWifiService
PRODUCT_PACKAGES += \
	WeChatWifiService 

# if not include common.mk for dream, it does not need to remove this package.	
# Remove ChromeCustomizations (syncup CL@9514933 on Hero2 N)
PRODUCT_PACKAGES += \
    -ChromeCustomizations
    
# Add screen recorder for CHN Models
PRODUCT_PACKAGES += \
    ScreenRecorder

# Arrange CHN feature:DataSaving
PRODUCT_PACKAGES += \
   DataSaving

# Remove Samsung Pass for CHN models
PRODUCT_PACKAGES += \
    -SamsungPass_1.3

# WeChatSight
PRODUCT_PACKAGES += \
    WeChatSight

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_6.0_Removable \
    SBrowser_6.0

# Remove OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

# Remove PEN.UP
PRODUCT_PACKAGES += \
    -PENUP_Removable

# Add Catch Favorites
PRODUCT_PACKAGES += \
    SinaFinanceEdge \
    -YahooEdgeSports \
    -YahooEdgeFinance

# add WebManual DO NOT add this package for USA & JPN
PRODUCT_PACKAGES += \
    WebManual

# Add ActivationDevice
PRODUCT_PACKAGES += \
    ActivationDevice
    
# Remove Google Daydream
PRODUCT_PACKAGES += \
    -Daydream_VrCore \
    -vr.default \
    -Daydream_VrOemReceiver
	
PRODUCT_PROPERTY_OVERRIDES += \
    -sys.use_fifo_ui=0

PRODUCT_COPY_FILES += \
    -frameworks/native/data/etc/android.hardware.vr.high_performance.xml:system/etc/permissions/android.hardware.vr.high_performance.xml \
    -vendor/google/etc/sysconfig/google_vr_build.xml:system/etc/sysconfig/google_vr_build.xml \
    -vendor/google/etc/permissions/default-permissions.xml:system/etc/default-permissions/default-permissions.xml
