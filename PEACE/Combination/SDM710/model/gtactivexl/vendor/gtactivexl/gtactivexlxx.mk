QCOM_PRODUCT_DEVICE := gtactivexl

$(call inherit-product, device/samsung/gtactivexl/device.mk)
include vendor/samsung/configs/gtactivexl_common/gtactivexl_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtactivexl/gms_gtactivexlxx.mk
endif

PRODUCT_NAME := gtactivexlxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T545

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

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
