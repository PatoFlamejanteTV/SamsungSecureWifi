/**
 * Copyright (C) 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.android.settings;

import static android.content.Intent.EXTRA_USER;
import static android.content.Intent.EXTRA_USER_ID;
import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;

import android.annotation.Nullable;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.hardware.face.FaceManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.preference.PreferenceFrameLayout;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings;
import android.sec.enterprise.content.SecContentProviderURI;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.TtsSpan;
import android.util.ArraySet;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import com.android.internal.app.UnlaunchableAppActivity;
import com.android.internal.util.ArrayUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.core.FeatureFlags;
import com.android.settings.development.featureflags.FeatureFlagPersistent;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.widget.ActionBarShadowController;

import com.samsung.android.desktopmode.SemDesktopModeManager;
import com.samsung.android.desktopmode.SemDesktopModeState;
import com.samsung.android.emergencymode.SemEmergencyManager;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.samsung.android.knox.container.KnoxContainerManager;
import com.samsung.android.knox.ContextInfo;
import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.EnterpriseKnoxManager;
import com.samsung.android.knox.SemPersonaManager;
import com.samsung.android.net.wifi.OpBrandingLoader;
import com.samsung.android.settings.connection.SecSimFeatureProvider;
import com.samsung.android.settings.widget.IconResizer;
import com.samsung.android.settings.Rune;
import com.samsung.android.settings.csc.CscParser;
import com.sec.android.app.CscFeatureTagSetting;
import com.sec.android.app.CscFeatureTagCommon;
import com.sec.android.app.CscFeatureTagWifi;
import com.sec.android.app.SecProductFeature_KNOX;
import com.sec.android.app.SecProductFeature_WLAN;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

// SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
import android.sec.enterprise.content.SecContentProviderURI;
// SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }

public final class Utils extends com.android.settingslib.Utils {

    public static final boolean MHSDBG = ("eng".equals(android.os.Build.TYPE) ||(android.os.Debug.semIsProductDev()));
    public static final int DEFAULT_TIMEOUT_MOBILEAP = SemCscFeature.getInstance().getInteger(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGMOBILEAPDEFAULTTIMEOUT, 1200);
    public static boolean USED_BIXBY;
    public static int USED_BIXBY_REASON;
    public static int MAX_CLIENT_4_MOBILEAP = SemCscFeature.getInstance().getInteger(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_MAXCLIENT4MOBILEAP, 10);
    public static boolean SUPPORT_MOBILEAP_WIFISHARING = false;
    public static boolean SUPPORT_MOBILEAP_5G = SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTMOBILEAP5G,false);
    public static String CONFIGOPBRANDINGFORMOBILEAP = SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGOPBRANDINGFORMOBILEAP, "ALL");
    private static final String TAG = "Settings_Utils";
    /**
     * Settings app-wide debug level:
     *   0 - no debug logging
     *   1 - normal debug logging if ro.debuggable is set (which is true in
     *       "eng" and "userdebug" builds but not "user" builds)
     *   2 - ultra-verbose debug logging
     *
     * Most individual classes in the phone app have a local DBG constant,
     * typically set to
     *   (Utils.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1)
     * or else
     *   (Utils.DBG_LEVEL >= 2)
     * depending on the desired verbosity.
     *
     */

    public static final int DBG_LEVEL = 2;

    public static final boolean DBG =
            (Utils.DBG_LEVEL >= 1) && (SystemProperties.getInt("ro.debuggable", 0) == 1);
    private static final Uri DEX_SETTINGS_URI = Uri.parse("content://com.sec.android.desktopmode.uiservice.SettingsProvider/");

    public static String mDeviceType;
    public static final int SIMSLOT1 = 0;
    public static final int SIMSLOT2 = 1;

    private static final String HELPHUB_PACKAGE_NAME = "com.samsung.helphub";
    /**
     * Set the preference's title to the matching activity's label.
     */
    public static final int UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY = 1;

    public static final String CONFIG_VENDOR_SSID_LIST = SemCscFeature.getInstance().getString(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGVENDORSSIDLIST);
    public static final boolean REMOVABLE_DEFAULT_AP = //SEC_PRODUCT_FEATURE_WLAN_USE_DEFAULT_AP
            OpBrandingLoader.getInstance().isSupportRemovableDefaultAp();
    public static final String CONFIG_OP_BRANDING = SemCscFeature.getInstance().getString(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGOPBRANDING);
    public static final boolean COMMON_SUPPORTCOMCASTWIFI = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTCOMCASTWIFI);  // CCT
    public static final boolean SUPPORT_AUTO_RECONNECT = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTAUTORECONNECT);
    public static final boolean ENABLE_WIFI_CONNECTION_TYPE = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_ENABLEMENUCONNECTIONTYPE); //CMCC
    public static final String CONFIG_SOCIAL_SVC_INTEGRATION = SemCscFeature.getInstance().getString(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGSOCIALSVCINTEGRATIONN); //CHN
    public static final String CONFIG_SECURE_SVC_INTEGRATION = SemCscFeature.getInstance().getString(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGSECURESVCINTEGRATION); //CHN
    public static final String CONFIG_LOCAL_SECURITY_POLICY = SemCscFeature.getInstance().getString(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_CONFIGLOCALSECURITYPOLICY);   //CHN
    public static final boolean SUPPORT_NOTIFICATION_MENU = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTNOTIFICATIONMENU); //USA
    public static final boolean SHOW_DETAILED_AP_INFO = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SHOWDETAILEDAPINFO); //CMCC
    public static final boolean SPRINT_EXTENSIONS = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_ENABLESPRINTEXTENSION);
    public static final String SETTINGS_PACKAGE_NAME = "com.android.settings";

    public static final String OS_PKG = "os";

    /**
     * Color spectrum to use to indicate badness.  0 is completely transparent (no data),
     * 1 is most bad (red), the last value is least bad (green).
     */
    public static final int[] BADNESS_COLORS = new int[] {
            0x00000000, 0xffc43828, 0xffe54918, 0xfff47b00,
            0xfffabf2c, 0xff679e37, 0xff0a7f42
    };

    /**
     * Whether to disable the new device identifier access restrictions.
     */
    public static final String PROPERTY_DEVICE_IDENTIFIER_ACCESS_RESTRICTIONS_DISABLED =
            "device_identifier_access_restrictions_disabled";

    /**
     * Whether to show the Permissions Hub.
     */
    public static final String PROPERTY_PERMISSIONS_HUB_ENABLED = "permissions_hub_enabled";

    public static boolean SPF_SupportMobileApEnhanced = SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ENHANCED_MOBILEAP;
    public static final boolean SPF_SupportMobileApDataLimit = SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MOBILEAP_DATA_LIMIT;
    public static boolean SUPPORT_MOBILEAP_WIFISHARINGLITE = false;
    public static final boolean SUPPORT_MOBILEAP_MAXCLIENT_MENU = SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTMENUMOBILEAPMAXCLIENT,false);
    public static boolean SUPPORT_MOBILEAP_5G_BASED_ON_COUNTRY = SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTMOBILEAP5GBASEDONCOUNTRY,false);
    public static final boolean ENABLE_SHOW_PASSWORD_AS_DEFAULT = SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_ENABLESHOWPASSWORDASDEFAULT);
    private static String SUPPORT_MOBILEAP_REGION="";

    /**
     * Finds a matching activity for a preference's intent. If a matching
     * activity is not found, it will remove the preference.
     *
     * @param context The context.
     * @param parentPreferenceGroup The preference group that contains the
     *            preference whose intent is being resolved.
     * @param preferenceKey The key of the preference whose intent is being
     *            resolved.
     * @param flags 0 or one or more of
     *            {@link #UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY}
     *            .
     * @return Whether an activity was found. If false, the preference was
     *         removed.
     */
    public static boolean updatePreferenceToSpecificActivityOrRemove(Context context,
            PreferenceGroup parentPreferenceGroup, String preferenceKey, int flags) {

        Preference preference = parentPreferenceGroup.findPreference(preferenceKey);
        if (preference == null) {
            return false;
        }

        Intent intent = preference.getIntent();
        if (intent != null) {
            // Find the activity that is in the system image
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
            int listSize = list.size();
            for (int i = 0; i < listSize; i++) {
                ResolveInfo resolveInfo = list.get(i);
                if ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                        != 0) {

                    // Replace the intent with this specific activity
                    preference.setIntent(new Intent().setClassName(
                            resolveInfo.activityInfo.packageName,
                            resolveInfo.activityInfo.name));

                    if ((flags & UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY) != 0) {
                        // Set the preference title to the activity's label
                        preference.setTitle(resolveInfo.loadLabel(pm));
                    }

                    return true;
                }
            }
        }

        // Did not find a matching activity, so remove the preference
        parentPreferenceGroup.removePreference(preference);

        return false;
    }

    /**
     * Returns the UserManager for a given context
     *
     * @throws IllegalStateException if no UserManager could be retrieved.
     */
    public static UserManager getUserManager(Context context) {
        UserManager um = UserManager.get(context);
        if (um == null) {
            throw new IllegalStateException("Unable to load UserManager");
        }
        return um;
    }

    /**
     * Returns true if Monkey is running.
     */
    public static boolean isMonkeyRunning() {
        return ActivityManager.isUserAMonkey();
    }

    /**
     * Returns whether the device is voice-capable (meaning, it is also a phone).
     */
    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony != null && telephony.isVoiceCapable();
    }

    /**
     * Returns the WIFI IP Addresses, if any, taking into account IPv4 and IPv6 style addresses.
     * @param context the application context
     * @return the formatted and newline-separated IP addresses, or null if none.
     */
    public static String getWifiIpAddresses(Context context) {
        WifiManager wifiManager = context.getSystemService(WifiManager.class);
        Network currentNetwork = wifiManager.getCurrentNetwork();
        if (currentNetwork != null) {
            ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
            LinkProperties prop = cm.getLinkProperties(currentNetwork);
            return formatIpAddresses(prop);
        }
        return null;
    }

    private static String formatIpAddresses(LinkProperties prop) {
        if (prop == null) return null;
        Iterator<InetAddress> iter = prop.getAllAddresses().iterator();
        // If there are no entries, return null
        if (!iter.hasNext()) return null;
        // Concatenate all available addresses, comma separated
        String addresses = "";
        while (iter.hasNext()) {
            addresses += iter.next().getHostAddress();
            if (iter.hasNext()) addresses += "\n";
        }
        return addresses;
    }

    public static Locale createLocaleFromString(String localeStr) {
        // TODO: is there a better way to actually construct a locale that will match?
        // The main problem is, on top of Java specs, locale.toString() and
        // new Locale(locale.toString()).toString() do not return equal() strings in
        // many cases, because the constructor takes the only string as the language
        // code. So : new Locale("en", "US").toString() => "en_US"
        // And : new Locale("en_US").toString() => "en_us"
        if (null == localeStr)
            return Locale.getDefault();
        String[] brokenDownLocale = localeStr.split("_", 3);
        // split may not return a 0-length array.
        if (1 == brokenDownLocale.length) {
            return new Locale(brokenDownLocale[0]);
        } else if (2 == brokenDownLocale.length) {
            return new Locale(brokenDownLocale[0], brokenDownLocale[1]);
        } else {
            return new Locale(brokenDownLocale[0], brokenDownLocale[1], brokenDownLocale[2]);
        }
    }

    public static boolean isBatteryPresent(Intent batteryChangedIntent) {
        return batteryChangedIntent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);
    }

    public static String getBatteryPercentage(Intent batteryChangedIntent) {
        return formatPercentage(getBatteryLevel(batteryChangedIntent));
    }

    /**
     * Prepare a custom preferences layout, moving padding to {@link ListView}
     * when outside scrollbars are requested. Usually used to display
     * {@link ListView} and {@link TabWidget} with correct padding.
     */
    public static void prepareCustomPreferencesList(
            ViewGroup parent, View child, View list, boolean ignoreSidePadding) {
        final boolean movePadding = list.getScrollBarStyle() == View.SCROLLBARS_OUTSIDE_OVERLAY;
        if (movePadding) {
            final Resources res = list.getResources();
            final int paddingBottom = res.getDimensionPixelSize(
                    com.android.internal.R.dimen.preference_fragment_padding_bottom);

            if (parent instanceof PreferenceFrameLayout) {
                ((PreferenceFrameLayout.LayoutParams) child.getLayoutParams()).removeBorders = true;
            }
            list.setPaddingRelative(0 /* start */, 0 /* top */, 0 /* end */, paddingBottom);
        }
    }

    public static void forceCustomPadding(View view, boolean additive) {
        final Resources res = view.getResources();

        final int paddingStart = additive ? view.getPaddingStart() : 0;
        final int paddingEnd = additive ? view.getPaddingEnd() : 0;
        final int paddingBottom = res.getDimensionPixelSize(
                com.android.internal.R.dimen.preference_fragment_padding_bottom);

        view.setPaddingRelative(paddingStart, 0, paddingEnd, paddingBottom);
    }

    // SEC_START
    public static String getMeProfileName(Context context, boolean full, UserInfo user) {
        if (full) {
            return getProfileDisplayName(context, user);
        } else {
            return getShorterNameIfPossible(context, user);
        }
    }

    private static String getShorterNameIfPossible(Context context, UserInfo user) {
        final String given = getLocalProfileGivenName(context);
        return !TextUtils.isEmpty(given) ? given : getProfileDisplayName(context, user);
    }

    private static String getLocalProfileGivenName(Context context) {
        return null;

        /*
            Following codes do not work properly.
            And I think there is no problem this function returns null.
            (If anyone who knows the purpose of this function, fix this.)

        final ContentResolver cr = context.getContentResolver();

        // Find the raw contact ID for the local ME profile raw contact.
        final long localRowProfileId;
        final Cursor localRawProfile = cr.query(
                Profile.CONTENT_RAW_CONTACTS_URI,
                new String[] {RawContacts._ID},
                RawContacts.ACCOUNT_TYPE + " IS NULL AND " +
                        RawContacts.ACCOUNT_NAME + " IS NULL",
                null, null);
        if (localRawProfile == null) return null;

        try {
            if (!localRawProfile.moveToFirst()) {
                return null;
            }
            localRowProfileId = localRawProfile.getLong(0);
        } finally {
            localRawProfile.close();
        }

        // Find the structured name for the raw contact.
        final Cursor structuredName = cr.query(
                Profile.CONTENT_URI.buildUpon().appendPath(Contacts.Data.CONTENT_DIRECTORY).build(),
                new String[] {CommonDataKinds.StructuredName.GIVEN_NAME,
                    CommonDataKinds.StructuredName.FAMILY_NAME},
                Data.RAW_CONTACT_ID + "=" + localRowProfileId,
                null, null);
        if (structuredName == null) return null;

        try {
            if (!structuredName.moveToFirst()) {
                return null;
            }
            String partialName = structuredName.getString(0);
            if (TextUtils.isEmpty(partialName)) {
                partialName = structuredName.getString(1);
            }
            return partialName;
        } finally {
            structuredName.close();
        }
        */
    }

    private static final String getProfileDisplayName(Context context, UserInfo user) {
        final ContentResolver cr = context.getContentResolver();
        final Cursor profile = cr.query(Profile.CONTENT_URI,
                new String[] {Profile.DISPLAY_NAME}, null, null, null);
        if (profile == null) return null;

        try {
            if (!profile.moveToFirst()) {
                return null;
            }
            UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
            int userId = user != null ? user.id : UserHandle.myUserId();

            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String phoneNumber = manager.getLine1Number();
            if( phoneNumber != null ) {
                if(!phoneNumber.equals(profile.getString(0))) {
                    um.setUserName(userId, profile.getString(0));
                } else {
                    return null;
                }
            } else {
                um.setUserName(userId, profile.getString(0));
            }

            return profile.getString(0);
        } finally {
            profile.close();
        }
    }
    // SEC_END

    public static boolean hasMultipleUsers(Context context) {
        return ((UserManager) context.getSystemService(Context.USER_SERVICE))
                .getUsers().size() > 1;
    }

    /**
     * Returns the managed profile of the current user or {@code null} if none is found or a profile
     * exists but it is disabled.
     */
    public static UserHandle getManagedProfile(UserManager userManager) {
        List<UserHandle> userProfiles = userManager.getUserProfiles();
        for (UserHandle profile : userProfiles) {
            if (profile.getIdentifier() == userManager.getUserHandle()) {
                continue;
            }
            final UserInfo userInfo = userManager.getUserInfo(profile.getIdentifier());
            if (userInfo.isManagedProfile()) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Returns the managed profile of the current user or {@code null} if none is found. Unlike
     * {@link #getManagedProfile} this method returns enabled and disabled managed profiles.
     */
    public static UserHandle getManagedProfileWithDisabled(UserManager userManager) {
        // TODO: Call getManagedProfileId from here once Robolectric supports
        // API level 24 and UserManager.getProfileIdsWithDisabled can be Mocked (to avoid having
        // yet another implementation that loops over user profiles in this method). In the meantime
        // we need to use UserManager.getProfiles that is available on API 23 (the one currently
        // used for Settings Robolectric tests).
        final int myUserId = UserHandle.myUserId();
        List<UserInfo> profiles = userManager.getProfiles(myUserId);
        final int count = profiles.size();
        for (int i = 0; i < count; i++) {
            final UserInfo profile = profiles.get(i);
            if (profile.isManagedProfile()
                    && profile.getUserHandle().getIdentifier() != myUserId) {
                return profile.getUserHandle();
            }
        }
        return null;
    }

    /**
     * Retrieves the id for the given user's managed profile.
     *
     * @return the managed profile id or UserHandle.USER_NULL if there is none.
     */
    public static int getManagedProfileId(UserManager um, int parentUserId) {
        int[] profileIds = um.getProfileIdsWithDisabled(parentUserId);
        for (int profileId : profileIds) {
            if (profileId != parentUserId) {
                return profileId;
            }
        }
        return UserHandle.USER_NULL;
    }

    /**
     * Returns the target user for a Settings activity.
     * <p>
     * User would be retrieved in this order:
     * <ul>
     * <li> If this activity is launched from other user, return that user id.
     * <li> If this is launched from the Settings app in same user, return the user contained as an
     *      extra in the arguments or intent extras.
     * <li> Otherwise, return UserHandle.myUserId().
     * </ul>
     * <p>
     * Note: This is secure in the sense that it only returns a target user different to the current
     * one if the app launching this activity is the Settings app itself, running in the same user
     * or in one that is in the same profile group, or if the user id is provided by the system.
     */
    public static UserHandle getSecureTargetUser(IBinder activityToken,
            UserManager um, @Nullable Bundle arguments, @Nullable Bundle intentExtras) {
        UserHandle currentUser = new UserHandle(UserHandle.myUserId());
        IActivityManager am = ActivityManager.getService();
        try {
            String launchedFromPackage = am.getLaunchedFromPackage(activityToken);
            boolean launchedFromSettingsApp = SETTINGS_PACKAGE_NAME.equals(launchedFromPackage);

            UserHandle launchedFromUser = new UserHandle(UserHandle.getUserId(
                    am.getLaunchedFromUid(activityToken)));
            if (launchedFromUser != null && !launchedFromUser.equals(currentUser)) {
                // Check it's secure
                if (isProfileOf(um, launchedFromUser)) {
                    return launchedFromUser;
                }
            }
            UserHandle extrasUser = getUserHandleFromBundle(intentExtras);
            if (extrasUser != null && !extrasUser.equals(currentUser)) {
                // Check it's secure
                if (launchedFromSettingsApp && isProfileOf(um, extrasUser)) {
                    return extrasUser;
                }
            }
            UserHandle argumentsUser = getUserHandleFromBundle(arguments);
            if (argumentsUser != null && !argumentsUser.equals(currentUser)) {
                // Check it's secure
                if (launchedFromSettingsApp && isProfileOf(um, argumentsUser)) {
                    return argumentsUser;
                }
            }
        } catch (RemoteException e) {
            // Should not happen
            Log.v(TAG, "Could not talk to activity manager.", e);
        }
        return currentUser;
    }

    /**
     * Lookup both {@link Intent#EXTRA_USER} and {@link Intent#EXTRA_USER_ID} in the bundle
     * and return the {@link UserHandle} object. Return {@code null} if nothing is found.
     */
    private static @Nullable UserHandle getUserHandleFromBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        final UserHandle user = bundle.getParcelable(EXTRA_USER);
        if (user != null) {
            return user;
        }
        final int userId = bundle.getInt(EXTRA_USER_ID, -1);
        if (userId != -1) {
            return UserHandle.of(userId);
        }
        return null;
    }

   /**
    * Returns true if the user provided is in the same profiles group as the current user.
    */
   private static boolean isProfileOf(UserManager um, UserHandle otherUser) {
       if (um == null || otherUser == null) return false;
       return (UserHandle.myUserId() == otherUser.getIdentifier())
               || um.getUserProfiles().contains(otherUser);
   }

    /**
     * Return whether or not the user should have a SIM Cards option in Settings.
     * TODO: Change back to returning true if count is greater than one after testing.
     * TODO: See bug 16533525.
     */
    public static boolean showSimCardTile(Context context) {
        if (FeatureFlagPersistent.isEnabled(context, FeatureFlags.NETWORK_INTERNET_V2)) {
            return false;
        }
        final TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        return tm.getSimCount() > 1;
    }

    /**
     * Queries for the UserInfo of a user. Returns null if the user doesn't exist (was removed).
     * @param userManager Instance of UserManager
     * @param checkUser The user to check the existence of.
     * @return UserInfo of the user or null for non-existent user.
     */
    public static UserInfo getExistingUser(UserManager userManager, UserHandle checkUser) {
        final List<UserInfo> users = userManager.getUsers(true /* excludeDying */);
        final int checkUserId = checkUser.getIdentifier();
        for (UserInfo user : users) {
            if (user.id == checkUserId) {
                return user;
            }
        }
        return null;
    }

    public static View inflateCategoryHeader(LayoutInflater inflater, ViewGroup parent) {
        /* final TypedArray a = inflater.getContext().obtainStyledAttributes(null,
                com.android.internal.R.styleable.Preference,
                com.android.internal.R.attr.preferenceCategoryStyle, 0);
        final int resId = a.getResourceId(com.android.internal.R.styleable.Preference_layout,
                0);
        a.recycle();*/
        return inflater.inflate(R.layout.sec_subheader_divider_layout, parent, false);
    }

    public static ArraySet<String> getHandledDomains(PackageManager pm, String packageName) {
        List<IntentFilterVerificationInfo> iviList = pm.getIntentFilterVerifications(packageName);
        List<IntentFilter> filters = pm.getAllIntentFilters(packageName);

        ArraySet<String> result = new ArraySet<>();
        if (iviList != null && iviList.size() > 0) {
            for (IntentFilterVerificationInfo ivi : iviList) {
                for (String host : ivi.getDomains()) {
                    result.add(host);
                }
            }
        }
        if (filters != null && filters.size() > 0) {
            for (IntentFilter filter : filters) {
                if (filter.hasCategory(Intent.CATEGORY_BROWSABLE)
                        && (filter.hasDataScheme(IntentFilter.SCHEME_HTTP) ||
                                filter.hasDataScheme(IntentFilter.SCHEME_HTTPS))) {
                    result.addAll(filter.getHostsList());
                }
            }
        }
        return result;
    }

    /**
     * Returns the application info of the currently installed MDM package.
     */
    public static ApplicationInfo getAdminApplicationInfo(Context context, int profileId) {
        DevicePolicyManager dpm =
                (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName mdmPackage = dpm.getProfileOwnerAsUser(profileId);
        if (mdmPackage == null) {
            return null;
        }
        String mdmPackageName = mdmPackage.getPackageName();
        try {
            IPackageManager ipm = AppGlobals.getPackageManager();
            ApplicationInfo mdmApplicationInfo =
                    ipm.getApplicationInfo(mdmPackageName, 0, profileId);
            return mdmApplicationInfo;
        } catch (RemoteException e) {
            Log.e(TAG, "Error while retrieving application info for package " + mdmPackageName
                    + ", userId " + profileId, e);
            return null;
        }
    }

    public static boolean isBandwidthControlEnabled() {
        final INetworkManagementService netManager = INetworkManagementService.Stub
                .asInterface(ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        try {
            return netManager.isBandwidthControlEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Returns an accessible SpannableString.
     * @param displayText the text to display
     * @param accessibileText the text text-to-speech engines should read
     */
    public static SpannableString createAccessibleSequence(CharSequence displayText,
            String accessibileText) {
        SpannableString str = new SpannableString(displayText);
        str.setSpan(new TtsSpan.TextBuilder(accessibileText).build(), 0,
                displayText.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return str;
    }

    /**
     * Returns the user id present in the bundle with
     * {@link Intent#EXTRA_USER_ID} if it belongs to the current user.
     *
     * @throws SecurityException if the given userId does not belong to the
     *             current user group.
     */
    public static int getUserIdFromBundle(Context context, Bundle bundle) {
        return getUserIdFromBundle(context, bundle, false);
    }

    /**
     * Returns the user id present in the bundle with
     * {@link Intent#EXTRA_USER_ID} if it belongs to the current user.
     *
     * @param isInternal indicating if the caller is "internal" to the system,
     *            meaning we're willing to trust extras like
     *            {@link ChooseLockSettingsHelper#EXTRA_ALLOW_ANY_USER}.
     * @throws SecurityException if the given userId does not belong to the
     *             current user group.
     */
    public static int getUserIdFromBundle(Context context, Bundle bundle, boolean isInternal) {
        if (bundle == null) {
            return getCredentialOwnerUserId(context);
        }
        final boolean allowAnyUser = isInternal
                && bundle.getBoolean(ChooseLockSettingsHelper.EXTRA_ALLOW_ANY_USER, false);
        int userId = bundle.getInt(Intent.EXTRA_USER_ID, UserHandle.myUserId());
        if (userId == LockPatternUtils.USER_FRP) {
            return allowAnyUser ? userId : enforceSystemUser(context, userId);
        } else {
            return allowAnyUser ? userId : enforceSameOwner(context, userId);
        }
    }

    /**
     * Returns the given user id if the current user is the system user.
     *
     * @throws SecurityException if the current user is not the system user.
     */
    public static int enforceSystemUser(Context context, int userId) {
        if (UserHandle.myUserId() == UserHandle.USER_SYSTEM) {
            return userId;
        }
        throw new SecurityException("Given user id " + userId + " must only be used from "
                + "USER_SYSTEM, but current user is " + UserHandle.myUserId());
    }

    /**
     * Returns the given user id if it belongs to the current user.
     *
     * @throws SecurityException if the given userId does not belong to the current user group.
     */
    public static int enforceSameOwner(Context context, int userId) {
        final UserManager um = getUserManager(context);
        final int[] profileIds = um.getProfileIdsWithDisabled(UserHandle.myUserId());
        if (ArrayUtils.contains(profileIds, userId)) {
            return userId;
        }
        throw new SecurityException("Given user id " + userId + " does not belong to user "
                + UserHandle.myUserId());
    }

    /**
     * Returns the effective credential owner of the calling user.
     */
    public static int getCredentialOwnerUserId(Context context) {
        return getCredentialOwnerUserId(context, UserHandle.myUserId());
    }

    /**
     * Returns the user id of the credential owner of the given user id.
     */
    public static int getCredentialOwnerUserId(Context context, int userId) {
        UserManager um = getUserManager(context);
        return um.getCredentialOwnerProfile(userId);
    }

    private static final StringBuilder sBuilder = new StringBuilder(50);
    private static final java.util.Formatter sFormatter = new java.util.Formatter(
            sBuilder, Locale.getDefault());

    public static String formatDateRange(Context context, long start, long end) {
        final int flags = FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH;

        synchronized (sBuilder) {
            sBuilder.setLength(0);
            return DateUtils.formatDateRange(context, sFormatter, start, end, flags, null)
                    .toString();
        }
    }

    public static boolean isDeviceProvisioned(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED, 0) != 0;
    }

    public static boolean startQuietModeDialogIfNecessary(Context context, UserManager um,
            int userId) {
        if (um.isQuietModeEnabled(UserHandle.of(userId))) {
            final Intent intent = UnlaunchableAppActivity.createInQuietModeDialogIntent(userId);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    public static boolean unlockWorkProfileIfNecessary(Context context, int userId) {
        try {
            if (!ActivityManager.getService().isUserRunning(userId,
                    ActivityManager.FLAG_AND_LOCKED)) {
                return false;
            }
        } catch (RemoteException e) {
            return false;
        }
        if (!(new LockPatternUtils(context)).isSecure(userId)) {
            return false;
        }
        return confirmWorkProfileCredentials(context, userId);
    }

    private static boolean confirmWorkProfileCredentials(Context context, int userId) {
        final KeyguardManager km = (KeyguardManager) context.getSystemService(
                Context.KEYGUARD_SERVICE);
        final Intent unlockIntent = km.createConfirmDeviceCredentialIntent(null, null, userId);
        if (unlockIntent != null) {
            context.startActivity(unlockIntent);
            return true;
        } else {
            return false;
        }
    }

    public static CharSequence getApplicationLabel(Context context, String packageName) {
        try {
            final ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    packageName,
                    PackageManager.MATCH_DISABLED_COMPONENTS
                    | PackageManager.MATCH_ANY_USER);
            return appInfo.loadLabel(context.getPackageManager());
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Unable to find info for package: " + packageName);
        }
        return null;
    }

    public static boolean isPackageDirectBootAware(Context context, String packageName) {
        try {
            final ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    packageName, 0);
            return ai.isDirectBootAware() || ai.isPartiallyDirectBootAware();
        } catch (NameNotFoundException ignored) {
        }
        return false;
    }

    /**
     * Returns a context created from the given context for the given user, or null if it fails
     */
    public static Context createPackageContextAsUser(Context context, int userId) {
        try {
            return context.createPackageContextAsUser(
                    context.getPackageName(), 0 /* flags */, UserHandle.of(userId));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to create user context", e);
        }
        return null;
    }

    public static FingerprintManager getFingerprintManagerOrNull(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        } else {
            return null;
        }
    }

    public static boolean hasFingerprintHardware(Context context) {
        FingerprintManager fingerprintManager = getFingerprintManagerOrNull(context);
        return fingerprintManager != null && fingerprintManager.isHardwareDetected();
    }

    public static FaceManager getFaceManagerOrNull(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FACE)) {
            return (FaceManager) context.getSystemService(Context.FACE_SERVICE);
        } else {
            return null;
        }
    }

    public static boolean hasFaceHardware(Context context) {
        FaceManager faceManager = getFaceManagerOrNull(context);
        return faceManager != null && faceManager.isHardwareDetected();
    }

    /**
     * Launches an intent which may optionally have a user id defined.
     * @param fragment Fragment to use to launch the activity.
     * @param intent Intent to launch.
     */
    public static void launchIntent(Fragment fragment, Intent intent) {
        try {
            final int userId = intent.getIntExtra(Intent.EXTRA_USER_ID, -1);

            if (userId == -1) {
                fragment.startActivity(intent);
            } else {
                fragment.getActivity().startActivityAsUser(intent, new UserHandle(userId));
            }
        } catch (ActivityNotFoundException e) {
            Log.w(TAG, "No activity found for " + intent);
        }
    }

    public static boolean isDemoUser(Context context) {
        return UserManager.isDeviceInDemoMode(context) && getUserManager(context).isDemoUser();
    }

    public static ComponentName getDeviceOwnerComponent(Context context) {
        final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        return dpm.getDeviceOwnerComponentOnAnyUser();
    }

    /**
     * Returns if a given user is a profile of another user.
     * @param user The user whose profiles wibe checked.
     * @param profile The (potential) profile.
     * @return if the profile is actually a profile
     */
    public static boolean isProfileOf(UserInfo user, UserInfo profile) {
        return user.id == profile.id ||
                (user.profileGroupId != UserInfo.NO_PROFILE_GROUP_ID
                        && user.profileGroupId == profile.profileGroupId);
    }

    /**
     * Tries to initalize a volume with the given bundle. If it is a valid, private, and readable
     * {@link VolumeInfo}, it is returned. If it is not valid, null is returned.
     */
    @Nullable
    public static VolumeInfo maybeInitializeVolume(StorageManager sm, Bundle bundle) {
        final String volumeId = bundle.getString(VolumeInfo.EXTRA_VOLUME_ID,
                VolumeInfo.ID_PRIVATE_INTERNAL);
        VolumeInfo volume = sm.findVolumeById(volumeId);
        return isVolumeValid(volume) ? volume : null;
    }

    /**
     * Return {@code true} if the supplied package is device owner or profile owner of at
     * least one user.
     * @param userManager used to get profile owner app for each user
     * @param devicePolicyManager used to check whether it is device owner app
     * @param packageName package to check about
     */
    public static boolean isProfileOrDeviceOwner(UserManager userManager,
            DevicePolicyManager devicePolicyManager, String packageName) {
        List<UserInfo> userInfos = userManager.getUsers();
        if (devicePolicyManager.isDeviceOwnerAppOnAnyUser(packageName)) {
            return true;
        }
        for (int i = 0, size = userInfos.size(); i < size; i++) {
            ComponentName cn = devicePolicyManager.getProfileOwnerAsUser(userInfos.get(i).id);
            if (cn != null && cn.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the resource id to represent the install status for an app
     */
    @StringRes
    public static int getInstallationStatus(PackageManager pm, ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
            return R.string.not_installed;
        } else if (AppUtils.isAutoDisabled(pm, info)) {
            return R.string.sec_auto_disabled;
        }
        return info.enabled ? R.string.installed : R.string.disabled;
    }

    private static boolean isVolumeValid(VolumeInfo volume) {
        return (volume != null) && (volume.getType() == VolumeInfo.TYPE_PRIVATE)
                && volume.isMountedReadable();
    }

    public static void setEditTextCursorPosition(EditText editText) {
        editText.setSelection(editText.getText().length());
    }

    /**
     * Sets the preference icon with a drawable that is scaled down to to avoid crashing Settings if
     * it's too big.
     */
    public static void setSafeIcon(Preference pref, Drawable icon) {
        Drawable safeIcon = icon;
        if ((icon != null) && !(icon instanceof VectorDrawable)) {
            safeIcon = getSafeDrawable(icon, 500, 500);
        }
        pref.setIcon(safeIcon);
    }

    /**
     * Gets a drawable with a limited size to avoid crashing Settings if it's too big.
     *
     * @param original original drawable, typically an app icon.
     * @param maxWidth maximum width, in pixels.
     * @param maxHeight maximum height, in pixels.
     */
    public static Drawable getSafeDrawable(Drawable original, int maxWidth, int maxHeight) {
        final int actualWidth = original.getMinimumWidth();
        final int actualHeight = original.getMinimumHeight();

        if (actualWidth <= maxWidth && actualHeight <= maxHeight) {
            return original;
        }

        float scaleWidth = ((float) maxWidth) / actualWidth;
        float scaleHeight = ((float) maxHeight) / actualHeight;
        float scale = Math.min(scaleWidth, scaleHeight);
        final int width = (int) (actualWidth * scale);
        final int height = (int) (actualHeight * scale);

        final Bitmap bitmap;
        if (original instanceof BitmapDrawable) {
            bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) original).getBitmap(), width,
                    height, false);
        } else {
            bitmap = createBitmap(original, width, height);
        }
        return new BitmapDrawable(null, bitmap);
    }

    /**
     * Create an Icon pointing to a drawable.
     */
    public static IconCompat createIconWithDrawable(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable)drawable).getBitmap();
        } else {
            final int width = drawable.getIntrinsicWidth();
            final int height = drawable.getIntrinsicHeight();
            bitmap = createBitmap(drawable,
                    width > 0 ? width : 1,
                    height > 0 ? height : 1);
        }
        return IconCompat.createWithBitmap(bitmap);
    }

    /**
     * Creates a drawable with specified width and height.
     */
    public static Bitmap createBitmap(Drawable drawable, int width, int height) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Get the {@link Drawable} that represents the app icon
     */
    public static Drawable getBadgedIcon(IconDrawableFactory iconDrawableFactory,
            PackageManager packageManager, String packageName, int userId) {
        try {
            final ApplicationInfo appInfo = packageManager.getApplicationInfoAsUser(
                    packageName, PackageManager.GET_META_DATA, userId);
            return iconDrawableFactory.getBadgedIcon(appInfo, userId);
        } catch (PackageManager.NameNotFoundException e) {
            return packageManager.getDefaultActivityIcon();
        }
    }

    /** Returns true if the current package is installed & enabled. */
    public static boolean isPackageEnabled(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, 0).enabled;
        } catch (Exception e) {
            Log.e(TAG, "Error while retrieving application info for package " + packageName, e);
        }
        return false;
    }

    /** Get {@link Resources} by subscription id if subscription id is valid. */
    public static Resources getResourcesForSubId(Context context, int subId) {
        if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            return SubscriptionManager.getResourcesForSubId(context, subId);
        } else {
            return context.getResources();
        }
    }

    /**
     * Returns true if SYSTEM_ALERT_WINDOW permission is available.
     * Starting from Q, SYSTEM_ALERT_WINDOW is disabled on low ram phones.
     */
    public static boolean isSystemAlertWindowEnabled(Context context) {
        // SYSTEM_ALERT_WINDOW is disabled on on low ram devices starting from Q
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return !(am.isLowRamDevice() && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q));
    }

    /**
     * Adds a shadow appear/disappear animation to action bar scroll.
     *
     * <p/>
     * This method must be called after {@link Fragment#onCreate(Bundle)}.
     */
    public static void setActionBarShadowAnimation(Activity activity, Lifecycle lifecycle,
            View scrollView) {
        if (activity == null) {
            Log.w(TAG, "No activity, cannot style actionbar.");
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar == null) {
            Log.w(TAG, "No actionbar, cannot style actionbar.");
            return;
        }
        actionBar.setElevation(0);

        if (lifecycle != null && scrollView != null) {
            ActionBarShadowController.attachToView(activity, lifecycle, scrollView);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // SEC_START
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void applyLandscapeFullScreen(Context context, Window window) {

        if (context == null) {
            return;
        }

        Activity activity = (Activity) context;
        int orientation = context.getResources().getConfiguration().orientation;

        if (!SystemProperties.get("ro.build.characteristics", "phone").contains("tablet")) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE && !activity.isInMultiWindowMode()) {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                lp.semAddExtensionFlags(2/*WindowManagerRef.LayoutParams.SAMSUNG_FLAG_ENABLE_STATUSBAR_OPEN_BY_NOTIFICATION*/);
                lp.semAddExtensionFlags(WindowManager.LayoutParams.SEM_EXTENSION_FLAG_RESIZE_FULLSCREEN_WINDOW_ON_SOFT_INPUT);
                window.setAttributes(lp);
            } else {
                WindowManager.LayoutParams lp = window.getAttributes();
                lp.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                lp.semAddExtensionFlags(WindowManager.LayoutParams.SEM_EXTENSION_FLAG_RESIZE_FULLSCREEN_WINDOW_ON_SOFT_INPUT);
                window.setAttributes(lp);
            }
        }
    }

    /**
     * Returns whether maximum power saving mode is enabled or not.
     *
     * @param context
     * @return if true, maximum power saving mode enabled. Otherwise, false.
     */
    public static boolean isMaximumPowerSavingModeEnabled(Context context) {
        SemEmergencyManager em = SemEmergencyManager.getInstance(context);
        return em != null && em.isEmergencyMode(context);
    }

    /**
     * @param context
     * @return
     */
    public static boolean isRTL(Context context) {
        int layout_dir = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_LAYOUTDIR_MASK;
        return (layout_dir == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL);
    }

    /**
     * Is the default locale right-to-left?
     *
     * @return true if the locale is Arabic, Farsi, Hebrew, Urdu, or Yiddish.
     */
    public static boolean isLocaleRTL() {
        return isLocaleRTL(Locale.getDefault());
    }

    /**
     * Is the locale right-to-left?
     *
     * @param locale the locale.
     * @return true if the locale is Arabic, Farsi, Hebrew, Urdu, or Yiddish.
     */
    public static boolean isLocaleRTL(Locale locale) {
        /** ISO 639 language code for "Arabic". */
        String ISO639_ARABIC = "ar";
        /** ISO 639 language code for "Farsi/Persian". */
        String ISO639_PERSIAN = "fa";
        /** ISO 639 language code for "Hebrew". */
        String ISO639_HEBREW = "he";
        /** ISO 639 language code for "Hebrew" - obsolete. */
        @Deprecated
        String ISO639_HEBREW_FORMER = "iw";
        /** ISO 639 language code for "Yiddish" - obsolete. */
        String ISO639_YIDDISH_FORMER = "ji";
        /** ISO 639 language code for "Urdu". */
        String ISO639_URDU = "ur";
        /** ISO 639 language code for "Yiddish". */
        String ISO639_YIDDISH = "yi";
        String iso639 = locale.getLanguage();
        return ISO639_ARABIC.equals(iso639) || ISO639_PERSIAN.equals(iso639)
                || ISO639_HEBREW.equals(iso639) || ISO639_URDU.equals(iso639)
                || ISO639_YIDDISH.equals(iso639)
                || ISO639_HEBREW_FORMER.equals(iso639)
                || ISO639_YIDDISH_FORMER.equals(iso639);
    }

    /**
     * Check whether the given package is installed on the device or not
     *
     * @return true if the given package is installed, false otherwise
     */
    public static boolean hasPackage(Context c, String pkg) {
        if (c == null) {
            return false;
        }
        PackageManager pm = c.getPackageManager();
        boolean hasPkg = true;

        try {
            pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            hasPkg = false;
            Log.d(TAG, "Package not found : " + pkg);
        }

        return hasPkg;
    }

    /**
     *
     * @param context
     * @param packageName
     * @return package' version name
     **/
    public static String getPackageVersionName(Context context, String packageName) {
        PackageInfo pkgInfo = null;
        if (context == null) {
            return null;
        }
        PackageManager pm = context.getPackageManager();
        try {
            pkgInfo = pm.getPackageInfo(packageName, 0);
            if (pkgInfo == null) {
                return null;
            }
        } catch (NameNotFoundException e) {
            return null;
        }
        return pkgInfo.versionName;
    }

    /**
     * Check whether the given activity exists or not
     *
     * @param context
     * @param targetPackage
     * @param activityName
     * @return true if the given activity exists, false otherwise
     */
    public static boolean isActivityExists(Context context, String targetPackage, String activityName) {
        Intent intent = new Intent();
        intent.setClassName(targetPackage, activityName);
        if (null != context.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)) {
            return true;
        }
        return false;
    }

    /**
     * Verify the intent will resolve to at least on activity
     *
     * @param context
     * @param action
     * @return true if there is at least one activity to resolve the given intent
     */
    public static boolean isIntentAvailable(Context context, String action) {
        PackageManager packageManager = context.getPackageManager();
        if(packageManager == null) return false;
        Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (list != null && list.size() > 0);
    }

    /**
     * Verify the intent will resolve to at least on activity
     *
     * @param context
     * @param intent
     * @return true if there is at least one activity to resolve the given intent
     */
    public static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) return false;
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return (list != null && list.size() > 0);
    }

    // VERTICAL_ADVANCED_SETTINGS_START
    /**
     * @param userManager
     * @return
     */
    public static boolean isKnoxContainer(UserManager userManager) {
        UserInfo currentUser = userManager.getUserInfo(userManager.getUserHandle());
        return currentUser.isKnoxWorkspace();
    }
    // VERTICAL_ADVANCED_SETTINGS_END

    /**
     * @param context
     * @return
     */
    public static boolean isAfwProfile(Context context) {

        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (mDPM.getDeviceOwner() == null) {
            //Log.secE("ODE", "*** is not Device Owner");

            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            List<UserInfo> profiles = userManager.getProfiles(UserHandle.myUserId());
            for (UserInfo userInfo : profiles) {
                if (userInfo.isManagedProfile()
                        // VERTICAL_ADVANCED_SETTINGS_START
                        && !userInfo.isKnoxWorkspace()
                        // VERTICAL_ADVANCED_SETTINGS_END
                        && !userInfo.isDualAppProfile()) {
                    //Log.secE("ODE", "*** is work profile");
                    return true;
                }
            }
            //Log.secE("ODE", "*** is not work profile");
            return false;
        } else {
            //Log.secE("ODE", "*** is Device Owner");
            return true;
        }
    }

    /**
     * Returns true if CTC build.
     */
    public static boolean isChinaCTCModel() {
        String salesCode = readSalesCode();
        return ("CTC".equals(salesCode));
    }

    /**
     * @param context
     * @return
     */
    public static boolean isGuestMode(Context context) {
        UserManager mUm = (UserManager) context.getSystemService(Context.USER_SERVICE);
        UserInfo info = mUm.getUserInfo(UserHandle.myUserId());
        return info.isRestricted();
    }

    /**
     * @param context
     * @return
     */
    public static boolean isGuestUser(Context context) {
        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            UserInfo info = UserManager.get(context).getUserInfo(UserHandle.myUserId());
            if (info != null && info.isGuest()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the device is message-capable
     *
     * @param context
     */
    public static boolean isSmsCapable(Context context) {
        // Some tablet has sim card but could not do telephony operations. Skip those.
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony != null && telephony.isSmsCapable();
    }

    /**
     * Check whether Game mode is enabled or not
     *
     * @param context
     * @return true if Game mode is enabled, false otherwise
     */
    public static boolean isGameModeEnabled(Context context) {
        /*  Q_PORTING
        boolean result = Rune.supportBoostMode()
                && Settings.Secure.getInt(context.getContentResolver(), "sem_perfomance_mode", 0) == 1;
        Log.d(TAG, "isGameModeEnabled: " + result);
        return result;
         Q_PORTING  */
        return false;
    }

    /**
     * Check whether Easy mode is enabled or not
     *
     * @param context
     * @return true if Easy mode is enabled, false otherwise
     */
    public static boolean isEasyModeEnabled(Context context) {
        boolean isDesktopEnabled = Rune.supportDesktopMode() && Rune.isSamsungDexMode(context);
        if (SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_SETTINGS_SUPPORT_EASY_MODE")
                && (UserHandle.myUserId() == UserHandle.SEM_USER_OWNER)
                && !isDesktopEnabled
                && !isGameModeEnabled(context)) {
            return true;
        } else {
            Log.d(TAG, "easy mode is not displayed.(" + SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_SETTINGS_SUPPORT_EASY_MODE")
                    + "," + isGuestMode(context) + "," + isDesktopEnabled + "," + isGameModeEnabled(context) + ")");
            return false;
        }
    }

    /**
     * @param mContext
     * @return
     */
    public static boolean isSharedDeviceEnabled(Context mContext) {
        boolean ret = false;
        int sd_status = Settings.Secure.getInt(mContext.getContentResolver(), "shared_device_status", 0);
        if (sd_status == 1 || sd_status == 2)
            ret = true;
        else if (sd_status == 0)
            ret = false;
        return ret;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isDesktopConnected(Context context) {
        SemDesktopModeManager desktopModeManager = (SemDesktopModeManager) context.getSystemService(Context.SEM_DESKTOP_MODE_SERVICE);
        if (desktopModeManager != null) {
            return desktopModeManager.isDeviceConnected();
        }
        return false;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isDesktopDockConnected(Context context) {
        SemDesktopModeManager desktopModeManager = (SemDesktopModeManager) context.getSystemService(Context.SEM_DESKTOP_MODE_SERVICE);
        if (desktopModeManager != null) {
            return desktopModeManager.isDesktopDockConnected();
        }
        return false;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isDesktopModeEnabled(Context context) {
        SemDesktopModeManager desktopModeManager = (SemDesktopModeManager) context.getSystemService(Context.SEM_DESKTOP_MODE_SERVICE);
        if (desktopModeManager != null) {
            SemDesktopModeState desktopModeState = desktopModeManager.getDesktopModeState();
            if (desktopModeState.enabled == SemDesktopModeState.ENABLED)
                return true;
        }
        return false;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isDesktopDualMode(Context context) {
        SemDesktopModeManager desktopModeManager = (SemDesktopModeManager) context.getSystemService(Context.SEM_DESKTOP_MODE_SERVICE);
        if (desktopModeManager != null) {
            SemDesktopModeState desktopModeState = desktopModeManager.getDesktopModeState();
            return (desktopModeState.getDisplayType() == SemDesktopModeState.DISPLAY_TYPE_DUAL);
        } else {
            Log.d("Utils", "isDesktopDualMode : desktopModeManager is null");
        }
        return false;
    }

    /**
     * @param context
     * @return
     */
    public static boolean isDesktopStandaloneMode(Context context) {
        SemDesktopModeManager desktopModeManager = (SemDesktopModeManager) context.getSystemService(Context.SEM_DESKTOP_MODE_SERVICE);
        if (desktopModeManager != null) {
            SemDesktopModeState desktopModeState = desktopModeManager.getDesktopModeState();
            return (desktopModeState.getDisplayType() == SemDesktopModeState.DISPLAY_TYPE_STANDALONE);
        } else {
            Log.d("Utils", "isDesktopStandaloneMode : desktopModeManager is null");
        }
        return false;
    }

    public static Drawable resizeIcon(Context context, Drawable original) {
        if(context == null)
            return original;
        IconResizer iconResizer = new IconResizer(context);
        iconResizer.setIconSize(R.dimen.sec_widget_list_app_icon_size);
        Drawable icon = null;
        if(original != null){
            icon = iconResizer.createIconThumbnail(original);
        }
        return icon;
    }

    public static void setMaxFontScale(Context context, Button button){
        float MAX_FONT_SCALE = 1.2f;
        float fontScale = context.getResources().getConfiguration().fontScale;
        float fontsize = button.getTextSize() / context.getResources().getDisplayMetrics().scaledDensity;

        if (fontScale > MAX_FONT_SCALE) {
            fontScale = MAX_FONT_SCALE;
        }

        button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float)(fontsize * fontScale));
    }

    public static void setMaxFontScale(Context context, TextView textView) {
        float MAX_FONT_SCALE = 1.2f;
        float fontScale = context.getResources().getConfiguration().fontScale;
        float fontsize = textView.getTextSize() / context.getResources().getDisplayMetrics().scaledDensity;

        if (fontScale > MAX_FONT_SCALE) {
            fontScale = MAX_FONT_SCALE;
        }

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (float) (fontsize * fontScale));
    }

    /**
     * Returns the menu tree code which is stored at TAG_CSCFEATURE_SETTING_CONFIGOPMENUSTRUCTURE csc config.
     * When the TAG_CSCFEATURE_SETTING_CONFIGOPMENUSTRUCTURE csc config is not defined, it returns sales code which stored at system properties.
     *
     * @return a string which gets from TAG_CSCFEATURE_SETTING_CONFIGOPMENUSTRUCTURE
     */
    private static String mMenutreeCode;

    public static String readSalesCode() {
        if (mMenutreeCode != null && !mMenutreeCode.isEmpty()) {
            return mMenutreeCode;
        }

        mMenutreeCode = SemCscFeature.getInstance().getString(CscFeatureTagSetting.TAG_CSCFEATURE_SETTING_CONFIGOPMENUSTRUCTURE);
        if (mMenutreeCode != null && mMenutreeCode.isEmpty())
            mMenutreeCode = getSalesCode();

        return mMenutreeCode;
    }

    /**
     * Return Sales Code
     *
     * @return a string which gets from ro.csc.sales_code or ril.sales_code.
     */
    public static String getSalesCode() {
        String sales_code = "";
        try {
            sales_code = SystemProperties.get("persist.omc.sales_code");
            if (TextUtils.isEmpty(sales_code)) {
                sales_code = SystemProperties.get("ro.csc.sales_code");
                if (TextUtils.isEmpty(sales_code)) {
                    sales_code = SystemProperties.get("ril.sales_code");
                }
            }
        } catch (Exception e) {
        }
        return sales_code;
    }

    public static String mCountryCode;

    public static String readCountryCode() {
        if (mCountryCode != null && !mCountryCode.isEmpty()) {
            return mCountryCode;
        }

        mCountryCode = SemCscFeature.getInstance().getString("CountryISO");
        Log.d(TAG, "readCountryCode(): country=" + mCountryCode);
        return mCountryCode;
    }

    public static void setDeXSettings(ContentResolver resolver, final String key, final String val) {
        Bundle extras = new Bundle();
        extras.putString("key", key);
        extras.putString("val", val);

        try {
            resolver.call(DEX_SETTINGS_URI, "setSettings", null, extras);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException :: setDeXSettings " + key);
        }
    }

    public static String getStringFromDeXSettings(ContentResolver resolver, final String key, final String def) {
        Bundle extras = new Bundle();
        extras.putString("key", key);
        extras.putString("def", def);

        try {
            Bundle result = resolver.call(DEX_SETTINGS_URI, "getSettings", null, extras);
            if (result != null) {
                return result.getString(key);
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException :: getDeXSettings " + key);
        }
        return def;
    }

    public static String getComma(Context ctx) {
        String comma = ctx.getString(R.string.comma) + " ";
        return comma;
    }

    public static String getKeywordForSearch(Context context, int resId) {
        return localeTranslate(context, new Locale("en"), resId);
    }

    private static String localeTranslate(Context context, Locale locale, int resId) {
        String translatedText = "";
        try {
            Configuration config = new Configuration(context.getResources().getConfiguration());
            config.setLocale(locale);
            translatedText = context.createConfigurationContext(config).getString(resId);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "(Res error)Failed to translate : " + e.toString());
        } catch (ClassCastException e1) {
            Log.e(TAG, "(Class error)Failed to translate : " + e1.toString());
        }
        return translatedText;
    }

    public static float getContentFrameWidthRatio(Context context) {
        float widthRatio = 1.0f;

        if (context == null) {
            return widthRatio;
        }

        int screenWidthDp = context.getResources().getConfiguration().screenWidthDp;

        if (Rune.DEBUG_DASHBOARD_TILE) {
            Log.i(TAG, "getContentFrameWidthRatio screenWidthDp : " + screenWidthDp
                    + " , densityDpi : " + context.getResources().getConfiguration().densityDpi
                    + " , smallestScreenWidthDp : " + context.getResources().getConfiguration().smallestScreenWidthDp
                    + " , semDesktopModeEnabled : " + context.getResources().getConfiguration().semDesktopModeEnabled);
        }

        if (screenWidthDp >= 685 && screenWidthDp <= 959) {
            widthRatio = 0.91f;
        } else if (screenWidthDp >= 960 && screenWidthDp <= 1919) {
            widthRatio = 0.75f;
        } else if (screenWidthDp >= 1920) {
            widthRatio = 0.5f;
        }
        return widthRatio;
    }

    public static String replaceSIMString (String str) {
        if(!Rune.isChinaCTCModel()) {
            return str;
        } else if(!(TelephonyManager.getDefault().getPhoneCount() > 1)) {
            return str.replace("SIM", "UIM");
        } else {
            return str.replace("SIM", "UIM/SIM");
        }
    }

    //VERTICAL_ADVANCED_SETTINGS_START
    public static List<UserHandle> getManagedProfiles(UserManager userManager, boolean excludeKnox) {
        List<UserHandle> resultList = new ArrayList<UserHandle>();
        List<UserHandle> userProfiles = userManager.getUserProfiles();

        final int count = userProfiles.size();
        for (int i = 0; i < count; i++) {
            final UserHandle profile = userProfiles.get(i);
            if (profile.getIdentifier() == userManager.getUserHandle()) {
                continue;
            }
            final UserInfo userInfo = userManager.getUserInfo(profile.getIdentifier());
            if (userInfo.isManagedProfile()) {
                if ((excludeKnox && userInfo.isKnoxWorkspace()) || (userInfo.isDualAppProfile())) {
                    continue;
                } else {
                    resultList.add(profile);
                }
            }
        }

        return resultList;
    }

    public static boolean isContainerOnlyModeFromOwner(Context context) {
        if (context == null) {
            return false;
        }

        if (SemPersonaManager.isKioskModeEnabled(context) && UserHandle.getCallingUserId() == UserHandle.USER_OWNER) {
            return true;
        }

        return false;
    }
    //VERTICAL_ADVANCED_SETTINGS_END

    public static boolean isTalkBackEnabled(Context context) {
        boolean talkbackEnabled = false;
        String accesibilityService = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (accesibilityService != null) {
            // com.google.android.marvin.talkback
            // com.samsung.android.app.talkback
            talkbackEnabled = (accesibilityService.matches("(?i).*com.samsung.android.app.talkback.TalkBackService.*") ||
                    accesibilityService.matches("(?i).*com.google.android.marvin.talkback.TalkBackService.*"));
        }

        return talkbackEnabled;
    }
    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
    }
    /**
     * Check device type that is tablet or not
     *
     * @return true if device type is tablet
     */
    public static boolean isTablet() {
        if (mDeviceType != null && mDeviceType.length() > 0) {
            return mDeviceType.contains("tablet");
        }

        mDeviceType = SystemProperties.get("ro.build.characteristics");
        return (mDeviceType != null && mDeviceType.contains("tablet"));
    }

    public static boolean isTablet(Context context) {
        if (mDeviceType != null && mDeviceType.length() > 0) {
            return mDeviceType.contains("tablet");
        }
        mDeviceType = SystemProperties.get("ro.build.characteristics");
        return (mDeviceType != null && mDeviceType.contains("tablet"));
    }

    public static boolean hasHaptic(Context context) {
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        return vibrator != null && vibrator.hasVibrator();
    }


    public static boolean isNewMessageClientInstalled(Context mContext) {
        boolean isNewMessageClientInstalled = false;
        PackageManager pm = mContext.getPackageManager();

        if (pm == null) {
            android.util.Log.e(TAG, "isNewMessageClientInstalled : pm = null");
            return false;
        }
        try {
            PackageInfo info = pm.getPackageInfo(Rune.COMMON_CONFIG_PACKAGE_NAME_MESSAGES, 0);

            if (info != null && info.versionCode >= 500000000) {
                android.util.Log.d(TAG, "isNewMessageClientInstalled : " + info.versionCode);
                isNewMessageClientInstalled = true;
            }
        } catch (Exception ex) {
            android.util.Log.e(TAG, "isNewMessageClientInstalled : can't get package info of message");
        }

        android.util.Log.d(TAG, "isNewMessageClientInstalled : " + isNewMessageClientInstalled);

        return isNewMessageClientInstalled;
    }

    //WTL_EDM_START
    public static final int EDM_NULL = -1;
    public static final int EDM_FALSE = 0;
    public static final int EDM_TRUE = 1;

    public static int getEnterprisePolicyEnabled(Context context, String edmUri, String projectionArgs) {
        Uri uri = Uri.parse(edmUri);
        Cursor cr = context.getContentResolver().query(uri, null, projectionArgs, null, null);
        if (cr != null && SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            try {
                cr.moveToFirst();
                if (cr.getString(cr.getColumnIndex(projectionArgs)).equals("true")) {
                    return EDM_TRUE;
                } else {
                    return EDM_FALSE;
                }
            } catch(Exception e) {
            } finally {
                cr.close();
            }
        }
        return EDM_NULL;
    }

    public static int getEnterprisePolicyEnabled(Context context, String edmUri, String projectionArgs, String[] selectionArgs) {
        int result = EDM_NULL;
        Uri uri = Uri.parse(edmUri);
        Cursor cr = context.getContentResolver().query(uri, null, projectionArgs, selectionArgs, null);
        if (cr != null && SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            try {
                cr.moveToFirst();
                if (cr.getString(cr.getColumnIndex(projectionArgs)).equals("true")) {
                    result = EDM_TRUE;
                } else {
                    result = EDM_FALSE;
                }
            } catch(Exception e) {
            } finally {
                cr.close();
            }
        }
        Log.w("SettingsEdm","projectionArgs:"+projectionArgs+"/"+result);
        return result;
    }

    public static int getEnterprisePolicyEnabledInt(Context context, String edmUri, String projectionArgs, String[] selectionArgs) {
        int result = 0;
        if (projectionArgs.equals(SecContentProviderURI.PASSWORDPOLICY_PASSWORDLOCKDELAY_METHOD)) {
            result = -1;
        }
        Uri uri = Uri.parse(edmUri);
        Cursor cr = context.getContentResolver().query(uri, null, projectionArgs, selectionArgs, null);
        if (cr != null && SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            try {
                cr.moveToFirst();
                result = cr.getInt(cr.getColumnIndex(projectionArgs));
            } catch(Exception e) {
            } finally {
                cr.close();
            }
        }
        return result;
    }

    public static void setEnterprisePolicyInt(Context context, String edmUri, String projectionArgs, int value) {
        int result = 0;
        Uri uri = Uri.parse(edmUri);
        ContentValues cv = new ContentValues();
        cv.put("API", projectionArgs);
        cv.put("flag", value);
        context.getContentResolver().insert(uri, cv);
    }

    public static String getEnterprisePolicyStringValue(Context context, String edmUri, String projectionArgs, String[] selectionArgs) {
        if(SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            Uri uri = Uri.parse(edmUri);
            Cursor cr = context.getContentResolver().query(uri, null, projectionArgs, selectionArgs, null);
            if (cr != null) {
                try {
                    cr.moveToFirst();
                    return cr.getString(cr.getColumnIndex(projectionArgs));
                } catch (Exception e) {
                } finally {
                    cr.close();
                }
            }
        }
        return null;
    }

    // For Contact us - START
    public static boolean isSupportContactUs(Context context) {
        return isSupportContactUs(context, 170001000);
    }

    public static Intent getContactUsIntent(Context context) {
        String packageName = context.getPackageName(); // packageName
        String appId = "be4f156x1l"; // appId
        String appName = "Settings"; // appName
        String faqUrl = "http://www.samsung.com"; // app faq url
        String url = "voc://view/contactUs";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.putExtra("packageName", packageName);
        intent.putExtra("appId", appId);
        intent.putExtra("appName", appName);
        return intent;
    }

    public static Intent getContactUsIntent(Context context, String packageName, String appName,  String appId) {
        String faqUrl = "http://www.samsung.com"; // app faq url
        String url = "voc://view/contactUs";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.putExtra("packageName", packageName);
        intent.putExtra("appId", appId);
        intent.putExtra("appName", appName);
        return intent;
    }

    public static boolean isSupportContactUs(Context context, long versionCode) {
        final String packageName = "com.samsung.android.voc";

        if (!Utils.hasPackage(context, packageName))
            return false;

        PackageManager pm = context.getApplicationContext().getPackageManager();
        if (getContactUsIntent(context).resolveActivity(pm) == null)
            return false;

        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            if (packageInfo.versionCode < versionCode)
                return false;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        return true;
    }
    // For Contact us - END

    // SEC_START : SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
    public static boolean isCaCertificateDisabledAsUser(Context context, String alias, int userId) {
        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            int isCaCertificateDisabledAsUser = getEnterprisePolicyEnabled(context,
                    SecContentProviderURI.CERTIFICATE_URI,
                    SecContentProviderURI.CERTIFICATEPOLICY_CACERTIFICATEDISABLEDASUSER_METHOD,
                    new String[]{alias, String.valueOf(userId)});

            if (isCaCertificateDisabledAsUser == EDM_TRUE) {
                Log.secD(TAG, "CaCertificateDisabledAsUser : disabled");
                return true;
            }
        }

        return false;
    }

    public static boolean isUserRemoveCertificateAllowedAsUser(Context context, int userId) {
        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            int isUserRemoveCertificatesAllowedAsUser = getEnterprisePolicyEnabled(context,
                            SecContentProviderURI.CERTIFICATE_URI,
                            SecContentProviderURI.CERTIFICATEPOLICY_USERREMOVECERTIFICATES_METHOD,
                            new String[]{String.valueOf(userId)});

            if (isUserRemoveCertificatesAllowedAsUser == EDM_FALSE) {
                Log.secD(TAG, "UserRemoveCertificatesAllowedAsUser : not allowed");
                return false;
            }
        }

        return true;
    }

    // >>>WCM>>>
    public static final int SIM_SLOT_1 = 0;
    public static final int SIM_SLOT_2 = 1;
    public static final int WIFI_INTERNET_SERVICE_CHECK_ENABLED = 1;
    public static final int WIFI_INTERNET_SERVICE_CHECK_DISABLED_NO_SIM = 2;
    public static final int WIFI_INTERNET_SERVICE_CHECK_DISABLED_AIRPLANE_MODE = 3;
    public static final int WIFI_INTERNET_SERVICE_CHECK_DISABLED_MOBILE_DATA_DISABLED = 4;
    public static final int WIFI_INTERNET_SERVICE_CHECK_DISABLED_BLOCKED_BY_DATA_ROAMING = 5;
    public static int updateSmartNetworkSwitchAvailability(Context context) {
        if (context == null) {
            Log.e("WifiSettings/AdvancedWifiSettings/SettingsSearchUtils", "context is null.");
            return -1;
        }
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int result = WIFI_INTERNET_SERVICE_CHECK_ENABLED;

        boolean mMobilePolicyDataEnable = mConnectivityManager.semIsMobilePolicyDataEnabled();

        boolean isAirplaneMode = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        boolean isMobileDataEnabled = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.MOBILE_DATA, 1) != 0;
        int simState=0;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            simState = TelephonyManager.SIM_STATE_UNKNOWN;
            Log.e("Utils", "TelephonyManager is null.");
        //SEC_PRODUCT_FEATURE_COMMON_DUOS_SUPPORT
        } else if ((TelephonyManager.getDefault().getPhoneCount() > 1)) {
            int simState1 = telephonyManager.getSimState(SIM_SLOT_1);
            int simState2 = telephonyManager.getSimState(SIM_SLOT_2);
            if (simState1 == TelephonyManager.SIM_STATE_READY
                    || simState2 == TelephonyManager.SIM_STATE_READY) {
                simState = TelephonyManager.SIM_STATE_READY;
            } else {
                simState = TelephonyManager.SIM_STATE_UNKNOWN;
            }
            if (DBG) Log.d("WifiSettings/AdvancedWifiSettings/SettingsSearchUtils",
                    "simState1 = " + simState1 + ", simState2 = " + simState2 + ", simState = " + simState);
        } else {        //SEC_PRODUCT_FEATURE_COMMON_DUOS_SUPPORT
            simState = telephonyManager.getSimState();
        }

        boolean mobileDataBlockedByRoaming = false;
        if (telephonyManager != null) {
            ServiceState ss = telephonyManager.getServiceState();
            if (ss != null) {
                mobileDataBlockedByRoaming = ss.getDataRoaming();
            }
        }

        if (simState != TelephonyManager.SIM_STATE_READY &&
                /* !SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SIMCHECK_DISABLE && */
                !(DBG && SystemProperties.get("SimCheck.disable").equals("1"))) {
            result = WIFI_INTERNET_SERVICE_CHECK_DISABLED_NO_SIM;
        } else if (isAirplaneMode) {
            result = WIFI_INTERNET_SERVICE_CHECK_DISABLED_AIRPLANE_MODE;
        } else if (!isMobileDataEnabled || !mMobilePolicyDataEnable) {
            result = WIFI_INTERNET_SERVICE_CHECK_DISABLED_MOBILE_DATA_DISABLED;
        } else if (mobileDataBlockedByRoaming) {
            result = WIFI_INTERNET_SERVICE_CHECK_DISABLED_BLOCKED_BY_DATA_ROAMING;
        }

        if (DBG) Log.d("WifiSettings/AdvancedWifiSettings/SettingsSearchUtils",
                "Checkbox - Airplane Mode Off / Mobile Data Enabled / SIM State-Ready / isMobilePolicyDataEnable / !mobileDataBlockedByRoaming / result : "
                + !isAirplaneMode + " / " + isMobileDataEnabled + " / " +
                (simState == TelephonyManager.SIM_STATE_READY) + " / " +
                mMobilePolicyDataEnable + " / " + !mobileDataBlockedByRoaming + " / " + result);
        return result;
    }

    // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
    public static final int WIFI_SMART_NETWORK_SWITCH_ON_WIFI_SETTINGS = 1;
    public static final int WIFI_SMART_NETWORK_SWITCH_ON_ADVANCED_WIFI_SETTINGS = 2;
    public static final int WIFI_SMART_NETWORK_SWITCH_DISABLED = 3;
    public static int locateSmartNetworkSwitch(Context context) {
        if (context == null) {
            Log.e("WifiSettings/AdvancedWifiSettings/SettingsSearchUtils", "context is null.");
            return -1;
        }
        if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
            Log.e("WifiSettings/AdvancedWifiSettings/SettingsSearchUtils",
                    "locateSmartNetworkSwitch() - WIFI_SMART_NETWORK_SWITCH_DISABLED(myUserId != USER_OWNER)");
            return WIFI_SMART_NETWORK_SWITCH_DISABLED;
        }

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE); 

        if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
                || (context != null && Settings.Global.getInt(context.getContentResolver(), Settings.Global.WIFI_WATCHDOG_ON, 1) != 1)
                || "REMOVED".equals(SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGSNSSTATUS))
                /* Q porting || isWifiOnly()*/) {
            if (DBG) Log.d("WifiSettings/AdvancedWifiSettings/SettingsSearchUtils",
                    "locateSmartNetworkSwitch() - WIFI_SMART_NETWORK_SWITCH_DISABLED");
            return WIFI_SMART_NETWORK_SWITCH_DISABLED;
        }
        if (DBG) Log.d("WifiSettings/AdvancedWifiSettings/SettingsSearchUtils",
                "locateSmartNetworkSwitch() - WIFI_SMART_NETWORK_SWITCH_ON_ADVANCED_WIFI_SETTINGS");
        return WIFI_SMART_NETWORK_SWITCH_ON_ADVANCED_WIFI_SETTINGS;
    }
   // <<<WCM<<<
 

    public static boolean isWifiConnected(Context context){
        boolean isWifiConnected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            isWifiConnected = activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        Log.d(TAG, "isWifiConnected : "+isWifiConnected);
        return isWifiConnected;
    }

    public static void initMHSFeature(Context context) {
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        if(wm == null) {
            Log.e(TAG, "Can't get WifiManager.");
            return ;
        }

        MAX_CLIENT_4_MOBILEAP = wm.semGetWifiApMaxClient();
        SUPPORT_MOBILEAP_5G =wm.semSupportWifiAp5G();
        SUPPORT_MOBILEAP_5G_BASED_ON_COUNTRY =wm.semSupportWifiAp5GBasedOnCountry();
        SUPPORT_MOBILEAP_REGION = getRegion();
        SUPPORT_MOBILEAP_WIFISHARING = wm.isWifiSharingSupported();
        SUPPORT_MOBILEAP_WIFISHARINGLITE = wm.isWifiSharingLiteSupported();

        if (MHSDBG) {
            String str = SystemProperties.get("mhs.customer");
            Log.w(TAG, " mhs.carrier:["+str+"]");
            if(str != null && !str.equals("")) {
                CONFIGOPBRANDINGFORMOBILEAP = str;
            }
        }


        Log.i(TAG, "initMHSFeature MAX_CLIENT_4_MOBILEAP :"+MAX_CLIENT_4_MOBILEAP);
        Log.i(TAG, "initMHSFeature SUPPORT_MOBILEAP_5G :"+SUPPORT_MOBILEAP_5G);
        Log.i(TAG, "initMHSFeature SUPPORT_MOBILEAP_5G_BASED_ON_COUNTRY :"+SUPPORT_MOBILEAP_5G_BASED_ON_COUNTRY);
        Log.i(TAG, "initMHSFeature SUPPORT_MOBILEAP_REGION :"+SUPPORT_MOBILEAP_REGION);
        Log.i(TAG, "initMHSFeature SUPPORT_MOBILEAP_WIFISHARING :"+SUPPORT_MOBILEAP_WIFISHARING);
        Log.i(TAG, "initMHSFeature SUPPORT_MOBILEAP_WIFISHARINGLITE :"+SUPPORT_MOBILEAP_WIFISHARINGLITE);
        Log.i(TAG, "initMHSFeature CONFIGOPBRANDINGFORMOBILEAP :"+CONFIGOPBRANDINGFORMOBILEAP);
    }

    public static boolean isLightTheme(Context context) {
        if (mDeviceType != null && mDeviceType.length() > 0) {
            if (mDeviceType.contains("lightTheme")) {
                return true;
            }
        }
        mDeviceType = SystemProperties.get("ro.build.characteristics");
        if (mDeviceType != null && mDeviceType.contains("lightTheme")) {
            return true;
        } else {
            Log.d("Utils","returning true by default for is light theme");

            return true;
        }
    }

    public static boolean isDuosModel(Context context, String filter) {
        SecSimFeatureProvider simFeature = FeatureFactory.getFactory(context).getSecSimFeatureProvider();
        if(simFeature.isMultiSimModel()) {
            if (isChinaCTCModel() && "CDMA".equals(filter)) {
                return true;
            }
        }
        return false;
    }

    public static String getRegion() {
        CscParser parser = new CscParser(CscParser.getCustomerPath());
        return parser.get("GeneralInfo.Region");
    }
    // SEC_END : SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM

    /**
     *InputFilter to limit the emoticon
     */
    public static class EmojiInputFilter implements InputFilter {
        private  Context mContext;
        public EmojiInputFilter(Context context) {
            mContext = context;
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
                                   int dend) {
            return emojiParsing(source,mContext,dest,dstart,dend);
        }
    }

    private static CharSequence emojiParsing(CharSequence chars, Context context, Spanned dest, int dstart,
                                             int dend) {
        int count = chars.length();

        boolean hasEmoji = false;

        for (int i = 0; i < count; i++) {
            int cu = (int) chars.charAt(i);
            if(Character.SURROGATE == Character.getType(chars.charAt(i))){
                hasEmoji = true;
            } else if (cu >= 0xD800 && cu <= 0xDBFF) {
                hasEmoji = true;
            } else {
                switch(cu) {
                    case 0x2049:
                    case 0x2139:
                    case 0x2194:
                    case 0x2195:
                    case 0x2196:
                    case 0x2197:
                    case 0x2198:
                    case 0x2199:
                    case 0x219A:
                    case 0x21AA:
                    case 0x231B:
                    case 0x23E9:
                    case 0x23EA:
                    case 0x23EB:
                    case 0x23EC:
                    case 0x23F0:
                    case 0x23F3:
                    case 0x24C2:
                    case 0x25AB:
                    case 0x25B6:
                    case 0x25C0:
                    case 0x25FB:
                    case 0x25FC:
                    case 0x25FD:
                    case 0x25FE:
                    case 0x2600:
                    case 0x2601:
                    case 0x2611:
                    case 0x2614:
                    case 0x2615:
                    case 0x261D:
                    case 0x2639:
                    case 0x263A:
                    case 0x263B:
                    case 0x2648:
                    case 0x2649:
                    case 0x264A:
                    case 0x264B:
                    case 0x264C:
                    case 0x264D:
                    case 0x264E:
                    case 0x264F:
                    case 0x2650:
                    case 0x2651:
                    case 0x2652:
                    case 0x2653:
                    case 0x2660:
                    case 0x2663:
                    case 0x2665:
                    case 0x2666:
                    case 0x2668:
                    case 0x267B:
                    case 0x267F:
                    case 0x26A0:
                    case 0x26A1:
                    case 0x26AA:
                    case 0x26AB:
                    case 0x26BD:
                    case 0x26BE:
                    case 0x26C4:
                    case 0x26C5:
                    case 0x26CE:
                    case 0x26D4:
                    case 0x26F3:
                    case 0x26F5:
                    case 0x26FA:
                    case 0x26FD:
                    case 0x2702:
                    case 0x2705:
                    case 0x2708:
                    case 0x270A:
                    case 0x2714:
                    case 0x2716:
                    case 0x270B:
                    case 0x270C:
                    case 0x270D:
                    case 0x2712:
                    case 0x2728:
                    case 0x2733:
                    case 0x2734:
                    case 0x2744:
                    case 0x2747:
                    case 0x274C:
                    case 0x274E:
                    case 0x2753:
                    case 0x2754:
                    case 0x2755:
                    case 0x2757:
                    case 0x2763:
                    case 0x2764:
                    case 0x2795:
                    case 0x2796:
                    case 0x2797:
                    case 0x27B0:
                    case 0x27BF:
                    case 0x2934:
                    case 0x2935:
                    case 0x2B05:
                    case 0x2B06:
                    case 0x2B07:
                    case 0x2B1B:
                    case 0x2B1C:
                    case 0x2B50:
                    case 0x2B55:
                    case 0x3030:
                    case 0x303D:
                    case 0x3297:
                    case 0x3299:
                        hasEmoji = true;
                        break;
                    default:
                        break;
                }
            }
            if (hasEmoji) {
                Toast.makeText(new ContextThemeWrapper(
                                context,android.R.style.Theme_DeviceDefault_Light),
                        context.getString(R.string.sec_fingerprint_invalid_rename_character),
                        Toast.LENGTH_SHORT).show();
                return dest.subSequence(dstart, dend);
            }
        }
        return chars;
    }


    /**
     * Returns true if Japan build.
     */
    public static boolean isJapanModel() {
        String countryCode = readCountryCode();
        return ("JP").equalsIgnoreCase(countryCode);
    }

    public static boolean isJapanDCMModel() {
        String salesCode = readSalesCode();
        return ("DCM".equals(salesCode));
    }

    public static boolean isJapanKDIModel() {
        String salesCode = readSalesCode();
        return ("KDI".equals(salesCode));
    }

    public static boolean isJapanKDIMvnoModel() {
        String salesCode = readSalesCode();
        return ("UQM".equals(salesCode) || "JCO".equals(salesCode));
    }

    //USER_MANUAL_START
    public static class OnlineHelpMenuState {
        public boolean removeTile;
        public Intent intent;
        public int titleRes;
        public int summaryRes;
        public int iconRes;

        OnlineHelpMenuState() {
            this.removeTile = false;
            this.intent = null;
            this.titleRes = 0;
            this.summaryRes = 0;
            this.iconRes = 0;
        }
    }

    private static OnlineHelpMenuState mHelpMenuData;
    public static OnlineHelpMenuState getOnlineHelpMenuState(Context context) {
        if(mHelpMenuData == null) {
            mHelpMenuData = getOnlineHelpMenuState(context, mHelpMenuData);
        }

        return mHelpMenuData;
    }

    private static OnlineHelpMenuState getOnlineHelpMenuState(Context context, OnlineHelpMenuState helpMenuData) {
        if (helpMenuData == null) {
            helpMenuData = new OnlineHelpMenuState();
        }

        Log.d(TAG, "CscFeature_Setting_ConfigTypeHelp: "+ SemCscFeature.getInstance().getInteger("CscFeature_Setting_ConfigTypeHelp") + "[0:dont support, 1:online, 2:ondevice]");
        if (SemCscFeature.getInstance().getInteger("CscFeature_Setting_ConfigTypeHelp") == 2) {
            helpMenuData.titleRes = R.string.help_title;
            helpMenuData.summaryRes = R.string.help_title;
            helpMenuData.iconRes = R.drawable.sec_ic_settings_help;
            if(isSupportHelpMenu(context)){
                Log.d(TAG, "isSupportOfflineHelpMenu");
                if(SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_ACCESSIBILITY_SUPPORT_MANAGE_ACCESSIBILITY")){
                    Log.d(TAG, "non mass");
                    Intent intent = new Intent("com.samsung.helphub.HELP");
                    helpMenuData.intent = intent;
                }else{
                    Log.d(TAG, "mass");
                    if("VZW".equals(Rune.readSalesCode())){
                        Log.d(TAG, "is mass. " + Rune.readSalesCode());
                        Intent intent = new Intent("com.samsung.helphub.HELP");
                        helpMenuData.intent = intent;
                    }else{
                        Log.d(TAG, "remove online help - is mass " + Rune.readSalesCode());
                        helpMenuData.removeTile = true;
                    }
                }
            }else{
                Log.d(TAG, "remove online help -  is not SupportOfflineHelpMenu.");
                helpMenuData.removeTile = true;
            }
        } else if(SemCscFeature.getInstance().getInteger("CscFeature_Setting_ConfigTypeHelp") == 0) { // don't support anything
            Log.d(TAG, "remove online help -  help csc feature is 0");
            helpMenuData.removeTile = true;
        } else { // support online help
            Log.d(TAG, "help csc feature is Default 1");

            helpMenuData.intent = getUserManualSearchURLIntent(context, null);
            helpMenuData.titleRes = R.string.user_manual;
            helpMenuData.summaryRes = R.string.user_manual;
            helpMenuData.iconRes = R.drawable.sec_ic_settings_user_manual;

            if(Rune.isUSA()) {
                helpMenuData.titleRes = R.string.help_title;
                helpMenuData.iconRes = R.drawable.sec_ic_settings_help;
            }
            if(!Utils.isIntentAvailable(context, helpMenuData.intent)) {
                Log.d(TAG, "do not support browser.");
                helpMenuData.removeTile = true;
            }
        }
        return helpMenuData;
    }

    public static Intent getUserManualSearchURLIntent(Context context, String keyword) {
        Uri targetUri = null;
        String manualURL = null;
        final String SBROWSER_PKG = "com.sec.android.app.sbrowser";
        final String SBROWSER_CLASS = "com.sec.android.app.sbrowser.SBrowserMainActivity";
        final ComponentName component = new ComponentName(SBROWSER_PKG, SBROWSER_CLASS);
        Intent intent = new Intent();

        if(Utils.hasPackage(context, SBROWSER_PKG)) {
            intent.setComponent(component);
            Log.d(TAG, "sbrowser is exist on device.");
        } else {
            intent.setAction(Intent.ACTION_VIEW);
        }

        try {
            manualURL = android.provider.Settings.Global.getString(context.getContentResolver(), "online_manual_url");
        } catch (Exception e) {
            Log.i(TAG, "no online_manual_url value");
        }
        if (manualURL == null || manualURL.length() < 1) {
            manualURL = "http://www.samsung.com/m-manual/common";
        }

        if (Rune.supportDesktopMode() && Rune.isSamsungDexMode(context)) {
            manualURL = manualURL.trim();
            if (manualURL.charAt(manualURL.length()-1)=='/') {
                manualURL = manualURL.substring(0, manualURL.length()-1);
            }
            //ex) http://www.samsung.com/m-manual/common/SM-G965F/Android8.0.0
            manualURL = manualURL + '/' + SystemProperties.get("ro.product.model") + "/Android" + SystemProperties.get("ro.build.version.release");
        }

        if(keyword != null && keyword.length() > 0) {
            Log.d(TAG, "Search values : "+ keyword);
            try {
                String query = URLEncoder.encode(keyword,"UTF-8");

                //ex) http://www.samsung.com/m-manual/common?appid=app&anchor=%EC%B9%B4%EB%A9%94%EB%9D%BC&pos=find
                targetUri = Uri.parse(manualURL
                        + "?appid=app&anchor="
                        + query
                        + "&pos=find");
            } catch (UnsupportedEncodingException e) {
                Log.i(TAG, "UnsupportedEncoding for keyword targetUri. using default manualURL");
                targetUri = Uri.parse(manualURL);
            }
        } else {
            Log.i(TAG, "No keyword - launch usermanual main");
            targetUri = Uri.parse(manualURL);
        }
        Log.d(TAG, "Uri: " + targetUri.toString());

        intent.setData(targetUri);
        return intent;
    }

    public static boolean isSupportHelpMenu(Context context) {
        // VERTICAL_ADVANCED_SETTINGS_START
        if (context == null || SemPersonaManager.isKnoxId(context.getUserId())) {
            return false;
        }
        // VERTICAL_ADVANCED_SETTINGS_END
        try {
            if (hasPackage(context, HELPHUB_PACKAGE_NAME)) {
                PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(HELPHUB_PACKAGE_NAME, 0);
                if (pkgInfo != null && pkgInfo.versionCode == 2) {
                    Log.d(TAG, "device support helphub pkg");
                    return true;
                }
            }
        } catch (NameNotFoundException e) {
            //e.printStackTrace();
            Log.i(TAG, "No HelpHub pkg");
        }

        return false;
    }

    // TIPS_AND_USER_MANUAL_START
    public static String getTipAndUserManualTitle(Context context) {
        OnlineHelpMenuState helpMenuData = getOnlineHelpMenuState(context);

        if (helpMenuData.removeTile) {
            return context.getString(R.string.sec_tips_title);
        } else {
            if (Rune.isUSA()) {
                return context.getString(R.string.sec_tips_and_help_title);
            } else {
                return context.getString(R.string.sec_tips_and_user_manual_title);
            }
        }
    }

    public static String getTipAndUserManualSummary(Context context) {
        OnlineHelpMenuState helpMenuData = getOnlineHelpMenuState(context);

        if (helpMenuData.removeTile) {
            return context.getString(R.string.sec_tips_summary);
        } else {
            if (Rune.isUSA()) {
                return context.getString(R.string.sec_tips_and_help_summary);
            } else {
                return context.getString(R.string.sec_tips_and_user_manual_summary);
            }
        }
    }
    // TIPS_AND_USER_MANUAL_END

    // KNOX_CONTAINER_V30_START
    public static KnoxContainerManager getKnoxContainerManager(Context cxt, int userid) {
        EnterpriseKnoxManager ekm = EnterpriseKnoxManager.getInstance();
        if (ekm == null) {
            return null;
        }
        KnoxContainerManager kcm = ekm.getKnoxContainerManager(cxt, userid);
        return kcm;
    }

    // KNOX_CONTAINER_V30_START
    public static KnoxContainerManager getKnoxContainerManager(Context cxt, ContextInfo contextInfo) {
        EnterpriseKnoxManager ekm = EnterpriseKnoxManager.getInstance();
        if (ekm == null) {
            return null;
        }
        KnoxContainerManager kcm = ekm.getKnoxContainerManager(cxt, contextInfo);
        return kcm;
    }

    public static boolean isMultifactorAuthEnforced(Context cxt, int userId) {
        boolean mIsMultifactorAuthEnforced = false;
        try {
            if (userId == UserHandle.USER_SYSTEM) {
                EnterpriseDeviceManager mEdm = (EnterpriseDeviceManager) cxt.getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
                if (mEdm != null) {
                    mIsMultifactorAuthEnforced = mEdm.getPasswordPolicy().isMultifactorAuthenticationEnabled();
                }
            } else {
                KnoxContainerManager containerMgr = getKnoxContainerManager(cxt, userId);
                if(containerMgr != null) {
                    mIsMultifactorAuthEnforced = containerMgr.getPasswordPolicy().isMultifactorAuthenticationEnabled();
                }
            }
        } catch (SecurityException e) {
            Log.d(TAG, "SecurityException: " + e);
        }
        return mIsMultifactorAuthEnforced;
    }

    /**
     * Check whether Enforce Multifactor policy was set or not
     * @param cxt Context
     * @return true if fingerprint template change is needed for Knox.
     */
    public static boolean isFPTemplateChangeForKnox(Context cxt) {
        boolean result = false;
        try {
            SemPersonaManager personaManager = (SemPersonaManager) cxt.getSystemService(Context.SEM_PERSONA_SERVICE);
            List<Integer> personaIds = personaManager.getKnoxIds(false);
            int userId = UserHandle.myUserId();
            if (SemPersonaManager.isSecureFolderId(userId)) {
                return false;
            }
            if (SemPersonaManager.isKnoxId(userId)) {
                result = isMultifactorAuthEnforced(cxt, userId);
                if (SemPersonaManager.isDoEnabled(UserHandle.USER_SYSTEM)) {
                    result |= isMultifactorAuthEnforced(cxt, UserHandle.USER_SYSTEM);
                }
            } else {
                if (SemPersonaManager.isDoEnabled(userId)) {
                    result = isMultifactorAuthEnforced(cxt, userId);
                }
                if (personaIds.size() > 0) {
                    for (int personaId : personaIds) {
                        result |= isMultifactorAuthEnforced(cxt, personaId);
                    }
                }
            }
            Log.d(TAG, "FPTemplateChange : size = " + personaIds.size() + ", result = " + result);
        } catch (SecurityException e) {
            Log.d(TAG, "SecurityException: " + e);
        }
        return result;
    }

    /**
     * Check whether the open theme is applied on device
     */
    public static boolean isSetOpenTheme(Context context) {
        String packageName = Settings.System.getString(context.getContentResolver(), Settings.System.SEM_CURRENT_THEME_PACKAGE);

        return !TextUtils.isEmpty(packageName);
    }

    /**
     * Check whether the current theme support the night mode or not
     **/
    public static boolean isCurrentThemeSupportNightTheme(Context context) {
        if (!Utils.isSetOpenTheme(context)) {
            return true;
        }
        return Settings.System.getInt(context.getContentResolver(), "current_theme_support_night_mode", 0) == 1;
    }

    /**
     * Check Accessibility > High contrast fonts is enabled status
     * @return true if High contrast fonts is ON state
     **/
    public static boolean isHightContrastFontsEnabled(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_HIGH_TEXT_CONTRAST_ENABLED, 0) != 0;
    }

    /**
     * Check Power mode > Medium power saving is enabled status
     *
     * @return true if Medium power saving is ON state
     **/
    public static boolean isMediumPowerSavingModeEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "low_power", -1) == 1;
    }

    /**
     * Check Power mode > Medium power > Dark Theme is enabled status
     *
     * @return true if Dark Theme is ON state
     **/
    public static boolean isPSMDarkModeEnabled(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "psm_dark_mode", -1) == 1;
    }
    //USER_MANUAL_END

    /**
     * Check Accessibility > Navigate colors is enabled status
     * @return true if Navigate colors is ON state
     **/
    public static boolean isNavigateColourEndabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.SEM_HIGH_CONTRAST, 0) != 0;
    }

    public static boolean isSupportSSecure() {
        String yuvaFeatures = SemCscFeature.getInstance().getString(CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_CONFIGYUVA);
        return !TextUtils.isEmpty(yuvaFeatures) && yuvaFeatures.contains("sprotect");
    }

    public static boolean isPackageExists(Context context, String targetPackage) {
        boolean result = false;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(targetPackage, 0);
            if(appInfo != null) {
                result = true;
            } else {
                Log.e(TAG, "isPackageExists :: appInfo is null");
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "isPackageExists :: target package = " + targetPackage + ", find = " + result);
        return result;
    }

    /**
     * @param context
     **/
    public static boolean isRestrictedProfile(Context context) {
        UserManager mUm = (UserManager) context.getSystemService(Context.USER_SERVICE);
        return mUm.hasUserRestriction(UserManager.DISALLOW_MODIFY_ACCOUNTS);
    }

    public static boolean isOnCall() {
        int currentCallstateSim1 = TelephonyManager.CALL_STATE_IDLE;
        int currentCallstateSim2 = TelephonyManager.CALL_STATE_IDLE;

        currentCallstateSim1 = TelephonyManager.getDefault().getCallStateForSlot(SIMSLOT1);
        if (TelephonyManager.getDefault().getPhoneCount() > 1) {
            currentCallstateSim2 = TelephonyManager.getDefault().getCallStateForSlot(SIMSLOT2);
        }
        Log.i(TAG, "Check Call state SIM1 : " + currentCallstateSim1 + ", SIM2 : " + currentCallstateSim2);
        if (currentCallstateSim1 == TelephonyManager.CALL_STATE_RINGING || currentCallstateSim1 == TelephonyManager.CALL_STATE_OFFHOOK
                || currentCallstateSim2 == TelephonyManager.CALL_STATE_RINGING || currentCallstateSim2 == TelephonyManager.CALL_STATE_OFFHOOK) {
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // SEC_END
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // { Rune.SUPPORT_NOTI_NOTIFICATION_STATUSBAR
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivity == null){
            return false;
        }
        NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if(info != null){
            for(int i = 0; i < info.length; i = i + 1){
                if(info[i].getState() == NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }
        return false;
    }
    // } Rune.SUPPORT_NOTI_NOTIFICATION_STATUSBAR

    // Samsung ODE
    public static boolean isSupportStrongProtectionMenu() {
        int firstApiLevel = SystemProperties.getInt("ro.product.first_api_level", 0);
        Log.d(TAG, "firstApiLevel " + firstApiLevel);

        // firstApiLevel >= Build.VERSION_CODES.Q : always on
        return (firstApiLevel < Build.VERSION_CODES.Q);
    }
    // Samsung ODE
    // SEC_START : SRIN.Settings
    public static boolean isCMCAvailable(Context ctx) {
        String packageName = "com.samsung.android.mdecservice";
        if(!Utils.hasPackage(ctx, packageName) || UserHandle.myUserId() != UserHandle.USER_OWNER) {
            return false;
        }
        boolean isCmcMenuSupported = Settings.Global.getInt(ctx.getContentResolver( ), "cmc_own_settings_menu_supported", 0) == 1 ? true : false;
        Log.d(TAG, "isCmcMenuSupported = " + isCmcMenuSupported);
        if(isCmcMenuSupported) {
            PackageInfo pi = null;
            try {
                pi = ctx.getPackageManager().getPackageInfo(packageName, 0);
                String versionName = pi.versionName;
                Log.d(TAG, "versionName = " + versionName);
                if (!TextUtils.isEmpty(versionName)) {
                    String[] versionNameArr = versionName.split("\\.");
                    if (versionNameArr != null && versionNameArr.length == 4) {
                        if (Integer.parseInt(versionNameArr[0]) >= 2) {
                            return true;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    // SEC_END : SRIN.Settings
    // SEC_START - 1 depth summary 
    public static String buildSummaryString(Context context, List<String> summaries) {
        return buildSummaryString(context, summaries, summaries.size());
    }

    public static String buildSummaryString(Context context, List<String> summaries, int maxCount) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < summaries.size() && i < maxCount; i++) {
            builder.append(summaries.get(i));
            if (i < Math.min(summaries.size(), maxCount) - 1) {
                builder.append(Utils.getComma(context));
            }
        }
        return builder.toString();
    }
    // SEC_END

    // To support multi window in Bixby 2.0
    public static Intent setTaskIdToIntent(Intent intent, Integer taskId) {
        if (taskId != null) {
            try {
                Method m = Intent.class.getDeclaredMethod("semSetLaunchOverTargetTask", new Class[]{int.class, boolean.class});
                m.invoke(intent, new Object[]{taskId.intValue(), false});
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        return intent;
    }
	
    //<!-- Secure Wi-Fi Start-->
    public static boolean isSupportedCountryForEurOnSecureWiFi() {
	        List<String> EUR_LIST = new ArrayList<String>() {
			{
                // EUR Countries
                add("AT"); add("AUT");// AUSTRIA
		    	add("BE"); add("BEL");// BELGIUM
                add("HR"); add("HRV");// CROATIA (local name: Hrvatska)
                add("BG"); add("BGR");// BULGARIA
                add("CY"); add("CYP");// CYPRUS
                add("CZ"); add("CZE");// CZECH REPUBLIC
                add("DK"); add("DNK");// DENMARK
                add("EE"); add("EST");// ESTONIA
                add("FI"); add("FIN");// FINLAND
                add("FR"); add("FRA");// FRANCE
                add("DE"); add("DEU");// GERMANY
                add("GR"); add("GRC");// GREECE
                add("HU"); add("HUN");// HUNGARY
                add("IE"); add("IRL");// IRELAND
                add("IT"); add("ITA");// ITALY
                add("LV"); add("LVA");// LATVIA
                add("LT"); add("LTU");// LITHUANIA
                add("LU"); add("LUX");// LUXEMBOURG
                add("MT"); add("MLT");// MALTA
                add("NL"); add("NLD");// NETHERLANDS
                add("NO"); add("NOR");// NORWAY
                add("PL"); add("POL");// POLAND
                add("PT"); add("PRT");// PORTUGAL
                add("RO"); add("ROU");// ROMANIA
                add("SK"); add("SVK");// SLOVAKIA
                add("SI"); add("SVN");// SLOVENIA
                add("ES"); add("ESP");// SPAIN
                add("SE"); add("SWE");// SWEDEN
                add("GB"); add("GBR");// UNITED KINGDOM
				add("BR"); add("BRA");// BRAZIL
            }
        };
		String countryISO = getSalesCode().equals("TEN") ? "NO" : readCountryCode();
            if (countryISO != null && EUR_LIST.contains(countryISO.toUpperCase())) {
                return true;
            }
		return false;
	}

     public static boolean isSupportSecureWifi(Context context) {
         return (SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTSECUREWIFI)
            || Utils.isSupportedCountryForEurOnSecureWiFi())
            && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_SECURE_WIFI")
            && !SemPersonaManager.isDoEnabled(UserHandle.myUserId()) 
            && isSecureWifiPackage(context)
            && UserManager.get(context).isSystemUser();
    }
	
    private static boolean isSecureWifiPackage(Context context) {
        PackageManager packageManager = context.getPackageManager();
        if(packageManager.checkSignatures("android", "com.samsung.android.fast") == PackageManager.SIGNATURE_MATCH) {
            return true;
        }
        Log.e(TAG,"Secure Wi-Fi signature mismatched");
        return false;
    }
    //<!-- Secure Wi-Fi End-->
}
