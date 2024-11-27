$(call inherit-product, device/samsung/d2s/device.mk)
include vendor/samsung/configs/d2s_common/d2s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/d2s/gms_d2sxx.mk
endif

PRODUCT_NAME := d2sxx
PRODUCT_DEVICE := d2s
PRODUCT_MODEL := SM-N975F

	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast