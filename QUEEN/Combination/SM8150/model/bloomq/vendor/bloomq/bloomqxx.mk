QCOM_PRODUCT_DEVICE := bloomq

$(call inherit-product, device/samsung/bloomq/device.mk)
include vendor/samsung/configs/bloomq_common/bloomq_common.mk

#include vendor/samsung/configs/bloomq/gms_bloomqxx.mk

PRODUCT_NAME := bloomqxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-F700F

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

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################	
# FactoryBinary only (end of line)
# PLEASE DO NOT ADD LINE BELOW
