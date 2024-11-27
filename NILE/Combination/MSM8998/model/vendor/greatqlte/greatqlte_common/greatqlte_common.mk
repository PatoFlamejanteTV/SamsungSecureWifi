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

# Support build for 3rd party binary. Decide inclusion of IssManager and IssmanagerLite.
ifeq ($(SEC_BUILD_OPTION_ISS), full)
PRODUCT_PROPERTY_OVERRIDES += ro.release.iss=true
PRODUCT_PACKAGES += IssManager
endif

ifeq ($(SEC_BUILD_OPTION_ISSUE_TRACKER), true)
PRODUCT_PACKAGES += IssueTracker
endif

ifeq ($(SEC_BUILD_OPTION_ISS), lite)
PRODUCT_PROPERTY_OVERRIDES += ro.release.iss=true
PRODUCT_PACKAGES += IssManagerLite
endif

ifeq ($(SEC_BUILD_OPTION_BIGDATA_UT), true)
PRODUCT_PROPERTY_OVERRIDES += ro.hwparam.ut=true
endif

# USB Default setting
#PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
#	persist.sys.usb.config=mtp

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
pu_image_path := vendor/samsung/external/graphics/puQmgResource/img
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

# QMG resource load
# add file if not exist in PRODUCT_COPY_FILES
addpdQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))
SHUTDOWN_ANIM_REVISION := 2016
TARGET_RESOLUTION := 1440x2960
PRODUCT_PACKAGES += \
    $(call addpdQmgResource,shutdown.qmg)\
    $(call addpdQmgResource,shutdown.qmg.cover)\
    $(call addpdQmgResource,shutdownloop.qmg)\
    $(call addpdQmgResource,SPR_shutdown.qmg)\
    $(call addpdQmgResource,shutdownafter.qmg)

# Samsung-Framework
# Multi-Screen
-include vendor/samsung/frameworks/samsung-framework/samsung-framework.mk

# Add Barge In New shared lib
PRODUCT_PACKAGES += \
  libBargeInEngine

# Add Samsung Barge In data
PRODUCT_PACKAGES += \
  BargeIn_Samsung_Camera

#Add semextendedformat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml

#Add saiv
PRODUCT_PACKAGES += \
    saiv \
    saiv.xml

# ADD AIR SERVICE
PRODUCT_PACKAGES += \
    visiond

# ADD AIR
PRODUCT_PACKAGES += \
    libair \
    libairservice \
    libclassifierprocessing \
    libHprVisualEffect \
    libinfomanager \
    libISOCREngine \
    libocrprocessing \
    libqrprocessing \
    libroiprocessing \
    libSceneText \
    libSSVILibs \
    libstrprocessing \
    libvfprocessing \
    libWineDetection

# Add VisionIntelligence
PRODUCT_PACKAGES += \
	VisionIntelligence

# Add Selective Focus Viewer
PRODUCT_PACKAGES += \
	SelectiveFocusViewer

# Add Dual OutFocus Viewer
PRODUCT_PACKAGES += \
	DualOutFocusViewer

# Add Interactive Shot Viewer
PRODUCT_PACKAGES += \
    InteractivePanoramaViewer_WQHD

# Add Self Motion Panorama Viewer
PRODUCT_PACKAGES += \
    SelfMotionPanoramaViewer

# Add Motion Panorama Viewer
PRODUCT_PACKAGES += \
	MotionPanoramaViewer

# Add Surround Shot Viewer
PRODUCT_PACKAGES += \
	Panorama360Viewer

# Knox Desktop Launcher
PRODUCT_PACKAGES += \
    KnoxDesktopLauncher

# DesktopModeUiService
PRODUCT_PACKAGES += \
    DesktopModeUiService

################################################################################################
## 2. SEC optional packages
## Optional Samsung packages
################################################################################################
# Add ContextProvider
PRODUCT_PACKAGES += \
	ContextProvider

PRODUCT_PACKAGES += \
    BadgeProvider_N

PRODUCT_PACKAGES += \
    TouchWizHome_2017 \
    -Protips

# Flipboard Briefing
PRODUCT_PACKAGES += \
    FlipboardBriefing

# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

# Settings
PRODUCT_PACKAGES +=  \
    PreloadInstaller \
    SecSetupWizard2015 \
    SecSettingsProvider2 \
    SecSettings2 \
    SettingsReceiver

# Add Sim card manager
PRODUCT_PACKAGES += \
    SimSettingMgr

# Add Clock
PRODUCT_PACKAGES += \
    -DeskClock \
    ClockPackage_N

# add ORG.
PRODUCT_PACKAGES += \
    SecCalculator_N_R

#Add SecGallery2015
PRODUCT_PACKAGES += \
    SecGallery2015 \
    com.sec.android.gallery3d.xml

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# Find My Mobile
PRODUCT_PACKAGES += \
    Fmm

# Add Virtual Screen Framework Feature
PRODUCT_PACKAGES += \
    com.samsung.feature.virtualscreen.xml

# Accessibility
PRODUCT_PACKAGES += \
    STalkback \
    AdvSoundDetector2015 \
    AccessControl_N \
    AssistantMenu_N \
    ColorBlind_N \
    UniversalSwitch

# Add SafetyInformation
PRODUCT_PACKAGES += \
    SafetyInformation

# SamsungInCallUI
PRODUCT_PACKAGES += \
  SamsungInCallUI

# SamsungNetworkUI
PRODUCT_PACKAGES += \
    NetworkUI

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SamsungContactsProvider85

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81

# Add SmartCallProvider
PRODUCT_PACKAGES += \
    SmartCallProvider
    
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

# Add Samsung Pay Stub
PRODUCT_PACKAGES += \
    SamsungPayStub

# Add Email
PRODUCT_PACKAGES += \
	SecEmail_N \
	com.samsung.android.email.provider.xml

PRODUCT_PACKAGES += \
    prepare_param.sh

# Voice Recorder for Dream
PRODUCT_PACKAGES += \
    VoiceNote_5.0_Task

# Safety Care
PRODUCT_PACKAGES += \
	EmergencyLauncher \
	EmergencyModeService \
	EmergencyProvider

# Add Smart Switch Agent
PRODUCT_PACKAGES += \
    SmartSwitchAgent

# Add Smart Switch
PRODUCT_PACKAGES += \
    SmartSwitch

# Add Kies
PRODUCT_PACKAGES += \
    wssyncmlnps2


# Add Cocktail QuickTools
PRODUCT_PACKAGES += \
    CocktailQuickTool

# Add Yahoo Edge
PRODUCT_PACKAGES += \
    YahooEdgeSports \
    YahooEdgeFinance

#Add AppsEdgePanel
PRODUCT_PACKAGES += AppsEdgePanel_v3.2

#Add TaskEdgePanel
PRODUCT_PACKAGES += TaskEdgePanel_v3.2

#Add 2nd Screen CocktailBar
PRODUCT_PACKAGES += CocktailBarService_v3.2

#Add PeopleStripe
PRODUCT_PACKAGES += PeopleStripe

#Add 2nd Screen CocktailBar feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.cocktailpanel.xml:system/etc/permissions/com.sec.feature.cocktailpanel.xml

#Add 2nd Screen CocktailBar v3 feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.edge_v03.xml:system/etc/permissions/com.sec.feature.edge_v03.xml

#Add people edge notification feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.people_edge_notification.xml:system/etc/permissions/com.sec.feature.people_edge_notification.xml

# Music AutoRecommandation(SemAudioThumbnail)
PRODUCT_PACKAGES += \
	libsmat \
	SemAudioThumbnail

PRODUCT_PACKAGES += \
	libsmata \
	smatlib

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
    StickerFaceAR3D

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3

# Add Iris Service packages
PRODUCT_PACKAGES += \
    SecIrisService

# add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyApps_3xh

# add Galaxy Apps
PRODUCT_PACKAGES += \
	GalaxyAppsWidget_Phone_Dream

# Add Samsung Push Service
PRODUCT_PACKAGES += \
	SPPPushClient_Prod
PRODUCT_COPY_FILES += \
	applications/par/services/SPPPushClient/samsungpushservice.xml:system/etc/sysconfig/samsungpushservice.xml 	
	
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
    EnhanceService \
    StoryService \
    MediaLearningPlatform
	
# Add EasySetup
PRODUCT_PACKAGES += \
    EasySetup

#Add My files
PRODUCT_PACKAGES += \
    SecMyFiles2017

# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    WfdBroker \
    withTV \
    SmartMirroring

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

# Add CloudGateway
PRODUCT_PACKAGES += \
    CloudGateway2017

PRODUCT_PACKAGES += \
    SMusicPicker

# Add FingeprintManagerService
PRODUCT_PACKAGES += FingerprintService2
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

# Add BioFaceService
PRODUCT_PACKAGES += BioFaceService

# SLocation
PRODUCT_COPY_FILES += \
	vendor/samsung/external/gps/common/slocation/libsgeo64.so:system/lib64/slocation/libsgeo.so
PRODUCT_COPY_FILES += \
	frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml
PRODUCT_COPY_FILES += \
	vendor/samsung/external/gps/common/slocation/libsgeo.so:system/lib/slocation/libsgeo.so
PRODUCT_PACKAGES += \
	SLocation

# Network Diagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic

# Radio Based Location
PRODUCT_PACKAGES += \
    RadioBasedLocation

# add CarmodeStub apk
PRODUCT_PACKAGES += \
    CarmodeStub

# Add Hearingdro(Adapt Sound6.0)
PRODUCT_PACKAGES += \
	Hearingdro_V6

# EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

# ADD VRSetupWizardStub
PRODUCT_PACKAGES += \
    VRSetupWizardStub

# Add SoundAliveFX Plug-in APK (new version : SoundAlive_52)
PRODUCT_PACKAGES += \
    SoundAlive_52 \
    -MusicFX

# ADD VRService
PRODUCT_PACKAGES += \
    RNB \
    RNBShell \
    com.samsung.feature.hmt.xml \
    oculus.crt
	
# DRParser
PRODUCT_PACKAGES += \
    DRParser

# Verizon Messaging
PRODUCT_PACKAGES += \
    vzmsg_default.xml

# Add SNS
PRODUCT_PACKAGES += \
    SNS_v2_N \
    SnsImageCache_N

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_WQHD_Great

# Add watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub
	
# Add GearManager
PRODUCT_PACKAGES += \
    GearManager

# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_WQHD_Dream
	
# Add Download apks
PRODUCT_PACKAGES += \
    SecDownloadProvider

# Add SCPM Client
PRODUCT_PACKAGES += \
    SCPMClient_N

# Add SmartManager
PRODUCT_PACKAGES += \
    SmartManager_v5_DeviceSecurity \
    SmartManager_v5

# Add DeviceHealthReporter
PRODUCT_PACKAGES += \
    DeviceHealthReporter

# Samsung Messaging
PRODUCT_PACKAGES += \
    Messaging_SEP81 \
    MsgCommService \
    SecTelephonyProvider_Epic

#    Messaging_Common \
#    MsgCommService \
#    SecTelephonyProvider_Epic \
#    msgwrapper_Fun

# Remove google app
PRODUCT_PACKAGES += \
    -messaging

# Send SOS Message
PRODUCT_PACKAGES += \
    SendHelpMessage

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
    libgthread-2.0

# Add WeatherWidget
PRODUCT_PACKAGES += \
   Weather2017_SEP8.5 \
   WeatherWidget2017_SEP8.5
   
# SPdfNote
PRODUCT_PACKAGES += \
    SPdfNote
    
# add MirrorLink tms
PRODUCT_PACKAGES += \
    audio.tms.default

# add MirrorLink apk
PRODUCT_PACKAGES += \
    MirrorLink

# TIMA add DownloadableKeystore apk
PRODUCT_PACKAGES += \
    DownloadableKeystore

# add MirrorLink apk
PRODUCT_PACKAGES += \
    libtmdisplay

# MirrorLink feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.mirrorlink_fw_level4.xml:system/etc/permissions/com.samsung.feature.mirrorlink_fw_level4.xml

# DLNA
PRODUCT_PACKAGES +=  \
    allshare \
    AllshareMediaShare
# Only VZW model include - AllshareMediaServer

# FileShare
PRODUCT_PACKAGES +=  \
    AllshareFileShare \
    SamsungConnect

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
#PRODUCT_PACKAGES += smt_addpack_T

# Add Dictionary(Updatable)
PRODUCT_PACKAGES += \
    DictDiotek_update

# add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer_Phone

# AppLinker
PRODUCT_PACKAGES += \
    AppLinker

# Add Video Editor
 PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/VideoEditor/Great/Lib64/lib_AnimationEngine_N.so:system/vendor/lib64/lib_AnimationEngine_N.so\
    vendor/samsung/frameworks/VideoEditor/Great/Lib64/lib_Samsung_DJ_Effect.so:system/vendor/lib64/lib_Samsung_DJ_Effect.so\
    vendor/samsung/frameworks/VideoEditor/Great/Lib64/libdmcFaceEngine.so:system/vendor/lib64/libdmcFaceEngine.so\
    vendor/samsung/frameworks/VideoEditor/Great/Lib64/libSoundAlive_SRC192_ver205a.so:system/vendor/lib64/libSoundAlive_SRC192_ver205a.so\
    vendor/samsung/frameworks/VideoEditor/Great/Lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so:system/vendor/lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so\
    vendor/samsung/frameworks/VideoEditor/Great/Lib64/libvideoeditor_jni_N_zf.so:system/vendor/lib64/libvideoeditor_jni_N_zf.so\
    vendor/samsung/frameworks/VideoEditor/Great/Lib64/libvideoeditor_N_zf.so:system/vendor/lib64/libvideoeditor_N_zf.so

# Add Samsung KMS Agent
PRODUCT_PACKAGES += \
    libsem_jni \
    libsem_factoryjni \
    SKMSAgent

# Add eSE MW
PRODUCT_PACKAGES += \
    libspictrl

# Add SEM
PRODUCT_PACKAGES += \
    libsec_sem \
    sem_daemon \
    SEMFactoryApp

# Add SKPM
PRODUCT_PACKAGES += \
    libsec_skpm

# Add MediaProvider
PRODUCT_PACKAGES += \
    SecMediaProvider

# Add CoreApps
PRODUCT_PACKAGES += \
   CoreApps_SDK_2017 \
   LinkSharing_v34
   
# Add SBrowser apk removable
PRODUCT_PACKAGES += \
    SBrowser_6.0_Removable \
    browser.xml \
    SecHTMLViewer \
    -Browser2

# Add SBrowserEdge
PRODUCT_PACKAGES += \
    SBrowserEdge

# Adding ChromeCustomizations
PRODUCT_PACKAGES += \
    ChromeCustomizations	
	
# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv6 \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic

# MobilePrint
PRODUCT_PACKAGES += \
    SPrintSpooler7

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder

#add Rubin 1.5 version
PRODUCT_PACKAGES += \
    RubinVersion15

#add Bixby
PRODUCT_PACKAGES += \
    Bixby

#add SVoice
PRODUCT_PACKAGES += \
    SVoicePLM \
    VoiceWakeUp

#add VoiceServiceFramework
PRODUCT_PACKAGES += \
    VoiceServiceFramework
	
# Add SmartCapture
PRODUCT_PACKAGES += \
    SmartCapture

# SamsungBilling
PRODUCT_PACKAGES += \
    SamsungBilling
	
# Add Samsung Cloud 2.5
PRODUCT_PACKAGES += \
	SamsungCloudGreat

# Add SHealth
PRODUCT_PACKAGES += \
    SHealth5 \
    HealthService

# Add VideoEditor
PRODUCT_PACKAGES += \
	VideoEditorLite_Dream_N
	
# Add SlowMotion
PRODUCT_PACKAGES += \
	SlowMotion_Dream_N
	
# Add Highlight Player
PRODUCT_PACKAGES += \
	StoryEditor_Dream_N

# Add ApexService
PRODUCT_PACKAGES += \
    ApexService

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices \
    -FBInstagram_stub \
    -FBMessenger_stub

# Add WhatsAppDownloader
PRODUCT_PACKAGES += \
    -WhatsAppDownloader

# Add Beamservice
PRODUCT_PACKAGES += \
    BeamService

#remove QCT&Google pkg
PRODUCT_PACKAGES += \
  -VoiceDialer \
  -SoundRecorder \
  -QSensorTest \
  -SoundRecorder \
  -SecProtect \
  -RIDLClient \
  -colorservice \
  -imssettings \
  -qti-logkit \
  -Dialer \
  -Csm \
  -com.quicinc.wipoweragent \
  -com.quicinc.wbcserviceapp \
  -access-qcom-logkit

# Add Samsung EGL extensions
# for VR
PRODUCT_PACKAGES += \
	libSEC_EGL

# Adding MobileOffice Stub
PRODUCT_PACKAGES += \
    Excel_SamsungStub \
    PowerPoint_SamsungStub \
    Word_SamsungStub

# Adding LinkedIn Stub
PRODUCT_PACKAGES += \
    LinkedIn_SamsungStub

# Add SFinder
PRODUCT_PACKAGES += \
    Finder_v7

# Add SFinder FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml

# Add SFinder System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.galaxyfinder_v7.xml:system/etc/permissions/com.samsung.feature.galaxyfinder_v7.xml

# Add ThemeCenter and UPSM Theme
PRODUCT_PACKAGES += \
    ThemeCenter \
    UPSMTheme
    
# Add StickerCenter
PRODUCT_PACKAGES += \
    StickerCenter
    
# Add Samsung Themes for Great
PRODUCT_PACKAGES += \
    SamsungThemes_v4

# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v3.1_GREAT
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_310.xml:system/etc/permissions/com.sec.feature.nsflp_level_310.xml

# Add AODService (Always on display)
PRODUCT_PACKAGES += \
    AODService_v28

# Add Always on display System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.aodservice_v04.xml:system/etc/permissions/com.samsung.feature.aodservice_v04.xml

PRODUCT_PACKAGES += \
    videoeditor_sdk

# Add SGames
PRODUCT_PACKAGES += \
    GameHome_Flumen \
    GameTools_Dream

# Add Handwriting
PRODUCT_PACKAGES += \
	HandwritingService \
	libSDKRecognitionText

# KNOX-DLP
PRODUCT_PACKAGES += \
    SamsungDLPService
    
# dual IM
PRODUCT_PACKAGES += \
    DAAgent    	
	
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

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

# Add LED notification icons
PRODUCT_PACKAGES += \
  LedCoverAppDream

# Add SmartFittingService
PRODUCT_PACKAGES += \
    SmartFittingService

# Add hdr flag
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.hdr_capable.xml:system/etc/permissions/com.samsung.feature.hdr_capable.xml

# Add Samsung Notes
PRODUCT_PACKAGES += Notes

# ADD VRSetupWizardStub
PRODUCT_PACKAGES += \
    VRSetupWizardStub

# ADD VRService
PRODUCT_PACKAGES += \
    RNB \
    RNBShell \
    com.samsung.feature.hmt.xml \
    oculus.crt

# ADD VisionCloudAgent
PRODUCT_PACKAGES += \
    VisionCloudAgent

# Add SamsungPass
PRODUCT_PACKAGES += \
    SamsungPass_1.3

# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome

# Mate Agent
PRODUCT_PACKAGES += \
    MateAgent

# copy SideSync Daemon
PRODUCT_COPY_FILES += \
  applications/sources/apps/SideSync/ss_conn_daemon:system/bin/ss_conn_daemon \
  applications/sources/apps/SideSync/ss_kbservice_daemon:system/bin/ss_kbservice_daemon


# Live Wallpaper
PRODUCT_PACKAGES += \
    SecLiveWallpapersPicker

# Live wallpaper feature
PRODUCT_COPY_FILES += \
    applications/sources/wallpapers/LiveWallpaperPicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml

# Add ClipboardEdge
PRODUCT_PACKAGES += \
    ClipboardEdge

# Keyguard wallpaper updator
PRODUCT_PACKAGES += \
    KeyguardWallpaperUpdator

# Add SVC Agent
PRODUCT_PACKAGES += \
    SVCAgent

# OneDrive
PRODUCT_PACKAGES += \
     OneDrive_Samsung_v3
     
# Add SmartEye(AirTranslate)
PRODUCT_PACKAGES += \
    SmartEye
PRODUCT_COPY_FILES += \
    applications/par/services/AirTranslate/airviewdictionaryservice.xml:system/etc/sysconfig/airviewdictionaryservice.xml

# Add LiveDrawing
PRODUCT_PACKAGES += \
    LiveDrawing
    
# Add AirMagnify
PRODUCT_PACKAGES += \
    AirReadingGlass
    
# Add AirGlance
PRODUCT_PACKAGES += \
    Pentastic

# ADD SPenHub
PRODUCT_PACKAGES += \
    SpenHub

#Add AirCommand
PRODUCT_PACKAGES += \
    AirCommand
    
# SpenKeeper
PRODUCT_PACKAGES += \
    SPenKeeper

# PEN.UP
PRODUCT_PACKAGES += \
    PENUP_Removable

# UnifiedProfile 1.1
PRODUCT_PACKAGES += \
    UnifiedProfileGreat

# Add OCRService (MOCR)
PRODUCT_PACKAGES += \
    OCRServiceLite_1.3

# Add TimeZoneUpdater
PRODUCT_PACKAGES += \
    TimeZoneUpdater

# Add DeviceQualityAgent
PRODUCT_PACKAGES += \
    DeviceQualityAgent

# Add SamsungWallpaper
PRODUCT_PACKAGES += \
    wallpaper-res

# Add GMS common property
include vendor/samsung/configs/greatqlte_common/gms_greatqlte_common.mk
    
# GPUDriver
PRODUCT_PACKAGES += \
    GPUDriver-N8Adreno540_71

PRODUCT_PROPERTY_OVERRIDES += \
	ro.gfx.driver.0=com.samsung.gpudriver.N8Adreno540_71 \
	ro.hardware.egl=adreno

# TuiService
PRODUCT_PACKAGES += \
    libTui \
    TuiService
