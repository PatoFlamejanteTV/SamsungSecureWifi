QCOM_PRODUCT_DEVICE := gtasliteltespr

$(call inherit-product, device/samsung/gtasliteltespr/device.mk)
include vendor/samsung/configs/gtaslitelte_common/gtaslitelte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtasliteltespr/gms_gtasliteltespr.mk
endif

PRODUCT_NAME := gtasliteltespr
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T387P

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
	
# Crane
PRODUCT_PACKAGES += \
    Crane

# Sprint Extension 
PRODUCT_PACKAGES += \
    SprintAndroidExtension2 

# LauncherFacade
PRODUCT_PACKAGES += \
    LauncherFacade
	
ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Removing WebManual
PRODUCT_PACKAGES += \
    -WebManual