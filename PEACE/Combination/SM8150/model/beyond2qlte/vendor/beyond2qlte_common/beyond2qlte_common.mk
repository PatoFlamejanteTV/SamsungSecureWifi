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

# 'SEC_COMMON_DEVICE' should be used for below properties only.
# If SEC_COMMON_DEVICE is not set, they use 'TARGET_DEVICE' by default.
# fingerprint, ro.product.device, ro.product.vendor.device
SEC_COMMON_DEVICE := beyond2q

include build/target/product/product_launched_with_p.mk

# define the default locales for phone device
include vendor/samsung/build/localelist/SecLocale_EUR_OPEN.mk

# Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

PU_RESOLUTION := 1440_3040
PRODUCT_PACKAGES += \
    $(call addpuQmgResource,bootsamsung.qmg)\
    $(call addpuQmgResource,bootsamsungloop.qmg)

# Encryption Animation
pu_image_path := vendor/samsung/external/puQmgResource/img/DreamLook
PRODUCT_COPY_FILES += \
    $(pu_image_path)/xx/1440_3040/crypt_bootsamsung.qmg:system/media/crypt_bootsamsung.qmg \
    $(pu_image_path)/xx/1440_3040/crypt_bootsamsungloop.qmg:system/media/crypt_bootsamsungloop.qmg

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
    
# Support build for 3rd party binary. Decide inclusion of IssManager and IssmanagerLite.
ifeq ($(SEC_BUILD_OPTION_ISS), full)
PRODUCT_PROPERTY_OVERRIDES += ro.release.iss=true
PRODUCT_PACKAGES += IssManager
endif

ifeq ($(SEC_BUILD_OPTION_ISS), lite)
PRODUCT_PROPERTY_OVERRIDES += ro.release.iss=true
PRODUCT_PACKAGES += IssManagerLite
endif

# Add GMS common property
include vendor/samsung/configs/beyond2qlte_common/gms_beyond2qlte_common.mk

# Add SamsungInCallUI
PRODUCT_PACKAGES += \
  SamsungInCallUI

# Add SHealth
PRODUCT_PACKAGES += \
    SHealth6 \
    HealthService

# Add DeviceSearch
PRODUCT_PACKAGES += \
    Finder

# Add Led Icon Editor
PRODUCT_PACKAGES += \
  	LedBackCoverAppStar


# Add DeviceSearch FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml

# Add DeviceSearch System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.galaxyfinder_v7.xml:system/etc/permissions/com.samsung.feature.galaxyfinder_v7.xml

# Add SamsungTelephonyUI
PRODUCT_PACKAGES += \
    TelephonyUI

# Add SmartCallProvider
PRODUCT_PACKAGES += \
    SmartCallProvider \
    HiyaService

# Biometrics
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

PRODUCT_PACKAGES += BioFaceService

PRODUCT_PACKAGES += BiometricSetting

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

# DRParser
PRODUCT_PACKAGES += \
    DRParser

# Add Download Providers
PRODUCT_PACKAGES += \
    SecDownloadProvider

# Add My Files
PRODUCT_PACKAGES += \
    SecMyFiles2018

# KNOX_CONTAINER V3.0
ifeq ($(SEC_KNOX_CONTAINER_VERSION),$(filter $(SEC_KNOX_CONTAINER_VERSION), v30 v35 v40))
PRODUCT_PACKAGES += \
    KnoxCore \
    ContainerAgent3 \
    BBCAgent \
    SecureFolder
endif

# Dual IM
PRODUCT_PACKAGES += \
    DAAgent

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

#Add SamsungClipboard UI
PRODUCT_PACKAGES += \
    ClipboardUIService \
    ClipboardSaveService

# Add Samsung Messages
PRODUCT_PACKAGES += \
    SamsungMessages_10.0 \
    SecTelephonyProvider
    
# SOS Message
 PRODUCT_PACKAGES += \
    SendHelpMessage

# for DataCreate
PRODUCT_PACKAGES += \
    AutomationTest_FB

# Add Calendar
PRODUCT_PACKAGES += \
    SamsungCalendar \
    SamsungCalendarProvider

# Safety Care
PRODUCT_PACKAGES += \
    EmergencyLauncher \
    EmergencyModeService \
    EmergencyProvider

# Add Samsung SetupWizard
PRODUCT_PACKAGES += \
    SecSetupWizard_Global

# Add SetupWizard Script Player 
PRODUCT_PACKAGES +=  \
     SuwScriptPlayer 

# Add SafetyInformation
PRODUCT_PACKAGES += \
    SafetyInformation

# Settings
PRODUCT_PACKAGES +=  \
    SecSettingsIntelligence \
    SecSettings \
    SettingsReceiver

# Add Sim card manager
PRODUCT_PACKAGES += \
    SimSettingMgr    

# Accessibility
PRODUCT_PACKAGES += \
    Accessibility

# Add PreloadInstaller
PRODUCT_PACKAGES += \
    PreloadInstaller

# DLNA
PRODUCT_PACKAGES +=  \
    allshare \
    AllshareMediaShare

# Add EasySetup
PRODUCT_PACKAGES += \
    EasySetup

# FileShare
PRODUCT_PACKAGES +=  \
    AllshareFileShare

# Samsung Galalxy Friends
PRODUCT_PACKAGES += \
    MateAgent 

# SamsungConnect
PRODUCT_PACKAGES +=  \
    SamsungConnect

PRODUCT_PACKAGES += \
    TouchWizHome_2017 \
    -Protips

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

# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v4.0
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_401.xml:system/etc/permissions/com.sec.feature.nsflp_level_401.xml
		
# Add StickerCenter
PRODUCT_PACKAGES += \
    StickerCenter

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
  svemanager \
  sveservice 

# Add SBrowser
PRODUCT_PACKAGES += \
  SBrowser_9.0_Removable

# Add SGames
PRODUCT_PACKAGES += \
    GameHome \
    GameTools_Dream

# Added ChromeCustomizations
PRODUCT_PACKAGES += \
    ChromeCustomizations
	
# adding SecHTMLViewer
PRODUCT_PACKAGES += \
    SecHTMLViewer

# Add SamsungPassAutofill
PRODUCT_PACKAGES += \
    SamsungPassAutofill_v1

# SemExtendedFormat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml \
    libSEF.quram.vendor

# Add SamsungCloud
PRODUCT_PACKAGES += \
    SamsungCloudClient
    
# Add SCPM Client
PRODUCT_PACKAGES += \
    SCPMClient_N

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

# Add SoundAlive
PRODUCT_PACKAGES += \
    SoundAlive_55 \
    -MusicFX

# Add AdaptSound
PRODUCT_PACKAGES += \
    Hearingdro_V7_P

# Add SAIV for prebuilt camera and bixby vision apps
PRODUCT_PACKAGES += \
    saiv \
    saiv.xml

# Add Calculator and Clock
PRODUCT_PACKAGES += \
    SecCalculator_R \
    ClockPackage

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

# Add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer
	
# Add VideoEditor Framework Solutions
PRODUCT_PACKAGES += \
    videoeditor_sdk	
	
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libvesgraphicframework.videoeditor.samsung.so:system/lib64/libvesgraphicframework.videoeditor.samsung.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libSoundAlive_SRC192_ver205a.so:system/lib64/libSoundAlive_SRC192_ver205a.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so:system/lib64/libSoundAlive_VSP_ver316a_ARMCpp_64bit.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/QCOM/libvesinterface.videoeditor.samsung.so:system/lib64/libvesinterface.videoeditor.samsung.so\
    vendor/samsung/frameworks/VideoEditor/Star/Lib64/QCOM/libvesframework.videoeditor.samsung.so:system/lib64/libvesframework.videoeditor.samsung.so	

# Add VideoEditor
PRODUCT_PACKAGES += \
    VideoEditorLite_Dream_N
	
# Add Story Video Editor
PRODUCT_PACKAGES += \
    StoryEditor_Dream_N
	
# add MirrorLink tms
PRODUCT_PACKAGES += \
    MirrorLink \
    libtmdisplay.mirrorlink.samsung \
    public.libraries-mirrorlink.samsung.txt \
    audio.tms.default \
    libdapjni

PRODUCT_COPY_FILES += \
	frameworks/base/data/etc/com.samsung.feature.mirrorlink_fw_level8.xml:system/etc/permissions/com.samsung.feature.mirrorlink_fw_level8.xml

# Add SlowMotion
PRODUCT_PACKAGES += \
    SlowMotionVideoEditor_BGMProvider
	
# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    SmartMirroring

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

# Add Barge In New shared lib
PRODUCT_PACKAGES += \
  libBargeInEngine

# Add Samsung Barge In data
PRODUCT_PACKAGES += \
  BargeIn_Samsung_Camera

# Add watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub

# add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyApps_OPEN

# Add Galaxy Essential Widget
PRODUCT_PACKAGES += \
    GalaxyAppsWidget_Phone_Dream

# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient
   
# Add GearManager
PRODUCT_PACKAGES += \
    GearManager

# Add Samsung Notes
PRODUCT_PACKAGES += \
    Notes_Removable

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
    
# Add Intelligent Proximity
PRODUCT_PACKAGES += \
    IpsGeofence

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder
    
# Network Diagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic

# Add Samsung Email
PRODUCT_PACKAGES += \
    SecEmail_P \
    com.samsung.android.email.provider.xml
	
#Add SettingsBixby
PRODUCT_PACKAGES += \
    SettingsBixby

########################################################################################
# RIL Build Packages
########################################################################################

BUILD_RIL_EVOLUTION_RIL := true

# [RIL] ATD
PRODUCT_PACKAGES += \
    libatparser \
    libomission_avoidance \
    libfactoryutil \
    at_distributor

# libsecdiag, sec_diag_uart_log
PRODUCT_PACKAGES += \
    libsecdiag \
    sec_diag_uart_log

# Add GearVRService
PRODUCT_PACKAGES += \
    GearVRService

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

#Add Routines
PRODUCT_PACKAGES += \
    Routines

#Add 2nd Screen CocktailBar feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.cocktailpanel.xml:system/etc/permissions/com.sec.feature.cocktailpanel.xml

#Add 2nd Screen CocktailBar v3 feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.edge_v03.xml:system/etc/permissions/com.sec.feature.edge_v03.xml

# Voice Recorder
PRODUCT_PACKAGES += \
    VoiceNote_5.0

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_Beyond

# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_Beyond

# Add Avatar Emoji Sticker
PRODUCT_PACKAGES += \
    AvatarEmojiSticker_Beyond

# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

# Add LiveFocus Viewer
PRODUCT_PACKAGES += \
    DualOutFocusViewer

# Add SingleImageOutFocus Viewer
PRODUCT_PACKAGES += \
    SingleOutFocusViewer

#Add Rubin 23
PRODUCT_PACKAGES += \
    RubinVersion23

# Add ApexService
PRODUCT_PACKAGES += \
    ApexService

# SmartFittingService
PRODUCT_PACKAGES += \
    SmartFittingService

# Add ThemeCenter and UPSM Theme
PRODUCT_PACKAGES += \
    ThemeCenter \
    UPSMTheme
	
# Add SamsungThemes
PRODUCT_PACKAGES += \
    ThemeStore
	
# Add WeatherWidget
PRODUCT_PACKAGES += \
   Weather_SEP10.1
   
# Add SmartCapture
PRODUCT_PACKAGES += \
    SmartCapture

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

# Add OneDrive
PRODUCT_PACKAGES += \
    OneDrive_Samsung_v3

#Add SystemUIDesktop
PRODUCT_PACKAGES += \
    SystemUIDesktop

# SamsungDeX Launcher
PRODUCT_PACKAGES += \
    KnoxDesktopLauncher

# Add DexCommunity
PRODUCT_PACKAGES += \
    DexCommunity
    
# Add DesktopModeUiService
PRODUCT_PACKAGES += \
    DesktopModeUiService

# eSE features
PRODUCT_PACKAGES += \
    sem_daemon \
    SEMFactoryApp

# Add AODService (Always on display)
PRODUCT_PACKAGES += \
    AODService_v40 \
    libDigitalClockEncoder.aod.samsung \
    public.libraries-aod.samsung.txt

# Add Always on display System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.aodservice_v06.xml:system/etc/permissions/com.samsung.feature.aodservice_v06.xml

# AppLinker
PRODUCT_PACKAGES += \
    AppLinker

# SLocation
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

# Add Handwriting
PRODUCT_PACKAGES += \
    HandwritingService \
    libSDKRecognitionText.spensdk.samsung

# EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

#Add PeopleStripe
PRODUCT_PACKAGES += \
    PeopleStripe

#Add people edge notification feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.people_edge_notification.xml:system/etc/permissions/com.sec.feature.people_edge_notification.xml

# Flipboard Briefing
PRODUCT_PACKAGES += \
    FlipboardBriefing 

# Add Upday
PRODUCT_PACKAGES += \
    Upday
	
# Add Bixby
PRODUCT_PACKAGES += \
    Bixby

# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome

# Add KidsHome
PRODUCT_PACKAGES += \
    KidsHome_P

# Flipfont
ifneq ($(filter %kx %ks %ktt %lgt %skt,$(TARGET_PRODUCT)),)
PRODUCT_PACKAGES += \
    SamsungOne \
    -ChocoEUKor \
    -Tinker \
    -AppleMint
else ifneq ($(filter %zc %zm %zn %ctc %zh %zt,$(TARGET_PRODUCT)),)
PRODUCT_PACKAGES += \
    -Miao \
    -ShaoNv \
    -Kaiti
else ifeq ($(filter %dcm %kdi %sbm %jpn %dcmactive,$(TARGET_PRODUCT)),)
PRODUCT_PACKAGES += \
    -ChocoEUKor \
    -RoseEUKor \
    -CoolEUKor \
    SamsungOne
endif

# Audio
-include vendor/samsung/frameworks/audio/audiopackages/AudioBuildProperties.mk

# FactoryBinary only (end of line)
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

# for FTL
FACTORY_BINARY_FOR_FTL_WIFI_FEATURE := disable

# factory common mk
include vendor/samsung/fac_vendor_common/fac_vendor_common.mk

# Behind Display Test
PRODUCT_PACKAGES += \
    BehindDispTest

# add test OIS SR TEST
PRODUCT_PACKAGES += \
    Hyvision

endif #end of SEC_FACTORY_BUILD
###############################################################
# FactoryBinary only (end of line)
# PLEASE DO NOT ADD LINE BELOW
