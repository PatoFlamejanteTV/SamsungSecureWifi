$(call inherit-product, device/samsung/a10s/device.mk)
include vendor/samsung/configs/a10s_common/a10s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a10s/gms_a10sxx.mk
endif

PRODUCT_NAME := a10sxx
PRODUCT_DEVICE := a10s
PRODUCT_MODEL := SM-A107F

# Add Samsung TTS
PRODUCT_PACKAGES += smt_vi_VN_f00

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add Airtel Stub Solution
PRODUCT_PACKAGES += \
    AirtelStub

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O


ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Add SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv3.3 \
    -SamsungIMEv3.3MASS

#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter

# Add Helo
PRODUCT_PACKAGES += \
    Helo

# Add Booking.com for India Market
PRODUCT_PACKAGES += \
    Booking_SWA
	
# Add Dailyhunt
PRODUCT_PACKAGES += \
    Dailyhunt
	
# Add Amazon Shopping
PRODUCT_PACKAGES += \
    Amazon_Shopping
	 