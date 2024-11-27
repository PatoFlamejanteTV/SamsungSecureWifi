QCOM_PRODUCT_DEVICE := gtasliteltevzw

$(call inherit-product, device/samsung/gtasliteltevzw/device.mk)
include vendor/samsung/configs/gtaslitelte_common/gtaslitelte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gtasliteltevzw/gms_gtasliteltevzw.mk
endif

PRODUCT_NAME := gtasliteltevzw
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T387V
PRODUCT_CHARACTERISTICS := tablet
PRODUCT_BRAND := Verizon

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

	
# Add LLK Agent
PRODUCT_PACKAGES += \
    LLKAgent \
    com.customermobile.preload.vzw
	
# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Remove Samsung Messages(VZW)
PRODUCT_PACKAGES += \
    -SamsungMessages_11

# Remove SmartManager
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Remove Onedrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

# Remove KidsHome
PRODUCT_PACKAGES += \
    -KidsHome
    -KidsHome_Installer
	
ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif
