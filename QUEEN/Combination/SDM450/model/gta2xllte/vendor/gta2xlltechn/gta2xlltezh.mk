QCOM_PRODUCT_DEVICE := gta2xlltechn

$(call inherit-product, device/samsung/gta2xlltechn/device.mk)
include vendor/samsung/configs/gta2xllte_common/gta2xllte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta2xlltechn/gms_gta2xlltezh.mk
endif

PRODUCT_NAME := gta2xlltezh
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T595C


# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00
PRODUCT_PACKAGES += smt_en_GB_f00

# Setup SBrowser removable
PRODUCT_PACKAGES += \
    -SBrowser_11.1 \
    SBrowser_11.1_Removable
	
# Crane
PRODUCT_PACKAGES += \
    Crane

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif
