QCOM_PRODUCT_DEVICE := gts4lwifi

$(call inherit-product, device/samsung/gts4lwifi/device.mk)
include vendor/samsung/configs/gts4lwifi_common/gts4lwifi_common.mk

include vendor/samsung/configs/gts4lwifi/gms_gts4lwifixx.mk

PRODUCT_NAME := gts4lwifixx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T830

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00

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

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService
