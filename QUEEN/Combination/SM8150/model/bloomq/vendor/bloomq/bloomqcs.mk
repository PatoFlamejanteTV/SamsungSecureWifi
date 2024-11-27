QCOM_PRODUCT_DEVICE := bloomq

$(call inherit-product, device/samsung/bloomq/device.mk)
include vendor/samsung/configs/bloomq_common/bloomq_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/bloomq/gms_bloomqcs.mk
endif

PRODUCT_NAME := bloomqcs
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-F700W

include vendor/samsung/build/localelist/SecLocale_CAN.mk



# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# [CMC] Samsung CMC
  PRODUCT_PACKAGES += \
    MdecService

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

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
# FactoryBinary only (end of line)
# PLEASE DO NOT ADD LINE BELOW

# for HiddenMenu
PRODUCT_PACKAGES += \
    HiddenMenu
