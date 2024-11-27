$(call inherit-product, device/samsung/d1x/device.mk)
include vendor/samsung/configs/d1x_common/d1x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/d1x/gms_d1xks.mk
endif

PRODUCT_NAME := d1xks
PRODUCT_DEVICE := d1x
PRODUCT_MODEL := SM-N971N

# define the default locales for phone device
include vendor/samsung/build/localelist/SecLocale_KOR.mk


ifneq ($(SEC_FACTORY_BUILD),true)
ifneq ($(filter SKC KTC LUC KOO, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# SKC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SKC)
include vendor/samsung/configs/d1x/d1xksskt_apps.mk
endif
# KTC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),KTC)
include vendor/samsung/configs/d1x/d1xksktt_apps.mk
endif
# LUC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),LUC)
include vendor/samsung/configs/d1x/d1xkslgt_apps.mk
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
include vendor/samsung/configs/d1x/d1xksskt_apps.mk
include vendor/samsung/configs/d1x/d1xksktt_apps.mk
include vendor/samsung/configs/d1x/d1xkslgt_apps.mk

# Add OPENMarketHiddenMenu
PRODUCT_PACKAGES += \
    OPENMarketHiddenMenu
endif
endif

# Add ONE store Service
PRODUCT_PACKAGES += \
    OneStoreService

# Copy SO file for ONE store Service
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/Common/OneStoreService/libARMClientService.so:system/lib/libARMClientService.so \
    applications/provisional/KOR/Common/OneStoreService/libARMPlatform.so:system/lib/libARMPlatform.so \
    applications/provisional/KOR/Common/OneStoreService/libARMService.so:system/lib/libARMService.so \
    applications/provisional/KOR/Common/OneStoreService/libCheshireCat.so:system/lib/libCheshireCat.so \
    applications/provisional/KOR/Common/OneStoreService/libSystem.so:system/lib/libSystem.so \
    applications/provisional/KOR/Common/OneStoreService/libARMClientService_arm64-v8a.so:system/lib/libARMClientService_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libARMPlatform_arm64-v8a.so:system/lib/libARMPlatform_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libARMService_arm64-v8a.so:system/lib/libARMService_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libCheshireCat_arm64-v8a.so:system/lib/libCheshireCat_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libSystem_arm64-v8a.so:system/lib/libSystem_arm64-v8a.so

# Copy DAT file for ONE-store
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/SKT/SKTOneStore/uafield/uafield_beyondxlteks.dat:system/skt/ua/uafield.dat

# Add DMB
PRODUCT_PACKAGES += \
    Tdmb

# Add DMB HW Service
PRODUCT_PACKAGES += \
    vendor.samsung.hardware.tdmb@1.0-service

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# Remove HiyaService for Smart Call 
PRODUCT_PACKAGES += \
    -HiyaService \

# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    Facebook_stub_Removable

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
    
# HiddenNetworkSetting 
PRODUCT_PACKAGES += \
    HiddenNetworkSetting

# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add BlockchainBasicKit
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Add BlockchainBasicKit
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_KOR.mk
endif
###############################################################

