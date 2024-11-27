QCOM_PRODUCT_DEVICE := gtaslitelteusc

$(call inherit-product, device/samsung/gtaslitelteusc/device.mk)
include vendor/samsung/configs/gtaslitelte_common/gtaslitelte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtaslitelteusc/gms_gtaslitelteusc.mk
endif

PRODUCT_NAME := gtaslitelteusc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T387R4

include vendor/samsung/build/localelist/SecLocale_USA.mk


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Removing WebManual
PRODUCT_PACKAGES += \
    -WebManual
    
# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# USC Ignite intgration
PRODUCT_PACKAGES += \
    Ignite-uscc

# Remove upday
PRODUCT_PACKAGES += \
    -Upday

# Remove SamsungMessages
PRODUCT_PACKAGES += \
	-SamsungMessages_11

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
