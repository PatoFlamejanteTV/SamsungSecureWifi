QCOM_PRODUCT_DEVICE := gts6l

$(call inherit-product, device/samsung/gts6l/device.mk)
include vendor/samsung/configs/gts6l_common/gts6l_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts6l/gms_gts6lxx.mk
endif


PRODUCT_NAME := gts6lxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T865

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += smt_es_MX_f00

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify	

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

ifneq ($(SEC_FACTORY_BUILD),true)
ifneq ($(filter eur_open, $(PROJECT_REGION)),)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    UnifiedWFC
endif
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
