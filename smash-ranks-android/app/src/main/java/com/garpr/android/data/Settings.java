package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.garpr.android.App;


public final class Settings {


    private static final String CNAME = "com.garpr.android.data";

    public static final IntegerSetting LastVersion;
    public static final RegionSetting Region;
    public static final com.garpr.android.data.User User;




    static {
        LastVersion = new IntegerSetting(CNAME + ".LAST_VERSION", 0);
        Region = new RegionSetting(CNAME + ".REGION_SETTING");
        User = new User(CNAME + ".USER");
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
