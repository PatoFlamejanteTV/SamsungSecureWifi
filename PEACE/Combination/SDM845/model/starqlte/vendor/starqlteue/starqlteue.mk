QCOM_PRODUCT_DEVICE := starqlteue

$(call inherit-product, device/samsung/starqlteue/device.mk)
include vendor/samsung/configs/starqlte_common/starqlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/starqlteue/gms_starqlteue.mk
endif

PRODUCT_NAME := starqlteue
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G960U1
 
# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

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

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    MnoDmClient \
    mnodm.rc

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# Add USA SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA

# Add unbranded SSO and APNService
PRODUCT_PACKAGES += \
    LoginClientUnbranded \
    VZWAPNService_VZW \
    VZWAPNLib_VZW \
    vzwapnlib.xml \
    feature_ehrpd.xml \
    feature_lte.xml \
    feature_srlte.xml

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_9.0_Removable \
    SBrowser_9.0

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Add YELP panel
PRODUCT_PACKAGES += \
    YelpPanel

# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel

	# Add Radio App Package
ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

# Add Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \
    SamsungPayApp

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

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Samsung Keyboard Beta
PRODUCT_PACKAGES += \
    SamsungIMEv3.3Tyme_Removable

# Add Samsung Plus Removable
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
    
# Remove OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

		
## Include Sprint OMADM , Chameleon + Preloaded Apps (SMF -Sprint Hub)
include vendor/samsung/configs/starqlteue/starqlteuespr_apps.mk
include vendor/samsung/configs/starqlteue/starqlteue_vzwspr_apps.mk
# VisualVoiceMail
PRODUCT_PACKAGES += \
    VisualVoiceMail_SE_stub


# Remove MobileOffice
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub

# Add Hancom Office Editor
PRODUCT_PACKAGES += \
    HancomOfficeEditor_Hidden_Install

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

