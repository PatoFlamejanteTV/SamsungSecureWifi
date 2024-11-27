include vendor/samsung/configs/a31/a31xx.mk

PRODUCT_NAME := a31zh
PRODUCT_MODEL := SM-A315G

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += -smt_pl_PL_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Remove Secure Wi-Fi
PRODUCT_PACKAGES += \
    -Fast
