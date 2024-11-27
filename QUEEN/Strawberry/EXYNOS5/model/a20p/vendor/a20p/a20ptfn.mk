BUILD_SYMLINK_TO_CARRIER := true

$(call inherit-product, device/samsung/a20p/device.mk)
include vendor/samsung/configs/a20p_common/a20p_common.mk

include build/target/product/product_launched_with_p.mk


# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a20p/gms_a20ptfn.mk
endif

PRODUCT_NAME := a20ptfn
PRODUCT_DEVICE := a20p
PRODUCT_MODEL := SM-S205DL

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Remove Voice Recorder
PRODUCT_PACKAGES += \
    -VoiceNote_5.0

# Remove WebManual DO NOT add this package for USA and JPN
PRODUCT_PACKAGES += \
    -WebManual

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Remove Dictionary for Sec
PRODUCT_PACKAGES += \
    -DictDiotekForSec

# Remove Microsoft OfficeMobile Stub
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub

# Add Microsoft OfficeMobile headless for USA
PRODUCT_PACKAGES += \
    OfficeMobile_SamsungHeadless

# Add Onedrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Samsung+ removable
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_REMOVABLE

# Remove SmartManager
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Add USA SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################	