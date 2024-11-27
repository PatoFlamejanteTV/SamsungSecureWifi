$(call inherit-product, device/samsung/a6elteue/device.mk)
include vendor/samsung/configs/a6elte_common/a6elte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately 
include vendor/samsung/configs/a6elteue/gms_a6elteue.mk
endif

# define google api level for google approval

PRODUCT_NAME := a6elteue
PRODUCT_DEVICE := a6elteue
PRODUCT_MODEL := SM-A600U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Radio App Package
ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

# Add Hancom Viewer for Smartphone
PRODUCT_PACKAGES += \
    HancomOfficeViewer

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
