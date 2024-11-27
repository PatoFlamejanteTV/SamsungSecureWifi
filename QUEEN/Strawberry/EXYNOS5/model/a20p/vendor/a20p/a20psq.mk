BUILD_SYMLINK_TO_CARRIER := true

$(call inherit-product, device/samsung/a20p/device.mk)
include vendor/samsung/configs/a20p_common/a20p_common.mk

include build/target/product/product_launched_with_p.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a20p/gms_a20psq.mk
endif

PRODUCT_NAME := a20psq
PRODUCT_DEVICE := a20p
PRODUCT_MODEL := SM-A205U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

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

# Remove Voice Recorder
PRODUCT_PACKAGES += \
    -VoiceNote_5.0

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Remove WebManual DO NOT add this package for USA and JPN
PRODUCT_PACKAGES += \
    -WebManual

# Remove Dictionary for Sec
PRODUCT_PACKAGES += \
    -DictDiotekForSec

# Remove Microsoft OfficeMobile Stub
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub

# Add Microsoft OfficeMobile headless for USA
PRODUCT_PACKAGES += \
    OfficeMobile_SamsungHeadless

# Add Onedrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Samsung+ removable
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_REMOVABLE

# Remove SmartManager
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Add USA SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension
	
# TetheringProvision for VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter CHA SPT TMB TMK USC VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

# SPR Preload Apps ONLY
     ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SPT)
        include vendor/samsung/configs/a20p/a20psq_apps_SPR_VZW.mk
        include vendor/samsung/configs/a20p/a20psq_apps_SPR.mk
     endif
# TMO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMB)
include vendor/samsung/configs/a20p/a20psqtmo_apps.mk
endif 
# TMK Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),TMK)
include vendor/samsung/configs/a20p/a20psqmtr_apps.mk
endif	 
else
# OYN SINGLESKU
# FactoryBinary doesn't need carrier packages.
	ifneq ($(SEC_FACTORY_BUILD),true)
# SPR Apps
        include vendor/samsung/configs/a20p/a20psq_apps_SPR_VZW.mk
        include vendor/samsung/configs/a20p/a20psq_apps_SPR.mk
# TMO Apps
include vendor/samsung/configs/a20p/a20psqtmo_apps.mk
# TMK Apps
include vendor/samsung/configs/a20p/a20psqmtr_apps.mk
    endif
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################	