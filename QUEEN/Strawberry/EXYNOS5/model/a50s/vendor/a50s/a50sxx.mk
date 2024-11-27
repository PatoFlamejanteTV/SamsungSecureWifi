$(call inherit-product, device/samsung/a50s/device.mk)
include vendor/samsung/configs/a50s_common/a50s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a50s/gms_a50sxx.mk
endif

PRODUCT_NAME := a50sxx
PRODUCT_DEVICE := a50s
PRODUCT_MODEL := SM-A507FN

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
	
# Crane
PRODUCT_PACKAGES += \
    Crane

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService
	
# Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter

# Add Samsung TTS
PRODUCT_PACKAGES += smt_vi_VN_f00

# Add Ignite App Package
PRODUCT_PACKAGES += \
    IGNITE

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock	

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_P

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast



# Ramen common
include vendor/samsung/hardware/gnss/slsi/ramen/common_evolution_64.mk

# GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
endif    

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

MSPAPP_ADDITIONAL_DAT_LIST := SM-A515FE SM-A515FT SM-A515FN SM-A515FM

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################	