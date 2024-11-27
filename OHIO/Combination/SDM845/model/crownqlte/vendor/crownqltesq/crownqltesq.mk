QCOM_PRODUCT_DEVICE := crownqltesq

$(call inherit-product, device/samsung/crownqltesq/device.mk)
include vendor/samsung/configs/crownqlte_common/crownqlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/crownqltesq/gms_crownqltesq.mk
endif

# define the default locales for phone device
include vendor/samsung/build/localelist/SecLocale_USA.mk

PRODUCT_NAME := crownqltesq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N960U

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
    imscoremanager \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    UnifiedWFC \
    verizon.net.sip \
    verizon_net_sip_library.xml

# Add USA SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA

# CMAS for ATT/VZW
PRODUCT_PACKAGES += \
    EmergencyAlert

# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_O
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_O
endif

# Add Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \
    SamsungPayApp

# Add YELP panel
PRODUCT_PACKAGES += \
    YelpPanel

# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

ifneq ($(SEC_FACTORY_BUILD),true)
###############################################################
#### Single SKU customizaion >>>>>
ifneq ($(filter VZW ATT TMB SPT USC AIO TMK TFN CCT, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# ATT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),ATT)
include vendor/samsung/configs/crownqltesq/crownqltesqatt_apps.mk
endif
# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/crownqltesq/crownqltesqtmo_apps.mk
endif 
# MPCS Preload Apps ONLY
#ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMK)
#include vendor/samsung/configs/crownqltesq/crownqltesqmtr_apps.mk
#endif
# SPR Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
include vendor/samsung/configs/crownqltesq/crownqltesqspr_apps.mk
include vendor/samsung/configs/crownqltesq/crownqltesq_vzwspr_apps.mk
endif 
# VZW Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),VZW)
include vendor/samsung/configs/crownqltesq/crownqltesq_vzwspr_apps.mk
include vendor/samsung/configs/crownqltesq/crownqltesqvzw_apps.mk
endif
# AIO Preload Apps ONLY
#ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),AIO)
#include vendor/samsung/configs/crownqltesq/crownqltesqaio_apps.mk
#endif
# USC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),USC)
include vendor/samsung/configs/crownqltesq/crownqltesqusc_apps.mk
endif 
# TFN Preload Apps ONLY
#ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TFN)
#include vvendor/samsung/configs/crownqltesq/crownqltesqtfn_apps.mk
#endif 
# CCT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),CCT)
include vendor/samsung/configs/crownqltesq/crownqltesqcct_apps.mk
endif 
else
# OYN SINGLESKU
# Include VZW,SPR common library
include vendor/samsung/configs/crownqltesq/crownqltesq_vzwspr_apps.mk
# VzW Apps
include vendor/samsung/configs/crownqltesq/crownqltesqvzw_apps.mk
# Sprint Preload & removable Apps
include vendor/samsung/configs/crownqltesq/crownqltesqspr_apps.mk
# ATT Preload & removable Apps
include vendor/samsung/configs/crownqltesq/crownqltesqatt_apps.mk
# TMO Apps
include vendor/samsung/configs/crownqltesq/crownqltesqtmo_apps.mk
# MPCS Apps
#include vendor/samsung/configs/crownqltesq/crownqltesqmtr_apps.mk
# AIO Preload Apps ONLY
#include vendor/samsung/configs/crownqltesq/crownqltesqaio_apps.mk
# USC Apps
include vendor/samsung/configs/crownqltesq/crownqltesqusc_apps.mk
# TFN Apps
#include vendor/samsung/configs/crownqltesq/crownqltesqtfn_apps.mk
# CCT Apps
include vendor/samsung/configs/crownqltesq/crownqltesqcct_apps.mk
endif
#### Single SKU customizaion <<<<<
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Samsung Plus Removable
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_8.0_Removable \
    SBrowser_8.0

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Hancom Office Editor
PRODUCT_PACKAGES += \
    HancomOfficeEditor_New_Hidden
    
# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add libs for HancomOffice Lite Editor
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libAndroidExport_e_nh.so:system/lib64/libAndroidExport_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libTFDrawingSDK_e_nh.so:system/lib64/libTFDrawingSDK_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libTfoWrite_e_nh.so:system/lib64/libTfoWrite_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libfontbase_e_nh.so:system/lib64/libfontbase_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libhncimageEffects_e_nh.so:system/lib64/libhncimageEffects_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libskia_tf_e_nh.so:system/lib64/libskia_tf_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libtfbidi_e_nh.so:system/lib64/libtfbidi_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libtfexternal_e_nh.so:system/lib64/libtfexternal_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libtffont2_e_nh.so:system/lib64/libtffont2_e.so 
PRODUCT_COPY_FILES += applications/par/edp/HCO/HancomOfficeEditorHidden/64bitOS/libtfimage_e_nh.so:system/lib64/libtfimage_e.so 

# Remove USP for customer delivery (-e omce)
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES += \
    -HybridRadio_O

PRODUCT_PACKAGES += \
    -SamsungCamera8 \
    -ShootingModeProvider2 \
    -FilterInstaller \
    -FilterProvider \
    -StickerFaceAR2 \
    -StickerFaceAR3D2 \
    -StickerStamp \
    -StickerWatermark \
    -StickerProvider \
    -StickerFaceARFrame \
    -StickerFaceARAvatar
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

