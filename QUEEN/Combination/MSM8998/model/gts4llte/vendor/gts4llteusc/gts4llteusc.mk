QCOM_PRODUCT_DEVICE := gts4llteusc

$(call inherit-product, device/samsung/gts4llteusc/device.mk)
include vendor/samsung/configs/gts4llte_common/gts4llte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts4llteusc/gms_gts4llteusc.mk
endif

PRODUCT_NAME := gts4llteusc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T837R4

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add MobileOffice Removabal Stub
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub \
    Excel_SamsungStub_Removable \
    PowerPoint_SamsungStub_Removable \
    Word_SamsungStub_Removable

# Removing WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Remove SmartCallProvider
PRODUCT_PACKAGES += \
	-SmartCallProvider \
	-HiyaService

# USC Ignite intgration
PRODUCT_PACKAGES += \
    Ignite-uscc

# Remove upday
PRODUCT_PACKAGES += \
    -Upday
