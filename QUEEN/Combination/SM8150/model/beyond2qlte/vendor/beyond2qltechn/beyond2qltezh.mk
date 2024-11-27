QCOM_PRODUCT_DEVICE := beyond2qltechn

$(call inherit-product, device/samsung/beyond2qltechn/device.mk)
include vendor/samsung/configs/beyond2qlte_common/beyond2qlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/beyond2qltechn/gms_beyond2qltezh.mk
endif

PRODUCT_NAME := beyond2qltezh
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G9750
ifeq ($(shell secgetspf SEC_PRODUCT_FEATURE_COMMON_FAKE_BINARY), TRUE)
PRODUCT_MODEL := SM-G9650
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00
	   
# Add WebManual
PRODUCT_PACKAGES += \
    WebManual
	
# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Add SamsungMembers
PRODUCT_PACKAGES += \
    SamsungMembers_Removable	

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME

# Remove Upday
PRODUCT_PACKAGES += \
    -Upday

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
