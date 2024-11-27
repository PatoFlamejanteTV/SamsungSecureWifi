QCOM_PRODUCT_DEVICE := a70q

$(call inherit-product, device/samsung/a70q/device.mk)
include vendor/samsung/configs/a70q_common/a70q_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/a70q/gms_a70qxx.mk
endif

PRODUCT_NAME := a70qxx
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := A70Q-EUR

PRODUCT_COPY_FILES += \
	device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_NAME).rc:vendor/etc/init/hw/init.carrier.rc
	
# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

# Crane
PRODUCT_PACKAGES += \
    Crane

# Add S Voice IME
PRODUCT_PACKAGES += \
    SVoiceIME

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00

# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast
