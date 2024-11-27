$(call inherit-product, device/samsung/a51x/device.mk)
include vendor/samsung/configs/a51x_common/a51x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a51x/gms_a51xue.mk
endif

PRODUCT_NAME := a51xue
PRODUCT_DEVICE := a51x
PRODUCT_MODEL := SM-A516U1

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit
    
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
