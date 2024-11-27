$(call inherit-product, device/samsung/a21s/device.mk)
include vendor/samsung/configs/a21s_common/a21s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a21s/gms_a21sxx.mk
endif

PRODUCT_NAME := a21sxx
PRODUCT_DEVICE := a21s
PRODUCT_MODEL := a21sxx

# Change default language on Factory binary
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_LOCALES := en_US $(filter-out en_US,$(PRODUCT_LOCALES))
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00


###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
