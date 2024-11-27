QCOM_PRODUCT_DEVICE := a6plte

$(call inherit-product, device/samsung/a6plte/device.mk)
include vendor/samsung/configs/a6plte_common/a6plte_common.mk

#include vendor/samsung/configs/a6plte/gms_a6pltexx.mk

PRODUCT_NAME := a6pltexx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A605FN

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
