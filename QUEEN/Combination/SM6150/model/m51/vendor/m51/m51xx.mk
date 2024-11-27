QCOM_PRODUCT_DEVICE := m51

$(call inherit-product, device/samsung/m51/device.mk)
include vendor/samsung/configs/m51_common/m51_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/m51/gms_m51xx.mk
endif

PRODUCT_NAME := m51xx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-M515F

# Crane
PRODUCT_PACKAGES += \
    Crane

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

# Add Discover Solution
PRODUCT_PACKAGES += \
    Discover
	
# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
	UltraDataSaving_O

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

# FactoryBinary doesn't need below packages.
ifneq ($(SEC_FACTORY_BUILD),true)

# Add Ignite App Package
PRODUCT_PACKAGES += \
    IGNITE

# Add Aura App Package
PRODUCT_PACKAGES += \
    AURA
	
# Add Glispa App Package
PRODUCT_PACKAGES += \
    GLISPA


#Add Prime Video Solution
PRODUCT_PACKAGES += \
	AmazonVideo_SWA

#Add Helo Solution
PRODUCT_PACKAGES += \
	Helo

#Add Amazon Shopping Solution
PRODUCT_PACKAGES += \
	Amazon_Shopping

# Add DisneyMagicKingdom
PRODUCT_PACKAGES += \
    DisneyMagicKingdom

# Add 99Taxis
PRODUCT_PACKAGES += \
    99Taxis
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

MSPAPP_ADDITIONAL_DAT_LIST := SM-A715F SM-A715FN SM-A715FM

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################

# Copy SetupIndiaServicesTnC Whitelist & Add SetupIndiaServicesTnC Packages
# FactoryBinary doesn't need below packages.
ifneq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    applications/par/idp/SetupIndiaServicesTnC/setupindiaservicestnc.json:/system/etc/setupindiaservicestnc.json    
PRODUCT_PACKAGES += \
    SetupIndiaServicesTnC
endif
