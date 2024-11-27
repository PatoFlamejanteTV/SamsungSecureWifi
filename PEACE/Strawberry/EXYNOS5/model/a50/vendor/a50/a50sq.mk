PRODUCT_COPY_FILES += device/samsung/a50_common/init.a50sq.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.exynos9610.rc

$(call inherit-product, device/samsung/a50/device.mk)
include vendor/samsung/configs/a50_common/a50_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a50/gms_a50sq.mk
endif

PRODUCT_NAME := a50sq
PRODUCT_DEVICE := a50
PRODUCT_MODEL := SM-A505U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# for HiddenMenu
PRODUCT_PACKAGES += \
    HiddenMenu

# for InputEventApp
PRODUCT_PACKAGES += \
    InputEventApp \
    libDiagService_
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# ATT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),ATT)
include vendor/samsung/configs/a50/a50sqatt_apps.mk
endif
# SPR Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
include vendor/samsung/configs/a50/a50sq_vzwspr_apps.mk
include vendor/samsung/configs/a50/a50sqspr_apps.mk
endif 
# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/a50/a50sqtmo_apps.mk
endif 
# USC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),USC)
include vendor/samsung/configs/a50/a50squsc_apps.mk
endif 
# VZW Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),VZW)
include vendor/samsung/configs/a50/a50sqvzw_apps.mk
endif
else
# OYN SINGLESKU
# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# ATT Apps
include vendor/samsung/configs/a50/a50sqatt_apps.mk
# SPR Apps
include vendor/samsung/configs/a50/a50sq_vzwspr_apps.mk
include vendor/samsung/configs/a50/a50sqspr_apps.mk
# TMO Apps
include vendor/samsung/configs/a50/a50sqtmo_apps.mk
# USC Apps
include vendor/samsung/configs/a50/a50squsc_apps.mk
# VZW Apps
include vendor/samsung/configs/a50/a50sqvzw_apps.mk
endif
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/a50/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a50/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a50/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a50/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a50/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a50/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a50/dummy.txt:system/carrier/XAA/dummy.txt

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################	
