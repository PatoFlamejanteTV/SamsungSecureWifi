QCOM_PRODUCT_DEVICE := beyondxq

$(call inherit-product, device/samsung/beyondxq/device.mk)
include vendor/samsung/configs/beyondxq_common/beyondxq_common.mk

# FactoryBinary doesn't need gms packages.
# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyondxq/gms_beyondxqvzw.mk
# VzW Preload & removable Apps
include vendor/samsung/configs/beyondxq/beyondxqsqvzw_apps.mk
endif

PRODUCT_NAME := beyondxqvzw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G977U

include vendor/samsung/build/localelist/SecLocale_USA.mk
	
# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_VZW \
    HuxExtension

# Remove SecSetupWizard_Global
PRODUCT_PACKAGES += \
    -SecSetupWizard_Global

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# TetheringProvision for ATT/VZW/AIO
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision
    
# Add Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \
    SamsungPayApp

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Hancom Office Editor
PRODUCT_PACKAGES += \
    HancomOfficeEditor_Hidden_Install

# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

#VZW/ATT apps
PRODUCT_PACKAGES += \
    EmergencyAlert	

# Add Samsung+ removable 12.01.08.7 as of July 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# Remove Microsoft OfficeMobile Stub
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub

# Add Microsoft OfficeMobile headless for USA
PRODUCT_PACKAGES += \
    OfficeMobile_SamsungHeadless

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit
	
# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/VZW/dummy.txt

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

# Remove USP for customer delivery (-e omce)
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES += \
    -HybridRadio
endif

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

# Add UnifiedTetheringProvision for ATT/VZW/AIO
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision
    
# VZW, SPR Common apps
## Below library is common for both VZW,SPR carrier, so include this make file in respective carrier based app make file.
PRODUCT_PACKAGES += \
	libmno_dmstack