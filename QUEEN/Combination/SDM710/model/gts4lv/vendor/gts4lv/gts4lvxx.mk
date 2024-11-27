QCOM_PRODUCT_DEVICE := gts4lv

$(call inherit-product, device/samsung/gts4lv/device.mk)
include vendor/samsung/configs/gts4lv_common/gts4lv_common.mk

# define google api level for google approval
include build/target/product/product_launched_with_p.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts4lv/gms_gts4lvxx.mk
endif

PRODUCT_NAME := gts4lvxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T725

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService
	
ifneq ($(SEC_FACTORY_BUILD),true)
ifneq ($(filter eur_open, $(PROJECT_REGION)),)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    UnifiedWFC
endif
endif

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
