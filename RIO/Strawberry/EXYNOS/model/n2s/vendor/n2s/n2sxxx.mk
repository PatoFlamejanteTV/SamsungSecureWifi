$(call inherit-product, device/samsung/n2s/device.mk)
include vendor/samsung/configs/n2s_common/n2s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/n2s/gms_n2sxxx.mk
endif


PRODUCT_NAME := n2sxxx
PRODUCT_DEVICE := n2s
PRODUCT_MODEL := SM-G996B

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