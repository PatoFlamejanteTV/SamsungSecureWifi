$(call inherit-product, device/samsung/greatqltecmcc/device.mk)
include vendor/samsung/configs/greatqlte_common/greatqlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

# Include gms packages depending on product seperately
include vendor/samsung/configs/greatqltecmcc/gms_greatqltezm.mk

PRODUCT_NAME := greatqltezm
PRODUCT_DEVICE := greatqltecmcc
PRODUCT_MODEL := SM-N9508
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
# Add or Change Package for CMCC model
#####################################################################

# Add VisionIntelligence
PRODUCT_PACKAGES += \
	-VisionIntelligence \
	VisionIntelligence_CN

# Video TG , remove Online function, use Local Player for CMCC A-ku model
PRODUCT_PACKAGES += \
    SamsungVideoList2016

# Samsung Members v2.x for CMCC model, remove SamsungMembers for CMCC A-ku model
PRODUCT_PACKAGES += \
    -SamsungMembers_CHN_Removable

# remove Online function, use Local Player for CMCC A-ku model
PRODUCT_PACKAGES += \
    music.xml \
    SMusic

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
# Remove Libray for CMCC A-ku model
#PRELOAD_LOCAL_PATH := applications/3rd_party/chn/common/NetworkLocation/NetworkLocation_Baidu/
#PRODUCT_COPY_FILES += \
#	$(PRELOAD_LOCAL_PATH)/lib/armeabi/liblocSDK_2_5OEM.so:system/lib/liblocSDK_2_5OEM.so \
#	$(PRELOAD_LOCAL_PATH)/lib/armeabi/liblocSDK6c.so:system/lib/liblocSDK6c.so \
#	$(PRELOAD_LOCAL_PATH)/lib/arm64/liblocSDK_2_5OEM.so:system/lib64/liblocSDK_2_5OEM.so \
#	$(PRELOAD_LOCAL_PATH)/lib/arm64/liblocSDK6c.so:system/lib64/liblocSDK6c.so
#
#PRODUCT_PACKAGES += \
#	OfflineNetworkLocation_Baidu \
#	NlpHub

# Add CMCC Navi for CMCC A-ku model	
PRODUCT_PACKAGES += \
	NetworkLocation_Autonavi \

# Add QRAgent
PRODUCT_PACKAGES += \
  QRAgent

# remove SoftsimService for CMCC A-ku model
PRODUCT_PACKAGES += \
	-SoftsimService_Dream_N

# remove SRoaming for CMCC A-ku model
PRODUCT_PACKAGES += \
	-SRoaming_v12_N
	
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

# Remove WeChatSight
PRODUCT_PACKAGES += \
    -WeChatSight

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
# 3rd party Application for CMCC model
#####################################################################

# remove Wechat, Alipay, Baidu Map, Weibo, Baidu for CMCC A-ku model
# use SohuNewsEdge apk at CMCC A-kU
PRODUCT_PACKAGES += \
    -BaiduMap_V9.0_Deletable \
    -Sinamicroblog_V6.0_Deletable \
    -WeChat_V6.0_Deletable \
    -SearchBoxBaidu_V8.0 \
    -Alipay_V9.9_Deletable \
    -Alipay_Service \
    -SohuNews_V5.0_S7 \
    SohuNewsEdge_V2.0_Deletable \
    139AutoMail_v1.1 \
    MM_Phone_V6.0 \
    mm_safe_60 \
    GameHall_Phone_V5.0_M \
    CMRead_Phone_V6.0_N \
    CMVoice_Phone_V3.0_M \
    CMWallet_Phone_V6.0 \
    CMEssential_Phone_V1.2_M \
    CMNavigation_Phone_V5.0 \
    CM10086_Phone_V3.0 \
    Meituan_Phone_V7.5_N \
    CMHeadline_Phone_V5.0_N \
    CMUCBrowser_Phone_V11.1.5 \
    TencentVideo_V4.8.0 \
    ShuqiReader_10.1 \
    CMFlyIME_Phone_V6.0_M \
    MobileMusic_Phone_V4.0

	
	
# Add ChinaUnionPay
PRODUCT_PACKAGES += \
    ChinaUnionPay

# Add ProductSearch
PRODUCT_PACKAGES += \
    ProductSearchDream

# remove SohuNews/ baidumap / Baidu Search Libary for CMCC A-ku model
# Copy Prebuilt Library for SohuNews
PRODUCT_COPY_FILES += \
    -applications/3rd_party/chn/open/SohuNews/libweibosdkcore.so:system/lib64/libweibosdkcore.so

# Add baidumap channel so file
PRODUCT_COPY_FILES += \
    -applications/3rd_party/chn/open/BaiduMap/Phone/libBDoeminfo_baidu.so:system/lib/libBDoeminfo_baidu.so

# Copy Prebuilt Library for Baidu Search
PRODUCT_COPY_FILES += \
    -applications/3rd_party/chn/open/SearchBoxBaidu/libBDoeminfo_baidusearch.so:system/lib/libBDoeminfo_baidusearch.so

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

# remove SamsungCloudEnabler for CMCC A-ku model
PRODUCT_PACKAGES += \
	-SamsungCloudEnabler
	
# remove SamsungCloud for CMCC A-ku model
PRODUCT_PACKAGES += \
	-SamsungCloudGreat

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

# remove WeChatWifiService for CMCC A-ku model  
PRODUCT_PACKAGES += \
	-WeChatWifiService 

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

# remove duplicated WeChatSight 
#PRODUCT_PACKAGES += \
#    WeChatSight

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_6.0_Removable \
    SBrowser_6.0

# Remove OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

# Add Catch Favorites
PRODUCT_PACKAGES += \
    SinaFinanceEdge \
    -YahooEdgeSports \
    -YahooEdgeFinance

# add WebManual DO NOT add this package for USA & JPN
PRODUCT_PACKAGES += \
    WebManual


# Remove MobileOffice Stub for CMCC A-ku model
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub

# Remove PEN.UP
PRODUCT_PACKAGES += \
    -PENUP_Removable

# Add Backup for CMCC A-ku model
PRODUCT_PACKAGES += \
    BackupAndRestore

# Add Hancom Office Editor Install version
PRODUCT_PACKAGES += \
    HancomOfficeEditor_Install

# Add for CMCC for CMCC A-ku model
PRODUCT_PACKAGES += \
    CM10086_Phone_V3.0 \
    CMEssential_Phone_V1.2_M \
    139AutoMail_v1.1

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
