QCOM_PRODUCT_DEVICE := gtasliteltetmo

$(call inherit-product, device/samsung/gtasliteltetmo/device.mk)
include vendor/samsung/configs/gtaslitelte_common/gtaslitelte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtasliteltetmo/gms_gtasliteltetmo.mk
endif

PRODUCT_NAME := gtasliteltetmo
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T387T

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
	
# Crane
PRODUCT_PACKAGES += \
    Crane

# Add AppLock
PRODUCT_PACKAGES += \
    AppLock
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O
	
ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Remove upday
PRODUCT_PACKAGES += \
    -Upday
    
# TMO 3rd party Carrier Apps
PRODUCT_PACKAGES += \
    AccessTmobile_TMO

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast