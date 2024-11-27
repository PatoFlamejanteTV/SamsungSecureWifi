$(call inherit-product, device/samsung/a10e/device.mk)
include vendor/samsung/configs/a10e_common/a10e_common.mk

include build/target/product/product_launched_with_p.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a10e/gms_a10esq.mk
endif

PRODUCT_NAME := a10esq
PRODUCT_DEVICE := a10e
PRODUCT_MODEL := SM-A102U

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Facebook apps removable
PRODUCT_PACKAGES += \
    -Facebook_stub \
    Facebook_stub_Removable

# Removing LinkedIn Stub
PRODUCT_PACKAGES += \
    -LinkedIn_SamsungStub_Deletable


# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
endif

# Remove upday
PRODUCT_PACKAGES += \
    -Upday
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01
	
# Remove HiyaService
PRODUCT_PACKAGES += \
  -HiyaService
  
# CMAS for ATT/VZW/AIO
PRODUCT_PACKAGES += \
    EmergencyAlert

#DEX_IN_DATA
PRODUCT_DEX_PREOPT_PACKAGES_CORE_APP_IN_DATA += \
	imsservice

# Add Subscription Calendar
PRODUCT_PACKAGES += \
    OpenCalendar

# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter ATT AIO CHA SPT TMB TMK USC VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

# ATT Preload Apps ONLY
	ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),ATT)
		include vendor/samsung/configs/a10e/a10esq_apps_ATT.mk
	endif
# AIO Preload Apps ONLY
	ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),AIO)
		include vendor/samsung/configs/a10e/a10esq_apps_AIO.mk
	endif
# CHA Preload Apps ONLY
	ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),CHA)
		include vendor/samsung/configs/a10e/a10esq_apps_CHA.mk
	endif
# SPR Preload Apps ONLY
     ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
        include vendor/samsung/configs/a10e/a10esq_apps_SPR_VZW.mk
        include vendor/samsung/configs/a10e/a10esq_apps_SPR.mk
     endif 
# TMO Preload Apps ONLY
	ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
		include vendor/samsung/configs/a10e/a10esq_apps_TMB.mk
	endif 
# TMK Preload Apps ONLY
	ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMK)
		include vendor/samsung/configs/a10e/a10esq_apps_TMK.mk
	endif 
# USC Preload Apps ONLY
	ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),USC)
		include vendor/samsung/configs/a10e/a10esq_apps_USC.mk
	endif 
# VZW Preload Apps ONLY
	ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),VZW)
		include vendor/samsung/configs/a10e/a10esq_apps_SPR_VZW.mk
		include vendor/samsung/configs/a10e/a10esq_apps_VZW.mk
	endif
else
# OYN SINGLESKU
# FactoryBinary doesn't need carrier packages.
	ifneq ($(SEC_FACTORY_BUILD),true)
# ATT Apps
        include vendor/samsung/configs/a10e/a10esq_apps_ATT.mk
# AIO Apps
        include vendor/samsung/configs/a10e/a10esq_apps_AIO.mk
# CHA Apps
		include vendor/samsung/configs/a10e/a10esq_apps_CHA.mk
# SPR Apps
        include vendor/samsung/configs/a10e/a10esq_apps_SPR_VZW.mk
        include vendor/samsung/configs/a10e/a10esq_apps_SPR.mk
# TMO Apps
	include vendor/samsung/configs/a10e/a10esq_apps_TMB.mk
# TMK Apps
	include vendor/samsung/configs/a10e/a10esq_apps_TMK.mk
# USC Apps
	include vendor/samsung/configs/a10e/a10esq_apps_USC.mk
# VZW Apps
	include vendor/samsung/configs/a10e/a10esq_apps_VZW.mk
    endif
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/a10e/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a10e/dummy.txt:system/carrier/AIO/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a10e/dummy.txt:system/carrier/CHA/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a10e/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a10e/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a10e/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a10e/dummy.txt:system/carrier/USC/dummy.txt

PRODUCT_COPY_FILES +=vendor/samsung/configs/a10e/dummy.txt:system/carrier/VZW/dummy.txt


# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Lassen common
include vendor/samsung/hardware/gnss/slsi/lassen/common_evolution_us.mk

# GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.na.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.na.cdma.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps_CDMA.cfg
endif