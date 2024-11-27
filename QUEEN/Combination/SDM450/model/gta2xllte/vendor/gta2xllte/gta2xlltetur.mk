QCOM_PRODUCT_DEVICE := gta2xllte

$(call inherit-product, device/samsung/gta2xllte/device.mk)
include vendor/samsung/configs/gta2xllte_common/gta2xllte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta2xllte/gms_gta2xlltetur.mk
endif

PRODUCT_NAME := gta2xlltetur
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T597

# Add Samsung TTS
PRODUCT_PACKAGES += smt_tr_TR_f00

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
