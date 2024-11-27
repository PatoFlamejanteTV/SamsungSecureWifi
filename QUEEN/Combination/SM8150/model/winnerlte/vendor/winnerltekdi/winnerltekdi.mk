QCOM_PRODUCT_DEVICE := winnerltekdi

$(call inherit-product, device/samsung/winnerltekdi/device.mk)
include vendor/samsung/configs/winnerlte_common/winnerlte_common.mk

# FactoryBinary doesn't need gms packages.
ifneq ($(SEC_FACTORY_BUILD),true)
# Include gms packages depending on product seperately
include vendor/samsung/configs/winnerltekdi/gms_winnerltekdi.mk
endif

PRODUCT_NAME := winnerltekdi
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SCV44
PRODUCT_BRAND := KDDI
PRODUCT_FINGERPRINT_TYPE := jpn_kdi

include vendor/samsung/build/localelist/SecLocale_KDI.mk

# for samsung hardware init
# PRODUCT_COPY_FILES += \
#    device/samsung/$(PRODUCT_DEVICE)/init.$(PRODUCT_DEVICE).rc:vendor/etc/init/hw/init.carrier.rc

# Add Samsung TTS
PRODUCT_PACKAGES += smt_en_GB_f00
PRODUCT_PACKAGES += smt_de_DE_f00
PRODUCT_PACKAGES += smt_fr_FR_f00
PRODUCT_PACKAGES += smt_it_IT_f00
PRODUCT_PACKAGES += smt_ru_RU_f00
PRODUCT_PACKAGES += smt_es_ES_f00
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00

# Setup SBrowser removable
PRODUCT_PACKAGES += \
    -SBrowser_11.0 \
    SBrowser_11.0_Removable

# [IMS] Samsung IMS (IMS 6.0)
PRODUCT_PACKAGES += \
    NSDSWebApp

# [MDEC] Samsung MDEC
  PRODUCT_PACKAGES += \
    MdecService

# SVoiceIME
PRODUCT_PACKAGES += \
    SVoiceIME
	
# Crane
PRODUCT_PACKAGES += \
    Crane

# Omc(Vanilla Device Customization)
PRODUCT_PACKAGES += \
    Omc

ifneq ($(SEC_FACTORY_BUILD),true)
# [IMS] Samsung RcsSettings
PRODUCT_PACKAGES += \
    RcsSettings
endif

# SamsungEuicc Service and Client
PRODUCT_PACKAGES += \
    EuiccService
ifeq (eng, $(TARGET_BUILD_VARIANT))
PRODUCT_PACKAGES += \
    EuiccClient
endif

###############################################################
# Factory Binary does not need below
###############################################################
ifeq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/fac_vendor_common/fac_vendor_EUR.mk
endif
###############################################################
