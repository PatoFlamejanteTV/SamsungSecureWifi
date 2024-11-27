$(call inherit-product, device/samsung/a10/device.mk)
include vendor/samsung/configs/a10_common/a10_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a10/gms_a10xx.mk
endif

PRODUCT_NAME := a10xx
PRODUCT_DEVICE := a10
PRODUCT_MODEL := SM-A105FN


# Change default language on Factory binary
ifeq ($(SEC_FACTORY_BUILD),true)
endif

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P
	
# Crane
PRODUCT_PACKAGES += \
    Crane

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add Samsung TTS
PRODUCT_PACKAGES += smt_hi_IN_f00
PRODUCT_PACKAGES += smt_en_IN_f00

# Add AirtelStub Solution
PRODUCT_PACKAGES += \
    AirtelStub	
	
# Add Samsung Pay Mini Stub
PRODUCT_PACKAGES += \
    SamsungPayStubMini

# Add Dailyhunt
PRODUCT_PACKAGES += \
    Dailyhunt

# Add Amazon Shopping
PRODUCT_PACKAGES += \
    Amazon_Shopping

#Removed Generic Samsung Pay Framework for SPay Mini India
PRODUCT_PACKAGES += \
    -PaymentFramework

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
