QCOM_PRODUCT_DEVICE := a6plte

$(call inherit-product, device/samsung/a6plte/device.mk)
include vendor/samsung/configs/a6plte_common/a6plte_common.mk

# define google api level for google approval
include build/target/product/product_launched_with_o.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a6plte/gms_a6pltexx.mk
endif

PRODUCT_NAME := a6pltexx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A605FN

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/a6plte_common/init.a6plte.rc:vendor/etc/init/hw/init.carrier.rc

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_O

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    imsmanager \
    ImsTelephonyService \
    imsservice \
    vsimmanager \
    vsimservice \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imscoremanager \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    RcsSettings
endif

# SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add Airtel Stub Solution
PRODUCT_PACKAGES += \
    AirtelStub
# Add AppLock
PRODUCT_PACKAGES += \
    AppLock
	
# Add Memory Solutions
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

# For MSP APP of NFC One binary
MSPAPP_ADDITIONAL_DAT_LIST := SM-A605F

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
