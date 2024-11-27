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
include build/target/product/product_launched_with_p.mk

PRODUCT_BRAND := samsung
PRODUCT_MANUFACTURER := samsung

# define the default locales for phone device
include vendor/samsung/build/localelist/SecLocale_EUR_OPEN.mk

# Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

PU_RESOLUTION := 1440_2560
PRODUCT_PACKAGES += \
    $(call addpuQmgResource,bootsamsung.qmg)\
    $(call addpuQmgResource,bootsamsungloop.qmg)

# Encryption Animation
pu_image_path := vendor/samsung/external/puQmgResource/img/DreamLook
PRODUCT_COPY_FILES += \
    $(pu_image_path)/xx/1440_2560/crypt_bootsamsung.qmg:system/media/crypt_bootsamsung.qmg \
    $(pu_image_path)/xx/1440_2560/crypt_bootsamsungloop.qmg:system/media/crypt_bootsamsungloop.qmg

# LPM Animation
addlpmSpiResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

LPM_LOOK_TYPE := BeyondLook
LPM_RESOLUTION := 1440_2560
PRODUCT_PACKAGES += \
    $(call addlpmSpiResource,lcd_density.txt)\
    $(call addlpmSpiResource,battery_fast_charging_global.spi)\
    $(call addlpmSpiResource,percentage.spi)\
    $(call addlpmSpiResource,battery_error.spi)\
    $(call addlpmSpiResource,battery_low.spi)\
    $(call addlpmSpiResource,dock_error_usb.spi)\
    $(call addlpmSpiResource,battery_temperature_error.spi)\
    $(call addlpmSpiResource,battery_temperature_limit.spi)\
    $(call addlpmSpiResource,temperature_limit_usb.spi)\
    $(call addlpmSpiResource,battery_water_usb.spi)\
    $(call addlpmSpiResource,water_protection_usb.spi)\
    $(call addlpmSpiResource,safety_timer_usb.spi)\
    $(call addlpmSpiResource,slow_charging_usb.spi)\
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
    SoundAlive_54 \
    -MusicFX

# Add AdaptSound
PRODUCT_PACKAGES += \
    Hearingdro_V7_P

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

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81

# Add My Files
PRODUCT_PACKAGES += \
    SecMyFiles2018
	
# Safety Care
PRODUCT_PACKAGES += \
    EmergencyLauncher \
    EmergencyModeService \
    EmergencyProvider

# DRParser
PRODUCT_PACKAGES += \
    DRParser

# Add Download Providers
PRODUCT_PACKAGES += \
    SecDownloadProvider

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3


# SemExtendedFormat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml \
    libSEF.quram.vendor

	
# Added ChromeCustomizations
PRODUCT_PACKAGES += \
    ChromeCustomizations
	
# adding SecHTMLViewer
PRODUCT_PACKAGES += \
    SecHTMLViewer

# Adding WebManual
PRODUCT_PACKAGES += \
     WebManual


#Add SAIV for prebuilt camera and bixby vision apps
PRODUCT_PACKAGES += \
    saiv \
    saiv.xml
	
# Add VisionProvider
PRODUCT_PACKAGES += \
    VisionProvider

# CMHProvider
PRODUCT_PACKAGES += \
    CMHProvider

# CMHServices
PRODUCT_PACKAGES += \
    IPService \
    FaceService \
    StoryService \
    MediaLearningPlatform

# Add Samsung Camera packages
PRODUCT_PACKAGES += \
    SamsungCamera \
    FilterProvider \
    StickerFaceAR2 \
    StickerFaceAR3D2 \
    StickerStamp \
    StickerWatermark \
    StickerProvider \
    StickerFaceARFrame \
    SamsungAvatarAuthoring2

# Add StickerCenter
PRODUCT_PACKAGES += \
    StickerCenter

# Add WallpaperPicker
PRODUCT_PACKAGES += \
    WallpaperPicker_2018

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# Add SamsungWallpaper
PRODUCT_PACKAGES += \
    wallpaper-res

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

# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv3.3 \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic

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
	
# DLNA
PRODUCT_PACKAGES +=  \
    allshare \
    AllshareMediaShare

# FileShare
PRODUCT_PACKAGES +=  \
    AllshareFileShare

PRODUCT_COPY_FILES += \
	frameworks/base/data/etc/com.samsung.feature.mirrorlink_fw_level8.xml:system/etc/permissions/com.samsung.feature.mirrorlink_fw_level8.xml

# add CarmodeStub apk
PRODUCT_PACKAGES += \
    CarmodeStub

# Add SBrowser
PRODUCT_PACKAGES += \
    SBrowser_9.0

# Add Samsung Messages
PRODUCT_PACKAGES += \
    SamsungMessages_10.0 \
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

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder

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

# Add PreloadInstaller
PRODUCT_PACKAGES += \
    PreloadInstaller

# Add SafetyInformation
PRODUCT_PACKAGES += \
    SafetyInformation

 # Add Sim card manager
PRODUCT_PACKAGES += \
    SimSettingMgr   

# Accessibility
PRODUCT_PACKAGES += \
    Accessibility \
    STalkback \
    AdvSoundDetector2015 \
    AssistantMenu_N \
    UniversalSwitch \
    -AccessControl_N \
    -ColorAdjustment

PRODUCT_PACKAGES += \
    BadgeProvider_N

PRODUCT_PACKAGES += \
    TouchWizHome_2017

#Add SamsungClipboard UI
PRODUCT_PACKAGES += \
    ClipboardUIService \
    ClipboardSaveService

# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient
   
# Biometrics
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

PRODUCT_PACKAGES += BioFaceService

PRODUCT_PACKAGES += IntelligentBiometricsService
    
# Add SCPM Client
PRODUCT_PACKAGES += \
    SCPMClient_N

# Add Samsung Billing
PRODUCT_PACKAGES += \
    SamsungBilling   

# Add Smart Switch Agent
PRODUCT_PACKAGES += \
    SmartSwitchAgent

# Add SmartManager
PRODUCT_PACKAGES += \
    SmartManager_v6_DeviceSecurity \
    SmartManager_v5

# Add Samsung Positioning
PRODUCT_PACKAGES += \
    SamsungPositioning
	
# Add Samsung Pass
PRODUCT_PACKAGES += \
    SamsungPass

# Network Diagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic
    
# Add Intelligent Proximity
PRODUCT_PACKAGES += \
    IpsGeofence

#Add SystemUIDesktop
PRODUCT_PACKAGES += \
    SystemUIDesktop

# Add SystemUIBixby2
PRODUCT_PACKAGES += \
    SystemUIBixby2

# Add Galaxy Essential Widget
PRODUCT_PACKAGES += \
    GalaxyAppsWidget_Phone_Dream

# Add SamsungPassAutofill
PRODUCT_PACKAGES += \
    SamsungPassAutofill_v1

# EasySetup
PRODUCT_PACKAGES += \
    EasySetup
    
# SLocation
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# eSE features
PRODUCT_PACKAGES += \
    sem_daemon \
    SEMFactoryApp

# KNOX_CONTAINER V3.0
ifeq ($(SEC_KNOX_CONTAINER_VERSION),$(filter $(SEC_KNOX_CONTAINER_VERSION), v30 v35 v40))
PRODUCT_PACKAGES += \
    KnoxCore \
    ContainerAgent3 \
    BBCAgent 
endif

# Add GMS common property
include vendor/samsung/configs/haechiy19_common/gms_haechiy19_common.mk

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v3.5
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_351.xml:system/etc/permissions/com.sec.feature.nsflp_level_351.xml

# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    SmartMirroring

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

# add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer

# Add VideoEditor
PRODUCT_PACKAGES += \
    videoeditor_sdk

PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/VideoEditor/Framework/v2/Lib64/libvesgraphicframework.videoeditor.samsung.so:system/lib64/libvesgraphicframework.videoeditor.samsung.so\
    vendor/samsung/frameworks/VideoEditor/Framework/v2/Lib64/libSoundAlive_SRC192_ver205a.so:system/lib64/libSoundAlive_SRC192_ver205a.so\
    vendor/samsung/frameworks/VideoEditor/Framework/v2/Lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so:system/lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so\
    vendor/samsung/frameworks/VideoEditor/Framework/v2/Lib64/LSI/libvesinterface.videoeditor.samsung.so:system/lib64/libvesinterface.videoeditor.samsung.so\
    vendor/samsung/frameworks/VideoEditor/Framework/v2/Lib64/LSI/libvesframework.videoeditor.samsung.so:system/lib64/libvesframework.videoeditor.samsung.so
	
# Add VideoEditor
PRODUCT_PACKAGES += \
    VideoEditorLite_Dream_N
	
# Add Story Video Editor
PRODUCT_PACKAGES += \
    StoryEditor_Dream_N
	
# Add SlowMotion
PRODUCT_PACKAGES += \
    SlowMotionVideoEditor

# Add Motion Panorama Viewer
PRODUCT_PACKAGES += \
    MotionPanoramaViewer

# Add Self Motion Panorama Viewer
PRODUCT_PACKAGES += \
    SelfMotionPanoramaViewer

# Add LiveFocus Viewer
PRODUCT_PACKAGES += \
    DualOutFocusViewer

# Add SelectiveFocus Viewer
PRODUCT_PACKAGES += \
    SelectiveFocusViewer

# Add Surround shot Viewer
PRODUCT_PACKAGES += \
    Panorama360Viewer

# TuiService
PRODUCT_PACKAGES += \
    libTui \
    TuiService 

# Add Linux On DeX
PRODUCT_PACKAGES += \
    nst

# Add Calculator and Clock
PRODUCT_PACKAGES += \
    SecCalculator_R \
    ClockPackage

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

# Voice Recorder
PRODUCT_PACKAGES += \
    VoiceNote_5.0

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_Beyond

# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_Beyond

# Add Samsung Notes
PRODUCT_PACKAGES += \
    Notes_Removable

# Add DeviceSearch
PRODUCT_PACKAGES += \
    Finder

# SVoice PLM
PRODUCT_PACKAGES += \
    SVoicePLM

# Add Samsung Email
PRODUCT_PACKAGES += \
    SecEmail_P \
    com.samsung.android.email.provider.xml

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

# Add WeatherWidget
PRODUCT_PACKAGES += \
   Weather_SEP10.1

# Add EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

# Add ContextProvider
PRODUCT_PACKAGES += \
    ContextProvider
  
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


# Add ARCore Stub apk
PRODUCT_PACKAGES += \
    ARCore

# Audio
-include vendor/samsung/frameworks/audio/audiopackages/AudioBuildProperties.mk

# Add ClockPack (LockScreen Clock)
PRODUCT_PACKAGES += \
    ClockPack_v40

# Add ClockPack System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.clockpack_v06.xml:system/etc/permissions/com.samsung.feature.clockpack_v06.xml

# AppLinker
PRODUCT_PACKAGES += \
    AppLinker

# Add Led Icon Editor
PRODUCT_PACKAGES += \
  	LedCoverAppStar

#OverlayMagnifier feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.overlaymagnifier.xml:system/etc/permissions/com.sec.feature.overlaymagnifier.xml

# Add Handwriting
PRODUCT_PACKAGES += \
    HandwritingService \
    libSDKRecognitionText.spensdk.samsung \
    public.libraries-spensdk.samsung.txt
	
# Add KidsHome
PRODUCT_PACKAGES += \
    KidsHome_Installer

# Flipboard Briefing
PRODUCT_PACKAGES += \
    FlipboardBriefing

# Add Upday
PRODUCT_PACKAGES += \
    Upday

#Add SettingsBixby
PRODUCT_PACKAGES += \
    SettingsBixby

# add Galaxy Beta Service for O OS Beta only.
PRODUCT_PACKAGES += \
    GalaxyBetaService
    
# Remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v10

# Samsung Experience Mobile Service
PRODUCT_PACKAGES += \
    -SamsungExperienceService

# Samsung Social
PRODUCT_PACKAGES += \
    -SamsungSocial

# SFEffect
PRODUCT_PACKAGES += \
    sfeffect \
    sfeffect.xml \
    libSFEffect.fonteffect.samsung \
    libgnustl_shared \
    public.libraries-fonteffect.samsung.txt

# GPUWatch service
PRODUCT_PACKAGES +=  \
   libhwobs
   
#remove Samsung Pay Framework
PRODUCT_PACKAGES += \
    -PaymentFramework	
	

PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp

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
