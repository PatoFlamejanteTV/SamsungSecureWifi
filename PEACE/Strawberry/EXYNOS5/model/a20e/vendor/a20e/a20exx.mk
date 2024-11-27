$(call inherit-product, device/samsung/a20e/device.mk)
include vendor/samsung/configs/a20e_common/a20e_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a20e/gms_a20exx.mk
endif

# define google api level for google approval
include build/target/product/product_launched_with_p.mk

PRODUCT_NAME := a20exx
PRODUCT_DEVICE := a20e
PRODUCT_MODEL := SM-A202F



# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings

# add Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS
endif

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast