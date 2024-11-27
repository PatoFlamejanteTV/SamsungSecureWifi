QCOM_PRODUCT_DEVICE := r5q

$(call inherit-product, device/samsung/r5q/device.mk)
include vendor/samsung/configs/r5q_common/r5q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r5q/gms_r5que.mk
endif

#include vendor/samsung/configs/beyond2qltesq/gms_beyond2qltesq.mk

PRODUCT_NAME := r5que
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G770U1

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Add USA SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA

# Setup SBrowser Non-Removable
PRODUCT_PACKAGES += \
    -SBrowser_13.0_Removable \
    SBrowser_13.0
	
# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
# Add Samsung DigitalKey in "Settings - Biometrics and security"
# DigitalKey (Supported Country) : EUR, KOR, USA, CAN, SEA/Oceania
# -DigitalKey (Not supported countries) : CHN, JPN
PRODUCT_PACKAGES += \
    DigitalKey
		
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast