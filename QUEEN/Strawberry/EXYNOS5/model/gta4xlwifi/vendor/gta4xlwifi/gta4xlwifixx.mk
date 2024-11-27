$(call inherit-product, device/samsung/gta4xlwifi/device.mk)
include vendor/samsung/configs/gta4xlwifi_common/gta4xlwifi_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta4xlwifi/gms_gta4xlwifixx.mk
endif

PRODUCT_NAME := gta4xlwifixx
PRODUCT_DEVICE := gta4xlwifi
PRODUCT_MODEL := SM-P610

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


# ++ LTN MobileTV
PRODUCT_PACKAGES += \
    MobileTV_LTN
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_P

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
    
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################	
