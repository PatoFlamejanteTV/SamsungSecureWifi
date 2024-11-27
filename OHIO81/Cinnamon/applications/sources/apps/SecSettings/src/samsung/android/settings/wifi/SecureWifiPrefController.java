/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.samsung.android.settings.wifi;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SecPreference;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.samsung.android.settings.SAUtils;

import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.TelephonyIntents;

import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.android.app.CscFeatureTagWifi;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

/**
 * {@link PreferenceController} Secure Wi-Fi
 */
public class SecureWifiPrefController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {
    private static final String TAG = "SecureWiFi";
    private static final String KEY_SECURE_WIFI = "secure_wifi";
    private static final String ACTION_SECURE_WIFI = "com.samsung.android.fast.ACTION_SECURE_WIFI";
    private static final String SECURE_WIFI_PACKAGE = "com.samsung.android.fast";
    private static boolean DBG = android.os.Debug.semIsProductDev();
    private final boolean mInSetupWizard;
    private final IntentFilter mFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
    private SecPreference mSecureWifi;
    private Intent mIntent;
    private boolean mIsAvailable = true;
    private final WifiManager mWifiManager;
    private SecureWifiManager mSecureWifiManager;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            togglePreferences();
        }
    };

    public SecureWifiPrefController(Context context, boolean inSetupWizard, Lifecycle lifecycle, WifiManager wifiManager) {
        super(context);
        mInSetupWizard = inSetupWizard;
        mSecureWifiManager = new SecureWifiManager(context);
        mWifiManager = wifiManager;
        mFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        lifecycle.addObserver(this);
    }

    @Override
    public boolean isAvailable() {
        return mIsAvailable;
    }

    @Override
    public void onResume() {
        mContext.registerReceiver(mReceiver, mFilter);
        togglePreferences();
    }

    @Override
    public void onPause() {
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SECURE_WIFI;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mSecureWifi = (SecPreference) screen.findPreference(KEY_SECURE_WIFI);
        mIntent = new Intent(ACTION_SECURE_WIFI);
        mIntent.setPackage(SECURE_WIFI_PACKAGE);
        if (mInSetupWizard) {
            if (mSecureWifi != null) {
                screen.removePreference(mSecureWifi);
            }
            mIsAvailable = false;
        } else {
            setSecureWifiSummary();
            if ((SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTSECUREWIFI) && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_SECURE_WIFI"))) {
                mSecureWifi.setIntent(mIntent);
                mIsAvailable = true;
            } else {
                if (mSecureWifi != null) {
                    screen.removePreference(mSecureWifi);
                }
                mIsAvailable = false;
            }

        }
    }

    @Override
    public void updateState(Preference preference) {
        if (mSecureWifi != null) {
            setSecureWifiSummary();
            togglePreferences();
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(mIntent, 0);
        if (mSecureWifi != null && KEY_SECURE_WIFI.equals(preference.getKey())) {
            if (resolveInfo != null && resolveInfo.activityInfo.isEnabled()) {
                mSecureWifiManager.insertSALog(mSecureWifiManager.getAdvancedSettingsScreenId(), mSecureWifiManager.getAdvancedSettingsSwifiEventId());
                mContext.startActivity(mIntent);
                return true;
            } else {
                Log.d(TAG, "Can't start Secure Wi-Fi");
                return false;
            }
        }
        return false;
    }

    private void togglePreferences() {
        if (mSecureWifi != null) {
            if(mWifiManager.isWifiEnabled()) {
                mSecureWifi.setEnabled(true);

            } else {
                TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE); 
                if (tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT){
                    mSecureWifi.setEnabled(false);
                } 
                else {
                    mSecureWifi.setEnabled(true); 
                }
            }
        }
    }

    private void setSecureWifiSummary() {
        String secureWifiMode = mSecureWifiManager.getSecureWifiMode();
        String secureWifiToC = mSecureWifiManager.getSecureWifiTermsAndConditionsAgreed();
        if (mSecureWifi != null) {
            if ("true".equals(secureWifiToC)) {
                if ("0".equals(secureWifiMode)) {
                    mSecureWifi.semSetSummaryColorToColorPrimaryDark(false);
                    mSecureWifi.setSummary(mContext.getString(R.string.secure_wifi_summary));
                } else {
                    mSecureWifi.semSetSummaryColorToColorPrimaryDark(true);
                    mSecureWifi.setSummary(mContext.getString(R.string.secure_wifi_summary_insecure));
                }
            } else {
                mSecureWifi.semSetSummaryColorToColorPrimaryDark(false);
                mSecureWifi.setSummary(mContext.getString(R.string.secure_wifi_summary));
            }
        }
    }
}
