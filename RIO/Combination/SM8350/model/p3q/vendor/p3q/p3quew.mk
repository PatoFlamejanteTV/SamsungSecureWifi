QCOM_PRODUCT_DEVICE := p3q

$(call inherit-product, device/samsung/p3q/device.mk)
include vendor/samsung/configs/p3q_common/p3q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/p3q/gms_p3quew.mk
endif

PRODUCT_NAME := p3quew
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G998U1

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Removing WebManual
PRODUCT_PACKAGES += \
    -WebManual
	
# Packages included only for eng or userdebug builds, previously debug tagged
PRODUCT_PACKAGES_DEBUG += \
    Keystring \
    AngryGPS \

# Add UnifiedTetheringProvision for ATT/VZW/AIO
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Add Verizon SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar
	
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
