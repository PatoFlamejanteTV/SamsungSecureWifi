$(call inherit-product, device/samsung/a50/device.mk)
include vendor/samsung/configs/a50_common/a50_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a50/gms_a50sq.mk
endif

PRODUCT_NAME := a50sq
PRODUCT_DEVICE := a50
PRODUCT_MODEL := SM-A505U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
else
PRODUCT_PACKAGES += \
    -HybridRadio
endif

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
PRODUCT_PACKAGES += smt_hi_IN_f00
PRODUCT_PACKAGES += smt_en_IN_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# Add Ignite App Package
PRODUCT_PACKAGES += \
    IGNITE

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock	

# Add Secure Wi-Fi 
PRODUCT_PACKAGES += \ 
    Fast

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_P

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh



# Ramen common
include vendor/samsung/hardware/gnss/slsi/ramen/common_evolution_64.mk

# Ramen GNSS configuration
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

# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual
