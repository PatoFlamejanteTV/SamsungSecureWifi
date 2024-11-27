# LPM Animation
addlpmSpiResource = $(if \
    $(and $(findstring $(1),$(findstring $(1),$(PRODUCT_COPY_FILES))),$(findstring $(findstring $(1),$(PRODUCT_COPY_FILES)),$(1))),,$(1))

LPM_LOOK_TYPE := BeyondLook
LPM_RESOLUTION := 1200_1920
PRODUCT_PACKAGES += \
    $(call addlpmSpiResource,slow_charging_text.spi)\
    $(call addlpmSpiResource,incomplete_connect_text.spi)\
    $(call addlpmSpiResource,incompatible_charger_text.spi)\
    $(call addlpmSpiResource,temperature_text.spi)\
    $(call addlpmSpiResource,temperature_text2.spi)\
    $(call addlpmSpiResource,fully_charged.spi)\
    $(call addlpmSpiResource,spare_digit.spi)\
    $(call addlpmSpiResource,spare_hr.spi)\
    $(call addlpmSpiResource,spare_min.spi)\
    $(call addlpmSpiResource,spare_text.spi)\
    $(call addlpmSpiResource,fast_charging_vzw.spi)

# Init files
$(call inherit-product, device/samsung/gta4s/device.mk)
include vendor/samsung/configs/gta4s_common/gta4s_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately 
include vendor/samsung/configs/gta4s/gms_gta4ssq.mk
endif

# define google api level for google approval
include build/target/product/product_launched_with_p.mk

PRODUCT_NAME := gta4ssq
PRODUCT_DEVICE := gta4s
PRODUCT_MODEL := SM-T307U

include vendor/samsung/build/localelist/SecLocale_USA.mk



# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    -NSDSWebApp

# Microsoft apps
PRODUCT_PACKAGES += \
    -MSSkype_stub

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    UnifiedWFC
endif

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00

### Sprint OMADM & Other Apps 

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################
