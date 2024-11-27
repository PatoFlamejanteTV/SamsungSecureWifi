$(call inherit-product, device/samsung/m12s/device.mk)
include vendor/samsung/configs/m12s_common/m12s_common.mk

#include vendor/samsung/configs/m12s/gms_m12sdd.mk

PRODUCT_NAME := m12sdd
PRODUCT_DEVICE := m12s
PRODUCT_MODEL := m12sdd

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
