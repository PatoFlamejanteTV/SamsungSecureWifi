QCOM_PRODUCT_DEVICE := a6pltektt

$(call inherit-product, device/samsung/a6pltektt/device.mk)
include vendor/samsung/configs/a6plte_common/a6plte_common.mk

include build/target/product/product_launched_with_o.mk

# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a6pltektt/gms_a6pltektt.mk
endif

PRODUCT_NAME := a6pltektt
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A605K

# define the default locales for phone device
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/build/localelist/SecLocale_KOR.mk
endif

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01

# Add Airtel Stub Solution
#PRODUCT_PACKAGES += \
#    AirtelStub
# Add AppLock
#PRODUCT_PACKAGES += \
#    AppLock
	
# Add Memory Solutions
#PRODUCT_PACKAGES += \
#    MemorySaver_O_Refresh
	
# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader
	
# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# KT LGU Contacts feature (KT114)
PRODUCT_PACKAGES += \
    KT114Provider2

# Add DMB
PRODUCT_PACKAGES += \
    Tdmb

# Add DMB HW Service
PRODUCT_PACKAGES += \
    vendor.samsung.hardware.tdmb@1.0-service

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
    applications/provisional/KOR/KTC/KTServiceAgent/conf/device_info_a6pltektt.xml:system/kaf/device_info.xml

# KT Uninstallable Package
PRODUCT_PACKAGES += \
    CLiP \
    KTCustomerService \
    KTOneStore \
    OneStoreService \
    KTPushNotiService \
    KTServiceMenu
	
# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# Add Flipfont for Korea
PRODUCT_PACKAGES += \
    -CoolEUKor \
    -RoseEUKor \
    Tinker \
    AppleMint

# Copy SO file for ONE store Service
PRODUCT_COPY_FILES += \
    applications/provisional/KOR/Common/OneStoreService/libARMClientService.so:system/lib/libARMClientService.so \
    applications/provisional/KOR/Common/OneStoreService/libARMPlatform.so:system/lib/libARMPlatform.so \
    applications/provisional/KOR/Common/OneStoreService/libARMService.so:system/lib/libARMService.so \
    applications/provisional/KOR/Common/OneStoreService/libCheshireCat.so:system/lib/libCheshireCat.so \
    applications/provisional/KOR/Common/OneStoreService/libSystem.so:system/lib/libSystem.so \
    applications/provisional/KOR/Common/OneStoreService/libARMClientService_arm64-v8a.so:system/lib/libARMClientService_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libARMPlatform_arm64-v8a.so:system/lib/libARMPlatform_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libARMService_arm64-v8a.so:system/lib/libARMService_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libCheshireCat_arm64-v8a.so:system/lib/libCheshireCat_arm64-v8a.so \
    applications/provisional/KOR/Common/OneStoreService/libSystem_arm64-v8a.so:system/lib/libSystem_arm64-v8a.so

# KT Erasable Full Package
PRODUCT_PACKAGES += \
    Accessory_Erasable \
    AlYacAndroid_Erasable \
    Genie_Erasable \
    KTTVMobile_Erasable \
    OneNavi_Erasable \
    KToon_Erasable \
    WhoWho_Erasable \
    KBank_Erasable \
    KTAuth_Erasable \
    GMarket_Erasable

# KT Erasable Icon Package
PRODUCT_PACKAGES += \
    KTFamilyBox_Stub \
    KTMemberShip_Stub \
    SmartBill_Stub \
    MediaPack_Stub \
    YDataBox_Stub
# << KT Carrier Apps

# Adding LinkedIn Stub
PRODUCT_PACKAGES += \
    -LinkedIn_SamsungStub

#Removed Generic Samsung Pay Framework
PRODUCT_PACKAGES += \
    -PaymentFramework

#Removed Crane
PRODUCT_PACKAGES += \
  -Crane

	
###############################################################
# FactoryBinary doesn't need KOR vendor packages.
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_KOR.mk
endif
###############################################################
