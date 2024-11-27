$(call inherit-product, device/samsung/a32x/device.mk)
include vendor/samsung/configs/a32x_common/a32x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a32x/gms_a32xxx.mk
endif

PRODUCT_NAME := a32xxx
PRODUCT_DEVICE := a32x
PRODUCT_MODEL := a32xxx

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

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
