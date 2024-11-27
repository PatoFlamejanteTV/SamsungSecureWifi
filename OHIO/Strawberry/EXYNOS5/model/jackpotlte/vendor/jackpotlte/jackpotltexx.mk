$(call inherit-product, device/samsung/jackpotlte/device.mk)
include vendor/samsung/configs/jackpotlte_common/jackpotlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

PRODUCT_NAME := jackpotltexx
PRODUCT_DEVICE := jackpotlte
PRODUCT_MODEL := SM-A530F


# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/jackpotlte/gms_jackpotltexx.mk
endif

ifneq ($(SEC_FACTORY_BUILD),true)
# Add SHealth
PRODUCT_PACKAGES += \
    SHealth5 \
    HealthService
endif

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_O

# Add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# OMC
PRODUCT_PACKAGES += \
    OMCAgent5 \
    PlayAutoInstallConfig

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Add Handwriting
PRODUCT_PACKAGES += \
    HandwritingService \
    libSDKRecognitionText    
# Add ProductSearch
PRODUCT_PACKAGES += \
    ProductSearch

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

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
