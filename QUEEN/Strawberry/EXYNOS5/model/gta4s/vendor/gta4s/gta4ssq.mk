$(call inherit-product, device/samsung/gta4s/device.mk)
include vendor/samsung/configs/gta4s_common/gta4s_common.mk

include build/target/product/product_launched_with_p.mk



# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta4s/gms_gta4ssq.mk
endif

PRODUCT_NAME := gta4ssq
PRODUCT_DEVICE := gta4s
PRODUCT_MODEL := SM-T307U

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    UnifiedWFC
endif

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Verizon SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter VZW ATT TMB SPT USC AIO TMK TFN CCT, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/gta4s/gta4ssq_apps_TMB.mk
endif 
# ATT Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),ATT)
include vendor/samsung/configs/gta4s/gta4ssq_apps_ATT.mk
endif 
# SPR Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
include vendor/samsung/configs/gta4s/gta4ssq_apps_SPR.mk
include vendor/samsung/configs/gta4s/gta4ssq_apps_SPR_VZW.mk
endif 


else

# OYN SINGLESKU
# Include VZW,SPR common library
include vendor/samsung/configs/gta4s/gta4ssq_apps_SPR_VZW.mk
# Sprint Preload & removable Apps
include vendor/samsung/configs/gta4s/gta4ssq_apps_SPR.mk
# TMO Apps
include vendor/samsung/configs/gta4s/gta4ssq_apps_TMB.mk
# ATT Apps
include vendor/samsung/configs/gta4s/gta4ssq_apps_ATT.mk


ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_ATT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMB.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_USC.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_VZW.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

endif

PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/VZW/dummy.txt

# Lassen common
    include vendor/samsung/hardware/gnss/slsi/lassen/common_evolution_us_64.mk

# GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.na.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.na.cdma.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps_CDMA.cfg
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast