QCOM_PRODUCT_DEVICE := r5q

$(call inherit-product, device/samsung/r5q/device.mk)
include vendor/samsung/configs/r5q_common/r5q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r5q/gms_r5qzh.mk
endif

PRODUCT_NAME := r5qzh
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G770F


# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00
PRODUCT_PACKAGES += smt_en_GB_f00

#FM Radio
PRODUCT_PACKAGES += \
    HybridRadio

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
