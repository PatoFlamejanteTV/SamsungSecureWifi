QCOM_PRODUCT_DEVICE := gts4lv

$(call inherit-product, device/samsung/gts4lv/device.mk)
include vendor/samsung/configs/gts4lv_common/gts4lv_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts4lv/gms_gts4lvvzw.mk
endif

PRODUCT_NAME := gts4lvusc
PRODUCT_MODEL := SM-T727R4
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)

include vendor/samsung/build/localelist/SecLocale_USA.mk

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_9.0_Removable \
    SBrowser_9.0
	
# USCC Preload
PRODUCT_PACKAGES += \
    Ignite-uscc
	
# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Remove WebManual
PRODUCT_PACKAGES += \
   -WebManual

#remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v10
  
# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify	

# [CMC] Samsung CMC
  PRODUCT_PACKAGES += \
    MdecService


###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
