QCOM_PRODUCT_DEVICE := gts4lv

$(call inherit-product, device/samsung/gts4lv/device.mk)
include vendor/samsung/configs/gts4lv_common/gts4lv_common.mk

include build/target/product/product_launched_with_p.mk


# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts4lv/gms_gts4lvue.mk
endif

PRODUCT_NAME := gts4lvue
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T727U

include vendor/samsung/build/localelist/SecLocale_USA.mk




# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0
	
# Removing WebManual
PRODUCT_PACKAGES += \
     -WebManual
	 
#remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v11
  
# Add Verizon SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA

# add CDFS MODE
PRODUCT_PACKAGES += \
    Cdfs

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
# QCOM eMBMS MSDC
PRODUCT_PACKAGES += \
    QAS_DVC_MSP_VZW

# eMBMS VzW API library
PRODUCT_PACKAGES += \
    vzw_msdc_api


# QCOM OBDM
PRODUCT_PACKAGES += \
    OBDM_Permissions \
    obdm_permissions.xml \
    DMAT_Stub


# eMBMS VzW API library
PRODUCT_COPY_FILES += \
   applications/par/edp/EMBMS/EmbmsQualcommMW/vzw/com.verizon.embms.xml:system/etc/permissions/com.verizon.embms.xml
	
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
	
#VZW apps
	
# Verizon Packages
	
# ALM
PRODUCT_PACKAGES += \
    DumpCollector

# [CMC] Samsung CMC
  PRODUCT_PACKAGES += \
    MdecService

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar