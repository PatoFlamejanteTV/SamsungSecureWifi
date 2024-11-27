BUILD_SYMLINK_TO_CARRIER := true

$(call inherit-product, device/samsung/a51/device.mk)
include vendor/samsung/configs/a51_common/a51_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a51/gms_a51sq.mk
endif

PRODUCT_NAME := a51sq
PRODUCT_DEVICE := a51
PRODUCT_MODEL := SM-A515U

include vendor/samsung/build/localelist/SecLocale_USA.mk

###############################################################
# Remove Packages not needed for USA
###############################################################
# Remove Upday
PRODUCT_PACKAGES += \
    -Upday

# Remove Voice Recorder
PRODUCT_PACKAGES += \
    -VoiceNote_5.0

# Remove GLISPA App Package
PRODUCT_PACKAGES += \
    -GLISPA	

# Remove Ignite App Package
PRODUCT_PACKAGES += \
    -AURA

# Add WebManual DO NOT add this package for USA and JPN
PRODUCT_PACKAGES += \
    -WebManual

# Remove SmartManager for USA
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
###############################################################

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add HiddenMenu for VZW
PRODUCT_PACKAGES += \
    HiddenMenu

# Add InputEventApp for VZW
PRODUCT_PACKAGES += \
    InputEventApp \
    libDiagService_

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# TetheringProvision for ATT/VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# Add Microsoft OfficeMobile headless for USA
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub \
    OfficeMobile_SamsungHeadless

# Add Onedrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Samsung+ removable
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_REMOVABLE

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
endif

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Ramen common
include vendor/samsung/hardware/gnss/slsi/ramen/common_evolution_us_64.mk

# Ramen GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else 
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.na.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.na.cdma.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps_CDMA.cfg
endif

# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter ATT AIO CCT CHA SPT TMB TMK USC VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# ATT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),ATT)
include vendor/samsung/configs/a51/a51sqatt_apps.mk
endif
# AIO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),AIO)
include vendor/samsung/configs/a51/a51sqaio_apps.mk
endif
# CCT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),CCT)
include vendor/samsung/configs/a51/a51sqcct_apps.mk
endif
# CHA Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),CHA)
include vendor/samsung/configs/a51/a51sqcha_apps.mk
endif
# SPR Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
include vendor/samsung/configs/a51/a51sq_vzwspr_apps.mk
include vendor/samsung/configs/a51/a51sqspr_apps.mk
endif 
# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/a51/a51sqtmo_apps.mk
endif 
# TMK Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMK)
include vendor/samsung/configs/a51/a10esqmtr_apps.mk
endif 
# USC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),USC)
include vendor/samsung/configs/a51/a51squsc_apps.mk
endif 
# VZW Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),VZW)
include vendor/samsung/configs/a51/a51sq_vzwspr_apps.mk
include vendor/samsung/configs/a51/a51sqvzw_apps.mk
endif
else
# OYN SINGLESKU
# ATT Apps
include vendor/samsung/configs/a51/a51sqatt_apps.mk
# AIO Apps
include vendor/samsung/configs/a51/a51sqaio_apps.mk
# CCT Apps
include vendor/samsung/configs/a51/a51sqcct_apps.mk
# CHA Apps
include vendor/samsung/configs/a51/a51sqcha_apps.mk
# SPR Apps
include vendor/samsung/configs/a51/a51sq_vzwspr_apps.mk
include vendor/samsung/configs/a51/a51sqspr_apps.mk
# TMO Apps
include vendor/samsung/configs/a51/a51sqtmo_apps.mk
# TMK Apps
include vendor/samsung/configs/a51/a51sqmtr_apps.mk
# USC Apps
include vendor/samsung/configs/a51/a51squsc_apps.mk
# VZW Apps
include vendor/samsung/configs/a51/a51sqvzw_apps.mk
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/AIO/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/CCT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/CHA/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51/dummy.txt:system/carrier/XAA/dummy.txt
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################	