QCOM_PRODUCT_DEVICE := d2q

$(call inherit-product, device/samsung/d2q/device.mk)
include vendor/samsung/configs/d2q_common/d2q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/d2q/gms_d2qldusq.mk
endif


PRODUCT_NAME := d2qldusq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N975XU

include vendor/samsung/build/localelist/SecLocale_USA.mk



# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Remove eSE features
PRODUCT_PACKAGES += \
    -sem_daemon \
    -SEMFactoryApp
    
#remove LinkSharing
PRODUCT_PACKAGES += \
  -CoreApps_EOP \
  -LinkSharing_v10

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Remove SamsungMembers
PRODUCT_PACKAGES += -SamsungMembers_Removable

# Remove Samsung Pass
PRODUCT_PACKAGES += \
    -SamsungPass
RECOVERY_FOR_LDU_BINARAY := true
RECOVERY_DELETE_USER_DATA := true

# Biometrics for LDU
PRODUCT_COPY_FILES += \
    -frameworks/base/data/etc/com.sec.feature.fingerprint_manager_service.xml:system/etc/permissions/com.sec.feature.fingerprint_manager_service.xml \
    -frameworks/native/data/etc/android.hardware.fingerprint.xml:system/etc/permissions/android.hardware.fingerprint.xml
    
PRODUCT_PACKAGES += -BioFaceService

# Remove Microsoft OfficeMobile Stub
PRODUCT_PACKAGES += \
    -OfficeMobile_SamsungStub

# Remove Samsung Pay Stub
PRODUCT_PACKAGES += \
    -SamsungPayStub
