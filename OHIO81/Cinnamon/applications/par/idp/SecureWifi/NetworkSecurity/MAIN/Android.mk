LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
$(info ***********************************)
$(info *              FAST               *)
$(info ***********************************)

LOCAL_MODULE := Fast
LOCAL_MODULE_TAGS := optional
LOCAL_ARTIFACT_ID := Fast-300010-3.0.00
LOCAL_SRC_FILES := Fast-release.apk
LOCAL_MODULE_CLASS := APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_CERTIFICATE := platform
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
