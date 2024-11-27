$(call inherit-product, device/samsung/beyondx/device.mk)
include vendor/samsung/configs/beyondx_common/beyondx_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyondx/gms_beyondxxx.mk
endif

PRODUCT_NAME := beyondxxx
PRODUCT_DEVICE := beyondx
PRODUCT_MODEL := SM-G977B

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Crane
PRODUCT_PACKAGES += \
    Crane

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify	

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
























###############################################################
# FactoryBinary only (end of line)
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################


# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService
