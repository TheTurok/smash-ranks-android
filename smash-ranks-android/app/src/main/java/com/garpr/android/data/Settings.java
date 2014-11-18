package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


public final class Settings {


    private static final String CNAME = Settings.class.getCanonicalName();
    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = Settings.class.getSimpleName();

    private static LinkedList<WeakReference<OnRegionChangedListener>> sRegionListeners;
    private static String sRegion;




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


    public static String getRegion() {
        if (!Utils.validStrings(sRegion)) {
            sRegion = get().getString(KEY_REGION, null);

            if (!Utils.validStrings(sRegion)) {
                setRegion(Constants.NORCAL, false);
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


    private static void setRegion(final String region, final boolean notify) {
        final Editor editor = edit();
        editor.putString(KEY_REGION, region);
        editor.apply();

        if (notify) {
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
        } else {
            Log.d(TAG, "Region is \"" + region + "\"");
        }

        sRegion = region;
    }


    public static void setRegion(final String region) {
        setRegion(region, true);
    }


}
