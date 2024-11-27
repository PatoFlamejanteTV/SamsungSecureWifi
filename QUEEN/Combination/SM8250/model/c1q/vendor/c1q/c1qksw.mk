QCOM_PRODUCT_DEVICE := c1q

$(call inherit-product, device/samsung/c1q/device.mk)
include vendor/samsung/configs/c1q_common/c1q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/c1q/gms_c1qksw.mk
endif

PRODUCT_NAME := c1qksw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N981N

include vendor/samsung/build/localelist/SecLocale_KOR.mk



# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01

# Setup SBrowser removable
PRODUCT_PACKAGES += \
    -SBrowser_12.0 \
    SBrowser_12.0_Removable

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Add Samsung+ removable 12.01.08.7 as of Aug 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# Remove DeviceSecurity for USA single
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
    
# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension
	
# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert	

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# UnifiedTetheringProvision for ATT, AIO, VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    MnoDmClient \
    MnoDmViewer \
    mnodm.rc \
    SDMHiddenMenu

# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# SamsungEuicc Service and Client
PRODUCT_PACKAGES += \
    EuiccService
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccClient
endif

# Packages included only for eng or userdebug builds, previously debug tagged
PRODUCT_PACKAGES_DEBUG += \
    Keystring \
    AngryGPS \

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# BlockchainBasicKit (Supported Country) : EUR, KOR, USA, CAN, SEA/Oceania
# -BlockchainBasicKit (Not supported countries) : CHN, JPN, LDU device
PRODUCT_PACKAGES += \
    BlockchainBasicKit

###############################################################
# SingleSKU Carrier preload apps
###############################################################


# Remove USP for user delivery
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES +=  \
    -ARZone
endif

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone
	
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
