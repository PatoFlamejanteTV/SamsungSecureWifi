QCOM_PRODUCT_DEVICE := gta2xlltespr

# Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),$(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1))),,$(1))

PU_RESOLUTION := 1200_1920
PRODUCT_PACKAGES += \
    $(call addpuQmgResource,BST_bootsamsung.qmg)\
    $(call addpuQmgResource,BST_bootsamsungloop.qmg)\
    $(call addpuQmgResource,SPR_bootsamsung.qmg)\
    $(call addpuQmgResource,SPR_bootsamsungloop.qmg)\
    $(call addpuQmgResource,VMU_bootsamsung.qmg)\
    $(call addpuQmgResource,VMU_bootsamsungloop.qmg)\
    $(call addpuQmgResource,XAS_bootsamsung.qmg)\
    $(call addpuQmgResource,XAS_bootsamsungloop.qmg)

$(call inherit-product, device/samsung/gta2xlltespr/device.mk)
include vendor/samsung/configs/gta2xllte_common/gta2xllte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gta2xlltespr/gms_gta2xlltespr.mk
endif

PRODUCT_NAME := gta2xlltespr
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T597P
PRODUCT_CHARACTERISTICS += tablet

include vendor/samsung/build/localelist/SecLocale_USA.mk

### Sprint OMADM & Other 3rd Party Apps 
include vendor/samsung/configs/gta2xlltespr/spr_apps.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
	
# Removing WebManual
PRODUCT_PACKAGES += \
    -WebManual
	
# Remove Phone app
PRODUCT_PACKAGES += \
    -SamsungDialer
	
# Remove SamsungInCallUI
PRODUCT_PACKAGES += \
  -SamsungInCallUI

# Microsoft Apps
PRODUCT_PACKAGES += \
    -MSSkype_stub
    
# Remove DeviceSecurity for USA single
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Crane
PRODUCT_PACKAGES += \
    Crane
	
ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS

# Add 64 bit HancomViewer
PRODUCT_PACKAGES += \
    hcellviewer \
    HancomOffice_Shared \
    hshowviewer \
    hanwidget \
    hwordviewer \
    hanviewerlauncher

# Add Hancom font.dat for KNOX
PRODUCT_COPY_FILES += \
    applications/par/edp/HCO/HancomOfficeViewerTablet/64bitOS/hncfont95.dat:system/hncfont95.dat

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar