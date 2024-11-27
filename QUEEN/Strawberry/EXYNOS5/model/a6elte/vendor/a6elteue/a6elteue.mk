$(call inherit-product, device/samsung/a6elteue/device.mk)
include vendor/samsung/configs/a6elte_common/a6elte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately 
include vendor/samsung/configs/a6elteue/gms_a6elteue.mk
endif

PRODUCT_NAME := a6elteue
PRODUCT_DEVICE := a6elteue
PRODUCT_MODEL := SM-A600U

include vendor/samsung/build/localelist/SecLocale_USA.mk




# SPR  OMADM Chameleon  and 3rd Party Preload Apps ONLY
## OMADM  command  Lib 

PRODUCT_PACKAGES += \
    CallProtect_ATT \
    SecVVM_v3

# Add Helo app tracking file changes
PRODUCT_PACKAGES += \
    pre_install.appsflyer
	
#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter
	
# Add Amazon Shopping
PRODUCT_PACKAGES += \
    Amazon_Shopping
	
# Add Amazon Prime Video
PRODUCT_PACKAGES += \
    AmazonVideo_SWA

# Add Dailyhunt
PRODUCT_PACKAGES += \
    Dailyhunt
	
# Add Booking tracking file
PRODUCT_PACKAGES += \
    .booking.data.aid

# Add MemorySaver_O_Refresh
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh	
	
# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock	

# Add Samsung+ as a stub app
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_STUB

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# Add Crane
PRODUCT_PACKAGES += \
  Crane
  
# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0	

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    -NSDSWebApp \
    -vsimservice
endif

# Add Emotify
PRODUCT_PACKAGES += \
    MyEmoji

# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Remove Microsoft office Stub
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub
