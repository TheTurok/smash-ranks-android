package com.garpr.android.data;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.garpr.android.App;
import com.garpr.android.misc.Heartbeat;


final class Network {


    private static final String BASE_URL = "http://www.garpr.com:5100";




    static String makeUrl(final String suffix) {
        return BASE_URL + '/' + App.getRegion() + '/' + suffix;
    }


    @SuppressWarnings("unchecked")
    static void sendRequest(final String url, final Callback callback) {
        final Heartbeat heartbeat = callback.getHeartbeat();

        if (heartbeat == null || !heartbeat.isAlive()) {
            return;
        }

        final RequestQueue requestQueue = App.getRequestQueue();
        final JsonObjectRequest request = new JsonObjectRequest(url, null, callback, callback);
        request.setTag(heartbeat);
        requestQueue.add(request);
    }


}
