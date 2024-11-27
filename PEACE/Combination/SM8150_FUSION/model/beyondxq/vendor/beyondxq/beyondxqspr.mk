QCOM_PRODUCT_DEVICE := beyondxq

$(call inherit-product, device/samsung/beyondxq/device.mk)
include vendor/samsung/configs/beyondxq_common/beyondxq_common.mk

# FactoryBinary doesn't need gms packages.
# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyondxq/gms_beyondxqspr.mk
# SPR Preload & removable Apps
include vendor/samsung/configs/beyondxq/beyondxqsq_vzwspr_apps.mk
include vendor/samsung/configs/beyondxq/beyondxqsqspr_apps.mk
endif

PRODUCT_NAME := beyondxqspr
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G977P

include vendor/samsung/build/localelist/SecLocale_USA.mk



# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Remove SecSetupWizard_Global
PRODUCT_PACKAGES += \
    -SecSetupWizard_Global

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
  -SBrowser_9.0_Removable \
  SBrowser_9.0

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

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

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

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyondxq/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyondxq/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyondxq/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyondxq/dummy.txt:system/carrier/VZW/dummy.txt

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
