QCOM_PRODUCT_DEVICE := gts4lv

$(call inherit-product, device/samsung/gts4lv/device.mk)
include vendor/samsung/configs/gts4lv_common/gts4lv_common.mk


# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts4lv/gms_gts4lvusc.mk
endif

PRODUCT_NAME := gts4lvusc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T727R4

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0
	
# Removing WebManual
PRODUCT_PACKAGES += \
     -WebManual
	 
#remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v11

# USC 3rd party apps
PRODUCT_PACKAGES += \
    Ignite-uscc


# [CMC] Samsung CMC
  PRODUCT_PACKAGES += \
    MdecService

# Remove DeviceSecurity for USA single
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast