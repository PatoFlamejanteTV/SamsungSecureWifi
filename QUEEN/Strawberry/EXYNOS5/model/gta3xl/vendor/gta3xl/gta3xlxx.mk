$(call inherit-product, device/samsung/gta3xl/device.mk)
include vendor/samsung/configs/gta3xl_common/gta3xl_common.mk

include build/target/product/product_launched_with_p.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta3xl/gms_gta3xlxx.mk
endif

PRODUCT_NAME := gta3xlxx
PRODUCT_DEVICE := gta3xl
PRODUCT_MODEL := SM-T515

# Add Samsung TTS
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    UnifiedWFC
endif
