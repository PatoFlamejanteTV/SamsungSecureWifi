QCOM_PRODUCT_DEVICE := a31

$(call inherit-product, device/samsung/a31/device.mk)
include vendor/samsung/configs/a31_common/a31_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a31/gms_a31xx.mk
endif

PRODUCT_NAME := a31xx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A315F
PRODUCT_MODEL := SM-A315F

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

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################