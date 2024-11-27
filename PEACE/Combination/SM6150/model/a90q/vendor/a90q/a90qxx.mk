QCOM_PRODUCT_DEVICE := a90q

$(call inherit-product, device/samsung/a90q/device.mk)
include vendor/samsung/configs/a90q_common/a90q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a90q/gms_a90qxx.mk
endif

PRODUCT_NAME := a90qxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A905F

# Add S Voice IME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Crane
PRODUCT_PACKAGES += \
  Crane

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
