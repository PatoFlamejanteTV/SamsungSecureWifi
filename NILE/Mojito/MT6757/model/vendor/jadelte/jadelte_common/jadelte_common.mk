################################################################################################
##
## You should add feature or packages at the each category.
##
## 1. SEC default packages
## Samsung framework(TW) boot essential + TG requested packages
##
## 2. SEC optional packages
## Optional Samsung packages
##
################################################################################################
 
################################################################################################
## 1. SEC default packages
## Samsung framework(TW) boot essential, Samsung default packages
## Some packages that TG wants to add to default category
################################################################################################

$(call inherit-product, $(SRC_TARGET_DIR)/product/full.mk)

PRODUCT_BRAND := samsung
PRODUCT_MANUFACTURER := samsung

# define the default locales for phone device
include vendor/samsung/build/core/SecLanguage_phone_full.mk

# USB Default setting
PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp

# Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

PU_RESOLUTION := 1080_1920
PRODUCT_PACKAGES += \
    $(call addpuQmgResource,bootsamsung.qmg)\
    $(call addpuQmgResource,bootsamsungloop.qmg)

# Encryption Animation
pu_image_path := vendor/samsung/external/graphics/puQmgResource/img
PRODUCT_COPY_FILES += \
    $(pu_image_path)/xx/1080_1920/crypt_bootsamsung.qmg:system/media/crypt_bootsamsung.qmg \
    $(pu_image_path)/xx/1080_1920/crypt_bootsamsungloop.qmg:system/media/crypt_bootsamsungloop.qmg

# LPM Animation
addlpmSpiResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

LPM_LOOK_TYPE := DreamLook
LPM_RESOLUTION := 1080_1920
PRODUCT_PACKAGES += \
    $(call addlpmSpiResource,lcd_density.txt)\
    $(call addlpmSpiResource,percentage.spi)\
    $(call addlpmSpiResource,battery_error.spi)\
    $(call addlpmSpiResource,battery_low.spi)\
    $(call addlpmSpiResource,dock_error.spi)\
    $(call addlpmSpiResource,battery_temperature_error.spi)\
    $(call addlpmSpiResource,battery_temperature_limit.spi)\
    $(call addlpmSpiResource,charging_A1.spi)\
    $(call addlpmSpiResource,charging_A2.spi)\
    $(call addlpmSpiResource,charging_B1.spi)\
    $(call addlpmSpiResource,charging_B2.spi)\
    $(call addlpmSpiResource,charging_C1.spi)\
    $(call addlpmSpiResource,charging_C2.spi)\
    $(call addlpmSpiResource,charging_C3.spi)\
    $(call addlpmSpiResource,charging_C4.spi)\
    $(call addlpmSpiResource,battery_100.spi)\
    $(foreach tens, 0 1 2 3 4 5 6 7 8 9, \
    $(foreach units, 0 1 2 3 4 5 6 7 8 9, \
    $(call addlpmSpiResource, battery_0$(join $(tens),$(units)).spi)))

# Power Down Animation
addpdQmgResource = $(if $(findstring $(1),$(PRODUCT_COPY_FILES)),,$(1))
SHUTDOWN_ANIM_REVISION := 2016
TARGET_RESOLUTION := 1080x1920
PRODUCT_PACKAGES += \
    $(call addpdQmgResource,shutdown.qmg)

# Samsung-Framework

# Add SamsungWallpaper
PRODUCT_PACKAGES += \
    wallpaper-res

# Multi-Screen
-include vendor/samsung/frameworks/samsung-framework/samsung-framework.mk

# Settings
PRODUCT_PACKAGES +=  \
    PreloadInstaller \
    SecSetupWizard2015 \
    SecSettingsProvider2 \
    SecSettings2 \
    SettingsReceiver

ifeq ($(SEC_BUILD_OPTION_BIGDATA_UT), true)
PRODUCT_PROPERTY_OVERRIDES += ro.hwparam.ut=true
endif

# Add DeviceQualityAgent
PRODUCT_PACKAGES += \
    DeviceQualityAgent

# Accessibility
PRODUCT_PACKAGES += \
    STalkback \
    AccessControl_N \
    AssistantMenu_N \
    ColorBlind_N \
    UniversalSwitch

# Add SoundAlive
PRODUCT_PACKAGES += \
    SoundAlive_52 \
    -MusicFX

# Add Hearingdro
PRODUCT_PACKAGES += \
    Hearingdro_V6

# MobilePrint
PRODUCT_PACKAGES += \
    SPrintSpooler7

# SamsungInCallUI
PRODUCT_PACKAGES += \
  SamsungInCallUI

# SamsungNetworkUI
PRODUCT_PACKAGES += \
    NetworkUI

# Add DRParser
PRODUCT_PACKAGES += \
    DRParser

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts81

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SecContactsProvider

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81

# for DataCreate
PRODUCT_PACKAGES += \
    AutomationTest_FB	

# Add SHealth
PRODUCT_PACKAGES += \
    SHealth5 \
    HealthService

# Launcher
PRODUCT_PACKAGES += \
    TouchWizHome_2017\
    BadgeProvider_N

# Samsung Messaging
PRODUCT_PACKAGES += \
    Messaging_SEP81 \
    MsgCommService \
    SecTelephonyProvider_Epic

# Allshare Framework
PRODUCT_PACKAGES +=  \
    allshare \
    AllshareMediaShare \
    SamsungConnect

# FileShare
PRODUCT_PACKAGES +=  \
    AllshareFileShare

# Remove google app
PRODUCT_PACKAGES += \
    -messaging

# Add MediaProvider
PRODUCT_PACKAGES += \
    SecMediaProvider

# Add FingeprintManagerService
PRODUCT_PACKAGES += FingerprintService2
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

# Fingerprint Daemon for Non TZ
ifeq ($(SEC_BUILD_CONF_USE_FINGERPRINT_TZ),false)
PRODUCT_PACKAGES += FingerprintDaemonForNonTZ
endif

# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv6 \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic

# Add Calendar
PRODUCT_PACKAGES += \
    SamsungCalendar_SEP85_GreatUX

# Add Calendar Providers
PRODUCT_PACKAGES += \
    SecCalendarProvider_NOTSTICKER \
    TasksProvider

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder

# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    WfdBroker \
    withTV \
    SmartMirroring

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

# Add SmartCapture
PRODUCT_PACKAGES += \
    SmartCapture

# Add Clock
PRODUCT_PACKAGES += \
    -DeskClock \
    ClockPackage_N

# Find My Mobile
PRODUCT_PACKAGES += \
    Fmm

# Remote Mobile manager
PRODUCT_PACKAGES += \
    Rlc

# add Calculator
PRODUCT_PACKAGES += \
    SecCalculator_N_R

#Add SecGallery2015
PRODUCT_PACKAGES += \
    SecGallery2015 \
    com.sec.android.gallery3d.xml

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_WQHD_Great
    
# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_WQHD_Dream

# Add watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub

# CMHProvider
PRODUCT_PACKAGES += \
    CMHProvider

# dual IM
PRODUCT_PACKAGES += \
    DAAgent

# KNOX-DLP
PRODUCT_PACKAGES += \
    SamsungDLPService

# CMHServices
PRODUCT_PACKAGES += \
    DCMService \
    IPService \
    FaceService \
    StoryService

#Add My files
PRODUCT_PACKAGES += \
    SecMyFiles2017

#Add Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

#Add Voice Recorder
PRODUCT_PACKAGES += \
    VoiceNote_5.0

# Add Samsung Notes
PRODUCT_PACKAGES += Notes15_Removable

# Add SGames
PRODUCT_PACKAGES += \
    GameHome \
    GameTools

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# Add Samsung Camera packages
PRODUCT_PACKAGES += \
    SamsungCamera7 \
    ShootingModeProvider2 \
    FilterInstaller \
    FilterProvider \
    StickerFaceAR \
    StickerWatermark2 \
    StickerStamp2 \
    StickerProvider2

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3

#Add SCPM_Client
PRODUCT_PACKAGES += \
    SCPMClient_N
    
# Add SmartManager
PRODUCT_PACKAGES += \
    SmartManager_v5_DeviceSecurity \
    SmartManager_v5

# Add DeviceHealthReporter
PRODUCT_PACKAGES += \
    DeviceHealthReporter

# Add semextendedformat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml

# Adding MobileOffice Stub
PRODUCT_PACKAGES += \
    Excel_SamsungStub \
    PowerPoint_SamsungStub \
    Word_SamsungStub

# Microsoft apps
PRODUCT_PACKAGES += \
    MSSkype_stub

#Add OneDrive
PRODUCT_PACKAGES += \
    OneDrive_Samsung_v2

#Add secvision
PRODUCT_PACKAGES += \
        secvision \
        secvision.xml

#Add saiv
PRODUCT_PACKAGES += \
    saiv \
    saiv.xml

# ADD AIR SERVICE (Funny Package)
PRODUCT_PACKAGES += \
    visiond

# Add Dual OutFocus Viewer
PRODUCT_PACKAGES += \
	DualOutFocusViewer

# Add VisionIntelligence
PRODUCT_PACKAGES += \
    VisionIntelligence
   
# MTK GPS
include vendor/samsung/external/gps/mediatek/MT6625L/common.mk

################################################################################################
## 2. SEC optional packages
## Optional Samsung packages
################################################################################################

# Add ContextProvider
PRODUCT_PACKAGES += \
	ContextProvider

# Send SOS Message
PRODUCT_PACKAGES += \
    SendHelpMessage

# Safety Care
PRODUCT_PACKAGES += \
    EmergencyLauncher \
    EmergencyModeService \
    EmergencyProvider

# Add SafetyInformation
PRODUCT_PACKAGES += \
    SafetyInformation

# Add Dictionary
PRODUCT_PACKAGES += \
    DictDiotek_update

# Add EasySetup
PRODUCT_PACKAGES += \
    EasySetup

# add Email
PRODUCT_PACKAGES += \
    SecEmail_N \
    com.samsung.android.email.provider.xml

# add SBrowser apk
PRODUCT_PACKAGES += \
    SBrowser_5.4 \
    browser.xml \
    com.sec.android.app.sbrowser.xml \
	SecHTMLViewer \
	-Browser2
	
# add ChromeCustomizations
PRODUCT_PACKAGES += \
    ChromeCustomizations

# Add Download apks
PRODUCT_PACKAGES += \
    SecDownloadProvider

# add WebManual DO NOT add this package for USA & JPN
PRODUCT_PACKAGES += \
    WebManual

# Add WeatherWidget
PRODUCT_PACKAGES += \
   Weather2017_MASS \
   WeatherWidget2017_MASS

# Add SimManager
PRODUCT_PACKAGES += \
    SimSettingMgr

# Add Smart Switch Agent
PRODUCT_PACKAGES += \
    SmartSwitchAgent

# Add Kies
PRODUCT_PACKAGES += \
    wssyncmlnps2

# EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

# Add Handwriting
PRODUCT_PACKAGES += \
	HandwritingService \
	libSDKRecognitionText
	
#KNOX_CONTAINER_START V2.0
ifeq ($(SEC_KNOX_CONTAINER_VERSION),$(filter $(SEC_KNOX_CONTAINER_VERSION), v30 v35 v40))
PRODUCT_PACKAGES += \
    KnoxRemoteContentsProvider \
    KnoxSetupWizardClient \
    KnoxShortcuts \
    KnoxSwitcher \
    KnoxFolderContainer2 \
    BBCAgent \
    SecureFolder \
    KnoxBBCProvider \
    KnoxKeyguard \
    KnoxAppsUpdateAgent \
    KnoxBluetooth \
    KnoxTrustAgent \
    ContainerAgent2
endif
#KNOX_CONTAINER_END V2.0

PRODUCT_PACKAGES += \
    SMusicPicker

# Add ThemeCenter and UPSM Theme
PRODUCT_PACKAGES += \
    ThemeCenter \
    UPSMTheme
	
# Add StickerCenter
PRODUCT_PACKAGES += \
    StickerCenter

# Add Samsung Themes
PRODUCT_PACKAGES += \
    SamsungThemes	

# Add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyApps_3xh
    
# Add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyAppsWidget_Phone_Hero    

# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient_Prod	
   
# Add Finder
PRODUCT_PACKAGES += \
    Finder_v7

# Add Finder FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml

# SLocation
PRODUCT_COPY_FILES += \
	frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml
PRODUCT_COPY_FILES += \
	vendor/samsung/external/gps/common/slocation/libsgeo.so:system/lib/slocation/libsgeo.so
PRODUCT_PACKAGES += \
	SLocation

#add Rubin 1.5 version
PRODUCT_PACKAGES += \
    RubinVersion15

# Add Samsung Cloud
PRODUCT_PACKAGES += \
    SamsungCloudDreamNewIcon

# Add Facebook Apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS

# UnifiedProfile
PRODUCT_PACKAGES += \
    UnifiedProfile

# add CarmodeStub apk
PRODUCT_PACKAGES += \
    CarmodeStub

# Add CoreApps
PRODUCT_PACKAGES += \
   CoreApps_SDK_2017 \
   LinkSharing_v34

# Keyguard wallpaper updator
PRODUCT_PACKAGES += \
    KeyguardWallpaperUpdator

# Add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer_Phone

# Add Samsung KMS Agent
PRODUCT_PACKAGES += \
    libsem_jni \
    SKMSAgent

# Add SKPM
PRODUCT_PACKAGES += \
    libsec_skpm

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter
  
# Add SVoicePLM
PRODUCT_PACKAGES += \
    SVoicePLM
	
#add VoiceServiceFramework
PRODUCT_PACKAGES += \
    VoiceServiceFramework
	

# add MirrorLink tms
PRODUCT_PACKAGES += \
    audio.tms.default

# add MirrorLink libtmdisplay
PRODUCT_PACKAGES += \
    libtmdisplay

# add MirrorLink apk
PRODUCT_PACKAGES += \
    MirrorLink

# MirrorLink feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.mirrorlink_fw_level4.xml:system/etc/permissions/com.samsung.feature.mirrorlink_fw_level4.xml
# copy SideSync Daemon
PRODUCT_COPY_FILES += \
  applications/sources/apps/SideSync/ss_conn_daemon:system/bin/ss_conn_daemon

# Add Samsung TTS
PRODUCT_PACKAGES += SamsungTTS
#PRODUCT_PACKAGES += smt_ko_KR_f00
#PRODUCT_PACKAGES += smt_ko_KR_m00
PRODUCT_PACKAGES += smt_en_US_f00
#PRODUCT_PACKAGES += smt_en_GB_f00
#PRODUCT_PACKAGES += smt_zh_CN_f00
#PRODUCT_PACKAGES += smt_zh_CN_m00
#PRODUCT_PACKAGES += smt_es_ES_f00
#PRODUCT_PACKAGES += smt_de_DE_f00
#PRODUCT_PACKAGES += smt_fr_FR_f00
#PRODUCT_PACKAGES += smt_it_IT_f00
#PRODUCT_PACKAGES += smt_ja_JP_f00
#PRODUCT_PACKAGES += smt_ja_JP_m00
#PRODUCT_PACKAGES += smt_ru_RU_f00
#PRODUCT_PACKAGES += smt_pt_PT_f00
#PRODUCT_PACKAGES += smt_pt_BR_f00
#PRODUCT_PACKAGES += smt_es_MX_f00
#PRODUCT_PACKAGES += smt_hi_IN_f00
#PRODUCT_PACKAGES += smt_en_IN_f00

# Add AODService (Always on display)
PRODUCT_PACKAGES += \
    AODService_v28

# Add Always on display System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.aodservice_v04.xml:system/etc/permissions/com.samsung.feature.aodservice_v04.xml

# Samsung Billing
PRODUCT_PACKAGES += \
    SamsungBilling

# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome

# Launcher
PRODUCT_PACKAGES += \
    TouchWizHome_2017\
    
# Live Wallpaper
PRODUCT_PACKAGES += \
    SecLiveWallpapersPicker

# Live wallpaper feature
PRODUCT_COPY_FILES += \
    applications/sources/wallpapers/LiveWallpaperPicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml

# Add BioFaceService
PRODUCT_PACKAGES += BioFaceService

# Add Virtual Screen Framework Feature
PRODUCT_PACKAGES += \
    com.samsung.feature.virtualscreen.xml
    
# Add AttestationProxy and FAST
PRODUCT_PACKAGES += \
    Fast \
    AttestationProxy

