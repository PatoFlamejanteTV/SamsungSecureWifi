QCOM_PRODUCT_DEVICE := d2q

$(call inherit-product, device/samsung/d2q/device.mk)
include vendor/samsung/configs/d2q_common/d2q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/d2q/gms_d2qrkt.mk
endif

PRODUCT_NAME := d2qrkt
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N975C

include vendor/samsung/build/localelist/SecLocale_JPN.mk



# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
# Add Samsung+ removable 12.01.08.7 as of July 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# Add CellBroadcastReceiver
PRODUCT_PACKAGES += \
    SecCellBroadcastReceiver    

# Removing WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Add Spotify
PRODUCT_PACKAGES += \
    Spotify

# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Remove eSE features
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

#[FeliCa] ADD Start
PRODUCT_PACKAGES += \
    FeliCaLock \
    FeliCaTest \
    MobileFeliCaClient \
    MobileFeliCaMenuMainApp \
    MobileFeliCaSettingApp \
    MobileFeliCaWebPlugin \
    MobileFeliCaWebPluginBoot \
    sysconfig_jpn_kdi.xml
#[FeliCa] ADD End
