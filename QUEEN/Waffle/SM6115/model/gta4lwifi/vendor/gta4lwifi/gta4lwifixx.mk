QCOM_PRODUCT_DEVICE := gta4lwifi

$(call inherit-product, device/samsung/gta4lwifi/device.mk)
include vendor/samsung/configs/gta4lwifi_common/gta4lwifi_common.mk

include vendor/samsung/configs/gta4lwifi/gms_gta4lwifixx.mk

PRODUCT_NAME := gta4lwifixx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T500

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