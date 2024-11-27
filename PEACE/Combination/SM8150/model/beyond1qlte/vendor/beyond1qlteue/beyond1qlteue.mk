QCOM_PRODUCT_DEVICE := beyond1qlteue

$(call inherit-product, device/samsung/beyond1qlteue/device.mk)
include vendor/samsung/configs/beyond1qlte_common/beyond1qlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyond1qlteue/gms_beyond1qlteue.mk
endif

PRODUCT_NAME := beyond1qlteue
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G973U1

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
  -SBrowser_9.0_Removable \
  SBrowser_9.0

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Gobal Setupwizard temporarily blocked(VZW Setupwizard checking is required)
PRODUCT_PACKAGES += \
    -SecSetupWizard_Global

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Samsung+ removable 10.19.1.3 as of Aug 2018
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE
	
# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

# Remove USP for customer delivery (-e omce)
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES += \
    -HybridRadio_P
endif

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    NSDSWebApp

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Sprint Preload & removable Apps
include vendor/samsung/configs/beyond1qlteue/beyond1qlteue_vzwspr_apps.mk
include vendor/samsung/configs/beyond1qlteue/beyond1qlteuespr_apps.mk
		
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
