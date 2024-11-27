QCOM_PRODUCT_DEVICE := gts6l

$(call inherit-product, device/samsung/gts6l/device.mk)
include vendor/samsung/configs/gts6l_common/gts6l_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts6l/gms_gts6lvzw.mk
endif

PRODUCT_NAME := gts6lvzw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T867V

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

#VZW/ATT apps
PRODUCT_PACKAGES += \
    EmergencyAlert	
	
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify
    
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# Add Verizon SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_VZW \
    HuxExtension \
    -SecSetupWizard_Global

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
	
# Remove KidsHome
PRODUCT_PACKAGES += \
    -KidsHome_P \
    -KidsHome_Installer

# Add CDFS Package
PRODUCT_PACKAGES += \
    Cdfs

# QCOM eMBMS MSDC
PRODUCT_PACKAGES += \
    QAS_DVC_MSP_VZW

# eMBMS VzW API library
PRODUCT_PACKAGES += \
    vzw_msdc_api

# eMBMS VzW API library
PRODUCT_COPY_FILES += \
    applications/par/edp/EMBMS/EmbmsQualcommMW/vzw/com.verizon.embms.xml:system/etc/permissions/com.verizon.embms.xml

#remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v11

# Remove Samsung Messaging
PRODUCT_PACKAGES += \
    -SamsungMessages_11
	
# ALM
PRODUCT_PACKAGES += \
    DumpCollector
