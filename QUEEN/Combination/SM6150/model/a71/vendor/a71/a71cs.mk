QCOM_PRODUCT_DEVICE := a71

$(call inherit-product, device/samsung/a71/device.mk)
include vendor/samsung/configs/a71_common/a71_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a71/gms_a71cs.mk
endif

PRODUCT_NAME := a71cs
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A715W

include vendor/samsung/build/localelist/SecLocale_CAN.mk

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
	
# Crane
PRODUCT_PACKAGES += \
    Crane
	
#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter
	
# Add Amazon Shopping
PRODUCT_PACKAGES += \
    Amazon_Shopping

# Add Helo
PRODUCT_PACKAGES += \
    Helo

# Add Snapchat
PRODUCT_PACKAGES += \
    Snapchat
	
# Add Booking.com 
PRODUCT_PACKAGES += \
    Booking_SWA
	
# Add Dailyhunt
PRODUCT_PACKAGES += \
    Dailyhunt
	
# Add Amazon Prime Video
PRODUCT_PACKAGES += \
    AmazonVideo_SWA

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit
	
# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
# TTS to Latin Region
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Add Ignite App Package
PRODUCT_PACKAGES += \
    IGNITE

# Add Aura App Package
PRODUCT_PACKAGES += \
    AURA
	
# Add Glispa App Package
PRODUCT_PACKAGES += \
    GLISPA
	
	
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

MSPAPP_ADDITIONAL_DAT_LIST := SM-A715F SM-A715FN SM-A715FM

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
