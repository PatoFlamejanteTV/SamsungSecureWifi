QCOM_PRODUCT_DEVICE := greatqltesq

$(call inherit-product, device/samsung/greatqltesq/device.mk)
include vendor/samsung/configs/greatqlte_common/greatqlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/greatqltesq/gms_greatqltesq.mk
endif


PRODUCT_NAME := greatqltesq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N950U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# for samsung hardware init
PRODUCT_COPY_FILES += \
    device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    -ImsTelephonyService \
    -imsservice \
    -vsimmanager \
    -vsimservice \
    -secimshttpclient \
    -ImsLogger \
    -ImsSettings \
    -imscoremanager \
    -imsd \
    -svemanager \
    -sveservice \
    -rcsopenapi \
    -RcsSettings
    
# Add WhatsAppDownloader
PRODUCT_PACKAGES += \
    -WhatsAppDownloader

# Remove VoiceNote
#PRODUCT_PACKAGES += \
#    -VoiceNote_5.0_Task

# Add nextradio wrapper package
#PRODUCT_PACKAGES += \
#    nextradio 

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Samsung Plus Removable
#PRODUCT_PACKAGES += \
#    SAMSUNG_PLUS_REMOVABLE

# Add Samsung Pay App
#PRODUCT_PACKAGES += \
#    -SamsungPayStub \
#    SamsungPayApp

# Hotspot TMO,MTR,USC
#PRODUCT_PACKAGES += \
#    MHSWrapperUSC

# Add S Voice IME
#PRODUCT_PACKAGES += \
#    SVoiceIME

# Add CNN panel
#PRODUCT_PACKAGES += \
#    CnnPanel

# Add YELP panel
#PRODUCT_PACKAGES += \
#    YelpPanel

# Add USA SetupWizard
#PRODUCT_PACKAGES += \
#    SetupWizard_USA

# Add Device Protection Manager
# PRODUCT_PACKAGES += \
#    ASKSManager

# add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable
		
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# <<<<<<<<<<<<<<<<<<<<<<< DO NOT ADD BELOW >>>>>>>>>>>>>>>>>>>>
# Please do not add apk below. if you want to add apk, please add apk above here.
ifeq ($(SEC_FACTORY_BUILD),true)
# FactoryBinary doesn't need CHN vendor packages.
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
# <<<<<<<<<<<<<<<<<<<<<<< DO NOT ADD ABOVE >>>>>>>>>>>>>>>>>>>>
