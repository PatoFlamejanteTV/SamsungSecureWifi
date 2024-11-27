$(call inherit-product, device/samsung/a30s/device.mk)
include vendor/samsung/configs/a30s_common/a30s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a30s/gms_a30sxx.mk
endif

PRODUCT_NAME := a30sxx
PRODUCT_DEVICE := a30s
PRODUCT_MODEL := SM-A307FN

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio
	
#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter
	
# Add Helo app tracking file changes
PRODUCT_PACKAGES += \
    pre_install.appsflyer
	
# Add Booking tracking file
PRODUCT_PACKAGES += \
    .booking.data.aid
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O
	
# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

# Add Samsung TTS
PRODUCT_PACKAGES += smt_hi_IN_f00
PRODUCT_PACKAGES += smt_en_IN_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add SamsungStubAppMini for India
PRODUCT_PACKAGES += \
    SamsungPayStubMini

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