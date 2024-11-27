include vendor/samsung/configs/a30/a30dd.mk

PRODUCT_NAME := a30ub
PRODUCT_MODEL := SM-A305G

# Add Samsung TTS
PRODUCT_PACKAGES += -smt_hi_IN_f00
PRODUCT_PACKAGES += -smt_en_IN_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Remove LogWriter Solution
PRODUCT_PACKAGES += \
    -LogWriter

# add UDS 
PRODUCT_PACKAGES += \
UltraDataSaving_O