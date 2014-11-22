package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;


public final class Settings {


    private static final String CNAME = Settings.class.getCanonicalName();
    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = Settings.class.getSimpleName();

    private static Region sRegion;




    private static Editor edit() {
        return edit(CNAME);
    }


    public static Editor edit(final String name) {
        return get(name).edit();
    }


    private static SharedPreferences get() {
        return get(CNAME);
    }


    public static SharedPreferences get(final String name) {
        return App.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    }


    public static Region getRegion() {
        if (sRegion == null) {
            Log.d(TAG, "Region is null, reading it in from JSON");
            final String regionString = get().getString(KEY_REGION, null);

            try {
                final JSONObject regionJSON = new JSONObject(regionString);
                sRegion = new Region(regionJSON);
            } catch (final JSONException e) {
                // this should never happen
                throw new RuntimeException(e);
            }
        }

        return sRegion;
    }


    public static void setRegion(final Region region) {
        if (!region.equals(sRegion)) {
            Log.d(TAG, "Region is being changed from " + sRegion + " to " + region);

            sRegion = region;
            final JSONObject regionJSON = sRegion.toJSON();
            final String regionString = regionJSON.toString();

            final Editor editor = edit();
            editor.putString(KEY_REGION, regionString);
            editor.apply();
        }
    }


}
