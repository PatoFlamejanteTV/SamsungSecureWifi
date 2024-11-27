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

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.INotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.ServiceManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;


import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.core.lifecycle.Lifecycle;
import com.android.settings.core.PreferenceController;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.network.NetworkScoreManagerWrapper;
import com.android.settings.network.NetworkScorerPickerPreferenceController;
import com.android.settings.network.WifiCallingPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.Utils;
import com.android.settings.utils.NotificationChannelHelper;
import com.android.settings.wifi.p2p.WifiP2pPreferenceController;

import com.samsung.android.settings.wifi.WifiPowerSavingModePrefController;
import com.samsung.android.settings.wifi.PasspointPreferenceController;
import com.samsung.android.settings.wifi.ScanForFreeWlansPrefController;
import com.samsung.android.settings.wifi.SecureWifiPrefController;
import com.samsung.android.settings.wifi.WifiManageNetworkPrefController;
import com.samsung.android.settings.wifi.AttAutoConnectionPrefController;
import com.samsung.android.settings.wifi.NotifyOpenNetworkPrefController;
import com.samsung.android.settings.wifi.WifiAutoConnectXfinityPrefController;
import com.samsung.android.settings.wifi.ShowWifiPopupPreferenceController;
import com.samsung.android.settings.wifi.WlanPermissionAvailablePrefController;
import com.samsung.android.settings.wifi.WifiConnectionTypePrefController;
import com.samsung.android.settings.wifi.WifiControlHistoryPrefController;
import com.samsung.android.settings.wifi.InstallCredentialsPrefController;
import com.samsung.android.settings.wifi.SmartNetworkSwitchPrefController;
import com.samsung.android.settings.wifi.CMCCEnableWarningPrefController;
import com.samsung.android.settings.wifi.AutoWifiPreferenceController;
import com.samsung.android.settings.wifi.WifiProgressiveDisclosureMixin;
import com.samsung.android.settings.bixby.EmSettingsManager;    // Bixby
import com.samsung.android.settings.SAUtils;

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

    private static final String EXTRA_LAUNCH_FROM_SETUPWIARD = "extra_launch_from_setupwizard";
    private static final int TILE_LIMIT = 2; //Number of Preference Category
    private static final String PREF_KEY_ADAPTIVE_WIFI = "advanced_adaptive_wifi";
    private static final String PREF_KEY_NETWORK_SETTINGS = "advanced_network_settings";
    private static final String PREF_KEY_SLEEP_POLICY = "sleep_policy";
    private static final String PREF_KEY_MANAGE_NETWORK = "wifi_manage_network";
    private static final String PREF_KEY_HOTSPOT_20 = "wifi_hs20_enable";
    private static final String PREF_KEY_WPS_PUSH = "wps_push_button";


    private boolean mInSetupWizard = false;

    private PreferenceCategory mAdpativeWifiCategory;
    private PreferenceCategory mNetworkSettingsCategory;

    private WifiProgressiveDisclosureMixin mWifiProgressiveDisclosureMixin;
    private static List<PreferenceController> controllers;
    private WifiManager mWifiManager;

    Context mContext;

    // Bixby
    private String mEmLastStateID;
    private boolean mWillRespondToEm = false;



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                if (mWillRespondToEm) {
                    mWillRespondToEm = false;

                    if ("WiFiManageNetworks".equals(mEmLastStateID)) {
                        startManageNetworkByBixby();
                    }
                }
            }
        }
    };


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferenceStates();

        // Bixby
        mEmSettingsManager = new EmSettingsManager();
        mEmSettingsManager.bindEmService(mContext, mEmCallback, "WiFiAdvanced");
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(mReceiver, filter,
                android.Manifest.permission.CHANGE_NETWORK_STATE, null);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Bixby
        mEmSettingsManager.clearEmService("WiFiAdvanced");
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CONFIGURE_WIFI;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_configure_settings;
    }

    @Override
    protected List<PreferenceController> getPreferenceControllers(Context context) {
        NetworkScoreManagerWrapper networkScoreManagerWrapper =
                new NetworkScoreManagerWrapper(context.getSystemService(NetworkScoreManager.class));
        mContext = getActivity().getBaseContext();
        Intent intent = getActivity().getIntent();
        boolean isSetupwizardFinish = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) != 0;
        if (intent.hasExtra(EXTRA_LAUNCH_FROM_SETUPWIARD)) {
            mInSetupWizard = intent.getBooleanExtra(EXTRA_LAUNCH_FROM_SETUPWIARD, false);
            Log.i(TAG, "Launch from setupwizard "+mInSetupWizard);
        } else if (!isSetupwizardFinish) {
            mInSetupWizard = true;
            Log.i(TAG, "Launch from setupwizard "+mInSetupWizard);
        } else {
            Log.i(TAG, "Launch from WifiSettings setupwizard : "+mInSetupWizard);
        }

        return ConfigureWifiPreferenceControllers(context, getActivity(), this /* fragment */,
                getLifecycle(), getFragmentManager(), networkScoreManagerWrapper, mInSetupWizard);
    }

    private static List<PreferenceController> ConfigureWifiPreferenceControllers(Context context,
            Activity activity, Fragment fragment, Lifecycle lifecycle, FragmentManager fragmentManger, NetworkScoreManagerWrapper networkScoreManagerWrapper, boolean inSetupWizard) {
        final WifiManager wifiManager = context.getSystemService(WifiManager.class);
        controllers = new ArrayList<>();
        controllers.add(new WifiSleepPolicyPreferenceController(context));
        controllers.add(new WifiInfoPreferenceController(context, lifecycle, wifiManager));
        controllers.add(new WifiPowerSavingModePrefController(context, lifecycle, wifiManager));
        controllers.add(new PasspointPreferenceController(context, lifecycle, wifiManager, inSetupWizard));
        controllers.add(new WpsPreferenceController(context, lifecycle, wifiManager, fragmentManger, inSetupWizard));
        controllers.add(new ScanForFreeWlansPrefController(context, inSetupWizard));
        controllers.add(new SecureWifiPrefController(context, inSetupWizard, lifecycle, wifiManager));
        controllers.add(new WifiManageNetworkPrefController(activity, fragment, context, lifecycle, wifiManager));
        controllers.add(new AttAutoConnectionPrefController(context, lifecycle, wifiManager));
        controllers.add(new NotifyOpenNetworkPrefController(context, lifecycle, wifiManager));
        controllers.add(new WifiAutoConnectXfinityPrefController(context, lifecycle));
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

        for (PreferenceController controller : controllers) {
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

        for (PreferenceController controller : controllers) {
            controller.displayPreference(getPreferenceScreen());
        }

        if (mWifiProgressiveDisclosureMixin == null && !mInSetupWizard){
            Log.d(TAG, "mWifiProgressiveDisclosureMixin set limit");
            mWifiProgressiveDisclosureMixin = new WifiProgressiveDisclosureMixin(context, getPreferenceScreen(), false);
            getLifecycle().addObserver(mWifiProgressiveDisclosureMixin);
            mWifiProgressiveDisclosureMixin.setTileLimit(TILE_LIMIT);
            mWifiProgressiveDisclosureMixin.collapse(getPreferenceScreen());
        }

        if (mInSetupWizard) {
            mAdpativeWifiCategory = (PreferenceCategory) findPreference(PREF_KEY_ADAPTIVE_WIFI);
            mNetworkSettingsCategory = (PreferenceCategory) findPreference(PREF_KEY_NETWORK_SETTINGS);
            mAdpativeWifiCategory.removeAll();
            mAdpativeWifiCategory.setVisible(false);
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

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final ArrayList<String> result = new ArrayList<String>();

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

                    if ("WeChatWiFi".equals(Utils.CONFIG_SOCIAL_SVC_INTEGRATION)) {
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

                    String removedList = "";
                    for(String list : result) {
                        removedList += list+" ";
                    }
                    Log.d(TAG, "Remove preferences at Advanced Wi-Fi Searching Result : "+removedList);

                    return result;
                }
            };


    // Bixby
    private boolean isMobileHotstpotEnabled() {
        if (mWifiManager == null) {
            Log.e(TAG, "isMobileHotstpotEnabled, Wifi Manager is null so returning false");
            return false;
        }

        int wifiApState = mWifiManager.getWifiApState();
        if (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED || wifiApState == WifiManager.WIFI_AP_STATE_ENABLING ) {
            return true;
        }

        return false;
    }


    private boolean isWifiSharingEnabled() {
        try {
            if (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.WIFI_AP_WIFI_SHARING) == 1) {
                return true;
            } else if (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.WIFI_AP_WIFI_SHARING) == 0) {
                return false;
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "isWifiSharingEnabled, SettingNotFoundException");
        }

        return false;
    }


    private void startManageNetworkByBixby() {
        Bundle args = new Bundle();
        args.putBoolean("manage_network", true);

        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).startPreferencePanel(
                this, WifiSettings.class.getCanonicalName(),
                args, R.string.wifi_manage_network, null, this, 0);
        } else {
            startFragment(this,
                WifiSettings.class.getCanonicalName(),
                R.string.wifi_manage_network, -1, args);
        }

        if (mEmSettingsManager.isLastState()) {
            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "yes");
            mEmSettingsManager.requestNlg("WiFiManageNetworks");
        }

        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);

        SAUtils.getInstance(mContext).insertLog(R.string.screen_wifi_advanced_setting, R.string.event_wifi_advanced_manage_networks);
    }


    private void enableHotspot20ByBixby(boolean enable) {
        for (PreferenceController controller : controllers) {
            if (PREF_KEY_HOTSPOT_20.equals(controller.getPreferenceKey())) {
                if (!controller.isAvailable()) {
                    mEmSettingsManager.addNlgScreenParam("Hotspot2.0AP", "TurnedOn", "no");
                    mEmSettingsManager.requestNlg("WiFiAdvanced");
                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);

                    return;
                }

                if (enable) {
                    if (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE, 0) == 0) {
                       Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE, 1);
                       controller.updateState(findPreference(controller.getPreferenceKey()));

                        if (mEmSettingsManager.isLastState()) {
                            mEmSettingsManager.addNlgScreenParam("Hotspot2.0", "AlreadyOn", "no");
                            mEmSettingsManager.requestNlg("WiFiAdvanced");
                        }

                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                    } else {
                        mEmSettingsManager.addNlgScreenParam("Hotspot2.0", "AlreadyOn", "yes");
                        mEmSettingsManager.requestNlg("WiFiAdvanced");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    }
                } else {
                    if (mWifiManager.isWifiEnabled()) {
                        if (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE, 0) == 1) {
                            Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE, 0);
                            controller.updateState(findPreference(controller.getPreferenceKey()));

                            mEmSettingsManager.addNlgScreenParam("Hotspot2.0", "AlreadyOff", "no");
                            mEmSettingsManager.requestNlg("WiFiAdvanced");
                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                        } else {
                            if (mEmSettingsManager.isLastState()) {
                                mEmSettingsManager.addNlgScreenParam("Hotspot2.0", "AlreadyOff", "yes");
                                mEmSettingsManager.requestNlg("WiFiAdvanced");
                            }

                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                        }
                    } else {
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "no");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    }
                }
            }
        }
    }


    private void showWpsDialogByBixby() {
        WpsFragment wpsFragment = new WpsFragment(WpsInfo.PBC);
        wpsFragment.show(getFragmentManager(), PREF_KEY_WPS_PUSH);

        if (mEmSettingsManager.isLastState()) {
            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "yes");
            mEmSettingsManager.requestNlg("WiFiAdvanced");
        }

        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);

        return;
    }


    public static class WpsFragment extends InstrumentedDialogFragment {
        private static int mWpsSetup;

        // Public default constructor is required for rotation.
        public WpsFragment() {
            super();
        }

        public WpsFragment(int wpsSetup) {
            super();
            mWpsSetup = wpsSetup;
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.DIALOG_WPS_SETUP;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new WpsDialog(getActivity(), mWpsSetup);
        }
    }


    public EmSettingsManager.IEmCallback mEmCallback = new EmSettingsManager.IEmCallback() {
        @Override
        public void onStateReceived() {
            String stateId = mEmSettingsManager.getStateId();
            Log.d(TAG, "mEmCallback, stateId: " + stateId);

            if (stateId.equals("WiFiManageNetworks")) {
                if (mWifiManager.isWifiEnabled()) {
                    startManageNetworkByBixby();
                } else {
                    if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                        mEmSettingsManager.addNlgScreenParam("MobileHotspot", "AlreadyOn", "yes");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    } else {
                        if (mWifiManager.setWifiEnabled(true)) {
                            mWillRespondToEm = true;
                            mEmLastStateID = stateId;
                        } else {
                            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "no");
                            mEmSettingsManager.requestNlg("WiFiSettings");
                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                        }
                    }
                }
            } else if (stateId.equals("WiFiSetPasspointOn")) {
                mWifiProgressiveDisclosureMixin.spread();

                if (mWifiManager.isWifiEnabled()) {
                    enableHotspot20ByBixby(true);
                } else {
                    if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                        mEmSettingsManager.addNlgScreenParam("MobileHotspot", "AlreadyOn", "yes");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    } else if (mWifiManager.setWifiEnabled(true)) {
                        enableHotspot20ByBixby(true);
                    } else {
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "no");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    }
                }
            } else if (stateId.equals("WiFiSetPasspointOff")) {
                mWifiProgressiveDisclosureMixin.spread();
                enableHotspot20ByBixby(false);
            } else if (stateId.equals("WiFiWpsPushButton")) {
                mWifiProgressiveDisclosureMixin.spread();

                if (mWifiManager.isWifiEnabled()) {
                    showWpsDialogByBixby();
                } else {
                    if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                        mEmSettingsManager.addNlgScreenParam("MobileHotspot", "AlreadyOn", "yes");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    } else {
                        if (mWifiManager.setWifiEnabled(true)) {
                            showWpsDialogByBixby();
                        } else {
                            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "no");
                            mEmSettingsManager.requestNlg("WiFiSettings");
                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                        }
                    }
                }
            } else if (stateId.equals("WiFiSetSleepPolicy")) {
                mWifiProgressiveDisclosureMixin.spread();

                if (mEmSettingsManager.getParamsSize() > 0) {
                    try {
                        int param = mEmSettingsManager.getParamInt(0);
                        Log.d(TAG, "mEmCallback, param: " + param);

                        int value = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
                            Settings.Global.WIFI_SLEEP_POLICY_NEVER);

                        if (param < 0 || param > 2) {
                            mEmSettingsManager.addNlgScreenParam("SleepPolicy", "Exist", "no");
                            mEmSettingsManager.requestNlg("WifiAdvanced");
                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);

                            return;
                        } else if (param == value) {
                            String[] values = mContext.getResources().getStringArray(R.array.wifi_sleep_policy_values);
                            final int summaryArrayResId = Utils.isWifiOnly(mContext) ?
                                R.array.wifi_sleep_policy_entries_wifi_only : R.array.wifi_sleep_policy_entries;
                            String[] summaries = mContext.getResources().getStringArray(summaryArrayResId);
                            String strParam = "";

                            if (param == 2) {
                                strParam = mContext.getString(R.string.background_data_restriction_always);
                            } else if (param == 0) {
                                strParam = mContext.getString(R.string.background_data_restriction_never);
                            } else {
                                strParam = summaries[1];
                            }

                            mEmSettingsManager.addNlgScreenParam("SleepPolicy", "AlreadySet", "yes");
                            mEmSettingsManager.addNlgResultParam("SleepPolicy", strParam);
                            mEmSettingsManager.requestNlg("WifiAdvanced");
                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);

                            return;
                        }

                        for (PreferenceController controller : controllers) {
                            if (PREF_KEY_SLEEP_POLICY.equals(controller.getPreferenceKey()))  {
                                Log.d(TAG, "mEmCallback, change the value of the sleep policy from " + value + " to " + param);

                                String newValue = String.valueOf(param);
                                Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
                                Integer.parseInt(newValue));

                                controller.updateState(findPreference(controller.getPreferenceKey()));

                                if (mEmSettingsManager.isLastState()) {
                                    mEmSettingsManager.addNlgScreenParam("SleepPolicy", "AlreadySet", "no");
                                    mEmSettingsManager.requestNlg("WifiAdvanced");
                                }

                                mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);

                                return;
                            }
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "mEmCallback, parameter is null.");
                        mEmSettingsManager.addNlgScreenParam("SleepPolicy", "Exist", "no");
                        mEmSettingsManager.requestNlg("WiFiAdvanced");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    }
                }
             } else if (stateId.equals("WiFiShowMAC")) {
                mWifiProgressiveDisclosureMixin.spread();


                if (mEmSettingsManager.isLastState()) {
                    mEmSettingsManager.requestNlg("WifiAdvanced");
                }

                mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
             } else if (stateId.equals("WiFiShowIPAddress")) {
                mWifiProgressiveDisclosureMixin.spread();

                if (mEmSettingsManager.isLastState()) {
                    mEmSettingsManager.requestNlg("WifiAdvanced");
                }

                mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
            }
        }
    };
}
