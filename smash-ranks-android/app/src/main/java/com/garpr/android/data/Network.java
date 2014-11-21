package com.garpr.android.data;


import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.App;
import com.garpr.android.misc.Heartbeat;


final class Network {


    private static final String BASE_URL = "http://api.garpr.com";
    private static final String TAG = Network.class.getSimpleName();




    static String makeUrl(final String suffix) {
        return BASE_URL + '/' + Settings.getRegion().getName() + '/' + suffix;
    }


    static String makeRegionFreeUrl(final String suffix) {
        return BASE_URL + '/' + suffix;
    }


    @SuppressWarnings("unchecked")
    static void sendRequest(final String url, final Callback callback) {
        final Heartbeat heartbeat = callback.getHeartbeat();

        if (heartbeat == null || !heartbeat.isAlive()) {
            Log.d(TAG, "API call to " + url + " was canceled");
        } else {
            Log.d(TAG, "Making API call to " + url);

            final RequestQueue requestQueue = App.getRequestQueue();
            final JsonObjectRequest request = new JsonObjectRequest(url, null, callback, callback);
            request.setTag(heartbeat);
            requestQueue.add(request);
        }
    }


}
