QCOM_PRODUCT_DEVICE := gta4l

$(call inherit-product, device/samsung/gta4l/device.mk)
include vendor/samsung/configs/gta4l_common/gta4l_common.mk

include vendor/samsung/configs/gta4l/gms_gta4lxx.mk

PRODUCT_NAME := gta4lxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T505

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    UnifiedWFC
endif