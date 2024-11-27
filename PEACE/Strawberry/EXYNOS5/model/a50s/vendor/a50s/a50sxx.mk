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

# Add Samsung TTS
PRODUCT_PACKAGES += smt_vi_VN_f00

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# Add SamsungStubAppMini
PRODUCT_PACKAGES += \
    SamsungPayStubMini

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
    
# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService
endif

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add Crane
PRODUCT_PACKAGES += \
  Crane

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_P

# Add AirtelStub Solution
PRODUCT_PACKAGES += \
    AirtelStub

#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter
	
# Add QRAgent
PRODUCT_PACKAGES += \
QRAgent

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

MSPAPP_ADDITIONAL_DAT_LIST := SM-A505FN SM-A505FM

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################	

# Ramen common
    include vendor/samsung/hardware/gnss/slsi/ramen/common_evolution.mk

# Ramen GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
endif
