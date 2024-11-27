$(call inherit-product, device/samsung/beyond2lte/device.mk)
include vendor/samsung/configs/beyond2lte_common/beyond2lte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyond2lte/gms_beyond2ltexx.mk
endif

PRODUCT_NAME := beyond2ltexx
PRODUCT_DEVICE := beyond2lte
PRODUCT_MODEL := beyond2ltexx

# Add Spotify
PRODUCT_PACKAGES += \
    Spotify

# add FortniteInstaller
PRODUCT_PACKAGES += \
    FortniteInstaller

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
