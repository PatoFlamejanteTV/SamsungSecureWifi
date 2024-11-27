/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor; // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.NetworkScorerAppManager;
import android.net.NetworkScorerAppManager.NetworkScorerAppData;
import android.net.Uri; // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.os.UserHandle;
import android.os.Handler;
import android.os.Message;
import android.os.SemSystemProperties;
// SEC_START : Preference widget
//import android.support.v14.preference.SwitchPreference;
//import android.support.v7.preference.ListPreference;
//import android.support.v7.preference.Preference;
//import android.preference.PreferenceActivity;
//import android.support.v7.preference.Preference.OnPreferenceClickListener;
//import android.support.v7.preference.PreferenceScreen;
//import android.support.v7.preference.PreferenceGroup;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.PreferenceGroup;
import android.preference.SwitchPreference;
// SEC_END : Preference widget
import android.provider.Settings;
import android.provider.Settings.Global;
import android.sec.enterprise.content.SecContentProviderURI; // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import android.security.Credentials;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.os.SystemProperties;
import android.view.View;
import android.telephony.TelephonyManager;


import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.AppListSwitchPreference;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.samsung.android.feature.SemFloatingFeature;
import com.samsung.android.settings.SecSettingsPreferenceFragment;
import com.samsung.android.settings.WifiApUtils;
import com.samsung.android.settings.wifi.WifiBigDataUtil;
import com.samsung.android.settings.wifi.WifiStatusReceiver;  // SEC_PRODUCT_FEATURE_WLAN_SEC_SETTINGS_UX
import com.samsung.android.settings.wifi.WifiControlHistory;
import com.samsung.android.settings.bixby.EmSettingsManager;    // Bixby
import com.sec.android.app.SecProductFeature_KNOX; //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.android.app.CscFeatureTagWifi;
import com.sec.android.app.CscFeatureTagCommon;
import com.sec.android.app.SecProductFeature_WLAN;


import java.io.File;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.lang.StringBuilder;
import java.util.List;
import java.util.Collection;
// authentication Info of AKA(KTT), ends
import com.sec.android.app.SecProductFeature_KNOX; //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM

import com.samsung.android.feature.SemCscFeature;
import com.sec.android.app.CscFeatureTagWifi;
import com.sec.android.app.CscFeatureTagCommon;
import com.sec.android.app.SecProductFeature_WLAN;
import com.samsung.android.location.SemLocationManager;
import com.samsung.android.settings.wifi.CMCCWaringDialogPreference;


// SEC_START : Preference widget
//public class AdvancedWifiSettings extends SettingsPreferenceFragment
public class AdvancedWifiSettings extends SecSettingsPreferenceFragment
// SEC_END : Preference widget
        implements OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private static boolean DBG = android.os.Debug.semIsProductDev();
    private static final String TAG = "AdvancedWifiSettings";
    private static final String KEY_WIFI_ADPS = "wifi_adps"; //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
    private static final String KEY_MAC_IP_ADDRESS = "mac_address";
    private static final String KEY_FREQUENCY_BAND = "frequency_band";
    private static final String KEY_NOTIFY_OPEN_NETWORKS = "notify_open_networks";
    private static final String KEY_SLEEP_POLICY = "sleep_policy";
    private static final String KEY_POOR_NETWORK_DETECTION = "wifi_poor_network_detection"; // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
    private static final String KEY_POOR_NETWORK_DETECTION_AGGRESSIVE_MODE = "wifi_poor_network_detection_aggressive_mode"; // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
//  private static final String KEY_SCAN_ALWAYS_AVAILABLE = "wifi_scan_always_available";
    private static final String KEY_US_ATT_AUTO_CONNECT = "att_auto_connect";
    private static final String KEY_SCAN_FOR_FREE_WLNAS = "wifi_scan_for_free_wlans";   // CscFeature_Wifi_ConfigSocialSvcIntegration
    private static final String KEY_INSTALL_CREDENTIALS = "install_credentials";
    private static final String KEY_WIFI_ASSISTANT = "wifi_assistant";
    private static final String KEY_HS20_ENABLE = "wifi_hs20_enable";  // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
    private static final String KEY_WLAN_PERMISSION_AVAILABLE = "wlan_permission_available";// TAG_CscFeature_Common_ConfigLocalSecurityPolicy
    private static final String KEY_WIFI_CONNECTION_TYPE = "wifi_connection_type"; //CscFeature_Wifi_EnableMenuConnectionType
    private static final String KEY_WLAN_NOTIFY_CMCC = "wlan_notify_cmcc"; 
    private static final String KEY_WPS_PUSH = "wps_push_button";
    private static final String KEY_WPS_PIN = "wps_pin_entry";
    private static final String KEY_MANAGE_NETWORK = "wifi_manage_network";
    private static final String KEY_CONTROL_HISTORY = "wifi_control_history";
    private static final String KEY_SECURE_WIFI = "secure_wifi";
    // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
    private static final int WIFI_RESET_DIALOG_ID = 1;
    /* package */ static final int WPS_PBC_DIALOG_ID = 2;
    private static final int WPS_PIN_DIALOG_ID = 3;
    private static final int PROGRESS_ANIMATION_TIMEOUT = 40000;
    private Handler mHandler;

    private WpsDialog mWpsDialog;
    private Context mContext;

    private String mLastShownDialogKey = null;
    
    private static final String EXTRA_LAUNCH_FROM_SETUPWIARD = "extra_launch_from_setupwizard";
    private boolean mInSetupWizard = false;
    private static final String EXTRA_OPEN_PREF_KEY = "extra_open_pref_key";
    private String mOpenPrefKey;

    // wifioffload setting
    private static final String KEY_SHOW_WIFI_POPUP = "show_wifi_popup";
    private SwitchPreference mShowWifiPopup;
    
    //wifi Power Saving Mode
    private static final boolean mIsSupportAdpsMenu = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS");
    private boolean mIsDisableAdpsMenu = SemCscFeature.getInstance().getBoolean("CscFeature_Wifi_DisableAdpsMenu");

    // SEC_START : TAG_CSCFEATURE_COMMON_SUPPORTCOMCASTWIFI
    private static boolean mIsComcastWifiSupported = SemCscFeature.getInstance().getBoolean(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTCOMCASTWIFI);
    private static final String KEY_AUTOCONNECT_XFINITY_WIFI = "auto_connect_xfinity_wifi";
    private SwitchPreference mAutoConnectXfinityWifi;
    private static final String NWD_ACTION_SHUTDOWN_CM = "com.smithmicro.mnd.action.SHUTDOWN_CM";
    private static final String NWD_ACTION_RESUME_CM = "com.smithmicro.mnd.action.RESUME_CM";
    private static final String NWD_REQUEST_SERVICE_STATUS = "com.smithmicro.mnd.action.SERVICE_STATUS";
    private static final String NWD_PACKAGE_NAME = "com.smithmicro.netwise.director.comcast.oem";
    private static final String NWD_COMPONENT_NAME = "com.smithmicro.mnd.SDKAPIReceiver";
    private static final String NWD_SDK_API_SERVICE_STATUS = "com.smithmicro.mnd.NWD_SDK_API_SERVICE_STATUS";
    private static final String NWD_ACTION_SDK_API_SERVICE_READY = NWD_PACKAGE_NAME + ".NWD_SDK_API_SERVICE_READY";
    private static final String NWD_ACTION_SDK_API_SERVICE_STOPPED = NWD_PACKAGE_NAME + ".NWD_SDK_API_SERVICE_STOPPED";
    private static final String NWD_SDK_PERMISSION = "com.smithmicro.netwise.director.comcast.oem.NWD_SDK";
    private static final String NWD_SDK_API_CM_SUSPENDED = "com.smithmicro.mnd.NWD_SDK_API_CM_SUSPENDED";
    private static final String NWD_CHECK_PERMISSIONS = "CHECK_PERMISSIONS";
    private static final String CscFeature_Wifi_ConfigAutoWifiNaming = "CscFeature_Wifi_ConfigAutoWifiNaming";
    private boolean mIsNwdCmEnabled = true;
    private IntentFilter mNetwiseIntentFilter;    
    private static final int NWD_INITIAL_RESULT_CODE = -1;
    private enum NWD_SDK_API_RESULT
    {
        OK,
        SERVICE_NOT_RUNNING,
        SERVICE_NOT_READY,
        SERVICE_SHUTTING_DOWN,
        SERVICE_DISABLED,
        NO_EFFECT,
        INVALID_PARAM,
        CONFLICTS_PENDING_ACTION,
        SERVICE_NEED_PERMISSIONS,
        API_NOT_SUPPORTED,
        SUSPENDED_BY_PRIORITY_CHECK
    }
 
    private enum NWD_SERVICE_STATE {
        UNKNOWN,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
    }
    // SEC_END : TAG_CSCFEATURE_COMMON_SUPPORTCOMCASTWIFI

    private SwitchPreference mWifiAdps; //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS

    private IntentFilter mFilter;
    private WifiManager mWifiManager;
    SwitchPreference mEnableHs20; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
    SwitchPreference mNotifyOpenNetworks; //SEC_PRODUCT_FEATURE_SEC_SETTINGS_UX
    //SEC_PRODUCT_FEATURE_WLAN_CONFIG_AUTO_CONNECT_HOTSPOT
    SwitchPreference mAttAutoConnection;
    Preference mManagePref;
    Preference mWpsPushPref;
    Preference mWpsPinPref;
    private NetworkScoreManager mNetworkScoreManager;
    private AppListSwitchPreference mWifiAssistantPreference;

    // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
    private Preference mPoorNetworkDetection = null;
    private SwitchPreference mPoorNetworkDetectionAggressiveMode = null;
    private WifiSmartNetworkSwitchEnabler mWifiSmartNetworkSwitchEnabler;
    // Bixby
    // EmSettingsManager mEmSettingsManager = null;
    private String mEmLastStateID;
    private boolean mWillRespondToEm = false;
    private static final String mBixbyCurrentStateId = "WiFiAdvanced";
    private static final String MANAGE_NETWORK = "manage_network";
    private static final String VendorNotificationStyle = SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGWIFINOTIFICATIONSTYLE); //TAG_CSCFEATURE_WIFI_CONFIGWIFINOTIFICATIONSTYLE
    private static final String mEnableAutoConnectHotspot = SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT);
    private static final String FEATURE_SLOCATION = "com.sec.feature.slocation";

    // Secure Wi-Fi
    private static final String ACTION_SECURE_WIFI = "com.samsung.android.fast.ACTION_SECURE_WIFI";
    private static final String SECURE_WIFI_PACKAGE = "com.samsung.android.fast";
    private Preference mSecureWifiPref;
    private SecureWifiManager mSecureWifiManager;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION) ||
                action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION) ||
                action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                refreshWifiInfo();
            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
                if (state == WifiManager.WIFI_STATE_ENABLED) {
                    if (mIsSupportAdpsMenu && mWifiAdps != null && !mIsDisableAdpsMenu) { //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
                        mWifiAdps.setEnabled(true);
                    }
                    if (mNotifyOpenNetworks != null) { //SEC_PRODUCT_FEATURE_SEC_SETTINGS_UX
                        mNotifyOpenNetworks.setEnabled(true);
                    }
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                            && mEnableHs20 != null) {
                        mEnableHs20.setEnabled(true);
                    }
                    if ("ATT".equals(mEnableAutoConnectHotspot) //TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT
                            && mAttAutoConnection != null) {
                        mAttAutoConnection.setEnabled(true);
                    }
                    if(mManagePref != null){
                        mManagePref.setEnabled(true);
                    }
                    if (mWpsPushPref != null){
                        mWpsPushPref.setEnabled(true);
                    }
                    if (mWpsPinPref != null){
                        mWpsPinPref.setEnabled(true);
                    }

                    // Bixby
                    if (mWillRespondToEm) {
                        mWillRespondToEm = false;
                        
                        if ("WiFiManageNetworks".equals(mEmLastStateID)) {
                            startManageNetworkByBixby();
                        } else if ("WiFiWpsPushButton".equals(mEmLastStateID)) {
                            showWpsDialogByBixby();
                        } else if ("WiFiSetPasspointOn".equals(mEmLastStateID)) {
                            enableHotspot20ByBixby(true);
                            return;
                        }
                    }
                } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                    if (mIsSupportAdpsMenu && mWifiAdps != null && !mIsDisableAdpsMenu) { //FEATURE_WIFI_ADPS
                        mWifiAdps.setEnabled(false);
                    }
                    if (mNotifyOpenNetworks != null) { //SEC_PRODUCT_FEATURE_SEC_SETTINGS_UX
                        mNotifyOpenNetworks.setEnabled(false);
                    }
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                            && mEnableHs20 != null) {
                        mEnableHs20.setEnabled(false);
                    }
                    if ("ATT".equals(mEnableAutoConnectHotspot) //TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT
                            && mAttAutoConnection != null) {
                        mAttAutoConnection.setEnabled(false);
                    }
                    if(mManagePref != null){
                        mManagePref.setEnabled(false);
                    }
                    if (mWpsPushPref != null){
                        mWpsPushPref.setEnabled(false);
                    }
                    if (mWpsPinPref != null){
                        mWpsPinPref.setEnabled(false);
                    }
                }
                // Secure Wi-Fi
                if (mSecureWifiPref != null) {
                    if(mWifiManager.isWifiEnabled()) {
                       mSecureWifiPref.setEnabled(true);
                    } else {
                        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE); 
                        if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
                            mSecureWifiPref.setEnabled(false);
                        } 
                        else {
                            mSecureWifiPref.setEnabled(true); 
                        }
                    }
                }
            } else if (WifiManager.HS20_DISABLED_COMPLETE_BY_CREDERROR_ACTION.equals(action)) {
                if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
                    Log.e(TAG, "HS20_DISABLED_COMPLETE_BY_CREDERROR_ACTION");
                    changeHotspot20(false);
                }
            } else if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                // Secure Wi-Fi
                if (mSecureWifiPref != null) {
                    if(mWifiManager.isWifiEnabled()) {
                       mSecureWifiPref.setEnabled(true);
                    } else {
                        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE); 
                        if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
                            mSecureWifiPref.setEnabled(false);
                        } 
                        else {
                            mSecureWifiPref.setEnabled(true); 
                        }
                    }
                }
			}
        }
    };

    // SEC_START : TAG_CSCFEATURE_COMMON_SUPPORTCOMCASTWIFI
    /**
     * Broadcast receiver for comcast ordered broadcast intents
     */
    private final BroadcastReceiver mNetwiseReceiver = new BroadcastReceiver() {
 
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (!mIsComcastWifiSupported) {
                Log.e(TAG, "Comcast Wi-Fi CSC is disabled");
                return;
            }
 
            String action = intent.getAction();
            int resultCode = getResultCode();
            Log.d(TAG, "Action : " + action + " , " + "Resultcode : " + resultCode);
 
            /**
             * Is this really required ?
             * Because when permissions are disabled, NWD service is STOPPED.
             * When any broadcast is sent, result code we get is SERVICE_NOT_RUNNING and not SERVICE_NEED_PERMISSIONS
             */
            if (resultCode == NWD_SDK_API_RESULT.SERVICE_NEED_PERMISSIONS.ordinal()) {
                Log.e(TAG, "Permissions are not enabled");
                sendBroadcastToNetwiseClient(NWD_CHECK_PERMISSIONS);
                mAutoConnectXfinityWifi.setChecked(mIsNwdCmEnabled);
                return;
            }
 
            if (NWD_ACTION_SHUTDOWN_CM.equals(action) || NWD_ACTION_RESUME_CM.equals(action)) {
                if (resultCode == NWD_SDK_API_RESULT.OK.ordinal()) { // Success
                    Log.d(TAG, "Action " + action + " is success. Lets wait for the Service to be restarted");
 
                    updateXfinitySetting(false);
                } else {
                    if (NWD_INITIAL_RESULT_CODE != resultCode) {
                        Log.e(TAG, "NWD SDK API result code : " + NWD_SDK_API_RESULT.values()[resultCode]);
                    }
                    // set to previous state due to wrong result code
                    mAutoConnectXfinityWifi.setChecked(mIsNwdCmEnabled);
                }
            } else if (NWD_REQUEST_SERVICE_STATUS.equals(action)) {
                if (resultCode == NWD_SDK_API_RESULT.OK.ordinal()) {
                    Bundle bundle = getResultExtras(false);
                    if (bundle != null) {
                        mIsNwdCmEnabled = !bundle.getBoolean(NWD_SDK_API_CM_SUSPENDED);
                        Log.d(TAG, "mIsNwdCmEnabled : " + mIsNwdCmEnabled);
                        mAutoConnectXfinityWifi.setChecked(mIsNwdCmEnabled);
 
                        int nwdServiceState = bundle.getInt(NWD_SDK_API_SERVICE_STATUS);
                        Log.d(TAG, "serviceState == " + nwdServiceState);
 
                        updateXfinitySetting(nwdServiceState == NWD_SERVICE_STATE.STARTED.ordinal() ? true : false);
                    }
                }  else {

                    // for result codes except OK make auto-connect option enable and switch preference OFF state.
                    mIsNwdCmEnabled = false;
                    mAutoConnectXfinityWifi.setChecked(mIsNwdCmEnabled);
                    updateXfinitySetting(true);

                }
            } else if (NWD_ACTION_SDK_API_SERVICE_READY.equals(action)) {
                Log.d(TAG, "NWD SDK API service state is Ready");
                updateXfinitySetting(true);
            }
        }
    };
 
    /**
     * Send ordered broadcast to Comcast(Netwise client) receiver
     * Set the comcast app package component and the 'magic word' as extras
     * @param action
     */
    private void sendBroadcastToNetwiseClient(final String action) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(NWD_PACKAGE_NAME, NWD_COMPONENT_NAME));
        intent.setAction(action);
        mContext.sendOrderedBroadcast(intent, NWD_SDK_PERMISSION, mNetwiseReceiver, null, Activity.RESULT_OK, null, null);
    }
 
    /**
     * Enable/disable Auto-Connect Xfinity and corresponding summary text
     *
     * @param enable or disable
     */
    private void updateXfinitySetting(final boolean enable) {
        mAutoConnectXfinityWifi.setEnabled(enable);
        if (enable) {
            mAutoConnectXfinityWifi.setSummary(getActivity().getString(R.string.auto_connect_xfinity_wifi_summary));
        } else {
            mAutoConnectXfinityWifi.setSummary(mAutoConnectXfinityWifi.isChecked() ? getActivity().getString(R.string.auto_connect_enabling) :
                    getActivity().getString(R.string.auto_connect_disabling));
        }
    }
    // SEC_END : TAG_CSCFEATURE_COMMON_SUPPORTCOMCASTWIFI

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wifi_advanced_settings);

        mContext = getActivity();

        if (mIsComcastWifiSupported) {
            mNetwiseIntentFilter = new IntentFilter();
            mNetwiseIntentFilter.addAction(NWD_ACTION_SHUTDOWN_CM);
            mNetwiseIntentFilter.addAction(NWD_ACTION_RESUME_CM);
            mNetwiseIntentFilter.addAction(NWD_REQUEST_SERVICE_STATUS);
            mNetwiseIntentFilter.addAction(NWD_ACTION_SDK_API_SERVICE_READY);
        }

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mNetworkScoreManager =
                (NetworkScoreManager) getSystemService(Context.NETWORK_SCORE_SERVICE);
        mSecureWifiManager = new SecureWifiManager(mContext);

        Intent intent = getActivity().getIntent();
        if (intent.hasExtra(EXTRA_LAUNCH_FROM_SETUPWIARD)) {
            mInSetupWizard = intent.getBooleanExtra(EXTRA_LAUNCH_FROM_SETUPWIARD, false);
            Log.i(TAG, "launch from setupwizard " + mInSetupWizard);
        } else {
            Log.i(TAG, "not setupwizard");
        }
        if (savedInstanceState == null && intent.hasExtra(EXTRA_OPEN_PREF_KEY)) {
            mOpenPrefKey = intent.getStringExtra(EXTRA_OPEN_PREF_KEY);
            Log.i(TAG, "start with " + mOpenPrefKey);
        } else {
            Log.i(TAG, "normal mode");
        }

        initPreferences();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.HS20_DISABLED_COMPLETE_BY_CREDERROR_ACTION);  // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
        mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);

        initSnsAggressiveModePreference();

        if ("AutoWiFi".equals(mOpenPrefKey)) {
            startPoorNetworkDetectionPressed();
            mOpenPrefKey = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsComcastWifiSupported) {
            getActivity().registerReceiver(mNetwiseReceiver, mNetwiseIntentFilter);
            sendBroadcastToNetwiseClient(NWD_REQUEST_SERVICE_STATUS);
        }
        modifyPreferences();
        getActivity().registerReceiver(mReceiver, mFilter,
                android.Manifest.permission.CHANGE_NETWORK_STATE, null);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        refreshWifiInfo();
        WifiStatusReceiver.mIsForegroundWifiSubSettings = true; //SEC_PRODUCT_FEATURE_WLAN_SEC_SETTINGS_UX
        if (Utils.isSupportGraceUX() && mWifiSmartNetworkSwitchEnabler != null) {
            mWifiSmartNetworkSwitchEnabler.resume();
        }

        // Bixby
        mEmSettingsManager = new EmSettingsManager();
        mEmSettingsManager.bindEmService(getContext(), mEmCallback, mBixbyCurrentStateId);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsComcastWifiSupported) {
            getActivity().unregisterReceiver(mNetwiseReceiver);
        }
        flushPreviousBigDataLogs();

        getActivity().unregisterReceiver(mReceiver);
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        WifiStatusReceiver.mIsForegroundWifiSubSettings = false; //SEC_PRODUCT_FEATURE_WLAN_SEC_SETTINGS_UX
        if (Utils.isSupportGraceUX() && mWifiSmartNetworkSwitchEnabler != null) {
            mWifiSmartNetworkSwitchEnabler.pause();
        }

        // Bixby
        mEmSettingsManager.clearEmService(mBixbyCurrentStateId);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.WIFI;
    }

    // Add/Remove Preference here
    private void initPreferences() {

        mWifiAdps = (SwitchPreference) findPreference(KEY_WIFI_ADPS); //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
        if (mWifiAdps != null) {
            if (mIsSupportAdpsMenu && !mIsDisableAdpsMenu) {
                mWifiAdps.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mWifiAdps);
                mWifiAdps = null;
            }
        }

        //Vzw Wifi Offload
        mShowWifiPopup = (SwitchPreference) findPreference(KEY_SHOW_WIFI_POPUP);
        if (mShowWifiPopup != null) {
            if (!mInSetupWizard && SemCscFeature.getInstance().getBoolean(
                    CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTHUXWIFIPROMPTDATAOVERUSE)) {
                mShowWifiPopup.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.WIFI_OFFLOAD_NETWORK_NOTIFY, 1) == 1);
                mShowWifiPopup.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mShowWifiPopup);
                mShowWifiPopup = null;
            }
        }

        // Comcast Auto-Connect Xfinity Wi-Fi setting
        mAutoConnectXfinityWifi = (SwitchPreference) findPreference(KEY_AUTOCONNECT_XFINITY_WIFI);
        if (!mInSetupWizard && mIsComcastWifiSupported) {
            if (mAutoConnectXfinityWifi != null) {
                getPreferenceScreen().addPreference(mAutoConnectXfinityWifi);
                updateXfinitySetting(false);
                mAutoConnectXfinityWifi.setOnPreferenceChangeListener(this);
            }
        } else {
            if (mAutoConnectXfinityWifi != null) {
                getPreferenceScreen().removePreference(mAutoConnectXfinityWifi);
            }
        }
       
        mNotifyOpenNetworks =
            (SwitchPreference) findPreference(KEY_NOTIFY_OPEN_NETWORKS);
        if (mNotifyOpenNetworks != null) {
            if (mInSetupWizard || !Utils.SUPPORT_NOTIFICATION_MENU) { 
                getPreferenceScreen().removePreference(mNotifyOpenNetworks);
            } else {
                if ("VZW".equals(Utils.readSalesCode())) {
                    mNotifyOpenNetworks.setTitle(getResources().getString(R.string.wifi_notify_open_networks_vzw));
                    mNotifyOpenNetworks.setSummary(getResources().getString(R.string.wifi_notify_open_networks_summary_vzw));
                }
                mNotifyOpenNetworks.setChecked(Settings.Global.getInt(getContentResolver(),
                    Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 1) == 1);
                mNotifyOpenNetworks.setOnPreferenceChangeListener(this);
            }
        }

        mManagePref = findPreference(KEY_MANAGE_NETWORK);
        if (mInSetupWizard || (!Utils.isSupportGraceUX() && mManagePref != null)) {
            getPreferenceScreen().removePreference(mManagePref);
        }

        mWpsPushPref = findPreference(KEY_WPS_PUSH);
        if(mWpsPushPref != null){
            if (!Utils.isSupportGraceUX()) {
                getPreferenceScreen().removePreference(mWpsPushPref);
            } else {
                mWpsPushPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference arg0) {
                        showDialog(WPS_PBC_DIALOG_ID);
                        return true;
                    }
                });
            }
        }

        mWpsPinPref = findPreference(KEY_WPS_PIN);
        if(mWpsPinPref != null){
            if (!Utils.isSupportGraceUX() || "KTT".equals(Utils.CONFIG_OP_BRANDING)) {
                getPreferenceScreen().removePreference(mWpsPinPref);
            } else {
                mWpsPinPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference arg0) {
                        showDialog(WPS_PIN_DIALOG_ID);
                        return true;
                    }
                });
            }
        }
       /* SwitchPreference scanAlwaysAvailable =
            (SwitchPreference) findPreference(KEY_SCAN_ALWAYS_AVAILABLE);
        scanAlwaysAvailable.setChecked(Global.getInt(getContentResolver(),
                    Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0) == 1);
*/

        SwitchPreference scanForFreeWlans = (SwitchPreference) findPreference(KEY_SCAN_FOR_FREE_WLNAS);
        if (scanForFreeWlans != null) {       
            if (mInSetupWizard || !"WeChatWiFi".equals(Utils.CONFIG_SOCIAL_SVC_INTEGRATION)) {
                getPreferenceScreen().removePreference(scanForFreeWlans);
            } else {
                scanForFreeWlans.setOnPreferenceChangeListener(this);
            }
        }

        CMCCWaringDialogPreference enableWarningcontrolPref = (CMCCWaringDialogPreference) findPreference(KEY_WLAN_NOTIFY_CMCC);
        if (!"CMCC".equals(VendorNotificationStyle) && enableWarningcontrolPref != null) {
                getPreferenceScreen().removePreference(enableWarningcontrolPref);
        }
        Intent intent = new Intent(Credentials.INSTALL_AS_USER_ACTION);
        intent.putExtra(Credentials.EXTRA_INSTALL_AS_UID, android.os.Process.WIFI_UID);
        Preference installCredentials = findPreference(KEY_INSTALL_CREDENTIALS);
        if (installCredentials != null) {
            installCredentials.setIntent(intent);
            if (mInSetupWizard) {
                getPreferenceScreen().removePreference(installCredentials);
            }
        }
        SwitchPreference isAllowPermissionPopup =
            (SwitchPreference) findPreference(KEY_WLAN_PERMISSION_AVAILABLE);
        if (!mInSetupWizard && Utils.isChinaNAL() && Utils.isSupportGraceUX() && isAllowPermissionPopup != null) {
            isAllowPermissionPopup.setChecked(Secure.getInt(getContentResolver(),Secure.WLAN_PERMISSION_AVAILABLE, 1) == 1);
            isAllowPermissionPopup.setOnPreferenceChangeListener(this);
        } else {
            if (isAllowPermissionPopup != null) {
                getPreferenceScreen().removePreference(isAllowPermissionPopup);
            }
        }

        mAttAutoConnection = (SwitchPreference) findPreference(KEY_US_ATT_AUTO_CONNECT);
        if (mAttAutoConnection != null) {
            mAttAutoConnection.setOnPreferenceChangeListener(this);
        }
        if (mInSetupWizard || !"ATT".equals(mEnableAutoConnectHotspot)) { //TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT
            if (mAttAutoConnection != null) {
                getPreferenceScreen().removePreference(mAttAutoConnection);
                mAttAutoConnection = null;
            }
        }

        final Context context = getActivity();
        mWifiAssistantPreference = (AppListSwitchPreference) findPreference(KEY_WIFI_ASSISTANT);
        Collection<NetworkScorerAppData> scorers =
                NetworkScorerAppManager.getAllValidScorers(context);
        if (!mInSetupWizard && UserHandle.myUserId() == UserHandle.USER_OWNER && !scorers.isEmpty()) {
            mWifiAssistantPreference.setOnPreferenceChangeListener(this);
            initWifiAssistantPreference(scorers);
        } else if (mWifiAssistantPreference != null) {
            getPreferenceScreen().removePreference(mWifiAssistantPreference);
        }

        //CscFeature_Wifi_EnableMenuConnectionType
        ListPreference wifiConnectType = (ListPreference) findPreference(KEY_WIFI_CONNECTION_TYPE);
        if (!mInSetupWizard && Utils.ENABLE_WIFI_CONNECTION_TYPE && wifiConnectType != null) {
            wifiConnectType.setOnPreferenceChangeListener(this);
            int value = Settings.System.getInt(getContentResolver(), Settings.System.WIFI_CONNECTION_TYPE, 0);
            String stringValue = String.valueOf(value);
            wifiConnectType.setValue(stringValue);
            updatecmccConnectTypeSummary(wifiConnectType, stringValue);
        } else {
            if (wifiConnectType != null) {
                getPreferenceScreen().removePreference(wifiConnectType);
            }
        }

        ListPreference sleepPolicyPref = (ListPreference) findPreference(KEY_SLEEP_POLICY);
        if (sleepPolicyPref != null) {
            if (!mInSetupWizard) {
                if (Utils.isWifiOnly(context)) {
                    sleepPolicyPref.setEntries(R.array.wifi_sleep_policy_entries_wifi_only);
                }
                sleepPolicyPref.setOnPreferenceChangeListener(this);
                int value = Settings.Global.getInt(getContentResolver(),
                        Settings.Global.WIFI_SLEEP_POLICY,
                        Settings.Global.WIFI_SLEEP_POLICY_NEVER);
                String stringValue = String.valueOf(value);
                sleepPolicyPref.setValue(stringValue);
                updateSleepPolicySummary(sleepPolicyPref, stringValue);
            } else {
                getPreferenceScreen().removePreference(sleepPolicyPref);
            }
        }

        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
        mEnableHs20 = (SwitchPreference) findPreference(KEY_HS20_ENABLE);
        if (mEnableHs20 != null) {
            mEnableHs20.setOnPreferenceChangeListener(this);
            mEnableHs20.setSummary(WifiApUtils.getString(getContext(),R.string.wifi_hotspot20_summary));
        }
        if (!mInSetupWizard && SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
            if ((!mWifiManager.isPasspointMenuVisible())
                    && (Settings.Secure.getInt(mContext.getContentResolver(),
                            Settings.Secure.WIFI_HOTSPOT20_CONNECTED_HISTORY, 0) == 0)) {
                if (mEnableHs20 != null)
                    getPreferenceScreen().removePreference(mEnableHs20);
                mEnableHs20 = null;
            }
        } else {
            if (mEnableHs20 != null)
                getPreferenceScreen().removePreference(mEnableHs20);
            mEnableHs20 = null;
        }

        Preference controlHistory = findPreference(KEY_CONTROL_HISTORY);
        if (controlHistory != null) {
            if (mInSetupWizard) {
                getPreferenceScreen().removePreference(controlHistory);
            }
        }

        // Secure Wi-Fi
        mSecureWifiPref = findPreference(KEY_SECURE_WIFI);
        if (mSecureWifiPref != null) {
            if (!(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTSECUREWIFI)
                    && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_SECURE_WIFI"))) {
                getPreferenceScreen().removePreference(mSecureWifiPref);
            }
        }
    }

    // Enable/Disable Preference here
    private void modifyPreferences() {

        boolean isWifiEnabled = mWifiManager.isWifiEnabled();

        if (mIsSupportAdpsMenu && mWifiAdps != null && !mIsDisableAdpsMenu) { //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
            mWifiAdps.setEnabled(isWifiEnabled);
            mWifiAdps.setChecked(Secure.getInt(getContentResolver(), Secure.WIFI_ADPS, 0) == 1);
        }

        //Vzw Wifi Offload
        if (mShowWifiPopup != null) {
            mShowWifiPopup.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.WIFI_OFFLOAD_NETWORK_NOTIFY, 1) == 1);
        }

        if (Utils.SUPPORT_NOTIFICATION_MENU) {
                mNotifyOpenNetworks.setChecked(Settings.Global.getInt(getContentResolver(),
                        Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
                mNotifyOpenNetworks.setEnabled(isWifiEnabled);
        }

        if (mManagePref != null) {
            mManagePref.setEnabled(isWifiEnabled);
        }

        if (mWpsPushPref != null){
            mWpsPushPref.setEnabled(isWifiEnabled);
        }
        if (mWpsPinPref != null) {
            mWpsPinPref.setEnabled(isWifiEnabled);
        }

        SwitchPreference scanForFreeWlans = (SwitchPreference) findPreference(KEY_SCAN_FOR_FREE_WLNAS);
        if (scanForFreeWlans != null) {
            if ("WeChatWiFi".equals(Utils.CONFIG_SOCIAL_SVC_INTEGRATION)) {
                scanForFreeWlans.setChecked(Secure.getInt(getContentResolver(), Secure.WIFI_SCAN_FOR_FREE_WLNAS, 1) == 1);
            }
        }
        //SEC_PRODUCT_FEATURE_WLAN_CHINA_CMCC_AP_NOTIFY
        if ("ATT".equals(mEnableAutoConnectHotspot)) { //TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT
            if (mAttAutoConnection != null) {
                if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
                    Uri uri = Uri.parse(SecContentProviderURI.WIFI_URI);
                    Cursor cr = mContext.getContentResolver().query(uri, null,
                               SecContentProviderURI.WIFIPOLICY_AUTOMATICCONNECTION_METHOD, null, null);
                    if (cr != null) {
                        try {
                            cr.moveToFirst();
                            if (cr.getString(cr.getColumnIndex(SecContentProviderURI.WIFIPOLICY_AUTOMATICCONNECTION_METHOD)).equals("false")) {
                                mAttAutoConnection.setEnabled(false);
                                mAttAutoConnection.setChecked(false);
                            } else {
                                mAttAutoConnection.setEnabled(isWifiEnabled);
                                mAttAutoConnection.setChecked(Secure.getInt(getContentResolver(), Secure.WIFI_AUTO_CONNECT, 1) == 1);
                            }
                        } finally {
                            cr.close();
                        }
                    }
                } else {
                    mAttAutoConnection.setEnabled(isWifiEnabled);
                    mAttAutoConnection.setChecked(Secure.getInt(getContentResolver(), Secure.WIFI_AUTO_CONNECT, 1) == 1);
                }
            }
        }
        //CscFeature_Wifi_EnableMenuConnectionType
        ListPreference wifiConnectType = (ListPreference) findPreference(KEY_WIFI_CONNECTION_TYPE);
        if (Utils.ENABLE_WIFI_CONNECTION_TYPE && wifiConnectType != null) {
            wifiConnectType.setEnabled(isWifiEnabled);
        }
        // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
        mEnableHs20 = (SwitchPreference) findPreference(KEY_HS20_ENABLE);
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
            if (mWifiManager.isPasspointMenuVisible()
                    || (Settings.Secure.getInt(mContext.getContentResolver(),
                            Settings.Secure.WIFI_HOTSPOT20_CONNECTED_HISTORY, 0) == 1)) {
                if (mEnableHs20 != null) {
                    mEnableHs20.setChecked(Settings.Secure.getInt(mContext.getContentResolver(),
                            Settings.Secure.WIFI_HOTSPOT20_ENABLE, 0) == 1);
                    mEnableHs20.setEnabled(isWifiEnabled);
                }
            }
        }
        // Secure Wi-Fi
        if (mSecureWifiPref != null) {
            String secureWifiMode = mSecureWifiManager.getSecureWifiMode();
            String secureWifiToC = mSecureWifiManager.getSecureWifiTermsAndConditionsAgreed();
            if ("true".equals(secureWifiToC)) {
                if ("0".equals(secureWifiMode)) {
                    mSecureWifiPref.semSetSummaryColorToColorPrimaryDark(false);
                    mSecureWifiPref.setSummary(mContext.getString(R.string.secure_wifi_summary));
                } else {
                    mSecureWifiPref.semSetSummaryColorToColorPrimaryDark(true);
                    mSecureWifiPref.setSummary(mContext.getString(R.string.secure_wifi_summary_insecure));
                }
            } else {
                mSecureWifiPref.semSetSummaryColorToColorPrimaryDark(false);
                mSecureWifiPref.setSummary(mContext.getString(R.string.secure_wifi_summary));
            }
		
            if(mWifiManager.isWifiEnabled()) {
                mSecureWifiPref.setEnabled(true);
            } else {
                TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE); 
                if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
                    mSecureWifiPref.setEnabled(false);
                } 
                else {
                    mSecureWifiPref.setEnabled(true); 
                }
            }
        }
    }

    private void initSnsAggressiveModePreference() {
        mPoorNetworkDetection = (SwitchPreference)findPreference(KEY_POOR_NETWORK_DETECTION);
        mPoorNetworkDetectionAggressiveMode = (SwitchPreference)findPreference(KEY_POOR_NETWORK_DETECTION_AGGRESSIVE_MODE);
        if(mPoorNetworkDetectionAggressiveMode != null && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_ADAPTIVE_WIFI") && 
                !"Smart Network Switch".equals(SemCscFeature.getInstance().getString(CscFeature_Wifi_ConfigAutoWifiNaming))){
            mPoorNetworkDetectionAggressiveMode.setTitle(mContext.getString(R.string.wifi_watchdog_connectivity_check_adaptive));
        }
        if (Utils.isSupportGraceUX()) {
            mWifiSmartNetworkSwitchEnabler = new WifiSmartNetworkSwitchEnabler(getActivity(), mPoorNetworkDetectionAggressiveMode);
        }
        updateSmartNetworkSwitchVisible();
    }


    private void initWifiAssistantPreference(Collection<NetworkScorerAppData> scorers) {
        int count = scorers.size();
        String[] packageNames = new String[count];
        int i = 0;
        for (NetworkScorerAppData scorer : scorers) {
            packageNames[i] = scorer.mPackageName;
            i++;
        }
        mWifiAssistantPreference.setPackageNames(packageNames,
                mNetworkScoreManager.getActiveScorerPackage());
    }

    private void updateSleepPolicySummary(Preference sleepPolicyPref, String value) {
        if (value != null) {
            String[] values = getResources().getStringArray(R.array.wifi_sleep_policy_values);
            final int summaryArrayResId = Utils.isWifiOnly(getActivity()) ?
                    R.array.wifi_sleep_policy_entries_wifi_only : R.array.wifi_sleep_policy_entries;
            String[] summaries = getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (value.equals(values[i])) {
                    if (i < summaries.length) {
                        if (i == 2) {
                            sleepPolicyPref.setSummary(mContext.getString(R.string.background_data_restriction_never));
                        } else if (i == 0) {
                            sleepPolicyPref.setSummary(mContext.getString(R.string.background_data_restriction_always));
                        } else {
                            sleepPolicyPref.setSummary(summaries[i]);
                        }
                        return;
                    }
                }
            }
        }

        sleepPolicyPref.setSummary("");
        Log.e(TAG, "Invalid sleep policy value: " + value);
    }
    private void updatecmccConnectTypeSummary(Preference wifiConnectType, String value) { //CscFeature_Wifi_EnableMenuConnectionType
      if (value != null) {
          String[] values = getResources().getStringArray(R.array.wifi_connect_values);
          String[] summaries = getResources().getStringArray(R.array.wifi_connect_entries);
          for (int i = 0; i < values.length; i++) {
              if (value.equals(values[i])) {
                  if (i < summaries.length) {
                      wifiConnectType.setSummary(summaries[i]);
                      return;
                  }
              }
          }
      }

      wifiConnectType.setSummary("");
      Log.e(TAG, "Invalid cmcc connect type value: " + value);
    }

    private void updateFrequencyBandSummary(Preference frequencyBandPref, int index) {
        String[] summaries = getResources().getStringArray(R.array.wifi_frequency_band_entries);
        frequencyBandPref.setSummary(summaries[index]);
    }

    public void onManageNetworkMenuPressed() {
        Bundle args = new Bundle();
        args.putBoolean(MANAGE_NETWORK, true);

        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).startPreferencePanel(
                    WifiSettings.class.getCanonicalName(),
                    args, R.string.wifi_manage_network, null, this, 0);
        } else {
            Log.e(TAG, "else");
            startFragment(this,
                    WifiSettings.class.getCanonicalName(),
                    R.string.wifi_manage_network, -1, args);
        }
    }

    private void startPoorNetworkDetectionPressed() {
        if (mPoorNetworkDetectionAggressiveMode == null 
            || !mPoorNetworkDetectionAggressiveMode.isEnabled()) {
            Log.i(TAG, "Can't start fragment, SNS disabled");
            return;
        }
        int titleResId = R.string.wifi_watchdog_connectivity_check;
        if(SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_ADAPTIVE_WIFI") && 
                !"Smart Network Switch".equals(SemCscFeature.getInstance().getString(CscFeature_Wifi_ConfigAutoWifiNaming))){
            titleResId = R.string.wifi_watchdog_connectivity_check_adaptive;
        }
        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).startPreferencePanel(
                    SmartNetworkSwitchSettings.class.getCanonicalName(),
                    null, titleResId, null, this, 0);
        } else {
            startFragment(this,
                    SmartNetworkSwitchSettings.class.getCanonicalName(),
                    titleResId, -1, null);
        }
    }

    private void startSecureWifi() {
        mSecureWifiManager.insertSALog(mSecureWifiManager.getAdvancedSettingsScreenId(), mSecureWifiManager.getAdvancedSettingsSwifiEventId());
        Intent intent = new Intent(ACTION_SECURE_WIFI);
        intent.setPackage(SECURE_WIFI_PACKAGE);

        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, 0);
        if (resolveInfo != null && resolveInfo.activityInfo.isEnabled()) {
            startActivity(intent);
        } else {
            Log.e(TAG, "Can't start Secure Wi-Fi");
        }
    }

    private void flushPreviousBigDataLogs() {
        if (mLastShownDialogKey != null) {
            if (KEY_SLEEP_POLICY.equals(mLastShownDialogKey)) {
                WifiBigDataUtil.getInstance(mContext).insertLogForUx(R.integer.keep_wifi_on_during_sleep_cancel_button);
            }
            mLastShownDialogKey = null;
        }
    }

    @Override
    // SEC_START : Preference widget
    //public boolean onPreferenceTreeClick(Preference preference) {
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    // SEC_END : Preference widget
        flushPreviousBigDataLogs();

        String key = preference.getKey();
        if (KEY_MANAGE_NETWORK.equals(key)) {
            onManageNetworkMenuPressed();
        } else if (KEY_CONTROL_HISTORY.equals(key)) {
            startControlHistory();
        } else if (KEY_POOR_NETWORK_DETECTION_AGGRESSIVE_MODE.equals(key)) {
            startPoorNetworkDetectionPressed();
        } else if (KEY_SECURE_WIFI.equals(key)) {
            startSecureWifi();
        } else {
            if (KEY_SLEEP_POLICY.equals(key)) {
                WifiBigDataUtil.getInstance(mContext).insertLogForUx(R.integer.keep_wifi_on_during_sleep);
                mLastShownDialogKey = key;
            } else if (KEY_INSTALL_CREDENTIALS.equals(key)) {
                WifiBigDataUtil.getInstance(mContext).insertLogForUx(R.integer.wifi_install_network_certificates);
            }
            // SEC_START : Preference widget
            //return super.onPreferenceTreeClick(preference);
            return super.onPreferenceTreeClick(preferenceScreen, preference);
            // SEC_END : Preference widget
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (KEY_SHOW_WIFI_POPUP.equals(key)) {
                Settings.System.putInt(getContentResolver(), Settings.System.WIFI_OFFLOAD_NETWORK_NOTIFY,
                    preferences.getBoolean(key, false) ? 1 : 0);
        }  else if (mIsComcastWifiSupported && KEY_AUTOCONNECT_XFINITY_WIFI.equals(key)) {
            sendBroadcastToNetwiseClient(preferences.getBoolean(key, false) ? NWD_ACTION_RESUME_CM : NWD_ACTION_SHUTDOWN_CM);
        } else if (KEY_NOTIFY_OPEN_NETWORKS.equals(key)) { // Auto network switch starts
            Global.putInt(getContentResolver(),
                    Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    preferences.getBoolean(key, false) ? 1 : 0);
        } else if (KEY_HS20_ENABLE.equals(key)) {  // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
            boolean isChecked = preferences.getBoolean(key, false);
            Log.d(TAG, "User triggered KEY_HS20_ENABLE: new value - "+ isChecked);
            if (isChecked) {
                enableHotspot20();
            } else {
                changeHotspot20(isChecked);

                WifiBigDataUtil.getInstance(mContext).insertLog(
                            WifiBigDataUtil.FEATURE_PASSPOINT, "USER_OFF");
            }
        } else if (KEY_WLAN_PERMISSION_AVAILABLE.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.WLAN_PERMISSION_AVAILABLE,
                    preferences.getBoolean(key, false) ? 1 : 0);
        } else if (KEY_SCAN_FOR_FREE_WLNAS.equals(key)) {
            boolean isChecked = preferences.getBoolean(key, false);
            Secure.putInt(getContentResolver(),Secure.WIFI_SCAN_FOR_FREE_WLNAS, isChecked ? 1 : 0);

            Intent intent = new Intent();
            intent.setAction("com.samsung.android.net.wifi.WECHAT_ENABLED_SCANNING");
            intent.putExtra("enable", isChecked);
            mContext.sendBroadcast(intent);
        } else if (KEY_POOR_NETWORK_DETECTION.equals(key)) { // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
            boolean isChecked = preferences.getBoolean(key, false);
            Log.d(TAG, " onSharedPreferenceChanged SNS enabled : "+ isChecked);
            Global.putInt(getContentResolver(),
                    Settings.Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, isChecked ? 1 : 0);
            //sendGSIMdata("SNSU", isChecked ? "ON" : "OFF"); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getActivity();
        String key = preference.getKey();
        mLastShownDialogKey = null;
        
        if (KEY_SHOW_WIFI_POPUP.equals(key)) {
            boolean isChecked = (Boolean)newValue;
            Settings.System.putInt(getContentResolver(), Settings.System.WIFI_OFFLOAD_NETWORK_NOTIFY,
                isChecked ? 1 : 0);
        } else if (mIsComcastWifiSupported && KEY_AUTOCONNECT_XFINITY_WIFI.equals(key)) {
            boolean isAutoConnectChecked = (Boolean)newValue;
            sendBroadcastToNetwiseClient(isAutoConnectChecked ? NWD_ACTION_RESUME_CM : NWD_ACTION_SHUTDOWN_CM);
        } else if (KEY_NOTIFY_OPEN_NETWORKS.equals(key)) {
            String stringValue = newValue.toString();
            int intValue1  = 0;
            if (stringValue.equals("false")) {
                intValue1 = 0;
            } else {
                intValue1 = 1;
            }
        Settings.Global.putInt(getContentResolver(),
                    Settings.Global.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,intValue1);
        } else if (KEY_US_ATT_AUTO_CONNECT.equals(key)) {
            boolean isChecked = (Boolean)newValue;
            Message msg = new Message();
            msg.what = WifiManager.SEC_COMMAND_ID_AUTO_CONNECT;

            Bundle args = new Bundle();
            args.putBoolean("enable", isChecked);
            msg.obj = args;
            if (mWifiManager.callSECApi(msg) == 0) {
                Secure.putInt(getContentResolver(), Secure.WIFI_AUTO_CONNECT, isChecked? 1 : 0);
            }
        } else if (KEY_WIFI_ADPS.equals(key)) { //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
            boolean isChecked = (Boolean)newValue;
            Log.d(TAG, "onPreferenceChange WIFI_ADPS : " + isChecked);
            Secure.putInt(getContentResolver(), Secure.WIFI_ADPS, isChecked? 1 : 0);
        }

        if (KEY_FREQUENCY_BAND.equals(key)) {
            try {
                int value = Integer.parseInt((String) newValue);
                mWifiManager.setFrequencyBand(value, true);
                updateFrequencyBandSummary(preference, value);
            } catch (NumberFormatException e) {
                Toast.makeText(context, R.string.wifi_setting_frequency_band_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (KEY_WIFI_ASSISTANT.equals(key)) {
                        NetworkScorerAppData wifiAssistant =
                    NetworkScorerAppManager.getScorer(context, (String) newValue);
            if (wifiAssistant == null) {
                mNetworkScoreManager.setActiveScorer(null);
                return true;
            }

            Intent intent = new Intent();
            if (wifiAssistant.mConfigurationActivityClassName != null) {
                // App has a custom configuration activity; launch that.
                // This custom activity will be responsible for launching the system
                // dialog.
                intent.setClassName(wifiAssistant.mPackageName,
                        wifiAssistant.mConfigurationActivityClassName);
            } else {
                // Fall back on the system dialog.
                intent.setAction(NetworkScoreManager.ACTION_CHANGE_ACTIVE);
                intent.putExtra(NetworkScoreManager.EXTRA_PACKAGE_NAME,
                        wifiAssistant.mPackageName);
            }

            startActivity(intent);
            // Don't update the preference widget state until the child activity returns.
            // It will be updated in onResume after the activity finishes.
            return false;
        } else if (KEY_HS20_ENABLE.equals(key)) {  // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
            boolean isChecked = (Boolean)newValue;
            if (isChecked) {
                changeHotspot20(true);
                WifiBigDataUtil.getInstance(mContext).insertLog(
                        WifiBigDataUtil.FEATURE_PASSPOINT, "USER_ON");
            } else {
                changeHotspot20(false);
                WifiBigDataUtil.getInstance(mContext).insertLog(
                            WifiBigDataUtil.FEATURE_PASSPOINT, "USER_OFF");
            }
        }

        if (KEY_SLEEP_POLICY.equals(key)) {
            try {
                String stringValue = (String) newValue;
                int intValue = Integer.parseInt(stringValue);
                Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY, intValue);
                updateSleepPolicySummary(preference, stringValue);

                WifiBigDataUtil.getInstance(mContext).insertLogForUx(R.integer.keep_wifi_on_during_sleep_button, intValue);
            } catch (NumberFormatException e) {
                Toast.makeText(context, R.string.wifi_setting_sleep_policy_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (KEY_WIFI_CONNECTION_TYPE.equals(key)) { //CscFeature_Wifi_EnableMenuConnectionType
            try {
                String stringValue = (String) newValue;
                Settings.System.putInt(getContentResolver(), Settings.System.WIFI_CONNECTION_TYPE, Integer.parseInt(stringValue));
                updatecmccConnectTypeSummary(preference, stringValue);
                Message msg = new Message();
                msg.what = WifiManager.SEC_COMMAND_ID_WIFI_CONNECTION_TYPE;
                Bundle args = new Bundle();
                args.putBoolean("enable", Integer.parseInt(stringValue) != 0? true : false);
                msg.obj = args;
                Log.i(TAG, "KEY_WIFI_CONNECTION_TYPE onPreferenceChange connectionType :  " + Integer.parseInt(stringValue));
                if (mWifiManager.callSECApi(msg) == 0) {
                    Settings.Secure.putInt(getContentResolver(), Settings.Secure.WIFI_CONNECTION_TYPE, Integer.parseInt(stringValue));
                }
                if (0 == Integer.parseInt(stringValue)) {
                    Toast.makeText(mContext, R.string.wlan_open_sucurity_toast, Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(mContext, R.string.wifi_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (KEY_SCAN_FOR_FREE_WLNAS.equals(key)) {
            boolean isChecked = (Boolean) newValue;
            Secure.putInt(getContentResolver(),Secure.WIFI_SCAN_FOR_FREE_WLNAS, isChecked ? 1 : 0);

            Intent intent = new Intent();
            intent.setAction("com.samsung.android.net.wifi.WECHAT_ENABLED_SCANNING");
            intent.putExtra("enable", isChecked);
            mContext.sendBroadcast(intent);
        } else if (KEY_WLAN_PERMISSION_AVAILABLE.equals(key)) {
            boolean isChecked = (Boolean)newValue;
            Secure.putInt(getContentResolver(), Secure.WLAN_PERMISSION_AVAILABLE, isChecked ? 1 : 0);
        }

        return true;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case WPS_PBC_DIALOG_ID:
                WifiBigDataUtil.getInstance(mContext).insertLogForUx(R.integer.wifi_wps_push);
                mWpsDialog = new WpsDialog(getActivity(), WpsInfo.PBC);
                return mWpsDialog;
            case WPS_PIN_DIALOG_ID:
                WifiBigDataUtil.getInstance(mContext).insertLogForUx(R.integer.wifi_wps_pin_entry);
                mWpsDialog = new WpsDialog(getActivity(), WpsInfo.DISPLAY);
                return mWpsDialog;
        }
        return super.onCreateDialog(dialogId);
    }

    private void refreshWifiInfo() {
        final Context context = getActivity();
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        Preference wifiMacIpAddressPref = findPreference(KEY_MAC_IP_ADDRESS);

        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        String ipAddress = Utils.getWifiIpAddresses(context);

        String finalSummary = null;

        if (Utils.isRTL(mContext)) {
            String macSummary = context.getString(R.string.wifi_advanced_mac_address_title)+"\u200F"+" : "+(!TextUtils.isEmpty(macAddress) ? macAddress : context.getString(R.string.status_unavailable));
            String ipSummary = context.getString(R.string.wifi_advanced_ip_address_title)+"\u200F"+" : "+(ipAddress == null ?
                    context.getString(R.string.wifi_no_network_connection) : ipAddress);
            finalSummary = macSummary+"\n"+ipSummary;
        } else {
            String macSummary = context.getString(R.string.wifi_advanced_mac_address_title)+" : "+(!TextUtils.isEmpty(macAddress) ? macAddress
                    : context.getString(R.string.status_unavailable));
            String ipSummary = context.getString(R.string.wifi_advanced_ip_address_title)+" : "+(ipAddress == null ?
                    context.getString(R.string.wifi_no_network_connection) : ipAddress);
            finalSummary = macSummary+"\n"+ipSummary;
        }

        wifiMacIpAddressPref.setSummary(finalSummary);

        //wifiIpAddressPref.setSelectable(false);

        if (mEnableHs20 != null) {
            mEnableHs20.setChecked(Settings.Secure.getInt(context.getContentResolver(),
                    Settings.Secure.WIFI_HOTSPOT20_ENABLE, 0) == 1);
        }
    }

    private void showWaringControlDialog() {
/*
        Intent startIntent = new Intent(mContext, CMCCWaringDialogControl.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(startIntent);
*/
    }
    private void updateSmartNetworkSwitchVisible() {
        if (mPoorNetworkDetection != null && mPoorNetworkDetectionAggressiveMode != null) {
            getPreferenceScreen().removePreference(mPoorNetworkDetection);
            getPreferenceScreen().removePreference(mPoorNetworkDetectionAggressiveMode);
            if (Utils.locateSmartNetworkSwitch(mContext) == Utils.WIFI_SMART_NETWORK_SWITCH_ON_ADVANCED_WIFI_SETTINGS) {
                if (Utils.isSupportGraceUX()) {
                    getPreferenceScreen().addPreference(mPoorNetworkDetectionAggressiveMode);

                    mPoorNetworkDetectionAggressiveMode.setChecked(Settings.Global.getInt(getContentResolver(),
                            Settings.Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, 0) == 1);
                    mPoorNetworkDetectionAggressiveMode.setEnabled(true);
                } else {
                    getPreferenceScreen().addPreference(mPoorNetworkDetection);
                    //mPoorNetworkDetection.setChecked(Settings.Global.getInt(getContentResolver(),
                    //    Settings.Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, 0) == 1);
                    mPoorNetworkDetection.setEnabled(true);
                }
            }
        }
    }

    // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
    private void changeHotspot20 (boolean enabled) {
        Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE, enabled ? 1: 0);

        Message msg = new Message();
        msg.what = WifiManager.SEC_COMMAND_ID_HS20_ENABLE;
        Bundle args = new Bundle();
        args.putBoolean("enable", enabled);
        msg.obj = args;

        if (DBG) Log.d(TAG, "changeHotspot20 : enabled "+enabled);
        if (!(mWifiManager.callSECApi(msg) == 0)) {
            Log.e(TAG, "HOTSPOT20 config store error");
            return;
        }

        if (!enabled && (mEnableHs20 != null))
            mEnableHs20.setChecked(false);
    }

    public void startControlHistory() {
         Bundle args = new Bundle();
    
         if (getActivity() instanceof SettingsActivity) {
             ((SettingsActivity) getActivity()).startPreferencePanel(
                     WifiControlHistory.class.getCanonicalName(),
                     args, R.string.wifi_control_history_title, null, this, 0);
         } else {
             Log.e(TAG, "else");
             startFragment(this,
                     WifiControlHistory.class.getCanonicalName(),
                     R.string.wifi_manage_network, -1, args);
         }
    }

    private void enableHotspot20() {
        changeHotspot20(true);
        mEnableHs20.setChecked(true);

        WifiBigDataUtil.getInstance(mContext).insertLog(
                WifiBigDataUtil.FEATURE_PASSPOINT, "USER_ON");
    }

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
            if (Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.WIFI_AP_WIFI_SHARING) == 1) {
                return true;
            } else if (Settings.Secure.getInt(getContext().getContentResolver(), Settings.Secure.WIFI_AP_WIFI_SHARING) == 0) {
                return false;
            }
        } catch (Settings.SettingNotFoundException e) {
            Log.d(TAG, "isWifiSharingEnabled, SettingNotFoundException");
        }

        return false;
    }


    private void enableHotspot20ByBixby(boolean enable) {
        if (mEnableHs20 == null) {
            mEmSettingsManager.addNlgScreenParam("Hotspot2.0AP", "TurnedOn", "no");
            mEmSettingsManager.requestNlg("WiFiAdvanced");
            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);

            return;
        }

        if (enable) {
            if (!mEnableHs20.isChecked()) {
                enableHotspot20();

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
                if (mEnableHs20.isChecked()) {
                    changeHotspot20(false);

                    WifiBigDataUtil.getInstance(mContext).insertLog(
                                WifiBigDataUtil.FEATURE_PASSPOINT, "USER_OFF");

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


    private void startManageNetworkByBixby() {
        onManageNetworkMenuPressed();

        if (mEmSettingsManager.isLastState()) {
            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "yes");
            mEmSettingsManager.requestNlg("WiFiManageNetworks");
        }

        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
    }


    private void showWpsDialogByBixby() {
        showDialog(WPS_PBC_DIALOG_ID);

        if (mEmSettingsManager.isLastState()) {
            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "yes");
            mEmSettingsManager.requestNlg("WiFiAdvanced");
        }

        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
    }


    private void setSleepPolicykByBixby(int param) {
        int value = Settings.Global.getInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
            Settings.Global.WIFI_SLEEP_POLICY_NEVER);

        if (param < 0 || param > 2) {
            mEmSettingsManager.addNlgScreenParam("SleepPolicy", "Exist", "no");
            mEmSettingsManager.requestNlg("WifiAdvanced");
            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);

            return;
        } else if (param == value) {
            String[] values = getResources().getStringArray(R.array.wifi_sleep_policy_values);
            final int summaryArrayResId = Utils.isWifiOnly(getActivity()) ?
                    R.array.wifi_sleep_policy_entries_wifi_only : R.array.wifi_sleep_policy_entries;
            String[] summaries = getResources().getStringArray(summaryArrayResId);
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

        Log.d(TAG, "mEmCallback, change the value of the sleep policy from " + value + " to "+param);
        ListPreference sleepPolicyPref = (ListPreference) findPreference(KEY_SLEEP_POLICY);
        if (sleepPolicyPref != null) {
            String newValue = String.valueOf(param);
            sleepPolicyPref.setValue(newValue);
            Settings.Global.putInt(getContentResolver(), Settings.Global.WIFI_SLEEP_POLICY,
                    Integer.parseInt(newValue));
            updateSleepPolicySummary(sleepPolicyPref, newValue);
        }

        if (mEmSettingsManager.isLastState()) {
            mEmSettingsManager.addNlgScreenParam("SleepPolicy", "AlreadySet", "no");
            mEmSettingsManager.requestNlg("WifiAdvanced");
        }

        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);      
    }

    public EmSettingsManager.IEmCallback mEmCallback = new EmSettingsManager.IEmCallback() {
        @Override
        public void onStateReceived() {
            String stateId = mEmSettingsManager.getStateId();
            Log.d(TAG, "mEmCallback, stateId: " + stateId);

            if (stateId.equals("WiFiSetSleepPolicy")) {
                if (mEmSettingsManager.getParamsSize() > 0) {
                    try {
                        int param = mEmSettingsManager.getParamInt(0);
                        Log.d(TAG, "mEmCallback, param: " + param);
                        setSleepPolicykByBixby(param);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "mEmCallback, parameter is null.");
                        mEmSettingsManager.addNlgScreenParam("SleepPolicy", "Exist", "no");
                        mEmSettingsManager.requestNlg("WiFiAdvanced");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    }
                }
            } else if (stateId.equals("WiFiManageNetworks")) {
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
            } else if (stateId.equals("WiFiWpsPushButton")) {
                if (mWifiManager.isWifiEnabled()) {
                    showWpsDialogByBixby();
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
                if (mWifiManager.isWifiEnabled()) {
                    enableHotspot20ByBixby(true);
                } else {
                    if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                        mEmSettingsManager.addNlgScreenParam("MobileHotspot", "AlreadyOn", "yes");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    } else if (mWifiManager.setWifiEnabled(true)) {
                        mWillRespondToEm = true;
                        mEmLastStateID = "WiFiSetPasspointOn";
                    } else {
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "no");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    }
                }
            } else if (stateId.equals("WiFiSetPasspointOff")) {
                 enableHotspot20ByBixby(false);
            } else if (stateId.equals("WiFiShowMAC")) {
                if (mEmSettingsManager.isLastState()) {
                    mEmSettingsManager.requestNlg("WifiAdvanced");
                }

                mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
            } else if (stateId.equals("WiFiShowIPAddress")) {
                if (mEmSettingsManager.isLastState()) {
                    mEmSettingsManager.requestNlg("WifiAdvanced");
                }

                mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
            } else if (stateId.equals("SmartNetworkSwitch")) {
                if (!mWifiManager.isWifiEnabled()) {
                    if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                        Log.d(TAG, "mEmCallback, Wi-Fi Hotspot enabled");
                        mEmSettingsManager.addNlgScreenParam("MobileHotspot", "AlreadyOn", "yes");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);    
                    } else {
                        Log.d(TAG, "mEmCallback, Wi-Fi disabled");
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOn", "no");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    }
                } else if (mPoorNetworkDetectionAggressiveMode.isEnabled()) {
                    Log.d(TAG, "mEmCallback, SNS enabled");
                    mPoorNetworkDetectionAggressiveMode.performClick(getPreferenceScreen());
                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                } else {
                    Log.d(TAG, "mEmCallback, SNS disabled");
                    mEmSettingsManager.addNlgScreenParam("MobileNetwork", "Available", "No");
                    mEmSettingsManager.requestNlg("WiFiAdvanced");
                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                }
            }
        }
    };
    // Bixby
}
