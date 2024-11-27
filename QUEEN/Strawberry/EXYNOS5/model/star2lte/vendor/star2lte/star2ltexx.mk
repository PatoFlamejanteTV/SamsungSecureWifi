$(call inherit-product, device/samsung/star2lte/device.mk)
include vendor/samsung/configs/star2lte_common/star2lte_common.mk

#include vendor/samsung/configs/star2lte/gms_star2ltexx.mk

PRODUCT_NAME := star2ltexx
PRODUCT_DEVICE := star2lte
PRODUCT_MODEL := SM-G965F

# Expway eMBMS package
PRODUCT_PACKAGES += \
    EweMBMSServer_TEL
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast