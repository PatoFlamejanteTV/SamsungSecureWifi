QCOM_PRODUCT_DEVICE := gta2xlltevzw

$(call inherit-product, device/samsung/gta2xlltevzw/device.mk)
include vendor/samsung/configs/gta2xllte_common/gta2xllte_common.mk

# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta2xlltevzw/gms_gta2xlltevzw.mk
endif

PRODUCT_NAME := gta2xlltevzw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T597V

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/gta2xllte_common/init.gta2xllte.rc:vendor/etc/init/hw/init.carrier.rc

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
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# For MSP APP of NFC One binary
MSPAPP_ADDITIONAL_DAT_LIST := SM-A605F

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
