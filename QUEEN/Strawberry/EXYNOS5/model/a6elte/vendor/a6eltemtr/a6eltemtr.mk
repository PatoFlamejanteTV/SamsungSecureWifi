$(call inherit-product, device/samsung/a6eltemtr/device.mk)
include vendor/samsung/configs/a6elte_common/a6elte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately 
include vendor/samsung/configs/a6eltemtr/gms_a6eltemtr.mk
endif

PRODUCT_NAME := a6eltemtr
PRODUCT_DEVICE := a6eltemtr
PRODUCT_MODEL := SM-A600T1

include vendor/samsung/build/localelist/SecLocale_USA.mk



# SPR  OMADM Chameleon  and 3rd Party Preload Apps ONLY
## OMADM  command  Lib 

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Crane
PRODUCT_PACKAGES += \
  Crane

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    -NSDSWebApp \
    -vsimservice
endif

# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual

# MNO Team II 3rd party VUX apps
PRODUCT_PACKAGES += \
    AdaptClient \
    MetroAppStore_MTR \
    Lookout_MTR \
    MetroZone_MTR \
    MyMetro_MTR \
    NameID_hvpl_MTR
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Samsung+ as a stub app
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_STUB

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0	
   
# MNO Team II 3rd party VUX apps
PRODUCT_PACKAGES += \
	Lookout_MTR

# Add Hancom Viewer for Smartphone
PRODUCT_PACKAGES += \
    HancomOfficeViewer
    
# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity