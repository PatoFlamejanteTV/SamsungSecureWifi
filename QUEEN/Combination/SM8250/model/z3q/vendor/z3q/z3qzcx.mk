QCOM_PRODUCT_DEVICE := z3q

$(call inherit-product, device/samsung/z3q/device.mk)
include vendor/samsung/configs/z3q_common/z3q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/z3q/gms_z3qzcx.mk
endif

PRODUCT_NAME := z3qzcx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G9880

include vendor/samsung/build/localelist/SecLocale_CHN.mk

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

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# UnifiedTetheringProvision for ATT, AIO, VZW
PRODUCT_PACKAGES += \
	UnifiedTetheringProvision

# Replace as Microsoft OfficeMobile China Stub.
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub \
    OfficeMobile_SamsungStubChina

# Remove Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    -FBAppManager_NS \
    -appmanager.conf \
    -FBInstaller_NS \
    -FBServices

# Remove Netflix apps
PRODUCT_PACKAGES += \
    -Netflix_stub \
    -Netflix_activationCommon

# add Baidu NLP
PRODUCT_PACKAGES += \
    OfflineNetworkLocation_Baidu

# Samsung Members for CHN models
PRODUCT_PACKAGES += \
    SamsungMembers_CHN_P_Removable

# add Baidu FLP
PRODUCT_PACKAGES += \
    FusedLocation_Baidu

# 3rd party Application for CHN model
PRODUCT_PACKAGES += \
    QQmusic

# Add Smart MTP, only for China open & CMCC models
PRODUCT_PACKAGES += \
    MtpShareApp

# Add S Assistant
PRODUCT_PACKAGES += \
    SAssistant_downloadable
PRODUCT_COPY_FILES += \
    applications/par/idp/SAssistant/sreminder:system/etc/sreminder

# Add HongbaoAssistant
PRODUCT_PACKAGES += \
    HongbaoAssistant

# WeChatWifiService
PRODUCT_PACKAGES += \
    WeChatWifiService    

#Add Tencent Security Wifi
PRODUCT_PACKAGES += \
    TencentWifiSecurity

# Setup SmartManager chn
PRODUCT_PACKAGES += \
    -SmartManager_v5 \
    SmartManager_v5_CN \
    -SmartManager_v6_DeviceSecurity \
    SmartManager_v6_DeviceSecurity_CN

#Add VIP mode
PRODUCT_PACKAGES += \
    Firewall

# Add Tencent Phone Number Locator
PRODUCT_PACKAGES += \
    PhoneNumberLocatorService

# Add YellowPage
PRODUCT_PACKAGES += \
    SamsungYellowPage

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Add CHN PermissionController
PRODUCT_PACKAGES += \
    PermissionController_CHN

PRELOAD_LOCAL_PATH := applications/par/edp/CHN/FusedLocationBaidu/
PRODUCT_COPY_FILES += \
    $(PRELOAD_LOCAL_PATH)/lib/arm/liblocSDK7.so:system/lib/liblocSDK7.so \
    $(PRELOAD_LOCAL_PATH)/lib/arm64/liblocSDK7.so:system/lib64/liblocSDK7.so \
    $(PRELOAD_LOCAL_PATH)/lib/arm64/libSGPDRLib.so:system/lib64/libSGPDRLib.so

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
