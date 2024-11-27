package com.android.systemui;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.FactoryTest;
import android.os.Debug;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.android.systemui.util.DeviceType;
import com.android.systemui.util.LogUtil;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;

import com.sec.android.app.CscFeatureTagCommon;
import com.sec.android.app.CscFeatureTagLockScreen;
import com.sec.android.app.CscFeatureTagMessage;
import com.sec.android.app.CscFeatureTagRIL;
import com.sec.android.app.CscFeatureTagSystemUI;
import com.sec.android.app.CscFeatureTagWifi;
import com.sec.android.app.SecProductFeature_COMMON;
import com.sec.android.app.SecProductFeature_FINGERPRINT;
import com.sec.android.app.SecProductFeature_FRAMEWORK;
import com.sec.android.app.SecProductFeature_GRAPHICS;
import com.sec.android.app.SecProductFeature_KNOX;
import com.sec.android.app.SecProductFeature_LCD;
import com.sec.android.app.SecProductFeature_LOCKSCREEN;
import com.sec.android.app.SecProductFeature_RIL;
import com.sec.android.app.SecProductFeature_SETTINGS;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Rune {

    private static final String TAG = "Rune";

    //--------------------------------------------------------//
    // Common Constants                                       //
    //--------------------------------------------------------//
    private static final boolean COMMON = true;
    private static final boolean LOCKSCREEN = true;
    private static final boolean QUICKPANEL = true;
    private static final boolean BASIC = true;
    private static final boolean WALLPAPER = true;
    // ----------
    // Module Feature
    // ----------
    private static final boolean KEYGUARD   = LOCKSCREEN;
    private static final boolean SECURITY   = LOCKSCREEN;
    private static final boolean LOCKUI     = LOCKSCREEN;
    private static final boolean AOD        = LOCKSCREEN;
    private static final boolean COVER      = LOCKSCREEN;
    private static final boolean STATBAR = QUICKPANEL;
    private static final boolean QPANEL = QUICKPANEL;
    private static final boolean NOTI = QUICKPANEL;
    private static final boolean PWRUI = QUICKPANEL;
    private static final boolean RECENT = BASIC;
    private static final boolean NAVBAR = BASIC;
    private static final boolean ASSIST = BASIC;
    private static final boolean VOLUME = BASIC;
    private static final boolean WPAPER = WALLPAPER;
    private static final boolean GLOBALACTIONS = BASIC;

    /* FEATURE_DESCRIPTION : Common feature for check product name  */
    public static final String SYSUI_COUNTRY_ISO = SemCscFeature.getInstance().getString("CountryISO", "");
    /* FEATURE_DESCRIPTION : Common feature to check the tablet model */
    public static final boolean SYSUI_IS_TABLET_DEVICE = DeviceType.isTablet();
    /* FEATURE_DESCRIPTION : Common feature for check product name  */
    private static final String SYSUI_PRODUCT_NAME = SystemProperties.get("ro.product.name","");
    /* FEATURE_DESCRIPTION : Common feature to check build type */
    public static final boolean SYSUI_IS_ENG_BUILD = "eng".equals(Build.TYPE);
    /* FEATURE_DESCRIPTION : Common feature for multi sim device */
    public static final boolean SYSUI_SUPPORT_MULTI_SIM_DEVICE = DeviceType.isMultiSimSupported();
    /* FEATURE DESCRIPTION : Support DeX */
    public static final boolean SYSUI_SUPPORT_DEX = COMMON;
    /* FEATURE_DESCRIPTION : Common feature for Samsung Analytics */
    public static final boolean SYSUI_SUPPORT_SAMSUNG_ANALYTICS = COMMON;
    /* FEATURE_DESCRIPTION : Support main thread monitor to detect ANR */
    public static final boolean SYSUI_SUPPORT_UI_THREAD_MONITOR = COMMON && (Debug.semIsProductDev() || LogUtil.isDebugLevelMid());
    /* FEATURE_DESCRIPTION : Common feature for SystemUI tests */
    public static final boolean SYSUI_SUPPORT_TESTS = COMMON;
    /* FEATURE_DESCRIPTION : Common feature for KnoxStateMonitor */
    public static final boolean SYSUI_SUPPORT_KNOX_MONITOR = COMMON;
    /* FEATURE_DESCRIPTION : Common feature for SPlugin */
    public static final boolean SYSUI_SUPPORT_SPLUGIN = COMMON;
    /* FEATURE_DESCRIPTION : Support BixBy */
    public static final boolean SYSUI_SUPPORT_BIXBY = COMMON && SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_BIXBY;
    /* FEATURE_DESCRIPTION : Support Sec log */
    public static final boolean SYSUI_SUPPORT_SEC_LOG = COMMON;
    /* FEATURE_DESCRIPTION : Common feature for check whether dependency is null*/
    public static final boolean SYSUI_SUPPORT_DEPENDENCY = COMMON;

    /* FEATURE_DESCRIPTION : Support gradle build and updatable SystemUI apk */
    public static final boolean SYSUI_SUPPORT_GRADLE_BUILD = true;

    /* FEATURE_DESCRIPTION : Knox container and Dual IM need to be distinguished from Android Managed Profile  */
    public static final boolean SYSUI_SUPPORT_SEC_MANAGED_PROFILE = true;
    /* FEATURE_DESCRIPTION : Common feature for Settings Helper */
    public static final boolean SYSUI_SUPPORT_SETTINGS_HELPER = COMMON;
    /* FEATURE_DESCRIPTION : Temporary feature for poring Q OS.
        This feature should be removed after finishing porting */
    public static final boolean SYSUI_PORTING_Q_TODO = COMMON;
    /* FEATURE_DESCRIPTION : Common feature for open theme implementations  */
    public static final boolean SYSUI_SUPPORT_OPEN_THEME = COMMON &&
            SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_ELASTIC_PLUGIN;
    /* FEATURE_DESCRIPTION : Common feature for DebugLogStore */
    public static final boolean SYSUI_SUPPORT_DEBUG_LOG = true;
    /* FEATURE_DESCRIPTION : Support debuggable UiOffLoadThread */
    public static final boolean SYSUI_SUPPORT_DEBUG_UI_OFFLOAD_THREAD = COMMON && (Debug.semIsProductDev() || LogUtil.isDebugLevelMid());
    /* FEATURE_DESCRIPTION : Common feature for Code Diet of StatusBar.java - PEACE : SYSUI_STATUSBAR_CODE_DIET*/
    public static final boolean SYSUI_SUPPORT_STATUS_BAR_CODE_DIET = COMMON;
    /* FEATURE_DESCRIPTION : SEC NOT support HumanInteractionClassifier for false touch */
    public static final boolean SYSUI_SUPPORT_FALSE_TOUCH = false;
    /* FEATURE_DESCRIPTION : Common feature for cover implementations  */
    public static final boolean SYSUI_SUPPORT_COVER = COMMON &&
            !TextUtils.isEmpty(SecProductFeature_FRAMEWORK.SEC_PRODUCT_FEATURE_FRAMEWORK_CONFIG_COVER_TYPES);
    /* FEATURE_DESCRIPTION : Common feature to check if the binary is built for factory mode  */
    public static final boolean SYSUI_IS_FACTORY_BINARY = FactoryTest.isFactoryBinary();

    // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CONTAINER {
    /* FEATURE_DESCRIPTION : Common feature for KnoxPremiumContainer */
    public static final boolean SYSUI_SUPPORT_KNOX_PREMIUM_CONTAINER = SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CONTAINER;
    // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CONTAINER }
    /* FEATURE_DESCRIPTION : Common feature for AOSP bug fix */
    public static final boolean SYSUI_SUPPORT_AOSP_BUG_FIX = true;
    /* FEATURE_DESCRIPTION : Support sec ims manager. */
    public static final boolean SYSUI_SUPPORT_SEC_IMS_MANAGER = false; //TODO true;
    /* FEATURE_DESCRIPTION : Support high performance mode */
    public static final boolean SYSUI_SUPPORT_HIGH_PERFORMANCE_MODE = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_COMMON_SUPPORT_HIGH_PERFORMANCE_MODE", false);
    /* FEATURE_DESCRIPTION : Support dual ims concept */
    public static final boolean SYSUI_SUPPORT_DUAL_IMS = "DSDS_DI".equals(SemFloatingFeature.getInstance().getString("SEC_FLOATING_FEATURE_COMMON_CONFIG_DUAL_IMS", ""))
                                                              && SemCscFeature.getInstance().getBoolean("CscFeature_Common_SupportDualIMS", true);
    /* FEATURE_DESCRIPTION : Support Quick Panel Expand Vi */
    public static final boolean SYSUI_SUPPORT_QUICK_PANEL_OVERLAPPING_EXPAND_VI = QUICKPANEL;

    /* FEATURE_DESCRIPTION : Support Integrity Verification for SystemUI */
    public static final boolean SYSUI_SUPPORT_INTEGRITY_VERIFICATION = COMMON;
    /* FEATURE_DESCRIPTION : Support touch proximity */
    public static final boolean SYSUI_SUPPORT_TOUCH_PROXIMITY = COMMON;

    /* FEATURE_DESCRIPTION : Support get display state for SystemUI */
    public static final boolean SYSUI_SUPPORT_DISPLAY_STATE = COMMON;

    //--------------------------------------------------------//
    // StatusBar Constants                                    //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Each operators or Nations has specific requirement about StatusBar icon. */
    public static final String STATBAR_ICON_BRANDING = STATBAR ? SemCscFeature.getInstance()
            .getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGOPBRANDINGFORINDICATORICON, "") : "";
    /* FEATURE_DESCRIPTION : Is Korea branding */
    public static final boolean STATBAR_IS_KOREA_BRANDING = "SKT".equals(STATBAR_ICON_BRANDING) || "KTT".equals(STATBAR_ICON_BRANDING) 
                                                               || "LGT".equals(STATBAR_ICON_BRANDING) || "KOO".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : apply Samsung StatusBar GUI concept (SEC) */
    public static final boolean STATBAR_SUPPORT_SEC_COMMON = STATBAR;
    /* FEATURE_DESCRIPTION : apply Samsung StatusBar DATA ICON GUI concept (SEC) */
    public static final boolean STATBAR_SUPPORT_SEC_DATA_ICON = STATBAR;
    /* FEATURE_DESCRIPTION : apply Samsung StatusBar SIGNAL ICON GUI concept (SEC) */
    public static final boolean STATBAR_SUPPORT_SEC_SIGNAL_ICON = STATBAR;
    /* FEATURE_DESCRIPTION : apply Samsung StatusBar WIFI ICON GUI concept (SEC) */
    public static final boolean STATBAR_SUPPORT_SEC_WIFI_ICON = STATBAR;
    /* FEATURE_DESCRIPTION : apply Samsung StatusBar SYSTEM ICON GUI concept (SEC) */
    public static final boolean STATBAR_SUPPORT_SEC_SYSTEM_ICON = STATBAR;
    /* FEATURE_DESCRIPTION : apply Samsung StatusBar PLMN GUI concept (SEC) */
    public static final boolean STATBAR_SUPPORT_SEC_PLMN = STATBAR;
    /* FEATURE_DESCRIPTION : apply Samsung StatusBar CUTOUT GUI concept (SEC) */
    public static final boolean STATBAR_SUPPORT_SEC_CUTOUT = STATBAR;
    /* FEATURE_DESCRIPTION : Each operators or Nations has specific requirement about StatusBar icon. */
    public static final boolean STATBAR_KEYGUARD_INDICATOR = STATBAR;
    /* FEATURE_DESCRIPTION : Support power saving mode in StatusBar */
    public static final boolean STATBAR_SUPPORT_STATUSBAR_POWER_SAVING_MODE = STATBAR_SUPPORT_SEC_SYSTEM_ICON;
    /* FEATURE_DESCRIPTION : Support power saving mode in StatusBar */
    public static final boolean STATBAR_SUPPORT_STATUSBAR_HIGH_PERFORMANCE_MODE = STATBAR_SUPPORT_SEC_SYSTEM_ICON && SYSUI_SUPPORT_HIGH_PERFORMANCE_MODE;
    /* FEATURE_DESCRIPTION : Support wifi only device */
    public static final boolean STATBAR_SUPPORT_WIFI_ONLY = SecProductFeature_RIL.SEC_PRODUCT_FEATURE_RIL_WIFI_ONLY;
    /* FEATURE_DESCRIPTION : IndicatorGardenPresenter is in charge of indicator layout work about cutout, rounded corner. */
    public static final boolean STATBAR_INDICATOR_GARDENER = STATBAR;
    /* FEATURE_DESCRIPTION : To make the indicator icon assume many situations so that it can check whether the indicator gardener works properly. If true, indicator attach a lot of icon as fake. */
    public static final boolean STATBAR_INDICATOR_GARDENER_JAM_TRIGGER = STATBAR_INDICATOR_GARDENER && (Rune.isTesting() || false);
    /* FEATURE_DESCRIPTION : Left-right padding value to arrange indicator elements while considering the display of the rounded edges that the device has. */
    public static final float   STATBAR_CONFIG_DEVICE_CORNER_ROUND =
            Float.parseFloat(SemFloatingFeature.getInstance().getString("SEC_FLOATING_FEATURE_SYSTEMUI_CONFIG_CORNER_ROUND","0.0"));
    public static final int     STATBAR_CONFIG_STATUSBAR_SIDE_PADDING =
            (int) (STATBAR_CONFIG_DEVICE_CORNER_ROUND * (10.0f - STATBAR_CONFIG_DEVICE_CORNER_ROUND) - 1f);
    /* FEATURE_DESCRIPTION : Support No sim notification */
    public static final boolean STATBAR_SUPPORT_NO_SIM_NOTIFICATION = STATBAR && SemCscFeature.getInstance().getBoolean(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_SUPPORTNOSIMNOTIFICATION, false);
    /* FEATURE_DESCRIPTION : Support change indicator BG color */
    public static final boolean STATBAR_SUPPORT_CHANGE_INDICATOR_BG_COLOR = STATBAR;
    /* FEATURE_DESCRIPTION : Support Jump Call Screen when user touch green indicator. */
    // http://mobilerndhub.sec.samsung.net/wiki/display/TGUIF/OneClick+Jump+Call+Screen+%7C+SB
    public static final boolean STATBAR_SUPPORT_ONECLICK_JUMP_CALL_SCREEN = STATBAR_SUPPORT_CHANGE_INDICATOR_BG_COLOR;
    /* FEATURE_DESCRIPTION : Support Signal state log */
    public static final boolean STATBAR_SUPPORT_DEBUG_SIGNAL_STATE_LOG = STATBAR;
    /* FEATURE_DESCRIPTION : Support multi sim slot off */
    public static final boolean STATBAR_SUPPORT_MULTI_SIM_SLOT_OFF = STATBAR_SUPPORT_SEC_SIGNAL_ICON;
    /* FEATURE_DESCRIPTION : Support Signal icon at emergency call only state */
    public static final boolean STATBAR_SUPPORT_SIGNAL_ICON_AT_EMERGENCY_CALL_ONLY_STATE = STATBAR_SUPPORT_SEC_SIGNAL_ICON;
    /* FEATURE_DESCRIPTION : Support Signal icon at emergency call only state */
    public static final boolean STATBAR_SUPPORT_MULTI_SIM = STATBAR_SUPPORT_SEC_SIGNAL_ICON && SYSUI_SUPPORT_MULTI_SIM_DEVICE;
    /* FEATURE_DESCRIPTION : Support signal infomation backup logic*/
    public static final boolean STATBAR_SUPPORT_SIGNAL_INFO_BACKUP = STATBAR_SUPPORT_SEC_SIGNAL_ICON;
    /* FEATURE_DESCRIPTION : Display Roaming Icon in roaming state. */
    public static final boolean STATBAR_SUPPORT_ROAMING_ICON = !(STATBAR_SUPPORT_SEC_SIGNAL_ICON && "USC".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : Display CDMA roaming icon (triangle icon) in cdma roaming state. */
    public static final boolean STATBAR_SUPPORT_CDMA_ROAMING_ICON = STATBAR_SUPPORT_SEC_SIGNAL_ICON;
    /* FEATURE_DESCRIPTION : Display GSM roaming icon (R icon) in gsm roaming state. */
    public static final boolean STATBAR_SUPPORT_GSM_ROAMING_ICON = !("ATT".equals(STATBAR_ICON_BRANDING) || "TMB".equals(STATBAR_ICON_BRANDING)
            || "AIO".equals(STATBAR_ICON_BRANDING) || "MTR".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : Config of Roaming icon`s type. */
    public static final String STATBAR_CONFIG_ROAMING_ICON_TYPE = SemCscFeature.getInstance().getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGROAMINGICONTYPE, "");
    /* FEATURE_DESCRIPTION : Display always GSM roaming icon (R icon) in roaming state. */
    public static final boolean STATBAR_USE_ONLY_GSM_ROAMING_ICON = STATBAR_SUPPORT_SEC_SIGNAL_ICON && "GSM".equals(STATBAR_CONFIG_ROAMING_ICON_TYPE);
    /* FEATURE_DESCRIPTION : Display always CDMA roaming icon (triangle icon) in roaming state. */
    public static final boolean STATBAR_USE_ONLY_CDMA_ROAMING_ICON = STATBAR_SUPPORT_SEC_SIGNAL_ICON && "CDMA".equals(STATBAR_CONFIG_ROAMING_ICON_TYPE);
    /* FEATURE_DESCRIPTION : Display femto cell icon when device is connected with femto cell*/
    public static final boolean STATBAR_SUPPORT_FEMTO_CELL = STATBAR_SUPPORT_SEC_SIGNAL_ICON && SemCscFeature.getInstance().getBoolean(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_SUPPORTFEMTOCELLICON, false);
    /* FEATURE_DESCRIPTION : Display roaming icon when device is connected only PS roaming network*/
    public static final boolean STATBAR_SUPPORT_CDMA_ROAMING_ICON_AT_PS_ONLY = STATBAR_SUPPORT_SEC_SIGNAL_ICON && "SPR".equals(STATBAR_ICON_BRANDING) || "VMU".equals(STATBAR_ICON_BRANDING) || "XAS".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : Don't display roaming icon in national roaming */
    public static final boolean STATBAR_SUPPORT_BLOCK_ROAMING_ICON_IN_NATIONAL_ROAMING = STATBAR_SUPPORT_SEC_SIGNAL_ICON;
    /* FEATURE_DESCRIPTION : Support S roaming */
    public static final boolean STATBAR_SUPPORT_S_ROAMING = STATBAR_SUPPORT_SEC_SIGNAL_ICON;
    /* FEATURE_DESCRIPTION : Signal Strength`s max value*/
    public static final int STATBAR_MAX_SIGNAL_LEVEL = STATBAR_SUPPORT_SEC_SIGNAL_ICON ? SemCscFeature.getInstance().getInteger(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGMAXRSSILEVEL, 4) : 4;
    /* FEATURE_DESCRIPTION : Support PS state combined signal */
    public static final boolean STATBAR_SUPPORT_PS_STATE_COMBINED_SIGNAL = STATBAR_SUPPORT_SEC_SIGNAL_ICON && "COMBINED".equals(SemCscFeature.getInstance().getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGDETERMINESIGNALSTATE, ""));
    /* FEATURE_DESCRIPTION : Support operator`s no service icon*/
    public static final boolean STATBAR_SUPPORT_OPERATOR_NO_SERVICE_ICON = STATBAR_SUPPORT_SEC_SIGNAL_ICON;
    /* FEATURE_DESCRIPTION : In SPR model,  indicator`s signal strength icon should be follow Google pure look. */
    public static final boolean STATBAR_SUPPORT_PURE_SIGNAL_ICON = STATBAR_SUPPORT_SEC_SIGNAL_ICON && ("SPR".equals(STATBAR_ICON_BRANDING) || "VMU".equals(STATBAR_ICON_BRANDING) || "BST".equals(STATBAR_ICON_BRANDING)
            || "XAS".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : Support hide signal bar at WFC state*/
    public static final boolean STATBAR_SUPPORT_HIDE_SIGNAL_LEVEL_AT_WFC_STATE = STATBAR_SUPPORT_SEC_SIGNAL_ICON && "XFA".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : Support showing level 0 in no svc when WFC is connected */
    public static final boolean STATBAR_SUPPORT_SIGNAL_LEVE_ZERO_IN_NO_SVC_AT_TMOWFC = STATBAR_SUPPORT_SEC_SIGNAL_ICON && SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTSECWFC)
            && ("TMB".equals(STATBAR_ICON_BRANDING) || "MTR".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : Signal icon`s level should be changed just one level per one sec*/
    public static final boolean STATBAR_SUPPORT_CHANGE_ONE_SIGNAL_LEVEL_PER_SEC = STATBAR_SUPPORT_SEC_SIGNAL_ICON && !"".equals(SemCscFeature.getInstance().getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGRULERSSILEVEL, ""));
    /* FEATURE_DESCRIPTION : Support simplified signal cluster concept.*/
    public static final boolean STATBAR_SUPPORT_SIMPLIFIED_SIGNAL_CLUSTER = STATBAR_SUPPORT_SEC_SIGNAL_ICON && ("CHC".equals(STATBAR_ICON_BRANDING)
            || "CHM".equals(STATBAR_ICON_BRANDING) || "CHU".equals(STATBAR_ICON_BRANDING) || "CTC".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : Support CTC operator`s signal spec*/
    public static final boolean STATBAR_SUPPORT_CTC_OP_SIGNAL_SPEC = STATBAR_SUPPORT_SEC_SIGNAL_ICON && "CTC".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : Support CTC operator`s signal spec*/
    public static final boolean STATBAR_SUPPORT_CTC_OP_SIGNAL_AT_CTC_CARD = STATBAR_SUPPORT_SEC_SIGNAL_ICON && (SemCscFeature.getInstance().getBoolean(CscFeatureTagRIL.TAG_CSCFEATURE_RIL_SUPPORT75MODE)
            || SemCscFeature.getInstance().getBoolean(CscFeatureTagRIL.TAG_CSCFEATURE_RIL_SUPPORTALLRAT))
            && ("CHC".equals(STATBAR_ICON_BRANDING) || "CHM".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : Display LTE icon instead of 4G icon*/
    public static final boolean STATBAR_DISPLAY_LTE_INSTEAD_OF_4G_ICON = STATBAR_SUPPORT_SEC_DATA_ICON && "LTE".equals(SemCscFeature.getInstance().getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGOVERRIDEDATAICON, ""));
    /* FEATURE_DESCRIPTION : Display 4G Plus icon instead of 4G icon*/
    public static final boolean STATBAR_DISPLAY_4G_PLUS_INSTEAD_OF_4G_ICON = STATBAR_SUPPORT_SEC_DATA_ICON && "DCM".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : Show HSDPA_data_icon*/
    public static final boolean STATBAR_SUPPORT_HSDPA_DATA_ICON = !(STATBAR_SUPPORT_SEC_DATA_ICON && (STATBAR_IS_KOREA_BRANDING || "OYB".equals(STATBAR_ICON_BRANDING) || "VID".equals(STATBAR_ICON_BRANDING)
            || "OYA".equals(STATBAR_ICON_BRANDING)));
    /* FEATURE_DESCRIPTION : config about LTE wide band icon */
    public static final String STATBAR_CONFIG_LTE_WIDE_BAND_ICON = STATBAR_SUPPORT_SEC_DATA_ICON ? SemCscFeature.getInstance().getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGOPBRANDINGLTEWIDEBANDICON, "") : "";
    /* FEATURE_DESCRIPTION : Support LTE wide band icon- 4G+/LTE+*/
    public static final boolean STATBAR_SUPPORT_LTE_WIDE_BAND = STATBAR_SUPPORT_SEC_DATA_ICON && !"NONE".equals(STATBAR_CONFIG_LTE_WIDE_BAND_ICON);
    /* FEATURE_DESCRIPTION : Display 4.5G icon instead of 4G+ icon*/
    public static final boolean STATBAR_DISPLAY_4_HALF_G_INSTEAD_OF_4G_PLUS_ICON = "4.5G".equals(STATBAR_CONFIG_LTE_WIDE_BAND_ICON);
    /* FEATURE_DESCRIPTION : Show disabled_data_icon*/
    public static final boolean STATBAR_SUPPORT_DISABLED_DATA_ICON = STATBAR_SUPPORT_SEC_SIGNAL_ICON && ("CHM".equals(STATBAR_ICON_BRANDING) || "CHU".equals(STATBAR_ICON_BRANDING) || "VZW".equals(STATBAR_ICON_BRANDING)
            || "ZVV".equals(STATBAR_ICON_BRANDING) || "ZTM".equals(STATBAR_ICON_BRANDING) || "TGY".equals(STATBAR_ICON_BRANDING)
            || "BRI".equals(STATBAR_ICON_BRANDING) || "CHC".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : When Device is connected with wifi, disabled data icon shouldn`t be displayed. */
    public static final boolean STATBAR_SUPPORT_HKTW_DISABLED_DATA_ICON_CONCEPT = "BRI".equals(STATBAR_ICON_BRANDING) || "TGY".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : Support indicator`s 5G data icon */
    public static final boolean STATBAR_SUPPORT_5G = STATBAR_SUPPORT_SEC_DATA_ICON;
    /* FEATURE_DESCRIPTION : Although 5G network is disconnected, 5G icon should be displayed during UWB timer. */
    public static final boolean STATBAR_SUPPORT_5G_ICON_DISPLAY_TIMER = STATBAR_SUPPORT_SEC_DATA_ICON && ("VZW".equals(Rune.STATBAR_ICON_BRANDING) || "TMB".equals(Rune.STATBAR_ICON_BRANDING) || STATBAR_IS_KOREA_BRANDING);
    /* FEATURE_DESCRIPTION : Support indicator`s 5G available data icon */
    public static final String STATBAR_CONFIG_5G_ICON = SemCscFeature.getInstance().getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGPOLICYDISPLAYOPLOGO, "");
    public static final boolean STATBAR_SUPPORT_5G_AVAILABLE_ICON = STATBAR_SUPPORT_SEC_DATA_ICON && STATBAR_CONFIG_5G_ICON.contains("5GAvailable");
    /* FEATURE_DESCRIPTION : Support WFC listener*/
    public static final boolean STATBAR_SUPPORT_WIFI_CALLING_LISTENER = STATBAR_SUPPORT_SEC_DATA_ICON;
    /* FEATURE_DESCRIPTION : Support check network status setting value for displaying 5G icon */
    public static final boolean STATBAR_SUPPORT_CHECK_NETWORK_STATUS_SETTING = STATBAR_SUPPORT_5G && STATBAR_IS_KOREA_BRANDING;
    /* FEATURE_DESCRIPTION : Support 6 generation wifi icons concept */
    public static final boolean STATBAR_SUPPORT_WIFI6_ICONS = STATBAR_SUPPORT_SEC_WIFI_ICON;
    /* FEATURE_DESCRIPTION : Support indicator`s Wifi Captive not login icon*/
    public static final boolean STATBAR_SUPPORT_WIFI_CAPTIVE_NOT_LOGIN = STATBAR_SUPPORT_SEC_WIFI_ICON;
    /* FEATURE_DESCRIPTION : Support wifi icon flash concept */
    public static final boolean STATBAR_SUPPORT_WIFI_FLASH_CONCEPT = STATBAR_SUPPORT_SEC_WIFI_ICON && !STATBAR_SUPPORT_WIFI_ONLY;
    /* FEATURE_DESCRIPTION : Support hide wifi icon during ps network is switched to mobile. */
    public static final boolean STATBAR_SUPPORT_HIDE_WIFI_DURING_SWITCH_TO_MOBILE = STATBAR_SUPPORT_SEC_WIFI_ICON && !STATBAR_SUPPORT_WIFI_ONLY;
    /* FEATURE_DESCRIPTION : Support wifi calling icon*/
    public static final boolean STATBAR_SUPPORT_WIFI_CALLING_ICON = STATBAR_SUPPORT_SEC_WIFI_ICON && ("ATT".equals(STATBAR_ICON_BRANDING) || "AIO".equals(STATBAR_ICON_BRANDING) || "TFN".equals(STATBAR_ICON_BRANDING) || "XAR".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : Support operator`s Specific Wifi */
    public static final boolean STATBAR_SUPPORT_CARRIER_WIFI_ICON = ("KTT".equals(STATBAR_ICON_BRANDING) && SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_PREFERREDBAND,"").contains("11AC"))
            || "LGT".equals(STATBAR_ICON_BRANDING) || "VZW".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : Support indicator system icon reduction concept */
    public static final boolean STATBAR_SUPPORT_INDICATOR_SYSTEM_ICON_REDUCTION = STATBAR_SUPPORT_SEC_SYSTEM_ICON;
    /* FEATURE_DESCRIPTION : SEC new clock and date view for performance : (PEACE) Rune.GUILD_IS_CLOCK_BELL_TOWER */
    public static final boolean STATBAR_QS_CLOCK_BELL_TOWER = STATBAR;
    /* FEATURE_DESCRIPTION : SEC shows the time and date in the Persian language. : (PEACE) Rune.QPANEL_SUPPORT_SUPPORT_PERSIAN_CALENDAR */
    public static final boolean STATBAR_QS_PERSIAN_CALENDAR = STATBAR && SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTPERSIANCALENDAR, false);
    /* FEATURE_DESCRIPTION : Support Samsung Battery GUI */
    public static final boolean STATBAR_SUPPORT_SEC_BATTERY_GUI = STATBAR;
    /* FEATURE_DESCRIPTION : Display Secure Wi-Fi icon when users are connected by Secure Wi-Fi during vpn connection */
    public static final boolean STATBAR_SUPPORT_VPN_SECURE_WIFI_ICON = STATBAR; 
	/* FEATURE_DESCRIPTION : Support BlueTooth tethering*/
    public static final boolean STATBAR_SUPPORT_BLUETOOTH_TETHERING = STATBAR;
    /* FEATURE_DESCRIPTION : Support Network Booster*/
    public static final boolean STATBAR_SUPPORT_NETWORK_BOOSTER = STATBAR_SUPPORT_SEC_COMMON;
    /* FEATURE_DESCRIPTION : Display Network Booster icon as simple type*/
    public static final boolean STATBAR_DISPLAY_NETWORK_BOOSTER_ICON_AS_SIMPLE_TYPE = STATBAR_SUPPORT_NETWORK_BOOSTER && (STATBAR_SUPPORT_DISABLED_DATA_ICON || "US".equals(SYSUI_COUNTRY_ISO));
    /* FEATURE_DESCRIPTION : Support MPTCP*/
    public static final boolean STATBAR_SUPPORT_MPTCP = STATBAR_SUPPORT_SEC_COMMON && (("KTT".equals(STATBAR_ICON_BRANDING) && !(SYSUI_PRODUCT_NAME.startsWith("beyondx") || SYSUI_PRODUCT_NAME.startsWith("winnerx") || SYSUI_PRODUCT_NAME.startsWith("d1x") || SYSUI_PRODUCT_NAME.startsWith("d2x")))
            || "TUR".equals(STATBAR_ICON_BRANDING) || "THL".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : When the user has expanded the panel, the Carrier Label PLMN information that appears at the bottom of the screen is displayed screen. */
    public static final boolean STATBAR_SUPPORT_NOTIFICATION_PANEL_PLMN = STATBAR; // PEACE: Rune.STATBAR_SUPPORT_NOTIFICATION_PANEL_PLMN
    public static final boolean STATBAR_SUPPORT_NOTIFICATION_PANEL_PLMN_ZVV = "ZVV".equals(STATBAR_ICON_BRANDING); // Brazil
    public static final boolean STATBAR_SUPPORT_NOTIFICATION_PANEL_PLMN_CTC = "CTC".equals(STATBAR_ICON_BRANDING); // China
    /*  FEATURE_DESCRIPTION : Carrier Information on indicator */
    public static final boolean STATBAR_CARRIER_INFORMATION = STATBAR; // Rune.STATBAR_SUPPORT_OPERATOR_LOGO_ICON
    /*  FEATURE_DESCRIPTION : Used for showing operator's logo icon on indicator */
    public static final boolean STATBAR_CHUNO_LOGO_TEST = STATBAR_CARRIER_INFORMATION && (STATBAR_INDICATOR_GARDENER_JAM_TRIGGER || false);
    public static final String STATBAR_CARRIER_LOGO = (!STATBAR_CARRIER_INFORMATION) ? ""
            : (STATBAR_CHUNO_LOGO_TEST ? "BOTH" : SemCscFeature.getInstance().getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGPOLICYDISPLAYOPLOGO, ""));
    /* FEATURE_DESCRIPTION : When LCD is turn on, PLMN info should be displayed at indicator`s left side during 3 second */
    public static final boolean STATBAR_CARRIER_PLMN_TEST = STATBAR_CARRIER_INFORMATION && false;
    public static final boolean STATBAR_CARRIER_PLMN = STATBAR_CARRIER_INFORMATION
            && (STATBAR_CARRIER_PLMN_TEST || "LcdOn_3sec".equals(SemCscFeature.getInstance().getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGRULEFORSHOWPLMN, "")));
    /* FEATURE_DESCRIPTION : Support hide function for network information : STATBAR_SUPPORT_INDICATOR_HIDE_NETWORK_INFORMATION */
    public static final boolean STATBAR_NETWORK_INFORMATION = "ORANGE".equals(STATBAR_ICON_BRANDING);

    //--------------------------------------------------------//
    // QuickSettings Constants                                    //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Each operators or Nations has specific requirement about QsTile icon. */
    public static final String CONFIG_QS_ICON_BRANDING = SemCscFeature.getInstance()
            .getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGOPBRANDINGQUICKSETTINGICON, "");
    /* FEATURE_DESCRIPTION : Each operators or Nations has specific requirement about QsTile popup. */
    public static final String CONFIG_QS_POPUP_BRANDING = SemCscFeature.getInstance()
            .getString(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGQUICKSETTINGPOPUP, "");

    /* FEATURE_DESCRIPTION : USA operator*/
    public static final boolean QPANEL_IS_VZW_POPUP = "VZW".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_ATT_POPUP = "ATT".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_AIO_POPUP = "AIO".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_TMB_POPUP = "TMB".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_MTR_POPUP = "MTR".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_SPR_POPUP = "SPR".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_VMU_POPUP = "VMU".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_BST_POPUP = "BST".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_XAS_POPUP = "XAS".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_USC_POPUP = "USC".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_LRA_POPUP = "LRA".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_TFN_POPUP = "TFN".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_CCT_POPUP = "CCT".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_CHA_POPUP = "CHA".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_ACG_POPUP = "ACG".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_CSP_POPUP = "CSP".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_XAR_POPUP = "XAR".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_XAA_POPUP = "XAA".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_USA_POPUP = QPANEL_IS_VZW_POPUP || QPANEL_IS_ATT_POPUP || QPANEL_IS_AIO_POPUP || QPANEL_IS_TMB_POPUP
            || QPANEL_IS_MTR_POPUP || QPANEL_IS_SPR_POPUP || QPANEL_IS_VMU_POPUP || QPANEL_IS_BST_POPUP || QPANEL_IS_XAS_POPUP || QPANEL_IS_USC_POPUP
            || QPANEL_IS_LRA_POPUP || QPANEL_IS_TFN_POPUP || QPANEL_IS_CCT_POPUP || QPANEL_IS_ACG_POPUP || QPANEL_IS_CSP_POPUP || QPANEL_IS_XAR_POPUP
            || QPANEL_IS_CHA_POPUP || QPANEL_IS_XAA_POPUP ;
    /* FEATURE_DESCRIPTION : eur special operator*/
    public static final boolean QPANEL_IS_OJT_POPUP = "OJT".equals(CONFIG_QS_POPUP_BRANDING);

    /* FEATURE_DESCRIPTION : KOREA operator*/
    public static final boolean QPANEL_IS_SKT_POPUP = "SKT".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_KTT_POPUP = "KTT".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_LGT_POPUP = "LGT".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_KOR_OPEN_POPUP = "KOO".equals(CONFIG_QS_POPUP_BRANDING);
    public static final boolean QPANEL_IS_KOREA_POPUP = QPANEL_IS_SKT_POPUP || QPANEL_IS_KTT_POPUP || QPANEL_IS_LGT_POPUP || QPANEL_IS_KOR_OPEN_POPUP;

    /* FEATURE_DESCRIPTION : CHINA operator*/
    public static final boolean QPANEL_IS_CMCC_POPUP = "CHM".equals(CONFIG_QS_POPUP_BRANDING);
    /* FEATURE_DESCRIPTION : FEATURE that defines the basic behavior of QSTILE of SEC STYLE  QSTileImpl*/
    public static final boolean QPANEL_SUPPORT_SEC_TILE_MANAGEMENT = QPANEL;
    /* FEATURE_DESCRIPTION : Use CSC feature value for default QSTile list */
    public static final boolean QPANEL_SUPPORT_CSC_FOR_DEFAULT_QSTILE_LIST = QPANEL_SUPPORT_SEC_TILE_MANAGEMENT;
    /* FEATURE_DESCRIPTION : Support dynamic tile visibility change*/
    public static final boolean QPANEL_SUPPORT_DYNAMIC_TILE_VISIBILITY_CHANGE = QPANEL_SUPPORT_SEC_TILE_MANAGEMENT;
    /* FEATURE_DESCRIPTION : Support customTile as default QS tile*/
    public static final boolean QPANEL_SUPPORT_CUSTOM_TILE_AS_DEFAULT_TILE = QPANEL_SUPPORT_SEC_TILE_MANAGEMENT;
    /* FEATURE_DESCRIPTION : Support default tiles auto adding function */
    public static final boolean QPANEL_SUPPORT_DEFAULT_TILES_AUTO_ADDING = QPANEL_SUPPORT_SEC_TILE_MANAGEMENT;
    /* FEATURE_DESCRIPTION : FEATURE that defines the basic behavior of QSTILE of SEC STYLE  ex) QSTileImpl*/
    public static final boolean QPANEL_SUPPORT_SEC_STYLE_TILE_BASE = QPANEL;
    /* FEATURE_DESCRIPTION : FEATURE that defines the basic behavior of QSTILEVIEW of SEC STYLE ex) QSTileView / QSIconView / */
    public static final boolean QPANEL_SUPPORT_SEC_STYLE_TILEVIEW_BASE = QPANEL;
    /* FEATURE_DESCRIPTION : FEATURE that defines QuickStatusbarHeader of SEC_STYLE */
    public static final boolean QPANEL_SUPPORT_SEC_STYLE_HEADER = QPANEL;
    /* FEATURE_DESCRIPTION : FEATURE that defines Panel of SEC_STYLE  ex) QSContainer, QSPanle, TileLayout*/
    public static final boolean QPANEL_SUPPORT_SEC_STYLE_PANEL = QPANEL;
    /* FEATURE_DESCRIPTION : FEATURE that defines DetailView of SEC_STYLE ex) QSDetail, QSDetail Item */
    public static final boolean QPANEL_SUPPORT_SEC_STYLE_DETAIL = QPANEL && true;
    /* FEATURE_DESCRIPTION : FEATURE that defines Footer of SEC_STYLE QSFooter */
    public static final boolean QPANEL_SUPPORT_SEC_STYLE_FOOTER = QPANEL;
    /* FEATURE_DESCRIPTION : FEATURE of the behavior of SEC_STYLE Custom Tile */
    public static final boolean QPANEL_SUPPORT_SEC_CUSTOM_TILE = QPANEL_SUPPORT_SEC_STYLE_TILE_BASE;
    /* FEATURE_DESCRIPTION : Support customTile`s detail view */
    public static final boolean QPANEL_SUPPORT_CUSTOM_TILE_DETAIL_VIEW = QPANEL_SUPPORT_SEC_CUSTOM_TILE && QPANEL_SUPPORT_SEC_STYLE_DETAIL;
    /* FEATURE_DESCRIPTION : Support emergency tile list */
    public static final boolean QPANEL_SUPPORT_EMERGENCY_MODE_TILE = QPANEL_SUPPORT_SEC_TILE_MANAGEMENT;
    /* FEATURE_DESCRIPTION : FEATURE of SoundMode Tile */
    public static final boolean QPANEL_SOUNDMODE_TILE = QPANEL_SUPPORT_SEC_STYLE_TILE_BASE;
    public static final boolean QPANEL_SOUNDMODE_TILE_SUPPORT_KNOX = QPANEL_SOUNDMODE_TILE && SYSUI_SUPPORT_KNOX_MONITOR;
    /* FEATURE_DESCRIPTION : Common feature for KnoxStateMonitor */
    public static final boolean QPANEL_SUPPORT_KNOX = SYSUI_SUPPORT_KNOX_MONITOR;
    /* FEATURE_DESCRIPTION : FEATURE ot the behavior of SEC_STYLE Airplane Mode */
    public static final boolean QPANEL_SUPPORT_SEC_AIRPLANEMODE = QPANEL_SUPPORT_SEC_STYLE_TILEVIEW_BASE;
    /* FEATURE_DESCRIPTION : FEATURE of the behavior SEC_STYLE Bluetooth in Quick Settings*/
    public static final boolean QPANEL_SUPPORT_SEC_BLUETOOTH = QPANEL_SUPPORT_SEC_STYLE_TILEVIEW_BASE;
    /* FEATURE_DESCRIPTION : FEATURE of the behavior SEC_STYLE Rotation lock in Quick Settings*/
    public static final boolean QPANEL_SUPPORT_SEC_ROTATIONlOCK = QPANEL_SUPPORT_SEC_STYLE_TILEVIEW_BASE;
    /* FEATURE_DESCRIPTION : FEATURE of the behavior SEC_STYLE Navigation Bar in Quick Settings*/
    public static final boolean QPANEL_SUPPORT_SEC_NAVBAR = QPANEL_SUPPORT_SEC_STYLE_TILEVIEW_BASE;
    /* FEATURE_DESCRIPTION : Support manual rotaion option to rotation lock tile */
    public static final boolean QPANEL_SUPPORT_ROTATIONLOCK_TILE_MANUAL_ROTATION = NAVBAR
            && !SemFloatingFeature.getInstance().getString("SEC_FLOATING_FEATURE_FRAMEWORK_CONFIG_NAVIGATION_BAR_THEME", "").isEmpty()
            && QPANEL_SUPPORT_SEC_ROTATIONlOCK && QPANEL_SUPPORT_SEC_STYLE_DETAIL;
    /* FEATURE_DESCRIPTION : FEATURE for Bar Controller  */
    public static final boolean QPANEL_SUPPORT_BAR_CONTROLLER = QPANEL;
    /* FEATURE_DESCRIPTION : BrightnessController, ToggleSlider */
    public static final boolean QPANEL_SUPPORT_SEC_BRIGHTNESS = QPANEL;
    /* FEATURE_DESCRIPTION : Support brightness detail */
    public static final boolean QPANEL_SUPPORT_BRIGHTNESS_DETAIL = QPANEL_SUPPORT_SEC_BRIGHTNESS;
    /* FEATURE_DESCRIPTION : Support outdoor mode for brightness*/
    public static final boolean QPANEL_SUPPORT_BRIGHTNESS_OUTDOOR_MODE = QPANEL_SUPPORT_SEC_BRIGHTNESS && SecProductFeature_SETTINGS.SEC_PRODUCT_FEATURE_SETTINGS_SUPPORT_OURDOOR_MODE;
    /* FEATURE_DESCRIPTION : Support personal auto-brightness control*/
    public static final boolean QPANEL_SUPPORT_PERSONAL_AUTO_BRIGHTNESS_CONTROL = QPANEL_SUPPORT_SEC_BRIGHTNESS && ("3".equals(SecProductFeature_LCD.SEC_PRODUCT_FEATURE_LCD_CONFIG_CONTROL_AUTO_BRIGHTNESS)
            || "4".equals(SecProductFeature_LCD.SEC_PRODUCT_FEATURE_LCD_CONFIG_CONTROL_AUTO_BRIGHTNESS));
    /* FEATURE_DESCRIPTION : Support wifi only device */
    public static final boolean QPANEL_SUPPORT_WIFI_ONLY = SecProductFeature_RIL.SEC_PRODUCT_FEATURE_RIL_WIFI_ONLY;
    // The popup is always showing on quick panel for ATT
    public static final boolean QPANEL_SUPPORT_AIRPLANE_MODE_ENABLE_POPUP = QPANEL_IS_ATT_POPUP;
    /* FEATURE_DESCRIPTION : Support china smart manager */
    public static final boolean QPANEL_SUPPORT_CHN_SMART_MANAGER = "com.samsung.android.sm_cn".equals(SemFloatingFeature.getInstance().getString("SEC_FLOATING_FEATURE_SMARTMANAGER_CONFIG_PACKAGE_NAME", "com.samsung.android.sm"));
    /* FEATURE_DESCRIPTION : if true, screen recorder tile is provided for global using smart screen capture*/
    public static final boolean QPANEL_SUPPORT_SCREEN_RECORDER_=  SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_SCREEN_RECORDER");
    /* FEATURE_DESCRIPTION : Support navigation scrim */
    public static final boolean QPANEL_SUPPORT_NAVIBAR_SCRIM = !QPANEL;
    /* FEATURE_DESCRIPTION : Support expanded 2nd panel */
    public static final boolean QPANEL_SUPPORT_EXPANDED_2ND_PANEL = QPANEL;
    /* FEATURE_DESCRIPTION : Disable Location Tile for Restricted User if Location service is disabled for the user */
    public static final boolean QPANEL_SUPPORT_DISABLE_LOCATION_FOR_RESTRICTED_USER = true;
    /* FEATURE_DESCRIPTION : Support to enable popup in Location qstile */
    public static final boolean QPANEL_SUPPORT_QS_LOCATION_ENABLE_POPUP = true; // TODO: QPANEL_IS_VZW_POPUP || QPANEL_IS_DCM_POPUP;
    public static final boolean QPANEL_SUPPORT_GPS_IN_LOCATION = false;//QPANEL_IS_CHINA_POPUP;
    public static final boolean QPANEL_SUPPORT_NEW_CUSTOM_TILE_BADGE_ICON = QPANEL;
    public static final boolean QPANEL_SUPPORT_SEC_STYLE_CUSTOMIZER = true;
    /* FEATURE_DESCRIPTION : Support transition aniamtor */
    public static final boolean QPANEL_SUPPORT_TRANSITION_ANIMATOR = true;
    /* FEATURE_DESCRIPTION : Support media and devices buttons on quick panel */
    public static final boolean QPANEL_SUPPORT_MEDIA_DEVICES_BAR = (Build.VERSION.SEM_PLATFORM_INT >= 100500);
    /* FEATURE_DESCRIPTION : FEATURE of remove side padding, GED has side padding of notificationpanel as 4dp */
    public static final boolean QPANEL_REMOVE_SIDE_PADDING = QPANEL;
    /* FEATURE_DESCRIPTION : FEATURE of Flashlight Tile */
    public static final boolean QPANEL_FLASHLIGHT_TILE = QPANEL_SUPPORT_SEC_STYLE_TILE_BASE;
    /* FEATURE_DESCRIPTION : Enable Torch Intensity*/
    public static final boolean QPANEL_ENABLE_TORCH_INTENSITY = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_CAMERA_SUPPORT_TORCH_BRIGHTNESS_LEVEL");
    /* FEATURE_DESCRIPTION : Flashlight can be controlled via intent */
    public static final boolean QPANEL_SUPPORT_FLASHLIGHT_CONTROL = QPANEL_SUPPORT_SEC_STYLE_TILE_BASE;
    /* FEATURE_DESCRIPTION : Make OFF State when Flashlight is unavailable */
    public static final boolean QPANEL_SUPPORT_FLASHLIGHT_UNAVAILABLE_TO_OFF_STATE = QPANEL_SUPPORT_SEC_STYLE_TILE_BASE;
    /* FEATURE_DESCRIPTION : Support SEC concept of content description in qstile */
    public static final boolean QPANEL_SUPPORT_SEC_CONTENT_DESCRIPTION = true;
    /* FEATURE_DESCRIPTION : FEATURE of Dnd Mode Tile */
    public static final boolean QPANEL_DND_TILE = QPANEL_SUPPORT_SEC_STYLE_TILE_BASE;
    /* FEATURE_DESCRIPTION : FEATURE of Wifi Mode Tile */
    public static final boolean QPANEL_WIFI_TILE = QPANEL_SUPPORT_SEC_STYLE_TILE_BASE;
    /* FEATURE_DESCRIPTION : FEATURE of Wifi Mode Tile */
    public static final boolean QPANEL_HOTSPOT_TILE = QPANEL_SUPPORT_SEC_STYLE_TILE_BASE;
     /* FEATURE_DESCRIPTION : Support the data roaming in MobileData qstile*/
    public static final boolean QPANEL_SUPPORT_DATA_ROAMING_IN_MOBILE_DATA = QPANEL_IS_KOREA_POPUP;
    /* FEATURE_DESCRIPTION : Support to show DATA ON/OFF POPUP*/
    public static final boolean QPANEL_SUPPORT_MOBILE_DATA_ON_OFF_POPUP_FOR_KOR = QPANEL_IS_KOREA_POPUP;
    /* FEATURE_DESCRIPTION : Support to show DATA OFF POPUP*/
    public static final boolean QPANEL_SUPPORT_MOBILE_DATA_OFF_POPUP = true; // TODO: QPANEL_IS_USA_POPUP|| QPANEL_IS_JAPAN_POPUP;
    public static final boolean QPANEL_SUPPORT_MOBILE_DATA_NOT_DISABLE_VOLTE_CALL = true; // TODO: QPANEL_IS_VZW_POPUP;
    /* FEATURE_DESCRIPTION : Support VOLTE */
    public static final boolean QPANEL_SUPPORT_VOLTE = !QPANEL_SUPPORT_WIFI_ONLY;
    /* FEATURE_DESCRIPTION : Support sim check when volte enable */
    public static final boolean QPANEL_SUPPORT_VOLTE_CHECK_OPERATOR = true;
    /* FEATURE_DESCRIPTION : Support Panel Color Manager on Quickpanel for Night Mode and QSColoring */
    public static final boolean QPANEL_COLOR_MANAGER = QPANEL;
    public static final boolean QPANEL_NIGHT_MODE_WITHOUT_REINFLATE = QPANEL_COLOR_MANAGER && true; // PEACE: Rune.QPANEL_SUPPORT_NIGHT_MODE_NOT_REINFLATE
    public static final boolean QPANEL_QUICKSTAR_QS_COLORING = QPANEL_COLOR_MANAGER && true;
    /* FEATURE_DESCRIPTION : Feature for Power button on quickpanel */
    public static final boolean QPANEL_SUPPORT_POWER_BUTTON = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_SETTINGS_SUPPORT_FUNCTION_KEY_MENU")
            && !SYSUI_IS_FACTORY_BINARY;
    /* FEATURE_DESCRIPTION : Support Quickpanel Backup and Restore */
    public static final boolean QPANEL_SUPPORT_BACKUP_RESTORE = QPANEL;
    /* FEATURE_DESCRIPTION : SEC_STYLE QS background */
    public static final boolean QPANEL_SUPPORT_ROUNDED_BACKGROUND = QPANEL;
    /* FEATURE_DESCRIPTION : Support eSIM for quick panel`s multi sim switch manager*/
    public static final boolean QPANEL_SUPPORT_ESIM = QPANEL && SemCscFeature.getInstance().getBoolean("CscFeature_RIL_SupportEsim");
    /* FEATURE_DESCRIPTION : Support media and devices buttons on quick panel */
    public static final boolean QPANEL_SUPPORT_MULTISIM_BAR = QPANEL_SUPPORT_BAR_CONTROLLER;
    /* FEATURE_DESCRIPTION : Support quick panel`s multi sim switch manager*/
    public static final boolean QPANEL_SUPPORT_QS_MULTISIM_SWITCH_MANAGER = QPANEL_SUPPORT_BAR_CONTROLLER;
    /* FEATURE_DESCRIPTION : Support first sim lock feature for quick panel`s multi sim switch manager*/
    public static final boolean QPANEL_SUPPORT_FIRST_SIM_LOCK = QPANEL_SUPPORT_BAR_CONTROLLER && "XNX".equals(CONFIG_QS_POPUP_BRANDING);
    /* FEATURE_DESCRIPTION : Tile state Logging */
    public static final boolean QPANEL_SUPPORT_TILE_STATE_LOGGING = QPANEL;
    /* FEATURE_DESCRIPTION : WorkMode Tile */
    public static final boolean QPANEL_SUPPORT_SEC_WORKMODE_TILE = QPANEL;
    //--------------------------------------------------------//
    // QuickStar Constants (QsTuner)                          //
    //--------------------------------------------------------//
    public static final String QUICKSTAR_TAG = "[QuickStar]";                 // PEACE: QUICKSTAR_DEBUG_TAG
    public static final boolean QUICKSTAR_SUPPORT = QUICKPANEL;
    public static final boolean QUICKSTAR_SLIM_INDICATOR = QUICKSTAR_SUPPORT; // PEACE: QPANEL_SUPPORT_SIMPLE_INDICATOR
    public static final boolean QUICKSTAR_QS_COLORING    = QUICKSTAR_SUPPORT; // PEACE: QPANEL_SUPPORT_COLORING_QPANEL
    public static final boolean QUICKSTAR_NOTI_FREEFORM  = QUICKSTAR_SUPPORT; // PEACE: NOTI_SUPPORT_FREEFORM

    //--------------------------------------------------------//
    // PowerUI Constants                                    //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Support Samsung's common battery concept */
    public static final boolean PWRUI_SUPPORT_SEC_COMMON_BATTERY_CONCEPT = PWRUI;
    /* FEATURE_DESCRIPTION : Support Samsung's low battery concept */
    public static final boolean PWRUI_SUPPORT_SEC_LOW_BATTERY_CONCEPT = PWRUI;
    /* FEATURE_DESCRIPTION : Support china smart manager */
    public static final boolean PWRUI_SUPPORT_CHN_SMART_MANAGER = PWRUI && "com.samsung.android.sm_cn".equals(
            SemFloatingFeature.getInstance().getString("SEC_FLOATING_FEATURE_SMARTMANAGER_CONFIG_PACKAGE_NAME", "com.samsung.android.sm"));
    /* FEATURE_DESCRIPTION : Support power saving mode in PowerUI */
    public static final boolean PWRUI_SUPPORT_POWERUI_POWER_SAVING_MODE = true;
    /* FEATURE_DESCRIPTION : Play battery power related sound */
    public static final boolean PWRUI_SUPPORT_SEC_BATTERY_POWER_SOUND = PWRUI;
    /* FEATURE_DESCRIPTION : Battery charging notice show */
    public static final boolean PWRUI_SUPPORT_BATTERY_CHARGING_NOTICE = PWRUI;
    /* FEATURE_DESCRIPTION : Support charging remaining time calcuration */
    public static final boolean PWRUI_SUPPORT_BATTERY_CHARGING_ESTIMATE_TIME = PWRUI && (new File("/sys/class/power_supply/battery/time_to_full_now")).exists();
    /* FEATURE_DESCRIPTION : Support wireless charger motion detection */
    public static final boolean PWRUI_SUPPORT_WIRELESS_CHARGER_MOTION_DETECTION = PWRUI;
    /* FEATURE_DESCRIPTION : Support Charger conncetion animation */
    public static final boolean PWRUI_SUPPORT_BATTERY_CHARGER_CONNECTION_VI = PWRUI;
    /* FEATURE_DESCRIPTION : Support wireless charger FOD popup */
    public static final boolean PWRUI_SUPPORT_WIRELESS_CHARGER_FOD_POPUP = PWRUI;
    /* FEATURE_DESCRIPTION : Support water protection popup */
    public static final boolean PWRUI_SUPPORT_WATER_PROTECTION_POPUP = PWRUI;
    /* FEATURE_DESCRIPTION : Support USB damage protection popup */
    public static final boolean PWRUI_SUPPORT_USB_DAMAGE_PROTECTION_POPUP = PWRUI;
    /* FEATURE_DESCRIPTION : Battery health state check */
    public static final boolean PWRUI_SUPPORT_BATTERY_HEALTH_CHECK = PWRUI;
    /* FEATURE_DESCRIPTION : device supports LED or not */
    public static final boolean PWRUI_SUPPORT_LED = PWRUI && (new File("/sys/class/sec/led/led_pattern")).exists();
    /* FEATURE_DESCRIPTION : Support keep display dimming in battery health interruption */
    public static final boolean PWRUI_SUPPORT_KEEP_DIMMING_AT_BATTERY_HEALTH_INTERRUPTION = PWRUI && (SYSUI_IS_TABLET_DEVICE || !PWRUI_SUPPORT_LED);
    /* FEATURE_DESCRIPTION : Support battery cooldown status check */
    public static final boolean PWRUI_SUPPORT_COOLDOWN_AND_SAFE_MODE = PWRUI;
    /* FEATURE_DESCRIPTION : Support unintentional LCD on pop up */
    public static final boolean PWRUI_SUPPORT_UNINTENTIONAL_LCD_ON_POPUP = PWRUI;
    /* FEATURE_DESCRIPTION : Support Power Sharing popup*/
    public static final boolean PWRUI_SUPPORT_POWER_SHARING_POPUP = PWRUI && SYSUI_IS_TABLET_DEVICE;
    /* FEATURE_DESCRIPTION : Support Battery swelling notice  */
    public static final boolean PWRUI_SUPPORT_BATTERY_SWELLING_NOTICE = PWRUI;
    /* FEATURE_DESCRIPTION : Support incompatible charger check */
    public static final boolean PWRUI_SUPPORT_INCOMPATIBLE_CHARGER_CHECK = PWRUI && "VZW".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : Support unintentional LCD on pop up's ear proximity */
    public static final boolean PWRUI_SUPPORT_EAR_PROXIMITY = PWRUI && SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_EAR_PROXIMITY;
    /* FEATURE_DESCRIPTION : Support VZW's specific request */
    public static final boolean PWRUI_SUPPORT_SPECIFIC_POWER_REQUEST_BY_VZW = PWRUI && "VZW".equals(STATBAR_ICON_BRANDING);
    /* FEATURE_DESCRIPTION : Support China's specific request */
    public static final boolean PWRUI_SUPPORT_SPECIFIC_POWER_REQUEST_BY_CHN = PWRUI && "China".equalsIgnoreCase(SystemProperties.get("ro.csc.country_code"));
    /* FEATURE_DESCRIPTION : Support JPN's specific request */
    public static final boolean PWRUI_SUPPORT_SPECIFIC_POWER_REQUEST_BY_JPN = PWRUI && ("DCM".equals(STATBAR_ICON_BRANDING)
            || "KDI".equals(STATBAR_ICON_BRANDING) || "SBM".equals(STATBAR_ICON_BRANDING) || "XJP".equals(STATBAR_ICON_BRANDING));
    /* FEATURE_DESCRIPTION : Support low battery dump */
    public static final boolean PWRUI_SUPPORT_LOW_BATTERY_DUMP = PWRUI;
    /* FEATURE_DESCRIPTION : Support FTA mode */
    public static final boolean PWRUI_SUPPORT_FTA_MODE = PWRUI;
    /* FEATURE_DESCRIPTION : Support automatic test mode */
    public static final boolean PWRUI_SUPPORT_AUTOMATIC_TEST_MODE = PWRUI;

    //--------------------------------------------------------//
    // Notification Constants                                 //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Support transition between KEYGUARD and SHADE_LOCKED */
    public static final boolean NOTI_SUPPORT_SEC_BARSTATE_KEEPLOCK = NOTI;
    /* FEATURE_DESCRIPTION : Common feature to notify the panel state */
    public static final boolean NOTI_SUPPORT_PANEL_STATE_NOTIFIER = NOTI;
    /* FEATURE_DESCRIPTION : Special feature to notify the panel state for KDDI */
    public static final boolean NOTI_SUPPORT_PANEL_STATE_NOTIFIER_FOR_KDDI = NOTI &&
            "KDI".equals(STATBAR_ICON_BRANDING);
    /* FEATURE DESCRIPTION : Support DeX */
    public static final boolean NOTI_SUPPORT_DEX = NOTI && SYSUI_SUPPORT_DEX;

    /* FEATURE_DESCIPTION : Support sec notification policy*/
    public static final boolean NOTI_SUPPORT_SEC_POLICY = NOTI;
    /* FEATURE_DESCIPTION : Support sec notification policy : sec priority*/
    public static final boolean NOTI_SUPPORT_SEC_POLICY_PRIORITY = NOTI_SUPPORT_SEC_POLICY;
    /* FEATURE_DESCIPTION : Support sec notification policy : do not make vibration on panel for DA*/
    public static final boolean NOTI_SUPPORT_SEC_POLICY_VIBRATION_TOUCH_ON_NOTIIFCATION = !NOTI_SUPPORT_SEC_POLICY;
    /* FEATURE_DESCRIPTION : Common feature determine sec blockable condition of notifiaction */
    public static final boolean NOTI_SUPPORT_SEC_POLICY_BLOCK_CONDITION = NOTI_SUPPORT_SEC_POLICY;
    /* FEATURE_DESCRIPTION : support lockscreen visiblity on none secure lockscreen */
    public static final boolean NOTI_SUPPORT_SEC_POLICY_LOCKSCREEN_VISIBILITY = NOTI_SUPPORT_SEC_POLICY;
    /* FEATURE_DESCRIPTION : Support removing sensitive notification on keyguard  */
    public static final boolean NOTI_SUPPORT_SEC_POLICY_REMOVE_SENSITIVE_NOTIFICATION  = NOTI_SUPPORT_SEC_POLICY;
    /* FEATURE_DESCRIPTION : support lockscreen notification type*/
    public static final boolean NOTI_SUPPORT_LOCKSCREEN_NOTIFICATION_TYPE = NOTI_SUPPORT_SEC_POLICY;
    /* FEATURE_DESCRIPTION : Notification icons only concept  */
    public static final boolean NOTI_SUPPORT_LOCKSCREEN_NOTIFICATION_TYPE_ICONS_ONLY = NOTI_SUPPORT_LOCKSCREEN_NOTIFICATION_TYPE;


    /* FEATURE_DESCIPTION : Support update min notification view as soon as importance changed*/
    public static final boolean NOTI_SUPPORT_AOSP_BUGFIX_MIN_NOTIIFCATION = NOTI;
    /* FEATURE_DESCRIPTION : Support disable_expand while ontracking */
    public static final boolean NOTI_SUPPORT_AOSP_BUGFIX_DISABLE_EXPAND = NOTI;
    /* FEATURE_DESCRIPTION : pending infalte need to update when densitiy changed */
    public static final boolean NOTI_SUPPORT_AOSP_BUGFIX_PENDING_INFLATE = NOTI;
    /* FEATURE_DESCRIPTION : Support sensitive notification for gruop summary on keyguard */
    public static final boolean NOTI_SUPPORT_AOSP_BUGFIX_SENSITIVE_GROUP_NOTIFICATION = NOTI;
    /* FEATURE_DESCRIPTION : Support leash animation */
    public static final boolean NOTI_SUPPORT_AOSP_BUGFIX_LEASH_ANIMATION = NOTI;

    /* FEATURE_DESCRIPTION : Support Remote input feature" */
    public static final boolean NOTI_SUPPORT_REMOTE_INPUT = NOTI;
    /* FEATURE_DESCRIPTION : Support Remote input is set privateImeOptions to "disableImage=true" in the remote_input.xml
     * Even if Turn off NOTI_SUPPORT_REMOTE_INPUT feature , NOTI_SUPPORT_REMOTE_INPUT_DISABLE_IMAGE feature won't turn off
     * because xml is modified */
    public static final boolean NOTI_SUPPORT_REMOTE_INPUT_DISABLE_IMAGE = NOTI_SUPPORT_REMOTE_INPUT;
    /* FEATURE_DESCRIPTION : Support limit max number of chars input from notification direct reply
     * This can be done by adding extra info on RemoteInput object as below
     *
     *  Bundle bundle = new Bundle();
     *  bundle.putInt("maxLength", 10);
     *
     *  RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
     *         .addExtras(bundle)
     *         .build();
     *
     *  For SMS, putBoolean("isSms", true) will limit the length as 160 chars with GSM-7bit encoding.
     *
     */
    public static final boolean NOTI_SUPPORT_REMOTE_INPUT_LIMIT_TOAST = NOTI_SUPPORT_REMOTE_INPUT;
    /* FEATURE_DESCRIPTION : Support Notification Large Icon Click  */
    public static final boolean NOTI_SUPPORT_REMOTE_INPUT_LARGEICON_CLICK  = NOTI_SUPPORT_REMOTE_INPUT;

    /* FEATURE DESCRIPTION : NotificationManagerRune.SUPPORT_NOTIFICATION_EASY_MUTE */
    public static final boolean NOTI_SUPPORT_NOTIFICATION_EASY_MUTE = NOTI;


    /* FEATURE_DESCRIPTION : Support GED View item feature*/
    public static final boolean NOTI_SUPPORT_GED_VIEW_IN_NSSL = !NOTI;
    /* FEATURE_DESCRIPTION : apply Samsung Notification GUI concept EmptyShadeView(SE10.x) */
    public static final boolean NOTI_SUPPORT_GED_VIEW_IN_NSSL_EMPTY_SHADE_VIEW = NOTI_SUPPORT_GED_VIEW_IN_NSSL;
    /* FEATURE_DESCRIPTION : apply Samsung Notification GUI concept FooterView(SE10.x) */
    public static final boolean NOTI_SUPPORT_GED_VIEW_IN_NSSL_FOOTER_VIEW = NOTI_SUPPORT_GED_VIEW_IN_NSSL;
    /* FEATURE_DESCRIPTION : apply Samsung Notification GUI concept SectionHeaderView(SE10.x) */
    /* TODO : remove NOTI_SUPPORT_GED_VIEW_IN_NSSL_SECTION_HEADER_VIEW */
    public static final boolean NOTI_SUPPORT_GED_VIEW_IN_NSSL_SECTION_HEADER_VIEW = NOTI;
    /* FEATURE_DESCRIPTION : apply Samsung Notification expand button in NSSL section header */
    public static final boolean NOTI_SUPPORT_EXPAND_BUTTON_IN_NSSL_SECTION_HEADER_VIEW = NOTI;
    /* FEATURE_DESCRIPTION : Support Do not disturb notification */
    public static final boolean NOTI_SUPPORT_DND_ONGOING_ALERT = NOTI;
    /* FEATURE_DESCRIPTION : Support app crash prevention when multi touch event is delivered to panel */
    public static final boolean NOTI_SUPPORT_AOSP_BUGFIX_MULTI_TOUCH_SYNCHRONIZATION = NOTI;

    /* FEATURE_DESCRIPTION : Show NotificationStackScrollLayout Bottom Bar for (ex : setting, Clear All) */
    public static final boolean NOTI_SUPPORT_STATIC_SHELF = NOTI;
    /* FEATURE_DESCRIPTION : NotificaitonShelf in items such as textArea, buttons, divider etc  */
    public static final boolean NOTI_SUPPORT_STATIC_SHELF_IN_ITEMS_SETTING = NOTI_SUPPORT_STATIC_SHELF;
    /* FEATURE_DESCRIPTION : Controll NotificationShelf in Icon container including icon size, padding etc */
    public static final boolean NOTI_SUPPORT_STATIC_SHELF_IN_ITEMS_SETTING_ICON_CONTAINER = NOTI_SUPPORT_STATIC_SHELF_IN_ITEMS_SETTING;
    /* FEATURE_DESCRIPTION : Show NotificationStackScrollLayout Bottom Bar Button VI */
    public static final boolean NOTI_SUPPORT_STATIC_SHELF_BUTTON_VI = NOTI_SUPPORT_STATIC_SHELF;
    /* FEATURE_DESCRIPTION : Support qs expansion without NSSL */
    public static final boolean NOTI_SUPPORT_STATIC_SHELF_NOT_OVERLAPPED_QUICK_PANEL = NOTI_SUPPORT_STATIC_SHELF;
    /* FEATURE_DESCRIPTION : Support set the NotificaitonShelf height  */
    public static final boolean NOTI_SUPPORT_STATIC_SHELF_HEIGHT = NOTI_SUPPORT_STATIC_SHELF;
    /* FEATURE_DESCRIPTION : Support RTL reply icon */
    public static final boolean NOTI_SUPPORT_RTL_REPLY_ICON = NOTI;
    /* FEATURE_DESCRIPTION : Support scroll automatically when gut is expanding */
    public static final boolean NOTI_SUPPORT_AUTO_SCROLL_GUT_EXPANDED = NOTI;
    // FEATURE_DESCRIPTION : Support to expand notification panel in SHADE_LOCKED */
    public static final boolean NOTI_SUPPORT_EXPAND_LOCKED_SHADE = NOTI;
    /* FEATURE_DESCRIPTION : Support AOD noti double tap to expand panel */
    public static final boolean NOTI_SUPPORT_AOD_DOUBLE_TAP_NOTI_EXPAND = NOTI;
    /* FEATURE_DESCRIPTION : Support BlueLight Filter*/
    public static final boolean QPANEL_SUPPORT_BLUELIGHT_FILTER = SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_MDNIE_BLUE_FILTER;
    /* FEATURE_DESCRIPTION : Reading mode feature HW(0), SW(1), NOTSUPPORT(2), default is 0  */
    public static final int QPANEL_SUPPORT_READINGMODE = Integer.parseInt(SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_CONFIG_READING_MODE);
    public static final boolean QPANEL_SUPPORT_SECONDARY_TILE_LABEL = false;
    /* FEATURE_DESCRIPTION : double tap notification on lockscreen timeout */
    public static final boolean NOTI_SUPPORT_DOUBLETAP_ON_LOCKSCREEN_NOT_GED = NOTI;
    /* FEATURE_DESCRIPTION : Block double tap notification on lockscreen to execute notification */
    public static final boolean NOTI_SUPPORT_DOUBLETAP_ON_LOCKSCREEN = NOTI
            && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_BLOCKDOUBLETAPNOTIONLOCKSCREEN, false));
    /* FEATURE_DESCRIPTION : Support new UX concept : delete extra duration for full shade animation */
    public static final boolean NOTI_SUPPORT_SEC_STYLE_FULL_SHADE_ANIMATION_NO_EXTRA_DURATION = NOTI;
    /* FEATURE_DESCRIPTION: Support log for custom notification, */
    public static final boolean NOTI_SUPPORT_SEC_LOG_CUSTOM_NOTIFICATION = NOTI;
    /* FEATURE_DESCRIPTION: Support view transform for custom notification, */
    public static final boolean NOTI_SUPPORT_REMAINING_VIEW_TRANSFORMATION = NOTI;
    /* FEATURE_DESCRIPTION : Support knox notification layout */
    public static final boolean NOTI_SUPPORT_KNOX_NOTIFICATION_LAYOUT = NOTI;
    /* FEATURE_DESCRIPTION : apply SEC style overflow number */
    public static final boolean NOTI_SUPPORT_SEC_STYLE_OVERFLOW_NUMBER = NOTI;



    /* FEATURE_DESCRIPTION : Support to clear all notifications for "com.sec.aecmonitor.ONE_CYCLE_FINISH" */
    public static final boolean NOTI_SUPPORT_PERFORMANCE = NOTI;
    /* FEATURE_DESCRIPTION : Support mum ringtone player P181108-05093 */
    public static final boolean NOTI_SUPPORT_BUG_FIX_MUM_RINGTONE = NOTI;
    /* FEATURE_DESCRIPTION : Show SEC Notification Background radius */
    public static final boolean NOTI_SUPPORT_SEC_NOTIFICATION_PANEL_BACKGROUND_RADIUS = NOTI;
    /* FEATURE_DESCRIPTION : Support NotificationStackScrollLayout Background */
    public static final boolean NOTI_SUPPORT_SEC_NSSL_BACKGROUND = NOTI;

    /* FEATURE_DESCRIPTION : Common feature for Samsung Simple status bar concept */
    public static final boolean NOTI_SUPPORT_SIMPLE_STATUSBAR = NOTI;
    /* FEATURE_DESCRIPTION : Support Notification Count on StatusBar */
    public static final boolean NOTI_SUPPORT_COUNT_NOTIFICATION = NOTI;
    /* FEATURE_DESCRIPTION : Common feature for Samsung style guts */
    public static final boolean NOTI_SUPPORT_SEC_GUTS = NOTI;
    /* FEATURE_DESCRIPTION : Support preview for lockscreen setting  */
    public static final boolean NOTI_SUPPORT_PREVIEW  = NOTI;
    /* FEATURE_DESCRIPTION : Support Notification color picker */
    public static final boolean NOTI_SUPPORT_COLOR_PICKER = NOTI;
    /* FEATURE_DESCRIPTION : Support Notification increase padding for expanded notification */
    public static final boolean NOTI_SUPPORT_USE_INCREASE_PADDING_FOR_EXPANDED_NOTIFICATION = NOTI;
    /* FEATURE_DESCRIPTION : Support Notification colorful small icon */
    public static final boolean NOTI_SUPPORT_COLORFUL_SMALL_ICON = NOTI;
    /* FEATURE_DESCRIPTION : Support Set NSSL Bottom margin */
    public static final boolean NOTI_SUPPORT_NSSL_BOTTOM_MARGIN = NOTI;
    /* FEATURE_DESCRIPTION : Support SEC style ztanslation */
    public static final boolean NOTI_SUPPORT_SEC_STYLE_NOTIFICATION_ZTRANSLATION = NOTI;
    /* FEATURE_DESCRIPTION : Support disable system expand */
    public static final boolean NOTI_SUPPORT_SEC_STYLE_SYSTEM_EXPAND_DISABLE = NOTI;
    /* FEATURE_DESCRIPTION : Support NSSL Alpha animation VI */
    public static final boolean NOTI_SUPPORT_QUICK_PANEL_EXPAND_NSSL_ALPHA_VI = NOTI;
    /* FEATURE_DESCRIPTION : Remove ambient view for SEC notification layout */
    public static final boolean NOTI_SUPPORT_AMBIENT_VIEW = !NOTI;
    /* FEATURE_DESCRIPTION : apply Samsung Notification GUI concept : nssl should be tinted when leash animation is in progress */
    public static final boolean NOTI_SUPPORT_SEC_STYLE_NSSL_LEASH = NOTI;
    /* FEATURE_DESCRIPTION : Support new UX concept : there is no header contents divider in SEP10.0 */
    public static final boolean NOTI_SUPPORT_SEC_STYLE_HEADER_NO_DIVIDER = NOTI;
    /* FEATURE_DESCRIPTION : Support heads up notification restriction */
    public static final boolean NOTI_SUPPORT_HEADS_UP_RESTRICTION = NOTI;
    /* FEATURE_DESCRIPTION : Support should_head_up log when not DEBUG*/
    public static final boolean NOTI_SUPPORT_SHOULD_HEADS_UP_LOG  = NOTI;
    /* FEATURE_DESCRIPTION : Support service box notification */
    public static final boolean NOTI_SUPPORT_SERVICEBOX_NOTIFICATION = NOTI;
    /* FEATURE_DESCRIPTION : Support NotificationPanel not open bug when use mouse*/
    public static final boolean NOTI_SUPPORT_NOTIFICATION_PANEL_NOT_OPEN_BUG_WHEN_USE_MOUSE = NOTI;
    /* FEATURE_DESCRIPTION : Support snooze and info view on lockscreen */
    public static final boolean NOTI_SUPPORT_SNOOZE_AND_INFO_VIEW_ON_LOCK = NOTI;
    /* FEATURE_DESCRIPTION : Support snooze and info view on lockscreen */
    public static final boolean NOTI_SUPPORT_SEC_STYLE_CHILDREN_CONTAINER_NO_DIVIDER = !NOTI;

    //--------------------------------------------------------//
    // Keyguard Constants                                     //
    //--------------------------------------------------------//
    /** FEATURE_DESCRIPTION : Q OS Porting TODO Feature */
    public static final boolean KEYGUARD_PORTING_Q_TODO = false;

    /* FEATURE_DESCRIPTION : 1. Feature for Keyguard AOSP bug fix */
    public static final boolean KEYGUARD_SUPPORT_AOSP_BUG_FIX = KEYGUARD;
    
    /* FEATURE_DESCRIPTION : 2. Support that user can unlock by swiping in any direction. */
    public static final boolean KEYGUARD_SUPPORT_ALL_DIRECTIONS_SWIPE_UNLOCK = KEYGUARD;

    /* FEATURE_DESCRIPTION : 3. Some kind of saving modes affect screen time out. */
    public static final boolean KEYGUARD_SUPPORT_USER_ACTIVITY_TIMEOUT = KEYGUARD;

    /* FEATURE_DESCRIPTION : 4. When screen turns on, deliver wakup reason from PMS */
    public static final boolean KEYGUARD_SUPPORT_WAKEUP_REASON = KEYGUARD;

    /* FEATURE_DESCRIPTION : 5. When screen turns on, deliver wakup reason from PMS */
    public static final boolean KEYGUARD_SUPPORT_DELAY_NOTIFY_DRAWN = KEYGUARD;

    /* FEATURE_DESCRIPTION : 6. Control lock and unlock sound in lockscreen */
    public static final boolean KEYGUARD_SUPPORT_LOCK_SOUND = KEYGUARD;

    /* FEATURE_DESCRIPTION : 7. Lockscreen feature for unlock VI */
    public static final boolean KEYGUARD_SUPPORT_UNLOCK_VI = KEYGUARD;

    /* FEATURE_DESCRIPTION : 8. Support SEC Keyguard Display Manager Feature.
     * This usually used to check if device supports Smart Mirroring. */
    public static final boolean KEYGUARD_SUPPORT_DISPLAY_MANAGER = KEYGUARD;

    /* FEATURE_DESCRIPTION : 9. Common feature to set keyguard dismiss action */
    public static final boolean KEYGUARD_SUPPORT_DISMISS_ACTION = KEYGUARD;

    /* FEATURE_DESCRIPTION : 10. To improve performance of keyguard */
    public static final boolean KEYGUARD_SUPPORT_PERFORMANCE_BOOSTER = KEYGUARD;

    /* FEATURE_DESCRIPTION : 11. disable dokeyguard policy */
    public static final boolean KEYGUARD_SUPPORT_DISABLE_LOCK = KEYGUARD;

    /* FEATURE_DESCRIPTION : 12. For monitoring Lock Type without LockSettingsService binder call */
    public static final boolean KEYGUARD_SUPPORT_CACHED_LOCK_TYPE = KEYGUARD;

    /* FEATURE DESCRIPTION : 13. Append additional log in GED source files & print event log in KeyguardManager class */
    public static final boolean KEYGUARD_SUPPORT_ADDITIONAL_LOG = KEYGUARD;

    /* FEATURE_DESCRIPTION : 14. CSC feature : CscFeature_LockScreen_DisableUnlockVI */
    public static final boolean KEYGUARD_SUPPORT_DISABLE_BIO_UNLOCK_VI = KEYGUARD && SemCscFeature.getInstance().getBoolean(
            "CscFeature_LockScreen_DisableUnlockVI", false);

    /* FEATURE_DESCRIPTION : 15. Common feature to set PendingIntent from applications while locked */
    public static final boolean KEYGUARD_SUPPORT_PENDING_INTENT = KEYGUARD;

    /* FEATURE_DESCRIPTION : 16. Common feature for hide behind bouncer implementations  */
    public static final boolean KEYGUARD_SUPPORT_HIDE_BEHIND_BOUNCER = KEYGUARD;

    /* FEATURE_DESCRIPTION : 17. Disable notification if it carrier lock state */
    public static final boolean KEYGUARD_SUPPORT_DISABLE_NOTI_IN_CARRIER_LOCK = KEYGUARD;

    /* FEATURE_DESCRIPTION : 18. Keyguard feature for broadcasting keyguard states*/
    public static final boolean KEYGUARD_SUPPORT_BR_STATE = KEYGUARD;

    /* FEATURE_DESCRIPTION : 19. Keyguard feature for extra user present broadcasting*/
    public static final boolean KEYGUARD_SUPPORT_EXTRA_USER_PRESENT = KEYGUARD;

    /* FEATURE_DESCRIPTION : 20. Common feature for unlock when spen detached. */
    public static final boolean KEYGUARD_SUPPORT_UNLOCK_WHEN_SPEN_DETACHED = KEYGUARD;

    /* FEATURE_DESCRIPTION : 21. Support FBE(file based encryption) concept */
    public static final boolean KEYGUARD_SUPPORT_FBE = KEYGUARD;

    /* FEATURE_DESCRIPTION : 22. Unlock the swipe lock by any key of external keyboard */
    public static final boolean KEYGUARD_UNLOCK_BY_EXT_KEYBOARD = KEYGUARD;

    /* FEATURE_DESCRIPTION : 23. Common feature for Samsung factory test mode */
    public static final boolean KEYGUARD_SUPPORT_FACTORY_TEST_MODE = KEYGUARD;

    /* FEATURE_DESCRIPTION : 24. Lockscreen feature for COVER */
    public static final boolean KEYGUARD_SUPPORT_COVER = KEYGUARD && SYSUI_SUPPORT_COVER;

    /* FEATURE DESCRIPTION : 25. Improve the performance on wake and unlock from screen off */
    public static final boolean KEYGUARD_SUPPORT_WAKE_AND_UNLOCK_PERFORMANCE = KEYGUARD;

    /* FEATURE DESCRIPTION : 26. knox state monitor for keyguard */
    public static final boolean KEYGUARD_SUPPORT_KNOX_MONITOR = KEYGUARD && SYSUI_SUPPORT_KNOX_MONITOR;

    /* FEATURE_DESCRIPTION : 27. Common feature for KNOX UCM support */
    public static final boolean KEYGUARD_SUPPORT_KNOX_UCM = KEYGUARD && SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_UCS;

    /* FEATURE_DESCRIPTION : 28. Common feature for KNOX MDM support */
    public static final boolean KEYGUARD_SUPPORT_KNOX_MDM = KEYGUARD && SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM;

    /* FEATURE_DESCRIPTION : 29. Keyguard feature for supporting dual display model, such as Winner */
    public static final boolean KEYGUARD_SUPPORT_DUAL_DISPLAY = KEYGUARD && SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_DUAL_DISPLAY;

    /* FEATURE_DESCRIPTION : 30. Keyguard feature for control rotation */
    public static final boolean KEYGUARD_SUPPORT_ROTATION = KEYGUARD;

    /* FEATURE_DESCRIPTION : 31. Support sensor operation */
    public static final boolean KEYGUARD_SUPPORT_SENSOR_PICKUP = KEYGUARD;

    /* FEATURE_DESCRIPTION : 32. Support DeX */
    public static final boolean KEYGUARD_SUPPORT_DEX = KEYGUARD && SYSUI_SUPPORT_DEX;

    //--------------------------------------------------------//
    // Security Constants                                     //
    //--------------------------------------------------------//
    /** FEATURE_DESCRIPTION : Q OS Porting TODO Feature */
    public static final boolean SECURITY_PORTING_Q_TODO = false;

    /* FEATURE_DESCRIPTION : 1. Feature for Keyguard AOSP bug fix */
    public static final boolean SECURITY_SUPPORT_AOSP_BUG_FIX = SECURITY;
    
    /* FEATURE_DESCRIPTION : 2. SEC concept Keyguard security view Feature */
    public static final boolean SECURITY_SUPPORT_SEC_VIEW = SECURITY;

    /* FEATURE_DESCRIPTION : 3. Lockscreen feature for bouncer window  */
    public static final boolean SECURITY_SUPPORT_BOUNCER_WINDOW = SECURITY;

    /* FEATURE_DESCRIPTION : 4. Common feature for voice assistant SEC concept. */
    public static final boolean SECURITY_SUPPORT_ACCESSIBILITY = SECURITY;

    /* FEATURE_DESCRIPTION : 5. Common Feature for Remote lockscreen( FMM, RMM, KnoxGuard, Carrierlock(SKT)) */
    public static final boolean SECURITY_SUPPORT_REMOTE_LOCKSCREEN = SECURITY;

    /* FEATURE_DESCRIPTION : 6. Keyguard feature for setting max length of password  */
    public static final boolean SECURITY_SUPPORT_MAX_LENGTH_OF_PASSWORD = SECURITY;

    /* FEATURE_DESCRIPTION : 7. Provide vibration feedback on wrong pattern, pin, password inputs. */
    public static final boolean SECURITY_SUPPORT_VIBRATE_ON_WRONG_PASSWORD = SECURITY;

    /* FEATURE_DESCRIPTION : 8. Support Password Hint for KOR csc feature  */
    public static final boolean SECURITY_SUPPORT_PASSWORD_HINT = SECURITY && "KR".equals(SYSUI_COUNTRY_ISO);

    /* FEATURE_DESCRIPTION : 9. Verify without ok button press when N digits pin input */
    public static final boolean SECURITY_SUPPORT_SIMPLE_PIN = SECURITY && SemFloatingFeature.getInstance()
            .getBoolean("SEC_FLOATING_FEATURE_LOCKSCREEN_SUPPORT_SIMPLE_PIN");

    /* FEATURE_DESCRIPTION : 10. Show bouncer on swipe lock. It must be classified for patent reason. */
    public static final boolean SECURITY_SUPPORT_SHOWING_SWIPE_BOUNCER = SECURITY && "US_NORTH".equals(
            SemCscFeature.getInstance().getString(CscFeatureTagLockScreen.TAG_CSCFEATURE_LOCKSCREEN_CONFIGDISPLAYSHORCUTCIRCLEARROW));

    /* FEATURE_DESCRIPTION : 11. Common feature for KNOX MDM support */
    public static final boolean SECURITY_SUPPORT_DEVICE_POLICY = SECURITY && SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM;

    /**
     * FEATURE_DESCRIPTION : CSC feature : CscFeature_LockScreen_ConfigCarrierTextPolicy
     *
     * "UseDefaultPlmnValueFromNetwork" - SPR
     * "BlockCarrierTextWhenSimNotReady"- CHU
     * "UseCdmaCardText" - CTC
     * "UseFixedPlmnValueForUSC" - USC
     * "DisplayUsimText" - KOR Carrier ( SKT / KTT / LGT )
     * "DisplayPlmnOnBottom" - KOR Carrier ( SKT )
     * "UseSKTSimText" - SKT
     * "UseKTTSimText" - KTT
     * "UseDCMSimLockText" - DCM
     * "UseKDDISimText" - KDDI
     */
    private static final String SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY = SemCscFeature.getInstance()
            .getString(CscFeatureTagLockScreen.TAG_CSCFEATURE_LOCKSCREEN_CONFIGCARRIERTEXTPOLICY);
    /* FEATURE_DESCRIPTION : 12. */
    public static final boolean SECURITY_SUPPORT_KOR_USIM_TEXT = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("DisplayUsimText");
    /* FEATURE_DESCRIPTION : 13. */
    public static final boolean SECURITY_SUPPORT_SKT_USIM_TEXT = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("UseSKTSimText");
    /* FEATURE_DESCRIPTION : 14. */
    public static final boolean SECURITY_SUPPORT_KTT_USIM_TEXT = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("UseKTTSimText");
    /* FEATURE_DESCRIPTION : 15. */
    public static final boolean SECURITY_SUPPORT_DCM_USIM_TEXT = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("UseDCMSimLockText");
    /* FEATURE_DESCRIPTION : 16. */
    public static final boolean SECURITY_SUPPORT_KDDI_USIM_TEXT = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("UseKDDISimText");
    /* FEATURE_DESCRIPTION : 17. */
    public static final boolean SECURITY_SUPPORT_SPR_USIM_TEXT = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("UseDefaultPlmnValueFromNetwork");
    /* FEATURE_DESCRIPTION : 18. */
    public static final boolean SECURITY_SUPPORT_USC_USIM_TEXT = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("UseFixedPlmnValueForUSC");
    /* FEATURE_DESCRIPTION : 19. */
    public static final boolean SECURITY_SUPPORT_USE_CDMA_CARD_TEXT = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("UseCdmaCardText");
    /* FEATURE_DESCRIPTION : 20. */
    public static final boolean SECURITY_SUPPORT_BLOCK_CARRIER_TEXT_WHEN_SIM_NOT_READY = SECURITY && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("BlockCarrierTextWhenSimNotReady") || SECURITY_SUPPORT_KOR_USIM_TEXT;

    /**
     * FEATURE_DESCRIPTION : CSC feature : CscFeature_Lockscreen_ConfigCarrierSecurityPolicy
     *
     * List of string constants for each operator :
     * "FactoryResetProtectionWarning" - VZW (FRP waring)
     * "SupportSimPermanentDisable" - KOR
     * "UseSKTSimText" - SKT
     * "UseKTTSimText" - KTT
     * "FactoryResetWithoutUI" - ATT
     * "UseTMOSIMPINLock" - TMO
     * "UseSimcardManangerOnBoot" - CTC
     * "UseSamsungAccountAuth" - CHINA
     * "UseDCMSimLockText" - DCM
     * "DisableEMCallButtonBySimState" - JPN
     */
    private static final String SECURITY_VALUE_CARRIER_SECURITY_POLICY = SemCscFeature.getInstance().getString(
            CscFeatureTagLockScreen.TAG_CSCFEATURE_LOCKSCREEN_CONFIGCARRIERSECURITYPOLICY);
    /* FEATURE_DESCRIPTION : 21. */
    public static final boolean SECURITY_SUPPORT_SIM_PERM_DISABLED = SECURITY && SECURITY_VALUE_CARRIER_SECURITY_POLICY.contains("SupportSimPermanentDisable");
    /* FEATURE_DESCRIPTION : 22. */
    public static final boolean SECURITY_SUPPORT_NOT_REQUIRE_SIMPUK_CODE = SECURITY && SECURITY_VALUE_CARRIER_SECURITY_POLICY.contains("UseTMOSIMPINLock");
    /* FEATURE_DESCRIPTION : 23. */
    public static final boolean SECURITY_SUPPORT_WARNING_WIPE_OUT_MESSAGE = SECURITY && SECURITY_VALUE_CARRIER_SECURITY_POLICY.contains("FactoryResetProtectionWarning");
    /* FEATURE_DESCRIPTION : 24. */
    public static final boolean SECURITY_SUPPORT_FACTORY_RESET_WITHOUT_UI = SECURITY && SECURITY_VALUE_CARRIER_SECURITY_POLICY.contains("FactoryResetWithoutUI");
    /* FEATURE_DESCRIPTION : 25. */
    public static final boolean SECURITY_SUPPORT_VZW_INSTRUCTION = SECURITY && SECURITY_VALUE_CARRIER_SECURITY_POLICY.contains("FactoryResetProtectionWarning");

    /* FEATURE_DESCRIPTION : 26. Provide sim state changing to PIN_REQUIRED after pin passed */
    public static final boolean SECURITY_SUPPORT_CHECK_SIMPIN_PASSED = SECURITY;

    /* FEATURE_DESCRIPTION : 27. Support SIM Perso lock for KOR csc feature (CscFeature_RIL_USIMPersonalizationKOR) */
    public static final boolean SECURITY_SUPPORT_SIM_PERSO_LOCK = SECURITY && SemCscFeature.getInstance()
            .getBoolean(CscFeatureTagRIL.TAG_CSCFEATURE_RIL_USIMPERSONALIZATIONKOR);

    /* FEATURE_DESCRIPTION : 28. CSC feature : CscFeature_RIL_SupportAllRat determine sim card crash (CHC / CHN / TGY) */
    public static final boolean SECURITY_SUPPORT_ALL_RAT = SECURITY && SemCscFeature.getInstance()
            .getBoolean(CscFeatureTagRIL.TAG_CSCFEATURE_RIL_SUPPORTALLRAT);

    /* FEATURE_DESCRIPTION : 29. CSC feature : CscFeature_Common_ConfigUsimPersonalLockPwdLength(SKT) */
    public static final int SECURITY_VALUE_PERSO_LOCK_PASSWORD_LENGTH = SemCscFeature.getInstance()
            .getInteger(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_CONFIGUSIMPERSONALLOCKPWDLENGTH, 8);

    /* FEATURE_DESCRIPTION : 30. CSC feature : CscFeature_LockScreen_SupportToastSimUnlockSuccess (ATT / AIO / TEL) */
    public static final boolean SECURITY_SUPPORT_SIM_UNLOCK_TOAST = SECURITY && SemCscFeature.getInstance()
            .getBoolean(CscFeatureTagLockScreen.TAG_CSCFEATURE_LOCKSCREEN_SUPPORTTOASTSIMUNLOCKSUCCESS);

    /* FEATURE_DESCRIPTION : 31. Esim feature */
    public static final boolean SECURITY_SUPPORT_ESIM = SECURITY && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_COMMON_SUPPORT_EMBEDDED_SIM");

    /* FEATURE_DESCRIPTION : 32. Common feature for emergency call SEC concept. */
    public static final boolean SECURITY_SUPPORT_EMERGENCY_CALL = SECURITY;

    /* FEATURE_DESCRIPTION : 33. Common feature for KOR emergency call button. */
    public static final boolean SECURITY_SUPPORT_KOR_EMERGENCY_CALL_BUTTON = SECURITY && SECURITY_SUPPORT_KOR_USIM_TEXT;

    /* FEATURE_DESCRIPTION : 34. CSC Feature : CscFeature_LockScreen_ConfigEmergencyCallPolicy */
    public static final boolean SECURITY_SUPPORT_DISABLE_EMERGENCY_CALL_WHEN_OFFLINE = SECURITY && SemCscFeature.getInstance().getString(
            CscFeatureTagLockScreen.TAG_CSCFEATURE_LOCKSCREEN_CONFIGEMERGENCYCALLPOLICY).contains("DisableEmergencyCallWhenOffline");

    /* FEATURE_DESCRIPTION : 35. Hide Emergency Button */
    public static final boolean SECURITY_SUPPORT_HIDE_EMC_BUTTON_BY_SIMSTATE = SECURITY && SECURITY_VALUE_CARRIER_SECURITY_POLICY.contains("DisableEMCallButtonBySimState");

    /* FEATURE_DESCRIPTION : 36. CSC feature : CscFeature_LockScreen_DirectCallToEcc - return Australia */
    public static final boolean SECURITY_SUPPORT_DIRECT_CALL_TO_ECC = SECURITY && SemCscFeature.getInstance().getBoolean(
            CscFeatureTagLockScreen.TAG_CSCFEATURE_LOCKSCREEN_DIRECTCALLTOECC);

    /* FEATURE_DESCRIPTION : 37. Keyguard feature for fingerprint */
    public static final boolean SECURITY_SUPPORT_FINGERPRINT = SECURITY &&
            SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_FINGERPRINT;

    /* FEATURE_DESCRIPTION : 38. Keyguard feature for in-display fingerprint */
    public static final boolean SECURITY_SUPPORT_FINGERPRINT_IN_DISPLAY = SECURITY &&
            DeviceType.isInDisplayFingerprintSupported();

    /* FEATURE_DESCRIPTION : 39. Keyguard feature for optical in-display fingerprint */
    public static final boolean SECURITY_SUPPORT_FINGERPRINT_IN_DISPLAY_OPTICAL = SECURITY &&
            DeviceType.isOpticalFingerprintSupported();

    /* FEATURE_DESCRIPTION : 40. Keyguard feature for side fingerprint */
    public static final boolean SECURITY_SUPPORT_FINGERPRINT_SIDE = SECURITY &&
            DeviceType.isSideFingerprintSupported();

    /* FEATURE_DESCRIPTION : 41. Keyguard feature for rear fingerprint */
    public static final boolean SECURITY_SUPPORT_FINGERPRINT_REAR = SECURITY &&
            DeviceType.isRearFingerprintSupported();

    /* FEATURE_DESCRIPTION : 42. Keyguard feature for wake on fingerprint */
    public static final boolean SECURITY_SUPPORT_WOF = SECURITY_SUPPORT_FINGERPRINT
            && DeviceType.isWOFSupported();

    /* FEATURE_DESCRIPTION : 43. Keyguard feature for face recognition */
    public static final boolean SECURITY_SUPPORT_FACE = SECURITY &&
            SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_BIOMETRICS_FACE_RECOGNITION;

    /* FEATURE_DESCRIPTION : 44. Keyguard feature for iris recognition */
    public static final boolean SECURITY_SUPPORT_IRIS = SECURITY &&
            SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_IRIS;

    /* FEATURE_DESCRIPTION : 45. Keyguard feature for intelligent biometrics */
    public static final boolean SECURITY_SUPPORT_IB = SECURITY &&
            SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_INTELLIGENT_BIOMETRICS_SERVICE;

    /* FEATURE_DESCRIPTION : 46. Keyguard feature for face/iris/intelligent biometrics */
    public static final boolean SECURITY_SUPPORT_BIOMETRICS = SECURITY &&
            SECURITY_SUPPORT_FACE || SECURITY_SUPPORT_IRIS || SECURITY_SUPPORT_IB;

    /* FEATURE_DESCRIPTION : 47. Keyguard feature for in-display fingerprint/face/iris/intelligent biometrics */
    public static final boolean SECURITY_SUPPORT_UI_BIOMETRICS = SECURITY &&
            SECURITY_SUPPORT_FINGERPRINT_IN_DISPLAY || SECURITY_SUPPORT_BIOMETRICS;

    /* FEATURE_DESCRIPTION : 48. Keyguard feature for biometrics authentication on tablet */
    public static final boolean SECURITY_SUPPORT_BIOMETRICS_TABLET = SECURITY &&
            SYSUI_IS_TABLET_DEVICE && SECURITY_SUPPORT_BIOMETRICS;

    /* FEATURE_DESCRIPTION : 49. Keyguard feature for support to stay on lock screen when biometrics authentication */
    public static final boolean SECURITY_SUPPORT_STAY_ON_LOCKSCREEN = SECURITY &&
            SECURITY_SUPPORT_BIOMETRICS;

    /* FEATURE_DESCRIPTION : 50. Keyguard feature for multi biometrics (fingerprint, Iris, face, etc...) */
    public static final boolean SECURITY_SUPPORT_MULTI_BIOMETRICS = SECURITY &&
            SECURITY_SUPPORT_FINGERPRINT && SECURITY_SUPPORT_BIOMETRICS;

    /* FEATURE_DESCRIPTION : 51. Hide Pin pad with iris */
    public static final boolean SECURITY_SUPPORT_PINPAD_HIDE = SECURITY && SECURITY_SUPPORT_IRIS;

    /* FEATURE_DESCRIPTION : 52. Keyguard feature for supporting VI of punch hole models */
    public static final String SECURITY_VALUE_LOCKSCREEN_CONFIG_PUNCH_HOLE_VI = SecProductFeature_LOCKSCREEN.SEC_PRODUCT_FEATURE_LOCKSCREEN_CONFIG_PUNCHHOLE_VI;
    public static final boolean SECURITY_SUPPORT_PUNCH_HOLE_VI = SECURITY && !TextUtils.isEmpty(SECURITY_VALUE_LOCKSCREEN_CONFIG_PUNCH_HOLE_VI);
    public static final boolean SECURITY_SUPPORT_PUNCH_HOLE_AFFORDANCE_VI = SECURITY && SECURITY_VALUE_LOCKSCREEN_CONFIG_PUNCH_HOLE_VI.contains("affordance");
    public static final boolean SECURITY_SUPPORT_PUNCH_HOLE_FACE_VI = SECURITY && SECURITY_VALUE_LOCKSCREEN_CONFIG_PUNCH_HOLE_VI.contains("face");

    /* FEATURE_DESCRIPTION : 53. Keyguard feature for background biometric authentication (fingerprint, Iris, face, etc.) */
    public static final boolean SECURITY_SUPPORT_BACKGROUND_AUTHENTICATION = SECURITY &&
            SemFloatingFeature.getInstance().getBoolean(
                    "SEC_FLOATING_FEATURE_LOCKSCREEN_SUPPORT_BACKGROUND_AUTHENTICATION");
    
    /* FEATURE_DESCRIPTION : 54. Common Feature for 2 step verification */
    public static final boolean SECURITY_SUPPORT_2_STEP_VERIFICATION = SECURITY;

    /* FEATURE_DESCRIPTION : 55. CscFeature_LockScreen_SupportAutoLockForGear for China */
    public static final boolean SECURITY_SUPPORT_AUTO_LOCK_FOR_GEAR = SECURITY && SemCscFeature.getInstance().getBoolean(
            CscFeatureTagLockScreen.TAG_CSCFEATURE_LOCKSCREEN_SUPPORTAUTOLOCKFORGEAR);

    /* FEATURE_DESCRIPTION : 56. Bouncer dim amount feature. */
    public static final boolean SECURITY_SUPPORT_PROPORTIONAL_DIM = SECURITY;

    /* FEATURE_DESCRIPTION : 57. Keyguard feature for supporting dual display model, such as Winner */
    public static final boolean SECURITY_SUPPORT_DUAL_DISPLAY = SECURITY && SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_DUAL_DISPLAY;

    /* FEATURE_DESCRIPTION : 58. for deadzone of bouncer */
    public static final boolean SECURITY_SUPPORT_DEAD_ZONE = SECURITY && !TextUtils.isEmpty(SemFloatingFeature.getInstance().getString("SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_TSP_STATE_MANAGER"));

    public static final boolean SECURITY_SUPPORT_SENSOR_LIMITATION_WIRELESS_CHARGER =
            SecProductFeature_FINGERPRINT.SEC_PRODUCT_FEATURE_FINGERPRINT_CONFIG_SENSOR.contains("limit_wireless_charging");

    //--------------------------------------------------------//
    // Lock UI Constants                                      //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Q OS Porting TODO Feature */
    public static final boolean LOCKUI_PORTING_Q_TODO = false;
    
    /* FEATURE_DESCRIPTION : Support face widget in keyguard status area */
    public static final boolean LOCKUI_SUPPORT_FACE_WIDGET = LOCKUI && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_COMMON_SUPPORT_SERVICEBOX");

    /* FEATURE_DESCRIPTION : Support SEC shortcut. changes concept of two shortcuts placed in bottom area with fancy stuff :) */
    public static final boolean LOCKUI_SUPPORT_SHORTCUT = LOCKUI;

    /* FEATURE_DESCRIPTION : Support SEC lockicon. */
    public static final boolean LOCKUI_SUPPORT_LOCKICON = LOCKUI;

    /* FEATURE_DESCRIPTION : Support SEC camera. */
    public static final boolean LOCKUI_SUPPORT_LAUNCH_CAMERA = LOCKUI;

    /* FEATURE_DESCRIPTION : Support floating shortcut. */
    public static final boolean LOCKUI_SUPPORT_FLOATING_SHORTCUT = LOCKUI && SemFloatingFeature.getInstance()
            .getBoolean("SEC_FLOATING_FEATURE_LOCKSCREEN_SUPPORT_FLOATING_SHORTCUT");

    /** FEATURE_DESCRIPTION : Supports Shortcut Preview. This exists to block creating preview when device is MASS, i.e. have low-memory.**/
    public static final boolean LOCKUI_SUPPORT_SHORTCUT_PREVIEW = LOCKUI &&
            SecProductFeature_LOCKSCREEN.SEC_PRODUCT_FEATURE_LOCKSCREEN_CONFIG_SHORTCUT_PREVIEW_TYPE.equals("Layout");
    
    /* FEATURE_DESCRIPTION : Lockscreen feature for crrier text (PLMN) */
    public static final boolean LOCKUI_SUPPORT_CARRIER_TEXT = LOCKUI;

    /* FEATURE_DESCRIPTION : Lockscreen feature for crrier text (PLMN) */
    public static final boolean LOCKUI_SUPPORT_DISAPPEAR_DEFAULT_PLMN = SECURITY_VALUE_CARRIER_SECURITY_POLICY.contains("UseTMOSIMPINLock");

    /* FEATURE_DESCRIPTION : Lockscreen feature for help text **/
    public static final boolean LOCKUI_SUPPORT_HELP_TEXT = LOCKUI;

    /* FEATURE_DESCRIPTION : Lockscreen feature for help text for china **/
    public static final boolean LOCKUI_SUPPORT_HELP_TEXT_FOR_CHN = LOCKUI && SECURITY_VALUE_CARRIER_SECURITY_POLICY.contains("UseSamsungAccountAuth");

    /* FEATURE_DESCRIPTION : Support SEC shortcut. changes concept of two shortcuts placed in bottom area with fancy stuff :) */
    public static final boolean LOCKUI_SUPPORT_BOTTOM_USIM_TEXT = LOCKUI && SECURITY_VALUE_CARRIER_TEXT_DISPLAY_POLICY.contains("DisplayUsimText");

    /* FEATURE_DESCRIPTION : Lockscreen feature for help text **/
    public static final boolean LOCKUI_SUPPORT_OPEN_THEME = LOCKUI && SYSUI_SUPPORT_OPEN_THEME;

    /* FEATURE_DESCRIPTION : Support Adaptive Color. Extract the gradient color of lockscreen wallpaper. */
    public static final boolean LOCKUI_SUPPORT_ADAPTIVE_COLOR = LOCKUI;

    /* FEATURE_DESCRIPTION : Support folder model like Winner */
    public static final boolean LOCKUI_SUPPORT_FOLDER_MODEL = LOCKUI && SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_DUAL_DISPLAY;

    /** FEATURE_DESCRIPTION : Lockscreen feature for Blur of bouncer window  */
    public static final boolean LOCKUI_SUPPORT_BLUR = LOCKUI && SecProductFeature_GRAPHICS.SEC_PRODUCT_FEATURE_GRAPHICS_ENABLE_3D_SURFACE_TRANSITION_FLAG;

    /** FEATURE_DESCRIPTION : Supports Setting's Reduce animation Function
     * Operations to control animation behavior
     * Lockscreen - Face Widget(Detail page VI)
     */
    public static final boolean LOCKUI_SUPPORT_SETTING_REDUCE_ANIMATION = LOCKUI;

    /* FEATURE_DESCRIPTION : Support capturing of Clock/Shortcuts */
    public static final boolean LOCKUI_CAPTURE_CLOCK_AND_SHORTCUTS = LOCKUI && SemFloatingFeature.getInstance()
            .getBoolean("SEC_FLOATING_FEATURE_LOCKSCREEN_SAVE_CAPTURE_FILE_FOR_WALLPAPER_PREVIEW", true);

    /* FEATURE_DESCRIPTION : CSC feature : CscFeature_Message_CMASOperator */
    private static final String LOCKUI_VALUE_CMAS_OPERATOR = SemCscFeature.getInstance().getString(CscFeatureTagMessage.TAG_CSCFEATURE_MESSAGE_CMASOPERATOR);
    public static final boolean LOCKUI_SUPPORT_CMAS = LOCKUI && LOCKUI_VALUE_CMAS_OPERATOR.startsWith("us");
    public static final boolean LOCKUI_SUPPORT_PRESIDENTIAL_CMAS = LOCKUI && "us-spr".equals(LOCKUI_VALUE_CMAS_OPERATOR);

    /* FEATURE_DESCRIPTION : Support AOD package  */
    public static final boolean LOCKUI_AOD_PACKAGE_AVAILABLE =
            LOCKUI && SemFloatingFeature.getInstance().getString("SEC_FLOATING_FEATURE_FRAMEWORK_CONFIG_AOD_ITEM", "").contains("aodversion");

    /* FEATURE_DESCRIPTION : Support Plugin Lock */
    public static final boolean LOCKUI_PLUGIN_LOCK = LOCKUI;

    //--------------------------------------------------------//
    // AOD Constants                                          //
    //--------------------------------------------------------//
    /** FEATURE_DESCRIPTION : Q OS Porting TODO Feature */
    public static final boolean AOD_PORTING_Q_TODO = false;

    /** FEATURE_DESCRIPTION : Q OS AOD Feature */
    public static final boolean AOD_SUPPORT_DOZE_SERVICE = AOD;
    public static final boolean AOD_SUPPORT_DOZE_UI = AOD;

    /* FEATURE_DESCRIPTION : support AOD notification list */
    public static final boolean AOD_SUPPORT_NOTIFICATION = AOD && NOTI_SUPPORT_SEC_POLICY;
    /* FEATURE_DESCRIPTION : support AOD charging info */
    public static final boolean AOD_SUPPORT_DOZE_BOTTOM_AREA = AOD && LOCKUI_SUPPORT_SHORTCUT;
    /* FEATURE_DESCRIPTION : support AOD music info */
    public static final boolean AOD_SUPPORT_MUSIC_INFO = AOD;
    /* FEATURE_DESCRIPTION : support FaceWidget in AOD */
    public static final boolean AOD_SUPPORT_FACE_WIDGET = AOD && LOCKUI_SUPPORT_FACE_WIDGET;

    //--------------------------------------------------------//
    // Cover Constants                                        //
    //--------------------------------------------------------//
    /** FEATURE_DESCRIPTION : Q OS Porting TODO Feature */
    public static final boolean COVER_PORTING_Q_TODO = false;

    /* FEATURE_DESCRIPTION : support COVER notification list */
    public static final boolean COVER_SUPPORT_NOTIFICATION = AOD_SUPPORT_NOTIFICATION;


    //--------------------------------------------------------//
    // Wallpaper Constants                                    //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Wallpaper feature for night filter wallpaper (oneUI 2.0) */
    public static final boolean WPAPER_SUPPORT_NIGHT_MODE_FILTER = WPAPER && false;

    /* FEATURE_DESCRIPTION : Wallpaper feature for smart crop when device rotated */
    public static final boolean WPAPER_SUPPORT_SMART_CROP_WHEN_ROTATE = WPAPER && true;

    /* FEATURE_DESCRIPTION : Wallpaper feature for Motione effect in Home screen */
    public static final boolean WPAPER_SUPPORT_MOTION_EFFECT_IN_LAUNCHER = WPAPER && false;

    //--------------------------------------------------------//
    // Keyguard Wallpaper Constants                           //
    //--------------------------------------------------------//

    /* FEATURE_DESCRIPTION : Lockscreen feature for Default wallpaper style value */
    public static final String WPAPER_VALUE_DEFAULT_WALLPAPER_STYLE = WPAPER ? SemFloatingFeature.getInstance()
            .getString("SEC_FLOATING_FEATURE_LOCKSCREEN_CONFIG_DEFAULT_WALLPAPER_STYLE") : "NULL";

    /* FEATURE_DESCRIPTION : Lockscreen feature for the Motion wallpaper */
    public static final boolean WPAPER_SUPPORT_MOTION_WALLPAPER =  WPAPER && SemFloatingFeature.getInstance()
            .getString("SEC_FLOATING_FEATURE_LOCKSCREEN_CONFIG_WALLPAPER_STYLE").contains("MOTION");

    /* FEATURE_DESCRIPTION : Lockscreen feature for the Infinity wallpaper */
    public static final boolean WPAPER_SUPPORT_INFINITY_WALLPAPER = WPAPER && SemFloatingFeature.getInstance()
            .getString("SEC_FLOATING_FEATURE_LOCKSCREEN_CONFIG_WALLPAPER_STYLE").contains("INFINITY");

    /* FEATURE_DESCRIPTION : Lockscreen feature for the Video wallpaper */
    public static final boolean WPAPER_SUPPORT_VIDEO_WALLPAPER = WPAPER && SemFloatingFeature.getInstance()
            .getString("SEC_FLOATING_FEATURE_LOCKSCREEN_CONFIG_WALLPAPER_STYLE").contains("VIDEO");

    /* FEATURE_DESCRIPTION : Do not fix wallpaper as deivce orientation. */
    public static final boolean WPAPER_SUPPORT_ROTATABLE_WALLPAPER = WPAPER && SemFloatingFeature.getInstance()
            .getString("SEC_FLOATING_FEATURE_LOCKSCREEN_CONFIG_WALLPAPER_STYLE").contains("ROTATABLE") || DeviceType.isTablet();

    /* FEATURE_DESCRIPTION : CSC feature : CscFeature_LockScreen_EnableOperatorWallpaper */
    public static final boolean WPAPER_SUPPORT_OPERATOR_WALLPAPER = WPAPER && SemCscFeature.getInstance().getBoolean(
            CscFeatureTagLockScreen.TAG_CSCFEATURE_LOCKSCREEN_ENABLEOPERATORWALLPAPER);

    /* FEATURE_DESCRIPTION : Wallpaper feature for that text's color is automatically changed after checking white wallpaper */
    public static final boolean WPAPER_SUPPORT_WHITE_WALLPAPER_SOLUTION = WPAPER && true;

    /* FEATURE_DESCRIPTION : Wallpaper feature for Lockscreen wallpaper */
    public static final boolean WPAPER_SUPPORT_SEC_LOCK_WALLPAPER = WPAPER && true;

    /* FEATURE_DESCRIPTION : Wallpaper feature for Blending Effect */
    public static final boolean WPAPER_SUPPORT_BLENDING_EFFECT = WPAPER && false;

    /* FEATURE_DESCRIPTION : Wallpaper feature for sub display wallpaper (winner) */
    public static final boolean WPAPER_SUPPORT_SUB_DISPLAY_MODE = WPAPER && SemFloatingFeature.getInstance()
            .getString("SEC_FLOATING_FEATURE_LOCKSCREEN_CONFIG_WALLPAPER_STYLE").contains("LID");

    /* FEATURE_DESCRIPTION : Wallpaper feature for elastic framework implementations  */
    public static final boolean WPAPER_SUPPORT_THEME_OPEN = WPAPER && SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_ELASTIC_PLUGIN;

    /* FEATURE_DESCRIPTION : Wallpaper features for DCM **/
    private static final String DCM_LAUNCHER_PACKAGE_NAME = "com.nttdocomo.android.dhome";
    private static final int DCM_LAUNCHER_VERSION_CODE_SINCE_DREAM = 4004;

    private static int getDcmLauncherVersionCode(Context context) {
        int versionCode = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(DCM_LAUNCHER_PACKAGE_NAME, 0/* flag */);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getDcmLauncherVersionCode NameNotFoundException!");
        }
        return versionCode;
    }

    public static boolean isBeforeDreamDcmLauncher(Context context) {
        return getDcmLauncherVersionCode(context) < DCM_LAUNCHER_VERSION_CODE_SINCE_DREAM;
    }

    public static boolean isDcmLauncher(Context context) {
        return /*KEYGUARD_SUPPORT_DCM_LIVEUX &&*/ isPreferredActivity(context, DCM_LAUNCHER_PACKAGE_NAME);
    }

    public static boolean isPreferredActivity(Context context, String packageName) {
        List<IntentFilter> intentList = new ArrayList<IntentFilter>();
        List<ComponentName> componentList = new ArrayList<ComponentName>();
        context.getPackageManager().getPreferredActivities(intentList, componentList, packageName);

        return (componentList.size() > 0);
    }
    /* FEATURE_DESCRIPTION : Wallpaper feature for DCM **/

    //--------------------------------------------------------//
    // NavigationBar Constants                                //
    //--------------------------------------------------------//
    private static final String NAVBAR_FLOATING_FEATURES = SemFloatingFeature.getInstance().getString("SEC_FLOATING_FEATURE_FRAMEWORK_CONFIG_NAVIGATION_BAR_THEME", "");
    /* FEATURE_DESCRIPTION : Common feature for support navigation bar model */
    public static final boolean NAVBAR_ENABLED = NAVBAR && !NAVBAR_FLOATING_FEATURES.isEmpty();
    /* FEATURE_DESCRIPTION : NavigationBar feature for SUW */
    public static final boolean NAVBAR_SUPPORT_SETUP_WIZARD = NAVBAR_ENABLED;
    /* FEATURE_DESCRIPTION : NavigationBar feature for Light NavigationBar */
    public static final boolean NAVBAR_SUPPORT_LIGHT_NAVIGATIONBAR = NAVBAR_ENABLED && NAVBAR_FLOATING_FEATURES.contains("SupportLightNavigationBar");
    /* FEATURE_DESCRIPTION : NavigationBar feature for ForceTouch */
    public static final boolean NAVBAR_SUPPORT_FORCE_TOUCH = NAVBAR_ENABLED && NAVBAR_FLOATING_FEATURES.contains("SupportForceTouch");
    /* FEATURE_DESCRIPTION : NavigationBar feature for Remoteview for app */
    public static final boolean NAVBAR_SUPPORT_REMOTEVIEW = NAVBAR_ENABLED && NAVBAR_FLOATING_FEATURES.contains("SupportNaviBarRemoteView");
    /* FEATURE_DESCRIPTION : NavigationBar feature for Icon movement (Marquee) */
    public static final boolean NAVBAR_SUPPORT_ICON_MOVEMENT = NAVBAR_ENABLED;
    /* FEATURE_DESCRIPTION : Common feature for support navigationbar layout inflate fixed ratio - LTR layout fix */
    public static final boolean NAVBAR_SUPPORT_STABLE_LAYOUT = NAVBAR_ENABLED;
    /* FEATURE_DESCRIPTION : Common feature for support navigationbar button order change */
    public static final boolean NAVBAR_SUPPORT_SWITCH_POSITION = NAVBAR_ENABLED;
    /* FEATURE_DESCRIPTION : Common feature for Fullscreen Gesture */
    public static final boolean NAVBAR_SUPPORT_GESTURE = NAVBAR_ENABLED;
    /* FEATURE_DESCRIPTION : Common feature for Gesture Extra Area */
    public static final boolean NAVBAR_SUPPORT_GESTURE_EXTRA_AREA = NAVBAR_ENABLED;
    /* FEATURE_DESCRIPTION : Common feature for Gesture POC */
    public static final boolean NAVBAR_SUPPORT_GESTURE_POC = NAVBAR_FLOATING_FEATURES.contains("SupportGesturePOC");
    /* FEATURE_DESCRIPTION : Common feature for Navigation bar movable - Winner*/
    public static final boolean NAVBAR_SUPPORT_MOVABLE_POSITION = NAVBAR_FLOATING_FEATURES.contains("SupportMovablePosition");
    /* FEATURE_DESCRIPTION : Common feature for Navigation bar double swipe for game tools enable */
    public static final boolean NAVBAR_SUPPORT_GESTURE_PROTECTION = NAVBAR_FLOATING_FEATURES.contains("SupportGestureProtection");
    /* FEATURE_DESCRIPTION : Common feature for Navigation bar DeX */
    public static final boolean NAVBAR_SUPPORT_DEX = SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_KNOX_DESKTOP;
    /* FEATURE_DESCRIPTION : Common feature for performance tuning */
    public static final boolean NAVBAR_SUPPORT_PERFORMANCE_TUNING = NAVBAR_ENABLED;

    //--------------------------------------------------------//
    // AssistManager Constants                                //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Feature for Assistance app setting dialog concept */
    public static final boolean ASSIST_SUPPORT_ASSISTANCE_APP_SETTING_POPUP = ASSIST && SemCscFeature.getInstance().getBoolean(CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_SUPPORTASSISTANCEAPPCHOOSER, false);
    /* FEATURE_DESCRIPTION : Support assist disclosure VI */
    public static final boolean ASSIST_SUPPORT_ASSIST_ROUND_DISCLOSURE = ASSIST && (STATBAR_CONFIG_DEVICE_CORNER_ROUND > 0);
    /* FEATURE_DESCRIPTION : Support make enable assist disclosure VI */
    public static final boolean ASSIST_SUPPORT_ASSIST_DISCLOSURE_ENABLED = ASSIST;


    //--------------------------------------------------------//
    // Global Actions Constants                                 //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Feature for Sec Global Actions */
    public static final boolean GLOBALACTIONS_SUPPORT_SEC_STYLE = GLOBALACTIONS;
    public static final boolean GLOBALACTIONS_SUPPORT_LOCK_DOWN = GLOBALACTIONS;
    public static final boolean GLOBALACTIONS_SUPPORT_SF_EFFECTS = GLOBALACTIONS && SecProductFeature_GRAPHICS.SEC_PRODUCT_FEATURE_GRAPHICS_ENABLE_3D_SURFACE_TRANSITION_FLAG;


    //--------------------------------------------------------//
    // Volume Panel Constants                                 //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Support Samsung Volume Dialog */
    public static final boolean VOLUME_SUPPORT_SEC_VOLUME_DIALOG = VOLUME;

    //--------------------------------------------------------//
    // PopupUI Constants                                 //
    //--------------------------------------------------------//
    /* FEATURE_DESCRIPTION : Common feature for Samsung system dialog concept */
    public static final boolean POPUP_SUPPORT_POPUP_UI = BASIC && true;
    /* FEATURE_DESCRIPTION : Support mobile device warning popup */
    public static final boolean POPUP_SUPPORT_MOBILE_DEVICE_WARNING_POPUP = POPUP_SUPPORT_POPUP_UI && QPANEL_IS_KOREA_POPUP;
    /* FEATURE_DESCRIPTION : Support flight mode enabled popup */
    public static final boolean POPUP_SUPPORT_FLIGHTMODE_ENABLED_POPUP = POPUP_SUPPORT_POPUP_UI && false;
    /* FEATURE_DESCRIPTION : Support SEC dialog of network over limit */
    public static final boolean POPUP_SUPPORT_SEC_NETWORK_OVER_LIMIT_POPUP = POPUP_SUPPORT_POPUP_UI && true;
    /* FEATURE_DESCRIPTION : Common feature for Samsung SIM card tray right dialog concept */
    public static final boolean POPUP_SUPPORT_SIM_CARD_TRAY_ON_RIGHT_WATER_PROTECTION_POPUP = POPUP_SUPPORT_POPUP_UI && SYSUI_PRODUCT_NAME.startsWith("poseidon");
    /* FEATURE_DESCRIPTION : Common feature for SD card tray support */
    public static final boolean POPUP_SUPPORT_SD_CARD_STORAGE = POPUP_SUPPORT_POPUP_UI && "1".equals(SystemProperties.get("ro.storage.support.sdcard","1"));

    //--------------------------------------------------------//
    // Common APIs                                            //
    //--------------------------------------------------------//

    // { Rune.NOTI_SUPPORT_SIMPLE_STATUSBAR
    public static int getSimpleStatsuBarDefaultValue() {
        return "On".equals(SemCscFeature.getInstance().getString(
                CscFeatureTagSystemUI.TAG_CSCFEATURE_SYSTEMUI_CONFIGDEFSTATUSSIMPLESTATUSBAR,
                "On")) ? 1 : 0;
    }
    // } Rune.NOTI_SUPPORT_SIMPLE_STATUSBAR

    // { STATBAR_SUPPORT_SIGNAL_ICON_AT_EMERGENCY_CALL_ONLY_STATE
    public static boolean isSupportSignalIconAtEmergencyOnly(int phoneId) {
        return SemCscFeature.getInstance().getBoolean(phoneId, CscFeatureTagRIL.TAG_CSCFEATURE_RIL_DISPLAYANTENNALIMITED);
    }
    // } STATBAR_SUPPORT_SIGNAL_ICON_AT_EMERGENCY_CALL_ONLY_STATE

    public static boolean supportDualIms() {
        return STATBAR_SUPPORT_MULTI_SIM && ("DSDS_DI".equals(SystemProperties.get("persist.ril.config.dualims", "")) || SYSUI_SUPPORT_DUAL_IMS);
    }

    /* FEATURE_DESCRIPTION : Support simplified signal cluster concept.*/
    public static final boolean STATBAR_SUPPORT_LIMITED_ICON_FOR_CTC_SLAVE_VOLTE_CONCEPT = ("CHC".equals(STATBAR_ICON_BRANDING) || "CTC".equals(STATBAR_ICON_BRANDING) || "CHM".equals(STATBAR_ICON_BRANDING)) && supportDualIms();

    //--------------------------------------------------------//
    // APIs for test                                          //
    //--------------------------------------------------------//
    public static boolean isTesting() {
        return SYSUI_SUPPORT_TESTS && "true".equals(System.getProperty("dexmaker.share_classloader"));
    }

    // VZW PCO START
    /* FEATURE_DESCRIPTION : Verizon PCO support for smartphones and tablets */
    public static final String VZW_PCO_FEATURE = SemCscFeature.getInstance().getString(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_CONFIGPCO, "");
    // VZW PCO ENDS
}
