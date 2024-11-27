QCOM_PRODUCT_DEVICE := d2xq

$(call inherit-product, device/samsung/d2xq/device.mk)
include vendor/samsung/configs/d2xq_common/d2xq_common.mk

#include vendor/samsung/configs/d2xq/gms_d2xqsq.mk

PRODUCT_NAME := d2xqsq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N976U


# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00


# Add BlockchainBasicKit
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
