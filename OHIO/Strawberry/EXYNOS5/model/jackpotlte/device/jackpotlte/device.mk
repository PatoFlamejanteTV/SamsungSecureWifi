# Figure out where we are.
define sec-my-dir
$(strip \
  $(eval md_file_ := $$(lastword $$(MAKEFILE_LIST))) \
  $(patsubst %/,%,$(dir $(md_file_))) \
)
endef

# Init files
PRODUCT_COPY_FILES += \
    device/samsung/jackpotlte_common/fstab.jackpotlte:root/fstab.samsungexynos7885 \
    device/samsung/jackpotlte_common/init.jackpotlte.rc:root/init.samsungexynos7885.rc \
    device/samsung/jackpotlte_common/init.jackpotlte.usb.rc:root/init.samsungexynos7885.usb.rc \
    device/samsung/jackpotlte_common/init.usb.configfs.rc:root/init.usb.configfs.rc \
    device/samsung/jackpotlte_common/init.ss327.rc:root/init.baseband.rc \
    device/samsung/jackpotlte/init.carrier.rc:root/init.carrier.rc

include device/samsung/jackpotlte_common/device_common.mk

# Audio Framework
include device/samsung/jackpotlte/AudioData/SecAudioData.mk

# NFC
include vendor/samsung/configs/nfc/samsung/s3fwrn81/nfc.mk

# VaultKeeper
include vendor/samsung/external/vaultkeeper/configs/vk_mobicore.mk

#FM Radio
PRODUCT_PACKAGES += \
    libfmradio_jni

# VT
PRODUCT_PACKAGES += \
    libvtmanagerjar \
    libvtmanager \
    libvtstack

# GNSS configuration
ifeq ($(SEC_FACTORY_BUILD),true)
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.debug.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
else
PRODUCT_COPY_FILES += \
    vendor/samsung/hardware/gnss/slsi/lassen/config/lassen.cfg:$(TARGET_COPY_OUT_VENDOR)/etc/gnss/gps.cfg
endif