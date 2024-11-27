QCOM_PRODUCT_DEVICE := r1q

$(call inherit-product, device/samsung/r1q/device.mk)
include vendor/samsung/configs/r1q_common/r1q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/r1q/gms_r1qzc.mk
endif

PRODUCT_NAME := r1qzc
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A8050

include vendor/samsung/build/localelist/SecLocale_CHN.mk

# [CMC] Samsung CMC
#PRODUCT_PACKAGES += \
#    MdecService

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# [IMS] Samsung RcsSettings
#PRODUCT_PACKAGES += \
#    RcsSettings
	
# Crane
#PRODUCT_PACKAGES += \
#    Crane
	
#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter
	
# Add Amazon Shopping
#PRODUCT_PACKAGES += \
#    Amazon_Shopping

# Add Helo
#PRODUCT_PACKAGES += \
#    Helo

# Add Snapchat
PRODUCT_PACKAGES += \
    Snapchat
	
# Add Booking.com 
#PRODUCT_PACKAGES += \
#    Booking_SWA
	
# Add Dailyhunt
#PRODUCT_PACKAGES += \
#    Dailyhunt

# FactoryBinary doesn't need below packages.
#ifneq ($(SEC_FACTORY_BUILD),true)
# Add Amazon Prime Video
#PRODUCT_PACKAGES += \
#    AmazonVideo_SWA
#endif

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
#PRODUCT_PACKAGES += \
#    BlockchainBasicKit
	
# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add MemorySaver Solution
#PRODUCT_PACKAGES += \
#    MemorySaver_O_Refresh

PRODUCT_PACKAGES += \
    -YahooEdgeSports \
    -YahooEdgeFinance

# Delete Google Search
PRODUCT_PACKAGES += \
    -QuickSearchBox

# Remove Pico TTS
PRODUCT_PACKAGES += \
    -PicoTts
		
# Remove flipboardBriefing app
PRODUCT_PACKAGES += \
    -FlipboardBriefing

# Remove EasterEgg app as NAL test
PRODUCT_PACKAGES += \
    -EasterEgg

# Remove Exchange2 : com.android.exchange
PRODUCT_PACKAGES += \
    -Exchange2

# Remove OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3

# Remove SmartCallProvider
PRODUCT_PACKAGES += \
    -SmartCallProvider \
    -HiyaService

# Facebook apps
PRODUCT_PACKAGES += \
    -Facebook_stub \
    -FBAppManager_NS \
    -appmanager.conf \
    -FBInstaller_NS \
    -FBServices

# Removed ChromeCustomizations
PRODUCT_PACKAGES += \
    -ChromeCustomizations

# Remove OMCAgent and PAI config(Stub)
PRODUCT_PACKAGES += \
    -OMCAgent5 \
    -SDMConfig \
    -PlayAutoInstallConfig

# Remove SPD Client
PRODUCT_PACKAGES += \
    -SPDClient

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# FactoryBinary doesn't need below packages.
ifneq ($(SEC_FACTORY_BUILD),true)

# Add Ignite App Package
PRODUCT_PACKAGES += \
    IGNITE

# Add Aura App Package
PRODUCT_PACKAGES += \
    AURA
	
# Add Glispa App Package
PRODUCT_PACKAGES += \
    GLISPA
	
endif	

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)

MSPAPP_ADDITIONAL_DAT_LIST := SM-A715F SM-A715FN SM-A715FM

include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
