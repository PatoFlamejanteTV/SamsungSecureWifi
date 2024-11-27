QCOM_PRODUCT_DEVICE := t2q

$(call inherit-product, device/samsung/t2q/device.mk)
include vendor/samsung/configs/t2q_common/t2q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/t2q/gms_t2qsqw.mk
endif

PRODUCT_NAME := t2qsqw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G996U

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# BlockchainBasicKit (Supported Country) : EUR, KOR, USA, CAN, SEA/Oceania
# -BlockchainBasicKit (Not supported countries) : CHN, JPN, LDU device
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Removing WebManual
PRODUCT_PACKAGES += \
    -WebManual
	
   
# Packages included only for eng or userdebug builds, previously debug tagged
PRODUCT_PACKAGES_DEBUG += \
    Keystring \
    AngryGPS \

# Add UnifiedTetheringProvision for ATT/VZW/AIO
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Add Verizon SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# SamsungEuicc Service and Client
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccService \
    EuiccClient
endif

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter AIO ATT CCT CHA SPR TMB TMK USC VZW XAA, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_$(SEC_BUILD_OPTION_SINGLESKU_CUST).mk

ifneq ($(filter SPR VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

else

ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_AIO.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_ATT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_CCT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_CHA.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMB.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMK.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_USC.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_VZW.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_XAA.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

endif
###############################################################
# SingleSKU Carrier preload apps
###############################################################

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/AIO/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/CCT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/CHA/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/XAA/dummy.txt

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
