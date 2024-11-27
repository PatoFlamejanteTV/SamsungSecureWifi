$(call inherit-product, device/samsung/a21/device.mk)
include vendor/samsung/configs/a21_common/a21_common.mk

include vendor/samsung/configs/a21/gms_a21sq.mk

PRODUCT_NAME := a21sq
PRODUCT_DEVICE := a21
PRODUCT_MODEL := SM-A215U

include vendor/samsung/build/localelist/SecLocale_USA.mk

###############################################################
# Remove Packages not needed for USA
###############################################################
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
# Add Packages only needed for USA
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

# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome

# Setup Onedrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Setup SamsungPlus
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_REMOVABLE

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# TetheringProvision for ATT/VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision
	
# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Radio App Package
ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
endif

# GPS
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.location.gps.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.location.gps.xml

# GNSS HAL for HIDL
PRODUCT_PACKAGES += \
    vendor.samsung.hardware.gnss@2.0-service

# GNSS dependency
PRODUCT_PACKAGES += \
    libandroid_net \
    libDR \
    libgeofence \
    libcurl \
    gps_drv.ko

# MTK GPS
include vendor/samsung/hardware/gnss/mediatek/MT6631/common_us.mk

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter CHA SPT TMB TMK USC VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# CHA Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),CHA)
include vendor/samsung/configs/a21/a21sqcha_apps.mk
endif
# SPR Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
include vendor/samsung/configs/a21/a21sq_vzwspr_apps.mk
include vendor/samsung/configs/a21/a21sqspr_apps.mk
endif 
# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/a21/a21sqtmo_apps.mk
endif 
# TMK Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMK)
include vendor/samsung/configs/a21/a21sqmtr_apps.mk
endif 
# USC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),USC)
include vendor/samsung/configs/a21/a21squsc_apps.mk
endif 
# VZW Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),VZW)
include vendor/samsung/configs/a21/a21sq_vzwspr_apps.mk
include vendor/samsung/configs/a21/a21sqvzw_apps.mk
endif
else
# OYN SINGLESKU
# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# CHA Apps
include vendor/samsung/configs/a21/a21sqcha_apps.mk
# SPR Apps
include vendor/samsung/configs/a21/a21sq_vzwspr_apps.mk
include vendor/samsung/configs/a21/a21sqspr_apps.mk
# TMO Apps
include vendor/samsung/configs/a21/a21sqtmo_apps.mk
# TMK Apps
include vendor/samsung/configs/a21/a21sqmtr_apps.mk
# USC Apps
include vendor/samsung/configs/a21/a21squsc_apps.mk
# VZW Apps
include vendor/samsung/configs/a21/a21sqvzw_apps.mk
endif
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/a21/dummy.txt:system/carrier/CHA/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a21/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a21/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a21/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a21/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a21/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a21/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a21/dummy.txt:system/carrier/XAA/dummy.txt
