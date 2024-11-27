QCOM_PRODUCT_DEVICE := crownqltekdi

$(call inherit-product, device/samsung/crownqltekdi/device.mk)
include vendor/samsung/configs/crownqlte_common/crownqlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/crownqltekdi/gms_crownqltekdi.mk
endif


PRODUCT_NAME := crownqltekdi
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SCV40
PRODUCT_BRAND := KDDI
PRODUCT_FINGERPRINT_TYPE := jpn_kdi

# for KDDI Locales 
# include vendor/samsung/build/localelist/SecLocale_KDI.mk
PRODUCT_LOCALES := ja_JP en_US ko_KR pt_BR vi_VN zh_CN es_ES fr_FR in_ID ml_IN ar_AE 

# KDDI
# Remove VoiceRecorder
#PRODUCT_PACKAGES += \
#   -VoiceNote_5.0

# KDDI   
# Add Samsung+ removable 12.01.08.7 as of July 2019
#PRODUCT_PACKAGES += \
#    SAMSUNG_PLUS_REMOVABLE

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
# PRODUCT_PACKAGES += \
#    BlockchainBasicKit

# Removing WebManual
# PRODUCT_PACKAGES += \
#    -WebManual

# KDDI
# VZW/ATT apps
#PRODUCT_PACKAGES += \
#    EmergencyAlert	
	
# Add Spotify
# PRODUCT_PACKAGES += \
#    Spotify

# KDDI    
# Add Hancom Office Editor
#PRODUCT_PACKAGES += \
#    HancomOfficeEditor_Hidden_Install

# KDDI
# Add nextradio wrapper package
#PRODUCT_PACKAGES += \
#    nextradio
	

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# KDDI
# Remove USP for customer delivery (-e omce)
#ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
#PRODUCT_PACKAGES += \
#    -HybridRadio
#endif


# KDDI
# Add OneDrive Removable
#PRODUCT_PACKAGES += \
#    -OneDrive_Samsung_v3 \
#    OneDrive_Samsung_v3_Removable


# Only Crown P

# KDDI
# Add Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \


# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# KDDI
# Add Samsung Ads Setting
#PRODUCT_PACKAGES += \
#    MasDynamicSetting

# Remove eSE features
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp

##### Add for KDDI

# Remove Pico TTS
PRODUCT_PACKAGES += \
     -PicoTts

# Samsung Japanese Keyboard
PRODUCT_PACKAGES += \
    SamsungIMEv3.5JPN \
    iWnnIME_Mushroom \
    -SamsungIMEv3.5

# Dictionary
PRODUCT_PACKAGES += \
     JpnDioDict4

# Remove SamsungConnect
PRODUCT_PACKAGES += \
    -SamsungConnect

# SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Add EasySettingManager
PRODUCT_PACKAGES += \
    EasySettingManager

#[FeliCa] ADD Start
PRODUCT_PACKAGES += \
    FeliCaLock \
    FeliCaRemoteLockKDI \
    FeliCaTest \
    MobileFeliCaClient \
    MobileFeliCaMenuMainApp \
    MobileFeliCaSettingApp \
    MobileFeliCaWebPlugin \
    MobileFeliCaWebPluginBoot \
    sysconfig_jpn_kdi.xml
#[FeliCa] ADD End

#Adding RLL support for My Au
PRODUCT_PACKAGES += \
    RLLHelper

# Samsung service for auShareLink
PRODUCT_PACKAGES += \
    WfdKDDILinkService
    
# Add JPN Preload
PRODUCT_PACKAGES += \
    PlusMessage \
    KLoPApp \
    DisasterApp \
    NfcTsmProxy \
    BtDunApp \
    Facebook_for_au \
    FacebookAppManager_for_au \
    FacebookInstaller_for_au \
    FacebookMessenger_for_au \
    FacebookServices_for_au \
    Instagram_for_au \
    ServiceTOP \
    NewsPass \
    MyAu \
    RepairReceipt \
    AuWidget \
    Twitter_for_au \
    MobileDeviceInformationProvider \
    DataStorageApp \
    WowmaForAu \
    AuMarket \
    TextingWhileWalkingAlert \
    auSmartPass \
    au_setting_menu \
    Satch \
    auWALLET \
    au_Wi-Fi_Connect \
    auBasicHome \
    AuEmail \
    SelfcareSettings \
    OptimalRemoteClient \
    SafetyAccess \
    AuInitialSetting \
    AuShareLink \
    AuWalletMarket
	
# add KDDI permission whitelist
PRODUCT_PACKAGES += \
    privapp-permissions-kddi.xml \
    kddiapp-hiddenapi-package-whitelist.xml
    
# Add Facebook Config Files for KDI
PRODUCT_COPY_FILES += \
    applications/provisional/JPN/KDI/FacebookAppManager/etc/appmanager.conf:system/etc/appmanager.conf \
    applications/provisional/JPN/KDI/FacebookInstaller/privapp-permissions-facebook.xml:system/etc/permissions/privapp-permissions-facebook.xml

# Add JPN Manual APK
PRODUCT_PACKAGES += \
	JpnManualStub
    
# Add MaintenanceMode.apk only in ENG binary
PRODUCT_PACKAGES_ENG += \
	maintenanceMode
	
# Remove samsung SMS for KDDI device
PRODUCT_PACKAGES += \
    -SamsungMessages_11