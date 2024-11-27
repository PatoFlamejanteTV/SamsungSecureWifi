QCOM_PRODUCT_DEVICE := z3q

$(call inherit-product, device/samsung/z3q/device.mk)
include vendor/samsung/configs/z3q_common/z3q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/z3q/gms_z3qjpnw.mk
endif

PRODUCT_NAME := z3qjpnw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G988Q

#change supported locale list same with KDI for JPN Open
#include vendor/samsung/build/localelist/SecLocale_JPN.mk
PRODUCT_LOCALES := ja_JP en_US ko_KR pt_BR vi_VN zh_CN es_ES fr_FR in_ID ml_IN ar_AE

# Samsung Japanese Keyboard
PRODUCT_PACKAGES += \
    HoneyBoardJPN \
    iWnnIME_Mushroom \
    -HoneyBoard

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# Add Samsung Memmbers
PRODUCT_PACKAGES += \
    SamsungMembers

# Add Samsung+ removable 12.01.08.7 as of Aug 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# Remove DeviceSecurity for USA single
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    -SetupWizard_USA \
    -HuxExtension
	
# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert	

# Remove SamsungConnect
PRODUCT_PACKAGES += \
    -SamsungConnect

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Add CellBroadcastReceiver
PRODUCT_PACKAGES += \
    SecCellBroadcastReceiver

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# UnifiedTetheringProvision for ATT, AIO, VZW
PRODUCT_PACKAGES += \
    -UnifiedTetheringProvision

# Add VZW FOTA/SDM
PRODUCT_PACKAGES += \
    -MnoDmClient \
    -MnoDmViewer \
    -mnodm.rc \
    -SDMHiddenMenu

# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    -SamsungPayStub

# Remove Bixby
PRODUCT_PACKAGES += \
    -Bixby

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


# Remove USP for user delivery
#ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
#PRODUCT_PACKAGES +=  \
#    -ARZone
#endif


###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_JPN.mk
endif
###############################################################
