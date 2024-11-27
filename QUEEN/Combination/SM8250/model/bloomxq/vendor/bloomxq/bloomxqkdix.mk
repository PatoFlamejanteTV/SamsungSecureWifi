QCOM_PRODUCT_DEVICE := bloomxq

$(call inherit-product, device/samsung/bloomxq/device.mk)
include vendor/samsung/configs/bloomxq_common/bloomxq_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/bloomxq/gms_bloomxqkdix.mk
endif

PRODUCT_NAME := bloomxqkdix
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := GTH
PRODUCT_FINGERPRINT_TYPE := jpn_kdi

# for KDDI Locales
#include vendor/samsung/build/localelist/SecLocale_JPN.mk
PRODUCT_LOCALES := ja_JP en_US ko_KR pt_BR vi_VN zh_CN es_ES fr_FR in_ID ml_IN ar_AE

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.1_Removable \
    SBrowser_11.1

# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual 

# remove Samsung Pay Stub
PRODUCT_PACKAGES += \
    -SamsungPayStub
    
# SamsungEuicc Service and Client
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccService \
    EuiccClient
endif

# Packages included only for eng or userdebug builds, previously debug tagged
PRODUCT_PACKAGES_DEBUG += \
    Keystring \
    AngryGPS \

# Remove USP for user delivery
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES +=  \
    -ARZone
endif

# Samsung Japanese Keyboard
PRODUCT_PACKAGES += \
    HoneyBoardJPN \
    iWnnIME_Mushroom \
    -HoneyBoard

# Remove Bixby
PRODUCT_PACKAGES += \
    -Bixby

# Remove Samsung Messages
PRODUCT_PACKAGES += \
    -SamsungMessages_11

# Add CellBroadcastReceiver
PRODUCT_PACKAGES += \
    SecCellBroadcastReceiver

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_JPN.mk
endif
###############################################################
