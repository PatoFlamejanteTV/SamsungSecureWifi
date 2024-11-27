include vendor/samsung/configs/r5q/r5qnaxx.mk

PRODUCT_NAME := r5qldu
PRODUCT_MODEL := SM-G770X

# Remove eSE features
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

#remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v11

# Remove Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast