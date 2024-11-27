QCOM_PRODUCT_DEVICE := r8q

$(call inherit-product, device/samsung/r8q/device.mk)
include vendor/samsung/configs/r8q_common/r8q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/r8q/gms_r8qxxx.mk
endif

PRODUCT_NAME := r8qxxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G781B
   
#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert   
   
# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Samsung+ removable 12.01.08.7 as of Aug 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

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
