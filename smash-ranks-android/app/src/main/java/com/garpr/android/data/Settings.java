package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.misc.OnRegionChangedListener;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


public final class Settings {


    private static final String CNAME = Settings.class.getCanonicalName();
    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = Settings.class.getSimpleName();

    private static LinkedList<WeakReference<OnRegionChangedListener>> sRegionListeners;
    private static Region sRegion;




    public static void addRegionListener(final OnRegionChangedListener listener) {
        final WeakReference<OnRegionChangedListener> reference = new WeakReference<OnRegionChangedListener>(listener);

        if (sRegionListeners == null) {
            sRegionListeners = new LinkedList<WeakReference<OnRegionChangedListener>>();
        }

        sRegionListeners.add(reference);
    }


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


    public static Region getRegion() {
        if (sRegion == null) {
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


    public static void removeRegionListener(final OnRegionChangedListener listener) {
        if (sRegionListeners == null || sRegionListeners.isEmpty()) {
            Log.d(TAG, "Went to remove an " + OnRegionChangedListener.class.getSimpleName() +
                    " but there aren't any");
            return;
        }

        for (int i = 0; i < sRegionListeners.size(); ++i) {
            final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
            final OnRegionChangedListener l = r.get();

            if (l == null || l == listener) {
                Log.d(TAG, "Removing " + OnRegionChangedListener.class.getSimpleName() +
                        " at index " + i);
                sRegionListeners.remove(i);
            } else {
                ++i;
            }
        }
    }


    public static void setRegion(final Region region) {
        final JSONObject regionJSON = region.toJSON();
        final String regionString = regionJSON.toString();

        final Editor editor = edit();
        editor.putString(KEY_REGION, regionString);
        editor.apply();

        Log.d(TAG, "Region changed from \"" + sRegion + "\" to \"" + region + "\"");

        if (sRegionListeners == null || sRegionListeners.isEmpty()) {
            Log.d(TAG, "There are no region listeners");
            return;
        }

        for (int i = 0; i < sRegionListeners.size(); ) {
            final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
            final OnRegionChangedListener l = r.get();

            if (l == null) {
                Log.d(TAG, "Dead " + OnRegionChangedListener.class.getSimpleName() +
                        " listener found at index " + i);
                sRegionListeners.remove(i);
            } else {
                l.onRegionChanged(region);
                ++i;
            }
        }

        sRegion = region;
    }


}
