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

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static android.net.wifi.WifiManager.WIFI_STATE_ENABLING;
import static android.net.wifi.WifiManager.WIFI_STATE_UNKNOWN;
//+SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import static android.sec.enterprise.WifiPolicy.ACTION_ENABLE_NETWORK_INTERNAL;
import static android.sec.enterprise.WifiPolicy.EXTRA_ENABLE_OTHERS_INTERNAL;
import static android.sec.enterprise.WifiPolicy.EXTRA_NETID_INTERNAL;
//-SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.DhcpResults;
import android.net.IpConfiguration;
import android.net.KeepalivePacketData;
import android.net.LinkProperties;
import android.net.MacAddress;
import android.net.metrics.IpManagerEvent;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.TrafficStats;
import android.net.dhcp.DhcpClient;
import android.net.ip.IpClient;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.RssiPacketCountInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt; //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiSsid;
import android.net.wifi.hotspot2.IProvisioningCallback;
import android.net.wifi.hotspot2.OsuProvider;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.net.wifi.p2p.IWifiP2pManager;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager; //CSC_SUPPORT_5G_ANT_SHARE
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
//+SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import android.sec.enterprise.auditlog.AuditLog;
import android.sec.enterprise.auditlog.AuditEvents;
import android.sec.enterprise.certificate.CertificatePolicy; //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM (MDM 5.0)
import android.sec.enterprise.content.SecContentProviderURI;
import android.sec.enterprise.EnterpriseDeviceManager;
import android.sec.enterprise.WifiPolicy;
import android.sec.enterprise.WifiPolicyCache;
//-SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import android.system.OsConstants;
//+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
//-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.hardware.input.InputManager;
import com.android.server.wifi.WifiController.P2pDisableListener;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.telephony.ITelephony; //CSC_SUPPORT_5G_ANT_SHARE
import com.android.internal.telephony.TelephonyIntents; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
import com.android.internal.util.AsyncChannel;
import com.android.internal.util.MessageUtils;
import com.android.internal.util.Protocol;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.hotspot2.anqp.ANQPElement; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
import com.android.server.wifi.hotspot2.anqp.Constants; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
import com.android.server.wifi.hotspot2.anqp.HSWanMetricsElement; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
import com.android.server.wifi.hotspot2.anqp.VenueNameElement; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
import com.android.server.wifi.hotspot2.AnqpEvent;
import com.android.server.wifi.hotspot2.IconEvent;
import com.android.server.wifi.hotspot2.NetworkDetail;
import com.android.server.wifi.hotspot2.PasspointManager;
import com.android.server.wifi.hotspot2.WnmData;
import com.android.server.wifi.hotspot2.Utils; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
import com.android.server.wifi.nano.WifiMetricsProto;
import com.android.server.wifi.nano.WifiMetricsProto.StaEvent;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.ScanResultUtil; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST
import com.android.server.wifi.util.StringUtil;
import com.android.server.wifi.util.TelephonyUtil;
import com.android.server.wifi.util.TelephonyUtil.SimAuthRequestData;
import com.android.server.wifi.util.TelephonyUtil.SimAuthResponseData;
import com.android.server.wifi.util.WifiPermissionsUtil;
import com.android.server.wifi.util.WifiPermissionsWrapper;

import com.samsung.android.feature.SemFloatingFeature;
import com.samsung.android.feature.SemCscFeature;
import com.samsung.android.location.SemLocationListener;
import com.samsung.android.location.SemLocationManager;
import com.samsung.android.net.wifi.OpBrandingLoader;
import com.samsung.android.net.wifi.OpBrandingLoader.Vendor;
import com.samsung.android.server.wifi.bigdata.WifiBigDataLogManager;
import com.samsung.android.server.wifi.bigdata.WifiChipInfo;
import com.samsung.android.server.wifi.dqa.ReportIdKey;
import com.samsung.android.server.wifi.dqa.ReportUtil;
import com.samsung.android.server.wifi.dqa.SemWifiIssueDetector;
import com.samsung.android.server.wifi.SemSarManager; //SEMSAR
import com.samsung.android.server.wifi.SemWifiFrameworkUxUtils;
import com.samsung.android.server.wifi.SemWifiHiddenNetworkTracker;
import com.samsung.android.server.wifi.UnstableApController;
import com.samsung.android.server.wifi.WifiDelayDisconnect;
import com.samsung.android.server.wifi.WifiMobileDeviceManager; //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import com.samsung.android.server.wifi.WifiRecommendNetworkDynamicScore; //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
import com.samsung.android.server.wifi.WifiRecommendNetworkManager; //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
import com.samsung.android.server.wifi.WifiRecommendNetworkLevelController; //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
import com.samsung.android.server.wifi.WlanTestHelper;
import com.samsung.android.server.wifi.mwips.MobileWIPS; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
import com.samsung.android.server.wifi.mwips.MWIPSDef; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
import com.samsung.android.knox.custom.CustomDeviceManagerProxy;

//[@MNO ConfigImplicitBroadcasts CSC feature, send explicit broadcast to MVS (Buganizer: 64022399)
import com.sec.android.app.CscFeatureTagCOMMON;
//]
import com.sec.android.app.CscFeatureTagCommon;
import com.sec.android.app.CscFeatureTagWifi;
import com.sec.android.app.SecProductFeature_COMMON;
import com.sec.android.app.SecProductFeature_KNOX; //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
import com.sec.android.app.SecProductFeature_WLAN;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream; //CSC_SUPPORT_5G_ANT_SHARE
import java.io.DataOutputStream; //CSC_SUPPORT_5G_ANT_SHARE
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//++SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
import java.net.SocketException;
import android.net.LinkAddress;
import com.samsung.android.server.wifi.ArpPeer;
//--SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS

/**
 * Track the state of Wifi connectivity. All event handling is done here,
 * and all changes in connectivity state are initiated here.
 *
 * Wi-Fi now supports three modes of operation: Client, SoftAp and p2p
 * In the current implementation, we support concurrent wifi p2p and wifi operation.
 * The WifiStateMachine handles Client operations while WifiP2pService
 * handles p2p operation.
 *
 * @hide
 */
public class WifiStateMachine extends StateMachine {

    private static final String NETWORKTYPE = "WIFI";
    private static final String NETWORKTYPE_UNTRUSTED = "WIFI_UT";
    @VisibleForTesting public static final short NUM_LOG_RECS_NORMAL = 1000;
    @VisibleForTesting public static final short NUM_LOG_RECS_VERBOSE_LOW_MEMORY = 200;
    @VisibleForTesting public static final short NUM_LOG_RECS_VERBOSE = 3000;
    private static final String TAG = "WifiStateMachine";
    private static boolean DBGMHS = ("eng".equals(android.os.Build.TYPE) || (android.os.Debug.semIsProductDev()));
    private static boolean DBG;
    private static final boolean DBG_PRODUCT_DEV = android.os.Debug.semIsProductDev();
    private static final boolean DBG_LOCATION_INFO = true;

    private static final int ONE_HOUR_MILLI = 1000 * 60 * 60;

    private static final String GOOGLE_OUI = "DA-A1-19";

    private static final String EXTRA_OSU_ICON_QUERY_BSSID = "BSSID";
    private static final String EXTRA_OSU_ICON_QUERY_FILENAME = "FILENAME";
    private static final String EXTRA_OSU_PROVIDER = "OsuProvider";

    private static final String CHARSET_CN = "gbk"; //TAG_CSCFEATURE_WIFI_CONFIGENCODINGCHARSET
    private static final String CHARSET_KOR = "ksc5601"; //TAG_CSCFEATURE_WIFI_CONFIGENCODINGCHARSET
    private static final String CONFIG_CHARSET = OpBrandingLoader.getInstance().getSupportCharacterSet(); //TAG_CSCFEATURE_WIFI_CONFIGENCODINGCHARSET
    private static final String CSC_CONFIG_EAP_AUTHMSG_POLICY = OpBrandingLoader.getInstance().getEapAuthMessagePolicy(); //TAG_CSCFEATURE_WIFI_CONFIGAUTHMSGDISPLAYPOLICY
    private static final boolean CSC_WIFI_SUPPORT_VZW_EAP_AKA = OpBrandingLoader.getInstance().isSupportEapAka(); //TAG_CSCFEATURE_WIFI_SUPPORTEAPAKA
    private static final boolean CSC_ENABLE_MENU_CONNECTION_TYPE = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_ENABLEMENUCONNECTIONTYPE); //CscFeature_Wifi_EnableMenuConnectionType
    private static final boolean CSC_WIFI_ERRORCODE = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE); //TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE
    private static final boolean CSC_SUPPORT_5G_ANT_SHARE = SemCscFeature.getInstance().getBoolean(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORT5GANTSHARE); //TAG_CSCFEATURE_WIFI_SUPPORT5GANTSHARE
    //It is a feature for Tencent Secure WiFi supporting in china market.
    //If "CSCFEATURE_WIFI_CONFIGSECURESVCINTEGRATION" feature is supported, MWIPS feature should not be supported
    private static final String CONFIG_SECURE_SVC_INTEGRATION = SemCscFeature.getInstance().getString(
            CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGSECURESVCINTEGRATION); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
    private static final boolean ENBLE_WLAN_CONFIG_ANALYTICS =
            Integer.parseInt(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS) == 1;
    private static final boolean SUPPORT_WPA3_SAE = !"0".equals(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_CONFIG_WPA3_SAE);

    private boolean mVerboseLoggingEnabled = false;

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

    //+SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
    private static final int SCORE_QUALITY_CHECK_STATE_NONE = 0;
    private static final int SCORE_QUALITY_CHECK_STATE_VALID_CHECK = 1;
    private static final int SCORE_QUALITY_CHECK_STATE_POOR_MONITOR = 2;
    private static final int SCORE_QUALITY_CHECK_STATE_POOR_CHECK = 3;

    private static final int SCORE_TXBAD_RATIO_THRESHOLD = 15;
    private int[] mPreviousScore = new int[3];
    private int mPrevoiusScoreAverage = 0;
    private long mPreviousTxBad = 0;
    private long mPreviousTxSuccess = 0;
    private long mPreviousTxBadTxGoodRatio = 0;
    private int mLastGoodScore = 1000;
    private int mLastPoorScore = 100;
    private int mScoreQualityCheckMode = SCORE_QUALITY_CHECK_STATE_NONE;
    private boolean mPoorCheckInProgress = false;
    private int mGoodScoreCount = 0;
    private int mGoodScoreTotal = 0;
    //-SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK

    //+SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
    private static final int ELE_MINIMUM_RSSI = -70;
    private static final int ELE_PREVIOUS_CHECK_CNT = 6;
    private static final int ELE_EXPIRE_COUNT = 180;
    
    private static final int ELE_POLLING_DEFAULT = 0;
    private static final int ELE_POLLING_ONE_TERM_SKIP = 1;
    
    private int mElePollingMode = ELE_POLLING_DEFAULT;

    private static final int ELE_CHECK_BEACON_NONE = 0;
    private static final int ELE_CHECK_BEACON_SUDDEN_DROP = 1;
    private static final int ELE_CHECK_BEACON_MISS = 2;
    
    private int mEleBcnCheckingState = ELE_CHECK_BEACON_NONE;
    private int mEleBcnCheckingPrevState = ELE_CHECK_BEACON_NONE;
    
    private int[] mElePrevMobileRssi = new int[ELE_PREVIOUS_CHECK_CNT];
    private int[] mElePrevWifiRssi = new int[ELE_PREVIOUS_CHECK_CNT];
    private int[] mElePrevBcnDiff = new int[ELE_PREVIOUS_CHECK_CNT];
    private boolean[] mElePrevGeoMagneticChanges = new boolean[ELE_PREVIOUS_CHECK_CNT];
    private boolean[] mElePrevStepState = new boolean[ELE_PREVIOUS_CHECK_CNT];

    private int mElePrevBcnDropCond = 0;
    private int mEleBcnDropExpireCnt = 0;
    
    private int mElePrevBcnCnt = -1;
    private int mEleBcnHistoryCnt = 0;
    private int mEleBcnMissExpireCnt = 0;

    private int mEleStableCount = 3;
    private int mEleExpireCount = ELE_EXPIRE_COUNT;

    private boolean mEleEnableMobileRssiPolling = false;
    
    private boolean mEleBlockRoamConnection = false;
    private Timer mEleBlockRoamTimer = null;
    
    private boolean mEleIsScanRunning = false;
    private boolean mEleDetectionPending = false;
  
    private boolean mEleIsStepPending = false;
  
    private boolean mElePollingSkip = false;
    private boolean mGeomagneticEleState = false;
    private boolean mPrevGeomagneticEleState = false;
    
    private int mEleInvalidByLegacyDelayCheckCnt = 0;
    private int mEleRoamingStationaryCnt = 0;
    
    private boolean mBlockUntilNewAssoc = false;
    //-SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK

    private Timer mFwLogTimer = null; //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
    private final WifiPermissionsWrapper mWifiPermissionsWrapper;

    //++SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL
    private boolean mQosGameIsRunning = false;
    private int mPersistQosTid = 0;
    private int mPersistQosUid = 0;
    //--SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL

    /* debug flag, indicating if handling of ASSOCIATION_REJECT ended up blacklisting
     * the corresponding BSSID.
     */
    private boolean didBlackListBSSID = false;

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

    //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
    private boolean mIsWifiOffByAirplane = false;
    //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

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

    private static double INVALID_LATITUDE_LONGITUDE = 1000L;//SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION

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
    private WifiMetrics mWifiMetrics;
    private WifiInjector mWifiInjector;
    private WifiMonitor mWifiMonitor;
    private WifiNative mWifiNative;
    private WifiPermissionsUtil mWifiPermissionsUtil;
    private WifiConfigManager mWifiConfigManager;
    private WifiConnectivityManager mWifiConnectivityManager;
    private WifiRecommendNetworkLevelController mWifiRecommendNetworkLevelController; //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
    private WifiRecommendNetworkManager mWifiRecommendNetworkManager; //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
    private ConnectivityManager mCm;
    private BaseWifiDiagnostics mWifiDiagnostics;
    private ScanRequestProxy mScanRequestProxy;
    private final boolean mP2pSupported;
    private final AtomicBoolean mP2pConnected = new AtomicBoolean(false);
    private boolean mIsP2pConnected;
    private int mCurrentP2pFreq;
    private boolean mTemporarilyDisconnectWifi = false;
    private final Clock mClock;
    private final PropertyService mPropertyService;
    private final BuildProperties mBuildProperties;
    private final WifiCountryCode mCountryCode;
    // Object holding most recent wifi score report and bad Linkspeed count
    private final WifiScoreReport mWifiScoreReport;
    private final SarManager mSarManager;
    private final SemSarManager mSemSarManager; //SEMSAR

    public WifiScoreReport getWifiScoreReport() {
        return mWifiScoreReport;
    }
    private final PasspointManager mPasspointManager;

    private final McastLockManagerFilterController mMcastLockManagerFilterController;

    private boolean mScreenOn = false;
    private P2pDisableListener mP2pDisableListener = null;
    private String mInterfaceName;

    private boolean mImsRssiPollingEnabled = false; //CscFeature_Wifi_SupportRssiPollStateDuringWifiCalling
    private int mLastSignalLevel = -1;
    private String mLastBssid;
    private int mLastNetworkId; // The network Id we successfully joined
    private int mLastConnectedNetworkId;

    private boolean mIpReachabilityDisconnectEnabled = false; //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX

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
                byte minRssi = mRssiRanges[i-1];
                // This value of hw has to be believed as this value is averaged and has breached
                // the rssi thresholds and raised event to host. This would be eggregious if this
                // value is invalid
                mWifiInfo.setRssi(curRssi);
                updateCapabilities();
                int ret = startRssiMonitoringOffload(maxRssi, minRssi, rssiHandler);
                Log.d(TAG, "Re-program RSSI thresholds for " + smToString(reason) +
                        ": [" + minRssi + ", " + maxRssi + "], curRssi=" + curRssi + " ret=" + ret);
                break;
            }
        }
    }

    // Testing various network disconnect cases by sending lots of spurious
    // disconnect to supplicant
    private boolean testNetworkDisconnect = false;

    private boolean mEnableRssiPolling = false;
    // Accessed via Binder thread ({get,set}PollRssiIntervalMsecs), and WifiStateMachine thread.
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
     * Interval in milliseconds between polling for RSSI
     * and linkspeed information
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

    /* Tracks sequence number on a periodic scan message */
    private int mPeriodicScanToken = 0;

    // Wakelock held during wifi start/stop and driver load/unload
    private PowerManager.WakeLock mWakeLock;

    // >>>WCM>>>
    private AsyncChannel mWcmChannel = null;

    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
    private AsyncChannel mIWCMonitorChannel = null;
    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION

    private int mLastManualConnectedNetId = WifiConfiguration.INVALID_NETWORK_ID;
    private int mLastWpsNetId = WifiConfiguration.INVALID_NETWORK_ID;

    public HashMap<Integer, DisabledCaptivePortal> mDisabledCaptivePortalList = new HashMap<Integer, DisabledCaptivePortal>();
    private int mLastDisabledCaptivePortalNetId = WifiConfiguration.INVALID_NETWORK_ID;
    private static final String DISABLED_NOTIFICATION_ID = "CaptivePortal.Disabled.Notification";
    private static final int ENABLE_DISABLED_CAPTIVE_PORTAL_MS = 10 * 60 * 1000;

    private boolean mIsFmcNetwork = false; // SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FMC

    private static int RECOVERABLE_RSSI_DIFF = 10;
    private static int RECOVERABLE_RSSI_COUNT = 2;
    private static int RECOVERABLE_RSSI_FOR_30SEC = -65;
    private static int RECOVERABLE_RSSI_FOR_60SEC = -75;
    private static int RECOVERABLE_RSSI_MAXTIME = 10*60*1000; // 10mins.

    private static final int ENABLE_DISABLED_NO_INTERNET_MS = 15 * 60 * 1000;
    private static final int ENABLE_DISABLED_POOR_NETWORK_MS = 5 * 60 * 1000;

    /** @hide */
    public int mIsReconn = 0; //SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
    public boolean mNeedRoamingInValid = false; //SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
    public int mInitialQcExtraInfo = -1;
    private int mWcmNoInternetReason = 0; // Samsung Analytic Disconnect Reason

    // <<<WCM<<<

    // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;
    static final int SECURITY_SAE = 4;

    private Context mContext;

    //+SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM (3.1)
    private WifiPolicy mWifiPolicy;
    //-SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM (3.1)

    private final Object mDhcpResultsLock = new Object();
    private DhcpResults mDhcpResults;

    // NOTE: Do not return to clients - see syncRequestConnectionInfo()
    private final ExtendedWifiInfo mWifiInfo;
    private NetworkInfo mNetworkInfo;
    private final NetworkCapabilities mDfltNetworkCapabilities;
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
    // This one is used to track whta is the current target network ID. This is used for error
    // handling during connection setup since many error message from supplicant does not report
    // SSID Once connected, it will be set to invalid
    private int mTargetNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
    private long mLastDriverRoamAttempt = 0;
    private WifiConfiguration targetWificonfiguration = null;

    private boolean mIsShutdown = false; //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
    private WifiDelayDisconnect mDelayDisconnect; //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
    private UnstableApController mUnstableApController; //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
    private final static Vendor mOpBranding = OpBrandingLoader.getInstance().getOpBranding();
    private SemWifiHiddenNetworkTracker mSemWifiHiddenNetworkTracker; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
    private SemLocationManager mSemLocationManager;
    PendingIntent mLocationPendingIntent;
    private int mLocationRequestNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
    private static final int ACTIVE_REQUEST_LOCATION = 1;
    private String mTipsVersionForDavinci = "1.3";
    private static final String WIFI_TIPS_VERSION = SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_CONFIG_TIPS_VERSION;
    private static final String ACTION_AP_LOCATION_PASSIVE_REQUEST = "com.android.server.wifi.AP_LOCATION_PASSIVE_REQUEST";
    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION

    int getPollRssiIntervalMsecs() {
        return mPollRssiIntervalMsecs;
    }

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

    private IpClient mIpClient;

    // Channel for sending replies.
    private AsyncChannel mReplyChannel = new AsyncChannel();

    // Used to initiate a connection with WifiP2pService
    private AsyncChannel mWifiP2pChannel;

    @GuardedBy("mWifiReqCountLock")
    private int mConnectionReqCount = 0;
    private WifiNetworkFactory mNetworkFactory;
    @GuardedBy("mWifiReqCountLock")
    private int mUntrustedReqCount = 0;
    private UntrustedWifiNetworkFactory mUntrustedNetworkFactory;
    private WifiNetworkAgent mNetworkAgent;
    private final Object mWifiReqCountLock = new Object();

    private byte[] mRssiRanges;

    // Used to filter out requests we couldn't possibly satisfy.
    private final NetworkCapabilities mNetworkCapabilitiesFilter = new NetworkCapabilities();

    // Provide packet filter capabilities to ConnectivityService.
    private final NetworkMisc mNetworkMisc = new NetworkMisc();

    /* The base for wifi message types */
    static final int BASE = Protocol.BASE_WIFI;
    /* Indicates Static IP succeeded */
    static final int CMD_STATIC_IP_SUCCESS                              = BASE + 15;
    /* Indicates Static IP failed */
    static final int CMD_STATIC_IP_FAILURE                              = BASE + 16;

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
    /* Enable suspend mode optimizations in the driver */
    static final int CMD_SET_SUSPEND_OPT_ENABLED                        = BASE + 86;
    /* Test network Disconnection NETWORK_DISCONNECT */
    static final int CMD_TEST_NETWORK_DISCONNECT                        = BASE + 89;

    private int testNetworkDisconnectCounter = 0;

    /* Enable TDLS on a specific MAC address */
    static final int CMD_ENABLE_TDLS                                    = BASE + 92;

    /**
     * Watchdog for protecting against b/16823537
     * Leave time for 4-way handshake to succeed
     */
    static final int ROAM_GUARD_TIMER_MSEC = 15000;

    int roamWatchdogCount = 0;
    /* Roam state watchdog */
    static final int CMD_ROAM_WATCHDOG_TIMER                            = BASE + 94;
    /* Screen change intent handling */
    static final int CMD_SCREEN_STATE_CHANGED                           = BASE + 95;

    /* Disconnecting state watchdog */
    static final int CMD_DISCONNECTING_WATCHDOG_TIMER                   = BASE + 96;

    /* Remove a packages associated configrations */
    static final int CMD_REMOVE_APP_CONFIGURATIONS                      = BASE + 97;

    /* Disable an ephemeral network */
    static final int CMD_DISABLE_EPHEMERAL_NETWORK                      = BASE + 98;

    /* Get matching network */
    static final int CMD_GET_MATCHING_CONFIG                            = BASE + 99;

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

    /* Commands from/to the SupplicantStateTracker */
    /* Reset the supplicant state tracker */
    static final int CMD_RESET_SUPPLICANT_STATE                         = BASE + 111;

    int disconnectingWatchdogCount = 0;
    static final int DISCONNECTING_GUARD_TIMER_MSEC = 5000;

    /* Disable p2p watchdog */
    static final int CMD_DISABLE_P2P_WATCHDOG_TIMER                   = BASE + 112;

    int mDisableP2pWatchdogCount = 0;
    static final int DISABLE_P2P_GUARD_TIMER_MSEC = 2000;

    /* P2p commands */
    /* We are ok with no response here since we wont do much with it anyway */
    public static final int CMD_ENABLE_P2P                              = BASE + 131;
    /* In order to shut down supplicant cleanly, we wait till p2p has
     * been disabled */
    public static final int CMD_DISABLE_P2P_REQ                         = BASE + 132;
    public static final int CMD_DISABLE_P2P_RSP                         = BASE + 133;

    /**
     * Indicates the end of boot process, should be used to trigger load from config store,
     * initiate connection attempt, etc.
     * */
    static final int CMD_BOOT_COMPLETED                                 = BASE + 134;
    /**
     * Initialize the WifiStateMachine. This is currently used to initialize the
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

    /* Reload all networks and reconnect */
    static final int CMD_RELOAD_TLS_AND_RECONNECT                       = BASE + 142;

    static final int CMD_START_CONNECT                                  = BASE + 143;

    private static final int NETWORK_STATUS_UNWANTED_DISCONNECT         = 0;
    private static final int NETWORK_STATUS_UNWANTED_VALIDATION_FAILED  = 1;
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN   = 2;
    private static final int NETWORK_STATUS_UNWANTED_DISABLE_USER_SELECTION   = 3;

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


    /* Get all matching Passpoint configurations */
    static final int CMD_GET_ALL_MATCHING_CONFIGS                       = BASE + 168;

    /**
     * Used to handle messages bounced between WifiStateMachine and IpClient.
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

    /* Read the APF program & data buffer */
    public static final int CMD_SEND_DHCP_RELEASE                       = BASE + 211;

    public static final int CMD_CHECK_DHCP_AFTER_ROAMING                = BASE + 212; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
    public static final int CMD_CHECK_DUPLICATED_IP                     = BASE + 213; //DUPLICATED_IP_USING_DETECTION

    private static final int CMD_REPLACE_PUBLIC_DNS                     = BASE + 214; //CscFeature_Wifi_SupportNetworkDiagnostics

    // >>>WCM>>>
    static final int CMD_NETWORK_STATUS_VALID                           = BASE + 220; // smar network switch, send network valid
    static final int CMD_SCAN_RESULT_AVAILABLE_WSM                      = BASE + 221;
    static final int CMD_DISABLED_CAPTIVE_PORTAL_SCAN_OUT               = BASE + 230; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ADVANCED_CAPTIVE_PORTAL
    static final int CMD_ENABLE_CAPTIVE_PORTAL                          = BASE + 231; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ADVANCED_CAPTIVE_PORTAL
    static final int CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON      = BASE + 232; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ADVANCED_CAPTIVE_PORTAL
    static final int CMD_CAPTIVE_PORTAL_NETWORK_STATUS_CHANGED          = BASE + 233; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ADVANCED_CAPTIVE_PORTAL

    static final int CMD_NETWORK_PROPERTIES_UPDATED                     = BASE + 234; // Smart network switch
    static final int CMD_DHCP_START_COMPLETE                            = BASE + 235; // Smart network switch
    static final int CMD_SCAN_REQUESTED                                 = BASE + 236; // Smart network switch
    static final int CMD_ROAM_START_COMPLETE                            = BASE + 237; // Smart network switch
    static final int CMD_PROVISIONING_FAIL                              = BASE + 238; // Smart network switch //SEC_PRODUCT_FEATURE_VALIDATION_CHECK_AFTER_PROVISIONING_FAIL
    static final int CMD_TRANSIT_TO_INVALID                             = BASE + 239; // Smart network switch, Roaming in Invalid
    static final int CMD_INITIAL_CONNECTION_TIMEOUT                     = BASE + 240;
    static final int CMD_SEC_IP_CONFIGURATION_LOST                      = BASE + 241; // Smart network switch //SEC_PRODUCT_FEATURE_VALIDATION_CHECK_AFTER_PROVISIONING_FAIL
    static final int CMD_REACHABILITY_LOST                              = BASE + 242; // Smart network switch //SEC_PRODUCT_FEATURE_VALIDATION_CHECK_AFTER_PROVISIONING_FAIL
    // <<<WCM<<<

    private static final int CMD_IMS_CALL_ESTABLISHED                   = BASE + 243; //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
    private static final int CMD_AUTO_CONNECT_CARRIER_AP_ENABLED        = BASE + 244; //SEC_PRODUCT_FEATURE_WLAN_AUTO_CONNECT_CARRIER_AP

    /* Signals that IClientInterface instance underpinning our state is dead. */
    private static final int CMD_CLIENT_INTERFACE_BINDER_DEATH          = BASE + 250;

    /* Signals that the Vendor HAL instance underpinning our state is dead. */
    private static final int CMD_VENDOR_HAL_HWBINDER_DEATH              = BASE + 251;

    /* Indicates that diagnostics should time out a connection start event. */
    private static final int CMD_DIAGS_CONNECT_TIMEOUT                  = BASE + 252;

    // Start subscription provisioning with a given provider
    private static final int CMD_START_SUBSCRIPTION_PROVISIONING        = BASE + 254;

    private static final int CMD_UPDATE_CONFIG_LOCATION                 = BASE + 255; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION

    static final int CMD_THREE_TIMES_SCAN_IN_IDLE                       = BASE + 309; // SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE

    //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
    static final int CMD_SET_ADPS_MODE                                  = BASE + 311;

    //SEC_PRODUCT_FEATURE_WLAN_GET_SPECIFIC_NETWORK
    static final int CMD_GET_SPECIFIC_CONFIGURED_NETWORKS               = BASE + 322;

    //+SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS
    /* Forcingly enable all networks after turning on */
    static final int CMD_FORCINGLY_ENABLE_ALL_NETWORKS                  = BASE + 330;
    //-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS

    /* SEC API */
    private static final int CMD_SEC_API_ASYNC                          = BASE + 501;
    private static final int CMD_SEC_API                                = BASE + 502;
    private static final int CMD_SEC_STRING_API                         = BASE + 503;
    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    public static final int CMD_SEC_LOGGING                             = BASE + 504;
    private static final int CMD_24HOURS_PASSED_AFTER_BOOT              = BASE + 507;
    private static final int CMD_REQUEST_FW_BIGDATA_PARAM               = BASE + 508;
    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    public static final int CMD_SCAN_RESULT_AVAILABLE                  = BASE + 512;

    private static final int CMD_CHECK_DEFAULT_GWMACADDRESS             = BASE + 521; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS

    public static final int CMD_P2P_FACTORY_RESET                       = BASE + 530; //SAMSUNG_P2P

    // For message logging.
    private static final Class[] sMessageClasses = {
            AsyncChannel.class, WifiStateMachine.class, DhcpClient.class };
    private static final SparseArray<String> sSmToString =
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
     * {@link NetworkAgent#explicitlySelected(boolean)}
     * after connecting to the network which the user last selected.
     */
    @VisibleForTesting
    public static final int LAST_SELECTED_NETWORK_EXPIRATION_AGE_MILLIS = 30 * 1000;

    /* Tracks if user has enabled suspend optimizations through settings */
    private AtomicBoolean mUserWantsSuspendOpt = new AtomicBoolean(true);

    /* Tracks if user has enabled Connected Mac Randomization through settings */
    private AtomicBoolean mEnableConnectedMacRandomization = new AtomicBoolean(false);

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
    ActivityManager mActivityManager;
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

    /**
     * If semConcurrentEnabled is true, do belows
     * 1) stop all scan
     * 2) stop all anqp
     * 3) TODO: cancle or stop wps
     */
    private boolean mConcurrentEnabled; //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
    private boolean mIsImsCallEstablished; //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER

    private boolean mIsBootCompleted;
    private int mConnectedApInternalType = 0;
    private WifiBigDataLogManager mBigDataManager; // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
    private SemWifiIssueDetector mIssueDetector; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
    private WifiSettingsStore mSettingsStore; //SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

    private boolean mIsHs20Enabled; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20

    /* SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
     * Tracks if Wi-Fi Power Saving is enabled or not
     */
    private AtomicBoolean mWifiAdpsEnabled = new AtomicBoolean(false);
    private static final boolean ENABLE_SUPPORT_ADPS = SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS");

//++SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
    private String mLastArpResultsForRoamingDhcp = null;
    private boolean mNoArpResponseAfterRoaming = false;
    private boolean semStartNudProbe = false;
    private boolean semLostProvisioningAfterRoaming = false;
//++SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
    private int semmDhcpRenewAfterRoamingMode = 0; // 0:default, 1:ForceRestart, 2:skip, SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY

    //+SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
    /* EAP event message */
    private static final String WPA_EVENT_EAP_TLS_CERT_ERROR = "CTRL-EVENT-EAP-TLS-CERT-ERROR "; //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
    private static final String WPA_EVENT_EAP_SUCCESS = "CTRL-EVENT-EAP-SUCCESS "; // For save SimNumber on WifiEnterpriseConfig
    private static final String WPA_EVENT_EAP_TLS_ALERT = "CTRL-EVENT-EAP-TLS-ALERT "; // CC/MDF: In scope of FAU_GEN.1/WLAN
    private static final String WPA_EVENT_EAP_TLS_HANDSHAKE_FAIL = "CTRL-EVENT-EAP-TLS-HANDSHAKE-FAIL "; // CC/MDF: In scope of FAU_GEN.1/WLAN
    private static final String WPA_EVENT_EAP_LOGGING = "CTRL-EVENT-EAP-LOGGING ";
    private static final String WPA_EVENT_EAP_FAILURE = "CTRL-EVENT-EAP-FAILURE "; // TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE
    private static final String WPA_EVENT_EAP_NOTIFICATION = "CTRL-EVENT-EAP-NOTIFICATION "; // TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE
    private static final String WPA_EVENT_EAP_ANONYMOUS_IDENTITY_UPDATED = "CTRL-EVENT-EAP-ANONYMOUS-IDENTITY-UPDATED ";
    private static final String WPA_EVENT_EAP_DEAUTH_8021X_AUTH_FAILED = "CTRL-EVENT-DEAUTH-8021X-AUTH-FAILED ";
    private static final String WPA_EVENT_EAP_KT_NOTIFICATION = "CTRL-EVENT-EAP-KT-NOTIFICATION ";
    private static final String WPA_EVENT_EAP_NO_CREDENTIALS = "CTRL-EVENT-EAP-NO-CREDENTIALS ";
    private static final String WPA_EVENT_EAP_ERROR_MESSAGE = "CTRL-EVENT-EAP-ERROR-MESSAGE ";

    /* KT NOTIFICATION CODE */
    private static final int KT_CODE_EAP_AUTH_FAIL = 5;
    private static final String INTENT_KT_EAP_NOTIFICATION = "com.kt.wifiapi.OEMExtension.NOTIFICATION";

    //EAP_LOG
    private static boolean mEaptLoggingControllConnecting = true;
    private static boolean mEaptLoggingControllAuthFailure = true;
    private static int EAP_LOGGING_STATE_CONNECTED = 1;
    private static int EAP_LOGGING_STATE_AUTH_FAILURE = 2;
    private static int EAP_LOGGING_STATE_ASSOC_REJECT = 3;
    private static int EAP_LOGGING_STATE_DHCP_FAILURE = 4;
    private static int EAP_LOGGING_STATE_NOTIFICATION = 5;
    //-SEC_PRODUCT_FEATURE_WLAN_EAP_XXX

    public WifiStateMachine(Context context, FrameworkFacade facade, Looper looper,
                            UserManager userManager, WifiInjector wifiInjector,
                            BackupManagerProxy backupManagerProxy, WifiCountryCode countryCode,
                            WifiNative wifiNative,
                            WrongPasswordNotifier wrongPasswordNotifier,
                            SarManager sarManager) {
        super("WifiStateMachine", looper);
        mWifiInjector = wifiInjector;
        mWifiMetrics = mWifiInjector.getWifiMetrics();
        mClock = wifiInjector.getClock();
        mPropertyService = wifiInjector.getPropertyService();
        mBuildProperties = wifiInjector.getBuildProperties();
        mContext = context;
        mFacade = facade;
        mWifiNative = wifiNative;
        mBackupManagerProxy = backupManagerProxy;
        mWrongPasswordNotifier = wrongPasswordNotifier;
        mSarManager = sarManager;

        mSemSarManager = new SemSarManager(mContext, mWifiNative); //SEMSAR

        // TODO refactor WifiNative use of context out into it's own class
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
        mScanRequestProxy = mWifiInjector.getScanRequestProxy();
        mWifiPermissionsWrapper = mWifiInjector.getWifiPermissionsWrapper();

        mWifiGeofenceManager = mWifiInjector.getWifiGeofenceManager(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE

        mWifiInfo = new ExtendedWifiInfo();
        mSupplicantStateTracker =
                mFacade.makeSupplicantStateTracker(context, mWifiConfigManager, getHandler());

        mLinkProperties = new LinkProperties();
        mMcastLockManagerFilterController = new McastLockManagerFilterController();

        mNetworkInfo.setIsAvailable(false);
        mLastBssid = null;
        mLastNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
        mLastSignalLevel = -1;

        mCountryCode = countryCode;

        mWifiScoreReport = new WifiScoreReport(mWifiInjector.getScoringParams(), mClock);

        //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
        mWifiAdpsEnabled.set(Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.WIFI_ADPS, 0) == 1);

        mDelayDisconnect = new WifiDelayDisconnect(mContext, mWifiInjector); //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION

        mNetworkCapabilitiesFilter.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED);
        mNetworkCapabilitiesFilter.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
        mNetworkCapabilitiesFilter.setLinkUpstreamBandwidthKbps(1024 * 1024);
        mNetworkCapabilitiesFilter.setLinkDownstreamBandwidthKbps(1024 * 1024);
        // TODO - needs to be a bit more dynamic
        mDfltNetworkCapabilities = new NetworkCapabilities(mNetworkCapabilitiesFilter);
        //+MHS
        IntentFilter sfilter = new IntentFilter();
        sfilter.addAction("softap_manager_bigdata"); //MHS
        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (DBG) Log.d(TAG, "Action : " + action);
                        if (action.equals("softap_manager_bigdata")) {
                            String mhsData= (String)intent.getExtra("mhsdata");
                            Bundle args = new Bundle();
                            args.putBoolean("bigdata", true);
                            args.putString("feature", "MHSS");
                            args.putString("data", mhsData);
                              sendMessage(obtainMessage(CMD_SEC_LOGGING, 0, 0, args));
                        }
                    }
                }, sfilter);
        //-MHS
        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(PowerManager.ACTION_SCREEN_ON_BY_PROXIMITY);
        filter.addAction(PowerManager.ACTION_SCREEN_OFF_BY_PROXIMITY);
        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (DBG) Log.d(TAG, "Screen Change Action : " + action);
                        if (action.equals(Intent.ACTION_SCREEN_ON)) {
                            sendMessage(CMD_SCREEN_STATE_CHANGED, 1);
                        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                            sendMessage(CMD_SCREEN_STATE_CHANGED, 0);
                        } else if (action.equals(PowerManager.ACTION_SCREEN_ON_BY_PROXIMITY)) {
                            sendMessage(CMD_SCREEN_STATE_CHANGED, 1);
                        } else if (action.equals(PowerManager.ACTION_SCREEN_OFF_BY_PROXIMITY)) {
                            sendMessage(CMD_SCREEN_STATE_CHANGED, 0);
                        }
                    }
                }, filter);
        //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON

        //+SEC_PRODUCT_FEATURE_WLAN_ISSUETRACKER_CONTROL
        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Bundle extras;
                        extras = intent.getExtras();
                        if (extras != null && extras.getSerializable("ONOFF") != null) {
                            mIssueTrackerOn = (Boolean)extras.getSerializable("ONOFF");
                        }
                    }
                },
        new IntentFilter("com.sec.android.ISSUE_TRACKER_ONOFF"));
        //-SEC_PRODUCT_FEATURE_WLAN_ISSUETRACKER_CONTROL

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

        mFacade.registerContentObserver(mContext, Settings.Global.getUriFor(
                        Settings.Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED), false,
                new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        mUserWantsSuspendOpt.set(mFacade.getIntegerSetting(mContext,
                                Settings.Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED, 1) == 1);
                    }
                });

        mFacade.registerContentObserver(mContext, Settings.Global.getUriFor(
                        Settings.Global.WIFI_CONNECTED_MAC_RANDOMIZATION_ENABLED), false,
                new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        updateConnectedMacRandomizationSetting();
                    }
                });

        mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(
                        Settings.Secure.WIFI_HOTSPOT20_ENABLE), false, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                new ContentObserver(getHandler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        int passpointEnabled = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE, 1);

                        if (passpointEnabled == 1 || passpointEnabled == 3) {
                            mIsHs20Enabled = true;
                        } else {
                            mIsHs20Enabled = false;
                        }

                        mPasspointManager.setHotspot20State(mIsHs20Enabled);
                        mWifiNative.setInterwokingEnabled(mInterfaceName, mIsHs20Enabled);

                        if (mWifiState.get() == WifiManager.WIFI_STATE_ENABLED) {
                            updatePasspointNetworkSelectionStatus(mIsHs20Enabled);
                        } else {
                            Log.e(TAG, "WIFI_HOTSPOT20_ENABLE change to : " + mIsHs20Enabled + ", but mWifiState is invalid : "+mWifiState.get());
                        }
                    }
                });

        //+SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
        mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor(
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
                        boolean heEnabled = Settings.Global.getInt(mContext.getContentResolver(),
                            Settings.Global.SAFE_WIFI, 0) == 0;

                        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_80211AX) {
                            if (!mWifiNative.setHECapability(heEnabled)) {
                                Log.e(TAG, "Failed to set safe Wi-Fi mode");
                            }
                        }
                    }
                });

        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        sendMessage(CMD_BOOT_COMPLETED);
                        mIsBootCompleted = true;
                    }
                },
                new IntentFilter(Intent.ACTION_LOCKED_BOOT_COMPLETED));

        mUserWantsSuspendOpt.set(mFacade.getIntegerSetting(mContext,
                Settings.Global.WIFI_SUSPEND_OPTIMIZATIONS_ENABLED, 1) == 1);

        updateConnectedMacRandomizationSetting();

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getName());

        mSuspendWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiSuspend");
        mSuspendWakeLock.setReferenceCounted(false);

        IntentFilter intentFilter = new IntentFilter();

        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            mWifiPolicy = EnterpriseDeviceManager.getInstance().getWifiPolicy();
            intentFilter.addAction(ACTION_ENABLE_NETWORK_INTERNAL);
        }

        if (CSC_SUPPORT_5G_ANT_SHARE) {
            intentFilter.addAction("android.intent.action.coexstatus");
        }

        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(WifiPolicy.ACTION_ENABLE_NETWORK_INTERNAL)
                        && SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
                    int netId = intent.getIntExtra(EXTRA_NETID_INTERNAL,
                            WifiConfiguration.INVALID_NETWORK_ID);
                    if (netId != WifiConfiguration.INVALID_NETWORK_ID) {
                        boolean enableOthers = intent
                                .getBooleanExtra(EXTRA_ENABLE_OTHERS_INTERNAL, false);
                        mWifiConfigManager.enableNetwork(netId, enableOthers,
                                Process.SYSTEM_UID);
                    } else {
                        Log.w(TAG,
                                "BroadcastReceiver - WifiPolicy.ACTION_ENABLE_NETWORK_INTERNAL : netId = "
                                + netId);
                    }
                } else if (intent.getAction().equals("android.intent.action.coexstatus")) { //CSC_SUPPORT_5G_ANT_SHARE
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

            mContext.registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            boolean is5GHz = false;
                            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE, WifiManager.WIFI_AP_STATE_FAILED);
                            if (state == WifiManager.WIFI_AP_STATE_ENABLED) {
                                WifiApConfigStore mWifiApConfigStore;
                                mWifiApConfigStore = WifiInjector.getInstance().getWifiApConfigStore();
                                WifiConfiguration tempWifiConfig =  mWifiApConfigStore.getApConfiguration();
                                if (tempWifiConfig != null) {
                                    is5GHz = (tempWifiConfig.apChannel > 14);
                                    sendIpcMessageToRilForLteu(LTEU_MOBILEHOTSPOT_5GHZ_ENABLED, true, is5GHz, false);
                                }
                            } else if (state == WifiManager.WIFI_AP_STATE_DISABLED || state == WifiManager.WIFI_AP_STATE_FAILED) {
                                    sendIpcMessageToRilForLteu(LTEU_MOBILEHOTSPOT_5GHZ_ENABLED, false, false, false);
                            }
                        }
                    },
                    new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION));
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

        if (isSupportWifiTipsVersion(mTipsVersionForDavinci) && isSemLocationManagerSupported(mContext)) {
            IntentFilter locationIntentFilter = new IntentFilter();
            locationIntentFilter.addAction(ACTION_AP_LOCATION_PASSIVE_REQUEST);
            mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        Log.d(TAG,"Action : " + action);
                        if (ACTION_AP_LOCATION_PASSIVE_REQUEST.equals(action)) {
                            Bundle bundle = intent.getExtras();
                            Location location = (Location) bundle.get(SemLocationManager.CURRENT_LOCATION);
                            WifiConfiguration config = getCurrentWifiConfiguration();
                            if (config != null) {
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                mWifiGeofenceManager.setLatitudeAndLongitude(config, latitude, longitude);
                                if (mLocationPendingIntent != null) {
                                    mSemLocationManager.removePassiveLocation(mLocationPendingIntent);
                                }
                            } else {
                                if (DBG_LOCATION_INFO) Log.d(TAG,"There is no config to update location");
                            }
                        }
                    }
                }, locationIntentFilter);
        }
/*
        mTcpBufferSizes = mContext.getResources().getString(
                com.android.internal.R.string.config_wifi_tcp_buffers);
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

        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
            int passpointEnabled = 0;

            try {
                passpointEnabled = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_ENABLE);

                if (passpointEnabled == 1 || passpointEnabled == 3) {
                    mIsHs20Enabled = true;
                } else {
                    mIsHs20Enabled = false;
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
                    mIsHs20Enabled = true;
                } else {
                    mIsHs20Enabled = false;
                }

                Settings.Secure.putInt(mContext.getContentResolver(),Settings.Secure.WIFI_HOTSPOT20_ENABLE, passpointEnabled);
            }

            mPasspointManager.setHotspot20State(mIsHs20Enabled);
            mPasspointManager.setHotspot20VendorSimState(false);
        }

        //mNetworkAutoConnectEnabled default set
        if (Vendor.ATT == mOpBranding) { //TAG_CSCFEATURE_WIFI_CONFIGAUTOCONNECTHOTSPOT ATT
            mWifiConfigManager.setNetworkAutoConnect((Settings.Secure.getInt(mContext.getContentResolver(),
                    Settings.Secure.WIFI_AUTO_CONNECT, 1) == 1));
            if (DBG) logi("ATT set mNetworkAutoConnectEnabled = " + mWifiConfigManager.getNetworkAutoConnectEnabled());
        } else if (CSC_ENABLE_MENU_CONNECTION_TYPE) { //CscFeature_Wifi_EnableMenuConnectionType
            mWifiConfigManager.setNetworkAutoConnect((Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.WIFI_CONNECTION_TYPE, 0) == 0));
            if (DBG) logi("CMCC set mNetworkAutoConnectEnabled = " + mWifiConfigManager.getNetworkAutoConnectEnabled());
        }

        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        mBigDataManager = new WifiBigDataLogManager(mContext, looper,
                new WifiBigDataLogManager.WifiBigDataLogAdapter() {
                        @Override
                        public List<WifiConfiguration> getSavedNetworks() {
                            return mWifiConfigManager.getSavedNetworks();
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

        //start the state machine
        start();

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
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.SUITABLE_NETWORK_NOT_FOUND_EVENT,
                getHandler()); //SEC_PRODUCT_FEATURE_WLAN_SUITABLE_NETWORK_NOT_FOUND_EVENT
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
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.SUP_BIGDATA_EVENT,
                getHandler());  //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        mWifiMonitor.registerHandler(mInterfaceName, WifiMonitor.WIPS_EVENT,
                getHandler()); //SEC_PRODUCT_FEATURE_WLAN_CONFIG_TIPS_VERSION 1.3
    }

    /**
     * Class to implement the MulticastLockManager.FilterController callback.
     */
    class McastLockManagerFilterController implements WifiMulticastLockManager.FilterController {
        /**
         * Start filtering Multicast v4 packets
         */
        public void startFilteringMulticastPackets() {
            if (mIpClient != null) {
                mIpClient.setMulticastFilter(true);
            }
        }

        /**
         * Stop filtering Multicast v4 packets
         */
        public void stopFilteringMulticastPackets() {
            if (mIpClient != null) {
                mIpClient.setMulticastFilter(false);
            }
        }
    }

    class IpClientCallback extends IpClient.Callback {
        @Override
        public void onPreDhcpAction() {
            sendMessage(DhcpClient.CMD_PRE_DHCP_ACTION);
        }

        @Override
        public void onPostDhcpAction() {
            sendMessage(DhcpClient.CMD_POST_DHCP_ACTION);
        }

        @Override
        public void onNewDhcpResults(DhcpResults dhcpResults) {
            if (dhcpResults != null) {
                sendMessage(CMD_IPV4_PROVISIONING_SUCCESS, dhcpResults);
                if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS ) {
                    if (!checkIfForceRestartDhcp()) {
                        mIpClient.saveDhcpResult(mLastBssid,dhcpResults);
                        sendMessageDelayed(CMD_CHECK_DEFAULT_GWMACADDRESS, 1000);
                    }
                }
            } else {
                sendMessage(CMD_IPV4_PROVISIONING_FAILURE);
                mWifiInjector.getWifiLastResortWatchdog().noteConnectionFailureAndTriggerIfNeeded(
                        getTargetSsid(), mTargetRoamBSSID,
                        WifiLastResortWatchdog.FAILURE_CODE_DHCP);
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
        public void onSemLog(int reason) {
            report(ReportIdKey.ID_DHCP_FAIL, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                    ReportUtil.getReportDataForDhcpResult(reason));
            if (ENBLE_WLAN_CONFIG_ANALYTICS) { //SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                setAnalyticsDhcpDisconnectReason(reason);
            }
        }

        @Override
        public void onSemProvisioningFail(int reason) {  //SEC_PRODUCT_FEATURE_VALIDATION_CHECK_AFTER_PROVISIONING_FAIL
            sendMessage(CMD_SEC_IP_CONFIGURATION_LOST);
        }
    }

    private void stopIpClient() {
        /* Restore power save and suspend optimizations */
        handlePostDhcpSetup();
        if (mIpClient != null) {
            mIpClient.stop();
        }
    }

    PendingIntent getPrivateBroadcast(String action, int requestCode) {
        Intent intent = new Intent(action, null);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.setPackage("android");
        return mFacade.getBroadcast(mContext, requestCode, intent, 0);
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
        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        mBigDataManager.setLogVisible(DBG_PRODUCT_DEV || mVerboseLoggingEnabled);
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

    public void clearANQPCache() {
        // TODO(b/31065385)
        // mWifiConfigManager.trimANQPCache(true);
    }

    public void disableP2p(P2pDisableListener mP2pDisableCallback) {
        if (mP2pSupported) {
            mP2pDisableListener = mP2pDisableCallback;
            p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
        }
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
        if (mWifiConfigManager.getConfiguredNetwork(netId) == null) {
            loge("connectToUserSelectNetwork Invalid network Id=" + netId);
            return false;
        }
        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) {
            WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(netId);
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

    public Messenger getMessenger() {
        return new Messenger(getHandler());
    }

    private long mDisconnectedTimeStamp = 0;

    public long getDisconnectedTimeMilli() {
        if (getCurrentState() == mDisconnectedState
                && mDisconnectedTimeStamp != 0) {
            long now_ms = mClock.getWallClockMillis();
            return now_ms - mDisconnectedTimeStamp;
        }
        return 0;
    }

    // Last connect attempt is used to prevent scan requests:
    //  - for a period of 10 seconds after attempting to connect
    private long lastConnectAttemptTimestamp = 0;
    private Set<Integer> lastScanFreqs = null;

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

    private int messageHandlingStatus = 0;

    //TODO: this is used only to track connection attempts, however the link state and packet per
    //TODO: second logic should be folded into that
    private boolean checkOrDeferScanAllowed(Message msg) {
        long now = mClock.getWallClockMillis();
        if (lastConnectAttemptTimestamp != 0 && (now - lastConnectAttemptTimestamp) < 10000) {
            Message dmsg = Message.obtain(msg);
            sendMessageDelayed(dmsg, 11000 - (now - lastConnectAttemptTimestamp));
            return false;
        }
        return true;
    }

    private int mOnTime = 0;
    private int mTxTime = 0;
    private int mRxTime = 0;

    private int mOnTimeScreenStateChange = 0;
    private long lastOntimeReportTimeStamp = 0;
    private long lastScreenStateChangeTimeStamp = 0;
    private int mOnTimeLastReport = 0;
    private int mTxTimeLastReport = 0;
    private int mRxTimeLastReport = 0;

    private long lastLinkLayerStatsUpdate = 0;

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
        int period = (int) (now - lastOntimeReportTimeStamp);
        lastOntimeReportTimeStamp = now;
        sb.append(String.format("[on:%d tx:%d rx:%d period:%d]", on, tx, rx, period));
        // Report stats since Screen State Changed
        on = mOnTime - mOnTimeScreenStateChange;
        period = (int) (now - lastScreenStateChangeTimeStamp);
        sb.append(String.format(" from screen [on:%d period:%d]", on, period));
        return sb.toString();
    }

    WifiLinkLayerStats getWifiLinkLayerStats() {
        if (mInterfaceName == null) {
            loge("getWifiLinkLayerStats called without an interface");
            return null;
        }
        logd("enter getWifiLinkLayerStats"); //SEC_PRODUCT_FEATURE_WLAN_DEBUG_HIDL
        lastLinkLayerStatsUpdate = mClock.getWallClockMillis();
        WifiLinkLayerStats stats = mWifiNative.getWifiLinkLayerStats(mInterfaceName);
        if (stats != null) {
            mOnTime = stats.on_time;
            mTxTime = stats.tx_time;
            mRxTime = stats.rx_time;
            mRunningBeaconCount = stats.beacon_rx;
            mWifiInfo.updatePacketRates(stats, lastLinkLayerStatsUpdate);
        } else {
            long mTxPkts = mFacade.getTxPackets(mInterfaceName);
            long mRxPkts = mFacade.getRxPackets(mInterfaceName);
            mWifiInfo.updatePacketRates(mTxPkts, mRxPkts, lastLinkLayerStatsUpdate);
        }
        return stats;
    }

    private byte[] getDstMacForKeepalive(KeepalivePacketData packetData)
            throws KeepalivePacketData.InvalidPacketException {
        try {
            InetAddress gateway = RouteInfo.selectBestRoute(
                    mLinkProperties.getRoutes(), packetData.dstAddress).getGateway();
            String dstMacStr = macAddressFromRoute(gateway.getHostAddress());
            return NativeUtil.macAddressToByteArray(dstMacStr);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new KeepalivePacketData.InvalidPacketException(
                    ConnectivityManager.PacketKeepalive.ERROR_INVALID_IP_ADDRESS);
        }
    }

    private static int getEtherProtoForKeepalive(KeepalivePacketData packetData)
            throws KeepalivePacketData.InvalidPacketException {
        if (packetData.dstAddress instanceof Inet4Address) {
            return OsConstants.ETH_P_IP;
        } else if (packetData.dstAddress instanceof Inet6Address) {
            return OsConstants.ETH_P_IPV6;
        } else {
            throw new KeepalivePacketData.InvalidPacketException(
                    ConnectivityManager.PacketKeepalive.ERROR_INVALID_IP_ADDRESS);
        }
    }

    int startWifiIPPacketOffload(int slot, KeepalivePacketData packetData, int intervalSeconds) {
        byte[] packet = null;
        byte[] dstMac = null;
        int proto = 0;

        try {
            packet = packetData.getPacket();
            dstMac = getDstMacForKeepalive(packetData);
            proto = getEtherProtoForKeepalive(packetData);
        } catch (KeepalivePacketData.InvalidPacketException e) {
            return e.error;
        }

        int ret = mWifiNative.startSendingOffloadedPacket(
                mInterfaceName, slot, dstMac, packet, proto, intervalSeconds * 1000);
        if (ret != 0) {
            loge("startWifiIPPacketOffload(" + slot + ", " + intervalSeconds +
                    "): hardware error " + ret);
            return ConnectivityManager.PacketKeepalive.ERROR_HARDWARE_ERROR;
        } else {
            return ConnectivityManager.PacketKeepalive.SUCCESS;
        }
    }

    int stopWifiIPPacketOffload(int slot) {
        int ret = mWifiNative.stopSendingOffloadedPacket(mInterfaceName, slot);
        if (ret != 0) {
            loge("stopWifiIPPacketOffload(" + slot + "): hardware error " + ret);
            return ConnectivityManager.PacketKeepalive.ERROR_HARDWARE_ERROR;
        } else {
            return ConnectivityManager.PacketKeepalive.SUCCESS;
        }
    }

    int startRssiMonitoringOffload(byte maxRssi, byte minRssi,
            WifiNative.WifiRssiEventHandler rssiHandler) {
        return mWifiNative.startRssiMonitoring(mInterfaceName, maxRssi, minRssi, rssiHandler);
    }

    int stopRssiMonitoringOffload() {
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

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI
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
                            true, WifiStateMachine.class.getSimpleName(), AuditEvents.WIFI_ENABLING);
                    }
                    else if (newState == WIFI_STATE_DISABLING) {
                        WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_APPLICATION,
                            true, WifiStateMachine.class.getSimpleName(), AuditEvents.WIFI_DISABLING);
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

    public boolean isUnstableAp(String bssid) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
        if (mUnstableApController != null) {
            return mUnstableApController.isUnstableAp(bssid);
        }
        return false;
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
     * TODO: doc
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

    public WifiInfo getWifiInfo() {
        return mWifiInfo;
    }

    //SEC_PRODUCT_FEATURE_WLAN_SEC_CONFIGURATION_EXTENSION
    public NetworkInfo syncGetNetworkInfo() {
        return new NetworkInfo(mNetworkInfo);
    }

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
     * Allow tests to confirm the operational mode for WSM.
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
        // TODO: cancle or stop wps
    }

    boolean getConcurrentEnabled() { //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
        return mConcurrentEnabled;
    }

    public void syncSetFccChannel(boolean enable) { //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL
        Log.d(TAG, "syncSetFccChannel: enable = " + enable);
        if (mIsRunning) {
            if (mBlockFccChannelCmd) {
                Log.d(TAG, "Block setFccChannelNative CMD by WlanMacAddress");
                return;
            }
            mWifiNative.setFccChannel(mInterfaceName, enable);
        }
    }

    /**
     * Retrieve the WifiMulticastLockManager.FilterController callback for registration.
     */
    protected WifiMulticastLockManager.FilterController getMcastLockManagerFilterController() {
        return mMcastLockManagerFilterController;
    }

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

    public void disableEphemeralNetwork(String SSID) {
        if (SSID != null) {
            sendMessage(CMD_DISABLE_EPHEMERAL_NETWORK, SSID);
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

    /**
     * Disconnect from Access Point
     */
    public void disconnectCommand() {
        sendMessage(CMD_DISCONNECT, 0, DISCONNECT_REASON_UNKNOWN);
    }

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
     * Reload networks and then reconnect; helps load correct data for TLS networks
     */

    public void reloadTlsNetworksAndReconnect() {
        sendMessage(CMD_RELOAD_TLS_AND_RECONNECT);
    }

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
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_NETWORK, from, 0, config); //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
        int result = resultMsg.arg1;
        resultMsg.recycle();
        return result;
    }

    /**
     * Get specific configured networks synchronously
     * SEC_PRODUCT_FEATURE_WLAN_GET_SPECIFIC_NETWORK
     *
     * @param channel
     * @return
     */
    public WifiConfiguration syncSpecificNetwork(AsyncChannel channel, int netId) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "can't get network because shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_SPECIFIC_CONFIGURED_NETWORKS, netId, 0);
        WifiConfiguration result = null;
        if (resultMsg.obj != null && resultMsg.obj instanceof WifiConfiguration) {
            result = (WifiConfiguration) resultMsg.obj;
        }
        resultMsg.recycle();
        return result;
    }

    /**
     * Get configured networks synchronously
     *
     * @param channel
     * @return
     */

    public List<WifiConfiguration> syncGetConfiguredNetworks(int uuid, AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_CONFIGURED_NETWORKS, uuid);
        if (resultMsg == null) { // an error has occurred
            return null;
        } else {
            List<WifiConfiguration> result = (List<WifiConfiguration>) resultMsg.obj;
            resultMsg.recycle();
            return result;
        }
    }

    public boolean isCarrierNetworkSaved() { //SEC_PRODUCT_FEATURE_WLAN_USE_DEFAULT_AP
        return mWifiConfigManager.isCarrierNetworkSaved();
    }

    public List<WifiConfiguration> syncGetPrivilegedConfiguredNetwork(AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(
                CMD_GET_PRIVILEGED_CONFIGURED_NETWORKS);
        List<WifiConfiguration> result = (List<WifiConfiguration>) resultMsg.obj;
        resultMsg.recycle();
        return result;
    }

    public WifiConfiguration syncGetMatchingWifiConfig(ScanResult scanResult, AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_MATCHING_CONFIG, scanResult);
        WifiConfiguration config = (WifiConfiguration) resultMsg.obj;
        resultMsg.recycle();
        return config;
    }

    List<WifiConfiguration> getAllMatchingWifiConfigs(ScanResult scanResult,
            AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_ALL_MATCHING_CONFIGS,
                scanResult);
        List<WifiConfiguration> configs = (List<WifiConfiguration>) resultMsg.obj;
        resultMsg.recycle();
        return configs;
    }

    /**
     * Retrieve a list of {@link OsuProvider} associated with the given AP synchronously.
     *
     * @param scanResult The scan result of the AP
     * @param channel Channel for communicating with the state machine
     * @return List of {@link OsuProvider}
     */
    public List<OsuProvider> syncGetMatchingOsuProviders(ScanResult scanResult,
            AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return null;
        }
        Message resultMsg =
                channel.sendMessageSynchronously(CMD_GET_MATCHING_OSU_PROVIDERS, scanResult);
        List<OsuProvider> providers = (List<OsuProvider>) resultMsg.obj;
        resultMsg.recycle();
        return providers;
    }

    /**
     * Add or update a Passpoint configuration synchronously.
     *
     * @param channel Channel for communicating with the state machine
     * @param config The configuration to add or update
     * @return true on success
     */
    public boolean syncAddOrUpdatePasspointConfig(AsyncChannel channel,
            PasspointConfiguration config, int uid) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return false;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG,
                uid, 0, config);
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
            return null;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_PASSPOINT_CONFIGS);
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
        boolean result = resultMsg.arg1 != 0;
        resultMsg.recycle();
        return result;
    }

    /**
     * Get adaptors synchronously
     */

    public int syncGetSupportedFeatures(AsyncChannel channel) {
        if (mIsShutdown) { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
            Log.d(TAG, "ignore processing beause shutdown is held");
            return 0;
        }
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_SUPPORTED_FEATURES);
        int supportedFeatureSet = resultMsg.arg1;
        resultMsg.recycle();

        // Mask the feature set against system properties.
        boolean disableRtt = mPropertyService.getBoolean("config.disable_rtt", false);
        if (disableRtt) {
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
        logd("enter syncGetLinkLayerStats"); //SEC_PRODUCT_FEATURE_WLAN_DEBUG_HIDL
        Message resultMsg = channel.sendMessageSynchronously(CMD_GET_LINK_LAYER_STATS);
        WifiLinkLayerStats result = (WifiLinkLayerStats) resultMsg.obj;
        resultMsg.recycle();
        logd("return syncGetLinkLayerStats"); //SEC_PRODUCT_FEATURE_WLAN_DEBUG_HIDL
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
        Message resultMsg = channel.sendMessageSynchronously(CMD_REMOVE_NETWORK, networkId, from); //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
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
        boolean result = (resultMsg.arg1 != FAILURE);
        resultMsg.recycle();
        if (result && mWcmChannel != null) {
            if (DBG) Log.d(TAG, "MultiNetwork enableNetwork WWSM_MULTI_NETWORK_REQUEST" );
            int uid = Binder.getCallingUid();
            int pid = Binder.getCallingPid();
            Bundle bundle = new Bundle();
            bundle.putInt("UID", uid);
            bundle.putInt("PID", pid);
            bundle.putInt("requestID", -1);
            mWcmChannel.sendMessage(WifiManager.WWSM_MULTI_NETWORK_REQUEST, 1, 0, bundle);
        }
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
        resultMsg.recycle();
        return result;
    }

    public void enableRssiPolling(boolean enabled) {
        sendMessage(CMD_ENABLE_RSSI_POLL, enabled ? 1 : 0, 0);
    }

    //+SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
    private void runEleBlockRoamTimer() {
        if (mEleBlockRoamTimer != null) {
            Log.i(TAG, "mEleBlockRoamTimer timer cancled");
            mEleBlockRoamTimer.cancel();
        }
        mEleBlockRoamTimer = new Timer();
        mEleBlockRoamTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "mEleBlockRoamTimer timer expired - enable Roam network valid transition");
                mEleBlockRoamTimer = null;
                if (mEleBlockRoamConnection) {
                    if (mWcmChannel != null) {
                        mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_ELE_ENABLE_VALID);
                        Log.d(TAG, "CheckEleEnvironment CMD_ELE_ENABLE_VALID was delivered ");
                    }
                    mEleBlockRoamConnection = false;
                }
            }
        }, 20000);
    }

    public void resetEleBlockRoamConnection() {
        mEleBlockRoamConnection = false;
    }

    private int getWifiEleBeaconStats() {
        if (mInterfaceName == null) {
            loge("getWifiEleBeaconStats called without an interface");
            return 0;
        }
        WifiLinkLayerStats stats = mWifiNative.getWifiLinkLayerStats(mInterfaceName);
        if (stats != null) {
            return stats.beacon_rx;
        } 
        return 0;
    }
    
    private int getCurrentRssi() {
        if (mInterfaceName == null) {
            return -1;
        }
        
        WifiNative.SignalPollResult pollResult = mWifiNative.signalPoll(mInterfaceName);
        if (pollResult == null) {
            return -1;
        } else {
            return pollResult.currentRssi;
        }
    }

    public boolean checkGeoMagneticSensorValidTiming() {
        if (mEleExpireCount > 5) {
            return true;
        } else {
            return false;
        }
    }

    public void enableEleMobileRssiPolling(boolean enable, boolean removeBlock) {
        if (isWifiOnly()) 
            return;

        Log.i(TAG, "enableEleMobileRssiPolling : " + enable);
        enableMobileRssiPolling(enable);
        
        if (removeBlock) {
            mBlockUntilNewAssoc = false;
        }
    }

    public void scanStarted() {
        Log.i(TAG, "Ele scanStarted");
        mEleIsScanRunning = true;

        // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
            if (MobileWIPS.getInstance() != null) {
                MobileWIPS.getInstance().sendEmptyMessage(MWIPSDef.EVENT_SCAN_STARTED);
            }
        }
        // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
    }
    
    public void scanFinished() {
        Log.i(TAG, "Ele scanFinished");
        mEleIsScanRunning = false;

        // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
            if (MobileWIPS.getInstance() != null) {
                MobileWIPS.getInstance().sendEmptyMessage(MWIPSDef.EVENT_SCAN_FINISHED);
            }
        }
        // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
    }

    public void setEleRecentStep() {
        mEleIsStepPending = true;
    }

    private void resetEleParameters(int stableCount, boolean newBssid, boolean resetHistory) {
        Log.i(TAG, "resetEleParameters");
        if (resetHistory) {
            for(int x=0; x<ELE_PREVIOUS_CHECK_CNT; x++) {
                mElePrevMobileRssi[x]=0;
                mElePrevWifiRssi[x] = 0;
                mElePrevBcnDiff[x]=0;
                mElePrevGeoMagneticChanges[x] = false;
                mElePrevStepState[x] = false;
            }
        } 

         mElePrevBcnCnt = -1;
         mEleBcnHistoryCnt = 0;
         mEleStableCount = stableCount;
         mEleBcnMissExpireCnt = 0;
         mElePollingSkip = false;
         mGeomagneticEleState = false;
         mEleDetectionPending = false;
         mEleInvalidByLegacyDelayCheckCnt = 0;
         mEleRoamingStationaryCnt = 0;
         mEleBcnCheckingPrevState = ELE_CHECK_BEACON_NONE;
         mEleBcnDropExpireCnt = 0;
         if (newBssid) {
             mBlockUntilNewAssoc = false; 
         }
    }

    public void setGeomagneticEleState(boolean state) {
        mGeomagneticEleState = state;
    }

    private boolean getMobileRssiChangeWithTimeDiff(int mobileRssi, int secondsScope, int diffCond) {
        int difference = 0;
        int index = ELE_PREVIOUS_CHECK_CNT - secondsScope;

        if (mElePrevMobileRssi[index] == 0)
            return false;

        if ( mElePrevMobileRssi[index] != 0) {
            difference = ((-mobileRssi) - (-mElePrevMobileRssi[index]));
        }

        if (difference >= diffCond) {
            Log.i(TAG, "CheckEleEnvironment - getMobileRssiChangeWithTimeDiff - mobileRssi : " + mobileRssi + " secondsCope :" + secondsScope + " diffCond :" + diffCond);
            return true;
        } else {
            return false;
        }
    }

    private boolean getWifiRssiChangeWithTimeDiff(int wifiRssi, int secondsScope, int diffCond) {
        int difference = 0;
        int index = ELE_PREVIOUS_CHECK_CNT - secondsScope;
        
        if (mElePrevWifiRssi[index] == 0)
            return false;

        if ( mElePrevWifiRssi[index] != 0) {
            difference = ((-wifiRssi) - (-mElePrevWifiRssi[index]));
        }
        if (difference >= diffCond) {
            Log.i(TAG, "CheckEleEnvironment - getWifiRssiChangeWithTimeDiff - wifiRssi : " + wifiRssi + " secondsCope :" + secondsScope + " diffCond :" + diffCond);
            return true;
        } else {
            return false;
        }
    }
    
    public void setElePollingSkipMode(boolean skipMode) {
        if (skipMode) {
            mElePollingMode = ELE_POLLING_ONE_TERM_SKIP;
        } else {
            mElePollingMode = ELE_POLLING_DEFAULT;
        }

        Log.i(TAG, "CheckEleEnvironment - setElePollingSkipMode mElePollingMode :" + mElePollingMode);
    }

    private void CheckEleEnvironment(int mobileRssi, int runningBcnCount, int wifiRssi) {
        int bcnDiff = -1;
        
        if ((mEleExpireCount % ELE_PREVIOUS_CHECK_CNT) == 0 || mEleExpireCount == 1) {
            Log.i(TAG, "CheckEleEnvironment BD : " + mElePrevBcnDiff[0] + " " + mElePrevBcnDiff[1] + " " + mElePrevBcnDiff[2] + " " + mElePrevBcnDiff[3] + " " + mElePrevBcnDiff[4] + " " + mElePrevBcnDiff[5] + 
                        " MD : " + mElePrevMobileRssi[0] + " " + mElePrevMobileRssi[1] + " " + mElePrevMobileRssi[2] + " " + mElePrevMobileRssi[3] + " " + mElePrevMobileRssi[4] + " " + mElePrevMobileRssi[5] +
                        " WD : " + mElePrevWifiRssi[0] + " " + mElePrevWifiRssi[1] + " " + mElePrevWifiRssi[2] + " " + mElePrevWifiRssi[3] + " " + mElePrevWifiRssi[4] + " " + mElePrevWifiRssi[5] +
                        " GC : " + mElePrevGeoMagneticChanges[0] + " " + mElePrevGeoMagneticChanges[1] + " " + mElePrevGeoMagneticChanges[2] + " " + mElePrevGeoMagneticChanges[3] + " " + mElePrevGeoMagneticChanges[4] + " " + mElePrevGeoMagneticChanges[5] +
                        " SC : " + mElePrevStepState[0] + " " + mElePrevStepState[1] + " " + mElePrevStepState[2] + " " + mElePrevStepState[3] + " " + mElePrevStepState[4] + " " + mElePrevStepState[5]);
        }

        if (mEleExpireCount-- == 0) {
            Log.i(TAG, "CheckEleEnvironment - finished by expiration count");
            enableMobileRssiPolling(false);
            return;
        } 
     
        int previousNonZeroBeaconCnt = getPrevNonZeroBcnCnt();        
        int previousStableBeaconCnt = getPrevStableBcnCnt();
        int prevAverBcnCnt = getPrevAverBcnCnt();

        if (mElePrevBcnCnt != -1) {
            if (runningBcnCount < 0) {
                resetEleParameters(1, false, true);
                return;
            } else {
                bcnDiff = runningBcnCount - mElePrevBcnCnt;
                if (bcnDiff <= -1) { 
                    Log.e(TAG, "CheckEleEnvironment - Abnormal beacon cnt : "+ bcnDiff);
                    bcnDiff = prevAverBcnCnt;
                }
                if (bcnDiff > 13) bcnDiff = 13;
                
                if (bcnDiff > 0) mEleDetectionPending = false;
            }
        }

        if (wifiRssi <= ELE_MINIMUM_RSSI && mEleBcnHistoryCnt >= ELE_PREVIOUS_CHECK_CNT && bcnDiff < 4 && wifiRssi <= mElePrevWifiRssi[0]) {
            checkEleEnvironmentConfirmFactors(bcnDiff, mobileRssi, wifiRssi, previousNonZeroBeaconCnt, previousStableBeaconCnt, prevAverBcnCnt);
        }

        shiftEleFactors(mobileRssi, wifiRssi, bcnDiff, (mPrevGeomagneticEleState == false && mGeomagneticEleState) ? true : false, mEleStableCount, mEleIsStepPending);
        mElePrevBcnCnt = runningBcnCount;
        mPrevGeomagneticEleState = mGeomagneticEleState;
        
        if (mEleStableCount !=0) {
            mEleStableCount --;
        }
        
        if (mEleInvalidByLegacyDelayCheckCnt != 0) {
            mEleInvalidByLegacyDelayCheckCnt --;
        }

        if (mEleRoamingStationaryCnt != 0) {
            mEleRoamingStationaryCnt --;
        }

        mEleIsStepPending = false;
    }

    private void checkEleEnvironmentConfirmFactors(int bcnDiff, int mobileRssi,  int wifiRssi, int previousNonZeroBeaconCnt, int previousStableBeaconCnt, int prevAverBcnCnt) {
        boolean bBigSignalChangeWithStationary = false;
        boolean bTwoBigSignalChangeWithStationary = false;
        boolean bStationaryForFiveSeconds = checkAllStepStateFalseByStationary();

        if (bStationaryForFiveSeconds) {
            boolean bMobileBigChange = false;
            boolean bWifiBigChange = false;
            bMobileBigChange = checkMobileSignalChange();
            bWifiBigChange = checkWifiSignalChange();

            if (bMobileBigChange && bWifiBigChange) {
                bTwoBigSignalChangeWithStationary = true;
                Log.i(TAG, "CheckEleEnvironment - bTwoBigSignalChangeWithStationary!!");
            }
            if (bMobileBigChange || bWifiBigChange) {
                bBigSignalChangeWithStationary = true;
                Log.i(TAG, "CheckEleEnvironment - bBigSignalLossDetected!!");
                if (mEleInvalidByLegacyDelayCheckCnt > 0) {
                    Log.i(TAG, "CheckEleEnvironment - Ele detection with prev Invalid and big signal change");
                    eleDetected(bcnDiff, mobileRssi, wifiRssi);
                    return;
                }
            }
        }

        if (bcnDiff == 0) {
            Log.d(TAG, "CheckEleEnvironment - bcnDiff Zero! mEleExpireCount : " + mEleExpireCount + " wifiRssi : " + wifiRssi + " mElePrevWifiRssi[0] : " + mElePrevWifiRssi[0] + " previousNonZeroBeaconCnt : " + previousNonZeroBeaconCnt + " previousStableBeaconCnt : " + previousStableBeaconCnt + " prevAverBcnCnt : " + prevAverBcnCnt);
            if (bTwoBigSignalChangeWithStationary) {
                Log.i(TAG, "CheckEleEnvironment - Ele detection by bTwoBigSignalChangeWithStationary");
                eleDetected(bcnDiff, mobileRssi, wifiRssi);
                return;
            }
            
            if (bBigSignalChangeWithStationary && mGeomagneticEleState) {
                eleDetected(bcnDiff, mobileRssi, wifiRssi);
                return;
            }
        }
          
        mEleBcnCheckingState = getBcnCheckingState(previousNonZeroBeaconCnt, previousStableBeaconCnt, prevAverBcnCnt, bcnDiff);

        if (mEleBcnCheckingState == ELE_CHECK_BEACON_MISS || mEleBcnCheckingState == ELE_CHECK_BEACON_SUDDEN_DROP) {
            if (mEleRoamingStationaryCnt > 0) {
                if (bBigSignalChangeWithStationary) {
                    Log.i(TAG, "CheckEleEnvironment - Ele situation with roaming and bBigSignalChangeWithStationary!");
                    eleDetected(bcnDiff, mobileRssi, wifiRssi);
                }
            } else {
                Log.i(TAG, "CheckEleEnvironment - Beacon loss checking starteded!");
                if((mGeomagneticEleState && checkGeoMagneticRecentChange()) ||
                        mEleDetectionPending ||
                        bBigSignalChangeWithStationary ||
                        (getMobileRssiChangeWithTimeDiff(mobileRssi, 3, 5) || getMobileRssiChangeWithTimeDiff(mobileRssi, 4, 6)) ||
                        (getMobileRssiChangeWithTimeDiff(mobileRssi, 6, 8) &&  wifiRssi < mElePrevWifiRssi[0]) ||
                        (checkContiousMobileRssiDecrease(mobileRssi) &&
                                (getMobileRssiChangeWithTimeDiff(mobileRssi, 2 , 3) && getWifiRssiChangeWithTimeDiff(wifiRssi, 2, 5)) ||
                                (getMobileRssiChangeWithTimeDiff(mobileRssi, 6 , 11) && wifiRssi <= mElePrevWifiRssi[0]))) {
                    if ((mEleIsScanRunning || mEleIsStepPending) && !mGeomagneticEleState) {
                        mEleDetectionPending = true;
                        if (mEleIsScanRunning) {
                            Log.i(TAG, "CheckEleEnvironment - Beacon loss ignored by ScanRunning");
                        } else {
                            Log.i(TAG, "CheckEleEnvironment - Beacon loss ignored by Step Cnt Pending");
                        }
                    } else {
                        Log.i(TAG, "CheckEleEnvironment - Ele situation detected! bGigSignal : " + bBigSignalChangeWithStationary);
                        eleDetected(bcnDiff, mobileRssi, wifiRssi);
                    }
                }
            }
        }
    }

    private int getBcnCheckingState(int previousNonZeroBeaconCnt, int previousStableBeaconCnt, int prevAverBcnCnt, int bcnDiff) {
        int checkingState = ELE_CHECK_BEACON_NONE;
        if (previousNonZeroBeaconCnt == ELE_PREVIOUS_CHECK_CNT) {
            Log.d(TAG, "CheckEleEnvironment - previousNonZeroBeaconCnt Non Zero Beacon Count Condition!");
            if (bcnDiff == 0 && previousStableBeaconCnt >= (ELE_PREVIOUS_CHECK_CNT - 2)) {
                checkingState = ELE_CHECK_BEACON_MISS;
                mEleBcnMissExpireCnt = 5;
            } else {
                if (mEleBcnCheckingPrevState == ELE_CHECK_BEACON_SUDDEN_DROP) {
                    if (mElePrevBcnDropCond >= bcnDiff) {
                        Log.d(TAG, "CheckEleEnvironment - Sudden Drop continue!");
                        checkingState = ELE_CHECK_BEACON_SUDDEN_DROP;
                        mEleBcnDropExpireCnt --;
                    } else {
                        Log.d(TAG, "CheckEleEnvironment - Sudden Drop finished.");
                        mEleBcnDropExpireCnt = 0;
                    }
                } else {
                    Log.i(TAG, "CheckEleEnvironment - Sudden Drop checking prevAverBcnCnt : " + prevAverBcnCnt);
                    if ((bcnDiff <= 2 && prevAverBcnCnt >= 7) || (bcnDiff <= 3 && prevAverBcnCnt >= 9)) {
                        if (prevAverBcnCnt >= 7) {
                            mElePrevBcnDropCond = 2;
                        } else {
                            mElePrevBcnDropCond = 3;
                        }
                        checkingState = ELE_CHECK_BEACON_SUDDEN_DROP;
                        mEleBcnDropExpireCnt = 5;
                    }
                }
            }
        } else {
            if (bcnDiff == 0 && mEleBcnMissExpireCnt > 0) {
                Log.d(TAG, "CheckEleEnvironment - already in beacon miss in progress!  mEleBcnMissExpireCnt : " + mEleBcnMissExpireCnt);
                checkingState = ELE_CHECK_BEACON_MISS;
                mEleBcnMissExpireCnt--;
            } else {
                mEleDetectionPending = false;
            }
        }

        if (bcnDiff == 0) {
            mEleBcnDropExpireCnt = 0;
        }

        mEleBcnCheckingPrevState = checkingState;
        return checkingState;
    }
    
    private void eleDetected(int bcnDiff, int mobileRssi, int wifiRssi) {
        if (mBlockUntilNewAssoc) {
            Log.i(TAG, "CheckEleEnvironment - mBlockUntilNewAssoc is ture. it could be continous ELE pattern.");
        } else if (mSemSarManager.isBodySarGrip()) {
            Log.i(TAG, "CheckEleEnvironment - isBodySarGrip true. eleDetection blocked.");
        }else  {
            mBlockUntilNewAssoc = true;
            if (mWcmChannel != null) {
                if(mGeomagneticEleState) {
                    mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_ELE_BY_GEO_DETECTED);
                } else {
                    mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_ELE_DETECTED);
                }
                mEleBlockRoamConnection = true;
                runEleBlockRoamTimer();
                setPreviousAverageScoreToZero();
                Log.i(TAG, "CheckEleEnvironment detection BD : " + mElePrevBcnDiff[0] + " " + mElePrevBcnDiff[1] + " " + mElePrevBcnDiff[2] + " " + mElePrevBcnDiff[3] + " " + mElePrevBcnDiff[4] + " " + mElePrevBcnDiff[5] + " " + bcnDiff +
                        " MD : " + mElePrevMobileRssi[0] + " " + mElePrevMobileRssi[1] + " " + mElePrevMobileRssi[2] + " " + mElePrevMobileRssi[3] + " " + mElePrevMobileRssi[4] + " " + mElePrevMobileRssi[5] + " " + mobileRssi +
                        " WD : " + mElePrevWifiRssi[0] + " " + mElePrevWifiRssi[1] + " " + mElePrevWifiRssi[2] + " " + mElePrevWifiRssi[3] + " " + mElePrevWifiRssi[4] + " " + mElePrevWifiRssi[5] + " " + wifiRssi +
                        " GC : " + mElePrevGeoMagneticChanges[0] + " " + mElePrevGeoMagneticChanges[1] + " " + mElePrevGeoMagneticChanges[2] + " " + mElePrevGeoMagneticChanges[3] + " " + mElePrevGeoMagneticChanges[4] + " " + mElePrevGeoMagneticChanges[5] +
                        " SC : " + mElePrevStepState[0] + " " + mElePrevStepState[1] + " " + mElePrevStepState[2] + " " + mElePrevStepState[3] + " " + mElePrevStepState[4] + " " + mElePrevStepState[5]);
                resetEleParameters(0, false, true);
                enableMobileRssiPolling(false);
            }
        }            
    }

    private boolean checkAllStepStateFalseByStationary() {
        if (mEleIsStepPending)
            return false;

        for(int x=1; x<ELE_PREVIOUS_CHECK_CNT; x++) {
            if (mElePrevStepState[x]) {
                return false;
            }
        }
        return true;
    }

    private boolean checkMobileSignalChange() {
        boolean bMobileBigLoss = false;
        int baseIndex = 1;
        int diffValue = 0;
        for (baseIndex = 1; baseIndex < (ELE_PREVIOUS_CHECK_CNT-1); baseIndex++) {
            for (int index = baseIndex; index <ELE_PREVIOUS_CHECK_CNT; index++ ) {
                diffValue = mElePrevMobileRssi[0] - mElePrevMobileRssi[index];
                if (diffValue >= 9 || diffValue <= -9) {
                bMobileBigLoss = true;
                break;
            }
        }
        }
        return bMobileBigLoss;
    }

    private boolean checkWifiSignalChange() {
        boolean bWifiBigLoss = false;
        int baseIndex = 1;
        int diffValue = 0;
        for (baseIndex = 1; baseIndex < (ELE_PREVIOUS_CHECK_CNT-1); baseIndex++) {
            for (int index = baseIndex; index <ELE_PREVIOUS_CHECK_CNT; index++ ) {
                diffValue = mElePrevWifiRssi[0] - mElePrevWifiRssi[index];
                if (diffValue >= 9) {
                    bWifiBigLoss = true;
                    break;
                }
            }
        }
        return bWifiBigLoss;
    }

    private boolean checkGeoMagneticRecentChange() {
        if(mPrevGeomagneticEleState == false && mGeomagneticEleState) {
            return true;
        }
        
        for(int x=1; x<ELE_PREVIOUS_CHECK_CNT; x++) {
            if (mElePrevGeoMagneticChanges[x])
                return true;
        }
        return false;
    }

    private void shiftEleFactors(int mobileRssi, int wifiRssi, int bcnDiff, boolean geoChange, int eleStableCount, boolean stepPending) {
        System.arraycopy(mElePrevMobileRssi, 1, mElePrevMobileRssi, 0, mElePrevMobileRssi.length - 1);
        mElePrevMobileRssi[mElePrevMobileRssi.length - 1] = mobileRssi;

        System.arraycopy(mElePrevWifiRssi,1 ,mElePrevWifiRssi ,0 ,mElePrevWifiRssi.length - 1);
        mElePrevWifiRssi[mElePrevWifiRssi.length - 1] = wifiRssi;

        if (bcnDiff != -1) {
            if (mEleBcnHistoryCnt < ELE_PREVIOUS_CHECK_CNT) {
                if (eleStableCount == 0) mEleBcnHistoryCnt ++;
            }

            System.arraycopy(mElePrevBcnDiff, 1, mElePrevBcnDiff, 0, mElePrevBcnDiff.length - 1);
            mElePrevBcnDiff[mElePrevBcnDiff.length - 1] = bcnDiff;
        }
        
        System.arraycopy(mElePrevGeoMagneticChanges,1 ,mElePrevGeoMagneticChanges ,0 ,mElePrevGeoMagneticChanges.length - 1);
        mElePrevGeoMagneticChanges[mElePrevGeoMagneticChanges.length - 1] = geoChange;
        
        System.arraycopy(mElePrevStepState,1 ,mElePrevStepState ,0 ,mElePrevStepState.length - 1);
        mElePrevStepState[mElePrevStepState.length - 1] = stepPending;
    }

    private boolean checkContiousMobileRssiDecrease(int mobileRssi) {
        if(mElePrevMobileRssi[0] == 0) 
            return false;
        if(mElePrevMobileRssi[0] >= mElePrevMobileRssi[1] &&
                mElePrevMobileRssi[1] >= mElePrevMobileRssi[2] &&
                mElePrevMobileRssi[2] >= mElePrevMobileRssi[3] &&
                mElePrevMobileRssi[3] >= mElePrevMobileRssi[4] &&
                mElePrevMobileRssi[4] >= mElePrevMobileRssi[5] && 
                mElePrevMobileRssi[5] >= mobileRssi) {
            Log.i(TAG, "CheckEleEnvironment - checkContiousMobileRssiDecrease true");
            return true;
        }
        return false;                        
    }

    private int getPrevAverBcnCnt() {
        int totalBeaconCount = 0;
        if(mEleBcnHistoryCnt == 0) 
            return 0;

        for(int x = ELE_PREVIOUS_CHECK_CNT - 1; x >= (ELE_PREVIOUS_CHECK_CNT - mEleBcnHistoryCnt) ; x--) {
            totalBeaconCount += mElePrevBcnDiff[x];
        }
        return totalBeaconCount / mEleBcnHistoryCnt;
    }

    private int getPrevStableBcnCnt() {
        int previousNonZeroBeaconCnt = 0;
        if(mEleBcnHistoryCnt != 0) {
            for(int x = ELE_PREVIOUS_CHECK_CNT - 1; x >= (ELE_PREVIOUS_CHECK_CNT - mEleBcnHistoryCnt) ; x--) {
                if(mElePrevBcnDiff[x] > 0) {
                    previousNonZeroBeaconCnt++;
                }
            }
         }
        return previousNonZeroBeaconCnt;
    }
    
    private int getPrevNonZeroBcnCnt() {
        int previousNonZeroBeaconCnt = 0;
        if(mEleBcnHistoryCnt != 0) {
            for(int x = ELE_PREVIOUS_CHECK_CNT - 1; x >= (ELE_PREVIOUS_CHECK_CNT - mEleBcnHistoryCnt) ; x--) {
                if(mElePrevBcnDiff[x] > 0) {
                    previousNonZeroBeaconCnt++;
                }
            }
         }
        return previousNonZeroBeaconCnt;
    }

    public void delayedEleCheckDisable() {
        mEleExpireCount = 6;
        mEleInvalidByLegacyDelayCheckCnt = 6;
    }

    private void enableMobileRssiPolling(boolean enabled) {
        if (enabled) {
            mEleExpireCount = ELE_EXPIRE_COUNT;
        }

        if (!mEleEnableMobileRssiPolling) {
            if (enabled) {
                resetEleParameters(0, false, true);
            }
        }
        
        if(enabled && mEleEnableMobileRssiPolling == false) {
            Log.i(TAG, "enableMobileRssiPolling true");
        } else if (enabled == false && mEleEnableMobileRssiPolling) {
            if (mWcmChannel != null) {
                mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_ELE_UNREGISTER_SENSORS);
            }
            Log.i(TAG, "enableMobileRssiPolling false");
        }
        
        mEleEnableMobileRssiPolling = enabled;
    }

    public boolean isEleCheckRunning() {
        return mEleEnableMobileRssiPolling;
    }
    //-SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
    
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
        if (mNetworkAgent != null) {
            try {
                return new Network(mNetworkAgent.netId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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
     * Get the wificonfiguration for specofoc netID
     * @hide
     */
    public WifiConfiguration getSpecificNetwork(int netID) {
        if (netID == WifiConfiguration.INVALID_NETWORK_ID) {
            return null;
        }
        return mWifiConfigManager.getConfiguredNetwork(netID);
    }

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

    public void dumpIpClient(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (mIpClient != null) {
            mIpClient.dump(fd, pw, args);
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
        pw.println("mNeedDeregisterationFlag " + mDelayDisconnect.isEnabled()); //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
        pw.println("semmDhcpRenewAfterRoamingMode : " + semmDhcpRenewAfterRoamingMode); //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
        mCountryCode.dump(fd, pw, args);

        if (mNetworkFactory != null) {
            mNetworkFactory.dump(fd, pw, args);
        } else {
            pw.println("mNetworkFactory is not initialized");
        }

        if (mUntrustedNetworkFactory != null) {
            mUntrustedNetworkFactory.dump(fd, pw, args);
        } else {
            pw.println("mUntrustedNetworkFactory is not initialized");
        }
        pw.println("Wlan Wake Reasons:" + mWifiNative.getWlanWakeReasonCount());
        pw.println();

        if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
            mWifiGeofenceManager.dump(fd, pw, args);
        }

        mWifiConfigManager.dump(fd, pw, args);
        pw.println();
        mPasspointManager.dump(pw);
        pw.println();
        mWifiDiagnostics.captureBugReportData(WifiDiagnostics.REPORT_REASON_USER_ACTION);
        mWifiDiagnostics.dump(fd, pw, args);
        dumpIpClient(fd, pw, args);
        if (mWifiConnectivityManager != null) {
            mWifiConnectivityManager.dump(fd, pw, args);
        } else {
            pw.println("mWifiConnectivityManager is not initialized");
        }
        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_AUTO_WIFI - disable google autowifi
        //mWifiInjector.getWakeupController().dump(fd, pw, args);
        pw.println();
        pw.println("mIsRfTestMode " + isRfTestMode());
        pw.println("mConcurrentEnabled " + mConcurrentEnabled); //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
        pw.println("mIsImsCallEstablished " + mIsImsCallEstablished); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
        if (mUnstableApController != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
            mUnstableApController.dump(fd, pw, args);
        }
        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
        pw.println("W24H (wifi scan auto fav sns agr ...):" + getWifiParameters(false));
        if (mIssueDetector != null) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
            mIssueDetector.dump(fd, pw, args);
        }

        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
            if (MobileWIPS.getInstance() != null) {
                MobileWIPS.getInstance().dump(fd, pw, args);
            }
        }

        if (mWifiRecommendNetworkLevelController != null) { //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
            mWifiRecommendNetworkLevelController.dump(fd, pw, args);
        }

        if (mScanRequestProxy != null) { //SEC_PRODUCT_FEATURE_WLAN_SCAN_CONTROLLER
            mScanRequestProxy.dump(fd, pw, args);
        }

        runFwDump(); //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
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
        if (reset && mScanRequestProxy != null) {
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
        //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
       List<WifiConfiguration> configs = mWifiConfigManager.getSavedNetworks();
        sb.append(configs.size()).append(" ");
        WifiRecommendNetworkDynamicScore wifiRecommendNetworkDynamicScore = mWifiInjector.getWifiRecommendNetworkDynamicScore();
        int connectionApScore = wifiRecommendNetworkDynamicScore.getConnectionAPScoreWeek();
        int connectedAutoWifiScore = wifiRecommendNetworkDynamicScore.getConnectionAPScoreAutoWifi();
        int savedConfigScore = wifiRecommendNetworkDynamicScore.getSavedConfigurationScore();
        int totalDynamicNetworkScore = connectionApScore + connectedAutoWifiScore + savedConfigScore;
        sb.append(totalDynamicNetworkScore).append(" ");
        int connectionApCount = wifiRecommendNetworkDynamicScore.getConnectionAPSize();
        sb.append(connectionApCount).append(" ");
        if (mWifiRecommendNetworkManager == null) {
            mWifiRecommendNetworkManager = mWifiInjector.getWifiRecommendNetworkManager();
        }
        sb.append(mWifiRecommendNetworkManager.getRecommendedNetworkCount());
        //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
        if (reset) {
            resetCounter();
            mWifiRecommendNetworkManager.resetRecommendedNetworkCount(); //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
        }

        return sb.toString();
    }

    //+SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
    public void startPoorQualityScoreChck() {
        Log.i(TAG, "startPoorQualityScoreCheck");
        mScoreQualityCheckMode = SCORE_QUALITY_CHECK_STATE_POOR_MONITOR;
        mPreviousScore[0] = 0;
        mPreviousScore[1] = 0;
        mPreviousScore[2] = 0;
        mPrevoiusScoreAverage = 0;
        mPreviousTxBad = 0;
        mPreviousTxSuccess = 0;
        mPreviousTxBadTxGoodRatio = 0;
        mLastGoodScore = 1000;
        mLastPoorScore = 100;
        mPoorCheckInProgress = false;
    }

    public void startGoodQualityScoreCheck() {
        Log.i(TAG, "startGoodQualityScoreCheck");
        mScoreQualityCheckMode = SCORE_QUALITY_CHECK_STATE_VALID_CHECK;
        mGoodScoreCount = 0;
        mGoodScoreTotal = 0;
    }
    
    public void stopScoreQualityCheck() {
        Log.i(TAG, "stopScoreQualityCheck");
        mScoreQualityCheckMode = SCORE_QUALITY_CHECK_STATE_NONE;
    }

    public void checkScoreBasedQuality(int s2Score) {
        int txGood = 0;
        int txBad = 0;

        WifiNative.TxPacketCounters counters =
                mWifiNative.getTxPacketCounters(mInterfaceName);
        if (counters != null) {
            txGood = counters.txSucceeded;
            txBad = counters.txFailed;
        }

        if (mScoreQualityCheckMode >= SCORE_QUALITY_CHECK_STATE_POOR_MONITOR) {
            long txBadDiff = txBad - mPreviousTxBad;
            long TxGoodDiff = txGood - mPreviousTxSuccess;
            long currentTxBadRatio = 0;

            int validScoreCnt = 0;

            if (s2Score <= ConnectedScore.WIFI_TRANSITION_SCORE) {
                if (mWcmChannel != null) {
                    Log.i(TAG, "checkScoreBasedQuality - less than 50 : s2Score : " + s2Score);
                    if (s2Score < mLastPoorScore) {
                        mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_TRANSITION_TRIGGER_BY_SCORE_50);
                        mLastPoorScore = s2Score;
                    }
                }
            }

            for (int x= 0; x < 3; x++) {
                if(mPreviousScore[x] != 0) validScoreCnt++;
            }

            int scoreTotal = mPreviousScore[0] + mPreviousScore[1] + mPreviousScore[2];
            if (validScoreCnt > 0) {
                mPrevoiusScoreAverage = scoreTotal/validScoreCnt;
            }
            Log.i(TAG, "checkScoreBasedQuality - " + " mPreviousScore[0]:" + mPreviousScore[0] + " mPreviousScore[1]:" + mPreviousScore[1] + " mPreviousScore[2]:" + mPreviousScore[2] + " s2Score:" + s2Score + "mPrevoiusScoreAverage:" + mPrevoiusScoreAverage);

            if ((mScoreQualityCheckMode == SCORE_QUALITY_CHECK_STATE_POOR_CHECK) && mLastGoodScore > s2Score) {
                if (mWcmChannel != null) {
                    if (mPoorCheckInProgress) {
                        mPoorCheckInProgress = false;
                    } else {
                        mPoorCheckInProgress = true;
                        Log.i(TAG, "checkScoreBasedQuality - Score Quality Check by score decrease");
                        mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_QUALITY_CHECK_BY_SCORE);
                    }
                }
            } else {
                if ( txBadDiff > 5 ) {
                    if (TxGoodDiff > 0) {
                        currentTxBadRatio = (int)(txBadDiff * 100.0f / TxGoodDiff);
                    } else {
                        currentTxBadRatio = 100 + txBadDiff;
                    }
                    Log.i(TAG, "checkScoreBasedQuality - " + " currentTxBadRatio:" + currentTxBadRatio);
                    if (currentTxBadRatio > SCORE_TXBAD_RATIO_THRESHOLD) {
                        if (mScoreQualityCheckMode == SCORE_QUALITY_CHECK_STATE_POOR_MONITOR) {
                            if ((s2Score * validScoreCnt) < scoreTotal) {
                                mScoreQualityCheckMode = SCORE_QUALITY_CHECK_STATE_POOR_CHECK;
                                if (mWcmChannel != null) {
                                    if (mPoorCheckInProgress) {
                                        mPoorCheckInProgress = false;
                                    } else {
                                        mPoorCheckInProgress = true;
                                        Log.i(TAG, "checkScoreBasedQuality - Score Quality Check by averageScore decrease");
                                        mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_QUALITY_CHECK_BY_SCORE);
                                    }
                                }
                            } 
                        } else {
                            Log.i(TAG, "SCORE_QUALITY_CHECK_STATE_POOR_CHECK: " + " mPreviousTxBadTxGoodRatio:" + mPreviousTxBadTxGoodRatio);
                            if (mPreviousTxBadTxGoodRatio != 0 && currentTxBadRatio > mPreviousTxBadTxGoodRatio) {
                                if (mWcmChannel != null) {
                                    if(mPoorCheckInProgress) {
                                        mPoorCheckInProgress = false;
                                    } else {
                                        Log.i(TAG, "checkScoreBasedQuality - Score Quality Check by txBadRatio increase");
                                        mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_QUALITY_CHECK_BY_SCORE);
                                        mPoorCheckInProgress = true;
                                    }
                                }
                            }
                        }
                        if (mPreviousTxBadTxGoodRatio < currentTxBadRatio) {
                            mPreviousTxBadTxGoodRatio = currentTxBadRatio;
                        }
                    }   
                }
            }

            mPreviousTxBad = txBad;
            mPreviousTxSuccess = txGood;
            mPreviousScore[0] = mPreviousScore[1];
            mPreviousScore[1] = mPreviousScore[2];
            if (mPoorCheckInProgress == false) {
                if (mLastGoodScore > mPreviousScore[2]) {
                    mLastGoodScore = mPreviousScore[2];
                }
            }
            mPreviousScore[2] = s2Score;
        } else {
            mGoodScoreCount += 1;
            mGoodScoreTotal += s2Score;
            if (mGoodScoreCount >= 3) {
                int newAverage = (mGoodScoreTotal/3);
                Log.i(TAG, "checkScoreBasedQuality - newAverage: "+ newAverage);
                if (newAverage > mPrevoiusScoreAverage) {
                    if (mWcmChannel != null) {
                        Log.i(TAG, "checkScoreBasedQuality - Score Quality Check by score increase");
                        mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_QUALITY_CHECK_BY_SCORE);
                    }
                    mPrevoiusScoreAverage = newAverage;
                }
                mGoodScoreTotal = 0;
                mGoodScoreCount = 0;
            }
        }
    }

    private void setPreviousAverageScoreToZero() {
        mPrevoiusScoreAverage = 0;
    }

    private void preventGoodQualityScoreCheck() {
        mPrevoiusScoreAverage = 1000;
    }
    //-SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK

    //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
    private void runFwDump(){
        mWifiNative.saveFwDump(mInterfaceName);
        runFwLogTimer();
    }

    //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
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

    //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
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

    //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
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

    public void handleUserSwitch(int userId) {
        sendMessage(CMD_USER_SWITCH, userId);
    }

    public void handleUserUnlock(int userId) {
        sendMessage(CMD_USER_UNLOCK, userId);
    }

    public void handleUserStop(int userId) {
        sendMessage(CMD_USER_STOP, userId);
    }

    /**
     * ******************************************************
     * Internal private functions
     * ******************************************************
     */

    private void logStateAndMessage(Message message, State state) {
        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
        ReportUtil.updateWifiStateMachineProcessMessage(
                state.getClass().getSimpleName(), message.what);

        messageHandlingStatus = 0;
        if (mVerboseLoggingEnabled) {
            logd(" " + state.getClass().getSimpleName() + " " + getLogRecString(message));
        }
    }

    @Override
    protected boolean recordLogRec(Message msg) {
        switch (msg.what) {
            case WifiManager.RSSI_PKTCNT_FETCH:
            //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE //case CMD_RSSI_POLL:
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
        if (mScreenOn) {
            sb.append("!");
        }
        if (messageHandlingStatus != MESSAGE_HANDLING_STATUS_UNKNOWN) {
            sb.append("(").append(messageHandlingStatus).append(")");
        }
        sb.append(smToString(msg));
        if (msg.sendingUid > 0 && msg.sendingUid != Process.WIFI_UID) {
            sb.append(" uid=" + msg.sendingUid);
        }
        sb.append(" rt=").append(mClock.getUptimeSinceBootMillis());
        sb.append("/").append(mClock.getElapsedSinceBootMillis());
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
                sb.append(" blacklist=" + Boolean.toString(didBlackListBSSID));
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
                sb.append(" locallyGenerated=").append(msg.arg1); //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                sb.append(" reason=").append(msg.arg2);
                // if (mLastBssid != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                //     sb.append(" lastbssid=").append(mLastBssid);
                // }
                sb.append(" bssid=").append((String) msg.obj); //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                if (mWifiInfo.getFrequency() != -1) {
                    sb.append(" freq=").append(mWifiInfo.getFrequency());
                    sb.append(" rssi=").append(mWifiInfo.getRssi());
                }
                break;
            case CMD_RSSI_POLL:
            case CMD_UNWANTED_NETWORK:
            case WifiManager.RSSI_PKTCNT_FETCH:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (mWifiInfo.getSSID() != null)
                    if (mWifiInfo.getSSID() != null)
                        sb.append(" ").append(mWifiInfo.getSSID());
                if (mWifiInfo.getBSSID() != null)
                    sb.append(" ").append(mWifiInfo.getBSSID());
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
            case DhcpClient.CMD_PRE_DHCP_ACTION:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" txpkts=").append(mWifiInfo.txSuccess);
                sb.append(",").append(mWifiInfo.txBad);
                sb.append(",").append(mWifiInfo.txRetries);
                break;
            case DhcpClient.CMD_POST_DHCP_ACTION:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                if (msg.arg1 == DhcpClient.DHCP_SUCCESS) {
                    sb.append(" OK ");
                } else if (msg.arg1 == DhcpClient.DHCP_FAILURE) {
                    sb.append(" FAIL ");
                }
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
                sb.append(" len=" + ((byte[])msg.obj).length);
                break;
            case CMD_SET_FALLBACK_PACKET_FILTERING:
                sb.append(" enabled=" + (boolean)msg.obj);
                break;
            case CMD_ROAM_WATCHDOG_TIMER:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(roamWatchdogCount);
                break;
            case CMD_DISCONNECTING_WATCHDOG_TIMER:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(disconnectingWatchdogCount);
                break;
            case CMD_DISABLE_P2P_WATCHDOG_TIMER:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" cur=").append(mDisableP2pWatchdogCount);
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
                if (msg.arg1 == DhcpClient.DHCP_SUCCESS) {
                    sb.append("DHCP_OK");
                } else if (msg.arg1 == CMD_STATIC_IP_SUCCESS) {
                    sb.append("STATIC_OK");
                } else {
                    sb.append(Integer.toString(msg.arg1));
                }
                break;
            case CMD_IPV4_PROVISIONING_FAILURE:
                sb.append(" ");
                if (msg.arg1 == DhcpClient.DHCP_FAILURE) {
                    sb.append("DHCP_FAIL");
                } else if (msg.arg1 == CMD_STATIC_IP_FAILURE) {
                    sb.append("STATIC_FAIL");
                } else {
                    sb.append(Integer.toString(msg.arg1));
                }
                break;
            // >>>WCM>>>
            case CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON:
                sb.append(" visible:");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" id:");
                sb.append(Integer.toString(msg.arg2));
                sb.append(" reason:");
                sb.append(msg.obj.toString());
                break;
            // <<<WCM<<<
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
            case WifiMonitor.WIPS_EVENT: //SEC_PRODUCT_FEATURE_WLAN_CONFIG_TIPS_VERSION 1.3
                if (msg.obj != null) {
                    sb.append(" BSSID=").append((String) msg.obj);
                }
                break;
            default:
                sb.append(" ");
                sb.append(Integer.toString(msg.arg1));
                sb.append(" ");
                sb.append(Integer.toString(msg.arg2));
                break;
        }

        return sb.toString();
    }

    private void handleScreenStateChanged(boolean screenOn) {
        mScreenOn = screenOn;
        if (mVerboseLoggingEnabled) {
            logd(" handleScreenStateChanged Enter: screenOn=" + screenOn
                    + " mUserWantsSuspendOpt=" + mUserWantsSuspendOpt
                    + " state " + getCurrentState().getName()
                    + " suppState:" + mSupplicantStateTracker.getSupplicantStateName());
        }
        enableRssiPolling(screenOn || getRssiPollingEnabledForIms()); //CscFeature_Wifi_SupportRssiPollStateDuringWifiCalling

        //+SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
        if (!screenOn) {
            mEleBlockRoamConnection = false;
            resetEleParameters(0, true, true);
        }
        //-SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK

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
        lastScreenStateChangeTimeStamp = lastLinkLayerStatsUpdate;

        mWifiMetrics.setScreenState(screenOn);

        if (mWifiConnectivityManager != null) {
            mWifiConnectivityManager.handleScreenStateChanged(screenOn);
        }

        if (mVerboseLoggingEnabled) log("handleScreenStateChanged Exit: " + screenOn);
    }

    private void checkAndSetConnectivityInstance() {
        if (mCm == null) {
            mCm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
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
                    if (now < mLastConnectedTime + delay * 1000) {
                        logd("KnoxCustom WifiAutoSwitch: delay " + delay);
                        return;
                    }

                    int bestRssi = thresholdRssi;
                    int bestNetworkId = -1;
                    List<ScanResult> scanResults = mScanRequestProxy.getScanResults();
                    List<WifiConfiguration> configs = mWifiConfigManager.getSavedNetworks();
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
        Integer newRssi = null;
        Integer newLinkSpeed = null;
        Integer newFrequency = null;
        WifiNative.SignalPollResult pollResult = mWifiNative.signalPoll(mInterfaceName);
        if (pollResult == null) {
            return;
        }

        newRssi = pollResult.currentRssi;
        newLinkSpeed = pollResult.txBitrate;
        newFrequency = pollResult.associationFrequency;

        if (mVerboseLoggingEnabled) {
            logd("fetchRssiLinkSpeedAndFrequencyNative rssi=" + newRssi +
                 " linkspeed=" + newLinkSpeed + " freq=" + newFrequency);
        }

        if (newRssi != null && newRssi > WifiInfo.INVALID_RSSI && newRssi < WifiInfo.MAX_RSSI) {
            // screen out invalid values
            /* some implementations avoid negative values by adding 256
             * so we need to adjust for that here.
             */
            if (newRssi > 0) newRssi -= 256;
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
                    newLinkSpeed);
            }
            mLastSignalLevel = newSignalLevel;
        } else {
            mWifiInfo.setRssi(WifiInfo.INVALID_RSSI);
            updateCapabilities();
            mLastSignalLevel = -1; //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
        }

        if (newLinkSpeed != null) {
            mWifiInfo.setLinkSpeed(newLinkSpeed);
        }
        if (newFrequency != null && newFrequency > 0) {
            mWifiInfo.setFrequency(newFrequency);
        }
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LINK_INFO) {
            int snr = mWifiNative.getSnr(mInterfaceName);
            int lqcmReport = mWifiNative.getLqcmReport(mInterfaceName);
            mWifiInfo.semSetSnr(snr > 0 ? snr : 0);
            int lqcmRxIndex = lqcmReport != -1 ? (lqcmReport & 0xff0000) >> 16 : 0xff;
            int lqcmTxIndex = lqcmReport != -1 ? (lqcmReport & 0x00ff00) >> 8 : 0xff;
            mWifiInfo.semSetLqcmTx(lqcmTxIndex);
            mWifiInfo.semSetLqcmRx(lqcmRxIndex);
        }
        mWifiConfigManager.updateScanDetailCacheFromWifiInfo(mWifiInfo);
        /*
         * Increment various performance metrics
         */
        if (newRssi != null && newLinkSpeed != null && newFrequency != null) {
            mWifiMetrics.handlePollResult(mWifiInfo);
        }
    }

    // Polling has completed, hence we wont have a score anymore
    private void cleanWifiScore() {
        mWifiInfo.txBadRate = 0;
        mWifiInfo.txSuccessRate = 0;
        mWifiInfo.txRetriesRate = 0;
        mWifiInfo.rxSuccessRate = 0;
        mWifiScoreReport.reset();
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
        if (mVerboseLoggingEnabled) {
            log("Link configuration changed for netId: " + mLastNetworkId
                    + " old: " + mLinkProperties + " new: " + newLp);
        }
        // We own this instance of LinkProperties because IpClient passes us a copy.
        mLinkProperties = newLp;
        if (mNetworkAgent != null) {
            mNetworkAgent.sendLinkProperties(mLinkProperties);
        }
        //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
            if (MobileWIPS.getInstance() != null) {
                MobileWIPS.getInstance().setLinkProperties(newLp);
            }
        }
        //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        
        if (getNetworkDetailedState() == DetailedState.CONNECTED) {
            // If anything has changed and we're already connected, send out a notification.
            // TODO: Update all callers to use NetworkCallbacks and delete this.
            sendLinkConfigurationChangedBroadcast();
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
        monitorNetworkPropertiesUpdate(); // Smart network switch
        // if (mRoamingRenew == 0) {
        if(mWcmChannel != null) {
            mWcmChannel.sendMessage(CMD_NETWORK_PROPERTIES_UPDATED, 0, 0, new LinkProperties(mLinkProperties));
        }
        // }
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

    /**
     * try to update default route MAC address.
     */
    private String updateDefaultRouteMacAddress(int timeout) {
        String address = null;
        for (RouteInfo route : mLinkProperties.getRoutes()) {
            if (route.isDefaultRoute() && route.hasGateway()) {
                InetAddress gateway = route.getGateway();
                if (gateway instanceof Inet4Address) {
                    if (mVerboseLoggingEnabled) {
                        logd("updateDefaultRouteMacAddress found Ipv4 default :"
                                + gateway.getHostAddress());
                    }
                    address = macAddressFromRoute(gateway.getHostAddress());
                    /* The gateway's MAC address is known */
                    // if ((address == null) && (timeout > 0)) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_NETWORK_EVALUATOR
                    //     boolean reachable = false;
                    //     TrafficStats.setThreadStatsTag(TrafficStats.TAG_SYSTEM_PROBE);
                    //     try {
                    //         reachable = gateway.isReachable(timeout);
                    //     } catch (Exception e) {
                    //         loge("updateDefaultRouteMacAddress exception reaching :"
                    //                 + gateway.getHostAddress());

                    //     } finally {
                    //         TrafficStats.clearThreadStatsTag();
                    //         if (reachable == true) {

                    //             address = macAddressFromRoute(gateway.getHostAddress());
                    //             if (mVerboseLoggingEnabled) {
                    //                 logd("updateDefaultRouteMacAddress reachable (tried again) :"
                    //                         + gateway.getHostAddress() + " found " + address);
                    //             }
                    //         }
                    //     }
                    // }
                    if (address != null) {
                        mWifiConfigManager.setNetworkDefaultGwMacAddress(mLastNetworkId, address);
                    }
                }
            }
        }
        return address;
    }

    private void sendRssiChangeBroadcast(final int newRssi) {
        try {
            mBatteryStats.noteWifiRssiChanged(newRssi);
        } catch (RemoteException e) {
            // Won't happen.
        }
        Intent intent = new Intent(WifiManager.RSSI_CHANGED_ACTION);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT);
        intent.putExtra(WifiManager.EXTRA_NEW_RSSI, newRssi);
        mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
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
        if ((SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_SUPPORTSECUREWIFI)
            && SemFloatingFeature.getInstance().getBoolean("SEC_FLOATING_FEATURE_WLAN_SUPPORT_SECURE_WIFI"))) {
            PackageManager pm = context.getPackageManager();
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

        if (hidden == true) {
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
            WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
            int hotspotLiveStatus = wifiManager.getSmartApConnectedStatus(stateChangeResult.BSSID); // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ENHANCED_MOBILEAP
            if (hotspotLiveStatus == 1 || hotspotLiveStatus == 2) { // ST_CONNECTING = 1 , ST_CONNECTED = 2
                Log.d(TAG, "setHotspotLiveAp true");
                mWifiInfo.setHotspotLiveAp(true);
            }
        } else {
            // Reset parameters according to WifiInfo.reset()
            mWifiInfo.setNetworkId(WifiConfiguration.INVALID_NETWORK_ID);
            mWifiInfo.setBSSID(null);
            mWifiInfo.setSSID(null);
            mWifiInfo.setHotspotLiveAp(false); // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ENHANCED_MOBILEAP
        }
        // SSID might have been updated, so call updateCapabilities
        updateCapabilities();

        final WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            mWifiInfo.setEphemeral(config.ephemeral);

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

                    // Set mCheckVsieForSns if scan result include LO's VSIE
                    boolean vsieFound = false;
                    for (ScanResult.InformationElement im : scanDetail.getScanResult().informationElements) {
                        if (im.id == 0xdd && im.bytes.length >= 3
                                && (im.bytes[0] == (byte) 0x00) && (im.bytes[1] == (byte) 0x17) && (im.bytes[2] == (byte) 0xf2)) {
                            // Includes Vendor Specific IE with OUI 0x0017f2
                            vsieFound = true;
                            Log.d(TAG, "setCheckVsieForSns");
                            break;
                        }
                    }
                    mWifiInfo.setCheckVsieForSns(vsieFound);

                } else {
                    Log.d(TAG, "can't update vendor infos, bssid: " + stateChangeResult.BSSID);
                }
            }
        }

        mSupplicantStateTracker.sendMessage(Message.obtain(message));
        return state;
    }

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

        stopRssiMonitoringOffload();

        clearTargetBssid("handleNetworkDisconnect");

        stopIpClient();

        /* Reset data structures */
        mWifiScoreReport.reset();
        mWifiInfo.reset();
        /* Reset roaming parameters */
        mIsAutoRoaming = false;

        setNetworkDetailedState(DetailedState.DISCONNECTED);
        if (mNetworkAgent != null) {
            checkAndResetMtu(); //SEC_PRODUCT_FEATURE_WLAN_RESET_MTU
            mNetworkAgent.sendNetworkInfo(mNetworkInfo);
            mNetworkAgent = null;
        }
        // >>>WCM>>>
        /* SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK */
        if (mWcmChannel != null) mWcmChannel.sendMessage(WifiManager.WWSM_NETWORK_DISCONNECTED);
        // <<<WCM<<<

        /* Clear network properties */
        clearLinkProperties();

        /* Cend event to CM & network change broadcast */
        sendNetworkStateChangeBroadcast(mLastBssid);

        mLastBssid = null;
        registerDisconnected();
        mLastNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
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
        mWifiNative.setPowerSave(mInterfaceName, false);

        // Update link layer stats
        getWifiLinkLayerStats();

        if (mWifiP2pChannel != null) {
            /* P2p discovery breaks dhcp, shut it down in order to get through this */
            Message msg = new Message();
            msg.what = WifiP2pServiceImpl.BLOCK_DISCOVERY;
            msg.arg1 = WifiP2pServiceImpl.ENABLED;
            msg.arg2 = DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE;
            msg.obj = WifiStateMachine.this;
            mWifiP2pChannel.sendMessage(msg);
        } else {
            // If the p2p service is not running, we can proceed directly.
            sendMessage(DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE);
        }
        notifyWcmDhcpSession("start"); // WCM
    }

    void handlePostDhcpSetup() {
        /* Restore power save and suspend optimizations */
        setSuspendOptimizationsNative(SUSPEND_DUE_TO_DHCP, true);
        mWifiNative.setPowerSave(mInterfaceName, true);

        p2pSendMessage(WifiP2pServiceImpl.BLOCK_DISCOVERY, WifiP2pServiceImpl.DISABLED);

        // Set the coexistence mode back to its default value
        mWifiNative.setBluetoothCoexistenceMode(
                mInterfaceName, WifiNative.BLUETOOTH_COEXISTENCE_MODE_SENSE);
        notifyWcmDhcpSession("complete"); // WCM
    }

    private static final long DIAGS_CONNECT_TIMEOUT_MILLIS = 60 * 1000;
    private long mDiagsConnectionStartMillis = -1;
    /**
     * Inform other components that a new connection attempt is starting.
     */
    private void reportConnectionAttemptStart(
            WifiConfiguration config, String targetBSSID, int roamType) {
        mWifiMetrics.startConnectionEvent(config, targetBSSID, roamType);
        mDiagsConnectionStartMillis = mClock.getElapsedSinceBootMillis();
        mWifiDiagnostics.reportConnectionEvent(
                mDiagsConnectionStartMillis, WifiDiagnostics.CONNECTION_EVENT_STARTED);
        mWrongPasswordNotifier.onNewConnectionAttempt();
        // TODO(b/35329124): Remove CMD_DIAGS_CONNECT_TIMEOUT, once WifiStateMachine
        // grows a proper CONNECTING state.
        sendMessageDelayed(CMD_DIAGS_CONNECT_TIMEOUT,
                mDiagsConnectionStartMillis, DIAGS_CONNECT_TIMEOUT_MILLIS);
    }

    /**
     * Inform other components (WifiMetrics, WifiDiagnostics, WifiConnectivityManager, etc.) that
     * the current connection attempt has concluded.
     */
    private void reportConnectionAttemptEnd(int level2FailureCode, int connectivityFailureCode) {
        mWifiMetrics.endConnectionEvent(level2FailureCode, connectivityFailureCode);
        mWifiConnectivityManager.handleConnectionAttemptEnded(level2FailureCode);
        switch (level2FailureCode) {
            case WifiMetrics.ConnectionEvent.FAILURE_NONE:
                // Ideally, we'd wait until IP reachability has been confirmed. this code falls
                // short in two ways:
                // - at the time of the CMD_IP_CONFIGURATION_SUCCESSFUL event, we don't know if we
                //   actually have ARP reachability. it might be better to wait until the wifi
                //   network has been validated by IpClient.
                // - in the case of a roaming event (intra-SSID), we probably trigger when L2 is
                //   complete.
                //
                // TODO(b/34181219): Fix the above.
                mWifiDiagnostics.reportConnectionEvent(
                        mDiagsConnectionStartMillis, WifiDiagnostics.CONNECTION_EVENT_SUCCEEDED);
                break;
            case WifiMetrics.ConnectionEvent.FAILURE_REDUNDANT_CONNECTION_ATTEMPT:
            case WifiMetrics.ConnectionEvent.FAILURE_CONNECT_NETWORK_FAILED:
                // WifiDiagnostics doesn't care about pre-empted connections, or cases
                // where we failed to initiate a connection attempt with supplicant.
                break;
            default:
                mWifiDiagnostics.reportConnectionEvent(
                        mDiagsConnectionStartMillis, WifiDiagnostics.CONNECTION_EVENT_FAILED);
        }
        mDiagsConnectionStartMillis = -1;
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
                logd("handleIPv4Success, roaming and address changed" +
                        mWifiInfo + " got: " + addr);
            }
        }

        mWifiInfo.setInetAddress(addr);

        final WifiConfiguration config = getCurrentWifiConfiguration();
        if (config != null) {
            mWifiInfo.setEphemeral(config.ephemeral);
        }

        // Set meteredHint if DHCP result says network is metered
        if (dhcpResults.hasMeteredHint()) {
            Log.d(TAG, "setMeteredHint by dhcpResults");
            mWifiInfo.setMeteredHint(true);
        } else if (mWifiInfo.isHotspotLiveAp()) { // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ENHANCED_MOBILEAP
            Log.d(TAG, "setMeteredHint for HotspotLiveAp");
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
                WifiMetricsProto.ConnectionEvent.HLF_DHCP);
        synchronized(mDhcpResultsLock) {
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

        //+SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(mLastNetworkId);
        if (config != null && config.enterpriseConfig != null) {
            sendEapLogging(config, EAP_LOGGING_STATE_DHCP_FAILURE, "None");
        }
        //-SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG

        /* DHCP times out after about 30 seconds, we do a
         * disconnect thru supplicant, we will let autojoin retry connecting to the network
         */
        mWifiNative.disconnect(mInterfaceName);
        mEaptLoggingControllConnecting = true; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
        mEaptLoggingControllAuthFailure = true; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
    }

    // TODO: De-duplicated this and handleIpConfigurationLost().
    private void handleIpReachabilityLost() {
        mWifiInfo.setInetAddress(null);
        mWifiInfo.setMeteredHint(false);

        // TODO: Determine whether to call some form of mWifiConfigManager.handleSSIDStateChange().

        // Disconnect via supplicant, and let autojoin retry connecting to the network.
        mWifiNative.disconnect(mInterfaceName);
        mEaptLoggingControllConnecting = true; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
        mEaptLoggingControllAuthFailure = true; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
    }

    /*
     * Read a MAC address in /proc/arp/table, used by WifistateMachine
     * so as to record MAC address of default gateway.
     **/
    private String macAddressFromRoute(String ipAddress) {
        String macAddress = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/net/arp"));

            // Skip over the line bearing colum titles
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
                loge("Did not find remoteAddress {" + ipAddress + "} in " +
                        "/proc/net/arp");
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

    private class WifiNetworkFactory extends NetworkFactory {
        public WifiNetworkFactory(Looper l, Context c, String TAG, NetworkCapabilities f) {
            super(l, c, TAG, f);
        }

        @Override
        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            synchronized (mWifiReqCountLock) {
                if (++mConnectionReqCount == 1) {
                    if (mWifiConnectivityManager != null && mUntrustedReqCount == 0) {
                        mWifiConnectivityManager.enable(true);
                    }
                }
            }
        }

        @Override
        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            synchronized (mWifiReqCountLock) {
                if (--mConnectionReqCount == 0) {
                    if (mWifiConnectivityManager != null && mUntrustedReqCount == 0) {
                        mWifiConnectivityManager.enable(false);
                    }
                }
            }
        }

        @Override
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mConnectionReqCount " + mConnectionReqCount);
        }

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

    private class UntrustedWifiNetworkFactory extends NetworkFactory {
        public UntrustedWifiNetworkFactory(Looper l, Context c, String tag, NetworkCapabilities f) {
            super(l, c, tag, f);
        }

        @Override
        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            if (!networkRequest.networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_TRUSTED)) {
                synchronized (mWifiReqCountLock) {
                    if (++mUntrustedReqCount == 1) {
                        if (mWifiConnectivityManager != null) {
                            if (mConnectionReqCount == 0) {
                                mWifiConnectivityManager.enable(true);
                            }
                            mWifiConnectivityManager.setUntrustedConnectionAllowed(true);
                        }
                    }
                }
            }
        }

        @Override
        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            if (!networkRequest.networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_TRUSTED)) {
                synchronized (mWifiReqCountLock) {
                    if (--mUntrustedReqCount == 0) {
                        if (mWifiConnectivityManager != null) {
                            mWifiConnectivityManager.setUntrustedConnectionAllowed(false);
                            if (mConnectionReqCount == 0) {
                                mWifiConnectivityManager.enable(false);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("mUntrustedReqCount " + mUntrustedReqCount);
        }
    }

    void maybeRegisterNetworkFactory() {
        if (mNetworkFactory == null) {
            checkAndSetConnectivityInstance();
            if (mCm != null) {
                mNetworkFactory = new WifiNetworkFactory(getHandler().getLooper(), mContext,
                        NETWORKTYPE, mNetworkCapabilitiesFilter);
                mNetworkFactory.setScoreFilter(60);
                mNetworkFactory.register();

                // We can't filter untrusted network in the capabilities filter because a trusted
                // network would still satisfy a request that accepts untrusted ones.
                mUntrustedNetworkFactory = new UntrustedWifiNetworkFactory(getHandler().getLooper(),
                        mContext, NETWORKTYPE_UNTRUSTED, mNetworkCapabilitiesFilter);
                mUntrustedNetworkFactory.setScoreFilter(Integer.MAX_VALUE);
                mUntrustedNetworkFactory.register();
            }
        }
    }

    /**
     * WifiStateMachine needs to enable/disable other services when wifi is in client mode.  This
     * method allows WifiStateMachine to get these additional system services.
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
            Log.d(TAG, "ConnectedMacRandomization SSID(" + config.getPrintableSsid()
                    + "). setMacAddress(" + newMac.toString() + ") from "
                    + currentMac.toString() + " = " + setMacSuccess);
        }
    }

    /**
     * Update whether Connected MAC Randomization is enabled in WifiStateMachine
     * and WifiInfo.
     */
    private void updateConnectedMacRandomizationSetting() {
        int macRandomizationFlag = mFacade.getIntegerSetting(
                mContext, Settings.Global.WIFI_CONNECTED_MAC_RANDOMIZATION_ENABLED, 0);
        boolean macRandomizationEnabled = (macRandomizationFlag == 1);
        mEnableConnectedMacRandomization.set(macRandomizationEnabled);
        mWifiInfo.setEnableConnectedMacRandomization(macRandomizationEnabled);
        mWifiMetrics.setIsMacRandomizationOn(macRandomizationEnabled);
        Log.d(TAG, "EnableConnectedMacRandomization Setting changed to "
                + macRandomizationEnabled);
    }

    /**
     * Helper method to check if Connected MAC Randomization is enabled - onDown events are skipped
     * if this feature is enabled (b/72459123).
     *
     * @return boolean true if Connected MAC randomization is enabled, false otherwise
     */
    public boolean isConnectedMacRandomizationEnabled() {
        return mEnableConnectedMacRandomization.get();
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
    }

    /********************************************************
     * HSM states
     *******************************************************/

    class DefaultState extends State {
        @Override
        public void enter() {
            sendMessageDelayed( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    CMD_24HOURS_PASSED_AFTER_BOOT,
                    DBG_PRODUCT_DEV ? 10 * 60 * 1000 : 24 * 60 * 60 * 1000);
            //+SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
            if (mEleBlockRoamConnection) {
                if (mWcmChannel != null) {
                    mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_ELE_ENABLE_VALID);
                    Log.d(TAG, "CheckEleEnvironment CMD_ELE_ENABLE_VALID was delivered ");
                }
                mEleBlockRoamConnection = false;
            }
            //-SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
        }

        @Override
        public boolean processMessage(Message message) {
            logStateAndMessage(message, this);

            switch (message.what) {
                case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED: {
                    AsyncChannel ac = (AsyncChannel) message.obj;
                    if (ac == mWifiP2pChannel) {
                        if (message.arg1 == AsyncChannel.STATUS_SUCCESSFUL) {
                            p2pSendMessage(AsyncChannel.CMD_CHANNEL_FULL_CONNECTION);
                            // since the p2p channel is connected, we should enable p2p if we are in
                            // connect mode.  We may not be in connect mode yet, we may have just
                            // set the operational mode and started to set up for connect mode.
                            if (mOperationalMode == CONNECT_MODE) {
                                // This message will only be handled if we are in Connect mode.
                                // If we are not in connect mode yet, this will be dropped and the
                                // ConnectMode.enter method will call to enable p2p.
                                sendMessage(CMD_ENABLE_P2P);
                            }
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
                    mBluetoothConnectionActive = (message.arg1 !=
                            BluetoothAdapter.STATE_DISCONNECTED);
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
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
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
                    updateBssidWhitelist(config); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
                    NetworkUpdateResult result = //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
                            mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid, from);
                    if (!result.isSuccess()) {
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    replyToMessage(message, message.what, result.getNetworkId());
                    break;
                case CMD_REMOVE_NETWORK:
                    deleteNetworkConfigAndSendReply(message, false);
                    break;
                case CMD_GET_CONFIGURED_NETWORKS:
                    replyToMessage(message, message.what, mWifiConfigManager.getSavedNetworks());
                    break;
                case CMD_GET_SPECIFIC_CONFIGURED_NETWORKS: //SEC_PRODUCT_FEATURE_WLAN_GET_SPECIFIC_NETWORK
                    WifiConfiguration pickConfig = mWifiConfigManager.getConfiguredNetwork(message.arg1);
                    if (pickConfig != null) {
                        replyToMessage(message, message.what, new WifiConfiguration(pickConfig));
                    } else {
                        replyToMessage(message, message.what, null);
                    }
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
                    mPasspointManager.initializeProvisioner(
                            mWifiInjector.getWifiServiceHandlerThread().getLooper());
                    initializeWifiChipInfo(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CHIP_INFO
                    replyToMessage(message, message.what, ok ? SUCCESS : FAILURE);
                    break;
                case CMD_BOOT_COMPLETED:
                    initializeWifiChipInfo(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CHIP_INFO
                    // get other services that we need to manage
                    getAdditionalWifiServiceInterfaces();
                    if (!mWifiConfigManager.loadFromStore()) {
                        Log.e(TAG, "Failed to load from config store");
                    }
                    maybeRegisterNetworkFactory();
                    resetFwLogFolder(); //SEC_PRODUCT_FEATURE_WLAN_FWDUMP_AUTO_REMOVE
                    mWifiConfigManager.forcinglyEnableAllNetworks(Process.SYSTEM_UID); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FORCINGLY_ENABLE_ALL_NETWORKS
                    break;
                case CMD_SCREEN_STATE_CHANGED:
                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
                    boolean screenOn = (message.arg1 != 0);
                    if (mScreenOn != screenOn) {
                        handleScreenStateChanged(screenOn);
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_MODE_BY_USER_LCD_ON
                    break;
                // >>>WCM>>>
                case CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON:
                    boolean visible = message.arg1 == 1 ? true : false;
                    showCaptivePortalDisabledNotification(visible);
                    break;
                case WifiConnectivityMonitor.CMD_INVALID_FW_DUMP:
                    int reason = message.arg1;
                    //+SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
                    if (mPreviousTxBadTxGoodRatio > 100) { 
                        reason = WifiConnectivityMonitor.BRCM_BIGDATA_ECNT_NOINTERNET_TXBAD;
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
                    sendMessage(CMD_REQUEST_FW_BIGDATA_PARAM, 0, reason);
                    
                    break;
                case WifiMonitor.EAP_EVENT_MESSAGE: //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
                    if (message.obj != null) {
                        processMessageForEap((String) message.obj);
                    }
                    break;
                // <<<WCM<<<
                case CMD_DISCONNECT:
                case CMD_RECONNECT:
                case CMD_REASSOCIATE:
                case CMD_RELOAD_TLS_AND_RECONNECT:
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                case WifiMonitor.AUTHENTICATION_FAILURE_EVENT:
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                case CMD_RSSI_POLL:
                case DhcpClient.CMD_PRE_DHCP_ACTION:
                case DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE:
                case DhcpClient.CMD_POST_DHCP_ACTION:
                case CMD_ENABLE_P2P:
                case WifiMonitor.SUP_REQUEST_IDENTITY:
                case CMD_TEST_NETWORK_DISCONNECT:
                case WifiMonitor.SUP_REQUEST_SIM_AUTH:
                case CMD_TARGET_BSSID:
                case CMD_START_CONNECT:
                case CMD_START_ROAM:
                case CMD_ASSOCIATED_BSSID:
                case CMD_UNWANTED_NETWORK:
                case CMD_DISCONNECTING_WATCHDOG_TIMER:
                case CMD_ROAM_WATCHDOG_TIMER:
                case CMD_DISABLE_P2P_WATCHDOG_TIMER:
                case CMD_DISABLE_EPHEMERAL_NETWORK:
                case CMD_SCAN_RESULT_AVAILABLE: //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                case CMD_CHECK_DHCP_AFTER_ROAMING: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
                case CMD_CHECK_DEFAULT_GWMACADDRESS: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
                case WifiConnectivityMonitor.LINK_DETECTION_DISABLED: // SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
                case CMD_ENABLE_WIFI_CONNECTIVITY_MANAGER: //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE
                case CMD_CHECK_DUPLICATED_IP: //DUPLICATED_IP_USING_DETECTION
                case CMD_REPLACE_PUBLIC_DNS: //CscFeature_Wifi_SupportNetworkDiagnostics
                case WifiMonitor.BSSID_PRUNED_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
                case WifiMonitor.SUITABLE_NETWORK_NOT_FOUND_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUITABLE_NETWORK_NOT_FOUND_EVENT
                case WifiMonitor.WIPS_EVENT: //SEC_PRODUCT_FEATURE_WLAN_CONFIG_TIPS_VERSION 1.3
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_DISABLE_P2P_RSP:
                    if(mP2pDisableListener != null){
                        Log.d(TAG,"DISABLE_P2P_RSP mP2pDisableListener == " +mP2pDisableListener);
                        mP2pDisableListener.onDisable();
                        mP2pDisableListener = null;
                    }
                    break;
                // >>>WCM>>>
                case WifiConnectivityMonitor.QC_UPDATE_SKIP_INTERNET_CHECK_VALUE: // WCM
                case CMD_DISABLED_CAPTIVE_PORTAL_SCAN_OUT:
                case CMD_ENABLE_CAPTIVE_PORTAL:
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                // <<<WCM<<<
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
                    int featureSet = mWifiNative.getSupportedFeatureSet(mInterfaceName);
                    replyToMessage(message, message.what, featureSet);
                    break;
                case CMD_GET_LINK_LAYER_STATS:
                    // Not supported hence reply with error message
                    replyToMessage(message, message.what, null);
                    logd("return HANDLED at DefaultState:processMessage:CMD_GET_LINK_LAYER_STATS"); //SEC_PRODUCT_FEATURE_WLAN_DEBUG_HIDL
                    break;
                case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED:
                    NetworkInfo info = (NetworkInfo) message.obj;
                    mP2pConnected.set(info.isConnected());
                    break;
                case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST:
                    mTemporarilyDisconnectWifi = (message.arg1 == 1);
                    replyToMessage(message, WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE);
                    break;
                /* Link configuration (IP address, DNS, ...) changes notified via netlink */
                case CMD_UPDATE_LINKPROPERTIES:
                    updateLinkProperties((LinkProperties) message.obj);
                    break;
                case CMD_GET_MATCHING_CONFIG:
                    replyToMessage(message, message.what);
                    break;
                case CMD_GET_MATCHING_OSU_PROVIDERS:
                    replyToMessage(message, message.what, new ArrayList<OsuProvider>());
                    break;
                case CMD_START_SUBSCRIPTION_PROVISIONING:
                case CMD_UPDATE_CONFIG_LOCATION: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
                    replyToMessage(message, message.what, 0);
                    break;
                case CMD_IP_CONFIGURATION_SUCCESSFUL:
                case CMD_IP_CONFIGURATION_LOST:
                case CMD_IP_REACHABILITY_LOST:
                case CMD_SEC_IP_CONFIGURATION_LOST: //SEC_PRODUCT_FEATURE_VALIDATION_CHECK_AFTER_PROVISIONING_FAIL
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_REMOVE_APP_CONFIGURATIONS:
                    deferMessage(message);
                    break;
                case CMD_REMOVE_USER_CONFIGURATIONS:
                    deferMessage(message);
                    break;
                case CMD_START_IP_PACKET_OFFLOAD:
                    if (mNetworkAgent != null) mNetworkAgent.onPacketKeepaliveEvent(
                            message.arg1,
                            ConnectivityManager.PacketKeepalive.ERROR_INVALID_NETWORK);
                    break;
                case CMD_STOP_IP_PACKET_OFFLOAD:
                    if (mNetworkAgent != null) mNetworkAgent.onPacketKeepaliveEvent(
                            message.arg1,
                            ConnectivityManager.PacketKeepalive.ERROR_INVALID_NETWORK);
                    break;
                case CMD_START_RSSI_MONITORING_OFFLOAD:
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_STOP_RSSI_MONITORING_OFFLOAD:
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_USER_SWITCH:
                    Set<Integer> removedNetworkIds =
                            mWifiConfigManager.handleUserSwitch(message.arg1);
                    if (removedNetworkIds.contains(mTargetNetworkId) ||
                            removedNetworkIds.contains(mLastNetworkId)) {
                        // Disconnect and let autojoin reselect a new network
                        disconnectCommand(0, DISCONNECT_REASON_USER_SWITCH); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
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
                    int addResult = mPasspointManager.addOrUpdateProvider(
                            (PasspointConfiguration) message.obj, message.arg1)
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
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DEFERRED;
                    deferMessage(message);
                    break;
                case CMD_INSTALL_PACKET_FILTER:
                    mWifiNative.installPacketFilter(mInterfaceName, (byte[]) message.obj);
                    break;
                case CMD_READ_PACKET_FILTER:
                    byte[] data = mWifiNative.readPacketFilter(mInterfaceName);
                    mIpClient.readPacketFilterComplete(data);
                    break;
                case CMD_SET_FALLBACK_PACKET_FILTERING:
                    if ((boolean) message.obj) {
                        mWifiNative.startFilteringMulticastV4Packets(mInterfaceName);
                    } else {
                        mWifiNative.stopFilteringMulticastV4Packets(mInterfaceName);
                    }
                    break;
                case CMD_CLIENT_INTERFACE_BINDER_DEATH:
                    report(ReportIdKey.ID_SYSTEM_PROBLEM, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                ReportUtil.getReportDataForHidlDeath());
                    /*TODO : Log.e(TAG, "wificond died unexpectedly. Triggering recovery");
                    mWifiMetrics.incrementNumWificondCrashes();
                    mWifiDiagnostics.captureBugReportData(
                            WifiDiagnostics.REPORT_REASON_WIFICOND_CRASH);
                    mWifiInjector.getSelfRecovery().trigger(SelfRecovery.REASON_WIFICOND_CRASH);*/
                    break;
                case CMD_VENDOR_HAL_HWBINDER_DEATH:
                    report(ReportIdKey.ID_SYSTEM_PROBLEM, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                ReportUtil.getReportDataForHidlDeath());
                    /*TODO : Log.e(TAG, "Vendor HAL died unexpectedly. Triggering recovery");
                    mWifiMetrics.incrementNumHalCrashes();
                    mWifiDiagnostics.captureBugReportData(WifiDiagnostics.REPORT_REASON_HAL_CRASH);
                    mWifiInjector.getSelfRecovery().trigger(SelfRecovery.REASON_HAL_CRASH);*/
                    break;
                case CMD_DIAGS_CONNECT_TIMEOUT:
                    mWifiDiagnostics.reportConnectionEvent(
                            (Long) message.obj, BaseWifiDiagnostics.CONNECTION_EVENT_FAILED);
                    break;
                case CMD_GET_ALL_MATCHING_CONFIGS:
                    replyToMessage(message, message.what, new ArrayList<WifiConfiguration>());
                    break;
                case CMD_SEC_API:
                    Log.d(TAG, "DefaultState::Handling CMD_SEC_API");
                    replyToMessage(message, message.what, -1);
                    break;
                case CMD_SEC_STRING_API:
                    String stringResult = processMessageOnDefaultStateForCallSECStringApi(message);
                    replyToMessage(message, message.what, stringResult);
                    break;
                // >>>WCM>>>
                case WifiConnectivityMonitor.CAPTIVE_PORTAL_STATE_EVENT:
                    if (message.arg1 == WifiConnectivityMonitor.CAPTIVE_PORTAL_EVENT_DETECTED) {
                        log("CAPTIVE_PORTAL_EVENT_DETECTED received - " + getCurrentState());
                        replyToMessage(message, message.what, -1); // failed
                    }
                    break;
                case WifiConnectivityMonitor.POOR_LINK_DETECTED:
                    break;
                case WifiConnectivityMonitor.INITIAL_CONNECTION_STARTED:
                    setStopScanForWCM(true);
                    break;
                case WifiConnectivityMonitor.INITIAL_CONNECTION_FINISHED:
                    setStopScanForWCM(false);
                    break;
                case CMD_INITIAL_CONNECTION_TIMEOUT:
                    setStopScanForWCM(false);
                    break;
                // <<<WCM<<<
                case CMD_IMS_CALL_ESTABLISHED: //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
                    if (mIsImsCallEstablished != (message.arg1 == 1)) {
                        mIsImsCallEstablished = (message.arg1 == 1);
                        if (mWifiConnectivityManager != null) {
                            mWifiConnectivityManager.changeMaxPeriodicScanMode(mIsImsCallEstablished ?
                                    WifiConnectivityManager.MAX_PERIODIC_SCAN_WAKEUP_TIMER :
                                    WifiConnectivityManager.MAX_PERIODIC_SCAN_NON_WAKEUP_TIMER);
                        }
                    }
                    // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                            && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                            && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                        if (MobileWIPS.getInstance() != null) {
                            Message msg = new Message();
                            msg.what = MWIPSDef.EVENT_IMS_CALL_ESTABLISHED;
                            msg.arg1 = message.arg1;
                            MobileWIPS.getInstance().sendMessage(msg);
                        }
                    }
                    // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
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
                                if (!"1".equals(categoryId)) {
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
                case CMD_REQUEST_FW_BIGDATA_PARAM: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_SET_ADPS_MODE: //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_SEC_API_ASYNC:
                    if (processMessageOnDefaultStateForCallSECApiAsync(message)) {
                        break;
                    }
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case CMD_P2P_FACTORY_RESET:
                    p2pSendMessage(WifiStateMachine.CMD_P2P_FACTORY_RESET);
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
            return HANDLED;
        }
    }

    String smToString(Message message) {
        return smToString(message.what);
    }

    String smToString(int what) {
        String s = sSmToString.get(what);
        if (s != null) {
            return s;
        }
        switch (what) {
            case AsyncChannel.CMD_CHANNEL_HALF_CONNECTED:
                s = "AsyncChannel.CMD_CHANNEL_HALF_CONNECTED";
                break;
            case AsyncChannel.CMD_CHANNEL_DISCONNECTED:
                s = "AsyncChannel.CMD_CHANNEL_DISCONNECTED";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST:
                s = "WifiP2pServiceImpl.DISCONNECT_WIFI_REQUEST";
                break;
            case WifiManager.DISABLE_NETWORK:
                s = "WifiManager.DISABLE_NETWORK";
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
                s = "WifiMonitor.ANQP_DONE_EVENT";
                break;
            case WifiMonitor.RX_HS20_ANQP_ICON_EVENT:
                s = "WifiMonitor.RX_HS20_ANQP_ICON_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_DONE_EVENT:
                s = "WifiMonitor.GAS_QUERY_DONE_EVENT";
                break;
            case WifiMonitor.HS20_REMEDIATION_EVENT:
                s = "WifiMonitor.HS20_REMEDIATION_EVENT";
                break;
            case WifiMonitor.GAS_QUERY_START_EVENT:
                s = "WifiMonitor.GAS_QUERY_START_EVENT";
                break;
            case WifiP2pServiceImpl.GROUP_CREATING_TIMED_OUT:
                s = "GROUP_CREATING_TIMED_OUT";
                break;
            case WifiP2pServiceImpl.P2P_CONNECTION_CHANGED:
                s = "P2P_CONNECTION_CHANGED";
                break;
            case WifiP2pServiceImpl.DISCONNECT_WIFI_RESPONSE:
                s = "P2P.DISCONNECT_WIFI_RESPONSE";
                break;
            case WifiP2pServiceImpl.SET_MIRACAST_MODE:
                s = "P2P.SET_MIRACAST_MODE";
                break;
            case WifiP2pServiceImpl.BLOCK_DISCOVERY:
                s = "P2P.BLOCK_DISCOVERY";
                break;
            case WifiManager.RSSI_PKTCNT_FETCH:
                s = "RSSI_PKTCNT_FETCH";
                break;
            // >>>WCM>>>
            case CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON:
                s = "CMD_SHOW_CAPTIVE_PORTAL_DISABLE_NOTIFICAITON";
                break;
            // <<<WCM<<<
            //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
            case WifiMonitor.SUP_BIGDATA_EVENT:
                s = "WifiMonitor.SUP_BIGDATA_EVENT";
                break;
            //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
            //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
            case WifiMonitor.BSSID_PRUNED_EVENT:
                s = "WifiMonitor.BSSID_PRUNED_EVENT";
                break;
            //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
            case WifiMonitor.SUITABLE_NETWORK_NOT_FOUND_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUITABLE_NETWORK_NOT_FOUND_EVENT
                s = "WifiMonitor.SUITABLE_NETWORK_NOT_FOUND_EVENT";
                break;
            case WifiMonitor.WIPS_EVENT: //SEC_PRODUCT_FEATURE_WLAN_CONFIG_TIPS_VERSION 1.3
                s = "WifiMonitor.WIPS_EVENT";
                break;
            default:
                s = "what:" + Integer.toString(what);
                break;
        }
        return s;
    }

    /**
     * Helper method to start other services and get state ready for client mode
     */
    private void setupClientMode() {
        Log.d(TAG, "setupClientMode() ifacename = " + mInterfaceName);
        mWifiStateTracker.updateState(WifiStateTracker.INVALID);

        if (mWifiConnectivityManager == null) {
            synchronized (mWifiReqCountLock) {
                mWifiConnectivityManager =
                        mWifiInjector.makeWifiConnectivityManager(mWifiInfo,
                                                                  hasConnectionRequests());
                mWifiConnectivityManager.setUntrustedConnectionAllowed(mUntrustedReqCount > 0);
                mWifiConnectivityManager.handleScreenStateChanged(mScreenOn);
                if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                    mWifiGeofenceManager.register(mWifiConnectivityManager);
                    mWifiGeofenceManager.register(mNetworkInfo);
                }
                mWifiRecommendNetworkLevelController = mWifiInjector.getWifiRecommendNetworkLevelController(); //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
                mWifiRecommendNetworkManager = mWifiInjector.getWifiRecommendNetworkManager(); //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
            }
        }

        mIpClient = mFacade.makeIpClient(mContext, mInterfaceName, new IpClientCallback());
        mIpClient.setMulticastFilter(true);
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
        // Attempt to migrate data out of legacy store.
        if (!mWifiConfigManager.migrateFromLegacyStore()) {
            Log.e(TAG, "Failed to migrate from legacy config store");
        }
        mWifiConfigManager.removeUnneccessaryNetworks(); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONFIG_MANAGER
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

        mWifiNative.setPowerSave(mInterfaceName, true);

        if (mP2pSupported) {
            p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
        }
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

        if (mP2pSupported) {
            // we are not going to wait for a response - will still temporarily send the
            // disable command until p2p can detect the interface up/down on its own.
            p2pSendMessage(WifiStateMachine.CMD_DISABLE_P2P_REQ);
        }

        mIsRunning = false;
        updateBatteryWorkSource(null);

        if (mIpClient != null) {
            mIpClient.shutdown();
            // Block to make sure IpClient has really shut down, lest cleanup
            // race with, say, bringup code over in tethering.
            mIpClient.awaitShutdown();
        }
        mNetworkInfo.setIsAvailable(false);
        if (mNetworkAgent != null) mNetworkAgent.sendNetworkInfo(mNetworkInfo);
        mCountryCode.setReadyForChange(false);
        mInterfaceName = null;
        // TODO: b/79504296 This broadcast has been deprecated and should be removed
        sendSupplicantConnectionChangedBroadcast(false);
    }

    void registerConnected() {
        if (mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
            mWifiConfigManager.updateNetworkAfterConnect(mLastNetworkId);
            // On connect, reset wifiScoreReport
            mWifiScoreReport.reset();

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
            // Let's remove any ephemeral or passpoint networks on every disconnect.
            // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
            // mWifiConfigManager.removeAllEphemeralOrPasspointConfiguredNetworks();
            mWifiConfigManager.removeAllEphemeralNetworks();
        }
    }

    /**
     * Returns Wificonfiguration object correponding to the currently connected network, null if
     * not connected.
     */
    public WifiConfiguration getCurrentWifiConfiguration() {
        if (mLastNetworkId == WifiConfiguration.INVALID_NETWORK_ID) {
            return null;
        }
        return mWifiConfigManager.getConfiguredNetwork(mLastNetworkId);
    }

    ScanResult getCurrentScanResult() {
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (config == null) {
            return null;
        }
        String BSSID = mWifiInfo.getBSSID();
        if (BSSID == null) {
            BSSID = mTargetRoamBSSID;
        }
        ScanDetailCache scanDetailCache =
                mWifiConfigManager.getScanDetailCacheForNetwork(config.networkId);

        if (scanDetailCache == null) {
            return null;
        }

        return scanDetailCache.getScanResult(BSSID);
    }

    void setFccChannel() { //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL
        Log.d(TAG, "setFccChannel() is called" );

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
                mWifiNative.updateCellularCapabilities("wlan0", mCellularCapaState, mNetworktype
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
                    mWifiNative.updateCellularCapabilities("wlan0", mCellularCapaState, mNetworktype
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
                        mWifiNative.updateCellularCapabilities("wlan0", mCellularCapaState
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
            mScanRequestProxy.enableScanningForHiddenNetworks(true);
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
                if (mWifiConfigManager.getSavedNetworks().size() > 0) {
                    mWifiGeofenceManager.startGeofenceThread(mWifiConfigManager.getSavedNetworks());
                }
            }

            // Inform WifiConnectivityManager that Wifi is enabled
            mWifiConnectivityManager.setWifiEnabled(true);
            // Inform metrics that Wifi is Enabled (but not yet connected)
            mWifiMetrics.setWifiState(WifiMetricsProto.WifiLog.WIFI_DISCONNECTED);
            // Inform p2p service that wifi is up and ready when applicable
            p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
            // Inform sar manager that wifi is Enabled
            mSarManager.setClientWifiState(WifiManager.WIFI_STATE_ENABLED);

            mSemSarManager.setClientWifiState(WifiManager.WIFI_STATE_ENABLED); //SEMSAR

            setConcurrentEnabled(mConcurrentEnabled); //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE

            initializeWifiChipInfo(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CHIP_INFO

            setFccChannel(); //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL

            if (mIsHs20Enabled) {
                mWifiNative.setInterwokingEnabled(mInterfaceName, true);
            }            

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
        }

        @Override
        public void exit() {
            semmDhcpRenewAfterRoamingMode = 0;  //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
            if (getCurrentState() == mConnectedState) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                mDelayDisconnect.checkAndWait(mNetworkInfo);
            }

            // >>>WCM>>>
            mDisabledCaptivePortalList.clear();
            if (mLastDisabledCaptivePortalNetId != WifiConfiguration.INVALID_NETWORK_ID) {
                // remove captive portal disabled notification
                Message notiMsg = new Message();
                notiMsg.what = CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON;
                notiMsg.arg1 = 0; // false
                notiMsg.obj = (Object)"ConnectModeState Exit";
                sendMessage(notiMsg);
                mLastDisabledCaptivePortalNetId = WifiConfiguration.INVALID_NETWORK_ID;
            }
            // <<<WCM<<<

            mOperationalMode = DISABLED_MODE;
            // Let the system know that wifi is not available since we are exiting client mode.
            mNetworkInfo.setIsAvailable(false);
            if (mNetworkAgent != null) mNetworkAgent.sendNetworkInfo(mNetworkInfo);

            // Inform WifiConnectivityManager that Wifi is disabled
            mWifiConnectivityManager.setWifiEnabled(false);
            // Inform metrics that Wifi is being disabled (Toggled, airplane enabled, etc)
            mWifiMetrics.setWifiState(WifiMetricsProto.WifiLog.WIFI_DISABLED);
            // Inform sar manager that wifi is being disabled
            mSarManager.setClientWifiState(WifiManager.WIFI_STATE_DISABLED);

            mSemSarManager.setClientWifiState(WifiManager.WIFI_STATE_DISABLED);

            if (!mWifiNative.removeAllNetworks(mInterfaceName)) {
                loge("Failed to remove networks on exiting connect mode");
            }
            mScanRequestProxy.enableScanningForHiddenNetworks(false);
            // Do we want to optimize when we move from client mode to scan only mode.
            mScanRequestProxy.clearScanResults();
            mWifiInfo.reset();
            mWifiInfo.setSupplicantState(SupplicantState.DISCONNECTED);
            stopClientMode();

            setConcurrentEnabled(false); //SEC_PRODUCT_FEATURE_WLAN_CONCURRENT_MODE

            if (mWifiGeofenceManager.isSupported()
                    && !isGeofenceUsedByAnotherPackage()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                mWifiGeofenceManager.deinitGeofence();
            }
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
            logStateAndMessage(message, this);

            switch (message.what) {
                case WifiMonitor.ASSOCIATION_REJECTION_EVENT:
                    mWifiDiagnostics.captureBugReportData(
                            WifiDiagnostics.REPORT_REASON_ASSOC_FAILURE);
                    didBlackListBSSID = false;
                    bssid = (String) message.obj;
                    timedOut = message.arg1 > 0;
                    reasonCode = message.arg2;
                    Log.d(TAG, "Assocation Rejection event: bssid=" + bssid + " reason code="
                            + reasonCode + " timedOut=" + Boolean.toString(timedOut));
                    if (bssid == null || TextUtils.isEmpty(bssid)) {
                        // If BSSID is null, use the target roam BSSID
                        bssid = mTargetRoamBSSID;
                    }
                    if (bssid != null) {
                        // If we have a BSSID, tell configStore to black list it
                        didBlackListBSSID = mWifiConnectivityManager.trackBssid(bssid, false,
                            reasonCode);
                    }
                    if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) { //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
                        config = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
                        if (config != null && config.enterpriseConfig != null) {
                            int typeOfEap = -1;
                            typeOfEap = config.enterpriseConfig.getEapMethod();
                            if (typeOfEap != -1 && config.SSID != null && !TextUtils.isEmpty(config.SSID)) {
                                sendEapLogging(config, EAP_LOGGING_STATE_ASSOC_REJECT, "None");
                            }
                        }
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
                            WifiMetricsProto.ConnectionEvent.HLF_NONE);
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
                            mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);

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
                                    || targetedNetwork.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WAPI_PSK)) {
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
                        handleEapAuthFailure(mTargetNetworkId, message.arg2);
                    }
                    mWifiConfigManager.updateNetworkSelectionStatus(
                            mTargetNetworkId, disableReason);
                    mWifiConfigManager.clearRecentFailureReason(mTargetNetworkId);
                    //If failure occurred while Metrics is tracking a ConnnectionEvent, end it.
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_AUTHENTICATION_FAILURE,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE);
                    mWifiInjector.getWifiLastResortWatchdog()
                            .noteConnectionFailureAndTriggerIfNeeded(
                                    getTargetSsid(), mTargetRoamBSSID,
                                    WifiLastResortWatchdog.FAILURE_CODE_AUTHENTICATION);
                    //+SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
                    if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID
                            && targetedNetwork != null
                            && targetedNetwork.enterpriseConfig != null
                            && mEaptLoggingControllAuthFailure) {
                        mEaptLoggingControllAuthFailure = false;
                        sendEapLogging(targetedNetwork, EAP_LOGGING_STATE_AUTH_FAILURE, "None");
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
                    // psk auth failure, write audit log
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
                case WifiMonitor.WIPS_EVENT: //SEC_PRODUCT_FEATURE_WLAN_CONFIG_TIPS_VERSION 1.3
                    bssid = (String) message.obj;
                    if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                        if (isSameNetwork(mTargetNetworkId, bssid)) {
                            mWifiConfigManager.updateNetworkSelectionStatus(
                                    mTargetNetworkId, WifiConfiguration.NetworkSelectionStatus
                                        .DISABLED_BY_WIPS);
                            break;
                        }
                    }
                    if (mLastConnectedNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                        if (isSameNetwork(mLastConnectedNetworkId, bssid)) {
                            mWifiConfigManager.updateNetworkSelectionStatus(
                                    mLastConnectedNetworkId, WifiConfiguration.NetworkSelectionStatus
                                        .DISABLED_BY_WIPS);
                            break;
                        }
                    }
                    Log.d(TAG, "can't find config, bssid:" + bssid);
                    break;
                case WifiMonitor.BSSID_PRUNED_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO
                    // TODO: Refer to commented lines copied from ASSOCIATION_REJECTION_EVENT below.
                    // mWifiDiagnostics.captureBugReportData(
                    //         WifiDiagnostics.REPORT_REASON_ASSOC_FAILURE);
                    didBlackListBSSID = false;
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
                        didBlackListBSSID = mWifiConnectivityManager.trackBssid(bssid, false,
                            reasonCode, timeRemaining);
                    }
                    // TODO: Refer to commented lines copied from ASSOCIATION_REJECTION_EVENT below.
                    // if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) { //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
                    //     config = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
                    //     if (config != null && config.enterpriseConfig != null) {
                    //         int typeOfEap = -1;
                    //         typeOfEap = config.enterpriseConfig.getEapMethod();
                    //         if (typeOfEap != -1 && config.SSID != null && !TextUtils.isEmpty(config.SSID)) {
                    //             sendEapLogging(config, EAP_LOGGING_STATE_ASSOC_REJECT, "None");
                    //         }
                    //     }
                    // }
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
                case WifiMonitor.SUITABLE_NETWORK_NOT_FOUND_EVENT: //SEC_PRODUCT_FEATURE_WLAN_SUITABLE_NETWORK_NOT_FOUND_EVENT
                    // TODO: Note that most source code was copied from ASSOCIATION_REJECTION_EVENT.
                    Log.d(TAG, "Suitable Network Not Found event:");
                    mWifiConfigManager.updateNetworkSelectionStatus(mTargetNetworkId,
                            WifiConfiguration.NetworkSelectionStatus
                            .DISABLED_SUITABLE_NETWORK_NOT_FOUND);
                    mWifiConfigManager.setRecentFailureAssociationStatus(mTargetNetworkId,
                            31/*temp for WLAN_STATUS_ASSOC_REJECTED_TEMPORARILY*/);
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
                    //+ SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
                    if (state == SupplicantState.ASSOCIATED) {
                        StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                        int mNetworkId = stateChangeResult.networkId;
                        String mBSSID = stateChangeResult.BSSID;

                        config = getCurrentWifiConfiguration();
                        if (config == null && mNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                            config = mWifiConfigManager.getConfiguredNetwork(mNetworkId);
                        }
                        if (config != null && updateBssidWhitelist(config, mBSSID)) {
                            result = mWifiConfigManager.addOrUpdateNetwork(config, Process.SYSTEM_UID);
                            if (!result.isSuccess()) {
                                loge("SUPPLICANT_STATE_CHANGE_EVENT  adding/updating config="
                                        + config.configKey() + " failed for bssidWhitelist");
                            }
                        }
                    }
                    //- SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR

                    // If we have COMPLETED a connection to a BSSID, start doing
                    // DNAv4/DNAv6 -style probing for on-link neighbors of
                    // interest (e.g. routers); harmless if none are configured.
                    if (state == SupplicantState.COMPLETED) {
                        mIpClient.confirmConfiguration();
                        mWifiScoreReport.noteIpCheck();
                            
                        // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                            if (MobileWIPS.getInstance() != null) {
                                MobileWIPS.getInstance().sendEmptyMessage(MWIPSDef.EVENT_CONNECTION_COMPLTED);
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
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                        break;
                    }

                    if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                        mWifiGeofenceManager.removeNetwork(config);
                    }

                    //  we successfully deleted the network config
                    if (netId == mTargetNetworkId || netId == mLastNetworkId) {
                        // Disconnect and let autojoin reselect a new network
                        disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
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
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                    }
                    replyToMessage(message, message.what, ok ? SUCCESS : FAILURE);
                    break;
                case WifiManager.DISABLE_NETWORK:
                    netId = message.arg1;
//++SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                    boolean replyDone = false;
                    if (!mWifiConfigManager.canDisableNetwork(netId, message.sendingUid)) {
                        loge("Failed to disable network");
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
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
                        }
                    } else {
                        loge("Failed to disable network");
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                        if (!replyDone) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                            replyToMessage(message, WifiManager.DISABLE_NETWORK_FAILED,
                                    WifiManager.ERROR);
                        }
                    }
                    break;
                case CMD_DISABLE_EPHEMERAL_NETWORK:
                    config = mWifiConfigManager.disableEphemeralNetwork((String)message.obj);
                    if (config != null) {
                        if (config.networkId == mTargetNetworkId
                                || config.networkId == mLastNetworkId) {
                            // Disconnect and let autojoin reselect a new network
                            disconnectCommand(0, DISCONNECT_REASON_DISABLE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        }
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_IDENTITY:
                    netId = message.arg2;
                    boolean identitySent = false;
                    // For SIM & AKA/AKA' EAP method Only, get identity from ICC
                    if (targetWificonfiguration != null
                            && targetWificonfiguration.networkId == netId
                            && TelephonyUtil.isSimConfig(targetWificonfiguration)) {
                        // Pair<identity, encrypted identity>
                        //+SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        /* Pair<String, String> identityPair =
                                TelephonyUtil.getSimIdentity(getTelephonyManager(),
                                        new TelephonyUtil(), targetWificonfiguration);
                            For dual sim model, getSimIdentity replace to semGetSimIdentity
                            */
                        int simNum = getConfiguredSimNum(targetWificonfiguration);
                        Pair<String, String> identityPair =
                                TelephonyUtil.semGetSimIdentity(getTelephonyManager(),
                                        new TelephonyUtil(), targetWificonfiguration, simNum);
                        //-SEC_PRODUCT_FEATURE_WLAN_EAP_SIM

                        if (identityPair != null && identityPair.first != null) {
                            updateIdentityWithSimNumber(netId, identityPair.first, simNum); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                            mWifiNative.simIdentityResponse(mInterfaceName, netId,
                                    identityPair.first, identityPair.second);
                            identitySent = true;
                        } else {
                            Log.e(TAG, "Unable to retrieve identity from Telephony");
                        }
                    }

                    if (!identitySent) {
                        // Supplicant lacks credentials to connect to that network, hence black list
                        ssid = (String) message.obj;
                        if (targetWificonfiguration != null && ssid != null
                                && targetWificonfiguration.SSID != null
//                                && targetWificonfiguration.SSID.equals("\"" + ssid + "\"")) {
                                && targetWificonfiguration.SSID.equals(ssid)) { //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX for retry popup
                            mWifiConfigManager.updateNetworkSelectionStatus(
                                    targetWificonfiguration.networkId,
                                    WifiConfiguration.NetworkSelectionStatus
                                            .DISABLED_AUTHENTICATION_NO_CREDENTIALS);
                        }
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                                StaEvent.DISCONNECT_GENERIC);
                        //mWifiNative.disconnect(mInterfaceName); //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX for retry popup
                    }
                    break;
                case WifiMonitor.SUP_REQUEST_SIM_AUTH:
                    logd("Received SUP_REQUEST_SIM_AUTH");
                    SimAuthRequestData requestData = (SimAuthRequestData) message.obj;
                    if (requestData != null) {
                        //+SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        int simNum = 1;
                        if (targetWificonfiguration != null && targetWificonfiguration.enterpriseConfig != null) {
                            simNum = getConfiguredSimNum(targetWificonfiguration);
                        }
                        //-SEC_PRODUCT_FEATURE_WLAN_EAP_SIM

                        if (requestData.protocol == WifiEnterpriseConfig.Eap.SIM) {
                            //handleGsmAuthRequest(requestData);
                            handleGsmAuthRequest(requestData, simNum); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        } else if (requestData.protocol == WifiEnterpriseConfig.Eap.AKA
                            || requestData.protocol == WifiEnterpriseConfig.Eap.AKA_PRIME) {
                            //handle3GAuthRequest(requestData);
                            handle3GAuthRequest(requestData, simNum); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                        }
                    } else {
                        loge("Invalid sim auth request");
                    }
                    break;
                case CMD_GET_MATCHING_CONFIG:
                    replyToMessage(message, message.what,
                            mPasspointManager.getMatchingWifiConfig((ScanResult) message.obj));
                    break;
                case CMD_GET_MATCHING_OSU_PROVIDERS:
                    replyToMessage(message, message.what,
                            mPasspointManager.getMatchingOsuProviders((ScanResult) message.obj));
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
                    if (mWifiInfo.getNetworkId() == WifiConfiguration.INVALID_NETWORK_ID) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_NETWORK_EVALUATOR
                        mWifiConnectivityManager.forceConnectivityScan(workSource);
                    } else {
                        logd("reconnect req received despite already connected. skip this req");
                    }
                    break;
                case CMD_REASSOCIATE:
                    lastConnectAttemptTimestamp = mClock.getWallClockMillis();
                    mWifiNative.reassociate(mInterfaceName);
                    break;
                case CMD_RELOAD_TLS_AND_RECONNECT:
                    if (mWifiConfigManager.needsUnlockedKeyStore()) {
                        logd("Reconnecting to give a chance to un-connected TLS networks");
                        mWifiNative.disconnect(mInterfaceName);
                        lastConnectAttemptTimestamp = mClock.getWallClockMillis();
                        mWifiNative.reconnect(mInterfaceName);
                        mEaptLoggingControllConnecting = true; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
                        mEaptLoggingControllAuthFailure = true; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
                    }
                    break;
                case CMD_START_ROAM:
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    return HANDLED;
                case CMD_START_CONNECT:
                    /* connect command coming from auto-join */
                    netId = message.arg1;
                    int uid = message.arg2;
                    bssid = (String) message.obj;

                    synchronized (mWifiReqCountLock) {
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
                    mTargetNetworkId = netId;
                    setTargetBssid(config, bssid);

                    if (mEnableConnectedMacRandomization.get()) {
                        configureRandomizedMacAddress(config);
                    }

                    String currentMacAddress = mWifiNative.getMacAddress(mInterfaceName);
                    mWifiInfo.setMacAddress(currentMacAddress);
                    if (DBG) Log.i(TAG, "Connecting with " + currentMacAddress + " as the mac address");

                    //+SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                    if (TelephonyUtil.isSimConfig(config)) {
                        setPermanentIdentity(config);
                        String identity = config.enterpriseConfig.getIdentity();
                        if (identity == null || identity.isEmpty()) {
                            Log.i(TAG, "CMD_START_CONNECT no Identity is set for EAP SimConfig network");
                            break;
                        }
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                    reportConnectionAttemptStart(config, mTargetRoamBSSID,
                            WifiMetricsProto.ConnectionEvent.ROAM_UNRELATED);

                    if (mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                            && mLastNetworkId != netId) {
                        if (getCurrentState() == mConnectedState) {
                            mDelayDisconnect.checkAndWait(mNetworkInfo);
                        }
                    }
                    if (mWifiNative.connectToNetwork(mInterfaceName, config)) {
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_CMD_START_CONNECT, config);

                        report(ReportIdKey.ID_TRY_TO_CONNECT, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                ReportUtil.getReportDataForTryToConnect(netId, config.SSID,
                                    config.numAssociation,
                                    bssid, false));

                        lastConnectAttemptTimestamp = mClock.getWallClockMillis();
                        targetWificonfiguration = config;
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
                            //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                            if (ENBLE_WLAN_CONFIG_ANALYTICS)
                                setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_TRIGGER_DISCON_CONNECT_OTHER_AP);
                            //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                            transitionTo(mDisconnectingState);
                        }
                    } else {
                        loge("CMD_START_CONNECT Failed to start connection to network " + config);
                        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_RETRY_POPUP
                        mWifiConfigManager.updateNetworkSelectionStatus(mTargetNetworkId,
                                WifiConfiguration.NetworkSelectionStatus.DISABLED_BY_WRONG_PASSWORD);
                        reportConnectionAttemptEnd(
                                WifiMetrics.ConnectionEvent.FAILURE_CONNECT_NETWORK_FAILED,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE);
                        replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                WifiManager.ERROR);
                        break;
                    }
                    break;
                case CMD_REMOVE_APP_CONFIGURATIONS:
                    removedNetworkIds =
                            mWifiConfigManager.removeNetworksForApp((ApplicationInfo) message.obj);
                    if (removedNetworkIds.contains(mTargetNetworkId) ||
                            removedNetworkIds.contains(mLastNetworkId)) {
                        // Disconnect and let autojoin reselect a new network.
                        disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                    }
                    break;
                case CMD_REMOVE_USER_CONFIGURATIONS:
                    removedNetworkIds =
                            mWifiConfigManager.removeNetworksForUser((Integer) message.arg1);
                    if (removedNetworkIds.contains(mTargetNetworkId) ||
                            removedNetworkIds.contains(mLastNetworkId)) {
                        // Disconnect and let autojoin reselect a new network.
                        disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
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
                    if (config == null) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST, SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
                        //if user select saved AP manually, delivery network ID without config
                        config = mWifiConfigManager.getConfiguredNetwork(netId);
                    }
                    boolean hasCredentialChanged = false;
                    boolean isNewNetwork = false; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                    // New network addition.
                    if (config != null) {
                        config.priority = mWifiConfigManager.increaseAndGetPriority(); //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS
                        boolean isAllowed = WifiPolicyCache.getInstance(mContext).isNetworkAllowed(config, false); //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM
                        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM && !isAllowed) {
                            logd("CONNECT_NETWORK isAllowed=" + isAllowed);
                            WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_NETWORK,
                                    false, TAG, AuditEvents.WIFI_CONNECTING_NETWORK + netId + AuditEvents.FAILED);
                            replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                    WifiManager.NOT_AUTHORIZED);
                            break;
                        } else {
                            //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
                            updateBssidWhitelist(config);
                            config.getNetworkSelectionStatus().setNetworkSelectionStatus(
                                    WifiConfiguration.NetworkSelectionStatus.NETWORK_SELECTION_ENABLE);
                            result = mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
                            if (!result.isSuccess()) {
                                loge("CONNECT_NETWORK adding/updating config=" + config + " failed");
                                messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                                replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                        WifiManager.ERROR);
                                break;
                            }
                        }
                        netId = result.getNetworkId();
                        isNewNetwork = result.isNewNetwork(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                        if (Vendor.VZW == mOpBranding || Vendor.SKT == mOpBranding) {
                            if (!TextUtils.isEmpty(config.SSID) //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, VZW, SKT
                                    && checkAndShowSimRemovedDialog(config)) {
                                break;
                            }
                        }
                        hasCredentialChanged = result.hasCredentialChanged();
                    }

                    // >>>WCM>>>
                    mLastManualConnectedNetId = netId;
                    // <<<WCM<<<

                    if (mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID
                            && mLastNetworkId != netId) {
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_START_CONNECT);
                    }

                    if (!connectToUserSelectNetwork(
                            netId, message.sendingUid, hasCredentialChanged)) {
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
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
                    config = (WifiConfiguration) message.obj;
                    // >>>WCM>>>
                    if (config.skipInternetCheck == -1) {
                        logd("SAVE_NETWORK manual connection");
                        mLastManualConnectedNetId = netId;
                    }
                    // <<<WCM<<<
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
                                log("Reconfiguring proxy on connection");
                                mIpClient.setHttpProxy(
                                        getCurrentWifiConfiguration().getHttpProxy());
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
                    //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                    if (ENBLE_WLAN_CONFIG_ANALYTICS)
                        setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_USER_TRIGGER_DISCON_REMOVE_PROFILE);
                    //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
                    if (!deleteNetworkConfigAndSendReply(message, true)) {
                        // Caller was notified of failure, nothing else to do
                        break;
                    }
                    // the network was deleted

                    // >>>WCM>>>
                    if (mLastDisabledCaptivePortalNetId == message.arg1) {
                        // remove captive portal disabled notification
                        log("Disabled CP AP forget");
                        Message notiMsg = new Message();
                        notiMsg.what = CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON;
                        notiMsg.arg1 = 0; // false
                        notiMsg.obj = (Object)"Forget AP";
                        sendMessage(notiMsg);
                        // reset disabled captive portal netid
                        mLastDisabledCaptivePortalNetId = WifiConfiguration.INVALID_NETWORK_ID;
                    }

                    if (mDisabledCaptivePortalList != null && mDisabledCaptivePortalList.containsKey(message.arg1)) {
                        boolean isDisabled = mDisabledCaptivePortalList.get(message.arg1).getDisabled();
                        mDisabledCaptivePortalList.remove(message.arg1);
                        if (!isDisabled && !mDisabledCaptivePortalList.isEmpty()) {
                            removeMessages(CMD_ENABLE_CAPTIVE_PORTAL);
                            for (DisabledCaptivePortal ap : mDisabledCaptivePortalList.values()) {
                                if (!ap.getDisabled()) {
                                    ap.restartEnableCaptivePortal();
                                }
                            }
                        }
                    }
                    if (mWcmChannel != null) mWcmChannel.sendMessage(WifiManager.WWSM_FORGET_NETWORK, message.arg1);
                    // <<<WCM<<<


                    if (netId == mTargetNetworkId || netId == mLastNetworkId) {
                        // Disconnect and let autojoin reselect a new network
                        disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
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
                            && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                            && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                        if (MobileWIPS.getInstance() != null) {
                            MobileWIPS.getInstance().sendEmptyMessage(MWIPSDef.EVENT_ASSOCIATED);
                        }
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    return NOT_HANDLED;
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    if (mVerboseLoggingEnabled) log("Network connection established");
                    mLastNetworkId = message.arg1;
                    mLastConnectedNetworkId = mLastNetworkId;
                    mWifiConfigManager.clearRecentFailureReason(mLastNetworkId);
                    mLastBssid = (String) message.obj;
                    reasonCode = message.arg2;
                    // TODO: This check should not be needed after WifiStateMachinePrime refactor.
                    // Currently, the last connected network configuration is left in
                    // wpa_supplicant, this may result in wpa_supplicant initiating connection
                    // to it after a config store reload. Hence the old network Id lookups may not
                    // work, so disconnect the network and let network selector reselect a new
                    // network.
                    //+SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
                    Log.i(TAG, "resetEleParameters - RoamingState : WifiMonitor.NETWORK_CONNECTION_EVENT");
                    if (mEleBlockRoamConnection) {
                        if (mWcmChannel != null) mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_ELE_BLOCK_VALID);
                        runEleBlockRoamTimer();
                    }
                    resetEleParameters(3, true, true);
                    //-SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
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
                                        config.enterpriseConfig.getEapMethod())) {
                            String anonymousIdentity =
                                    mWifiNative.getEapAnonymousIdentity(mInterfaceName);
                            if (anonymousIdentity != null) {
                                config.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
                            } else {
                                Log.d(TAG, "Failed to get updated anonymous identity"
                                        + " from supplicant, reset it in WifiConfiguration.");
                                config.enterpriseConfig.setAnonymousIdentity(null);
                            }
                            mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
                        }
                        if (mUnstableApController != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                            mUnstableApController.l2Connected(mLastNetworkId);
                        }
                        // >>>WCM>>>
                        if (config.skipInternetCheck == 1) {
                            Log.i(TAG, "skipInternetCheck true by isSkipInternetCheck().");
                            mWifiInfo.setSkipInternetCheck(true);
                            mWifiInfo.setChnKeepConnection(false);
                        } else if (config.skipInternetCheck == 2) { // chn keep connection
                            Log.i(TAG, "chnKeepConnection true");
                            mWifiInfo.setSkipInternetCheck(false);
                            mWifiInfo.setChnKeepConnection(true);
                        } else {
                            mWifiInfo.setSkipInternetCheck(false);
                            mWifiInfo.setChnKeepConnection(false);
                        }
                        // <<<WCM<<<
                        sendNetworkStateChangeBroadcast(mLastBssid);

                        report(ReportIdKey.ID_L2_CONNECTED, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    ReportUtil.getReportDataForL2Connected(mLastNetworkId, mLastBssid));

                        transitionTo(mObtainingIpState);
                    } else {
                        logw("Connected to unknown networkId " + mLastNetworkId
                                + ", disconnecting...");
                        disconnectCommand(0, DISCONNECT_REASON_NO_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
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

                    if (mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                        bssid = (String) message.obj;
                        boolean isSameNetwork = true;
                        ScanDetailCache scanDetailCache =
                                mWifiConfigManager.getScanDetailCacheForNetwork(mLastNetworkId);
                        if (bssid != null && scanDetailCache != null) {
                            ScanResult scanResult = scanDetailCache.getScanResult(bssid);
                            if (scanResult == null) {
                                Log.i(TAG, "disconnected, but not for current network");
                                isSameNetwork = false;
                            }
                        }
                        if (mUnstableApController != null //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                                && /*locallyGenerated*/ message.arg1 == 0
                                && isSameNetwork) {
                            int disconnectReason = message.arg2;
                            if (disconnectReason == 77) {
                                mWifiConfigManager.updateNetworkSelectionStatus(
                                        mLastNetworkId, WifiConfiguration.NetworkSelectionStatus
                                            .DISABLED_BY_WIPS);
                            }
                            boolean isDetected = mUnstableApController.disconnectWithAuthFail(
                                    mLastNetworkId, bssid, mWifiInfo.getRssi(), disconnectReason,
                                    getCurrentState() == mConnectedState);
                            if (isDetected) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                report(ReportIdKey.ID_UNSTABLE_AP_DETECTED,
                                        ReportUtil.getReportDataForUnstableAp(mLastNetworkId));
                            }
                        }
                        if (isSameNetwork && getCurrentState() != mConnectedState) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            report(ReportIdKey.ID_L2_CONNECT_FAIL,
                                        ReportUtil.getReportDataForL2ConnectFail(mLastNetworkId));
                        }
                    }

                    handleNetworkDisconnect();
                    transitionTo(mDisconnectedState);
                    break;
                case CMD_QUERY_OSU_ICON:
                    mPasspointManager.queryPasspointIcon(
                            ((Bundle) message.obj).getLong(EXTRA_OSU_ICON_QUERY_BSSID),
                            ((Bundle) message.obj).getString(EXTRA_OSU_ICON_QUERY_FILENAME));
                    //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE //break;
                case CMD_MATCH_PROVIDER_NETWORK:
                    // TODO(b/31065385): Passpoint config management.
                    replyToMessage(message, message.what, 0);
                    break;
                case CMD_ADD_OR_UPDATE_PASSPOINT_CONFIG:
                    PasspointConfiguration passpointConfig = (PasspointConfiguration) message.obj;
                    if (mPasspointManager.addOrUpdateProvider(passpointConfig, message.arg1)) {
                        String fqdn = passpointConfig.getHomeSp().getFqdn();
                        if (isProviderOwnedNetwork(mTargetNetworkId, fqdn)
                                || isProviderOwnedNetwork(mLastNetworkId, fqdn)) {
                            logd("Disconnect from current network since its provider is updated");
                            disconnectCommand(0, DISCONNECT_REASON_ADD_OR_UPDATE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        }
                        replyToMessage(message, message.what, SUCCESS);
                    } else {
                        replyToMessage(message, message.what, FAILURE);
                    }
                    break;
                case CMD_REMOVE_PASSPOINT_CONFIG:
                    String fqdn = (String) message.obj;
                    if (mPasspointManager.removeProvider(fqdn)) {
                        boolean needTosendDisconnectMsg = false; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                        if (isProviderOwnedNetwork(mTargetNetworkId, fqdn)
                                || isProviderOwnedNetwork(mLastNetworkId, fqdn)) {
                            logd("Disconnect from current network since its provider is removed");
                            needTosendDisconnectMsg = true;
                        }
                        //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20-[
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
                        if (needTosendDisconnectMsg) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20-]
                            disconnectCommand(0, DISCONNECT_REASON_REMOVE_NETWORK); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        }
                        replyToMessage(message, message.what, SUCCESS);
                    } else {
                        replyToMessage(message, message.what, FAILURE);
                    }
                    break;
                case CMD_ENABLE_P2P:
                    p2pSendMessage(WifiStateMachine.CMD_ENABLE_P2P);
                    break;
                // >>>WCM>>>
                case CMD_DISABLED_CAPTIVE_PORTAL_SCAN_OUT:
                    int id = message.arg1;
                    log("Disabled captive portal scan out: " + id);
                    Message msg = new Message();
                    msg.what = CMD_ENABLE_CAPTIVE_PORTAL;
                    msg.arg1 = id;
                    sendMessageDelayed(msg, ENABLE_DISABLED_CAPTIVE_PORTAL_MS);

                    if (mDisabledCaptivePortalList.containsKey(id)) {
                        mDisabledCaptivePortalList.get(id).setDisabled(false);
                    }
                    if (mLastDisabledCaptivePortalNetId == id) {
                        // remove captive portal disabled notification
                        Message notiMsg = new Message();
                        notiMsg.what = CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON;
                        notiMsg.arg1 = 0; // false
                        notiMsg.obj = (Object)"OOR";
                        sendMessage(notiMsg);
                        mLastDisabledCaptivePortalNetId = WifiConfiguration.INVALID_NETWORK_ID;
                    }
                    break;
                case CMD_ENABLE_CAPTIVE_PORTAL:
                    int enableId = message.arg1;
                    log("Enable captive portal: " + enableId);
                    if (mDisabledCaptivePortalList.containsKey(enableId)) {
                        mDisabledCaptivePortalList.remove(enableId);
                    }
                    mWifiConfigManager.updateNetworkSelectionStatus(enableId,
                            WifiConfiguration.NetworkSelectionStatus.NETWORK_SELECTION_ENABLE);

                    if (mLastDisabledCaptivePortalNetId == enableId) {
                        // remove captive portal disabled notification
                        Message notiMsg = new Message();
                        notiMsg.what = CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON;
                        notiMsg.arg1 = 0; // false
                        notiMsg.obj = (Object)"OOR";
                        sendMessage(notiMsg);
                        mLastDisabledCaptivePortalNetId = WifiConfiguration.INVALID_NETWORK_ID;
                    }

                    if(DBGMHS && message.what == CMD_UNWANTED_NETWORK) {
                        Intent intentUnwanted;
                        intentUnwanted = new Intent("com.sec.android.connection_action");
                        intentUnwanted.putExtra("com.sec.android.connection_action", "unwanted_action");
                        sendBroadcastFromWifiStateMachine(intentUnwanted);
                    }
                    break;
                case WifiConnectivityMonitor.QC_UPDATE_SKIP_INTERNET_CHECK_VALUE:
                    mWifiConfigManager.updateSkipInternetCheck(message.arg1, message.arg2);
                    if (message.arg2 == 2) { // chn keep connection
                        mWifiInfo.setChnKeepConnection(true);
                    } else {
                        mWifiInfo.setSkipInternetCheck(message.arg2 == 1 ? true : false);
                    }
                    break;
                // <<<WCM<<<
                case CMD_GET_ALL_MATCHING_CONFIGS:
                    replyToMessage(message, message.what,
                            mPasspointManager.getAllMatchingWifiConfigs((ScanResult) message.obj));
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
                    logd("return HANDLED at ConnectModeState:processMessage:CMD_GET_LINK_LAYER_STATS"); //SEC_PRODUCT_FEATURE_WLAN_DEBUG_HIDL
                    break;
                case CMD_RESET_SIM_NETWORKS:
                    log("resetting EAP-SIM/AKA/AKA' networks since SIM was changed");
                    mWifiConfigManager.resetSimNetworks(message.arg1 == 1);
                    if (mUnstableApController != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                        mUnstableApController.setSimCardState(
                                TelephonyUtil.isSimCardReady(getTelephonyManager()));
                    }
                    //+SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                    if (CSC_WIFI_SUPPORT_VZW_EAP_AKA) {
                        config = getCurrentWifiConfiguration();
                        if (config != null && !TextUtils.isEmpty(config.SSID)
                                && config.semIsVendorSpecificSsid) {
                            if (message.arg1 == 0) { //sim absent
                                SemWifiFrameworkUxUtils.showWarningDialog(mContext,
                                        SemWifiFrameworkUxUtils.WARN_SIM_REMOVED_WHEN_CONNECTED,
                                        new String[] {StringUtil.removeDoubleQuotes(config.SSID)});
                            }
                            mWifiNative.pmksaClearInScanAlwaysMode(mInterfaceName); // WifiNative.WIFI_NATIVE_CMD_PMKSA_CLEAR_IN_SCAN_ALWAYS_MODE
                        } else {
                            mWifiNative.pmksaClearExceptCurrentNetwork(mInterfaceName); // WifiNative.WIFI_NATIVE_CMD_PMKSA_CLEAR_EXCEPT_CURRENT_NETWORK
                        }
                    }
                    if (message.arg1 == 0) { //sim absent
                        removePasspointNetworkForSimAbsent(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20
                        mWifiNative.simAbsent(mInterfaceName);
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
                    updateHotspot20VendorSimState(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20

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
                        mNetworkAgent.onPacketKeepaliveEvent(slot, ret);
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
                    if (mWcmChannel != null) mWcmChannel.sendMessage(CMD_SCAN_RESULT_AVAILABLE_WSM);
                    // <<<WCM<<<
                    break;
                case CMD_REQUEST_FW_BIGDATA_PARAM: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_FWLOG_CONTROL) {
                        mWifiNative.requestFwBigDataParams(mInterfaceName, message.arg1  , message.arg2, 0);
                        mWifiNative.saveFwDump(mInterfaceName);
                    }
                    break;
                default:
                    return NOT_HANDLED;
            }
            return HANDLED;
        }
    }

    public void updateCapabilities() {
        updateCapabilities(getCurrentWifiConfiguration());
    }

    private void updateCapabilities(WifiConfiguration config) {
        if (mNetworkAgent == null) {
            return;
        }

        final NetworkCapabilities result = new NetworkCapabilities(mDfltNetworkCapabilities);

        if (mWifiInfo != null && !mWifiInfo.isEphemeral()) {
            result.addCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
        } else {
            result.removeCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED);
        }

        if (mWifiInfo != null && !WifiConfiguration.isMetered(config, mWifiInfo)) {
            result.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        } else {
            result.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        }

        if (mWifiInfo != null && mWifiInfo.getRssi() != WifiInfo.INVALID_RSSI) {
            result.setSignalStrength(mWifiInfo.getRssi());
        } else {
            result.setSignalStrength(NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED);
        }

        if (mWifiInfo != null && !mWifiInfo.getSSID().equals(WifiSsid.NONE)) {
            result.setSSID(mWifiInfo.getSSID());
        } else {
            result.setSSID(null);
        }

        mNetworkAgent.sendNetworkCapabilities(result);
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
                        getTelephonyManager().resetCarrierKeysForImsiEncryption();
                    }
                    if (CSC_WIFI_ERRORCODE || "KTT".equals(CSC_CONFIG_EAP_AUTHMSG_POLICY)) { //TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE
                        processCodeForEap(errorCode);
                    }
                    break;

                default:
                    // Do Nothing
            }
        }
    }

    private class WifiNetworkAgent extends NetworkAgent {
        public WifiNetworkAgent(Looper l, Context c, String TAG, NetworkInfo ni,
                NetworkCapabilities nc, LinkProperties lp, int score, NetworkMisc misc) {
            super(l, c, TAG, ni, nc, lp, score, misc);
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
                duplicatedIpChecker(); //DUPLICATED_IP_USING_DETECTION
            } else if (status == NetworkAgent.VALID_NETWORK) {
                mInitialQcExtraInfo = -1; // WCM
                if (mVerboseLoggingEnabled) {
                    log("WifiNetworkAgent -> Wifi networkStatus valid, score= "
                            + Integer.toString(mWifiInfo.score));
                }
                if (mWcmChannel != null) mWcmChannel.sendMessage(CMD_NETWORK_STATUS_VALID);
                mWifiMetrics.logStaEvent(StaEvent.TYPE_NETWORK_AGENT_VALID_NETWORK);
                doNetworkStatus(status);
            }
        }

        @Override
        protected void saveAcceptUnvalidated(boolean accept) {
            if (this != mNetworkAgent) return;
            WifiStateMachine.this.sendMessage(CMD_ACCEPT_UNVALIDATED, accept ? 1 : 0);
        }

        @Override
        protected void startPacketKeepalive(Message msg) {
            WifiStateMachine.this.sendMessage(
                    CMD_START_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        @Override
        protected void stopPacketKeepalive(Message msg) {
            WifiStateMachine.this.sendMessage(
                    CMD_STOP_IP_PACKET_OFFLOAD, msg.arg1, msg.arg2, msg.obj);
        }

        @Override
        protected void setSignalStrengthThresholds(int[] thresholds) {
            // 0. If there are no thresholds, or if the thresholds are invalid, stop RSSI monitoring.
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
                    WifiStateMachine.this.sendMessage(CMD_STOP_RSSI_MONITORING_OFFLOAD,
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
                    WifiStateMachine.this.sendMessage(CMD_STOP_RSSI_MONITORING_OFFLOAD,
                            mWifiInfo.getRssi());
                    return;
                }
            }
            // TODO: Do we quash rssi values in this sorted array which are very close?
            mRssiRanges = rssiRange;
            WifiStateMachine.this.sendMessage(CMD_START_RSSI_MONITORING_OFFLOAD,
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
    
    void checkDuplcatedIp(int timedelay) { // DUPLICATED_IP_USING_DETECTION
        removeMessages(CMD_CHECK_DUPLICATED_IP);
        sendMessageDelayed(CMD_CHECK_DUPLICATED_IP, timedelay);
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

        if (imsi == null || imsi.isEmpty())
            return "";

        if (eapMethod == WifiEnterpriseConfig.Eap.SIM)
            prefix = "1";
        else if (eapMethod == WifiEnterpriseConfig.Eap.AKA)
            prefix = "0";
        else if (eapMethod == WifiEnterpriseConfig.Eap.AKA_PRIME)
            prefix = "6";
        else  // not a valide EapMethod
            return "";

        /* extract mcc & mnc from mccMnc */
        if (mccMnc != null && !mccMnc.isEmpty()) {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2)
                mnc = "0" + mnc;
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
                sendMessage(CMD_RSSI_POLL, mRssiPollToken, 0);
            }
            if (mNetworkAgent != null) {
                loge("Have NetworkAgent when entering L2Connected");
                setNetworkDetailedState(DetailedState.DISCONNECTED);
                // >>>WCM>>>
                /* SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK */
                if (mWcmChannel != null) mWcmChannel.sendMessage(WifiManager.WWSM_NETWORK_DISCONNECTED);
                // <<<WCM<<<
            }
            setNetworkDetailedState(DetailedState.CONNECTING);

            final NetworkCapabilities nc;
            if (mWifiInfo != null && !mWifiInfo.getSSID().equals(WifiSsid.NONE)) {
                nc = new NetworkCapabilities(mNetworkCapabilitiesFilter);
                nc.setSSID(mWifiInfo.getSSID());
            } else {
                nc = mNetworkCapabilitiesFilter;
            }
            mNetworkAgent = new WifiNetworkAgent(getHandler().getLooper(), mContext,
                    "WifiNetworkAgent", mNetworkInfo, nc, mLinkProperties, 60, mNetworkMisc);

            // We must clear the config BSSID, as the wifi chipset may decide to roam
            // from this point on and having the BSSID specified in the network block would
            // cause the roam to faile and the device to disconnect
            clearTargetBssid("L2ConnectedState");
            mCountryCode.setReadyForChange(false);
            mWifiMetrics.setWifiState(WifiMetricsProto.WifiLog.WIFI_ASSOCIATED);
        }

        @Override
        public void exit() {
            mIpClient.stop();

            // This is handled by receiving a NETWORK_DISCONNECTION_EVENT in ConnectModeState
            // Bug: 15347363
            // For paranoia's sake, call handleNetworkDisconnect
            // only if BSSID is null or last networkId
            // is not invalid.
            if (mVerboseLoggingEnabled) {
                StringBuilder sb = new StringBuilder();
                sb.append("leaving L2ConnectedState state nid=" + Integer.toString(mLastNetworkId));
                if (mLastBssid !=null) {
                    sb.append(" ").append(mLastBssid);
                }
            }
            if (mLastBssid != null || mLastNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                handleNetworkDisconnect();
            }
            mCountryCode.setReadyForChange(true);
            mWifiMetrics.setWifiState(WifiMetricsProto.WifiLog.WIFI_DISCONNECTED);
            mWifiStateTracker.updateState(WifiStateTracker.DISCONNECTED);
        }

        @Override
        public boolean processMessage(Message message) {
            logStateAndMessage(message, this);

            switch (message.what) {
                case CMD_CHECK_DHCP_AFTER_ROAMING:
                    if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS ) {
                        if (semLostProvisioningAfterRoaming) {
                            if (DBG) log("NUD FAILED After Roaming, Restart DHCP");
                            mIpClient.stop();
                            setTcpBufferAndProxySettingsForIpClient();
                            final IpClient.ProvisioningConfiguration prov;
                            prov = IpClient.buildProvisioningConfiguration()
                                        .withPreDhcpAction()
                                        .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                                        .build();
                            mIpClient.startProvisioning(prov);
                            notifyWcmDhcpSession("start");  // WCM - Head start for DHCP initiation (overlap roam session and DHCP session for WCM)
                        }
                        semStartNudProbe = false;
                        semLostProvisioningAfterRoaming = false;
                        notifyWcmRoamSession("complete");   // WCM - Send dealyed Roam Complete
                    }
                    break;
                case CMD_CHECK_DEFAULT_GWMACADDRESS:
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS ) {
                        if (mNoArpResponseAfterRoaming) {
                            mNoArpResponseAfterRoaming = false;
                            CheckIfDefaultGatewaySame("ff:ff:ff:ff:ff:ff");
                            mIpClient.updateDefaultRouteMacAddress(mLastBssid,mLastArpResultsForRoamingDhcp, true);
                            mLastArpResultsForRoamingDhcp = null;
                        } else {
                            mIpClient.updateDefaultRouteMacAddress(mLastBssid,updateDefaultRouteMacAddress(1000), true);
                        }
                    }
                    break;
                case DhcpClient.CMD_PRE_DHCP_ACTION:
                    handlePreDhcpSetup();
                    break;
                case DhcpClient.CMD_PRE_DHCP_ACTION_COMPLETE:
                    mIpClient.completedPreDhcpAction();
                    break;
                case DhcpClient.CMD_POST_DHCP_ACTION:
                    if (message.arg1 == DhcpClient.DHCP_FAILURE) { // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_DHCP_FAIL);
                    }
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
                    // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                            && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                            && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                        if (MobileWIPS.getInstance() != null) {
                            MobileWIPS.getInstance().sendEmptyMessage(MWIPSDef.EVENT_PROVISION_COMPLTED);
                        }
                    }
                    // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    break;
                }
                case CMD_IPV4_PROVISIONING_FAILURE: {
                    handleIPv4Failure();
                    break;
                }
                case CMD_IP_CONFIGURATION_SUCCESSFUL:
                    handleSuccessfulIpConfiguration();
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
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_NONE,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE);
                    // >>>WCM>>>
                    if (getCurrentState() == mObtainingIpState) {
                        setStopScanForWCM(true);
                    }
                    // <<<WCM<<<
                    if (getCurrentWifiConfiguration() == null) {
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_NO_NETWORK);
                        // The current config may have been removed while we were connecting,
                        // trigger a disconnect to clear up state.
                        mWifiNative.disconnect(mInterfaceName);
                        transitionTo(mDisconnectingState);
                    } else {
                        sendConnectedState();
                        transitionTo(mConnectedState);
                    }
                    break;
                case CMD_IP_CONFIGURATION_LOST:
                    // Get Link layer stats so that we get fresh tx packet counters.
                    getWifiLinkLayerStats();
                    handleIpConfigurationLost();
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_DHCP,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE);
                    notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                            DISCONNECT_REASON_DHCP_FAIL);
                    transitionTo(mDisconnectingState);
                    break;
                case CMD_SEC_IP_CONFIGURATION_LOST: //SEC_PRODUCT_FEATURE_VALIDATION_CHECK_AFTER_PROVISIONING_FAIL
                    if (mWcmChannel != null) {
                        mWcmChannel.sendMessage(CMD_PROVISIONING_FAIL);
                    }
                    break;
                case CMD_IP_REACHABILITY_LOST:
                    if (mVerboseLoggingEnabled && message.obj != null) log((String) message.obj);
                    if (mIpReachabilityDisconnectEnabled) {
                        handleIpReachabilityLost();
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_DHCP_FAIL);
                        transitionTo(mDisconnectingState);
                    } else {
                        logd("CMD_IP_REACHABILITY_LOST but disconnect disabled -- ignore");
                        if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS ) {
                            if (semStartNudProbe) {
                                semLostProvisioningAfterRoaming = true;
                            }
                        }
//++SEC_PRODUCT_FEATURE_VALIDATION_CHECK_AFTER_PROVISIONING_FAIL
                        if (mWcmChannel != null) {
                            mWcmChannel.sendMessage(CMD_REACHABILITY_LOST);
                        }
//--SEC_PRODUCT_FEATURE_VALIDATION_CHECK_AFTER_PROVISIONING_FAIL
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
                case CMD_SET_OPERATIONAL_MODE:
                    if (message.arg1 != CONNECT_MODE) {
                        deferMessage(message);
                    }
                    break;
                    /* Ignore connection to same network */
                case WifiManager.CONNECT_NETWORK:
                    int netId = message.arg1;
                    if (mWifiInfo.getNetworkId() == netId) {
                        replyToMessage(message, WifiManager.CONNECT_NETWORK_SUCCEEDED);
                        break;
                    }
                    return NOT_HANDLED;
                case WifiMonitor.NETWORK_CONNECTION_EVENT:
                    if (DBG) log("dongle roaming established");
                    boolean wcmDoNotSendRoamComplete = false;   // WCM
                    //+SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
                    if (mEleBlockRoamConnection) {
                        if (mWcmChannel != null) mWcmChannel.sendMessage(WifiConnectivityMonitor.CMD_ELE_BLOCK_VALID);
                        runEleBlockRoamTimer();
                    } else {
                        mEleRoamingStationaryCnt = 5;
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
                    //+SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
                    preventGoodQualityScoreCheck();
                    //-SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
                    if (mIWCMonitorChannel != null) {
                        mIWCMonitorChannel.sendMessage(IWCMonitor.DONGLE_ROAMING_ESTABLISHED);
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_INTELLIGENT_WIFI_CONNECTION
                    String prevBssid = mLastBssid; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
                    mWifiInfo.setBSSID((String) message.obj);
                    if (message.arg1 != WifiConfiguration.INVALID_NETWORK_ID) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_WPS
                        mLastNetworkId = message.arg1;
                        mLastConnectedNetworkId = mLastNetworkId;
                        mWifiInfo.setNetworkId(mLastNetworkId);
                    }
                    mWifiInfo.setMacAddress(mWifiNative.getMacAddress(mInterfaceName));
                    if(!mLastBssid.equals(message.obj)) {
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

//+SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP , SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
                        int tmpDhcpRenewAfterRoamingMode = getDhcpRenewAfterRoamingMode();
                        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
                        boolean isUsingStaticIp = false;
                        if (currentConfig != null) {
                            isUsingStaticIp = (currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC);
                        }

                        if (isUsingStaticIp) {
                            if (DBG) log("Static ip - skip renew");
                            tmpDhcpRenewAfterRoamingMode = -1;
                        }

                        if (tmpDhcpRenewAfterRoamingMode == 0 || tmpDhcpRenewAfterRoamingMode == 1) {
                            if (checkIfForceRestartDhcp()) {
                                mIpClient.stop();
                                setTcpBufferAndProxySettingsForIpClient();
                                final IpClient.ProvisioningConfiguration prov;
                                if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS ) {
                                    prov = IpClient.buildProvisioningConfiguration()
                                                .withPreDhcpAction()
                                                .withoutIpReachabilityMonitor()
                                                .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                                                .build();
                                } else {
                                    prov = IpClient.buildProvisioningConfiguration()
                                                .withPreDhcpAction()
                                                .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                                                .build();
                                }
                                mIpClient.startProvisioning(prov);
                            } else {
                                if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS ) {
                                    String defaultGwMac = updateDefaultRouteMacAddress(1000);
                                    if (CheckIfDefaultGatewaySame(defaultGwMac)) {
                                        mNoArpResponseAfterRoaming = false;
                                        log("Same Network - do not need to dhcp again");
                                        mIpClient.saveDhcpResult(mLastBssid, mIpClient.getDhcpResult(prevBssid));
                                        mIpClient.updateDefaultRouteMacAddress(mLastBssid, defaultGwMac, false);

                                        // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                                        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                                                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                                                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                                            if (MobileWIPS.getInstance() != null) {
                                                MobileWIPS.getInstance()
                                                        .sendEmptyMessage(MWIPSDef.EVENT_ROAMING_SAME_NETWORK);
                                            }
                                        }
                                        // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                                    } else {
                                        DhcpResults results = mIpClient.getDhcpResult(mLastBssid);
                                        mNoArpResponseAfterRoaming = true;
                                        mIpClient.stop();
                                        setTcpBufferAndProxySettingsForIpClient();
                                        final IpClient.ProvisioningConfiguration prov;
                                        if (results != null) {
                                            log("remembered bssid - Try DHCP Request using previous info");
                                            prov = IpClient.buildProvisioningConfiguration()
                                                        .withPreDhcpAction()
                                                        .withoutIpReachabilityMonitor()
                                                        .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                                                        .withDhcpRequestFirst(mLastBssid)
                                                        .build();
                                        } else {
                                            log("DHCP Restart - No saved bssid");
                                            prov = IpClient.buildProvisioningConfiguration()
                                                        .withPreDhcpAction()
                                                        .withoutIpReachabilityMonitor()
                                                        .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                                                        .build();
                                        }
                                        mIpClient.startProvisioning(prov);
                                        mIpClient.setRequestAfterRoamingArpFail(true);
                                        notifyWcmDhcpSession("start");  // WCM - Head start for DHCP initiation (overlap roam session and DHCP session for WCM)
                                    }
                                } else {
                                    if (DBG) log("NUD_PROBE after dongle roaming");
                                    semStartNudProbe = true;
                                    removeMessages(CMD_CHECK_DHCP_AFTER_ROAMING);
                                    sendMessageDelayed(CMD_CHECK_DHCP_AFTER_ROAMING, 4 * 1000);
                                    wcmDoNotSendRoamComplete = true;    // WCM
                                }
                            }
                        } else {
                            if (DBG) log("Skip Dhcp after Roaming tmpDhcpRenewAfterRoamingMode : " + tmpDhcpRenewAfterRoamingMode);

                            // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                            if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                                    && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                                    && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                                if (MobileWIPS.getInstance() != null) {
                                    MobileWIPS.getInstance().sendEmptyMessage(MWIPSDef.EVENT_ROAMING_SAME_NETWORK);
                                }
                            }
                            // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                        }

                        report(ReportIdKey.ID_ROAMING_DHCP_TRIGGER, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                ReportUtil.getReportDataForRoamingDhcpStart(
                                    mWifiInfo.getSSID(),
                                    mLastBssid,
                                    tmpDhcpRenewAfterRoamingMode,
                                    semStartNudProbe));
                    }

                    // Smart network switch - Roaming complete
                    if (!wcmDoNotSendRoamComplete) {
                        notifyWcmRoamSession("complete");
                    }
                    break;
                case CMD_RSSI_POLL:
                    if (message.arg1 == mRssiPollToken) {
                        getWifiLinkLayerStats();
                        // Get Info and continue polling
                        fetchRssiLinkSpeedAndFrequencyNative();
                        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CUSTOMIZATION_SDK) {
                            knoxAutoSwitchPolicy(mWifiInfo.getRssi());
                        }
                        // Send the update score to network agent.
                        mWifiScoreReport.calculateAndReportScore(
                                mWifiInfo, mNetworkAgent, mWifiMetrics);
                        //+SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
                        if (mScoreQualityCheckMode != SCORE_QUALITY_CHECK_STATE_NONE) {
                            try {
                                checkScoreBasedQuality(mWifiScoreReport.getCurrentS2Score());
                            } catch (Exception e) {
                                Log.e(TAG, "checkScoreBasedQuality exception happend : "+ e.toString());
                            }
                        }
                        //-SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK

                        if (mWifiScoreReport.shouldCheckIpLayer()) {
                            mIpClient.confirmConfiguration();
                            mWifiScoreReport.noteIpCheck();
                        }
                        sendMessageDelayed(obtainMessage(CMD_RSSI_POLL, mRssiPollToken, 0),
                                mPollRssiIntervalMsecs);
                        if (mVerboseLoggingEnabled) sendRssiChangeBroadcast(mWifiInfo.getRssi());
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

                    //+SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
                    if (mEleEnableMobileRssiPolling) {
                        boolean goPolling = false;
                        if (mElePollingMode == ELE_POLLING_DEFAULT) {
                            goPolling = true;
                        } else {
                            if (mElePollingSkip) {
                                mElePollingSkip = false;
                            } else {
                                mElePollingSkip = true;
                                goPolling = true;
                            }
                        }

                        if (goPolling) {
                            try {
                                CheckEleEnvironment(getTelephonyManager().getSignalStrength().getDbm(), getWifiEleBeaconStats(), getCurrentRssi());
                            } catch (Exception e) {
                                Log.e(TAG, "CheckEleEnvironment exception happend : "+ e.toString());
                            }
                        }
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
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
                    notifyWcmRoamSession("start"); // Smart network switch
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
                            notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    DISCONNECT_REASON_SIM_REMOVED);
                            mWifiNative.disconnect(mInterfaceName);
                            transitionTo(mDisconnectingState);
                        }
                    }
                    /* allow parent state to reset data for other networks */
                    return NOT_HANDLED;

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
                    return NOT_HANDLED;
            }

            return HANDLED;
        }
    }

    class ObtainingIpState extends State {
        @Override
        public void enter() {
            if (mScanRequestProxy != null) { //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX
                mScanRequestProxy.setScanningEnabled(false, "START_DHCP");
            }
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

            final IpClient.ProvisioningConfiguration prov;
            if (!isUsingStaticIp) {
                IpClient.ProvisioningConfiguration.Builder builder =
                        IpClient.buildProvisioningConfiguration();
                builder.withPreDhcpAction();
                builder.withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName));
                if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS ) {
                    builder.withoutIpReachabilityMonitor();
                }
                if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS  && mLastBssid != null) {
                    builder.withDhcpRequestFirst(mLastBssid);
                }
                prov = builder.build();
            } else {
                StaticIpConfiguration staticIpConfig = currentConfig.getStaticIpConfiguration();
                IpClient.ProvisioningConfiguration.Builder builder =
                        IpClient.buildProvisioningConfiguration();
                builder.withStaticConfiguration(staticIpConfig);
                builder.withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName));
                if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS ) {
                    builder.withoutIpReachabilityMonitor();
                }
                prov = builder.build();
            }
            mIpClient.startProvisioning(prov);
            // Get Link layer stats so as we get fresh tx packet counters
            getWifiLinkLayerStats();
        }

        @Override
        public boolean processMessage(Message message) {
            logStateAndMessage(message, this);

            switch(message.what) {
                case CMD_START_CONNECT:
                    //+SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
                    String bssid = (String) message.obj;
                    if (SUPPLICANT_BSSID_ANY.equals(bssid)) {
                        return NOT_HANDLED;
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_CONNECTIVITY_MANAGER
                case CMD_START_ROAM:
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
                    break;
                case WifiManager.SAVE_NETWORK:
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DEFERRED;
                    deferMessage(message);
                    break;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_NETWORK_DISCONNECTION,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE);
                    return NOT_HANDLED;
                case CMD_SET_HIGH_PERF_MODE:
                    messageHandlingStatus = MESSAGE_HANDLING_STATUS_DEFERRED;
                    deferMessage(message);
                    break;
                default:
                    return NOT_HANDLED;
            }
            return HANDLED;
        }

        @Override
        public void exit() {
            if (mScanRequestProxy != null) { //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX
                mScanRequestProxy.setScanningEnabled(true, "STOP_DHCP");
            }
        }
    }

    /**
     * Helper function to check if we need to invoke
     * {@link NetworkAgent#explicitlySelected(boolean)} to indicate that we connected to a network
     * which the user just chose
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
        // If this network was explicitly selected by the user, evaluate whether to call
        // explicitlySelected() so the system can treat it appropriately.
        WifiConfiguration config = getCurrentWifiConfiguration();
        if (shouldEvaluateWhetherToSendExplicitlySelected(config)) {
            boolean prompt =
                    mWifiPermissionsUtil.checkNetworkSettingsPermission(config.lastConnectUid);
            if (mVerboseLoggingEnabled) {
                log("Network selected by UID " + config.lastConnectUid + " prompt=" + prompt);
            }
            if (prompt) {
                // Selected by the user via Settings or QuickSettings. If this network has Internet
                // access, switch to it. Otherwise, switch to it only if the user confirms that they
                // really want to switch, or has already confirmed and selected "Don't ask again".
                if (mVerboseLoggingEnabled) {
                    log("explictlySelected acceptUnvalidated=" + config.noInternetAccessExpected);
                }
                if (mNetworkAgent != null) {
                    mNetworkAgent.explicitlySelected(config.noInternetAccessExpected);
                }
            }
        }

        // >>>WCM>>>
        boolean isManualConnection = (config != null && (mLastManualConnectedNetId == config.networkId)) ? true : false;

        if (isManualConnection) {
            Log.d(TAG, "sendConnectedState - setManualConnection: true");
            mWifiInfo.setManualConnection(isManualConnection);
        } else if (config != null && mLastWpsNetId == config.networkId) {
            Log.d(TAG, "sendConnectedState - WPS connection setManualConnection: true");
            mWifiInfo.setManualConnection(true);
        } else {
            Log.d(TAG, "sendConnectedState - setManualConnection: false");
            mWifiInfo.setManualConnection(false);
        }
        mLastManualConnectedNetId = WifiConfiguration.INVALID_NETWORK_ID;
        mLastWpsNetId = WifiConfiguration.INVALID_NETWORK_ID;
        // <<<WCM<<<
        setNetworkDetailedState(DetailedState.CONNECTED);
        sendNetworkStateChangeBroadcast(mLastBssid);

        // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
            if (MobileWIPS.getInstance() != null) {
                MobileWIPS.getInstance().sendEmptyMessage(MWIPSDef.EVENT_CONNECTED);
            }
        }
        // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
    }

    class RoamingState extends State {
        boolean mAssociated;
        @Override
        public void enter() {
            if (mVerboseLoggingEnabled) {
                log("RoamingState Enter"
                        + " mScreenOn=" + mScreenOn );
            }

            // Make sure we disconnect if roaming fails
            roamWatchdogCount++;
            logd("Start Roam Watchdog " + roamWatchdogCount);
            sendMessageDelayed(obtainMessage(CMD_ROAM_WATCHDOG_TIMER,
                    roamWatchdogCount, 0), ROAM_GUARD_TIMER_MSEC);
            mAssociated = false;

            report(ReportIdKey.ID_ROAMING_TRIGGER, //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                        ReportUtil.getReportDataForRoamingEnter(
                            "framework",
                            mWifiInfo.getSSID(),
                            mWifiInfo.getBSSID()));

            mBigDataManager.addOrUpdateValue( // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    WifiBigDataLogManager.LOGGING_TYPE_ROAM_TRIGGER, 1);

            notifyWcmRoamSession("start"); // Smart network switch
        }
        @Override
        public boolean processMessage(Message message) {
            logStateAndMessage(message, this);
            WifiConfiguration config;
            switch (message.what) {
                case CMD_IP_CONFIGURATION_LOST:
                    config = getCurrentWifiConfiguration();
                    if (config != null) {
                        mWifiDiagnostics.captureBugReportData(
                                WifiDiagnostics.REPORT_REASON_AUTOROAM_FAILURE);
                    }
                    return NOT_HANDLED;
                case CMD_UNWANTED_NETWORK:
                    if (mVerboseLoggingEnabled) {
                        log("Roaming and CS doesnt want the network -> ignore");
                    }
                    return HANDLED;
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
                                    + stateChangeResult.toString() );
                        }
                        if (stateChangeResult.BSSID != null
                                && stateChangeResult.BSSID.equals(mTargetRoamBSSID)) {
                            notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    DISCONNECT_REASON_ROAM_FAIL);
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
                    if (roamWatchdogCount == message.arg1) {
                        if (mVerboseLoggingEnabled) log("roaming watchdog! -> disconnect");
                        mWifiMetrics.endConnectionEvent(
                                WifiMetrics.ConnectionEvent.FAILURE_ROAM_TIMEOUT,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE);
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
                    //+SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
                    Log.i(TAG, "resetEleParameters - ConnectModeState : WifiMonitor.NETWORK_CONNECTION_EVENT");
                    resetEleParameters(3, true, true);
                    //-SEC_PRODUCT_FEATURE_WLAN_ELE_CHECK
                    //+SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
                    preventGoodQualityScoreCheck();
                    //-SEC_PRODUCT_FEATURE_WLAN_SCORE_QUALITY_CHECK
                    if (mAssociated) {
                        if (mVerboseLoggingEnabled) {
                            log("roaming and Network connection established");
                        }
                        mLastNetworkId = message.arg1;
                        mLastConnectedNetworkId = mLastNetworkId;
                        mLastBssid = (String) message.obj;
                        mWifiInfo.setBSSID(mLastBssid);
                        mWifiInfo.setNetworkId(mLastNetworkId);
                        int reasonCode = message.arg2;
                        mWifiConnectivityManager.trackBssid(mLastBssid, true, reasonCode);
                        sendNetworkStateChangeBroadcast(mLastBssid);

                        // Successful framework roam! (probably)
                        reportConnectionAttemptEnd(
                                WifiMetrics.ConnectionEvent.FAILURE_NONE,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE);

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
                        transitionTo(mConnectedState);
                    } else {
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_DISCARD;
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
                    return NOT_HANDLED;
            }
            return HANDLED;
        }

        @Override
        public void exit() {
            logd("WifiStateMachine: Leaving Roaming state");

            mBigDataManager.addOrUpdateValue( // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_LOGGING
                    WifiBigDataLogManager.LOGGING_TYPE_ROAM_TRIGGER, 0);

            notifyWcmRoamSession("complete"); // Smart network switch
        }
    }

    class ConnectedState extends State {
        private WifiConfiguration mCurrentConfig; //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20, SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE

        @Override
        public void enter() {
            // TODO: b/64349637 Investigate getting default router IP/MAC address info from
            // IpClient
            //updateDefaultRouteMacAddress(1000);
            if (mVerboseLoggingEnabled) {
                log("Enter ConnectedState "
                       + " mScreenOn=" + mScreenOn);
            }

            //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
            mLastConnectedTime = mClock.getElapsedSinceBootMillis();
            //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }

            if (mQosGameIsRunning) { //SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL
                mWifiNative.setTidMode(mInterfaceName, 2, mPersistQosUid, mPersistQosTid);
            }

            mWifiConnectivityManager.handleConnectionStateChanged(
                    WifiConnectivityManager.WIFI_STATE_CONNECTED);
            registerConnected();
            lastConnectAttemptTimestamp = 0;
            targetWificonfiguration = null;

            // Not roaming anymore
            mIsAutoRoaming = false;

            if (testNetworkDisconnect) {
                testNetworkDisconnectCounter++;
                logd("ConnectedState Enter start disconnect test " +
                        testNetworkDisconnectCounter);
                sendMessageDelayed(obtainMessage(CMD_TEST_NETWORK_DISCONNECT,
                        testNetworkDisconnectCounter, 0), 15000);
            }

            mWifiConfigManager.setCurrentNetworkId(mTargetNetworkId); //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS
            mLastDriverRoamAttempt = 0;
            mTargetNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
            mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(true);
            mWifiStateTracker.updateState(WifiStateTracker.CONNECTED);
            mCurrentConfig = getCurrentWifiConfiguration(); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20, SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE

            if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20) {
                if (mCurrentConfig != null && mCurrentConfig.isPasspoint()
                    && Settings.Secure.getInt(mContext.getContentResolver(),Settings.Secure.WIFI_HOTSPOT20_CONNECTED_HISTORY, 0) == 0) {
                     Log.d(TAG, "WIFI_HOTSPOT20_CONNECTED_HISTORY is set to 1");
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

            if (mWifiGeofenceManager.isSupported()) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                if (mWifiGeofenceManager.isValidAccessPointToUseGeofence(mWifiInfo, mCurrentConfig)) {
                    mWifiGeofenceManager.triggerStartLearning(mCurrentConfig);
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

            //+SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
            if(targetWificonfiguration != null && targetWificonfiguration.enterpriseConfig != null && mEaptLoggingControllConnecting) {
                mEaptLoggingControllConnecting = false;
                sendEapLogging(targetWificonfiguration, EAP_LOGGING_STATE_CONNECTED, "None");
            }
            //-SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG

            if (CSC_SUPPORT_5G_ANT_SHARE)
                sendIpcMessageToRilForLteu(LTEU_STA_5GHZ_CONNECTED, true, mWifiInfo.is5GHz(), false);

            // >>>WCM>>>
            if (mDisabledCaptivePortalList != null && mDisabledCaptivePortalList.containsKey(mWifiInfo.getNetworkId())) {
                boolean isDisabled = mDisabledCaptivePortalList.get(mWifiInfo.getNetworkId()).getDisabled();
                mDisabledCaptivePortalList.remove(mWifiInfo.getNetworkId());
                if (!isDisabled && !mDisabledCaptivePortalList.isEmpty()) {
                    removeMessages(CMD_ENABLE_CAPTIVE_PORTAL);
                    for (DisabledCaptivePortal ap : mDisabledCaptivePortalList.values()) {
                        if (!ap.getDisabled()) {
                            ap.restartEnableCaptivePortal();
                        }
                    }
                }
            }
            // remove captive portal disabled notification
            if (mLastDisabledCaptivePortalNetId != WifiConfiguration.INVALID_NETWORK_ID) {
                log("Connected - remove captive portal disabled notification");
                Message notiMsg = new Message();
                notiMsg.what = CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON;
                notiMsg.arg1 = 0; // false
                notiMsg.obj = (Object)"Connected";
                sendMessage(notiMsg);
            }
            mInitialQcExtraInfo = -1;
            // <<<WCM<<<
            if(SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO)
                handleCellularCapabilities(true); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MBO

            //duplicatedIpChecker(); //DUPLICATED_IP_USING_DETECTION

            if (isSupportWifiTipsVersion(mTipsVersionForDavinci) && isSemLocationManagerSupported(mContext)) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
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
            }
        }
        @Override
        public boolean processMessage(Message message) {
            WifiConfiguration config = null;
            WifiConfiguration usableInternetConfig = getSpecificNetwork(mWifiInfo.getNetworkId()); // WCM
            logStateAndMessage(message, this);

            switch (message.what) {
                case CMD_UPDATE_CONFIG_LOCATION: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
                    updateLocation(message.arg1);
                case CMD_CHECK_DUPLICATED_IP:// DUPLICATED_IP_USING_DETECTION
                    if (WifiChipInfo.getInstance().getDuplicatedIpDetect()) {
                        WifiChipInfo.getInstance().setDuplicatedIpDetect(false);
                        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
                        boolean isUsingStaticIp = false;
                        if (currentConfig != null) {
                            isUsingStaticIp = (currentConfig.getIpAssignment() == IpConfiguration.IpAssignment.STATIC);
                        }
                        if (!isUsingStaticIp) {
                            mIpClient.stop();
                            setTcpBufferAndProxySettingsForIpClient();
                            final IpClient.ProvisioningConfiguration prov;
                            if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS) {
                                prov = IpClient.buildProvisioningConfiguration()
                                        .withPreDhcpAction()
                                        .withoutIpReachabilityMonitor()
                                        .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                                        .build();
                            } else {
                                prov = IpClient.buildProvisioningConfiguration()
                                        .withPreDhcpAction()
                                        .withApfCapabilities(mWifiNative.getApfCapabilities(mInterfaceName))
                                        .build();
                            }
                            mIpClient.startProvisioning(prov);
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
                    } else if (message.arg1 == NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN ||
                            message.arg1 == NETWORK_STATUS_UNWANTED_VALIDATION_FAILED) {
                        Log.d(TAG, (message.arg1 == NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN
                                ? "NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN"
                                : "NETWORK_STATUS_UNWANTED_VALIDATION_FAILED"));
                        config = getCurrentWifiConfiguration();
                        if (config != null) {
                            // Disable autojoin
                            if (message.arg1 == NETWORK_STATUS_UNWANTED_DISABLE_AUTOJOIN) {
                                mWifiConfigManager.setNetworkValidatedInternetAccess(
                                        config.networkId, false);
                                mWifiConfigManager.updateNetworkSelectionStatus(config.networkId,
                                        WifiConfiguration.NetworkSelectionStatus
                                        .DISABLED_NO_INTERNET_PERMANENT);
                            } else {
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
                    // >>>WCM>>>
                    } else if (message.arg1 == NETWORK_STATUS_UNWANTED_DISABLE_USER_SELECTION) {
                        /*config = getCurrentWifiConfiguration();
                        if (config != null) {
                            if ((Settings.Global.getInt(mContext.getContentResolver(),
                                    Settings.Global.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, 0) == 1)) {
                                Log.d(TAG, "SNS disable reason : DISABLED_NO_INTERNET");
                                mWifiConfigManager.disablePoorNetwork(config.networkId, mWifiInfo.getRssi(), mLastBssid, true);
                            }
                        }*/
                        notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                DISCONNECT_REASON_UNWANTED_BY_USER);
                        //transitionTo(mDisconnectingState);
                    }
                    if ((mWlanAdvancedDebugState & WLAN_ADVANCED_DEBUG_DISC) != 0) { // SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLAN_ADVANCED_DEBUG
                        sendBroadcastIssueTrackerSysDump(ISSUE_TRACKER_SYSDUMP_UNWANTED);
                    }
                    // <<<WCM<<<
                    return HANDLED;
                case CMD_SEND_DHCP_RELEASE: // CscFeature_Wifi_SendSignalDuringPowerOff
                    if (mIpClient != null) {
                        mIpClient.sendDhcpReleasePacket();
                    }
                    if (waitForDhcpRelease() != 0) {
                        loge("waitForDhcpRelease error");
                    } else {
                        loge("waitForDhcpRelease() Success");
                    }
                    break;

                case CMD_NETWORK_STATUS:
                    if (message.arg1 == NetworkAgent.VALID_NETWORK) {
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
                    return HANDLED;
                case CMD_ACCEPT_UNVALIDATED:
                    boolean accept = (message.arg1 != 0);
                    mWifiConfigManager.setNetworkNoInternetAccessExpected(mLastNetworkId, accept);
                    return HANDLED;
                case CMD_TEST_NETWORK_DISCONNECT:
                    // Force a disconnect
                    if (message.arg1 == testNetworkDisconnectCounter) {
                        mWifiNative.disconnect(mInterfaceName);
                    }
                    break;
                case CMD_ASSOCIATED_BSSID:
                    // ASSOCIATING to a new BSSID while already connected, indicates
                    // that driver is roaming
                    mLastDriverRoamAttempt = mClock.getWallClockMillis();
                    report(ReportIdKey.ID_ROAMING_TRIGGER, //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                            ReportUtil.getReportDataForRoamingEnter(
                                "dongle",
                                mWifiInfo.getSSID(),
                                (String) message.obj));

                    //+SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                            && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                            && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
                        if (MobileWIPS.getInstance() != null) {
                            MobileWIPS.getInstance().sendEmptyMessage(MWIPSDef.EVENT_ROAMING);
                        }
                    }
                    //-SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                    return NOT_HANDLED;
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    long lastRoam = 0;
                    reportConnectionAttemptEnd(
                            WifiMetrics.ConnectionEvent.FAILURE_NETWORK_DISCONNECTION,
                            WifiMetricsProto.ConnectionEvent.HLF_NONE);
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

                    if (mUnstableApController != null && config != null) { //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                        int disconnectReason = message.arg2;
                        if (disconnectReason == 77) {
                            mWifiConfigManager.updateNetworkSelectionStatus(
                                    config.networkId, WifiConfiguration.NetworkSelectionStatus
                                        .DISABLED_BY_WIPS);
                        }
                        boolean isDetected = mUnstableApController.disconnect(
                                mWifiInfo.getBSSID(),
                                mWifiInfo.getRssi(), config, disconnectReason);
                        if (isDetected) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                            report(ReportIdKey.ID_UNSTABLE_AP_DETECTED,
                                    ReportUtil.getReportDataForUnstableAp(config.networkId));
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
                    ScanResult candidate = (ScanResult)message.obj;
                    String bssid = SUPPLICANT_BSSID_ANY;
                    if (candidate != null) {
                        bssid = candidate.BSSID;
                    }
                    config = mWifiConfigManager.getConfiguredNetworkWithoutMasking(netId);
                    if (config == null) {
                        loge("CMD_START_ROAM and no config, bail out...");
                        break;
                    }

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
                        lastConnectAttemptTimestamp = mClock.getWallClockMillis();
                        targetWificonfiguration = config;
                        mIsAutoRoaming = true;
                        mWifiMetrics.logStaEvent(StaEvent.TYPE_CMD_START_ROAM, config);
                        transitionTo(mRoamingState);
                    } else {
                        loge("CMD_START_ROAM Failed to start roaming to network " + config);
                        reportConnectionAttemptEnd(
                                WifiMetrics.ConnectionEvent.FAILURE_CONNECT_NETWORK_FAILED,
                                WifiMetricsProto.ConnectionEvent.HLF_NONE);
                        replyToMessage(message, WifiManager.CONNECT_NETWORK_FAILED,
                                WifiManager.ERROR);
                        messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
                        break;
                    }
                    break;
                case CMD_START_IP_PACKET_OFFLOAD: {
                    int slot = message.arg1;
                    int intervalSeconds = message.arg2;
                    KeepalivePacketData pkt = (KeepalivePacketData) message.obj;
                    int result = startWifiIPPacketOffload(slot, pkt, intervalSeconds);
                    if (mNetworkAgent != null) {
                        mNetworkAgent.onPacketKeepaliveEvent(slot, result);
                    }
                    break;
                }
                /* Porting CL 5970214, Device can not connect to EAP-TLS after reboot */
                case CMD_RELOAD_TLS_AND_RECONNECT: //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
                    Log.d(TAG,"WiFi already connected. do nothing");
                    break;
                // >>>WCM>>>
                case WifiConnectivityMonitor.CAPTIVE_PORTAL_STATE_EVENT:
                    log("CAPTIVE_PORTAL_STATE_EVENT received");
                    switch (message.arg1) {
                        case WifiConnectivityMonitor.CAPTIVE_PORTAL_EVENT_DETECTED:
                            log("CAPTIVE_PORTAL_EVENT_DETECTED received");
                            try {
                                // late detect
                                mWifiInfo.setCaptivePortal(true);
                                mWifiInfo.setAuthenticated(false);
                                // Broadcast RSSI change for sync with mWifiInfo
                                // in WWSM
                                sendRssiChangeBroadcast(mWifiInfo.getRssi());
                                if (!mWifiConfigManager.setCaptivePortal(mWifiInfo.getNetworkId(), true)) {
                                    Log.w(TAG, "Failed to write config captive portal true");
                                }
                                if (!mWifiConfigManager.setAuthenticated(mWifiInfo.getNetworkId(), false)){
                                    Log.w(TAG, "Failed to write config authenticated false");
                                }

                                replyToMessage(message, message.what, 0); // success

                                // notify captive portal state changed
                                sendCaptivePortaChangedBroadcast(false);
                            } catch (Exception e) {
                                replyToMessage(message, message.what, -1); // failed
                                Log.w(TAG, "Exception occured while set captive portal values: " + e);
                            }
                            break;
                        case WifiConnectivityMonitor.CAPTIVE_PORTAL_EVENT_AUTHENTICATED:
                            log("CAPTIVE_PORTAL_EVENT_AUTHENTICATED received");
                            if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ADVANCED_CAPTIVE_PORTAL) {
                                // update isAuthenticated variable.
                                mWifiInfo.setAuthenticated(true);
                                // Broadcast RSSI change for sync with mWifiInfo in WWSM
                                sendRssiChangeBroadcast(mWifiInfo.getRssi());
                                // update WiFi icon.
                                Intent intent = new Intent("android.net.netmon.captive_portal_logged_in");
                                intent.putExtra(Intent.EXTRA_TEXT, String.valueOf(mWifiInfo.getNetworkId()));
                                intent.putExtra("result", Integer.toString(2));
                                sendBroadcastFromWifiStateMachine(intent);
                            }

                            if (!mWifiConfigManager.setAuthenticated(mWifiInfo.getNetworkId(), true)){
                                Log.w(TAG, "Failed to write config authenticated true");
                            }

                            // notify captive portal state changed
                            sendCaptivePortaChangedBroadcast(true);
                            break;
                        }
                    break;
                case WifiConnectivityMonitor.CAPTIVE_PORTAL_EVENT_DISABLED:
                    log("CAPTIVE_PORTAL_EVENT_DISABLED received");
                    mWifiConfigManager.updateNetworkSelectionStatus(mWifiInfo.getNetworkId(),
                            WifiConfiguration.NetworkSelectionStatus.DISABLED_CAPTIVE_PORTAL);

                    mDisabledCaptivePortalList.put(mWifiInfo.getNetworkId(),
                            new DisabledCaptivePortal(mWifiInfo.getNetworkId(), mWifiInfo.getSSID().replace("\"", "")));

                    // add captive portal disabled notification
                    mLastDisabledCaptivePortalNetId = mWifiInfo.getNetworkId();
                    Message notiMsg = new Message();
                    notiMsg.what = CMD_SHOW_DISABLED_CAPTIVE_PORTAL_NOTIFICAITON;
                    notiMsg.arg1 = 1; // true
                    notiMsg.obj = (Object)"Disabled";
                    sendMessage(notiMsg);
                    break;
                case WifiConnectivityMonitor.NEED_TO_ROAM_IN_VALID:
                    Log.d(TAG, "ConnectedState - NEED_TO_ROAM_IN_VALID, message.arg1 : ");
                    mInitialQcExtraInfo = message.arg1;

                    mNeedRoamingInValid = true;

                    mScanRequestProxy.startScan(Process.SYSTEM_UID, null);

                    break;
                case WifiConnectivityMonitor.POOR_LINK_DETECTED:
                    log("Watchdog reports poor link");
                    Log.d(TAG, "ConnectedState - POOR_LINK_DETECTED , message.arg1 : " + message.arg1 + "message.arg2 : " + message.arg2);

                    mInitialQcExtraInfo = message.arg1;
                    mWcmNoInternetReason = message.arg2;

                    mIsReconn = 1;
                    mNeedRoamingInValid = false;

                    mScanRequestProxy.startScan(Process.SYSTEM_UID, null);

                    break;
                case WifiConnectivityMonitor.CHECK_ALTERNATIVE_NETWORKS:
                    if (mInitialQcExtraInfo == -1) {
                        if (DBG) log(getName() + "CONNECTED : CHECK_ALTERNATIVE_NETWORKS but NOT_HANDLED");
                        return NOT_HANDLED;
                    }
                    Log.d(TAG, "CONNECTED : CHECK_ALTERNATIVE_NETWORKS");

                    /*Internet available AP may be missed in scanResult at the disconnect time.
                    when supplicant updates new scan result, check again.*/

                    boolean bKeepL2 = true;
                    List<ScanResult> scanResults = mScanRequestProxy.getScanResults();
                    List<WifiConfiguration> configs = mWifiConfigManager.getSavedNetworks();
                    boolean bHS20Enabled = false;
                    boolean needRoam = false;
                    boolean isRoamingNetwork = false;
                    config = getCurrentWifiConfiguration(); //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_NETWORK_EVALUATOR

                    // O porting
                    //if (mSemWifiUtil.getHs20State()/* && config.isPasspoint()*/) {
                    //    bHS20Enabled = true;
                    //}
                    /*Check remembered AP list in case of LINK_STATUS_EXTRA_INFO_POOR_LINK.
                    If there's Internet available one,
                    disable current poor quality AP(to give a chance).
                    */
                    ScanResult bestCandidate = null;
                    int bestCandidateNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
                    if (scanResults != null) {
                        Log.d(TAG, "scanResult is not null");

                        // Find Roaming Target BSSID
                        WifiConfiguration currConfig = getSpecificNetwork(mWifiInfo.getNetworkId());
                        int candidateCount = 0;
                        if (bKeepL2 && currConfig != null) {
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
                                } else if (SUPPORT_WPA3_SAE && scanResult.capabilities.contains("SAE")) {
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
                        if (!needRoam && bKeepL2 && configs != null) {
                            for (WifiConfiguration conf : configs) {
                                if ((conf.isUsableInternet || !conf.hasNoInternetAccess())
                                        && conf.semAutoReconnect == 1) {
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
                                        } else if (SUPPORT_WPA3_SAE && scanResult.capabilities.contains("SAE")) {
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
                                            Log.d(TAG, "There's internet available AP. Disable current AP.");
                                            bKeepL2 = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!mNeedRoamingInValid) {
                        if (needRoam) {
                            startRoamToNetwork(bestCandidateNetworkId, bestCandidate);
                        } else if (!bKeepL2) {
                            // Can't reach Internet. Update 'no internet' flag to prevent auto-connect.
                            if (!isRoamingNetwork && (mInitialQcExtraInfo == WifiConnectivityMonitor.LINK_STATUS_EXTRA_INFO_NO_INTERNET)
                                    && (usableInternetConfig != null) && (usableInternetConfig.isUsableInternet)) {
                                mWifiConfigManager.updateUsableInternet(mLastNetworkId, false);
                            }
                            if (ENBLE_WLAN_CONFIG_ANALYTICS) setAnalyticsNoInternetDisconnectReason(mWcmNoInternetReason);
                            mWifiConfigManager.disablePoorNetwork(
                                    mWifiInfo.getNetworkId() , mWifiInfo.getRssi(), mLastBssid);

                            if (config != null) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_NETWORK_EVALUATOR
                                Log.d(TAG, "Current config's validatedInternetAccess sets as false because bKeepL2 is false.");
                                config.validatedInternetAccess = false;
                            }

                            notifyDisconnectInternalReason( //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                    DISCONNECT_REASON_NO_INTERNET);

                            transitionTo(mDisconnectingState);
                        }
                    } else {
                        if (needRoam) {
                            startRoamToNetwork(bestCandidateNetworkId, bestCandidate);
                        } else {
                            if (mWcmChannel != null) mWcmChannel.sendMessage(CMD_TRANSIT_TO_INVALID);
                        }
                    }
                    return HANDLED;

                // <<<WCM<<<
                default:
                    return NOT_HANDLED;
            }
            return HANDLED;
        }

        @Override
        public void exit() {
            logd("WifiStateMachine: Leaving Connected state");

            //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
            mLastConnectedTime = -1;
            //SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }

            if (mQosGameIsRunning) { //SEC_FLOATING_FEATURE_WLAN_SUPPORT_QOS_CONTROL
                mWifiNative.setTidMode(mInterfaceName, 0, 0, 0);
            }

            mWifiConnectivityManager.handleConnectionStateChanged(
                     WifiConnectivityManager.WIFI_STATE_TRANSITIONING);

            mLastDriverRoamAttempt = 0;
            mWifiInjector.getWifiLastResortWatchdog().connectedStateTransition(false);
            // >>>WCM>>>
            mInitialQcExtraInfo = -1;
            setStopScanForWCM(false);
            // <<<WCM<<<

            mDelayDisconnect.setEnable(false, 0); //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
            mWifiConfigManager.setCurrentNetworkId(-1); //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS

            if (CSC_SUPPORT_5G_ANT_SHARE)
                sendIpcMessageToRilForLteu(LTEU_STA_5GHZ_CONNECTED, false, false, false);

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

            if (mWifiInjector.getSemWifiApChipInfo().supportWifiSharing()) {
                logd("Wifi got Disconnected in connectedstate, Send provisioning intent mIsAutoRoaming" + mIsAutoRoaming);
                if (Vendor.VZW == mOpBranding){
                    if (!mIsAutoRoaming){
                        Intent provisionIntent = new Intent("com.samsung.intent.action.START_PROVISIONING");
                        provisionIntent.putExtra("wState", WifiConnectivityManager.WIFI_STATE_DISCONNECTED);
                        sendBroadcastFromWifiStateMachine(provisionIntent);
                        //O os banned implicit broadcastreceiver
                        provisionIntent.setPackage("com.android.settings");
                        sendBroadcastFromWifiStateMachine(provisionIntent);
                    }
                } else {
                    Intent provisionIntent = new Intent("com.samsung.intent.action.START_PROVISIONING");
                    provisionIntent.putExtra("wState", WifiConnectivityManager.WIFI_STATE_DISCONNECTED);
                    sendBroadcastFromWifiStateMachine(provisionIntent);
                    //O os banned implicit broadcastreceiver
                    provisionIntent.setPackage("com.android.settings");
                    sendBroadcastFromWifiStateMachine(provisionIntent);
                }
            } else if (!mIsAutoRoaming && Vendor.VZW == mOpBranding) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                SemWifiFrameworkUxUtils.sendShowInfoIntent(mContext,
                        SemWifiFrameworkUxUtils.INFO_TYPE_DISCONNECT_TOAST);
            }

            if (isSupportWifiTipsVersion(mTipsVersionForDavinci) && mSemLocationManager != null) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
                if (DBG_LOCATION_INFO) Log.d(TAG,"Remove location updates");
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
            disconnectingWatchdogCount++;
            logd("Start Disconnecting Watchdog " + disconnectingWatchdogCount);
            sendMessageDelayed(obtainMessage(CMD_DISCONNECTING_WATCHDOG_TIMER,
                    disconnectingWatchdogCount, 0), DISCONNECTING_GUARD_TIMER_MSEC);
        }

        @Override
        public boolean processMessage(Message message) {
            logStateAndMessage(message, this);
            switch (message.what) {
                case CMD_DISCONNECT:
                    if (mVerboseLoggingEnabled) log("Ignore CMD_DISCONNECT when already disconnecting.");
                    break;
                case CMD_DISCONNECTING_WATCHDOG_TIMER:
                    if (disconnectingWatchdogCount == message.arg1) {
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
                    return NOT_HANDLED;
            }
            return HANDLED;
        }
    }

    class DisconnectedState extends State {
        @Override
        public void enter() {
            Log.i(TAG, "disconnectedstate enter");
            // We dont scan frequently if this is a temporary disconnect
            // due to p2p
            mEaptLoggingControllConnecting = true; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG
            mEaptLoggingControllAuthFailure = true; //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX, EAP_LOG

            // >>>WCM>>>
            mIsReconn = 0;
            mNeedRoamingInValid = false;
            // <<<WCM<<<

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

            /** clear the roaming state, if we were roaming, we failed */
            mIsAutoRoaming = false;

            mWifiConnectivityManager.handleConnectionStateChanged(
                    WifiConnectivityManager.WIFI_STATE_DISCONNECTED);

            mDisconnectedTimeStamp = mClock.getWallClockMillis();

            if (mIsAutoRoaming && Vendor.VZW == mOpBranding) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HIDDEN_AP_DISCONNECT_TOAST
                if (mWifiInjector.getSemWifiApChipInfo().supportWifiSharing()) {
                    logd("Wifi got in DisconnectedState, Send provisioning intent mIsAutoRoaming" + mIsAutoRoaming);
                    Intent provisionIntent = new Intent("com.samsung.intent.action.START_PROVISIONING");
                    provisionIntent.putExtra("wState", WifiConnectivityManager.WIFI_STATE_DISCONNECTED);                    
                    sendBroadcastFromWifiStateMachine(provisionIntent);
                    //O os banned implicit broadcastreceiver
                    provisionIntent.setPackage("com.android.settings");
                    sendBroadcastFromWifiStateMachine(provisionIntent);
                } else {
                    SemWifiFrameworkUxUtils.sendShowInfoIntent(mContext,
                            SemWifiFrameworkUxUtils.INFO_TYPE_DISCONNECT_TOAST);
                }
            }
        }

        @Override
        public boolean processMessage(Message message) {
            boolean ret = HANDLED;

            logStateAndMessage(message, this);

            switch (message.what) {
                case CMD_THREE_TIMES_SCAN_IN_IDLE: // SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE
                    if ((mScanResultsEventCounter++ < MAX_SCAN_RESULTS_EVENT_COUNT_IN_IDLE) && mScreenOn) {
                        Log.e(TAG, "DisconnectedState  CMD_THREE_TIMES_SCAN_IN_IDLE && mScreenOn");
                        if (mScanRequestProxy != null) {
                            String packageName = mContext.getOpPackageName();
                            mScanRequestProxy.startScan(Process.SYSTEM_UID, packageName);
                        }
                        sendMessageDelayed(CMD_THREE_TIMES_SCAN_IN_IDLE, 8000);
                    }
                    break;
                case CMD_DISCONNECT:
                    mWifiMetrics.logStaEvent(StaEvent.TYPE_FRAMEWORK_DISCONNECT,
                            StaEvent.DISCONNECT_GENERIC);
                    mWifiNative.disconnect(mInterfaceName);
                    break;
                /* Ignore network disconnect */
                case WifiMonitor.NETWORK_DISCONNECTION_EVENT:
                    if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID
                            && mUnstableApController != null //SEC_PRODUCT_FEATURE_WLAN_DISABLE_AUTOCONNECT_WITH_WEAK_AP
                            && /*locallyGenerated*/ message.arg1 == 0) {
                        String bssid = (String) message.obj;
                        boolean isSameNetwork = true;
                        ScanDetailCache scanDetailCache =
                                mWifiConfigManager.getScanDetailCacheForNetwork(mTargetNetworkId);
                        if (bssid != null && scanDetailCache != null) {
                            ScanResult scanResult = scanDetailCache.getScanResult(bssid);
                            if (scanResult == null) {
                                Log.i(TAG, "disconnected, but not for current network");
                                isSameNetwork = false;
                            }
                        }
                        if (isSameNetwork) {
                            int disconnectReason = message.arg2;
                            if (disconnectReason == 77) {
                                mWifiConfigManager.updateNetworkSelectionStatus(
                                        mTargetNetworkId, WifiConfiguration.NetworkSelectionStatus
                                            .DISABLED_BY_WIPS);
                            }
                            boolean isDetected = mUnstableApController.disconnectWithAuthFail(
                                    mTargetNetworkId, bssid, mWifiInfo.getRssi(), disconnectReason,
                                    getCurrentState() == mConnectedState);
                            if (isDetected) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                                report(ReportIdKey.ID_UNSTABLE_AP_DETECTED,
                                        ReportUtil.getReportDataForUnstableAp(mTargetNetworkId));
                            }
                        }
                    }
                    // Interpret this as an L2 connection failure
                    break;
                case WifiMonitor.SUPPLICANT_STATE_CHANGE_EVENT:
                    StateChangeResult stateChangeResult = (StateChangeResult) message.obj;
                    if (mVerboseLoggingEnabled) {
                        logd("SUPPLICANT_STATE_CHANGE_EVENT state=" + stateChangeResult.state
                                + " -> state= "
                                + WifiInfo.getDetailedStateOf(stateChangeResult.state));
                    }
                    setNetworkDetailedState(WifiInfo.getDetailedStateOf(stateChangeResult.state));

                    /* ConnectModeState does the rest of the handling */
                    ret = NOT_HANDLED;
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
                        ret = NOT_HANDLED;
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
                    ret = NOT_HANDLED;
            }
            return ret;
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

//    void handleGsmAuthRequest(SimAuthRequestData requestData) {
        void handleGsmAuthRequest(SimAuthRequestData requestData, int simNum) { //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
        if (targetWificonfiguration == null
                || targetWificonfiguration.networkId
                == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
        } else {
            logd("id does not match targetWifiConfiguration");
            return;
        }

        String response =
//                TelephonyUtil.getGsmSimAuthResponse(requestData.data, getTelephonyManager());
                TelephonyUtil.semGetGsmSimAuthResponse(requestData.data, getTelephonyManager(), simNum); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
        if (response == null) {
            mWifiNative.simAuthFailedResponse(mInterfaceName, requestData.networkId);
        } else {
            logv("Supplicant Response -" + response);
            mWifiNative.simAuthResponse(
                    mInterfaceName, requestData.networkId,
                    WifiNative.SIM_AUTH_RESP_TYPE_GSM_AUTH, response);
        }
    }

//    void handle3GAuthRequest(SimAuthRequestData requestData) {
    void handle3GAuthRequest(SimAuthRequestData requestData, int simNum) { //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
        if (targetWificonfiguration == null
                || targetWificonfiguration.networkId
                == requestData.networkId) {
            logd("id matches targetWifiConfiguration");
        } else {
            logd("id does not match targetWifiConfiguration");
            return;
        }

        SimAuthResponseData response =
//                TelephonyUtil.get3GAuthResponse(requestData, getTelephonyManager());
                TelephonyUtil.semGet3GAuthResponse(requestData, getTelephonyManager(), simNum); //SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
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
        mWifiMetrics.updateSavedNetworks(mWifiConfigManager.getSavedNetworks());
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
            messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
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
            messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, WifiManager.SAVE_NETWORK_FAILED, WifiManager.ERROR);
            return new NetworkUpdateResult(WifiConfiguration.INVALID_NETWORK_ID);
        }
        updateBssidWhitelist(config); //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
        config.priority = mWifiConfigManager.increaseAndGetPriority(); //SEC_PRODUCT_FEATURE_WLAN_CLEANING_UP_CONFIGURED_NETWORKS
        NetworkUpdateResult result =
                mWifiConfigManager.addOrUpdateNetwork(config, message.sendingUid);
        if (!result.isSuccess()) {
            loge("SAVE_NETWORK adding/updating config=" + config + " failed");
            messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
            replyToMessage(message, WifiManager.SAVE_NETWORK_FAILED, WifiManager.ERROR);
            return result;
        }
        if (!mWifiConfigManager.enableNetwork(
                result.getNetworkId(), false, message.sendingUid)) {
            loge("SAVE_NETWORK enabling config=" + config + " failed");
            messageHandlingStatus = MESSAGE_HANDLING_STATUS_FAIL;
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
     * Note, caller of this helper function must acquire mWifiReqCountLock.
     */
    private boolean hasConnectionRequests() {
        return mConnectionReqCount > 0 || mUntrustedReqCount > 0;
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

    // >>>WCM>>>
    public void setWcmAsyncChannel (Handler wcmHandler) {
            log("setWcmAsyncChannel");
            if (mWcmChannel == null){
                if (DBG) log("new mWcmChannel created");
                mWcmChannel = new AsyncChannel();
            }
            if (DBG) log("mWcmChannel connected");
            mWcmChannel.connect(mContext, getHandler(), wcmHandler);
    }
    // <<<WCM<<<

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

    /**
     * Sends a message to initialize the WifiStateMachine.
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

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
    public void report(int reportId, Bundle args) {
        if (mIssueDetector != null && args != null) {
            mIssueDetector.sendMessage(
                    mIssueDetector.obtainMessage(0, reportId, 0, args));
        }
    }

    public boolean isForegroundWifiSettings() { //SEC_PRODUCT_FEATURE_WLAN_THREE_TIMES_SCAN_IN_IDLE
        String packageName = null;
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        }
        if (mActivityManager != null) {
            List<ActivityManager.RunningTaskInfo> tasks = mActivityManager.getRunningTasks(1);
            if (!tasks.isEmpty()) {
                packageName = tasks.get(0).topActivity.getPackageName();
            }
        }
        if (DBG) Log.i(TAG, "Foreground PackageName : " + packageName);

        if (packageName != null && "com.android.settings".equals(packageName.trim())) {
            return true;
        }
        return false;
    }

    private void sendBroadcastFromWifiStateMachine(Intent intent) {
        if (mContext == null)
            return;
        try {
            mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        } catch (IllegalStateException e) {
            loge("Send broadcast before boot - action:" + intent.getAction());
        }
    }

    // >>>WCM>>>
    public static final int NETWORK_PROPERTIES_IP_CHANGE        = 1;
    public static final int NETWORK_PROPERTIES_MAC_CHANGE       = 2;
    public static final int NETWORK_PROPERTIES_DNS_CHANGE       = 3;
    public static final int NETWORK_PROPERTIES_POSSIBLE_CHANGE  = 4;

    // Smart network switch
    private void notifyWcmDhcpSession(String startComplete) {
        if (mWcmChannel != null) mWcmChannel.sendMessage(CMD_DHCP_START_COMPLETE, 0, 0, startComplete);
    }

    // Smart network switch
    private void notifyWcmRoamSession(String startComplete) {
        // +SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        if (SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
                && "".equals(CONFIG_SECURE_SVC_INTEGRATION)
                && !(SemCscFeature.getInstance().getBoolean(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_DISABLEMWIPS))) {
            if (MobileWIPS.getInstance() != null && startComplete.equals("start")) {
                Message msg = new Message();
                msg.what = MWIPSDef.EVENT_ROAMING_STARTED;
                MobileWIPS.getInstance().sendMessage(msg);
            }
        }
        // -SEC_PRODUCT_FEATURE_WLAN_SUPPORT_MALICIOUS_HOTSPOT_DETECTION
        if (mWcmChannel != null) mWcmChannel.sendMessage(CMD_ROAM_START_COMPLETE, 0, 0, startComplete);
    }

    private boolean updateBssidWhitelist(WifiConfiguration config) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
        List<ScanResult> mScanResults = mScanRequestProxy.getScanResults();
        if (config == null) {
            loge("updateBssidWhitelist: config is null");
            return false;
        }

        String ssid = config.getPrintableSsid();
        if (ssid != null && (ssid.equals("iptime") || ssid.equals("iptime5G"))
                && WifiConfigurationUtil.isConfigForOpenNetwork(config)) {
            if (mScanResults != null && mScanResults.size() > 0) {
                long currentTime = System.currentTimeMillis();
                for (ScanResult scanResult : mScanResults) {
                    if (ScanResultMatchInfo.fromScanResult(scanResult).
                            equals(ScanResultMatchInfo.fromWifiConfiguration(config))) {
                        if (config.bssidWhitelist != null) {
                            synchronized (config.bssidWhitelist) {
                                config.bssidWhitelist.put(scanResult.BSSID, currentTime);
                            }
                            log("updateBssidWhitelist: " + scanResult.BSSID + " is added in whitelist");
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean updateBssidWhitelist(WifiConfiguration config, String bssid) { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_BSSID_WHITELIST for KOR
        List<ScanResult> mScanResults = mScanRequestProxy.getScanResults();
        if (config == null) {
            loge("updateBssidWhitelist: config is null");
            return false;
        }
        if (config.bssidWhitelist == null) {
            loge("updateBssidWhitelist: mBssidWhitelist is null");
            return false;
        }
        if (bssid == null) {
            loge("updateBssidWhitelist: bssid is null");
            return false;
        }

        String ssid = config.getPrintableSsid();
        if (ssid != null && (ssid.equals("iptime") || ssid.equals("iptime5G"))
                && WifiConfigurationUtil.isConfigForOpenNetwork(config)) {
            if (config.bssidWhitelist.containsBssid(bssid)) {
                long currentTime = System.currentTimeMillis();
                synchronized (config.bssidWhitelist) {
                    config.bssidWhitelist.put(bssid, currentTime);
                }
                log("updateBssidWhitelist: " + bssid +
                        " is contained in whitelist. So updatedTime is set as " + currentTime);
            }

            if (mScanResults != null && mScanResults.size() > 0) {
                long currentTime = System.currentTimeMillis();
                for (ScanResult scanResult : mScanResults) {
                    if (ScanResultMatchInfo.fromScanResult(scanResult).
                            equals(ScanResultMatchInfo.fromWifiConfiguration(config))) {
                        if (config.bssidWhitelist != null) {
                            synchronized (config.bssidWhitelist) {
                                config.bssidWhitelist.put(scanResult.BSSID, currentTime);
                            }
                            log("updateBssidWhitelist: " + scanResult.BSSID + " is added in whitelist!!");
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    void setAutoConnectCarrierApEnabled(boolean enabled) { //SEC_PRODUCT_FEATURE_WLAN_AUTO_CONNECT_CARRIER_AP
        sendMessage(CMD_AUTO_CONNECT_CARRIER_AP_ENABLED, enabled ? 1 : 0, 0);
    }

    public void setWwsmAsyncChannel (Handler wcmHandler) {
        log("setWcmAsyncChannel");
        if (mWcmChannel == null){
            if (DBG) log("new mWcmChannel created");
            mWcmChannel = new AsyncChannel();
        }
        if (DBG) log("mWcmChannel connected");
        mWcmChannel.connect(mContext, getHandler(), wcmHandler);
    }

    private void updateAdpsState() { //SEC_FLOATING_FEATURE_WIFI_SUPPORT_ADPS
        mWifiNative.setAdps(mInterfaceName, mWifiAdpsEnabled.get());
        mBigDataManager.addOrUpdateValue(WifiBigDataLogManager.LOGGING_TYPE_ADPS_STATE,mWifiAdpsEnabled.get() ? 1 : 0);
    }

    class DisabledCaptivePortal {
        private final int SCAN_OUT_COUNT_MAX = 2;
        private final int ENABLE_DELAY_TIME = 1000;
        private int netId;
        private String ssid;
        private int scanOutCount;
        private long enableTime;
        private boolean isDisabled;

        public DisabledCaptivePortal(int netId, String ssid) {
            this.netId = netId;
            this.ssid = ssid;
            scanOutCount = 0;
            enableTime = 0L;
            isDisabled = true;
        }

        public int getNetId() {
            return netId;
        }

        public String getSSID() {
            return ssid;
        }

        public boolean getDisabled() {
            return isDisabled;
        }

        public void setDisabled(boolean isDisabled) {
            this.isDisabled = isDisabled;
        }

        public void restartEnableCaptivePortal() {
            Message msg = new Message();
            msg.what = CMD_ENABLE_CAPTIVE_PORTAL;
            msg.arg1 = netId;
            long delayTime = enableTime - SystemClock.elapsedRealtime();
            sendMessageDelayed(msg, delayTime);
            if (DBG) Log.i(TAG, "restartEnableCaptivePortal - " + ssid + " after " + delayTime/1000);
        }

        public void updateScanResult(boolean isScaned) {
            if (isDisabled) {
                if (isScaned) {
                    if (DBG) Log.i(TAG, "updateScanResult - " + ssid + " found");
                    scanOutCount = 0;
                } else {
                    scanOutCount++;
                    if (DBG) Log.i(TAG, "updateScanResult - " + ssid + " scan out:" + scanOutCount + " times");
                    if (scanOutCount >= SCAN_OUT_COUNT_MAX) {
                        if (DBG) Log.i(TAG, "updateScanResult - " + ssid + " enable 10 min later.");
                        isDisabled = false;
                        enableTime = SystemClock.elapsedRealtime() + ENABLE_DISABLED_CAPTIVE_PORTAL_MS;
                        Message msg = new Message();
                        msg.what = CMD_DISABLED_CAPTIVE_PORTAL_SCAN_OUT;
                        msg.arg1 = netId;
                        sendMessage(msg);
                    }
                }
            } else {
                long now = SystemClock.elapsedRealtime();
                if (now > enableTime) {
                    log("updateScanResult - enable time exceeded");
                    Message msg = new Message();
                    msg.what = CMD_ENABLE_CAPTIVE_PORTAL;
                    msg.arg1 = netId;
                    sendMessageDelayed(msg, ENABLE_DELAY_TIME);
                }
            }
        }
    }

    private void showCaptivePortalDisabledNotification(boolean visible) {
        log("showCaptivePortalDisabledNotification - " + visible);
        NotificationManager notificationManager = (NotificationManager)mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (visible) {
            Intent intent = new Intent();
            intent.setClassName("com.android.settings", "com.android.settings.Settings$WifiSettingsActivity");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingContentIntent = PendingIntent.getActivityAsUser(mContext, 0, intent, 0, null, UserHandle.CURRENT);

            int icon = R.drawable.stat_notify_wifi_in_range;
            String title = mContext.getString(R.string.wifi_notification_captive_portal_disabled_title);
            String details = mContext.getString(R.string.wifi_notification_captive_portal_disabled_detail);
            Notification.BigTextStyle style = new Notification.BigTextStyle();
            style.bigText(details);

            String channelId = SystemNotificationChannels.NETWORK_STATUS;
            Notification notification = new Notification.Builder(mContext, channelId)
                    .setSmallIcon(icon)
                    .setStyle(style)
                    .setAutoCancel(true)
                    .setTicker(title)
                    .setContentTitle(title)
                    .setContentText(details)
                    .setContentIntent(pendingContentIntent)
                    .build();
            notification.when = System.currentTimeMillis();

            notificationManager.notify(DISABLED_NOTIFICATION_ID, 302, notification);
        } else {
            notificationManager.cancel(DISABLED_NOTIFICATION_ID, 302);
        }
    }

    public boolean setCaptivePortalUrl(String url) {
        if (mWifiConfigManager == null) {
            Log.e(TAG, "setCaptivePortalUrl: mWifiConfigManager is NULL! - NetworkId: " + mWifiInfo.getNetworkId());
            return false;
        }
        url = "\"" + url + "\"";
        if (DBG) Log.d(TAG, "setCaptivePortalUrl: loginUrl set as " + url);
        return mWifiConfigManager.setLoginUrl(mWifiInfo.getNetworkId(), url);
    }

    private InetAddress mMonitorIpAddress = null;
    private String mMonitorBssid = null;
    private LinkProperties mMonitorLinkProperties = new LinkProperties();

    // Smart network switch
    private void monitorNetworkPropertiesUpdate() {
        int reason = 0;
        if (mWifiInfo == null) return;
        try {
            // Network properties changed on ConnectedState
            if ("ConnectedState".equals(getCurrentState().getName())) {
                if (!NetworkUtils.intToInetAddress(mWifiInfo.getIpAddress()).equals(mMonitorIpAddress)) {
                    reason = NETWORK_PROPERTIES_IP_CHANGE;
                } else if (!mWifiInfo.getBSSID().equals(mMonitorBssid)) {
                    reason = NETWORK_PROPERTIES_MAC_CHANGE;
                } else if (!mLinkProperties.isIdenticalDnses(mMonitorLinkProperties)) {
                    reason = NETWORK_PROPERTIES_DNS_CHANGE;
                } else {
                    reason = NETWORK_PROPERTIES_POSSIBLE_CHANGE;
                }
            }

            if (reason != 0 && reason != NETWORK_PROPERTIES_POSSIBLE_CHANGE) {
                // if (mRoamingRenew == 0) {
                if(mWcmChannel != null) mWcmChannel.sendMessage(CMD_NETWORK_PROPERTIES_UPDATED, reason);
                // }
            }
            mMonitorIpAddress = NetworkUtils.intToInetAddress(mWifiInfo.getIpAddress());
            mMonitorBssid = mWifiInfo.getBSSID();
            mMonitorLinkProperties = mLinkProperties;
        } catch (Exception e) {
            Log.e(TAG, "monitorNetworkPropertiesUpdate - " + e);
        }
    }

    public boolean setIsFmcNetwork(boolean enable) {
        try {
            mIsFmcNetwork = enable;
            // TODO updatePoorNetworkParameters(true);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "setIsFmcNetwork - Exception while setting isFmcNetwork");
            return false;
        }
    }

    private void sendCaptivePortaChangedBroadcast(boolean isAuthenticated) {
        String action = isAuthenticated ? WifiManager.CAPTIVE_PORTAL_AUTHENTICATED
                : WifiManager.CAPTIVE_PORTAL_DETECTED;
        Intent intent = new Intent(action);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public int setDhcpRenewAfterRoamingMode(int mode) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
        semmDhcpRenewAfterRoamingMode = mode;
        Log.d(TAG, "Set semmDhcpRenewAfterRoamingMode : " + semmDhcpRenewAfterRoamingMode);
        return semmDhcpRenewAfterRoamingMode;
    }

    public int getDhcpRenewAfterRoamingMode() { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
        Log.d(TAG, "Get semmDhcpRenewAfterRoamingMode : " + semmDhcpRenewAfterRoamingMode);
        return semmDhcpRenewAfterRoamingMode;
    }

    private void setStopScanForWCM(boolean stopScan) {
        if (!isWCMEnabled()) {
            return;
        }
        Log.i(TAG, "sns inital connection check, scan set : " + stopScan);
        removeMessages(CMD_INITIAL_CONNECTION_TIMEOUT);
        if (mScanRequestProxy != null) {
            mScanRequestProxy.setScanningEnabled(!stopScan, "WCM_INITIAL_CONNECTION_CHECK");
        }
        if (stopScan) sendMessageDelayed(CMD_INITIAL_CONNECTION_TIMEOUT, 10000);
    }
    private boolean isWCMEnabled() {
        if (!SecProductFeature_WLAN.SEC_PRODUCT_FEATURE_WLAN_SEC_CONNECTIVITY_CHECK
                || SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_MHS_DONGLE
                || "REMOVED".equals(SemCscFeature.getInstance().getString(CscFeatureTagWifi.TAG_CSCFEATURE_WIFI_CONFIGSNSSTATUS))
                || SystemProperties.getBoolean("ro.radio.noril", false)) {
            return false;
        }
        return true;
    }
    // <<<WCM<<<
    public boolean isUsbTethered() { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_USB_TETHERING_UX
        checkAndSetConnectivityInstance();

        String[] tethered = mCm.getTetheredIfaces();
        String[] usbRegexs = mCm.getTetherableUsbRegexs();

        for (String tether : tethered) {
            if ("ncm0".equals(tether)) {
                Log.d(TAG, "enabled tetheredIface : ncm0");
                return false;
            }
            for (String regex : usbRegexs) {
                if (tether.matches(regex))
                    return true;
            }
        }
        return false;
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
                && mIsHs20Enabled && mOpBranding == Vendor.SKT
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

    void setShutdown() { //SEC_PRODUCT_FEATURE_WLAN_PREVENT_EXCEPTION_CASE
        mIsShutdown = true;
        //+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
        if (getCurrentState() == mConnectedState) {
            setAnalyticsUserDisconnectReason(WifiNative.ANALYTICS_DISCONNECT_REASON_USER_TRIGGER_DISCON_POWERONOFF_WIFIOFF);
        }
        //-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
        if (mDelayDisconnect.isEnabled()) { //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
            mDelayDisconnect.setEnable(false, 0);
        }
    }

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
    private SemLocationListener mSemLocationListener = new SemLocationListener() {
        @Override
        public void onLocationAvailable(Location[] locations) {
        }
        @Override
        public void onLocationChanged(Location location, Address address) {
            if (DBG_LOCATION_INFO) Log.d(TAG,"onLocationChanged is called");
            if (mLocationRequestNetworkId == WifiConfiguration.INVALID_NETWORK_ID) {
                if (DBG_LOCATION_INFO) Log.d(TAG,"There is no config to update location");
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
            mWifiGeofenceManager.setLatitudeAndLongitude(config, latitude, longitude);
            if (DBG_LOCATION_INFO) Log.d(TAG,"Location updated : " + config.SSID + "    " + latitude + ", " + longitude);
            mLocationRequestNetworkId = WifiConfiguration.INVALID_NETWORK_ID;
        }
    };

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

    private static final int[] mIgnorableApMASK = {0x002BA8C0};
    private static final int[] mApMaskCheckVsie = {0x000A14AC};
    private boolean isLocationSupportedAp(WifiConfiguration config) {
        if (config == null) return false;
        if (config.semSamsungSpecificFlags.get(WifiConfiguration.SamsungFlag.SEC_MOBILE_AP)
                || config.semIsVendorSpecificSsid) {
            Log.d(TAG, "This is a Samsung Hotspot");
            return false;
        }
        for (int mask : mIgnorableApMASK) {
            if (mWifiInfo != null && ((mWifiInfo.getIpAddress() & 0x00FFFFFF) == mask)) {
                Log.d(TAG, "This is an Android Hotspot");
                return false;
            }
        }

        if (mWifiInfo != null && mWifiInfo.getCheckVsieForSns()) {
            for (int mask : mApMaskCheckVsie) {
                if (mWifiInfo != null && ((mWifiInfo.getIpAddress() & 0x00FFFFFF) == mask)) {
                    Log.d(TAG, "This is a Mobile Hotspot");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSemLocationManagerSupported(Context context) {
        return (context.getPackageManager().hasSystemFeature("com.sec.feature.slocation"));
    }

    private boolean isSupportWifiTipsVersion(String version) {
        if (!TextUtils.isEmpty(WIFI_TIPS_VERSION) && version.equals(WIFI_TIPS_VERSION)) {
            return true;
        }
        return false;
    }

    private boolean isSameNetwork(int networkId, String bssid) {
        boolean isSameNetwork = true;
        ScanDetailCache scanDetailCache =
                mWifiConfigManager.getScanDetailCacheForNetwork(networkId);
        if (scanDetailCache != null) {
            ScanResult scanResult = scanDetailCache.getScanResult(bssid);
            if (scanResult == null) {
                isSameNetwork = false;
            }
        }
        return isSameNetwork;
    }
    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION

    // Samsung Reserved functions...
    public synchronized int callSECApi(Message msg) {
        if (mVerboseLoggingEnabled) Log.i(getName(), "callSECApi what=" + msg.what);
        int retValue = -1;

        switch (msg.what) {
            case WifiManager.SEC_COMMAND_ID_INIT:
                return 0;
            case WifiManager.SEC_COMMAND_ID_CONTROL_SENSOR_MONITOR: { //SEC_PRODUCT_FEATURE_WLAN_CONFIG_SENSOR_MONITOR
                Bundle args = (Bundle) msg.obj;
                if (args == null) {
                    return mSemSarManager.isGripSensorEnabled() ? 1 : 0;
                }
                int enable = args.getInt("enable");

                mSemSarManager.enableGripSensorMonitor(enable == 1);

                return 0;
            }
            case WifiManager.SEC_COMMAND_ID_WIFI_RECOMMENDATION_TEST: {//SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
                Bundle args = (Bundle) msg.obj;
                if (args == null) {
                    return -1;
                }
                int dynamic_score = args.getInt("dynamic_score", -1);
                int learning_score = args.getInt("learning_score", -1);
                
                int elapsed_level1 = args.getInt("elapsed_level1", -1);
                int elapsed_level2 = args.getInt("elapsed_level2", -1);

                int level = args.getInt("level", -1);

                if (dynamic_score != -1) {
                    if (mWifiRecommendNetworkLevelController == null) {
                        mWifiRecommendNetworkLevelController = mWifiInjector.getWifiRecommendNetworkLevelController();
                    }
                    Log.e(TAG, "dynamic_score : " + dynamic_score + ", learning_score : " + learning_score);
                    mWifiRecommendNetworkLevelController.setDynamicNetworkScoresForTest(dynamic_score, learning_score);
                } else if (elapsed_level1 != -1) {
                    if (mWifiRecommendNetworkManager == null) {
                        mWifiRecommendNetworkManager = mWifiInjector.getWifiRecommendNetworkManager();
                    }
                    Log.e(TAG, "elapsed_level1 : " + elapsed_level1 + ", elapsed_level2 : " + elapsed_level2);
                    mWifiRecommendNetworkManager.setElapseTime(elapsed_level1, elapsed_level2);
                } else if (level != -1) {
                    if (mWifiRecommendNetworkLevelController == null) {
                        mWifiRecommendNetworkLevelController = mWifiInjector.getWifiRecommendNetworkLevelController();
                    }
                    Log.e(TAG, "level : " + level);
                    mWifiRecommendNetworkLevelController.setLevelForTest(level);
                }
                return 0;
            }
            case WifiManager.SEC_COMMAND_ID_SAR_BACK_OFF: {  //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLANTEST
                Bundle args = (Bundle) msg.obj;
                if (args == null) {
                    return -1;
                }
                int enable = args.getInt("enable");
                if (enable == 1) {
                    mSemSarManager.enable_WiFi_PowerBackoff(true);
                } else {
                    mSemSarManager.enable_WiFi_PowerBackoff(false);
                }
                return 0;
            }

            case WifiManager.SEC_COMMAND_ID_DELAY_DISCONNECT_TRANSITION: //SEC_PRODUCT_FEATURE_WLAN_DELAY_DISCONNECT_TRANSITION
                mDelayDisconnect.setEnable(msg.arg1 == 1, msg.arg2);
                return 0;
            case WifiManager.SEC_COMMAND_ID_DISABLE_FCCCHANNEL_BACKOFF: { //SEC_PRODUCT_FEATURE_WLAN_SEC_SET_FCC_CHANNEL
                Bundle args = (Bundle) msg.obj;
                mBlockFccChannelCmd = args.getBoolean("enable");
                return 0;
            }
            case WifiManager.SEC_COMMAND_ID_SET_IMS_RSSI_POLL_STATE: { //CscFeature_Wifi_SupportRssiPollStateDuringWifiCalling
                Bundle args = (Bundle) msg.obj;
                mImsRssiPollingEnabled = args.getBoolean("state");
                if (getRssiPollingEnabledForIms()) {
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
                if (mScanRequestProxy == null) {
                    Log.e(TAG, "can't initialize mScanRequestProxy. try again few minutes later");
                    return -1;
                }
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
            case WifiManager.SEC_COMMAND_ID_TEST_MIGRATE_STORE: //SEC_PRODUCT_FEATURE_WLAN_AUTO_TEST
                mWifiConfigManager.testMigrate();
                return 0;

            case WifiManager.SEC_COMMAND_ID_TEST_RELOAD_CONFIG_STORE: //SEC_PRODUCT_FEATURE_WLAN_AUTO_TEST
                if (!mWifiConfigManager.loadFromStore()) {
                    Log.e(TAG, "Failed to load from config store");
                }
                return 0;

            case WifiManager.SEC_COMMAND_ID_TEST_REPLACE_CONFIG_FILE: //SEC_PRODUCT_FEATURE_WLAN_AUTO_TEST
                return WlanTestHelper.replaceConfigFile((Bundle) msg.obj);

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
                    case WLAN_ADVANCED_DEBUG_UDI:
                        Log.i(TAG, "WLAN_ADVANCED_DEBUG_UDI changed to true");
                        mWlanAdvancedDebugState |= WLAN_ADVANCED_DEBUG_UDI;
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
                if (args != null && mScanRequestProxy != null) {
                    boolean stop = args.getBoolean("stop", false);
                    mScanRequestProxy.setScanningEnabled(!stop, "SEC_COMMAND_ID_STOP_PERIODIC_SCAN");
                }
                break;
            case WifiManager.SEC_COMMAND_ID_SET_PCIE_IRQ_CORE: { //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_DATA_ACTIVITY_AFFINITY_BOOSTER
                if (args != null) {
                    mWifiNative.setAffinityBooster(mInterfaceName, args.getInt("enable"));
                }
                break;
            }
            case WifiManager.SEC_COMMAND_ID_SET_AUTO_RECONNECT: {// CscFeature_Wifi_DisalbeAutoReconnect
                if (args != null) {
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
            case WifiManager.SEC_COMMAND_ID_SET_MU_MIMO_MODE: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_DYNAMIC_SET_MU_MIMO_MODE
                int enable = innerMsg.arg1;
                Log.d(TAG, "WifiManager.SEC_COMMAND_ID_SET_MU_MIMO_MODE : " + enable);
                mWifiNative.setMimoMode(mInterfaceName,enable);
                break;
            //TODO: case WifiManager.SEC_COMMAND_ID_SET_WECHAT_WIFI_INFO: // CscFeature_Wifi_ConfigSocialSvcIntegration
            // >>>WCM>>>
            case WifiManager.SEC_COMMAND_ID_ANS_EXCEPTION_ANSWER: {
                if (args != null) {
                    boolean keepConnection = args.getBoolean("keep_connection");
                    Integer netId = args.getInt("netId");
                    if (DBG) {
                        Log.d(TAG, "SEC_COMMAND_ID_ANS_EXCEPTION_ANSWER : netId(" + netId +"), skipInternetCheck(" + keepConnection +")");
                    }
                    mWifiInfo.setSkipInternetCheck(keepConnection);
                    mWifiConfigManager.updateSkipInternetCheck(netId, keepConnection);
                }
                break;
            }
            // <<<WCM<<<
            default:
                return false;
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
            case WifiManager.SEC_COMMAND_ID_CHECK_BSS_SUPPORT_MU_MIMO: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_DYNAMIC_SET_MU_MIMO_MODE
                intResult = mWifiNative.checkMimoSupport(mInterfaceName);
                Log.d(TAG, "WifiManager.SEC_COMMAND_ID_GET_MU_MIMO_MODE : " + intResult);
                break;
            case WifiManager.SEC_COMMAND_ID_GET_MU_MIMO_MODE:  //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_DYNAMIC_SET_MU_MIMO_MODE
                intResult = mWifiNative.getMimoMode(mInterfaceName);
                Log.d(TAG, "WifiManager.SEC_COMMAND_ID_GET_MU_MIMO_MODE : " + intResult);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_MINIMIZE_RETRY: //SEC_PRODUCT_FEATURE_WLAN_ENABLE_TO_SET_MINIMIZE_RETRY
                args = (Bundle) innerMsg.obj;
                if (args != null) {
                    intResult = mWifiNative.setMinimizeRetry(
                            mInterfaceName,
                            args.getInt("enable") == 1 ? true : false);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_SET_AMPDU_MPDU: { //SEC_PRODUCT_FEATURE_WLAN_ENABLE_TO_SET_AMPDU
                args = (Bundle) innerMsg.obj;
                if (args != null) {
                    intResult = mWifiNative.setAmpdu(mInterfaceName, args.getInt("ampdu"));
                }
                break;
            }
            //TODO: case WifiManager.SEC_COMMAND_ID_GET_ROAM_SCAN_CONTROL:
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
                if (args != null)
                    intResult = mWifiNative.reAssoc(mInterfaceName , args.getString("param"));
                break;
            case WifiManager.SEC_COMMAND_ID_GET_ROAM_TRIGGER:
                intResult = mWifiNative.getRoamTrigger(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_ROAM_TRIGGER:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = (mWifiNative.setRoamTrigger(mInterfaceName,args.getInt("level")) ? 1 : 0);
                break;
            case WifiManager.SEC_COMMAND_ID_GET_ROAM_DELTA:
                intResult = mWifiNative.getRoamDelta(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_ROAM_DELTA:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = (mWifiNative.setRoamDelta(mInterfaceName,args.getInt("level")) ? 1 : 0);
                break;
            case WifiManager.SEC_COMMAND_ID_GET_ROAM_SCAN_PERIOD:
                intResult = mWifiNative.getRoamScanPeriod(mInterfaceName);
                break;
            case WifiManager.SEC_COMMAND_ID_SET_ROAM_SCAN_PERIOD:
                args = (Bundle) innerMsg.obj;
                if (args != null)
                    intResult = (mWifiNative.setRoamScanPeriod(mInterfaceName,args.getInt("time")) ? 1 : 0);
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
            //-SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_FMC
            // >>>WCM>>>
            case WifiManager.SEC_COMMAND_ID_SNS_DELETE_EXCLUDED:
                args = (Bundle) innerMsg.obj;
                Integer netId = args.getInt("excluded_networkId");
                if (DBG) {
                    Log.d(TAG, "SEC_COMMAND_ID_SNS_DELETE_EXCLUDED : netId(" + netId +"), delete excluded network");
                }
                if (netId == mWifiInfo.getNetworkId()) {
                    mWifiInfo.setSkipInternetCheck(false);
                }
                mWifiConfigManager.updateSkipInternetCheck(netId, false);
                WifiConfiguration currentConfig = mWifiConfigManager.getConfiguredNetwork(netId);
                
                Intent intent = new Intent();
                intent.setAction("ACTION_WCM_CONFIGURATION_CHANGED");
                sendBroadcastFromWifiStateMachine(intent);
                intResult = 1;
                break;
            case WifiManager.SEC_COMMAND_ID_SET_INTERNET_CHECK_OPTION:
                boolean mSkipInternetCheck = false;
                if (innerMsg.arg2 == 1) {
                    mSkipInternetCheck = true;
                } else {
                    mSkipInternetCheck = false;
                }
                mWifiConfigManager.updateSkipInternetCheck(innerMsg.arg1, mSkipInternetCheck);
                mWifiInfo.setSkipInternetCheck(mSkipInternetCheck);

                intResult = 1;
                break;
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
                if (mScanRequestProxy != null) {
                    stringResult = mScanRequestProxy.semDumpCachedScanController();
                }
                break;
            }
            case WifiManager.SEC_COMMAND_ID_GET_ISSUE_DETECTOR_DUMP: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_ISSUE_DETECTOR
                if (mIssueDetector != null) {
                    int size = innerMsg.arg1;
                    stringResult = mIssueDetector.getRawData(size == 0 ? 5 : size);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_GET_GEOFENCE_INFORMATION: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_SCAN_INTERVAL_USING_GEOFENCE
                stringResult = mWifiGeofenceManager.getGeofenceInformation();
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
                        if (mWifiNative.writeOutVendorConnFile(innerMsg.arg1, data)) {
                            stringResult = "OK";
                        }
                    }
                }
                break;
            case WifiManager.SEC_COMMAND_ID_TEST_GET_VENDOR_PROP: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLANTEST
                if (innerMsg.obj != null) {
                    Bundle prop_info = (Bundle) innerMsg.obj;
                    String prop_name = prop_info.getString("prop_name");
                    if (prop_name == null) break;
                    stringResult = mWifiNative.getVendorProperty(prop_name);
                }
                break;
            case WifiManager.SEC_COMMAND_ID_TEST_SET_VENDOR_PROP: //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_WLANTEST
                if (innerMsg.obj != null) {
                    Bundle prop_info = (Bundle) innerMsg.obj;
                    String prop_name = prop_info.getString("prop_name");
                    String data = prop_info.getString("data");
                    if (prop_name == null) break;
                    if (mWifiNative.setVendorProperty(prop_name, data)) {
                        stringResult = "OK";
                    }
                }
                break;
            case WifiManager.SEC_COMMAND_ID_WIFI_RECOMMENDATION_DUMP: //SEC_FLOATING_FEATURE_WLAN_SUPPORT_RECOMMEND_WIFI
                if (mWifiRecommendNetworkLevelController == null) {
                    mWifiRecommendNetworkLevelController = mWifiInjector.getWifiRecommendNetworkLevelController();
                }
                stringResult  = mWifiRecommendNetworkLevelController.getDynamicNetworkScoresForTest();
                break;
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

    public boolean getRssiPollingEnabledForIms() { //CscFeature_Wifi_SupportRssiPollStateDuringWifiCalling
        return mImsRssiPollingEnabled;
    }

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
            mIpClient.setHttpProxy(currentConfig.getHttpProxy());
        }
        if (!TextUtils.isEmpty(mTcpBufferSizes)) {
            mIpClient.setTcpBufferSizes(mTcpBufferSizes);
        }
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

    private void removePasspointNetworkForSimAbsent() {
        List<WifiConfiguration> savedNetworks = mWifiConfigManager.getConfiguredNetworks();
        for (WifiConfiguration network : savedNetworks) {
            if (!network.isPasspoint() || network.networkId == WifiConfiguration.INVALID_NETWORK_ID) {
                continue;
            }

            int currentEapMethod = network.enterpriseConfig.getEapMethod();
            if (TelephonyUtil.isSimEapMethod(currentEapMethod)) {
                Log.w(TAG, "removePasspointNetworkForSimAbsent : network "+network.configKey()+" try to remove");
                mWifiConfigManager.disableNetwork(network.networkId, network.creatorUid);
                mWifiConfigManager.removeNetwork(network.networkId, network.creatorUid);
            }
        }
    }

    public boolean getHotspot20State() {
        return mIsHs20Enabled;
    }

    private void updateHotspot20VendorSimState() {
        boolean isUseableVendorUsim = false;
        isUseableVendorUsim = TelephonyUtil.isVendorApUsimUseable(getTelephonyManager());
        Log.i(TAG, "updateHotspot20VendorSimState : " + isUseableVendorUsim);
        mPasspointManager.setHotspot20VendorSimState(isUseableVendorUsim);
        Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.WIFI_HOTSPOT20_USEABLE_VENDOR_USIM, isUseableVendorUsim ? 1 : 0);
    }
    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_HOTSPOT_20 -]

//+SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
    boolean checkAndShowSimRemovedDialog(WifiConfiguration config) {
        WifiEnterpriseConfig enterpriseConfig = config.enterpriseConfig;
        if (config.semIsVendorSpecificSsid
                && enterpriseConfig != null
                && (enterpriseConfig.getEapMethod() == WifiEnterpriseConfig.Eap.AKA
                    || enterpriseConfig.getEapMethod() == WifiEnterpriseConfig.Eap.AKA_PRIME)) {
            int simState = getTelephonyManager().getSimState();
            Log.i(TAG, "simState is " + simState + " for " + config.SSID);
            if (simState == TelephonyManager.SIM_STATE_ABSENT) {
                Log.d(TAG, "trying to connect without SIM, show alert dialog");
                SemWifiFrameworkUxUtils.showWarningDialog(mContext,
                        SemWifiFrameworkUxUtils.WARN_SIM_REMOVED,
                        new String[] {StringUtil.removeDoubleQuotes(config.SSID)});
                return true;
            }
        }
        return false;
    }

    void processCodeForEap(int code) { //TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE
        String content = String.valueOf(code);
        Log.i(TAG, "eap code : " + code + ", targetId: " + mTargetNetworkId);
        /* EapErrorCode: Error code for EAP or EAP Method as per RFC-4186
            SIM_GENERAL_FAILURE_AFTER_AUTH = 0,
            SIM_TEMPORARILY_DENIED = 1026,
            SIM_NOT_SUBSCRIBED = 1031,
            SIM_GENERAL_FAILURE_BEFORE_AUTH = 16384,
            SIM_VENDOR_SPECIFIC_EXPIRED_CERT = 16385,
            SIM_SUCCESS = 32768
        */
        if (code >= 0 && code < 32768) {
            if (CSC_WIFI_ERRORCODE) { //TAG_CSCFEATURE_WIFI_ENABLEDETAILEAPERRORCODESANDSTATE
                SemWifiFrameworkUxUtils.sendShowInfoIntent(mContext,
                        SemWifiFrameworkUxUtils.INFO_TYPE_EAP_MESSAGE, "message", content);
            } else if ("KTT".equals(CSC_CONFIG_EAP_AUTHMSG_POLICY)) { //TAG_CSCFEATURE_WIFI_CONFIGAUTHMSGDISPLAYPOLICY, KTT
                Intent ktIntent = new Intent(INTENT_KT_EAP_NOTIFICATION);
                ktIntent.putExtra("EAP_AKA_NOTIFICATION", content);
                sendBroadcastFromWifiStateMachine(ktIntent);
            }
        }
    }

    private int mLastEAPFailureNetworkId = -1;
    private int mLastEAPFailureCount = 0;
    private boolean checkAndRetryConnect(int targetNetworkId) { //SEC_PRODUCT_FEATURE_WLAN_EAP_XXX
        if (mLastEAPFailureNetworkId != targetNetworkId) {
            mLastEAPFailureNetworkId = targetNetworkId;
            mLastEAPFailureCount = 0;
        }
        if (++mLastEAPFailureCount > 3) {
            return false;
        }
        return true;
    }

    void processMessageForEap(String message) {
        Log.i(TAG, "eap message : " + message + ", targetId: " + mTargetNetworkId);
        String content = "";
        WifiConfiguration currentConfig = null;

        mWifiMetrics.logStaEvent(StaEvent.TYPE_EAP_ERROR, message);

        if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
            currentConfig = mWifiConfigManager.getConfiguredNetwork(mTargetNetworkId);
        }

        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM) { //SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM (3.1)
            if (message.startsWith(WPA_EVENT_EAP_TLS_CERT_ERROR)) {
                CertificatePolicy certPolicy = new CertificatePolicy();
                certPolicy.notifyCertificateFailureAsUser(CertificatePolicy.WIFI_MODULE, message, true, UserHandle.USER_OWNER);
                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_SECURITY,
                            false, TAG, "Certificate verification failed: " + message);
            } else if (message.startsWith(WPA_EVENT_EAP_TLS_ALERT) || message.startsWith(WPA_EVENT_EAP_TLS_HANDSHAKE_FAIL)) {
                // FAU_GEN.1/WLAN
                WifiMobileDeviceManager.auditLog(mContext, AuditLog.AUDIT_LOG_GROUP_SECURITY,
                            false, TAG, "EAP-TLS handshake failed: " + message);
            }
        }
        if (message.startsWith(WPA_EVENT_EAP_FAILURE)) {
            if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                updateSimNumber(mTargetNetworkId);
                if (currentConfig != null && currentConfig.enterpriseConfig != null) {
                    boolean hasEverConnected = currentConfig.getNetworkSelectionStatus().getHasEverConnected();
                    boolean isNetworkPermanentlyDisabled = currentConfig.getNetworkSelectionStatus().isNetworkPermanentlyDisabled();
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
                }
            }
        } else if (message.startsWith(WPA_EVENT_EAP_LOGGING)) {
            content = message.replaceAll(WPA_EVENT_EAP_LOGGING, "");
            if (currentConfig != null && currentConfig.enterpriseConfig != null) {
                sendEapLogging(currentConfig, EAP_LOGGING_STATE_NOTIFICATION, content.substring(5, content.length()));
            }
        } else if (message.startsWith(WPA_EVENT_EAP_NOTIFICATION)) {
            content = message.replaceAll(WPA_EVENT_EAP_NOTIFICATION, "");
            SemWifiFrameworkUxUtils.sendShowInfoIntent(mContext,
                    SemWifiFrameworkUxUtils.INFO_TYPE_EAP_MESSAGE, "message", content);
            if ("KTT".equals(CSC_CONFIG_EAP_AUTHMSG_POLICY)) { //TAG_CSCFEATURE_WIFI_CONFIGAUTHMSGDISPLAYPOLICY, KTT
                //KT spec 4.5.2.3.2 EAP_NOTIFICATION
                Intent ktIntent = new Intent(INTENT_KT_EAP_NOTIFICATION);
                String kt_msg = content;
                ktIntent.putExtra("EAP_NOTIFICATION", kt_msg);
                sendBroadcastFromWifiStateMachine(ktIntent);
            }
        } else if (message.startsWith(WPA_EVENT_EAP_KT_NOTIFICATION)) { //TAG_CSCFEATURE_WIFI_CONFIGAUTHMSGDISPLAYPOLICY, KTT
            content = message.replaceAll(WPA_EVENT_EAP_KT_NOTIFICATION, "");
            int err_code = -1;
            if (!TextUtils.isEmpty(content)) {
                err_code = Integer.parseInt(content);
            }
            logi("notification for KT, err_code= " + err_code);
            if (err_code == -1) {
                return;
            } else {
                Intent ktIntent = new Intent(INTENT_KT_EAP_NOTIFICATION);
                ktIntent.putExtra("ERROR_NOTIFICATION", err_code);
                if (err_code == KT_CODE_EAP_AUTH_FAIL) {
                    String kt_akamsg = mContext.getResources().getString(R.string.wifi_authentication_failed_kt);
                    ktIntent.putExtra("EAP_NOTIFICATION", kt_akamsg);
                }
                sendBroadcastFromWifiStateMachine(ktIntent);
            }
        } else if (message.startsWith(WPA_EVENT_EAP_ANONYMOUS_IDENTITY_UPDATED)) {
            if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
                updateAnonymousIdentity(mTargetNetworkId);
            }
        } else if (message.startsWith(WPA_EVENT_EAP_DEAUTH_8021X_AUTH_FAILED)) {
            content = message.replaceAll(WPA_EVENT_EAP_DEAUTH_8021X_AUTH_FAILED, "");
            if (currentConfig != null && currentConfig.enterpriseConfig != null) {
                if (TelephonyUtil.isSimEapMethod(currentConfig.enterpriseConfig.getEapMethod())) {
                    updateSimNumber(mTargetNetworkId);
                }
                boolean hasEverConnected = currentConfig.getNetworkSelectionStatus().getHasEverConnected();
                boolean isNetworkPermanentlyDisabled = currentConfig.getNetworkSelectionStatus().isNetworkPermanentlyDisabled();
                Log.i(TAG, "network "+currentConfig.configKey()+" has ever connected "+hasEverConnected+", isNetworkPermanentlyDisabled, "+isNetworkPermanentlyDisabled);
                if (!isNetworkPermanentlyDisabled) {
                    mWifiConfigManager.updateNetworkSelectionStatus(mTargetNetworkId,
                            WifiConfiguration.NetworkSelectionStatus.DISABLED_AUTHENTICATION_FAILURE);
                    if (checkAndRetryConnect(mTargetNetworkId)) {
                        Log.w(TAG, "update network status to auth failure , retry to conect ");
                        startConnectToNetwork(mTargetNetworkId, Process.WIFI_UID, SUPPLICANT_BSSID_ANY);
                    }
                    //for black list processing
                    Log.w(TAG, "trackBssid for 802.1x auth failure : "+content);
                    if (!TextUtils.isEmpty(content) && !"00:00:00:00:00:00".equals(content)) {
                        mWifiConnectivityManager.trackBssid(content, false, WifiConfiguration.NetworkSelectionStatus
                            .DISABLED_AUTHENTICATION_FAILURE);
                    }
                }
            }
        } else if (message.startsWith(WPA_EVENT_EAP_NO_CREDENTIALS)) {
            //TODO event process
        } else if (message.startsWith(WPA_EVENT_EAP_ERROR_MESSAGE)) {
            //TODO event process
        }
    }

    private boolean sendEapLogging(WifiConfiguration config, int state, String notification) {
        int typeOfEap = -1;
        typeOfEap = config.enterpriseConfig.getEapMethod();
        if (typeOfEap == -1 || config.SSID == null || config.SSID.isEmpty()) {
            return false;
        }
        String ssid = StringUtil.removeDoubleQuotes(config.SSID).replace(" ", "_").replace("\"", "_").replace("\\", "_").replace("/", "_") + " ";
        String typeOfPhase1 = config.enterpriseConfig.getPhase1Method();
        int typeOfPhase2 = config.enterpriseConfig.getPhase2Method();
        String useCaCerti = config.enterpriseConfig.getCaCertificateAlias();
        String useAnonymous = config.enterpriseConfig.getAnonymousIdentity();
        notification+=" ";

        Bundle args = new Bundle();
        StringBuilder sbet = new StringBuilder();
        args.putBoolean("bigdata", true); // variable that informing BigData logging
        args.putString("feature", "EAPT");

        //et_typ, et_pho, et_pht, et_cac, et_ani, et_sid, et_stt, et_not, et_kmt
        if (typeOfEap == WifiEnterpriseConfig.Eap.PEAP) sbet.append("PEAP ");
        else if (typeOfEap == WifiEnterpriseConfig.Eap.TLS) sbet.append("TLS ");
        else if (typeOfEap == WifiEnterpriseConfig.Eap.TTLS) sbet.append("TTLS ");
        else if (typeOfEap == WifiEnterpriseConfig.Eap.PWD) sbet.append("PWD ");
        else if (typeOfEap == WifiEnterpriseConfig.Eap.SIM) sbet.append("SIM ");
        else if (typeOfEap == WifiEnterpriseConfig.Eap.AKA) sbet.append("AKA ");
        else if (typeOfEap == WifiEnterpriseConfig.Eap.AKA_PRIME) sbet.append("AKAPRIME ");
        else if (typeOfEap == WifiEnterpriseConfig.Eap.FAST) sbet.append("FAST ");
        else if (typeOfEap == WifiEnterpriseConfig.Eap.LEAP) sbet.append("LEAP ");
        else sbet.append("typeOfEap_" + typeOfEap + " ");

        if (typeOfEap == WifiEnterpriseConfig.Eap.PEAP || typeOfEap == WifiEnterpriseConfig.Eap.TTLS || typeOfEap == WifiEnterpriseConfig.Eap.FAST) { //PEAP TTLS FAST
            if (typeOfPhase1.contains("0")) sbet.append("Provision0 ");
            else if (typeOfPhase1.contains("1")) sbet.append("Provision1 ");
            else if (typeOfPhase1.contains("2")) sbet.append("Provision2 ");
            else if (typeOfPhase1.contains("3")) sbet.append("Provision3 ");
            else sbet.append("NoOption "); // for PEAP TTLS

            if (typeOfPhase2 == WifiEnterpriseConfig.Phase2.NONE) sbet.append("None ");
            else if (typeOfPhase2 == WifiEnterpriseConfig.Phase2.PAP) sbet.append("PAP ");
            else if (typeOfPhase2 == WifiEnterpriseConfig.Phase2.MSCHAP) sbet.append("MSCHAP ");
            else if (typeOfPhase2 == WifiEnterpriseConfig.Phase2.MSCHAPV2) sbet.append("MSCHAPV2 ");
            else if (typeOfPhase2 == WifiEnterpriseConfig.Phase2.GTC) sbet.append("GTC ");
            else if (typeOfPhase2 == WifiEnterpriseConfig.Phase2.SIM) sbet.append("SIM ");
            else if (typeOfPhase2 == WifiEnterpriseConfig.Phase2.AKA) sbet.append("AKA ");
            else if (typeOfPhase2 == WifiEnterpriseConfig.Phase2.AKA_PRIME) sbet.append("AKAPRIME ");
            else sbet.append("typeOfPhase2_" + typeOfPhase2 + " ");

            if (!TextUtils.isEmpty(useCaCerti)) sbet.append("UseCerti ");
            else sbet.append("NoUseCerti ");

            if (!TextUtils.isEmpty(useAnonymous) && typeOfEap != 7) sbet.append("UseAnomymous ");
            else sbet.append("NoUseAnomyous ");
        } else {
            sbet.append("NoOption NoOption NoOption NoOption ");
        }

        sbet.append(ssid);

        if (state == EAP_LOGGING_STATE_CONNECTED) sbet.append("CONNECTED ");
        else if (state == EAP_LOGGING_STATE_AUTH_FAILURE) sbet.append("AUTH_FAILURE ");
        else if (state == EAP_LOGGING_STATE_ASSOC_REJECT) sbet.append("ASSOC_REJECT ");
        else if (state == EAP_LOGGING_STATE_DHCP_FAILURE) sbet.append("DHCP_FAILURE ");
        else if (state == EAP_LOGGING_STATE_NOTIFICATION) sbet.append("NOTIFICATION ");
        else sbet.append("WRONG_STATE ");

        sbet.append(notification);

        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP)) sbet.append("WPA_EAP ");
        else if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) sbet.append("IEEE8021X ");
        else sbet.append("WRONG_KEYMGMT ");

        log("sendEapLogging : EAPT " + sbet.toString());
        args.putString("data", sbet.toString()); // variable that informing BigData logging
        sendMessage(obtainMessage(CMD_SEC_LOGGING, 0, 0, args));
        return true;
    }
//-SEC_PRODUCT_FEATURE_WLAN_EAP_XXX

//+SEC_PRODUCT_FEATURE_WLAN_EAP_SIM
    private int getConfiguredSimNum(WifiConfiguration config) {
        int simNum = 1;
        String simNumStr = config.enterpriseConfig.getSimNumber();
        if (simNumStr != null && !simNumStr.isEmpty()) {
            Log.i(TAG, "getConfiguredSimNum() simNumStr:" + simNumStr);
            try {
                simNum = Integer.parseInt(simNumStr);
            } catch ( NumberFormatException e ) {
                Log.e(TAG, "getConfiguredSimNum() failed to getSimNumber ");
            }
        } else {
            Log.i(TAG, "getConfiguredSimNum() simNumStr empty");
        }
        Log.i(TAG, "getConfiguredSimNum() previous saved simNum:" + simNum);
        return simNum;
    }

    private void setPermanentIdentity(WifiConfiguration config) {
        if (mTargetNetworkId != WifiConfiguration.INVALID_NETWORK_ID) {
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
            Log.i(TAG, "setPermanentIdentity() set simNum:" + simNum);
            config.enterpriseConfig.setSimNumber(simNum);
            Pair<String, String> identityPair = TelephonyUtil.semGetSimIdentity(getTelephonyManager(), new TelephonyUtil(), config, simNum);
            if (identityPair != null && identityPair.first != null) {
                String oldIdentity = config.enterpriseConfig.getIdentity();
                if (oldIdentity != null && !oldIdentity.equals(identityPair.first) && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
                    Log.d(TAG, "setPermanentIdentity() Identity has been changed. setAnonymousIdentity to null for EAP method SIM/AKA/AKA'");
                    config.enterpriseConfig.setAnonymousIdentity(null);
                }
                String fullId = identityPair.first;
                String naiRealm = "";
                String imsi = "";
                int nai = fullId.indexOf('@');
                if (fullId.length() > 7 && nai > 0) {
                    imsi = fullId.substring(0, 7); // mcc mnc information only
                    naiRealm = fullId.substring(nai);
                }
                Log.d(TAG, "setPermanentIdentity() setIdentity identity : "+ imsi + "****"+naiRealm);
                config.enterpriseConfig.setIdentity(fullId);
            } else {
                Log.e(TAG, "setPermanentIdentity() Failed to get updated identity"
                        + " from supplicant, reset it in WifiConfiguration.");
                config.enterpriseConfig.setIdentity(null);
            }
            mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
        }else {
            Log.e(TAG, "setPermanentIdentity : mTargetNetworkId is INVALID_NETWORK_ID");
        }
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

    private void updateIdentityWithSimNumber(int netId, String identity, int simNum) {
        Log.i(TAG, "updateIdentityWithSimNumber() netId:" + netId + " simNum:" + simNum);
        // We need to get the updated identity from supplicant for EAP-SIM/AKA/AKA'
        // We need to get the updated simnumber from supplicant for EAP-SIM/AKA/AKA' and EAP-PEAP phase2 SIM/AKA/AKA'
        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(netId);
        if (config != null && config.enterpriseConfig != null) {
            if (TelephonyUtil.isSimConfig(config)) {
                Log.i(TAG, "updateIdentityWithSimNumber() setSimNumber " + simNum);
                config.enterpriseConfig.setSimNumber(simNum);
            }
            if (TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
                if (identity != null) {
                    Log.i(TAG, "updateIdentityWithSimNumber() setIdentity");
                    config.enterpriseConfig.setIdentity(identity);
                } else {
                    Log.i(TAG, "updateIdentityWithSimNumber() Failed to get updated identity"
                            + " from supplicant, reset it in WifiConfiguration.");
                    config.enterpriseConfig.setIdentity(null);
                }
            }
            mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
        } else {
            Log.i(TAG, "updateIdentityWithSimNumber() config is null");
        }
    }

    private void updateAnonymousIdentity(int netId) {
        Log.i(TAG, "updateAnonymousIdentity(" + netId + ")");
        WifiConfiguration config = mWifiConfigManager.getConfiguredNetwork(netId);
        // We need to get the updated pseudonym from supplicant for EAP-SIM/AKA/AKA'
        if (config != null && config.enterpriseConfig != null && TelephonyUtil.isSimEapMethod(config.enterpriseConfig.getEapMethod())) {
            String anonymousIdentity = mWifiNative.getEapAnonymousIdentity(mInterfaceName);
            if (anonymousIdentity != null) {
                Log.i(TAG, "updateAnonymousIdentity - anonymousIdentity : " + anonymousIdentity);
                config.enterpriseConfig.setAnonymousIdentity(anonymousIdentity);
            } else {
                Log.i(TAG, "Failed to get updated anonymous identity"
                        + " from supplicant, reset it in WifiConfiguration.");
                config.enterpriseConfig.setAnonymousIdentity(null);
            }
            mWifiConfigManager.addOrUpdateNetwork(config, Process.WIFI_UID);
        }
    }
//-SEC_PRODUCT_FEATURE_WLAN_EAP_SIM

//++SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS
    private boolean CheckIfDefaultGatewaySame(String defaultGatewayMacAddress) {
        boolean ret = false;
        String ssid = mWifiInfo.getSSID();
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

        if(inetAddress != null && gateway != null) {
            try {
                ArpPeer peer = new ArpPeer(mLinkProperties.getInterfaceName(), inetAddress, mWifiInfo.getMacAddress(), gateway);
                StringBuilder tmp_strbuilder = new StringBuilder();
                for(int i = 1; i <= 3; i++) {
                    if (defaultGatewayMacAddress != null) {
                        byte[] tmp_LastArpResults = peer.doArp(i*100, defaultGatewayMacAddress);
                        if (tmp_LastArpResults != null) {
                            mLastArpResultsForRoamingDhcp = null;
                            mLastArpResultsForRoamingDhcp  = macAddressFromArpResult(tmp_LastArpResults);
                            if (mLastArpResultsForRoamingDhcp != null) {
                                ret = true;
                                break;
                            }
                        }
                    } else {
                        Log.d(TAG, "defaultGatewayMacAddress is null");
                        break;
                    }
                }
                peer.close();
            } catch (SocketException e) {
                ret = false;
                Log.e(TAG, "SocketException: " + e);
            } catch (IllegalArgumentException e) {
                ret = false;
                Log.e(TAG, "IllegalArgumentException: " + e);
            }
        }

        Log.d(TAG, "CheckIfDefaultGatewaySame return " + ret);
        return ret;
    }

    private String macAddressFromArpResult(byte[] result) {
        StringBuilder tmp_strbuilder = new StringBuilder();
        for (int i = 0; i < 6;i++){
            try {
                String tmp_str = Integer.toHexString(result[i]);
                int length = tmp_str.length();
                if (length == 1) {
                    tmp_strbuilder.append("0").append(tmp_str);
                } else {
                    tmp_strbuilder.append(tmp_str.substring(length-2,length));
                }
                if ( i != 5){
                    tmp_strbuilder.append(":");
                }
            } catch (IndexOutOfBoundsException e) {
                Log.d(TAG,"macAddressFromArpResult indexoutofboundsexception");
                return null;
            }
        }
        return tmp_strbuilder.toString();
    }
//--SEC_PRODUCT_FEATURE_WLAN_SUPPORT_CACHED_DHCP_RESULTS

    private void setTcpBufferAndProxySettingsForIpClient() { //SAMSUNG_PATCH_WIFI_FRAMEWORK_HOTFIX
        WifiConfiguration currentConfig = getCurrentWifiConfiguration();
        if (currentConfig != null) {
            mIpClient.setHttpProxy(currentConfig.getHttpProxy());
        }
        if (!TextUtils.isEmpty(mTcpBufferSizes)) {
            mIpClient.setTcpBufferSizes(mTcpBufferSizes);
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
            String macAddress = mWifiNative.getVendorConnFileInfo(6 /*.mac.info*/);
            if (macAddress != null && macAddress.length() >= 17) {
                WifiChipInfo.getInstance().setMacAddress(macAddress.substring(0, 17));
                if (!mWifiInfo.hasRealMacAddress()) { //SEC_PRODUCT_FEATURE_WLAN_GET_MAC_ADDRESS_FROM_FILE
                    mWifiInfo.setMacAddress(macAddress.substring(0, 17));
                }
            }
            String softapInfo = mWifiNative.getVendorConnFileInfo(7 /*.softap.info*/);
            mWifiInjector.getSemWifiApChipInfo().readSoftApInfo(softapInfo); //SEC_PRODUCT_FEATURE_WLAN_SEC_MOBILEAP
            if (WifiChipInfo.getInstance().isReady()) {
                Log.d(TAG, "chipset information is ready");
            }
        }
    }

//++SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP
    private boolean checkIfForceRestartDhcp() {
        String ssid = mWifiInfo.getSSID();
        if ((ssid != null) && (ssid.contains("marente")  || ssid.contains("0001docomo") 
                || ssid.contains("ollehWiFi") || ssid.contains("olleh GiGA WiFi")
                || ssid.contains("KT WiFi")|| ssid.contains("KT GiGA WiFi"))) {
            if (DBG) log("ForceRestartDhcp");
            return true;
        }
        if (getDhcpRenewAfterRoamingMode() == 1) { //SEC_PRODUCT_FEATURE_WLAN_SAMSUNG_UREADY
            if (DBG) log("ForceRestartDhcp by uready");
            return true;
        }
        return false;
    }
//--SEC_PRODUCT_FEATURE_WLAN_ROAMING_DHCP

    boolean isRfTestMode() {
        return mSemSarManager.isRfTestMode();
    }

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

//+SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS
    private void setAnalyticsDhcpDisconnectReason(int reason) {
        switch (reason) {
            case IpClient.DHCP_TIMEOUT:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_DHCP_FAIL_TIME_OUT);
                break;
            case IpClient.ROAMING_DHCP_TIMEOUT:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_DHCP_FAIL_TIME__ROAMING);
                break;
            case IpClient.LEASE_EXPIRED:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_DHCP_FAIL_LEASE_EXPIRED);
                break;
            case IpClient.NAK_IN_RENEW:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_DHCP_FAIL_NACK_IN_RENEW);
                break;
            case IpClient.RENEW_LEASE_WRONG_IP:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_DHCP_FAIL_RENEW_LEASE_WROING_IP);
                break;
            case IpClient.UDP_SOCK_CONNECT_FAIL:
            case IpClient.INIT_IFACE_OR_SOCKET:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_DHCP_FAIL_INTERNAL);
                break;
            case IpClient.NAK_IN_ROAMING_DHCP:
            case IpClient.NAK_IN_PRE_REQUEST :
            case IpManagerEvent.PROVISIONING_OK:
            case IpManagerEvent.PROVISIONING_FAIL:
            case IpManagerEvent.COMPLETE_LIFECYCLE:
            case IpManagerEvent.ERROR_STARTING_IPV4:
            case IpManagerEvent.ERROR_STARTING_IPV6:
            case IpManagerEvent.ERROR_STARTING_IPREACHABILITYMONITOR:
            case IpManagerEvent.ERROR_INVALID_PROVISIONING:
            case IpManagerEvent.ERROR_INTERFACE_NOT_FOUND:
                return;
            default:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_DHCP_FAIL_UNSPECIFIED);
                break;                
        }
    }

    private void setAnalyticsUserDisconnectReason(short reason) {
        mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, reason);
    }

    // >>>WCM>>>
    private void setAnalyticsNoInternetDisconnectReason(int reason) {
        switch (reason) {
            case WifiConnectivityMonitor.ANALYTICS_DISCONNECT_REASON_RESERVED:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_NO_INTERNET_UNSPECIFIED);
                break;
            case WifiConnectivityMonitor.ANALYTICS_DISCONNECT_REASON_DNS_PRIVATE_IP:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_NO_INTERNET_DNS_PRIVATE_IP);
                break;
            case WifiConnectivityMonitor.ANALYTICS_DISCONNECT_REASON_DNS_DNS_REFUSED:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_NO_INTERNET_DNS_DNS_REFUSE);
                break;
            case WifiConnectivityMonitor.ANALYTICS_DISCONNECT_REASON_ARP_NO_RESPONSE:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_NO_INTERNET_ARP_NO_RESP);
                break;
            default:
                mWifiNative.setAnalyticsDisconnectReason(mInterfaceName, WifiNative.ANALYTICS_DISCONNECT_REASON_NO_INTERNET_UNSPECIFIED);
                break; 
        }
    }
    // <<<WCM<<<

//-SEC_PRODUCT_FEATURE_WLAN_CONFIG_ANALYTICS

    // SWITCH_FOR_INDIVIDUAL_APPS_FEATURE - START
    public int getBeaconCount() {
        return mRunningBeaconCount;
    }
    // SWITCH_FOR_INDIVIDUAL_APPS_FEATURE - END

    //DUPLICATED_IP_USING_DETECTION -- Start
    public void duplicatedIpChecker() {

        InetAddress inetAddress = null;
        ArrayList<String[]> sendArpResult = new ArrayList<String[]>();

        for (LinkAddress la : mLinkProperties.getLinkAddresses()) {
            if (la.getAddress() instanceof Inet4Address) {
                inetAddress = la.getAddress();
                break;
            }
        }
        if (mLinkProperties != null && mLinkProperties.getInterfaceName() != null && inetAddress != null) {
            try {
                ArpPeer conflictPeer = new ArpPeer(mLinkProperties.getInterfaceName(), inetAddress, mWifiInfo.getMacAddress());
                conflictPeer.sendGarp(mContext);
                checkDuplcatedIp(700);
            } catch (SocketException e) {
                Log.e(TAG, "SocketException: " + e);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException: " + e);
            }
        }
    }
    //DUPLICATED_IP_USING_DETECTION -- END

    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
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
    //SEC_PRODUCT_FEATURE_WLAN_SUPPORT_TIPS_VERSION 1.3 - LOCATION
}
