PRODUCT_COPY_FILES += device/samsung/a10_common/init.a10sq.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.samsungexynos7885.rc

$(call inherit-product, device/samsung/a10/device.mk)
include vendor/samsung/configs/a10_common/a10_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a10/gms_a10sq.mk
endif

# define google api level for google approval
include build/target/product/product_launched_with_p.mk

PRODUCT_NAME := a10sq
PRODUCT_DEVICE := a10
PRODUCT_MODEL := SM-A105U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# Add Samsung TTS
PRODUCT_PACKAGES += smt_pl_PL_f00
PRODUCT_PACKAGES += smt_ru_RU_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
