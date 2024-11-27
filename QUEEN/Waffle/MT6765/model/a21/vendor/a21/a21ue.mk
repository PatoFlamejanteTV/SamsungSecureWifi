$(call inherit-product, device/samsung/a21/device.mk)
include vendor/samsung/configs/a21_common/a21_common.mk

include vendor/samsung/configs/a21/gms_a21ue.mk

PRODUCT_NAME := a21ue
PRODUCT_DEVICE := a21
PRODUCT_MODEL := SM-A215U1

include vendor/samsung/build/localelist/SecLocale_USA.mk


###############################################################
# Remove Packages not needed for USA
###############################################################
# Remove SmartManager for USA
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Remove Voice Recorder
PRODUCT_PACKAGES += \
    -VoiceNote_5.0

# Add WebManual DO NOT add this package for USA and JPN
PRODUCT_PACKAGES += \
    -WebManual

###############################################################
# Add Packages only needed for USA
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

# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome

# Setup Onedrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Setup SamsungPlus
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_REMOVABLE

# Add Verizon SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# TetheringProvision for ATT/VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# GPS
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.location.gps.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.location.gps.xml

# GNSS HAL for HIDL
PRODUCT_PACKAGES += \
    vendor.samsung.hardware.gnss@2.0-service

# GNSS dependency
PRODUCT_PACKAGES += \
    libandroid_net \
    libDR \
    libgeofence \
    libcurl \
    gps_drv.ko

# MTK GPS
include vendor/samsung/hardware/gnss/mediatek/MT6631/common_us.mk
