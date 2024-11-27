include vendor/samsung/configs/r1q/r1qxx.mk

PRODUCT_NAME := r1qldu
PRODUCT_MODEL := SM-A805X

# recovery for ldu
RECOVERY_FOR_LDU_BINARAY := true
RECOVERY_DELETE_USER_DATA := true

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

PRODUCT_PACKAGES += \
    SMusic

# Removed package
PRODUCT_PACKAGES += \
    -SamsungPass

# Remove SamsungPassAutofill
PRODUCT_PACKAGES += \
    -SamsungPassAutofill_v1

# Remove Secure Wi-Fi
PRODUCT_PACKAGES += \
    -Fast

