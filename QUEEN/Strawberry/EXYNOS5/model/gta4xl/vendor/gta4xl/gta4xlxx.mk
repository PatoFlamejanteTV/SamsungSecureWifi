$(call inherit-product, device/samsung/gta4xl/device.mk)
include vendor/samsung/configs/gta4xl_common/gta4xl_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta4xl/gms_gta4xlxx.mk
endif

PRODUCT_NAME := gta4xlxx
PRODUCT_DEVICE := gta4xl
PRODUCT_MODEL := SM-P615

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00

# Ramen common
include vendor/samsung/hardware/gnss/slsi/ramen/common_evolution_64.mk
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
endif
    
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################	

ifneq ($(SEC_FACTORY_BUILD),true)
ifneq ($(filter eur_open, $(PROJECT_REGION)),)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    UnifiedWFC
endif
endif
