QCOM_PRODUCT_DEVICE := a01q

$(call inherit-product, device/samsung/a01q/device.mk)
include vendor/samsung/configs/a01q_common/a01q_common.mk

include vendor/samsung/configs/a01q/gms_a01qtfn.mk

PRODUCT_NAME := a01qtfn
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-S111DL

include vendor/samsung/build/localelist/SecLocale_USA.mk


# for InputEventApp
PRODUCT_PACKAGES += \
    InputEventApp \
    libDiagService_

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
endif

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
>>>> ORIGINAL //QUEEN/Hotdog/SDM439/model/a01q/vendor/a01q/a01qtfn.mk#5
==== THEIRS //QUEEN/Hotdog/SDM439/model/a01q/vendor/a01q/a01qtfn.mk#6
  
# Add TFN Apps
PRODUCT_PACKAGES += \
    DevicePulse \
	Ignite_TFN \
	InboxApp \
    MyAccountInstaller \
    MySitesTFN \
	TracfoneWifiUtil
	
   
   
# TFN Third Party APPS
PRODUCT_PACKAGES += \
    Weather \
	