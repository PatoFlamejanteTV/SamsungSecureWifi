$(call inherit-product, device/samsung/a50/device.mk)
include vendor/samsung/configs/a50_common/a50_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a50/gms_a50dd.mk
endif

PRODUCT_NAME := a50dd
PRODUCT_DEVICE := a50
PRODUCT_MODEL := SM-A505F

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

# Add Secure Wi-Fi 
PRODUCT_PACKAGES += \ 
    Fast
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_hi_IN_f00
PRODUCT_PACKAGES += smt_en_IN_f00

# Add SamsungStubAppMini for India
PRODUCT_PACKAGES += \
    SamsungPayStubMini
	
	
# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock	

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

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


#Add Booking.com Solution
PRODUCT_PACKAGES += \
	.booking.data.aid

#Add Helo Solution
PRODUCT_PACKAGES += \
	pre_install.appsflyer