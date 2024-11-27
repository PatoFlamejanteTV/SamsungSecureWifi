QCOM_PRODUCT_DEVICE := beyond0qltechn

$(call inherit-product, device/samsung/beyond0qltechn/device.mk)
include vendor/samsung/configs/beyond0qlte_common/beyond0qlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyond0qltechn/gms_beyond0qltezh.mk
endif

PRODUCT_NAME := beyond0qltezh
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G9700
ifeq ($(shell secgetspf SEC_PRODUCT_FEATURE_COMMON_FAKE_BINARY), TRUE)
PRODUCT_MODEL := SM-A9200
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00

# Adding WebManual
PRODUCT_PACKAGES += \
    WebManual

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio_P
	
# Add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify	

# [CMC] Samsung CMC
  PRODUCT_PACKAGES += \
    MdecService
	
#Add BixbyTouch
PRODUCT_PACKAGES += \
    BixbyTouch
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
