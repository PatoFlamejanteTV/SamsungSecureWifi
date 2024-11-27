/*
 * Copyright (C) 2010 The Android Open Source Project
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

import static android.os.UserManager.DISALLOW_CONFIG_WIFI;

import android.annotation.NonNull;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.InsetDrawable;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.PreferenceActivity;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.SemExpandableListView;
import android.widget.SemExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ProgressBar;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.LinkifyUtils;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.location.ScanningSettings;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.Utils;
import com.android.settings.widget.SummaryUpdater.OnSummaryChangeListener;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBarController;
import com.android.settings.wifi.ConfigureWifiSettings;
import com.android.settings.wifi.details.WifiNetworkDetailsFragment;
import com.android.settings.wifi.WpsDialog;

import com.android.settings.wifi.p2p.WifiP2pSettings;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.AccessPoint.AccessPointListener;
import com.samsung.android.settingslib.wifi.AccessPointPreference;
import com.android.settingslib.wifi.WifiTracker;
import com.android.settingslib.wifi.WifiTrackerFactory;

import com.samsung.android.emergencymode.SemEmergencyManager;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.settings.SAUtils;
import com.samsung.android.settings.guide.WifiHelpPage;
import com.samsung.android.settings.guide.GuideFragment;
import com.samsung.android.settings.guide.GuideModeHelper;
import com.samsung.android.settings.wifi.ListAnimationController;
import com.samsung.android.settings.wifi.WifiBigDataUtil;
import com.samsung.android.settings.wifi.WifiPickerDialog;
import com.samsung.android.settings.wifi.WifiPickerHelper;
import com.samsung.android.settings.wifi.WifiDevicePolicyManager;
import com.samsung.android.settings.wifi.WifiPreferenceCategory;
import com.samsung.android.settings.bixby.EmSettingsManager;    // Bixby
import com.samsung.android.settings.wifi.AccessPointPreferenceGroup;
import com.samsung.android.settings.wifi.AccessPointExpListAdapter;
import com.samsung.android.support.sesl.core.widget.SeslSwipeRefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.MalformedURLException;

// VZW WiFi Offload
import com.sec.android.app.CscFeatureTagCommon;
import com.sec.android.app.SecProductFeature_WLAN;
import com.samsung.android.settings.wifi.WifiOffloadDialog;

/**
 * Two types of UI are provided here.
 *
 * The first is for "usual Settings", appearing as any other Setup fragment.
 *
 * The second is for Setup Wizard, with a simplified interface that hides the action bar
 * and menus.
 */
public class WifiSettings extends RestrictedSettingsFragment
        implements Indexable, WifiTracker.WifiListener, WifiTracker.SemWifiListener {

    private static final String TAG = "WifiSettings";
    private static final boolean DBG = android.os.Debug.semIsProductDev();
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private static final String ARGS_MANAGE_NETWORK = "manage_network";

    /* package */ static final int MENU_ID_WPS_PBC = Menu.FIRST;
    private static final int MENU_ID_WPS_PIN = Menu.FIRST + 1;
    private static final int MENU_ID_ADVANCED = Menu.FIRST + 4;
    private static final int MENU_ID_CONNECT = Menu.FIRST + 6;
    private static final int MENU_ID_FORGET = Menu.FIRST + 7;
    private static final int MENU_ID_MODIFY = Menu.FIRST + 8;
    private static final int MENU_ID_WRITE_NFC = Menu.FIRST + 9;
    private static final int MENU_ID_HELP = Menu.FIRST + 11;
    private static final int MENU_ID_CONTACT_US = Menu.FIRST + 12;

    public static final int WIFI_DIALOG_ID = 1;
    /* package */ static final int WPS_PBC_DIALOG_ID = 2;
    private static final int WPS_PIN_DIALOG_ID = 3;
    private static final int WRITE_NFC_DIALOG_ID = 6;

    private static final long SCAN_PROGRESS_DELAY = 1700;
    public static final int SOCKET_TIMEOUT_MS = 10000;

    // Instance state keys
    private static final String SAVE_DIALOG_MODE = "dialog_mode";
    private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";
    private static final String SAVED_WIFI_NFC_DIALOG_STATE = "wifi_nfc_dlg_state";

    private static final String PREF_KEY_EMPTY_WIFI_LIST = "wifi_empty_list";
    private static final String PREF_KEY_CONNECTED_ACCESS_POINTS = "connected_access_point";
    private static final String PREF_KEY_PASSPOINT_NETWORK = "passpoint_access_point";
    private static final String PREF_KEY_ACCESS_POINTS = "access_points";
    private static final String PREF_KEY_HEADER_SETTINGS = "header_settings";
    private static final String PREF_KEY_ADD_NETWORK = "add_network";
    private static final String PREF_KEY_CRICKET_WIFI_MANAGER = "wifi_cricket_manager";

    private static final String VENDOR_FRIENDLY_NAME = "Vendor Hotspot2.0 Profile";

    /*private final Runnable mHideProgressBarRunnable = () -> {
        setProgressBarVisible(false);
    };*/
    protected WifiManager mWifiManager;
    private WifiManager.ActionListener mConnectListener;
    private WifiManager.ActionListener mSaveListener;
    private WifiManager.ActionListener mForgetListener;

    /**
     * The state of {@link #isUiRestricted()} at {@link #onCreate(Bundle)}}. This is neccesary to
     * ensure that behavior is consistent if {@link #isUiRestricted()} changes. It could be changed
     * by the Test DPC tool in AFW mode.
     */
    private boolean mIsRestricted;

    private WifiEnabler mWifiEnabler;
    // An access point being editted is stored here.
    private AccessPoint mSelectedAccessPoint;

    private WifiDialog mDialog;
    private WriteWifiConfigToNfcDialog mWifiToNfcDialog;

    //private ProgressBar mProgressHeader;
    private SwitchBar mProgressHeader;

    // this boolean extra specifies whether to disable the Next button when not connected. Used by
    // account creation outside of setup wizard.
    private static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";
    // This string extra specifies a network to open the connect dialog on, so the user can enter
    // network credentials.  This is used by quick settings for secured networks.
    private static final String EXTRA_START_CONNECT_SSID = "wifi_start_connect_ssid";
    private static final String EXTRA_START_CONNECT_SECURITY = "wifi_start_connect_security";
    // should Next button only be enabled when we have a connection?
    private boolean mEnableNextOnConnection;

    // Save the dialog details
    private int mDialogMode;
    private AccessPoint mDlgAccessPoint;
    private Bundle mAccessPointSavedState;
    private Bundle mWifiNfcDialogSavedState;

    // Save Passpoint APs for Manage Network
    private HashMap<String, PasspointConfiguration> mPasspointInfoMap = new HashMap<String, PasspointConfiguration>();
    private List<PasspointConfiguration> mPasspointConfigList;
    private PasspointConfiguration mSelectedPasspointConfig;
    private String mSelectedPasspointFqdn;
    private String mSelectedpasspointName;

    private WifiTracker mWifiTracker;
    private String mOpenSsid;
    private int mOpenSecurity;
    private boolean mGoToSettingsFromQuickPanelFirstTime;
    private boolean mIsSupportedContactUs;

    private HandlerThread mBgThread;

    private AccessPointPreference.UserBadgeCache mUserBadgeCache;

    private AccessPointPreferenceGroup mConnectedAccessPointPreferenceGroup;
    private AccessPointPreferenceGroup mAccessPointsPreferenceGroup;
    private AccessPointPreferenceGroup mPassPointPreferenceGroup;

    private WifiPreferenceCategory mHeaderPreferenceCategory;
    private WifiPreferenceCategory mConnectedAccessPointPreferenceCategory;
    private WifiPreferenceCategory mAccessPointsPreferenceCategory;
    private WifiPreferenceCategory mPasspointPreferenceCategory;
    private Preference mAddPreference;
    private Preference mCricketWifiPreference;
    //private Preference mConfigureWifiSettingsPreference;
    //private Preference mSavedNetworksPreference;

    // For Search
    private static final String DATA_KEY_REFERENCE = "main_toggle_wifi";

    /* End of "used in Wifi Setup context" */
    /* Samsung Modifications */
    private ConnectivityManager mConnectivityManager;
    private boolean mFinishIfConnected = false;
    private boolean mFinishIfWifiDisabled = false;
    private boolean mHideHeaderCategory = false;

    private long mScrollTimer;
    private boolean mInManageNetwork;
    private boolean mInPickerActivity;
    private boolean mInPickerDialog;
    private boolean mShowRetryDialog;
    private boolean mShowNotInRagededAp;
    public static boolean mWpsInProgress;
    protected WpsDialog mWpsDialog;
    private static boolean mWifiSettingsForeground = false;
    private boolean mIsSupportedHelpHub = false;
    private boolean mIsSupportedCricketManager = false;
    private boolean mHideContextMenus = false;
    private boolean mHideActionBarMenus = false;

    private WifiPickerHelper mWifiPickerHelper;

    private TextView mWifiDirect;
    private TextView mWifiAdvanced;
    private TextView contactUsView;
    private TextView mPreEmptyView;

    //SEC_FLOATING_FEATURE_WLAN_SUPPORT_AP_LINK
    private String mGateway = null;
    public static boolean goToWebPageLinkViewed = false;
    public static boolean goToWebPageLinkClicked = false;
    public static int goToWebPageHTTPResponse = 0;
    private static final int MSG_CHECK_GO_TO_WEBPAGE_HTTP_RESPONSE = 0;
    private String mOpBranding = Utils.CONFIG_OP_BRANDING;
    private int mSAScreenId = R.string.screen_wifi_setting;
    private int mPreWifiState = WifiManager.WIFI_STATE_UNKNOWN;
    /* End of Samsung Modifications */
    //SHARED DEVICE START
    private Context mContext;
    //SHARED DEVICE END
    private ListAnimationController mAnimationController; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AP_LIST_ANIMATION
    private View mListAnimationView; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AP_LIST_ANIMATION
    private ViewGroup mContainer; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AP_LIST_ANIMATION
    private View mCricketManagerHeader;

    //VZW Wifi Offload
    private boolean mInOffloadDialog;
    private boolean mForceScrollToTop;

    // Bixby
    private static final String mBixbyCurrentStateId = "WiFiSettings";
    private String mEmLastStateID;
    private boolean mWillRespondToEm = false;
    private CountDownTimer mBixbyCountDownTimer = null;

    // QR code

    private WifiConfiguration mQrConfig = null;
    private boolean isWifiQr = false;

    public WifiSettings() {
        super(DISALLOW_CONFIG_WIFI);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Activity activity = getActivity();
        if (activity != null) {
            //remove Google Style //mProgressHeader = (ProgressBar)
            //        setPinnedHeaderView(R.layout.wifi_progress_header);
            if (activity instanceof SettingsActivity) {
                final SettingsActivity settingsActivity = (SettingsActivity) activity;
                mProgressHeader = settingsActivity.getSwitchBar();
                setProgressBarVisible(false);
            }
        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        if (DBG) Log.d(TAG, "onCreate");
        super.onCreate(icicle);

        Bundle args = getArguments();
        if (args != null) {
            mInManageNetwork = args.getBoolean(ARGS_MANAGE_NETWORK, false);
        }

        mIsSupportedCricketManager = ((DBG || "AIO".equals(Utils.readSalesCode()))
                && isCricketManagerSupport());
        boolean showScanItems = true;
        if (mInManageNetwork) {
            showScanItems = false;
            mShowNotInRagededAp = true;
            mIsSupportedCricketManager = false;
            mHideActionBarMenus = true;
            mFinishIfWifiDisabled = true;
            mHideContextMenus = true;
            mSAScreenId = R.string.screen_wifi_manage_network;
        }
        mInPickerDialog = (getActivity() instanceof WifiPickerDialog);
        //SHARED DEVICE START
        mContext = getActivity().getApplicationContext();
        //SHARED DEVICE END

        Context prefContext = getPrefContext();
        mUserBadgeCache = new AccessPointPreference.UserBadgeCache(getPackageManager());

        mBgThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mBgThread.start();

        mWifiTracker = WifiTrackerFactory.create(
                mContext, this,
                mBgThread.getLooper(), true, showScanItems, false);
        mWifiManager = mWifiTracker.getManager();
        mWifiTracker.setAccessPointVisible(mShowNotInRagededAp);
        mWifiTracker.setSemWifiListener(this);


        mAnimationController = new ListAnimationController(getActivity());
        mAnimationController.setWifiTracker(mWifiTracker);
        mAnimationController.setListener(mViListListener);
        mConnectedAccessPointPreferenceGroup  = new AccessPointPreferenceGroup((Context)getActivity());
        mAccessPointsPreferenceGroup  = new AccessPointPreferenceGroup((Context)getActivity());
        mPassPointPreferenceGroup  = new AccessPointPreferenceGroup((Context)getActivity());

        mIsSupportedHelpHub = Utils.isSupportHelpMenu(mContext);
        mIsSupportedContactUs = Utils.isSupportContactUs(mContext, 231501000);

        mGoToSettingsFromQuickPanelFirstTime = true;

        //SEC_FLOATING_FEATURE_WLAN_SUPPORT_AP_LINK
        goToWebPageLinkViewed = false;
        goToWebPageLinkClicked = false;
        goToWebPageHTTPResponse = 0;

        mInOffloadDialog = (getActivity() instanceof WifiOffloadDialog);
        //VZW Wifi Offload
        // Bixby
        mEmSettingsManager = new EmSettingsManager();
    }

    @Override
    public void onDestroy() {
        if (DBG) Log.d(TAG, "onDestroy");
        mBgThread.quit();

        //SEC_FLOATING_FEATURE_WLAN_SUPPORT_AP_LINK, send bigdata for AP config Link
        if (goToWebPageLinkViewed) {
            String data = (goToWebPageLinkClicked ? "1" : "0") + " " + Integer.toString(goToWebPageHTTPResponse);
            WifiBigDataUtil.getInstance(getActivity()).insertBigdataLog(2,data);
        }

        if (mWifiTracker != null) {
            mWifiTracker.setSemWifiListener(null);
            mWifiTracker = null;
        }

        mEmSettingsManager = null;
        mAnimationController.destroyView();

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContainer = container;
        mListAnimationView = mAnimationController.createView(inflater, container,
                savedInstanceState, mInManageNetwork, mInPickerDialog);
        return mListAnimationView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (DBG) Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

        mConnectListener = new WifiManager.ActionListener() {
                                   @Override
                                   public void onSuccess() {
                                   }
                                   @Override
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
                                           Toast.makeText(activity,
                                                R.string.wifi_failed_connect_message,
                                                Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        mSaveListener = new WifiManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                }
                                @Override
                                public void onFailure(int reason) {
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        Toast.makeText(activity,
                                            R.string.wifi_failed_save_message,
                                            Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };

        mForgetListener = new WifiManager.ActionListener() {
                                   @Override
                                   public void onSuccess() {
                                   }
                                   @Override
                                   public void onFailure(int reason) {
                                       Activity activity = getActivity();
                                       if (activity != null) {
                                           Toast.makeText(activity,
                                               R.string.wifi_failed_forget_message,
                                               Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               };

        if (savedInstanceState != null) {
            mDialogMode = savedInstanceState.getInt(SAVE_DIALOG_MODE);
            if (savedInstanceState.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
                mAccessPointSavedState =
                    savedInstanceState.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
            }

            if (savedInstanceState.containsKey(SAVED_WIFI_NFC_DIALOG_STATE)) {
                mWifiNfcDialogSavedState =
                    savedInstanceState.getBundle(SAVED_WIFI_NFC_DIALOG_STATE);
            }
        }

        Activity activity = getActivity();
        if (activity instanceof WifiPickerDialog) {
            if (mWifiSettingsForeground) {
                Log.i(TAG, "finished Wi-Fi picker dialog because another Wi-Fi settings activity is activated");
                popOrFinishThisActivity();
                return;
            } else if (!mWifiManager.isWifiEnabled()) {
                Log.i(TAG, "finished Wi-Fi picker dialog because Wi-Fi is disabled");
                popOrFinishThisActivity();
                return;
            } else if (checkWifiConnectivity()) {
                Log.i(TAG, "finished Wi-Fi picker dialog because device was connected with AP");
                popOrFinishThisActivity();
                return;
            }

            Log.i(TAG, "Wi-Fi picker dialog is showing");
            mFinishIfConnected = true;
            mFinishIfWifiDisabled = true;
            mHideHeaderCategory = true;

            mSAScreenId = R.string.screen_wifi_picker_dialog;
        } else if (activity instanceof WifiPickerActivity) {
            mInPickerActivity = true;
            mSAScreenId = R.string.screen_wifi_picker_activity;
        }

        // if we're supposed to enable/disable the Next button based on our current connection
        // state, start it off in the right state
        Intent intent = activity.getIntent();
        mEnableNextOnConnection = intent.getBooleanExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, false);
        boolean isGuideMode = GuideFragment.isInGuideMode(activity)
                || intent.hasExtra(GuideModeHelper.SETTINGS_GUIDE_MODE);

        if (isGuideMode) {
            Log.i(TAG, "it's guide mode. hide some UI");
            mFinishIfWifiDisabled = true;
            mHideContextMenus = true;
            mHideActionBarMenus = true;
            mSAScreenId = R.string.screen_wifi_help_activity;
        }

        if (WifiPickerHelper.SEC_PICKER_ACTION.equals(intent.getAction())) {
            mWifiPickerHelper = new WifiPickerHelper(mWifiManager, intent);
            mHideActionBarMenus = true;
            if (mWifiPickerHelper.needToHideContextMenu()) {
                mHideContextMenus = true;
            }
        }

        if (Utils.isEnabledUltraPowerSaving(getActivity())) {
            mHideActionBarMenus = true;
        }

        if (hasNextButton() || hasNextButtonImage()) {
            boolean isSetupwizardFinish = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) != 0 ;
            if(!isSetupwizardFinish) {
                Log.d(TAG, "next/prev button is showing at SetupWizard, hide navigation buttons");
                int visibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                getActivity().getWindow().getDecorView().setSystemUiVisibility(visibility);
            }

            if (mEnableNextOnConnection) {
                changeNextButtonState(checkWifiConnectivity());
            }
        }

        if (!mHideContextMenus) {
            registerForContextMenu(getViListView());
        }

        setupListDivider();

        TextView emptyTextView = getEmptyTextView();
        if (emptyTextView != null) {
            emptyTextView.setGravity(Gravity.START|Gravity.TOP);
            emptyTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            emptyTextView.setTextAppearance(getActivity(), R.style.description_text);
            emptyTextView.setLinkTextColor(getActivity().getResources().getColor(R.color.description_link_text_color));
        }
        contactUsView = mAnimationController.getContactUsView();
        if (contactUsView != null) {
            contactUsView.setGravity(Gravity.START|Gravity.TOP);
            contactUsView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            contactUsView.setTextAppearance(getActivity(), R.style.description_text);
            contactUsView.setLinkTextColor(getActivity().getResources().getColor(R.color.description_link_text_color));
        }
        mPreEmptyView = mAnimationController.getPreEmptyView();
        if (mPreEmptyView != null) {
            mPreEmptyView.setTextAppearance(getActivity(), R.style.description_text);
        }

        if (mIsSupportedCricketManager) {
            final LayoutInflater cricketInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mCricketManagerHeader = cricketInflater.inflate(R.layout.wifi_cricket_manager, getViListView(), false);
            mCricketManagerHeader.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            mCricketManagerHeader.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onCricketManagerClicked();
                }
            });
            getViListView().addHeaderView(mCricketManagerHeader, null, false);
        }

        if (intent.hasExtra(EXTRA_START_CONNECT_SSID)) {
            mOpenSsid = intent.getStringExtra(EXTRA_START_CONNECT_SSID);
            if(intent.hasExtra(EXTRA_START_CONNECT_SECURITY)){
                mOpenSecurity = intent.getIntExtra(EXTRA_START_CONNECT_SECURITY,0);
            }
            if(mGoToSettingsFromQuickPanelFirstTime == false)
                mOpenSecurity = -1;
        }


        if (intent != null && intent.hasExtra("AUTH_TYPE")) {
            isWifiQr = true;
            WifiConfiguration mQrAccessPointConfig = getWifiConfigFromIntent(intent);
            Bundle mQrAccessPointSavedState = new Bundle();
            mQrAccessPointSavedState.putParcelable("key_config", mQrAccessPointConfig);
            AccessPoint mQrAccessPoint = new AccessPoint(getActivity(), mQrAccessPointSavedState);
            showDialog(mQrAccessPoint, WifiConfigUiBase.MODE_WIFIQR);
        }

        if (!mInManageNetwork) {
            if (mWifiEnabler == null) {
                mWifiEnabler = createWifiEnabler();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mListAnimationView != null) {
            ((ViewGroup) mListAnimationView).removeAllViews();
            mListAnimationView = null;
            Log.d(TAG, "onDestroyView, mListAnimationView is removed");
        }
        if (mWifiEnabler != null) {
            mWifiEnabler.teardownSwitchController();
            mWifiEnabler = null;
        }
        if (mWifiDirect != null) {
            mWifiDirect = null;
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        mWifiSettingsForeground = true;

        SAUtils.getInstance(mContext).insertLog(mSAScreenId);
        if (mWifiEnabler != null) {
            mWifiEnabler.setListener(mWifiEnablerListener);
            mWifiEnabler.setScreenIdForSA(mSAScreenId);
            mWifiEnabler.start(mContext);
        }
        mWifiTracker.startTracking();

        mIsRestricted = isUiRestricted();
        if (mIsRestricted) {
            showRestrictionEmptyView();
            return;
        }
        /*if(mAccessPointsPreferenceGroup != null) {
            addDeviceCategory(mAccessPointsPreferenceGroup, R.string.wifi_preference_availavle_networks, 0);
        }*/
        onWifiStateChanged(mWifiTracker.getWifiState());
    }

    private void showRestrictionEmptyView() {
        if (!isUiRestrictedByOnlyAdmin()) {
            Log.i(TAG, "ui rstricted by user");
            addMessagePreference(R.string.wifi_empty_list_user_restricted);
        }
        mAnimationController.removeAll();
        onDataSetChanged();
        mAnimationController.updateEmptyView(isUiRestrictedByOnlyAdmin());
    }

    private boolean checkWifiConnectivity() {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (mConnectivityManager != null) {
            NetworkInfo info = mConnectivityManager.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI);
            return info.isConnected();
        }
        return false;
    }

    private boolean checkWifiConnected() {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager)
                    getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (mConnectivityManager != null) {
            NetworkInfo info = mConnectivityManager.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI);
            if ((info.getDetailedState() == DetailedState.CAPTIVE_PORTAL_CHECK)
                    || (info.getDetailedState() == DetailedState.CONNECTED)) {
                return true;
            }
        }
        return false;
    }

    private WifiConfiguration getConfig(String quotedSsid, int keyMgmt) {
        if (quotedSsid == null) return null;
        List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
            if (configs != null) {
                for (WifiConfiguration config : configs) {
                    if (quotedSsid.equals(config.SSID) && config.allowedKeyManagement.get(keyMgmt)) {
                        return config;
                    }
                }
            }
        return null;
    }

    private void forceUpdateAPs() {
        setProgressBarVisible(true);
        mWifiTracker.forceUpdate();
        if (DEBUG) {
            Log.d(TAG, "WifiSettings force update APs: " + mWifiTracker.getAccessPoints());
        }

    }

    private void setupListDivider() {
        if (mAnimationController == null) {
            return;
        }

        mAnimationController.setListDivider(null);
        /*Resources resources = getResources();
        ListView listView = getViListView();
        int dividerInsetSize = resources.getDimensionPixelSize(R.dimen.wifi_list_item_start_padding)
                + resources.getDimensionPixelSize(R.dimen.list_app_icon_size);
        if (Utils.isRTL(getActivity())) {
            mAnimationController.setListDivider(new InsetDrawable(
                    listView.getDivider(), 0, 0, dividerInsetSize, 0));
        } else {
            mAnimationController.setListDivider(new InsetDrawable(
                    listView.getDivider(), dividerInsetSize, 0, 0, 0));
        }
        if (mInPickerDialog) {
            mAnimationController.setListDivider(resources.getDrawable(
                    R.drawable.wifi_list_divider_picker));
        }*/
    }

    /**
     * @return new WifiEnabler or null (as overridden by WifiSettingsForSetupWizard)
     */
    private WifiEnabler createWifiEnabler() {
        Activity activity = getActivity();
        if (activity instanceof SettingsActivity) {
            final SettingsActivity settingsActivity = (SettingsActivity) activity;
            return new WifiEnabler(settingsActivity, new SwitchBarController(settingsActivity.getSwitchBar()),
                mMetricsFeatureProvider);
        }
        return null;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        final Activity activity = getActivity();
        super.onResume();
        if (mWifiEnabler != null) {
            mWifiEnabler.resume();
        }

        //SHARED DEVICE START
        if (Settings.Secure.getInt(mContext.getContentResolver(),
                    "shared_device_status", 0) == 2) {
            Log.d(TAG, "isSharedDeviceKeyguardOn!");
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
        //SHARED DEVICE END

        //+SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
        Message msg = new Message();
        msg.what = WifiManager.SEC_COMMAND_ID_SET_WIFI_SCAN_WITH_P2P;
        Bundle args = new Bundle();
        args.putBoolean("enable", false);
        args.putBoolean("lock", true);
        msg.obj = args;
        if (mWifiManager.callSECApi(msg) == 0) {
            Log.d(TAG, "Stop p2p discovery after start legacy scan and assoc");
        }
        //-SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE

        // if (mInManageNetwork) {
        //     setPasspointPreferenceCategory();
        //     mAccessPointsPreferenceCategory.setTitle(R.string.wifi_access_points_manage);
        // } else {
        //     mPasspointPreferenceCategory.setVisible(false);
        // }

        //update ap immediately
        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            forceUpdateAPs();
        }

        // Bixby
        mEmSettingsManager.bindEmService(getContext(), mEmCallback, mBixbyCurrentStateId);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (mWifiEnabler != null) {
            mWifiEnabler.pause();
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (mWpsInProgress && isScreenOn && !Utils.SHOW_DETAILED_AP_INFO) {
            if (mWpsDialog != null) {
                mWpsDialog.dismiss();
                mWpsDialog = null;
            }
        }
        mCheckHttpResponseHandler.removeMessages(MSG_CHECK_GO_TO_WEBPAGE_HTTP_RESPONSE); //SEC_FLOATING_FEATURE_WLAN_SUPPORT_AP_LINK

        // Bixby
        mEmSettingsManager.clearEmService(mBixbyCurrentStateId);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        if (mWifiEnabler != null) {
            mWifiEnabler.stop();
            mWifiEnabler.setListener(null);
        }
        mWifiTracker.stopTracking();
        //getView().removeCallbacks(mHideProgressBarRunnable);
        mWifiSettingsForeground = false;
        super.onStop();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.WIFI;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If the dialog is showing, save its state.
        if (mDialog != null && mDialog.isShowing()) {
            outState.putInt(SAVE_DIALOG_MODE, mDialogMode);
            if (mDlgAccessPoint != null) {
                mAccessPointSavedState = new Bundle();
                mDlgAccessPoint.saveWifiState(mAccessPointSavedState);
                outState.putBundle(SAVE_DIALOG_ACCESS_POINT_STATE, mAccessPointSavedState);
            }
        }

        if (mWifiToNfcDialog != null && mWifiToNfcDialog.isShowing()) {
            Bundle savedState = new Bundle();
            mWifiToNfcDialog.saveState(savedState);
            outState.putBundle(SAVED_WIFI_NFC_DIALOG_STATE, savedState);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the user is not allowed to configure wifi, do not show the menu.
        if (mIsRestricted) {
            return;
        }

        if (mHideActionBarMenus) {
            return;
        } else if (mWifiDirect == null) {
            setHasOptionsMenu(true);
            setupActionBarMenus();

            ViewGroup toolbar = (ViewGroup) getActivity().getWindow().getDecorView().findViewById(
                getResources().getIdentifier("action_bar", "id", "android"));
            if (toolbar != null) {
                toolbar.setOnHierarchyChangeListener(mOnHierarchyChangeListener);
            }
        }

        if (!"VZW".equals(mOpBranding) && !mIsSupportedContactUs) {
            return;
        }

        Log.d(TAG, "create options menu");
        menu.add(Menu.NONE, MENU_ID_ADVANCED, 0, R.string.wifi_menu_advanced)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        if (mIsSupportedHelpHub
                || (SemCscFeature.getInstance().getInteger(
                        "CscFeature_Setting_ConfigTypeHelp", 1) == 1)
                    && Utils.isUSA()) {
            Log.d(TAG, "support help menu");
            menu.add(Menu.NONE, MENU_ID_HELP, 0, R.string.help_label)
                    .setIcon(R.drawable.header_btn_icon_help)
                    .setVisible(true)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        if (mIsSupportedContactUs) {
            Log.d(TAG, "support contact us menu");
            menu.add(Menu.NONE, MENU_ID_CONTACT_US, 0, R.string.contact_us_title)
                    .setVisible(true)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If the user is not allowed to configure wifi, do not handle menu selections.
        if (mIsRestricted) {
            return false;
        }

        switch (item.getItemId()) {
            case MENU_ID_ADVANCED:
                startWifiConfigSettings();
                return true;
            case MENU_ID_HELP:
                startWifiHelpScreen();
                return true;
            case MENU_ID_CONTACT_US:
                startContactUsActivity();
                return true;
            case MENU_ID_WPS_PBC:
                showDialog(WPS_PBC_DIALOG_ID);
                return true;
                /*
            case MENU_ID_P2P:
                startWifiP2pSettings();
                return true;
                */
            case MENU_ID_WPS_PIN:
                showDialog(WPS_PIN_DIALOG_ID);
                return true;
            case android.R.id.home:
                SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                        R.string.event_wifi_setting_up_button);
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "create context menu");
        if (mHideContextMenus) {
            return;
        }
        // if not working with expandable long-press, or if not child
        if (!(menuInfo instanceof ExpandableListContextMenuInfo)) return;

        final ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
        final int groupPosition = getViListView().getPackedPositionGroup(info.packedPosition);
        final int childPosition = getViListView().getPackedPositionChild(info.packedPosition);

        // Skip long-press on expandable parents
        if (childPosition == -1) return;

        //Preference preference = (Preference) getViListView().getItemAtPosition(
                   // ((AdapterContextMenuInfo) info).position);

        Preference preference = (Preference) getListAdapter().getChild(groupPosition,childPosition);
        if (preference instanceof LongPressAccessPointPreference) {
            SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                    R.string.event_wifi_setting_longpress_network_name);
            mSelectedAccessPoint =
                    ((LongPressAccessPointPreference) preference).getAccessPoint();
            menu.setHeaderTitle(mSelectedAccessPoint.getSsid());

            /** Bypass dialog for unsupported secured, unsaved networks , show toast for unsupported secured, saved networks */
            boolean isSupportedSecurity = mSelectedAccessPoint.isSupportedSecurityType();
            if (!isSupportedSecurity) {
                if (mSelectedAccessPoint.isSaved()) {
                    menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
                    Toast.makeText(getActivity(), R.string.wifi_unknown_secured_ap, Toast.LENGTH_LONG).show();
                }
                return;
            }

            if (mSelectedAccessPoint.isConnectable()) {
                menu.add(Menu.NONE, MENU_ID_CONNECT, 0, R.string.wifi_menu_connect);
            }

            WifiConfiguration config = mSelectedAccessPoint.getConfig();
            // Some configs are ineditable
            if (isEditabilityLockedDown(getActivity(), config)) {
                return;
            }

            if (mSelectedAccessPoint.isSaved() || mSelectedAccessPoint.isEphemeral()) {
                // Allow forgetting a network if either the network is saved or ephemerally
                // connected. (In the latter case, "forget" blacklists the network so it won't
                // be used again, ephemerally).
                if (Utils.REMOVABLE_DEFAULT_AP || !mSelectedAccessPoint.isVendorAp()) {//SEC_PRODUCT_FEATURE_WLAN_USE_DEFAULT_AP
                    menu.add(Menu.NONE, MENU_ID_FORGET, 0, R.string.wifi_menu_forget);
                }
            }
            if (mSelectedAccessPoint.isSaved() &&
                !(SemCscFeature.getInstance().getBoolean("CscFeature_Wifi_SupportEapAka") && "VerizonWiFiAccess".equals(mSelectedAccessPoint.getSsidStr())) ) {
                if (Utils.REMOVABLE_DEFAULT_AP || !mSelectedAccessPoint.isVendorAp()) { //SEC_PRODUCT_FEATURE_WLAN_USE_DEFAULT_AP
                    menu.add(Menu.NONE, MENU_ID_MODIFY, 0, R.string.wifi_menu_modify);
                }
                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
                if (mSelectedAccessPoint.isActive() && nfcAdapter != null && nfcAdapter.isEnabled() &&
                        mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_PSK) {
                    if (config != null && !config.allowedKeyManagement.get(KeyMgmt.FT_PSK)) { //SEC_PRODUCT_FEATURE_WLAN_SEC_SETTINGS_UX
                        // Only allow writing of NFC tags for password-protected networks.
                        menu.add(Menu.NONE, MENU_ID_WRITE_NFC, 0, R.string.wifi_menu_write_to_nfc);
                    }
                }
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mSelectedAccessPoint == null) {
            return super.onContextItemSelected(item);
        }
        switch (item.getItemId()) {
            case MENU_ID_CONNECT: {
                SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                        R.string.event_wifi_setting_context_connect_network);
                boolean isSavedNetwork = mSelectedAccessPoint.isSaved();
                if (isSavedNetwork) {
                    connect(mSelectedAccessPoint.getConfig(), isSavedNetwork);
                /* Always show wifi dialog
                } else if (mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE) {
                    /** Bypass dialog for unsecured networks
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    connect(mSelectedAccessPoint.getConfig(), isSavedNetwork);*/
                } else if (mSelectedAccessPoint.isWeChatAp()) { // WeChat Free Wi-Fi
                    connectWeChatAccessPoint(mSelectedAccessPoint.getSsid().toString(), mSelectedAccessPoint.getWeChatBssid(),
                        mSelectedAccessPoint.getRssi());
                } else {
                    showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_CONNECT);
                }
                return true;
            }
            case MENU_ID_FORGET: {
                SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                        R.string.event_wifi_setting_context_forget_network);
                forget();
                return true;
            }
            case MENU_ID_MODIFY: {
                SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                        R.string.event_wifi_setting_context_manage_network);
                showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_MODIFY);
                return true;
            }
            case MENU_ID_WRITE_NFC:
                SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                        R.string.event_wifi_setting_context_write_to_nfc_tag);
                showDialog(WRITE_NFC_DIALOG_ID);
                return true;

        }
        return super.onContextItemSelected(item);
    }

    private void addDeviceCategory(AccessPointPreferenceGroup preferenceGroup, int titleId, int index) {
        Log.d(TAG, "addDeviceCategory "+ getString(titleId));
        preferenceGroup.setTitle(getString(titleId));
        getListAdapter().addPreferenceGroup(index, preferenceGroup);
        getListAdapter().setAcessPointListGroup(preferenceGroup);
    }

    public AccessPointExpListAdapter getListAdapter(){
        return mAnimationController.getListAdapter();
    }

    public void onPassPointPreferenceClick(Preference preference) {
        Log.d(TAG, "onPassPointPreferenceClick");
        SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                R.string.event_wifi_setting_select_passpoint);
        String key = preference.getKey().toString();
        mSelectedPasspointConfig = mPasspointInfoMap.get(key);
        if(mSelectedPasspointConfig == null) {
            Log.e(TAG, "no matched any passpoint : " +key);
            return;
        }
        mSelectedPasspointFqdn = key;
        mSelectedpasspointName = preference.getTitle().toString();
        if (mSelectedPasspointConfig != null) {
            if (mSelectedPasspointConfig.getHomeSp() != null && mSelectedPasspointConfig.getHomeSp().isVendorSpecificSsid()) {
                showDialog(null, WifiConfigUiBase.MODE_PASSPOINT_PRELOADED);
            } else {
                showDialog(null, WifiConfigUiBase.MODE_PASSPOINT_REMOVABLE);
            }
        }
    }

    public boolean onAccessPointPreferenceClick(Preference preference) {
        Log.d(TAG, "onAccessPointPreferenceClick");
        /* If the preference has a fragment set, open that
        google code
        if (preference.getFragment() != null) {
            preference.setOnPreferenceClickListener(null);
            return super.onPreferenceTreeClick(preference);
        }*/

        if (preference instanceof LongPressAccessPointPreference) {
            SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                    R.string.event_wifi_setting_select_network_name);
            mSelectedAccessPoint = ((LongPressAccessPointPreference) preference).getAccessPoint();
            if (mSelectedAccessPoint == null) {
                return false;
            }

            if (mWifiPickerHelper != null && mWifiPickerHelper.userPickedAp(mSelectedAccessPoint)) {
                getActivity().setResult(Activity.RESULT_OK,
                        mWifiPickerHelper.getApIntent(mSelectedAccessPoint));
                popOrFinishThisActivity();
                return true;
            }

            if (isCoreanVendorAp(mSelectedAccessPoint)) { // SKT, KTT, LGU, ATO
                return true;
            }

            /** Bypass dialog for unsupported secured, unsaved networks , show toast for unsupported secured, saved networks */
            boolean isSupportedSecurity = mSelectedAccessPoint.isSupportedSecurityType();
            if (!isSupportedSecurity) {
                if (mSelectedAccessPoint.isSaved()) {
                    Toast.makeText(getActivity(), R.string.wifi_unknown_secured_ap, Toast.LENGTH_LONG).show();
                }
                return true;
            }

            WifiConfiguration config = mSelectedAccessPoint.getConfig();
            /**
             * Bypass dialog and connect to unsecured networks, or previously connected saved
             * networks, or Passpoint provided networks.
             */
            if (mSelectedAccessPoint.isSaved()) {
                // >>>WCM>>>
                if (mSelectedAccessPoint.getDetailedState() == DetailedState.CONNECTED
                        && config != null && config.isCaptivePortal && !config.isAuthenticated
                        && !mInManageNetwork) {
                    Log.i(TAG, "START captive portal login activity");
                    Intent intent = new Intent("android.net.netmon.launchCaptivePortalApp");
                    intent.putExtra("reason", 3); // SETTINGS = 3
                    getActivity().sendBroadcast(intent);
                    return true;
                }
                // <<<WCM<<<

                //check to match security type
                if (config != null && SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_11R) {
                    if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)
                            && mSelectedAccessPoint.isFTOnlyType()) {
                        Log.i(TAG, "it's FT/PSK only secured AP, Wi-Fi configuration should be changed");
                        showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_CONNECT);
                        return true;
                    } else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)
                            && mSelectedAccessPoint.isFTOnlyType()) {
                        Log.i(TAG, "it's FT/EAP only secured AP, Wi-Fi configuration should be changed");
                        showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_CONNECT);
                        return true;
                    }
                }

                if (mSelectedAccessPoint.isActive() || mInManageNetwork || "VZW".equals(mOpBranding)) {
                    //Remove Google UX //return super.onPreferenceTreeClick(preference);
                    showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_VIEW);
                } else {
                    connect(config, true /* isSavedNetwork */);
                }
            } else if (mSelectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE) {
                if (("VZW".equals(mOpBranding) || "ATT".equals(mOpBranding))
                        && !mSelectedAccessPoint.isActive()) {
                    showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_CONNECT);
                } else {
                    mSelectedAccessPoint.generateOpenNetworkConfig();
                    connect(mSelectedAccessPoint.getConfig(), mSelectedAccessPoint.isSaved());
                }
            } else if (mSelectedAccessPoint.isWeChatAp()) { // WeChat Free Wi-Fi
                connectWeChatAccessPoint(mSelectedAccessPoint.getSsid().toString(), mSelectedAccessPoint.getWeChatBssid(),
                    mSelectedAccessPoint.getRssi());
            } else if (mSelectedAccessPoint.isPasspoint()) {
                // Access point provided by an installed Passpoint provider, connect using
                // the associated config.
                connect(config, true /* isSavedNetwork */);
            } else {
                showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_CONNECT);
            }
        } else {
            if (preference == mAddPreference) {
                onAddNetworkPressed();
            /*} else if (preference == mCricketWifiPreference) {
                onCricketManagerClicked();*/
            } else {
                SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                        R.string.event_wifi_setting_select_passpoint);
                String key = preference.getKey().toString();
                mSelectedPasspointConfig = mPasspointInfoMap.get(key);
                mSelectedPasspointFqdn = key;
                mSelectedpasspointName = preference.getTitle().toString();
                if (mSelectedPasspointConfig.getHomeSp().isVendorSpecificSsid()) {
                    showDialog(null, WifiConfigUiBase.MODE_PASSPOINT_PRELOADED);
                } else {
                    showDialog(null, WifiConfigUiBase.MODE_PASSPOINT_REMOVABLE);
                }
            }
        }
        return true;

    }

    private boolean isCoreanVendorAp(AccessPoint ap) {
        WifiConfiguration config = ap.getConfig();
        if (config == null)
            return false;
        String ssid = ap.getSsid().toString();
        WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
        if (enterpriseConfig != null) {
            int method = enterpriseConfig.getEapMethod();
            if(method == WifiEnterpriseConfig.Eap.AKA) {
                if (isVendorSsid(ssid) && "SKT".equals(mOpBranding)) {
                    if (isUsimUseable() == false ) {
                        return true;
                    }
                } else if (isVendorSsid(ssid) && "KTT".equals(mOpBranding)) {
                    if (isUsimUseable() == false ) {
                        return true;
                    }
                } else if (isVendorSsid(ssid) && "LGU".equals(mOpBranding)) {
                    if (isUsimUseable() == false ) {
                        return true;
                    }
                } else if("UPC Wi-Free".equals(ssid) && "ATO".equals(mOpBranding)) {
                    if (isUsimUseable() == false ) {
                         return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isVendorSsid(String ssidStr) {
        String[] vendorSsids = Utils.CONFIG_VENDOR_SSID_LIST.split(",");
        for (String ssid : vendorSsids) {
            if (ssid.trim().equals(ssidStr)) return true;
        }
        return false;
    }

    // Samsung Corean Mobile Ux(SKT, KTT) and ATO
    private boolean isUsimUseable() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int simSate = telephonyManager.getSimState();
        boolean isAirplaneMode = Settings.Global.getInt(getActivity().getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

        if ("ATO".equals(mOpBranding)) {
            String imsi = telephonyManager.getSubscriberId();
            if (imsi != null && !imsi.startsWith("20601")) {
               Log.i(TAG, "ATO_USIM this mccmnc is not allowed");
               return false;
            } else {
                return true;
            }
        }

        if( simSate != TelephonyManager.SIM_STATE_READY && !isAirplaneMode ) {
            Toast.makeText(getActivity(), R.string.wifi_no_usim_warning, Toast.LENGTH_SHORT).show();
            return false;
        }

        if ("SKT".equals(mOpBranding)) {
           String imsi = telephonyManager.getSubscriberId();

           if(imsi != null && !imsi.startsWith("45005") && !imsi.startsWith("45000") ) {
               Toast.makeText(getActivity(), R.string.wifi_not_support_usim_warning, Toast.LENGTH_SHORT).show();
               return false;
           }
        } else if ("KTT".equals(mOpBranding)) {
          String imsi = telephonyManager.getSubscriberId();

          if(imsi != null && !imsi.startsWith("45008") && !imsi.startsWith("45002") ) {
                Toast.makeText(getActivity(), R.string.wifi_invalid_usim_warning, Toast.LENGTH_SHORT).show();
              return false;
          }
        }
        return true;
    }
    private void startScanningSettings() {
        if (getActivity() instanceof SettingsActivity) {
            //final SettingsActivity settingsActivity = (SettingsActivity) activity;
           ((SettingsActivity) getActivity()).startPreferencePanel(WifiSettings.this,
                    ScanningSettings.class.getName(),
                    null, R.string.security_settings_improve_accuracy_title, null, null, 0);
        } else {
            startFragment(this, ScanningSettings.class.getCanonicalName(),
                    R.string.security_settings_improve_accuracy_title, -1 /* Do not request a results */,
                    null);
        }
    }
    private void startWifiConfigSettings() {
        Log.d(TAG, "start config settings");
        SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                R.string.event_wifi_setting_advanced_option);
        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).startPreferencePanel(
                    this, ConfigureWifiSettings.class.getName(), null,
                    R.string.wifi_menu_advanced_button, null, null, 0);
        } else {
            startFragment(this, ConfigureWifiSettings.class.getCanonicalName(),
                    R.string.wifi_menu_advanced_button, -1 /* Do not request a results */,
                    null);
        }
    }

    private void startWifiP2pSettings() {
        Log.d(TAG, "start p2p settings");
        SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                R.string.event_wifi_setting_wifi_direct);
        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).startPreferencePanel(
                    this, WifiP2pSettings.class.getName(), null,
                    R.string.wifi_menu_p2p, null, null, 0);
        } else {
            startFragment(this, WifiP2pSettings.class.getCanonicalName(),
                    R.string.wifi_menu_p2p, -1, null);
        }
    }

    private void startContactUsActivity() {
        Log.d(TAG, "start contact us activity");
        SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                R.string.event_wifi_setting_contact_us_option);
        if (mIsSupportedContactUs) {
            String url = "voc://view/contactUs";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.putExtra("packageName", "com.android.settings.wifi");
            intent.putExtra("appId", "6u17f9w7m9");
            intent.putExtra("appName", getContext().getResources().getString(
                    R.string.wifi_settings_title));
            intent.putExtra("faqUrl", "voc://view/categories");

            startActivity(intent);
        }
    }

    private void startWifiHelpScreen() {
        Log.d(TAG, "start help activity");
        SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                R.string.event_wifi_setting_help_option);
        if (mIsSupportedHelpHub) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo("com.samsung.helphub", 0);
                Log.d(TAG, "onHelpMenuPressed : " + info.versionCode);
                if (info.versionCode%10 == 1) {
                    //nothing to do
                } else if (info.versionCode%10 == 2) {
                    Intent intent = new Intent("com.samsung.helphub.HELP");
                    intent.putExtra("helphub:section", "wifi");
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (info.versionCode%10 == 3) {
                    Intent intent = new Intent("com.samsung.helphub.HELP");
                    intent.putExtra("helphub:appid", "wi_fi");
                    startActivity(intent);
                }
            } catch (NameNotFoundException e) {
                Log.e(TAG, "can't start helphub activity");
            } catch (Exception e) {
                Log.e(TAG, "can't start helphub activity");
            }
        } else {
            if (getActivity() instanceof SettingsActivity) {
                ((SettingsActivity) getActivity()).startPreferencePanel(
                        this, WifiHelpPage.class.getName(), null,
                        R.string.item_wifi_connect_on_device_help, null, null, 0);
            /*} else if (getActivity() instanceof PreferenceActivity) {
            ((PreferenceActivity) getActivity()).startPreferencePanel(
                WifiHelpPage.class.getName(), null,
                R.string.item_wifi_connect_on_device_help, null, null, 0);*/
            } else {
                startFragment(this, WifiHelpPage.class.getCanonicalName(),
                        R.string.item_wifi_connect_on_device_help, -1, null);
            }
        }
    }

    private void showDialog(AccessPoint accessPoint, int dialogMode) {
        if (accessPoint != null) {
            WifiConfiguration config = accessPoint.getConfig();
            if (isEditabilityLockedDown(getActivity(), config) && accessPoint.isActive()) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(),
                        RestrictedLockUtils.getDeviceOwner(getActivity()));
                return;
            }
        }

        if (mDialog != null) {
            removeDialog(WIFI_DIALOG_ID);
            mDialog = null;
        }

        // Save the access point and edit mode
        mDlgAccessPoint = accessPoint;
        mDialogMode = dialogMode;

        showDialog(WIFI_DIALOG_ID);
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case WIFI_DIALOG_ID:
                AccessPoint ap = mDlgAccessPoint; // For manual launch
                if (ap == null) { // For re-launch from saved state
                    if (mAccessPointSavedState != null) {
                        ap = new AccessPoint(getActivity(), mAccessPointSavedState);
                        // For repeated orientation changes
                        mDlgAccessPoint = ap;
                        // Reset the saved access point data
                        mAccessPointSavedState = null;
                    }
                }
                // If it's null, fine, it's for Add Network
                mSelectedAccessPoint = ap;
                if (mDialogMode == WifiConfigUiBase.MODE_PASSPOINT_PRELOADED || mDialogMode == WifiConfigUiBase.MODE_PASSPOINT_REMOVABLE) {
                    Bundle args = new Bundle();
                    args.putString("fqdn", mSelectedPasspointFqdn);
                    args.putString("name", mSelectedpasspointName);
                    args.putInt(WifiConfigController.ARGS_SCREEN_ID, mSAScreenId);
                    mDialog = new WifiDialog(getActivity(), mDialogListener, ap, mDialogMode,
                        /* no hide submit/connect */ false, args);
                } else {
                    mDialog = new WifiDialog(getActivity(), mDialogListener, ap, mDialogMode,
                        /* no hide submit/connect */ false, getWifiDialogArgs(ap));
                }
                return mDialog;

            case WPS_PBC_DIALOG_ID:
                mWpsDialog = new WpsDialog(getActivity(), WpsInfo.PBC);
                return mWpsDialog;

            case WPS_PIN_DIALOG_ID:
                mWpsDialog = new WpsDialog(getActivity(), WpsInfo.DISPLAY);
                return mWpsDialog;

            case WRITE_NFC_DIALOG_ID:
                if (mSelectedAccessPoint != null) {
                    mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(
                            getActivity(),
                            mSelectedAccessPoint.getSecurity(),
                            new WifiManagerWrapper(mWifiManager));
                } else if (mWifiNfcDialogSavedState != null) {
                    mWifiToNfcDialog = new WriteWifiConfigToNfcDialog(getActivity(),
                            mWifiNfcDialogSavedState, new WifiManagerWrapper(mWifiManager));
                }
                return mWifiToNfcDialog;
        }
        return super.onCreateDialog(dialogId);
    }

    @Override
    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case WIFI_DIALOG_ID:
                return MetricsEvent.DIALOG_WIFI_AP_EDIT;
            case WPS_PBC_DIALOG_ID:
                return MetricsEvent.DIALOG_WIFI_PBC;
            case WPS_PIN_DIALOG_ID:
                return MetricsEvent.DIALOG_WIFI_PIN;
            case WRITE_NFC_DIALOG_ID:
                return MetricsEvent.DIALOG_WIFI_WRITE_NFC;
            default:
                return 0;
        }
    }

    private void dismissDialog(int id) {
        switch(id) {
        case WIFI_DIALOG_ID:
            if (isWifiQr && mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
                break;
            }
            if (mDialog != null) {
                mDialog.dismiss();
                removeDialog(WIFI_DIALOG_ID);
                mDialog = null;
            }
             break;
        }
    }

    /**
     * Called to indicate the list of AccessPoints has been updated and
     * getAccessPoints should be called to get the latest information.
     */
    @Override
    public void onAccessPointsChanged() {

    }

    //WifiTracker.SemWifiListener
    /**
     * Shows the latest access points available with supplemental information like
     * the strength of network and the security for it.
     */
    @Override
    public void onConfiguredNetworksChanged(WifiConfiguration config, boolean isMultipleChanged, int reason) {
        if (DBG) Log.d(TAG, "onConfiguredNetworksChanged: config = " + ((config == null) ? "null" : config.configKey())
                    + ", isMultipleChanged = " + isMultipleChanged);
        int wifiState = mWifiTracker.getWifiState();
        // in case state has changed
        if (wifiState == WifiManager.WIFI_STATE_DISABLED
                || wifiState == WifiManager.WIFI_STATE_DISABLING
                || wifiState == WifiManager.WIFI_STATE_UNKNOWN) {
            Log.d(TAG, "onConfiguredNetworksChanged - "
                    + "Wi-Fi state is disabling/disabled " + wifiState);
            return;
        } else if (mWifiEnabler != null && !mWifiEnabler.isSwitchBarChecked()) {
            Log.d(TAG, "onConfiguredNetworksChanged - WifiEnabler is not checked");
            return;
        }
        if (config == null) return;
        if (isMultipleChanged) return;

        if (config.isVendorSpecificSsid) {
            if (DBG) Log.d(TAG, "Failed to show retry popup. It's default AP");
        } else if (config.getAuthType() == KeyMgmt.NONE) {
            if (DBG) Log.d(TAG, "Failed to show retry popup. It's none secured (OPEN) AP");
        } else {
            WifiConfiguration.NetworkSelectionStatus networkStatus = config.getNetworkSelectionStatus();
            if (!networkStatus.isNetworkEnabled()) {
                int disableReason = networkStatus.getNetworkSelectionDisableReason();
                if (disableReason == WifiConfiguration.NetworkSelectionStatus
                            .DISABLED_AUTHENTICATION_FAILURE
                        || disableReason == WifiConfiguration.NetworkSelectionStatus
                            .DISABLED_AUTHENTICATION_NO_CREDENTIALS) {
                    if (config.enterpriseConfig != null
                            && config.enterpriseConfig.getEapMethod() != WifiEnterpriseConfig.Eap.NONE
                            && reason != WifiManager.CHANGE_REASON_REMOVED) {
                        Toast.makeText(getActivity(), R.string.wifi_eap_popup_error, Toast.LENGTH_LONG).show();
                    }
                    if (DBG) Log.d(TAG, "onConfiguredNetworksChanged: mShowRetryDialog = " + mShowRetryDialog);
                    if (mShowRetryDialog) {
                        showDialogForRetry(findAccessPoint(config));
                        mShowRetryDialog = false;
                    }
                }
            }
        }
    }
    @Override
    public void onScanStateChange(int state) {
        if (state == WifiTracker.TYPE_SCAN_STATE_REQUESTED) {
            int wifiState = mWifiTracker.getWifiState();
            if (wifiState != WifiManager.WIFI_STATE_DISABLED) {
                setProgressBarVisible(true);
            }
        }
    }
    @Override
    public void onAccessPointsChanged(boolean updatedScanResult) {
        updateAccessPointPreferences(updatedScanResult);
    }

    /** Called when the state of Wifi has changed. */
    @Override
    public void onWifiStateChanged(int wifiState) {
        Log.d(TAG, "onWifiStateChanged wifiState is "+ wifiState);
        if (mIsRestricted) {
            return;
        }
        mPreWifiState = wifiState;
        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                forceUpdateAPs();
                updateDirectMenu(wifiState);

                if (mWillRespondToEm) {
                    if ("WiFiScan".equals(mEmLastStateID)) {
                        forceScanByBixby();
                    }

                    mWillRespondToEm = false;
                }

                if (mQrConfig != null) {
                    saveConfig(mQrConfig);
                    connect(mQrConfig, false);
                    mQrConfig = null;
                }

                break;

            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(R.string.wifi_starting);
                setProgressBarVisible(true);
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                if (mFinishIfWifiDisabled) {
                    popOrFinishThisActivity();
                    return;
                }
                addMessagePreference(R.string.wifi_stopping);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                setVisiblePreEmptyView(true);
                if (mIsSupportedContactUs) {
                    setVisibleContactUs(true);
                }
                if (mFinishIfWifiDisabled) {
                    popOrFinishThisActivity();
                    return;
                }
                dismissDialog(WIFI_DIALOG_ID);
                setOffMessage();
                setProgressBarVisible(false);
                updateDirectMenu(wifiState);

                //VZW Wifi Offload
                if (mInOffloadDialog) {
                    if (SemCscFeature.getInstance().getBoolean(
                            CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTHUXWIFIPROMPTDATAOVERUSE)) {
                        getActivity().finish();
                        return;
                    }
                }

                break;

            default: //unknown state
                break;
        }
    }

    private void popOrFinishThisActivity() {
        if (Utils.isTablet() && getFragmentManager() != null
                && getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            getActivity().finish();
        }
    }

    /**
     * Called when the connection state of wifi has changed and isConnected
     * should be called to get the updated state.
     */
    @Override
    public void onConnectedChanged() {
        boolean isConnected = mWifiTracker.isConnected();
         Log.d(TAG, "onConnectedChanged: isConnected:" + isConnected);
        if (isConnected) {
            mShowRetryDialog = false;
            if (mWifiPickerHelper != null && mWifiPickerHelper.isUserPickedAp()) {
                getActivity().setResult(Activity.RESULT_OK,
                        mWifiPickerHelper.getApIntent(null));
                popOrFinishThisActivity();
                return;
            }
        }

        //VZW Wifi Offload
        if (mWifiTracker.isConnected() && mInOffloadDialog) {
            if (SemCscFeature.getInstance().getBoolean(
                    CscFeatureTagCommon.TAG_CSCFEATURE_COMMON_SUPPORTHUXWIFIPROMPTDATAOVERUSE)) {
                getActivity().finish();
                return;
            }
        }

        if (mFinishIfConnected && isConnected) {
            popOrFinishThisActivity();
            return;
        }

        //SEC_FLOATING_FEATURE_WLAN_SUPPORT_AP_LINK
        if (isConnected){
            mCheckHttpResponseHandler.sendMessageDelayed(
                    mCheckHttpResponseHandler.obtainMessage(
                        MSG_CHECK_GO_TO_WEBPAGE_HTTP_RESPONSE), 1000);
        } else {
            goToWebPageHTTPResponse = 0;
        }

        changeNextButtonState(isConnected);
    }

    private void updateAccessPointPreferences(boolean updatedScanResult) {
        if (mIsRestricted) {
            Log.d(TAG, "updateAccessPointPreferences - UI restricted");
            setProgressBarVisible(false);
            showRestrictionEmptyView();
            return;
        }
        // Safeguard from some delayed event handling
        if (getActivity() == null
                || mPreWifiState != WifiManager.WIFI_STATE_ENABLED) {
            setProgressBarVisible(false);
            Log.d(TAG, "updateAccessPointPreferences - Wi-Fi is not enabled. prevState:" + mPreWifiState);
            return;
        }
        // Dialog is showing
        if (mDialog != null && mDialog.isShowing()) {
            Log.d(TAG, "updateAccessPointPreferences - dialog is showing.");
        }

        int wifiState = mWifiTracker.getWifiState();
        // in case state has changed
        if (wifiState == WifiManager.WIFI_STATE_DISABLED
                || wifiState == WifiManager.WIFI_STATE_DISABLING
                || wifiState == WifiManager.WIFI_STATE_UNKNOWN) {
            if(wifiState == WifiManager.WIFI_STATE_DISABLED) {
                setProgressBarVisible(false);
            }
            Log.d(TAG, "updateAccessPointPreferences - "
                    + "Wi-Fi state is disabling/disabled " + wifiState);
            return;
        } else if (mWifiEnabler != null && !mWifiEnabler.isSwitchBarChecked()) {
            setProgressBarVisible(false);
            mAnimationController.removeAll();
            addMessagePreference(R.string.wifi_stopping);
            return;
        }

        setProgressBarVisible(true);
        // AccessPoints are sorted by the WifiTracker
        final List<AccessPoint> accessPoints = mWifiTracker.getAccessPoints();
        ArrayList<AccessPointPreference> newList = null;
        boolean hasAvailableAccessPoints = false;
        int index = 0;
        newList = new ArrayList<AccessPointPreference>();
        if(mAccessPointsPreferenceGroup == null) {
            mAccessPointsPreferenceGroup  = new AccessPointPreferenceGroup((Context)getActivity());
        }
        int numAccessPoints = accessPoints.size();
        if(numAccessPoints == 0) {
            getListAdapter().removeAll();
        } else {
            if (mInManageNetwork) {
                addDeviceCategory(mAccessPointsPreferenceGroup, R.string.wifi_access_points_manage, 0);
            } else {
                addDeviceCategory(mAccessPointsPreferenceGroup, R.string.wifi_preference_availavle_networks, 0);
            }
        }
        index = configureConnectedAccessPointPreferenceCategory(accessPoints) ? 1 : 0;
        //Scanned Networks
        if(index != 0) hasAvailableAccessPoints = true;

        Log.d(TAG, "updateAccessPointPreferences size:" + numAccessPoints
                + " connected:" + index + " scan:" + updatedScanResult);
        for (; index < numAccessPoints; index++) {
            AccessPoint accessPoint = accessPoints.get(index);
            // Ignore access points that are out of range.
            if (accessPoint.isReachable() || mInManageNetwork) {
                String key = accessPoint.getBssid();
                if (TextUtils.isEmpty(key)) {
                    key = accessPoint.getSsidStr()
                            + (accessPoint.getNetworkId())
                            + (accessPoint.isPasspoint() ? "1" : "0" );
                }

                hasAvailableAccessPoints = true;
                //LongPressAccessPointPreference pref = (LongPressAccessPointPreference) getCachedPreference(key);
                //if (pref instanceof LongPressAccessPointPreference) {
                    //if (pref != null) {
                   //     pref.setOrder(index);
                   //     continue;
                   // }
                    LongPressAccessPointPreference preference =
                            createLongPressActionPointPreference(accessPoint);
                    preference.setKey(key);
                    preference.setOrder(index);

                    if (mOpenSsid != null && mOpenSsid.equals(accessPoint.getSsidStr())
                            && accessPoint.getSecurity() == mOpenSecurity && !mInManageNetwork) {
                        onAccessPointPreferenceClick(preference);
                        mOpenSsid = null;
                        mGoToSettingsFromQuickPanelFirstTime = false;
                    }

                    newList.add(preference);
                //}
            }
        }
        boolean hasPassPoints = false;
        if(mInManageNetwork) {
          hasPassPoints = setPasspointPreferenceCategory();
        }

        //Searching Text (if scan result is not ready or empty)

        if (!hasAvailableAccessPoints) {
            if (mInManageNetwork) {
                setProgressBarVisible(false);
                if(!hasPassPoints) {
                    if ("VZW".equals(Utils.CONFIG_OP_BRANDING)) {
                        addMessagePreference(R.string.wifi_manage_network_no_aps);
                    } else {
                        addMessagePreference(R.string.wifi_no_accesspoint_found);
                    }
                }
            } else {
                addDeviceCategory(mAccessPointsPreferenceGroup, R.string.wifi_preference_availavle_networks, 0);
                setProgressBarVisible(true);
                LongPressAccessPointPreference pref = new LongPressAccessPointPreference(null,
                    getPrefContext(), mUserBadgeCache, false, this);
                pref.setSelectable(false);
                pref.setOrder(0);
                pref.setTitle(R.string.wifi_empty_list_wifi_on);
                pref.setKey(PREF_KEY_EMPTY_WIFI_LIST);
                newList.add(pref);
                mAnimationController.updateWithAnimation(newList,false);
                mAnimationController.updateEmptyView();
            }
        } else {
            if (updatedScanResult && !mInManageNetwork) {
                mAnimationController.updateWithAnimation(newList,true);
            } else {
                mAnimationController.updateWithAnimation(newList,false);
            }
            mAnimationController.updateEmptyView();
            setProgressBarVisible(false);
            // Continuing showing progress bar for an additional delay to overlap with animation
            //getView().postDelayed(mHideProgressBarRunnable, SCAN_PROGRESS_DELAY /* delay millis */);
        }

        if (mScrollTimer != 0) {
            forceScrollToTopOfList();
        }

    }

    private void forceScrollToTopOfList() {
        if (SystemClock.currentThreadTimeMillis() - mScrollTimer > 1500) {
            if (DBG) Log.d(TAG, "scroll end");
            mScrollTimer = 0;
        } else if (getViListView() != null && getViListView().getCount() > 0) {
            if (DBG) Log.d(TAG, "force scroll up");
            getViListView().setSelection(0);
/*            if (mIsSupportedCricketManager) {
                scrollToPreference("wifi_cricket_manager");
            } else if (mConnectedAccessPointPreferenceCategory.getPreferenceCount() != 0) {
                scrollToPreference("connected_access_point");
            } else {
                scrollToPreference("access_points");
            }
*/
        }
    }

    @NonNull
    private LongPressAccessPointPreference createLongPressActionPointPreference(
            AccessPoint accessPoint) {
        return new LongPressAccessPointPreference(accessPoint, getPrefContext(), mUserBadgeCache,
                false, R.drawable.ic_wifi_signal_0, this);
    }

    /**
     * Configure the ConnectedAccessPointPreferenceCategory and return true if the Category was
     * shown.
     */
    private boolean configureConnectedAccessPointPreferenceCategory(
            List<AccessPoint> accessPoints) {

        if (accessPoints.size() == 0) {
            if(!mInManageNetwork) {
                getListAdapter().removeAll();
            } else {
                removeConnectedAccessPointPreference();
                if(getListAdapter().getAvailableAccesspointGroup() != null) {
                    getListAdapter().getAvailableAccesspointGroup().removeAll();
                }
            }
            return false;
        }

        AccessPoint connectedAp = accessPoints.get(0);
        if (!connectedAp.isConnected() && mConnectedAccessPointPreferenceGroup != null) {
            removeConnectedAccessPointPreference();
            return false;
        }

        if (mOpenSsid != null && mOpenSsid.equals(connectedAp.getSsidStr())
                && connectedAp.getSecurity() == mOpenSecurity && !mInManageNetwork) {
            onAccessPointPreferenceClick(createLongPressActionPointPreference(connectedAp));
            mOpenSsid = null;
            mGoToSettingsFromQuickPanelFirstTime = false;
        }

        // Is the preference category empty?
        if (mAnimationController.getConnectedAPCount() == 0) {
            addConnectedAccessPointPreference(connectedAp);
            return true;
        }
        if(getListAdapter().getConnectedAccesspointGroup() == null) {
            return false;
        }
        AccessPointPreference connectedAccessPointPreference = (AccessPointPreference) getListAdapter().getConnectedAccesspointGroup().getPreference(0);
        // Is the previous currently connected SSID different from the new one?
        if (connectedAccessPointPreference!= null &&
                    !connectedAccessPointPreference.getAccessPoint().getSsidStr().equals(
                    connectedAp.getSsidStr())) {
            Log.d(TAG, "remove and add connected accesspointpreference");
            removeConnectedAccessPointPreference();
            addConnectedAccessPointPreference(connectedAp);
            return true;
        }
        if (connectedAccessPointPreference!= null &&
                    connectedAccessPointPreference.getAccessPoint().isWeChatAp() !=
                    connectedAp.isWeChatAp()) {
            Log.d(TAG, "remove and add connected wechat accesspointpreference");
            removeConnectedAccessPointPreference();
            addConnectedAccessPointPreference(connectedAp);
        }
        // Else same AP is connected, simply refresh the connected access point preference
        // (first and only access point in this category).
        if(connectedAccessPointPreference != null) {
            connectedAccessPointPreference.refresh();
        }
        return true;
    }

    private boolean setPasspointPreferenceCategory() { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
        if(mPassPointPreferenceGroup != null) {
            mPassPointPreferenceGroup.removeAll();
        } else {
            return false;
        }
        addDeviceCategory(mPassPointPreferenceGroup, R.string.wifi_preference_passpoint_networks, getListAdapter().getGroupCount());
        mPasspointInfoMap.clear();
        mPasspointConfigList = mWifiManager.getPasspointConfigurations();
        int passpointConfigSize = mPasspointConfigList.size();

        if (mInManageNetwork && passpointConfigSize > 0) {
            for (int i = 0 ; i < passpointConfigSize; i++) {
                PasspointConfiguration passpoint = mPasspointConfigList.get(i);
                if (!passpoint.getHomeSp().getFriendlyName().equals(VENDOR_FRIENDLY_NAME)) {
                    String fqdn = passpoint.getHomeSp().getFqdn();
                    mPasspointInfoMap.put(fqdn, passpoint);
                    //Preference pref = new Preference(getPrefContext());
                    LongPressAccessPointPreference pref = new LongPressAccessPointPreference(null,
                        getPrefContext(), mUserBadgeCache, true, this);
                    pref.setOrder(i);
                    Log.d(TAG, "passpoint FriendlyName" + passpoint.getHomeSp().getFriendlyName());
                    pref.setPasspointTitle(passpoint.getHomeSp().getFriendlyName());
                    pref.setKey(passpoint.getHomeSp().getFqdn());
                    if(pref != null && getListAdapter().getPasspointPreferenceGroup() != null) {
                        getListAdapter().getPasspointPreferenceGroup().insert(pref, i);
                    }
                }
            }
            getListAdapter().notifyDataSetChanged();

            if (mPasspointInfoMap.size() <= 0) {
                if(getListAdapter().getPasspointPreferenceGroup() != null) {
                    getListAdapter().getPasspointPreferenceGroup().removeAll();
                }
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Creates a Preference for the given {@link AccessPoint} and adds it to the
     * {@link #mConnectedAccessPointPreferenceCategory}.
     */
    private void addConnectedAccessPointPreference(AccessPoint connectedAp) {
        addDeviceCategory(mConnectedAccessPointPreferenceGroup, R.string.wifi_preference_connected_network, 0);
        String key = connectedAp.getSsidStr()+ connectedAp.getSecurity() + connectedAp.isPasspoint();
        LongPressAccessPointPreference pref = createLongPressActionPointPreference(connectedAp);
        pref.setKey(key);
        pref.setOrder(0);
        pref.getAccessPoint().saveWifiState(pref.getExtras());
        if(getListAdapter().getConnectedAccesspointGroup() != null) {
            getListAdapter().getConnectedAccesspointGroup().removeAll();
            getListAdapter().getConnectedAccesspointGroup().insert(pref, 0);
            if(getListAdapter().getAvailableAccesspointGroup().getPreference(0) != null
                    && ((AccessPointPreference)getListAdapter().getAvailableAccesspointGroup().getPreference(0)).getAccessPoint() != null) {
                if (((AccessPointPreference)
                        getListAdapter().getAvailableAccesspointGroup().getPreference(0)).getAccessPoint().matches(connectedAp)) {
                    getListAdapter().getAvailableAccesspointGroup().removeAt(0);
                }
            }
        }
    }

    /** Removes all preferences and hide the {@link #mConnectedAccessPointPreferenceCategory}. */
    private void removeConnectedAccessPointPreference() {
        if(getListAdapter().getConnectedAccesspointGroup() != null)
        getListAdapter().getConnectedAccesspointGroup().removeAll();
        //getListAdapter().removePreferenceGroup(mConnectedAccessPointPreferenceGroup);
    }

    private void setupActionBarMenus() {
        final Activity activity = getActivity();
        final ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.wifi_settings_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.END | Gravity.CENTER_VERTICAL);
            View customLayout = activity.getLayoutInflater().inflate(
                    R.layout.wifi_custom_actionbar, null);
            mWifiDirect = (TextView) customLayout.findViewById(R.id.wifi_direct);
            mWifiAdvanced = (TextView) customLayout.findViewById(R.id.wifi_advanced);
            if ("VZW".equals(mOpBranding) || mIsSupportedContactUs) {
                Log.d(TAG, "hide advanced menu on actionbar");
                mWifiAdvanced.setVisibility(View.GONE);
            }

            if ((actionBar.getCustomView() == null) || (actionBar.getCustomView().findViewById(R.id.wifi_direct) == null)) {
                actionBar.setDisplayShowCustomEnabled(true);
                actionBar.setCustomView(customLayout, layoutParams);
            }

            TypedValue outValue = new TypedValue();
            activity.getTheme().resolveAttribute(com.android.internal.R.attr.parentIsDeviceDefault,
                    outValue, true);

            float defaultTextSize = 15;
            float maxFontScale = 1.2f;
            float curFontScale = getContext().getResources().getConfiguration().fontScale;

            if (curFontScale > maxFontScale) {
                curFontScale = maxFontScale;
            }
            boolean isThemeDeviceDefaultFamily = outValue.data != 0;
            int backgroundResId = 0;
            int backgroundDefaultResId = com.android.internal.R.drawable
                    .sem_action_item_with_button_background_light;
            if (/*SHOW_BUTTON_BACKGROUND &&*/ isThemeDeviceDefaultFamily) {
                TypedArray av = activity.obtainStyledAttributes(null,
                        com.android.internal.R.styleable.View,
                        com.android.internal.R.attr.actionButtonStyle, 0);
                backgroundResId = av.getResourceId(com.android.internal.R.styleable.View_background,
                        com.android.internal.R.drawable.sem_action_item_background_borderless_material);
                av.recycle();
            }

            if (mWifiDirect != null) {
                mWifiDirect.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                        (float)(defaultTextSize * curFontScale));

                if (/*SHOW_BUTTON_BACKGROUND &&*/ isThemeDeviceDefaultFamily) {
                    if (Settings.System.getInt(getContext().getContentResolver(),
                            Settings.System.SHOW_BUTTON_BACKGROUND, 0) == 1) {
                        mWifiDirect.setBackgroundResource(backgroundDefaultResId);
                    }
                    else {
                        mWifiDirect.setBackgroundResource(backgroundResId);
                    }
                }

                updateContentDescriptionForDirectMenu(false);
                mWifiDirect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startWifiP2pSettings();
                    }
                });
            }

            if(mWifiAdvanced != null) {
                mWifiAdvanced.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
                        (float)(defaultTextSize * curFontScale));

                if (/*SHOW_BUTTON_BACKGROUND &&*/ isThemeDeviceDefaultFamily) {
                    if (Settings.System.getInt(getContext().getContentResolver(),
                            Settings.System.SHOW_BUTTON_BACKGROUND, 0) == 1) {
                        mWifiAdvanced.setBackgroundResource(backgroundDefaultResId);
                    } else {
                        mWifiAdvanced.setBackgroundResource(backgroundResId);
                    }
                }
                updateContentDescriptionForAdvancedMenu(false);
                mWifiAdvanced.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startWifiConfigSettings();
                    }
                });
            }

            if (mInManageNetwork) {
                actionBar.setTitle(R.string.wifi_manage_network);
            }

            updateDirectMenu(mWifiTracker.getWifiState());
        }
    }

    private void updateDirectMenu(int wifiState) {
        if (mWifiDirect != null) {
            if (!WifiDevicePolicyManager.isAllowedWifiDirectEnabled(getActivity())
                    || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
                Log.i(TAG, "not allowed Wi-Fi direct. disable Wi-Fi direct menu");
                mWifiDirect.setClickable(false);
                mWifiDirect.setEnabled(false);
                updateContentDescriptionForDirectMenu(false);
            } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                mWifiDirect.setClickable(true);
                mWifiDirect.setEnabled(true);
                updateContentDescriptionForDirectMenu(true);
                updateContentDescriptionForAdvancedMenu(true);
            } else if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                mWifiDirect.setClickable(false);
                mWifiDirect.setEnabled(false);
                updateContentDescriptionForDirectMenu(false);
                updateContentDescriptionForAdvancedMenu(false);
            }
        }
    }

    private OnHierarchyChangeListener mOnHierarchyChangeListener = new OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(final View parent, final View child) {
            Log.d(TAG, "onChildViewAdded() :: parent - " + parent);
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup)parent;
                for (int i=0; i < viewGroup.getChildCount(); i++) {
                    View titleView = viewGroup.getChildAt(i);
                    Log.d(TAG, "actionbar :: titleView - " + titleView);
                    if (titleView instanceof TextView) {
                        Log.d(TAG, "setallcaps false");
                        ((TextView) titleView).setAllCaps(false);
                    }
                }
            }
        }
        @Override
        public void onChildViewRemoved(View parent, View child) {
        }
    };

    private void updateContentDescriptionForDirectMenu(boolean enable) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (mWifiDirect != null) {
            String wifiDirectMenuTts = "";
            if (enable) {
                wifiDirectMenuTts = activity.getString(R.string.wifi_menu_p2p)
                        + " " + activity.getString(R.string.button_tts);
            } else {
                wifiDirectMenuTts = activity.getString(R.string.wifi_menu_p2p)
                        + " " + activity.getString(R.string.button_tts)
                        + " " + activity.getString(R.string.value_disabled);
            }
            mWifiDirect.setContentDescription(wifiDirectMenuTts);
        }
    }

    private void updateContentDescriptionForAdvancedMenu(boolean enable) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (mWifiAdvanced != null) {
            String wifiAdvancedMenuTts = "";
            if (enable) {
                wifiAdvancedMenuTts = activity.getString(R.string.wifi_menu_advanced_button)
                        + " " + activity.getString(R.string.button_tts);
            } else {
                wifiAdvancedMenuTts = activity.getString(R.string.wifi_menu_advanced_button)
                        + " " + activity.getString(R.string.button_tts)
                        + " " + activity.getString(R.string.value_disabled);
            }
            mWifiAdvanced.setContentDescription(wifiAdvancedMenuTts);
        }
    }


    private void setOffMessage() {
        TextView emptyTextView = getEmptyTextView();
        if (emptyTextView == null) {
            return;
        }
        setProgressBarVisible(false);
        // Don't use WifiManager.isScanAlwaysAvailable() to check the Wi-Fi scanning mode. Instead,
        // read the system settings directly. Because when the device is in Airplane mode, even if
        // Wi-Fi scanning mode is on, WifiManager.isScanAlwaysAvailable() still returns "off".
        final StringBuilder contentBuilder = new StringBuilder();
        final boolean wifiScanningMode = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0) == 1;
        final CharSequence description = wifiScanningMode ? getText(R.string.wifi_scan_notify_text)
                : getText(R.string.wifi_scan_notify_text_scanning_off);
        Log.d(TAG, "showOffNessage " + description);
        contentBuilder.append(description);
        LinkifyUtils.linkify(emptyTextView, contentBuilder, new LinkifyUtils.OnClickListener() {
            @Override
            public void onClick() {
                startScanningSettings();
            }
        });

        if(mIsSupportedContactUs) {
            contactUsView = mAnimationController.getContactUsView();
            if(contactUsView == null) return;
            StringBuilder contactUsBuilder = new StringBuilder();
            CharSequence content = getText(R.string.wifi_empty_view_contact_us_description);
            contactUsBuilder.append(content);
            LinkifyUtils.linkify(contactUsView, contactUsBuilder, new LinkifyUtils.OnClickListener() {
                @Override
                public void onClick() {
                    startContactUsActivity();
                }
            });
            setTextBoldSpan(contactUsView.getText(), new StringBuilder(content));
        }

        mAnimationController.removeAll();
        mAnimationController.updateEmptyView();

        emptyTextView.setLineSpacing(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX, 4.0f,  getResources().getDisplayMetrics()), 1.0f);
        setTextBoldSpan(emptyTextView.getText(), new StringBuilder(description));
    }

    private void setTextBoldSpan(CharSequence text, StringBuilder briefText) {
        if (text instanceof Spannable) {
            String PLACE_HOLDER_LINK_BEGIN = "LINK_BEGIN";
            String PLACE_HOLDER_LINK_END = "LINK_END";

            Spannable boldSpan = (Spannable) text;

            int beginIndex = briefText.indexOf(PLACE_HOLDER_LINK_BEGIN);
            briefText.delete(0, beginIndex + PLACE_HOLDER_LINK_BEGIN.length());
            int endIndex = briefText.indexOf(PLACE_HOLDER_LINK_END);
            briefText.delete(endIndex, briefText.length());

            int briefIndex = new StringBuilder(text).indexOf(briefText.toString());
            boldSpan.setSpan(
                new StyleSpan(android.graphics.Typeface.BOLD), briefIndex,
                briefIndex + briefText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void addMessagePreference(int messageId) {
        Log.d(TAG, "addMessagePreference id:" + messageId);

        TextView emptyTextView = getEmptyTextView();

        if (emptyTextView != null) {
            emptyTextView.setText(messageId);
        }
         if (messageId == R.string.wifi_starting || messageId == R.string.wifi_stopping) {
            setVisiblePreEmptyView(false);
            if (mIsSupportedContactUs) {
                setVisibleContactUs(false);
            }
        }
        mAnimationController.removeAll();
        mAnimationController.updateEmptyView();
    }

    private void setVisibleContactUs(boolean isVisible) {
        if (contactUsView == null) {
            contactUsView = mAnimationController.getContactUsView();
            if (contactUsView == null) return;
        }
        if (isVisible) {
            contactUsView.setVisibility(View.VISIBLE);
        } else {
            contactUsView.setVisibility(View.GONE);
        }
    }

    private void setVisiblePreEmptyView(boolean isVisible) {
        if (mPreEmptyView == null) {
            mPreEmptyView = mAnimationController.getPreEmptyView();
            if (mPreEmptyView == null) return;
        }
        if (isVisible) {
            mPreEmptyView.setVisibility(View.VISIBLE);
        } else {
            mPreEmptyView.setVisibility(View.GONE);
        }
    }

    protected void setProgressBarVisible(boolean visible) {
        if(DBG) Log.d(TAG, "setProgressBarVisible :" + visible);
        if (mProgressHeader != null) {
            //mProgressHeader.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
            mProgressHeader.setProgressBarVisible(visible);
        }
    }

    /**
     * Renames/replaces "Next" button when appropriate. "Next" button usually exists in
     * Wifi setup screens, not in usual wifi settings screen.
     *
     * @param enabled true when the device is connected to a wifi network.
     */
    private void changeNextButtonState(boolean enabled) {
        if (mEnableNextOnConnection) {
            if (hasNextButton()) {
                getNextButton().setEnabled(enabled);
            } else if (hasNextButtonImage()) {
                setEnableNextButtonImage(enabled);
            }
        }
    }

    private AccessPoint findAccessPoint(WifiConfiguration config) {
        final Collection<AccessPoint> accessPoints = mWifiTracker.getAccessPoints();
        for (AccessPoint ap : accessPoints) {
            if (ap.matches(config)) {
                return ap;
            }
        }
        Log.e(TAG, "can't find configured AP, networkId:" + config.networkId);
        return null;
    }

    private void showDialogForRetry(AccessPoint targetAp) {
        Log.e(TAG, "showDialogForRetry : "+WifiDevicePolicyManager.isAllowedToShowRetryDialog(mContext));
        if(WifiDevicePolicyManager.isAllowedToShowRetryDialog(mContext)) {
            if (targetAp == null) {
                Log.e(TAG, "target AP is null, ignored retry popup");
                return;
            }
            mSelectedAccessPoint = targetAp;
            Log.e(TAG, "showDialogForRetry showDialog");
            showDialog(targetAp, WifiConfigUiBase.MODE_RETRY);
        }
    }
    /* package */ void submit(WifiConfigController configController) {
        mScrollTimer = SystemClock.currentThreadTimeMillis();
        final WifiConfiguration config = configController.getConfig();
        forceScrollToTopOfList();
        if (config != null && configController.isNeedToReconnect()) {
            disconnect();
        } else if("VZW".equals(mOpBranding)){
            disconnect();
        }

        if (config == null) {
            if (mSelectedAccessPoint != null
                    && mSelectedAccessPoint.isSaved()) {
                connect(mSelectedAccessPoint.getConfig(), true /* isSavedNetwork */);
            }
        } else if (isWifiQr &&
                       mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            isWifiQr = false;
            mQrConfig = config;
            mWifiManager.setWifiEnabled(true);
            return;
        } else if (configController.getMode() == WifiConfigUiBase.MODE_MODIFY) {
            saveConfig(config);
        } else {
            //not necessary
            //saveConfig(config);
//SEC_PRODUCT_FEATURE_WLAN_ADDNETWORK_CONNECT
//            if (mSelectedAccessPoint != null) { // Not an "Add network"
                connect(config, false /* isSavedNetwork */);
//            }
        }

        mWifiTracker.resumeScanning();
    }

    private void disconnect() {
        Log.i(TAG, "disconnect");
        mWifiManager.disconnect();
    }

    private void startScan() {
        Log.i(TAG, "startScan");
        mWifiTracker.forceScan();
    }

    private void saveConfig(WifiConfiguration config) {
        Log.i(TAG, "saveConfig - networkId:" + config.networkId + " key:" + config.configKey());
        mWifiManager.save(config, mSaveListener);
    }

    private void showDialogForModify() {
        showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_MODIFY);
    }

    private void disable() {
        if (mSelectedAccessPoint != null && mSelectedAccessPoint.getConfig() != null && mSelectedAccessPoint.isActive()) {
            Log.d(TAG, "disconnecting AP...");
            mWifiManager.disableNetwork(mSelectedAccessPoint.getConfig().networkId);
        }
    }

    /* package */ void forget() {
        mMetricsFeatureProvider.action(getActivity(), MetricsEvent.ACTION_WIFI_FORGET);
        if (mSelectedAccessPoint == null && mSelectedPasspointConfig != null) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
            // If user selects forget Hotspot 2.0 networks at Advanced > Manage Network > Hotspot 2.0 Networks, mSelectAccesspoint is null
            mWifiManager.removePasspointConfiguration(mSelectedPasspointConfig.getHomeSp().getFqdn());
            setPasspointPreferenceCategory();
        } else if (mSelectedAccessPoint != null && !mSelectedAccessPoint.isSaved()) {
            if (mSelectedAccessPoint.getNetworkInfo() != null &&
                    mSelectedAccessPoint.getNetworkInfo().getState() != State.DISCONNECTED) {
                // Network is active but has no network ID - must be ephemeral.
                mWifiManager.disableEphemeralNetwork(
                        AccessPoint.convertToQuotedString(mSelectedAccessPoint.getSsidStr()));
            } else {
                // Should not happen, but a monkey seems to trigger it
                Log.e(TAG, "Failed to forget invalid network " + mSelectedAccessPoint.getConfig());
                return;
            }
        } else if (mSelectedAccessPoint != null) {
            WifiConfiguration config = mSelectedAccessPoint.getConfig();
            Log.i(TAG, "forget - " + config.configKey());
            if (config.isPasspoint()) {
                mWifiManager.removePasspointConfiguration(config.FQDN);
            } else {
                mWifiManager.forget(config.networkId, mForgetListener);
                forceUpdateAPs();
            }
        }

        mWifiTracker.resumeScanning();

        // We need to rename/replace "Next" button in wifi setup context.
        changeNextButtonState(false);
    }

    protected void connect(final WifiConfiguration config, boolean isSavedNetwork) {
        // Log subtype if configuration is a saved network.
        mShowRetryDialog = true;
        mMetricsFeatureProvider.action(getActivity(), MetricsEvent.ACTION_WIFI_CONNECT,
                isSavedNetwork);
        Log.i(TAG, "connect - " + config.configKey());
        mWifiManager.connect(config, mConnectListener);
        mScrollTimer = SystemClock.currentThreadTimeMillis();
    }

    protected void connect(final int networkId, boolean isSavedNetwork) {
        // Log subtype if configuration is a saved network.
        mShowRetryDialog = true;
        mMetricsFeatureProvider.action(getActivity(), MetricsEvent.ACTION_WIFI_CONNECT,
                isSavedNetwork);
        Log.i(TAG, "connect - " + networkId);
        mWifiManager.connect(networkId, mConnectListener);
        mScrollTimer = SystemClock.currentThreadTimeMillis();
    }

    /**
     * Called when "add network" button is pressed.
     */
    /* package */ void onAddNetworkPressed() {
        SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                R.string.event_wifi_setting_add_network);
        mMetricsFeatureProvider.action(getActivity(), MetricsEvent.ACTION_WIFI_ADD_NETWORK);
        // No exact access point is selected.
        mSelectedAccessPoint = null;
        showDialog(null, WifiConfigUiBase.MODE_CONNECT);
    }


    public SemExpandableListView getViListView() {
        return mAnimationController.getListView();
    }


    @Override
    public TextView getEmptyTextView() {
        return mAnimationController.getEmptyTextView();
    }

    @Override
    public void setEmptyView(View v) {
        return; //Do not supported
        /*if (mInPickerDialog) return ;
        if (mInManageNetwork) return;
        super.setEmptyView(v);*/
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_wifi;
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {
            @Override
            public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
                final List<SearchIndexableRaw> result = new ArrayList<>();
                final Resources res = context.getResources();

                // Add fragment title
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = res.getString(R.string.wifi_settings_title);
                data.screenTitle = res.getString(R.string.wifi_settings_title);
                data.keywords = res.getString(R.string.keywords_wifi);
                data.key = DATA_KEY_REFERENCE;
                result.add(data);

                //Add advanced menu of wi-fi 
                data = new SearchIndexableRaw(context);
                data.key = "Advanced_wi-fi";
                data.className = ConfigureWifiSettings.class.getName();
                data.title = context.getResources().getString(R.string.wifi_menu_advanced);
                data.keywords = Utils.getKeywordForSearch(context, R.string.wifi_menu_advanced);
                data.screenTitle = context.getResources().getString(R.string.wifi_menu_advanced);
                result.add(data);

                // Add saved Wi-Fi access points
                final List<AccessPoint> accessPoints =
                        WifiTracker.getCurrentAccessPoints(context, true, false, false);
                for (AccessPoint accessPoint : accessPoints) {
                    data = new SearchIndexableRaw(context);
                    data.title = accessPoint.getSsidStr();
                    data.screenTitle = res.getString(R.string.wifi_settings);
                    data.enabled = enabled;
                    result.add(data);
                }

                return result;
            }
        };

    /**
     * Returns true if the config is not editable through Settings.
     * @param context Context of caller
     * @param config The WiFi config.
     * @return true if the config is not editable through Settings.
     */
    static boolean isEditabilityLockedDown(Context context, WifiConfiguration config) {
        return !canModifyNetwork(context, config);
    }

    protected Bundle getWifiDialogArgs(AccessPoint ap) {
        if (ap == null) {
            return null;
        }
        if (DBG) Log.d(TAG, "getWifiDialogArgs - apInfo: " + ap.toString());
        Bundle args = new Bundle();
        if (mWifiTracker.isConnected() && ap.isActive()) {
            LinkProperties linkProperties = mWifiTracker.getLinkProperties();
            if (linkProperties != null) {
                args.putParcelable(WifiConfigController.ARGS_LINK_PROPERTIES, linkProperties);
            }
        }
        args.putInt(WifiConfigController.ARGS_SCREEN_ID, mSAScreenId);
        return args;
    }

    /**
     * This method is a stripped version of WifiConfigStore.canModifyNetwork.
     * TODO: refactor to have only one method.
     * @param context Context of caller
     * @param config The WiFi config.
     * @return true if Settings can modify the config.
     */
    static boolean canModifyNetwork(Context context, WifiConfiguration config) {
        if (config == null) {
            return true;
        }

        final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        // Check if device has DPM capability. If it has and dpm is still null, then we
        // treat this case with suspicion and bail out.
        final PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_DEVICE_ADMIN) && dpm == null) {
            return false;
        }

        boolean isConfigEligibleForLockdown = false;
        if (dpm != null) {
            final ComponentName deviceOwner = dpm.getDeviceOwnerComponentOnAnyUser();
            if (deviceOwner != null) {
                final int deviceOwnerUserId = dpm.getDeviceOwnerUserId();
                try {
                    final int deviceOwnerUid = pm.getPackageUidAsUser(deviceOwner.getPackageName(),
                            deviceOwnerUserId);
                    isConfigEligibleForLockdown = deviceOwnerUid == config.creatorUid;
                } catch (NameNotFoundException e) {
                    // don't care
                }
            }
        }
        if (!isConfigEligibleForLockdown) {
            return true;
        }

        final ContentResolver resolver = context.getContentResolver();
        final boolean isLockdownFeatureEnabled = Settings.Global.getInt(resolver,
                Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN, 0) != 0;
        return !isLockdownFeatureEnabled;
    }

    private ListAnimationController.OnEventListener mViListListener
            = new ListAnimationController.OnEventListener() {
        @Override
        public void onItemClick(AccessPointPreference preference) {
            if (DBG) Log.i(TAG, "onItemClick - " + preference.toString());
            LongPressAccessPointPreference apPref = null;
            AccessPoint ap = null;
            if (preference instanceof LongPressAccessPointPreference) {
                apPref = (LongPressAccessPointPreference) preference;
                ap = preference.getAccessPoint();
            } else {
                ap = preference.getAccessPoint();
                if (ap != null) {
                    apPref = createLongPressActionPointPreference(ap);
                }
            }
            if (apPref != null && ap != null) {
                onAccessPointPreferenceClick(apPref);
            } else if (apPref != null && ap == null) {
                onPassPointPreferenceClick(preference);
            } else {
                Log.e(TAG, "no matched any accesspoint");
            }
        }

        @Override
        public void onAddNetworkPressed() {
            WifiSettings.this.onAddNetworkPressed();
        }

        @Override
        public void onRefresh() {
            startScan();
        }
    };

    protected WifiDialog.WifiDialogListener mDialogListener = new WifiDialog.WifiDialogListener() {
        @Override
        public void onForget(WifiDialog dialog) {
            forget();
        }

        @Override
        public void onSave(WifiDialog dialog) {
            WifiConfigController configController = dialog.getController();
            final WifiConfiguration config = configController.getConfig();
            if (config != null) {
                if (configController.isNeedToReconnect()) {
                    disconnect();
                    saveConfig(config);
                    connect(config, true);
                } else {
                    saveConfig(config);
                }
            }
        }

        @Override
        public void onConnect(WifiDialog dialog) {
            submit(dialog.getController());
        }

        @Override
        public void onEdit(WifiDialog dialog) {
            showDialogForModify();
        }

        @Override
        public void onDisable(WifiDialog dialog) {
            disable();
        }
    };

    private WifiEnabler.IWifiEnablerListener mWifiEnablerListener
            = new WifiEnabler.IWifiEnablerListener() {
        @Override
        public void onSwitchChanged(boolean checked) {
            Log.d(TAG, "enabler changed - " + checked);
            if (checked) {
                onWifiStateChanged(WifiManager.WIFI_STATE_ENABLING);
            } else {
                onWifiStateChanged(WifiManager.WIFI_STATE_DISABLING);
            }
        }
    };

    private static class SummaryProvider
            implements SummaryLoader.SummaryProvider, OnSummaryChangeListener {

        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        @VisibleForTesting
        WifiSummaryUpdater mSummaryHelper;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            mContext = context;
            mSummaryLoader = summaryLoader;
            if (mContext != null) {
                mSummaryHelper = new WifiSummaryUpdater(mContext, this);
            }
        }

        @Override
        public void setListening(boolean listening) {
            if (mContext != null) {
                mSummaryHelper.register(listening);
            }
        }

        @Override
        public void onSummaryChanged(String summary) {
            mSummaryLoader.setSummary(this, summary);
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
            = new SummaryLoader.SummaryProviderFactory() {
        @Override
        public SummaryLoader.SummaryProvider createSummaryProvider(Activity activity,
                                                                   SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };

    //SEC_FLOATING_FEATURE_WLAN_SUPPORT_AP_LINK
    private final Handler mCheckHttpResponseHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_CHECK_GO_TO_WEBPAGE_HTTP_RESPONSE :
                    checkGoToWebPageHTTPResponse();
                    break;
                default :
                    break;
            }
        }
    };
    public void checkGoToWebPageHTTPResponse(){
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        if (dhcpInfo != null){
            Log.d(TAG, "dhcpInfo.gateway : " + dhcpInfo.gateway);
            if (dhcpInfo.gateway != 0) {
                mGateway = "http://" + Formatter.formatIpAddress(dhcpInfo.gateway);
                Log.d(TAG, "Go to Webpage: gateway addr: " + mGateway);
                AsyncTask<String, Void, Integer> gatewayTask = new AsyncTask<String, Void, Integer>() {
                    HttpURLConnection urlConnection;
                    int responseCode = 0;

                    @Override
                    protected Integer doInBackground(String... pageName) {
                        try {
                            URL url = new URL(pageName[0]);
                            urlConnection = (HttpURLConnection) url.openConnection();
                            urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
                            responseCode = urlConnection.getResponseCode();
                            Log.d(TAG, "Go To Webpage: HTTP Response "+Integer.toString(responseCode));
                            return responseCode;
                        } catch (MalformedURLException e) {
                            Log.d(TAG, "Go to Webpage: Error getting URL");
                            e.printStackTrace();
                        } catch (IOException f) {
                            Log.d(TAG, "Go to Webpage: Error opening connection");
                            f.printStackTrace();
                        } finally {
                            Log.d(TAG, "Go to Webpage: reach to finally");
                            if (urlConnection != null) urlConnection.disconnect();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        if(result != null) {
                            goToWebPageHTTPResponse = result;
                        }
                        else Log.d(TAG, "Go to Webpage: HTTP response is null");
                    }
                };
                gatewayTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mGateway);
            }
        }
    }
    //SEC_FLOATING_FEATURE_WLAN_SUPPORT_AP_LINK

    private boolean isCricketManagerSupport() {
        if (!Utils.isPackageExists(getActivity(), "com.smithmicro.netwise.director.cricket")) {
            return false;
        }
        return true;
    }
    public void onCricketManagerClicked() {
        Log.d(TAG, "starting cricket manager");
        SAUtils.getInstance(mContext).insertLog(mSAScreenId,
                R.string.event_wifi_setting_cricket_manager);
        Intent intent = new Intent("com.smithmicro.mnd.MNDSettings");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "ActivityNotFoundException : " + e);
        }
    }


    protected void connectWeChatAccessPoint(String ssid, String bssid, int rssi) {
        Intent intent = new Intent();
        intent.setAction("com.samsung.android.net.wifi.WECHAT_CONNECT_AP");
        intent.putExtra("ssid", ssid);
        intent.putExtra("bssid", bssid);
        intent.putExtra("rssi", rssi);
        getActivity().sendBroadcast(intent);
    }


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


    // Bixby
    private void forceScanByBixby() {
        mWifiTracker.forceScan();

        if (mEmSettingsManager.isLastState()) {
            mEmSettingsManager.addNlgScreenParam("WiFi", "Searching", "yes");
            mEmSettingsManager.requestNlg("WiFiSettings");
        }

        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
    }


    protected void showDialogToDisplayInfo(AccessPoint accessPoint) {
        if (accessPoint == null) {
            Log.e(TAG, "showDialogToDisplayInfo, accessPoint is null");
            return;
        }

        mSelectedAccessPoint = accessPoint;
        showDialog(mSelectedAccessPoint, WifiConfigUiBase.MODE_VIEW);
    }


    public EmSettingsManager.IEmCallback mEmCallback = new EmSettingsManager.IEmCallback() {
        @Override
        public void onStateReceived() {
            String stateId = mEmSettingsManager.getStateId();

            Log.d(TAG, "mEmCallback, stateId: " + stateId);

            if (stateId.equals("WiFiTurnOn")) {
                if (mWifiManager.isWifiEnabled()) {
                    mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyON", "yes");
                    mEmSettingsManager.requestNlg("WiFiSettings");
                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                } else if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                    mEmSettingsManager.addNlgScreenParam("MobileHotspot", "AlreadyOn", "yes");
                    mEmSettingsManager.requestNlg("WiFiSettings");
                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                } else {
                    mWifiManager.setWifiEnabled(true);
                    if (mEmSettingsManager.isLastState()) {
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyON", "no");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                    }

                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                }
            } else if (stateId.equals("WiFiTurnOff")) {
                if (mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(false);
                    if (mEmSettingsManager.isLastState()) {
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOFF", "no");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                    }

                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                } else {
                    mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyOFF", "yes");
                    mEmSettingsManager.requestNlg("WiFiSettings");
                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                }
            } else if (stateId.equals("WiFiScan")) {
                if (mWifiManager.isWifiEnabled()) {
                    forceScanByBixby();
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
                            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyON", "no");
                            mEmSettingsManager.requestNlg("WiFiSettings");
                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                        }
                    }
                }
            } else if (stateId.equals("WiFiSelectScanedAp")) {
                String param = mEmSettingsManager.getParamString(0);
                Log.d(TAG, "mEmCallback, param: " + param);

                if (mWifiManager.isWifiEnabled()) {
                    mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyON", "yes");
                    mEmSettingsManager.requestNlg("WiFiSettings");
                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                } else {
                    if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                        mEmSettingsManager.addNlgScreenParam("MobileHotspot", "AlreadyOn", "yes");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                        mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                    } else {
                        if (mWifiManager.setWifiEnabled(true)) {
                            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyON", "yes");
                            mEmSettingsManager.requestNlg("WiFiSettings");
                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                        } else {
                            mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyON", "no");
                            mEmSettingsManager.requestNlg("WiFiSettings");
                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                        }
                    }
                }
            } else if (stateId.equals("WiFiRemoveConnectAp")) {
                if (mWifiManager.isWifiEnabled()) {
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                    int networkId = WifiConfiguration.INVALID_NETWORK_ID;
                    if (wifiInfo != null) {
                        networkId = wifiInfo.getNetworkId();
                    }

                    if (networkId != WifiConfiguration.INVALID_NETWORK_ID) {
                        if (mWifiManager.disableNetwork(networkId)) {
                            if (mEmSettingsManager.isLastState()) {
                                mEmSettingsManager.addNlgScreenParam("WiFi", "Disconnected", "yes");
                                mEmSettingsManager.requestNlg("WiFiSettings");
                            }

                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                        } else {
                            if (mEmSettingsManager.isLastState()) {
                                mEmSettingsManager.addNlgScreenParam("WiFi", "Disconnected", "no");
                                mEmSettingsManager.requestNlg("WiFiSettings");
                            }

                            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                        }

                        return;
                    }
                }

                mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyDisconnected", "yes");
                mEmSettingsManager.requestNlg("WiFiSettings");
                mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
            } else if (stateId.equals("WiFiSelectConnectedAp")) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                int networkId = WifiConfiguration.INVALID_NETWORK_ID;
                if (wifiInfo != null) {
                    networkId = wifiInfo.getNetworkId();
                }

                if (networkId != WifiConfiguration.INVALID_NETWORK_ID) {
                    WifiConfiguration config = mWifiManager.getSpecificNetwork(networkId);
                    showDialogToDisplayInfo(findAccessPoint(config));

                    if (mEmSettingsManager.isLastState()) {
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyConnected", "yes");
                        mEmSettingsManager.requestNlg("WiFiSelectConnectedAp");
                    }

                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
                } else {
                    if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                         mEmSettingsManager.addNlgScreenParam("MobileHotspot", "AlreadyOn", "yes");
                         mEmSettingsManager.requestNlg("WiFiSettings");
                    } else if (!mWifiManager.isWifiEnabled()) {
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyON", "no");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                    } else {
                        mEmSettingsManager.addNlgScreenParam("WiFi", "AlreadyConnected", "no");
                        mEmSettingsManager.requestNlg("WiFiSettings");
                    }

                    mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
                }
            } else if (stateId.equals("WiFiAdvanced")) {
                startWifiConfigSettings();
                if (mEmSettingsManager.isLastState()) {
                    mEmSettingsManager.requestNlg("WiFiAdvanced");
                }

                mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
           } else if (stateId.equals("WiFiDirectSettings")) {
                if (!mWifiManager.isWifiEnabled())
                    mWifiManager.setWifiEnabled(true);

                if(mBixbyCountDownTimer != null) {
                    mBixbyCountDownTimer.cancel();
                    mBixbyCountDownTimer = null;
                }

                mBixbyCountDownTimer = new CountDownTimer(6000 , 1000) {
                    public void onTick(long millisUntilFinished) {
                        Log.d(TAG, "mBixbyCountDownTimer ontick ");
                        if (mWifiManager.isWifiEnabled()) {
                            startWifiP2pSettings();
                            responseP2pToEm();
                        } else if (!isWifiSharingEnabled() && isMobileHotstpotEnabled()) {
                            responseP2pToEm();
                        }
                    }
                    public void onFinish() {
                        Log.d(TAG, "mBixbyCountDownTimer onfinished ");
                        responseP2pToEm();
                    }
                };
                mBixbyCountDownTimer.start();
           }
        }
    };


    private void responseP2pToEm() {
        if(mBixbyCountDownTimer != null) {
            mBixbyCountDownTimer.cancel();
            mBixbyCountDownTimer = null;
        }
        if(isMobileHotstpotEnabled()) {
            mEmSettingsManager.addNlgScreenParam("MobileHotspot","AlreadyON","yes");
            mEmSettingsManager.requestNlg("WiFiSettings");
            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_FAILURE);
        } else if(mEmSettingsManager.isLastState()) {
            mEmSettingsManager.addNlgScreenParam("MobileHotspot","AlreadyON","no");
            mEmSettingsManager.requestNlg("WiFiDirectSettings");
            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
        } else {
            mEmSettingsManager.sendResponse(EmSettingsManager.EM_RESPONSE_RESULT_SUCCESS);
        }
    }

    private WifiConfiguration getWifiConfigFromIntent(Intent intent) {
        WifiConfiguration config = new WifiConfiguration();
        int type = intent.getIntExtra("AUTH_TYPE", -1);
        String ssid = intent.getStringExtra("SSID");
        String password = intent.getStringExtra("PASSWORD");
        boolean hidden = intent.getBooleanExtra("HIDDEN", false);
        config.SSID = ssid;
        switch(type) {
            case AccessPoint.SECURITY_NONE:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                break;
            case AccessPoint.SECURITY_WEP:
                config.allowedKeyManagement.set(KeyMgmt.NONE);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
                if (password != null) {
                    int length = password.length();
                    if ((length == 10 || length == 26 || length == 58) && password.matches("[0-9A-Fa-f]*")) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = '"' + password + '"';
                    }
                }
                break;
            case AccessPoint.SECURITY_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
                if (password != null) {
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        String part = '"' + password;
                        String str = part + '"';
                        password.clear();
                        part.clear();
                        config.preSharedKey = str;
                    }
                }
                break;
            case AccessPoint.SECURITY_WAPI_PSK:
                config.allowedKeyManagement.set(KeyMgmt.WAPI_PSK);
                if (password != null) {
                    if (password.matches("[0-9A-Fa-f]{64}")) {
                        config.preSharedKey = password;
                    } else {
                        String part = '"' + password;
                        String str = part + '"';
                        password.clear();
                        part.clear();
                        config.preSharedKey = str;
                    }
                }
                break;
            default:
                return null;
        }
        config.hiddenSSID = hidden;
        Log.d(TAG, "getWifiConfigFromIntent return config : " + config);
        return config;
    }

}
