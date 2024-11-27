$(call inherit-product, device/samsung/a40/device.mk)
include vendor/samsung/configs/a40_common/a40_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a40/gms_a40xx.mk
endif

# define google api level for google approval
include build/target/product/product_launched_with_p.mk

PRODUCT_NAME := a40xx
PRODUCT_DEVICE := a40
PRODUCT_MODEL := SM-A405FN

MSPAPP_ADDITIONAL_DAT_LIST := SM-A405FM

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# FactoryBinary doesn't need below packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
