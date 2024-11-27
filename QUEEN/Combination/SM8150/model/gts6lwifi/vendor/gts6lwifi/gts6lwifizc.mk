QCOM_PRODUCT_DEVICE := gts6lwifi

$(call inherit-product, device/samsung/gts6lwifi/device.mk)
include vendor/samsung/configs/gts6lwifi_common/gts6lwifi_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts6lwifi/gms_gts6lwifizc.mk
endif

PRODUCT_NAME := gts6lwifizc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T860

include vendor/samsung/build/localelist/SecLocale_CHN.mk

# Remove ImsSettings
PRODUCT_PACKAGES += \
    -ImsSettings


# Add China Activation
PRODUCT_PACKAGES += \
    ActivationDevice_V2 \
    libactivation-jni

# 3rd party Application for CHN model
PRODUCT_PACKAGES += \
    Ctrip \
    SReading \
    NovelStory \
    MJWeather \
    TencentVideoPad


# Add DeviceIdService
PRODUCT_PACKAGES += \
    DeviceIdService

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Add CHN PermissionController
PRODUCT_PACKAGES += \
    PermissionController_CHN

# Add HongbaoAssistant
PRODUCT_PACKAGES += \
    HongbaoAssistant

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# Remove SamsungDaily and Add BixbyHome_Disable
PRODUCT_PACKAGES += \
    -SamsungDaily \
    BixbyHome_Disable

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

#Add Tencent Security Wifi
PRODUCT_PACKAGES += \
    TencentWifiSecurity

# Samsung Members for CHN models
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SamsungMembers_CHN_P_Removable

# Replace as Microsoft OfficeMobile China Stub.
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub \
    OfficeMobile_SamsungStubChina

# add Baidu NLP
PRODUCT_PACKAGES += \
    OfflineNetworkLocation_Baidu

# Remove PENUP
PRODUCT_PACKAGES += \
	-PENUP

# Remove OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

# Remove Dialer only in China model
PRODUCT_PACKAGES += \
    -SamsungDialer

#Add PushServiceCN
PRODUCT_PACKAGES += \
    PushServiceCN

#Add TNCPageCN
PRODUCT_PACKAGES += \
    TNCPageCN

# Remove Samsung Messages only in China model
PRODUCT_PACKAGES += \
    -SamsungMessages_11 \

# Setup SmartManager chn
PRODUCT_PACKAGES += \
    -SmartManager_v5 \
    SmartManager_v5_CN \
    -SmartManager_v6_DeviceSecurity \
    SmartManager_v6_DeviceSecurity_CN

PRODUCT_PACKAGES += \
    SMusic

# Add Smart MTP
PRODUCT_PACKAGES += \
    MtpShareApp
