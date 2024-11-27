QCOM_PRODUCT_DEVICE := beyond2qltecmcc

$(call inherit-product, device/samsung/beyond2qltecmcc/device.mk)
include vendor/samsung/configs/beyond2qlte_common/beyond2qlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyond2qltecmcc/gms_beyond2qltezm.mk
endif

# define the default locales for phone device
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/build/localelist/SecLocale_CHN.mk
endif

PRODUCT_NAME := beyond2qltezm
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G9758
ifeq ($(shell secgetspf SEC_PRODUCT_FEATURE_COMMON_FAKE_BINARY), TRUE)
PRODUCT_MODEL := SM-G9550
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload
	   
# Add WebManual
PRODUCT_PACKAGES += \
    WebManual
	
# Remove BixbyHome and Add BixbyHome_Disable
PRODUCT_PACKAGES += \
    -BixbyHome \
    BixbyHome_Disable
	
# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Replace as Microsoft OfficeMobile China Stub.
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub \
    OfficeMobile_SamsungStubChina

# Add QRAgent
PRODUCT_PACKAGES += \
    QRAgent

# Add S Assistant
PRODUCT_PACKAGES += \
    SAssistant_downloadable
PRODUCT_COPY_FILES += \
    applications/par/idp/SAssistant/sreminder:system/etc/sreminder

###################################################################
# Package for Open Model
###################################################################

# Add Samsung Pay CN Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# Add ChinaUnionPay
PRODUCT_PACKAGES += \
    ChinaUnionPay

# Add YellowPage
PRODUCT_PACKAGES += \
    SamsungYellowPage
	
# Add Cmcc VVM
PRODUCT_PACKAGES += \
    CmccVoiceMail
	
# Add AppLock
PRODUCT_PACKAGES += \
    AppLock

# Add HongbaoAssistant
PRODUCT_PACKAGES += \
    HongbaoAssistant

# CHN DM
PRODUCT_PACKAGES += \
    DeviceManagement

# Setup SmartManager chn
PRODUCT_PACKAGES += \
    -SmartManager_v5 \
    SmartManager_v5_CN \
    -SmartManager_v6_DeviceSecurity \
    SmartManager_v6_DeviceSecurity_CN
	
# Add VIP mode
PRODUCT_PACKAGES += \
    Firewall

# Add Spam Call
PRODUCT_PACKAGES += \
    BstSpamCallService
	
# Add Tencent Phone Number Locator
PRODUCT_PACKAGES += \
    PhoneNumberLocatorService
	
# ChinaHiddenMenu
PRODUCT_PACKAGES += \
    ChinaHiddenMenu
	
# Add Screen Recorder
PRODUCT_PACKAGES += \
    ScreenRecorder
    
# Samsung Members for CHN models
PRODUCT_PACKAGES += \
    -SamsungMembers_CHN_P_Removable

# WeChatWifiService
PRODUCT_PACKAGES += \
	WeChatWifiService
	
#Add Tencent Security Wifi
PRODUCT_PACKAGES += \
    TencentWifiSecurity

# Add Smart MTP
PRODUCT_PACKAGES += \
    MtpShareApp

# Add Dynamic Lockscreen
PRODUCT_PACKAGES += \
    DynamicLockscreen

#add for MessagingExtension
PRODUCT_PACKAGES += \
    SamsungMessages_Extension_Chn_10.0

# Add China Activation 
PRODUCT_PACKAGES += \
    ActivationDevice_V2 \
    libactivation-jni 

PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/BaiduSearch/libBDoeminfo_baidusearch.so:system/lib64/libBDoeminfo_baidusearch.so

# add vips channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Vips/vipchannel.txt:system/etc/vipchannel.txt

# add UCWeb_OPEN channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/UCWeb/uconfig.ini:system/etc/uconfig.ini
 
# add netease channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/CTC/NetEaseNews/netease_news.channel:system/etc/netease_news.channel
  
# add YiDian channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/YiDian/yidian_preload.json:system/etc/yidian_preload.json

# add Meituan channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Meituan_OPEN/mtconfig.ini:system/etc/mtconfig.ini

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
	
# add Booking channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Booking/booking.data.aid:system/etc/booking.data.aid

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
# Package for CMCC Model
##################################################################

# Add Hancom Office Editor Install version
PRODUCT_PACKAGES += \
    HancomOfficeEditor_Install	

# add Autonavi NLP
PRODUCT_PACKAGES += \
    NetworkLocation_Autonavi

# 3rd party Application for CMCC model
PRODUCT_PACKAGES += \
    CM139Mail \
    Cm10086 \
    CMEssential14 \
    CMAndCloud \
    CMAutoMail \
    IflyteckIME \
    MiguReader \
    MiguVideo \
    MobileMarket \
    MMSafe63 \
    MobileMusic \
    TencentVideo \
    TodayHeadline \
    UCWeb \
    ApplicationAccounts \
    HeFetion \
    BaiduSearch_CMCC \
    BackuprestoreCMCC \
    CMCCWelfare \
    Aweme \
    WechatPluginMiniApp
 
# Remove xtra_t_app for CHN model
PRODUCT_PACKAGES += \
    -xtra_t_app
###################################################################
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
    
# Remove YourPhoneCompanion
PRODUCT_PACKAGES += \
    -YourPhone_P1_5

# Remove WindowsLinkService
PRODUCT_PACKAGES += \
    -LinkToWindowsService

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

# Remove Upday
PRODUCT_PACKAGES += \
    -Upday

# Add SoftsimService
PRODUCT_PACKAGES += \
    SoftsimService_V41

# Add softsimd daemon
PRODUCT_PACKAGES += \
    libcommapiaidl.so \
    softsimd

# Add SamsungDataStore
#PRODUCT_PACKAGES += \
#    SamsungDataStore

# Add CHN GameLauncher
PRODUCT_PACKAGES += \
    -GameHome \
    GameHome_CHN

#Add BixbyTouch
PRODUCT_PACKAGES += \
    BixbyTouch

# Add CHN PermissionController
PRODUCT_PACKAGES += \
    PermissionController_CHN

# Remove Global Goals
PRODUCT_PACKAGES += \
    -GlobalGoals

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_CHN.mk
endif
