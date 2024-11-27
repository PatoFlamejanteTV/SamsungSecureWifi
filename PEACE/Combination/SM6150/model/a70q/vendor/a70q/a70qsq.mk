QCOM_PRODUCT_DEVICE := a70q

$(call inherit-product, device/samsung/a70q/device.mk)
include vendor/samsung/configs/a70q_common/a70q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a70q/gms_a70qsq.mk
endif

PRODUCT_NAME := a70qsq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A705U

PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_NAME).rc:vendor/etc/init/hw/init.carrier.rc

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# for HiddenMenu
PRODUCT_PACKAGES += \
    HiddenMenu

# for InputEventApp
PRODUCT_PACKAGES += \
    InputEventApp \
    libDiagService_

# Add S Voice IME
PRODUCT_PACKAGES += \
    SVoiceIME

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_9.0_Removable \
    SBrowser_9.0

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# ATT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),ATT)
include vendor/samsung/configs/a70q/a70qsqatt_apps.mk
endif
# SPR Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
include vendor/samsung/configs/a70q/a70qsqspr_apps.mk
endif 
# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/a70q/a70qsqtmo_apps.mk
endif 
# USC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),USC)
include vendor/samsung/configs/a70q/a70qsqusc_apps.mk
endif 
# VZW Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),VZW)
include vendor/samsung/configs/a70q/a70qsqvzw_apps.mk
endif
else
# OYN SINGLESKU
# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# ATT Apps
include vendor/samsung/configs/a70q/a70qsqatt_apps.mk
# SPR Apps
include vendor/samsung/configs/a70q/a70qsqspr_apps.mk
# TMO Apps
include vendor/samsung/configs/a70q/a70qsqtmo_apps.mk
# USC Apps
include vendor/samsung/configs/a70q/a70qsqusc_apps.mk
# VZW Apps
include vendor/samsung/configs/a70q/a70qsqvzw_apps.mk
endif
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/a70q/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a70q/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a70q/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a70q/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a70q/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a70q/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a70q/dummy.txt:system/carrier/XAA/dummy.txt

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################	

