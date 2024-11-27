$(call inherit-product, device/samsung/a6eltetmo/device.mk)
include vendor/samsung/configs/a6elte_common/a6elte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately 
include vendor/samsung/configs/a6eltetmo/gms_a6eltetmo.mk
endif

PRODUCT_NAME := a6eltetmo
PRODUCT_DEVICE := a6eltetmo
PRODUCT_MODEL := SM-A600T

include vendor/samsung/build/localelist/SecLocale_USA.mk



# SPR  OMADM Chameleon  and 3rd Party Preload Apps ONLY
## OMADM  command  Lib 


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00


# TMO 3rd party MNO Team 2 VUX/HUX apps
PRODUCT_PACKAGES += \
	AmazonMDIP_USA

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    -NSDSWebApp \
    -vsimservice
endif

# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Samsung+ as a stub app
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_STUB

# TMO 3rd party MNO Team 2 VUX/HUX apps
PRODUCT_PACKAGES += \
     AccessTmobile_TMO \
     AdaptClient \
     NameIDVPL_TMO

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0	
   
# Remove Microsoft office Stub
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub

# Add Hancom Viewer for Smartphone
PRODUCT_PACKAGES += \
    HancomOfficeViewer
    
# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity