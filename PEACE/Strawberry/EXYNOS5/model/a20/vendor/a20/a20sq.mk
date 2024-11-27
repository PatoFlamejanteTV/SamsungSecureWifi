PRODUCT_COPY_FILES += device/samsung/a20_common/init.a20sq.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.samsungexynos7885.rc

$(call inherit-product, device/samsung/a20/device.mk)
include vendor/samsung/configs/a20_common/a20_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a20/gms_a20sq.mk
endif

# define google api level for google approval
include build/target/product/product_launched_with_p.mk

PRODUCT_NAME := a20sq
PRODUCT_DEVICE := a20
PRODUCT_MODEL := SM-A205U

include vendor/samsung/build/localelist/SecLocale_USA.mk



# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
