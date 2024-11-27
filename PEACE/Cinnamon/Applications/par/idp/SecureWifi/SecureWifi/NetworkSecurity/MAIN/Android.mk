LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
$(info ***********************************)
$(info *              FAST               *)
$(info ***********************************)

LOCAL_MODULE := Fast
LOCAL_MODULE_TAGS := optional
ifneq ($(filter a6plte% a6lte% a10%,$(TARGET_PRODUCT)),)
# A6+ eur open
LOCAL_ARTIFACT_ID := Fast-500001-5.0.00
else
LOCAL_ARTIFACT_ID := Fast-500004-5.0.00
endif
LOCAL_SRC_FILES := Fast-release.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
ifeq ($(call spf_get,SEC_PRODUCT_FEATURE_COMMON_CONFIG_SEP_CATEGORY), sep_lite)
    LOCAL_CERTIFICATE := R_platform
else
    LOCAL_CERTIFICATE := platform
endif
LOCAL_PRIVILEGED_MODULE := true
#For avoid pre-optimization
ifeq (true,$(call spf_check,SEC_PRODUCT_FEATURE_SECURITY_SUPPORT_PROCA,FALSE))
    LOCAL_DEX_PREOPT := false
endif
LOCAL_REQUIRED_MODULES := privapp-permissions-com.samsung.android.fast.xml

include $(BUILD_PREBUILT)
include $(CLEAR_VARS)
LOCAL_MODULE := privapp-permissions-com.samsung.android.fast.xml
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)
