QCOM_PRODUCT_DEVICE := bloomq

$(call inherit-product, device/samsung/bloomq/device.mk)
include vendor/samsung/configs/bloomq_common/bloomq_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/bloomq/gms_bloomqkdi.mk
endif

PRODUCT_NAME := bloomqkdi
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := NYS
PRODUCT_FINGERPRINT_TYPE := jpn_kdi

include vendor/samsung/build/localelist/SecLocale_KDI.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# Remove Samsung Messages
PRODUCT_PACKAGES += \
    -SamsungMessages_11

# Add CellBroadcastReceiver
PRODUCT_PACKAGES += \
    SecCellBroadcastReceiver

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

include vendor/samsung/fac_vendor_common/fac_vendor_JPN.mk
endif
###############################################################	
# FactoryBinary only (end of line)
# PLEASE DO NOT ADD LINE BELOW
