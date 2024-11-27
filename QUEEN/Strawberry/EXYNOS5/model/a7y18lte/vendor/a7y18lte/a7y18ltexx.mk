$(call inherit-product, device/samsung/a7y18lte/device.mk)
include vendor/samsung/configs/a7y18lte_common/a7y18lte_common.mk

include build/target/product/product_launched_with_o.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a7y18lte/gms_a7y18ltexx.mk
endif

PRODUCT_NAME := a7y18ltexx
PRODUCT_DEVICE := a7y18lte
PRODUCT_MODEL := SM-A750FN

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio
	
# Add Helo app tracking file changes
PRODUCT_PACKAGES += \
    pre_install.appsflyer
	
# Add Booking tracking file
PRODUCT_PACKAGES += \
    .booking.data.aid
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O
	
# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Crane
PRODUCT_PACKAGES += \
    Crane	

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif