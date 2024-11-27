$(call inherit-product, device/samsung/picasso2/device.mk)
include vendor/samsung/configs/picasso2_common/picasso2_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/picasso2/gms_picasso2xx.mk
endif

PRODUCT_NAME := picasso2xx
PRODUCT_DEVICE := picasso2
PRODUCT_MODEL := picasso2xx

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
