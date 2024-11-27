include vendor/samsung/configs/a6plte/a6pltexx.mk

PRODUCT_NAME := a6pltedx
PRODUCT_MODEL := SM-A605G

# Add MemorySaver Solution
PRODUCT_PACKAGES += \
    MemorySaver_O_Refresh

#Add LogWriter Solution
PRODUCT_PACKAGES += \
	LogWriter

#Add Amazon Shopping Solution
PRODUCT_PACKAGES += \
	Amazon_Shopping

# Add Samsung TTS
PRODUCT_PACKAGES += smt_vi_VN_f00
PRODUCT_PACKAGES += -smt_en_GB_f00
PRODUCT_PACKAGES += -smt_de_DE_f00
PRODUCT_PACKAGES += -smt_fr_FR_f00
PRODUCT_PACKAGES += -smt_it_IT_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_es_ES_f00

#Add Dailyhunt Solution
PRODUCT_PACKAGES += \
	Dailyhunt
	
# Add SamsungPayStubMini
PRODUCT_PACKAGES += \
    SamsungPayStubMini
	
# Remove Secure Wi-Fi
PRODUCT_PACKAGES += \
    -Fast