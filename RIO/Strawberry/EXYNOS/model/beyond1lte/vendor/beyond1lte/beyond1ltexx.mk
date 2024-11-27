$(call inherit-product, device/samsung/beyond1lte/device.mk)
include vendor/samsung/configs/beyond1lte_common/beyond1lte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyond1lte/gms_beyond1ltexx.mk
endif

PRODUCT_NAME := beyond1ltexx
PRODUCT_DEVICE := beyond1lte
PRODUCT_MODEL := beyond1ltexx

# Add Spotify
PRODUCT_PACKAGES += \
    Spotify

# add FortniteInstaller
PRODUCT_PACKAGES += \
    FortniteInstaller

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
