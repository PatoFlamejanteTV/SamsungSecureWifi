QCOM_PRODUCT_DEVICE := gts6x

$(call inherit-product, device/samsung/gts6x/device.mk)
include vendor/samsung/configs/gts6x_common/gts6x_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/gts6x/gms_gts6xkx.mk
endif

PRODUCT_NAME := gts6xkx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-T866N

include vendor/samsung/build/localelist/SecLocale_KOR.mk

	
# Add Yes24ebook
PRODUCT_PACKAGES += \
    Yes24ebook_Erasable

# Add Joins
PRODUCT_PACKAGES += \
    Joins_Erasable

# Add IMS-DM
PRODUCT_PACKAGES += \
    OpenImsDm

#VZW/ATT apps
PRODUCT_PACKAGES += \
    EmergencyAlert	
	
# Remove Spotify
PRODUCT_PACKAGES += \
    -Spotify
	
# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar
    
###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_USA.mk
endif
###############################################################

# Add Samsung TTS
PRODUCT_PACKAGES += smt_ko_KR_f00
PRODUCT_PACKAGES += smt_en_US_dict_f01

# [CMC] Samsung CMC
PRODUCT_PACKAGES += \
    MdecService

