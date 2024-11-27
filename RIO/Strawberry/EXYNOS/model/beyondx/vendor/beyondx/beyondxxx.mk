$(call inherit-product, device/samsung/beyondx/device.mk)
include vendor/samsung/configs/beyondx_common/beyondx_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyondx/gms_beyondxxx.mk
endif

PRODUCT_NAME := beyondxxx
PRODUCT_DEVICE := beyondx
PRODUCT_MODEL := beyondxxx

# Add Spotify
PRODUCT_PACKAGES += \
    Spotify

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
