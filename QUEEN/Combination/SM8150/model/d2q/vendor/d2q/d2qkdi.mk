QCOM_PRODUCT_DEVICE := d2q

$(call inherit-product, device/samsung/d2q/device.mk)
include vendor/samsung/configs/d2q_common/d2q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/d2q/gms_d2qkdi.mk
endif

PRODUCT_NAME := d2qkdi
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SCV45
PRODUCT_BRAND := KDDI
PRODUCT_FINGERPRINT_TYPE := jpn_kdi

# for KDDI Locales
#include vendor/samsung/build/localelist/SecLocale_JPN.mk
PRODUCT_LOCALES := ja_JP en_US ko_KR pt_BR vi_VN zh_CN es_ES fr_FR in_ID ml_IN ar_AE
 
# Remove Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \

# SVoiceIME
PRODUCT_PACKAGES += \
    -SVoiceIME

# Add Spotify
PRODUCT_PACKAGES += \
    Spotify

# Remove eSE features
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp

# Add CellBroadcastReceiver
PRODUCT_PACKAGES += \
    SecCellBroadcastReceiver   

# Remove SetupWizard Bixby
PRODUCT_PACKAGES +=  \
    -SuwScriptPlayer

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

# Dictionary for Japan market
PRODUCT_PACKAGES += \
     JpnDioDict4

# Remove Samsung Messages
PRODUCT_PACKAGES += \
    -SamsungMessages_11

# SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Remove SamsungConnect
PRODUCT_PACKAGES += \
    -SamsungConnect
     
# Samsung Japanese Keyboard
PRODUCT_PACKAGES += \
	SamsungIMEv3.5JPN \
	iWnnIME_Mushroom \
	-SamsungIMEv3.5

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_JPN.mk
endif
###############################################################

# Add EasySettingManager
PRODUCT_PACKAGES += \
    EasySettingManager

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ja_JP_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

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
    AuInitialSetting
	
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

#Adding RLL support for My Au
PRODUCT_PACKAGES += \
    RLLHelper