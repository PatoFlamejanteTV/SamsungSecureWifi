include vendor/samsung/configs/a20/a20ser.mk

PRODUCT_NAME := a20ub
PRODUCT_MODEL := SM-A205G

# Remove LogWriter Solution
PRODUCT_PACKAGES += \
    -LogWriter

# Add UltraDataSaving
PRODUCT_PACKAGES += \
	UltraDataSaving_O

# Add DisneyMagicKingdom for ZTO, CHO, BVO, ARO, UPO, UYO, PEO, COO, MXO, TTT, TPA, GTO, EON, DOO
PRODUCT_PACKAGES += \
    DisneyMagicKingdom	

# Add AsphaltNitro for CHO
PRODUCT_PACKAGES += \
    AsphaltNitro

# Add 99taxis.com for ZTO
PRODUCT_PACKAGES += \
    99Taxis

# define the default locales for phone device
include vendor/samsung/build/localelist/SecLocale_EUR_OPEN.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService
		
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast