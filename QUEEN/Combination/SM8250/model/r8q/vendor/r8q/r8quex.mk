QCOM_PRODUCT_DEVICE := r8q

$(call inherit-product, device/samsung/r8q/device.mk)
include vendor/samsung/configs/r8q_common/r8q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/r8q/gms_r8quex.mk
endif

PRODUCT_NAME := r8quex
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G781U1

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

# Add unbranded SSO and APNService
PRODUCT_PACKAGES += \
    LoginClientUnbranded \
    VZWAPNService_VZW \
    VZWAPNLib_VZW \
    vzwapnlib.xml \
    feature_ehrpd.xml \
    feature_lte.xml \
    feature_srlte.xml	
	
# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

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
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
