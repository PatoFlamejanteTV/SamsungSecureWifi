$(call inherit-product, device/samsung/a51x/device.mk)
include vendor/samsung/configs/a51x_common/a51x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a51x/gms_a51xnaxx.mk
endif

PRODUCT_NAME := a51xnaxx
PRODUCT_DEVICE := a51x
PRODUCT_MODEL := SM-A516B

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Add SKT-DM (LAWMO)
#PRODUCT_PACKAGES += \
#    SKTFindLostPhone

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Remove HiyaService for Smart Call 
PRODUCT_PACKAGES += \
    -HiyaService \

# Facebook apps
#PRODUCT_PACKAGES += \
#    -Facebook_stub \
#    Facebook_stub_Removable

# Remove LinkedIn Stub
#PRODUCT_PACKAGES += \
#    -LinkedIn_SamsungStub_Deletable

# Add OneDrive removable
#PRODUCT_PACKAGES += \
#    -OneDrive_Samsung_v3 \
#    OneDrive_Samsung_v3_Removable

# Remove Spotify
#PRODUCT_PACKAGES += \
#    -Spotify

# Keystring
PRODUCT_PACKAGES += \
    k09900
	
# Add Ignite App Package
PRODUCT_PACKAGES += \
IGNITE

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# BlockchainBasicKit (Supported Country) : EUR, KOR, USA, CAN, SEA/Oceania
# -BlockchainBasicKit (Not supported countries) : CHN, JPN, LDU device
PRODUCT_PACKAGES += \
    BlockchainBasicKit

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
