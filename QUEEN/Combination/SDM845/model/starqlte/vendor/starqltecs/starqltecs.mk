QCOM_PRODUCT_DEVICE := starqltecs

$(call inherit-product, device/samsung/starqltecs/device.mk)
include vendor/samsung/configs/starqlte_common/starqlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/starqltecs/gms_starqltecs.mk
endif


PRODUCT_NAME := starqltecs
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G960W

include vendor/samsung/build/localelist/SecLocale_CAN.mk



# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
# Add Samsung+ removable 12.01.08.7 as of July 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
# PRODUCT_PACKAGES += \
#    BlockchainBasicKit

# Removing WebManual
# PRODUCT_PACKAGES += \
#    -WebManual

# VZW/ATT apps
PRODUCT_PACKAGES += \
    EmergencyAlert	
	
# Add Spotify
# PRODUCT_PACKAGES += \
#    Spotify
    
# Add Hancom Office Editor
PRODUCT_PACKAGES += \
    HancomOfficeEditor_Hidden_Install    

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio
	
# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_fr_CA_f00

# Remove USP for customer delivery (-e omce)
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES += \
    -HybridRadio
endif

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add UsByod package
PRODUCT_PACKAGES += \
    UsByod

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add UnifiedTetheringProvision for ATT/VZW/AIO
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Only Crown P

# Need to be checked crown Samsung IMS

# Add unbranded SSO and APNService for CCT/CHA
PRODUCT_PACKAGES += \
    LoginClientUnbranded_sku \
    VZWAPNService_sku

# Add YELP panel
PRODUCT_PACKAGES += \
    YelpPanel

# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel
    
# Add Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \
    SamsungPayApp

# Hotspot TMO,MTR,USC
PRODUCT_PACKAGES += \
    MHSWrapperUSC
    
# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Samsung Ads Setting
PRODUCT_PACKAGES += \
    MasDynamicSetting
