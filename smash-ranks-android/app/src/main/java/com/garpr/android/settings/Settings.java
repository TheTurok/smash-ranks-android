package com.garpr.android.settings;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.garpr.android.App;


public final class Settings {


    private static final String CNAME = "com.garpr.android.settings.Settings";

    public static final BooleanSetting OnboardingComplete;
    public static final BooleanSetting SyncChargingIsNecessary;
    public static final BooleanSetting SyncIsEnabled;
    public static final BooleanSetting SyncIsPending;
    public static final BooleanSetting SyncWifiIsNecessary;
    public static final IntegerSetting LastVersion;
    public static final LongSetting RankingsDate;
    public static final RegionSetting Region;




    static {
        OnboardingComplete = new BooleanSetting(CNAME + ".ONBOARDING_COMPLETE", false);
        LastVersion = new IntegerSetting(CNAME + ".LAST_VERSION", 0);
        RankingsDate = new LongSetting(CNAME + ".RANKINGS_DATE", 0L);
        Region = new RegionSetting(CNAME + ".REGION_SETTING");
        SyncChargingIsNecessary = new BooleanSetting(CNAME + ".SYNC_CHARGING_NECESSARY", false);
        SyncIsEnabled = new BooleanSetting(CNAME + ".SYNC_ENABLED", true);
        SyncIsPending = new BooleanSetting(CNAME + ".SYNC_PENDING", false);
        SyncWifiIsNecessary = new BooleanSetting(CNAME + ".SYNC_WIFI_NECESSARY", true);
    }


    public static Editor edit() {
        return get().edit();
    }


    public static Editor edit(final String name) {
        return get(name).edit();
    }


    public static SharedPreferences get() {
        final Context context = App.getContext();
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    public static SharedPreferences get(final String name) {
        final Context context = App.getContext();
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }


}
