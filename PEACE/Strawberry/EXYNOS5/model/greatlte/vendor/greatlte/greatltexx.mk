$(call inherit-product, device/samsung/greatlte/device.mk)
include vendor/samsung/configs/greatlte_common/greatlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

include vendor/samsung/configs/greatlte/gms_greatltexx.mk

PRODUCT_NAME := greatltexx
PRODUCT_DEVICE := greatlte
PRODUCT_MODEL := SM-N950F

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# OMC
PRODUCT_PACKAGES += \
    OMCAgent5 \
    PlayAutoInstallConfig

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel

# Crane
PRODUCT_PACKAGES += \
    Crane
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast