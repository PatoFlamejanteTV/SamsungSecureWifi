QCOM_PRODUCT_DEVICE := gts4lltetmo

$(call inherit-product, device/samsung/gts4lltetmo/device.mk)
include vendor/samsung/configs/gts4llte_common/gts4llte_common.mk

#include vendor/samsung/configs/gts4lltetmo/gms_gts4lltetmo.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts4lltetmo/gms_gts4lltetmo.mk
endif

PRODUCT_NAME := gts4lltetmo
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T837T
PRODUCT_CHARACTERISTICS += tmo

# TMO 3rd party Carrier Apps
PRODUCT_PACKAGES += \
    AccessTmobile_TMO

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

# Remove WebManual
PRODUCT_PACKAGES += \
    -WebManual

# Remove upday
PRODUCT_PACKAGES += \
    -Upday
	
# Remove Facebook Apps as per Delta with P-OS
PRODUCT_PACKAGES += \
    -Facebook_stub \
    -FBAppManager_NS \
    -appmanager.conf \
    -FBInstaller_NS
