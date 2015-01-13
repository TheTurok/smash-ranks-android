package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.garpr.android.App;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


public final class Settings {


    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = "Settings";

    private static final LinkedList<WeakReference<OnRegionChangedListener>> sRegionListeners;
    private static final Object sRegionLock;
    private static Region sRegion;




    static {
        sRegionListeners = new LinkedList<>();
        sRegionLock = new Object();
    }


    public static void addRegionListener(final OnRegionChangedListener listener) {
        synchronized (sRegionListeners) {
            if (!sRegionListeners.isEmpty()) {
                for (int i = 0; i < sRegionListeners.size(); ) {
                    final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
                    final OnRegionChangedListener l = r.get();

                    if (l == null) {
                        Console.d(TAG, "Removing dead RegionListener at index " + i);
                        sRegionListeners.remove(i);
                    } else {
                        ++i;
                    }
                }
            }

            final WeakReference<OnRegionChangedListener> reference = new WeakReference<>(listener);
            int indexOf = sRegionListeners.indexOf(reference);

            if (indexOf == -1) {
                sRegionListeners.add(reference);
                indexOf = sRegionListeners.indexOf(reference);
                Console.d(TAG, "Added RegionListener at index " + indexOf + ". There are now " +
                        + sRegionListeners.size() + " listener(s).");
            } else {
                Console.d(TAG, "Went to add a RegionListener but it already exists at index "
                        + indexOf + ". There are " + sRegionListeners.size() + " listener(s).");
            }
        }
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


    public static Region getRegion() {
        synchronized (sRegionLock) {
            if (sRegion == null) {
                Console.d(TAG, "Region is null, going to try reading it in from SharedPreferences");
                final String regionString = get().getString(KEY_REGION, null);

                if (Utils.validStrings(regionString)) {
                    Console.d(TAG, "Read in region from SharedPreferences: " + regionString);

                    try {
                        final JSONObject regionJSON = new JSONObject(regionString);
                        sRegion = new Region(regionJSON);
                    } catch (final JSONException e) {
                        // this should never happen
                        throw new RuntimeException(e);
                    }
                } else {
                    Console.d(TAG, "Region doesn't exist in SharedPreferences");
                    saveRegion(User.getRegion());
                }
            }
        }

        return sRegion;
    }


    private static void notifyRegionListeners() {
        synchronized (sRegionListeners) {
            if (sRegionListeners.isEmpty()) {
                Console.d(TAG, "Region was changed but there are no listeners");
            } else {
                for (int i = 0; i < sRegionListeners.size(); ) {
                    final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
                    final OnRegionChangedListener l = r.get();

                    if (l == null) {
                        sRegionListeners.remove(i);
                        Console.d(TAG, "Removing dead RegionListener at index " + i +
                                ". There are now " + sRegionListeners.size() + " listener(s).");
                    } else {
                        l.onRegionChanged(sRegion);
                        ++i;
                    }
                }
            }
        }
    }


    public static void removeRegionListener(final OnRegionChangedListener listener) {
        synchronized (sRegionListeners) {
            if (sRegionListeners.isEmpty()) {
                Console.d(TAG, "Went to remove a RegionListener but there are none");
            } else {
                for (int i = 0; i < sRegionListeners.size(); ) {
                    final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
                    final OnRegionChangedListener l = r.get();

                    if (l == null) {
                        Console.d(TAG, "Removing dead RegionListener at index " + i +
                                ". There are now " + sRegionListeners.size() + " listener(s).");
                        sRegionListeners.remove(i);
                    } else if (l == listener) {
                        Console.d(TAG, "Removing RegionListener at index " + i + ". There are now "
                                + sRegionListeners.size() + " listener(s).");
                        sRegionListeners.remove(i);
                    } else {
                        ++i;
                    }
                }
            }
        }
    }


    private static void saveRegion(final Region region) {
        Console.d(TAG, "Region is being changed from " + sRegion + " to " + region);

        sRegion = region;
        final JSONObject regionJSON = sRegion.toJSON();
        final String regionString = regionJSON.toString();

        final Editor editor = edit();
        editor.putString(KEY_REGION, regionString);
        editor.apply();
    }


    static void setRegion(final Region region, final boolean notifyListeners) {
        if (region.equals(sRegion)) {
            return;
        }

        synchronized (sRegionLock) {
            saveRegion(region);
        }

        if (notifyListeners) {
            notifyRegionListeners();
        }
    }


    public static void setRegion(final Region region) {
        setRegion(region, true);
    }




    public interface OnRegionChangedListener {


        public void onRegionChanged(final Region region);


    }


}
