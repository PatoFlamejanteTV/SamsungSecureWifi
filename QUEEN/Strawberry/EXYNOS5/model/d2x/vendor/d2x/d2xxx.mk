$(call inherit-product, device/samsung/d2x/device.mk)
include vendor/samsung/configs/d2x_common/d2x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/d2x/gms_d2xxx.mk
endif

PRODUCT_NAME := d2xxx
PRODUCT_DEVICE := d2x
PRODUCT_MODEL := SM-N976B

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Remove HiyaService for Smart Call 
PRODUCT_PACKAGES += \
    -HiyaService \

# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    Facebook_stub_Removable

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# HiddenNetworkSetting 
PRODUCT_PACKAGES += \
    HiddenNetworkSetting

# Remove Upday 
PRODUCT_PACKAGES += \
    -Upday \

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast