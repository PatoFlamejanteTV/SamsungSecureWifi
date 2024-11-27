$(call inherit-product, device/samsung/greatqltesq/device.mk)
include vendor/samsung/configs/greatqlte_common/greatqlte_common.mk

include build/target/product/product_launched_with_n_mr1.mk

# Include gms packages depending on product seperately
include vendor/samsung/configs/greatqltesq/gms_greatqlteldusq.mk

PRODUCT_NAME := greatqlteldusq
PRODUCT_DEVICE := greatqltesq
PRODUCT_MODEL := SM-N950XU
PRODUCT_LOCALES := en_US es_US fr_FR de_DE it_IT vi_VN ko_KR zh_CN zh_TW zh_HK ja_JP pt_BR
PRODUCT_FINGERPRINT_TYPE := pilot

# for samsung hardware init
PRODUCT_COPY_FILES += \
    device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:root/init.carrier.rc


# PAI Stub
PRODUCT_PACKAGES += \
    PlayAutoInstallConfig

# Add YELP panel
PRODUCT_PACKAGES += \
    YelpPanel

# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_6.0_Removable \
    SBrowser_6.0


# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00


# Fingeprint Manager Service
PRODUCT_PACKAGES += -FingerprintService2
PRODUCT_COPY_FILES += \
    -frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    -frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml

# Add Samsung Pay App
PRODUCT_PACKAGES += \
    -SamsungPayStub \
    -SamsungPayApp

# Remove OneDrive
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3
    
# Add S Voice IME
PRODUCT_PACKAGES += \
    SVoiceIME

# Find My Mobile
PRODUCT_PACKAGES += \
    -Fmm

# recovery for ldu
RECOVERY_FOR_LDU_BINARAY := true
RECOVERY_DELETE_USER_DATA := true

# Remove NS-FLP (Samsung Location SDK) at LDU
PRODUCT_PACKAGES += \
    -NSFusedLocation_v3.0_DREAM
PRODUCT_COPY_FILES += \
    -frameworks/base/data/etc/com.sec.feature.nsflp_level_301.xml:system/etc/permissions/com.sec.feature.nsflp_level_301.xml

# Remove SecureFolder
PRODUCT_PACKAGES += \
   -SecureFolder

# Remove Coreapps and LinkShare
PRODUCT_PACKAGES += \
   -CoreApps_SDK_2017 \
   -LinkSharing_v34

# Remove Samsung KMS Agent
PRODUCT_PACKAGES += \
    -libsem_jni \
    -SKMSAgent

# Remove eSE MW
PRODUCT_PACKAGES += \
    -libspictrl

# Remove SEM
PRODUCT_PACKAGES += \
    -libsec_sem \
    -sem_daemon \
    -SEMFactoryApp 

PRODUCT_PACKAGES += \
    SMusic

# recovery for ldu
RECOVERY_FOR_LDU_BINARAY := true
RECOVERY_DELETE_USER_DATA := true
