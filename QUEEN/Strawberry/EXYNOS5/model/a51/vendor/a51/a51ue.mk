$(call inherit-product, device/samsung/a51/device.mk)
include vendor/samsung/configs/a51_common/a51_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a51/gms_a51ue.mk
endif

PRODUCT_NAME := a51ue
PRODUCT_DEVICE := a51
PRODUCT_MODEL := SM-A515U1

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# Ramen common
include vendor/samsung/hardware/gnss/slsi/ramen/common_evolution_us_64.mk

# Ramen GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else 
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.na.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.na.cdma.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps_CDMA.cfg
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################	