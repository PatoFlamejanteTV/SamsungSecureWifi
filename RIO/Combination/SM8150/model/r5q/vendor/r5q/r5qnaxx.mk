QCOM_PRODUCT_DEVICE := r5q

$(call inherit-product, device/samsung/r5q/device.mk)
include vendor/samsung/configs/r5q_common/r5q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r5q/gms_r5qnaxx.mk
endif

#include vendor/samsung/configs/beyond2qltesq/gms_beyond2qltesq.mk

PRODUCT_NAME := r5qnaxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G770F

#FM Radio
PRODUCT_PACKAGES += \
    HybridRadio

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Add Crane
PRODUCT_PACKAGES += \
  Crane

# Add Samsung DigitalKey in "Settings - Biometrics and security"
# DigitalKey (Supported Country) : EUR, KOR, USA, CAN, SEA/Oceania
# -DigitalKey (Not supported countries) : CHN, JPN
PRODUCT_PACKAGES += \
    DigitalKey

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast