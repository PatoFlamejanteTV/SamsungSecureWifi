QCOM_PRODUCT_DEVICE := a01q

$(call inherit-product, device/samsung/a01q/device.mk)
include vendor/samsung/configs/a01q_common/a01q_common.mk

include vendor/samsung/configs/a01q/gms_a01qmtr.mk

PRODUCT_NAME := a01qmtr
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A015T1

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Setup SBrowser not removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0_Removable \
    SBrowser_11.0

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
# MNO Team II 3rd party VUX apps
PRODUCT_PACKAGES += \
    AdaptClient \
    MetroZone_MTR \
    MyMetro_MTR

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
# Add nextradio wrapper package
PRODUCT_PACKAGES += \
    nextradio
	
# Add Radio App Package
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_PACKAGES += \
    HybridRadio
else ifeq ($(SEC_BUILD_OPTION_TYPE), eng)
PRODUCT_PACKAGES += \
    HybridRadio
else
PRODUCT_PACKAGES += \
    -HybridRadio
endif

# MNO Team II 3rd party VUX apps
PRODUCT_PACKAGES += \
	Lookout_MTR 