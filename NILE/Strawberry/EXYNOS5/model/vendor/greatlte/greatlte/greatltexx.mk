$(call inherit-product, device/samsung/greatlte/device.mk)
include vendor/samsung/configs/greatlte_common/greatlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

# Include gms packages depending on product seperately
include vendor/samsung/configs/greatlte/gms_greatltexx.mk

PRODUCT_NAME := greatltexx
PRODUCT_DEVICE := greatlte
PRODUCT_MODEL := SM-N950F
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    imsmanager \
    imsservice \
    vsimmanager \
    vsimservice \
    secimshttpclient \
    ImsLogger+ \
    ImsSettings \
    libsec-ims \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    RcsSettings

# Crane
PRODUCT_PACKAGES += \
  Crane

# Add GalaxyCare
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_hi_IN_f00
PRODUCT_PACKAGES += smt_en_IN_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_zh_TW_f00

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES +=  \
    Omc

# OMC
PRODUCT_PACKAGES += \
    OMCAgent \
    PlayAutoInstallConfig

# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel

# Remove NaverV Panel
PRODUCT_PACKAGES += \
    -NaverV_N

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
