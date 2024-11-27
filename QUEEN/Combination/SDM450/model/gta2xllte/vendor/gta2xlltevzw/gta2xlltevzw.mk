QCOM_PRODUCT_DEVICE := gta2xlltevzw

# LPM Animation
addlpmSpiResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),$(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1))),,$(1))

LPM_LOOK_TYPE := NewtypeLook
LPM_RESOLUTION := 1200_1920
PRODUCT_PACKAGES += \
    $(call addlpmSpiResource,slow_charging_text.spi)\
    $(call addlpmSpiResource,incomplete_connect_text.spi)\
    $(call addlpmSpiResource,incompatible_charger_text.spi)\
    $(call addlpmSpiResource,temperature_text.spi)\
    $(call addlpmSpiResource,temperature_text2.spi)\
    $(call addlpmSpiResource,fully_charged.spi)\
    $(call addlpmSpiResource,spare_digit.spi)\
    $(call addlpmSpiResource,spare_hr.spi)\
    $(call addlpmSpiResource,spare_min.spi)\
    $(call addlpmSpiResource,spare_text.spi)\
    $(call addlpmSpiResource,fast_charging_vzw.spi)

$(call inherit-product, device/samsung/gta2xlltevzw/device.mk)
include vendor/samsung/configs/gta2xllte_common/gta2xllte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta2xlltevzw/gms_gta2xlltevzw.mk
endif

PRODUCT_NAME := gta2xlltevzw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T597V
PRODUCT_CHARACTERISTICS += tablet
PRODUCT_BRAND := Verizon

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_VZW \
    HuxExtension

# Remove SecSetupWizard_Global
PRODUCT_PACKAGES += \
    -SecSetupWizard_Global

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# ALM
PRODUCT_PACKAGES += \
    DumpCollector

# Add VUX SNP Packages
PRODUCT_PACKAGES += \
    SNP

# Add LLK Agent
PRODUCT_PACKAGES += \
    LLKAgent \
    com.customermobile.preload.vzw	
	
#MyVerizonService and MyVerizonMobile
PRODUCT_PACKAGES += \
    MyVerizonService \
    MyVerizonMobile

# VZW APNLib
PRODUCT_PACKAGES += \
    VZWAPNLib_VZW \
    vzwapnlib.xml \
    feature_ehrpd.xml \
    feature_lte.xml
	
#Vzw Build API
PRODUCT_PACKAGES += \
    com.verizon.os \
    com.verizon.os.xml

# CDFS MODE remove sysem server
PRODUCT_PACKAGES += \
    Cdfs

# Add VUX Packages
PRODUCT_PACKAGES += \
    IgniteVerizon

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Remove Samsung Messages
PRODUCT_PACKAGES += \
    -SamsungMessages_11 \
    
# Remove DeviceSecurity for USA single
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
	
# Verizon Packages
PRODUCT_PACKAGES += \
    VzCloud


# QCOM OBDM
PRODUCT_PACKAGES += \
    OBDM_Permissions \
    obdm_permissions.xml \
    DMAT_Stub

# QCOM eMBMS MSDC
PRODUCT_PACKAGES += \
    QAS_DVC_MSP_VZW

# eMBMS VzW API library
PRODUCT_PACKAGES += \
    vzw_msdc_api

# eMBMS VzW API library
PRODUCT_COPY_FILES += \
    applications/par/edp/EMBMS/EmbmsQualcommMW/vzw/com.verizon.embms.xml:system/etc/permissions/com.verizon.embms.xml

# Add VZW SUA ISO
PRODUCT_COPY_FILES += \
    vendor/regional/usa/vzw/SUA_ISO/gta2xlltevzw/autorun.iso:system/etc/autorun.iso

# Remove KidsHome
PRODUCT_PACKAGES += \
    -KidsHome \
    -KidsHome_Installer
    
#remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v11
  
# UnifiedSettings
PRODUCT_PACKAGES += \
    UnifiedSettingService \
    unifiedsettinginterface \
    com.verizon.provider \
    com.verizon.provider.xml

# VZ System Properties
PRODUCT_PACKAGES += \
    vzsysprop \
    vzsysprop.rc