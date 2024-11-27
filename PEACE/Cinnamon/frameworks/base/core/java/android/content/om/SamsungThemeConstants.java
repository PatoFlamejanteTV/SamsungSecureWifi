/* { SAMSUNGTHEME */
/**
 * Copyright (C) 2011 Samsung Electronics Co., Ltd. All rights reserved.
 * <p>
 * Mobile Communication Division,
 * Digital Media & Communications Business, Samsung Electronics Co., Ltd.
 * <p>
 * This software and its documentation are confidential and proprietary
 * information of Samsung Electronics Co., Ltd.  No part of the software and
 * documents may be copied, reproduced, transmitted, translated, or reduced to
 * any electronic medium or machine-readable form without the prior written
 * consent of Samsung Electronics.
 * <p>
 * Samsung Electronics makes no representations with respect to the contents,
 * and assumes no responsibility for any errors that might appear in the
 * software and documents. This publication and the contents hereof are subject
 * to change without notice.
 */

package android.content.om;

import com.sec.android.app.SecProductFeature_COMMON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/* { SAMSUNGTHEME_COMMON_OVERLAY_FOR_MULTITARGET */
import java.util.List;
/* SAMSUNGTHEME_COMMON_OVERLAY_FOR_MULTITARGET } */

/**
 * Constants related to SAMSUNGTHEME Framework
 * @hide
 */
public final class SamsungThemeConstants {

    /** @hide */
    public static final boolean IS_SAMSUNG_THEMES_ENABLED = SecProductFeature_COMMON.SEC_PRODUCT_FEATURE_COMMON_SUPPORT_ELASTIC_PLUGIN;
    /** @hide */
    public static final boolean DEBUG_THEMES = IS_SAMSUNG_THEMES_ENABLED;

    /** @hide */
    public static final String PERMISSION_SAMSUNG_OVERLAY = "com.samsung.android.permission.SAMSUNG_OVERLAY_";
    /** @hide */
    public static final String PERMISSION_OVERLAY_LANGUAGE = "com.samsung.android.permission.SAMSUNG_OVERLAY_LANGUAGE";
    /** @hide */
    public static final String PERMISSION_OVERLAY_COMPONENT = "com.samsung.android.permission.SAMSUNG_OVERLAY_COMPONENT";
    /** @hide */
    public static final String PERMISSION_OVERLAY_THEME = "com.samsung.android.permission.SAMSUNG_OVERLAY_THEME";

    /** @hide */
    public static final String PATH_LOCAL_TEMP = "/data/local/tmp";
    /** @hide */
    public static final String PATH_OVERLAY = "/data/overlays";

    /** @hide */
    public static final String DATA_OVERLAY_DIR = "/data/overlays";
    /** @hide */
    public static final String THEME_OVERLAY_DIR = DATA_OVERLAY_DIR + "/style";
    /** @hide */
    public static final String PATH_OVERLAY_CURRENT_STYLE = DATA_OVERLAY_DIR + "/currentstyle";

    /** @hide */
    public static final String DATA_APP_DIR = "/data/app";

    /** @hide */
    public static final HashSet<String> changeableApps = new HashSet<String>(
            Arrays.asList(
                    /* { Samsung DeX */
                    com.samsung.android.desktopmode.SemDesktopModeManager.LAUNCHER_PACKAGE,
                    com.samsung.android.desktopmode.SemDesktopModeManager.UI_SERVICE_PACKAGE,
                    /* Samsung DeX } */
                    "com.sec.android.app.eventnotification",
                    "com.samsung.tmowfc.wfcpref",
                    "com.sec.android.app.launcher",
                    "com.sec.android.app.FlashBarService",
                    "com.android.nfc",
                    "com.samsung.felicalock",
                    "com.android.apps.tag",
                    "com.samsung.app.newtrim",
                    "com.adnroid.dreams.phototable",
                    "com.policydm",
                    "com.samsung.android.securitylogagent",
                    "com.sec.android.app.SecSetupWizard",
                    "com.samsung.safetyinformation",
                    "com.sec.app.samsungprinterservice",
                    "com.samsung.spg",
                    "com.sec.android.app.capabilitymanager",
                    "com.sec.android.app.wallpaperchooser",
                    "com.bst.airmessage",
                    "com.sec.android.app.simsettingmgr",
                    "com.sec.android.app.simcardmanagement",
                    "com.sec.android.widgetapp.dualsimwidget",
                    "com.sec.android.app.irsettings",
                    "com.samsung.android.app.shareaccessibilitysettings",
                    "com.google.android.marvin.talkback",
                    "com.samsung.android.SettingsReceiver",
                    "com.sec.android.app.popupuireceiver",
                    "com.sec.android.wallpapercropper2",
                    "com.samsung.android.MtpApplication",
                    /*From Hero*/
                    "com.sec.android.app.soundalive",
                    "com.samsung.android.app.galaxylabs",
                    /*For PhotoEditor */
                    "com.sec.android.mimage.photoretouching",
                    /*For Photo360Editor */
                    "com.sec.android.mimage.gear360editor",
                    "com.android.providers.media",
                    "com.samsung.android.slinkcloud",
                    "com.sec.android.emergencylauncher",
                    /*For CHN new feature*/
                    "com.samsung.hongbaoassistant",
                    /*For CHN smartManager 2nd page support Theme */
                    "com.sec.android.app.firewall",
                    "com.bst.spamcall",
                    "com.sec.app.screenrecorder",
                    "com.samsung.android.bixbytouch",
                    "com.samsung.android.mateagent",
                    "com.android.bluetooth",
                    /*For Mobile Newtorks from Peace*/
                    "com.samsung.android.app.telephonyui.netsettings",
                    /*For Theme Test app*/
                    "com.samsung.theme",
                    "com.sec.winset",
                    "com.sec.sesl.tester",
                    /*For Advanced Calling from Android O OS*/
                    "com.samsung.advancedcalling",
                    /*For Android Security Keystore */
                    "com.android.certinstaller",
                    "com.android.keychain",
                    "com.sec.android.app.quicktool",
                    /*For UnifiedWFC from Android P OS*/
                    "com.sec.unifiedwfc",
                    "com.samsung.advancedcalling",
                    "com.samsung.knox.securefolder",
                    "com.samsung.android.knox.containeragent",
                    /*For Tencent Secure Wi-Fi */
                    "com.samsung.android.tencentwifisecurity",
                    /*Screen Capture */
                    "com.samsung.android.app.smartcapture",
                    /*For Clipboard Edge */
                    "com.samsung.android.app.clipboardedge",
                    /*For Android Security Keystore */
                    "com.android.certinstaller",
                    "com.android.keychain",
                    /*For fmm */
                    "com.samsung.android.fmm"
            ));

    /** @hide */
    public static HashMap<String, String> overlayTargetMap = new HashMap<String, String>() {{
        /* key : overlay target */                       /* value : real overlay target */
        put("fwk", "android");
        put("com.sec.android.app.voicenote", null);
        put("com.sec.android.app.music", null);
        put("com.samsung.android.video", null);
        put("com.sec.android.app.vepreload", null);
        put("com.samsung.app.highlightplayer", null);
        put("com.sec.android.app.clipvideo", null);
        put("com.samsung.android.scloud.backup", "com.samsung.android.scloud");
        put("com.sec.android.widgetapp.ap.hero.accuweather", "com.sec.android.daemonapp");
        put("com.samsung.android.qconnect", null);
        put("com.samsung.android.app.omcagent", null);
        put("com.samsung.android.app.dtv.dmb", null);
        put("com.sec.android.app.dmb", null);
        put("com.samsung.android.smartmirroring", null);
        put("com.samsung.android.fast", null);
    }};

    /** @hide */
    public static ArrayList<String> immortalApps = new ArrayList<String>(
            Arrays.asList(
                    "com.android.systemui",
                    "com.samsung.android.themecenter",
                    "com.android.nfc",
                    "com.samsung.android.universalswitch",
                    "com.android.providers.media",
                    "com.android.incallui",
                    "com.android.phone",
                    "com.samsung.android.app.aodservice",
                    "com.sec.android.app.launcher",
                    "com.sec.android.app.safetyassurance",
                    "com.samsung.android.incallui",
                    "com.samsung.android.scloud",
                    "com.android.bluetooth",
                    "com.samsung.android.mateagent",
                    "com.samsung.android.messaging",
                    "com.android.frameworks.gofservicetests"
            ));

    /** @hide */
    public static ArrayList<String> protectedApps = new ArrayList<String>(
            Arrays.asList(
                    "com.samsung.android.themecenter"
            ));

    /** @hide */
    public static ArrayList<String> ignoreThemeIconApps = new ArrayList<String>(
            Arrays.asList(
                    "com.nttdocomo.android.dhome"
            ));

    /** @hide */
    public static final String OLYMPIC_THEME_PACKAGE_NAME = "SamsungElectronics.Tokyo2020.appicon";
    /** @hide */
    public static final String STARWARS_THEME_PACKAGE_NAME = "com.liquidanimation.DarkSideRC.appicon";
    /** @hide */
    public static final String THEMECENTER_PACKAGE_NAME = "com.samsung.android.themecenter";
    /** @hide */
    public static final String PATH_THEME_PREFERENCES = "/data/overlays/preferences/samsung.andorid.themes.component_preference.xml";
    /** @hide */
    public static final String ACTION_THEME_APPLY = "com.samsung.android.theme.themecenter.THEME_APPLY";

    /** @hide */
    public static final String LEGACY_CONTACT_PACKAGE_NAME = "com.android.contacts";
    /** @hide */
    public static final String LEGACY_MESSAGE_PACKAGE_NAME = "com.android.mms";
    /** @hide */
    public static final String LEGACY_CALENDAR_PACKAGE_NAME = "com.android.calendar";
    /** @hide */
    public static final String LEGACY_INCALLUI_PACKAGE_NAME = "com.android.incallui";

    /* { SAMSUNGTHEME_COMMON_OVERLAY_FOR_MULTITARGET */
    /** @hide */
    public static HashMap<String, List<String>> siblingTargetMap = new HashMap<String, List<String>>();
    /** @hide */
    public static final String SAMSUNGTHEME_SIBLING = "samsungtheme-sibling";
    /* SAMSUNGTHEME_COMMON_OVERLAY_FOR_MULTITARGET } */
}
