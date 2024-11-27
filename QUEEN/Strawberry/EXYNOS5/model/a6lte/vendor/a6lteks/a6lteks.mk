$(call inherit-product, device/samsung/a6lteks/device.mk)
include vendor/samsung/configs/a6lte_common/a6lte_common.mk

include build/target/product/product_launched_with_o.mk

include vendor/samsung/configs/a6lteks/gms_a6lteks.mk

PRODUCT_NAME := a6lteks
PRODUCT_DEVICE := a6lteks
PRODUCT_MODEL := SM-A600N

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

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# Add ONE store Service
PRODUCT_PACKAGES += \
    OneStoreService

# Copy DAT file for ONE-store
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/SKT/SKTOneStore/uafield/uafield_a6lteks.dat:system/skt/ua/uafield.dat

# IMS START
PRODUCT_PACKAGES += \
    imsmanager

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

ifeq ($(findstring ldu, $(TARGET_PRODUCT)),)
PRODUCT_PACKAGES += \
    imsservice \
    ImsTelephonyService \
    vsimmanager \
    vsimservice \
    NSDSWebApp \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    SimMobilityKit

ifneq ($(SEC_FACTORY_BUILD),true)
# IMS Telephony Feature
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.telephony.ims.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.telephony.ims.xml
endif
endif

include vendor/samsung/configs/ims/ims_common.mk
# IMS END
