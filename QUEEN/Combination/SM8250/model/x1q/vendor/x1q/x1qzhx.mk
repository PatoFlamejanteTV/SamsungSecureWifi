QCOM_PRODUCT_DEVICE := x1q

$(call inherit-product, device/samsung/x1q/device.mk)
include vendor/samsung/configs/x1q_common/x1q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/x1q/gms_x1qzhx.mk
endif

PRODUCT_NAME := x1qzhx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G9810

include vendor/samsung/build/localelist/SecLocale_CHN.mk



# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert   
   
# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Samsung+ removable 12.01.08.7 as of Aug 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# UnifiedTetheringProvision for ATT, AIO, VZW
PRODUCT_PACKAGES += \
	UnifiedTetheringProvision

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    MnoDmClient \
    MnoDmViewer \
    mnodm.rc \
    SDMHiddenMenu

# SamsungEuicc Service and Client
PRODUCT_PACKAGES += \
    EuiccService
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccClient
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
