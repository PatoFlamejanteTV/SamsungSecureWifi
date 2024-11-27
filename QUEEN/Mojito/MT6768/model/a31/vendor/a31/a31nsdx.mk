include vendor/samsung/configs/a31/a31xx.mk

PRODUCT_NAME := a31nsdx
PRODUCT_MODEL := SM-A315G
PRODUCT_LOCALES := en_GB $(filter-out en_GB,$(PRODUCT_LOCALES))

# Add Samsung TTS
PRODUCT_PACKAGES += smt_vi_VN_f00
PRODUCT_PACKAGES += -smt_pl_PL_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O
	
# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add 99taxis.com for ZTO
PRODUCT_PACKAGES += \
    99Taxis
	
# add 99Taxis channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/LATIN/Taxis99/pre_install.appsflyer:system/etc/pre_install.appsflyer

# Add DisneyMagicKingdom for ZTO, CHO, BVO, ARO, UPO, UYO, PEO, COO, MXO, TTT, TPA, GTO, EON, DOO, CHK,CHP,CHQ
PRODUCT_PACKAGES += \
    DisneyMagicKingdom
	
# Add AsphaltNitro for CHO, CHK,CHP,CHQ
PRODUCT_PACKAGES += \
    AsphaltNitro	

