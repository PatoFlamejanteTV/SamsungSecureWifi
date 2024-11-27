$(call inherit-product, device/samsung/a21/device.mk)
include vendor/samsung/configs/a21_common/a21_common.mk

include vendor/samsung/configs/a21/gms_a21tfn.mk

PRODUCT_NAME := a21tfn
PRODUCT_DEVICE := a21
PRODUCT_MODEL := SM-S215DL

include vendor/samsung/build/localelist/SecLocale_USA.mk


###############################################################
# Remove Packages not needed for USA
###############################################################
# Remove Voice Recorder
PRODUCT_PACKAGES += \
    -VoiceNote_5.0

# Add WebManual DO NOT add this package for USA and JPN
PRODUCT_PACKAGES += \
    -WebManual

# Remove SmartManager for USA
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
###############################################################

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add HiddenMenu for VZW
PRODUCT_PACKAGES += \
    HiddenMenu

# Add InputEventApp for VZW
PRODUCT_PACKAGES += \
    InputEventApp \
    libDiagService_

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# TetheringProvision for ATT/VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Add Samsung+ removable
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_REMOVABLE

# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
endif

#GPS
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.location.gps.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.location.gps.xml

#GNSS HAL for HIDL
PRODUCT_PACKAGES += \
    vendor.samsung.hardware.gnss@2.0-service

#GNSS dependency
PRODUCT_PACKAGES += \
    libandroid_net \
    libDR \
    libgeofence \
    libcurl \
    gps_drv.ko

# MTK GPS
include vendor/samsung/hardware/gnss/mediatek/MT6631/common_us.mk