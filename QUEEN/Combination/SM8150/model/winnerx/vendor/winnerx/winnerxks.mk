QCOM_PRODUCT_DEVICE := winnerx

$(call inherit-product, device/samsung/winnerx/device.mk)
include vendor/samsung/configs/winnerx_common/winnerx_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/winnerx/gms_winnerxks.mk
endif

PRODUCT_NAME := winnerxks
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-F907N

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
    applications/provisional/KOR/SKT/SKTOneStore/uafield/uafield_winnerxks.dat:system/skt/ua/uafield.dat

# for samsung hardware init
# PRODUCT_COPY_FILES += \
#    device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01

# Setup SBrowser removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0 \
    SBrowser_11.0_Removable

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Crane
PRODUCT_PACKAGES += \
    Crane

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc


# Remove HiyaService
PRODUCT_PACKAGES += \
    -HiyaService
	
# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
