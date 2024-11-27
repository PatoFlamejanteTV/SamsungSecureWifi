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

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.SettingsActivity;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.Utils;
import com.android.settings.wifi.p2p.WifiP2pPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;

import com.samsung.android.settings.wifi.WifiPowerSavingModePrefController;
import com.samsung.android.settings.wifi.PasspointPreferenceController;
import com.samsung.android.settings.wifi.ScanForFreeWlansPrefController;
import com.samsung.android.settings.wifi.WifiManageNetworkPrefController;
import com.samsung.android.settings.wifi.NotifyOpenNetworkPrefController;
import com.samsung.android.settings.wifi.WifiAutoConnectXfinityPrefController;
import com.samsung.android.settings.wifi.ShowWifiPopupPreferenceController;
import com.samsung.android.settings.wifi.WlanPermissionAvailablePrefController;
import com.samsung.android.settings.wifi.WifiConnectionTypePrefController;
import com.samsung.android.settings.wifi.WifiControlHistoryPrefController;
import com.samsung.android.settings.wifi.InstallCredentialsPrefController;
import com.samsung.android.settings.wifi.SmartNetworkSwitchPrefController;
import com.samsung.android.settings.wifi.AutoWifiPreferenceController;
import com.samsung.android.settings.wifi.CMCCEnableWarningPrefController;
import com.samsung.android.settings.wifi.WifiProgressiveDisclosureMixin;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sec.android.app.CscFeatureTagCommon;
import com.sec.android.app.CscFeatureTagWifi;
import com.sec.android.app.SecProductFeature_WLAN;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;


public class ConfigureWifiSettings extends DashboardFragment {

    private static final String TAG = "ConfigureWifiSettings";

    public static final String KEY_IP_ADDRESS = "current_ip_address";
    public static final String KEY_WLAN_PERMISSION_AVAILABLE = "wlan_permission_available";
    public static final int WIFI_WAKEUP_REQUEST_CODE = 600;
    
    private static final String EXTRA_LAUNCH_FROM_SETUPWIARD = "extra_launch_from_setupwizard";
    private static final String PREF_KEY_ADAPTIVE_WIFI = "advanced_adaptive_wifi";
    private static final String PREF_KEY_NETWORK_SETTINGS = "advanced_network_settings";
    private static final String PREF_KEY_SLEEP_POLICY = "sleep_policy";
    private static final String PREF_KEY_MANAGE_NETWORK = "wifi_manage_network";
    private static final String PREF_KEY_HOTSPOT_20 = "wifi_hs20_enable";
    private static final String PREF_KEY_WPS_PUSH = "wps_push_button";

    private static final int TILE_LIMIT = 2; //Number of Preference Category

    private boolean mInSetupWizard = false;

    private PreferenceCategory mAdaptiveWifiCategory;
    private PreferenceCategory mNetworkSettingsCategory;

    private WifiProgressiveDisclosureMixin mWifiProgressiveDisclosureMixin;
    private static List<AbstractPreferenceController> controllers;
    private WifiManager mWifiManager;

    private static final String CSC_COMMON_CHINA_NAL_SECURITY_TYPE =
            SemCscFeature.getInstance().getString(
            CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_CONFIGLOCALSECURITYPOLICY);
    private Context mContext;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CONFIGURE_WIFI;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceStates();
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_configure_settings;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        mContext = getActivity().getBaseContext();
        Intent intent = getActivity().getIntent();
        boolean isSetupwizardFinish = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) != 0;
        mInSetupWizard = intent.getBooleanExtra(EXTRA_LAUNCH_FROM_SETUPWIARD, false);

        if (!isSetupwizardFinish && mInSetupWizard) {
            Log.i(TAG, "Launch from setupwizard "+mInSetupWizard);
        } else {
            Log.i(TAG, "Launch from WifiSettings setupwizard : "+mInSetupWizard);
        }

        return ConfigureWifiPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle(), getFragmentManager(), mInSetupWizard);
    }

    private static List<AbstractPreferenceController> ConfigureWifiPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle, FragmentManager fragmentManger, boolean inSetupWizard) {
        final WifiManager wifiManager = context.getSystemService(WifiManager.class);
        if (controllers != null) {
            Log.e(TAG, "controllers is not null. clear");
            controllers.clear();
        } else {
            Log.e(TAG, "controllers is null. new alloc");
            controllers = new ArrayList<>();
        }
        controllers.add(new WifiInfoPreferenceController(context, lifecycle, wifiManager));
        controllers.add(new WifiPowerSavingModePrefController(context, lifecycle, wifiManager));
        controllers.add(new PasspointPreferenceController(context, lifecycle, wifiManager, inSetupWizard));
        //controllers.add(new WpsPreferenceController(context, lifecycle, wifiManager, fragmentManger, inSetupWizard));
        controllers.add(new ScanForFreeWlansPrefController(context, inSetupWizard));
        controllers.add(new SecureWifiPrefController(context, inSetupWizard, lifecycle, wifiManager));
        controllers.add(new WifiManageNetworkPrefController(activity, fragment, context, lifecycle, wifiManager));
        controllers.add(new NotifyOpenNetworkPrefController(context, lifecycle, wifiManager));
        controllers.add(new WifiAutoConnectXfinityPrefController(context, lifecycle));
        //controllers.add(new CharterWifiController(context, lifecycle));
        controllers.add(new ShowWifiPopupPreferenceController(context, inSetupWizard));
        controllers.add(new WlanPermissionAvailablePrefController(context, inSetupWizard));
        controllers.add(new WifiConnectionTypePrefController(context, wifiManager, inSetupWizard));
        controllers.add(new WifiControlHistoryPrefController(activity, fragment, context));
        controllers.add(new InstallCredentialsPrefController(context, inSetupWizard));
        controllers.add(new SmartNetworkSwitchPrefController(activity, fragment, context));
        controllers.add(new AutoWifiPreferenceController(context, lifecycle, wifiManager));
        controllers.add(new CMCCEnableWarningPrefController(context, inSetupWizard));
        return controllers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*if (resultCode == WIFI_WAKEUP_REQUEST_CODE && mWifiWakeupPreferenceController != null) {
            mWifiWakeupPreferenceController.onActivityResult(requestCode, resultCode);
            return;
        }
        if (resultCode == UseOpenWifiPreferenceController.REQUEST_CODE_OPEN_WIFI_AUTOMATICALLY
                && mUseOpenWifiPreferenceController == null) {
            mUseOpenWifiPreferenceController.onActivityResult(requestCode, resultCode);
            return;
        }*/
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        refreshAllPreferences(mContext);
    }

    @Override
    protected void updatePreferenceStates() {
        super.updatePreferenceStates();
        final PreferenceScreen screen = getPreferenceScreen();
        if (mWifiProgressiveDisclosureMixin == null) {
            return;
        }

        mWifiProgressiveDisclosureMixin.updateDivider();

        for (AbstractPreferenceController controller : controllers) {
            if (!controller.isAvailable()) {
                continue;
            }
            final String key = controller.getPreferenceKey();

            final Preference preference = mWifiProgressiveDisclosureMixin.findPreference(screen, key);
            if (preference == null) {
                Log.d(TAG, String.format("Cannot find preference with key %s in Controller %s",
                        key, controller.getClass().getSimpleName()));
                continue;
            }
            controller.updateState(preference);
        }
    }

    private void refreshAllPreferences(Context context) {
        // First remove old preferences.
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
        mNetworkSettingsCategory = (PreferenceCategory) findPreference(PREF_KEY_NETWORK_SETTINGS);

        if (mWifiProgressiveDisclosureMixin == null && !mInSetupWizard){
            Log.d(TAG, "mWifiProgressiveDisclosureMixin set limit");
            mWifiProgressiveDisclosureMixin = new WifiProgressiveDisclosureMixin(context, getPreferenceScreen(), false);
            getLifecycle().addObserver(mWifiProgressiveDisclosureMixin);
            if (!Utils.isTablet()){
                mWifiProgressiveDisclosureMixin.setTileLimit(TILE_LIMIT);
                mWifiProgressiveDisclosureMixin.collapse(getPreferenceScreen());
            }
            //remove Adaptive Wi-Fi Preference Category header for Wi-Fi only model
            if (mWifiProgressiveDisclosureMixin.isEmptyCategory(getPreferenceScreen())) {
                mAdaptiveWifiCategory.removeAll();
                mAdaptiveWifiCategory.setVisible(false);
            }
        }

        if (mInSetupWizard) {
            mAdaptiveWifiCategory.removeAll();
            mAdaptiveWifiCategory.setVisible(false);
            mNetworkSettingsCategory.removeAll();
            mNetworkSettingsCategory.setVisible(false);
        }
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.wifi_configure_settings;
                    return Arrays.asList(sir);
                }

                /*@Override
                public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                    final List<SearchIndexableRaw> result = new ArrayList<SearchIndexableRaw>();
                    final Resources res = context.getResources();
                    SearchIndexableRaw data = new SearchIndexableRaw(context);
                    if ("VZW".equals(Utils.readSalesCode())) {
                        data.key = "notify_open_networks";
                        data.title = res.getString(R.string.wifi_notify_open_networks_vzw);
                        data.keywords = Utils.getKeywordForSearch(context, R.string.wifi_notify_open_networks_summary_vzw);
                        result.add(data);
                    }
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    // >>>WCM>>>
                    if (Utils.locateSmartNetworkSwitch(context) == Utils.WIFI_SMART_NETWORK_SWITCH_DISABLED) {
                        result.add("wifi_poor_network_detection");
                    }
                    // <<<WCM<<<
                    List<String> keys = super.getNonIndexableKeys(context);

                    // If connected to WiFi, this IP address will be the same as the Status IP.
                    // Or, if there is no connection they will say unavailable.
                    boolean mIsSupprotAutoWifi = SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI;
                    if (!mIsSupprotAutoWifi || Utils.isWifiOnly(context)) {
                        result.add("auto_wifi");
                    }

                    boolean mIsSupportAdpsMenu = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS");
                    if (!mIsSupportAdpsMenu) {
                        result.add("wifi_adps");
                    }

                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
                        String cscFeature = Utils.CONFIG_MENU_HOTSPOT20;
                        String productFeature = SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_CONFIG_STATUS_HOTSPOT2_0;
                        int connected = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_CONNECTED_HISTORY, 0);
                        if (cscFeature.isEmpty()) {
                            if (productFeature.isEmpty()) {
                                result.add("wifi_hs20_enable");
                            } else if (!productFeature.contains("MENU_ON") && connected == 0) {
                                result.add("wifi_hs20_enable");
                            }
                        } else if (!cscFeature.contains("MENU_ON") && connected == 0) {
                            result.add("wifi_hs20_enable");
                        }
                    } else {
                        result.add("wifi_hs20_enable");
                    }

                    if (!SemCscFeature.getInstance().getBoolean("CscFeature_Wifi_SupportSleepPolicy")) {
                        result.add("sleep_policy");
                    }

                    if (!"WeChatWiFi".equals(Utils.CONFIG_SOCIAL_SVC_INTEGRATION)) {
                        result.add("wifi_scan_for_free_wlans");
                    }

                    String mEnableAutoConnectHotspot = SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT);
                    if (!"ATT".equals(mEnableAutoConnectHotspot)) {
                        result.add("att_auto_connect");
                    }

                    if (!Utils.SUPPORT_NOTIFICATION_MENU) {
                        result.add("notify_open_networks");
                    }

                    boolean mIsComcastWifiSupported = SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTCOMCASTWIFI);
                    if (!mIsComcastWifiSupported) {
                        result.add("auto_connect_xfinity_wifi");
                    }

                    boolean mIsSupprotWifiPromptDataOveruse = SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTHUXWIFIPROMPTDATAOVERUSE);
                    if (!mIsSupprotWifiPromptDataOveruse) {
                        result.add("show_wifi_popup");
                    }

                    if (!Utils.isChinaNAL()) {
                        result.add("wlan_permission_available");
                    }

                    if (!Utils.ENABLE_WIFI_CONNECTION_TYPE) {
                        result.add("wifi_connection_type");
                    }

                    String vendorNotificationStyle = SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGWIFINOTIFICATIONSTYLE);
                    if (!"CMCC".equals(vendorNotificationStyle)) {
                        result.add("wlan_notify_cmcc");
                    }

                    if(!(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTSECUREWIFI) && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_SECURE_WIFI"))) {
                        result.add("secure_wifi");
                    }

                    // >>>WCM>>>
                    if (Utils.locateSmartNetworkSwitch(context) == Utils.WIFI_SMART_NETWORK_SWITCH_DISABLED) {
                        result.add("wifi_poor_network_detection");
                    }
                    // <<<WCM<<<

                    if (!"CHA".equals(Utils.getSalesCode())) {
                        result.add("optimize_charter_wifi_networks");
                    }
                    if (!"ChinaNalSecurity".equals(CSC_COMMON_CHINA_NAL_SECURITY_TYPE)) {
                        keys.add(KEY_WLAN_PERMISSION_AVAILABLE);

                    String removedList = "";
                    for(String list : result) {
                        removedList += list+" ";
                    }

                    Log.d(TAG, "Remove preferences at Advanced Wi-Fi Searching Result : "+removedList);

                    return result;
                }*/             //todo

                /*protected boolean isPageSearchEnabled(Context context) {
                    return context.getResources()
                            .getBoolean(R.bool.config_show_wifi_settings);
                }*/
            };
}
