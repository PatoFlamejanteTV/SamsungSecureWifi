QCOM_PRODUCT_DEVICE := gts6l

$(call inherit-product, device/samsung/gts6l/device.mk)
include vendor/samsung/configs/gts6l_common/gts6l_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts6l/gms_gts6lsq.mk
endif

PRODUCT_NAME := gts6lsq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T867U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

#VZW/ATT apps
PRODUCT_PACKAGES += \
    EmergencyAlert	
	
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify
    
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add UsByod package
PRODUCT_PACKAGES += \
    UsByod

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
