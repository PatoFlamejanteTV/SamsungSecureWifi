include vendor/samsung/configs/a10/a10dd.mk

PRODUCT_NAME := a10ub
PRODUCT_MODEL := SM-A105M

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += -smt_hi_IN_f00
PRODUCT_PACKAGES += -smt_en_IN_f00

# Remove LogWriter Solution
PRODUCT_PACKAGES += \
    -LogWriter

# Add UDS
PRODUCT_PACKAGES += \
	UltraDataSaving_O

# Add 99taxis.com for ZTO
PRODUCT_PACKAGES += \
    99Taxis

# Add DisneyMagicKingdom for ZTO, CHO, BVO, ARO, UPO, UYO, PEO, COO, MXO, TTT, TPA, GTO, EON, DOO
PRODUCT_PACKAGES += \
    DisneyMagicKingdom

# Add AsphaltNitro for CHO
PRODUCT_PACKAGES += \
    AsphaltNitro
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast