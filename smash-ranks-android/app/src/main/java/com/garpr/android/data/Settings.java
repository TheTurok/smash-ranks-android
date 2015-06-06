package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.garpr.android.App;


public final class Settings {


    public static final IntegerSetting LastVersion;
    public static final RegionSetting Region;




    static {
        LastVersion = new IntegerSetting("KEY_LAST_VERSION", 0);
        Region = new RegionSetting("KEY_REGION_SETTING");
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
