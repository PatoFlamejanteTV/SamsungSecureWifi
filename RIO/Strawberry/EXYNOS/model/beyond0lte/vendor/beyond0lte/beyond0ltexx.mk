$(call inherit-product, device/samsung/beyond0lte/device.mk)
include vendor/samsung/configs/beyond0lte_common/beyond0lte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyond0lte/gms_beyond0ltexx.mk
endif

PRODUCT_NAME := beyond0ltexx
PRODUCT_DEVICE := beyond0lte
PRODUCT_MODEL := beyond0ltexx

# Add Spotify
PRODUCT_PACKAGES += \
    Spotify

# add FortniteInstaller
PRODUCT_PACKAGES += \
    FortniteInstaller

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
