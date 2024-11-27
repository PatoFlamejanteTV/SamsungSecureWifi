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

PRODUCT_BRAND := samsung
PRODUCT_MANUFACTURER := samsung

# define the default locales for phone device
include vendor/samsung/build/localelist/SecLocale_EUR_OPEN.mk

include build/target/product/product_launched_with_o.mk

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
pu_image_path := vendor/samsung/external/puQmgResource/img/DreamLook
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
TARGET_RESOLUTION := 1080x2220
PRODUCT_PACKAGES += \
    $(call addpdQmgResource,shutdown.qmg)

# Add GMS common property
include vendor/samsung/configs/a9y18qlte_common/gms_a9y18qlte_common.mk

PRODUCT_PACKAGES += \
    BadgeProvider_N

PRODUCT_PACKAGES += \
    TouchWizHome_2017

# Add SoundAliveFX Plug-in APK
PRODUCT_PACKAGES += \
    SoundAlive_52 \
    -MusicFX

# Add FingeprintManagerService
PRODUCT_PACKAGES += FingerprintService2
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

# Add AdaptSound
PRODUCT_PACKAGES += \
    Hearingdro_V7

# Add Samsung Camera packages
PRODUCT_PACKAGES += \
    SamsungCamera8 \
    ShootingModeProvider2 \
    FilterInstaller \
    FilterProvider \
    StickerFaceAR \
    StickerFaceAR3D \
    StickerStamp \
    StickerWatermark \
    StickerProvider \
    StickerCenter \
    CameraBokehService

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3

# Add Samsung Pass
PRODUCT_PACKAGES += \
    SamsungPass

# SamsungPassAutofill
PRODUCT_PACKAGES += \
    SamsungPassAutofill_v1

# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome

#add Bixby
PRODUCT_PACKAGES += \
    Bixby \
    VoiceWakeUp

# Add Bixby Plm Sync
PRODUCT_PACKAGES += \
    BixbyPLMSync

# Add Barge In New shared lib
PRODUCT_PACKAGES += \
    libBargeInEngine

# Add Samsung Barge In data
PRODUCT_PACKAGES += \
    BargeIn_Samsung_Camera

# Add KNOX_CONTAINER V3.0
ifeq ($(SEC_KNOX_CONTAINER_VERSION),$(filter $(SEC_KNOX_CONTAINER_VERSION), v30 v35 v40))
PRODUCT_PACKAGES += \
    KnoxCore \
    ContainerAgent3 \
    BBCAgent \
    KnoxBBCProvider \
    KnoxBluetooth \
    SecureFolder \
    SecureFolderSetupPage
endif

# KNOX-DLP
PRODUCT_PACKAGES += \
    SamsungDLPService

# Dual IM
PRODUCT_PACKAGES += \
    DAAgent

# Add Samsung Messages
PRODUCT_PACKAGES += \
    Messaging_Common_5.0 \
    Messaging_sticker_plugin \
    SecTelephonyProvider

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

# Add EasySetup
PRODUCT_PACKAGES +=  \
    EasySetup

# Add SamsungInCallUI
PRODUCT_PACKAGES += \
    SamsungInCallUI

# SamsungNetworkUI
PRODUCT_PACKAGES += \
    NetworkUI

# FindMyMobile
PRODUCT_PACKAGES += \
    Fmm

# Remote Mobile Manager
PRODUCT_PACKAGES += \
    Rlc

# HiddenNetworkSetting
PRODUCT_PACKAGES += \
    HiddenNetworkSetting

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SamsungContactsProvider90

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81

# DRParser
PRODUCT_PACKAGES += \
    DRParser

# Add SmartCallProvider
PRODUCT_PACKAGES += \
    SmartCallProvider \
    HiyaService

# Add Kies
PRODUCT_PACKAGES += \
    wssyncmlnps2

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices

# Add SmartManager
PRODUCT_PACKAGES += \
    SmartManager_v6_DeviceSecurity \
    SmartManager_v5

# Add DeviceHealthReporter
PRODUCT_PACKAGES += \
    DeviceHealthReporter

# if bin User - User - Ship, remove DHR
ifeq ($(SEC_FACTORY_BUILD),false)
ifeq ($(SEC_BUILD_OPTION_TYPE),user)
ifeq ($(SEC_BUILD_OPTION_PRODUCT_SHIP),true)
PRODUCT_PACKAGES += \
    -DeviceHealthReporter
endif
endif
endif

# Add ShortcutBackupService
PRODUCT_PACKAGES += \
    ShortcutBackupService

# Add Smart Switch Agent
PRODUCT_PACKAGES += \
    SmartSwitchAgent

# Network Diagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic

# Samsung Positioning
PRODUCT_PACKAGES += \
    SamsungPositioning

# Add Coreapps
PRODUCT_PACKAGES += \
    CoreApps_EOP

# Add SCPM Client
PRODUCT_PACKAGES += \
    SCPMClient_N

# MobilePrint
PRODUCT_PACKAGES += \
    SPrintSpooler

# Settings
PRODUCT_PACKAGES +=  \
    SecSettingsProvider \
    SecSettings \
    SettingsReceiver

# Accessibility
PRODUCT_PACKAGES += \
    STalkback \
    AdvSoundDetector2015 \
    AccessControl_N \
    AssistantMenu_N \
    ColorAdjustment \
    UniversalSwitch

# Add Sim card manager
PRODUCT_PACKAGES += \
    SimSettingMgr

# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    withTV \
    SmartMirroring

# MirrorLink
PRODUCT_PACKAGES += \
    MirrorLink \
    libtmdisplay \
    audio.tms.default \
    libdapjni

# add CarmodeStub apk
PRODUCT_PACKAGES += \
    CarmodeStub

PRODUCT_COPY_FILES += \
	frameworks/base/data/etc/com.samsung.feature.mirrorlink_fw_level8.xml:system/etc/permissions/com.samsung.feature.mirrorlink_fw_level8.xml

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

# Add PreloadInstaller
PRODUCT_PACKAGES += \
    PreloadInstaller

# Add Samsung SetupWizard
PRODUCT_PACKAGES += \
    SecSetupWizard_Global

# add ORG.
PRODUCT_PACKAGES += \
    SecCalculator_N_R \
    ClockPackage

# Adding MobileOffice Stub
PRODUCT_PACKAGES += \
    Excel_SamsungStub \
    PowerPoint_SamsungStub \
    Word_SamsungStub

# Adding LinkedIn Stub
PRODUCT_PACKAGES += \
    LinkedIn_SamsungStub

#FM Radio
PRODUCT_PACKAGES += \
    HybridRadio_O

# Add SBrowser
PRODUCT_PACKAGES += \
    SBrowser_7.4_Removable

# Adding SecDownloadProvider
PRODUCT_PACKAGES += \
    SecDownloadProvider

# Adding ChromeCustomizations
PRODUCT_PACKAGES += \
    ChromeCustomizations

# Adding SecHTMLViewer
PRODUCT_PACKAGES += \
	SecHTMLViewer

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder

#Add saiv
PRODUCT_PACKAGES += \
    saiv \
    saiv.xml

# Add Bixby Vision 2.0 framework
PRODUCT_PACKAGES += \
    BixbyVisionFramework

# Add bixby vision app
PRODUCT_PACKAGES += \
    VisionIntelligence2

# Add SemExtendedFormat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml

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

# Add Samsung Billing
PRODUCT_PACKAGES += \
    SamsungBilling

# for DataCreate
PRODUCT_PACKAGES += \
    AutomationTest_FB

# Add Calendar
PRODUCT_PACKAGES += \
    SamsungCalendar \
    SamsungCalendarProvider

# SLocation
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/location/libsgeo/libsgeo.so:system/lib/slocation/libsgeo.so \
    vendor/samsung/frameworks/location/libsgeo/libsgeo64.so:system/lib64/slocation/libsgeo.so \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# AllshareFramework
PRODUCT_PACKAGES +=  \
    allshare \
    AllshareMediaShare

# FileShare
PRODUCT_PACKAGES +=  \
    AllshareFileShare

# SamsungConnect
PRODUCT_PACKAGES +=  \
    SamsungConnect

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_WQHD_Star

# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_WQHD_Star

# Add SmartCapture
PRODUCT_PACKAGES += \
    SmartCapture

# Add DualOutFocus Viewer (LiveFocus Viewer)
PRODUCT_PACKAGES += \
    DualOutFocusViewer

# Add watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub

# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv3.0 \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic

#remove QCT&Google pkg
PRODUCT_PACKAGES += \
    -CMFileManager \
    -SoundRecorder \
    -VoiceDialer \
    -imssettings \
    -dialer \
    -messaging \
    -qti-logkit \
    -seccamsample \
    -VoicePrintDemo \
    -VoicePrintTest \
    -SnapdragonSVA \
    -CNESettings \
    -QSensorTest \
    -SecProtect \
    -RIDLClient \
    -colorservice \
    -improveTouchStudio \
    -QuickSearchBox \
    -com.quicinc.wipoweragent \
    -com.quicinc.wbcserviceapp \
    -access-qcom-logkit

# Add My Files
PRODUCT_PACKAGES += \
    SecMyFiles2017

# add Galaxy Apps
PRODUCT_PACKAGES += \
	GalaxyApps_OPEN

# Add Galaxy Apps widget
PRODUCT_PACKAGES += \
    GalaxyAppsWidget_Phone_Dream

#Add Rubin 2.0
PRODUCT_PACKAGES += \
    RubinVersion20

# Add SFinder
PRODUCT_PACKAGES += \
    Finder_v8

# Samsung Galaxy Friends
PRODUCT_PACKAGES += \
      MateAgent

# Add eSE MW
PRODUCT_PACKAGES += \
    libspictrl

# Add SEM
PRODUCT_PACKAGES += \
    libsec_sem \
    sem_daemon \
    SEMFactoryApp

# Add My Files
PRODUCT_PACKAGES += \
    SecMyFiles2017

# Voice Recorder
PRODUCT_PACKAGES += \
    VoiceNote_5.0

# Add SFinder FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml

# Add SFinder System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.galaxyfinder_v7.xml:system/etc/permissions/com.samsung.feature.galaxyfinder_v7.xml
# Add Handwriting
PRODUCT_PACKAGES += \
    HandwritingService \
    libSDKRecognitionText

# Add Samsung Email
PRODUCT_PACKAGES += \
    SecEmail_O \
    com.samsung.android.email.provider.xml

#Add SecGallery2015
PRODUCT_PACKAGES += \
    SecGallery2015 \
    com.sec.android.gallery3d.xml

# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# Add SamsungWallpaper
PRODUCT_PACKAGES += \
    wallpaper-res

# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient

# Add SamsungClipboard UI
PRODUCT_PACKAGES += \
    ClipboardUIService \
    ClipboardSaveService

# Keyguard wallpaper updator
PRODUCT_PACKAGES += \
    KeyguardWallpaperUpdator

# Live Wallpaper
PRODUCT_PACKAGES += \
    SecLiveWallpapersPicker

# Live wallpaper feature
PRODUCT_COPY_FILES += \
    applications/sources/wallpapers/LiveWallpaperPicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml

#OverlayMagnifier feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.overlaymagnifier.xml:system/etc/permissions/com.sec.feature.overlaymagnifier.xml

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

# Add SamsungCloud
PRODUCT_PACKAGES += \
    SamsungCloud

# Add Video Editor
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/lib_AnimationEngine_O.so:$(TARGET_COPY_OUT_VENDOR)/lib64/lib_AnimationEngine_O.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libSoundAlive_SRC192_ver205a.so:$(TARGET_COPY_OUT_VENDOR)/lib64/libSoundAlive_SRC192_ver205a.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so:$(TARGET_COPY_OUT_VENDOR)/lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libvideoeditor_jni_O_zf.so:$(TARGET_COPY_OUT_VENDOR)/lib64/libvideoeditor_jni_O_zf.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libvideoeditor_O_zf.so:$(TARGET_COPY_OUT_VENDOR)/lib64/libvideoeditor_O_zf.so

PRODUCT_PACKAGES += \
    videoeditor_sdk

# Add SlowMotion
PRODUCT_PACKAGES += \
    SlowMotion_Star_N

# add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer_SEP90

# Add ThemeStore
PRODUCT_PACKAGES += \
    ThemeStore

# Add ThemeCenter and UPSM Theme
PRODUCT_PACKAGES += \
    ThemeCenter \
    UPSMTheme

# Add SHealth
PRODUCT_PACKAGES += \
    SHealth5 \
    HealthService

PRODUCT_PACKAGES += \
    SMusicPicker

# Add Samsung Notes Removable
PRODUCT_PACKAGES += \
    Notes_Removable

# Add SGames
PRODUCT_PACKAGES += \
    GameHome \
    GameTools_Dream

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


# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v3.3
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_330.xml:system/etc/permissions/com.sec.feature.nsflp_level_330.xml

# Add AODService (Always on display)
PRODUCT_PACKAGES += \
    AODService_v30

# Add Always on display System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.aodservice_v05.xml:system/etc/permissions/com.samsung.feature.aodservice_v05.xml

# Add NlpHub
PRODUCT_PACKAGES += \
    NlpHub

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

# Add BioFaceService
PRODUCT_PACKAGES += BioFaceService

# Add Upday
PRODUCT_PACKAGES += \
    Upday

# Flipboard Briefing
PRODUCT_PACKAGES += \
    FlipboardBriefing

# Add WeatherWidget
PRODUCT_PACKAGES += \
   Weather_SEP9.0

# Add OneDrive
PRODUCT_PACKAGES += \
    OneDrive_Samsung_v3

# Add Dictionry for Sec
PRODUCT_PACKAGES += \
    DictDiotekForSec

# EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

# Add ContextProvider
PRODUCT_PACKAGES += \
    ContextProvider

# AppLinker
PRODUCT_PACKAGES += \
    AppLinker

# Add SVoicePLM
PRODUCT_PACKAGES += \
    SVoicePLM

# ADD VRService
PRODUCT_PACKAGES += \
    RNB \
    com.samsung.feature.hmt.xml \
    oculus.crt

# Add SystemUIBixby
PRODUCT_PACKAGES += \
    SystemUIBixby

# SmartFittingService
PRODUCT_PACKAGES += \
    SmartFittingService

# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp



# FactoryBinary only (end of line)
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
# Setting PRODUCT_PREBUILT_WEBVIEWCHROMIUM as yes will prevent building src
# use prebuilt WebViewGoogle.apk in GMS pack
PRODUCT_PACKAGES += WebViewGoogle
PRODUCT_PACKAGE_OVERLAYS += vendor/google/apps/WebViewGoogle/overlay
# Package installer for factory build
PRODUCT_PACKAGES += GooglePackageInstaller

# for FTL
FACTORY_BINARY_FOR_FTL_WIFI_FEATURE := disable

# factory common mk
include vendor/samsung/fac_vendor_common/fac_vendor_common.mk

endif
###############################################################
# FactoryBinary only (end of line)
# PLEASE DO NOT ADD LINE BELOW
###############################################################
