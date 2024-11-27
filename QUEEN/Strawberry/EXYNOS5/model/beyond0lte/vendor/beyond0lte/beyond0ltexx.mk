$(call inherit-product, device/samsung/beyond0lte/device.mk)
include vendor/samsung/configs/beyond0lte_common/beyond0lte_common.mk

#include vendor/samsung/configs/beyond0lte/gms_beyond0ltexx.mk

PRODUCT_NAME := beyond0ltexx
PRODUCT_DEVICE := beyond0lte
PRODUCT_MODEL := SM-G970F

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