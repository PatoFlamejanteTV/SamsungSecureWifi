$(call inherit-product, device/samsung/greatqltesq/device.mk)
include vendor/samsung/configs/greatqlte_common/greatqlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

# Include gms packages depending on product seperately
include vendor/samsung/configs/greatqltesq/gms_greatqltesq.mk

PRODUCT_NAME := greatqltesq
PRODUCT_DEVICE := greatqltesq
PRODUCT_MODEL := SM-N950U
PRODUCT_LOCALES := en_US es_US fr_FR de_DE it_IT vi_VN ko_KR zh_CN zh_TW zh_HK ja_JP pt_BR
PRODUCT_FINGERPRINT_TYPE := pilot

# for samsung hardware init
PRODUCT_COPY_FILES += \
    device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:root/init.carrier.rc

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    imsservice \
    vsimmanager \
    vsimservice \
    NSDSWebApp \
    secimshttpclient \
    ImsLogger+ \
    ImsSettings \
    libsec-ims \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    VoWifiSPG \
    verizon.net.sip \
    verizon_net_sip_library.xml

# OMC
PRODUCT_PACKAGES += \
    OMCAgent

# PAI Stub
PRODUCT_PACKAGES += \
    PlayAutoInstallConfig
	
# Facebook apps
PRODUCT_PACKAGES += \
    FBInstagram_stub \
    FBMessenger_stub

# Add WhatsAppDownloader
PRODUCT_PACKAGES += \
    -WhatsAppDownloader

# Add Hancom Office Editor
PRODUCT_PACKAGES += \
    HancomOfficeEditor_New_Hidden

# Add libs for HancomOffice Lite Editor
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libAndroidExport_e_nh.so:system/lib64/libAndroidExport_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libhncimageEffects_e_nh.so:system/lib64/libhncimageEffects_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libskia_tf_e_nh.so:system/lib64/libskia_tf_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libtfbidi_e_nh.so:system/lib64/libtfbidi_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libTFDrawingSDK_e_nh.so:system/lib64/libTFDrawingSDK_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libtfexternal_e_nh.so:system/lib64/libtfexternal_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libtffont2_e_nh.so:system/lib64/libtffont2_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libtfimage_e_nh.so:system/lib64/libtfimage_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libTfoWrite_e_nh.so:system/lib64/libTfoWrite_e.so
PRODUCT_COPY_FILES +=applications/3rd_party/general/HancomOffice/Editor_SMP_Hidden/64bitOS/libfontbase_e_nh.so:system/lib64/libfontbase_e.so

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0_Task

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_6.0_Removable \
    SBrowser_6.0

# Add Radio App Package
ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_N
endif

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio 

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Samsung Plus Removable
PRODUCT_PACKAGES += \
	SAMSUNG_PLUS_REMOVABLE

# Add Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \
    SamsungPayApp

# Add OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
    
# Add MobileOffice Removabal Stub
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub \
    Excel_SamsungStub_Removable \
    PowerPoint_SamsungStub_Removable \
    Word_SamsungStub_Removable

# Hotspot TMO,MTR,USC
PRODUCT_PACKAGES += \
    MHSWrapperUSC

# Add YELP panel
PRODUCT_PACKAGES += \
    YelpPanel

# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel

# Add S Voice IME
PRODUCT_PACKAGES += \
    SVoiceIME

ifneq ($(filter VZW ATT TMB SPT USC TFN CCT, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# ATT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),ATT)  								
include vendor/samsung/configs/greatqltesq/greatqltesqatt_apps.mk
endif
# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/greatqltesq/greatqltesqtmo_apps.mk
endif 
# SPR Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
include vendor/samsung/configs/greatqltesq/greatqltesqspr_apps.mk
include vendor/samsung/configs/greatqltesq/greatqltesq_vzwspr_apps.mk
endif 
# VZW Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),VZW)
include vendor/samsung/configs/greatqltesq/greatqltesq_vzwspr_apps.mk
include vendor/samsung/configs/greatqltesq/greatqltesqvzw_apps.mk
endif
# USC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),USC)
include vendor/samsung/configs/greatqltesq/greatqltesqusc_apps.mk
endif 
# TFN Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TFN)
include vendor/samsung/configs/greatqltesq/greatqltesqtfn_apps.mk
endif 
# CCT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),CCT)
include vendor/samsung/configs/greatqltesq/greatqltesqcct_apps.mk
endif 
else
# OYN SINGLESKU
# Include VZW,SPR common library
include vendor/samsung/configs/greatqltesq/greatqltesq_vzwspr_apps.mk
# VzW Apps
include vendor/samsung/configs/greatqltesq/greatqltesqvzw_apps.mk
# Sprint Preload & removable Apps
include vendor/samsung/configs/greatqltesq/greatqltesqspr_apps.mk
# ATT Preload & removable Apps
include vendor/samsung/configs/greatqltesq/greatqltesqatt_apps.mk
# TMO Apps
include vendor/samsung/configs/greatqltesq/greatqltesqtmo_apps.mk
# USC Apps
include vendor/samsung/configs/greatqltesq/greatqltesqusc_apps.mk
# TFN Apps
include vendor/samsung/configs/greatqltesq/greatqltesqtfn_apps.mk
# CCT Apps
include vendor/samsung/configs/greatqltesq/greatqltesqcct_apps.mk
endif 

# Remove USP for user delivery
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES +=  \
    -SamsungConnect \
    -Bixby
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/ACG/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/AIO/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/CCT/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/LRA/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/TFN/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=applications/3rd_party/dummy.txt:system/carrier/XAA/dummy.txt

# Add Device Protection Manager
PRODUCT_PACKAGES += \
    ASKSManager

# Add VZW SUA ISO
PRODUCT_COPY_FILES += \
    vendor/regional/usa/vzw/SUA_ISO/greatqlte/autorun.iso:system/etc/autorun.iso

# Add Setup Wizard and PCO client for VZW, CCT and TFN
PRODUCT_PACKAGES += \
    SetupWizard_USA

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

