QCOM_PRODUCT_DEVICE := gta2xlltekx

$(call inherit-product, device/samsung/gta2xlltekx/device.mk)
include vendor/samsung/configs/gta2xllte_common/gta2xllte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta2xlltekx/gms_gta2xlltekx.mk
endif

PRODUCT_NAME := gta2xlltekx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T595N

include vendor/samsung/build/localelist/SecLocale_KOR.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01

# Setup SBrowser removable
PRODUCT_PACKAGES += \
    -SBrowser_11.1 \
    SBrowser_11.1_Removable

# Crane
PRODUCT_PACKAGES += \
    Crane

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService
    
# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Add OPENMarketHiddenMenu
PRODUCT_PACKAGES += \
    OPENMarketHiddenMenu

# HiddenNetworkSetting 
PRODUCT_PACKAGES += \
    HiddenNetworkSetting