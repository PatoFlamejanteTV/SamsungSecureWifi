QCOM_PRODUCT_DEVICE := a01q

$(call inherit-product, device/samsung/a01q/device.mk)
include vendor/samsung/configs/a01q_common/a01q_common.mk

# removing gms for bulk binary
# ifeq ($(shell secgetspf SEC_PRODUCT_FEATURE_BUILD_JDM_BULK_BINARY), FALSE)
include vendor/samsung/configs/a01q/gms_a01qxx.mk
# endif

PRODUCT_NAME := a01qxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-A015F

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Radio App Package
PRODUCT_PACKAGES += \
    HybridRadio

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
	
ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung IMS
PRODUCT_PACKAGES += \
    RcsSettings
endif

# Add Handwriting
PRODUCT_PACKAGES += \
    HandwritingService \
    libSDKRecognitionText.spensdk.samsung \
    public.libraries-spensdk.samsung.txt





# Only for making bulk binary
###############################################################
ifeq ($(shell secgetspf SEC_PRODUCT_FEATURE_BUILD_JDM_BULK_BINARY), TRUE)

# bulk common mk
include vendor/samsung/configs/a01q/a01q_bulk.mk
        
endif #end of SEC_JDM_BULK_BINARY
###############################################################
# end of line
# PLEASE DO NOT ADD LINE BELOW
