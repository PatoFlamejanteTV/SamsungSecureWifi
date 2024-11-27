QCOM_PRODUCT_DEVICE := gtasliteltecs

$(call inherit-product, device/samsung/gtasliteltecs/device.mk)
include vendor/samsung/configs/gtaslitelte_common/gtaslitelte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtasliteltecs/gms_gtasliteltecs.mk
endif

PRODUCT_NAME := gtasliteltecs
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T387W

include vendor/samsung/build/localelist/SecLocale_CAN.mk


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_fr_CA_f00
	
# Add GearManager
PRODUCT_PACKAGES += \
    GearManager
	
# Add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers
	
ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif
	
# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    -appmanager.conf \
    FBInstaller_NS
