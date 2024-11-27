$(call inherit-product, device/samsung/d1/device.mk)
include vendor/samsung/configs/d1_common/d1_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/d1/gms_d1ldu.mk
endif

PRODUCT_NAME := d1ldu
PRODUCT_DEVICE := d1
PRODUCT_MODEL := SM-N970X

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
	
# Crane
PRODUCT_PACKAGES += \
  Crane
  
# Biometrics for LDU
PRODUCT_COPY_FILES += \
    -frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    -frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml
    
PRODUCT_PACKAGES += -BioFaceService

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# Add BlockchainBasicKit
PRODUCT_PACKAGES += \
    BlockchainBasicKit
    
#remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v10

# Remove eSE features
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp

# GNSS
# GPS Evolution RIL interface.
PRODUCT_PACKAGES += \
    -libwrappergps

# BCM47755 GNSS configuration
PRODUCT_COPY_FILES += \
	-vendor/samsung/hardware/gnss/broadcom/gsm/47755/config/gps.GnssYearOfHardware2016.ACSD.xml:vendor/etc/gnss/gps.xml \
	vendor/samsung/hardware/gnss/common/EvoRIL/fake_libwrappergps_dualsim.so:vendor/lib64/libwrappergps.so \
	vendor/samsung/hardware/gnss/broadcom/gsm/47755/config/gps.ged.xml:vendor/etc/gnss/gps.xml

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################

# recovery for ldu
RECOVERY_FOR_LDU_BINARAY := true
RECOVERY_DELETE_USER_DATA := true
