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

# Power ON Animation
addpuQmgResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),\
          $(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1)))\
    ,,$(1))

PU_RESOLUTION := 1080_2340
PRODUCT_PACKAGES += \
    $(call addpuQmgResource,bootsamsung.qmg)\
    $(call addpuQmgResource,bootsamsungloop.qmg)

# Encryption Animation
pu_image_path := vendor/samsung/external/puQmgResource/img/DreamLook
PRODUCT_COPY_FILES += \
    $(pu_image_path)/xx/1080_2340/crypt_bootsamsung.qmg:system/media/crypt_bootsamsung.qmg \
    $(pu_image_path)/xx/1080_2340/crypt_bootsamsungloop.qmg:system/media/crypt_bootsamsungloop.qmg

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
TARGET_RESOLUTION := 720x1280

PRODUCT_PACKAGES += \
    $(call addpdQmgResource,shutdown.qmg)

PRODUCT_PACKAGES += \
    BadgeProvider_N

PRODUCT_PACKAGES += \
    TouchWizHome_2017

# Live Wallpaper
PRODUCT_PACKAGES += \
    SecLiveWallpapersPicker

# Live wallpaper feature
PRODUCT_COPY_FILES += \
    applications/sources/wallpapers/LiveWallpaperPicker/android.software.live_wallpaper.xml:system/etc/permissions/android.software.live_wallpaper.xml

# Add GMS common property
include vendor/samsung/configs/m30lte_common/gms_m30lte_common.mk

# Add Samsung Camera packages
PRODUCT_PACKAGES += \
    SamsungCamera8 \
    ShootingModeProvider2 \
    FilterInstaller \
    FilterProvider \
    StickerFaceAR \
    StickerStamp \
    StickerProvider \
    CameraBokehService \
    libbokehbpengine.camera.samsung

# Add FingeprintManagerService
PRODUCT_PACKAGES += FingerprintService2
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

# Add Magnifier3
PRODUCT_PACKAGES += \
    SamsungMagnifier3

# Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS \
    FBServices

# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv3.2_NEWJ \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic


# Samsung Messaging for SEP Lite
PRODUCT_PACKAGES += \
    Messaging_Common_5.0 \
    -Messaging_sticker_plugin \
    SecTelephonyProvider

# Remove google app
PRODUCT_PACKAGES += \
    -messaging

# Add SamsungInCallUILite
PRODUCT_PACKAGES += \
    SamsungInCallUILite

# SamsungNetworkUI
PRODUCT_PACKAGES += \
    NetworkUI

# Safety Care
PRODUCT_PACKAGES += \
    EmergencyLauncher \
    EmergencyModeService \
    EmergencyProvider

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SamsungContactsProvider95

# add Easymode favorite contacts widget
#PRODUCT_PACKAGES += \
#  EasymodeContactsWidget81

# Add SmartCallProvider
PRODUCT_PACKAGES += \
    SmartCallProvider\
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

# Add SafetyInformation
PRODUCT_PACKAGES += \
    SafetyInformation

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES +=  \
    Omc

# Accessibility
PRODUCT_PACKAGES += \
    AccessControl_N \
    AssistantMenu_N

# Add Sim card manager
PRODUCT_PACKAGES += \
    SimSettingMgr

# WifiDisplay
PRODUCT_PACKAGES += \
    libuibc \
    SmartMirroring

# UIBCVirtualSoftkey for Screen Mirroring
PRODUCT_PACKAGES += \
    UIBCVirtualSoftkey

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

# FileShare
PRODUCT_PACKAGES +=  \
	allshare \
    AllshareFileShare

# DLNA
PRODUCT_PACKAGES +=  \
    AllshareMediaShare

# SmartFittingService
PRODUCT_PACKAGES += \
    SmartFittingService

# Add Beamservice
PRODUCT_PACKAGES += \
    BeamService

# Video TG
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

# add Samsung Notes
PRODUCT_PACKAGES += \
    Notes_Removable

# add ORG.
PRODUCT_PACKAGES += \
    SecCalculator_N_R \
    -DeskClock \
    ClockPackage

# Add Samsung Email
PRODUCT_PACKAGES += \
    SecEmail_O \
    com.samsung.android.email.provider.xml

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_WQHD_Star_Lite_Rize

# Add watchmanagerstub
PRODUCT_PACKAGES += \
    GearManagerStub

#Add secvision
PRODUCT_PACKAGES += \
        secvision \
        secvision.xml

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

# Add Galaxy Apps widget
PRODUCT_PACKAGES += \
    GalaxyAppsWidget_Phone_Dream

# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient

# Add SamsungCloud
PRODUCT_PACKAGES += \
    SamsungCloudClient
   
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


# CMHProvider
PRODUCT_PACKAGES += \
    CMHProvider

# CMHServices
PRODUCT_PACKAGES += \
    IPService \
    FaceService \
    StoryService \
    MediaLearningPlatform

# Add SBrowser
PRODUCT_PACKAGES += \
    SBrowser_8.3_Removable

# Adding WebManual
PRODUCT_PACKAGES += \
    WebManual

# Adding ChromeCustomizations
PRODUCT_PACKAGES += \
    ChromeCustomizations

# Adding SecHTMLViewer
PRODUCT_PACKAGES += \
    SecHTMLViewer

# Add SCPM Client
PRODUCT_PACKAGES += \
    SCPMClient_N

# Added Dictionry for Sec
PRODUCT_PACKAGES += \
    DictDiotekForSec

# Add Download Provider
PRODUCT_PACKAGES += \
    SecDownloadProvider

# EasyOneHand
PRODUCT_PACKAGES += \
    EasyOneHand3

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


# Dual IM
PRODUCT_PACKAGES += \
    DAAgent

# SOS Message
 PRODUCT_PACKAGES += \
    SendHelpMessage

# Add SGames
PRODUCT_PACKAGES += \
    GameHome \
    GameTools_Dream

# WallpaperCropper2
PRODUCT_PACKAGES += \
     WallpaperCropper2

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
    Weather_SEP9.1

PRODUCT_PACKAGES += \
    SMusicPicker

# Add Samsung Billing
PRODUCT_PACKAGES += \
    SamsungBilling

# Add SamsungWallpaper
PRODUCT_PACKAGES += \
    wallpaper-res

# Keyguard wallpaper updator
PRODUCT_PACKAGES += \
    KeyguardWallpaperUpdator

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

# Add Samsung TTS
PRODUCT_PACKAGES += SamsungTTS
PRODUCT_PACKAGES += smt_en_US_f00

# SLocation
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/location/libsgeo/libsgeo.so:system/lib/slocation/libsgeo.so \
    vendor/samsung/frameworks/location/libsgeo/libsgeo64.so:system/lib64/slocation/libsgeo.so \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation


#Sensors feature
PRODUCT_COPY_FILES += \
    frameworks/native/data/etc/android.hardware.sensor.accelerometer.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.sensor.accelerometer.xml \
    frameworks/native/data/etc/android.hardware.sensor.gyroscope.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.sensor.gyroscope.xml \
    frameworks/native/data/etc/android.hardware.sensor.compass.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.sensor.compass.xml \
    frameworks/native/data/etc/android.hardware.sensor.light.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.sensor.light.xml \
    frameworks/native/data/etc/android.hardware.sensor.proximity.xml:$(TARGET_COPY_OUT_VENDOR)/etc/permissions/android.hardware.sensor.proximity.xml

#Motion packages
PRODUCT_PACKAGES += \
    motionrecognitionservice

PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.motionrecognition_service.xml:system/etc/permissions/com.sec.feature.motionrecognition_service.xml

# NetworkDiagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic

# SamsungPositioning
PRODUCT_PACKAGES += \
    SamsungPositioning

# Add NS-FLP (Samsung Location SDK)
PRODUCT_PACKAGES += \
    NSFusedLocation_v3.3
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.nsflp_level_330.xml:system/etc/permissions/com.sec.feature.nsflp_level_330.xml

# Add SFinder
PRODUCT_PACKAGES += \
    Finder_v8

# Add SFinder FinDo feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.sec.feature.findo.xml:system/etc/permissions/com.sec.feature.findo.xml

# Add SFinder System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.galaxyfinder_v7.xml:system/etc/permissions/com.samsung.feature.galaxyfinder_v7.xml

PRODUCT_PACKAGES += \
    SMusicPicker

# Added VoiceNote
PRODUCT_PACKAGES += \
    VoiceNote_5.0

# Add OneDrive
PRODUCT_PACKAGES += \
    OneDrive_Samsung_v3

#Add Rubin 22
PRODUCT_PACKAGES += \
    RubinVersion22

# Add ContextProvider
PRODUCT_PACKAGES += \
    ContextProvider

#Add SamsungClipboard UI
PRODUCT_PACKAGES += \
    ClipboardUIService \
    ClipboardSaveService

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    imsmanager \
    ImsTelephonyService \
    imsservice \
    vsimmanager \
    vsimservice \
    secimshttpclient \
    ImsLogger \
    ImsSettings \
    imscoremanager \
    imsd \
    svemanager \
    sveservice \
    rcsopenapi \
    RcsSettings
endif

# Add BioFaceService
PRODUCT_PACKAGES += BioFaceService

# Google Approval Test
PRODUCT_PROPERTY_OVERRIDES += \
    ro.error.receiver.default=com.samsung.receiver.error

PRODUCT_DEFAULT_PROPERTY_OVERRIDES += \
    persist.sys.usb.config=mtp

# Add SmartFittingService
PRODUCT_PACKAGES += \
    SmartFittingService

# SLocation
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/location/libsgeo/libsgeo.so:system/lib/slocation/libsgeo.so \
    vendor/samsung/frameworks/location/libsgeo/libsgeo64.so:system/lib64/slocation/libsgeo.so \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# Add AODService (Always on display)
PRODUCT_PACKAGES += \
    AODService_v30

# Add Always on display System Feature
PRODUCT_COPY_FILES += \
    frameworks/base/data/etc/com.samsung.feature.aodservice_v05.xml:system/etc/permissions/com.samsung.feature.aodservice_v05.xml

# copy SideSync Daemon
PRODUCT_COPY_FILES += \
    applications/par/idp/SideSync/ss_conn_daemon:system/bin/ss_conn_daemon

# Add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer_SEP90

# Add AdaptiveBrightness
PRODUCT_PACKAGES += \
    AdaptiveBrightness

#remove Samsung Pay Framework
PRODUCT_PACKAGES += \
    -PaymentFramework	

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
