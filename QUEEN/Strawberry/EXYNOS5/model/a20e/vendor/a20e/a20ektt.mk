$(call inherit-product, device/samsung/a20e/device.mk)
include vendor/samsung/configs/a20e_common/a20e_common.mk

include build/target/product/product_launched_with_p.mk


# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a20e/gms_a20ektt.mk
endif

PRODUCT_NAME := a20ektt
PRODUCT_DEVICE := a20e
PRODUCT_MODEL := SM-A202K

include vendor/samsung/build/localelist/SecLocale_KOR.mk

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio
	
#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter
	
# Add Helo app tracking file changes
PRODUCT_PACKAGES += \
    pre_install.appsflyer
	
# Add Booking tracking file
PRODUCT_PACKAGES += \
    .booking.data.aid
	
# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# add Facebook apps
PRODUCT_PACKAGES += \
    Facebook_stub \
    FBAppManager_NS \
    appmanager.conf \
    FBInstaller_NS

# Crane
PRODUCT_PACKAGES += \
    Crane	

# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify

# Add DMB
PRODUCT_PACKAGES += \
    Tdmb

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm
	
# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# >> KT Carrier Apps
# KT HiddenMenu
PRODUCT_PACKAGES += \
    KTHiddenMenu

# KT Service Agent
PRODUCT_PACKAGES += \
    KTServiceAgent \
    libktauthclient \
    libktuca2 \
    libuca-ril

# Copy Prebuilt KT Service Agent device_info
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/KTC/KTServiceAgent/conf/device_info_a20ektt.xml:system/kaf/device_info.xml

# KT Uninstallable Package
PRODUCT_PACKAGES += \
    CLiP \
    KTCustomerService \
    KTOneStore \
    OneStoreService \
    KTPushNotiService \
    KTServiceMenu

# KT Erasable Full Package
PRODUCT_PACKAGES += \
    AlYacAndroid_Erasable \
    Genie_Erasable \
    KTTVMobile_Erasable \
    OneNavi_Erasable \
    WhoWho_Erasable \
    KBank_Erasable \
    KTAuth_Erasable \
    GMarket_Erasable \
    SmartBill_Erasable

# KT Erasable Icon Package
PRODUCT_PACKAGES += \
    KTFamilyBox_Stub \
    KTMemberShip_Stub \
    MediaPack_Stub \
    YDataBox_Stub \
    IspPaybook_Stub
# >> KT Carrier Apps