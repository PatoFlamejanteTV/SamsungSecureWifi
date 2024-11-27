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

package com.android.server.connectivity;

import static android.Manifest.permission.BIND_VPN_SERVICE;
import static android.net.ConnectivityManager.NETID_UNSET;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING;
import static android.net.RouteInfo.RTN_THROW;
import static android.net.RouteInfo.RTN_UNREACHABLE;

import android.Manifest;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.UserIdInt;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.INetworkManagementEventObserver;
import android.net.IpPrefix;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Network;
import android.net.NetworkAgent;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkMisc;
import android.net.NetworkUtils;
import android.net.RouteInfo;
import android.net.UidRange;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemService;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.security.Credentials;
import android.security.KeyStore;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;

import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.messages.nano.SystemMessageProto.SystemMessage;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.ArrayUtils;
import com.android.server.ConnectivityService;
import com.android.server.DeviceIdleController;
import com.android.server.LocalServices;
import com.android.server.net.BaseNetworkObserver;
import com.android.internal.util.CollectionUtils;

import libcore.io.IoUtils;

// SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
import com.samsung.android.knox.net.vpn.VpnPolicy;
import com.samsung.android.knox.net.vpn.KnoxVpnPolicyConstants;
import com.samsung.android.knox.net.vpn.IKnoxVpnPolicy;
import com.android.server.enterprise.vpn.knoxvpn.KnoxVpnConstants;
import android.os.ServiceManager;
import com.sec.android.app.SecProductFeature_KNOX;
import com.android.server.net.LockdownVpnTracker;
// SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }

// SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
import com.samsung.android.knox.EnterpriseDeviceManager;
import com.samsung.android.knox.SemPersonaManager;
import android.sec.enterprise.auditlog.AuditLog;
import android.sec.enterprise.auditlog.AuditEvents;
// SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

// strongSwan support - START
import com.sec.android.app.SecProductFeature_SECURITY;
import java.net.NetworkInterface;
import java.util.Enumeration;
import android.security.KeyChain;
import android.os.SystemProperties;
import com.samsung.android.security.mdf.MdfUtils;
import com.android.server.net.LockdownVpnTracker;
// strongSwan support - END
import com.samsung.android.emergencymode.SemEmergencyConstants;
import com.samsung.android.emergencymode.SemEmergencyManager;

//KNOX CCM {
import android.util.Base64;
import com.sec.tima_keychain.TimaKeychain;

import java.security.cert.X509Certificate;
//KNOX CCM }

// Secure Wi-Fi - START
import com.samsung.android.knox.SemPersonaManager;
// Secure Wi-Fi - END

// SEC VPN {
import com.samsung.android.app.SemDualAppManager;
// } SEC VPN 

/**
 * @hide
 */
public class Vpn {
    private static final String NETWORKTYPE = "VPN";
    private static final String TAG = "Vpn";
    private static final boolean LOGD = true;

    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
    private VpnPolicy mVpnPolicy;
    private EnterpriseDeviceManager mEDM;
    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }

    // Length of time (in milliseconds) that an app hosting an always-on VPN is placed on
    // the device idle whitelist during service launch and VPN bootstrap.
    private static final long VPN_LAUNCH_IDLE_WHITELIST_DURATION_MS = 60 * 1000;

    // Settings for how much of the address space should be routed so that Vpn considers
    // "most" of the address space is routed. This is used to determine whether this Vpn
    // should be marked with the INTERNET capability.
    private static final long MOST_IPV4_ADDRESSES_COUNT;
    private static final BigInteger MOST_IPV6_ADDRESSES_COUNT;
    static {
        // 85% of the address space must be routed for Vpn to consider this VPN to provide
        // INTERNET access.
        final int howManyPercentIsMost = 85;

        final long twoPower32 = 1L << 32;
        MOST_IPV4_ADDRESSES_COUNT = twoPower32 * howManyPercentIsMost / 100;
        final BigInteger twoPower128 = BigInteger.ONE.shiftLeft(128);
        MOST_IPV6_ADDRESSES_COUNT = twoPower128
                .multiply(BigInteger.valueOf(howManyPercentIsMost))
                .divide(BigInteger.valueOf(100));
    }
    // How many routes to evaluate before bailing and declaring this Vpn should provide
    // the INTERNET capability. This is necessary because computing the address space is
    // O(n²) and this is running in the system service, so a limit is needed to alleviate
    // the risk of attack.
    // This is taken as a total of IPv4 + IPV6 routes for simplicity, but the algorithm
    // is actually O(n²)+O(n²).
    private static final int MAX_ROUTES_TO_EVALUATE = 150;

    // TODO: create separate trackers for each unique VPN to support
    // automated reconnection

    private final Context mContext;
    private final NetworkInfo mNetworkInfo;
    private String mPackage;
    private int mOwnerUID;
    private boolean mIsPackageTargetingAtLeastQ;
    private String mInterface;
    private Connection mConnection;
    private LegacyVpnRunner mLegacyVpnRunner;
    private String mLegacyAddress;
    private volatile boolean mEnableLockdownVpn = false;
    private PendingIntent mStatusIntent;
    private volatile boolean mEnableTeardown = true;
    private final INetworkManagementService mNetd;
    @VisibleForTesting
    protected VpnConfig mConfig;
    @VisibleForTesting
    protected NetworkAgent mNetworkAgent;
    private final Looper mLooper;
    @VisibleForTesting
    protected final NetworkCapabilities mNetworkCapabilities;
    private final SystemServices mSystemServices;

    /**
     * Whether to keep the connection active after rebooting, or upgrading or reinstalling. This
     * only applies to {@link VpnService} connections.
     */
    private boolean mAlwaysOn = false;

    /**
     * Whether to disable traffic outside of this VPN even when the VPN is not connected. System
     * apps can still bypass by choosing explicit networks. Has no effect if {@link mAlwaysOn} is
     * not set.
     */
    private boolean mLockdown = false;

    /**
     * Set of packages in addition to the VPN app itself that can access the network directly when
     * VPN is not connected even if {@code mLockdown} is set.
     */
    private @NonNull List<String> mLockdownWhitelist = Collections.emptyList();

    /**
     * List of UIDs for which networking should be blocked until VPN is ready, during brief periods
     * when VPN is not running. For example, during system startup or after a crash.
     * @see mLockdown
     */
    @GuardedBy("this")
    private Set<UidRange> mBlockedUsers = new ArraySet<>();

    // Handle of the user initiating VPN.
    private final int mUserHandle;

    // strongSwan - START
    private static final String CC_PROPERTY = "security.mdf";
    private String mEgressIface;
    private VpnRules mVpnRules;
    // strongSwan - END

    //KNOX CCM {
    private static final String KNOX_TAG = " [Knox]";
    private static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String PEM_CERT_HEADER = "-----BEGIN CERTIFICATE-----\n";
    private static final String PEM_CERT_FOOTER = "-----END CERTIFICATE-----";
    private static final int PEM_CERT_LINE_CHARS = 64;
    //KNOX CCM }

    // Secure Wi-Fi - START
    private static final String FAST_PACKAGE_NAME = "com.samsung.android.fast";
    // Secure Wi-Fi - END

    // SEC VPN {
    // Listen locale changed to update notification
    private volatile int mLockdownNotiTitleRes, mLockdownNotiIconRes;
    private boolean mIsLocaleChangedReceiverRegistered = false;
    private final BroadcastReceiver mLocaleChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) return;
            switch(action) {
                case Intent.ACTION_LOCALE_CHANGED:
                    Log.i(TAG, "Locale changed!");
                    if (mEnableLockdownVpn) {
                        // Legacy Always-on
                        showNotificationForLockdown(mLockdownNotiTitleRes, mLockdownNotiIconRes);
                    } else if (mAlwaysOn && mNetworkInfo.getDetailedState() != DetailedState.CONNECTED) {
                        // 3rd-party Always-on which not connected yet
                        updateAlwaysOnNotification(mNetworkInfo.getDetailedState());
                    } else if (mConfig != null && mConfig.legacy) {
                        // Legacy VPN connected
                        showNotificationForLegacy(mUserHandle);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private void registerLocaleChangedReceiver() {
        if (!mIsLocaleChangedReceiverRegistered) {
            mContext.registerReceiver(mLocaleChangedReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
            mIsLocaleChangedReceiverRegistered = true;
        }
    }
    private void unregisterLocaleChangedReceiver() {
        if (mIsLocaleChangedReceiverRegistered && mLocaleChangedReceiver != null) {
            try {
                mContext.unregisterReceiver(mLocaleChangedReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to unregister LocaleChangedReceiver");
            }
            mIsLocaleChangedReceiverRegistered = false;
        }
    }
    // } SEC VPN

    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
    private static final boolean DBG = android.os.Debug.semIsProductDev();
    private static final boolean PER_APP_VPN  = false;
    private static final boolean SYSTEM_VPN  = true;
    private String mProfileName = null;
    private String mAddress = null;
    private String mV6Address = null;
    private int KNOXVPN_FEATURE = -1;
    private int mTun_type = KnoxVpnConstants.KNOX_VPN_INTERFACE_NONE;
    private final int KNOXVPN_MDM_ENABLED = 1;
    private final int KNOXVPN_CONTAINER_ENABLED = 2;
    private IKnoxVpnPolicy mKnoxVpnService;
    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }

    public Vpn(Looper looper, Context context, INetworkManagementService netService,
            @UserIdInt int userHandle) {
        this(looper, context, netService, userHandle, new SystemServices(context));
    }

    @VisibleForTesting
    protected Vpn(Looper looper, Context context, INetworkManagementService netService,
            int userHandle, SystemServices systemServices) {
        mContext = context;
        mNetd = netService;
        mUserHandle = userHandle;
        mLooper = looper;
        mSystemServices = systemServices;

        mPackage = VpnConfig.LEGACY_VPN;
        mOwnerUID = getAppUid(mPackage, mUserHandle);
        mVpnRules = new VpnRules();
        mIsPackageTargetingAtLeastQ = doesPackageTargetAtLeastQ(mPackage);

        try {
            netService.registerObserver(mObserver);
        } catch (RemoteException e) {
            Log.wtf(TAG, "Problem registering observer", e);
        }

        mNetworkInfo = new NetworkInfo(ConnectivityManager.TYPE_VPN, 0 /* subtype */, NETWORKTYPE,
                "" /* subtypeName */);
        mNetworkCapabilities = new NetworkCapabilities();
        mNetworkCapabilities.addTransportType(NetworkCapabilities.TRANSPORT_VPN);
        mNetworkCapabilities.removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN);
        updateCapabilities(null /* defaultNetwork */);

        loadAlwaysOnPackage();
    }

    /**
     * Set whether this object is responsible for watching for {@link NetworkInfo}
     * teardown. When {@code false}, teardown is handled externally by someone
     * else.
     */
    public void setEnableTeardown(boolean enableTeardown) {
        mEnableTeardown = enableTeardown;
    }

    /**
     * Set if this object try to connect Always-on VPN.
     */
    public void setEnableLockdownVpn(boolean enableLockdownVpn) {
        mEnableLockdownVpn = enableLockdownVpn;
    }

    /**
     * Update current state, dispaching event to listeners.
     */
    @VisibleForTesting
    protected void updateState(DetailedState detailedState, String reason) {
        if (LOGD) Log.d(TAG, "setting state=" + detailedState + ", reason=" + reason);
        mNetworkInfo.setDetailedState(detailedState, reason, null);
        if (mNetworkAgent != null) {
            mNetworkAgent.sendNetworkInfo(mNetworkInfo);
        }
        // SEC VPN { Fix to attempt to connect LockdownVpn up to 4 times
        else if (mEnableLockdownVpn && detailedState == DetailedState.FAILED) {
            long token = Binder.clearCallingIdentity();
            try {
                mNetworkAgent = new NetworkAgent(mLooper, mContext, NETWORKTYPE, mNetworkInfo, mNetworkCapabilities, new LinkProperties(), 0, null) {
                    @Override
                    public void unwanted() {}
                };
            } finally {
                Binder.restoreCallingIdentity(token);
            }
            mNetworkAgent.sendNetworkInfo(mNetworkInfo);
            mNetworkAgent = null;
        }
        // } SEC VPN
        updateAlwaysOnNotification(detailedState);
    }

    /**
     * Updates {@link #mNetworkCapabilities} based on current underlying networks and returns a
     * defensive copy.
     *
     * <p>Does not propagate updated capabilities to apps.
     *
     * @param defaultNetwork underlying network for VPNs following platform's default
     */
    public synchronized NetworkCapabilities updateCapabilities(
            @Nullable Network defaultNetwork) {
        if (mConfig == null) {
            // VPN is not running.
            return null;
        }

        Network[] underlyingNetworks = mConfig.underlyingNetworks;
        if (underlyingNetworks == null && defaultNetwork != null) {
            // null underlying networks means to track the default.
            underlyingNetworks = new Network[] { defaultNetwork };
        }
        // Only apps targeting Q and above can explicitly declare themselves as metered.
        final boolean isAlwaysMetered = mIsPackageTargetingAtLeastQ && mConfig.isMetered;

        applyUnderlyingCapabilities(
                mContext.getSystemService(ConnectivityManager.class),
                underlyingNetworks,
                mNetworkCapabilities,
                isAlwaysMetered);

        return new NetworkCapabilities(mNetworkCapabilities);
    }

    @VisibleForTesting
    public static void applyUnderlyingCapabilities(
            ConnectivityManager cm,
            Network[] underlyingNetworks,
            NetworkCapabilities caps,
            boolean isAlwaysMetered) {
        int[] transportTypes = new int[] { NetworkCapabilities.TRANSPORT_VPN };
        int downKbps = NetworkCapabilities.LINK_BANDWIDTH_UNSPECIFIED;
        int upKbps = NetworkCapabilities.LINK_BANDWIDTH_UNSPECIFIED;
        // VPN's meteredness is OR'd with isAlwaysMetered and meteredness of its underlying
        // networks.
        boolean metered = isAlwaysMetered;
        boolean roaming = false;
        boolean congested = false;

        boolean hadUnderlyingNetworks = false;
        if (null != underlyingNetworks) {
            for (Network underlying : underlyingNetworks) {
                // TODO(b/124469351): Get capabilities directly from ConnectivityService instead.
                final NetworkCapabilities underlyingCaps = cm.getNetworkCapabilities(underlying);
                if (underlyingCaps == null) continue;
                hadUnderlyingNetworks = true;
                for (int underlyingType : underlyingCaps.getTransportTypes()) {
                    transportTypes = ArrayUtils.appendInt(transportTypes, underlyingType);
                }

                // When we have multiple networks, we have to assume the
                // worst-case link speed and restrictions.
                downKbps = NetworkCapabilities.minBandwidth(downKbps,
                        underlyingCaps.getLinkDownstreamBandwidthKbps());
                upKbps = NetworkCapabilities.minBandwidth(upKbps,
                        underlyingCaps.getLinkUpstreamBandwidthKbps());
                metered |= !underlyingCaps.hasCapability(NET_CAPABILITY_NOT_METERED);
                roaming |= !underlyingCaps.hasCapability(NET_CAPABILITY_NOT_ROAMING);
                congested |= !underlyingCaps.hasCapability(NET_CAPABILITY_NOT_CONGESTED);
            }
        }
        if (!hadUnderlyingNetworks) {
            // No idea what the underlying networks are; assume sane defaults
            metered = true;
            roaming = false;
            congested = false;
        }

        caps.setTransportTypes(transportTypes);
        caps.setLinkDownstreamBandwidthKbps(downKbps);
        caps.setLinkUpstreamBandwidthKbps(upKbps);
        caps.setCapability(NET_CAPABILITY_NOT_METERED, !metered);
        caps.setCapability(NET_CAPABILITY_NOT_ROAMING, !roaming);
        caps.setCapability(NET_CAPABILITY_NOT_CONGESTED, !congested);
    }

    /**
     * Chooses whether to force all connections to go though VPN.
     *
     * Used to enable/disable legacy VPN lockdown.
     *
     * This uses the same ip rule mechanism as
     * {@link #setAlwaysOnPackage(String, boolean, List<String>)}; previous settings from calling
     * that function will be replaced and saved with the always-on state.
     *
     * @param lockdown whether to prevent all traffic outside of a VPN.
     */
    public synchronized void setLockdown(boolean lockdown) {
        enforceControlPermissionOrInternalCaller();

        setVpnForcedLocked(lockdown);
        mLockdown = lockdown;

        // Update app lockdown setting if it changed. Legacy VPN lockdown status is controlled by
        // LockdownVpnTracker.isEnabled() which keeps track of its own state.
        if (mAlwaysOn) {
            saveAlwaysOnPackage();
        }
    }

    /**
     * Check whether to prevent all traffic outside of a VPN even when the VPN is not connected.
     *
     * @return {@code true} if VPN lockdown is enabled.
     */
    public synchronized boolean getLockdown() {
        return mLockdown;
    }

    /**
     * Returns whether VPN is configured as always-on.
     */
    public synchronized boolean getAlwaysOn() {
        return mAlwaysOn;
    }

    /**
     * Checks if a VPN app supports always-on mode.
     *
     * In order to support the always-on feature, an app has to
     * <ul>
     *     <li>target {@link VERSION_CODES#N API 24} or above, and
     *     <li>not opt out through the {@link VpnService#SERVICE_META_DATA_SUPPORTS_ALWAYS_ON}
     *         meta-data field.
     * </ul>
     *
     * @param packageName the canonical package name of the VPN app
     * @return {@code true} if and only if the VPN app exists and supports always-on mode
     */
    public boolean isAlwaysOnPackageSupported(String packageName) {
        enforceSettingsPermission();

        if (packageName == null) {
            return false;
        }

        PackageManager pm = mContext.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfoAsUser(packageName, 0 /*flags*/, mUserHandle);
        } catch (NameNotFoundException unused) {
            Log.w(TAG, "Can't find \"" + packageName + "\" when checking always-on support");
        }
        if (appInfo == null || appInfo.targetSdkVersion < VERSION_CODES.N) {
            return false;
        }

        final Intent intent = new Intent(VpnConfig.SERVICE_INTERFACE);
        intent.setPackage(packageName);
        List<ResolveInfo> services =
                pm.queryIntentServicesAsUser(intent, PackageManager.GET_META_DATA, mUserHandle);
        if (services == null || services.size() == 0) {
            return false;
        }

        for (ResolveInfo rInfo : services) {
            final Bundle metaData = rInfo.serviceInfo.metaData;
            if (metaData != null &&
                    !metaData.getBoolean(VpnService.SERVICE_META_DATA_SUPPORTS_ALWAYS_ON, true)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Configures an always-on VPN connection through a specific application.
     * This connection is automatically granted and persisted after a reboot.
     *
     * <p>The designated package should exist and declare a {@link VpnService} in its
     *    manifest guarded by {@link android.Manifest.permission.BIND_VPN_SERVICE},
     *    otherwise the call will fail.
     *
     * <p>Note that this method does not check if the VPN app supports always-on mode. The check is
     *    delayed to {@link #startAlwaysOnVpn()}, which is always called immediately after this
     *    method in {@link android.net.IConnectivityManager#setAlwaysOnVpnPackage}.
     *
     * @param packageName the package to designate as always-on VPN supplier.
     * @param lockdown whether to prevent traffic outside of a VPN, for example while connecting.
     * @param lockdownWhitelist packages to be whitelisted from lockdown.
     * @return {@code true} if the package has been set as always-on, {@code false} otherwise.
     */
    public synchronized boolean setAlwaysOnPackage(
            String packageName, boolean lockdown, List<String> lockdownWhitelist) {
        enforceControlPermissionOrInternalCaller();

        if (setAlwaysOnPackageInternal(packageName, lockdown, lockdownWhitelist)) {
            saveAlwaysOnPackage();
            return true;
        }
        return false;
    }

    /**
     * Configures an always-on VPN connection through a specific application, the same as
     * {@link #setAlwaysOnPackage}.
     *
     * Does not perform permission checks. Does not persist any of the changes to storage.
     *
     * @param packageName the package to designate as always-on VPN supplier.
     * @param lockdown whether to prevent traffic outside of a VPN, for example while connecting.
     * @param lockdownWhitelist packages to be whitelisted from lockdown. This is only used if
     *        {@code lockdown} is {@code true}. Packages must not contain commas.
     * @return {@code true} if the package has been set as always-on, {@code false} otherwise.
     */
    @GuardedBy("this")
    private boolean setAlwaysOnPackageInternal(
            String packageName, boolean lockdown, List<String> lockdownWhitelist) {
        if (VpnConfig.LEGACY_VPN.equals(packageName)) {
            Log.w(TAG, "Not setting legacy VPN \"" + packageName + "\" as always-on.");
            return false;
        }

        if (lockdownWhitelist != null) {
            for (String pkg : lockdownWhitelist) {
                if (pkg.contains(",")) {
                    Log.w(TAG, "Not setting always-on vpn, invalid whitelisted package: " + pkg);
                    return false;
                }
            }
        }

        if (packageName != null) {
            // Pre-authorize new always-on VPN package.
            if (!setPackageAuthorization(packageName, true)) {
                return false;
            }
            mAlwaysOn = true;
        } else {
            packageName = VpnConfig.LEGACY_VPN;
            mAlwaysOn = false;
        }

        mLockdown = (mAlwaysOn && lockdown);
        mLockdownWhitelist = (mLockdown && lockdownWhitelist != null)
                ? Collections.unmodifiableList(new ArrayList<>(lockdownWhitelist))
                : Collections.emptyList();

        if (isCurrentPreparedPackage(packageName)) {
            updateAlwaysOnNotification(mNetworkInfo.getDetailedState());
            setVpnForcedLocked(mLockdown);
        } else {
            // Prepare this app. The notification will update as a side-effect of updateState().
            // It also calls setVpnForcedLocked().
            Log.w(TAG, "Prepare Internal " + packageName + " as always-on.");
            prepareInternal(packageName);
        }
        return true;
    }

    private static boolean isNullOrLegacyVpn(String packageName) {
        return packageName == null || VpnConfig.LEGACY_VPN.equals(packageName);
    }

    /**
     * @return the package name of the VPN controller responsible for always-on VPN,
     *         or {@code null} if none is set or always-on VPN is controlled through
     *         lockdown instead.
     */
    public synchronized String getAlwaysOnPackage() {
        enforceControlPermissionOrInternalCaller();
        return (mAlwaysOn ? mPackage : null);
    }

    /**
     * @return an immutable list of packages whitelisted from always-on VPN lockdown.
     */
    public synchronized List<String> getLockdownWhitelist() {
        return mLockdown ? mLockdownWhitelist : null;
    }

    /**
     * Save the always-on package and lockdown config into Settings.Secure
     */
    @GuardedBy("this")
    private void saveAlwaysOnPackage() {
        final long token = Binder.clearCallingIdentity();
        try {
            mSystemServices.settingsSecurePutStringForUser(Settings.Secure.ALWAYS_ON_VPN_APP,
                    getAlwaysOnPackage(), mUserHandle);
            mSystemServices.settingsSecurePutIntForUser(Settings.Secure.ALWAYS_ON_VPN_LOCKDOWN,
                    (mAlwaysOn && mLockdown ? 1 : 0), mUserHandle);
            mSystemServices.settingsSecurePutStringForUser(
                    Settings.Secure.ALWAYS_ON_VPN_LOCKDOWN_WHITELIST,
                    String.join(",", mLockdownWhitelist), mUserHandle);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * Load the always-on package and lockdown config from Settings.Secure
     */
    @GuardedBy("this")
    private void loadAlwaysOnPackage() {
        final long token = Binder.clearCallingIdentity();
        try {
            final String alwaysOnPackage = mSystemServices.settingsSecureGetStringForUser(
                    Settings.Secure.ALWAYS_ON_VPN_APP, mUserHandle);
            final boolean alwaysOnLockdown = mSystemServices.settingsSecureGetIntForUser(
                    Settings.Secure.ALWAYS_ON_VPN_LOCKDOWN, 0 /*default*/, mUserHandle) != 0;
            final String whitelistString = mSystemServices.settingsSecureGetStringForUser(
                    Settings.Secure.ALWAYS_ON_VPN_LOCKDOWN_WHITELIST, mUserHandle);
            final List<String> whitelistedPackages = TextUtils.isEmpty(whitelistString)
                    ? Collections.emptyList() : Arrays.asList(whitelistString.split(","));
            setAlwaysOnPackageInternal(alwaysOnPackage, alwaysOnLockdown, whitelistedPackages);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * @return {@code true} if the service was started, the service was already connected, or there
     *         was no always-on VPN to start. {@code false} otherwise.
     */
    public boolean startAlwaysOnVpn() {
        final String alwaysOnPackage;
        synchronized (this) {
            alwaysOnPackage = getAlwaysOnPackage();
            // Skip if there is no service to start.
            if (alwaysOnPackage == null) {
                return true;
            }
            // Remove always-on VPN if it's not supported.
            if (!isAlwaysOnPackageSupported(alwaysOnPackage)) {
                setAlwaysOnPackage(null, false, null);
                return false;
            }
            // Skip if the service is already established. This isn't bulletproof: it's not bound
            // until after establish(), so if it's mid-setup onStartCommand will be sent twice,
            // which may restart the connection.
            if (getNetworkInfo().isConnected()) {
                return true;
            }
        }

        // Tell the OS that background services in this app need to be allowed for
        // a short time, so we can bootstrap the VPN service.
        final long oldId = Binder.clearCallingIdentity();
        try {
            DeviceIdleController.LocalService idleController =
                    LocalServices.getService(DeviceIdleController.LocalService.class);
            idleController.addPowerSaveTempWhitelistApp(Process.myUid(), alwaysOnPackage,
                    VPN_LAUNCH_IDLE_WHITELIST_DURATION_MS, mUserHandle, false, "vpn");

            // Start the VPN service declared in the app's manifest.
            Intent serviceIntent = new Intent(VpnConfig.SERVICE_INTERFACE);
            serviceIntent.setPackage(alwaysOnPackage);
            try {
                return mContext.startServiceAsUser(serviceIntent, UserHandle.of(mUserHandle)) != null;
            } catch (RuntimeException e) {
                Log.e(TAG, "VpnService " + serviceIntent + " failed to start", e);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(oldId);
        }
    }

    /**
     * Prepare for a VPN application. This method is designed to solve
     * race conditions. It first compares the current prepared package
     * with {@code oldPackage}. If they are the same, the prepared
     * package is revoked and replaced with {@code newPackage}. If
     * {@code oldPackage} is {@code null}, the comparison is omitted.
     * If {@code newPackage} is the same package or {@code null}, the
     * revocation is omitted. This method returns {@code true} if the
     * operation is succeeded.
     *
     * Legacy VPN is handled specially since it is not a real package.
     * It uses {@link VpnConfig#LEGACY_VPN} as its package name, and
     * it can be revoked by itself.
     *
     * Note: when we added VPN pre-consent in
     * https://android.googlesource.com/platform/frameworks/base/+/0554260
     * the names oldPackage and newPackage became misleading, because when
     * an app is pre-consented, we actually prepare oldPackage, not newPackage.
     *
     * Their meanings actually are:
     *
     * - oldPackage non-null, newPackage null: App calling VpnService#prepare().
     * - oldPackage null, newPackage non-null: ConfirmDialog calling prepareVpn().
     * - oldPackage null, newPackage=LEGACY_VPN: Used internally to disconnect
     *   and revoke any current app VPN and re-prepare legacy vpn.
     *
     * TODO: Rename the variables - or split this method into two - and end this confusion.
     * TODO: b/29032008 Migrate code from prepare(oldPackage=non-null, newPackage=LEGACY_VPN)
     * to prepare(oldPackage=null, newPackage=LEGACY_VPN)
     *
     * @param oldPackage The package name of the old VPN application
     * @param newPackage The package name of the new VPN application
     *
     * @return true if the operation succeeded.
     */
    public synchronized boolean prepare(String oldPackage, String newPackage) {
        Log.w(TAG, "oldPackage: " + oldPackage + ", newPackage: " + newPackage + ", CallingUid:" + Binder.getCallingUid());
        if (oldPackage != null) {
            // Stop an existing always-on VPN from being dethroned by other apps.
            if (mAlwaysOn && !isCurrentPreparedPackage(oldPackage)) {
                Log.w(TAG, "Stop prepare() by 3rd party app cause exit an always-on VPN (not oldPackage).");
                return false;
            }

            // Package is not the same or old package was reinstalled.
            if (!isCurrentPreparedPackage(oldPackage)) {
                // The package doesn't match. We return false (to obtain user consent) unless the
                // user has already consented to that VPN package.
                if (!oldPackage.equals(VpnConfig.LEGACY_VPN) && isVpnUserPreConsented(oldPackage)) {
                    Log.w(TAG, "Prepare consented 3rd party app VPN (package doesn't match). Prepare internal oldPackage");
                    prepareInternal(oldPackage);
                    return true;
                }
                return false;
            } else if (!oldPackage.equals(VpnConfig.LEGACY_VPN)
                    && !isVpnUserPreConsented(oldPackage)) {
                // Currently prepared VPN is revoked, so unprepare it and return false.
                Log.w(TAG, "Currently prepared VPN is revoked. Prepare internal LEGACY_VPN");
                prepareInternal(VpnConfig.LEGACY_VPN);
                return false;
            }
        }

        // Return true if we do not need to revoke.
        if (newPackage == null || (!newPackage.equals(VpnConfig.LEGACY_VPN) &&
                isCurrentPreparedPackage(newPackage))) {
            Log.w(TAG, "newPackage is null or current prepared package is LEGACY_VPN");
            return true;
        }

        // Check that the caller is authorized.
        // Internal caller must also be able to call this method for EDM Vpn Policy use case
        enforceControlPermissionOrInternalCaller();

        // Stop an existing always-on VPN from being dethroned by other apps.
        if (mAlwaysOn && !isCurrentPreparedPackage(newPackage)) {
			Log.w(TAG, "Stop prepare() by 3rd party app cause exit an always-on VPN (not newPackage).");
            return false;
        }
        Log.w(TAG, "Prepare internal newPackage");
        prepareInternal(newPackage);
        return true;
    }

    private boolean isCurrentPreparedPackage(String packageName) {
        // We can't just check that packageName matches mPackage, because if the app was uninstalled
        // and reinstalled it will no longer be prepared. Instead check the UID.
        return getAppUid(packageName, mUserHandle) == mOwnerUID;
    }

    /** Prepare the VPN for the given package. Does not perform permission checks. */
    private void prepareInternal(String newPackage) {
        long token = Binder.clearCallingIdentity();
        try {
            // Reset the interface.
            if (mInterface != null) {
                final Set<UidRange> existingRanges = mNetworkCapabilities.getUids();
                if (existingRanges != null) {
                    for (UidRange uidRange : existingRanges) {
                        hideNotification(uidRange.getStartUser());
                    }
                }
                mStatusIntent = null;
                agentDisconnect();
                jniReset(mInterface);
                mInterface = null;
                mNetworkCapabilities.setUids(null);
            }

            // Revoke the connection or stop LegacyVpnRunner.
            if (mConnection != null) {
                try {
                    Log.w(TAG, "Revoke the connection or stop LegacyVpnRunner.");
                    mConnection.mService.transact(IBinder.LAST_CALL_TRANSACTION,
                            Parcel.obtain(), null, IBinder.FLAG_ONEWAY);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to revoke the connection or stop LegacyVpnRunner.");
                }
                mContext.unbindService(mConnection);
                mConnection = null;
            } else if (mLegacyVpnRunner != null) {
                mLegacyVpnRunner.exit();
                mLegacyVpnRunner = null;
            }

            try {
                mNetd.denyProtect(mOwnerUID);
            } catch (Exception e) {
                Log.wtf(TAG, "Failed to disallow UID " + mOwnerUID + " to call protect() " + e);
            }

            Log.i(TAG, "Switched from " + mPackage + " to " + newPackage);
            mPackage = newPackage;
            mOwnerUID = getAppUid(newPackage, mUserHandle);
            mIsPackageTargetingAtLeastQ = doesPackageTargetAtLeastQ(newPackage);
            try {
                mNetd.allowProtect(mOwnerUID);
            } catch (Exception e) {
                Log.wtf(TAG, "Failed to allow UID " + mOwnerUID + " to call protect() " + e);
            }
            mConfig = null;

            updateState(DetailedState.IDLE, "prepare");
            setVpnForcedLocked(mLockdown);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * Set whether a package has the ability to launch VPNs without user intervention.
     */
    public boolean setPackageAuthorization(String packageName, boolean authorized) {
        // Check if the caller is authorized.
        enforceControlPermissionOrInternalCaller();

        int uid = getAppUid(packageName, mUserHandle);
        if (uid == -1 || VpnConfig.LEGACY_VPN.equals(packageName)) {
            // Authorization for nonexistent packages (or fake ones) can't be updated.
            return false;
        }

        long token = Binder.clearCallingIdentity();
        try {
            AppOpsManager appOps =
                    (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
            appOps.setMode(AppOpsManager.OP_ACTIVATE_VPN, uid, packageName,
                    authorized ? AppOpsManager.MODE_ALLOWED : AppOpsManager.MODE_IGNORED);
            return true;
        } catch (Exception e) {
            Log.wtf(TAG, "Failed to set app ops for package " + packageName + ", uid " + uid, e);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        return false;
    }

    private boolean isVpnUserPreConsented(String packageName) {
        AppOpsManager appOps =
                (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);

        // Verify that the caller matches the given package and has permission to activate VPNs.
        return appOps.noteOpNoThrow(AppOpsManager.OP_ACTIVATE_VPN, Binder.getCallingUid(),
                packageName) == AppOpsManager.MODE_ALLOWED;
    }

    private int getAppUid(String app, int userHandle) {
        if (VpnConfig.LEGACY_VPN.equals(app)) {
            return Process.myUid();
        }
        PackageManager pm = mContext.getPackageManager();
        int result;
        try {
            result = pm.getPackageUidAsUser(app, userHandle);
        } catch (NameNotFoundException e) {
            result = -1;
        } catch (SecurityException e) {
            result = -1;
        }
        return result;
    }

    private boolean doesPackageTargetAtLeastQ(String packageName) {
        if (VpnConfig.LEGACY_VPN.equals(packageName)) {
            return true;
        }
        PackageManager pm = mContext.getPackageManager();
        try {
            ApplicationInfo appInfo =
                    pm.getApplicationInfoAsUser(packageName, 0 /*flags*/, mUserHandle);
            return appInfo.targetSdkVersion >= VERSION_CODES.Q;
        } catch (NameNotFoundException unused) {
            Log.w(TAG, "Can't find \"" + packageName + "\"");
            return false;
        }
    }

    public NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }

    public int getNetId() {
        return mNetworkAgent != null ? mNetworkAgent.netId : NETID_UNSET;
    }

    private LinkProperties makeLinkProperties() {
        boolean allowIPv4 = mConfig.allowIPv4;
        boolean allowIPv6 = mConfig.allowIPv6;

        LinkProperties lp = new LinkProperties();

        lp.setInterfaceName(mInterface);

        if (mConfig.addresses != null) {
            for (LinkAddress address : mConfig.addresses) {
                lp.addLinkAddress(address);
                allowIPv4 |= address.getAddress() instanceof Inet4Address;
                allowIPv6 |= address.getAddress() instanceof Inet6Address;
            }
        }

        if (mConfig.routes != null) {
            for (RouteInfo route : mConfig.routes) {
                lp.addRoute(route);
                InetAddress address = route.getDestination().getAddress();
                allowIPv4 |= address instanceof Inet4Address;
                allowIPv6 |= address instanceof Inet6Address;
            }
        }

        if (mConfig.dnsServers != null) {
            for (String dnsServer : mConfig.dnsServers) {
                InetAddress address = InetAddress.parseNumericAddress(dnsServer);
                lp.addDnsServer(address);
                allowIPv4 |= address instanceof Inet4Address;
                allowIPv6 |= address instanceof Inet6Address;
            }
        }

        lp.setHttpProxy(mConfig.proxyInfo);

        if (!allowIPv4) {
            lp.addRoute(new RouteInfo(new IpPrefix(Inet4Address.ANY, 0), RTN_UNREACHABLE));
        }
        if (!allowIPv6) {
            lp.addRoute(new RouteInfo(new IpPrefix(Inet6Address.ANY, 0), RTN_UNREACHABLE));
        }

        // Concatenate search domains into a string.
        StringBuilder buffer = new StringBuilder();
        if (mConfig.searchDomains != null) {
            for (String domain : mConfig.searchDomains) {
                buffer.append(domain).append(' ');
            }
        }
        lp.setDomains(buffer.toString().trim());

        // TODO: Stop setting the MTU in jniCreate and set it here.

        return lp;
    }

    /**
     * Analyzes the passed LinkedProperties to figure out whether it routes to most of the IP space.
     *
     * This returns true if the passed LinkedProperties contains routes to either most of the IPv4
     * space or to most of the IPv6 address space, where "most" is defined by the value of the
     * MOST_IPV{4,6}_ADDRESSES_COUNT constants : if more than this number of addresses are matched
     * by any of the routes, then it's decided that most of the space is routed.
     * @hide
     */
    @VisibleForTesting
    static boolean providesRoutesToMostDestinations(LinkProperties lp) {
        final List<RouteInfo> routes = lp.getAllRoutes();
        if (routes.size() > MAX_ROUTES_TO_EVALUATE) return true;
        final Comparator<IpPrefix> prefixLengthComparator = IpPrefix.lengthComparator();
        TreeSet<IpPrefix> ipv4Prefixes = new TreeSet<>(prefixLengthComparator);
        TreeSet<IpPrefix> ipv6Prefixes = new TreeSet<>(prefixLengthComparator);
        for (final RouteInfo route : routes) {
            if (route.getType() == RouteInfo.RTN_UNREACHABLE) continue;
            IpPrefix destination = route.getDestination();
            if (destination.isIPv4()) {
                ipv4Prefixes.add(destination);
            } else {
                ipv6Prefixes.add(destination);
            }
        }
        if (NetworkUtils.routedIPv4AddressCount(ipv4Prefixes) > MOST_IPV4_ADDRESSES_COUNT) {
            return true;
        }
        return NetworkUtils.routedIPv6AddressCount(ipv6Prefixes)
                .compareTo(MOST_IPV6_ADDRESSES_COUNT) >= 0;
    }

    /**
     * Attempt to perform a seamless handover of VPNs by only updating LinkProperties without
     * registering a new NetworkAgent. This is not always possible if the new VPN configuration
     * has certain changes, in which case this method would just return {@code false}.
     */
    private boolean updateLinkPropertiesInPlaceIfPossible(NetworkAgent agent, VpnConfig oldConfig) {
        // NetworkMisc cannot be updated without registering a new NetworkAgent.
        if (oldConfig.allowBypass != mConfig.allowBypass) {
            Log.i(TAG, "Handover not possible due to changes to allowBypass");
            return false;
        }

        // TODO: we currently do not support seamless handover if the allowed or disallowed
        // applications have changed. Consider diffing UID ranges and only applying the delta.
        if (!Objects.equals(oldConfig.allowedApplications, mConfig.allowedApplications) ||
                !Objects.equals(oldConfig.disallowedApplications, mConfig.disallowedApplications)) {
            Log.i(TAG, "Handover not possible due to changes to whitelisted/blacklisted apps");
            return false;
        }

        // Secure Wi-Fi - START
        if (isSecureWifiPackage(mContext)) {
            int secureFolderId = SemPersonaManager.getSecureFolderId(mContext);
            if (secureFolderId > 0) {
                if(!Objects.equals(oldConfig.allowedSecureFolderApps, mConfig.allowedSecureFolderApps) ||
                    !Objects.equals(oldConfig.disallowedSecureFolderApps, mConfig.disallowedSecureFolderApps)) {
                    Log.i(TAG, "secureFolder's config is changed. return false");
                    return false;
                }
                Log.i(TAG, "secureFolder's config is not changed");
            }
        }
        // Secure Wi-Fi - END
        agent.sendLinkProperties(makeLinkProperties());
        return true;
    }

    private void agentConnect() {
        LinkProperties lp = makeLinkProperties();

        // VPN either provide a default route (IPv4 or IPv6 or both), or they are a split tunnel
        // that falls back to the default network, which by definition provides INTERNET (unless
        // there is no default network, in which case none of this matters in any sense).
        // Also, it guarantees that when a VPN applies to an app, the VPN will always be reported
        // as the network by getDefaultNetwork and registerDefaultNetworkCallback. This in turn
        // protects the invariant that apps calling CM#bindProcessToNetwork(getDefaultNetwork())
        // the same as if they use the default network.
        mNetworkCapabilities.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        mNetworkInfo.setDetailedState(DetailedState.CONNECTING, null, null);

        NetworkMisc networkMisc = new NetworkMisc();
        networkMisc.allowBypass = mConfig.allowBypass && !mLockdown;

        mNetworkCapabilities.setEstablishingVpnAppUid(Binder.getCallingUid());
        mNetworkCapabilities.setUids(createUserAndRestrictedProfilesRanges(mUserHandle,
                mConfig.allowedApplications, mConfig.disallowedApplications));
        // Secure Wi-Fi - START
        if (isSecureWifiPackage(mContext)) {
            int secureFolderId = SemPersonaManager.getSecureFolderId(mContext);
            if (secureFolderId > 0) {
                Log.i(TAG, "Apply Secure Wi-Fi to Secure Folder");
                Set<UidRange> vpnUsers = mNetworkCapabilities.getUids();
                addUserToRanges(vpnUsers, secureFolderId, mConfig.allowedSecureFolderApps, mConfig.disallowedSecureFolderApps);
                mNetworkCapabilities.setUids(vpnUsers);
            }
        }
        // Secure Wi-Fi - END
        long token = Binder.clearCallingIdentity();
        try {
            mNetworkAgent = new NetworkAgent(mLooper, mContext, NETWORKTYPE /* logtag */,
                    mNetworkInfo, mNetworkCapabilities, lp,
                    ConnectivityConstants.VPN_DEFAULT_SCORE, networkMisc,
                    NetworkFactory.SerialNumber.VPN) {
                            @Override
                            public void unwanted() {
                                // We are user controlled, not driven by NetworkRequest.
                            }
                        };
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        mNetworkInfo.setIsAvailable(true);
        updateState(DetailedState.CONNECTED, "agentConnect");
    }

    private boolean canHaveRestrictedProfile(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            return UserManager.get(mContext).canHaveRestrictedProfile(userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private void agentDisconnect(NetworkAgent networkAgent) {
        if (networkAgent != null) {
            NetworkInfo networkInfo = new NetworkInfo(mNetworkInfo);
            networkInfo.setIsAvailable(false);
            networkInfo.setDetailedState(DetailedState.DISCONNECTED, null, null);
            networkAgent.sendNetworkInfo(networkInfo);
        }
    }

    private void agentDisconnect() {
        if (mNetworkInfo.isConnected()) {
            mNetworkInfo.setIsAvailable(false);
            updateState(DetailedState.DISCONNECTED, "agentDisconnect");
            // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
            if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
                if(getKnoxVpnFeature() >= KNOXVPN_MDM_ENABLED) {
                    if(!getknoxVpnTypeForStrongswanProfile()) {
                        hideNotification(mUserHandle);
                    }
                }
            }
            // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }
            mNetworkAgent = null;
        }
    }

    /**
     * Establish a VPN network and return the file descriptor of the VPN interface. This methods
     * returns {@code null} if the application is revoked or not prepared.
     *
     * @param config The parameters to configure the network.
     * @return The file descriptor of the VPN interface.
     */
    public synchronized ParcelFileDescriptor establish(VpnConfig config) {
        // Check if the caller is already prepared.
        UserManager mgr = UserManager.get(mContext);
        Log.w(TAG, "Binder.getCallingUid(): " + Binder.getCallingUid() + ", mOwnerUID: " + mOwnerUID);
        if (Binder.getCallingUid() != mOwnerUID) {
            return null;
        }
        // Check to ensure consent hasn't been revoked since we were prepared.
        if (!isVpnUserPreConsented(mPackage)) {
			Log.e(TAG, "isVpnUserPreConsented(mPackage): false");
            return null;
        }
        // Check if the service is properly declared.
        Intent intent = new Intent(VpnConfig.SERVICE_INTERFACE);
        intent.setClassName(mPackage, config.user);
        long token = Binder.clearCallingIdentity();
        try {
            // Restricted users are not allowed to create VPNs, they are tied to Owner
            UserInfo user = mgr.getUserInfo(mUserHandle);
            if (user.isRestricted()) {
				Log.w(TAG, "throw SecurityException: Restricted users cannot establish VPNs");
                throw new SecurityException("Restricted users cannot establish VPNs");
            }

            ResolveInfo info = AppGlobals.getPackageManager().resolveService(intent,
                    null, 0, mUserHandle);
            if (info == null) {
                Log.w(TAG, "throw SecurityException: Cannot find user");
                throw new SecurityException("Cannot find " + config.user);
            }
            if (!BIND_VPN_SERVICE.equals(info.serviceInfo.permission)) {
                Log.w(TAG, "throw SecurityException: user does not require BIND_VPN_SERVICE");
                throw new SecurityException(config.user + " does not require " + BIND_VPN_SERVICE);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "RemoteException: Cannot find user");
            throw new SecurityException("Cannot find " + config.user);
        } finally {
            Binder.restoreCallingIdentity(token);
        }

        // Save the old config in case we need to go back.
        VpnConfig oldConfig = mConfig;
        String oldInterface = mInterface;
        Connection oldConnection = mConnection;
        NetworkAgent oldNetworkAgent = mNetworkAgent;
        Set<UidRange> oldUsers = mNetworkCapabilities.getUids();

        // Configure the interface. Abort if any of these steps fails.
        ParcelFileDescriptor tun;
        // Secure Wi-Fi - START
        if (isSecureWifiPackage(mContext)) {
            tun = ParcelFileDescriptor.adoptFd(jniCreateSecureWifi(config.mtu));
        }
        // Secure Wi-Fi - END
        else {
            tun = ParcelFileDescriptor.adoptFd(jniCreate(config.mtu));
        }
        try {
            String interfaze = jniGetName(tun.getFd());

            // TEMP use the old jni calls until there is support for netd address setting
            StringBuilder builder = new StringBuilder();
            for (LinkAddress address : config.addresses) {
                builder.append(" " + address);
            }
            if (jniSetAddresses(interfaze, builder.toString()) < 1) {
                throw new IllegalArgumentException("At least one address must be specified");
            }
            Connection connection = new Connection();
            if (!mContext.bindServiceAsUser(intent, connection,
                    Context.BIND_AUTO_CREATE | Context.BIND_FOREGROUND_SERVICE,
                    new UserHandle(mUserHandle))) {
                throw new IllegalStateException("Cannot bind " + config.user);
            }

            mConnection = connection;
            mInterface = interfaze;

            // Fill more values.
            config.user = mPackage;
            config.interfaze = mInterface;
            config.startTime = SystemClock.elapsedRealtime();
            mConfig = config;

            // Set up forwarding and DNS rules.
            // First attempt to do a seamless handover that only changes the interface name and
            // parameters. If that fails, disconnect.
            if (oldConfig != null
                    && updateLinkPropertiesInPlaceIfPossible(mNetworkAgent, oldConfig)) {
                // Keep mNetworkAgent unchanged
            } else {
                mNetworkAgent = null;
                updateState(DetailedState.CONNECTING, "establish");
                // Set up forwarding and DNS rules.
                agentConnect();
                // Remove the old tun's user forwarding rules
                // The new tun's user rules have already been added above so they will take over
                // as rules are deleted. This prevents data leakage as the rules are moved over.
                agentDisconnect(oldNetworkAgent);
            }

            if (oldConnection != null) {
                mContext.unbindService(oldConnection);
            }

            if (oldInterface != null && !oldInterface.equals(interfaze)) {
                jniReset(oldInterface);
            }

            try {
                IoUtils.setBlocking(tun.getFileDescriptor(), config.blocking);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "Cannot set tunnel's fd as blocking=" + config.blocking, e);
            }
        } catch (RuntimeException e) {
            IoUtils.closeQuietly(tun);
            agentDisconnect();
            // restore old state
            mConfig = oldConfig;
            mConnection = oldConnection;
            mNetworkCapabilities.setUids(oldUsers);
            mNetworkAgent = oldNetworkAgent;
            mInterface = oldInterface;
            throw e;
        }
        Log.i(TAG, "Established by " + config.user + " on " + mInterface);
        return tun;
    }

    private boolean isRunningLocked() {
        return mNetworkAgent != null && mInterface != null;
    }

    // Returns true if the VPN has been established and the calling UID is its owner. Used to check
    // that a call to mutate VPN state is admissible.
    @VisibleForTesting
    protected boolean isCallerEstablishedOwnerLocked() {
        return isRunningLocked() && Binder.getCallingUid() == mOwnerUID;
    }

    // Note: Return type guarantees results are deduped and sorted, which callers require.
    private SortedSet<Integer> getAppsUids(List<String> packageNames, int userHandle) {
        SortedSet<Integer> uids = new TreeSet<Integer>();
        for (String app : packageNames) {
            int uid = getAppUid(app, userHandle);
            if (uid != -1) uids.add(uid);
        }
        return uids;
    }

    /**
     * Creates a {@link Set} of non-intersecting {@link UidRange} objects including all UIDs
     * associated with one user, and any restricted profiles attached to that user.
     *
     * <p>If one of {@param allowedApplications} or {@param disallowedApplications} is provided,
     * the UID ranges will match the app whitelist or blacklist specified there. Otherwise, all UIDs
     * in each user and profile will be included.
     *
     * @param userHandle The userId to create UID ranges for along with any of its restricted
     *                   profiles.
     * @param allowedApplications (optional) whitelist of applications to include.
     * @param disallowedApplications (optional) blacklist of applications to exclude.
     */
    @VisibleForTesting
    Set<UidRange> createUserAndRestrictedProfilesRanges(@UserIdInt int userHandle,
            @Nullable List<String> allowedApplications,
            @Nullable List<String> disallowedApplications) {
        final Set<UidRange> ranges = new ArraySet<>();

        // Assign the top-level user to the set of ranges
        addUserToRanges(ranges, userHandle, allowedApplications, disallowedApplications);
        // SEC VPN {
        if (isDualAppEnabled() 
                && isFullTunneling(allowedApplications, disallowedApplications)) {
            Log.d(TAG, "Add uid on dualAppProfile");
            addUserToRanges(ranges, SemDualAppManager.getDualAppProfileId(), allowedApplications, disallowedApplications);
        } // } SEC VPN 
        // If the user can have restricted profiles, assign all its restricted profiles too
        if (canHaveRestrictedProfile(userHandle)) {
            final long token = Binder.clearCallingIdentity();
            List<UserInfo> users;
            try {
                users = UserManager.get(mContext).getUsers(true);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
            for (UserInfo user : users) {
                if (user.isRestricted() && (user.restrictedProfileParentId == userHandle)) {
                    addUserToRanges(ranges, user.id, allowedApplications, disallowedApplications);
                }
            }
        }
        return ranges;
    }
	
    // SEC VPN {
    private boolean isDualAppEnabled() {
        boolean result = false;
        int dualAppProfileId = SemDualAppManager.getDualAppProfileId();
        if (dualAppProfileId != UserHandle.USER_NULL) {
            result = true;
        }
        return result;
    }

    private boolean isFullTunneling(@Nullable List<String> allowedApplications,
        @Nullable List<String> disallowedApplications) {
        boolean result = false;
        if(CollectionUtils.isEmpty(allowedApplications) && CollectionUtils.isEmpty(disallowedApplications)) {
            result = true;
        }
        return result;
    }
    // } SEC VPN 
	 
    /**
     * Updates a {@link Set} of non-intersecting {@link UidRange} objects to include all UIDs
     * associated with one user.
     *
     * <p>If one of {@param allowedApplications} or {@param disallowedApplications} is provided,
     * the UID ranges will match the app whitelist or blacklist specified there. Otherwise, all UIDs
     * in the user will be included.
     *
     * @param ranges {@link Set} of {@link UidRange}s to which to add.
     * @param userHandle The userId to add to {@param ranges}.
     * @param allowedApplications (optional) whitelist of applications to include.
     * @param disallowedApplications (optional) blacklist of applications to exclude.
     */
    @VisibleForTesting
    void addUserToRanges(@NonNull Set<UidRange> ranges, @UserIdInt int userHandle,
            @Nullable List<String> allowedApplications,
            @Nullable List<String> disallowedApplications) {
        if (allowedApplications != null) {
            // Add ranges covering all UIDs for allowedApplications.
            int start = -1, stop = -1;
            for (int uid : getAppsUids(allowedApplications, userHandle)) {
                if (start == -1) {
                    start = uid;
                } else if (uid != stop + 1) {
                    ranges.add(new UidRange(start, stop));
                    start = uid;
                }
                stop = uid;
            }
            if (start != -1) ranges.add(new UidRange(start, stop));
        } else if (disallowedApplications != null) {
            // Add all ranges for user skipping UIDs for disallowedApplications.
            final UidRange userRange = UidRange.createForUser(userHandle);
            int start = userRange.start;
            for (int uid : getAppsUids(disallowedApplications, userHandle)) {
                if (uid == start) {
                    start++;
                } else {
                    ranges.add(new UidRange(start, uid - 1));
                    start = uid + 1;
                }
            }
            if (start <= userRange.stop) ranges.add(new UidRange(start, userRange.stop));
        } else {
            // Add all UIDs for the user.
            ranges.add(UidRange.createForUser(userHandle));
        }

        // SEC VPN { Only show notification for Legacy VPN
        prepareStatusIntent(userHandle);
        if (mConfig != null && !SemDualAppManager.isDualAppId(userHandle)) {
            showNotificationForLegacy(userHandle);
        }
        // } SEC VPN
    }

    // Returns the subset of the full list of active UID ranges the VPN applies to (mVpnUsers) that
    // apply to userHandle.
    static private List<UidRange> uidRangesForUser(int userHandle, Set<UidRange> existingRanges) {
        // UidRange#createForUser returns the entire range of UIDs available to a macro-user.
        // This is something like 0-99999 ; {@see UserHandle#PER_USER_RANGE}
        final UidRange userRange = UidRange.createForUser(userHandle);
        final List<UidRange> ranges = new ArrayList<UidRange>();
        for (UidRange range : existingRanges) {
            if (userRange.containsRange(range)) {
                ranges.add(range);
            }
        }
        return ranges;
    }

    /**
     * Updates UID ranges for this VPN and also updates its internal capabilities.
     *
     * <p>Should be called on primary ConnectivityService thread.
     */
    public void onUserAdded(int userHandle) {
        // If the user is restricted tie them to the parent user's VPN
        UserInfo user = UserManager.get(mContext).getUserInfo(userHandle);
        if (user != null && user.isRestricted() && user.restrictedProfileParentId == mUserHandle) {
            synchronized(Vpn.this) {
                final Set<UidRange> existingRanges = mNetworkCapabilities.getUids();
                if (existingRanges != null) {
                    try {
                        addUserToRanges(existingRanges, userHandle, mConfig.allowedApplications,
                                mConfig.disallowedApplications);
                        // ConnectivityService will call {@link #updateCapabilities} and apply
                        // those for VPN network.
                        mNetworkCapabilities.setUids(existingRanges);
                    } catch (Exception e) {
                        Log.wtf(TAG, "Failed to add restricted user to owner", e);
                    }
                }
                setVpnForcedLocked(mLockdown);
            }
        }
    }

    /**
     * Updates UID ranges for this VPN and also updates its capabilities.
     *
     * <p>Should be called on primary ConnectivityService thread.
     */
    public void onUserRemoved(int userHandle) {
        // clean up if restricted
        UserInfo user = UserManager.get(mContext).getUserInfo(userHandle);
        if (user != null && user.isRestricted() && user.restrictedProfileParentId == mUserHandle) {
            synchronized(Vpn.this) {
                final Set<UidRange> existingRanges = mNetworkCapabilities.getUids();
                if (existingRanges != null) {
                    try {
                        final List<UidRange> removedRanges =
                            uidRangesForUser(userHandle, existingRanges);
                        existingRanges.removeAll(removedRanges);
                        // ConnectivityService will call {@link #updateCapabilities} and
                        // apply those for VPN network.
                        mNetworkCapabilities.setUids(existingRanges);
                    } catch (Exception e) {
                        Log.wtf(TAG, "Failed to remove restricted user to owner", e);
                    }
                }
                setVpnForcedLocked(mLockdown);
            }
        }
    }

    /**
     * Called when the user associated with this VPN has just been stopped.
     */
    public synchronized void onUserStopped() {
        // Switch off networking lockdown (if it was enabled)
        setLockdown(false);
        mAlwaysOn = false;

        // Quit any active connections
        agentDisconnect();
    }

    /**
     * Restricts network access from all UIDs affected by this {@link Vpn}, apart from the VPN
     * service app itself and whitelisted packages, to only sockets that have had {@code protect()}
     * called on them. All non-VPN traffic is blocked via a {@code PROHIBIT} response from the
     * kernel.
     *
     * The exception for the VPN UID isn't technically necessary -- setup should use protected
     * sockets -- but in practice it saves apps that don't protect their sockets from breaking.
     *
     * Calling multiple times with {@param enforce} = {@code true} will recreate the set of UIDs to
     * block every time, and if anything has changed update using {@link #setAllowOnlyVpnForUids}.
     *
     * @param enforce {@code true} to require that all traffic under the jurisdiction of this
     *                {@link Vpn} goes through a VPN connection or is blocked until one is
     *                available, {@code false} to lift the requirement.
     *
     * @see #mBlockedUsers
     */
    @GuardedBy("this")
    private void setVpnForcedLocked(boolean enforce) {
        final List<String> exemptedPackages;
        if (isNullOrLegacyVpn(mPackage)) {
            exemptedPackages = null;
        } else {
            exemptedPackages = new ArrayList<>(mLockdownWhitelist);
            exemptedPackages.add(mPackage);
        }
        final Set<UidRange> removedRanges = new ArraySet<>(mBlockedUsers);

        Set<UidRange> addedRanges = Collections.emptySet();
        if (enforce) {
            addedRanges = createUserAndRestrictedProfilesRanges(mUserHandle,
                    /* allowedApplications */ null,
                    /* disallowedApplications */ exemptedPackages);

            // The UID range of the first user (0-99999) would block the IPSec traffic, which comes
            // directly from the kernel and is marked as uid=0. So we adjust the range to allow
            // it through (b/69873852).
            for (UidRange range : addedRanges) {
                if (range.start == 0) {
                    addedRanges.remove(range);
                    if (range.stop != 0) {
                        addedRanges.add(new UidRange(1, range.stop));
                    }
                }
            }

            removedRanges.removeAll(addedRanges);
            addedRanges.removeAll(mBlockedUsers);
        }

        setAllowOnlyVpnForUids(false, removedRanges);
        setAllowOnlyVpnForUids(true, addedRanges);
    }

    /**
     * Either add or remove a list of {@link UidRange}s to the list of UIDs that are only allowed
     * to make connections through sockets that have had {@code protect()} called on them.
     *
     * @param enforce {@code true} to add to the blacklist, {@code false} to remove.
     * @param ranges {@link Collection} of {@link UidRange}s to add (if {@param enforce} is
     *               {@code true}) or to remove.
     * @return {@code true} if all of the UIDs were added/removed. {@code false} otherwise,
     *         including added ranges that already existed or removed ones that didn't.
     */
    @GuardedBy("this")
    private boolean setAllowOnlyVpnForUids(boolean enforce, Collection<UidRange> ranges) {
        if (ranges.size() == 0) {
            return true;
        }
        final UidRange[] rangesArray = ranges.toArray(new UidRange[ranges.size()]);
        try {
            mNetd.setAllowOnlyVpnForUids(enforce, rangesArray);
        } catch (RemoteException | RuntimeException e) {
            Log.e(TAG, "Updating blocked=" + enforce
                    + " for UIDs " + Arrays.toString(ranges.toArray()) + " failed", e);
            return false;
        }
        if (enforce) {
            mBlockedUsers.addAll(ranges);
        } else {
            mBlockedUsers.removeAll(ranges);
        }
        return true;
    }

    /**
     * Return the configuration of the currently running VPN.
     */
    public VpnConfig getVpnConfig() {
        enforceControlPermission();
        return mConfig;
    }

    @Deprecated
    public synchronized void interfaceStatusChanged(String iface, boolean up) {
        try {
            mObserver.interfaceStatusChanged(iface, up);
        } catch (RemoteException e) {
            // ignored; target is local
        }
    }

    private INetworkManagementEventObserver mObserver = new BaseNetworkObserver() {
        @Override
        public void interfaceStatusChanged(String interfaze, boolean up) {
            synchronized (Vpn.this) {
                if (!up && mLegacyVpnRunner != null) {
                    mLegacyVpnRunner.check(interfaze);
                }
            }
        }

        @Override
        public void interfaceRemoved(String interfaze) {
            synchronized (Vpn.this) {
                if (interfaze.equals(mInterface) && jniCheck(interfaze) == 0) {
                    final Set<UidRange> existingRanges = mNetworkCapabilities.getUids();
                    if (existingRanges != null) {
                        for (UidRange uidRange : existingRanges) {
                            hideNotification(uidRange.getStartUser());
                        }
                    }
                    mStatusIntent = null;
                    mNetworkCapabilities.setUids(null);
                    mConfig = null;
                    mInterface = null;
                    if (mConnection != null) {
                        mContext.unbindService(mConnection);
                        mConnection = null;
                        agentDisconnect();
                    } else if (mLegacyVpnRunner != null) {
                        mLegacyVpnRunner.exit();
                        mLegacyVpnRunner = null;
                    }
                }
            }
        }
    };

    private void enforceControlPermission() {
        mContext.enforceCallingPermission(Manifest.permission.CONTROL_VPN, "Unauthorized Caller");
    }

    private void enforceControlPermissionOrInternalCaller() {
        // Require the caller to be either an application with CONTROL_VPN permission or a process
        // in the system server.
        mContext.enforceCallingOrSelfPermission(Manifest.permission.CONTROL_VPN,
                "Unauthorized Caller");
    }

    private void enforceSettingsPermission() {
        mContext.enforceCallingOrSelfPermission(Manifest.permission.NETWORK_SETTINGS,
                "Unauthorized Caller");
    }

    private class Connection implements ServiceConnection {
        private IBinder mService;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    }

    private void prepareStatusIntent(int user) {
        int callinguid = Binder.getCallingUid();
        final long token = Binder.clearCallingIdentity();
        try {
            if(callinguid == Process.SYSTEM_UID) {
                mStatusIntent = VpnConfig.getIntentForStatusPanel(mContext);
            } else {
                mStatusIntent = VpnConfig.getIntentForStatusPanelAsUser(mContext, user);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    // SEC VPN {
    public Notification createNotification(String label, Bitmap icon, PendingIntent intent) {
        String title = (label == null) ? mContext.getString(R.string.vpn_title) :
                mContext.getString(R.string.vpn_title_long, label);
        String text = (mConfig.session == null) ? mContext.getString(R.string.vpn_text) :
                mContext.getString(R.string.vpn_text_long, mConfig.session);

        Notification notification = new Notification.Builder(mContext, SystemNotificationChannels.VPN)
                    .setSmallIcon(R.drawable.vpn_connected)
                    .setLargeIcon(icon)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setContentIntent(intent)
                    .setDefaults(0)
                    .setOngoing(true)
                    .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT
                           | Notification.FLAG_FOREGROUND_SERVICE
                           | Notification.FLAG_NO_CLEAR;
        return notification;
    }

    private void hideNotification(int user) {
        mStatusIntent = null;
        NotificationManager nm = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm != null) {
            final long token = Binder.clearCallingIdentity();
            try {
                nm.cancelAsUser(null, R.drawable.vpn_connected, new UserHandle(user));
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
        unregisterLocaleChangedReceiver();
    }

    public void showNotificationForLockdown(int titleRes, int iconRes) {
        Log.v(TAG, "showNotificationForLockdown :"+mContext.getString(titleRes));

        final UserHandle user = UserHandle.of(mUserHandle);
        // open VPN settings instead of ManageDialog when click notification
        final Intent configIntent = new Intent(Settings.ACTION_VPN_SETTINGS);
        final PendingIntent configPendingIntent = PendingIntent.getActivityAsUser(
                    mContext, /* request */ 0, configIntent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT,
                    null, user);
        // reconnect LockdownVpn when click Reset button in notification
        final Intent resetIntent = new Intent(LockdownVpnTracker.ACTION_LOCKDOWN_RESET);
        resetIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        final PendingIntent resetPendingIntent = PendingIntent.getBroadcast(mContext, 0, resetIntent, 0);

        NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            final long token = Binder.clearCallingIdentity();
            try {
                Notification notification = new Notification.Builder(mContext, SystemNotificationChannels.VPN)
                        .setSmallIcon(iconRes)
                        .setContentTitle(mContext.getString(titleRes))
                        .setContentText(mContext.getString(R.string.vpn_lockdown_config))
                        .setContentIntent(configPendingIntent)
                        .setDefaults(0)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_menu_refresh, mContext.getString(R.string.reset), resetPendingIntent)
                        .build();
                notification.flags |= Notification.FLAG_ONGOING_EVENT
                                   | Notification.FLAG_FOREGROUND_SERVICE
                                   | Notification.FLAG_NO_CLEAR;
                nm.notifyAsUser(null, R.drawable.vpn_connected, notification, user);
            } finally {
                Binder.restoreCallingIdentity(token);
            }

            mLockdownNotiTitleRes = titleRes;
            mLockdownNotiIconRes = iconRes;
            registerLocaleChangedReceiver();
        }
    }

    public void hideNotificationForLockdown() {
        Log.v(TAG, "hideNotificationForLockdown");
        hideNotification(mUserHandle);
    }

    private void showNotificationForLegacy(int userId) {
        if (!mPackage.equals(VpnConfig.LEGACY_VPN)) {
            Log.v(TAG, "showNotificationForLegacy: not LEGACY_VPN");
            return;
        }
        if (KeyStore.getInstance().contains(Credentials.LOCKDOWN_VPN)) {
            Log.v(TAG, "showNotificationForLegacy: exist LOCKDOWN_VPN");
            return;
        }
        NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            final long token = Binder.clearCallingIdentity();
            try {
                Notification notification = createNotification(null, null, mStatusIntent);
                nm.notifyAsUser(null, R.drawable.vpn_connected, notification, new UserHandle(userId));
            } finally {
                Binder.restoreCallingIdentity(token);
            }
            registerLocaleChangedReceiver();
        }
    }
    // } SEC VPN

    public synchronized boolean addAddress(String address, int prefixLength) {
        if (!isCallerEstablishedOwnerLocked()) {
            return false;
        }
        boolean success = jniAddAddress(mInterface, address, prefixLength);
        mNetworkAgent.sendLinkProperties(makeLinkProperties());
        return success;
    }

    public synchronized boolean removeAddress(String address, int prefixLength) {
        if (!isCallerEstablishedOwnerLocked()) {
            return false;
        }
        boolean success = jniDelAddress(mInterface, address, prefixLength);
        mNetworkAgent.sendLinkProperties(makeLinkProperties());
        return success;
    }

    /**
     * Updates underlying network set.
     *
     * <p>Note: Does not updates capabilities. Call {@link #updateCapabilities} from
     * ConnectivityService thread to get updated capabilities.
     */
    public synchronized boolean setUnderlyingNetworks(Network[] networks) {
        if (!isCallerEstablishedOwnerLocked()) {
            return false;
        }
        if (networks == null) {
            mConfig.underlyingNetworks = null;
        } else {
            mConfig.underlyingNetworks = new Network[networks.length];
            for (int i = 0; i < networks.length; ++i) {
                if (networks[i] == null) {
                    mConfig.underlyingNetworks[i] = null;
                } else {
                    mConfig.underlyingNetworks[i] = new Network(networks[i].netId);
                }
            }
        }
        return true;
    }

    public synchronized Network[] getUnderlyingNetworks() {
        if (!isRunningLocked()) {
            return null;
        }
        return mConfig.underlyingNetworks;
    }

    /**
     * This method should only be called by ConnectivityService because it doesn't
     * have enough data to fill VpnInfo.primaryUnderlyingIface field.
     */
    public synchronized VpnInfo getVpnInfo() {
        if (!isRunningLocked()) {
            return null;
        }

        VpnInfo info = new VpnInfo();
        info.ownerUid = mOwnerUID;
        info.vpnIface = mInterface;
        return info;
    }

    public synchronized boolean appliesToUid(int uid) {
        if (!isRunningLocked()) {
            return false;
        }
        return mNetworkCapabilities.appliesToUid(uid);
    }

    /**
     * @param uid The target uid.
     *
     * @return {@code true} if {@code uid} is included in one of the mBlockedUsers ranges and the
     * VPN is not connected, or if the VPN is connected but does not apply to the {@code uid}.
     *
     * @apiNote This method don't check VPN lockdown status.
     * @see #mBlockedUsers
     */
    public synchronized boolean isBlockingUid(int uid) {
        if (mNetworkInfo.isConnected()) {
            return !appliesToUid(uid);
        } else {
            return UidRange.containsUid(mBlockedUsers, uid);
        }
    }

    private void updateAlwaysOnNotification(DetailedState networkState) {
        final boolean visible = (mAlwaysOn && networkState != DetailedState.CONNECTED);

        final UserHandle user = UserHandle.of(mUserHandle);
        final long token = Binder.clearCallingIdentity();
        try {
            final NotificationManager notificationManager = NotificationManager.from(mContext);
            if (!visible) {
                notificationManager.cancelAsUser(TAG, SystemMessage.NOTE_VPN_DISCONNECTED, user);
                // SEC VPN {
                if (!mAlwaysOn && !mEnableLockdownVpn && networkState != DetailedState.CONNECTED) {
                    // try to unregister if VPN is not Always-on and not connected yet
                    unregisterLocaleChangedReceiver();
                }
                // } SEC VPN
                return;
            }
            final Intent intent = new Intent();
            intent.setComponent(ComponentName.unflattenFromString(mContext.getString(
                    R.string.config_customVpnAlwaysOnDisconnectedDialogComponent)));
            intent.putExtra("lockdown", mLockdown);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            final PendingIntent configIntent = mSystemServices.pendingIntentGetActivityAsUser(
                    intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT, user);
            final Notification.Builder builder =
                    new Notification.Builder(mContext, SystemNotificationChannels.VPN)
                            .setSmallIcon(R.drawable.vpn_connected)
                            .setContentTitle(mContext.getString(R.string.vpn_lockdown_disconnected))
                            .setContentText(mContext.getString(R.string.vpn_lockdown_config))
                            .setContentIntent(configIntent)
                            .setCategory(Notification.CATEGORY_SYSTEM)
                            .setVisibility(Notification.VISIBILITY_PUBLIC)
                            .setOngoing(true)
                            .setColor(mContext.getColor(R.color.system_notification_accent_color));
            notificationManager.notifyAsUser(TAG, SystemMessage.NOTE_VPN_DISCONNECTED,
                    builder.build(), user);
            // SEC VPN {
            registerLocaleChangedReceiver();
            // } SEC VPN
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /**
     * Facade for system service calls that change, or depend on, state outside of
     * {@link ConnectivityService} and have hard-to-mock interfaces.
     *
     * @see com.android.server.connectivity.VpnTest
     */
    @VisibleForTesting
    public static class SystemServices {
        private final Context mContext;

        public SystemServices(@NonNull Context context) {
            mContext = context;
        }

        /**
         * @see PendingIntent#getActivityAsUser()
         */
        public PendingIntent pendingIntentGetActivityAsUser(
                Intent intent, int flags, UserHandle user) {
            return PendingIntent.getActivityAsUser(mContext, 0 /*request*/, intent, flags,
                    null /*options*/, user);
        }

        /**
         * @see Settings.Secure#putStringForUser
         */
        public void settingsSecurePutStringForUser(String key, String value, int userId) {
            Settings.Secure.putStringForUser(mContext.getContentResolver(), key, value, userId);
        }

        /**
         * @see Settings.Secure#putIntForUser
         */
        public void settingsSecurePutIntForUser(String key, int value, int userId) {
            Settings.Secure.putIntForUser(mContext.getContentResolver(), key, value, userId);
        }

        /**
         * @see Settings.Secure#getStringForUser
         */
        public String settingsSecureGetStringForUser(String key, int userId) {
            return Settings.Secure.getStringForUser(mContext.getContentResolver(), key, userId);
        }

        /**
         * @see Settings.Secure#getIntForUser
         */
        public int settingsSecureGetIntForUser(String key, int def, int userId) {
            return Settings.Secure.getIntForUser(mContext.getContentResolver(), key, def, userId);
        }
    }

    private native int jniCreate(int mtu);
    // Secure Wi-Fi - START
    private native int jniCreateSecureWifi(int mtu);
    // Secure Wi-Fi - END
    private native String jniGetName(int tun);
    private native int jniSetAddresses(String interfaze, String addresses);
    private native void jniReset(String interfaze);
    private native int jniCheck(String interfaze);
    private native boolean jniAddAddress(String interfaze, String address, int prefixLen);
    private native boolean jniDelAddress(String interfaze, String address, int prefixLen);

    private static RouteInfo findIPv4DefaultRoute(LinkProperties prop) {
        for (RouteInfo route : prop.getAllRoutes()) {
            // Currently legacy VPN only works on IPv4.
            if (route.isDefaultRoute() && route.getGateway() instanceof Inet4Address) {
                return route;
            }
        }

        throw new IllegalStateException("Unable to find IPv4 default gateway");
    }

    /**
     * Start legacy VPN, controlling native daemons as needed. Creates a
     * secondary thread to perform connection work, returning quickly.
     *
     * Should only be called to respond to Binder requests as this enforces caller permission. Use
     * {@link #startLegacyVpnPrivileged(VpnProfile, KeyStore, LinkProperties)} to skip the
     * permission check only when the caller is trusted (or the call is initiated by the system).
     */
    public void startLegacyVpn(VpnProfile profile, KeyStore keyStore, LinkProperties egress) {
        enforceControlPermission();
        long token = Binder.clearCallingIdentity();
        try {
            startLegacyVpnPrivileged(profile, keyStore, egress);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
    private boolean isVpnConnectionSecure(String[] racoon) {
        long token = Binder.clearCallingIdentity();
        mEDM = EnterpriseDeviceManager.getInstance(mContext);
        boolean result = true;
        if (mEDM != null) {
            mVpnPolicy = mEDM.getVpnPolicy();
        }
        if (mVpnPolicy != null) {
            result = mVpnPolicy.checkRacoonSecurity(racoon);
        }
        Binder.restoreCallingIdentity(token);
        return result;
    }

    private String getVpnTypeString(int type) {
        switch(type){
            case VpnProfile.TYPE_PPTP:
                return "PPTP";
            case VpnProfile.TYPE_L2TP_IPSEC_RSA:
                return "L2TP_IPSEC_RSA";
            case VpnProfile.TYPE_L2TP_IPSEC_PSK:
                return "L2TP_IPSEC_PSK";
            case VpnProfile.TYPE_IPSEC_XAUTH_RSA:
                return "IPSEC_XAUTH_RSA";
            case VpnProfile.TYPE_IPSEC_XAUTH_PSK:
                return "IPSEC_XAUTH_PSK";
            case VpnProfile.TYPE_IPSEC_IKEV2_RSA:
                return "IPSEC_IKEV2_RSA";
            case VpnProfile.TYPE_IPSEC_IKEV2_PSK:
                return "IPSEC_IKEV2_PSK";
            case VpnProfile.TYPE_IPSEC_HYBRID_RSA:
                return "IPSEC_HYBRID_RSA";
            default:
                return "";
        }
    }
    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }

    /**
     * Like {@link #startLegacyVpn(VpnProfile, KeyStore, LinkProperties)}, but does not check
     * permissions under the assumption that the caller is the system.
     *
     * Callers are responsible for checking permissions if needed.
     */
    public void startLegacyVpnPrivileged(VpnProfile profile, KeyStore keyStore,
            LinkProperties egress) {
        UserManager mgr = UserManager.get(mContext);
        UserInfo user = mgr.getUserInfo(mUserHandle);
        if (user.isRestricted() || mgr.hasUserRestriction(UserManager.DISALLOW_CONFIG_VPN,
                    new UserHandle(mUserHandle))) {
            throw new SecurityException("Restricted users cannot establish VPNs");
        }

        final RouteInfo ipv4DefaultRoute = findIPv4DefaultRoute(egress);
        final String gateway = ipv4DefaultRoute.getGateway().getHostAddress();
        final String iface = ipv4DefaultRoute.getInterface();

        // strongSwan support - START
        mEgressIface = iface;
        Log.d(TAG, "Egress Iface ("+mEgressIface+") activated");

        String hostAddress = "";
        try {
            Enumeration<InetAddress> inetAddresses = NetworkInterface.getByName(mEgressIface).getInetAddresses();
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                hostAddress = inetAddress.getHostAddress();
            }
        }
        catch (Exception e) {
            Log.d(TAG, "startLegacyVpnPrivileged Egress Iface ("+mEgressIface+") doesn't exist");
        }
        // strongSwan support - END

        // Load certificates.
        String privateKey = "";
        String userCert = "";
        String caCert = "";
        String serverCert = "";
        //KNOX CCM {
        boolean isCcmEnabled = isCcmEnabled();
        Log.d(TAG, "startLegacyVpnPrivileged: isCcmEnabled=" + isCcmEnabled);
        //KNOX CCM }
        if (!profile.ipsecUserCert.isEmpty()) {
            Log.d(TAG, "startLegacyVpnPrivileged: load userCert from AKS:" + profile.ipsecUserCert);
            privateKey = Credentials.USER_PRIVATE_KEY + profile.ipsecUserCert;
            byte[] value = keyStore.get(Credentials.USER_CERTIFICATE + profile.ipsecUserCert);
            userCert = (value == null) ? null : new String(value, StandardCharsets.UTF_8);
            //KNOX CCM {
            if (userCert == null && isCcmEnabled) {
                Log.d(TAG, "startLegacyVpnPrivileged: load userCert from CCM:" + profile.ipsecUserCert);
                privateKey = Credentials.USER_PRIVATE_KEY + getCcmContainerAlias(profile.ipsecUserCert, false);
                userCert = getCcmCertificate(Credentials.USER_CERTIFICATE, profile.ipsecUserCert);
				if (userCert == null && containsKnoxTag(profile.ipsecUserCert)) {
                    privateKey = Credentials.USER_PRIVATE_KEY + getCcmContainerAlias(profile.ipsecUserCert, true);
					String displayAlias = removeKnoxTag(profile.ipsecUserCert);
                    Log.d(TAG, "startLegacyVpnPrivileged: load userCert from CCM:" + displayAlias);
                    userCert = getCcmCertificate(Credentials.USER_CERTIFICATE, displayAlias);
				}
            }
            //KNOX CCM }
        }
        if (!profile.ipsecCaCert.isEmpty()) {
            Log.d(TAG, "startLegacyVpnPrivileged: load caCert from AKS:" + profile.ipsecCaCert);
            byte[] value = keyStore.get(Credentials.CA_CERTIFICATE + profile.ipsecCaCert);
            caCert = (value == null) ? null : new String(value, StandardCharsets.UTF_8);
            //KNOX CCM {
            if (caCert == null && isCcmEnabled) {
                Log.d(TAG, "startLegacyVpnPrivileged: load caCert from CCM:" + profile.ipsecCaCert);
                caCert = getCcmCertificate(Credentials.CA_CERTIFICATE, profile.ipsecCaCert);
				if (caCert == null && containsKnoxTag(profile.ipsecCaCert)) {
					String displayAlias = removeKnoxTag(profile.ipsecCaCert);
                    Log.d(TAG, "startLegacyVpnPrivileged: load caCert from CCM:" + displayAlias);
                    caCert = getCcmCertificate(Credentials.CA_CERTIFICATE, displayAlias);
				}
            }
            //KNOX CCM }
        }
        if (!profile.ipsecServerCert.isEmpty()) {
            Log.d(TAG, "startLegacyVpnPrivileged: load serverCert from AKS:" + profile.ipsecServerCert);
            byte[] value = keyStore.get(Credentials.USER_CERTIFICATE + profile.ipsecServerCert);
            serverCert = (value == null) ? null : new String(value, StandardCharsets.UTF_8);
            //KNOX CCM {
            if (serverCert == null && isCcmEnabled) {
                Log.d(TAG, "startLegacyVpnPrivileged: load serverCert from CCM:" + profile.ipsecServerCert);
                serverCert = getCcmCertificate(Credentials.USER_CERTIFICATE, profile.ipsecServerCert);
				if (serverCert == null && containsKnoxTag(profile.ipsecServerCert)) {
					String displayAlias = removeKnoxTag(profile.ipsecServerCert);
                    Log.d(TAG, "startLegacyVpnPrivileged: load serverCert from CCM:" + displayAlias);
                    serverCert = getCcmCertificate(Credentials.USER_CERTIFICATE, displayAlias);
				}
            }
            //KNOX CCM }
        }
        if (privateKey == null || userCert == null || caCert == null || serverCert == null) {
            Log.e(TAG, "Cannot load credentials");
            throw new IllegalStateException("Cannot load credentials");
        }

        // { @vpn.HQ : Encrypt/Decrypt VPN Profile
        VpnProfile.decrypt(profile);
        // { @vpn.HQ : Encrypt/Decrypt VPN Profile

        // Prepare arguments for racoon.
        String[] racoon = null;
        switch (profile.type) {
            case VpnProfile.TYPE_L2TP_IPSEC_PSK:
                racoon = new String[] {
                    iface, profile.server, "udppsk", profile.ipsecIdentifier,
                    profile.ipsecSecret, "1701",
                };
                break;
            case VpnProfile.TYPE_L2TP_IPSEC_RSA:
                racoon = new String[] {
                    iface, profile.server, "udprsa", privateKey, userCert,
                    caCert, serverCert, "1701",
                };
                break;
            case VpnProfile.TYPE_IPSEC_HYBRID_RSA:
                racoon = new String[] {
                    iface, profile.server, "hybridrsa",
                    caCert, serverCert, profile.username, profile.password, "", gateway,
                };
                break;
        }

        String ccModePropertyValue = SystemProperties.get(CC_PROPERTY, "None");
        if (SecProductFeature_SECURITY.SEC_PRODUCT_FEATURE_SECURITY_SUPPORT_VPN_STRONG_SWAN) {
            if (!MdfUtils.isMdfEnforced()){
                profile.ocspServerUrl = "";
            }

            // Prepare arguments for strongSwan.
            switch (profile.type) {
                case VpnProfile.TYPE_IPSEC_XAUTH_PSK:
                    racoon = new String[] {
                        profile.server, "xauthpsk", profile.ipsecIdentifier,
                        profile.ipsecSecret, ccModePropertyValue,
                        (profile.isPFS ? "+pfs" : "nonpfs"), hostAddress,
                        profile.username, profile.password
                    };
                    Log.v(TAG, "charon: type = IPSEC_XAUTH_PSK : "+ccModePropertyValue);
                    break;
                case VpnProfile.TYPE_IPSEC_XAUTH_RSA:
                    racoon = new String[] {
                        profile.server, "xauthrsa", privateKey, userCert,
                        caCert, serverCert, profile.ocspServerUrl, ccModePropertyValue,
                        (profile.isPFS ? "+pfs" : "nonpfs"), hostAddress,
                        profile.username, profile.password
                    };
                    Log.v(TAG, "charon: type = IPSEC_XAUTH_RSA : "+ccModePropertyValue);
                    break;
                case VpnProfile.TYPE_IPSEC_IKEV2_PSK:
                    racoon = new String[] {
                        profile.server, "ikev2psk", profile.ipsecIdentifier,
                        profile.ipsecSecret, ccModePropertyValue,
                        (profile.isPFS ? "+pfs" : "nonpfs"), hostAddress
                    };
                    Log.v(TAG, "charon: type = IPSEC_IKEV2_PSK : "+ccModePropertyValue);
                    break;
                case VpnProfile.TYPE_IPSEC_IKEV2_RSA:
                    racoon = new String[] {
                        profile.server, "ikev2rsa", privateKey, userCert,
                        caCert, serverCert, profile.ocspServerUrl, ccModePropertyValue,
                        (profile.isPFS ? "+pfs" : "nonpfs"), hostAddress
                    };
                    Log.v(TAG, "charon: type = IPSEC_IKEV2_RSA : "+ccModePropertyValue);
                    break;
            }
        } else {
            Log.v(TAG, "Prepare arguments for racoon, because strongSwan (charon) is not supported.");

            // Prepare arguments for racoon, because strongSwan is not supported.
            switch (profile.type) {
                case VpnProfile.TYPE_IPSEC_XAUTH_PSK:
                    racoon = new String[] {
                        iface, profile.server, "xauthpsk", profile.ipsecIdentifier,
                        profile.ipsecSecret, profile.username, profile.password, "", gateway,
                    };
                    break;
                case VpnProfile.TYPE_IPSEC_XAUTH_RSA:
                    racoon = new String[] {
                        iface, profile.server, "xauthrsa", privateKey, userCert,
                        caCert, serverCert, profile.username, profile.password, "", gateway,
                    };
                    break;
                case VpnProfile.TYPE_IPSEC_HYBRID_RSA:
                    racoon = new String[] {
                        iface, profile.server, "hybridrsa",
                        caCert, serverCert, profile.username, profile.password, "", gateway,
                    };
                    break;
            }
        }

        // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
        // Security check: If parameters are set for charon use them. If not
        // check racoon parameters. (Only either one can be set, not both.)
        String[] vpnParameters = racoon;
        if(!isVpnConnectionSecure(vpnParameters)) {
            AuditLog.logAsUser(AuditLog.NOTICE, AuditLog.AUDIT_LOG_GROUP_APPLICATION, false,
                    android.os.Process.myPid(), "Vpn",
                    AuditEvents.CONNECTING_VPN + profile.name + " to server address : "
                            + profile.server + " from type " + getVpnTypeString(profile.type)
                            + AuditEvents.FAILED_UNSECURE,
                    UserHandle.getUserId(Binder.getCallingUid()));
            return;
        }
        // } SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN
        
        // Prepare arguments for mtpd.
        String[] mtpd = null;
        switch (profile.type) {
            case VpnProfile.TYPE_PPTP:
                mtpd = new String[] {
                    iface, "pptp", profile.server, "1723",
                    "name", profile.username, "password", profile.password,
                    "linkname", "vpn", "refuse-eap", "nodefaultroute",
                    "usepeerdns", "idle", "1800", "mtu", "1350", "mru", "1350",
                    (profile.mppe ? "+mppe" : "nomppe"),
                };
                break;
            case VpnProfile.TYPE_L2TP_IPSEC_PSK:
            case VpnProfile.TYPE_L2TP_IPSEC_RSA:
                mtpd = new String[] {
                    iface, "l2tp", profile.server, "1701", profile.l2tpSecret,
                    "name", profile.username, "password", profile.password,
                    "linkname", "vpn", "refuse-eap", "nodefaultroute",
                    "usepeerdns", "idle", "1800", "mtu", "1300", "mru", "1300",
                };
                break;
        }

        VpnConfig config = new VpnConfig();
        config.legacy = true;
        config.user = profile.key;
        config.interfaze = iface;
        config.session = profile.name;
        config.isMetered = false;
        config.proxyInfo = profile.proxy;
        // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
        if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
            mProfileName = profile.name;
        }
        // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }

        config.addLegacyRoutes(profile.routes);
        if (profile.type < 4) {
            config.isIKE = false;
        } else {
            config.isIKE = true;
        }
        if (!profile.dnsServers.isEmpty()) {
            config.dnsServers = Arrays.asList(profile.dnsServers.split(" +"));
        }
        if (!profile.searchDomains.isEmpty()) {
            config.searchDomains = Arrays.asList(profile.searchDomains.split(" +"));
        }
        startLegacyVpn(config, racoon, mtpd, profile);
    }

    private synchronized void startLegacyVpn(VpnConfig config, String[] racoon, String[] mtpd,
            VpnProfile profile) {
        stopLegacyVpnPrivileged();

        // Prepare for the new request.
		Log.w(TAG, "Prepare internal LEGACY_VPN");
        prepareInternal(VpnConfig.LEGACY_VPN);
        updateState(DetailedState.CONNECTING, "startLegacyVpn");

        // Start a new LegacyVpnRunner and we are done!
        mLegacyVpnRunner = new LegacyVpnRunner(config, racoon, mtpd, profile);
        mLegacyVpnRunner.start();
    }

    /** Stop legacy VPN. Permissions must be checked by callers. */
    public synchronized void stopLegacyVpnPrivileged() {
        if (mLegacyVpnRunner != null) {
            mLegacyVpnRunner.exit();
            mLegacyVpnRunner = null;

            synchronized (LegacyVpnRunner.TAG) {
                // wait for old thread to completely finish before spinning up
                // new instance, otherwise state updates can be out of order.
            }
        }
    }

    /**
     * Return the information of the current ongoing legacy VPN.
     */
    public synchronized LegacyVpnInfo getLegacyVpnInfo() {
        // Check if the caller is authorized.
        enforceControlPermissionOrInternalCaller();
        return getLegacyVpnInfoPrivileged();
    }

    /**
     * Return the information of the current ongoing legacy VPN.
     * Callers are responsible for checking permissions if needed.
     */
    public synchronized LegacyVpnInfo getLegacyVpnInfoPrivileged() {
        if (mLegacyVpnRunner == null) return null;

        final LegacyVpnInfo info = new LegacyVpnInfo();
        info.key = mConfig.user;
        info.state = LegacyVpnInfo.stateFromNetworkInfo(mNetworkInfo);
        if (mNetworkInfo.isConnected()) {
            info.intent = mStatusIntent;
        }
        return info;
    }

    public VpnConfig getLegacyVpnConfig() {
        if (mLegacyVpnRunner != null) {
            return mConfig;
        } else {
            return null;
        }
    }

    /**
     * Bringing up a VPN connection takes time, and that is all this thread
     * does. Here we have plenty of time. The only thing we need to take
     * care of is responding to interruptions as soon as possible. Otherwise
     * requests will pile up. This could be done in a Handler as a state
     * machine, but it is much easier to read in the current form.
     */
    private class LegacyVpnRunner extends Thread {
        private static final String TAG = "LegacyVpnRunner";

        private final String[] mDaemons;
        private final String[][] mArguments;
        private final LocalSocket[] mSockets;
        private final String mOuterInterface;
        private final AtomicInteger mOuterConnection =
                new AtomicInteger(ConnectivityManager.TYPE_NONE);
        private final VpnProfile mProfile;

        private long mBringupStartTime = -1;
        private List <String> ipAddresses = null;
        private String mServerIP;

        /**
         * Watch for the outer connection (passing in the constructor) going away.
         */
        private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!mEnableTeardown) return;

                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    if (intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE,
                            ConnectivityManager.TYPE_NONE) == mOuterConnection.get()) {
                        NetworkInfo info = (NetworkInfo)intent.getExtra(
                                ConnectivityManager.EXTRA_NETWORK_INFO);
                        if (info != null && !info.isConnectedOrConnecting()) {
                            try {
                                mObserver.interfaceStatusChanged(mOuterInterface, false);
                            } catch (RemoteException e) {}
                        }
                    }
                } else if (intent.getAction().equals(SemEmergencyConstants.EMERGENCY_STATE_CHANGED)) {
                    if (SemEmergencyManager.isEmergencyMode(context)) {
                        Log.d(TAG, "mBroadcastReceiver: MPSM is enabled");
                        prepare(null, VpnConfig.LEGACY_VPN);
                    }
				}
            }
        };

        LegacyVpnRunner(VpnConfig config, String[] racoon, String[] mtpd, VpnProfile profile) {
            super(TAG);
            mConfig = config;

            if(SecProductFeature_SECURITY.SEC_PRODUCT_FEATURE_SECURITY_SUPPORT_VPN_STRONG_SWAN
                && mtpd == null && !racoon[2].equals("hybridrsa")) {
                mDaemons = new String[] {"charon", "mtpd"};
            } else {
                mDaemons = new String[] {"racoon", "mtpd"};
            }
            Log.i(TAG, "Daemon[0]: " + mDaemons[0]);

            // TODO: clear arguments from memory once launched
            mArguments = new String[][] {racoon, mtpd};
            mSockets = new LocalSocket[mDaemons.length];

            // This is the interface which VPN is running on,
            // mConfig.interfaze will change to point to OUR
            // internal interface soon. TODO - add inner/outer to mconfig
            // TODO - we have a race - if the outer iface goes away/disconnects before we hit this
            // we will leave the VPN up.  We should check that it's still there/connected after
            // registering
            mOuterInterface = mConfig.interfaze;

            mProfile = profile;

            if (!TextUtils.isEmpty(mOuterInterface)) {
                final ConnectivityManager cm = ConnectivityManager.from(mContext);
                for (Network network : cm.getAllNetworks()) {
                    final LinkProperties lp = cm.getLinkProperties(network);
                    if (lp != null && lp.getAllInterfaceNames().contains(mOuterInterface)) {
                        final NetworkInfo networkInfo = cm.getNetworkInfo(network);
                        if (networkInfo != null) mOuterConnection.set(networkInfo.getType());
                    }
                }
            }

            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            filter.addAction(SemEmergencyConstants.EMERGENCY_STATE_CHANGED);
            mContext.registerReceiver(mBroadcastReceiver, filter);
        }

        public void check(String interfaze) {
            if (interfaze.equals(mOuterInterface)) {
                Log.i(TAG, "Legacy VPN is going down with " + interfaze);
                exit();
            }
        }

        public void exit() {
            // We assume that everything is reset after stopping the daemons.
            interrupt();
            agentDisconnect();
            try {
                mContext.unregisterReceiver(mBroadcastReceiver);
            } catch (IllegalArgumentException e) {}
        }

        @Override
        public void run() {
            // Wait for the previous thread since it has been interrupted.
            Log.v(TAG, "Waiting");
            synchronized (TAG) {
                Log.v(TAG, "Executing");
                // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
                boolean isKnoxVpnProfile = false;
                // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }
                try {
                    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
                    if((SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) && getKnoxVpnFeature() >= KNOXVPN_MDM_ENABLED) {
                        if(!getknoxVpnTypeForStrongswanProfile()) {
                            isKnoxVpnProfile = true;
                            bringup(isKnoxVpnProfile);
                        } else {
                            bringup(isKnoxVpnProfile);
                        }
                    } 
                    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }
                    else {
                        bringup(isKnoxVpnProfile);
                    }

                    waitForDaemonsToStop();
                    interrupted(); // Clear interrupt flag if execute called exit.
                } catch (InterruptedException e) {
                } finally {
                    if (!mEnableLockdownVpn) {
                        setupIpRulesForCcMode(mServerIP, false);
                    }
                    // Delete VPN rules [
                    Log.d(TAG, "delete vpn rules");
                    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
                    if(!isKnoxVpnProfile) {
                        mVpnRules.deleteVpnPostroutingChain();
                        if (mDaemons[0].compareTo("racoon") == 0) {
                            mVpnRules.flushXfrmStateRule();
                        }
                    }
                    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }
                    // Delete VPN rules ]
                    stopLegacyDaemons();
                    // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
                    String typeForAudit = "";
                    String addressForAudit = "";
                    if (mArguments.length > 1 && mArguments[1] != null) {
                        String[] argumentsForAudit = mArguments[1];
                        if(argumentsForAudit.length > 2) {
                            typeForAudit = argumentsForAudit[1];
                            addressForAudit = argumentsForAudit[2];
                        }
                    }
                    AuditLog.logAsUser(AuditLog.NOTICE, AuditLog.AUDIT_LOG_GROUP_APPLICATION, true,
                            android.os.Process.myPid(), "Vpn",
                            AuditEvents.DISCONNECTING_VPN
                                    + "to server address " + addressForAudit
                                    + " from type " + typeForAudit
                                    + AuditEvents.SUCCEEDED,
                            UserHandle.getUserId(Binder.getCallingUid()));
                    // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }
                    for (LocalSocket socket : mSockets) {
                        IoUtils.closeQuietly(socket);
                    }
                    // This sleep is necessary for racoon to successfully complete sending delete
                    // message to server.
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                    for (String daemon : mDaemons) {
                        SystemService.stop(daemon);
                    }
                }
                agentDisconnect();
            }
        }

        private void checkInterruptAndDelay(boolean sleepLonger) throws InterruptedException {
            long now = SystemClock.elapsedRealtime();
            if (now - mBringupStartTime <= 60000) {
                Thread.sleep(sleepLonger ? 200 : 1);
            } else {
                updateState(DetailedState.FAILED, "checkpoint");
                throw new IllegalStateException("VPN bringup took too long");
            }
        }

        /** The following changes has been made in the below method to make it co-exist with knox vpn profile.
        *   1. The SetFirewallForVPN_CCMode  has been disabled for knox vpn.
        *   2. The route parameter for the knox vpn profile has been set to null by default.
        *   3. agentConnectForKnoxInterface is being called instead of agentConnect method.
        **/
        private void bringup(boolean isKnoxProfile) {
            // Catch all exceptions so we can clean up a few things.
            boolean initFinished = false;
            // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
            String typeForAudit = "";
            String addressForAudit = "";
            if (mArguments.length > 1 && mArguments[1] != null) {
                String[] argumentsForAudit = mArguments[1];
                if(argumentsForAudit.length > 2) {
                    typeForAudit = argumentsForAudit[1];
                    addressForAudit = argumentsForAudit[2];
                }
            }
            // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }
            try {
                // Initialize the timer.
                mBringupStartTime = SystemClock.elapsedRealtime();

                // Wait for the daemons to stop.
                for (String daemon : mDaemons) {
                    while (!SystemService.isStopped(daemon)) {
                        checkInterruptAndDelay(true);
                    }
                }

                // Clear the previous state.
                File state = new File("/data/misc/vpn/state");
                state.delete();
                if (state.exists()) {
                    throw new IllegalStateException("Cannot delete the state");
                }
                new File("/data/misc/vpn/abort").delete();
                initFinished = true;

                // Check if we need to restart any of the daemons.
                boolean restart = false;
                for (String[] arguments : mArguments) {
                    restart = restart || (arguments != null);
                }
                if (!restart) {
                    agentDisconnect();
                    // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
                    AuditLog.logAsUser(AuditLog.NOTICE, AuditLog.AUDIT_LOG_GROUP_APPLICATION, false,
                            android.os.Process.myPid(), "Vpn",
                            AuditEvents.CONNECTING_VPN
                                    + "to server address " + addressForAudit
                                    + " from type " + typeForAudit
                                    + AuditEvents.FAILED,
                            UserHandle.getUserId(Binder.getCallingUid()));
                    // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }
                    return;
                }
                updateState(DetailedState.CONNECTING, "execute");

                // Start the daemon with arguments.
                for (int i = 0; i < mDaemons.length; ++i) {
                    String[] arguments = mArguments[i];
                    if (arguments == null) {
                        continue;
                    }

                    // Start the daemon.
                    String daemon = mDaemons[i];
                    if (daemon.equals("charon") && MdfUtils.isMdfEnforced()) {
                        boolean integrityTestPassed = false;
                        int tryCount = 0;

                        synchronized (Vpn.this) {
                            Log.i(TAG, "security manager ready. Doing the integrity test");
                            integrityTestPassed = testStrongSwanIntegrity();
                        }

                        if (!integrityTestPassed) {
                            Log.i(TAG, "strongSwan failed the integrity test.");
                            throw new SecurityException("strongSwan integrity test failed.");
                        } else {
                            Log.i(TAG, "strongSwan passed the integrity test.");
                        }
                    }
                    SystemService.start(daemon);

                    // Wait for the daemon to start.
                    while (!SystemService.isRunning(daemon)) {
                        checkInterruptAndDelay(true);
                    }

                    // Create the control socket.
                    mSockets[i] = new LocalSocket();
                    LocalSocketAddress address = new LocalSocketAddress(
                            daemon, LocalSocketAddress.Namespace.RESERVED);

                    // Wait for the socket to connect.
                    while (true) {
                        try {
                            mSockets[i].connect(address);
                            break;
                        } catch (Exception e) {
                            // ignore
                        }
                        checkInterruptAndDelay(true);
                    }
                    mSockets[i].setSoTimeout(500);

                    // Send over the arguments.
                    OutputStream out = mSockets[i].getOutputStream();
                    for (String argument : arguments) {
                        byte[] bytes = argument.getBytes(StandardCharsets.UTF_8);
                        if (bytes.length >= 0xFFFF) {
                            throw new IllegalArgumentException("Argument is too large");
                        }
                        out.write(bytes.length >> 8);
                        out.write(bytes.length);
                        out.write(bytes);
                        checkInterruptAndDelay(false);
                    }
                    out.write(0xFF);
                    out.write(0xFF);
                    out.flush();

                    // Charon control socket is left open. We will use it
                    // to signal charon teardown.
                    if (daemon.compareTo("charon") != 0) {                    
                        // Wait for End-of-File.
                        InputStream in = mSockets[i].getInputStream();
                        while (true) {
                            try {
                                if (in.read() == -1) {
                                    break;
                                }
                            } catch (Exception e) {
                                // ignore
                            }
                            checkInterruptAndDelay(true);
                        }
                    }
                }

                // Wait for the daemons to create the new state.
                while (!state.exists()) {
                    // Check if a running daemon is dead.
                    for (int i = 0; i < mDaemons.length; ++i) {
                        String daemon = mDaemons[i];
                        if (mArguments[i] != null && !SystemService.isRunning(daemon)) {
                            throw new IllegalStateException(daemon + " is dead");
                        }
                    }
                    checkInterruptAndDelay(true);
                }

                // Now we are connected. Read and parse the new state.
                String[] parameters = FileUtils.readTextFile(state, 0, null).split("\n", -1);
                if (parameters.length != 7) {
                    throw new IllegalStateException("Cannot parse the state");
                }

                // Set the interface and the addresses in the config.
                mConfig.interfaze = parameters[0].trim();

                mConfig.addLegacyAddresses(parameters[1]);
                mLegacyAddress = parameters[1].split("/")[0];

                // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
                if(!isKnoxProfile) {
                    // Set the routes if they are not set in the config.
                    if (mConfig.routes == null || mConfig.routes.isEmpty()) {
                        mConfig.addLegacyRoutes(parameters[2]);
                    }
                }
                // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }

                // Set the DNS servers if they are not set in the config.
                if (mConfig.dnsServers == null || mConfig.dnsServers.size() == 0) {
                    String dnsServers = parameters[3].trim();
                    if (!dnsServers.isEmpty()) {
                        mConfig.dnsServers = Arrays.asList(dnsServers.split(" "));
                    }
                }

                // Set the search domains if they are not set in the config.
                if (mConfig.searchDomains == null || mConfig.searchDomains.size() == 0) {
                    String searchDomains = parameters[4].trim();
                    if (!searchDomains.isEmpty()) {
                        mConfig.searchDomains = Arrays.asList(searchDomains.split(" "));
                    }
                }
                // SEC VPN {
                //a throw route for the VPN server endpoint must be applied if uses new ppp driver.
                //If server addr is domain name, .IllegalArgumentException occurs.
                //So it need to parse domain name in advance.
                try {
                    InetAddress names[] = InetAddress.getAllByName(mProfile.server);
                    for(InetAddress address : names) {
                        if(address instanceof Inet4Address) {
                            mServerIP = address.getHostAddress();
                            break;
                        } else if(address instanceof Inet6Address) {
                            mServerIP = address.getHostAddress();
                        }
                    }
                    Log.i(TAG, "resolved mServerIP: " + mServerIP);
                } catch (Exception e) {
                    mServerIP = mProfile.server;
                    Log.w(TAG, "Failed to get host address:" + mProfile.server);
                }
                // } SEC VPN 

                // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
                if(!isKnoxProfile) {
                    // Add a throw route for the VPN server endpoint, if one was specified.
                    String endpoint = parameters[5].isEmpty() ? mServerIP : parameters[5];
                    if (!endpoint.isEmpty()) {
                        try {
                            InetAddress addr = InetAddress.parseNumericAddress(endpoint);
                            mServerIP = addr.getHostAddress();
                            if (addr instanceof Inet4Address) {
                                mConfig.routes.add(new RouteInfo(new IpPrefix(addr, 32), RTN_THROW));
                            } else if (addr instanceof Inet6Address) {
                                mConfig.routes.add(new RouteInfo(new IpPrefix(addr, 128), RTN_THROW));
                            } else {
                                Log.e(TAG, "Unknown IP address family for VPN endpoint: " + endpoint);
                            }
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, "Exception constructing throw route to " + endpoint + ": " + e);
                        }
                    }
                    // Set prohibit rule for CC mode
                    if (!mEnableLockdownVpn) {
                        setupIpRulesForCcMode(mServerIP, true);
                    }
                }
                // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }

                // Here is the last step and it must be done synchronously.
                synchronized (Vpn.this) {
                    // Set the start time
                    mConfig.startTime = SystemClock.elapsedRealtime();

                    // Check if the thread was interrupted while we were waiting on the lock.
                    checkInterruptAndDelay(false);

                    // Check if the interface is gone while we are waiting.
                    if (jniCheck(mConfig.interfaze) == 0) {
                        throw new IllegalStateException(mConfig.interfaze + " is gone");
                    }

                    // Now INetworkManagementEventObserver is watching our back.
                    mInterface = mConfig.interfaze;
                    int user = UserHandle.getUserId(Binder.getCallingUid());
                    prepareStatusIntent(user);

                    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
                    if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN && isKnoxProfile) {
                        agentConnectForKnoxInterface();
                    }
                    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }
                    else {
                        agentConnect();
                    }

                    Log.i(TAG, "Connected!");
                    // Add VPN rules [
                    Log.d(TAG, "add vpn rules");
                    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
                    if(!isKnoxProfile) {
                        mVpnRules.createVpnPostroutingChain();
                        mVpnRules.addTcpmssClampRule();
                    }
                    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }
                    // Add VPN rules ]
                }
                // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
                AuditLog.logAsUser(AuditLog.NOTICE, AuditLog.AUDIT_LOG_GROUP_APPLICATION, true,
                        android.os.Process.myPid(), "Vpn",
                        AuditEvents.CONNECTING_VPN
                                + " to server address " + addressForAudit
                                + " from type " + typeForAudit
                                + AuditEvents.SUCCEEDED,
                        UserHandle.getUserId(Binder.getCallingUid()));
                // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }
            } catch (Exception e) {
                // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM {
                AuditLog.logAsUser(AuditLog.NOTICE, AuditLog.AUDIT_LOG_GROUP_APPLICATION, false,
                        android.os.Process.myPid(), "Vpn",
                        AuditEvents.CONNECTING_VPN
                                + "to server address "+ addressForAudit
                                + " from type " + typeForAudit
                                + AuditEvents.FAILED,
                        UserHandle.getUserId(Binder.getCallingUid()));
                // SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_MDM }
                Log.i(TAG, "Aborting", e);
                if (!mEnableLockdownVpn) {
                    setupIpRulesForCcMode(mServerIP, false);
                }
                stopLegacyDaemons();
                updateState(DetailedState.FAILED, e.getMessage());
                exit();
                // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
                if (SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
                    if(isKnoxProfile) {
                        Log.d(TAG,"Sending message to clear connecting notification due to some connection error");
                        sendBroadcastToClearConnectingNotification(KnoxVpnConstants.TUN_INTERFACE_DOWN);
                    }
                }
                // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }
            }
        }

        /**
         * Check all daemons every two seconds. Return when one of them is stopped.
         * The caller will move to the disconnected state when this function returns,
         * which can happen if a daemon failed or if the VPN was torn down.
         */
        private void waitForDaemonsToStop() throws InterruptedException {
            if (!mNetworkInfo.isConnected()) {
                return;
            }
            while (true) {
                Thread.sleep(2000);
                if (mDaemons[0].compareTo("racoon") == 0) {
                    if(!isEgressActivated())
                        break;
                } else if (mDaemons[0].compareTo("charon") == 0) {
                     if(!isCharonActivated() || !isEgressActivated())
                    break;
                }

                for (int i = 0; i < mDaemons.length; i++) {
                    if (mArguments[i] != null && SystemService.isStopped(mDaemons[i])) {
                        return;
                    }
                }
            }
        }

        private boolean isCharonActivated() {
            try {
                Enumeration<InetAddress> inetAddresses = NetworkInterface.getByName("ipsec0").getInetAddresses();
                return true;
            } catch (Exception e) {
                // case when using pptp or l2tp
                Log.d(TAG, "charon Iface doesn't exist");
                return false;
            }
        }

        private boolean isEgressActivated() {
            try {
                boolean hasIpAddr = false;
                Enumeration<InetAddress> inetAddresses = NetworkInterface.getByName(mEgressIface).getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    hasIpAddr = true;
                }
                if(!hasIpAddr) {
                    Log.d(TAG, "Egress Iface ("+mEgressIface+") deactivated");
                    return false;
                }
                return true;
            } catch (Exception e) {
                // case when using pptp or l2tp
                Log.d(TAG, "Egress Iface ("+mEgressIface+") doesn't exist");
                return false;
            }
        }

        private void stopLegacyDaemons() {
            if (LOGD) Log.v(TAG, "stopLegacyDaemons: begin");
            for (int i = 0; i < mDaemons.length; ++i) {
                String daemon = mDaemons[i];
                if (daemon.compareTo("charon") == 0) {
                    //Charon does not like to be killed violently.
                    //Try more gentle approach first.
                    IoUtils.closeQuietly(mSockets[i]);
                    //Give charon some time (4s) to close down.
                    for (int j = 0; j < 20 && SystemService.isRunning(daemon); j++) {
                        try {
                            Thread.sleep(200);
                        } catch(Exception e) { /*ignore*/ }
                    }
                } else {
                    IoUtils.closeQuietly(mSockets[i]);
                }
                SystemService.stop(daemon);
            }
        }
    }

    private void setupIpRulesForCcMode(String serverIP, boolean block) {
        if (MdfUtils.isMdfEnforced()) {
            Log.i(TAG, "setupIpRulesForCcMode: " + serverIP + ", " + mLegacyAddress + ", " + block);
			// set firewall rules
            try {
                mVpnRules.setFirewallEnabled(block);
                mVpnRules.setFirewallEgressDestRule(serverIP, 500, block);
                mVpnRules.setFirewallEgressDestRule(serverIP, 4500, block);
                mVpnRules.setFirewallEgressDestRule(serverIP, 1701, block);
                mVpnRules.setFirewallInterfaceRule("ipsec0" , block);
                mVpnRules.setFirewallEgressSourceRule(mLegacyAddress, block);
                Log.i(TAG, "setupIpRulesForCcMode: v4=" + serverIP);
                // for IPv6 address
                String clatIpv6addr = clatIpv4to6(serverIP);
                mVpnRules.setFirewallEgressDestRule(clatIpv6addr, 500, block);
                mVpnRules.setFirewallEgressDestRule(clatIpv6addr, 4500, block);
                mVpnRules.setFirewallEgressDestRule(clatIpv6addr, 1701, block);
                mVpnRules.setFirewallEgressSourceRule("fe80::/64", block);
                mVpnRules.setFirewallEgressSourceRule("2000::/4", block);
                Log.i(TAG, "setupIpRulesForCcMode: v6=" + clatIpv6addr);
            } catch (Exception e) {
                Log.e(TAG, "Failed to set firewall rule: " + e);
            }
        }
    }

    public String clatIpv4to6(String ip) {
        if (ip == null) {
            return null;
        }

       StringBuffer clatIpv6addr = new StringBuffer("64:ff9b::");
       String[] octets = ip.split("\\.");

       for (int i = 0; i < 4; ++i) {
            clatIpv6addr.append(Integer.toHexString(Integer.parseInt(octets[i])));
            if(i == 1) clatIpv6addr.append(":");
       }
       return clatIpv6addr.toString();
    }

    // strongSwan binary integrity check
    private boolean testStrongSwanIntegrity() {
        boolean integrityTestPassed = false;
        try {
            Log.d(TAG, "Doing strongSwan integrity test");
            int verifyResult = -1;
            try{
                verifyResult = MdfUtils.verifyVPN();
            } catch (Exception e) {
                Log.d(TAG, "Failed to verifyVPN : exception");
            }
            Log.d(TAG, "verifyVpn returned: " + verifyResult);

            integrityTestPassed = (verifyResult == 0);
        } catch (Exception e) {
            Log.d(TAG, "Failed to do strongSwan integrity test: " + e);
        }
        return integrityTestPassed;
    }

    //KNOX CCM {
    private boolean isCcmEnabled() {
        return SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CCM_REDIRECTION 
                && TimaKeychain.isTimaKeystoreAndCCMEnabledForPackage(SETTINGS_PACKAGE_NAME);
    }

    private boolean containsKnoxTag(String aliasWithKnoxTag) {
        if (aliasWithKnoxTag == null || !aliasWithKnoxTag.endsWith(KNOX_TAG)) {
            return false;
        }
		return true;
    }

    private String removeKnoxTag(String aliasWithKnoxTag) {
		String ret = aliasWithKnoxTag;
		if (containsKnoxTag(aliasWithKnoxTag)) {
			ret = aliasWithKnoxTag.substring(0, aliasWithKnoxTag.length() - KNOX_TAG.length());
		}
		Log.i(TAG, "removeKnoxTag:" + aliasWithKnoxTag + "," + ret);
        return ret;
    }

    private String getCcmContainerAlias(String aliasWithKnoxTag, boolean checkKnoxTag) {
		String ret = aliasWithKnoxTag;
		if (checkKnoxTag && containsKnoxTag(aliasWithKnoxTag)) {
		    ret = TimaKeychain.getCcmInstallerPrefix() + removeKnoxTag(aliasWithKnoxTag);
		} else {
			ret = TimaKeychain.getCcmInstallerPrefix() + aliasWithKnoxTag;
		}
		Log.i(TAG, "getCcmContainerAlias:" + aliasWithKnoxTag + "," + ret);
        return ret;
    }

    private String formatPemCert(String noWrapBase64) {
        String wrapBase64 = "";
        int totalLen = noWrapBase64.length();
        int count = 0;
        int nextLineLen;
        for (int i = 0; i < totalLen; i = i + PEM_CERT_LINE_CHARS) {
            nextLineLen = (totalLen - count < PEM_CERT_LINE_CHARS ? totalLen - count : PEM_CERT_LINE_CHARS);
            count += nextLineLen;
            wrapBase64 = wrapBase64 + noWrapBase64.substring(i, i + nextLineLen) + "\n";
        }
        return PEM_CERT_HEADER + wrapBase64 + PEM_CERT_FOOTER;
    }

    private String convertToPemCert(X509Certificate x509Cert) {
        try {
            byte[] encoded = x509Cert.getEncoded();
            String pemStr = Base64.encodeToString(encoded, Base64.NO_WRAP);
            pemStr = formatPemCert(pemStr);
            return pemStr;
        } catch (Exception e) {
            Log.e(TAG, "Failed to convertToPemCert: " + e);
        }
        return null;
    }

    private String getCcmCertificate(String type, String displayAlias) {
        String ccmAlias = TimaKeychain.display2KeyChainAlias(displayAlias, false);
        Log.i(TAG, "getCcmCertificate:" + type + "," + displayAlias + "," + ccmAlias + ".");
        X509Certificate[] ccmCertList = TimaKeychain.getCertificateChainFromTimaKeystore(ccmAlias);
        if (ccmCertList != null) {
            for (X509Certificate x509Cert : ccmCertList) {
                if (x509Cert != null) {
                    try {
                        if (x509Cert.getBasicConstraints() > 0) {
                            if (type.equals(Credentials.CA_CERTIFICATE)) {
                                return convertToPemCert(x509Cert);
                            }
                        } else {
                            if (type.equals(Credentials.USER_CERTIFICATE)) {
                                return convertToPemCert(x509Cert);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to convertToPemCert, " + e);
                    }
                }
            }
        }
        Log.e(TAG, "Failed to getCcmCertificate");
        return null;
    }
    //KNOX CCM }

    // Secure Wi-Fi - START
    private boolean isSecureWifiPackage(Context context) {
        if(FAST_PACKAGE_NAME.equals(mPackage)) {
            PackageManager packageManager = context.getPackageManager();
            if(packageManager.checkSignatures("android", FAST_PACKAGE_NAME) == PackageManager.SIGNATURE_MATCH) {
                return true;
            }
            Log.e(TAG,"Secure Wi-Fi signature mismatched");
        }
        return false;
    }
    // Secure Wi-Fi - END

    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN {
    private boolean getknoxVpnTypeForStrongswanProfile() {
        if (!SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
            return SYSTEM_VPN;
        }
        boolean profileType = SYSTEM_VPN;
        if(getKnoxVpnFeature() >= KNOXVPN_MDM_ENABLED){
            PackageManager pm = mContext.getPackageManager();
            try {
                if((getKnoxVpnService() != null) && (mProfileName != null))  {
                    if(DBG) Log.d(TAG,"getknoxVpnTypeForStrongswanProfile: profile name is " + mProfileName);
                    String vendorOwningProfile = mKnoxVpnService.getVendorNameForProfile(mProfileName);
                    if(DBG) Log.d(TAG,"getknoxVpnTypeForStrongswanProfile vendorOwningProfile value is " + vendorOwningProfile);
                    if(vendorOwningProfile != null) {
                        int vpnDialogUid = pm.getPackageUidAsUser(VpnConfig.DIALOGS_PACKAGE,UserHandle.USER_SYSTEM);
                        if(vendorOwningProfile.equalsIgnoreCase(KnoxVpnConstants.STRONSWAN_PROXY_PGK) || vpnDialogUid == UserHandle.getAppId(Binder.getCallingUid())) {
                            if(DBG) Log.d(TAG,"Caller is either the strongswan proxy or the vpn dialog app");
                            int type = mKnoxVpnService.getKnoxVpnProfileType(mProfileName);
                            if(type == KnoxVpnConstants.SYSTEM_VPN) {
                                profileType = SYSTEM_VPN;
                            } else if(type == KnoxVpnConstants.PER_APP_VPN) {
                                profileType = PER_APP_VPN;
                            } else {
                                profileType = SYSTEM_VPN;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if(DBG) Log.e(TAG,"Exception at checkIfStrongswanProfileIsKnoxBased " + Log.getStackTraceString(e));
            }
        }
        if(DBG) Log.d(TAG,"getknoxVpnTypeForStrongswanProfile profileType value is " + profileType);
        return profileType;
    }

    private int getKnoxVpnFeature() {
        if (!SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
            return -1;
        }
        KNOXVPN_FEATURE = SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_CONTAINER == false ? KNOXVPN_MDM_ENABLED : KNOXVPN_CONTAINER_ENABLED;
        return KNOXVPN_FEATURE;
    }

    private IKnoxVpnPolicy getKnoxVpnService() {
        if (!SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
            return null;
        }
        if (getKnoxVpnFeature() >= KNOXVPN_MDM_ENABLED) {
            if(mKnoxVpnService == null) {
                mKnoxVpnService = IKnoxVpnPolicy.Stub.asInterface(ServiceManager
                    .getService(KnoxVpnPolicyConstants.KNOX_VPN_POLICY_SERVICE));
            }
        } else {
            mKnoxVpnService = null;
        }
        return mKnoxVpnService;
    }

    private void agentConnectForKnoxInterface() {
        if (!SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
            return;
        }
        LinkProperties lp = new LinkProperties();
        lp.setInterfaceName(mInterface);
        // Need to understand how the the below call works for knos
        mNetworkCapabilities.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        mNetworkCapabilities.setEstablishingVpnAppUid(Binder.getCallingUid());

        mNetworkInfo.setDetailedState(DetailedState.CONNECTING, null, null);

        if (mConfig.dnsServers != null) {
            for (String dnsServer : mConfig.dnsServers) {
                InetAddress address = InetAddress.parseNumericAddress(dnsServer);
                lp.addDnsServer(address);
            }
        }

        if (mConfig.addresses != null) {
            for (LinkAddress address : mConfig.addresses) {
                if (address.getAddress() instanceof Inet4Address) {
                    mAddress = address.getAddress().getHostAddress();
                    Log.d(TAG,"The ipv4 address added to the knox vpn interface is " + mAddress);
                }
                if (address.getAddress() instanceof Inet6Address) {
                    mV6Address = address.getAddress().getHostAddress();
                    Log.d(TAG,"The ipv6 address added to the knox vpn interface is " + mV6Address);
                }
                lp.addLinkAddress(address);
            }
        }

        if((mAddress != null) && (mV6Address != null)) {
            mTun_type = KnoxVpnConstants.KNOX_VPN_INTERFACE_V46;
        } else if (mAddress != null) {
            mTun_type = KnoxVpnConstants.KNOX_VPN_INTERFACE_V4;
        } else if(mV6Address != null) {
            mTun_type = KnoxVpnConstants.KNOX_VPN_INTERFACE_V6;
        }

        // Concatenate search domains into a string.
        StringBuilder buffer = new StringBuilder();
        if (mConfig.searchDomains != null) {
            for (String domain : mConfig.searchDomains) {
                buffer.append(domain).append(' ');
            }
        }
        lp.setDomains(buffer.toString().trim());

        NetworkMisc networkMisc = new NetworkMisc();
        networkMisc.allowBypass = false; // Not allowing bypass vpn to take place for knox vpn
        networkMisc.knoxProfile = true;

        mNetworkInfo.setKnoxProfile(true);
        mNetworkInfo.setInterfaceType(mTun_type);
        mNetworkInfo.setprofileName(mProfileName);
        mNetworkInfo.setV4Address(mAddress);
        mNetworkInfo.setV6Address(mV6Address);
        mNetworkInfo.setKnoxInterfaceName(mInterface);
        mNetworkInfo.setKnoxVpnClientType(KnoxVpnConstants.NATIVE_VPN_CLIENT);
        mNetworkInfo.setKnoxVpnCallerUid(UserHandle.getUid(mUserHandle,Process.SYSTEM_UID));
        mNetworkInfo.setKnoxNetworkState(true);

        long token = Binder.clearCallingIdentity();

        showConnectingNotification("Connecting",mProfileName);
        try {
            mNetworkAgent = new NetworkAgent(mLooper, mContext, NETWORKTYPE /* logtag */,
                mNetworkInfo, mNetworkCapabilities, lp,
                ConnectivityConstants.VPN_DEFAULT_SCORE, networkMisc,
                NetworkFactory.SerialNumber.VPN) {
                    @Override
                    public void unwanted() {
                        // We are user controlled, not driven by NetworkRequest.
                    }
                };
        } finally {
            Binder.restoreCallingIdentity(token);
        }
        mNetworkInfo.setIsAvailable(true);
        mNetworkInfo.setDetailedState(DetailedState.CONNECTED, "agentConnect", null);
        sendStrongSwanInterfaceToKnoxVpn(KnoxVpnConstants.TUN_INTERFACE_UP);
        mNetworkAgent.sendNetworkInfo(mNetworkInfo);
    }

    private void sendStrongSwanInterfaceToKnoxVpn(int state) {
        if (!SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
            return;
        }
        if(getKnoxVpnFeature() >= KNOXVPN_MDM_ENABLED){
            final long token = Binder.clearCallingIdentity();
            try {
                // Broadcast intent here with Tun ID. Should remove the fixed strings later
                Intent intent = new Intent();
                intent.setAction(KnoxVpnConstants.ACTION_VPN_INTERFACE_STATUS_INTERNAL);
                intent.setPackage(KnoxVpnConstants.STRONSWAN_PROXY_PGK);
                intent.putExtra(KnoxVpnConstants.EXTRA_ACTION_INTERNAL, "tun_info");
                if(mProfileName != null) {
                    intent.putExtra(KnoxVpnConstants.EXTRA_PROFILE_NAME_INTERNAL, mProfileName);
                }
                if(mInterface != null) {
                    intent.putExtra(KnoxVpnConstants.EXTRA_TUN_ID_INTERNAL, mInterface);
                }
                intent.putExtra(KnoxVpnConstants.EXTRA_STATE_INTERNAL, state);
                intent.putExtra(KnoxVpnConstants.EXTRA_TUN_ADDRESS_INTERNAL, mAddress);
                intent.putExtra(KnoxVpnConstants.EXTRA_TUN_V6_ADDRESS_INTERNAL, mV6Address);
                intent.putExtra(KnoxVpnConstants.EXTRA_VPN_CLIENT_TYPE_INTERNAL,1);
                intent.putExtra(KnoxVpnConstants.KNOX_VPN_INTERFACE_TYPE_INTERNAL, mTun_type);
                mContext.sendBroadcastAsUser(intent,new UserHandle(mUserHandle),android.Manifest.permission.KNOX_VPN_SOLUTION);

                // KNOX_VPN_OLD_START
                //TODO: KNOX_SECURE_NETWORK_Q_PORTING_START
                // ONS Code Should be removed
                intent = new Intent();
                intent.setAction(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.ACTION_MDM_VPN_INTERFACE_STATUS);
                intent.setPackage(KnoxVpnConstants.STRONSWAN_PROXY_PGK);
                intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_ACTION, "tun_info");
                if(mProfileName != null) {
                    intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_PROFILE_NAME, mProfileName);
                }
                if(mInterface != null) {
                    intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_TUN_ID, mInterface);
                }
                intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_STATE, state);
                intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_TUN_ADDRESS, mAddress);
                intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_TUN_V6_ADDRESS, mV6Address);
                intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.VPN_CLIENT_TYPE,1);
                if(DBG) Log.d(TAG, "sendStrongSwanInterfaceToKnoxVpn : Sends again for old proxy apk");
                mContext.sendBroadcastAsUser(intent,new UserHandle(mUserHandle),android.Manifest.permission.MDM_ENTERPRISE_VPN_SOLUTION);
                //TODO: KNOX_SECURE_NETWORK_Q_PORTING_START
                // KNOX_VPN_OLD_END
                
            } catch(Exception e) {
                Log.e(TAG,"unable to send interface details");
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }   
    }

     private void sendBroadcastToClearConnectingNotification(int state) {
        if (!SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
            return;
        }
        if(getKnoxVpnFeature() >= KNOXVPN_MDM_ENABLED){
            final long token = Binder.clearCallingIdentity();
            try {
                // Broadcast intent here with Tun ID. Should remove the fixed strings later
                if(mProfileName != null) {
                    Intent intent = new Intent();
                    intent.setAction(KnoxVpnConstants.ACTION_VPN_INTERFACE_STATUS_INTERNAL);
                    intent.setPackage(KnoxVpnConstants.STRONSWAN_PROXY_PGK);
                    intent.putExtra(KnoxVpnConstants.EXTRA_PROFILE_NAME_INTERNAL, mProfileName);
                    intent.putExtra(KnoxVpnConstants.EXTRA_TUN_ID_INTERNAL, "ipsec0");
                    intent.putExtra(KnoxVpnConstants.EXTRA_STATE_INTERNAL, state);
                    Log.d(TAG,"send message to clear connecting notificiation due to some error while connecting to server");
                    mContext.sendBroadcastAsUser(intent,new UserHandle(mUserHandle),android.Manifest.permission.KNOX_VPN_SOLUTION);
                    // KNOX_VPN_OLD_START
                    /* TODO: KNOX_SECURE_NETWORK_Q_PORTING_START
                    // ONS Code Should be removed
                    intent = new Intent();
                    intent.setAction(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.ACTION_MDM_VPN_INTERFACE_STATUS);
                    intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_PROFILE_NAME, mProfileName);
                    intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_TUN_ID, "ipsec0");
                    intent.putExtra(com.sec.enterprise.knox.vpn.KnoxVpnPolicyConstants.EXTRA_STATE, state);
                    Log.d(TAG,"send message to clear connecting notificiation due to some error while connecting to server");
                    mContext.sendBroadcast(intent, android.Manifest.permission.MDM_ENTERPRISE_VPN_SOLUTION);
                    TODO: KNOX_SECURE_NETWORK_Q_PORTING_START */
                    // KNOX_VPN_OLD_END
                }                
            } catch(Exception e) {
                Log.e(TAG,"unable to send message to clear connecting notification");
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }  
    }

    public void showNotificationForKnoxStrongSwan(String label, Bitmap icon, int user) {
        if (!SecProductFeature_KNOX.SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN) {
            return;
        }
        dismissConnectingNotification();
        if(getKnoxVpnFeature() >= KNOXVPN_MDM_ENABLED){
            mStatusIntent = VpnConfig.getIntentForStatusPanelAsUser(mContext,mUserHandle);
            NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                Notification notification = createNotification(label, icon, mStatusIntent);
                nm.notifyAsUser(null, R.drawable.vpn_connected, notification, new UserHandle(user));
            }
        }
    }

    private NotificationManager mNotificationManager = null;
    private int netId = KnoxVpnConstants.KNOX_STRONGSWAN_NET_ID;

    private void showConnectingNotification(String status, String pname) {
        /*
        knox_vpn_TO_DO uncomment it if Notification can be enabled in home screen or else it is not useful
        try {
            Log.d(TAG,"showConnectingNotification "+ netId + " " + pname);
            Intent intent = new Intent();
            PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            Notification notification = new Notification.Builder(mContext,SystemNotificationChannels.VPN).setContentTitle(status)
                    .setContentText("Profile " + status + " " + pname).setContentIntent(pIntent)
                    .setSmallIcon(R.drawable.connecting_vpn_icon).setOnlyAlertOnce(true).setOngoing(true)
                    .setAutoCancel(false).setTicker(status).build();
            getNotificationManager().notifyAsUser("knox_vpn_connecting_"+pname, netId, notification, new UserHandle(UserHandle.USER_SYSTEM));
        } catch(Exception e) {
            Log.e(TAG,"Error occured while trying to show the connecting notification for profile " + mProfileName);
        }
        */
    }

    private void dismissConnectingNotification() {
        /*
        knox_vpn_TO_DO uncomment it if Notification can be enabled in home screen or else it is not useful
        try {
            Log.d(TAG,"dismissConnectingNotification "+ netId + " " + mProfileName);
            getNotificationManager().cancelAsUser("knox_vpn_connecting_"+mProfileName, netId,new UserHandle(UserHandle.USER_SYSTEM));
        } catch(Exception e) {
           Log.e(TAG,"Error occured while trying to dismiss the connecting notification for profile " + mProfileName);
        }
        */
    }

    private NotificationManager getNotificationManager() {
        if(mNotificationManager == null) {
            mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }
    // SEC_PRODUCT_FEATURE_KNOX_SUPPORT_VPN }
}