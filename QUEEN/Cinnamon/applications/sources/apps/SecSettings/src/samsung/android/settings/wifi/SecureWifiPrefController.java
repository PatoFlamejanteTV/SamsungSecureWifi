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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.sec.android.app.CscFeatureTagWifi;
import com.samsung.android.knox.SemPersonaManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.os.UserManager;
/**
 * {@link PreferenceController} Secure Wi-Fi
 */
public class SecureWifiPrefController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {
    private static final String TAG = "SecureWiFi";
    private static final String KEY_SECURE_WIFI = "secure_wifi";
    private static final String ACTION_SECURE_WIFI = "com.samsung.android.fast.ACTION_SECURE_WIFI";
    private static final String SECURE_WIFI_PACKAGE = "com.samsung.android.fast";
    private static boolean DBG = android.os.Debug.semIsProductDev();
    private final IntentFilter mFilter;
    private Preference mSecureWifi;
    private Intent mIntent;
    private boolean mIsAvailable = true;
    private final WifiManager mWifiManager;
    private SecureWifiManager mSecureWifiManager;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState(mSecureWifi);
        }
    };

    public SecureWifiPrefController(Context context, Lifecycle lifecycle, WifiManager wifiManager) {
        super(context);
        mSecureWifiManager = new SecureWifiManager(context);
        mWifiManager = wifiManager;
        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    public boolean isAvailable() {
        if(!isUserAdmin()) {
            return false;
        }
        return mIsAvailable;
    }

    @Override
    public void onResume() {
        mContext.registerReceiver(mReceiver, mFilter);
        updateState(mSecureWifi);
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
        mSecureWifi = screen.findPreference(KEY_SECURE_WIFI);
        mIntent = new Intent(ACTION_SECURE_WIFI);
        mIntent.setPackage(SECURE_WIFI_PACKAGE);
        boolean SecureWiFiInstalled = isPackageInstalled("com.samsung.android.fast", mContext.getPackageManager());
        if (SemPersonaManager.isDoEnabled(UserHandle.myUserId()) && !SecureWiFiInstalled && isSecureWifiPackage(mContext)) {
                if (mSecureWifi != null) {
                    screen.removePreference(mSecureWifi);
                }
                mIsAvailable = false;
        } else {
            if ((SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTSECUREWIFI)
                    || Utils.isSupportedCountryForEurOnSecureWiFi())
                    && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_SECURE_WIFI")
                    && isSingleUser()) {
                setSecureWifiSummary();
                if (mSecureWifi != null) {
                    mSecureWifi.setIntent(mIntent);
                }
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
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(mIntent, 0);
		if (mSecureWifi != null && KEY_SECURE_WIFI.equals(preference.getKey())) {
			if (resolveInfo != null) {
				if (resolveInfo.activityInfo.isEnabled()) {
					mSecureWifiManager.insertSALog(mSecureWifiManager.getAdvancedSettingsScreenId(), mSecureWifiManager.getAdvancedSettingsSwifiEventId());
					mContext.startActivity(mIntent);
					return true;
				} else {
					Log.d(TAG, "Can't start Secure Wi-Fi by admin");
				}
			} else {
				Log.d(TAG, "Can't start Secure Wi-Fi");
			}
		}
		return false;
	}
		 
 	private boolean isPackageInstalled(String packageName, PackageManager packageManager) { 
 		boolean found = true; 
 		try { 
 			packageManager.getPackageInfo(packageName, 0); 
 		} catch (PackageManager.NameNotFoundException e) { 
 			found = false; 
 		} 
 		return found; 
 	}
	private boolean isSingleUser() {
 		UserManager userManager = UserManager.get(mContext);
 		int userCount = userManager.getUserCount();		
 		Log.e(TAG,"isSingleUser: userCount= " + Integer.toString(userCount));		
 		return userCount == 1		
 			|| (UserManager.isSplitSystemUser() && userCount == 2);		
	}
	
	private boolean isSecureWifiPackage(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if(packageManager.checkSignatures("android", SECURE_WIFI_PACKAGE) == PackageManager.SIGNATURE_MATCH) {
            return true;
        }
        Log.e(TAG,"Secure Wi-Fi signature mismatched");
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
                    setSummaryColorToColorPrimaryDark(false);
                    mSecureWifi.setSummary(mContext.getString(R.string.secure_wifi_summary));
                } else {
                    setSummaryColorToColorPrimaryDark(true);
                    mSecureWifi.setSummary(mContext.getString(R.string.secure_wifi_summary_insecure));
                }
            } else {
                setSummaryColorToColorPrimaryDark(false);
                mSecureWifi.setSummary(mContext.getString(R.string.secure_wifi_summary));
            }
        }
    }

    // Refer to applySummaryColorPrimaryDark, applySummaryColorSecondary methods in SecPreferenceUtils.java
    private void setSummaryColorToColorPrimaryDark(boolean isPrimaryDarkColor) {
        int summaryColor = 0;
        android.content.res.ColorStateList colorStateList = null;
        if (isPrimaryDarkColor) {
            int colorPrimaryDark = mContext.getResources().getColor(R.color.sec_widget_color_primary, mContext.getTheme());
            summaryColor = getColorWithAlpha(colorPrimaryDark, mSecureWifi.isEnabled() ? 1.0f : 0.4f);
        } else {
            android.util.TypedValue outValue = new android.util.TypedValue();
            mContext.getTheme().resolveAttribute(android.R.attr.textColorSecondary, outValue, true);
            if (outValue.resourceId > 0) {
                colorStateList = mContext.getResources().getColorStateList(outValue.resourceId);
            }
        }
        if (colorStateList != null) {
            int[] colorState = {-android.R.attr.state_enabled};
            if (mSecureWifi.isEnabled()) {
                colorState = new int[]{android.R.attr.state_enabled};
            }
            summaryColor = colorStateList.getColorForState(colorState, colorStateList.getDefaultColor());
        }
        if (summaryColor != 0) {
            mSecureWifi.seslSetSummaryColor(summaryColor);
        }
    }

    private int getColorWithAlpha(int color, float ratio) {
        int alpha = Math.round(android.graphics.Color.alpha(color) * ratio);
        int r = android.graphics.Color.red(color);
        int g = android.graphics.Color.green(color);
        int b = android.graphics.Color.blue(color);
        return android.graphics.Color.argb(alpha, r, g, b);
    }
}
