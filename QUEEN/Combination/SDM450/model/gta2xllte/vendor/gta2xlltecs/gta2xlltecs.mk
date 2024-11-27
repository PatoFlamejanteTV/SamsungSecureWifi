QCOM_PRODUCT_DEVICE := gta2xlltecs

$(call inherit-product, device/samsung/gta2xlltecs/device.mk)
include vendor/samsung/configs/gta2xllte_common/gta2xllte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta2xlltecs/gms_gta2xlltecs.mk
endif

PRODUCT_NAME := gta2xlltecs
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T597W

include vendor/samsung/build/localelist/SecLocale_CAN.mk


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_fr_CA_f00

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    FBInstaller_NS

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif
