$(call inherit-product, device/samsung/a6eltemtr/device.mk)
include vendor/samsung/configs/a6elte_common/a6elte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately 
include vendor/samsung/configs/a6eltemtr/gms_a6eltemtr.mk
endif

# define google api level for google approval

PRODUCT_NAME := a6eltemtr
PRODUCT_DEVICE := a6eltemtr
PRODUCT_MODEL := SM-A600T1

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Radio App Package
ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

# Add Hancom Viewer for Smartphone
PRODUCT_PACKAGES += \
    HancomOfficeViewer

# MNO Team II 3rd party VUX apps
PRODUCT_PACKAGES += \
    AdaptClient \
    MetroAppStore_MTR \
    Lookout_MTR \
    MetroZone_MTR \
    MyMetro_MTR

# MNO Team II HUX apps
PRODUCT_PACKAGES += \
    NameID_MTR 

# MPCS RSU
PRODUCT_PACKAGES += \
    SimLock_MTR

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
