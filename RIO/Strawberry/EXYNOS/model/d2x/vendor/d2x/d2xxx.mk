$(call inherit-product, device/samsung/d2x/device.mk)
include vendor/samsung/configs/d2x_common/d2x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/d2x/gms_d2xxx.mk
endif

PRODUCT_NAME := d2xxx
PRODUCT_DEVICE := d2x
PRODUCT_MODEL := SM-N976B

	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast