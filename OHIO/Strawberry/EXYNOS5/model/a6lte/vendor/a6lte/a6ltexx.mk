$(call inherit-product, device/samsung/a6lte/device.mk)
include vendor/samsung/configs/a6lte_common/a6lte_common.mk

include build/target/product/product_launched_with_o.mk

# define the default locales for phone device

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a6lte/gms_a6ltexx.mk
endif


PRODUCT_NAME := a6ltexx
PRODUCT_DEVICE := a6lte
PRODUCT_MODEL := SM-A600FN

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

ifneq ($(SEC_FACTORY_BUILD),true)
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
endif

#Add SamsungMembers 
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Crane
PRODUCT_PACKAGES += \
    Crane
# Add Airtel Stub Solution
PRODUCT_PACKAGES += \
    AirtelStub
# Add AppLock
PRODUCT_PACKAGES += \
    AppLock

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Memory Solutions
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

########################################################################################
