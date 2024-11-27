QCOM_PRODUCT_DEVICE := d2xq2

$(call inherit-product, device/samsung/d2xq2/device.mk)
include vendor/samsung/configs/d2xq2_common/d2xq2_common.mk

#include vendor/samsung/configs/d2xq2/gms_d2xq2sq.mk

PRODUCT_NAME := d2xq2sq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-N976U

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Remove upday
PRODUCT_PACKAGES += \
    -Upday

# Add USA SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter ATT TMB TMK, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_$(SEC_BUILD_OPTION_SINGLESKU_CUST).mk
else
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_ATT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMB.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMK.mk
endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/d2xq2/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/d2xq2/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/d2xq2/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/d2xq2/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/d2xq2/dummy.txt:system/carrier/XAA/dummy.txt
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast