QCOM_PRODUCT_DEVICE := winnerltesq

$(call inherit-product, device/samsung/winnerltesq/device.mk)
include vendor/samsung/configs/winnerlte_common/winnerlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/winnerltesq/gms_winnerltesq.mk
endif

PRODUCT_NAME := winnerltesq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-F900U
PRODUCT_LOCALES := en_US es_US fr_FR de_DE it_IT vi_VN ko_KR zh_CN zh_TW zh_HK ja_JP pt_BR

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

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    NSDSWebApp

# Add Samsung_PLUS Removable
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE	

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
