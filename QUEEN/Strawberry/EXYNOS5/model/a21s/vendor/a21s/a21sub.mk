include vendor/samsung/configs/a21s/a21snsxx.mk

PRODUCT_NAME := a21sub
PRODUCT_MODEL := SM-A217M

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += -smt_en_GB_f00
PRODUCT_PACKAGES += -smt_de_DE_f00
PRODUCT_PACKAGES += -smt_fr_FR_f00
PRODUCT_PACKAGES += -smt_it_IT_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_es_ES_f00

# Add AsphaltNitro for CHO, CHK,CHP,CHQ
PRODUCT_PACKAGES += \
    AsphaltNitro
	
# Add 99taxis.com for ZTO
PRODUCT_PACKAGES += \
    99Taxis

# Add PreloadInstaller
PRODUCT_PACKAGES += \
    PreloadInstaller
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast