QCOM_PRODUCT_DEVICE := beyond2qltesq

$(call inherit-product, device/samsung/beyond2qltesq/device.mk)
include vendor/samsung/configs/beyond2qlte_common/beyond2qlte_common.mk

include vendor/samsung/configs/beyond2qltesq/gms_beyond2qltesq.mk

PRODUCT_NAME := beyond2qltesq
PRODUCT_DEVICE := $(QCOM_PRODUCT_DEVICE)
PRODUCT_MODEL := SM-G975U

include vendor/samsung/build/localelist/SecLocale_USA.mk

###############################################################
# SingleSKU Carrier preload apps
###############################################################
ifneq ($(filter ATT CCT CHA SPR TMB TMK USC VZW XAA, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)

include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_$(SEC_BUILD_OPTION_SINGLESKU_CUST).mk

ifneq ($(filter SPR VZW, $(SEC_BUILD_OPTION_SINGLESKU_CUST)),)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

else

ifneq ($(SEC_FACTORY_BUILD),true)
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_ATT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_CCT.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_CHA.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMB.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_TMK.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_USC.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_VZW.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_XAA.mk
include vendor/samsung/configs/$(PRODUCT_DEVICE)/$(PRODUCT_NAME)_apps_SPR_VZW.mk
endif

endif

# create empty folder for every US sales code
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/ATT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/CCT/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/CHA/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/GCF/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/SPR/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMK/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/USC/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/VZW/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/XAA/dummy.txt

# create app folders of  some priv-apps of SPR to TMB priv-app folder
ifeq ($(SEC_BUILD_ONESKU_PRODUCT),true)
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/priv-app/MobileID/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/priv-app/MobileInstaller/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/priv-app/CarrierDeviceManager/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/priv-app/SprintAndroidExtension2/dummy.txt
PRODUCT_COPY_FILES +=vendor/samsung/configs/$(PRODUCT_DEVICE)/dummy.txt:system/carrier/TMB/priv-app/SprintHub/dummy.txt
endif

# Remove VoiceRecorder
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
# Removing WebManual
PRODUCT_PACKAGES += \
    -WebManual

#VZW/ATT apps
#PRODUCT_PACKAGES += \
#    EmergencyAlert	
# need to be checked later
	
# Remove DeviceSecurity
PRODUCT_PACKAGES += \
    -SmartManager_v6_DeviceSecurity

# Remove SecSetupWizard_Global
PRODUCT_PACKAGES += \
    -SecSetupWizard_Global \
    -SetupWizard

# remove KidsHome
PRODUCT_PACKAGES += \
    -KidsHome \
    -KidsHome_Installer

# Removing Fmm
PRODUCT_PACKAGES += \
    -Fmm

# Remove VoiceNote
PRODUCT_PACKAGES += \
   -VoiceNote_5.0
   
# Add Spotify
PRODUCT_PACKAGES += \
    Spotify

# Remove upday
PRODUCT_PACKAGES += \
    -Upday

# Add USA SetupWizard Packages
PRODUCT_PACKAGES += \
    SetupWizard_USA \
    HuxExtension

# Add Subscription Calendar 
PRODUCT_PACKAGES += \
    OpenCalendar
	
# Add Secure Wi-Fi
PRODUCT_PACKAGES += \
    Fast

# Samsung SIM Unlock (SSU)
ifneq ($(filter %sq, $(TARGET_PRODUCT)), )
PRODUCT_PACKAGES += 	\
        ssud		\
        SsuService
endif
