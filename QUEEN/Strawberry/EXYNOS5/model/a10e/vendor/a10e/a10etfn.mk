$(call inherit-product, device/samsung/a10e/device.mk)
include vendor/samsung/configs/a10e_common/a10e_common.mk


# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a10e/gms_a10etfn.mk
endif

PRODUCT_NAME := a10etfn
PRODUCT_DEVICE := a10e
PRODUCT_MODEL := SM-S102DL

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

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

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

# Lassen common
include vendor/samsung/hardware/gnss/slsi/lassen/common_evolution_us.mk

# GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.na.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.na.cdma.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps_CDMA.cfg
endif