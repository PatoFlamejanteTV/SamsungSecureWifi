QCOM_PRODUCT_DEVICE := gtactivexlwifi

$(call inherit-product, device/samsung/gtactivexlwifi/device.mk)
include vendor/samsung/configs/gtactivexlwifi_common/gtactivexlwifi_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtactivexlwifi/gms_gtactivexlwifixx.mk
endif

PRODUCT_NAME := gtactivexlwifixx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T540

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
# Add CSC feature for GPS
PRODUCT_COPY_FILES += \
    vendor/samsung/configs/gtactivexlwifi/others.xml:system/csc/others.xml
endif
###############################################################

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    -NSDSWebApp
    
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
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
