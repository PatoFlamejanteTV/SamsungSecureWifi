$(call inherit-product, device/samsung/a6lte/device.mk)
include vendor/samsung/configs/a6lte_common/a6lte_common.mk

include vendor/samsung/configs/a6lte/gms_a6ltexx.mk

PRODUCT_NAME := a6ltexx
PRODUCT_DEVICE := a6lte
PRODUCT_MODEL := SM-A600FN

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P
    
# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Crane
PRODUCT_PACKAGES += \
    Crane

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
