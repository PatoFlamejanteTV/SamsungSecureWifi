BUILD_SYMLINK_TO_CARRIER := true

$(call inherit-product, device/samsung/a51x/device.mk)
include vendor/samsung/configs/a51x_common/a51x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/a51x/gms_a51xsq.mk
endif

PRODUCT_NAME := a51xsq
PRODUCT_DEVICE := a51x
PRODUCT_MODEL := SM-A516U

include vendor/samsung/build/localelist/SecLocale_USA.mk

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_US_dict_f01

# Add Samsung+ removable
PRODUCT_PACKAGES += \
    -SamsungMembers \
    SAMSUNG_PLUS_REMOVABLE

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar

# Add Samsung Blockchain Keystore in "Settings - Biometrics and security"
# Only for EUR, KOR, USA, CAN country , Not included LDU device 
PRODUCT_PACKAGES += \
    BlockchainBasicKit

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter ATT AIO SPR TMB TMK USC, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_$(SEC_BUILD_OPTION_SINGLESKU_CUST).mk

else

# FactoryBinary doesn't need carrier packages.
ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_ATT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMB.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMK.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_USC.mk
endif

endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51x/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51x/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51x/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51x/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51x/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/a51x/dummy.txt:system/carrier/XAA/dummy.txt


###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
