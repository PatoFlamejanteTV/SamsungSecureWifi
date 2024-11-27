$(call inherit-product, device/samsung/jackpotlte/device.mk)
include vendor/samsung/configs/jackpotlte_common/jackpotlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately 
include vendor/samsung/configs/jackpotlte/gms_jackpotltexx.mk

# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings

# Crane
PRODUCT_PACKAGES += \
    Crane
endif

# define google api level for google approval

PRODUCT_NAME := jackpotltexx
PRODUCT_DEVICE := jackpotlte
PRODUCT_MODEL := SM-A530F

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# OMC
PRODUCT_PACKAGES += \
    OMCAgent5

# PAI Stub
PRODUCT_PACKAGES += \
    PlayAutoInstallConfig

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast