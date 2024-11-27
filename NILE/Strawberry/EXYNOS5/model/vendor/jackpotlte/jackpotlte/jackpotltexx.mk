$(call inherit-product, device/samsung/jackpotlte/device.mk)
include vendor/samsung/configs/jackpotlte_common/jackpotlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

# Include gms packages depending on product seperately
include vendor/samsung/configs/jackpotlte/gms_jackpotltexx.mk

PRODUCT_NAME := jackpotltexx
PRODUCT_DEVICE := jackpotlte
PRODUCT_MODEL := SM-A530F
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

PRODUCT_PACKAGES += \
    HybridRadio_N

# lassen GPS
include vendor/samsung/external/gps/slsi/lassen/common_evolution_d.mk

# OMC
PRODUCT_PACKAGES += \
    OMCAgent \
    PlayAutoInstallConfig

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add OneDrive
PRODUCT_PACKAGES += \
    OneDrive_Samsung_v3
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
