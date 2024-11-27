$(call inherit-product, device/samsung/d2x/device.mk)
include vendor/samsung/configs/d2x_common/d2x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/d2x/gms_d2xxx.mk
endif

PRODUCT_NAME := d2xxx
PRODUCT_DEVICE := d2x
PRODUCT_MODEL := SM-N976B


# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService


# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add BlockchainBasicKit
PRODUCT_PACKAGES += \
    BlockchainBasicKit



###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################

