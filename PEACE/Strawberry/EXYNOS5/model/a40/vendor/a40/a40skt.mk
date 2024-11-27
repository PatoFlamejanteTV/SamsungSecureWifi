PRODUCT_COPY_FILES += device/samsung/a40_common/init.a40skt.rc:$(TARGET_COPY_OUT_VENDOR)/etc/init/init.exynos7885.rc

include vendor/samsung/configs/a40/a40xx.mk

PRODUCT_NAME := a40skt
PRODUCT_MODEL := SM-A405S

include vendor/samsung/build/localelist/SecLocale_KOR.mk

# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader

# Add DMB
PRODUCT_PACKAGES += \
    Tdmb

# Add DMB HW Service
PRODUCT_PACKAGES += \
    vendor.samsung.hardware.tdmb@1.0-service

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# Add LinkedIn Stub
PRODUCT_PACKAGES += \
    -LinkedIn_SamsungStub

# Remove Facebook apps
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

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01
PRODUCT_PACKAGES += -smt_en_GB_f00
PRODUCT_PACKAGES += -smt_de_DE_f00
PRODUCT_PACKAGES += -smt_fr_FR_f00
PRODUCT_PACKAGES += -smt_it_IT_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_es_ES_f00

########################################################################################
# SKT Carrier Apps
# Add HiddenMenu
PRODUCT_PACKAGES += \
    SKTHiddenMenu

#Add System app
PRODUCT_PACKAGES += \
    TPhoneOnePackage \
	mobileTworld \
	SKTMemberShip \
    SKTOneStore

# Add Removable app
PRODUCT_PACKAGES += \
    11st \
	TContacts \
    TCallHelper \
    MusicMate \
    TGuard_Full \
    OKCashbag \
    SmartWallet \
    Oksusu \
    Tmap40_Erasable \
    SmartBill_Full \
    NatePortal \
    Pooq \
    TMapTaxi \
    CloudBerry \
    SyrupGifticon_stub \
    Tpay_stub

# Add Hidden App	
PRODUCT_PACKAGES += \
    SktUsimService \
	TPhoneSetup \
	OneStoreService \
	HPSClient \
	ONEstoreSetupwizard \
	SmartPush \
	SafetyCleaner
	
# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Copy SO file for ONE store Service
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/Common/OneStoreService/libARMClientService.so:system/lib/libARMClientService.so \
    applications/provisional/KOR/Common/OneStoreService/libARMPlatform.so:system/lib/libARMPlatform.so \
    applications/provisional/KOR/Common/OneStoreService/libARMService.so:system/lib/libARMService.so \
    applications/provisional/KOR/Common/OneStoreService/libCheshireCat.so:system/lib/libCheshireCat.so \
    applications/provisional/KOR/Common/OneStoreService/libSystem.so:system/lib/libSystem.so \
    applications/provisional/KOR/Common/OneStoreService/libARMClientService_arm64-v8a.so:system/lib/libARMClientService_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libARMPlatform_arm64-v8a.so:system/lib/libARMPlatform_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libARMService_arm64-v8a.so:system/lib/libARMService_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libCheshireCat_arm64-v8a.so:system/lib/libCheshireCat_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libSystem_arm64-v8a.so:system/lib/libSystem_arm64-v8a.so

# Copy DAT file for ONE-store
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/SKT/SKTOneStore/uafield/uafield_a20skt.dat:system/skt/ua/uafield.dat
########################################################################################

# HiddenNetworkSetting
PRODUCT_PACKAGES += \
  HiddenNetworkSetting 

# Remove SmartCallProvider
PRODUCT_PACKAGES += \
  -HiyaService

# Remove [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
  -RcsSettings

# Remove Crane
PRODUCT_PACKAGES += \
  -Crane
  
# Remove Secure Wi-Fi
PRODUCT_PACKAGES += \
   -Fast
