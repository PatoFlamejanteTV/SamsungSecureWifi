QCOM_PRODUCT_DEVICE := gts6l

$(call inherit-product, device/samsung/gts6l/device.mk)
include vendor/samsung/configs/gts6l_common/gts6l_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts6l/gms_gts6lsq.mk
endif


PRODUCT_NAME := gts6lsq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T867U

include vendor/samsung/build/localelist/SecLocale_USA.mk


# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.$(PRODUCT_DEVICE).rc \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_NAME).rc:vendor/etc/init/hw/init.carrier.rc

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Remove WebManual
PRODUCT_PACKAGES += \
   -WebManual

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_10.0_Removable \
    SBrowser_10.0

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast	

# Sprint Preload & 3rdParty Apps Apps
include vendor/samsung/configs/gts6l/gts6lsq_vzwspr_apps.mk
include vendor/samsung/configs/gts6l/gts6lsqspr_apps.mk
include vendor/samsung/configs/gts6l/gts6lsqtmo_apps.mk

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
