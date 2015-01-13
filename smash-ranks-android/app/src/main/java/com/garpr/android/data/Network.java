package com.garpr.android.data;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.App;
import com.garpr.android.misc.Console;
import com.garpr.android.misc.Constants;
import com.garpr.android.misc.Heartbeat;


final class Network {


    private static final String TAG = "Network";




    static String makeUrl(final String suffix) {
        return Constants.API_URL + '/' + Settings.getRegion().getId() + '/' + suffix;
    }


    static String makeRegionFreeUrl(final String suffix) {
        return Constants.API_URL + '/' + suffix;
    }


    @SuppressWarnings("unchecked")
    static void sendRequest(final String url, final Callback callback) {
        final Heartbeat heartbeat = callback.getHeartbeat();

        if (heartbeat == null || !heartbeat.isAlive()) {
            Console.d(TAG, "API call to " + url + " was canceled");
        } else {
            Console.d(TAG, "Making API call to " + url);

            final RequestQueue requestQueue = App.getRequestQueue();
            final JsonObjectRequest request = new JsonObjectRequest(url, null, callback, callback);
            request.setTag(heartbeat);
            requestQueue.add(request);
        }
    }


}
