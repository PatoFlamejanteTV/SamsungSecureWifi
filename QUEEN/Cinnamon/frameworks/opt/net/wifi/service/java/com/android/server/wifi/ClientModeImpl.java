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

package com.android.server.wifi;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.KeepalivePacketData;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.MatchAllNetworkSpecifier;
import android.net.NattKeepalivePacketData;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.SocketKeepalive;
import android.net.SocketKeepalive.InvalidPacketException;
import android.net.StaticIpConfiguration;
import android.net.TcpKeepalivePacketData;
import android.net.ip.IIpClient;
import android.net.ip.IpClientCallbacks;
import android.net.ip.IpClientManager;
import android.net.shared.ProvisioningConfiguration;
import android.net.util.InterfaceParams;
import android.net.wifi.INetworkRequestMatchCallback;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.DeviceMobilityState;
import android.net.wifi.WifiNetworkAgentSpecifier;
import android.net.wifi.WifiSsid;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.p2p.IWifiP2pManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.sec.enterprise.EnterpriseDeviceManager;
import android.sec.enterprise.WifiPolicy;
import android.sec.enterprise.WifiPolicyCache;
import android.sec.enterprise.auditlog.AuditEvents;
import android.sec.enterprise.auditlog.AuditLog;
import android.sec.enterprise.certificate.CertificatePolicy;
import android.system.OsConstants;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.StatsLog;

import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.telephony.ITelephony;
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.Protocol;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.WifiController.P2pDisableListener;
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.anqp.ANQPElement;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.android.server.wifi.hotspot2.anqp.HSWanMetricsElement;
import com.android.server.wifi.hotspot2.anqp.VenueNameElement;
import com.android.server.wifi.nano.WifiMetricsProto;
import com.android.server.wifi.nano.WifiMetricsProto.StaEvent;
import com.android.server.wifi.nano.WifiMetricsProto.WifiIsUnusableEvent;
import com.android.server.wifi.nano.WifiMetricsProto.WifiUsabilityStats;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.StringUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.TelephonyUtil.SimAuthRequestData;
import com.android.server.wifi.util.TelephonyUtil.SimAuthResponseData;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.feature.SemFloatingFeature;
import com.samsung.android.knox.custom.CustomDeviceManagerProxy;
import com.samsung.android.location.SemLocationListener;
import com.samsung.android.location.SemLocationManager;
import com.samsung.android.net.wifi.OpBrandingLoader;
import com.samsung.android.net.wifi.OpBrandingLoader.Vendor;
import com.samsung.android.server.wifi.ArpPeer;
import com.samsung.android.server.wifi.SemSarManager;
import com.samsung.android.server.wifi.SemWifiFrameworkUxUtils;
import com.samsung.android.server.wifi.SemWifiHiddenNetworkTracker;
import com.samsung.android.server.wifi.UnstableApController;
import com.samsung.android.server.wifi.WifiB2BConfigurationPolicy;
import com.samsung.android.server.wifi.WifiDelayDisconnect;
import com.samsung.android.server.wifi.WifiMobileDeviceManager;
import com.samsung.android.server.wifi.WifiRoamingAssistant;
import com.samsung.android.server.wifi.WlanTestHelper;
import com.samsung.android.server.wifi.bigdata.WifiBigDataLogManager;
import com.samsung.android.server.wifi.bigdata.WifiChipInfo;
import com.samsung.android.server.wifi.dqa.IssueCommandIds;
import com.samsung.android.server.wifi.dqa.ReportIdKey;
import com.samsung.android.server.wifi.dqa.ReportUtil;
import com.samsung.android.server.wifi.dqa.SemWifiIssueDetector;
import com.samsung.android.server.wifi.mobilewips.client.MobileWipsDef;
import com.samsung.android.server.wifi.mobilewips.framework.MobileWipsFrameworkService;
import com.samsung.android.server.wifi.softap.SemConnectivityPacketTracker;
import com.sec.android.app.CscFeatureTagCOMMON;
import com.sec.android.app.CscFeatureTagWifi;
import com.sec.android.app.SecProductFeature_COMMON;
import com.sec.android.app.SecProductFeature_KNOX;
import com.sec.android.app.SecProductFeature_WLAN;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN;
import static android.sec.enterprise.WifiPolicy.ACTION_ENABLE_NETWORK_INTERNAL;
import static android.sec.enterprise.WifiPolicy.EXTRA_ENABLE_OTHERS_INTERNAL;
import static android.sec.enterprise.WifiPolicy.EXTRA_NETID_INTERNAL;

/**
 * Implementation of ClientMode.  Event handling for Client mode logic is done here,
 * and all changes in connectivity state are initiated here.
 *
 * @hide
 */
public class ClientModeImpl extends StateMachine {

    private static final String NETWORKTYPE = "WIFI";
    @VisibleForTesting public static final short NUM_LOG_RECS_NORMAL = 1000; //SEC_PRODUCT_FEATURE_WLAN_REDUCE_LOWRAM_DEVICE_MAXLOG_SIZE
    @VisibleForTesting public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = 200;
    @VisibleForTesting public static final short NUM_LOG_RECS_VERBOSE = 3000;
    private static final String TAG = "WifiClientModeImpl";
    private static boolean DBG;
    private static final boolean DBG_PRODUCT_DEV = android.os.Debug.semIsProductDev();

    private static final int ONE_HOUR_MILLI = 1000 * 60 * 60;

    private static final String GOOGLE_OUI = "DA-A1-19";

    private static final String EXTRA_OSU_ICON_QUERY_BSSID = "BSSID";
    private static final String EXTRA_OSU_ICON_QUERY_FILENAME = "FILENAME";
    private static final String EXTRA_OSU_PROVIDER = "OsuProvider";
    private static final String EXTRA_UID = "uid";
    private static final String EXTRA_PACKAGE_NAME = "PackageName";
    private static final String EXTRA_PASSPOINT_CONFIGURATION = "PasspointConfiguration";
    private static final int IPCLIENT_TIMEOUT_MS = 10_000;
    private static final boolean CSC_SUPPORT_5G_ANT_SHARE = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORT5GANTSHARE); //TAG_CSCFEATURE_WIFI_SUPPORT5GANTSHARE
    //It is a feature for Tencent Secure WiFi supporting in china market.
    //If "CSCFEATURE_WIFI_CONFIGSECURESVCINTEGRATION" feature is supported, MWIPS feature should not be supported
    private static final String CONFIG_SECURE_SVC_INTEGRATION = SemCscFeature.getInstance().getString(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGSECURESVCINTEGRATION); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION

    private static final boolean ENBLE_WLAN_CONFIG_ANALYTICS =
            Integer.parseInt(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS) == 1;

    private boolean mVerboseLoggingEnabled = false;

    private static final String CHARSET_CN = "gbk"; //TAG_CSCFEATURE_WIFI_CONFIGENCODINGCHARSET
    private static final String CHARSET_KOR = "ksc5601"; //TAG_CSCFEATURE_WIFI_CONFIGENCODINGCHARSET
    private static final String CONFIG_CHARSET = OpBrandingLoader.getInstance().getSupportCharacterSet(); //TAG_CSCFEATURE_WIFI_CONFIGENCODINGCHARSET

    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLAN_ADVANCED_DEBUG
    private static int mWlanAdvancedDebugState = 0;
    private static final int WLAN_ADVANCED_DEBUG_RESET = 0;
    private static final int WLAN_ADVANCED_DEBUG_PKT = 1;
    private static final int WLAN_ADVANCED_DEBUG_UNWANTED = 1<<1;
    private static final int WLAN_ADVANCED_DEBUG_DISC = 1<<2;
    private static final int WLAN_ADVANCED_DEBUG_UDI = 1<<3;
    private static final int WLAN_ADVANCED_DEBUG_STATE = 1<<4;
    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLAN_ADVANCED_DEBUG

    //+SEC_PRODUCT_FEATURE_WLAN_ISSUETRACKER_CONTROL
    private static boolean mIssueTrackerOn = false;
    private static final int ISSUE_TRACKER_SYSDUMP_HANG = 0;
    private static final int ISSUE_TRACKER_SYSDUMP_UNWANTED = 1;
    private static final int ISSUE_TRACKER_SYSDUMP_DISC = 2;
    //-SEC_PRODUCT_FEATURE_WLAN_ISSUETRACKER_CONTROL

    private final WifiPermissionsWrapper mWifiPermissionsWrapper;

    //++SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL
    private boolean mQosGameIsRunning = false;
    private int mPersistQosTid = 0;
    private int mPersistQosUid = 0;
    //--SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL

    /* debug flag, indicating if handling of ASSOCIATION_REJECT ended up blacklisting
     * the corresponding BSSID.
     */
    private boolean mDidBlackListBSSID = false;

    //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
    private long mLastConnectedTime = -1;
    //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }

    //+CSC_SUPPORT_5G_ANT_SHARE
    private static int mLteuState = 0;
    private static int mLteuEnable = 0;
    private static boolean mScellEnter = false;
    private final int LTEU_MOBILEHOTSPOT_5GHZ_ENABLED = 1;
    private final int LTEU_P2P_5GHZ_CONNECTED = 2;
    private final int LTEU_STA_5GHZ_CONNECTED = 4;
    //-CSC_SUPPORT_5G_ANT_SHARE

    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
    private static boolean mIsPolicyMobileData = false;
    private static boolean mChanged = false;
    private static byte mCellularSignalLevel = 0;
    private static byte mCellularCapaState = 0;
    private static byte mNetworktype = WifiNative.MBO_TYPE_NETWORK_CLASS_UNKNOWN;
    private static byte [] mCellularCellId = new byte[2];
    private static final String DATA_LIMIT_INTENT = "com.android.intent.action.DATAUSAGE_REACH_TO_LIMIT";
    private static int mPhoneStateEvent = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_CARRIER_NETWORK_CHANGE | PhoneStateListener.LISTEN_USER_MOBILE_DATA_STATE;
    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO

    private static final String CSC_CONFIG_OP_BRANDING = SemCscFeature.getInstance().getString(
                CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGOPBRANDING); //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
    private static final boolean CSC_WIFI_ERRORCODE = SemCscFeature.getInstance().getBoolean(
                CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE); //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
    private static final boolean CSC_WIFI_SUPPORT_VZW_EAP_AKA = SemCscFeature.getInstance().getBoolean(
                CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTEAPAKA); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM

    //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
    private boolean mIsWifiOffByAirplane = false;
    //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

    private Timer mFwLogTimer = null; //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE

    /**
     * Log with error attribute
     *
     * @param s is string log
     */
    @Override
    protected void loge(String s) {
        Log.e(getName(), s);
    }
    @Override
    protected void logd(String s) {
        Log.d(getName(), s);
    }
    @Override
    protected void log(String s) {
        Log.d(getName(), s);
    }
    private final WifiMetrics mWifiMetrics;
    private final WifiInjector mWifiInjector;
    private final WifiMonitor mWifiMonitor;
    private final WifiNative mWifiNative;
    private final WifiPermissionsUtil mWifiPermissionsUtil;
    private final WifiConfigManager mWifiConfigManager;
    private final WifiConnectivityManager mWifiConnectivityManager;
    private ConnectivityManager mCm;
    private BaseWifiDiagnostics mWifiDiagnostics;
    private final ScanRequestProxy mScanRequestProxy; //SEC_PRODUCT_FEATURE_WLAN_SCAN_CONTROLLER
    private final boolean mP2pSupported;
    private P2pDisableListener mP2pDisableListener = null;
    private final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private boolean mIsP2pConnected;
    private int mCurrentP2pFreq;
    private boolean mTemporarilyDisconnectWifi = false;
    private final Clock mClock;
    private final PropertyService mPropertyService;
    private final BuildProperties mBuildProperties;
    private final WifiCountryCode mCountryCode;
    private final WifiScoreCard mWifiScoreCard;
    private final WifiScoreReport mWifiScoreReport;
    private final SarManager mSarManager;
    private final SemSarManager mSemSarManager; //SEMSAR
    private final WifiTrafficPoller mWifiTrafficPoller;
    public WifiScoreReport getWifiScoreReport() {
        return mWifiScoreReport;
    }
    private final PasspointManager mPasspointManager;
    private final WifiDataStall mWifiDataStall;
    private final LinkProbeManager mLinkProbeManager;

    private final McastLockManagerFilterController mMcastLockManagerFilterController;

    private boolean mScreenOn = false;

    private String mInterfaceName;

    //+SEC_PRODUCT_FEATURE_WLAN_SWITCHBOARD
    private static int mRssiPollingScreenOffEnabled = 0;
    private static final int RSSI_POLL_ENABLE_DURING_LCD_OFF_FOR_IMS = 1;
    private static final int RSSI_POLL_ENABLE_DURING_LCD_OFF_FOR_SWITCHBOARD = 1<<1;
    //-SEC_PRODUCT_FEATURE_WLAN_SWITCHBOARD
    private int mLastSignalLevel = -1;
    private String mLastBssid;
    private int mLastNetworkId; // The network Id we successfully joined
    private int mLastConnectedNetworkId; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING

    private boolean mIpReachabilityDisconnectEnabled = false; //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX
    private boolean mHandleIfaceIsUp = false; //SEC_PRODUCT_FEATURE_WLAN_ERROR_CODE_REFACTORING
    private int mConnectedApInternalType = 0;
    private boolean mIsBootCompleted; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    private WifiBigDataLogManager mBigDataManager; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    private SemWifiIssueDetector mIssueDetector; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
    private WifiSettingsStore mSettingsStore; //SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

    //EDM Wi-Fi Configuration
    private final WifiB2BConfigurationPolicy mWifiB2bConfigPolicy; 
    private boolean mIsNchoParamSet = false;

    private static final int NCHO_VERSION_UNKNOWN = -1;
    private static final int NCHO_VERSION_ERROR = 0;
    private static final int NCHO_VERSION_1_0 = 1;
    private static final int NCHO_VERSION_2_0 = 2;
    private int mNchoVersion = NCHO_VERSION_UNKNOWN;

    private static final int NCHO_VER1_STATE_ERROR = -1;
    private static final int NCHO_VER1_STATE_INIT = 0;
    private static final int NCHO_VER1_STATE_BACKUP = 1;
    private int mNcho10State = NCHO_VER1_STATE_INIT;
    private int mDefaultRoamTrigger = WifiB2BConfigurationPolicy.WIFICONF_RSSI_THRESHOLD_DEFAULT;
    private int mDefaultRoamDelta = WifiB2BConfigurationPolicy.WIFICONF_RSSI_ROAMDELTA_DEFAULT;
    private int mDefaultRoamScanPeriod = WifiB2BConfigurationPolicy.WIFICONF_ROAM_SCANPERIOD_DEFAULT;

    private static final int NCHO_VER2_STATE_ERROR = -1;
    private static final int NCHO_VER2_STATE_DISABLED = 0;
    private static final int NCHO_VER2_STATE_ENABLED = 1;
    private int mNcho20State = NCHO_VER2_STATE_DISABLED;
    //End EDM Wi-Fi Configuration

// for wifiap Packet log
    private String mApInterfaceName = "Not_use";
    private static final String INTERFACENAMEOFWLAN = "wlan0";
    private static final int MAX_PACKET_RECORDS = 500;
    private SemConnectivityPacketTracker mPacketTrackerForHotspot;
    private LocalLog mConnectivityPacketLogForHotspot;
    private SemConnectivityPacketTracker mPacketTrackerForWlan0;
    private LocalLog mConnectivityPacketLogForWlan0;
    private boolean mFirstTurnOn = true;
    private static final ConcurrentHashMap<String, LocalLog> sPktLogsMhs = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, LocalLog> sPktLogsWlan = new ConcurrentHashMap<>();
// for wifiap Packet log

    /* SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
     * Tracks if Wi-Fi Power Saving is enabled or not
     */
    private AtomicBoolean mWifiAdpsEnabled = new AtomicBoolean(false);
    private static final boolean ENABLE_SUPPORT_ADPS = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS");

//+SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
    private static final int ROAM_DHCP_DEFAULT = 0;
    private static final int ROAM_DHCP_RESTART = 1;
    private static final int ROAM_DHCP_SKIP = 2;
    private int mRoamDhcpPolicy = ROAM_DHCP_DEFAULT; // 0:default, 1:ForceRestart, 2:skip
    private int mRoamDhcpPolicyByB2bConfig = ROAM_DHCP_DEFAULT; // 0:default, 2:skip
//-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY

    private void processRssiThreshold(byte curRssi, int reason,
            WifiNative.WifiRssiEventHandler rssiHandler) {
        if (curRssi == Byte.MAX_VALUE || curRssi == Byte.MIN_VALUE) {
            Log.wtf(TAG, "processRssiThreshold: Invalid rssi " + curRssi);
            return;
        }
        for (int i = 0; i < mRssiRanges.length; i++) {
            if (curRssi < mRssiRanges[i]) {
                // Assume sorted values(ascending order) for rssi,
                // bounded by high(127) and low(-128) at extremeties
                byte maxRssi = mRssiRanges[i];
                byte minRssi = mRssiRanges[i - 1];
                // This value of hw has to be believed as this value is averaged and has breached
                // the rssi thresholds and raised event to host. This would be eggregious if this
                // value is invalid
                mWifiInfo.setRssi(curRssi);
                updateCapabilities();
                int ret = startRssiMonitoringOffload(maxRssi, minRssi, rssiHandler);
                Log.d(TAG, "Re-program RSSI thresholds for " + getWhatToString(reason)
                        + ": [" + minRssi + ", " + maxRssi + "], curRssi=" + curRssi
                        + " ret=" + ret);
                break;
            }
        }
    }

    private boolean mEnableRssiPolling = false;
    // Accessed via Binder thread ({get,set}PollRssiIntervalMsecs), and ClientModeImpl thread.
    private volatile int mPollRssiIntervalMsecs = DEFAULT_POLL_RSSI_INTERVAL_MSECS;
    private int mRssiPollToken = 0;
    /* 3 operational states for STA operation: CONNECT_MODE, SCAN_ONLY_MODE, SCAN_ONLY_WIFI_OFF_MODE
    * In CONNECT_MODE, the STA can scan and connect to an access point
    * In SCAN_ONLY_MODE, the STA can only scan for access points
    * In SCAN_ONLY_WIFI_OFF_MODE, the STA can only scan for access points with wifi toggle being off
    */
    private int mOperationalMode = DISABLED_MODE;

    // variable indicating we are expecting a mode switch - do not attempt recovery for failures
    private boolean mModeChange = false;

    private ClientModeManager.Listener mClientModeCallback = null;

    private boolean mBluetoothConnectionActive = false;

    private PowerManager.WakeLock mSuspendWakeLock;

    /**
     * Interval in milliseconds between polling for RSSI and linkspeed information.
     * This is also used as the polling interval for WifiTrafficPoller, which updates
     * its data activity on every CMD_RSSI_POLL.
     */
    private static final int DEFAULT_POLL_RSSI_INTERVAL_MSECS = 3000;

    /**
     * Interval in milliseconds between receiving a disconnect event
     * while connected to a good AP, and handling the disconnect proper
     */
    private static final int LINK_FLAPPING_DEBOUNCE_MSEC = 4000;

    /**
     * Delay between supplicant restarts upon failure to establish connection
     */
    private static final int SUPPLICANT_RESTART_INTERVAL_MSECS = 5000;

    /**
     * Number of times we attempt to restart supplicant
     */
    private static final int SUPPLICANT_RESTART_TRIES = 5;

    /**
     * Value to set in wpa_supplicant "bssid" field when we don't want to restrict connection to
     * a specific AP.
     */
    public static final String SUPPLICANT_BSSID_ANY = "any";

    /**
     * The link properties of the wifi interface.
     * Do not modify this directly; use updateLinkProperties instead.
     */
    private LinkProperties mLinkProperties;
    private LinkProperties mOldLinkProperties;

    /* Tracks sequence number on a periodic scan message */
    private int mPeriodicScanToken = 0;

    // Wakelock held during wifi start/stop and driver load/unload
    private PowerManager.WakeLock mWakeLock;

    private Context mContext;

    //+SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM (3.1)
    private WifiPolicy mWifiPolicy;
    //-SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM (3.1)

    private final Object mDhcpResultsLock = new Object();
    private DhcpResults mDhcpResults;

    // NOTE: Do not return to clients - see syncRequestConnectionInfo()
    private final ExtendedWifiInfo mWifiInfo;
    private NetworkInfo mNetworkInfo;
    private SupplicantStateTracker mSupplicantStateTracker;

    // Indicates that framework is attempting to roam, set true on CMD_START_ROAM, set false when
    // wifi connects or fails to connect
    private boolean mIsAutoRoaming = false;

    private boolean mBlockFccChannelCmd = false; //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL

    // Roaming failure count
    private int mRoamFailCount = 0;

    // This is the BSSID we are trying to associate to, it can be set to SUPPLICANT_BSSID_ANY
    // if we havent selected a BSSID for joining.
    private String mTargetRoamBSSID = SUPPLICANT_BSSID_ANY;
    // This one is used to track the current target network ID. This is used for error
    // handling during connection setup since many error message from supplicant does not report
    // SSID Once connected, it will be set to invalid
    private int mTargetNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
    private long mLastDriverRoamAttempt = 0;
    private WifiConfiguration mTargetWifiConfiguration = null;

    private final static Vendor mOpBranding = OpBrandingLoader.getInstance().getOpBranding();
    private SemWifiHiddenNetworkTracker mSemWifiHiddenNetworkTracker; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST

    int getPollRssiIntervalMsecs() {
        return mPollRssiIntervalMsecs;
    }

    private boolean mIsShutdown = false; //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
    private WifiDelayDisconnect mDelayDisconnect; //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
    private boolean mIsRoaming = false; //SEC_PRODUCT_FEATURE_WLAN_GET_ROAMING_STATUS_FOR_MOBIKE
    private UnstableApController mUnstableApController; //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP

    void setPollRssiIntervalMsecs(int newPollIntervalMsecs) {
        mPollRssiIntervalMsecs = newPollIntervalMsecs;
    }

    /**
     * Method to clear {@link #mTargetRoamBSSID} and reset the the current connected network's
     * bssid in wpa_supplicant after a roam/connect attempt.
     */
    public boolean clearTargetBssid(String dbg) {
        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
        if (config == null) {
            return false;
        }
        if (!mHandleIfaceIsUp) { //SEC_PRODUCT_FEATURE_WLAN_ERROR_CODE_REFACTORING
            Log.w(TAG, "clearTargetBssid, mHandleIfaceIsUp is false");
            return false;
        }
        String bssid = SUPPLICANT_BSSID_ANY;
        if (config.BSSID != null) {
            bssid = config.BSSID;
            if (mVerboseLoggingEnabled) {
                Log.d(TAG, "force BSSID to " + bssid + "due to config");
            }
        }
        if (mVerboseLoggingEnabled) {
            logd(dbg + " clearTargetBssid " + bssid + " key=" + config.configKey());
        }
        mTargetRoamBSSID = bssid;
        return mWifiNative.setConfiguredNetworkBSSID(mInterfaceName, bssid);
    }

    /**
     * Set Config's default BSSID (for association purpose) and {@link #mTargetRoamBSSID}
     * @param config config need set BSSID
     * @param bssid  default BSSID to assocaite with when connect to this network
     * @return false -- does not change the current default BSSID of the configure
     *         true -- change the  current default BSSID of the configur
     */
    private boolean setTargetBssid(WifiConfiguration config, String bssid) {
        if (config == null || bssid == null) {
            return false;
        }
        if (config.BSSID != null) {
            bssid = config.BSSID;
            if (mVerboseLoggingEnabled) {
                Log.d(TAG, "force BSSID to " + bssid + "due to config");
            }
        }
        if (mVerboseLoggingEnabled) {
            Log.d(TAG, "setTargetBssid set to " + bssid + " key=" + config.configKey());
        }
        mTargetRoamBSSID = bssid;
        config.getNetworkSelectionStatus().setNetworkSelectionBSSID(bssid);
        return true;
    }

    private volatile IpClientManager mIpClient;
    private IpClientCallbacksImpl mIpClientCallbacks;

    // Channel for sending replies.
    private AsyncChannel mReplyChannel = new AsyncChannel();

    // Used to initiate a connection with WifiP2pService
    private AsyncChannel mWifiP2pChannel;

    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
    private AsyncChannel mIWCMonitorChannel = null;
    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION

    private WifiNetworkFactory mNetworkFactory;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    @GuardedBy("mNetworkAgentLock")
    private WifiNetworkAgent mNetworkAgent;
    private final Object mNetworkAgentLock = new Object();

    private byte[] mRssiRanges;

    // Used to filter out requests we couldn't possibly satisfy.
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();

    // Provide packet filter capabilities to ConnectivityService.
    private final NetworkMisc mNetworkMisc = new NetworkMisc();

    /* The base for wifi message types */
    static final int BASE = Protocol.BASE_WIFI;

    static final int CMD_BLUETOOTH_ADAPTER_STATE_CHANGE                 = BASE + 31;

    /* Supplicant commands */
    /* Add/update a network configuration */
    static final int CMD_ADD_OR_UPDATE_NETWORK                          = BASE + 52;
    /* Delete a network */
    static final int CMD_REMOVE_NETWORK                                 = BASE + 53;
    /* Enable a network. The device will attempt a connection to the given network. */
    static final int CMD_ENABLE_NETWORK                                 = BASE + 54;
    /* Get configured networks */
    static final int CMD_GET_CONFIGURED_NETWORKS                        = BASE + 59;
    /* Get adaptors */
    static final int CMD_GET_SUPPORTED_FEATURES                         = BASE + 61;
    /* Get configured networks with real preSharedKey */
    static final int CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS             = BASE + 62;
    /* Get Link Layer Stats thru HAL */
    static final int CMD_GET_LINK_LAYER_STATS                           = BASE + 63;
    /* Supplicant commands after driver start*/
    /* Set operational mode. CONNECT, SCAN ONLY, SCAN_ONLY with Wi-Fi off mode */
    static final int CMD_SET_OPERATIONAL_MODE                           = BASE + 72;
    /* Disconnect from a network */
    static final int CMD_DISCONNECT                                     = BASE + 73;
    /* Reconnect to a network */
    static final int CMD_RECONNECT                                      = BASE + 74;
    /* Reassociate to a network */
    static final int CMD_REASSOCIATE                                    = BASE + 75;

    /* Controls suspend mode optimizations
     *
     * When high perf mode is enabled, suspend mode optimizations are disabled
     *
     * When high perf mode is disabled, suspend mode optimizations are enabled
     *
     * Suspend mode optimizations include:
     * - packet filtering
     * - turn off roaming
     * - DTIM wake up settings
     */
    static final int CMD_SET_HIGH_PERF_MODE                             = BASE + 77;
    /* Enables RSSI poll */
    static final int CMD_ENABLE_RSSI_POLL                               = BASE + 82;
    /* RSSI poll */
    static final int CMD_RSSI_POLL                                      = BASE + 83;
    /** Runs RSSI poll once */
    static final int CMD_ONESHOT_RSSI_POLL                              = BASE + 84;
    /* Enable suspend mode optimizations in the driver */
    static final int CMD_SET_SUSPEND_OPT_ENABLED                        = BASE + 86;

    /* Enable TDLS on a specific MAC address */
    static final int CMD_ENABLE_TDLS                                    = BASE + 92;

    /**
     * Watchdog for protecting against b/16823537
     * Leave time for 4-way handshake to succeed
     */
    static final int ROAM_GUARD_TIMER_MSEC = 15000;

    int mRoamWatchdogCount = 0;
    /* Roam state watchdog */
    static final int CMD_ROAM_WATCHDOG_TIMER                            = BASE + 94;
    /* Screen change intent handling */
    static final int CMD_SCREEN_STATE_CHANGED                           = BASE + 95;

    /* Disconnecting state watchdog */
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER                   = BASE + 96;

    /* Remove a packages associated configurations */
    static final int CMD_REMOVE_APP_CONFIGURATIONS                      = BASE + 97;

    /* Disable an ephemeral network */
    static final int CMD_DISABLE_EPHEMERAL_NETWORK                      = BASE + 98;

    /* SIM is removed; reset any cached data for it */
    static final int CMD_RESET_SIM_NETWORKS                             = BASE + 101;

    /* OSU APIs */
    static final int CMD_QUERY_OSU_ICON                                 = BASE + 104;

    /* try to match a provider with current network */
    static final int CMD_MATCH_PROVIDER_NETWORK                         = BASE + 105;

    // Add or update a Passpoint configuration.
    static final int CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG                 = BASE + 106;

    // Remove a Passpoint configuration.
    static final int CMD_REMOVE_PASSPOINT_CONFIG                        = BASE + 107;

    // Get the list of installed Passpoint configurations.
    static final int CMD_GET_PASSPOINT_CONFIGS                          = BASE + 108;

    // Get the list of OSU providers associated with a Passpoint network.
    static final int CMD_GET_MATCHING_OSU_PROVIDERS                     = BASE + 109;

    // Get the list of installed Passpoint configurations matched with OSU providers
    static final int CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS = BASE + 110;

    /* Commands from/to the SupplicantStateTracker */
    /* Reset the supplicant state tracker */
    static final int CMD_RESET_SUPPLICANT_STATE                         = BASE + 111;

    // Get the list of wifi configurations for installed Passpoint profiles
    static final int CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES = BASE + 112;

    int mDisconnectingWatchdogCount = 0;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;

    /**
     * Indicates the end of boot process, should be used to trigger load from config store,
     * initiate connection attempt, etc.
     * */
    static final int CMD_BOOT_COMPLETED                                 = BASE + 134;
    /**
     * Initialize ClientModeImpl. This is currently used to initialize the
     * {@link HalDeviceManager} module.
     */
    static final int CMD_INITIALIZE                                     = BASE + 135;

    /* We now have a valid IP configuration. */
    static final int CMD_IP_CONFIGURATION_SUCCESSFUL                    = BASE + 138;
    /* We no longer have a valid IP configuration. */
    static final int CMD_IP_CONFIGURATION_LOST                          = BASE + 139;
    /* Link configuration (IP address, DNS, ...) changes notified via netlink */
    static final int CMD_UPDATE_LINKPROPERTIES                          = BASE + 140;

    /* Supplicant is trying to associate to a given BSSID */
    static final int CMD_TARGET_BSSID                                   = BASE + 141;

    static final int CMD_START_CONNECT                                  = BASE + 143;

    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT         = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED  = 1;
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN   = 2;

    static final int CMD_UNWANTED_NETWORK                               = BASE + 144;

    static final int CMD_START_ROAM                                     = BASE + 145;

    static final int CMD_ASSOCIATED_BSSID                               = BASE + 147;

    static final int CMD_NETWORK_STATUS                                 = BASE + 148;

    /* A layer 3 neighbor on the Wi-Fi link became unreachable. */
    static final int CMD_IP_REACHABILITY_LOST                           = BASE + 149;

    /* Remove a packages associated configrations */
    static final int CMD_REMOVE_USER_CONFIGURATIONS                     = BASE + 152;

    static final int CMD_ACCEPT_UNVALIDATED                             = BASE + 153;

    /* used to offload sending IP packet */
    static final int CMD_START_IP_PACKET_OFFLOAD                        = BASE + 160;

    /* used to stop offload sending IP packet */
    static final int CMD_STOP_IP_PACKET_OFFLOAD                         = BASE + 161;

    /* used to start rssi monitoring in hw */
    static final int CMD_START_RSSI_MONITORING_OFFLOAD                  = BASE + 162;

    /* used to stop rssi moniroting in hw */
    static final int CMD_STOP_RSSI_MONITORING_OFFLOAD                   = BASE + 163;

    /* used to indicated RSSI threshold breach in hw */
    static final int CMD_RSSI_THRESHOLD_BREACHED                        = BASE + 164;

    /* Enable/Disable WifiConnectivityManager */
    static final int CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER               = BASE + 166;


    /* Get FQDN list for Passpoint profiles matched with a given scanResults */
    static final int CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS = BASE + 168;

    /**
     * Used to handle messages bounced between ClientModeImpl and IpClient.
     */
    static final int CMD_IPV4_PROVISIONING_SUCCESS                      = BASE + 200;
    static final int CMD_IPV4_PROVISIONING_FAILURE                      = BASE + 201;

    /* Push a new APF program to the HAL */
    static final int CMD_INSTALL_PACKET_FILTER                          = BASE + 202;

    /* Enable/disable fallback packet filtering */
    static final int CMD_SET_FALLBACK_PACKET_FILTERING                  = BASE + 203;

    /* Enable/disable Neighbor Discovery offload functionality. */
    static final int CMD_CONFIG_ND_OFFLOAD                              = BASE + 204;

    /* used to indicate that the foreground user was switched */
    static final int CMD_USER_SWITCH                                    = BASE + 205;

    /* used to indicate that the foreground user was switched */
    static final int CMD_USER_UNLOCK                                    = BASE + 206;

    /* used to indicate that the foreground user was switched */
    static final int CMD_USER_STOP                                      = BASE + 207;

    /* Read the APF program & data buffer */
    static final int CMD_READ_PACKET_FILTER                             = BASE + 208;

    /** Used to add packet filter to apf. */
    static final int CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF = BASE + 209;

    /** Used to remove packet filter from apf. */
    static final int CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF = BASE + 210;

    public static final int CMD_SEND_DHCP_RELEASE                       = BASE + 211; //CscFeature_Wifi_SendSignalDuringPowerOff

    private static final int CMD_REPLACE_PUBLIC_DNS                     = BASE + 214; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_NETWORK_DIAGNOSTICS

    private static final int CMD_IMS_CALL_ESTABLISHED                   = BASE + 243; //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
    private static final int CMD_AUTO_CONNECT_CARRIER_AP_ENABLED        = BASE + 244; //SEC_PRODUCT_FEATURE_WLAN_AUTO_CONNECT_CARRIER_AP
    /* Indicates that diagnostics should time out a connection start event. */
    static final int CMD_DIAGS_CONNECT_TIMEOUT                          = BASE + 252;

    // Start subscription provisioning with a given provider
    private static final int CMD_START_SUBSCRIPTION_PROVISIONING        = BASE + 254;

    static final int CMD_THREE_TIMES_SCAN_IN_IDLE                       = BASE + 309; // SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE

    //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
    static final int CMD_SET_ADPS_MODE                                  = BASE + 311;

    @VisibleForTesting
    static final int CMD_PRE_DHCP_ACTION                                = BASE + 255;
    private static final int CMD_PRE_DHCP_ACTION_COMPLETE               = BASE + 256;
    private static final int CMD_POST_DHCP_ACTION                       = BASE + 257;


    private static final int CMD_UPDATE_CONFIG_LOCATION                 = BASE + 260; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION

    //+SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS
    /* Forcingly enable all networks after turning on */
    static final int CMD_FORCINGLY_ENABLE_ALL_NETWORKS                  = BASE + 330;
    //-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS

    /* SEC API */
    private static final int CMD_SEC_API_ASYNC                          = BASE + 501;
    private static final int CMD_SEC_API                                = BASE + 502;
    private static final int CMD_SEC_STRING_API                         = BASE + 503;
    public static final int CMD_SEC_LOGGING                             = BASE + 504;
    private static final int CMD_24HOURS_PASSED_AFTER_BOOT              = BASE + 507;

    private static final int CMD_GET_A_CONFIGURED_NETWORK               = BASE + 509; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
    private static final int CMD_SHOW_TOAST_MSG                         = BASE + 510;
    private static final int CMD_RELOAD_CONFIG_STORE_FILE               = BASE + 511; //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONFIG_MANAGER
    public static final int CMD_SCAN_RESULT_AVAILABLE                   = BASE + 512; //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP

    private static final int CMD_CHECK_ARP_RESULT                       = BASE + 550; //SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP
    private static final int CMD_SEND_ARP                               = BASE + 551; //SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP

    // For message logging.
    private static final Class[] sMessageClasses = {
            AsyncChannel.class, ClientModeImpl.class };
    private static final SparseArray<String> sGetWhatToString =
            MessageUtils.findMessageNames(sMessageClasses);


    /* Wifi state machine modes of operation */
    /* CONNECT_MODE - connect to any 'known' AP when it becomes available */
    public static final int CONNECT_MODE = 1;
    /* SCAN_ONLY_MODE - don't connect to any APs; scan, but only while apps hold lock */
    public static final int SCAN_ONLY_MODE = 2;
    /* SCAN_ONLY_WITH_WIFI_OFF - scan, but don't connect to any APs */
    public static final int SCAN_ONLY_WITH_WIFI_OFF_MODE = 3;
    /* DISABLED_MODE - Don't connect, don't scan, don't be an AP */
    public static final int DISABLED_MODE = 4;

    private static final int SUCCESS = 1;
    private static final int FAILURE = -1;

    /* Tracks if suspend optimizations need to be disabled by DHCP,
     * screen or due to high perf mode.
     * When any of them needs to disable it, we keep the suspend optimizations
     * disabled
     */
    private int mSuspendOptNeedsDisabled = 0;

    private static final int SUSPEND_DUE_TO_DHCP = 1;
    private static final int SUSPEND_DUE_TO_HIGH_PERF = 1 << 1;
    private static final int SUSPEND_DUE_TO_SCREEN = 1 << 2;

    /**
     * Time window in milliseconds for which we send
     * {@link NetworkAgent#explicitlySelected(boolean, boolean)}
     * after connecting to the network which the user last selected.
     */
    @VisibleForTesting
    public static final int LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS = 30 * 1000;

    /* Tracks if user has enabled suspend optimizations through settings */
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);

    /* Tracks if user has enabled Connected Mac Randomization through settings */

    /**
     * Supplicant scan interval in milliseconds.
     * Comes from {@link Settings.Global#WIFI_SUPPLICANT_SCAN_INTERVAL_MS} or
     * from the default config if the setting is not set
     */
    private long mSupplicantScanIntervalMs;

    int mRunningBeaconCount = 0;

    //+SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE
    private static final int MAX_SCAN_RESULTS_EVENT_COUNT_IN_IDLE = 2;
    private int mScanResultsEventCounter;
    //-SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE

    private final WifiGeofenceManager mWifiGeofenceManager; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE

    /* Default parent state */
    private State mDefaultState = new DefaultState();
    /* Connecting to an access point */
    private State mConnectModeState = new ConnectModeState();
    /* Connected at 802.11 (L2) level */
    private State mL2ConnectedState = new L2ConnectedState();
    /* fetching IP after connection to access point (assoc+auth complete) */
    private State mObtainingIpState = new ObtainingIpState();
    /* Connected with IP addr */
    private State mConnectedState = new ConnectedState();
    /* Roaming */
    private State mRoamingState = new RoamingState();
    /* disconnect issued, waiting for network disconnect confirmation */
    private State mDisconnectingState = new DisconnectingState();
    /* Network is not connected, supplicant assoc+auth is not complete */
    private State mDisconnectedState = new DisconnectedState();

    /*
     * Here is Samsung SemFloatingFeature - SEC_FLOATING_FEATURE_WIFI_XXX
     */

    private static final boolean ENABLE_SUPPORT_QOSCONTROL = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL");

    /**
     * One of  {@link WifiManager#WIFI_STATE_DISABLED},
     * {@link WifiManager#WIFI_STATE_DISABLING},
     * {@link WifiManager#WIFI_STATE_ENABLED},
     * {@link WifiManager#WIFI_STATE_ENABLING},
     * {@link WifiManager#WIFI_STATE_UNKNOWN}
     */
    private final AtomicInteger mWifiState = new AtomicInteger(WIFI_STATE_DISABLED);

    /**
     * Work source to use to blame usage on the WiFi service
     */
    public static final WorkSource WIFI_WORK_SOURCE = new WorkSource(Process.WIFI_UID);

    /**
     * Keep track of whether WIFI is running.
     */
    private boolean mIsRunning = false;

    /**
     * Keep track of whether we last told the battery stats we had started.
     */
    private boolean mReportedRunning = false;

    /**
     * Most recently set source of starting WIFI.
     */
    private final WorkSource mRunningWifiUids = new WorkSource();

    /**
     * The last reported UIDs that were responsible for starting WIFI.
     */
    private final WorkSource mLastRunningWifiUids = new WorkSource();

    private TelephonyManager mTelephonyManager;
    private TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            mTelephonyManager = mWifiInjector.makeTelephonyManager();
        }
        return mTelephonyManager;
    }

    private final IBatteryStats mBatteryStats;

    private final String mTcpBufferSizes;

    // Used for debug and stats gathering
    private static int sScanAlarmIntentCount = 0;

    private FrameworkFacade mFacade;
    private WifiStateTracker mWifiStateTracker;
    private final BackupManagerProxy mBackupManagerProxy;
    private final WrongPasswordNotifier mWrongPasswordNotifier;
    private boolean mIsImsCallEstablished; //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER

    private WifiNetworkSuggestionsManager mWifiNetworkSuggestionsManager;
    private boolean mConnectedMacRandomzationSupported;

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
    private SemLocationManager mSemLocationManager;
    PendingIntent mLocationPendingIntent;
    private int mLocationRequestNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
    private static final int ACTIVE_REQUEST_LOCATION = 1;
    private static final String ACTION_AP_LOCATION_PASSIVE_REQUEST = "com.android.server.wifi.AP_LOCATION_PASSIVE_REQUEST";
    private static double INVALID_LATITUDE_LONGITUDE = 1000L;
    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION

    /**
     * If semConcurrentEnabled is true, do belows
     * 1) stop all scan
     * 2) stop all anqp
     * 3) TODO: cancle or stop wps
     */
    private boolean mConcurrentEnabled; //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
    private boolean mIsPasspointEnabled; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20

    //[SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
    //EAP_EVENT
    public static final int EAP_EVENT_ANONYMOUS_IDENTITY_UPDATED = 1;
    public static final int EAP_EVENT_DEAUTH_8021X_AUTH_FAILED = 2;
    public static final int EAP_EVENT_EAP_FAILURE = 3;
    public static final int EAP_EVENT_ERROR_MESSAGE = 4;
    public static final int EAP_EVENT_LOGGING = 5;
    public static final int EAP_EVENT_NO_CREDENTIALS = 6;
    public static final int EAP_EVENT_NOTIFICATION = 7;
    public static final int EAP_EVENT_SUCCESS = 8;
    public static final int EAP_EVENT_TLS_ALERT = 9;
    public static final int EAP_EVENT_TLS_CERT_ERROR = 10;
    public static final int EAP_EVENT_TLS_HANDSHAKE_FAIL = 11;

    //EAP_NOTIFICATION
    public static final int EAP_NOTIFICATION_KT_WIFI_SUCCESS = 0;
    public static final int EAP_NOTIFICATION_KT_WIFI_INVALID_USIM = 1;
    public static final int EAP_NOTIFICATION_KT_WIFI_NO_USIM = 2;
    public static final int EAP_NOTIFICATION_KT_WIFI_WEP_PSK_INVALID_KEY = 3;
    public static final int EAP_NOTIFICATION_KT_WIFI_INVALID_IDPW = 4;
    public static final int EAP_NOTIFICATION_KT_WIFI_AUTH_FAIL = 5;
    public static final int EAP_NOTIFICATION_KT_WIFI_NO_RESPONSE = 6;
    public static final int EAP_NOTIFICATION_NO_NOTIFICATION_INFORMATION = 987654321;
    //]SEC_PRODUCT_FEATURE_WLAN_EAP_XXX

    public ClientModeImpl(Context context, FrameworkFacade facade, Looper looper,
                            UserManager userManager, WifiInjector wifiInjector,
                            BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode,
                            WifiNative wifiNative, WrongPasswordNotifier wrongPasswordNotifier,
                            SarManager sarManager, WifiTrafficPoller wifiTrafficPoller,
                            LinkProbeManager linkProbeManager) {
        super(TAG, looper);
        mWifiInjector = wifiInjector;
        mWifiMetrics = mWifiInjector.getWifiMetrics();
        mClock = wifiInjector.getClock();
        mPropertyService = wifiInjector.getPropertyService();
        mBuildProperties = wifiInjector.getBuildProperties();
        mWifiScoreCard = wifiInjector.getWifiScoreCard();
        mContext = context;
        mFacade = facade;
        mWifiNative = wifiNative;
        mBackupManagerProxy = backupManagerProxy;
        mWrongPasswordNotifier = wrongPasswordNotifier;
        mSarManager = sarManager;
        mSemSarManager = new SemSarManager(mContext, mWifiNative); //SEMSAR
        mWifiTrafficPoller = wifiTrafficPoller;
        mLinkProbeManager = linkProbeManager;

        mNetworkInfo = new NetworkInfo(ConnectivityManager.TYPE_WIFI, 0, NETWORKTYPE, "");
        mBatteryStats = IBatteryStats.Stub.asInterface(mFacade.getService(
                BatteryStats.SERVICE_NAME));
        mWifiStateTracker = wifiInjector.getWifiStateTracker();
        IBinder b = mFacade.getService(Context.NETWORKMANAGEMENT_SERVICE);

        mP2pSupported = mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_WIFI_DIRECT);

        mWifiPermissionsUtil = mWifiInjector.getWifiPermissionsUtil();
        mWifiConfigManager = mWifiInjector.getWifiConfigManager();

        mPasspointManager = mWifiInjector.getPasspointManager();

        mWifiMonitor = mWifiInjector.getWifiMonitor();
        mWifiDiagnostics = mWifiInjector.getWifiDiagnostics();
        mScanRequestProxy = mWifiInjector.getScanRequestProxy(); //SEC_PRODUCT_FEATURE_WLAN_SCAN_CONTROLLER
        mWifiPermissionsWrapper = mWifiInjector.getWifiPermissionsWrapper();
        mWifiDataStall = mWifiInjector.getWifiDataStall();

        mWifiInfo = new ExtendedWifiInfo();
        mSupplicantStateTracker =
                mFacade.makeSupplicantStateTracker(context, mWifiConfigManager, getHandler());
        mWifiConnectivityManager = mWifiInjector.makeWifiConnectivityManager(this);

        mWifiGeofenceManager = mWifiInjector.getWifiGeofenceManager(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
        if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
            mWifiGeofenceManager.register(mWifiConnectivityManager);
            mWifiGeofenceManager.register(mNetworkInfo);
        }

        mLinkProperties = new LinkProperties();
        mOldLinkProperties = new LinkProperties();

        mMcastLockManagerFilterController = new McastLockManagerFilterController();

        mNetworkInfo.setIsAvailable(false);
        mLastBssid = null;
        mLastNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
        mLastConnectedNetworkId = WifiConfiguration.INVALID_NETWORK_ID; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        mLastSignalLevel = -1;

        mCountryCode = countryCode;

        mWifiScoreReport = new WifiScoreReport(mWifiInjector.getScoringParams(), mClock);

        mDelayDisconnect = new WifiDelayDisconnect(mContext, mWifiInjector); //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION

        mNetworkCapabilitiesFilter.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
        // TODO - needs to be a bit more dynamic
        mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(1024 * 1024);
        mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(1024 * 1024);
        mNetworkCapabilitiesFilter.setNetworkSpecifier(new MatchAllNetworkSpecifier());
        // Make the network factories.
        mNetworkFactory = mWifiInjector.makeWifiNetworkFactory(
                mNetworkCapabilitiesFilter, mWifiConnectivityManager);
        // We can't filter untrusted network in the capabilities filter because a trusted
        // network would still satisfy a request that accepts untrusted ones.
        // We need a second network factory for untrusted network requests because we need a
        // different score filter for these requests.
        mUntrustedNetworkFactory = mWifiInjector.makeUntrustedWifiNetworkFactory(
                mNetworkCapabilitiesFilter, mWifiConnectivityManager);

        mWifiNetworkSuggestionsManager = mWifiInjector.getWifiNetworkSuggestionsManager();

        //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
        mWifiAdpsEnabled.set(Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.WIFI_ADPS, 0) == 1);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
        filter.addAction(PowerManager.ACTION_SCREEN_ON_BY_PROXIMITY);
        filter.addAction(PowerManager.ACTION_SCREEN_OFF_BY_PROXIMITY);
        //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();

                        if (action.equals(Intent.ACTION_SCREEN_ON)) {
                            sendMessage(CMD_SCREEN_STATE_CHANGED, 1);
                        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                            sendMessage(CMD_SCREEN_STATE_CHANGED, 0);
                        } else if (action.equals(PowerManager.ACTION_SCREEN_ON_BY_PROXIMITY)) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
                            sendMessage(CMD_SCREEN_STATE_CHANGED, 1);
                        } else if (action.equals(PowerManager.ACTION_SCREEN_OFF_BY_PROXIMITY)) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
                            sendMessage(CMD_SCREEN_STATE_CHANGED, 0);
                        }
                    }
                }, filter);
        //++SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL
        IntentFilter qosControlFilter = new IntentFilter();
        if (ENABLE_SUPPORT_QOSCONTROL) {
            qosControlFilter.addAction("com.samsung.android.game.intent.action.WIFI_QOS_CONTROL_START");
            qosControlFilter.addAction("com.samsung.android.game.intent.action.WIFI_QOS_CONTROL_END");
        }

        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        mPersistQosTid = intent.getIntExtra("tid", 0);
                        mPersistQosUid = intent.getIntExtra("uid", 0);

                        if (action.equals("com.samsung.android.game.intent.action.WIFI_QOS_CONTROL_START")) {
                            mQosGameIsRunning = true;
                        } else if (action.equals("com.samsung.android.game.intent.action.WIFI_QOS_CONTROL_END")){
                            mQosGameIsRunning = false;
                        }

                        if (isConnected()) {
                            Log.d(TAG,"isConnected");
                            mWifiNative.setTidMode(mInterfaceName, mQosGameIsRunning ? 2 : 0, mPersistQosUid, mPersistQosTid);
                        }
                    }
                }, qosControlFilter, "android.permission.HARDWARE_TEST", null);
        //--SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL

        mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(
                        Settings.Secure.WIFI_HOTSPOT20_ENABLE), false, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        int passpointEnabled = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE, 1);

                        if (passpointEnabled == 1 || passpointEnabled == 3) {
                            mIsPasspointEnabled = true;
                        } else {
                            mIsPasspointEnabled = false;
                        }

                        mPasspointManager.setPasspointEnabled(mIsPasspointEnabled);
                        mWifiNative.setInterwokingEnabled(mInterfaceName, mIsPasspointEnabled);

                        if (mWifiState.get() == WifiManager.WIFI_STATE_ENABLED) {
                            updatePasspointNetworkSelectionStatus(mIsPasspointEnabled);
                        } else {
                            Log.e(TAG, "WIFI_HOTSPOT20_ENABLE change to : " + mIsPasspointEnabled + ", but mWifiState is invalid : " + mWifiState.get());
                        }
                    }
                });

        //+SEC_PRODUCT_FEATURE_WLAN_ISSUETRACKER_CONTROL
        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle extras;
                        extras = intent.getExtras();
                        if (extras != null && extras.getSerializable("ONOFF") != null) {
                            mIssueTrackerOn = (Boolean)extras.getSerializable("ONOFF");
                            // SEC_PRODUCT_FEATURE_WLAN_DEBUG_LEVEL
                            if (mIssueTrackerOn)
                                updateWlanDebugLevel();
                        }
                    }
                },
        new IntentFilter("com.sec.android.ISSUE_TRACKER_ONOFF"));
        //-SEC_PRODUCT_FEATURE_WLAN_ISSUETRACKER_CONTROL

        mFacade.registerContentObserver(mContext, Settings.Global.getUriFor(
                        Settings.Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED), false,
                new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        mUserWantsSuspendOpt.set(mFacade.getIntegerSetting(mContext,
                                Settings.Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED, 1) == 1);
                    }
                });

        //+SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
        mFacade.registerContentObserver(mContext, Settings.Secure.getUriFor(
                        Settings.Secure.WIFI_ADPS), false,
                new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        mWifiAdpsEnabled.set(Settings.Secure.getInt(mContext.getContentResolver(),
                                Settings.Secure.WIFI_ADPS, 0) == 1);
                        sendMessage(CMD_SET_ADPS_MODE);
                    }
                });
        //-SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
        mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor(
                        Settings.Global.SAFE_WIFI), false,
                new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        boolean safeModeEnabled = Settings.Global.getInt(mContext.getContentResolver(),
                            Settings.Global.SAFE_WIFI, 0) == 1;

                        boolean heEnabled = Settings.Global.getInt(mContext.getContentResolver(),
                            Settings.Global.SAFE_WIFI, 0) == 0;

                        if (!mWifiNative.setSafeMode(mInterfaceName, safeModeEnabled)) {
                            Log.e(TAG, "Failed to set safe Wi-Fi mode");
                        }

                        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_80211AX) {
                            if (!mWifiNative.setHeEnabled(heEnabled)) {
                                Log.e(TAG, "Failed to set safe Wi-Fi mode (HE Control)");
                            }
                        }
                    }
                });

        mUserWantsSuspendOpt.set(mFacade.getIntegerSetting(mContext,
                Settings.Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED, 1) == 1);

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getName());

        mSuspendWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiSuspend");
        mSuspendWakeLock.setReferenceCounted(false);

        IntentFilter intentFilter = new IntentFilter();

        if (CSC_SUPPORT_5G_ANT_SHARE) {
            intentFilter.addAction("android.intent.action.coexstatus");
        }

        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            mWifiPolicy = EnterpriseDeviceManager.getInstance().getWifiPolicy();
            intentFilter.addAction(ACTION_ENABLE_NETWORK_INTERNAL);
        }

        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(WifiPolicy.ACTION_ENABLE_NETWORK_INTERNAL)
                        && SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
                    ArrayList<Integer> netIdsList = intent.getIntegerArrayListExtra(EXTRA_NETID_INTERNAL);
                    boolean enableOthers = intent
                            .getBooleanExtra(EXTRA_ENABLE_OTHERS_INTERNAL, false);        
                    if (netIdsList != null) {
                        for(int netId : netIdsList) {
                            if(netId != WifiConfiguration.INVALID_NETWORK_ID) {
                                mWifiConfigManager.enableNetwork(netId, enableOthers,
                                    Process.SYSTEM_UID);
                            }                                
                        }
                    } else {
                        Log.w(TAG,
                                "BroadcastReceiver - WifiPolicy.ACTION_ENABLE_NETWORK_INTERNAL : netIdList is null");
                    }
                 }
                 else if (intent.getAction().equals("android.intent.action.coexstatus")) { //CSC_SUPPORT_5G_ANT_SHARE
                    int state = intent.getIntExtra("STATUS", 0);
                    if (state == 1) {
                        mScellEnter = true;
                    } else {
                        mScellEnter = false;
                    }
                    Log.e(TAG, "get android.intent.action.coexstatus mScellEnter : " + mScellEnter);
                    if (mScellEnter)
                        sendIpcMessageToRilForLteu(LTEU_STA_5GHZ_CONNECTED, isConnected(), mWifiInfo.is5GHz(), true);
                }
            }
        }, intentFilter);

        if (CSC_SUPPORT_5G_ANT_SHARE) {
            mContext.registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            NetworkInfo ni = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                            WifiP2pGroup wpg = (WifiP2pGroup) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP_INFO);
                            mIsP2pConnected = (ni != null && ni.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) ? true : false;
                            if (wpg != null && mIsP2pConnected == true) {
                                mCurrentP2pFreq = wpg.getFrequency();
                                sendIpcMessageToRilForLteu(LTEU_P2P_5GHZ_CONNECTED, true, (mCurrentP2pFreq/1000 == 5), false);
                            } else {
                                mCurrentP2pFreq = 0;
                                sendIpcMessageToRilForLteu(LTEU_P2P_5GHZ_CONNECTED, false, false, false);
                            }
                        }
                    },
                    new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION));
        }

        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        boolean is5GHz = false;
                        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED);
                        if (state == WifiManager.WIFI_AP_STATE_ENABLED) {
                            if(CSC_SUPPORT_5G_ANT_SHARE) {
                                WifiApConfigStore mWifiApConfigStore;
                                mWifiApConfigStore = WifiInjector.getInstance().getWifiApConfigStore();
                                WifiConfiguration tempWifiConfig =  mWifiApConfigStore.getApConfiguration();
                                if (tempWifiConfig != null) {
                                    is5GHz = (tempWifiConfig.apChannel > 14);
                                    sendIpcMessageToRilForLteu(LTEU_MOBILEHOTSPOT_5GHZ_ENABLED, true, is5GHz, false);
                                }
                            }
                            if(WifiInjector.getInstance().getSemWifiApChipInfo().supportWifiSharing()) mApInterfaceName = "swlan0";
                            else mApInterfaceName = "wlan0";
                            if(mFirstTurnOn) {
                                Log.d(TAG, "mFirstTurnOn true initialize mConnectivityPacketLogForHotspot");
                                sPktLogsMhs.putIfAbsent(mApInterfaceName, new LocalLog(MAX_PACKET_RECORDS));
                                mConnectivityPacketLogForHotspot = sPktLogsMhs.get(mApInterfaceName);
                            }
                            if(mFirstTurnOn && WifiInjector.getInstance().getSemWifiApChipInfo().supportWifiSharing()) {
                                Log.d(TAG, "mFirstTurnOn true initialize mConnectivityPacketLogForWlan0");
                                sPktLogsWlan.putIfAbsent(INTERFACENAMEOFWLAN, new LocalLog(MAX_PACKET_RECORDS));
                                mConnectivityPacketLogForWlan0 = sPktLogsWlan.get(INTERFACENAMEOFWLAN);
                            }
                            mPacketTrackerForHotspot = createPacketTracker(InterfaceParams.getByName(mApInterfaceName), mConnectivityPacketLogForHotspot);
                            if (mPacketTrackerForHotspot != null) {
                                Log.d(TAG, "mPacketTrackerForHotspot start");
                                try {
                                    mPacketTrackerForHotspot.start(mApInterfaceName);
                                } catch ( NullPointerException e) {
                                // TODO: throw new IllegalStateException.
                                    Log.e(TAG,"Failed to start tracking interface : " + e);
                                }
                            }
                            if(WifiInjector.getInstance().getSemWifiApChipInfo().supportWifiSharing()) {
                                mPacketTrackerForWlan0 = createPacketTracker(InterfaceParams.getByName(INTERFACENAMEOFWLAN), mConnectivityPacketLogForWlan0);
                                if (mPacketTrackerForWlan0 != null) {
                                    Log.d(TAG, "mPacketTrackerForwlan0 start");
                                    try {
                                        mPacketTrackerForWlan0.start(INTERFACENAMEOFWLAN);
                                    } catch ( NullPointerException e) {
                                    // TODO: throw new IllegalStateException.
                                        Log.e(TAG,"Failed to start tracking interface : " + e);
                                    }
                                }
                            }
                            if(mFirstTurnOn)
                                mFirstTurnOn = false;
                        } else if (state == WifiManager.WIFI_AP_STATE_DISABLED || state == WifiManager.WIFI_AP_STATE_FAILED) {
                            if(CSC_SUPPORT_5G_ANT_SHARE)
                                sendIpcMessageToRilForLteu(LTEU_MOBILEHOTSPOT_5GHZ_ENABLED, false, false, false);
                            if (mPacketTrackerForHotspot != null) {
                                mPacketTrackerForHotspot.stop();
                                mPacketTrackerForHotspot = null;
                                if(WifiInjector.getInstance().getSemWifiApChipInfo().supportWifiSharing() && mPacketTrackerForWlan0 != null ) {
                                    mPacketTrackerForWlan0.stop();
                                    mPacketTrackerForWlan0 = null;
                                }
                            }
                        }
                    }
                },
                new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION));

        mContext.registerReceiver( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d(TAG,"receive ACTION_AP_LOCATION_PASSIVE_REQUEST");
                        Bundle bundle = intent.getExtras();
                        Location location = (Location) bundle.get(SemLocationManager.CURRENT_LOCATION);
                        WifiConfiguration config = getCurrentWifiConfiguration();
                        if (config != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            if (isValidLocation(latitude,longitude)) {
                                mWifiGeofenceManager.setLatitudeAndLongitude(config, latitude, longitude);
                                if (mLocationPendingIntent != null) {
                                    mSemLocationManager.removePassiveLocation(mLocationPendingIntent);
                                }
                            }
                        } else {
                            Log.d(TAG,"There is no config to update location");
                        }
                    }
                }, new IntentFilter(ACTION_AP_LOCATION_PASSIVE_REQUEST));

        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            mWifiPolicy = EnterpriseDeviceManager.getInstance().getWifiPolicy();
            intentFilter.addAction(ACTION_ENABLE_NETWORK_INTERNAL);
        }

        if(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO) {
            //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
            mFacade.registerContentObserver(mContext, Settings.Global.getUriFor(
                            Settings.Global.DATA_ROAMING), false,
                    new ContentObserver(getHandler()) {
                        @Override
                        public void onChange(boolean selfChange) {
                            Log.d(TAG, "Settings.Global.DATA_ROAMING: onChange=" + selfChange);
                            handleCellularCapabilities();
                        }
                    });

            IntentFilter cellularIntentFilter = new IntentFilter();
            cellularIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            cellularIntentFilter.addAction(DATA_LIMIT_INTENT);

            mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        switch (action) {
                            case DATA_LIMIT_INTENT: {
                                mIsPolicyMobileData = intent.getBooleanExtra("policyData", false);
                                Log.d(TAG, "DATA_LIMIT_INTENT " + mIsPolicyMobileData);
                                handleCellularCapabilities();
                                break;
                            }
                            case ConnectivityManager.CONNECTIVITY_ACTION: {
                                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                                if (networkInfo != null) {
                                    Log.d(TAG, "mCellularReceiver: action=" + action + ", Network type="
                                        + networkInfo.getType() + ", isConnected=" + networkInfo.isConnected());
                                    handleCellularCapabilities();
                                }
                                break;
                            }
                            default: {
                                Log.d(TAG, "mCellularReceiver: undefined case: action=" + action);
                                break;
                            }
                        }
                    }
                }, cellularIntentFilter);
            //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
        }

        mConnectedMacRandomzationSupported = mContext.getResources()
                .getBoolean(R.bool.config_wifi_connected_mac_randomization_supported);
        mWifiInfo.setEnableConnectedMacRandomization(mConnectedMacRandomzationSupported);
        mWifiMetrics.setIsMacRandomizationOn(mConnectedMacRandomzationSupported);
/*
        mTcpBufferSizes = mContext.getResources().getString(
                R.string.config_wifi_tcp_buffers);
*/
        mTcpBufferSizes = SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_CONFIG_TCP_BUFFERSIZE;

        // CHECKSTYLE:OFF IndentationCheck
        addState(mDefaultState);
            addState(mConnectModeState, mDefaultState);
                addState(mL2ConnectedState, mConnectModeState);
                    addState(mObtainingIpState, mL2ConnectedState);
                    addState(mConnectedState, mL2ConnectedState);
                    addState(mRoamingState, mL2ConnectedState);
                addState(mDisconnectingState, mConnectModeState);
                addState(mDisconnectedState, mConnectModeState);
        // CHECKSTYLE:ON IndentationCheck

        setInitialState(mDefaultState);

        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_REDUCE_LOWRAM_DEVICE_MAXLOG_SIZE) {
            setLogRecSize(500);
        } else {
            setLogRecSize(DBG ? NUM_LOG_RECS_VERBOSE : NUM_LOG_RECS_NORMAL);
        }
        setLogOnlyTransitions(false);

        //mNetworkAutoConnectEnabled default set
        if (Vendor.ATT == mOpBranding) { //TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT ATT
            mWifiConfigManager.setNetworkAutoConnect((Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.WIFI_AUTO_CONNECT, 1) == 1));
            if (DBG) logi("ATT set mNetworkAutoConnectEnabled = " + mWifiConfigManager.getNetworkAutoConnectEnabled());
        }

        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        mBigDataManager = new WifiBigDataLogManager(mContext, looper,
                new WifiBigDataLogManager.WifiBigDataLogAdapter() {
                        @Override
                        public List<WifiConfiguration> getSavedNetworks() {
                            return mWifiConfigManager.getSavedNetworks(Process.WIFI_UID);
                        }

                        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING - CHIPSET_VENDOR_OUI
                        @Override
                        public String getChipsetOuis() {
                            StringBuilder sb = new StringBuilder("00:00:00");
                            ScanDetailCache scanDetailCache =
                                    mWifiConfigManager.getScanDetailCacheForNetwork(mLastNetworkId);
                            if (scanDetailCache != null) {
                                ScanDetail scanDetail =
                                        scanDetailCache.getScanDetail(mLastBssid);
                                if (scanDetail != null) {
                                    NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                                    if (networkDetail != null) {
                                        Set<Integer> chipset_ouis = networkDetail.getChipsetOuis();
                                        String prefix = "";
                                        int oui_count = 0;
                                        for (int chipset_oui : chipset_ouis) {
                                            if (oui_count == 0) sb.setLength(0);
                                            sb.append(prefix);
                                            prefix = ",";
                                            sb.append(NetworkDetail.toOUIString(chipset_oui));
                                            if (++oui_count >= 5/*max # of chip OUIs*/) break;
                                        }
                                    }
                                }
                            }
                            return sb.toString();
                        }
                        //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING - CHIPSET_VENDOR_OUI
                });
        mIssueDetector = mWifiInjector.getIssueDetector(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
        mSettingsStore = mWifiInjector.getWifiSettingsStore(); //SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
            int passpointEnabled = 0;

            try {
                passpointEnabled = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE);

                if (passpointEnabled == 1 || passpointEnabled == 3) {
                    mIsPasspointEnabled = true;
                } else {
                    mIsPasspointEnabled = false;
                }
            } catch (SettingNotFoundException ex) {
                Log.e(TAG, "WIFI_HOTSPOT20_ENABLE SettingNotFoundException");
                String passpointCscFeature = OpBrandingLoader.getInstance().getMenuStatusForPasspoint();
                if (TextUtils.isEmpty(passpointCscFeature) || passpointCscFeature.contains("DEFAULT_ON")) {
                    passpointEnabled = 3;
                } else if (passpointCscFeature.contains("DEFAULT_OFF")) {
                    passpointEnabled = 2;
                }

                if (passpointEnabled == 1 || passpointEnabled == 3) {
                    mIsPasspointEnabled = true;
                } else {
                    mIsPasspointEnabled = false;
                }

                Settings.Secure.putInt(mContext.getContentResolver(),Settings.Secure.WIFI_HOTSPOT20_ENABLE, passpointEnabled);
            }

            mPasspointManager.setPasspointEnabled(mIsPasspointEnabled);
            mPasspointManager.setVendorSimUseable(false);
        }

        mWifiB2bConfigPolicy = mWifiInjector.getWifiB2bConfigPolicy(); //EDM Wi-Fi Configuration
    }

    @Override
    public void start() {
        super.start();

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);

        // Learn the initial state of whether the screen is on.
        // We update this field when we receive broadcasts from the system.
        handleScreenStateChanged(powerManager.isInteractive());
    }

    private void registerForWifiMonitorEvents()  {
        mWifiMonitor.registerHandler(mInterfaceName, CMD_TARGET_BSSID, getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, CMD_ASSOCIATED_BSSID, getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.ANQP_DONE_EVENT, getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.GAS_QUERY_DONE_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.GAS_QUERY_START_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.HS20_REMEDIATION_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.RX_HS20_ANQP_ICON_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.SUP_REQUEST_IDENTITY,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.SUP_REQUEST_SIM_AUTH,
                getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.EAP_EVENT_MESSAGE,
                getHandler()); //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.BSSID_PRUNED_EVENT,
                getHandler()); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.SUP_BIGDATA_EVENT,
                getHandler());  //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.ASSOCIATION_REJECTION_EVENT,
                mWifiMetrics.getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.AUTHENTICATION_FAILURE_EVENT,
                mWifiMetrics.getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.NETWORK_CONNECTION_EVENT,
                mWifiMetrics.getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.NETWORK_DISCONNECTION_EVENT,
                mWifiMetrics.getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT,
                mWifiMetrics.getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, CMD_ASSOCIATED_BSSID,
                mWifiMetrics.getHandler());
        mWifiMonitor.registerHandler(mInterfaceName, CMD_TARGET_BSSID,
                mWifiMetrics.getHandler());
    }

    private void setMulticastFilter(boolean enabled) {
        if (mIpClient != null) {
            mIpClient.setMulticastFilter(enabled);
        }
    }

    /**
     * Class to implement the MulticastLockManager.FilterController callback.
     */
    class McastLockManagerFilterController implements WifiMulticastLockManager.FilterController {
        /**
         * Start filtering Multicast v4 packets
         */
        public void startFilteringMulticastPackets() {
            setMulticastFilter(true);
        }

        /**
         * Stop filtering Multicast v4 packets
         */
        public void stopFilteringMulticastPackets() {
            setMulticastFilter(false);
        }
    }

    class IpClientCallbacksImpl extends IpClientCallbacks {
        private final ConditionVariable mWaitForCreationCv = new ConditionVariable(false);
        private final ConditionVariable mWaitForStopCv = new ConditionVariable(false);

        @Override
        public void onIpClientCreated(IIpClient ipClient) {
            mIpClient = new IpClientManager(ipClient, getName());
            mWaitForCreationCv.open();
        }

        @Override
        public void onPreDhcpAction() {
            sendMessage(CMD_PRE_DHCP_ACTION);
        }

        @Override
        public void onPostDhcpAction() {
            sendMessage(CMD_POST_DHCP_ACTION);
        }

        @Override
        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults != null) {
                sendMessage(CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
            } else {
                sendMessage(CMD_IPV4_PROVISIONING_FAILURE);
            }
        }

        @Override
        public void onProvisioningSuccess(LinkProperties newLp) {
            mWifiMetrics.logStaEvent(StaEvent.TYPE_CMD_IP_CONFIGURATION_SUCCESSFUL);
            sendMessage(CMD_UPDATE_LINKPROPERTIES, newLp);
            sendMessage(CMD_IP_CONFIGURATION_SUCCESSFUL);
        }

        @Override
        public void onProvisioningFailure(LinkProperties newLp) {
            mWifiMetrics.logStaEvent(StaEvent.TYPE_CMD_IP_CONFIGURATION_LOST);
            sendMessage(CMD_IP_CONFIGURATION_LOST);
        }

        @Override
        public void onLinkPropertiesChange(LinkProperties newLp) {
            sendMessage(CMD_UPDATE_LINKPROPERTIES, newLp);
        }

        @Override
        public void onReachabilityLost(String logMsg) {
            mWifiMetrics.logStaEvent(StaEvent.TYPE_CMD_IP_REACHABILITY_LOST);
            sendMessage(CMD_IP_REACHABILITY_LOST, logMsg);
        }

        @Override
        public void installPacketFilter(byte[] filter) {
            sendMessage(CMD_INSTALL_PACKET_FILTER, filter);
        }

        @Override
        public void startReadPacketFilter() {
            sendMessage(CMD_READ_PACKET_FILTER);
        }

        @Override
        public void setFallbackMulticastFilter(boolean enabled) {
            sendMessage(CMD_SET_FALLBACK_PACKET_FILTERING, enabled);
        }

        @Override
        public void setNeighborDiscoveryOffload(boolean enabled) {
            sendMessage(CMD_CONFIG_ND_OFFLOAD, (enabled ? 1 : 0));
        }

        @Override
        public void onQuit() {
            mWaitForStopCv.open();
        }

        boolean awaitCreation() {
            return mWaitForCreationCv.block(IPCLIENT_TIMEOUT_MS);
        }

        boolean awaitShutdown() {
            return mWaitForStopCv.block(IPCLIENT_TIMEOUT_MS);
        }
    }

    private void stopIpClient() {
        /* Restore power save and suspend optimizations */
        handlePostDhcpSetup();
        if (mIpClient != null) {
            mIpClient.stop();
        }
    }

    /**
     * Set wpa_supplicant log level using |mVerboseLoggingLevel| flag.
     */
    void setSupplicantLogLevel() {
        mWifiNative.setSupplicantLogLevel(mVerboseLoggingEnabled);
    }

    /**
     * Method to update logging level in wifi service related classes.
     *
     * @param verbose int logging level to use
     */
    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            mVerboseLoggingEnabled = true;
            DBG = true;
            setLogRecSize(ActivityManager.isLowRamDeviceStatic()
                    ? NUM_LOG_RECS_VERBOSE_LOW_MEMORY : NUM_LOG_RECS_VERBOSE);
        } else {
            mVerboseLoggingEnabled = false;
            DBG = false;
            setLogRecSize(NUM_LOG_RECS_NORMAL);
        }
        configureVerboseHalLogging(mVerboseLoggingEnabled);
        setSupplicantLogLevel();
        mCountryCode.enableVerboseLogging(verbose);
        mWifiScoreReport.enableVerboseLogging(mVerboseLoggingEnabled);
        mWifiDiagnostics.startLogging(mVerboseLoggingEnabled);
        mWifiMonitor.enableVerboseLogging(verbose);
        mWifiNative.enableVerboseLogging(verbose);
        mWifiConfigManager.enableVerboseLogging(verbose);
        mSupplicantStateTracker.enableVerboseLogging(verbose);
        mPasspointManager.enableVerboseLogging(verbose);
        mWifiGeofenceManager.enableVerboseLogging(verbose); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
        mNetworkFactory.enableVerboseLogging(verbose);
        mLinkProbeManager.enableVerboseLogging(mVerboseLoggingEnabled);
        mWifiB2bConfigPolicy.enableVerboseLogging(verbose);   //EDM Wi-Fi Configuration

        mBigDataManager.setLogVisible(DBG_PRODUCT_DEV || mVerboseLoggingEnabled); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    }

    private static final String SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL = "log.tag.WifiHAL";
    private static final String LOGD_LEVEL_DEBUG = "D";
    private static final String LOGD_LEVEL_VERBOSE = "V";
    private void configureVerboseHalLogging(boolean enableVerbose) {
        if (mBuildProperties.isUserBuild()) {  // Verbose HAL logging not supported on user builds.
            return;
        }
        mPropertyService.set(SYSTEM_PROPERTY_LOG_CONTROL_WIFIHAL,
                enableVerbose ? LOGD_LEVEL_VERBOSE : LOGD_LEVEL_DEBUG);
    }

    private boolean setRandomMacOui() {
        String oui = mContext.getResources().getString(R.string.config_wifi_random_mac_oui);
        if (TextUtils.isEmpty(oui)) {
            oui = GOOGLE_OUI;
        }
        String[] ouiParts = oui.split("-");
        byte[] ouiBytes = new byte[3];
        ouiBytes[0] = (byte) (Integer.parseInt(ouiParts[0], 16) & 0xFF);
        ouiBytes[1] = (byte) (Integer.parseInt(ouiParts[1], 16) & 0xFF);
        ouiBytes[2] = (byte) (Integer.parseInt(ouiParts[2], 16) & 0xFF);

        logd("Setting OUI to " + oui);
        return mWifiNative.setScanningMacOui(mInterfaceName, ouiBytes);
    }

    /**
     * Initiates connection to a network specified by the user/app. This method checks if the
     * requesting app holds the NETWORK_SETTINGS permission.
     *
     * @param netId Id network to initiate connection.
     * @param uid UID of the app requesting the connection.
     * @param forceReconnect Whether to force a connection even if we're connected to the same
     *                       network currently.
     */
    private boolean connectToUserSelectNetwork(int netId, int uid, boolean forceReconnect) {
        logd("connectToUserSelectNetwork netId " + netId + ", uid " + uid
                + ", forceReconnect = " + forceReconnect);
        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(netId);
        if (config == null) {
            loge("connectToUserSelectNetwork Invalid network Id=" + netId);
            return false;
        }
        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            boolean result = WifiPolicyCache.getInstance(mContext).isNetworkAllowed(config, false);
            // MDF : Auditable Event: Attempt to connect to Access Point
            // Mandatory additional audit record content: Identity of access point being connected
            if (config != null) {
                logd("connectToUserSelectNetwork isNetworkAllowed=" + result); //temp before SRA
                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_NETWORK,
                        result, TAG, "Performing an attempt to connect with AP. SSID: " + config.SSID);
             }

             if (result) {
                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_NETWORK,
                        true, TAG, AuditEvents.WIFI_CONNECTING_NETWORK + netId + AuditEvents.SUCCEEDED);
            } else {
                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_NETWORK,
                        false, TAG, AuditEvents.WIFI_CONNECTING_NETWORK + netId + AuditEvents.FAILED);
                return false;
            }
        }
        if (!mWifiConfigManager.enableNetwork(netId, true, uid)
                || !mWifiConfigManager.updateLastConnectUid(netId, uid)) {
            logi("connectToUserSelectNetwork Allowing uid " + uid
                    + " with insufficient permissions to connect=" + netId);
        } else if (mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
            // Note user connect choice here, so that it will be considered in the next network
            // selection.
            mWifiConnectivityManager.setUserConnectChoice(netId);
        }
        if (!forceReconnect && mWifiInfo.getNetworkId() == netId) {
            // We're already connected to the user specified network, don't trigger a
            // reconnection unless it was forced.
            logi("connectToUserSelectNetwork already connecting/connected=" + netId);
        } else {
            mWifiConnectivityManager.prepareForForcedConnection(netId);
            if (uid == Process.SYSTEM_UID) {
                mWifiMetrics.setNominatorForNetwork(config.networkId,
                        WifiMetricsProto.ConnectionEvent.NOMINATOR_MANUAL);
            }
            startConnectToNetwork(netId, uid, SUPPLICANT_BSSID_ANY);
            if (!isWifiOnly()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CHANGEABLE_ENTRY_RSSI
                mWifiConfigManager.resetEntryRssi(netId);
            }
        }
        return true;
    }

    /**
     * ******************************************************
     * Methods exposed for public use
     * ******************************************************
     */

    /**
     * Retrieve a Messenger for the ClientModeImpl Handler
     *
     * @return Messenger
     */
    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    // Last connect attempt is used to prevent scan requests:
    //  - for a period of 10 seconds after attempting to connect
    private long mLastConnectAttemptTimestamp = 0;

    // For debugging, keep track of last message status handling
    // TODO, find an equivalent mechanism as part of parent class
    private static final int MESSAGE_HANDLING_STATUS_PROCESSED = 2;
    private static final int MESSAGE_HANDLING_STATUS_OK = 1;
    private static final int MESSAGE_HANDLING_STATUS_UNKNOWN = 0;
    private static final int MESSAGE_HANDLING_STATUS_REFUSED = -1;
    private static final int MESSAGE_HANDLING_STATUS_FAIL = -2;
    private static final int MESSAGE_HANDLING_STATUS_OBSOLETE = -3;
    private static final int MESSAGE_HANDLING_STATUS_DEFERRED = -4;
    private static final int MESSAGE_HANDLING_STATUS_DISCARD = -5;
    private static final int MESSAGE_HANDLING_STATUS_LOOPED = -6;
    private static final int MESSAGE_HANDLING_STATUS_HANDLING_ERROR = -7;

    private int mMessageHandlingStatus = 0;

    private int mOnTime = 0;
    private int mTxTime = 0;
    private int mRxTime = 0;

    private int mOnTimeScreenStateChange = 0;
    private long mLastOntimeReportTimeStamp = 0;
    private long mLastScreenStateChangeTimeStamp = 0;
    private int mOnTimeLastReport = 0;
    private int mTxTimeLastReport = 0;
    private int mRxTimeLastReport = 0;

    private WifiLinkLayerStats mLastLinkLayerStats;
    private long mLastLinkLayerStatsUpdate = 0;

    String reportOnTime() {
        long now = mClock.getWallClockMillis();
        StringBuilder sb = new StringBuilder();
        // Report stats since last report
        int on = mOnTime - mOnTimeLastReport;
        mOnTimeLastReport = mOnTime;
        int tx = mTxTime - mTxTimeLastReport;
        mTxTimeLastReport = mTxTime;
        int rx = mRxTime - mRxTimeLastReport;
        mRxTimeLastReport = mRxTime;
        int period = (int) (now - mLastOntimeReportTimeStamp);
        mLastOntimeReportTimeStamp = now;
        sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", on, tx, rx, period));
        // Report stats since Screen State Changed
        on = mOnTime - mOnTimeScreenStateChange;
        period = (int) (now - mLastScreenStateChangeTimeStamp);
        sb.append(String.format(" from screen [on:%d period:%d]", on, period));
        return sb.toString();
    }

    WifiLinkLayerStats getWifiLinkLayerStats() {
        if (mInterfaceName == null) {
            logw("getWifiLinkLayerStats called without an interface"); //SEC_PRODUCT_FEATURE_WLAN_ERROR_CODE_REFACTORING
            return null;
        }
        mLastLinkLayerStatsUpdate = mClock.getWallClockMillis();
        WifiLinkLayerStats stats = mWifiNative.getWifiLinkLayerStats(mInterfaceName);
        if (stats != null) {
            mOnTime = stats.on_time;
            mTxTime = stats.tx_time;
            mRxTime = stats.rx_time;
            mRunningBeaconCount = stats.beacon_rx;
            mWifiInfo.updatePacketRates(stats, mLastLinkLayerStatsUpdate);
        } else {
            long mTxPkts = mFacade.getTxPackets(mInterfaceName);
            long mRxPkts = mFacade.getRxPackets(mInterfaceName);
            mWifiInfo.updatePacketRates(mTxPkts, mRxPkts, mLastLinkLayerStatsUpdate);
        }
        return stats;
    }

    private byte[] getDstMacForKeepalive(KeepalivePacketData packetData)
            throws InvalidPacketException {
        try {
            InetAddress gateway = RouteInfo.selectBestRoute(
                    mLinkProperties.getRoutes(), packetData.dstAddress).getGateway();
            String dstMacStr = macAddressFromRoute(gateway.getHostAddress());
            return NativeUtil.macAddressToByteArray(dstMacStr);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new InvalidPacketException(SocketKeepalive.ERROR_INVALID_IP_ADDRESS);
        }
    }

    private static int getEtherProtoForKeepalive(KeepalivePacketData packetData)
            throws InvalidPacketException {
        if (packetData.dstAddress instanceof Inet4Address) {
            return OsConstants.ETH_P_IP;
        } else if (packetData.dstAddress instanceof Inet6Address) {
            return OsConstants.ETH_P_IPV6;
        } else {
            throw new InvalidPacketException(SocketKeepalive.ERROR_INVALID_IP_ADDRESS);
        }
    }

    private int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData,
            int intervalSeconds) {
        byte[] packet = null;
        byte[] dstMac = null;
        int proto = 0;

        try {
            packet = packetData.getPacket();
            dstMac = getDstMacForKeepalive(packetData);
            proto = getEtherProtoForKeepalive(packetData);
        } catch (InvalidPacketException e) {
            return e.error;
        }

        int ret = mWifiNative.startSendingOffloadedPacket(
                mInterfaceName, slot, dstMac, packet, proto, intervalSeconds * 1000);
        if (ret != 0) {
            loge("startWifiIPPacketOffload(" + slot + ", " + intervalSeconds
                    + "): hardware error " + ret);
            return SocketKeepalive.ERROR_HARDWARE_ERROR;
        } else {
            return SocketKeepalive.SUCCESS;
        }
    }

    private int stopWifiIPPacketOffload(int slot) {
        int ret = mWifiNative.stopSendingOffloadedPacket(mInterfaceName, slot);
        if (ret != 0) {
            loge("stopWifiIPPacketOffload(" + slot + "): hardware error " + ret);
            return SocketKeepalive.ERROR_HARDWARE_ERROR;
        } else {
            return SocketKeepalive.SUCCESS;
        }
    }

    private int startRssiMonitoringOffload(byte maxRssi, byte minRssi,
            WifiNative.WifiRssiEventHandler rssiHandler) {
        return mWifiNative.startRssiMonitoring(mInterfaceName, maxRssi, minRssi, rssiHandler);
    }

    private int stopRssiMonitoringOffload() {
        return mWifiNative.stopRssiMonitoring(mInterfaceName);
    }

    public boolean isSupportedGeofence() { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
        return mWifiGeofenceManager.isSupported();
    }

    public void setWifiGeofenceListener(WifiGeofenceManager.WifiGeofenceStateListener listener) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
        if (mWifiGeofenceManager.isSupported()) {
            mWifiGeofenceManager.setWifiGeofenceListener(listener);
        }
    }

    public int getCurrentGeofenceState() { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
        if (mWifiGeofenceManager.isSupported()) {
            return mWifiGeofenceManager.getCurrentGeofenceState();
        }
        return -1;
    }

    private String mLastRequestPackageNameForGeofence = null;
    private boolean isGeofenceUsedByAnotherPackage() {
        if (mLastRequestPackageNameForGeofence == null) return false;
        return true;
    }

    public void requestGeofenceState(boolean enabled, String packageName) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI
        if (mWifiGeofenceManager.isSupported()) {
            if (enabled) {
                mLastRequestPackageNameForGeofence = packageName;
                mWifiGeofenceManager.initGeofence();
            } else {
                mLastRequestPackageNameForGeofence = null;
                mWifiGeofenceManager.deinitGeofence();
            }
            mWifiGeofenceManager.setGeofenceStateByAnotherPackage(enabled);
        }
    }

    public List<String> getGeofenceEnterKeys() { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI
        if (mWifiGeofenceManager.isSupported()) {
            return mWifiGeofenceManager.getGeofenceEnterKeys();
        }
        return new ArrayList<String>();
    }

    public int getGeofenceCellCount(String configKey) {
        if (mWifiGeofenceManager.isSupported()) {
            return mWifiGeofenceManager.getGeofenceCellCount(configKey);
        }
        return 0;
    }

    /**
     * Temporary method that allows the active ClientModeManager to set the wifi state that is
     * retrieved by API calls. This will be removed when WifiServiceImpl no longer directly calls
     * this class (b/31479117).
     *
     * @param newState new state to set, invalid states are ignored.
     */
    public void setWifiStateForApiCalls(int newState) {
        switch (newState) {
            case WIFI_STATE_DISABLING:
                //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                if (getCurrentState() == mConnectedState && !isWifiOffByAirplane()) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                    if (ENBLE_WLAN_CONFIG_ANALYTICS) {
                        setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_USER_TRIGGER_DISCON_POWERONOFF_WIFIOFF);
                    }
                }
                //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
            case WIFI_STATE_DISABLED:
            case WIFI_STATE_ENABLING:
            case WIFI_STATE_ENABLED:
            case WIFI_STATE_UNKNOWN:
                if (mVerboseLoggingEnabled) {
                    Log.d(TAG, "setting wifi state to: " + newState);
                }
                mWifiState.set(newState);
                if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
                    if (newState == WIFI_STATE_ENABLING) {
                        WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                            true, ClientModeImpl.class.getSimpleName(), AuditEvents.WIFI_ENABLING);
                    }
                    else if (newState == WIFI_STATE_DISABLING) {
                        WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                            true, ClientModeImpl.class.getSimpleName(), AuditEvents.WIFI_DISABLING);
                    }
                }
                if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                    mWifiGeofenceManager.notifyWifiState(newState);
                }
                return;
            default:
                Log.d(TAG, "attempted to set an invalid state: " + newState);
                return;
        }
    }

    /**
     * Method used by WifiServiceImpl to get the current state of Wifi (in client mode) for API
     * calls.  This will be removed when WifiService no longer directly calls this class
     * (b/31479117).
     */
    public int syncGetWifiState() {
        return mWifiState.get();
    }

    /**
     * Converts the current wifi state to a printable form.
     */
    public String syncGetWifiStateByName() {
        switch (mWifiState.get()) {
            case WIFI_STATE_DISABLING:
                return "disabling";
            case WIFI_STATE_DISABLED:
                return "disabled";
            case WIFI_STATE_ENABLING:
                return "enabling";
            case WIFI_STATE_ENABLED:
                return "enabled";
            case WIFI_STATE_UNKNOWN:
                return "unknown state";
            default:
                return "[invalid state]";
        }
    }

    public boolean isConnected() {
        return getCurrentState() == mConnectedState;
    }

    public boolean isDisconnected() {
        return getCurrentState() == mDisconnectedState;
    }

    /**
     * Method checking if supplicant is in a transient state
     *
     * @return boolean true if in transient state
     */
    public boolean isSupplicantTransientState() {
        SupplicantState supplicantState = mWifiInfo.getSupplicantState();
        if (supplicantState == SupplicantState.ASSOCIATING
                || supplicantState == SupplicantState.AUTHENTICATING
                || supplicantState == SupplicantState.FOUR_WAY_HANDSHAKE
                || supplicantState == SupplicantState.GROUP_HANDSHAKE) {

            if (mVerboseLoggingEnabled) {
                Log.d(TAG, "Supplicant is under transient state: " + supplicantState);
            }
            return true;
        } else {
            if (mVerboseLoggingEnabled) {
                Log.d(TAG, "Supplicant is under steady state: " + supplicantState);
            }
        }

        return false;
    }

    /**
     * Get status information for the current connection, if any.
     *
     * @return a {@link WifiInfo} object containing information about the current connection
     */
    public WifiInfo syncRequestConnectionInfo() {
        WifiInfo result = new WifiInfo(mWifiInfo);
        return result;
    }

    /**
     * Method to retrieve the current WifiInfo
     *
     * @returns WifiInfo
     */
    public WifiInfo getWifiInfo() {
        return mWifiInfo;
    }

    //SEC_PRODUCT_FEATURE_WLAN_SEC_CONFIGURATION_EXTENSION
    public NetworkInfo syncGetNetworkInfo() {
        return new NetworkInfo(mNetworkInfo);
    }

    /**
     * Blocking call to get the current DHCP results
     *
     * @return DhcpResults current results
     */
    public DhcpResults syncGetDhcpResults() {
        synchronized (mDhcpResultsLock) {
            return new DhcpResults(mDhcpResults);
        }
    }

    /**
     * When the underlying interface is destroyed, we must immediately tell connectivity service to
     * mark network agent as disconnected and stop the ip client.
     */
    public void handleIfaceDestroyed() {
        mHandleIfaceIsUp = false; //SEC_PRODUCT_FEATURE_WLAN_ERROR_CODE_REFACTORING
        handleNetworkDisconnect();
    }

    //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
    public void setIsWifiOffByAirplane(boolean enabled) {
        mIsWifiOffByAirplane = enabled;
    }

    private boolean isWifiOffByAirplane() {
        return mIsWifiOffByAirplane;
    }
    //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

    /**
     * TODO: doc
     */
    public void setOperationalMode(int mode, String ifaceName) {
        if (mVerboseLoggingEnabled) {
            log("setting operational mode to " + String.valueOf(mode) + " for iface: " + ifaceName);
        }
        mModeChange = true;
        if (mode != CONNECT_MODE) {
            if (getCurrentState() == mConnectedState) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                       DISCONNECT_REASON_TURN_OFF_WIFI);
                //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                if (ENBLE_WLAN_CONFIG_ANALYTICS) {
                    if (isWifiOffByAirplane()) {
                        setIsWifiOffByAirplane(false);
                        setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_USER_TRIGGER_DISCON_AIRPLANE);
                    }
                }
                //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                mDelayDisconnect.checkAndWait(mNetworkInfo);
            }
            // we are disabling client mode...   need to exit connect mode now
            transitionTo(mDefaultState);
        } else {
            // do a quick sanity check on the iface name, make sure it isn't null
            if (ifaceName != null) {
                mInterfaceName = ifaceName;
                transitionTo(mDisconnectedState);
                mHandleIfaceIsUp = true; //SEC_PRODUCT_FEATURE_WLAN_ERROR_CODE_REFACTORING
            } else {
                Log.e(TAG, "supposed to enter connect mode, but iface is null -> DefaultState");
                transitionTo(mDefaultState);
            }
        }
        // use the CMD_SET_OPERATIONAL_MODE to force the transitions before other messages are
        // handled.
        sendMessageAtFrontOfQueue(CMD_SET_OPERATIONAL_MODE);
    }

    /**
     * Initiates a system-level bugreport, in a non-blocking fashion.
     */
    public void takeBugReport(String bugTitle, String bugDetail) {
        mWifiDiagnostics.takeBugReport(bugTitle, bugDetail);
    }

    /**
     * Allow tests to confirm the operational mode for ClientModeImpl for testing.
     */
    @VisibleForTesting
    protected int getOperationalModeForTest() {
        return mOperationalMode;
    }

    void setConcurrentEnabled(boolean enable) { //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
        mConcurrentEnabled = enable;
        mScanRequestProxy.setScanningEnabled(!mConcurrentEnabled, "SEC_COMMAND_ID_SET_WIFI_XXX_WITH_P2P");
        enableWifiConnectivityManager(!mConcurrentEnabled);
        mPasspointManager.setRequestANQPEnabled(!mConcurrentEnabled);
    }

    boolean getConcurrentEnabled() { //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
        return mConcurrentEnabled;
    }

    public void syncSetFccChannel(boolean enable) { //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL
        if (DBG) Log.d(TAG, "syncSetFccChannel: enable = " + enable);
        if (mIsRunning) {
            if (mBlockFccChannelCmd) {
                Log.d(TAG, "Block setFccChannelNative CMD by WlanMacAddress");
                return;
            }
            mWifiNative.setFccChannel(mInterfaceName, enable);
        }
    }

    void setFccChannel() { //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL
        if (DBG) Log.d(TAG, "setFccChannel() is called" );
        boolean isAirplaneModeEnabled = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        if (isWifiOnly()) {
            syncSetFccChannel(true);
        } else {
            if(isAirplaneModeEnabled) {
                syncSetFccChannel(true);
            } else {
                syncSetFccChannel(false);
            }
        }
    }

    /**
     * Retrieve the WifiMulticastLockManager.FilterController callback for registration.
     */
    protected WifiMulticastLockManager.FilterController getMcastLockManagerFilterController() {
        return mMcastLockManagerFilterController;
    }

    /**
     * Blocking method to retrieve the passpoint icon.
     *
     * @param channel AsyncChannel for the response
     * @param bssid representation of the bssid as a long
     * @param fileName name of the file
     *
     * @return boolean returning the result of the call
     */
    public boolean syncQueryPasspointIcon(AsyncChannel channel, long bssid, String fileName) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_OSU_ICON_QUERY_BSSID, bssid);
        bundle.putString(EXTRA_OSU_ICON_QUERY_FILENAME, fileName);
        Message resultMsg = channel.sendMessageSynchronously(CMD_QUERY_OSU_ICON, bundle);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result == 1;
    }

    /**
     * Blocking method to match the provider with the current network
     *
     * @param channel AsyncChannel to use for the response
     * @param fqdn
     * @return int returns message result
     */
    public int matchProviderWithCurrentNetwork(AsyncChannel channel, String fqdn) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return -1;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_MATCH_PROVIDER_NETWORK, fqdn);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    /**
     * Deauthenticate and set the re-authentication hold off time for the current network
     * @param holdoff hold off time in milliseconds
     * @param ess set if the hold off pertains to an ESS rather than a BSS
     */
    public void deauthenticateNetwork(AsyncChannel channel, long holdoff, boolean ess) {
        // TODO: This needs an implementation
    }

    /**
     * Method to disable an ephemeral config for an ssid
     *
     * @param ssid network name to disable
     */
    public void disableEphemeralNetwork(String ssid) {
        if (ssid != null) {
            sendMessage(CMD_DISABLE_EPHEMERAL_NETWORK, ssid);
        }
    }

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
    private static final int DISCONNECT_REASON_UNKNOWN = 0;
    private static final int DISCONNECT_REASON_DHCP_FAIL = 1;
    public static final int DISCONNECT_REASON_API = 2;
    private static final int DISCONNECT_REASON_START_CONNECT = 3;
    private static final int DISCONNECT_REASON_SIM_REMOVED = 4;
    private static final int DISCONNECT_REASON_ROAM_TIMEOUT = 5;
    private static final int DISCONNECT_REASON_ROAM_FAIL = 6;
    //private static final int DISCONNECT_REASON_ROAM_TEMP_DISABLED = 7;
    private static final int DISCONNECT_REASON_UNWANTED = 8;
    private static final int DISCONNECT_REASON_UNWANTED_BY_USER = 9;
    private static final int DISCONNECT_REASON_NO_INTERNET = 10;
    //private static final int DISCONNECT_REASON_WPS_RUNNING = 11;
    private static final int DISCONNECT_REASON_ASSOC_REJECTED = 12;
    private static final int DISCONNECT_REASON_AUTH_FAIL = 13;
    //private static final int DISCONNECT_REASON_AUTO_CONNECT = 14;
    private static final int DISCONNECT_REASON_DISCONNECT_BY_MDM = 15;
    private static final int DISCONNECT_REASON_DISCONNECT_BY_P2P = 16;
    private static final int DISCONNECT_REASON_TURN_OFF_WIFI = 17;
    private static final int DISCONNECT_REASON_REMOVE_NETWORK = 18;
    private static final int DISCONNECT_REASON_DISABLE_NETWORK = 19;
    private static final int DISCONNECT_REASON_USER_SWITCH = 20;
    private static final int DISCONNECT_REASON_ADD_OR_UPDATE_NETWORK = 21;
    private static final int DISCONNECT_REASON_NO_NETWORK = 22;
    public static final int DISCONNECT_REASON_FACTORY_RESET = 23;
    private static final int DISCONNECT_REASON_DHCP_FAIL_WITH_IPCLIENT_ISSUE = 24;

    /**
     * Disconnect from Access Point
     */
    public void disconnectCommand() {
        sendMessage(CMD_DISCONNECT);
    }

    /**
     * Method to trigger a disconnect.
     *
     * @param uid UID of requesting caller
     * @param reason disconnect reason
     */
    public void disconnectCommand(int uid, int reason) {
        sendMessage(CMD_DISCONNECT, uid, reason);
    }

    /**
     * Notify internal disconnect reason.
     * DO NOT call this method directly, only allow to call on the state machine
     */
    private void notifyDisconnectInternalReason(int reason) {
        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
        mBigDataManager.addOrUpdateValue(
                WifiBigDataLogManager.LOGGING_TYPE_LOCAL_DISCONNECT_REASON,
                reason);
    }

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
    public void report(int reportId, Bundle report) {
        if (mIssueDetector != null && report != null) {
            mIssueDetector.captureBugReport(reportId, report);
        }
    }

    public void disableP2p(P2pDisableListener mP2pDisableCallback) {
        if (mP2pSupported) {
            mP2pDisableListener = mP2pDisableCallback;
            if ( mWifiP2pChannel != null) {
                Message message = new Message();
                message.what = WifiP2pServiceImpl.DISABLE_P2P;
                mWifiP2pChannel.sendMessage(message);
            }
        }
    }

    /**
     * Initiate a reconnection to AP
     */
    public void reconnectCommand(WorkSource workSource) {
        sendMessage(CMD_RECONNECT, workSource);
    }

    /**
     * Initiate a re-association to AP
     */
    public void reassociateCommand() {
        sendMessage(CMD_REASSOCIATE);
    }

    /**
     * Checks for a null Message.
     *
     * This can happen with sendMessageSynchronously, for example if an
     * InterruptedException occurs. If this just happens once, silently
     * ignore it, because it is probably a side effect of shutting down.
     * If it happens a second time, generate a WTF.
     */
    private boolean messageIsNull(Message resultMsg) {
        if (resultMsg != null) return false;
        if (mNullMessageCounter.getAndIncrement() > 0) {
            Log.wtf(TAG, "Persistent null Message", new RuntimeException());
        }
        return true;
    }
    private AtomicInteger mNullMessageCounter = new AtomicInteger(0);

    /**
     * Add a network synchronously
     *
     * @return network id of the new network
     */
    public int syncAddOrUpdateNetwork(AsyncChannel channel, WifiConfiguration config) { //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
        return syncAddOrUpdateNetwork(channel, WifiManager.CALLED_FROM_DEFAULT, config);
    }

    public int syncAddOrUpdateNetwork(AsyncChannel channel, int from, WifiConfiguration config) { //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore add or update network because shutdown is held");
            return -1;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_NETWORK, from, 0, config);
        if (messageIsNull(resultMsg)) return WifiConfiguration.INVALID_NETWORK_ID;
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    /**
     * Get configured networks synchronously
     *
     * @param channel
     * @return
     */
    //+SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM - semGetConfiguredNetworks
    public List<WifiConfiguration> syncGetConfiguredNetworks(int uuid, AsyncChannel channel,
            int targetUid) {
        return syncGetConfiguredNetworks(uuid, channel, WifiManager.CALLED_FROM_DEFAULT, targetUid);
    }
    //-SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM - semGetConfiguredNetworks

    public List<WifiConfiguration> syncGetConfiguredNetworks(int uuid, AsyncChannel channel,
            int from, int targetUid) { //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM - semGetConfiguredNetworks
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CONFIGURED_NETWORKS, uuid,
                targetUid, new Integer(from)); //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM - semGetConfiguredNetworks
        if (messageIsNull(resultMsg)) return null;
        if (!(resultMsg.obj instanceof List)) {
            Log.e(TAG, "Wrong type object is delivered. "
                    + "request what:" + CMD_GET_CONFIGURED_NETWORKS
                    + ", reply what:" + resultMsg.what  // for tracking reply message from
                    + " arg1:" + resultMsg.arg1
                    + " arg2:" + resultMsg.arg2
                    + " obj:" + resultMsg.obj);
            return null;
        }
        List<WifiConfiguration> result = (List<WifiConfiguration>) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    /**
     * Get a configured network synchronously
     *
     * @param channel
     * @param networkId
     * @return
     */
    public WifiConfiguration syncGetSpecificNetwork(AsyncChannel channel, int networkId) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_A_CONFIGURED_NETWORK, networkId);
        if (messageIsNull(resultMsg)) return null;

        if (resultMsg.obj != null && resultMsg.obj instanceof WifiConfiguration) {        
            WifiConfiguration result = (WifiConfiguration) resultMsg.obj;
            resultMsg.recycle();
            return result;
        }
        Log.d(TAG, "resultMsg.obj is null or not instance of WifiConfiguration");
        return null;
    }

    /**
     * Blocking call to get the current WifiConfiguration by a privileged caller so private data,
     * like the password, is not redacted.
     *
     * @param channel AsyncChannel to use for the response
     * @return List list of configured networks configs
     */
    public List<WifiConfiguration> syncGetPrivilegedConfiguredNetwork(AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(
                CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS);
        if (messageIsNull(resultMsg)) return null;
        List<WifiConfiguration> result = (List<WifiConfiguration>) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    /**
     * Returns the list of FQDN (Fully Qualified Domain Name) to installed Passpoint configurations.
     *
     * Return the map of all matching configurations with corresponding scanResults (or an empty map
     * if none).
     *
     * @param scanResults The list of scan results
     * @return Map that consists of FQDN (Fully Qualified Domain Name) and corresponding
     * scanResults per network type({@link WifiManager#PASSPOINT_HOME_NETWORK} and {@link
     * WifiManager#PASSPOINT_ROAMING_NETWORK}).
     */
    @NonNull
    Map<String, Map<Integer, List<ScanResult>>> syncGetAllMatchingFqdnsForScanResults(
            List<ScanResult> scanResults,
            AsyncChannel channel) {
        Message resultMsg = channel.sendMessageSynchronously(
                CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS,
                scanResults);
        if (messageIsNull(resultMsg)) return new HashMap<>();
        Map<String, Map<Integer, List<ScanResult>>> configs =
                (Map<String, Map<Integer, List<ScanResult>>>) resultMsg.obj;
        resultMsg.recycle();
        return configs;
    }

    /**
     * Retrieve a list of {@link OsuProvider} associated with the given list of ScanResult
     * synchronously.
     *
     * @param scanResults a list of ScanResult that has Passpoint APs.
     * @param channel     Channel for communicating with the state machine
     * @return Map that consists of {@link OsuProvider} and a matching list of {@link ScanResult}.
     */
    @NonNull
    public Map<OsuProvider, List<ScanResult>> syncGetMatchingOsuProviders(
            List<ScanResult> scanResults,
            AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return new HashMap<>();
        }

        Message resultMsg =
                channel.sendMessageSynchronously(CMD_GET_MATCHING_OSU_PROVIDERS, scanResults);
        if (messageIsNull(resultMsg)) return new HashMap<>();
        Map<OsuProvider, List<ScanResult>> providers =
                (Map<OsuProvider, List<ScanResult>>) resultMsg.obj;
        resultMsg.recycle();
        return providers;
    }

    /**
     * Returns the matching Passpoint configurations for given OSU(Online Sign-Up) Providers
     *
     * @param osuProviders a list of {@link OsuProvider}
     * @param channel  AsyncChannel to use for the response
     * @return Map that consists of {@link OsuProvider} and matching {@link PasspointConfiguration}.
     */
    @NonNull
    public Map<OsuProvider, PasspointConfiguration> syncGetMatchingPasspointConfigsForOsuProviders(
            List<OsuProvider> osuProviders, AsyncChannel channel) {
        Message resultMsg =
                channel.sendMessageSynchronously(
                        CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS, osuProviders);
        if (messageIsNull(resultMsg)) return new HashMap<>();
        Map<OsuProvider, PasspointConfiguration> result =
                (Map<OsuProvider, PasspointConfiguration>) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    /**
     * Returns the corresponding wifi configurations for given FQDN (Fully Qualified Domain Name)
     * list.
     *
     * An empty list will be returned when no match is found.
     *
     * @param fqdnList a list of FQDN
     * @param channel  AsyncChannel to use for the response
     * @return List of {@link WifiConfiguration} converted from
     * {@link com.android.server.wifi.hotspot2.PasspointProvider}
     */
    @NonNull
    public List<WifiConfiguration> syncGetWifiConfigsForPasspointProfiles(List<String> fqdnList,
            AsyncChannel channel) {
        Message resultMsg =
                channel.sendMessageSynchronously(
                        CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES, fqdnList);
        if (messageIsNull(resultMsg)) return new ArrayList<>();
        List<WifiConfiguration> result = (List<WifiConfiguration>) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    /**
     * Add or update a Passpoint configuration synchronously.
     *
     * @param channel Channel for communicating with the state machine
     * @param config The configuration to add or update
     * @param packageName Package name of the app adding/updating {@code config}.
     * @return true on success
     */
    public boolean syncAddOrUpdatePasspointConfig(AsyncChannel channel,
            PasspointConfiguration config, int uid, String packageName) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_UID, uid);
        bundle.putString(EXTRA_PACKAGE_NAME, packageName);
        bundle.putParcelable(EXTRA_PASSPOINT_CONFIGURATION, config);
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG,
                bundle);
        if (messageIsNull(resultMsg)) return false;
        boolean result = (resultMsg.arg1 == SUCCESS);
        resultMsg.recycle();
        return result;
    }

    /**
     * Remove a Passpoint configuration synchronously.
     *
     * @param channel Channel for communicating with the state machine
     * @param fqdn The FQDN of the Passpoint configuration to remove
     * @return true on success
     */
    public boolean syncRemovePasspointConfig(AsyncChannel channel, String fqdn) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_PASSPOINT_CONFIG,
                fqdn);
        if (messageIsNull(resultMsg)) return false;
        boolean result = (resultMsg.arg1 == SUCCESS);
        resultMsg.recycle();
        return result;
    }

    /**
     * Get the list of installed Passpoint configurations synchronously.
     *
     * @param channel Channel for communicating with the state machine
     * @return List of {@link PasspointConfiguration}
     */
    public List<PasspointConfiguration> syncGetPasspointConfigs(AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return new ArrayList<>();
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PASSPOINT_CONFIGS);
        if (messageIsNull(resultMsg)) return new ArrayList<>();
        List<PasspointConfiguration> result = (List<PasspointConfiguration>) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    /**
     * Start subscription provisioning synchronously
     *
     * @param provider {@link OsuProvider} the provider to provision with
     * @param callback {@link IProvisioningCallback} callback for provisioning status
     * @return boolean true indicates provisioning was started, false otherwise
     */
    public boolean syncStartSubscriptionProvisioning(int callingUid, OsuProvider provider,
            IProvisioningCallback callback, AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Message msg = Message.obtain();
        msg.what = CMD_START_SUBSCRIPTION_PROVISIONING;
        msg.arg1 = callingUid;
        msg.obj = callback;
        msg.getData().putParcelable(EXTRA_OSU_PROVIDER, provider);
        Message resultMsg = channel.sendMessageSynchronously(msg);
        if (messageIsNull(resultMsg)) return false;
        boolean result = resultMsg.arg1 != 0;
        resultMsg.recycle();
        return result;
    }

    /**
     * Get the supported feature set synchronously
     */
    public long syncGetSupportedFeatures(AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return 0;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_SUPPORTED_FEATURES);
        if (messageIsNull(resultMsg)) return 0;
        long supportedFeatureSet = ((Long) resultMsg.obj).longValue();
        resultMsg.recycle();

        // Mask the feature set against system properties.
        boolean rttSupported = mContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_WIFI_RTT);
        if (!rttSupported) {
            supportedFeatureSet &=
                    ~(WifiManager.WIFI_FEATURE_D2D_RTT | WifiManager.WIFI_FEATURE_D2AP_RTT);
        }

        return supportedFeatureSet;
    }

    /**
     * Get link layers stats for adapter synchronously
     */
    public WifiLinkLayerStats syncGetLinkLayerStats(AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_LINK_LAYER_STATS);
        if (messageIsNull(resultMsg)) return null;
        WifiLinkLayerStats result = (WifiLinkLayerStats) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    /**
     * Delete a network
     *
     * @param networkId id of the network to be removed
     */
    public boolean syncRemoveNetwork(AsyncChannel channel, int networkId) { //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
        return syncRemoveNetwork(channel, WifiManager.CALLED_FROM_DEFAULT, networkId);
    }

    public boolean syncRemoveNetwork(AsyncChannel channel, int from, int networkId) { //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_NETWORK, networkId, from);
        if (messageIsNull(resultMsg)) return false;
        boolean result = (resultMsg.arg1 != FAILURE);
        resultMsg.recycle();
        return result;
    }

    /**
     * Enable a network
     *
     * @param netId         network id of the network
     * @param disableOthers true, if all other networks have to be disabled
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     */
    public boolean syncEnableNetwork(AsyncChannel channel, int netId, boolean disableOthers) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_ENABLE_NETWORK, netId,
                disableOthers ? 1 : 0);
        if (messageIsNull(resultMsg)) return false;
        boolean result = (resultMsg.arg1 != FAILURE);
        resultMsg.recycle();
        return result;
    }

    /**
     * Forcingly enable all networks
     */
    public void forcinglyEnableAllNetworks(AsyncChannel channel) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "can't forcingly enable all networks because shutdown is held");
            return;
        }
        channel.sendMessage(CMD_FORCINGLY_ENABLE_ALL_NETWORKS);
    }

    /**
     * Disable a network
     *
     * @param netId network id of the network
     * @return {@code true} if the operation succeeds, {@code false} otherwise
     */
    public boolean syncDisableNetwork(AsyncChannel channel, int netId) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(WifiManager.DISABLE_NETWORK, netId);
        boolean result = (resultMsg.what != WifiManager.DISABLE_NETWORK_FAILED);
        if (messageIsNull(resultMsg)) return false;
        resultMsg.recycle();
        return result;
    }

    /**
     * Method to enable/disable RSSI polling
     * @param enabled boolean idicating if polling should start
     */
    public void enableRssiPolling(boolean enabled) {
        sendMessage(CMD_ENABLE_RSSI_POLL, enabled ? 1 : 0, 0);
    }

    /**
     * Set high performance mode of operation.
     * Enabling would set active power mode and disable suspend optimizations;
     * disabling would set auto power mode and enable suspend optimizations
     *
     * @param enable true if enable, false otherwise
     */
    public void setHighPerfModeEnabled(boolean enable) {
        sendMessage(CMD_SET_HIGH_PERF_MODE, enable ? 1 : 0, 0);
    }


    /**
     * reset cached SIM credential data
     */
    public synchronized void resetSimAuthNetworks(boolean simPresent) {
        sendMessage(CMD_RESET_SIM_NETWORKS, simPresent ? 1 : 0);
    }

    /**
     * Get Network object of current wifi network
     * @return Network object of current wifi network
     */
    public Network getCurrentNetwork() {
        synchronized (mNetworkAgentLock) {
            if (mNetworkAgent != null) {
                return new Network(mNetworkAgent.netId);
            } else {
                return null;
            }
        }
    }

    /**
     * Enable TDLS for a specific MAC address
     */
    public void enableTdls(String remoteMacAddress, boolean enable) {
        int enabler = enable ? 1 : 0;
        sendMessage(CMD_ENABLE_TDLS, enabler, 0, remoteMacAddress);
    }

    /**
     * Send a message indicating bluetooth adapter connection state changed
     */
    public void sendBluetoothAdapterStateChange(int state) {
        sendMessage(CMD_BLUETOOTH_ADAPTER_STATE_CHANGE, state, 0);
    }

    /**
     * Send a message indicating a package has been uninstalled.
     */
    public void removeAppConfigs(String packageName, int uid) {
        // Build partial AppInfo manually - package may not exist in database any more
        ApplicationInfo ai = new ApplicationInfo();
        ai.packageName = packageName;
        ai.uid = uid;
        sendMessage(CMD_REMOVE_APP_CONFIGURATIONS, ai);
    }

    /**
     * Send a message indicating a user has been removed.
     */
    public void removeUserConfigs(int userId) {
        sendMessage(CMD_REMOVE_USER_CONFIGURATIONS, userId);
    }

    /**
     * Update the BatteryStats WorkSource.
     */
    public void updateBatteryWorkSource(WorkSource newSource) {
        synchronized (mRunningWifiUids) {
            try {
                if (newSource != null) {
                    mRunningWifiUids.set(newSource);
                }
                if (mIsRunning) {
                    if (mReportedRunning) {
                        // If the work source has changed since last time, need
                        // to remove old work from battery stats.
                        if (!mLastRunningWifiUids.equals(mRunningWifiUids)) {
                            mBatteryStats.noteWifiRunningChanged(mLastRunningWifiUids,
                                    mRunningWifiUids);
                            mLastRunningWifiUids.set(mRunningWifiUids);
                        }
                    } else {
                        // Now being started, report it.
                        mBatteryStats.noteWifiRunning(mRunningWifiUids);
                        mLastRunningWifiUids.set(mRunningWifiUids);
                        mReportedRunning = true;
                    }
                } else {
                    if (mReportedRunning) {
                        // Last reported we were running, time to stop.
                        mBatteryStats.noteWifiStopped(mLastRunningWifiUids);
                        mLastRunningWifiUids.clear();
                        mReportedRunning = false;
                    }
                }
                mWakeLock.setWorkSource(newSource);
            } catch (RemoteException ignore) {
            }
        }
    }

    /**
     * Trigger dump on the class IpClient object.
     */
    public void dumpIpClient(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mIpClient != null) {
            // All dumpIpClient does is print this log message.
            // TODO: consider deleting this, since it's not useful.
            pw.println("IpClient logs have moved to dumpsys network_stack");
        }
    }

    @Override
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dump(fd, pw, args);
        mSupplicantStateTracker.dump(fd, pw, args);
        pw.println("mLinkProperties " + mLinkProperties);
        pw.println("mWifiInfo " + mWifiInfo);
        pw.println("mDhcpResults " + mDhcpResults);
        pw.println("mNetworkInfo " + mNetworkInfo);
        pw.println("mLastSignalLevel " + mLastSignalLevel);
        pw.println("mLastBssid " + mLastBssid);
        pw.println("mLastNetworkId " + mLastNetworkId);
        pw.println("mOperationalMode " + mOperationalMode);
        pw.println("mUserWantsSuspendOpt " + mUserWantsSuspendOpt);
        pw.println("mSuspendOptNeedsDisabled " + mSuspendOptNeedsDisabled);
        pw.println("mIsWifiOnly : " + isWifiOnly()); //SEC_PRODUCT_FEATURE_WLAN_SEC_WIFIONLY_CHECK
        pw.println("FactoryMAC: " + mWifiNative.getVendorConnFileInfo(0 /*.mac.info*/)); //SEC_PRODUCT_FEATURE_WLAN_PRINT_FACTORYMAC
        mCountryCode.dump(fd, pw, args);
        mNetworkFactory.dump(fd, pw, args);
        mUntrustedNetworkFactory.dump(fd, pw, args);
        pw.println("Wlan Wake Reasons:" + mWifiNative.getWlanWakeReasonCount());
        pw.println();

        if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
            mWifiGeofenceManager.dump(fd, pw, args);
        }

        mWifiConfigManager.dump(fd, pw, args);
        pw.println();
        mWifiInjector.getCarrierNetworkConfig().dump(fd, pw, args);
        pw.println();
        mPasspointManager.dump(pw);
        pw.println();
        mWifiDiagnostics.captureBugReportData(WifiDiagnostics.REPORT_REASON_USER_ACTION);
        mWifiDiagnostics.dump(fd, pw, args);
        dumpIpClient(fd, pw, args);
        mWifiConnectivityManager.dump(fd, pw, args);
        pw.println("mConcurrentEnabled " + mConcurrentEnabled); //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
        pw.println("mIsImsCallEstablished " + mIsImsCallEstablished); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
        if (mUnstableApController != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
            mUnstableApController.dump(fd, pw, args);
        }
        pw.println("W24H (wifi scan auto fav sns agr ...):" + getWifiParameters(false)); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        if (mIssueDetector != null) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
            mIssueDetector.dump(fd, pw, args);
        }
        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI - disable google autowifi
        //mWifiInjector.getWakeupController().dump(fd, pw, args);
        mLinkProbeManager.dump(fd, pw, args);
        mWifiInjector.getWifiLastResortWatchdog().dump(fd, pw, args);
        mSemSarManager.dump(fd, pw, args); //SEMSAR

        pw.println(TAG + " connectivity packet log:");
        pw.println(TAG + " Name of interface : " + mApInterfaceName);
        pw.println();
        if(mConnectivityPacketLogForHotspot != null) {
            mConnectivityPacketLogForHotspot.readOnlyLocalLog().dump(fd, pw, args);
            pw.println(TAG + " connectivity packet log:");
            pw.println(TAG + " Name of interface : wlan0");
            if(WifiInjector.getInstance().getSemWifiApChipInfo().supportWifiSharing() && mConnectivityPacketLogForWlan0 !=null) {
                mConnectivityPacketLogForWlan0.readOnlyLocalLog().dump(fd, pw, args);
            }
        }

        runFwDump(); //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
    }

    private SemConnectivityPacketTracker createPacketTracker(InterfaceParams mInterfaceParams, LocalLog mLog) {
        try {
            return new SemConnectivityPacketTracker(getHandler(), mInterfaceParams, mLog);
        } catch (IllegalArgumentException e) {
            Log.e(TAG,"Failed to get ConnectivityPacketTracker object: " + e);
            return null;
        }
    }

    /**
     * Trigger message to handle boot completed event.
     */
    public void handleBootCompleted() {
        sendMessage(CMD_BOOT_COMPLETED);
    }

    /**
     * Trigger message to handle user switch event.
     */
    public void handleUserSwitch(int userId) {
        sendMessage(CMD_USER_SWITCH, userId);
    }

    /**
     * Trigger message to handle user unlock event.
     */
    public void handleUserUnlock(int userId) {
        sendMessage(CMD_USER_UNLOCK, userId);
    }

    /**
     * Trigger message to handle user stop event.
     */
    public void handleUserStop(int userId) {
        sendMessage(CMD_USER_STOP, userId);
    }

    /**
     * ******************************************************
     * Internal private functions
     * ******************************************************
     */

    //+SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
    private void runFwDump(){
        mWifiNative.saveFwDump(mInterfaceName);
        runFwLogTimer();
    }

    private void runFwLogTimer() {
        if (DBG_PRODUCT_DEV)
            return;

        if (mFwLogTimer != null) {
            Log.i(TAG, "mFwLogTimer timer cancled");
            mFwLogTimer.cancel();
        }
        mFwLogTimer = new Timer();
        mFwLogTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "mFwLogTimer timer expired - start folder initialization");
                resetFwLogFolder();
                mFwLogTimer = null;
            }
        }, 600000);
    }

    private void resetFwLogFolder() {
        if (DBG_PRODUCT_DEV)
            return;

        Log.i(TAG, "resetFwLogFolder");
        try {
            File folder = new File("/data/log/wifi/");
            if (folder.exists()) {
                removeFolderFiles(folder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* clear /data/vendor/log/wifi via HAL */
        if (!mWifiNative.removeVendorLogFiles()){
            Log.e(TAG, "Removing vendor logs got failed.");
        }
    }

    private void removeFolderFiles(File folder) {
        try {
            File[] logFiles = folder.listFiles();
            if (logFiles != null && logFiles.length > 0) {
                for (File logFile : logFiles) {
                    Log.i(TAG, "WifiStateMachine : " + logFile + " deleted");
                    logFile.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //-SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE

    private void logStateAndMessage(Message message, State state) {
        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
        ReportUtil.updateWifiStateMachineProcessMessage(
                state.getClass().getSimpleName(), message.what);

        mMessageHandlingStatus = 0;
        if (mVerboseLoggingEnabled) {
            logd(" " + state.getClass().getSimpleName() + " " + getLogRecString(message));
        }
    }

    @Override
    protected boolean recordLogRec(Message msg) {
        switch (msg.what) {
            case CMD_RSSI_POLL:
                return mVerboseLoggingEnabled;
            default:
                return true;
        }
    }

    /**
     * Return the additional string to be logged by LogRec, default
     *
     * @param msg that was processed
     * @return information to be logged as a String
     */
    @Override
    protected String getLogRecString(Message msg) {
        WifiConfiguration config;
        Long now;
        String report;
        String key;
        StringBuilder sb = new StringBuilder();
        sb.append("screen=").append(mScreenOn ? "on" : "off");
        if (mMessageHandlingStatus != MESSAGE_HANDLING_STATUS_UNKNOWN) {
            sb.append("(").append(mMessageHandlingStatus).append(")");
        }
        if (msg.sendingUid > 0 && msg.sendingUid != Process.WIFI_UID) {
            sb.append(" uid=" + msg.sendingUid);
        }
        switch (msg.what) {
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                StateChangeResult stateChangeResult = (StateChangeResult) msg.obj;
                if (stateChangeResult != null) {
                    sb.append(stateChangeResult.toString());
                }
                break;
            case WifiManager.SAVE_NETWORK:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = (WifiConfiguration) msg.obj;
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    sb.append(" nid=").append(config.networkId);
                    if (config.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (config.preSharedKey != null
                            && !config.preSharedKey.equals("*")) {
                        sb.append(" hasPSK");
                    }
                    if (config.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(config.creatorUid);
                    sb.append(" suid=").append(config.lastUpdateUid);
                }
                break;
            case WifiManager.FORGET_NETWORK:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = (WifiConfiguration) msg.obj;
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                    sb.append(" nid=").append(config.networkId);
                    if (config.hiddenSSID) {
                        sb.append(" hidden");
                    }
                    if (config.preSharedKey != null) {
                        sb.append(" hasPSK");
                    }
                    if (config.ephemeral) {
                        sb.append(" ephemeral");
                    }
                    if (config.selfAdded) {
                        sb.append(" selfAdded");
                    }
                    sb.append(" cuid=").append(config.creatorUid);
                    sb.append(" suid=").append(config.lastUpdateUid);
                    WifiConfiguration.NetworkSelectionStatus netWorkSelectionStatus =
                            config.getNetworkSelectionStatus();
                    sb.append(" ajst=").append(
                            netWorkSelectionStatus.getNetworkStatusString());
                }
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                sb.append(" ");
                sb.append(" timedOut=" + Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                String bssid = (String) msg.obj;
                if (bssid != null && bssid.length() > 0) {
                    sb.append(" ");
                    sb.append(bssid);
                }
                sb.append(" blacklist=" + Boolean.toString(mDidBlackListBSSID));
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" ").append(mLastBssid);
                sb.append(" nid=").append(mLastNetworkId);
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                }
                key = mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=").append(key);
                }
                break;
            case CMD_TARGET_BSSID:
            case CMD_ASSOCIATED_BSSID:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    sb.append(" BSSID=").append((String) msg.obj);
                }
                if (mTargetRoamBSSID != null) {
                    sb.append(" Target=").append(mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(mIsAutoRoaming));
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                }
                sb.append(" nid=").append(msg.arg1);
                sb.append(" reason=").append(msg.arg2);
                if (mLastBssid != null) {
                    sb.append(" lastbssid=").append(mLastBssid);
                }
                if (mWifiInfo.getFrequency() != -1) {
                    sb.append(" freq=").append(mWifiInfo.getFrequency());
                    sb.append(" rssi=").append(mWifiInfo.getRssi());
                }
                break;
            case CMD_RSSI_POLL:
            case CMD_ONESHOT_RSSI_POLL:
            case CMD_UNWANTED_NETWORK:
            case WifiManager.RSSI_PKTCNT_FETCH:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (mWifiInfo.getSSID() != null) {
                    if (mWifiInfo.getSSID() != null) {
                        sb.append(" ").append(mWifiInfo.getSSID());
                    }
                }
                if (mWifiInfo.getBSSID() != null) {
                    sb.append(" ").append(mWifiInfo.getBSSID());
                }
                sb.append(" rssi=").append(mWifiInfo.getRssi());
                sb.append(" f=").append(mWifiInfo.getFrequency());
                sb.append(" sc=").append(mWifiInfo.score);
                sb.append(" link=").append(mWifiInfo.getLinkSpeed());
                sb.append(String.format(" tx=%.1f,", mWifiInfo.txSuccessRate));
                sb.append(String.format(" %.1f,", mWifiInfo.txRetriesRate));
                sb.append(String.format(" %.1f ", mWifiInfo.txBadRate));
                sb.append(String.format(" rx=%.1f", mWifiInfo.rxSuccessRate));
                sb.append(String.format(" bcn=%d", mRunningBeaconCount));
                if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LINK_INFO) {
                    sb.append(String.format(" snr=%d", mWifiInfo.semGetSnr()));
                    sb.append(String.format(" lqcm_tx=%d", mWifiInfo.semGetLqcmTx()));
                    sb.append(String.format(" lqcm_rx=%d", mWifiInfo.semGetLqcmRx()));
                    sb.append(String.format(" ap_cu=%d", mWifiInfo.semGetApCu()));
                }
                report = reportOnTime();
                if (report != null) {
                    sb.append(" ").append(report);
                }
                sb.append(String.format(" score=%d", mWifiInfo.score));
                break;
            case CMD_START_CONNECT:
            case WifiManager.CONNECT_NETWORK:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                config = mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config != null) {
                    sb.append(" ").append(config.configKey());
                }
                if (mTargetRoamBSSID != null) {
                    sb.append(" ").append(mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(mIsAutoRoaming));
                config = getCurrentWifiConfiguration();
                if (config != null) {
                    sb.append(config.configKey());
                }
                break;
            case CMD_START_ROAM:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                ScanResult result = (ScanResult) msg.obj;
                if (result != null) {
                    now = mClock.getWallClockMillis();
                    sb.append(" bssid=").append(result.BSSID);
                    sb.append(" rssi=").append(result.level);
                    sb.append(" freq=").append(result.frequency);
                    if (result.seen > 0 && result.seen < now) {
                        sb.append(" seen=").append(now - result.seen);
                    } else {
                        // Somehow the timestamp for this scan result is inconsistent
                        sb.append(" !seen=").append(result.seen);
                    }
                }
                if (mTargetRoamBSSID != null) {
                    sb.append(" ").append(mTargetRoamBSSID);
                }
                sb.append(" roam=").append(Boolean.toString(mIsAutoRoaming));
                sb.append(" fail count=").append(Integer.toString(mRoamFailCount));
                break;
            case CMD_ADD_OR_UPDATE_NETWORK:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    config = (WifiConfiguration) msg.obj;
                    sb.append(" ").append(config.configKey());
                    sb.append(" prio=").append(config.priority);
                    sb.append(" status=").append(config.status);
                    if (config.BSSID != null) {
                        sb.append(" ").append(config.BSSID);
                    }
                    WifiConfiguration curConfig = getCurrentWifiConfiguration();
                    if (curConfig != null) {
                        if (curConfig.configKey().equals(config.configKey())) {
                            sb.append(" is current");
                        } else {
                            sb.append(" current=").append(curConfig.configKey());
                            sb.append(" prio=").append(curConfig.priority);
                            sb.append(" status=").append(curConfig.status);
                        }
                    }
                }
                break;
            case WifiManager.DISABLE_NETWORK:
            case CMD_ENABLE_NETWORK:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                key = mWifiConfigManager.getLastSelectedNetworkConfigKey();
                if (key != null) {
                    sb.append(" last=").append(key);
                }
                config = mWifiConfigManager.getConfiguredNetwork(msg.arg1);
                if (config != null && (key == null || !config.configKey().equals(key))) {
                    sb.append(" target=").append(key);
                }
                break;
            case CMD_GET_CONFIGURED_NETWORKS:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" num=").append(mWifiConfigManager.getConfiguredNetworks().size());
                break;
            case CMD_GET_A_CONFIGURED_NETWORK: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                sb.append(" networkId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case CMD_PRE_DHCP_ACTION:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" txpkts=").append(mWifiInfo.txSuccess);
                sb.append(",").append(mWifiInfo.txBad);
                sb.append(",").append(mWifiInfo.txRetries);
                break;
            case CMD_POST_DHCP_ACTION:
                if (mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(mLinkProperties));
                }
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    NetworkInfo info = (NetworkInfo) msg.obj;
                    NetworkInfo.State state = info.getState();
                    NetworkInfo.DetailedState detailedState = info.getDetailedState();
                    if (state != null) {
                        sb.append(" st=").append(state);
                    }
                    if (detailedState != null) {
                        sb.append("/").append(detailedState);
                    }
                }
                break;
            case CMD_IP_CONFIGURATION_LOST:
                int count = -1;
                WifiConfiguration c = getCurrentWifiConfiguration();
                if (c != null) {
                    count = c.getNetworkSelectionStatus().getDisableReasonCounter(
                            WifiConfiguration.NetworkSelectionStatus.DISABLED_DHCP_FAILURE);
                }
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" failures: ");
                sb.append(Integer.toString(count));
                sb.append("/");
                sb.append(Integer.toString(mFacade.getIntegerSetting(
                        mContext, Settings.Global.WIFI_MAX_DHCP_RETRY_COUNT, 0)));
                if (mWifiInfo.getBSSID() != null) {
                    sb.append(" ").append(mWifiInfo.getBSSID());
                }
                sb.append(String.format(" bcn=%d", mRunningBeaconCount));
                break;
            case CMD_UPDATE_LINKPROPERTIES:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (mLinkProperties != null) {
                    sb.append(" ");
                    sb.append(getLinkPropertiesSummary(mLinkProperties));
                }
                break;
            case CMD_IP_REACHABILITY_LOST:
                if (msg.obj != null) {
                    sb.append(" ").append((String) msg.obj);
                }
                break;
            case CMD_INSTALL_PACKET_FILTER:
                sb.append(" len=" + ((byte[]) msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING:
                sb.append(" enabled=" + (boolean) msg.obj);
                break;
            case CMD_ROAM_WATCHDOG_TIMER:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(mRoamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(mDisconnectingWatchdogCount);
                break;
            case CMD_START_RSSI_MONITORING_OFFLOAD:
            case CMD_STOP_RSSI_MONITORING_OFFLOAD:
            case CMD_RSSI_THRESHOLD_BREACHED:
                sb.append(" rssi=");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" thresholds=");
                sb.append(Arrays.toString(mRssiRanges));
                break;
            case CMD_USER_SWITCH:
                sb.append(" userId=");
                sb.append(Integer.toString(msg.arg1));
                break;
            case CMD_IPV4_PROVISIONING_SUCCESS:
                sb.append(" ");
                sb.append(/* DhcpResults */ msg.obj);
                break;
            //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
            case WifiMonitor.ANQP_DONE_EVENT:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    AnqpEvent anqpEvent = (AnqpEvent)msg.obj;
                    if (anqpEvent.getBssid() != 0) {
                        sb.append(" BSSID=").append(Utils.macToString(anqpEvent.getBssid()));
                    }
                }
                break;
            //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
            //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
            case WifiMonitor.BSSID_PRUNED_EVENT:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.obj != null) {
                    sb.append(" ");
                    sb.append(msg.obj.toString());
                }
                break;
            //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
            default:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                break;
        }

        return sb.toString();
    }

    @Override
    protected String getWhatToString(int what) {
        String s = sGetWhatToString.get(what);
        if (s != null) {
            return s;
        }
        switch (what) {
            case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                s = "CMD_CHANNEL_HALF_CONNECTED";
                break;
            case AsyncChannel.CMD_CHANNEL_DISCONNECTED:
                s = "CMD_CHANNEL_DISCONNECTED";
                break;
            case WifiManager.DISABLE_NETWORK:
                s = "DISABLE_NETWORK";
                break;
            case WifiManager.CONNECT_NETWORK:
                s = "CONNECT_NETWORK";
                break;
            case WifiManager.SAVE_NETWORK:
                s = "SAVE_NETWORK";
                break;
            case WifiManager.FORGET_NETWORK:
                s = "FORGET_NETWORK";
                break;
            case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                s = "SUPPLICANT_STATE_CHANGE_EVENT";
                break;
            case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                s = "AUTHENTICATION_FAILURE_EVENT";
                break;
            case WifiMonitor.SUP_REQUEST_IDENTITY:
                s = "SUP_REQUEST_IDENTITY";
                break;
            case WifiMonitor.NETWORK_CONNECTION_EVENT:
                s = "NETWORK_CONNECTION_EVENT";
                break;
            case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                s = "NETWORK_DISCONNECTION_EVENT";
                break;
            case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                s = "ASSOCIATION_REJECTION_EVENT";
                break;
            case WifiMonitor.ANQP_DONE_EVENT:
                s = "ANQP_DONE_EVENT";
                break;
            case WifiMonitor.RX_HS20_ANQP_ICON_EVENT:
                s = "RX_HS20_ANQP_ICON_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_DONE_EVENT:
                s = "GAS_QUERY_DONE_EVENT";
                break;
            case WifiMonitor.HS20_REMEDIATION_EVENT:
                s = "HS20_REMEDIATION_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_START_EVENT:
                s = "GAS_QUERY_START_EVENT";
                break;
            case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT:
                s = "GROUP_CREATING_TIMED_OUT";
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED:
                s = "P2P_CONNECTION_CHANGED";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST:
                s = "DISCONNECT_WIFI_REQUEST";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE:
                s = "DISCONNECT_WIFI_RESPONSE";
                break;
            case WifiP2pServiceImpl.SET_MIRACAST_MODE:
                s = "SET_MIRACAST_MODE";
                break;
            case WifiP2pServiceImpl.BLOCK_DISCOVERY:
                s = "BLOCK_DISCOVERY";
                break;
            case WifiP2pServiceImpl.DISABLE_P2P_RSP:
                s = "DISABLE_P2P_RSP";
                break;
            case WifiManager.RSSI_PKTCNT_FETCH:
                s = "RSSI_PKTCNT_FETCH";
                break;
            case WifiMonitor.SUP_BIGDATA_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                s = "WifiMonitor.SUP_BIGDATA_EVENT";
                break;
            case WifiMonitor.EAP_EVENT_MESSAGE: //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
                s = "WifiMonitor.EAP_EVENT_MESSAGE";
                break;
            //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
            case WifiMonitor.BSSID_PRUNED_EVENT:
                s = "WifiMonitor.BSSID_PRUNED_EVENT";
                break;
            //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
            default:
                s = "what:" + Integer.toString(what);
                break;
        }
        return s;
    }

    private void handleScreenStateChanged(boolean screenOn) {
        mScreenOn = screenOn;
        if (mVerboseLoggingEnabled) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn
                    + " mUserWantsSuspendOpt=" + mUserWantsSuspendOpt
                    + " state " + getCurrentState().getName()
                    + " suppState:" + mSupplicantStateTracker.getSupplicantStateName());
        }
        enableRssiPolling(screenOn || (mRssiPollingScreenOffEnabled != 0)); //CscFeature_Wifi_SupportRssiPollStateDuringWifiCalling //SEC_PRODUCT_FEATURE_WLAN_SWITCHBOARD
        if (mUserWantsSuspendOpt.get()) {
            int shouldReleaseWakeLock = 0;
            if (screenOn) {
                sendMessage(CMD_SET_SUSPEND_OPT_ENABLED, 0, shouldReleaseWakeLock);
            } else {
                if (isConnected()) {
                    // Allow 2s for suspend optimizations to be set
                    mSuspendWakeLock.acquire(2000);
                    shouldReleaseWakeLock = 1;
                }
                sendMessage(CMD_SET_SUSPEND_OPT_ENABLED, 1, shouldReleaseWakeLock);
            }
        }

        getWifiLinkLayerStats();
        mOnTimeScreenStateChange = mOnTime;
        mLastScreenStateChangeTimeStamp = mLastLinkLayerStatsUpdate;

        mWifiMetrics.setScreenState(screenOn);

        mWifiConnectivityManager.handleScreenStateChanged(screenOn);
        mNetworkFactory.handleScreenStateChanged(screenOn);

        WifiLockManager wifiLockManager = mWifiInjector.getWifiLockManager();
        if (wifiLockManager != null) {
            wifiLockManager.handleScreenStateChanged(screenOn);
        }

        mSarManager.handleScreenStateChanged(screenOn);

        if (mVerboseLoggingEnabled) log("handleScreenStateChanged Exit: " + screenOn);
    }

    private boolean checkAndSetConnectivityInstance() {
        if (mCm == null) {
            mCm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        if (mCm == null) {
            Log.e(TAG, "Cannot retrieve connectivity service");
            return false;
        }
        return true;
    }

    private void setSuspendOptimizationsNative(int reason, boolean enabled) {
        if (mVerboseLoggingEnabled) {
            log("setSuspendOptimizationsNative: " + reason + " " + enabled
                    + " -want " + mUserWantsSuspendOpt.get()
                    + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName()
                    + " - " + Thread.currentThread().getStackTrace()[3].getMethodName()
                    + " - " + Thread.currentThread().getStackTrace()[4].getMethodName()
                    + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }
        //mWifiNative.setSuspendOptimizations(enabled);

        if (enabled) {
            mSuspendOptNeedsDisabled &= ~reason;
            /* None of dhcp, screen or highperf need it disabled and user wants it enabled */
            if (mSuspendOptNeedsDisabled == 0 && mUserWantsSuspendOpt.get()) {
                if (mVerboseLoggingEnabled) {
                    log("setSuspendOptimizationsNative do it " + reason + " " + enabled
                            + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName()
                            + " - " + Thread.currentThread().getStackTrace()[3].getMethodName()
                            + " - " + Thread.currentThread().getStackTrace()[4].getMethodName()
                            + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
                }
                mWifiNative.setSuspendOptimizations(mInterfaceName, true);
            }
        } else {
            mSuspendOptNeedsDisabled |= reason;
            mWifiNative.setSuspendOptimizations(mInterfaceName, false);
        }
    }

    /**
     * Makes a record of the user intent about suspend optimizations.
     */
    private void setSuspendOptimizations(int reason, boolean enabled) {
        if (mVerboseLoggingEnabled) log("setSuspendOptimizations: " + reason + " " + enabled);
        if (enabled) {
            mSuspendOptNeedsDisabled &= ~reason;
        } else {
            mSuspendOptNeedsDisabled |= reason;
        }
        if (mVerboseLoggingEnabled) log("mSuspendOptNeedsDisabled " + mSuspendOptNeedsDisabled);
    }

    //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
    private CustomDeviceManagerProxy customDeviceManager = null;
    private void knoxAutoSwitchPolicy(int newRssi) {
        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CUSTOMIZATION_SDK) {
            if (mLastConnectedTime == -1) {
                logd("knoxCustom WifiAutoSwitch: not connected yet");
                return;
            }
            if (newRssi == WifiInfo.INVALID_RSSI) {
                logd("knoxCustom WifiAutoSwitch: newRssi is invalid");
                return;
            }
            if (customDeviceManager == null) {
                customDeviceManager = CustomDeviceManagerProxy.getInstance();
            }
            if (customDeviceManager.getWifiAutoSwitchState()) {
                int thresholdRssi = customDeviceManager.getWifiAutoSwitchThreshold();
                if(newRssi < thresholdRssi) {
                    // Check to see if there is another access point that we can connect to.
                    // Only attempt to connect to configured networks.
                    if (DBG) {
                        logd("KnoxCustom WifiAutoSwitch: current = " + newRssi);
                    }
                    long now = mClock.getElapsedSinceBootMillis();
                    if (DBG) {
                        logd("KnoxCustom WifiAutoSwitch: last check was " + (now - mLastConnectedTime) + " ms ago");
                    }
                    int delay = customDeviceManager.getWifiAutoSwitchDelay();
                    if (now < mLastConnectedTime + delay * 1000L) {
                        logd("KnoxCustom WifiAutoSwitch: delay " + delay);
                        return;
                    }

                    int bestRssi = thresholdRssi;
                    int bestNetworkId = -1;
                    List<ScanResult> scanResults = mScanRequestProxy.getScanResults();
                    List<WifiConfiguration> configs = mWifiConfigManager.getSavedNetworks(Process.WIFI_UID);
                    for (WifiConfiguration config : configs) {
                        for (ScanResult result : scanResults) {
                            String ssid = "\"" + result.SSID + "\"";
                            if(config.SSID.equals(ssid)) {
                                if (DBG) {
                                    logd("KnoxCustom WifiAutoSwitch: " + config.SSID + " = " + result.level);
                                }
                                if (result.level > bestRssi) {
                                    bestRssi = result.level;
                                    bestNetworkId = config.networkId;
                                }
                            }
                        }
                    }
                    if (bestNetworkId != -1) {
                        // Switch to this network.
                        if (DBG) {
                            logd("KnoxCustom WifiAutoSwitch: switching to " + bestNetworkId);
                        }
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_DISCONNECT_BY_MDM);
                        mWifiNative.disconnect(mInterfaceName);
                        mWifiConfigManager.enableNetwork(bestNetworkId, true, Process.SYSTEM_UID);
                        mWifiNative.reconnect(mInterfaceName);
                    }
                }
            }
        }
    }
    //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }

    /*
     * Fetch RSSI, linkspeed, and frequency on current connection
     */
    private void fetchRssiLinkSpeedAndFrequencyNative() {
        WifiNative.SignalPollResult pollResult = mWifiNative.signalPoll(mInterfaceName);
        if (pollResult == null) {
            return;
        }

        int newRssi = pollResult.currentRssi;
        int newTxLinkSpeed = pollResult.txBitrate;
        int newFrequency = pollResult.associationFrequency;
        int newRxLinkSpeed = pollResult.rxBitrate;

        if (mVerboseLoggingEnabled) {
            logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + newRssi
                    + " TxLinkspeed=" + newTxLinkSpeed + " freq=" + newFrequency
                    + " RxLinkSpeed=" + newRxLinkSpeed);
        }

        if (newRssi > WifiInfo.INVALID_RSSI && newRssi < WifiInfo.MAX_RSSI) {
            // screen out invalid values
            /* some implementations avoid negative values by adding 256
             * so we need to adjust for that here.
             */
            if (newRssi > 0) {
                Log.wtf(TAG, "Error! +ve value RSSI: " + newRssi);
                newRssi -= 256;
            }
            mWifiInfo.setRssi(newRssi);
            /*
             * Rather then sending the raw RSSI out every time it
             * changes, we precalculate the signal level that would
             * be displayed in the status bar, and only send the
             * broadcast if that much more coarse-grained number
             * changes. This cuts down greatly on the number of
             * broadcasts, at the cost of not informing others
             * interested in RSSI of all the changes in signal
             * level.
             */
            int newSignalLevel = WifiManager.calculateSignalLevel(newRssi, WifiManager.RSSI_LEVELS);
            if (newSignalLevel != mLastSignalLevel) {
                updateCapabilities();
                sendRssiChangeBroadcast(newRssi);

                mBigDataManager.addOrUpdateValue( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    WifiBigDataLogManager.LOGGING_TYPE_UPDATE_DATA_RATE,
                    newTxLinkSpeed);
            }
            mLastSignalLevel = newSignalLevel;
        } else {
            mWifiInfo.setRssi(WifiInfo.INVALID_RSSI);
            updateCapabilities();
        }
        /*
         * set Tx link speed only if it is valid
         */
        if (newTxLinkSpeed > 0) {
            mWifiInfo.setLinkSpeed(newTxLinkSpeed);
            mWifiInfo.setTxLinkSpeedMbps(newTxLinkSpeed);
        }
        /*
         * set Rx link speed only if it is valid
         */
        if (newRxLinkSpeed > 0) {
            mWifiInfo.setRxLinkSpeedMbps(newRxLinkSpeed);
        }
        if (newFrequency > 0) {
            mWifiInfo.setFrequency(newFrequency);
        }
        mWifiInfo.semSetBcnCnt(mRunningBeaconCount); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LINK_INFO
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LINK_INFO) {
            int snr = mWifiNative.getSnr(mInterfaceName);
            int lqcmReport = mWifiNative.getLqcmReport(mInterfaceName);
            int ApCu = mWifiNative.getApCu(mInterfaceName);
            mWifiInfo.semSetSnr(snr > 0 ? snr : 0);
            int lqcmRxIndex = lqcmReport != -1 ? (lqcmReport & 0xff0000) >> 16 : 0xff;
            int lqcmTxIndex = lqcmReport != -1 ? (lqcmReport & 0x00ff00) >> 8 : 0xff;
            mWifiInfo.semSetLqcmTx(lqcmTxIndex);
            mWifiInfo.semSetLqcmRx(lqcmRxIndex);
            mWifiInfo.semSetApCu(ApCu > -1 ? ApCu : -1);
        }
        mWifiConfigManager.updateScanDetailCacheFromWifiInfo(mWifiInfo);
        /*
         * Increment various performance metrics
         */
        mWifiMetrics.handlePollResult(mWifiInfo);
    }

    // Polling has completed, hence we won't have a score anymore
    private void cleanWifiScore() {
        mWifiInfo.txBadRate = 0;
        mWifiInfo.txSuccessRate = 0;
        mWifiInfo.txRetriesRate = 0;
        mWifiInfo.rxSuccessRate = 0;
        mWifiScoreReport.reset();
        mLastLinkLayerStats = null;
    }

    private void checkAndResetMtu() { //SEC_PRODUCT_FEATURE_WLAN_RESET_MTU
        final int DEFAULT_MTU_VALUE = 1500;
        if (mLinkProperties != null) {
            int mtu = mLinkProperties.getMtu();
            if (mtu != DEFAULT_MTU_VALUE && mtu != 0) {
                Log.i(TAG, "reset MTU value from " + mtu);
                mWifiNative.initializeMtu(mInterfaceName);
            }
        }
    }

    private void updateLinkProperties(LinkProperties newLp) {
        if (true) {
            log("Link configuration changed for netId: " + mLastNetworkId
                    + " old: " + mLinkProperties + " new: " + newLp);
        }
        mOldLinkProperties = mLinkProperties;

        // We own this instance of LinkProperties because IpClient passes us a copy.
        mLinkProperties = newLp;
        if (mNetworkAgent != null) {
            mNetworkAgent.sendLinkProperties(mLinkProperties);
        }

        if (getNetworkDetailedState() == DetailedState.CONNECTED) {
            // If anything has changed and we're already connected, send out a notification.
            // TODO: Update all callers to use NetworkCallbacks and delete this.
            sendLinkConfigurationChangedBroadcast();

            // >>>WCM>>>
            if (detectIpv6ProvisioningFailure(mOldLinkProperties, mLinkProperties)) {
                handleWifiNetworkCallbacks(WifiClientModeChannel.CALLBACK_NOTIFY_PROVISIONING_FAIL);
            }
            // <<<WCM<<<
        }

        if (mVerboseLoggingEnabled) {
            StringBuilder sb = new StringBuilder();
            sb.append("updateLinkProperties nid: " + mLastNetworkId);
            sb.append(" state: " + getNetworkDetailedState());

            if (mLinkProperties != null) {
                sb.append(" ");
                sb.append(getLinkPropertiesSummary(mLinkProperties));
            }
            logd(sb.toString());
        }

        // >>>WCM>>>
        handleWifiNetworkCallbacks(WifiClientModeChannel.CALLBACK_NOTIFY_LINK_PROPERTIES_UPDATED);
        // <<<WCM<<<
    }

    /**
     * Clears all our link properties.
     */
    private void clearLinkProperties() {
        // Clear the link properties obtained from DHCP. The only caller of this
        // function has already called IpClient#stop(), which clears its state.
        synchronized (mDhcpResultsLock) {
            if (mDhcpResults != null) {
                mDhcpResults.clear();
            }
        }

        // Now clear the merged link properties.
        mLinkProperties.clear();
        if (mNetworkAgent != null) mNetworkAgent.sendLinkProperties(mLinkProperties);
    }

    private void sendRssiChangeBroadcast(final int newRssi) {
        try {
            mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
            // Won't happen.
        }
        StatsLog.write(StatsLog.WIFI_SIGNAL_STRENGTH_CHANGED,
                WifiManager.calculateSignalLevel(newRssi, WifiManager.RSSI_LEVELS));

        Intent intent = new Intent(WifiManager.RSSI_CHANGED_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.putExtra(WifiManager.EXTRA_NEW_RSSI, newRssi);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL,
                android.Manifest.permission.ACCESS_WIFI_STATE);
    }

    private void sendNetworkStateChangeBroadcast(String bssid) {
        Intent intent = new Intent(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        NetworkInfo networkInfo = new NetworkInfo(mNetworkInfo);
        networkInfo.setExtraInfo(null);
        intent.putExtra(WifiManager.EXTRA_NETWORK_INFO, networkInfo);
        //TODO(b/69974497) This should be non-sticky, but settings needs fixing first.
        mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        //[@MNO ConfigImplicitBroadcasts CSC feature, send explicit broadcast to MVS (Buganizer: 64022399)
        if ("VZW".equals(SemCscFeature.getInstance().getString(CscFeatureTagCOMMON.TAG_CSCFEATURE_COMMON_CONFIGIMPLICITBROADCASTS))) {
            Intent cloneIntent = (Intent) intent.clone();
            cloneIntent.setPackage("com.verizon.mips.services");
            mContext.sendBroadcastAsUser(cloneIntent, UserHandle.ALL);
        }
        //]

        // Secure Wi-Fi - START
        if (SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_SECURE_WIFI")) {
            PackageManager pm = mContext.getPackageManager();
            //check whether if package is exist or not.
            try {
                pm.getPackageInfo("com.samsung.android.fast", 0);
                Intent cloneIntent = (Intent) intent.clone();
                cloneIntent.setPackage("com.samsung.android.fast");
                mContext.sendBroadcastAsUser(cloneIntent, UserHandle.ALL);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        // Secure Wi-Fi - END

        // Tencent Security Wi-Fi - START	
        if ("TencentSecurityWiFi".equals(CONFIG_SECURE_SVC_INTEGRATION)) {
            PackageManager pm = mContext.getPackageManager();
            try {
                pm.getPackageInfo("com.samsung.android.tencentwifisecurity", 0);
                Intent cloneIntent = (Intent) intent.clone();
                cloneIntent.setPackage("com.samsung.android.tencentwifisecurity");
                mContext.sendBroadcastAsUser(cloneIntent, UserHandle.ALL);
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        // Tencent Security Wi-Fi - END

    }

    private void sendLinkConfigurationChangedBroadcast() {
        Intent intent = new Intent(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.putExtra(WifiManager.EXTRA_LINK_PROPERTIES, new LinkProperties(mLinkProperties));
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /**
     * Helper method used to send state about supplicant - This is NOT information about the current
     * wifi connection state.
     *
     * TODO: b/79504296 This broadcast has been deprecated and should be removed
     */
    private void sendSupplicantConnectionChangedBroadcast(boolean connected) {
        Intent intent = new Intent(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.putExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, connected);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /**
     * Record the detailed state of a network.
     *
     * @param state the new {@code DetailedState}
     */
    private boolean setNetworkDetailedState(NetworkInfo.DetailedState state) {
        boolean hidden = false;

        if (mIsAutoRoaming) {
            // There is generally a confusion in the system about colluding
            // WiFi Layer 2 state (as reported by supplicant) and the Network state
            // which leads to multiple confusion.
            //
            // If link is roaming, we already have an IP address
            // as well we were connected and are doing L2 cycles of
            // reconnecting or renewing IP address to check that we still have it
            // This L2 link flapping should ne be reflected into the Network state
            // which is the state of the WiFi Network visible to Layer 3 and applications
            // Note that once roaming is completed, we will
            // set the Network state to where it should be, or leave it as unchanged
            //
            hidden = true;
        }
        if (mVerboseLoggingEnabled) {
            log("setDetailed state, old ="
                    + mNetworkInfo.getDetailedState() + " and new state=" + state
                    + " hidden=" + hidden);
        }

        if (Vendor.VZW == mOpBranding ////SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                && mSemWifiHiddenNetworkTracker != null
                && state == DetailedState.CONNECTING) {
            mSemWifiHiddenNetworkTracker.stopTracking();
        }

        if (hidden) {
            return false;
        }

        if (state != mNetworkInfo.getDetailedState()) {
            mNetworkInfo.setDetailedState(state, null, null);
            if (mNetworkAgent != null) {
                mNetworkAgent.sendNetworkInfo(mNetworkInfo);
            }
            sendNetworkStateChangeBroadcast(null);
            return true;
        }
        return false;
    }

    private DetailedState getNetworkDetailedState() {
        return mNetworkInfo.getDetailedState();
    }

    private SupplicantState handleSupplicantStateChange(Message message) {
        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
        SupplicantState state = stateChangeResult.state;
        mWifiScoreCard.noteSupplicantStateChanging(mWifiInfo, state);
        // Supplicant state change
        // [31-13] Reserved for future use
        // [8 - 0] Supplicant state (as defined in SupplicantState.java)
        // 50023 supplicant_state_changed (custom|1|5)
        mWifiInfo.setSupplicantState(state);
        // Network id and SSID are only valid when we start connecting
        if (SupplicantState.isConnecting(state)) {
            mWifiInfo.setNetworkId(stateChangeResult.networkId);
            mWifiInfo.setBSSID(stateChangeResult.BSSID);
            mWifiInfo.setSSID(stateChangeResult.wifiSsid);
        } else {
            // Reset parameters according to WifiInfo.reset()
            mWifiInfo.setNetworkId(WifiConfiguration.INVALID_NETWORK_ID);
            mWifiInfo.setBSSID(null);
            mWifiInfo.setSSID(null);
        }
        updateL2KeyAndGroupHint();
        // SSID might have been updated, so call updateCapabilities
        updateCapabilities();

        final WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            mWifiInfo.setEphemeral(config.ephemeral);
            mWifiInfo.setTrusted(config.trusted);
            mWifiInfo.setOsuAp(config.osu);
            if (config.fromWifiNetworkSpecifier || config.fromWifiNetworkSuggestion) {
                mWifiInfo.setNetworkSuggestionOrSpecifierPackageName(config.creatorName);
            }

            // Set meteredHint if scan result says network is expensive
            ScanDetailCache scanDetailCache = mWifiConfigManager.getScanDetailCacheForNetwork(
                    config.networkId);
            if (scanDetailCache != null) {
                ScanDetail scanDetail = scanDetailCache.getScanDetail(stateChangeResult.BSSID);
                if (scanDetail != null) {
                    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_KTT_GIGA_AP
                    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                    updateWifiInfoForVendors(scanDetail.getScanResult());
                    mWifiInfo.setFrequency(scanDetail.getScanResult().frequency);
                    NetworkDetail networkDetail = scanDetail.getNetworkDetail();
                    if (networkDetail != null
                            && networkDetail.getAnt() == NetworkDetail.Ant.ChargeablePublic) {
                        Log.d(TAG, "setMeteredHint by ChargeablePublic");
                        mWifiInfo.setMeteredHint(true);
                    }
                    mWifiInfo.setWifiMode(scanDetail.getScanResult().wifiMode); //SEC_PRODUCT_FEATURE_WLAN_WIFIMODE
                } else {
                    Log.d(TAG, "can't update vendor infos, bssid: " + stateChangeResult.BSSID);
                }
            }
        }

        mSupplicantStateTracker.sendMessage(Message.obtain(message));
        mWifiScoreCard.noteSupplicantStateChanged(mWifiInfo);
        return state;
    }

    /**
     * Tells IpClient what L2Key and GroupHint to use for IpMemoryStore.
     */
    private void updateL2KeyAndGroupHint() {
        if (mIpClient != null) {
            Pair<String, String> p = mWifiScoreCard.getL2KeyAndGroupHint(mWifiInfo);
            if (!p.equals(mLastL2KeyAndGroupHint)) {
                if (mIpClient.setL2KeyAndGroupHint(p.first, p.second)) {
                    mLastL2KeyAndGroupHint = p;
                } else {
                    mLastL2KeyAndGroupHint = null;
                }
            }
        }
    }
    private @Nullable Pair<String, String> mLastL2KeyAndGroupHint = null;

    /**
     * Resets the Wi-Fi Connections by clearing any state, resetting any sockets
     * using the interface, stopping DHCP & disabling interface
     */
    private void handleNetworkDisconnect() {
        if (mVerboseLoggingEnabled) {
            log("handleNetworkDisconnect: Stopping DHCP and clearing IP"
                    + " stack:" + Thread.currentThread().getStackTrace()[2].getMethodName()
                    + " - " + Thread.currentThread().getStackTrace()[3].getMethodName()
                    + " - " + Thread.currentThread().getStackTrace()[4].getMethodName()
                    + " - " + Thread.currentThread().getStackTrace()[5].getMethodName());
        }

        WifiConfiguration wifiConfig = getCurrentWifiConfiguration();
        if (wifiConfig != null) {
            ScanResultMatchInfo matchInfo = ScanResultMatchInfo.fromWifiConfiguration(wifiConfig);
            //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI - disable google autowifi
            //mWifiInjector.getWakeupController().setLastDisconnectInfo(matchInfo);
            mWifiNetworkSuggestionsManager.handleDisconnect(wifiConfig, getCurrentBSSID());
        }

        stopRssiMonitoringOffload();

        clearTargetBssid("handleNetworkDisconnect");

        stopIpClient();

        /* Reset data structures */
        mWifiScoreReport.reset();
        mWifiInfo.reset();
        /* Reset roaming parameters */
        mIsAutoRoaming = false;

        setNetworkDetailedState(DetailedState.DISCONNECTED);
        synchronized (mNetworkAgentLock) {
            if (mNetworkAgent != null) {
                checkAndResetMtu(); //SEC_PRODUCT_FEATURE_WLAN_RESET_MTU
                mNetworkAgent.sendNetworkInfo(mNetworkInfo);
                mNetworkAgent = null;
            }
        }

        /* Clear network properties */
        clearLinkProperties();

        /* Cend event to CM & network change broadcast */
        sendNetworkStateChangeBroadcast(mLastBssid);

        mLastBssid = null;
        mLastLinkLayerStats = null;
        registerDisconnected();
        mLastNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
        mWifiScoreCard.resetConnectionState();
        updateL2KeyAndGroupHint();
    }

    void handlePreDhcpSetup() {
        if (!mBluetoothConnectionActive) {
            /*
             * There are problems setting the Wi-Fi driver's power
             * mode to active when bluetooth coexistence mode is
             * enabled or sense.
             * <p>
             * We set Wi-Fi to active mode when
             * obtaining an IP address because we've found
             * compatibility issues with some routers with low power
             * mode.
             * <p>
             * In order for this active power mode to properly be set,
             * we disable coexistence mode until we're done with
             * obtaining an IP address.  One exception is if we
             * are currently connected to a headset, since disabling
             * coexistence would interrupt that connection.
             */
            // Disable the coexistence mode
            mWifiNative.setBluetoothCoexistenceMode(
                    mInterfaceName, WifiNative.BLUETOOTH_COEXISTENCE_MODE_DISABLED);
        }

        // Disable power save and suspend optimizations during DHCP
        // Note: The order here is important for now. Brcm driver changes
        // power settings when we control suspend mode optimizations.
        // TODO: Remove this comment when the driver is fixed.
        setSuspendOptimizationsNative(SUSPEND_DUE_TO_DHCP, false);
        setPowerSave(false);

        // Update link layer stats
        getWifiLinkLayerStats();

        if (mWifiP2pChannel != null) {
            /* P2p discovery breaks dhcp, shut it down in order to get through this */
            Message msg = new Message();
            msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
            msg.arg1 = WifiP2pServiceImpl.ENABLED;
            msg.arg2 = CMD_PRE_DHCP_ACTION_COMPLETE;
            msg.obj = ClientModeImpl.this;
            mWifiP2pChannel.sendMessage(msg);
        } else {
            // If the p2p service is not running, we can proceed directly.
            sendMessage(CMD_PRE_DHCP_ACTION_COMPLETE);
        }
        // >>>WCM>>>
        handleWifiNetworkCallbacks(WifiClientModeChannel.CALLBACK_NOTIFY_DHCP_SESSION_START);
        // <<<WCM<<<
    }

    void handlePostDhcpSetup() {
        /* Restore power save and suspend optimizations */
        setSuspendOptimizationsNative(SUSPEND_DUE_TO_DHCP, true);
        setPowerSave(true);

        p2pSendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, WifiP2pServiceImpl.DISABLED);

        if (!mHandleIfaceIsUp) { //SEC_PRODUCT_FEATURE_WLAN_ERROR_CODE_REFACTORING
            Log.w(TAG, "handlePostDhcpSetup, mHandleIfaceIsUp is false. skip setBluetoothCoexistenceMode");
            return;
        }
        // Set the coexistence mode back to its default value
        mWifiNative.setBluetoothCoexistenceMode(
                mInterfaceName, WifiNative.BLUETOOTH_COEXISTENCE_MODE_SENSE);
        // >>>WCM>>>
        handleWifiNetworkCallbacks(WifiClientModeChannel.CALLBACK_NOTIFY_DHCP_SESSION_COMPLETE);
        // <<<WCM<<<
    }

    /**
     * Set power save mode
     *
     * @param ps true to enable power save (default behavior)
     *           false to disable power save.
     * @return true for success, false for failure
     */
    public boolean setPowerSave(boolean ps) {
        if (mInterfaceName != null) {
            if (mVerboseLoggingEnabled) {
                Log.d(TAG, "Setting power save for: " + mInterfaceName + " to: " + ps);
            }
            if (!mHandleIfaceIsUp) { //SEC_PRODUCT_FEATURE_WLAN_ERROR_CODE_REFACTORING
                Log.w(TAG, "setPowerSave, mHandleIfaceIsUp is false");
                return false;
            }
            mWifiNative.setPowerSave(mInterfaceName, ps);
        } else {
            Log.e(TAG, "Failed to setPowerSave, interfaceName is null");
            return false;
        }
        return true;
    }

    /**
     * Set low latency mode
     *
     * @param enabled true to enable low latency
     *                false to disable low latency (default behavior).
     * @return true for success, false for failure
     */
    public boolean setLowLatencyMode(boolean enabled) {
        if (mVerboseLoggingEnabled) {
            Log.d(TAG, "Setting low latency mode to " + enabled);
        }
        if (!mWifiNative.setLowLatencyMode(enabled)) {
            Log.e(TAG, "Failed to setLowLatencyMode");
            return false;
        }
        return true;
    }

    @VisibleForTesting
    public static final long DIAGS_CONNECT_TIMEOUT_MILLIS = 60 * 1000;
    /**
     * Inform other components that a new connection attempt is starting.
     */
    private void reportConnectionAttemptStart(
            WifiConfiguration config, String targetBSSID, int roamType) {
        mWifiMetrics.startConnectionEvent(config, targetBSSID, roamType);
        mWifiDiagnostics.reportConnectionEvent(WifiDiagnostics.CONNECTION_EVENT_STARTED);
        mWrongPasswordNotifier.onNewConnectionAttempt();
        removeMessages(CMD_DIAGS_CONNECT_TIMEOUT);
        sendMessageDelayed(CMD_DIAGS_CONNECT_TIMEOUT, DIAGS_CONNECT_TIMEOUT_MILLIS);
    }

    private void handleConnectionAttemptEndForDiagnostics(int level2FailureCode) {
        switch (level2FailureCode) {
            case WifiMetrics.ConnectionEvent.FAILURE_NONE:
                break;
            case WifiMetrics.ConnectionEvent.FAILURE_CONNECT_NETWORK_FAILED:
                // WifiDiagnostics doesn't care about pre-empted connections, or cases
                // where we failed to initiate a connection attempt with supplicant.
                break;
            default:
                removeMessages(CMD_DIAGS_CONNECT_TIMEOUT);
                mWifiDiagnostics.reportConnectionEvent(WifiDiagnostics.CONNECTION_EVENT_FAILED);
        }
    }

    /**
     * Inform other components (WifiMetrics, WifiDiagnostics, WifiConnectivityManager, etc.) that
     * the current connection attempt has concluded.
     */
    private void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode,
            int level2FailureReason) {
        if (level2FailureCode != WifiMetrics.ConnectionEvent.FAILURE_NONE) {
            mWifiScoreCard.noteConnectionFailure(mWifiInfo,
                    level2FailureCode, connectivityFailureCode);
        }
        // if connected, this should be non-null.
        WifiConfiguration configuration = getCurrentWifiConfiguration();
        if (configuration == null) {
            // If not connected, this should be non-null.
            configuration = getTargetWifiConfiguration();
        }
        mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode,
                level2FailureReason);
        mWifiConnectivityManager.handleConnectionAttemptEnded(level2FailureCode);
        if (configuration != null) {
            mNetworkFactory.handleConnectionAttemptEnded(level2FailureCode, configuration);
            mWifiNetworkSuggestionsManager.handleConnectionAttemptEnded(
                    level2FailureCode, configuration, getCurrentBSSID());
        }
        handleConnectionAttemptEndForDiagnostics(level2FailureCode);
    }

    private void handleIPv4Success(DhcpResults dhcpResults) {
        if (mVerboseLoggingEnabled) {
            logd("handleIPv4Success <" + dhcpResults.toString() + ">");
            logd("link address " + dhcpResults.ipAddress);
        }

        Inet4Address addr;
        synchronized (mDhcpResultsLock) {
            mDhcpResults = dhcpResults;
            ReportUtil.updateDhcpResults(mDhcpResults); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
            addr = (Inet4Address) dhcpResults.ipAddress.getAddress();
        }

        if (mIsAutoRoaming) {
            int previousAddress = mWifiInfo.getIpAddress();
            int newAddress = NetworkUtils.inetAddressToInt(addr);
            if (previousAddress != newAddress) {
                logd("handleIPv4Success, roaming and address changed"
                        + mWifiInfo + " got: " + addr);
            }
        }

        mWifiInfo.setInetAddress(addr);

        final WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            mWifiInfo.setEphemeral(config.ephemeral);
            mWifiInfo.setTrusted(config.trusted);
        }

        // Set meteredHint if DHCP result says network is metered
        if (dhcpResults.hasMeteredHint()) {
            mWifiInfo.setMeteredHint(true);
        }

        updateCapabilities(config);
    }

    private void handleSuccessfulIpConfiguration() {
        mLastSignalLevel = -1; // Force update of signal strength
        WifiConfiguration c = getCurrentWifiConfiguration();
        if (c != null) {
            // Reset IP failure tracking
            c.getNetworkSelectionStatus().clearDisableReasonCounter(
                    WifiConfiguration.NetworkSelectionStatus.DISABLED_DHCP_FAILURE);

            // Tell the framework whether the newly connected network is trusted or untrusted.
            updateCapabilities(c);
        }
        mWifiScoreCard.noteIpConfiguration(mWifiInfo);
    }

    private void handleIPv4Failure() {
        // TODO: Move this to provisioning failure, not DHCP failure.
        // DHCPv4 failure is expected on an IPv6-only network.
        mWifiDiagnostics.captureBugReportData(WifiDiagnostics.REPORT_REASON_DHCP_FAILURE);
        if (mVerboseLoggingEnabled) {
            int count = -1;
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (config != null) {
                count = config.getNetworkSelectionStatus().getDisableReasonCounter(
                        WifiConfiguration.NetworkSelectionStatus.DISABLED_DHCP_FAILURE);
            }
            log("DHCP failure count=" + count);
        }
        reportConnectionAttemptEnd(
                WifiMetrics.ConnectionEvent.FAILURE_DHCP,
                WifiMetricsProto.ConnectionEvent.HLF_DHCP,
                WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
        synchronized (mDhcpResultsLock) {
            if (mDhcpResults != null) {
                mDhcpResults.clear();
            }
        }
        if (mVerboseLoggingEnabled) {
            logd("handleIPv4Failure");
        }
    }

    private void handleIpConfigurationLost() {
        mWifiInfo.setInetAddress(null);
        mWifiInfo.setMeteredHint(false);

        mWifiConfigManager.updateNetworkSelectionStatus(mLastNetworkId,
                WifiConfiguration.NetworkSelectionStatus.DISABLED_DHCP_FAILURE);

        /* DHCP times out after about 30 seconds, we do a
         * disconnect thru supplicant, we will let autojoin retry connecting to the network
         */
        mWifiNative.disconnect(mInterfaceName);
    }

    private void handleIpReachabilityLost() {
        mWifiScoreCard.noteIpReachabilityLost(mWifiInfo);
        mWifiInfo.setInetAddress(null);
        mWifiInfo.setMeteredHint(false);

        // Disconnect via supplicant, and let autojoin retry connecting to the network.
        mWifiNative.disconnect(mInterfaceName);
    }

    /*
     * Read a MAC address in /proc/arp/table, used by ClientModeImpl
     * so as to record MAC address of default gateway.
     **/
    private String macAddressFromRoute(String ipAddress) {
        String macAddress = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/net/arp"));

            // Skip over the line bearing column titles
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("[ ]+");
                if (tokens.length < 6) {
                    continue;
                }

                // ARP column format is
                // Address HWType HWAddress Flags Mask IFace
                String ip = tokens[0];
                String mac = tokens[3];

                if (ipAddress.equals(ip)) {
                    macAddress = mac;
                    break;
                }
            }

            if (macAddress == null) {
                loge("Did not find remoteAddress {" + ipAddress + "} in /proc/net/arp");
            }

        } catch (FileNotFoundException e) {
            loge("Could not open /proc/net/arp to lookup mac address");
        } catch (IOException e) {
            loge("Could not read /proc/net/arp to lookup mac address");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                // Do nothing
            }
        }
        return macAddress;

    }

    /**
     * Determine if the specified auth failure is considered to be a permanent wrong password
     * failure. The criteria for such failure is when wrong password error is detected
     * and the network had never been connected before.
     *
     * For networks that have previously connected successfully, we consider wrong password
     * failures to be temporary, to be on the conservative side.  Since this might be the
     * case where we are trying to connect to a wrong network (e.g. A network with same SSID
     * but different password).
     * SEC_PRODUCT_FEATURE_WLAN_SUPPORT_RETRY_POPUP
     */
    private boolean isPermanentWrongPasswordFailure(WifiConfiguration network, int reasonCode) {
        if (reasonCode != WifiManager.ERROR_AUTH_FAILURE_WRONG_PSWD) {
            return false;
        }
        if (network != null && network.getNetworkSelectionStatus().getHasEverConnected()) {
            return false;
        }
        return true;
    }

    void registerNetworkFactory() {
        if (!checkAndSetConnectivityInstance()) return;
        mNetworkFactory.register();
        mUntrustedNetworkFactory.register();
    }

    public void sendBroadcastIssueTrackerSysDump(int reason){ //SEC_PRODUCT_FEATURE_WLAN_ISSUETRACKER_CONTROL
        Log.i(TAG, "sendBroadcastIssueTrackerSysDump reason : " + reason);

        if (mIssueTrackerOn) {
            Log.i(TAG, "sendBroadcastIssueTrackerSysDump mIssueTrackerOn true");

            Intent issueTrackerIntent = new Intent("com.sec.android.ISSUE_TRACKER_ACTION");
            issueTrackerIntent.putExtra("ERRPKG", "WifiStateMachine");

            switch (reason) {
                case ISSUE_TRACKER_SYSDUMP_HANG:
                    issueTrackerIntent.putExtra("ERRCODE", -110);
                    issueTrackerIntent.putExtra("ERRNAME", "HANGED");
                    issueTrackerIntent.putExtra("ERRMSG", "Wi-Fi chip HANGED");
                    break;

                case ISSUE_TRACKER_SYSDUMP_UNWANTED:
                    issueTrackerIntent.putExtra("ERRCODE", -110);
                    issueTrackerIntent.putExtra("ERRNAME", "UNWANTED");
                    issueTrackerIntent.putExtra("ERRMSG", "Wi-Fi UNWANTED happend");
                    break;

                case ISSUE_TRACKER_SYSDUMP_DISC:
                    issueTrackerIntent.putExtra("ERRCODE", -110);
                    issueTrackerIntent.putExtra("ERRNAME", "DISC");
                    issueTrackerIntent.putExtra("ERRMSG", "Wi-Fi DISC happend");
                    break;
            }

            mContext.sendBroadcastAsUser(issueTrackerIntent, UserHandle.ALL);
        }
    }

    /**
     * ClientModeImpl needs to enable/disable other services when wifi is in client mode.  This
     * method allows ClientModeImpl to get these additional system services.
     *
     * At this time, this method is used to setup variables for P2P service and Wifi Aware.
     */
    private void getAdditionalWifiServiceInterfaces() {
        // First set up Wifi Direct
        if (mP2pSupported) {
            IBinder s1 = mFacade.getService(Context.WIFI_P2P_SERVICE);
            WifiP2pServiceImpl wifiP2pServiceImpl =
                    (WifiP2pServiceImpl) IWifiP2pManager.Stub.asInterface(s1);

            if (wifiP2pServiceImpl != null) {
                mWifiP2pChannel = new AsyncChannel();
                mWifiP2pChannel.connect(mContext, getHandler(),
                        wifiP2pServiceImpl.getP2pStateMachineMessenger());
            }
        }
    }

     /**
     * Dynamically change the MAC address to use the locally randomized
     * MAC address generated for each network.
     * @param config WifiConfiguration with mRandomizedMacAddress to change into. If the address
     * is masked out or not set, it will generate a new random MAC address.
     */
    private void configureRandomizedMacAddress(WifiConfiguration config) {
        if (config == null) {
            Log.e(TAG, "No config to change MAC address to");
            return;
        }
        MacAddress currentMac = MacAddress.fromString(mWifiNative.getMacAddress(mInterfaceName));
        MacAddress newMac = config.getOrCreateRandomizedMacAddress();
        mWifiConfigManager.setNetworkRandomizedMacAddress(config.networkId, newMac);
        if (!WifiConfiguration.isValidMacAddressForRandomization(newMac)) {
            Log.wtf(TAG, "Config generated an invalid MAC address");
        } else if (currentMac.equals(newMac)) {
            Log.d(TAG, "No changes in MAC address");
        } else {
            mWifiMetrics.logStaEvent(StaEvent.TYPE_MAC_CHANGE, config);
            boolean setMacSuccess =
                    mWifiNative.setMacAddress(mInterfaceName, newMac);
            if(DBG)
                Log.d(TAG, "ConnectedMacRandomization SSID(" + config.getPrintableSsid()
                        + "). setMacAddress(" + newMac.toString() + ") from "
                        + currentMac.toString() + " = " + setMacSuccess);
        }
    }

    /**
     * Sets the current MAC to the factory MAC address.
     */
    private void setCurrentMacToFactoryMac(WifiConfiguration config) {
        MacAddress factoryMac = mWifiNative.getFactoryMacAddress(mInterfaceName);
        if (factoryMac == null) {
            Log.e(TAG, "Fail to set factory MAC address. Factory MAC is null.");
            return;
        }
        String currentMacStr = mWifiNative.getMacAddress(mInterfaceName);
        if (!TextUtils.equals(currentMacStr, factoryMac.toString())) {
            if (mWifiNative.setMacAddress(mInterfaceName, factoryMac)) {
                mWifiMetrics.logStaEvent(StaEvent.TYPE_MAC_CHANGE, config);
            } else {
                Log.e(TAG, "Failed to set MAC address to " + "'" + factoryMac.toString() + "'");
            }
        }
    }

    /**
     * Helper method to check if Connected MAC Randomization is supported - onDown events are
     * skipped if this feature is enabled (b/72459123).
     *
     * @return boolean true if Connected MAC randomization is supported, false otherwise
     */
    public boolean isConnectedMacRandomizationEnabled() {
        return mConnectedMacRandomzationSupported;
    }

    /**
     * Helper method allowing ClientModeManager to report an error (interface went down) and trigger
     * recovery.
     *
     * @param reason int indicating the SelfRecovery failure type.
     */
    public void failureDetected(int reason) {
        // report a failure
        mWifiInjector.getSelfRecovery().trigger(SelfRecovery.REASON_STA_IFACE_DOWN);

        if (mIssueDetector != null) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
            mIssueDetector.captureBugReport(ReportIdKey.ID_HIDL_PROBLEM,
                    ReportUtil.getReportDataForHidlDeath(SelfRecovery.REASON_STA_IFACE_DOWN));
        }
    }

    /********************************************************
     * HSM states
     *******************************************************/

    class DefaultState extends State {

        @Override
        public boolean processMessage(Message message) {
            boolean handleStatus = HANDLED;

            switch (message.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED: {
                    AsyncChannel ac = (AsyncChannel) message.obj;
                    if (ac == mWifiP2pChannel) {
                        if (message.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                            p2pSendMessage(AsyncChannel.CMD_CHANNEL_FULL_CONNECTION);
                        } else {
                            // TODO: We should probably do some cleanup or attempt a retry
                            // b/34283611
                            loge("WifiP2pService connection failure, error=" + message.arg1);
                        }
                    } else {
                        loge("got HALF_CONNECTED for unknown channel");
                    }
                    break;
                }
                case AsyncChannel.CMD_CHANNEL_DISCONNECTED: {
                    AsyncChannel ac = (AsyncChannel) message.obj;
                    if (ac == mWifiP2pChannel) {
                        loge("WifiP2pService channel lost, message.arg1 =" + message.arg1);
                        //TODO: Re-establish connection to state machine after a delay (b/34283611)
                        // mWifiP2pChannel.connect(mContext, getHandler(),
                        // mWifiP2pManager.getMessenger());
                    }
                    break;
                }
                case CMD_BLUETOOTH_ADAPTER_STATE_CHANGE:
                    mBluetoothConnectionActive =
                            (message.arg1 != BluetoothAdapter.STATE_DISCONNECTED);
                    if (mWifiConnectivityManager != null) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_NETWORK_SELECTOR
                        mWifiConnectivityManager.setBluetoothConnected(mBluetoothConnectionActive);
                    }
                    mBigDataManager.addOrUpdateValue( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                            WifiBigDataLogManager.LOGGING_TYPE_BLUETOOTH_CONNECTION,
                            mBluetoothConnectionActive ? 1: 0);
                    break;
                case CMD_ENABLE_NETWORK:
                    boolean disableOthers = message.arg2 == 1;
                    int netId = message.arg1;
                    boolean ok = mWifiConfigManager.enableNetwork(
                            netId, disableOthers, message.sendingUid);
                    if (!ok) {
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    replyToMessage(message, message.what, ok ? SUCCESS : FAILURE);
                    break;
                //+SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS
                case CMD_FORCINGLY_ENABLE_ALL_NETWORKS:
                    mWifiConfigManager.forcinglyEnableAllNetworks(message.sendingUid);
                    break;
                //-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS
                case CMD_ADD_OR_UPDATE_NETWORK:
                    int from = message.arg1; //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
                    WifiConfiguration config = (WifiConfiguration) message.obj;
                    if (config.networkId == -1) { //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS for add network from 3rd party app
                        config.priority = mWifiConfigManager.increaseAndGetPriority();
                    }
                    mWifiConfigManager.updateBssidWhitelist(config, mScanRequestProxy.getScanResults()); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
                    NetworkUpdateResult result = //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
                            mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid, from, null);
                    if (!result.isSuccess()) {
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    replyToMessage(message, message.what, result.getNetworkId());
                    break;
                case CMD_REMOVE_NETWORK:
                    deleteNetworkConfigAndSendReply(message, false);
                    break;
                case CMD_GET_CONFIGURED_NETWORKS:
                    //+SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM - semGetConfiguredNetworks
                    // in case of MDM need to return all networks (including those created by Network Suggestion)
                    if (WifiManager.CALLED_FROM_MDM == ((Integer)message.obj).intValue()) {
                        replyToMessage(message, message.what,
                                mWifiConfigManager.getConfiguredNetworks());
                    //-SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM - semGetConfiguredNetworks
                    } else {
                        replyToMessage(message, message.what,
                                mWifiConfigManager.getSavedNetworks(message.arg2));
                    }
                    break;
                case CMD_GET_A_CONFIGURED_NETWORK: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                    replyToMessage(message, message.what,
                            mWifiConfigManager.getConfiguredNetwork(message.arg1));
                    break;
                case CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS:
                    replyToMessage(message, message.what,
                            mWifiConfigManager.getConfiguredNetworksWithPasswords());
                    break;
                case CMD_ENABLE_RSSI_POLL:
                    mEnableRssiPolling = (message.arg1 == 1);
                    break;
                case CMD_SET_HIGH_PERF_MODE:
                    if (message.arg1 == 1) {
                        setSuspendOptimizations(SUSPEND_DUE_TO_HIGH_PERF, false);
                    } else {
                        setSuspendOptimizations(SUSPEND_DUE_TO_HIGH_PERF, true);
                    }
                    break;
                case CMD_INITIALIZE:
                    ok = mWifiNative.initialize();
                    initializeWifiChipInfo(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CHIP_INFO
                    replyToMessage(message, message.what, ok ? SUCCESS : FAILURE);
                    break;
                case CMD_BOOT_COMPLETED:
                    mIsBootCompleted = true;
                    initializeWifiChipInfo(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CHIP_INFO
                    // get other services that we need to manage
                    getAdditionalWifiServiceInterfaces();
                    new MemoryStoreImpl(mContext, mWifiInjector, mWifiScoreCard).start();
                    if (!mWifiConfigManager.loadFromStore(false)) {
                        Log.e(TAG, "Failed to load from config store, retry later");
                        //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONFIG_MANAGER
                        sendMessageDelayed(CMD_RELOAD_CONFIG_STORE_FILE, 1, 0, 1000L);
                    }
                    registerNetworkFactory();
                    resetFwLogFolder(); //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
                    mWifiConfigManager.forcinglyEnableAllNetworks(Process.SYSTEM_UID); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS
                    break;
                case CMD_RELOAD_CONFIG_STORE_FILE: //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONFIG_MANAGER
                    if (message.arg1 <= 3 && !mWifiConfigManager.loadFromStore(message.arg1 == 3)) {
                        Log.e(TAG, "Failed to load from config store, retry " + message.arg1);
                        sendMessageDelayed(CMD_RELOAD_CONFIG_STORE_FILE,
                                message.arg1 + 1, 0, 3000L);
                    } else {
                        mWifiConfigManager.forcinglyEnableAllNetworks(Process.SYSTEM_UID); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS
                    }
                    break;
                case CMD_SCREEN_STATE_CHANGED:
                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
                    boolean screenOn = (message.arg1 != 0);
                    if (mScreenOn != screenOn) {
                        handleScreenStateChanged(screenOn);
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
                    break;
                case CMD_DISCONNECT:
                case CMD_RECONNECT:
                case CMD_REASSOCIATE:
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                case CMD_RSSI_POLL:
                case CMD_ONESHOT_RSSI_POLL:
                case CMD_PRE_DHCP_ACTION:
                case CMD_PRE_DHCP_ACTION_COMPLETE:
                case CMD_POST_DHCP_ACTION:
                case WifiMonitor.SUP_REQUEST_IDENTITY:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH:
                case CMD_TARGET_BSSID:
                case CMD_START_CONNECT:
                case CMD_START_ROAM:
                case CMD_ASSOCIATED_BSSID:
                case CMD_UNWANTED_NETWORK:
                case CMD_DISCONNECTING_WATCHDOG_TIMER:
                case CMD_ROAM_WATCHDOG_TIMER:
                case CMD_DISABLE_EPHEMERAL_NETWORK:
                case CMD_THREE_TIMES_SCAN_IN_IDLE: //SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE
                case CMD_SCAN_RESULT_AVAILABLE: //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                case WifiMonitor.BSSID_PRUNED_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
                case CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER: //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
                case CMD_CHECK_ARP_RESULT: //SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP
                case CMD_SEND_ARP: //SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP
                case CMD_REPLACE_PUBLIC_DNS: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_NETWORK_DIAGNOSTICS
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_SET_OPERATIONAL_MODE:
                    // using the CMD_SET_OPERATIONAL_MODE (sent at front of queue) to trigger the
                    // state transitions performed in setOperationalMode.
                    break;
                case CMD_SET_SUSPEND_OPT_ENABLED:
                    if (message.arg1 == 1) {
                        if (message.arg2 == 1) {
                            mSuspendWakeLock.release();
                        }
                        setSuspendOptimizations(SUSPEND_DUE_TO_SCREEN, true);
                    } else {
                        setSuspendOptimizations(SUSPEND_DUE_TO_SCREEN, false);
                    }
                    break;
                case WifiManager.CONNECT_NETWORK:
                    replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                            WifiManager.BUSY);
                    break;
                case WifiManager.FORGET_NETWORK:
                    deleteNetworkConfigAndSendReply(message, true);
                    break;
                case WifiManager.SAVE_NETWORK:
                    saveNetworkConfigAndSendReply(message);
                    break;
                case WifiManager.DISABLE_NETWORK:
                    replyToMessage(message, WifiManager.DISABLE_NETWORK_FAILED,
                            WifiManager.BUSY);
                    break;
                case WifiManager.RSSI_PKTCNT_FETCH:
                    replyToMessage(message, WifiManager.RSSI_PKTCNT_FETCH_FAILED,
                            WifiManager.BUSY);
                    break;
                case CMD_GET_SUPPORTED_FEATURES:
                    long featureSet = (mWifiNative.getSupportedFeatureSet(mInterfaceName));
                    replyToMessage(message, message.what, Long.valueOf(featureSet));
                    break;
                case CMD_GET_LINK_LAYER_STATS:
                    // Not supported hence reply with error message
                    replyToMessage(message, message.what, null);
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED:
                    NetworkInfo info = (NetworkInfo) message.obj;
                    mP2pConnected.set(info.isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST:
                    mTemporarilyDisconnectWifi = (message.arg1 == 1);
                    replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                case WifiP2pServiceImpl.DISABLE_P2P_RSP:
                    if(mP2pDisableListener != null){
                        Log.d(TAG,"DISABLE_P2P_RSP mP2pDisableListener == " +mP2pDisableListener);
                        mP2pDisableListener.onDisable();
                        mP2pDisableListener = null;
                    }
                    break;
                /* Link configuration (IP address, DNS, ...) changes notified via netlink */
                case CMD_UPDATE_LINKPROPERTIES:
                    updateLinkProperties((LinkProperties) message.obj);
                    break;
                case CMD_GET_MATCHING_OSU_PROVIDERS:
                    replyToMessage(message, message.what, new HashMap<>());
                    break;
                case CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS:
                    replyToMessage(message, message.what,
                            new HashMap<OsuProvider, PasspointConfiguration>());
                    break;
                case CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES:
                    replyToMessage(message, message.what, new ArrayList<>());
                    break;
                case CMD_START_SUBSCRIPTION_PROVISIONING:
                case CMD_UPDATE_CONFIG_LOCATION: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
                    replyToMessage(message, message.what, 0);
                    break;
                case CMD_IP_CONFIGURATION_SUCCESSFUL:
                case CMD_IP_CONFIGURATION_LOST:
                case CMD_IP_REACHABILITY_LOST:
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_REMOVE_APP_CONFIGURATIONS:
                    deferMessage(message);
                    break;
                case CMD_REMOVE_USER_CONFIGURATIONS:
                    deferMessage(message);
                    break;
                case CMD_START_IP_PACKET_OFFLOAD:
                    /* fall-through */
                case CMD_STOP_IP_PACKET_OFFLOAD:
                case CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF:
                case CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF:
                    if (mNetworkAgent != null) {
                        mNetworkAgent.onSocketKeepaliveEvent(message.arg1,
                                SocketKeepalive.ERROR_INVALID_NETWORK);
                    }
                    break;
                case CMD_START_RSSI_MONITORING_OFFLOAD:
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_STOP_RSSI_MONITORING_OFFLOAD:
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_USER_SWITCH:
                    Set<Integer> removedNetworkIds =
                            mWifiConfigManager.handleUserSwitch(message.arg1);
                    if (removedNetworkIds.contains(mTargetNetworkId)
                            || removedNetworkIds.contains(mLastNetworkId)) {
                        // Disconnect and let autojoin reselect a new network
                        disconnectCommand(0, DISCONNECT_REASON_USER_SWITCH); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        //sendMessage(CMD_DISCONNECT);
                    }
                    break;
                case CMD_USER_UNLOCK:
                    mWifiConfigManager.handleUserUnlock(message.arg1);
                    break;
                case CMD_USER_STOP:
                    mWifiConfigManager.handleUserStop(message.arg1);
                    break;
                case CMD_QUERY_OSU_ICON:
                case CMD_MATCH_PROVIDER_NETWORK:
                    /* reply with arg1 = 0 - it returns API failure to the calling app
                     * (message.what is not looked at)
                     */
                    replyToMessage(message, message.what);
                    break;
                case CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG:
                    Bundle bundle = (Bundle) message.obj;
                    int addResult = mPasspointManager.addOrUpdateProvider(bundle.getParcelable(
                            EXTRA_PASSPOINT_CONFIGURATION),
                            bundle.getInt(EXTRA_UID),
                            bundle.getString(EXTRA_PACKAGE_NAME))
                            ? SUCCESS : FAILURE;
                    replyToMessage(message, message.what, addResult);
                    break;
                case CMD_REMOVE_PASSPOINT_CONFIG:
                    String fqdn = (String) message.obj;
                    int removeResult = mPasspointManager.removeProvider(
                            (String) message.obj) ? SUCCESS : FAILURE;

                    // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20 - start
                    List<WifiConfiguration> savedNetworks = mWifiConfigManager.getConfiguredNetworks();
                    for (WifiConfiguration network : savedNetworks) {
                        if (!network.isPasspoint()) {
                            continue;
                        }
                        if (fqdn.equals(network.FQDN)) {
                            mWifiConfigManager.removeNetwork(network.networkId, network.creatorUid);
                            break;
                        }
                    }
                    // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20 - End

                    replyToMessage(message, message.what, removeResult);
                    break;
                case CMD_GET_PASSPOINT_CONFIGS:
                    replyToMessage(message, message.what, mPasspointManager.getProviderConfigs());
                    break;
                case CMD_RESET_SIM_NETWORKS:
                    /* Defer this message until supplicant is started. */
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DEFERRED;
                    deferMessage(message);
                    break;
                case CMD_INSTALL_PACKET_FILTER:
                    mWifiNative.installPacketFilter(mInterfaceName, (byte[]) message.obj);
                    break;
                case CMD_READ_PACKET_FILTER:
                    byte[] data = mWifiNative.readPacketFilter(mInterfaceName);
                    if (mIpClient != null) {
                        mIpClient.readPacketFilterComplete(data);
                    }
                    break;
                case CMD_SET_FALLBACK_PACKET_FILTERING:
                    if ((boolean) message.obj) {
                        mWifiNative.startFilteringMulticastV4Packets(mInterfaceName);
                    } else {
                        mWifiNative.stopFilteringMulticastV4Packets(mInterfaceName);
                    }
                    break;
                case CMD_DIAGS_CONNECT_TIMEOUT:
                    mWifiDiagnostics.reportConnectionEvent(
                            BaseWifiDiagnostics.CONNECTION_EVENT_TIMEOUT);
                    break;
                case CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS:
                    replyToMessage(message, message.what, new HashMap<>());
                    break;
                case CMD_SEC_API:
                    Log.d(TAG, "DefaultState::Handling CMD_SEC_API");
                    replyToMessage(message, message.what, -1);
                    break;
                case CMD_SEC_STRING_API:
                    String stringResult = processMessageOnDefaultStateForCallSECStringApi(message);
                    replyToMessage(message, message.what, stringResult);
                    break;
                case CMD_IMS_CALL_ESTABLISHED: //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
                    if (mIsImsCallEstablished != (message.arg1 == 1)) {
                        mIsImsCallEstablished = (message.arg1 == 1);
                        if (mWifiConnectivityManager != null) {
                            mWifiConnectivityManager.changeMaxPeriodicScanMode(mIsImsCallEstablished ?
                                    WifiConnectivityManager.MAX_PERIODIC_SCAN_WAKEUP_TIMER :
                                    WifiConnectivityManager.MAX_PERIODIC_SCAN_NON_WAKEUP_TIMER);
                        }
                    // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SET_LATENCY_CRITICAL
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SET_LATENCY_CRITICAL) {
                        mWifiNative.setLatencyCritical(mInterfaceName, message.arg1);
                        Log.d(TAG, "mIsImsCallEstablished Call LATENCY CMD- " + message.arg1);
                    }
                    // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SET_LATENCY_CRITICAL
                    }
                    break;
                case CMD_AUTO_CONNECT_CARRIER_AP_ENABLED: //SEC_PRODUCT_FEATURE_WLAN_AUTO_CONNECT_CARRIER_AP
                    mWifiConfigManager.setAutoConnectCarrierApEnabled(message.arg1 == 1);
                    break;
                case CMD_SEC_LOGGING: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                case WifiMonitor.SUP_BIGDATA_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    Bundle args = (Bundle) message.obj;
                    String feature = null;
                    if(args != null) {
                        feature = args.getString("feature", null);
                    }
                    if (mIsShutdown) {
                        Log.d(TAG, "shutdowning device");
                    } else {
                        if (!mIsBootCompleted) {
                            sendMessageDelayed(obtainMessage(WifiMonitor.SUP_BIGDATA_EVENT, 0, 0, message.obj), 20000);
                            break;
                        }
                        if (feature != null) {
                            if (WifiBigDataLogManager.ENABLE_SURVEY_MODE) {
                                mBigDataManager.insertLog(args);
                            } else if (DBG) {
                                Log.e(TAG, "survey mode is disabled");
                            }

                            //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            if (WifiBigDataLogManager.FEATURE_DISC.equals(feature)) {
                                String dataString = args.getString("data", null);
                                if (dataString != null) {
                                    report(ReportIdKey.ID_BIGDATA_DISC,
                                            ReportUtil.getReportDataFromBigDataParamsOfDISC(
                                                dataString, mConnectedApInternalType,
                                                mBigDataManager.getAndResetLastInternalReason(), mLastConnectedNetworkId));
                                }
                            } else if (WifiBigDataLogManager.FEATURE_ON_OFF.equals(feature)) {
                                String dataString = args.getString("data", null);
                                if (dataString != null) {
                                    report(ReportIdKey.ID_BIGDATA_WIFI_ON_OFF,
                                            ReportUtil.getReportDataFromBigDataParamsOfONOF(dataString));
                                }
                            } else if (WifiBigDataLogManager.FEATURE_ASSOC.equals(feature)) {
                                String dataString = args.getString("data", null);
                                if (dataString != null) {
                                    report(ReportIdKey.ID_BIGDATA_ASSOC_REJECT,
                                            ReportUtil.getReportDataFromBigDataParamsOfASSO(dataString, mLastConnectedNetworkId));
                                }
                            } else if (WifiBigDataLogManager.FEATURE_ISSUE_DETECTOR_DISC1.equals(feature)) {
                                int categoryId = args.getInt("categoryId", 0);
                                if (categoryId == 1) {
                                    //sendMessage(CMD_REQUEST_FW_BIGDATA_PARAM, 2, 0);
                                }
                            } else if (WifiBigDataLogManager.FEATURE_ISSUE_DETECTOR_DISC2.equals(feature)) {
                                int categoryId = args.getInt("categoryId", 0);
                                if (categoryId == 1) { //HANG or HAL Fail
                                    sendBroadcastIssueTrackerSysDump(ISSUE_TRACKER_SYSDUMP_HANG);
                                }
                            } else if (WifiBigDataLogManager.FEATURE_HANG.equals(feature)) {
                                increaseCounter(WifiMonitor.DRIVER_HUNG_EVENT);
                            }
                        } else {
                            if (DBG) Log.e(TAG, "CMD_SEC_LOGGING - feature is null");
                        }
                    }
                    break;
                case CMD_24HOURS_PASSED_AFTER_BOOT: { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    String paramData = getWifiParameters(true);
                    Log.i(TAG, "Counter: " + paramData);
                    if (WifiBigDataLogManager.ENABLE_SURVEY_MODE) {
                        Bundle paramArgs = WifiBigDataLogManager.getBigDataBundle(WifiBigDataLogManager.FEATURE_24HR, paramData);
                        sendMessage(obtainMessage(CMD_SEC_LOGGING, 0, 0, paramArgs));
                    }

                    report(ReportIdKey.ID_BIGDATA_W24H,
                            ReportUtil.getReportDatatForW24H(paramData));
                    removeMessages(CMD_24HOURS_PASSED_AFTER_BOOT);
                    sendMessageDelayed(CMD_24HOURS_PASSED_AFTER_BOOT, 24 * 60 * 60 * 1000);
                    break;
                }
                case CMD_SET_ADPS_MODE: //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_SEC_API_ASYNC:
                    if (processMessageOnDefaultStateForCallSECApiAsync(message)) {
                        break;
                    }
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiMonitor.EAP_EVENT_MESSAGE: //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
                    if (message.obj != null) {
                        int eapEvent = message.arg1;
                        processMessageForEap(eapEvent, message.arg2, (String) message.obj);

                        if (eapEvent == EAP_EVENT_DEAUTH_8021X_AUTH_FAILED
                                || eapEvent == EAP_EVENT_EAP_FAILURE) {
                            notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    DISCONNECT_REASON_AUTH_FAIL);
                            if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                if (mTargetWifiConfiguration != null) {
                                    report(ReportIdKey.ID_AUTH_FAIL,
                                            ReportUtil.getReportDataForAuthFailForEap(mTargetNetworkId, eapEvent,
                                                    mTargetWifiConfiguration.status,
                                                    mTargetWifiConfiguration.numAssociation,
                                                    mTargetWifiConfiguration.getNetworkSelectionStatus().getNetworkSelectionStatus(),
                                                    mTargetWifiConfiguration.getNetworkSelectionStatus().getNetworkSelectionDisableReason()));
                                }
                            }
                        }
                    }
                    break;
                case CMD_SHOW_TOAST_MSG:
                    SemWifiFrameworkUxUtils.showToast(mContext, message.arg1, (String) message.obj);
                    break;
                case 0:
                    // We want to notice any empty messages (with what == 0) that might crop up.
                    // For example, we may have recycled a message sent to multiple handlers.
                    Log.wtf(TAG, "Error! empty message encountered");
                    break;
                default:
                    loge("Error! unhandled message" + message);
                    break;
            }

            if (handleStatus == HANDLED) {
                logStateAndMessage(message, this);
            }

            return handleStatus;
        }
    }

    /**
     * Helper method to start other services and get state ready for client mode
     */
    private void setupClientMode() {
        Log.d(TAG, "setupClientMode() ifacename = " + mInterfaceName);

        setHighPerfModeEnabled(false);

        mWifiStateTracker.updateState(WifiStateTracker.INVALID);

        mIpClientCallbacks = new IpClientCallbacksImpl();
        mFacade.makeIpClient(mContext, mInterfaceName, mIpClientCallbacks);
        if (!mIpClientCallbacks.awaitCreation()) {
            loge("Timeout waiting for IpClient");
        }

        setMulticastFilter(true);
        registerForWifiMonitorEvents();
        mWifiInjector.getWifiLastResortWatchdog().clearAllFailureCounts();
        setSupplicantLogLevel();

        // reset state related to supplicant starting
        mSupplicantStateTracker.sendMessage(CMD_RESET_SUPPLICANT_STATE);
        // Initialize data structures
        mLastBssid = null;
        mLastNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
        mLastSignalLevel = -1;
        mWifiInfo.setMacAddress(mWifiNative.getMacAddress(mInterfaceName));
        // TODO: b/79504296 This broadcast has been deprecated and should be removed
        sendSupplicantConnectionChangedBroadcast(true);

        mScanResultsEventCounter = 0; // SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE

        mWifiNative.setExternalSim(mInterfaceName, true);

        setRandomMacOui();
        mCountryCode.setReadyForChangeAndUpdate(true);//SEC_PRODUCT_FEATURE_WLAN_COUNTRY_CODE

        //+SEC_PRODUCT_FEATURE_WLAN_COUNTRY_CODE
        boolean isAirplaneModeEnabled = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) == 1;
        if (isAirplaneModeEnabled) {
            Log.e(TAG, "SupplicantStarted - enter() isAirplaneModeEnabled !!  ");
            mCountryCode.setCountryCodeNative("CSC", true);
        }
        //-SEC_PRODUCT_FEATURE_WLAN_COUNTRY_CODE

        mWifiDiagnostics.startLogging(mVerboseLoggingEnabled);
        mIsRunning = true;
        updateBatteryWorkSource(null);

        /**
         * Enable bluetooth coexistence scan mode when bluetooth connection is active.
         * When this mode is on, some of the low-level scan parameters used by the
         * driver are changed to reduce interference with bluetooth
         */
        mWifiNative.setBluetoothCoexistenceScanMode(mInterfaceName, mBluetoothConnectionActive);

        // initialize network state
        setNetworkDetailedState(DetailedState.DISCONNECTED);

        // Disable legacy multicast filtering, which on some chipsets defaults to enabled.
        // Legacy IPv6 multicast filtering blocks ICMPv6 router advertisements which breaks IPv6
        // provisioning. Legacy IPv4 multicast filtering may be re-enabled later via
        // IpClient.Callback.setFallbackMulticastFilter()
        mWifiNative.stopFilteringMulticastV4Packets(mInterfaceName);
        mWifiNative.stopFilteringMulticastV6Packets(mInterfaceName);

        // Set the right suspend mode settings
        mWifiNative.setSuspendOptimizations(mInterfaceName, mSuspendOptNeedsDisabled == 0
                && mUserWantsSuspendOpt.get());

        setPowerSave(true);

        // Disable wpa_supplicant from auto reconnecting.
        mWifiNative.enableStaAutoReconnect(mInterfaceName, false);
        // STA has higher priority over P2P
        mWifiNative.setConcurrencyPriority(true);

        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                && !isWifiOnly()) {
            if (mUnstableApController == null) {
                mUnstableApController = new UnstableApController(
                        new UnstableApController.UnstableApAdapter() {
                            @Override
                            public void addToBlackList(String bssid) {
                                mWifiConnectivityManager.trackBssid(bssid, false,
                                        WifiConnectivityManager.REASON_CODE_AP_UNABLE_TO_HANDLE_NEW_STA);
                            }

                            @Override
                            public void updateUnstableApNetwork(int networkId, int reason) {
                                if (reason == WifiConfiguration.NetworkSelectionStatus.DISABLED_ASSOCIATION_REJECTION) {
                                    //disabled network with assoication reject reason
                                    for (int i = 0; i < 5; i++) {
                                        mWifiConfigManager.updateNetworkSelectionStatus(networkId, reason);
                                    }
                                } else {
                                    mWifiConfigManager.updateNetworkSelectionStatus(networkId, reason);
                                }
                            }

                            @Override
                            public void enableNetwork(int networkId) {
                                mWifiConfigManager.enableNetwork(networkId, false, Process.SYSTEM_UID);
                            }

                            @Override
                            public WifiConfiguration getNetwork(int networkId) {
                                return mWifiConfigManager.getConfiguredNetwork(networkId);
                            }
                        });
            }
            mUnstableApController.clearAll();
            mUnstableApController.setSimCardState(
                    TelephonyUtil.isSimCardReady(getTelephonyManager()));
        }

        if (Vendor.VZW == mOpBranding) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
            if (mSemWifiHiddenNetworkTracker == null) {
                mSemWifiHiddenNetworkTracker = new SemWifiHiddenNetworkTracker(mContext,
                        new SemWifiHiddenNetworkTracker.WifiHiddenNetworkAdapter() {
                            @Override
                            public List<ScanResult> getScanResults() {
                                final List<ScanResult> scanResults = new ArrayList<>();
                                scanResults.addAll(mScanRequestProxy.getScanResults());
                                return scanResults;
                            }
                        });
            }
        }

        if (ENABLE_SUPPORT_ADPS) { //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
            updateAdpsState();
            sendMessage(CMD_SET_ADPS_MODE);
        }

        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_TACTICAL_MODE) {
            mWifiNative.setDtimInSuspend(mInterfaceName, 1); // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SET_DTIM_IN_SUSPEND
            mWifiNative.setMaxDtimInSuspend(mInterfaceName, false); // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SET_MAX_DTIM_IN_SUSPEND
        }
    }

    /**
     * Helper method to stop external services and clean up state from client mode.
     */
    private void stopClientMode() {
        // exiting supplicant started state is now only applicable to client mode
        mWifiDiagnostics.stopLogging();

        mIsRunning = false;
        updateBatteryWorkSource(null);

        if (mIpClient != null && mIpClient.shutdown()) {
            // Block to make sure IpClient has really shut down, lest cleanup
            // race with, say, bringup code over in tethering.
            mIpClientCallbacks.awaitShutdown();
        }
        mNetworkInfo.setIsAvailable(false);
        if (mNetworkAgent != null) mNetworkAgent.sendNetworkInfo(mNetworkInfo);
        mCountryCode.setReadyForChange(false);
        mInterfaceName = null;
        // TODO: b/79504296 This broadcast has been deprecated and should be removed
        sendSupplicantConnectionChangedBroadcast(false);

        // Let's remove any ephemeral or passpoint networks.
        mWifiConfigManager.removeAllEphemeralOrPasspointConfiguredNetworks();
    }

    void registerConnected() {
        if (mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
            mWifiConfigManager.updateNetworkAfterConnect(mLastNetworkId);
            // Notify PasspointManager of Passpoint network connected event.
            WifiConfiguration currentNetwork = getCurrentWifiConfiguration();
            if (currentNetwork != null && currentNetwork.isPasspoint()) {
                mPasspointManager.onPasspointNetworkConnected(currentNetwork.FQDN);
            }
        }
    }

    void registerDisconnected() {
        if (mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
            mWifiConfigManager.updateNetworkAfterDisconnect(mLastNetworkId);
        }
    }

    /**
     * Returns WifiConfiguration object corresponding to the currently connected network, null if
     * not connected.
     */
    public WifiConfiguration getCurrentWifiConfiguration() {
        if (mLastNetworkId == WifiConfiguration.INVALID_NETWORK_ID) {
            return null;
        }
        return mWifiConfigManager.getConfiguredNetwork(mLastNetworkId);
    }

    private WifiConfiguration getTargetWifiConfiguration() {
        if (mTargetNetworkId == WifiConfiguration.INVALID_NETWORK_ID) {
            return null;
        }
        return mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
    }

    ScanResult getCurrentScanResult() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        String bssid = mWifiInfo.getBSSID();
        if (bssid == null) {
            bssid = mTargetRoamBSSID;
        }
        ScanDetailCache scanDetailCache =
                mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);

        if (scanDetailCache == null) {
            return null;
        }

        return scanDetailCache.getScanResult(bssid);
    }

    String getCurrentBSSID() {
        return mLastBssid;
    }

    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
    private PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            Log.d(TAG, "onDataConnectionStateChanged: state =" + String.valueOf(state)
                    + ", networkType =" + TelephonyManager.getNetworkTypeName(networkType));
            handleCellularCapabilities();
        }

        @Override
        public void onUserMobileDataStateChanged(boolean enabled) {
            Log.d(TAG, "onUserMobileDataStateChanged: enabled=" + enabled);
            handleCellularCapabilities();
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            byte newLevel = (byte) signalStrength.getLevel();
            if (mCellularSignalLevel == newLevel)
                return;
            mCellularSignalLevel = newLevel;
            mChanged = true;
            Log.d(TAG, "onSignalStrengthsChanged: mCellularSignalLevel=" + mCellularSignalLevel);
            handleCellularCapabilities();
        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            Log.d(TAG, "onCellLocationChanged: CellLocation=" + location);
            byte [] curCellularCellId = new byte[2];

            if (location instanceof GsmCellLocation) {
                GsmCellLocation loc = (GsmCellLocation)location;
                int cid = loc.getCid();
                curCellularCellId = toBytes(cid);
            } else if (location instanceof CdmaCellLocation) {
                CdmaCellLocation loc = (CdmaCellLocation)location;
                int bid = loc.getBaseStationId();
                curCellularCellId = toBytes(bid);
            } else {
                Log.d(TAG, "unknown location.");
                mCellularSignalLevel = (byte)0;
            }

            if (Arrays.equals(mCellularCellId, curCellularCellId))
                return;
            else {
                System.arraycopy(curCellularCellId, 0, mCellularCellId, 0, 2);
                mChanged = true;
                handleCellularCapabilities();
            }
        }

        @Override
        public void onCarrierNetworkChange(boolean active) {
            Log.d(TAG, "onCarrierNetworkChange: active=" + active);
            handleCellularCapabilities();
        }
    };

    private void handleCellularCapabilities() {
        handleCellularCapabilities(false);
    }

    private void handleCellularCapabilities(boolean bForce) {
        byte curNetworkType = WifiNative.MBO_TYPE_NETWORK_CLASS_UNKNOWN;
        byte curCellularCapaState = WifiNative.MBO_STATE_CELLULAR_DATA_UNAVAILABLE;

        if (isWifiOnly()) {
            mCellularCapaState = WifiNative.MBO_STATE_NO_CELLULAR_DATA;
            mNetworktype = WifiNative.MBO_TYPE_NETWORK_CLASS_UNKNOWN;
            mCellularSignalLevel = 0;
            if (mWifiState.get() == WifiManager.WIFI_STATE_ENABLED)
                mWifiNative.updateCellularCapabilities(mInterfaceName, mCellularCapaState, mNetworktype
                                                      , mCellularSignalLevel, mCellularCellId);
            return;
        }
        try {
            TelephonyManager telephonyManager = getTelephonyManager();
            boolean isNetworkRoaming = telephonyManager.isNetworkRoaming();
            boolean isDataRoamingEnabled = Settings.Global.getInt(mContext.getContentResolver(),
                            Settings.Global.DATA_ROAMING, 0) != 0;
            boolean isDataEnabled = telephonyManager.getDataEnabled();
            int simCardState = telephonyManager.getSimCardState();

            if (simCardState == TelephonyManager.SIM_STATE_PRESENT) {
                curNetworkType = (byte) telephonyManager.getNetworkClass(telephonyManager.getNetworkType());
                if (isNetworkRoaming) {
                    if ( isDataRoamingEnabled && !mIsPolicyMobileData && isDataEnabled
                        && curNetworkType != TelephonyManager.NETWORK_CLASS_UNKNOWN) {
                        curCellularCapaState = WifiNative.MBO_STATE_CELLULAR_DATA_AVAILABLE;
                    }
                }
                else {
                    if ( isDataEnabled && !mIsPolicyMobileData
                        && curNetworkType != TelephonyManager.NETWORK_CLASS_UNKNOWN) {
                        curCellularCapaState = WifiNative.MBO_STATE_CELLULAR_DATA_AVAILABLE;
                    }
                }
            } else {
                Arrays.fill(mCellularCellId, (byte)0);
                mCellularSignalLevel = 0;
            }

            if (bForce && curNetworkType != WifiNative.MBO_TYPE_NETWORK_CLASS_UNKNOWN) {
                List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
                if (cellInfoList != null)
                {
                    for (CellInfo cellInfo : cellInfoList) {
                        Log.d(TAG, "isRegistered " + cellInfo.isRegistered());
                        if (cellInfo.isRegistered()) {
                            mCellularSignalLevel = (byte) getCellLevel(cellInfo);
                            mCellularCellId = getCellId(cellInfo);
                            break;
                        }
                    }
                } else {
                    Log.d(TAG, "cellInfoList is null.");
                }
            }

            if(bForce) {
                mNetworktype = curNetworkType;
                mCellularCapaState = curCellularCapaState;

                if (mWifiState.get() == WifiManager.WIFI_STATE_ENABLED)
                    mWifiNative.updateCellularCapabilities(mInterfaceName, mCellularCapaState, mNetworktype
                                                          , mCellularSignalLevel, mCellularCellId);
                mChanged = false;
            } else {
                if(curNetworkType == mNetworktype && curCellularCapaState == mCellularCapaState
                    && !mChanged) {
                    Log.d(TAG, "handleCellularCapabilities duplicated values...so skip.");
                }
                else if(simCardState != TelephonyManager.SIM_STATE_PRESENT
                          && curCellularCapaState == mCellularCapaState && !mChanged) {
                    Log.d(TAG, "handleCellularCapabilities sim not present...so skip.");
                } else {
                    mNetworktype = curNetworkType;
                    mCellularCapaState = curCellularCapaState;

                    if (mWifiState.get() == WifiManager.WIFI_STATE_ENABLED)
                        mWifiNative.updateCellularCapabilities(mInterfaceName, mCellularCapaState
                                                              , mNetworktype, mCellularSignalLevel
                                                              , mCellularCellId);
                    mChanged = false;
                }
            }
        } catch (Exception e) {
              Log.e(TAG, "handleCellularCapabilities exception " + e.toString());
        }
    }

    private byte[] toBytes(int i) {
        Log.d(TAG, "toBytes:" + Integer.toHexString(i));
        byte[] result = new byte[2];
        result[0] = (byte) (i >> 8);
        result[1] = (byte) (i /* >> 0 */);
        Log.d(TAG, "toBytes:" + result[0] + "," + result[1]);
        return result;
    }

    private byte[] getCellId(CellInfo cellInfo) {
        int value = 0;
        if (cellInfo instanceof CellInfoLte) {
            value = ((CellInfoLte) cellInfo).getCellIdentity().getCi();
        } else if (cellInfo instanceof CellInfoWcdma) {
            value = ((CellInfoWcdma) cellInfo).getCellIdentity().getCid();
        } else if (cellInfo instanceof CellInfoGsm) {
            value = ((CellInfoGsm) cellInfo).getCellIdentity().getCid();
        } else if (cellInfo instanceof CellInfoCdma) {
            value = ((CellInfoCdma) cellInfo).getCellIdentity().getBasestationId();
        } else {
            Log.e(TAG, "Invalid CellInfo type");
        }
        return toBytes(value);
    }

    private int getCellLevel(CellInfo cellInfo) {
        if (cellInfo instanceof CellInfoLte) {
            return ((CellInfoLte) cellInfo).getCellSignalStrength().getLevel();
        } else if (cellInfo instanceof CellInfoWcdma) {
            return ((CellInfoWcdma) cellInfo).getCellSignalStrength().getLevel();
        } else if (cellInfo instanceof CellInfoGsm) {
            return ((CellInfoGsm) cellInfo).getCellSignalStrength().getLevel();
        } else if (cellInfo instanceof CellInfoCdma) {
            return ((CellInfoCdma) cellInfo).getCellSignalStrength().getLevel();
        } else {
            Log.e(TAG, "Invalid CellInfo type");
            return 0;
        }
    }
    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO

    //+SEMSAR
    private void enable5GSarBackOff() {
        Log.d(TAG, "enable5GSarBackOff()");
        ServiceState serviceState = getTelephonyManager().getServiceState();
        if (serviceState == null)
            return;
        int nrBearerStatus = serviceState.getNrBearerStatus();
        Log.d(TAG, "serviceState.getNrBearerStatus()=" + nrBearerStatus);
        InputManager im = mContext.getSystemService(InputManager.class);
        if (im.getLidState() != InputManager.SWITCH_STATE_ON) {
            if (nrBearerStatus == ServiceState.NR_5G_BEARER_STATUS_MMW_ALLOCATED
            || nrBearerStatus == ServiceState.NR_5G_BEARER_STATUS_ALLOCATED) {
                mSemSarManager.set5GSarBackOff(nrBearerStatus);
            }
        }
    }

    public void set5GSarBackOff(int mode) {
        Log.d(TAG, "set5GSarBackOff " + mode);
        mSemSarManager.set5GSarBackOff(mode);
    }
    //-SEMSAR

    class ConnectModeState extends State {

        @Override
        public void enter() {
            Log.d(TAG, "entering ConnectModeState: ifaceName = " + mInterfaceName);
            mOperationalMode = CONNECT_MODE;
            setupClientMode();
            if (!mWifiNative.removeAllNetworks(mInterfaceName)) {
                loge("Failed to remove networks on entering connect mode");
            }
            mWifiInfo.reset();
            mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);

            if (CHARSET_CN.equals(CONFIG_CHARSET) || CHARSET_KOR.equals(CONFIG_CHARSET)) { //TAG_CSCFEATURE_WIFI_CONFIGENCODINGCHARSET
                NetworkDetail.clearNonUTF8SsidLists();
            }

            //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI - disable google autowifi
            //mWifiInjector.getWakeupController().reset();

            mNetworkInfo.setIsAvailable(true);
            if (mNetworkAgent != null) mNetworkAgent.sendNetworkInfo(mNetworkInfo);

            // initialize network state
            setNetworkDetailedState(DetailedState.DISCONNECTED);

            if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                if (mWifiConfigManager.getSavedNetworks(Process.WIFI_UID).size() > 0) {
                    mWifiGeofenceManager.startGeofenceThread(mWifiConfigManager.getSavedNetworks(Process.WIFI_UID));
                }
            }

            // Inform WifiConnectivityManager that Wifi is enabled
            mWifiConnectivityManager.setWifiEnabled(true);
            mNetworkFactory.setWifiState(true);
            // Inform metrics that Wifi is Enabled (but not yet connected)
            mWifiMetrics.setWifiState(WifiMetricsProto.WifiLog.WIFI_DISCONNECTED);
            mWifiMetrics.logStaEvent(StaEvent.TYPE_WIFI_ENABLED);
            // Inform sar manager that wifi is Enabled
            mSarManager.setClientWifiState(WifiManager.WIFI_STATE_ENABLED);
            mSemSarManager.setClientWifiState(WifiManager.WIFI_STATE_ENABLED); //SEMSAR
            setConcurrentEnabled(mConcurrentEnabled); //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
            mWifiScoreCard.noteSupplicantStateChanged(mWifiInfo);
            initializeWifiChipInfo(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CHIP_INFO
            setFccChannel(); //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL

            if(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO) {
                //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
                try {
                    if (getTelephonyManager().getSimCardState() == TelephonyManager.SIM_STATE_PRESENT)
                        mPhoneStateEvent |= PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
                    else
                        mPhoneStateEvent &= ~PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
                    getTelephonyManager().listen(mPhoneStateListener, mPhoneStateEvent);
                } catch (Exception e) {
                    Log.e(TAG, "TelephonyManager.listen exception happend : "+ e.toString());
                }
                handleCellularCapabilities(true);
                //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
            }

            mLastEAPFailureCount = 0; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
            enable5GSarBackOff(); //SEMSAR
            getNCHOVersion(); //EDM Wi-Fi Configuration

            //WiFi Safe Mode
            boolean safeModeEnabled = Settings.Global.getInt(mContext.getContentResolver(),
                    Settings.Global.SAFE_WIFI, 0) == 1;
            if (!mWifiNative.setSafeMode(mInterfaceName, safeModeEnabled)) {
                Log.e(TAG, "Failed to set safe Wi-Fi mode");
            }

            if (CSC_WIFI_SUPPORT_VZW_EAP_AKA) {
                mWifiConfigManager.semRemoveUnneccessaryNetworks();
            }
        }

        @Override
        public void exit() {
            mOperationalMode = DISABLED_MODE;
            if (getCurrentState() == mConnectedState) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                mDelayDisconnect.checkAndWait(mNetworkInfo);
            }
            // Let the system know that wifi is not available since we are exiting client mode.
            mNetworkInfo.setIsAvailable(false);
            if (mNetworkAgent != null) mNetworkAgent.sendNetworkInfo(mNetworkInfo);

            // Inform WifiConnectivityManager that Wifi is disabled
            mWifiConnectivityManager.setWifiEnabled(false);
            mNetworkFactory.setWifiState(false);
            // Inform metrics that Wifi is being disabled (Toggled, airplane enabled, etc)
            mWifiMetrics.setWifiState(WifiMetricsProto.WifiLog.WIFI_DISABLED);
            mWifiMetrics.logStaEvent(StaEvent.TYPE_WIFI_DISABLED);
            // Inform scorecard that wifi is being disabled
            mWifiScoreCard.noteWifiDisabled(mWifiInfo);
            // Inform sar manager that wifi is being disabled
            mSarManager.setClientWifiState(WifiManager.WIFI_STATE_DISABLED);
            mSemSarManager.setClientWifiState(WifiManager.WIFI_STATE_DISABLED);
            if (!mHandleIfaceIsUp) { //SEC_PRODUCT_FEATURE_WLAN_ERROR_CODE_REFACTORING
                Log.w(TAG, "mHandleIfaceIsUp is false on exiting connect mode, skip removeAllNetworks");
            } else {
                if (!mWifiNative.removeAllNetworks(mInterfaceName)) {
                    loge("Failed to remove networks on exiting connect mode");
                }
            }
            mWifiInfo.reset();
            mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            mWifiScoreCard.noteSupplicantStateChanged(mWifiInfo);
            stopClientMode();
            setConcurrentEnabled(false); //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE

            if (mWifiGeofenceManager.isSupported()
                    && !isGeofenceUsedByAnotherPackage()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                mWifiGeofenceManager.deinitGeofence();
            }
            mCntRoamingStartSent = 0;
        }

        @Override
        public boolean processMessage(Message message) {
            WifiConfiguration config;
            int netId;
            boolean ok;
            boolean didDisconnect;
            String bssid;
            String ssid;
            NetworkUpdateResult result;
            Set<Integer> removedNetworkIds;
            int reasonCode;
            boolean timedOut;
            boolean handleStatus = HANDLED;

            switch (message.what) {
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                    mWifiDiagnostics.captureBugReportData(
                            WifiDiagnostics.REPORT_REASON_ASSOC_FAILURE);
                    mDidBlackListBSSID = false;
                    bssid = (String) message.obj;
                    timedOut = message.arg1 > 0;
                    reasonCode = message.arg2;
                    Log.d(TAG, "Association Rejection event: bssid=" + bssid + " reason code="
                            + reasonCode + " timedOut=" + Boolean.toString(timedOut));
                    if (bssid == null || TextUtils.isEmpty(bssid)) {
                        // If BSSID is null, use the target roam BSSID
                        bssid = mTargetRoamBSSID;
                    }
                    if (bssid != null) {
                        // If we have a BSSID, tell configStore to black list it
                        mDidBlackListBSSID = mWifiConnectivityManager.trackBssid(bssid, false,
                            reasonCode);
                    }
                    mWifiConfigManager.updateNetworkSelectionStatus(mTargetNetworkId,
                            WifiConfiguration.NetworkSelectionStatus
                            .DISABLED_ASSOCIATION_REJECTION);
                    mWifiConfigManager.setRecentFailureAssociationStatus(mTargetNetworkId,
                            reasonCode);
                    mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                    // If rejection occurred while Metrics is tracking a ConnnectionEvent, end it.
                    reportConnectionAttemptEnd(
                            timedOut
                                    ? WifiMetrics.ConnectionEvent.FAILURE_ASSOCIATION_TIMED_OUT
                                    : WifiMetrics.ConnectionEvent.FAILURE_ASSOCIATION_REJECTION,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE,
                            WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
                    mWifiInjector.getWifiLastResortWatchdog()
                            .noteConnectionFailureAndTriggerIfNeeded(
                                    getTargetSsid(), bssid,
                                    WifiLastResortWatchdog.FAILURE_CODE_ASSOCIATION);

                    notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            DISCONNECT_REASON_ASSOC_REJECTED);
                    if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        config = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
                        if (config != null) {
                            report(ReportIdKey.ID_ASSOC_REJECT,
                                    ReportUtil.getReportDataForAssocReject(mTargetNetworkId, bssid, reasonCode,
                                        config.status,
                                        config.numAssociation,
                                        config.getNetworkSelectionStatus().getNetworkSelectionStatus(),
                                        config.getNetworkSelectionStatus().getNetworkSelectionDisableReason()));
                        }
                    }
                    break;
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                    mWifiDiagnostics.captureBugReportData(
                            WifiDiagnostics.REPORT_REASON_AUTH_FAILURE);
                    mSupplicantStateTracker.sendMessage(WifiMonitor.AUTHENTICATION_FAILURE_EVENT);
                    int disableReason = WifiConfiguration.NetworkSelectionStatus
                            .DISABLED_AUTHENTICATION_FAILURE;
                    reasonCode = message.arg1;
                    WifiConfiguration targetedNetwork =
                            mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_RETRY_POPUP
                    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_RETRY_POPUP
                    if (targetedNetwork != null
                            && WifiManager.ERROR_AUTH_FAILURE_WRONG_PSWD == reasonCode) {
                        bssid = (String) message.obj;
                        boolean isSameNetwork = true;
                        ScanDetailCache scanDetailCache =
                                mWifiConfigManager.getScanDetailCacheForNetwork(targetedNetwork.networkId);
                        if (bssid != null && scanDetailCache != null) {
                            ScanResult scanResult = scanDetailCache.getScanResult(bssid);
                            if (scanResult == null) {
                                Log.i(TAG, "authentication failure, but not for target network");
                                isSameNetwork = false;
                            }
                        }
                        if (isSameNetwork) {
                            if (targetedNetwork.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)
                                    || targetedNetwork.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.SAE)
                                    || targetedNetwork.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA2_PSK)
                                    || targetedNetwork.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WAPI_PSK)
                                    || (targetedNetwork.allowedKeyManagement.get(KeyMgmt.NONE) && targetedNetwork.wepKeys[0] != null)) {
                                disableReason = WifiConfiguration.NetworkSelectionStatus
                                        .DISABLED_BY_WRONG_PASSWORD;
                            } else if (isPermanentWrongPasswordFailure(targetedNetwork, reasonCode)) {
                                // Check if this is a permanent wrong password failure.
                                disableReason = WifiConfiguration.NetworkSelectionStatus
                                        .DISABLED_BY_WRONG_PASSWORD;
                                /*if (targetedNetwork != null) {
                                    mWrongPasswordNotifier.onWrongPasswordError(
                                            targetedNetwork.SSID);
                                }*/
                            }
                        }
                    }
                    if (reasonCode == WifiManager.ERROR_AUTH_FAILURE_EAP_FAILURE) {
                        int errorCode = message.arg2;
                        handleEapAuthFailure(mTargetNetworkId, errorCode);
                        if (errorCode == WifiNative.EAP_SIM_NOT_SUBSCRIBED) {
                            disableReason = WifiConfiguration.NetworkSelectionStatus
                                .DISABLED_AUTHENTICATION_NO_SUBSCRIPTION;
                        }
                    }
                    if (targetedNetwork != null && TelephonyUtil.isSimEapMethod(targetedNetwork.enterpriseConfig.getEapMethod())
                        && !TextUtils.isEmpty(targetedNetwork.enterpriseConfig.getAnonymousIdentity())) { //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        log("EAP Pseudonym reset due to AUTHENTICATION_FAILURE");
                        targetedNetwork.enterpriseConfig.setAnonymousIdentity(null);
                        mWifiConfigManager.addOrUpdateNetwork(targetedNetwork, Process.WIFI_UID);
                    }
                    mWifiConfigManager.updateNetworkSelectionStatus(
                            mTargetNetworkId, disableReason);
                    mWifiConfigManager.clearRecentFailureReason(mTargetNetworkId);

                    //If failure occurred while Metrics is tracking a ConnnectionEvent, end it.
                    int level2FailureReason;
                    switch (reasonCode) {
                        case WifiManager.ERROR_AUTH_FAILURE_NONE:
                            level2FailureReason =
                                    WifiMetricsProto.ConnectionEvent.AUTH_FAILURE_NONE;
                            break;
                        case WifiManager.ERROR_AUTH_FAILURE_TIMEOUT:
                            level2FailureReason =
                                    WifiMetricsProto.ConnectionEvent.AUTH_FAILURE_TIMEOUT;
                            break;
                        case WifiManager.ERROR_AUTH_FAILURE_WRONG_PSWD:
                            level2FailureReason =
                                    WifiMetricsProto.ConnectionEvent.AUTH_FAILURE_WRONG_PSWD;
                            break;
                        case WifiManager.ERROR_AUTH_FAILURE_EAP_FAILURE:
                            level2FailureReason =
                                    WifiMetricsProto.ConnectionEvent.AUTH_FAILURE_EAP_FAILURE;
                            break;
                        default:
                            level2FailureReason =
                                    WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN;
                            break;
                    }
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_AUTHENTICATION_FAILURE,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE,
                            level2FailureReason);
                    if (reasonCode != WifiManager.ERROR_AUTH_FAILURE_WRONG_PSWD) {
                        mWifiInjector.getWifiLastResortWatchdog()
                                .noteConnectionFailureAndTriggerIfNeeded(
                                        getTargetSsid(), mTargetRoamBSSID,
                                        WifiLastResortWatchdog.FAILURE_CODE_AUTHENTICATION);
                    }
                    if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
                        String logMessage = "Wi-Fi is failed to connect to ";
                        if (targetedNetwork != null && targetedNetwork.SSID != null) {
                            logMessage += targetedNetwork.getPrintableSsid() + " network using ";
                            if (targetedNetwork.allowedKeyManagement.get(KeyMgmt.NONE) ||
                                targetedNetwork.allowedKeyManagement.get(KeyMgmt.WPA_PSK) ||
                                targetedNetwork.allowedKeyManagement.get(KeyMgmt.WPA2_PSK) ||
                                targetedNetwork.allowedKeyManagement.get(KeyMgmt.WAPI_PSK)) {
                                logMessage += "802.11-2012 channel.";
                            } else if (targetedNetwork.enterpriseConfig != null) {
                                if (targetedNetwork.enterpriseConfig.getEapMethod() == WifiEnterpriseConfig.Eap.TLS) {
                                    logMessage += "EAP-TLS channel";
                                    Log.e(TAG, "Wi-Fi is failed to connect to "
                                            + targetedNetwork.getPrintableSsid()
                                            + " network using EAP-TLS channel");
                                } else {
                                    logMessage += "802.1X channel";
                                    Log.e(TAG, "Wi-Fi is connected to "
                                            + targetedNetwork.getPrintableSsid()
                                            + " network using 802.1X channel");
                                }
                            }
                        } else {
                            logMessage += mWifiInfo.getSSID() + " network.";
                        }
                        WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                            false, TAG, logMessage + " Reason: Authentication failure.");
                    }
                    notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            DISCONNECT_REASON_AUTH_FAIL);
                    if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        if (targetedNetwork != null) {
                            report(ReportIdKey.ID_AUTH_FAIL,
                                    ReportUtil.getReportDataForAuthFail(mTargetNetworkId, message.arg2,
                                        targetedNetwork.status,
                                        targetedNetwork.numAssociation,
                                        targetedNetwork.getNetworkSelectionStatus().getNetworkSelectionStatus(),
                                        targetedNetwork.getNetworkSelectionStatus().getNetworkSelectionDisableReason()));
                        }
                    }
                    break;
                case WifiMonitor.BSSID_PRUNED_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
                    // TODO: Refer to commented lines copied from ASSOCIATION_REJECTION_EVENT below.
                    // mWifiDiagnostics.captureBugReportData(
                    //         WifiDiagnostics.REPORT_REASON_ASSOC_FAILURE);
                    mDidBlackListBSSID = false;
                    String str_obj = (String) message.obj;
                    if (str_obj == null) {
                        Log.e(TAG, "Bssid Pruned event: no obj in message!");
                        break;
                    }
                    String[] tokens = str_obj.split("\\s");
                    if (tokens.length != 4) {
                        Log.e(TAG, "Bssid Pruned event: wrong string obj format!");
                        break;
                    }
                    ssid = tokens[0];
                    bssid = tokens[1];
                    int timeRemaining = Integer.MIN_VALUE;
                    try {
                        reasonCode = WifiConnectivityManager.REASON_CODE_BSSID_PRUNED_BASE
                                + Integer.parseInt(tokens[2]);
                        if (reasonCode == WifiConnectivityManager.REASON_CODE_BSSID_PRUNED_ASSOC_RETRY_DELAY
                                || reasonCode == WifiConnectivityManager.REASON_CODE_BSSID_PRUNED_RSSI_ASSOC_REJ) {
                            timeRemaining = Integer.parseInt(tokens[3]) * 1000;
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Bssid Pruned event: wrong reasonCode or timeRemaining!" + e);
                        break;
                    }
                    Log.d(TAG, "Bssid Pruned event: ssid=" + ssid + " bssid=" + bssid + " reason code="
                            + reasonCode + " timeRemaining=" + timeRemaining);
                    if (bssid == null || TextUtils.isEmpty(bssid)) {
                        // If BSSID is null, use the target roam BSSID
                        bssid = mTargetRoamBSSID;
                    }
                    if (bssid != null) {
                        // Remove network from supplicant to block excessive ASSOCIATION_REJECTION_EVENTs
                        // unless the network selection bssid set as "any".
                        config = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
                        if (config != null) {
                            String networkSelectionBSSID = config.getNetworkSelectionStatus()
                                    .getNetworkSelectionBSSID();
                            if (!SUPPLICANT_BSSID_ANY.equals(networkSelectionBSSID)) {
                                mWifiNative.removeNetworkIfCurrent(mInterfaceName, mTargetNetworkId);
                                Log.d(TAG, "Bssid Pruned event: remove networkid=" + mTargetNetworkId + " from supplicant");
                            }
                        }
                        // If we have a BSSID, tell configStore to black list it
                        mDidBlackListBSSID = mWifiConnectivityManager.trackBssid(bssid, false,
                            reasonCode, timeRemaining);
                    }
                    // TODO: Refer to commented lines copied from ASSOCIATION_REJECTION_EVENT below.
                    // mWifiConfigManager.updateNetworkSelectionStatus(mTargetNetworkId,
                    //         WifiConfiguration.NetworkSelectionStatus
                    //         .DISABLED_BSSID_PRUNED);
                    // mWifiConfigManager.setRecentFailureAssociationStatus(mTargetNetworkId,
                    //         reasonCode);
                    // mSupplicantStateTracker.sendMessage(WifiMonitor.ASSOCIATION_REJECTION_EVENT);
                    // If rejection occurred while Metrics is tracking a ConnnectionEvent, end it.
                    // reportConnectionAttemptEnd(
                    //         timedOut
                    //                 ? WifiMetrics.ConnectionEvent.FAILURE_ASSOCIATION_TIMED_OUT
                    //                 : WifiMetrics.ConnectionEvent.FAILURE_ASSOCIATION_REJECTION,
                    //         WifiMetricsProto.ConnectionEvent.HLF_NONE);
                    // mWifiInjector.getWifiLastResortWatchdog()
                    //         .noteConnectionFailureAndTriggerIfNeeded(
                    //                 getTargetSsid(), bssid,
                    //                 WifiLastResortWatchdog.FAILURE_CODE_ASSOCIATION);
                    // notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                    //         DISCONNECT_REASON_ASSOC_REJECTED);
                    // if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                    //     config = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
                    //     if (config != null) {
                    //         report(ReportIdKey.ID_ASSOC_REJECT,
                    //                 ReportUtil.getReportDataForAssocReject(mTargetNetworkId, bssid, reasonCode,
                    //                     config.status,
                    //                     config.numAssociation,
                    //                     config.getNetworkSelectionStatus().getNetworkSelectionStatus(),
                    //                     config.getNetworkSelectionStatus().getNetworkSelectionDisableReason()));
                    //     }
                    // }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    SupplicantState state = handleSupplicantStateChange(message);

                    // Supplicant can fail to report a NETWORK_DISCONNECTION_EVENT
                    // when authentication times out after a successful connection,
                    // we can figure this from the supplicant state. If supplicant
                    // state is DISCONNECTED, but the mNetworkInfo says we are not
                    // disconnected, we need to handle a disconnection
                    if (state == SupplicantState.DISCONNECTED
                            && mNetworkInfo.getState() != NetworkInfo.State.DISCONNECTED) {
                        if (mVerboseLoggingEnabled) {
                            log("Missed CTRL-EVENT-DISCONNECTED, disconnect");
                        }
                        handleNetworkDisconnect();
                        transitionTo(mDisconnectedState);
                    }

                    // If we have COMPLETED a connection to a BSSID, start doing
                    // DNAv4/DNAv6 -style probing for on-link neighbors of
                    // interest (e.g. routers); harmless if none are configured.
                    if (state == SupplicantState.COMPLETED) {
                        if (mIpClient != null) {
                            mIpClient.confirmConfiguration();
                        }
                        mWifiScoreReport.noteIpCheck();

                        // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                                && "".equals(CONFIG_SECURE_SVC_INTEGRATION) && !(SemCscFeature.getInstance()
                                        .getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                            if (MobileWipsFrameworkService.getInstance() != null) {
                                MobileWipsFrameworkService.getInstance().sendEmptyMessage(MobileWipsDef.EVENT_CONNECTION_COMPLETED);
                            }
                        }
                        // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    }
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST:
                    if (message.arg1 == 1) {
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                                StaEvent.DISCONNECT_P2P_DISCONNECT_WIFI_REQUEST);
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_DISCONNECT_BY_P2P);
                        mWifiNative.disconnect(mInterfaceName);
                        mTemporarilyDisconnectWifi = true;
                    } else {
                        mWifiNative.reconnect(mInterfaceName);
                        mTemporarilyDisconnectWifi = false;
                    }
                    break;
                case CMD_REMOVE_NETWORK:
                    netId = message.arg1;
                    config = mWifiConfigManager.getConfiguredNetwork(netId); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE

                    if (!deleteNetworkConfigAndSendReply(message, false)) {
                        // failed to remove the config and caller was notified
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                        break;
                    }
                    if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                        mWifiGeofenceManager.removeNetwork(config);
                    }
                    //  we successfully deleted the network config
                    if (netId == mTargetNetworkId || netId == mLastNetworkId) {
                        // Disconnect and let autojoin reselect a new network
                        disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        //sendMessage(CMD_DISCONNECT);
                    }
                    break;
                case CMD_ENABLE_NETWORK:
                    boolean disableOthers = message.arg2 == 1;
                    netId = message.arg1;
                    if (disableOthers) {
                        // If the app has all the necessary permissions, this will trigger a connect
                        // attempt.
                        ok = connectToUserSelectNetwork(netId, message.sendingUid, false);
                    } else {
                        ok = mWifiConfigManager.enableNetwork(netId, false, message.sendingUid);
                    }
                    if (!ok) {
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    replyToMessage(message, message.what, ok ? SUCCESS : FAILURE);
                    break;
                case WifiManager.DISABLE_NETWORK:
                    netId = message.arg1;
                    //++SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                    boolean replyDone = false;
                    if (!mWifiConfigManager.canDisableNetwork(netId, message.sendingUid)) {
                        loge("Failed to disable network");
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                        replyToMessage(message, WifiManager.DISABLE_NETWORK_FAILED,
                                WifiManager.ERROR);
                        break;
                    }
                    if (netId == mTargetNetworkId || netId == mLastNetworkId) {
                        if (getCurrentState() == mConnectedState) {
                            replyToMessage(message, WifiManager.DISABLE_NETWORK_SUCCEEDED);
                            replyDone = true;
                            notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                   DISCONNECT_REASON_DISABLE_NETWORK);
                            mDelayDisconnect.checkAndWait(mNetworkInfo);
                        }
                    }
                    //--SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                    if (mWifiConfigManager.disableNetwork(netId, message.sendingUid)) {
                        if (!replyDone) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                            replyToMessage(message, WifiManager.DISABLE_NETWORK_SUCCEEDED);
                        }
                        if (netId == mTargetNetworkId || netId == mLastNetworkId) {
                            // Disconnect and let autojoin reselect a new network
                            disconnectCommand(0, DISCONNECT_REASON_DISABLE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            //sendMessage(CMD_DISCONNECT);
                        }
                    } else {
                        loge("Failed to disable network");
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                        if (!replyDone) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                            replyToMessage(message, WifiManager.DISABLE_NETWORK_FAILED,
                                    WifiManager.ERROR);
                        }
                    }
                    break;
                case CMD_DISABLE_EPHEMERAL_NETWORK:
                    config = mWifiConfigManager.disableEphemeralNetwork((String) message.obj);
                    if (config != null) {
                        if (config.networkId == mTargetNetworkId
                                || config.networkId == mLastNetworkId) {
                            // Disconnect and let autojoin reselect a new network
                            disconnectCommand(0, DISCONNECT_REASON_DISABLE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            //sendMessage(CMD_DISCONNECT);
                        }
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY:
                    netId = message.arg2;
                    boolean identitySent = false;
                    // For SIM & AKA/AKA' EAP method Only, get identity from ICC
                    if (mTargetWifiConfiguration != null
                            && mTargetWifiConfiguration.networkId == netId
                            && TelephonyUtil.isSimConfig(mTargetWifiConfiguration)) {
                        // Pair<identity, encrypted identity>
                        Pair<String, String> identityPair =
                                TelephonyUtil.getSimIdentity(getTelephonyManager(),
                                        new TelephonyUtil(), mTargetWifiConfiguration,
                                        mWifiInjector.getCarrierNetworkConfig());
                        //Log.i(TAG, "SUP_REQUEST_IDENTITY: identityPair=" + identityPair); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        if (identityPair != null && identityPair.first != null) {
                            Log.i(TAG, "SUP_REQUEST_IDENTITY: identity =" + identityPair.first.substring(0, 7)); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                            mWifiNative.simIdentityResponse(mInterfaceName, netId,
                                    identityPair.first, identityPair.second);
                            identitySent = true;
                            if (TextUtils.isEmpty(identityPair.second)) { //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                                updateIdentityOnWifiConfiguration(mTargetWifiConfiguration, identityPair.first);
                            }
                        } else {
                            Log.e(TAG, "Unable to retrieve identity from Telephony");
                        }
                    }

                    if (!identitySent) {
                        // Supplicant lacks credentials to connect to that network, hence black list
                        ssid = (String) message.obj;
                        if (mTargetWifiConfiguration != null && ssid != null
                                && mTargetWifiConfiguration.SSID != null
//                                && mTargetWifiConfiguration.SSID.equals("\"" + ssid + "\"")) {
                                && mTargetWifiConfiguration.SSID.equals(ssid)) { //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX for retry popup
                            mWifiConfigManager.updateNetworkSelectionStatus(
                                    mTargetWifiConfiguration.networkId,
                                    WifiConfiguration.NetworkSelectionStatus
                                            .DISABLED_AUTHENTICATION_NO_CREDENTIALS);
                        }
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                                StaEvent.DISCONNECT_GENERIC);
//                        mWifiNative.disconnect(mInterfaceName); //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX for retry popup
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH:
                    logd("Received SUP_REQUEST_SIM_AUTH");
                    SimAuthRequestData requestData = (SimAuthRequestData) message.obj;
                    if (requestData != null) {
                        if (requestData.protocol == WifiEnterpriseConfig.Eap.SIM) {
                            handleGsmAuthRequest(requestData);
                        } else if (requestData.protocol == WifiEnterpriseConfig.Eap.AKA
                                || requestData.protocol == WifiEnterpriseConfig.Eap.AKA_PRIME) {
                            handle3GAuthRequest(requestData);
                        }
                    } else {
                        loge("Invalid SIM auth request");
                    }
                    break;
                case CMD_GET_MATCHING_OSU_PROVIDERS:
                    replyToMessage(message, message.what,
                            mPasspointManager.getMatchingOsuProviders(
                                    (List<ScanResult>) message.obj));
                    break;
                case CMD_GET_MATCHING_PASSPOINT_CONFIGS_FOR_OSU_PROVIDERS:
                    replyToMessage(message, message.what,
                            mPasspointManager.getMatchingPasspointConfigsForOsuProviders(
                                    (List<OsuProvider>) message.obj));
                    break;
                case CMD_GET_WIFI_CONFIGS_FOR_PASSPOINT_PROFILES:
                    replyToMessage(message, message.what,
                            mPasspointManager.getWifiConfigsForPasspointProfiles(
                                    (List<String>) message.obj));

                    break;
                case CMD_START_SUBSCRIPTION_PROVISIONING:
                    IProvisioningCallback callback = (IProvisioningCallback) message.obj;
                    OsuProvider provider =
                            (OsuProvider) message.getData().getParcelable(EXTRA_OSU_PROVIDER);
                    int res = mPasspointManager.startSubscriptionProvisioning(
                                    message.arg1, provider, callback) ? 1 : 0;
                    replyToMessage(message, message.what, res);
                    break;
                case CMD_RECONNECT:
                    WorkSource workSource = (WorkSource) message.obj;
                    mWifiConnectivityManager.forceConnectivityScan(workSource);
                    break;
                case CMD_REASSOCIATE:
                    mLastConnectAttemptTimestamp = mClock.getWallClockMillis();
                    mWifiNative.reassociate(mInterfaceName);
                    break;
                case CMD_START_ROAM:
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_START_CONNECT:
                    /* connect command coming from auto-join */
                    netId = message.arg1;
                    int uid = message.arg2;
                    bssid = (String) message.obj;

                    if (!hasConnectionRequests()) {
                        if (mNetworkAgent == null) {
                            loge("CMD_START_CONNECT but no requests and not connected,"
                                    + " bailing");
                            break;
                        } else if (!mWifiPermissionsUtil.checkNetworkSettingsPermission(uid)) {
                            loge("CMD_START_CONNECT but no requests and connected, but app "
                                    + "does not have sufficient permissions, bailing");
                            break;
                        }
                    }
                    config = mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId);
                    logd("CMD_START_CONNECT sup state "
                            + mSupplicantStateTracker.getSupplicantStateName()
                            + " my state " + getCurrentState().getName()
                            + " nid=" + Integer.toString(netId)
                            + " roam=" + Boolean.toString(mIsAutoRoaming));
                    if (config == null) {
                        loge("CMD_START_CONNECT and no config, bail out...");
                        break;
                    }
                    // Update scorecard while there is still state from existing connection
                    mWifiScoreCard.noteConnectionAttempt(mWifiInfo);
                    mTargetNetworkId = netId;
                    setTargetBssid(config, bssid);

                    if (TelephonyUtil.isSimConfig(config) && !setPermanentIdentity(config)) { //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        Log.i(TAG, "CMD_START_CONNECT , There is no Identity for EAP SimConfig network, skip connection");
                        break;
                    }
                    if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_EAP_FAST
                        && config.enterpriseConfig.getEapMethod() == WifiEnterpriseConfig.Eap.FAST) {
                        config.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
                        config.enterpriseConfig.setPhase1Method(WifiEnterpriseConfig.Phase1.NONE);
                        config.enterpriseConfig.setPacFile("");
                        mWifiConfigManager.addOrUpdateNetwork(config, Process.SYSTEM_UID);
                    }
                    reportConnectionAttemptStart(config, mTargetRoamBSSID,
                            WifiMetricsProto.ConnectionEvent.ROAM_UNRELATED);

                    if (ENBLE_WLAN_CONFIG_ANALYTICS) {
                        if (!isDisconnected() && Settings.Global.getInt(mContext.getContentResolver(),
                                Settings.Global.SAFE_WIFI, 0) != 1) {
                            if (!mWifiNative.setExtendedAnalyticsDisconnectReason(mInterfaceName,
                                    WifiNative.ANALYTICS_DISCONNECT_REASON_TRIGGER_DISCON_CONNECT_OTHER_AP)) {
                                Log.e(TAG, "Failed to set ExtendedAnalyticsDisconnectReason");
                            }
                        }
                    }

                    if (config.macRandomizationSetting
                            == WifiConfiguration.RANDOMIZATION_PERSISTENT
                            && mConnectedMacRandomzationSupported
                            && isSupportRandomMac(config)) { //SEC_DISABLE_MAC_RANDOMIZATION_FOR_KOR_CARRIER_HOTSPOT
                        configureRandomizedMacAddress(config);
                    } else {
                        setCurrentMacToFactoryMac(config);
                    }

                    String currentMacAddress = mWifiNative.getMacAddress(mInterfaceName);
                    mWifiInfo.setMacAddress(currentMacAddress);
                    if(DBG)
                        Log.d(TAG, "Connecting with " + currentMacAddress + " as the mac address");
                    if (mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                            && mLastNetworkId != netId) {
                        if (getCurrentState() == mConnectedState) {
                            mDelayDisconnect.checkAndWait(mNetworkInfo);
                        }
                    }
                    if (config.enterpriseConfig != null
                            && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())
                            && mWifiInjector.getCarrierNetworkConfig()
                                    .isCarrierEncryptionInfoAvailable()
                            && TextUtils.isEmpty(config.enterpriseConfig.getAnonymousIdentity())
                            && config.semIsVendorSpecificSsid) { //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        String anonAtRealm = TelephonyUtil.getAnonymousIdentityWith3GppRealm(
                                getTelephonyManager());
                        //+SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        int eapMethod = config.enterpriseConfig.getEapMethod();
                        String prefix = "";
                        String prefixAnonAtRealm = "";
                        if (eapMethod == WifiEnterpriseConfig.Eap.SIM) {
                            prefix = "1";
                        } else if (eapMethod == WifiEnterpriseConfig.Eap.AKA) {
                            prefix = "0";
                        } else if (eapMethod == WifiEnterpriseConfig.Eap.AKA_PRIME) {
                            prefix = "6";
                        } else {
                            Log.e(TAG, " config is not a valid EapMethod " + eapMethod);
                        }
                        prefixAnonAtRealm = prefix+anonAtRealm;
                        Log.i(TAG, "setAnonymousIdentity " + prefixAnonAtRealm);
                        //-SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        config.enterpriseConfig.setAnonymousIdentity(prefixAnonAtRealm/*anonAtRealm*/); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                    }

                    //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

                    if (mWifiNative.connectToNetwork(mInterfaceName, config)) {
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_CMD_START_CONNECT, config);

                        report(ReportIdKey.ID_TRY_TO_CONNECT, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                ReportUtil.getReportDataForTryToConnect(netId, config.SSID,
                                    config.numAssociation,
                                    bssid, false));

                        mLastConnectAttemptTimestamp = mClock.getWallClockMillis();
                        mTargetWifiConfiguration = config;
                        mIsAutoRoaming = false;

                        //+SEC_PRODUCT_FEATURE_WLAN_SUPPLICANT_STA_IFACE_HAL
                        String networkSelectionBSSID = config.getNetworkSelectionStatus()
                                .getNetworkSelectionBSSID();
                        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
                        String networkSelectionBSSIDCurrent = (currentConfig == null)
                            ? null
                            : currentConfig.getNetworkSelectionStatus().getNetworkSelectionBSSID();
                        //-SEC_PRODUCT_FEATURE_WLAN_SUPPLICANT_STA_IFACE_HAL
                        if (getCurrentState() != mDisconnectedState //SEC_PRODUCT_FEATURE_WLAN_SUPPLICANT_STA_IFACE_HAL
                                && !(mLastNetworkId == netId
                                && Objects.equals(networkSelectionBSSID, networkSelectionBSSIDCurrent))) {
                            notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    DISCONNECT_REASON_START_CONNECT);
                            transitionTo(mDisconnectingState);
                        }
                    } else {
                        loge("CMD_START_CONNECT Failed to start connection to network " + config);
                        reportConnectionAttemptEnd(
                                WifiMetrics.ConnectionEvent.FAILURE_CONNECT_NETWORK_FAILED,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE,
                                WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
                        replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                WifiManager.ERROR);

                        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_RETRY_POPUP
                        if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)
                                || config.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)
                                || config.allowedKeyManagement.get(KeyMgmt.SAE)
                                || config.allowedKeyManagement.get(KeyMgmt.WAPI_PSK)
                                || (config.allowedKeyManagement.get(KeyMgmt.NONE) && config.wepKeys[0] != null)) {
                            mWifiConfigManager.updateNetworkSelectionStatus(netId,
                                    WifiConfiguration.NetworkSelectionStatus.DISABLED_BY_WRONG_PASSWORD);
                            SemWifiFrameworkUxUtils.sendShowInfoIntentToSettings(mContext,
                                    SemWifiFrameworkUxUtils.INFO_TYPE_NETWORK_TEMPORARILY_DISABLED,
                                    config.networkId,
                                    WifiConfiguration.NetworkSelectionStatus.DISABLED_BY_WRONG_PASSWORD);
                        }
                        break;
                    }
                    break;
                case CMD_REMOVE_APP_CONFIGURATIONS:
                    removedNetworkIds =
                            mWifiConfigManager.removeNetworksForApp((ApplicationInfo) message.obj);
                    if (removedNetworkIds.contains(mTargetNetworkId)
                            || removedNetworkIds.contains(mLastNetworkId)) {
                        // Disconnect and let autojoin reselect a new network.
                        disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        //sendMessage(CMD_DISCONNECT);
                    }
                    break;
                case CMD_REMOVE_USER_CONFIGURATIONS:
                    removedNetworkIds =
                            mWifiConfigManager.removeNetworksForUser((Integer) message.arg1);
                    if (removedNetworkIds.contains(mTargetNetworkId)
                            || removedNetworkIds.contains(mLastNetworkId)) {
                        // Disconnect and let autojoin reselect a new network.
                        disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        //sendMessage(CMD_DISCONNECT);
                    }
                    break;
                case WifiManager.CONNECT_NETWORK:
                    /**
                     * The connect message can contain a network id passed as arg1 on message or
                     * or a config passed as obj on message.
                     * For a new network, a config is passed to create and connect.
                     * For an existing network, a network id is passed
                     */
                    netId = message.arg1;
                    config = (WifiConfiguration) message.obj;
                    boolean hasCredentialChanged = false;
                    boolean isNewNetwork = false; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST

                    if (config != null) {
                        config.priority = mWifiConfigManager.increaseAndGetPriority(); //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS

                        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
                            boolean isAllowed = WifiPolicyCache.getInstance(mContext).isNetworkAllowed(config, false);
                            logd("CONNECT_NETWORK isAllowed=" + isAllowed);
                            if (!isAllowed) {
                                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_NETWORK,
                                        false, TAG, AuditEvents.WIFI_CONNECTING_NETWORK + netId + AuditEvents.FAILED);
                                mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                                replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                        WifiManager.NOT_AUTHORIZED);
                                break;
                            }
                        }

                        if (!config.isEphemeral()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
                            mWifiConfigManager.updateBssidWhitelist(config,
                                    mScanRequestProxy.getScanResults());
                        }

                        result = mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
                        if (!result.isSuccess()) {
                            loge("CONNECT_NETWORK adding/updating config=" + config + " failed");
                            mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                            replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                    WifiManager.ERROR);
                            break;
                        }

                        if ("VZW".equals(CSC_CONFIG_OP_BRANDING) || "SKT".equals(CSC_CONFIG_OP_BRANDING)) { //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, VZW, SKT
                            if (!TextUtils.isEmpty(config.SSID)
                                    && checkAndShowSimRemovedDialog(config)) {
                                mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                                replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                        WifiManager.ERROR);
                                break;
                            }
                        }
                        netId = result.getNetworkId();
                        if (Vendor.VZW == mOpBranding) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                            isNewNetwork = result.isNewNetwork();
                        }
                        hasCredentialChanged = result.hasCredentialChanged();
                    }

                    if (mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID
                            && mLastNetworkId != netId) {
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_START_CONNECT);
                    }
                    if (!connectToUserSelectNetwork(
                            netId, message.sendingUid, hasCredentialChanged)) {
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                        replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                WifiManager.NOT_AUTHORIZED);
                        break;
                    }

                    if (Vendor.VZW == mOpBranding //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                            && isNewNetwork
                            && mSemWifiHiddenNetworkTracker != null
                            && config != null
                            && config.hiddenSSID) {
                        mSemWifiHiddenNetworkTracker.startTracking(config);
                    }

                    mWifiMetrics.logStaEvent(StaEvent.TYPE_CONNECT_NETWORK, config);
                    broadcastWifiCredentialChanged(WifiManager.WIFI_CREDENTIAL_SAVED, config);
                    replyToMessage(message, WifiManager.CONNECT_NETWORK_SUCCEEDED);
                    break;
                case WifiManager.SAVE_NETWORK:
                    result = saveNetworkConfigAndSendReply(message);
                    netId = result.getNetworkId();
                    if (result.isSuccess() && mWifiInfo.getNetworkId() == netId) {
                        if (result.hasCredentialChanged()) {
                            config = (WifiConfiguration) message.obj;
                            // The network credentials changed and we're connected to this network,
                            // start a new connection with the updated credentials.
                            logi("SAVE_NETWORK credential changed for config=" + config.configKey()
                                    + ", Reconnecting.");
                            startConnectToNetwork(netId, message.sendingUid, SUPPLICANT_BSSID_ANY);
                        } else {
                            if (result.hasProxyChanged()) {
                                if (mIpClient != null) {
                                    log("Reconfiguring proxy on connection");
                                    mIpClient.setHttpProxy(
                                            getCurrentWifiConfiguration().getHttpProxy());
                                }
                            }
                            if (result.hasIpChanged()) {
                                // The current connection configuration was changed
                                // We switched from DHCP to static or from static to DHCP, or the
                                // static IP address has changed.
                                log("Reconfiguring IP on connection");
                                // TODO(b/36576642): clear addresses and disable IPv6
                                // to simplify obtainingIpState.
                                transitionTo(mObtainingIpState);
                            }
                        }
                    }
                    break;
                case WifiManager.FORGET_NETWORK:
                    netId = message.arg1;
                    WifiConfiguration toRemove = mWifiConfigManager.getConfiguredNetwork(netId); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                    if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                        mWifiGeofenceManager.forgetNetwork(toRemove);
                    }

                    if (!deleteNetworkConfigAndSendReply(message, true)) {
                        // Caller was notified of failure, nothing else to do
                        break;
                    }
                    // the network was deleted

                    //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                    if (ENBLE_WLAN_CONFIG_ANALYTICS)
                        setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_USER_TRIGGER_DISCON_REMOVE_PROFILE);
                    //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

                    if (netId == mTargetNetworkId || netId == mLastNetworkId) {
                        // Disconnect and let autojoin reselect a new network
                        disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        //sendMessage(CMD_DISCONNECT);
                    }
                    break;
                case CMD_ASSOCIATED_BSSID:
                    // This is where we can confirm the connection BSSID. Use it to find the
                    // right ScanDetail to populate metrics.
                    String someBssid = (String) message.obj;
                    if (someBssid != null) {
                        // Get the ScanDetail associated with this BSSID.
                        ScanDetailCache scanDetailCache =
                                mWifiConfigManager.getScanDetailCacheForNetwork(mTargetNetworkId);
                        if (scanDetailCache != null) {
                            mWifiMetrics.setConnectionScanDetail(scanDetailCache.getScanDetail(
                                    someBssid));
                        }
                    }

                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                            && "".equals(CONFIG_SECURE_SVC_INTEGRATION) && !(SemCscFeature.getInstance()
                                    .getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                        if (MobileWipsFrameworkService.getInstance() != null) {
                            MobileWipsFrameworkService.getInstance().sendEmptyMessage(MobileWipsDef.EVENT_ASSOCIATED);
                        }
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION

                    handleStatus = NOT_HANDLED;
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    if (mVerboseLoggingEnabled) log("Network connection established");
                    mLastNetworkId = message.arg1;
                    mLastConnectedNetworkId = mLastNetworkId; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    mWifiConfigManager.clearRecentFailureReason(mLastNetworkId);
                    mLastBssid = (String) message.obj;
                    reasonCode = message.arg2;
                    // TODO: This check should not be needed after ClientModeImpl refactor.
                    // Currently, the last connected network configuration is left in
                    // wpa_supplicant, this may result in wpa_supplicant initiating connection
                    // to it after a config store reload. Hence the old network Id lookups may not
                    // work, so disconnect the network and let network selector reselect a new
                    // network.
                    config = getCurrentWifiConfiguration();
                    if (config != null) {
                        mWifiInfo.setBSSID(mLastBssid);
                        mWifiInfo.setNetworkId(mLastNetworkId);
                        mWifiInfo.setMacAddress(mWifiNative.getMacAddress(mInterfaceName));

                        ScanDetailCache scanDetailCache =
                                mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);
                        if (scanDetailCache != null && mLastBssid != null) {
                            Log.d(TAG, "scan detail is in cache, find scanResult from cache");
                            ScanResult scanResult = scanDetailCache.getScanResult(mLastBssid);
                            if (scanResult != null) {
                                Log.d(TAG, "found scanResult! update mWifiInfo");
                                mWifiInfo.setFrequency(scanResult.frequency);
                                updateWifiInfoForVendors(scanResult); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_KTT_GIGA_AP, SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                                mWifiInfo.setWifiMode(scanResult.wifiMode); //SEC_PRODUCT_FEATURE_WLAN_WIFIMODE
                            } else {
                                Log.d(TAG, "can't update vendor infos, bssid: " + mLastBssid);
                            }
                        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_KTT_GIGA_AP, SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20, SEC_PRODUCT_FEATURE_WLAN_WIFIMODE
                        } else if (scanDetailCache == null && mLastBssid != null) {
                            Log.d(TAG, "scan detail is not in cache, find scanResult from last native scan results");
                            ArrayList<ScanDetail> scanDetails = mWifiNative.getScanResults(mInterfaceName);
                            for (ScanDetail scanDetail : scanDetails) {
                                if (mLastBssid.equals(scanDetail.getBSSIDString())) {
                                    ScanResult scanResult = scanDetail.getScanResult();
                                    Log.d(TAG, "found scanResult! update the cache and mWifiInfo");
                                    mWifiConfigManager.updateScanDetailForNetwork(mLastNetworkId, scanDetail);
                                    mWifiInfo.setFrequency(scanResult.frequency);
                                    updateWifiInfoForVendors(scanResult);
                                    mWifiInfo.setWifiMode(scanResult.wifiMode);
                                    break;
                                }
                            }
                        }
                        //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_KTT_GIGA_AP, SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20, SEC_PRODUCT_FEATURE_WLAN_WIFIMODE
                        mWifiConnectivityManager.trackBssid(mLastBssid, true, reasonCode);

                        // We need to get the updated pseudonym from supplicant for EAP-SIM/AKA/AKA'
                        if (config.enterpriseConfig != null
                                && TelephonyUtil.isSimEapMethod(
                                        config.enterpriseConfig.getEapMethod())
                                // if using anonymous@<realm>, do not use pseudonym identity on
                                // reauthentication. Instead, use full authentication using
                                // anonymous@<realm> followed by encrypted IMSI every time.
                                // This is because the encrypted IMSI spec does not specify its
                                // compatibility with the pseudonym identity specified by EAP-AKA.
                                && !TelephonyUtil.isAnonymousAtRealmIdentity(
                                        config.enterpriseConfig.getAnonymousIdentity())) {
                            String anonymousIdentity =
                                    mWifiNative.getEapAnonymousIdentity(mInterfaceName);
                            if (mVerboseLoggingEnabled) {
                                log("EAP Pseudonym: " + anonymousIdentity);
                            }
                            config.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                            mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
                        }
                        if (mUnstableApController != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                            mUnstableApController.l2Connected(mLastNetworkId);
                        }
                        sendNetworkStateChangeBroadcast(mLastBssid);

                        report(ReportIdKey.ID_L2_CONNECTED, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    ReportUtil.getReportDataForL2Connected(mLastNetworkId, mLastBssid));

                        transitionTo(mObtainingIpState);
                    } else {
                        logw("Connected to unknown networkId " + mLastNetworkId
                                + ", disconnecting...");
                        disconnectCommand(0, DISCONNECT_REASON_NO_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        //sendMessage(CMD_DISCONNECT);
                    }
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    // Calling handleNetworkDisconnect here is redundant because we might already
                    // have called it when leaving L2ConnectedState to go to disconnecting state
                    // or thru other path
                    // We should normally check the mWifiInfo or mLastNetworkId so as to check
                    // if they are valid, and only in this case call handleNEtworkDisconnect,
                    // TODO: this should be fixed for a L MR release
                    // The side effect of calling handleNetworkDisconnect twice is that a bunch of
                    // idempotent commands are executed twice (stopping Dhcp, enabling the SPS mode
                    // at the chip etc...
                    if (mVerboseLoggingEnabled) log("ConnectModeState: Network connection lost ");

                    //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                    checkAndUpdateUnstableAp(mTargetNetworkId, (String) message.obj,
                            message.arg1 != 0, message.arg2);

                    handleNetworkDisconnect();
                    transitionTo(mDisconnectedState);
                    break;
                case CMD_QUERY_OSU_ICON:
                    mPasspointManager.queryPasspointIcon(
                            ((Bundle) message.obj).getLong(EXTRA_OSU_ICON_QUERY_BSSID),
                            ((Bundle) message.obj).getString(EXTRA_OSU_ICON_QUERY_FILENAME));
                    break;
                case CMD_MATCH_PROVIDER_NETWORK:
                    // TODO(b/31065385): Passpoint config management.
                    replyToMessage(message, message.what, 0);
                    break;
                case CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG:
                    Bundle bundle = (Bundle) message.obj;
                    PasspointConfiguration passpointConfig = bundle.getParcelable(
                            EXTRA_PASSPOINT_CONFIGURATION);
                    if (mPasspointManager.addOrUpdateProvider(passpointConfig,
                            bundle.getInt(EXTRA_UID),
                            bundle.getString(EXTRA_PACKAGE_NAME))) {
                        String fqdn = passpointConfig.getHomeSp().getFqdn();
                        if (isProviderOwnedNetwork(mTargetNetworkId, fqdn)
                                || isProviderOwnedNetwork(mLastNetworkId, fqdn)) {
                            logd("Disconnect from current network since its provider is updated");
                            disconnectCommand(0, DISCONNECT_REASON_ADD_OR_UPDATE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            //sendMessage(CMD_DISCONNECT);
                        }
                        replyToMessage(message, message.what, SUCCESS);
                    } else {
                        replyToMessage(message, message.what, FAILURE);
                    }
                    break;
                case CMD_REMOVE_PASSPOINT_CONFIG:
                    String fqdn = (String) message.obj;
                    if (mPasspointManager.removeProvider(fqdn)) {
                        boolean needTosendDisconnectMsg = false;    // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                        if (isProviderOwnedNetwork(mTargetNetworkId, fqdn)
                                || isProviderOwnedNetwork(mLastNetworkId, fqdn)) {
                            logd("Disconnect from current network since its provider is removed");
                            needTosendDisconnectMsg = true;
                        }
                        // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20 - Start
                        List<WifiConfiguration> savedNetworks = mWifiConfigManager.getConfiguredNetworks();
                        for (WifiConfiguration network : savedNetworks) {
                            if (!network.isPasspoint()) {
                                continue;
                            }
                            if (fqdn.equals(network.FQDN)) {
                                mWifiConfigManager.removeNetwork(network.networkId, network.creatorUid);
                                break;
                            }
                        }
                        if (needTosendDisconnectMsg) {
                            disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            //sendMessage(CMD_DISCONNECT);
                        }
                        // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20 - End
                        mWifiConfigManager.removePasspointConfiguredNetwork(fqdn);
                        replyToMessage(message, message.what, SUCCESS);
                    } else {
                        replyToMessage(message, message.what, FAILURE);
                    }
                    break;
                case CMD_GET_ALL_MATCHING_FQDNS_FOR_SCAN_RESULTS:
                    replyToMessage(message, message.what,
                            mPasspointManager.getAllMatchingFqdnsForScanResults(
                                    (List<ScanResult>) message.obj));
                    break;
                case CMD_TARGET_BSSID:
                    // Trying to associate to this BSSID
                    if (message.obj != null) {
                        mTargetRoamBSSID = (String) message.obj;
                    }
                    break;
                case CMD_GET_LINK_LAYER_STATS:
                    WifiLinkLayerStats stats = getWifiLinkLayerStats();
                    replyToMessage(message, message.what, stats);
                    break;
                case CMD_RESET_SIM_NETWORKS:
                    log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed");
                    boolean simPresent = message.arg1 == 1;
                    if (!simPresent) {
                        mPasspointManager.removeEphemeralProviders();
                        mWifiConfigManager.resetSimNetworks();
                        mWifiNative.simAbsent(mInterfaceName); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                    }

                    if (mUnstableApController != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                        mUnstableApController.setSimCardState(
                                TelephonyUtil.isSimCardReady(getTelephonyManager()));
                    }
                    //[SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                    if (!simPresent && CSC_WIFI_SUPPORT_VZW_EAP_AKA) {
                        config = getCurrentWifiConfiguration();
                        if (config != null && !TextUtils.isEmpty(config.SSID)
                                && config.semIsVendorSpecificSsid) {
                            SemWifiFrameworkUxUtils.showWarningDialog(mContext,
                                    SemWifiFrameworkUxUtils.WARN_SIM_REMOVED_WHEN_CONNECTED,
                                    new String[] {StringUtil.removeDoubleQuotes(config.SSID)});
                        }
                    }//]SEC_PRODUCT_FEATURE_WLAN_EAP_SIM

                    if (message.arg1 == 0) { //sim absent
                        removePasspointNetworkIfSimAbsent();    // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                    }

                    updateVendorApSimState();    // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20

                    if(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO) {
                        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
                        try {
                            if (getTelephonyManager().getSimCardState() == TelephonyManager.SIM_STATE_PRESENT)
                                mPhoneStateEvent |= PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
                            else
                                mPhoneStateEvent &= ~PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
                            getTelephonyManager().listen(mPhoneStateListener, mPhoneStateEvent);
                        } catch (Exception e) {
                            Log.e(TAG, "TelephonyManager.listen exception happend : "+ e.toString());
                        }
                        handleCellularCapabilities();
                        //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
                    }
                    break;
                case CMD_BLUETOOTH_ADAPTER_STATE_CHANGE:
                    mBluetoothConnectionActive = (message.arg1
                            != BluetoothAdapter.STATE_DISCONNECTED);
                    mWifiNative.setBluetoothCoexistenceScanMode(
                            mInterfaceName, mBluetoothConnectionActive);
                    if (mWifiConnectivityManager != null) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_NETWORK_SELECTOR
                        mWifiConnectivityManager.setBluetoothConnected(mBluetoothConnectionActive);
                    }
                    break;
                case CMD_SET_SUSPEND_OPT_ENABLED:
                    if (message.arg1 == 1) {
                        setSuspendOptimizationsNative(SUSPEND_DUE_TO_SCREEN, true);
                        if (message.arg2 == 1) {
                            mSuspendWakeLock.release();
                        }
                    } else {
                        setSuspendOptimizationsNative(SUSPEND_DUE_TO_SCREEN, false);
                    }
                    break;
                case CMD_SET_HIGH_PERF_MODE:
                    if (message.arg1 == 1) {
                        setSuspendOptimizationsNative(SUSPEND_DUE_TO_HIGH_PERF, false);
                    } else {
                        setSuspendOptimizationsNative(SUSPEND_DUE_TO_HIGH_PERF, true);
                    }
                    break;
                case CMD_ENABLE_TDLS:
                    if (message.obj != null) {
                        String remoteAddress = (String) message.obj;
                        boolean enable = (message.arg1 == 1);
                        mWifiNative.startTdls(mInterfaceName, remoteAddress, enable);
                    }
                    break;
                case WifiMonitor.ANQP_DONE_EVENT:
                    // TODO(zqiu): remove this when switch over to wificond for ANQP requests.
                    mPasspointManager.notifyANQPDone((AnqpEvent) message.obj);
                    break;
                case CMD_STOP_IP_PACKET_OFFLOAD: {
                    int slot = message.arg1;
                    int ret = stopWifiIPPacketOffload(slot);
                    if (mNetworkAgent != null) {
                        mNetworkAgent.onSocketKeepaliveEvent(slot, ret);
                    }
                    break;
                }
                case WifiMonitor.RX_HS20_ANQP_ICON_EVENT:
                    // TODO(zqiu): remove this when switch over to wificond for icon requests.
                    mPasspointManager.notifyIconDone((IconEvent) message.obj);
                    break;
                case WifiMonitor.HS20_REMEDIATION_EVENT:
                    // TODO(zqiu): remove this when switch over to wificond for WNM frames
                    // monitoring.
                    mPasspointManager.receivedWnmFrame((WnmData) message.obj);
                    break;
                case CMD_SET_ADPS_MODE: //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
                    updateAdpsState();
                    break;
                case CMD_CONFIG_ND_OFFLOAD:
                    final boolean enabled = (message.arg1 > 0);
                    mWifiNative.configureNeighborDiscoveryOffload(mInterfaceName, enabled);
                    break;
                case CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER:
                    mWifiConnectivityManager.enable(message.arg1 == 1 ? true : false);
                    break;
                case CMD_SEC_API_ASYNC:
                    if (!processMessageForCallSECApiAsync(message)) {
                        return NOT_HANDLED;
                    }
                    break;
                case CMD_SEC_API:
                    int intResult = processMessageForCallSECApi(message);
                    replyToMessage(message, message.what, intResult);
                    break;
                case CMD_SEC_STRING_API:
                    String stringResult = processMessageForCallSECStringApi(message);
                    if (stringResult != null) {
                        replyToMessage(message, message.what, stringResult);
                        break;
                    }
                    return NOT_HANDLED;
                case CMD_SCAN_RESULT_AVAILABLE: //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                    if (mUnstableApController != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                        final List<ScanResult> scanResults = new ArrayList<>();
                        scanResults.addAll(mScanRequestProxy.getScanResults());
                        mUnstableApController.verifyAll(scanResults);
                    }
                    // >>>WCM>>>
                    try {
                        if (!mIsRoamNetwork && isRoamNetwork()) {
                            if (checkAndSetConnectivityInstance()) mCm.setWifiRoamNetwork(true);
                            mIsRoamNetwork = true;
                            Log.i(TAG, "Roam Network");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // <<<WCM<<<
                    break;
                // >>>WCM>>>
                case WifiConnectivityMonitor.CMD_CONFIG_SET_CAPTIVE_PORTAL:
                    boolean captivePortal = (message.arg2 == 1);
                    mWifiConfigManager.setCaptivePortal(message.arg1, captivePortal);
                    break;
                case WifiConnectivityMonitor.CMD_CONFIG_UPDATE_NETWORK_SELECTION:
                    mWifiConfigManager.updateNetworkSelectionStatus(message.arg1, message.arg2);
                default:
                    handleStatus = NOT_HANDLED;
                    break;
            }

            if (handleStatus == HANDLED) {
                logStateAndMessage(message, this);
            }

            return handleStatus;
        }
    }

    public boolean mIsRoamNetwork = false;
    private boolean isRoamNetwork() {
        if (mIsRoamNetwork) return true;
        final List<ScanResult> scanResults = new ArrayList<>();
        scanResults.addAll(mScanRequestProxy.getScanResults());
        if (scanResults != null) {
            // Find Roaming Target BSSID
            WifiConfiguration currConfig = mWifiConfigManager.getConfiguredNetwork(mWifiInfo.getNetworkId());
            int candidateCount = 0;
            if (currConfig != null) {
                String configSsid = currConfig.SSID;
                int configuredSecurity = -1;
                if (currConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
                    configuredSecurity = SECURITY_PSK;
                } else if (currConfig.allowedKeyManagement.get(KeyMgmt.SAE)) {
                    configuredSecurity = SECURITY_SAE;
                } else if (currConfig.allowedKeyManagement.get(KeyMgmt.WPA_EAP)
                        || currConfig.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
                    configuredSecurity = SECURITY_EAP;
                } else {
                    configuredSecurity = (currConfig.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
                }
                //ScanResult bestCandidate = null;
                for (ScanResult scanResult : scanResults) {
                    int scanedSecurity = SECURITY_NONE;
                    if (scanResult.capabilities.contains("WEP")) {
                        scanedSecurity = SECURITY_WEP;
                    } else if (scanResult.capabilities.contains("SAE")) {
                        scanedSecurity = SECURITY_SAE;
                    } else if (scanResult.capabilities.contains("PSK")) {
                        scanedSecurity = SECURITY_PSK;
                    } else if (scanResult.capabilities.contains("EAP")) {
                        scanedSecurity = SECURITY_EAP;
                    }
                    if (scanResult.SSID != null && configSsid != null && configSsid.length() > 2
                            && scanResult.SSID.equals(configSsid.substring(1, configSsid.length() - 1))
                            && (configuredSecurity == scanedSecurity)) {
                        if (scanResult.BSSID != null && !scanResult.BSSID.equals(mWifiInfo.getBSSID())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private WifiNetworkAgentSpecifier createNetworkAgentSpecifier(
            @NonNull WifiConfiguration currentWifiConfiguration, @Nullable String currentBssid,
            int specificRequestUid, @NonNull String specificRequestPackageName) {
        currentWifiConfiguration.BSSID = currentBssid;
        WifiNetworkAgentSpecifier wns =
                new WifiNetworkAgentSpecifier(currentWifiConfiguration, specificRequestUid,
                        specificRequestPackageName);
        return wns;
    }

    private NetworkCapabilities getCapabilities(WifiConfiguration currentWifiConfiguration) {
        final NetworkCapabilities result = new NetworkCapabilities(mNetworkCapabilitiesFilter);
        // MatchAllNetworkSpecifier set in the mNetworkCapabilitiesFilter should never be set in the
        // agent's specifier.
        result.setNetworkSpecifier(null);
        if (currentWifiConfiguration == null) {
            return result;
        }

        if (!mWifiInfo.isTrusted()) {
            result.removeCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
        } else {
            result.addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
        }

        if (!WifiConfiguration.isMetered(currentWifiConfiguration, mWifiInfo)) {
            result.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        } else {
            result.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }

        if (mWifiInfo.getRssi() != WifiInfo.INVALID_RSSI) {
            result.setSignalStrength(mWifiInfo.getRssi());
        } else {
            result.setSignalStrength(NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED);
        }

        if (currentWifiConfiguration.osu) {
            result.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }

        if (!mWifiInfo.getSSID().equals(WifiSsid.NONE)) {
            result.setSSID(mWifiInfo.getSSID());
        } else {
            result.setSSID(null);
        }
        Pair<Integer, String> specificRequestUidAndPackageName =
                mNetworkFactory.getSpecificNetworkRequestUidAndPackageName(
                        currentWifiConfiguration);
        // There is an active specific request.
        if (specificRequestUidAndPackageName.first != Process.INVALID_UID) {
            // Remove internet capability.
            result.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        // Fill up the network agent specifier for this connection.
        result.setNetworkSpecifier(
                createNetworkAgentSpecifier(
                        currentWifiConfiguration, getCurrentBSSID(),
                        specificRequestUidAndPackageName.first,
                        specificRequestUidAndPackageName.second));
        return result;
    }

    /**
     * Method to update network capabilities from the current WifiConfiguration.
     */
    public void updateCapabilities() {
        updateCapabilities(getCurrentWifiConfiguration());
    }

    private void updateCapabilities(WifiConfiguration currentWifiConfiguration) {
        if (mNetworkAgent == null) {
            return;
        }
        mNetworkAgent.sendNetworkCapabilities(getCapabilities(currentWifiConfiguration));
    }

    /**
     * Checks if the given network |networkdId| is provided by the given Passpoint provider with
     * |providerFqdn|.
     *
     * @param networkId The ID of the network to check
     * @param providerFqdn The FQDN of the Passpoint provider
     * @return true if the given network is provided by the given Passpoint provider
     */
    private boolean isProviderOwnedNetwork(int networkId, String providerFqdn) {
        if (networkId == WifiConfiguration.INVALID_NETWORK_ID) {
            return false;
        }
        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(networkId);
        if (config == null) {
            return false;
        }
        return TextUtils.equals(config.FQDN, providerFqdn);
    }

    private void handleEapAuthFailure(int networkId, int errorCode) {
        WifiConfiguration targetedNetwork =
                mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
        if (targetedNetwork != null) {
            switch (targetedNetwork.enterpriseConfig.getEapMethod()) {
                case WifiEnterpriseConfig.Eap.SIM:
                case WifiEnterpriseConfig.Eap.AKA:
                case WifiEnterpriseConfig.Eap.AKA_PRIME:
                    if (errorCode == WifiNative.EAP_SIM_VENDOR_SPECIFIC_CERT_EXPIRED) {
                        getTelephonyManager()
                                .createForSubscriptionId(
                                        SubscriptionManager.getDefaultDataSubscriptionId())
                                .resetCarrierKeysForImsiEncryption();
                    }
                    if (CSC_WIFI_ERRORCODE) { //TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE
                        showEapNotificationToast(errorCode);
                    }
                    break;

                default:
                    // Do Nothing
            }
        }
    }

    private class WifiNetworkAgent extends NetworkAgent {
        WifiNetworkAgent(Looper l, Context c, String tag, NetworkInfo ni,
                NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, tag, ni, nc, lp, score, misc);
        }
        private int mLastNetworkStatus = -1; // To detect when the status really changes

        @Override
        protected void unwanted() {
            // Ignore if we're not the current networkAgent.
            if (this != mNetworkAgent) return;
            if (mVerboseLoggingEnabled) {
                log("WifiNetworkAgent -> Wifi unwanted score " + Integer.toString(mWifiInfo.score));
            }
            unwantedNetwork(NETWORK_STATUS_UNWANTED_DISCONNECT);
        }

        @Override
        protected void networkStatus(int status, String redirectUrl) {
            if (this != mNetworkAgent) return;
            if (status == mLastNetworkStatus) return;
            mLastNetworkStatus = status;
            if (status == NetworkAgent.INVALID_NETWORK) {
                if (mVerboseLoggingEnabled) {
                    log("WifiNetworkAgent -> Wifi networkStatus invalid, score="
                            + Integer.toString(mWifiInfo.score));
                }
                unwantedNetwork(NETWORK_STATUS_UNWANTED_VALIDATION_FAILED);
            } else if (status == NetworkAgent.VALID_NETWORK) {
                if (mVerboseLoggingEnabled) {
                    log("WifiNetworkAgent -> Wifi networkStatus valid, score= "
                            + Integer.toString(mWifiInfo.score));
                }
                mWifiMetrics.logStaEvent(StaEvent.TYPE_NETWORK_AGENT_VALID_NETWORK);
                doNetworkStatus(status);
            }
        }

        @Override
        protected void saveAcceptUnvalidated(boolean accept) {
            if (this != mNetworkAgent) return;
            ClientModeImpl.this.sendMessage(CMD_ACCEPT_UNVALIDATED, accept ? 1 : 0);
        }

        @Override
        protected void startSocketKeepalive(Message msg) {
            ClientModeImpl.this.sendMessage(
                    CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        @Override
        protected void stopSocketKeepalive(Message msg) {
            ClientModeImpl.this.sendMessage(
                    CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        @Override
        protected void addKeepalivePacketFilter(Message msg) {
            ClientModeImpl.this.sendMessage(
                    CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF, msg.arg1, msg.arg2, msg.obj);
        }

        @Override
        protected void removeKeepalivePacketFilter(Message msg) {
            ClientModeImpl.this.sendMessage(
                    CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF, msg.arg1, msg.arg2, msg.obj);
        }

        @Override
        protected void setSignalStrengthThresholds(int[] thresholds) {
            // 0. If there are no thresholds, or if the thresholds are invalid,
            //    stop RSSI monitoring.
            // 1. Tell the hardware to start RSSI monitoring here, possibly adding MIN_VALUE and
            //    MAX_VALUE at the start/end of the thresholds array if necessary.
            // 2. Ensure that when the hardware event fires, we fetch the RSSI from the hardware
            //    event, call mWifiInfo.setRssi() with it, and call updateCapabilities(), and then
            //    re-arm the hardware event. This needs to be done on the state machine thread to
            //    avoid race conditions. The RSSI used to re-arm the event (and perhaps also the one
            //    sent in the NetworkCapabilities) must be the one received from the hardware event
            //    received, or we might skip callbacks.
            // 3. Ensure that when we disconnect, RSSI monitoring is stopped.
            log("Received signal strength thresholds: " + Arrays.toString(thresholds));
            if (thresholds.length == 0) {
                //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
                boolean isKnoxCustomAutoSwitchEnabled = false;
                if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CUSTOMIZATION_SDK) {
                    CustomDeviceManagerProxy customDeviceManager = CustomDeviceManagerProxy.getInstance();
                    if (customDeviceManager != null && customDeviceManager.getWifiAutoSwitchState()) {
                        isKnoxCustomAutoSwitchEnabled = true;
                        if (DBG) {
                            logd("KnoxCustom WifiAutoSwitch: not stopping RSSI monitoring");
                        }
                    }
                }
                if(!isKnoxCustomAutoSwitchEnabled) {
                //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }
                    ClientModeImpl.this.sendMessage(CMD_STOP_RSSI_MONITORING_OFFLOAD,
                            mWifiInfo.getRssi());
                    return;
                //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
                }
                //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }
            }
            int [] rssiVals = Arrays.copyOf(thresholds, thresholds.length + 2);
            rssiVals[rssiVals.length - 2] = Byte.MIN_VALUE;
            rssiVals[rssiVals.length - 1] = Byte.MAX_VALUE;
            Arrays.sort(rssiVals);
            byte[] rssiRange = new byte[rssiVals.length];
            for (int i = 0; i < rssiVals.length; i++) {
                int val = rssiVals[i];
                if (val <= Byte.MAX_VALUE && val >= Byte.MIN_VALUE) {
                    rssiRange[i] = (byte) val;
                } else {
                    Log.e(TAG, "Illegal value " + val + " for RSSI thresholds: "
                            + Arrays.toString(rssiVals));
                    ClientModeImpl.this.sendMessage(CMD_STOP_RSSI_MONITORING_OFFLOAD,
                            mWifiInfo.getRssi());
                    return;
                }
            }
            // TODO: Do we quash rssi values in this sorted array which are very close?
            mRssiRanges = rssiRange;
            ClientModeImpl.this.sendMessage(CMD_START_RSSI_MONITORING_OFFLOAD,
                    mWifiInfo.getRssi());
        }

        @Override
        protected void preventAutomaticReconnect() {
            if (this != mNetworkAgent) return;
            unwantedNetwork(NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN);
        }
    }

    void unwantedNetwork(int reason) {
        sendMessage(CMD_UNWANTED_NETWORK, reason);
    }

    void doNetworkStatus(int status) {
        sendMessage(CMD_NETWORK_STATUS, status);
    }

    // rfc4186 & rfc4187:
    // create Permanent Identity base on IMSI,
    // identity = usernam@realm
    // with username = prefix | IMSI
    // and realm is derived MMC/MNC tuple according 3GGP spec(TS23.003)
    private String buildIdentity(int eapMethod, String imsi, String mccMnc) {
        String mcc;
        String mnc;
        String prefix;

        if (imsi == null || imsi.isEmpty()) {
            return "";
        }

        if (eapMethod == WifiEnterpriseConfig.Eap.SIM) {
            prefix = "1";
        } else if (eapMethod == WifiEnterpriseConfig.Eap.AKA) {
            prefix = "0";
        } else if (eapMethod == WifiEnterpriseConfig.Eap.AKA_PRIME) {
            prefix = "6";
        } else {
            // not a valid EapMethod
            return "";
        }

        /* extract mcc & mnc from mccMnc */
        if (mccMnc != null && !mccMnc.isEmpty()) {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = "0" + mnc;
            }
        } else {
            // extract mcc & mnc from IMSI, assume mnc size is 3
            mcc = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
        }

        return prefix + imsi + "@wlan.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    class L2ConnectedState extends State {
        class RssiEventHandler implements WifiNative.WifiRssiEventHandler {
            @Override
            public void onRssiThresholdBreached(byte curRssi) {
                if (mVerboseLoggingEnabled) {
                    Log.e(TAG, "onRssiThresholdBreach event. Cur Rssi = " + curRssi);
                }
                sendMessage(CMD_RSSI_THRESHOLD_BREACHED, curRssi);
            }
        }

        RssiEventHandler mRssiEventHandler = new RssiEventHandler();

        @Override
        public void enter() {
            mRssiPollToken++;
            if (mEnableRssiPolling) {
                mLinkProbeManager.resetOnNewConnection();
                sendMessage(CMD_RSSI_POLL, mRssiPollToken, 0);
            }
            if (mNetworkAgent != null) {
                loge("Have NetworkAgent when entering L2Connected");
                setNetworkDetailedState(DetailedState.DISCONNECTED);
            }
            setNetworkDetailedState(DetailedState.CONNECTING);
            handleWifiNetworkCallbacks(WifiClientModeChannel.CALLBACK_CHECK_IS_CAPTIVE_PORTAL_EXCEPTION);
            final NetworkCapabilities nc = getCapabilities(getCurrentWifiConfiguration());
            synchronized (mNetworkAgentLock) {
                mNetworkAgent = new WifiNetworkAgent(getHandler().getLooper(), mContext,
                    "WifiNetworkAgent", mNetworkInfo, nc, mLinkProperties, 60, mNetworkMisc);
                // >>>WCM>>>
                mWifiScoreReport.setNeteworkAgent(mNetworkAgent);
                // <<<WCM<<<
            }

            // We must clear the config BSSID, as the wifi chipset may decide to roam
            // from this point on and having the BSSID specified in the network block would
            // cause the roam to faile and the device to disconnect
            clearTargetBssid("L2ConnectedState");
            mCountryCode.setReadyForChange(false);
            mWifiMetrics.setWifiState(WifiMetricsProto.WifiLog.WIFI_ASSOCIATED);
            mWifiScoreCard.noteNetworkAgentCreated(mWifiInfo, mNetworkAgent.netId);

            // >>>WCM>>>
            mCntRoamingStartSent = 0;
            isDhcpStartSent = false;
            // <<<WCM<<<
        }

        @Override
        public void exit() {
            if (mIpClient != null) {
                mIpClient.stop();
            }

            // This is handled by receiving a NETWORK_DISCONNECTION_EVENT in ConnectModeState
            // Bug: 15347363
            // For paranoia's sake, call handleNetworkDisconnect
            // only if BSSID is null or last networkId
            // is not invalid.
            if (mVerboseLoggingEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=" + Integer.toString(mLastNetworkId));
                if (mLastBssid != null) {
                    sb.append(" ").append(mLastBssid);
                }
            }
            if (mLastBssid != null || mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                handleNetworkDisconnect();
            }
            mCountryCode.setReadyForChange(true);
            mWifiMetrics.setWifiState(WifiMetricsProto.WifiLog.WIFI_DISCONNECTED);
            mWifiStateTracker.updateState(WifiStateTracker.DISCONNECTED);
            //Inform WifiLockManager
            WifiLockManager wifiLockManager = mWifiInjector.getWifiLockManager();
            wifiLockManager.updateWifiClientConnected(false);
        }

        @Override
        public boolean processMessage(Message message) {
            boolean handleStatus = HANDLED;

            switch (message.what) {
                case CMD_PRE_DHCP_ACTION:
                    handlePreDhcpSetup();
                    break;
                case CMD_PRE_DHCP_ACTION_COMPLETE:
                    if (mIpClient != null) {
                        mIpClient.completedPreDhcpAction();
                    }
                    break;
                case CMD_POST_DHCP_ACTION:
                    handlePostDhcpSetup();
                    // We advance to mConnectedState because IpClient will also send a
                    // CMD_IPV4_PROVISIONING_SUCCESS message, which calls handleIPv4Success(),
                    // which calls updateLinkProperties, which then sends
                    // CMD_IP_CONFIGURATION_SUCCESSFUL.
                    //
                    // In the event of failure, we transition to mDisconnectingState
                    // similarly--via messages sent back from IpClient.
                    break;
                case CMD_IPV4_PROVISIONING_SUCCESS: {
                    handleIPv4Success((DhcpResults) message.obj);
                    sendNetworkStateChangeBroadcast(mLastBssid);
                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                            && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                            && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                        if (MobileWipsFrameworkService.getInstance() != null) {
                            MobileWipsFrameworkService.getInstance().sendEmptyMessage(MobileWipsDef.EVENT_PROVISION_COMPLETED);
                        }
                    }
                    if (mWifiInjector.getSemWifiApChipInfo().supportWifiSharing()) {
                        //only handle ipv4, ignore ipv6 case for softapreaset
                        if(mWifiNative.CheckWifiSoftApIpReset()){
                             Log.d(TAG,"IP Subnet of MobileAp needs to be modified. So Reset Mobile Ap");
                             Intent resetIntent = new Intent("com.samsung.android.intent.action.WIFIAP_RESET");
                             if(mContext != null){
                                 mContext.sendBroadcast(resetIntent);
                                 resetIntent.setClassName("com.android.settings", "com.samsung.android.settings.wifi.mobileap.WifiApBroadcastReceiver");
                                 mContext.sendBroadcast(resetIntent);
                             }
                        }
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    break;
                }
                case CMD_IPV4_PROVISIONING_FAILURE: {
                    handleIPv4Failure();
                    mWifiInjector.getWifiLastResortWatchdog()
                            .noteConnectionFailureAndTriggerIfNeeded(
                                    getTargetSsid(), mTargetRoamBSSID,
                                    WifiLastResortWatchdog.FAILURE_CODE_DHCP);
                    break;
                }
                case CMD_IP_CONFIGURATION_SUCCESSFUL:
                    if (getCurrentWifiConfiguration() == null) {
                        // The current config may have been removed while we were connecting,
                        // trigger a disconnect to clear up state.
                        reportConnectionAttemptEnd(
                                WifiMetrics.ConnectionEvent.FAILURE_NETWORK_DISCONNECTION,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE,
                                WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_NO_NETWORK);
                        mWifiNative.disconnect(mInterfaceName);
                        transitionTo(mDisconnectingState);
                    } else {
                        handleSuccessfulIpConfiguration();
                        if (isRoaming()) { //SEC_PRODUCT_FEATURE_WLAN_GET_ROAMING_STATUS_FOR_MOBIKE
                            setRoamTriggered(false);
                        }
                        if (getCurrentState() == mConnectedState) { // SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
                            /**
                             * Samsung patch
                             * State transition from Connected to VerifyingLinkState(DetailedState.VERIFYING_POOR_LINK, State.CONNECTING)
                             * can make some application misunderstand State.CONNECTING as State.DISCONNECTED because it's not CONNECTED.
                             * So, we don't have to make a state transition in this situation(ip renewal success).
                             */
                            if (DBG)
                                log("DHCP renew post action!!! - Don't need to make state transition");
                            break;
                        }
                        report(ReportIdKey.ID_DHCP_FAIL, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                ReportUtil.getReportDataForDhcpResult(IssueCommandIds.DHCP_OK));
                        sendConnectedState();
                        transitionTo(mConnectedState);
                    }
                    break;
                case CMD_IP_CONFIGURATION_LOST:
                    if (ENBLE_WLAN_CONFIG_ANALYTICS) { //SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                        setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_DHCP_FAIL_UNSPECIFIED);
                    }
                    // Get Link layer stats so that we get fresh tx packet counters.
                    getWifiLinkLayerStats();
                    handleIpConfigurationLost();
                    report(ReportIdKey.ID_DHCP_FAIL, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            ReportUtil.getReportDataForDhcpResult(IssueCommandIds.DHCP_FAIL));
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_DHCP,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE,
                            WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
                    mWifiInjector.getWifiLastResortWatchdog()
                            .noteConnectionFailureAndTriggerIfNeeded(
                                    getTargetSsid(), mTargetRoamBSSID,
                                    WifiLastResortWatchdog.FAILURE_CODE_DHCP);
                    notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            DISCONNECT_REASON_DHCP_FAIL);
                    transitionTo(mDisconnectingState);
                    break;
                case CMD_IP_REACHABILITY_LOST:
                    if (mVerboseLoggingEnabled && message.obj != null) log((String) message.obj);
                    mWifiDiagnostics.captureBugReportData(
                            WifiDiagnostics.REPORT_REASON_REACHABILITY_LOST);
                    mWifiMetrics.logWifiIsUnusableEvent(
                            WifiIsUnusableEvent.TYPE_IP_REACHABILITY_LOST);
                    mWifiMetrics.addToWifiUsabilityStatsList(WifiUsabilityStats.LABEL_BAD,
                            WifiUsabilityStats.TYPE_IP_REACHABILITY_LOST, -1);
                    if (mIpReachabilityDisconnectEnabled) {
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_DHCP_FAIL);
                        handleIpReachabilityLost();
                        transitionTo(mDisconnectingState);
                    } else {
                        logd("CMD_IP_REACHABILITY_LOST but disconnect disabled -- ignore");
                        // >>>WCM>>>
                        handleWifiNetworkCallbacks(WifiClientModeChannel.CALLBACK_NOTIFY_REACHABILITY_LOST);
                        // <<<WCM<<<
                    }
                    break;
                case CMD_DISCONNECT:
                    mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                            StaEvent.DISCONNECT_GENERIC);
                    notifyDisconnectInternalReason(message.arg2); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                    if (getCurrentState() == mConnectedState) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                        //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                        if (ENBLE_WLAN_CONFIG_ANALYTICS && !mSettingsStore.isWifiToggleEnabled()) {
                            setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_USER_TRIGGER_DISCON_POWERONOFF_WIFIOFF);
                        }
                        //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                        mDelayDisconnect.checkAndWait(mNetworkInfo);
                    }
                    mWifiNative.disconnect(mInterfaceName);
                    transitionTo(mDisconnectingState);
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST:
                    if (message.arg1 == 1) {
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                                StaEvent.DISCONNECT_P2P_DISCONNECT_WIFI_REQUEST);
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_DISCONNECT_BY_P2P);
                        mWifiNative.disconnect(mInterfaceName);
                        mTemporarilyDisconnectWifi = true;
                        transitionTo(mDisconnectingState);
                    }
                    break;
                    /* Ignore connection to same network */
                case WifiManager.CONNECT_NETWORK:
                    int netId = message.arg1;
                    if (mWifiInfo.getNetworkId() == netId) {
                        replyToMessage(message, WifiManager.CONNECT_NETWORK_SUCCEEDED);
                        break;
                    }
                    handleStatus = NOT_HANDLED;
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    if (DBG) log("dongle roaming established");
                    mWifiInfo.setBSSID((String) message.obj);
                    mLastNetworkId = message.arg1;
                    mLastConnectedNetworkId = mLastNetworkId; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    mWifiInfo.setNetworkId(mLastNetworkId);
                    mWifiInfo.setMacAddress(mWifiNative.getMacAddress(mInterfaceName));
                    if (!mLastBssid.equals(message.obj)) {
                        setRoamTriggered(true);
                        mLastBssid = (String) message.obj;
                        //+SEC_PRODUCT_FEATURE_WLAN_GET_FREQUENCY
                        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_KTT_GIGA_AP
                        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                        ScanDetailCache scanDetailCache =
                                mWifiConfigManager.getScanDetailCacheForNetwork(mLastNetworkId);
                        ScanResult scanResult = null;
                        if (scanDetailCache != null) {
                            scanResult = scanDetailCache.getScanResult(mLastBssid);
                        }
                        if (scanResult == null) {
                            Log.d(TAG, "roamed scan result is not in cache, find it from last native scan results");
                            ArrayList<ScanDetail> scanDetails = mWifiNative.getScanResults(mInterfaceName);
                            for (ScanDetail scanDetail : scanDetails) {
                                if (mLastBssid.equals(scanDetail.getBSSIDString())) {
                                    Log.d(TAG, "found it! update the cache");
                                    scanResult = scanDetail.getScanResult();
                                    mWifiConfigManager.updateScanDetailForNetwork(mLastNetworkId, scanDetail);
                                    break;
                                }
                            }
                        }
                        if (scanResult != null) {
                            mWifiInfo.setFrequency(scanResult.frequency);
                            updateWifiInfoForVendors(scanResult);
                            mWifiInfo.setWifiMode(scanResult.wifiMode); //SEC_PRODUCT_FEATURE_WLAN_WIFIMODE
                            if (CSC_SUPPORT_5G_ANT_SHARE) {
                                sendIpcMessageToRilForLteu(LTEU_STA_5GHZ_CONNECTED, true, mWifiInfo.is5GHz(), false);
                            }
                        } else {
                            Log.d(TAG, "can't update vendor infos, bssid: " + mLastBssid);
                        }
                        //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                        //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_KTT_GIGA_AP
                        //-SEC_PRODUCT_FEATURE_WLAN_GET_FREQUENCY
                        sendNetworkStateChangeBroadcast(mLastBssid);

                        //+SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP,SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
                        int tmpDhcpRenewAfterRoamingMode = getRoamDhcpPolicy();
                        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
                        boolean isUsingStaticIp = false;
                        if (currentConfig != null) {
                            isUsingStaticIp = (currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC);
                        }
                        if (isUsingStaticIp) {
                            log("Static ip - skip renew");
                            tmpDhcpRenewAfterRoamingMode = -1;
                        }
                        if (mRoamDhcpPolicyByB2bConfig == ROAM_DHCP_SKIP || tmpDhcpRenewAfterRoamingMode == ROAM_DHCP_SKIP
                                ||  tmpDhcpRenewAfterRoamingMode == -1) {
                            log("Skip Dhcp - mRoamDhcpPolicyByB2bConfig:" + mRoamDhcpPolicyByB2bConfig 
                                    + " ,tmpDhcpRenewAfterRoamingMode : " + tmpDhcpRenewAfterRoamingMode);
                            setRoamTriggered(false);
                        } else {
                            if (checkIfForceRestartDhcp()) {
                                restartDhcp(currentConfig);
                            } else {
                                CheckIfDefaultGatewaySame();
                            }
                        }

                        if(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO)
                            handleCellularCapabilities(true); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO

                        if (WifiRoamingAssistant.getInstance() != null) {
                            WifiRoamingAssistant.getInstance().updateRcl(StringUtil.removeDoubleQuotes(mWifiInfo.getSSID()), mWifiInfo.getFrequency(), true);
                        }
                    }
                    break;
                case CMD_ONESHOT_RSSI_POLL:
                    if (!mEnableRssiPolling) {
                        updateLinkLayerStatsRssiAndScoreReportInternal();
                    }
                    break;
                case CMD_RSSI_POLL:
                    if (message.arg1 == mRssiPollToken) {
                        WifiLinkLayerStats stats = updateLinkLayerStatsRssiAndScoreReportInternal();
                        mWifiMetrics.updateWifiUsabilityStatsEntries(mWifiInfo, stats);
                        if (mWifiScoreReport.shouldCheckIpLayer()) {
                            if (mIpClient != null) {
                                mIpClient.confirmConfiguration();
                            }
                            mWifiScoreReport.noteIpCheck();
                        }
                        int statusDataStall =
                                mWifiDataStall.checkForDataStall(mLastLinkLayerStats, stats);
                        if (statusDataStall != WifiIsUnusableEvent.TYPE_UNKNOWN) {
                            mWifiMetrics.addToWifiUsabilityStatsList(WifiUsabilityStats.LABEL_BAD,
                                    convertToUsabilityStatsTriggerType(statusDataStall), -1);
                        }
                        mWifiMetrics.incrementWifiLinkLayerUsageStats(stats);
                        mLastLinkLayerStats = stats;
                        mWifiScoreCard.noteSignalPoll(mWifiInfo);
                        mLinkProbeManager.updateConnectionStats(
                                mWifiInfo, mInterfaceName);
                        sendMessageDelayed(obtainMessage(CMD_RSSI_POLL, mRssiPollToken, 0),
                                mPollRssiIntervalMsecs);
                        if (mVerboseLoggingEnabled) sendRssiChangeBroadcast(mWifiInfo.getRssi());
                        mWifiTrafficPoller.notifyOnDataActivity(mWifiInfo.txSuccess,
                                mWifiInfo.rxSuccess);
                        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CUSTOMIZATION_SDK) {
                            knoxAutoSwitchPolicy(mWifiInfo.getRssi());
                        }
                    } else {
                        // Polling has completed
                    }
                    break;
                case CMD_ENABLE_RSSI_POLL:
                    cleanWifiScore();
                    mEnableRssiPolling = (message.arg1 == 1);
                    mRssiPollToken++;
                    if (mEnableRssiPolling) {
                        // First poll
                        mLastSignalLevel = -1;
                        mLinkProbeManager.resetOnScreenTurnedOn();
                        fetchRssiLinkSpeedAndFrequencyNative();
                        sendMessageDelayed(obtainMessage(CMD_RSSI_POLL, mRssiPollToken, 0),
                                mPollRssiIntervalMsecs);
                    }
                    break;
                case WifiManager.RSSI_PKTCNT_FETCH:
                    RssiPacketCountInfo info = new RssiPacketCountInfo();
                    fetchRssiLinkSpeedAndFrequencyNative();
                    info.rssi = mWifiInfo.getRssi();
                    if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CUSTOMIZATION_SDK) {
                        knoxAutoSwitchPolicy(mWifiInfo.getRssi());
                    }
                    WifiNative.TxPacketCounters counters =
                            mWifiNative.getTxPacketCounters(mInterfaceName);
                    if (counters != null) {
                        info.txgood = counters.txSucceeded;
                        info.txbad = counters.txFailed;
                        replyToMessage(message, WifiManager.RSSI_PKTCNT_FETCH_SUCCEEDED, info);
                    } else {
                        replyToMessage(message,
                                WifiManager.RSSI_PKTCNT_FETCH_FAILED, WifiManager.ERROR);
                    }
                    break;
                case CMD_ASSOCIATED_BSSID:
                    if ((String) message.obj == null) {
                        logw("Associated command w/o BSSID");
                        break;
                    }
                    if ((getNetworkDetailedState() == DetailedState.CONNECTED ||
                         getNetworkDetailedState() == DetailedState.VERIFYING_POOR_LINK)) { // SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP
                        log("NOT update Last BSSID");
                    } else {
                        mLastBssid = (String) message.obj;
                        if (mLastBssid != null && (mWifiInfo.getBSSID() == null
                                || !mLastBssid.equals(mWifiInfo.getBSSID()))) {
                            mWifiInfo.setBSSID(mLastBssid);
                            WifiConfiguration config = getCurrentWifiConfiguration();
                            if (config != null) {
                                ScanDetailCache scanDetailCache = mWifiConfigManager
                                        .getScanDetailCacheForNetwork(config.networkId);
                                if (scanDetailCache != null) {
                                    ScanResult scanResult = scanDetailCache.getScanResult(mLastBssid);
                                    if (scanResult != null) {
                                        mWifiInfo.setFrequency(scanResult.frequency);
                                        mWifiInfo.setWifiMode(scanResult.wifiMode); //SEC_PRODUCT_FEATURE_WLAN_WIFIMODE
                                    }
                                }
                            }
                            sendNetworkStateChangeBroadcast(mLastBssid);
                        }
                    }
                    notifyMobilewipsRoamEvent("start");
                    break;
                case CMD_START_RSSI_MONITORING_OFFLOAD:
                case CMD_RSSI_THRESHOLD_BREACHED:
                    byte currRssi = (byte) message.arg1;
                    processRssiThreshold(currRssi, message.what, mRssiEventHandler);
                    break;
                case CMD_STOP_RSSI_MONITORING_OFFLOAD:
                    stopRssiMonitoringOffload();
                    break;
                case CMD_RECONNECT:
                    log(" Ignore CMD_RECONNECT request because wifi is already connected");
                    break;
                case CMD_RESET_SIM_NETWORKS:
                    if (message.arg1 == 0 // sim was removed
                            && mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                        WifiConfiguration config =
                                mWifiConfigManager.getConfiguredNetwork(mLastNetworkId);
                        if (TelephonyUtil.isSimConfig(config)) {
                            mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                                    StaEvent.DISCONNECT_RESET_SIM_NETWORKS);
                            // TODO(b/132385576): STA may immediately connect back to the network
                            //  that we just disconnected from
                            notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    DISCONNECT_REASON_SIM_REMOVED);
                            mWifiNative.disconnect(mInterfaceName);
                            transitionTo(mDisconnectingState);
                        }
                    }
                    /* allow parent state to reset data for other networks */
                    handleStatus = NOT_HANDLED;
                    break;
                case CMD_START_IP_PACKET_OFFLOAD: {
                    int slot = message.arg1;
                    int intervalSeconds = message.arg2;
                    KeepalivePacketData pkt = (KeepalivePacketData) message.obj;
                    int result = startWifiIPPacketOffload(slot, pkt, intervalSeconds);
                    if (mNetworkAgent != null) {
                        mNetworkAgent.onSocketKeepaliveEvent(slot, result);
                    }
                    break;
                }
                case CMD_ADD_KEEPALIVE_PACKET_FILTER_TO_APF: {
                    if (mIpClient != null) {
                        final int slot = message.arg1;
                        if (message.obj instanceof NattKeepalivePacketData) {
                            final NattKeepalivePacketData pkt =
                                    (NattKeepalivePacketData) message.obj;
                            mIpClient.addKeepalivePacketFilter(slot, pkt);
                        } else if (message.obj instanceof TcpKeepalivePacketData) {
                            final TcpKeepalivePacketData pkt =
                                    (TcpKeepalivePacketData) message.obj;
                            mIpClient.addKeepalivePacketFilter(slot, pkt);
                        }
                    }
                    break;
                }
                case CMD_REMOVE_KEEPALIVE_PACKET_FILTER_FROM_APF: {
                    if (mIpClient != null) {
                        mIpClient.removeKeepalivePacketFilter(message.arg1);
                    }
                    break;
                }
                case CMD_REPLACE_PUBLIC_DNS:
                    if (mLinkProperties != null) {
                        String publicDnsIp = ((Bundle) message.obj).getString("publicDnsServer");
                        ArrayList<InetAddress> dnsList = new ArrayList<>(mLinkProperties.getDnsServers());
                        dnsList.add(NetworkUtils.numericToInetAddress(publicDnsIp));
                        mLinkProperties.setDnsServers(dnsList);
                        updateLinkProperties(mLinkProperties);
                    }
                    break;

                default:
                    handleStatus = NOT_HANDLED;
                    break;
            }

            if (handleStatus == HANDLED) {
                logStateAndMessage(message, this);
            }

            return handleStatus;
        }

        /**
         * Fetches link stats and updates Wifi Score Report.
         */
        private WifiLinkLayerStats updateLinkLayerStatsRssiAndScoreReportInternal() {
            WifiLinkLayerStats stats = getWifiLinkLayerStats();
            // Get Info and continue polling
            fetchRssiLinkSpeedAndFrequencyNative();
            // Send the update score to network agent.
            mWifiScoreReport.calculateAndReportScore(mWifiInfo, mNetworkAgent, mWifiMetrics);
            return stats;
        }
    }

    /**
     * Fetches link stats and updates Wifi Score Report.
     */
    public void updateLinkLayerStatsRssiAndScoreReport() {
        sendMessage(CMD_ONESHOT_RSSI_POLL);
    }

    private static int convertToUsabilityStatsTriggerType(int unusableEventTriggerType) {
        int triggerType;
        switch (unusableEventTriggerType) {
            case WifiIsUnusableEvent.TYPE_DATA_STALL_BAD_TX:
                triggerType = WifiUsabilityStats.TYPE_DATA_STALL_BAD_TX;
                break;
            case WifiIsUnusableEvent.TYPE_DATA_STALL_TX_WITHOUT_RX:
                triggerType = WifiUsabilityStats.TYPE_DATA_STALL_TX_WITHOUT_RX;
                break;
            case WifiIsUnusableEvent.TYPE_DATA_STALL_BOTH:
                triggerType = WifiUsabilityStats.TYPE_DATA_STALL_BOTH;
                break;
            case WifiIsUnusableEvent.TYPE_FIRMWARE_ALERT:
                triggerType = WifiUsabilityStats.TYPE_FIRMWARE_ALERT;
                break;
            case WifiIsUnusableEvent.TYPE_IP_REACHABILITY_LOST:
                triggerType = WifiUsabilityStats.TYPE_IP_REACHABILITY_LOST;
                break;
            default:
                triggerType = WifiUsabilityStats.TYPE_UNKNOWN;
                Log.e(TAG, "Unknown WifiIsUnusableEvent: " + unusableEventTriggerType);
        }
        return triggerType;
    }

    class ObtainingIpState extends State {
        @Override
        public void enter() {
            // mScanRequestProxy.enableScanning(false, false, "START_DHCP"); //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX
            final WifiConfiguration currentConfig = getCurrentWifiConfiguration();
            final boolean isUsingStaticIp =
                    (currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC);
            if (mVerboseLoggingEnabled) {
                final String key = currentConfig.configKey();
                log("enter ObtainingIpState netId=" + Integer.toString(mLastNetworkId)
                        + " " + key + " "
                        + " roam=" + mIsAutoRoaming
                        + " static=" + isUsingStaticIp);
            }

            // Send event to CM & network change broadcast
            setNetworkDetailedState(DetailedState.OBTAINING_IPADDR);

            // We must clear the config BSSID, as the wifi chipset may decide to roam
            // from this point on and having the BSSID specified in the network block would
            // cause the roam to fail and the device to disconnect.
            clearTargetBssid("ObtainingIpAddress");

            // Stop IpClient in case we're switching from DHCP to static
            // configuration or vice versa.
            //
            // TODO: Only ever enter this state the first time we connect to a
            // network, never on switching between static configuration and
            // DHCP. When we transition from static configuration to DHCP in
            // particular, we must tell ConnectivityService that we're
            // disconnected, because DHCP might take a long time during which
            // connectivity APIs such as getActiveNetworkInfo should not return
            // CONNECTED.
            stopIpClient();

            setTcpBufferAndProxySettingsForIpManager(); //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX

            final ProvisioningConfiguration prov;
            if (!isUsingStaticIp) {
                prov = new ProvisioningConfiguration.Builder()
                            .withPreDhcpAction()
                            .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                            .withNetwork(getCurrentNetwork())
                            .withDisplayName(currentConfig.SSID)
                            .withRandomMacAddress()
                            .build();
            } else {
                StaticIpConfiguration staticIpConfig = currentConfig.getStaticIpConfiguration();
                prov = new ProvisioningConfiguration.Builder()
                            .withStaticConfiguration(staticIpConfig)
                            .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                            .withNetwork(getCurrentNetwork())
                            .withDisplayName(currentConfig.SSID)
                            .build();
            }
            if (mIpClient != null) {
                mIpClient.startProvisioning(prov);
                // Get Link layer stats so as we get fresh tx packet counters
                getWifiLinkLayerStats();
            } else { //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX
                Log.d(TAG, "IpClient is not ready to use, going back to disconnected state");
                notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                    DISCONNECT_REASON_DHCP_FAIL_WITH_IPCLIENT_ISSUE);
                mWifiNative.disconnect(mInterfaceName);
            }
        }

        @Override
        public boolean processMessage(Message message) {
            boolean handleStatus = HANDLED;

            switch(message.what) {
                case CMD_START_CONNECT:
                    //+SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
                    String bssid = (String) message.obj;
                    if (SUPPLICANT_BSSID_ANY.equals(bssid)) {
                        return NOT_HANDLED;
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
                case CMD_START_ROAM:
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiManager.SAVE_NETWORK:
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DEFERRED;
                    deferMessage(message);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_NETWORK_DISCONNECTION,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE,
                            WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
                    handleStatus = NOT_HANDLED;
                    break;
                case CMD_SET_HIGH_PERF_MODE:
                    mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DEFERRED;
                    deferMessage(message);
                    break;
                default:
                    handleStatus = NOT_HANDLED;
                    break;
            }

            if (handleStatus == HANDLED) {
                logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        @Override
        public void exit() {
            // mScanRequestProxy.enableScanning(true, true, "STOP_DHCP"); //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX
        }
    }

    /**
     * Helper function to check if we need to invoke
     * {@link NetworkAgent#explicitlySelected(boolean, boolean)} to indicate that we connected to a
     * network which the user just chose
     * (i.e less than {@link #LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS) before).
     */
    @VisibleForTesting
    public boolean shouldEvaluateWhetherToSendExplicitlySelected(WifiConfiguration currentConfig) {
        if (currentConfig == null) {
            Log.wtf(TAG, "Current WifiConfiguration is null, but IP provisioning just succeeded");
            return false;
        }
        long currentTimeMillis = mClock.getElapsedSinceBootMillis();
        return (mWifiConfigManager.getLastSelectedNetwork() == currentConfig.networkId
                && currentTimeMillis - mWifiConfigManager.getLastSelectedTimeStamp()
                < LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS);
    }

    private void sendConnectedState() {
        // If this network was explicitly selected by the user, evaluate whether to inform
        // ConnectivityService of that fact so the system can treat it appropriately.
        WifiConfiguration config = getCurrentWifiConfiguration();

        boolean explicitlySelected = false;
        int issuedUid = getIssueUidForConnectingNetwork(config);
        if (shouldEvaluateWhetherToSendExplicitlySelected(config)) {
            // If explicitlySelected is true, the network was selected by the user via Settings or
            // QuickSettings. If this network has Internet access, switch to it. Otherwise, switch
            // to it only if the user confirms that they really want to switch, or has already
            // confirmed and selected "Don't ask again".
            explicitlySelected =
                    mWifiPermissionsUtil.checkNetworkSettingsPermission(issuedUid);
            if (mVerboseLoggingEnabled) {
                log("Network selected by UID " + issuedUid + " explicitlySelected="
                        + explicitlySelected);
            }
        }

        if (mVerboseLoggingEnabled) {
            log("explictlySelected=" + explicitlySelected + " acceptUnvalidated="
                    + config.noInternetAccessExpected);
        }
        // >>>WCM>>>
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager packageManager = mContext.getPackageManager();
        String packageName = packageManager.getNameForUid(issuedUid);
        if (explicitlySelected && (config.isEphemeral() || 
                (packageName != null && NETWORKSETTINGS_PERMISSION_EXCEPTION_PACKAGE_LIST.contains(packageName)))) {
            // smartThings should not treat as app which has Network_Settings permission
            explicitlySelected = false;
            if (mVerboseLoggingEnabled) {
                log("explictlySelected Exception case for smarthings =" + explicitlySelected + " acceptUnvalidated="
                        + config.noInternetAccessExpected);
            }
        }
        Log.d(TAG, "noInternetAccessExpected : " + config.isNoInternetAccessExpected()
                + ", CUid : " + config.creatorUid + ",sLUid : " + config.lastUpdateUid);
        mIsManualSelection = explicitlySelected;
        mCm.setMultiNetwork(false, issuedUid);
        if (mNetworkAgent != null) {
            if (!config.isCaptivePortal && (config.isNoInternetAccessExpected()
                    || (!explicitlySelected && !isPoorNetworkTestEnabled() && 
                    !isMultiNetworkAvailableApp(config.creatorUid, issuedUid, packageName)))) {
                mNetworkAgent.explicitlySelected(true, true);
            } else {
                if (!explicitlySelected && (config.isEphemeral() || 
                        isMultiNetworkAvailableApp(config.creatorUid, issuedUid, packageName))) {
                    log("MultiNetwork - package : " + packageName);
                    mCm.setMultiNetwork(true, issuedUid);
                }
                // <<<WCM<<<
                mNetworkAgent.explicitlySelected(explicitlySelected, config.noInternetAccessExpected);
            }
        }
        setNetworkDetailedState(DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(mLastBssid);

        // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
            if (MobileWipsFrameworkService.getInstance() != null) {
                MobileWipsFrameworkService.getInstance().sendEmptyMessage(MobileWipsDef.EVENT_CONNECTED);
            }
        }
        // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
    }

    // >>>WCM>>>
    public final List<String> NETWORKSETTINGS_PERMISSION_EXCEPTION_PACKAGE_LIST = Arrays.asList(new String[] {
            "com.samsung.android.oneconnect",
            "sdet.pack",
            "sdet.pack.channel"
    });

    public final List<String> MULTINETWORK_EXCEPTION_PACKAGE_LIST = Arrays.asList(new String[]{
            "com.android.systemui",
            "android.uid.systemui",
            "com.samsung.android.app.aodservice",
            "com.sec.android.cover.ledcover",
            "com.samsung.android.app.routines",
            "com.android.systemui",
            "com.samsung.desktopsystemui",
            "com.samsung.android.gesture.MotionRecognitionService",
            "com.android.systemui.sensor.PickupController",
            "com.samsung.uready.agent"
    });
    public final List<String> MULTINETWORK_ALLOWING_SYSTEM_PACKAGE_LIST = Arrays.asList(new String[]{
        "com.samsung.android.oneconnect",
        "com.samsung.android.app.mirrorlink",
        "com.google.android.gms",
        "com.google.android.projection.gearhead"
    });

    public int getIssueUidForConnectingNetwork(WifiConfiguration config) {
        int[] uids = {config.creatorUid, config.lastUpdateUid, config.lastConnectUid};
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager packageManager = mContext.getPackageManager();
        for (int uid : uids) {
            if (uid <= 1010) continue;
            try {
                String packageName = packageManager.getNameForUid(uid);
                if (MULTINETWORK_ALLOWING_SYSTEM_PACKAGE_LIST.contains(packageName)) {
                    return uid;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return config.lastConnectUid >= 1000 ? config.lastConnectUid : config.creatorUid;
    }

    private boolean isMultiNetworkAvailableApp(int cuid, int issuedUid, String packageName) {
        if (cuid <= 1010 || issuedUid <= 1010) {
            if (MULTINETWORK_ALLOWING_SYSTEM_PACKAGE_LIST.contains(packageName)) {
                return true;
            } else {
                return false;
            }
        } 
        if (packageName != null && MULTINETWORK_EXCEPTION_PACKAGE_LIST.contains(packageName)) return false;
        /*
        *if (packageName == null || packageName.equals("android") || MULTINETWORK_EXCEPTION_PACKAGE_LIST.contains(packageName)) return false; 
        *if (MULTINETWORK_ALLOWING_SYSTEM_PACKAGE_LIST.contains(packageName)) return true;
        *boolean isSystemApp = false; 
        *PackageManager pm = mContext.getPackageManager();
        *try {
        *    PackageInfo packageInfo = pm.getPackageInfo(packageName, 0 );// flags
        *    if (uid < 10000 || (packageInfo != null && (packageInfo.applicationInfo.flags & 
        *            (ApplicationInfo.FLAG_SYSTEM |ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)) {
        *        isSystemApp = true;
        *    }
        *} catch (PackageManager.NameNotFoundException e) {
        *    Log.w(TAG, String.format("Could not find package [%s]", packageName), e);
        *    return false;
        *} catch (NullPointerException e) {
        *    Log.w(TAG, "Could not find package NullPointException");
        *    return false;
        *}
        *return !isSystemApp;
        */
        return issuedUid > 1010;
    }
    // <<< WCM <<<

    class RoamingState extends State {
        boolean mAssociated;
        @Override
        public void enter() {
            if (mVerboseLoggingEnabled) {
                log("RoamingState Enter mScreenOn=" + mScreenOn);
            }

            // Make sure we disconnect if roaming fails
            mRoamWatchdogCount++;
            logd("Start Roam Watchdog " + mRoamWatchdogCount);
            sendMessageDelayed(obtainMessage(CMD_ROAM_WATCHDOG_TIMER,
                    mRoamWatchdogCount, 0), ROAM_GUARD_TIMER_MSEC);
            mAssociated = false;

            report(ReportIdKey.ID_ROAMING_TRIGGER, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        ReportUtil.getReportDataForRoamingEnter(
                            "framework-start",
                            mWifiInfo.getSSID(),mWifiInfo.getBSSID(), mWifiInfo.getRssi()));

            mBigDataManager.addOrUpdateValue( // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    WifiBigDataLogManager.LOGGING_TYPE_ROAM_TRIGGER, 1);

            notifyMobilewipsRoamEvent("start");
        }
        @Override
        public boolean processMessage(Message message) {
            WifiConfiguration config;
            boolean handleStatus = HANDLED;

            switch (message.what) {
                case CMD_IP_CONFIGURATION_LOST:
                    config = getCurrentWifiConfiguration();
                    if (config != null) {
                        mWifiDiagnostics.captureBugReportData(
                                WifiDiagnostics.REPORT_REASON_AUTOROAM_FAILURE);
                    }
                    handleStatus = NOT_HANDLED;
                    break;
                case CMD_UNWANTED_NETWORK:
                    if (mVerboseLoggingEnabled) {
                        log("Roaming and CS doesn't want the network -> ignore");
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    /**
                     * If we get a SUPPLICANT_STATE_CHANGE_EVENT indicating a DISCONNECT
                     * before NETWORK_DISCONNECTION_EVENT
                     * And there is an associated BSSID corresponding to our target BSSID, then
                     * we have missed the network disconnection, transition to mDisconnectedState
                     * and handle the rest of the events there.
                     */
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (stateChangeResult.state == SupplicantState.DISCONNECTED
                            || stateChangeResult.state == SupplicantState.INACTIVE
                            || stateChangeResult.state == SupplicantState.INTERFACE_DISABLED) {
                        if (mVerboseLoggingEnabled) {
                            log("STATE_CHANGE_EVENT in roaming state "
                                    + stateChangeResult.toString());
                        }
                        if (stateChangeResult.BSSID != null
                                && stateChangeResult.BSSID.equals(mTargetRoamBSSID)) {
                            notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    DISCONNECT_REASON_ROAM_FAIL);
                            //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
                            if (mIWCMonitorChannel != null) {
                                mIWCMonitorChannel.sendMessage(IWCMonitor.IWC_WIFI_DISCONNECTED, 0);
                            }
                            //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
                            handleNetworkDisconnect();
                            transitionTo(mDisconnectedState);
                        }
                    }
                    if (stateChangeResult.state == SupplicantState.ASSOCIATED) {
                        // We completed the layer2 roaming part
                        mAssociated = true;
                        if (stateChangeResult.BSSID != null) {
                            mTargetRoamBSSID = stateChangeResult.BSSID;
                        }
                    }
                    break;
                case CMD_ROAM_WATCHDOG_TIMER:
                    if (mRoamWatchdogCount == message.arg1) {
                        if (mVerboseLoggingEnabled) log("roaming watchdog! -> disconnect");
                        mWifiMetrics.endConnectionEvent(
                                WifiMetrics.ConnectionEvent.FAILURE_ROAM_TIMEOUT,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE,
                                WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
                        mRoamFailCount++;
                        handleNetworkDisconnect();
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                                StaEvent.DISCONNECT_ROAM_WATCHDOG_TIMER);
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_ROAM_TIMEOUT);
                        mWifiNative.disconnect(mInterfaceName);
                        transitionTo(mDisconnectedState);
                    }
                    break;
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    if (mAssociated) {
                        if (mVerboseLoggingEnabled) {
                            log("roaming and Network connection established");
                        }
                        setRoamTriggered(true);
                        mLastNetworkId = message.arg1;
                        mLastConnectedNetworkId = mLastNetworkId; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                        mLastBssid = (String) message.obj;
                        mWifiInfo.setBSSID(mLastBssid);
                        mWifiInfo.setNetworkId(mLastNetworkId);
                        int reasonCode = message.arg2;
                        mWifiConnectivityManager.trackBssid(mLastBssid, true, reasonCode);
                        sendNetworkStateChangeBroadcast(mLastBssid);

                        // Successful framework roam! (probably)
                        reportConnectionAttemptEnd(
                                WifiMetrics.ConnectionEvent.FAILURE_NONE,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE,
                                WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);

                        report(ReportIdKey.ID_ROAMING_TRIGGER, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                ReportUtil.getReportDataForRoamingEnter(
                                        "framework-completed",
                                        mWifiInfo.getSSID(),mWifiInfo.getBSSID(), mWifiInfo.getRssi()));

                        // We must clear the config BSSID, as the wifi chipset may decide to roam
                        // from this point on and having the BSSID specified by QNS would cause
                        // the roam to fail and the device to disconnect.
                        // When transition from RoamingState to DisconnectingState or
                        // DisconnectedState, the config BSSID is cleared by
                        // handleNetworkDisconnect().
                        clearTargetBssid("RoamingCompleted");

                        // We used to transition to ObtainingIpState in an
                        // attempt to do DHCPv4 RENEWs on framework roams.
                        // DHCP can take too long to time out, and we now rely
                        // upon IpClient's use of IpReachabilityMonitor to
                        // confirm our current network configuration.
                        //
                        // mIpClient.confirmConfiguration() is called within
                        // the handling of SupplicantState.COMPLETED.

                        //+SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP,SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
                        int tmpDhcpRenewAfterRoamingMode = getRoamDhcpPolicy();
                        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
                        boolean isUsingStaticIp = false;
                        if (currentConfig != null) {
                            isUsingStaticIp = (currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC);
                        }
                        if (isUsingStaticIp) {
                            log(" Static ip - skip renew");
                            tmpDhcpRenewAfterRoamingMode = -1;
                        }
                        if (mRoamDhcpPolicyByB2bConfig == ROAM_DHCP_SKIP || tmpDhcpRenewAfterRoamingMode == ROAM_DHCP_SKIP
                                ||  tmpDhcpRenewAfterRoamingMode == -1) {
                            log(" Skip Dhcp - mRoamDhcpPolicyByB2bConfig:" + mRoamDhcpPolicyByB2bConfig 
                                    + " ,tmpDhcpRenewAfterRoamingMode : " + tmpDhcpRenewAfterRoamingMode);
                            setRoamTriggered(false);
                        } else {
                            if (checkIfForceRestartDhcp()) {
                                restartDhcp(currentConfig);
                            } else {
                                CheckIfDefaultGatewaySame();
                            }
                        }

                        transitionTo(mConnectedState);
                    } else {
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    }
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    // Throw away but only if it corresponds to the network we're roaming to
                    String bssid = (String) message.obj;
                    if (true) {
                        String target = "";
                        if (mTargetRoamBSSID != null) target = mTargetRoamBSSID;
                        log("NETWORK_DISCONNECTION_EVENT in roaming state"
                                + " BSSID=" + bssid
                                + " target=" + target);
                    }
                    if (bssid != null && bssid.equals(mTargetRoamBSSID)) {
                        handleNetworkDisconnect();
                        transitionTo(mDisconnectedState);
                    }
                    break;
                default:
                    handleStatus = NOT_HANDLED;
                    break;
            }

            if (handleStatus == HANDLED) {
                logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        @Override
        public void exit() {
            logd("ClientModeImpl: Leaving Roaming state");

            mBigDataManager.addOrUpdateValue( // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    WifiBigDataLogManager.LOGGING_TYPE_ROAM_TRIGGER, 0);
        }
    }

    class ConnectedState extends State {
        private WifiConfiguration mCurrentConfig;   // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20, SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE

        @Override
        public void enter() {
            if (mVerboseLoggingEnabled) {
                log("Enter ConnectedState  mScreenOn=" + mScreenOn);
            }
            //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
            mLastConnectedTime = mClock.getElapsedSinceBootMillis();
            //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }

            reportConnectionAttemptEnd(
                    WifiMetrics.ConnectionEvent.FAILURE_NONE,
                    WifiMetricsProto.ConnectionEvent.HLF_NONE,
                    WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
            mWifiConnectivityManager.handleConnectionStateChanged(
                    WifiConnectivityManager.WIFI_STATE_CONNECTED);
            registerConnected();
            mLastConnectAttemptTimestamp = 0;
            mTargetWifiConfiguration = null;
            mWifiScoreReport.reset();
            mLastSignalLevel = -1;

            // Not roaming anymore
            mIsAutoRoaming = false;
            mWifiConfigManager.setCurrentNetworkId(mTargetNetworkId); //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS

            mLastDriverRoamAttempt = 0;
            mTargetNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
            mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(true);
            mWifiStateTracker.updateState(WifiStateTracker.CONNECTED);
            mCurrentConfig = getCurrentWifiConfiguration(); // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20, SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE

            if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
                if (mCurrentConfig != null && mCurrentConfig.isPasspoint()
                    && Settings.Secure.getInt(mContext.getContentResolver(),Settings.Secure.WIFI_HOTSPOT20_CONNECTED_HISTORY, 0) == 0) {
                     Log.d(TAG, "ConnectedState, WIFI_HOTSPOT20_CONNECTED_HISTORY    is set to 1");
                     Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_CONNECTED_HISTORY, 1);
                }
            }
            if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
                if (mCurrentConfig != null && mCurrentConfig.SSID != null) {
                    if (mCurrentConfig.allowedKeyManagement.get(KeyMgmt.NONE) ||
                        mCurrentConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK) ||
                        mCurrentConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK) ||
                        mCurrentConfig.allowedKeyManagement.get(KeyMgmt.WAPI_PSK)) {
                        WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                            true, TAG, "Wi-Fi is connected to " + mCurrentConfig.getPrintableSsid() + " network using 802.11-2012 channel");
                    } else {
                        if (mCurrentConfig.enterpriseConfig != null) {
                            if (mCurrentConfig.enterpriseConfig.getEapMethod() == WifiEnterpriseConfig.Eap.TLS) {
                                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                                    true, TAG, "Wi-Fi is connected to " + mCurrentConfig.getPrintableSsid() + " network using EAP-TLS channel");
                            } else {
                                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                                    true, TAG, "Wi-Fi is connected to " + mCurrentConfig.getPrintableSsid() + " network using 802.1X channel");
                            }
                        }
                    }
                }
            }

            //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
            ReportUtil.startTimerDuringConnection();
            ReportUtil.updateWifiInfo(mWifiInfo);
            if (mCurrentConfig != null) {
                if (mCurrentConfig.semIsVendorSpecificSsid) {
                    mConnectedApInternalType = 4;
                } else if (mCurrentConfig.semSamsungSpecificFlags.get(
                        WifiConfiguration.SamsungFlag.SEC_MOBILE_AP)) {
                    mConnectedApInternalType = 3;
                } else if (mCurrentConfig.isPasspoint()) {
                    mConnectedApInternalType = 2;
                } else if (mCurrentConfig.semIsWeChatAp) {
                    mConnectedApInternalType = 1;
                } else if (mCurrentConfig.isCaptivePortal) {
                    mConnectedApInternalType = 6;
                } else if (mCurrentConfig.semAutoWifiScore > 4) {
                    mConnectedApInternalType = 5;
                } else if (mCurrentConfig.isEphemeral()) {
                    mConnectedApInternalType = 7;
                } else {
                    mConnectedApInternalType = 0;
                }
                mBigDataManager.addOrUpdateValue( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                        WifiBigDataLogManager.LOGGING_TYPE_CONFIG_NETWORK_TYPE,
                        mConnectedApInternalType);
            }
            mBigDataManager.addOrUpdateValue(
                    WifiBigDataLogManager.LOGGING_TYPE_UPDATE_DATA_RATE, 0);
            mBigDataManager.addOrUpdateValue(
                    WifiBigDataLogManager.LOGGING_TYPE_SET_CONNECTION_START_TIME, 0);

            report(ReportIdKey.ID_CONNECTED,
                    ReportUtil.getReportDataForConnectTranstion(mConnectedApInternalType));

            if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                if (mWifiGeofenceManager.isValidAccessPointToUseGeofence(mWifiInfo, mCurrentConfig)) {
                    mWifiGeofenceManager.triggerStartLearning(mCurrentConfig);
                }
            }

            //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
            WifiConfiguration config = getCurrentWifiConfiguration();
            if (isLocationSupportedAp(config)) {
                double[] latitudeLongitude = getLatitudeLongitude(config);
                if (latitudeLongitude[0] != INVALID_LATITUDE_LONGITUDE
                        && latitudeLongitude[1] != INVALID_LATITUDE_LONGITUDE) {
                    sendMessage(CMD_UPDATE_CONFIG_LOCATION, 0);
                } else {
                    mLocationRequestNetworkId = config.networkId;
                    sendMessageDelayed(CMD_UPDATE_CONFIG_LOCATION, ACTIVE_REQUEST_LOCATION, 10 * 1000);
                }
            }
            //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION

            //Inform WifiLockManager
            WifiLockManager wifiLockManager = mWifiInjector.getWifiLockManager();
            wifiLockManager.updateWifiClientConnected(true);
            if (CSC_SUPPORT_5G_ANT_SHARE)
                sendIpcMessageToRilForLteu(LTEU_STA_5GHZ_CONNECTED, true, mWifiInfo.is5GHz(), false);

            if(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO)
                handleCellularCapabilities(true); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO

            updateEDMWiFiPolicy();  //Start EDM Wi-Fi Configuration

            if (WifiRoamingAssistant.getInstance() != null) {
                WifiRoamingAssistant.getInstance().updateRcl(StringUtil.removeDoubleQuotes(mWifiInfo.getSSID()), mWifiInfo.getFrequency(), true);
            }
        }
        @Override
        public boolean processMessage(Message message) {
            WifiConfiguration config = null;
            boolean handleStatus = HANDLED;

            switch (message.what) {
                case CMD_UPDATE_CONFIG_LOCATION: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
                    updateLocation(message.arg1);
                    break;
                case CMD_CHECK_ARP_RESULT: { //SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP
                    removeMessages(CMD_SEND_ARP);
                    boolean arpSucceed;
                    arpSucceed = WifiChipInfo.getInstance().getArpResult();
                    if(!arpSucceed) {
                        restartDhcp(getCurrentWifiConfiguration());
                    } else {
                        setRoamTriggered(false);
                    }
                    break;
                }
                case CMD_SEND_ARP: { //SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP
                    if (mIpClient != null) {
                        mIpClient.confirmConfiguration();
                        sendMessageDelayed(CMD_SEND_ARP, 500);
                    }
                    break;
                }
                case CMD_SEND_DHCP_RELEASE: // CscFeature_Wifi_SendSignalDuringPowerOff
                    if (mIpClient != null) {
                        mIpClient.sendDhcpReleasePacket();
                        if (waitForDhcpRelease() != 0) {
                            loge("waitForDhcpRelease error");
                        } else {
                            loge("waitForDhcpRelease() Success");
                        }
                    }

                    break;
                case CMD_UNWANTED_NETWORK:
                    report(ReportIdKey.ID_UNWANTED, //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                                ReportUtil.getReportDataForUnwantedMessage(message.arg1));
                    if (message.arg1 == NETWORK_STATUS_UNWANTED_DISCONNECT) {
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                                StaEvent.DISCONNECT_UNWANTED);
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_UNWANTED);
                        mWifiNative.disconnect(mInterfaceName);
                        transitionTo(mDisconnectingState);
                    } else if (message.arg1 == NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN
                            || message.arg1 == NETWORK_STATUS_UNWANTED_VALIDATION_FAILED) {
                        Log.d(TAG, (message.arg1 == NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN
                                ? "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN"
                                : "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED"));
                        config = getCurrentWifiConfiguration();
                        if (config != null) {
                            mWifiConfigManager.setNetworkValidatedInternetAccess(
                                    config.networkId, false);
                            // Disable autojoin
                            if (message.arg1 == NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN) {
                                mWifiConfigManager.updateNetworkSelectionStatus(config.networkId,
                                        WifiConfiguration.NetworkSelectionStatus
                                        .DISABLED_NO_INTERNET_PERMANENT);
                            } else {
                                // stop collect last-mile stats since validation fail
                                removeMessages(CMD_DIAGS_CONNECT_TIMEOUT);
                                mWifiDiagnostics.reportConnectionEvent(
                                        WifiDiagnostics.CONNECTION_EVENT_FAILED);
                                mWifiConfigManager.incrementNetworkNoInternetAccessReports(
                                        config.networkId);
                                // If this was not the last selected network, update network
                                // selection status to temporarily disable the network.
                                if (!isWCMEnabled() || isWifiOnly()) { // WCM
                                    if (mWifiConfigManager.getLastSelectedNetwork() != config.networkId
                                            && !config.noInternetAccessExpected) {
                                        Log.i(TAG, "Temporarily disabling network because of"
                                                + "no-internet access");
                                        mWifiConfigManager.updateNetworkSelectionStatus(
                                                config.networkId,
                                                WifiConfiguration.NetworkSelectionStatus
                                                        .DISABLED_NO_INTERNET_TEMPORARY);
                                    }
                                }
                            }
                        }
                    }
                    if ((mWlanAdvancedDebugState & WLAN_ADVANCED_DEBUG_DISC) != 0) { // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLAN_ADVANCED_DEBUG
                        sendBroadcastIssueTrackerSysDump(ISSUE_TRACKER_SYSDUMP_UNWANTED);
                    }
                    break;
                case CMD_NETWORK_STATUS:
                    if (message.arg1 == NetworkAgent.VALID_NETWORK) {
                        // stop collect last-mile stats since validation pass
                        removeMessages(CMD_DIAGS_CONNECT_TIMEOUT);
                        mWifiDiagnostics.reportConnectionEvent(
                                WifiDiagnostics.CONNECTION_EVENT_SUCCEEDED);
                        mWifiScoreCard.noteValidationSuccess(mWifiInfo);
                        config = getCurrentWifiConfiguration();
                        if (config != null) {
                            // re-enable autojoin
                            mWifiConfigManager.updateNetworkSelectionStatus(
                                    config.networkId,
                                    WifiConfiguration.NetworkSelectionStatus
                                            .NETWORK_SELECTION_ENABLE);
                            mWifiConfigManager.setNetworkValidatedInternetAccess(
                                    config.networkId, true);
                        }
                    }
                    break;
                case CMD_ACCEPT_UNVALIDATED:
                    boolean accept = (message.arg1 != 0);
                    mWifiConfigManager.setNetworkNoInternetAccessExpected(mLastNetworkId, accept);
                    sendWcmConfigurationChanged(); // WCM
                    break;
                case CMD_ASSOCIATED_BSSID:
                    // ASSOCIATING to a new BSSID while already connected, indicates
                    // that driver is roaming
                    mLastDriverRoamAttempt = mClock.getWallClockMillis();
                    report(ReportIdKey.ID_ROAMING_TRIGGER, //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                            ReportUtil.getReportDataForRoamingEnter(
                                "dongle",
                                mWifiInfo.getSSID(),
                                (String) message.obj, mWifiInfo.getRssi()));
                    handleStatus = NOT_HANDLED;
                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                            && "".equals(CONFIG_SECURE_SVC_INTEGRATION) && !(SemCscFeature.getInstance()
                                    .getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                        if (MobileWipsFrameworkService.getInstance() != null) {
                            MobileWipsFrameworkService.getInstance().sendEmptyMessage(MobileWipsDef.EVENT_ROAMING);
                        }
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    long lastRoam = 0;
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_NETWORK_DISCONNECTION,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE,
                            WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
                    if (mLastDriverRoamAttempt != 0) {
                        // Calculate time since last driver roam attempt
                        lastRoam = mClock.getWallClockMillis() - mLastDriverRoamAttempt;
                        mLastDriverRoamAttempt = 0;
                    }
                    if (unexpectedDisconnectedReason(message.arg2)) {
                        mWifiDiagnostics.captureBugReportData(
                                WifiDiagnostics.REPORT_REASON_UNEXPECTED_DISCONNECT);
                    }
                    config = getCurrentWifiConfiguration();

                    Log.d(TAG, "disconnected reason " + message.arg2);
                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
                    if (mIWCMonitorChannel != null) {
                        mIWCMonitorChannel.sendMessage(IWCMonitor.IWC_WIFI_DISCONNECTED, message.arg2);
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
                    //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                    if (mUnstableApController != null && config != null) {
                        if (mUnstableApController.disconnect(
                                mWifiInfo.getBSSID(),
                                mWifiInfo.getRssi(), config, message.arg2)) {
                            report(ReportIdKey.ID_UNSTABLE_AP_DETECTED,
                                    ReportUtil.getReportDataForUnstableAp(config.networkId,
                                            mWifiInfo.getBSSID()));
                        }
                    }

                    if (mVerboseLoggingEnabled) {
                        log("NETWORK_DISCONNECTION_EVENT in connected state"
                                + " BSSID=" + mWifiInfo.getBSSID()
                                + " RSSI=" + mWifiInfo.getRssi()
                                + " freq=" + mWifiInfo.getFrequency()
                                + " reason=" + message.arg2
                                + " Network Selection Status=" + (config == null ? "Unavailable"
                                    : config.getNetworkSelectionStatus().getNetworkStatusString()));
                    }
                    if ((mWlanAdvancedDebugState & WLAN_ADVANCED_DEBUG_DISC) != 0) { // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLAN_ADVANCED_DEBUG
                        sendBroadcastIssueTrackerSysDump(ISSUE_TRACKER_SYSDUMP_DISC);
                    }
                    break;
                case CMD_START_ROAM:
                    // Clear the driver roam indication since we are attempting a framework roam
                    mLastDriverRoamAttempt = 0;

                    /* Connect command coming from auto-join */
                    int netId = message.arg1;
                    ScanResult candidate = (ScanResult) message.obj;
                    String bssid = SUPPLICANT_BSSID_ANY;
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    config = mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId);
                    if (config == null) {
                        loge("CMD_START_ROAM and no config, bail out...");
                        break;
                    }
                    mWifiScoreCard.noteConnectionAttempt(mWifiInfo);
                    setTargetBssid(config, bssid);
                    mTargetNetworkId = netId;

                    logd("CMD_START_ROAM sup state "
                            + mSupplicantStateTracker.getSupplicantStateName()
                            + " my state " + getCurrentState().getName()
                            + " nid=" + Integer.toString(netId)
                            + " config " + config.configKey()
                            + " targetRoamBSSID " + mTargetRoamBSSID);

                    reportConnectionAttemptStart(config, mTargetRoamBSSID,
                            WifiMetricsProto.ConnectionEvent.ROAM_ENTERPRISE);
                    if (mWifiNative.roamToNetwork(mInterfaceName, config)) {
                        mLastConnectAttemptTimestamp = mClock.getWallClockMillis();
                        mTargetWifiConfiguration = config;
                        mIsAutoRoaming = true;
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_CMD_START_ROAM, config);
                        transitionTo(mRoamingState);
                    } else {
                        loge("CMD_START_ROAM Failed to start roaming to network " + config);
                        reportConnectionAttemptEnd(
                                WifiMetrics.ConnectionEvent.FAILURE_CONNECT_NETWORK_FAILED,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE,
                                WifiMetricsProto.ConnectionEvent.FAILURE_REASON_UNKNOWN);
                        replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                WifiManager.ERROR);
                        mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                        break;
                    }
                    break;
                // >>>WCM>>>
                case WifiConnectivityMonitor.CHECK_ALTERNATIVE_NETWORKS:
                    Log.d(TAG, "CONNECTED : CHECK_ALTERNATIVE_NETWORKS");
                
                    /*Internet available AP may be missed in scanResult at the disconnect time.
                    when supplicant updates new scan result, check again.*/

                    boolean needRoamingInHighQuality = (message.arg1 == 1);
                    WifiConfiguration usableInternetConfig = mWifiConfigManager.getConfiguredNetwork(mWifiInfo.getNetworkId());
                    boolean alternativeNetworkFound = false;
                    final List<ScanResult> scanResults = new ArrayList<>();
                    scanResults.addAll(mScanRequestProxy.getScanResults());
                    List<WifiConfiguration> configs = mWifiConfigManager.getSavedNetworks(Process.WIFI_UID);
                    boolean needRoam = false;
                    boolean isRoamingNetwork = false;
                    config = getCurrentWifiConfiguration(); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_NETWORK_EVALUATOR
                
                    ScanResult bestCandidate = null;
                    int bestCandidateNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
                    if (scanResults != null) {
                        // Find Roaming Target BSSID
                        WifiConfiguration currConfig = mWifiConfigManager.getConfiguredNetwork(mWifiInfo.getNetworkId());
                        int candidateCount = 0;
                        if (currConfig != null) {
                            String configSsid = currConfig.SSID;
                            int configuredSecurity = -1;
                            if (currConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK)) {
                                configuredSecurity = SECURITY_PSK;
                            } else if (currConfig.allowedKeyManagement.get(KeyMgmt.SAE)) {
                                configuredSecurity = SECURITY_SAE;
                            } else if (currConfig.allowedKeyManagement.get(KeyMgmt.WPA_EAP)
                                    || currConfig.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
                                configuredSecurity = SECURITY_EAP;
                            } else {
                                configuredSecurity = (currConfig.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
                            }
                            //ScanResult bestCandidate = null;
                            for (ScanResult scanResult : scanResults) {
                                int scanedSecurity = SECURITY_NONE;
                                if (scanResult.capabilities.contains("WEP")) {
                                    scanedSecurity = SECURITY_WEP;
                                } else if (scanResult.capabilities.contains("SAE")) {
                                    scanedSecurity = SECURITY_SAE;
                                } else if (scanResult.capabilities.contains("PSK")) {
                                    scanedSecurity = SECURITY_PSK;
                                } else if (scanResult.capabilities.contains("EAP")) {
                                    scanedSecurity = SECURITY_EAP;
                                }
                                if (scanResult.SSID != null && configSsid != null && configSsid.length() > 2
                                        && scanResult.SSID.equals(configSsid.substring(1, configSsid.length() - 1))
                                        && (configuredSecurity == scanedSecurity)
                                        && (currConfig.isCaptivePortal == false)
                                        && ((scanResult.is24GHz() && (scanResult.level > -64)) //signal level 4.
                                                || (scanResult.is5GHz() && (scanResult.level > -70)))) {
                                    if (scanResult.BSSID != null && !scanResult.BSSID.equals(mWifiInfo.getBSSID())) {
                                        if (bestCandidate == null) {
                                            bestCandidate = scanResult;
                                            bestCandidateNetworkId = currConfig.networkId;
                                        } else if (bestCandidate.level < scanResult.level) {
                                            bestCandidate = scanResult;
                                            bestCandidateNetworkId = currConfig.networkId;
                                        }
                                        candidateCount++;
                                    }
                                }
                            }
                            if (bestCandidate != null && bestCandidate.BSSID != null
                                    && (bestCandidate.level > mWifiInfo.getRssi() + 5)) { // 5 dB delta to prevent ping-pong
                                needRoam = true;
                                Log.d(TAG, "There's available BSSID to roam to. Reassociate to the BSSID. "
                                        + (DBG ? bestCandidate.toString() : ""));
                            }
                            isRoamingNetwork = (candidateCount > 0) ? true : false;
                        }
                
                        // Find Alternative Network
                        if (!needRoam && configs != null) {
                            for (WifiConfiguration conf : configs) {
                                if (conf.validatedInternetAccess && conf.semAutoReconnect == 1) {
                                    String configSsid = conf.SSID;
                                    int configuredSecurity = -1;
                                    if (conf.allowedKeyManagement.get(KeyMgmt.WPA_PSK)
                                            || conf.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
                                        configuredSecurity = SECURITY_PSK;
                                    } else if (conf.allowedKeyManagement.get(KeyMgmt.SAE)) {
                                        configuredSecurity = SECURITY_SAE;
                                    } else if (conf.allowedKeyManagement.get(KeyMgmt.WPA_EAP)
                                            || conf.allowedKeyManagement.get(KeyMgmt.IEEE8021X)) {
                                        configuredSecurity = SECURITY_EAP;
                                    } else {
                                        configuredSecurity = (conf.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
                                    }
                                    for (ScanResult scanResult : scanResults) {
                
                                        int scanedSecurity = SECURITY_NONE;
                                        if (scanResult.capabilities.contains("WEP")) {
                                            scanedSecurity = SECURITY_WEP;
                                        } else if (scanResult.capabilities.contains("SAE")) {
                                            scanedSecurity = SECURITY_SAE;
                                        } else if (scanResult.capabilities.contains("PSK")) {
                                            scanedSecurity = SECURITY_PSK;
                                        } else if (scanResult.capabilities.contains("EAP")) {
                                            scanedSecurity = SECURITY_EAP;
                                        }
                
                                        if ((mWifiInfo.getNetworkId() != conf.networkId) && configSsid != null && configSsid.length() > 2
                                                && scanResult.SSID.equals(configSsid.substring(1, configSsid.length() - 1))
                                                && (configuredSecurity == scanedSecurity)
                                                && (conf.getNetworkSelectionStatus().isNetworkEnabled())
                                                && (conf.isCaptivePortal == false)
                                                && ((scanResult.is24GHz() && (scanResult.level > -64)) //signal level 4.
                                                        || (scanResult.is5GHz() && (scanResult.level > -70)))) {
                                            Log.d(TAG, "There's internet available AP. Disable current AP. "
                                                    + (DBG ? configSsid : ""));
                                            alternativeNetworkFound = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                
                    if (!needRoamingInHighQuality) {
                        if (needRoam) {
                            startRoamToNetwork(bestCandidateNetworkId, bestCandidate);
                        } else if (alternativeNetworkFound) {
                            // Can't reach Internet. Update 'no internet' flag to prevent auto-connect.
                            // Q porting if (ENBLE_WLAN_CONFIG_ANALYTICS) setAnalyticsNoInternetDisconnectReason(mWcmNoInternetReason);
                
                            if (config != null) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_NETWORK_EVALUATOR
                                Log.d(TAG, "Current config's validatedInternetAccess sets as false because alternativeNetwork is Found.");
                                config.validatedInternetAccess = false;
                            }

                            if (config.getNetworkSelectionStatus().getNetworkSelectionStatus()
                                    != WifiConfiguration.NetworkSelectionStatus.NETWORK_SELECTION_PERMANENTLY_DISABLED) {
                                // Disconnect and disable the current network temporarily
                                mWifiConfigManager.updateNetworkSelectionStatus(
                                        config.networkId,
                                        WifiConfiguration.NetworkSelectionStatus.DISABLED_POOR_LINK);
                                Log.d(TAG, "Disable the current network temporarily. DISABLED_POOR_LINK");
                            } else {
                                // Already permanently disabled. Disaconnect from the current network
                                disconnectCommand(0, DISCONNECT_REASON_NO_INTERNET);
                                Log.d(TAG, "Already permanently disabled");
                            }
                        }
                    } else {
                        if (needRoam) {
                            startRoamToNetwork(bestCandidateNetworkId, bestCandidate);
                        }
                        for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                            wncb.handleResultRoamInLevel1State(needRoam);
                        }
                    }
                    return HANDLED;
                
                // <<<WCM<<<

                default:
                    handleStatus = NOT_HANDLED;
                    break;
            }

            if (handleStatus == HANDLED) {
                logStateAndMessage(message, this);
            }

            return handleStatus;
        }

        @Override
        public void exit() {
            logd("ClientModeImpl: Leaving Connected state");
            //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
            mLastConnectedTime = -1;
            //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }
            mWifiConnectivityManager.handleConnectionStateChanged(
                     WifiConnectivityManager.WIFI_STATE_TRANSITIONING);

            mLastDriverRoamAttempt = 0;
            mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(false);

            mDelayDisconnect.setEnable(false, 0); //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION

            mWifiConfigManager.setCurrentNetworkId(-1); //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS
            if (CSC_SUPPORT_5G_ANT_SHARE) {
                sendIpcMessageToRilForLteu(LTEU_STA_5GHZ_CONNECTED, false, false, false);
            }

            if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
                if (mCurrentConfig != null && mCurrentConfig.SSID != null) {
                    if (mCurrentConfig.allowedKeyManagement.get(KeyMgmt.NONE) ||
                        mCurrentConfig.allowedKeyManagement.get(KeyMgmt.WPA_PSK) ||
                        mCurrentConfig.allowedKeyManagement.get(KeyMgmt.WPA2_PSK) ||
                        mCurrentConfig.allowedKeyManagement.get(KeyMgmt.WAPI_PSK)) {
                        WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                            true, TAG, "Wi-Fi is disconnected from " + mCurrentConfig.getPrintableSsid() + " network using 802.11-2012 channel");
                    } else {
                        if (mCurrentConfig.enterpriseConfig != null) {
                            if (mCurrentConfig.enterpriseConfig.getEapMethod() == WifiEnterpriseConfig.Eap.TLS) {
                                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                                    true, TAG, "Wi-Fi is disconnected from " + mCurrentConfig.getPrintableSsid() + " network using EAP-TLS channel");
                            } else {
                                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                                    true, TAG, "Wi-Fi is disconnected from " + mCurrentConfig.getPrintableSsid() + " network using 802.1X channel");
                            }
                        }
                    }
                }
            }
            if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                mWifiGeofenceManager.triggerStopLearning(mCurrentConfig);
            }
            if (mWifiInjector.getSemWifiApChipInfo().supportWifiSharing() && Vendor.VZW != mOpBranding ) {
                logd("Wifi got Disconnected in connectedstate, Send provisioning intent mIsAutoRoaming" + mIsAutoRoaming);
                 Intent provisionIntent = new Intent("com.samsung.intent.action.START_PROVISIONING");
                 provisionIntent.putExtra("wState", WifiConnectivityManager.WIFI_STATE_DISCONNECTED);
                 if(mContext != null){
                     mContext.sendBroadcast(provisionIntent);
                     provisionIntent.setPackage("com.android.settings");
                     mContext.sendBroadcast(provisionIntent);
                 }
            } else if(mWifiInjector.getSemWifiApChipInfo().supportWifiSharing() && Vendor.VZW == mOpBranding && isWifiSharingProvisioning()){
                 if (!mIsAutoRoaming){
                     Intent provisionIntent = new Intent("com.samsung.intent.action.START_PROVISIONING");
                     provisionIntent.putExtra("wState", WifiConnectivityManager.WIFI_STATE_DISCONNECTED);
                     if(mContext != null){
                         mContext.sendBroadcast(provisionIntent);
                         provisionIntent.setPackage("com.android.settings");
                         mContext.sendBroadcast(provisionIntent);
                     }
                 }
            }else if (!mIsAutoRoaming && Vendor.VZW == mOpBranding) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                SemWifiFrameworkUxUtils.showToast(mContext,
                        SemWifiFrameworkUxUtils.INFO_TYPE_DISCONNECT_TOAST, null);
            }

            if (mSemLocationManager != null) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
                if (DBG) Log.d(TAG,"Remove location updates");
                if (mLocationRequestNetworkId == WifiConfiguration.INVALID_NETWORK_ID) {
                    mSemLocationManager.removeLocationUpdates(mSemLocationListener);
                }
                if (mLocationPendingIntent != null) {
                    mSemLocationManager.removePassiveLocation(mLocationPendingIntent);
                }
            }

            report(ReportIdKey.ID_DISCONNECT, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                    ReportUtil.getReportDataForDisconnectTranstion(
                        mScreenOn, 2, mWifiAdpsEnabled.get() ? 1 : 0));

            if (mWifiInfo.getSSID() != null) {
                if (WifiRoamingAssistant.getInstance() != null) {
                    WifiRoamingAssistant.getInstance().updateRcl(StringUtil.removeDoubleQuotes(mWifiInfo.getSSID()), mWifiInfo.getFrequency(), false);
                }
            }

            clearEDMWiFiPolicy();   //Start EDM Wi-Fi Configuration
        }
    }

    class DisconnectingState extends State {

        @Override
        public void enter() {

            if (mVerboseLoggingEnabled) {
                logd(" Enter DisconnectingState State screenOn=" + mScreenOn);
            }

            // Make sure we disconnect: we enter this state prior to connecting to a new
            // network, waiting for either a DISCONNECT event or a SUPPLICANT_STATE_CHANGE
            // event which in this case will be indicating that supplicant started to associate.
            // In some cases supplicant doesn't ignore the connect requests (it might not
            // find the target SSID in its cache),
            // Therefore we end up stuck that state, hence the need for the watchdog.
            mDisconnectingWatchdogCount++;
            logd("Start Disconnecting Watchdog " + mDisconnectingWatchdogCount);
            sendMessageDelayed(obtainMessage(CMD_DISCONNECTING_WATCHDOG_TIMER,
                    mDisconnectingWatchdogCount, 0), DISCONNECTING_GUARD_TIMER_MSEC);
        }

        @Override
        public boolean processMessage(Message message) {
            boolean handleStatus = HANDLED;

            switch (message.what) {
                case CMD_DISCONNECT:
                    if (mVerboseLoggingEnabled) {
                        log("Ignore CMD_DISCONNECT when already disconnecting.");
                    }
                    break;
                case CMD_DISCONNECTING_WATCHDOG_TIMER:
                    if (mDisconnectingWatchdogCount == message.arg1) {
                        if (mVerboseLoggingEnabled) log("disconnecting watchdog! -> disconnect");
                        handleNetworkDisconnect();
                        transitionTo(mDisconnectedState);
                    }
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    /**
                     * If we get a SUPPLICANT_STATE_CHANGE_EVENT before NETWORK_DISCONNECTION_EVENT
                     * we have missed the network disconnection, transition to mDisconnectedState
                     * and handle the rest of the events there
                     */
                    deferMessage(message);
                    handleNetworkDisconnect();
                    transitionTo(mDisconnectedState);
                    break;
                default:
                    handleStatus = NOT_HANDLED;
                    break;
            }

            if (handleStatus == HANDLED) {
                logStateAndMessage(message, this);
            }
            return handleStatus;
        }
    }

    class DisconnectedState extends State {
        @Override
        public void enter() {
            Log.i(TAG, "disconnectedstate enter");
            mIsRoamNetwork = false; // WCM
            setRoamTriggered(false);
            // We don't scan frequently if this is a temporary disconnect
            // due to p2p
            if (mTemporarilyDisconnectWifi) {
                p2pSendMessage(WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                return;
            }

            if (mVerboseLoggingEnabled) {
                logd(" Enter DisconnectedState screenOn=" + mScreenOn);
            }

            if (true/*(mWifiConfigManager.getConfiguredNetworks().size() == 0)*/) { // SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE
                removeMessages(CMD_THREE_TIMES_SCAN_IN_IDLE);
                sendMessage(CMD_THREE_TIMES_SCAN_IN_IDLE);
            }

            mWifiConnectivityManager.handleConnectionStateChanged(
                    WifiConnectivityManager.WIFI_STATE_DISCONNECTED);
            if (mIsAutoRoaming && Vendor.VZW == mOpBranding) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                if (mWifiInjector.getSemWifiApChipInfo().supportWifiSharing() && isWifiSharingProvisioning()) {
                    logd("Wifi got in DisconnectedState, Send provisioning intent mIsAutoRoaming" + mIsAutoRoaming);
                    Intent provisionIntent = new Intent("com.samsung.intent.action.START_PROVISIONING");
                    provisionIntent.putExtra("wState", WifiConnectivityManager.WIFI_STATE_DISCONNECTED);
                    if(mContext != null){
                        mContext.sendBroadcast(provisionIntent);
                        provisionIntent.setPackage("com.android.settings");
                        mContext.sendBroadcast(provisionIntent);
                    }
                } else {
                   SemWifiFrameworkUxUtils.showToast(mContext,
                           SemWifiFrameworkUxUtils.INFO_TYPE_DISCONNECT_TOAST, null);
                }
            }

            /** clear the roaming state, if we were roaming, we failed */
            mIsAutoRoaming = false;

            // >>>WCM>>>
            mWifiScoreReport.resetNetworkAgent();
            // <<<WCM<<<
        }

        @Override
        public boolean processMessage(Message message) {
            boolean handleStatus = HANDLED;

            switch (message.what) {
                case CMD_THREE_TIMES_SCAN_IN_IDLE: // SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE
                    if ((mScanResultsEventCounter++ < MAX_SCAN_RESULTS_EVENT_COUNT_IN_IDLE) && mScreenOn) {
                        Log.e(TAG, "DisconnectedState  CMD_THREE_TIMES_SCAN_IN_IDLE && mScreenOn");
                        String packageName = mContext.getOpPackageName();
                        mScanRequestProxy.startScan(Process.SYSTEM_UID, packageName);
                        sendMessageDelayed(CMD_THREE_TIMES_SCAN_IN_IDLE, 8000);
                    }
                    break;
                case CMD_DISCONNECT:
                    mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                            StaEvent.DISCONNECT_GENERIC);
                    mWifiNative.disconnect(mInterfaceName);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    if (message.arg2 == 15 /* FOURWAY_HANDSHAKE_TIMEOUT */) {
                        String bssid = (message.obj == null)
                                ? mTargetRoamBSSID : (String) message.obj;
                        mWifiInjector.getWifiLastResortWatchdog()
                                .noteConnectionFailureAndTriggerIfNeeded(
                                        getTargetSsid(), bssid,
                                        WifiLastResortWatchdog.FAILURE_CODE_AUTHENTICATION);
                    }
                    //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                    checkAndUpdateUnstableAp(mTargetNetworkId,
                            (String) message.obj,
                            message.arg1 != 0, message.arg2);
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (mVerboseLoggingEnabled) {
                        logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state
                                + " -> state= "
                                + WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    }
                    if (SupplicantState.isConnecting(stateChangeResult.state)) {
                        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(
                                stateChangeResult.networkId);

                        // Update Passpoint information before setNetworkDetailedState as
                        // WifiTracker monitors NETWORK_STATE_CHANGED_ACTION to update UI.
                        mWifiInfo.setFQDN(null);
                        mWifiInfo.setOsuAp(false);
                        mWifiInfo.setProviderFriendlyName(null);
                        if (config != null && (config.isPasspoint() || config.osu)) {
                            if (config.isPasspoint()) {
                                mWifiInfo.setFQDN(config.FQDN);
                            } else {
                                mWifiInfo.setOsuAp(true);
                            }
                            mWifiInfo.setProviderFriendlyName(config.providerFriendlyName);
                        }
                    }
                    setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    /* ConnectModeState does the rest of the handling */
                    handleStatus = NOT_HANDLED;
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED:
                    NetworkInfo info = (NetworkInfo) message.obj;
                    mP2pConnected.set(info.isConnected());
                    break;
                case CMD_RECONNECT:
                case CMD_REASSOCIATE:
                    if (mTemporarilyDisconnectWifi) {
                        // Drop a third party reconnect/reassociate if STA is
                        // temporarily disconnected for p2p
                        break;
                    } else {
                        // ConnectModeState handles it
                        handleStatus = NOT_HANDLED;
                    }
                    break;
                case CMD_SCREEN_STATE_CHANGED:
                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
                    boolean screenOn = (message.arg1 != 0);
                    if (mScreenOn != screenOn) {
                        handleScreenStateChanged(screenOn);
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
                    break;
                default:
                    handleStatus = NOT_HANDLED;
                    break;
            }

            if (handleStatus == HANDLED) {
                logStateAndMessage(message, this);
            }
            return handleStatus;
        }

        @Override
        public void exit() {
            mWifiConnectivityManager.handleConnectionStateChanged(
                     WifiConnectivityManager.WIFI_STATE_TRANSITIONING);
        }
    }

    /**
     * State machine initiated requests can have replyTo set to null, indicating
     * there are no recipients, we ignore those reply actions.
     */
    private void replyToMessage(Message msg, int what) {
        if (msg.replyTo == null) return;
        Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
        mReplyChannel.replyToMessage(msg, dstMsg);
    }

    private void replyToMessage(Message msg, int what, int arg1) {
        if (msg.replyTo == null) return;
        Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
        dstMsg.arg1 = arg1;
        mReplyChannel.replyToMessage(msg, dstMsg);
    }

    private void replyToMessage(Message msg, int what, Object obj) {
        if (msg.replyTo == null) return;
        Message dstMsg = obtainMessageWithWhatAndArg2(msg, what);
        dstMsg.obj = obj;
        mReplyChannel.replyToMessage(msg, dstMsg);
    }

    /**
     * arg2 on the source message has a unique id that needs to be retained in replies
     * to match the request
     * <p>see WifiManager for details
     */
    private Message obtainMessageWithWhatAndArg2(Message srcMsg, int what) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg2 = srcMsg.arg2;
        return msg;
    }

    /**
     * Notify interested parties if a wifi config has been changed.
     *
     * @param wifiCredentialEventType WIFI_CREDENTIAL_SAVED or WIFI_CREDENTIAL_FORGOT
     * @param config Must have a WifiConfiguration object to succeed
     * TODO: b/35258354 investigate if this can be removed.  Is the broadcast sent by
     * WifiConfigManager sufficient?
     */
    private void broadcastWifiCredentialChanged(int wifiCredentialEventType,
            WifiConfiguration config) {
        if (config != null && config.preSharedKey != null) {
            Intent intent = new Intent(WifiManager.WIFI_CREDENTIAL_CHANGED_ACTION);
            intent.putExtra(WifiManager.EXTRA_WIFI_CREDENTIAL_SSID, config.SSID);
            intent.putExtra(WifiManager.EXTRA_WIFI_CREDENTIAL_EVENT_TYPE,
                    wifiCredentialEventType);
            mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT,
                    android.Manifest.permission.RECEIVE_WIFI_CREDENTIAL_CHANGE);
        }
    }

    void handleGsmAuthRequest(SimAuthRequestData requestData) {
        if (mTargetWifiConfiguration == null
                || mTargetWifiConfiguration.networkId
                == requestData.networkId) {
            logd("id matches mTargetWifiConfiguration");
        } else {
            logd("id does not match mTargetWifiConfiguration");
            return;
        }

        /*
         * Try authentication in the following order.
         *
         *    Standard       Cellular_auth     Type Command
         *
         * 1. 3GPP TS 31.102 3G_authentication [Length][RAND][Length][AUTN]
         *                            [Length][RES][Length][CK][Length][IK] and more
         * 2. 3GPP TS 31.102 2G_authentication [Length][RAND]
         *                            [Length][SRES][Length][Cipher Key Kc]
         * 3. 3GPP TS 11.11  2G_authentication [RAND]
         *                            [SRES][Cipher Key Kc]
         */
        String response =
                TelephonyUtil.getGsmSimAuthResponse(requestData.data, getTelephonyManager());
        if (response == null) {
            // In case of failure, issue may be due to sim type, retry as No.2 case
            response =
                TelephonyUtil.getGsmSimpleSimAuthResponse(requestData.data, getTelephonyManager());
            if (response == null) {
                // In case of failure, issue may be due to sim type, retry as No.3 case
                response =
                    TelephonyUtil.getGsmSimpleSimNoLengthAuthResponse(requestData.data, getTelephonyManager());
            }
        }
        if (response == null || response.length() == 0) {
            mWifiNative.simAuthFailedResponse(mInterfaceName, requestData.networkId);
        } else {
            logv("Supplicant Response -" + response);
            mWifiNative.simAuthResponse(
                    mInterfaceName, requestData.networkId,
                    WifiNative.SIM_AUTH_RESP_TYPE_GSM_AUTH, response);
        }
    }

    void handle3GAuthRequest(SimAuthRequestData requestData) {
        if (mTargetWifiConfiguration == null
                || mTargetWifiConfiguration.networkId
                == requestData.networkId) {
            logd("id matches mTargetWifiConfiguration");
        } else {
            logd("id does not match mTargetWifiConfiguration");
            return;
        }

        SimAuthResponseData response =
                TelephonyUtil.get3GAuthResponse(requestData, getTelephonyManager());
        if (response != null) {
            mWifiNative.simAuthResponse(
                    mInterfaceName, requestData.networkId, response.type, response.response);
        } else {
            mWifiNative.umtsAuthFailedResponse(mInterfaceName, requestData.networkId);
        }
    }

    /**
     * Automatically connect to the network specified
     *
     * @param networkId ID of the network to connect to
     * @param uid UID of the app triggering the connection.
     * @param bssid BSSID of the network
     */
    public void startConnectToNetwork(int networkId, int uid, String bssid) {
        sendMessage(CMD_START_CONNECT, networkId, uid, bssid);
    }

    /**
     * Automatically roam to the network specified
     *
     * @param networkId ID of the network to roam to
     * @param scanResult scan result which identifies the network to roam to
     */
    public void startRoamToNetwork(int networkId, ScanResult scanResult) {
        sendMessage(CMD_START_ROAM, networkId, 0, scanResult);
    }

    /**
     * Dynamically turn on/off WifiConnectivityManager
     *
     * @param enabled true-enable; false-disable
     */
    public void enableWifiConnectivityManager(boolean enabled) {
        sendMessage(CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER, enabled ? 1 : 0);
    }

    /**
     * @param reason reason code from supplicant on network disconnected event
     * @return true if this is a suspicious disconnect
     */
    static boolean unexpectedDisconnectedReason(int reason) {
        return reason == 2              // PREV_AUTH_NOT_VALID
                || reason == 6          // CLASS2_FRAME_FROM_NONAUTH_STA
                || reason == 7          // FRAME_FROM_NONASSOC_STA
                || reason == 8          // STA_HAS_LEFT
                || reason == 9          // STA_REQ_ASSOC_WITHOUT_AUTH
                || reason == 14         // MICHAEL_MIC_FAILURE
                || reason == 15         // 4WAY_HANDSHAKE_TIMEOUT
                || reason == 16         // GROUP_KEY_UPDATE_TIMEOUT
                || reason == 18         // GROUP_CIPHER_NOT_VALID
                || reason == 19         // PAIRWISE_CIPHER_NOT_VALID
                || reason == 23         // IEEE_802_1X_AUTH_FAILED
                || reason == 34;        // DISASSOC_LOW_ACK
    }

    /**
     * Update WifiMetrics before dumping
     */
    public void updateWifiMetrics() {
        mWifiMetrics.updateSavedNetworks(mWifiConfigManager.getSavedNetworks(Process.WIFI_UID));
        mPasspointManager.updateMetrics();
    }

    /**
     * Private method to handle calling WifiConfigManager to forget/remove network configs and reply
     * to the message from the sender of the outcome.
     *
     * The current implementation requires that forget and remove be handled in different ways
     * (responses are handled differently).  In the interests of organization, the handling is all
     * now in this helper method.  TODO: b/35257965 is filed to track the possibility of merging
     * the two call paths.
     */
    private boolean deleteNetworkConfigAndSendReply(Message message, boolean calledFromForget) {
        boolean success = mWifiConfigManager.removeNetwork(message.arg1, message.sendingUid, message.arg2); //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
        if (!success) {
            loge("Failed to remove network");
        }

        if (calledFromForget) {
            if (success) {
                replyToMessage(message, WifiManager.FORGET_NETWORK_SUCCEEDED);
                broadcastWifiCredentialChanged(WifiManager.WIFI_CREDENTIAL_FORGOT,
                                               (WifiConfiguration) message.obj);
                return true;
            }
            replyToMessage(message, WifiManager.FORGET_NETWORK_FAILED, WifiManager.ERROR);
            return false;
        } else {
            // Remaining calls are from the removeNetwork path
            if (success) {
                replyToMessage(message, message.what, SUCCESS);
                return true;
            }
            mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, message.what, FAILURE);
            return false;
        }
    }

    /**
     * Private method to handle calling WifiConfigManager to add & enable network configs and reply
     * to the message from the sender of the outcome.
     *
     * @return NetworkUpdateResult with networkId of the added/updated configuration. Will return
     * {@link WifiConfiguration#INVALID_NETWORK_ID} in case of error.
     */
    private NetworkUpdateResult saveNetworkConfigAndSendReply(Message message) {
        WifiConfiguration config = (WifiConfiguration) message.obj;
        if (config == null) {
            loge("SAVE_NETWORK with null configuration "
                    + mSupplicantStateTracker.getSupplicantStateName()
                    + " my state " + getCurrentState().getName());
            mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, WifiManager.SAVE_NETWORK_FAILED, WifiManager.ERROR);
            return new NetworkUpdateResult(WifiConfiguration.INVALID_NETWORK_ID);
        }
        config.priority = mWifiConfigManager.increaseAndGetPriority(); //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS
        mWifiConfigManager.updateBssidWhitelist(config, mScanRequestProxy.getScanResults()); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
        NetworkUpdateResult result =
                mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
        if (!result.isSuccess()) {
            loge("SAVE_NETWORK adding/updating config=" + config + " failed");
            mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, WifiManager.SAVE_NETWORK_FAILED, WifiManager.ERROR);
            return result;
        }
        if (!mWifiConfigManager.enableNetwork(
                result.getNetworkId(), false, message.sendingUid)) {
            loge("SAVE_NETWORK enabling config=" + config + " failed");
            mMessageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, WifiManager.SAVE_NETWORK_FAILED, WifiManager.ERROR);
            return new NetworkUpdateResult(WifiConfiguration.INVALID_NETWORK_ID);
        }
        broadcastWifiCredentialChanged(WifiManager.WIFI_CREDENTIAL_SAVED, config);
        replyToMessage(message, WifiManager.SAVE_NETWORK_SUCCEEDED);
        return result;
    }

    private static String getLinkPropertiesSummary(LinkProperties lp) {
        List<String> attributes = new ArrayList<>(6);
        if (lp.hasIPv4Address()) {
            attributes.add("v4");
        }
        if (lp.hasIPv4DefaultRoute()) {
            attributes.add("v4r");
        }
        if (lp.hasIPv4DnsServer()) {
            attributes.add("v4dns");
        }
        if (lp.hasGlobalIPv6Address()) {
            attributes.add("v6");
        }
        if (lp.hasIPv6DefaultRoute()) {
            attributes.add("v6r");
        }
        if (lp.hasIPv6DnsServer()) {
            attributes.add("v6dns");
        }

        return TextUtils.join(" ", attributes);
    }

    /**
     * Gets the SSID from the WifiConfiguration pointed at by 'mTargetNetworkId'
     * This should match the network config framework is attempting to connect to.
     */
    private String getTargetSsid() {
        WifiConfiguration currentConfig = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
        if (currentConfig != null) {
            return currentConfig.SSID;
        }
        return null;
    }

    /**
     * Send message to WifiP2pServiceImpl.
     * @return true if message is sent.
     *         false if there is no channel configured for WifiP2pServiceImpl.
     */
    private boolean p2pSendMessage(int what) {
        if (mWifiP2pChannel != null) {
            mWifiP2pChannel.sendMessage(what);
            return true;
        }
        return false;
    }

    /**
     * Send message to WifiP2pServiceImpl with an additional param |arg1|.
     * @return true if message is sent.
     *         false if there is no channel configured for WifiP2pServiceImpl.
     */
    private boolean p2pSendMessage(int what, int arg1) {
        if (mWifiP2pChannel != null) {
            mWifiP2pChannel.sendMessage(what, arg1);
            return true;
        }
        return false;
    }

    /**
     * Check if there is any connection request for WiFi network.
     */
    private boolean hasConnectionRequests() {
        return mNetworkFactory.hasConnectionRequests()
                || mUntrustedNetworkFactory.hasConnectionRequests();
    }

    /**
     * Returns whether CMD_IP_REACHABILITY_LOST events should trigger disconnects.
     */
    public boolean getIpReachabilityDisconnectEnabled() {
        return mIpReachabilityDisconnectEnabled;
    }

    /**
     * Sets whether CMD_IP_REACHABILITY_LOST events should trigger disconnects.
     */
    public void setIpReachabilityDisconnectEnabled(boolean enabled) {
        mIpReachabilityDisconnectEnabled = enabled;
    }

    /**
     * Sends a message to initialize the ClientModeImpl.
     *
     * @return true if succeeded, false otherwise.
     */
    public boolean syncInitialize(AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_INITIALIZE);
        boolean result = (resultMsg.arg1 != FAILURE);
        resultMsg.recycle();
        return result;
    }

    void setAutoConnectCarrierApEnabled(boolean enabled) { //SEC_PRODUCT_FEATURE_WLAN_AUTO_CONNECT_CARRIER_AP
        sendMessage(CMD_AUTO_CONNECT_CARRIER_AP_ENABLED, enabled ? 1 : 0, 0);
    }

    /**
     * Add a network request match callback to {@link WifiNetworkFactory}.
     */
    public void addNetworkRequestMatchCallback(IBinder binder,
                                               INetworkRequestMatchCallback callback,
                                               int callbackIdentifier) {
        mNetworkFactory.addCallback(binder, callback, callbackIdentifier);
    }

    /**
     * Remove a network request match callback from {@link WifiNetworkFactory}.
     */
    public void removeNetworkRequestMatchCallback(int callbackIdentifier) {
        mNetworkFactory.removeCallback(callbackIdentifier);
    }

    /**
     * Remove all approved access points from {@link WifiNetworkFactory} for the provided package.
     */
    public void removeNetworkRequestUserApprovedAccessPointsForApp(@NonNull String packageName) {
        mNetworkFactory.removeUserApprovedAccessPointsForApp(packageName);
    }

    /**
     * Clear all approved access points from {@link WifiNetworkFactory}.
     */
    public void clearNetworkRequestUserApprovedAccessPoints() {
        mNetworkFactory.clear();
    }

    /**
     * Gets the factory MAC address of wlan0 (station interface).
     * @return String representation of the factory MAC address.
     */
    public String getFactoryMacAddress() {
        MacAddress macAddress = mWifiNative.getFactoryMacAddress(mInterfaceName);
        if (macAddress != null) {
            return macAddress.toString();
        }
        if (!mConnectedMacRandomzationSupported) {
            return mWifiNative.getMacAddress(mInterfaceName);
        }
        return null;
    }

    /**
     * Sets the current device mobility state.
     * @param state the new device mobility state
     */
    public void setDeviceMobilityState(@DeviceMobilityState int state) {
        mWifiConnectivityManager.setDeviceMobilityState(state);
    }

    /**
     * Updates the Wi-Fi usability score.
     * @param seqNum Sequence number of the Wi-Fi usability score.
     * @param score The Wi-Fi usability score.
     * @param predictionHorizonSec Prediction horizon of the Wi-Fi usability score.
     */
    public void updateWifiUsabilityScore(int seqNum, int score, int predictionHorizonSec) {
        mWifiMetrics.incrementWifiUsabilityScoreCount(seqNum, score, predictionHorizonSec);
    }

    /**
     * Sends a link probe.
     */
    @VisibleForTesting
    public void probeLink(WifiNative.SendMgmtFrameCallback callback, int mcs) {
        mWifiNative.probeLink(mInterfaceName, MacAddress.fromString(mWifiInfo.getBSSID()),
                callback, mcs);
    }

    private void updateWifiInfoForVendors(ScanResult scanResult) {
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_80211AC) {
            if (mOpBranding == Vendor.KTT) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_KTT_GIGA_AP
                String capabilities = scanResult.capabilities;
                if (capabilities != null) {
                    if (capabilities.contains("[VSI]") && capabilities.contains("[VHT]")) {
                        if (DBG) Log.d(TAG, "setGigaAp: true");
                        mWifiInfo.setGigaAp(true);
                    } else {
                        if (DBG) {
                            Log.d(TAG, "setGigaAp: false, bssid: "
                                    + scanResult.BSSID + ", capa: " + capabilities);
                        }
                        mWifiInfo.setGigaAp(false);
                    }
                }
            }
        }
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                && mIsPasspointEnabled && mOpBranding == Vendor.SKT
                && mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID
                && !TextUtils.isEmpty(scanResult.BSSID)) {
            WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(mLastNetworkId);
            if (config != null && config.semIsVendorSpecificSsid && config.isPasspoint() && !config.isHomeProviderNetwork) {
                ScanDetailCache scanDetailCache = mWifiConfigManager.getScanDetailCacheForNetwork(mLastNetworkId);
                if (scanDetailCache != null) {
                    ScanDetail scanDetail = scanDetailCache.getScanDetail(scanResult.BSSID);
                    // Skip non-Passpoint APs.
                    if (scanDetail != null && scanDetail.getNetworkDetail().isInterworking()) {
                        // Set venuname if scan result says network is passpoint
                        Map<Constants.ANQPElementType, ANQPElement> anqpElements = null;
                        anqpElements = mPasspointManager.getANQPElements(scanResult);
                        if (anqpElements != null && anqpElements.size() > 0) {
                            HSWanMetricsElement wm = (HSWanMetricsElement) anqpElements.get(
                                    Constants.ANQPElementType.HSWANMetrics);
                            VenueNameElement vne = (VenueNameElement) anqpElements.get(
                                    Constants.ANQPElementType.ANQPVenueName);
                            if (vne != null && !vne.getNames().isEmpty()) {
                                String venueName = vne.getNames().get(0).getText();
                                Log.i(TAG, "updateVenueNameInWifiInfo: venueName is " + venueName);
                                mWifiInfo.setVenueName(venueName);
                            }
                        } else {
                            Log.d(TAG, "There is no anqpElements, so send anqp query to " + scanResult.SSID);
                            mPasspointManager.forceRequestAnqp(scanResult);
                        }
                    }
                }
            }
        }
    }

    private int mIsWifiOnly = -1; //SEC_PRODUCT_FEATURE_WLAN_SEC_WIFIONLY_CHECK
    public boolean isWifiOnly() {
        if (mIsWifiOnly == -1) {
            checkAndSetConnectivityInstance();
            if (mCm != null && mCm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
                mIsWifiOnly = 0;
            } else {
                mIsWifiOnly = 1;
            }
        }
        return (mIsWifiOnly == 1);
    }
    void setShutdown() { //SEC_PRODUCT_FEATURE_WLAN_SET_SHUTDOWN
        mIsShutdown = true;
        mWifiConfigManager.semStopToSaveStore(); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONFIG_MANAGER shutdown
        //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
        if (getCurrentState() == mConnectedState) {
            setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_USER_TRIGGER_DISCON_POWERONOFF_WIFIOFF);
        }
        //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
        if (mDelayDisconnect.isEnabled()) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
            mDelayDisconnect.setEnable(false, 0);
        }
    }

    private void updateAdpsState() { //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
        mWifiNative.setAdps(mInterfaceName, mWifiAdpsEnabled.get());
        mBigDataManager.addOrUpdateValue(WifiBigDataLogManager.LOGGING_TYPE_ADPS_STATE,mWifiAdpsEnabled.get() ? 1 : 0);
    }

    // Samsung Reserved functions...
    public synchronized int callSECApi(Message msg) {
        if (mVerboseLoggingEnabled) Log.i(getName(), "callSECApi what=" + msg.what);
        int retValue = -1;

        switch (msg.what) {
            //TODO: case WifiManager.SEC_COMMAND_ID_INIT:
            case WifiManager.SEC_COMMAND_ID_CONTROL_SENSOR_MONITOR: { //SEC_PRODUCT_FEATURE_WLAN_CONFIG_SENSOR_MONITOR
                Bundle args = (Bundle) msg.obj;
                if (args == null) {
                    return mSemSarManager.isGripSensorEnabled() ? 1 : 0;
                }
                int enable = args.getInt("enable");

                mSemSarManager.enableGripSensorMonitor(enable == 1);

                return 0;
            }
            //TODO: case WifiManager.SEC_COMMAND_ID_WIFI_RECOMMENDATION_TEST: {//SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
            //TODO: case WifiManager.SEC_COMMAND_ID_SAR_BACK_OFF: {  //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLANTEST
            case WifiManager.SEC_COMMAND_ID_DELAY_DISCONNECT_TRANSITION: //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                mDelayDisconnect.setEnable(msg.arg1 == 1, msg.arg2);
                return 0;
            case WifiManager.SEC_COMMAND_ID_GET_ROAM_IS_RUNNING: //SEC_PRODUCT_FEATURE_WLAN_GET_ROAMING_STATUS_FOR_MOBIKE
                retValue = isRoaming() ? 1 : 0;
                break;
            case WifiManager.SEC_COMMAND_ID_DISABLE_FCCCHANNEL_BACKOFF: { //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL
                Bundle args = (Bundle) msg.obj;
                mBlockFccChannelCmd = args.getBoolean("enable");
                return 0;
            }
            case WifiManager.SEC_COMMAND_ID_SET_IMS_RSSI_POLL_STATE: { //CscFeature_Wifi_SupportRssiPollStateDuringWifiCalling
                Bundle args = (Bundle) msg.obj;
                if (args.getBoolean("state"))
                    mRssiPollingScreenOffEnabled |= RSSI_POLL_ENABLE_DURING_LCD_OFF_FOR_IMS;
                else
                    mRssiPollingScreenOffEnabled &= ~RSSI_POLL_ENABLE_DURING_LCD_OFF_FOR_IMS;
                if (mRssiPollingScreenOffEnabled != 0) {
                    if (!mEnableRssiPolling) {
                        enableRssiPolling(true);
                    }
                } else {
                    if (mEnableRssiPolling && !mScreenOn) {
                        enableRssiPolling(false);
                    }
                }
                return 0;
            }
            case WifiManager.SEC_COMMAND_ID_SET_SCAN_CONTROLLER_SETTINGS: { //SEC_PRODUCT_FEATURE_WLAN_SCAN_CONTROLLER
                Bundle args = (Bundle) msg.obj;
                if (args.containsKey("pkgNames") && args.containsKey("scanTypes")
                        && args.containsKey("scanDelays")) {
                    String[] pkgNames = args.getStringArray("pkgNames");
                    int[] scanTypes = args.getIntArray("scanTypes");
                    int[] scanDelays = args.getIntArray("scanDelays");
                    if (pkgNames.length == scanTypes.length && scanTypes.length == scanDelays.length) {
                        for (int i=0; i<pkgNames.length; i++) {
                            mScanRequestProxy.semSetCustomScanPolicy(pkgNames[i], scanTypes[i], scanDelays[i]);
                        }
                    }
                } else if (args.containsKey("pkgName") && args.containsKey("scanType")
                        && args.containsKey("scanDelay")) {
                    String pkgName = args.getString("pkgName");
                    if (pkgName != null && pkgName.length() > 0) {
                        int scanType = args.getInt("scanType", WifiManager.SEM_SCAN_TYPE_FULL);
                        int scanDelay = args.getInt("scanDelay", 0);
                        mScanRequestProxy.semSetCustomScanPolicy(pkgName, scanType, scanDelay);
                    }
                }
                int duration = args.getInt("duration", -1);
                if (duration != -1) {
                    mScanRequestProxy.semSetMaxDurationForCachedScan(duration);
                }
                int useSMD = args.getInt("useSMD", -1);
                if (useSMD != -1) {
                    mScanRequestProxy.semUseSMDForCachedScan(useSMD == 1);
                }
                return 0;
            }
            case WifiManager.SEC_COMMAND_ID_LOGGING: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                sendMessage(obtainMessage(CMD_SEC_LOGGING, 0, 0, msg.obj));
                return 0;
            // >>>WCM>>>
            case WifiManager.SEC_COMMAND_ID_GET_INTERNET_CHECK_OPTION: //TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT "KTT"
                retValue = (mWifiConfigManager.isSkipInternetCheck(msg.arg1)) ? 1 : 0;
                break;
            // <<<WCM<<<
            case WifiManager.SEC_COMMAND_ID_SET_MAX_DTIM_IN_SUSPEND: { // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SET_MAX_DTIM_IN_SUSPEND
                Bundle args = (Bundle) msg.obj;
                if (args == null) {
                    return -1;
                }
                int enable = args.getInt("enable");
                if(enable == 1) {
                    mWifiNative.setMaxDtimInSuspend(mInterfaceName, true);
                }
                else {
                    mWifiNative.setMaxDtimInSuspend(mInterfaceName, false);
                }
                return 0;
            }
            case WifiManager.SEC_COMMAND_ID_WLAN_ADVANCED_DEBUG: { // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLAN_ADVANCED_DEBUG
                Bundle args = (Bundle) msg.obj;
                if (args == null) {
                    return -1;
                }
                int type = args.getInt("type");
                switch (type) {
                    case WLAN_ADVANCED_DEBUG_PKT:
                        Log.i(TAG, "pktlog filter removed, size changed to 1280");
                        mWlanAdvancedDebugState |= WLAN_ADVANCED_DEBUG_PKT;
                        mWifiNative.enablePktlogFilter(mInterfaceName, false);
                        mWifiNative.changePktlogSize(mInterfaceName, "1920");
                        break;
                    case WLAN_ADVANCED_DEBUG_UNWANTED:
                        Log.i(TAG, "WLAN_ADVANCED_DEBUG_UNWANTED changed to true");
                        mWlanAdvancedDebugState |= WLAN_ADVANCED_DEBUG_UNWANTED;
                        break;
                    case WLAN_ADVANCED_DEBUG_DISC:
                        Log.i(TAG, "WLAN_ADVANCED_DEBUG_DISC changed to true");
                        mWlanAdvancedDebugState |= WLAN_ADVANCED_DEBUG_DISC;
                        break;
                    case WLAN_ADVANCED_DEBUG_RESET:
                        Log.i(TAG, "pktlog filter enabled again, size changed to 320 default value again");
                        Log.i(TAG, "WLAN_ADVANCED_DEBUG_UNWANTED changed to false");
                        Log.i(TAG, "WLAN_ADVANCED_DEBUG_DISC changed to false");
                        mWifiNative.enablePktlogFilter(mInterfaceName, true);
                        mWifiNative.changePktlogSize(mInterfaceName, "320");
                        mWlanAdvancedDebugState = WLAN_ADVANCED_DEBUG_RESET;
                        break;
                    case WLAN_ADVANCED_DEBUG_STATE:
                        return mWlanAdvancedDebugState;
                    default:
                        break;
                }
                return 0;
            }
            //TODO: case WifiManager.SEC_COMMAND_ID_REPLACE_PUBLIC_DNS:
            //return 0;
            //Start EDM Wi-Fi Configuration
            case WifiManager.SEC_COMMAND_ID_SET_EDM_WIFI_POLICY:
                mWifiB2bConfigPolicy.setWiFiConfiguration((Bundle) msg.obj);
                updateEDMWiFiPolicy();
                Log.d(TAG, "SEC_COMMAND_ID_SET_EDM_WIFI_POLICY: " + retValue);
                break;
            //End EDM Wi-Fi Configuration
            case WifiManager.SEC_COMMAND_ID_SET_LATENCY_CRITICAL: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SET_LATENCY_CRITICAL
                Bundle args = (Bundle) msg.obj;
                if (args == null) {
                    return -1;
                }
                int enable = args.getInt("enable");
                mWifiNative.setLatencyCritical(mInterfaceName, enable);
                return 0;

            case WifiManager.SEC_COMMAND_ID_REPLACE_PUBLIC_DNS:
                sendMessage(obtainMessage(CMD_REPLACE_PUBLIC_DNS, 0, 0, msg.obj));
            return 0;

            default:
                if (mVerboseLoggingEnabled) Log.e(TAG, "ignore message : not implementation yet");
                break;
        }
        return retValue;
    }

    private boolean processMessageOnDefaultStateForCallSECApiAsync(Message msg) {
        Message innerMsg = (Message) msg.obj;
        if (innerMsg == null) {
            Log.e(TAG, "CMD_SEC_API_ASYNC, invalid innerMsg");
            return false;
        }
        switch (innerMsg.what) {
            case WifiManager.SEC_COMMAND_ID_RESET_CONFIGURATION: //TAG_CSCFEATURE_WIFI_ENABLEMENURESETCONFIGURATION
                mWifiConfigManager.removeFilesInDataMiscDirectory();
                mWifiConfigManager.removeFilesInDataMiscCeDirectory();
                break;
            default:
                return false;
        }
        return true;
    }

    //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
    private void checkAndUpdateUnstableAp(int networkId, String bssid,
                                                  boolean locallyGenerated, int disconnectReason) {
        if (mVerboseLoggingEnabled) {
            Log.d(TAG, "checkAndUpdateUnstableAp netId:" + networkId
                    + ", " + bssid + ", locally:" + locallyGenerated
                    + ", reason:" + disconnectReason);
        }
        if (networkId == WifiConfiguration.INVALID_NETWORK_ID) {
            Log.d(TAG, "disconnected, can't get network id");
        }
        if (TextUtils.isEmpty(bssid)) {
            Log.d(TAG, "disconnected, can't get bssid");
            return;
        }

        boolean isSameNetwork = true, isHotspotAp = false;
        ScanDetailCache scanDetailCache =
                mWifiConfigManager.getScanDetailCacheForNetwork(networkId);
        if (scanDetailCache != null) {
            ScanResult scanResult = scanDetailCache.getScanResult(bssid);
            if (scanResult == null) {
                Log.i(TAG, "disconnected, but not for current network");
                isSameNetwork = false;
            } else if (scanResult.capabilities.contains("[SEC80]")) {
                isHotspotAp = true;
            }
        }
        if (mUnstableApController != null
                && !locallyGenerated
                && isSameNetwork) {
            if (disconnectReason == 77) {
                mWifiConfigManager.updateNetworkSelectionStatus(
                        networkId, WifiConfiguration.NetworkSelectionStatus
                                .DISABLED_BY_WIPS);
            } else if (mUnstableApController.disconnectWithAuthFail(
                    networkId, bssid, mWifiInfo.getRssi(), disconnectReason,
                    getCurrentState() == mConnectedState, isHotspotAp)) {
                //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                report(ReportIdKey.ID_UNSTABLE_AP_DETECTED,
                        ReportUtil.getReportDataForUnstableAp(networkId, bssid));
            }
        }
        if (isSameNetwork && getCurrentState() != mConnectedState) {
            //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
            report(ReportIdKey.ID_L2_CONNECT_FAIL,
                    ReportUtil.getReportDataForL2ConnectFail(networkId, bssid));
        }
    }

    //+ SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FMC
    public boolean syncSetRoamTrigger(int roamTrigger) {
        return mWifiNative.setRoamTrigger(mInterfaceName, roamTrigger);
    }

    public int syncGetRoamTrigger() {
        return mWifiNative.getRoamTrigger(mInterfaceName);
    }

    public boolean syncSetRoamDelta(int roamDelta) {
        return mWifiNative.setRoamDelta(mInterfaceName, roamDelta);
    }

    public int syncGetRoamDelta() {
        return mWifiNative.getRoamDelta(mInterfaceName);
    }
    public boolean syncSetRoamScanPeriod(int roamScanPeriod) {
        return mWifiNative.setRoamScanPeriod(mInterfaceName, roamScanPeriod);
    }
    public int syncGetRoamScanPeriod() {
        return mWifiNative.getRoamScanPeriod(mInterfaceName);
    }

    public boolean syncSetRoamBand(int band) {
        return mWifiNative.setRoamBand(mInterfaceName, band);
    }

    public int syncGetRoamBand() {
        return mWifiNative.getRoamBand(mInterfaceName);
    }

    public boolean syncSetCountryRev(String countryRev) {
        return mWifiNative.setCountryRev(mInterfaceName, countryRev);
    }

    public String syncGetCountryRev() {
        return mWifiNative.getCountryRev(mInterfaceName);
    }
    //- SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FMC

    private boolean processMessageForCallSECApiAsync(Message msg) {
        Message innerMsg = (Message) msg.obj;
        if (innerMsg == null) {
            Log.e(TAG, "CMD_SEC_API_ASYNC, invalid innerMsg");
            return true;
        }

        if (mVerboseLoggingEnabled) Log.d(TAG, "CMD_SEC_API_ASYNC, inner msg.what:" + innerMsg.what);
        Bundle args = (Bundle) innerMsg.obj;

        switch (innerMsg.what) {
            case WifiManager.SEC_COMMAND_ID_SET_WIFI_SCAN_WITH_P2P: //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
            case WifiManager.SEC_COMMAND_ID_SET_WIFI_ENABLED_WITH_P2P: //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
                if (args != null) {
                    boolean enable = args.getBoolean("enable");
                    boolean lock = args.getBoolean("lock");
                    setConcurrentEnabled(enable);
                    Log.d(TAG, "SEC_COMMAND_ID_SET_WIFI_XXX_WITH_P2P mConcurrentEnabled " + mConcurrentEnabled);
                    if (!enable && lock && mP2pSupported && mWifiP2pChannel != null) {
                        Message message = new Message();
                        message.what = WifiP2pManager.STOP_DISCOVERY;
                        mWifiP2pChannel.sendMessage(message);
                    }
                }
                break;
            case WifiManager.SEC_COMMAND_ID_STOP_PERIODIC_SCAN: //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
                if (args != null) {
                    boolean stop = args.getBoolean("stop", false);
                    mScanRequestProxy.setScanningEnabled(!stop, "SEC_COMMAND_ID_STOP_PERIODIC_SCAN");
                }
                break;
            case WifiManager.SEC_COMMAND_ID_SET_PCIE_IRQ_CORE: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_DATA_ACTIVITY_AFFINITY_BOOSTER
                if (args != null) {
                    mWifiNative.setAffinityBooster(mInterfaceName, args.getInt("enable"));
                }
                break;
            case WifiManager.SEC_COMMAND_ID_SET_AUTO_RECONNECT: {// CscFeature_Wifi_DisalbeAutoReconnect
                if (args != null) {
                    mWifiNative.setAffinityBooster(mInterfaceName, args.getInt("enable"));
                    Integer netId = args.getInt("netId");
                    Integer autoReconnect = args.getInt("autoReconnect");
                    Log.d(TAG, "SEC_COMMAND_ID_SET_AUTO_RECONNECT  autoReconnect: " + autoReconnect);
                    //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                    if (ENBLE_WLAN_CONFIG_ANALYTICS) {
                        if (autoReconnect == 0 && (netId == mTargetNetworkId || netId == mLastNetworkId)) {
                            setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_USER_TRIGGER_DISCON_DISABLE_AUTO_RECONNECT);
                        }
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                    mWifiConfigManager.setAutoReconnect(netId, autoReconnect);
                }
                break;
            }
            case WifiManager.SEC_COMMAND_ID_WIFI_CONNECTION_TYPE: { //CscFeature_Wifi_EnableMenuConnectionType
                if (args != null) {
                    boolean enable = args.getBoolean("enable", false);
                    Log.d(TAG, "SEC_COMMAND_ID_WIFI_CONNECTION_TYPE  enable: " + enable);
                    mWifiConfigManager.setNetworkAutoConnect(enable);
                }
                break;
            }
            case WifiManager.SEC_COMMAND_ID_AUTO_CONNECT: { //TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT ATT
                if (args != null) {
                    boolean enable = args.getBoolean("enable", false);
                    Log.d(TAG, "SEC_COMMAND_ID_AUTO_CONNECT, enable: " + enable);
                    mWifiConfigManager.setNetworkAutoConnect(enable);
                }
                break;
            }
            case WifiManager.SEC_COMMAND_ID_RESET_CONFIGURATION: { //TAG_CSCFEATURE_WIFI_ENABLEMENURESETCONFIGURATION
                return false;
            }
            //TODO: case WifiManager.SEC_COMMAND_ID_SET_WECHAT_WIFI_INFO: // CscFeature_Wifi_ConfigSocialSvcIntegration
            // >>>WCM>>>
            case WifiManager.SEC_COMMAND_ID_ANS_EXCEPTION_ANSWER: {
                if (args != null) {
                    boolean keepConnection = args.getBoolean("keep_connection", false);
                    WifiConfiguration config = getCurrentWifiConfiguration();
                    if (config != null && keepConnection) {
                        Log.d(TAG, "SEC_COMMAND_ID_ANS_EXCEPTION_ANSWER, networkId : " + config.networkId + ", keep connection : " + keepConnection);
                        mWifiConfigManager.updateNetworkSelectionStatus(
                                config.networkId,
                                WifiConfiguration.NetworkSelectionStatus
                                        .NETWORK_SELECTION_ENABLE);
                    }
                }
                break;
            }
            // <<<WCM<<<
            default:
                break; //return false;
        }
        return true;
    }

    private int processMessageForCallSECApi(Message message) {
        int intResult = -1;
        Message innerMsg = (Message) message.obj;
        if (innerMsg == null) {
            Log.e(TAG, "CMD_SEC_API, invalid innerMsg");
            return intResult;
        }

        if (mVerboseLoggingEnabled) Log.d(TAG, "CMD_SEC_API, inner msg.what:" + innerMsg.what);

        Bundle args;
        switch (innerMsg.what) {
            case WifiManager.SEC_COMMAND_ID_SET_AMPDU_MPDU: //SEC_PRODUCT_FEATURE_WLAN_ENABLE_TO_SET_AMPDU
                args = (Bundle) innerMsg.obj;
                if (args != null) {
                    intResult = mWifiNative.setAmpdu(mInterfaceName, args.getInt("ampdu"));
                }
                break;
            //TODO: //TODO: case WifiManager.SEC_COMMAND_ID_GET_ROAM_SCAN_CONTROL:
            //+SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FMC
            case WifiManager.SEC_COMMAND_ID_SET_ROAM_SCAN_CONTROL:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = mWifiNative.setRoamScanControl(mInterfaceName,args.getInt("mode"));
                break;
            case WifiManager.SEC_COMMAND_ID_SET_ROAM_SCAN_CHANNELS:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = mWifiNative.setRoamScanChannels(mInterfaceName,args.getString("chinfo"));
                break;
            case WifiManager.SEC_COMMAND_ID_GET_SCAN_CHANNEL_TIME:
                intResult = mWifiNative.getScanChannelTime(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_SCAN_CHANNEL_TIME:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                   intResult = mWifiNative.setScanChannelTime(mInterfaceName,args.getString("time"));
                break;
            case WifiManager.SEC_COMMAND_ID_GET_SCAN_HOME_TIME:
                intResult = mWifiNative.getScanHomeTime(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_SCAN_HOME_TIME:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                   intResult = mWifiNative.setScanHomeTime(mInterfaceName,args.getString("time"));
                break;
            case WifiManager.SEC_COMMAND_ID_GET_SCAN_HOME_AWAY_TIME:
                intResult = mWifiNative.getScanHomeAwayTime(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_SCAN_HOME_AWAY_TIME:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                   intResult = mWifiNative.setScanHomeAwayTime(mInterfaceName,args.getString("time"));
                break;
            case WifiManager.SEC_COMMAND_ID_GET_SCAN_NPROBES:
                intResult = mWifiNative.getScanNProbes(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_SCAN_NPROBES:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = mWifiNative.setScanNProbes(mInterfaceName,args.getString("num"));
                break;
            case WifiManager.SEC_COMMAND_ID_SEND_ACTION_FRAME:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = mWifiNative.sendActionFrame(mInterfaceName,args.getString("param"));
                break;
            case WifiManager.SEC_COMMAND_ID_REASSOC:
                args = (Bundle) innerMsg.obj;
                if (args != null) {
                    if (getNCHOVersion() == NCHO_VERSION_2_0 && getNCHO20State() == NCHO_VER2_STATE_DISABLED && "eng".equals(android.os.Build.TYPE)) {
                        intResult = mWifiNative.reAssocLegacy(mInterfaceName , args.getString("param"));
                    } else {
                        intResult = mWifiNative.reAssoc(mInterfaceName , args.getString("param"));
                    }
                }
                break;
            case WifiManager.SEC_COMMAND_ID_GET_ROAM_TRIGGER:
                if (getNCHOVersion() == NCHO_VERSION_2_0 && getNCHO20State() == NCHO_VER2_STATE_DISABLED && "eng".equals(android.os.Build.TYPE)) {
                    intResult = mWifiNative.getRoamTriggerLegacy(mInterfaceName);
                } else {
                    intResult = mWifiNative.getRoamTrigger(mInterfaceName);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_SET_ROAM_TRIGGER:
                args = (Bundle) innerMsg.obj;
                if (args != null) {
                    mIsNchoParamSet = true;
                    if (getNCHOVersion() == NCHO_VERSION_2_0 && getNCHO20State() == NCHO_VER2_STATE_DISABLED && "eng".equals(android.os.Build.TYPE)) {
                        intResult = (mWifiNative.setRoamTriggerLegacy(mInterfaceName,args.getInt("level")) ? 1 : 0);
                    } else {
                        intResult = (mWifiNative.setRoamTrigger(mInterfaceName,args.getInt("level")) ? 1 : 0);
                    }
                }
                break;
            case WifiManager.SEC_COMMAND_ID_GET_ROAM_DELTA:
                intResult = mWifiNative.getRoamDelta(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_ROAM_DELTA:
                args = (Bundle) innerMsg.obj;
                if (args != null) {
                    mIsNchoParamSet = true;
                    intResult = (mWifiNative.setRoamDelta(mInterfaceName,args.getInt("level")) ? 1 : 0);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_GET_ROAM_SCAN_PERIOD:
                intResult = mWifiNative.getRoamScanPeriod(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_ROAM_SCAN_PERIOD:
                args = (Bundle) innerMsg.obj;
                if (args != null) {
                    mIsNchoParamSet = true;
                    intResult = (mWifiNative.setRoamScanPeriod(mInterfaceName,args.getInt("time")) ? 1 : 0);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_SET_COUNTRY_REV:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = (mWifiNative.setCountryRev(mInterfaceName,args.getString("country")) ? 1 : 0);
                break;
            case WifiManager.SEC_COMMAND_ID_GET_BAND:
                intResult = mWifiNative.getBand(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_BAND:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = (mWifiNative.setBand(mInterfaceName,args.getInt("band")) ? 1 : 0);
                break;
            case WifiManager.SEC_COMMAND_ID_GET_DFS_SCAN_MODE:
                intResult = mWifiNative.getDfsScanMode(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_DFS_SCAN_MODE:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = (mWifiNative.setDfsScanMode(mInterfaceName,args.getInt("mode")) ? 1 : 0);
                break;
            case WifiManager.SEC_COMMAND_ID_GET_WES_MODE:
                intResult = mWifiNative.getWesMode(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_WES_MODE:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = (mWifiNative.setWesMode(mInterfaceName,args.getInt("mode")) ? 1 : 0);
                break;
            case WifiManager.SEC_COMMAND_ID_GET_NCHO_MODE:
                if (getNCHOVersion() == NCHO_VERSION_2_0) {
                    intResult = mWifiNative.getNCHOMode(mInterfaceName);
                    if (mVerboseLoggingEnabled) Log.d(TAG, "Get ncho mode: " + intResult );
                    if (intResult ==  NCHO_VER2_STATE_DISABLED || intResult ==  NCHO_VER2_STATE_ENABLED)
                        setNCHO20State(intResult, false);
                    else
                        Log.e(TAG, "Get ncho mode - Something Wrong: " + intResult );
                }
                break;
            case WifiManager.SEC_COMMAND_ID_SET_NCHO_MODE:
                args = (Bundle) innerMsg.obj;
                if (args != null) {
                    int setVal = args.getInt("mode");
                    if (setVal != 0 && setVal != 1) {
                        Log.e(TAG, "Set ncho mode - invalid set value: " + setVal);
                        break;
                    }

                    int nchoversion = getNCHOVersion();
                    if (nchoversion == NCHO_VERSION_2_0) {
                        intResult = (setNCHO20State(setVal, true) ? 1 : 0);
                        if (intResult == 1) {
                            mIsNchoParamSet = false;
                        } else {
                            Log.e(TAG, "Fail to set NCHO to Firmware:" + intResult);
                        }
                    } else if (nchoversion == NCHO_VERSION_1_0) {
                        if (setVal == 0 && getNCHO10State() == NCHO_VER1_STATE_BACKUP) {
                            restoreNcho10Param();
                            mIsNchoParamSet = false;
                        }
                    }
                }
                break;
            //-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FMC
            //TODO: // >>>WCM>>>
            case WifiManager.SEC_COMMAND_ID_SNS_DELETE_EXCLUDED:
                args = (Bundle) innerMsg.obj;
                Integer netId = args.getInt("excluded_networkId");
                if (DBG) {
                    Log.d(TAG, "SEC_COMMAND_ID_SNS_DELETE_EXCLUDED : netId(" + netId +"), delete excluded network");
                }
                if (mWifiInfo.getNetworkId() == netId && mCm != null) { // Current Connected Network
                    mCm.setAcceptUnvalidated(getCurrentNetwork(), false, true);
                }
                mWifiConfigManager.setNetworkNoInternetAccessExpected(netId, false);
                sendWcmConfigurationChanged();
                intResult = 1;
                break;
            //TODO: case WifiManager.SEC_COMMAND_ID_SET_INTERNET_CHECK_OPTION:
            // <<<WCM<<<
            //+TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT "KTT"
            default:
                if (mVerboseLoggingEnabled) Log.e(TAG, "ignore message : not implementation yet");
                break;
        }
        return intResult;
    }

    private String processMessageOnDefaultStateForCallSECStringApi(Message message) {
        final int VENDOR_CONN_FILE_MAX = 10;
        String stringResult = null;
        Message innerMsg = (Message) message.obj;
        if (innerMsg == null) {
            Log.e(TAG, "CMD_SEC_STRING_API, invalid innerMsg");
            return stringResult;
        }

        if (mVerboseLoggingEnabled) Log.d(TAG, "CMD_SEC_STRING_API, inner msg.what:" + innerMsg.what);

        switch (innerMsg.what) {
            case WifiManager.SEC_COMMAND_ID_GET_SCAN_CONTROLLER_SETTINGS: { //SEC_PRODUCT_FEATURE_WLAN_SCAN_CONTROLLER
                stringResult = mScanRequestProxy.semDumpCachedScanController();
                break;
            }
            case WifiManager.SEC_COMMAND_ID_GET_ISSUE_DETECTOR_DUMP: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                if (mIssueDetector != null) {
                    int size = innerMsg.arg1;
                    stringResult = mIssueDetector.getRawData(size == 0 ? 5 : size);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_TEST_GET_CONFIG_FILE: //SEC_PRODUCT_FEATURE_WLAN_AUTO_TEST
                stringResult = WlanTestHelper.getConfigFileString();
                break;
            case WifiManager.SEC_COMMAND_ID_TEST_GET_MODE_INFO: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLANTEST
                if (innerMsg.arg1 < VENDOR_CONN_FILE_MAX) {
                    stringResult = mWifiNative.getVendorConnFileInfo(innerMsg.arg1);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_TEST_SET_MODE_INFO: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLANTEST
                if (innerMsg.obj != null) {
                    Bundle fileData = (Bundle) innerMsg.obj;
                    String data = fileData.getString("data");
                    if (data == null) break;
                    if (innerMsg.arg1 < VENDOR_CONN_FILE_MAX) {
                        if ("!remove".equals(data)) {
                            if (mWifiNative.removeVendorConnFile(innerMsg.arg1)) {
                                stringResult = "OK";
                            }
                        } else {
                            if (mWifiNative.putVendorConnFile(innerMsg.arg1, data)) {
                                stringResult = "OK";
                            }
                        }
                    }
                }
                break;
            case WifiManager.SEC_COMMAND_ID_TEST_GET_VENDOR_PROP: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLANTEST
                if (innerMsg.obj != null) {
                    Bundle prop_info = (Bundle) innerMsg.obj;
                    String prop_name = prop_info.getString("prop_name");
                    int propType = 0;
                    if (prop_name == null) break;
                    else if ("vendor.wlandriver.mode".equals(prop_name)) propType = 0;
                    else if ("vendor.wlandriver.status".equals(prop_name)) propType = 1;
                    else break;
                    stringResult = mWifiNative.getVendorProperty(propType);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_TEST_SET_VENDOR_PROP: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLANTEST
                if (innerMsg.obj != null) {
                    Bundle prop_info = (Bundle) innerMsg.obj;
                    String prop_name = prop_info.getString("prop_name");
                    String data = prop_info.getString("data");
                    int propType = 0;
                    if (prop_name == null) break;
                    else if ("vendor.wlandriver.mode".equals(prop_name)) propType = 0;
                    else if ("vendor.wlandriver.status".equals(prop_name)) propType = 1;
                    else break;
                    if (mWifiNative.setVendorProperty(propType, data)) {
                        stringResult = "OK";
                    }
                }
                break;
            case WifiManager.SEC_COMMAND_ID_GET_GEOFENCE_INFORMATION: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                stringResult = mWifiGeofenceManager.getGeofenceInformation();
                break;
            //TODO: case WifiManager.SEC_COMMAND_ID_WIFI_RECOMMENDATION_DUMP: //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
            default:
                break;
        }
        return stringResult;
    }

    private String processMessageForCallSECStringApi(Message message) {
        String stringResult = null;
        Message innerMsg = (Message) message.obj;
        if (innerMsg == null) {
            Log.e(TAG, "CMD_SEC_STRING_API, invalid innerMsg");
            return stringResult;
        }

        if (mVerboseLoggingEnabled) Log.d(TAG, "CMD_SEC_STRING_API, inner msg.what:" + innerMsg.what);

        switch (innerMsg.what) {
            case WifiManager.SEC_COMMAND_ID_GET_ROAM_SCAN_CHANNELS:
                stringResult = mWifiNative.getRoamScanChannels(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_GET_COUNTRY_REV:
                stringResult = mWifiNative.getCountryRev(mInterfaceName);
                break;
            default:
                if (mVerboseLoggingEnabled) Log.e(TAG, "ignore message : not implementation yet");
                break;
        }
        return stringResult;
    }

    /**
     * Call SEC API ASYNC
     *
     */
    void sendCallSECApiAsync(Message msg, int callingPid) {
        if (mVerboseLoggingEnabled) Log.i(TAG, "sendCallSECApiAsync what=" + msg.what);
        sendMessage(obtainMessage(
                CMD_SEC_API_ASYNC, msg.what, callingPid, Message.obtain(msg)));
    }

    /**
     * Call SEC API
     *
     * @return result
     */
    int syncCallSECApi(AsyncChannel channel, Message msg) {
        if (mVerboseLoggingEnabled) Log.i(TAG, "syncCallSECApi what=" + msg.what);
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            return -1;
        }
        if (channel == null) {
            Log.e(TAG, "Channel is not initialized");
            return -1;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_SEC_API, msg.what, 0, msg);
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    /**
     * Call SEC String API
     *
     * @return result
     */
    String syncCallSECStringApi(AsyncChannel channel, Message msg) {
        if (mVerboseLoggingEnabled) Log.i(TAG, "syncCallSECStringApi what=" + msg.what);
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            return null;
        }

        Message resultMsg = channel.sendMessageSynchronously(CMD_SEC_STRING_API, msg.what, 0, msg);
        String result = (String)resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public void showToastMsg(int type, String extraString) {
        sendMessage(CMD_SHOW_TOAST_MSG, type, 0, extraString);
    }

    //+SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
    void setImsCallEstablished(boolean isEstablished) {
        sendMessage(CMD_IMS_CALL_ESTABLISHED, isEstablished ? 1 : 0, 0);
    }

    boolean isImsCallEstablished() {
        return mIsImsCallEstablished;
    }

    /**
     *  reset Periodic scan time
     */
    public void resetPeriodicScanTimer() {
        if (mWifiConnectivityManager != null) {
            mWifiConnectivityManager.resetPeriodicScanTime();
        }
    }
    //-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER

    private void setTcpBufferAndProxySettingsForIpManager() { //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX
        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
        if (currentConfig != null) {
            if (mIpClient != null) {
                mIpClient.setHttpProxy(currentConfig.getHttpProxy());
            }
        }
        if (!TextUtils.isEmpty(mTcpBufferSizes)) {
            if (mIpClient != null) {
                mIpClient.setTcpBufferSizes(mTcpBufferSizes);
            }
        }
    }

    public void initializeWifiChipInfo() { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CHIP_INFO
        if (!WifiChipInfo.getInstance().isReady()) {
            Log.d(TAG, "chipset information is not ready, try to get the information");
            String cidInfo = mWifiNative.getVendorConnFileInfo(1 /*.cid.info*/);
            if (DBG_PRODUCT_DEV) {
                Log.d(TAG, ".cid.info: " + cidInfo);
            }
            String wifiVerInfo = mWifiNative.getVendorConnFileInfo(5 /*.wifiver.info*/);
            if (DBG_PRODUCT_DEV) {
                Log.d(TAG, ".wifiver.info: " + wifiVerInfo);
            }
            WifiChipInfo.getInstance().updateChipInfos(cidInfo, wifiVerInfo);
            String macAddress = mWifiNative.getVendorConnFileInfo(0 /*.mac.info*/);
            Log.d(TAG, "chipset information is macAddress"+macAddress);
            if (macAddress != null && macAddress.length() >= 17) {
                WifiChipInfo.getInstance().setMacAddress(macAddress.substring(0, 17));
                if (!mWifiInfo.hasRealMacAddress()) { //SEC_PRODUCT_FEATURE_WLAN_GET_MAC_ADDRESS_FROM_FILE
                    mWifiInfo.setMacAddress(macAddress.substring(0, 17));
                }
            }
            String softapInfo = mWifiNative.getVendorConnFileInfo(6 /*.softap.info*/);
            Log.d(TAG, "chipset information is softapInfo"+softapInfo);
            mWifiInjector.getSemWifiApChipInfo().readSoftApInfo(softapInfo); //SEC_PRODUCT_FEATURE_WLAN_SEC_MOBILEAP
            if (WifiChipInfo.getInstance().isReady()) {
                Log.d(TAG, "chipset information is ready");
            }
        }
    }

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    HashMap<Integer, Long> mEventCounter = new HashMap<Integer, Long>();
    private void increaseCounter(int what) {
        long value = 1;
        if (mEventCounter.containsKey(what)) {
            value = mEventCounter.get(what);
            value ++;
        }
        mEventCounter.put(what, value);
    }

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    private long getCounter(int what, long defaultValue) {
        if (mEventCounter.containsKey(what)) {
            return mEventCounter.get(what);
        }
        return defaultValue;
    }

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    private void resetCounter() {
        mEventCounter.clear();
    }

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    private String getWifiParameters(boolean reset) {
        int wifiState = mWifiState.get() == WifiManager.WIFI_STATE_ENABLED ? 1 : 0;
        int alwaysAllowScanningMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, 0);
        int smartNetworkSwitch = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, 0);
        int agressiveSmartNetworkSwitch = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.WIFI_WATCHDOG_POOR_NETWORK_AGGRESSIVE_MODE_ON, 0);
        int favoriteApCount = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.SEM_AUTO_WIFI_FAVORITE_AP_COUNT, 0);
        int isAutoWifiEnabled = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.WIFI_ADAPTIVE_WIFI_CONTROL_ENABLED, 0);
        int safeModeEnabled = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.SAFE_WIFI, 0);
        StringBuffer sb = new StringBuffer();
        sb.append(wifiState).append(" ");
        sb.append(alwaysAllowScanningMode).append(" ");
        sb.append(isAutoWifiEnabled).append(" ");
        sb.append(favoriteApCount).append(" ");
        sb.append(smartNetworkSwitch).append(" ");
        sb.append(agressiveSmartNetworkSwitch).append(" ");
        sb.append(safeModeEnabled).append(" ");
        String scanValues = null;
        if (reset) {
            scanValues = mScanRequestProxy.semGetScanCounterForBigData(reset); //SEC_PRODUCT_FEATURE_WLAN_SCAN_CONTROLLER
        }
        if (scanValues != null) {
            sb.append(scanValues).append(" ");
        } else {
            sb.append("-1 -1 -1 -1 ");
        }
        sb.append(getCounter(WifiManager.START_WPS, 0)).append(" ");
        sb.append(getCounter(WifiManager.WPS_FAILED, 0)).append(" ");
        sb.append(getCounter(WifiManager.WPS_COMPLETED, 0)).append(" ");
        sb.append(getCounter(WifiMonitor.DRIVER_HUNG_EVENT, 0)).append(" ");
        sb.append(mWifiAdpsEnabled.get() ? 1 : 0).append(" ");

       List<WifiConfiguration> configs = mWifiConfigManager.getSavedNetworks(Process.WIFI_UID);
        sb.append(configs.size()).append(" ");
        sb.append(0).append(" ");
        sb.append(0).append(" ");
        sb.append(0);
        if (reset) {
            resetCounter();
        }

        return sb.toString();
    }

    public boolean isUnstableAp(String bssid) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
        if (mUnstableApController != null) {
            return mUnstableApController.isUnstableAp(bssid);
        }
        return false;
    }

    private boolean isWifiSharingProvisioning() {
         WifiManager mManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
         Log.i(TAG, "getProvisionSuccess : " + mManager.getProvisionSuccess()+ " isWifiSharingEnabled " +mManager.isWifiSharingEnabled());
         return mManager.isWifiSharingEnabled() && (mManager.getProvisionSuccess() == WifiManager.PROVISION_FAILED);
    }
    
    private void updatePasspointNetworkSelectionStatus(boolean enabled) {
        if (enabled) {
            return;
        }
        List<WifiConfiguration> savedNetworks = mWifiConfigManager.getConfiguredNetworks();
        for (WifiConfiguration network : savedNetworks) {
            if (!network.isPasspoint() || network.networkId == WifiConfiguration.INVALID_NETWORK_ID) {
                continue;
            }
            mWifiConfigManager.disableNetwork(network.networkId, network.creatorUid);
            mWifiConfigManager.removeNetwork(network.networkId, network.creatorUid);
        }
    }

    private void removePasspointNetworkIfSimAbsent() {
        List<WifiConfiguration> savedNetworks = mWifiConfigManager.getConfiguredNetworks();
        for (WifiConfiguration network : savedNetworks) {
            if (!network.isPasspoint() || network.networkId == WifiConfiguration.INVALID_NETWORK_ID) {
                continue;
            }

            int currentEapMethod = network.enterpriseConfig.getEapMethod();
            if (TelephonyUtil.isSimEapMethod(currentEapMethod)) {
                Log.w(TAG, "removePasspointNetworkIfSimAbsent : network "+network.configKey()+" try to remove");
                mWifiConfigManager.disableNetwork(network.networkId, network.creatorUid);
                mWifiConfigManager.removeNetwork(network.networkId, network.creatorUid);
            }
        }
    }

    public boolean isPasspointEnabled() {
        return mIsPasspointEnabled;
    }

    private void updateVendorApSimState() {
        boolean isUseableVendorUsim = false;
        isUseableVendorUsim = TelephonyUtil.isVendorApUsimUseable(getTelephonyManager());
        Log.i(TAG, "updateVendorApSimState : " + isUseableVendorUsim);
        mPasspointManager.setVendorSimUseable(isUseableVendorUsim);
        Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_USEABLE_VENDOR_USIM, isUseableVendorUsim ? 1 : 0);
    }
    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20 -]

    // SEC_PRODUCT_FEATURE_WLAN_DEBUG_LEVEL
    private void updateWlanDebugLevel() {
       String memdumpInfo = mWifiNative.getVendorConnFileInfo(7 /*.memdump.info*/);
       Log.i(TAG, "updateWlanDebugLevel : current level is " + memdumpInfo);
       if (memdumpInfo != null) {
           if(!memdumpInfo.equals("2")) {
               if(mWifiNative.putVendorConnFile(7, "2"))
                   Log.i(TAG, "updateWlanDebugLevel : update to 2 succeed");
               else
                   Log.i(TAG, "updateWlanDebugLevel : update to 2 failed");
            }
        }
    }

//[SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
    boolean checkAndShowSimRemovedDialog(WifiConfiguration config) {
        WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
        if (config.semIsVendorSpecificSsid
                && enterpriseConfig != null
                && (enterpriseConfig.getEapMethod() == WifiEnterpriseConfig.Eap.AKA
                    || enterpriseConfig.getEapMethod() == WifiEnterpriseConfig.Eap.AKA_PRIME)) {
            int simState = getTelephonyManager().getSimState();
            Log.i(TAG, "simState is " + simState + " for " + config.SSID);
            if (simState == TelephonyManager.SIM_STATE_ABSENT || simState == TelephonyManager.SIM_STATE_UNKNOWN) {
                Log.d(TAG, "trying to connect without SIM, show alert dialog");
                SemWifiFrameworkUxUtils.showWarningDialog(mContext,
                        SemWifiFrameworkUxUtils.WARN_SIM_REMOVED,
                        new String[] {StringUtil.removeDoubleQuotes(config.SSID)});
                return true;
            }
        }
        return false;
    }

    void showEapNotificationToast(int code) {
        Log.i(TAG, "eap code : " + code + ", targetId: " + mTargetNetworkId);
        if (code == EAP_NOTIFICATION_NO_NOTIFICATION_INFORMATION)
            return;
        if (CSC_WIFI_SUPPORT_VZW_EAP_AKA) { //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
            SemWifiFrameworkUxUtils.showEapToastVzw(mContext, code);
        } else if (CSC_WIFI_ERRORCODE) { //TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE
            SemWifiFrameworkUxUtils.showEapToast(mContext, code);
        }
    }

    private int mLastEAPFailureNetworkId = -1;
    private int mLastEAPFailureCount = 0;
    private boolean checkAndRetryConnect(int targetNetworkId) {
        if (mLastEAPFailureNetworkId != targetNetworkId) {
            mLastEAPFailureNetworkId = targetNetworkId;
            mLastEAPFailureCount = 0;
        }
        if (++mLastEAPFailureCount > 3) {
            return false;
        }
        return true;
    }

    void processMessageForEap(int event, int status, String message) {
        String noti_status = (status == EAP_NOTIFICATION_NO_NOTIFICATION_INFORMATION) ? "none":Integer.toString(status);
        Log.i(TAG, "eap message : event ["+event+"] , status ["+ noti_status+"] , message '"+message+"', targetId: " + mTargetNetworkId);
        WifiConfiguration currentConfig = null;
        if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
            currentConfig = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
        }
        if (currentConfig == null) {
            Log.e(TAG, "ignore eap message : currentConfig is null");
            StringBuilder eapLogTemp = new StringBuilder();
            eapLogTemp.append("events: { EAP_EVENT_" + event + "},");
            eapLogTemp.append(" extra_info: { " + message + " }");
            mWifiMetrics.logStaEvent(StaEvent.TYPE_EAP_ERROR, eapLogTemp.toString());
            return;
        }
        if (event < 1) {
            Log.e(TAG, "ignore eap message : event is not defined");
            return;
        }
        boolean hasEverConnected = currentConfig.getNetworkSelectionStatus().getHasEverConnected();
        boolean isNetworkPermanentlyDisabled = currentConfig.getNetworkSelectionStatus().isNetworkPermanentlyDisabled();
        String eapEventMsg = null;
        switch (event) {
            case EAP_EVENT_ANONYMOUS_IDENTITY_UPDATED:
                updateAnonymousIdentity(mTargetNetworkId);
                eapEventMsg = "ANONYMOUS_IDENTITY_UPDATED ";
                break;
            case EAP_EVENT_DEAUTH_8021X_AUTH_FAILED:
                if (TelephonyUtil.isSimEapMethod(currentConfig.enterpriseConfig.getEapMethod())) {
                    updateSimNumber(mTargetNetworkId);
                }
                Log.i(TAG, "network "+currentConfig.configKey()+" has ever connected "+hasEverConnected+", isNetworkPermanentlyDisabled, "+isNetworkPermanentlyDisabled);
                if (!isNetworkPermanentlyDisabled) {
                    mWifiConfigManager.updateNetworkSelectionStatus(mTargetNetworkId,
                            WifiConfiguration.NetworkSelectionStatus.DISABLED_AUTHENTICATION_FAILURE);
                    if (checkAndRetryConnect(mTargetNetworkId)) {
                        Log.w(TAG, "update network status to auth failure , retry to conect ");
                        startConnectToNetwork(mTargetNetworkId, Process.WIFI_UID, SUPPLICANT_BSSID_ANY);
                    }
                    //for black list processing
                    Log.w(TAG, "trackBssid for 802.1x auth failure : "+message);
                    if (!TextUtils.isEmpty(message) && !"00:00:00:00:00:00".equals(message)) {
                        mWifiConnectivityManager.trackBssid(message, false, WifiConfiguration.NetworkSelectionStatus
                            .DISABLED_AUTHENTICATION_FAILURE);
                    }
                }
                eapEventMsg = "DEAUTH_8021X_AUTH_FAILED ";
                break;
            case EAP_EVENT_EAP_FAILURE:
                if (TelephonyUtil.isSimEapMethod(currentConfig.enterpriseConfig.getEapMethod())) {
                    updateSimNumber(mTargetNetworkId);
                }
                Log.i(TAG, "network "+currentConfig.configKey()+" has ever connected "+hasEverConnected+", isNetworkPermanentlyDisabled, "+isNetworkPermanentlyDisabled);
                int currentEapMethod = currentConfig.enterpriseConfig.getEapMethod();
                if (!hasEverConnected) {
                    if (currentEapMethod == WifiEnterpriseConfig.Eap.PEAP
                        || currentEapMethod == WifiEnterpriseConfig.Eap.TTLS
                        || currentEapMethod == WifiEnterpriseConfig.Eap.PWD) {
                        Log.i(TAG, "update network status to wrong password ");
                        mWifiConfigManager.updateNetworkSelectionStatus(mTargetNetworkId,
                                        WifiConfiguration.NetworkSelectionStatus.DISABLED_BY_WRONG_PASSWORD);
                    } else if (!isNetworkPermanentlyDisabled) {
                        mWifiConfigManager.updateNetworkSelectionStatus(mTargetNetworkId,
                              WifiConfiguration.NetworkSelectionStatus.DISABLED_AUTHENTICATION_FAILURE);
                        if (checkAndRetryConnect(mTargetNetworkId)) {
                            Log.w(TAG, "update network status to eap failure , retry to conect , "+mLastEAPFailureCount);
                            startConnectToNetwork(mTargetNetworkId, Process.WIFI_UID, SUPPLICANT_BSSID_ANY);
                        }
                    }
                }
                eapEventMsg = "FAIL ";
                break;
            case EAP_EVENT_ERROR_MESSAGE:
                eapEventMsg = "ERROR ";
                break;
            case EAP_EVENT_LOGGING:
                eapEventMsg = "LOG ";
                break;
            case EAP_EVENT_NO_CREDENTIALS:
                eapEventMsg = "NO_CREDENTIALS ";
                break;
            case EAP_EVENT_NOTIFICATION:
                showEapNotificationToast(status);
                eapEventMsg = "NOTIFICATION ";
                break;
            case EAP_EVENT_SUCCESS:
                eapEventMsg = "SUCCESS ";
                break;
            case EAP_EVENT_TLS_CERT_ERROR:
                if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) { //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM (3.1)
                    CertificatePolicy certPolicy = new CertificatePolicy();
                    certPolicy.notifyCertificateFailureAsUser(CertificatePolicy.WIFI_MODULE, message, true, UserHandle.USER_OWNER);
                    WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_SECURITY,
                                false, TAG, "Certificate verification failed: " + message);
                }
                eapEventMsg = "TLS_CERT_ERROR ";
                break;
            case EAP_EVENT_TLS_ALERT:
            case EAP_EVENT_TLS_HANDSHAKE_FAIL:
                if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) { //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM (3.1)
                    // FAU_GEN.1/WLAN
                    WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_SECURITY,
                                false, TAG, "EAP-TLS handshake failed: " + message);
                }
                eapEventMsg = "TLS_HANDSHAKE_FAIL ";
                break;
            default:
                if (mVerboseLoggingEnabled) Log.e(TAG, "ignore eap message : not implementation yet");
                break;
        }
        StringBuilder eapLog = new StringBuilder();
        eapLog.append("events: { EAP_EVENT_" + eapEventMsg + "},");
        if (status != EAP_NOTIFICATION_NO_NOTIFICATION_INFORMATION) {
            eapLog.append(" notification_status=" + status);
        }
        eapLog.append(" extra_info: { " + message + " }");
        mWifiMetrics.logStaEvent(StaEvent.TYPE_EAP_ERROR, eapLog.toString());
    }
//]SEC_PRODUCT_FEATURE_WLAN_EAP_XXX

//[SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
    private int getConfiguredSimNum(WifiConfiguration config) {
        int simNum = 1;
        String simNumStr = config.enterpriseConfig.getSimNumber();
        if (simNumStr != null && !simNumStr.isEmpty()) {
            try {
                simNum = Integer.parseInt(simNumStr);
            } catch ( NumberFormatException e ) {
                Log.e(TAG, "getConfiguredSimNum - failed to getSimNumber ");
            }
        }
        Log.i(TAG, "getConfiguredSimNum - previous saved simNum:" + simNum);
        return simNum;
    }

    private boolean setPermanentIdentity(WifiConfiguration config) {
        if (mTargetNetworkId == WifiConfiguration.INVALID_NETWORK_ID) {
            Log.e(TAG, "PermanentIdentity : NetworkId is INVALID_NETWORK_ID");
            return false;
        }

        int simNum = getConfiguredSimNum(config);
        if (getTelephonyManager().getPhoneCount() > 1) {
            // for dual SIM model
            int multiSimState = TelephonyUtil.semGetMultiSimState(getTelephonyManager());
            if (multiSimState == TelephonyUtil.SLOT1_ONLY_READY) {
                simNum = 1;
            } else if (multiSimState == TelephonyUtil.SLOT2_ONLY_READY) {
                simNum = 2;
            }
        } else {
            simNum = 1; // for single SIM model
        }
        Log.i(TAG, "PermanentIdentity set simNum:" + simNum);
        config.enterpriseConfig.setSimNumber(simNum);
        TelephonyUtil.setSimIndex(simNum);
        Pair<String, String> identityPair = TelephonyUtil.getSimIdentity(getTelephonyManager(), new TelephonyUtil(), config, mWifiInjector.getCarrierNetworkConfig());
        if (identityPair == null || identityPair.first == null) {
            Log.i(TAG, "PermanentIdentity identityPair is invalid ");
            return false;
        }
        if (!config.semIsVendorSpecificSsid || TextUtils.isEmpty(identityPair.second)) {
            String oldIdentity = config.enterpriseConfig.getIdentity();
            if (oldIdentity != null && !oldIdentity.equals(identityPair.first) && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
                Log.d(TAG, "PermanentIdentity has been changed. setAnonymousIdentity to null for EAP method SIM/AKA/AKA'");
                config.enterpriseConfig.setAnonymousIdentity(null);
            }
            Log.d(TAG, "PermanentIdentity is set to : "+identityPair.first.substring(0, 7));
            config.enterpriseConfig.setIdentity(identityPair.first);
        } else {
            Log.i(TAG, "PermanentIdentity , identity is encrypted , need SUP_REQUEST_IDENTITY ");
            config.enterpriseConfig.setIdentity(null);
        }
        mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
        return true;
    }

    private void updateSimNumber(int netId) {
        Log.i(TAG, "updateSimNumber() netId : " + netId);
        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(netId);
        if (config != null && config.enterpriseConfig != null && TelephonyUtil.isSimConfig(config)) {
            int simNum = getConfiguredSimNum(config);
            if (getTelephonyManager().getPhoneCount() > 1) {
                // for dual SIM model
                int multiSimState = TelephonyUtil.semGetMultiSimState(getTelephonyManager());
                if (multiSimState == TelephonyUtil.SLOT1_ONLY_READY) {
                    simNum = 1;
                } else if (multiSimState == TelephonyUtil.SLOT2_ONLY_READY) {
                    simNum = 2;
                } else if (multiSimState == TelephonyUtil.SLOT12_BOTH_READY) {
                    if (simNum == 2) simNum = 1;
                    else simNum = 2;
                }
            } else {
                simNum = 1; // for single SIM model
            }
            Log.i(TAG, "updateSimNumber() set simNum:" + simNum);
            config.enterpriseConfig.setSimNumber(simNum);
            mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
        }
    }

    private void updateIdentityOnWifiConfiguration(WifiConfiguration config, String identity) {
        Log.i(TAG, "updateIdentityOnWifiConfiguration -  network :" + config.configKey());
        if (config.enterpriseConfig != null) {
            String oldIdentity = config.enterpriseConfig.getIdentity();
            if (!identity.equals(oldIdentity)) {
                Log.d(TAG, "updateIdentityOnWifiConfiguration -  Identity has been changed. setAnonymousIdentity to null for EAP method SIM/AKA/AKA'");
                config.enterpriseConfig.setIdentity(identity);
                config.enterpriseConfig.setAnonymousIdentity(null);
                mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
            }
        }
    }

    private void updateAnonymousIdentity(int netId) {
        Log.i(TAG, "updateAnonymousIdentity(" + netId + ")");
        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(netId);
        // We need to get the updated pseudonym from supplicant for EAP-SIM/AKA/AKA'
        if (config != null && config.enterpriseConfig != null
                && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())
                && !TextUtils.isEmpty(config.enterpriseConfig.getAnonymousIdentity())) {
            Log.i(TAG, "reset Anonymousidentity from supplicant, so reset it in WifiConfiguration.");
            config.enterpriseConfig.setAnonymousIdentity(null);
            mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
        }
    }
//]SEC_PRODUCT_FEATURE_WLAN_EAP_SIM

//CSC_SUPPORT_5G_ANT_SHARE
    private void sendIpcMessageToRilForLteu(int LTEU_WIFI_STATE, boolean isEnabled, boolean is5GHz, boolean forceSend) {
        int lteuState = 0;
        int lteuEnable = 0;
        lteuState = mLteuState;
        Log.d(TAG, "previous lteuState = " + lteuState + ", lteuEnable = " + mLteuEnable);
        if (isEnabled && is5GHz) {
            lteuState |= LTEU_WIFI_STATE;
        } else {
            lteuState &= ~LTEU_WIFI_STATE;
        }
        if (lteuState > 0 && lteuState < 8) {
            lteuEnable = 0;
        } else {
            lteuEnable = 1;
        }
        Log.d(TAG, "input = " + LTEU_WIFI_STATE + ", is5GHz = " + is5GHz);
        Log.d(TAG, "new lteuState = " + lteuState + ", lteuEnable = " + lteuEnable);
        if (forceSend || (mScellEnter && (lteuState != mLteuState))) {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                dos.writeByte(0x11);        // 0x11
                dos.writeByte(0x90);        // SET: 0x90, GET: 0x91
                dos.writeShort(0x05);       // size
                dos.writeByte(lteuEnable); // 0x00 or 0x01
            } catch (IOException e) {
                Log.e(TAG, "IOException occurs in set lteuEnable");
                return;
            } finally {
                try {
                    dos.close();
                } catch (Exception ex) {
                }
            }
            try {
                byte [] responseData = new byte[2048];
                if (phone != null) {
                    int ret = phone.invokeOemRilRequestRaw(bos.toByteArray(), responseData);
                    Log.i(TAG, "invokeOemRilRequestRaw : return value: " + ret);
                } else {
                    Log.d(TAG, "ITelephony is null");
                }
            } catch (RemoteException e) {
                Log.e(TAG, "invokeOemRilRequestRaw : RemoteException: " + e);
            }
        }
        mLteuState = lteuState;
        mLteuEnable = lteuEnable;
    }
//CSC_SUPPORT_5G_ANT_SHARE

    private int waitForDhcpRelease( ) { //CscFeature_Wifi_SendSignalDuringPowerOff
        int sleepTime = 500;
            try {
                Thread.sleep(sleepTime);
            }
            catch(InterruptedException ex) {
                loge("waitForDhcpRelease sleep exception:" + ex);
            }
        return 0;
    }
    // >>>WCM>>> callbacks of WCM
    // @hide
    private ArrayList<ClientModeChannel.WifiNetworkCallback> mWifiNetworkCallbackList = null;
    private boolean isSystem(int uid) {
        return uid < Process.FIRST_APPLICATION_UID;
    }
    public void registerWifiNetworkCallbacks (ClientModeChannel.WifiNetworkCallback wifiNetworkCallback) {
        if (!isSystem(Binder.getCallingUid())) {
            Log.w(TAG, "This is only for system service");
            throw new SecurityException("This is only for system service");
        }
        Log.w(TAG, "registerWCMCallbacks");
        if (mWifiNetworkCallbackList  == null) mWifiNetworkCallbackList = new ArrayList<WifiClientModeChannel.WifiNetworkCallback>();
        mWifiNetworkCallbackList.add(wifiNetworkCallback);
    }

    private void handleWifiNetworkCallbacks(int method) {
        switch(method) {
        case WifiClientModeChannel.CALLBACK_CHECK_IS_CAPTIVE_PORTAL_EXCEPTION:
            for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                wncb.checkIsCaptivePortalException(getTargetSsid());
            }
            break;
        case WifiClientModeChannel.CALLBACK_NOTIFY_ROAM_SESSION_START:
            mCntRoamingStartSent++;
            for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                wncb.notifyRoamSession("start");
            }
            break;
        case WifiClientModeChannel.CALLBACK_NOTIFY_ROAM_SESSION_COMPLETE:
            if (--mCntRoamingStartSent > 0) break;
            for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                wncb.notifyRoamSession("complete");
            }
            break;
        case WifiClientModeChannel.CALLBACK_NOTIFY_DHCP_SESSION_START:
            isDhcpStartSent = true;
            for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                wncb.notifyDhcpSession("start");
            }
            break;
        case WifiClientModeChannel.CALLBACK_NOTIFY_DHCP_SESSION_COMPLETE:
            if (!isDhcpStartSent) break;
            isDhcpStartSent = false;
            for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                wncb.notifyDhcpSession("complete");
            }
            break;
        case WifiClientModeChannel.CALLBACK_NOTIFY_LINK_PROPERTIES_UPDATED:
            for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                wncb.notifyLinkPropertiesUpdated(mLinkProperties);
            }
            break;
        case WifiClientModeChannel.CALLBACK_NOTIFY_REACHABILITY_LOST:
            for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                wncb.notifyReachabilityLost();
            }
            break;
        case WifiClientModeChannel.CALLBACK_NOTIFY_PROVISIONING_FAIL:
            for(ClientModeChannel.WifiNetworkCallback wncb : mWifiNetworkCallbackList) {
                wncb.notifyProvisioningFail();
            }
            break;
        }
        return;
    }

    public WifiClientModeChannel makeWifiClientModeChannel() {
        return new WifiClientModeChannel();
    }
    public int mCntRoamingStartSent = 0;
    public boolean isDhcpStartSent = false;
    public boolean mIsManualSelection = false;
    public class WifiClientModeChannel implements ClientModeChannel {
        public static final int CALLBACK_CHECK_IS_CAPTIVE_PORTAL_EXCEPTION = 1;
        public static final int CALLBACK_FETCH_RSSI_PKTCNT_SUCCESS         = 2;
        public static final int CALLBACK_FETCH_RSSI_PKTCNT_FAILED          = 3;
        public static final int CALLBACK_FETCH_RSSI_PKTCNT_DISCONNECTED    = 4;
        public static final int CALLBACK_NOTIFY_ROAM_SESSION_START         = 5;
        public static final int CALLBACK_NOTIFY_ROAM_SESSION_COMPLETE      = 6;
        public static final int CALLBACK_NOTIFY_LINK_PROPERTIES_UPDATED    = 7;
        public static final int CALLBACK_NOTIFY_DHCP_SESSION_START         = 8;
        public static final int CALLBACK_NOTIFY_DHCP_SESSION_COMPLETE      = 9;
        public static final int CALLBACK_NOTIFY_REACHABILITY_LOST          = 10;
        public static final int CALLBACK_NOTIFY_PROVISIONING_FAIL          = 11;

        public WifiClientModeChannel() {
            mCntRoamingStartSent = 0;
            isDhcpStartSent = false;
        }
        // method to let other module to call
        public Message fetchPacketCountNative() {
            Message msg = obtainMessage();
            if (!isConnected()) {
                msg.what = WifiClientModeChannel.CALLBACK_FETCH_RSSI_PKTCNT_DISCONNECTED;
                return msg;
            }
            fetchRssiLinkSpeedAndFrequencyNative();
            WifiNative.TxPacketCounters counters =
                    mWifiNative.getTxPacketCounters(mInterfaceName);
            if (counters != null) {
                msg.what = WifiClientModeChannel.CALLBACK_FETCH_RSSI_PKTCNT_SUCCESS;
                msg.arg1 = counters.txSucceeded;
                msg.arg2 = counters.txFailed;
            } else {
                msg.what = WifiClientModeChannel.CALLBACK_FETCH_RSSI_PKTCNT_FAILED;
            }
            return msg;
        }
        public void setWifiNetworkEnabled(boolean valid) {
            mWifiScoreReport.setWifiNetworkEnabled(valid);
        }
        public boolean getManualSelection() {
            return mIsManualSelection;
        }
        public void setCaptivePortal(int netId, boolean captivePortal) {
            sendMessage(WifiConnectivityMonitor.CMD_CONFIG_SET_CAPTIVE_PORTAL, netId, captivePortal ? 1 : 0);
        }
        public void updateNetworkSelectionStatus(int netId, int reason) {
            sendMessage(WifiConnectivityMonitor.CMD_CONFIG_UPDATE_NETWORK_SELECTION, netId, reason);
        }
        //
    }
    // <<<WCM<<<
    private void sendWcmConfigurationChanged() {
        Intent intent = new Intent();
        intent.setAction("ACTION_WCM_CONFIGURATION_CHANGED");
        if (mContext != null) {
            try {
                mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            } catch (IllegalStateException e) {
                loge("Send broadcast - action:" + intent.getAction());
            }
        }
    }
    public interface ClientModeChannel {
        public interface WifiNetworkCallback {
            public abstract void checkIsCaptivePortalException(String ssid);
            public abstract void notifyRoamSession(String startComplete);
            public abstract void notifyDhcpSession(String startComplete);
            public abstract void notifyLinkPropertiesUpdated(LinkProperties lp);
            public abstract void handleResultRoamInLevel1State(boolean roamFound);
            public abstract void notifyReachabilityLost();
            public abstract void notifyProvisioningFail();
        }
        public abstract Message fetchPacketCountNative();
    }

    private boolean isPoorNetworkTestEnabled() {
        return Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, 0) == 1;
    }

    private boolean isWCMEnabled() {
        if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
                || SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_MHS_DONGLE
                || "REMOVED".equals(SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGSNSSTATUS))) {
            return false;
        }
        return true;
    }
    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
    public void setIWCMonitorAsyncChannel (Handler handler) {
            log("setIWCAsyncChannel");
            if (mIWCMonitorChannel == null){
                if (DBG) log("new mWcmChannel created");
                mIWCMonitorChannel = new AsyncChannel();
            }
            if (DBG) log("mWcmChannel connected");
            mIWCMonitorChannel.connect(mContext, getHandler(), handler);
    }
    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION

    //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
    private void setAnalyticsUserDisconnectReason(short reason) {
        Log.d(TAG, "setAnalyticsUserDisconnectReason " + reason);
        mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, reason);
    }
    //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
    // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
    private void notifyMobilewipsRoamEvent(String startComplete) {
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
            if (MobileWipsFrameworkService.getInstance() != null && startComplete.equals("start")) {
                MobileWipsFrameworkService.getInstance()
                        .sendEmptyMessage(MobileWipsDef.EVENT_ROAMING_STARTED);
            }
        }
    }
     // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION

    /**
     * Apply EDM Policy - Apply EDM policy to currently Connected Network called when Wi-Fi is connected or Policy Updated.
     * When policy exits in currently connected network, apply policy, if does not exists, clear policy
     */
    private void updateEDMWiFiPolicy() {
        mWifiConfigManager.forcinglyEnablePolicyUpdatedNetworks(Process.SYSTEM_UID); //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM - EDM Wi-Fi Configuration

        if (mWifiInfo == null || !isConnected()) {
            Log.d(TAG, "wifi Info is null or no connected AP.");
            return;
        }
        Log.d(TAG, "updateEDMWiFiPolicy. SSID: " + mWifiInfo.getSSID());
        if (mWifiInfo.getSSID() != null && !mWifiInfo.getSSID().isEmpty()) {
            WifiB2BConfigurationPolicy.B2BConfiguration conf = mWifiB2bConfigPolicy.getConfiguration(StringUtil.removeDoubleQuotes(mWifiInfo.getSSID()));
            if (conf != null) {
                //Roam Trigger Configuration
                boolean result = true;

                //check all is not configured
                if (conf.getRoamTrigger() == WifiB2BConfigurationPolicy.VAL_NOT_CONFIGURED
                        && conf.getRoamDelta() == WifiB2BConfigurationPolicy.VAL_NOT_CONFIGURED
                        && conf.getScanPeriod() == WifiB2BConfigurationPolicy.VAL_NOT_CONFIGURED) {
                    //policy update, so previous roam policy is all cleared
                    if (mWifiB2bConfigPolicy.isPolicyApplied()) {
                        Log.d(TAG, "No policy exists, clear previous Policy");
                        clearEDMWiFiPolicy();
                    }
                } else {
                    if (!mWifiB2bConfigPolicy.isPolicyApplied()) {
                         if (getNCHOVersion() == NCHO_VERSION_2_0) {
                            result = setNCHO20State(NCHO_VER2_STATE_ENABLED, true);
                         }
                        mWifiB2bConfigPolicy.setPolicyApplied(true);

                        Log.d(TAG, "updateEDMWiFiPolicy - setNCHOMode: " + result);
                    }

                    int roamTrigger = (conf.getRoamTrigger() == WifiB2BConfigurationPolicy.VAL_NOT_CONFIGURED) ?
                        WifiB2BConfigurationPolicy.WIFICONF_RSSI_THRESHOLD_DEFAULT : conf.getRoamTrigger();
                    mWifiNative.setRoamTrigger(mInterfaceName, roamTrigger);

                    int roamDelta = (conf.getRoamDelta() == WifiB2BConfigurationPolicy.VAL_NOT_CONFIGURED) ? 
                        WifiB2BConfigurationPolicy.WIFICONF_RSSI_ROAMDELTA_DEFAULT : conf.getRoamDelta();
                    mWifiNative.setRoamDelta(mInterfaceName, roamDelta);

                    int roamScanPediod = (conf.getScanPeriod() == WifiB2BConfigurationPolicy.VAL_NOT_CONFIGURED) ? 
                        WifiB2BConfigurationPolicy.WIFICONF_ROAM_SCANPERIOD_DEFAULT : conf.getScanPeriod();
                    mWifiNative.setRoamScanPeriod(mInterfaceName, roamScanPediod);
                }
                //Do not renew DHCP during the Roam
                if (conf.skipDHCPRenewal()) {
                    mRoamDhcpPolicyByB2bConfig = ROAM_DHCP_SKIP;
                } else {
                    mRoamDhcpPolicyByB2bConfig = ROAM_DHCP_DEFAULT;
                }
            }else {
                //Configuration does not exists, clear all poilcy.
                clearEDMWiFiPolicy();
            }
        }
    }

    /**
     * Clear EDM Policy - it is called when disconnected or policy is updated - No poilcy exists or policy has invalid value.
     */
    private void clearEDMWiFiPolicy() {
        Log.d(TAG, "clearEDMWiFiPolicy: " + mWifiB2bConfigPolicy.isPolicyApplied() + "/" + mIsNchoParamSet);
        mRoamDhcpPolicyByB2bConfig = ROAM_DHCP_DEFAULT;

        if (mWifiB2bConfigPolicy.isPolicyApplied() || mIsNchoParamSet) {
            int nchoVersion = getNCHOVersion();
            if (nchoVersion == NCHO_VERSION_1_0 && getNCHO10State() == NCHO_VER1_STATE_BACKUP) {
                 restoreNcho10Param();
            } else if (nchoVersion == NCHO_VERSION_2_0) {
                setNCHO20State(NCHO_VER2_STATE_DISABLED, true);
            }
        }
        mWifiB2bConfigPolicy.setPolicyApplied(false);
        mIsNchoParamSet = false; 
    }

    /**
     * Get NCHO version
     * return 0 - unknown, 1 - version 1.0, 2 - version 2.0
     */
    private int getNCHOVersion() {
        if (mNchoVersion == NCHO_VERSION_UNKNOWN) {
            if (mWifiState.get() != WifiManager.WIFI_STATE_ENABLED) {
                Log.e(TAG, "getNCHOVersion Wi-Fi is not enabled state");
                return NCHO_VERSION_UNKNOWN;
            }

            int result = mWifiNative.getNCHOMode(mInterfaceName);
            if (result == -1) {
                mNchoVersion = NCHO_VERSION_1_0;
                if (getNCHO10State() != NCHO_VER1_STATE_BACKUP) {
                    backUpNcho10Param();
                }
            } else if (result == NCHO_VER2_STATE_DISABLED || result == NCHO_VER2_STATE_ENABLED) {
                mNchoVersion = NCHO_VERSION_2_0;
                setNCHO20State(result, false);
            } else {
                Log.e(TAG, "getNCHOVersion Error: " + mNchoVersion);
                mNchoVersion = NCHO_VERSION_ERROR;
            }
        }

        if (mVerboseLoggingEnabled) Log.d(TAG, "getNCHOVersion Version: " + mNchoVersion);
        return mNchoVersion;
    }

    /**
     * Get NCHO version 1.0 state
     * return 0 - error, 1 - init state, need to backup, 2 - backup is done
     */
    private int getNCHO10State() {
        if (getNCHOVersion() != NCHO_VERSION_1_0) {
            Log.e(TAG, "getNCHO10State version is not 1.0"); 
            return NCHO_VER1_STATE_ERROR;
        }

        if (mVerboseLoggingEnabled) Log.d(TAG, "getNCHO10State: " + mNcho10State); 
        return mNcho10State;
    }

    /**
     * Set NCHO version 1.0 state. After resotre is done in version 1.0, this state is not changed.
     * param -1 : error, 0 - init state, need to backup, 1 - back up is done
     * return true - successfully set, false - fail to set
     */
    private boolean setNCHO10State(int state) {
        if (getNCHOVersion() != NCHO_VERSION_1_0) {
            Log.e(TAG, "setNCHO10State version is not 1.0");
            return false;
        }

        if (mVerboseLoggingEnabled) Log.d(TAG, "setNCHO10State: " + state);
        mNcho10State = state;
        return true;
    }

    /**
     * Get NCHO version 2.0 state
     * return 0 - unknown, 1 - disabled state, 2 - enabled state
     */
    private int getNCHO20State() {
        if (getNCHOVersion() != NCHO_VERSION_2_0) {
            Log.e(TAG, "getNCHO20State version is not 2.0"); 
            return NCHO_VER2_STATE_ERROR;
        }

        if (mVerboseLoggingEnabled) Log.d(TAG, "getNCHO20State: " + mNcho20State);
        return mNcho20State;
    }

    /**
     * Set NCHO version 2.0 state
     * param -1 - state error, 0 - unknown, 1 - disabled state, 2 - enabled state
     * return true - successfully set, false - fail to set
     */
    private boolean setNCHO20State(int state, boolean setToDriver) {
        boolean result = true;
        if (getNCHOVersion() != NCHO_VERSION_2_0) {
            Log.e(TAG, "setNCHO20State version is not 2.0");
            return false;
        }
        if (mVerboseLoggingEnabled) Log.d(TAG, "setNCHO20State: " + state + ", setToDriver: " + setToDriver);

        if (setToDriver) 
            result = mWifiNative.setNCHOMode(mInterfaceName, state);

        if (result)
            mNcho20State = state;
        else
            Log.e(TAG, "setNCHO20State setNCHOMode fail");

        return result;
    }

    /**
     * Back up B2B parameters for the case NCHO 1.0 version.
     * return true - success, false - failed
     */
    private boolean backUpNcho10Param() {
        if (getNCHOVersion() != NCHO_VERSION_1_0) {
            Log.e(TAG, "backUpNcho10Param NCHO version is not 1.0");
            return false;
        }

        if (getNCHO10State() == NCHO_VER1_STATE_BACKUP ) {
            Log.e(TAG, "backUpNcho10Param already backed up");
            return false;
        }

        mDefaultRoamTrigger = mWifiNative.getRoamTrigger(mInterfaceName);
        if (mDefaultRoamTrigger == -1)
            mDefaultRoamTrigger = WifiB2BConfigurationPolicy.WIFICONF_RSSI_THRESHOLD_DEFAULT;
        mDefaultRoamDelta = mWifiNative.getRoamDelta(mInterfaceName);
        if (mDefaultRoamDelta == -1)
            mDefaultRoamDelta = WifiB2BConfigurationPolicy.WIFICONF_RSSI_ROAMDELTA_DEFAULT;
        mDefaultRoamScanPeriod = mWifiNative.getRoamScanPeriod(mInterfaceName);
        if (mDefaultRoamScanPeriod == -1)
            mDefaultRoamScanPeriod = WifiB2BConfigurationPolicy.WIFICONF_ROAM_SCANPERIOD_DEFAULT;

        setNCHO10State(NCHO_VER1_STATE_BACKUP);
        Log.d(TAG, "ncho10BackUp: " + mDefaultRoamTrigger + "/" + mDefaultRoamDelta + "/" + mDefaultRoamScanPeriod);
        return true;
    }

    /**
     * Back up B2B parameters for the case NCHO 1.0 version.
     * return true - success, false - failed
     */
    private boolean restoreNcho10Param() {
        if (getNCHOVersion() != NCHO_VERSION_1_0) {
            Log.e(TAG, "ncho10BackUp NCHO version is not 1.0");
            return false;
        }

        if (getNCHO10State() != NCHO_VER1_STATE_BACKUP ) {
            Log.e(TAG, "ncho 10 is not backed up");
            return false;
        }

        //Restore default value
        Log.d(TAG, "restoreNcho10Param: " + mDefaultRoamTrigger + "/" + mDefaultRoamDelta + "/" + mDefaultRoamScanPeriod);
        mWifiNative.setRoamTrigger(mInterfaceName, mDefaultRoamTrigger);
        mWifiNative.setRoamDelta(mInterfaceName, mDefaultRoamDelta);
        mWifiNative.setRoamScanPeriod(mInterfaceName, mDefaultRoamScanPeriod);
        return true;
    }
    //End EDM Wi-Fi Configuration

    public int setRoamDhcpPolicy(int mode) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
        mRoamDhcpPolicy = mode;
        Log.d(TAG, "Set mRoamDhcpPolicy : " + mRoamDhcpPolicy);
        return mRoamDhcpPolicy;
    }

    public int getRoamDhcpPolicy() { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
        Log.d(TAG, "Get mRoamDhcpPolicy : " + mRoamDhcpPolicy);
        return mRoamDhcpPolicy;
    }

    //+ SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
    private SemLocationListener mSemLocationListener = new SemLocationListener() {
        @Override
        public void onLocationAvailable(Location[] locations) {
        }
        @Override
        public void onLocationChanged(Location location, Address address) {
            Log.d(TAG,"onLocationChanged is called");
            if (mLocationRequestNetworkId == WifiConfiguration.INVALID_NETWORK_ID) {
                if (DBG) Log.d(TAG,"There is no config to update location");
                return;
            }
            if (location == null) {
                Log.d(TAG,"onLocationChanged is called but location is null");
                return;
            }
            WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(mLocationRequestNetworkId);
            if (config == null) {
                Log.d(TAG,"Try to updateLocation but config is null");
                return;
            }
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            if (isValidLocation(latitude,longitude)) {
                mWifiGeofenceManager.setLatitudeAndLongitude(config, latitude, longitude);
                mLocationRequestNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
            }
        }
    };

    boolean isValidLocation(double latitude, double longitude) {
        //latitude ranges from -90 to 90 and longitude ranges from -180 to 180
        if (latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180) {
            return true;
        }
        Log.d(TAG,"invalid location");
        return false;
    }

    void updateLocation(int isActiveRequest) {
        Log.d(TAG,"updateLocation " + isActiveRequest);
        mSemLocationManager = (SemLocationManager) mContext.getSystemService(Context.SEM_LOCATION_SERVICE);
        if (isActiveRequest == ACTIVE_REQUEST_LOCATION) {
            mSemLocationManager.requestSingleLocation(100, 30, false, mSemLocationListener);
        } else {
            Intent intent = new Intent(ACTION_AP_LOCATION_PASSIVE_REQUEST);
            mLocationPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
            mSemLocationManager.requestPassiveLocation(mLocationPendingIntent);
        }
    }

    private static final int[] MHS_PRIVATE_NETWORK_MASK = {0x002BA8C0, 0x000A14AC}; //192.168.43.x, 172.20.10.x
    private boolean isLocationSupportedAp(WifiConfiguration config) {
        if (config == null) return false;
        if (config.semSamsungSpecificFlags.get(WifiConfiguration.SamsungFlag.SEC_MOBILE_AP)
                || config.semIsVendorSpecificSsid) {
            Log.d(TAG, "This is a Samsung Hotspot");
            return false;
        }
        if (mWifiInfo != null) {
            for (int mask : MHS_PRIVATE_NETWORK_MASK) {
                if ((mWifiInfo.getIpAddress() & 0x00FFFFFF) == mask) {
                    Log.d(TAG, "This is a Mobile Hotspot");
                    return false;
                }
            }
        }
        if (config.semIsVendorSpecificSsid) {
            Log.d(TAG, "This is vendor AP");
            return false;
        }
        return true;
    }

    //Return latitude and longitude to Double array type
    public double[] getLatitudeLongitude(WifiConfiguration config) {
        String latitudeLongitude = mWifiGeofenceManager.getLatitudeAndLongitude(config);
        double[] latitudeLongitudeDouble = new double[2];
        if (latitudeLongitude != null) {
            String[] latitudeLongitudeString = latitudeLongitude.split(":");
            latitudeLongitudeDouble[0] = Double.parseDouble(latitudeLongitudeString[0]);
            latitudeLongitudeDouble[1] = Double.parseDouble(latitudeLongitudeString[1]);
        } else {
            latitudeLongitudeDouble[0] = INVALID_LATITUDE_LONGITUDE;
            latitudeLongitudeDouble[1] = INVALID_LATITUDE_LONGITUDE;
        }
        return latitudeLongitudeDouble;
    }
    //- SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION

    // >>>WCM>>>
    // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP  = 1;
    static final int SECURITY_PSK  = 2;
    static final int SECURITY_EAP  = 3;
    static final int SECURITY_SAE  = 4;
    public void checkAlternativeNetworksForWmc(boolean mNeedRoamingInHighQuality) {
        sendMessage(WifiConnectivityMonitor.CHECK_ALTERNATIVE_NETWORKS,
                (mNeedRoamingInHighQuality ? 1 : 0));
    }
    public void startScanFromWcm() {
        mScanRequestProxy.startScan(Process.SYSTEM_UID, mContext.getOpPackageName());
    }
    // <<<WCM<<<

//++SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP
    private boolean checkIfForceRestartDhcp() {
        if (getRoamDhcpPolicy() == ROAM_DHCP_RESTART) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
            if (DBG) log("ForceRestartDhcp by uready");
            return true;
        }
        String ssid = mWifiInfo.getSSID();
        if ((ssid != null) && (ssid.contains("marente")  || ssid.contains("0001docomo")
                || ssid.contains("ollehWiFi") || ssid.contains("olleh GiGA WiFi")
                || ssid.contains("KT WiFi")|| ssid.contains("KT GiGA WiFi"))) {
            if (DBG) log("ForceRestartDhcp");
            return true;
        }
        return false;
    }

    private void restartDhcp(WifiConfiguration wc) {
        if (wc == null) {
            Log.d(TAG, "Stop restarting Dhcp as currentConfig is null");
            return;
        }
        if (mIpClient != null) {
            mIpClient.stop();
        }
        setTcpBufferAndProxySettingsForIpManager();
        final ProvisioningConfiguration prov;
        prov = new ProvisioningConfiguration.Builder()
                    .withPreDhcpAction()
                    .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                    .withNetwork(getCurrentNetwork())
                    .withDisplayName(wc.SSID)
                    .withRandomMacAddress()
                    .build();
        if (mIpClient != null) {
            mIpClient.startProvisioning(prov);
        }
    }

    private void CheckIfDefaultGatewaySame() {
        Log.d(TAG, "CheckIfDefaultGatewaySame");
        removeMessages(CMD_CHECK_ARP_RESULT);
        removeMessages(CMD_SEND_ARP);
        InetAddress inetAddress = null;
        InetAddress gateway = null;

        for (LinkAddress la : mLinkProperties.getLinkAddresses()) {
            if (la.getAddress() instanceof Inet4Address) {
                inetAddress = la.getAddress();
                break;
            }
        }
        for (RouteInfo route : mLinkProperties.getRoutes()) {
            if (route.getGateway() instanceof Inet4Address) {
                gateway = route.getGateway();
            }
        }

        if (inetAddress != null && gateway != null) {
            try {
                ArpPeer peer = new ArpPeer();
                peer.checkArpReply(mLinkProperties, 1000, gateway.getAddress(), inetAddress.getAddress(), mWifiInfo.getMacAddress());

                if (mIpClient != null) {
                    mIpClient.confirmConfiguration();
                }
                sendMessageDelayed(CMD_SEND_ARP, 100);
                sendMessageDelayed(CMD_CHECK_ARP_RESULT, 1000);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e);
            }
        } else {
            restartDhcp(getCurrentWifiConfiguration());
        }
    }
//--SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP

    //SEC_DISABLE_MAC_RANDOMIZATION_FOR_KOR_CARRIER_HOTSPOT
    private boolean isSupportRandomMac(WifiConfiguration config) {
        if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_RANDOM_MAC_FOR_CONNECTION) return false; //random Mac SECFEATURE
        if (config != null) {
            String ssid = StringUtil.removeDoubleQuotes(config.SSID);
            if (config.isPasspoint()) {
                return false;
            }
            if (config.semIsVendorSpecificSsid) {
                if (Vendor.DCM == mOpBranding && ("0000docomo".equals(ssid) || "0001docomo".equals(ssid))) {
                    return true;
                }
                return false;
            }
            int configuredSecurity = -1;
            if (config.allowedKeyManagement.get(KeyMgmt.WPA_PSK)
                    || config.allowedKeyManagement.get(KeyMgmt.WPA2_PSK)) {
                configuredSecurity = SECURITY_PSK;
            }
            if (mOpBranding.getCountry() == Vendor.COUNTRY_KOREA
                    && ("ollehWiFi ".equals(ssid) || "olleh GiGA WiFi ".equals(ssid) || "KT WiFi ".equals(ssid)
                    || "KT GiGA WiFi ".equals(ssid) || "T wifi zone".equals(ssid)
                    || "U+zone".equals(ssid) || "U+zone_5G".equals(ssid) || "5G_U+zone".equals(ssid))) {
                return false;
            }
        }
        return true;
    }
    //SEC_DISABLE_MAC_RANDOMIZATION_FOR_KOR_CARRIER_HOTSPOT

    //+SEC_PRODUCT_FEATURE_WLAN_SWITCHBOARD
    public void enablePollingRssiForSwitchboard(boolean enable, int newPollIntervalMsecs ) {
        if (enable)
            mRssiPollingScreenOffEnabled |= RSSI_POLL_ENABLE_DURING_LCD_OFF_FOR_SWITCHBOARD;
        else
            mRssiPollingScreenOffEnabled &= ~RSSI_POLL_ENABLE_DURING_LCD_OFF_FOR_SWITCHBOARD;
        setPollRssiIntervalMsecs(newPollIntervalMsecs);
        if (mRssiPollingScreenOffEnabled != 0) {
            if (!mEnableRssiPolling) {
                enableRssiPolling(true);
            }
        }
        else {
            if (mEnableRssiPolling && !mScreenOn) {
                enableRssiPolling(false);
            }
        }
    }
    //-SEC_PRODUCT_FEATURE_WLAN_SWITCHBOARD

    // SWITCH_FOR_INDIVIDUAL_APPS_FEATURE - START
    public int getBeaconCount() {
        return mRunningBeaconCount;
    }
    // SWITCH_FOR_INDIVIDUAL_APPS_FEATURE - END

    //+SEC_PRODUCT_FEATURE_WLAN_GET_ROAMING_STATUS_FOR_MOBIKE
    private void setRoamTriggered(boolean enabled) {
        mIsRoaming = enabled;
        // >>>WCM>>>
        if (enabled) {
            handleWifiNetworkCallbacks(WifiClientModeChannel.CALLBACK_NOTIFY_ROAM_SESSION_START);
        } else {
            handleWifiNetworkCallbacks(WifiClientModeChannel.CALLBACK_NOTIFY_ROAM_SESSION_COMPLETE);
        }
        // <<<WCM<<<
    }

    private boolean isRoaming() {
        return mIsRoaming;
    }
    //-SEC_PRODUCT_FEATURE_WLAN_GET_ROAMING_STATUS_FOR_MOBIKE

    private boolean detectIpv6ProvisioningFailure(LinkProperties oldLp, LinkProperties newLp) {
        if (oldLp == null) {
            return false;
        }
        if (newLp == null) {
            return false;
        }
        final boolean lostIPv6 = oldLp.isIpv6Provisioned() && !newLp.isIpv6Provisioned();
        final boolean lostIPv4Address = oldLp.hasIpv4Address() && !newLp.hasIpv4Address();
        final boolean lostIPv6Router = oldLp.hasIpv6DefaultRoute() && !newLp.hasIpv6DefaultRoute();

        if (lostIPv4Address) {
            return false;
        }
        if (lostIPv6) {
            Log.d(TAG, "lostIPv6");
            return true;
        }

        if (oldLp.hasGlobalIpv6Address() && lostIPv6Router) {
            Log.d(TAG, "return true by ipv6 provisioning failure");
            return true;            
        }

        return false;
    }
}
