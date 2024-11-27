$(call inherit-product, device/samsung/r7/device.mk)
include vendor/samsung/configs/r7_common/r7_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r7/gms_r7zh.mk
endif

PRODUCT_NAME := r7zh
PRODUCT_DEVICE := r7
PRODUCT_MODEL := SM-N770F

	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# [CMC] Samsung CMC
  PRODUCT_PACKAGES += \
    MdecService
	
# Add Ignite App Package
PRODUCT_PACKAGES += \
    IGNITE	

# Add Crane
PRODUCT_PACKAGES += \
  Crane

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
MSPAPP_ADDITIONAL_DAT_LIST := SM-N770FM
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
