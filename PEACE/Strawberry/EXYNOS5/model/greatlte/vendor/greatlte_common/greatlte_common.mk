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

# define google api level for google approval
include build/target/product/product_launched_with_n.mk

PRODUCT_BRAND := samsung
PRODUCT_MANUFACTURER := samsung

# define the default locales for phone device
include vendor/samsung/build/localelist/SecLocale_EUR_OPEN.mk

# Add Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

PU_RESOLUTION := 1440_2960
PRODUCT_PACKAGES += \
    $(call addpuQmgResource,bootsamsung.qmg)\
    $(call addpuQmgResource,bootsamsungloop.qmg)

PRODUCT_PACKAGES += \
    Bixby

# Add Encryption Animation
pu_image_path := vendor/samsung/external/puQmgResource/img/DreamLook
PRODUCT_COPY_FILES += \
    $(pu_image_path)/xx/1440_2960/crypt_bootsamsung.qmg:system/media/crypt_bootsamsung.qmg \
    $(pu_image_path)/xx/1440_2960/crypt_bootsamsungloop.qmg:system/media/crypt_bootsamsungloop.qmg

# Add LPM Animation
addlpmSpiResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

LPM_LOOK_TYPE := DreamLook
LPM_RESOLUTION := 1440_2960
PRODUCT_PACKAGES += \
    $(call addlpmSpiResource,lcd_density.txt)\
    $(call addlpmSpiResource,battery_fast_charging_global.spi)\
    $(call addlpmSpiResource,percentage.spi)\
    $(call addlpmSpiResource,battery_error.spi)\
    $(call addlpmSpiResource,battery_low.spi)\
    $(call addlpmSpiResource,dock_error_yellow.spi)\
    $(call addlpmSpiResource,battery_temperature_error.spi)\
    $(call addlpmSpiResource,battery_temperature_limit.spi)\
    $(call addlpmSpiResource,temperature_limit_usb.spi)\
    $(call addlpmSpiResource,battery_water_usb.spi)\
    $(call addlpmSpiResource,water_protection_usb.spi)\
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

# Add Power Down Animation
addpdQmgResource = $(if $(findstring $(1),$(PRODUCT_COPY_FILES)),,$(1))
SHUTDOWN_ANIM_REVISION := 2016
TARGET_RESOLUTION := 1440x2960
PRODUCT_PACKAGES += \
    $(call addpdQmgResource,shutdown.qmg) \
    $(call addpdQmgResource,shutdown_WH.qmg)
PRODUCT_PACKAGES += \
    BadgeProvider_N

# Add Samsung Experience Home
PRODUCT_PACKAGES += \
    TouchWizHome_2017

# Add SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv3.3 \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic

# Add Samsung Messages
PRODUCT_PACKAGES += \
    SamsungMessages_10.0 \
    SecTelephonyProvider

# Add LED notification icons
PRODUCT_PACKAGES += \
  LedCoverAppGreat
  
# Add SamsungInCallUI
PRODUCT_PACKAGES += \
  SamsungInCallUI

# Add SamsungTelephonyUI
PRODUCT_PACKAGES += \
    TelephonyUI

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts \
    SamsungDialer

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SamsungContactsProvider100
            
# Add SmartCallProvider
PRODUCT_PACKAGES += \
    SmartCallProvider \
    HiyaService

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81

#Add SamsungClipboard UI
PRODUCT_PACKAGES += \
    ClipboardUIService \
    ClipboardSaveService

# Add ClipboardEdge
PRODUCT_PACKAGES += \
    ClipboardEdge

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# Add SBrowser
PRODUCT_PACKAGES += \
    SBrowser_9.0_Removable

# Add SBrowserEdge
PRODUCT_PACKAGES += \
    SBrowserEdge

# Add Calculator and Clock
PRODUCT_PACKAGES += \
    SecCalculator_R \
    ClockPackage

# SPdfNote
PRODUCT_PACKAGES += \
    SPdfNote

# Adding WebManual
PRODUCT_PACKAGES += \
    WebManual

# Add ChromeCustomizations
PRODUCT_PACKAGES += \
    ChromeCustomizations

# Add SecHTMLViewer
PRODUCT_PACKAGES += \
    SecHTMLViewer

# Safety Care
PRODUCT_PACKAGES += \
    EmergencyLauncher \
    EmergencyModeService \
    EmergencyProvider

# for DataCreate
PRODUCT_PACKAGES += \
    AutomationTest_FB

# Add Calendar
PRODUCT_PACKAGES += \
    SamsungCalendar \
    SamsungCalendarProvider

# Add Settings
PRODUCT_PACKAGES +=  \
    SecSettingsIntelligence \
    SecSettings \
    SettingsReceiver

# Add PreloadInstaller
PRODUCT_PACKAGES += \
    PreloadInstaller

# DRParser
PRODUCT_PACKAGES += \
    DRParser

# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016
	
# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient
    
# Add SamsungWallpaper
PRODUCT_PACKAGES += \
    wallpaper-res

# SLocation
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/location/libsgeo/libsgeo.so:system/lib/slocation/libsgeo.so \
    vendor/samsung/frameworks/location/libsgeo/libsgeo64.so:system/lib64/slocation/libsgeo.so \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# Network Diagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic

# Add Samsung Positioning
PRODUCT_PACKAGES += \
    SamsungPositioning

# Add AdaptSound
PRODUCT_PACKAGES += \
    Hearingdro_V7_P

# Add SoundAlive
PRODUCT_PACKAGES += \
    SoundAlive_52 \
    -MusicFX

# Add My Files
PRODUCT_PACKAGES += \
    SecMyFiles2018

# Add Download Provider
PRODUCT_PACKAGES += \
    SecDownloadProvider

# SemExtendedFormat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml\
    libSEF.quram.vendor

#Add SAIV for prebuilt camera and bixby vision apps
PRODUCT_PACKAGES += \
    saiv \
    saiv.xml
    
# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome	

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder

# Add Upday
PRODUCT_PACKAGES += \
    Upday

# Samsung Galalxy Friends
PRODUCT_PACKAGES += \
    MateAgent 
   
# CMHProvider
PRODUCT_PACKAGES += \
    CMHProvider

# CMHServices
PRODUCT_PACKAGES += \
    IPService \
    FaceService \
    StoryService \
    MediaLearningPlatform

# Find My Mobile
PRODUCT_PACKAGES += \
    Fmm

# Remote Lock Manager
PRODUCT_PACKAGES += \
    Rlc

# Add SamsungPass
PRODUCT_PACKAGES += \
    SamsungPass

# Add SamsungPassAutofill
PRODUCT_PACKAGES += \
    SamsungPassAutofill_v1

# Dual IM
PRODUCT_PACKAGES += \
    DAAgent

# Add Cocktail QuickTools
PRODUCT_PACKAGES += \
    CocktailQuickTool

# Add Yahoo Edge
PRODUCT_PACKAGES += \
    YahooEdgeSports \
    YahooEdgeFinance

#Add NaverPanel
PRODUCT_PACKAGES += NaverV_N

#Add AppsEdgePanel
PRODUCT_PACKAGES += \
    AppsEdgePanel_v3.2

#Add TaskEdgePanel
PRODUCT_PACKAGES += \
    TaskEdgePanel_v3.2

#Add 2nd Screen CocktailBar
PRODUCT_PACKAGES += \
    CocktailBarService_v3.2

#Add 2nd Screen CocktailBar feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.cocktailpanel.xml:system/etc/permissions/com.sec.feature.cocktailpanel.xml

#Add 2nd Screen CocktailBar v3 feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.edge_v03.xml:system/etc/permissions/com.sec.feature.edge_v03.xml

# Add Samsung Camera packages
PRODUCT_PACKAGES += \
    SamsungCamera \
    FilterProvider \
    StickerFaceAR \
    StickerStamp \
    StickerWatermark \
    StickerProvider

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3

# Add StickerCenter
PRODUCT_PACKAGES += \
    StickerCenter

# Keyguard wallpaper updator
PRODUCT_PACKAGES += \
    KeyguardWallpaperUpdator    

# Live Wallpaper
PRODUCT_PACKAGES += \
    SecLiveWallpapersPicker

# Live wallpaper feature
PRODUCT_COPY_FILES += \
    applications/sources/wallpapers/LiveWallpaperPicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml    
	
# Add Iris Service packages
PRODUCT_PACKAGES += \
    SecIrisService

# Added Dictionary for Sec
PRODUCT_PACKAGES += \
    DictDiotekForSec

# add MirrorLink tms
PRODUCT_PACKAGES += \
    MirrorLink \
    libtmdisplay.mirrorlink.samsung \
    public.libraries-mirrorlink.samsung.txt \
    audio.tms.default \
    libdapjni

# add CarmodeStub apk
PRODUCT_PACKAGES += \
    CarmodeStub

# Send SOS Message
PRODUCT_PACKAGES += \
    SendHelpMessage

# Add Samsung SetupWizard
PRODUCT_PACKAGES += \
    SecSetupWizard_Global

# Add SetupWizard Script Player 
PRODUCT_PACKAGES +=  \
     SuwScriptPlayer 

# MobilePrint
PRODUCT_PACKAGES += \
    SPrintSpooler

# Add SafetyInformation
PRODUCT_PACKAGES += \
    SafetyInformation

 # Add Sim card manager
PRODUCT_PACKAGES += \
    SimSettingMgr   

# Accessib ility
PRODUCT_PACKAGES += \
    Accessibility \
    STalkback \
    -AdvSoundDetector2015 \
    -AccessControl_N \
    -AssistantMenu_N \
    -ColorAdjustment \
    -UniversalSwitch
    
# Add SCPM Client
PRODUCT_PACKAGES += \
    SCPMClient_N

# Add Samsung Billing
PRODUCT_PACKAGES += \
    SamsungBilling   

# Add Smart Switch Agent
PRODUCT_PACKAGES += \
    SmartSwitchAgent

# Add Smart Switch
PRODUCT_PACKAGES += \
    SmartSwitch
	
# Add SmartManager
PRODUCT_PACKAGES += \
    SmartManager_v6_DeviceSecurity \
    SmartManager_v5

# EasySetup
PRODUCT_PACKAGES += \
    EasySetup
# DLNA
PRODUCT_PACKAGES +=  \
    allshare \
    AllshareMediaShare

# FileShare
PRODUCT_PACKAGES +=  \
    AllshareFileShare
# Dual IM
PRODUCT_PACKAGES += \
    DAAgent

# KNOX_CONTAINER V3.0
ifeq ($(SEC_KNOX_CONTAINER_VERSION),$(filter $(SEC_KNOX_CONTAINER_VERSION), v30 v35 v40))
PRODUCT_PACKAGES += \
    KnoxCore \
    ContainerAgent3 \
    BBCAgent \
    SecureFolder
endif

# Biometrics
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

PRODUCT_PACKAGES += BioFaceService

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

# Add AODService (Always on display)
PRODUCT_PACKAGES += \
    AODService_v40 \
    libDigitalClockEncoder.aod.samsung \
    public.libraries-aod.samsung.txt

# Add Always on display System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.aodservice_v06.xml:system/etc/permissions/com.samsung.feature.aodservice_v06.xml

# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    SmartMirroring

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

# Add Self Motion Panorama Viewer
PRODUCT_PACKAGES += \
    SelfMotionPanoramaViewer

# Add SelectiveFocus Viewer
PRODUCT_PACKAGES += \
    SelectiveFocusViewer

# TuiService
PRODUCT_PACKAGES += \
    libTui \
    TuiService 

# eSE features
PRODUCT_PACKAGES += \
    sem_daemon \
    SEMFactoryApp

# DesktopModeUiService
PRODUCT_PACKAGES += \
    DesktopModeUiService

# Add ThemeCenter
PRODUCT_PACKAGES += \
    ThemeCenter
	
# Add Samsung Themes
PRODUCT_PACKAGES += \
	ThemeStore

# Add SGames
PRODUCT_PACKAGES += \
    GameHome \
    GameTools_Dream

# SVoice PLM
PRODUCT_PACKAGES += \
    SVoicePLM

# Add Samsung Email
PRODUCT_PACKAGES += \
    SecEmail_P \
    com.samsung.android.email.provider.xml

# Add DeviceSearch
PRODUCT_PACKAGES += \
    Finder

# Add SFinder FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml

# Add SFinder System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.galaxyfinder_v7.xml:system/etc/permissions/com.samsung.feature.galaxyfinder_v7.xml

PRODUCT_PACKAGES += \
    SMusicPicker

# Add ApexService
PRODUCT_PACKAGES += \
    ApexService

# Add SmartCapture
PRODUCT_PACKAGES += \
    SmartCapture

# Add EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

# AppLinker
PRODUCT_PACKAGES += \
    AppLinker
    
# Add WeatherWidget
PRODUCT_PACKAGES += \
   Weather_SEP10.1

# Adding MobileOffice Stub
PRODUCT_PACKAGES += \
    Excel_SamsungStub \
    PowerPoint_SamsungStub \
    Word_SamsungStub

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices

# add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyApps_OPEN
	
# Adding LinkedIn Stub
PRODUCT_PACKAGES += \
    LinkedIn_SamsungStub

#OverlayMagnifier feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.overlaymagnifier.xml:system/etc/permissions/com.sec.feature.overlaymagnifier.xml

#Add people edge notification feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.people_edge_notification.xml:system/etc/permissions/com.sec.feature.people_edge_notification.xml

# Flipboard Briefing
PRODUCT_PACKAGES += \
    FlipboardBriefing

# Add GMS common property
include vendor/samsung/configs/greatlte_common/gms_greatlte_common.mk

# Voice Recorder
PRODUCT_PACKAGES += \
    VoiceNote_5.0

# Samsung Notes (Non-deletable for SPen)
PRODUCT_PACKAGES += \
    Notes

# Add SHealth
PRODUCT_PACKAGES += \
    SHealth6 \
    HealthService

# Add GearManager
PRODUCT_PACKAGES += \
    GearManager

# add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# SamsungDeX Launcher
PRODUCT_PACKAGES += \
    KnoxDesktopLauncher

# Add DexCommunity
PRODUCT_PACKAGES += \
    DexCommunity
    
# Add PENUP
PRODUCT_PACKAGES += \
    PENUP

PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp

# Add Handwriting
PRODUCT_PACKAGES += \
    HandwritingService \
    libSDKRecognitionText.spensdk.samsung \
    public.libraries-spensdk.samsung.txt

#Add Rubin 23
PRODUCT_PACKAGES += \
    RubinVersion23

# Spen feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.spen_usp_level30.xml:system/etc/permissions/com.sec.feature.spen_usp_level30.xml

# Enables network use when in power save mode (SmartEye)
PRODUCT_COPY_FILES += \
    applications/par/idp/SmartEye/AirTranslate_P/MAIN/airviewdictionaryservice.xml:system/etc/sysconfig/airviewdictionaryservice.xml

# Add AirCommand feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.aircommand.xml:system/etc/permissions/com.sec.feature.aircommand.xml

# Add AirCommand
PRODUCT_PACKAGES += \
    AirCommand

# Add SmartCapture
PRODUCT_PACKAGES += \
    SmartCapture

# Add SmartEye(AirTranslate)
PRODUCT_PACKAGES += \
    SmartEye

# Add LiveDrawing
PRODUCT_PACKAGES += \
    LiveDrawing

# Add AirMagnify
PRODUCT_PACKAGES += \
    AirReadingGlass

# Add AirGlance
PRODUCT_PACKAGES += \
    Pentastic

# Add ContextProvider
PRODUCT_PACKAGES += \
    ContextProvider

# SmartFittingService
PRODUCT_PACKAGES += \
    SmartFittingService
	
# Add VideoEditor
PRODUCT_PACKAGES += \
    VideoEditorLite_Dream_N
	
# Add Story Video Editor
PRODUCT_PACKAGES += \
    StoryEditor_Dream_N
	
# Add SlowMotion
PRODUCT_PACKAGES += \
    SlowMotion_Star_N

# add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer

# Add VideoEditor
PRODUCT_PACKAGES += \
    videoeditor_sdk

# SamsungConnect
PRODUCT_PACKAGES +=  \
    SamsungConnect

PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/VideoEditor/Framework/v1/Lib64/libvesgraphicframework.videoeditor.samsung.so:system/lib64/libvesgraphicframework.videoeditor.samsung.so\
    vendor/samsung/frameworks/VideoEditor/Framework/v1/Lib64/libSoundAlive_SRC192_ver205a.so:system/lib64/libSoundAlive_SRC192_ver205a.so\
    vendor/samsung/frameworks/VideoEditor/Framework/v1/Lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so:system/lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so\
    vendor/samsung/frameworks/VideoEditor/Framework/v1/Lib64/LSI/libvesinterface.videoeditor.samsung.so:system/lib64/libvesinterface.videoeditor.samsung.so\
    vendor/samsung/frameworks/VideoEditor/Framework/v1/Lib64/LSI/libvesframework.videoeditor.samsung.so:system/lib64/libvesframework.videoeditor.samsung.so

# Add OCRService (MOCR)
PRODUCT_PACKAGES += \
    OCRServiceLite_1.4

# SFEffect
PRODUCT_PACKAGES += \
    sfeffect \
    sfeffect.xml \
    libSFEffect.fonteffect.samsung \
    libgnustl_shared \
    public.libraries-fonteffect.samsung.txt

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_Beyond

# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_Beyond

# Add GearVRService
PRODUCT_PACKAGES += \
    GearVRService

# Add OneDrive
PRODUCT_PACKAGES += \
    OneDrive_Samsung_v3

# Add KidsHome
PRODUCT_PACKAGES += \
    KidsHome_P \
    KidsHome_Installer	

# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v3.5
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_351.xml:system/etc/permissions/com.sec.feature.nsflp_level_351.xml