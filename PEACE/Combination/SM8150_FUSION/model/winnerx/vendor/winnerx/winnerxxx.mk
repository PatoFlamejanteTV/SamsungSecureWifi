QCOM_PRODUCT_DEVICE := winnerx

$(call inherit-product, device/samsung/winnerx/device.mk)
include vendor/samsung/configs/winnerx_common/winnerx_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/winnerx/gms_winnerxxx.mk
endif

PRODUCT_NAME := winnerxxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-F907B

# for samsung hardware init
PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01

# Add PreloadAppDownload
PRODUCT_PACKAGES += \
    PreloadAppDownload

# KOR FEATURE : QR reader
PRODUCT_PACKAGES += \
    QRreader

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# Add SKT-DM (LAWMO)
PRODUCT_PACKAGES += \
    SKTFindLostPhone

# Add OneDrive removable
PRODUCT_PACKAGES += \
    -OneDrive_Samsung_v3 \
    OneDrive_Samsung_v3_Removable

# change facebook to removable
PRODUCT_PACKAGES += \
    -Facebook_stub \
    Facebook_stub_Removable

# Remove HiyaService for Smart Call 
PRODUCT_PACKAGES += \
    -HiyaService

# HiddenNetworkSetting 
PRODUCT_PACKAGES += \
    HiddenNetworkSetting

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify	

# Add BlockchainBasicKit
PRODUCT_PACKAGES += \
    BlockchainBasicKit
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
