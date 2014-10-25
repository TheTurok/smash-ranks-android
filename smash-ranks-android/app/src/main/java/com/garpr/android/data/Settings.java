package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;


public final class Settings {


    private static final String CNAME = Settings.class.getCanonicalName();
    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = Settings.class.getSimpleName();

    private static String sRegion;




    public static Editor edit() {
        return edit(CNAME);
    }


    public static Editor edit(final String name) {
        return get(name).edit();
    }


    public static SharedPreferences get() {
        return get(CNAME);
    }


    public static SharedPreferences get(final String name) {
        return App.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }


    public static String getRegion() {
        if (!Utils.validStrings(sRegion)) {
            sRegion = get().getString(KEY_REGION, null);

            if (!Utils.validStrings(sRegion)) {
                setRegion(Constants.NORCAL);
            }
        }

        return sRegion;
    }


    public static void setRegion(final String region) {
        Log.d(TAG, "Region changed from \"" + sRegion + "\" to \"" + region + "\"");
        sRegion = region;
        Database.onRegionChanged();
        final Editor editor = edit();
        editor.putString(KEY_REGION, sRegion);
        editor.apply();
    }


}
