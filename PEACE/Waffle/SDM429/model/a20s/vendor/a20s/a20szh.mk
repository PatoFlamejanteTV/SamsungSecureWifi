QCOM_PRODUCT_DEVICE := a20s

$(call inherit-product, device/samsung/a20s/device.mk)
include vendor/samsung/configs/a20s_common/a20s_common.mk

include vendor/samsung/configs/a20s/gms_a20szh.mk

PRODUCT_NAME := a20szh
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A2070

include vendor/samsung/build/localelist/SecLocale_CHN.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_zh_CN_f00
PRODUCT_PACKAGES += smt_zh_TW_f00
PRODUCT_PACKAGES += smt_zh_HK_f00
PRODUCT_PACKAGES += smt_en_GB_f00

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

# Add AppLock Solution
PRODUCT_PACKAGES += \
    AppLock

# Add Airtel Stub Solution
PRODUCT_PACKAGES += \
    AirtelStub

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_O
