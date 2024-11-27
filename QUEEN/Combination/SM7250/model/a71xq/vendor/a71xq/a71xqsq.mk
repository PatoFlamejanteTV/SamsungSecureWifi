QCOM_PRODUCT_DEVICE := a71xq

$(call inherit-product, device/samsung/a71xq/device.mk)
include vendor/samsung/configs/a71xq_common/a71xq_common.mk

#include vendor/samsung/configs/a71xq/gms_a71xqsq.mk

PRODUCT_NAME := a71xqsq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A716U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Removed WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################