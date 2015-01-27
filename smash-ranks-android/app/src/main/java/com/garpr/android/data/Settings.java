package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.garpr.android.App;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.LinkedList;


public final class Settings {


    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = "Settings";

    private static final LinkedList<WeakReference<OnRegionChangedListener>> REGION_LISTENERS;
    private static final Object REGION_LOCK;
    private static Region sRegion;




    static {
        REGION_LISTENERS = new LinkedList<>();
        REGION_LOCK = new Object();
    }


    public static void attachRegionListener(final OnRegionChangedListener listener) {
        synchronized (REGION_LISTENERS) {
            boolean addListener = true;

            if (!REGION_LISTENERS.isEmpty()) {
                int i = 0;

                do {
                    final WeakReference<OnRegionChangedListener> r = REGION_LISTENERS.get(i);
                    final OnRegionChangedListener l = r.get();

                    if (l == null) {
                        Console.d(TAG, "Removing dead RegionListener at index " + i);
                        REGION_LISTENERS.remove(i);
                    } else if (l == listener) {
                        Console.d(TAG, "Won't add RegionListener, it already exists at index " + i);
                        addListener = false;
                    } else {
                        ++i;
                    }
                } while (i < REGION_LISTENERS.size());
            }

            if (addListener) {
                REGION_LISTENERS.add(new WeakReference<>(listener));
                Console.d(TAG, "Added RegionListener (" + listener.toString() + "), there are now "
                        + REGION_LISTENERS.size() + " listener(s)");
            }
        }
    }


    public static void detachRegionListener(final OnRegionChangedListener listener) {
        synchronized (REGION_LISTENERS) {
            if (REGION_LISTENERS.isEmpty()) {
                Console.d(TAG, "Went to remove a RegionListener but there are none");
            } else {
                for (int i = 0; i < REGION_LISTENERS.size(); ) {
                    final WeakReference<OnRegionChangedListener> r = REGION_LISTENERS.get(i);
                    final OnRegionChangedListener l = r.get();

                    if (l == null) {
                        Console.d(TAG, "Removing dead RegionListener at index " + i +
                                ". There are now " + REGION_LISTENERS.size() + " listener(s)");
                        REGION_LISTENERS.remove(i);
                    } else if (l == listener) {
                        Console.d(TAG, "Removing RegionListener (" + listener.toString() +
                                ") at index " + i + ". There are now " + REGION_LISTENERS.size()
                                + " listener(s)");
                        REGION_LISTENERS.remove(i);
                    } else {
                        ++i;
                    }
                }
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
        synchronized (REGION_LOCK) {
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
        synchronized (REGION_LISTENERS) {
            if (REGION_LISTENERS.isEmpty()) {
                Console.w(TAG, "Region was changed but there are no listeners");
            } else {
                for (int i = 0; i < REGION_LISTENERS.size(); ) {
                    final WeakReference<OnRegionChangedListener> r = REGION_LISTENERS.get(i);
                    final OnRegionChangedListener l = r.get();

                    if (l == null) {
                        REGION_LISTENERS.remove(i);
                        Console.d(TAG, "Removing dead RegionListener at index " + i +
                                ". There are now " + REGION_LISTENERS.size() + " listener(s)");
                    } else {
                        l.onRegionChanged(sRegion);
                        ++i;
                    }
                }
            }
        }
    }


    private static void saveRegion(final Region region) {
        Console.d(TAG, "Region is being changed from " + sRegion + " to " + region);

        final JSONObject regionJSON = region.toJSON();
        final String regionString = regionJSON.toString();

        Crashlytics.setString(Constants.NEW_REGION, regionString);

        if (sRegion == null) {
            Crashlytics.setString(Constants.OLD_REGION, Constants.NULL);
        } else {
            final JSONObject sRegionJSON = sRegion.toJSON();
            final String sRegionString = sRegionJSON.toString();
            Crashlytics.setString(Constants.OLD_REGION, sRegionString);
        }

        sRegion = region;

        final Editor editor = edit();
        editor.putString(KEY_REGION, regionString);
        editor.apply();
    }


    static void setRegion(final Region region, final boolean notifyListeners) {
        if (region.equals(sRegion)) {
            return;
        }

        synchronized (REGION_LOCK) {
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
