/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.wifi;

import static android.content.Context.WIFI_SERVICE;

import android.app.settings.SettingsEnums;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.Utils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.search.SearchIndexable;

import com.samsung.android.net.wifi.OpBrandingLoader;
import com.samsung.android.net.wifi.OpBrandingLoader.Vendor;
import com.samsung.android.settings.widget.RelativeLinkView;
import com.samsung.android.settings.SettingsPreferenceFragmentLinkData;
import com.samsung.android.settings.search.SecBaseSearchIndexProvider;
import com.samsung.android.settings.search.SecIndexable;
import com.samsung.android.settings.wifi.WifiPowerSavingModePrefController;
import com.samsung.android.settings.wifi.PasspointPreferenceController;
import com.samsung.android.settings.wifi.PasspointPreferenceSwitchController;
import com.samsung.android.settings.wifi.ScanForFreeWlansPrefController;
import com.samsung.android.settings.wifi.TencentWifiDetectionPrefController;
import com.samsung.android.settings.wifi.WifiManageNetworkPrefController;
import com.samsung.android.settings.wifi.WifiNetworkDiagnosticsPrefController;
//import com.samsung.android.settings.wifi.AttAutoConnectionPrefController;
import com.samsung.android.settings.wifi.NotifyOpenNetworkPrefController;
import com.samsung.android.settings.wifi.CharterWifiController;
import com.samsung.android.settings.wifi.ShowWifiPopupPreferenceController;
import com.samsung.android.settings.wifi.WlanPermissionAvailablePrefController;
//Deactivate from Hubble
//import com.samsung.android.settings.wifi.WifiConnectionTypePrefController;
import com.samsung.android.settings.wifi.WifiControlHistoryPrefController;
import com.samsung.android.settings.wifi.InstallCredentialsPrefController;
import com.samsung.android.settings.wifi.SmartNetworkSwitchPrefController;
import com.samsung.android.settings.wifi.AutoWifiPreferenceController;
//Deactivate from Hubble
//import com.samsung.android.settings.wifi.CMCCEnableWarningPrefController;
import com.samsung.android.settings.wifi.CricketManagerPreferenceController;
import com.samsung.android.settings.wifi.MobileWIPSPrefController;
import com.samsung.android.settings.wifi.AutoWifiJSONUtils;
//
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sec.android.app.CscFeatureTagCommon;
import com.sec.android.app.CscFeatureTagWifi;
import com.sec.android.app.SecProductFeature_WLAN;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;

import com.samsung.android.settings.logging.LoggingHelper;
import com.samsung.android.settings.logging.SAConstant;

//SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
import com.samsung.android.knox.custom.CustomDeviceManager;
import com.samsung.android.knox.custom.SettingsManager;
//SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }

//<!-- Secure Wi-Fi Start-->
import android.os.UserManager;
//<!-- Secure Wi-Fi End-->

@SearchIndexable
public class ConfigureWifiSettings extends DashboardFragment implements SecIndexable {

    private static final String TAG = "ConfigureWifiSettings";

    public static final int WIFI_WAKEUP_REQUEST_CODE = 600;
    private static final String PREF_KEY_ADAPTIVE_WIFI = "advanced_adaptive_wifi";
    private static final String PREF_KEY_NETWORK_SETTINGS = "advanced_network_settings";

    private static List<AbstractPreferenceController> controllers;
    private PreferenceCategory mAdaptiveWifiCategory;
    private static final Vendor mOpBranding = OpBrandingLoader.getInstance().getOpBranding();

    private Context mContext;
    private final String mSAScreenId = SAConstant.wifi_advanced_setting;

    //Preference Keys
    private final static String KEY_ADVANCED_SCREEN = "wifi_configure_settings_screen";
    private final static String KEY_SWITCH_TO_MOBILE_DATA = "wifi_poor_network_detection";
    private final static String KEY_AUTO_WIFI = "auto_wifi";
    private final static String KEY_MOBILE_WIPS = "MobileWIPS";
    private final static String KEY_ADPS = "wifi_adps";
    private final static String KEY_SECURE_WIFI = "secure_wifi";
    private final static String KEY_CRICKET_MANAGER = "cricket_manager";
    private final static String KEY_OPTIMIZE_CHARTER = "optimize_charter_wifi_networks";
    //private final static String KEY_AUTO_CONNECT_ATT = "att_auto_connect";
    private final static String KEY_NOTIFY_OPEN_NETWORK = "notify_open_networks";
    //private final static String KEY_NOTIFY_RECOMMEND = "notify_wifi_recommend";
    private final static String KEY_WLAN_PERMISSION_AVAILABLE = "wlan_permission_available";
    private final static String KEY_SHOW_WIFI_POPUP = "show_wifi_popup";
    private final static String KEY_WIFI_CONNECT_TYPE = "wifi_connection_type";
    private final static String KEY_NOTIFY_CMCC = "wlan_notify_cmcc";
    private final static String KEY_SCAN_FOR_FREE_WLAN = "wifi_scan_for_free_wlans";
    private final static String KEY_TENCENT_SECURITY = "tencent_wifi_security_detection";
    private final static String KEY_NETWORK_DIAGNOSTICS = "wifi_network_diagnostics";
    private final static String KEY_WIFI_MANAGE_NETWORK = "wifi_manage_network";
    private final static String KEY_WIFI_CONTROL_HISTORY = "wifi_control_history";
    private final static String KEY_HOTSPOT_20_RENEWAL = "wifi_hs20_profile";
    private final static String KEY_HOTSPOT_20_NO_SWITCH = "wifi_hs20_list";
    private final static String KEY_INSTALL_CREDENTIAL = "install_credentials";
    private final static String KEY_IP_MAC_ADDRESS = "mac_address";

    //Features
    private static final String CSC_COMMON_CHINA_NAL_SECURITY_TYPE =
            SemCscFeature.getInstance().getString(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_CONFIGLOCALSECURITYPOLICY);
    private static final boolean mIsSupprotAutoWifi = SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI;
    private static final boolean mIsSupportAdpsMenu = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS");
    private static final String mEnableAutoConnectHotspot = SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT);
    private static final boolean mIsComcastWifiSupported = SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTCOMCASTWIFI);
    private static final boolean mIsSupprotWifiPromptDataOveruse = SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTHUXWIFIPROMPTDATAOVERUSE);
    private static final String mVendorNotificationStyle = SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGWIFINOTIFICATIONSTYLE);
    private static final boolean mIsSupportMWIPS = SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS));
 /*
     *mIsCharterWifiNotSupported tag is used to check for if this device will have feature of Charter wifi control UI in settings or not.
     * i.e, SE devices will not have Charter UI in WIFI settings when TAG value is true
     * */
    private static boolean mIsCharterWifiNotSupported = SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLECHARTERMENU);
    private static String hidePasspointSwitch;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.CONFIGURE_WIFI;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        hidePasspointSwitch = OpBrandingLoader.getInstance().getMenuStatusForPasspoint();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAllPreferences(mContext);
        setLinkedDataView();
    }

    @Override
    public void onStart() {
        super.onStart();
        LoggingHelper.insertEventLogging(mSAScreenId);
        LoggingHelper.insertEventLogging(mSAScreenId, SAConstant.wifi_advanced_with_badge, AutoWifiJSONUtils.whatsNew(mContext) ? 1 : 0);
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_configure_settings;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                LoggingHelper.insertEventLogging(mSAScreenId, SAConstant.wifi_advanced_up_button);
                try {
                    finish();
                    return true;
                } catch(IllegalStateException e) {
                    Log.e(TAG,"IllegalStateException: Can not perform this action after onSaveInstanceState");
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }

        final int resId = getPreferenceScreenResId();
        if (resId <= 0) {
            return;
        }
        addPreferencesFromResource(resId);
        for (AbstractPreferenceController controller : controllers) {
            controller.displayPreference(getPreferenceScreen());
        }
        mAdaptiveWifiCategory = (PreferenceCategory) findPreference(PREF_KEY_ADAPTIVE_WIFI);
        if (mAdaptiveWifiCategory != null && Utils.isWifiOnly(mContext) && !mIsSupportAdpsMenu) {
            mAdaptiveWifiCategory.removeAll();
            mAdaptiveWifiCategory.setVisible(false);
        }
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {

        mContext = getActivity().getBaseContext();
        Intent intent = getActivity().getIntent();

        return ConfigureWifiPreferenceControllers(context, getActivity(), this,
                getSettingsLifecycle(), getFragmentManager());
    }

    private static List<AbstractPreferenceController> ConfigureWifiPreferenceControllers(Context context,
            Activity activity, SettingsPreferenceFragment fragment, Lifecycle lifecycle, FragmentManager fragmentManger) {
        final WifiManager wifiManager = context.getSystemService(WifiManager.class);
        if (controllers != null) {
            controllers.clear();
        } else {
            controllers = new ArrayList<>();
        }
        controllers.add(new WifiInfoPreferenceController(context, lifecycle, wifiManager));
        controllers.add(new WifiPowerSavingModePrefController(context, lifecycle, wifiManager));
        controllers.add(new PasspointPreferenceController(context, lifecycle, wifiManager));
        controllers.add(new PasspointPreferenceSwitchController(context, lifecycle, wifiManager));
        controllers.add(new ScanForFreeWlansPrefController(context));
        controllers.add(new TencentWifiDetectionPrefController(context));
        controllers.add(new WifiManageNetworkPrefController(activity, context, lifecycle, wifiManager));
        controllers.add(new WifiNetworkDiagnosticsPrefController(context, lifecycle, wifiManager));
        //controllers.add(new AttAutoConnectionPrefController(context, lifecycle, wifiManager));
        controllers.add(new NotifyOpenNetworkPrefController(context, lifecycle, wifiManager));
        controllers.add(new CharterWifiController(context, lifecycle));
        controllers.add(new ShowWifiPopupPreferenceController(context));
        controllers.add(new WlanPermissionAvailablePrefController(context));
        //controllers.add(new WifiConnectionTypePrefController(context, wifiManager));
        controllers.add(new WifiControlHistoryPrefController(activity, context));
        controllers.add(new InstallCredentialsPrefController(context));
        controllers.add(new SmartNetworkSwitchPrefController(activity, context, lifecycle));
        controllers.add(new AutoWifiPreferenceController(context, lifecycle, wifiManager));
        //controllers.add(new CMCCEnableWarningPrefController(context));
        controllers.add(new CricketManagerPreferenceController(activity, context));
        controllers.add(new MobileWIPSPrefController(activity, fragment, context, lifecycle)); // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION

        return controllers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (requestCode == WIFI_WAKEUP_REQUEST_CODE && mWifiWakeupPreferenceController != null) {
            mWifiWakeupPreferenceController.onActivityResult(requestCode, resultCode);
            return;
        }
        if (requestCode == UseOpenWifiPreferenceController.REQUEST_CODE_OPEN_WIFI_AUTOMATICALLY
                && mUseOpenWifiPreferenceController != null) {
            mUseOpenWifiPreferenceController.onActivityResult(requestCode, resultCode);
            return;
        }*/
        super.onActivityResult(requestCode, resultCode, data);
    }


    //<!-- Secure Wi-Fi Start-->
    private void setLinkedDataView() {
        RelativeLinkView relativeLinkView = new RelativeLinkView(getActivity());
        if (Utils.isSupportSecureWifi(mContext)) {

            SettingsPreferenceFragmentLinkData secureWifiLinkedData = new SettingsPreferenceFragmentLinkData();
            Intent secureWifiIntent = new Intent("com.samsung.android.fast.ACTION_SECURE_WIFI");
            secureWifiIntent.setPackage("com.samsung.android.fast");
            secureWifiIntent.putExtra("flowId", SAConstant.wifi_advanced_relative_link_secure_wifi);
            secureWifiIntent.putExtra("callerMetric", getMetricsCategory());
            secureWifiLinkedData.intent = secureWifiIntent;
            secureWifiLinkedData.titleRes = R.string.secure_wifi;
            relativeLinkView.pushLinkData(secureWifiLinkedData);

            if (relativeLinkView.isValid()) {
                    relativeLinkView.create(this);
            }
        }
    }

    private boolean isSingleUser() {
        UserManager userManager = UserManager.get(mContext);
        int userCount = userManager.getUserCount();
        return userCount == 1
                    || (UserManager.isSplitSystemUser() && userCount == 2);

    }
    //<!-- Secure Wi-Fi End-->

    private void refreshAllPreferences(Context context) {
        if (getPreferenceScreen() != null) {
            getPreferenceScreen().removeAll();
        }

        final int resId = getPreferenceScreenResId();
        if (resId <= 0) {
            return;
        }
        addPreferencesFromResource(resId);

        for (AbstractPreferenceController controller : controllers) {
            controller.displayPreference(getPreferenceScreen());
        }

        mAdaptiveWifiCategory = (PreferenceCategory) findPreference(PREF_KEY_ADAPTIVE_WIFI);
        if (mAdaptiveWifiCategory != null && Utils.isWifiOnly(context) && !mIsSupportAdpsMenu) {
            mAdaptiveWifiCategory.removeAll();
            mAdaptiveWifiCategory.setVisible(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        for (AbstractPreferenceController controller : controllers) {
            controller.displayPreference(getPreferenceScreen());
        }
    }

    public static final SecSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new SecBaseSearchIndexProvider() {
                /*@Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();

                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.wifi_configure_settings;
                    result.add(sir);
                    return result;
                }*/

                @Override
                public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                    final List<SearchIndexableRaw> result = new ArrayList<>();
                    final Resources res = context.getResources();

                    // Add fragment title if we are showing this fragment
                    SearchIndexableRaw data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_menu_advanced_button);
                    data.screenTitle = res.getString(R.string.wifi_settings);
                    data.key = KEY_ADVANCED_SCREEN;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_switch_to_mobile_data);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_use_mobile_data_detail);
                    data.summaryOff = res.getString(R.string.wifi_use_mobile_data_detail);
                    data.key = KEY_SWITCH_TO_MOBILE_DATA;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_autowifi_title);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_autowifi_summary);
                    data.summaryOff = res.getString(R.string.wifi_autowifi_summary);
                    data.key = KEY_AUTO_WIFI;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_mobile_wips);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_mobile_wips_detail);
                    data.summaryOff = res.getString(R.string.wifi_mobile_wips_detail);
                    data.key = KEY_MOBILE_WIPS;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_power_saving_mode_title);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_power_saving_mode_summary);
                    data.summaryOff = res.getString(R.string.wifi_power_saving_mode_summary);
                    data.key = KEY_ADPS;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_cricket_manager_title);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_cricket_manager_summary);
                    data.summaryOff = res.getString(R.string.wifi_cricket_manager_summary);
                    data.key = KEY_CRICKET_MANAGER;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.optimize_charter_wifi_networks);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.optimize_charter_wifi_networks_summary);
                    data.summaryOff = res.getString(R.string.optimize_charter_wifi_networks_summary);
                    data.key = KEY_OPTIMIZE_CHARTER;
                    result.add(data);

                    /*data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_att_auto_connect);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_att_auto_connect_summary);
                    data.summaryOff = res.getString(R.string.wifi_att_auto_connect_summary);
                    data.key = KEY_AUTO_CONNECT_ATT;
                    result.add(data);*/

                    data = new SearchIndexableRaw(context);
                    if (Vendor.VZW == mOpBranding) {
                        data.title = res.getString(R.string.wifi_notify_open_networks_vzw);
                        data.screenTitle = res.getString(R.string.wifi_notify_open_networks_summary_vzw);
                    }
                    else{
                        data.title = res.getString(R.string.wifi_notify_open_networks);
                        data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    }
                    data.summaryOn = res.getString(R.string.wifi_notify_open_networks_summary);
                    data.summaryOff = res.getString(R.string.wifi_notify_open_networks_summary);
                    data.key = KEY_NOTIFY_OPEN_NETWORK;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wlan_permission_available);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wlan_permission_available_message);
                    data.summaryOff = res.getString(R.string.wlan_permission_available_message);
                    data.key = KEY_WLAN_PERMISSION_AVAILABLE;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.notify_me_available_network);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.notify_me_available_network_summary);
                    data.summaryOff = res.getString(R.string.notify_me_available_network_summary);
                    data.key = KEY_SHOW_WIFI_POPUP;
                    result.add(data);

                    /*data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_connect_type_title);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.key = KEY_WIFI_CONNECT_TYPE;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.cmcc_warning_dialog_control);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.key = KEY_NOTIFY_CMCC;
                    result.add(data);*/

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_scan_for_free_wlans_title);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_scan_for_free_wlans_summary);
                    data.summaryOff = res.getString(R.string.wifi_scan_for_free_wlans_summary);
                    data.key = KEY_SCAN_FOR_FREE_WLAN;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.tencent_wifi_detection_title);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.tencent_wifi_detection_summary);
                    data.summaryOff = res.getString(R.string.tencent_wifi_detection_summary);
                    data.key = KEY_TENCENT_SECURITY;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_network_diagnostics);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_network_diagnostics_summary);
                    data.summaryOff = res.getString(R.string.wifi_network_diagnostics_summary);
                    data.key = KEY_NETWORK_DIAGNOSTICS;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_manage_network_title);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_manage_network_summary);
                    data.summaryOff = res.getString(R.string.wifi_manage_network_summary);
                    data.key = KEY_WIFI_MANAGE_NETWORK;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_control_history_title);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_control_history_summary);
                    data.summaryOff = res.getString(R.string.wifi_control_history_summary);
                    data.key = KEY_WIFI_CONTROL_HISTORY;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_hotspot20_enable);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_hotspot20_summary);
                    data.summaryOff = res.getString(R.string.wifi_hotspot20_summary);
                    data.key = KEY_HOTSPOT_20_RENEWAL;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_hotspot20_enable);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_hotspot20_summary);
                    data.summaryOff = res.getString(R.string.wifi_hotspot20_summary);
                    data.key = KEY_HOTSPOT_20_NO_SWITCH;
                    result.add(data);

                    data = new SearchIndexableRaw(context);
                    data.title = res.getString(R.string.wifi_install_network_certificates);
                    data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                    data.summaryOn = res.getString(R.string.wifi_install_network_credentials_summary);
                    data.summaryOff = res.getString(R.string.wifi_install_network_credentials_summary);
                    data.key = KEY_INSTALL_CREDENTIAL;
                    result.add(data);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final ArrayList<String> result = new ArrayList<String>();

                    if (!mIsSupprotAutoWifi || Utils.isWifiOnly(context)) {
                        result.add(KEY_AUTO_WIFI);
                    }

                    if (!mIsSupportAdpsMenu) {
                        result.add(KEY_ADPS);
                    }

                    if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
                        result.add(KEY_HOTSPOT_20_RENEWAL);
                        result.add(KEY_HOTSPOT_20_NO_SWITCH);
                    } else {
                        if (hidePasspointSwitch != null && hidePasspointSwitch.contains("MENU_OFF")) {
                            result.add(KEY_HOTSPOT_20_RENEWAL);
                        } else {
                            result.add(KEY_HOTSPOT_20_NO_SWITCH);
                        }
                    }

                    if (!"WeChatWiFi".equals(Utils.CONFIG_SOCIAL_SVC_INTEGRATION)) {
                        result.add(KEY_SCAN_FOR_FREE_WLAN);
                    }

                    if (!"TencentSecurityWiFi".equals(Utils.CONFIG_SECURE_SVC_INTEGRATION)) {
                        result.add(KEY_TENCENT_SECURITY);
                    }

                    if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_NETWORK_DIAGNOSTICS) {
                        result.add(KEY_NETWORK_DIAGNOSTICS);
                    }

                    /*if (!"ATT".equals(mEnableAutoConnectHotspot)) {
                        result.add(KEY_AUTO_CONNECT_ATT);
                    }*/

                    if (!Utils.SUPPORT_NOTIFICATION_MENU || Vendor.VZW == mOpBranding) {
                        result.add(KEY_NOTIFY_OPEN_NETWORK);
                    }


                    if (!mIsSupprotWifiPromptDataOveruse) {
                        result.add(KEY_SHOW_WIFI_POPUP);
                    }

                    if (!"ChinaNalSecurity".equals(Utils.CONFIG_LOCAL_SECURITY_POLICY)) {
                        result.add(KEY_WLAN_PERMISSION_AVAILABLE);
                    }

                    /*if (!Utils.ENABLE_WIFI_CONNECTION_TYPE) {
                        result.add(KEY_WIFI_CONNECT_TYPE);
                    }*/

                    if (Vendor.AIO != mOpBranding || !Utils.isPackageExists(context, "com.smithmicro.netwise.director.cricket")) {
                        result.add(KEY_CRICKET_MANAGER);
                    }

                    /*if (!"CMCC".equals(mVendorNotificationStyle)) {
                        result.add(KEY_NOTIFY_CMCC);
                    }*/

                    if(!((SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTSECUREWIFI)
                            || Utils.isSupportedCountryForEurOnSecureWiFi())
                            && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_SECURE_WIFI"))) {
                        result.add(KEY_SECURE_WIFI);
                    }

                    if (!mIsSupportMWIPS || !"".equals(Utils.CONFIG_SECURE_SVC_INTEGRATION)) {
                        result.add(KEY_MOBILE_WIPS);
                    }

                    // >>>WCM>>>
                    if (Utils.locateSmartNetworkSwitch(context) == Utils.WIFI_SMART_NETWORK_SWITCH_DISABLED || Utils.isWifiOnly(context)) {
                        result.add(KEY_SWITCH_TO_MOBILE_DATA);
                    }
                    // <<<WCM<<<

                    String salesCode = Utils.getSalesCode();
                    if (!"CHA".equals(salesCode)|| mIsCharterWifiNotSupported) {
                        Log.d(TAG, "salesCode "+salesCode+" remove Charter Menu");
                        result.add(KEY_OPTIMIZE_CHARTER);
                    }

                    if (!"ChinaNalSecurity".equals(CSC_COMMON_CHINA_NAL_SECURITY_TYPE)) {
                        result.add(KEY_WLAN_PERMISSION_AVAILABLE);
                    }

                    if (Utils.isTablet()) {
                        result.add(KEY_INSTALL_CREDENTIAL);
                    }

                    String removedList = "";
                    for(String list : result) {
                        removedList += list+" ";
                    }

                    Log.d(TAG, "Remove preferences at Advanced Wi-Fi Searching Result : "+removedList);

                    return result;
                }

                @Override
                public List<SearchIndexableRaw> secGetVariableRawDataToIndex(Context context, boolean enabled) {
                    final List<SearchIndexableRaw> results = new ArrayList<>();
                    SearchIndexableRaw data;
                    final Resources res = context.getResources();
                    if (Vendor.VZW == mOpBranding) {
                        data = new SearchIndexableRaw(context);
                        data.key = KEY_NOTIFY_OPEN_NETWORK;
                        data.title = res.getString(R.string.wifi_notify_open_networks_vzw);
                        data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                        data.summaryOn = res.getString(R.string.wifi_notify_open_networks_summary_vzw);
                        results.add(data);
                    }

                    if (Utils.isTablet()) {
                        data = new SearchIndexableRaw(context);
                        data.key = KEY_INSTALL_CREDENTIAL;
                        data.title = res.getString(R.string.wifi_install_network_certificates);
                        data.screenTitle = res.getString(R.string.wifi_menu_advanced_button);
                        data.summaryOn = res.getString(R.string.wifi_install_network_credentials_summary_tablet);
                        results.add(data);
                    }

                    return results;
                }

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    //SEC_START : SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
                    int knoxCustomSettingsState = 0;
                    SettingsManager knoxCustomSettingsManager = SettingsManager.getInstance();
                    if(knoxCustomSettingsManager != null) {
                        knoxCustomSettingsState = knoxCustomSettingsManager.getSettingsHiddenState();
                    }
                    if(((knoxCustomSettingsState & CustomDeviceManager.SETTINGS_DEVELOPER) != 0) ) {
                        return false;
                    } else {
                        return true;
                    }
               }
            };
}
