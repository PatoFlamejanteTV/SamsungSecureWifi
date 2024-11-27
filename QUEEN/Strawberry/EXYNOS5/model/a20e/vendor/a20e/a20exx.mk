$(call inherit-product, device/samsung/a20e/device.mk)
include vendor/samsung/configs/a20e_common/a20e_common.mk

include build/target/product/product_launched_with_p.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a20e/gms_a20exx.mk
endif

PRODUCT_NAME := a20exx
PRODUCT_DEVICE := a20e
PRODUCT_MODEL := SM-A202F

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio
	
#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter
	
# Add Helo app tracking file changes
PRODUCT_PACKAGES += \
    pre_install.appsflyer
	
# Add Booking tracking file
PRODUCT_PACKAGES += \
    .booking.data.aid
	
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

# Crane
PRODUCT_PACKAGES += \
    Crane	

# Add Secure Wi-Fi 
PRODUCT_PACKAGES += \ 
    Fast

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif