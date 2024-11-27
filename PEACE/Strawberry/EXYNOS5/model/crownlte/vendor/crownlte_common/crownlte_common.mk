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
include build/target/product/product_launched_with_o_mr1.mk

PRODUCT_BRAND := samsung
PRODUCT_MANUFACTURER := samsung

# Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

PU_RESOLUTION := 1440_2960
PRODUCT_PACKAGES += \
    $(call addpuQmgResource,bootsamsung.qmg)\
    $(call addpuQmgResource,bootsamsungloop.qmg)

# Encryption Animation
pu_image_path := vendor/samsung/external/puQmgResource/img/DreamLook
PRODUCT_COPY_FILES += \
    $(pu_image_path)/xx/1440_2960/crypt_bootsamsung.qmg:system/media/crypt_bootsamsung.qmg \
    $(pu_image_path)/xx/1440_2960/crypt_bootsamsungloop.qmg:system/media/crypt_bootsamsungloop.qmg

# LPM Animation
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

# Power Down Animation
addpdQmgResource = $(if $(findstring $(1),$(PRODUCT_COPY_FILES)),,$(1))
SHUTDOWN_ANIM_REVISION := 2016
TARGET_RESOLUTION := 1440x2960
PRODUCT_PACKAGES += \
    $(call addpdQmgResource,shutdown.qmg) \
    $(call addpdQmgResource,shutdown_WH.qmg)

# Add SoundAlive
PRODUCT_PACKAGES += \
    SoundAlive_52 \
    -MusicFX

# Add AdaptSound
PRODUCT_PACKAGES += \
    Hearingdro_V7

# Add SamsungInCallUI
PRODUCT_PACKAGES += \
  SamsungInCallUI

# Add SamsungTelephonyUI
PRODUCT_PACKAGES += \
    TelephonyUI

# Add SmartCallProvider
PRODUCT_PACKAGES += \
    SmartCallProvider \
    HiyaService

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts \
    SamsungDialer

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SamsungContactsProvider100

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81

# Add My Files
PRODUCT_PACKAGES += \
    SecMyFiles2018

# Add SHealth
PRODUCT_PACKAGES += \
    SHealth6 \
    HealthService

# DRParser
PRODUCT_PACKAGES += \
    DRParser

# Add Download Providers
PRODUCT_PACKAGES += \
    SecDownloadProvider

# SemExtendedFormat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml\
    libSEF.quram.vendor

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3

#Add SAIV for prebuilt camera and bixby vision apps
PRODUCT_PACKAGES += \
    saiv \
    saiv.xml

# Add bixby vision framework
PRODUCT_PACKAGES += \
    AIR2 \
    BixbyVisionFramework

# Add bixby vision app
PRODUCT_PACKAGES += \
    VisionIntelligence2

# Add Handwriting
PRODUCT_PACKAGES += \
    HandwritingService \
    libSDKRecognitionText

# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome
	
# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient
   
# Samsung Galalxy Friends
PRODUCT_PACKAGES += \
    MateAgent 
    

# Add Microsoft Office Stub
PRODUCT_PACKAGES += \
    Excel_SamsungStub \
    PowerPoint_SamsungStub \
    Word_SamsungStub

# Add LinkedIn Stub
PRODUCT_PACKAGES += \
    LinkedIn_SamsungStub

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices

# Add ClipboardEdge
PRODUCT_PACKAGES += \
    ClipboardEdge

# Add SBrowserEdge
PRODUCT_PACKAGES += \
    SBrowserEdge

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
#Add SystemUIDesktop
PRODUCT_PACKAGES += \
    SystemUIDesktop

#Add 2nd Screen CocktailBar v3 feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.edge_v03.xml:system/etc/permissions/com.sec.feature.edge_v03.xml

# Add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyApps_OPEN

# Add Galaxy Essential Widget
PRODUCT_PACKAGES += \
    GalaxyAppsWidget_Phone_Dream
   
# Add VisionCloudAgent
PRODUCT_PACKAGES += \
    VisionCloudAgent

# Add VisionProvider
PRODUCT_PACKAGES += \
    VisionProvider

# CMHProvider
PRODUCT_PACKAGES += \
    CMHProvider

# Find My Mobile
PRODUCT_PACKAGES += \
    Fmm
	
# CMHServices
PRODUCT_PACKAGES += \
    IPService \
    FaceService \
    StoryService \
    MediaLearningPlatform

# ADD GearVRService
PRODUCT_PACKAGES += \
    GearVRService

# Add SamsungPassAutofill
PRODUCT_PACKAGES += \
    SamsungPassAutofill_v1

# Add Samsung Camera packages
PRODUCT_PACKAGES += \
    SamsungCamera \
    FilterInstaller \
    FilterProvider \
    StickerFaceAR2 \
    StickerFaceAR3D2 \
    StickerStamp \
    StickerWatermark \
    StickerProvider \
    StickerFaceARFrame \
    SamsungAvatarAuthoring

# NlpHub
PRODUCT_PACKAGES += \
    NlpHub
		
# Add StickerCenter
PRODUCT_PACKAGES += \
    StickerCenter

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# Add Iris Service packages
PRODUCT_PACKAGES += \
    SecIrisService

# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv3.3 \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic

#Add SamsungClipboard UI
PRODUCT_PACKAGES += \
    ClipboardUIService \
    ClipboardSaveService

# Add Samsung Keyboard Beta
PRODUCT_PACKAGES += \
    SamsungIMEv3.3Tyme_Removable

# add MirrorLink tms
PRODUCT_PACKAGES += \
    MirrorLink \
    libtmdisplay.mirrorlink.samsung \
    public.libraries-mirrorlink.samsung.txt \
    audio.tms.default \
    libdapjni

PRODUCT_COPY_FILES += \
	frameworks/base/data/etc/com.samsung.feature.mirrorlink_fw_level8.xml:system/etc/permissions/com.samsung.feature.mirrorlink_fw_level8.xml

# Add Samsung Messages
PRODUCT_PACKAGES += \
    SamsungMessages_10.0 \
    Messaging_sticker_plugin \
    SecTelephonyProvider

# Send SOS Message
PRODUCT_PACKAGES += \
    SendHelpMessage

# Add Samsung SetupWizard
PRODUCT_PACKAGES += \
    SecSetupWizard_Global

# Add SetupWizard Script Player 
PRODUCT_PACKAGES +=  \
     SuwScriptPlayer 

# for DataCreate
PRODUCT_PACKAGES += \
    AutomationTest_FB

# Add Calendar
PRODUCT_PACKAGES += \
    SamsungCalendar \
    SamsungCalendarProvider

# MobilePrint
PRODUCT_PACKAGES += \
    SPrintSpooler

# Settings
PRODUCT_PACKAGES +=  \
    SecSettingsIntelligence \
    SecSettings \
    SettingsReceiver

# Safety Care
PRODUCT_PACKAGES += \
    EmergencyLauncher \
    EmergencyModeService \
    EmergencyProvider

# Add PreloadInstaller
PRODUCT_PACKAGES += \
    PreloadInstaller

# Added Dictionary for Sec
PRODUCT_PACKAGES += \
    DictDiotekForSec

 # Add Sim card manager
PRODUCT_PACKAGES += \
    SimSettingMgr   

# Accessibility
PRODUCT_PACKAGES += \
    Accessibility \
    STalkback \
    -AdvSoundDetector2015 \
    -AccessControl_N \
    -AssistantMenu_N \
    -ColorAdjustment \
    -UniversalSwitch

PRODUCT_PACKAGES += \
    TouchWizHome_2017

#Add SamsungClipboard UI
PRODUCT_PACKAGES += \
    ClipboardUIService \
    ClipboardSaveService

# Biometrics
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

PRODUCT_PACKAGES += BioFaceService

PRODUCT_PACKAGES += IntelligentBiometricsService

# Add SamsungCloud
PRODUCT_PACKAGES += \
    SamsungCloud_MediaSync
    
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

# Add Samsung Positioning
PRODUCT_PACKAGES += \
    SamsungPositioning

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder
	
# Network Diagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic
    
# Add Intelligent Proximity
PRODUCT_PACKAGES += \
    IpsGeofence

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

# SamsungConnect
PRODUCT_PACKAGES +=  \
    SamsungConnect

# SLocation
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/location/libsgeo/libsgeo.so:system/lib/slocation/libsgeo.so \
    vendor/samsung/frameworks/location/libsgeo/libsgeo64.so:system/lib64/slocation/libsgeo.so \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# KNOX-DLP
PRODUCT_PACKAGES += \
    SamsungDLPService

# Dual IM
PRODUCT_PACKAGES += \
    DAAgent

# KNOX_CONTAINER V3.0
ifeq ($(SEC_KNOX_CONTAINER_VERSION),$(filter $(SEC_KNOX_CONTAINER_VERSION), v30 v35 v40))
PRODUCT_PACKAGES += \
    KnoxCore \
    ContainerAgent3 \
    BBCAgent \
    KnoxBluetooth \
    SecureFolder \
    SecureFolderSetupPage \
    KnoxContainerDesktop
endif

# Add GMS common property
include vendor/samsung/configs/crownlte_common/gms_crownlte_common.mk

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v3.5
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_351.xml:system/etc/permissions/com.sec.feature.nsflp_level_351.xml

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

# SmartFittingService
PRODUCT_PACKAGES += \
    SmartFittingService

# add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer

# Add VideoEditor
PRODUCT_PACKAGES += \
    videoeditor_sdk

PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libvesgraphicframework.videoeditor.samsung.so:system/lib64/libvesgraphicframework.videoeditor.samsung.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libSoundAlive_SRC192_ver205a.so:system/lib64/libSoundAlive_SRC192_ver205a.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so:system/lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/LSI/libvesinterface.videoeditor.samsung.so:system/lib64/libvesinterface.videoeditor.samsung.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/LSI/libvesframework.videoeditor.samsung.so:system/lib64/libvesframework.videoeditor.samsung.so
	
# Add VideoEditor
PRODUCT_PACKAGES += \
    VideoEditorLite_Dream_N
	
# Add Story Video Editor
PRODUCT_PACKAGES += \
    StoryEditor_Dream_N
	
# Add SlowMotion
PRODUCT_PACKAGES += \
    SlowMotion_Star_N

# Add Motion Panorama Viewer
PRODUCT_PACKAGES += \
    MotionPanoramaViewer

# Add Self Motion Panorama Viewer
PRODUCT_PACKAGES += \
    SelfMotionPanoramaViewer

# Add LiveFocus Viewer
PRODUCT_PACKAGES += \
    DualOutFocusViewer

# Add Surround Shot Viewer
PRODUCT_PACKAGES += \
    Panorama360Viewer

# TuiService
PRODUCT_PACKAGES += \
    libTui \
    TuiService 

# eSE features
PRODUCT_PACKAGES += \
    sem_daemon \
    SEMFactoryApp

# Add Calculator and Clock
PRODUCT_PACKAGES += \
    SecCalculator_R \
    ClockPackage

# Add ShortcutBackupService
PRODUCT_PACKAGES += \
    ShortcutBackupService

# SamsungDeX Launcher
PRODUCT_PACKAGES += \
    KnoxDesktopLauncher

# Add DexCommunity
PRODUCT_PACKAGES += \
    DexCommunity

# DesktopModeUiService
PRODUCT_PACKAGES += \
    DesktopModeUiService

# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

# Add Linux On DeX
PRODUCT_PACKAGES += \
    nst

# Add ThemeCenter and UPSM Theme
PRODUCT_PACKAGES += \
    ThemeCenter \
    UPSMTheme

# Voice Recorder
PRODUCT_PACKAGES += \
    VoiceNote_5.0

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_Beyond

# Add SGames
PRODUCT_PACKAGES += \
    GameHome \
    GameTools_Dream

# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_Beyond

# Add watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub

# Add GearManager
PRODUCT_PACKAGES += \
    GearManager

# Add Samsung Notes
PRODUCT_PACKAGES += \
    Notes23

# add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

#Add Rubin 23
PRODUCT_PACKAGES += \
    RubinVersion23

# Add DeviceSearch
PRODUCT_PACKAGES += \
    Finder

# SVoice PLM
PRODUCT_PACKAGES += \
    SVoicePLM
	
# Add SFinder FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml

# Add SFinder System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.galaxyfinder_v7.xml:system/etc/permissions/com.samsung.feature.galaxyfinder_v7.xml

# Add Samsung Email
PRODUCT_PACKAGES += \
    SecEmail_P \
    com.samsung.android.email.provider.xml
	
# Add WeatherWidget
PRODUCT_PACKAGES += \
   Weather_SEP10.0

# Add ContextProvider
PRODUCT_PACKAGES += \
    ContextProvider
 
#Add SettingsBixby
PRODUCT_PACKAGES += \
    SettingsBixby

# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# GPUDriver
PRODUCT_PACKAGES += \
    GPUDriver-N9MaliG72_90

PRODUCT_PROPERTY_OVERRIDES += \
    ro.gfx.driver.0=com.samsung.gpudriver.N9MaliG72_90 \
    ro.hardware.egl=mali \
    ro.zygote.disable_gl_preload=true

# Add OneDrive
PRODUCT_PACKAGES += \
    OneDrive_Samsung_v3

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

PRODUCT_PACKAGES += \
    SMusicPicker

# FactoryBinary only (end of line)
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

# for FTL
FACTORY_BINARY_FOR_FTL_WIFI_FEATURE := disable

# factory common mk
include vendor/samsung/fac_vendor_common/fac_vendor_common.mk

endif #end of SEC_FACTORY_BUILD
###############################################################
# FactoryBinary only (end of line)
# PLEASE DO NOT ADD LINE BELOW

PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp

# Add LED notification icons
PRODUCT_PACKAGES += \
  LedCoverAppCrown

#OverlayMagnifier feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.overlaymagnifier.xml:system/etc/permissions/com.sec.feature.overlaymagnifier.xml

# Add Bixby
PRODUCT_PACKAGES += \
    Bixby

# Add SBrowser
PRODUCT_PACKAGES += \
    SBrowser_9.0_Removable

# Add PENUP
PRODUCT_PACKAGES += \
    PENUP

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
