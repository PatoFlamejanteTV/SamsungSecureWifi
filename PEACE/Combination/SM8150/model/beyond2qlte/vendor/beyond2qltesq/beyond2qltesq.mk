QCOM_PRODUCT_DEVICE := beyond2qltesq

$(call inherit-product, device/samsung/beyond2qltesq/device.mk)
include vendor/samsung/configs/beyond2qlte_common/beyond2qlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyond2qltesq/gms_beyond2qltesq.mk
endif

PRODUCT_NAME := beyond2qltesq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G975U

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
  -SBrowser_9.0_Removable \
  SBrowser_9.0

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Samsung+ removable 10.19.1.3 as of Aug 2018
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# CMAS for ATT/VZW
PRODUCT_PACKAGES += \
    EmergencyAlert

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast


# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

# Remove USP for customer delivery (-e omce)
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES += \
    -HybridRadio_P
endif

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    NSDSWebApp

# SingleSKU Carrier preload apps
ifneq ($(filter ATT CCT CHA SPT TMB USC VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# ATT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),ATT)
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqatt_apps.mk
endif
# CCT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),CCT)
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqcct_apps.mk
endif
# CHA Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),CHA)
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqcha_apps.mk
endif 
# SPR Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
include vendor/samsung/configs/beyond2qltesq/beyond2qltesq_vzwspr_apps.mk
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqspr_apps.mk
endif 
# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqtmo_apps.mk
endif 
# USC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),USC)
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqusc_apps.mk
endif 
# VZW Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),VZW)
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqvzw_apps.mk
include vendor/samsung/configs/beyond2qltesq/beyond2qltesq_vzwspr_apps.mk
endif
else
# OYN SINGLESKU
# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# ATT Preload & removable Apps
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqatt_apps.mk
# CCT Apps
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqcct_apps.mk
# CHA Apps
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqcha_apps.mk
# Sprint Preload & removable Apps
include vendor/samsung/configs/beyond2qltesq/beyond2qltesq_vzwspr_apps.mk
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqspr_apps.mk
# TMO Apps
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqtmo_apps.mk
# USC Apps
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqusc_apps.mk
# VzW Apps
include vendor/samsung/configs/beyond2qltesq/beyond2qltesqvzw_apps.mk
endif
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/CCT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/CHA/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/beyond2qltesq/dummy.txt:system/carrier/XAA/dummy.txt

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

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
