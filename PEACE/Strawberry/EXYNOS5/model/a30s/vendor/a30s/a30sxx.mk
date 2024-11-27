$(call inherit-product, device/samsung/a30s/device.mk)
include vendor/samsung/configs/a30s_common/a30s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a30s/gms_a30sxx.mk
endif

PRODUCT_NAME := a30sxx
PRODUCT_DEVICE := a30s
PRODUCT_MODEL := SM-A307FN

MSPAPP_ADDITIONAL_DAT_LIST := SM-A305FN

# Change default language on Factory binary
ifeq ($(SEC_FACTORY_BUILD),true)
endif

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P
	
# Crane
PRODUCT_PACKAGES += \
    Crane

# Add SamsungStubAppMini
PRODUCT_PACKAGES += \
    SamsungPayStubMini

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_P

#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter
	
# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add AirtelStub Solution
PRODUCT_PACKAGES += \
    AirtelStub	

# Add Samsung TTS
PRODUCT_PACKAGES += smt_hi_IN_f00
PRODUCT_PACKAGES += smt_en_IN_f00

# Add QRAgent
PRODUCT_PACKAGES += \
QRAgent
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast