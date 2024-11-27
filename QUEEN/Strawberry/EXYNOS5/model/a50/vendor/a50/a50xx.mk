include vendor/samsung/configs/a50/a50dd.mk

PRODUCT_NAME := a50xx
PRODUCT_MODEL := SM-A505FN

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += -smt_hi_IN_f00
PRODUCT_PACKAGES += -smt_en_IN_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
