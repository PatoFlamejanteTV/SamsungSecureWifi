QCOM_PRODUCT_DEVICE := gta2xlwifi

$(call inherit-product, device/samsung/gta2xlwifi/device.mk)
include vendor/samsung/configs/gta2xlwifi_common/gta2xlwifi_common.mk

# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta2xlwifi/gms_gta2xlwifixx.mk
endif

PRODUCT_NAME := gta2xlwifixx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T590

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/gta2xlwifi_common/init.gta2xlwifi.rc:vendor/etc/init/hw/init.carrier.rc

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
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    RcsSettings

# Add Airtel Stub Solution
PRODUCT_PACKAGES += \
    AirtelStub
# Add AppLock
PRODUCT_PACKAGES += \
    AppLock

# Add Skype
PRODUCT_PACKAGES += \
    MSSkype_stub

# For MSP APP of NFC One binary
MSPAPP_ADDITIONAL_DAT_LIST := SM-A605F

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
