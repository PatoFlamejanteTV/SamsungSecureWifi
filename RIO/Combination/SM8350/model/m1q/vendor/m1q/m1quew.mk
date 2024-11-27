QCOM_PRODUCT_DEVICE := m1q

$(call inherit-product, device/samsung/m1q/device.mk)
include vendor/samsung/configs/m1q_common/m1q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/m1q/gms_m1quew.mk
endif

PRODUCT_NAME := m1quew
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G991U1

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

# Add Verizon SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add UnifiedTetheringProvision for ATT/VZW/AIO
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# SamsungEuicc Service and Client
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccService \
    EuiccClient
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
