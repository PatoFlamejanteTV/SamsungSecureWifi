QCOM_PRODUCT_DEVICE := gtaslitelteatt

$(call inherit-product, device/samsung/gtaslitelteatt/device.mk)
include vendor/samsung/configs/gtaslitelte_common/gtaslitelte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtaslitelteatt/gms_gtaslitelteuc.mk
endif

PRODUCT_NAME := gtaslitelteuc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T387AA
PRODUCT_CHARACTERISTICS += att

include vendor/samsung/build/localelist/SecLocale_USA.mk

# 3rd party ATT APKs
PRODUCT_PACKAGES += \
    ready2Go_ATT
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

PRODUCT_PACKAGES += \
    AppSelect_ATT \
    ATTAPNWidget_ATT \
    APNWidgetBaseRoot_ATT \
    ATTMessage_ATT \
    AttTvMode \
    Directv_ATT \
    MyATT_ATT \
    RemoteSupport_ATT \
    AttSmartHelp_ATT

# remove MobileOffice Stub
PRODUCT_PACKAGES += \
    -Excel_SamsungStub \
    -PowerPoint_SamsungStub \
    -Word_SamsungStub

# Remove Samsung FOTA & SyncML packages
PRODUCT_PACKAGES += \
    -FotaAgent \

# Add ATT FOTA for NA ATT
PRODUCT_PACKAGES += \
    wssyncmldm

# SRIN MNO Team : QoSIndicator
PRODUCT_PACKAGES += \
    QoSIndicator_ATT
	
ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Add HancomViewer
PRODUCT_PACKAGES += \
    hcellviewer \
    HancomOffice_Shared \
    hshowviewer \
    hanwidget \
    hwordviewer \
    hanviewerlauncher

# Add Hancom font.dat for KNOX
PRODUCT_COPY_FILES += \
    applications/par/edp/HCO/HancomOfficeViewerTablet/hncfont95.dat:system/hncfont95.dat

# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS
# Remove Samsung Messages
PRODUCT_PACKAGES += \
    -SamsungMessages_11

# Remove SmartManager
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity
