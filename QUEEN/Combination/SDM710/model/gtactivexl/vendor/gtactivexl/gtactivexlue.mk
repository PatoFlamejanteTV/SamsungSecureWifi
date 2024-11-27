QCOM_PRODUCT_DEVICE := gtactivexl

$(call inherit-product, device/samsung/gtactivexl/device.mk)
include vendor/samsung/configs/gtactivexl_common/gtactivexl_common.mk

# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtactivexl/gms_gtactivexlue.mk
endif

PRODUCT_NAME := gtactivexlue
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T547U

include vendor/samsung/build/localelist/SecLocale_USA.mk

### Sprint OMADM, Chameleon and Sprint Hub Apps 
include vendor/samsung/configs/gtactivexl/spr_apps.mk
include vendor/samsung/configs/gtactivexl/vzwspr_apps.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Setup SBrowser Non-Removable
PRODUCT_PACKAGES += \
    -SBrowser_11.1_Removable \
    SBrowser_11.1

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast