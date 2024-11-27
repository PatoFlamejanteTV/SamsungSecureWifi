QCOM_PRODUCT_DEVICE := a71

$(call inherit-product, device/samsung/a71/device.mk)
include vendor/samsung/configs/a71_common/a71_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a71/gms_a71xx.mk
endif

PRODUCT_NAME := a71xx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A715F

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

MSPAPP_ADDITIONAL_DAT_LIST := SM-A715F SM-A715FN SM-A715FM

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
