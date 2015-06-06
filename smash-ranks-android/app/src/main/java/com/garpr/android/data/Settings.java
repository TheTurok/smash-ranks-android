package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.garpr.android.App;


public final class Settings {


    private static final String CNAME = "com.garpr.android.data.Settings";

    public static final BooleanSetting OnboardingComplete;
    public static final IntegerSetting LastVersion;
    public static final LongSetting RankingsDate;
    public static final RegionSetting Region;




    static {
        OnboardingComplete = new BooleanSetting(CNAME + ".ONBOARDING_COMPLETE", false);
        LastVersion = new IntegerSetting(CNAME + ".LAST_VERSION", 0);
        RankingsDate = new LongSetting(CNAME + ".RANKINGS_DATE", 0L);
        Region = new RegionSetting(CNAME + ".REGION_SETTING");
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
