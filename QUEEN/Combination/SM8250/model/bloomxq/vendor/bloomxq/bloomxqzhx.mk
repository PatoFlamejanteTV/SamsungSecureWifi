QCOM_PRODUCT_DEVICE := bloomxq

$(call inherit-product, device/samsung/bloomxq/device.mk)
include vendor/samsung/configs/bloomxq_common/bloomxq_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/bloomxq/gms_bloomxqzhx.mk
endif

PRODUCT_NAME := bloomxqzhx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-F7070

include vendor/samsung/build/localelist/SecLocale_CHN.mk
   
#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert   
   
# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00
PRODUCT_PACKAGES += smt_en_GB_f00

# Add Samsung+ removable 12.01.08.7 as of Aug 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Remove DeviceSecurity for USA single
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# UnifiedTetheringProvision for ATT, AIO, VZW
PRODUCT_PACKAGES += \
	UnifiedTetheringProvision
	
# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    MnoDmClient \
    MnoDmViewer \
    mnodm.rc \
    SDMHiddenMenu

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

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
