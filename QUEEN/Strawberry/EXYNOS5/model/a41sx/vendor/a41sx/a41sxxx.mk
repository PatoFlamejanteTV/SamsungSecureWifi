$(call inherit-product, device/samsung/a41sx/device.mk)
include vendor/samsung/configs/a41sx_common/a41sx_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a41sx/gms_a41sxxx.mk
endif

PRODUCT_NAME := a41sxxx
PRODUCT_DEVICE := a41sx
PRODUCT_MODEL := SM-A416F


ifneq ($(SEC_FACTORY_BUILD),true)
# Add ONE store Service
PRODUCT_PACKAGES += \
    OneStoreService

# Copy DAT file for ONE-store
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/SKT/SKTOneStore/uafield/uafield_x1qksx.dat:system/skt/ua/uafield.dat	
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# Remove HiyaService for Smart Call 
PRODUCT_PACKAGES += \
    -HiyaService \

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    Facebook_stub_Removable

 # Remove LinkedIn Stub
PRODUCT_PACKAGES += \
    -LinkedIn_SamsungStub_Deletable

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
    
# HiddenNetworkSetting 
PRODUCT_PACKAGES += \
    HiddenNetworkSetting

# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Keystring
PRODUCT_PACKAGES += \
    k09900

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
