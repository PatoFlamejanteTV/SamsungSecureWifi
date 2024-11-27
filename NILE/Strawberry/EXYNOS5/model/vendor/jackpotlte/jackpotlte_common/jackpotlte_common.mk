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

# GPS Evolution RIL interface.
PRODUCT_PACKAGES += \
    libwrappergps

# Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

PU_RESOLUTION := 1080_2220
PRODUCT_PACKAGES += \
    $(call addpuQmgResource,bootsamsung.qmg)\
    $(call addpuQmgResource,bootsamsungloop.qmg)

# Encryption Animation
pu_image_path := vendor/samsung/external/graphics/puQmgResource/img/DreamLook
PRODUCT_COPY_FILES += \
    $(pu_image_path)/xx/1080_2220/crypt_bootsamsung.qmg:system/media/crypt_bootsamsung.qmg \
    $(pu_image_path)/xx/1080_2220/crypt_bootsamsungloop.qmg:system/media/crypt_bootsamsungloop.qmg

# LPM Animation
addlpmSpiResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

LPM_LOOK_TYPE := DreamLook
LPM_RESOLUTION := 1080_2220
PRODUCT_PACKAGES += \
    $(call addlpmSpiResource,lcd_density.txt)\
    $(call addlpmSpiResource,battery_fast_charging_global.spi)\
    $(call addlpmSpiResource,percentage.spi)\
    $(call addlpmSpiResource,battery_error.spi)\
    $(call addlpmSpiResource,battery_low.spi)\
    $(call addlpmSpiResource,dock_error.spi)\
    $(call addlpmSpiResource,battery_temperature_error.spi)\
    $(call addlpmSpiResource,battery_temperature_limit.spi)\
    $(call addlpmSpiResource,temperature_limit_unplug.spi)\
    $(call addlpmSpiResource,water_protection.spi)\
    $(call addlpmSpiResource,battery_water.spi)\
    $(call addlpmSpiResource,incomplete_connect.spi)\
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
TARGET_RESOLUTION := 1080x2220
PRODUCT_PACKAGES += \
    $(call addpdQmgResource,shutdown.qmg)

# Samsung-Framework
# Multi-Screen
-include vendor/samsung/frameworks/samsung-framework/samsung-framework.mk

# Settings
PRODUCT_PACKAGES +=  \
    PreloadInstaller \
    SecSetupWizard2015 \
    SecSettingsProvider2 \
    SecSettings2 \
    SettingsReceiver

# Accessibility
PRODUCT_PACKAGES += \
    STalkback \
    AccessControl_N \
    AssistantMenu_N \
    ColorBlind_N \
    UniversalSwitch

# Add Barge In New shared lib
PRODUCT_PACKAGES += \
  libBargeInEngine

# Add Samsung Barge In data
PRODUCT_PACKAGES += \
  BargeIn_Samsung_Camera

# Add Sim card manager(DUOS model)
PRODUCT_PACKAGES += \
    SimSettingMgr   

# MobilePrint
PRODUCT_PACKAGES += \
    MobilePrintSvc_Samsung \
    SPrintSpooler7

# Add Dictionary(Updatable)
PRODUCT_PACKAGES += \
    DictDiotek_update

# SamsungInCallUI
PRODUCT_PACKAGES += \
  SamsungInCallUI

# SamsungNetworkUI
PRODUCT_PACKAGES += \
    NetworkUI

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts85

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SamsungContactsProvider85

# Add CoreApps
PRODUCT_PACKAGES += \
   CoreApps_SDK_2017 \
   LinkSharing_v34

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81

# EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

# Launcher
PRODUCT_PACKAGES += \
    TouchWizHome_2017 \
    BadgeProvider_N

# Flipboard Briefing
PRODUCT_PACKAGES += \
    FlipboardBriefing

# Samsung Messaging
PRODUCT_PACKAGES += \
    Messaging_SEP81 \
    MsgCommService \
    SecTelephonyProvider_Epic

# Remove google app
PRODUCT_PACKAGES += \
    -messaging

# Add MediaProvider
PRODUCT_PACKAGES += \
    SecMediaProvider

# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv6_Jackpot \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic

# for DataCreate
PRODUCT_PACKAGES += \
    AutomationTest_FB

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

# Add Clock
PRODUCT_PACKAGES += \
    -DeskClock \
    ClockPackage_N_Verified

# Find My Mobile
PRODUCT_PACKAGES += \
    Fmm

# Remote Mobile Manager
PRODUCT_PACKAGES += \
    Rlc
	
# add Calculator
PRODUCT_PACKAGES += \
    SecCalculator_N_R

#Add SecGallery2015
PRODUCT_PACKAGES += \
    SecGallery2015 \
    com.sec.android.gallery3d.xml
	
# Add VisionProvider
PRODUCT_PACKAGES += \
	VisionProvider
	
# CMHProvider
PRODUCT_PACKAGES += \
    CMHProvider

# CMHServices
PRODUCT_PACKAGES += \
    DCMService \
    IPService \
    FaceService \
    StoryService \
    MediaLearningPlatform
	

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_WQHD_Great

# Add watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub	

# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_WQHD_Dream

#Add My files
PRODUCT_PACKAGES += \
    SecMyFiles2017

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# Keyguard wallpaper updator
PRODUCT_PACKAGES += \
    KeyguardWallpaperUpdator

# Live Wallpaper
PRODUCT_PACKAGES += \
    SecLiveWallpapersPicker

# Add SamsungWallpaper
PRODUCT_PACKAGES += \
    wallpaper-res

# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    withTV \
    SmartMirroring

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

# Add Samsung Camera packages
PRODUCT_PACKAGES += \
    SamsungCamera7 \
    ShootingModeProvider2 \
    FilterInstaller \
    FilterProvider \
    StickerFaceAR \
    StickerStamp2 \
    StickerWatermark2 \
    StickerProvider2 \
    StickerCenter

#Add semextendedformat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml

# Add SmartCapture
PRODUCT_PACKAGES += \
    SmartCapture

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3

# Chrome Customization
PRODUCT_PACKAGES += \
    ChromeCustomizations
	
# Add SCPM Client
PRODUCT_PACKAGES += \
    SCPMClient_N

# Add SmartManager
PRODUCT_PACKAGES += \
    SmartManager_v5_DeviceSecurity \
    SmartManager_v5

# UnifiedProfile 1.1
PRODUCT_PACKAGES += \
    UnifiedProfileGreat

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

# Add VisionIntelligence
PRODUCT_PACKAGES += \
    VisionIntelligence

# Adding MobileOffice Stub
PRODUCT_PACKAGES += \
    Excel_SamsungStub \
    PowerPoint_SamsungStub \
    Word_SamsungStub

# Adding LinkedIn Stub
PRODUCT_PACKAGES += \
    LinkedIn_SamsungStub

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

# Add EasySetup
PRODUCT_PACKAGES += \
    EasySetup

# add Email
PRODUCT_PACKAGES += \
    SecEmail_N \
    com.samsung.android.email.provider.xml

# add SBrowser apk
PRODUCT_PACKAGES += \
    SBrowser_6.0_Removable \
    browser.xml \
    com.sec.android.app.sbrowser.xml

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
    KnoxTrustAgent \
    KnoxBluetooth \
    ContainerAgent2 \
    KnoxAppsUpdateAgent
endif
#KNOX_CONTAINER_END V2.0

# dual IM
PRODUCT_PACKAGES += \
    DAAgent

# KNOX-DLP
PRODUCT_PACKAGES += \
    SamsungDLPService	

# Add Download apks
PRODUCT_PACKAGES += \
    SecDownloadProvider

# add WebManual DO NOT add this package for USA & JPN
PRODUCT_PACKAGES += \
    WebManual

# Add WeatherWidget
PRODUCT_PACKAGES += \
   Weather2017_SEP8.5 \
   WeatherWidget2017_SEP8.5

# SoundAlive
PRODUCT_PACKAGES += \
    SoundAlive_52 \
    -MusicFX

# Add Hearingdro(Adapt Sound6.0)
PRODUCT_PACKAGES += \
    Hearingdro_V6

# Add Smart Switch Agent
PRODUCT_PACKAGES += \
    SmartSwitchAgent

# Add Kies
PRODUCT_PACKAGES += \
    wssyncmlnps2

# Add SVC Agent
PRODUCT_PACKAGES += \
    SVCAgent

PRODUCT_PACKAGES += \
    SMusicPicker

ifeq ($(SEC_BUILD_OPTION_BIGDATA_UT), true)
PRODUCT_PROPERTY_OVERRIDES += ro.hwparam.ut=true
endif

# Add DeviceQualityAgent
PRODUCT_PACKAGES += \
    DeviceQualityAgent

# Add ThemeCenter and UPSM Theme
PRODUCT_PACKAGES += \
    ThemeCenter \
    UPSMTheme

# Add StickerCenter
PRODUCT_PACKAGES += \
    StickerCenter
    
# Add Samsung Themes
PRODUCT_PACKAGES += \
    SamsungThemes_v4_jackpot

# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

# Add VideoTrimmer
PRODUCT_PACKAGES += \
   VideoTrimmer_Phone

# Voice Recorder
PRODUCT_PACKAGES += \
    VoiceNote_5.0_Task
	
#add VoiceServiceFramework
PRODUCT_PACKAGES += \
    VoiceServiceFramework \
    SVoicePLM 
    
#add Voice wakeup APK
PRODUCT_PACKAGES += \
    VoiceWakeUp

# Add Samsung Cloud 2.5
PRODUCT_PACKAGES += \
	SamsungCloudJackpot

# Add SHealth
PRODUCT_PACKAGES += \
    SHealth5 \
    HealthService

#add Rubin 1.5 version
PRODUCT_PACKAGES += \
    RubinVersion15
    
#add Bixby
PRODUCT_PACKAGES += \
    Bixby
    
# Add BioFaceService
PRODUCT_PACKAGES += BioFaceService

# Add FingeprintManagerService
PRODUCT_PACKAGES += FingerprintService2
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

# Fingerprint Daemon for Non TZ
ifeq ($(SEC_BUILD_CONF_USE_FINGERPRINT_TZ),false)
PRODUCT_PACKAGES += FingerprintDaemonForNonTZ
endif

# Add Beamservice
PRODUCT_PACKAGES += \
    BeamService

# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# Add Handwriting
PRODUCT_PACKAGES += \
    HandwritingService \
    libSDKRecognitionText

# add CarmodeStub apk
PRODUCT_PACKAGES += \
    CarmodeStub

# add MirrorLink tms
PRODUCT_PACKAGES += \
    audio.tms.default

# add MirrorLink libtmdisplay
PRODUCT_PACKAGES += \
    libtmdisplay

# add MirrorLink apk
PRODUCT_PACKAGES += \
    MirrorLink
    
# SamsungConnect
PRODUCT_PACKAGES +=  \
    SamsungConnect

# MirrorLink feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.mirrorlink_fw_level4.xml:system/etc/permissions/com.samsung.feature.mirrorlink_fw_level4.xml

# Add SFinder
PRODUCT_PACKAGES += \
    Finder_v7

# Add SFinder FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml
    
# Mate Agent
PRODUCT_PACKAGES += \
    MateAgent

# ProfessionalAudio
PRODUCT_PACKAGES += \
    SapaAudioConnectionService \
    SapaMonitor \
    libapa_jni \
    libapa \
    libapa_client \
    libjackservice \
    libapa_control \
    apaservice \
    apaclient \
    jackservice \
    jack_alsa_mixer.json \
    sapa_feature.xml \
	apa_settings.cfg \
    libjackshm \
    libjackserver \
    libjack \
    androidshmservice \
    jackd \
    jack_dummy \
    jack_alsa \
    jack_opensles \
    jack_loopback \
    in \
    out \
    jack_connect \
    jack_disconnect \
    jack_lsp \
    jack_showtime \
    jack_simple_client \
    jack_transport \
    libasound \
    libjansson \
    libglib-2.0 \
    libgthread-2.0 \
    libjackfakeplayer

#UPnP discover(for smart view)
PRODUCT_PACKAGES +=  \
    allshare \
    AllshareMediaShare \
    AllshareFileShare
    
# AppLinker
PRODUCT_PACKAGES += \
    AppLinker

# Add SmartCallProvider
PRODUCT_PACKAGES += \
	SmartCallProvider

# DRParser
PRODUCT_PACKAGES += \
    DRParser
	
# add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyApps_3xh	

# add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyAppsWidget_Phone_Dream

# Add Samsung Push Service	
PRODUCT_PACKAGES += \
	SPPPushClient_Prod		
	
# Add Removable Samsung Notes
PRODUCT_PACKAGES += \
    Notes_Removable

# Samsung Billing
PRODUCT_PACKAGES += \
    SamsungBilling

# Network Diagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic

# Radio Based Location
PRODUCT_PACKAGES += \
    RadioBasedLocation

# Samsung Pass
PRODUCT_PACKAGES += \
    SamsungPass

# Add Samsung KMS Agent
PRODUCT_PACKAGES += \
    libsem_jni \
    SKMSAgent

# Add eSE MW
PRODUCT_PACKAGES += \
    libspictrl

# Add SEM
PRODUCT_PACKAGES += \
    libsec_sem \
    sem_daemon \
    libsem_factoryjni \
    SEMFactoryApp

# Add SKPM
PRODUCT_PACKAGES += \
    libsec_skpm

# Add Galaxy Care
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Add GMS common property
include vendor/samsung/configs/jackpotlte_common/gms_jackpotlte_common.mk

# Add SafetyInformation
PRODUCT_PACKAGES += \
    SafetyInformation

# Add AODService (Always on display)
PRODUCT_PACKAGES += \
    AODService_v28

# Add Always on display System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.aodservice_v04.xml:system/etc/permissions/com.samsung.feature.aodservice_v04.xml

# Add SGames
PRODUCT_PACKAGES += \
    GameHome \
    GameTools_Dream
    
# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices \

# Add Upday
PRODUCT_PACKAGES += \
    Upday

# TuiService
PRODUCT_PACKAGES += \
    libTui \
    TuiService

# ADD VRSetupWizardStub
PRODUCT_PACKAGES += \
    VRSetupWizardStub

# ADD VRService
PRODUCT_PACKAGES += \
    RNB \
    RNBShell \
    com.samsung.feature.hmt.xml \
    oculus.crt

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES +=  \
    Omc

# SLocation
PRODUCT_COPY_FILES += \
vendor/samsung/external/gps/common/slocation/libsgeo.so:system/lib/slocation/libsgeo.so \
vendor/samsung/external/gps/common/slocation/libsgeo64.so:system/lib64/slocation/libsgeo.so \
frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v3.1
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_310.xml:system/etc/permissions/com.sec.feature.nsflp_level_310.xml

# Add Samsung TTS
PRODUCT_PACKAGES += SamsungTTS
#PRODUCT_PACKAGES += smt_ko_KR_f00
#PRODUCT_PACKAGES += smt_ko_KR_m00
PRODUCT_PACKAGES += smt_en_US_f02
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

# Add SmartFittingService
PRODUCT_PACKAGES += \
    SmartFittingService

# copy SideSync Daemon
PRODUCT_COPY_FILES += \
  applications/sources/apps/SideSync/ss_conn_daemon:system/bin/ss_conn_daemon

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast