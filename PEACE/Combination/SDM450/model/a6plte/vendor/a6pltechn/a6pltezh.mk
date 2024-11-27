QCOM_PRODUCT_DEVICE := a6pltechn

$(call inherit-product, device/samsung/a6pltechn/device.mk)
include vendor/samsung/configs/a6plte_common/a6plte_common.mk

include build/target/product/product_launched_with_o.mk


# define google api level for google approval

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a6pltechn/gms_a6pltezh.mk
endif

PRODUCT_NAME := a6pltezh
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A6050

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P

# SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00
PRODUCT_PACKAGES += smt_en_GB_f00

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add Airtel Stub Solution
PRODUCT_PACKAGES += \
    AirtelStub
# Add AppLock
PRODUCT_PACKAGES += \
    AppLock
	
# Add Memory Solutions
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh
	
# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# For MSP APP of NFC One binary
MSPAPP_ADDITIONAL_DAT_LIST := SM-A605F

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
