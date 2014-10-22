package com.garpr.android.data;


import android.util.Log;

import com.android.volley.VolleyError;
import com.garpr.android.R;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public final class Regions {


    private static final String TAG = Regions.class.getSimpleName();




    public static void get(final RegionsCallback callback) {
        final String url = Network.makeRegionFreeUrl(Constants.REGIONS);
        Network.sendRequest(url, callback);
    }


    private static void getFromJSON(final RegionsCallback callback) {
        if (callback.isAlive()) {
            Log.d(TAG, "Grabbing regions from JSON");
            final AsyncReadRegionsFile task = new AsyncReadRegionsFile(callback);
            task.execute();
        } else {
            Log.d(TAG, "Canceled grabbing regions from JSON");
        }
    }


    private static ArrayList<String> parseJSON(final JSONObject json) throws JSONException {
        final JSONArray regionsJSON = json.getJSONArray(Constants.REGIONS);
        final int regionsLength = regionsJSON.length();
        final ArrayList<String> regions = new ArrayList<String>(regionsLength);

        for (int i = 0; i < regionsLength; ++i) {
            try {
                final String region = regionsJSON.getString(i);
                regions.add(region);
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when grabbing region at index " + i, e);
            }
        }

        regions.trimToSize();
        return regions;
    }




    private static class AsyncReadRegionsFile extends AsyncReadFile<String> {


        private static final String TAG = AsyncReadRegionsFile.class.getSimpleName();


        private AsyncReadRegionsFile(final RegionsCallback callback) {
            super(callback);
        }


        @Override
        int getRawResourceId() {
            return R.raw.regions;
        }


        @Override
        ArrayList<String> parseJSON(final JSONObject json) throws JSONException {
            final ArrayList<String> regions = Regions.parseJSON(json);
            Log.d(TAG, "Read in " + regions.size() + " regions from the JSON file");

            return regions;
        }


    }


    public static abstract class RegionsCallback extends Callback<String> {


        private static final String TAG = RegionsCallback.class.getSimpleName();


        public RegionsCallback(final Heartbeat heartbeat) {
            super(heartbeat);
        }


        @Override
        public final void onErrorResponse(final VolleyError error) {
            Log.e(TAG, "Exception when downloading regions", error);
            getFromJSON(this);
        }


        @Override
        public final void onResponse(final JSONObject json) {
            try {
                final ArrayList<String> regions = Regions.parseJSON(json);
                Log.d(TAG, "Read in " + regions.size() + " regions from JSON response");

                if (regions.isEmpty()) {
                    getFromJSON(this);
                } else {
                    if (isAlive()) {
                        response(regions);
                    } else {
                        Log.d(TAG, "Regions response canceled because the listener is dead");
                    }
                }
            } catch (final JSONException e) {
                Log.e(TAG, "Exception when parsing regions JSON response", e);
                getFromJSON(this);
            }
        }


        @Override
        public final void response(final String item) {
            final ArrayList<String> list = new ArrayList<String>(1);
            list.add(item);
            response(list);
        }


    }


}
