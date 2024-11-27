$(call inherit-product, device/samsung/a51/device.mk)
include vendor/samsung/configs/a51_common/a51_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a51/gms_a51nsxx.mk
endif

PRODUCT_NAME := a51nsxx
PRODUCT_DEVICE := a51
PRODUCT_MODEL := SM-A515F

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
	
# Add Amazon Prime Video
PRODUCT_PACKAGES += \
     AmazonVideo_SWA
   
# Add Amazon Shopping
PRODUCT_PACKAGES += \
    Amazon_Shopping
	
# Add Dailyhunt
PRODUCT_PACKAGES += \
    Dailyhunt
	
# Add Helo
PRODUCT_PACKAGES += \
    Helo
	
# Add Booking.com
PRODUCT_PACKAGES += \
    Booking_SWA
	
# Add Snapchat
PRODUCT_PACKAGES += \
   Snapchat

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Ignite App Package
PRODUCT_PACKAGES += \
    IGNITE

# ++ LTN MobileTV
PRODUCT_PACKAGES += \
    MobileTV_LTN

PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.latin.dtv.rc:system/etc/init/init.dtv.rc
# -- LTN MobileTV

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock	

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

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

# Copy SetupIndiaServicesTnC Whitelist & Add SetupIndiaServicesTnC Packages
PRODUCT_COPY_FILES += \
    applications/par/idp/SetupIndiaServicesTnC/setupindiaservicestnc.json:/system/etc/setupindiaservicestnc.json    
PRODUCT_PACKAGES += \
    SetupIndiaServicesTnC
    
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################