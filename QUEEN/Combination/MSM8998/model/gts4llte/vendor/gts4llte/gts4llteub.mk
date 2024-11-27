include vendor/samsung/configs/gts4llte/gts4lltexx.mk

PRODUCT_NAME := gts4llteub
PRODUCT_MODEL := SM-T835

# Add Samsung TTS
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += -smt_en_GB_f00
PRODUCT_PACKAGES += -smt_de_DE_f00
PRODUCT_PACKAGES += -smt_fr_FR_f00
PRODUCT_PACKAGES += -smt_it_IT_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_es_ES_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
