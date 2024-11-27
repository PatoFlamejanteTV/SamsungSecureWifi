$(call inherit-product, device/samsung/picasso3/device.mk)
include vendor/samsung/configs/picasso3_common/picasso3_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/picasso3/gms_picasso3xx.mk
endif

PRODUCT_NAME := picasso3xx
PRODUCT_DEVICE := picasso3
PRODUCT_MODEL := picasso3xx

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################

