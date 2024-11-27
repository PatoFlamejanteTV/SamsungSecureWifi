$(call inherit-product, device/samsung/a40/device.mk)
include vendor/samsung/configs/a40_common/a40_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a40/gms_a40xx.mk
endif

PRODUCT_NAME := a40xx
PRODUCT_DEVICE := a40
PRODUCT_MODEL := SM-A405FN

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio
	
#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices
	
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
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add SamsungStubAppMini for India
PRODUCT_PACKAGES += \
    SamsungPayStubMini

# Crane
PRODUCT_PACKAGES += \
    Crane	

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif