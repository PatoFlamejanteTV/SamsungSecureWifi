QCOM_PRODUCT_DEVICE := star2qlteue

$(call inherit-product, device/samsung/star2qlteue/device.mk)
include vendor/samsung/configs/star2qlte_common/star2qlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/star2qlteue/gms_star2qlteue.mk
endif

PRODUCT_NAME := star2qlteue
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G965U1

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

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    MnoDmClient \
    mnodm.rc

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

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

# Add Samsung Keyboard Beta
PRODUCT_PACKAGES += \
    SamsungIMEv3.3Tyme_Removable

# Add Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \
    SamsungPayApp

# Add Samsung Plus Removable
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

## Include Sprint OMADM , Chameleon + Preloaded Apps (SMF -Sprint Hub)
include vendor/samsung/configs/star2qlteue/star2qlteuespr_apps.mk
include vendor/samsung/configs/star2qlteue/star2qlteue_vzwspr_apps.mk

# VisualVoiceMail
PRODUCT_PACKAGES += \
    VisualVoiceMail_SE_stub

# Remove OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

# Remove MobileOffice
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

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
