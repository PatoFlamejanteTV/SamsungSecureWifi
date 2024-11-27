QCOM_PRODUCT_DEVICE := gts5l

$(call inherit-product, device/samsung/gts5l/device.mk)
include vendor/samsung/configs/gts5l_common/gts5l_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts5l/gms_gts5lzc.mk
endif


PRODUCT_NAME := gts5lzc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T865C

include vendor/samsung/build/localelist/SecLocale_CHN.mk

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Samsung Members for CHN models
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SamsungMembers_CHN_P_Removable

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
	
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify	
	
# Remove SmartCallProvider
PRODUCT_PACKAGES += \
    -SmartCallProvider \
    -HiyaService
	
# Add Spam Call
PRODUCT_PACKAGES += \
    BstSpamCallService
	
# Add Tencent Phone Number Locator
PRODUCT_PACKAGES += \
    PhoneNumberLocatorService 

# Setup SmartManager chn 
PRODUCT_PACKAGES += \
    -SmartManager_v5 \
    SmartManager_v5_CN \
    -SmartManager_v6_DeviceSecurity \
    SmartManager_v6_DeviceSecurity_CN

# Delete Google Search
PRODUCT_PACKAGES += \
    -QuickSearchBox

# add SGames - TwoDotsMini
PRODUCT_PACKAGES += \
    -TwoDotsMini
	
# Add Screen Recorder
PRODUCT_PACKAGES += \
    ScreenRecorder

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_CHN.mk
endif
###############################################################
