QCOM_PRODUCT_DEVICE := a9y18qlte

$(call inherit-product, device/samsung/a9y18qlte/device.mk)
include vendor/samsung/configs/a9y18qlte_common/a9y18qlte_common.mk

# define google api level for google approval
include build/target/product/product_launched_with_o.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a9y18qlte/gms_a9y18qltexx.mk
endif

PRODUCT_NAME := a9y18qltexx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A920F

# for samsung hardware init
PRODUCT_COPY_FILES += \
    device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

#Crane
PRODUCT_PACKAGES += \
    Crane

#Add LogWriter Solution
PRODUCT_PACKAGES += \
    LogWriter

#Add AirtelStub Solution
PRODUCT_PACKAGES += \
    AirtelStub

#Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast