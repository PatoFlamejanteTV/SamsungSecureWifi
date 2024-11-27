$(call inherit-product, device/samsung/gta3xlwifi/device.mk)
include vendor/samsung/configs/gta3xlwifi_common/gta3xlwifi_common.mk

include build/target/product/product_launched_with_p.mk


# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta3xlwifi/gms_gta3xlwifixx.mk
endif

PRODUCT_NAME := gta3xlwifixx
PRODUCT_DEVICE := gta3xlwifi
PRODUCT_MODEL := SM-T510


# Add Samsung TTS
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
