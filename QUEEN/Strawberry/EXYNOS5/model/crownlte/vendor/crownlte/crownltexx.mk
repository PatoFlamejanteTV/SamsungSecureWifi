$(call inherit-product, device/samsung/crownlte/device.mk)
include vendor/samsung/configs/crownlte_common/crownlte_common.mk

include build/target/product/product_launched_with_o_mr1.mk

#include vendor/samsung/configs/crownlte/gms_crownltexx.mk

PRODUCT_NAME := crownltexx
PRODUCT_DEVICE := crownlte
PRODUCT_MODEL := SM-N960F

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast