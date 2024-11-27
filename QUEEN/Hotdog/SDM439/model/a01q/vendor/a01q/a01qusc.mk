QCOM_PRODUCT_DEVICE := a01q

$(call inherit-product, device/samsung/a01q/device.mk)
include vendor/samsung/configs/a01q_common/a01q_common.mk

# removing gms for bulk binary
# ifeq ($(shell secgetspf SEC_PRODUCT_FEATURE_BUILD_JDM_BULK_BINARY), FALSE)
include vendor/samsung/configs/a01q/gms_a01qusc.mk
# endif

PRODUCT_NAME := a01qusc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A015R4

include vendor/samsung/build/localelist/SecLocale_USA.mk


###############################################################
# Remove Packages not needed for USA
###############################################################
# Remove Aura App Package
PRODUCT_PACKAGES += \
    -AURA

# Remove Dictionary for Sec
PRODUCT_PACKAGES += \
    -DictDiotekForSec

# Remove Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    -FBAppManager_NS \
    -appmanager.conf \
    -FBInstaller_NS \
    -FBServices

# Remove Find My Mobile
PRODUCT_PACKAGES += \
    -Fmm

# Remove Glispa App Package
PRODUCT_PACKAGES += \
    -GLISPA

# Remove Ignite App Package
PRODUCT_PACKAGES += \
    -IGNITE

# Remove LinkedIn Stub
PRODUCT_PACKAGES += \
    -LinkedIn_SamsungStub_Deletable

# Remove Onedrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

# Remove SamsungMembers
PRODUCT_PACKAGES += \
    -SamsungMembers \
    
# Remove SmartManager for USA
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Remove Voice Recorder
PRODUCT_PACKAGES += \
    -VoiceNote_5.0

# Add WebManual DO NOT add this package for USA and JPN
PRODUCT_PACKAGES += \
    -WebManual
###############################################################

# for InputEventApp
PRODUCT_PACKAGES += \
    InputEventApp \
    libDiagService_

# for HiddenMenu
PRODUCT_PACKAGES += \
    HiddenMenu

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
endif

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0
	
# CDFS MODE remove sysem server
PRODUCT_PACKAGES += \
    Cdfs

# MyVerizonServie
	
# VZW OS Library
PRODUCT_PACKAGES += \
    com.verizon.os \
    com.verizon.os.xml

# VZW APNLib
PRODUCT_PACKAGES += \
    VZWAPNLib_VZW \
    vzwapnlib.xml \
    feature_ehrpd.xml \
    feature_lte.xml \
    feature_srlte.xml	

# Add VZW SUA ISO
PRODUCT_COPY_FILES += \
    vendor/regional/usa/vzw/SUA_ISO/a01q/autorun.iso:system/etc/autorun.iso

# QCOM eMBMS MSDC
PRODUCT_PACKAGES += \
    QAS_DVC_MSP_VZW

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
# eMBMS VzW API library
PRODUCT_PACKAGES += \
    vzw_msdc_api

# TetheringProvision for ATT/VZW/AIO
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# eMBMS VzW API library
PRODUCT_COPY_FILES += \
    applications/par/edp/EMBMS/EmbmsQualcommMW/vzw/com.verizon.embms.xml:system/etc/permissions/com.verizon.embms.xml

# Add Verizon SetupWizard Packages
#PRODUCT_PACKAGES += \
#    SetupWizard_VZW \
#    HuxExtension

# Remove SecSetupWizard_Global
#PRODUCT_PACKAGES += \
#    -SecSetupWizard_Global

# Only for making bulk binary
###############################################################
ifeq ($(shell secgetspf SEC_PRODUCT_FEATURE_BUILD_JDM_BULK_BINARY), TRUE)

# bulk common mk
include vendor/samsung/configs/a01q/a01q_bulk.mk
        
endif #end of SEC_JDM_BULK_BINARY
###############################################################
# end of line
# PLEASE DO NOT ADD LINE BELOW
