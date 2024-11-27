$(call inherit-product, device/samsung/d1/device.mk)
include vendor/samsung/configs/d1_common/d1_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/d1/gms_d1xx.mk
endif

PRODUCT_NAME := d1xx
PRODUCT_DEVICE := d1
PRODUCT_MODEL := SM-N970F

	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast