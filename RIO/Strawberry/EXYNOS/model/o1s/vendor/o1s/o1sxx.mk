$(call inherit-product, device/samsung/o1s/device.mk)
include vendor/samsung/configs/o1s_common/o1s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/o1s/gms_o1sxx.mk
endif


PRODUCT_NAME := o1sxx
PRODUCT_DEVICE := o1s
PRODUCT_MODEL := SM-G990F

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# BlockchainBasicKit (Supported Country) : EUR, KOR, USA, CAN, SEA/Oceania
# -BlockchainBasicKit (Not supported countries) : CHN, JPN, LDU device
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# SamsungEuicc Service and Client
PRODUCT_PACKAGES += \
    EuiccService
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccClient
endif


# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

















# ADDED ANYTHING YOU NEED BEFORE THIS LINE.
# BELOW THIS LINE IS ONLY FOR FACTORY BINARY.
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
# DO NOT ADD ANYTHING AFTER THIS LINE. ADD BEFORE "END OF FILE" LINE.