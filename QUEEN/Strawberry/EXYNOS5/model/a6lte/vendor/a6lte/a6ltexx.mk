$(call inherit-product, device/samsung/a6lte/device.mk)
include vendor/samsung/configs/a6lte_common/a6lte_common.mk

#include vendor/samsung/configs/a6lte/gms_a6ltexx.mk

PRODUCT_NAME := a6ltexx
PRODUCT_DEVICE := a6lte
PRODUCT_MODEL := SM-A600FN

# IMS START
PRODUCT_PACKAGES += \
    imsmanager

ifeq ($(findstring ldu, $(TARGET_PRODUCT)),)
PRODUCT_PACKAGES += \
    imsservice \
    ImsTelephonyService \
    vsimmanager \
    vsimservice \
    NSDSWebApp \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    SimMobilityKit

ifneq ($(SEC_FACTORY_BUILD),true)
# IMS Telephony Feature
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.telephony.ims.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.telephony.ims.xml
endif
endif

include vendor/samsung/configs/ims/ims_common.mk
# IMS END

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
