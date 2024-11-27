$(call inherit-product, device/samsung/a10/device.mk)
include vendor/samsung/configs/a10_common/a10_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
#include vendor/samsung/configs/a10/gms_a10xx.mk
endif

PRODUCT_NAME := a10xx
PRODUCT_DEVICE := a10
PRODUCT_MODEL := SM-A105FN

# Add Radio App Package
#PRODUCT_PACKAGES += \
    #HybridRadio
	
#Add LogWriter Solution
#PRODUCT_PACKAGES += \
    #LogWriter
	
# Add Helo app tracking file changes
PRODUCT_PACKAGES += \
    pre_install.appsflyer
	
# Add Booking tracking file
PRODUCT_PACKAGES += \
    .booking.data.aid
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_hi_IN_f00
PRODUCT_PACKAGES += smt_en_IN_f00

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add SamsungStubAppMini for India
#PRODUCT_PACKAGES += \
    #SamsungPayStubMini

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Crane
PRODUCT_PACKAGES += \
    Crane	

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif
