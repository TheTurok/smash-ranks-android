package com.garpr.android.data;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.garpr.android.App;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Utils;
import com.garpr.android.models.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;


public final class Settings {


    private static final String CNAME = Settings.class.getCanonicalName();
    private static final String KEY_LAST_ROSTER_UPDATE = "KEY_LAST_ROSTER_UPDATE";
    private static final String KEY_REGION = "KEY_REGION";
    private static final String TAG = Settings.class.getSimpleName();

    private static final LinkedList<WeakReference<OnRegionChangedListener>> sRegionListeners;
    private static final SimpleDateFormat sDateFormat;
    private static Region sRegion;




    static {
        sRegionListeners = new LinkedList<>();
        sDateFormat = new SimpleDateFormat(Constants.ROSTER_DATE_FORMAT);
    }


    public static synchronized void addRegionListener(final OnRegionChangedListener listener) {
        if (!sRegionListeners.isEmpty()) {
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

        final WeakReference<OnRegionChangedListener> reference = new WeakReference<>(listener);

        if (sRegionListeners.contains(reference)) {
            Log.d(TAG, "Went to add a RegionListener but it already exists at index "
                    + sRegionListeners.indexOf(reference) + ". There are "
                    + sRegionListeners.size() + " listener(s).");
        } else {
            sRegionListeners.add(reference);
            Log.d(TAG, "Added RegionListener at index " + sRegionListeners.indexOf(reference)
                    + ". There are now " + sRegionListeners.size() + " listener(s).");
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
        final Context context = App.getContext();
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }


    public static long getMostRecentRosterUpdate() {
        return get().getLong(KEY_LAST_ROSTER_UPDATE, 0L);
    }


    public static synchronized Region getRegion() {
        if (sRegion == null) {
            Log.d(TAG, "Region is null, going to try reading it in from SharedPreferences");
            final String regionString = get().getString(KEY_REGION, null);

            if (Utils.validStrings(regionString)) {
                Log.d(TAG, "Read in region from SharedPreferences: " + regionString);

                try {
                    final JSONObject regionJSON = new JSONObject(regionString);
                    sRegion = new Region(regionJSON);
                } catch (final JSONException e) {
                    // this should never happen
                    throw new RuntimeException(e);
                }
            } else {
                Log.d(TAG, "Region doesn't exist in SharedPreferences");
                sRegion = User.getRegion();
                saveRegion(sRegion);
            }
        }

        return sRegion;
    }


    private static synchronized void notifyRegionListeners() {
        if (sRegionListeners.isEmpty()) {
            Log.d(TAG, "Region was changed but there are no listeners");
        } else {
            for (int i = 0; i < sRegionListeners.size(); ) {
                final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
                final OnRegionChangedListener l = r.get();

                if (l == null) {
                    sRegionListeners.remove(i);
                    Log.d(TAG, "Removing dead RegionListener at index " + i + ". There are now "
                            + sRegionListeners.size() + " listener(s).");
                } else {
                    l.onRegionChanged(sRegion);
                    ++i;
                }
            }
        }
    }


    public static synchronized void removeRegionListener(final OnRegionChangedListener listener) {
        if (sRegionListeners.isEmpty()) {
            Log.d(TAG, "Went to remove a RegionListener but there are none");
        } else {
            for (int i = 0; i < sRegionListeners.size(); ) {
                final WeakReference<OnRegionChangedListener> r = sRegionListeners.get(i);
                final OnRegionChangedListener l = r.get();

                if (l == null) {
                    Log.d(TAG, "Removing dead RegionListener at index " + i + ". There are now "
                            + sRegionListeners.size() + " listener(s).");
                    sRegionListeners.remove(i);
                } else if (l == listener) {
                    Log.d(TAG, "Removing RegionListener at index " + i + ". There are now " +
                            sRegionListeners.size() + " listener(s).");
                    sRegionListeners.remove(i);
                } else {
                    ++i;
                }
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


    public static void setMostRecentRosterUpdate(final String dateString) {
        try {
            final Date date = sDateFormat.parse(dateString);
            final long time = date.getTime();
            edit().putLong(KEY_LAST_ROSTER_UPDATE, time).apply();
        } catch (final ParseException e) {
            Log.e(TAG, "Couldn't parse the date: \"" + dateString + "\"", e);
            throw new RuntimeException(e);
        }
    }


    static void setRegion(final Region region, final boolean notifyListeners) {
        if (region.equals(sRegion)) {
            return;
        }

        saveRegion(region);

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
