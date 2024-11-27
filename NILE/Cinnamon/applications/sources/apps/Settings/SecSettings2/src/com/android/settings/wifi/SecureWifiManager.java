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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import com.samsung.context.sdk.samsunganalytics.Configuration;
import com.samsung.context.sdk.samsunganalytics.LogBuilders;
import com.samsung.context.sdk.samsunganalytics.SamsungAnalytics;

public class SecureWifiManager {
    private Context mContext;

    private static final String AUTHORITY = "com.samsung.android.fast.securewifiprovider";
    private static final String PATH_PREFERENCE = "preference";
    private static final Uri PREFERENCE_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_PREFERENCE);
    private static final String ADVANCED_SETTINGS_SCREEN_ID = "SWIFI001";
    private static final String ADVANCED_SETTINGS_SWIFI_EVENT_ID = "0010";

    private static final class Preference {
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_KEY = "key";
        private static final String COLUMN_VALUE = "value";

        private static final String KEY_SECURE_WIFI_MODE_INDEX = "secure_wifi_mode";
        private static final String KEY_TERMS_AND_CONDITIONS_AGREED = "terms_and_conditions_agreed";
    }

    public SecureWifiManager(Context context) {
        mContext = context;
    }

    public String getSecureWifiMode() {
        return getPrefValue(Preference.KEY_SECURE_WIFI_MODE_INDEX);
    }
	
    public String getSecureWifiTermsAndConditionsAgreed() {
        return getPrefValue(Preference.KEY_TERMS_AND_CONDITIONS_AGREED);
    }

    public static void insertSALog(final String screenID, final String eventID) {
        SamsungAnalytics.getInstance().sendLog(new LogBuilders.EventBuilder()
                .setScreenView(screenID)
                .setEventName(eventID)
                .build());
    }

    public String getAdvancedSettingsScreenId() {
        return ADVANCED_SETTINGS_SCREEN_ID;
    }

    public String getAdvancedSettingsSwifiEventId() {
        return ADVANCED_SETTINGS_SWIFI_EVENT_ID;
    }

    private String getPrefValue(String key) {
        String value = null;
        Cursor c = mContext.getContentResolver().query(
                Uri.parse(PREFERENCE_URI.toString() + "/" + key),
                null, null, null, null);
        if (c != null) {
            if (c.moveToNext()) {
                value = c.getString(c.getColumnIndex(Preference.COLUMN_VALUE));
            }
            c.close();
        }
        return value;
    }

    private boolean getPrefBoolean(String key, boolean defaultValue){
        String stringValue = getPrefValue(key);
        if (stringValue == null){
            return defaultValue;
        }
        return Boolean.parseBoolean(stringValue);
    }
}

