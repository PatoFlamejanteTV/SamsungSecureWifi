QCOM_PRODUCT_DEVICE := r8q

$(call inherit-product, device/samsung/r8q/device.mk)
include vendor/samsung/configs/r8q_common/r8q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/r8q/gms_r8qsqx.mk
endif

PRODUCT_NAME := r8qsqx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G781U

include vendor/samsung/build/localelist/SecLocale_USA.mk


#############################################################
# Remove Packages not needed for USA
###############################################################
# Remove Upday
PRODUCT_PACKAGES += \
    -Upday

# Remove Voice Recorder
PRODUCT_PACKAGES += \
    -VoiceNote_5.0

# Add WebManual DO NOT add this package for USA and JPN
PRODUCT_PACKAGES += \
    -WebManual

# Remove SmartManager for USA
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
###############################################################

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Crane
PRODUCT_PACKAGES += \
    Crane

# EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert   
 
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Setup SBrowser Non-Removable
PRODUCT_PACKAGES += \
    -SBrowser_11.2_Removable \
    SBrowser_11.2

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# UnifiedTetheringProvision for ATT, AIO, VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

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
ifneq ($(filter ATT CCT CHA SPR TMB TMK USC XAA, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_$(SEC_BUILD_OPTION_SINGLESKU_CUST).mk

ifneq ($(filter SPR, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

else

ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_ATT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMB.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMK.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_USC.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_XAA.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/XAA/dummy.txt
	
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
