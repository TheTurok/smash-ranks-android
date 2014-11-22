package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.misc.Utils;
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
        if (sRegionListeners == null) {
            sRegionListeners = new LinkedList<WeakReference<OnRegionChangedListener>>();
        } else {
            for (int i = 0; i < sRegionListeners.size(); ) {
                final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
                final OnRegionChangedListener l = r.get();

                if (l == null) {
                    Log.d(TAG, "Removing dead RegionListener at index " + i);
                    sRegionListeners.remove(i);
                } else {
                    ++i;
                }
            }
        }

        final WeakReference<OnRegionChangedListener> reference = new WeakReference<OnRegionChangedListener>(listener);

        if (!sRegionListeners.contains(reference)) {
            sRegionListeners.add(reference);
        }
    }


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

            if (Utils.validStrings(regionString)) {
                try {
                    final JSONObject regionJSON = new JSONObject(regionString);
                    sRegion = new Region(regionJSON);
                } catch (final JSONException e) {
                    // this should never happen
                    throw new RuntimeException(e);
                }
            } else {
                sRegion = User.getRegion();
                saveRegion(sRegion);
            }
        }

        return sRegion;
    }


    public static void removeRegionListener(final OnRegionChangedListener listener) {
        if (sRegionListeners == null || sRegionListeners.isEmpty()) {
            return;
        }

        for (int i = 0; i < sRegionListeners.size(); ) {
            final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
            final OnRegionChangedListener l = r.get();

            if (l == null) {
                sRegionListeners.remove(i);
            } else if (l == listener) {
                Log.d(TAG, "Removing dead RegionListener at index " + i);
                sRegionListeners.remove(i);
            } else {
                ++i;
            }
        }
    }


    private static void saveRegion(final Region region) {
        Log.d(TAG, "Region is being changed from " + sRegion + " to " + region);

        sRegion = region;
        final JSONObject regionJSON = sRegion.toJSON();
        final String regionString = regionJSON.toString();

        final Editor editor = edit();
        editor.putString(KEY_REGION, regionString);
        editor.apply();
    }


    public static void setRegion(final Region region) {
        if (!region.equals(sRegion)) {
            saveRegion(region);

            if (sRegionListeners == null || sRegionListeners.isEmpty()) {
                Log.d(TAG, "Region was changed but there are no listeners");
                return;
            }

            for (int i = 0; i < sRegionListeners.size(); ) {
                final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
                final OnRegionChangedListener l = r.get();

                if (l == null) {
                    sRegionListeners.remove(i);
                } else {
                    l.onRegionChanged(sRegion);
                    ++i;
                }
            }
        }
    }




    public interface OnRegionChangedListener {


        public void onRegionChanged(final Region region);


    }


}
