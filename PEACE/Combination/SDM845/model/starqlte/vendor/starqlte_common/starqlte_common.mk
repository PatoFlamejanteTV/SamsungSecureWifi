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

include build/target/product/product_launched_with_o.mk

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

# Add Contacts/Dialer
PRODUCT_PACKAGES += \
    SamsungContacts

# Add Contacts Providers
PRODUCT_PACKAGES += \
    SamsungContactsProvider100

# add Easymode favorite contacts widget
PRODUCT_PACKAGES += \
    EasymodeContactsWidget81

# Add My Files
PRODUCT_PACKAGES += \
    SecMyFiles2017

# DRParser
PRODUCT_PACKAGES += \
    DRParser
	
# Add Download Providers
PRODUCT_PACKAGES += \
    SecDownloadProvider
    
# SemExtendedFormat
PRODUCT_PACKAGES += \
    semextendedformat \
    semextendedformat.xml \
    libSEF.quram.vendor

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

# Add SmartReminder
PRODUCT_PACKAGES += \
    SmartReminder

# Samsung Galalxy Friends
PRODUCT_PACKAGES += \
    MateAgent 

# Add Galaxy Apps
PRODUCT_PACKAGES += \
    GalaxyApps_OPEN

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

# Add GearVRService
PRODUCT_PACKAGES += \
    GearVRService

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

# WallpaperCropper2
PRODUCT_PACKAGES += \
    WallpaperCropper2

# Add Iris Service packages
PRODUCT_PACKAGES += \
    SecIrisService

# SamsungIME
PRODUCT_PACKAGES += \
    SamsungIMEv3.2 \
    -LatinIME \
    -PinyinIME \
    -OpenWnn \
    -libwnndict \
    -libWnnEngDic \
    -libWnnJpnDic

# Add Samsung Messages
PRODUCT_PACKAGES += \
    Messaging_Common_5.0 \
    Messaging_sticker_plugin \
    SecTelephonyProvider

# Send SOS Message
PRODUCT_PACKAGES += \
    SendHelpMessage

# Add Samsung SetupWizard
#PRODUCT_PACKAGES += \
#    SecSetupWizard_Global

# for DataCreate
PRODUCT_PACKAGES += \
    AutomationTest_FB

# Add Calendar
PRODUCT_PACKAGES += \
    SamsungCalendar \
    SamsungCalendarProvider

# Settings
PRODUCT_PACKAGES +=  \
    SecSettingsIntelligence \
    SecSettings \
    SettingsReceiver

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

# Add Barge In New shared lib
PRODUCT_PACKAGES += \
  libBargeInEngine

# Add Samsung Barge In data
PRODUCT_PACKAGES += \
  BargeIn_Samsung_Camera

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
	
# Add Samsung Push Service
PRODUCT_PACKAGES += \
   SPPPushClient
   
# Add Smart Switch Agent
PRODUCT_PACKAGES += \
    SmartSwitchAgent

# Add Smart Switch
PRODUCT_PACKAGES += \
    SmartSwitch
	
# Add Samsung Positioning
PRODUCT_PACKAGES += \
    SamsungPositioning
	
# Network Diagnostic
PRODUCT_PACKAGES += \
    NetworkDiagnostic

# Add EasySetup
PRODUCT_PACKAGES += \
    EasySetup
	
# DLNA
PRODUCT_PACKAGES +=  \
    allshare \
    AllshareMediaShare

# SamsungConnect
PRODUCT_PACKAGES +=  \
    SamsungConnect

# FileShare
PRODUCT_PACKAGES +=  \
    AllshareFileShare

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
  svemanager \
  sveservice 

# SLocation
PRODUCT_COPY_FILES += \
    vendor/samsung/frameworks/location/libsgeo/libsgeo.so:system/lib/slocation/libsgeo.so \
    vendor/samsung/frameworks/location/libsgeo/libsgeo64.so:system/lib64/slocation/libsgeo.so \
    frameworks/base/data/etc/com.sec.feature.slocation_level3.xml:system/etc/permissions/com.sec.feature.slocation_level3.xml

PRODUCT_PACKAGES += \
    SLocation

# Add GMS common property
include vendor/samsung/configs/starqlte_common/gms_starqlte_common.mk

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
    SecureFolderSetupPage
endif

# Add BlueLightFilter Service
PRODUCT_PACKAGES += \
  BlueLightFilter

# eSE features
PRODUCT_PACKAGES += \
    sem_daemon \
    SEMFactoryApp

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
    frameworks/base/data/etc/com.samsung.feature.aodservice_v05.xml:system/etc/permissions/com.samsung.feature.aodservice_v05.xml

#Remove QCT &Google pkg
PRODUCT_PACKAGES += \
    -CMFileManager \
    -QtiDialer \
    -SoundRecorder \
    -VoiceDialer \
    -PresenceApp \
    -ConnectionManagerTestApp \
    -imssettings

# Add Calculator and Clock
PRODUCT_PACKAGES += \
    SecCalculator_R \
    ClockPackage

#Add SecGallery2015
PRODUCT_PACKAGES += \
    SecGallery2015 \
    com.sec.android.gallery3d.xml

# Add ShortcutBackupService
PRODUCT_PACKAGES += \
    ShortcutBackupService

# Add SamsungVideoPlayer
PRODUCT_PACKAGES += \
    SamsungVideoPlayer2016

# add VideoTrimmer
PRODUCT_PACKAGES += \
    VideoTrimmer

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
	
# Add SlowMotion
PRODUCT_PACKAGES += \
    SlowMotion_Star_N

# SamsungDeX Launcher
PRODUCT_PACKAGES += \
    KnoxDesktopLauncher

# DesktopModeUiService
PRODUCT_PACKAGES += \
    DesktopModeUiService

# Add Photo Studio
PRODUCT_PACKAGES += \
    PhotoStudio_Beyond

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
    Notes23_Removable

#Add Rubin 23
PRODUCT_PACKAGES += \
    RubinVersion23

# Add ThemeCenter and UPSM Theme
PRODUCT_PACKAGES += \
    ThemeCenter \
    UPSMTheme

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
