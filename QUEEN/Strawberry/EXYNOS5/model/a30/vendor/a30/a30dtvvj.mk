include vendor/samsung/configs/a30/a30dd.mk

PRODUCT_NAME := a30dtvvj
PRODUCT_MODEL := SM-A305GT

# ++ LTN MobileTV
PRODUCT_PACKAGES += \
    MobileTV_LTN

PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.latin.dtv.rc:system/etc/init/init.dtv.rc
# -- LTN MobileTV

# Add Samsung TTS
PRODUCT_PACKAGES += -smt_hi_IN_f00
PRODUCT_PACKAGES += -smt_en_IN_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Remove LogWriter Solution
PRODUCT_PACKAGES += \
    -LogWriter

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
# add UDS 
PRODUCT_PACKAGES += \
UltraDataSaving_O
