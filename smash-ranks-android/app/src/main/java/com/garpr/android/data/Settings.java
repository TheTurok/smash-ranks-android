package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.garpr.android.App;
import com.garpr.android.misc.Constants;


public final class Settings {


    private static final String KEY_REGION = "KEY_REGION";
    private static final String NAME = Settings.class.getCanonicalName();

    private static String sRegion;




    public static Editor editPreferences() {
        return editPreferences(NAME);
    }


    public static Editor editPreferences(final String name) {
        return getPreferences(name).edit();
    }


    public static SharedPreferences getPreferences() {
        return getPreferences(NAME);
    }


    public static SharedPreferences getPreferences(final String name) {
        return App.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }


    public static String getRegion() {
        if (TextUtils.isEmpty(sRegion)) {
            sRegion = getPreferences().getString(KEY_REGION, null);

            if (TextUtils.isEmpty(sRegion)) {
                setRegion(Constants.NORCAL);
            }
        }

        return sRegion;
    }


    public static void setRegion(final String region) {
        sRegion = region;

        final Editor editor = editPreferences();
        editor.putString(KEY_REGION, sRegion);
        editor.apply();
    }


}
