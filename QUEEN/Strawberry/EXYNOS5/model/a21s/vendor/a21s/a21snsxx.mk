$(call inherit-product, device/samsung/a21s/device.mk)
include vendor/samsung/configs/a21s_common/a21s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a21s/gms_a21snsxx.mk
endif

PRODUCT_NAME := a21snsxx
PRODUCT_DEVICE := a21s
PRODUCT_MODEL := SM-A217F

# Change default language on Factory binary
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_LOCALES := en_US $(filter-out en_US,$(PRODUCT_LOCALES))
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Add Ignite App Package
PRODUCT_PACKAGES += \
    IGNITE

# Add GLISPA App Package
PRODUCT_PACKAGES += \
    GLISPA	

# Add AURA App Package
PRODUCT_PACKAGES += \
    AURA

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

#Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Copy SetupIndiaServicesTnC Whitelist & Add SetupIndiaServicesTnC Packages
PRODUCT_COPY_FILES += \
    applications/par/idp/SetupIndiaServicesTnC/setupindiaservicestnc.json:/system/etc/setupindiaservicestnc.json    
PRODUCT_PACKAGES += \
    SetupIndiaServicesTnC
	
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
####################DO NOT UPDATE BELOW########################

#Add LogWriter Solution
PRODUCT_PACKAGES += \
	LogWriter

#Add Amazon Shopping Solution
PRODUCT_PACKAGES += \
	Amazon_Shopping