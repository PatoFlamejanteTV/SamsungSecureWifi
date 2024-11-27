LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
$(info ***********************************)
$(info *              FAST               *)
$(info ***********************************)

LOCAL_MODULE := Fast
LOCAL_MODULE_TAGS := optional
LOCAL_ARTIFACT_ID := Fast-600003-6.5.00
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
LOCAL_DEX_PREOPT := false
LOCAL_REQUIRED_MODULES := privapp-permissions-com.samsung.android.fast.xml

include $(BUILD_PREBUILT)
include $(CLEAR_VARS)
LOCAL_MODULE := privapp-permissions-com.samsung.android.fast.xml
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)
