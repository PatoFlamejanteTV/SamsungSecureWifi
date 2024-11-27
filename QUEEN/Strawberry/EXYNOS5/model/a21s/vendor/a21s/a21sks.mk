$(call inherit-product, device/samsung/a21s/device.mk)
include vendor/samsung/configs/a21s_common/a21s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a21s/gms_a21sks.mk
endif

PRODUCT_NAME := a21sks
PRODUCT_DEVICE := a21s
PRODUCT_MODEL := SM-A217N

include vendor/samsung/build/localelist/SecLocale_KOR.mk

ifneq ($(filter SKC KTC LUC KOO, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
# SKC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),SKC)
include vendor/samsung/configs/a21s/a21sksskt_apps.mk
endif
# KTC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),KTC)
include vendor/samsung/configs/a21s/a21sksktt_apps.mk
endif
# LUC Preload Apps ONLY
ifeq ($(SEC_BUILD_OPTION_SINGLESKU_CUST),LUC)
include vendor/samsung/configs/a21s/a21skslgt_apps.mk
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
include vendor/samsung/configs/a21s/a21sksskt_apps.mk
include vendor/samsung/configs/a21s/a21sksktt_apps.mk
include vendor/samsung/configs/a21s/a21skslgt_apps.mk

# Add OPENMarketHiddenMenu
PRODUCT_PACKAGES += \
    OPENMarketHiddenMenu
endif


# Change default language on Factory binary
ifeq ($(SEC_FACTORY_BUILD),true)
endif

# Add ONE store Service
PRODUCT_PACKAGES += \
    OneStoreService
	
# Copy DAT file for ONE-store
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/SKT/SKTOneStore/uafield/uafield_a30ks.dat:system/skt/ua/uafield.dat	

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Facebook apps removable
PRODUCT_PACKAGES += \
    -Facebook_stub \
    Facebook_stub_Removable

# Removing LinkedIn Stub
PRODUCT_PACKAGES += \
    -LinkedIn_SamsungStub_Deletable


# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload
    
# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Remove upday
PRODUCT_PACKAGES += \
    -Upday
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01
	
# Remove HiyaService
PRODUCT_PACKAGES += \
  -HiyaService

# Remove Spotify
PRODUCT_PACKAGES += \
  -Spotify

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

#DEX_IN_DATA
PRODUCT_DEX_PREOPT_PACKAGES_CORE_APP_IN_DATA += \
	imsservice

# Add Subscription Calendar
PRODUCT_PACKAGES += \
    OpenCalendar

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01


###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
