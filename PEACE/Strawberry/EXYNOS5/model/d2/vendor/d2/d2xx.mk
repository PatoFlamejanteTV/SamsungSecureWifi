$(call inherit-product, device/samsung/d2/device.mk)
include vendor/samsung/configs/d2_common/d2_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/d2/gms_d2xx.mk
endif

PRODUCT_NAME := d2xx
PRODUCT_DEVICE := d2
PRODUCT_MODEL := SM-N975F

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

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings


# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################

