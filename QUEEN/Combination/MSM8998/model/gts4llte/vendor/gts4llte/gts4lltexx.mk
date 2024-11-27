QCOM_PRODUCT_DEVICE := gts4llte

$(call inherit-product, device/samsung/gts4llte/device.mk)
include vendor/samsung/configs/gts4llte_common/gts4llte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts4llte/gms_gts4lltexx.mk
endif

PRODUCT_NAME := gts4lltexx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T835

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Setup SBrowser removable
PRODUCT_PACKAGES += \
    -SBrowser_11.1 \
    SBrowser_11.1_Removable  

# Add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers

ifneq ($(SEC_FACTORY_BUILD),true)
ifneq ($(filter sea_open, $(PROJECT_REGION)),)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    UnifiedWFC
endif
endif

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService