$(call inherit-product, device/samsung/j8lte/device.mk)
include vendor/samsung/configs/j8lte_common/j8lte_common.mk

include build/target/product/product_launched_with_o.mk

# define the default locales for phone device

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/j8lte/gms_j8ltexx.mk
endif


PRODUCT_NAME := j8ltexx
PRODUCT_DEVICE := j8lte
PRODUCT_MODEL := SM-J800FN

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_O

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    imsmanager \
    ImsTelephonyService \
    imsservice \
    vsimmanager \
    vsimservice \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imscoremanager \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    RcsSettings

########################################################################################
