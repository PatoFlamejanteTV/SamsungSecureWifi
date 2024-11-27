QCOM_PRODUCT_DEVICE := a20s

$(call inherit-product, device/samsung/a20s/device.mk)
include vendor/samsung/configs/a20s_common/a20s_common.mk

include vendor/samsung/configs/a20s/gms_a20sxx.mk

PRODUCT_NAME := a20sxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A207F

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Crane
PRODUCT_PACKAGES += \
    Crane

PRODUCT_PACKAGES += \
    extSdUnionStorage \
    extSDCardServiceVold \
    libIss_jni \
    libIss_mw \
    libIss_utils.so \
    libunionfs.so \
    libIss_Operations.so
