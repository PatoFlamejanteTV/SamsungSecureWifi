$(call inherit-product, device/samsung/j7velte/device.mk)
include vendor/samsung/configs/j7velte_common/j7velte_common.mk

include build/target/product/product_launched_with_n.mk


include vendor/samsung/configs/j7velte/gms_j7veltedx.mk

PRODUCT_NAME := j7veltedx
PRODUCT_DEVICE := j7velte
PRODUCT_MODEL := SM-J701F

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Crane
PRODUCT_PACKAGES += \
    Crane

#Add AirtelStub Solution
PRODUCT_PACKAGES += \
    AirtelStub

#Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# Add Samsung TTS
PRODUCT_PACKAGES += smt_vi_VN_f00
