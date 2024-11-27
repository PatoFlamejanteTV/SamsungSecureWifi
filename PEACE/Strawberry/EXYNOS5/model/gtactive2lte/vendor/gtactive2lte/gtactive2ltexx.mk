$(call inherit-product, device/samsung/gtactive2lte/device.mk)
include vendor/samsung/configs/gtactive2lte_common/gtactive2lte_common.mk

include build/target/product/product_launched_with_n_mr1.mk



include vendor/samsung/configs/gtactive2lte/gms_gtactive2ltexx.mk

PRODUCT_NAME := gtactive2ltexx
PRODUCT_DEVICE := gtactive2lte
PRODUCT_MODEL := SM-T395

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

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
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00