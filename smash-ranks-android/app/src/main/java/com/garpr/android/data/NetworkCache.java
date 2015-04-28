package com.garpr.android.data;


import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.garpr.android.misc.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;


public final class NetworkCache {


    private static final int MAX_SIZE = 32;
    private static final long TIMESTAMP_CUTOFF = 60L * 60L * 24L * 5L; // 5 days
    private static final String CNAME = "com.garpr.android.data.NetworkCache";
    private static final String JSON = CNAME + ".JSON";
    private static final String TIMESTAMPS = CNAME + ".TIMESTAMPS";

    private static final Comparator<String> TIMESTAMP_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(final String lhs, final String rhs) {
            final int lhsTime = (int) (Long.parseLong(lhs) / 1000L);
            final int rhsTime = (int) (Long.parseLong(rhs) / 1000L);
            return lhsTime - rhsTime;
        }
    };




    @SuppressWarnings("unchecked")
    private synchronized static void clean() {
        final SharedPreferences timestampsCache = Settings.get(TIMESTAMPS);
        final Map<String, String> timestamps = (Map<String, String>) timestampsCache.getAll();

        if (timestamps == null || timestamps.isEmpty()) {
            return;
        }

        final long now = System.currentTimeMillis() / 1000L;

        boolean editing = false;
        Editor timestampsCacheEditor = null;
        Editor jsonCacheEditor = null;

        for (final String timestamp : timestamps.keySet()) {
            final long then = Long.parseLong(timestamp);

            if (now - then >= TIMESTAMP_CUTOFF) {
                if (!editing) {
                    editing = true;
                    timestampsCacheEditor = timestampsCache.edit();
                    jsonCacheEditor = Settings.get(JSON).edit();
                }

                timestampsCacheEditor.remove(timestamp);
                final String url = timestamps.get(timestamp);
                jsonCacheEditor.remove(url);
            }
        }

        if (editing) {
            timestampsCacheEditor.apply();
            jsonCacheEditor.apply();
        }
    }


    public synchronized static JSONObject get(final String url) {
        clean();

        final SharedPreferences jsonCache = Settings.get(JSON);
        final String jsonString = jsonCache.getString(url, null);
        final JSONObject json;

        if (Utils.validStrings(jsonString)) {
            try {
                json = new JSONObject(jsonString);
            } catch (final JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            json = null;
        }

        return json;
    }


    private static SharedPreferences getJsonCache() {
        return Settings.get(JSON);
    }


    private static SharedPreferences getTimestampsCache() {
        return Settings.get(TIMESTAMPS);
    }


    @SuppressWarnings("unchecked")
    public synchronized static void set(final String url, final JSONObject json) {
        final SharedPreferences timestampsCache = getTimestampsCache();
        final Map<String, String> timestamps = (Map<String, String>) timestampsCache.getAll();
        Editor timestampsCacheEditor = null;

        if (timestamps != null && timestamps.size() >= MAX_SIZE) {
            timestampsCacheEditor = Settings.edit(TIMESTAMPS);
            final LinkedList<String> timestampsList = new LinkedList<>(timestamps.keySet());
            Collections.sort(timestampsList, TIMESTAMP_COMPARATOR);

            do {
                final String timestamp = timestampsList.removeLast();
                final String oldUrl = timestamps.get(timestamp);
                timestampsCacheEditor.remove(timestamp);
                
            } while (timestampsList.size() >= MAX_SIZE);
        }

        // TODO
        // trim to max size - 1

        // TODO
        // search for a key with the value of url in timestamps (it needs to be removed if it exists)

        final String timestamp = String.valueOf(System.currentTimeMillis());
        timestampsCacheEditor.putString(timestamp, url);
        timestampsCacheEditor.apply();

        final Editor jsonCacheEditor = Settings.edit(JSON);
        jsonCacheEditor.putString(url, json.toString());
        jsonCacheEditor.apply();
    }


}
