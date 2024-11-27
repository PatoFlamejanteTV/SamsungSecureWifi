QCOM_PRODUCT_DEVICE := r3q

$(call inherit-product, device/samsung/r3q/device.mk)
include vendor/samsung/configs/r3q_common/r3q_common.mk

# FactoryBinary doesn't need gms packages.
# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r3q/gms_r3qxx.mk
endif

PRODUCT_NAME := r3qxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A908B

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01


# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Remove HiyaService for Smart Call 
PRODUCT_PACKAGES += \
    -HiyaService \

# Remove Upday 
PRODUCT_PACKAGES += \
    -Upday \

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    Facebook_stub_Removable

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
    
# HiddenNetworkSetting 
PRODUCT_PACKAGES += \
    HiddenNetworkSetting

# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone
	
#FM Radio
PRODUCT_PACKAGES += \
    HybridRadio
    
# Remove Spotify 
PRODUCT_PACKAGES += \
    -Spotify

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

