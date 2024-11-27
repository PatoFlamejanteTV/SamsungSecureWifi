include vendor/samsung/configs/a70q/a70qxx.mk

PRODUCT_NAME := a70qxtc
PRODUCT_MODEL := SM-A705MN

# Add Samsung TTS
PRODUCT_PACKAGES += smt_vi_VN_f00
PRODUCT_PACKAGES += -smt_en_GB_f00
PRODUCT_PACKAGES += -smt_de_DE_f00
PRODUCT_PACKAGES += -smt_fr_FR_f00
PRODUCT_PACKAGES += -smt_it_IT_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Remove LogWriter Solution
PRODUCT_PACKAGES += \
    -LogWriter

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

PRODUCT_PACKAGES += \
	UltraDataSaving_O

# Add DisneyMagicKingdom for ZTO, CHO, BVO, ARO, UPO, UYO, PEO, COO, MXO, TTT, TPA, GTO, EON, DOO
PRODUCT_PACKAGES += \
    DisneyMagicKingdom

# Add 99taxis.com for ZTO
PRODUCT_PACKAGES += \
    99Taxis

# add 99taxis channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/LATIN/Taxis99/pre_install.appsflyer:system/etc/pre_install.appsflyer

# Add AsphaltNitro for CHO
PRODUCT_PACKAGES += \
    AsphaltNitro
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast