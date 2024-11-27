QCOM_PRODUCT_DEVICE := a6pltecmcc

$(call inherit-product, device/samsung/a6pltecmcc/device.mk)
include vendor/samsung/configs/a6plte_common/a6plte_common.mk

include build/target/product/product_launched_with_o.mk


# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a6pltecmcc/gms_a6pltezm.mk
endif

PRODUCT_NAME := a6pltezm
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A6058

include vendor/samsung/build/localelist/SecLocale_CHN.mk

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# Samsung Members for CHN models
PRODUCT_PACKAGES += \
    -SamsungMembers_Removable \
    SamsungMembers_CHN_Removable

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add Airtel Stub Solution
PRODUCT_PACKAGES += \
    AirtelStub
# Add AppLock
PRODUCT_PACKAGES += \
    AppLock
	
# Add Memory Solutions
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O
	
# Add Screen Recorder
PRODUCT_PACKAGES += \
    ScreenRecorder

# Add SMusic
PRODUCT_PACKAGES += \
    music.xml \
    SMusic

# TencentWifiSecurity
PRODUCT_PACKAGES += \
    TencentWifiSecurity

# add Autonavi NLP
PRODUCT_PACKAGES += \
    NetworkLocation_Autonavi

# add Baidu FLP
PRODUCT_PACKAGES += \
    FusedLocation_Baidu
PRELOAD_LOCAL_PATH := applications/par/edp/CHN/FusedLocationBaidu/
PRODUCT_COPY_FILES += \
    $(PRELOAD_LOCAL_PATH)/lib/arm/liblocSDK7.so:system/lib/liblocSDK7.so \
    $(PRELOAD_LOCAL_PATH)/lib/arm/libSGPDRLib.so:system/lib/libSGPDRLib.so

#CHN DM
PRODUCT_PACKAGES += \
    DeviceManagement

# Add Tencent Phone Number Locator
PRODUCT_PACKAGES += \
    PhoneNumberLocatorService

# Add Spam Call
PRODUCT_PACKAGES += \
    BstSpamCallService

# ChinaHiddenMenu
PRODUCT_PACKAGES += \
    ChinaHiddenMenu	
	
# Setup SmartManager chn
PRODUCT_PACKAGES += \
    -SmartManager_v5 \
    SmartManager_v5_CN \
    -SmartManager_v6_DeviceSecurity \
    SmartManager_v6_DeviceSecurity_CN
	
# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoList2016

# Add Microsoft OfficeMobile Stub
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub

# Add Hancom Office Editor Install version
PRODUCT_PACKAGES += \
    HancomOfficeEditor_Install

#Add BackupAndRestore for CMCC model
PRODUCT_PACKAGES += \
    BackupAndRestore_R

# 3rd party Application for CMCC model
PRODUCT_PACKAGES += \
    Cm10086 \
    CMAutoMail \
    CMEssential14 \
    MobileMarket \
    MMSafe63 \
    MiguReader \
    Meituan \
    TodayHeadline \
    UCWeb \
    TencentVideo \
    ShuqiReader \
    IflyteckIME \
    MiguVideo \
    CMAndCloud \
    CM139Mail \
    HeFetion \
    MobileMusic

PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/BaiduMap/libBDoeminfo_baidu.so:system/lib64/libBDoeminfo_baidu.so

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

#Add VIP mode
PRODUCT_PACKAGES += \
    Firewall

# For MSP APP of NFC One binary
MSPAPP_ADDITIONAL_DAT_LIST := SM-A605F

# Arrange YellowPage
PRODUCT_PACKAGES += \
    SamsungYellowPage
	
# Add for MessagingExtension
PRODUCT_PACKAGES += \
    SamsungMessages_Extension_Chn_10.0

# Add S Assistant
PRODUCT_PACKAGES += \
    SAssistant_downloadable
PRODUCT_COPY_FILES += \
    applications/par/idp/SAssistant/sreminder:system/etc/sreminder

# Add SoftsimService
PRODUCT_PACKAGES += \
    SoftsimService_V41

# Add softsimd daemon
PRODUCT_PACKAGES += \
    libcommapiaidl.so \
    softsimd

# Removing ChromeCustomizations
PRODUCT_PACKAGES += \
    -ChromeCustomizations

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_9.0_Removable \
    SBrowser_9.0

# Add Smart MTP
PRODUCT_PACKAGES += \
    MtpShareApp

# Add HongbaoAssistant
PRODUCT_PACKAGES += \
    HongbaoAssistant

# Add China Activation 
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
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
