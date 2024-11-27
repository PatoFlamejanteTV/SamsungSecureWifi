$(call inherit-product, device/samsung/crownlte/device.mk)
include vendor/samsung/configs/crownlte_common/crownlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/crownlte/gms_crownltexx.mk
endif

PRODUCT_NAME := crownltexx
PRODUCT_DEVICE := crownlte
PRODUCT_MODEL := SM-N960F

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    imsmanager \
    ImsTelephonyService \
    imsservice \
    vsimmanager \
    vsimservice \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imscoremanager \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    RcsSettings
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00


# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel


# Crane
PRODUCT_PACKAGES += \
    Crane

# SetupWizard Bixby
PRODUCT_PACKAGES +=  \
    SuwScriptPlayer

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
