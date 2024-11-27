QCOM_PRODUCT_DEVICE := j8plte

$(call inherit-product, device/samsung/j8plte/device.mk)
include vendor/samsung/configs/j8plte_common/j8plte_common.mk

# define google api level for google approval
include build/target/product/product_launched_with_o.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/j8plte/gms_j8pltexx.mk
endif

PRODUCT_NAME := j8pltexx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-J805FN

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_O

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    imsmanager \
    ImsTelephonyService \
    imsservice \
    vsimmanager \
    vsimservice \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imscoremanager \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    RcsSettings
endif

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
