$(call inherit-product, device/samsung/y2s/device.mk)
include vendor/samsung/configs/y2s_common/y2s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/y2s/gms_y2sksx.mk
endif

PRODUCT_NAME := y2sksx
PRODUCT_DEVICE := y2s
PRODUCT_MODEL := SM-G986N

include vendor/samsung/build/localelist/SecLocale_KOR.mk

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter SKC KTC LUC KOO, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_$(SEC_BUILD_OPTION_SINGLESKU_CUST).mk
else
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SKC.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_KTC.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_LUC.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_KOO.mk
endif

# Add ONE store Service
PRODUCT_PACKAGES += \
    OneStoreService

# Copy DAT file for ONE-store
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/SKT/SKTOneStore/uafield/uafield_y2sksx.dat:system/skt/ua/uafield.dat		
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
