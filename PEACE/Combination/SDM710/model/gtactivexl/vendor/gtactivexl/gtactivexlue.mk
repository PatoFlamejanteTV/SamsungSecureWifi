QCOM_PRODUCT_DEVICE := gtactivexl

$(call inherit-product, device/samsung/gtactivexl/device.mk)
include vendor/samsung/configs/gtactivexl_common/gtactivexl_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtactivexl/gms_gtactivexlue.mk
endif

PRODUCT_NAME := gtactivexlue
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T547U

include vendor/samsung/build/localelist/SecLocale_USA.mk

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    -NSDSWebApp

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add USA SetupWizard
PRODUCT_PACKAGES += \
    SetupWizard_USA \
	
# for HiddenMenu
PRODUCT_PACKAGES += \
    HiddenMenu
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
    
    ### Sprint OMADM, Chameleon and Sprint Hub Apps 
include vendor/samsung/configs/gtactivexl/spr_apps.mk
include vendor/samsung/configs/gtactivexl/vzwspr_apps.mk
