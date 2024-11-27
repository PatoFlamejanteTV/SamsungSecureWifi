QCOM_PRODUCT_DEVICE := r8q

$(call inherit-product, device/samsung/r8q/device.mk)
include vendor/samsung/configs/r8q_common/r8q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/r8q/gms_r8qsqw.mk
endif

PRODUCT_NAME := r8qsqw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G781V

include vendor/samsung/build/localelist/SecLocale_USA.mk




# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert   
   
# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Setup SBrowser Non-Removable
PRODUCT_PACKAGES += \
    -SBrowser_11.2_Removable \
    SBrowser_11.2

# Add Samsung+ removable 12.01.08.7 as of Aug 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Remove DeviceSecurity for USA single
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# UnifiedTetheringProvision for ATT, AIO, VZW
PRODUCT_PACKAGES += \
	UnifiedTetheringProvision
	
# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    MnoDmClient \
    MnoDmViewer \
    mnodm.rc \
    SDMHiddenMenu

# SamsungEuicc Service and Client
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccService \
    EuiccClient
endif

# Packages included only for eng or userdebug builds, previously debug tagged
PRODUCT_PACKAGES_DEBUG += \
    Keystring \
    AngryGPS \


###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_$(SEC_BUILD_OPTION_SINGLESKU_CUST).mk

else

# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_VZW.mk
endif

endif

# Remove USP for user delivery
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES +=  \
    -ARZone
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/VZW/dummy.txt
	
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
