$(call inherit-product, device/samsung/a10e/device.mk)
include vendor/samsung/configs/a10e_common/a10e_common.mk

include build/target/product/product_launched_with_p.mk


# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a10e/gms_a10eue.mk
endif

PRODUCT_NAME := a10eue
PRODUCT_DEVICE := a10e
PRODUCT_MODEL := SM-A102U1

include vendor/samsung/build/localelist/SecLocale_USA.mk



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

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
endif

# Remove upday
PRODUCT_PACKAGES += \
    -Upday
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01
	
# Remove HiyaService
PRODUCT_PACKAGES += \
  -HiyaService

#DEX_IN_DATA
PRODUCT_DEX_PREOPT_PACKAGES_CORE_APP_IN_DATA += \
	imsservice

# Add Subscription Calendar
PRODUCT_PACKAGES += \
    OpenCalendar