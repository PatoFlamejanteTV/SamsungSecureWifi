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
PRODUCT_MODEL := SM-A906B



# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

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

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Remove Onedrive
PRODUCT_PACKAGES += \
 -OneDrive_Samsung_v3

# Add Hancom Office Editor
PRODUCT_PACKAGES += \
    HancomOfficeEditor_Hidden_Install

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# CMAS for ATT/VZW
PRODUCT_PACKAGES += \
    EmergencyAlert

# Add Samsung Ads Setting
PRODUCT_PACKAGES += \
    MasDynamicSetting

# Add BlockchainBasicKit
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Remove Microsoft OfficeMobile Stub
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub

# Add Microsoft OfficeMobile headless for USA
PRODUCT_PACKAGES += \
    OfficeMobile_SamsungHeadless

# Remove SmartManager
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

ifneq ($(SEC_FACTORY_BUILD),true)
# Remove eSE features for only normal binary
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
