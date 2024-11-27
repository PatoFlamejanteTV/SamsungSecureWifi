$(call inherit-product, device/samsung/j6lte/device.mk)
include vendor/samsung/configs/j6lte_common/j6lte_common.mk

include build/target/product/product_launched_with_o.mk

include vendor/samsung/configs/j6lte/gms_j6lteser.mk

PRODUCT_NAME := j6lteser
PRODUCT_DEVICE := j6lte
PRODUCT_MODEL := SM-J600F

include vendor/samsung/build/localelist/SecLocale_SER.mk

# IMS START
PRODUCT_PACKAGES += \
    imsmanager

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

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

# Add Samsung TTS
PRODUCT_PACKAGES += smt_pl_PL_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
