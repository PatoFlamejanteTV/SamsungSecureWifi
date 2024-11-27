QCOM_PRODUCT_DEVICE := gtactivexlwifi

$(call inherit-product, device/samsung/gtactivexlwifi/device.mk)
include vendor/samsung/configs/gtactivexlwifi_common/gtactivexlwifi_common.mk

# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtactivexlwifi/gms_gtactivexlwifixx.mk
endif

PRODUCT_NAME := gtactivexlwifixx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T540

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
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
