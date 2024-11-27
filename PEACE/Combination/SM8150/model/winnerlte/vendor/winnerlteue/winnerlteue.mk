QCOM_PRODUCT_DEVICE := winnerlteue

$(call inherit-product, device/samsung/winnerlteue/device.mk)
include vendor/samsung/configs/winnerlte_common/winnerlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/winnerlteue/gms_winnerlteue.mk
endif

PRODUCT_NAME := winnerlteue
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-F900U1

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Gobal Setupwizard temporarily blocked
PRODUCT_PACKAGES += \
    -SecSetupWizard_Global

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_9.0_Removable \
    SBrowser_9.0

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    NSDSWebApp

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
