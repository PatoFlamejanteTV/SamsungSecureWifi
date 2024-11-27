QCOM_PRODUCT_DEVICE := r1q

$(call inherit-product, device/samsung/r1q/device.mk)
include vendor/samsung/configs/r1q_common/r1q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r1q/gms_r1qks.mk
endif

PRODUCT_NAME := r1qks
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A805N

include vendor/samsung/build/localelist/SecLocale_KOR.mk

ifneq ($(filter SKC KTC LUC KOO, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# SKC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SKC)
include vendor/samsung/configs/r1q/r1qksskt_apps.mk
endif
# KTC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),KTC)
include vendor/samsung/configs/r1q/r1qksktt_apps.mk
endif
# LUC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),LUC)
include vendor/samsung/configs/r1q/r1qkslgt_apps.mk
endif
# KOO Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),KOO)
PRODUCT_PACKAGES += \
    OPENMarketHiddenMenu

# Add Mobile Tmoney(Tcash) api
PRODUCT_PACKAGES += \
    SktUsimService

# KT USIM Extention
PRODUCT_PACKAGES += \
    libktuca2 \
    libuca-ril
endif
else
include vendor/samsung/configs/r1q/r1qksskt_apps.mk
include vendor/samsung/configs/r1q/r1qksktt_apps.mk
include vendor/samsung/configs/r1q/r1qkslgt_apps.mk

# Add OPENMarketHiddenMenu
PRODUCT_PACKAGES += \
    OPENMarketHiddenMenu
endif

# Add ONE store Service
PRODUCT_PACKAGES += \
    OneStoreService

# Add S Voice IME
PRODUCT_PACKAGES += \
    SVoiceIME



ifneq ($(SEC_FACTORY_BUILD),true)
# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock


# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

    
# Add OneDrive removable
PRODUCT_PACKAGES += \
-OneDrive_Samsung_v3 \
OneDrive_Samsung_v3_Removable


# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone


# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader


# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload


# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# HiddenNetworkSetting 
PRODUCT_PACKAGES += \
    HiddenNetworkSetting

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    Facebook_stub_Removable


###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)


include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
