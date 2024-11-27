QCOM_PRODUCT_DEVICE := r1q

$(call inherit-product, device/samsung/r1q/device.mk)
include vendor/samsung/configs/r1q_common/r1q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r1q/gms_r1qzc.mk
endif

PRODUCT_NAME := r1qzc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A8050
ifeq ($(shell secgetspf SEC_PRODUCT_FEATURE_COMMON_FAKE_BINARY), TRUE)
PRODUCT_MODEL := SM-A9200
endif

include vendor/samsung/build/localelist/SecLocale_CHN.mk

PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_NAME).rc:vendor/etc/init/hw/init.carrier.rc


#Add VIP mode
PRODUCT_PACKAGES += \
    Firewall

# Add S Voice IME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_9.0_Removable \
    SBrowser_9.0

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# Add SmartManager CHN
PRODUCT_PACKAGES += \
    -SmartManager_v5 \
    SmartManager_v5_CN \
    -SmartManager_v6_DeviceSecurity \
    SmartManager_v6_DeviceSecurity_CN

# Add YellowPage
PRODUCT_PACKAGES += \
    SamsungYellowPage

# Add AppLock
PRODUCT_PACKAGES += \
    AppLock

# Add HongbaoAssistant
PRODUCT_PACKAGES += \
    HongbaoAssistant

# Samsung Members for CHN models
PRODUCT_PACKAGES += \
    -SamsungMembers_Removable \
    SamsungMembers_CHN_P_Removable

# CHN DM
PRODUCT_PACKAGES += \
    DeviceManagement

# ChinaHiddenMenu
PRODUCT_PACKAGES += \
    ChinaHiddenMenu

# Add Screen Recorder
PRODUCT_PACKAGES += \
    ScreenRecorder

# Replace as Microsoft OfficeMobile China Stub.
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub \
    OfficeMobile_SamsungStubChina

# Add SoftsimService
PRODUCT_PACKAGES += \
    SoftsimService_V41

# Add softsimd daemon
PRODUCT_PACKAGES += \
    libcommapiaidl.so \
    softsimd

# Add SamsungDataStore
PRODUCT_PACKAGES += \
    SamsungDataStore

# Remove BixbyHome and Add BixbyHome_Disable
PRODUCT_PACKAGES += \
    -BixbyHome \
    BixbyHome_Disable

# Removed package 
PRODUCT_PACKAGES += \
    -SamsungPass

# Remove SamsungPassAutofill
PRODUCT_PACKAGES += \
    -SamsungPassAutofill_v1

PRODUCT_PACKAGES += \
    -YahooEdgeSports \
    -YahooEdgeFinance

# Delete Google Search
PRODUCT_PACKAGES += \
    -QuickSearchBox

# Remove Pico TTS
PRODUCT_PACKAGES += \
    -PicoTts
		
# Remove flipboardBriefing app
PRODUCT_PACKAGES += \
    -FlipboardBriefing

# Remove EasterEgg app as NAL test
PRODUCT_PACKAGES += \
    -EasterEgg

# Remove Exchange2 : com.android.exchange
PRODUCT_PACKAGES += \
    -Exchange2

# Remove OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

# Remove SmartCallProvider
PRODUCT_PACKAGES += \
    -SmartCallProvider \
    -HiyaService

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    -FBAppManager_NS \
    -appmanager.conf \
    -FBInstaller_NS \
    -FBServices

# Removed ChromeCustomizations
PRODUCT_PACKAGES += \
    -ChromeCustomizations

# Remove OMCAgent and PAI config(Stub)
PRODUCT_PACKAGES += \
    -OMCAgent5 \
    -SDMConfig \
    -PlayAutoInstallConfig

# Remove SPD Client
PRODUCT_PACKAGES += \
    -SPDClient
    
# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Add Smart MTP
PRODUCT_PACKAGES += \
    MtpShareApp
	
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

# WeChatWifiService
PRODUCT_PACKAGES += \
    WeChatWifiService

#Add Tencent Security Wifi
PRODUCT_PACKAGES += \
    TencentWifiSecurity

# Add CHN GameLauncher
PRODUCT_PACKAGES += \
    -GameHome \
    GameHome_CHN

# Add ChinaUnionPay
PRODUCT_PACKAGES += \
    ChinaUnionPay

# Add DynamicLockscreen apk
PRODUCT_PACKAGES += \
    DynamicLockscreen

#FM Radio
PRODUCT_PACKAGES += \
    HybridRadio_P

# Add Spam Call
PRODUCT_PACKAGES += \
    BstSpamCallService
    
# Add Tencent Phone Number Locator
PRODUCT_PACKAGES += \
    PhoneNumberLocatorService

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify
    
#Remove YourPhoneCompanion
PRODUCT_PACKAGES += \
    -YourPhone_P1_5
	
#Remove WindowsLinkService
PRODUCT_PACKAGES += \
    -LinkToWindowsService
    
PRELOAD_LOCAL_PATH := applications/par/edp/CHN/FusedLocationBaidu/
PRODUCT_COPY_FILES += \
    $(PRELOAD_LOCAL_PATH)/lib/arm/liblocSDK7.so:system/lib/liblocSDK7.so \
    $(PRELOAD_LOCAL_PATH)/lib/arm64/liblocSDK7.so:system/lib64/liblocSDK7.so \
    $(PRELOAD_LOCAL_PATH)/lib/arm64/libSGPDRLib.so:system/lib64/libSGPDRLib.so

# 3rd party Application for CHN model
PRODUCT_PACKAGES += \
    WeChat \
    Sinamicroblog \
    BaiduSearch \
    JDMall \
    BaiduMap \
    Ctrip \
    Vips \
    NetEaseNews \
    UCWeb_OPEN \
    TencentVideo \
    Qiyi \
    Meituan_OPEN \
    Hao123news \
    Booking \
    XimalayaFM \
    SReading \
    Pinduoduo \
    SohuNewsClient \
    QTfm \
    Taobao

PRODUCT_PACKAGES += \
    OnlineMusicChina

# add baidusearch channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/BaiduSearch/libBDoeminfo_baidusearch.so:system/lib64/libBDoeminfo_baidusearch.so

# add baidumap channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/BaiduMap/libBDoeminfo_baidu.so:system/lib64/libBDoeminfo_baidu.so

# add vips channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Vips/vipchannel.txt:system/etc/vipchannel.txt

# add Meituan channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Meituan_OPEN/mtconfig.ini:system/etc/mtconfig.ini

# add JD channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/JDMall/jdPreInstalledInfo.dat:system/etc/jdPreInstalledInfo.dat


# add XimalayaFM channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/XimalayaFM/xmlyconfig.ini:system/etc/xmlyconfig.ini

# add netease channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/CTC/NetEaseNews/netease_news.channel:system/etc/netease_news.channel

# add sinaweibo channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Sinamicroblog/weibo_channel.txt:system/etc/weibo_channel.txt

# add pinduoduo channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Pinduoduo/pre_pdd_channel.txt:system/etc/pre_pdd_channel.txt

# add Booking channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Booking/booking.data.aid:system/etc/booking.data.aid

# add UCWeb_OPEN channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/UCWeb/uconfig.ini:system/etc/uconfig.ini

# add SohuNewsClient channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/SohuNewsClient/SohuNews_channel.txt:system/etc/SohuNews_channel.txt

# add Taobao channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Taobao/sjtbconfig.ini:system/etc/sjtbconfig.ini

# Add for MessagingExtension
PRODUCT_PACKAGES += \
    SamsungMessages_Extension_Chn_10.0

PRODUCT_PACKAGES += \
    ActivationDevice_V2 \
    libactivation-jni 

# Add QRAgent
PRODUCT_PACKAGES += \
    QRAgent

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_CHN.mk
endif
###############################################################
