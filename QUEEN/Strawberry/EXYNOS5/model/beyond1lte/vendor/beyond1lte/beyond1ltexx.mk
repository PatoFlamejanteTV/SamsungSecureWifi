$(call inherit-product, device/samsung/beyond1lte/device.mk)
include vendor/samsung/configs/beyond1lte_common/beyond1lte_common.mk

#include vendor/samsung/configs/beyond1lte/gms_beyond1ltexx.mk

PRODUCT_NAME := beyond1ltexx
PRODUCT_DEVICE := beyond1lte
PRODUCT_MODEL := SM-G973F

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Crane
PRODUCT_PACKAGES += \
    Crane

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast