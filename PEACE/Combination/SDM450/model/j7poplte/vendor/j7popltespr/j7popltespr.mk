QCOM_PRODUCT_DEVICE := j7popltespr

$(call inherit-product, device/samsung/j7popltespr/device.mk)
include vendor/samsung/configs/j7poplte_common/j7poplte_common.mk

include build/target/product/product_launched_with_n.mk


# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/j7popltespr/gms_j7popltespr.mk
endif

PRODUCT_NAME := j7popltespr
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-J727P

include vendor/samsung/build/localelist/SecLocale_USA.mk
    

# Microsoft Apps
PRODUCT_PACKAGES += \
    -MSSkype_stub
    
# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# VUX Packages
	
# VZ System Properties
PRODUCT_PACKAGES += \
    vzsysprop

# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual
	
# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/hw/init.carrier.rc	

# Add Radio App Package
ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    imsmanager \
    ImsTelephonyService \
    imsservice \
    vsimmanager \
    vsimservice \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imscoremanager \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    RcsSettings
endif

# SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# CDFS MODE remove sysem server
#PRODUCT_PACKAGES += \
#   Cdfs
  
# Add Hancom Viewer for Smartphone
PRODUCT_PACKAGES += \
    HancomOfficeViewer

# Add Accessibility
PRODUCT_PACKAGES += \
    STalkback \
    UniversalSwitch

# Add watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub

# Remove SmartManager
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# removing Sticker Camera packages
PRODUCT_PACKAGES += \
    -StickerFaceAR \
    -StickerStamp \
    -StickerProvider

# Sprint OMADM and Preload Apps
include vendor/samsung/configs/j7popltespr/spr_apps.mk
