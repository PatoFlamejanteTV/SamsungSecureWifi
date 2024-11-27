QCOM_PRODUCT_DEVICE := c2q

$(call inherit-product, device/samsung/c2q/device.mk)
include vendor/samsung/configs/c2q_common/c2q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/c2q/gms_c2qkdiw.mk
endif

PRODUCT_NAME := c2qkdiw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N986J

include vendor/samsung/build/localelist/SecLocale_JPN.mk


# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# Add Samsung+ removable 12.01.08.7 as of Aug 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# Remove DeviceSecurity for USA single
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
    
# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension
	
# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert	

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# UnifiedTetheringProvision for ATT, AIO, VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    MnoDmClient \
    MnoDmViewer \
    mnodm.rc \
    SDMHiddenMenu

# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# SamsungEuicc Service and Client
PRODUCT_PACKAGES += \
    EuiccService
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
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

#Adding RLL support for My Au
PRODUCT_PACKAGES += \
    RLLHelper