QCOM_PRODUCT_DEVICE := a41

$(call inherit-product, device/samsung/a41/device.mk)
include vendor/samsung/configs/a41_common/a41_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a41/gms_a41xx.mk
endif


PRODUCT_NAME := a41xx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A415F
PRODUCT_LOCALES := ru_RU $(filter-out ru_RU,$(PRODUCT_LOCALES))

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