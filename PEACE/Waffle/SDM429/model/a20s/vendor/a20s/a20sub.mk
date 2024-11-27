include vendor/samsung/configs/a20s/a20sxx.mk

PRODUCT_NAME := a20sub
PRODUCT_MODEL := SM-A207M

# Add Samsung TTS
PRODUCT_PACKAGES += smt_es_MX_f00
PRODUCT_PACKAGES += smt_pt_BR_f00
PRODUCT_PACKAGES += -smt_en_GB_f00
PRODUCT_PACKAGES += -smt_de_DE_f00
PRODUCT_PACKAGES += -smt_fr_FR_f00
PRODUCT_PACKAGES += -smt_it_IT_f00
PRODUCT_PACKAGES += -smt_ru_RU_f00
PRODUCT_PACKAGES += -smt_es_ES_f00

# Add UltraDataSaving
PRODUCT_PACKAGES += \
    UltraDataSaving_P

# Remove UltraDataSaving
PRODUCT_PACKAGES += \
    -UltraDataSaving_O

# Add Booking.com for ZTO
PRODUCT_PACKAGES += \
    Booking_deletable

# add Booking channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/OPEN/Booking/zto/.booking.data.aid:system/etc/.booking.data.aid

# Add El Tiempo for COO
PRODUCT_PACKAGES += \
    ElTiempo

# Add 99taxis.com for ZTO
PRODUCT_PACKAGES += \
    99Taxis
# add 99Taxis channel file
PRODUCT_COPY_FILES += \
    applications/par/edp/LATIN/Taxis99/pre_install.appsflyer:system/etc/pre_install.appsflyer

# Remove Secure Wi-Fi
PRODUCT_PACKAGES += \
    -Fast

# Add Samsung Email
PRODUCT_PACKAGES += \
    SecEmail_P \
    com.samsung.android.email.provider.xml
