BUILD_SYMLINK_TO_CARRIER := true

$(call inherit-product, device/samsung/a51/device.mk)
include vendor/samsung/configs/a51_common/a51_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a51/gms_a51tfn.mk
endif

PRODUCT_NAME := a51tfn
PRODUCT_DEVICE := a51
PRODUCT_MODEL := SM-S515DL

include vendor/samsung/build/localelist/SecLocale_USA.mk


###############################################################
# Remove Packages not needed for USA
###############################################################
# Remove Upday
PRODUCT_PACKAGES += \
    -Upday

# Remove Voice Recorder
PRODUCT_PACKAGES += \
    -VoiceNote_5.0

# Remove GLISPA App Package
PRODUCT_PACKAGES += \
    -GLISPA	

# Remove Ignite App Package
PRODUCT_PACKAGES += \
    -AURA

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

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# TetheringProvision for ATT/VZW
PRODUCT_PACKAGES += \
    UnifiedTetheringProvision

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# Add Microsoft OfficeMobile headless for USA
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub \
    OfficeMobile_SamsungHeadless

# Add Onedrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

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

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Ramen common
include vendor/samsung/hardware/gnss/slsi/ramen/common_evolution_us_64.mk

# Ramen GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else 
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.na.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg \
    vendor/samsung/hardware/gnss/slsi/ramen/config/ramen.na.cdma.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps_CDMA.cfg
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################	