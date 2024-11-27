include vendor/samsung/configs/gta2xllte/gta2xlltexx.mk

PRODUCT_NAME := gta2xllteser
PRODUCT_MODEL := SM-T595

include vendor/samsung/build/localelist/SecLocale_SER.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_pl_PL_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_en_GB_f00
PRODUCT_PACKAGES += -smt_de_DE_f00
PRODUCT_PACKAGES += -smt_fr_FR_f00
PRODUCT_PACKAGES += -smt_it_IT_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_es_ES_f00

# Setup SBrowser removable
PRODUCT_PACKAGES += \
    -SBrowser_11.1 \
    SBrowser_11.1_Removable