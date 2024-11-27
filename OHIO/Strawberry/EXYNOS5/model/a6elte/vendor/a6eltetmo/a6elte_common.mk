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
include build/target/product/product_launched_with_o.mk

PRODUCT_BRAND := samsung
PRODUCT_MANUFACTURER := samsung

# Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))
PU_RESOLUTION := 720_1480

PRODUCT_PACKAGES += \
    $(call addpuQmgResource,bootsamsung.qmg)\
    $(call addpuQmgResource,bootsamsungloop.qmg)

# Encryption Animation
pu_image_path := vendor/samsung/external/puQmgResource/img/DreamLook
PRODUCT_COPY_FILES += \
    $(pu_image_path)/xx/720_1480/crypt_bootsamsung.qmg:system/media/crypt_bootsamsung.qmg \
    $(pu_image_path)/xx/720_1480/crypt_bootsamsungloop.qmg:system/media/crypt_bootsamsungloop.qmg

# LPM Animation
addlpmSpiResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

LPM_LOOK_TYPE := DreamLook
LPM_RESOLUTION := 720_1480
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
TARGET_RESOLUTION := 720x1480

PRODUCT_PACKAGES += \
    $(call addpdQmgResource,shutdown.qmg)\
    $(call addpdQmgResource,shutdownafter.qmg)

PRODUCT_PACKAGES += \
    BadgeProvider_N

PRODUCT_PACKAGES += \
    TouchWizHome_2017

# Add GMS common property
include vendor/samsung/configs/a6elte_common/gms_a6elte_common.mk

# Add FingeprintManagerService
PRODUCT_PACKAGES += FingerprintService2
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

# Add BioFaceService
PRODUCT_PACKAGES += BioFaceService

# Add Samsung Camera packages
PRODUCT_PACKAGES += \
    SamsungCamera7 \
    ShootingModeProvider2 \
    FilterProvider \
    StickerFaceAR \
    StickerStamp \
    StickerProvider

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3

# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv3.0_A6 \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic


# Samsung Messaging
PRODUCT_PACKAGES += \
    Messaging_Common_5.0 \
    Messaging_sticker_plugin \
    SecTelephonyProvider

# Remove google app
PRODUCT_PACKAGES += \
    -messaging

# Add SamsungInCallUI
PRODUCT_PACKAGES += \
    SamsungInCallUI

# SamsungNetworkUI
PRODUCT_PACKAGES += \
    NetworkUI

# Safety Care
PRODUCT_PACKAGES += \
    EmergencyLauncher \
    EmergencyModeService \
    EmergencyProvider

# Add SafetyInformation
PRODUCT_PACKAGES += \
    SafetyInformation

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SamsungContactsProvider90

# DRParser
PRODUCT_PACKAGES += \
    DRParser

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81


# Add SmartCallProvider
PRODUCT_PACKAGES += \
    SmartCallProvider \
    HiyaService

# add Fmm client
PRODUCT_PACKAGES += \
    Fmm

# for DataCreate
PRODUCT_PACKAGES += \
    AutomationTest_FB

# Add Calendar
PRODUCT_PACKAGES += \
    SamsungCalendar \
    SamsungCalendarProvider

# Settings
PRODUCT_PACKAGES +=  \
    SecSettingsProvider \
    SecSettings \
    SettingsReceiver

# Samsung Print
PRODUCT_PACKAGES +=  \
    SPrintSpooler

# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    withTV \
    SmartMirroring

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

# Accessibility  - Setting 
PRODUCT_PACKAGES += \
    STalkback \
    UniversalSwitch \
    AccessControl_N \
    AssistantMenu_N \
    ColorAdjustment
 

# Add EasySetup
PRODUCT_PACKAGES +=  \
    EasySetup

# Add PreloadInstaller
PRODUCT_PACKAGES += \
    PreloadInstaller

# Add Samsung SetupWizard
PRODUCT_PACKAGES += \
    SecSetupWizard_Global

# add My Files
PRODUCT_PACKAGES += \
    SecMyFiles2017

# Add LinkSharing
PRODUCT_PACKAGES += \
   CoreApps_EOP \
   LinkSharing_v40

#Add DLNA, FileShare
PRODUCT_PACKAGES += \
    allshare \
    AllshareFileShare \
    AllshareMediaShare
	
# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

# add Samsung Notes
PRODUCT_PACKAGES += \
    Notes_Removable

# add ORG.
PRODUCT_PACKAGES += \
    SecCalculator_N_R \
    ClockPackage

# Add Samsung Email
PRODUCT_PACKAGES += \
    SecEmail_O \
    com.samsung.android.email.provider.xml

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_WQHD_Star
    
# Add Gear360Editor
PRODUCT_PACKAGES += \
    Gear360Editor_WQHD_Star

#ADD watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub

# Add GearManager
PRODUCT_PACKAGES += \
    GearManager

#Add saiv
PRODUCT_PACKAGES += \
    saiv \
    saiv.xml

# Add SoundAliveFX Plug-in APK (new version : SoundAlive_52)
PRODUCT_PACKAGES += \
    SoundAlive_52 \
    -MusicFX

# Add AdaptSound
PRODUCT_PACKAGES += \
    Hearingdro_V7

# SamsungNetworkUI
PRODUCT_PACKAGES += \
    NetworkUI

# add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyApps_OPEN

# Add GalaxyApps Widget
PRODUCT_PACKAGES += \
    GalaxyAppsWidget_Phone_Dream
	
# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient

# Add ThemeCenter and UPSM Theme
PRODUCT_PACKAGES += \
    ThemeCenter \
    UPSMTheme

# Add Samsung Themes
PRODUCT_PACKAGES += \
    ThemeStore

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

# Add SBrowser
PRODUCT_PACKAGES += \
    SBrowser_7.2

# Add ChromeCustomization
PRODUCT_PACKAGES += \
    ChromeCustomizations

# Add SecHTMLViewer
PRODUCT_PACKAGES += \
    SecHTMLViewer

# Added Dictionry for Sec
PRODUCT_PACKAGES += \
    DictDiotekForSec

# Add Download Provider
PRODUCT_PACKAGES += \
    SecDownloadProvider

# EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

# CMHServices
PRODUCT_PACKAGES += \
    DCMService \
    FaceService \
    StoryService \
    MediaLearningPlatform

# Add Smart Switch Agent
PRODUCT_PACKAGES += \
    SmartSwitchAgent

# Add ShortcutBackupService
PRODUCT_PACKAGES += \
    ShortcutBackupService

# Add Kies
PRODUCT_PACKAGES += \
    wssyncmlnps2	

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder

# Add SmartManager
PRODUCT_PACKAGES += \
    SmartManager_v6_DeviceSecurity \
    SmartManager_v5

# Add BixbyHome
PRODUCT_PACKAGES += \
    BixbyHome

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

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
    
# Dual IM
PRODUCT_PACKAGES += \
    DAAgent

# SOS Message
 PRODUCT_PACKAGES += \
    SendHelpMessage

# KNOX-DLP
PRODUCT_PACKAGES += \
    SamsungDLPService

# Add SGames
PRODUCT_PACKAGES += \
    GameHome \
    GameTools

# Flipboard Briefing
PRODUCT_PACKAGES += \
    FlipboardBriefing
	
# WallpaperCropper2
PRODUCT_PACKAGES += \
     WallpaperCropper2

# Live Wallpaper
PRODUCT_PACKAGES += \
    SecLiveWallpapersPicker

# Live wallpaper feature
PRODUCT_COPY_FILES += \
    applications/sources/wallpapers/LiveWallpaperPicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml

# Add SamsungWallpaper
PRODUCT_PACKAGES += \
    wallpaper-res
    
# Keyguard wallpaper updator
PRODUCT_PACKAGES += \
    KeyguardWallpaperUpdator

# Add StickerCenter
PRODUCT_PACKAGES += \
    StickerCenter

#Add SecGallery2015
PRODUCT_PACKAGES += \
    SecGallery2015 \
    com.sec.android.gallery3d.xml

#OverlayMagnifier feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.overlaymagnifier.xml:system/etc/permissions/com.sec.feature.overlaymagnifier.xml

# Adding MobileOffice Stub
PRODUCT_PACKAGES += \
    Excel_SamsungStub \
    PowerPoint_SamsungStub \
    Word_SamsungStub

# Adding LinkedIn Stub
PRODUCT_PACKAGES += \
    LinkedIn_SamsungStub

# Add WeatherWidget
PRODUCT_PACKAGES += \
    Weather_SEP9.0

PRODUCT_PACKAGES += \
    SMusicPicker

# Add SamsungConnect
PRODUCT_PACKAGES += \
    SamsungConnect
    
# Add Samsung Billing
PRODUCT_PACKAGES += \
    SamsungBilling
    
# Add SamsungCloud
PRODUCT_PACKAGES += \
    SamsungCloud

#Add SamsungClipboard UI
PRODUCT_PACKAGES += \
    ClipboardUIService \
    ClipboardSaveService

# Add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer_SEP90

# Add ContextProvider
PRODUCT_PACKAGES += \
	ContextProvider

# Add SFinder
PRODUCT_PACKAGES += \
    Finder_v8

# Add SFinder FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml

# Add SFinder System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.galaxyfinder_v7.xml:system/etc/permissions/com.samsung.feature.galaxyfinder_v7.xml

# Add SHealth
PRODUCT_PACKAGES += \
    SHealth5 \
    HealthService

# Add Samsung Pass
PRODUCT_PACKAGES += \
    SamsungPass
	
# SamsungPassAutofill
PRODUCT_PACKAGES += \
    SamsungPassAutofill_v1

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

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

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

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

# SLocation
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/location/libsgeo/libsgeo.so:system/lib/slocation/libsgeo.so \
    vendor/samsung/frameworks/location/libsgeo/libsgeo64.so:system/lib64/slocation/libsgeo.so \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# Add DeviceHealthReporter
PRODUCT_PACKAGES += \
    DeviceHealthReporter

#Add Rubin 2.0
PRODUCT_PACKAGES += \
    RubinVersion20

# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v3.3
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_330.xml:system/etc/permissions/com.sec.feature.nsflp_level_330.xml

# Add Bixby Vision 2.0 framework
PRODUCT_PACKAGES += \
    BixbyVisionFramework

# Add bixby vision 2.0 app
PRODUCT_PACKAGES += \
    VisionIntelligence2

# if bin User - User - Ship, remove DHR
ifeq ($(SEC_FACTORY_BUILD),false)
ifeq ($(SEC_BUILD_OPTION_TYPE),user)
ifeq ($(SEC_BUILD_OPTION_PRODUCT_SHIP),true)
PRODUCT_PACKAGES += \
    -DeviceHealthReporter
endif
endif
endif

# Add ClockPack (LockScreen Clock)
PRODUCT_PACKAGES += \
    ClockPack_v30

# Add ClockPack System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.clockpack_v05.xml:system/etc/permissions/com.samsung.feature.clockpack_v05.xml

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

# Hwparam
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/base/services/hqm/res/res/raw/hqm_j7topelte.xml:system/factory/hqm_j7topelte.xml

endif
###############################################################
# FactoryBinary only (end of line)
# PLEASE DO NOT ADD LINE BELOW

PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp
