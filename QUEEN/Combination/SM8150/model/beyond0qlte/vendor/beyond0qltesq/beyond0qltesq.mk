QCOM_PRODUCT_DEVICE := beyond0qltesq

$(call inherit-product, device/samsung/beyond0qltesq/device.mk)
include vendor/samsung/configs/beyond0qlte_common/beyond0qlte_common.mk

#include vendor/samsung/configs/beyond0qltesq/gms_beyond0qltesq.mk

PRODUCT_NAME := beyond0qltesq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G970U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   

# Add BlockchainBasicKit
PRODUCT_PACKAGES += \
    BlockchainBasicKit

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio
	
# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio_P
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio_P
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Remove USP for customer delivery (-e omce)
ifneq ($(filter omce, $(SEC_BUILD_OPTION_EXTRA_BUILD_CONFIG)),)
PRODUCT_PACKAGES += \
    -HybridRadio_P
endif

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter ATT CCT CHA SPR TMB TMK USC VZW XAA, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_$(SEC_BUILD_OPTION_SINGLESKU_CUST).mk

ifneq ($(filter SPR VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

else

ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_ATT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_CCT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_CHA.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMB.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMK.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_USC.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_VZW.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_XAA.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/CCT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/CHA/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/XAA/dummy.txt