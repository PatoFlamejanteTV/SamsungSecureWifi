QCOM_PRODUCT_DEVICE := crownqltedcm

$(call inherit-product, device/samsung/crownqltedcm/device.mk)
include vendor/samsung/configs/crownqlte_common/crownqlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/crownqltedcm/gms_crownqltedcm.mk
endif

# FactoryBinary doesn't need DCM apps
ifneq ($(SEC_FACTORY_BUILD),true)
    include vendor/samsung/configs/crownqltedcm/crownqltedcm_apps.mk
endif

PRODUCT_NAME := crownqltedcm
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SC-01L
PRODUCT_FINGERPRINT_TYPE := jpn_com

include vendor/samsung/build/localelist/SecLocale_JPN.mk


# VoiceRecorder
#PRODUCT_PACKAGES += \
#   VoiceNote_5.0  

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# Remove Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub

# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Samsung Japanese Keyboard
PRODUCT_PACKAGES += \
    SamsungIMEv3.5DCM \
    iWnnIME_Mushroom \
    -SamsungIMEv3.5

# Dictionary
PRODUCT_PACKAGES += \
     JpnDioDict4
     
# Remove eSE features
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp

#[FeliCa] ADD Start
PRODUCT_PACKAGES += \
    FeliCaLock \
    FeliCaRemoteLockDCM \
    FeliCaTest \
    MobileFeliCaClient \
    MobileFeliCaMenuMainApp \
    MobileFeliCaSettingApp \
    MobileFeliCaWebPlugin \
    MobileFeliCaWebPluginBoot \
    sysconfig_jpn_dcm.xml
#[FeliCa] ADD End

# SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Remove SamsungConnect
PRODUCT_PACKAGES += \
    -SamsungConnect
