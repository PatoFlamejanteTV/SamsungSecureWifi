QCOM_PRODUCT_DEVICE := y2q

$(call inherit-product, device/samsung/y2q/device.mk)
include vendor/samsung/configs/y2q_common/y2q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/y2q/gms_y2qkdiw.mk
endif

PRODUCT_NAME := y2qkdiw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := AJL
PRODUCT_BRAND := KDDI
PRODUCT_FINGERPRINT_TYPE := jpn_kdi

include vendor/samsung/build/localelist/SecLocale_JPN.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# Add Samsung+ removable 12.01.08.7 as of Aug 2019
PRODUCT_PACKAGES += \
    SAMSUNG_PLUS_REMOVABLE

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

#EmergencyAlert apps
PRODUCT_PACKAGES += \
    EmergencyAlert 	
	
# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add OneDrive Removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# SamsungEuicc Service and Client
PRODUCT_PACKAGES += \
    EuiccService
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccClient
endif

# Remove eSE features
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
