QCOM_PRODUCT_DEVICE := r3q

$(call inherit-product, device/samsung/r3q/device.mk)
include vendor/samsung/configs/r3q_common/r3q_common.mk

# FactoryBinary doesn't need gms packages.
# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r3q/gms_r3qzc.mk
endif

PRODUCT_NAME := r3qzc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A9080

include vendor/samsung/build/localelist/SecLocale_CHN.mk

# Add SoftsimService
PRODUCT_PACKAGES += \
    SoftsimService_V41

# Add softsimd daemon
PRODUCT_PACKAGES += \
    libcommapiaidl.so \
    softsimd

# Add SamsungDataStore
PRODUCT_PACKAGES += \
    SamsungDataStore

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01


# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Remove HiyaService for Smart Call 
PRODUCT_PACKAGES += \
    -HiyaService \

# Remove Upday 
PRODUCT_PACKAGES += \
    -Upday \

# Delete Google Search
PRODUCT_PACKAGES += \
    -QuickSearchBox

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    -FBAppManager_NS \
    -appmanager.conf \
    -FBInstaller_NS \
    -FBServices

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable
    
# HiddenNetworkSetting 
PRODUCT_PACKAGES += \
    HiddenNetworkSetting

# Samsung Members for CHN models
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SamsungMembers_CHN_P_Removable

# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader

# Add for MessagingExtension
PRODUCT_PACKAGES += \
    SamsungMessages_Extension_Chn_11.0

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone
	
#FM Radio
PRODUCT_PACKAGES += \
    HybridRadio
    
# Remove Spotify 
PRODUCT_PACKAGES += \
    -Spotify

# CHN DM
PRODUCT_PACKAGES += \
    DeviceManagement

# Add HongbaoAssistant
PRODUCT_PACKAGES += \
    HongbaoAssistant

# Add VIP mode
PRODUCT_PACKAGES += \
    Firewall

# Add Spam Call
PRODUCT_PACKAGES += \
    BstSpamCallService

# Add Tencent Phone Number Locator
PRODUCT_PACKAGES += \
    PhoneNumberLocatorService

# Remove SmartCallProvider
PRODUCT_PACKAGES += \
    -SmartCallProvider \
    -HiyaService

# Add CHN PermissionController
PRODUCT_PACKAGES += \
    PermissionController_CHN