$(call inherit-product, device/samsung/haechiy19/device.mk)
include vendor/samsung/configs/haechiy19_common/haechiy19_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/haechiy19/gms_haechiy19dx.mk
endif

PRODUCT_NAME := haechiy19dx
PRODUCT_DEVICE := haechiy19
PRODUCT_MODEL := SM-G889G

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################

ifneq ($(SEC_FACTORY_BUILD),true)
	
# Expway eMBMS package
PRODUCT_PACKAGES += \
   EweMBMSServer_TEL	
   
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings

endif

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add CNN panel
PRODUCT_PACKAGES += \
    CnnPanel
    
# [CMC] Samsung CMC
  PRODUCT_PACKAGES += \
    MdecService

# Critical Communications
false_startup_image_path := bootable/bootloader/exynos/images/official/haechiy19
PRODUCT_COPY_FILES += \
    $(false_startup_image_path)/logo_SM-G889F.jpg:system/media/logo_SM-G889F.jpg