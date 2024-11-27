QCOM_PRODUCT_DEVICE := m40

$(call inherit-product, device/samsung/m40/device.mk)
include vendor/samsung/configs/m40_common/m40_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/m40/gms_m40dd.mk
endif

PRODUCT_NAME := m40dd
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-M405F

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O	
	
# Add AirtelStub
PRODUCT_PACKAGES += \
    AirtelStub
	
# Add AppLock
PRODUCT_PACKAGES += \
    AppLock

# Add MemorySaver_O_Refresh
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_hi_IN_f00
PRODUCT_PACKAGES += smt_en_IN_f00
	
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

MSPAPP_ADDITIONAL_DAT_LIST := SM-A715F SM-A715FN SM-A715FM

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
