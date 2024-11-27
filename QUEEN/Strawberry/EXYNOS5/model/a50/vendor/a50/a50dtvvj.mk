include vendor/samsung/configs/a50/a50dd.mk

PRODUCT_NAME := a50dtvvj
PRODUCT_MODEL := SM-A505GT

# Add Samsung TTS
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += -smt_hi_IN_f00
PRODUCT_PACKAGES += -smt_en_IN_f00

# Remove LogWriter Solution
PRODUCT_PACKAGES += \
    -LogWriter

# Add 99taxis.com for ZTO
PRODUCT_PACKAGES += \
    99Taxis

# Add DisneyMagicKingdom for ZTO, CHO, BVO, ARO, UPO, UYO, PEO, COO, MXO, TTT, TPA, GTO, EON, DOO
PRODUCT_PACKAGES += \
    DisneyMagicKingdom
	
# ++ LTN MobileTV
PRODUCT_PACKAGES += \
    MobileTV_LTN
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.latin.dtv.rc:system/etc/init/init.dtv.rc
# -- LTN MobileTV
