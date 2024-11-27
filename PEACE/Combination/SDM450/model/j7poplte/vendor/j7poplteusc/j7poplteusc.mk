QCOM_PRODUCT_DEVICE := j7poplteusc

$(call inherit-product, device/samsung/j7poplteusc/device.mk)
include vendor/samsung/configs/j7poplte_common/j7poplte_common.mk

include build/target/product/product_launched_with_n.mk



# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/j7poplteusc/gms_j7poplteusc.mk
endif

PRODUCT_NAME := j7poplteusc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-J727R4

include vendor/samsung/build/localelist/SecLocale_USA.mk

    
# Add SHealth Stub
PRODUCT_PACKAGES += \
	-SHealth5 \
    SHealthStub

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

#Remove Samsung FOTA client
PRODUCT_PACKAGES += \
    -FotaAgent \
    -OmaCP

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

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add Memory Solutions
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# QCOM eMBMS MSDC
PRODUCT_PACKAGES += \
    QAS_DVC_MSP_VZW

# CDFS MODE remove sysem server
PRODUCT_PACKAGES += \
    Cdfs

# eMBMS VzW API library
PRODUCT_PACKAGES += \
    vzw_msdc_api
	
# eMBMS VzW API library
PRODUCT_COPY_FILES += \
   applications/par/edp/EMBMS/EmbmsQualcommMW/vzw/com.verizon.embms.xml:system/etc/permissions/com.verizon.embms.xml
   
# Add Hancom Viewer for Smartphone
PRODUCT_PACKAGES += \
    HancomOfficeViewer

# Remove SmartManager
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
	
# Add KidsHome
PRODUCT_PACKAGES += \
    KidsHome_Installer