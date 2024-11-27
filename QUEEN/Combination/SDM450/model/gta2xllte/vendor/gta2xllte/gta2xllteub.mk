include vendor/samsung/configs/gta2xllte/gta2xlltexx.mk

PRODUCT_NAME := gta2xllteub
PRODUCT_MODEL := SM-T595

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += -smt_en_GB_f00
PRODUCT_PACKAGES += -smt_de_DE_f00
PRODUCT_PACKAGES += -smt_fr_FR_f00
PRODUCT_PACKAGES += -smt_it_IT_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_es_ES_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
