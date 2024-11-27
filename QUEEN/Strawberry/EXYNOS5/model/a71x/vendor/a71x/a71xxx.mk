$(call inherit-product, device/samsung/a71x/device.mk)
include vendor/samsung/configs/a71x_common/a71x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a71x/gms_a71xxx.mk
endif

PRODUCT_NAME := a71xxx
PRODUCT_DEVICE := a71x
PRODUCT_MODEL := SM-A716B


# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# Replace as Microsoft OfficeMobile China Stub.
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub \
    OfficeMobile_SamsungStubChina

# Add Smart MTP, only for China open & CMCC models
PRODUCT_PACKAGES += \
    MtpShareApp

# Add SoftsimService
PRODUCT_PACKAGES += \
    SoftsimService_V41

# Add softsimd daemon
PRODUCT_PACKAGES += \
    libcommapiaidl.so \
    softsimd

# WeChatWifiService
PRODUCT_PACKAGES += \
    WeChatWifiService

#Add Tencent Security Wifi
PRODUCT_PACKAGES += \
    TencentWifiSecurity

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add SamsungDataStore
PRODUCT_PACKAGES += \
    SamsungDataStore

# Samsung Members for CHN models
PRODUCT_PACKAGES += \
    SamsungMembers_CHN_P_Removable

# Add for MessagingExtension
PRODUCT_PACKAGES += \
    SamsungMessages_Extension_Chn_11.0

# Add Samsung Pay CN Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# Add ChinaUnionPay
PRODUCT_PACKAGES += \
    ChinaUnionPay

# Add DeviceIdService
PRODUCT_PACKAGES += \
    DeviceIdService

# Add VIP mode
PRODUCT_PACKAGES += \
    Firewall
	
# ChinaHiddenMenu
PRODUCT_PACKAGES += \
    ChinaHiddenMenu

# Arrange YellowPage
PRODUCT_PACKAGES += \
    SamsungYellowPage

# add for Tencent PhoneNumberLocator
PRODUCT_PACKAGES += \
    PhoneNumberLocatorService

# Add BixbyTouch
PRODUCT_PACKAGES += \
    BixbyTouch

# Add Secure Wi-Fi 
PRODUCT_PACKAGES += \ 
    Fast

# Add CHN PermissionController
PRODUCT_PACKAGES += \
    PermissionController_CHN

# Setup SmartManager chn
PRODUCT_PACKAGES += \
    -SmartManager_v5 \
    SmartManager_v5_CN \
    -SmartManager_v6_DeviceSecurity \
    SmartManager_v6_DeviceSecurity_CN

# Add HongbaoAssistant
PRODUCT_PACKAGES += \
    HongbaoAssistant
	
# Add CHN GameLauncher
PRODUCT_PACKAGES += \
    -GameHome \
    GameHome_CHN
	
# Add S Assistant
PRODUCT_PACKAGES += \
    SAssistant_downloadable
PRODUCT_COPY_FILES += \
    applications/par/idp/SAssistant/sreminder:system/etc/sreminder

# add Baidu NLP
PRODUCT_PACKAGES += \
    OfflineNetworkLocation_Baidu

# add Baidu FLP
PRODUCT_PACKAGES += \
    FusedLocation_Baidu

PRELOAD_LOCAL_PATH := applications/par/edp/CHN/FusedLocationBaidu/
PRODUCT_COPY_FILES += \
    $(PRELOAD_LOCAL_PATH)/lib/arm/liblocSDK7.so:system/lib/liblocSDK7.so \
    $(PRELOAD_LOCAL_PATH)/lib/arm64/liblocSDK7.so:system/lib64/liblocSDK7.so \
    $(PRELOAD_LOCAL_PATH)/lib/arm64/libSGPDRLib.so:system/lib64/libSGPDRLib.so

	
###################################################################
# Removed package 
###################################################################	

# Delete Google Search
PRODUCT_PACKAGES += \
	-QuickSearchBox

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    -FBAppManager_NS \
    -appmanager.conf \
    -FBInstaller_NS \
    -FBServices

# Netflix apps
PRODUCT_PACKAGES += \
    -Netflix_stub \
    -Netflix_activationCommon

# Remove SmartCallProvider
PRODUCT_PACKAGES += \
    -SmartCallProvider \
    -HiyaService

# Remove SamsungDaily and Add BixbyHome_Disable
PRODUCT_PACKAGES += \
    -SamsungDaily \
    BixbyHome_Disable

#Add PushServiceCN
PRODUCT_PACKAGES += \
    PushServiceCN

#Add TNCPageCN
PRODUCT_PACKAGES += \
    TNCPageCN

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify
    
#Remove YourPhoneCompanion
PRODUCT_PACKAGES += \
    YourPhone_P1_5

# CU DM2.0
PRODUCT_PACKAGES += \
    SystemHelper

# 3rd party Application for CHN model
PRODUCT_PACKAGES += \
    Sinamicroblog \
    BaiduSearch \
    SReading \
    QTfm \
    JDMall \
    Ctrip \
    Vips \
    NetEaseNews \
    UCWeb_OPEN \
    Qiyi \
    DianPing \
    XimalayaFM \
    Pinduoduo \
    QQmusic \
    Taobao \
    Amap_OPEN \
    TodayHeadline_OPEN \
    Booking \
    ZMWeather \
    KwaiVideo \
    WuBa \
    Aweme_OPEN \
    TencentVideo_open \
    WechatPluginMiniApp

# add Baidu search channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/BaiduSearch/libBDoeminfo_baidusearch.so:system/lib64/libBDoeminfo_baidusearch.so

# add Amap channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Amap_OPEN/amapconfig.ini:system/etc/amapconfig.ini

# add Booking channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Booking/booking.data.aid:system/etc/booking.data.aid

# add vips channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Vips/vipchannel.txt:system/etc/vipchannel.txt

# add UCWeb_OPEN channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/UCWeb/uconfig.ini:system/etc/uconfig.ini
 
# add netease channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/CTC/NetEaseNews/netease_news.channel:system/etc/netease_news.channel

# add JD channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/JDMall/jdPreInstalledInfo.dat:system/etc/jdPreInstalledInfo.dat

# add Dianping channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/DianPing/dpsource.source:system/etc/dpsource.source

# add sinaweibo channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Sinamicroblog/weibo_channel.txt:system/etc/weibo_channel.txt

# add pinduoduo channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Pinduoduo/pre_pdd_channel.txt:system/etc/pre_pdd_channel.txt

# add Taobao channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Taobao/sjtbconfig.ini:system/etc/sjtbconfig.ini

# add XimalayaFM channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/XimalayaFM/xmlyconfig.ini:system/etc/xmlyconfig.ini

# add KwaiVideo channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/KwaiVideo/kwai_samsung.txt:system/etc/kwai_samsung.txt

# add wuba channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/WuBa/wuba_channel:system/etc/wuba_channel

# add Ctrip channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Ctrip/ctripPreInstalledInfo.dat:system/etc/ctripPreInstalledInfo.dat
    
# add TencentVideo_open channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/TencentVideo_open/firmChannel.ini:system/etc/firmChannel.ini
    
# add ZMWeather channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/ZMWeather/zm-config.ini:system/etc/zm-config.ini
    
# add TodayHeadline_OPEN channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/TodayHeadline/samsung_ttmain.properties:system/etc/samsung_ttmain.properties
    
# add Aweme_OPEN channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Aweme_OPEN/aweme_400_pre_install.config:system/etc/aweme_400_pre_install.config

# Remove WindowsLinkService
PRODUCT_PACKAGES += \
    -LinkToWindowsService

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_CHN.mk
endif
###############################################################
# PLEASE DO NOT ADD LINE BELOW