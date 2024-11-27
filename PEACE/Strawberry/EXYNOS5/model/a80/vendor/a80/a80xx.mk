$(call inherit-product, device/samsung/a80/device.mk)
include vendor/samsung/configs/a80_common/a80_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a80/gms_a80xx.mk
endif

PRODUCT_NAME := a80xx
PRODUCT_DEVICE := a80
PRODUCT_MODEL := SM-A805F

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

#Removed Generic Samsung Pay Framework for SPay Mini India
PRODUCT_PACKAGES += \
    -PaymentFramework

# Add SamsungStubAppMini
PRODUCT_PACKAGES += \
    SamsungPayStubMini

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
    
# [CMC] Samsung CMC
  PRODUCT_PACKAGES += \
    MdecService
endif
	
# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# Add AirtelStub Solution
PRODUCT_PACKAGES += \
    AirtelStub
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

MSPAPP_ADDITIONAL_DAT_LIST := SM-A505FN SM-A505FM

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################	

# Ramen common
    include vendor/samsung/hardware/gnss/slsi/ramen/common_evolution.mk

# Ramen GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
endif
