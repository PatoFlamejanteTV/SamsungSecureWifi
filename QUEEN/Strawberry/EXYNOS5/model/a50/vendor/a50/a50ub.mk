include vendor/samsung/configs/a50/a50dd.mk

PRODUCT_NAME := a50ub
PRODUCT_MODEL := SM-A505G

# Add Samsung TTS
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += -smt_hi_IN_f00
PRODUCT_PACKAGES += -smt_en_IN_f00

# Remove LogWriter Solution
PRODUCT_PACKAGES += \
    -LogWriter

# Add DisneyMagicKingdom for ZTO, CHO, BVO, ARO, UPO, UYO, PEO, COO, MXO, TTT, TPA, GTO, EON, DOO
PRODUCT_PACKAGES += \
    DisneyMagicKingdom

# Add AsphaltNitro for CHO
PRODUCT_PACKAGES += \
    AsphaltNitro

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast