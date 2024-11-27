QCOM_PRODUCT_DEVICE := crownqlteue

$(call inherit-product, device/samsung/crownqlteue/device.mk)
include vendor/samsung/configs/crownqlte_common/crownqlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/crownqlteue/gms_crownqlteue.mk
endif

# define the default locales for phone device
include vendor/samsung/build/localelist/SecLocale_USA.mk

PRODUCT_NAME := crownqlteue
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N960U1

# Remove MobileOffice Stub
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub

# Removing LinkedIn Stub
PRODUCT_PACKAGES += \
    -LinkedIn_SamsungStub
    
# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    imsservice \
    ImsTelephonyService \
    vsimmanager \
    vsimservice \
    NSDSWebApp \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    UnifiedWFC \
    verizon.net.sip \
    verizon_net_sip_library.xml

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# CMAS for ATT/VZW
PRODUCT_PACKAGES += \
    EmergencyAlert

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    MnoDmClient \
    mnodm.rc

# Add USA SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

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
 
 # Add Samsung Plus Removable  
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE
   
# Add Radio App Package
ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Remove USP for customer delivery (-e omce)
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    -HybridRadio_P
endif
endif
# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_9.0_Removable \
    SBrowser_9.0

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Hotspot TMO,MTR,USC
PRODUCT_PACKAGES += \
    MHSWrapperUSC

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    -OneDrive_Samsung_v3_Removable
    
# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Hancom Office Editor
PRODUCT_PACKAGES += \
    HancomOfficeEditor_Hidden_Install

# Add Samsung Keyboard Beta
PRODUCT_PACKAGES += \
    SamsungIMEv3.3Tyme_Removable

# SPR Preload Apps ONLY
include vendor/samsung/configs/crownqlteue/crownqlteuespr_apps.mk
include vendor/samsung/configs/crownqlteue/crownqlteue_vzwspr_apps.mk

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
